# 02 – Quy trình làm việc của một bản cập nhật

Áp dụng cho mọi bản `v0.0.N`. Chi tiết build xem `03`, kiểm thử xem `05`, chia việc nhiều tác nhân xem `09`, bàn giao xem `11`, nhật ký phiên xem `12`.

## Bước 1 – Đọc & nắm yêu cầu
1. Đọc hết thư mục A.I Brain (00 → 12); đọc kỹ mục mới nhất trong `12` và mục "HOÀN THÀNH"/"TẠM DỪNG" cuối `WORK_PROGRESS` của bản trước.
2. Đọc `TECHNICAL_UPDATE_NOTES.md`, ít nhất phần của bản gần nhất và các mục "Giới hạn / việc tiếp theo".
3. Quét 2 tệp Excel Kiệt gửi:
   - *Project – Applied Astrology.xlsx*: sheet **Feature Check** (danh sách việc) + **Creat & Update** (yêu cầu tính năng) + Update Link.
   - *Project GitHub.xlsx*: khoảng 660 link tham chiếu, chia theo nhóm (Flash Player, Runtime Native/Hybrid, Trình duyệt, Luna Calendar, Liquid Glass, VPN & DNS…).
4. Xem kết quả kiểm thử bản trước (`test_reports/<ver-1>/TEST_REPORT.md`, `all_results.json`): bước nào FAIL, số đo hiệu năng.
5. Kiểm tra bản trước đã phát hành đủ chưa: Release trên GitHub, SHA-256, có commit nào chưa push.

## Bước 2 – Tóm tắt & chờ xác nhận
- Viết tóm tắt **toàn bộ** yêu cầu bằng tiếng Việt, nêu rõ những gì thiếu hoặc mâu thuẫn. Ví dụ:
  - Sheet "App Feature Set" không có trong tệp Excel.
  - "Windows 7 ARM" không tồn tại.
- Hỏi tối đa 1–2 câu thật cần thiết. **Chờ Kiệt xác nhận** rồi mới sửa code.
- Nếu Kiệt không trả lời câu hỏi phụ: chọn mặc định hợp lý, **ghi rõ mặc định đã chọn** vào WORK_PROGRESS và báo lại.

## Bước 3 – Chuẩn bị
- Xin quyền điều khiển máy (File Explorer, Antigravity, GitHub Desktop, cmd…). Quyền hết hạn sau 30 phút không dùng (xem `08`).
- Tạo `WORK_PROGRESS_v<ver>.md`: bảng *Thời gian | Việc đã làm | Trạng thái*.
  - Ghi **khoảng 5 phút/lần** bằng `D:\yh\aa006_progress.sh "<ghi chú>" [trạng thái]` (đổi tên theo bản).
  - Khi xong thì ghi mục "HOÀN THÀNH" và dừng ghi.
- Nếu có Antigravity: giao việc qua `/swarm` theo `09`.
- Lập danh sách công việc (task list) để Kiệt theo dõi.

## Bước 4 – Phân tích & sửa
1. **Đo trước khi sửa**, đừng đoán:
   - Dùng số liệu selftest 46–51 và gfxinfo của bản trước.
   - Tìm nguyên nhân gốc trong mã (xem `04`, `07`).
2. Sửa trên bản sao ở cloud: kéo `src/` về bằng tgz qua `device_stage_files`.
3. Sửa **cả Java và C#** nếu đụng lõi (xem `01` §2).
4. Biên dịch thử ở cloud: `javac -source 8 -target 8 -bootclasspath android-35/android.jar`; `node tools/build_native_data.js`.
5. Chuỗi giao diện mới ⇒ thêm `tools/i18n_v<N>` (đủ 68 ngôn ngữ, 0 khóa lấy tạm).
6. Thêm bước selftest cho tính năng hoặc lỗi mới (xem `05`).
7. Nâng phiên bản ở mọi nơi (danh sách ở `03` §4).
8. Tạo `build_release_0.0.N.ps1` + 4 tệp `.bat` từ bản trước. Dùng script biến đổi có `assert`, không sửa tay.
9. Đồng bộ lên máy: đóng gói tgz có timestamp trong `/mnt/user-data/outputs/` → `device_commit_files` vào `D:\A.I Project\yh\` → giải nén bằng `device_bash` rồi `cp -rf` vào thư mục dự án.

## Bước 5 – Build & kiểm thử (xem `03`, `05`)
1. `BUILD_ONLY_v<ver>.bat`: biên dịch + đối chiếu chéo + đóng gói (khoảng 9 phút).
2. `BUILD_TEST_v<ver>.bat` lượt 1: build + kiểm thử toàn bộ, không phát hành (khoảng 40–60 phút).
3. Bước nào FAIL ⇒ tìm nguyên nhân, sửa, rồi chạy lại `BUILD_TEST`. Lưu báo cáo lượt cũ vào `test_reports/<ver>_round1/`.
4. Trong lúc chờ: soạn README, CHANGELOG, RELEASE_NOTES, TECHNICAL_UPDATE_NOTES; gửi ảnh kiểm thử cho Kiệt.

## Bước 6 – Phát hành (xem `03` §5)
1. Điền số liệu cuối vào README (bảng tải `releases/download/v<ver>/…`), `CHANGELOG.md`, `docs/RELEASE_NOTES_<ver>.md` và `TECHNICAL_UPDATE_NOTES.md`.
2. Cập nhật mục **Tải về** trên `docs/index.html` (phiên bản, tên tệp, dung lượng, bản cũ).
3. Chạy `PUBLISH_v<ver>.bat` (đã kiểm thử xong thì không cần build lại):
   - Bước 30: git push. Bước 31: Release + xác minh SHA-256. Bước 32: GitHub Pages + mục About.
4. Kiểm tra độc lập từ bên ngoài:
   - Link tải trả mã 206.
   - README trên GitHub hiển thị đúng.
   - Trang chủ và trang Chính sách quyền riêng tư trả HTTP 200.
   - Mở trang thật để xem hiển thị.

## Bước 7 – Ghi chép & bàn giao (checklist đầy đủ ở `11`)
- `TECHNICAL_UPDATE_NOTES.md`: phạm vi; lỗi → nguyên nhân → cách sửa (kèm tệp); dòng lệnh; công cụ; kết quả kiểm thử từng lượt; so sánh với bản trước; giới hạn; phát hành; bảng SHA-256; **ngày giờ**.
- Cập nhật A.I Brain (đúng tệp theo chủ đề), thêm 1 mục vào `12_session log`, cập nhật dòng "Cập nhật lần cuối" trong `00`.
- Chạy `PUBLISH` thêm một lần để đẩy tài liệu cuối lên. Các tệp Release đã khớp SHA-256 sẽ được bỏ qua.
- Gửi Kiệt:
  - Tệp cài đặt (đã đối chiếu SHA-256).
  - Ảnh kiểm thử.
  - Bảng "tính năng mới / lỗi đã sửa / số đo trước–sau".
  - Những việc **chưa làm được** (nói thẳng, không che).
