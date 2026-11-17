#!/usr/bin/env python3
# Applied Astrology v0.0.5 – 4 khóa mới (công tắc Liquid Glass) × 68 ngôn ngữ -> tools/i18n_v5/<mã>.json
# Thứ tự: lg_enable, lg_enable_d, lg_enabled, lg_disabled
import json, os
KEYS = ["lg_enable", "lg_enable_d", "lg_enabled", "lg_disabled"]
T = {
"vi": ["Bật Liquid Glass", "Tắt để dùng giao diện phẳng, nhẹ nhất (khuyên dùng cho máy cấu hình thấp).", "Đã bật Liquid Glass", "Đã tắt Liquid Glass"],
"en": ["Enable Liquid Glass", "Turn off for a flat, lightest interface (recommended on low-end devices).", "Liquid Glass on", "Liquid Glass off"],
"am": ["Liquid Glassን አንቃ", "ጠፍጣፋና በጣም ቀላል በይነገጽ ለመጠቀም ያጥፉት (ለአነስተኛ አቅም መሣሪያዎች ይመከራል)።", "Liquid Glass በርቷል", "Liquid Glass ጠፍቷል"],
"ar": ["تفعيل Liquid Glass", "أوقفه لواجهة مسطحة وأخف ما يمكن (موصى به للأجهزة الضعيفة).", "تم تفعيل Liquid Glass", "تم إيقاف Liquid Glass"],
"as": ["Liquid Glass অন কৰক", "সমতল আৰু আটাইতকৈ পাতল ইন্টাৰফেচৰ বাবে বন্ধ কৰক (কম ক্ষমতাৰ ডিভাইচৰ বাবে পৰামৰ্শিত)।", "Liquid Glass অন কৰা হ'ল", "Liquid Glass বন্ধ কৰা হ'ল"],
"be": ["Уключыць Liquid Glass", "Выключыце для плоскага, найлягчэйшага інтэрфейсу (рэкамендуецца для слабых прылад).", "Liquid Glass уключаны", "Liquid Glass выключаны"],
"bg": ["Включване на Liquid Glass", "Изключете за плосък и най-лек интерфейс (препоръчително за по-слаби устройства).", "Liquid Glass е включен", "Liquid Glass е изключен"],
"bn": ["Liquid Glass চালু করুন", "সমতল ও সবচেয়ে হালকা ইন্টারফেসের জন্য বন্ধ করুন (কম ক্ষমতার ডিভাইসের জন্য প্রস্তাবিত)।", "Liquid Glass চালু হয়েছে", "Liquid Glass বন্ধ হয়েছে"],
"ca": ["Activa Liquid Glass", "Desactiva-ho per a una interfície plana i més lleugera (recomanat en dispositius modestos).", "Liquid Glass activat", "Liquid Glass desactivat"],
"ceb": ["I-on ang Liquid Glass", "I-off para sa patag ug labing gaan nga interface (girekomenda sa mga hinay nga device).", "Na-on ang Liquid Glass", "Na-off ang Liquid Glass"],
"cs": ["Zapnout Liquid Glass", "Vypněte pro ploché a nejlehčí rozhraní (doporučeno pro slabší zařízení).", "Liquid Glass zapnuto", "Liquid Glass vypnuto"],
"cv": ["Liquid Glass ҫут", "Тикӗс тата чи ҫӑмӑл интерфейс валли сӳнтерӗр (вӑйсӑр хатӗрсем валли сӗнетпӗр).", "Liquid Glass ҫутӑ", "Liquid Glass сӳнтернӗ"],
"da": ["Slå Liquid Glass til", "Slå fra for en flad og lettest mulig brugerflade (anbefales på svagere enheder).", "Liquid Glass slået til", "Liquid Glass slået fra"],
"de": ["Liquid Glass aktivieren", "Ausschalten für eine flache, besonders leichte Oberfläche (empfohlen für leistungsschwache Geräte).", "Liquid Glass eingeschaltet", "Liquid Glass ausgeschaltet"],
"el": ["Ενεργοποίηση Liquid Glass", "Απενεργοποιήστε για επίπεδη και ελαφρύτερη διεπαφή (προτείνεται για πιο αδύναμες συσκευές).", "Το Liquid Glass ενεργοποιήθηκε", "Το Liquid Glass απενεργοποιήθηκε"],
"eo": ["Ŝalti Liquid Glass", "Malŝaltu por plata kaj plej malpeza interfaco (rekomendata por malfortaj aparatoj).", "Liquid Glass ŝaltita", "Liquid Glass malŝaltita"],
"es": ["Activar Liquid Glass", "Desactívalo para una interfaz plana y más ligera (recomendado en dispositivos modestos).", "Liquid Glass activado", "Liquid Glass desactivado"],
"es-419": ["Activar Liquid Glass", "Desactívalo para una interfaz plana y más ligera (recomendado en equipos de gama baja).", "Liquid Glass activado", "Liquid Glass desactivado"],
"eu": ["Aktibatu Liquid Glass", "Desaktibatu interfaze lau eta arinena izateko (gailu apaletan gomendatua).", "Liquid Glass aktibatuta", "Liquid Glass desaktibatuta"],
"fa": ["روشن کردن Liquid Glass", "برای رابطی تخت و سبک‌ترین حالت خاموش کنید (برای دستگاه‌های ضعیف توصیه می‌شود).", "Liquid Glass روشن شد", "Liquid Glass خاموش شد"],
"fi": ["Ota Liquid Glass käyttöön", "Poista käytöstä saadaksesi tasaisen ja kevyimmän käyttöliittymän (suositellaan heikommille laitteille).", "Liquid Glass päällä", "Liquid Glass pois päältä"],
"fil": ["I-on ang Liquid Glass", "I-off para sa patag at pinakamagaang interface (inirerekomenda sa mga mahinang device).", "Naka-on ang Liquid Glass", "Naka-off ang Liquid Glass"],
"fr": ["Activer Liquid Glass", "Désactivez pour une interface plate et plus légère (recommandé sur les appareils modestes).", "Liquid Glass activé", "Liquid Glass désactivé"],
"gl": ["Activar Liquid Glass", "Desactívao para unha interface plana e máis lixeira (recomendado en dispositivos modestos).", "Liquid Glass activado", "Liquid Glass desactivado"],
"he": ["הפעלת Liquid Glass", "כבו לממשק שטוח וקל ביותר (מומלץ במכשירים חלשים).", "Liquid Glass הופעל", "Liquid Glass כובה"],
"hi": ["Liquid Glass चालू करें", "सपाट और सबसे हल्के इंटरफ़ेस के लिए बंद करें (कम क्षमता वाले डिवाइस के लिए अनुशंसित)।", "Liquid Glass चालू", "Liquid Glass बंद"],
"hr": ["Uključi Liquid Glass", "Isključite za ravno i najlakše sučelje (preporučeno za slabije uređaje).", "Liquid Glass uključen", "Liquid Glass isključen"],
"hu": ["Liquid Glass bekapcsolása", "Kapcsolja ki a sík, legkönnyebb felülethez (gyengébb eszközökön ajánlott).", "Liquid Glass bekapcsolva", "Liquid Glass kikapcsolva"],
"id": ["Aktifkan Liquid Glass", "Matikan untuk antarmuka datar dan paling ringan (disarankan untuk perangkat kelas bawah).", "Liquid Glass aktif", "Liquid Glass nonaktif"],
"it": ["Attiva Liquid Glass", "Disattiva per un'interfaccia piatta e più leggera (consigliato sui dispositivi meno potenti).", "Liquid Glass attivato", "Liquid Glass disattivato"],
"ja": ["Liquid Glass を有効にする", "オフにするとフラットで最も軽い画面になります（低スペック端末におすすめ）。", "Liquid Glass をオンにしました", "Liquid Glass をオフにしました"],
"jv": ["Uripaké Liquid Glass", "Patèni kanggo antarmuka rata lan paling entheng (disaranaké kanggo piranti sing ringkih).", "Liquid Glass urip", "Liquid Glass mati"],
"ka": ["Liquid Glass-ის ჩართვა", "გამორთეთ ბრტყელი და ყველაზე მსუბუქი ინტერფეისისთვის (რეკომენდებულია სუსტი მოწყობილობებისთვის).", "Liquid Glass ჩართულია", "Liquid Glass გამორთულია"],
"kk": ["Liquid Glass қосу", "Тегіс әрі ең жеңіл интерфейс үшін өшіріңіз (әлсіз құрылғыларға ұсынылады).", "Liquid Glass қосулы", "Liquid Glass өшірулі"],
"km": ["បើក Liquid Glass", "បិទដើម្បីប្រើផ្ទៃរាបស្មើ និងស្រាលបំផុត (ណែនាំសម្រាប់ឧបករណ៍កម្លាំងខ្សោយ)។", "បានបើក Liquid Glass", "បានបិទ Liquid Glass"],
"kn": ["Liquid Glass ಆನ್ ಮಾಡಿ", "ಸಮತಟ್ಟಾದ ಮತ್ತು ಅತ್ಯಂತ ಹಗುರ ಇಂಟರ್ಫೇಸ್‌ಗಾಗಿ ಆಫ್ ಮಾಡಿ (ಕಡಿಮೆ ಸಾಮರ್ಥ್ಯದ ಸಾಧನಗಳಿಗೆ ಶಿಫಾರಸು).", "Liquid Glass ಆನ್ ಆಗಿದೆ", "Liquid Glass ಆಫ್ ಆಗಿದೆ"],
"ko": ["Liquid Glass 사용", "끄면 평면적이고 가장 가벼운 화면을 사용합니다(저사양 기기에 권장).", "Liquid Glass 켜짐", "Liquid Glass 꺼짐"],
"lt": ["Įjungti Liquid Glass", "Išjunkite, jei norite plokščios ir lengviausios sąsajos (rekomenduojama silpnesniems įrenginiams).", "Liquid Glass įjungtas", "Liquid Glass išjungtas"],
"lv": ["Ieslēgt Liquid Glass", "Izslēdziet plakanai un vieglākajai saskarnei (ieteicams vājākām ierīcēm).", "Liquid Glass ieslēgts", "Liquid Glass izslēgts"],
"ml": ["Liquid Glass ഓൺ ചെയ്യുക", "പരന്നതും ഏറ്റവും ഭാരം കുറഞ്ഞതുമായ ഇന്റർഫേസിനായി ഓഫ് ചെയ്യുക (ശേഷി കുറഞ്ഞ ഉപകരണങ്ങൾക്ക് ശുപാർശ ചെയ്യുന്നു).", "Liquid Glass ഓണാണ്", "Liquid Glass ഓഫാണ്"],
"mr": ["Liquid Glass सुरू करा", "सपाट आणि सर्वात हलक्या इंटरफेससाठी बंद करा (कमी क्षमतेच्या उपकरणांसाठी शिफारस).", "Liquid Glass सुरू केले", "Liquid Glass बंद केले"],
"ms": ["Hidupkan Liquid Glass", "Matikan untuk antara muka rata dan paling ringan (disyorkan untuk peranti rendah).", "Liquid Glass dihidupkan", "Liquid Glass dimatikan"],
"my": ["Liquid Glass ဖွင့်ရန်", "ပြားပြီး အပေါ့ဆုံး မျက်နှာပြင်အတွက် ပိတ်ပါ (စွမ်းဆောင်ရည်နိမ့် စက်များအတွက် အကြံပြုသည်)။", "Liquid Glass ဖွင့်ပြီး", "Liquid Glass ပိတ်ပြီး"],
"nb": ["Slå på Liquid Glass", "Slå av for et flatt og lettest mulig grensesnitt (anbefales på svakere enheter).", "Liquid Glass slått på", "Liquid Glass slått av"],
"ne": ["Liquid Glass सक्रिय गर्नुहोस्", "समतल र सबैभन्दा हल्का इन्टरफेसका लागि बन्द गर्नुहोस् (कम क्षमताका उपकरणका लागि सिफारिस गरिन्छ)।", "Liquid Glass सक्रिय भयो", "Liquid Glass बन्द भयो"],
"nl": ["Liquid Glass inschakelen", "Schakel uit voor een vlakke, zo licht mogelijke interface (aanbevolen op minder krachtige apparaten).", "Liquid Glass ingeschakeld", "Liquid Glass uitgeschakeld"],
"nn": ["Slå på Liquid Glass", "Slå av for eit flatt og lettast mogleg grensesnitt (tilrådd på svakare einingar).", "Liquid Glass slått på", "Liquid Glass slått av"],
"pl": ["Włącz Liquid Glass", "Wyłącz, aby uzyskać płaski i najlżejszy interfejs (zalecane na słabszych urządzeniach).", "Liquid Glass włączony", "Liquid Glass wyłączony"],
"pt": ["Ativar Liquid Glass", "Desative para uma interface plana e mais leve (recomendado em dispositivos modestos).", "Liquid Glass ativado", "Liquid Glass desativado"],
"pt-BR": ["Ativar Liquid Glass", "Desative para uma interface plana e mais leve (recomendado em aparelhos mais simples).", "Liquid Glass ativado", "Liquid Glass desativado"],
"ro": ["Activează Liquid Glass", "Dezactivează pentru o interfață plată și cât mai ușoară (recomandat pe dispozitive slabe).", "Liquid Glass activat", "Liquid Glass dezactivat"],
"ru": ["Включить Liquid Glass", "Отключите для плоского и самого лёгкого интерфейса (рекомендуется для слабых устройств).", "Liquid Glass включён", "Liquid Glass выключен"],
"sa": ["Liquid Glass सक्रियं कुरु", "समतलाय लघुतमाय च अन्तरापृष्ठाय निष्क्रियं कुरु (अल्पशक्तियन्त्रेभ्यः अनुशंसितम्)।", "Liquid Glass सक्रियम्", "Liquid Glass निष्क्रियम्"],
"sah": ["Liquid Glass холбоо", "Тэп-тэкс уонна саамай чэпчэки интерфейс туһугар араарыҥ (күүһэ суох тэриллэргэ сүбэлэнэр).", "Liquid Glass холбонно", "Liquid Glass араарылынна"],
"sc": ["Ativa Liquid Glass", "Disativa pro un'interfàtzia prana e prus lèbia (cussigiadu in is dispositivos prus dèbiles).", "Liquid Glass ativadu", "Liquid Glass disativadu"],
"sdh": ["Liquid Glass چالاک بکە", "بۆ ڕووکارێکی تەخت و سووکترین بیکوژێنەوە (بۆ ئامێرە لاوازەکان پێشنیار دەکرێت).", "Liquid Glass چالاک کرا", "Liquid Glass کوژایەوە"],
"sk": ["Zapnúť Liquid Glass", "Vypnite pre ploché a najľahšie rozhranie (odporúča sa pre slabšie zariadenia).", "Liquid Glass zapnutý", "Liquid Glass vypnutý"],
"sq": ["Aktivizo Liquid Glass", "Çaktivizoje për një ndërfaqe të sheshtë dhe më të lehtë (rekomandohet për pajisje më të dobëta).", "Liquid Glass u aktivizua", "Liquid Glass u çaktivizua"],
"sr": ["Укључи Liquid Glass", "Искључите за раван и најлакши интерфејс (препоручено за слабије уређаје).", "Liquid Glass укључен", "Liquid Glass искључен"],
"sv": ["Slå på Liquid Glass", "Stäng av för ett platt och lättast möjligt gränssnitt (rekommenderas på svagare enheter).", "Liquid Glass på", "Liquid Glass av"],
"ta": ["Liquid Glass-ஐ இயக்கு", "தட்டையான, மிக இலகுவான இடைமுகத்திற்கு அணைக்கவும் (குறைந்த திறன் கொண்ட சாதனங்களுக்குப் பரிந்துரைக்கப்படுகிறது).", "Liquid Glass இயக்கப்பட்டது", "Liquid Glass அணைக்கப்பட்டது"],
"te": ["Liquid Glass ఆన్ చేయండి", "సమతలమైన, అత్యంత తేలికైన ఇంటర్‌ఫేస్ కోసం ఆఫ్ చేయండి (తక్కువ సామర్థ్య పరికరాలకు సిఫార్సు చేయబడింది).", "Liquid Glass ఆన్ అయింది", "Liquid Glass ఆఫ్ అయింది"],
"th": ["เปิด Liquid Glass", "ปิดเพื่อใช้หน้าจอแบบเรียบและเบาที่สุด (แนะนำสำหรับอุปกรณ์สเปกต่ำ)", "เปิด Liquid Glass แล้ว", "ปิด Liquid Glass แล้ว"],
"tr": ["Liquid Glass'ı aç", "Düz ve en hafif arayüz için kapatın (düşük donanımlı cihazlarda önerilir).", "Liquid Glass açıldı", "Liquid Glass kapatıldı"],
"uk": ["Увімкнути Liquid Glass", "Вимкніть для плаского й найлегшого інтерфейсу (рекомендовано для слабких пристроїв).", "Liquid Glass увімкнено", "Liquid Glass вимкнено"],
"uz": ["Liquid Glass’ni yoqish", "Tekis va eng yengil interfeys uchun oʻchiring (kuchsiz qurilmalar uchun tavsiya etiladi).", "Liquid Glass yoqildi", "Liquid Glass oʻchirildi"],
"zh-CN": ["启用 Liquid Glass", "关闭后使用扁平、最轻量的界面（推荐低配设备使用）。", "已开启 Liquid Glass", "已关闭 Liquid Glass"],
"zh-TW": ["啟用 Liquid Glass", "關閉後使用扁平、最輕量的介面（建議低階裝置使用）。", "已開啟 Liquid Glass", "已關閉 Liquid Glass"],
}
here = os.path.dirname(os.path.abspath(__file__))
v4 = sorted(f[:-5] for f in os.listdir(os.path.join(here, "..", "i18n_v4")) if f.endswith(".json"))
missing = [c for c in v4 if c not in T]
assert not missing, "thiếu ngôn ngữ: %s" % missing
for code, vals in T.items():
    assert len(vals) == len(KEYS), code
    with open(os.path.join(here, code + ".json"), "w", encoding="utf-8") as f:
        json.dump(dict(zip(KEYS, vals)), f, ensure_ascii=False, indent=0)
print("i18n_v5: %d ngôn ngữ × %d khóa" % (len(T), len(KEYS)))
