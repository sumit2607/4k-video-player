package com.demo.mxplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demo.mxplayer.R;
import com.demo.mxplayer.models.FolderModel;

import java.util.List;


public class LokedFolderAdapter extends RecyclerView.Adapter<LokedFolderAdapter.ViewHolder> {

    private Context mContext;
    private List<FolderModel> folderModelList;


    public LokedFolderAdapter(Context mContext, List<FolderModel> folderModelList) {
        this.mContext = mContext;
        this.folderModelList = folderModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.lock_folder_item_layout, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final FolderModel folder = folderModelList.get(i);
        viewHolder.title.setText(folder.getName());
        viewHolder.tvTotalVideosCount.setText(folder.getTotal_video()+" videos");
       // Log.i("newfoldertag",folder.getNewvideo());
        if(!folder.getNewvideo().equalsIgnoreCase("0")){
            viewHolder.foldertag.setVisibility(View.VISIBLE);
            viewHolder.foldertag.setText(folder.getNewvideo());
         }

    }

    @Override
    public int getItemCount() {
        return folderModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title,tvTotalVideosCount,foldertag;
        ImageView option;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.folder_title);
            tvTotalVideosCount = itemView.findViewById(R.id.tvTotalVideosCount);
            option = itemView.findViewById(R.id.video_options);
            foldertag = itemView.findViewById(R.id.foldertag);
        }
    }

}
