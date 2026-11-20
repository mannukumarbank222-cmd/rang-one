package app.bharosa.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Patterns;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    static final String ACTION_NEW_TAB = "app.bharosa.browser.NEW_TAB";
    static final String ACTION_NEW_PRIVATE = "app.bharosa.browser.NEW_PRIVATE_TAB";
    static final String ACTION_BOOKMARKS = "app.bharosa.browser.BOOKMARKS";
    static final String ACTION_SHOW_TABS = "app.bharosa.browser.SHOW_TABS";
    static final String ACTION_HISTORY = "app.bharosa.browser.HISTORY";
    static final String EXTRA_OFFLINE_PATH = "offline_path";
    static final String EXTRA_TITLE = "title";

    static final int REQ_LIBRARY = 11;
    static final int REQ_FILE = 12;
    static final int REQ_SETTINGS = 13;

    static final String DESKTOP_UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private Tab cur;

    private FrameLayout root;
    private LinearLayout topBar, addressBox, findBar;
    private EditText urlEdit, findEdit;
    private TextView findCount, tabCount;
    private ImageView siteIcon, actionIcon;
    private ProgressBar progress;
    private FrameLayout content, webHolder;
    private ScrollView homeScroll;
    private ImageView backBtn, fwdBtn;
    private FrameLayout tabSwitcher;
    private FrameLayout fullscreenHolder;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;
    private ValueCallback<Uri[]> fileCallback;
    private String themeAtStart, langAtStart;
    private Dialog menuDialog;

    // ------------------------------------------------------------------ lifecycle

    @Override
    protected void onCreate(Bundle saved) {
        Ui.apply(this);
        super.onCreate(saved);
        themeAtStart = Prefs.theme(this) + Ui.dark;
        langAtStart = Prefs.lang(this);
        buildUi();

        boolean restored = false;
        if (saved != null) {
            ArrayList<String> urls = saved.getStringArrayList("tabs");
            if (urls != null && !urls.isEmpty()) {
                for (String u : urls) {
                    Tab t = newTab(false, false);
                    if (u != null && !u.isEmpty()) { t.url = u; t.home = false; }
                }
                int idx = Math.max(0, Math.min(saved.getInt("cur", 0), tabs.size() - 1));
                switchTo(tabs.get(idx));
                restored = true;
            }
        }
        if (!restored) {
            switchTo(newTab(false, false));
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        ArrayList<String> urls = new ArrayList<>();
        int curIdx = 0;
        for (Tab t : tabs) {
            if (t.incognito) continue;
            if (t == cur) curIdx = urls.size();
            urls.add(t.home || t.offlineView || t.url == null ? "" : t.url);
        }
        out.putStringArrayList("tabs", urls);
        out.putInt("cur", curIdx);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String th = Prefs.theme(this) + Prefs.isDark(this);
        if (!th.equals(themeAtStart) || !Prefs.lang(this).equals(langAtStart)) {
            recreate();
            return;
        }
        for (Tab t : tabs) if (t.web != null) applySettings(t);
        if (cur != null && cur.web != null) cur.web.onResume();
        if (cur != null && cur.home) showHome();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cur != null && cur.web != null) cur.web.onPause();
        CookieManager.getInstance().flush();
    }

    @Override
    protected void onDestroy() {
        for (Tab t : tabs) destroyWeb(t);
        tabs.clear();
        CookieManager.getInstance().setAcceptCookie(true);
        super.onDestroy();
    }

    private void handleIntent(Intent i) {
        if (i == null) return;
        String a = i.getAction();
        if (Intent.ACTION_VIEW.equals(a) && i.getData() != null) {
            String u = i.getData().toString();
            if (u.startsWith("http://") || u.startsWith("https://")) openInNewTab(u, false);
        } else if (Intent.ACTION_WEB_SEARCH.equals(a)) {
            String q = i.getStringExtra(SearchManager.QUERY);
            if (q != null && !q.trim().isEmpty()) openInNewTab(toUrl(q), false);
        } else if (Intent.ACTION_SEND.equals(a)) {
            String s = i.getStringExtra(Intent.EXTRA_TEXT);
            if (s != null && !s.trim().isEmpty()) openInNewTab(toUrl(s.trim()), false);
        } else if (ACTION_NEW_TAB.equals(a)) {
            switchTo(newTab(false, true));
        } else if (ACTION_NEW_PRIVATE.equals(a)) {
            switchTo(newTab(true, true));
        } else if (ACTION_BOOKMARKS.equals(a)) {
            openLibrary(LibraryActivity.MODE_BOOKMARKS);
        } else if (ACTION_HISTORY.equals(a)) {
            openLibrary(LibraryActivity.MODE_HISTORY);
        } else if (ACTION_SHOW_TABS.equals(a)) {
            showTabs();
        }
    }

    // ------------------------------------------------------------------ UI

    private void buildUi() {
        root = new FrameLayout(this);
        root.setBackgroundColor(Ui.bg);

        LinearLayout main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        root.addView(main, new FrameLayout.LayoutParams(-1, -1));

        // top bar with address box
        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), dp(8), dp(10), dp(8));
        topBar.setBackgroundColor(Ui.surface);
        main.addView(topBar, new LinearLayout.LayoutParams(-1, dp(64)));

        addressBox = new LinearLayout(this);
        addressBox.setOrientation(LinearLayout.HORIZONTAL);
        addressBox.setGravity(Gravity.CENTER_VERTICAL);
        addressBox.setPadding(dp(6), 0, dp(4), 0);
        topBar.addView(addressBox, new LinearLayout.LayoutParams(0, -1, 1));

        siteIcon = Ui.icon(this, R.drawable.ic_search, Ui.text2, 36, "");
        siteIcon.setClickable(false);
        siteIcon.setBackground(null);
        addressBox.addView(siteIcon);

        urlEdit = new EditText(this);
        urlEdit.setBackground(null);
        urlEdit.setSingleLine(true);
        urlEdit.setHint(L.t("search_hint"));
        urlEdit.setHintTextColor(Ui.text2);
        urlEdit.setTextColor(Ui.text);
        urlEdit.setTextSize(16);
        urlEdit.setPadding(dp(4), 0, dp(4), 0);
        urlEdit.setImeOptions(EditorInfo.IME_ACTION_GO | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        urlEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlEdit.setSelectAllOnFocus(true);
        urlEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int id, KeyEvent e) {
                String s = urlEdit.getText().toString().trim();
                if (s.isEmpty()) return true;
                hideKeyboard();
                urlEdit.clearFocus();
                load(cur, toUrl(s));
                return true;
            }
        });
        urlEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean has) {
                if (has && cur != null && !cur.home && cur.url != null && !cur.offlineView) urlEdit.setText(cur.url);
                if (has) urlEdit.selectAll();
                if (!has) updateBars();
                actionIcon.setImageResource(has ? R.drawable.ic_close : R.drawable.ic_refresh);
                actionIcon.setVisibility(has || (cur != null && !cur.home) ? View.VISIBLE : View.GONE);
            }
        });
        addressBox.addView(urlEdit, new LinearLayout.LayoutParams(0, -1, 1));

        actionIcon = Ui.icon(this, R.drawable.ic_refresh, Ui.text2, 40, L.t("reload"));
        actionIcon.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (urlEdit.hasFocus()) {
                    if (urlEdit.getText().length() > 0) urlEdit.setText("");
                    else { urlEdit.clearFocus(); hideKeyboard(); }
                } else if (cur != null && cur.web != null) {
                    if (progress.getVisibility() == View.VISIBLE) cur.web.stopLoading(); else cur.web.reload();
                }
            }
        });
        addressBox.addView(actionIcon);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Ui.accent));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.surface));
        progress.setVisibility(View.GONE);
        FrameLayout.LayoutParams plp;
        main.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));

        buildFindBar(main);

        content = new FrameLayout(this);
        main.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));
        webHolder = new FrameLayout(this);
        content.addView(webHolder, new FrameLayout.LayoutParams(-1, -1));
        homeScroll = new ScrollView(this);
        homeScroll.setFillViewport(true);
        homeScroll.setBackgroundColor(Ui.bg);
        content.addView(homeScroll, new FrameLayout.LayoutParams(-1, -1));

        main.addView(Ui.divider(this));
        // bottom bar
        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER_VERTICAL);
        bottom.setBackgroundColor(Ui.surface);
        main.addView(bottom, new LinearLayout.LayoutParams(-1, dp(56)));

        backBtn = Ui.icon(this, R.drawable.ic_back, Ui.text, 48, L.t("back"));
        backBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { goBack(); } });
        fwdBtn = Ui.icon(this, R.drawable.ic_forward, Ui.text, 48, L.t("forward"));
        fwdBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { goForward(); } });
        ImageView homeBtn = Ui.icon(this, R.drawable.ic_home, Ui.text, 48, L.t("home"));
        homeBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showHome(); } });

        FrameLayout tabsBtn = new FrameLayout(this);
        tabsBtn.setBackground(Ui.rippleBorderless());
        tabsBtn.setContentDescription(L.t("tabs"));
        tabCount = new TextView(this);
        tabCount.setGravity(Gravity.CENTER);
        tabCount.setTextColor(Ui.text);
        tabCount.setTextSize(12);
        tabCount.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        tabCount.setBackground(Ui.roundStroke(Color.TRANSPARENT, dp(5), dp(2), Ui.text));
        FrameLayout.LayoutParams tlp = new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER);
        tabsBtn.addView(tabCount, tlp);
        tabsBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showTabs(); } });

        ImageView menuBtn = Ui.icon(this, R.drawable.ic_more, Ui.text, 48, L.t("menu"));
        menuBtn.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { showMenu(); } });

        View[] bs = {backBtn, fwdBtn, homeBtn, tabsBtn, menuBtn};
        for (View b : bs) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
            if (b instanceof ImageView) {
                FrameLayout wrap = new FrameLayout(this);
                ((ViewGroup.LayoutParams) b.getLayoutParams()).width = dp(48);
                wrap.addView(b, new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER));
                bottom.addView(wrap, lp);
            } else {
                bottom.addView(b, lp);
            }
        }

        // tab switcher overlay
        tabSwitcher = new FrameLayout(this);
        tabSwitcher.setBackgroundColor(Ui.bg);
        tabSwitcher.setVisibility(View.GONE);
        tabSwitcher.setClickable(true);
        root.addView(tabSwitcher, new FrameLayout.LayoutParams(-1, -1));

        fullscreenHolder = new FrameLayout(this);
        fullscreenHolder.setBackgroundColor(Color.BLACK);
        fullscreenHolder.setVisibility(View.GONE);
        root.addView(fullscreenHolder, new FrameLayout.LayoutParams(-1, -1));

        setContentView(root);
    }

    private void buildFindBar(LinearLayout main) {
        findBar = new LinearLayout(this);
        findBar.setOrientation(LinearLayout.HORIZONTAL);
        findBar.setGravity(Gravity.CENTER_VERTICAL);
        findBar.setBackgroundColor(Ui.surface);
        findBar.setPadding(dp(12), 0, dp(4), 0);
        findBar.setVisibility(View.GONE);
        main.addView(findBar, new LinearLayout.LayoutParams(-1, dp(52)));
        findEdit = new EditText(this);
        findEdit.setSingleLine(true);
        findEdit.setHint(L.t("find"));
        findEdit.setTextColor(Ui.text);
        findEdit.setHintTextColor(Ui.text2);
        findEdit.setBackground(null);
        findEdit.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        findEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                if (cur != null && cur.web != null) {
                    if (s.length() == 0) { cur.web.clearMatches(); findCount.setText(""); }
                    else cur.web.findAllAsync(s.toString());
                }
            }
        });
        findEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int id, KeyEvent e) {
                if (cur != null && cur.web != null) cur.web.findNext(true);
                hideKeyboard();
                return true;
            }
        });
        findBar.addView(findEdit, new LinearLayout.LayoutParams(0, -1, 1));
        findCount = Ui.text(this, "", 13, Ui.text2, false);
        findCount.setPadding(dp(6), 0, dp(6), 0);
        findBar.addView(findCount);
        ImageView up = Ui.icon(this, R.drawable.ic_up, Ui.text, 44, "");
        up.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if (cur.web != null) cur.web.findNext(false); } });
        ImageView down = Ui.icon(this, R.drawable.ic_down, Ui.text, 44, "");
        down.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { if (cur.web != null) cur.web.findNext(true); } });
        ImageView close = Ui.icon(this, R.drawable.ic_close, Ui.text, 44, L.t("close"));
        close.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { closeFind(); } });
        findBar.addView(up);
        findBar.addView(down);
        findBar.addView(close);
    }

    private int dp(float v) { return Ui.dp(this, v); }

    private void updateBars() {
        if (cur == null) return;
        boolean priv = cur.incognito;
        int barBg = priv ? Ui.privateBg : Ui.surface;
        int fg = priv ? Ui.privateText : Ui.text;
        int fg2 = priv ? 0xFFB9AEDB : Ui.text2;
        topBar.setBackgroundColor(barBg);
        addressBox.setBackground(Ui.round(priv ? 0xFF3A3256 : Ui.surface2, dp(22)));
        urlEdit.setTextColor(fg);
        urlEdit.setHintTextColor(fg2);
        getWindow().setStatusBarColor(barBg);
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (!priv && !Ui.dark) flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            else flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        if (!urlEdit.hasFocus()) {
            if (cur.home || cur.web == null) {
                urlEdit.setText("");
                urlEdit.setHint(priv ? L.t("private_title") + " · " + L.t("search_hint") : L.t("search_hint"));
                siteIcon.setImageResource(priv ? R.drawable.ic_private : R.drawable.ic_search);
                siteIcon.setColorFilter(fg2);
                actionIcon.setVisibility(View.GONE);
            } else {
                String u = cur.url == null ? "" : cur.url;
                if (cur.offlineView) {
                    urlEdit.setText(L.t("offline_badge") + " · " + cur.title);
                    siteIcon.setImageResource(R.drawable.ic_save);
                } else {
                    urlEdit.setText(Ui.hostOf(u));
                    if (priv) siteIcon.setImageResource(R.drawable.ic_private);
                    else siteIcon.setImageResource(u.startsWith("https://") ? R.drawable.ic_lock : R.drawable.ic_info);
                }
                siteIcon.setColorFilter(u.startsWith("http://") && !cur.offlineView ? 0xFFD32F2F : fg2);
                siteIcon.setContentDescription(u.startsWith("http://") ? L.t("not_secure") : "");
                actionIcon.setVisibility(View.VISIBLE);
            }
        }
        actionIcon.setColorFilter(fg2);
        tabCount.setText(String.valueOf(tabs.size()));
        boolean canBack = cur.web != null && (!cur.home && (cur.web.canGoBack() || cur.parent != null || true));
        backBtn.setAlpha(cur.home && cur.parent == null && tabs.size() <= 1 ? 0.35f : 1f);
        boolean canFwd = cur.web != null && (cur.home ? true : cur.web.canGoForward());
        fwdBtn.setAlpha(canFwd ? 1f : 0.35f);
        CookieManager.getInstance().setAcceptCookie(!priv);
    }

    // ------------------------------------------------------------------ home screen

    private void showHome() {
        if (cur == null) return;
        if (cur.web != null && !cur.home) captureThumb(cur);
        cur.home = true;
        closeFind();
        buildHome();
        homeScroll.setVisibility(View.VISIBLE);
        webHolder.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        updateBars();
    }

    private void buildHome() {
        homeScroll.removeAllViews();
        homeScroll.setBackgroundColor(cur.incognito ? 0xFF1C1830 : Ui.bg);
        int fg = cur.incognito ? Ui.privateText : Ui.text;
        int fg2 = cur.incognito ? 0xFFB9AEDB : Ui.text2;
        int card = cur.incognito ? 0xFF2B2440 : Ui.surface;

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(20), dp(28), dp(20), dp(24));
        homeScroll.addView(col, new FrameLayout.LayoutParams(-1, -2));

        ImageView logo = new ImageView(this);
        if (cur.incognito) {
            logo.setImageResource(R.drawable.ic_private);
            logo.setColorFilter(Ui.privateText);
            logo.setPadding(dp(14), dp(14), dp(14), dp(14));
            logo.setBackground(Ui.round(0xFF3A3256, dp(40)));
        } else {
            logo.setImageResource(R.drawable.logo);
        }
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(dp(76), dp(76));
        llp.gravity = Gravity.CENTER_HORIZONTAL;
        col.addView(logo, llp);

        TextView title = Ui.text(this, cur.incognito ? L.t("private_title") : L.t("greet"), 22, fg, true);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(-1, -2);
        tl.topMargin = dp(14);
        col.addView(title, tl);
        TextView sub = Ui.text(this, cur.incognito ? L.t("private_info") : L.t("greet_sub"), 14, fg2, false);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(-1, -2);
        sl.topMargin = dp(4);
        col.addView(sub, sl);

        // search box
        LinearLayout sb = new LinearLayout(this);
        sb.setOrientation(LinearLayout.HORIZONTAL);
        sb.setGravity(Gravity.CENTER_VERTICAL);
        sb.setPadding(dp(16), 0, dp(16), 0);
        sb.setBackground(Ui.ripple(Ui.roundStroke(card, dp(26), dp(1), cur.incognito ? 0xFF4A4170 : Ui.divider), Ui.round(Color.WHITE, dp(26))));
        sb.setElevation(dp(2));
        ImageView si = new ImageView(this);
        si.setImageResource(R.drawable.ic_search);
        si.setColorFilter(Ui.accent);
        sb.addView(si, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView sh = Ui.single(this, L.t("search_hint"), 16, fg2, false);
        sh.setPadding(dp(12), 0, 0, 0);
        sb.addView(sh, new LinearLayout.LayoutParams(0, -2, 1));
        TextView eng = Ui.single(this, Prefs.engineName(this), 12, Ui.accent, true);
        sb.addView(eng);
        sb.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { focusAddress(); }
        });
        LinearLayout.LayoutParams sbl = new LinearLayout.LayoutParams(-1, dp(52));
        sbl.topMargin = dp(22);
        col.addView(sb, sbl);

        if (!isOnline()) {
            TextView off = Ui.text(this, L.t("no_internet"), 13, 0xFF8A4B00, false);
            off.setBackground(Ui.round(0xFFFFE8CC, dp(12)));
            off.setPadding(dp(14), dp(10), dp(14), dp(10));
            off.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLibrary(LibraryActivity.MODE_OFFLINE); } });
            LinearLayout.LayoutParams ol = new LinearLayout.LayoutParams(-1, -2);
            ol.topMargin = dp(14);
            col.addView(off, ol);
        }

        // speed dial
        TextView sdT = Ui.text(this, L.t("shortcuts"), 15, fg, true);
        LinearLayout.LayoutParams sdl = new LinearLayout.LayoutParams(-1, -2);
        sdl.topMargin = dp(26);
        sdl.bottomMargin = dp(10);
        col.addView(sdT, sdl);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        List<Db.Item> sd = Db.get(this).speedDial();
        for (final Db.Item it : sd) {
            grid.addView(dialCell(it.title, it.url, fg, card, new View.OnClickListener() {
                @Override public void onClick(View v) { load(cur, it.url); }
            }, new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) { confirmRemoveDial(it); return true; }
            }, 0), cellLp());
        }
        if (sd.size() < 16) {
            grid.addView(dialCell(L.t("add_shortcut"), null, fg, card, new View.OnClickListener() {
                @Override public void onClick(View v) { addDialDialog(); }
            }, null, R.drawable.ic_add), cellLp());
        }
        col.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        // quick library row
        LinearLayout quick = new LinearLayout(this);
        quick.setOrientation(LinearLayout.HORIZONTAL);
        quick.setBackground(Ui.round(card, dp(16)));
        quick.setPadding(dp(4), dp(10), dp(4), dp(10));
        int[] qi = {R.drawable.ic_bookmark, R.drawable.ic_history, R.drawable.ic_download, R.drawable.ic_save};
        String[] qt = {L.t("bookmarks"), L.t("history"), L.t("downloads"), L.t("offline")};
        final int[] qm = {LibraryActivity.MODE_BOOKMARKS, LibraryActivity.MODE_HISTORY, LibraryActivity.MODE_DOWNLOADS, LibraryActivity.MODE_OFFLINE};
        for (int i = 0; i < 4; i++) {
            final int mode = qm[i];
            LinearLayout q = new LinearLayout(this);
            q.setOrientation(LinearLayout.VERTICAL);
            q.setGravity(Gravity.CENTER_HORIZONTAL);
            q.setBackground(Ui.rippleRound(Color.TRANSPARENT, dp(12)));
            q.setPadding(0, dp(6), 0, dp(6));
            ImageView iv = new ImageView(this);
            iv.setImageResource(qi[i]);
            iv.setColorFilter(Ui.accent);
            q.addView(iv, new LinearLayout.LayoutParams(dp(26), dp(26)));
            TextView tv = Ui.single(this, qt[i], 12, fg, false);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp(2), dp(6), dp(2), 0);
            q.addView(tv, new LinearLayout.LayoutParams(-1, -2));
            q.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { openLibrary(mode); } });
            quick.addView(q, new LinearLayout.LayoutParams(0, -2, 1));
        }
        LinearLayout.LayoutParams ql = new LinearLayout.LayoutParams(-1, -2);
        ql.topMargin = dp(18);
        col.addView(quick, ql);

        // recent
        if (!cur.incognito) {
            List<Db.Item> recent = Db.get(this).history(4);
            if (!recent.isEmpty()) {
                TextView rt = Ui.text(this, L.t("recent"), 15, fg, true);
                LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(-1, -2);
                rl.topMargin = dp(24);
                rl.bottomMargin = dp(6);
                col.addView(rt, rl);
                LinearLayout box = new LinearLayout(this);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setBackground(Ui.round(card, dp(16)));
                for (final Db.Item it : recent) {
                    LinearLayout row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(14), dp(10), dp(14), dp(10));
                    row.setBackground(Ui.rippleRound(Color.TRANSPARENT, dp(16)));
                    row.addView(Ui.letterTile(this, Ui.hostOf(it.url), it.url, 32));
                    LinearLayout tx = new LinearLayout(this);
                    tx.setOrientation(LinearLayout.VERTICAL);
                    tx.setPadding(dp(12), 0, 0, 0);
                    tx.addView(Ui.single(this, it.title, 14, fg, false));
                    tx.addView(Ui.single(this, Ui.hostOf(it.url), 12, fg2, false));
                    row.addView(tx, new LinearLayout.LayoutParams(0, -2, 1));
                    row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { load(cur, it.url); } });
                    box.addView(row, new LinearLayout.LayoutParams(-1, -2));
                }
                col.addView(box, new LinearLayout.LayoutParams(-1, -2));
            }
        }
    }

    private GridLayout.LayoutParams cellLp() {
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        lp.setMargins(0, dp(4), 0, dp(10));
        return lp;
    }

    private View dialCell(String title, String url, int fg, int card, View.OnClickListener click, View.OnLongClickListener longClick, int iconRes) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(dp(2), dp(6), dp(2), dp(6));
        c.setBackground(Ui.rippleRound(Color.TRANSPARENT, dp(14)));
        View tile;
        if (iconRes != 0) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(iconRes);
            iv.setColorFilter(Ui.accent);
            iv.setPadding(dp(14), dp(14), dp(14), dp(14));
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(card);
            g.setStroke(dp(1), Ui.accent);
            iv.setBackground(g);
            iv.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
            tile = iv;
        } else {
            tile = Ui.letterTile(this, title, url, 52);
        }
        c.addView(tile);
        TextView t = Ui.single(this, title, 12, fg, false);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(6), 0, 0);
        c.addView(t, new LinearLayout.LayoutParams(-1, -2));
        c.setOnClickListener(click);
        if (longClick != null) c.setOnLongClickListener(longClick);
        return c;
    }

    private void confirmRemoveDial(final Db.Item it) {
        new AlertDialog.Builder(this).setTitle(L.t("remove_shortcut")).setMessage(it.title)
                .setPositiveButton(L.t("delete"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { Db.get(MainActivity.this).removeSpeedDial(it.id); buildHome(); }
                }).setNegativeButton(L.t("cancel"), null).show();
    }

    private void addDialDialog() {
        LinearLayout f = new LinearLayout(this);
        f.setOrientation(LinearLayout.VERTICAL);
        f.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText name = new EditText(this);
        name.setHint(L.t("shortcut_name"));
        name.setSingleLine(true);
        final EditText url = new EditText(this);
        url.setHint(L.t("shortcut_url"));
        url.setSingleLine(true);
        url.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        if (cur != null && cur.web != null && cur.url != null && cur.url.startsWith("http")) {
            url.setText(cur.url);
            name.setText(cur.title);
        }
        f.addView(name);
        f.addView(url);
        new AlertDialog.Builder(this).setTitle(L.t("add_shortcut")).setView(f)
                .setPositiveButton(L.t("ok"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        String u = url.getText().toString().trim();
                        String n = name.getText().toString().trim();
                        if (u.isEmpty()) return;
                        u = toUrl(u);
                        if (n.isEmpty()) n = Ui.hostOf(u);
                        Db.get(MainActivity.this).addSpeedDial(n, u);
                        buildHome();
                    }
                }).setNegativeButton(L.t("cancel"), null).show();
    }

    private void focusAddress() {
        urlEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(urlEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
    }

    private boolean isOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return true;
            Network n = cm.getActiveNetwork();
            if (n == null) return false;
            NetworkCapabilities nc = cm.getNetworkCapabilities(n);
            return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        } catch (Exception e) {
            return true;
        }
    }

    // ------------------------------------------------------------------ tabs

    private Tab newTab(boolean incognito, boolean focusSearch) {
        Tab t = new Tab(incognito);
        tabs.add(t);
        if (focusSearch) {
            root.post(new Runnable() { @Override public void run() { focusAddress(); } });
        }
        return t;
    }

    private void switchTo(Tab t) {
        if (cur != null && cur != t) {
            if (cur.web != null) {
                captureThumb(cur);
                cur.web.onPause();
            }
        }
        cur = t;
        webHolder.removeAllViews();
        closeFind();
        if (!t.home && t.web == null && t.url != null && !t.url.isEmpty()) {
            String u = t.url;
            t.url = "";
            load(t, u);
            return;
        }
        if (t.web != null) {
            ViewGroup p = (ViewGroup) t.web.getParent();
            if (p != null) p.removeView(t.web);
            webHolder.addView(t.web, new FrameLayout.LayoutParams(-1, -1));
            t.web.onResume();
        }
        if (t.home || t.web == null) showHome();
        else {
            homeScroll.setVisibility(View.GONE);
            webHolder.setVisibility(View.VISIBLE);
        }
        updateBars();
    }

    private void closeTab(Tab t) {
        int idx = tabs.indexOf(t);
        if (idx < 0) return;
        tabs.remove(idx);
        boolean wasPrivate = t.incognito;
        destroyWeb(t);
        for (Tab o : tabs) if (o.parent == t) o.parent = null;
        if (wasPrivate) {
            boolean anyPrivate = false;
            for (Tab o : tabs) if (o.incognito) anyPrivate = true;
            if (!anyPrivate) clearPrivateTraces();
        }
        if (tabs.isEmpty()) {
            cur = null;
            switchTo(newTab(false, false));
        } else if (t == cur) {
            cur = null;
            Tab next = t.parent != null && tabs.contains(t.parent) ? t.parent : tabs.get(Math.max(0, idx - 1));
            switchTo(next);
        }
        updateBars();
    }

    private void clearPrivateTraces() {
        WebView w = new WebView(this);
        w.clearCache(true);
        w.destroy();
    }

    private void destroyWeb(Tab t) {
        if (t.web == null) return;
        ViewGroup p = (ViewGroup) t.web.getParent();
        if (p != null) p.removeView(t.web);
        t.web.stopLoading();
        t.web.destroy();
        t.web = null;
    }

    private void openInNewTab(String url, boolean incognito) {
        Tab t = newTab(incognito, false);
        t.parent = cur;
        switchTo(t);
        load(t, url);
    }

    private void captureThumb(Tab t) {
        try {
            if (t.web == null || t.web.getWidth() == 0) return;
            int w = t.web.getWidth(), h = t.web.getHeight();
            float s = 0.45f;
            Bitmap b = Bitmap.createBitmap((int) (w * s), (int) (h * s), Bitmap.Config.RGB_565);
            Canvas c = new Canvas(b);
            c.scale(s, s);
            c.translate(-t.web.getScrollX(), -t.web.getScrollY());
            t.web.draw(c);
            t.thumb = b;
        } catch (Throwable ignored) { }
    }

    private void showTabs() {
        if (cur != null && cur.web != null && !cur.home) captureThumb(cur);
        hideKeyboard();
        tabSwitcher.removeAllViews();
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        tabSwitcher.addView(col, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout head = new LinearLayout(this);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setBackgroundColor(Ui.surface);
        head.setPadding(dp(4), 0, dp(8), 0);
        ImageView back = Ui.icon(this, R.drawable.ic_back, Ui.text, 48, L.t("back"));
        back.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { hideTabs(); } });
        head.addView(back);
        TextView ht = Ui.single(this, L.t("tabs") + " (" + tabs.size() + ")", 20, Ui.text, true);
        ht.setPadding(dp(8), 0, 0, 0);
        head.addView(ht, new LinearLayout.LayoutParams(0, -2, 1));
        TextView ca = Ui.text(this, L.t("close_all"), 14, Ui.accent, true);
        ca.setPadding(dp(12), dp(10), dp(12), dp(10));
        ca.setBackground(Ui.rippleRound(Color.TRANSPARENT, dp(18)));
        ca.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                for (Tab t : new ArrayList<>(tabs)) destroyWeb(t);
                tabs.clear();
                clearPrivateTraces();
                cur = null;
                switchTo(newTab(false, false));
                hideTabs();
            }
        });
        head.addView(ca);
        getWindow().setStatusBarColor(Ui.surface);
        col.addView(head, new LinearLayout.LayoutParams(-1, dp(56)));

        ScrollView sc = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(10), dp(10), dp(10), dp(10));
        sc.addView(list);
        col.addView(sc, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout row = null;
        for (int i = 0; i < tabs.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                list.addView(row, new LinearLayout.LayoutParams(-1, -2));
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(250), 1);
            lp.setMargins(dp(6), dp(6), dp(6), dp(6));
            row.addView(tabCard(tabs.get(i)), lp);
        }
        if (tabs.size() % 2 == 1 && row != null) {
            View sp = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(250), 1);
            lp.setMargins(dp(6), dp(6), dp(6), dp(6));
            row.addView(sp, lp);
        }

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setPadding(dp(12), dp(10), dp(12), dp(14));
        bottom.setBackgroundColor(Ui.surface);
        bottom.addView(pillButton(L.t("new_tab"), R.drawable.ic_add, Ui.accent, Color.WHITE, new View.OnClickListener() {
            @Override public void onClick(View v) { hideTabs(); switchTo(newTab(false, true)); }
        }), pillLp());
        bottom.addView(pillButton(L.t("new_private"), R.drawable.ic_private, Ui.privateBg, Ui.privateText, new View.OnClickListener() {
            @Override public void onClick(View v) { hideTabs(); switchTo(newTab(true, true)); }
        }), pillLp());
        col.addView(bottom, new LinearLayout.LayoutParams(-1, -2));
        tabSwitcher.setVisibility(View.VISIBLE);
    }

    private LinearLayout.LayoutParams pillLp() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(48), 1);
        lp.setMargins(dp(6), 0, dp(6), 0);
        return lp;
    }

    private View pillButton(String label, int icon, int bg, int fg, View.OnClickListener l) {
        LinearLayout b = new LinearLayout(this);
        b.setOrientation(LinearLayout.HORIZONTAL);
        b.setGravity(Gravity.CENTER);
        b.setBackground(Ui.rippleRound(bg, dp(24)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon);
        iv.setColorFilter(fg);
        b.addView(iv, new LinearLayout.LayoutParams(dp(20), dp(20)));
        TextView t = Ui.single(this, label, 14, fg, true);
        t.setPadding(dp(8), 0, 0, 0);
        b.addView(t);
        b.setOnClickListener(l);
        return b;
    }

    private View tabCard(final Tab t) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int bg = t.incognito ? Ui.privateBg : Ui.surface;
        int fg = t.incognito ? Ui.privateText : Ui.text;
        int stroke = t == cur ? Ui.accent : (t.incognito ? 0xFF4A4170 : Ui.divider);
        card.setBackground(Ui.roundStroke(bg, dp(14), dp(t == cur ? 2 : 1), stroke));
        card.setElevation(dp(2));
        card.setClipToOutline(true);
        card.setPadding(dp(1), dp(1), dp(1), dp(1));

        LinearLayout h = new LinearLayout(this);
        h.setOrientation(LinearLayout.HORIZONTAL);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setPadding(dp(10), 0, 0, 0);
        if (t.incognito) {
            ImageView pi = new ImageView(this);
            pi.setImageResource(R.drawable.ic_private);
            pi.setColorFilter(Ui.privateText);
            h.addView(pi, new LinearLayout.LayoutParams(dp(18), dp(18)));
        } else if (t.home || t.web == null) {
            ImageView pi = new ImageView(this);
            pi.setImageResource(R.drawable.ic_home);
            pi.setColorFilter(Ui.accent);
            h.addView(pi, new LinearLayout.LayoutParams(dp(18), dp(18)));
        } else {
            h.addView(Ui.letterTile(this, Ui.hostOf(t.url), t.url, 18));
        }
        TextView tt = Ui.single(this, t.incognito && (t.home || t.web == null) ? L.t("private_title") : t.displayTitle(), 13, fg, true);
        tt.setPadding(dp(8), 0, 0, 0);
        h.addView(tt, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView x = Ui.icon(this, R.drawable.ic_close, fg, 40, L.t("close"));
        x.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { closeTab(t); showTabs(); }
        });
        h.addView(x);
        card.addView(h, new LinearLayout.LayoutParams(-1, dp(40)));

        FrameLayout body = new FrameLayout(this);
        body.setBackgroundColor(t.incognito ? 0xFF1C1830 : Ui.bg);
        if (!t.home && t.thumb != null) {
            ImageView th = new ImageView(this);
            th.setImageBitmap(t.thumb);
            th.setScaleType(ImageView.ScaleType.MATRIX);
            float scale = 1f;
            th.setScaleType(ImageView.ScaleType.CENTER_CROP);
            body.addView(th, new FrameLayout.LayoutParams(-1, -1));
        } else {
            ImageView lg = new ImageView(this);
            if (t.incognito) { lg.setImageResource(R.drawable.ic_private); lg.setColorFilter(Ui.privateText); }
            else lg.setImageResource(R.drawable.logo);
            body.addView(lg, new FrameLayout.LayoutParams(dp(56), dp(56), Gravity.CENTER));
        }
        card.addView(body, new LinearLayout.LayoutParams(-1, 0, 1));
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { hideTabs(); switchTo(t); }
        });
        return card;
    }

    private void hideTabs() {
        tabSwitcher.setVisibility(View.GONE);
        tabSwitcher.removeAllViews();
        updateBars();
    }

    // ------------------------------------------------------------------ web

    static boolean looksLikeUrl(String s) {
        if (s.contains(" ")) return false;
        if (s.startsWith("http://") || s.startsWith("https://")) return true;
        if (s.startsWith("localhost")) return true;
        return Patterns.WEB_URL.matcher(s).matches() && s.contains(".");
    }

    String toUrl(String s) {
        s = s.trim();
        String low = s.toLowerCase();
        if (low.startsWith("http://") || low.startsWith("https://")) return s;
        if (low.startsWith("javascript:") || low.startsWith("file:") || low.startsWith("content:")) return Prefs.searchUrl(this, s);
        if (looksLikeUrl(s)) return "https://" + s;
        return Prefs.searchUrl(this, s);
    }

    private void load(Tab t, String url) {
        if (t == null) return;
        if (t.web == null) createWeb(t);
        t.home = false;
        t.offlineView = false;
        t.url = url;
        applySettings(t);
        t.web.loadUrl(url);
        if (t == cur) {
            homeScroll.setVisibility(View.GONE);
            webHolder.setVisibility(View.VISIBLE);
            if (t.web.getParent() == null) webHolder.addView(t.web, new FrameLayout.LayoutParams(-1, -1));
            updateBars();
        }
    }

    void openOffline(Tab t, String path, String title, String origUrl) {
        if (t.web == null) createWeb(t);
        t.home = false;
        t.offlineView = true;
        t.title = title;
        t.url = origUrl;
        applySettings(t);
        t.web.loadUrl(Uri.fromFile(new File(path)).toString());
        if (t == cur) {
            homeScroll.setVisibility(View.GONE);
            webHolder.setVisibility(View.VISIBLE);
            if (t.web.getParent() == null) webHolder.addView(t.web, new FrameLayout.LayoutParams(-1, -1));
            updateBars();
        }
    }

    private void applySettings(Tab t) {
        WebSettings s = t.web.getSettings();
        s.setJavaScriptEnabled(!t.offlineView && Prefs.js(this));
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(!t.incognito);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(t.desktop);
        s.setMediaPlaybackRequiresUserGesture(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptCanOpenWindowsAutomatically(!Prefs.blockPopups(this));
        s.setAllowFileAccess(t.offlineView);
        s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setGeolocationEnabled(false);
        s.setSaveFormData(false);
        s.setCacheMode(t.incognito ? WebSettings.LOAD_NO_CACHE : WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setUserAgentString(t.desktop ? DESKTOP_UA : null);
        if (Build.VERSION.SDK_INT >= 33) {
            s.setAlgorithmicDarkeningAllowed(Ui.dark);
        } else if (Build.VERSION.SDK_INT >= 29) {
            s.setForceDark(Ui.dark ? WebSettings.FORCE_DARK_AUTO : WebSettings.FORCE_DARK_OFF);
        }
        CookieManager.getInstance().setAcceptThirdPartyCookies(t.web, !t.incognito);
    }

    private void createWeb(final Tab t) {
        final WebView w = new WebView(this);
        t.web = w;
        t.desktop = Prefs.desktopDefault(this);
        w.setBackgroundColor(Ui.dark ? Color.BLACK : Color.WHITE);
        applySettings(t);
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String u = req.getUrl().toString();
                String sc = req.getUrl().getScheme();
                if ("http".equals(sc) || "https".equals(sc)) return false;
                if ("about".equals(sc) || "blob".equals(sc) || "data".equals(sc)) return false;
                openExternal(u, view);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    if (t.offlineView) {
                        t.offlineView = false;
                        applySettings(t);
                    }
                    t.url = url;
                }
                if (t == cur) {
                    progress.setVisibility(View.VISIBLE);
                    actionIcon.setImageResource(R.drawable.ic_close);
                    updateBars();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (t == cur) {
                    progress.setVisibility(View.GONE);
                    if (!urlEdit.hasFocus()) actionIcon.setImageResource(R.drawable.ic_refresh);
                    updateBars();
                }
                if (!t.incognito && !t.offlineView && url != null) Db.get(MainActivity.this).addHistory(view.getTitle(), url);
            }
        });
        w.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int p) {
                if (t == cur) progress.setProgress(p);
            }

            @Override public void onReceivedTitle(WebView view, String title) {
                if (!t.offlineView) t.title = title;
                if (!t.incognito && !t.offlineView) Db.get(MainActivity.this).updateHistoryTitle(view.getUrl(), title);
            }

            @Override public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                if (Prefs.blockPopups(MainActivity.this) && !isUserGesture) return false;
                Tab nt = newTab(t.incognito, false);
                nt.parent = t;
                createWeb(nt);
                nt.home = false;
                switchTo(nt);
                WebView.WebViewTransport tr = (WebView.WebViewTransport) resultMsg.obj;
                tr.setWebView(nt.web);
                resultMsg.sendToTarget();
                return true;
            }

            @Override public void onCloseWindow(WebView window) {
                for (Tab o : new ArrayList<>(tabs)) if (o.web == window) closeTab(o);
            }

            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, false, false);
            }

            @Override public void onPermissionRequest(PermissionRequest request) {
                request.deny();
            }

            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = cb;
                try {
                    startActivityForResult(params.createIntent(), REQ_FILE);
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }

            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView = view;
                customCallback = callback;
                fullscreenHolder.addView(view, new FrameLayout.LayoutParams(-1, -1));
                fullscreenHolder.setVisibility(View.VISIBLE);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            @Override public void onHideCustomView() { exitFullscreen(); }
        });
        w.setDownloadListener(new DownloadListener() {
            @Override public void onDownloadStart(String url, String ua, String cd, String mime, long len) {
                askDownload(t, url, ua, cd, mime, len);
            }
        });
        w.setFindListener(new WebView.FindListener() {
            @Override public void onFindResultReceived(int active, int count, boolean done) {
                if (count == 0) findCount.setText(findEdit.getText().length() > 0 ? L.t("no_match") : "");
                else findCount.setText(String.format(L.t("matches"), active + 1, count));
            }
        });
        w.setOnLongClickListener(new View.OnLongClickListener() {
            @Override public boolean onLongClick(View v) { return onWebLongPress(t); }
        });
    }

    private void exitFullscreen() {
        if (customView == null) return;
        fullscreenHolder.removeView(customView);
        fullscreenHolder.setVisibility(View.GONE);
        customView = null;
        if (customCallback != null) customCallback.onCustomViewHidden();
        customCallback = null;
        getWindow().getDecorView().setSystemUiVisibility(0);
        updateBars();
    }

    private boolean onWebLongPress(final Tab t) {
        WebView.HitTestResult r = t.web.getHitTestResult();
        if (r == null) return false;
        final String extra = r.getExtra();
        if (extra == null) return false;
        int type = r.getType();
        if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE || type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
            String link = extra;
            if (type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                Message m = new Message();
                t.web.requestFocusNodeHref(m);
                String href = m.getData() == null ? null : m.getData().getString("url");
                if (href != null && !href.isEmpty()) link = href;
            }
            final String fl = link;
            String[] items = {L.t("open_new_tab"), L.t("new_private"), L.t("copy_link"), L.t("share")};
            new AlertDialog.Builder(this).setTitle(fl).setItems(items, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int which) {
                    if (which == 0) openInNewTab(fl, t.incognito);
                    else if (which == 1) openInNewTab(fl, true);
                    else if (which == 2) copy(fl);
                    else shareText(fl, null);
                }
            }).show();
            return true;
        }
        if (type == WebView.HitTestResult.IMAGE_TYPE) {
            String[] items = {L.t("open_new_tab"), L.t("download"), L.t("copy_link")};
            new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int which) {
                    if (which == 0) openInNewTab(extra, t.incognito);
                    else if (which == 1) askDownload(t, extra, t.web.getSettings().getUserAgentString(), null, null, -1);
                    else copy(extra);
                }
            }).show();
            return true;
        }
        return false;
    }

    private void openExternal(final String url, WebView view) {
        Intent i;
        try {
            if (url.startsWith("intent:")) {
                i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                i.addCategory(Intent.CATEGORY_BROWSABLE);
                i.setComponent(null);
                i.setSelector(null);
            } else {
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }
        } catch (Exception e) {
            return;
        }
        final Intent fi = i;
        final String fallback = url.startsWith("intent:") ? i.getStringExtra("browser_fallback_url") : null;
        if (fi.resolveActivity(getPackageManager()) == null && fallback != null) {
            if (fallback.startsWith("http")) view.loadUrl(fallback);
            return;
        }
        new AlertDialog.Builder(this).setTitle(L.t("external_q")).setMessage(url.length() > 120 ? url.substring(0, 120) + "…" : url)
                .setPositiveButton(L.t("open"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        try { startActivity(fi); } catch (Exception e) { toast(L.t("cant_open")); }
                    }
                }).setNegativeButton(L.t("cancel"), null).show();
    }

    // ------------------------------------------------------------------ downloads & offline

    private void askDownload(final Tab t, final String url, final String ua, final String cd, final String mime, long len) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) {
            toast(L.t("download_failed"));
            return;
        }
        final String name = URLUtil.guessFileName(url, cd, mime);
        String size = len > 0 ? " (" + Formatter.formatShortFileSize(this, len) + ")" : "";
        new AlertDialog.Builder(this).setTitle(L.t("download_q")).setMessage(name + size)
                .setPositiveButton(L.t("download"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { startDownload(t, url, ua, mime, name); }
                }).setNegativeButton(L.t("cancel"), null).show();
    }

    private void startDownload(Tab t, String url, String ua, String mime, String name) {
        try {
            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
            if (mime != null) r.setMimeType(mime);
            if (ua != null) r.addRequestHeader("User-Agent", ua);
            if (!t.incognito) {
                String ck = CookieManager.getInstance().getCookie(url);
                if (ck != null) r.addRequestHeader("Cookie", ck);
            }
            r.setTitle(name);
            r.setDescription(Ui.hostOf(url));
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            if (Build.VERSION.SDK_INT >= 29) {
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
            } else {
                // App-specific folder: no storage permission needed on Android 7-9.
                r.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, name);
            }
            long id = dm.enqueue(r);
            Db.get(this).addDownload(name, url, id);
            toast(L.t("download_started"));
        } catch (Exception e) {
            toast(L.t("download_failed"));
        }
    }

    private void saveOffline() {
        final Tab t = cur;
        if (t == null || t.web == null || t.home || t.offlineView || t.url == null || !t.url.startsWith("http")) {
            toast(L.t("open_page_first"));
            return;
        }
        File dir = new File(getFilesDir(), "offline");
        if (!dir.exists()) dir.mkdirs();
        final File f = new File(dir, "page_" + System.currentTimeMillis() + ".mht");
        final String title = t.title == null || t.title.isEmpty() ? Ui.hostOf(t.url) : t.title;
        final String url = t.url;
        t.web.saveWebArchive(f.getAbsolutePath(), false, new ValueCallback<String>() {
            @Override public void onReceiveValue(String path) {
                if (path != null && new File(path).length() > 0) {
                    Db.get(MainActivity.this).addOffline(title, url, path);
                    toast(L.t("saved_offline"));
                } else {
                    toast(L.t("save_failed"));
                }
            }
        });
    }

    // ------------------------------------------------------------------ menu

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { showMenu(); return true; }
        return super.onKeyUp(keyCode, event);
    }

    private void showMenu() {
        hideKeyboard();
        if (menuDialog != null && menuDialog.isShowing()) menuDialog.dismiss();
        final Dialog d = new Dialog(this);
        menuDialog = d;
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable g = new GradientDrawable();
        g.setColor(Ui.surface);
        float r = dp(22);
        g.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        sheet.setBackground(g);
        sheet.setPadding(dp(12), dp(10), dp(12), dp(14));

        View handle = new View(this);
        handle.setBackground(Ui.round(Ui.divider, dp(3)));
        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(dp(40), dp(5));
        hl.gravity = Gravity.CENTER_HORIZONTAL;
        hl.bottomMargin = dp(8);
        sheet.addView(handle, hl);

        final boolean page = cur != null && cur.web != null && !cur.home;
        final boolean pageHttp = page && !cur.offlineView && cur.url != null && cur.url.startsWith("http");
        // quick row
        LinearLayout q = new LinearLayout(this);
        q.setOrientation(LinearLayout.HORIZONTAL);
        q.setGravity(Gravity.CENTER_VERTICAL);
        final boolean bm = pageHttp && Db.get(this).isBookmarked(cur.url);
        int[] qi = {R.drawable.ic_back, R.drawable.ic_forward, bm ? R.drawable.ic_star_on : R.drawable.ic_star, R.drawable.ic_refresh, R.drawable.ic_share};
        String[] qd = {L.t("back"), L.t("forward"), L.t("add_bookmark"), L.t("reload"), L.t("share")};
        for (int i = 0; i < qi.length; i++) {
            final int idx = i;
            ImageView iv = Ui.icon(this, qi[i], i == 2 && bm ? Ui.accent : Ui.text, 48, qd[i]);
            boolean enabled = i == 0 || (i == 1 ? (cur != null && cur.web != null && (cur.home || cur.web.canGoForward())) : pageHttp || (i == 3 && page));
            iv.setAlpha(enabled ? 1f : 0.35f);
            iv.setEnabled(enabled);
            iv.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    d.dismiss();
                    if (idx == 0) goBack();
                    else if (idx == 1) goForward();
                    else if (idx == 2) toggleBookmark();
                    else if (idx == 3) cur.web.reload();
                    else shareText(cur.url, cur.title);
                }
            });
            FrameLayout wrap = new FrameLayout(this);
            wrap.addView(iv, new FrameLayout.LayoutParams(dp(48), dp(48), Gravity.CENTER));
            q.addView(wrap, new LinearLayout.LayoutParams(0, dp(52), 1));
        }
        sheet.addView(q, new LinearLayout.LayoutParams(-1, -2));
        sheet.addView(Ui.divider(this));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setPadding(0, dp(8), 0, 0);
        addMenuItem(d, grid, R.drawable.ic_add, L.t("new_tab"), false, new Runnable() { public void run() { switchTo(newTab(false, true)); } });
        addMenuItem(d, grid, R.drawable.ic_private, L.t("new_private"), false, new Runnable() { public void run() { switchTo(newTab(true, true)); } });
        addMenuItem(d, grid, R.drawable.ic_bookmark, L.t("bookmarks"), false, new Runnable() { public void run() { openLibrary(LibraryActivity.MODE_BOOKMARKS); } });
        addMenuItem(d, grid, R.drawable.ic_history, L.t("history"), false, new Runnable() { public void run() { openLibrary(LibraryActivity.MODE_HISTORY); } });
        addMenuItem(d, grid, R.drawable.ic_download, L.t("downloads"), false, new Runnable() { public void run() { openLibrary(LibraryActivity.MODE_DOWNLOADS); } });
        addMenuItem(d, grid, R.drawable.ic_folder, L.t("offline"), false, new Runnable() { public void run() { openLibrary(LibraryActivity.MODE_OFFLINE); } });
        addMenuItem(d, grid, R.drawable.ic_save, L.t("save_offline"), false, new Runnable() { public void run() { saveOffline(); } });
        addMenuItem(d, grid, R.drawable.ic_search, L.t("find"), false, new Runnable() { public void run() { openFind(); } });
        addMenuItem(d, grid, R.drawable.ic_desktop, L.t("desktop"), page && cur.desktop, new Runnable() { public void run() { toggleDesktop(); } });
        addMenuItem(d, grid, R.drawable.ic_tabs, L.t("tabs"), false, new Runnable() { public void run() { showTabs(); } });
        addMenuItem(d, grid, R.drawable.ic_settings, L.t("settings"), false, new Runnable() { public void run() { startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), REQ_SETTINGS); } });
        addMenuItem(d, grid, R.drawable.ic_info, L.t("about"), false, new Runnable() { public void run() { startActivity(new Intent(MainActivity.this, AboutActivity.class)); } });
        sheet.addView(grid, new LinearLayout.LayoutParams(-1, -2));

        d.setContentView(sheet);
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            w.setGravity(Gravity.BOTTOM);
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setDimAmount(0.35f);
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        d.show();
    }

    private void addMenuItem(final Dialog d, GridLayout grid, int icon, String label, boolean active, final Runnable action) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setGravity(Gravity.CENTER_HORIZONTAL);
        c.setPadding(dp(2), dp(10), dp(2), dp(10));
        c.setBackground(Ui.rippleRound(Color.TRANSPARENT, dp(14)));
        ImageView iv = new ImageView(this);
        iv.setImageResource(icon);
        iv.setColorFilter(active ? Color.WHITE : Ui.accent);
        iv.setPadding(dp(11), dp(11), dp(11), dp(11));
        iv.setBackground(Ui.round(active ? Ui.accent : (Ui.dark ? 0xFF33261C : 0xFFFDEBDF), dp(23)));
        c.addView(iv, new LinearLayout.LayoutParams(dp(46), dp(46)));
        TextView t = Ui.text(this, label, 12, Ui.text, false);
        t.setGravity(Gravity.CENTER);
        t.setMaxLines(2);
        t.setPadding(0, dp(6), 0, 0);
        c.addView(t, new LinearLayout.LayoutParams(-1, -2));
        c.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { d.dismiss(); action.run(); }
        });
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
        lp.width = 0;
        grid.addView(c, lp);
    }

    private void toggleBookmark() {
        if (cur == null || cur.web == null || cur.url == null) return;
        Db db = Db.get(this);
        if (db.isBookmarked(cur.url)) {
            db.removeBookmark(cur.url);
            toast(L.t("bookmark_removed"));
        } else {
            db.addBookmark(cur.title == null || cur.title.isEmpty() ? Ui.hostOf(cur.url) : cur.title, cur.url);
            toast(L.t("bookmarked"));
        }
    }

    private void toggleDesktop() {
        if (cur == null || cur.web == null || cur.home) { toast(L.t("open_page_first")); return; }
        cur.desktop = !cur.desktop;
        applySettings(cur);
        cur.web.reload();
    }

    private void openFind() {
        if (cur == null || cur.web == null || cur.home) { toast(L.t("open_page_first")); return; }
        findBar.setVisibility(View.VISIBLE);
        findEdit.setText("");
        findCount.setText("");
        findEdit.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(findEdit, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeFind() {
        if (findBar == null || findBar.getVisibility() != View.VISIBLE) return;
        findBar.setVisibility(View.GONE);
        if (cur != null && cur.web != null) cur.web.clearMatches();
        hideKeyboard();
    }

    private void shareText(String url, String title) {
        if (url == null) return;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, url);
        if (title != null) i.putExtra(Intent.EXTRA_SUBJECT, title);
        startActivity(Intent.createChooser(i, L.t("share")));
    }

    private void copy(String s) {
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("link", s));
        toast(L.t("copied"));
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void openLibrary(int mode) {
        Intent i = new Intent(this, LibraryActivity.class);
        i.putExtra(LibraryActivity.EXTRA_MODE, mode);
        startActivityForResult(i, REQ_LIBRARY);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == REQ_FILE) {
            if (fileCallback != null) fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            fileCallback = null;
            return;
        }
        if (req == REQ_LIBRARY && res == RESULT_OK && data != null) {
            String url = data.getStringExtra("url");
            String off = data.getStringExtra(EXTRA_OFFLINE_PATH);
            if (cur == null) switchTo(newTab(false, false));
            if (off != null) openOffline(cur, off, data.getStringExtra(EXTRA_TITLE), url);
            else if (url != null) load(cur, url);
        }
    }

    // ------------------------------------------------------------------ navigation

    private void goBack() {
        if (cur == null) return;
        if (!cur.home && cur.web != null && cur.web.canGoBack()) { cur.web.goBack(); return; }
        if (!cur.home) {
            if (cur.parent != null && tabs.contains(cur.parent)) { closeTab(cur); return; }
            showHome();
            return;
        }
        if (tabs.size() > 1) { closeTab(cur); return; }
        moveTaskToBack(true);
    }

    private void goForward() {
        if (cur == null || cur.web == null) return;
        if (cur.home) {
            cur.home = false;
            homeScroll.setVisibility(View.GONE);
            webHolder.setVisibility(View.VISIBLE);
            updateBars();
            return;
        }
        if (cur.web.canGoForward()) cur.web.goForward();
    }

    @Override
    public void onBackPressed() {
        if (customView != null) { exitFullscreen(); return; }
        if (tabSwitcher.getVisibility() == View.VISIBLE) { hideTabs(); return; }
        if (findBar.getVisibility() == View.VISIBLE) { closeFind(); return; }
        if (urlEdit.hasFocus()) { urlEdit.clearFocus(); hideKeyboard(); return; }
        goBack();
    }
}
