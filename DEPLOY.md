# TinyPaws — FREE-TIER deployment status (no Blaze, no Firebase Storage)

## ✅ DONE & LIVE-VERIFIED (nothing left for you here)
- Firestore rules deployed to project `tinypaws-69383` and **verified live**
- Signed release APK rebuilt with the free-tier architecture:
  `app\build\outputs\apk\release\app-release.apk` (signature verified)
- All unit tests pass
- **Firebase Storage is NOT used** — photo uploads go through Cloudflare Worker → ImgBB (free)
- **No firebase.storage SDK dependency** — reduces APK size and attack surface
- Cloud Functions are NOT used — welcome email goes through Cloudflare Worker + Resend

## ⚡ WHEN YOU HAVE THE KEYS (one command each)

1) Deploy the worker and set both required secrets (photo relay + welcome email):
```powershell
powershell -ExecutionPolicy Bypass -File deploy-worker.ps1 `
    -ApiToken "CLOUDFLARE_API_TOKEN" `
    -AccountId "CLOUDFLARE_ACCOUNT_ID" `
    -ImgbbKey "IMGBB_API_KEY" `
    -ResendKey "RESEND_API_KEY"
```

2) Done. Photo uploads + welcome emails work immediately — no APK rebuild needed.

## Image upload architecture (all $0, no Firebase Storage)

```
App → Firebase Auth token → Cloudflare Worker → ImgBB API → hosted image URL → Firestore
```

- **Cat Report photos**: via `uploadImageToStorage("cat_photos")`
- **Feeding Station photos**: via `uploadImageToStorage("feeding_stations")`
- **Rescue Story photos**: via `uploadImageToStorage("rescue_photos")`
- `deleteImageFromStorage()` removed — ImgBB manages its own retention; no Firebase Storage cleanup needed

## Feature status on the free plan
| Feature | Status |
|---|---|
| Reports, cats near me, feeding stations | ✅ fully working |
| Stars/badges/leaderboard/counters | ✅ client-side, rule-guarded |
| Global statistics | ✅ client-side increments, whitelisted fields |
| Reminders/alarms/weather alerts | ✅ fully local, unaffected |
| Backup/restore, AI chat/art/facts | ✅ unaffected |
| Photo uploads | ✅ via Cloudflare Worker + ImgBB (no Firebase Storage) |
| Welcome email | ✅ after deploy-worker.ps1 with both secrets |
| Status-change emails (helped/adopted notices) | ⏸ deferred (needs future free backend) |
| Proximity push notifications (20 km FCM) | ⏸ deferred (OneSignal or Blaze later; tokens already stored) |
