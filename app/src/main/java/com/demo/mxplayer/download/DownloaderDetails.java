package com.demo.mxplayer.download;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.provider.FontsContractCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.demo.mxplayer.App;
import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.DownloaderThread;
import com.demo.mxplayer.models.SingleFile;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class DownloaderDetails extends Fragment implements OnClickListener {
    public static final String TAG = "DownloaderDetails";
    SingleFile downloadFile;
    Button fabDelete;
    Button fabPause;
    public static int fileid;
    boolean isPaused;
    long lastCompleted;
    long lastTime = -1;
    TextView lblOverall;
    TextView lblSingle;
    TextView lblSinglePer;
    TextView lblSpeed;
    TextView lblSpeedSingle;
    TextView lblTh1Per;
    TextView lblTh2Per;
    TextView lblTh3Per;
    TextView lbltotalBytes;
    MainActivity mActivity;
    public static DownloaderService mDownloaderService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            int intExtra;
            if (intent.getAction().equals(Constants.BROADCAST_FILE_DOWNLOAD_COMPLETE)) {
                if (intent.hasExtra(FontsContractCompat.Columns.FILE_ID) && DownloaderDetails.this.mActivity.activityResumed) {
                    intExtra = intent.getIntExtra(FontsContractCompat.Columns.FILE_ID, -1);
                    if (intExtra > 0 && intExtra == DownloaderDetails.this.fileid) {

                        new FileDetailDialog(DownloaderDetails.this.mActivity, DownloaderDetails.this.mDownloaderService, DownloaderDetails.this.fileid).show();
                        DownloaderDetails.this.getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            } else if (intent.getAction().equals(Constants.BROADCAST_FILE_DELETE) && intent.hasExtra(FontsContractCompat.Columns.FILE_ID)) {
                intExtra = intent.getIntExtra(FontsContractCompat.Columns.FILE_ID, -1);
                if (intExtra > 0 && intExtra == DownloaderDetails.this.fileid) {

                    DownloaderDetails.this.getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        }
    };

    Handler mHandler = new Handler(new Callback() {
        public boolean handleMessage(Message message) {
            try {
                Bundle data = message.getData();
                if (data != null) {
                    TextView textView;
                    StringBuilder stringBuilder;
                    long j = data.getLong("downloaded", 0);
                    int i = data.getInt("percentage", 0);
                    DownloaderDetails downloaderDetails = DownloaderDetails.this;
                    downloaderDetails.totalCompleted += j;
                    StringBuilder stringBuilder2;
                    switch (message.what) {
                        case 501:
                            if (DownloaderDetails.this.singleThread) {
                                if (DownloaderDetails.this.downloadFile.size > 0) {
                                    textView = DownloaderDetails.this.lblSingle;
                                    stringBuilder2 = new StringBuilder();
                                    stringBuilder2.append(App.humanReadableByteCount(DownloaderDetails.this.totalCompleted, false));
                                    stringBuilder2.append(" / ");
                                    stringBuilder2.append(App.humanReadableByteCount(DownloaderDetails.this.downloadFile.size, false));
                                    textView.setText(stringBuilder2.toString());
                                    DownloaderDetails.this.singleProgress.setProgress(i);
                                    textView = DownloaderDetails.this.lblSinglePer;
                                    stringBuilder2 = new StringBuilder();
                                    stringBuilder2.append(i);
                                    stringBuilder2.append(" %");
                                    textView.setText(stringBuilder2.toString());
                                    break;
                                }
                                textView = DownloaderDetails.this.lblSingle;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(App.humanReadableByteCount(DownloaderDetails.this.totalCompleted, false));
                                stringBuilder.append(" / ");
                                stringBuilder.append(DownloaderDetails.this.getString(R.string.string_unknown));
                                textView.setText(stringBuilder.toString());
                                break;
                            }
                            DownloaderDetails.this.thread1Completed = i;
                            textView = DownloaderDetails.this.lblTh1Per;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(i);
                            stringBuilder2.append("%");
                            textView.setText(stringBuilder2.toString());
                            DownloaderDetails.this.thread1Progress.setProgress(i);
                            break;
                        case 502:
                            DownloaderDetails.this.thread2Completed = i;
                            textView = DownloaderDetails.this.lblTh2Per;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(i);
                            stringBuilder2.append("%");
                            textView.setText(stringBuilder2.toString());
                            DownloaderDetails.this.thread2Progress.setProgress(i);
                            break;
                        case 503:
                            DownloaderDetails.this.thread3Completed = i;
                            textView = DownloaderDetails.this.lblTh3Per;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(i);
                            stringBuilder2.append("%");
                            textView.setText(stringBuilder2.toString());
                            DownloaderDetails.this.thread3Progress.setProgress(i);
                            break;
                        default:
                            break;
                    }
                    DownloaderDetails.this.totalCompletedPercent = ((DownloaderDetails.this.thread1Completed + DownloaderDetails.this.thread2Completed) + DownloaderDetails.this.thread3Completed) / 3;
                    j = System.currentTimeMillis();
                    if (DownloaderDetails.this.lastTime <= 0) {
                        DownloaderDetails.this.lastTime = j;
                        DownloaderDetails.this.lastCompleted = DownloaderDetails.this.totalCompleted;
                    }
                    long j2 = DownloaderDetails.this.totalCompleted - DownloaderDetails.this.lastCompleted;
                    if (j - DownloaderDetails.this.lastTime >= 1000) {
                        if (DownloaderDetails.this.singleThread) {
                            textView = DownloaderDetails.this.lblSpeedSingle;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(App.humanReadableByteCount(j2, false));
                            stringBuilder.append("/s");
                            textView.setText(stringBuilder.toString());
                            textView = DownloaderDetails.this.lblSingle;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(App.humanReadableByteCount(DownloaderDetails.this.totalCompleted, false));
                            stringBuilder.append(" / ");
                            stringBuilder.append(DownloaderDetails.this.getString(R.string.string_unknown));
                            textView.setText(stringBuilder.toString());
                        } else {
                            textView = DownloaderDetails.this.lblSpeed;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(App.humanReadableByteCount(j2, false));
                            stringBuilder.append("/s");
                            textView.setText(stringBuilder.toString());
                            textView = DownloaderDetails.this.lbltotalBytes;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(App.humanReadableByteCount(DownloaderDetails.this.totalCompleted, false));
                            stringBuilder.append(" / ");
                            stringBuilder.append(App.humanReadableByteCount(DownloaderDetails.this.downloadFile.size, false));
                            textView.setText(stringBuilder.toString());
                        }
                        DownloaderDetails.this.lastCompleted = DownloaderDetails.this.totalCompleted;
                        DownloaderDetails.this.lastTime = j;
                    }
                    DownloaderDetails.this.totalProgress.setProgress(DownloaderDetails.this.totalCompletedPercent);
                    textView = DownloaderDetails.this.lblOverall;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(DownloaderDetails.this.totalCompletedPercent);
                    stringBuilder.append("%");
                    textView.setText(stringBuilder.toString());
                } else if (message.what == 540) {
                    DownloaderDetails.this.getActivity().getSupportFragmentManager().beginTransaction().remove(DownloaderDetails.this).commit();
                }
            } catch (Exception e) {
                String str = DownloaderDetails.TAG;
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append("Error handling msg : ");
                stringBuilder3.append(e.getMessage());
                Log.e(str, stringBuilder3.toString());
            }
            return false;
        }
    });
    final int msgRemoveFragment = 540;
    RelativeLayout rlMulti;
    RelativeLayout rlSingle;
    ProgressBar singleProgress;
    public static boolean singleThread = false;
    public static int status;
    int thread1Completed;
    ProgressBar thread1Progress;
    int thread2Completed;
    ProgressBar thread2Progress;
    int thread3Completed;
    ProgressBar thread3Progress;
    ArrayList<DownloaderThread> threads;
    long totalCompleted;
    int totalCompletedPercent;
    ProgressBar totalProgress;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.activity_downloader_details, viewGroup, false);

      //  inflate.findViewById(R.id.img_toolbar_back).setOnClickListener(this);
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("File ID : ");
        stringBuilder.append(this.fileid);
        Log.i(str, stringBuilder.toString());
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("Status : ");
        stringBuilder.append(this.status);
        Log.i(str, stringBuilder.toString());
        str = TAG;
        stringBuilder = new StringBuilder();
        stringBuilder.append("singleThread : ");
        stringBuilder.append(this.singleThread);
        Log.i(str, stringBuilder.toString());
        this.threads = new ArrayList();
        DbHandler.openDB();
        this.downloadFile = DbHandler.getFileDetail(this.fileid);
        DbHandler.closeDB();
        if (this.downloadFile != null) {
            this.status = this.downloadFile.status;
            this.singleThread = this.downloadFile.singleThread;
            setView(inflate);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_FILE_DELETE);
            intentFilter.addAction(Constants.BROADCAST_FILE_DOWNLOAD_COMPLETE);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.mBroadcastReceiver, intentFilter);
        } else {
            this.mHandler.sendEmptyMessageDelayed(540, 100);
        }
        return inflate;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        super.onAttach(getActivity());
        if (context instanceof MainActivity) {
            this.mActivity = (MainActivity) context;
            if (this.mDownloaderService == null) {
                this.mDownloaderService = this.mActivity.mDownloaderService;
            }
        }
    }

    public void onDestroy() {
        for (int i = 0; i < this.threads.size(); i++) {
            Log.i(TAG, "Handler set NULL");
            ((DownloaderThread) this.threads.get(i)).showDetailProgress = false;
            ((DownloaderThread) this.threads.get(i)).mDetailHandler = null;
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.mBroadcastReceiver);
        super.onDestroy();
    }

    private void setView(View view) {
        StringBuilder stringBuilder;
        this.rlMulti = (RelativeLayout) view.findViewById(R.id.rl_download_detail_multithread);
        this.rlSingle = (RelativeLayout) view.findViewById(R.id.rl_download_detail_singlethread);
        this.totalProgress = (ProgressBar) view.findViewById(R.id.progress_overall);
        this.thread1Progress = (ProgressBar) view.findViewById(R.id.progress_thread_1);
        this.thread2Progress = (ProgressBar) view.findViewById(R.id.progress_thread_2);
        this.thread3Progress = (ProgressBar) view.findViewById(R.id.progress_thread_3);
        this.singleProgress = (ProgressBar) view.findViewById(R.id.progress_single);
        this.lblTh1Per = (TextView) view.findViewById(R.id.lbl_thread_1_per);
        this.lblTh2Per = (TextView) view.findViewById(R.id.lbl_thread_2_per);
        this.lblTh3Per = (TextView) view.findViewById(R.id.lbl_thread_3_per);
        this.lblOverall = (TextView) view.findViewById(R.id.lbl_overall_per);
        this.lblSingle = (TextView) view.findViewById(R.id.lbl_single_completed);
        this.lbltotalBytes = (TextView) view.findViewById(R.id.lbl_overall_bytes);
        this.lblSpeed = (TextView) view.findViewById(R.id.lbl_download_speed);
        this.lblSpeedSingle = (TextView) view.findViewById(R.id.lbl_download_speed_single);
        this.lblSinglePer = (TextView) view.findViewById(R.id.lbl_single_per);
//        ((TextView) view.findViewById(R.id.lbl_title)).setText(this.downloadFile.title);
        this.fabPause = (Button) view.findViewById(R.id.btn_multi_download_pause_resume);
        this.fabDelete = (Button) view.findViewById(R.id.btn_multi_download_delete);
        this.fabPause.setOnClickListener(this);
        this.fabDelete.setOnClickListener(this);
        view.findViewById(R.id.btn_single_download_delete).setOnClickListener(this);
        view.findViewById(R.id.btn_single_download_info).setOnClickListener(this);
        view.findViewById(R.id.btn_multi_download_info).setOnClickListener(this);

        this.totalCompleted = 0;
        this.totalCompletedPercent = 0;
        if (this.fileid != -1) {
            int i;
            switch (this.status) {
                case 2:
                    this.isPaused = false;
                    this.fabPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.download_pause, 0, 0, 0);
                    this.fabPause.setText(getString(R.string.string_pause));
                    DbHandler.openDB();
                    this.threads = DbHandler.getCompletedThreadsByFileid(this.fileid);
                    String str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("completed threads size : ");
                    stringBuilder.append(this.threads.size());
                    Log.i(str, stringBuilder.toString());
                    for (i = 0; i < this.mDownloaderService.activeThreads.size(); i++) {
                        if (((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).id == this.fileid) {
                            ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).showDetailProgress = true;
                            this.threads.add(this.mDownloaderService.activeThreads.get(i));
                            switch (((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).partNo) {
                                case 1:
                                    this.thread1Completed = ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).completedPercentage;
                                    break;
                                case 2:
                                    this.thread2Completed = ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).completedPercentage;
                                    break;
                                case 3:
                                    this.thread3Completed = ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).completedPercentage;
                                    break;
                            }
                            ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).mDetailHandler = this.mHandler;
                            this.totalCompleted += ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).completedDownload;
                            this.totalCompletedPercent += ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).completedPercentage;
                        }
                    }
                    DbHandler.closeDB();
                    break;
                case 3:
                    this.isPaused = true;
                    this.fabPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.download_resume, 0, 0, 0);
                    this.fabPause.setText(getString(R.string.string_resume));
                    DbHandler.openDB();
                    this.threads = DbHandler.getThreadsByFileid(this.fileid);
                    for (i = 0; i < this.threads.size(); i++) {
                        switch (((DownloaderThread) this.threads.get(i)).partNo) {
                            case 1:
                                this.thread1Completed = ((DownloaderThread) this.threads.get(i)).completedPercentage;
                                break;
                            case 2:
                                this.thread2Completed = ((DownloaderThread) this.threads.get(i)).completedPercentage;
                                break;
                            case 3:
                                this.thread3Completed = ((DownloaderThread) this.threads.get(i)).completedPercentage;
                                break;
                            default:
                                break;
                        }
                        this.totalCompleted += ((DownloaderThread) this.threads.get(i)).completedDownload;
                        this.totalCompletedPercent += ((DownloaderThread) this.threads.get(i)).completedPercentage;
                    }
                    DbHandler.closeDB();
                    this.threads.removeAll(this.threads);
                    break;
            }
        }
        this.thread1Progress.setProgress(this.thread1Completed);
        this.thread2Progress.setProgress(this.thread2Completed);
        this.thread3Progress.setProgress(this.thread3Completed);
        this.singleProgress.setProgress(this.thread1Completed);
        TextView textView = this.lblTh1Per;
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.thread1Completed);
        stringBuilder.append("%");
        textView.setText(stringBuilder.toString());
        textView = this.lblTh2Per;
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.thread2Completed);
        stringBuilder.append("%");
        textView.setText(stringBuilder.toString());
        textView = this.lblTh3Per;
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.thread3Completed);
        stringBuilder.append("%");
        textView.setText(stringBuilder.toString());
        this.totalCompletedPercent /= 3;
        this.totalProgress.setProgress(this.totalCompletedPercent);
        textView = this.lblOverall;
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.totalCompletedPercent);
        stringBuilder.append("%");
        textView.setText(stringBuilder.toString());
        textView = this.lbltotalBytes;
        stringBuilder = new StringBuilder();
        stringBuilder.append(App.humanReadableByteCount(this.totalCompleted, false));
        stringBuilder.append(File.separator);
        stringBuilder.append(App.humanReadableByteCount(this.downloadFile.size, false));
        textView.setText(stringBuilder.toString());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM y  K:m a");
        if (this.singleThread) {
            this.rlMulti.setVisibility(View.GONE);
            this.rlSingle.setVisibility(View.VISIBLE);
            EditText editText = (EditText) view.findViewById(R.id.lbl_s_path);
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(this.downloadFile.path);
            stringBuilder2.append(File.separator);
            stringBuilder2.append(this.downloadFile.title);
            editText.setText(stringBuilder2.toString());
            ((EditText) view.findViewById(R.id.lbl_s_url)).setText(this.downloadFile.url);
            ((EditText) view.findViewById(R.id.lbl_s_path)).setInputType(0);
            ((EditText) view.findViewById(R.id.lbl_s_url)).setInputType(0);
            ((TextView) view.findViewById(R.id.lbl_s_datetime)).setText(simpleDateFormat.format(this.downloadFile.created));
            view.findViewById(R.id.btn_s_copy).setOnClickListener(this);
            TextView textView2;
            StringBuilder stringBuilder3;
            if (this.downloadFile.size <= 0) {
                textView2 = this.lblSingle;
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append(App.humanReadableByteCount(this.totalCompleted, false));
                stringBuilder3.append(" / ");
                stringBuilder3.append(getString(R.string.string_unknown));
                textView2.setText(stringBuilder3.toString());
                this.singleProgress.setIndeterminate(true);
                this.lblSinglePer.setText("");
                return;
            }
            textView2 = this.lblSingle;
            stringBuilder3 = new StringBuilder();
            stringBuilder3.append(App.humanReadableByteCount(this.totalCompleted, false));
            stringBuilder3.append(" / ");
            stringBuilder3.append(App.humanReadableByteCount(this.downloadFile.size, false));
            textView2.setText(stringBuilder3.toString());
            this.singleProgress.setIndeterminate(false);
            int i2 = (int) ((this.totalCompleted * 100) / this.downloadFile.size);
            this.singleProgress.setProgress(i2);
            TextView textView3 = this.lblSinglePer;
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(i2);
            stringBuilder4.append(" %");
            textView3.setText(stringBuilder4.toString());
            return;
        }
        this.rlMulti.setVisibility(View.VISIBLE);
        this.rlSingle.setVisibility(View.GONE);
        EditText editText2 = (EditText) view.findViewById(R.id.lbl_m_path);
        StringBuilder stringBuilder5 = new StringBuilder();
        stringBuilder5.append(this.downloadFile.path);
        stringBuilder5.append(File.separator);
        stringBuilder5.append(this.downloadFile.title);
        editText2.setText(stringBuilder5.toString());
        ((EditText) view.findViewById(R.id.lbl_m_url)).setText(this.downloadFile.url);
        ((EditText) view.findViewById(R.id.lbl_m_path)).setInputType(0);
        ((EditText) view.findViewById(R.id.lbl_m_url)).setInputType(0);
        ((TextView) view.findViewById(R.id.lbl_m_datetime)).setText(simpleDateFormat.format(this.downloadFile.created));
        view.findViewById(R.id.btn_m_copy).setOnClickListener(this);
    }


    public void onClick(View view) {
        StringBuilder stringBuilder;
      int  id = view.getId();


            int i = 0;
            switch (id) {
                case R.id.btn_m_copy:
                case R.id.btn_s_copy:
                    try {
                        if (Build.VERSION.SDK_INT < 11) {
                            ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setText(this.downloadFile.url);
                        } else {
                            ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Copied Text", this.downloadFile.url));
                        }
                        Toast.makeText(getActivity(), getString(R.string.string_url_copied),Toast.LENGTH_SHORT).show();
                        return;
                    } catch (Exception view2) {
                        String str = TAG;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("Copy url Error : ");
                        stringBuilder.append(view2.getMessage());
                        Log.e(str, stringBuilder.toString());
                    }
                case R.id.btn_multi_download_delete:
                    AlertDialog.Builder   view2 = new AlertDialog.Builder(new ContextThemeWrapper(this.mActivity, R.style.AlertDialogCustom));
                    view2.setTitle(getString(R.string.dialog_file_detail_downloading_title));
                    view2.setMessage(getString(R.string.dialog_file_detail_downloading_details));
                    view2.setPositiveButton(getString(R.string.string_remove), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            DownloaderDetails.this.deleteDownload();
                        }
                    }).setNegativeButton(getString(R.string.string_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    view2.create().show();
                    return;
                case R.id.btn_multi_download_info:
                    new FileDetailDialog(getActivity(), this.mDownloaderService, this.fileid).show();
                    return;
                case R.id.btn_multi_download_pause_resume:
                    Log.i(TAG, "fab_pause_resume clicked");
                    long j = 0;
                    if (this.isPaused ) {
                        this.fabPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.download_pause, 0, 0, 0);
                        this.fabPause.setText(getString(R.string.string_pause));
                        if (isOnline(getActivity())) {
                            this.threads.removeAll(this.threads);
                            this.totalCompleted = 0;
                            DbHandler.openDB();
                            this.threads = DbHandler.getThreadsByFileid(this.fileid);
                            this.mDownloaderService.activeThreads.addAll(this.threads);

                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Paused Threads Size : ");
                            stringBuilder.append(this.threads.size());
                            Log.i(TAG, stringBuilder.toString());

                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Show overview progress : ");
                            stringBuilder.append(false);
                            Log.i(TAG, stringBuilder.toString());
                            while (i < this.threads.size()) {
                                this.totalCompleted += ((DownloaderThread) this.threads.get(i)).completedDownload;
                                switch (((DownloaderThread) this.threads.get(i)).partNo) {
                                    case DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED:
                                        this.thread1Completed = ((DownloaderThread) this.threads.get(i)).completedPercentage;
                                        break;
                                    case  2:
                                        this.thread2Completed = ((DownloaderThread) this.threads.get(i)).completedPercentage;
                                        break;
                                    case 3:
                                        this.thread3Completed = ((DownloaderThread) this.threads.get(i)).completedPercentage;
                                        break;
                                    default:
                                        break;
                                }
                                ((DownloaderThread) this.threads.get(i)).mServiceHandler = this.mDownloaderService.mHandler;
                                ((DownloaderThread) this.threads.get(i)).showDetailProgress = true;
                                ((DownloaderThread) this.threads.get(i)).mDetailHandler = this.mHandler;
                                ((DownloaderThread) this.threads.get(i)).start();
                                DbHandler.setThreadRunning(this.fileid, ((DownloaderThread) this.threads.get(i)).partNo);
                                i++;
                            }
                            this.thread1Progress.setProgress(this.thread1Completed);
                            this.thread2Progress.setProgress(this.thread2Completed);
                            this.thread3Progress.setProgress(this.thread3Completed);
                            this.totalCompletedPercent = ((this.thread1Completed + this.thread2Completed) + this.thread3Completed) / 3;
                            this.totalProgress.setProgress(this.totalCompletedPercent);
                            DbHandler.setFileRunning(this.fileid);
                            DbHandler.closeDB();
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.string_no_internet),Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        this.fabPause.setCompoundDrawablesWithIntrinsicBounds(R.drawable.download_resume, 0, 0, 0);
                        this.fabPause.setText(getString(R.string.string_resume));
                        this.lblSpeed.setText("00 KB/s");
                        this.lblSpeedSingle.setText("00 KB/s");
                        DbHandler.openDB();
                       int  view3 = 0;
                        while ( view3  < this.threads.size()) {
                            ((DownloaderThread) this.threads.get(view3)).running = false;
                            ((DownloaderThread) this.threads.get(view3)).showDetailProgress = false;
                            ((DownloaderThread) this.threads.get(view3)).showOverviewProgress = false;
                            ((DownloaderThread) this.threads.get(view3)).mDetailHandler = null;
                            ((DownloaderThread) this.threads.get(view3)).mOverviewHandler = null;
                            long j2 = ((DownloaderThread) this.threads.get(view3)).completedDownload;
                            long j3 = j + j2;
                            DbHandler.setThreadPause(this.fileid, ((DownloaderThread) this.threads.get(view3)).partNo, j2);
                            this.mDownloaderService.removeDownloadingThread(((DownloaderThread) this.threads.get(view3)).id, ((DownloaderThread) this.threads.get(view3)).partNo);
                            view3++;
                            j = j3;
                        }
                        this.threads.removeAll(this.threads);
                        DbHandler.setFilePause(this.fileid, j);
                        DbHandler.closeDB();
                    }
                    this.isPaused = true;
                    return;
                default:
                    switch (id) {
                        case R.id.btn_single_download_delete:
                            view2 = new AlertDialog.Builder(new ContextThemeWrapper(this.mActivity, R.style.AlertDialogCustom));
                            view2.setTitle(getString(R.string.dialog_file_detail_downloading_title));
                            view2.setMessage(getString(R.string.dialog_file_detail_downloading_details));
                            view2.setPositiveButton(getString(R.string.string_remove), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    DownloaderDetails.this.deleteDownload();
                                }
                            }).setNegativeButton(getString(R.string.string_cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            view2.create().show();
                            return;
                        case R.id.btn_single_download_info:
                            new FileDetailDialog(getActivity(), this.mDownloaderService, this.fileid).show();
                            return;
                        default:
                            return;
                    }
            }

        //getActivity().getSupportFragmentManager().popBackStack();
    }

    public void deleteDownload() {
        StringBuilder stringBuilder;
        DbHandler.openDB();
        int i = 0;
        for (int i2 = 0; i2 < this.threads.size(); i2++) {
            ((DownloaderThread) this.threads.get(i2)).running = false;
            ((DownloaderThread) this.threads.get(i2)).showDetailProgress = false;
            ((DownloaderThread) this.threads.get(i2)).showOverviewProgress = false;
            ((DownloaderThread) this.threads.get(i2)).mDetailHandler = null;
            ((DownloaderThread) this.threads.get(i2)).mOverviewHandler = null;
            DbHandler.setThreadPause(this.fileid, ((DownloaderThread) this.threads.get(i2)).partNo, 0);
            this.mDownloaderService.removeDownloadingThread(((DownloaderThread) this.threads.get(i2)).id, ((DownloaderThread) this.threads.get(i2)).partNo);
        }
        this.threads.removeAll(this.threads);
        DbHandler.deleteFileThreads(this.fileid);
        DbHandler.closeDB();
        while (i < this.threads.size()) {
            stringBuilder = new StringBuilder();
            stringBuilder.append(App.partPath);
            stringBuilder.append(File.separator);
            stringBuilder.append(this.fileid);
            stringBuilder.append(File.separator);
            stringBuilder.append(this.downloadFile.title);
            stringBuilder.append(".");
            stringBuilder.append(((DownloaderThread) this.threads.get(i)).partNo);
            stringBuilder.append("_part");
            File file = new File(stringBuilder.toString());
            if (file.exists()) {
                String str = TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("Part File : ");
                stringBuilder2.append(file.getAbsolutePath());
                stringBuilder2.append(" Deleted");
                Log.i(str, stringBuilder2.toString());
                file.delete();
            }
            i++;
        }
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(App.partPath);
        stringBuilder3.append(File.separator);
        stringBuilder3.append(this.fileid);
        File file2 = new File(stringBuilder3.toString());
        if (file2.exists()) {
            String str2 = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Part Folder : ");
            stringBuilder.append(file2.getAbsolutePath());
            stringBuilder.append(" Deleted");
            Log.i(str2, stringBuilder.toString());
            file2.delete();
        }
        Intent intent = new Intent(Constants.BROADCAST_FILE_DELETE);
        intent.putExtra(FontsContractCompat.Columns.FILE_ID, this.fileid);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        this.mActivity.getSupportFragmentManager().popBackStackImmediate();
    }

    public boolean isOnline(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
