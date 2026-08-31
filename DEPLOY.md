# TinyPaws — FREE-TIER deployment status (no Blaze needed)

## ✅ DONE & LIVE-VERIFIED (nothing left for you here)
- Firestore rules deployed to project `tinypaws-69383` and **verified live**:
  public reads OK (reports/stats/publicProfiles) · /users, /mail, internal docs 403 ·
  reward counters monotonic-only · statistics writes field-whitelisted
- Signed release APK rebuilt with the free-tier architecture:
  `app\build\outputs\apk\release\app-release.apk` (signature verified)
- All unit tests pass (3/3)
- Cloud Functions are NOT used anymore — see functions/index.js header.
  Welcome email now goes through your free Cloudflare Worker + Resend.

## ⚡ WHEN YOU HAVE THE KEYS (one command each)

1) Deploy the worker + set secrets (welcome email + photo relay):
```powershell
powershell -ExecutionPolicy Bypass -File deploy-worker.ps1 `
    -ApiToken "CLOUDFLARE_API_TOKEN" `
    -AccountId "CLOUDFLARE_ACCOUNT_ID" `
    -ImgbbKey "IMGBB_API_KEY"
```
2) Done. Photo uploads + welcome emails work immediately — no APK rebuild needed.

## Feature status on the free plan
| Feature | Status |
|---|---|
| Reports, cats near me, feeding stations | ✅ fully working |
| Stars/badges/leaderboard/counters | ✅ client-side, rule-guarded |
| Global statistics | ✅ client-side increments, whitelisted fields |
| Reminders/alarms/weather alerts | ✅ fully local, unaffected |
| Backup/restore, AI chat/art/facts | ✅ unaffected |
| Welcome email | ✅ after running deploy-worker.ps1 once |
| **Photo uploads** | ✅ after deploy-worker.ps1 with `-ImgbbKey` (relay built into this APK) |
| Status-change emails (helped/adopted notices) | ⏸ deferred (needs future free backend) |
| Proximity push notifications (20 km FCM) | ⏸ deferred (OneSignal or Blaze later; tokens already stored) |
