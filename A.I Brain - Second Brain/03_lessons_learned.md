# Bài học kinh nghiệm (lỗi thật đã gặp)

## Android
- Thêm assets bằng `aapt2 -A` trên Windows sẽ ghi đường dẫn `assets/web\css` mà Android không đọc được. Phải tự thêm vào zip với dấu `/`.
- Truyền từng `.class` cho `d8.bat` khi có nhiều lớp sẽ lỗi "The command line is too long". Gom vào `classes.jar` trước (`jar cf`) (0.0.4).
- Android 15 (API 35) bắt buộc edge-to-edge: phải tự đệm theo `systemBars | displayCutout | ime`, nếu không nút và tab bị che (0.0.3).
- Android 6 thiếu root CA DigiCert Global Root G2 nên không tải được từ GitHub. Dùng `TlsCompat` + `assets/certs/*.pem`.
- Emoji ☾ ☯ trên tab biến thành emoji màu trên Samsung. Từ 0.0.4 dùng biểu tượng vẽ vector.
- Nhãn tab dài (tiếng Pháp "Bibliothèque") đè lên tab bên cạnh. Sửa bằng `fitLabel`: thu nhỏ chữ tới 8sp, còn thiếu chỗ thì thêm "…" (0.0.4).
- Note 9 (USB): Play Protect có thể hỏi "Gửi ứng dụng để quét". Xiaomi: phải bật "Cài đặt qua USB".
- Bản release không cho `run-as`, nên không đọc được `/proc/<pid>/maps`. Việc "không có WebView" được kiểm bằng selftest bước 43 (đếm WebView = 0).

## Windows
- `Control.Visible` = false trước khi form hiện, nên đo chiều cao ra 0 và màn hình trắng. Dùng cờ `Collapsed` riêng và gọi `DoLayout` trong `Shown`.
- Đo chữ mỗi lần layout rất chậm (31 s trên Mono). Cache `PrefHeight/PrefWidth` và lan truyền `Dirty()` thì còn khoảng 1 s.
- Trùng tên với thành viên của `Form`: trường `Scale`, phương thức `Convert`. Phải gọi đầy đủ `System.Convert`.
- AppX từ chối đường dẫn junction (`D:\yh`), nên copy bundle vào `%TEMP%` rồi mới `Add-AppxPackage`.
- appx tự ký: lỗi 0x80073CF0 thực chất là 0x800B0109. Cần import `.cer` vào **LocalMachine\TrustedPeople** (đã có `appx-install.bat` tự nâng quyền).
- SmartScreen vẫn cảnh báo với mọi exe không có chứng chỉ thương mại (EV/OV). Đây **không** phải lỗi native/trình duyệt; nói rõ với người dùng.
- Khi nâng cấp từ bản WebView2, Setup.exe phải xóa `web/`, `runtimes/` và `Microsoft.Web.WebView2.*.dll` cũ.

## Git / GitHub
- Người dùng có thể commit thẳng trên github.com (vd. 16/11/2026: `Add Scala CI workflow`). Khi đó push bị từ chối (non-fast-forward). **Không force-push**: `git fetch` + `git rebase origin/main` rồi mới push (bước 30 của script 0.0.4 đã tự làm).
- Bước test nào đọc `results.json` thì phải xóa thư mục kết quả trước khi chạy, nếu không sẽ đọc phải kết quả cũ.
- `git credential fill` qua pipe PowerShell lỗi "missing protocol". Phải ghi thẳng vào stdin của tiến trình.
- Push lớn qua mạng yếu: đặt `http.postBuffer 524288000` và thử lại 5 lần.
- Tải asset lên Release: bỏ qua chỉ khi **digest sha256** trùng (cùng kích thước là chưa đủ). Upload hỏng giữa chừng thì xóa asset trước khi thử lại.

## Quy trình
- Trước khi viết lại một module, **viết bộ đối chiếu** với bản cũ (Dump.java / DumpCs.cs / js_dump.js). Nhờ đó bản native khớp 100% ngay từ đầu.
- Ghi tiến độ vào `WORK_PROGRESS_v<ver>.md` khoảng 5 phút một lần (người dùng yêu cầu).
- Ghi mọi lỗi, dòng lệnh, công cụ, kết quả và ngày giờ vào `TECHNICAL_UPDATE_NOTES.md`.
