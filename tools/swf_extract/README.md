# Công cụ trích xuất SWF (v0.0.2)

Các script Python dùng để khôi phục nội dung từ `Tử Vi Ứng Dụng - Bửu Đình.swf` (Flash 8, 26 MB):

| Script | Việc làm |
|---|---|
| `swfparse.py` | Đọc thẻ SWF: DefineFont/FontInfo/Font2 (bảng mã glyph), DefineText2 (chữ tĩnh), DefineEditText (bài viết HTML) |
| `s3.py` | Dựng cây Sprite / Frame / Button để biết bài viết thuộc chương nào (Frame 4…16 của timeline gốc) |
| `glyphs.py`, `decode.py` | Giải mã chữ tĩnh bị lỗi bảng mã (byte thấp của Unicode): so khớp hình dạng glyph + từ điển tần suất âm tiết |
| `vni.py` | Chuyển chữ phông VNI-Win sang Unicode NFC |
| `t.py`, `dump.py` | Trích bài học chương "An Sao" theo thứ tự hiển thị |
| `tmpl.py`, `tmpl2.py` | Trích 60 mẫu lá số Can Chi (vị trí sao theo ô) → `tools/tests/swf_templates.json` |
| `build_library.py` | Sinh `src/web/data/lib_index.js` và `lib_01..13.js` (611 bài, 13 chương) |

Đường dẫn tạm (`/tmp/swfx`, `/tmp/swf.bin`) là thư mục làm việc khi chạy trong môi trường Linux; `swf.bin` là SWF đã giải nén (FWS).
