# Changelog

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
- Biểu tượng ☾ hiển thị thành emoji trên Samsung.
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
