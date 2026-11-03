# Applied Astrology 0.0.3 - Test report (final build, 2026-11-03)

Bản phát hành = build lượt 4 (18:01, có biểu tượng SVG, TLS Android 6, IE11 dựng hình phần mềm).

| Target | Engine / Device | Passed | Run | Notes |
|---|---|---|---|---|
| android_emulator_Medium_Phone_API_37.0 | Android WebView, Android 17 (API 37) x86_64, edge-to-edge | 36/36 | lượt 4 (18:17) | nâng cấp 0.0.2 → 0.0.3 Success |
| android_emulator_OOS_Android6_API23 | Android WebView, Android 6.0 (API 23) x86_64 | 36/36 | lượt 4 (18:21) | tải cập nhật qua TLS dự phòng OK |
| android_device_SM-N960N (Galaxy Note 9, thiết bị thật) | Android WebView, Android 10 (API 29) arm64-v8a, DeX | 35/36 | lượt 4b (18:39) | 30/31 kiểm tra + tải **v0.0.3 thật** OK; 08_library_chapter lỗi 1 lần (màn hình thư viện bị thay đổi giữa 2 bước, nghi do thao tác trên điện thoại thật; đạt ở lượt 1, 2, 3, 4) |
| android_device_SM-N960N | 〃 | 34/36 | lượt 4 (18:07) | máy mất Internet: 23 "No Internet", 30/31 lỗi |
| android_device_25069PTEBG (Xiaomi/Redmi, Android 16) | – | không cài được | lượt 4 | INSTALL_FAILED_USER_RESTRICTED: cần bật "Cài đặt qua USB" và bấm Cho phép trên điện thoại |
| windows_portable_webview2 | Edge WebView2 156, Windows 11 x64 | 36/36 | lượt 4b (18:41) | lượt 4: 34/36 do máy mất mạng |
| windows_portable_ie11 | IE11 / MSHTML (dựng hình phần mềm) | 36/36 | lượt 4 + 4b | ảnh chụp có nội dung |
| windows_setup_exe | Edge WebView2 | 36/36 | lượt 4 + 4b | cài đè 0.0.2 → 0.0.3.0, gỡ sạch |
| windows_msi_x64 | Edge WebView2 | 36/36 | lượt 4 + 4b | |
| windows_appxbundle | Edge WebView2 | 36/36 | lượt 4 + 4b | layout x64 (developer); gói ký cần `appx-install.bat` (0x800B0109) |

Lượt 3 (build trước khi đổi biểu tượng): 8/8 cấu hình, 288/288 PASS (xem `build_logs/TEST_REPORT_round3.md` trên máy build).
Video: Release v0.0.3 (`test-video-*.mp4`).
