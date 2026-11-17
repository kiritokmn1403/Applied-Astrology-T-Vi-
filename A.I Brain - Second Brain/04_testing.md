# Kiểm thử

- **Selftest (52 bước ở 0.0.5)** chạy ngay trong app:
  - Android: `am start -n com.appliedastrology.tuvi/.MainActivity --ez selftest true`. Kết quả ở `/sdcard/Android/data/com.appliedastrology.tuvi/files/selftest/results.json` kèm ảnh PNG cho từng bước.
  - Windows: `AppliedAstrology.exe --selftest --out <dir> [--compat win7]`. Bản appx dùng tệp cờ `%APPDATA%\AppliedAstrology\selftest.flag` (nội dung là thư mục kết quả).
  - Bước 01–36: chức năng (lá số, QR, PNG, hồ sơ, thư viện, tìm kiếm, trợ lý, cài đặt, ngôn ngữ, Âm Lịch, cập nhật, nhật ký, báo lỗi, vùng an toàn…).
  - Bước 37–44: Liquid Glass, không WebView, đủ 68 ngôn ngữ.
  - **Bước 45–52 (0.0.5):** công tắc Liquid Glass; `46_perf_scroll` (Choreographer, 180 khung, avg/p90/max/jank); `47_perf_modal` (thời gian mở trên luồng UI + thời gian dải viền sẵn sàng); `48_perf_tabs`; `49_perf_settings`; `50_glass_stability` (đổi chế độ/nền liên tục); `51_perf_memory`; `52_lunar_full_range` (73.018 ngày 1900–2099).
- Các bước 30–31 (kiểm tra / tải cập nhật) cần mạng tới api.github.com. Nếu môi trường chặn mạng (vd. proxy cloud trả 403) thì 2 bước này FAIL; đó không phải lỗi app.
- **Đo hiệu năng Android (0.0.5):** `Measure-Gfx` trong script – mở tab Cài Đặt (`--es tab settings`), `dumpsys gfxinfo <pkg> reset`, 12 lần `input swipe`, đọc "Janky frames" và p50/p90/p99. Đo bản cũ trước khi cài đè, đo bản mới sau selftest (selftest để lại kính mặc định = liquid) → so sánh công bằng trên cùng máy.
- **Thiết bị đã dùng:**
  - Samsung Galaxy Note 9 SM-N960N (Android 10, USB)
  - AVD `Medium_Phone_API_37.0`, `OOS_Android6_API23`
  - Windows 11 x64
- **Kiểm thử nâng cấp:** cài APK hoặc Setup của bản trước, rồi cài bản mới đè lên; kiểm tra versionName / FileVersion.
- **Đối chiếu engine:** bước `01b`. Java vs JS: 3000 lá số / 4000 ngày / 1608 lần Âm→Dương. C# phải trùng từng byte với Java; kính Java↔C# trùng từng byte.
- Video: Android dùng `screenrecord` (tối đa 180 s → script ghi nhiều phần `android_test_video_partN.mp4`), Windows dùng ffmpeg gdigrab. Video được đính kèm vào Release.
- Mono (cloud) chạy được selftest Windows để bắt lỗi logic, nhưng `DrawToBitmap` của Mono chỉ vẽ nền form → ảnh chụp không đại diện; phải xem ảnh trên Windows thật.
