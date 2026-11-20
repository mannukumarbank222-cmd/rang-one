package app.bharosa.browser;

import android.content.Context;
import java.util.HashMap;
import java.util.Locale;

/** Simple in-app Hindi / English strings. */
public final class L {
    private static final HashMap<String, String[]> M = new HashMap<>();
    private static boolean hindi = false;

    static void p(String k, String en, String hi) { M.put(k, new String[]{en, hi}); }

    static {
        p("search_hint", "Search or type web address", "खोजें या वेब पता लिखें");
        p("greet", "Welcome to Rang One", "Rang One में आपका स्वागत है");
        p("greet_sub", "Fast, simple and private browsing", "तेज़, आसान और सुरक्षित ब्राउज़िंग");
        p("shortcuts", "Shortcuts", "शॉर्टकट");
        p("recent", "Recently visited", "हाल ही में देखे गए");
        p("add_shortcut", "Add", "जोड़ें");
        p("new_tab", "New tab", "नया टैब");
        p("new_private", "New private tab", "नया प्राइवेट टैब");
        p("private", "Private", "प्राइवेट");
        p("private_title", "Private tab", "प्राइवेट टैब");
        p("private_info", "Pages you open in private tabs are not saved in history. Cookies are not stored while a private tab is open, and the cache is cleared when you close your private tabs.",
                "प्राइवेट टैब में खोले गए पेज हिस्ट्री में सेव नहीं होते। प्राइवेट टैब खुला होने पर कुकीज़ सेव नहीं होतीं, और प्राइवेट टैब बंद करने पर कैश साफ़ हो जाता है।");
        p("tabs", "Tabs", "टैब");
        p("close_all", "Close all", "सभी बंद करें");
        p("bookmarks", "Bookmarks", "बुकमार्क");
        p("history", "History", "हिस्ट्री");
        p("downloads", "Downloads", "डाउनलोड");
        p("offline", "Offline pages", "ऑफ़लाइन पेज");
        p("save_offline", "Save offline", "ऑफ़लाइन सेव");
        p("add_bookmark", "Bookmark", "बुकमार्क करें");
        p("bookmarked", "Bookmarked", "बुकमार्क हो गया");
        p("bookmark_removed", "Bookmark removed", "बुकमार्क हटाया गया");
        p("find", "Find in page", "पेज में खोजें");
        p("desktop", "Desktop site", "डेस्कटॉप साइट");
        p("share", "Share", "शेयर");
        p("settings", "Settings", "सेटिंग्स");
        p("about", "About", "ऐप के बारे में");
        p("exit", "Exit", "बंद करें");
        p("reload", "Reload", "रीलोड");
        p("home", "Home", "होम");
        p("clear_history", "Clear history", "हिस्ट्री साफ़ करें");
        p("clear", "Clear", "साफ़ करें");
        p("empty_bookmarks", "No bookmarks yet. Tap the star in the menu to save a page.", "अभी कोई बुकमार्क नहीं। पेज सेव करने के लिए मेनू में स्टार दबाएँ।");
        p("empty_history", "No history yet.", "अभी कोई हिस्ट्री नहीं।");
        p("empty_downloads", "No downloads yet. Files you download appear here.", "अभी कोई डाउनलोड नहीं। डाउनलोड की गई फ़ाइलें यहाँ दिखेंगी।");
        p("empty_offline", "No offline pages. Use \"Save offline\" in the menu to read a page without internet.", "कोई ऑफ़लाइन पेज नहीं। बिना इंटरनेट पढ़ने के लिए मेनू में \"ऑफ़लाइन सेव\" दबाएँ।");
        p("saved_offline", "Page saved for offline reading", "पेज ऑफ़लाइन पढ़ने के लिए सेव हो गया");
        p("save_failed", "Could not save this page", "यह पेज सेव नहीं हो सका");
        p("open_page_first", "Open a web page first", "पहले कोई वेब पेज खोलें");
        p("download_started", "Download started", "डाउनलोड शुरू हुआ");
        p("download_failed", "Download could not start", "डाउनलोड शुरू नहीं हो सका");
        p("download_q", "Download this file?", "यह फ़ाइल डाउनलोड करें?");
        p("download", "Download", "डाउनलोड");
        p("cancel", "Cancel", "रद्द करें");
        p("delete", "Delete", "हटाएँ");
        p("open", "Open", "खोलें");
        p("open_new_tab", "Open in new tab", "नए टैब में खोलें");
        p("copy_link", "Copy link", "लिंक कॉपी करें");
        p("copied", "Copied", "कॉपी हो गया");
        p("search_engine", "Search engine", "सर्च इंजन");
        p("theme", "Theme", "थीम");
        p("theme_system", "System default", "फ़ोन के अनुसार");
        p("theme_light", "Light", "लाइट");
        p("theme_dark", "Dark", "डार्क");
        p("language", "App language", "ऐप की भाषा");
        p("lang_auto", "Phone language", "फ़ोन की भाषा");
        p("clear_data", "Clear browsing data", "ब्राउज़िंग डेटा साफ़ करें");
        p("clear_data_sub", "History, cookies, cache, site data", "हिस्ट्री, कुकीज़, कैश, साइट डेटा");
        p("cleared", "Browsing data cleared", "ब्राउज़िंग डेटा साफ़ हो गया");
        p("c_history", "Browsing history", "ब्राउज़िंग हिस्ट्री");
        p("c_cookies", "Cookies and site data", "कुकीज़ और साइट डेटा");
        p("c_cache", "Cached images and files", "कैश की गई इमेज और फ़ाइलें");
        p("c_offline", "Offline pages", "ऑफ़लाइन पेज");
        p("c_bookmarks", "Bookmarks", "बुकमार्क");
        p("block_popups", "Block pop-ups", "पॉप-अप रोकें");
        p("js", "JavaScript", "जावास्क्रिप्ट");
        p("desktop_default", "Always request desktop site", "हमेशा डेस्कटॉप साइट माँगें");
        p("about_app", "About Rang One", "Rang One के बारे में");
        p("privacy", "Privacy policy", "गोपनीयता नीति");
        p("matches", "%1$d of %2$d", "%2$d में से %1$d");
        p("no_match", "No matches", "कोई मिलान नहीं");
        p("shortcut_name", "Name", "नाम");
        p("shortcut_url", "Web address", "वेब पता");
        p("remove_shortcut", "Remove shortcut?", "शॉर्टकट हटाएँ?");
        p("not_secure", "Not secure", "सुरक्षित नहीं");
        p("offline_badge", "Offline copy", "ऑफ़लाइन कॉपी");
        p("no_internet", "No internet connection. Your offline pages are still available.", "इंटरनेट कनेक्शन नहीं है। आपके ऑफ़लाइन पेज फिर भी उपलब्ध हैं।");
        p("version", "Version", "वर्ज़न");
        p("developer", "Developer", "डेवलपर");
        p("contact", "Contact", "संपर्क");
        p("about_text", "Rang One is a light web browser made in India. It has tabs, private tabs, bookmarks, history, downloads, offline pages, find in page and desktop mode. Web pages are shown with Android System WebView.",
                "Rang One भारत में बना एक हल्का वेब ब्राउज़र है। इसमें टैब, प्राइवेट टैब, बुकमार्क, हिस्ट्री, डाउनलोड, ऑफ़लाइन पेज, पेज में खोज और डेस्कटॉप मोड है। वेब पेज Android System WebView से दिखाए जाते हैं।");
        p("about_data", "No account or login. No ads. Your bookmarks, history, downloads list and offline pages are stored only on your phone. Searches go directly to the search engine you choose.",
                "कोई अकाउंट या लॉगिन नहीं। कोई विज्ञापन नहीं। आपके बुकमार्क, हिस्ट्री, डाउनलोड सूची और ऑफ़लाइन पेज सिर्फ़ आपके फ़ोन में रहते हैं। खोज सीधे आपके चुने हुए सर्च इंजन पर जाती है।");
        p("on", "On", "चालू");
        p("off", "Off", "बंद");
        p("ok", "OK", "ठीक है");
        p("close", "Close", "बंद करें");
        p("back", "Back", "पीछे");
        p("forward", "Forward", "आगे");
        p("menu", "Menu", "मेनू");
        p("pending", "Waiting", "इंतज़ार");
        p("running", "Downloading", "डाउनलोड हो रहा है");
        p("done", "Completed", "पूरा हुआ");
        p("failed", "Failed", "असफल");
        p("paused", "Paused", "रुका हुआ");
        p("cant_open", "No app found to open this file", "इस फ़ाइल को खोलने के लिए कोई ऐप नहीं मिला");
        p("external_q", "Open in another app?", "दूसरे ऐप में खोलें?");
    }

    public static void init(Context c) {
        String pref = Prefs.lang(c);
        if ("hi".equals(pref)) hindi = true;
        else if ("en".equals(pref)) hindi = false;
        else {
            Locale l = c.getResources().getConfiguration().getLocales().get(0);
            hindi = l != null && "hi".equals(l.getLanguage());
        }
    }

    public static boolean isHindi() { return hindi; }

    public static String t(String k) {
        String[] v = M.get(k);
        if (v == null) return k;
        return hindi ? v[1] : v[0];
    }
}
