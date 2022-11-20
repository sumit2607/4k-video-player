package com.demo.mxplayer.player;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.NativeAdLayout;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.demo.mxplayer.App;
import com.demo.mxplayer.R;
import com.demo.mxplayer.adapter.VideoAdapter;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.VideoModel;
import com.demo.mxplayer.utils.CustomVerticalSeekbar;
import com.demo.mxplayer.utils.Filedialog;
import com.demo.mxplayer.utils.MyUtils;
import com.demo.mxplayer.utils.ZoomableExoPlayerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackPreparer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.offline.FilteringManifestParser;
import com.google.android.exoplayer2.offline.StreamKey;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.ads.AdsLoader;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifestParser;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.hls.playlist.DefaultHlsPlaylistParserFactory;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.manifest.SsManifestParser;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.RandomTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.TrackSelectionView;
import com.google.android.exoplayer2.ui.spherical.SphericalSurfaceView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.ErrorMessageProvider;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;


import java.io.File;
import java.io.FileOutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.google.android.exoplayer2.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;


public class Mediaplayer extends AppCompatActivity implements View.OnClickListener, PlaybackPreparer, PlayerControlView.VisibilityListener {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String ABR_ALGORITHM_DEFAULT = "default";
    public static final String ABR_ALGORITHM_EXTRA = "abr_algorithm";
    public static final String ABR_ALGORITHM_RANDOM = "random";
    public static final String TAG = "PlayerGestureListener";
    private static final CookieManager DEFAULT_COOKIE_MANAGER = new CookieManager();
    public static final String DRM_KEY_REQUEST_PROPERTIES_EXTRA = "drm_key_request_properties";
    public static final String DRM_LICENSE_URL_EXTRA = "drm_license_url";
    public static final String DRM_MULTI_SESSION_EXTRA = "drm_multi_session";
    public static final String DRM_SCHEME_EXTRA = "drm_scheme";
    private static final String DRM_SCHEME_UUID_EXTRA = "drm_scheme_uuid";
    public static final String EXTENSION_EXTRA = "extension";
    public static final String EXTENSION_LIST_EXTRA = "extension_list";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private static final String KEY_POSITION = "position";
    private static final String KEY_TRACK_SELECTOR_PARAMETERS = "track_selector_parameters";
    private static final String KEY_WINDOW = "window";
    public static final String PREFER_EXTENSION_DECODERS_EXTRA = "prefer_extension_decoders";
    public static final String SPHERICAL_STEREO_MODE_EXTRA = "spherical_stereo_mode";
    public static final String SPHERICAL_STEREO_MODE_LEFT_RIGHT = "left_right";
    public static final String SPHERICAL_STEREO_MODE_MONO = "mono";
    public static final String SPHERICAL_STEREO_MODE_TOP_BOTTOM = "top_bottom";
    AlertDialog alert;
    private ViewGroup adUiViewGroup;
    private AdsLoader adsLoader;
    private AudioManager audioManager;
    private float brightness = -1.0f;
    boolean is_locked=false;
    private ImageView btnAspect,lockedcontrol,screenshot,setting,btnAudio,btnText,btnVideo;

    private DataSource.Factory dataSourceFactory;
    private LinearLayout debugRootView;
    RelativeLayout control_click;
    private TextView debugTextView,tv_title;
    private DebugTextViewHelper debugViewHelper;
    FrameLayout root_view;
    DbHandler db;
    SharedPreferences session_media;
    CustomVerticalSeekbar customVerticalSeekbar;
    private int NONE = 0;
    private  int DRAG = 1;
    private  int ZOOM = 2;
    private  int mode = NONE;
    String MainActivityInterstitialID="";
    com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAdMob;
    public void loadMob() {


        AdRequest adRequest = new AdRequest.Builder().build();
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("5568E99994901F4C1E938022EB5334EC","3E577FA421751B6DB467B936D6E01D6B")).build();
        MobileAds.setRequestConfiguration(configuration);
        com.google.android.gms.ads.interstitial.InterstitialAd.load(Mediaplayer.this, getResources().getString(R.string.admob_interstitial), adRequest, new InterstitialAdLoadCallback() {
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






    private NativeAd nativeAd;
    private NativeAdLayout nativeAdLayout;
    private com.facebook.ads.NativeAd nativeAd2;
    String nativeads="";
    private Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    try {
                        if (Mediaplayer.this.newPosition >= 0) {
                            Mediaplayer.this.playerView.getPlayer().seekTo((long) ((int) Mediaplayer.this.newPosition));
                            Mediaplayer.this.newPosition = -1;
                            return;
                        }
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                case 4:
                    try {
                        Mediaplayer.this.query.id(R.id.app_video_volume_box).gone();
                        Mediaplayer.this.query.id(R.id.app_video_brightness_box).gone();
                        Mediaplayer.this.query.id(R.id.app_video_fastForward_box).gone();
                        Mediaplayer.this.query.id(R.id.app_video_ratio_box).gone();
                        customVerticalSeekbar.setVisibility(View.GONE);
                        return;
                    } catch (Exception e2) {
                        e2.printStackTrace();
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private TrackGroupArray lastSeenTrackGroupArray;
    private Uri loadedAdTagUri;
    private int mMaxVolume;
    private FrameworkMediaDrm mediaDrm;
    private MediaSource mediaSource;
    private long newPosition = -1;
    boolean orien = false;
    private SimpleExoPlayer player;
    private ZoomableExoPlayerView playerView;
    private Query query;
    private int screenWidthPixels;
    private boolean startAutoPlay;
    private long startPosition;
    private int startWindow;
    private DefaultTrackSelector trackSelector;
    private DefaultTrackSelector.Parameters trackSelectorParameters;
    private int volume = -1;
    String subtitlefile="";
    public static  GestureDetector  gestureDetector;
    private ScaleGestureDetector mScaleDetector;

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener{
        private boolean firstTouch;
        private boolean toSeek;
        private boolean volumeControl;

        public boolean onDoubleTap(MotionEvent e) {

            return true;
        }

        public boolean onDown(MotionEvent e) {
            this.firstTouch = true;

            return super.onDown(e);
        }



        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(!MyUtils.is_zoom){
                boolean z = true;
                float mOldX = e1.getX();
                float deltaY = e1.getY() - e2.getY();
                float deltaX = mOldX - e2.getX();

                if (this.firstTouch) {
                    this.toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                    if (mOldX <= ((float) Mediaplayer.this.screenWidthPixels) * 0.5f) {
                        z = false;
                    }
                    this.volumeControl = z;
                    this.firstTouch = false;
                }

                if (this.toSeek) {
                    Mediaplayer playerActivity = Mediaplayer.this;
                    playerActivity.onProgressSlide((-deltaX) / ((float) playerActivity.playerView.getWidth()));
                } else {
                    float percent = deltaY / ((float) Mediaplayer.this.playerView.getHeight());

                    Log.d("volumeslide",Mediaplayer.this.playerView.getHeight()+"");
                    if (this.volumeControl) {
                        Mediaplayer.this.onVolumeSlide(percent);
                    } else {
                        float percent2 = deltaY / 4500;
                        Mediaplayer.this.onBrightnessSlide(percent2);
                    }
                }
            }
            endGesture();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        public boolean onSingleTapUp(MotionEvent e) {

            return true;
        }
    }

    class Query {
        private final Activity activity;
        private View view;

        public Query(Activity activity) {
            this.activity = activity;
        }

        public Query id(int id) {
            this.view = Mediaplayer.this.findViewById(id);
            return this;
        }

        public Query image(int resId) {
            View view = this.view;
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        @SuppressLint({"WrongConstant"})
        public Query visible() {
            View view = this.view;
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            return this;
        }

        @SuppressLint({"WrongConstant"})
        public Query gone() {
            View view = this.view;
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        @SuppressLint({"WrongConstant"})
        public Query invisible() {
            View view = this.view;
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query text(CharSequence text) {
            View view = this.view;
            if (view != null && (view instanceof TextView)) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            View view = this.view;
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip) {
            try {
                if (this.view != null) {
                    ViewGroup.LayoutParams lp = this.view.getLayoutParams();
                    if (n > 0 && dip) {
                        n = dip2pixel(this.activity, (float) n);
                    }
                    if (width) {
                        lp.width = n;
                    } else {
                        lp.height = n;
                    }
                    this.view.setLayoutParams(lp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void height(int height, boolean dip) {
            try {
                size(false, height, dip);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int dip2pixel(Context context, float n) {
            return (int) TypedValue.applyDimension(1, n, context.getResources().getDisplayMetrics());
        }

        public float pixel2dip(Context context, float n) {
            return n / (((float) context.getResources().getDisplayMetrics().densityDpi) / 160.0f);
        }
    }

    private class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {
        private PlayerErrorMessageProvider() {
        }


        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = Mediaplayer.this.getString(R.string.error_generic);
            if (e.type == 1) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException = (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName != null) {
                        errorString = Mediaplayer.this.getString(R.string.error_instantiating_decoder, new Object[]{decoderInitializationException.decoderName});
                    } else if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                        errorString = Mediaplayer.this.getString(R.string.error_querying_decoders);
                    } else if (decoderInitializationException.secureDecoderRequired) {
                        errorString = Mediaplayer.this.getString(R.string.error_no_secure_decoder, new Object[]{decoderInitializationException.mimeType});
                    } else {
                        errorString = Mediaplayer.this.getString(R.string.error_no_decoder, new Object[]{decoderInitializationException.mimeType});
                    }
                }
            }
            return Pair.create(Integer.valueOf(0), errorString);
        }
    }
boolean autonext=false;
    private class PlayerEventListener implements Player.EventListener {


        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

            if (playbackState == 4) {
                Log.i("plaerstate","change");
                try {
                    Mediaplayer.this.showControls();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
            if (playWhenReady && playbackState == Player.STATE_READY) {
               continueplayer();

            } else if (playWhenReady) {
                // might be idle (plays after prepare()),
                // buffering (plays when data available)
                // or ended (plays when seek away from end)

            } else {
                    pauseplayer();
            }
            Mediaplayer.this.updateButtonVisibilities();
        }



        public void onPositionDiscontinuity(int reason) {
            String media_id=VideoAdapter.videoList.get(Mediaplayer.this.player.getCurrentWindowIndex()).getMedia_id();
            String name=VideoAdapter.videoList.get(Mediaplayer.this.player.getCurrentWindowIndex()).getName();
            Log.i("playername",name);
            try {
                if (Mediaplayer.this.player.getPlaybackError() != null) {

                    Mediaplayer.this.updateStartPosition();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void onPlayerError(ExoPlaybackException e) {
            try {
                if (Mediaplayer.isBehindLiveWindow(e)) {
                    Mediaplayer.this.clearStartPosition();
                    Mediaplayer.this.initializePlayer();
                    return;
                }
                Mediaplayer.this.updateStartPosition();
                Mediaplayer.this.updateButtonVisibilities();
                Mediaplayer.this.showControls();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            try {
                Mediaplayer.this.updateButtonVisibilities();
                if (trackGroups != Mediaplayer.this.lastSeenTrackGroupArray) {
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = Mediaplayer.this.trackSelector.getCurrentMappedTrackInfo();
                    if (mappedTrackInfo != null) {
                        if (mappedTrackInfo.getTypeSupport(2) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                            Mediaplayer.this.showToast((int) R.string.error_unsupported_video);
                        }
                        if (mappedTrackInfo.getTypeSupport(1) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_UNSUPPORTED_TRACKS) {
                            Mediaplayer.this.showToast((int) R.string.error_unsupported_audio);
                        }
                    }
                    Mediaplayer.this.lastSeenTrackGroupArray = trackGroups;


                    String media_id=VideoAdapter.videoList.get(Mediaplayer.this.player.getCurrentWindowIndex()).getMedia_id();
                    tv_title.setText(VideoAdapter.videoList.get(Mediaplayer.this.player.getCurrentWindowIndex()).getName());
                    String action =  getIntent().getAction();
                    if(session_media.getBoolean("is_autoplay",false)) {

                        if (autonext == true  ) {
                            Log.i("plaerstate","stop");

                            player.setPlayWhenReady(false);

                        }else{
                            autonext=true;
                        }
                        if(session_media.getBoolean("is_resume",false)) {
                            if (db.CheckIsVideoView("track_id", media_id)) {
                                player.setPlayWhenReady(false);

                                showResumeDialog(Mediaplayer.this.player.getCurrentWindowIndex());

                            }



                        }
                    }else{
                        if(session_media.getBoolean("is_resume",false)) {
                            if (db.CheckIsVideoView("track_id", media_id)) {
                                player.setPlayWhenReady(false);

                                showResumeDialog(Mediaplayer.this.player.getCurrentWindowIndex());

                            }



                        }

                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static {
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }
    private Context context;

    private float minScale = 1f;
    private float maxScale = 5f;
    private float saveScale = 1f;


    private Matrix matrix = new Matrix();

    private float[] m;

    private PointF last = new PointF();
    private PointF start = new PointF();
    private float right, bottom;
    @SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
    private void GoogleNAtiveAds(NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);
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
    public void GoogleNAtiveShow(final FrameLayout frameLayout) {

        AdSize adSize = AdSize.getInlineAdaptiveBannerAdSize (300, 250);


        AdView bannerView = new AdView(this);
        bannerView.setAdUnitId( getResources().getString(R.string.admob_banner));
        bannerView.setAdSize(adSize);


        AdRequest adRequest = new AdRequest.Builder().build();
        bannerView.loadAd(adRequest);
        frameLayout.removeAllViews();
        frameLayout.addView(bannerView);



    }




    public void pauseplayer(){
        native_ad_container2.setVisibility(View.VISIBLE);
        native_ad_container3.setVisibility(View.VISIBLE);
    }
    public void continueplayer(){
        native_ad_container2.setVisibility(View.INVISIBLE);
        native_ad_container3.setVisibility(View.INVISIBLE);
    }
    FrameLayout native_ad_container2,native_ad_container3;
    ImageView iv_back;
    public void onCreate(Bundle savedInstanceState) {
        try {
            String sphericalStereoMode = getIntent().getStringExtra(SPHERICAL_STEREO_MODE_EXTRA);
            if (sphericalStereoMode != null) {
                setTheme(R.style.PlayerTheme_Spherical);
            }
            super.onCreate(savedInstanceState);
            this.dataSourceFactory = buildDataSourceFactory();
            if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
                CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
            }
            getWindow().setFlags(1024, 1024);
            setContentView(R.layout.activity_mediaplayer);
            this.iv_back = findViewById(R.id.iv_back);
            this.native_ad_container2 = findViewById(R.id.native_ad_container2);
            this.native_ad_container3 = findViewById(R.id.native_ad_container3);
            GoogleNAtiveShow((FrameLayout) findViewById(R.id.native_ad_container2));

            loadMob();

            session_media=getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
            db = new DbHandler(this);

            findViewById(R.id.root).setOnClickListener(this);
            this.debugRootView = (LinearLayout) findViewById(R.id.controls_root);
            this.control_click = (RelativeLayout) findViewById(R.id.rl_tt);
            this.debugTextView = (TextView) findViewById(R.id.debug_text_view);
            this.tv_title = (TextView) findViewById(R.id.tv_title);
            this.root_view = (FrameLayout) findViewById(R.id.root);
            this.playerView =  findViewById(R.id.player_view);
            this.lockedcontrol=(ImageView)findViewById(R.id.locked_control);
            customVerticalSeekbar=(CustomVerticalSeekbar)findViewById(R.id.volume_seekbar);
            customVerticalSeekbar.setVisibility(View.INVISIBLE);
            lockedcontrol.setOnClickListener(this);
            this.setting=(ImageView)findViewById(R.id.setting);
            setting.setOnClickListener(this);
            this.screenshot=(ImageView)findViewById(R.id.screenshot);
            screenshot.setOnClickListener(this);
            this.playerView.setControllerVisibilityListener(this);
            this.playerView.setErrorMessageProvider(new PlayerErrorMessageProvider());
            this.playerView.requestFocus();
            if (sphericalStereoMode != null) {
                int stereoMode;
                if (SPHERICAL_STEREO_MODE_MONO.equals(sphericalStereoMode)) {
                    stereoMode = 0;
                } else if (SPHERICAL_STEREO_MODE_TOP_BOTTOM.equals(sphericalStereoMode)) {
                    stereoMode = 1;
                } else if (SPHERICAL_STEREO_MODE_LEFT_RIGHT.equals(sphericalStereoMode)) {
                    stereoMode = 2;
                } else {
                    showToast((int) R.string.error_unrecognized_stereo_mode);
                    finish();
                    return;
                }
                ((SphericalSurfaceView) this.playerView.getVideoSurfaceView()).setDefaultStereoMode(stereoMode);
            }
            if (savedInstanceState != null) {
                this.trackSelectorParameters = (DefaultTrackSelector.Parameters) savedInstanceState.getParcelable(KEY_TRACK_SELECTOR_PARAMETERS);
                this.startAutoPlay = savedInstanceState.getBoolean(KEY_AUTO_PLAY);
                this.startWindow = savedInstanceState.getInt(KEY_WINDOW);

                this.startPosition = savedInstanceState.getLong(KEY_POSITION);
            } else {
                this.trackSelectorParameters = new DefaultTrackSelector.ParametersBuilder().build();
                clearStartPosition();
            }
            this.btnAspect = (ImageView) findViewById(R.id.btnRatio);
            this.btnAspect.setOnClickListener(this);
            this.btnAudio = (ImageView) findViewById(R.id.btnAudio);
            this.btnVideo = (ImageView) findViewById(R.id.btnVideo);
            this.btnText = (ImageView) findViewById(R.id.btnText);
            try {
                this.query = new Query(this);
                this.screenWidthPixels = getResources().getDisplayMetrics().widthPixels;
                this.audioManager = (AudioManager) getSystemService(MimeTypes.BASE_TYPE_AUDIO);
                try {
                    this.mMaxVolume = this.audioManager.getStreamMaxVolume(3);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {

                gestureDetector = new GestureDetector(this, new PlayerGestureListener());
                this.playerView.setClickable(true);
                this.playerView.setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View view, MotionEvent motionEvent) {

                        int action = motionEvent.getActionMasked();
                        if( (action == MotionEvent.ACTION_DOWN) && (action != MotionEvent.ACTION_POINTER_DOWN)) {
                            Log.i("TouchListner","Single Touch");
                            MyUtils.is_zoom=false;

                            // Single touch
                        }else if ( action == MotionEvent.ACTION_POINTER_DOWN) {
                            //multi touch
                            MyUtils.is_zoom=true;
                            Log.i("TouchListner","multi Touch");


                        }

                        if(gestureDetector.onTouchEvent(motionEvent)) {

                            return true;
                        }
                        if ((motionEvent.getAction() & 255) == 1) {
                            try {
                                Mediaplayer.this.endGesture();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        return false;
                    }
                });
            } catch (Exception e22) {
                e22.printStackTrace();
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


    public void endGesture() {
        try {
            volume = -1;
            brightness = -1.0f;
            if (newPosition >= 0) {
                handler.removeMessages(3);
                handler.sendEmptyMessage(3);
            }
            handler.removeMessages(4);
            handler.sendEmptyMessageDelayed(4, 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onBrightnessSlide(float percent) {
        try {
            if (this.brightness < 0.0f) {
                this.brightness = getWindow().getAttributes().screenBrightness;
                if (this.brightness <= 0.0f) {
                    this.brightness = 0.5f;
                } else if (this.brightness < 0.01f) {
                    this.brightness = 0.01f;
                }
            }
            this.query.id(R.id.app_video_brightness_box).visible();
            WindowManager.LayoutParams lpa = getWindow().getAttributes();
            lpa.screenBrightness = this.brightness + percent;
            if (lpa.screenBrightness > 1.0f) {
                lpa.screenBrightness = 1.0f;
            } else if (lpa.screenBrightness < 0.01f) {
                lpa.screenBrightness = 0.01f;
            }
            Query id = this.query.id(R.id.app_video_brightness);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append((int) (lpa.screenBrightness * 100.0f));
            stringBuilder.append("");
            id.text(stringBuilder.toString());
            getWindow().setAttributes(lpa);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onVolumeSlide(float percent) {

        try {
            if (this.volume == -1) {
                this.volume = this.audioManager.getStreamVolume(3);
                if (this.volume < 0) {
                    this.volume = 0;
                }
            }
            int index = ((int) (((float) this.mMaxVolume) * percent)) + this.volume;
            if (index > this.mMaxVolume) {
                index = this.mMaxVolume;
            } else if (index < 0) {
                index = 0;
            }

            this.audioManager.setStreamVolume(3, index, 0);
            double d = (double) index;
            Double.isNaN(d);
            d *= 1.0d;
            double d2 = (double) this.mMaxVolume;
            Double.isNaN(d2);
            int i = (int) ((d / d2) * 100.0d);
            StringBuilder s = new StringBuilder();
            s.append(i);
            String s1 = s.toString();
            if (i == 0) {
                s1 = "off";
            }
            this.query.id(R.id.app_video_volume_icon).image(i == 0 ? R.drawable.ic_volume_off_white_36dp : R.drawable.ic_volume_up_white_36dp);
            customVerticalSeekbar.setVisibility(View.VISIBLE);
            if (i == 0) {
                this.query.id(R.id.app_video_brightness_box).gone();
                this.query.id(R.id.app_video_ratio_box).gone();
                this.query.id(R.id.app_video_volume_box).visible();
                this.query.id(R.id.app_video_volume).text(s1).visible();
                customVerticalSeekbar.setProgress(0);
                return;
            }

            customVerticalSeekbar.setProgress(Integer.parseInt(s1));
            this.query.id(R.id.app_video_brightness_box).gone();
            this.query.id(R.id.app_video_ratio_box).gone();
            this.query.id(R.id.app_video_volume_box).visible();
            this.query.id(R.id.app_video_volume).text(s1).visible();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onProgressSlide(float percent) {
        try {
            long position = this.playerView.getPlayer().getCurrentPosition();
            long duration = this.playerView.getPlayer().getDuration();
            long delta = (long) (((float) Math.min(5000, duration - position)) * percent);
            this.newPosition = delta + position;
            if (this.newPosition > duration) {
                this.newPosition = duration;
            } else if (this.newPosition <= 0) {
                this.newPosition = 0;
                delta = -position;
            }
            int showDelta = ((int) delta) / 1000;
            if (showDelta != 0) {
                StringBuilder stringBuilder;
                this.query.id(R.id.app_video_fastForward_box).visible();
                Query id = this.query.id(R.id.app_video_fastForward);
                StringBuilder stringBuilder2 = new StringBuilder();
                if (showDelta > 0) {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("+");
                    stringBuilder.append(showDelta);
                } else {
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("");
                    stringBuilder.append(showDelta);
                }
                stringBuilder2.append(stringBuilder.toString());
                stringBuilder2.append("s");
                id.text(stringBuilder2.toString());
                id = this.query.id(R.id.app_video_fastForward_target);
                stringBuilder2 = new StringBuilder();
                stringBuilder2.append(generateTime(this.newPosition));
                stringBuilder2.append("/");
                id.text(stringBuilder2.toString());
                this.query.id(R.id.app_video_fastForward_all).text(generateTime(duration));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        if (totalSeconds / 3600 > 0) {
            return String.format("%02d:%02d:%02d", new Object[]{Integer.valueOf(totalSeconds / 3600), Integer.valueOf(minutes), Integer.valueOf(seconds)});
        }
        return String.format("%02d:%02d", new Object[]{Integer.valueOf(minutes), Integer.valueOf(seconds)});
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        try {
            releasePlayer();
            // releaseAdsLoader();
            clearStartPosition();
            setIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStart() {
        super.onStart();
        try {

            if (Util.SDK_INT > 23) {
                initializePlayer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        try {
         //  continueplayer();
            if (Util.SDK_INT > 23) {
                if (this.player != null) {
                    return;
                }
            }
            initializePlayer();
            if (this.playerView != null) {
                this.playerView.onResume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPause() {
        super.onPause();
        try {
          //  pauseplayer();
            if (Util.SDK_INT <= 23) {
                if (this.playerView != null) {
                    this.playerView.onPause();
                }
                releasePlayer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStop() {
        super.onStop();
        try {
        //  pauseplayer();
            if (Util.SDK_INT > 23) {
                if (this.playerView != null) {
                    this.newPosition = this.playerView.getPlayer().getCurrentPosition();
                    this.playerView.onPause();
                    this.player.release();
                }
                releasePlayer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        try {
        //    releaseAdsLoader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (grantResults.length != 0) {
                if (grantResults[0] == 0) {
                    initializePlayer();
                } else {
                    showToast((int) R.string.storage_permission_denied);
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        try {
            updateTrackSelectorParameters();
            updateStartPosition();
            outState.putParcelable(KEY_TRACK_SELECTOR_PARAMETERS, this.trackSelectorParameters);
            outState.putBoolean(KEY_AUTO_PLAY, this.startAutoPlay);
            outState.putInt(KEY_WINDOW, this.startWindow);
            outState.putLong(KEY_POSITION, this.startPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        return this.playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }
    public  Bitmap takescreenshot(View v) {

        this.playerView.setUseController(false);
        this.debugRootView.setVisibility(View.GONE);
        lockedcontrol.setVisibility(View.GONE);
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        this.playerView.setUseController(true);
        this.debugRootView.setVisibility(View.VISIBLE);
        lockedcontrol.setVisibility(View.VISIBLE);
        return b;
    }

    public void onClick(View view) {
        try {
            if (view.getId() == R.id.btnRatio) {
                if (this.orien) {
                    this.btnAspect.setImageResource(R.drawable.ic_screen_rotation_black_24dp);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    this.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                    this.orien = false;
                    return;
                }
                this.btnAspect.setImageResource(R.drawable.ic_screen_rotation_black_24dp);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                this.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                this.orien = true;
            }

            if (view.getId() == R.id.locked_control) {
                if (!this.is_locked) {
                    this.lockedcontrol.setImageResource(R.drawable.lockvideo);

                    debugRootView.setVisibility(View.GONE);
                    playerView.setUseController(false);
                    this.is_locked = true;
                    lockedcontrol.setVisibility(View.VISIBLE);
                    return;
                }
                this.is_locked = false;
                playerView.setUseController(true);
                this.lockedcontrol.setImageResource(R.drawable.locked);
                debugRootView.setVisibility(View.VISIBLE);
                playerView.showController();

            }


            if (view.getId() == R.id.setting) {

                openOptionMenu(view);
            }
            if (view.getParent() == this.control_click) {
                if (view.getId() == R.id.screenshot) {
                    this.playerView.onPause();
                    View v1 = getWindow().getDecorView().getRootView();
                    //   Bitmap bm= takescreenshot((AspectRatioFrameLayout)findViewById(R.id.exo_content_frame));
                    Bitmap bm= takescreenshot((AspectRatioFrameLayout)findViewById(R.id.exo_content_frame));
                    store_screenshot(this,bm);
                    this.playerView.onResume();


                }
                boolean allowAdaptiveSelections = false;
                if (this.trackSelector.getCurrentMappedTrackInfo() != null) {
                    MappingTrackSelector.MappedTrackInfo mappedTrackInfo = this.trackSelector.getCurrentMappedTrackInfo();
                    if (mappedTrackInfo != null) {
                        Pair<AlertDialog, TrackSelectionView> dialogPair = null;
                        int rendererIndex = ((Integer) view.getTag()).intValue();
                        int rendererType = mappedTrackInfo.getRendererType(rendererIndex);
                        if (rendererType == 2 || (rendererType == 1 && mappedTrackInfo.getTypeSupport(2) == MappingTrackSelector.MappedTrackInfo.RENDERER_SUPPORT_NO_TRACKS)) {
                            allowAdaptiveSelections = true;
                        }
                        if (((ImageView) view).getId() == R.id.btnAudio) {
                            dialogPair = TrackSelectionView.getDialog(this, "Audio", this.trackSelector, rendererIndex);
                        } else if (((ImageView) view).getId() == R.id.btnVideo) {
                            dialogPair = TrackSelectionView.getDialog(this, "Video", this.trackSelector, rendererIndex);
                        } else if (((ImageView) view).getId() == R.id.btnText) {
                            dialogPair = TrackSelectionView.getDialog(this, "Text", this.trackSelector, rendererIndex);
                        }
                        ((TrackSelectionView) dialogPair.second).setShowDisableOption(true);
                        ((TrackSelectionView) dialogPair.second).setAllowAdaptiveSelections(allowAdaptiveSelections);
                        ((AlertDialog) dialogPair.first).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void openOptionMenu(View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.getMenuInflater().inflate(R.menu.player_option, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                int id = item.getItemId();
                if (id == R.id.subtitle) {
                    if(session_media.getBoolean("is_subtitle_show",false)) {
                        File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
                        Filedialog fileDialog = new Filedialog(Mediaplayer.this, mPath, ".srt");
                        fileDialog.addFileListener(new Filedialog.FileSelectedListener() {
                            public void fileSelected(File file) {

                                subtitlefile = file.toString();
                                releasePlayer();
                                initializePlayer();
                            }

                        });
                        fileDialog.showDialog();
                    }else{
                        Toast.makeText(getApplicationContext(),"Enable Subtitle Feature From Settings",Toast.LENGTH_LONG).show();
                    }
                    //fileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
                    //  public void directorySelected(File directory) {
                    //      Log.d(getClass().getName(), "selected dir " + directory.toString());
                    //  }
                    //});
                    //fileDialog.setSelectDirectoryOption(false);

                }

                return true;
            }
        });

        popup.show();

    }
    public static void store_screenshot(Context context,Bitmap bm) {
        File mainDir = new File(
                Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.app_name));

        //If File is not present create directory
        if (!mainDir.exists()) {
            if (mainDir.mkdir())
                Log.e("Create Directory", "Main Directory Created : " + mainDir);
        }
        File dir = new File(mainDir.getAbsolutePath());
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(mainDir.getAbsolutePath(), "screenshot"+System.currentTimeMillis()+".jpeg");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();

            Toast.makeText(context,"Screenshot Saved",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void preparePlayback() {
        initializePlayer();
    }

    public void onVisibilityChange(int visibility) {
        this.debugRootView.setVisibility(visibility);
        lockedcontrol.setVisibility(visibility);
    }

    private void initializePlayer() {
        try {
            if (this.player == null) {
                String url;
                int i;
                Intent intent = getIntent();
                Uri[] uris = new Uri[0];
                String[] extensions = new String[0];
                String action = intent.getAction();
                int i1;
                if ("video_list".equals(action)) {
                    if (VideoAdapter.videoList != null) {
                        String[] uriStrings = new String[VideoAdapter.videoList.size()];
                        this.startWindow = intent.getIntExtra(KEY_POSITION, -1);
                        for (int i0 = 0; i0 < VideoAdapter.videoList.size(); i0++) {
                            if(VideoAdapter.videoList.get(i0)!=null) {
                                uriStrings[i0] = ((VideoModel) VideoAdapter.videoList.get(i0)).getPath();
                            }
                        }
                        uris = new Uri[uriStrings.length];
                        for (i1 = 0; i1 < uriStrings.length; i1++) {
                            uris[i1] = Uri.parse(uriStrings[i1]);
                        }
                        extensions = intent.getStringArrayExtra(EXTENSION_LIST_EXTRA);
                        if (extensions == null) {
                            extensions = new String[uriStrings.length];
                        }
                    } else if (intent.getStringExtra("url") != null) {
                        url = intent.getStringExtra("url");
                        if (url.contains("/external_files/")) {
                            url = url.replaceAll("/external_files/", "/storage/emulated/0/");
                        } else if (url.contains("/root_files/")) {
                            url = url.replaceAll("/root_files/", "");
                        } else if (url.contains("file:///storage/emulated/0/")) {
                            url = url.replaceAll("file:///storage/emulated/0/", "/storage/emulated/0/");
                        } else if (url.contains("file://Download")) {
                            url = url.replaceAll("file://Download", "/storage/emulated/0/Download");
                        } else if (url.contains("file:///storage/emulated/0/Download/")) {
                            url = url.replaceAll("file:///storage/emulated/0/Download/", "/storage/emulated/0/Download");
                        }
                        uris = new Uri[]{Uri.parse(url)};
                        extensions = new String[]{intent.getStringExtra(EXTENSION_EXTRA)};
                    }
                } else if ("video_single".equals(action)) {
                    url = intent.getStringExtra("url");
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("UORIGNAL::");
                    stringBuilder.append(url);
                    Log.v("SINGLEURITAG", stringBuilder.toString());
                    if (url.contains("/external_files/")) {
                        url = url.replaceAll("/external_files/", "/storage/emulated/0/");
                    } else if (url.contains("/root_files/")) {
                        url = url.replaceAll("/root_files/", "");
                    } else if (url.contains("file:///storage/emulated/0/")) {
                        url = url.replaceAll("file:///storage/emulated/0/", "/storage/emulated/0/");
                    } else if (url.contains("file://Download")) {
                        url = url.replaceAll("file://Download", "/storage/emulated/0/Download");
                    } else if (url.contains("file:///storage/emulated/0/Download/")) {
                        url = url.replaceAll("file:///storage/emulated/0/Download/", "/storage/emulated/0/Download");
                    }
                    uris = new Uri[]{Uri.parse(url)};
                    extensions = new String[]{intent.getStringExtra(EXTENSION_EXTRA)};
                }
                if (!Util.checkCleartextTrafficPermitted(uris)) {
                    showToast("not permitted");
                    return;
                } else if (!Util.maybeRequestReadExternalStoragePermission(this, uris)) {
                    TrackSelection.Factory trackSelectionFactory;
                    DrmSessionManager drmSessionManager = null;
                    if (intent.hasExtra(DRM_SCHEME_EXTRA) || intent.hasExtra(DRM_SCHEME_UUID_EXTRA)) {
                        String drmLicenseUrl = intent.getStringExtra(DRM_LICENSE_URL_EXTRA);
                        String[] keyRequestPropertiesArray = intent.getStringArrayExtra(DRM_KEY_REQUEST_PROPERTIES_EXTRA);
                        boolean multiSession = intent.getBooleanExtra(DRM_MULTI_SESSION_EXTRA, false);
                        if (Util.SDK_INT < 18) {
                            i = R.string.error_drm_not_supported;
                        } else {
                            try {
                                UUID drmSchemeUuid = Util.getDrmUuid(intent.getStringExtra(intent.hasExtra(DRM_SCHEME_EXTRA) ? DRM_SCHEME_EXTRA : DRM_SCHEME_UUID_EXTRA));
                                if (drmSchemeUuid == null) {
                                    i = R.string.error_drm_unsupported_scheme;
                                } else {
                                    drmSessionManager = buildDrmSessionManagerV18(drmSchemeUuid, drmLicenseUrl, keyRequestPropertiesArray, multiSession);
                                    i = R.string.error_drm_unknown;
                                }
                            } catch (UnsupportedDrmException e) {
                                i = e.reason == 1 ? R.string.error_drm_unsupported_scheme : R.string.error_drm_unknown;
                            }
                        }
                        if (drmSessionManager == null) {
                            showToast(i);
                            finish();
                            return;
                        }
                    }
                    url = intent.getStringExtra(ABR_ALGORITHM_EXTRA);
                    if (url == null || ABR_ALGORITHM_DEFAULT.equals(url)) {
                        trackSelectionFactory = new AdaptiveTrackSelection.Factory();
                    } else if (ABR_ALGORITHM_RANDOM.equals(url)) {
                        trackSelectionFactory = new RandomTrackSelection.Factory();
                    } else {
                        showToast((int) R.string.error_unrecognized_abr_algorithm);
                        finish();
                        return;
                    }
                    int extensionRendererMode = ((App) getApplication()).useExtensionRenderers() ? intent.getBooleanExtra(PREFER_EXTENSION_DECODERS_EXTRA, false) ? 2 : 1 : 0;

                    RenderersFactory renderersFactory = new DefaultRenderersFactory(this, EXTENSION_RENDERER_MODE_PREFER);
                    this.trackSelector = new DefaultTrackSelector(trackSelectionFactory);
                    this.trackSelector.setParameters(this.trackSelectorParameters);
                    this.lastSeenTrackGroupArray = null;
                    this.player = ExoPlayerFactory.newSimpleInstance(this,new DefaultRenderersFactory(this), this.trackSelector,new DefaultLoadControl());
                    this.player.addListener(new PlayerEventListener());
                    this.player.setPlayWhenReady(this.startAutoPlay);
                    this.player.addAnalyticsListener(new EventLogger(this.trackSelector));
                    this.playerView.setPlayer(this.player);
                    this.playerView.setPlaybackPreparer(this);
                    this.debugViewHelper = new DebugTextViewHelper(this.player, this.debugTextView);
                    this.debugViewHelper.start();

                    MediaSource[] mediaSources = new MediaSource[uris.length];
                    for (int i2 = 0; i2 < uris.length; i2++) {
                        if(i2 == startWindow && !subtitlefile.equalsIgnoreCase("")){
                            Toast.makeText(getApplicationContext(),"subtitlein",Toast.LENGTH_LONG).show();
                            MediaSource mm   = buildMediaSource(uris[i2]);

                            Format textFormat = Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,
                                    null, Format.NO_VALUE, Format.NO_VALUE, "en", null, Format.OFFSET_SAMPLE_RELATIVE);
                            DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory();
                            MediaSource textMediaSource = new SingleSampleMediaSource.Factory(mediaDataSourceFactory)
                                    .createMediaSource(Uri.parse(subtitlefile), textFormat, C.TIME_UNSET);
                            mediaSources[i2] = new MergingMediaSource(mm, textMediaSource);
                        }else{
                            mediaSources[i2] = buildMediaSource(uris[i2], extensions[i2]);
                        }



                    }
                    this.mediaSource = mediaSources.length == 1 ? mediaSources[0] : new ConcatenatingMediaSource(mediaSources);
//                    String adTagUriString = intent.getStringExtra(AD_TAG_URI_EXTRA);
//                    if (adTagUriString != null) {
//                        Uri adTagUri = Uri.parse(adTagUriString);
//                        if (!adTagUri.equals(this.loadedAdTagUri)) {
//                            releaseAdsLoader();
//                            this.loadedAdTagUri = adTagUri;
//                        }
//                        MediaSource adsMediaSource = createAdsMediaSource(this.mediaSource, Uri.parse(adTagUriString));
//                        if (adsMediaSource != null) {
//                            this.mediaSource = adsMediaSource;
//                        } else {
//                            //  showToast((int) R.string.ima_not_loaded);
//                        }
//
//                    } else {
//                        releaseAdsLoader();
//                    }
                } else {
                    return;
                }
            }
            this.tv_title.setText(VideoAdapter.videoList.get(startWindow
            ).getName());
            String media_id=VideoAdapter.videoList.get(startWindow
            ).getMedia_id();

//            if(subtitlefile.equalsIgnoreCase("")){
//            if(session_media.getBoolean("is_resume",false)) {
//                if (db.CheckIsVideoView("track_id", media_id)) {
//                    this.player.setPlayWhenReady(false);
//
//                    showResumeDialog(startWindow);
//
//                }
//
//            }
//
//            }


            boolean haveStartPosition = this.startWindow != -1;
            if (haveStartPosition) {
                this.player.seekTo(this.startWindow, this.startPosition);
            }
            this.player.prepare(this.mediaSource, !haveStartPosition, false);

            updateButtonVisibilities();
        } catch (Exception e22) {
            e22.printStackTrace();
        }
    }
    public void showResumeDialog(int startwindow) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you wish to resume from where you stopped?")
                .setCancelable(false)

                .setIcon(R.mipmap.ic_launcher)
                .setTitle(VideoAdapter.videoList.get(startwindow).getName())
                .setPositiveButton("Start Over", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        player.setPlayWhenReady(true);
                        alert.dismiss();
                    }
                })
                .setNegativeButton("Resume", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startPosition= db.get_single_video_last_view("track_id",VideoAdapter.videoList.get(startwindow).getMedia_id());
                        player.setPlayWhenReady(true);
                        player.seekTo(startwindow,startPosition);
                        alert.dismiss();

                    }
                });
        alert = builder.create();
        alert.show();

    }

    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }

    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension) {
        int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case 0:
                return new DashMediaSource.Factory(this.dataSourceFactory).setManifestParser(new FilteringManifestParser(new DashManifestParser(), getOfflineStreamKeys(uri))).createMediaSource(uri);
            case 1:
                return new SsMediaSource.Factory(this.dataSourceFactory).setManifestParser(new FilteringManifestParser(new SsManifestParser(), getOfflineStreamKeys(uri))).createMediaSource(uri);
            case 2:
                return new HlsMediaSource.Factory(this.dataSourceFactory).setPlaylistParserFactory(new DefaultHlsPlaylistParserFactory(getOfflineStreamKeys(uri))).createMediaSource(uri);
            case 3:
                return new ExtractorMediaSource.Factory(this.dataSourceFactory).createMediaSource(uri);
            default:
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unsupported type: ");
                stringBuilder.append(type);
                throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private List<StreamKey> getOfflineStreamKeys(Uri uri) {
        return ((App) getApplication()).getDownloadTracker().getOfflineStreamKeys(uri);
    }

    private DefaultDrmSessionManager<FrameworkMediaCrypto> buildDrmSessionManagerV18(UUID uuid, String licenseUrl, String[] keyRequestPropertiesArray, boolean multiSession) throws UnsupportedDrmException {
        HttpMediaDrmCallback drmCallback = new HttpMediaDrmCallback(licenseUrl, ((App) getApplication()).buildHttpDataSourceFactory());
        if (keyRequestPropertiesArray != null) {
            for (int i = 0; i < keyRequestPropertiesArray.length - 1; i += 2) {
                drmCallback.setKeyRequestProperty(keyRequestPropertiesArray[i], keyRequestPropertiesArray[i + 1]);
            }
        }
        releaseMediaDrm();
        this.mediaDrm = FrameworkMediaDrm.newInstance(uuid);
        return new DefaultDrmSessionManager(uuid, this.mediaDrm, drmCallback, null, multiSession);
    }

    private void releasePlayer() {
        try {

            if (this.player != null) {
                updateTrackSelectorParameters();
                updateStartPosition();
                this.debugViewHelper.stop();
                this.debugViewHelper = null;
                this.player.release();
                this.player = null;
                this.mediaSource = null;
                this.trackSelector = null;
            }
            releaseMediaDrm();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseMediaDrm() {
        try {
            if (this.mediaDrm != null) {
                this.mediaDrm.release();
                this.mediaDrm = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseAdsLoader() {
        try {
            if (this.adsLoader != null) {
                this.adsLoader.release();
                this.adsLoader = null;
                this.loadedAdTagUri = null;
                this.playerView.getOverlayFrameLayout().removeAllViews();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTrackSelectorParameters() {
        try {
            if (this.trackSelector != null) {
                this.trackSelectorParameters = this.trackSelector.getParameters();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStartPosition() {
        try {
            if (this.player != null) {
                this.startAutoPlay = this.player.getPlayWhenReady();
                this.startWindow = this.player.getCurrentWindowIndex();
                Bundle extras = getIntent().getExtras();

                this.startPosition = Math.max(0, this.player.getContentPosition());
                String media_id=VideoAdapter.videoList.get(startWindow).getMedia_id();
                if (getIntent().hasExtra("destination") && extras!= null &&  extras.getString("destination").equals("locked")) {
                }else{
    if (db.CheckIsVideoView("track_id", media_id)) {
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        db.update_video_last_view(startPosition, media_id, ts);
    } else {
        db.insert_video_last_view(startPosition, media_id);
    }
}
                if (!db.CheckIsVideoexits("track_id", media_id)) {

                    db.insert_all_video(media_id, VideoAdapter.videoList.get(startWindow).getName(), VideoAdapter.videoList.get(startWindow).getDuration());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearStartPosition() {
        try {
            this.startAutoPlay = true;
            this.startWindow = -1;
            this.startPosition = C.TIME_UNSET;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataSource.Factory buildDataSourceFactory() {
        return ((App) getApplication()).buildDataSourceFactory();
    }

//    @Nullable
//    private MediaSource createAdsMediaSource(MediaSource mediaSource, Uri adTagUri) {
//        try {
//            Class<?> loaderClass = Class.forName("com.google.android.exoplayer2.ext.ima.ImaAdsLoader");
//            if (this.adsLoader == null) {
//                this.adsLoader = (AdsLoader) loaderClass.asSubclass(AdsLoader.class).getConstructor(new Class[]{Context.class, Uri.class}).newInstance(new Object[]{this, adTagUri});
//                this.adUiViewGroup = new FrameLayout(this);
//                this.playerView.getOverlayFrameLayout().addView(this.adUiViewGroup);
//            }
//            return new AdsMediaSource(mediaSource, new AdsMediaSource.MediaSourceFactory() {
//                public MediaSource createMediaSource(Uri uri) {
//                    return Mediaplayer.this.buildMediaSource(uri);
//                }
//
//                public int[] getSupportedTypes() {
//                    return new int[]{0, 1, 2, 3};
//                }
//            }, this.adsLoader, this.adUiViewGroup);
//        } catch (ClassNotFoundException e) {
//            return null;
//        } catch (Exception e2) {
//            throw new RuntimeException(e2);
//        }
//    }

    @SuppressLint({"WrongConstant"})
    private void updateButtonVisibilities() {
        try {
            this.btnVideo.setVisibility(View.GONE);
            this.btnAudio.setVisibility(View.GONE);
            this.btnText.setVisibility(View.GONE);
            if (this.player != null) {
                MappingTrackSelector.MappedTrackInfo mappedTrackInfo = this.trackSelector.getCurrentMappedTrackInfo();
                if (mappedTrackInfo != null) {
                    for (int i = 0; i < mappedTrackInfo.length; i++) {
                        if (mappedTrackInfo.getTrackGroups(i).length != 0) {
                            switch (this.player.getRendererType(i)) {
                                case 1:
                                    try {
                                        this.btnAudio.setTag(Integer.valueOf(i));
                                        this.btnAudio.setVisibility(View.VISIBLE);
                                        this.btnAudio.setOnClickListener(this);
                                        continue;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                case 2:
                                    try {
                                        this.btnVideo.setTag(Integer.valueOf(i));
                                        this.btnVideo.setOnClickListener(this);
                                        this.btnVideo.setVisibility(View.GONE);
                                        continue;
                                    } catch (Exception e2) {
                                        e2.printStackTrace();
                                        break;
                                    }
                                case 3:
                                    break;
                                default:
                                    break;
                            }
                            try {
                                this.btnText.setTag(Integer.valueOf(i));
                                this.btnText.setOnClickListener(this);
                                if(session_media.getBoolean("is_subtitle_show",false)) {
                                    this.btnText.setVisibility(View.VISIBLE);

                                }else{
                                    Mediaplayer.this.query.id(R.id.exo_subtitles).gone();
                                }
                            } catch (Exception e22) {
                                e22.printStackTrace();
                            }


                        }


                    }

//                    if(btnText.getVisibility()==8){
//                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) btnAudio.getLayoutParams();
//                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//                        params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
//                        params.removeRule(RelativeLayout.LEFT_OF);
//                        btnAudio.setLayoutParams(params);
//                    }
                }
            }
        } catch (Exception e3) {
            e3.printStackTrace();
        }
    }

    private void showControls() {
        try {
            this.debugRootView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(int messageId) {
        try {
            showToast(getString(messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToast(String message) {
        try {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void ShowInterstitalAds() {
    if (this.mInterstitialAdMob != null) {
            this.mInterstitialAdMob.show(Mediaplayer.this);

        }
    }
    private static boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != 0) {
            return false;
        }
        for (Throwable cause = e.getSourceException(); cause != null; cause = cause.getCause()) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        updateStartPosition();
        ShowInterstitalAds();
        super.onBackPressed();

        
        
        
        
    }
}