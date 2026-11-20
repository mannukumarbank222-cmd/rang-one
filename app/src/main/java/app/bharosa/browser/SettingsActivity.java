package app.bharosa.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewDatabase;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle b) {
        Ui.apply(this);
        super.onCreate(b);
        build();
    }

    private void build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg);
        root.addView(Ui.topBar(this, L.t("settings")));
        root.addView(Ui.divider(this));
        ScrollView sc = new ScrollView(this);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, Ui.dp(this, 8), 0, Ui.dp(this, 24));
        sc.addView(list);
        root.addView(sc, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        row(R.drawable.ic_search, L.t("search_engine"), Prefs.engineName(this), new View.OnClickListener() {
            @Override public void onClick(View v) { chooseEngine(); }
        });
        String th = Prefs.theme(this);
        row(R.drawable.ic_desktop, L.t("theme"), "dark".equals(th) ? L.t("theme_dark") : "light".equals(th) ? L.t("theme_light") : L.t("theme_system"), new View.OnClickListener() {
            @Override public void onClick(View v) { chooseTheme(); }
        });
        String lg = Prefs.lang(this);
        row(R.drawable.ic_info, L.t("language"), "hi".equals(lg) ? "हिंदी" : "en".equals(lg) ? "English" : L.t("lang_auto"), new View.OnClickListener() {
            @Override public void onClick(View v) { chooseLang(); }
        });
        toggle(L.t("block_popups"), Prefs.blockPopups(this), new Toggle() { public void set(boolean v) { Prefs.setBlockPopups(SettingsActivity.this, v); } });
        toggle(L.t("js"), Prefs.js(this), new Toggle() { public void set(boolean v) { Prefs.setJs(SettingsActivity.this, v); } });
        toggle(L.t("desktop_default"), Prefs.desktopDefault(this), new Toggle() { public void set(boolean v) { Prefs.setDesktopDefault(SettingsActivity.this, v); } });
        row(R.drawable.ic_delete, L.t("clear_data"), L.t("clear_data_sub"), new View.OnClickListener() {
            @Override public void onClick(View v) { clearData(); }
        });
        row(R.drawable.ic_info, L.t("about_app"), getString(R.string.app_name) + " " + versionName(), new View.OnClickListener() {
            @Override public void onClick(View v) { startActivity(new Intent(SettingsActivity.this, AboutActivity.class)); }
        });
    }

    String versionName() {
        try { return getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception e) { return ""; }
    }

    interface Toggle { void set(boolean v); }

    private void row(int icon, String title, String sub, View.OnClickListener l) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        int p = Ui.dp(this, 18);
        r.setPadding(p, Ui.dp(this, 14), p, Ui.dp(this, 14));
        r.setBackground(Ui.rippleRound(Color.TRANSPARENT, 0));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon);
        iv.setColorFilter(Ui.accent);
        r.addView(iv, new LinearLayout.LayoutParams(Ui.dp(this, 24), Ui.dp(this, 24)));
        LinearLayout tx = new LinearLayout(this);
        tx.setOrientation(LinearLayout.VERTICAL);
        tx.setPadding(p, 0, 0, 0);
        tx.addView(Ui.text(this, title, 16, Ui.text, false));
        if (sub != null) tx.addView(Ui.text(this, sub, 13, Ui.text2, false));
        r.addView(tx, new LinearLayout.LayoutParams(0, -2, 1));
        r.setOnClickListener(l);
        list.addView(r, new LinearLayout.LayoutParams(-1, -2));
    }

    private void toggle(String title, boolean on, final Toggle t) {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        int p = Ui.dp(this, 18);
        r.setPadding(Ui.dp(this, 60), Ui.dp(this, 10), p, Ui.dp(this, 10));
        TextView tv = Ui.text(this, title, 16, Ui.text, false);
        r.addView(tv, new LinearLayout.LayoutParams(0, -2, 1));
        final Switch sw = new Switch(this);
        sw.setChecked(on);
        sw.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(android.widget.CompoundButton b, boolean v) { t.set(v); }
        });
        r.addView(sw);
        r.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { sw.toggle(); } });
        list.addView(r, new LinearLayout.LayoutParams(-1, -2));
    }

    private void chooseEngine() {
        String cur = Prefs.engine(this);
        int sel = 0;
        for (int i = 0; i < Prefs.ENGINE_IDS.length; i++) if (Prefs.ENGINE_IDS[i].equals(cur)) sel = i;
        new AlertDialog.Builder(this).setTitle(L.t("search_engine")).setSingleChoiceItems(Prefs.ENGINE_NAMES, sel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int w) { Prefs.setEngine(SettingsActivity.this, Prefs.ENGINE_IDS[w]); d.dismiss(); recreate(); }
        }).show();
    }

    private void chooseTheme() {
        final String[] ids = {"system", "light", "dark"};
        String[] names = {L.t("theme_system"), L.t("theme_light"), L.t("theme_dark")};
        int sel = 0;
        for (int i = 0; i < ids.length; i++) if (ids[i].equals(Prefs.theme(this))) sel = i;
        new AlertDialog.Builder(this).setTitle(L.t("theme")).setSingleChoiceItems(names, sel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int w) { Prefs.setTheme(SettingsActivity.this, ids[w]); d.dismiss(); recreate(); }
        }).show();
    }

    private void chooseLang() {
        final String[] ids = {"auto", "hi", "en"};
        String[] names = {L.t("lang_auto"), "हिंदी", "English"};
        int sel = 0;
        for (int i = 0; i < ids.length; i++) if (ids[i].equals(Prefs.lang(this))) sel = i;
        new AlertDialog.Builder(this).setTitle(L.t("language")).setSingleChoiceItems(names, sel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface d, int w) { Prefs.setLang(SettingsActivity.this, ids[w]); d.dismiss(); recreate(); }
        }).show();
    }

    private void clearData() {
        final String[] items = {L.t("c_history"), L.t("c_cookies"), L.t("c_cache"), L.t("c_offline"), L.t("c_bookmarks")};
        final boolean[] checked = {true, true, true, false, false};
        new AlertDialog.Builder(this).setTitle(L.t("clear_data"))
                .setMultiChoiceItems(items, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override public void onClick(DialogInterface d, int w, boolean c) { checked[w] = c; }
                })
                .setPositiveButton(L.t("clear"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Db db = Db.get(SettingsActivity.this);
                        if (checked[0]) db.clearHistory();
                        if (checked[1]) {
                            CookieManager.getInstance().removeAllCookies(null);
                            CookieManager.getInstance().flush();
                            WebStorage.getInstance().deleteAllData();
                            WebViewDatabase.getInstance(SettingsActivity.this).clearHttpAuthUsernamePassword();
                        }
                        if (checked[2]) {
                            WebView wv = new WebView(SettingsActivity.this);
                            wv.clearCache(true);
                            wv.clearFormData();
                            wv.destroy();
                        }
                        if (checked[3]) db.clearOffline();
                        if (checked[4]) db.clearBookmarks();
                        Toast.makeText(SettingsActivity.this, L.t("cleared"), Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton(L.t("cancel"), null).show();
    }
}
