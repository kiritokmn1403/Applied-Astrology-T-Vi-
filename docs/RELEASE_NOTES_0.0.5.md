# Applied Astrology v0.0.5 – Tử Vi Ứng Dụng (bản sửa hiệu năng)

Bản cập nhật tập trung **sửa lỗi ứng dụng lag, hình ảnh giật và treo**, thêm **công tắc Bật/Tắt Liquid Glass**, sửa lỗi Âm lịch "mùng 0" và giảm dung lượng gói cài.

### Sửa lỗi lag, giật, treo
- **Liquid Glass viết lại**:
  - Khúc xạ chỉ tính ở dải viền vát (~5–10% số điểm ảnh); lõi tấm kính do GPU vẽ.
  - Mọi phép tính ảnh chạy ở luồng nền: mở hộp thoại, thông báo, đổi cài đặt không còn đứng hình.
  - Cuộn: chỉ vẽ lại thẻ đang hiển thị và thật sự đổi vị trí, tối đa 1 lần mỗi khung hình; bỏ bóng đổ từng thẻ.
  - Windows: nền và kính dựng sẵn đúng kích thước, vẽ không co giãn, có bộ đệm cho từng khung; mở hộp thoại không còn chụp lại toàn cửa sổ.
- Bộ nhớ dành cho kính giảm khoảng 10 lần.

### Mới
- **Công tắc Bật/Tắt Liquid Glass** trong *Cài Đặt › Liquid Glass*. Tắt = giao diện phẳng, nhẹ nhất. Máy cấu hình thấp lần đầu tự dùng chế độ Kính mờ. Đủ 68 ngôn ngữ.
- Tự kiểm thử **52 bước**, có đo hiệu năng (cuộn, hộp thoại, đổi tab, đổi cài đặt, bộ nhớ) và kiểm tra toàn dải Âm lịch 1900–2099.

### Sửa lỗi khác
- Âm lịch: một số ngày bị đổi thành "mùng 0" (vd. 7/5/2054, 9/4/2062) – đã sửa; 73.018 ngày 1900–2099 đều hợp lệ.
- Nền tinh vân hết vệt vòng cung; kính lỏng sắc nét hơn.

### Dung lượng
- APK, bản portable và Setup.exe được nén lại bằng zopfli (vẫn là định dạng chuẩn).

## Cài đặt
| Thiết bị | Tệp |
|---|---|
| Android 6.0+ ARM 64-bit (phần lớn điện thoại) | `AppliedAstrology-0.0.5-arm64-v8a.apk` |
| Android 6.0+ ARM 32-bit | `AppliedAstrology-0.0.5-armeabi-v7a.apk` |
| Android x86_64 / emulator / Chromebook | `AppliedAstrology-0.0.5-x86_64.apk` |
| Windows 7 SP1 → 11: bộ cài .exe (khuyên dùng) | `AppliedAstrology-0.0.5-windows-Setup.exe` |
| Windows 10/11: .appxbundle | chạy `AppliedAstrology-0.0.5-windows-appx-install.bat`, để cùng thư mục với `.appxbundle` và `.cer` |
| Windows 7 SP1 → 11: portable (không cần cài) | `AppliedAstrology-0.0.5-windows-portable.zip` (giải nén, chạy `AppliedAstrology.exe`) |
| Windows: MSI | `AppliedAstrology-0.0.5-windows-x64.msi` / `-x86.msi` / `-arm64.msi` |

Bản ARM64 dành cho Windows 10/11 trên ARM (Windows 7 không có bản ARM). Windows 7 SP1 cần .NET Framework 4.5+.

**Nâng cấp từ 0.0.4 (hoặc cũ hơn):** cài đè trực tiếp, hồ sơ/đánh dấu/cài đặt được giữ nguyên. App 0.0.4 cũng tự tìm thấy bản này trong *Cài Đặt › Giới thiệu › Cập nhật*.

Kiểm tra toàn vẹn bằng `SHA256SUMS-0.0.5.txt`. Bộ cài ký bằng chứng chỉ tự tạo nên SmartScreen có thể cảnh báo: bấm *More info › Run anyway*.

## Kiểm thử
Chi tiết, số liệu hiệu năng trước/sau và video: `test_reports/0.0.5/TEST_REPORT.md`, `TECHNICAL_UPDATE_NOTES.md`, các tệp `test-video-*.mp4` trong Release này.
