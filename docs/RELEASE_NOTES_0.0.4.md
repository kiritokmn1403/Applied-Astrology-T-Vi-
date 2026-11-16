# Applied Astrology v0.0.4 – Tử Vi Ứng Dụng (bản native)

Cập nhật lớn: **viết lại toàn bộ thành ứng dụng native**, không còn WebView, WebView2 hay IE. Thêm giao diện **Liquid Glass** có trang cài đặt riêng.

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

## Cài đặt
| Thiết bị | Tệp |
|---|---|
| Android 6.0+ ARM 64-bit (phần lớn điện thoại) | `AppliedAstrology-0.0.4-arm64-v8a.apk` |
| Android 6.0+ ARM 32-bit | `AppliedAstrology-0.0.4-armeabi-v7a.apk` |
| Android x86_64 / emulator / Chromebook | `AppliedAstrology-0.0.4-x86_64.apk` |
| Windows 7 SP1 → 11: bộ cài .exe (khuyên dùng) | `AppliedAstrology-0.0.4-windows-Setup.exe` |
| Windows 10/11: .appxbundle | chạy `AppliedAstrology-0.0.4-windows-appx-install.bat`, để cùng thư mục với `.appxbundle` và `.cer` |
| Windows 7 SP1 → 11: portable | `AppliedAstrology-0.0.4-windows-portable.zip` (giải nén, chạy `AppliedAstrology.exe`) |
| Windows: MSI | `AppliedAstrology-0.0.4-windows-x64.msi` / `-x86.msi` / `-arm64.msi` |

**Nâng cấp từ 0.0.3:**
- Cài đè trực tiếp, không cần gỡ bản cũ.
- Hồ sơ, đánh dấu và cài đặt được giữ nguyên.
- Ứng dụng 0.0.3 cũng tự tìm thấy bản này trong *Cài Đặt › Giới thiệu › Cập nhật*.

Kiểm tra toàn vẹn bằng `SHA256SUMS-0.0.4.txt`.

**Về cảnh báo SmartScreen:** bộ cài được ký bằng chứng chỉ tự tạo, không phải chứng chỉ thương mại. Vì vậy Windows có thể vẫn hiện "Windows protected your PC". Bấm *More info › Run anyway* để tiếp tục. Cảnh báo này không liên quan tới việc app là native hay trình duyệt.

## Kiểm thử
Tự kiểm thử 44 bước đã chạy trên:
- Samsung Galaxy Note 9 (máy thật, Android 10)
- emulator Android 17 và Android 6.0
- Windows 11: portable, chế độ `--compat win7`, Setup.exe (nâng cấp từ 0.0.3), MSI, appxbundle

Có kiểm tra rằng app không nạp DLL trình duyệt nào. Video kiểm thử là các tệp `test-video-*.mp4` trong Release này. Chi tiết xem `TECHNICAL_UPDATE_NOTES.md` và `test_reports/0.0.4/TEST_REPORT.md`.
