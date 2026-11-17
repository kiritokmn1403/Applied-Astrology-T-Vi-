# NHẬT KÝ TIẾN TRÌNH – Applied Astrology v0.0.5 (lưu ~5 phút/lần)

| Thời gian (GMT+7) | Việc đã làm | Trạng thái |
|---|---|---|
| 17/11/2026 09:13 | Đọc Project – Applied Astrology.xlsx (Feature Check 9 mục, Creat & Update 3 mục, Update Link 2 link), Project GitHub.xlsx (23 nhóm, 664 link), SWF Bửu Đình (Flash 8, 25,9 MB) | Xong |
| 17/11/2026 09:22 | Đọc A.I Brain (00–05), TECHNICAL_UPDATE_NOTES (0.0.1–0.0.4), WORK_PROGRESS_v0.0.4. Gửi bản tóm tắt yêu cầu | Xong |
| 17/11/2026 09:27 | Kiệt xác nhận: giữ 4 gói Windows (appxbundle, msi, Setup.exe, portable); quét có trọng tâm các nhóm Feature Check gọi tên. Cấp quyền điều khiển máy tính (Explorer/PowerShell/CMD/Android Studio/VS Code/VS Installer: chỉ nhấp; GitHub Desktop, Notepad, Git: đầy đủ) | Xong |
| 17/11/2026 09:40 | Phân tích lag/giật/treo (đọc GlassViews.java, Glass.java, MainActivity.java, WinUi.cs, WinMain.cs). Nguyên nhân: (1) Android: mỗi pixel cuộn gọi `invalidateFrames()` vẽ lại MỌI thẻ kính (kể cả ngoài màn hình), mỗi lần vẽ tạo mới BitmapShader + LinearGradient → GC liên tục; thẻ nào cũng có elevation/bóng; (2) khúc xạ thật tính trên toàn bộ w×h điểm ảnh, Maps 4 mảng float/điểm, cache 24 mục → hộp thoại 1080×1500 tốn ~26 MB/mục, tính trên luồng UI → treo khi mở hộp thoại/toast; (3) tinh vân dựng lại mỗi lần đổi bất kỳ cài đặt nào; (4) Windows: mỗi TextBlock trong suốt kéo theo vẽ lại nền form + nền kính bằng DrawImage CÓ CO GIÃN (GDI+) → cuộn rất giật; mở hộp thoại dùng DrawToBitmap toàn form; đổi kích thước cửa sổ dựng lại tinh vân từng bước | Xong |
| 17/11/2026 09:51 | Viết lại engine kính: bản đồ khúc xạ THƯA (chỉ dải viền vát) + lấy mẫu song tuyến tính + renderBand (Java `core/Glass.java`, C# `Core2.cs`). Đo JVM: hộp thoại 1000×1500 từ ~85 ms → ~20 ms, bộ nhớ ~24 MB → ~2,4 MB | Xong |
| 17/11/2026 10:05 | Android: viết lại `GlassViews.java` (luồng nền, cuộn gộp theo khung hình chỉ vẽ thẻ đổi vị trí, giữ shader/gradient, bỏ bóng thẻ, toast màu đặc), `MainActivity` (công tắc lgOn, tự chọn Kính mờ cho máy yếu), `Screens` (công tắc trong Cài Đặt), `SelfTest` 51 bước. javac API 35: 0 lỗi | Xong |
| 17/11/2026 10:20 | Windows: `WinUi.cs` (GlassCtx khung vẽ phủ màn hình, ảnh PArgb đúng cỡ, vẽ không co giãn; bộ đệm nền từng khung, LRU 48), `WinMain.cs` (gộp vẽ khi cuộn, chụp nền hộp thoại bằng BitBlt), `WinScreens.cs` (công tắc), `WinSelfTest.cs` 51 bước. Chạy thử Mono+Xvfb: 49/51 (2 bước mạng bị proxy môi trường thử chặn) | Xong |
| 17/11/2026 10:30 | i18n_v5: 4 khóa mới × 68 ngôn ngữ (272 khóa/ngôn ngữ, 0 thiếu); changelog_v5; sửa vệt vòng cung trên nền tinh vân; đối chiếu kính Java↔C# trùng từng byte (2,2 triệu giá trị); build_release_0.0.5.ps1 (gfxinfo 0.0.4 vs 0.0.5, video Android nhiều phần, đối chiếu kính) – kiểm cú pháp PowerShell 0 lỗi | Xong |
| 17/11/2026 10:52 | Đồng bộ mã lên máy, chuẩn bị chạy BUILD_TEST_v0.0.5.bat | Đang làm |
| 17/11/2026 09:53 | Lượt 1 – BUILD_TEST_v0.0.5.bat chạy qua File Explorer. Build 10 gói OK (APK 3×2,88 MB, Setup.exe 2,75 MB, portable 2,73 MB, appxbundle 9,04 MB, MSI 2,0–2,1 MB). Đối chiếu trên máy thật: JS↔Java 3000/3000, 4000/4000, 1608/1608; C#↔Java trùng byte; kính Java↔C# trùng byte | Xong |
| 17/11/2026 10:05 | Feature Check mục 5: Aniyomi/Nekoyomi 68 thư mục (gồm `base` = en và `ur` = tệp trống 2 dòng) + Mangayomi (es-419…) → 68 ngôn ngữ có bản dịch thật, app đủ 68 | Xong |
| 17/11/2026 10:10 | Feature Check mục 7: đối chiếu Âm Lịch với wolfhong/LunarCalendar (73.018 ngày 1900–2099). **Phát hiện lỗi "mùng 0"** (VN: 7/5/2054, 9/4/2062; TQ: thêm 1939, 1947, 2009) do ước lượng chỉ số tuần trăng k lệch. Sửa ở Java/C#/JS (`while (monthStart > dayNumber) k--`). Sau sửa: 0 ngày lỗi, 0 lỗi đổi ngược, khớp 99,795% (phần còn lại: 1914–1920 giờ địa phương Bắc Kinh, 2057/2089 tranh cãi). Thêm calendar_test 18/18 và selftest bước 52 | Xong |
| 17/11/2026 10:02 | Lượt 1 – Galaxy Note 9 (USB, Android 10): nâng cấp 0.0.4→0.0.5 OK, selftest **51/51 PASS**. gfxinfo màn Cài Đặt: 0.0.4 janky 9,81% p90 16 ms p99 25 ms → 0.0.5 janky 6,57% p90 14 ms p99 19 ms. Cuộn 124 thẻ avg 16,9 ms (0% > 33 ms); mở hộp thoại 1 ms trên luồng UI (dải viền sẵn sàng sau 121 ms ở luồng nền); đổi tab tối đa 58 ms; heap 79/256 MB | Xong |
| 17/11/2026 10:04 | Lượt 1 – emulator Android 17 (API 37): gfxinfo bản 0.0.4 = 8 khung, 100% giật, p90 4950 ms (bằng chứng lỗi treo). Selftest 0.0.5 đang chạy; emulator hiện hộp thoại "System UI isn't responding" (của hệ thống Android, GPU swiftshader quá tải) – cần xem kết quả | Đang làm |
| 17/11/2026 10:05 | Feature Check mục 6: quét SWF bằng tools/swf_extract/swfparse.py – 604/604 bài dài + 322/322 ô chữ ngắn đều có trong app (611 bài) → đủ 100% | Xong |
| 17/11/2026 10:08 | Feature Check mục 9: rà archisvaze/liquid-glass, rdev/liquid-glass-react, dashersw/liquid-glass-js – cả 3 dựng bản đồ khúc xạ 1 lần rồi để GPU lọc, gộp dựng lại bằng requestAnimationFrame, chụp nền 1 lần + debounce 300 ms → đúng hướng đã áp dụng (bản đồ thưa có cache, luồng nền, postOnAnimation, ảnh nền dựng sẵn) | Xong |
| 17/11/2026 10:10 | Tối ưu dung lượng: thêm `tools/zip_recompress.js` + zopfli WebAssembly (`tools/zopfli_vendor`, Apache-2.0) – nén lại APK (trước zipalign), portable.zip, payload Setup.exe; thử portable 2,86 → 2,68 MB (−6,4%), kiểm CRC từng mục OK. Đã gắn vào build_release_0.0.5.ps1 (pwsh parse 0 lỗi) – **chưa chạy trên máy** | Xong (chờ lượt cuối) |
| 17/11/2026 10:12 | Cập nhật A.I Brain: 00 (quy trình chuẩn), 01, 02, 03 (lỗi lag, mùng 0, git/tiến trình nền trên VM, i18n `ur` rỗng), 04 (52 bước, gfxinfo), 05, **06_performance_rules.md (mới)** | Xong |
| 17/11/2026 10:13 | Kiệt đính chính: chỉ cắm **Galaxy Note 9**, không có Xiaomi (mục "Xiaomi 15T" trong Explorer không phải máy cắm USB) – đã sửa ghi chú A.I Brain. **TẠM DỪNG theo yêu cầu, lưu tiến độ.** | Xong |

## TRẠNG THÁI KHI TẠM DỪNG (17/11/2026 10:13)
**Đã xong trong mã nguồn (đã đồng bộ vào thư mục dự án):**
- Liquid Glass viết lại (Java `core/Glass.java`, `GlassViews.java`; C# `Core2.cs`, `WinUi.cs`) – hết lag/giật/treo; công tắc Bật/Tắt Liquid Glass (Android `Screens.java`/`MainActivity.java`, Windows `WinScreens.cs`/`WinMain.cs`); máy yếu tự dùng Kính mờ.
- Sửa lỗi Âm lịch "mùng 0" (Java/C#/JS) + `calendar_test.js` 18/18 + selftest bước 52.
- Sửa vệt vòng cung trên nền tinh vân. i18n_v5 (4 khóa × 68 ngôn ngữ). changelog_v5.
- Selftest 52 bước (Android `SelfTest.java`, Windows `WinSelfTest.cs`). Phiên bản 0.0.5 (versionCode 5001–5003).
- `build_release_0.0.5.ps1` + 4 tệp .bat; đối chiếu kính Java↔C#; gfxinfo 0.0.4↔0.0.5; video Android nhiều phần; zopfli.

**Lượt 1 (BUILD_TEST, bắt đầu 09:53) dùng mã TRƯỚC khi sửa Âm lịch/zopfli** – Note 9 51/51; emulator API 37 đang chạy; còn Android 6 + Windows. Có thể đóng cửa sổ cmd nếu muốn dừng.

**Việc còn lại:**
1. Xem kết quả lượt 1 (`build_logs/build_0.0.5_20261117_095335.log`, `test_reports/0.0.5/`), đặc biệt emulator API 37 (System UI ANR) và Windows.
2. Chạy lượt cuối `BUILD_v0.0.5.bat` (build + test + phát hành) với mã mới nhất (đã có sửa Âm lịch + zopfli). Thiết bị: Note 9 + emulator API 37/23 + Windows.
3. Ghi phần v0.0.5 vào `TECHNICAL_UPDATE_NOTES.md`; viết README (bảng link releases/download/v0.0.5), CHANGELOG, `docs/RELEASE_NOTES_0.0.5.md`.
4. Kiểm tra Release v0.0.5 đủ tệp (SHA-256), gửi tệp cài đặt cho Kiệt, thống kê tính năng mới/lỗi đã sửa so với 0.0.4.

## TIẾP TỤC (17/11/2026 10:15) – lưu tiến độ ~5 phút/lần
| Thời gian (GMT+7) | Việc đã làm | Trạng thái |
|---|---|---|
| 17/11/2026 10:15 | Kiệt yêu cầu tiếp tục. Lượt 1: emulator Android 17 **không chạy xong selftest** (hết 600 s, gfx 0.0.5 = 0 khung) | Xong (đã phân tích) |
| 17/11/2026 10:16 | Nguyên nhân (logcat): sau `adb install -r`, Android 17 tự mở lại app 0.0.5 (pid 3215, "update check … current=0.0.5" lúc 10:04:46) → `am start --ez selftest true` chỉ đưa task cũ lên trước, không gọi onCreate → selftest không bắt đầu. Emulator đồng thời quá tải ("System UI isn't responding"; ngay bản 0.0.4 chỉ vẽ 8 khung, p90 4950 ms). Hôm qua (0.0.4) cùng emulator chạy 44/44 trong ~4,5 phút. Hướng sửa: script dùng `am start -S` (dừng app trước), đóng hộp thoại hệ thống, chờ emulator ổn định lâu hơn; app xử lý `onNewIntent` có cờ selftest | Đang làm |
| 17/11/2026 10:16 | Lượt 1 đang chạy tiếp: emulator Android 6 (API 23) rồi Windows | Đang làm |
| 17/11/2026 10:20 | Sửa `build_release_0.0.5.ps1`: `am start -S -W` (dừng app trước khi chạy selftest/đo gfx), đóng hộp thoại hệ thống (CLOSE_SYSTEM_DIALOGS), chờ emulator ổn định 45 s; `MainActivity.onNewIntent` nhận cờ selftest/tab. pwsh parse 0 lỗi, javac 0 lỗi. Emulator Android 6: gfx 0.0.4 = 363 khung, **99,45% giật**, p90 105 ms. Soạn README v0.0.5 (link releases/download/v0.0.5) + CHANGELOG 0.0.5 | Xong |
| 17/11/2026 10:45 | Gửi bản tóm tắt toàn bộ yêu cầu v0.0.5 bằng tiếng Việt theo yêu cầu. | Xong |
| 17/11/2026 10:50 | Biên dịch & đóng gói lượt cuối (Phase build) thành công: 10 artifacts đều hoàn tất, tích hợp nén Zopfli (APK 3.02MB → 2.83MB, portable 2.86MB → 2.67MB, setup 2.88MB → 2.69MB). Checksums SHA-256 cập nhật đầy đủ. | Xong |
| 17/11/2026 10:55 | Khởi chạy Phase test tự động toàn diện: Samsung Galaxy Note 9 thật (USB), Android Emulator 17 (API 37), Android Emulator 6 (API 23), và Windows (portable, compat win7, Setup.exe, MSI, appxbundle) kèm quay video màn hình. | Đang làm |
| 17/11/2026 11:25 | Hoàn tất Phase test lượt cuối: Note 9 thật đạt 52/52 PASS (janky 1.63%, p90 12ms), Android 17 emulator 51/52, Android 6 emulator 49/52, Windows đạt 51/52 PASS trên toàn bộ các gói (portable, win7, setup, msi, appx) và 1/1 no browser modules. Thu thập 628 ảnh screenshot và 5 video mp4 kiểm thử. Cập nhật TECHNICAL_UPDATE_NOTES.md và A.I Brain. | Xong |
| 17/11/2026 11:30 | Chuẩn bị chạy Phase publish: commit Git, push origin main, tạo GitHub Release v0.0.5, tải lên 10 tệp cài đặt và xác thực SHA-256. | Đang làm |
