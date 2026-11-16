package com.appliedastrology.tuvi.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bộ đọc/ghi JSON tối giản dùng chung cho lõi native (không phụ thuộc org.json để chạy được cả trên JVM khi kiểm thử).
 * Kiểu trả về: Map (LinkedHashMap), List, String, Double/Long, Boolean, null.
 */
public final class Json {
    private final String s; private int p;
    private Json(String s) { this.s = s; }

    public static Object parse(String text) {
        if (text == null) return null;
        Json j = new Json(text); j.ws(); Object v = j.value(); return v;
    }
    @SuppressWarnings("unchecked") public static Map<String, Object> obj(Object o) { return o instanceof Map ? (Map<String, Object>) o : null; }
    @SuppressWarnings("unchecked") public static List<Object> arr(Object o) { return o instanceof List ? (List<Object>) o : null; }
    public static String str(Object o, String def) { return o == null ? def : String.valueOf(o); }
    public static int num(Object o, int def) {
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof String) try { return Integer.parseInt((String) o); } catch (NumberFormatException e) { return def; }
        return def;
    }
    public static double dbl(Object o, double def) { return o instanceof Number ? ((Number) o).doubleValue() : def; }
    public static boolean bool(Object o, boolean def) { return o instanceof Boolean ? (Boolean) o : def; }

    private void ws() { while (p < s.length() && s.charAt(p) <= ' ') p++; }
    private Object value() {
        if (p >= s.length()) return null;
        char c = s.charAt(p);
        if (c == '{') {
            p++; Map<String, Object> m = new LinkedHashMap<String, Object>(); ws();
            if (p < s.length() && s.charAt(p) == '}') { p++; return m; }
            while (p < s.length()) {
                ws(); String k = string(); ws(); p++; ws(); m.put(k, value()); ws();
                if (p < s.length() && s.charAt(p) == ',') { p++; continue; }
                p++; break;
            }
            return m;
        }
        if (c == '[') {
            p++; List<Object> l = new ArrayList<Object>(); ws();
            if (p < s.length() && s.charAt(p) == ']') { p++; return l; }
            while (p < s.length()) {
                ws(); l.add(value()); ws();
                if (p < s.length() && s.charAt(p) == ',') { p++; continue; }
                p++; break;
            }
            return l;
        }
        if (c == '"') return string();
        if (s.startsWith("true", p)) { p += 4; return Boolean.TRUE; }
        if (s.startsWith("false", p)) { p += 5; return Boolean.FALSE; }
        if (s.startsWith("null", p)) { p += 4; return null; }
        int st = p; boolean fl = false;
        while (p < s.length()) {
            char d = s.charAt(p);
            if ((d >= '0' && d <= '9') || d == '-' || d == '+') p++;
            else if (d == '.' || d == 'e' || d == 'E') { fl = true; p++; }
            else break;
        }
        String n = s.substring(st, p);
        try { if (!fl) { long v = Long.parseLong(n); return v; } return Double.parseDouble(n); }
        catch (NumberFormatException e) { return 0L; }
    }
    private String string() {
        StringBuilder b = new StringBuilder(); p++;
        while (p < s.length()) {
            char c = s.charAt(p++);
            if (c == '"') break;
            if (c == '\\' && p < s.length()) {
                char e = s.charAt(p++);
                switch (e) {
                    case 'n': b.append('\n'); break; case 't': b.append('\t'); break; case 'r': b.append('\r'); break;
                    case 'b': b.append('\b'); break; case 'f': b.append('\f'); break;
                    case 'u': b.append((char) Integer.parseInt(s.substring(p, p + 4), 16)); p += 4; break;
                    default: b.append(e);
                }
            } else b.append(c);
        }
        return b.toString();
    }

    // ---------------- Ghi ----------------
    public static String stringify(Object o) { StringBuilder b = new StringBuilder(); write(b, o); return b.toString(); }
    @SuppressWarnings("unchecked")
    private static void write(StringBuilder b, Object o) {
        if (o == null) b.append("null");
        else if (o instanceof String) quote(b, (String) o);
        else if (o instanceof Boolean) b.append(o.toString());
        else if (o instanceof Double || o instanceof Float) {
            double d = ((Number) o).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) b.append((long) d); else b.append(d);
        }
        else if (o instanceof Number) b.append(o.toString());
        else if (o instanceof Map) {
            b.append('{'); boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) o).entrySet()) {
                if (!first) b.append(','); first = false; quote(b, e.getKey()); b.append(':'); write(b, e.getValue());
            }
            b.append('}');
        } else if (o instanceof List) {
            b.append('['); boolean first = true;
            for (Object x : (List<Object>) o) { if (!first) b.append(','); first = false; write(b, x); }
            b.append(']');
        } else quote(b, o.toString());
    }
    public static void quote(StringBuilder b, String s) {
        b.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': b.append("\\\""); break; case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n"); break; case '\r': b.append("\\r"); break; case '\t': b.append("\\t"); break;
                default: if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c);
            }
        }
        b.append('"');
    }
    /** Tiện ích tạo Map nhanh: map("a", 1, "b", "x") */
    public static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }
}
