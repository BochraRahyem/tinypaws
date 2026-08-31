# AGENTS.md

TinyPaws Diary: single-module Android app (`:app`), Kotlin + Jetpack Compose + Firebase. Namespace is `com.example`; applicationId is `com.tinypaws.app`.

## Build & verify

- No Gradle wrapper is committed. Use a local Gradle compatible with AGP 9.1.1 (Gradle 9.x) or Android Studio.
- Build: `gradle :app:assembleDebug`
- Unit tests (Robolectric): `gradle :app:testDebugUnitTest`
- Roborazzi screenshot tests: record with `gradle :app:recordRobolectricScreenshotTest`, verify with `gradle :app:verifyRobolectricScreenshotTest`. Reference PNGs live in `app/src/test/screenshots/`.
- Lint never fails the build (`abortOnError = false`). No CI configured.
- Signing gotchas:
  - Debug signing expects `debug.keystore` at the repo root; it is gitignored. If missing, generate one (keytool) or debug builds fail validation.
  - Release signing needs env vars `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`.

## Secrets & Firebase

- API keys are injected into `BuildConfig` from a root `.env` (gitignored) via the secrets Gradle plugin: `GEMINI_API_KEY`, `GROQ_API_KEY` (used by `data/GeminiClient.kt`, `GroqClient.kt`). Without `.env`, placeholder values from `.env.example` apply.
- Missing `google-services.json` only produces a WARN, not a build failure.

## Architecture

- Single-activity Compose app with no navigation library (dependency intentionally commented out): `MainActivity.kt` (~3k lines) hosts and switches all screens; shared state in `ui/TinyPawsViewModel.kt`.
- `data/`: Room DB (`AppDatabase`) for local storage plus Firestore repositories for sync/cloud features.
- i18n is custom: use `com.example.ui.stringResource(id)` / `LocalLanguage` (TranslationManager reads `assets/translations.json`), NOT `androidx.compose.ui.res.stringResource`. Languages: en/ar/es/fr.

## Backend pieces outside the app module

- `functions/`: Firebase Cloud Functions — plain JS, Node 18, Firestore triggers + one HTTPS endpoint; email uses the `RESEND_API_KEY` secret. Deploy via `firebase deploy --only functions` (predeploy runs its eslint). Rules live at root: `firestore.rules`, `storage.rules`.
- `cloudflare-worker.js`: standalone Cloudflare Worker for email via Resend (the root `package.json` `resend` dependency belongs to this, not the app).

## Repo quirks

- Root contains many one-off scratch scripts/logs from past sessions (`fix_*.sh`, `update_*.sh`, `count_*.py`, `apply_quiz_fix.py`, `build*.log`, stray `tip_of_day.kt`). Not part of the build; do not treat them as source or "fix" them.
- Unused dependencies in `app/build.gradle.kts` are intentionally kept commented out for easy re-enablement.

# Image Generation Guidelines

## Character & Scene Consistency Rule
For all DIY tutorial illustrations:
- **Consistent Characters**: Every illustration MUST feature the same main character: Lily, a young girl with two hair buns wearing a cozy beige knit sweater, and her orange tabby cat Pip.
- **Consistent Room & Theme**: Every illustration MUST take place in the same cozy craft room with shelves of pastel yarn, potted plants, and warm lighting.
- **Action Variation**: Keep the characters, art style, room, and theme identical, and only change the specific DIY step/action being performed (e.g. measuring, cutting, assembling, painting, stuffing).
- **Art Style**: Maintain the "soft pastel children's storybook illustration" art style (cozy, premium, high detail, warm lighting) across all images.
