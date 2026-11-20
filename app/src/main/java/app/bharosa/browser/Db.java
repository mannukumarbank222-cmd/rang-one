package app.bharosa.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Local-only storage: bookmarks, history, downloads list, offline pages, speed dial. */
public final class Db extends SQLiteOpenHelper {
    private static Db inst;

    public static synchronized Db get(Context c) {
        if (inst == null) inst = new Db(c.getApplicationContext());
        return inst;
    }

    private Db(Context c) { super(c, "rangone.db", null, 1); }

    public static final class Item {
        public long id;
        public String title;
        public String url;
        public String extra; // file path for offline pages
        public long time;
        public long dmId;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE bookmarks(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT UNIQUE, ts INTEGER)");
        db.execSQL("CREATE TABLE history(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, ts INTEGER)");
        db.execSQL("CREATE TABLE downloads(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, dm_id INTEGER, ts INTEGER)");
        db.execSQL("CREATE TABLE offline(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, path TEXT, ts INTEGER)");
        db.execSQL("CREATE TABLE speeddial(_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, url TEXT, pos INTEGER)");
        String[][] d = {
                {"Google", "https://www.google.com/"},
                {"YouTube", "https://m.youtube.com/"},
                {"विकिपीडिया", "https://hi.wikipedia.org/"},
                {"News", "https://news.google.com/?hl=hi&gl=IN&ceid=IN:hi"},
                {"India.gov.in", "https://www.india.gov.in/"},
                {"IRCTC", "https://www.irctc.co.in/"},
                {"मौसम", "https://www.google.com/search?q=weather"},
                {"Maps", "https://www.google.com/maps"},
        };
        for (int i = 0; i < d.length; i++) {
            ContentValues v = new ContentValues();
            v.put("title", d[i][0]);
            v.put("url", d[i][1]);
            v.put("pos", i);
            db.insert("speeddial", null, v);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int o, int n) { }

    private static Item read(Cursor c, String extraCol, boolean dm) {
        Item it = new Item();
        it.id = c.getLong(c.getColumnIndexOrThrow("_id"));
        it.title = c.getString(c.getColumnIndexOrThrow("title"));
        it.url = c.getString(c.getColumnIndexOrThrow("url"));
        if (extraCol != null) it.extra = c.getString(c.getColumnIndexOrThrow(extraCol));
        if (dm) it.dmId = c.getLong(c.getColumnIndexOrThrow("dm_id"));
        int ti = c.getColumnIndex("ts");
        if (ti >= 0) it.time = c.getLong(ti);
        return it;
    }

    private List<Item> list(String table, String order, String extraCol, boolean dm, int limit) {
        ArrayList<Item> out = new ArrayList<>();
        Cursor c = getReadableDatabase().query(table, null, null, null, null, null, order, limit > 0 ? String.valueOf(limit) : null);
        try {
            while (c.moveToNext()) out.add(read(c, extraCol, dm));
        } finally {
            c.close();
        }
        return out;
    }

    // Bookmarks
    public List<Item> bookmarks() { return list("bookmarks", "ts DESC", null, false, 0); }

    public boolean isBookmarked(String url) {
        if (url == null) return false;
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM bookmarks WHERE url=?", new String[]{url});
        try { return c.moveToFirst(); } finally { c.close(); }
    }

    public void addBookmark(String title, String url) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("url", url);
        v.put("ts", System.currentTimeMillis());
        getWritableDatabase().insertWithOnConflict("bookmarks", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void removeBookmark(String url) { getWritableDatabase().delete("bookmarks", "url=?", new String[]{url}); }

    // History
    public List<Item> history(int limit) { return list("history", "ts DESC", null, false, limit); }

    public void addHistory(String title, String url) {
        if (url == null || !(url.startsWith("http://") || url.startsWith("https://"))) return;
        SQLiteDatabase db = getWritableDatabase();
        db.delete("history", "url=?", new String[]{url});
        ContentValues v = new ContentValues();
        v.put("title", title == null || title.isEmpty() ? url : title);
        v.put("url", url);
        v.put("ts", System.currentTimeMillis());
        db.insert("history", null, v);
    }

    public void updateHistoryTitle(String url, String title) {
        if (url == null || title == null || title.isEmpty()) return;
        ContentValues v = new ContentValues();
        v.put("title", title);
        getWritableDatabase().update("history", v, "url=?", new String[]{url});
    }

    public void deleteHistory(long id) { getWritableDatabase().delete("history", "_id=?", new String[]{String.valueOf(id)}); }

    public void clearHistory() { getWritableDatabase().delete("history", null, null); }

    // Downloads
    public List<Item> downloads() { return list("downloads", "ts DESC", null, true, 0); }

    public void addDownload(String title, String url, long dmId) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("url", url);
        v.put("dm_id", dmId);
        v.put("ts", System.currentTimeMillis());
        getWritableDatabase().insert("downloads", null, v);
    }

    public void deleteDownload(long id) { getWritableDatabase().delete("downloads", "_id=?", new String[]{String.valueOf(id)}); }

    // Offline pages
    public List<Item> offline() { return list("offline", "ts DESC", "path", false, 0); }

    public void addOffline(String title, String url, String path) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("url", url);
        v.put("path", path);
        v.put("ts", System.currentTimeMillis());
        getWritableDatabase().insert("offline", null, v);
    }

    public void deleteOffline(Item it) {
        if (it.extra != null) new File(it.extra).delete();
        getWritableDatabase().delete("offline", "_id=?", new String[]{String.valueOf(it.id)});
    }

    public void clearOffline() {
        for (Item it : offline()) deleteOffline(it);
    }

    public void clearBookmarks() { getWritableDatabase().delete("bookmarks", null, null); }

    // Speed dial
    public List<Item> speedDial() { return list("speeddial", "pos ASC, _id ASC", null, false, 0); }

    public void addSpeedDial(String title, String url) {
        ContentValues v = new ContentValues();
        v.put("title", title);
        v.put("url", url);
        v.put("pos", 1000);
        getWritableDatabase().insert("speeddial", null, v);
    }

    public void removeSpeedDial(long id) { getWritableDatabase().delete("speeddial", "_id=?", new String[]{String.valueOf(id)}); }
}
