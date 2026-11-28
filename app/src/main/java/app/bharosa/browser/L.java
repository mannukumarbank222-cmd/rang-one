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
        p("about_text", "Rang One is a light web browser made in India. It has tabs, private tabs, bookmarks, history, a video and file downloader, a built-in ad and tracker blocker, reading mode, night mode for web pages, save as PDF, offline pages, translate, text size, data saver, home screen shortcuts, find in page and desktop mode. Web pages are shown with Android System WebView.",
                "Rang One भारत में बना एक हल्का वेब ब्राउज़र है। इसमें टैब, प्राइवेट टैब, बुकमार्क, हिस्ट्री, वीडियो और फ़ाइल डाउनलोडर, बिल्ट-इन ऐड और ट्रैकर ब्लॉकर, पढ़ने का मोड, वेब पेज के लिए नाइट मोड, PDF सेव, ऑफ़लाइन पेज, अनुवाद, टेक्स्ट का आकार, डेटा सेवर, होम स्क्रीन शॉर्टकट, पेज में खोज और डेस्कटॉप मोड है। वेब पेज Android System WebView से दिखाए जाते हैं।");
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

        // 4.8: media downloader
        p("dl_video", "Download video", "वीडियो डाउनलोड");
        p("dl_video_n", "Download video (%d)", "वीडियो डाउनलोड (%d)");
        p("media_found", "Videos and audio on this page", "इस पेज के वीडियो और ऑडियो");
        p("no_media", "No video found on this page yet. Start playing the video, then try again.", "इस पेज पर अभी कोई वीडियो नहीं मिला। पहले वीडियो चलाएँ, फिर दोबारा कोशिश करें।");
        p("k_video", "Video", "वीडियो");
        p("k_audio", "Audio", "ऑडियो");
        p("k_stream", "Streaming video (cannot save)", "स्ट्रीमिंग वीडियो (सेव नहीं हो सकता)");
        p("k_blob", "Protected stream (cannot save)", "सुरक्षित स्ट्रीम (सेव नहीं हो सकता)");
        p("stream_title", "This video can't be saved", "यह वीडियो सेव नहीं हो सकता");
        p("stream_msg", "This video is streamed in small pieces (HLS / blob stream), so it cannot be saved as one file. Look for a direct MP4 link or the website's own download button.",
                "यह वीडियो छोटे-छोटे टुकड़ों में स्ट्रीम होता है (HLS / blob स्ट्रीम), इसलिए इसे एक फ़ाइल के रूप में सेव नहीं किया जा सकता। सीधा MP4 लिंक या वेबसाइट का अपना डाउनलोड बटन देखें।");
        p("video_link", "Video", "वीडियो");
        p("copy_video_link", "Copy video link", "वीडियो लिंक कॉपी करें");
        p("open_video", "Play in new tab", "नए टैब में चलाएँ");
        p("hide", "Hide", "छिपाएँ");
        p("perm_storage", "Storage permission is needed to save files in Downloads. The file will be saved in the app folder instead.", "डाउनलोड फ़ोल्डर में फ़ाइल सेव करने के लिए स्टोरेज अनुमति चाहिए। फ़ाइल ऐप फ़ोल्डर में सेव होगी।");
        p("share_file", "Share file", "फ़ाइल शेयर करें");
        p("not_finished", "This download is not finished yet", "यह डाउनलोड अभी पूरा नहीं हुआ है");
        p("all_downloads", "All downloads", "सभी डाउनलोड");

        // 4.8: ad blocker
        p("adblock", "Ad blocker", "ऐड ब्लॉकर");
        p("adblock_long", "Block ads and trackers", "विज्ञापन और ट्रैकर रोकें");
        p("adblock_count", "Ads and trackers blocked on this page: %d", "इस पेज पर रोके गए विज्ञापन और ट्रैकर: %d");
        p("adblock_off_msg", "The ad blocker is off.", "ऐड ब्लॉकर बंद है।");
        p("turn_on", "Turn on", "चालू करें");
        p("turn_off", "Turn off", "बंद करें");
        p("blocked_n", "%d blocked", "%d रोके गए");

        // 4.8: reading mode
        p("reader", "Reading mode", "पढ़ने का मोड");
        p("reader_none", "Reading mode is not available for this page", "इस पेज के लिए पढ़ने का मोड उपलब्ध नहीं है");
        p("font_smaller", "Smaller text", "छोटा टेक्स्ट");
        p("font_bigger", "Bigger text", "बड़ा टेक्स्ट");
        p("open_original", "Open original page", "असली पेज खोलें");

        // 4.8: other page tools
        p("night", "Night mode", "नाइट मोड");
        p("night_long", "Night mode for web pages", "वेब पेज के लिए नाइट मोड");
        p("night_on", "Night mode on for web pages", "वेब पेज के लिए नाइट मोड चालू");
        p("night_off", "Night mode off", "नाइट मोड बंद");
        p("pdf", "Save as PDF", "PDF सेव करें");
        p("add_home", "Add to Home screen", "होम स्क्रीन पर जोड़ें");
        p("add_home_done", "Shortcut request sent to your launcher", "शॉर्टकट आपके लॉन्चर को भेज दिया गया");
        p("not_supported", "Not supported on this phone", "इस फ़ोन पर उपलब्ध नहीं");
        p("translate", "Translate", "अनुवाद");
        p("translate_to", "Translate page to", "पेज का अनुवाद करें");
        p("text_size", "Text size", "टेक्स्ट का आकार");
        p("reset", "Reset", "रीसेट");
        p("data_saver", "Data saver", "डेटा सेवर");
        p("data_saver_long", "Data saver (don't load images)", "डेटा सेवर (इमेज लोड न करें)");
        p("data_saver_on", "Data saver on: images will not load", "डेटा सेवर चालू: इमेज लोड नहीं होंगी");
        p("data_saver_off", "Data saver off", "डेटा सेवर बंद");
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
