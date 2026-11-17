# Applied Astrology 0.0.5 - Test report (2026-11-17 11:24)

| Target | Engine / Device | Passed | Notes |
|---|---|---|---|
| android_device_SM-N960N | Native Android Views (no WebView) Android 10 (API 29) arm64-v8a samsung SM-N960N | 52/52 |  |
| android_emulator_Medium_Phone_API_37.0 | Native Android Views (no WebView) Android 17 (API 37) x86_64 Google sdk_gphone64_x86_64 | 51/52 | 46_perf_scroll |
| android_emulator_OOS_Android6_API23 | Native Android Views (no WebView) Android 6.0 (API 23) x86_64 unknown Android SDK built for x86_64 | 49/52 | 37_glass_liquid, 47_perf_modal, 50_glass_stability |
| windows_portable_native | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 51/52 | 46_perf_scroll |
| windows_portable_compat_win7 | Native WinForms + GDI+ (no WebView2/IE) [compat win7] Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 51/52 | 46_perf_scroll |
| windows_no_browser_modules |  | 1/1 |  |
| windows_setup_exe | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 51/52 | 46_perf_scroll |
| windows_msi_x64 | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 51/52 | 46_perf_scroll |
| windows_appxbundle | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 51/52 | 46_perf_scroll |

## Performance (dumpsys gfxinfo, Settings screen, 12 swipes)

| Device | Version | Frames | Janky | p50 | p90 | p99 |
|---|---|---|---|---|---|---|
| device_SM-N960N | 0.0.4 | 672 | 7 (1.04%) | 7 ms | 11 ms | 17 ms |
| device_SM-N960N | 0.0.5 | 673 | 11 (1.63%) | 7 ms | 12 ms | 17 ms |
| emulator_Medium_Phone_API_37.0 | 0.0.4 | 149 | 148 (99.33%) | 125 ms | 150 ms | 200 ms |
| emulator_Medium_Phone_API_37.0 | 0.0.5 | 214 | 211 (98.6%) | 81 ms | 125 ms | 200 ms |
| emulator_OOS_Android6_API23 | 0.0.4 | 337 | 319 (94.66%) |  ms | 125 ms | 133 ms |
| emulator_OOS_Android6_API23 | 0.0.5 | 0 |  (%) |  ms | 133 ms | 133 ms |
