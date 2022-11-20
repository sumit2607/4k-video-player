package com.demo.mxplayer.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.mxplayer.R;
import com.demo.mxplayer.models.Song;
import com.demo.mxplayer.models.SongModel;
import com.demo.mxplayer.playback.PlayerAdapter;
import com.demo.mxplayer.utils.Utils;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    public static ArrayList<SongModel> songModels = new ArrayList<>();
    LinearLayout songs_click;
    private ArrayList songsList = new ArrayList<Song>();
    private SongClicked songClicked;
    Context context;
    private PlayerAdapter mPlayerAdapter;
    public RecyclerAdapter(SongClicked clicked, Context context) {
        songClicked = clicked;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.track_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @SuppressLint("RecyclerView") int i) {
        final Song song= (Song) songsList.get(i);
        viewHolder.bind(song);
        viewHolder.songs_click.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songClicked.onSongClicked(song);
                    }
                }
        );

        viewHolder.more_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Dialog dialog;
                    dialog = new Dialog(context, R.style.DialogTheme);
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
                    title.setText(song.title);



                }



        });
    }



    @Override
    public int getItemCount() {
        return songsList.size();
    }

    public void addSongs(ArrayList songs) {
        songsList.clear();
        songsList = songs;
        notifyDataSetChanged();
    }

    public interface SongClicked {
        void onSongClicked(Song song);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title,artist;
        ImageView track_image;
        LinearLayout songs_click,more_song;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textViewSongTitle);
            artist = itemView.findViewById(R.id.textViewArtistName);
            track_image = itemView.findViewById(R.id.track_image);
            songs_click = itemView.findViewById(R.id.songs_click);
            more_song = itemView.findViewById(R.id.more_song);
        }
        void bind(Song song){
            title.setText(song.title);
            artist.setText(song.artistName);
            track_image.setImageBitmap(Utils.songArt1(song.path, context));


        }
    }
}
