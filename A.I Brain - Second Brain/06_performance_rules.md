# Quy tắc hiệu năng (rút ra từ lỗi lag 0.0.4 → 0.0.5)

Áp dụng mỗi khi sửa giao diện, Liquid Glass hoặc danh sách dài. Vi phạm một quy tắc = nguy cơ lag/treo quay lại.

1. **Không tính điểm ảnh trên luồng UI.** Tinh vân, làm mờ, khúc xạ → luồng nền (`GlassViews.WORK` trên Android). Luồng UI chỉ vẽ bitmap đã có; khi chưa có thì vẽ tạm bản rẻ (kính mờ).
2. **Không cấp phát trong `onDraw` / `OnPaint`.** Giữ lại Shader, Gradient, Paint, Path; tạo lại chỉ khi khóa (kích thước, tham số) đổi.
3. **Cuộn: chỉ vẽ lại cái thay đổi.** Gộp sự kiện cuộn theo khung hình (`postOnAnimation` / `BeginInvoke`), bỏ qua view ngoài màn hình hoặc không đổi vị trí.
4. **Không elevation/bóng đổ cho phần tử lặp lại** (thẻ danh sách). Chỉ thanh tiêu đề, thanh tab, hộp thoại.
5. **Tính phần nhỏ nhất cần thiết.** Khúc xạ chỉ ở dải viền vát; phần lõi để GPU (BitmapShader + ColorMatrix) hoặc vẽ bitmap.
6. **Bộ nhớ đệm có giới hạn.** Cache bản đồ ≤ 12 mục (dạng thưa); bộ đệm nền Windows LRU 48.
7. **GDI+: không co giãn khi vẽ lặp lại.** Co giãn 1 lần ra bitmap `Format32bppPArgb` đúng kích thước rồi `DrawImageUnscaled`.
8. **Đổi cài đặt không được dựng lại thứ không đổi.** Tinh vân khóa theo (kích thước, chủ đề, kiểu nền); làm mờ khóa theo bán kính.
9. **Đo, đừng đoán.** Mỗi bản phải có số liệu: selftest 46–51 và bảng gfxinfo bản cũ ↔ bản mới trong `TEST_REPORT.md`. Ngưỡng hiện tại: cuộn avg < 60 ms/khung, max < 800 ms; mở hộp thoại < 400 ms (Android, luồng UI).
10. **Dung lượng:** dữ liệu thư viện chiếm ~80% gói; luôn chạy `zip_recompress.js` (zopfli) cho APK/zip; không thêm tài nguyên ảnh lớn khi có thể vẽ vector.
