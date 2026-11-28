package app.bachatmela.deals;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Tiny async image loader: memory LRU + disk cache, no third-party libraries. */
public final class ImageLoader {
    private static ImageLoader inst;
    private final LruCache<String, Bitmap> mem;
    private final File dir;
    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private final Handler main = new Handler(Looper.getMainLooper());

    public static synchronized ImageLoader get(Context c) {
        if (inst == null) inst = new ImageLoader(c.getApplicationContext());
        return inst;
    }

    private ImageLoader(Context c) {
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024 / 8);
        mem = new LruCache<String, Bitmap>(maxKb) {
            @Override protected int sizeOf(String k, Bitmap b) { return b.getByteCount() / 1024; }
        };
        dir = new File(c.getCacheDir(), "img");
        if (!dir.exists()) dir.mkdirs();
    }

    /** Shows the placeholder immediately, then the real image when it arrives (if the view still wants it). */
    public void load(final String url, final ImageView iv, final String category, final String label) {
        iv.setTag(R.id.tag_url, url);
        iv.setImageDrawable(new Placeholder(Cats.color(category), label));
        if (url == null || url.isEmpty()) return;
        Bitmap b = mem.get(url);
        if (b != null) { iv.setImageBitmap(b); return; }
        pool.execute(new Runnable() {
            public void run() {
                final Bitmap bm = fetch(url);
                if (bm == null) return;
                mem.put(url, bm);
                main.post(new Runnable() {
                    public void run() {
                        if (url.equals(iv.getTag(R.id.tag_url))) iv.setImageBitmap(bm);
                    }
                });
            }
        });
    }

    private Bitmap fetch(String url) {
        File f = new File(dir, md5(url));
        try {
            if (f.exists() && f.length() > 0) {
                Bitmap b = decode(f);
                if (b != null) return b;
            }
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setConnectTimeout(8000);
            con.setReadTimeout(12000);
            con.setInstanceFollowRedirects(true);
            con.setRequestProperty("User-Agent", "BachatMela/1.0 (Android)");
            if (con.getResponseCode() != 200) { con.disconnect(); return null; }
            File tmp = new File(dir, f.getName() + ".tmp");
            InputStream in = con.getInputStream();
            OutputStream out = new FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            out.close();
            in.close();
            con.disconnect();
            if (!tmp.renameTo(f)) return null;
            return decode(f);
        } catch (Exception e) {
            return null;
        }
    }

    private static Bitmap decode(File f) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), o);
        int s = 1;
        while (o.outWidth / (s * 2) >= 480 && o.outHeight / (s * 2) >= 480) s *= 2;
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = s;
        return BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
    }

    private static String md5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}
