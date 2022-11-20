package com.demo.mxplayer.download;

import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;

import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.demo.mxplayer.App;
import com.demo.mxplayer.BuildConfig;
import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.db.DbHandler;

import net.rdrei.android.dirchooser.DirectoryChooserConfig;
import net.rdrei.android.dirchooser.DirectoryChooserFragment;
import net.rdrei.android.dirchooser.DirectoryChooserFragment.OnFragmentInteractionListener;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

public class DownloadReceiver extends AppCompatActivity implements OnClickListener, OnFragmentInteractionListener {
    public static final String TAG = "DownloadReceiver";
    boolean acceptsRange = false;
    boolean applicationRunning = false;
    boolean authRequired = false;
    Button btnStartDownload;
    long downloadFileSize = -1;
    public DirectoryChooserFragment exportPathDialog;
    String fileName;
    boolean fileNameSet = false;
    HttpURLConnection httpconection;
    TextView lblFileSize;
    EditText lblSavePath;
    EditText lblUrl;
    LinearLayout llAuthInfo;
    LinearLayout llFileSize;
    LinearLayout llSaveAs;
    DownloaderService mDownloaderService;
    public Handler mHandler = new Handler(new Callback() {
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case Constants.Show_ProgressBar /*1200*/:
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.VISIBLE);
                    break;
                case Constants.Show_Authentication_View /*1201*/:
                    DownloadReceiver.this.llAuthInfo.setVisibility(View.VISIBLE);
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.GONE);
                    DownloadReceiver.this.btnStartDownload.setEnabled(true);
                    break;
                case Constants.Show_FileSaveAs_View /*1202*/:
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.GONE);
                    DownloadReceiver.this.btnStartDownload.setEnabled(true);
                    DownloadReceiver.this.btnStartDownload.setText(DownloadReceiver.this.getString(R.string.string_start_download));
                    DownloadReceiver.this.txtSaveFileName.setText(DownloadReceiver.this.fileName);
                    DownloadReceiver.this.llAuthInfo.setVisibility(View.GONE);
                    DownloadReceiver.this.llSaveAs.setVisibility(View.VISIBLE);
                    if (DownloadReceiver.this.fileName.length() <= 0 || !DownloadReceiver.this.fileName.contains(".")) {
                        DownloadReceiver.this.savePath = App.sdPath;
                    } else {
                        DbHandler.openDB();
                        DownloadReceiver.this.savePath = DbHandler.getSavePath(DownloadReceiver.this.fileName.substring(DownloadReceiver.this.fileName.lastIndexOf(".")));
                        DbHandler.closeDB();
                    }
                    DownloadReceiver.this.lblSavePath.setText(DownloadReceiver.this.savePath);
                    DownloadReceiver.this.lblSavePath.setSelection(DownloadReceiver.this.savePath.length());
                    if (DownloadReceiver.this.downloadFileSize > 0) {
                        DownloadReceiver.this.lblFileSize.setText(App.humanReadableByteCount(DownloadReceiver.this.downloadFileSize, false));
                        DownloadReceiver.this.llFileSize.setVisibility(View.VISIBLE);
                    } else {
                        DownloadReceiver.this.llFileSize.setVisibility(View.GONE);
                    }
                    DownloadReceiver.this.fileNameSet = true;
                    break;
                case Constants.Show_Error_Message /*1203*/:
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.GONE);
                    DownloadReceiver downloadReceiver = DownloadReceiver.this;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(DownloadReceiver.this.getString(R.string.string_connection_error));
                    stringBuilder.append(DownloadReceiver.this.responseCode);
                    Toast.makeText(downloadReceiver, stringBuilder.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case Constants.Show_Toast_Message /*1204*/:
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.GONE);
                    String string = DownloadReceiver.this.getString(R.string.string_connection_error);
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(string);
                    stringBuilder2.append((String) message.obj);
                    Toast.makeText(DownloadReceiver.this, stringBuilder2.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case Constants.Show_Invalid_Url_Error /*1205*/:
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.GONE);
                    DownloadReceiver.this.lblUrl.setError(DownloadReceiver.this.getString(R.string.string_homepage_invalid_url_warning));
                    DownloadReceiver.this.lblUrl.requestFocus();
                    break;
                case Constants.Show_Youtube_Error /*1206*/:
                    Builder builder = new Builder(DownloadReceiver.this);
                    builder.setTitle(DownloadReceiver.this.getString(R.string.dialog_youtube_not_allowed_title));
                    builder.setMessage(DownloadReceiver.this.getString(R.string.dialog_youtube_not_allowed_details));
                    builder.setNegativeButton(DownloadReceiver.this.getString(R.string.string_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                    DownloadReceiver.this.progressGetInfo.setVisibility(View.GONE);
                    DownloadReceiver.this.btnStartDownload.setEnabled(true);
                    break;
                case Constants.Show_Download_Start_Toast /*1207*/:
                    Toast.makeText(DownloadReceiver.this, (String) message.obj, Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DownloaderService.DownloaderServiceBinder downloaderServiceBinder = (DownloaderService.DownloaderServiceBinder) iBinder;
            DownloadReceiver.this.mDownloaderService = downloaderServiceBinder.getService();
            DownloadReceiver.this.serviceConnected = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            DownloadReceiver.this.serviceConnected = false;
            DownloadReceiver.this.mDownloaderService = null;
        }
    };
    String password = "";
    View progressGetInfo;
    int responseCode;
    private String savePath;
    boolean serviceConnected = false;
    SharedPreferences sp;
    EditText txtPassword;
    EditText txtSaveFileName;
    EditText txtUserName;
    public String url;
    String userName = "";

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setupLanguage();
        setContentView((int) R.layout.activity_download_receiver);
        setFinishOnTouchOutside(false);
        DbHandler dBConnector = new DbHandler(this);
        this.sp = getSharedPreferences(Constants.Shared_Pref_Name, 0);
        this.url = getIntent().getDataString();
        setView();
    }

    private void setView() {
        this.btnStartDownload = (Button) findViewById(R.id.btn_start_download);
        this.llAuthInfo = (LinearLayout) findViewById(R.id.ll_auth_wrapper);
        this.llSaveAs = (LinearLayout) findViewById(R.id.ll_filesaveas_wrapper);
        this.llFileSize = (LinearLayout) findViewById(R.id.ll_file_size_wrapper);
        this.txtUserName = (EditText) findViewById(R.id.txt_auth_username);
        this.txtPassword = (EditText) findViewById(R.id.txt_auth_password);
        this.txtSaveFileName = (EditText) findViewById(R.id.txt_save_filename);
        this.progressGetInfo = findViewById(R.id.progress_get_info);
        this.lblFileSize = (TextView) findViewById(R.id.lbl_download_file_size);
        this.btnStartDownload.setOnClickListener(this);
        findViewById(R.id.btn_cancel_download).setOnClickListener(this);
        findViewById(R.id.btn_select_save_path).setOnClickListener(this);
        findViewById(R.id.btn_url_paste).setOnClickListener(this);
        this.lblUrl = (EditText) findViewById(R.id.lbl_download_url);
        this.lblSavePath = (EditText) findViewById(R.id.lbl_file_save_path);
        this.lblSavePath.setInputType(0);
        this.lblUrl.setText(this.url);
        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                .newDirectoryName("New Folder")
                .allowNewDirectoryNameModification(true)
                .allowReadOnlyDirectory(true)

                .build();
        this.exportPathDialog = DirectoryChooserFragment.newInstance(config);
        if (this.url != null && this.url.length() > 0) {
            performDownloadClick();
        }
    }

    public void performDownloadClick() {
        if (isOnline(this)) {
            new Thread() {
                public void run() {
                    if (DownloadReceiver.this.fileNameSet) {
                        Log.i(DownloadReceiver.TAG, "fileNameSet start download");
                        DownloadReceiver.this.fileName = DownloadReceiver.this.txtSaveFileName.getText().toString();
                        DownloadReceiver.this.startDownload();
                    } else if (DownloadReceiver.this.authRequired) {
                        DownloadReceiver.this.mHandler.sendEmptyMessage(Constants.Show_ProgressBar);
                        Log.i(DownloadReceiver.TAG, "authRequired");
                        DownloadReceiver.this.userName = DownloadReceiver.this.txtUserName.getText().toString();
                        DownloadReceiver.this.password = DownloadReceiver.this.txtPassword.getText().toString();
                        DownloadReceiver.this.sendAuthInfoRequest();
                    } else {
                        DownloadReceiver.this.mHandler.sendEmptyMessage(Constants.Show_ProgressBar);
                        DownloadReceiver.this.url = DownloadReceiver.this.lblUrl.getText().toString().trim();
                        if (!Patterns.WEB_URL.matcher(DownloadReceiver.this.url).matches()) {
                            DownloadReceiver.this.mHandler.sendEmptyMessage(Constants.Show_Invalid_Url_Error);
                        } else if (DownloadReceiver.this.sendInfoRequest()) {
                            Log.i(DownloadReceiver.TAG, "authRequired");
                            DownloadReceiver.this.mHandler.sendEmptyMessage(Constants.Show_Authentication_View);
                        } else if (DownloadReceiver.this.responseCode < HttpStatus.SC_BAD_REQUEST) {
                            DownloadReceiver.this.mHandler.sendEmptyMessage(Constants.Show_FileSaveAs_View);
                        }
                    }
                }
            }.start();
        } else {
            Toast.makeText(this, getString(R.string.string_no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_cancel_download) {
            finish();
        } else if (id == R.id.btn_select_save_path) {
            this.exportPathDialog.show(getFragmentManager(), null);
        } else if (id == R.id.btn_start_download) {
            performDownloadClick();
        } else if (id == R.id.btn_url_paste) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboardManager.hasPrimaryClip()) {
                ClipDescription primaryClipDescription = clipboardManager.getPrimaryClipDescription();
                ClipData primaryClip = clipboardManager.getPrimaryClip();
                if (primaryClip != null && primaryClipDescription != null && primaryClipDescription.hasMimeType(StringPart.DEFAULT_CONTENT_TYPE)) {
                    this.lblUrl.setText(String.valueOf(primaryClip.getItemAt(0).getText()));
                }
            }
        }
    }

    public void sendAuthInfoRequest() {
        Message message;
        this.responseCode = 1000;
        String stringBuilder;
        StringBuilder stringBuilder2;
        try {
            URI uri = new URI(this.url.replaceAll("\\[", "").replaceAll("\\]", ""));
            URL url = new URL(this.url);
            this.httpconection.disconnect();
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Basic ");
            StringBuilder stringBuilder4 = new StringBuilder();
            stringBuilder4.append(this.userName);
            stringBuilder4.append(":");
            stringBuilder4.append(this.password);
            stringBuilder3.append(new String(Base64.encode(stringBuilder4.toString().getBytes(), 2)));
            stringBuilder = stringBuilder3.toString();
            this.httpconection = (HttpURLConnection) url.openConnection();
            this.httpconection.setRequestProperty("Authorization", stringBuilder);
            this.httpconection.setInstanceFollowRedirects(true);
            HttpURLConnection.setFollowRedirects(true);
            this.httpconection.connect();
            this.responseCode = this.httpconection.getResponseCode();
            String str = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Response Code : ");
            stringBuilder2.append(this.responseCode);
            Log.i(str, stringBuilder2.toString());
            int i = 0;
            if (this.responseCode == HttpStatus.SC_UNAUTHORIZED) {
                this.llAuthInfo.setVisibility(View.VISIBLE);
            } else if (this.responseCode < HttpStatus.SC_BAD_REQUEST) {
                if (this.responseCode == HttpStatus.SC_MOVED_TEMPORARILY || this.responseCode == HttpStatus.SC_MOVED_PERMANENTLY || this.responseCode == HttpStatus.SC_SEE_OTHER) {
                    this.url = this.httpconection.getHeaderField("Location");
                    url = new URL(this.url);
                    this.httpconection.disconnect();
                    stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Basic ");
                    StringBuilder stringBuilder5 = new StringBuilder();
                    stringBuilder5.append(this.userName);
                    stringBuilder5.append(":");
                    stringBuilder5.append(this.password);
                    stringBuilder2.append(new String(Base64.encode(stringBuilder5.toString().getBytes(), 2)));
                    String stringBuilder6 = stringBuilder2.toString();
                    this.httpconection = (HttpURLConnection) url.openConnection();
                    this.httpconection.setRequestProperty("Authorization", stringBuilder6);
                    this.httpconection.setInstanceFollowRedirects(true);
                    HttpURLConnection.setFollowRedirects(true);
                    this.httpconection.connect();
                    this.responseCode = this.httpconection.getResponseCode();
                }
                this.progressGetInfo.setVisibility(View.GONE);
                this.btnStartDownload.setEnabled(true);
                if (this.httpconection.getHeaderField("Accept-Ranges") != null) {
                    if (this.httpconection.getHeaderField("Accept-Ranges").equalsIgnoreCase("bytes")) {
                        this.acceptsRange = true;
                    } else {
                        this.acceptsRange = false;
                    }
                    this.acceptsRange = true;
                } else {
                    this.acceptsRange = false;
                }
                if (this.httpconection.getHeaderField("Content-Disposition") != null) {
                    str = this.httpconection.getHeaderField("Content-Disposition");
                    if (!(str == null || str.indexOf("=") == -1)) {
                        this.fileName = str.split("=")[1];
                    }
                }
                if (this.fileName == null || this.fileName.equals("")) {
                    this.fileName = this.url.substring(this.url.lastIndexOf(File.separator) + 1);
                    this.fileName = URLDecoder.decode(this.fileName);
                    if (this.fileName.contains("?")) {
                        this.fileName = this.fileName.substring(0, this.fileName.indexOf("?"));
                    }
                }
                if (getIntent().hasExtra("FILE_NAME")) {
                    StringBuilder stringBuilder7 = new StringBuilder();
                    stringBuilder7.append(getIntent().getStringExtra("FILE_NAME"));
                    stringBuilder7.append(this.fileName.substring(this.fileName.lastIndexOf(".")));
                    this.fileName = stringBuilder7.toString();
                }
                String[] strArr = new String[]{"|", "\\", "?", "*", "<", "\"", ":", ">"};
                int length = strArr.length;
                while (i < length) {
                    this.fileName = this.fileName.replace(strArr[i], "");
                    i++;
                }
                str = TAG;
                stringBuilder3 = new StringBuilder();
                stringBuilder3.append("***File Name : ");
                stringBuilder3.append(this.fileName);
                Log.i(str, stringBuilder3.toString());
                this.mHandler.sendEmptyMessage(Constants.Show_FileSaveAs_View);
            } else {
                this.mHandler.sendEmptyMessage(Constants.Show_Error_Message);
            }
        } catch (URISyntaxException e) {
            stringBuilder = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("URISyntaxException : ");
            stringBuilder2.append(e.getMessage());
            Log.e(stringBuilder, stringBuilder2.toString());
            message = new Message();
            message.what = Constants.Show_Toast_Message;
            message.obj = e.getLocalizedMessage();
            this.mHandler.sendMessage(message);
        } catch (MalformedURLException e2) {
            stringBuilder = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("MalformedURLException : ");
            stringBuilder2.append(e2.getMessage());
            Log.e(stringBuilder, stringBuilder2.toString());
            message = new Message();
            message.what = Constants.Show_Toast_Message;
            message.obj = e2.getMessage();
            this.mHandler.sendMessage(message);
        } catch (IOException e3) {
            stringBuilder = TAG;
            stringBuilder2 = new StringBuilder();
            stringBuilder2.append("IOException : ");
            stringBuilder2.append(e3.getMessage());
            Log.e(stringBuilder, stringBuilder2.toString());
            message = new Message();
            message.what = Constants.Show_Toast_Message;
            message.obj = e3.getMessage();
            this.mHandler.sendMessage(message);
        }
    }

    public boolean sendInfoRequest() {
        Message message;
        this.responseCode = 1000;
        String str;
        StringBuilder stringBuilder;
        try {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("sendInfoRequest : ");
            stringBuilder.append(this.url);
            Log.i(str, stringBuilder.toString());
            if (this.url == null || this.url.length() <= 0) {
                this.mHandler.sendEmptyMessage(Constants.Show_Invalid_Url_Error);
                return this.authRequired;
            }
            StringBuilder stringBuilder2;
            if (!(this.url.startsWith("http://") || this.url.startsWith("ftp://") || this.url.startsWith("https://"))) {
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append("http://");
                stringBuilder2.append(this.url);
                this.url = stringBuilder2.toString();
            }
            URI uri = new URI(this.url.replaceAll("\\[", "").replaceAll("\\]", ""));
            Log.i(TAG, "URL checked");
            if (!(this.url.startsWith("http://www.youtube") || this.url.startsWith("http://youtube") || this.url.startsWith("https://www.youtube") || this.url.startsWith("https://youtube") || this.url.startsWith("http://www.m.youtube") || this.url.startsWith("http://m.youtube") || this.url.startsWith("https://www.m.youtube"))) {
                if (!this.url.startsWith("https://m.youtube")) {
                    URL url = new URL(this.url);
                    Log.i(TAG, "Download url created");
                    this.httpconection = (HttpURLConnection) url.openConnection();
                    int i = 1;
                    this.httpconection.setInstanceFollowRedirects(true);
                    HttpURLConnection.setFollowRedirects(true);
                    this.responseCode = this.httpconection.getResponseCode();
                    str = TAG;
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("Response Code : ");
                    stringBuilder3.append(this.responseCode);
                    Log.i(str, stringBuilder3.toString());
                    if (this.responseCode == HttpStatus.SC_UNAUTHORIZED) {
                        this.authRequired = true;
                    } else if (this.responseCode < HttpStatus.SC_BAD_REQUEST) {
                        if (this.responseCode == HttpStatus.SC_MOVED_TEMPORARILY || this.responseCode == HttpStatus.SC_MOVED_PERMANENTLY || this.responseCode == HttpStatus.SC_SEE_OTHER) {
                            this.url = this.httpconection.getHeaderField("Location");
                            url = new URL(this.url);
                            this.httpconection.disconnect();
                            this.httpconection = (HttpURLConnection) url.openConnection();
                            this.httpconection.setInstanceFollowRedirects(true);
                            HttpURLConnection.setFollowRedirects(true);
                            this.httpconection.connect();
                            this.responseCode = this.httpconection.getResponseCode();
                        }
                        if (this.httpconection.getHeaderField("Accept-Ranges") != null) {
                            str = this.httpconection.getHeaderField("Accept-Ranges");
                            if (str == null || str.length() <= 0 || !str.equalsIgnoreCase("bytes")) {
                                this.acceptsRange = false;
                            } else {
                                this.acceptsRange = true;
                            }
                        } else {
                            this.acceptsRange = false;
                        }
                        this.downloadFileSize = (long) this.httpconection.getContentLength();
                        if (this.httpconection.getHeaderField("Content-Disposition") != null) {
                            str = this.httpconection.getHeaderField("Content-Disposition");
                            if (str != null && str.contains("=") && str.split("=").length >= 2) {
                                this.fileName = str.split("=")[1];
                            }
                        }
                        if (this.fileName == null || this.fileName.equals("")) {
                            this.fileName = this.url.substring(this.url.lastIndexOf(File.separator) + 1);
                            this.fileName = URLDecoder.decode(this.fileName);
                            if (this.fileName.contains("?")) {
                                this.fileName = this.fileName.substring(0, this.fileName.indexOf("?"));
                            }
                        }
                        if (getIntent().hasExtra("FILE_NAME")) {
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(getIntent().getStringExtra("FILE_NAME"));
                            stringBuilder2.append(this.fileName.substring(this.fileName.lastIndexOf(".")));
                            this.fileName = stringBuilder2.toString();
                        }
                        for (String str2 : new String[]{"|", "\\", "?", "*", "<", "\"", ":", ">"}) {
                            String str3 = TAG;
                            StringBuilder stringBuilder4 = new StringBuilder();
                            stringBuilder4.append("replace character : ");
                            stringBuilder4.append(str2);
                            Log.i(str3, stringBuilder4.toString());
                            this.fileName = this.fileName.replace(str2, "");
                        }
                        StringBuilder stringBuilder5;
                        if (this.fileName.length() <= 0 || !this.fileName.contains(".")) {
                            this.fileName = "new-file";
                            this.savePath = App.sdPath;
                            stringBuilder3 = new StringBuilder();
                            stringBuilder3.append(this.savePath);
                            stringBuilder3.append(File.separator);
                            stringBuilder3.append(this.fileName);
                            if (new File(stringBuilder3.toString()).exists()) {
                                str = this.fileName;
                                while (i <= 1000) {
                                    StringBuilder stringBuilder6 = new StringBuilder();
                                    stringBuilder6.append(this.savePath);
                                    stringBuilder6.append(File.separator);
                                    stringBuilder6.append(str);
                                    stringBuilder6.append("-");
                                    stringBuilder6.append(i);
                                    if (!new File(stringBuilder6.toString()).exists()) {
                                        stringBuilder5 = new StringBuilder();
                                        stringBuilder5.append(str);
                                        stringBuilder5.append("-");
                                        stringBuilder5.append(i);
                                        str = stringBuilder5.toString();
                                        break;
                                    }
                                    i++;
                                }
                                this.fileName = str;
                            }
                        } else {
                            str = this.fileName.substring(this.fileName.lastIndexOf("."));
                            DbHandler.openDB();
                            if (this.savePath == null || this.savePath.length() <= 0) {
                                this.savePath = DbHandler.getSavePath(str);
                            }
                            DbHandler.closeDB();
                            StringBuilder stringBuilder7 = new StringBuilder();
                            stringBuilder7.append(this.savePath);
                            stringBuilder7.append(File.separator);
                            stringBuilder7.append(this.fileName);
                            if (new File(stringBuilder7.toString()).exists()) {
                                String substring = this.fileName.substring(0, this.fileName.lastIndexOf(46));
                                while (i <= 1000) {
                                    stringBuilder7 = new StringBuilder();
                                    stringBuilder7.append(this.savePath);
                                    stringBuilder7.append(File.separator);
                                    stringBuilder7.append(substring);
                                    stringBuilder7.append("-");
                                    stringBuilder7.append(i);
                                    stringBuilder7.append(str);
                                    if (!new File(stringBuilder7.toString()).exists()) {
                                        stringBuilder5 = new StringBuilder();
                                        stringBuilder5.append(substring);
                                        stringBuilder5.append("-");
                                        stringBuilder5.append(i);
                                        substring = stringBuilder5.toString();
                                        break;
                                    }
                                    i++;
                                }
                                stringBuilder5 = new StringBuilder();
                                stringBuilder5.append(substring);
                                stringBuilder5.append(str);
                                this.fileName = stringBuilder5.toString();
                            }
                        }
                        String str4 = TAG;
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("File Name : ");
                        stringBuilder2.append(this.fileName);
                        Log.i(str4, stringBuilder2.toString());
                        this.mHandler.sendEmptyMessage(Constants.Show_FileSaveAs_View);
                    } else {
                        this.mHandler.sendEmptyMessage(Constants.Show_Error_Message);
                    }
                    return this.authRequired;
                }
            }
            this.mHandler.sendEmptyMessage(Constants.Show_Youtube_Error);
            return this.authRequired;
        } catch (URISyntaxException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("URISyntaxException : ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            message = new Message();
            message.what = Constants.Show_Toast_Message;
            message.obj = e.getMessage();
            this.mHandler.sendMessage(message);
        } catch (MalformedURLException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("MalformedURLException : ");
            stringBuilder.append(e2.getMessage());
            Log.e(str, stringBuilder.toString());
            message = new Message();
            message.what = Constants.Show_Toast_Message;
            message.obj = e2.getMessage();
            this.mHandler.sendMessage(message);
        } catch (IOException e3) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("IOException : ");
            stringBuilder.append(e3.getMessage());
            Log.e(str, stringBuilder.toString());
            message = new Message();
            message.what = Constants.Show_Toast_Message;
            message.obj = e3.getMessage();
            this.mHandler.sendMessage(message);
        }
        return this.authRequired;
    }

    public void startTestDownload() {
        new TestFileDownloader(this, 5, this.url, Constants.sdPath, this.fileName).execute(new String[0]);
    }

    public void startDownload() {
        new Thread() {
            public void run() {
                StringBuilder stringBuilder;
                try {
                    if (DownloadReceiver.this.serviceConnected) {
                        StringBuilder stringBuilder2;
                        StringBuilder stringBuilder3;
                        int contentLength;
                        boolean z;
                        DownloaderService downloaderService;
                        String str;
                        StringBuilder stringBuilder4;
                        Message message;
                        Intent intent;
                        long j;
                        DownloaderService downloaderService2;
                        String str2;
                        StringBuilder stringBuilder5;
                        long j2;
                        long j3;
                        StringBuilder stringBuilder6;
                        DownloadReceiver.this.downloadFileSize = (long) DownloadReceiver.this.httpconection.getContentLength();
                        DownloadReceiver.this.fileName = DownloadReceiver.this.txtSaveFileName.getText().toString();
                        String[] strArr = new String[]{"|", "\\", "?", "*", "<", "\"", ":", ">"};
                        int length = strArr.length;
                        for (int i = 0; i < length; i++) {
                            CharSequence charSequence = strArr[i];
                            String str3 = TAG;
                            StringBuilder stringBuilder7 = new StringBuilder();
                            stringBuilder7.append("replace character : ");
                            stringBuilder7.append(charSequence);
                            Log.i(str3, stringBuilder7.toString());
                            DownloadReceiver.this.fileName = DownloadReceiver.this.fileName.replace(charSequence, BuildConfig.VERSION_NAME);
                        }
                        DbHandler.openDB();
                        String substring = DownloadReceiver.this.fileName.substring(DownloadReceiver.this.fileName.lastIndexOf("."));
                        if (DownloadReceiver.this.savePath == null && DownloadReceiver.this.savePath.length() <= 0) {
                            DownloadReceiver.this.savePath = DbHandler.getSavePath(substring);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(DownloadReceiver.this.savePath);
                        stringBuilder.append(File.separator);
                        stringBuilder.append(DownloadReceiver.this.fileName);
                        if (new File(stringBuilder.toString()).exists()) {
                            String substring2 = DownloadReceiver.this.fileName.substring(0, DownloadReceiver.this.fileName.lastIndexOf(R.styleable.AppCompatTheme_checkboxStyle));
                            for (length = 1; length <= 1000; length++) {
                                stringBuilder2 = new StringBuilder();
                                stringBuilder2.append(DownloadReceiver.this.savePath);
                                stringBuilder2.append(File.separator);
                                stringBuilder2.append(substring2);
                                stringBuilder2.append("-");
                                stringBuilder2.append(length);
                                stringBuilder2.append(substring);
                                if (!new File(stringBuilder2.toString()).exists()) {
                                    stringBuilder3 = new StringBuilder();
                                    stringBuilder3.append(substring2);
                                    stringBuilder3.append("-");
                                    stringBuilder3.append(length);
                                    substring2 = stringBuilder3.toString();
                                    break;
                                }
                            }
                            DownloadReceiver downloadReceiver = DownloadReceiver.this;
                            stringBuilder3 = new StringBuilder();
                            stringBuilder3.append(substring2);
                            stringBuilder3.append(substring);
                            downloadReceiver.fileName = stringBuilder3.toString();
                        }
                        DownloadReceiver.this.httpconection.disconnect();
                        if (!DownloadReceiver.this.acceptsRange) {
                            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(DownloadReceiver.this.url).openConnection();
                            httpURLConnection.setRequestMethod("HEAD");
                            httpURLConnection.setRequestProperty("Range", "bytes=0-0");
                            length = httpURLConnection.getResponseCode();
                            contentLength = httpURLConnection.getContentLength();
                            String str4 = TAG;
                            stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("single check file size : ");
                            stringBuilder2.append(App.humanReadableByteCount((long) contentLength, false));
                            Log.i(str4, stringBuilder2.toString());
                            if (length != 206 || contentLength != 1) {
                                z = false;
                                if (z) {
                                    contentLength = DbHandler.insertFile(DownloadReceiver.this.url, DownloadReceiver.this.fileName, DownloadReceiver.this.savePath, DownloadReceiver.this.downloadFileSize, substring, true, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append(Constants.partPath);
                                    stringBuilder.append(File.separator);
                                    stringBuilder.append(contentLength);
                                    new File(stringBuilder.toString()).mkdirs();
                                    downloaderService = DownloadReceiver.this.mDownloaderService;
                                    str = DownloadReceiver.this.url;
                                    stringBuilder4 = new StringBuilder();
                                    stringBuilder4.append(Constants.partPath);
                                    stringBuilder4.append(File.separator);
                                    stringBuilder4.append(contentLength);
                                    stringBuilder4.append(File.separator);
                                    stringBuilder4.append(DownloadReceiver.this.fileName);
                                    downloaderService.addNewDownloadingThread(contentLength, 1, str, stringBuilder4.toString(), DownloadReceiver.this.fileName, 0, -1, 0, DownloadReceiver.this.downloadFileSize, true, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                                    DbHandler.setThreadRunning(contentLength, 1);
                                    DbHandler.setFileRunning(contentLength);
                                    DbHandler.closeDB();
                                    if (DownloadReceiver.this.getIntent().hasExtra("FROM_APP")) {
                                        DownloadReceiver.this.applicationRunning = DownloadReceiver.this.getIntent().getBooleanExtra("FROM_APP", false);
                                    }
                                    message = new Message();
                                    message.what = 1207;
                                    stringBuilder = new StringBuilder();
                                    stringBuilder.append(DownloadReceiver.this.getString(R.string.drawer_menu_downloading));
                                    stringBuilder.append(" : ");
                                    stringBuilder.append(DownloadReceiver.this.fileName);
                                    message.obj = stringBuilder.toString();
                                    DownloadReceiver.this.mHandler.sendMessage(message);
                                    if (DownloadReceiver.this.applicationRunning) {
                                        intent = new Intent(DownloadReceiver.this, MainActivity.class);
                                        intent.putExtra("showDetails", true);
                                        intent.putExtra("fileID", contentLength);
                                        intent.putExtra("status", 3);
                                        intent.putExtra("fileName", DownloadReceiver.this.fileName);
                                        intent.putExtra("singleThread", true);
                                        DownloadReceiver.this.startActivity(intent);
                                    } else {
                                        intent = new Intent(Constants.BROADCAST_NEW_DOWNLOAD);
                                        intent.putExtra("showDetails", true);
                                        intent.putExtra("fileID", contentLength);
                                        intent.putExtra("status",3);
                                        intent.putExtra("fileName", DownloadReceiver.this.fileName);
                                        intent.putExtra("singleThread", true);
                                        LocalBroadcastManager.getInstance(DownloadReceiver.this).sendBroadcast(intent);
                                    }
                                    DownloadReceiver.this.finish();
                                    return;
                                }
                                j = DownloadReceiver.this.downloadFileSize / 3;
                                contentLength = DbHandler.insertFile(DownloadReceiver.this.url, DownloadReceiver.this.fileName, DownloadReceiver.this.savePath, DownloadReceiver.this.downloadFileSize, substring, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                                stringBuilder2 = new StringBuilder();
                                stringBuilder2.append(Constants.partPath);
                                stringBuilder2.append(File.separator);
                                stringBuilder2.append(contentLength);
                                new File(stringBuilder2.toString()).mkdirs();
                                downloaderService2 = DownloadReceiver.this.mDownloaderService;
                                str2 = DownloadReceiver.this.url;
                                stringBuilder5 = new StringBuilder();
                                stringBuilder5.append(Constants.partPath);
                                stringBuilder5.append(File.separator);
                                stringBuilder5.append(contentLength);
                                stringBuilder5.append(File.separator);
                                stringBuilder5.append(DownloadReceiver.this.fileName);
                                stringBuilder5.append(".1_part");
                                downloaderService2.addNewDownloadingThread(contentLength, 1, str2, stringBuilder5.toString(), DownloadReceiver.this.fileName, 0, j, 0, DownloadReceiver.this.downloadFileSize, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                                stringBuilder3 = new StringBuilder();
                                stringBuilder3.append(Constants.partPath);
                                stringBuilder3.append(File.separator);
                                stringBuilder3.append(contentLength);
                                stringBuilder3.append(File.separator);
                                stringBuilder3.append(DownloadReceiver.this.fileName);
                                stringBuilder3.append(".1_part");
                                DbHandler.insertThreads(contentLength, 1, stringBuilder3.toString(), DownloadReceiver.this.fileName, 0, j, 0);
                                downloaderService2 = DownloadReceiver.this.mDownloaderService;
                                str2 = DownloadReceiver.this.url;
                                stringBuilder5 = new StringBuilder();
                                stringBuilder5.append(Constants.partPath);
                                stringBuilder5.append(File.separator);
                                stringBuilder5.append(contentLength);
                                stringBuilder5.append(File.separator);
                                stringBuilder5.append(DownloadReceiver.this.fileName);
                                stringBuilder5.append(".2_part");
                                j2 = j + 1;
                                j3 = 2 * j;
                                downloaderService2.addNewDownloadingThread(contentLength, 3, str2, stringBuilder5.toString(), DownloadReceiver.this.fileName, j2, j3, 0, DownloadReceiver.this.downloadFileSize, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                                stringBuilder6 = new StringBuilder();
                                stringBuilder6.append(Constants.partPath);
                                stringBuilder6.append(File.separator);
                                stringBuilder6.append(contentLength);
                                stringBuilder6.append(File.separator);
                                stringBuilder6.append(DownloadReceiver.this.fileName);
                                stringBuilder6.append(".2_part");
                                DbHandler.insertThreads(contentLength, 3, stringBuilder6.toString(), DownloadReceiver.this.fileName, j2, j3, 0);
                                downloaderService = DownloadReceiver.this.mDownloaderService;
                                str = DownloadReceiver.this.url;
                                stringBuilder4 = new StringBuilder();
                                stringBuilder4.append(Constants.partPath);
                                stringBuilder4.append(File.separator);
                                stringBuilder4.append(contentLength);
                                stringBuilder4.append(File.separator);
                                stringBuilder4.append(DownloadReceiver.this.fileName);
                                stringBuilder4.append(".3_part");
                                j2 = j3 + 1;
                                downloaderService.addNewDownloadingThread(contentLength, 2, str, stringBuilder4.toString(), DownloadReceiver.this.fileName, j2, DownloadReceiver.this.downloadFileSize, 0, DownloadReceiver.this.downloadFileSize, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                                stringBuilder6 = new StringBuilder();
                                stringBuilder6.append(Constants.partPath);
                                stringBuilder6.append(File.separator);
                                stringBuilder6.append(contentLength);
                                stringBuilder6.append(File.separator);
                                stringBuilder6.append(DownloadReceiver.this.fileName);
                                stringBuilder6.append(".3_part");
                                DbHandler.insertThreads(contentLength, 2, stringBuilder6.toString(), DownloadReceiver.this.fileName, j2, DownloadReceiver.this.downloadFileSize, 0);
                                DbHandler.setThreadRunning(contentLength, 1);
                                DbHandler.setThreadRunning(contentLength,3);
                                DbHandler.setThreadRunning(contentLength, 2);
                                DbHandler.setFileRunning(contentLength);
                                DbHandler.closeDB();
                                DownloadReceiver.this.applicationRunning = false;
                                if (DownloadReceiver.this.getIntent().hasExtra("FROM_APP")) {
                                    DownloadReceiver.this.applicationRunning = DownloadReceiver.this.getIntent().getBooleanExtra("FROM_APP", false);
                                }
                                message = new Message();
                                message.what = 1207;
                                stringBuilder = new StringBuilder();
                                stringBuilder.append(DownloadReceiver.this.getString(R.string.drawer_menu_downloading));
                                stringBuilder.append(" : ");
                                stringBuilder.append(DownloadReceiver.this.fileName);
                                message.obj = stringBuilder.toString();
                                DownloadReceiver.this.mHandler.sendMessage(message);
                                if (DownloadReceiver.this.applicationRunning) {
                                    intent = new Intent(DownloadReceiver.this, MainActivity.class);
                                    intent.putExtra("showDetails", true);
                                    intent.putExtra("fileID", contentLength);
                                    intent.putExtra("status",3);
                                    intent.putExtra("fileName", DownloadReceiver.this.fileName);
                                    intent.putExtra("singleThread", false);
                                    DownloadReceiver.this.startActivity(intent);
                                } else {
                                    intent = new Intent(Constants.BROADCAST_NEW_DOWNLOAD);
                                    intent.putExtra("showDetails", true);
                                    intent.putExtra("fileID", contentLength);
                                    intent.putExtra("status",3);
                                    intent.putExtra("fileName", DownloadReceiver.this.fileName);
                                    intent.putExtra("singleThread", false);
                                    LocalBroadcastManager.getInstance(DownloadReceiver.this).sendBroadcast(intent);
                                }
                                DownloadReceiver.this.finish();
                                return;
                            }
                        }
                        z = true;
                        if (z) {
                            contentLength = DbHandler.insertFile(DownloadReceiver.this.url, DownloadReceiver.this.fileName, DownloadReceiver.this.savePath, DownloadReceiver.this.downloadFileSize, substring, true, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(Constants.partPath);
                            stringBuilder.append(File.separator);
                            stringBuilder.append(contentLength);
                            new File(stringBuilder.toString()).mkdirs();
                            downloaderService = DownloadReceiver.this.mDownloaderService;
                            str = DownloadReceiver.this.url;
                            stringBuilder4 = new StringBuilder();
                            stringBuilder4.append(Constants.partPath);
                            stringBuilder4.append(File.separator);
                            stringBuilder4.append(contentLength);
                            stringBuilder4.append(File.separator);
                            stringBuilder4.append(DownloadReceiver.this.fileName);
                            downloaderService.addNewDownloadingThread(contentLength, 1, str, stringBuilder4.toString(), DownloadReceiver.this.fileName, 0, -1, 0, DownloadReceiver.this.downloadFileSize, true, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                            DbHandler.setThreadRunning(contentLength, 1);
                            DbHandler.setFileRunning(contentLength);
                            DbHandler.closeDB();
                            if (DownloadReceiver.this.getIntent().hasExtra("FROM_APP")) {
                                DownloadReceiver.this.applicationRunning = DownloadReceiver.this.getIntent().getBooleanExtra("FROM_APP", false);
                            }
                            message = new Message();
                            message.what = 1207;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append(DownloadReceiver.this.getString(R.string.drawer_menu_downloading));
                            stringBuilder.append(" : ");
                            stringBuilder.append(DownloadReceiver.this.fileName);
                            message.obj = stringBuilder.toString();
                            DownloadReceiver.this.mHandler.sendMessage(message);
                            if (DownloadReceiver.this.applicationRunning) {
                                intent = new Intent(DownloadReceiver.this, MainActivity.class);
                                intent.putExtra("showDetails", true);
                                intent.putExtra("fileID", contentLength);
                                intent.putExtra("status", 3);
                                intent.putExtra("fileName", DownloadReceiver.this.fileName);
                                intent.putExtra("singleThread", true);
                                DownloadReceiver.this.startActivity(intent);
                            } else {
                                intent = new Intent(Constants.BROADCAST_NEW_DOWNLOAD);
                                intent.putExtra("showDetails", true);
                                intent.putExtra("fileID", contentLength);
                                intent.putExtra("status",3);
                                intent.putExtra("fileName", DownloadReceiver.this.fileName);
                                intent.putExtra("singleThread", true);
                                LocalBroadcastManager.getInstance(DownloadReceiver.this).sendBroadcast(intent);
                            }
                            DownloadReceiver.this.finish();
                            return;
                        }
                        j = DownloadReceiver.this.downloadFileSize / 3;
                        contentLength = DbHandler.insertFile(DownloadReceiver.this.url, DownloadReceiver.this.fileName, DownloadReceiver.this.savePath, DownloadReceiver.this.downloadFileSize, substring, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                        stringBuilder2 = new StringBuilder();
                        stringBuilder2.append(Constants.partPath);
                        stringBuilder2.append(File.separator);
                        stringBuilder2.append(contentLength);
                        new File(stringBuilder2.toString()).mkdirs();
                        downloaderService2 = DownloadReceiver.this.mDownloaderService;
                        str2 = DownloadReceiver.this.url;
                        stringBuilder5 = new StringBuilder();
                        stringBuilder5.append(Constants.partPath);
                        stringBuilder5.append(File.separator);
                        stringBuilder5.append(contentLength);
                        stringBuilder5.append(File.separator);
                        stringBuilder5.append(DownloadReceiver.this.fileName);
                        stringBuilder5.append(".1_part");


                        downloaderService2.addNewDownloadingThread(contentLength, 1, str2, stringBuilder5.toString(), DownloadReceiver.this.fileName, 0, j, 0, DownloadReceiver.this.downloadFileSize, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                        stringBuilder3 = new StringBuilder();
                        stringBuilder3.append(Constants.partPath);
                        stringBuilder3.append(File.separator);
                        stringBuilder3.append(contentLength);
                        stringBuilder3.append(File.separator);
                        stringBuilder3.append(DownloadReceiver.this.fileName);
                        stringBuilder3.append(".1_part");
                        DbHandler.insertThreads(contentLength, 1, stringBuilder3.toString(), DownloadReceiver.this.fileName, 0, j, 0);
                        downloaderService2 = DownloadReceiver.this.mDownloaderService;
                        str2 = DownloadReceiver.this.url;
                        stringBuilder5 = new StringBuilder();
                        stringBuilder5.append(Constants.partPath);
                        stringBuilder5.append(File.separator);
                        stringBuilder5.append(contentLength);
                        stringBuilder5.append(File.separator);
                        stringBuilder5.append(DownloadReceiver.this.fileName);
                        stringBuilder5.append(".2_part");
                        j2 = j + 1;
                        j3 = 2 * j;
                        downloaderService2.addNewDownloadingThread(contentLength,3, str2, stringBuilder5.toString(), DownloadReceiver.this.fileName, j2, j3, 0, DownloadReceiver.this.downloadFileSize, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                        stringBuilder6 = new StringBuilder();
                        stringBuilder6.append(Constants.partPath);
                        stringBuilder6.append(File.separator);
                        stringBuilder6.append(contentLength);
                        stringBuilder6.append(File.separator);
                        stringBuilder6.append(DownloadReceiver.this.fileName);
                        stringBuilder6.append(".2_part");
                        DbHandler.insertThreads(contentLength, 3, stringBuilder6.toString(), DownloadReceiver.this.fileName, j2, j3, 0);
                        downloaderService = DownloadReceiver.this.mDownloaderService;
                        str = DownloadReceiver.this.url;
                        stringBuilder4 = new StringBuilder();
                        stringBuilder4.append(Constants.partPath);
                        stringBuilder4.append(File.separator);
                        stringBuilder4.append(contentLength);
                        stringBuilder4.append(File.separator);
                        stringBuilder4.append(DownloadReceiver.this.fileName);
                        stringBuilder4.append(".3_part");
                        j2 = j3 + 1;
                        downloaderService.addNewDownloadingThread(contentLength, 3, str, stringBuilder4.toString(), DownloadReceiver.this.fileName, j2, DownloadReceiver.this.downloadFileSize, 0, DownloadReceiver.this.downloadFileSize, false, DownloadReceiver.this.userName, DownloadReceiver.this.password);
                        stringBuilder6 = new StringBuilder();
                        stringBuilder6.append(Constants.partPath);
                        stringBuilder6.append(File.separator);
                        stringBuilder6.append(contentLength);
                        stringBuilder6.append(File.separator);
                        stringBuilder6.append(DownloadReceiver.this.fileName);
                        stringBuilder6.append(".3_part");
                        DbHandler.insertThreads(contentLength, 3, stringBuilder6.toString(), DownloadReceiver.this.fileName, j2, DownloadReceiver.this.downloadFileSize, 0);
                        DbHandler.setThreadRunning(contentLength, 1);
                        DbHandler.setThreadRunning(contentLength, 2);
                        DbHandler.setThreadRunning(contentLength, 3);
                        DbHandler.setFileRunning(contentLength);
                        DbHandler.closeDB();
                        DownloadReceiver.this.applicationRunning = false;
                        if (DownloadReceiver.this.getIntent().hasExtra("FROM_APP")) {
                            DownloadReceiver.this.applicationRunning = DownloadReceiver.this.getIntent().getBooleanExtra("FROM_APP", false);
                        }
                        message = new Message();
                        message.what = 1207;
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(DownloadReceiver.this.getString(R.string.drawer_menu_downloading));
                        stringBuilder.append(" : ");
                        stringBuilder.append(DownloadReceiver.this.fileName);
                        message.obj = stringBuilder.toString();
                        DownloadReceiver.this.mHandler.sendMessage(message);
                        if (DownloadReceiver.this.applicationRunning) {
                            intent = new Intent(DownloadReceiver.this, MainActivity.class);
                            intent.putExtra("showDetails", true);
                            intent.putExtra("fileID", contentLength);
                            intent.putExtra("status", 2);
                            intent.putExtra("fileName", DownloadReceiver.this.fileName);
                            intent.putExtra("singleThread", false);
                            DownloadReceiver.this.startActivity(intent);
                        } else {
                            intent = new Intent(Constants.BROADCAST_NEW_DOWNLOAD);
                            intent.putExtra("showDetails", true);
                            intent.putExtra("fileID", contentLength);
                            intent.putExtra("status", 2);
                            intent.putExtra("fileName", DownloadReceiver.this.fileName);
                            intent.putExtra("singleThread", false);
                            LocalBroadcastManager.getInstance(DownloadReceiver.this).sendBroadcast(intent);
                        }
                        DownloadReceiver.this.finish();
                        return;
                    }
                    Toast.makeText(DownloadReceiver.this, "Download service is not connected",Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Exception exception = e;
                    String str5 = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Exception : ");
                    stringBuilder.append(exception.getMessage());
                    Log.e(str5, stringBuilder.toString());
                }
            }
        }.start();

    }

    public void onSelectDirectory(@NonNull String str) {
        int i = 1;
        if (((str != null ? 1 : 0) & (str.length() > 0 ? 1 : 0)) != 0) {
            String str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Export Path  : ");
            stringBuilder.append(str);
            Log.i(str2, stringBuilder.toString());
            this.savePath = str;
            this.lblSavePath.setText(this.savePath);
            this.lblSavePath.setSelection(this.savePath.length());
            this.fileName = this.txtSaveFileName.getText().toString();
            if (this.fileName.length() > 0 && this.fileName.contains(".")) {
                str = this.fileName.substring(this.fileName.lastIndexOf("."));
                stringBuilder = new StringBuilder();
                stringBuilder.append(this.savePath);
                stringBuilder.append(File.separator);
                stringBuilder.append(this.fileName);
                if (new File(stringBuilder.toString()).exists()) {
                    String substring = this.fileName.substring(0, this.fileName.lastIndexOf(46));
                    while (i <= 1000) {
                        stringBuilder = new StringBuilder();
                        stringBuilder.append(this.savePath);
                        stringBuilder.append(File.separator);
                        stringBuilder.append(substring);
                        stringBuilder.append("-");
                        stringBuilder.append(i);
                        stringBuilder.append(str);
                        if (!new File(stringBuilder.toString()).exists()) {
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append(substring);
                            stringBuilder2.append("-");
                            stringBuilder2.append(i);
                            substring = stringBuilder2.toString();
                            break;
                        }
                        i++;
                    }
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append(substring);
                    stringBuilder3.append(str);
                    this.fileName = stringBuilder3.toString();
                    this.txtSaveFileName.setText(this.fileName);
                }
            }
        }
        this.exportPathDialog.dismiss();
    }

    public void onCancelChooser() {
        this.exportPathDialog.dismiss();
    }

    public boolean isOnline(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    /* Access modifiers changed, original: protected */
    public void onStart() {
        super.onStart();
        bindService(new Intent(this, DownloaderService.class), this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    /* Access modifiers changed, original: protected */
    public void onStop() {
        super.onStop();
        if (this.serviceConnected) {
            unbindService(this.mServiceConnection);
            this.serviceConnected = false;
        }
    }

    private void setupLanguage() {
        Locale locale = new Locale(getSharedPreferences(Constants.Shared_Pref_Name, 0).getString(Constants.PREF_LANGUAGE_CODE, "en"));
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
    }
}
