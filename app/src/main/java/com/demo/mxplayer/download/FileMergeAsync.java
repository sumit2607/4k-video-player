package com.demo.mxplayer.download;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;

import androidx.core.provider.FontsContractCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.db.DbHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileMergeAsync extends AsyncTask<Void, Void, Integer> {
    public static final String TAG = "FileMergeAsync";
    Context context;
    int fileID = -1;
    String fileTitle = "";
    Handler mHandler;
    boolean singleThread = false;

    public FileMergeAsync(Context context, Handler handler, int i, String str, boolean z) {
        this.context = context;
        this.fileID = i;
        this.fileTitle = str;
        this.singleThread = z;
        this.mHandler = handler;
    }

    /* Access modifiers changed, original: protected */
    public void onPreExecute() {
        super.onPreExecute();
    }

    /* Access modifiers changed, original: protected|varargs */
    public Integer doInBackground(Void... voidArr) {
        String savePathForFile;
        try {
            StringBuilder stringBuilder;
            new File(Constants.sdPath).mkdirs();
            String str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("title : ");
            stringBuilder2.append(this.fileTitle);
            Log.i(str, stringBuilder2.toString());
            str = this.fileTitle.substring(this.fileTitle.lastIndexOf("."));
            DbHandler.openDB();
            savePathForFile = DbHandler.getSavePathForFile(this.fileID);
            if (savePathForFile.length() <= 0) {
                savePathForFile = DbHandler.getSavePath(str);
            }
            new File(savePathForFile).mkdirs();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append(savePathForFile);
            stringBuilder3.append(File.separator);
            stringBuilder3.append(this.fileTitle);
            File file = new File(stringBuilder3.toString());
            String str2 = this.fileTitle;
            if (file.exists()) {
                String substring = this.fileTitle.substring(0, this.fileTitle.lastIndexOf(46));
                for (int i = 1; i <= 1000; i++) {
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append(savePathForFile);
                    stringBuilder4.append(File.separator);
                    stringBuilder4.append(substring);
                    stringBuilder4.append("-");
                    stringBuilder4.append(i);
                    stringBuilder4.append(str);
                    if (!new File(stringBuilder4.toString()).exists()) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(substring);
                        stringBuilder.append("-");
                        stringBuilder.append(i);
                        substring = stringBuilder.toString();
                        break;
                    }
                }
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append(substring);
                stringBuilder3.append(str);
                str2 = stringBuilder3.toString();
                StringBuilder stringBuilder5 = new StringBuilder();
                stringBuilder5.append(savePathForFile);
                stringBuilder5.append(File.separator);
                stringBuilder5.append(str2);
                file = new File(stringBuilder5.toString());
                DbHandler.updateFileName(this.fileID, str2);
            }
            DbHandler.closeDB();
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            File file2;
            FileInputStream fileInputStream;
            byte[] bArr;
            int read;
            if (this.singleThread) {
                savePathForFile = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Single Thread Completed : id : ");
                stringBuilder.append(this.fileID);
                Log.i(savePathForFile, stringBuilder.toString());
                stringBuilder = new StringBuilder();
                stringBuilder.append(Constants.partPath);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileID);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileTitle);
                file2 = new File(stringBuilder.toString());
                fileInputStream = new FileInputStream(file2);
                bArr = new byte[2048];
                while (true) {
                    read = fileInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileInputStream.close();
                long length = file2.length();
                file2.delete();
                fileOutputStream.close();
                fileOutputStream.flush();
                DbHandler.openDB();
                DbHandler.updateSingleThreadFileSize(this.fileID, length);
                DbHandler.closeDB();
            } else {
                stringBuilder = new StringBuilder();
                stringBuilder.append(Constants.partPath);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileID);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileTitle);
                stringBuilder.append(".1_part");
                file2 = new File(stringBuilder.toString());
                fileInputStream = new FileInputStream(file2);
                bArr = new byte[2048];
                while (true) {
                    read = fileInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileInputStream.close();
                file2.delete();
                stringBuilder = new StringBuilder();
                stringBuilder.append(Constants.partPath);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileID);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileTitle);
                stringBuilder.append(".2_part");
                file2 = new File(stringBuilder.toString());
                fileInputStream = new FileInputStream(file2);
                while (true) {
                    read = fileInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileInputStream.close();
                file2.delete();
                stringBuilder = new StringBuilder();
                stringBuilder.append(Constants.partPath);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileID);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileTitle);
                stringBuilder.append(".3_part");
                file2 = new File(stringBuilder.toString());
                fileInputStream = new FileInputStream(file2);
                while (true) {
                    read = fileInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileInputStream.close();
                file2.delete();
                fileOutputStream.close();
                fileOutputStream.flush();
            }
            str = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("File Download Complete : ");
            stringBuilder2.append(str2);
            Log.i(str, stringBuilder2.toString());
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(Constants.partPath);
            stringBuilder2.append(File.separator);
            stringBuilder2.append(this.fileID);
            File file3 = new File(stringBuilder2.toString());
            if (file3.exists()) {
                file3.delete();
            }
        } catch (Exception e) {
            savePathForFile = TAG;
            StringBuilder stringBuilder6 = new StringBuilder();
            stringBuilder6.append("Error : ");
            stringBuilder6.append(e.getMessage());
            Log.e(savePathForFile, stringBuilder6.toString());
        }
        return Integer.valueOf(-1);
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(Integer num) {
        Builder builder;
        Notification build;
        super.onPostExecute(num);
        Message message = new Message();
        message.what = Constants.Thread_Remove_Completed;
        message.arg1 = this.fileID;
        if (this.singleThread) {
            message.arg2 = 11;
        } else {
            message.arg2 = 10;
        }
        this.mHandler.sendMessage(message);
        Intent intent = new Intent(Constants.BROADCAST_FILE_DOWNLOAD_COMPLETE);
        intent.putExtra(FontsContractCompat.Columns.FILE_ID, this.fileID);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
        intent = new Intent(this.context, MainActivity.class);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("completed noti file id : ");
        stringBuilder.append(this.fileID);
        Log.i(str, stringBuilder.toString());
        intent.putExtra(FontsContractCompat.Columns.FILE_ID, this.fileID);
        intent.putExtra("from_notification", true);
        PendingIntent activity = PendingIntent.getActivity(this.context, this.fileID, intent, 134217728);
        NotificationManager notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        Builder contentTitle;
        StringBuilder stringBuilder2;
        if (VERSION.SDK_INT >= 26) {
            builder = new Builder(this.context, Constants.FILE_COMPLETE_NOTIFICATION_CHANNEL_ID);
            contentTitle = builder.setContentTitle(this.context.getString(R.string.notification_completed_title));
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.fileTitle);
            stringBuilder2.append(" ");
            stringBuilder2.append(this.context.getString(R.string.notification_file_completed_suffix));
            contentTitle.setContentText(stringBuilder2.toString()).setContentIntent(activity).setOngoing(false).setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(this.context.getResources(), R.mipmap.ic_launcher)).setAutoCancel(true).setOnlyAlertOnce(true);
        } else {
            builder = new Builder(this.context);
            contentTitle = builder.setContentTitle(this.context.getString(R.string.notification_completed_title));
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.fileTitle);
            stringBuilder2.append(" ");
            stringBuilder2.append(this.context.getString(R.string.notification_file_completed_suffix));
            contentTitle.setContentText(stringBuilder2.toString()).setOngoing(false).setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(this.context.getResources(), R.mipmap.ic_launcher)).setContentIntent(activity).setAutoCancel(true).setSound(System.DEFAULT_NOTIFICATION_URI).setVibrate(new long[]{100}).setOnlyAlertOnce(true);
        }
        if (VERSION.SDK_INT >= 16) {
            build = builder.build();
        } else {
            build = builder.getNotification();
        }
        notificationManager.notify(this.fileID, build);
    }
}
