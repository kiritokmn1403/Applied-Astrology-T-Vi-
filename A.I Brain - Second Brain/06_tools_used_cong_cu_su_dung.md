# 06 – Công cụ sử dụng

Đường dẫn lấy từ bước `00_probe_tools` của `build_release_0.0.6.ps1` (18/11/2026). Đổi máy hoặc đường dẫn thì sửa phần dò công cụ trong script.

## 1. Trên máy Windows của Kiệt (máy `KIRITOKMN_LAP`, Windows 11 Pro x64)
| Công cụ | Đường dẫn | Dùng để |
|---|---|---|
| Android SDK | `D:\yh\tools\android-sdk` (platforms 34–37, build-tools **36.0.0**, cmdline-tools, platform-tools) | `android.jar` (API 35), d8, aapt2, zipalign, apksigner, adb, sdkmanager, avdmanager |
| Android NDK | `D:\yh\tools\android-sdk\ndk\28.2.13676358` | `clang` biên dịch `libaacore.so` cho 3 ABI, `llvm-strip` |
| Emulator + AVD | `C:\Users\nguye\AppData\Local\Android\Sdk\emulator\emulator.exe` | Chạy `Medium_Phone_API_37.0`, `OOS_Android6_API23` (`-gpu auto`) |
| JDK | `D:\yh\tools\jdk` | `javac`, `jar`, `java` (đối chiếu chéo) |
| Roslyn `csc` | `C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\MSBuild\Current\Bin\Roslyn\csc.exe` | Biên dịch app WinForms + Setup.exe (tham chiếu .NET 4.x tại `C:\Windows\Microsoft.NET\Framework64\v4.0.30319`) |
| Windows SDK 10.0.26100 | `C:\Program Files (x86)\Windows Kits\10\bin\10.0.26100.0\x64\` | `makeappx.exe` (appxbundle), `signtool.exe` (ký exe/msi/appx) |
| WiX 3.14 | `D:\yh\tools\wix314` | Tạo MSI x64/x86/arm64 |
| NuGet | `D:\yh\tools\nuget\nuget.exe` | Gói phụ trợ (nếu cần) |
| Node.js | `C:\Program Files\nodejs\node.exe` | `build_native_data.js`, unit test JS, `zip_recompress.js` (zopfli WebAssembly ở `tools/zopfli_vendor`) |
| ffmpeg 9.0.2 | `D:\yh\tools\ffmpeg-9.0.2-essentials_build\bin\ffmpeg.exe` | Quay video kiểm thử Windows (`gdigrab`) |
| Git | `C:\Program Files\Git\cmd\git.exe` + Git Credential Manager | Commit/push, lấy token cho API Release (không in ra) |
| GitHub Desktop | `C:\Users\nguye\AppData\Local\GitHubDesktop` | Đăng nhập GitHub (cấp credential), xem lịch sử commit, cấu hình danh tính git |
| PowerShell 5.1 | hệ thống | Chạy `build_release_0.0.N.ps1` (qua `.bat`) |
| scrcpy 5.0.1 | `D:\yh\tools\scrcpy` | Xem/điều khiển màn hình Android qua USB (thủ công) |
| Android Studio | `D:\programs\ads\bin\studio64.exe` | Có sẵn nhưng pipeline **không** dùng Gradle/IDE; chỉ dùng SDK/emulator của nó |
| Visual Studio Code | `C:\Users\nguye\AppData\Local\Programs\Microsoft VS Code` | Xem/sửa tay mã nguồn (tùy chọn) |
| Google Antigravity | `C:\Users\nguye\AppData\Local\Programs\Antigravity\antigravity.exe` | Tác nhân AI thứ hai (Gemini), nhận việc qua `/swarm` (xem `09`) |
| Có sẵn, chưa dùng cho dự án | `D:\yh\tools\flutter`, `flutter329`, `mingw64`, `cargo`, `rustup`, gradle 9.3.1 (zip) | – |

## 2. Phía Claude (phiên AI từ xa)
| Công cụ | Dùng để |
|---|---|
| Cloud container (Linux) | Sửa mã trên bản sao; `javac -source 8 -target 8 -bootclasspath android.jar`; `node build_native_data.js`; Python (sinh i18n, biến đổi script); `curl` kiểm tra link Release (mã 206) |
| `device_bash` (máy ảo Linux trên máy Kiệt) | Đọc/ghi/tìm trong thư mục dự án, giải nén gói đồng bộ, đọc log/kết quả, chạy `aa006_progress.sh`. **Không** chạy được exe Windows và **không** ra mạng GitHub |
| `device_stage_files` / `device_commit_files` | Chuyển tệp giữa máy và cloud (tgz có timestamp) |
| Điều khiển màn hình (computer use) | File Explorer (chỉ click: mở `.bat`), Antigravity (gõ được), GitHub Desktop; cmd/PowerShell/IDE chỉ click |
| Trình duyệt tích hợp (built-in browser) | Mở GitHub Pages thật để xem hiển thị và chụp màn hình |
| WebFetch / WebSearch | Kiểm tra trang GitHub (có thể bị giới hạn lượt) |
| Công cụ không có ở cloud (mạng cài gói bị tắt) | Mono `mcs`, PowerShell 7 → C# và `.ps1` được kiểm bằng bước build trên máy (BUILD_ONLY chạy được đến bước 05 nghĩa là cú pháp và biên dịch đều OK) |

## 3. Script & công cụ của dự án (`tools/`)
- `build_native_data.js`: sinh `src/shared/data` (thư viện, ngôn ngữ, changelog).
- `zip_recompress.js` + `zopfli_vendor/`: nén lại APK/zip/payload (deflate chuẩn).
- `i18n_v4`, `i18n_v5`, `i18n_v6` (+ `make_i18n_v6.py`): chuỗi giao diện 68 ngôn ngữ. `i18n_src`, `i18n_v3`, `build_i18n.py`, `merge_i18n_v3.py` là lịch sử.
- `changelog_v4/v5/v6.json`: "Có gì mới" trong app (vi + en).
- `swf_extract/`: trích nội dung từ SWF Bửu Đình (`swfparse.py`).
- `tests/`: `engine_test.js`, `calendar_test.js`, `i18n_test.js`, `swf_templates.json`; `tests/native/`: `Dump.java`, `DumpCs.cs`, `js_dump.js`, `GlassDumpJ.java`, `GlassDumpCs.cs`, `GlassDemo.java`, `QrJ.java`.
- `D:\yh\aa006_progress.sh "<ghi chú>" [trạng thái]`: ghi 1 dòng vào `WORK_PROGRESS_v0.0.6.md`, kèm bước build hiện tại (đổi số bản cho bản sau).
