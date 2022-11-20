package com.demo.mxplayer.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

import java.util.ArrayList;

public class SingleOverview {
    public static final String TAG = "SingleOverview";
    public long compareTotalCompleted;
    public int id;
    public Handler listHandlerRef;
    public Handler mHandler;
    public String originalFileName;
    public int position;
    public boolean singleThread;
    public int status;
    public int thread1Completed;
    public int thread2Completed;
    public int thread3Completed;
    public ArrayList<DownloaderThread> threads;
    public long totalByteSize;
    public long totalCompleted;
    public int totalCompletedPercentage;

    public SingleOverview() {
        this.compareTotalCompleted = 0;
        this.mHandler = new Handler(new Callback() {
            public boolean handleMessage(Message message) {
                Bundle data = message.getData();
                long j = data.getLong("downloaded", 0);
                int i = data.getInt("percentage", 0);
                SingleOverview singleOverview = SingleOverview.this;
                singleOverview.totalCompleted += j;
                if (data != null) {
                    switch (message.what) {
                        case 501:
                            SingleOverview.this.thread1Completed = i;
                            break;
                        case 502:
                            SingleOverview.this.thread2Completed = i;
                            break;
                        case 503:
                            SingleOverview.this.thread3Completed = i;
                            break;
                    }
                }
                if (SingleOverview.this.singleThread) {
                    SingleOverview.this.totalCompletedPercentage = SingleOverview.this.thread1Completed;
                } else {
                    SingleOverview.this.totalCompletedPercentage = ((SingleOverview.this.thread1Completed + SingleOverview.this.thread2Completed) + SingleOverview.this.thread3Completed) / 3;
                }
                if (SingleOverview.this.listHandlerRef != null) {
                    Message message2 = new Message();
                    Bundle data2 = message.getData();
                    if (SingleOverview.this.totalByteSize <= 0) {
                        data2.putInt("percentage", 0);
                    } else {
                        data2.putInt("percentage", SingleOverview.this.totalCompletedPercentage);
                    }
                    data2.putLong("downloaded", SingleOverview.this.totalCompleted);
                    message2.what = 500;
                    message2.arg1 = SingleOverview.this.position;
                    message2.setData(data);
                    SingleOverview.this.listHandlerRef.sendMessage(message2);
                }
                return false;
            }
        });
        this.threads = new ArrayList();
    }

    public SingleOverview(int i, String str, int i2) {
        this.compareTotalCompleted = 0;
        this.mHandler = new Handler(/* anonymous class already generated */);
        this.id = i;
        this.originalFileName = str;
        this.position = i2;
        this.threads = new ArrayList();
    }

    public void addThreadRef(DownloaderThread downloaderThread) {
        this.threads.add(downloaderThread);
        switch (downloaderThread.partNo) {
            case 1:
                this.thread1Completed = downloaderThread.completedPercentage;
                break;
            case 2:
                this.thread2Completed = downloaderThread.completedPercentage;
                break;
            case 3:
                this.thread3Completed = downloaderThread.completedPercentage;
                break;
        }
        this.totalCompletedPercentage = ((this.thread1Completed + this.thread2Completed) + this.thread3Completed) / 3;
    }
}
