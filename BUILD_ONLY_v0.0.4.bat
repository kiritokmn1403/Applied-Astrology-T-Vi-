@echo off
rem Applied Astrology v0.0.4 (native) - build only (no tests, no publish)
cd /d "%~dp0"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_release_0.0.4.ps1" -Phase build
pause
