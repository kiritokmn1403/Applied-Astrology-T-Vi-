# Changelog

## 0.0.6 – 18/11/2026
### Sửa lỗi lag Windows (Creat & Update mục 4) – đo trên cùng máy, bản 0.0.5 ↔ 0.0.6
- **Cuộn**: 94,6 ms/khung → **39,2 ms/khung** (bản portable, 120 thẻ kết quả, 60 khung).
  - Khung cuộn vẽ nền tinh vân không co giãn (0.0.5 vẽ `DrawImage` có nội suy + vẽ nền form 2 lần mỗi khung).
  - Chỉ vẽ lại điều khiển đang nằm trong khung nhìn (0.0.5 vẽ lại cả cây, kể cả hàng trăm điều khiển ngoài màn hình).
  - Đang cuộn: thẻ kính dùng lại nền đã dựng; dừng cuộn 220 ms mới vẽ lại chính xác (~100 ms, 1 lần).
  - Khung không kính và chữ trong suốt vẽ thẳng ảnh nền thay cho cơ chế nền trong suốt nhiều tầng của WinForms.
  - Form và bố cục dùng chung một kích thước ảnh nền (không co giãn lại liên tục).
- **Tìm kiếm**: 2342 ms → 506 ms – thư viện 611 bài được nạp và bỏ dấu sẵn ở luồng nền khi mở app.
- **Mở tab**: tab chậm nhất 1955 ms → 745 ms – kết quả tìm kiếm phân trang.
- Android: Galaxy Note 9 khung giật 12,67% → 5,69%; Android 6.0 14,87% → 0%.
- Kiểm thử: 54/54 trên Note 9, Android 17, Android 6.0, Windows portable/Win7/Setup.exe/MSI/appxbundle.
### Mới
- **Kết quả tìm kiếm phân trang** 40 mục/lần + nút **"Xem thêm (n)"** (Android & Windows), tối đa 300 kết quả; chuỗi mới `show_more` × 68 ngôn ngữ.
- **Trang chủ** (`docs/index.html`) và **Chính sách quyền riêng tư** (`docs/privacy.html`) song ngữ trên GitHub Pages, gắn vào mục About của repo.
- Tự kiểm thử **54 bước** (thêm `53_results_paging`, `54_library_warm`); báo cáo có bảng hiệu năng Windows 0.0.5 ↔ 0.0.6 (chạy selftest bản 0.0.5 trên cùng máy).
- Bắt đầu dịch thư viện 611 bài sang 67 ngôn ngữ (`translations/`, bảng thuật ngữ thống nhất) – sẽ tích hợp vào app theo kiểu tải khi cần ở bản sau.
### Sửa lỗi khác
- **Android 6**: Liquid Glass không dựng xong / hộp thoại kích thước 0 khi màn hình tắt giữa chừng – app giữ màn hình sáng khi tự kiểm thử; script giữ emulator luôn sáng (`stayon true`).
- Emulator kiểm thử dùng GPU thật (`-EmuGpu auto`); trước đây GPU phần mềm làm mọi bản đều "giật" 98–100% trên emulator.
- Script phát hành không còn lưu email; danh tính git lấy từ cấu hình máy.

## 0.0.5 – 17/11/2026
### Sửa lỗi lag, giật, treo (Creat & Update mục 3)
- **Liquid Glass viết lại** (Android `GlassViews`, lõi `Glass` Java/C#, Windows `WinUi`):
  - Khúc xạ chỉ tính ở **dải viền vát** (bản đồ khúc xạ thưa, ~5–10% số điểm ảnh); phần lõi tấm kính do **GPU** vẽ (BitmapShader + ColorMatrix).
  - Mọi phép tính ảnh (tinh vân, làm mờ, khúc xạ) chạy ở **luồng nền**; mở hộp thoại, thông báo, đổi cài đặt không còn đứng hình.
  - Cuộn: chỉ vẽ lại thẻ đang hiển thị **và đổi vị trí**, tối đa 1 lần mỗi khung hình; bỏ bóng đổ từng thẻ; không tạo đối tượng vẽ mới mỗi khung.
  - Bộ nhớ bản đồ khúc xạ giảm ~10 lần (hộp thoại 1342×2385: ~51 MB → ~3,5 MB/mục, cache 24 → 12 mục).
  - Windows: nền và ảnh mờ dựng sẵn đúng kích thước (PArgb), vẽ không co giãn; bộ đệm nền cho từng khung kính (LRU 48); gộp yêu cầu vẽ khi cuộn; mở hộp thoại chụp nền bằng BitBlt thay cho DrawToBitmap; đổi cỡ cửa sổ không dựng lại tinh vân.
- Kết quả đo (gfxinfo, cùng máy, cùng thao tác): Galaxy Note 9 – khung giật 9,81% → 6,57%, p99 25 → 19 ms; mở hộp thoại ~1 ms trên luồng UI.
### Mới
- **Công tắc Bật/Tắt Liquid Glass** trong *Cài Đặt › Liquid Glass* (Creat & Update mục 2). Tắt = giao diện phẳng nhẹ nhất. Máy cấu hình thấp (RAM ít, Android 6–7) lần đầu tự dùng Kính mờ. 4 chuỗi mới × 68 ngôn ngữ.
- Tự kiểm thử **52 bước** (thêm: công tắc Liquid Glass, đo cuộn, mở hộp thoại, đổi tab, đổi cài đặt, độ ổn định kính, bộ nhớ, toàn dải Âm Lịch 1900–2099).
- Build: đối chiếu engine kính Java↔C# (trùng từng byte); đo gfxinfo bản cũ ↔ bản mới trên mỗi thiết bị Android; video kiểm thử Android ghi đủ (nhiều phần).
### Sửa lỗi khác
- **Âm lịch "mùng 0"**: một số ngày (7/5/2054, 9/4/2062) bị đổi thành mùng 0 do ước lượng chỉ số tuần trăng lệch. Đã sửa ở Java, C# và engine JS chuẩn; kiểm tra 73.018 ngày 1900–2099: 0 lỗi, đổi xuôi/ngược khớp hoàn toàn.
- Nền tinh vân hết vệt vòng cung (mép các đám tinh vân mờ dần liên tục).
- Kính lỏng sắc nét hơn: lấy mẫu song tuyến tính thay cho điểm gần nhất.
- Selftest khởi động được cả khi hệ thống tự mở lại app sau khi cài đè (Android 17).
### Dung lượng
- APK, bản portable và Setup.exe nén lại bằng **zopfli** (deflate chuẩn, tương thích Android 6 / Windows 7).

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
