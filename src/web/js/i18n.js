/*
 * Applied Astrology v0.0.3 - Đa ngôn ngữ
 * Danh sách ngôn ngữ = hợp của Aniyomi/Nekoyomi (moko-resources, 63 locale) và Mangayomi (19 locale).
 * Tiếng Việt & tiếng Anh nằm sẵn trong tệp này; các ngôn ngữ khác nạp theo yêu cầu từ data/lang/<mã>.js
 * Nội dung sách Bửu Đình giữ nguyên văn tiếng Việt (bản gốc); tên sao, cung, chương được dịch.
 */
(function (root) {
  'use strict';
  var LANGS = [
    ['vi', 'Tiếng Việt'], ['en', 'English'], ['am', 'አማርኛ'], ['ar', 'العربية', 1], ['as', 'অসমীয়া'], ['be', 'Беларуская'],
    ['bg', 'Български'], ['bn', 'বাংলা'], ['ca', 'Català'], ['ceb', 'Cebuano'], ['cs', 'Čeština'], ['cv', 'Чӑвашла'],
    ['da', 'Dansk'], ['de', 'Deutsch'], ['el', 'Ελληνικά'], ['eo', 'Esperanto'], ['es', 'Español'], ['es-419', 'Español (Latinoamérica)'],
    ['eu', 'Euskara'], ['fa', 'فارسی', 1], ['fi', 'Suomi'], ['fil', 'Filipino'], ['fr', 'Français'], ['gl', 'Galego'],
    ['he', 'עברית', 1], ['hi', 'हिन्दी'], ['hr', 'Hrvatski'], ['hu', 'Magyar'], ['id', 'Bahasa Indonesia'], ['it', 'Italiano'],
    ['ja', '日本語'], ['jv', 'Basa Jawa'], ['ka', 'ქართული'], ['kk', 'Қазақ тілі'], ['km', 'ខ្មែរ'], ['kn', 'ಕನ್ನಡ'],
    ['ko', '한국어'], ['lt', 'Lietuvių'], ['lv', 'Latviešu'], ['ml', 'മലയാളം'], ['mr', 'मराठी'], ['ms', 'Bahasa Melayu'],
    ['my', 'မြန်မာ'], ['nb', 'Norsk bokmål'], ['ne', 'नेपाली'], ['nl', 'Nederlands'], ['nn', 'Norsk nynorsk'], ['pl', 'Polski'],
    ['pt', 'Português'], ['pt-BR', 'Português (Brasil)'], ['ro', 'Română'], ['ru', 'Русский'], ['sa', 'संस्कृतम्'], ['sah', 'Саха тыла'],
    ['sc', 'Sardu'], ['sdh', 'کوردی خوارین', 1], ['sk', 'Slovenčina'], ['sq', 'Shqip'], ['sr', 'Српски'], ['sv', 'Svenska'],
    ['ta', 'தமிழ்'], ['te', 'తెలుగు'], ['th', 'ไทย'], ['tr', 'Türkçe'], ['uk', 'Українська'], ['uz', 'Oʻzbekcha'],
    ['zh-CN', '简体中文'], ['zh-TW', '繁體中文']
  ];

  var D = {};
  D.vi = {
    app_name: 'Tử Vi Ứng Dụng', app_sub: 'Applied Astrology · Di sản Bửu Đình',
    tab_chart: 'Lá Số', tab_library: 'Thư Viện', tab_assistant: 'Luận Giải', tab_saved: 'Hồ Sơ', tab_settings: 'Cài Đặt',
    f_name: 'Họ và tên', f_gender: 'Giới tính', male: 'Nam', female: 'Nữ', f_calendar: 'Lịch', solar: 'Dương lịch', lunar: 'Âm lịch',
    f_day: 'Ngày', f_month: 'Tháng', f_year: 'Năm', f_hour: 'Giờ sinh', f_leap: 'Tháng nhuận', f_viewyear: 'Xem hạn năm',
    btn_build: 'Lập lá số', btn_save: 'Lưu hồ sơ', btn_png: 'Xuất ảnh PNG', btn_qr: 'Mã QR', btn_share: 'Chia sẻ', btn_print: 'In',
    btn_close: 'Đóng', btn_back: 'Quay lại', btn_prev: 'Bài trước', btn_next: 'Bài sau', btn_open: 'Mở', btn_delete: 'Xóa',
    btn_bookmark: 'Đánh dấu', btn_translate: 'Dịch trực tuyến', btn_search: 'Tìm', btn_send: 'Gửi', btn_reset: 'Khôi phục mặc định',
    thien_ban: 'Thiên Bàn', solar_date: 'Dương lịch', lunar_date: 'Âm lịch', year_cc: 'Năm', month_cc: 'Tháng', day_cc: 'Ngày', hour_cc: 'Giờ',
    am_duong: 'Âm Dương', ban_menh: 'Bản mệnh', cuc: 'Cục', menh_cuc: 'Mệnh – Cục', menh_chu: 'Mệnh chủ', than_chu: 'Thân chủ',
    menh: 'Mệnh', than: 'Thân', vcd: 'Vô Chính Diệu', dai_han: 'Đại hạn', tieu_han: 'Tiểu hạn', tuan: 'Tuần', triet: 'Triệt', age: 'Tuổi',
    major_stars: 'Chính tinh', good_stars: 'Cát tinh', bad_stars: 'Sát / Bại tinh', other_stars: 'Trung tinh', luu_stars: 'Lưu niên',
    rings: 'Vòng sao', tam_phuong: 'Tam phương tứ chính', related: 'Bài viết Bửu Đình liên quan', no_related: 'Chưa tìm thấy bài viết liên quan.',
    pick_palace: 'Chạm vào một cung để xem chi tiết và bài bình giải của cụ Bửu Đình.',
    lib_title: 'Thư viện Tử Vi Ứng Dụng – Bửu Đình', lib_desc: '{n} bài viết nguyên văn trong {c} chương, khôi phục từ tệp gốc.',
    lib_search_ph: 'Tìm trong toàn bộ thư viện (có dấu hoặc không dấu)…', lib_results: '{n} kết quả', lib_loading: 'Đang nạp dữ liệu…',
    lib_bookmarks: 'Đã đánh dấu', lib_recent: 'Đọc gần đây', lib_continue: 'Đọc tiếp', articles: 'bài', chars: 'ký tự',
    reader_font: 'Cỡ chữ', reader_original_note: 'Nội dung giữ nguyên văn tiếng Việt của tác giả Bửu Đình.',
    as_title: 'Trợ lý luận giải ngoại tuyến', as_note: 'Phân tích dựa trên lá số hiện tại và kho bài viết Bửu Đình. Không cần mạng, không phải AI tạo sinh.',
    as_ph: 'Hỏi về cung, sao, đại hạn… (ví dụ: công danh, tài lộc, hạn năm nay)', as_overview: 'Tổng quan lá số', as_need_chart: 'Hãy lập lá số trước.',
    as_q_menh: 'Cung Mệnh', as_q_career: 'Công danh', as_q_money: 'Tài lộc', as_q_love: 'Phu thê', as_q_year: 'Hạn năm nay', as_q_health: 'Sức khỏe',
    saved_charts: 'Lá số đã lưu', saved_empty: 'Chưa có lá số nào được lưu.', saved_ok: 'Đã lưu lá số.', deleted: 'Đã xóa.',
    s_language: 'Ngôn ngữ', s_theme: 'Giao diện', s_font: 'Cỡ chữ', s_layout: 'Bố cục lá số', s_desktop: 'Chế độ màn hình lớn / Samsung DeX',
    s_desktop_d: 'Hiển thị lá số và bình giải song song (2 cột).', s_rings: 'Hiển thị vòng sao', s_trangsinh: 'Tràng Sinh', s_bacsi: 'Bác Sĩ',
    s_thaitue: 'Thái Tuế', s_luu: 'Sao lưu niên', s_leap: 'Sinh tháng nhuận', s_leap_same: 'Tính như tháng chính', s_leap_next: 'Từ ngày 16 tính sang tháng sau',
    s_network: 'Mạng & VPN', s_network_d: 'Kiểm tra kết nối thực tế (Cloudflare trace). Ứng dụng không tự bật/tắt VPN; VPN được kiểm tra bên ngoài ứng dụng.',
    s_check: 'Kiểm tra kết nối', s_backup: 'Sao lưu & khôi phục', s_export: 'Xuất tệp sao lưu', s_import: 'Nhập từ tệp', s_import_text: 'Dán nội dung JSON',
    s_clear: 'Xóa dữ liệu ứng dụng', s_about: 'Giới thiệu', s_version: 'Phiên bản', s_platform: 'Nền tảng',
    s_about_d: 'Ứng dụng độc lập, mã nguồn mở. Dữ liệu sách: tác phẩm "Tử Vi Ứng Dụng" của tác giả Bửu Đình (Tử Vũ).',
    th_celestial: 'Huyền Không Tinh Tú (tối)', th_gold: 'Hoàng Triều Kim Sắc', th_ivory: 'Bạch Ngọc Cung Đình (sáng)', th_red: 'Chu Sa Cát Tường',
    fs_compact: 'Nhỏ', fs_normal: 'Vừa', fs_large: 'Lớn', fs_xlarge: 'Rất lớn',
    lay_auto: 'Tự động', lay_board: 'Bàn 4×4 cổ điển', lay_list: 'Danh sách 12 cung',
    net_checking: 'Đang kiểm tra…', net_ok: 'Đã kết nối', net_fail: 'Không có kết nối Internet', net_vpn_on: 'Đang đi qua WARP/VPN',
    net_vpn_off: 'Không phát hiện WARP', net_ip: 'IP', net_loc: 'Vị trí', net_latency: 'Độ trễ',
    qr_title: 'Mã QR lá số', qr_desc: 'Quét mã bằng Applied Astrology trên thiết bị khác hoặc lưu lại để lập lại lá số.',
    png_ok: 'Đã xuất ảnh lá số', import_ok: 'Đã khôi phục dữ liệu', import_fail: 'Tệp không hợp lệ', confirm_clear: 'Xóa toàn bộ hồ sơ và cài đặt?',
    invalid_date: 'Ngày tháng không hợp lệ', invalid_lunar: 'Ngày âm lịch không tồn tại (kiểm tra tháng nhuận / ngày 30)',
    ch_chinh_tinh: 'Chính Tinh', ch_can_tinh: 'Can Tinh', ch_chi_tinh: 'Chi Tinh', ch_sao_khac: 'Các Sao Khác', ch_cac_cung: 'Các Cung',
    ch_72_cach: '72 Cách', ch_an_sao: 'An Sao', ch_han: 'Hạn', ch_la_so_nam: 'Lá Số Nam', ch_la_so_nu: 'Lá Số Nữ', ch_tu_dien: 'Từ Điển Tử Vi',
    ch_2_chinh_tinh: '2 Chính Tinh', ch_cac_bo: 'Các Bộ Sao',
    p_menh: 'Mệnh', p_phumau: 'Phụ Mẫu', p_phucduc: 'Phúc Đức', p_dientrach: 'Điền Trạch', p_quanloc: 'Quan Lộc', p_nobOc: 'Nô Bộc',
    p_thiendi: 'Thiên Di', p_tatach: 'Tật Ách', p_taibach: 'Tài Bạch', p_tutuc: 'Tử Tức', p_phuthe: 'Phu Thê', p_huynhde: 'Huynh Đệ',
    tab_calendar: 'Âm Lịch', cal_leap: 'nhuận', cal_wd: 'T2,T3,T4,T5,T6,T7,CN', cal_wd_full: 'Thứ Hai,Thứ Ba,Thứ Tư,Thứ Năm,Thứ Sáu,Thứ Bảy,Chủ Nhật',
    cal_month_title: 'Tháng {m} năm {y}', cal_today: 'Hôm nay', cal_tz: 'Giờ Việt Nam (GMT+7)', cal_hd: 'Hoàng đạo', cal_hkd: 'Hắc đạo',
    cal_events: 'Ngày lễ', cal_convert: 'Đổi ngày Dương ↔ Âm', cal_s2l: 'Dương → Âm', cal_l2s: 'Âm → Dương', cal_60: 'Lục Thập Hoa Giáp & Nạp Âm',
    cal_60_d: 'Bảng 60 năm Can Chi, mệnh nạp âm và các năm sinh tương ứng (theo khung lập lá số của phần mềm gốc). Chạm vào một dòng để xem tuổi.',
    cal_find_year: 'Tra năm sinh', cal_napam: 'Nạp âm', cal_years: 'Các năm sinh', cal_tiet: 'Tiết khí', cal_tiet_start: 'bắt đầu tiết',
    cal_daytype: 'Ngày', cal_truc: 'Trực', cal_hour0: 'Giờ Tí đầu ngày', cal_good_hours: 'Giờ hoàng đạo', cal_bad_hours: 'Giờ hắc đạo',
    cal_build_chart: 'Lập lá số theo ngày này', cal_open_day: 'Xem ngày này', cal_age: 'Tuổi (âm)',
    s_paper: 'Giấy lót nền lá số', s_paper_d: 'Màu nền các cung giống phần mềm gốc Bửu Đình.', paper_none: 'Theo giao diện', paper_vangkyxao: 'Vàng kỹ xảo',
    paper_vangchanh: 'Vàng chanh', paper_xanhnhat: 'Xanh nhạt', paper_lucnhat: 'Lục nhạt', paper_timnhat: 'Tím nhạt', paper_vang: 'Vàng',
    ab_open: 'Giới thiệu ứng dụng', ab_title: 'Giới thiệu ứng dụng', ab_updates: 'Cập nhật', ab_auto_check: 'Tự động kiểm tra cập nhật khi mở ứng dụng',
    ab_auto_install: 'Tự cài đặt sau khi tải xong', ab_check: 'Kiểm tra cập nhật', ab_checking: 'Đang kiểm tra bản cập nhật…', ab_last_check: 'Lần kiểm tra gần nhất',
    ab_latest: 'Bạn đang dùng bản mới nhất ({v}).', ab_new: 'Có bản mới: {v}', ab_update_fail: 'Không kiểm tra được bản cập nhật (kiểm tra kết nối mạng).',
    ab_releases: 'Trang phát hành (Releases)', ab_whats_new: 'Có gì mới', ab_download: 'Tải về và cài đặt', ab_downloading: 'Đang tải {p}%',
    ab_dl_done: 'Đã tải xong.', ab_dl_fail: 'Tải về thất bại', ab_install: 'Cài đặt ngay', ab_install_fail: 'Không mở được trình cài đặt',
    ab_no_asset: 'Bản phát hành này chưa có tệp cho thiết bị của bạn. Hãy mở trang Releases.',
    ab_install_note_android: 'Android sẽ hỏi xác nhận. Nếu được yêu cầu, hãy cho phép Applied Astrology cài ứng dụng không rõ nguồn gốc rồi bấm Cài đặt lần nữa.',
    ab_install_note_win: 'Ứng dụng sẽ đóng, cài bản mới rồi tự mở lại.', ab_need_perm: 'Hãy cho phép "Cài ứng dụng không rõ nguồn gốc" cho Applied Astrology, sau đó bấm Cài đặt lần nữa.',
    ab_diag: 'Chẩn đoán', ab_logs: 'Nhật ký ứng dụng', ab_logs_on: 'Ghi nhật ký ứng dụng', ab_logs_view: 'Xem nhật ký', ab_logs_share: 'Xuất / chia sẻ nhật ký',
    ab_logs_clear: 'Xóa nhật ký', ab_errors: 'Báo cáo lỗi', ab_errors_d: 'Lỗi giao diện (JS) và sự cố native đã ghi nhận trên thiết bị này.', ab_errors_none: 'Chưa ghi nhận lỗi nào.',
    ab_errors_clear: 'Xóa báo cáo lỗi', ab_report: 'Gửi báo cáo lỗi', ab_report_d: 'Mở GitHub Issues với thông tin thiết bị điền sẵn; bạn xem lại trước khi gửi.',
    ab_copy_debug: 'Sao chép thông tin gỡ lỗi', ab_copied: 'Đã sao chép', ab_licenses: 'Giấy phép mã nguồn mở', ab_links: 'Liên kết'
  };
  D.en = {
    app_name: 'Applied Astrology', app_sub: 'Tử Vi Ứng Dụng · Bửu Đình heritage',
    tab_chart: 'Chart', tab_library: 'Library', tab_assistant: 'Reading', tab_saved: 'Profiles', tab_settings: 'Settings',
    f_name: 'Full name', f_gender: 'Gender', male: 'Male', female: 'Female', f_calendar: 'Calendar', solar: 'Solar', lunar: 'Lunar',
    f_day: 'Day', f_month: 'Month', f_year: 'Year', f_hour: 'Birth hour', f_leap: 'Leap month', f_viewyear: 'Forecast year',
    btn_build: 'Build chart', btn_save: 'Save profile', btn_png: 'Export PNG', btn_qr: 'QR code', btn_share: 'Share', btn_print: 'Print',
    btn_close: 'Close', btn_back: 'Back', btn_prev: 'Previous', btn_next: 'Next', btn_open: 'Open', btn_delete: 'Delete',
    btn_bookmark: 'Bookmark', btn_translate: 'Translate online', btn_search: 'Search', btn_send: 'Send', btn_reset: 'Reset to defaults',
    thien_ban: 'Heaven Plate', solar_date: 'Solar date', lunar_date: 'Lunar date', year_cc: 'Year', month_cc: 'Month', day_cc: 'Day', hour_cc: 'Hour',
    am_duong: 'Yin–Yang', ban_menh: 'Life element', cuc: 'Element bureau', menh_cuc: 'Life – Bureau', menh_chu: 'Life lord', than_chu: 'Body lord',
    menh: 'Life', than: 'Body', vcd: 'No major star', dai_han: 'Decade', tieu_han: 'Year cycle', tuan: 'Tuần void', triet: 'Triệt void', age: 'Age',
    major_stars: 'Major stars', good_stars: 'Auspicious stars', bad_stars: 'Malefic stars', other_stars: 'Neutral stars', luu_stars: 'Annual stars',
    rings: 'Star rings', tam_phuong: 'Triad & opposite palaces', related: 'Related Bửu Đình articles', no_related: 'No related article found.',
    pick_palace: 'Tap a palace to see its details and Bửu Đình commentary.',
    lib_title: 'Applied Astrology Library – Bửu Đình', lib_desc: '{n} original articles in {c} chapters, restored from the source file.',
    lib_search_ph: 'Search the whole library (with or without Vietnamese accents)…', lib_results: '{n} results', lib_loading: 'Loading data…',
    lib_bookmarks: 'Bookmarks', lib_recent: 'Recently read', lib_continue: 'Continue reading', articles: 'articles', chars: 'characters',
    reader_font: 'Font size', reader_original_note: 'Text is kept in the author\'s original Vietnamese.',
    as_title: 'Offline reading assistant', as_note: 'Analysis based on the current chart and the Bửu Đình library. Works offline; not generative AI.',
    as_ph: 'Ask about a palace, star or period (e.g. career, wealth, this year)', as_overview: 'Chart overview', as_need_chart: 'Please build a chart first.',
    as_q_menh: 'Life palace', as_q_career: 'Career', as_q_money: 'Wealth', as_q_love: 'Marriage', as_q_year: 'This year', as_q_health: 'Health',
    saved_charts: 'Saved charts', saved_empty: 'No saved charts yet.', saved_ok: 'Chart saved.', deleted: 'Deleted.',
    s_language: 'Language', s_theme: 'Theme', s_font: 'Font size', s_layout: 'Chart layout', s_desktop: 'Large screen / Samsung DeX mode',
    s_desktop_d: 'Show chart and commentary side by side (2 columns).', s_rings: 'Show star rings', s_trangsinh: 'Tràng Sinh', s_bacsi: 'Bác Sĩ',
    s_thaitue: 'Thái Tuế', s_luu: 'Annual stars', s_leap: 'Born in a leap month', s_leap_same: 'Count as the regular month', s_leap_next: 'From day 16 count as next month',
    s_network: 'Network & VPN', s_network_d: 'Real connectivity check (Cloudflare trace). The app does not toggle VPN; VPN is tested outside the app.',
    s_check: 'Check connection', s_backup: 'Backup & restore', s_export: 'Export backup file', s_import: 'Import from file', s_import_text: 'Paste JSON',
    s_clear: 'Clear app data', s_about: 'About', s_version: 'Version', s_platform: 'Platform',
    s_about_d: 'Independent open-source app. Book content: "Tử Vi Ứng Dụng" by Bửu Đình (Tử Vũ).',
    th_celestial: 'Celestial Midnight (dark)', th_gold: 'Imperial Gold', th_ivory: 'Ivory Palace (light)', th_red: 'Cinnabar Fortune',
    fs_compact: 'Small', fs_normal: 'Medium', fs_large: 'Large', fs_xlarge: 'Extra large',
    lay_auto: 'Automatic', lay_board: 'Classic 4×4 board', lay_list: '12-palace list',
    net_checking: 'Checking…', net_ok: 'Connected', net_fail: 'No Internet connection', net_vpn_on: 'Traffic goes through WARP/VPN',
    net_vpn_off: 'WARP not detected', net_ip: 'IP', net_loc: 'Location', net_latency: 'Latency',
    qr_title: 'Chart QR code', qr_desc: 'Scan with Applied Astrology on another device, or keep it to rebuild the chart.',
    png_ok: 'Chart image exported', import_ok: 'Data restored', import_fail: 'Invalid file', confirm_clear: 'Delete all profiles and settings?',
    invalid_date: 'Invalid date', invalid_lunar: 'This lunar date does not exist (check leap month / day 30)',
    ch_chinh_tinh: 'Major Stars', ch_can_tinh: 'Stem Stars', ch_chi_tinh: 'Branch Stars', ch_sao_khac: 'Other Stars', ch_cac_cung: 'The Palaces',
    ch_72_cach: '72 Patterns', ch_an_sao: 'Placing the Stars', ch_han: 'Periods', ch_la_so_nam: 'Male Charts', ch_la_so_nu: 'Female Charts', ch_tu_dien: 'Tử Vi Dictionary',
    ch_2_chinh_tinh: 'Major Star Pairs', ch_cac_bo: 'Star Groups',
    p_menh: 'Life', p_phumau: 'Parents', p_phucduc: 'Fortune', p_dientrach: 'Property', p_quanloc: 'Career', p_nobOc: 'Friends',
    p_thiendi: 'Travel', p_tatach: 'Health', p_taibach: 'Wealth', p_tutuc: 'Children', p_phuthe: 'Spouse', p_huynhde: 'Siblings',
    tab_calendar: 'Calendar', cal_leap: 'leap', cal_wd: 'Mon,Tue,Wed,Thu,Fri,Sat,Sun', cal_wd_full: 'Monday,Tuesday,Wednesday,Thursday,Friday,Saturday,Sunday',
    cal_month_title: '{m}/{y}', cal_today: 'Today', cal_tz: 'Vietnam time (GMT+7)', cal_hd: 'Auspicious', cal_hkd: 'Inauspicious',
    cal_events: 'Holidays', cal_convert: 'Solar ↔ lunar converter', cal_s2l: 'Solar → Lunar', cal_l2s: 'Lunar → Solar', cal_60: 'Sexagenary cycle & Nạp Âm',
    cal_60_d: 'The 60 stem-branch years, their Nạp Âm element and matching birth years (as in the original chart builder). Tap a row to see the age.',
    cal_find_year: 'Look up year', cal_napam: 'Nạp Âm', cal_years: 'Birth years', cal_tiet: 'Solar term', cal_tiet_start: 'term begins',
    cal_daytype: 'Day', cal_truc: 'Officer', cal_hour0: 'First hour (Tí)', cal_good_hours: 'Auspicious hours', cal_bad_hours: 'Inauspicious hours',
    cal_build_chart: 'Build a chart for this day', cal_open_day: 'Show this day', cal_age: 'Age (lunar)',
    s_paper: 'Chart paper background', s_paper_d: 'Palace background colours as in the original Bửu Đình software.', paper_none: 'Follow theme', paper_vangkyxao: 'Antique gold',
    paper_vangchanh: 'Lemon yellow', paper_xanhnhat: 'Light blue', paper_lucnhat: 'Light green', paper_timnhat: 'Light violet', paper_vang: 'Gold',
    ab_open: 'About the app', ab_title: 'About the app', ab_updates: 'Updates', ab_auto_check: 'Check for updates automatically at start-up',
    ab_auto_install: 'Install automatically after download', ab_check: 'Check for updates', ab_checking: 'Checking for updates…', ab_last_check: 'Last checked',
    ab_latest: 'You are on the latest version ({v}).', ab_new: 'New version available: {v}', ab_update_fail: 'Could not check for updates (check your connection).',
    ab_releases: 'Releases page', ab_whats_new: 'What\'s new', ab_download: 'Download and install', ab_downloading: 'Downloading {p}%',
    ab_dl_done: 'Download complete.', ab_dl_fail: 'Download failed', ab_install: 'Install now', ab_install_fail: 'Could not start the installer',
    ab_no_asset: 'This release has no file for your device yet. Please open the Releases page.',
    ab_install_note_android: 'Android will ask for confirmation. If asked, allow Applied Astrology to install unknown apps, then tap Install again.',
    ab_install_note_win: 'The app will close, install the new version and reopen.', ab_need_perm: 'Allow "Install unknown apps" for Applied Astrology, then tap Install again.',
    ab_diag: 'Diagnostics', ab_logs: 'App logs', ab_logs_on: 'Record app logs', ab_logs_view: 'View logs', ab_logs_share: 'Export / share logs',
    ab_logs_clear: 'Clear logs', ab_errors: 'Error reports', ab_errors_d: 'Interface (JS) errors and native crashes recorded on this device.', ab_errors_none: 'No errors recorded.',
    ab_errors_clear: 'Clear error reports', ab_report: 'Report a bug', ab_report_d: 'Opens GitHub Issues with device details pre-filled; review before submitting.',
    ab_copy_debug: 'Copy debug info', ab_copied: 'Copied', ab_licenses: 'Open-source licences', ab_links: 'Links'
  };

  var PALACE_KEY = { 'Mệnh': 'p_menh', 'Phụ Mẫu': 'p_phumau', 'Phúc Đức': 'p_phucduc', 'Điền Trạch': 'p_dientrach', 'Quan Lộc': 'p_quanloc',
    'Nô Bộc': 'p_nobOc', 'Thiên Di': 'p_thiendi', 'Tật Ách': 'p_tatach', 'Tài Bạch': 'p_taibach', 'Tử Tức': 'p_tutuc', 'Phu Thê': 'p_phuthe', 'Huynh Đệ': 'p_huynhde' };

  var current = 'vi', listeners = [];
  function t(key, vars) {
    var s = (D[current] && D[current][key]) || D.en[key] || D.vi[key] || key;
    if (vars) for (var k in vars) if (vars.hasOwnProperty(k)) s = s.split('{' + k + '}').join(vars[k]);
    return s;
  }
  function apply(rootEl) {
    var els = (rootEl || document).querySelectorAll('[data-i18n]');
    for (var i = 0; i < els.length; i++) els[i].textContent = t(els[i].getAttribute('data-i18n'));
    els = (rootEl || document).querySelectorAll('[data-i18n-ph]');
    for (i = 0; i < els.length; i++) els[i].setAttribute('placeholder', t(els[i].getAttribute('data-i18n-ph')));
    els = (rootEl || document).querySelectorAll('[data-i18n-title]');
    for (i = 0; i < els.length; i++) els[i].setAttribute('title', t(els[i].getAttribute('data-i18n-title')));
  }
  function isRtl(code) { for (var i = 0; i < LANGS.length; i++) if (LANGS[i][0] === code) return !!LANGS[i][2]; return false; }
  function setLang(code, cb) {
    function done() {
      current = D[code] ? code : 'en';
      document.documentElement.setAttribute('lang', current);
      document.documentElement.setAttribute('dir', isRtl(current) ? 'rtl' : 'ltr');
      apply();
      for (var i = 0; i < listeners.length; i++) listeners[i](current);
      if (cb) cb(current);
    }
    if (D[code]) { done(); return; }
    var s = document.createElement('script');
    s.src = 'data/lang/' + code + '.js';
    s.onload = done;
    s.onerror = function () { code = 'en'; done(); };
    document.getElementsByTagName('head')[0].appendChild(s);
  }
  root.AAI18n = {
    LANGS: LANGS, t: t, apply: apply, setLang: setLang, onChange: function (fn) { listeners.push(fn); },
    current: function () { return current; },
    register: function (code, dict) { D[code] = dict; },
    palace: function (vn) { return PALACE_KEY[vn] ? t(PALACE_KEY[vn]) : vn; },
    dict: D, keys: function () { return Object.keys(D.vi); }
  };
  root.t = t;
})(window);
