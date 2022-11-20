package com.demo.mxplayer.download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.provider.Settings.System;

import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import com.demo.mxplayer.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestFileDownloader extends AsyncTask<String, String, String> {
    public static final String TAG = "TestFileDownloader";
    PendingIntent contentIntent;
    Context context;
    String downloadURL;
    int fileID = -1;
    int lastProgress = 0;
    Builder mBuilder;
    NotificationManager mNotifyManager;
    String saveFileName;
    String savePath;

    public TestFileDownloader(Context context, int i, String str, String str2, String str3) {
        this.context = context.getApplicationContext();
        this.fileID = i;
        this.downloadURL = str;
        this.savePath = str2;
        this.saveFileName = str3;
        this.mNotifyManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void downloadNotification() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setType("audio/*");
        this.contentIntent = PendingIntent.getActivity(this.context, 0, intent, 0);
        this.mNotifyManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder;
        StringBuilder stringBuilder;
        if (VERSION.SDK_INT >= 26) {
            this.mBuilder = new Builder(this.context, Constants.FILE_COMPLETE_NOTIFICATION_CHANNEL_ID);
            builder = this.mBuilder;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Downloading File : ");
            stringBuilder.append(this.saveFileName);
            builder.setContentTitle(stringBuilder.toString()).setContentText("0% complete").setProgress(0, 100, false).setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setOnlyAlertOnce(true);
        } else {
            this.mBuilder = new Builder(this.context);
            builder = this.mBuilder;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Downloading File : ");
            stringBuilder.append(this.saveFileName);
            builder.setContentTitle(stringBuilder.toString()).setContentText("0% complete").setProgress(0, 100, false).setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setPriority(0).setAutoCancel(false).setSound(System.DEFAULT_NOTIFICATION_URI).setVibrate(new long[]{100}).setOnlyAlertOnce(true);
        }
        this.mNotifyManager.notify(this.fileID, this.mBuilder.build());
    }

    /* Access modifiers changed, original: protected */
    public void onPreExecute() {
        super.onPreExecute();
        downloadNotification();
    }

    /* Access modifiers changed, original: protected|varargs */
    public String doInBackground(String... strArr) {
        String str;
        StringBuilder stringBuilder;
        try {
            boolean z;
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(this.downloadURL).openConnection();
            httpURLConnection.setRequestProperty("connection", "close");
            int contentLength = httpURLConnection.getContentLength();
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("File Length : ");
            stringBuilder2.append(contentLength);
            Log.i(str2, stringBuilder2.toString());
            File file = new File(this.savePath);
            int i = 1;
            if (file.exists()) {
                z = true;
            } else {
                z = file.mkdirs();
            }
            if (z) {
                file = new File(this.savePath, this.saveFileName);
                file.createNewFile();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream());
                byte[] bArr = new byte[1024];
                long j = 0;
                while (true) {
                    int read = bufferedInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    long j2 = j + ((long) read);
                    String[] strArr2 = new String[i];
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("");
                    String[] strArr3 = strArr2;
                    stringBuilder3.append((int) ((100 * j2) / ((long) contentLength)));
                    strArr3[0] = stringBuilder3.toString();
                    publishProgress(strArr3);
                    fileOutputStream.write(bArr, 0, read);
                    j = j2;
                    i = 1;
                }
                fileOutputStream.close();
                bufferedInputStream.close();
                publishProgress(new String[]{"100"});
            } else {
                String str3 = TAG;
                StringBuilder stringBuilder4 = new StringBuilder();
                stringBuilder4.append("Error in creating file : ");
                stringBuilder4.append(this.savePath);
                stringBuilder4.append(File.separator);
                stringBuilder4.append(this.saveFileName);
                Log.e(str3, stringBuilder4.toString());
            }
        } catch (IllegalArgumentException e) {
            IllegalArgumentException illegalArgumentException = e;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("IllegalArgumentException : ");
            stringBuilder.append(illegalArgumentException.getMessage());
            Log.e(str, stringBuilder.toString());
            illegalArgumentException.printStackTrace();
        } catch (IllegalStateException e2) {
            IllegalStateException illegalStateException = e2;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("IllegalStateException : ");
            stringBuilder.append(illegalStateException.getMessage());
            Log.e(str, stringBuilder.toString());
            illegalStateException.printStackTrace();
        } catch (IOException e3) {
            IOException iOException = e3;
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("IOException : ");
            stringBuilder.append(iOException.getMessage());
            Log.e(str, stringBuilder.toString());
            iOException.printStackTrace();
        }
        return null;
    }

    public void onProgressUpdate(String... strArr) {
        int parseInt = Integer.parseInt(strArr[0]);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("onProgressUpdate : ");
        stringBuilder.append(parseInt);
        Log.i(str, stringBuilder.toString());
        if (parseInt > this.lastProgress) {
            Builder builder = this.mBuilder;
            stringBuilder = new StringBuilder();
            stringBuilder.append(parseInt);
            stringBuilder.append("% complete");
            builder = builder.setContentInfo(stringBuilder.toString());
            stringBuilder = new StringBuilder();
            stringBuilder.append(parseInt);
            stringBuilder.append("% complete");
            builder.setContentText(stringBuilder.toString()).setProgress(100, parseInt, false);
            this.mNotifyManager.notify(this.fileID, this.mBuilder.build());
            this.lastProgress = parseInt;
        }
        super.onProgressUpdate(strArr);
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(String str) {
        super.onPostExecute(str);
        Log.i(TAG, "onPostExecute");
        Builder progress = this.mBuilder.setProgress(100, 100, false);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("File Download Completed : ");
        stringBuilder.append(this.saveFileName);
        progress.setContentTitle(stringBuilder.toString()).setContentInfo("100% Completed").setContentText("100% Completed").setAutoCancel(true).setOngoing(false);
        this.mNotifyManager.notify(this.fileID, this.mBuilder.build());
    }
}
