# 05 – Quy trình kiểm tra

## 1. Selftest trong app (54 bước ở 0.0.6)
- **Android:** `adb shell am start -S -W -n com.appliedastrology.tuvi/.MainActivity --ez selftest true`
  - Kết quả: `/sdcard/Android/data/com.appliedastrology.tuvi/files/selftest/results.json` + ảnh PNG từng bước + `steps.jsonl`.
- **Windows:** `AppliedAstrology.exe --selftest --out <thư mục> [--compat win7]`.
  - Bản appx dùng tệp cờ `%APPDATA%\AppliedAstrology\selftest.flag` (nội dung = thư mục kết quả).
- **Nhóm bước:**
  - 01–36 chức năng: lá số, cung, nhập Âm lịch, QR, PNG, hồ sơ, thư viện, đọc bài, tìm kiếm có/không dấu, trợ lý, giao diện, ngôn ngữ, bố cục, sao lưu/khôi phục, kiểm tra mạng & VPN (23), Âm Lịch (24–27), nền giấy, giới thiệu, kiểm tra/tải cập nhật (30–31), nhật ký, báo lỗi, vùng an toàn (34), ngôn ngữ mới, khôi phục mặc định.
  - 37–45 Liquid Glass (liquid/frost/off, thanh trượt, nền aurora, reset, công tắc), không WebView (43), đủ 68 ngôn ngữ (44).
  - 46–51 hiệu năng:
    - `46_perf_scroll`: cuộn 120 thẻ; avg/p90/max, Windows thêm `settle`.
    - `47_perf_modal`: mở hộp thoại. `48_perf_tabs`: đổi tab. `49_perf_settings`: đổi cài đặt.
    - `50_glass_stability`: đổi chế độ/nền liên tục. `51_perf_memory`: bộ nhớ.
  - 52 Âm lịch toàn dải (73.018 ngày 1900–2099).
  - **53 kết quả tìm kiếm phân trang** (40 mục, "Xem thêm" → ≥ 80).
  - **54 thư viện nạp sẵn** (Android: mở bài cuối mỗi chương; Windows: tìm kiếm sau khi `Warm()` xong < 1200 ms).
- **Ngưỡng hiệu năng:**
  - Cuộn avg < 60 ms/khung, max < 800 ms; Windows `settle` < 800 ms.
  - Mở hộp thoại trên luồng UI (Android) < 400 ms.
  - Mỗi tab < 2500 ms.
- Bước 30–31 cần mạng tới `api.github.com`. Môi trường chặn mạng (proxy cloud trả 403) thì FAIL; đó **không** phải lỗi app.
- Thêm tính năng hoặc sửa lỗi ⇒ **thêm bước selftest** cho cả Android (`SelfTest.java`) và Windows (`WinSelfTest.cs`) cùng tên.

## 2. Đo hiệu năng Android (gfxinfo)
- `Measure-Gfx` trong script:
  1. Mở tab Cài Đặt (`--es tab settings`).
  2. `dumpsys gfxinfo <pkg> reset`.
  3. Cho 12 lần `input swipe`.
  4. Đọc "Janky frames", p50/p90/p99.
- Đo **bản cũ trước khi cài đè**, đo bản mới sau selftest, trên cùng máy.
- Emulator phải chạy **GPU thật** (`-EmuGpu auto`). Với swiftshader thì mọi bản đều giật gần 100%, số đo vô nghĩa.

## 3. Đo hiệu năng Windows
- Bước 21 chạy selftest **bản portable trước** (`prev_portable_<ver-1>`) trên cùng máy. Báo cáo có bảng "Windows performance (cũ vs mới)" gồm các bước 10/46/47/48/49/51.
- Kết quả đối chứng **không** tính vào bảng PASS.

## 4. Thiết bị
| Thiết bị | Ghi chú |
|---|---|
| Samsung Galaxy Note 9 SM-N960N, Android 10, arm64 (USB, serial `281038ac31217ece`) | Máy thật duy nhất được cắm; xác nhận bằng `adb devices` |
| AVD `Medium_Phone_API_37.0` (Android 17, x86_64) | Emulator chính |
| AVD `OOS_Android6_API23` (Android 6.0, x86_64) | Kiểm minSdk 23 |
| AVD khác có sẵn: `YH_Android10_API29`, `YomiHub056_Phone_API34`, `YomiHub_TV_API34` | Chưa dùng cho dự án này |
| Windows 11 Pro x64 (máy Kiệt) + chế độ `--compat win7` | Không có máy Windows 7 thật |

## 5. Kiểm thử nâng cấp & cài đặt
- Android: cài APK bản trước → cài đè bản mới (`install -r`) → kiểm tra `versionName`.
- Windows:
  - Cài Setup.exe bản trước → `Setup.exe /S` bản mới → FileVersion phải đổi. Sau đó cài MSI, rồi cài appx (copy vào `%TEMP%`, chứng chỉ ở TrustedPeople).
  - Kiểm tra module: không được nạp `WebView2`/`mshtml`/`ieframe` (bước `windows_no_browser_modules`).

## 6. Đối chiếu engine (bước 01b)
- JS ↔ Java: 3000 lá số, 4000 ngày, 1608 lần đổi Âm→Dương.
- C# ↔ Java: trùng từng byte.
- Kính Java ↔ C#: trùng từng byte (8.828.016 byte ở 0.0.6).

## 7. Ảnh chụp & video
- Ảnh: `test_reports/<ver>/android/<thiết bị>/selftest/NN_<bước>.png`, `test_reports/<ver>/windows/<gói>/NN_<bước>.png`.
- Video:
  - Android dùng `screenrecord`, tối đa 180 s/phần, nên script ghi nhiều phần `android_test_video_partN.mp4`.
  - Windows dùng ffmpeg `gdigrab` (`windows_test_video.mp4`).
  - Video chỉ đính kèm Release.
- Gửi Kiệt vài ảnh tiêu biểu sau mỗi lượt (đã xem trước, không gửi ảnh lỗi hiển thị mà không giải thích).
- Mono ở cloud chạy được selftest Windows để bắt lỗi logic, nhưng `DrawToBitmap` của Mono chỉ vẽ nền form. Phải xem ảnh trên Windows thật.

## 8. Đọc kết quả
- `test_reports/<ver>/TEST_REPORT.md`: bảng PASS theo nền tảng + gfxinfo + hiệu năng Windows.
- `test_reports/<ver>/all_results.json` có BOM UTF-8, nên đọc bằng `encoding='utf-8-sig'`.
- Log: `grep -aE "SELFTEST|    FAIL [0-9]|\[ERR\]|gfx 0|upgrade test"` trên `build_logs/build_<ver>_*.log`.
- Kết quả 0.0.6 cuối: **54/54** trên Note 9, Android 17, Android 6.0, Windows portable/win7/Setup/MSI/appx; no-browser 1/1.
