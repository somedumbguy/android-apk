# Overload

A frictionless weightlifting log that uses AI to predict your next weight and
help with data entry. ("Overload" is a working name.)

## What works

- **Home:** personal records with forecast dates for the next plate milestone, today's
  target with a "PR attempt" badge, streak, weekly goal ring, 7-day volume chart.
- **Intensity:** Cruise / Build / Send it. Changing it updates every prescription and
  forecast date live.
- **Workout:** sets are pre-filled. Tap ✓ if you did it as planned, or tap the reps to record
  what you actually did. Includes plate math, warm-up sets and an auto-starting rest timer.
  Finishing saves the session, detects PRs and feeds the next prescription.

## Code vs AI

Anything that can be calculated is plain, deterministic code in `engine/`:

| File | Does |
| --- | --- |
| `ProgressionEngine.java` | Next weight and reps. Linear (3×5) and double progression (3×8–12), 10% deload after 3 stalls |
| `Intensity.java` | Jump size per successful session: 2.5 / 5 / 7.5 lb (doubled for deadlifts) |
| `Forecast.java` | Projected date for the next plate milestone (135, 225, 315, 405, ...) |
| `PlateMath.java` | Plates per side and warm-up sets |

AI is reserved for what code can't do well, such as recognising gym equipment from the
camera and understanding spoken or typed logs.

## Placeholders to replace

| What | Where | Replace with |
| --- | --- | --- |
| Sign-in | `auth/MockAuthProvider.java` via `auth/Auth.java` | Google Sign-In |
| Starting history | `data/SampleData.java` | Replaced per exercise once you log it |
| Streak, weekly goal, earlier days' volume | `data/SampleData.java` | Real workout history |

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
