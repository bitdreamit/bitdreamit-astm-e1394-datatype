# bitdreamit-astm-e1394-datatype

ASTM E1394-91/97 record-level data type plugin for Mirth Connect / BridgeLink.

This plugin adds a fully-featured **ASTM E1394 Data Type** to Mirth Connect 4.x,
enabling Mirth to accept, parse, and transform messages following the
ASTM E1394 standard. As with other Mirth data types, an incoming message is
serialized into a simple XML format, after which all the usual transformer
steps can be used to transform the message or convert it to / from another
data type.

## Repository layout

```
bitdreamit-astm-e1394-datatype/
├── README.md
├── PRODUCTION_NOTES.md
├── LICENSE                       (MIT)
├── shared/
│   └── src/com/bitdreamit/connect/plugins/datatypes/astm/shared/
│       └── ASTME1394Constants.java
├── server/
│   ├── resources/plugin.xml
│   └── src/com/bitdreamit/connect/plugins/datatypes/astm/server/
│       ├── ASTME1394DataTypeServerPlugin.java
│       ├── ASTME1394DataTypeDelegate.java
│       ├── ASTME1394DataTypeProperties.java
│       ├── ASTME1394Serializer.java            ← IMessageSerializer entry point
│       ├── ASTME1394Deserializer.java          ← ASTM text → XML
│       ├── ASTME1394FromXmlConverter.java       ← XML → ASTM text
│       ├── ASTME1394EscapeUtil.java             ← &F / &S / &R / &E / &Hxx handling
│       ├── ASTME1394SerializationProperties.java
│       ├── ASTME1394DeserializationProperties.java
│       ├── ASTME1394BatchProperties.java
│       ├── ASTME1394BatchAdaptor.java           ← H..L batch splitting
│       ├── ASTME1394BatchAdaptorFactory.java
│       ├── ASTME1394BatchStreamReader.java
│       ├── ASTME1394AutoResponder.java          ← E1381-framed ACK generation
│       ├── ASTME1394ResponseValidator.java
│       ├── ASTME1394ResponseGenerationProperties.java
│       └── ASTME1394ResponseValidationProperties.java
├── client/
│   ├── resources/plugin.xml
│   └── src/com/bitdreamit/connect/plugins/datatypes/astm/client/
│       ├── ASTME1394DataTypeClientPlugin.java
│       └── ASTME1394DataTypeSettingsPanel.java  ← Swing UI for Administrator
├── test/
│   └── src/com/bitdreamit/connect/plugins/datatypes/astm/test/
│       └── ASTME1394RoundTripTest.java
└── distribution/
    └── build.sh                                  ← production build script
```

## IntelliJ IDEA setup

1. Copy Mirth jars into a sibling `mirth-libs/` folder so the IDE can resolve
   the Mirth Connect APIs at compile time:

   ```
   ../mirth-libs/server/mirth-server.jar
   ../mirth-libs/server/donkey-server.jar
   ../mirth-libs/server/mirth-core.jar
   ../mirth-libs/client/mirth-client.jar
   ../mirth-libs/client/mirth-core.jar
   ../mirth-libs/test/junit-4.13.2.jar
   ../mirth-libs/test/hamcrest-core-1.3.jar
   ```

2. Open this folder in IntelliJ IDEA (`File → Open`).

3. The four modules `shared`, `server`, `client`, `test` load automatically
   from their respective `.iml` files.

4. `Build → Build Project` (Ctrl + F9).

## Build & deploy

The build script is hardened for production use:

```bash
cd distribution
chmod +x build.sh
./build.sh
```

By default the script:

1. Validates that every required Mirth jar is present under `../mirth-libs/`.
2. Compiles `shared` → `server` → `client` → `test`.
3. Runs `ASTME1394RoundTripTest` and fails the build if any test fails.
4. Produces two signed-style JARs with manifest metadata in `out/`:

   * `bitdreamit-astm-e1394-datatype-server.jar`
   * `bitdreamit-astm-e1394-datatype-client.jar`

Environment overrides:

| Variable       | Purpose                                           |
|----------------|---------------------------------------------------|
| `MIRTH_LIBS`   | Override the `../mirth-libs` base path.          |
| `JAVAC_OPTS`   | Pass additional `javac` flags (e.g. `-Xlint`).    |
| `SKIP_TESTS=1` | Skip test compilation and execution.              |

Install into Mirth Connect:

```bash
mkdir -p $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype
cp out/bitdreamit-astm-e1394-datatype-server.jar $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/
cp out/bitdreamit-astm-e1394-datatype-client.jar $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/
cp server/resources/plugin.xml $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/plugin.xml.server
cp client/resources/plugin.xml $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/plugin.xml.client
# Restart Mirth Connect service.
```

## Canonical XML format

The serializer produces a flat XML tree matching the Mirth HL7v2.x style.
Element names use letter prefixes (`F` for field, `C` for component, `R` for
repeat) because XML element names cannot start with a digit:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ASTM>
  <H>
    <F1>H</F1>
    <F2>\^&amp;</F2>
    <F3></F3>
    <F4></F4>
    <F5><C1>BitDreamLIS</C1><C2>1.0</C2></F5>
    ...
  </H>
  <P>
    <F1>P</F1>
    <F2>1</F2>
    ...
  </P>
  ...
  <L>
    <F1>L</F1>
    <F2>1</F2>
    <F3>N</F3>
  </L>
</ASTM>
```

The first field of every record (`<F1>`) holds the record-type letter
itself (`H`, `P`, `O`, `R`, `Q`, `C`, `M`, `L`). The serializer reconstructs
the record-type letter from the parent element name and skips `<F1>`'s
content during outbound serialization, so the format round-trips
byte-for-byte through `fromXML(toXML(message))`.

Field 2 of the header record (`<H><F2>…</F2>`) holds the delimiter
definition (e.g. `\^&`) and is preserved verbatim — neither split into
components nor escaped — because its character values ARE the delimiters
that the parser uses.

## Features

- **Full record coverage**: H / P / O / R / Q / C / M / L record parsing.
- **Canonical XML**: flat tree matching Mirth HL7v2.x conventions.
- **Complete escape handling**: `&F` (field), `&S` (component), `&R`
  (repeat), `&E` (escape char), `&Hxx` (hex byte).
- **Header-delimiter derivation**: extracts delimiters from the `H` record's
  field-2 delimiter definition when enabled.
- **Batch splitting**: configurable `H..L` boundary, per-record, or no-split
  modes.
- **Strict vs. lenient validation** mode.
- **DoS protection**: configurable `maxMessageSize` hard limit on inbound
  messages (default 256 KB).
- **Control-char stripping**: removes E1381 framing characters (STX / ETX /
  ETB / EOT / ENQ) that leak through to the data-type layer.
- **Line-break normalization**: tolerates CRLF / CR / LF inputs.
- **Configurable auto-responder**: emits ASTM E1381-framed ACKs with LRC,
  optional sequence numbering, and timestamps.
- **Response validation**: enforces positive ACK, frame-structure, and retry
  semantics.
- **Swing settings panel** in the Administrator UI.
- **Round-trip serialization**: ASTM text → XML → ASTM text.

## Test

Run `ASTME1394RoundTripTest` via the IntelliJ JUnit runner or:

```bash
cd test
javac -cp ../out/shared:../out/server:$JUNIT_JAR:../out/../../mirth-libs/test/junit-4.13.2.jar \
    src/com/bitdreamit/connect/plugins/datatypes/astm/test/*.java
java -cp ../out/shared:../out/server:../out/test:$JUNIT_JAR \
    org.junit.runner.JUnitCore \
    com.bitdreamit.connect.plugins.datatypes.astm.test.ASTME1394RoundTripTest
```

The build script also runs the tests automatically unless `SKIP_TESTS=1` is
set.

## Compatibility

- **Mirth Connect**: 4.0.0 and newer (tested against 4.4.x and 4.5.x).
- **JDK**: 8+ (Mirth 4.x baseline).
- **ASTM standard**: E1394-91 and E1394-97 (both are supported — they share
  the same record structure and delimiter conventions).
- **ASTM E1381**: pairs with the dedicated ASTM E1381 transmission-mode
  plugin when used over TCP / serial links. The data-type layer is
  transport-agnostic.

## License

MIT — see `LICENSE`.

## See also

- `PRODUCTION_NOTES.md` — detailed changelog of v1.0.0 → v1.1.0 production fixes.
