package com.demo.mxplayer.download;

import android.content.ContextWrapper;
import android.os.Environment;

import com.demo.mxplayer.utils.MyUtils;

public class


Constants {

    public static final int BACKGROUND_SERVICE_NOTIFICATION_ID = 9999999;
    public static final String BROADCAST_BOOKMARK_UPDATED = "BROADCAST_BOOKMARK_UPDATED";
    public static final String BROADCAST_FILE_DELETE = "BROADCAST_FILE_DELETE";
    public static final String BROADCAST_FILE_DOWNLOAD_COMPLETE = "BROADCAST_FILE_DOWNLOAD_COMPLETE";

    public static final String BROADCAST_NEW_DOWNLOAD = "BROADCAST_NEW_DOWNLOAD";

    public static final String FILE_COMPLETE_NOTIFICATION_CHANNEL_ID = "EM_SMD_FILE_COMPLETE_NOTIFICATION_CHANNEL_ID";
    public static final String FILE_COMPLETE_NOTIFICATION_CHANNEL_NAME = "MX Download Manager Download Complete Notifications";
    public static final String FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_ID = "EM_SMD_SERVICE_NOTIFICATION_CHANNEL_ID";
    public static final String FOREGROUND_SERVICE_NOTIFICATION_CHANNEL_NAME = "MX Download Manager Background Service Notification";

    public static final String PREF_LANGUAGE_CODE = "PREF_LANGUAGE_CODE";

    public static final String Resume_All_On_Connection_Receive = "ResumeOnConnectionReceive";

    public static final String Shared_Pref_Name = MyUtils.pref_key;

    public static final int Show_Authentication_View = 1201;
    public static final String Show_Browser_Shortcut = "ShowBrowserShortcut";
    public static final int Show_Download_Start_Toast = 1207;
    public static final int Show_Error_Message = 1203;
    public static final int Show_FileSaveAs_View = 1202;
    public static final int Show_Invalid_Url_Error = 1205;
    public static final int Show_ProgressBar = 1200;
    public static final int Show_Toast_Message = 1204;
    public static final int Show_Youtube_Error = 1206;

    public static final int Thread_Download_Complete = 1050;
    public static final int Thread_Download_Pause = 1060;
    public static final int Thread_Download_Resume = 1070;
    public static final int Thread_Pause_All = 1080;
    public static final int Thread_Remove_Completed = 1091;
    public static final int Thread_Resume_All = 1090;
    public static final String Video_Played = "VideoPlayed";

    public static String partPath = null;
    public static String sdPath = null;


    private static final ContextWrapper cw = null;

    static {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath());

        stringBuilder.append("/MXDownloadManager");
        sdPath = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath());
        stringBuilder.append("/MXDownloadManager/.parts");
        partPath = stringBuilder.toString();
    }
}
