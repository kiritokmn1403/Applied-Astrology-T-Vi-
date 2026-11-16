# Applied Astrology – Tử Vi Ứng Dụng (v0.0.4, native)

Ứng dụng lập và luận giải lá số Tử Vi theo phương pháp của tác giả **Bửu Đình**, kèm **lịch Âm Dương Việt Nam**. Nội dung được khôi phục từ phần mềm Flash gốc *Tử Vi Ứng Dụng – Bửu Đình.swf* (phát hành 6/2021).

Đây là ứng dụng **native** độc lập, mã nguồn mở:
- Android 6.0 trở lên: Java Views + NDK.
- Windows 7 SP1 trở lên: C# WinForms + GDI+.
- Từ v0.0.4 không còn dùng WebView, WebView2 hay IE.

## Tải về – v0.0.4 ([Releases](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.4) · thư mục [`output/0.0.4`](output/0.0.4))

| Nền tảng | Tệp | Ghi chú |
|---|---|---|
| Android 6.0+ (ARM 64-bit) | [`AppliedAstrology-0.0.4-arm64-v8a.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-arm64-v8a.apk) | Hầu hết điện thoại từ 2016 trở lại đây |
| Android 6.0+ (ARM 32-bit) | [`AppliedAstrology-0.0.4-armeabi-v7a.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-armeabi-v7a.apk) | Máy cũ / máy giá rẻ |
| Android 6.0+ (x86_64) | [`AppliedAstrology-0.0.4-x86_64.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-x86_64.apk) | Emulator, Chromebook, máy Intel |
| Windows 7 SP1 → 11 (x86/x64/ARM64) | [`AppliedAstrology-0.0.4-windows-Setup.exe`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows-Setup.exe) | **Bộ cài .exe**: cài cho người dùng hiện tại, không cần quyền Admin, có trình gỡ |
| Windows 10/11 | [`AppliedAstrology-0.0.4-windows.appxbundle`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows.appxbundle) | **Gói .appxbundle** (x86/x64/ARM64). Chạy [`AppliedAstrology-0.0.4-windows-appx-install.bat`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows-appx-install.bat): tệp này tự xin quyền Admin, cài chứng chỉ [`AppliedAstrology-0.0.4-signing-certificate.cer`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-signing-certificate.cer) rồi cài gói |
| Windows 7 SP1 → 11 | [`AppliedAstrology-0.0.4-windows-portable.zip`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows-portable.zip) | **Bản portable**: giải nén rồi chạy `AppliedAstrology.exe`, không cần cài |
| Windows 7 SP1 → 11 | [`x64.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows-x64.msi) · [`x86.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows-x86.msi) · [`arm64.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/AppliedAstrology-0.0.4-windows-arm64.msi) | Bộ cài Windows Installer |
| Kiểm tra toàn vẹn | [`SHA256SUMS-0.0.4.txt`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.4/SHA256SUMS-0.0.4.txt) | SHA-256 của mọi tệp |

**Đã cài v0.0.3?**
- Cài đè trực tiếp (cùng khóa ký); dữ liệu được giữ nguyên.
- Hoặc vào *Cài Đặt › Giới thiệu ứng dụng › Cập nhật* để app tự tải và cài.

Windows cần .NET Framework 4.5 trở lên (Windows 8/10/11 có sẵn; Windows 7 SP1 cần cài thêm). Bộ cài được ký bằng chứng chỉ tự tạo, nên SmartScreen có thể vẫn cảnh báo. Khi đó bấm *More info › Run anyway*.

Bản cũ: [v0.0.3](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.3) · [v0.0.2](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.2)

## Tính năng
- **Lập lá số**:
  - Nhập Dương lịch hoặc Âm lịch (có tháng nhuận). Âm lịch Việt Nam GMT+7 theo thuật toán Hồ Ngọc Đức.
  - Có 14 chính tinh (Miếu/Vượng/Đắc/Bình/Hãm), hơn 70 phụ tinh, Tứ Hóa, Tuần/Triệt, vòng Tràng Sinh/Bác Sĩ/Thái Tuế, đại hạn, tiểu hạn, sao lưu niên, Mệnh chủ/Thân chủ, Mệnh–Cục.
  - 6 màu giấy lót nền.
- **Âm Lịch**:
  - Lịch tháng Âm Dương; Can Chi ngày/tháng/năm, nạp âm, 24 tiết khí.
  - Ngày Hoàng đạo/Hắc đạo, 12 Trực, giờ hoàng đạo, ngày lễ.
  - Đổi Dương ↔ Âm; Lục Thập Hoa Giáp; lập lá số từ một ngày.
- **Thư viện Bửu Đình**: 611 bài nguyên văn trong 13 chương. Tìm kiếm có dấu hoặc không dấu (lõi tìm kiếm C/NDK trên Android); đánh dấu, đọc tiếp, chỉnh cỡ chữ.
- **Bài liên quan** cho từng cung, và **Trợ lý luận giải ngoại tuyến** kèm trích dẫn.
- **Hồ sơ**: lưu nhiều lá số, xuất PNG, mã QR, chia sẻ, in.
- **Liquid Glass** *(mới v0.0.4)*:
  - Thanh tiêu đề, thanh tab và hộp thoại khúc xạ kiểu kính lỏng; thẻ dùng kính mờ.
  - 11 tùy chọn trong *Cài Đặt › Liquid Glass*: chế độ, nền, độ mờ, khúc xạ, viền, phủ màu, phản sáng, bão hòa, kính cho thẻ và lá số.
- **Giới thiệu ứng dụng**: kiểm tra cập nhật từ GitHub Releases, tự tải và cài; có gì mới; nhật ký; báo cáo lỗi; giấy phép.
- **Cài đặt**:
  - 68 ngôn ngữ (đổi ngay trong app), 4 chủ đề, 4 cỡ chữ, 3 bố cục, chế độ màn hình lớn / Samsung DeX.
  - Kiểm tra mạng & VPN; sao lưu/khôi phục JSON.
- **Android 15+**: giao diện tránh thanh trạng thái, thanh điều hướng, tai thỏ và bàn phím.

## Cấu trúc mã nguồn
```
src/android/    App Android native (Java, không AndroidX/WebView):
                core/ (Lunar, Engine, Cal, Library, I18n, Assistant, Theme, Glass, Json)
                + Screens, MainActivity, GlassViews, ChartViews, MonthView, Updates, SelfTest
                + jni/aacore.c (tìm kiếm NDK) + certs/ (root CA dự phòng cho Android 6)
src/windows/    App Windows native (C# WinForms + GDI+, .NET 4.5):
                Core.cs, Core2.cs (cùng lõi với Java), Qr.cs, WinHost, WinUi, WinMain, WinScreens, WinSelfTest
                + Setup.cs, mẫu AppX, trình cài appx
src/web/        Bản gốc v0.0.3 (HTML/JS). Là nguồn dữ liệu và "chuẩn" để đối chiếu lõi native
src/legacy_0.0.3/  Vỏ WebView2/IE cũ (lưu trữ)
tools/          build_native_data.js (sinh src/shared/data), i18n, trích xuất SWF,
                tests/ (engine/calendar/i18n + native: Dump.java, DumpCs.cs, js_dump.js)
build_release_0.0.4.ps1   Build + kiểm thử + phát hành
```

## Build
- `BUILD_ONLY_v0.0.4.bat`: chỉ build.
- `BUILD_TEST_v0.0.4.bat`: build và kiểm thử trên emulator, thiết bị USB, Windows.
- `BUILD_v0.0.4.bat`: build, kiểm thử rồi phát hành.
- `PUBLISH_v0.0.4.bat`: chỉ phát hành.

Công cụ cần có:
- JDK 17
- Android SDK: build-tools, platform 35, NDK, emulator
- Windows SDK (makeappx, signtool)
- Visual Studio 2022 Build Tools (Roslyn)
- WiX 3.14, Node.js, ffmpeg

Không dùng Gradle, NuGet hay WebView2 SDK.

Chi tiết: [`TECHNICAL_UPDATE_NOTES.md`](TECHNICAL_UPDATE_NOTES.md), [`test_reports/0.0.4/TEST_REPORT.md`](test_reports/0.0.4/TEST_REPORT.md), [`A.I Brain - Second Brain`](A.I%20Brain%20-%20Second%20Brain) (bài học kinh nghiệm).

## Bản quyền
- Mã nguồn ứng dụng: xem `LICENSE`.
- Nội dung sách thuộc tác giả Bửu Đình (Tử Vũ).
- Bộ tạo QR từ Project Nayuki (MIT).
- Thuật toán âm lịch của Hồ Ngọc Đức; dữ liệu lịch tham khảo hochanh/amlich, copthuy/amlich.
- Hiệu ứng Liquid Glass tham khảo các dự án liquid-glass trong sheet Project GitHub. Đã viết lại bằng Java/C#, không chép mã.
