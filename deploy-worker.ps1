# Deploys cloudflare-worker.js to your Cloudflare account and sets required secrets.
# Usage after pasting keys:
#   powershell -ExecutionPolicy Bypass -File deploy-worker.ps1 `
#     -ApiToken "CLOUDFLARE_API_TOKEN" `
#     -AccountId "CLOUDFLARE_ACCOUNT_ID" `
#     -ImgbbKey "IMGBB_API_KEY" `
#     -ResendKey "RESEND_API_KEY"
param(
    [Parameter(Mandatory=$true)][string]$ApiToken,
    [Parameter(Mandatory=$true)][string]$AccountId,
    [Parameter(Mandatory=$false)][string]$ImgbbKey,
    [Parameter(Mandatory=$false)][string]$ResendKey
)
$ErrorActionPreference = 'Stop'
Set-Location 'D:\workspace\tinypaws'

$wrangler = 'C:\Users\Bochra.R\AppData\Local\Temp\opencode\wrangler-tool\node_modules\wrangler\bin\wrangler.js'
if (-not (Test-Path $wrangler)) { Write-Host 'wrangler not installed - run npm i -g wrangler'; exit 1 }

$env:CLOUDFLARE_API_TOKEN = $ApiToken
$env:CLOUDFLARE_ACCOUNT_ID = $AccountId

Write-Host '==> Deploying worker...'
& node $wrangler deploy --name tinypaws-email --compatibility-date 2025-01-01
if ($LASTEXITCODE -ne 0) { Write-Host 'DEPLOY FAILED'; exit 1 }

if ($ImgbbKey) {
    Write-Host '==> Setting IMGBB_API_KEY secret...'
    $ImgbbKey | & node $wrangler secret put IMGBB_API_KEY --name tinypaws-email
}

if ($ResendKey) {
    Write-Host '==> Setting RESEND_API_KEY secret...'
    $ResendKey | & node $wrangler secret put RESEND_API_KEY --name tinypaws-email
}

Write-Host '==> Verifying endpoint...'
try {
    Invoke-WebRequest -Uri 'https://tinypaws-email.bochra0rhayem.workers.dev' -Method POST -ContentType 'application/json' -Body '{}' -UseBasicParsing -TimeoutSec 30 | Out-Null
} catch {
    Write-Host ("no-auth probe -> HTTP {0} (401 expected = live & auth-gated)" -f [int]$_.Exception.Response.StatusCode)
}
Write-Host 'DONE. Worker deployed with secrets configured.'
