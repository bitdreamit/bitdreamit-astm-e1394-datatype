#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# bitdreamit-astm-e1394-datatype — production build script (v1.1.1)
# ---------------------------------------------------------------------------
# Builds three JARs (shared, server, client) for the ASTM E1394 Mirth Connect
# data type plugin, plus a standalone plugin.xml metadata file.
#
# Designed for Mirth Connect 4.5.x. Tested against Mirth 4.4.x and 4.5.x.
#
# Required environment:
#   * JDK 8+ (Mirth 4.x is JDK 8 baseline; JDK 11 also works)
#   * Mirth 4.x server + client JARs available in ../mirth-libs/
#
# Expected ../mirth-libs/ layout (see README.md):
#   ../mirth-libs/server/mirth-server.jar
#   ../mirth-libs/server/donkey-server.jar
#   ../mirth-libs/server/mirth-core.jar  (or mirth-client-core.jar)
#   ../mirth-libs/client/mirth-client.jar
#   ../mirth-libs/client/mirth-core.jar  (or mirth-client-core.jar)
#   ../mirth-libs/test/junit-4.13.2.jar
#   ../mirth-libs/test/hamcrest-core-1.3.jar
#
# Optional environment overrides:
#   MIRTH_LIBS       — override the ../mirth-libs base path
#   JAVAC_OPTS       — additional javac flags (e.g. -Xlint:unchecked)
#   SKIP_TESTS=1     — skip compiling / running tests
# ---------------------------------------------------------------------------

set -euo pipefail

# Resolve project layout.
PROJECT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$PROJECT_DIR/out"
BUILD_DIR="$OUT_DIR/build"
LIBS_DIR="${MIRTH_LIBS:-$PROJECT_DIR/../mirth-libs}"

# Locate required tools.
JAVAC="${JAVAC:-javac}"
JAR="${JAR:-jar}"
JAVA_BIN="${JAVA_BIN:-java}"

# Version (mirrors plugin.xml pluginVersion + ASTME1394Constants.PLUGIN_VERSION).
PLUGIN_VERSION="1.1.7"

# Colors for status output (disabled when not a TTY).
if [ -t 1 ]; then
    GREEN="\033[0;32m"; YELLOW="\033[0;33m"; RED="\033[0;31m"; RESET="\033[0m"
else
    GREEN=""; YELLOW=""; RED=""; RESET=""
fi

log()   { echo -e "${GREEN}[build]${RESET} $*"; }
warn()  { echo -e "${YELLOW}[warn]${RESET}  $*"; }
fatal() { echo -e "${RED}[fatal]${RESET} $*"; exit 1; }

# ---------------------------------------------------------------------------
# Pre-flight checks
# ---------------------------------------------------------------------------
log "bitdreamit-astm-e1394-datatype v${PLUGIN_VERSION} build"
log "PROJECT_DIR=${PROJECT_DIR}"
log "OUT_DIR=${OUT_DIR}"
log "LIBS_DIR=${LIBS_DIR}"

command -v "$JAVAC" >/dev/null 2>&1 || fatal "javac not found on PATH; install JDK 8+ and retry."
command -v "$JAR"  >/dev/null 2>&1 || fatal "jar not found on PATH; install JDK 8+ and retry."

# Resolve Mirth jar paths (tolerate both mirth-core.jar and mirth-client-core.jar naming).
resolve_jar() {
    local primary="$1"; shift
    local alt="$1"; shift
    local base="$LIBS_DIR/$primary"
    if [ -f "$base" ]; then echo "$base"; return; fi
    if [ -n "$alt" ] && [ -f "$LIBS_DIR/$alt" ]; then echo "$LIBS_DIR/$alt"; return; fi
    fatal "Missing required jar: $primary (also tried $alt) under $LIBS_DIR"
}

MIRTH_SERVER_JAR="$(resolve_jar server/mirth-server.jar)"
DONKEY_SERVER_JAR="$(resolve_jar server/donkey-server.jar)"
MIRTH_CORE_JAR="$(resolve_jar server/mirth-core.jar server/mirth-client-core.jar)"
MIRTH_CLIENT_JAR="$(resolve_jar client/mirth-client.jar)"
MIRTH_CLIENT_CORE_JAR="$(resolve_jar client/mirth-core.jar client/mirth-client-core.jar)"
JUNIT_JAR="$(resolve_jar test/junit-4.13.2.jar test/junit.jar)"
HAMCREST_JAR="$(resolve_jar test/hamcrest-core-1.3.jar test/hamcrest-core.jar)"

log "Found mirth-server.jar:      $MIRTH_SERVER_JAR"
log "Found donkey-server.jar:     $DONKEY_SERVER_JAR"
log "Found mirth-core.jar:        $MIRTH_CORE_JAR"
log "Found mirth-client.jar:      $MIRTH_CLIENT_JAR"
log "Found mirth-client-core.jar: $MIRTH_CLIENT_CORE_JAR"
log "Found junit.jar:             $JUNIT_JAR"
log "Found hamcrest-core.jar:     $HAMCREST_JAR"

# Build classpaths.
SERVER_CP="$MIRTH_SERVER_JAR:$DONKEY_SERVER_JAR:$MIRTH_CORE_JAR:$MIRTH_CLIENT_CORE_JAR"
# Client classpath — includes miglayout and log4j if present (the Mirth
# Administrator UI ships these jars; they are required by the settings
# panel's parent class AbstractSettingsPanel even though the panel itself
# only uses standard Swing components).
CLIENT_CP="$MIRTH_CLIENT_JAR:$MIRTH_CLIENT_CORE_JAR"
for extra in miglayout-swing.jar miglayout-core.jar miglayout-3.7.4.jar log4j-1.2-api-2.17.2.jar log4j-api-2.17.2.jar log4j-core-2.17.2.jar swingx.jar swing-worker.jar; do
    if [ -f "$LIBS_DIR/client/$extra" ]; then
        CLIENT_CP="$CLIENT_CP:$LIBS_DIR/client/$extra"
    fi
done
TEST_CP="$SERVER_CP:$JUNIT_JAR:$HAMCREST_JAR"

# ---------------------------------------------------------------------------
# Clean + prepare output dirs
# ---------------------------------------------------------------------------
log "Cleaning previous build artifacts"
rm -rf "$OUT_DIR"
mkdir -p "$BUILD_DIR/shared" "$BUILD_DIR/server" "$BUILD_DIR/client" "$BUILD_DIR/test"

# ---------------------------------------------------------------------------
# Compile shared module
# ---------------------------------------------------------------------------
log "Compiling shared module"
"$JAVAC" $JAVAC_OPTS -encoding UTF-8 -d "$BUILD_DIR/shared" \
    -sourcepath "$PROJECT_DIR/shared/src" \
    $(find "$PROJECT_DIR/shared/src" -name '*.java')

# ---------------------------------------------------------------------------
# Compile server module (depends on shared)
# ---------------------------------------------------------------------------
log "Compiling server module"
"$JAVAC" $JAVAC_OPTS -encoding UTF-8 \
    -cp "$BUILD_DIR/shared:$SERVER_CP" \
    -d "$BUILD_DIR/server" \
    -sourcepath "$PROJECT_DIR/server/src" \
    $(find "$PROJECT_DIR/server/src" -name '*.java')

# ---------------------------------------------------------------------------
# Compile client module (depends on shared)
# ---------------------------------------------------------------------------
log "Compiling client module"
"$JAVAC" $JAVAC_OPTS -encoding UTF-8 \
    -cp "$BUILD_DIR/shared:$CLIENT_CP" \
    -d "$BUILD_DIR/client" \
    -sourcepath "$PROJECT_DIR/client/src" \
    $(find "$PROJECT_DIR/client/src" -name '*.java')

# ---------------------------------------------------------------------------
# Compile + run tests (unless SKIP_TESTS=1)
# ---------------------------------------------------------------------------
if [ "${SKIP_TESTS:-0}" = "1" ]; then
    warn "SKIP_TESTS=1 — skipping test compilation and execution"
else
    log "Compiling test module"
    "$JAVAC" $JAVAC_OPTS -encoding UTF-8 \
        -cp "$BUILD_DIR/shared:$BUILD_DIR/server:$TEST_CP" \
        -d "$BUILD_DIR/test" \
        -sourcepath "$PROJECT_DIR/test/src" \
        $(find "$PROJECT_DIR/test/src" -name '*.java')

    log "Running tests"
    "$JAVA_BIN" -cp "$BUILD_DIR/shared:$BUILD_DIR/server:$BUILD_DIR/test:$TEST_CP" \
        org.junit.runner.JUnitCore \
        com.bitdreamit.connect.plugins.datatypes.astm.test.ASTME1394RoundTripTest
fi

# ---------------------------------------------------------------------------
# Generate JAR manifests with version metadata
# ---------------------------------------------------------------------------
MANIFEST_DIR="$OUT_DIR/manifest"
mkdir -p "$MANIFEST_DIR"

cat > "$MANIFEST_DIR/shared-manifest.mf" <<EOF
Manifest-Version: 1.0
Created-By: bitdreamit-astm-e1394-datatype build.sh ${PLUGIN_VERSION}
Implementation-Title: ASTM E1394 Data Type (shared)
Implementation-Version: ${PLUGIN_VERSION}
Implementation-Vendor: Bit Dream IT
Implementation-URL: https://bitdreamit.com
Specification-Title: ASTM E1394-91/97
Specification-Version: E1394
Mirth-Connect-Plugin-Type: SHARED
Mirth-Connect-Plugin-Name: ASTM E1394
Mirth-Connect-Plugin-Version: ${PLUGIN_VERSION}
Mirth-Connect-Compatible-Versions: 4.0.0+
EOF

cat > "$MANIFEST_DIR/server-manifest.mf" <<EOF
Manifest-Version: 1.0
Created-By: bitdreamit-astm-e1394-datatype build.sh ${PLUGIN_VERSION}
Implementation-Title: ASTM E1394 Data Type (server)
Implementation-Version: ${PLUGIN_VERSION}
Implementation-Vendor: Bit Dream IT
Implementation-URL: https://bitdreamit.com
Specification-Title: ASTM E1394-91/97
Specification-Version: E1394
Mirth-Connect-Plugin-Type: SERVER
Mirth-Connect-Plugin-Name: ASTM E1394
Mirth-Connect-Plugin-Version: ${PLUGIN_VERSION}
Mirth-Connect-Compatible-Versions: 4.0.0+
EOF

cat > "$MANIFEST_DIR/client-manifest.mf" <<EOF
Manifest-Version: 1.0
Created-By: bitdreamit-astm-e1394-datatype build.sh ${PLUGIN_VERSION}
Implementation-Title: ASTM E1394 Data Type (client)
Implementation-Version: ${PLUGIN_VERSION}
Implementation-Vendor: Bit Dream IT
Implementation-URL: https://bitdreamit.com
Specification-Title: ASTM E1394-91/97
Specification-Version: E1394
Mirth-Connect-Plugin-Type: CLIENT
Mirth-Connect-Plugin-Name: ASTM E1394
Mirth-Connect-Plugin-Version: ${PLUGIN_VERSION}
Mirth-Connect-Compatible-Versions: 4.0.0+
EOF

# ---------------------------------------------------------------------------
# Package SHARED JAR (shared classes only — loaded by both server & client)
#
# JAR name follows the Mirth Connect 4.5.x convention used by the built-in
# data-type plugins (e.g. datatype-hl7v2-shared.jar, datatype-dicom-shared.jar).
# The extension folder name in $MIRTH_HOME/extensions/ must match the `path`
# attribute in plugin.xml — i.e. `datatype-astm-e1394`.
# ---------------------------------------------------------------------------
SHARED_JAR="$OUT_DIR/datatype-astm-e1394-shared.jar"
log "Packaging shared JAR: $SHARED_JAR"
"$JAR" cfm "$SHARED_JAR" "$MANIFEST_DIR/shared-manifest.mf" \
    -C "$BUILD_DIR/shared" .

# ---------------------------------------------------------------------------
# Package SERVER JAR (server classes only — shared classes are in shared.jar)
# ---------------------------------------------------------------------------
SERVER_JAR="$OUT_DIR/datatype-astm-e1394-server.jar"
log "Packaging server JAR: $SERVER_JAR"
"$JAR" cfm "$SERVER_JAR" "$MANIFEST_DIR/server-manifest.mf" \
    -C "$BUILD_DIR/server" .

# ---------------------------------------------------------------------------
# Package CLIENT JAR (client classes only — shared classes are in shared.jar)
# ---------------------------------------------------------------------------
CLIENT_JAR="$OUT_DIR/datatype-astm-e1394-client.jar"
log "Packaging client JAR: $CLIENT_JAR"
"$JAR" cfm "$CLIENT_JAR" "$MANIFEST_DIR/client-manifest.mf" \
    -C "$BUILD_DIR/client" .

# ---------------------------------------------------------------------------
# Copy unified plugin.xml to output (standalone, not inside any JAR)
# ---------------------------------------------------------------------------
PLUGIN_XML="$OUT_DIR/plugin.xml"
log "Copying unified plugin.xml: $PLUGIN_XML"
cp "$PROJECT_DIR/server/resources/plugin.xml" "$PLUGIN_XML"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
log "Build complete."
echo
echo "Output artifacts:"
echo "  $SHARED_JAR"
echo "  $SERVER_JAR"
echo "  $CLIENT_JAR"
echo "  $PLUGIN_XML"
echo
echo "Install into Mirth Connect:"
echo "  mkdir -p \$MIRTH_HOME/extensions/datatype-astm-e1394"
echo "  cp $SHARED_JAR  \$MIRTH_HOME/extensions/datatype-astm-e1394/"
echo "  cp $SERVER_JAR  \$MIRTH_HOME/extensions/datatype-astm-e1394/"
echo "  cp $CLIENT_JAR  \$MIRTH_HOME/extensions/datatype-astm-e1394/"
echo "  cp $PLUGIN_XML  \$MIRTH_HOME/extensions/datatype-astm-e1394/plugin.xml"
echo "  # Restart Mirth Connect service."
