package app.bharosa.browser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Reading mode: the page's main text in a clean native view with adjustable font size. */
public class ReaderActivity extends Activity {
    /** Hand-off from MainActivity (kept static to avoid Binder size limits on long articles). */
    static String pTitle, pUrl;
    static List<String[]> pBlocks;

    private String title, url;
    private List<String[]> blocks;
    private LinearLayout body;
    private int font;

    static void open(Activity a, String title, String url, List<String[]> blocks) {
        pTitle = title;
        pUrl = url;
        pBlocks = blocks;
        a.startActivity(new Intent(a, ReaderActivity.class));
    }

    @Override
    protected void onCreate(Bundle b) {
        Ui.apply(this);
        super.onCreate(b);
        title = pTitle;
        url = pUrl;
        blocks = pBlocks;
        if (blocks == null) { finish(); return; }
        font = Prefs.readerFont(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg);
        LinearLayout bar = Ui.topBar(this, L.t("reader"));
        bar.addView(sizeButton("A−", L.t("font_smaller"), -2));
        bar.addView(sizeButton("A+", L.t("font_bigger"), 2));
        root.addView(bar);
        root.addView(Ui.divider(this));

        ScrollView sc = new ScrollView(this);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 20);
        body.setPadding(p, Ui.dp(this, 18), p, Ui.dp(this, 40));
        sc.addView(body);
        root.addView(sc, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        render();
    }

    private TextView sizeButton(String label, String desc, final int delta) {
        TextView t = Ui.text(this, label, 17, Ui.accent, true);
        t.setGravity(Gravity.CENTER);
        t.setContentDescription(desc);
        t.setBackground(Ui.rippleRound(Color.TRANSPARENT, Ui.dp(this, 20)));
        t.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 44)));
        t.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                font = Math.max(12, Math.min(32, font + delta));
                Prefs.setReaderFont(ReaderActivity.this, font);
                render();
            }
        });
        return t;
    }

    private void render() {
        body.removeAllViews();
        TextView h = Ui.text(this, title == null ? "" : title, font + 8, Ui.text, true);
        h.setTextIsSelectable(true);
        body.addView(h, new LinearLayout.LayoutParams(-1, -2));
        TextView host = Ui.text(this, Ui.hostOf(url), 13, Ui.accent, false);
        host.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 14));
        host.setContentDescription(L.t("open_original"));
        host.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { finish(); } });
        body.addView(host, new LinearLayout.LayoutParams(-1, -2));
        for (String[] bl : blocks) {
            String k = bl[0], s = bl[1];
            TextView t;
            if ("h".equals(k)) {
                t = Ui.text(this, s, font + 3, Ui.text, true);
                t.setPadding(0, Ui.dp(this, 14), 0, Ui.dp(this, 4));
            } else if ("l".equals(k)) {
                t = Ui.text(this, "•  " + s, font, Ui.text, false);
                t.setPadding(Ui.dp(this, 8), Ui.dp(this, 3), 0, Ui.dp(this, 3));
            } else if ("q".equals(k)) {
                t = Ui.text(this, s, font, Ui.text2, false);
                t.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                t.setPadding(Ui.dp(this, 14), Ui.dp(this, 6), 0, Ui.dp(this, 6));
            } else if ("c".equals(k)) {
                t = Ui.text(this, s, Math.max(12, font - 4), Ui.text2, false);
                t.setPadding(0, Ui.dp(this, 2), 0, Ui.dp(this, 8));
            } else {
                t = Ui.text(this, s, font, Ui.text, false);
                t.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            }
            t.setLineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, font * 0.35f, getResources().getDisplayMetrics()), 1f);
            t.setTextIsSelectable(true);
            body.addView(t, new LinearLayout.LayoutParams(-1, -2));
        }
    }

    /** Parses the READER script result; returns null when there is not enough text. */
    static List<String[]> parse(org.json.JSONObject j) {
        ArrayList<String[]> out = new ArrayList<>();
        org.json.JSONArray a = j.optJSONArray("b");
        if (a == null) return null;
        int chars = 0;
        for (int i = 0; i < a.length(); i++) {
            org.json.JSONArray e = a.optJSONArray(i);
            if (e == null || e.length() < 2) continue;
            String s = e.optString(1, "");
            if (s.isEmpty()) continue;
            out.add(new String[]{e.optString(0, "p"), s});
            chars += s.length();
        }
        return chars < 200 ? null : out;
    }
}
