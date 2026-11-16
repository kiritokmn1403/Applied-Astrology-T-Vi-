# Changelog

## 0.0.4 – 16/11/2026
### Mới
- **Viết lại thành ứng dụng native hoàn toàn**. Bản trước chạy giao diện HTML trong WebView/WebView2/IE11. Nay:
  - Android dùng Java Views kết hợp NDK.
  - Windows dùng C# WinForms + GDI+, chạy trên .NET Framework 4.5+.
  - Không còn trình duyệt nhúng nào, nên Windows Task Manager, SmartScreen và Android không còn nhận app là trình duyệt web.
- **Liquid Glass** gồm 3 chế độ: tắt, kính mờ, kính lỏng. Thanh tiêu đề, thanh tab và hộp thoại khúc xạ thật (Snell, mép kính phản sáng); thẻ dùng kính mờ.
  - **Cài Đặt › Liquid Glass** có 11 tùy chọn: chế độ, nền (tinh vân / cực quang / trơn), độ mờ, độ khúc xạ, độ dày viền, độ phủ màu, phản sáng, bão hòa, kính cho thẻ, kính cho lá số và khôi phục mặc định.
- Biểu tượng tab vẽ bằng vector, hiển thị giống nhau trên mọi máy.
- Logo mới (sao 8 cánh) vẽ native.
- Tự kiểm thử **44 bước**, trước là 36. Các bước mới: Liquid Glass, kiểm tra không có WebView, và cả 68 ngôn ngữ.
- Build có bước đối chiếu lõi native với engine JS gốc: 3000 lá số, 4000 ngày âm lịch và 1608 lần đổi Âm→Dương đều khớp. Tệp kết quả của C# trùng từng byte với Java.
### Sửa lỗi / cải thiện
- Android 15+: nội dung và nút không bị thanh trạng thái, thanh điều hướng, tai thỏ hay bàn phím che (giữ nguyên ở bản native).
- Thanh tab: nhãn dài (ví dụ tiếng Pháp) tự thu nhỏ cỡ chữ hoặc rút gọn bằng "…" thay vì đè lên tab bên cạnh.
- Windows 7: bỏ API của .NET 4.6 (ToUnixTimeMilliseconds) để chạy được trên .NET 4.5.
- Khi nâng cấp từ 0.0.3, Setup.exe tự xóa các tệp cũ không còn dùng: `web/`, `runtimes/` và các DLL WebView2.
- Dung lượng giảm: APK 2,94 → 2,87 MB, Setup.exe 3,19 → 2,87 MB, portable 3,17 → 2,85 MB, appxbundle 10,0 → 9,45 MB, MSI 2,31 → 2,13 MB. App mở nhanh hơn vì không phải khởi động trình duyệt.
- Dữ liệu người dùng giữ nguyên khi cập nhật (cùng khóa lưu với 0.0.3).

## 0.0.3 – 03/11/2026
### Mới
- Mục **Âm Lịch** riêng: lịch tháng Âm Dương GMT+7, Can Chi ngày/tháng/năm, nạp âm, 24 tiết khí, ngày Hoàng đạo/Hắc đạo, 12 Trực, giờ hoàng đạo, ngày lễ âm/dương, đổi Dương ↔ Âm, lập lá số từ một ngày.
- **Lục Thập Hoa Giáp & Nạp Âm** kèm năm sinh, và **6 màu giấy lót nền** lá số – bổ sung từ khung lập lá số (Frame 3) của SWF gốc.
- Trang **Giới thiệu ứng dụng** (kiểu Aniyomi/Mangayomi): kiểm tra cập nhật từ GitHub Releases, **tự tải và cài** (Android: trình cài hệ thống; Windows: Setup.exe/MSI/portable/appx), có gì mới, **nhật ký ứng dụng**, **báo cáo lỗi** (JS + sự cố native, gửi GitHub Issues), thông tin gỡ lỗi, giấy phép.
- **68/68 ngôn ngữ dịch đầy đủ** (thêm 17 ngôn ngữ trước đây dùng tiếng Anh).
- Thanh trạng thái/điều hướng Android đổi màu theo giao diện; trình cài appx tự xin quyền Admin.
- Tự kiểm thử 36 bước (từ 24), kiểm thử nâng cấp 0.0.2 → 0.0.3, unit test lịch.
### Sửa lỗi
- Android 15+: nội dung, nút và thanh tab bị thanh trạng thái/thanh điều hướng che (edge-to-edge).
- Android 6.0: không tải được tệp từ GitHub (thiếu root CA DigiCert Global Root G2) – thêm root CA dự phòng.
- appxbundle: làm rõ lỗi 0x80073CF0 (thực chất 0x800B0109 – chứng chỉ chưa được tin cậy ở Local Machine) và thêm trình cài.
- Biểu tượng thanh tab (☾ ☯) hiển thị thành emoji trên Samsung – thay bằng biểu tượng SVG.
- Windows (chế độ IE11 cho Windows 7/8.1): cửa sổ trắng trên máy có driver đồ họa không tương thích – chuyển MSHTML sang dựng hình phần mềm.

## 0.0.2 – 02/11/2026
### Mới
- Thư viện Bửu Đình: 611 bài nguyên văn / 13 chương khôi phục từ SWF, có chương An Sao (giải mã chữ tĩnh). Tìm kiếm toàn văn có dấu hoặc không dấu, đánh dấu bài, đọc tiếp, chỉnh cỡ chữ.
- Bài viết liên quan cho từng cung (theo chính tinh, vị trí, cung chức, cặp sao, bộ sao).
- Trợ lý luận giải ngoại tuyến: tổng quan, công danh, tài lộc, phu thê, sức khỏe, hạn năm, kèm trích dẫn.
- Lưu niên (lưu Thái Tuế, Lộc Tồn, Kình, Đà, Mã, Tứ Hóa), đại hạn và tiểu hạn đang đi, Mệnh chủ/Thân chủ, quan hệ Mệnh–Cục, Âm Dương thuận/nghịch lý.
- Hồ sơ nhiều lá số; xuất PNG, mã QR, chia sẻ, in; sao lưu/khôi phục JSON.
- 68 ngôn ngữ (51 đã dịch đầy đủ), hỗ trợ RTL.
- Android: thư viện native `libaacore.so` cho từng ABI (tìm kiếm C/NDK); hỗ trợ DeX/đa cửa sổ; đọc trạng thái mạng.
- Windows: WebView2 + IE11 (Windows 7), x86/x64/ARM64; Setup.exe (cài không cần quyền Admin, có trình gỡ), MSI, appxbundle, portable.
- Tự kiểm thử 24 bước; script build/kiểm thử/phát hành `build_release_0.0.2.ps1`.
### Sửa lỗi
- Âm lịch sai, 12 cung an ngược chiều, Tuần sai, Khôi/Việt, độ sáng chính tinh, trợ lý giả lập, VPN luôn báo an toàn, APK chứa thư viện của app khác, APK trùng hash, JS ES6 lỗi trên IE11, tên Visicons.

## 0.0.1 – 01/11/2026
- Bản đầu tiên chuyển từ Flash SWF.
