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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.demo.mxplayer.R;
import com.demo.mxplayer.activity.MainActivity;
import com.demo.mxplayer.adapter.FolderAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.FolderModel;
import com.demo.mxplayer.utils.Filename;
import com.demo.mxplayer.utils.MyUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class LockFolders extends Fragment {

    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private List<FolderModel> foldersList;
    private List<String> folderpath;
    int video_id = 0;
    private String[] VIDEO_COLUMNS = new String[]{"_id", "_display_name", "title", "date_added", "duration", "resolution", "_size", "_data", "mime_type"};
    public LockFolders() {
        // Required empty public constructor
    }
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    int new_added=0;
    DbHandler db;
    ArrayList<String> newvideoid;
    MenuItem lock;
    String folder_location;
    double size=0;
    TextView nofound;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_videos_list, container, false);
        sharedPreferences = getActivity().getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        foldersList = new ArrayList<>();
        folderpath = new ArrayList<>();
        db=new DbHandler(getActivity());
        recyclerView = v.findViewById(R.id.videoRecycler);
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.simpleSwipeRefreshLayout);
        nofound = v.findViewById(R.id.nofound);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
      //  parseAllVideo();
        getUniqueFolders();
        adapter = new FolderAdapter(getContext(), foldersList);
        recyclerView.setAdapter(adapter);
        adapterclick();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                foldersList.clear();

                swipeRefreshLayout.setRefreshing(false);

                getUniqueFolders();
                adapter = new FolderAdapter(getContext(), foldersList);
                recyclerView.setAdapter(adapter);
                adapterclick();
            }
        });
        adapter.notifyDataSetChanged();
        return v;
    }
    public void adapterclick() {


        adapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position =   (int) v.getTag();

                if(v.getId()==R.id.clickevent) {
                    final FolderModel folder = foldersList.get(position);
//                    Fragment myFragment = new VideosList();
//                    Bundle args = new Bundle();
//                    args.putString("folder_path",  folder.getPath());
//                    args.putString("destination",  "locked");
//                    args.putSerializable("newvideoid",  folder.getNewvideoid());
//                    myFragment.setArguments(args);
//                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, myFragment).addToBackStack(null).commit();
Log.i("lockfolder", folder.getPath());
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("folder_path",  folder.getPath());
                    intent.putExtra("destination", "locked");
                    intent.putExtra( "newvideoid",  folder.getNewvideoid());
                    startActivity(intent);
                }
                if(v.getId()==R.id.optiontag) {

                    openOptionMenu(v,position);
                }

            }
        });
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
        lock.setText("Unlock");

        lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                db.RemoveLockedFolder("track_id",foldersList.get(position).getPath());
                foldersList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, foldersList.size());
                Toast.makeText(getActivity(),"Unlock Successfully",Toast.LENGTH_LONG).show();
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
                                    db.RemoveLockedFolder("track_id",foldersList.get(position).getPath());
                                    foldersList.remove(position);
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

    }
    private void parseAllVideo() {
        try {

            Cursor videocursor = getContext().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[]{"bucket_display_name", "_data",MediaStore.Video.Media.DISPLAY_NAME,MediaStore.Video.Media._ID}, null, null, null);
            if (videocursor != null) {
                if (videocursor.moveToFirst()) {
                    int path_index = videocursor.getColumnIndexOrThrow("bucket_display_name");
                    int path_id = videocursor.getColumnIndexOrThrow("_data");
                    do {
                        String filepath = videocursor.getString(path_index);
                        folderpath.add(filepath);

                    } while (videocursor.moveToNext());
                }
                videocursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUniqueFolders() {
        folderpath=db.GetLockedFolder();
        List<String> newList = new ArrayList<>(new HashSet<>(folderpath));

                if(folderpath.size()>0) {

                    for (int i = 0; i < folderpath.size(); i++) {

                        int total_video = parseAllVideo(folderpath.get(i));
                        String folder_name = folderpath.get(i).substring(folderpath.get(i).lastIndexOf("/") + 1);
                        String filesize = MyUtils.formatFileSize(size);
                        FolderModel folder = new FolderModel(folder_name, folderpath.get(i), total_video, new_added + "", newvideoid, filesize, folder_location);
                        foldersList.add(folder);

//                        if (i == 2 || i  == 6 && i != 0) {
//                            foldersList.add(null);
//                        }

                    }

                }else{
                    recyclerView.setVisibility(View.GONE);

                    nofound.setVisibility(View.VISIBLE);
                }


    }
    private int parseAllVideo(String folderpath) {
         video_id = 0;
        new_added=0;
        newvideoid = new ArrayList<>();
        size=0;
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
                    int video_size= videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    do {


                        size=size+Long.parseLong(videocursor.getString(video_size));
                        String path=videocursor.getString(path_index);
                        Filename fileinfo = new Filename(path, '/', '.');
                        folder_location=fileinfo.path();

                            if(!db.CheckIsVideoexits("track_id",videocursor.getString(video_index))){
                                newvideoid.add(videocursor.getString(video_index));
                                new_added++;
                            }

                        video_id++;


                    } while (videocursor.moveToNext());
                }
                videocursor.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return video_id;
    }

}
