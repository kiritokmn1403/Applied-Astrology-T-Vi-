# Applied Astrology v0.0.3 – Tử Vi Ứng Dụng

Cập nhật lớn: **Âm Lịch**, **Giới thiệu + tự cập nhật**, sửa giao diện bị che trên **Android 15+**, **68 ngôn ngữ dịch đầy đủ**.

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

## Cài đặt
| Thiết bị | Tệp |
|---|---|
| Android 6.0+ ARM 64-bit (phần lớn điện thoại) | `AppliedAstrology-0.0.3-arm64-v8a.apk` |
| Android 6.0+ ARM 32-bit | `AppliedAstrology-0.0.3-armeabi-v7a.apk` |
| Android x86_64 / emulator / Chromebook | `AppliedAstrology-0.0.3-x86_64.apk` |
| Windows – bộ cài .exe (khuyên dùng) | `AppliedAstrology-0.0.3-windows-Setup.exe` |
| Windows 10/11 – .appxbundle | chạy `AppliedAstrology-0.0.3-windows-appx-install.bat` (cùng thư mục với `.appxbundle` và `.cer`) |
| Windows – portable | `AppliedAstrology-0.0.3-windows-portable.zip` |
| Windows – MSI | `AppliedAstrology-0.0.3-windows-x64.msi` / `-x86.msi` / `-arm64.msi` |

Đã cài 0.0.2: cài đè trực tiếp, không cần gỡ. Kiểm tra toàn vẹn bằng `SHA256SUMS-0.0.3.txt`.

## Kiểm thử
Tự kiểm thử 36 bước trên Samsung Galaxy Note 9 (thiết bị thật), emulator Android 17 và Android 6.0, Windows 11 (WebView2, IE11, Setup.exe, MSI, appxbundle). Video kiểm thử: các tệp `test-video-*.mp4` trong Release này. Chi tiết: `TECHNICAL_UPDATE_NOTES.md`, `test_reports/0.0.3/TEST_REPORT.md`.
