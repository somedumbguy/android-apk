# Overload: coding standards

Read this before changing code. Every rule below is checked automatically; `./gradlew check`
fails if any is broken. Run it before you finish, and `./gradlew spotlessApply` to fix
formatting.

## Rules and where they're enforced

| Standard | Rule | Enforced by |
| --- | --- | --- |
| Formatting | Palantir Java Format, no unused or wildcard imports | Spotless (`spotlessCheck`) |
| Pure domain | `domain/` is plain Java: no Android, no app code | `domain` module has no Android dependency; `ArchitectureTest` |
| Testable screen logic | Presenters, formatters, `*State` and `RestTimer` don't touch Android | `ArchitectureTest` |
| Dependency injection | Only `AppContainer` creates repositories, `TrainingService`, `Forecaster` and the auth provider | `ArchitectureTest` |
| Swappable storage | `ui/` never uses `data/` classes; go through the domain's repository interfaces | `ArchitectureTest` |
| Views have no logic | `*View` and `*Sheet` classes don't call use cases; they draw presenter state | `ArchitectureTest` |
| Injected time | Never read the system clock (`Instant.now()`, `LocalDate.now()`, `System.currentTimeMillis()`...). Take a `java.time.Clock` | `ArchitectureTest` |
| DRY | No copy-pasted blocks (70+ tokens) | PMD CPD (`cpdCheck`) |
| No warnings | Java compiler warnings are errors | `-Werror` |
| Android correctness | Any Android Lint warning fails the build | Lint (`warningsAsErrors`) |
| Tested | Domain coverage at least 90% of lines and 80% of branches | JaCoCo (`jacocoTestCoverageVerification`) |
| No secrets in the app | API keys and credentials live on the backend; no key, secret, password or token string constants | gitleaks (CI, pre-push); `ArchitectureTest` |
| No admin in the app | Admin is a separate client with server-side roles; no `*Admin*` classes here | `ArchitectureTest` |

## Where it runs

1. **Locally:** `./gradlew check`.
2. **Git hooks** in `.githooks/`: formatting before each commit, the full check before each
   push. Any Android build installs them (`installGitHooks`).
3. **Claude Code:** a Stop hook (`.claude/hooks/check-standards.sh`) runs the check when
   code has uncommitted changes and won't let Claude finish while it fails.
4. **GitHub Actions** (`.github/workflows/ci.yml`): the same check on every push and pull
   request. This is the gate that can't be skipped.

## How to add things

- **A progression scheme:** implement `ProgressionStrategy` in `domain/progression` and
  register it in `ProgressionEngine.standard()`. Add tests.
- **A screen:** a `*Presenter` (pure Java, unit-tested) produces a state object, and a
  `*View` draws it. Wire the presenter in `AppContainer`.
- **An AI feature:** define an interface (for example `EquipmentRecognizer`), ship a mock
  first, and wire it in `AppContainer`, the way `MockAuthProvider` works.
- **Prefer deterministic code to AI** wherever an answer can be calculated.

## Seeing the screens

`./gradlew :app:testDebugUnitTest --tests '*ScreenshotTest*'` renders every screen to
`app/build/screenshots/`. Look at them after any UI change; CI attaches them to each pull
request. Add a test there for each new screen.

## Changing a rule

Rules change only when the design deliberately changes. Edit the rule and its reason in
`ArchitectureTest` or the build file, and update this table in the same change.

## Security

Anything in the APK is public. Read [docs/security.md](docs/security.md) before adding
anything that talks to a server, stores credentials or changes app-wide settings. In short:
secrets live on the backend, app-wide settings are read-only remote config, and admin is a
separate client whose permissions the server checks on every request.

## Store listing

Keep `docs/play-store-listing.md` in sync: don't list features the app doesn't have.
