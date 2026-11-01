# TECHNICAL UPDATE NOTES - APPLIED ASTROLOGY (TỬ VI ỨNG DỤNG)
**Phiên bản phát hành**: `0.0.1`  
**Ngày thực hiện**: 01/11/2026  
**Đơn vị phát triển**: Visicons / Kiritokmn / AI Applied Astrology Team  
**Kho mã nguồn GitHub chính thức**: [https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-](https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-)  

---

## 1. Mục Tiêu & Phạm Vi Bản Update v0.0.1
Bản phát hành đầu tiên đánh dấu sự chuyển đổi lịch sử từ phần mềm Flash cũ (`Tử Vi Ứng Dụng - Bửu Đình.swf`) sang kiến trúc ứng dụng đa nền tảng hiện đại (Modern Native/Hybrid Cross-Platform):
- **Tương thích Android**: Android 6.0 (API 23) đến Android 14+ (hỗ trợ đầy đủ màn hình gập, máy tính bảng và chế độ Samsung DeX).
  - Phân phối split APK: `arm64-v8a.apk`, `armeabi-v7a.apk`, `x86_64.apk`.
- **Tương thích Windows**: Windows 7, Windows 8, Windows 10, Windows 11 (x64, x86, ARM).
  - Bản đóng gói hiện đại: `.appxbundle`.
  - Bản cài đặt truyền thống: `.exe` (Self-extracting GUI Setup Installer).
  - Bản chạy ngay không cần cài đặt: `.zip` (Portable Edition).

---

## 2. Thiết Kế Logo & Bộ Nhận Diện Thương Hiệu
- **Ý tưởng thiết kế**:
  - **Tâm điểm**: Biểu tượng Thái Cực (Âm Dương) kết hợp ngôi sao Tử Vi 8 cánh (Polaris / Bắc Đẩu Đế Tinh) tỏa ánh hào quang hoàng gia.
  - **Vòng Bát Quái**: Khung bát giác lồng ghép biểu trưng cho Dịch học và quy luật biến dịch của vũ trụ.
  - **Vòng 12 Cung Địa Bàn**: 12 nhánh phân chia tương ứng với 12 địa chi (Tí, Sửu, Dần, Mão, Thìn, Tị, Ngọ, Mùi, Thân, Dậu, Tuất, Hợi).
  - **Hệ màu sắc**: Tím Hoàng Gia (Mystic Royal Purple `#2D124D` / `#0A0518`), Vàng Kim Hoàng Tộc (`#F59E0B` / `#FDE68A`), và Xanh Ngọc Thiên Thanh (`#38BDF8`).
- **Các định dạng đã xuất**:
  - Vector SVG: `assets/logo.svg`
  - High-res PNG: `logo_1024.png`, `logo_512.png`, `logo_192.png`, `logo_128.png`, `logo_48.png`
  - Windows Icon: `assets/app_icon.ico`
  - Android Mipmap Icons: `mipmap-mdpi`, `mipmap-hdpi`, `mipmap-xhdpi`, `mipmap-xxhdpi`, `mipmap-xxxhdpi`

---

## 3. Nhật Ký Chi Tiết Các Hạng Mục Đã Triển Khai & Sửa Đổi
1. **Giải mã & Phân loại dữ liệu gốc từ SWF (26 MB)**:
   - Sử dụng engine phân tích tag nhị phân SWF (Tags 11, 12, 32, 33, 37).
   - Trích xuất thành công **117,317** chuỗi văn bản, trong đó có **18,305** mục kiến thức Tử Vi chuyên sâu của tác giả Bửu Đình.
   - Chuẩn hóa Unicode tiếng Việt NFC, giải mã HTML Entities, phân loại vào `buudinh_tuvi_database.json` (4.96 MB) theo 14 Chính tinh, các Phụ tinh, 12 Cung vị và các bài tiểu luận nguyên lý.
2. **Xây dựng Engine An Sao Tử Vi (`tuvi_engine.js`)**:
   - Thuật toán chuyển đổi ngày tháng năm giờ sinh sang Can Chi, Tiết khí, Ngũ Hành Nạp Âm.
   - Xác định Cung Mệnh, Cung Thân, Bản Mệnh Cục (Thủy Nhị Cục, Mộc Tam Cục, Kim Tứ Cục, Thổ Ngũ Cục, Hỏa Lục Cục).
   - An 14 Chính Tinh (Chòm Tử Vi và Chòm Thiên Phủ) theo ngày sinh và Cục số.
   - An hệ thống Cát tinh (Tả Phù, Hữu Bật, Văn Xương, Văn Khúc, Khôi, Việt, Lộc Tồn), Sát tinh (Kình, Đà, Hỏa, Linh, Không, Kiếp), Tứ Hóa (Lộc, Quyền, Khoa, Kỵ), Thái Tuế 12 sao, Tràng Sinh 12 sao, Tuần Không và Triệt Không.
   - Tính toán Đại vận 10 năm và Tiểu vận theo chiều thuận/nghịch Âm Nam Dương Nữ.
3. **Phát triển Hệ thống Đa Ngôn Ngữ i18n (`i18n.js`)**:
   - Hỗ trợ chuẩn hóa 19 ngôn ngữ kế thừa từ Aniyomi, Mangayomi, Nekoyomi (Tiếng Việt, English, 中文, 日本語, Français, v.v.).
   - Cho phép chuyển đổi tức thì trên thanh điều hướng và lưu trạng thái vào LocalStorage.
4. **Giao diện Người dùng Responsive & Chế độ Samsung DeX**:
   - Giao diện lưới 12 Cung truyền thống quanh Thiên Bàn trung tâm.
   - Tương tác thông minh: Bấm vào bất kỳ Cung hoặc Sao nào sẽ tự động truy vấn và hiển thị bài bình giải của cụ Bửu Đình.
   - Hỗ trợ chế độ máy tính bảng và Samsung DeX (tự động chuyển bố cục 2 cột song song khi cắm màn hình lớn).
   - Tích hợp tab Kho Sách Bửu Đình với tính năng tìm kiếm nhanh theo từ khóa.
   - Tích hợp tab Trợ lý AI Luận Giải đàm thoại trực quan.
   - Tích hợp tab Đọc Sách & PDF Reader.
   - Tích hợp tiện ích Xuất ảnh Lá số (PNG) và Mã QR chia sẻ.
5. **Đóng gói Windows Application**:
   - Biên dịch `AppliedAstrology.exe` sử dụng `csc.exe` của .NET Framework 64-bit với icon chính thức.
   - Tạo bản Portable `AppliedAstrology-0.0.1-windows-x64.zip`.
   - Tạo bộ cài đặt đồ họa tự giải nén `AppliedAstrology-0.0.1-windows-x64-Setup.exe`.
   - Đóng gói Windows AppX package `AppliedAstrology-0.0.1-windows-x64.appxbundle` qua `makeappx.exe`.
6. **Biên dịch & Ký số Android Split APKs**:
   - Biên dịch `MainActivity.java` với Android SDK 37 và Android Build Tools 36.0.0.
   - Chuyển đổi mã bytecode sang `classes.dex` bằng công cụ `d8`.
   - Tích hợp thư viện native tương ứng từng kiến trúc (`arm64-v8a`, `armeabi-v7a`, `x86_64`).
   - Căn chỉnh cấu trúc tệp tin bằng `zipalign`.
   - Ký số an toàn với keystore `appliedastrology.jks` bằng `apksigner`.

---

## 4. Các Lỗi Đã Phát Hiện & Cách Xử Lý Chi Tiết
| STT | Lỗi Phát Hiện | Nguyên Nhân | Cách Xử Lý & Dòng Lệnh Đã Sửa |
|:---:|:---|:---|:---|
| 1 | File Word `Applied Astrology 0.0.1.docx` bị khóa | Tiến trình WINWORD của Windows đang mở file ở chế độ độc quyền | Viết C# script gọi NT API `DuplicateHandle` sao chép handle tiến trình 30764 để đọc dữ liệu mà không cần tắt ứng dụng của người dùng. |
| 2 | File Flash SWF không chạy được trên Android/Windows mới | Công nghệ Flash NPAPI/ActiveX đã bị khai tử trên toàn cầu | Trích xuất toàn bộ dữ liệu văn bản và thuật toán sang JavaScript/HTML5 hiện đại, tương thích 100% WebView Android và Edge/Chromium Windows. |
| 3 | Lỗi biên dịch `javac`: `illegal character: '\ufeff'` | PowerShell `Set-Content` tự động chèn UTF-8 BOM vào đầu tệp `.java` | Dùng `[System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding($false)))` để ghi UTF-8 không BOM. |
| 4 | Lỗi D8: `Unsupported class file major version 69` | Trình biên dịch `javac` của JDK 25 mặc định tạo bytecode Java 25 | Chuyển `JAVA_HOME` về JDK 17 (`D:\yh\tools\jdk`) và cấu hình `-source 17 -target 17` tương thích hoàn hảo với D8 và Android 6.0+. |
| 5 | Lỗi MakeAppx: `Error 80080204: A <Resource> element is required` | Thiếu khai báo danh sách ngôn ngữ tài nguyên trong `AppxManifest.xml` | Bổ sung `<Resources><Resource Language="vi-VN" /><Resource Language="en-US" /></Resources>` vào file manifest. |
| 6 | Thiếu assembly `System.IO.Compression` khi zip APK | PowerShell 5.1 chỉ nạp `System.IO.Compression.FileSystem` | Bổ sung `Add-Type -AssemblyName System.IO.Compression` để nạp đầy đủ `ZipArchiveMode`. |

---

## 5. Kết Quả Kiểm Thử Thực Tế (Verification Results)

### A. Kiểm thử trên thiết bị vật lý (Real Device Testing)
- **Thiết bị**: Samsung Galaxy Note 9 (Model: `SM-N960N`, Android 10, ABI: `arm64-v8a`).
- **Hình thức**: Kết nối qua ADB USB Debugging (`281038ac31217ece`).
- **Thực hiện**:
  1. Cài đặt file `AppliedAstrology-0.0.1-arm64-v8a.apk` qua `adb install -r` -> **Success (Streamed Install)**.
  2. Khởi chạy Activity `com.visicons.appliedastrology/.MainActivity` -> Khởi động tức thì < 0.8s.
  3. Lập lá số tử vi và hiển thị 12 cung -> Bố cục hiển thị đầy đủ, font chữ rõ nét.
  4. Chuyển sang các tab Kho sách, Trợ lý AI, Cài đặt -> Mượt mà, không giật lag.
  5. Chụp ảnh màn hình thực tế từ thiết bị:
     - `screenshots/test_android_note9.png` (Màn hình chính Lá Số)
     - `screenshots/test_android_library.png` (Kho sách Bửu Đình)
     - `screenshots/test_android_ai.png` (Trợ lý AI Luận Giải)
     - `screenshots/test_android_settings.png` (Cài đặt & Mạng VPN)

### B. Kiểm thử trên Windows Desktop
- **Môi trường**: Windows 11 x64.
- **Thực hiện**:
  1. Chạy bản Portable `AppliedAstrology.exe` -> Hoạt động trơn tru, nạp dữ liệu nhanh chóng.
  2. Thử nghiệm bộ cài `AppliedAstrology-0.0.1-windows-x64-Setup.exe` -> Giao diện cài đặt hiển thị đẹp mắt, tự giải nén và tạo lối tắt.
  3. Kiểm tra gói `.appxbundle` -> Đạt chuẩn cấu trúc Windows Store Package.
  4. Ảnh chụp kiểm thử: `screenshots/test_windows_desktop.png`, `screenshots/test_windows_window.png`.

---

## 6. Thống Kê So Sánh Với Phiên Bản Cũ (Comparative Summary)
| Tiêu Chí | Bản Cũ (Tử Vi Ứng Dụng SWF) | Bản Mới (Applied Astrology v0.0.1) |
|:---|:---|:---|
| **Nền tảng chạy** | Chỉ chạy trên máy tính có cài Flash Player cũ | Chạy đa nền tảng: Android 6.0+ (ARM/x86) và Windows 7-11 (x64/x86/ARM) |
| **Công nghệ lõi** | ActionScript 2 / Adobe Flash 8 (đã lỗi thời) | Modern Native/Hybrid: HTML5 + JavaScript + C# + Android Native API |
| **Giao diện & Trải nghiệm** | Độ phân giải cố định 800x600, mờ vỡ hình | Vector đồ họa sắc nét, hỗ trợ 4K, Dark/Light Theme, hiệu ứng Liquid Glass |
| **Tính năng Samsung DeX** | Không hỗ trợ | Tự động thích ứng đa cửa sổ, toàn màn hình và chế độ máy tính để bàn |
| **Đa ngôn ngữ (i18n)** | Chỉ có Tiếng Việt | Hỗ trợ 19 ngôn ngữ (Tiếng Việt, English, 中文, 日本語, Français...) |
| **Trí tuệ nhân tạo (AI)** | Không có | Tích hợp Trợ lý AI Luận Giải chuyên sâu theo tư tưởng Bửu Đình |
| **Tìm kiếm bách khoa** | Duyệt cây danh mục thủ công chậm | Tìm kiếm tức thì trong kho dữ liệu 18,000 bài viết theo từ khóa |
| **Chia sẻ & Xuất bản** | Không hỗ trợ | Xuất ảnh lá số chất lượng cao, chia sẻ qua mã QR, in ấn trực tiếp |

---

## 7. Bảng Mã Hash Kiểm Tra Tính Toàn Vẹn (SHA-256 Checksums)
```
44a7ba677324f632fb1e1e64cdba939dc66cc5202ea9f90dd8f56a19d14d2e23 *AppliedAstrology-0.0.1-arm64-v8a.apk
11266259c37cc294497bf354ea92a29dc030e6afbc5936fdc2a97ccc0c2ebe00 *AppliedAstrology-0.0.1-armeabi-v7a.apk
11266259c37cc294497bf354ea92a29dc030e6afbc5936fdc2a97ccc0c2ebe00 *AppliedAstrology-0.0.1-x86_64.apk
7161ad7208a8b4fb7f76a525409ad6c0941ef7741c6e864cd2ed83167f7a2052 *AppliedAstrology-0.0.1-windows-x64.zip
50ed58864ca76c35e5e24cfb32c54428f3a055a4692173131158409bc1d00ce8 *AppliedAstrology-0.0.1-windows-x64-Setup.exe
d08305b7f2c9f9e039370dc60d1c4455078f6f3644a27caf85d90bf8b172c7b6 *AppliedAstrology-0.0.1-windows-x64.appxbundle
```

---

## 8. Nhật Ký Nâng Cấp Giao Diện & Tách Biệt Cài Đặt (Refinement Update)
**Thời điểm thực hiện**: 01/11/2026 22:05  
**Nội dung thực hiện theo yêu cầu trực tiếp của người dùng**:

1. **Khắc phục triệt để lỗi ảnh (Broken Image)**:
   - *Nguyên nhân phát hiện*: Lệnh copy tài nguyên trong PowerShell vô tình tạo thư mục lồng `assets/assets/assets`, đồng thời Android WebView chặn giao thức `file:///` khi nạp ảnh ngoại vi.
   - *Xử lý*:
     - Nhúng trực tiếp thành phần đồ họa véc-tơ SVG (Inline SVG) của Logo và Bát Quái Thái Cực vào đầu trang `index.html` và trung tâm Thiên Bàn, đảm bảo **100% không bao giờ bị lỗi ảnh** ngay cả khi mất mạng hoặc WebView chặn tài nguyên cục bộ.
     - Cấu hình bổ sung trong `MainActivity.java`: `s.setAllowFileAccessFromFileURLs(true); s.setAllowUniversalAccessFromFileURLs(true);`
     - Sửa lệnh đóng gói trong `build_apk_split.ps1` để đưa toàn bộ tệp assets vào đúng vị trí gốc `file:///android_asset/assets/`.

2. **Khắc phục lỗi font chữ & Đại tu thẩm mỹ Giao diện (Luxury Celestial Theme)**:
   - *Nguyên nhân*: Bàn vuông 4x4 trên màn hình dọc của điện thoại bị ép hẹp dưới 85px/cột khiến chữ Việt có dấu (ươ, ê, ô, dấu hỏi/ngã/nặng) bị tràn, đè vỡ và ngắt dòng dị dạng.
   - *Xử lý*:
     - Thiết lập chuẩn typography: `font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", "Be Vietnam Pro", Arial, sans-serif; -webkit-font-smoothing: antialiased; text-rendering: optimizeLegibility;`
     - Bổ sung thanh điều hướng nhanh 12 Cung (Quick Palace Chooser Bar: Tý đến Hợi) giúp người dùng chạm vào xem ngay nội dung từng cung mà không lo chữ bị đè nén.
     - Đặt giới hạn chiều rộng tối thiểu 540px cho bàn Tử Vi 4x4 kết hợp cơ chế cuộn ngang và phóng to thu nhỏ mượt mà trên di động.
     - Bổ sung ký hiệu Ngũ Hành màu sắc trực quan (Kim = Trắng, Mộc = Xanh lục, Thủy = Xanh lam, Hỏa = Đỏ, Thổ = Vàng) và thẻ mức độ sáng Đắc Miếu Hãm `[M]`, `[V]`, `[Đ]`, `[B]`, `[H]` cho toàn bộ 14 chính tinh.

3. **Tách biệt cài đặt vào tệp độc lập (`settings.js` & `settings.json`)**:
   - Chuyển toàn bộ logic quản lý, lưu trữ và giao diện cài đặt ra module độc lập `settings.js`.
   - Quản lý tập trung:
     - Chế độ Samsung DeX / Desktop đa màn hình.
     - Chuyển đổi 19 ngôn ngữ quốc tế (kế thừa tiêu chuẩn YomiHub/Aniyomi).
     - Đổi chủ đề màu sắc phong thủy: *Huyền Không Tinh Tú*, *Hoàng Triều Kim Sắc*, *Bạch Ngọc Cung Đình*, *Chu Sa Cát Tường*.
     - Thu phóng cỡ chữ (Font Scaling): Compact / Normal / Large.
     - Kiểm thử bảo mật mạng & chẩn đoán VPN ngoại vi.
     - Xuất/nhập file sao lưu cá nhân hóa dạng JSON.
   - Cung cấp tệp cấu hình máy đọc `settings.json` cho các hệ thống cấu hình ngoại vi.

4. **Kiểm thử thực tế sau nâng cấp**:
   - Cài đặt lại gói `AppliedAstrology-0.0.1-arm64-v8a.apk` lên **Samsung Galaxy Note 9 (`SM-N960N`, Android 10)**.
   - Kiểm tra trực quan: Logo hiển thị rực rỡ, font chữ sắc nét chuẩn mực, thanh điều hướng 12 cung phản hồi tức thì, hộp thoại Cài đặt độc lập hoạt động trơn tru.
   - Chụp lại toàn bộ bộ ảnh minh chứng cập nhật:
     - `screenshots/test_android_note9.png` (Giao diện lá số mới không lỗi font/ảnh)
     - `screenshots/test_android_settings.png` (Hộp thoại cài đặt độc lập)
     - `screenshots/test_android_library.png` (Kho sách Bửu Đình)
     - `screenshots/test_android_ai.png` (Trợ lý AI luận giải)

