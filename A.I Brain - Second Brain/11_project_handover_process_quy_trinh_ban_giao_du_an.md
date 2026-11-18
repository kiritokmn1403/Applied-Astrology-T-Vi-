# 11 – Quy trình bàn giao dự án

Áp dụng khi:
- (a) kết thúc một bản cập nhật và giao cho Kiệt;
- (b) dừng giữa chừng (Kiệt yêu cầu tạm dừng, hết phiên, mất kết nối);
- (c) chuyển việc sang phiên hoặc tác nhân khác (Claude ↔ Antigravity ↔ người).

Mục tiêu: người nhận **không phải hỏi lại** mà vẫn biết đã xong gì, đang dở gì, làm tiếp thế nào.

## 1. Bàn giao khi hoàn thành một bản (checklist)
### A. Sản phẩm
- [ ] `output/<ver>/` đủ 12 tệp:
  - Android: 3 APK.
  - Windows: Setup.exe, portable.zip, appxbundle + `appx-install.bat` + `.cer`, MSI x64/x86/arm64.
  - `SHA256SUMS-<ver>.txt`.
- [ ] `sha256sum -c SHA256SUMS-<ver>.txt` → tất cả OK.
- [ ] GitHub Release `v<ver>`:
  - Đủ tệp cài đặt + video kiểm thử; bước 31 báo `verified` cho **từng** tệp.
  - Release được đánh dấu Latest.
- [ ] Tệp cài đặt đã gửi cho Kiệt qua chat (`SendUserFile`, kiểu đính kèm) và có sẵn trên máy ở `output/<ver>/`.

### B. Mã nguồn & GitHub
- [ ] Bước 30 đã push (`<cũ>..<mới> HEAD -> main`), có tag `v<ver>`, **không** force-push.
- [ ] README trên GitHub: tiêu đề đúng phiên bản, bảng tải `releases/download/v<ver>/…`, link Trang chủ + Chính sách quyền riêng tư.
- [ ] GitHub Pages:
  - Trang chủ + `privacy.html` trả HTTP 200.
  - Mục **Tải về** đúng phiên bản; mọi link trả mã 206.
  - About (Website, mô tả, topics) đúng.
- [ ] Mở trang thật bằng trình duyệt tích hợp để xem hiển thị (chụp màn hình nếu Kiệt cần).

### C. Tài liệu
- [ ] `TECHNICAL_UPDATE_NOTES.md`, phần `CẬP NHẬT v<ver>` gồm:
  - Phạm vi (từng mục Feature Check / Creat & Update → kết quả).
  - Lỗi → nguyên nhân → cách sửa (tệp).
  - Dòng lệnh, công cụ.
  - Kết quả từng lượt kiểm thử, so sánh với bản trước.
  - Giới hạn / việc tiếp theo, phát hành, bảng SHA-256, **ngày giờ**.
- [ ] `CHANGELOG.md`, `docs/RELEASE_NOTES_<ver>.md`, `tools/changelog_v<N>.json` (mục "Có gì mới" trong app).
- [ ] `WORK_PROGRESS_v<ver>.md` có mục **"HOÀN THÀNH"** (kết quả cuối, việc còn lại); dừng ghi 5 phút/lần.
- [ ] A.I Brain:
  - Bài học mới vào đúng tệp (04/05/07/08/09).
  - Cập nhật trạng thái trong `00`.
  - Thêm một mục vào `12_session log`.
- [ ] Chạy `PUBLISH_v<ver>.bat` thêm một lần để đẩy tài liệu cuối lên. Tệp Release trùng SHA-256 sẽ được bỏ qua.

### D. Báo cáo cho Kiệt (tiếng Việt, ngắn gọn)
1. Kết quả kiểm thử: bảng nền tảng → x/y.
2. **So với bản trước:** tính năng mới, lỗi đã sửa, bảng số đo trước ↔ sau (cùng máy).
3. Link Release, Trang chủ, commit.
4. **Những gì chưa làm được hoặc chưa kiểm** (nói thẳng). Ví dụ 0.0.6: chưa kiểm thử VPN thật, dịch thư viện mới 1/13 chương, `47_perf_modal` Windows chậm hơn khoảng 80 ms.
5. Gợi ý bước tiếp theo (nếu có thật).
- Ảnh chụp: vài ảnh tiêu biểu đã xem trước. Video: đã nằm trong Release.

## 2. Bàn giao khi dừng giữa chừng
Ghi ngay vào cuối `WORK_PROGRESS_v<ver>.md` một mục **"TRẠNG THÁI KHI TẠM DỪNG (<ngày giờ>)"** (mẫu có ở `WORK_PROGRESS_v0.0.5.md`):
- **Đã xong trong mã nguồn** (đã đồng bộ lên máy hay chưa).
- **Đang chạy:**
  - Tệp `.bat` nào, bắt đầu lúc mấy giờ, log nào (`build_logs/build_<ver>_<giờ>.log`).
  - Có an toàn để đóng cửa sổ cmd không.
- **Việc còn lại**, đánh số theo thứ tự làm.
- Quyết định đang chờ Kiệt (nếu có).

Thêm một dòng vào `12_session log`. **Không** để thay đổi chỉ nằm ở cloud: đồng bộ lên máy trước khi dừng.

## 3. Bàn giao giữa các tác nhân AI
- Người nhận đọc: A.I Brain (00 → 12) → mục "HOÀN THÀNH" hoặc "TẠM DỪNG" trong `WORK_PROGRESS` → phần mới nhất của `TECHNICAL_UPDATE_NOTES.md` → `AG_REPORT_v<ver>.md` (nếu có).
- Người giao ghi rõ (xem `09`):
  - Phạm vi tệp được sửa.
  - Đầu vào / đầu ra.
  - Tiêu chí kiểm tra, tệp báo cáo, dấu hiệu xong.
- Việc dài đang chạy (ví dụ dịch thư viện của Antigravity) phải có bảng tiến độ riêng (`translations/PROGRESS.md`). Người nhận đọc bảng đó, **không** làm lại phần đã xong.
- Bên nhận **rà lại** đầu ra của bên giao trước khi phát hành (xem `09` §5).

## 4. Những thứ KHÔNG bàn giao qua tài liệu
- Khóa ký, mật khẩu, token GitHub, email: chỉ nằm ở `D:\yh\aa002_keys` và Git Credential Manager của máy. Không chép vào tài liệu, chat hay commit.
- Quyền điều khiển máy, quyền xóa tệp: mỗi phiên tự xin lại.
