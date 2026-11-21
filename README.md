# Overload

A frictionless weightlifting log built around progressive overload. ("Overload" is a
working name.)

- **Home:** personal records with forecast dates for the next plate milestone, today's
  target with a "PR attempt" badge, streak, weekly goal ring and 7-day volume chart.
- **Intensity:** Cruise / Build / Send it. Changing it updates every prescription and
  forecast date live.
- **Workout:** sets are pre-filled. Tap a set if you did it as planned, or tap its reps to
  record what you actually did. Includes plate math, warm-ups and an auto-starting rest
  timer. Finishing saves the session, detects PRs and feeds the next prescription.

## Build and test

Requires JDK 17+ and the Android SDK (set `sdk.dir` in `local.properties`, or `ANDROID_HOME`).

```bash
./gradlew test             # all unit tests (domain + app), no device needed
./gradlew lintDebug        # Android Lint
./gradlew assembleDebug    # app/build/outputs/apk/debug/app-debug.apk
```

To sign debug builds with a shared key (so builds install as updates and keep the same
fingerprint for Google Sign-In), copy `keystore.properties.example` to
`keystore.properties` and point it at your key. Both files are git-ignored except the example.

## Architecture

Two modules, so the rules are enforced by the build, not by convention:

```
domain/   Pure Java. No Android. All training logic lives here.
app/      Android: storage, sign-in, screens. Depends on domain.
```

### `domain` (pure Java, unit-tested on the JVM)

| Package | Contains |
| --- | --- |
| `model` | `Exercise` (stable id, built with a builder), `SetRecord`, `Suggestion`, `PersonalRecord`, `WorkoutPlan` |
| `catalog` | The known exercises and the Push Day plan |
| `progression` | `ProgressionStrategy` interface, `LinearProgression`, `DoubleProgression`, `Intensity`, `ProgressionEngine` |
| `forecast` | `Forecaster`: projected date for the next plate milestone |
| `plates` | `PlateMath`: plates per side and warm-ups, for any bar and plate set |
| `session` | `WorkoutSession`: all set state during a workout |
| `history` | Ports: `WorkoutRepository`, `SettingsRepository` |
| `training` | `TrainingService`: the use cases (suggest, forecast, start and complete a workout) |

Test fakes (`InMemoryWorkoutRepository`, fixed clocks, `TestTraining`) live in
`domain/src/testFixtures` and are shared by both modules' tests.

### `app` (Android)

| Package | Contains |
| --- | --- |
| `app` | `AppContainer`: the composition root where everything is created and wired |
| `data` | `SharedPrefs*Repository` implementations, `SeedData` demo history |
| `auth` | `AuthProvider` interface, `MockAuthProvider` |
| `ui/home`, `ui/intensity`, `ui/workout` | A presenter (pure Java, tested) produces state; a view draws it |
| `ui/common`, `ui/widget`, `ui/format` | Shared UI building blocks, chart views and text formatting |

### Principles

- **Dependency injection without a framework.** `AppContainer` builds every object and
  passes dependencies through constructors. Activities, which Android creates, fetch it
  with `AppContainer.from(context)`. Swap an implementation there and nowhere else.
- **Interfaces where things vary:** storage, sign-in and progression schemes. A new
  scheme (5/3/1, GZCLP) is a new `ProgressionStrategy` registered in `ProgressionEngine`,
  with no changes elsewhere.
- **Deterministic code over AI** wherever an answer can be calculated: prescriptions,
  deloads, forecasts, plate math, PR detection. AI is reserved for things code can't do,
  such as recognising equipment from the camera or understanding spoken logs. Those will
  plug in behind interfaces, with a mock first, like sign-in.
- **Time is injected** (`java.time.Clock`), so tests are repeatable.
- **Views have no logic.** Presenters turn domain data into plain state objects, which are
  unit-tested; views only draw them.

## Placeholders to replace

| What | Where | Replace with |
| --- | --- | --- |
| Sign-in | `MockAuthProvider`, wired in `AppContainer` | Google Sign-In (Credential Manager) |
| Starting history | `SeedData.demo()` | Replaced per exercise once you log it |
| Streak, weekly goal, earlier days' volume | `SeedData.demo()` | Real workout history |
