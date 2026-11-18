# A.I Brain – Second Brain (Applied Astrology)

> **Quy tắc bắt buộc:** mọi phiên làm việc mới (người hoặc AI) **phải đọc hết thư mục này trước khi sửa code, build hay phát hành**. Phiên nào học thêm được điều gì thì ghi vào đây trước khi kết thúc.

| Tệp | Nội dung |
|---|---|
| `01_architecture.md` | Kiến trúc native (0.0.4+), Liquid Glass bất đồng bộ (0.0.5), nơi đặt mã, cách sinh dữ liệu dùng chung |
| `02_build_release.md` | Quy trình build / test / phát hành, công cụ, đường dẫn, khóa ký, bảng phiên bản |
| `03_lessons_learned.md` | Lỗi đã gặp và cách sửa (0.0.2 → 0.0.5) – **đọc kỹ nhất** |
| `04_testing.md` | Cách tự kiểm thử (52 bước), đo hiệu năng (gfxinfo), thiết bị, đọc kết quả |
| `05_ai_session_tips.md` | Mẹo riêng cho phiên AI làm việc từ xa trên máy này |
| `06_performance_rules.md` | **Quy tắc hiệu năng** (bắt buộc khi sửa giao diện/kính) – rút ra từ lỗi lag 0.0.4 |

Thứ tự làm việc chuẩn của một bản cập nhật:
1. Đọc hết thư mục này + `TECHNICAL_UPDATE_NOTES.md` + hai tệp Excel (Feature Check, Project GitHub).
2. Tóm tắt yêu cầu bằng tiếng Việt, chờ Kiệt xác nhận.
3. Tạo `WORK_PROGRESS_v<ver>.md`, ghi ~5 phút/lần.
4. Sửa code → biên dịch thử ở cloud (javac + android.jar, mcs/Mono, pwsh parse) → đồng bộ lên máy.
5. `BUILD_TEST_v<ver>.bat` (lượt 1) → sửa lỗi → `BUILD_v<ver>.bat` (lượt cuối: build + test + phát hành).
6. Ghi `TECHNICAL_UPDATE_NOTES.md`, README, CHANGELOG, RELEASE_NOTES, cập nhật thư mục này.

Cập nhật lần cuối: 18/11/2026 (v0.0.6).

**Từ v0.0.6:** có thể chia việc cho Google Antigravity bằng `/swarm` (xem `03_lessons_learned.md` › "Làm việc cùng Google Antigravity"); ghi tiến độ 5 phút/lần bằng `D:\yh\aa006_progress.sh`; luôn rà nội dung do agent khác viết trước khi phát hành.
