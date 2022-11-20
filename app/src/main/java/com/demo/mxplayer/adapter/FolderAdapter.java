package com.demo.mxplayer.adapter;

import android.content.Context;
import android.content.res.Resources;

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
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinSdkUtils;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.demo.mxplayer.R;

import com.demo.mxplayer.models.FolderModel;
import com.demo.mxplayer.utils.MyUtils;

import java.util.ArrayList;
import java.util.List;




public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    public List<FolderModel> folderModelList;
    Boolean grid=false;
    View.OnClickListener mClickListener;
    public List<FolderModel> selected_usersList=new ArrayList<>();
    private static final int MENU_ITEM_VIEW_TYPE = 0;
    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

    public void setClickListener(View.OnClickListener callback) {
        mClickListener = callback;
    }
    public FolderAdapter(Context mContext, List<FolderModel> folderModelList) {
        this.mContext = mContext;
        this.folderModelList = folderModelList;

    }
    public FolderAdapter(Context mContext, List<FolderModel> folderModelList,Boolean grid) {
        this.mContext = mContext;
        this.folderModelList = folderModelList;
        this.grid = grid;

    }
    public FolderAdapter(Context mContext, List<FolderModel> folderModelList,Boolean grid,List<FolderModel> selectedList) {
        this.mContext = mContext;
        this.folderModelList = folderModelList;
        this.grid = grid;
        this.selected_usersList = selectedList;

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
    public  RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
       // View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.folder_item_layout, viewGroup, false);
        if (i == UNIFIED_NATIVE_AD_VIEW_TYPE) {
            //Inflate ad native container
            View bannerLayoutView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_native_adview, viewGroup, false);

            //Create View Holder
            AdHolder myAdViewHolder = new AdHolder(bannerLayoutView);

            return myAdViewHolder;
        }else {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(grid ? R.layout.folder_item_layout_gridview : R.layout.folder_item_layout, viewGroup, false);


            return new ViewHolder(v);
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (folderModelList.get(position) == null) {
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return MENU_ITEM_VIEW_TYPE;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder,  int i) {
        int viewType = getItemViewType(i);
        if (viewType == UNIFIED_NATIVE_AD_VIEW_TYPE) {
            AdHolder adHolder = (AdHolder) viewHolder;
            adHolder.nativeAdLayout.removeAllViews();
            adHolder.frameLayout.removeAllViews();
            nativeAd(adHolder);
        }else {
            ViewHolder holder1 = (ViewHolder) viewHolder;
            final FolderModel folder = folderModelList.get(i);
            holder1.title.setText(folder.getName());
            Log.i("foldertitle", folder.getName());
            holder1.tvTotalVideosCount.setText(folder.getTotal_video() + " videos");
            // Log.i("newfoldertag",folder.getNewvideo());
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = mContext.getTheme();
            theme.resolveAttribute(R.attr.backgroundcolor, typedValue, true);
            @ColorInt int color = typedValue.data;
            if (selected_usersList.contains(folderModelList.get(i)))
                holder1.mainbackcolor.setBackgroundColor(ContextCompat.getColor(mContext, R.color.list_item_selected_state));
            else
                holder1.mainbackcolor.setBackgroundColor(color);

            if (!folder.getNewvideo().equalsIgnoreCase("0")) {
                holder1.foldertag.setText(folder.getNewvideo());
                holder1.newtag.setVisibility(View.VISIBLE);
                holder1.foldertag.setVisibility(View.VISIBLE);

            } else {
                holder1.foldertag.setVisibility(View.GONE);
                holder1.newtag.setVisibility(View.GONE);
            }


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
        }

    }

    @Override
    public int getItemCount() {
        return folderModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        TextView title,tvTotalVideosCount,foldertag,newtag;
        ImageView option;
        LinearLayout optiontag,clickevent,mainbackcolor;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.folder_title);

            tvTotalVideosCount = itemView.findViewById(R.id.tvTotalVideosCount);
            option = itemView.findViewById(R.id.video_options);
            foldertag = itemView.findViewById(R.id.foldertag);
            optiontag = itemView.findViewById(R.id.optiontag);
            clickevent = itemView.findViewById(R.id.clickevent);
            newtag = itemView.findViewById(R.id.newtag);
            mainbackcolor = itemView.findViewById(R.id.mainbackcolor);
//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    mClickListener.onClick(view);
//                }
//            });
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
