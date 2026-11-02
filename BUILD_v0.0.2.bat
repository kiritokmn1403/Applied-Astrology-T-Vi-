@echo off
chcp 65001 >nul
echo Applied Astrology 0.0.2 - build + test + upload GitHub Release
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_release_0.0.2.ps1" %*
echo.
echo Xong. Log trong thu muc build_logs. Nhan phim bat ky de dong.
pause >nul
