package com.demo.mxplayer.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.cut_video;
import com.demo.mxplayer.activity.lockedmedia;
import com.demo.mxplayer.adapter.RecyclerItemClickListener;
import com.demo.mxplayer.adapter.VideoAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.VideoModel;
import com.demo.mxplayer.pinlib.CustomPinActivity;
import com.demo.mxplayer.player.Mediaplayer;
import com.demo.mxplayer.utils.MyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosList extends Fragment {

    private RecyclerView recyclerView;
    public static List<VideoModel> videoModelList,templist;
    private VideoAdapter adapter;
    ArrayList<String> newvideoid;
    private String[] VIDEO_COLUMNS = new String[]{"_id", "_display_name", "title", "date_added", "duration", "resolution", "_size", "_data", "mime_type"};
    public static Cursor videocursor;
    public static String videoFilePath,destination;
    DbHandler db;
    SwipeRefreshLayout swipeRefreshLayout;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    String path="";
    List<VideoModel> filteredDataList;
    RelativeLayout maincontent;
    boolean isMultiSelect = false;
    List<VideoModel> multiselect_list = new ArrayList<>();
    ActionMode mActionMode;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_videos_list, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        db = new DbHandler(getActivity());
        setHasOptionsMenu(true);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        path = getArguments().getString("folder_path");
        sharedPreferences = getActivity().getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        newvideoid = (ArrayList<String>) getArguments().getSerializable("newvideoid");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(path);
        destination = getArguments().getString("destination");
        videoModelList = new ArrayList<>();
        templist = new ArrayList<>();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.simpleSwipeRefreshLayout);
        recyclerView = view.findViewById(R.id.videoRecycler);
        maincontent = view.findViewById(R.id.main_content);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );


        parseAllVideo(path);


        adapter = new VideoAdapter(getActivity(), videoModelList,sharedPreferences.getBoolean("is_grid", false),multiselect_list);
        recyclerView.setAdapter(adapter);
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
        adapterclick();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                videoModelList.clear();
                templist.clear();
                swipeRefreshLayout.setRefreshing(false);
                parseAllVideo(path);

                adapter = new VideoAdapter(getActivity(), videoModelList,sharedPreferences.getBoolean("is_grid", false) );
                recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );
                recyclerView.setAdapter(adapter);
                adapterclick();
            }
        });
        adapter.notifyDataSetChanged();


    }

    public void multi_select(int position) {
        if (mActionMode != null) {
            if (multiselect_list.contains(videoModelList.get(position)))
                multiselect_list.remove(videoModelList.get(position));
            else
                multiselect_list.add(videoModelList.get(position));

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
            inflater.inflate(R.menu.video_option, menu);

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
                            .setMessage("Do you really want to delete this folder?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {


                                    if(multiselect_list.size()>0)
                                    {
                                        for(int i=0;i<multiselect_list.size();i++) {





                                                File fdelete = new File(multiselect_list.get(i).getPath());
                                                if (fdelete.exists()) {
                                                    if (fdelete.delete()) {
                                                        MediaScannerConnection.scanFile(getContext(), new String[]{multiselect_list.get(i).getPath()}, null, null);
                                                        db.RemoveLockedVideo("track_id",multiselect_list.get(i).getMedia_id());
                                                        videoModelList.remove(multiselect_list.get(i));


                                                        Toast.makeText(getActivity(),multiselect_list.get(i).getName()+ " Delete Successfully!",Toast.LENGTH_LONG).show();

                                                    } else {
                                                        Toast.makeText(getActivity(), multiselect_list.get(i).getName()+" File not Deleted", Toast.LENGTH_LONG).show();
                                                    }
                                                }

                                        }

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
                        dialog.setContentView(R.layout.dialog_video_details);
                        TextView text_videodirName = (TextView) dialog.findViewById(R.id.text_videoName);
                        TextView text_videodirSize = (TextView) dialog.findViewById(R.id.text_videoSize);

                        TextView text_videodirPath = (TextView) dialog.findViewById(R.id.text_videoPath);
                        TextView lenght = (TextView) dialog.findViewById(R.id.lenght);
                        TextView format = (TextView) dialog.findViewById(R.id.format);
                        TextView resolution = (TextView) dialog.findViewById(R.id.text_videoResolution);
                        TextView bt_confirm_videoListItemDetailsDialog = (TextView) dialog.findViewById(R.id.bt_confirm_videoListItemDetailsDialog);
                        text_videodirName.setText("Name: "+multiselect_list.get(0).getName());
                        text_videodirPath.setText("Path: "+multiselect_list.get(0).getPath());
                        text_videodirSize.setText("Size: "+MyUtils.formatFileSize(Long.parseLong(multiselect_list.get(0).getLenght())));
                        resolution.setText("Resolution: "+multiselect_list.get(0).getResolution());

                        lenght.setText("Lenght: "+multiselect_list.get(0).getDuration());
                        format.setText("Format: "+multiselect_list.get(0).getFormat());
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
                        long totalcount=0;
                        for(int i=0;i<multiselect_list.size();i++) {
                            totalcount=totalcount+Long.parseLong(multiselect_list.get(i).getLenght());

                        }
                        final Dialog dialog = new Dialog(getActivity());
                        dialog.setContentView(R.layout.dialog_video_details);
                        TextView text_videodirName = (TextView) dialog.findViewById(R.id.text_videoName);
                        TextView text_videodirSize = (TextView) dialog.findViewById(R.id.text_videoSize);

                        TextView text_videodirPath = (TextView) dialog.findViewById(R.id.text_videoPath);
                        TextView lenght = (TextView) dialog.findViewById(R.id.lenght);
                        TextView format = (TextView) dialog.findViewById(R.id.format);
                        TextView resolution = (TextView) dialog.findViewById(R.id.text_videoResolution);
                        TextView bt_confirm_videoListItemDetailsDialog = (TextView) dialog.findViewById(R.id.bt_confirm_videoListItemDetailsDialog);

                        text_videodirName.setVisibility(View.GONE);
                        text_videodirPath.setVisibility(View.GONE);
                        lenght.setVisibility(View.GONE);
                        format.setVisibility(View.GONE);
                        resolution.setVisibility(View.GONE);
                        text_videodirSize.setText("Size: "+MyUtils.formatFileSize(totalcount));

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
        adapter.videoList=videoModelList;
        adapter.notifyDataSetChanged();
    }
    public void adapterclick(){
        adapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int) v.getTag();
                if (!isMultiSelect) {
                    if (v.getId() == R.id.clickevent) {

                        if (videocursor.moveToPosition(position)) {
//              ArrayList<VideoViewInfo> videoRows = new ArrayList<VideoViewInfo>();
                            String title = videocursor.getString(videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                            int video_duration = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                            int video_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                            if (!db.CheckIsVideoexits("track_id", videocursor.getString(video_index))) {
                                newvideoid.remove(videocursor.getString(video_index));
                                db.insert_all_video(videocursor.getString(video_index), title, videocursor.getString(video_duration));
                            }

                            int fileColumn = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                            int mimeColumn = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                            videoFilePath = videocursor.getString(fileColumn);

//                            Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
//                            intent.putExtra("videoFilePath", videoFilePath);
//                            intent.putExtra("pos", position);
//                            intent.putExtra("title", title);
//                            intent.putExtra("tag", "normal");
//                            intent.putExtra("back_folder_path", path);
//                            intent.putStringArrayListExtra("newvideoid", newvideoid);
//                            startActivity(intent);
                            Intent intent = new Intent(getActivity(), Mediaplayer.class);
                            intent.setAction("video_list");
                            intent.putExtra("position", position);
                            startActivity(intent);

                        }
                    }
                    if (v.getId() == R.id.clickevent1) {

                        if (videocursor.moveToPosition(position)) {
//              ArrayList<VideoViewInfo> videoRows = new ArrayList<VideoViewInfo>();
                            String title = videocursor.getString(videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
                            int video_duration = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                            int video_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                            if (!db.CheckIsVideoexits("track_id", videocursor.getString(video_index))) {
                                newvideoid.remove(videocursor.getString(video_index));
                                db.insert_all_video(videocursor.getString(video_index), title, videocursor.getString(video_duration));
                            }

                            int fileColumn = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                            int mimeColumn = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                            videoFilePath = videocursor.getString(fileColumn);

//                            Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
//                            intent.putExtra("videoFilePath", videoFilePath);
//                            intent.putExtra("pos", position);
//                            intent.putExtra("title", title);
//                            intent.putExtra("tag", "normal");
//                            intent.putExtra("back_folder_path", path);
//                            intent.putStringArrayListExtra("newvideoid", newvideoid);
//                            startActivity(intent);
                            Intent intent = new Intent(getActivity(), Mediaplayer.class);
                            intent.setAction("video_list");
                            intent.putExtra("position", position);
                            intent.putExtra("destination", destination);
                            startActivity(intent);

                        }
                    }
                    if (v.getId() == R.id.optiontag) {

                        openOptionMenu(v, position);
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
        searchView.setQueryHint("Search Video");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.equalsIgnoreCase("")) {
                    videoModelList.clear();

                    parseAllVideo(path);
                    adapter = new VideoAdapter(getActivity(), videoModelList);
                    recyclerView.setAdapter(adapter);
                    adapterclick();

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equalsIgnoreCase("")) {
                    filteredDataList = filter(videoModelList, newText);
                    videoModelList = filteredDataList;
                    adapter = new VideoAdapter(getActivity(), videoModelList);
                    recyclerView.setAdapter(adapter);
                }else{
                    videoModelList=templist;
                    adapter = new VideoAdapter(getActivity(), templist);
                    recyclerView.setAdapter(adapter);
                }
                adapterclick();
                return false;
            }
        });

        super.onCreateOptionsMenu(menu,inflater);
    }
    private List<VideoModel> filter(List<VideoModel> dataList, String newText) {
        newText=newText.toLowerCase();
        String text;
        filteredDataList=new ArrayList<>();
        for(VideoModel dataFromDataList:dataList){

            if(dataFromDataList!=null) {
                text = dataFromDataList.getName().toLowerCase();

                if (text.contains(newText)) {
                    filteredDataList.add(dataFromDataList);
                }
            }
        }
        if(filteredDataList.size()<0){
            videoModelList=templist;
        }

        return filteredDataList;
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
            adapter = new VideoAdapter(getActivity(), videoModelList,sharedPreferences.getBoolean("is_grid", false) );
            recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );
            recyclerView.setAdapter(adapter);
            adapterclick();

        }
        return super.onOptionsItemSelected(item);
    }

    public void openOptionMenu(View v,final int position){


        Dialog dialog;
        dialog = new Dialog(getContext(), R.style.DialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.video_popupmenu);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        dialog.show();
        TextView delete = (TextView) dialog.findViewById(R.id.delete);
        TextView rename = (TextView) dialog.findViewById(R.id.rename);
        TextView lock = (TextView) dialog.findViewById(R.id.lock);
        TextView details = (TextView) dialog.findViewById(R.id.details);
        TextView title = (TextView) dialog.findViewById(R.id.title);
        TextView play = (TextView) dialog.findViewById(R.id.play);
        TextView favorite = (TextView) dialog.findViewById(R.id.favorite);
        TextView cut_video = (TextView) dialog.findViewById(R.id.cut_video);
        TextView share = (TextView) dialog.findViewById(R.id.share);
        title.setText(videoModelList.get(position).getName());

        if(destination.equalsIgnoreCase("locked")){
            lock.setVisibility(View.GONE);
            favorite.setVisibility(View.GONE);
            rename.setVisibility(View.GONE);

        }
if(db.CheckIsVideoFavexits("track_id",videoModelList.get(position).getMedia_id())){

    favorite.setText("Remove Favorite");

}
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), Mediaplayer.class);
                intent.setAction("video_list");
                intent.putExtra("position", position);
                intent.putExtra("destination", destination);
                startActivity(intent);
            }
        });

        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sharedPreferences.getBoolean("is_pin_enable", false)) {
                    dialog.dismiss();
                    Intent intent = new Intent(getActivity(), CustomPinActivity.class);
                    intent.putExtra("type", 4);
                    intent.putExtra("position", position);
                    intent.putExtra("DATA", videoModelList.get(position).getMedia_id());
                    startActivityForResult(intent, 123);
                }else{
                    Toast.makeText(getActivity(),"Please Enable Lock Feature From Settings",Toast.LENGTH_LONG).show();

                }
            }
        });
        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(db.CheckIsVideoFavexits("track_id",videoModelList.get(position).getMedia_id())) {
                    db.delete_video_favorite(videoModelList.get(position).getMedia_id());
                    Toast.makeText(getActivity(),"Remove From Favorite Successfully",Toast.LENGTH_LONG).show();
                }else{
                    db.insert_video_favorite(videoModelList.get(position).getMedia_id());
                    Toast.makeText(getActivity(),"Add To Favorite Successfully",Toast.LENGTH_LONG).show();
                }
            }
        });
        cut_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), cut_video.class);

                intent.putExtra("videofilename", videoModelList.get(position).getPath());
                startActivity(intent);
            }
        });
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Uri uri = Uri.fromFile(new File(videoModelList.get(position).getPath()));
                Intent videoshare = new Intent(Intent.ACTION_SEND);
                videoshare.setType("Video/*");
                videoshare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                videoshare.putExtra(Intent.EXTRA_STREAM,uri);
                startActivity(Intent.createChooser(videoshare, "Share Video"));
            }
        });
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_video_details);
                TextView text_videodirName = (TextView) dialog.findViewById(R.id.text_videoName);
                TextView text_videodirSize = (TextView) dialog.findViewById(R.id.text_videoSize);

                TextView text_videodirPath = (TextView) dialog.findViewById(R.id.text_videoPath);
                TextView lenght = (TextView) dialog.findViewById(R.id.lenght);
                TextView format = (TextView) dialog.findViewById(R.id.format);
                TextView resolution = (TextView) dialog.findViewById(R.id.text_videoResolution);
                TextView bt_confirm_videoListItemDetailsDialog = (TextView) dialog.findViewById(R.id.bt_confirm_videoListItemDetailsDialog);
                text_videodirName.setText("Name: "+videoModelList.get(position).getName());
                text_videodirPath.setText("Path: "+videoModelList.get(position).getPath());
                text_videodirSize.setText("Size: "+MyUtils.formatFileSize(Long.parseLong(videoModelList.get(position).getLenght())));
                resolution.setText("Resolution: "+videoModelList.get(position).getResolution());

                lenght.setText("Lenght: "+videoModelList.get(position).getDuration());
                format.setText("Format: "+videoModelList.get(position).getFormat());
                bt_confirm_videoListItemDetailsDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });
        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                final EditText oldname = new EditText(getActivity());


                oldname.setText( videoModelList.get(position).getName());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Rename to")
                        .setView(oldname)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newname = oldname.getText().toString();

                                if(MyUtils.renamevideo(videoModelList.get(position).getPath(),videoModelList.get(position).getName(),newname,getContext())){

                                    videoModelList.get(position).setName(newname);
                                    adapter.notifyItemChanged(position);
                                    adapter.notifyItemRangeChanged(position, videoModelList.size());
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

                                File fdelete = new File(videoModelList.get(position).getPath());
                                if (fdelete.exists()) {
                                    if (fdelete.delete()) {
                                        MediaScannerConnection.scanFile(getContext(), new String[]{videoModelList.get(position).getPath()}, null, null);
                                        db.RemoveLockedVideo("track_id",videoModelList.get(position).getMedia_id());
                                        videoModelList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        adapter.notifyItemRangeChanged(position, videoModelList.size());

                                        Toast.makeText(getActivity(),"Delete Successfully!",Toast.LENGTH_LONG).show();

                                    } else {
                                        Toast.makeText(getActivity(), "File not Deleted", Toast.LENGTH_LONG).show();
                                    }
                                }


                            }})
                        .setNegativeButton(android.R.string.no, null).show();
            }
        });

    }

    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            int position=data.getIntExtra("position",0);
            db.insert_lock_video(data.getStringExtra("DATA"));
            videoModelList.remove(position);
            templist.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, videoModelList.size());


        }
        if (requestCode == 121) {

            Intent  intent = new Intent(getContext(), lockedmedia.class);


            startActivity(intent);
        }
    }
    private void parseAllVideo(String folderpath) {
        try {
//            String[] proj = {MediaStore.Video.Media._ID,
//                    MediaStore.Video.Media.DATA,
//                    MediaStore.Video.Media.TITLE,
//                    MediaStore.Video.Media.SIZE,
//                    MediaStore.Video.Media.DURATION};
            String[] proj = new String[]{folderpath};
            String  selection = "bucket_display_name = ?";
             videocursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    VIDEO_COLUMNS, selection, proj, null);
Boolean tag=false;
            if (videocursor != null) {
                if (videocursor.moveToFirst()) {
                    int display_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int name_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                    int duration_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    int path_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                    int video_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int video_resolution = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.RESOLUTION);
                    int video_format = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
                    int video_lenght = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int video_id = 0;
                    do {
                        String name = videocursor.getString(display_index);
                        String filepath = videocursor.getString(path_index);
                        String duration = MyUtils.milisecondToHour(videocursor.getLong(duration_index));
                        String resolution = videocursor.getString(video_resolution);
                        String format = videocursor.getString(video_format);
                        String lenght = videocursor.getString(video_lenght);
                        if(!db.CheckIsVideoLocked("track_id",videocursor.getString(video_index))) {

                            if (newvideoid.contains(videocursor.getString(video_index))) {

                                tag = true;
                            }else{
                                tag = false;
                            }
                            VideoModel video = new VideoModel(video_id, name, filepath, duration, tag,resolution,format,lenght,videocursor.getString(video_index),videocursor.getLong(duration_index));
                            videoModelList.add(video);
                            templist.add(video);

//                if (video_id == 2 || video_id  == 6 && video_id != 0) {
//                                                videoModelList.add(null);
//                                                templist.add(null);
//                }
                          video_id++;
                        }

                    } while (videocursor.moveToNext());
                }
//                videocursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK){
                    if(destination.equals("locked")){
                        Intent intent = new Intent(getActivity(), lockedmedia.class);

                        startActivity(intent);
                    }else{
                        Fragment   fragment=new VideoFolders();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.main_fragment, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }

                    return true;
                }

                return false;
            }
        });
    }
}
