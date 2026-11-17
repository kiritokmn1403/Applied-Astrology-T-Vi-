# Build / Test / Release

- **Thư mục dự án:** `D:\A.I Project\A.I Project - Applied Astrology (Tử Vi Ứng Dụng)\Applied Astrology (Tử Vi)\Applied Astrology (Tử Vi)`
- **Thư mục build ASCII:** `D:\yh\aa00X` (0.0.5: `D:\yh\aa005`).
  - `D:\yh` là junction tới `D:\A.I Project\yh`.
  - adb, apksigner và NDK lỗi với đường dẫn có dấu tiếng Việt, nên mọi bước build chạy trong `D:\yh`.
- **Công cụ** (`D:\yh\tools`): android-sdk (platform 34–37, build-tools 36, NDK 28), jdk, ffmpeg, wix314, nuget. Roslyn csc lấy từ VS 2022 BuildTools. Node.js (bắt buộc: sinh dữ liệu + zopfli). Emulator nằm ở `%LOCALAPPDATA%\Android\Sdk`.
- **AVD có sẵn:** `Medium_Phone_API_37.0`, `OOS_Android6_API23`, `YH_Android10_API29`, `YomiHub056_Phone_API34`, `YomiHub_TV_API34`. Script chọn API 37 + API 23.
- **Khóa ký** (`D:\yh\aa002_keys`, **không bao giờ commit**):
  - Android: `appliedastrology-release.jks` + `keystore-password.txt`.
  - Windows: `AppliedAstrology-codesign.pfx`, mật khẩu là `aa002-` + COMPUTERNAME.
- **Script:** `build_release_0.0.X.ps1 -Phase all|build|test|publish [-NoPublish] [-AndroidOnly] [-WindowsOnly] [-SkipEmulator]`
  - Các file .bat: `BUILD_ONLY`, `BUILD_TEST`, `BUILD`, `PUBLISH` (mở bằng double-click trong File Explorer; cửa sổ có `pause` ở cuối).
  - Theo dõi tiến độ trong `build_logs/status.json` và `build_logs/build_*.log`.
- **Các bước:**
  1. `00` dò công cụ (~1 phút vì tìm ffmpeg đệ quy)
  2. `01` stage + `build_native_data` + unit test JS (engine 98,18%, calendar 18/18, i18n)
  3. `01b` đối chiếu native (JS↔Java, C#↔Java, **kính Java↔C#**)
  4. `02–03` APK (javac → jar → d8 → clang → aapt2 → thêm dex/so/assets → **zopfli** → zipalign → apksigner)
  5. `05–12` Windows (csc → layout → portable (**zopfli**) → Setup.exe (payload **zopfli**) → cert → appxbundle → MSI → checksums)
  6. `20` Android: USB + emulator API 37 và 23. Mỗi máy: cài 0.0.(N-1) → **đo gfxinfo** → cài đè bản mới → selftest (video nhiều phần, 180 s/phần) → **đo gfxinfo** lại
  7. `21` Windows (portable, `--compat win7`, kiểm tra DLL nạp vào, Setup nâng cấp, MSI, appx)
  8. `22` báo cáo (`test_reports/<ver>/TEST_REPORT.md` có bảng hiệu năng, `perf_compare_android.json`)
  9. `30` git push (fetch + rebase nếu GitHub có commit mới; không force-push)
  10. `31` GitHub Release (token lấy từ Git Credential Manager, không in ra; so digest sha256 rồi mới bỏ qua; xác minh)
- **Khi đổi phiên bản:** sao chép script cũ rồi đổi `$Ver`/`$PrevVer`/`$Work`, versionCode (`$Abis`), AppxManifest `Version`, `app.manifest`, `Setup.cs`, `WinHost.cs` (`Version`, `BuildDate`, AssemblyVersion), `Host.VERSION`/`BUILD_DATE`, AndroidManifest `versionName`, `tools/changelog_vN.json`, thông điệp commit ở bước 30.
- **Release:** installer để trong `output/<ver>/` và đính kèm vào GitHub Release. Video test (*.mp4) chỉ đính kèm Release, không đưa vào git (giới hạn 100 MB).
- **README** phải có bảng link `releases/download/v<ver>/...`.
