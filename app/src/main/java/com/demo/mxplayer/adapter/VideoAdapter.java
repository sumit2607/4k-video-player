package com.demo.mxplayer.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.ads.NativeAdLayout;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.demo.mxplayer.LineProgress.LineProgress;
import com.demo.mxplayer.R;

import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.VideoModel;
import com.demo.mxplayer.utils.MyUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;




public class VideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    public static  List<VideoModel> videoList2;
    public static  List<VideoModel> videoList=new ArrayList<>();
    String option="";
    Boolean grid=false;
    DbHandler db;
    View.OnClickListener mClickListener;
    public List<VideoModel> selected_usersList=new ArrayList<>();


    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;
    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }
    public VideoAdapter(Context mContext, List<VideoModel> videolList) {
        videoList.clear();
        this.mContext = mContext;
        this.videoList2 = videolList;
        for (int i0 = 0; i0 < videolList.size(); i0++) {
            if(videolList.get(i0)!=null) {
                videoList.add(videolList.get(i0));
            }
        }


    }

    public VideoAdapter(Context mContext, List<VideoModel> videolList,String option) {
        this.mContext = mContext;
        videoList.clear();
        this.videoList2 = videolList;
        for (int i0 = 0; i0 < videolList.size(); i0++) {
            if(videolList.get(i0)!=null) {
                videoList.add(videolList.get(i0));
            }
        }
        this.option = option;


    }

    public VideoAdapter(Context mContext, List<VideoModel> videolList,Boolean grid) {
        this.mContext = mContext;
        videoList.clear();
        this.videoList2 = videolList;
        for (int i0 = 0; i0 < videolList.size(); i0++) {
            if(videolList.get(i0)!=null) {
                videoList.add(videolList.get(i0));
            }
        }

        this.grid = grid;


    }

    public VideoAdapter(Context mContext, List<VideoModel> videolList,Boolean grid,List<VideoModel> selected_usersList) {
        this.mContext = mContext;
        videoList.clear();
        this.videoList2 = videolList;
        for (int i0 = 0; i0 < videolList.size(); i0++) {
            if(videolList.get(i0)!=null) {
                videoList.add(videolList.get(i0));
            }
        }
        this.grid = grid;
        this.selected_usersList = selected_usersList;


    }
    public class AdHolder extends RecyclerView.ViewHolder {
        private NativeAdLayout nativeAdLayout;
        private FrameLayout frameLayout;
        private FrameLayout frameLayout3;
        //   private LinearLayout adLayout;

        public AdHolder(@NonNull View itemView) {
            super(itemView);
            nativeAdLayout = itemView.findViewById(R.id.native_ad_container);
            frameLayout = itemView.findViewById(R.id.native_ad_container2);
            frameLayout3 = itemView.findViewById(R.id.native_ad_container3);
        }
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == UNIFIED_NATIVE_AD_VIEW_TYPE) {
            //Inflate ad native container
            View bannerLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_native_adview, viewGroup, false);

            //Create View Holder
         AdHolder myAdViewHolder = new AdHolder(bannerLayoutView);

            return myAdViewHolder;
        }else {
            View v;
            if (option.equalsIgnoreCase("")) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(grid ? R.layout.video_list_item_gridview : R.layout.video_single_item, viewGroup, false);
            } else {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.video_single_item_last_view, viewGroup, false);

            }
            return new ViewHolder(v);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (videoList2.get(position) == null) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return MENU_ITEM_VIEW_TYPE;
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @SuppressLint("RecyclerView") int i) {

        int viewType = getItemViewType(i);

        if (viewType == UNIFIED_NATIVE_AD_VIEW_TYPE) {

            AdHolder adHolder = (AdHolder) viewHolder;

            adHolder.nativeAdLayout.removeAllViews();
            adHolder.frameLayout.removeAllViews();
            nativeAd(adHolder);
        }else {
            ViewHolder holder1 = (ViewHolder) viewHolder;

            final VideoModel video = videoList2.get(i);
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(video.getPath()))).placeholder(R.mipmap.ic_launcher)
                    .into(holder1.thumb);

            holder1.title.setText(video.getName());

            if (option.equalsIgnoreCase("")) {
                holder1.duration.setText(video.getDuration());

                if (db.CheckIsVideoView("track_id", video.getMedia_id())) {
                    try {
                        int position = db.get_single_video_last_view("track_id", video.getMedia_id());
                        holder1.lineProgress.setVisibility(View.VISIBLE);
                        holder1.lineProgress.setRoundEdge(true);
                        holder1.lineProgress.setShadow(true);
                        holder1.lineProgress.setProgress((int) ((position * 100) / video.getMilisecond()));

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                } else {
                    holder1.lineProgress.setVisibility(View.GONE);
                }

                if (video.getNewtag()) {
                    holder1.foldertag.setVisibility(View.VISIBLE);
                } else {
                    holder1.foldertag.setVisibility(View.GONE);
                }
            } else {
                holder1.videotime.setText(video.getDuration());
            }
            if (option.equalsIgnoreCase("")) {

                TypedValue typedValue = new TypedValue();
                Resources.Theme theme = mContext.getTheme();
                theme.resolveAttribute(R.attr.backgroundcolor, typedValue, true);
                @ColorInt int color = typedValue.data;
                if (selected_usersList.contains(videoList2.get(i)))
                    holder1.maincolor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.list_item_selected_state));
                else
                    holder1.maincolor.setBackgroundColor(color);
                holder1.clickevent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder1.clickevent.setTag(i);
                        mClickListener.onClick(view);
                    }
                });
                holder1.optiontag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder1.optiontag.setTag(i);
                        mClickListener.onClick(view);
                    }
                });
                if (grid) {
                    holder1.clickevent1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            holder1.clickevent1.setTag(i);
                            mClickListener.onClick(view);
                        }
                    });
                }
            } else {
                int startPosition = db.get_single_video_last_view("track_id", video.getMedia_id());


                holder1.startend.setText(MyUtils.milisecondToHour((long) startPosition) + "/" + video.getDuration());
            }

        }
    }

    @Override
    public int getItemCount() {

        return videoList2.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView thumb;
        TextView title, duration,foldertag,videotime,startend;
LinearLayout clickevent,optiontag,maincolor;
RelativeLayout clickevent1;

        LineProgress lineProgress;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.video_thumb);
            title = itemView.findViewById(R.id.video_title);
            videotime = itemView.findViewById(R.id.videotime);

            try {
                videotime.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                videotime.setSingleLine(true);
                videotime.setMarqueeRepeatLimit(-1);
                videotime.setSelected(true);

            } catch (Exception e)
            {

            }

            db = new DbHandler(mContext);
            if(option.equalsIgnoreCase("")) {
                maincolor = itemView.findViewById(R.id.maincolor);

                if(grid){
                    clickevent1 = itemView.findViewById(R.id.clickevent1);
                }
                duration = itemView.findViewById(R.id.video_duration);
                foldertag = itemView.findViewById(R.id.foldertag);
                foldertag = itemView.findViewById(R.id.foldertag);
                lineProgress = (LineProgress) itemView.findViewById(R.id.line_progress);
                clickevent = itemView.findViewById(R.id.clickevent);
                optiontag = itemView.findViewById(R.id.optiontag);

            }else{
                startend = itemView.findViewById(R.id.startend);
            }


        }




    }


    private void nativeAd(final AdHolder holder) {

        AdLoader.Builder builder = new AdLoader.Builder(mContext, mContext.getResources().getString(R.string.admob_native_id));
        builder.forNativeAd(new com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(com.google.android.gms.ads.nativead.NativeAd nativeAd) {

                NativeAdView adView = (NativeAdView) LayoutInflater.from(mContext).inflate(R.layout.nativeadmob, null);
                populateUnifiedNativeAdView(nativeAd, adView);
                holder.frameLayout.removeAllViews();
                holder.frameLayout.addView(adView);
            }
        });
        VideoOptions videoOptions = new VideoOptions.Builder().build();
        com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new com.google.android.gms.ads.nativead.NativeAdOptions.Builder().setVideoOptions(videoOptions).build();
        builder.withNativeAdOptions(adOptions);
        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());


    }

    private void populateUnifiedNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        Log.i("nativegoogle","populateUnifiedNativeAdView");
        com.google.android.gms.ads.nativead.MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }
        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }


}
