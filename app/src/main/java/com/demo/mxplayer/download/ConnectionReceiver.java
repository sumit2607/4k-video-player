package com.demo.mxplayer.download;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.demo.mxplayer.App;

public class ConnectionReceiver extends BroadcastReceiver {
    public static final String TAG = "ConnectionReceiver";

    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onConnectionReceive");
        if (isOnline(context)) {
            Log.i(TAG, "connected");
            SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.Shared_Pref_Name, 0);
            if (!sharedPreferences.contains(Constants.Resume_All_On_Connection_Receive)) {
                Editor edit = sharedPreferences.edit();
                edit.putBoolean(Constants.Resume_All_On_Connection_Receive, false);
                edit.commit();
                return;
            } else if (sharedPreferences.getBoolean(Constants.Resume_All_On_Connection_Receive, false)) {
                ((App) context.getApplicationContext()).mHandler.sendEmptyMessage(Constants.Thread_Resume_All);
                return;
            } else {
                return;
            }
        }
        Log.i(TAG, "disconnected");
        ((App) context.getApplicationContext()).mHandler.sendEmptyMessage(Constants.Thread_Pause_All);
    }

    public boolean isOnline(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
