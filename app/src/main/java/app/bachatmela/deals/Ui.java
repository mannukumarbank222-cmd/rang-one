package app.bachatmela.deals;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Palette and plain-View helpers shared by all screens. */
public final class Ui {
    public static final int ORANGE = 0xFFF26B1D, ORANGE_DARK = 0xFFD9560D, SAFFRON = 0xFFFFB020,
            GREEN = 0xFF1E9E4A, GREEN_LIGHT = 0xFFE6F6EC, BG = 0xFFFFF8F1, SURFACE = 0xFFFFFFFF,
            TEXT = 0xFF221A14, TEXT2 = 0xFF6F655E, DIVIDER = 0xFFF0E4D8, CHIP = 0xFFFFEBDD, RED = 0xFFE03131;

    public static void window(Activity a) {
        Window w = a.getWindow();
        w.setStatusBarColor(ORANGE_DARK);
        w.setNavigationBarColor(SURFACE);
        if (Build.VERSION.SDK_INT >= 26) w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    public static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }

    public static GradientDrawable round(int color, float r) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(r);
        return g;
    }

    public static GradientDrawable stroke(int fill, int strokeColor, int strokePx, float r) {
        GradientDrawable g = round(fill, r);
        g.setStroke(strokePx, strokeColor);
        return g;
    }

    public static void ripple(View v, int fill, float r) {
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(0x33F26B1D), round(fill, r), round(0xFFFFFFFF, r)));
        v.setClickable(true);
        v.setFocusable(true);
    }

    public static TextView tv(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    public static TextView ellipsize(TextView t, int lines) {
        t.setMaxLines(lines);
        t.setEllipsize(TextUtils.TruncateAt.END);
        return t;
    }

    public static TextView strike(TextView t) {
        t.setPaintFlags(t.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        return t;
    }

    public static ImageView icon(Context c, int res, int color, int sizeDp) {
        ImageView iv = new ImageView(c);
        iv.setImageResource(res);
        iv.setColorFilter(color);
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return iv;
    }

    public static LinearLayout.LayoutParams lp(int w, int h) { return new LinearLayout.LayoutParams(w, h); }
    public static LinearLayout.LayoutParams lpw(float weight) { return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight); }
    public static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT, WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static LinearLayout.LayoutParams margins(LinearLayout.LayoutParams p, Context c, int l, int t, int r, int b) {
        p.setMargins(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
        return p;
    }

    /** Store name as a plain text badge — never a logo. */
    public static TextView storeBadge(Context c, String store) {
        TextView t = tv(c, store, 11, 0xFFFFFFFF, true);
        t.setBackground(round(Cats.storeColor(store), dp(c, 4)));
        t.setPadding(dp(c, 6), dp(c, 1), dp(c, 6), dp(c, 2));
        return t;
    }

    public static TextView offBadge(Context c, int pct) {
        TextView t = tv(c, pct + "% छूट", 11, 0xFFFFFFFF, true);
        t.setBackground(round(pct >= 70 ? RED : GREEN, dp(c, 4)));
        t.setPadding(dp(c, 6), dp(c, 2), dp(c, 6), dp(c, 2));
        return t;
    }

    public static TextView chip(Context c, String s) {
        TextView t = tv(c, s, 13, TEXT, false);
        t.setPadding(dp(c, 14), dp(c, 7), dp(c, 14), dp(c, 8));
        t.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = lp(WRAP, WRAP);
        p.setMargins(0, 0, dp(c, 8), 0);
        t.setLayoutParams(p);
        setChip(t, false, ORANGE);
        return t;
    }

    public static void setChip(TextView t, boolean on, int color) {
        Context c = t.getContext();
        if (on) {
            t.setBackground(round(color, dp(c, 18)));
            t.setTextColor(0xFFFFFFFF);
            t.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            t.setBackground(stroke(SURFACE, DIVIDER, dp(c, 1), dp(c, 18)));
            t.setTextColor(TEXT);
            t.setTypeface(Typeface.DEFAULT);
        }
    }

    public static View card(Context c, View inner) {
        FrameLayout f = new FrameLayout(c);
        f.setBackground(round(SURFACE, dp(c, 12)));
        f.setElevation(dp(c, 2));
        f.addView(inner);
        return f;
    }

    public static TextView sectionTitle(Context c, String hi, String en) {
        TextView t = tv(c, hi + "  ·  " + en, 15, TEXT, true);
        t.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 8));
        return t;
    }

    private Ui() { }
}
