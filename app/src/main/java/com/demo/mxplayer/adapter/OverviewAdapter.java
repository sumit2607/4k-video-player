package com.demo.mxplayer.adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.demo.mxplayer.App;
import com.demo.mxplayer.R;
import com.demo.mxplayer.download.Constants;
import com.demo.mxplayer.download.DownloaderOverview;
import com.demo.mxplayer.download.DownloaderService;
import com.demo.mxplayer.download.FileDetailDialog;
import com.demo.mxplayer.models.SingleOverview;

import java.util.ArrayList;
import java.util.List;

public class OverviewAdapter extends ArrayAdapter<SingleOverview> {
    public static final String TAG = "OverviewAdapter";
    Context context;
    int layoutID;
    ArrayList<TextView> lblKBProgress;
    ArrayList<TextView> lblProgress;
    DownloaderService mDownloaderService;
    public Handler mHandler = new Handler(new Callback() {
        public boolean handleMessage(Message message) {
            Bundle data = message.getData();
            long j = data.getLong("downloaded", 0);
            int i = data.getInt("percentage", 0);
            if (data != null && message.what == 500) {
                try {
                    int i2 = message.arg1;
                    if (OverviewAdapter.this.records.size() <= i2 || ((SingleOverview) OverviewAdapter.this.records.get(i2)).totalByteSize > 0) {
                        if (OverviewAdapter.this.lblKBProgress.size() > i2 && OverviewAdapter.this.lblProgress.size() > i2 && OverviewAdapter.this.progressOverview.size() > i2 && OverviewAdapter.this.lblProgress.size() > i2) {
                            TextView textView = (TextView) OverviewAdapter.this.lblProgress.get(i2);
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(i);
                            stringBuilder.append("%");
                            textView.setText(stringBuilder.toString());
                            ((ProgressBar) OverviewAdapter.this.progressOverview.get(i2)).setProgress(i);
                            long j2 = ((SingleOverview) OverviewAdapter.this.records.get(i2)).totalByteSize;
                            TextView textView2 = (TextView) OverviewAdapter.this.lblKBProgress.get(i2);
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(App.humanReadableByteCount(j, false));
                            stringBuilder.append(" / ");
                            stringBuilder.append(App.humanReadableByteCount(j2, false));
                            textView2.setText(stringBuilder.toString());
                        }
                    } else if (OverviewAdapter.this.lblKBProgress.size() > i2) {
                        ((TextView) OverviewAdapter.this.lblKBProgress.get(i2)).setText(String.valueOf(App.humanReadableByteCount(j, false)));
                    }
                } catch (Exception e) {
                    String str = OverviewAdapter.TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("handle message exception : ");
                    stringBuilder2.append(e.getMessage());
                    Log.e(str, stringBuilder2.toString());
                }
            }
            return false;
        }
    });
    Fragment parentFragment;
    ArrayList<ProgressBar> progressOverview;
    ArrayList<SingleOverview> records;
    LayoutInflater vi;

    public OverviewAdapter(Context context, DownloaderService downloaderService, int i, List<SingleOverview> list, Fragment fragment) {
        super(context, i, list);
        this.context = context;
        this.records = (ArrayList) list;
        this.layoutID = i;
        this.parentFragment = fragment;
        this.mDownloaderService = downloaderService;
        this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.lblProgress = new ArrayList(this.records.size());
        this.lblKBProgress = new ArrayList(this.records.size());
        this.progressOverview = new ArrayList(this.records.size());
        for (int i2 = 0; i2 < this.records.size(); i2++) {
            this.lblProgress.add(null);
            this.lblKBProgress.add(null);
            this.progressOverview.add(null);
        }
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.vi.inflate(this.layoutID, null);
        }
        SingleOverview singleOverview = (SingleOverview) this.records.get(i);
        ((TextView) view.findViewById(R.id.lbl_overview_file_name)).setText(singleOverview.originalFileName);
        TextView textView = (TextView) view.findViewById(R.id.lbl_size_progress);
        TextView textView2 = (TextView) view.findViewById(R.id.lbl_overview_progress);
        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progress_overview);
        this.lblProgress.set(i, textView2);
        this.progressOverview.set(i, progressBar);
        this.lblKBProgress.set(i, textView);
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.btn_dl_pause_resume);
        if (singleOverview.totalByteSize <= 0) {
            textView.setText(App.humanReadableByteCount(singleOverview.totalCompleted, false));
            textView2.setVisibility(View.INVISIBLE);
            progressBar.setIndeterminate(true);
        } else {
            textView2.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(false);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(singleOverview.totalCompletedPercentage);
            stringBuilder.append("%");
            textView2.setText(stringBuilder.toString());
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(App.humanReadableByteCount(singleOverview.totalCompleted, false));
            stringBuilder2.append(" / ");
            stringBuilder2.append(App.humanReadableByteCount(singleOverview.totalByteSize, false));
            textView.setText(stringBuilder2.toString());
            progressBar.setProgress(singleOverview.totalCompletedPercentage);
        }
        singleOverview.listHandlerRef = this.mHandler;
        view.findViewById(R.id.btn_dl_info).setTag(Integer.valueOf(i));
        imageButton.setTag(Integer.valueOf(i));
        if (!singleOverview.singleThread) {
            imageButton.setEnabled(true);
            switch (singleOverview.status) {
                case 2:
                    imageButton.setImageResource(R.drawable.list_paus_button);
                    break;
                case 3:
                    imageButton.setImageResource(R.drawable.btn_dl_resume);
                    break;
            }
        }
        imageButton.setEnabled(false);
        view.setTag(Integer.valueOf(i));
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ((DownloaderOverview) OverviewAdapter.this.parentFragment).listRowClicked(((Integer) view.getTag()).intValue());
            }
        });
        view.findViewById(R.id.btn_dl_info).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                new FileDetailDialog(OverviewAdapter.this.context, OverviewAdapter.this.mDownloaderService, ((SingleOverview) OverviewAdapter.this.records.get(((Integer) view.getTag()).intValue())).id).show();
            }
        });
        imageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                int intValue = ((Integer) view.getTag()).intValue();
                SingleOverview singleOverview = (SingleOverview) OverviewAdapter.this.records.get(intValue);
                Message message;
                if (((SingleOverview) OverviewAdapter.this.records.get(intValue)).status == 2) {
                    message = new Message();
                    message.what = Constants.Thread_Download_Pause;
                    message.arg1 = singleOverview.id;
                    OverviewAdapter.this.mDownloaderService.mHandler.sendMessage(message);
                    ((ImageButton) view).setImageResource(R.drawable.btn_dl_resume);
                    ((SingleOverview) OverviewAdapter.this.records.get(intValue)).status = 3;
                } else if (((SingleOverview) OverviewAdapter.this.records.get(intValue)).status != 3) {
                } else {
                    if (OverviewAdapter.this.isOnline(OverviewAdapter.this.context)) {
                        message = new Message();
                        message.what = Constants.Thread_Download_Resume;
                        message.arg1 = singleOverview.id;
                        message.obj = ((SingleOverview) OverviewAdapter.this.records.get(intValue)).mHandler;
                        OverviewAdapter.this.mDownloaderService.mHandler.sendMessage(message);
                        ((ImageButton) view).setImageResource(R.drawable.list_paus_button);
                        ((SingleOverview) OverviewAdapter.this.records.get(intValue)).status = 2;
                        return;
                    }
                    Toast.makeText(OverviewAdapter.this.context, "Not connected to internet.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public boolean isOnline(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public void refreshRecords(ArrayList<SingleOverview> arrayList) {
        this.records = arrayList;
        this.lblProgress.removeAll(this.lblProgress);
        this.lblKBProgress.removeAll(this.lblKBProgress);
        this.progressOverview.removeAll(this.progressOverview);
        this.lblProgress = new ArrayList(this.records.size());
        this.lblKBProgress = new ArrayList(this.records.size());
        this.progressOverview = new ArrayList(this.records.size());
        for (int i = 0; i < this.records.size(); i++) {
            this.lblProgress.add(null);
            this.lblKBProgress.add(null);
            this.progressOverview.add(null);
        }
        notifyDataSetChanged();
    }

    public long getItemId(int i) {
        return (long) ((SingleOverview) this.records.get(i)).id;
    }

    public SingleOverview getItem(int i) {
        return (SingleOverview) this.records.get(i);
    }

    public int getCount() {
        return this.records.size();
    }
}
