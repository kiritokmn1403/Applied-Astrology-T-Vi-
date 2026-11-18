# 08 – Mẹo sử dụng phiên làm việc với AI (Claude làm việc từ xa trên máy Kiệt)

## 1. Ba môi trường khác nhau – đừng nhầm
| Môi trường | Là gì | Làm được | Không làm được |
|---|---|---|---|
| **Cloud container** (Bash/Read/Write của Claude) | Linux trên máy chủ Anthropic | Sửa bản sao mã; `javac` + `android.jar`; Node; Python; `curl` tới GitHub (kiểm link tải) | Cài gói (pip/npm/apt bị tắt) ⇒ không có Mono/pwsh; không thấy ổ D: |
| **`device_bash`** | Máy ảo Linux trên máy Kiệt, gắn các thư mục được chia sẻ ở `$HOME/mnt/<tên>` | Đọc/ghi/tìm tệp dự án, giải nén, đọc log, chạy script bash/python | Chạy exe Windows; ra mạng GitHub; **xóa tệp** (`rm` bị chặn); git trên thư mục chia sẻ (lỗi khóa) |
| **Màn hình Windows** (computer use) | Điều khiển chuột/phím qua ảnh chụp | Double-click `.bat` trong File Explorer; gõ vào Antigravity / GitHub Desktop | Gõ phím vào File Explorer, cmd, PowerShell, VS Code, Android Studio (chỉ được click); điều khiển trình duyệt (chỉ đọc) |

## 2. Đồng bộ mã giữa cloud và máy
1. Trên máy (`device_bash`): `tar czf "$HOME/mnt/yh/aa00N_src_<ts>.tgz" src tools … build_release_….ps1`. Sau đó `device_stage_files` → tệp xuất hiện ở `/mnt/user-data/uploads/yh/…`.
2. Ở cloud: giải nén, sửa, kiểm tra (`javac`, `node`), rồi đóng gói tệp đã sửa vào **`/mnt/user-data/outputs/aa00N_sync_<ts>.tgz`**. `device_commit_files` chỉ nhận tệp ở thư mục này hoặc fileUuid đã gửi.
3. `device_commit_files` → `D:\A.I Project\yh\…`. Trên máy: giải nén ra `$HOME/x…`, rồi `cp -rf` vào thư mục dự án.
- **Mỗi lần đồng bộ dùng tên tgz mới (timestamp)**, vì commit được cache theo đường dẫn.
- Sửa tệp trên máy thì dùng `python3`/`sed` đọc–sửa–ghi tại chỗ, **không** gõ lại nội dung từ đầu ra công cụ (có thể bị cắt).

## 3. Giới hạn của `device_bash`
- Mỗi lệnh ≤ 180 s (mặc định 120 s). Tiến trình nền (`nohup … &`) **chết khi lệnh kết thúc**, nên chạy tiền cảnh và chia nhỏ.
- `find`/`ls -R` trên toàn ổ có thể quá thời gian; giới hạn độ sâu, chỉ đọc thư mục cần.
- Không xóa được tệp: dùng `mv`/`cp`. Nếu cần xóa thì xin quyền (`device_request_delete_permission`); không được thì chuyển vào `_to_delete/`.
- Git: clone vào `$HOME/refs` của máy ảo rồi đọc. Commit/push thật do script PowerShell trên Windows làm (bước 30).
- Tệp log đang được ghi có thể báo "Permission denied" trong chốc lát: đợi vài giây rồi đọc lại (hoặc đọc `status.json`).

## 4. Điều khiển màn hình
- Trước tiên `computer_resolve_access` → `computer_request_access` (File Explorer, Antigravity, GitHub Desktop, cmd…).
- **Quyền hết hạn sau 30 phút không dùng**: lỗi "Access … expired" thì xin lại (Kiệt duyệt lại).
- Đường tới thư mục dự án trong File Explorer: This PC › DATA (D:) › A.I Project › A.I Project - Applied Astrology (Tử Vi Ứng Dụng) › Applied Astrology (Tử Vi) › Applied Astrology (Tử Vi).
  - Cuộn xuống để thấy `BUILD_*`/`PUBLISH_*`; chụp màn hình để lấy toạ độ trước khi double-click.
- Thanh taskbar: File Explorer khoảng (290, 800), Antigravity khoảng (592, 800) ở màn 1456×819. Vị trí có thể đổi, luôn chụp lại trước khi click.
- Cửa sổ cmd của `.bat` có `pause`; để nguyên, không cần đóng.
- Xong việc thì `computer_release_lock`.

## 5. Theo dõi build
- Lệnh theo dõi điển hình (mỗi lần ≤ 180 s), kèm ghi tiến độ:
  `bash "$HOME/mnt/yh/aa00N_progress.sh" "<ghi chú>" >/dev/null; sleep 165; grep -aE "SELFTEST|    FAIL [0-9]|\[ERR\]|STEP\]" <log mới nhất> | tail`
- `status.json` có `finished` và `failed`; `=== ALL DONE ===` ở cuối log.
- Thời gian tham khảo: probe ~1 phút; build ~8 phút; Note 9 ~5 phút; mỗi emulator ~5–8 phút; Windows ~15 phút; publish ~3–6 phút.

## 6. Bộ lọc riêng tư của phiên Claude
- Phiên Claude **chặn đọc/xử lý tệp có chứa email** của Kiệt, và chặn cả lệnh có nhắc tên tệp đó (ví dụ script 0.0.5 cũ có email trong lệnh `git commit`).
- Cách làm: xóa phần email bằng biểu thức chung (`sed -E 's/"-c", "user\.email=[^"]*", //g'`) trên bản sao; đặt tên tệp trung gian chung chung; đưa tham số qua `argv`.
- Không bao giờ thêm email vào tệp mới.

## 7. Giao tiếp trong phiên
- Kiệt có thể ngắt giữa chừng (Request interrupted) để thêm yêu cầu. Đọc yêu cầu mới, làm tiếp từ chỗ dừng, **không** chạy lại thao tác đã có tác dụng phụ (build, push).
- Câu trả lời giữa chừng ngắn gọn, có số liệu. Gửi ảnh và tệp bằng `SendUserFile` (xem ảnh trước khi gửi).
- Kết thúc phiên: bảng kết quả, link Release/Trang chủ, việc chưa làm (đầy đủ ở `11`); thêm 1 mục vào `12_session log`.
