package com.demo.mxplayer.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;

import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.demo.mxplayer.adapter.VideoAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.VideoModel;
import com.demo.mxplayer.player.Mediaplayer;
import com.demo.mxplayer.utils.MyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class favorite_video extends Fragment {


    private RecyclerView recyclerView;
    private List<VideoModel> videoModelList;
    List<String> favvideo_id;
    private VideoAdapter adapter;
    ArrayList<String> newvideoid;
    private String[] VIDEO_COLUMNS = new String[]{"_id", "_display_name", "title", "date_added", "duration", "resolution", "_size", "_data", "mime_type"};
    public static Cursor videocursor;
    public static String videoFilePath;
    DbHandler db;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_videos_list, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {


       // String path =getArguments().getString("folder_path");
      //  newvideoid=(ArrayList<String>)getArguments().getSerializable("newvideoid");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        db=new DbHandler(getActivity());
        videoModelList = new ArrayList<>();
        favvideo_id=new ArrayList<>();
        recyclerView = view.findViewById(R.id.videoRecycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.simpleSwipeRefreshLayout);

        parseAllVideo();



        adapter = new VideoAdapter(getActivity(), videoModelList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapterclick();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // cancel the Visual indication of a refresh
                videoModelList.clear();

                swipeRefreshLayout.setRefreshing(false);
                parseAllVideo();

                adapter = new VideoAdapter(getActivity(), videoModelList);
            //    recyclerView.setLayoutManager(sharedPreferences.getBoolean("is_grid", false) ? new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL) :new LinearLayoutManager(getContext()) );
                recyclerView.setAdapter(adapter);
                adapterclick();
            }
        });

    }

    public void adapterclick() {

        adapter.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position =   (int) v.getTag();

                if(v.getId()==R.id.clickevent) {
                    Log.i("position",position+"");


                        //              ArrayList<VideoViewInfo> videoRows = new ArrayList<VideoViewInfo>();
//                        Log.i("position1",position+"");
//                        String title = videocursor.getString(videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE));
//                        int video_duration = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
//                        int video_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
//                        if (!db.CheckIsVideoexits("track_id", videocursor.getString(video_index))) {
//                            newvideoid.remove(videocursor.getString(video_index));
//                            db.insert_all_video(videocursor.getString(video_index), title, videocursor.getString(video_duration));
//                        }
//                        int fileColumn = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
//                        int mimeColumn = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
//                        videoFilePath = videocursor.getString(fileColumn);

                        Intent intent = new Intent(getActivity(), Mediaplayer.class);
                        intent.setAction("video_list");
                        intent.putExtra("position", position);
                    intent.putExtra("destination", "favorite");
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
        title.setText(videoModelList.get(position).getName());
        lock.setVisibility(View.GONE);
        TextView share = (TextView) dialog.findViewById(R.id.share);

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
                startActivity(intent);
            }
        });


        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(db.CheckIsVideoFavexits("track_id",videoModelList.get(position).getMedia_id())) {
                    db.delete_video_favorite(videoModelList.get(position).getMedia_id());
                    videoModelList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, videoModelList.size());

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
                Intent intent = new Intent(getActivity(), com.demo.mxplayer.activity.cut_video.class);

                intent.putExtra("videofilename", videoModelList.get(position).getPath());
                startActivity(intent);
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
    private void parseAllVideo() {
        try {
            favvideo_id=db.GetFavVideo();
            Log.i("videoidsize",favvideo_id.get(0)+"");
            for (int i = 0; i < favvideo_id.size(); i++) {
                if (!db.CheckIsVideoLocked("track_id",favvideo_id.get(i))) {
                String[] proj = new String[]{favvideo_id.get(i)};
                String selection = "_id = ?";
                videocursor = getActivity().getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        VIDEO_COLUMNS, selection, proj, null);
                Boolean tag = false;
        if(videocursor != null ) {
            if (videocursor.moveToFirst()) {
                int name_index = videocursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
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


                    if (!db.CheckIsVideoexits("track_id", videocursor.getString(video_index))) {

                        tag = true;
                    }
                    VideoModel video = new VideoModel(video_id, name, filepath, duration, tag, resolution, format, lenght, videocursor.getString(video_index), videocursor.getLong(duration_index));
                    videoModelList.add(video);
//                    if (video_id == 2 && video_id  == 6 && video_id != 0) {
//                        videoModelList.add(null);
//
//                    }
                    video_id++;


                } while (videocursor.moveToNext());
            }
        }
//                videocursor.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
