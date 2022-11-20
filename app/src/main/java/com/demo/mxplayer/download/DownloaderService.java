package com.demo.mxplayer.download;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.provider.FontsContractCompat;

import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.DownloaderThread;
import com.demo.mxplayer.models.SingleFile;

import java.util.ArrayList;
import java.util.HashMap;

public class DownloaderService extends Service {
    public static final String TAG = "DownloaderService";
    public static String partPath;
    public static String sdPath;
    public ArrayList<DownloaderThread> activeThreads = new ArrayList();
    private final IBinder downloaderServiceBinder = new DownloaderServiceBinder();
    public Handler mHandler = new Handler(new Callback() {
        public boolean handleMessage(Message message) {
            int i = message.what;
            int i2 = 1;
            int i3;
            String str;
            StringBuilder stringBuilder;
            String str2;
            StringBuilder stringBuilder2;
            ArrayList threadsByFileid;
            if (i == Constants.Thread_Download_Complete) {
                i = message.arg1;
                i3 = message.arg2;
                DbHandler.openDB();
                DbHandler.setThreadCompleted(i, i3);
                i3 = DbHandler.getCompletedThreadCount(i);
                String str3 = DownloaderService.TAG;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Completed Thread Count :");
                stringBuilder3.append(i3);
                Log.i(str3, stringBuilder3.toString());
                SingleFile fileDetail = DbHandler.getFileDetail(i);
                if ((fileDetail == null || !fileDetail.singleThread) && i3 != 3) {
                    i2 = 0;
                } else {
                    str = DownloaderService.TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("File Completed : ");
                    stringBuilder.append(i);
                    Log.i(str, stringBuilder.toString());
                    DbHandler.setFileCompleted(i);
                }
                DbHandler.closeDB();
                if (i2 != 0) {
                    DownloaderService.this.mergeParts(i, message.obj.toString(), fileDetail.singleThread);
                }
            } else if (i == Constants.Thread_Download_Pause) {
                str2 = DownloaderService.TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("********************* Pause File : ");
                stringBuilder2.append(message.arg1);
                Log.i(str2, stringBuilder2.toString());
                int i4 = message.arg1;
                DbHandler.openDB();
                long j = 0;
                for (int size = DownloaderService.this.activeThreads.size() - 1; size >= 0; size--) {
                    if (((DownloaderThread) DownloaderService.this.activeThreads.get(size)).id == i4) {
                        ((DownloaderThread) DownloaderService.this.activeThreads.get(size)).running = false;
                        long j2 = j + ((DownloaderThread) DownloaderService.this.activeThreads.get(size)).completedDownload;
                        DbHandler.setThreadPause(i4, ((DownloaderThread) DownloaderService.this.activeThreads.get(size)).partNo, ((DownloaderThread) DownloaderService.this.activeThreads.get(size)).completedDownload);
                        DownloaderService.this.activeThreads.remove(size);
                        j = j2;
                    }
                }
                DbHandler.setFilePause(i4, j);
                DbHandler.closeDB();
            } else if (i == Constants.Thread_Download_Resume) {
                str2 = DownloaderService.TAG;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("********************* Resume File : ");
                stringBuilder2.append(message.arg1);
                Log.i(str2, stringBuilder2.toString());
                DbHandler.openDB();
                threadsByFileid = DbHandler.getThreadsByFileid(message.arg1);
                str = DownloaderService.TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Paused Threads Size : ");
                stringBuilder.append(threadsByFileid.size());
                Log.i(str, stringBuilder.toString());
                DownloaderService.this.activeThreads.addAll(threadsByFileid);
                for (i3 = 0; i3 < threadsByFileid.size(); i3++) {
                    if (((DownloaderThread) threadsByFileid.get(i3)).id == message.arg1) {
                        ((DownloaderThread) threadsByFileid.get(i3)).showOverviewProgress = true;
                        ((DownloaderThread) threadsByFileid.get(i3)).mOverviewHandler = (Handler) message.obj;
                        ((DownloaderThread) threadsByFileid.get(i3)).mServiceHandler = DownloaderService.this.mHandler;
                        ((DownloaderThread) threadsByFileid.get(i3)).start();
                        DbHandler.setThreadRunning(message.arg1, ((DownloaderThread) threadsByFileid.get(i3)).partNo);
                    }
                }
                DbHandler.setFileRunning(message.arg1);
                DbHandler.closeDB();
            } else if (i != Constants.Thread_Pause_All) {
                switch (i) {
                    case Constants.Thread_Resume_All /*1090*/:
                        Log.i(DownloaderService.TAG, "********************* Resume All Files ");
                        DbHandler.openDB();
                        threadsByFileid = DbHandler.getAllPausedThreads();
                        str = DownloaderService.TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Paused Threads Size : ");
                        stringBuilder.append(threadsByFileid.size());
                        Log.i(str, stringBuilder.toString());
                        DownloaderService.this.activeThreads.addAll(threadsByFileid);
                        for (i3 = 0; i3 < threadsByFileid.size(); i3++) {
                            ((DownloaderThread) threadsByFileid.get(i3)).showOverviewProgress = false;
                            ((DownloaderThread) threadsByFileid.get(i3)).mOverviewHandler = null;
                            ((DownloaderThread) threadsByFileid.get(i3)).showDetailProgress = false;
                            ((DownloaderThread) threadsByFileid.get(i3)).mDetailHandler = null;
                            ((DownloaderThread) threadsByFileid.get(i3)).mServiceHandler = DownloaderService.this.mHandler;
                            ((DownloaderThread) threadsByFileid.get(i3)).start();
                            DbHandler.setThreadRunning(message.arg1, ((DownloaderThread) threadsByFileid.get(i3)).partNo);
                            DbHandler.setFileRunning(((DownloaderThread) threadsByFileid.get(i3)).id);
                        }
                        DbHandler.closeDB();
                        break;
                    case Constants.Thread_Remove_Completed /*1091*/:
                        if (message.arg2 != 10) {
                            if (message.arg2 == 11) {
                                DownloaderService.this.removeDownloadingThread(message.arg1, 1);
                                break;
                            }
                        }
                        DownloaderService.this.removeDownloadingThread(message.arg1, 1);
                        DownloaderService.this.removeDownloadingThread(message.arg1, 2);
                        DownloaderService.this.removeDownloadingThread(message.arg1, 3);
                        break;

                }
            } else {
                Log.i(DownloaderService.TAG, "********************* Pause All Files");
                DbHandler.openDB();
                HashMap hashMap = new HashMap();
                for (i = DownloaderService.this.activeThreads.size() - 1; i >= 0; i--) {
                    i3 = ((DownloaderThread) DownloaderService.this.activeThreads.get(i)).id;
                    ((DownloaderThread) DownloaderService.this.activeThreads.get(i)).running = false;
                    long j3 = ((DownloaderThread) DownloaderService.this.activeThreads.get(i)).completedDownload;
                    DbHandler.setThreadPause(i3, ((DownloaderThread) DownloaderService.this.activeThreads.get(i)).partNo, ((DownloaderThread) DownloaderService.this.activeThreads.get(i)).completedDownload);
                    if (hashMap.containsKey(new Integer(i3))) {
                        hashMap.put(new Integer(i3), Long.valueOf(((Long) hashMap.get(new Integer(i3))).longValue() + j3));
                    } else {
                        hashMap.put(new Integer(i3), new Long(((DownloaderThread) DownloaderService.this.activeThreads.get(i)).completedDownload));
                    }
                    DownloaderService.this.activeThreads.remove(i);
                }
                for ( Object num : hashMap.keySet()) {
                    DbHandler.setFilePause(Integer.valueOf(num.toString()), ((Long) hashMap.get(num)).longValue());
                }
                DbHandler.closeDB();
            }
            return false;
        }
    });

    public class DownloaderServiceBinder extends Binder {
        /* Access modifiers changed, original: 0000 */
        public DownloaderService getService() {
            return DownloaderService.this;
        }
    }

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append("/SmartDownloadManager");
        sdPath = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append("/SmartDownloadManager/.parts");
        partPath = stringBuilder.toString();
    }

    public IBinder onBind(Intent intent) {
        Log.i(TAG, "========== onBind");
        return this.downloaderServiceBinder;
    }

    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "========== onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "========== onDestroy");
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        Log.i(TAG, "========== onStartCommand");
        super.onStartCommand(intent, i, i2);
        if (VERSION.SDK_INT >= 26) {
            Log.i(TAG, ">= Android Oreo create ongoing notification for background service");
            Intent intent2 = new Intent(this, MainActivity.class);
            intent2.putExtra(FontsContractCompat.Columns.FILE_ID, 0);
            intent2.putExtra("from_notification", true);
            PendingIntent activity = PendingIntent.getActivity(this, 0, intent2, 134217728);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID);
            builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.downloader_background_service)).setContentIntent(activity).setOngoing(true).setSmallIcon(R.mipmap.ic_launcher).setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher)).setAutoCancel(false).setOnlyAlertOnce(true);
            startForeground(Constants.BACKGROUND_SERVICE_NOTIFICATION_ID, builder.build());
            Log.i(TAG, "==== startForeground");
        }
        if (intent == null) {
            Log.i(TAG, "======= intent is null");
        } else {
            Log.i(TAG, "======= intent");
            try {
                DbHandler.openDB();
                if (this.activeThreads.size() <= 0) {
                    ArrayList allRunningThreads = DbHandler.getAllRunningThreads();
                    this.activeThreads.addAll(allRunningThreads);
                    for (i = 0; i < allRunningThreads.size(); i++) {
                        ((DownloaderThread) allRunningThreads.get(i)).showOverviewProgress = false;
                        ((DownloaderThread) allRunningThreads.get(i)).mOverviewHandler = null;
                        ((DownloaderThread) allRunningThreads.get(i)).showDetailProgress = false;
                        ((DownloaderThread) allRunningThreads.get(i)).mDetailHandler = null;
                        ((DownloaderThread) allRunningThreads.get(i)).mServiceHandler = this.mHandler;
                        ((DownloaderThread) allRunningThreads.get(i)).start();
                    }
                }
            } catch (Exception e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("onStartCommand Exception : ");
                stringBuilder.append(e.getMessage());
                Log.e(str, stringBuilder.toString());
            } catch (Throwable th) {
                DbHandler.closeDB();
            }
            DbHandler.closeDB();
        }
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("running threads count : ");
        stringBuilder2.append(this.activeThreads.size());
        Log.i(str2, stringBuilder2.toString());
        return 1;
    }

    public void onTaskRemoved(Intent intent) {
        Log.i(TAG, "========== onTaskRemoved");
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("running threads count : ");
        stringBuilder.append(this.activeThreads.size());
        Log.i(str, stringBuilder.toString());
        Intent intent2 = new Intent(getApplicationContext(), getClass());
        intent2.setPackage(getPackageName());
        ((AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, PendingIntent.getService(getApplicationContext(), 1, intent2, 1073741824));
        super.onTaskRemoved(intent);
    }

    public void addNewDownloadingThread(int i, int i2, String str, String str2, String str3, long j, long j2, long j3, long j4, boolean z, String str4, String str5) {

        DownloaderThread downloaderThread2 = new DownloaderThread(i, i2, str, str2, str3, j, j2, j3, j4, z, str4, str5);
        DownloaderThread downloaderThread3 = downloaderThread2;
        downloaderThread3.mServiceHandler = this.mHandler;
        this.activeThreads.add(downloaderThread3);
        downloaderThread3.start();
    }

    public void removeDownloadingThread(DownloaderThread downloaderThread) {
        int size = this.activeThreads.size() - 1;
        while (size >= 0) {
            if (((DownloaderThread) this.activeThreads.get(size)).id == downloaderThread.id && ((DownloaderThread) this.activeThreads.get(size)).partNo == downloaderThread.partNo) {
                this.activeThreads.remove(size);
            }
            size--;
        }
    }

    public void removeDownloadingThread(int i, int i2) {
        int size = this.activeThreads.size() - 1;
        while (size >= 0) {
            if (((DownloaderThread) this.activeThreads.get(size)).id == i && ((DownloaderThread) this.activeThreads.get(size)).partNo == i2) {
                this.activeThreads.remove(size);
            }
            size--;
        }
    }

    public void mergeParts(int i, String str, boolean z) {
        new FileMergeAsync(getApplicationContext(), this.mHandler, i, str, z).execute(new Void[0]);
    }
}
