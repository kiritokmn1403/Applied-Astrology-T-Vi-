# 10 – Cấu trúc thư mục dự án

## 1. Thư mục dự án (repo GitHub `kiritokmn1403/Applied-Astrology-T-Vi-`)
`D:\A.I Project\A.I Project - Applied Astrology (Tử Vi Ứng Dụng)\Applied Astrology (Tử Vi)\Applied Astrology (Tử Vi)\`

```
├─ A.I Brain - Second Brain/      ← bộ nhớ dự án, 13 tệp 00–12 (đọc 00 trước tiên)
├─ src/
│  ├─ android/
│  │  ├─ AndroidManifest.xml       (versionName; versionCode điền lúc build)
│  │  ├─ java/com/appliedastrology/tuvi/        MainActivity, Screens, Ui, ChartViews, MonthView, GlassViews,
│  │  │                                         Updates, TlsCompat, ApkProvider, NativeCore, Host, SelfTest
│  │  ├─ java/com/appliedastrology/tuvi/core/   lõi dùng chung: Lunar, Engine, Cal, Json, Fold, Library, I18n, Assistant, Theme, Glass
│  │  ├─ java/io/nayuki/qrcodegen/              thư viện QR
│  │  ├─ jni/aacore.c                           libaacore.so (tìm kiếm)
│  │  ├─ certs/ , res/                          CA cho Android 6, biểu tượng
│  ├─ windows/                     Core.cs, Core2.cs (lõi), WinMain/WinScreens/WinUi/WinHost/WinSelfTest.cs,
│  │                               Setup.cs, Qr.cs, app.manifest, App.config, AppxManifest.template.xml, appx_assets/,
│  │                               Install-AppliedAstrology-appx.bat
│  ├─ web/                         engine JS gốc 0.0.3 = CHUẨN ĐỐI CHIẾU (js/, data/ thư viện + dữ liệu, css/) – KHÔNG XÓA
│  ├─ legacy_0.0.3/                mã Windows WebView2 cũ (tham khảo)
│  ├─ webx/                        (trống)
│  └─ shared/                      SINH LÚC BUILD (gitignore): data/lib, data/lang, langs.json, changelog.json
├─ tools/                          build_native_data.js, zip_recompress.js, zopfli_vendor/, i18n_v3…v6/, i18n_src/,
│                                  changelog_v4…v6.json, swf_extract/, tests/ (+ tests/native/ bộ đối chiếu)
├─ translations/                   (0.0.6, Antigravity) glossary.json, PROGRESS.md, lib/<mã>/NN.json – chưa đóng gói
├─ docs/                           GitHub Pages: index.html (Trang chủ + #download), privacy.html; RELEASE_NOTES_<ver>.md
├─ assets/                         logo.svg, logo_*.png, app_icon.ico
├─ output/<ver>/                   tệp cài đặt + SHA256SUMS (0.0.1 … 0.0.6) – được commit
├─ test_reports/<ver>/             TEST_REPORT.md, all_results.json, perf_compare*.json, native_crosscheck.txt,
│                                  android/<thiết bị>/selftest/*.png, windows/<gói>/*.png, *.mp4 (mp4 không commit)
│  └─ 0.0.6_round1/                báo cáo lượt 1 của 0.0.6
├─ build_logs/                     build_<ver>_<ngày>_<giờ>.log, status.json (gitignore)
├─ screenshots/                    ảnh minh họa cũ cho README
├─ .github/workflows/scala.yml     do Kiệt thêm trên github.com (16/11/2026)
├─ android/ , windows/             (trống, lịch sử)
├─ build_release_0.0.2 … 0.0.6.ps1 script build/test/phát hành từng bản
├─ BUILD_ONLY_/BUILD_TEST_/BUILD_/PUBLISH_v<ver>.bat
├─ README.md, CHANGELOG.md, LICENSE (GPL-3.0)
├─ TECHNICAL_UPDATE_NOTES.md       nhật ký kỹ thuật mọi bản (0.0.1 → 0.0.6)
├─ WORK_PROGRESS_v0.0.4/5/6.md     tiến độ theo thời gian từng bản
├─ AG_REPORT_v0.0.6.md             báo cáo của Google Antigravity
└─ _note9_*, _check_ie11.bat       tệp phụ trợ thủ công từ 0.0.2–0.0.3
```
- `.gitignore`: `build_logs/`, `*.jks`, `*.pfx`, `*.idsig`, `*.mp4`, `src/shared/`, `__pycache__/`, `src/web/qa_selftest.html`.

## 2. Thư mục build & công cụ (`D:\yh` = junction tới `D:\A.I Project\yh`)
```
D:\yh\
├─ aa006\                build 0.0.6: stage\ (bị xóa mỗi lần build), crosscheck\, android\, windows\, test_* …
├─ aa002 … aa005\        build các bản trước
├─ aa002_keys\           KHÓA KÝ (jks, pfx, mật khẩu) – không bao giờ commit/in ra
├─ aa006_refs\           repo tham chiếu clone nông (liquid-glass, LunarCalendar, aniyomi, mangayomi, Nekoyomi…)
├─ tools\                android-sdk, jdk, wix314, nuget, ffmpeg-9.0.2…, git, scrcpy, flutter, mingw64, cargo, rustup
├─ aa006_progress.sh     ghi tiến độ vào WORK_PROGRESS_v0.0.6.md
├─ aa00N_src_/sync_/docs_<timestamp>.tgz   gói đồng bộ giữa cloud và máy (có thể dọn khi cần)
└─ avd\, build*\, emu*.log, *.zip …        phụ trợ/lịch sử
```

## 3. Các thư mục khác được chia sẻ trong phiên
- `D:\A.I Project\A.I Project - Applied Astrology (Tử Vi Ứng Dụng)\`: chứa thư mục dự án + `Tử Vi Ứng Dụng - Bửu Đình.swf` (25 MB, nguồn nội dung gốc).
- `D:\A.I Project\GitHub\`: các repo tham chiếu khác của Kiệt (PdfBox-Android, QR-Code-generator, onedrive-sync…).
- `D:\A.I Project\Claude outputs\`: ảnh/tệp xuất từ các phiên Claude cho nhiều dự án.
- Emulator: `C:\Users\nguye\AppData\Local\Android\Sdk\emulator`.

## 4. Đường dẫn trên điện thoại (Android)
- Dữ liệu app: SharedPreferences `aa_store`.
- Kết quả selftest: `/sdcard/Android/data/com.appliedastrology.tuvi/files/selftest/` (`results.json`, `steps.jsonl`, `NN_<bước>.png`).
