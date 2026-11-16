# Mẹo cho phiên AI làm việc từ xa

- `device_bash` là máy ảo Linux gắn các thư mục được chia sẻ. Nó **không xóa được tệp** (rm bị chặn): dùng `cp -rf` / `mv`, và giải nén ra `$HOME` rồi mới copy sang.
- `device_commit_files` cache theo đường dẫn, nên mỗi lần đồng bộ phải dùng tên tgz khác nhau (có timestamp).
- Không chạy được exe Windows từ `device_bash`. Thay vào đó, mở File Explorer bằng computer use (chỉ được click) rồi double-click file `.bat`. Theo dõi qua `build_logs`.
- Có thể kiểm tra trước ở cloud container: `javac` với `android.jar`, `mcs` (Mono) cho C# WinForms, `Xvfb` để chạy thử giao diện. Mono `DrawToBitmap` chỉ vẽ nền form.
- Không bao giờ in hay ghi token GitHub, mật khẩu keystore, mật khẩu pfx. Email người dùng là thông tin riêng tư.
