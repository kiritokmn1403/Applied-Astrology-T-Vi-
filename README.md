# Applied Astrology – Tử Vi Ứng Dụng (v0.0.5, native)

Ứng dụng lập và luận giải lá số Tử Vi theo phương pháp của tác giả **Bửu Đình**, kèm **lịch Âm Dương Việt Nam**. Nội dung được khôi phục từ phần mềm Flash gốc *Tử Vi Ứng Dụng – Bửu Đình.swf* (phát hành 6/2021).

Đây là ứng dụng **native** độc lập, mã nguồn mở:
- Android 6.0 trở lên: Java Views + NDK.
- Windows 7 SP1 trở lên: C# WinForms + GDI+.
- Từ v0.0.4 không còn dùng WebView, WebView2 hay IE.

## Tải về – v0.0.5 ([Releases](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.5) · thư mục [`output/0.0.5`](output/0.0.5))

| Nền tảng | Tệp | Ghi chú |
|---|---|---|
| Android 6.0+ (ARM 64-bit) | [`AppliedAstrology-0.0.5-arm64-v8a.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-arm64-v8a.apk) | Hầu hết điện thoại từ 2016 trở lại đây |
| Android 6.0+ (ARM 32-bit) | [`AppliedAstrology-0.0.5-armeabi-v7a.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-armeabi-v7a.apk) | Máy cũ / máy giá rẻ |
| Android 6.0+ (x86_64) | [`AppliedAstrology-0.0.5-x86_64.apk`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-x86_64.apk) | Emulator, Chromebook, máy Intel |
| Windows 7 SP1 → 11 (x86/x64/ARM64) | [`AppliedAstrology-0.0.5-windows-Setup.exe`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows-Setup.exe) | **Bộ cài .exe**: cài cho người dùng hiện tại, không cần quyền Admin, có trình gỡ |
| Windows 10/11 | [`AppliedAstrology-0.0.5-windows.appxbundle`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows.appxbundle) | **Gói .appxbundle** (x86/x64/ARM64). Chạy [`AppliedAstrology-0.0.5-windows-appx-install.bat`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows-appx-install.bat): tệp này tự xin quyền Admin, cài chứng chỉ [`AppliedAstrology-0.0.5-signing-certificate.cer`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-signing-certificate.cer) rồi cài gói |
| Windows 7 SP1 → 11 | [`AppliedAstrology-0.0.5-windows-portable.zip`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows-portable.zip) | **Bản portable**: giải nén rồi chạy `AppliedAstrology.exe`, không cần cài |
| Windows 7 SP1 → 11 | [`x64.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows-x64.msi) · [`x86.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows-x86.msi) · [`arm64.msi`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/AppliedAstrology-0.0.5-windows-arm64.msi) | Bộ cài Windows Installer |
| Kiểm tra toàn vẹn | [`SHA256SUMS-0.0.5.txt`](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/download/v0.0.5/SHA256SUMS-0.0.5.txt) | SHA-256 của mọi tệp |

**Đã cài v0.0.4 (hoặc cũ hơn)?**
- Cài đè trực tiếp (cùng khóa ký); dữ liệu được giữ nguyên.
- Hoặc vào *Cài Đặt › Giới thiệu ứng dụng › Cập nhật* để app tự tải và cài.

Bản ARM64 (MSI arm64, appxbundle) chạy trên Windows 10/11 ARM; Windows 7 chỉ có x86/x64. Windows cần .NET Framework 4.5 trở lên (Windows 8/10/11 có sẵn; Windows 7 SP1 cần cài thêm). Bộ cài được ký bằng chứng chỉ tự tạo, nên SmartScreen có thể vẫn cảnh báo. Khi đó bấm *More info › Run anyway*.

Bản cũ: [v0.0.4](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.4) · [v0.0.3](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.3) · [v0.0.2](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.2)

### Có gì mới ở v0.0.5
- **Hết lag, giật, treo**: Liquid Glass được viết lại. Khúc xạ chỉ tính ở dải viền vát, phần lõi kính do GPU vẽ, mọi phép tính ảnh chạy ở luồng nền. Khi cuộn chỉ vẽ lại thẻ đang hiển thị và thật sự đổi vị trí. Bản Windows vẽ nền và kính không co giãn, có bộ đệm cho từng khung.
- **Công tắc Bật/Tắt Liquid Glass** trong *Cài Đặt › Liquid Glass* (tắt = giao diện phẳng, nhẹ nhất). Máy cấu hình thấp lần đầu tự dùng chế độ Kính mờ.
- **Sửa lỗi Âm lịch "mùng 0"** (ví dụ 7/5/2054, 9/4/2062): kiểm tra toàn dải 1900–2099, 73.018 ngày đều hợp lệ.
- Nền tinh vân hết vệt vòng cung; kính lỏng sắc nét hơn (lấy mẫu song tuyến tính).
- Gói cài nhỏ hơn nhờ nén zopfli. Tự kiểm thử 52 bước, có đo hiệu năng.

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
- **Liquid Glass** *(v0.0.4, viết lại ở v0.0.5 – nhanh, có công tắc Bật/Tắt)*:
  - Thanh tiêu đề, thanh tab và hộp thoại khúc xạ kiểu kính lỏng; thẻ dùng kính mờ.
  - Công tắc Bật/Tắt + 11 tùy chọn trong *Cài Đặt › Liquid Glass*: chế độ (kính mờ / kính lỏng), nền, độ mờ, khúc xạ, viền, phủ màu, phản sáng, bão hòa, kính cho thẻ và lá số.
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
                tests/ (engine/calendar/i18n + native: Dump.java, DumpCs.cs, js_dump.js, GlassDumpJ.java, GlassDumpCs.cs)
                zip_recompress.js + zopfli_vendor/ (nén lại APK/zip bằng zopfli)
build_release_0.0.5.ps1   Build + kiểm thử + phát hành
```

## Build
- `BUILD_ONLY_v0.0.5.bat`: chỉ build.
- `BUILD_TEST_v0.0.5.bat`: build và kiểm thử trên emulator, thiết bị USB, Windows.
- `BUILD_v0.0.5.bat`: build, kiểm thử rồi phát hành.
- `PUBLISH_v0.0.5.bat`: chỉ phát hành.

Công cụ cần có:
- JDK 17
- Android SDK: build-tools, platform 35, NDK, emulator
- Windows SDK (makeappx, signtool)
- Visual Studio 2022 Build Tools (Roslyn)
- WiX 3.14, Node.js, ffmpeg

Không dùng Gradle, NuGet hay WebView2 SDK.

Chi tiết: [`TECHNICAL_UPDATE_NOTES.md`](TECHNICAL_UPDATE_NOTES.md), [`test_reports/0.0.5/TEST_REPORT.md`](test_reports/0.0.5/TEST_REPORT.md), [`A.I Brain - Second Brain`](A.I%20Brain%20-%20Second%20Brain) (bài học kinh nghiệm).

## Bản quyền
- Mã nguồn ứng dụng: xem `LICENSE`.
- Nội dung sách thuộc tác giả Bửu Đình (Tử Vũ).
- Bộ tạo QR từ Project Nayuki (MIT).
- Thuật toán âm lịch của Hồ Ngọc Đức; dữ liệu lịch tham khảo hochanh/amlich, copthuy/amlich.
- Nén zopfli: @gfx/zopfli (Apache-2.0), chỉ dùng lúc build.
- Hiệu ứng Liquid Glass tham khảo các dự án liquid-glass trong sheet Project GitHub. Đã viết lại bằng Java/C#, không chép mã.
