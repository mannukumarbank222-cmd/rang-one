package app.bachatmela.deals;

import android.content.Context;
import android.widget.ImageView;

public class SquareImageView extends ImageView {
    public float ratio = 1f;  // height / width
    public SquareImageView(Context c) {
        super(c);
        setScaleType(ScaleType.CENTER_CROP);
    }
    @Override protected void onMeasure(int w, int h) {
        super.onMeasure(w, w);
        int s = getMeasuredWidth();
        setMeasuredDimension(s, (int) (s * ratio));
    }
}
