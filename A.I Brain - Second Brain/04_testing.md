# Kiểm thử

- **Selftest (44 bước ở 0.0.4)** chạy ngay trong app:
  - Android: `am start -n com.appliedastrology.tuvi/.MainActivity --ez selftest true`. Kết quả ở `/sdcard/Android/data/com.appliedastrology.tuvi/files/selftest/results.json` kèm ảnh PNG cho từng bước.
  - Windows: `AppliedAstrology.exe --selftest --out <dir> [--compat win7]`. Bản appx dùng tệp cờ `%APPDATA%\AppliedAstrology\selftest.flag` (nội dung là thư mục kết quả).
- Các bước 30–31 (kiểm tra / tải cập nhật) cần mạng tới api.github.com. Nếu môi trường chặn mạng thì 2 bước này FAIL; đó không phải lỗi app.
- **Thiết bị đã dùng:**
  - Samsung Galaxy Note 9 SM-N960N (Android 10, USB)
  - AVD `Medium_Phone_API_37.0`, `OOS_Android6_API23`
  - Windows 11 x64
- **Kiểm thử nâng cấp:** cài APK hoặc Setup của bản trước, rồi cài bản mới đè lên; kiểm tra versionName / FileVersion.
- **Đối chiếu engine:** bước `01b`. Java vs JS: 3000 lá số / 4000 ngày / 1608 lần Âm→Dương. C# phải trùng từng byte với Java.
- Video: Android dùng `screenrecord`, Windows dùng ffmpeg gdigrab. Video được đính kèm vào Release.
