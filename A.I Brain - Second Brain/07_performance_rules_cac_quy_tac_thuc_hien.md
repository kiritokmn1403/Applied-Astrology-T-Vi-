# 07 – Các quy tắc thực hiện

Vi phạm một quy tắc = nguy cơ lỗi cũ quay lại. Mỗi quy tắc đều rút ra từ lỗi thật (xem `04`).

## A. Hiệu năng giao diện (bắt buộc khi sửa giao diện, Liquid Glass, danh sách)
1. **Không tính điểm ảnh trên luồng UI.** Tinh vân, làm mờ, khúc xạ chạy ở luồng nền (`GlassViews.WORK` trên Android). Luồng UI chỉ vẽ bitmap có sẵn; chưa có thì vẽ tạm bản rẻ (kính mờ).
2. **Không cấp phát trong `onDraw` / `OnPaint`.** Giữ lại Shader, Gradient, Paint, Path; chỉ tạo lại khi khóa (kích thước, tham số) đổi.
3. **Cuộn: chỉ vẽ lại cái đang hiện và thật sự đổi.**
   - Gộp sự kiện cuộn theo khung hình (`postOnAnimation` / `BeginInvoke`).
   - Bỏ qua view ngoài màn hình. Windows: `InvalidateVisible`, không bao giờ `Invalidate(true)` cả cây.
4. **Không dựng lại bộ đệm chỉ vì vị trí đổi trong lúc cuộn.** Dùng lại nền đã dựng (`Box.Scrolling`), dừng cuộn mới vẽ lại chính xác (`ScrollSettle`).
5. **Không elevation/bóng đổ cho phần tử lặp lại** (thẻ danh sách). Chỉ dùng cho thanh tiêu đề, thanh tab, hộp thoại.
6. **Tính phần nhỏ nhất cần thiết.** Khúc xạ chỉ ở dải viền vát; phần lõi để GPU (`BitmapShader` + `ColorMatrix`) hoặc bitmap lo.
7. **Bộ nhớ đệm có giới hạn.** Bản đồ kính ≤ 12 mục (dạng thưa); bộ đệm nền Windows LRU 48.
8. **GDI+: không co giãn khi vẽ lặp lại.**
   - Co giãn 1 lần ra bitmap `Format32bppPArgb` đúng kích thước, rồi `DrawImageUnscaled`.
   - Mọi nơi gọi `G.Ensure` dùng **cùng** `GlassSize`.
9. **Đổi cài đặt không dựng lại thứ không đổi.** Tinh vân khóa theo (kích thước, chủ đề, kiểu nền); làm mờ khóa theo bán kính.
10. **Danh sách dài phải phân trang** (40 mục + "Xem thêm"). Không tạo hàng trăm điều khiển/HWND cùng lúc.
11. **Không làm việc nặng trên luồng UI lúc khởi động hay lần dùng đầu.** Nạp sẵn ở luồng nền (`Library.Warm()/warm()`); cache dùng chung phải có khóa đồng bộ.
12. **Đo, đừng đoán.**
    - Mỗi bản phải có selftest 46–51, gfxinfo cũ↔mới (Android) và bảng hiệu năng Windows cũ↔mới.
    - Ngưỡng: cuộn avg < 60 ms/khung, max < 800 ms, settle < 800 ms; hộp thoại Android (luồng UI) < 400 ms; tab < 2500 ms.
    - Emulator phải dùng GPU thật.
13. **Selftest đo đúng đường mã người dùng chạy**, không đi đường tắt riêng cho test.
14. **Dung lượng:**
    - Dữ liệu thư viện chiếm khoảng 80% gói. Luôn chạy `zip_recompress.js` (zopfli).
    - Không thêm ảnh lớn khi vẽ vector được.
    - Không nhúng bản dịch thư viện 67 ngôn ngữ vào gói: dùng tải khi cần.

## B. Mã nguồn & tương thích
15. **Lõi Java và C# phải giống hệt.** Sửa một bên thì sửa bên kia; nếu là Lunar/Engine thì sửa cả `src/web/js`. Bước 01b phải khớp 100% / trùng byte.
16. **Windows chỉ dùng API .NET 4.5** (chạy được trên Windows 7 SP1). **Android minSdk 23**: API mới hơn phải kiểm `Build.VERSION.SDK_INT`.
17. **Không đổi tên khóa lưu trữ** (`aa.*`, `lg*`), để nâng cấp không mất dữ liệu. Giá trị cũ phải được chuyển đổi (ví dụ `lgMode=off` → `lgOn=false`).
18. **versionCode tăng dần, cùng khóa ký**, để cài đè được.
19. **Chuỗi giao diện mới phải đủ 68 ngôn ngữ thật.** Log `0 filled from en/vi`.
20. **Không thêm WebView / WebView2 / IE / trình duyệt nhúng** (bước 43 và `windows_no_browser_modules` phải PASS).
21. **Thêm kết nối mạng mới ⇒ cập nhật `docs/privacy.html`** (mục 3 / 3b).
22. Sửa script/tệp lớn bằng **script biến đổi có `assert`** (thay đúng chuỗi, báo lỗi nếu không tìm thấy); không gõ lại cả tệp từ đầu ra công cụ (có thể bị cắt).

## C. Bảo mật & riêng tư
23. **Không bao giờ in, ghi hay commit**: token GitHub, mật khẩu keystore/pfx, `*.jks`, `*.pfx`, `keystore-password.txt`.
24. **Không ghi email hay thông tin cá nhân của Kiệt** vào script, tài liệu, commit message. Danh tính git lấy từ cấu hình máy; dự phòng dùng `<owner>@users.noreply.github.com`.
25. **Không force-push.** Không xóa Release hay tag cũ. Không đổi chế độ hiển thị của repo.
26. Xóa tệp trên máy Kiệt phải xin quyền. Nếu không được thì chuyển vào `_to_delete/` và báo lại.

## D. Tài liệu & giao tiếp
27. Mọi tài liệu và trả lời **bằng tiếng Việt**. Tóm tắt yêu cầu và **chờ xác nhận** trước khi sửa code.
28. `WORK_PROGRESS_v<ver>.md` ghi khoảng 5 phút/lần; xong thì ghi "HOÀN THÀNH" và dừng.
29. `TECHNICAL_UPDATE_NOTES.md` ghi đủ: hạng mục, lỗi phát hiện, dòng lệnh, công cụ, kết quả kiểm thử, **ngày giờ**.
30. **Nói thật**:
    - Bước nào FAIL, việc nào chưa làm (ví dụ chưa kiểm thử VPN thật), số liệu nào xấu đi (ví dụ `47_perf_modal` Windows +80 ms) đều phải ghi rõ.
    - Không viết câu quảng cáo phóng đại ("60 FPS", "chuẩn xác tuyệt đối").
31. Cập nhật A.I Brain ở cuối mỗi phiên, đúng tệp theo chủ đề; bàn giao theo `11`; thêm 1 mục vào `12_session log`.
32. Dừng giữa chừng thì **phải** ghi mục "TRẠNG THÁI KHI TẠM DỪNG" và đồng bộ mã lên máy (không để thay đổi chỉ nằm ở cloud).
