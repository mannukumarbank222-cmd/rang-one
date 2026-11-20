package app.bharosa.browser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;

public final class Prefs {
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("rangone", Context.MODE_PRIVATE); }

    public static final String[] ENGINE_IDS = {"google", "bing", "duckduckgo"};
    public static final String[] ENGINE_NAMES = {"Google", "Bing", "DuckDuckGo"};

    public static String engine(Context c) { return sp(c).getString("engine", "google"); }
    public static void setEngine(Context c, String v) { sp(c).edit().putString("engine", v).apply(); }

    public static String engineName(Context c) {
        String e = engine(c);
        for (int i = 0; i < ENGINE_IDS.length; i++) if (ENGINE_IDS[i].equals(e)) return ENGINE_NAMES[i];
        return "Google";
    }

    public static String searchUrl(Context c, String q) {
        String e = Uri.encode(q);
        String eng = engine(c);
        if ("bing".equals(eng)) return "https://www.bing.com/search?q=" + e;
        if ("duckduckgo".equals(eng)) return "https://duckduckgo.com/?q=" + e;
        return "https://www.google.com/search?q=" + e;
    }

    /** "system", "light" or "dark" */
    public static String theme(Context c) { return sp(c).getString("theme", "system"); }
    public static void setTheme(Context c, String v) { sp(c).edit().putString("theme", v).apply(); }

    public static boolean isDark(Context c) {
        String t = theme(c);
        if ("dark".equals(t)) return true;
        if ("light".equals(t)) return false;
        int m = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return m == Configuration.UI_MODE_NIGHT_YES;
    }

    /** "auto", "hi", "en" */
    public static String lang(Context c) { return sp(c).getString("lang", "auto"); }
    public static void setLang(Context c, String v) { sp(c).edit().putString("lang", v).apply(); }

    public static boolean blockPopups(Context c) { return sp(c).getBoolean("popups", true); }
    public static void setBlockPopups(Context c, boolean v) { sp(c).edit().putBoolean("popups", v).apply(); }

    public static boolean js(Context c) { return sp(c).getBoolean("js", true); }
    public static void setJs(Context c, boolean v) { sp(c).edit().putBoolean("js", v).apply(); }

    public static boolean desktopDefault(Context c) { return sp(c).getBoolean("desktop", false); }
    public static void setDesktopDefault(Context c, boolean v) { sp(c).edit().putBoolean("desktop", v).apply(); }
}
