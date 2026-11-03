@echo off
rem Applied Astrology v0.0.3 - publish only (git push + GitHub Release + verify)
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_release_0.0.3.ps1" -Phase publish
pause
