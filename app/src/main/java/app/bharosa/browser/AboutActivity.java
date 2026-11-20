package app.bharosa.browser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {
    static final String DEVELOPER = "PlayRang Technologies";
    static final String EMAIL = "mannukumarbank222@gmail.com";
    static final String PRIVACY_URL = "https://mannukumarbank222-cmd.github.io/rang-studio/privacy-policy-rang-one.html";

    @Override
    protected void onCreate(Bundle b) {
        Ui.apply(this);
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg);
        root.addView(Ui.topBar(this, L.t("about")));
        root.addView(Ui.divider(this));
        ScrollView sc = new ScrollView(this);
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 20);
        col.setPadding(p, Ui.dp(this, 28), p, Ui.dp(this, 28));
        sc.addView(col);
        root.addView(sc, new LinearLayout.LayoutParams(-1, 0, 1));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.logo);
        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(Ui.dp(this, 88), Ui.dp(this, 88));
        ll.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(logo, ll);
        TextView name = Ui.text(this, getString(R.string.app_name), 24, Ui.text, true);
        name.setGravity(Gravity.CENTER);
        name.setPadding(0, Ui.dp(this, 12), 0, 0);
        col.addView(name, new LinearLayout.LayoutParams(-1, -2));
        String vn = "";
        try { vn = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception ignored) { }
        TextView ver = Ui.text(this, L.t("version") + " " + vn, 14, Ui.text2, false);
        ver.setGravity(Gravity.CENTER);
        col.addView(ver, new LinearLayout.LayoutParams(-1, -2));

        col.addView(card(L.t("about_text")), cardLp());
        col.addView(card(L.t("about_data")), cardLp());

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setBackground(Ui.round(Ui.surface, Ui.dp(this, 16)));
        info.addView(row(L.t("developer"), DEVELOPER, null));
        info.addView(Ui.divider(this));
        info.addView(row(L.t("contact"), EMAIL, new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL));
                i.putExtra(Intent.EXTRA_SUBJECT, "Rang One");
                try { startActivity(i); } catch (Exception e) { Toast.makeText(AboutActivity.this, EMAIL, Toast.LENGTH_LONG).show(); }
            }
        }));
        info.addView(Ui.divider(this));
        info.addView(row(L.t("privacy"), "mannukumarbank222-cmd.github.io", new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL));
                i.setClass(AboutActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }));
        col.addView(info, cardLp());

        TextView made = Ui.text(this, "Made in India · भारत में बना", 13, Ui.text2, false);
        made.setGravity(Gravity.CENTER);
        made.setPadding(0, Ui.dp(this, 20), 0, 0);
        col.addView(made, new LinearLayout.LayoutParams(-1, -2));
        TextView cp = Ui.text(this, "© 2026 " + DEVELOPER, 12, Ui.text2, false);
        cp.setGravity(Gravity.CENTER);
        col.addView(cp, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }

    private LinearLayout.LayoutParams cardLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = Ui.dp(this, 16);
        return lp;
    }

    private View card(String s) {
        TextView t = Ui.text(this, s, 14, Ui.text, false);
        t.setLineSpacing(0, 1.25f);
        int p = Ui.dp(this, 16);
        t.setPadding(p, p, p, p);
        t.setBackground(Ui.round(Ui.surface, Ui.dp(this, 16)));
        return t;
    }

    private View row(String k, String v, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.VERTICAL);
        int p = Ui.dp(this, 16);
        r.setPadding(p, Ui.dp(this, 12), p, Ui.dp(this, 12));
        r.addView(Ui.text(this, k, 12, Ui.text2, false));
        r.addView(Ui.text(this, v, 15, l != null ? Ui.accent : Ui.text, l != null));
        if (l != null) {
            r.setBackground(Ui.rippleRound(Color.TRANSPARENT, Ui.dp(this, 16)));
            r.setOnClickListener(l);
        }
        return r;
    }
}
