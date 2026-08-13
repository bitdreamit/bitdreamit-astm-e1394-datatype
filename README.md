# bitdreamit-astm-e1394-datatype

ASTM E1394-97 record-level data type plugin for Mirth Connect / BridgeLink.

## IntelliJ IDEA Setup

1. Copy Mirth jars to sibling `mirth-libs/` folder:
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

3. Modules `shared`, `server`, `client`, `test` load automatically.

4. `Build → Build Project` (Ctrl+F9).

## Build & Deploy

```bash
cd distribution
chmod +x build.sh
./build.sh
```

Copy to Mirth extensions:
```bash
cp out/bitdreamit-astm-e1394-datatype-server.jar $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/
cp server/resources/plugin.xml $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/
cp out/bitdreamit-astm-e1394-datatype-client.jar $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/
cp client/resources/plugin.xml $MIRTH_HOME/extensions/bitdreamit-astm-e1394-datatype/
```

Restart Mirth service.

## Features
- H/P/O/R/C/Q/M/L record parsing
- XML tree output matching Mirth HL7v2 style (`<ASTM><H><1>H</1>...</H>...</ASTM>`)
- Full E1394 escape handling (`&F`, `&S`, `&R`, `&E`, `&Hxx`)
- Header record delimiter derivation at parse time
- Batch splitting on H..L boundaries (for file/plain-TCP use without E1381)
- Strict vs. lenient validation mode
- Swing settings panel in Administrator UI
- Round-trip serialization (ASTM text → XML → ASTM text)

## Test
Run `ASTME1394RoundTripTest` via IntelliJ JUnit runner or:
```bash
cd test
javac -cp ../out/shared:../out/server:$JUNIT_JAR src/com/bitdreamit/connect/plugins/datatypes/astm/test/*.java
java -cp ../out/shared:../out/server:$JUNIT_JAR:src org.junit.runner.JUnitCore com.bitdreamit.connect.plugins.datatypes.astm.test.ASTME1394RoundTripTest
```
