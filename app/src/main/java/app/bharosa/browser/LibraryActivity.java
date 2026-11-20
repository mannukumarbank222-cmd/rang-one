package app.bharosa.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Bookmarks, history, downloads and offline pages lists. */
public class LibraryActivity extends Activity {
    public static final String EXTRA_MODE = "mode";
    public static final int MODE_BOOKMARKS = 0, MODE_HISTORY = 1, MODE_DOWNLOADS = 2, MODE_OFFLINE = 3;

    private int mode;
    private final List<Db.Item> all = new ArrayList<>();
    private final List<Db.Item> shown = new ArrayList<>();
    private Adapter adapter;
    private TextView empty;
    private String filter = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (mode == MODE_DOWNLOADS) { adapter.notifyDataSetChanged(); handler.postDelayed(this, 1500); }
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        Ui.apply(this);
        super.onCreate(b);
        mode = getIntent().getIntExtra(EXTRA_MODE, MODE_BOOKMARKS);
        String[] titles = {L.t("bookmarks"), L.t("history"), L.t("downloads"), L.t("offline")};

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Ui.bg);
        LinearLayout bar = Ui.topBar(this, titles[mode]);
        if (mode == MODE_HISTORY || mode == MODE_DOWNLOADS) {
            TextView act = Ui.text(this, mode == MODE_HISTORY ? L.t("clear") : L.t("open"), 14, Ui.accent, true);
            act.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 12), Ui.dp(this, 10));
            act.setBackground(Ui.rippleRound(Color.TRANSPARENT, Ui.dp(this, 18)));
            act.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (mode == MODE_HISTORY) confirmClearHistory();
                    else {
                        try { startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)); } catch (Exception e) { toast(L.t("cant_open")); }
                    }
                }
            });
            bar.addView(act);
        }
        root.addView(bar);
        root.addView(Ui.divider(this));

        if (mode == MODE_HISTORY || mode == MODE_BOOKMARKS) {
            EditText search = new EditText(this);
            search.setSingleLine(true);
            search.setHint(L.t("search_hint").split(" ")[0] + "…");
            search.setTextColor(Ui.text);
            search.setHintTextColor(Ui.text2);
            search.setTextSize(15);
            int p = Ui.dp(this, 16);
            search.setPadding(p, 0, p, 0);
            search.setBackground(Ui.round(Ui.surface2, Ui.dp(this, 22)));
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) { }
                @Override public void onTextChanged(CharSequence s, int a, int c, int d) { }
                @Override public void afterTextChanged(Editable s) { filter = s.toString().toLowerCase(Locale.ROOT); applyFilter(); }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 44));
            lp.setMargins(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 4));
            root.addView(search, lp);
        }

        empty = Ui.text(this, "", 15, Ui.text2, false);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(Ui.dp(this, 32), Ui.dp(this, 48), Ui.dp(this, 32), 0);
        root.addView(empty, new LinearLayout.LayoutParams(-1, -2));

        ListView lv = new ListView(this);
        lv.setDivider(null);
        lv.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
        lv.setClipToPadding(false);
        adapter = new Adapter();
        lv.setAdapter(adapter);
        root.addView(lv, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        reload();
    }

    @Override protected void onResume() { super.onResume(); handler.post(tick); }
    @Override protected void onPause() { super.onPause(); handler.removeCallbacks(tick); }

    private void reload() {
        Db db = Db.get(this);
        all.clear();
        if (mode == MODE_BOOKMARKS) all.addAll(db.bookmarks());
        else if (mode == MODE_HISTORY) all.addAll(db.history(1000));
        else if (mode == MODE_DOWNLOADS) all.addAll(db.downloads());
        else all.addAll(db.offline());
        applyFilter();
    }

    private void applyFilter() {
        shown.clear();
        for (Db.Item it : all) {
            if (filter.isEmpty() || (it.title != null && it.title.toLowerCase(Locale.ROOT).contains(filter)) || (it.url != null && it.url.toLowerCase(Locale.ROOT).contains(filter)))
                shown.add(it);
        }
        String[] e = {L.t("empty_bookmarks"), L.t("empty_history"), L.t("empty_downloads"), L.t("empty_offline")};
        empty.setText(e[mode]);
        empty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    private void confirmClearHistory() {
        new AlertDialog.Builder(this).setTitle(L.t("clear_history"))
                .setPositiveButton(L.t("clear"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { Db.get(LibraryActivity.this).clearHistory(); reload(); }
                }).setNegativeButton(L.t("cancel"), null).show();
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    private void open(Db.Item it) {
        if (mode == MODE_DOWNLOADS) { openDownload(it); return; }
        Intent r = new Intent();
        r.putExtra("url", it.url);
        if (mode == MODE_OFFLINE) {
            r.putExtra(MainActivity.EXTRA_OFFLINE_PATH, it.extra);
            r.putExtra(MainActivity.EXTRA_TITLE, it.title);
        }
        setResult(RESULT_OK, r);
        finish();
    }

    private void openDownload(Db.Item it) {
        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        int[] st = status(dm, it.dmId);
        if (st[0] != DownloadManager.STATUS_SUCCESSFUL) return;
        try {
            Uri u = dm.getUriForDownloadedFile(it.dmId);
            String mime = dm.getMimeTypeForDownloadedFile(it.dmId);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setDataAndType(u, mime == null ? "*/*" : mime);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(i);
        } catch (Exception e) {
            toast(L.t("cant_open"));
        }
    }

    private void delete(final Db.Item it) {
        new AlertDialog.Builder(this).setTitle(L.t("delete")).setMessage(it.title)
                .setPositiveButton(L.t("delete"), new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) {
                        Db db = Db.get(LibraryActivity.this);
                        if (mode == MODE_BOOKMARKS) db.removeBookmark(it.url);
                        else if (mode == MODE_HISTORY) db.deleteHistory(it.id);
                        else if (mode == MODE_OFFLINE) db.deleteOffline(it);
                        else {
                            try { ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).remove(it.dmId); } catch (Exception ignored) { }
                            db.deleteDownload(it.id);
                        }
                        reload();
                    }
                }).setNegativeButton(L.t("cancel"), null).show();
    }

    /** {status, downloadedBytes, totalBytes} */
    private static int[] status(DownloadManager dm, long id) {
        int[] r = {-1, 0, 0};
        Cursor c = null;
        try {
            c = dm.query(new DownloadManager.Query().setFilterById(id));
            if (c != null && c.moveToFirst()) {
                r[0] = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                r[1] = (int) Math.min(Integer.MAX_VALUE, c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
                r[2] = (int) Math.min(Integer.MAX_VALUE, c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
            }
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
        return r;
    }

    private String subtitle(Db.Item it) {
        String when = DateUtils.getRelativeTimeSpanString(it.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString();
        if (mode == MODE_DOWNLOADS) {
            int[] st = status((DownloadManager) getSystemService(DOWNLOAD_SERVICE), it.dmId);
            String s;
            switch (st[0]) {
                case DownloadManager.STATUS_SUCCESSFUL: s = L.t("done") + " · " + Formatter.formatShortFileSize(this, st[2]); break;
                case DownloadManager.STATUS_RUNNING:
                    s = L.t("running") + (st[2] > 0 ? " · " + (st[1] * 100L / st[2]) + "%" : ""); break;
                case DownloadManager.STATUS_PENDING: s = L.t("pending"); break;
                case DownloadManager.STATUS_PAUSED: s = L.t("paused"); break;
                default: s = L.t("failed");
            }
            return s + " · " + Ui.hostOf(it.url);
        }
        return Ui.hostOf(it.url) + " · " + when;
    }

    private final class Adapter extends BaseAdapter {
        @Override public int getCount() { return shown.size(); }
        @Override public Object getItem(int p) { return shown.get(p); }
        @Override public long getItemId(int p) { return shown.get(p).id; }

        @Override
        public View getView(int pos, View convert, ViewGroup parent) {
            final Db.Item it = shown.get(pos);
            LinearLayout row = new LinearLayout(LibraryActivity.this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int p = Ui.dp(LibraryActivity.this, 14);
            row.setPadding(p, Ui.dp(LibraryActivity.this, 10), Ui.dp(LibraryActivity.this, 4), Ui.dp(LibraryActivity.this, 10));
            row.setBackground(Ui.rippleRound(Color.TRANSPARENT, 0));
            View lead;
            if (mode == MODE_DOWNLOADS || mode == MODE_OFFLINE) {
                ImageView iv = new ImageView(LibraryActivity.this);
                iv.setImageResource(mode == MODE_DOWNLOADS ? R.drawable.ic_download : R.drawable.ic_save);
                iv.setColorFilter(Ui.accent);
                int ip = Ui.dp(LibraryActivity.this, 9);
                iv.setPadding(ip, ip, ip, ip);
                iv.setBackground(Ui.round(Ui.dark ? 0xFF33261C : 0xFFFDEBDF, Ui.dp(LibraryActivity.this, 20)));
                iv.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(LibraryActivity.this, 40), Ui.dp(LibraryActivity.this, 40)));
                lead = iv;
            } else {
                lead = Ui.letterTile(LibraryActivity.this, Ui.hostOf(it.url), it.url, 40);
            }
            row.addView(lead);
            LinearLayout tx = new LinearLayout(LibraryActivity.this);
            tx.setOrientation(LinearLayout.VERTICAL);
            tx.setPadding(p, 0, 0, 0);
            tx.addView(Ui.single(LibraryActivity.this, it.title, 15, Ui.text, false));
            tx.addView(Ui.single(LibraryActivity.this, subtitle(it), 12, Ui.text2, false));
            row.addView(tx, new LinearLayout.LayoutParams(0, -2, 1));
            ImageView del = Ui.icon(LibraryActivity.this, R.drawable.ic_delete, Ui.text2, 44, L.t("delete"));
            del.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { delete(it); } });
            row.addView(del);
            row.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { open(it); } });
            return row;
        }
    }
}
