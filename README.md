# Hello World Android app

A minimal Android app with a sign-in flow. Authentication currently uses a mock
provider that signs in as a fake test user, so development can continue before
Google Sign-In is set up.

- Package: `com.example.hello`
- Min Android version: 6.0 (API 23), target API 34
- No Gradle: built directly with `aapt`, `dx` and `apksigner`

## Authentication

The app only talks to the `AuthProvider` interface in
`src/com/example/hello/auth/`:

| File | Purpose |
| --- | --- |
| `AuthProvider.java` | Interface: `getCurrentUser()`, `signIn()`, `signOut()`, `isMock()` |
| `User.java` | The signed-in user (id, display name, email) |
| `UserStore.java` | Remembers the user between launches |
| `MockAuthProvider.java` | Fake provider: Google-style sheet, signs in as `Test User` |
| `Auth.java` | Chooses the provider. **This is the only line to change.** |

To switch to real Google Sign-In, add a `GoogleAuthProvider` that implements
`AuthProvider` with Credential Manager and the Web client ID from Google Cloud
Console, then return it from `Auth.create()`.

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
