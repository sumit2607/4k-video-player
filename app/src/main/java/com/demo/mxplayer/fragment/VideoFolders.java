package com.demo.mxplayer.fragment;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.lockedmedia;
import com.demo.mxplayer.adapter.FolderAdapter;
import com.demo.mxplayer.adapter.RecyclerItemClickListener;
import com.demo.mxplayer.adapter.VideoAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.FolderModel;

import com.demo.mxplayer.models.VideoModel;
import com.demo.mxplayer.pinlib.CustomPinActivity;
import com.demo.mxplayer.player.Mediaplayer;
import com.demo.mxplayer.utils.Filename;
import com.demo.mxplayer.utils.MyUtils;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.demo.mxplayer.utils.MyUtils.deleteCache;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFolders extends Fragment {

    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private List<FolderModel> foldersList,templist,filderlist;
    private List<String> folderpath;
    int video_id = 0;
    private String[] VIDEO_COLUMNS = new String[]{"_id", "_display_name", "title", "date_added", "duration", "resolution", "_size", "_data", "mime_type"};
    public VideoFolders() {
        // Required empty public constructor
    }
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    int new_added=0;
    String folder_location;
    double size=0;
    DbHandler db;
    ArrayList<String> newvideoid;
    MenuItem lock;
    SwipeRefreshLayout swipeRefreshLayout;
    RelativeLayout lastinfo;
    private RecyclerView recyclerView_last_view;
    public static List<VideoModel> videoModelList;
    private VideoAdapter lastviewadapter;
    TextView nofound;
    List<String> lastid;
    LinearLayout namesorting;
    boolean isMultiSelect = false;
    List<FolderModel> multiselect_list = new ArrayList<>();

    ActionMode mActionMode;

    com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAdMob;
    public void loadMob() {


        AdRequest adRequest = new AdRequest.Builder().build();
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("5568E99994901F4C1E938022EB5334EC","3E577FA421751B6DB467B936D6E01D6B")).build();
        MobileAds.setRequestConfiguration(configuration);
        com.google.android.gms.ads.interstitial.InterstitialAd.load(getActivity(),  getResources().getString(R.string.admob_interstitial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {
                mInterstitialAdMob = interstitialAd;
                interstitialAd.setFullScreenContentCallback(
                        new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {


                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {

                            }

                            @Override
                            public void onAdShowedFullScreenContent() {

                            }
                        });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
               mInterstitialAdMob = null;
//                MainActivityInterstitialID=GetActivityInterstitial;
//                faninit();
                //  load(SplashInterstitial);

            }
        });

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_videos_folder, container, false);
        deleteCache(getContext());
        setHasOptionsMenu(true);
        lastinfo=v.findViewById(R.id.lastinfo);

        loadMob();


        sharedPreferences = getActivity().getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Folders");
        foldersList = new ArrayList<>();
        folderpath = new ArrayList<>();
        lastid=new ArrayList<>();
        videoModelList = new ArrayList<>();
        templist = new ArrayList<>();
        db=new DbHandler(getActivity());
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.simpleSwipeRefreshLayout);
        recyclerView = v.findViewById(R.id.videoRecycler);
        namesorting = v.findViewById(R.id.namesorting);
        recyclerView.setHasFixedSize(true);

      //  recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );

        nofound=v.findViewById(R.id.nofound);

        recyclerView_last_view = v.findViewById(R.id.lastviewlist);
        recyclerView_last_view.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        parsealllastview();
        parseAllVideo();
        getUniqueFolders();
        adapter = new FolderAdapter(getContext(), foldersList,sharedPreferences.getBoolean("is_grid",false),multiselect_list);
        recyclerView.setAdapter(adapter);

        lastviewadapter = new VideoAdapter(getContext(), videoModelList,"view");
        recyclerView_last_view.setAdapter(lastviewadapter);

        adapterclick();

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect)
                    multi_select(position);

            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect) {
                    multiselect_list = new ArrayList<>();
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = getActivity().startActionMode(mActionModeCallback);
                    }
                }

                multi_select(position);

            }
        }));
//        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, final int position) {
//
//                        TextView folder_title =  view.findViewById(R.id.folder_title);
//                        ImageView  option = view.findViewById(R.id.video_options);
//                        LinearLayout clickevent = view.findViewById(R.id.clickevent);
//                        LinearLayout optionevent = view.findViewById(R.id.optiontag);
//                        final FolderModel folder = foldersList.get(position);
//                       if(view.getId()==R.id.folder_title){
//
//
//                                Fragment myFragment = new VideosList();
//                                Bundle args = new Bundle();
//                                args.putString("folder_path",  folder.getPath());
//                                args.putSerializable("newvideoid",  folder.getNewvideoid());
//                                args.putString("destination",  "normal");
//                                myFragment.setArguments(args);
//                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, myFragment).addToBackStack(null).commit();
//
//                            }
//
//
//                        if(view == optionevent){
//
//                                openOptionMenu(v,position);
//
//                            }
//
//                    }
//                })
//        );
        recyclerView_last_view.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(),recyclerView_last_view,new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {

                Intent intent = new Intent(getActivity(), Mediaplayer.class);
                intent.setAction("video_list");
                intent.putExtra("position", position);
                startActivity(intent);

            }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }
                })
        );
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                foldersList.clear();
                templist.clear();
                folderpath.clear();

                swipeRefreshLayout.setRefreshing(false);
                parseAllVideo();
                getUniqueFolders();
                adapter = new FolderAdapter(getContext(), foldersList,sharedPreferences.getBoolean("is_grid",false));
                recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );
                recyclerView.setAdapter(adapter);
                adapterclick();
            }
        });
        adapter.notifyDataSetChanged();
        return v;
    }
    private void ShowInterstitalAds() {
            if (this.mInterstitialAdMob != null) {
            this.mInterstitialAdMob.show(getActivity());

        }
    }
    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(foldersList.get(position)))
                multiselect_list.remove(foldersList.get(position));
            else
                multiselect_list.add(foldersList.get(position));

            if (multiselect_list.size() > 0)
                mActionMode.setTitle("" + multiselect_list.size());
            else
                mActionMode.setTitle("");

            refreshAdapter();

        }
    }
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.folder_option, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Confirm")
                            .setMessage("Do you really want to delete selected folder?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if(multiselect_list.size()>0)
                                    {
                                        for(int i=0;i<multiselect_list.size();i++) {

                                            if(MyUtils.deleteFolder(multiselect_list.get(i).getPath(),getContext())) {
                                                foldersList.remove(multiselect_list.get(i));
                                                templist.remove(multiselect_list.get(i));
                                         }
                                        }
                                        Toast.makeText(getActivity(),"Delete Successfully!",Toast.LENGTH_LONG).show();
                                        adapter.notifyDataSetChanged();

                                        if (mActionMode != null) {
                                            mActionMode.finish();
                                        }

                                    }


                                }})
                            .setNegativeButton(android.R.string.no, null).show();

                    return true;
                case R.id.details:
                    if(multiselect_list.size()==1) {
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.dialog_videodir_details);
                        TextView text_videodirName = (TextView) dialog.findViewById(R.id.text_videodirName);
                        TextView text_videodirSize = (TextView) dialog.findViewById(R.id.text_videodirSize);
                        TextView text_videoCount = (TextView) dialog.findViewById(R.id.text_videoCount);
                        TextView text_videodirPath = (TextView) dialog.findViewById(R.id.text_videodirPath);
                        TextView bt_confirm_videoListItemDetailsDialog = (TextView) dialog.findViewById(R.id.bt_confirm_videoListItemDetailsDialog);
                        text_videodirName.setText("Name: " + multiselect_list.get(0).getPath());
                        text_videodirSize.setText("Size: " + multiselect_list.get(0).getTotal_size());
                        text_videoCount.setText("Total Video: " + multiselect_list.get(0).getTotal_video());
                        text_videodirPath.setText("Path: " + multiselect_list.get(0).getDirectoy_path());
                        bt_confirm_videoListItemDetailsDialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    }
                    if(multiselect_list.size()>1)
                    {
                        int totalsize=0;
                        int totalcount=0;
                        for(int i=0;i<multiselect_list.size();i++) {
                            totalcount=totalcount+multiselect_list.get(i).getTotal_video();

                        }
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.dialog_videodir_details);
                        TextView text_videodirName = (TextView) dialog.findViewById(R.id.text_videodirName);
                        TextView text_videodirSize = (TextView) dialog.findViewById(R.id.text_videodirSize);
                        TextView text_videoCount = (TextView) dialog.findViewById(R.id.text_videoCount);
                        TextView text_videodirPath = (TextView) dialog.findViewById(R.id.text_videodirPath);
                        TextView bt_confirm_videoListItemDetailsDialog = (TextView) dialog.findViewById(R.id.bt_confirm_videoListItemDetailsDialog);
                        text_videodirName.setVisibility(View.GONE);
                        text_videodirSize.setVisibility(View.GONE);
                        text_videodirPath.setVisibility(View.GONE);
                        text_videoCount.setText("Total Video: " + totalcount);

                        bt_confirm_videoListItemDetailsDialog.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();


                    }

                    return true;

                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            multiselect_list = new ArrayList<>();
            refreshAdapter();
        }
    };

    public void refreshAdapter()
    {
        adapter.selected_usersList=multiselect_list;
        adapter.folderModelList=foldersList;
        adapter.notifyDataSetChanged();
    }
    public void adapterclick() {
        adapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos =   (int) v.getTag();
                if (!isMultiSelect) {
                    if (v.getId() == R.id.clickevent) {

                        final FolderModel folder = foldersList.get(pos);
                        Fragment myFragment = new VideosList();
                        Bundle args = new Bundle();
                        args.putString("folder_path", folder.getPath());
                        args.putSerializable("newvideoid", folder.getNewvideoid());
                        args.putString("destination", "normal");
                        myFragment.setArguments(args);
                        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, myFragment).addToBackStack(null).commit();
                        ShowInterstitalAds();
                    }
                    if (v.getId() == R.id.optiontag) {

                        openOptionMenu(v, pos);
                    }
                }

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main, menu);

        if (sharedPreferences.getBoolean("is_pin_enable", false)) {
            menu.findItem(R.id.lockmedia).setVisible(true);

        }else{
            menu.findItem(R.id.lockmedia).setVisible(false);

        }

        if (sharedPreferences.getBoolean("is_grid", false)) {
            menu.findItem(R.id.sorting).setIcon(R.drawable.list_213);
        }else{
            menu.findItem(R.id.sorting).setIcon(R.drawable.group_213);
        }
        MenuItem mSearch = menu.findItem(R.id.searchBar);
        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.setQueryHint("Search Folder");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.equalsIgnoreCase("")) {
                    foldersList.clear();
                    parseAllVideo();
                    getUniqueFolders();
                    adapter = new FolderAdapter(getContext(), foldersList);
                    recyclerView.setAdapter(adapter);

                }


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equalsIgnoreCase("")) {
                    filderlist = filter(foldersList, newText);
                    foldersList = filderlist;
                    adapter = new FolderAdapter(getContext(), foldersList,sharedPreferences.getBoolean("is_grid",false));
                    recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );

                    recyclerView.setAdapter(adapter);
                }else{
                    foldersList=templist;
                    adapter = new FolderAdapter(getContext(), templist,sharedPreferences.getBoolean("is_grid",false));
                    recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );

                    recyclerView.setAdapter(adapter);
                }
                adapterclick();
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.lockmedia) {
//            startActivity(new Intent(MainActivity.this, lockedmedia.class));
//            finish();

            Intent  intent = new Intent(getContext(), CustomPinActivity.class);
            intent.putExtra("type", 4);

            startActivityForResult(intent, 121);
        }

        if (id == R.id.sorting) {
            if (sharedPreferences.getBoolean("is_grid", false)) {
                editor.putBoolean("is_grid",false).apply();
                item.setIcon(R.drawable.group_213);
            }else{
                item.setIcon(R.drawable.list_213);
                editor.putBoolean("is_grid",true).apply();
            }
            adapter = new FolderAdapter(getContext(), foldersList,sharedPreferences.getBoolean("is_grid",false));
            recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );
            recyclerView.setAdapter(adapter);
            adapterclick();

        }




        return super.onOptionsItemSelected(item);
    }

    public void openOptionMenu(View v,final int position){
         Dialog dialog;
        dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.folder_popupmenu);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
        TextView delete = (TextView) dialog.findViewById(R.id.delete);
        TextView rename = (TextView) dialog.findViewById(R.id.rename);
        TextView lock = (TextView) dialog.findViewById(R.id.lock);
        TextView details = (TextView) dialog.findViewById(R.id.details);
        TextView title = (TextView) dialog.findViewById(R.id.title);

        title.setText(foldersList.get(position).getName());
        rename.setVisibility(View.GONE);
        if (sharedPreferences.getBoolean("is_pin_enable", false)) {

            lock.setVisibility(View.VISIBLE);
        }else{
            lock.setVisibility(View.GONE);

        }
        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent  intent = new Intent(getActivity(), CustomPinActivity.class);
                intent.putExtra("type", 4);
                intent.putExtra("position", position);
                intent.putExtra("DATA", foldersList.get(position).getPath());
                startActivityForResult(intent, 123);
            }
        });
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_videodir_details);
                TextView text_videodirName = (TextView) dialog.findViewById(R.id.text_videodirName);
                TextView text_videodirSize = (TextView) dialog.findViewById(R.id.text_videodirSize);
                TextView text_videoCount = (TextView) dialog.findViewById(R.id.text_videoCount);
                TextView text_videodirPath = (TextView) dialog.findViewById(R.id.text_videodirPath);
                TextView bt_confirm_videoListItemDetailsDialog = (TextView) dialog.findViewById(R.id.bt_confirm_videoListItemDetailsDialog);
                text_videodirName.setText("Name: "+foldersList.get(position).getPath());
                text_videodirSize.setText("Size: "+foldersList.get(position).getTotal_size());
                text_videoCount.setText("Total Video: "+foldersList.get(position).getTotal_video());
                text_videodirPath.setText("Path: "+foldersList.get(position).getDirectoy_path());
                bt_confirm_videoListItemDetailsDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm")
                        .setMessage("Do you really want to delete this folder?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(MyUtils.deleteFolder(foldersList.get(position).getPath(),getContext())) {
                                    foldersList.remove(position);
                                    templist.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, foldersList.size());
                                    Toast.makeText(getActivity(),"Delete Successfully!",Toast.LENGTH_LONG).show();
                                }

                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final EditText oldname = new EditText(getActivity());


                oldname.setText( foldersList.get(position).getPath());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Rename to")
                        .setView(oldname)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newname = oldname.getText().toString();

                                if(MyUtils.renamefolder(foldersList.get(position).getDirectoy_path(),foldersList.get(position).getPath(),newname,getContext())){

                                    foldersList.get(position).setName(newname);
                                    adapter.notifyItemChanged(position);
                                    adapter.notifyItemRangeChanged(position, foldersList.size());
                                    Toast.makeText(getActivity(),"Rename Successfully!",Toast.LENGTH_LONG).show();
                                }else{
                                    Toast.makeText(getActivity(),"Rename Failed!",Toast.LENGTH_LONG).show();
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        })
                        .show();
            }
        });

    }
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            int position=data.getIntExtra("position",0);
            db.insert_lock_folder( data.getStringExtra("DATA"));
            foldersList.remove(position);
            templist.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, foldersList.size());

        }
        if (requestCode == 121) {

            Intent  intent = new Intent(getContext(), lockedmedia.class);


            startActivity(intent);
        }
    }
    private List<FolderModel> filter(List<FolderModel> dataList, String newText) {
        newText=newText.toLowerCase();
        String text;
        filderlist=new ArrayList<>();
        for(FolderModel dataFromDataList:dataList){
            if(dataFromDataList!=null) {
                text = dataFromDataList.getName().toLowerCase();

                if (text.contains(newText)) {
                    filderlist.add(dataFromDataList);
                }
            }
        }
        if(filderlist.size()<0){
            foldersList=templist;
        }

        return filderlist;
    }
    private void parseAllVideo() {

        try {

            Cursor videocursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, "_data",MediaStore.Video.Media.DISPLAY_NAME}, null, null, null);
            videocursor = MediaStore.Video.query(getContext().getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME, "_data",MediaStore.Video.Media.DISPLAY_NAME});

            if (videocursor != null) {
                if (videocursor.moveToFirst()) {
                    int path_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME);
                    int path_id = videocursor.getColumnIndexOrThrow("_data");

                    do {

                            String filepath = videocursor.getString(path_index);
                        if(filepath!=null) {
                            Log.i("parseAllVideo", filepath);
                            folderpath.add(filepath);
                        }


                    } while (videocursor.moveToNext());
                }
                videocursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUniqueFolders() {
        List<String> newList = new ArrayList<>(new HashSet<>(folderpath));
        Collections.sort(newList);
        if(newList.size() > 0) {
            for (int i = 0; i < newList.size(); i++) {

                int total_video = parseAllVideo(newList.get(i));
                String folder_name = newList.get(i).substring(newList.get(i).lastIndexOf("/") + 1);
                String filesize = MyUtils.formatFileSize(size);
                if (!db.CheckIsFolderLocked("track_id", folder_name)) {
                    FolderModel folder = new FolderModel(folder_name, newList.get(i), total_video, new_added + "", newvideoid, filesize, folder_location);
                    foldersList.add(folder);
                    templist.add(folder);

                }
//                if (i  == 2 || i  == 6 &&  i != 0) {
//                    foldersList.add(null);
//                    templist.add(null);
//                }

            }
            if (sharedPreferences.getBoolean("is_new", true)) {
                editor.putBoolean("is_new", false).apply();
            }
        }else{
            nofound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

    }
    private int parseAllVideo(String folderpath) {
         video_id = 0;
        new_added=0;
        size=0;
        newvideoid = new ArrayList<>();
        try {

            String[] proj = new String[]{folderpath};
            String  selection = "bucket_display_name = ?";
            Cursor videocursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    VIDEO_COLUMNS, selection, proj, null);

            if (videocursor != null) {
                if (videocursor.moveToFirst()) {
                    int path_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    int video_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int video_name = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int video_duration= videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    int video_size= videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    do {
                        size=size+Long.parseLong(videocursor.getString(video_size));
                        String path=videocursor.getString(path_index);
                        Filename fileinfo = new Filename(path, '/', '.');
                        folder_location=fileinfo.path();


                        if (sharedPreferences.getBoolean("is_new", true)) {
                            if(!db.CheckIsVideoexits("track_id",videocursor.getString(video_index))) {
                                db.insert_all_video(videocursor.getString(video_index), videocursor.getString(video_name), videocursor.getString(video_duration));
                            }
                        }else{
                            if(!db.CheckIsVideoexits("track_id",videocursor.getString(video_index))){
                                newvideoid.add(videocursor.getString(video_index));
                                new_added++;
                            }
                        }
                        if(!db.CheckIsVideoLocked("track_id",videocursor.getString(video_index))) {
                            video_id++;
                        }


                    } while (videocursor.moveToNext());
                }
                videocursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return video_id;
    }

    private void parsealllastview() {
        try {
            lastid =db.GetLastView();
            Log.i("lastview",lastid.size()+"");
            if(lastid.size()>0) {

                for (int i = 0; i < lastid.size(); i++) {

                    String[] proj = new String[]{lastid.get(i)};
                    String selection = "_id = ?";
                    Cursor videocursor    = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            VIDEO_COLUMNS, selection, proj, null);
                    Boolean tag = false;
                    if (videocursor != null && !db.CheckIsVideoLocked("track_id", lastid.get(i))) {
                        if (videocursor.moveToFirst()) {
                            int name_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                            int duration_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                            int path_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                            int video_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                            int video_resolution = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION);
                            int video_format = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                            int video_lenght = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                            int video_id = 0;
                            do {
                                String name = videocursor.getString(name_index);
                                String filepath = videocursor.getString(path_index);
                                String duration = MyUtils.milisecondToHour(videocursor.getLong(duration_index));
                                String resolution = videocursor.getString(video_resolution);
                                String format = videocursor.getString(video_format);
                                String lenght = videocursor.getString(video_lenght);
                                VideoModel video = new VideoModel(video_id, name, filepath, duration, tag, resolution, format, lenght, videocursor.getString(video_index));
                                videoModelList.add(video);
                                video_id++;



                            } while (videocursor.moveToNext());
                        }
                        videocursor.close();
                    }
                }
                if(videoModelList.size()>0){
                    lastinfo.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
