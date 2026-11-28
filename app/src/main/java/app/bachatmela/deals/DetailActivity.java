package app.bachatmela.deals;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import static app.bachatmela.deals.Ui.*;

/** Native deal detail. "Buy" hands the link to the store's app or the browser — no in-app WebView. */
public class DetailActivity extends Activity {
    private Deal d;
    private ImageView favIcon;
    private TextView favText;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        Ui.window(this);
        String id = getIntent().getStringExtra("id");
        d = id == null ? null : DealRepo.byId(id);
        if (d == null) {
            try { d = Deal.fromJson(new JSONObject(getIntent().getStringExtra("json"))); } catch (Exception e) { finish(); return; }
        }
        Saved.addRecent(this, d);
        build();
    }

    private void build() {
        LinearLayout root = col(this);
        root.setBackgroundColor(BG);

        LinearLayout bar = row(this);
        bar.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{ORANGE_DARK, ORANGE, SAFFRON}));
        bar.setPadding(dp(this, 4), dp(this, 4), dp(this, 4), dp(this, 4));
        ImageView back = barIcon(R.drawable.ic_back, "वापस / Back");
        back.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finish(); } });
        bar.addView(back);
        TextView title = tv(this, "डील की जानकारी", 18, 0xFFFFFFFF, true);
        title.setPadding(dp(this, 6), 0, 0, 0);
        bar.addView(title, lpw(1));
        ImageView share = barIcon(R.drawable.ic_share, "शेयर करें / Share");
        share.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { share(); } });
        bar.addView(share);
        root.addView(bar, lp(MATCH, WRAP));

        ScrollView sv = new ScrollView(this);
        LinearLayout col = col(this);
        sv.addView(col);
        root.addView(sv, new LinearLayout.LayoutParams(MATCH, 0, 1));

        FrameLayout imgBox = new FrameLayout(this);
        imgBox.setBackgroundColor(SURFACE);
        SquareImageView iv = new SquareImageView(this);
        imgBox.addView(iv, new FrameLayout.LayoutParams(MATCH, WRAP));
        ImageLoader.get(this).load(d.image, iv, d.category, d.title);
        TextView off = offBadge(this, d.percentOff());
        off.setTextSize(14);
        FrameLayout.LayoutParams op = new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP | Gravity.START);
        op.setMargins(dp(this, 12), dp(this, 12), 0, 0);
        imgBox.addView(off, op);
        // keep the photo from taking the whole screen
        LinearLayout.LayoutParams ip = lp(MATCH, WRAP);
        ip.setMargins(dp(this, 60), dp(this, 12), dp(this, 60), 0);
        col.addView(imgBox, ip);

        LinearLayout info = col(this);
        info.setPadding(dp(this, 16), dp(this, 14), dp(this, 16), dp(this, 16));
        LinearLayout badges = row(this);
        badges.addView(storeBadge(this, d.store));
        TextView catT = tv(this, Cats.hi(d.category) + " · " + Cats.en(d.category), 12, TEXT2, false);
        catT.setPadding(dp(this, 8), 0, 0, 0);
        badges.addView(catT);
        info.addView(badges);
        info.addView(tv(this, d.title, 19, TEXT, true), margins(lp(MATCH, WRAP), this, 0, 8, 0, 8));

        LinearLayout pr = row(this);
        pr.setGravity(Gravity.BOTTOM);
        pr.addView(tv(this, Deal.rupees(d.price), 28, TEXT, true));
        TextView mrp = strike(tv(this, "MRP " + Deal.rupees(d.mrp), 15, TEXT2, false));
        mrp.setPadding(dp(this, 10), 0, 0, dp(this, 4));
        pr.addView(mrp);
        TextView pct = tv(this, d.percentOff() + "% छूट", 15, d.isLoot() ? RED : GREEN, true);
        pct.setPadding(dp(this, 10), 0, 0, dp(this, 4));
        pr.addView(pct);
        info.addView(pr);

        TextView save = tv(this, "आप बचाते हैं " + Deal.rupees(d.savings()) + "  ·  You save " + Deal.rupees(d.savings()), 15, GREEN, true);
        save.setBackground(round(GREEN_LIGHT, dp(this, 10)));
        save.setPadding(dp(this, 12), dp(this, 10), dp(this, 12), dp(this, 10));
        info.addView(save, margins(lp(MATCH, WRAP), this, 0, 12, 0, 0));

        info.addView(tv(this, "विवरण · Description", 15, TEXT, true), margins(lp(WRAP, WRAP), this, 0, 16, 0, 4));
        TextView desc = tv(this, d.description, 14, TEXT, false);
        desc.setLineSpacing(0, 1.15f);
        info.addView(desc);
        TextView note = tv(this, "दाम और स्टॉक स्टोर पर बदल सकते हैं। ख़रीदारी " + d.store
                + " पर होगी। यह लिंक affiliate हो सकता है — आपके लिए दाम नहीं बदलता।", 12, TEXT2, false);
        info.addView(note, margins(lp(MATCH, WRAP), this, 0, 14, 0, 0));
        col.addView(info);

        // bottom action bar
        View div = new View(this);
        div.setBackgroundColor(DIVIDER);
        root.addView(div, lp(MATCH, 1));
        LinearLayout act = row(this);
        act.setBackgroundColor(SURFACE);
        act.setPadding(dp(this, 12), dp(this, 10), dp(this, 12), dp(this, 10));
        LinearLayout fav = row(this);
        fav.setGravity(Gravity.CENTER);
        fav.setPadding(dp(this, 12), dp(this, 12), dp(this, 14), dp(this, 12));
        fav.setBackground(stroke(SURFACE, ORANGE, dp(this, 1), dp(this, 24)));
        favIcon = icon(this, R.drawable.ic_heart_o, ORANGE, 22);
        fav.addView(favIcon);
        favText = tv(this, "", 14, ORANGE, true);
        favText.setPadding(dp(this, 6), 0, 0, 0);
        fav.addView(favText);
        fav.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean on = Saved.toggleFav(DetailActivity.this, d);
                syncFav();
                Toast.makeText(DetailActivity.this, on ? "पसंदीदा में सेव हुआ" : "पसंदीदा से हटाया", Toast.LENGTH_SHORT).show();
            }
        });
        act.addView(fav);
        TextView buy = tv(this, "ख़रीदें · Buy on " + d.store, 16, 0xFFFFFFFF, true);
        buy.setGravity(Gravity.CENTER);
        buy.setPadding(dp(this, 12), dp(this, 13), dp(this, 12), dp(this, 13));
        Ui.ripple(buy, GREEN, dp(this, 24));
        buy.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { buy(); } });
        LinearLayout.LayoutParams bp = lpw(1);
        bp.setMargins(dp(this, 10), 0, 0, 0);
        act.addView(buy, bp);
        root.addView(act, lp(MATCH, WRAP));
        setContentView(root);
        syncFav();
    }

    private ImageView barIcon(int res, String desc) {
        ImageView i = icon(this, res, 0xFFFFFFFF, 24);
        i.setLayoutParams(lp(dp(this, 48), dp(this, 48)));
        i.setPadding(dp(this, 12), dp(this, 12), dp(this, 12), dp(this, 12));
        i.setContentDescription(desc);
        i.setBackgroundResource(android.R.drawable.list_selector_background);
        return i;
    }

    private void syncFav() {
        boolean on = Saved.isFav(this, d.id);
        favIcon.setImageResource(on ? R.drawable.ic_heart : R.drawable.ic_heart_o);
        favText.setText(on ? "सेव है" : "सेव करें");
    }

    private void buy() {
        if (d.link == null || d.link.isEmpty()) { Toast.makeText(this, "लिंक उपलब्ध नहीं", Toast.LENGTH_SHORT).show(); return; }
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(d.link));
        i.addCategory(Intent.CATEGORY_BROWSABLE);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "इसे खोलने के लिए कोई ब्राउज़र नहीं मिला", Toast.LENGTH_LONG).show();
        }
    }

    private void share() {
        String text = d.title + "\n" + Deal.rupees(d.price) + " (MRP " + Deal.rupees(d.mrp) + ", " + d.percentOff() + "% छूट) — "
                + d.store + "\n" + d.link + "\n\nबचत मेला ऐप पर मिली डील";
        Intent s = new Intent(Intent.ACTION_SEND);
        s.setType("text/plain");
        s.putExtra(Intent.EXTRA_SUBJECT, d.title);
        s.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(s, "डील शेयर करें"));
    }
}
