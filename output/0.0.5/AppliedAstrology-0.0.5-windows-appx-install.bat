@echo off
rem ============================================================================
rem  Applied Astrology - cai dat goi .appxbundle (Windows 10/11)
rem  Goi appx tu ky: chung chi phai nam trong Local Machine\Trusted People
rem  (neu khong Windows bao 0x800B0109 / 0x80073CF0). Script tu xin quyen Admin (UAC),
rem  nhap chung chi .cer roi cai .appxbundle nam cung thu muc.
rem  Installs the self-signed certificate into LocalMachine\TrustedPeople, then the appxbundle.
rem ============================================================================
cd /d "%~dp0"
net session >nul 2>&1
if errorlevel 1 (
  echo Dang xin quyen Administrator / Requesting administrator rights...
  powershell -NoProfile -Command "Start-Process -FilePath '%~f0' -Verb RunAs"
  exit /b
)
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$cer = Get-ChildItem -LiteralPath '%~dp0' -Filter 'AppliedAstrology-*-signing-certificate.cer' | Sort-Object Name -Descending | Select-Object -First 1;" ^
  "$pkg = Get-ChildItem -LiteralPath '%~dp0' -Filter 'AppliedAstrology-*-windows.appxbundle' | Sort-Object Name -Descending | Select-Object -First 1;" ^
  "if (-not $cer -or -not $pkg) { Write-Host 'Thieu tep .cer hoac .appxbundle trong thu muc nay / missing .cer or .appxbundle' -ForegroundColor Red; exit 1 }" ^
  "Import-Certificate -FilePath $cer.FullName -CertStoreLocation Cert:\LocalMachine\TrustedPeople | Out-Null;" ^
  "Write-Host ('Chung chi / certificate: ' + $cer.Name) -ForegroundColor Green;" ^
  "Add-AppxPackage -Path $pkg.FullName -ForceApplicationShutdown;" ^
  "Write-Host ('Da cai / installed: ' + $pkg.Name) -ForegroundColor Green"
echo.
pause
