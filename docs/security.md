# Security

## The one rule

**Anything shipped in the app is public.** An APK can be unzipped and decompiled in
minutes, and obfuscation only slows that down. So the app holds no secrets and no admin
powers. Secrets and admin live on servers we control.

## Where things live

| Thing | Where it lives | Why |
| --- | --- | --- |
| Third-party API keys (Claude, etc.) | Backend, in a secret manager (e.g. Google Secret Manager) | The app calls our backend; the backend calls the provider with its own key |
| Database and service credentials | Backend, in the secret manager | Never in the app or the repository |
| User session token | On the phone, encrypted with the Android Keystore | Short-lived, belongs to one user, revocable on the server |
| App-wide settings (feature flags, AI model, tuning) | Remote config, **read-only** in the app | Admins change it elsewhere; the app ships defaults and caches the last values |
| User preferences (e.g. intensity) | On the phone | They belong to the user |
| App signing key | Google Play App Signing | We keep only an upload key, which Google can reset if it leaks |
| CI credentials | GitHub encrypted secrets, short-lived OIDC credentials where possible | Never in the repository |

Remote config is not secret: every user can read it. Never put secrets there.

## How the app proves who's calling

The backend accepts a request only when both of these check out:

1. **The user:** a Google Sign-In ID token, verified on every request.
2. **The app:** a Play Integrity or Firebase App Check token, showing the request comes from
   our real app on a real device. This blocks most scripted abuse; it doesn't replace (1).

The backend also enforces per-user rate limits and cost caps on AI calls, so one abused
account can't run up a large bill.

## Admin

Admin features are **never** in the consumer app, not even hidden. Anything in the APK can
be found and switched on.

Admin is a separate client: a web console (preferred) or a separate, privately distributed
app. Either way:

- **The server decides.** Admin endpoints check the caller's role on every request. The
  client never says who is an admin, so a leaked admin client is useless without an admin
  account.
- **Strong sign-in:** single sign-on with two-factor authentication, short sessions.
- **Audit log:** every change records who made it and when, and can be reversed.
- **Instant revocation:** removing a role on the server cuts access immediately.
- **Separate environments:** development, staging and production have separate keys and data.

A separate admin Android app is fine under these rules. Distribute it through Google Play
internal testing or as a private managed app, never the public listing, and keep it in its
own module so admin code can't ship inside Overload.

## What's enforced automatically

| Check | Where |
| --- | --- |
| No secrets committed, in any file or in git history | gitleaks in CI; also before each push when installed locally |
| No admin code in the consumer app (no `*Admin*` classes) | `ArchitectureTest` |
| No hard-coded keys, secrets, passwords or tokens in string constants | `ArchitectureTest` (catches the obvious cases; gitleaks catches the rest) |
| Signing keys and `keystore.properties` never committed | `.gitignore` |
| Sign-in data excluded from Android cloud backups and device transfers | `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml` |

Also turn on GitHub's **secret scanning** and **push protection** (Settings > Code security),
which block a push that contains a known key format.

## Current state

The app has no real secrets yet. Sign-in is a mock, there's no backend, and the test
signing key stays on the developer's machine. The planned AI features (camera recognition,
voice logging) must go through a backend as described above.
