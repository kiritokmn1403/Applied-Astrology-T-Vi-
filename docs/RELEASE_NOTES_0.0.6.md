# Applied Astrology v0.0.6 – Tử Vi Ứng Dụng (sửa lag Windows, phân trang, Trang chủ & Chính sách quyền riêng tư)

Bản cập nhật tập trung **sửa lỗi lag khi cuộn / mở tab / tìm kiếm trên Windows**, thêm **phân trang kết quả tìm kiếm**, sửa lỗi Liquid Glass trên **Android 6**, và ra mắt **Trang chủ** + **Chính sách quyền riêng tư** của ứng dụng.

🏠 Trang chủ: https://kiritokmn1403.github.io/Applied-Astrology-T-Vi-/ · 🔒 Chính sách quyền riêng tư: https://kiritokmn1403.github.io/Applied-Astrology-T-Vi-/privacy.html

### Hiệu năng (đo trên cùng máy, bản cũ ↔ bản mới)
| Thiết bị / chỉ số | 0.0.5 | 0.0.6 |
|---|---|---|
| Windows – cuộn danh sách 120 thẻ (ms/khung, trung bình) | 94,6 | **39,2** |
| Windows – tìm kiếm | 2342 ms | **506 ms** |
| Windows – mở tab chậm nhất | 1955 ms | **745 ms** |
| Galaxy Note 9 – khung giật khi cuộn Cài Đặt | 12,67% | **5,69%** |
| Android 6.0 (emulator) – khung giật / p90 | 14,87% / 19 ms | **0% / 7 ms** |

### Mới
- **Kết quả tìm kiếm phân trang** 40 mục/lần, nút **"Xem thêm"** (68 ngôn ngữ), tối đa 300 kết quả.
- **Thư viện nạp sẵn ở luồng nền** khi mở app.
- **Trang chủ** và **Chính sách quyền riêng tư** trên GitHub Pages.
- Tự kiểm thử **54 bước**; bảng so sánh hiệu năng Windows 0.0.5 ↔ 0.0.6.

### Sửa lỗi
- Windows: nền khung cuộn vẽ không co giãn; chỉ vẽ lại phần đang hiện; thẻ kính dùng lại nền khi đang cuộn, vẽ lại chính xác khi dừng.
- Android 6: Liquid Glass không dựng xong khi màn hình tắt giữa chừng.

### Kết quả kiểm thử
| Nền tảng | Kết quả |
|---|---|
| Samsung Galaxy Note 9 (Android 10, USB) – nâng cấp từ 0.0.5 | 54/54 |
| Emulator Android 17 (API 37) | 54/54 |
| Emulator Android 6.0 (API 23) | 54/54 |
| Windows portable / tương thích Windows 7 | 54/54 / 54/54 |
| Windows Setup.exe (nâng cấp từ 0.0.5) / MSI x64 / appxbundle | 54/54 / 54/54 / 54/54 |
| Không nạp WebView2 / IE / trình duyệt | 1/1 |

Video kiểm thử (Android nhiều phần, Windows) được đính kèm trong Release này.

## Cài đặt
| Thiết bị | Tệp |
|---|---|
| Android 6.0+ ARM 64-bit (phần lớn điện thoại) | `AppliedAstrology-0.0.6-arm64-v8a.apk` |
| Android 6.0+ ARM 32-bit | `AppliedAstrology-0.0.6-armeabi-v7a.apk` |
| Android x86_64 / emulator / Chromebook | `AppliedAstrology-0.0.6-x86_64.apk` |
| Windows 7 SP1 → 11: bộ cài .exe (khuyên dùng) | `AppliedAstrology-0.0.6-windows-Setup.exe` |
| Windows 10/11: .appxbundle | chạy `AppliedAstrology-0.0.6-windows-appx-install.bat`, để cùng thư mục với `.appxbundle` và `.cer` |
| Windows 7 SP1 → 11: portable (không cần cài) | `AppliedAstrology-0.0.6-windows-portable.zip` (giải nén, chạy `AppliedAstrology.exe`) |
| Windows: MSI | `AppliedAstrology-0.0.6-windows-x64.msi` / `-x86.msi` / `-arm64.msi` |

Bản ARM64 dành cho Windows 10/11 trên ARM (Windows 7 không có bản ARM). Windows 7 SP1 cần .NET Framework 4.5+.

**Nâng cấp từ 0.0.5 (hoặc cũ hơn):** cài đè trực tiếp, hồ sơ/đánh dấu/cài đặt được giữ nguyên. App 0.0.5 cũng tự tìm thấy bản này trong *Cài Đặt › Giới thiệu › Cập nhật*.

Kiểm tra toàn vẹn bằng `SHA256SUMS-0.0.6.txt`. Bộ cài ký bằng chứng chỉ tự tạo nên SmartScreen có thể cảnh báo: bấm *More info › Run anyway*.

## Kiểm thử
Chi tiết, số liệu hiệu năng trước/sau và video: `test_reports/0.0.6/TEST_REPORT.md`, `TECHNICAL_UPDATE_NOTES.md`, các tệp `test-video-*.mp4` trong Release này.
