package com.demo.mxplayer.utils;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.demo.mxplayer.R;
import com.google.android.exoplayer2.offline.ActionFile;
import com.google.android.exoplayer2.offline.DownloadAction;
import com.google.android.exoplayer2.offline.DownloadAction.Deserializer;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadHelper.Callback;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadManager.TaskState;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.ProgressiveDownloadHelper;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.offline.TrackKey;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.offline.DashDownloadHelper;
import com.google.android.exoplayer2.source.hls.offline.HlsDownloadHelper;
import com.google.android.exoplayer2.source.smoothstreaming.offline.SsDownloadHelper;
import com.google.android.exoplayer2.ui.DefaultTrackNameProvider;
import com.google.android.exoplayer2.ui.TrackNameProvider;
import com.google.android.exoplayer2.upstream.DataSource.Factory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

public class DownloadTracker implements DownloadManager.Listener {
    private static final String TAG = "DownloadTracker";
    private final ActionFile actionFile;
    private final Handler actionFileWriteHandler;
    private final Context context;
    private final Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet();
    private final TrackNameProvider trackNameProvider;
    private final HashMap<Uri, DownloadAction> trackedDownloadStates = new HashMap();

    public interface Listener {
        void onDownloadsChanged();
    }

    private final class StartDownloadDialogHelper implements Callback, OnClickListener {
         Builder builder=null;
        private final View dialogView = LayoutInflater.from(this.builder.getContext()).inflate(R.layout.start_download_dialog, null);
        private final DownloadHelper downloadHelper;
        private final String name;
        private final ListView representationList = ((ListView) this.dialogView.findViewById(R.id.representation_list));
        private final List<TrackKey> trackKeys = new ArrayList();
        private final ArrayAdapter<String> trackTitles = new ArrayAdapter(this.builder.getContext(), 17367056);

        public StartDownloadDialogHelper(Activity activity, DownloadHelper downloadHelper, String name) {
            this.downloadHelper = downloadHelper;
            this.name = name;
            this.builder = new Builder(activity).setTitle(R.string.exo_download_description).setPositiveButton(17039370, this).setNegativeButton(17039360, null);
            this.representationList.setChoiceMode(2);
            this.representationList.setAdapter(this.trackTitles);
        }

        public void prepare() {
            this.downloadHelper.prepare(this);
        }

        public void onPrepared(DownloadHelper helper) {
            for (int i = 0; i < this.downloadHelper.getPeriodCount(); i++) {
                TrackGroupArray trackGroups = this.downloadHelper.getTrackGroups(i);
                for (int j = 0; j < trackGroups.length; j++) {
                    TrackGroup trackGroup = trackGroups.get(j);
                    for (int k = 0; k < trackGroup.length; k++) {
                        this.trackKeys.add(new TrackKey(i, j, k));
                        this.trackTitles.add(DownloadTracker.this.trackNameProvider.getTrackName(trackGroup.getFormat(k)));
                    }
                }
            }
            if (!this.trackKeys.isEmpty()) {
                this.builder.setView(this.dialogView);
            }
            this.builder.create().show();
        }

        public void onPrepareError(DownloadHelper helper, IOException e) {
            Toast.makeText(DownloadTracker.this.context.getApplicationContext(), R.string.download_start_error, Toast.LENGTH_SHORT).show();
            Log.e(DownloadTracker.TAG, "Failed to start download", e);
        }

        public void onClick(DialogInterface dialog, int which) {
            ArrayList<TrackKey> selectedTrackKeys = new ArrayList();
            for (int i = 0; i < this.representationList.getChildCount(); i++) {
                if (this.representationList.isItemChecked(i)) {
                    selectedTrackKeys.add(this.trackKeys.get(i));
                }
            }
            if (!selectedTrackKeys.isEmpty() || this.trackKeys.isEmpty()) {
                DownloadTracker.this.startDownload(this.downloadHelper.getDownloadAction(Util.getUtf8Bytes(this.name), selectedTrackKeys));
            }
        }
    }

    public DownloadTracker(Context context, Factory dataSourceFactory, File actionFile, Deserializer... deserializers) {
        Deserializer[] deserializerArr;
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        this.actionFile = new ActionFile(actionFile);
        this.trackNameProvider = new DefaultTrackNameProvider(context.getResources());
        HandlerThread actionFileWriteThread = new HandlerThread(TAG);
        actionFileWriteThread.start();
        this.actionFileWriteHandler = new Handler(actionFileWriteThread.getLooper());
        if (deserializers.length > 0) {
            deserializerArr = deserializers;
        } else {
            deserializerArr = DownloadAction.getDefaultDeserializers();
        }
        loadTrackedActions(deserializerArr);
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        return this.trackedDownloadStates.containsKey(uri);
    }

    public List<StreamKey> getOfflineStreamKeys(Uri uri) {
        if (this.trackedDownloadStates.containsKey(uri)) {
            return ((DownloadAction) this.trackedDownloadStates.get(uri)).getKeys();
        }
        return Collections.emptyList();
    }

    public void toggleDownload(Activity activity, String name, Uri uri, String extension) {
        if (isDownloaded(uri)) {
            startServiceWithAction(getDownloadHelper(uri, extension).getRemoveAction(Util.getUtf8Bytes(name)));
        } else {
            new StartDownloadDialogHelper(activity, getDownloadHelper(uri, extension), name).prepare();
        }
    }

    public void onInitialized(DownloadManager downloadManager) {
    }

    public void onTaskStateChanged(DownloadManager downloadManager, TaskState taskState) {
        DownloadAction action = taskState.action;
        Uri uri = action.uri;
        if (((action.isRemoveAction && taskState.state == 2) || (!action.isRemoveAction && taskState.state == 4)) && this.trackedDownloadStates.remove(uri) != null) {
            handleTrackedDownloadStatesChanged();
        }
    }

    public void onIdle(DownloadManager downloadManager) {
    }

    private void loadTrackedActions(Deserializer[] deserializers) {
        try {
            for (DownloadAction action : this.actionFile.load(deserializers)) {
                this.trackedDownloadStates.put(action.uri, action);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to load tracked actions", e);
        }
    }

    private void handleTrackedDownloadStatesChanged() {
        Iterator it = this.listeners.iterator();
        while (it.hasNext()) {
            ((Listener) it.next()).onDownloadsChanged();
        }
        final DownloadAction[] actions = (DownloadAction[]) this.trackedDownloadStates.values().toArray(new DownloadAction[0]);
        this.actionFileWriteHandler.post(new Runnable() {
            public void run() {
                try {
                    DownloadTracker.this.actionFile.store(actions);
                } catch (IOException e) {
                    Log.e(DownloadTracker.TAG, "Failed to store tracked actions", e);
                }
            }
        });
    }

    private void startDownload(DownloadAction action) {
        if (!this.trackedDownloadStates.containsKey(action.uri)) {
            this.trackedDownloadStates.put(action.uri, action);
            handleTrackedDownloadStatesChanged();
            startServiceWithAction(action);
        }
    }

    private void startServiceWithAction(DownloadAction action) {
        DownloadService.startWithAction(this.context, DemoDownloadService.class, action, false);
    }

    private DownloadHelper getDownloadHelper(Uri uri, String extension) {
        int type = Util.inferContentType(uri, extension);
        switch (type) {
            case 0:
                return new DashDownloadHelper(uri, this.dataSourceFactory);
            case 1:
                return new SsDownloadHelper(uri, this.dataSourceFactory);
            case 2:
                return new HlsDownloadHelper(uri, this.dataSourceFactory);
            case 3:
                return new ProgressiveDownloadHelper(uri);
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unsupported type: ");
                stringBuilder.append(type);
                throw new IllegalStateException(stringBuilder.toString());
        }
    }
}
