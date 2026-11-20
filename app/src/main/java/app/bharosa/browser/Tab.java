package app.bharosa.browser;

import android.graphics.Bitmap;
import android.webkit.WebView;

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
    public Tab parent;

    Tab(boolean incognito) { this.incognito = incognito; }

    String displayTitle() {
        if (home || web == null) return L.t("new_tab");
        if (title != null && !title.isEmpty()) return title;
        return url == null ? "" : url;
    }
}
