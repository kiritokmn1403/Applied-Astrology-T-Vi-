# Applied Astrology 0.0.6 - Test report (2026-11-18 18:40)

| Target | Engine / Device | Passed | Notes |
|---|---|---|---|
| android_device_SM-N960N | Native Android Views (no WebView) Android 10 (API 29) arm64-v8a samsung SM-N960N | 54/54 |  |
| android_emulator_Medium_Phone_API_37.0 | Native Android Views (no WebView) Android 17 (API 37) x86_64 Google sdk_gphone64_x86_64 | 54/54 |  |
| android_emulator_OOS_Android6_API23 | Native Android Views (no WebView) Android 6.0 (API 23) x86_64 unknown Android SDK built for x86_64 | 54/54 |  |
| windows_portable_native | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 54/54 |  |
| windows_portable_compat_win7 | Native WinForms + GDI+ (no WebView2/IE) [compat win7] Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 54/54 |  |
| windows_no_browser_modules |  | 1/1 |  |
| windows_setup_exe | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 54/54 |  |
| windows_msi_x64 | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 54/54 |  |
| windows_appxbundle | Native WinForms + GDI+ (no WebView2/IE) Windows 11 Pro (build 26300) 64-bit  KIRITOKMN_LAP | 54/54 |  |

## Performance (dumpsys gfxinfo, Settings screen, 12 swipes)

| Device | Version | Frames | Janky | p50 | p90 | p99 |
|---|---|---|---|---|---|---|
| device_SM-N960N | 0.0.5 | 671 | 85 (12.67%) | 7 ms | 17 ms | 23 ms |
| device_SM-N960N | 0.0.6 | 650 | 37 (5.69%) | 7 ms | 14 ms | 19 ms |
| emulator_Medium_Phone_API_37.0 | 0.0.5 | 495 | 36 (7.27%) | 23 ms | 34 ms | 48 ms |
| emulator_Medium_Phone_API_37.0 | 0.0.6 | 529 | 42 (7.94%) | 28 ms | 38 ms | 53 ms |
| emulator_OOS_Android6_API23 | 0.0.5 | 612 | 91 (14.87%) |  ms | 19 ms | 38 ms |
| emulator_OOS_Android6_API23 | 0.0.6 | 616 | 0 (0%) |  ms | 7 ms | 10 ms |

## Windows performance (0.0.5 portable vs 0.0.6 portable, same machine)

| Step | 0.0.5 | 0.0.6 |
|---|---|---|
| 10_search | results=599 2342ms | results=599 506ms |
| 46_perf_scroll | frames=60 avg=94.6ms p90=102.7ms max=111.8ms bgBuilds=469 range=11698px | frames=60 avg=39.2ms p90=46.8ms max=58.7ms settle=99.6ms bgBuilds=194 held=1044 range=11740px cards=120 |
| 47_perf_modal | open+paint=367ms size=620x712 liquid=True | open+paint=444ms size=620x712 liquid=True |
| 48_perf_tabs | max=1955ms chart=286ms calendar=617ms library=1955ms assistant=170ms saved=65ms settings=302ms about=252ms  | max=745ms chart=330ms calendar=745ms library=346ms assistant=164ms saved=65ms settings=370ms about=299ms  |
| 49_perf_settings | applySettings=416ms/533ms resize=684ms nebula=22ms builds=10 | applySettings=455ms/406ms resize=428ms nebula=20ms builds=10 |
| 51_perf_memory | managed=36MB workingSet=142MB private=116MB bgBuilds=829 | managed=36MB workingSet=139MB private=114MB bgBuilds=554 |
