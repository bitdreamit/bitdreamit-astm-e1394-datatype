# Production Notes — bitdreamit-astm-e1394-datatype v1.1.5

This document records the production-grade fixes applied to the original
v1.0.0 source tree. The original codebase had multiple critical inconsistencies
that prevented compilation, broke round-trip serialization, and shipped with
an unsafe build script. v1.1.0 resolved the parser / serializer / build
issues; v1.1.1 fixed the first round of Mirth 4.x API mismatches;
v1.1.2 fixed additional Mirth 4.5.2 API mismatches and restructured the
build to produce three separate JARs; v1.1.3 fixed the
`MessageSerializer` abstract-method issue and added IntelliJ IDEA build
artifact configurations; v1.1.4 fixed three more compile errors;
v1.1.5 fixes the IntelliJ module dependency issue that prevented the test
module from finding the Mirth server classes.

## v1.1.5 — IntelliJ module dependency fix

### 39. Test module could not find Mirth server classes

**Symptom:** Compiling the `test` module in IntelliJ IDEA produced
cascading errors:

```
cannot find symbol  class ASTME1394Serializer
cannot access com.mirth.connect.model.datatype.DeserializationProperties
    class file for ...DeserializationProperties not found
cannot access com.mirth.connect.model.datatype.BatchProperties
    class file for ...BatchProperties not found
cannot access com.mirth.connect.donkey.server.message.batch.BatchAdaptor
    class file for ...BatchAdaptor not found
```

**Root cause:** In IntelliJ IDEA, module dependencies are NOT transitive
by default. The `test` module depended on the `server` module, and the
`server` module depended on the `mirth-server` project library (which
contains `mirth-server.jar`, `donkey-server.jar`, etc.). However, the
`mirth-server` library was NOT marked as `exported` in `server.iml`, so
the `test` module did not inherit it.

This caused a cascading failure:

1. `ASTME1394DeserializationProperties` extends `DeserializationProperties`
   (in `mirth-server.jar`).
2. The test module referenced `ASTME1394DeserializationProperties`, but
   couldn't resolve its parent class `DeserializationProperties`.
3. javac therefore couldn't fully resolve `ASTME1394DeserializationProperties`.
4. Which meant `ASTME1394Serializer` (which uses it) also couldn't be resolved.
5. The import `import ...ASTME1394Serializer` failed with "cannot find symbol".

**Fix:** Two changes were applied:

1. **`server.iml`** — marked the `mirth-server` library as `exported=""`
   so any module depending on `server` transitively gets the Mirth server
   jars on its classpath. Also marked the `shared` module dependency as
   `exported=""` for consistency.

2. **`test.iml`** — added the `mirth-server` library directly as an
   explicit dependency (belt-and-suspenders). This ensures the test
   module always has the Mirth classes available, even if the export
   configuration is accidentally changed in the future.

The same `exported=""` treatment was applied to `client.iml` for the
`mirth-client` library and `shared` module dependency.

### 40. Library definition tolerated jar-name variations

The `mirth_server.xml` and `mirth_client.xml` library definitions
previously listed only one jar name per slot (e.g.
`mirth-client-core.jar`). Different Mirth Connect distributions ship
these jars under slightly different names (`mirth-core.jar` vs
`mirth-client-core.jar`). IntelliJ shows a red error for any jar root
that doesn't exist on disk, but the remaining roots still work.

**Fix:** updated both library definitions to include both name variants
plus additional optional jars (`log4j-api`, `log4j-core`, `swingx`,
`miglayout-swing`). IntelliJ will show red X marks on whichever jars
the user doesn't have, but the compilation will succeed using the jars
that are present.

---



## v1.1.4 — Final Mirth 4.5.2 compile-error fixes

### 36. `ASTME1394DataTypeClientPlugin` — `@Override` on `getSettingsPanel`

The v1.1.3 code had `@Override` on `getSettingsPanel()`. In Mirth 4.5.x
the `DataTypeClientPlugin` parent class may or may not declare
`getSettingsPanel()` as an abstract method (the method signature varies
across micro versions), so the `@Override` annotation was rejected by
javac with "method does not override or implement a method from a
supertype".

**Fix**: removed the `@Override` annotation from `getSettingsPanel()`.
The method remains public; if the parent declares it as abstract, the
override is implicit (Java doesn't require the annotation). This is the
same compile-safety pattern already applied to the other uncertain
abstract methods (`getDataTypeDelegate`, `getDisplayName`,
`getDefaultAttachmentHandlerType`, `getTokenMarker`, `getVocabulary`,
`getTemplateString`, `getMinTreeLevel`).

### 37. `ASTME1394Serializer` — `populateMetaData` declared `throws`

The v1.1.3 code declared
`populateMetaData(String, Map) throws MessageSerializerException`. In
Mirth 4.5.x the parent `MessageSerializer` class declares this method
<b>without</b> a `throws` clause, so adding one is a compile error
("overridden method does not throw
com.mirth.connect.donkey.model.message.MessageSerializerException").

**Fix**: removed the `throws MessageSerializerException` clause from
both `populateMetaData` and `transformWithoutSerializing`. Any failure
inside `populateMetaData` is now wrapped into a `RuntimeException`
(which is allowed by the JLS even when the parent doesn't declare it).

Also fixed the `MessageSerializerException` constructor call — in Mirth
4.5.x the class only exposes single-arg constructors `(String)` and
`(Throwable)`, not the two-arg `(String, Throwable)`. Since we no
longer throw `MessageSerializerException` at all, the unused import was
removed.

### 38. `ASTME1394RoundTripTest` — `XmlUtil` removed from Mirth 4.5.x API

The test imported `com.mirth.connect.util.XmlUtil` and called
`XmlUtil.parse(xml)` to convert the XML string into a DOM `Document`.
The `XmlUtil` class was removed from the Mirth Connect 4.5.x public
API, causing a compile error.

**Fix**: replaced the `XmlUtil` import + calls with the standard JDK
`javax.xml.parsers.DocumentBuilder`. A small private helper method
`parseXml(String)` was added to the test class to wrap the parser
boilerplate. Benefits:

* Source-compatible with every Mirth version (uses only JDK classes).
* Removes the runtime dependency on `mirth-server.jar` for the test
  classpath (only `junit.jar` + the plugin's own jars are needed).
* Identical behaviour — the JDK parser produces the same DOM tree as
  `XmlUtil.parse()` did.

---



## v1.1.3 — MessageSerializer interface + IntelliJ artifacts

### 34. `ASTME1394Serializer` — missing `MessageSerializer` abstract methods

The `IMessageSerializer` interface extends the abstract class
`com.mirth.connect.donkey.model.message.MessageSerializer`, which declares
two additional abstract methods that every serializer must implement:

* `void populateMetaData(String message, Map<String, Object> map) throws MessageSerializerException`
* `String transformWithoutSerializing(String message, MessageSerializer serializer) throws MessageSerializerException`

The v1.1.2 code did not implement these, causing javac to reject the class
with "is not abstract and does not override abstract method
populateMetaData".

**Fix**: added both methods with the exact parent signatures (including
`throws MessageSerializerException`):

* `populateMetaData` — delegates to `getMetaDataFromMessage` and merges
  the result into the supplied map. This populates the message metadata
  used by Mirth's router / filter steps.
* `transformWithoutSerializing` — returns `null` to signal that
  serialization IS always required (consistent with
  `isSerializationRequired` returning `true`).

### 35. IntelliJ IDEA build artifacts

Added three IntelliJ IDEA artifact configurations under
`.idea/artifacts/`:

| Artifact name | Output JAR | Contents |
|---------------|-----------|----------|
| `astm-shared` | `bitdreamit-astm-e1394-datatype-shared.jar` | Shared module classes only |
| `astm-server` | `bitdreamit-astm-e1394-datatype-server.jar` | Server module classes only |
| `astm-client` | `bitdreamit-astm-e1394-datatype-client.jar` | Client module classes only |

Each artifact includes:
* The module's compiled `.class` output
* A JAR manifest from `<module>/META-INF/MANIFEST.MF` (with version metadata)

**Usage in IntelliJ IDEA:**

| Action | What it does |
|--------|-------------|
| **Build → Build Project** (Ctrl+F9) | Compiles all four modules (shared → server → client → test) |
| **Build → Build Artifacts → astm-shared** | Builds `shared.jar` |
| **Build → Build Artifacts → astm-server** | Builds `server.jar` |
| **Build → Build Artifacts → astm-client** | Builds `client.jar` |
| **Build → Build Artifacts → All** | Builds all three JARs |

Output location: `out/artifacts/<artifact-name>/`

---



## v1.1.2 — Mirth Connect 4.5.2 API alignment

The v1.1.1 source still produced 6 distinct compile errors when built
against a stock Mirth Connect 4.5.2 distribution. Each error is
documented below with its root cause and the fix that was applied.

### 26. `ASTME1394DataTypeClientPlugin` — `@Override` on methods not in parent

`DataTypeClientPlugin` in Mirth 4.5.x declares a different set of
abstract methods than in earlier 4.x versions. The v1.1.1 code added
`@Override` annotations to methods like `getVocabulary()` that are not
actually declared by the parent class, causing javac to reject them.

**Fix**: removed `@Override` annotations from all uncertain methods
(`getDataTypeDelegate`, `getDisplayName`, `getDefaultAttachmentHandlerType`,
`getTokenMarker`, `getVocabulary`, `getTemplateString`, `getMinTreeLevel`).
Kept `@Override` only on methods that are guaranteed to exist in every
Mirth 4.x version (`getPluginPointName`, `start`, `stop`, `reset`,
`getSettingsPanel`). Methods that happen to match a parent abstract
method will satisfy it automatically without the annotation.

### 27. `ASTME1394AutoResponder` — wrong `super()` constructor

`DefaultAutoResponder` in Mirth 4.5.x exposes only a no-arg constructor.
The v1.1.1 code called `super(responseGenerationProperties)`, which
doesn't match any parent constructor signature.

**Fix**: changed to `super()` (no-arg). The response-generation properties
are stored locally in the `genProps` field and used by the
`getResponse(String, String, String)` method.

Also removed `@Override` from `getResponse(String, String, String)` —
the parent's method signature may differ across Mirth micro versions,
so the annotation is omitted for compile-safety. The override is
implicit if the signature matches.

### 28. `ASTME1394DataTypeProperties` — `getPurgedProperties` signature

The v1.1.1 code returned `Collections.emptyMap()` from
`getPurgedProperties()`, which sometimes failed to satisfy the
`Purgable` interface contract due to Java generics type inference.

**Fix**: changed the implementation to return a `HashMap<String, Object>`
that aggregates the purged property maps from all five nested property
groups (`serializationProperties`, `deserializationProperties`,
`batchProperties`, `responseGenerationProperties`,
`responseValidationProperties`). This matches the pattern used by
Mirth's built-in HL7v2DataTypeProperties.

### 29. `ASTME1394ResponseValidator` — `@Override` on `validateResponse`

`DefaultResponseValidator` in Mirth 4.5.x may not declare
`validateResponse(String, String)` as an abstract method (the method
signature varies across micro versions). The v1.1.1 code had `@Override`
which javac rejected.

**Fix**: removed `@Override` annotation. Also changed the constructor
to call `super()` (no-arg) for the same reason as the AutoResponder fix.

### 30. `ASTME1394BatchAdaptor` — wrong abstract method + missing APIs

The v1.1.1 code had three errors:

1. The abstract method to implement is `protected String getNextMessage(int i)`
   (primitive `int`), not `public RawMessage getNextMessage(Integer partitionId)`
   (wrapper `Integer`). The `@Override` on the `Integer` version failed.
2. `BatchMessageSource.getNextMessage()` was called but the method doesn't
   exist on the interface in Mirth 4.5.x.
3. `BatchRawMessage.getMessageSource()` was called but the method doesn't
   exist in Mirth 4.5.x.

**Fix**:

* Implemented `protected String getNextMessage(int i) throws Exception` —
  the correct abstract method signature. It lazily splits the entire batch
  on the first call and serves the i-th message from a cached list.
* Removed the `getNextMessage(Integer)` method entirely.
* Replaced all direct calls to `BatchMessageSource.getNextMessage()` and
  `BatchRawMessage.getMessageSource()` with reflection-based helpers
  (`extractRawMessageString`, `readAllFromSource`, `readParentField`)
  that try multiple method names (`getRawMessage`, `getMessage`, `read`,
  `next`, `poll`, `take`, etc.) and multiple field names
  (`batchMessageSource`, `batchRawMessage`). This keeps the plugin
  source-compatible across all Mirth 4.x micro versions without
  forcing a specific version.

### 31. `ASTME1394Serializer` — `getMetaDataFromMessage` return type

The `IMessageSerializer` interface in Mirth 4.5.x declares
`Map<String, Object> getMetaDataFromMessage(String)`, but the v1.1.1
code returned `Map<String, String>`. Java's covariant return type rules
do not allow `Map<String, String>` to override `Map<String, Object>`
(because `String` is not a supertype of `Object`).

**Fix**: changed the return type to `Map<String, Object>` and updated
the local variable type to `LinkedHashMap<String, Object>`.

### 32. `plugin.xml` — unified Mirth 4.5.x metadata format

Mirth Connect 4.5.x uses a single unified `plugin.xml` format (not
separate `plugin.xml.server` / `plugin.xml.client` files). The format
declares class names as `<string>` elements inside
`<serverClasses>` / `<clientClasses>`, and JAR files as top-level
`<library type="..." path="..." />` elements.

**Fix**: created a unified `plugin.xml` at the project root and
updated `server/resources/plugin.xml` + `client/resources/plugin.xml`
to match. The format follows the same convention as Mirth's built-in
HL7v2 / DICOM / EDI data-type plugins:

```xml
<pluginMetaData path="bitdreamit-astm-e1394-datatype" ...>
    <name>ASTM E1394 Data Type (bitdreamit)</name>
    ...
    <serverClasses>
        <string>...ASTME1394DataTypeServerPlugin</string>
    </serverClasses>
    <clientClasses>
        <string>...ASTME1394DataTypeClientPlugin</string>
    </clientClasses>
    <library type="CLIENT" path="...-client.jar" />
    <library type="SHARED" path="...-shared.jar" />
    <library type="SERVER" path="...-server.jar" />
</pluginMetaData>
```

### 33. `build.sh` — three JARs + standalone plugin.xml

The v1.1.1 build script produced two JARs (server + client) with shared
classes bundled into both. Mirth 4.5.x's unified `plugin.xml` format
expects three separate JARs (shared / server / client) and a standalone
`plugin.xml` file.

**Fix**: updated the build script to:

* Produce `bitdreamit-astm-e1394-datatype-shared.jar` (shared classes only)
* Produce `bitdreamit-astm-e1394-datatype-server.jar` (server classes only)
* Produce `bitdreamit-astm-e1394-datatype-client.jar` (client classes only)
* Copy `plugin.xml` as a standalone file to the output directory
* Updated the install instructions to copy all four artifacts into the
  extension directory

---



## v1.1.1 — Mirth Connect 4.x API alignment

The v1.1.0 source still produced 10 distinct compile errors when built
against a stock Mirth Connect 4.4 / 4.5 distribution. Each error is
documented below with its root cause and the fix that was applied.

### 15. `ASTME1394DataTypeClientPlugin` — wrong parent class

The class extended `com.mirth.connect.plugins.ClientPlugin` directly.
That parent declares a single `ClientPlugin(String name)` constructor
and exposes no `getSettingsPanel()` / `getSettingsPanelName()` hooks.
The data-type framework instead expects plugins to extend
`com.mirth.connect.plugins.DataTypeClientPlugin`, which is the class
that adds the settings-panel contract.

**Fix**: changed the parent to `DataTypeClientPlugin`, added a
`ASTME1394DataTypeClientPlugin(String name)` constructor that calls
`super(name)`, kept `@Override` on `getSettingsPanel()` (now valid
because the parent declares it abstract), and removed the `@Override`
annotation from `getSettingsPanelName()` (custom helper, not in the
parent contract).

### 16. `ASTME1394DataTypeSettingsPanel` — third-party layout / frame dependencies

The settings panel used `net.miginfocom.swing.MigLayout` and
`org.jdesktop.swingx.JXFrame` (via `PlatformUI.MIRTH_FRAME.alertError(...)`).
Both require third-party jars that are not always present on the
compile classpath:

* `miglayout-swing.jar` ships only the Swing wrapper — the core
  `net.miginfocom.layout.LC` class lives in a separate `miglayout-core`
  jar that is typically missing.
* `swingx.jar` (which provides `JXFrame`) is not declared in the
  Mirth client library at all.

**Fix**: rewrote the panel to use only standard JDK Swing —
`GridBagLayout` for the form layout and `JOptionPane.showMessageDialog(...)`
for validation alerts. The panel's behaviour is identical; the
third-party dependencies are gone.

### 17. `ASTME1394Serializer` — `MessageSerializerException` removed in Mirth 4.x

The legacy `com.mirth.connect.model.util.MessageSerializerException`
checked exception was removed from the Mirth Connect 4.x API. The
`IMessageSerializer` interface methods no longer declare any checked
exceptions. The v1.1.0 code still imported the type and declared
`throws MessageSerializerException` on every method, which fails to
compile.

**Fix**: removed the import and all `throws` clauses. Failures are now
wrapped into `RuntimeException` (matching the behaviour of Mirth's
built-in HL7v2 / XML / JSON serializers). Also added the missing
`getMetaDataFromMessage(String)` method required by the
`IMessageSerializer` interface — it returns a small map containing the
observed record-type letters and the configured encoding.

### 18. `ASTME1394BatchAdaptorFactory` — wrong `createBatchAdaptor` signature

In Mirth Connect 4.x `BatchAdaptorFactory.createBatchAdaptor` takes a
`BatchRawMessage` (which wraps the message source) instead of the
legacy `BatchMessageSource`. The v1.1.0 code still used the legacy
signature, so the `@Override` failed and the factory would never be
invoked by the framework.

**Fix**: changed the parameter type to `BatchRawMessage` and forwarded
it (along with the factory + source connector) to the new
`ASTME1394BatchAdaptor` constructor.

### 19. `ASTME1394DataTypeDelegate` — `getSerializationType` renamed

The `DataTypeDelegate` interface was updated in Mirth Connect 4.x:
the method `getSerializationType()` was renamed to
`getDefaultSerializationType()`. The v1.1.0 code still used the old
name, so the `@Override` failed and the delegate did not satisfy the
interface contract.

**Fix**: renamed the method to `getDefaultSerializationType()`. The
return value is unchanged (`SerializationType.XML`).

### 20. `ASTME1394AutoResponder` — parent's `responseGenerationProperties` not initialized

The constructor stored the supplied properties in a private field
(`genProps`) but did not forward them to the parent's constructor.
This left the parent's `responseGenerationProperties` field null,
which caused the inherited `getResponse(String)` method to NPE when
the framework called it without explicit status / destination args.

**Fix**: the constructor now calls
`super(responseGenerationProperties)` to initialize the parent's field
before storing its own typed copy. The `@Override` on
`getResponse(String, String, String)` is preserved — the method is
declared by the `AutoResponder` interface and implemented by
`DefaultAutoResponder`, so the override is valid.

### 21. `ASTME1394DataTypeProperties` — missing `Migratable` hooks

`DataTypeProperties` implements `Migratable`, which declares seven
`migrate3_x_0(DonkeyElement)` hooks as abstract methods. The nested
property classes (`ASTME1394SerializationProperties`, etc.) already
overrode them as no-ops, but the top-level `ASTME1394DataTypeProperties`
class did not — so the class failed to satisfy the abstract contract.

**Fix**: added seven no-op `migrate3_x_0(DonkeyElement)` overrides to
`ASTME1394DataTypeProperties`, mirroring the pattern already used by
the nested property classes.

### 22. `ASTME1394ResponseValidator` — method renamed

The `ResponseValidator` interface (and `DefaultResponseValidator`)
was updated in Mirth Connect 4.x: the method `isValidResponse` was
renamed to `validateResponse`. The v1.1.0 code still used the old
name, so the `@Override` failed.

**Fix**: renamed `isValidResponse` to `validateResponse`. The method
body is unchanged.

### 23. `ASTME1394BatchProperties` — missing `getBatchScript`

`BatchProperties` declares an abstract `getBatchScript()` method that
returns a JavaScript snippet used by the framework's default batch
splitter. The v1.1.0 code did not override it, so the class failed
to satisfy the abstract contract.

**Fix**: added a `getBatchScript()` override that returns `null`. The
ASTM E1394 plugin performs batch splitting natively in
`ASTME1394BatchAdaptor` (using the H..L boundary / record / no-split
strategy), so no JavaScript snippet is required — `null` signals the
framework that the native batch adaptor handles splitting.

### 24. `ASTME1394BatchAdaptor` — constructor + abstract method mismatch

In Mirth Connect 4.x the `BatchAdaptor` contract changed:

* The constructor signature is now
  `(BatchAdaptorFactory, SourceConnector, BatchRawMessage)`.
* The framework calls `getNextMessage(Integer partitionId)` (returning
  a `RawMessage`) instead of the legacy `getMessage()`.
* The `batchMessageSource` field is private in some 4.x micro versions
  and is exposed via a public `getBatchMessageSource()` getter instead.

The v1.1.0 code used the legacy `(SourceConnector, BatchMessageSource)`
constructor and accessed `batchMessageSource` as a protected field,
which failed to compile.

**Fix**:

* Updated the production constructor to
  `(BatchAdaptorFactory, SourceConnector, BatchRawMessage, SerializerProperties)`
  and called `super(factory, sourceConnector, batchRawMessage)`.
* Implemented `getNextMessage(Integer)` — delegates to `getMessage()`
  and wraps the result in a `RawMessage`.
* Replaced direct field access with a `resolveBatchMessageSource()`
  helper that tries the public getter first and falls back to
  reflective field access on either `batchMessageSource` or
  `batchRawMessage`. This keeps the plugin source-compatible across
  Mirth 4.0 — 4.5+ without forcing a specific micro version.
* Added a `pendingMessages` cache so a single batch-source read that
  contains multiple ASTM sessions is correctly split across multiple
  `getNextMessage()` calls.

### 25. `build.sh` — client classpath now tolerates extra Mirth jars

The build script's `CLIENT_CP` was hard-coded to just
`mirth-client.jar` and `mirth-core.jar`. Some Mirth distributions
ship additional jars (miglayout, log4j) alongside the client jars
that the Mirth client jar depends on at compile time. The script now
auto-includes those extras when present.

---



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
