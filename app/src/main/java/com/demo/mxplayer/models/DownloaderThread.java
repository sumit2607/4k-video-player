package com.demo.mxplayer.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;

import com.demo.mxplayer.db.DbHandler;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import org.apache.commons.httpclient.auth.HttpAuthenticator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloaderThread extends Thread {
    public String TAG = "DownloaderThread_0_0";
    public long bytesFrom;
    public long bytesTo;
    public boolean completed = false;
    public long completedDownload;
    public int completedPercentage = 0;
    public boolean exists = false;
    public String fileName;
    public long fileSize;
    HttpURLConnection httpconection;
    public int id;
    public Handler mDetailHandler;
    public Handler mOverviewHandler;
    public Handler mServiceHandler;
    public String originalFile;
    public int partNo;
    String password = "";
    public boolean running = true;
    public boolean showDetailProgress = false;
    public boolean showOverviewProgress = false;
    public boolean singleThread = false;
    public long totalDownload;
    public String url;
    String userName = "";

    public DownloaderThread(int i, int i2, String str, String str2, String str3, long j, long j2, long j3, long j4, boolean z, String str4, String str5) {
        int i3 = i;
        int i4 = i2;
        String str6 = str2;
        long j5 = j4;
        boolean z2 = z;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DownloaderThread_");
        stringBuilder.append(i3);
        stringBuilder.append("_");
        stringBuilder.append(i4);
        this.TAG = stringBuilder.toString();
        this.id = i3;
        this.partNo = i4;
        this.url = str;
        this.fileName = str6;
        this.bytesFrom = j;
        this.bytesTo = j2;
        this.totalDownload = this.bytesTo - this.bytesFrom;
        this.completedDownload = j3;
        this.originalFile = str3;
        this.singleThread = z2;
        this.userName = str4;
        this.password = str5;
        String str7 = this.TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Part File : ");
        stringBuilder2.append(this.fileName);
        Log.i(str7, stringBuilder2.toString());
        File file = new File(str6);
        this.fileSize = j5;
        if (z2 && this.fileSize > 0) {
            this.totalDownload = this.fileSize;
        }
        if (file.exists()) {
            long j6 = j5 / 3;
            j5 = file.length();
            str7 = this.TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Base : ");
            stringBuilder.append(((long) (this.partNo - 1)) * j6);
            Log.i(str7, stringBuilder.toString());
            str7 = this.TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Start From File : ");
            stringBuilder2.append(j5);
            Log.i(str7, stringBuilder2.toString());
            this.completedDownload = j5;
            this.bytesFrom += this.completedDownload;
            this.exists = true;
            this.completedPercentage = (int) ((100.0f * ((float) this.completedDownload)) / ((float) this.totalDownload));
        }
        if (!z2 && this.bytesTo <= this.bytesFrom) {
            this.completed = true;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:80:0x0002 A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x01be A:{Catch:{ IOException -> 0x021e, Exception -> 0x01ca }} */
    public void run() {
        DownloaderThread downloaderThread = this;
        while (downloaderThread.running && !downloaderThread.completed) {
            try {
                FileOutputStream fileOutputStream;
                if (downloaderThread.exists) {
                    fileOutputStream = new FileOutputStream(new File(downloaderThread.fileName), true);
                } else {
                    fileOutputStream = new FileOutputStream(new File(downloaderThread.fileName));
                }
                downloaderThread.httpconection = (HttpURLConnection) new URL(downloaderThread.url).openConnection();
                if (!downloaderThread.singleThread) {
                    String str = downloaderThread.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Part No : ");
                    stringBuilder.append(downloaderThread.partNo);
                    stringBuilder.append(" : Bytes From  : ");
                    stringBuilder.append(downloaderThread.bytesFrom);
                    stringBuilder.append(" : Bytes To : ");
                    stringBuilder.append(downloaderThread.bytesTo);
                    Log.i(str, stringBuilder.toString());
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("bytes=");
                    stringBuilder2.append(downloaderThread.bytesFrom);
                    stringBuilder2.append("-");
                    stringBuilder2.append(downloaderThread.bytesTo);
                    downloaderThread.httpconection.setRequestProperty("Range", stringBuilder2.toString());
                }
                downloaderThread.httpconection.setRequestProperty("connection", "close");
                if (downloaderThread.httpconection.getResponseCode() == 401) {
                    URL url = new URL(downloaderThread.url);
                    downloaderThread.httpconection.disconnect();
                    StringBuilder  stringBuilder = new StringBuilder();
                    stringBuilder.append("Basic ");
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(downloaderThread.userName);
                    stringBuilder3.append(":");
                    stringBuilder3.append(downloaderThread.password);
                    stringBuilder.append(new String(Base64.encode(stringBuilder3.toString().getBytes(), 3)));
                    String toString = stringBuilder.toString();
                    downloaderThread.httpconection = (HttpURLConnection) url.openConnection();
                    downloaderThread.httpconection.setRequestProperty(HttpAuthenticator.WWW_AUTH_RESP, toString);
                }
                InputStream bufferedInputStream = new BufferedInputStream(downloaderThread.httpconection.getInputStream());
                byte[] bArr = new byte[4096];
                while (downloaderThread.running) {
                    int read = bufferedInputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream.write(bArr, 0, read);
                        long j = (long) read;
                        downloaderThread.completedDownload += j;
                        downloaderThread.completedPercentage = (int) ((100.0f * ((float) downloaderThread.completedDownload)) / ((float) downloaderThread.totalDownload));
                        if (downloaderThread.showDetailProgress && downloaderThread.mDetailHandler != null) {
                            Message message = new Message();
                            switch (downloaderThread.partNo) {
                                case DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED:
                                    message.what = 501;
                                    break;
                                case 3:
                                    message.what = 502;
                                    break;
                                case 2:
                                    message.what = 503;
                                    break;
                                default:
                                    break;
                            }
                            Bundle bundle = new Bundle();
                            bundle.putLong("downloaded", j);
                            if (downloaderThread.fileSize <= 0) {
                                bundle.putInt("percentage", 0);
                            } else {
                                bundle.putInt("percentage", downloaderThread.completedPercentage);
                            }
                            message.setData(bundle);
                            downloaderThread.mDetailHandler.sendMessage(message);
                        }
                        if (downloaderThread.showOverviewProgress && downloaderThread.mOverviewHandler != null) {
                            Message message2 = new Message();
                            switch (downloaderThread.partNo) {
                                case DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED:
                                    message2.what = 501;
                                    break;
                                case 3:
                                    message2.what = 502;
                                    break;
                                case 2:
                                    message2.what = 503;
                                    break;
                                default:
                                    break;
                            }
                            Bundle bundle2 = new Bundle();
                            bundle2.putLong("downloaded", j);
                            if (downloaderThread.fileSize <= 0) {
                                bundle2.putInt("percentage", 0);
                            } else {
                                bundle2.putInt("percentage", downloaderThread.completedPercentage);
                            }
                            message2.setData(bundle2);
                            downloaderThread.mOverviewHandler.sendMessage(message2);
                        }
                    } else {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        bufferedInputStream.close();
                        if (downloaderThread.running) {
                            downloaderThread.completed = true;
                            Log.i(downloaderThread.TAG, "=========== Thread Completed =============");
                        }
                    }
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                bufferedInputStream.close();
                if (downloaderThread.running) {
                    downloaderThread.completed = true;
                    Log.i(downloaderThread.TAG, "=========== Thread Completed =============");
                }
            } catch (IOException e) {
                IOException iOException = e;
                String str2 = downloaderThread.TAG;
                StringBuilder stringBuilder4 = new StringBuilder();
                stringBuilder4.append("IOException : ");
                stringBuilder4.append(iOException.getMessage());
                Log.e(str2, stringBuilder4.toString());
            } catch (Exception e2) {
                Exception exception = e2;
                downloaderThread.running = false;
                try {
                    DbHandler.openDB();
                    DbHandler.setThreadPause(downloaderThread.id, downloaderThread.partNo, downloaderThread.totalDownload);
                } catch (Exception e22) {
                    Exception exception2 = e22;
                    try {
                        String str3 = downloaderThread.TAG;
                        StringBuilder stringBuilder5 = new StringBuilder();
                        stringBuilder5.append("db handle error : ");
                        stringBuilder5.append(exception2.getMessage());
                        Log.e(str3, stringBuilder5.toString());
                    } catch (Throwable th) {
                        Throwable th2 = th;
                    }
                }
                DbHandler.closeDB();
                String str4 = downloaderThread.TAG;
                StringBuilder  stringBuilder4 = new StringBuilder();
                stringBuilder4.append("Error in run : ");
                stringBuilder4.append(exception.getMessage());
                Log.e(str4, stringBuilder4.toString());
            }
        }
        if (downloaderThread.completed) {
            Message message3 = new Message();
            message3.what = 1050;
            message3.arg1 = downloaderThread.id;
            message3.arg2 = downloaderThread.partNo;
            message3.obj = downloaderThread.originalFile;
            Log.i(downloaderThread.TAG, "=========== Thread Completed Message Send=============");
            downloaderThread.mServiceHandler.sendMessage(message3);
            return;
        }

        StringBuilder stringBuilder6 = new StringBuilder();
        stringBuilder6.append("Pause At : ");
        stringBuilder6.append(downloaderThread.bytesFrom + downloaderThread.completedDownload);
        Log.i(TAG, stringBuilder6.toString());
    }
}
