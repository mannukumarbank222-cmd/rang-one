package app.bachatmela.deals;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/** Coloured tile with the first letter of the deal, shown while loading or when the image fails. */
public class Placeholder extends Drawable {
    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG), fg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String letter;

    public Placeholder(int color, String label) {
        bg.setColor(color);
        bg.setAlpha(60);
        fg.setColor(color);
        fg.setTypeface(Typeface.DEFAULT_BOLD);
        fg.setTextAlign(Paint.Align.CENTER);
        letter = label == null || label.isEmpty() ? "?" : label.substring(0, 1).toUpperCase();
    }

    @Override public void draw(Canvas c) {
        Rect r = getBounds();
        c.drawRect(r, bg);
        fg.setTextSize(Math.min(r.width(), r.height()) * 0.4f);
        float y = r.exactCenterY() - (fg.descent() + fg.ascent()) / 2f;
        c.drawText(letter, r.exactCenterX(), y, fg);
    }
    @Override public void setAlpha(int a) { }
    @Override public void setColorFilter(ColorFilter cf) { }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
