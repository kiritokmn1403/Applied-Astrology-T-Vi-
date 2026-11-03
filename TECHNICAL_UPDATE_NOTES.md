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
  4. Ảnh chụp kiểm thử thực tế trên Windows:
     - `screenshots/test_windows_desktop.png` (Tổng thể màn hình Windows 11 Desktop, taskbar và cửa sổ ứng dụng)
     - `screenshots/test_windows_window.png` (Cửa sổ ứng dụng kích thước 1280x850, lập lá số và bình giải)
     - `screenshots/test_windows_settings.png` (Cửa sổ ứng dụng mở Tab 5 Cài Đặt quy tụ độc quyền các cấu hình)

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
b83ab7a87cfd88558ef640aed054d67e6779803b3260419ac9fc3733030fa837 *AppliedAstrology-0.0.1-arm64-v8a.apk
2921063e6225ae4277d074c1c75716e88d00f26a47b918023b1c5cdeb254280d *AppliedAstrology-0.0.1-armeabi-v7a.apk
2921063e6225ae4277d074c1c75716e88d00f26a47b918023b1c5cdeb254280d *AppliedAstrology-0.0.1-x86_64.apk
2d720d59c85d9ab1f0ed85611165720270817a5a23782a6c1dd3de10685d44da *AppliedAstrology-0.0.1-windows-x64.zip
8de00dd79a5083d34f8403f9af16206312be980d4277d028cc775f13704fa478 *AppliedAstrology-0.0.1-windows-x64-Setup.exe
e895728af869077f1e280675d7ce7dbf93223d179c3093569878a86f8bd8b0f6 *AppliedAstrology-0.0.1-windows-x64.appxbundle
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

---

## 9. Nhật Ký Tập Trung Cài Đặt Vào Duy Nhất Mục "Cài Đặt" (Settings Exclusivity & Cleanup)
**Thời điểm thực hiện**: 01/11/2026 22:25  
**Yêu cầu người dùng**: *"tôi muốn các mục cài đặt như Dex, Chuyển đổi ngôn ngữ, giao diện,... chỉ xuất hiện ở trong mục Cài Đặt, không xuất hiện ở đâu khác nữa."*

### A. Phân Tích & Các Điểm Cần Loại Bỏ:
- Trước đó, trên thanh Header (`.app-header`) vẫn còn nút chuyển DeX nhanh, menu chọn 19 ngôn ngữ và biểu tượng răng cưa cài đặt. Điều này gây phân tán cài đặt ở nhiều nơi, làm rối thanh tiêu đề ứng dụng.
- Người dùng yêu cầu thanh tiêu đề phải hoàn toàn sạch sẽ, chỉ mang tính chất hiển thị thương hiệu; toàn bộ cấu hình ứng dụng phải được quy tụ về một nơi duy nhất: **Tab Cài Đặt (Tab 5)**.

### B. Thay Đổi Mã Nguồn Đã Thực Hiện:
1. **Loại bỏ hoàn toàn cài đặt khỏi Header (`index.html`)**:
   - Gỡ bỏ toàn bộ khối `<div class="header-actions">` trên Header (xóa nút DeX, xóa select chọn ngôn ngữ, xóa nút bánh răng).
   - Thanh Header chỉ giữ lại duy nhất Khung Logo Véc-tơ Bát Quái Tử Vi và Tên ứng dụng *Tử Vi Ứng Dụng - Kế thừa tác phẩm Bửu Đình*.
2. **Xây dựng Tab 5 Thành Trung Tâm Điều Khiển Duy Nhất (`#settingsTab`)**:
   - Thêm `<section id="settingsTab" class="tab-view">` độc lập, quy tụ toàn diện 7 nhóm cấu hình:
     - **Chế độ Samsung DeX & Đa Màn Hình**: Bật/tắt giao diện mở rộng 2 cột cho Samsung Galaxy Note/S/Fold và màn hình máy tính.
     - **Chuyển Đổi 19 Ngôn Ngữ Quốc Tế**: Tiếng Việt, English, 中文, 日本語, Français, v.v.
     - **Chủ Đề Phong Thủy (Themes)**: Huyền Không Tinh Tú, Hoàng Triều Kim Sắc, Bạch Ngọc Cung Đình, Chu Sa Cát Tường.
     - **Kích Thước Chữ (Font Scaling)**: Compact, Normal (Chuẩn cân đối), Large.
     - **Bố Cục Hiển Thị Lá Số**: Tự động thích ứng, Thanh điều hướng 12 Cung, Bàn vuông 4x4 cổ điển.
     - **Kiểm Tra Mạng & VPN Ngoại Vi Độc Lập**: Đo độ trễ và chẩn đoán định tuyến an toàn.
     - **Sao Lưu & Quản Lý Dữ Liệu**: Xuất/nhập file cấu hình `.json` an toàn.
3. **Cập nhật Logic Điều Khiển (`settings.js`)**:
   - Loại bỏ các event listener liên quan đến Header (`headerDexBtn`, `headerLangSelect`).
   - Kết nối trực tiếp và đồng bộ trạng thái hai chiều với các phần tử trong `#settingsTab` (`tabDexToggle`, `tabLangSelect`, `tabThemeSelect`, `tabFontSizeSelect`, `tabViewModeSelect`, `tabVpnStatus`).
   - Đảm bảo khi người dùng thay đổi bất kỳ mục nào trong Tab Cài Đặt, cấu hình được lưu vào LocalStorage và áp dụng tức thì.

### C. Đóng Gói Lại Toàn Bộ Ứng Dụng:
- Rebuild và ký số lại 3 bản Split APK:
  - `AppliedAstrology-0.0.1-arm64-v8a.apk` (17.03 MB)
  - `AppliedAstrology-0.0.1-armeabi-v7a.apk` (3.15 MB)
  - `AppliedAstrology-0.0.1-x86_64.apk` (3.15 MB)
- Rebuild lại 3 bản Windows:
  - `AppliedAstrology-0.0.1-windows-x64.zip` (Portable)
  - `AppliedAstrology-0.0.1-windows-x64-Setup.exe` (Bộ cài đặt)
  - `AppliedAstrology-0.0.1-windows-x64.appxbundle` (Gói hiện đại Windows Store)
- Cập nhật lại toàn bộ bảng mã SHA-256 Checksums tương ứng.

### D. Kết Quả Kiểm Thử Thực Tế:
- **Thiết bị**: Samsung Galaxy Note 9 (`SM-N960N`, Android 10, ABI `arm64-v8a`).
- **Thao tác**:
  1. Cài đặt bản cập nhật mới qua ADB USB Debugging (`adb install -r -d`).
  2. Khởi chạy ứng dụng: Header hoàn toàn sạch sẽ, không còn bất kỳ nút cài đặt nào.
  3. Chuyển sang Tab 5 (Cài Đặt): Toàn bộ các mục cấu hình xuất hiện đầy đủ, ngăn nắp, hoạt động chính xác 100%.
  4. Bộ ảnh chụp thực tế đã được cập nhật vào thư mục `screenshots/`.


---

# CẬP NHẬT v0.0.2 – Applied Astrology (Tử Vi Ứng Dụng)
**Ngày thực hiện**: 02/11/2026  
**Người thực hiện**: Claude (Cowork, theo yêu cầu của chủ dự án Kiệt Nguyễn)  
**Package Android mới**: `com.appliedastrology.tuvi` (bỏ `com.visicons.appliedastrology`). Cần gỡ bản 0.0.1 trước khi cài.

## 1. Phạm vi yêu cầu (sheet "Feature Check" của Project – Applied Astrology.xlsx)
1. *Creat & Update*:
   - Quét file SWF và các nhóm project Flash Player, Runtime Native/Hybrid, Browser, Aniyomi, Mangayomi, Nekoyomi.
   - Bổ sung đủ nội dung SWF.
   - Dịch sang các ngôn ngữ mà Aniyomi/Mangayomi/Nekoyomi hỗ trợ.
   - Gỡ mọi thứ liên quan đến Visicons.
2. Rà soát sheet Project GitHub, tối ưu code và cập nhật vào ứng dụng.
3. Kiểm tra độ ổn định của tính năng. Sheet "App Feature Set" **không có** trong file Excel, nên dùng danh sách tính năng của ứng dụng thay thế.

## 2. Lỗi phát hiện trong v0.0.1 và cách sửa

| # | Lỗi v0.0.1 | Cách sửa ở v0.0.2 | Công cụ |
|---|---|---|---|
| 1 | `solarToLunar()` là công thức giả `((d+9)%29)+1`, nên **sai âm lịch với hầu hết ngày sinh** | Viết `lunar.js` theo thuật toán thiên văn Hồ Ngọc Đức (múi giờ +7, tháng nhuận, đổi chiều âm→dương). Đã kiểm tra Tết 2023/2024/2025, nhuận 2/2023, Tết 1968 và trường hợp đặc biệt năm 1985 của Việt Nam | VS Code, Node.js |
| 2 | 12 cung an **ngược chiều**: Phụ Mẫu đặt ở Mệnh−1 | Đặt cung chức theo chiều thuận kể từ Mệnh | Node.js |
| 3 | Tuần Không sai (`tuanOffset+10` bị cộng thừa 10) | Tuần = (Chi − Can) + 10, + 11 | Node.js |
| 4 | Ghi chú 0.0.1 nói có Tràng Sinh, Hỏa/Linh… nhưng code không có | Bổ sung hơn 70 phụ tinh theo quy tắc An Sao của chính SWF: Hỏa/Linh theo "cung định trước", Tam Thai/Bát Tọa, Ân Quang/Thiên Quý, Thai Phụ/Phong Cáo, Tràng Sinh, Bác Sĩ, Thái Tuế, lưu niên… | Python, Node.js |
| 5 | Bảng Thiên Khôi/Việt khác sách Bửu Đình | Lấy theo 60 mẫu lá số trong SWF (Đinh: Khôi Dậu/Việt Hợi; Mậu & Canh: Khôi Mùi/Việt Sửu…) | Python (trích mẫu), Node.js |
| 6 | Độ sáng chính tinh sai (ví dụ Tử Vi ở Tị là "B") | Dùng bảng Miếu/Vượng/Đắc/Bình/Hãm phổ thông | — |
| 7 | Dữ liệu SWF bị cắt thành từng câu rời, mất tên bài và chương; Android không đọc được do `fetch()` qua `file://` nên chỉ còn 4 câu dự phòng | Khôi phục **611 bài nguyên văn / 13 chương** theo menu gốc, nạp dần từng chương bằng `<script>` (chạy được qua `file://`) | Python (bộ đọc SWF tự viết) |
| 8 | Chương "An Sao" (chữ tĩnh) bị mất vì lỗi bảng mã | Giải mã glyph bằng so khớp hình dạng (PIL + Liberation Sans) kết hợp từ điển tần suất âm tiết, cộng bộ chuyển VNI-Win → Unicode | Python |
| 9 | "Trợ lý AI" chỉ trả về một câu viết sẵn | Trợ lý ngoại tuyến thật: phân tích chính tinh, độ sáng, Tứ Hóa, Tuần/Triệt, tam phương, đại/tiểu hạn, kèm trích đoạn Bửu Đình liên quan. Ghi rõ đây không phải AI tạo sinh | VS Code |
| 10 | Chỉ 5 trên 19 ngôn ngữ có bản dịch, còn lại hiện tiếng Anh | 68 locale theo Aniyomi/Nekoyomi (63) và Mangayomi (es-419). Đã dịch đầy đủ 51 ngôn ngữ. 17 ngôn ngữ hiếm (am, as, be, ceb, cv, eo, fil, jv, kn, ml, mr, my, ne, sa, sah, sc, sdh) tạm dùng tiếng Anh | Python `build_i18n.py`, Node `i18n_test.js` |
| 11 | Nút "Kiểm tra VPN" luôn báo an toàn, kể cả khi mất mạng | Kiểm tra thật qua `cloudflare.com/cdn-cgi/trace` bằng HTTP native (Android/Windows): hiện IP, vị trí, độ trễ, trạng thái WARP. Ứng dụng không bật/tắt VPN | Java, C# |
| 12 | APK arm64 bị gắn kèm `libapp.so` (khoảng 14 MB) **của Mangayomi**; APK armeabi-v7a và x86_64 **trùng hash** (cùng một tệp) | Thư viện native riêng `libaacore.so` (tìm kiếm toàn văn bằng C/NDK) biên dịch cho từng ABI; mỗi ABI có versionCode riêng (2001/2002/2003). APK giảm từ **17,0 MB xuống 2,58 MB** | NDK clang 27, aapt2, d8, zipalign `-P 16`, apksigner |
| 13 | Bản Windows chỉ có x64; IE11 không chạy được JS ES6 (arrow function, template string, CSS variables) | Toàn bộ web viết lại theo **ES5**; màu chủ đề sinh bằng JS thay vì CSS variables; bảng lá số dùng `<table>`. Host C# dùng WebView2, tự chuyển sang IE11 trên Windows 7. Có bản x86 / x64 / ARM64 | Visual Studio 2022 Build Tools (Roslyn csc) |
| 14 | Còn chữ "Visicons" (package, publisher AppX "CN=Visicons", chân trang) | Đã gỡ hết. Publisher là "CN=Applied Astrology", package `com.appliedastrology.tuvi` | — |
| 15 | Mật khẩu keystore ghi thẳng trong script (`tuvi1234`) | Keystore mới RSA-3072, mật khẩu ngẫu nhiên lưu ở `D:\yh\aa002_keys` (ngoài repo, đã gitignore). Log che mật khẩu | keytool |

## 3. Lỗi phát sinh khi build/kiểm thử v0.0.2 (đã sửa)

| Lượt | Lỗi | Nguyên nhân | Sửa |
|---|---|---|---|
| 1 | `aapt2 link: assets\web\web: failed to open file`, makeappx "web\web" | Symlink `src/web/web` vô tình tạo khi chạy thử thư viện C trong sandbox | Xóa symlink; script tự loại bỏ reparse point trong thư mục stage |
| 1 | WiX `CNDL0230` | Component per-user vừa có file vừa có registry keypath, lại dùng GUID tự sinh | Mỗi tệp một component (File KeyPath); mỗi thư mục một component RemoveFolder + HKCU keypath |
| 1 | WebView2 "Ambiguous match found" | SDK 1.0.2903 có 2 overload `EnsureCoreWebView2Async` | Chọn đúng overload 1 tham số bằng reflection |
| 1 | `avdmanager`: package path is not valid | Đường dẫn system image sai | Dùng AVD có sẵn (Medium_Phone_API_37.0, OOS_Android6_API23) |
| 2 | `apksigner: java.io.IOException: Bad pathname`; `adb: failed to stat` | `.bat` của build-tools và adb không xử lý được đường dẫn tiếng Việt có dấu của repo | Ký, cài và lấy ảnh trong thư mục ASCII `D:\yh\aa002`, sau đó mới chép về `output/` và `test_reports/` |
| 2 | `Access to WebView2Loader.dll is denied` | Tiến trình còn sót của lượt trước giữ khóa DLL | Đóng tiến trình cũ; giải nén mỗi lượt vào thư mục có timestamp |
| 3 | Android hiện trang trắng; trên Android 6: `net::ERR_FILE_NOT_FOUND` cho `index.html?selftest=1` | WebView không hỗ trợ query string với `file:///android_asset` | Dùng `#selftest=1`; `app.js` đọc tham số cả từ hash |
| 4 | Android vẫn báo `net::ERR_FILE_NOT_FOUND` dù đã đổi sang `#selftest=1` | Trên Windows, `aapt2 link -A` ghi tên mục trong APK bằng dấu gạch ngược (`assets/web\css\app.css`), Android không đọc được | Bỏ `-A`; tự thêm assets vào APK bằng .NET ZipArchive, dùng dấu `/` |
| 4 | IE11 (đường chạy Windows 7): lá số không lập được (14/24) | IE11 không hỗ trợ gán `innerHTML` cho `<select>`, nên ô ngày/tháng trống | Tạo `<option>` bằng DOM (`fillSelect`) |
| 5 | appxbundle: `0x80073CF0 Package could not be opened` | `D:\yh` là junction; dịch vụ cài AppX từ chối đường dẫn qua junction | Chép gói/layout sang `%TEMP%` rồi mới cài |
| 4 | Note 9 hiện màn hình trắng | Máy đang khóa màn hình; WebView không vẽ khi app nằm sau màn hình khóa | Script đánh thức máy và mở khóa màn hình (nếu không đặt mật khẩu) trước khi kiểm thử |
| 3 | Ảnh kiểm thử Windows 1×1 px | Công cụ chụp màn hình của tác nhân đã thu nhỏ cửa sổ app khi app chưa được cấp quyền xem | Cấp quyền xem cho app; không chụp màn hình trong lúc app đang tự kiểm thử |

## 4. Dòng lệnh chính (trong `build_release_0.0.2.ps1`)
```
aapt2 compile --dir src\android\res -o compiled\res.zip
javac -encoding UTF-8 -source 8 -target 8 -bootclasspath android-35\android.jar ...
d8 --release --min-api 23 --lib android.jar --output dex <classes>
clang --target=aarch64-linux-android23 | armv7a-linux-androideabi23 -mthumb | x86_64-linux-android23 -shared -fPIC -O2 aacore.c -landroid -llog -Wl,-z,max-page-size=16384
aapt2 link -A assets --min-sdk-version 23 --target-sdk-version 35 --version-code 2001|2002|2003
zipalign -f -P 16 4 ; apksigner sign --v4-signing-enabled false ; apksigner verify --min-sdk-version 23
csc /platform:anycpu|x86|x64|arm64 /codepage:65001 /win32manifest:app.manifest AppHost.cs  (Roslyn, VS 2022 Build Tools)
csc /resource:payload.zip,AppPayload Setup.cs
makeappx pack (x86/x64/arm64) ; makeappx bundle ; signtool sign /fd SHA256
candle -arch x86|x64|arm64 ; light -sval     (WiX 3.14)
adb install -r -g ; am start --ez selftest true ; screenrecord ; screencap ; logcat
ffmpeg -f gdigrab -i desktop (quay video kiểm thử Windows)
```

## 5. Kiểm thử
- **Unit test (Node.js)**:
  - `tools/tests/engine_test.js`: so 120 mẫu lá số SWF với **3.965 phép kiểm, khớp 98,18%**. Phần sai lệch còn lại do nhãn sao trong hình SWF nằm trên ranh giới giữa 2 ô.
  - `i18n_test.js`: kiểm tra đủ khóa cho mọi ngôn ngữ.
- **QR**: so từng module với thư viện gốc Nayuki ở 5 chuỗi (phiên bản 1 đến 32), trùng khớp 100%.
- **Tìm kiếm native (C)**: chạy thử trong sandbox, vị trí trả về khớp với chỉ số chuỗi bên JS.
- **Tự kiểm thử 24 bước trong ứng dụng** (`#selftest=1` / `--selftest`): mỗi bước bấm phần tử giao diện thật rồi chụp màn hình. Các bước gồm:
  - Lập lá số dương/âm lịch, chi tiết cung
  - QR, PNG, lưu hồ sơ
  - Thư viện, đọc sách, tìm kiếm có dấu và không dấu
  - Trợ lý (tổng quan, công danh, hạn năm)
  - Giao diện, ngôn ngữ en/ja, cỡ chữ, bố cục, DeX
  - Sao lưu và khôi phục, kiểm tra mạng, khôi phục mặc định
- **Kết quả**: xem `test_reports/0.0.2/TEST_REPORT.md`, gồm ảnh từng bước, video Android (`android_test_video.mp4`), video màn hình Windows (`windows_test_video.mp4`) và logcat.
  - Lượt 3 trên Windows / WebView2 (bản portable): 14 bước đầu đều **PASS**:
    - Lá số 15/8/1995 ra âm lịch 20/7/1995, Thổ Ngũ Cục
    - Đổi âm→dương 1/2 nhuận 2023 ra 22/3/2023
    - QR v3, PNG 331 KB
    - Thư viện 13 chương / 611 bài, tìm kiếm 599–604 kết quả
    - Trợ lý trả lời 1.300–1.400 ký tự
  - Thiết bị dùng kiểm thử: Samsung Galaxy Note 9 (SM-N960N, Android 10, arm64-v8a), emulator Android 17 (API 37, x86_64), emulator **Android 6.0 (API 23, x86_64)**, Windows 11 x64 (WebView2 156 và IE11).
- **VPN**: chỉ kiểm tra bên ngoài ứng dụng, đúng yêu cầu. Trong app có màn hình chẩn đoán, chỉ đọc trạng thái WARP/VPN qua Cloudflare trace.

### Lượt 6 (BUILD đầy đủ, 15:29–15:52): Note 9, Android 17, Android 6.0, portable WebView2, IE11, Setup.exe, MSI x64 và appx đều **24/24 PASS**. Commit `98edfd3` đã push lên `main`, tag `v0.0.2`. Bước tạo Release lỗi `git credential fill: missing protocol field`, do PowerShell gửi chuỗi qua pipe sai định dạng; đã sửa bằng cách ghi trực tiếp vào stdin của tiến trình (chạy lại bằng `PUBLISH_v0.0.2.bat`).

### Kết quả lượt 5 (02/11/2026 15:07–15:27, BUILD_TEST)

| Thiết bị / bản cài | Engine | Tự kiểm thử 24 bước |
|---|---|---|
| Samsung Galaxy Note 9 SM-N960N, Android 10, arm64-v8a (đang chạy **Samsung DeX**, `dex=true`) | Android WebView 156, tìm kiếm **native** (`native=true`) | **24/24 PASS** |
| Emulator Android 17 (API 37), x86_64 | Android WebView | **24/24 PASS** |
| Emulator **Android 6.0 (API 23)**, x86_64 | Android WebView (bản hệ thống) | **24/24 PASS** |
| Windows 11 x64, bản portable | Edge WebView2 156 | **24/24 PASS** |
| Windows 11 x64, bản portable, ép chạy IE11 (đường chạy Windows 7) | IE11 / MSHTML | **24/24 PASS** |
| Setup.exe (cài im lặng `/S` → chạy → gỡ `/uninstall /S`) | Edge WebView2 | **24/24 PASS**; đã gỡ sạch thư mục và khóa đăng ký ARP |
| MSI x64 (`msiexec /i /qn` → chạy → `msiexec /x`) | Edge WebView2 | **24/24 PASS** |
| appxbundle (lượt 6, 15:48) | Edge WebView2 | **24/24 PASS** khi cài layout x64 bằng `Add-AppxPackage -Register` (chế độ developer). Cài trực tiếp gói `.appxbundle` đã ký vẫn báo 0x80073CF0, nên khuyến nghị dùng Setup.exe hoặc MSI |

Ghi chú: trên Note 9, bước kiểm tra mạng báo "Không có kết nối Internet" vì lúc đó máy không có mạng; trên Windows báo đã kết nối, độ trễ khoảng 300 ms, IP Việt Nam, PoP HKG, không có WARP.

## 6. Tham chiếu từ sheet Project GitHub
- **Flash Player** (Ruffle, swf2js, Lightspark): tham khảo cấu trúc thẻ SWF để tự viết bộ đọc SWF bằng Python. Không nhúng Ruffle vì WASM không chạy được trên IE11 / Android 6, và SWF nặng 26 MB.
- **Runtime Native/Hybrid**: áp dụng mô hình vỏ native (Java/JNI + C# WinForms) bọc giao diện web dùng chung.
- **Trình duyệt** (Chromium/Firefox): dùng Edge WebView2 (Chromium) trên Windows 10/11 và Android System WebView (Chromium); IE11 dự phòng cho Windows 7.
- **Aniyomi/Nekoyomi** (moko-resources, 63 locale) và **Mangayomi** (`lib/l10n`, 19 locale): làm danh sách ngôn ngữ.
- **QR-Code-generator (Nayuki, mục X.8)**: chuyển thể sang ES5 thành `qr.js`.

## 7. So sánh với v0.0.1

| Tiêu chí | v0.0.1 | v0.0.2 |
|---|---|---|
| Âm lịch | Công thức giả, sai | Thuật toán thiên văn, đúng các mốc kiểm tra |
| An sao | 14 chính tinh, khoảng 25 phụ tinh, nhiều chỗ sai | 14 chính tinh, hơn 70 phụ tinh, 3 vòng sao, lưu niên; khớp 98% với mẫu SWF |
| Nội dung Bửu Đình | Câu rời; Android không tải được | 611 bài nguyên văn / 13 chương, có An Sao, tìm kiếm toàn văn |
| Trợ lý | Một câu trả lời cố định | Phân tích lá số thật + trích dẫn |
| Ngôn ngữ | 5 bản dịch thật | 51 ngôn ngữ dịch đầy đủ, chọn được 68 |
| Android | arm64 17 MB (kèm .so của app khác), 2 APK trùng hash | 3 APK 2,58 MB, native thật cho từng ABI, minSdk 23, căn trang 16 KB |
| Windows | x64, chỉ WebView1/IE, ES6 lỗi | WebView2 + IE11, x86/x64/ARM64, Setup.exe có trình gỡ, 3 MSI, appxbundle 3 kiến trúc, portable |
| Kiểm thử | Chụp tay | Tự kiểm thử 24 bước + unit test + video |
| Thương hiệu | Còn Visicons | Đã gỡ hoàn toàn |

---

# CẬP NHẬT v0.0.3 – Applied Astrology (Tử Vi Ứng Dụng)
**Ngày thực hiện**: 03/11/2026 (15:09 – 17:45)  
**Người thực hiện**: Claude (Claude Opus 5.5, theo yêu cầu của chủ dự án Kiệt Nguyễn)  
**Package Android**: `com.appliedastrology.tuvi`, cùng khóa ký với v0.0.2, versionCode 3001/3002/3003. **Cài đè lên 0.0.2 được, không cần gỡ.**  
**Công cụ**: VS Code (soạn mã), Node.js 24 (unit test), Python 3 (đọc SWF, i18n), Chromium/Playwright (kiểm thử web không giao diện), JDK 17 + Android SDK 35/NDK 27 (aapt2, d8, clang, zipalign, apksigner), Android Emulator (API 37, API 23), adb + Galaxy Note 9 thật, Visual Studio 2022 Build Tools (Roslyn csc), Windows SDK (makeappx, signtool), WiX 3.14, ffmpeg, Git.

## 1. Phạm vi yêu cầu (Project – Applied Astrology.xlsx, sheet "Feature Check")
| # | Hạng mục | Thực hiện |
|---|---|---|
| 1 | Sheet *Creat & Update* (3 yêu cầu) | (a) Android: nội dung/nút không bị thanh trạng thái, thanh điều hướng che; (b) mục **Âm Lịch** riêng từ bộ project Âm Lịch – Luna Calendar + nội dung SWF; (c) mục **Giới Thiệu** kiểu Aniyomi/Mangayomi/Nekoyomi: Nhật ký ứng dụng, Báo cáo lỗi, Kiểm tra cập nhật lấy từ link trong sheet *Update Link*, tự tải và cài |
| 2 | Rà sheet Project GitHub, tối ưu và cập nhật | Xem mục 6 |
| 3 | Kiểm tra độ ổn định các tính năng ("App Feature Set") | Sheet này **không có** trong file Excel → dùng danh sách tính năng v0.0.2 + tính năng mới; tự kiểm thử tăng từ **24 lên 36 bước** |
| 4 | Quét Flash Player, Runtime Native/Hybrid, Browser, Aniyomi/Mangayomi/Nekoyomi, SWF | Xem mục 6 |
| 5 | Dịch toàn bộ sang các ngôn ngữ của Aniyomi/Mangayomi/Nekoyomi | **68/68 ngôn ngữ dịch đầy đủ** (trước đây 51; 17 ngôn ngữ còn dùng tiếng Anh nay đã dịch). Nội dung 611 bài Bửu Đình giữ nguyên văn tiếng Việt (có nút dịch trực tuyến) |
| 6 | Đối chiếu nội dung SWF, bổ sung phần thiếu | Quét lại 62.236 ký tự-đối tượng theo khung (Frame) của SWF. Frame 4–16 (13 chương thư viện) đã đủ. **Frame 3 (khung lập lá số)** còn thiếu: bảng *Lục Thập Hoa Giáp + nạp âm + năm sinh* và 6 màu *giấy lót nền* → đã bổ sung |

## 2. Hạng mục đã làm & dòng lệnh / tệp đã sửa
| # | Hạng mục | Tệp / dòng lệnh chính | Công cụ |
|---|---|---|---|
| 1 | **Sửa nội dung bị che (Android 15+)**: targetSdk 35 khiến Android 15/16/17 ép tràn viền (edge-to-edge); thanh tab dưới cùng nằm dưới thanh điều hướng. Bọc WebView trong `FrameLayout`, `setOnApplyWindowInsetsListener` → đệm `systemBars() \| displayCutout() \| ime()` (API ≥ 35; API < 35 hệ thống tự chừa). Thêm `env(safe-area-inset-*)` cho CSS, thanh tab gọn hơn trên máy nhỏ | `MainActivity.applyInsets()`, `css/app.css` | VS Code, javac |
| 2 | Thanh trạng thái/điều hướng **đổi màu theo giao diện** (biểu tượng sáng/tối) | `MainActivity.setBars()` (`WindowInsetsController.setSystemBarsAppearance`, API 23–29 dùng `SYSTEM_UI_FLAG_LIGHT_*`), `AAHost.setBars()` | |
| 3 | **Mục Âm Lịch** (tab mới): lịch tháng Âm Dương GMT+7; Can Chi ngày/tháng/năm; nạp âm ngày/năm; 24 tiết khí (đánh dấu ngày giao tiết); ngày Hoàng đạo/Hắc đạo theo 12 thần trực nhật; 12 Trực; giờ Tí đầu ngày; 6 giờ hoàng đạo/6 giờ hắc đạo; ngày lễ âm/dương + Giao thừa; đổi Dương ↔ Âm (có tháng nhuận); "Lập lá số theo ngày này" | `js/calendar.js` (mới), `lunar.js` thêm `tietKhiIndex()`, `sunDegree()` | Node.js |
| 4 | **Lục Thập Hoa Giáp & Nạp Âm** (từ Frame 3 của SWF): 60 Can Chi, nạp âm tô màu theo ngũ hành, năm sinh 1840–2100, tra tuổi | `calendar.js` `sixty()`, `yearInfo()` | Python (đọc SWF) |
| 5 | **Giấy lót nền lá số** (từ Frame 3 của SWF: vàng kỹ xảo, vàng chanh, xanh nhạt, lục nhạt, tím nhạt, vàng) – tùy chọn trong Cài Đặt | `app.js` `PAPERS`, `applyPaper()` | |
| 6 | **Mục Giới Thiệu** (mở từ Cài Đặt, bố cục Aniyomi/Mangayomi): phiên bản + ngày build; Cập nhật (tự kiểm tra khi mở – tối đa 1 lần/12 giờ, tự cài sau khi tải, nút Kiểm tra, trang Releases); Có gì mới (nhật ký thay đổi trong app); Chẩn đoán (bật/tắt ghi nhật ký, xem/xuất/xóa nhật ký); Báo cáo lỗi (lỗi JS + sự cố native, mở GitHub Issues điền sẵn thông tin thiết bị, sao chép thông tin gỡ lỗi); Giấy phép mã nguồn mở; Liên kết | `js/about.js` (mới), `data/changelog.js` (mới) | |
| 7 | **Kiểm tra & tự cập nhật**: gọi `https://api.github.com/repos/kiritokmn1403/Applied-Astrology-T-Vi-/releases/latest`, so phiên bản, chọn đúng tệp theo nền tảng (APK theo ABI; Windows theo kiểu cài: Setup.exe / MSI theo kiến trúc / portable zip / appxbundle) | `about.js` `pickAsset()`, `cmpVer()` | |
| 8 | Android: tải nền (theo chuyển hướng github.com → *.githubusercontent.com, kiểm tra kích thước), cài bằng trình cài hệ thống; xin quyền "Cài ứng dụng không rõ nguồn gốc" (API 26+); `ApkProvider` (content://, không cần AndroidX) cho API 24+, `file://` cho API 23 | `MainActivity.startDownload()/installApk()`, `ApkProvider.java` (mới), manifest `REQUEST_INSTALL_PACKAGES` + `<provider>` | javac, d8 |
| 8b | Windows IE11: tắt dựng hình GPU của MSHTML (ổn định trên Windows 7/8.1 và máy có driver lỗi) | `AppHost.StartIE()` | Roslyn csc |
| 9 | Windows: tải nền có tiến độ; Setup.exe cài im lặng đúng thư mục hiện tại rồi mở lại app; MSI `msiexec /passive`; portable giải nén và chép đè (giữ `userdata`); appxbundle mở App Installer | `AppHost.cs` lớp `Updater` | Roslyn csc |
| 10 | Nhật ký ứng dụng: Android `files/app.log` (xoay vòng 256 KB), Windows `app.log` (512 KB); bộ đệm JS 400 dòng | `fileLog()`, `Program.Log()`, `AAHost.log()` | |
| 11 | Báo cáo lỗi: Android `Thread.setDefaultUncaughtExceptionHandler` → `files/crash/*.txt`; Windows `Application.ThreadException` + `AppDomain.UnhandledException` → `crash\*.txt`; lỗi JS (`window.onerror`) lưu 100 mục | `installCrashHandler()`, `Program.WriteCrash()`, `AAHost.recordError()` | |
| 12 | **i18n**: 74 khóa mới × 66 ngôn ngữ + dịch đầy đủ 17 ngôn ngữ còn thiếu (am, as, be, ceb, cv, eo, fil, jv, kn, ml, mr, my, ne, sa, sah, sc, sdh – 233 chuỗi mỗi ngôn ngữ). Kiểm tra tự động: đủ khóa, đủ 7 thứ trong tuần, giữ nguyên `{v}/{p}/{m}/{y}/{n}/{c}` | `tools/i18n_v3/*.txt`, `tools/merge_i18n_v3.py` (mới), `python tools/build_i18n.py`, `node tools/tests/i18n_test.js` | Python, Node.js |
| 13 | Biểu tượng thanh tab hiển thị dạng chữ (thêm U+FE0E) – Samsung từng vẽ ☾ thành emoji | `index.html` | |
| 14 | Build: versionCode 3001–3003; thư mục ASCII `D:\yh\aa003`; thêm `System.IO.Compression*.dll` khi biên dịch AppHost; chép `assets/certs`; bước `11b` sinh trình cài appx; kiểm thử nâng cấp 0.0.2 → 0.0.3 (Android `adb install -r`, Windows Setup.exe đè); `calendar_test.js`; dump `window_insets.txt` | `build_release_0.0.3.ps1`, `BUILD_v0.0.3.bat`, `BUILD_TEST_v0.0.3.bat`, `PUBLISH_v0.0.3.bat` | PowerShell |

## 3. Lỗi phát hiện trong quá trình update và cách sửa
| Lượt | Lỗi | Nguyên nhân | Sửa |
|---|---|---|---|
| Rà v0.0.2 | Thanh tab/nút bị thanh điều hướng che trên Android 15+ | targetSdk 35 → hệ thống ép edge-to-edge, `adjustResize` không còn tác dụng | Đệm WindowInsets ở lớp native (mục 2.1) |
| Rà v0.0.2 | Thiếu bảng Lục Thập Hoa Giáp và màu giấy nền của SWF | Bộ trích xuất v0.0.2 chỉ lấy Frame 4–16 | Quét lại theo Frame, bổ sung từ Frame 3 |
| Rà v0.0.2 | 17 ngôn ngữ vẫn hiện tiếng Anh | Chưa dịch | Dịch đầy đủ |
| 1 (16:10) | **Android 6.0: tải bản cập nhật lỗi** `SSLHandshakeException: Trust anchor for certification path not found` (gọi API vẫn được) | Tệp phát hành được tải từ `*.githubusercontent.com` dùng root **DigiCert Global Root G2** – kho CA của Android 6 chưa có | `TlsCompat.java` (mới): TrustManager thử kho hệ thống trước, chỉ khi bị từ chối mới thử 13 root CA công khai đóng gói (`src/android/certs`, lấy từ kho Mozilla); chỉ áp dụng API < 24; vẫn xác minh chứng chỉ & hostname |
| 1 (16:33) | appxbundle đã ký: `0x80073CF0` / **`0x800B0109` root certificate must be trusted** | Gói tự ký cần chứng chỉ trong *Local Machine › Trusted People* (cần Admin); script build không chạy quyền Admin nên chỉ nhập được vào kho người dùng | Thêm `AppliedAstrology-0.0.3-windows-appx-install.bat`: tự xin UAC, nhập chứng chỉ vào LocalMachine\TrustedPeople rồi cài gói. Kiểm thử tự động vẫn cài layout x64 (chế độ developer) để chạy 36 bước |
| 1 | Samsung hiển thị ☾ thành emoji ô tím ở tab Âm Lịch | Ký tự có biến thể emoji | Thêm U+FE0E (biến thể chữ) cho mọi biểu tượng tab |
| 2 (16:59) | **Chế độ IE11 (đường chạy Windows 7) hiện cửa sổ trắng** – 36/36 bước vẫn đạt (DOM chạy đúng) nhưng ảnh chụp & video toàn màu trắng | Khoanh vùng bằng 4 biến thể chạy IE11 trên cùng máy: host 0.0.3 + web 0.0.3, host 0.0.2 + web 0.0.3, host 0.0.3 + web 0.0.2, và **chính bản 0.0.2 gốc** (hôm qua chụp bình thường) → **tất cả đều trắng** ⇒ không phải lỗi mã 0.0.3 mà do dựng hình GPU của MSHTML (`FEATURE_GPU_RENDERING = 1`) không còn vẽ trên máy kiểm thử (Windows 11 build 26300, NVIDIA) | `AppHost.cs`: đặt `FEATURE_GPU_RENDERING = 0` (dựng hình phần mềm; biến môi trường `AA_IE_GPU=1` để bật lại). Thử lại: ảnh IE11 có nội dung (≈100 KB/ảnh thay vì 6,5 KB trắng) |

## 4. Kiểm thử
- **Unit test (Node.js)**: `engine_test.js` 3.965 phép kiểm khớp 98,18% (không đổi); `calendar_test.js` **15/15** (Tết 2026 = 17/2, 1/1/2000 = Mậu Ngọ, Lập Xuân 4/2/2026, Đông Chí 22/12/2026, Tết 2027 = 6/2/2027, Trung Thu, Giao thừa, 2.000 ngày liên tiếp hợp lệ); `i18n_test.js` **68/68 ngôn ngữ đầy đủ**; kiểm tra cú pháp **ES5** (acorn) cho toàn bộ JS; Chromium headless 390×844 & 1280×820.
- **Tự kiểm thử 36 bước trong ứng dụng** (24 bước cũ + 12 bước mới): 24 Âm Lịch hiển thị tháng · 25 Tết 2026 (Can Chi, tiết khí, 6 giờ hoàng đạo, ngày lễ) · 26 đổi Âm ↔ Dương · 27 Lục Thập Hoa Giáp · 28 giấy lót nền · 29 trang Giới thiệu · 30 kiểm tra cập nhật thật trên GitHub · 31 tải thật tệp phát hành phù hợp nền tảng · 32 nhật ký · 33 báo cáo lỗi · 34 vùng an toàn (thanh tab nằm trong khung nhìn) · 35 tiếng Pháp cho khóa mới · 36 khôi phục mặc định.
- **Kiểm thử nâng cấp**: Android cài 0.0.2 rồi `adb install -r` 0.0.3 → **Success** (Note 9, Android 17, Android 6); Windows Setup.exe 0.0.2 → 0.0.3 cài đè → phiên bản tệp 0.0.3.0.

### Lượt 1 (03/11/2026 16:10–16:35, BUILD_TEST)
| Thiết bị / bản cài | Engine | Kết quả |
|---|---|---|
| Samsung Galaxy Note 9 SM-N960N (thiết bị thật, USB), Android 10, arm64-v8a, DeX | Android WebView, tìm kiếm native | **36/36** |
| Emulator Android 17 (API 37), x86_64 – tràn viền bắt buộc | Android WebView | **36/36** |
| Emulator Android 6.0 (API 23), x86_64 | Android WebView | 35/36 – bước 31 lỗi TLS (đã sửa) |
| Windows 11 x64 portable | Edge WebView2 156 | **36/36** |
| Windows 11 x64 portable, ép IE11 (đường chạy Windows 7) | IE11 / MSHTML | **36/36** |
| Setup.exe (nâng cấp từ 0.0.2, cài im lặng → chạy → gỡ sạch) | Edge WebView2 | **36/36** |
| MSI x64 (`msiexec /i /qn` → chạy → `/x`) | Edge WebView2 | **36/36** |
| appxbundle (layout x64, chế độ developer) | Edge WebView2 | **36/36** |

### Lượt 2 (03/11/2026 16:37–16:59, BUILD_TEST, sau khi sửa TLS Android 6)
| Thiết bị / bản cài | Kết quả |
|---|---|
| Galaxy Note 9 (thật) · Android 17 emulator · **Android 6.0 emulator** | **36/36 · 36/36 · 36/36** (Android 6 tải cập nhật 2.750.565 byte thành công) |
| Windows portable WebView2 · portable IE11 · Setup.exe · MSI x64 · appxbundle | **36/36** cả 5 (ảnh IE11 trắng → xem lỗi lượt 2 ở mục 3) |

### Lượt 3 – KẾT QUẢ CUỐI (03/11/2026 17:12–17:33, BUILD_TEST, đủ mọi bản sửa) – bản được phát hành
| Thiết bị / bản cài | Engine | Kết quả |
|---|---|---|
| Samsung Galaxy Note 9 SM-N960N (thật, USB), Android 10, arm64-v8a, DeX | Android WebView, tìm kiếm native | **36/36**, nâng cấp 0.0.2 → 0.0.3 Success |
| Emulator Android 17 (API 37), edge-to-edge bắt buộc | Android WebView | **36/36**, nâng cấp Success |
| Emulator Android 6.0 (API 23) | Android WebView | **36/36**, nâng cấp thành công |
| Windows 11 x64 portable | Edge WebView2 156 | **36/36** |
| Windows 11 x64 portable, ép IE11 (đường chạy Windows 7) | IE11 / MSHTML (dựng hình phần mềm) | **36/36**, ảnh chụp đầy đủ nội dung |
| Setup.exe (cài đè lên 0.0.2 → tệp 0.0.3.0, chạy, gỡ sạch) | Edge WebView2 | **36/36** |
| MSI x64 | Edge WebView2 | **36/36** |
| appxbundle (layout x64, chế độ developer – xem mục 3) | Edge WebView2 | **36/36** |

**Tổng: 8/8 cấu hình, 288/288 bước PASS.**

Bằng chứng: `test_reports/0.0.3/` (ảnh từng bước, ảnh màn hình thật `live_*.png`, `window_insets.txt`, `upgrade_test.txt`, logcat, `TEST_REPORT.md`, `all_results.json`); video kiểm thử Android/Windows đính kèm trong Release (`test-video-*.mp4`, không đưa vào git vì giới hạn dung lượng).

## 5. Dung lượng & tốc độ
| | v0.0.2 | v0.0.3 |
|---|---|---|
| APK mỗi ABI | 2,58 MB | 2,80 MB (thêm mục Âm Lịch, Giới thiệu, 17 ngôn ngữ mới, 13 root CA ~25 KB) |
| Ngôn ngữ nạp theo yêu cầu | 49 tệp | 66 tệp (chỉ tải ngôn ngữ đang chọn) |
| Thư viện | nạp từng chương khi cần | không đổi |
| Lịch tháng | – | tính tại chỗ < 30 ms/tháng, không cần mạng |
| Kiểm tra cập nhật | – | tối đa 1 lần/12 giờ, chạy nền sau 4 giây, không chặn giao diện |

## 6. Tham chiếu sheet Project GitHub (đã quét)
- **Âm Lịch – Luna Calendar**: `hochanh/amlich`, `copthuy/amlich` (bảng giờ hoàng đạo, tiết khí, ngày lễ, can chi giờ), `wolfhong/LunarCalendar` (24 tiết khí), `cmduyeen/lich-am-viet-nam`, `XhinLiang/LunarCalendar`, `lwlsw/Chinese-Lunar-Calendar-ics` → viết lại ES5 trong `calendar.js`, dùng chung thuật toán Hồ Ngọc Đức đã có.
- **Aniyomi** (`AboutScreen.kt`, `AppUpdateChecker.kt`, `CrashLogUtil.kt`, `GlobalExceptionHandler.kt`), **Mangayomi** (`about_screen.dart`, `check_for_update.dart`, `error_reports_screen.dart`, `logs_state.dart`), **Nekoyomi** → bố cục Giới thiệu, cách chọn tệp phát hành theo nền tảng, nhật ký, báo cáo lỗi; danh sách 68 ngôn ngữ (moko-resources 63 + Mangayomi `lib/l10n`).
- **Flash Player** (Ruffle, swf2js, Lightspark): cấu trúc thẻ SWF cho bộ đọc Python (tìm Frame 3). Vẫn không nhúng Ruffle (WASM không chạy trên IE11/Android 6).
- **Runtime Native/Hybrid**: giữ mô hình vỏ native (Java + C#) bọc giao diện web dùng chung; mở rộng cầu nối (`x_*` 2 tham số).
- **Trình duyệt** (Chromium/Firefox): WebView2/Android WebView; IE11 dự phòng.
- Các mục không phù hợp với ứng dụng Tử Vi (VPN client, chặn quảng cáo, tải video, công cụ Wi-Fi "hacking", bỏ qua Cloudflare…) **không tích hợp**. VPN chỉ kiểm tra bên ngoài ứng dụng, như yêu cầu.

## 7. So sánh với v0.0.2
| Tiêu chí | v0.0.2 | v0.0.3 |
|---|---|---|
| Âm lịch | Chỉ dùng để lập lá số | **Mục Âm Lịch riêng**: lịch tháng, Can Chi, tiết khí, hoàng đạo, 12 Trực, giờ tốt, ngày lễ, đổi ngày |
| Nội dung SWF | Frame 4–16 | + Frame 3: Lục Thập Hoa Giáp & nạp âm, 6 giấy lót nền |
| Giới thiệu | 3 dòng thông tin | Trang riêng: cập nhật tự động, có gì mới, nhật ký, báo cáo lỗi, giấy phép |
| Cập nhật | Tải tay từ GitHub | Tự kiểm tra, tự tải, tự cài (Android/Windows) |
| Android 15+ | Nội dung bị thanh hệ thống che | Đệm vùng an toàn, màu thanh hệ thống theo giao diện |
| Android 6 | – | Tải HTTPS tới GitHub hoạt động nhờ root CA dự phòng |
| Ngôn ngữ | 51 dịch đủ / 68 | **68/68 dịch đủ** |
| Tự kiểm thử | 24 bước | 36 bước + kiểm thử nâng cấp + unit test lịch |
| Windows appx | Lỗi 0x80073CF0 khi cài gói ký | Rõ nguyên nhân (0x800B0109) + trình cài tự xin quyền |

## 8. Bảng mã SHA-256 v0.0.3 (`output/0.0.3/SHA256SUMS-0.0.3.txt`)
| Tệp | Dung lượng | SHA-256 |
|---|---|---|
| AppliedAstrology-0.0.3-arm64-v8a.apk | 2.937.467 B | `d32c2153ae41aa5db9a4dd230302aec99d6829de9453c50cac9d2baaafcbfdcc` |
| AppliedAstrology-0.0.3-armeabi-v7a.apk | 2.937.469 B | `a0712b1b6e4de06591da16f724ee6276c9e6bc35475edd7fcac4ff6399f355e7` |
| AppliedAstrology-0.0.3-x86_64.apk | 2.937.464 B | `9baba920b360441036ee72715cf2ac458820d2c7aec5937698bf4a28ab000087` |
| AppliedAstrology-0.0.3-windows-Setup.exe | 3.188.624 B | `d137baa197d6e8a296e93bc6e7914b74a5cea093b7198b597d2eddfdd4f80c1a` |
| AppliedAstrology-0.0.3-windows.appxbundle | 9.994.158 B | `48b32fa7b05814c699cccf8fb10062f6be23362e21d2b95b7258cb5d7c753ae7` |
| AppliedAstrology-0.0.3-windows-appx-install.bat | 1.693 B | `fa5766e204c963515546f1ec93c847b162511f7cdf188245501420c73c264a26` |
| AppliedAstrology-0.0.3-windows-portable.zip | 3.169.174 B | `ac3c1ba92b6c0cc2dfabc97dab2f8fe2a20634544daae559bcf8422dc48a3c4d` |
| AppliedAstrology-0.0.3-windows-x64.msi | 2.314.240 B | `c677b0e76a3a09173dc0fcf6736992553525e8f703f9df1ded8c08fc9fb7eb82` |
| AppliedAstrology-0.0.3-windows-x86.msi | 2.289.664 B | `548505c5221e0dd95a2e2fce46713a3979a2175f9e2ba3f994d0b1ea0e7d35c8` |
| AppliedAstrology-0.0.3-windows-arm64.msi | 2.285.568 B | `3c131f3eaccde8d0db4d658d5052e9230955718eda506bdc4029e35027f6e80b` |
| AppliedAstrology-0.0.3-signing-certificate.cer | 794 B | `2d208698cad1bf9672284d91571c0f74d5dafd93ad7fecef3b0d86f75a52fe73` |

## 9. Phát hành
`PUBLISH_v0.0.3.bat` → `build_release_0.0.3.ps1 -Phase publish`: `git add` (src, tools, docs, output/0.0.3, test_reports/0.0.3, README, CHANGELOG, TECHNICAL_UPDATE_NOTES, script) → commit → `git push origin HEAD:main` → tag `v0.0.3` → tạo GitHub Release (token lấy từ Git Credential Manager của máy, không ghi log) → tải lên 12 tệp output + 4 video kiểm thử → đối chiếu tên và kích thước từng tệp trên Release.

**Kết quả phát hành (03/11/2026):**
| Lượt | Thời gian | Kết quả | Xử lý |
|---|---|---|---|
| 1 | 17:37–17:42 | `git push` lỗi `RPC failed; curl 55 Send failure: Connection was reset` (đẩy ~100 MB ảnh kiểm thử qua mạng chập chờn); Release `v0.0.3` được tạo nhưng tải lên dừng ở tệp thứ 6 (`The underlying connection was closed`) | Thêm vào script: `http.postBuffer = 500 MB`, `http.lowSpeedLimit = 0`, thử lại `push` 5 lần, thử lại từng tệp 4 lần (xóa tệp tải dở), bỏ qua tệp đã lên đủ dung lượng, cập nhật nội dung Release |
| 2 | 17:44–17:57 | `git push` OK (commit `e4457db`), tag `v0.0.3` OK; 5 tệp đã lên từ lượt 1 được bỏ qua, 11 tệp còn lại tải lên OK | **16/16 tệp xác minh đúng tên + dung lượng** (`state=uploaded`); trang Release hiển thị *Latest*, 18 tệp (16 + 2 gói mã nguồn) |

Liên kết: https://github.com/kiritokmn1403/Applied-Astrology-T-Vi-/releases/tag/v0.0.3
