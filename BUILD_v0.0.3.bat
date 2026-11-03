@echo off
rem Applied Astrology v0.0.3 - build + test (emulator, USB devices, Windows) + publish to GitHub
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_release_0.0.3.ps1" -Phase all
echo.
echo Log: build_logs\  - Status: build_logs\status.json
pause
