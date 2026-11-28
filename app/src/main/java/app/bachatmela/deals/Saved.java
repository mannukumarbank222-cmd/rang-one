package app.bachatmela.deals;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Favourites and recently viewed, stored on the device only (SharedPreferences, JSON). */
public final class Saved {
    private static final String P = "bachat_saved", FAV = "favs", RECENT = "recent";
    private static final int MAX_RECENT = 30;

    private static SharedPreferences sp(Context c) { return c.getSharedPreferences(P, Context.MODE_PRIVATE); }

    private static List<Deal> read(Context c, String key) {
        List<Deal> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp(c).getString(key, "[]"));
            for (int i = 0; i < a.length(); i++) {
                Deal stored = Deal.fromJson(a.getJSONObject(i));
                Deal fresh = DealRepo.byId(stored.id);   // prefer the latest price/link if the deal still exists
                out.add(fresh != null ? fresh : stored);
            }
        } catch (Exception ignored) { }
        return out;
    }

    private static void write(Context c, String key, List<Deal> l) {
        JSONArray a = new JSONArray();
        for (Deal d : l) a.put(d.toJson());
        sp(c).edit().putString(key, a.toString()).apply();
    }

    public static List<Deal> favourites(Context c) { return read(c, FAV); }
    public static List<Deal> recent(Context c) { return read(c, RECENT); }

    public static boolean isFav(Context c, String id) {
        for (Deal d : read(c, FAV)) if (d.id.equals(id)) return true;
        return false;
    }

    /** Returns the new state. */
    public static boolean toggleFav(Context c, Deal deal) {
        List<Deal> l = read(c, FAV);
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).id.equals(deal.id)) { l.remove(i); write(c, FAV, l); return false; }
        }
        l.add(0, deal);
        write(c, FAV, l);
        return true;
    }

    public static void addRecent(Context c, Deal deal) {
        List<Deal> l = read(c, RECENT);
        for (int i = l.size() - 1; i >= 0; i--) if (l.get(i).id.equals(deal.id)) l.remove(i);
        l.add(0, deal);
        while (l.size() > MAX_RECENT) l.remove(l.size() - 1);
        write(c, RECENT, l);
    }

    public static void clearRecent(Context c) { sp(c).edit().remove(RECENT).apply(); }

    private Saved() { }
}
