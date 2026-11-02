# Applied Astrology – Tử Vi Ứng Dụng (v0.0.2)

Ứng dụng lập và luận giải lá số Tử Vi theo phương pháp của tác giả **Bửu Đình**. Toàn bộ nội dung được khôi phục từ phần mềm Flash gốc *Tử Vi Ứng Dụng – Bửu Đình.swf* (phát hành 6/2021). Đây là ứng dụng độc lập, mã nguồn mở, chạy trên Android và Windows.

## Tải về (mục Releases / thư mục `output/0.0.2`)

| Nền tảng | Tệp | Ghi chú |
|---|---|---|
| Android 6.0+ (ARM 64-bit) | `AppliedAstrology-0.0.2-arm64-v8a.apk` | Hầu hết điện thoại từ 2016 trở lại đây |
| Android 6.0+ (ARM 32-bit) | `AppliedAstrology-0.0.2-armeabi-v7a.apk` | Máy cũ / máy giá rẻ |
| Android 6.0+ (x86_64) | `AppliedAstrology-0.0.2-x86_64.apk` | Emulator, Chromebook, máy Intel |
| Windows 7 SP1 → 11 (x86/x64/ARM64) | `AppliedAstrology-0.0.2-windows-Setup.exe` | Cài theo người dùng, không cần quyền Admin |
| Windows 7 SP1 → 11 | `AppliedAstrology-0.0.2-windows-<x86/x64/arm64>.msi` | Bộ cài Windows Installer |
| Windows 10/11 | `AppliedAstrology-0.0.2-windows.appxbundle` | Gói MSIX/AppX (x86, x64, ARM64). Cài chứng chỉ `.cer` trước khi cài gói |
| Windows 7 SP1 → 11 | `AppliedAstrology-0.0.2-windows-portable.zip` | Giải nén rồi chạy `AppliedAstrology.exe`, không cần cài |

Windows yêu cầu .NET Framework 4.5 trở lên. Trên Windows 10/11, ứng dụng dùng Edge WebView2. Trên Windows 7/8.1, hoặc khi máy không có WebView2, ứng dụng tự chuyển sang IE11.

## Tính năng

- **Lập lá số**: nhập Dương lịch hoặc Âm lịch (có tháng nhuận); âm lịch Việt Nam múi giờ +7 theo thuật toán Hồ Ngọc Đức. Hiển thị:
  - 14 chính tinh kèm độ sáng Miếu/Vượng/Đắc/Bình/Hãm
  - Hơn 70 phụ tinh theo quy tắc an sao của Bửu Đình
  - Tứ Hóa, Tuần/Triệt
  - Vòng Tràng Sinh, Bác Sĩ, Thái Tuế
  - Đại hạn, Tiểu hạn, sao lưu niên
  - Mệnh chủ/Thân chủ, quan hệ Mệnh–Cục, Âm Dương thuận/nghịch lý
- **Thư viện Bửu Đình**: 611 bài nguyên văn trong 13 chương (Chính Tinh, Can Tinh, Chi Tinh, Các Sao Khác, Các Cung, 72 Cách, An Sao, Hạn, Lá Số Nam, Lá Số Nữ, Từ Điển Tử Vi, 2 Chính Tinh, Các Bộ Sao). Tìm kiếm có dấu hoặc không dấu, đánh dấu bài, đọc tiếp, chỉnh cỡ chữ.
- **Bài liên quan**: chạm vào một cung để xem các bài bình giải phù hợp với chính tinh, vị trí và cung chức.
- **Trợ lý luận giải ngoại tuyến**: tổng quan lá số, các câu hỏi về công danh, tài lộc, hôn nhân, sức khỏe, hạn năm, kèm trích dẫn nguyên văn Bửu Đình.
- **Hồ sơ**: lưu nhiều lá số, xuất ảnh PNG, mã QR, chia sẻ, in.
- **Cài đặt (chỉ nằm trong tab Cài Đặt)**:
  - 68 ngôn ngữ giao diện
  - 4 chủ đề màu, 4 cỡ chữ, 3 bố cục lá số
  - Chế độ màn hình lớn / Samsung DeX
  - Bật/tắt vòng sao, cách tính tháng nhuận
  - Kiểm tra mạng & VPN thật
  - Sao lưu/khôi phục JSON

## Cấu trúc mã nguồn

```
src/web/        Giao diện chung (ES5: chạy trên Android 6 WebView và IE11)
  js/           lunar.js, tuvi_engine.js, library.js, assistant.js, chart_view.js, qr.js, i18n.js, platform.js, app.js
  data/         lib_index.js + lib_01..13.js (thư viện), lang/*.js (ngôn ngữ)
src/android/    Vỏ Android (Java) + thư viện native libaacore.so (C, NDK) cho từng ABI
src/windows/    Vỏ Windows (C# WinForms: WebView2/IE11), bộ cài Setup.exe, mẫu AppX
tools/          Trích xuất SWF, sinh dữ liệu, sinh ngôn ngữ, kiểm thử (engine_test.js, i18n_test.js)
build_release_0.0.2.ps1   Build + kiểm thử + phát hành (chạy qua BUILD_v0.0.2.bat)
```

## Build

Chạy `BUILD_TEST_v0.0.2.bat` để build và kiểm thử, hoặc `BUILD_v0.0.2.bat` để build, kiểm thử và phát hành lên GitHub. Công cụ cần có: JDK 17, Android SDK (build-tools, platform 35, NDK), Windows SDK (makeappx/signtool), .NET Framework 4.x (csc) hoặc Visual Studio 2022 (Roslyn), Node.js. Chi tiết xem `TECHNICAL_UPDATE_NOTES.md`.

## Bản quyền

Mã nguồn ứng dụng: xem `LICENSE`. Nội dung sách thuộc tác giả Bửu Đình (Tử Vũ). Bộ tạo QR chuyển thể từ Project Nayuki (MIT).
