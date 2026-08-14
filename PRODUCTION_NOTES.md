# Production Notes — bitdreamit-astm-e1394-datatype v1.1.0

This document records the production-grade fixes applied to the original
v1.0.0 source tree. The original codebase had multiple critical inconsistencies
that prevented compilation, broke round-trip serialization, and shipped with
an unsafe build script. v1.1.0 resolves all of them.

## Summary of issues found in v1.0.0

### 1. Package-declaration mismatch (CRITICAL — compilation failure)

20 of the 24 Java source files declared their `package` as
`com.bitdreamit.mirth.astm.e1394.{shared,server,client}` while physically
living under `com/bitdreamit/connect/plugins/datatypes/astm/{shared,server,client}/`.
The directory layout, the `plugin.xml` references, and the four "good" files
(`ASTME1394Deserializer`, `ASTME1394EscapeUtil`, `ASTME1394MessageSerializer`,
`ASTME1394RoundTripTest`) all used `com.bitdreamit.connect.plugins.datatypes.astm.*`.

As a result `javac` could not resolve imports between sibling files and the
build failed before producing any usable artifact.

**Fix**: aligned every `package` declaration and every `import` statement to
`com.bitdreamit.connect.plugins.datatypes.astm.*` (the path already used by
the plugin.xml metadata files). This is the canonical Mirth Connect plugin
package convention.

### 2. Two competing XML schemas (CRITICAL — round-trip broken)

The codebase contained two completely independent converters that produced
two completely different XML formats from the same ASTM input:

* `ASTME1394Deserializer.toXML()` produced the canonical `<ASTM><H><1>H</1>
  <2>\^&amp;</2>…</H>…</ASTM>` tree (the format documented in the README and
  exercised by the test).
* `ASTME1394ToXmlConverter.convert()` produced a different
  `<ASTMMessage><H><Field1>H</Field1><Field2>…</Field2>…</H>…</ASTMMessage>`
  tree, with different element names and an inconsistent component model.

Both implementations were wired into the `IMessageSerializer` entry point
through different code paths, which meant that an inbound `toXML` and an
outbound `fromXML` would not round-trip.

**Fix**: removed `ASTME1394ToXmlConverter` and `ASTME1394MessageSerializer`.
`ASTME1394Deserializer` is now the single source of truth for
`toXML`; `ASTME1394FromXmlConverter` is the single source of truth for
`fromXML` and is the exact inverse of the deserializer.

### 3. `ASTME1394Serializer` constructor / API mismatch (CRITICAL — compilation failure)

`ASTME1394MessageSerializer.fromXML()` invoked
`new ASTME1394Serializer(serializationProperties)` where
`serializationProperties` was of type `SerializationProperties`, but
`ASTME1394Serializer` only exposed a constructor accepting the wrapper type
`SerializerProperties`. This was a raw type error.

**Fix**: rewrote `ASTME1394Serializer` to be the single
`IMessageSerializer`. It accepts `SerializerProperties` in its primary
constructor (Mirth's normal instantiation path) and also exposes a
two-arg convenience constructor `(ASTME1394SerializationProperties,
ASTME1394DeserializationProperties)` used by the test suite. The serializer
delegates `toXML` to `ASTME1394Deserializer` and `fromXML` to
`ASTME1394FromXmlConverter`, so there is a single end-to-end path.

### 4. Properties exposed `String`-based delimiter accessors but the parser expected `char` (CRITICAL — type mismatch)

`ASTME1394SerializationProperties` and `ASTME1394DeserializationProperties`
stored delimiters as `String` and exposed only `String` getters
(`getFieldDelimiter(): String`), while `ASTME1394Deserializer.toXML()`
called `props.getFieldDelimiter()` and immediately assigned the result to a
`char` local. The original `DeserializationProperties` class did not even
declare any delimiter fields, so the call sites were undefined.

**Fix**: both property classes now store delimiters as `char` primitives and
expose `char` getters, matching what the parser hot path needs. The
`DataTypePropertyDescriptor` map wraps each `char` back into a single-character
`String` for the Mirth Administrator UI.

### 5. `ASTME1394EscapeUtil` was correct, but its sibling
`ASTME1394FromXmlConverter.escapeDelimiters()` used a different escape format

`ASTME1394EscapeUtil` correctly handles the single-character ASTM escape
sequences `&F`, `&S`, `&R`, `&E`, `&Hxx`. However
`ASTME1394FromXmlConverter.escapeDelimiters()` was independently escaping
delimiters using a non-standard two-character format (`&F&`, `&R&`, `&C&`).
Worse, it used `&C` for the component delimiter, but the standard escape
for the component delimiter is `&S` (the component delimiter is also called
the "separator"). This would have corrupted every round-trip message.

**Fix**: `ASTME1394FromXmlConverter` now delegates all escaping to
`ASTME1394EscapeUtil`, so the escape logic exists in exactly one place and
uses the canonical single-character format.

### 6. `ASTME1394FrameParser.unescape()` stripped the escape character

The original `ASTME1394FrameParser.unescape()` simply removed the escape
character (`return value.replace(escapeCharacter, "")`) — that is not
unescaping, that is deleting. Every `&F`, `&S`, `&R`, `&E`, `&Hxx` would
have been corrupted into literal `F`, `S`, `R`, `E`, `Hxx`.

**Fix**: deleted `ASTME1394FrameParser` entirely. The deserializer now
parses records directly and delegates escaping to `ASTME1394EscapeUtil`,
which implements the correct unescape algorithm.

### 7. `ASTME1394BatchAdaptor.getMessage()` returned the entire batch as a single message

The original `ASTME1394BatchAdaptor` returned one giant string from
`getMessage()` with no batch splitting, despite the README claiming
"batch splitting on H..L boundaries". The test suite also expected a
`getMessages(String)` method that returned a `List<String>`, which did not
exist.

**Fix**: `ASTME1394BatchAdaptor` now exposes both `getMessage()` (the
Mirth-framework contract, which still returns a single string per call)
and `getMessages(String batch)` (a list-returning helper used by transformer
steps and the test). The split strategy is configurable through
`ASTME1394BatchProperties.setSplitBatchBy()`:

| Strategy        | Behavior                                                       |
|-----------------|----------------------------------------------------------------|
| `H_L_BOUNDARY`  | Each `H…L` session becomes its own message (default).         |
| `RECORD`        | Every record becomes its own message.                         |
| `NONE`          | Entire batch is passed through as a single message.           |

Incomplete sessions (missing `L` terminator) are flushed with a synthetic
`L|1|I` (incomplete) marker when `includeTerminator=true`.

### 8. Test file used wrong escape syntax

The test file `ASTME1394RoundTripTest.testEscapeRoundTrip()` used HL7-style
escape sequences (`\F`, `\S`, `\R`, `\E`, `\H0A`) — but the ASTM E1394
escape character is `&`, not `\`. The escape sequences should be `&F`,
`&S`, `&R`, `&E`, `&H0A`.

**Fix**: rewrote the test file to use the correct ASTM escape syntax and
expanded the test coverage:

* `testRoundTrip` — basic H/P/O/R/L round-trip using legacy `\^&` delimiters.
* `testRoundTripWithCanonicalDelimiters` — same but using canonical `&|\^`
  delimiter order.
* `testHeaderDelimiterDerivation` — overrides default delimiters with
  `#@!&` from the header record and verifies derived delimiters take effect.
* `testEscapeRoundTrip` — verifies `&F`, `&S`, `&R`, `&E`, `&H0A` survive
  the round-trip.
* `testBatchSplit` — verifies H..L boundary splitting produces one message
  per session.
* `testBatchSplitNoSplit` — verifies `NONE` strategy passes through.

### 9. Header-delimiter derivation used wrong character order

The original `deriveDelimitersFromHeader()` read positions 0–3 as
`escape, field, repeat, component`, but the SAMPLE test message had a
3-character delimDef (`\^&`) which would not satisfy the `length() < 4`
check. Both the test sample and the order were inconsistent.

**Fix**: documented that the canonical encoding order is
`<escape><field><repeat><component>` (yielding `&|\^` for default delimiters)
and updated the test sample accordingly. The legacy `\^&` form is still
tolerated by the parser when `deriveDelimitersFromHeader=false` (the default).

### 10. Build script referenced non-existent jar names

`build.sh` referenced `mirth-client-core.jar` for the SERVER classpath, but
the README listed `mirth-core.jar`. Neither jar was actually present in any
real Mirth distribution under that exact name.

**Fix**: rewrote `build.sh` from scratch. It now:

* Validates every required jar exists before invoking `javac`.
* Tolerates alternate jar names (`mirth-core.jar` vs.
  `mirth-client-core.jar`; `junit-4.13.2.jar` vs. `junit.jar`).
* Compiles each module into a separate `out/build/<module>` directory.
* Generates JAR manifests with `Implementation-Title`, `Implementation-Version`,
  `Mirth-Connect-Plugin-Type`, and `Mirth-Connect-Compatible-Versions`
  attributes.
* Packages both server and client JARs with the shared classes included.
* Runs the JUnit test suite before packaging unless `SKIP_TESTS=1`.
* Exits non-zero on any error (`set -euo pipefail`).

### 11. `ASTME1394BatchStreamReader` used raw `List`

The original `checkForIntermediateMessage()` declared a raw `List`
parameter and cast each element to `Byte` with `(Byte) b`. This produced
an unchecked-warning at every compile and was fragile to generification.

**Fix**: kept the raw `List` parameter (the parent class signature
requires it) but added `@SuppressWarnings("rawtypes")` and switched to
`((Byte) b).byteValue()` for the cast.

### 12. `ASTME1394Constants` had incorrect comment and undersized message-size default

* Comment said "ASTM E1394-91" but the codebase targets E1394-91/97 (both
  share the same record format and delimiter conventions).
* `DEFAULT_MAX_MESSAGE_SIZE = 10240` (10 KB) was far too small for any
  realistic ASTM session — a single patient order with results easily
  exceeds that. The new default is 256 KB (matches the Donkey server
  inbound limit).
* Added `char` constants for each default delimiter for direct comparison
  in the parser hot path.
* Added E1381 frame-byte constants (`FRAME_STX`, `FRAME_ETX`, `FRAME_ETB`,
  `FRAME_EOT`, `FRAME_ENQ`, `FRAME_ACK`, `FRAME_NAK`) so the auto-responder
  and response validator don't sprinkle magic numbers throughout.

### 13. `ASTME1394AutoResponder` LRC calculation did not respect encoding

The original `calculateLRC()` XOR-ed each `char` of the payload string,
which on multi-byte encodings (UTF-8) would produce the wrong byte-level
LRC. The auto-responder also concatenated raw `char` values into a
`StringBuilder`, which silently truncated any byte > 0x7F to a UTF-16 code
unit.

**Fix**: LRC is now computed from the byte array of the payload in the
configured encoding, and the response is built from explicit `char` values
in the ASTM range (0x00–0xFF).

### 14. Settings panel did not enforce delimiter uniqueness

The original settings panel accepted any four single-character delimiters
without checking that they were mutually distinct. Allowing e.g.
field-delimiter = escape-character = `|` would have caused infinite parser
loops and ambiguous round-trips.

**Fix**: `doSave()` now rejects any configuration where two or more
delimiters collide, with a user-facing alert.

## Files removed in v1.1.0

| File                              | Reason                                                |
|-----------------------------------|-------------------------------------------------------|
| `ASTME1394FrameParser.java`       | Broken unescape logic; redundant with `Deserializer`. |
| `ASTME1394ToXmlConverter.java`    | Competing XML format; redundant with `Deserializer`.  |
| `ASTME1394MessageSerializer.java` | Did not compile; redundant with `Serializer`.         |

## Files added in v1.1.0

| File                  | Reason                                       |
|----------------------|----------------------------------------------|
| `PRODUCTION_NOTES.md` | This document — explains every fix to users. |

## Migration guide: v1.0.0 → v1.1.0

If you previously deployed v1.0.0 in production:

1. Stop Mirth Connect.
2. Remove the old `bitdreamit-astm-e1394-datatype-*.jar` files from
   `$MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/`.
3. Run `distribution/build.sh` against your Mirth 4.x installation.
4. Copy the new `out/bitdreamit-astm-e1394-datatype-{server,client}.jar`
   files into the extension directory.
5. Restart Mirth Connect.
6. Existing channels that reference the data type by name
   (`"ASTM E1394"`) continue to work without modification. Channel
   exports from v1.0.0 are forward-compatible.
