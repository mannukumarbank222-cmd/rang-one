package app.bachatmela.deals;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Deals come from three places, best first: the remote JSON on GitHub (so the owner can change deals
 * and affiliate links without an app update), the last cached copy of it, and the copy bundled in assets.
 */
public class DealRepo {
    public static final String REMOTE_URL =
            "https://raw.githubusercontent.com/mannukumarbank222-cmd/rang-one/bachat-mela/deals/deals.json";

    public enum Source { REMOTE, CACHE, BUNDLED }

    public interface Callback { void onDeals(List<Deal> deals, Source source, boolean offline); }

    private static List<Deal> current = new ArrayList<>();
    private static Source currentSource = Source.BUNDLED;

    public static List<Deal> all() { return current; }
    public static Source source() { return currentSource; }

    public static Deal byId(String id) {
        for (Deal d : current) if (d.id.equals(id)) return d;
        return null;
    }

    private static File cacheFile(Context c) { return new File(c.getFilesDir(), "deals_cache.json"); }

    /** Loads cache or bundled data synchronously so the first screen is never empty. */
    public static void loadLocal(Context c) {
        try {
            File f = cacheFile(c);
            if (f.exists()) {
                List<Deal> l = Deal.parseList(readAll(new FileInputStream(f)));
                if (!l.isEmpty()) { current = l; currentSource = Source.CACHE; return; }
            }
        } catch (Exception ignored) { }
        try {
            current = Deal.parseList(readAll(c.getAssets().open("deals.json")));
            currentSource = Source.BUNDLED;
        } catch (Exception e) {
            current = new ArrayList<>();
        }
    }

    public static boolean online(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm == null ? null : cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        } catch (Exception e) { return true; }
    }

    /** Fetches the remote JSON in the background; calls back on the main thread. */
    public static void refresh(final Context ctx, final Callback cb) {
        final Context c = ctx.getApplicationContext();
        final Handler h = new Handler(Looper.getMainLooper());
        if (!online(c)) {
            h.post(new Runnable() { public void run() { cb.onDeals(current, currentSource, true); } });
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                boolean ok = false;
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL(REMOTE_URL + "?t=" + (System.currentTimeMillis() / 60000)).openConnection();
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(10000);
                    con.setRequestProperty("Accept", "application/json");
                    if (con.getResponseCode() == 200) {
                        String body = readAll(con.getInputStream());
                        List<Deal> l = Deal.parseList(body);
                        if (!l.isEmpty()) {
                            FileOutputStream fo = new FileOutputStream(cacheFile(c));
                            fo.write(body.getBytes("UTF-8"));
                            fo.close();
                            current = l;
                            currentSource = Source.REMOTE;
                            ok = true;
                        }
                    }
                    con.disconnect();
                } catch (Exception ignored) { }
                final boolean offline = !ok;
                h.post(new Runnable() { public void run() { cb.onDeals(current, currentSource, offline); } });
            }
        }, "deals-fetch").start();
    }

    static String readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] b = new byte[8192];
        int n;
        while ((n = in.read(b)) > 0) bo.write(b, 0, n);
        in.close();
        return bo.toString("UTF-8");
    }
}
