package app.bachatmela.deals;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static app.bachatmela.deals.Ui.*;

public class MainActivity extends Activity {
    static final int HOME = 0, CATS = 1, FAVS = 2, CALC = 3, ABOUT = 4;
    private static final String[] TAB_HI = {"होम", "श्रेणियाँ", "पसंदीदा", "कैलकुलेटर", "जानकारी"};
    private static final int[] TAB_IC = {R.drawable.ic_home, R.drawable.ic_grid, R.drawable.ic_heart, R.drawable.ic_calc, R.drawable.ic_info};

    private FrameLayout content;
    private TextView banner;
    private final LinearLayout[] tabViews = new LinearLayout[5];
    private int tab = HOME;

    // Home filter state
    private String query = "", cat = null, store = null;
    private boolean grid = true, favRecent = false;
    private LinearLayout dealsBox;
    private TextView countLine;
    private final List<TextView> catChips = new ArrayList<>(), storeChips = new ArrayList<>();

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.window(this);
        DealRepo.loadLocal(this);
        buildShell();
        if (!handleIntent(getIntent())) show(HOME);
        refresh(false);
    }

    @Override protected void onNewIntent(Intent i) {
        super.onNewIntent(i);
        setIntent(i);
        handleIntent(i);
    }

    @Override protected void onResume() {
        super.onResume();
        if (tab == FAVS) show(FAVS);   // favourites may have changed in the detail screen
    }

    /** Extras let the CI screenshot script jump to a screen: tab, cat, store, q, deal, fav. */
    private boolean handleIntent(Intent i) {
        if (i == null || i.getExtras() == null) return false;
        String t = i.getStringExtra("tab");
        if (i.hasExtra("cat")) cat = emptyToNull(i.getStringExtra("cat"));
        if (i.hasExtra("store")) store = emptyToNull(i.getStringExtra("store"));
        if (i.hasExtra("q")) query = i.getStringExtra("q");
        if (i.hasExtra("list")) grid = !"1".equals(i.getStringExtra("list"));
        if (i.hasExtra("recent")) favRecent = "1".equals(i.getStringExtra("recent"));
        int target = HOME;
        if ("cats".equals(t)) target = CATS;
        else if ("favs".equals(t)) target = FAVS;
        else if ("calc".equals(t)) target = CALC;
        else if ("about".equals(t)) target = ABOUT;
        show(target);
        String dealId = i.getStringExtra("deal");
        if (dealId != null) {
            Deal d = DealRepo.byId(dealId);
            if (d != null) DealViews.open(this, d);
        }
        return true;
    }

    private static String emptyToNull(String s) { return s == null || s.isEmpty() || "all".equals(s) ? null : s; }

    // ---------------------------------------------------------------- shell

    private void buildShell() {
        LinearLayout root = col(this);
        root.setBackgroundColor(BG);

        LinearLayout bar = row(this);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{ORANGE_DARK, ORANGE, SAFFRON});
        bar.setBackground(g);
        bar.setPadding(dp(this, 16), dp(this, 10), dp(this, 8), dp(this, 10));
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        bar.addView(logo, lp(dp(this, 38), dp(this, 38)));
        LinearLayout titles = col(this);
        titles.setPadding(dp(this, 10), 0, 0, 0);
        titles.addView(tv(this, "बचत मेला", 21, 0xFFFFFFFF, true));
        titles.addView(tv(this, "Bachat Mela · रोज़ की बेस्ट डील्स", 12, 0xEEFFFFFF, false));
        bar.addView(titles, lpw(1));
        ImageView ref = icon(this, R.drawable.ic_refresh, 0xFFFFFFFF, 24);
        ref.setPadding(dp(this, 10), dp(this, 10), dp(this, 10), dp(this, 10));
        ref.setLayoutParams(lp(dp(this, 46), dp(this, 46)));
        ref.setContentDescription("रीफ्रेश / Refresh");
        ref.setBackgroundResource(android.R.drawable.list_selector_background);
        ref.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { refresh(true); } });
        bar.addView(ref);
        root.addView(bar, lp(MATCH, WRAP));

        banner = tv(this, "", 12, 0xFF7A4B00, false);
        banner.setBackgroundColor(0xFFFFF1C9);
        banner.setPadding(dp(this, 16), dp(this, 6), dp(this, 16), dp(this, 6));
        banner.setVisibility(View.GONE);
        root.addView(banner, lp(MATCH, WRAP));

        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(MATCH, 0, 1));

        View div = new View(this);
        div.setBackgroundColor(DIVIDER);
        root.addView(div, lp(MATCH, 1));
        LinearLayout nav = row(this);
        nav.setBackgroundColor(SURFACE);
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            LinearLayout it = col(this);
            it.setGravity(Gravity.CENTER);
            it.setPadding(0, dp(this, 7), 0, dp(this, 7));
            it.addView(icon(this, TAB_IC[i], TEXT2, 24));
            TextView l = tv(this, TAB_HI[i], 11, TEXT2, false);
            l.setGravity(Gravity.CENTER);
            l.setSingleLine(true);
            it.addView(l);
            it.setBackgroundResource(android.R.drawable.list_selector_background);
            it.setContentDescription(TAB_HI[i]);
            it.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { show(idx); } });
            nav.addView(it, lpw(1));
            tabViews[i] = it;
        }
        root.addView(nav, lp(MATCH, WRAP));
        setContentView(root);
    }

    private void show(int t) {
        tab = t;
        for (int i = 0; i < 5; i++) {
            int c = i == t ? ORANGE : TEXT2;
            ((ImageView) tabViews[i].getChildAt(0)).setColorFilter(c);
            TextView l = (TextView) tabViews[i].getChildAt(1);
            l.setTextColor(c);
            l.setTypeface(i == t ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        }
        hideKeyboard();
        content.removeAllViews();
        View v;
        switch (t) {
            case CATS: v = categories(); break;
            case FAVS: v = favourites(); break;
            case CALC: v = calculator(); break;
            case ABOUT: v = about(); break;
            default: v = home();
        }
        content.addView(v, new FrameLayout.LayoutParams(MATCH, MATCH));
    }

    @Override public void onBackPressed() {
        if (tab != HOME) { show(HOME); return; }
        if (cat != null || store != null || !query.isEmpty()) {
            cat = null; store = null; query = ""; show(HOME); return;
        }
        super.onBackPressed();
    }

    private void hideKeyboard() {
        View f = getCurrentFocus();
        if (f != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (im != null) im.hideSoftInputFromWindow(f.getWindowToken(), 0);
        }
    }

    private void refresh(final boolean manual) {
        if (manual) Toast.makeText(this, "नई डील्स ला रहे हैं…", Toast.LENGTH_SHORT).show();
        DealRepo.refresh(this, new DealRepo.Callback() {
            public void onDeals(List<Deal> deals, DealRepo.Source src, boolean offline) {
                if (isFinishing()) return;
                if (offline) {
                    banner.setText("आप ऑफ़लाइन हैं — सेव की हुई डील्स दिखा रहे हैं (Offline: showing saved deals)");
                    banner.setVisibility(View.VISIBLE);
                } else {
                    banner.setVisibility(View.GONE);
                }
                if (manual) Toast.makeText(MainActivity.this, offline ? "इंटरनेट नहीं मिला" : "डील्स अपडेट हो गईं", Toast.LENGTH_SHORT).show();
                if (tab == HOME) renderDeals();
                else if (tab == CATS) show(CATS);
            }
        });
    }

    // ---------------------------------------------------------------- home

    private View home() {
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        LinearLayout col = col(this);
        col.setPadding(0, 0, 0, dp(this, 16));
        sv.addView(col);

        LinearLayout search = row(this);
        search.setBackground(stroke(SURFACE, DIVIDER, dp(this, 1), dp(this, 24)));
        search.setPadding(dp(this, 14), 0, dp(this, 6), 0);
        search.addView(icon(this, R.drawable.ic_search, TEXT2, 22));
        final EditText et = new EditText(this);
        et.setHint("डील खोजें… (Search deals)");
        et.setText(query);
        et.setSingleLine(true);
        et.setTextSize(15);
        et.setBackground(null);
        et.setTextColor(TEXT);
        et.setHintTextColor(0xFF9C928A);
        et.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        et.setPadding(dp(this, 10), dp(this, 11), dp(this, 4), dp(this, 11));
        search.addView(et, lpw(1));
        final ImageView clear = icon(this, R.drawable.ic_close, TEXT2, 20);
        clear.setPadding(dp(this, 6), dp(this, 6), dp(this, 6), dp(this, 6));
        clear.setLayoutParams(lp(dp(this, 34), dp(this, 34)));
        clear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
        clear.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { et.setText(""); } });
        search.addView(clear);
        et.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) {
                query = s.toString();
                clear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                renderDeals();
            }
        });
        col.addView(search, margins(lp(MATCH, WRAP), this, 12, 12, 12, 4));

        // category chips
        catChips.clear();
        LinearLayout cr = row(this);
        cr.setPadding(dp(this, 12), dp(this, 8), dp(this, 4), dp(this, 2));
        addCatChip(cr, null, "सभी · All", ORANGE);
        for (int i = 0; i < Cats.KEYS.length; i++) addCatChip(cr, Cats.KEYS[i], Cats.HI[i], Cats.COLOR[i]);
        col.addView(hscroll(cr));

        // store chips
        storeChips.clear();
        LinearLayout sr = row(this);
        sr.setPadding(dp(this, 12), dp(this, 6), dp(this, 4), dp(this, 2));
        TextView lbl = tv(this, "स्टोर:", 13, TEXT2, true);
        lbl.setPadding(0, 0, dp(this, 8), 0);
        sr.addView(lbl);
        addStoreChip(sr, null, "सभी");
        for (String s : Cats.STORES) addStoreChip(sr, s, s);
        col.addView(hscroll(sr));

        LinearLayout head = row(this);
        head.setPadding(dp(this, 16), dp(this, 10), dp(this, 8), dp(this, 2));
        countLine = tv(this, "", 14, TEXT, true);
        head.addView(countLine, lpw(1));
        final TextView toggle = tv(this, grid ? "☰ सूची" : "▦ ग्रिड", 13, ORANGE, true);
        toggle.setPadding(dp(this, 10), dp(this, 6), dp(this, 10), dp(this, 6));
        toggle.setBackground(round(CHIP, dp(this, 14)));
        toggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                grid = !grid;
                toggle.setText(grid ? "☰ सूची" : "▦ ग्रिड");
                renderDeals();
            }
        });
        head.addView(toggle);
        col.addView(head, lp(MATCH, WRAP));

        dealsBox = col(this);
        dealsBox.setPadding(0, dp(this, 4), 0, 0);
        col.addView(dealsBox, lp(MATCH, WRAP));
        renderDeals();
        return sv;
    }

    private HorizontalScrollView hscroll(View v) {
        HorizontalScrollView h = new HorizontalScrollView(this);
        h.setHorizontalScrollBarEnabled(false);
        h.addView(v);
        return h;
    }

    private void addCatChip(LinearLayout parent, final String key, String label, final int color) {
        final TextView c = chip(this, label);
        c.setTag(key);
        c.setTag(R.id.tag_url, color);
        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { cat = key; styleChips(); renderDeals(); }
        });
        parent.addView(c);
        catChips.add(c);
    }

    private void addStoreChip(LinearLayout parent, final String key, String label) {
        final TextView c = chip(this, label);
        c.setTag(key);
        c.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { store = key; styleChips(); renderDeals(); }
        });
        parent.addView(c);
        storeChips.add(c);
    }

    private void styleChips() {
        for (TextView c : catChips) {
            Object k = c.getTag();
            boolean on = k == null ? cat == null : k.equals(cat);
            setChip(c, on, (Integer) c.getTag(R.id.tag_url));
        }
        for (TextView c : storeChips) {
            Object k = c.getTag();
            boolean on = k == null ? store == null : k.equals(store);
            setChip(c, on, k == null ? ORANGE : Cats.storeColor((String) k));
        }
    }

    static List<Deal> filter(List<Deal> all, String q, String cat, String store) {
        List<Deal> out = new ArrayList<>();
        String qq = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        for (Deal d : all) {
            if (cat != null) {
                if (Cats.LOOT.equals(cat)) { if (!d.isLoot()) continue; }
                else if (!cat.equals(d.category)) continue;
            }
            if (store != null && !store.equalsIgnoreCase(d.store)) continue;
            if (!qq.isEmpty()) {
                String hay = (d.title + " " + d.description + " " + d.store + " " + Cats.hi(d.category) + " " + Cats.en(d.category)).toLowerCase(Locale.ROOT);
                if (!hay.contains(qq)) continue;
            }
            out.add(d);
        }
        return out;
    }

    private void renderDeals() {
        if (dealsBox == null || tab != HOME) return;
        styleChips();
        List<Deal> l = filter(DealRepo.all(), query, cat, store);
        String what = cat == null ? "सभी डील्स" : Cats.hi(cat);
        if (store != null) what += " · " + store;
        countLine.setText(what + " (" + l.size() + ")");
        if (l.isEmpty()) {
            dealsBox.removeAllViews();
            TextView e = tv(this, "कोई डील नहीं मिली।\nNo deals match — फ़िल्टर बदलकर देखें।", 15, TEXT2, false);
            e.setGravity(Gravity.CENTER);
            e.setPadding(dp(this, 24), dp(this, 48), dp(this, 24), dp(this, 48));
            dealsBox.addView(e, lp(MATCH, WRAP));
            return;
        }
        DealViews.fill(this, dealsBox, l, grid);
    }

    // ---------------------------------------------------------------- categories

    private View categories() {
        ScrollView sv = new ScrollView(this);
        LinearLayout col = col(this);
        col.setPadding(dp(this, 6), 0, dp(this, 6), dp(this, 16));
        sv.addView(col);
        col.addView(sectionTitle(this, "श्रेणियाँ", "Categories"));
        List<Deal> all = DealRepo.all();
        for (int i = 0; i < Cats.KEYS.length; i += 2) {
            LinearLayout r = row(this);
            r.setBaselineAligned(false);
            for (int j = i; j < i + 2 && j < Cats.KEYS.length; j++) {
                final String key = Cats.KEYS[j];
                int n = filter(all, "", key, null).size();
                LinearLayout tile = col(this);
                tile.setPadding(dp(this, 14), dp(this, 16), dp(this, 14), dp(this, 16));
                GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                        new int[]{Cats.COLOR[j], (Cats.COLOR[j] & 0x00FFFFFF) | 0xAA000000});
                g.setCornerRadius(dp(this, 14));
                tile.setBackground(g);
                tile.setElevation(dp(this, 2));
                TextView initial = tv(this, Cats.HI[j].substring(0, 1), 26, 0xFFFFFFFF, true);
                tile.addView(initial);
                tile.addView(ellipsize(tv(this, Cats.HI[j], 16, 0xFFFFFFFF, true), 2));
                tile.addView(tv(this, Cats.EN[j], 12, 0xE6FFFFFF, false));
                tile.addView(tv(this, n + " डील्स →", 13, 0xFFFFFFFF, true), margins(lp(WRAP, WRAP), this, 0, 8, 0, 0));
                tile.setMinimumHeight(dp(this, 140));
                tile.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) { cat = key; store = null; query = ""; show(HOME); }
                });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, WRAP, 1);
                p.setMargins(dp(this, 6), dp(this, 6), dp(this, 6), dp(this, 6));
                r.addView(tile, p);
            }
            col.addView(r, lp(MATCH, WRAP));
        }
        col.addView(sectionTitle(this, "स्टोर के हिसाब से", "By store"));
        LinearLayout sr = col(this);
        for (final String s : Cats.STORES) {
            int n = filter(all, "", null, s).size();
            LinearLayout r = row(this);
            r.setPadding(dp(this, 14), dp(this, 12), dp(this, 14), dp(this, 12));
            r.addView(storeBadge(this, s));
            TextView t = tv(this, n + " डील्स", 14, TEXT, false);
            t.setPadding(dp(this, 12), 0, 0, 0);
            r.addView(t, lpw(1));
            r.addView(tv(this, "देखें →", 13, ORANGE, true));
            Ui.ripple(r, SURFACE, dp(this, 10));
            r.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { store = s; cat = null; query = ""; show(HOME); }
            });
            sr.addView(r, margins(lp(MATCH, WRAP), this, 6, 3, 6, 3));
        }
        col.addView(sr);
        return sv;
    }

    // ---------------------------------------------------------------- favourites + recent

    private View favourites() {
        LinearLayout outer = col(this);
        LinearLayout seg = row(this);
        seg.setPadding(dp(this, 12), dp(this, 12), dp(this, 12), dp(this, 6));
        final List<Deal> favs = Saved.favourites(this), recent = Saved.recent(this);
        TextView a = chip(this, "♥ पसंदीदा (" + favs.size() + ")");
        TextView b = chip(this, "हाल ही में देखे (" + recent.size() + ")");
        setChip(a, !favRecent, ORANGE);
        setChip(b, favRecent, ORANGE);
        a.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { favRecent = false; show(FAVS); } });
        b.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { favRecent = true; show(FAVS); } });
        seg.addView(a);
        seg.addView(b);
        outer.addView(seg, lp(MATCH, WRAP));

        ScrollView sv = new ScrollView(this);
        LinearLayout col = col(this);
        col.setPadding(0, 0, 0, dp(this, 16));
        sv.addView(col);
        outer.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));
        List<Deal> l = favRecent ? recent : favs;
        if (l.isEmpty()) {
            TextView e = tv(this, favRecent
                    ? "अभी तक कोई डील नहीं देखी।\nRecently viewed deals will appear here."
                    : "अभी कोई डील सेव नहीं है।\nडील खोलकर ♥ सेव करें दबाएँ।\nSaved deals will appear here.", 15, TEXT2, false);
            e.setGravity(Gravity.CENTER);
            e.setPadding(dp(this, 24), dp(this, 64), dp(this, 24), dp(this, 24));
            col.addView(e, lp(MATCH, WRAP));
            return outer;
        }
        if (!favRecent) {
            double save = 0;
            for (Deal d : l) save += d.savings();
            TextView t = tv(this, "सेव की गई डील्स पर कुल संभावित बचत: " + Deal.rupees(save), 13, GREEN, true);
            t.setBackground(round(GREEN_LIGHT, dp(this, 10)));
            t.setPadding(dp(this, 12), dp(this, 8), dp(this, 12), dp(this, 8));
            col.addView(t, margins(lp(MATCH, WRAP), this, 12, 2, 12, 4));
        }
        LinearLayout box = col(this);
        DealViews.fill(this, box, l, false);
        col.addView(box, lp(MATCH, WRAP));
        if (favRecent) {
            TextView clr = tv(this, "इतिहास साफ़ करें · Clear history", 13, RED, true);
            clr.setGravity(Gravity.CENTER);
            clr.setPadding(dp(this, 12), dp(this, 14), dp(this, 12), dp(this, 14));
            clr.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { Saved.clearRecent(MainActivity.this); show(FAVS); }
            });
            col.addView(clr, lp(MATCH, WRAP));
        }
        return outer;
    }

    // ---------------------------------------------------------------- calculator

    private EditText numField(String hint, String val) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(val);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        e.setSingleLine(true);
        e.setTextSize(18);
        e.setTextColor(TEXT);
        e.setBackground(stroke(SURFACE, DIVIDER, dp(this, 1), dp(this, 10)));
        e.setPadding(dp(this, 14), dp(this, 12), dp(this, 14), dp(this, 12));
        return e;
    }

    private static double num(EditText e) {
        try { return Double.parseDouble(e.getText().toString().trim()); } catch (Exception x) { return -1; }
    }

    private View calculator() {
        ScrollView sv = new ScrollView(this);
        LinearLayout col = col(this);
        col.setPadding(dp(this, 16), 0, dp(this, 16), dp(this, 24));
        sv.addView(col);
        TextView h = sectionTitle(this, "बचत कैलकुलेटर", "Bachat Calculator");
        h.setPadding(0, dp(this, 14), 0, dp(this, 4));
        col.addView(h);
        col.addView(tv(this, "दाम और छूट % डालें — तुरंत देखें कितनी बचत होगी।", 13, TEXT2, false));

        col.addView(tv(this, "असली दाम / MRP (₹)", 13, TEXT, true), margins(lp(WRAP, WRAP), this, 0, 14, 0, 6));
        final EditText price = numField("जैसे 2499", "2499");
        col.addView(price, lp(MATCH, WRAP));
        col.addView(tv(this, "छूट / Discount (%)", 13, TEXT, true), margins(lp(WRAP, WRAP), this, 0, 12, 0, 6));
        final EditText pct = numField("जैसे 40", "40");
        col.addView(pct, lp(MATCH, WRAP));

        LinearLayout quick = row(this);
        for (final int q : new int[]{10, 25, 40, 50, 70}) {
            TextView c = chip(this, q + "%");
            c.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { pct.setText(String.valueOf(q)); } });
            quick.addView(c);
        }
        col.addView(hscroll(quick), margins(lp(MATCH, WRAP), this, 0, 10, 0, 0));

        LinearLayout res = col(this);
        res.setBackground(round(GREEN_LIGHT, dp(this, 14)));
        res.setPadding(dp(this, 16), dp(this, 14), dp(this, 16), dp(this, 14));
        final TextView saveT = tv(this, "", 26, GREEN, true);
        final TextView payT = tv(this, "", 16, TEXT, true);
        res.addView(tv(this, "आपकी बचत · You save", 13, TEXT2, false));
        res.addView(saveT);
        res.addView(tv(this, "आपको चुकाने होंगे · You pay", 13, TEXT2, false), margins(lp(WRAP, WRAP), this, 0, 8, 0, 0));
        res.addView(payT);
        col.addView(res, margins(lp(MATCH, WRAP), this, 0, 16, 0, 0));

        final Runnable calc1 = new Runnable() {
            public void run() {
                double p = num(price), d = num(pct);
                if (p < 0 || d < 0 || d > 100) { saveT.setText("—"); payT.setText(d > 100 ? "छूट 100% से ज़्यादा नहीं हो सकती" : "सही संख्या डालें"); return; }
                double s = p * d / 100.0;
                saveT.setText(Deal.rupeesExact(s));
                payT.setText(Deal.rupeesExact(p - s));
            }
        };

        // reverse: MRP + sale price -> % off
        TextView h2 = sectionTitle(this, "असली छूट कितनी है?", "Find real % off");
        h2.setPadding(0, dp(this, 22), 0, dp(this, 4));
        col.addView(h2);
        LinearLayout two = row(this);
        final EditText mrp2 = numField("MRP ₹", "1999");
        final EditText sale2 = numField("सेल दाम ₹", "799");
        two.addView(mrp2, lpw(1));
        View sp = new View(this);
        two.addView(sp, lp(dp(this, 10), 1));
        two.addView(sale2, lpw(1));
        col.addView(two, margins(lp(MATCH, WRAP), this, 0, 6, 0, 0));
        final TextView offT = tv(this, "", 16, ORANGE_DARK, true);
        offT.setBackground(round(CHIP, dp(this, 12)));
        offT.setPadding(dp(this, 16), dp(this, 12), dp(this, 16), dp(this, 12));
        col.addView(offT, margins(lp(MATCH, WRAP), this, 0, 12, 0, 0));
        final Runnable calc2 = new Runnable() {
            public void run() {
                double m = num(mrp2), s = num(sale2);
                if (m <= 0 || s < 0) { offT.setText("MRP और सेल दाम डालें"); return; }
                if (s > m) { offT.setText("सेल दाम MRP से ज़्यादा है — यह छूट नहीं है"); return; }
                double off = (m - s) * 100.0 / m;
                offT.setText(String.format(Locale.US, "%.1f%% छूट  ·  बचत %s", off, Deal.rupeesExact(m - s)));
            }
        };
        TextWatcher w = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            public void onTextChanged(CharSequence s, int a, int b, int c) { }
            public void afterTextChanged(Editable s) { calc1.run(); calc2.run(); }
        };
        price.addTextChangedListener(w);
        pct.addTextChangedListener(w);
        mrp2.addTextChangedListener(w);
        sale2.addTextChangedListener(w);
        calc1.run();
        calc2.run();
        return sv;
    }

    // ---------------------------------------------------------------- about

    private View about() {
        ScrollView sv = new ScrollView(this);
        LinearLayout col = col(this);
        col.setPadding(dp(this, 16), dp(this, 16), dp(this, 16), dp(this, 24));
        sv.addView(col);
        LinearLayout top = row(this);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.ic_launcher);
        top.addView(logo, lp(dp(this, 64), dp(this, 64)));
        LinearLayout t = col(this);
        t.setPadding(dp(this, 14), 0, 0, 0);
        t.addView(tv(this, "बचत मेला", 22, TEXT, true));
        String ver = "1.0";
        try { ver = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (Exception ignored) { }
        t.addView(tv(this, "Bachat Mela · संस्करण " + ver, 13, TEXT2, false));
        top.addView(t, lpw(1));
        col.addView(top);

        col.addView(block("अस्वीकरण · Disclaimer",
                "Bachat Mela ek deal finder aur price comparison app hai. Hum Amazon, Flipkart, Meesho, Myntra ya Ajio se jude nahi hain. Kuch links affiliate links hain; aapke kharidne par humein chhota commission mil sakta hai, aapke liye daam nahi badhta.\n\n"
                        + "बचत मेला एक डील खोजने और दाम तुलना करने वाला ऐप है। हम Amazon, Flipkart, Meesho, Myntra या Ajio से जुड़े नहीं हैं। सभी ब्रांड नाम उनके मालिकों के हैं। दाम बदल सकते हैं — ख़रीदने से पहले स्टोर पर अंतिम दाम ज़रूर देखें।",
                CHIP));
        col.addView(block("प्राइवेसी · Privacy",
                "• कोई लॉगिन नहीं, कोई अकाउंट नहीं।\n• हम आपका कोई निजी डेटा इकट्ठा नहीं करते।\n• पसंदीदा और हाल में देखी डील्स सिर्फ़ आपके फ़ोन में सेव होती हैं।\n• ऐप सिर्फ़ इंटरनेट अनुमति (INTERNET, नेटवर्क स्थिति) माँगता है — डील्स और फ़ोटो लाने के लिए।\n\nNo login, no personal data collected. Favourites stay on your device. Only internet permission is used.",
                GREEN_LIGHT));
        col.addView(block("कैसे काम करता है · How it works",
                "हम अलग-अलग स्टोर की डील्स एक जगह दिखाते हैं। \"ख़रीदें\" दबाने पर डील उस स्टोर की वेबसाइट या ऐप में खुलती है — ख़रीदारी और भुगतान वहीं होता है, इस ऐप में नहीं।\n\nडील्स की सूची: " + srcLabel(),
                SURFACE));
        return sv;
    }

    private String srcLabel() {
        switch (DealRepo.source()) {
            case REMOTE: return "ऑनलाइन अपडेट (" + DealRepo.all().size() + " डील्स)";
            case CACHE: return "पिछली बार सेव की गई (" + DealRepo.all().size() + " डील्स)";
            default: return "ऐप में शामिल (" + DealRepo.all().size() + " डील्स)";
        }
    }

    private View block(String title, String body, int bg) {
        LinearLayout b = col(this);
        b.setBackground(bg == SURFACE ? stroke(SURFACE, DIVIDER, dp(this, 1), dp(this, 14)) : round(bg, dp(this, 14)));
        b.setPadding(dp(this, 16), dp(this, 14), dp(this, 16), dp(this, 14));
        b.addView(tv(this, title, 15, TEXT, true));
        TextView t = tv(this, body, 14, TEXT, false);
        t.setLineSpacing(0, 1.15f);
        b.addView(t, margins(lp(MATCH, WRAP), this, 0, 6, 0, 0));
        LinearLayout.LayoutParams p = lp(MATCH, WRAP);
        p.setMargins(0, dp(this, 14), 0, 0);
        b.setLayoutParams(p);
        return b;
    }
}
