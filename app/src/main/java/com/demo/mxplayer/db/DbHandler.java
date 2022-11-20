package com.demo.mxplayer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.demo.mxplayer.App;
import com.demo.mxplayer.models.DownloaderThread;
import com.demo.mxplayer.models.SingleFile;
import com.demo.mxplayer.models.SingleOverview;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "mx_player_video.sqlite";
    public static final String TAG = "DbHandler";
    private static final String TABLE_FAVORITE_VIDEO = "table_favorite_video";
    private static final String TABLE_LAST_VIEW = "table_last_view";
    public static final String TABLE_LOCK_VIDEO = "table_lock_video";
    public static final String TABLE_LOCK_FOLDER = "table_lock_folder";
    private static final String TABLE_LAST_PLAY = "table_last_play";
    private static final String TABLE_ALLVIDEO = "table_all_video";
    private static final String TABLE_ALLSONG = "table_all_song";
    private static final String _ID = "_id";
    public static final String DB_PATH;
    public static SQLiteDatabase db;

    public DbHandler(Context context, String str, SQLiteDatabase.CursorFactory cursorFactory, int i) {
        super(context, str, cursorFactory, i);
    }

    public DbHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        db = getWritableDatabase();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_lock_video ( _id INTEGER PRIMARY KEY AUTOINCREMENT, track_id TEXT,track_name TEXT,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_lock_folder ( _id INTEGER PRIMARY KEY AUTOINCREMENT, track_id TEXT,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_favorite_video ( _id INTEGER PRIMARY KEY AUTOINCREMENT, position_video INTEGER DEFAULT 0, channel_id VARCHAR, channel_title VARCHAR, track_id TEXT, track_name TEXT, track_duration VARCHAR,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_last_view ( _id INTEGER PRIMARY KEY AUTOINCREMENT, other TEXT, track_id TEXT, position_video INTEGER DEFAULT 0,track_name TEXT,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_last_play ( _id INTEGER PRIMARY KEY AUTOINCREMENT, other TEXT, track_id TEXT, position_track INTEGER DEFAULT 0,track_name TEXT,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_all_video ( _id INTEGER PRIMARY KEY AUTOINCREMENT, track_id VARCHAR, track_name TEXT, track_duration VARCHAR,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS table_all_song ( _id INTEGER PRIMARY KEY AUTOINCREMENT, track_id VARCHAR, track_name TEXT, track_duration VARCHAR,date_added DATETIME DEFAULT CURRENT_TIMESTAMP);");


        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Files (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , url VARCHAR, save_path VARCHAR, filename VARCHAR, status INTEGER, size DOUBLE, completed DOUBLE, type_id INTEGER,singlethread BOOL,username VARCHAR,password VARCHAR, created DATETIME,completedtime DATETIME)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Threads (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , file_id INTEGER, part_no INTEGER,status INTEGER,part_path VARCHAR, part_name VARCHAR, from_bytes DOUBLE, to_bytes DOUBLE, completed_bytes DOUBLE)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Types (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , title VARCHAR, save_path VARCHAR)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Extensions (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , type_id INTEGER, extension VARCHAR)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS Settings (settings_key VARCHAR NOT NULL  UNIQUE , settings_value VARCHAR)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WebHistory (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,url VARCHAR,page_title VARCHAR,favicon_path VARCHAR,created DATETIME)");
        sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS WebBookmarks (id INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,url VARCHAR,page_title VARCHAR,favicon_path VARCHAR,created DATETIME)");
        String[] strArr = new String[3];
        strArr[0] = "1";
        strArr[1] = "Music";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(App.sdPath);
        stringBuilder.append("/Music");
        strArr[2] = stringBuilder.toString();
        sQLiteDatabase.execSQL("INSERT INTO Types (id,title,save_path) VALUES (?,?,?)", strArr);
        strArr = new String[3];
        strArr[0] = "2";
        strArr[1] = "Video";
        stringBuilder = new StringBuilder();
        stringBuilder.append(App.sdPath);
        stringBuilder.append("/Video");
        strArr[2] = stringBuilder.toString();
        sQLiteDatabase.execSQL("INSERT INTO Types (id,title,save_path) VALUES (?,?,?)", strArr);
        strArr = new String[3];
        strArr[0] = "3";
        strArr[1] = "Documents";
        stringBuilder = new StringBuilder();
        stringBuilder.append(App.sdPath);
        stringBuilder.append("/Document");
        strArr[2] = stringBuilder.toString();
        sQLiteDatabase.execSQL("INSERT INTO Types (id,title,save_path) VALUES (?,?,?)", strArr);
        strArr = new String[3];
        strArr[0] = "4";
        strArr[1] = "Compressed";
        stringBuilder = new StringBuilder();
        stringBuilder.append(App.sdPath);
        stringBuilder.append("/Compressed");
        strArr[2] = stringBuilder.toString();
        sQLiteDatabase.execSQL("INSERT INTO Types (id,title,save_path) VALUES (?,?,?)", strArr);
        strArr = new String[3];
        strArr[0] = "5";
        strArr[1] = "Images";
        stringBuilder = new StringBuilder();
        stringBuilder.append(App.sdPath);
        stringBuilder.append("/Image");
        strArr[2] = stringBuilder.toString();
        sQLiteDatabase.execSQL("INSERT INTO Types (id,title,save_path) VALUES (?,?,?)", strArr);
        String[] strArr2 = new String[3];
        strArr2[0] = "6";
        strArr2[1] = "Executable";
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(App.sdPath);
        stringBuilder2.append("/Executable");
        strArr2[2] = stringBuilder2.toString();
        sQLiteDatabase.execSQL("INSERT INTO Types (id,title,save_path) VALUES (?,?,?)", strArr2);
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"1", ".mp3"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"1", ".mp+"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"1", ".ogg"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"1", ".mp2"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"1", ".m4a"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".mp4"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".3gp"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".3gpp"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".mov"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".avi"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".wmv"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".mkv"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".movie"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".anim"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".movie"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".wma"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".mpeg"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"2", ".flv"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".pdf"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".doc"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".docx"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".ppt"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".pptx"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".xls"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".xlsx"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".xml"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".txt"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".htm"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".html"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".csv"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"3", ".epub"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"4", ".zip"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"4", ".rar"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"4", ".7z"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"4", ".tar"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"4", ".gz"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"4", ".tbz"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"5", ".png"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"5", ".jpg"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"5", ".jpeg"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"5", ".psd"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"5", ".png"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"5", ".gif"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"6", ".apk"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"6", ".exe"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"6", ".dmg"});
        sQLiteDatabase.execSQL("INSERT INTO Extensions (type_id,extension) VALUES (?,?)", new String[]{"6", ".ipa"});

    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/data/data/");
        stringBuilder.append(App.class.getPackage().getName());
        stringBuilder.append("/databases/");
        stringBuilder.append(DB_NAME);
        DB_PATH = stringBuilder.toString();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITE_VIDEO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_VIEW);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCK_VIDEO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCK_FOLDER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAST_PLAY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALLVIDEO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALLSONG);
        // Create tables again
        onCreate(db);
    }


    public void insert_all_song(String track_id, String track_name, String track_duration) {
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put("track_id", track_id);
        cValues.put("track_name", track_name);
        cValues.put("track_duration", track_duration);
        long newRowId = db.insert(TABLE_ALLSONG, null, cValues);
        db.close();
    }


    public boolean CheckIsSongexits(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_ALLSONG + " where " + dbfield + " = " + fieldValue;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public void insert_all_video(String track_id, String track_name, String track_duration) {
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put("track_id", track_id);
        cValues.put("track_name", track_name);
        cValues.put("track_duration", track_duration);
        long newRowId = db.insert(TABLE_ALLVIDEO, null, cValues);
        db.close();
    }

    public static void openDB() {
        if (db == null || !db.isOpen()) {
            db = SQLiteDatabase.openDatabase(DB_PATH, null, 0);
        }
    }

    public static void closeDB() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public void insert_lock_video(String track_id) {
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put("track_id", track_id);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_LOCK_VIDEO, null, cValues);
        db.close();
    }

    public void insert_lock_folder(String track_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put("track_id", track_id);

        long newRowId = db.insert(TABLE_LOCK_FOLDER, null, cValues);
        db.close();
    }

    public void insert_video_favorite(String track_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put("track_id", track_id);

        long newRowId = db.insert(TABLE_FAVORITE_VIDEO, null, cValues);
        db.close();
    }

    public void delete_video_favorite(String track_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        String dbfield = "track_id";

        db.execSQL("DELETE FROM " + TABLE_FAVORITE_VIDEO + " WHERE " + dbfield + "='" + track_id + "'");
        db.close();
    }

    public boolean CheckIsVideoFavexits(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_FAVORITE_VIDEO + " where " + dbfield + " = '" + fieldValue + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public List<String> GetFavVideo() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> lockedlist = new ArrayList<>();
        String query = "Select * from " + TABLE_FAVORITE_VIDEO;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {


                lockedlist.add(cursor.getString(cursor.getColumnIndex("track_id")));
                cursor.moveToNext();
            }
        }
        return lockedlist;
    }

    public void insert_video_last_view(Long position_video, String track_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();

        cValues.put("position_video", position_video);
        cValues.put("track_id", track_id);
        long newRowId = db.insert(TABLE_LAST_VIEW, null, cValues);
        db.close();
    }

    public void update_video_last_view(Long position_video, String track_id, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();

        cValues.put("position_video", position_video);
        cValues.put("track_id", track_id);

        String where = "track_id";
        String whereVal = track_id;
        db.execSQL("DELETE FROM " + TABLE_LAST_VIEW + " WHERE " + where + "='" + whereVal + "'");

        long newRowId = db.insert(TABLE_LAST_VIEW, null, cValues);
        db.close();
    }

    public int get_single_video_last_view(String dbfield, String fieldValue) {


        Cursor cursor = null;
        int position = 0;


        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_LAST_VIEW + " where  " + dbfield + "='" + fieldValue + "'";
        cursor = db.rawQuery(Query, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            position = cursor.getInt(cursor.getColumnIndex("position_video"));
            cursor.close();

        }
        cursor.close();

        db.close();
        return position;
    }

    public List<String> GetLastView() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> lockedlist = new ArrayList<>();
        String orderby = "date_added";
        String query = "Select * from " + TABLE_LAST_VIEW + " ORDER BY " + orderby + " DESC ";
        //    String query = "Select * from "+ TABLE_LAST_VIEW ;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                lockedlist.add(cursor.getString(cursor.getColumnIndex("track_id")));

                cursor.moveToNext();
            }
        }
        return lockedlist;
    }

    public void insert_last_play_song(String track_id, int position_track) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put("track_id", track_id);
        cValues.put("position_track", position_track);
        Log.i("insert_play", track_id + " " + position_track);
        long newRowId = db.insert(TABLE_LAST_PLAY, null, cValues);
        db.close();
    }

    public void update_last_play_song(int position_play, String track_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();

        cValues.put("position_track", position_play);
        cValues.put("track_id", track_id);

        String where = "track_id";
        String whereVal = track_id;

        Log.i("update_play", track_id + " " + position_play);
        db.execSQL("DELETE FROM " + TABLE_LAST_PLAY + " WHERE " + where + "='" + whereVal + "'");

        long newRowId = db.insert(TABLE_LAST_PLAY, null, cValues);
        db.close();
    }


    public boolean CheckIsVideoexits(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_ALLVIDEO + " where " + dbfield + " = " + fieldValue;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean CheckIsFolderLocked(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_LOCK_FOLDER + " where " + dbfield + " = '" + fieldValue + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean CheckIsVideoLocked(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_LOCK_VIDEO + " where " + dbfield + " = " + fieldValue;
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean CheckIsVideoView(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_LAST_VIEW + " where " + dbfield + "='" + fieldValue + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public boolean CheckIsTrackPlay(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        String Query = "Select * from " + TABLE_LAST_PLAY + " where " + dbfield + "='" + fieldValue + "'";
        Cursor cursor = db.rawQuery(Query, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public List<String> GetLockedFolder() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> lockedlist = new ArrayList<>();
        String query = "Select * from " + TABLE_LOCK_FOLDER;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {

            while (!cursor.isAfterLast()) {


                lockedlist.add(cursor.getString(cursor.getColumnIndex("track_id")));
                cursor.moveToNext();
            }
        }
        return lockedlist;
    }

    public List<String> GetLockedVideo() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<String> lockedlist = new ArrayList<>();
        String query = "Select * from " + TABLE_LOCK_VIDEO;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {


                lockedlist.add(cursor.getString(cursor.getColumnIndex("track_id")));
                cursor.moveToNext();
            }
        }
        return lockedlist;
    }

    public void RemoveLockedFolder(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCK_FOLDER + " WHERE " + dbfield + "='" + fieldValue + "'");
        db.close();
    }

    public void RemoveLockedVideo(String dbfield, String fieldValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_LOCK_VIDEO + " WHERE " + dbfield + "='" + fieldValue + "'");
        db.close();
    }


    /*****for download manager **/

    public static boolean isDBOpen() {
        return db.isOpen();
    }

    public static int getLastFileID() {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        int i = 0;
        Cursor rawQuery = db.rawQuery("SELECT id FROM Files ORDER BY id DESC LIMIT 1", null);
        if (rawQuery.moveToFirst()) {
            i = rawQuery.getInt(rawQuery.getColumnIndex("id"));
        }
        rawQuery.close();
        return i;
    }

    public static int insertHistory(String str, String str2, String str3, long j) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("INSERT INTO WebHistory (url,page_title,favicon_path,created) VALUES (?,?,?,?)", new String[]{str, str2, str3, String.valueOf(j)});
        int i = -1;
        Cursor rawQuery = db.rawQuery("SELECT id FROM WebHistory ORDER BY id DESC LIMIT 1", null);
        if (rawQuery.moveToFirst()) {
            i = rawQuery.getInt(0);
        }
        rawQuery.close();
        return i;
    }

    public static void setHistoryFaviconPath(int i, String str) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE WebHistory SET favicon_path = ? WHERE id = ?", new String[]{str, String.valueOf(i)});
    }

    public static int insertBookmark(String str, String str2, String str3, long j) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("INSERT INTO WebBookmarks (url,page_title,favicon_path,created) VALUES (?,?,?,?)", new String[]{str, str2, str3, String.valueOf(j)});
        int i = -1;
        Cursor rawQuery = db.rawQuery("SELECT id FROM WebBookmarks ORDER BY id DESC LIMIT 1", null);
        if (rawQuery.moveToFirst()) {
            i = rawQuery.getInt(0);
        }
        rawQuery.close();
        return i;
    }

    public static void setBookmarkFaviconPath(int i, String str) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE WebBookmarks SET favicon_path = ? WHERE id = ?", new String[]{str, String.valueOf(i)});
    }

    public static int insertFile(String str, String str2, String str3, long j, String str4, boolean z, String str5, String str6) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        int typeID = getTypeID(str4);
        SQLiteDatabase sQLiteDatabase = db;
        String str7 = "INSERT INTO Files (url,filename,save_path,status,size,completed,type_id,created,singlethread,username,password) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        String[] strArr = new String[11];
        strArr[0] = str;
        strArr[1] = str2;
        strArr[2] = str3;
        strArr[3] = "0";
        strArr[4] = String.valueOf(j);
        strArr[5] = "0";
        strArr[6] = String.valueOf(typeID);
        strArr[7] = String.valueOf(System.currentTimeMillis());
        strArr[8] = z ? "1" : "0";
        strArr[9] = str5;
        strArr[10] = str6;
        sQLiteDatabase.execSQL(str7, strArr);
        int i = -1;
        Cursor rawQuery = db.rawQuery("SELECT id FROM Files ORDER BY id DESC LIMIT 1", null);
        if (rawQuery.moveToFirst()) {
            i = rawQuery.getInt(rawQuery.getColumnIndex("id"));
        }
        rawQuery.close();
        return i;
    }

    public static void updateFileName(int i, String str) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE Files SET filename=? WHERE id =?", new String[]{str, String.valueOf(i)});
    }

    public static void setFilePause(int i, long j) {
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            db.execSQL("UPDATE Files SET completed=?, status=? WHERE id =?", new String[]{String.valueOf(j), "3", String.valueOf(i)});
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setFilePause error : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }

    public static void setFileRunning(int i) {
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            db.execSQL("UPDATE Files SET status=? WHERE id =?", new String[]{"2", String.valueOf(i)});
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("setFilePause error : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }

    public static void setFileCompleted(int i) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE Files SET completed=size, status=? ,completedtime=? WHERE id =?", new String[]{"4", String.valueOf(System.currentTimeMillis()), String.valueOf(i)});
    }

    public static void deleteFileThreads(int i) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("DELETE FROM Files WHERE id = ?", new String[]{String.valueOf(i)});
        db.execSQL("DELETE FROM Threads WHERE file_id = ?", new String[]{String.valueOf(i)});
    }


    public static ArrayList<SingleOverview> getNotCompletedFiles() {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        ArrayList arrayList = new ArrayList();
        Cursor rawQuery = db.rawQuery("SELECT * FROM Files WHERE status <> ? ", new String[]{"4"});
        if (rawQuery.moveToFirst()) {
            int i = 0;
            do {
                SingleOverview singleOverview = new SingleOverview();
                singleOverview.id = rawQuery.getInt(rawQuery.getColumnIndex("id"));
                singleOverview.originalFileName = rawQuery.getString(rawQuery.getColumnIndex("filename"));
                singleOverview.position = i;
                singleOverview.status = rawQuery.getInt(rawQuery.getColumnIndex("status"));
                long j = (long) rawQuery.getInt(rawQuery.getColumnIndex("completed"));
                singleOverview.singleThread = rawQuery.getInt(rawQuery.getColumnIndex("singlethread")) == 1;
                int i2 = rawQuery.getInt(rawQuery.getColumnIndex("size"));
                if (i2 <= 0) {
                    singleOverview.totalByteSize = -1;
                    singleOverview.totalCompletedPercentage = 0;
                } else {
                    singleOverview.totalByteSize = (long) i2;
                    singleOverview.totalCompletedPercentage = (int) ((100.0f * ((float) j)) / ((float) singleOverview.totalByteSize));
                }
                singleOverview.totalCompleted = j;
                arrayList.add(singleOverview);
                i++;
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        return arrayList;
    }

    public static void insertThreads(int i, int i2, String str, String str2, long j, long j2, long j3) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("INSERT INTO Threads (file_id,part_no,status,part_path,part_name,from_bytes,to_bytes,completed_bytes) VALUES (?,?,?,?,?,?,?,?)", new String[]{String.valueOf(i), String.valueOf(i2), "1", str, str2, String.valueOf(j), String.valueOf(j2), String.valueOf(j3)});
    }

    public static int getCompletedThreadCount(int i) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        int i2 = 0;
        try {
            Cursor rawQuery = db.rawQuery("SELECT COUNT(id) FROM Threads WHERE file_id=? AND status = ?", new String[]{String.valueOf(i), "4"});
            if (rawQuery.moveToFirst()) {
                i2 = rawQuery.getInt(0);
            }
            rawQuery.close();
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in getCompletedThreadCount : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
        return i2;
    }

    public static void setThreadCompleted(int i, int i2) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE Threads SET completed_bytes = to_bytes - from_bytes ,status = ? WHERE file_id = ? AND part_no=?", new String[]{"4", String.valueOf(i), String.valueOf(i2)});
    }

    public static void setThreadPause(int i, int i2, long j) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE Threads SET completed_bytes = ? ,status = ? WHERE file_id = ? AND part_no = ?", new String[]{String.valueOf(j), "3", String.valueOf(i), String.valueOf(i2)});
    }

    public static void setThreadRunning(int i, int i2) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        db.execSQL("UPDATE Threads SET status = ? WHERE file_id = ? AND part_no = ?", new String[]{"2", String.valueOf(i), String.valueOf(i2)});
    }

    public static ArrayList<DownloaderThread> getThreadsByFileid(int i) {
        long j;
        String string;
        boolean z;
        Cursor rawQuery = db.rawQuery("SELECT size,singlethread,username,password FROM Files WHERE id = ? LIMIT 1", new String[]{String.valueOf(i)});
        String str = "";
        String str2 = "";
        if (rawQuery.moveToFirst()) {
            j = rawQuery.getLong(0);
            boolean z2 = rawQuery.getInt(1) == 1;
            String string2 = rawQuery.getString(2);
            string = rawQuery.getString(3);
            String str3 = string2;
            z = z2;
            str = str3;
        } else {
            string = str2;
            z = false;
            j = 0;
        }
        rawQuery.close();
        ArrayList arrayList = new ArrayList();
        Cursor rawQuery2 = db.rawQuery("SELECT * FROM Threads LEFT JOIN Files ON Files.id = Threads.file_id WHERE file_id = ?", new String[]{String.valueOf(i)});
        if (rawQuery2.moveToFirst()) {
            do {
                arrayList.add(new DownloaderThread(i, rawQuery2.getInt(rawQuery2.getColumnIndex("part_no")), rawQuery2.getString(rawQuery2.getColumnIndex("url")), rawQuery2.getString(rawQuery2.getColumnIndex("part_path")), rawQuery2.getString(rawQuery2.getColumnIndex("part_name")), rawQuery2.getLong(rawQuery2.getColumnIndex("from_bytes")), rawQuery2.getLong(rawQuery2.getColumnIndex("to_bytes")), rawQuery2.getLong(rawQuery2.getColumnIndex("completed_bytes")), j, z, str, string));
            } while (rawQuery2.moveToNext());
        }
        rawQuery2.close();
        return arrayList;
    }

    public static ArrayList<DownloaderThread> getAllPausedThreads() {
        Cursor rawQuery = db.rawQuery("SELECT t.file_id,t.part_no,f.url,t.part_path,t.part_name,t.from_bytes,t.to_bytes,t.completed_bytes,f.size,f.singlethread,f.username,f.password FROM Threads t LEFT JOIN Files f ON f.id = t.file_id WHERE t.status = ?", new String[]{"3"});
        ArrayList arrayList = new ArrayList();
        if (rawQuery.moveToFirst()) {
            do {
                arrayList.add(new DownloaderThread(rawQuery.getInt(0), rawQuery.getInt(1), rawQuery.getString(2), rawQuery.getString(3), rawQuery.getString(4), rawQuery.getLong(5), rawQuery.getLong(6), rawQuery.getLong(7), rawQuery.getLong(8), rawQuery.getInt(9) == 1, rawQuery.getString(10), rawQuery.getString(11)));
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        return arrayList;
    }

    public static ArrayList<DownloaderThread> getAllRunningThreads() {
        Cursor rawQuery = db.rawQuery("SELECT t.file_id,t.part_no,f.url,t.part_path,t.part_name,t.from_bytes,t.to_bytes,t.completed_bytes,f.size,f.singlethread,f.username,f.password FROM Threads t LEFT JOIN Files f ON f.id = t.file_id WHERE t.status = ?", new String[]{"2"});
        ArrayList arrayList = new ArrayList();
        if (rawQuery.moveToFirst()) {
            do {
                arrayList.add(new DownloaderThread(rawQuery.getInt(0), rawQuery.getInt(1), rawQuery.getString(2), rawQuery.getString(3), rawQuery.getString(4), rawQuery.getLong(5), rawQuery.getLong(6), rawQuery.getLong(7), rawQuery.getLong(8), rawQuery.getInt(9) == 1, rawQuery.getString(10), rawQuery.getString(11)));
            } while (rawQuery.moveToNext());
        }
        rawQuery.close();
        return arrayList;
    }

    public static ArrayList<DownloaderThread> getCompletedThreadsByFileid(int i) {
        long j;
        String string;
        boolean z;
        Cursor rawQuery = db.rawQuery("SELECT size,singlethread,username,password FROM Files WHERE id = ? LIMIT 1", new String[]{String.valueOf(i)});
        String str = "";
        String str2 = "";
        if (rawQuery.moveToFirst()) {
            j = rawQuery.getLong(0);
            boolean z2 = rawQuery.getInt(1) == 1;
            String string2 = rawQuery.getString(2);
            string = rawQuery.getString(3);
            String str3 = string2;
            z = z2;
            str = str3;
        } else {
            string = str2;
            z = false;
            j = 0;
        }
        rawQuery.close();
        ArrayList arrayList = new ArrayList();
        Cursor rawQuery2 = db.rawQuery("SELECT * FROM Threads LEFT JOIN Files ON Files.id = Threads.file_id WHERE file_id = ? AND Threads.status = ?", new String[]{String.valueOf(i), "4"});
        if (rawQuery2.moveToFirst()) {
            do {
                arrayList.add(new DownloaderThread(i, rawQuery2.getInt(rawQuery2.getColumnIndex("part_no")), rawQuery2.getString(rawQuery2.getColumnIndex("url")), rawQuery2.getString(rawQuery2.getColumnIndex("part_path")), rawQuery2.getString(rawQuery2.getColumnIndex("part_name")), rawQuery2.getLong(rawQuery2.getColumnIndex("from_bytes")), rawQuery2.getLong(rawQuery2.getColumnIndex("to_bytes")), rawQuery2.getLong(rawQuery2.getColumnIndex("completed_bytes")), j, z, str, string));
            } while (rawQuery2.moveToNext());
        }
        rawQuery2.close();
        return arrayList;
    }

    public static int getTypeID(String str) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        int i = -1;
        String str2 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getTypeID for extension : ");
        stringBuilder.append(str);
        Log.i(str2, stringBuilder.toString());
        SQLiteDatabase sQLiteDatabase = db;
        stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT type_id FROM Extensions WHERE extension='");
        stringBuilder.append(str);
        stringBuilder.append("' LIMIT 1");
        Cursor rawQuery = sQLiteDatabase.rawQuery(stringBuilder.toString(), null);
        if (rawQuery.moveToFirst()) {
            i = rawQuery.getInt(rawQuery.getColumnIndex("type_id"));
        }
        rawQuery.close();
        str = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("getTypeID : ");
        stringBuilder2.append(i);
        Log.i(str, stringBuilder2.toString());
        return i;
    }

    public static String getSavePath(String str) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        String str2 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getSavePath for extension : ");
        stringBuilder.append(str);
        Log.i(str2, stringBuilder.toString());
        str2 = App.sdPath;
        SQLiteDatabase sQLiteDatabase = db;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("SELECT t.save_path FROM Types t LEFT JOIN Extensions e ON t.id = e.type_id WHERE e.extension='");
        stringBuilder2.append(str);
        stringBuilder2.append("' LIMIT 1");
        Cursor rawQuery = sQLiteDatabase.rawQuery(stringBuilder2.toString(), null);
        if (rawQuery.moveToFirst()) {
            str2 = rawQuery.getString(rawQuery.getColumnIndex("save_path"));
        }
        rawQuery.close();
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("getSavePath : ");
        stringBuilder.append(str2);
        Log.i(str, stringBuilder.toString());
        return str2;
    }

    public static String getSettingsValue(String str) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        String str2 = "";
        Cursor rawQuery = db.rawQuery("SELECT settings_value FROM Settings WHERE settings_key=? LIMIT 1", new String[]{str});
        if (rawQuery.moveToFirst()) {
            str2 = rawQuery.getString(0);
        }
        rawQuery.close();
        return str2;
    }

    public static void updateSettings(String str, String str2) {
        if (db == null || !db.isOpen()) {
            openDB();
        }
        String str3 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Setting Update : Key - ");
        stringBuilder.append(str);
        stringBuilder.append(" Value - ");
        stringBuilder.append(str2);
        Log.i(str3, stringBuilder.toString());
        db.execSQL("INSERT OR REPLACE INTO Settings (settings_key, settings_value) VALUES (?,?)", new String[]{str, str2});
    }

    public static void updateSingleThreadFileSize(int i, long j) {
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            db.execSQL("UPDATE Files SET size=? WHERE id = ?", new String[]{String.valueOf(j), String.valueOf(i)});
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in getCompletedFiles : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }

    public static void removeFile(int i) {
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            db.execSQL("DELETE FROM Files WHERE id = ?", new String[]{String.valueOf(i)});
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in removeFile : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }

    public static ArrayList<SingleFile> getCompletedFiles() {
        ArrayList arrayList = new ArrayList();
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            Cursor rawQuery = db.rawQuery("SELECT * FROM Files WHERE status = ? ORDER BY completedtime DESC", new String[]{"4"});
            if (rawQuery.moveToFirst()) {
                int columnIndex = rawQuery.getColumnIndex("id");
                int columnIndex2 = rawQuery.getColumnIndex("url");
                int columnIndex3 = rawQuery.getColumnIndex("save_path");
                int columnIndex4 = rawQuery.getColumnIndex("filename");
                int columnIndex5 = rawQuery.getColumnIndex("size");
                int columnIndex6 = rawQuery.getColumnIndex("created");
                do {
                    SingleFile singleFile = new SingleFile();
                    singleFile.id = rawQuery.getInt(columnIndex);
                    singleFile.url = rawQuery.getString(columnIndex2);
                    singleFile.title = rawQuery.getString(columnIndex4);
                    singleFile.path = rawQuery.getString(columnIndex3);
                    singleFile.size = rawQuery.getLong(columnIndex5);
                    singleFile.sizeString = App.humanReadableByteCount(singleFile.size, false);
                    singleFile.created = new Date(rawQuery.getLong(columnIndex6));
                    arrayList.add(singleFile);
                } while (rawQuery.moveToNext());
            }
            rawQuery.close();
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in getCompletedFiles : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
        return arrayList;
    }

    public static String getSavePathForFile(int i) {
        String str = "";
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            Cursor rawQuery = db.rawQuery("SELECT save_path FROM Files WHERE id = ? LIMIT 1", new String[]{String.valueOf(i)});
            if (rawQuery.moveToFirst()) {
                str = rawQuery.getString(rawQuery.getColumnIndex("save_path"));
            }
            rawQuery.close();
        } catch (Exception e) {
            String str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error in getSavePathForFile : ");
            stringBuilder.append(e.getMessage());
            Log.e(str2, stringBuilder.toString());
        }
        return str;
    }

    public static SingleFile getFileDetail(int i) {
        Exception e;
        SingleFile singleFile = null;
        try {
            if (db == null || !db.isOpen()) {
                openDB();
            }
            boolean z = true;
            Cursor rawQuery = db.rawQuery("SELECT * FROM Files WHERE id = ? LIMIT 1", new String[]{String.valueOf(i)});
            if (rawQuery.moveToFirst()) {
                int columnIndex = rawQuery.getColumnIndex("id");
                int columnIndex2 = rawQuery.getColumnIndex("url");
                int columnIndex3 = rawQuery.getColumnIndex("save_path");
                int columnIndex4 = rawQuery.getColumnIndex("filename");
                int columnIndex5 = rawQuery.getColumnIndex("size");
                int columnIndex6 = rawQuery.getColumnIndex("created");
                int columnIndex7 = rawQuery.getColumnIndex("completedtime");
                int columnIndex8 = rawQuery.getColumnIndex("singlethread");
                int columnIndex9 = rawQuery.getColumnIndex("status");
                SingleFile singleFile2 = new SingleFile();
                try {
                    singleFile2.id = rawQuery.getInt(columnIndex);
                    singleFile2.url = rawQuery.getString(columnIndex2);
                    singleFile2.title = rawQuery.getString(columnIndex4);
                    singleFile2.path = rawQuery.getString(columnIndex3);
                    singleFile2.size = rawQuery.getLong(columnIndex5);
                    singleFile2.sizeString = App.humanReadableByteCount(singleFile2.size, false);
                    singleFile2.created = new Date(rawQuery.getLong(columnIndex6));
                    singleFile2.completed = new Date(rawQuery.getLong(columnIndex7));
                    if (rawQuery.getInt(columnIndex8) != 1) {
                        z = false;
                    }
                    singleFile2.singleThread = z;
                    singleFile2.status = rawQuery.getInt(columnIndex9);
                    singleFile = singleFile2;
                } catch (Exception e2) {
                    e = e2;
                    singleFile = singleFile2;
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Error in getCompletedFiles : ");
                    stringBuilder.append(e.getMessage());
                    Log.e(str, stringBuilder.toString());
                    return singleFile;
                }
            }
            rawQuery.close();
        } catch (Exception e3) {
            e = e3;
        }
        return singleFile;
    }

}
