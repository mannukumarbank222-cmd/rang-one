package app.bachatmela.deals;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** One deal. The link field may be a plain store URL today and an affiliate (EarnKaro / Cuelinks) link later. */
public class Deal {
    public String id, title, store, category, image, link, description;
    public double mrp, price;

    public int percentOff() {
        if (mrp <= 0 || price >= mrp) return 0;
        return (int) Math.round((mrp - price) * 100.0 / mrp);
    }

    public double savings() { return Math.max(0, mrp - price); }

    public boolean isLoot() { return percentOff() >= 70; }

    public static Deal fromJson(JSONObject o) {
        Deal d = new Deal();
        d.id = o.optString("id");
        d.title = o.optString("title");
        d.store = o.optString("store");
        d.category = o.optString("category");
        d.image = o.optString("image");
        d.link = o.optString("link");
        d.description = o.optString("description");
        d.mrp = o.optDouble("mrp", 0);
        d.price = o.optDouble("price", 0);
        return d;
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id); o.put("title", title); o.put("store", store); o.put("category", category);
            o.put("image", image); o.put("link", link); o.put("description", description);
            o.put("mrp", mrp); o.put("price", price);
        } catch (JSONException ignored) { }
        return o;
    }

    /** Accepts either {"deals":[...]} or a bare array. Skips malformed rows. */
    public static List<Deal> parseList(String json) throws JSONException {
        String s = json.trim();
        JSONArray arr = s.startsWith("[") ? new JSONArray(s) : new JSONObject(s).getJSONArray("deals");
        List<Deal> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.optJSONObject(i);
            if (o == null) continue;
            Deal d = fromJson(o);
            if (d.id.isEmpty() || d.title.isEmpty()) continue;
            out.add(d);
        }
        return out;
    }

    public static String rupees(double v) {
        long r = Math.round(v);
        // Indian grouping: 1,23,456
        String n = Long.toString(Math.abs(r));
        StringBuilder sb = new StringBuilder();
        int len = n.length();
        if (len <= 3) sb.append(n);
        else {
            String head = n.substring(0, len - 3), tail = n.substring(len - 3);
            StringBuilder h = new StringBuilder();
            for (int i = head.length(); i > 0; i -= 2) {
                int st = Math.max(0, i - 2);
                h.insert(0, head.substring(st, i));
                if (st > 0) h.insert(0, ",");
            }
            sb.append(h).append(",").append(tail);
        }
        return (r < 0 ? "-₹" : "₹") + sb;
    }

    public static String rupeesExact(double v) {
        if (Math.abs(v - Math.round(v)) < 0.005) return rupees(v);
        return "₹" + String.format(Locale.US, "%,.2f", v);
    }
}
