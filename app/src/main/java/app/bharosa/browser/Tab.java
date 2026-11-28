package app.bharosa.browser;

import android.graphics.Bitmap;
import android.webkit.WebView;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** One browser tab. A tab shows the native home screen until a page is opened. */
public final class Tab {
    public WebView web;
    public boolean incognito;
    public boolean home = true;
    public boolean desktop;
    public boolean offlineView;
    public String title = "";
    public String url = "";
    public Bitmap thumb;
    public Bitmap favicon;
    public Tab parent;

    /** Media found on the current page: url -> {url, kind, label}. Touched on the UI thread only. */
    public final LinkedHashMap<String, String[]> media = new LinkedHashMap<>();
    /** User hid the floating download button for this page. */
    public boolean mediaHidden;
    /** Ads / trackers blocked on the current page (updated from the network thread). */
    public final AtomicInteger blocked = new AtomicInteger();

    Tab(boolean incognito) { this.incognito = incognito; }

    String displayTitle() {
        if (home || web == null) return L.t("new_tab");
        if (title != null && !title.isEmpty()) return title;
        return url == null ? "" : url;
    }
}
