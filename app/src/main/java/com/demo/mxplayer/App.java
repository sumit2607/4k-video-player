package com.demo.mxplayer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.demo.mxplayer.download.Constants;
import com.demo.mxplayer.download.DownloaderService;
import com.demo.mxplayer.utils.DownloadTracker;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class App extends Application {

    public static Context applicationContext=null;
    public static volatile Handler applicationHandler = null;
    public static Point displaySize = new Point();
    public static float density = 1;
    private static final String DOWNLOAD_ACTION_FILE = "actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final int MAX_SIMULTANEOUS_DOWNLOADS = 2;
    private Cache downloadCache;
    private File downloadDirectory;
    private DownloadManager downloadManager;
    private DownloadTracker downloadTracker;
    protected String userAgent;
    public static String partPath;
    public static String sdPath,historyPath;
    public static String TAG="App";
    boolean downloaderServiceConnected = false;
    DownloaderService mDownloaderService;
    private static AppOpenManager appOpenManager;
    public static NotificationManager mNotifyManager;
    public Handler mHandler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message message) {
            int i = message.what;
            if (i == Constants.Thread_Pause_All) {
                Log.i(App.TAG, "********************* Pause All Files");
                if (App.this.downloaderServiceConnected && App.this.mDownloaderService != null) {
                    App.this.mDownloaderService.mHandler.sendEmptyMessage(Constants.Thread_Pause_All);
                }
            } else if (i == Constants.Thread_Resume_All) {
                Log.i(App.TAG, "********************* Resume All Files ");
                if (App.this.downloaderServiceConnected && App.this.mDownloaderService != null) {
                    App.this.mDownloaderService.mHandler.sendEmptyMessage(Constants.Thread_Resume_All);
                }
            }
            return false;
        }
    });
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DownloaderService.DownloaderServiceBinder downloaderServiceBinder = (DownloaderService.DownloaderServiceBinder) iBinder;
            App.this.mDownloaderService = downloaderServiceBinder.getService();
            App.this.downloaderServiceConnected = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            App.this.downloaderServiceConnected = false;
            App.this.mDownloaderService = null;
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());
        checkDisplaySize();
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        density = App.applicationContext.getResources().getDisplayMetrics().density;
        this.userAgent = Util.getUserAgent(this, getResources().getString(R.string.app_name));
        Intent intent = new Intent(this, DownloaderService.class);
        createNotificationChannels();
        if (Build.VERSION.SDK_INT >= 26) {
            Log.i(TAG, "> Android Oero start foreground service");
          //  startForegroundService(intent);
        } else {
            //startService(intent);
        }
        bindService(intent, this.mServiceConnection,Context.BIND_AUTO_CREATE);
        Firebase.setAndroidContext(getApplicationContext());
        FirebaseApp.initializeApp(this);

//        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        Map<String, Object> remoteConfigDefaults = new HashMap();
//        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_REQUIRED, false);
//        remoteConfigDefaults.put(ForceUpdateChecker.KEY_CURRENT_VERSION, "1.0.0");
//        remoteConfigDefaults.put(ForceUpdateChecker.KEY_UPDATE_URL,
//                "https://play.google.com/store/apps/details?id="+getPackageName());
//
//        firebaseRemoteConfig.setDefaultsAsync(remoteConfigDefaults);
//        firebaseRemoteConfig.fetch(10) // fetch every minutes
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        Log.d(TAG, "remote config is fetched.");
//                        firebaseRemoteConfig.fetchAndActivate();
//                    }
//                });
       appOpenManager = new AppOpenManager(this);

    }
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(Constants.FILE_COMPLETE_NOTIFICATION_CHANNEL_ID, Constants.FILE_COMPLETE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(-16711936);
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100});
            mNotifyManager.createNotificationChannel(notificationChannel);
            notificationChannel = new NotificationChannel(Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID, Constants.FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
            notificationChannel.enableVibration(false);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }
    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append(File.separator);
        stringBuilder.append("MXDownloadManager");
        sdPath = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append(File.separator);
        stringBuilder.append("MXDownloadManager");
        stringBuilder.append(File.separator);
        stringBuilder.append(".parts");
        partPath = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append(File.separator);
        stringBuilder.append("MXDownloadManager");
        stringBuilder.append(File.separator);
        stringBuilder.append(".history");
        historyPath = stringBuilder.toString();

    }
    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory((Context) this, (TransferListener) bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(this.userAgent, bandwidthMeter);
    }

    public boolean useExtensionRenderers() {
        return "withExtensions".equals("extensions");
    }

    public DownloadTracker getDownloadTracker() {
        initDownloadManager();
        return this.downloadTracker;
    }


    public DownloadManager getDownloadManager() {
        initDownloadManager();
        return this.downloadManager;
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static int dp(float value) {
        return (int)Math.ceil(density * value);
    }

    public static void checkDisplaySize() {
        try {
            WindowManager manager = (WindowManager)App.applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    if(Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    public DataSource.Factory buildDataSourceFactory() {
        return buildReadOnlyCacheDataSource(new DefaultDataSourceFactory((Context) this, buildHttpDataSourceFactory()), getDownloadCache());
    }

    private static CacheDataSourceFactory buildReadOnlyCacheDataSource(DefaultDataSourceFactory upstreamFactory, Cache cache) {
        return new CacheDataSourceFactory(cache, upstreamFactory, new FileDataSourceFactory(), null, 2, null);
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory() {
        return new DefaultHttpDataSourceFactory(this.userAgent);
    }

    private synchronized void initDownloadManager() {
        if (this.downloadManager == null) {
            this.downloadManager = new DownloadManager(new DownloaderConstructorHelper(getDownloadCache(), buildHttpDataSourceFactory()), 2, 5, new File(getDownloadDirectory(), DOWNLOAD_ACTION_FILE), new DownloadAction.Deserializer[0]);
            this.downloadTracker = new DownloadTracker(this, buildDataSourceFactory(), new File(getDownloadDirectory(), DOWNLOAD_TRACKER_ACTION_FILE), new DownloadAction.Deserializer[0]);
            this.downloadManager.addListener(this.downloadTracker);
        }
    }

    private synchronized Cache getDownloadCache() {
        if (this.downloadCache == null) {
            this.downloadCache = new SimpleCache(new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY), new NoOpCacheEvictor());
        }
        return this.downloadCache;
    }

    private File getDownloadDirectory() {
        if (this.downloadDirectory == null) {
            this.downloadDirectory = getExternalFilesDir(null);
            if (this.downloadDirectory == null) {
                this.downloadDirectory = getFilesDir();
            }
        }
        return this.downloadDirectory;
    }
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
