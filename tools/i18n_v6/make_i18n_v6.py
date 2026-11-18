#!/usr/bin/env python3
# Applied Astrology v0.0.6 – 1 khóa mới × 68 ngôn ngữ -> tools/i18n_v6/<mã>.json
# show_more: nút "Xem thêm" của danh sách kết quả tìm kiếm phân trang; {n} = số kết quả còn lại
import json, os
KEYS = ["show_more"]
T = {
"vi": "Xem thêm ({n})", "en": "Show more ({n})", "am": "ተጨማሪ አሳይ ({n})", "ar": "عرض المزيد ({n})",
"as": "অধিক দেখুৱাওক ({n})", "be": "Паказаць больш ({n})", "bg": "Покажи още ({n})", "bn": "আরও দেখান ({n})",
"ca": "Mostra'n més ({n})", "ceb": "Ipakita ang dugang ({n})", "cs": "Zobrazit další ({n})", "cv": "Тата кӑтарт ({n})",
"da": "Vis flere ({n})", "de": "Mehr anzeigen ({n})", "el": "Εμφάνιση περισσότερων ({n})", "eo": "Montri pli ({n})",
"es": "Mostrar más ({n})", "es-419": "Mostrar más ({n})", "eu": "Erakutsi gehiago ({n})", "fa": "نمایش بیشتر ({n})",
"fi": "Näytä lisää ({n})", "fil": "Magpakita pa ({n})", "fr": "Afficher plus ({n})", "gl": "Amosar máis ({n})",
"he": "הצג עוד ({n})", "hi": "और दिखाएँ ({n})", "hr": "Prikaži više ({n})", "hu": "Továbbiak ({n})",
"id": "Tampilkan lainnya ({n})", "it": "Mostra altri ({n})", "ja": "さらに表示（{n}）", "jv": "Tampilaké liyané ({n})",
"ka": "მეტის ჩვენება ({n})", "kk": "Тағы көрсету ({n})", "km": "បង្ហាញបន្ថែម ({n})", "kn": "ಇನ್ನಷ್ಟು ತೋರಿಸಿ ({n})",
"ko": "더 보기 ({n})", "lt": "Rodyti daugiau ({n})", "lv": "Rādīt vairāk ({n})", "ml": "കൂടുതൽ കാണിക്കുക ({n})",
"mr": "आणखी दाखवा ({n})", "ms": "Tunjukkan lagi ({n})", "my": "ထပ်ပြရန် ({n})", "nb": "Vis flere ({n})",
"ne": "थप देखाउनुहोस् ({n})", "nl": "Meer weergeven ({n})", "nn": "Vis fleire ({n})", "pl": "Pokaż więcej ({n})",
"pt": "Mostrar mais ({n})", "pt-BR": "Mostrar mais ({n})", "ro": "Afișează mai multe ({n})", "ru": "Показать ещё ({n})",
"sa": "अधिकं दर्शय ({n})", "sah": "Өссө көрдөр ({n})", "sc": "Mustra·nde àteros ({n})", "sdh": "زیاتر نیشان بەو ({n})",
"sk": "Zobraziť viac ({n})", "sq": "Shfaq më shumë ({n})", "sr": "Прикажи више ({n})", "sv": "Visa fler ({n})",
"ta": "மேலும் காட்டு ({n})", "te": "మరిన్ని చూపించు ({n})", "th": "แสดงเพิ่มเติม ({n})", "tr": "Daha fazla göster ({n})",
"uk": "Показати ще ({n})", "uz": "Yana koʻrsatish ({n})", "zh-CN": "显示更多（{n}）", "zh-TW": "顯示更多（{n}）",
}
here = os.path.dirname(os.path.abspath(__file__))
v5 = os.path.join(here, "..", "i18n_v5")
codes = sorted(f[:-5] for f in os.listdir(v5) if f.endswith(".json"))
missing = [c for c in codes if c not in T]
extra = [c for c in T if c not in codes]
if missing or extra: raise SystemExit("missing=%s extra=%s" % (missing, extra))
for c in codes:
    with open(os.path.join(here, c + ".json"), "w", encoding="utf-8") as f:
        json.dump({"show_more": T[c]}, f, ensure_ascii=False, indent=0)
print("i18n_v6: %d languages x %d keys" % (len(codes), len(KEYS)))
