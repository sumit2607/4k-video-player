package com.demo.mxplayer.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.demo.mxplayer.R;
import com.demo.mxplayer.models.Song;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class MyUtils {
    public static String pref_key = "mxplayer_2019_uv";
    public static boolean is_zoom = false;
    public static List<Song> shuffledPlaylist = null;
    public static void settingTheme(Context context, int theme) {

        switch (theme) {
            case 1:
                context.setTheme(R.style.AppTheme);
                break;
            case 2:
                context.setTheme(R.style.AppTheme2);
                break;
            case 3:
                context.setTheme(R.style.AppTheme3);
                break;
            case 4:
                context.setTheme(R.style.AppTheme4);
                break;
            case 5:
                context.setTheme(R.style.AppTheme5);
                break;
            case 6:
                context.setTheme(R.style.AppTheme6);
                break;
            case 7:
                context.setTheme(R.style.AppTheme7);
                break;
            case 8:
                context.setTheme(R.style.AppTheme8);
                break;
            case 9:
                context.setTheme(R.style.AppTheme9);
                break;
            case 10:
                context.setTheme(R.style.AppTheme10);
                break;
            case 111:
                context.setTheme(R.style.NightMode);
                break;
            default:
                context.setTheme(R.style.AppTheme);
                break;
        }
    }
    public static void changeColorSet(Context context, ImageView img, boolean isSelected) {
        try {
            if (!isSelected) {
                img.setColorFilter(Color.WHITE);
                return;
            }

            final TypedValue typedValue = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorAccent, typedValue, true);
            final int color = typedValue.data;
            img.setColorFilter(color);
            if (Build.VERSION.SDK_INT > 15) {
                img.setImageAlpha(255);
            } else {
                img.setAlpha(255);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SuppressLint("DefaultLocale")
    public static String milisecondToHour(Long millis) {
        long totalSecs = millis/1000;
        long hours = (totalSecs / 3600);
        long mins = (totalSecs / 60) % 60;
        long secs = totalSecs % 60;
        String minsString = (mins == 0)
                ? "00"
                : ((mins < 10)
                ? "0" + mins
                : "" + mins);
        String secsString = (secs == 0)
                ? "00"
                : ((secs < 10)
                ? "0" + secs
                : "" + secs);
        if (hours > 0)
            return hours + ":" + minsString + ":" + secsString;
        else if (mins > 0)
            return "00:"+mins+ ":" + secsString;
        else return "00:"+"00"+":" + secsString;

    }


    public static boolean renamefolder(String path,String oldfolder,String newfolder_name,Context context) {
        boolean bool = false;
        try {
            File file = new File(path);
            File oldFolder = new File(file.getParent(),oldfolder);
        File newFolder = new File(file.getParent(),newfolder_name);
         bool = oldFolder.renameTo(newFolder);
            Log.i("Directory is", file.getParent().toString());
            Log.i("Default path is", path);
            Log.i("From path is", oldFolder.toString());
            Log.i("To path is", newFolder.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        //something that you want to do
                    }
                });
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.parse("file://" + path)));
            }
        } catch(Exception e) {
            // if any error occurs
            e.printStackTrace();
        }

        return  bool;
    }





   public static boolean renamevideo(String path,String oldname,String newname,Context context) {
       boolean bool = false;
        try {
            String extension = path.substring(path.lastIndexOf("."));
            File file = new File(path);
            File from = new File(file.getParent(), oldname+extension);
            String parent = file.getParent();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(newname);
            stringBuilder.append(extension);
            File to = new File(parent, stringBuilder.toString());
            bool =   from.renameTo(to);
            Log.i("Directory is", file.getParent().toString());
            Log.i("Default path is", path);
            Log.i("From path is", from.toString());
            Log.i("To path is", to.toString());
            MediaScannerConnection.scanFile(context, new String[]{to.toString()}, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
       return  bool;
    }
    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static boolean deleteFolder(String foldername,Context context) {

        try {
            return context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,"bucket_display_name = ?", new String[] { foldername })  > 0;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteVideo(String id,Context context) {

        try {
            return context.getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,"_id = ?", new String[] { id })  > 0;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
    public static int getStatusHeight(Activity activity)
    {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = activity.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
            Rect frame = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            statusBarHeight = frame.top;
        }
        return statusBarHeight;
    }
    public static int getScreenWidth(Activity context)
    {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        return  mScreenWidth;
    }
    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }
    public static int getScreenHeight(Activity context)
    {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int mScreenHeight = dm.heightPixels;
        return  mScreenHeight;
    }
    public static String formatFileSize(double size) {

        if (size < 1024) {
            return (double) Math.round(size * 100d) / 100d + "B";
        } else {
            size = size / 1024d;
        }

        if (size < 1024) {
            return (double) Math.round(size * 100d) / 100d + "KB";

        } else {
            size = size / 1024d;
        }
        if (size < 1024) {
            return (double) Math.round(size * 100d) / 100d + "MB";

        } else {

            size = size / 1024d;
            return (double) Math.round(size * 100d) / 100d + "GB";
        }
    }
    public static String getParentDirPath(String fileOrDirPath) {
        boolean endsWithSlash = fileOrDirPath.endsWith(File.separator);
        return fileOrDirPath.substring(0, fileOrDirPath.lastIndexOf(File.separatorChar,
                endsWithSlash ? fileOrDirPath.length() - 2 : fileOrDirPath.length() - 1));
    }
    public static String humanReadableByteCount(long j, boolean z2) {
        int i = z2 ? 1000 : 1024;
        StringBuilder  z1;
        if (j < ((long) i)) {
            z1 = new StringBuilder();
            z1.append(j);
            z1.append(" B");
            return z1.toString();
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append((z2 ? "kMGTPE" : "KMGTPE").charAt(((int) (Math.log((double) j) / Math.log((double) i))) - 1));
        stringBuilder.append("");
       String z = stringBuilder.toString();
        return String.format(Locale.US, "%.1f %sB", new Object[]{Double.valueOf(((double) j) / Math.pow((double) i, (double) ((int) (Math.log((double) j) / Math.log((double) i))))), z});
    }
    public static String humanReadableDate(long j) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyy hh:mm:ss", Locale.US);
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(j);
        return simpleDateFormat.format(instance.getTime());
    }
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
    public static String parseTimeFromMilliseconds(String str1) {
        if (str1 == null) {
            return "";
        }
        long floor = (long) Math.floor((double) (Long.parseLong(str1.trim()) / 1000));
        if (floor <= 59) {
            StringBuilder    str = new StringBuilder();
            str.append(prependZero((int) floor));
            str.append("s");
            return str.toString();
        }
        long floor2 = (long) Math.floor((double) (floor / 60));
        if (floor2 <= 59) {
            StringBuilder   str = new StringBuilder();
            str.append(prependZero((int) floor2));
            str.append(":");
            str.append(prependZero((int) (floor % 60)));
            return str.toString();
        }
        StringBuilder    str = new StringBuilder();
        str.append(prependZero((int) ((long) Math.floor((double) (floor2 / 60)))));
        str.append(":");
        str.append(prependZero((int) (floor2 % 60)));
        str.append(":");
        str.append(prependZero((int) (floor % 60)));
        return str.toString();
    }
    private static String prependZero(int i) {
        StringBuilder stringBuilder;
        if (i < 10) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("0");
            stringBuilder.append(i);
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append(i);
            stringBuilder.append("");
        }
        return stringBuilder.toString();
    }
    public static SharedPreferences getPreferanse(Context context) {
        return context.getSharedPreferences(pref_key, Activity.MODE_PRIVATE);
    }
    public static void setRepeat(Context context, int isOn) {
        SharedPreferences.Editor editor = getPreferanse(context).edit();
        editor.putInt("repeat", isOn);
        editor.commit();
    }

    public static int getRepeat(Context context) {
        SharedPreferences mSharedPreferences = getPreferanse(context);
        return mSharedPreferences.getInt("repeat", 0);
    }


    public static void setShuffel(Context context, boolean isOn) {
        SharedPreferences.Editor editor = getPreferanse(context).edit();
        editor.putBoolean("shuffel", isOn);
        editor.commit();
    }

    public static boolean getShuffel(Context context) {
        SharedPreferences mSharedPreferences = getPreferanse(context);
        return mSharedPreferences.getBoolean("shuffel", false);
    }
    public static String getVideoListNative(Context context) {
        SharedPreferences mSharedPreferences = getPreferanse(context);
        return mSharedPreferences.getString("VideoListNative", "");
    }

    public static void  setVideoListNative(Context context, String isOn) {
        SharedPreferences.Editor editor = getPreferanse(context).edit();
        editor.putString("VideoListNative", isOn);
        editor.commit();
    }

    public static String getFolderListNative(Context context) {
        SharedPreferences mSharedPreferences = getPreferanse(context);
        return mSharedPreferences.getString("FolderListNative", "");
    }

    public static void  setFolderListNative(Context context, String isOn) {
        SharedPreferences.Editor editor = getPreferanse(context).edit();
        editor.putString("FolderListNative", isOn);
        editor.commit();
    }

}
