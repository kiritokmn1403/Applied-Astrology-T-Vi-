# Applied Astrology – Tử Vi Ứng Dụng (v0.0.3)

Ứng dụng lập và luận giải lá số Tử Vi theo phương pháp của tác giả **Bửu Đình**, có kèm **lịch Âm Dương Việt Nam**. Toàn bộ nội dung được khôi phục từ phần mềm Flash gốc *Tử Vi Ứng Dụng – Bửu Đình.swf* (phát hành 6/2021). Đây là ứng dụng độc lập, mã nguồn mở, chạy trên Android 6.0 trở lên và Windows 7 SP1 trở lên.

## Tải về – v0.0.3 ([Releases](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.3) · thư mục [`output/0.0.3`](output/0.0.3))

| Nền tảng | Tệp | Ghi chú |
|---|---|---|
| Android 6.0+ (ARM 64-bit) | [`AppliedAstrology-0.0.3-arm64-v8a.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-arm64-v8a.apk) | Hầu hết điện thoại từ 2016 trở lại đây |
| Android 6.0+ (ARM 32-bit) | [`AppliedAstrology-0.0.3-armeabi-v7a.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-armeabi-v7a.apk) | Máy cũ / máy giá rẻ |
| Android 6.0+ (x86_64) | [`AppliedAstrology-0.0.3-x86_64.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-x86_64.apk) | Emulator, Chromebook, máy Intel |
| Windows 7 SP1 → 11 (x86/x64/ARM64) | [`AppliedAstrology-0.0.3-windows-Setup.exe`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows-Setup.exe) | **Bộ cài .exe** – cài theo người dùng, không cần quyền Admin, có trình gỡ |
| Windows 10/11 | [`AppliedAstrology-0.0.3-windows.appxbundle`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows.appxbundle) | **Gói .appxbundle** (x86/x64/ARM64). Chạy [`AppliedAstrology-0.0.3-windows-appx-install.bat`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows-appx-install.bat) (tự xin quyền Admin, cài chứng chỉ [`AppliedAstrology-0.0.3-signing-certificate.cer`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-signing-certificate.cer) rồi cài gói) |
| Windows 7 SP1 → 11 | [`AppliedAstrology-0.0.3-windows-portable.zip`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows-portable.zip) | **Bản portable** – giải nén rồi chạy `AppliedAstrology.exe`, không cần cài |
| Windows 7 SP1 → 11 | [`AppliedAstrology-0.0.3-windows-x64.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows-x64.msi) · [`AppliedAstrology-0.0.3-windows-x86.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows-x86.msi) · [`AppliedAstrology-0.0.3-windows-arm64.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/AppliedAstrology-0.0.3-windows-arm64.msi) | Bộ cài Windows Installer |
| Kiểm tra toàn vẹn | [`SHA256SUMS-0.0.3.txt`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.3/SHA256SUMS-0.0.3.txt) | SHA-256 của mọi tệp |

**Đã cài v0.0.2?** Cài đè trực tiếp (cùng khóa ký). Từ v0.0.3 trở đi, ứng dụng **tự kiểm tra, tải và cài bản mới** (Cài Đặt › Giới thiệu ứng dụng › Cập nhật).

Windows yêu cầu .NET Framework 4.5 trở lên. Trên Windows 10/11, ứng dụng dùng Edge WebView2. Trên Windows 7/8.1, hoặc khi máy không có WebView2, ứng dụng tự chuyển sang IE11.

## Tính năng
- **Lập lá số**: nhập Dương lịch hoặc Âm lịch (có tháng nhuận); âm lịch Việt Nam GMT+7 theo thuật toán Hồ Ngọc Đức. Hiển thị 14 chính tinh (Miếu/Vượng/Đắc/Bình/Hãm), hơn 70 phụ tinh, Tứ Hóa, Tuần/Triệt, vòng Tràng Sinh/Bác Sĩ/Thái Tuế, đại hạn, tiểu hạn, sao lưu niên, Mệnh chủ/Thân chủ, Mệnh–Cục. **6 màu giấy lót nền** như phần mềm gốc.
- **Âm Lịch** *(mới v0.0.3)*: lịch tháng Âm Dương; Can Chi ngày/tháng/năm và nạp âm; 24 tiết khí; ngày Hoàng đạo/Hắc đạo; 12 Trực; giờ hoàng đạo/hắc đạo; ngày lễ âm/dương; đổi Dương ↔ Âm; **Lục Thập Hoa Giáp & Nạp Âm** kèm năm sinh; lập lá số từ một ngày.
- **Thư viện Bửu Đình**: 611 bài nguyên văn / 13 chương; tìm kiếm có dấu hoặc không dấu; đánh dấu, đọc tiếp, chỉnh cỡ chữ.
- **Bài liên quan** cho từng cung; **Trợ lý luận giải ngoại tuyến** kèm trích dẫn nguyên văn.
- **Hồ sơ**: lưu nhiều lá số, xuất PNG, mã QR, chia sẻ, in.
- **Giới thiệu ứng dụng** *(mới v0.0.3, kiểu Aniyomi/Mangayomi)*: kiểm tra cập nhật từ GitHub Releases, tự tải và cài; có gì mới; nhật ký ứng dụng; báo cáo lỗi (gửi lên GitHub Issues); giấy phép mã nguồn mở.
- **Cài đặt**: 68 ngôn ngữ (dịch đầy đủ), 4 chủ đề, 4 cỡ chữ, 3 bố cục, màn hình lớn / Samsung DeX, vòng sao, tháng nhuận, giấy lót nền, kiểm tra mạng & VPN thật, sao lưu/khôi phục JSON.
- **Android 15+**: giao diện tránh thanh trạng thái, thanh điều hướng, tai thỏ và bàn phím.

## Cấu trúc mã nguồn
```
src/web/        Giao diện chung (ES5: chạy trên Android 6 WebView và IE11)
  js/           lunar.js, calendar.js, tuvi_engine.js, library.js, assistant.js, chart_view.js, about.js, qr.js, i18n.js, platform.js, theme.js, app.js
  data/         lib_index.js + lib_01..13.js (thư viện), changelog.js, lang/*.js (66 ngôn ngữ)
src/android/    Vỏ Android (Java: MainActivity, ApkProvider, TlsCompat, NativeCore) + libaacore.so (C, NDK) + certs/
src/windows/    Vỏ Windows (C# WinForms: WebView2/IE11, Updater), Setup.cs, mẫu AppX, trình cài appx
tools/          Trích xuất SWF, i18n (i18n_src, i18n_v3, build_i18n.py, merge_i18n_v3.py), kiểm thử (engine/calendar/i18n)
build_release_0.0.3.ps1   Build + kiểm thử + phát hành
```

## Build
`BUILD_TEST_v0.0.3.bat` (build + kiểm thử trên emulator, thiết bị USB, Windows) · `BUILD_v0.0.3.bat` (build + kiểm thử + phát hành) · `PUBLISH_v0.0.3.bat` (chỉ phát hành). Cần JDK 17, Android SDK (build-tools, platform 35, NDK, emulator), Windows SDK (makeappx/signtool), Visual Studio 2022 Build Tools (Roslyn), WiX 3.14, Node.js, ffmpeg. Chi tiết và nhật ký kiểm thử: [`TECHNICAL_UPDATE_NOTES.md`](TECHNICAL_UPDATE_NOTES.md), [`test_reports/0.0.3/TEST_REPORT.md`](test_reports/0.0.3/TEST_REPORT.md).

## Bản quyền
Mã nguồn ứng dụng: xem `LICENSE`. Nội dung sách thuộc tác giả Bửu Đình (Tử Vũ). Bộ tạo QR chuyển thể từ Project Nayuki (MIT). Dữ liệu lịch tham khảo hochanh/amlich, copthuy/amlich.
