package com.appliedastrology.tuvi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.appliedastrology.tuvi.core.Cal;
import com.appliedastrology.tuvi.core.Engine;
import com.appliedastrology.tuvi.core.I18n;
import com.appliedastrology.tuvi.core.Json;
import com.appliedastrology.tuvi.core.Library;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tự kiểm thử trong ứng dụng (am start ... --ez selftest true): mỗi bước thao
 * tác trên giao diện native thật, kiểm tra kết quả,
 * chụp màn hình (files/selftest/NN_ten.png) rồi ghi results.json – cùng giao
 * thức với build_release (bản 0.0.3).
 * v0.0.4: 44 bước = 36 bước cũ + 8 bước mới (Liquid Glass, không còn WebView,
 * đủ khóa 68 ngôn ngữ).
 * v0.0.5: 52 bước = 44 + công tắc Liquid Glass + 6 bước đo hiệu năng (cuộn, hộp
 * thoại, đổi tab, đổi cài đặt, bộ nhớ, ổn định kính) + toàn dải Âm Lịch
 * 1900–2099.
 */
public final class SelfTest {
    interface Done {
        void done();
    }

    interface Step {
        void run(Done d) throws Exception;
    }

    final MainActivity a;
    final File dir;
    final List<String> names = new ArrayList<String>();
    final List<Step> steps = new ArrayList<Step>();
    final List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    int idx = 0;
    static final int DELAY = 1700;

    SelfTest(MainActivity a) {
        this.a = a;
        dir = new File(a.getExternalFilesDir(null), "selftest");
        File[] old = dir.listFiles();
        if (old != null)
            for (File f : old)
                f.delete();
        dir.mkdirs();
        define();
    }

    void step(String n, Step s) {
        names.add(n);
        steps.add(s);
    }

    void rec(String name, boolean ok, String detail) {
        results.add(Json.map("step", name, "ok", ok, "detail", detail == null ? "" : detail));
        a.host.log("SELFTEST " + (ok ? "PASS" : "FAIL") + " " + name + " " + detail);
    }

    Screens sc() {
        return a.sc;
    }

    View find(View v, String tag) {
        if (tag.equals(String.valueOf(v.getTag())))
            return v;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View r = find(g.getChildAt(i), tag);
                if (r != null)
                    return r;
            }
        }
        return null;
    }

    int count(View v, Class<?> c) {
        int n = c.isInstance(v) ? 1 : 0;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++)
                n += count(g.getChildAt(i), c);
        }
        return n;
    }

    void click(String tag) {
        View v = find(a.root, tag);
        if (v != null)
            v.performClick();
    }

    String tabText(String view) {
        View t = find(a.nav, "tab:" + view);
        return t == null ? "" : String.valueOf(((android.widget.TextView) ((ViewGroup) t).getChildAt(1)).getText());
    }

    void set(String k, Object v) {
        a.S.put(k, v);
        a.save();
    }

    Engine.Input in(String cal, int d, int m, int y, int h, String g, boolean leap) {
        Engine.Input i = new Engine.Input();
        i.name = "Nguyễn Văn An";
        i.gender = g;
        i.calendar = cal;
        i.day = d;
        i.month = m;
        i.year = y;
        i.hourChi = h;
        i.leap = leap;
        i.viewYear = MainActivity.year();
        return i;
    }

    void define() {
        step("01_chart_build", new Step() {
            public void run(Done d) {
                a.show("chart");
                sc().form = sc().withMode(in("solar", 15, 8, 1995, 6, "Nam", false));
                a.show("chart");
                Engine.Chart c = sc().build();
                rec("01_chart_build",
                        c != null && c.lunar.day == 20 && c.lunar.month == 7 && find(a.root, "detailBox") != null,
                        c == null ? "null"
                                : "lunar=" + c.lunar.day + "/" + c.lunar.month + "/" + c.lunar.year + " cuc="
                                        + c.cucName + " menh=" + Engine.CHI[c.menh]);
                d.done();
            }
        });
        step("02_palace_detail", new Step() {
            public void run(Done d) {
                Engine.Chart c = sc().chart;
                sc().selChi = (c.menh + 4) % 12;
                a.show("chart");
                List<Library.Hit> rel = a.lib.related(c, sc().selChi);
                rec("02_palace_detail", rel.size() > 0 && find(a.root, "detailBox") != null, "related=" + rel.size());
                d.done();
            }
        });
        step("03_lunar_input", new Step() {
            public void run(Done d) {
                sc().form = sc().withMode(in("lunar", 1, 2, 2023, 3, "Nữ", true));
                a.show("chart");
                Engine.Chart c = sc().build();
                rec("03_lunar_input", c != null && c.sDay == 22 && c.sMonth == 3,
                        c == null ? "null" : "solar=" + c.sDay + "/" + c.sMonth + "/" + c.sYear);
                d.done();
            }
        });
        step("04_qr", new Step() {
            public void run(final Done d) {
                io.nayuki.qrcodegen.QrCode q = sc().showQr();
                rec("04_qr", q != null && q.size >= 21 && a.modalOpen(), q == null ? "" : "size=" + q.size);
                later(d, true);
            }
        });
        step("05_png", new Step() {
            public void run(Done d) {
                byte[] p = sc().exportPng();
                rec("05_png", p != null && p.length > 50000, "bytes=" + (p == null ? 0 : p.length));
                later(d, true);
            }
        });
        step("06_save_profile", new Step() {
            public void run(Done d) {
                int n = a.host.list("aa.saved").size();
                sc().saveProfile();
                a.show("saved");
                rec("06_save_profile", a.host.list("aa.saved").size() == n + 1,
                        "saved=" + a.host.list("aa.saved").size());
                d.done();
            }
        });
        step("07_library_home", new Step() {
            public void run(Done d) {
                sc().libMode = "home";
                a.show("library");
                View g = find(a.root, "chapters");
                rec("07_library_home", g != null && ((ViewGroup) g).getChildCount() == 13, "chapters="
                        + (g == null ? 0 : ((ViewGroup) g).getChildCount()) + " articles=" + a.lib.totalArticles());
                d.done();
            }
        });
        step("08_library_chapter", new Step() {
            public void run(Done d) {
                click("ch:an-sao");
                View l = find(a.root, "chapterList");
                rec("08_library_chapter", l != null && ((ViewGroup) l).getChildCount() > 5,
                        "items=" + (l == null ? 0 : ((ViewGroup) l).getChildCount()));
                d.done();
            }
        });
        step("09_reader", new Step() {
            public void run(Done d) {
                sc().openArticle("chinh-tinh", 0);
                View b = find(a.root, "rBody");
                int n = b == null ? 0 : ((android.widget.TextView) b).getText().length();
                click("rBm");
                rec("09_reader", n > 1000, "chars=" + n);
                d.done();
            }
        });
        step("10_search", new Step() {
            public void run(Done d) {
                long t0 = System.currentTimeMillis();
                a.show("library");
                List<Library.Hit> r = sc().search("Hóa Kỵ");
                rec("10_search", r.size() > 20, "results=" + r.size() + " native=" + !"none".equals(NativeCore.abi())
                        + " " + (System.currentTimeMillis() - t0) + "ms");
                d.done();
            }
        });
        step("11_search_noaccent", new Step() {
            public void run(Done d) {
                List<Library.Hit> r = sc().search("thien phu");
                rec("11_search_noaccent", r.size() > 20, "results=" + r.size());
                d.done();
            }
        });
        step("12_assistant_overview", new Step() {
            public void run(Done d) {
                a.show("assistant");
                String s = sc().ask("__overview");
                rec("12_assistant_overview", s.length() > 200, "chars=" + s.length());
                d.done();
            }
        });
        step("13_assistant_question", new Step() {
            public void run(Done d) {
                String s = sc().ask("Công danh sự nghiệp của tôi thế nào?");
                rec("13_assistant_question", s.contains("Quan Lộc"), "chars=" + s.length());
                d.done();
            }
        });
        step("14_assistant_year", new Step() {
            public void run(Done d) {
                String s = sc().ask("Hạn năm nay ra sao?");
                rec("14_assistant_year", s.contains(String.valueOf(sc().chart.viewYear)), "chars=" + s.length());
                d.done();
            }
        });
        step("15_settings_theme", new Step() {
            public void run(Done d) {
                a.show("settings");
                set("theme", "ivory-light");
                a.applySettings();
                rec("15_settings_theme", "ivory-light".equals(a.gcfg.th.id) && !a.gcfg.th.dark, a.gcfg.th.id);
                d.done();
            }
        });
        step("16_settings_language_en", new Step() {
            public void run(Done d) {
                sc().setLang("en");
                rec("16_settings_language_en", "Settings".equals(tabText("settings")), tabText("settings"));
                d.done();
            }
        });
        step("17_settings_language_ja", new Step() {
            public void run(Done d) {
                sc().setLang("ja");
                String s = tabText("library");
                rec("17_settings_language_ja", !s.equals("Library") && !s.equals("Thư Viện") && s.length() > 0, s);
                d.done();
            }
        });
        step("18_settings_font_layout", new Step() {
            public void run(Done d) {
                set("font", "large");
                set("layout", "list");
                a.applySettings();
                rec("18_settings_font_layout", "large".equals(a.s("font")) && "list".equals(a.s("layout")), "");
                d.done();
            }
        });
        step("19_chart_list_layout", new Step() {
            public void run(Done d) {
                a.show("chart");
                rec("19_chart_list_layout", find(a.root, "palaceList") != null, "");
                d.done();
            }
        });
        step("20_desktop_dex", new Step() {
            public void run(Done d) {
                set("desktop", true);
                set("layout", "board");
                a.applySettings();
                a.show("chart");
                rec("20_desktop_dex", a.wide && find(a.root, "board") != null,
                        "wide=" + a.wide + " dex=" + a.host.isDex());
                d.done();
            }
        });
        step("21_backup_export", new Step() {
            public void run(Done d) {
                a.show("settings");
                Map<String, Object> b = sc().exportBackup();
                rec("21_backup_export", b != null && Json.arr(b.get("saved")).size() > 0, "");
                d.done();
            }
        });
        step("22_backup_import", new Step() {
            public void run(Done d) {
                Map<String, Object> st = new java.util.LinkedHashMap<String, Object>(a.S);
                st.put("theme", "royal-gold");
                boolean ok = sc().importBackup(Json.stringify(Json.map("app", "Applied Astrology", "settings", st)));
                rec("22_backup_import", ok && "royal-gold".equals(a.gcfg.th.id), a.gcfg.th.id);
                d.done();
            }
        });
        step("23_network_check", new Step() {
            public void run(final Done d) {
                a.show("settings");
                click("sNet");
                a.h.postDelayed(new Runnable() {
                    public void run() {
                        View o = find(a.root, "netOut");
                        String s = o == null ? "" : String.valueOf(((android.widget.TextView) o).getText());
                        rec("23_network_check", s.length() > 5 && !s.equals(a.i18n.t("net_checking")),
                                s.length() > 140 ? s.substring(0, 140) : s);
                        d.done();
                    }
                }, 7000);
            }
        });
        step("24_calendar_view", new Step() {
            public void run(Done d) {
                java.util.Calendar n = java.util.Calendar.getInstance();
                sc().calY = n.get(1);
                sc().calM = n.get(2) + 1;
                sc().calD = n.get(5);
                a.show("calendar");
                View mv = find(a.root, "monthView");
                int dim = com.appliedastrology.tuvi.core.Lunar.daysInMonth(sc().calM, sc().calY);
                rec("24_calendar_view",
                        mv != null && ((MonthView) mv).info[dim] != null && find(a.root, "calDetail") != null,
                        "days=" + dim + " today=" + sc().calD + "/" + sc().calM);
                d.done();
            }
        });
        step("25_calendar_tet_2026", new Step() {
            public void run(Done d) {
                sc().calY = 2026;
                sc().calM = 2;
                sc().calD = 17;
                a.show("calendar");
                Cal.DayInfo i = Cal.dayInfo(17, 2, 2026);
                rec("25_calendar_tet_2026",
                        i.lunar.day == 1 && i.lunar.month == 1 && i.lunar.year == 2026 && "Bính Ngọ".equals(i.yearCC)
                                && i.goodHours.size() == 6 && !i.events.isEmpty(),
                        i.lunar.day + "/" + i.lunar.month + "/" + i.lunar.year + " " + i.dayCC + " | " + i.monthCC
                                + " | " + i.yearCC + " tiet=" + i.tiet + " " + i.than + " truc=" + i.truc + " ev="
                                + (i.events.isEmpty() ? "" : i.events.get(0).name));
                d.done();
            }
        });
        step("26_calendar_convert", new Step() {
            public void run(Done d) {
                android.widget.EditText cd = (android.widget.EditText) find(a.root, "cvD"),
                        cm = (android.widget.EditText) find(a.root, "cvM"),
                        cy = (android.widget.EditText) find(a.root, "cvY");
                android.widget.CheckBox lp = (android.widget.CheckBox) find(a.root, "cvLeap");
                android.widget.LinearLayout out = (android.widget.LinearLayout) find(a.root, "cvOut");
                cd.setText("1");
                cm.setText("1");
                cy.setText("2027");
                lp.setChecked(false);
                int[] r = sc().convert(false, cd, cm, cy, lp, out);
                cd.setText("3");
                cm.setText("11");
                cy.setText("2026");
                int[] r2 = sc().convert(true, cd, cm, cy, lp, out);
                rec("26_calendar_convert",
                        r != null && r[0] == 6 && r[1] == 2 && r[2] == 2027 && r2 != null && r2[3] == 25 && r2[4] == 9,
                        "Tet2027=" + (r == null ? "-" : r[0] + "/" + r[1] + "/" + r[2]) + " 3/11/2026->"
                                + (r2 == null ? "-" : r2[3] + "/" + r2[4] + "/" + r2[5]));
                d.done();
            }
        });
        step("27_calendar_sixty", new Step() {
            public void run(Done d) {
                android.widget.EditText e = (android.widget.EditText) find(a.root, "c60Y");
                e.setText("1995");
                Cal.YearInfo yi = sc().findYear(e, (android.widget.TextView) find(a.root, "c60Out"));
                View t = find(a.root, "sixty");
                int rows = t == null ? 0 : ((ViewGroup) t).getChildCount();
                a.scrollTo(find(a.root, "c60Out"));
                rec("27_calendar_sixty",
                        rows == 60 && yi != null && "Ất Hợi".equals(yi.name) && "Sơn Đầu Hỏa".equals(yi.napAm),
                        "rows=" + rows + " 1995=" + (yi == null ? "" : yi.name + " " + yi.napAm));
                d.done();
            }
        });
        step("28_paper_background", new Step() {
            public void run(Done d) {
                set("paper", "vangchanh");
                a.show("chart");
                ChartViews.Opts o = sc().opts();
                rec("28_paper_background", o.paper != null && o.paper[0] == 0xFFF7F1A8, "paper=" + a.s("paper"));
                d.done();
            }
        });
        step("29_about_view", new Step() {
            public void run(Done d) {
                set("paper", "none");
                a.show("settings");
                click("sAboutOpen");
                rec("29_about_view", "about".equals(a.current) && find(a.root, "abCheck") != null
                        && find(a.root, "abDiag") != null && find(a.root, "abUpdates") != null, "current=" + a.current);
                d.done();
            }
        });
        step("30_update_check", new Step() {
            public void run(final Done d) {
                sc().updates.check(false, new Updates.Cb() {
                    public void done(Map<String, Object> r) {
                        rec("30_update_check", Json.bool(r.get("ok"), false) && r.get("latest") != null,
                                Json.bool(r.get("ok"), false) ? "latest=" + r.get("latest") + " current="
                                        + r.get("current") + " newer=" + r.get("newer") + " assets=" + r.get("assets")
                                        + " asset=" + r.get("asset") : "status=" + r.get("status"));
                        a.sc.refreshAbout();
                        d.done();
                    }
                });
            }
        });
        step("31_update_download", new Step() {
            public void run(final Done d) {
                final Updates u = sc().updates;
                if (u.asset == null) {
                    rec("31_update_download", false, "no asset for platform");
                    d.done();
                    return;
                }
                final long t0 = System.currentTimeMillis();
                final boolean started = a.host.download(Json.str(u.asset.get("browser_download_url"), ""),
                        Json.str(u.asset.get("name"), "x.apk"));
                a.h.postDelayed(new Runnable() {
                    public void run() {
                        if ("running".equals(a.host.dlState) && System.currentTimeMillis() - t0 < 120000) {
                            a.h.postDelayed(this, 700);
                            return;
                        }
                        rec("31_update_download",
                                started && "done".equals(a.host.dlState) && a.host.dlGot > 100000
                                        && (a.host.dlTotal <= 0 || a.host.dlGot == a.host.dlTotal),
                                u.asset.get("name") + " " + a.host.dlState + " " + a.host.dlGot + "/" + a.host.dlTotal
                                        + " " + (System.currentTimeMillis() - t0) + "ms " + a.host.dlError);
                        d.done();
                    }
                }, 700);
            }
        });
        step("32_app_logs", new Step() {
            public void run(Done d) {
                String mk = "SELFTEST-LOG-" + System.currentTimeMillis();
                a.host.log(mk);
                String l = a.host.readLog();
                rec("32_app_logs", l.contains(mk), "native=" + l.length() + " chars");
                d.done();
            }
        });
        step("33_error_report", new Step() {
            public void run(Done d) {
                a.host.writeCrash("SELFTEST", new RuntimeException("SELFTEST synthetic error"));
                a.show("about");
                boolean ok = !a.host.crashList().isEmpty()
                        && a.host.crashList().get(0)[1].contains("SELFTEST synthetic error")
                        && sc().updates.reportUrl().contains("/issues/new")
                        && sc().updates.debugInfo().contains(Host.VERSION);
                rec("33_error_report", ok, "crash files=" + a.host.crashList().size() + " url="
                        + sc().updates.reportUrl().length() + " chars");
                a.host.clearCrash();
                d.done();
            }
        });
        step("34_safe_area", new Step() {
            public void run(Done d) {
                set("desktop", null);
                a.applySettings();
                a.show("chart");
                // v0.0.4: applySettings() dựng lại khung -> đo sau khi layout xong (trước đây
                // đo ngay nên nav = 0-0 trên API 35+)
                a.h.postDelayed(new Runnable() {
                    public void run() {
                        int[] nl = new int[2], hl = new int[2], rl = new int[2];
                        a.nav.getLocationInWindow(nl);
                        a.header.getLocationInWindow(hl);
                        a.root.getLocationInWindow(rl);
                        int rootH = a.root.getHeight(), navBottom = nl[1] - rl[1] + a.nav.getHeight(),
                                hdTop = hl[1] - rl[1];
                        boolean ok = navBottom <= rootH - a.insetB + 1 && hdTop >= a.insetT - 1 && nl[1] >= 0;
                        rec("34_safe_area", ok,
                                "nav " + (nl[1] - rl[1]) + "-" + navBottom + " / root " + rootH + " insets t="
                                        + a.insetT + " b=" + a.insetB + " header.top=" + hdTop + " sdk="
                                        + android.os.Build.VERSION.SDK_INT);
                        d.done();
                    }
                }, 900);
            }
        });
        step("35_language_fr_new", new Step() {
            public void run(Done d) {
                sc().setLang("fr");
                String s = tabText("calendar");
                rec("35_language_fr_new", s.length() > 0 && !s.equals(a.i18n.tIn("en", "tab_calendar"))
                        && !s.equals(a.i18n.tIn("vi", "tab_calendar")), s);
                d.done();
            }
        });
        step("36_restore_defaults", new Step() {
            public void run(Done d) {
                sc().resetDefaults();
                sc().form = sc().withMode(in("solar", 15, 8, 1995, 6, "Nam", false));
                a.show("chart");
                sc().build();
                rec("36_restore_defaults", "celestial-dark".equals(a.s("theme")) && "vi".equals(a.s("lang"))
                        && "none".equals(a.s("paper")) && "liquid".equals(a.s("lgMode")), "");
                d.done();
            }
        });
        // ---- v0.0.4 ----
        step("37_glass_liquid", new Step() {
            public void run(Done d) {
                set("lgOn", true);
                set("lgMode", "liquid");
                a.applySettings();
                a.show("settings");
                a.header.invalidate();
                // v0.0.5: dải viền khúc xạ tính ở luồng nền -> chờ tối đa 15 s (cho cả emulator
                // chậm)
                final long t0 = System.currentTimeMillis();
                final Done fd = d;
                a.h.post(new Runnable() {
                    public void run() {
                        if (!a.header.hasBand() && System.currentTimeMillis() - t0 < 15000) {
                            a.header.invalidate();
                            a.h.postDelayed(this, 120);
                            return;
                        }
                        Bitmap c = a.header.band;
                        int alpha = c == null ? -1 : (c.getPixel(c.getWidth() / 2, 2) >>> 24);
                        rec("37_glass_liquid",
                                "liquid".equals(a.gcfg.mode) && a.bg.blurBmp != null
                                        && find(a.root, "glassSettings") != null && a.header.hasBand(),
                                "blur=" + (a.bg.blurBmp == null ? "-"
                                        : a.bg.blurBmp.getWidth() + "x" + a.bg.blurBmp.getHeight()) + " band="
                                        + (c == null ? "pending" : c.getWidth() + "x" + c.getHeight() + " a=" + alpha)
                                        + " ready=" + (System.currentTimeMillis() - t0) + "ms render="
                                        + GlassViews.lastBandMs + "ms");
                        fd.done();
                    }
                });
            }
        });
        step("38_glass_frost", new Step() {
            public void run(Done d) {
                set("lgMode", "frost");
                a.applySettings();
                a.scrollTo(find(a.root, "glassSettings"));
                rec("38_glass_frost", "frost".equals(a.gcfg.mode), a.gcfg.mode);
                d.done();
            }
        });
        step("39_glass_off", new Step() {
            public void run(Done d) {
                set("lgOn", false);
                a.applySettings();
                a.show("chart");
                rec("39_glass_off", "off".equals(a.gcfg.mode) && a.header.getElevation() >= 0, a.gcfg.mode);
                d.done();
            }
        });
        step("40_glass_sliders", new Step() {
            public void run(Done d) {
                set("lgOn", true);
                set("lgMode", "liquid");
                set("lgBlur", 30L);
                set("lgRefr", 90L);
                set("lgSpec", 80L);
                a.applySettings();
                a.show("calendar");
                rec("40_glass_sliders",
                        a.gcfg.p.blur == 30 && Math.abs(a.gcfg.p.refraction - 0.9) < 1e-6
                                && Math.abs(a.gcfg.p.specular - 0.8) < 1e-6,
                        "blur=" + a.gcfg.p.blur + " refr=" + a.gcfg.p.refraction + " spec=" + a.gcfg.p.specular);
                d.done();
            }
        });
        step("41_glass_bg_aurora", new Step() {
            public void run(Done d) {
                set("lgBg", "aurora");
                set("lgChart", true);
                a.applySettings();
                a.show("chart");
                rec("41_glass_bg_aurora", "aurora".equals(a.gcfg.bg) && a.gcfg.chart, a.gcfg.bg);
                d.done();
            }
        });
        step("42_glass_reset", new Step() {
            public void run(Done d) {
                a.show("settings");
                sc().resetGlass();
                rec("42_glass_reset", "nebula".equals(a.s("lgBg")) && a.n("lgBlur") == 18
                        && "liquid".equals(a.s("lgMode")) && a.b("lgOn") && !a.b("lgChart"),
                        "bg=" + a.s("lgBg") + " blur=" + a.n("lgBlur"));
                d.done();
            }
        });
        step("43_native_no_webview", new Step() {
            public void run(Done d) {
                a.show("chart");
                int wv = count(a.getWindow().getDecorView(), WebView.class);
                rec("43_native_no_webview", wv == 0 && a.host.engine().startsWith("Native"),
                        "webviews=" + wv + " engine=" + a.host.engine());
                d.done();
            }
        });
        step("44_lang_all_68", new Step() {
            public void run(Done d) {
                int full = 0;
                String miss = "";
                for (I18n.Lang l : a.i18n.langs) {
                    boolean ok = a.i18n.tIn(l.code, "s_glass") != null && a.i18n.tIn(l.code, "lg_liquid") != null
                            && a.i18n.tIn(l.code, "tab_calendar") != null && a.i18n.tIn(l.code, "ab_report") != null;
                    if (ok)
                        full++;
                    else
                        miss += l.code + " ";
                }
                rec("44_lang_all_68", full == a.i18n.langs.size() && full == 68,
                        full + "/" + a.i18n.langs.size() + " " + miss);
                d.done();
            }
        });
        // ---- v0.0.5 ----
        step("45_glass_toggle", new Step() {
            public void run(Done d) {
                a.show("settings");
                sc().setS("lgOn", false);
                a.applySettings();
                a.show("settings");
                boolean offOk = "off".equals(a.gcfg.mode) && find(a.root, "sLgOn") != null
                        && find(a.root, "sLgMode") == null;
                Map<String, Object> st = Json.obj(a.host.get("aa.settings"));
                boolean saved = st != null && Boolean.FALSE.equals(st.get("lgOn"));
                sc().setS("lgOn", true);
                a.applySettings();
                a.show("settings");
                boolean onOk = !"off".equals(a.gcfg.mode) && find(a.root, "sLgMode") != null;
                boolean words = true;
                for (I18n.Lang l : a.i18n.langs)
                    if (a.i18n.tIn(l.code, "lg_enable") == null || a.i18n.tIn(l.code, "lg_enable_d") == null)
                        words = false;
                a.scrollTo(find(a.root, "glassSettings"));
                rec("45_glass_toggle", offOk && saved && onOk && words, "off=" + offOk + " saved=" + saved + " on="
                        + onOk + " i18n68=" + words + " mode=" + a.gcfg.mode);
                d.done();
            }
        });
        step("46_perf_scroll", new Step() {
            public void run(final Done d) {
                a.show("library");
                sc().search("Hóa Kỵ");
                final android.widget.ScrollView sv = a.scroller();
                final long r0 = GlassViews.scrollRedraws, k0 = GlassViews.scrollTicks;
                final List<Long> gaps = new ArrayList<Long>();
                final long[] last = { 0 };
                final int[] fr = { 0 };
                final int step = a.ui.dp(16);
                android.view.Choreographer.getInstance()
                        .postFrameCallback(new android.view.Choreographer.FrameCallback() {
                            public void doFrame(long ns) {
                                if (last[0] != 0)
                                    gaps.add((ns - last[0]) / 1000L);
                                last[0] = ns;
                                fr[0]++;
                                if (sv != null)
                                    sv.scrollBy(0, fr[0] < 90 ? step : -step);
                                if (fr[0] < 180) {
                                    android.view.Choreographer.getInstance().postFrameCallback(this);
                                    return;
                                }
                                long sum = 0, max = 0;
                                int jank = 0;
                                for (long g : gaps) {
                                    sum += g;
                                    max = Math.max(max, g);
                                    if (g > 33400)
                                        jank++;
                                }
                                java.util.Collections.sort(gaps);
                                long p90 = gaps.isEmpty() ? 0 : gaps.get((int) (gaps.size() * 0.9));
                                double avg = gaps.isEmpty() ? 0 : sum / 1000.0 / gaps.size();
                                rec("46_perf_scroll", sv != null && avg < 60 && max < 800000, String.format(
                                        java.util.Locale.US,
                                        "frames=%d avg=%.1fms p90=%.1fms max=%.1fms jank(>33ms)=%d%% cards=%d redraws=%d/ticks=%d",
                                        gaps.size(), avg, p90 / 1000.0, max / 1000.0,
                                        gaps.isEmpty() ? 0 : jank * 100 / gaps.size(), a.bg.frameCount(),
                                        GlassViews.scrollRedraws - r0, GlassViews.scrollTicks - k0));
                                d.done();
                            }
                        });
            }
        });
        step("47_perf_modal", new Step() {
            public void run(final Done d) {
                set("lgOn", true);
                set("lgMode", "liquid");
                a.applySettings();
                a.show("chart");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 40; i++)
                    sb.append("Liquid Glass ").append(i).append(" – Tử Vi Ứng Dụng Bửu Đình\n");
                long t0 = System.nanoTime();
                a.modal("Liquid Glass", a.ui.body(sb.toString()));
                final long openMs = (System.nanoTime() - t0) / 1000000L;
                final long t1 = System.currentTimeMillis();
                a.h.post(new Runnable() {
                    public void run() {
                        View m = find(a.root, "modal");
                        GlassViews.Frame box = m instanceof GlassViews.Frame ? (GlassViews.Frame) m : null;
                        if (box != null && !box.hasBand() && System.currentTimeMillis() - t1 < 15000) {
                            a.h.postDelayed(this, 100);
                            return;
                        }
                        long ready = System.currentTimeMillis() - t1;
                        rec("47_perf_modal", box != null && openMs < 400 && box.hasBand(),
                                "open=" + openMs + "ms (UI) bandReady=" + ready + "ms render=" + GlassViews.lastBandMs
                                        + "ms size=" + (box == null ? "-" : box.getWidth() + "x" + box.getHeight()));
                        later(d, true);
                    }
                });
            }
        });
        step("48_perf_tabs", new Step() {
            public void run(Done d) {
                String[] tabs = { "chart", "calendar", "library", "assistant", "saved", "settings", "about" };
                StringBuilder sb = new StringBuilder();
                long max = 0;
                for (String t : tabs) {
                    long t0 = System.nanoTime();
                    a.show(t);
                    long ms = (System.nanoTime() - t0) / 1000000L;
                    max = Math.max(max, ms);
                    sb.append(t).append('=').append(ms).append("ms ");
                }
                a.show("chart");
                rec("48_perf_tabs", max < 1500, "max=" + max + "ms " + sb);
                d.done();
            }
        });
        step("49_perf_settings", new Step() {
            public void run(Done d) {
                a.show("settings");
                long t0 = System.nanoTime();
                set("lgTint", 40L);
                a.applySettings();
                long ms1 = (System.nanoTime() - t0) / 1000000L;
                t0 = System.nanoTime();
                set("lgTint", 35L);
                a.applySettings();
                long ms2 = (System.nanoTime() - t0) / 1000000L;
                rec("49_perf_settings", ms1 < 1500 && ms2 < 1500, "applySettings=" + ms1 + "ms / " + ms2 + "ms bgBuild="
                        + GlassViews.lastBgMs + "ms (luồng nền)");
                d.done();
            }
        });
        step("50_glass_stability", new Step() {
            public void run(final Done d) {
                // đổi qua lại 3 chế độ × 2 kiểu nền liên tục, rồi kiểm tra không lỗi và khung
                // vẫn vẽ được
                String[] modes = { "liquid", "frost", "liquid" };
                String[] bgs = { "nebula", "aurora", "plain" };
                int errs = 0;
                for (int i = 0; i < 6; i++) {
                    try {
                        set("lgOn", i != 3);
                        set("lgMode", modes[i % 3]);
                        set("lgBg", bgs[i % 3]);
                        a.applySettings();
                        a.show(i % 2 == 0 ? "chart" : "settings");
                    } catch (Throwable e) {
                        errs++;
                        a.host.writeCrash("glass stability", e);
                    }
                }
                sc().resetGlass();
                a.show("chart");
                final int fe = errs;
                final long t0 = System.currentTimeMillis();
                a.h.post(new Runnable() {
                    public void run() {
                        if (!(a.bg.ready() && a.header.hasBand()) && System.currentTimeMillis() - t0 < 15000) {
                            a.header.invalidate();
                            a.h.postDelayed(this, 120);
                            return;
                        }
                        rec("50_glass_stability",
                                fe == 0 && a.bg.ready() && a.header.hasBand() && a.host.crashList().isEmpty(),
                                "errors=" + fe + " bgReady=" + a.bg.ready() + " band=" + a.header.hasBand()
                                        + " crashes=" + a.host.crashList().size() + " settle="
                                        + (System.currentTimeMillis() - t0) + "ms");
                        d.done();
                    }
                });
            }
        });
        step("51_perf_memory", new Step() {
            public void run(Done d) {
                Runtime rt = Runtime.getRuntime();
                System.gc();
                long used = (rt.totalMemory() - rt.freeMemory()) / 1048576L, max = rt.maxMemory() / 1048576L;
                rec("51_perf_memory", used < max * 0.75, "heap=" + used + "/" + max + " MB bands="
                        + GlassViews.bandCount + " frames=" + a.bg.frameCount());
                d.done();
            }
        });
        step("52_lunar_full_range", new Step() {
            public void run(Done d) {
                // Feature Check mục 7: toàn dải 1900–2099 – ngày âm hợp lệ (1..30), đổi
                // xuôi/ngược khớp, 7/5/2054 = 30/3 (lỗi "mùng 0" của 0.0.4)
                long t0 = System.nanoTime();
                int n = 0, bad = 0, rt = 0;
                for (int jd = com.appliedastrology.tuvi.core.Lunar.jdFromDate(1, 2,
                        1900); jd <= com.appliedastrology.tuvi.core.Lunar.jdFromDate(31, 12, 2099); jd++) {
                    int[] q = com.appliedastrology.tuvi.core.Lunar.jdToDate(jd);
                    com.appliedastrology.tuvi.core.Lunar.LDate l = com.appliedastrology.tuvi.core.Lunar
                            .solar2lunar(q[0], q[1], q[2]);
                    n++;
                    if (l.day < 1 || l.day > 30)
                        bad++;
                    int[] b = com.appliedastrology.tuvi.core.Lunar.lunar2solar(l.day, l.month, l.year, l.leap);
                    if (b == null || b[0] != q[0] || b[1] != q[1] || b[2] != q[2])
                        rt++;
                }
                com.appliedastrology.tuvi.core.Lunar.LDate x = com.appliedastrology.tuvi.core.Lunar.solar2lunar(7, 5,
                        2054);
                boolean fix = x.day == 30 && x.month == 3;
                rec("52_lunar_full_range", bad == 0 && rt == 0 && fix,
                        "days=" + n + " invalid=" + bad + " roundtripFail=" + rt + " 7/5/2054=" + x.day + "/" + x.month
                                + "/" + x.year + " " + (System.nanoTime() - t0) / 1000000 + "ms");
                d.done();
            }
        });
    }

    void later(final Done d, final boolean closeModal) {
        a.h.postDelayed(new Runnable() {
            public void run() {
                if (closeModal) {
                    capture(names.get(idx - 1) + "_modal");
                    a.closeModal();
                }
                d.done();
            }
        }, DELAY);
    }

    void start() {
        a.host.log("SELFTEST start " + steps.size() + " steps");
        next();
    }

    void next() {
        if (idx >= steps.size()) {
            finish();
            return;
        }
        final int i = idx++;
        final String name = names.get(i);
        try {
            steps.get(i).run(new Done() {
                public void done() {
                    a.h.postDelayed(new Runnable() {
                        public void run() {
                            capture(name);
                            a.h.postDelayed(new Runnable() {
                                public void run() {
                                    next();
                                }
                            }, DELAY);
                        }
                    }, 500);
                }
            });
        } catch (Throwable e) {
            rec(name, false, "EXCEPTION " + e);
            a.host.writeCrash("selftest " + name, e);
            a.h.postDelayed(new Runnable() {
                public void run() {
                    capture(name);
                    next();
                }
            }, DELAY);
        }
    }

    void capture(String name) {
        try {
            View v = a.root;
            Bitmap b = Bitmap.createBitmap(Math.max(1, v.getWidth()), Math.max(1, v.getHeight()),
                    Bitmap.Config.ARGB_8888);
            v.draw(new Canvas(b));
            FileOutputStream fo = new FileOutputStream(new File(dir, name + ".png"));
            b.compress(Bitmap.CompressFormat.PNG, 100, fo);
            fo.close();
            b.recycle();
            FileOutputStream sj = new FileOutputStream(new File(dir, "steps.jsonl"), true);
            sj.write((Json.stringify(
                    Json.map("step", name, "data", results.isEmpty() ? null : results.get(results.size() - 1))) + "\n")
                    .getBytes("UTF-8"));
            sj.close();
        } catch (Exception e) {
            a.host.log("capture " + e);
        }
    }

    void finish() {
        int pass = 0;
        for (Map<String, Object> r : results)
            if (Json.bool(r.get("ok"), false))
                pass++;
        Map<String, Object> sum = Json.map("app", "Applied Astrology", "version", Host.VERSION,
                "platform",
                Json.map("kind", "android", "os", a.host.os(), "device", a.host.device(), "abi", a.host.abi(), "engine",
                        a.host.engine()),
                "pass", (long) pass, "total", (long) results.size(), "results", results);
        try {
            FileOutputStream fo = new FileOutputStream(new File(dir, "results.json"));
            fo.write(Json.stringify(sum).getBytes("UTF-8"));
            fo.close();
        } catch (Exception e) {
            a.host.log("results " + e);
        }
        a.host.log("SELFTEST_DONE " + pass + "/" + results.size());
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> r : results)
            sb.append(Json.bool(r.get("ok"), false) ? "✔ " : "✘ ").append(r.get("step")).append("  ")
                    .append(r.get("detail")).append('\n');
        android.widget.TextView tv = a.ui.text(sb.toString(), 11.5f, a.gcfg.th.text, false);
        tv.setTypeface(android.graphics.Typeface.MONOSPACE);
        a.modal("SELFTEST " + pass + "/" + results.size(), tv);
        capture("99_done");
    }
}
