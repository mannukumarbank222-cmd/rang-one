package app.bharosa.browser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.content.res.ColorStateList;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Colours and small view helpers shared by all screens. */
public final class Ui {
    public static boolean dark;
    public static int bg, surface, surface2, text, text2, accent, divider, privateBg, privateText;

    public static final int ORANGE = 0xFFE8641A;

    public static void apply(Activity a) {
        dark = Prefs.isDark(a);
        a.setTheme(dark ? R.style.AppDark : R.style.AppLight);
        if (dark) {
            bg = 0xFF121212; surface = 0xFF1E1E1E; surface2 = 0xFF2C2C2C; text = 0xFFECECEC; text2 = 0xFFA6A6A6;
            accent = 0xFFFF8A3D; divider = 0xFF333333;
        } else {
            bg = 0xFFFFF7F2; surface = 0xFFFFFFFF; surface2 = 0xFFF3ECE7; text = 0xFF1F1A17; text2 = 0xFF6E6560;
            accent = ORANGE; divider = 0xFFE9DFD8;
        }
        privateBg = 0xFF2B2440; privateText = 0xFFEDE7FF;
        L.init(a);
        Window w = a.getWindow();
        w.setStatusBarColor(surface);
        w.setNavigationBarColor(surface);
        int flags = 0;
        if (!dark) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        w.getDecorView().setSystemUiVisibility(flags);
    }

    public static int dp(Context c, float v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }

    public static GradientDrawable round(int color, float radiusPx) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(radiusPx);
        return g;
    }

    public static GradientDrawable roundStroke(int color, float radiusPx, int strokePx, int strokeColor) {
        GradientDrawable g = round(color, radiusPx);
        g.setStroke(strokePx, strokeColor);
        return g;
    }

    public static Drawable ripple(Drawable content, Drawable mask) {
        int rc = dark ? 0x33FFFFFF : 0x22000000;
        return new RippleDrawable(ColorStateList.valueOf(rc), content, mask);
    }

    public static Drawable rippleRound(int color, float r) {
        return ripple(round(color, r), round(Color.WHITE, r));
    }

    public static Drawable rippleBorderless() {
        int rc = dark ? 0x33FFFFFF : 0x22000000;
        return new RippleDrawable(ColorStateList.valueOf(rc), null, null);
    }

    public static ImageView icon(Context c, int res, int color, int sizeDp, String desc) {
        ImageView iv = new ImageView(c);
        iv.setImageResource(res);
        iv.setColorFilter(color);
        int pad = dp(c, (sizeDp - 24) / 2f);
        iv.setPadding(pad, pad, pad, pad);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        iv.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        iv.setBackground(rippleBorderless());
        iv.setContentDescription(desc);
        iv.setClickable(true);
        iv.setFocusable(true);
        return iv;
    }

    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    public static TextView single(Context c, String s, float sp, int color, boolean bold) {
        TextView t = text(c, s, sp, color, bold);
        t.setSingleLine(true);
        t.setEllipsize(TextUtils.TruncateAt.END);
        return t;
    }

    /** Coloured letter tile for a site (no third-party logos). */
    public static TextView letterTile(Context c, String title, String url, int sizeDp) {
        String s = title == null || title.isEmpty() ? "?" : title.substring(0, Character.charCount(title.codePointAt(0)));
        TextView t = new TextView(c);
        t.setText(s.toUpperCase());
        t.setGravity(Gravity.CENTER);
        t.setTextColor(Color.WHITE);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeDp * 0.42f);
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(colorFor(url == null ? s : url));
        t.setBackground(g);
        t.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return t;
    }

    private static final int[] TILE = {0xFFE8641A, 0xFF2E7D32, 0xFF1565C0, 0xFFC62828, 0xFF6A1B9A, 0xFF00838F, 0xFFAD1457, 0xFF5D4037, 0xFF283593, 0xFFEF6C00};

    public static int colorFor(String key) {
        String host = key;
        try {
            android.net.Uri u = android.net.Uri.parse(key);
            if (u.getHost() != null) host = u.getHost() + u.getPath();
        } catch (Exception ignored) { }
        int h = host.hashCode();
        return TILE[Math.abs(h % TILE.length)];
    }

    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(divider);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(c, 0.7f))));
        return v;
    }

    /** Standard top bar for secondary screens: back arrow + title. Returns the bar. */
    public static LinearLayout topBar(final Activity a, String title) {
        LinearLayout bar = new LinearLayout(a);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(surface);
        bar.setPadding(dp(a, 4), 0, dp(a, 8), 0);
        bar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(a, 56)));
        ImageView back = icon(a, R.drawable.ic_back, text, 48, L.t("back"));
        back.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { a.finish(); }
        });
        bar.addView(back);
        TextView t = single(a, title, 20, text, true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        lp.leftMargin = dp(a, 8);
        bar.addView(t, lp);
        return bar;
    }

    public static String hostOf(String url) {
        if (url == null) return "";
        try {
            String h = android.net.Uri.parse(url).getHost();
            if (h == null) return url;
            return h.startsWith("www.") ? h.substring(4) : h;
        } catch (Exception e) {
            return url;
        }
    }
}
