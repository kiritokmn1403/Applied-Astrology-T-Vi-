package com.appliedastrology.tuvi;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.appliedastrology.tuvi.core.Theme;

import java.util.List;

/** Bộ dựng giao diện native (không XML, không AndroidX) – màu theo giao diện, cỡ chữ theo Cài Đặt. */
public final class Ui {
    final Context ctx; Theme th; float fs = 1f; final float dens;
    public Ui(Context c) { ctx = c; dens = c.getResources().getDisplayMetrics().density; }
    public int dp(float v) { return Math.round(v * dens); }
    public float sp(float v) { return v * fs; }
    public void setTheme(Theme t, String font) {
        th = t;
        fs = "compact".equals(font) ? 0.87f : "large".equals(font) ? 1.13f : "xlarge".equals(font) ? 1.27f : 1f;
    }

    public LinearLayout col() { LinearLayout l = new LinearLayout(ctx); l.setOrientation(LinearLayout.VERTICAL); return l; }
    public LinearLayout row() { LinearLayout l = new LinearLayout(ctx); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l; }
    public static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    public static LinearLayout.LayoutParams wrap() { return lp(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    public static LinearLayout.LayoutParams fill() { return lp(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    public static LinearLayout.LayoutParams weight(float w) { LinearLayout.LayoutParams p = lp(0, ViewGroup.LayoutParams.WRAP_CONTENT); p.weight = w; return p; }
    public LinearLayout.LayoutParams margins(LinearLayout.LayoutParams p, int l, int t, int r, int b) { p.setMargins(dp(l), dp(t), dp(r), dp(b)); return p; }

    public TextView text(CharSequence s, float size, int color, boolean bold) {
        TextView t = new TextView(ctx); t.setText(s); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(size)); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setLineSpacing(0, 1.18f);
        return t;
    }
    public TextView h2(CharSequence s) { TextView t = text(s, 17, th.gold2, true); t.setPadding(0, 0, 0, dp(6)); return t; }
    public TextView h3(CharSequence s) { TextView t = text(s, 15, th.gold2, true); t.setPadding(0, dp(8), 0, dp(4)); return t; }
    public TextView hint(CharSequence s) { return text(s, 12.5f, th.muted, false); }
    public TextView body(CharSequence s) { return text(s, 14, th.text, false); }
    public TextView grp(CharSequence s) { TextView t = text(s, 12.5f, th.muted, true); t.setPadding(0, dp(8), 0, dp(2)); return t; }

    GradientDrawable shape(int fill, int stroke, float radius) {
        GradientDrawable g = new GradientDrawable(); g.setColor(fill); g.setCornerRadius(dp(radius)); if (stroke != 0) g.setStroke(dp(1), stroke); return g;
    }
    public TextView button(CharSequence s, boolean primary, View.OnClickListener l) {
        TextView b = text(s, 14, primary ? (th.dark ? 0xFF1A1205 : Color.WHITE) : th.text, primary);
        b.setPadding(dp(13), dp(8), dp(13), dp(8)); b.setGravity(Gravity.CENTER);
        int fill = primary ? th.gold : Theme.alpha(th.card2, 200);
        StateListDrawable sl = new StateListDrawable();
        sl.addState(new int[]{android.R.attr.state_pressed}, shape(Theme.mix(fill, th.gold, 0.35), th.gold, 9));
        sl.addState(new int[]{}, shape(fill, primary ? th.gold : Theme.alpha(th.line, 220), 9));
        b.setBackground(sl); b.setClickable(true); b.setFocusable(true); b.setOnClickListener(l);
        b.setMinHeight(dp(40)); b.setMinimumHeight(dp(40));
        return b;
    }
    public TextView small(CharSequence s, View.OnClickListener l) { TextView b = button(s, false, l); b.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(12.5f)); b.setPadding(dp(9), dp(5), dp(9), dp(5)); b.setMinHeight(dp(34)); b.setMinimumHeight(dp(34)); return b; }
    public TextView chip(CharSequence s, View.OnClickListener l) {
        TextView b = text(s, 13, th.text, false); b.setPadding(dp(11), dp(5), dp(11), dp(5));
        b.setBackground(shape(Theme.alpha(th.card2, 190), th.line, 16)); b.setOnClickListener(l); b.setClickable(true); return b;
    }
    public EditText edit(String value, String hint, boolean number) {
        EditText e = new EditText(ctx); e.setText(value); e.setHint(hint); e.setTextColor(th.text); e.setHintTextColor(th.muted);
        e.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(14)); e.setSingleLine(true); e.setPadding(dp(9), dp(7), dp(9), dp(7));
        e.setBackground(shape(Theme.alpha(th.card2, 215), th.line, 8));
        if (number) e.setInputType(InputType.TYPE_CLASS_NUMBER);
        return e;
    }
    public Spinner spinner(final List<String> items, int sel, final OnPick pick) {
        Spinner s = new Spinner(ctx, Spinner.MODE_DROPDOWN);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(ctx, android.R.layout.simple_spinner_item, items) {
            @Override public View getView(int pos, View cv, ViewGroup parent) { TextView t = (TextView) super.getView(pos, cv, parent); t.setTextColor(th.text); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(14)); return t; }
            @Override public View getDropDownView(int pos, View cv, ViewGroup parent) {
                TextView t = (TextView) super.getDropDownView(pos, cv, parent); t.setTextColor(th.text); t.setBackgroundColor(th.card); t.setPadding(dp(12), dp(10), dp(12), dp(10)); t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(14)); return t;
            }
        };
        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(ad); s.setBackground(shape(Theme.alpha(th.card2, 215), th.line, 8)); s.setPadding(dp(4), dp(2), dp(4), dp(2));
        s.setPopupBackgroundDrawable(shape(th.card, th.line, 8));
        if (sel >= 0 && sel < items.size()) s.setSelection(sel, false);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { if (first) { first = false; return; } if (pick != null) pick.pick(pos); }
            @Override public void onNothingSelected(AdapterView<?> p) { }
        });
        return s;
    }
    public interface OnPick { void pick(int pos); }
    public interface OnBool { void set(boolean v); }
    public interface OnInt { void set(int v); }
    public CheckBox check(CharSequence label, boolean on, final OnBool cb) {
        CheckBox c = new CheckBox(ctx); c.setText(label); c.setTextColor(th.text); c.setChecked(on); c.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(14));
        c.setButtonTintList(ColorStateList.valueOf(th.gold));
        c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { @Override public void onCheckedChanged(CompoundButton b, boolean v) { if (cb != null) cb.set(v); } });
        return c;
    }
    public Switch toggle(CharSequence label, boolean on, final OnBool cb) {
        Switch s = new Switch(ctx); s.setText(label); s.setTextColor(th.text); s.setChecked(on); s.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp(13.5f));
        s.setThumbTintList(ColorStateList.valueOf(th.gold)); s.setTrackTintList(ColorStateList.valueOf(Theme.alpha(th.gold, 120)));
        s.setPadding(0, dp(4), 0, dp(4));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { @Override public void onCheckedChanged(CompoundButton b, boolean v) { if (cb != null) cb.set(v); } });
        return s;
    }
    /** Thanh trượt có nhãn và giá trị: min..max */
    public LinearLayout slider(final String label, final int min, int max, int value, final String unit, final OnInt cb) {
        LinearLayout box = col();
        final TextView lab = text(label + ": " + value + unit, 13, th.text, false); box.addView(lab);
        SeekBar sb = new SeekBar(ctx); sb.setMax(max - min); sb.setProgress(value - min);
        sb.setProgressTintList(ColorStateList.valueOf(th.gold)); sb.setThumbTintList(ColorStateList.valueOf(th.gold2));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) { lab.setText(label + ": " + (p + min) + unit); }
            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) { if (cb != null) cb.set(s.getProgress() + min); }
        });
        sb.setTag("slider"); box.addView(sb, fill());
        return box;
    }
    public View divider() { View v = new View(ctx); v.setBackgroundColor(Theme.alpha(th.line, 160)); v.setLayoutParams(margins(lp(ViewGroup.LayoutParams.MATCH_PARENT, 1), 0, 6, 0, 6)); return v; }

    /** Bố cục xuống dòng tự động (thay cho flex-wrap) */
    public static final class Flow extends ViewGroup {
        final int gap;
        public Flow(Context c, int gapPx) { super(c); gap = gapPx; }
        @Override protected void onMeasure(int ws, int hs) {
            int maxW = MeasureSpec.getSize(ws), x = 0, y = 0, lineH = 0, usedW = 0;
            if (MeasureSpec.getMode(ws) == MeasureSpec.UNSPECIFIED) maxW = Integer.MAX_VALUE;
            for (int i = 0; i < getChildCount(); i++) {
                View c = getChildAt(i); if (c.getVisibility() == GONE) continue;
                c.measure(MeasureSpec.makeMeasureSpec(maxW, MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int w = c.getMeasuredWidth(), h = c.getMeasuredHeight();
                if (x > 0 && x + w > maxW) { x = 0; y += lineH + gap; lineH = 0; }
                x += w + gap; lineH = Math.max(lineH, h); usedW = Math.max(usedW, x);
            }
            setMeasuredDimension(MeasureSpec.getMode(ws) == MeasureSpec.EXACTLY ? MeasureSpec.getSize(ws) : Math.min(usedW, maxW), y + lineH);
        }
        @Override protected void onLayout(boolean ch, int l, int t, int r, int b) {
            int maxW = r - l, x = 0, y = 0, lineH = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View c = getChildAt(i); if (c.getVisibility() == GONE) continue;
                int w = c.getMeasuredWidth(), h = c.getMeasuredHeight();
                if (x > 0 && x + w > maxW) { x = 0; y += lineH + gap; lineH = 0; }
                c.layout(x, y, x + w, y + h); x += w + gap; lineH = Math.max(lineH, h);
            }
        }
    }
    public Flow flow() { return new Flow(ctx, dp(6)); }

    /** Hàng nhãn + điều khiển (form) */
    public LinearLayout field(String label, View control, float weight) {
        LinearLayout c = col(); TextView l = text(label, 12, th.muted, false); c.addView(l);
        c.addView(control, fill()); c.setPadding(dp(3), dp(3), dp(3), dp(3));
        c.setLayoutParams(weight(weight)); return c;
    }
}
