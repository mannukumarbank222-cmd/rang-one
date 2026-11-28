package app.bachatmela.deals;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import static app.bachatmela.deals.Ui.*;

/** Deal cards in grid and list form. */
public final class DealViews {

    public static void open(Activity a, Deal d) {
        Intent i = new Intent(a, DetailActivity.class);
        i.putExtra("id", d.id);
        i.putExtra("json", d.toJson().toString());
        a.startActivity(i);
    }

    private static FrameLayout shell(final Activity a, final Deal d, View inner) {
        FrameLayout f = new FrameLayout(a);
        f.setBackground(round(SURFACE, dp(a, 12)));
        f.setElevation(dp(a, 2));
        f.setForeground(new RippleDrawable(ColorStateList.valueOf(0x22F26B1D), null, round(0xFFFFFFFF, dp(a, 12))));
        f.setClipToOutline(true);
        f.addView(inner);
        f.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { open(a, d); } });
        f.setContentDescription(d.title);
        return f;
    }

    private static LinearLayout priceRow(Activity a, Deal d, float sp) {
        LinearLayout pr = row(a);
        pr.setGravity(Gravity.BOTTOM);
        pr.addView(tv(a, Deal.rupees(d.price), sp, TEXT, true));
        TextView mrp = strike(tv(a, Deal.rupees(d.mrp), sp - 4, TEXT2, false));
        LinearLayout.LayoutParams p = lp(WRAP, WRAP);
        p.setMargins(dp(a, 6), 0, 0, dp(a, 1));
        pr.addView(mrp, p);
        return pr;
    }

    public static View gridCard(Activity a, Deal d) {
        LinearLayout col = col(a);
        FrameLayout imgBox = new FrameLayout(a);
        SquareImageView iv = new SquareImageView(a);
        imgBox.addView(iv, new FrameLayout.LayoutParams(MATCH, WRAP));
        ImageLoader.get(a).load(d.image, iv, d.category, d.title);
        TextView off = offBadge(a, d.percentOff());
        FrameLayout.LayoutParams op = new FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP | Gravity.START);
        op.setMargins(dp(a, 8), dp(a, 8), 0, 0);
        imgBox.addView(off, op);
        col.addView(imgBox, lp(MATCH, WRAP));

        LinearLayout info = col(a);
        info.setPadding(dp(a, 10), dp(a, 8), dp(a, 10), dp(a, 10));
        info.addView(storeBadge(a, d.store), lp(WRAP, WRAP));
        TextView title = ellipsize(tv(a, d.title, 13, TEXT, false), 2);
        title.setMinLines(2);
        info.addView(title, margins(lp(MATCH, WRAP), a, 0, 5, 0, 4));
        info.addView(priceRow(a, d, 16));
        TextView save = tv(a, Deal.rupees(d.savings()) + " की बचत", 11, GREEN, true);
        info.addView(save, margins(lp(WRAP, WRAP), a, 0, 2, 0, 0));
        col.addView(info);
        return shell(a, d, col);
    }

    public static View listCard(Activity a, Deal d) {
        LinearLayout r = row(a);
        r.setGravity(Gravity.TOP);
        r.setPadding(dp(a, 10), dp(a, 10), dp(a, 10), dp(a, 10));
        ImageView iv = new ImageView(a);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackground(round(DIVIDER, dp(a, 8)));
        iv.setClipToOutline(true);
        r.addView(iv, lp(dp(a, 88), dp(a, 88)));
        ImageLoader.get(a).load(d.image, iv, d.category, d.title);
        LinearLayout info = col(a);
        info.setPadding(dp(a, 12), 0, 0, 0);
        LinearLayout badges = row(a);
        badges.addView(storeBadge(a, d.store));
        TextView off = offBadge(a, d.percentOff());
        badges.addView(off, margins(lp(WRAP, WRAP), a, 6, 0, 0, 0));
        info.addView(badges);
        info.addView(ellipsize(tv(a, d.title, 14, TEXT, false), 2), margins(lp(MATCH, WRAP), a, 0, 5, 0, 4));
        info.addView(priceRow(a, d, 16));
        r.addView(info, lpw(1));
        return shell(a, d, r);
    }

    /** Lays deals out two per row (grid) or one per row (list). */
    public static void fill(Activity a, LinearLayout container, List<Deal> deals, boolean grid) {
        container.removeAllViews();
        int gap = dp(a, 6);
        if (!grid) {
            for (Deal d : deals) {
                LinearLayout.LayoutParams p = lp(MATCH, WRAP);
                p.setMargins(dp(a, 12), gap, dp(a, 12), gap);
                container.addView(listCard(a, d), p);
            }
            return;
        }
        for (int i = 0; i < deals.size(); i += 2) {
            LinearLayout r = row(a);
            r.setGravity(Gravity.TOP);
            r.setPadding(dp(a, 6), 0, dp(a, 6), 0);
            r.setBaselineAligned(false);
            for (int j = i; j < i + 2; j++) {
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, WRAP, 1);
                p.setMargins(gap, gap, gap, gap);
                if (j < deals.size()) r.addView(gridCard(a, deals.get(j)), p);
                else r.addView(new View(a), p);
            }
            container.addView(r, lp(MATCH, WRAP));
        }
    }

    private DealViews() { }
}
