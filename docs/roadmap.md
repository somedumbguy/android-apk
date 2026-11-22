# Roadmap

The improvement list, in order. Each iteration ships as a pull request and a release; tick
items here in the same pull request that delivers them.

## 1. Fix real problems first

- [x] **Don't lose a workout on rotation or when Android closes the app.** Progress is saved
      after every tap and restored when the screen comes back (v0.7).
- [x] **Rest timer that survives leaving the app.** A live countdown notification with
      Done / +30s / Skip buttons (lock-screen logging) and an alarm when rest is over (v0.8).
- [ ] **Real package name.** `com.example.hello` can't be published on Google Play and can
      never change after the first upload. *Waiting on a decision: what should it be?*
- [x] **Real workout history.** Every finished workout is logged; streak (weeks in a row
      hitting the weekly goal), the weekly ring and volume are calculated from it. Demo data
      shows only until the first real workout and is labelled DEMO (v0.9). A simple log is
      enough for these summaries; move to Room when storing every set for charts and export.

## 2. See what's built

- [x] **Screenshot tests:** Robolectric renders every screen on a simulated phone; PNGs are
      attached to each pull request (v0.10). Already caught a clipped records row and a
      volume drop shown as a win.
- [ ] **Compare screenshots against approved images** (e.g. Roborazzi), once rendering
      proves identical between machines.

## 3. Ship builds automatically

- [ ] **Builds to your phone from every merge** (Firebase App Distribution or the Play
      internal testing track).
- [ ] **Release pipeline:** R8 shrinking, Play App Signing, automatic version codes.
- [ ] **Crash reporting** (Crashlytics or Sentry).

## 4. Product

- [ ] **Programs:** A/B alternation (StrongLifts-style), an exercise library, and a
      3-question onboarding that picks a program.
- [x] **Better PRs:** estimated one-rep max (Epley) on the record cards, and more reps at
      the same weight now counts as a PR (v0.11).
- [ ] **Your gym's equipment:** kg or lb, bar weight, available plates and microplates.
- [ ] **Starting weights from one test set.**
- [ ] **Health Connect export.**
- [ ] **Set the weekly goal** in the app (currently 3).
- [ ] **Full set-by-set history** in Room, for charts, history screens and export.
- [x] **Haptic feedback** when checking off a set (v0.11).
- [ ] **Accessibility:** screen-reader labels for custom views, large-font layouts,
      contrast checks.
- [ ] **Move UI text to resources** for translation.

## 5. AI features (on-device first)

- [ ] **Voice logging:** Android's on-device speech recognition, plus a deterministic
      parser for "Squat, 3 sets of 5 at 225". No AI model needed.
- [ ] **Camera equipment recognition:** an on-device image classifier (ML Kit / LiteRT),
      with a cloud model only for equipment it doesn't know.
- [ ] Optional: Gemini Nano or Gemma on supported phones for messy phrasing.

## 6. Engineering

- [ ] **Jetpack Compose (and Kotlin)** for new screens; migrate old ones gradually.
- [ ] **Property-based tests** (jqwik) and **mutation testing** (PIT) for the progression engine.
- [ ] **Lint the domain module.**
- [ ] **Automatic dependency updates** (Renovate or Dependabot), gated by the check.
- [x] **Update deprecated GitHub Actions versions** (v0.7).
- [ ] **Error Prone and NullAway.**

## 7. Before the Play Store

- [ ] Privacy policy and Data safety form (workout data is health-related).
- [ ] Account deletion, in the app and on the web.
- [ ] Backend for sign-in, sync and backup (see docs/security.md).

## Waiting on you

- Automatic builds to your phone and crash reporting need a Firebase project or Google Play
  Console account. Tell me which, and I'll wire it up.

- Package name (see section 1).
- GitHub settings: allow auto-merge, protect `main` with the **check** requirement,
  turn on secret scanning and push protection.
