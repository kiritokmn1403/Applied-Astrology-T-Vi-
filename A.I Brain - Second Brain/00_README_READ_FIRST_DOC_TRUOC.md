# 00 – BẮT BUỘC ĐỌC TRƯỚC · A.I Brain – Second Brain (Applied Astrology / Tử Vi Ứng Dụng)

> ⚠️ **QUY TẮC BẮT BUỘC**
> 1. Mọi phiên làm việc mới (người, Claude, Google Antigravity hay tác nhân AI khác) **phải đọc hết thư mục này** trước khi sửa code, build, kiểm thử hay phát hành.
> 2. Đọc **theo thứ tự 00 → 12**. Các tệp đánh dấu ★ là tối thiểu phải đọc kỹ.
> 3. Trước khi kết thúc phiên: làm theo `11` (bàn giao), thêm 1 mục vào `12` (nhật ký phiên), ghi bài học vào đúng tệp, rồi cập nhật dòng "Cập nhật lần cuối" ở cuối tệp này.

## Mục lục

| # | Tệp | Nội dung |
|---|---|---|
| 00 | `00_README_READ_FIRST_DOC_TRUOC.md` | ★ Tệp này: quy tắc bắt buộc, mục lục, tóm tắt trạng thái dự án |
| 01 | `01_architecture_kien_truc.md` | ★ Kiến trúc dự án: native Android/Windows, lõi dùng chung, Liquid Glass, dữ liệu, lưu trữ, phiên bản |
| 02 | `02_procedure_quy_trinh.md` | ★ Quy trình làm việc của một bản cập nhật (từ nhận yêu cầu đến bàn giao) |
| 03 | `03_build_release_xay_dung_ban_phat_hanh.md` | ★ Xây dựng bản phát hành: script, các bước, đổi phiên bản, Release, GitHub Pages |
| 04 | `04_technical_lesson_bai_hoc_ky_thuat.md` | ★ Bài học kỹ thuật: lỗi thật đã gặp từ 0.0.2 đến 0.0.6 và cách sửa |
| 05 | `05_testing_kiem_tra.md` | Quy trình kiểm tra: selftest 54 bước, đo hiệu năng, thiết bị, đọc kết quả |
| 06 | `06_tools_used_cong_cu_su_dung.md` | Công cụ sử dụng: đường dẫn, phiên bản, dùng vào việc gì |
| 07 | `07_performance_rules_cac_quy_tac_thuc_hien.md` | ★ Các quy tắc thực hiện: hiệu năng, mã nguồn, bảo mật, tài liệu |
| 08 | `08_ai_session_tips_meo_su_dung_phien_lam_viec_voi_ai.md` | Mẹo cho phiên làm việc với AI (Claude từ xa, máy ảo, điều khiển màn hình) |
| 09 | `09_A.I._agent orchestration_dieu_phoi_tac_nhan.md` | Điều phối tác nhân AI: Claude ↔ Google Antigravity (`/swarm`), chia việc, rà soát |
| 10 | `10_project_directory_structure_cau_truc_thu_muc_du_an.md` | Cấu trúc thư mục dự án và thư mục build |
| 11 | `11_project_handover_process_quy_trinh_ban_giao_du_an.md` | ★ Quy trình bàn giao: checklist khi xong bản, khi dừng giữa chừng, khi chuyển giữa các tác nhân |
| 12 | `12_session log_nhat_ky_phien_lam_viec.md` | ★ Nhật ký phiên làm việc (mới nhất trên cùng) – đọc mục đầu tiên để biết phiên trước dừng ở đâu |

## Trạng thái dự án (18/11/2026)

- **Phiên bản hiện tại: v0.0.6** – đã phát hành. Release: https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.6
  - 12 tệp cài đặt + 6 video kiểm thử, 18/18 khớp SHA-256.
- **Kiểm thử:** 54/54 trên 9 cấu hình:
  - Galaxy Note 9 thật, emulator Android 17 và Android 6.0.
  - Windows: portable, chế độ tương thích win7, Setup.exe, MSI, appxbundle.
  - Không nạp module trình duyệt.
- **Trang chủ:** https://kiritokmn1403.github.io/Applied-Astrology-T-Vi-/ (có mục Tải về `#download`) · **Chính sách quyền riêng tư:** `…/privacy.html`.
- **Việc đang làm dở:** Google Antigravity dịch dần thư viện 611 bài sang 67 ngôn ngữ.
  - Kết quả ghi vào `translations/`; xem tiến độ ở `translations/PROGRESS.md`.
  - Ngày 18/11/2026 mới xong 1/13 chương tiếng Anh.
  - Bản dịch **chưa** được đóng gói vào app.
- **Việc còn lại / ý tưởng bản sau:**
  - Tích hợp bản dịch thư viện theo kiểu tải khi cần.
  - Kiểm thử với VPN thật bật bên ngoài app.
  - Xem lại `47_perf_modal` trên Windows (444 ms, chậm hơn 0.0.5 khoảng 80 ms).

## Người dùng & cách giao tiếp

- Chủ dự án: **Kiệt** (GitHub `kiritokmn1403`). Luôn trả lời và ghi tài liệu **bằng tiếng Việt**.
- Mỗi lần nhận yêu cầu cập nhật: **tóm tắt lại toàn bộ yêu cầu bằng tiếng Việt và chờ xác nhận** trước khi sửa code.
- Kiệt muốn:
  - Thấy rõ tiến trình làm việc.
  - Có ghi tiến độ khoảng 5 phút/lần.
  - Nhận ảnh chụp và video kiểm thử.
  - Nhận tệp cài đặt khi xong.
  - Có thống kê "tính năng mới / lỗi đã sửa so với bản trước".
- Thông tin riêng tư (email, token, mật khẩu) **không bao giờ** ghi vào tài liệu, script hay commit.

Cập nhật lần cuối: 18/11/2026 (v0.0.6) – sắp xếp lại A.I Brain thành 13 tệp (00–12) theo yêu cầu của Kiệt.
