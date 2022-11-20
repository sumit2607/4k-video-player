package com.demo.mxplayer.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.adapter.OverviewAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.DownloaderThread;
import com.demo.mxplayer.models.SingleOverview;

import java.util.ArrayList;

public class DownloaderOverview extends Fragment implements OnClickListener {
    public static final String TAG = "DownloaderOverview";
    public ArrayList<SingleOverview> activeList;
    OverviewAdapter adapter;
    TextView lblNoRecordFound;
    ListView listOverview;
    MainActivity mActivity;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BROADCAST_FILE_DOWNLOAD_COMPLETE)) {
                DownloaderOverview.this.refreshDownloadingList();
            } else if (intent.getAction().equals(Constants.BROADCAST_FILE_DELETE)) {
                DownloaderOverview.this.refreshDownloadingList();
            }
        }
    };
    public static DownloaderService mDownloaderService;
    Handler mHandler = new Handler(new Callback() {
        public boolean handleMessage(Message message) {
            if (message.what == 540) {
                DownloaderOverview.this.getActivity().getSupportFragmentManager().beginTransaction().remove(DownloaderOverview.this).commit();
            }
            return false;
        }
    });
    final int msgRemoveFragment = 540;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.activity_download_overview, viewGroup, false);

        this.activeList = new ArrayList();
        if (this.mDownloaderService == null || this.mDownloaderService.activeThreads == null) {
            this.mHandler.sendEmptyMessageDelayed(540, 100);
        } else {
            int i;
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Total Active Threads : ");
            stringBuilder.append(this.mDownloaderService.activeThreads.size());
            Log.i(str, stringBuilder.toString());
            DbHandler.openDB();
            this.activeList = DbHandler.getNotCompletedFiles();
            for (i = 0; i < this.activeList.size(); i++) {
                int i2 = 0;
                int i3 = i2;
                while (i2 < this.mDownloaderService.activeThreads.size()) {
                    ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).showOverviewProgress = true;
                    if (((SingleOverview) this.activeList.get(i)).id == ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).id) {
                        ((SingleOverview) this.activeList.get(i)).addThreadRef((DownloaderThread) this.mDownloaderService.activeThreads.get(i2));
                        ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).mOverviewHandler = ((SingleOverview) this.activeList.get(i)).mHandler;
                        if (((SingleOverview) this.activeList.get(i)).status == 2) {
                            SingleOverview singleOverview = (SingleOverview) this.activeList.get(i);
                            singleOverview.compareTotalCompleted += ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).completedDownload;
                        }
                        i3 = 1;
                    }
                    i2++;
                }
                if (i3 == 0) {
                    ArrayList threadsByFileid = DbHandler.getThreadsByFileid(((SingleOverview) this.activeList.get(i)).id);
                    int i4 = 0;
                    long j = 0;
                    int i5 = i4;
                    while (i5 < threadsByFileid.size()) {
                        long j2 = j + ((DownloaderThread) threadsByFileid.get(i5)).completedDownload;
                        i4 += ((DownloaderThread) threadsByFileid.get(i5)).completedPercentage;
                        ((DownloaderThread) threadsByFileid.get(i5)).showOverviewProgress = true;
                        ((SingleOverview) this.activeList.get(i)).addThreadRef((DownloaderThread) threadsByFileid.get(i5));
                        ((DownloaderThread) threadsByFileid.get(i5)).mOverviewHandler = ((SingleOverview) this.activeList.get(i)).mHandler;
                        i5++;
                        j = j2;
                    }
                    ((SingleOverview) this.activeList.get(i)).totalCompleted = j;
                    ((SingleOverview) this.activeList.get(i)).totalCompletedPercentage = i4 / 3;
                }
            }
            for (i = 0; i < this.activeList.size(); i++) {
                if (((SingleOverview) this.activeList.get(i)).compareTotalCompleted > ((SingleOverview) this.activeList.get(i)).totalCompleted) {
                    ((SingleOverview) this.activeList.get(i)).totalCompleted = ((SingleOverview) this.activeList.get(i)).compareTotalCompleted;
                }
                ((SingleOverview) this.activeList.get(i)).compareTotalCompleted = 0;
            }
            this.lblNoRecordFound = (TextView) inflate.findViewById(R.id.lbl_no_item_found);
            if (this.activeList.size() <= 0) {
                this.lblNoRecordFound.setVisibility(View.VISIBLE);
            } else {
                this.lblNoRecordFound.setVisibility(View.GONE);
            }
            DbHandler.closeDB();
            str = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Active Download Size : ");
            stringBuilder2.append(this.activeList.size());
            Log.i(str, stringBuilder2.toString());
            this.listOverview = (ListView) inflate.findViewById(R.id.list_active_downloads);
            this.adapter = new OverviewAdapter(getActivity(), this.mDownloaderService, R.layout.single_download_overview, this.activeList, this);
            this.listOverview.setAdapter(this.adapter);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.BROADCAST_FILE_DELETE);
            intentFilter.addAction(Constants.BROADCAST_FILE_DOWNLOAD_COMPLETE);
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(this.mBroadcastReceiver, intentFilter);
        }
        return inflate;
    }

    public void listRowClicked(int i) {
        if (i < this.activeList.size()) {
            FragmentTransaction beginTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            DownloaderDetails downloaderDetails = new DownloaderDetails();
            downloaderDetails.mDownloaderService = this.mDownloaderService;
            downloaderDetails.fileid = ((SingleOverview) this.activeList.get(i)).id;
            downloaderDetails.status = ((SingleOverview) this.activeList.get(i)).status;
            downloaderDetails.singleThread = ((SingleOverview) this.activeList.get(i)).singleThread;
            beginTransaction.add(R.id.main_fragment, downloaderDetails, "DownloadDetails");
            beginTransaction.addToBackStack("DownloadDetails");
            beginTransaction.commit();
        }
    }

    public void onClick(View view) {

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
        for (int i = 0; i < this.mDownloaderService.activeThreads.size(); i++) {
            ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).showOverviewProgress = false;
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(this.mBroadcastReceiver);
        super.onDestroy();
    }

    /* Access modifiers changed, original: 0000 */
    public void refreshDownloadingList() {
        int i;
        for (i = 0; i < this.mDownloaderService.activeThreads.size(); i++) {
            ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).showOverviewProgress = false;
            ((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).mDetailHandler = null;
        }
        for (i = this.activeList.size() - 1; i >= 0; i--) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("======= setting list ref null activeList : ");
            stringBuilder.append(i);
            Log.i(str, stringBuilder.toString());
            ((SingleOverview) this.activeList.get(i)).listHandlerRef = null;
            this.activeList.remove(i);
        }
        this.activeList.removeAll(this.activeList);
        DbHandler.openDB();
        this.activeList = DbHandler.getNotCompletedFiles();
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("new files count : ");
        stringBuilder2.append(this.activeList.size());
        Log.i(str2, stringBuilder2.toString());
        for (i = 0; i < this.activeList.size(); i++) {
            int i2 = 0;
            int i3 = i2;
            while (i2 < this.mDownloaderService.activeThreads.size()) {
                ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).showOverviewProgress = true;
                if (((SingleOverview) this.activeList.get(i)).id == ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).id) {
                    ((SingleOverview) this.activeList.get(i)).addThreadRef((DownloaderThread) this.mDownloaderService.activeThreads.get(i2));
                    ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).mOverviewHandler = ((SingleOverview) this.activeList.get(i)).mHandler;
                    if (((SingleOverview) this.activeList.get(i)).status == 2) {
                        SingleOverview singleOverview = (SingleOverview) this.activeList.get(i);
                        singleOverview.compareTotalCompleted += ((DownloaderThread) this.mDownloaderService.activeThreads.get(i2)).completedDownload;
                    }
                    i3 = 1;
                }
                i2++;
            }
            if (i3 == 0) {
                ArrayList threadsByFileid = DbHandler.getThreadsByFileid(((SingleOverview) this.activeList.get(i)).id);
                int i4 = 0;
                long j = 0;
                int i5 = i4;
                while (i5 < threadsByFileid.size()) {
                    long j2 = j + ((DownloaderThread) threadsByFileid.get(i5)).completedDownload;
                    i4 += ((DownloaderThread) threadsByFileid.get(i5)).completedPercentage;
                    ((DownloaderThread) threadsByFileid.get(i5)).showOverviewProgress = true;
                    ((SingleOverview) this.activeList.get(i)).addThreadRef((DownloaderThread) threadsByFileid.get(i5));
                    ((DownloaderThread) threadsByFileid.get(i5)).mOverviewHandler = ((SingleOverview) this.activeList.get(i)).mHandler;
                    i5++;
                    j = j2;
                }
                ((SingleOverview) this.activeList.get(i)).totalCompleted = j;
                ((SingleOverview) this.activeList.get(i)).totalCompletedPercentage = i4 / 3;
            }
        }
        for (i = 0; i < this.activeList.size(); i++) {
            if (((SingleOverview) this.activeList.get(i)).compareTotalCompleted > ((SingleOverview) this.activeList.get(i)).totalCompleted) {
                ((SingleOverview) this.activeList.get(i)).totalCompleted = ((SingleOverview) this.activeList.get(i)).compareTotalCompleted;
            }
            ((SingleOverview) this.activeList.get(i)).compareTotalCompleted = 0;
        }
        DbHandler.closeDB();
        this.adapter.refreshRecords(this.activeList);
        if (this.activeList.size() <= 0) {
            this.lblNoRecordFound.setVisibility(View.VISIBLE);
        } else {
            this.lblNoRecordFound.setVisibility(View.GONE);
        }
    }
}
