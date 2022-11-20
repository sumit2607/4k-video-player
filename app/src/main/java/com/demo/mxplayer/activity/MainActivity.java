package com.demo.mxplayer.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.provider.FontsContractCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.demo.mxplayer.R;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.download.Constants;
import com.demo.mxplayer.download.DownloadReceiver;
import com.demo.mxplayer.download.DownloaderDetails;
import com.demo.mxplayer.download.DownloaderOverview;
import com.demo.mxplayer.download.DownloaderService;
import com.demo.mxplayer.download.FileDetailDialog;
import com.demo.mxplayer.floatingactionbutton.FloatingActionButton;
import com.demo.mxplayer.floatingactionbutton.FloatingActionsMenu;
import com.demo.mxplayer.fragment.Download_list;
import com.demo.mxplayer.fragment.GalleryFragment;
import com.demo.mxplayer.fragment.Songfragment;
import com.demo.mxplayer.fragment.VideoFolders;
import com.demo.mxplayer.fragment.VideosList;
import com.demo.mxplayer.fragment.favorite_video;
import com.demo.mxplayer.models.SingleFile;
import com.demo.mxplayer.utils.MyUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;



public class    MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_CODE = 10;
    Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    private int theme;
    Class fragmentClass = null;
    Fragment fragment;
    private Context context;
    WifiManager wifi;
    FloatingActionButton add_url,donwload_list,nowdownloading;
    FloatingActionsMenu fabbutton;
    public  static DownloaderService mDownloaderService;
    public  static   boolean activityResumed = false;
    public  static   boolean deleteFiles = false;
    public  static   int detailsID = -1;
    public  static   boolean selectionMode = false;
    public  static  boolean serviceConnected = false;
    public  static   boolean showDetails = false;
    public  static String TAG = "MainActvity";
    boolean doubleBackToExitPressedOnce = false;
    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.BROADCAST_NEW_DOWNLOAD)) {
                Log.i(MainActivity.TAG, "======= BROADCAST_NEW_DOWNLOAD received");
                int intExtra = intent.getIntExtra("fileID", -1);
                int intExtra2 = intent.getIntExtra("status", -1);
                boolean booleanExtra = intent.getBooleanExtra("singleThread", false);
                if (intExtra <= 0) {
                    return;
                }
                if (MainActivity.this.activityResumed) {
                    String str = MainActivity.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("File Id : ");
                    stringBuilder.append(intExtra);
                    Log.i(str, stringBuilder.toString());
                    String str2 = MainActivity.TAG;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("Status : ");
                    stringBuilder2.append(intExtra2);
                    Log.i(str2, stringBuilder2.toString());
                    str2 = MainActivity.TAG;
                    StringBuilder stringBuilder3 = new StringBuilder();
                    stringBuilder3.append("singleThread : ");
                    stringBuilder3.append(booleanExtra);
                    Log.i(str2, stringBuilder3.toString());
                    FragmentManager supportFragmentManager = MainActivity.this.getSupportFragmentManager();
                    FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
                    supportFragmentManager.popBackStack(null, 1);
                    DownloaderDetails downloaderDetails = new DownloaderDetails();
                    downloaderDetails.mDownloaderService = MainActivity.this.mDownloaderService;
                    downloaderDetails.fileid = MainActivity.this.detailsID;
                    beginTransaction.replace(R.id.main_fragment, downloaderDetails, "DownloadDetails");
                    beginTransaction.addToBackStack("DownloadDetails");
                    beginTransaction.commit();
                    MainActivity.this.showDetails = false;
                    MainActivity.this.detailsID = -1;
                    return;
                }
                MainActivity.this.showDetails = true;
                MainActivity.this.detailsID = intExtra;
            }
        }
    };
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            DownloaderService.DownloaderServiceBinder downloaderServiceBinder = (DownloaderService.DownloaderServiceBinder) iBinder;
            MainActivity.this.mDownloaderService = downloaderServiceBinder.getService();
            if (MainActivity.this.mDownloaderService != null) {
                MainActivity.this.serviceConnected = true;
                MainActivity.this.showDetailFragment(MainActivity.this.getIntent());
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.this.serviceConnected = false;
            MainActivity.this.mDownloaderService = null;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = MainActivity.this;
        getWindow().setFlags(1024, 1024);
        theme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);




//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.menuicon));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if(sharedPreferences.getInt("THEME", 0)!=111) {
            toolbar.setNavigationIcon(R.drawable.menu);
        }else{
            toolbar.setNavigationIcon(R.drawable.menu_white);

        }
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        add_url = (FloatingActionButton) findViewById(R.id.add_url);
        nowdownloading = (FloatingActionButton) findViewById(R.id.nowdownloading);

        fabbutton = (FloatingActionsMenu) findViewById(R.id.multiple_actions);
        add_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabbutton.collapse();
                Intent intent = new Intent(MainActivity.this, DownloadReceiver.class);
                intent.putExtra("FROM_APP", true);
                startActivity(intent);
            }
        });

        donwload_list = (FloatingActionButton) findViewById(R.id.downloadlist);
        donwload_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabbutton.collapse();
                toolbar.setTitle("Download List");
                Fragment   fragment = new Download_list();
                loadFragment(fragment);
            }
        });

        nowdownloading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setTitle("Now Downloading");
                fabbutton.collapse();
                FragmentManager supportFragmentManager = getSupportFragmentManager();
                FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
                DownloaderOverview downloaderOverview = new DownloaderOverview();
                downloaderOverview.mDownloaderService = mDownloaderService;
                beginTransaction.add(R.id.main_fragment, downloaderOverview, "DownloadOverview");
                beginTransaction.addToBackStack(null);
                beginTransaction.commit();
            }
        });
       permissionHandle();
    }
    public void theme() {
        sharedPreferences = getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        theme = sharedPreferences.getInt("THEME", 0);

        MyUtils.settingTheme(context, theme);
    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            switch (item.getItemId()) {
                case R.id.local:
                    fabbutton.setVisibility(View.VISIBLE);
                    toolbar.setTitle("Folders");
                    fragment = new VideoFolders();
                    loadFragment(fragment);
                    return true;
                case R.id.nav_music:
                    fabbutton.setVisibility(View.GONE);
                    toolbar.setTitle("Music Library");
                    fragment = new Songfragment();
                    loadFragment(fragment);
                    return true;
                case R.id.gallery:
                    fabbutton.setVisibility(View.GONE);
                    toolbar.setTitle("Gallery");
                    fragment = new GalleryFragment();
                    loadFragment(fragment);

                    return true;

            }

            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main_fragment, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showDetailFragment(Intent intent) {

        if (intent.getExtras() != null && (intent.getFlags() & 1048576) == 0) {
            if (!(intent.hasExtra("occupied") ? intent.getBooleanExtra("occupied", false) : false)) {
                int intExtra;
                if (intent.hasExtra("showDetails")) {
                    if (intent.getBooleanExtra("showDetails", false) && intent.hasExtra("fileID")) {
                        intExtra = intent.getIntExtra("fileID", -1);
                        int intExtra2 = intent.getIntExtra("status", -1);
                        boolean booleanExtra = intent.getBooleanExtra("singleThread", false);
                        String stringExtra = intent.getStringExtra("fileName");
                        if (intExtra != -1) {
                            String str = TAG;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("File Id : ");
                            stringBuilder.append(intExtra);
                            Log.i(str, stringBuilder.toString());
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("Status : ");
                            stringBuilder.append(intExtra2);
                            Log.i(str, stringBuilder.toString());
                            str = TAG;
                            stringBuilder = new StringBuilder();
                            stringBuilder.append("singleThread : ");
                            stringBuilder.append(booleanExtra);
                            Log.i(str, stringBuilder.toString());
                            FragmentManager supportFragmentManager = getSupportFragmentManager();
                            supportFragmentManager.popBackStack(null, 1);
                            FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
                            DownloaderDetails downloaderDetails = new DownloaderDetails();
                            downloaderDetails.mDownloaderService = this.mDownloaderService;
                            downloaderDetails.fileid = intExtra;
                            downloaderDetails.status = intExtra2;
                            downloaderDetails.singleThread = booleanExtra;
                            if (stringExtra.length() > 0) {
                                setTitle(stringExtra);
                            }
                            beginTransaction.add(R.id.main_fragment, downloaderDetails, "DownloadDetails");
                            beginTransaction.addToBackStack("DownloadDetails");
                            beginTransaction.commit();
                        }
                    }
                    intent.putExtra("occupied", true);
                } else if (intent.hasExtra("from_notification")) {
                    if (intent.hasExtra(FontsContractCompat.Columns.FILE_ID) && intent.getIntExtra(FontsContractCompat.Columns.FILE_ID, 0) > 0) {
                        intExtra = intent.getIntExtra(FontsContractCompat.Columns.FILE_ID, 0);
                        String str2 = TAG;
                        StringBuilder stringBuilder2 = new StringBuilder();
                        stringBuilder2.append("notification file id : ");
                        stringBuilder2.append(intExtra);
                        Log.i(str2, stringBuilder2.toString());
                        if (intExtra > 0) {
                            new FileDetailDialog(this, this.mDownloaderService, intExtra).show();
                        }
                    }
                    intent.putExtra("occupied", true);
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                MainActivity.this.finish();
                System.exit(0);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//
//        if (sharedPreferences.getBoolean("is_pin_enable", false)) {
//menu.findItem(R.id.lockmedia).setVisible(true);
//
//        }else{
//            menu.findItem(R.id.lockmedia).setVisible(false);
//
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.lockmedia) {
//            startActivity(new Intent(MainActivity.this, lockedmedia.class));
//            finish();
//
//            Intent  intent = new Intent(MainActivity.this, CustomPinActivity.class);
//            intent.putExtra("type", 4);
//
//            startActivityForResult(intent, 123);
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;
        if (id == R.id.nav_music) {
            toolbar.setTitle("Music Library");
            fragment = new Songfragment();
            loadFragment(fragment);

        }
      else  if (id == R.id.videofolder) {
            toolbar.setTitle("Video Folder");
            fragment = new VideoFolders();
            loadFragment(fragment);

        }
        else if (id == R.id.favorite_video) {
            toolbar.setTitle("Favorite Video");
            fragment = new favorite_video();
            loadFragment(fragment);


        }
        else if (id == R.id.nav_setting) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            finish();

        }
        else if (id == R.id.rateus) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            }
        }
        else if (id == R.id.privacypolicy) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://jamessmithofficial.blogspot.com/2021/11/privacy-policy.html")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://jamessmithofficial.blogspot.com/2021/11/privacy-policy.html")));
            }
        }
        else if (id == R.id.shareapp) {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                String shareMessage= "\nLet me recommend you this application\n\n";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName() +"\n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "Share"));
            } catch(Exception e) {
                //e.toString();
            }

        }
        else if (id == R.id.adsblock) {
            Toast.makeText(context, "coming soon", Toast.LENGTH_SHORT).show();

        }
        else if (id == R.id.screen_mirroring) {
            enablingWiFiDisplay();


        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void enablingWiFiDisplay() {
        if (wifi.isWifiEnabled()) {
            wifidisplay();
            return;
        }
        wifi.setWifiEnabled(true);
        wifidisplay();
    }

    public void wifidisplay() {
        try {
            startActivity(new Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            try {
                startActivity(getPackageManager().getLaunchIntentForPackage("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"));
            } catch (Exception e2) {
                try {
                    startActivity(new Intent("android.settings.CAST_SETTINGS"));
                } catch (Exception e3) {
                    Toast.makeText(getApplicationContext(), "Device not supported", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {

            Intent  intent = new Intent(MainActivity.this, lockedmedia.class);


            startActivity(intent);
        }
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Bundle extras = getIntent().getExtras();

                    if (getIntent().hasExtra("destination") && extras != null && extras.getString("destination").equals("locked")) {
                        fragment = new VideosList();

                        Bundle args = new Bundle();
                        args.putString("folder_path", extras.getString("folder_path"));
                        args.putString("destination", "locked");
                        args.putSerializable("newvideoid", extras.getSerializable("newvideoid"));
                        fragment.setArguments(args);
                        loadFragment(fragment);
                    } else {
                        fragment = new VideoFolders();
                        loadFragment(fragment);
                    }
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void permissionHandle() {

        if (SDK_INT >= 23) {
            if (checkPermission()) {


                    Bundle extras = getIntent().getExtras();

                    if (getIntent().hasExtra("destination") && extras!= null &&  extras.getString("destination").equals("locked")) {
                        fragment = new VideosList();

                        Bundle args = new Bundle();
                        args.putString("folder_path", extras.getString("folder_path"));
                        args.putString("destination", "locked");
                        args.putSerializable("newvideoid", extras.getSerializable("newvideoid"));
                        fragment.setArguments(args);
                        loadFragment(fragment);
                    } else {
                        fragment = new VideoFolders();
                        loadFragment(fragment);
                    }

            }else {
                requestPermission();
            }
        }else{
            fragment = new VideoFolders();
            loadFragment(fragment);
        }
    }

    private boolean checkPermission() {
//        if (SDK_INT >= Build.VERSION_CODES.R) {
//            return Environment.isExternalStorageManager();
//
//
//
//        } else {
//
//        }

        int result = ContextCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
//        if (SDK_INT >= Build.VERSION_CODES.R) {
//
//
//            AlertDialog.Builder dialog=new AlertDialog.Builder(this);
//            dialog.setMessage("Video player  needs to allow access and manage external storage permission for  use features like view video,hide video,locked video,cut video,delete,rename video etc. This is mandatory permission is required by Android for our app. This permission required only theses features, but we are not collecting any data in our personal use and no store in any server.");
//            dialog.setTitle("Why app needs All File Access Permission?");
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("Ok",
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog,
//                                            int which) {
//                            dialog.dismiss();
//                            //   Log.i("requestPermission","devicelocation");
//                            try {
//                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                                intent.addCategory("android.intent.category.DEFAULT");
//                                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
//                                startActivityForResult(intent, 2296);
//                            } catch (Exception e) {
//                                Intent intent = new Intent();
//                                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                                startActivityForResult(intent, 2296);
//                            }
//
//
//
//                        }
//                    });
//            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            });
//          dialog.create();
//            dialog.show();
//
//        } else {
//            //below android 11
//
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted");
                    Intent intent = getIntent();


                    Bundle extras = getIntent().getExtras();

                    if (getIntent().hasExtra("destination") && extras != null && extras.getString("destination").equals("locked")) {
                        fragment = new VideosList();

                        Bundle args = new Bundle();
                        args.putString("folder_path", extras.getString("folder_path"));
                        args.putString("destination", "locked");
                        args.putSerializable("newvideoid", extras.getSerializable("newvideoid"));
                        fragment.setArguments(args);
                        loadFragment(fragment);
                    } else {
                        fragment = new VideoFolders();
                        loadFragment(fragment);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Please allow storage permission in App Settings.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public void onResume() {
        super.onResume();
        this.activityResumed = true;
        String str;
        StringBuilder stringBuilder;
        try {
            str = "MainActivity";
            stringBuilder = new StringBuilder();
            stringBuilder.append("onResume showDetails  : ");
            stringBuilder.append(this.showDetails);
            stringBuilder.append(" detailsID : ");
            stringBuilder.append(this.detailsID);
            Log.i(str, stringBuilder.toString());
            if (this.showDetails && this.detailsID > 0) {
                DbHandler.openDB();
                SingleFile fileDetail = DbHandler.getFileDetail(this.detailsID);
                DbHandler.closeDB();
                if (!(fileDetail == null || fileDetail.status == 4)) {
                    FragmentManager supportFragmentManager = getSupportFragmentManager();
                    FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
                    supportFragmentManager.popBackStack(null, 1);
                    DownloaderDetails downloaderDetails = new DownloaderDetails();
                    downloaderDetails.mDownloaderService = this.mDownloaderService;
                    downloaderDetails.fileid = this.detailsID;
                    beginTransaction.replace(R.id.main_fragment, downloaderDetails, "DownloadDetails");
                    beginTransaction.addToBackStack("DownloadDetails");
                    beginTransaction.commit();

                }
                this.showDetails = false;
                this.detailsID = -1;
            }
        } catch (Exception e) {
            str = "MainActivity";
            stringBuilder = new StringBuilder();
            stringBuilder.append("onResume Exception : ");
            stringBuilder.append(e.getMessage());
            Log.i(str, stringBuilder.toString());
        }
    }

    public void onPause() {
        super.onPause();
        this.activityResumed = false;
    }

    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mBroadcastReceiver);
        super.onDestroy();


    }

    public void onStart() {
        super.onStart();

        bindService(new Intent(this, DownloaderService.class), this.mServiceConnection, Context.BIND_AUTO_CREATE);
    }
    public void onStop() {
        super.onStop();

        if (this.serviceConnected) {
            unbindService(this.mServiceConnection);
            this.serviceConnected = false;
        }
    }

}