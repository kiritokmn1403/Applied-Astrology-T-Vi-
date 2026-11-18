# Bài học kinh nghiệm (lỗi thật đã gặp)

## Android
- Thêm assets bằng `aapt2 -A` trên Windows sẽ ghi đường dẫn `assets/web\css` mà Android không đọc được. Phải tự thêm vào zip với dấu `/`.
- Truyền từng `.class` cho `d8.bat` khi có nhiều lớp sẽ lỗi "The command line is too long". Gom vào `classes.jar` trước (`jar cf`) (0.0.4).
- Android 15 (API 35) bắt buộc edge-to-edge: phải tự đệm theo `systemBars | displayCutout | ime`, nếu không nút và tab bị che (0.0.3).
- Android 6 thiếu root CA DigiCert Global Root G2 nên không tải được từ GitHub. Dùng `TlsCompat` + `assets/certs/*.pem`.
- Emoji ☾ ☯ trên tab biến thành emoji màu trên Samsung. Từ 0.0.4 dùng biểu tượng vẽ vector.
- Nhãn tab dài (tiếng Pháp "Bibliothèque") đè lên tab bên cạnh. Sửa bằng `fitLabel`: thu nhỏ chữ tới 8sp, còn thiếu chỗ thì thêm "…" (0.0.4).
- Note 9 (USB): Play Protect có thể hỏi "Gửi ứng dụng để quét". Xiaomi: phải bật "Cài đặt qua USB". Lưu ý (17/11/2026): thanh bên File Explorer có mục "Xiaomi 15T" nhưng đó **không** phải máy cắm USB – Kiệt chỉ cắm Galaxy Note 9. Thiết bị thật để thử là máy có trong `adb devices`, đừng suy ra từ Explorer.
- Bản release không cho `run-as`, nên không đọc được `/proc/<pid>/maps`. Việc "không có WebView" được kiểm bằng selftest bước 43 (đếm WebView = 0).
- **Lag/giật/treo 0.0.4 (sửa ở 0.0.5)** – nguyên nhân thật, đo được:
  - `OnScrollChangedListener` gọi `invalidateFrames()` cho **mọi** khung kính ở **mỗi pixel cuộn** (kể cả thẻ ngoài màn hình, danh sách 120+ thẻ).
  - `onDraw` tạo mới `BitmapShader` + `LinearGradient` mỗi lần vẽ → GC liên tục.
  - Mọi thẻ có `setElevation` → bóng đổ cho hàng trăm view.
  - Khúc xạ thật tính **đồng bộ trên luồng UI** cho cả w×h điểm ảnh; `Glass.Maps` 4 mảng float/điểm, cache 24 mục (hộp thoại 1342×2385 ≈ 51 MB/mục) → mở hộp thoại/toast bị đứng.
  - Đổi bất kỳ cài đặt nào cũng dựng lại tinh vân trên luồng UI.
  - Bằng chứng: emulator API 37 bản 0.0.4 – gfxinfo 8 khung, 100% giật, p90 = 4950 ms.
- `ColorMatrixColorFilter` + `BitmapShader` chạy được trên HWUI từ API 23 – dùng để GPU vẽ lõi kính thay cho CPU.

## Windows
- `Control.Visible` = false trước khi form hiện, nên đo chiều cao ra 0 và màn hình trắng. Dùng cờ `Collapsed` riêng và gọi `DoLayout` trong `Shown`.
- Đo chữ mỗi lần layout rất chậm (31 s trên Mono). Cache `PrefHeight/PrefWidth` và lan truyền `Dirty()` thì còn khoảng 1 s.
- Trùng tên với thành viên của `Form`: trường `Scale`, phương thức `Convert`. Phải gọi đầy đủ `System.Convert`.
- AppX từ chối đường dẫn junction (`D:\yh`), nên copy bundle vào `%TEMP%` rồi mới `Add-AppxPackage`.
- appx tự ký: lỗi 0x80073CF0 thực chất là 0x800B0109. Cần import `.cer` vào **LocalMachine\TrustedPeople** (đã có `appx-install.bat` tự nâng quyền).
- SmartScreen vẫn cảnh báo với mọi exe không có chứng chỉ thương mại (EV/OV). Đây **không** phải lỗi native/trình duyệt; nói rõ với người dùng.
- Khi nâng cấp từ bản WebView2, Setup.exe phải xóa `web/`, `runtimes/` và `Microsoft.Web.WebView2.*.dll` cũ.
- **Lag 0.0.4 (sửa ở 0.0.5):** mỗi điều khiển trong suốt (TextBlock, nút) khi vẽ lại kéo theo `OnPaintBackground` của cha → form vẽ `DrawImage` **có co giãn** ảnh tinh vân + khung kính vẽ `DrawImage` co giãn ×4 ảnh mờ. GDI+ co giãn rất chậm. Sửa: bitmap PArgb đúng cỡ + `DrawImageUnscaled` + bộ đệm nền từng khung. Mở hộp thoại dùng `DrawToBitmap` toàn form (chậm) → đổi sang `CopyFromScreen` (BitBlt) khi cửa sổ đang active.

## Âm lịch
- **Lỗi "mùng 0" (có từ thuật toán gốc, sửa ở 0.0.5):** `k = INT((jd - 2415021.077)/29.53)` có thể lệch 1 → `getNewMoonDay(k)` vẫn > ngày cần đổi → ngày âm = 0. VN: 7/5/2054, 9/4/2062. Sửa: `while (monthStart > dayNumber) { k--; monthStart = getNewMoonDay(k, tz); }` ở Java/C#/JS. Kiểm bằng `calendar_test.js` (quét 1900–2099) + selftest bước 52.
- Đối chiếu với `wolfhong/LunarCalendar` (pip `lunarcalendar`, lịch Trung Quốc) bằng tz = 8: khớp 99,795%; khác biệt còn lại 1914/1916/1920 (TQ trước 1929 dùng giờ địa phương Bắc Kinh) và 2057/2089 (Sóc sát nửa đêm, có tranh cãi) – **không phải lỗi**.
- Lịch VN dùng GMT+7 cho mọi năm (theo Hồ Ngọc Đức và SWF Bửu Đình). Trước 1968 miền Bắc dùng lịch GMT+8; **không đổi** vì phải giữ khớp SWF – chỉ ghi chú.

## Đa ngôn ngữ
- Aniyomi/Nekoyomi có 68 thư mục moko-resources nhưng gồm `base` (= en) và `ur` (Urdu) **rỗng** (2 dòng). Cộng `es-419` của Mangayomi → 68 ngôn ngữ có bản dịch thật. Lần sau đếm lại bằng số chuỗi, đừng chỉ đếm thư mục.

## Git / GitHub
- Người dùng có thể commit thẳng trên github.com (vd. 16/11/2026: `Add Scala CI workflow`). Khi đó push bị từ chối (non-fast-forward). **Không force-push**: `git fetch` + `git rebase origin/main` rồi mới push (bước 30 đã tự làm).
- Bước test nào đọc `results.json` thì phải xóa thư mục kết quả trước khi chạy, nếu không sẽ đọc phải kết quả cũ.
- `git credential fill` qua pipe PowerShell lỗi "missing protocol". Phải ghi thẳng vào stdin của tiến trình.
- Push lớn qua mạng yếu: đặt `http.postBuffer 524288000` và thử lại 5 lần.
- Tải asset lên Release: bỏ qua chỉ khi **digest sha256** trùng (cùng kích thước là chưa đủ). Upload hỏng giữa chừng thì xóa asset trước khi thử lại.

## Quy trình
- Trước khi viết lại một module, **viết bộ đối chiếu** với bản cũ (Dump.java / DumpCs.cs / js_dump.js / GlassDump*). Nhờ đó bản native khớp 100% ngay từ đầu.
- Khi người dùng báo "lag", **đo trước khi sửa**: gfxinfo (Android), Stopwatch trong selftest (Windows), benchmark JVM cho lõi. Ghi số liệu trước/sau vào TECHNICAL_UPDATE_NOTES.
- Ghi tiến độ vào `WORK_PROGRESS_v<ver>.md` khoảng 5 phút một lần (người dùng yêu cầu).
- Ghi mọi lỗi, dòng lệnh, công cụ, kết quả và ngày giờ vào `TECHNICAL_UPDATE_NOTES.md`.
- Nền tinh vân: hàm mờ dần của từng đám phải về 0 liên tục ở mép (`(exp(-d²·2.2) − e₀)/(1 − e₀)`), nếu cắt cụt sẽ thấy vệt vòng cung (sửa ở 0.0.5).
- **Windows Forms ClientSize = 0 khi Minimized:** Khi form bị thu nhỏ hoặc mất focus trong lúc chạy tự kiểm thử, Win32 gán `ClientSize = (0, 0)`. Điều này khiến `f.ClientRectangle` thành (0, 0) gây lỗi bước `safe_area` và làm modal box có chiều rộng bằng 0, đồng thời gây lỗi `new Bitmap(0, 0)`. Luôn bảo vệ bằng `Math.Max(D(720), ClientSize.Width)` và tự động khôi phục `WindowState = FormWindowState.Normal`.
- **Android 17 Emulator tự mở lại app sau `install -r`:** Trên Android 17, sau khi cập nhật gói apk hệ điều hành tự kích hoạt lại tiến trình cũ. Lệnh `am start` thông thường chỉ đưa task cũ lên trước mà không gọi `onCreate` làm mất cờ selftest. Phải dùng `am start -S -W` kết hợp gửi broadcast `android.intent.action.CLOSE_SYSTEM_DIALOGS` để đóng các hộp thoại hệ thống bị treo.
- **Máy ảo Android 6.0 chạy GPU phần mềm:** Trình giả lập cũ dùng SwiftShader CPU tạo tinh vân mất nhiều thời gian, khiến `requestBand` bị trễ. Cần đặt timeout tự kiểm thử tối thiểu 15 giây cho các bước Liquid Glass trên môi trường giả lập.
- **Tối ưu hóa dung lượng với Zopfli:** Nén 10 vòng Deflate bằng Zopfli WebAssembly trước khi ký và zipalign giúp giảm kích thước tệp APK và ZIP từ 6–7% mà vẫn đảm bảo tính tương thích tuyệt đối với mọi trình giải nén tiêu chuẩn.

## Bài học v0.0.6 (18/11/2026)

### Windows – lag còn sót ở 0.0.5 (đo bằng selftest 46/48/10)
- `DBPanel` (khung cuộn) vẽ ảnh nền bằng `DrawImage(img, Rectangle)` **có co giãn** + gọi nền form cha → mỗi khung vẽ nền 2 lần, có nội suy. Luôn `DrawImageUnscaled(G.Full, -Left, -Top)`.
- Form và bố cục phải dùng **cùng một kích thước** cho `G.Ensure` (`GlassSize`), nếu không ảnh nền bị co giãn lại liên tục.
- Không `Scroll.Invalidate(true)` cả cây: chỉ `Invalidate` điều khiển giao với khung nhìn (`InvalidateVisible`). Chú ý selftest phải đi **đúng đường vẽ** người dùng dùng (`ScrollStep`), nếu không số đo không phản ánh thực tế.
- Khóa bộ đệm nền có **vị trí** ⇒ cuộn = dựng lại mọi thẻ đang hiện. Cách sửa: khi đang cuộn dùng lại nền cũ nếu chỉ khác vị trí, dừng cuộn 220 ms vẽ lại chính xác (`ScrollStep/ScrollSettle`, `Box.Scrolling`). Selftest đo cả `settle`.
- Danh sách dài = hàng trăm HWND → phân trang 40 mục + "Xem thêm". Tìm kiếm lần đầu chậm vì nạp + bỏ dấu trên luồng UI → `Library.Warm()` ở luồng nền (cache có `lock`).
- Kết quả (cùng máy): tìm kiếm 2312 → 597 ms, tab chậm nhất 2251 → 821 ms (lượt 1).

### Android / emulator
- `svc power stayon usb` **không** giữ màn hình emulator (không có nguồn USB). Dùng `stayon true` + `settings put system screen_off_timeout 1800000`; app tự `FLAG_KEEP_SCREEN_ON | SHOW_WHEN_LOCKED | DISMISS_KEYGUARD | TURN_SCREEN_ON` khi `selftest`. Dấu hiệu màn hình tắt: hộp thoại 0x0, `ticks=0`, kính "pending" 15 s.
- `-gpu swiftshader_indirect` = vẽ bằng CPU → mọi bản (kể cả bản cũ) giật 98–100% trên emulator; **không** dùng số đo emulator đó để kết luận lag. Script 0.0.6 có `-EmuGpu auto` (mặc định).
- Selftest 54 bước: thêm `53_results_paging`, `54_library_warm`; bước 46 mở rộng danh sách tới 120 thẻ để so sánh công bằng với 0.0.5.

### Làm việc cùng Google Antigravity (/swarm)
- Antigravity đang chạy thì tin nhắn mới vào **hàng đợi** ("Queued Messages") và tự gửi khi nó xong lượt – không cần chờ.
- Luôn chia rõ **tệp nào ai sửa** (Claude: `src/`, `tools/`, script build, TECHNICAL_UPDATE_NOTES, WORK_PROGRESS; Antigravity: `docs/`, `AG_REPORT_v<ver>.md`, `translations/`) để không ghi đè nhau.
- **Rà lại nội dung Antigravity viết** trước khi phát hành: bản nháp trang chủ có câu sai ("108 tinh tú", "chỉ mục nhị phân", "60 FPS", "không gửi IP"). Đối chiếu với mã nguồn (`grep` URL trong `src/`) để trang quyền riêng tư nêu đủ mọi kết nối mạng.
- Dịch thư viện 611 bài × 67 ngôn ngữ: Antigravity làm dần vào `translations/lib/<mã>/NN.json` + `translations/glossary.json` + `translations/PROGRESS.md`. **Không nhúng hết vào APK** (thư viện ≈ 80% gói ⇒ hàng trăm MB) – tích hợp kiểu tải theo nhu cầu ở bản sau.

### Phiên AI
- Quyền điều khiển máy (computer use) **hết hạn sau 30 phút không dùng** → phải `computer_resolve_access` + `computer_request_access` lại.
- Không ghi email người dùng vào script/tài liệu; danh tính git lấy từ cấu hình máy (GitHub Desktop), thiếu thì dùng `<owner>@users.noreply.github.com`. Bộ lọc riêng tư của phiên Claude chặn đọc/sửa tệp chứa email.
- `device_commit_files` chỉ nhận tệp nằm trong `/mnt/user-data/outputs/` (hoặc fileUuid đã gửi).
- Ghi tiến độ 5 phút/lần bằng `D:\yh\aa006_progress.sh "<ghi chú>" [trạng thái]` (tự kèm bước build hiện tại từ `build_logs/status.json`).
