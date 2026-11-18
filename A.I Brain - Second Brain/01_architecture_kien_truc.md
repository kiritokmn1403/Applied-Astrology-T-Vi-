# 01 – Kiến trúc dự án

## 1. Tổng quan
- **Ứng dụng:** lập và luận giải lá số Tử Vi theo phương pháp Bửu Đình, kèm lịch Âm Dương Việt Nam và thư viện 611 bài.
  - Nội dung khôi phục từ phần mềm Flash gốc *Tử Vi Ứng Dụng – Bửu Đình.swf* (6/2021).
- **Lịch sử kiến trúc:**
  - 0.0.1–0.0.3: giao diện HTML/JS chạy trong WebView (Android) / WebView2 + IE11 (Windows).
  - **Từ 0.0.4: native hoàn toàn.** Không dùng WebView, WebView2 hay IE, để hệ điều hành (Task Manager, SmartScreen, Android) không nhận nhầm app là trình duyệt.
- **Nền tảng:**
  - **Android 6.0+ (API 23):**
    - Java Views dựng bằng code: không AndroidX, không XML layout.
    - Gói `com.appliedastrology.tuvi`, minSdk 23, targetSdk 35.
    - `libaacore.so` (C/NDK, `src/android/jni/aacore.c`) dùng cho tìm kiếm thư viện.
  - **Windows 7 SP1+:**
    - C# WinForms + GDI+ trên .NET Framework 4.5.
    - **Chỉ dùng API có từ .NET 4.5.** Ví dụ: không dùng `DateTimeOffset.ToUnixTimeMilliseconds`, mà dùng `Host.NowMs/FromMs`.
    - Windows 7 không có bản ARM; ARM64 chỉ cho Windows 10/11.

## 2. Lõi dùng chung (phải giống hệt ở 2 nền tảng)
| Thành phần | Java (`src/android/java/com/appliedastrology/tuvi/core/`) | C# (`src/windows/`) |
|---|---|---|
| Lunar (Hồ Ngọc Đức), Engine (an sao), Cal (lịch), Json, Fold (bỏ dấu), Library, I18n, Assistant, Theme, Glass | `Lunar.java`, `Engine.java`, `Cal.java`, `Json.java`, `Fold.java`, `Library.java`, `I18n.java`, `Assistant.java`, `Theme.java`, `Glass.java` | `Core.cs`, `Core2.cs` |

- Sửa một bên **phải sửa bên kia**. Nếu sửa Lunar/Engine thì sửa cả `src/web/js`, vì đó là **chuẩn đối chiếu**.
  - Chuẩn này là engine JS bản 0.0.3, đã khớp SWF 98,18%.
  - **Không xóa `src/web`.**
- Bước build `01b_native_crosscheck` kiểm tra tự động; lệch là báo lỗi:
  - JS↔Java: 3000 lá số / 4000 ngày / 1608 lần đổi Âm→Dương.
  - C#↔Java: trùng từng byte.
  - Engine kính Java↔C#: trùng từng byte (`GlassDumpJ.java` ↔ `GlassDumpCs.cs`).

## 3. Lớp giao diện
- **Android** (`src/android/java/com/appliedastrology/tuvi/`):
  - `MainActivity` (khởi động, tab, edge-to-edge, selftest, nạp sẵn thư viện ở luồng `aa-warm`).
  - `Screens` (các màn hình, kết quả tìm kiếm phân trang), `Ui`, `ChartViews`, `MonthView`, `GlassViews`.
  - `Updates` (kiểm tra/tải cập nhật), `TlsCompat` (CA cho Android 6), `ApkProvider`, `NativeCore` (JNI), `Host`, `SelfTest`.
- **Windows** (`src/windows/`):
  - `WinMain.cs` (form chính, `DBPanel` khung cuộn, `ScrollStep/ScrollSettle`, nạp sẵn thư viện).
  - `WinScreens.cs` (màn hình, `AddResults/ExpandResults`), `WinUi.cs` (`Box`, `GButton`, `TextBlock`, `GlassCtx`).
  - `WinHost.cs` (phiên bản, lưu trữ, nhật ký), `WinSelfTest.cs`, `Setup.cs` (bộ cài native), `Qr.cs`.
  - `app.manifest`, `AppxManifest.template.xml`.

## 4. Liquid Glass
- **Lõi `Glass`:** bản đồ khúc xạ **thưa**, chỉ lưu điểm ảnh trong dải viền vát (`Maps.idx/dx/dy/mask/spec`, `rowL[y]`).
  - `render()` = lõi + dải (Windows), `renderBand()` = chỉ dải (Android).
  - Lấy mẫu song tuyến tính; cache tối đa 12 mục.
- **Android `GlassViews`:**
  - Luồng nền `aa-glass` (ưu tiên thấp) dựng tinh vân, làm mờ và dải viền. Khi chưa xong thì vẽ tạm kính mờ.
  - Lõi kính = `BitmapShader` + `ColorMatrixColorFilter` + lớp phủ màu, do GPU vẽ.
  - Cuộn: `Bg.onScroll()` gộp bằng `postOnAnimation`; chỉ vẽ lại thẻ đang gắn vào cửa sổ **và** đổi vị trí.
  - `kind`: 0 thẻ (không elevation), 1 thanh/hộp thoại, 2 khối màu đặc (toast).
- **Windows `GlassCtx` + `Box`:**
  - Tinh vân dựng 1 lần (khung bội 64 px). `Full`/`BlurBmp` là bitmap PArgb **đúng kích thước** (`GlassSize`, dùng chung cho form và bố cục), luôn vẽ bằng `DrawImageUnscaled`.
  - `Box` có `bgCache` (LRU 48).
  - **0.0.6:**
    - `PlainChain/PaintFull`: khung không kính vẽ thẳng ảnh nền.
    - `DBPanel` vẽ nền không co giãn.
    - Khi đang cuộn (`Box.Scrolling`), dùng lại nền kính đã dựng. Dừng cuộn 220 ms thì `ScrollSettle` vẽ lại chính xác.
    - Chỉ `Invalidate` điều khiển đang nằm trong khung nhìn (`InvalidateVisible`).
- **Công tắc:** `lgOn` (0.0.5). Máy yếu (Android: `isLowRamDevice`, heap ≤ 128 MB hoặc API < 26) lần đầu dùng `frost`.

## 5. Dữ liệu dùng chung (sinh lúc build)
- `tools/build_native_data.js` sinh ra `src/shared/data/` (đã nằm trong `.gitignore`).
- **Nguồn:**
  - `src/web/data` (thư viện, dữ liệu lá số).
  - `tools/i18n_v4` + `i18n_v5` + `i18n_v6` (`<mã>.json`).
  - `tools/changelog_v4/v5/v6.json`.
- **Kết quả:**
  - `lib/index.json`, `lib/NN.json` (`[[tiêu_đề, nội_dung], …]`, 13 chương, 611 bài).
  - `lang/<mã>.json`, `langs.json`, `changelog.json`.
- **68 ngôn ngữ, 273 khóa** (0.0.6). Dòng log phải là `(273 keys, 0 filled from en/vi)`.
- **Thêm chuỗi giao diện:**
  1. Tạo `tools/i18n_v<N>/make_i18n_v<N>.py` với đủ 68 ngôn ngữ (script tự báo nếu thiếu).
  2. Chạy script đó.
  3. Thêm thư mục vào vòng lặp trong `build_native_data.js`.
- **Bản dịch thư viện** (đang làm, chưa đóng gói):
  - `translations/lib/<mã>/NN.json` cùng cấu trúc với `lib/NN.json`.
  - Kèm `translations/glossary.json` và `translations/PROGRESS.md`.
  - Dự kiến tích hợp kiểu **tải khi cần**, vì thư viện chiếm khoảng 80% dung lượng gói.

## 6. Lưu trữ (giữ tương thích từ 0.0.3, không đổi tên khóa)
- Nơi lưu:
  - Android: SharedPreferences `aa_store`.
  - Windows: `%APPDATA%\AppliedAstrology\store.json`; bản portable dùng `userdata\`.
- Các khóa: `aa.settings`, `aa.saved`, `aa.bookmarks`, `aa.recent`, `aa.last`, `aa.readerFont`, `aa.lastUpdateCheck`.
- Liquid Glass (trong `aa.settings`): `lgOn`, `lgMode` (`frost`/`liquid`; giá trị cũ `off` được đổi thành `lgOn=false`), `lgBg`, `lgBlur`, `lgRefr`, `lgBezel`, `lgTint`, `lgSpec`, `lgSat`, `lgCards`, `lgChart`.

## 7. Kết nối mạng của app (phải khớp trang Chính sách quyền riêng tư)
- **Tự động:** chỉ `api.github.com/repos/kiritokmn1403/Applied-Astrology-T-Vi-/releases` (kiểm tra cập nhật). Tải gói cập nhật từ GitHub Releases khi người dùng đồng ý.
- **Chỉ khi người dùng bấm:**
  - *Kiểm tra mạng & VPN* → `https://www.cloudflare.com/cdn-cgi/trace`.
  - *Dịch* → mở Google Dịch trong trình duyệt.
  - Chia sẻ / mở liên kết / báo lỗi qua ứng dụng người dùng chọn.
- Thêm kết nối mới ⇒ **bắt buộc** cập nhật `docs/privacy.html`.

## 8. Phiên bản & ký
- **versionCode Android** = `<ver>00<n>`:
  - 0.0.4 → 4001–4003.
  - 0.0.5 → 5001–5003.
  - **0.0.6 → 6001 (armeabi-v7a), 6002 (x86_64), 6003 (arm64-v8a).**
- Bản sau phải có versionCode **lớn hơn** và dùng **cùng khóa ký**, nếu không sẽ không cài đè được.
- Windows: `AssemblyVersion`/FileVersion `0.0.N.0`; AppX `Version="0.0.N.0"`. Ký bằng chứng chỉ tự ký `AppliedAstrology-codesign.pfx`.
- Đóng gói: `tools/zip_recompress.js` (zopfli WebAssembly) nén lại APK (trước zipalign), portable.zip và payload Setup.exe; nhỏ hơn khoảng 6%.
