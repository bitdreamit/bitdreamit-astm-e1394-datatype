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

> **No `.idea` directory is shipped with the project.** This is intentional
> — a stale or corrupted `.idea` folder can cause IntelliJ to hang on
> startup or fail to load the project. When you open the project for the
> first time, IntelliJ will auto-generate a fresh `.idea` from the `.iml`
> files and source folders.

1. Copy Mirth jars into a sibling `mirth-libs/` folder so the IDE can resolve
   the Mirth Connect APIs at compile time:

   ```
   ../mirth-libs/server/mirth-server.jar
   ../mirth-libs/server/donkey-server.jar
   ../mirth-libs/server/mirth-core.jar            (or mirth-client-core.jar)
   ../mirth-libs/client/mirth-client.jar
   ../mirth-libs/client/mirth-core.jar            (or mirth-client-core.jar)
   ../mirth-libs/client/miglayout-swing-4.2.jar   (optional — for MirthCheckBox / MirthTextField)
   ../mirth-libs/client/log4j-1.2-api-2.17.2.jar  (optional)
   ../mirth-libs/test/junit-4.13.2.jar
   ../mirth-libs/test/hamcrest-core-1.3.jar
   ```

2. Open this folder in IntelliJ IDEA (`File → Open`).

3. IntelliJ will prompt you to import the project. Accept the defaults —
   it will detect the four `.iml` files (`shared`, `server`, `client`,
   `test`) and set up the modules automatically.

4. If IntelliJ asks whether to trust the project, click **Trust Project**.

5. If you see red X marks on the Mirth library jars in
   **File → Project Structure → Libraries**, that's normal — IntelliJ
   shows red X for any jar root that doesn't exist on disk, but the
   compilation will succeed using whichever jars are present.

## Configuring the Mirth libraries in IntelliJ

If IntelliJ doesn't auto-detect the libraries, set them up manually:

1. **File → Project Structure → Libraries → + → Java**
2. Select `../mirth-libs/server/` and add these jars:
   - `mirth-server.jar`
   - `donkey-server.jar`
   - `mirth-core.jar` (or `mirth-client-core.jar`)
   - `log4j-1.2-api-2.17.2.jar`
3. Name the library `mirth-server`.
4. Repeat for `../mirth-libs/client/` and name it `mirth-client`.
5. Repeat for `../mirth-libs/test/` and name it `junit-4`.

Then attach the libraries to the modules:

1. **File → Project Structure → Modules → server → Dependencies → + → Library → mirth-server**
2. **Modules → client → Dependencies → + → Library → mirth-client**
3. **Modules → test → Dependencies → + → Library → mirth-server** (yes, the server library — the test needs it)
4. **Modules → test → Dependencies → + → Library → junit-4**
5. Click **Apply** and **OK**.

## Building in IntelliJ IDEA

### Option A: Build Project (compile only)

**Build → Build Project** (Ctrl+F9) — compiles all four modules
(`shared → server → client → test`) into `.class` files.

Output goes to `out/production/<module>/`.

This does NOT produce JAR files — use Option B for that.

### Option B: Build Artifacts (produce JARs)

No artifacts are pre-configured (we removed `.idea` to avoid IDE hangs).
You can create them manually:

1. **File → Project Structure → Artifacts → + → JAR → From modules with dependencies**
2. For each JAR (shared, server, client):
   - **Module:** select the module
   - **Output directory:** `out/artifacts/<name>`
   - **JAR files from libraries:** select "extract to the target JAR"
   - **Manifest file:** `<module>/META-INF/MANIFEST.MF`
3. Click **OK**.
4. **Build → Build Artifacts → <name>** to build the JAR.

Output goes to `out/artifacts/<artifact-name>/`.

### Running tests in IntelliJ

No run configuration is pre-configured. To run the test:

1. Open `test/src/com/bitdreamit/connect/plugins/datatypes/astm/test/ASTME1394RoundTripTest.java`
2. Right-click anywhere in the editor → **Run 'ASTME1394RoundTripTest'**
   (or press Ctrl+Shift+F10).

IntelliJ will create a temporary run configuration automatically.

### Build artifacts vs. production build script

| Feature | IntelliJ Artifacts | `distribution/build.sh` |
|---------|-------------------|------------------------|
| Produces JARs | Yes (manual setup) | Yes |
| Produces standalone `plugin.xml` | No | Yes |
| Runs tests | No (use Run config) | Yes (unless `SKIP_TESTS=1`) |
| Includes version manifests | Yes (from META-INF) | Yes |
| Cross-platform | IntelliJ only | Any Unix with JDK |
| IDE config required | Yes | No |

**Recommendation:** For production deployment, use `distribution/build.sh`.
For development debugging, use IntelliJ's Build Project.

## Build & deploy

### Prerequisites

1. Install JDK 8+ (`java -version` to verify).
2. Create a `mirth-libs/` folder **next to** the project folder containing
   the Mirth 4.5.x JARs (see "IntelliJ IDEA setup" above for the list).

### Build

```bash
cd distribution
chmod +x build.sh
./build.sh
```

The build script:

1. Validates that every required Mirth jar is present under `../mirth-libs/`.
2. Compiles `shared` → `server` → `client` → `test`.
3. Runs `ASTME1394RoundTripTest` (unless `SKIP_TESTS=1`).
4. Produces in `out/`:

   | File | Purpose |
   |------|---------|
   | `datatype-astm-e1394-shared.jar` | Shared classes — loaded by both server & client |
   | `datatype-astm-e1394-server.jar` | Server classes (serializer, batch adaptor, etc.) |
   | `datatype-astm-e1394-client.jar` | Client classes (settings panel) |
   | `plugin.xml` | Unified plugin metadata |
   | **`datatype-astm-e1394-extension.zip`** | **Upload-ready zip (see below)** |

### Install into Mirth Connect — Method 1: Upload zip (recommended)

This is the easiest method and uses Mirth's built-in extension uploader.

1. Run `distribution/build.sh` (see above).
2. Locate the file `out/datatype-astm-e1394-extension.zip`.
3. Open Mirth Administrator → **Extensions** → **Upload Extension**.
4. Select `datatype-astm-e1394-extension.zip`.
5. Click **OK** — Mirth extracts the zip and installs the extension.
6. **Restart Mirth Connect service** (critical — extensions load at startup).

The extension zip contains exactly:

```
datatype-astm-e1394-extension.zip
    ├── plugin.xml
    ├── datatype-astm-e1394-shared.jar
    ├── datatype-astm-e1394-server.jar
    └── datatype-astm-e1394-client.jar
```

All files at the zip root (no subdirectories). This is the format Mirth's
extension uploader expects.

### Install into Mirth Connect — Method 2: Manual copy

```bash
mkdir -p $MIRTH_HOME/extensions/datatype-astm-e1394
cp out/datatype-astm-e1394-shared.jar $MIRTH_HOME/extensions/datatype-astm-e1394/
cp out/datatype-astm-e1394-server.jar $MIRTH_HOME/extensions/datatype-astm-e1394/
cp out/datatype-astm-e1394-client.jar $MIRTH_HOME/extensions/datatype-astm-e1394/
cp out/plugin.xml $MIRTH_HOME/extensions/datatype-astm-e1394/plugin.xml
# Restart Mirth Connect service.
```

### Verify installation

After restarting Mirth Connect:

1. Open Mirth Administrator.
2. **Extensions** → you should see "ASTM E1394 Data Type" in the list.
3. Create a new channel → Source tab → Data Type dropdown should show
   "ASTM E1394".

### Troubleshooting

**"500 Server Error" on JNLP URL** (`/webstart/extensions/datatype-astm-e1394.jnlp`):

This means the Mirth server couldn't generate the Java Web Start descriptor
for the extension. The most common causes are:

1. **Missing JAR files** — all three JARs declared in `plugin.xml` must
   exist in the extension folder. Check `$MIRTH_HOME/extensions/datatype-astm-e1394/`
   contains `datatype-astm-e1394-shared.jar`, `-server.jar`, and `-client.jar`.
2. **Folder name mismatch** — the folder name must match the `path`
   attribute in `plugin.xml` exactly (`datatype-astm-e1394`).
3. **Server not restarted** — Mirth only loads extensions at startup.
   Always restart after installing.
4. **Uploaded the wrong zip** — the source-code zip (from this project)
   is NOT the same as the extension zip. You must run `build.sh` first
   to produce `out/datatype-astm-e1394-extension.zip`, then upload THAT.

Check `$MIRTH_HOME/logs/mirth.log` for the actual server-side exception.

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

- `PRODUCTION_NOTES.md` — detailed changelog of v1.0.0 → v1.1.1 production fixes.
