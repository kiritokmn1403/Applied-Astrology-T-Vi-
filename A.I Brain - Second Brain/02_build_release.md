# Build / Test / Release

- **Thư mục dự án:** `D:\A.I Project\A.I Project - Applied Astrology (Tử Vi Ứng Dụng)\Applied Astrology (Tử Vi)\Applied Astrology (Tử Vi)`
- **Thư mục build ASCII:** `D:\yh\aa00X`.
  - `D:\yh` là junction tới `D:\A.I Project\yh`.
  - adb, apksigner và NDK lỗi với đường dẫn có dấu tiếng Việt, nên mọi bước build chạy trong `D:\yh`.
- **Công cụ** (`D:\yh\tools`): android-sdk (platform 34–37, build-tools, NDK), jdk, ffmpeg, wix314, nuget. Roslyn csc lấy từ VS 2022 BuildTools.
- **Khóa ký** (`D:\yh\aa002_keys`, **không bao giờ commit**):
  - Android: `appliedastrology-release.jks` + `keystore-password.txt`.
  - Windows: `AppliedAstrology-codesign.pfx`, mật khẩu là `aa002-` + COMPUTERNAME.
- **Script:** `build_release_0.0.X.ps1 -Phase all|build|test|publish [-NoPublish] [-AndroidOnly] [-WindowsOnly] [-SkipEmulator]`
  - Các file .bat: `BUILD_ONLY`, `BUILD_TEST`, `BUILD`, `PUBLISH`.
  - Theo dõi tiến độ trong `build_logs/status.json` và `build_logs/build_*.log`.
- **Các bước:**
  1. `00` dò công cụ
  2. `01` stage + `build_native_data` + unit test JS
  3. `01b` đối chiếu native
  4. `02–03` APK
  5. `05–12` Windows (csc → layout → portable → Setup.exe → cert → appxbundle → MSI → checksums)
  6. `20` Android (USB + emulator API 37 và 23)
  7. `21` Windows (portable, `--compat win7`, kiểm tra DLL nạp vào, Setup nâng cấp, MSI, appx)
  8. `22` báo cáo
  9. `30` git push
  10. `31` GitHub Release (token lấy từ Git Credential Manager, không in ra; so digest sha256 rồi mới bỏ qua; xác minh)
- **Khi đổi phiên bản:** sao chép script cũ rồi đổi `$Ver`/`$PrevVer`/`$Work`, versionCode, AppxManifest `Version`, `app.manifest`, `Setup.cs`, `Host.VERSION`/`BUILD_DATE`, AndroidManifest `versionName`, `tools/changelog_vN.json`.
- **Release:** installer để trong `output/<ver>/` và đính kèm vào GitHub Release. Video test (*.mp4) chỉ đính kèm Release, không đưa vào git (giới hạn 100 MB).
- **README** phải có bảng link `releases/download/v<ver>/...`.
