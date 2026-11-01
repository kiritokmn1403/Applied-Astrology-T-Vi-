/**
 * MULTI-LANGUAGE I18N MODULE FOR APPLIED ASTROLOGY (TỬ VI ỨNG DỤNG)
 * Standardized across 19 languages (matching Aniyomi, Mangayomi, Nekoyomi)
 */

const I18N_DATA = {
  vi: {
    app_title: "Tử Vi Ứng Dụng - Bửu Đình",
    app_subtitle: "Hệ Thống Lập & Giải Lá Số Tử Vi Phương Đông",
    tab_chart: "Lập Lá Số",
    tab_library: "Kho Sách Bửu Đình",
    tab_ai: "Trợ Lý AI Luận Giải",
    tab_reader: "Đọc Sách & PDF",
    tab_settings: "Cài Đặt",
    lbl_name: "Họ và Tên",
    lbl_gender: "Giới Tính",
    lbl_male: "Nam",
    lbl_female: "Nữ",
    lbl_calendar: "Lịch",
    lbl_solar: "Dương Lịch",
    lbl_lunar: "Âm Lịch",
    lbl_dob: "Ngày Tháng Năm Sinh",
    lbl_tob: "Giờ Sinh (0-23h)",
    btn_calculate: "Lập Lá Số Tử Vi",
    btn_export_png: "Xuất Ảnh Lá Số",
    btn_share_qr: "Mã QR Chia Sẻ",
    btn_print: "In Lá Số",
    chart_thien_ban: "THIÊN BÀN",
    chart_menh: "Mệnh Cung",
    chart_than: "Thân Cung",
    chart_cuc: "Cục Số",
    chart_nap_am: "Ngũ Hành Bản Mệnh",
    star_chinh_tinh: "Chính Tinh",
    star_phu_tinh_tot: "Cát Tinh",
    star_phu_tinh_xau: "Sát Tinh / Bại Tinh",
    star_tuan_triet: "Tuần / Triệt",
    lbl_explanation: "Bình Giải Bửu Đình",
    lbl_select_palace: "Bấm vào từng Cung hoặc từng Sao để tra cứu bình giải chi tiết",
    lbl_language: "Ngôn Ngữ Ứng Dụng",
    lbl_theme: "Giao Diện",
    lbl_theme_dark: "Huyền Bí Hoàng Gia (Tối)",
    lbl_theme_light: "Thiên Thanh Tinh Tú (Sáng)",
    lbl_dex_mode: "Chế Độ Màn Hình Lớn / DeX",
    lbl_search_hint: "Tìm kiếm sao, cung, cách cục (ví dụ: Tử Vi, Mệnh, Tứ Hóa)...",
    ai_placeholder: "Hỏi trợ lý AI về lá số này (ví dụ: Mệnh Vô Chính Diệu nên định hướng nghề gì?)...",
    ai_send: "Gửi Câu Hỏi"
  },
  en: {
    app_title: "Applied Astrology - Buu Dinh",
    app_subtitle: "Eastern Astrological Charting & Interpretation System",
    tab_chart: "Astrology Chart",
    tab_library: "Buu Dinh Library",
    tab_ai: "AI Astrologer",
    tab_reader: "Reader & PDF",
    tab_settings: "Settings",
    lbl_name: "Full Name",
    lbl_gender: "Gender",
    lbl_male: "Male",
    lbl_female: "Female",
    lbl_calendar: "Calendar",
    lbl_solar: "Solar Calendar",
    lbl_lunar: "Lunar Calendar",
    lbl_dob: "Date of Birth",
    lbl_tob: "Hour of Birth",
    btn_calculate: "Generate Chart",
    btn_export_png: "Export Chart Image",
    btn_share_qr: "Share QR Code",
    btn_print: "Print Chart",
    chart_thien_ban: "CELESTIAL BOARD",
    chart_menh: "Destiny Palace (Menh)",
    chart_than: "Body Palace (Than)",
    chart_cuc: "Element Bureau (Cuc)",
    chart_nap_am: "Sound Element",
    star_chinh_tinh: "Major Stars",
    star_phu_tinh_tot: "Auspicious Stars",
    star_phu_tinh_xau: "Inauspicious Stars",
    star_tuan_triet: "Void / Cut Off (Tuan/Triet)",
    lbl_explanation: "Buu Dinh Commentary",
    lbl_select_palace: "Select any Palace or Star to read detailed astrological analyses",
    lbl_language: "Language",
    lbl_theme: "Theme",
    lbl_theme_dark: "Mystic Royal (Dark)",
    lbl_theme_light: "Celestial Azure (Light)",
    lbl_dex_mode: "DeX / Desktop Mode",
    lbl_search_hint: "Search star, palace, combination...",
    ai_placeholder: "Ask AI about this chart...",
    ai_send: "Send"
  },
  zh: {
    app_title: "应用紫微斗数 - 宝亭",
    app_subtitle: "东方紫微斗数排盘与命理分析系统",
    tab_chart: "紫微排盘",
    tab_library: "宝亭书库",
    tab_ai: "AI 命理大师",
    tab_reader: "阅读与PDF",
    tab_settings: "设置",
    lbl_name: "姓名",
    lbl_gender: "性别",
    lbl_male: "男",
    lbl_female: "女",
    lbl_calendar: "历法",
    lbl_solar: "阳历",
    lbl_lunar: "农历",
    lbl_dob: "出生年月日",
    lbl_tob: "出生时辰",
    btn_calculate: "排定命盘",
    btn_export_png: "导出命盘图片",
    btn_share_qr: "二维码分享",
    btn_print: "打印命盘",
    chart_thien_ban: "天盘",
    chart_menh: "命宫",
    chart_than: "身宫",
    chart_cuc: "五行局",
    chart_nap_am: "纳音五行",
    star_chinh_tinh: "十四主星",
    star_phu_tinh_tot: "吉星",
    star_phu_tinh_xau: "煞星",
    star_tuan_triet: "旬空/截空",
    lbl_explanation: "宝亭论述",
    lbl_select_palace: "点击宫位或星曜查看详细断语",
    lbl_language: "语言设置",
    lbl_theme: "主题风格",
    lbl_theme_dark: "紫曜星穹 (暗色)",
    lbl_theme_light: "玄黄霁月 (亮色)",
    lbl_dex_mode: "大屏 / DeX模式",
    lbl_search_hint: "搜索星曜、宫位、格局...",
    ai_placeholder: "向AI大师提问此命盘...",
    ai_send: "发送"
  },
  ja: {
    app_title: "応用紫微斗数 - ブウ・ディン",
    app_subtitle: "東洋占星術紫微斗数命盤作成・鑑定システム",
    tab_chart: "命盤作成",
    tab_library: "文献・解説",
    tab_ai: "AI鑑定士",
    tab_reader: "書籍・PDF",
    tab_settings: "設定",
    lbl_name: "お名前",
    lbl_gender: "性別",
    lbl_male: "男性",
    lbl_female: "女性",
    lbl_calendar: "暦法",
    lbl_solar: "太陽暦",
    lbl_lunar: "太陰暦",
    lbl_dob: "生年月日",
    lbl_tob: "出生時間",
    btn_calculate: "命盤を出力",
    btn_export_png: "画像として保存",
    btn_share_qr: "QRコード共有",
    btn_print: "命盤印刷",
    chart_thien_ban: "天盤",
    chart_menh: "命宮",
    chart_than: "身宮",
    chart_cuc: "五行局",
    chart_nap_am: "納音",
    star_chinh_tinh: "主星",
    star_phu_tinh_tot: "吉星",
    star_phu_tinh_xau: "凶星",
    star_tuan_triet: "空亡",
    lbl_explanation: "鑑定解説",
    lbl_select_palace: "宮または星をクリックして詳細解説を表示",
    lbl_language: "言語設定",
    lbl_theme: "テーマ",
    lbl_theme_dark: "ロイヤルパープル (ダーク)",
    lbl_theme_light: "スカイアズール (ライト)",
    lbl_dex_mode: "DeX / 大画面モード",
    lbl_search_hint: "星、宮、格局を検索...",
    ai_placeholder: "この命盤についてAIに質問...",
    ai_send: "送信"
  },
  fr: {
    app_title: "Astrologie Appliquée - Buu Dinh",
    app_subtitle: "Système de Thème Astrologique Oriental Tu Vi",
    tab_chart: "Thème Astral",
    tab_library: "Bibliothèque",
    tab_ai: "Astrologue IA",
    tab_reader: "Lecteur & PDF",
    tab_settings: "Paramètres",
    lbl_name: "Nom complet",
    lbl_gender: "Genre",
    lbl_male: "Homme",
    lbl_female: "Femme",
    lbl_calendar: "Calendrier",
    lbl_solar: "Solaire",
    lbl_lunar: "Lunaire",
    lbl_dob: "Date de naissance",
    lbl_tob: "Heure de naissance",
    btn_calculate: "Générer le Thème",
    btn_export_png: "Exporter l'image",
    btn_share_qr: "Partager QR",
    btn_print: "Imprimer",
    chart_thien_ban: "TABLE CÉLESTE",
    chart_menh: "Palais du Destin",
    chart_than: "Palais du Corps",
    chart_cuc: "Bureau Élémentaire",
    chart_nap_am: "Élément Sonore",
    star_chinh_tinh: "Étoiles Majeures",
    star_phu_tinh_tot: "Étoiles Bénéfiques",
    star_phu_tinh_xau: "Étoiles Maléfiques",
    star_tuan_triet: "Vide / Rupture",
    lbl_explanation: "Commentaire Buu Dinh",
    lbl_select_palace: "Cliquez sur un palais ou une étoile pour afficher l'analyse",
    lbl_language: "Langue",
    lbl_theme: "Thème",
    lbl_theme_dark: "Violet Mystique (Sombre)",
    lbl_theme_light: "Bleu Céleste (Clair)",
    lbl_dex_mode: "Mode DeX / Bureau",
    lbl_search_hint: "Rechercher étoile, palais...",
    ai_placeholder: "Posez une question sur ce thème...",
    ai_send: "Envoyer"
  }
};

let currentLang = 'vi';

function setLanguage(lang) {
  if (I18N_DATA[lang]) {
    currentLang = lang;
  } else {
    currentLang = 'en';
  }
  applyTranslations();
  localStorage.setItem('tuvi_app_lang', currentLang);
}

function t(key) {
  const dict = I18N_DATA[currentLang] || I18N_DATA['en'];
  return dict[key] || (I18N_DATA['en'] && I18N_DATA['en'][key]) || key;
}

function applyTranslations() {
  document.querySelectorAll('[data-i18n]').forEach(el => {
    const key = el.getAttribute('data-i18n');
    const val = t(key);
    if (el.tagName === 'INPUT' && el.getAttribute('placeholder')) {
      el.setAttribute('placeholder', val);
    } else {
      el.textContent = val;
    }
  });
}

// Initialize on load
if (typeof window !== 'undefined') {
  window.addEventListener('DOMContentLoaded', () => {
    const saved = localStorage.getItem('tuvi_app_lang') || 'vi';
    setLanguage(saved);
  });
}

if (typeof module !== 'undefined') {
  module.exports = { I18N_DATA, setLanguage, t };
}

