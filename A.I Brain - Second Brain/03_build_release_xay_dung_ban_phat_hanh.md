# 03 – Xây dựng bản phát hành

## 1. Đường dẫn
- **Thư mục dự án:** `D:\A.I Project\A.I Project - Applied Astrology (Tử Vi Ứng Dụng)\Applied Astrology (Tử Vi)\Applied Astrology (Tử Vi)`
- **Thư mục build ASCII:** `D:\yh\aa00N` (0.0.6: `D:\yh\aa006`).
  - `D:\yh` là junction tới `D:\A.I Project\yh`.
  - adb, apksigner và NDK lỗi với đường dẫn có dấu tiếng Việt, nên mọi bước build chạy trong `D:\yh`.
  - `D:\yh\aa006\stage\` bị xóa và tạo lại ở mỗi lần build (bước 01).
- **Khóa ký** (`D:\yh\aa002_keys`, **không bao giờ commit hay in ra**):
  - Android: `appliedastrology-release.jks` + `keystore-password.txt`.
  - Windows: `AppliedAstrology-codesign.pfx` (mật khẩu sinh từ COMPUTERNAME, script tự xử lý).

## 2. Script & tệp chạy
- `build_release_0.0.N.ps1 -Phase all|build|test|publish [-NoPublish] [-AndroidOnly] [-WindowsOnly] [-SkipEmulator] [-EmuGpu auto|host|swiftshader_indirect]`
- 4 tệp `.bat`, mở bằng double-click trong File Explorer (cửa sổ cmd có `pause` ở cuối):

| Tệp | Làm gì | Thời gian |
|---|---|---|
| `BUILD_ONLY_v<ver>.bat` | `-Phase build` | ~9 phút |
| `BUILD_TEST_v<ver>.bat` | `-Phase all -NoPublish` | ~40–60 phút |
| `BUILD_v<ver>.bat` | `-Phase all` (build + test + phát hành) | ~60–70 phút |
| `PUBLISH_v<ver>.bat` | `-Phase publish` (01/01b + 30/31/32) | ~3–6 phút |

- Theo dõi tiến độ: `build_logs/status.json` (bước hiện tại, `finished`, `failed`) và `build_logs/build_<ver>_<ngày>_<giờ>.log`.

## 3. Các bước của script
| Bước | Nội dung |
|---|---|
| 00 | Dò công cụ (~1 phút) – ghi đường dẫn từng công cụ vào log |
| 01 | Stage mã vào `D:\yh\aa00N\stage` + `build_native_data.js` + unit test JS (engine 98,18%, calendar 18/18, i18n). Các dòng `FAIL Hồng Loan…` là chênh lệch đã biết với SWF, **không phải lỗi mới** |
| 01b | Đối chiếu native: JS↔Java, C#↔Java (trùng byte), kính Java↔C# (trùng byte) |
| 02–03 | Android: keystore → javac → jar → d8 → clang (3 ABI) → aapt2 → dex/so/assets → **zopfli** → zipalign → apksigner |
| 05–12 | Windows: csc → layout → portable.zip (zopfli) → Setup.exe (payload zopfli, ký) → chứng chỉ → appxbundle → MSI x64/x86/arm64 → `appx-install.bat` → SHA256SUMS |
| 20 | Android: USB + emulator API 37 và 23. Mỗi máy: cài bản trước → đo gfxinfo → cài đè bản mới → selftest (video nhiều phần) → đo gfxinfo |
| 21 | Windows: portable, `--compat win7`, **portable bản trước (đối chứng hiệu năng)**, module nạp vào, Setup.exe nâng cấp, MSI, appx |
| 22 | Báo cáo `test_reports/<ver>/TEST_REPORT.md` (bảng PASS, gfxinfo, bảng hiệu năng Windows cũ↔mới) |
| 30 | git add/commit/push (fetch + rebase nếu GitHub có commit mới, **không force-push**). Danh tính git lấy từ cấu hình máy; thiếu thì dùng `<owner>@users.noreply.github.com` |
| 31 | GitHub Release: token lấy từ Git Credential Manager (không in ra). Tải lên tệp cài đặt + video; bỏ qua chỉ khi **digest SHA-256 trùng**; xác minh từng tệp |
| 32 | GitHub Pages (main `/docs`) + Website/mô tả/topics của repo; chờ build Pages; kiểm tra Trang chủ và Chính sách trả HTTP 200 |

## 4. Đổi phiên bản (checklist)
1. Sao chép script cũ bằng script biến đổi (có `assert`). Đổi `$Ver`, `$PrevVer`, `$Work`, versionCode trong `$Abis`, thông điệp commit (bước 30), chú thích đầu tệp.
2. Tạo 4 tệp `.bat` mới (thay số phiên bản).
3. Mã nguồn:
   - `src/android/AndroidManifest.xml` `versionName`.
   - `Host.java` `VERSION`/`BUILD_DATE`.
   - `WinHost.cs` (`Version`, `BuildDate`, AssemblyVersion), `Setup.cs`.
   - `AppxManifest.template.xml` `Version`, `app.manifest` `version`.
4. `tools/changelog_v<N>.json` (vi + en) + thêm vào `build_native_data.js`.
5. `docs/index.html` mục `#download` (phiên bản, tên tệp, dung lượng, bản cũ).
6. README (bảng tải), CHANGELOG, `docs/RELEASE_NOTES_<ver>.md`.

## 5. Sản phẩm phát hành (`output/<ver>/` → GitHub Release)
- Android:
  - `AppliedAstrology-<ver>-arm64-v8a.apk`
  - `AppliedAstrology-<ver>-armeabi-v7a.apk`
  - `AppliedAstrology-<ver>-x86_64.apk`
- Windows:
  - `AppliedAstrology-<ver>-windows-Setup.exe`
  - `AppliedAstrology-<ver>-windows-portable.zip`
  - `AppliedAstrology-<ver>-windows.appxbundle` + `windows-appx-install.bat` + `signing-certificate.cer`
  - MSI `-windows-x64.msi` / `-x86.msi` / `-arm64.msi`
- `SHA256SUMS-<ver>.txt`.
- Video kiểm thử `test-video-<thư mục>-<tên tệp>.mp4`: **chỉ** đính kèm Release, không đưa vào git (`.gitignore` có `*.mp4`; giới hạn 100 MB/tệp).
- README phải có bảng link `releases/download/v<ver>/…`. Trang chủ phải có mục Tải về.
- Kiểm tra link từ cloud: `curl -L -r 0-0 -w "%{http_code}"`, mã 206 = OK.
  - Máy ảo `device_bash` **không** ra được GitHub.
  - Trang tag HTML có thể trả 403 cho cloud; đó không phải lỗi.

## 6. Bảng phiên bản
| Bản | Ngày | Ghi chú chính | versionCode |
|---|---|---|---|
| 0.0.1 | 01/11/2026 | Bản đầu (WebView) | – |
| 0.0.2 | 02/11/2026 | 24 bước selftest | – |
| 0.0.3 | 03/11/2026 | TLS Android 6, khớp SWF 98,18% | – |
| 0.0.4 | 16/11/2026 | **Native hoàn toàn**, Liquid Glass, 44 bước | 4001–4003 |
| 0.0.5 | 17/11/2026 | Sửa lag (kính bất đồng bộ), công tắc kính, lỗi "mùng 0", zopfli, 52 bước | 5001–5003 |
| 0.0.6 | 18/11/2026 | Sửa lag Windows, phân trang, Android 6, Trang chủ + Chính sách, 54 bước | 6001–6003 |
