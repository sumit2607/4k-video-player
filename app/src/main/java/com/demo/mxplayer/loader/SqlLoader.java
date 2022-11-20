package com.demo.mxplayer.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.provider.MediaStore.Video.Media;

import java.util.HashSet;

public class SqlLoader {
    protected static final String TAG = "SqlLoader";

    private SqlLoader() {
    }

    public static Loader<HashSet<String>> initVideosAlbums(final Context ctx) {
        return new AsyncTaskLoader<HashSet<String>>(ctx) {
            public HashSet<String> loadInBackground() {
                HashSet<String> map = new HashSet();
                Cursor cursor = ctx.getContentResolver().query(Media.EXTERNAL_CONTENT_URI, new String[]{"bucket_display_name", "_data"}, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        map.add(cursor.getString(cursor.getColumnIndex("bucket_display_name")));
                    } while (cursor.moveToNext());
                    cursor.close();
                }
                return map;
            }
        };
    }
}
