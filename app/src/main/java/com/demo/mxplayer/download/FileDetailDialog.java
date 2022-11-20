package com.demo.mxplayer.download;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;

import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.FileProvider;
import androidx.core.provider.FontsContractCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.demo.mxplayer.App;
import com.demo.mxplayer.R;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.DownloaderThread;
import com.demo.mxplayer.models.SingleFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FileDetailDialog extends AlertDialog implements OnClickListener {
    public static final String TAG = "FileDetailDialog";
    private Context context;
    boolean deleteFiles;
    SingleFile fileDetail;
    DownloaderService mDownloaderService;
    private int mFileID;

    public FileDetailDialog(Context context, DownloaderService downloaderService, int i) {
        super(context);
        this.context = context;
        this.mFileID = i;
        this.mDownloaderService = downloaderService;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.dialog_file_details);
        setView();
    }

    private void setView() {
        StringBuilder stringBuilder;
        try {
            DbHandler.openDB();
            this.fileDetail = DbHandler.getFileDetail(this.mFileID);
        } catch (Exception e) {
            String str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("setView DB Error : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        } catch (Throwable th) {
            DbHandler.closeDB();
        }
        DbHandler.closeDB();
        if (this.fileDetail == null) {
            dismiss();
            return;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM y  K:m a");
        TextView textView = (TextView) findViewById(R.id.lbl_file_detail_path);
        stringBuilder = new StringBuilder();
        stringBuilder.append(this.fileDetail.path);
        stringBuilder.append(File.separator);
        stringBuilder.append(this.fileDetail.title);
        textView.setText(stringBuilder.toString());
        ((TextView) findViewById(R.id.lbl_file_detail_url)).setText(this.fileDetail.url);
        ((TextView) findViewById(R.id.lbl_file_detail_file_title)).setText(this.fileDetail.title);
        if (this.fileDetail.status == 4) {
            ((TextView) findViewById(R.id.lbl_file_detail_datetime)).setText(simpleDateFormat.format(this.fileDetail.completed));
            ((TextView) findViewById(R.id.lbl_file_detail_size)).setText(App.humanReadableByteCount(this.fileDetail.size, false));
            findViewById(R.id.btn_file_detail_open).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_file_detail_cancel).setVisibility(View.GONE);
        } else {
            ((TextView) findViewById(R.id.lbl_file_detail_datetime)).setText(simpleDateFormat.format(this.fileDetail.created));
            if (this.fileDetail.singleThread) {
                ((TextView) findViewById(R.id.lbl_file_detail_size)).setVisibility(View.GONE);
            } else {
                ((TextView) findViewById(R.id.lbl_file_detail_size)).setText(App.humanReadableByteCount(this.fileDetail.size, false));
            }
            findViewById(R.id.btn_file_detail_open).setVisibility(View.GONE);
            findViewById(R.id.btn_file_detail_cancel).setVisibility(View.VISIBLE);
        }
        switch (this.fileDetail.status) {
            case 0:
            case 1:
                ((TextView) findViewById(R.id.lbl_file_detail_status)).setText(this.context.getString(R.string.string_status_created));
                break;
            case 2:
                ((TextView) findViewById(R.id.lbl_file_detail_status)).setText(this.context.getString(R.string.string_status_downloading));
                break;
            case 3:
                ((TextView) findViewById(R.id.lbl_file_detail_status)).setText(this.context.getString(R.string.string_status_paused));
                break;
            case 4:
                ((TextView) findViewById(R.id.lbl_file_detail_status)).setText(this.context.getString(R.string.string_status_completed));
                break;
        }
        findViewById(R.id.btn_file_detail_delete).setOnClickListener(this);
        findViewById(R.id.btn_file_detail_open).setOnClickListener(this);
        findViewById(R.id.btn_file_detail_copy_url).setOnClickListener(this);
        findViewById(R.id.btn_file_detail_cancel).setOnClickListener(this);
    }

    /* Access modifiers changed, original: 0000 */
    public void deleteFile() {
        String str;
        StringBuilder stringBuilder;
        try {
            DbHandler.openDB();
            SingleFile fileDetail = DbHandler.getFileDetail(this.fileDetail.id);
            if (fileDetail != null) {
                ArrayList arrayList = new ArrayList();
                if (fileDetail.status <= 3) {
                    StringBuilder stringBuilder2;
                    int i;
                    String str2;
                    StringBuilder stringBuilder3;
                    File file;
                    int i2 = 0;
                    switch (fileDetail.status) {
                        case 2:
                            arrayList = DbHandler.getCompletedThreadsByFileid(this.mFileID);
                            String str3 = TAG;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("completed threads size : ");
                            stringBuilder2.append(arrayList.size());
                            Log.i(str3, stringBuilder2.toString());
                            for (i = 0; i < this.mDownloaderService.activeThreads.size(); i++) {
                                if (((DownloaderThread) this.mDownloaderService.activeThreads.get(i)).id == this.mFileID) {
                                    arrayList.add(this.mDownloaderService.activeThreads.get(i));
                                }
                            }
                            break;
                        case 3:
                            arrayList = DbHandler.getThreadsByFileid(this.mFileID);
                            break;
                        default:
                            break;
                    }
                    for (i = 0; i < arrayList.size(); i++) {
                        ((DownloaderThread) arrayList.get(i)).running = false;
                        ((DownloaderThread) arrayList.get(i)).showDetailProgress = false;
                        ((DownloaderThread) arrayList.get(i)).showOverviewProgress = false;
                        ((DownloaderThread) arrayList.get(i)).mDetailHandler = null;
                        ((DownloaderThread) arrayList.get(i)).mOverviewHandler = null;
                        str2 = TAG;
                        stringBuilder3 = new StringBuilder();
                        stringBuilder3.append("============= set false thread : ");
                        stringBuilder3.append(((DownloaderThread) arrayList.get(i)).id);
                        stringBuilder3.append(" Part : ");
                        stringBuilder3.append(((DownloaderThread) arrayList.get(i)).partNo);
                        Log.i(str2, stringBuilder3.toString());
                        DbHandler.setThreadPause(this.mFileID, ((DownloaderThread) arrayList.get(i)).partNo, 0);
                        this.mDownloaderService.removeDownloadingThread(((DownloaderThread) arrayList.get(i)).id, ((DownloaderThread) arrayList.get(i)).partNo);
                    }
                    DbHandler.deleteFileThreads(this.mFileID);
                    while (i2 < arrayList.size()) {
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(App.partPath);
                        stringBuilder2.append(File.separator);
                        stringBuilder2.append(this.mFileID);
                        stringBuilder2.append(File.separator);
                        stringBuilder2.append(fileDetail.title);
                        stringBuilder2.append(".");
                        stringBuilder2.append(((DownloaderThread) arrayList.get(i2)).partNo);
                        stringBuilder2.append("_part");
                        file = new File(stringBuilder2.toString());
                        if (file.exists()) {
                            str2 = TAG;
                            stringBuilder3 = new StringBuilder();
                            stringBuilder3.append("Part File : ");
                            stringBuilder3.append(file.getAbsolutePath());
                            stringBuilder3.append(" Deleted");
                            Log.i(str2, stringBuilder3.toString());
                            file.delete();
                        }
                        i2++;
                    }
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append(App.partPath);
                    stringBuilder4.append(File.separator);
                    stringBuilder4.append(this.mFileID);
                    file = new File(stringBuilder4.toString());
                    if (file.exists()) {
                        String str4 = TAG;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("Part Folder : ");
                        stringBuilder2.append(file.getAbsolutePath());
                        stringBuilder2.append(" Deleted");
                        Log.i(str4, stringBuilder2.toString());
                        file.delete();
                    }
                    arrayList.removeAll(arrayList);
                } else if (fileDetail.status == 4 && this.deleteFiles) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Delete File: ");
                    stringBuilder.append(fileDetail.path);
                    stringBuilder.append(File.separator);
                    stringBuilder.append(fileDetail.title);
                    Log.i(str, stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                    stringBuilder.append(fileDetail.path);
                    stringBuilder.append(File.separator);
                    stringBuilder.append(fileDetail.title);
                    File file2 = new File(stringBuilder.toString());
                    if (file2.exists()) {
                        file2.delete();
                    }
                }
                DbHandler.removeFile(fileDetail.id);
                DbHandler.closeDB();
                Intent intent = new Intent(Constants.BROADCAST_FILE_DELETE);
                intent.putExtra(FontsContractCompat.Columns.FILE_ID, fileDetail.id);
                LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            }
        } catch (Exception e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Error in File DELETE : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }

    public void onClick(View view) {
        String str;
        StringBuilder stringBuilder;
        Context context;
        StringBuilder stringBuilder2;
        switch (view.getId()) {
            case R.id.btn_file_detail_cancel /*2131296315*/:
                dismiss();
                break;
            case R.id.btn_file_detail_copy_url /*2131296316*/:
                try {
                    if (VERSION.SDK_INT < 11) {
                        ((ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE)).setText(this.fileDetail.url);
                    } else {
                        ((android.content.ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Copied Text", this.fileDetail.url));
                    }
                    Toast.makeText(this.context, this.context.getText(R.string.string_url_copied), Toast.LENGTH_SHORT).show();
                    break;
                } catch (Exception e) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Copy url Error : ");
                    stringBuilder.append(e.getMessage());
                    Log.e(str, stringBuilder.toString());
                    break;
                }
            case R.id.btn_file_detail_delete /*2131296317*/:
                CharSequence[] charSequenceArr = new CharSequence[]{this.context.getString(R.string.dialog_file_detail_delete_checkbox)};
                Builder builder = new Builder(new ContextThemeWrapper(this.context, (int) R.style.AlertDialogCustom));
                if (this.fileDetail.status == 4) {
                    builder.setTitle(this.context.getString(R.string.dialog_file_detail_completed_title));
                    builder.setMultiChoiceItems(charSequenceArr, null, new OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i, boolean z) {
                            if (z) {
                                FileDetailDialog.this.deleteFiles = true;
                            } else {
                                FileDetailDialog.this.deleteFiles = false;
                            }
                        }
                    });
                } else {
                    builder.setTitle(this.context.getString(R.string.dialog_file_detail_downloading_title));
                    builder.setMessage(this.context.getString(R.string.dialog_file_detail_downloading_details));
                }
                builder.setPositiveButton(this.context.getString(R.string.string_delete), new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        FileDetailDialog.this.deleteFile();
                    }
                }).setNegativeButton(this.context.getString(R.string.string_cancel), new OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.create().show();
                dismiss();
                break;
            case R.id.btn_file_detail_open /*2131296318*/:
                StringBuilder stringBuilder3 = new StringBuilder();
                stringBuilder3.append(this.fileDetail.path);
                stringBuilder3.append(File.separator);
                stringBuilder3.append(this.fileDetail.title);
                if (new File(stringBuilder3.toString()).exists()) {
                    String str2 = "";
                    if (this.fileDetail.title.contains(".")) {
                        str2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.fileDetail.title.substring(this.fileDetail.title.lastIndexOf(".") + 1));
                        if (str2 == null) {
                            str2 = "";
                        }
                    }
                    String str3 = TAG;
                    StringBuilder stringBuilder4 = new StringBuilder();
                    stringBuilder4.append("File mime : ");
                    stringBuilder4.append(str2);
                    Log.i(str3, stringBuilder4.toString());
                    if (str2 == null || !(str2.contains("video") || str2.contains("mp4") || str2.contains("3gp"))) {
                        try {
                            if (VERSION.SDK_INT >= 24) {
                                int i;
                                Log.i(TAG, "android >= N ");
                                if (!this.fileDetail.title.endsWith("apk") || VERSION.SDK_INT < 26 || this.context.getPackageManager().canRequestPackageInstalls()) {
                                    i = 0;
                                } else {
                                    this.context.startActivity(new Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES", Uri.parse("package:com.divineinfosoft.mxplayer")));
                                    i = 1;
                                }
                                if (i == 0) {
                                    Intent intent = new Intent();
                                    intent.setAction("android.intent.action.VIEW");
                                    Context context2 = this.context;
                                    StringBuilder stringBuilder5 = new StringBuilder();
                                    stringBuilder5.append(this.context.getApplicationContext().getPackageName());
                                    stringBuilder5.append(".fileprovider");
                                    String stringBuilder6 = stringBuilder5.toString();
                                    StringBuilder stringBuilder7 = new StringBuilder();
                                    stringBuilder7.append(this.fileDetail.path);
                                    stringBuilder7.append(File.separator);
                                    stringBuilder7.append(this.fileDetail.title);
                                    Uri uriForFile = FileProvider.getUriForFile(context2, stringBuilder6, new File(stringBuilder7.toString()));
                                    stringBuilder6 = TAG;
                                    StringBuilder stringBuilder8 = new StringBuilder();
                                    stringBuilder8.append("File : ");
                                    stringBuilder8.append(this.fileDetail.path);
                                    stringBuilder8.append(File.separator);
                                    stringBuilder8.append(this.fileDetail.title);
                                    Log.i(stringBuilder6, stringBuilder8.toString());
                                    stringBuilder6 = TAG;
                                    stringBuilder8 = new StringBuilder();
                                    stringBuilder8.append("URI : ");
                                    stringBuilder8.append(uriForFile.toString());
                                    Log.i(stringBuilder6, stringBuilder8.toString());
                                    if (str2.length() > 0) {
                                        intent.setDataAndType(uriForFile, str2);
                                        Log.i(TAG, "data with mime");
                                    } else {
                                        intent.setData(uriForFile);
                                    }
                                    intent.addFlags(1);
                                    this.context.startActivity(intent);
                                }
                            } else {
                                Log.i(TAG, "android < N ");
                                Intent intent2 = new Intent();
                                intent2.setAction("android.intent.action.VIEW");
                                stringBuilder4 = new StringBuilder();
                                stringBuilder4.append(this.fileDetail.path);
                                stringBuilder4.append(File.separator);
                                stringBuilder4.append(this.fileDetail.title);
                                Uri fromFile = Uri.fromFile(new File(stringBuilder4.toString()));
                                if (str2.length() > 0) {
                                    intent2.setDataAndType(fromFile, str2);
                                    Log.i(TAG, "data with mime");
                                } else {
                                    intent2.setData(fromFile);
                                }
                                this.context.startActivity(intent2);
                            }
                        } catch (ActivityNotFoundException unused) {
                            context = this.context;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(this.context.getString(R.string.string_no_app_found_to_open));
                            stringBuilder2.append(" : ");
                            stringBuilder2.append(this.fileDetail.path);
                            stringBuilder2.append(File.separator);
                            stringBuilder2.append(this.fileDetail.title);
                            Toast.makeText(context, stringBuilder2.toString(),Toast.LENGTH_SHORT).show();
                            str2 = TAG;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("No Application Found That can Open this file : ");
                            stringBuilder2.append(this.fileDetail.path);
                            stringBuilder2.append(File.separator);
                            stringBuilder2.append(this.fileDetail.title);
                            Log.e(str2, stringBuilder2.toString());
                        } catch (Exception e2) {
                            Context context3 = this.context;
                            stringBuilder3 = new StringBuilder();
                            stringBuilder3.append("Error Opening File : ");
                            stringBuilder3.append(this.fileDetail.path);
                            stringBuilder3.append(File.separator);
                            stringBuilder3.append(this.fileDetail.title);
                            Toast.makeText(context3, stringBuilder3.toString(), Toast.LENGTH_SHORT).show();
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Error Opening File : ");
                            stringBuilder.append(this.fileDetail.path);
                            stringBuilder.append(File.separator);
                            stringBuilder.append(this.fileDetail.title);
                            stringBuilder.append(e2.getMessage());
                            Log.e(str, stringBuilder.toString());
                        }
                    } else {
//                        Editor edit = this.context.getSharedPreferences(Constants.Shared_Pref_Name, 0).edit();
//                        edit.putBoolean(Constants.Video_Played, true);
//                        edit.commit();
//                        Intent intent3 = new Intent(this.context, ViewVidoeActivity.class);
//                        stringBuilder = new StringBuilder();
//                        stringBuilder.append(this.fileDetail.path);
//                        stringBuilder.append(File.separator);
//                        stringBuilder.append(this.fileDetail.title);
//                        intent3.putExtra("filePath", stringBuilder.toString());
//                        intent3.putExtra("fileid", this.fileDetail.id);
//                        this.context.startActivity(intent3);
                    }
                    dismiss();
                    break;
                }
                context = this.context;
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(this.context.getString(R.string.string_file_delete_success));
                stringBuilder2.append(" : ");
                stringBuilder2.append(this.fileDetail.path);
                stringBuilder2.append(File.separator);
                stringBuilder2.append(this.fileDetail.title);
                Toast.makeText(context, stringBuilder2.toString(), Toast.LENGTH_SHORT).show();
                return;
        }
    }
}
