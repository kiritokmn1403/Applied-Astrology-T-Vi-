# 04 – Bài học kỹ thuật (lỗi thật đã gặp, nguyên nhân, cách sửa)

Ghi theo dạng: **triệu chứng → nguyên nhân → cách sửa (bản)**. Bài học về phiên AI xem `08`; về điều phối tác nhân xem `09`.

## 1. Hiệu năng – lag, giật, treo
### Android (0.0.4 → sửa ở 0.0.5)
- **Triệu chứng:** emulator API 37 chỉ vẽ được 8 khung, 100% giật, p90 = 4950 ms. **Nguyên nhân:**
  - `OnScrollChangedListener` gọi `invalidateFrames()` cho **mọi** khung kính ở mỗi pixel cuộn, kể cả thẻ ngoài màn hình.
  - `onDraw` tạo mới `BitmapShader`/`LinearGradient` mỗi lần vẽ, nên GC chạy liên tục.
  - Mọi thẻ đều có `setElevation`, tức hàng trăm bóng đổ.
  - Khúc xạ tính **đồng bộ trên luồng UI** cho cả w×h điểm ảnh; cache 24 mục ~51 MB/mục.
  - Đổi bất kỳ cài đặt nào cũng dựng lại tinh vân trên luồng UI.
- **Sửa:** viết lại kính:
  - Bản đồ thưa, dựng ở luồng nền; GPU vẽ lõi kính (`BitmapShader` + `ColorMatrixColorFilter`, chạy được trên HWUI từ API 23).
  - Gộp sự kiện cuộn theo khung hình; bỏ elevation trên thẻ.

### Windows (0.0.4 → 0.0.5, phần còn sót sửa ở 0.0.6)
- **0.0.4:** điều khiển trong suốt vẽ lại kéo theo `OnPaintBackground` của cha.
  - Form vẽ `DrawImage` **co giãn** ảnh tinh vân, khung kính co giãn ×4. GDI+ co giãn rất chậm.
  - Sửa ở 0.0.5: bitmap PArgb đúng cỡ + `DrawImageUnscaled` + bộ đệm nền từng khung.
  - Mở hộp thoại: thay `DrawToBitmap` toàn form bằng `CopyFromScreen` (BitBlt).
- **0.0.5 vẫn lag** (cuộn 95–145 ms/khung, tab Thư viện 1,9 s, tìm kiếm 2,3 s). Năm nguyên nhân, đều sửa ở 0.0.6:
  1. `DBPanel` (khung cuộn) vẽ nền bằng `DrawImage(img, Rectangle)` **có co giãn** và còn vẽ nền form cha, tức 2 lần mỗi khung. → `DrawImageUnscaled(G.Full, -Left, -Top)`.
  2. Form và bố cục gọi `G.Ensure` với 2 kích thước khác nhau, nên ảnh nền bị co giãn lại liên tục. → `GlassSize` dùng chung.
  3. `Scroll.Invalidate(true)` vẽ lại **cả cây**, kể cả hàng trăm điều khiển ngoài màn hình. → `InvalidateVisible` chỉ vẽ phần giao với khung nhìn.
  4. Khóa bộ đệm nền thẻ có **vị trí**, nên khi cuộn mọi thẻ đang hiện bị dựng lại (466 lần/60 khung). → Đang cuộn thì dùng lại nền (`Box.Scrolling`); dừng 220 ms thì `ScrollSettle` vẽ lại chính xác.
  5. Danh sách 120 kết quả = hàng trăm cửa sổ con (HWND); tìm kiếm lần đầu nạp + bỏ dấu 611 bài trên luồng UI. → Phân trang 40 mục + "Xem thêm"; `Library.Warm()` ở luồng nền (cache bỏ dấu có `lock`).
  - **Kết quả (cùng máy):** cuộn 94,6 → 39,2 ms/khung; tìm kiếm 2342 → 506 ms; tab chậm nhất 1955 → 745 ms.
- **Selftest phải đi đúng đường vẽ của người dùng.** 0.0.5 đo bằng `Invalidate(true)` trong khi người dùng đi qua `QueueScrollPaint`; 0.0.6 đo bằng `ScrollStep` + đo cả `settle`.

## 2. Android
- `aapt2 -A` trên Windows ghi đường dẫn `assets/web\css`, Android không đọc được. → Tự thêm vào zip với dấu `/`.
- Truyền từng `.class` cho `d8.bat` báo lỗi "The command line is too long". → Gom vào `classes.jar` (`jar cf`) (0.0.4).
- Android 15 (API 35) bắt buộc edge-to-edge. → Tự đệm theo `systemBars | displayCutout | ime`, nếu không nút và tab bị che (0.0.3).
- Android 6 thiếu root CA DigiCert Global Root G2 nên không tải được từ GitHub. → `TlsCompat` + `assets/certs/*.pem` (0.0.3).
- Emoji ☾ ☯ trên tab thành emoji màu trên Samsung. → Biểu tượng vẽ vector (0.0.4).
- Nhãn tab dài ("Bibliothèque") đè tab bên. → `fitLabel` thu nhỏ chữ tới 8sp, còn thiếu thì thêm "…" (0.0.4).
- Bản release không cho `run-as` (không đọc được `/proc/<pid>/maps`). → Kiểm "không WebView" bằng selftest bước 43.
- **Android 17 tự mở lại app sau `install -r`**, nên `am start` chỉ đưa task cũ lên mà không gọi `onCreate` (mất cờ selftest). → `am start -S -W` + broadcast `CLOSE_SYSTEM_DIALOGS`; app xử lý cờ trong `onNewIntent` (0.0.5).
- **Emulator tắt màn hình giữa selftest** (0.0.5, Android 6: hộp thoại 0x0, `ticks=0`, kính "pending" 15 s).
  - Nguyên nhân: `svc power stayon usb` không áp dụng cho emulator; `wm dismiss-keyguard` không có ở API 23.
  - Sửa: `stayon true` + `screen_off_timeout 1800000`; app bật `FLAG_KEEP_SCREEN_ON | SHOW_WHEN_LOCKED | DISMISS_KEYGUARD | TURN_SCREEN_ON` khi selftest (0.0.6).
- **`-gpu swiftshader_indirect` = vẽ bằng CPU**: mọi bản (cả 0.0.4/0.0.5) giật 98–100% trên emulator. Số đo đó **không** nói gì về máy thật. → `-EmuGpu auto` (0.0.6). Với GPU thật: 0.0.5 = 7,27%, Android 6 0.0.6 = 0%.
- Note 9 (USB): Play Protect có thể hỏi "Gửi ứng dụng để quét". Xiaomi phải bật "Cài đặt qua USB".
  - Mục "Xiaomi 15T" trong File Explorer **không** phải máy đang cắm. Thiết bị thật để thử là máy có trong `adb devices`.

## 3. Windows
- `Control.Visible = false` trước khi form hiện ⇒ đo chiều cao ra 0, màn hình trắng. → Cờ `Collapsed` riêng + `DoLayout` trong `Shown`.
- Đo chữ mỗi lần layout rất chậm (31 s trên Mono). → Cache `PrefHeight/PrefWidth` + lan truyền `Dirty()`, còn khoảng 1 s.
- Trùng tên với thành viên của `Form`: `Scale`, `Convert`. → Gọi đầy đủ `System.Convert`.
- **ClientSize = 0 khi cửa sổ bị thu nhỏ hoặc mất focus** trong selftest ⇒ `new Bitmap(0,0)`, hộp thoại rộng 0. → `Math.Max(D(720), ClientSize.Width)` + tự khôi phục `WindowState.Normal`.
- AppX từ chối đường dẫn junction (`D:\yh`). → Copy bundle vào `%TEMP%` rồi mới `Add-AppxPackage`.
- appx tự ký báo 0x80073CF0, thực chất là 0x800B0109. → Import `.cer` vào **LocalMachine\TrustedPeople** (`appx-install.bat` tự nâng quyền).
- SmartScreen cảnh báo mọi exe không có chứng chỉ thương mại (EV/OV): **không** phải lỗi app, cần nói rõ với người dùng.
- Nâng cấp từ bản WebView2: Setup.exe phải xóa `web/`, `runtimes/`, `Microsoft.Web.WebView2.*.dll` cũ.

## 4. Âm lịch
- **Lỗi "mùng 0"** (có từ thuật toán gốc, sửa ở 0.0.5):
  - Nguyên nhân: `k = INT((jd − 2415021.077)/29.53)` có thể lệch 1 ⇒ ngày âm = 0 (VN: 7/5/2054, 9/4/2062).
  - Sửa ở Java/C#/JS: `while (monthStart > dayNumber) { k--; monthStart = getNewMoonDay(k, tz); }`.
  - Kiểm bằng `calendar_test.js` + selftest 52 (73.018 ngày 1900–2099).
- Đối chiếu `wolfhong/LunarCalendar` (tz = 8): khớp 99,795%. Phần còn lại (1914/1916/1920 giờ địa phương Bắc Kinh; 2057/2089 Sóc sát nửa đêm) **không phải lỗi**.
- Lịch VN dùng GMT+7 cho mọi năm (theo Hồ Ngọc Đức và SWF). Trước 1968 miền Bắc dùng GMT+8; **không đổi** để giữ khớp SWF.
- Nền tinh vân có vệt vòng cung khi hàm mờ dần bị cắt cụt. → `(exp(−d²·2.2) − e₀)/(1 − e₀)` (0.0.5).

## 5. Đa ngôn ngữ & nội dung
- Aniyomi/Nekoyomi có 68 thư mục, nhưng `base` (= en) và `ur` **rỗng**. Cộng `es-419` (Mangayomi) thì được 68 ngôn ngữ thật.
  - **Đếm theo số chuỗi, không đếm thư mục.** 0.0.6: Antigravity xác nhận app đủ 68/68, không thiếu mã nào.
- Thêm khóa giao diện phải có đủ 68 bản dịch thật (không lấy tạm en/vi): log `0 filled`.
- Dịch thư viện: 611 bài × 67 ngôn ngữ rất chậm (khoảng 1,5 giờ được 1 chương tiếng Anh). Không nhúng vào APK vì thư viện chiếm khoảng 80% gói.

## 6. Git / GitHub
- Kiệt có thể commit thẳng trên github.com, nên push bị từ chối (non-fast-forward). → `git fetch` + `git rebase origin/main`, **không force-push** (bước 30 tự làm).
- `git credential fill` qua pipe PowerShell lỗi "missing protocol". → Ghi thẳng vào stdin của tiến trình.
- Push lớn qua mạng yếu: `http.postBuffer 524288000`, thử lại 5 lần.
- Asset Release: chỉ bỏ qua khi **digest SHA-256** trùng (cùng kích thước là chưa đủ). Upload hỏng giữa chừng thì xóa asset rồi thử lại.
- **Video cùng thư mục bị trùng tên asset** (`android_test_video` và `_part2`), phần 2 ghi đè phần 1. → Tên = `test-video-<thư mục>-<tên tệp>.mp4` (0.0.6).
- Bước test đọc `results.json` phải xóa thư mục kết quả cũ trước khi chạy, nếu không sẽ đọc phải kết quả cũ.

## 7. Phương pháp
- Trước khi viết lại một module, **viết bộ đối chiếu** với bản cũ (`Dump.java`, `DumpCs.cs`, `js_dump.js`, `GlassDump*`), để bản mới khớp 100% ngay từ đầu.
- Người dùng báo "lag" ⇒ **đo trước khi sửa**:
  - Android: gfxinfo. Windows: Stopwatch trong selftest. Lõi: benchmark JVM.
  - Chạy selftest **bản cũ trên cùng máy** để so sánh công bằng.
- Sửa một lỗi mà selftest vẫn FAIL ⇒ tìm tiếp nguyên nhân thứ hai, đừng nới ngưỡng (ví dụ lag Windows 0.0.6 cần 2 lượt sửa).
- Nội dung do AI khác viết (trang web, mô tả) phải đối chiếu với mã nguồn trước khi đăng (xem `09`).
