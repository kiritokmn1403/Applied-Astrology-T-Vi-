# 12 – Nhật ký phiên làm việc

Mỗi phiên (người hoặc AI) thêm **một mục ở đầu danh sách** (mới nhất trên cùng) trước khi kết thúc, theo mẫu:

```
## <dd/mm/yyyy hh:mm–hh:mm> · <tác nhân> · <phiên bản> · <mục tiêu>
- Đã làm: …
- Kết quả: … (số liệu, link)
- Còn dở / bàn giao: …
- Bài học đã ghi vào: <tệp A.I Brain>
```
Chi tiết theo phút nằm trong `WORK_PROGRESS_v<ver>.md`; chi tiết kỹ thuật nằm trong `TECHNICAL_UPDATE_NOTES.md`. Nhật ký này chỉ là bản tóm tắt để nhìn nhanh lịch sử.

---

## 18/11/2026 21:21–21:50 · Claude · A.I Brain · Sắp xếp lại bộ nhớ dự án
- Đã làm: viết lại A.I Brain theo danh mục Kiệt đặt:
  - 00 bắt buộc đọc, 01 kiến trúc, 02 quy trình, 03 phát hành, 04 bài học kỹ thuật, 05 kiểm tra, 06 công cụ.
  - 07 quy tắc, 08 mẹo phiên AI, 09 điều phối tác nhân, 10 cấu trúc thư mục, **11 bàn giao, 12 nhật ký**.
  - Gộp `03_lessons_learned.md` cũ vào 04 rồi xóa (Kiệt cấp quyền xóa).
- Kết quả: 13 tệp. Đường dẫn công cụ lấy từ log `00_probe_tools`; cấu trúc thư mục lấy từ `ls` thật.
- Còn dở: các thay đổi A.I Brain mới ở trên máy; cần chạy `PUBLISH_v0.0.6.bat` để đẩy lên GitHub (nếu Kiệt muốn).
- Ghi chú: trong `D:\yh\aa006_refs` có `translate_en_07.js` của Antigravity, nên có thể nó đang dịch bằng script. Chưa kiểm tra.

## 18/11/2026 19:33–19:40 · Claude · v0.0.6 · Mục Tải về trên Trang chủ
- Đã làm:
  - Thêm `#download` vào `docs/index.html`: 12 thẻ có biểu tượng tải, link `releases/download/v0.0.6/…`, dung lượng, liên kết bản cũ.
  - Nút "Tải bản mới nhất" ở đầu trang trỏ tới mục này.
  - Sửa bảng thông số (Setup.exe native; appx x86/x64/arm64).
- Kết quả: 12/12 link trả mã 206; Pages HTTP 200 (37.968 byte); đã mở trang thật kiểm tra hiển thị.
- Bài học: `03` §5, `03` §4 (mỗi bản phải cập nhật mục Tải về).

## 18/11/2026 16:44–19:00 · Claude (+ Google Antigravity qua /swarm) · v0.0.6 · Sửa lag Windows, phân trang, Trang chủ
- Đã làm:
  - Đọc A.I Brain + Excel; tóm tắt và Kiệt xác nhận.
  - Sửa 5 nguyên nhân lag Windows; phân trang kết quả + `show_more` × 68 ngôn ngữ; nạp sẵn thư viện.
  - Android 6: giữ màn hình sáng khi selftest; emulator `-EmuGpu auto`; selftest 54 bước.
  - Script 0.0.6 có bước 32 (GitHub Pages + About), không còn lưu email.
  - 2 lượt BUILD_TEST, PUBLISH 2 lần.
- Antigravity:
  - Kiểm tra Release 0.0.5 (đủ, khớp SHA-256), adb/AVD, đếm ngôn ngữ (68/68).
  - Viết Trang chủ + Chính sách (Claude sửa câu sai và bổ sung các kết nối mạng).
  - Bắt đầu dịch thư viện vào `translations/`.
- Kết quả:
  - **54/54** trên 9 cấu hình. Windows: cuộn 94,6 → 39,2 ms/khung, tìm kiếm 2342 → 506 ms, tab chậm nhất 1955 → 745 ms. Note 9 khung giật 12,67% → 5,69%; Android 6 14,87% → 0%.
  - Release v0.0.6: 18/18 tệp khớp SHA-256; commit `096180b`, `5760d48`.
- Còn dở:
  - Dịch thư viện (1/13 chương tiếng Anh).
  - Chưa kiểm thử VPN thật.
  - `47_perf_modal` Windows 367 → 444 ms.
- Bài học: 04 (lag Windows, emulator), 05, 07, 08, 09.

## 17/11/2026 09:13–~11:40 · Claude, phần cuối do Google Antigravity · v0.0.5 · Sửa lag, công tắc Liquid Glass, Âm lịch
- Đã làm:
  - Viết lại Liquid Glass bất đồng bộ (bản đồ thưa, luồng nền, GPU vẽ lõi); công tắc `lgOn`.
  - Sửa lỗi Âm lịch "mùng 0" (Java/C#/JS); zopfli; selftest 52 bước; đo gfxinfo cũ↔mới.
  - Lượt 1 BUILD_TEST do Claude chạy. Theo TECHNICAL_UPDATE_NOTES, lượt cuối và phát hành do **Antigravity** hoàn tất.
- Kết quả: Note 9 52/52 (giật 1,63%), Android 17 51/52, Android 6 49/52, Windows 51/52 ở cả 5 gói (đều FAIL `46_perf_scroll`, sửa ở 0.0.6).
- Bài học: 04 (lag 0.0.4, mùng 0, Android 17 tự mở lại app), 07 (quy tắc hiệu năng).

## 16/11/2026 21:15–23:29 · Claude · v0.0.4 · Viết lại native hoàn toàn
- Đã làm:
  - Chuyển lõi JS sang Java + C#; Android Java Views không WebView; Windows WinForms không WebView2/IE.
  - Liquid Glass tự vẽ; đối chiếu chéo 3000 lá số / 4000 ngày / 1608 lần Âm→Dương; selftest 44 bước.
- Kết quả: lượt cuối 22:58–23:29 **44/44** trên Note 9, Android 17, Android 6, Windows portable/win7/Setup/MSI/appx; đã phát hành.
- Bài học: 04 (d8 dòng lệnh dài, fitLabel, ClientSize=0…).

## 03/11/2026 15:09–19:28 · v0.0.3 · TLS Android 6, khớp SWF, biểu tượng SVG
- Kết quả: 3 lượt BUILD_TEST; lượt cuối 17:12–17:33 đủ mọi bản sửa và được phát hành; lượt build phát hành cuối 18:01–18:48 (biểu tượng SVG); chạy lại trên Note 9 lúc 19:21.
- Bài học: 04 (CA DigiCert cho Android 6, edge-to-edge Android 15).

## 02/11/2026 · v0.0.2 · Selftest 24 bước, Release tự động
- Kết quả: lượt 6 (15:29–15:52) 24/24 PASS trên Note 9, Android 17, Android 6, WebView2 portable, IE11, Setup, MSI, appx. Bước tạo Release lỗi `git credential fill` và được sửa bằng `PUBLISH_v0.0.2.bat`.
- Bài học: 04 (credential qua stdin).

## 01/11/2026 · v0.0.1 · Bản đầu tiên
- Chuyển phần mềm Flash `Tử Vi Ứng Dụng - Bửu Đình.swf` sang ứng dụng đa nền tảng (WebView), APK chia theo ABI + bộ cài Windows.
