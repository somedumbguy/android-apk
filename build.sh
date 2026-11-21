#!/usr/bin/env bash
# Builds and signs HelloWorld.apk without Gradle, using Ubuntu's Android tools:
#   sudo apt-get install -y android-sdk android-sdk-platform-23 apksigner dalvik-exchange
set -euo pipefail
cd "$(dirname "$0")"

JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
KEYSTORE=${KEYSTORE:-build/test.keystore}
KS_PASS=${KS_PASS:-android}

rm -rf build/classes build/*.apk build/classes.dex
mkdir -p build/classes

if [ ! -f "$KEYSTORE" ]; then
  echo "No keystore found; creating a test key at $KEYSTORE"
  keytool -genkeypair -keystore "$KEYSTORE" -alias hello -keyalg RSA -keysize 2048 \
    -validity 10000 -storepass "$KS_PASS" -keypass "$KS_PASS" -dname "CN=Hello Test" -noprompt
fi

javac -source 8 -target 8 -bootclasspath "$JAR" -d build/classes $(find src -name '*.java')
dalvik-exchange --dex --output=build/classes.dex build/classes
aapt package -f -M AndroidManifest.xml -I "$JAR" -F build/unsigned.apk
(cd build && aapt add unsigned.apk classes.dex >/dev/null)
zipalign -f 4 build/unsigned.apk build/aligned.apk
apksigner sign --ks "$KEYSTORE" --ks-pass "pass:$KS_PASS" --out build/HelloWorld.apk build/aligned.apk
apksigner verify build/HelloWorld.apk
echo "Built build/HelloWorld.apk"
