# Mẹo cho phiên AI làm việc từ xa

- `device_bash` là máy ảo Linux gắn các thư mục được chia sẻ. Nó **không xóa được tệp** (rm bị chặn): dùng `cp -rf` / `mv`, và giải nén ra `$HOME` rồi mới copy sang.
- **Git trên thư mục được chia sẻ lỗi** ("could not lock config file … File exists") vì git phải xóa tệp khóa. Clone vào `$HOME/refs` của máy ảo rồi đọc/chép từ đó (0.0.5).
- **Tiến trình nền trong `device_bash` bị dừng khi lệnh kết thúc** (nohup … & không sống sót). Chạy tiền cảnh, ≤ 180 s mỗi lệnh, chia nhỏ (0.0.5).
- `device_commit_files` cache theo đường dẫn, nên mỗi lần đồng bộ phải dùng tên tgz khác nhau (có timestamp).
- Không chạy được exe Windows từ `device_bash`. Thay vào đó, mở File Explorer bằng computer use (chỉ được click) rồi double-click file `.bat`. Theo dõi qua `build_logs`. Đường đi: This PC › DATA (D:) › A.I Project › A.I Project - Applied Astrology (Tử Vi Ứng Dụng) › Applied Astrology (Tử Vi) › Applied Astrology (Tử Vi).
- Có thể kiểm tra trước ở cloud container:
  - `javac -bootclasspath android.jar` (stage `D:\yh\tools\android-sdk\platforms\android-35\android.jar` bằng `device_stage_files`).
  - `apt-get install mono-mcs libmono-system-windows-forms4.0-cil libmono-system-io-compression*-cil libgdiplus` → `mcs` biên dịch C# WinForms; `Xvfb` + `mono AppliedAstrology.exe --selftest --out …` chạy thử.
  - PowerShell 7 (tải tarball từ GitHub về `/opt/pwsh`) để kiểm cú pháp `.ps1`: `[System.Management.Automation.Language.Parser]::ParseFile(...)`.
  - `pip install lunarcalendar zopfli` để đối chiếu Âm lịch / thử nén.
- Không bao giờ in hay ghi token GitHub, mật khẩu keystore, mật khẩu pfx. Email người dùng là thông tin riêng tư.
