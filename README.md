# Hello World Android app

A minimal Android app that greets you by your Google account. It opens the system
account picker, then shows "Hello, <account>!" and remembers the choice.

- Package: `com.example.hello`
- Min Android version: 6.0 (API 23), target API 34
- No Gradle: built directly with `aapt`, `dx` and `apksigner`

## Build

On Ubuntu:

```bash
sudo apt-get install -y android-sdk android-sdk-platform-23 apksigner dalvik-exchange
./build.sh
```

The signed APK is written to `build/HelloWorld.apk`.

## Signing key

`build.sh` creates a test key at `build/test.keystore` if none exists. Keys are not
committed. To sign with your own key:

```bash
KEYSTORE=/path/to/key.keystore KS_PASS=yourpassword ./build.sh
```

The key's SHA-1 fingerprint must match the Android OAuth client in Google Cloud
Console for Google Sign-In to work. Print it with:

```bash
keytool -list -v -keystore build/test.keystore -storepass android | grep SHA1
```
