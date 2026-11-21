# Overload

A frictionless weightlifting log that uses AI to predict your next weight and
help with data entry. ("Overload" is a working name.)

Round one is the sign-in and home screens: an AI coach card with today's
suggested weight, streak, weekly goal ring, 7-day volume chart, a voice-logging
entry point and personal records. Buttons for features that don't exist yet
show a "coming in round two" message.

## Placeholders to replace

| What | Where | Replace with |
| --- | --- | --- |
| Sign-in | `auth/MockAuthProvider.java` via `auth/Auth.java` | Google Sign-In |
| Weight prediction | `ai/RuleBasedPredictor.java` via `ai/Predictors.java` | An AI model |
| Training history | `data/SampleData.java` | Stored workouts |

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
