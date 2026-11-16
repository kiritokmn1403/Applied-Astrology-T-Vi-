package com.appliedastrology.tuvi.core;

import java.util.Locale;

/** Bỏ dấu tiếng Việt + chữ thường (giống AAHost.fold của bản web và fold_table.h của libaacore). */
public final class Fold {
    private Fold() { }
    private static final String SRC = "àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ";
    private static final String DST = "aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd";
    private static final char[] MAP = new char[0x1F00];
    static { for (int i = 0; i < SRC.length(); i++) MAP[SRC.charAt(i)] = DST.charAt(i); }

    public static String fold(String s) {
        if (s == null) return "";
        String l = s.toLowerCase(Locale.ROOT);
        StringBuilder b = new StringBuilder(l.length());
        for (int i = 0; i < l.length(); i++) {
            char c = l.charAt(i);
            if (c < MAP.length && MAP[c] != 0) b.append(MAP[c]);
            else if (c == '̀' || c == '́' || c == '̃' || c == '̉' || c == '̣') continue;
            else b.append(c);
        }
        return b.toString();
    }
}
