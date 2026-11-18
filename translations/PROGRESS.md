# BẢNG THEO DÕI TIẾN ĐỘ DỊCH THƯ VIỆN TỬ VI BỬU ĐÌNH (translations/PROGRESS.md)

- **Quy mô dự án:** 611 bài luận / 13 chương sách Tử Vi Ứng Dụng (Bửu Đình)
- **Nguồn dữ liệu gốc:** `D:\yh\aa006\stage\src\shared\data\lib\01.json ... 13.json`
- **Số ngôn ngữ đích:** 67 ngôn ngữ (tất cả các ngôn ngữ trong `tools/i18n_v6/*.json` ngoại trừ `vi`)
- **Bộ thuật ngữ chuẩn hóa:** [translations/glossary.json](file:///D:/A.I%20Project/A.I%20Project%20-%20Applied%20Astrology%20(T%E1%BB%AD%20Vi%20%E1%BB%A8ng%20D%E1%BB%A5ng)/Applied%20Astrology%20(T%E1%BB%AD%20Vi)/Applied%20Astrology%20(T%E1%BB%AD%20Vi)/translations/glossary.json)
- **Quy tắc kiểm thử bắt buộc sau mỗi tệp:**
  1. JSON parse thành công 100%.
  2. Số lượng bài đúng bằng bản gốc (Chương 01: 41, 02: 25, 03: 21, 04: 24, 05: 25, 06: 73, 07: 8, 08: 51, 09: 109, 10: 100, 11: 33, 12: 41, 13: 60).
  3. Không có bài rỗng (`len > 0`), thứ tự bài giữ nguyên tuyệt đối.
  4. Giữ nguyên định dạng xuống dòng, đánh số danh sách, bảng biểu.
  5. Dịch trung thực, không tóm tắt, không bình luận.
- **Thứ tự ưu tiên triển khai:** `en`, `zh-CN`, `zh-TW`, `ja`, `ko`, `fr`, `de`, `es`, `ru`, `pt-BR`, `th`, `id`, sau đó đến các ngôn ngữ còn lại.

---

## 1. THỐNG KÊ 13 CHƯƠNG GỐC (TIẾNG VIỆT)

| Chương | Mã tệp | Tên chương | Số bài | Dung lượng gốc (JSON) |
|:---:|:---:|---|:---:|:---:|
| 01 | `01.json` | Chính Tinh | 41 bài | ~885 KB |
| 02 | `02.json` | Can Tinh | 25 bài | ~641 KB |
| 03 | `03.json` | Chi Tinh | 21 bài | ~515 KB |
| 04 | `04.json` | Các Sao Khác | 24 bài | ~460 KB |
| 05 | `05.json` | Các Cung | 25 bài | ~808 KB |
| 06 | `06.json` | 72 Cách | 73 bài | ~1.11 MB |
| 07 | `07.json` | An Sao | 8 bài | ~22 KB |
| 08 | `08.json` | Hạn | 51 bài | ~539 KB |
| 09 | `09.json` | Lá Số Nam | 109 bài | ~936 KB |
| 10 | `10.json` | Lá Số Nữ | 100 bài | ~760 KB |
| 11 | `11.json` | Từ Điển Tử Vi | 33 bài | ~684 KB |
| 12 | `12.json` | 2 Chính Tinh | 41 bài | ~338 KB |
| 13 | `13.json` | Các Bộ Sao | 60 bài | ~411 KB |
| **Tổng** | **13 tệp** | **Toàn bộ kho sách Bửu Đình** | **611 bài** | **~8.11 MB** |

---

## 2. MA TRẬN TIẾN ĐỘ DỊCH (67 NGÔN NGỮ × 13 CHƯƠNG)

*Ký hiệu: ⏳ Chưa dịch | 🔄 Đang dịch | ✅ Đã hoàn thành (Đạt chuẩn kiểm thử)*

| Mã | Tên Ngôn Ngữ | C01 | C02 | C03 | C04 | C05 | C06 | C07 | C08 | C09 | C10 | C11 | C12 | C13 | Tổng Ký Tự | Cập nhật lần cuối |
|:---:|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **en** | English (Ưu tiên 1) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ✅ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 22.575 | 18/11/2026 18:00 |
| **zh-CN** | Chinese Simplified (Ưu tiên 2) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **zh-TW** | Chinese Traditional (Ưu tiên 3) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **ja** | Japanese (Ưu tiên 4) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **ko** | Korean (Ưu tiên 5) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **fr** | French (Ưu tiên 6) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **de** | German (Ưu tiên 7) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **es** | Spanish (Ưu tiên 8) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **ru** | Russian (Ưu tiên 9) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **pt-BR** | Portuguese Brazil (Ưu tiên 10) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **th** | Thai (Ưu tiên 11) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| **id** | Indonesian (Ưu tiên 12) | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |
| *(55 ngôn ngữ còn lại)* | am, ar, as, be, bg, bn, ca, ceb, cs, cv, da, el, eo, es-419, eu, fa, fi, fil, gl, he, hi, hr, hu, it, jv, ka, kk, km, kn, lt, lv, ml, mr, ms, my, nb, ne, nl, nn, pl, pt, ro, sa, sah, sc, sdh, sk, sq, sr, sv, ta, te, tr, uk, uz | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | ⏳ | 0 | 18/11/2026 17:45 |

---

## 3. NHẬT KÝ CHI TIẾT TỪNG TỆP ĐÃ HOÀN THÀNH

- **Tệp:** `translations/lib/en/07.json` (An Sao - 8 bài)
  - **Thời gian hoàn thành:** 18/11/2026 18:00 (GMT+7)
  - **Số bài:** 8/8 (Khớp 100% bản gốc)
  - **Tổng số ký tự:** 22.575 ký tự
  - **Kết quả kiểm thử:**
    - Parse JSON: PASS
    - Kiểm tra bài rỗng: 0 bài rỗng (PASS)
    - Tuân thủ glossary thuật ngữ: PASS (Zi Wei, Tian Fu, Lu Cun, Tai Sui, Tuan, Triet, Four Transformations...)
