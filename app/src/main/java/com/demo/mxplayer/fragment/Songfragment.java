package com.demo.mxplayer.fragment;
import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.demo.mxplayer.R;
import com.demo.mxplayer.db.DbHandler;
import com.demo.mxplayer.models.Song;
import com.demo.mxplayer.playback.MediaPlayerHolder;
import com.demo.mxplayer.playback.MusicNotificationManager;
import com.demo.mxplayer.playback.MusicService;
import com.demo.mxplayer.playback.PlaybackInfoListener;
import com.demo.mxplayer.playback.PlayerAdapter;
import com.demo.mxplayer.adapter.RecyclerAdapter;
import com.demo.mxplayer.utils.MyUtils;
import com.demo.mxplayer.utils.SongProvider;
import com.demo.mxplayer.utils.Utils;
import com.demo.mxplayer.utils.songtime;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;




public class Songfragment extends Fragment implements View.OnClickListener,
        RecyclerAdapter.SongClicked {

    private RecyclerView recyclerView;
    private SeekBar seekBar,seekBar2;
    private ImageButton playPause, next, previous;
    ImageView playPause2,next2,previous2,btn_suffel,btn_toggle;
    private TextView songTitle,textViewArtistName,songTitles,textViewArtistNames,starttime,endtime,endtimes,starttimes;
    private MusicService mMusicService;
    private Boolean mIsBound;
    private PlayerAdapter mPlayerAdapter;
    private boolean mUserIsSeeking = false;
    private PlaybackListener mPlaybackListener;
    private List<Song> mSelectedArtistSongs;
    View include;
    private MusicNotificationManager mMusicNotificationManager;
    private RecyclerAdapter recyclerAdapter;
    private Song mSelectedSong;
    private SlidingUpPanelLayout mLayout;
    ImageView track_image,track_images;
    DbHandler db;
    ConstraintLayout toplayout;
    com.demo.mxplayer.utils.songtime songtime;
    private Handler hdlr = new Handler();
    private int duration;
    RelativeLayout adContainerView;
    com.facebook.ads.AdView adView;
    AdView adViewone;
    String AdaptuveBannerAdmob;

    String TAG = "bannerload";
    RelativeLayout adContainer;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            mMusicService = ((MusicService.LocalBinder) iBinder).getInstance();
            mPlayerAdapter = mMusicService.getMediaPlayerHolder();
            mMusicNotificationManager = mMusicService.getMusicNotificationManager();

            if (mPlaybackListener == null) {
                mPlaybackListener = new PlaybackListener();
                mPlayerAdapter.setPlaybackInfoListener(mPlaybackListener);
            }
            if (mPlayerAdapter != null && mPlayerAdapter.isPlaying()) {

                restorePlayerStatus();
            }
            checkReadStoragePermissions();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMusicService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_song, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        doBindService();
        setViews(view);

        initializeSeekBar();
        songtime = new songtime();
    }

    private AdSize BannerGetSize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getActivity(), adWidth);
    }

    public void CommonBAnner(View view) {
        adContainerView = view.findViewById(R.id.adMobView);
        adViewone = new AdView(getActivity());
        adViewone.setAdUnitId(getResources().getString(R.string.admob_banner));
        adContainerView.addView(adViewone);
        BannerLoad();
    }

    private void BannerLoad() {
        AdRequest adRequest = new AdRequest.Builder().build();
        AdSize adSize = BannerGetSize();
        adViewone.setAdSize(adSize);
        adViewone.loadAd(adRequest);
    }
    @Override
    public void onPause() {
        super.onPause();

        doUnbindService();
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {
            mPlayerAdapter.onPauseActivity();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        doBindService();
        if (mPlayerAdapter != null && mPlayerAdapter.isPlaying()) {

            restorePlayerStatus();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private void setViews(View view) {
        db = new DbHandler(getContext());
        track_image = view.findViewById(R.id.track_image);
        track_images = view.findViewById(R.id.track_images);
        playPause = view.findViewById(R.id.buttonPlayPause);
        starttime = view.findViewById(R.id.starttime);
        starttimes = view.findViewById(R.id.starttimes);
        endtime = view.findViewById(R.id.endtime);
        endtimes = view.findViewById(R.id.endtimes);
        next = view.findViewById(R.id.buttonNext);
        previous = view.findViewById(R.id.buttonPrevious);
        seekBar = view.findViewById(R.id.seekBar);

        recyclerView = view.findViewById(R.id.recyclerView);
        songTitle = view.findViewById(R.id.songTitle);
        textViewArtistName = view.findViewById(R.id.textViewArtistName);
        songTitles = view.findViewById(R.id.songTitles);
        textViewArtistNames = view.findViewById(R.id.textViewArtistNames);
          include = (View)view. findViewById(R.id.bottom_palyLayout);
        playPause2 = view.findViewById(R.id.buttonPlayPause2);
        next2 = view.findViewById(R.id.buttonNext2);
        previous2 = view.findViewById(R.id.buttonPrevious2);
        seekBar2 = view.findViewById(R.id.seekBar2);
        btn_suffel = view.findViewById(R.id.btn_suffel);
        btn_toggle = view.findViewById(R.id.btn_toggle);
        btn_suffel.setOnClickListener(this);
        btn_toggle.setOnClickListener(this);
        //To listen to clicks
        mLayout = (SlidingUpPanelLayout)view.findViewById(R.id.sliding_layout);
        toplayout=view.findViewById(R.id.toplayout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i("songfragment", "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i("songfragment", "onPanelStateChanged " + newState);
                if(newState==SlidingUpPanelLayout.PanelState.EXPANDED){
                    toplayout.setVisibility(View.GONE);
                    CommonBAnner(view);
                }
                if(newState==SlidingUpPanelLayout.PanelState.COLLAPSED){
                    toplayout.setVisibility(View.VISIBLE);

                }
            }
        });
        if (mLayout != null) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }
        playPause.setOnClickListener(this);
        next.setOnClickListener(this);
        previous.setOnClickListener(this);

        playPause2.setOnClickListener(this);
        next2.setOnClickListener(this);
        previous2.setOnClickListener(this);
        //set adapter
        recyclerAdapter = new RecyclerAdapter(this,getContext());
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        //get songs
        mSelectedArtistSongs = SongProvider.getAllDeviceSongs(getActivity());
        recyclerAdapter.addSongs((ArrayList) mSelectedArtistSongs);
        btn_suffel.setSelected(MyUtils.getShuffel(getContext()));
        MediaPlayerHolder.shuffleMusic = btn_suffel.isSelected() ? true : false;
        MyUtils.changeColorSet(getContext(), (ImageView) btn_suffel, btn_suffel.isSelected());

        btn_toggle.setSelected((MyUtils.getRepeat(getContext()) == 1) ? true : false);
        MediaPlayerHolder.repeatMode = btn_toggle.isSelected() ? 1 : 0;
        MyUtils.changeColorSet(getContext(), (ImageView) btn_toggle, btn_toggle.isSelected());

        SongProvider.shuffleList(mSelectedArtistSongs);


    }

    private void checkReadStoragePermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void updatePlayingInfo(boolean restore, boolean startPlay) {

        if (startPlay) {
            mPlayerAdapter.getMediaPlayer().start();
            new Handler().postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    mMusicService.startForeground(MusicNotificationManager.NOTIFICATION_ID,
                            mMusicNotificationManager.createNotification());
                }
            }, 250);
        }

        final Song selectedSong = mPlayerAdapter.getCurrentSong();

        songTitle.setText(selectedSong.title);
        textViewArtistName.setText(selectedSong.artistName);
        songTitles.setText(selectedSong.title);
        textViewArtistNames.setText(selectedSong.artistName);
        duration = selectedSong.duration;
        endtime.setText(""+songtime.milliSecondsToTimer(duration));
        endtimes.setText(""+songtime.milliSecondsToTimer(duration));
//        final int currentduration = mPlayerAdapter.getPlayerPosition();
//       starttime.setText(""+songtime.milliSecondsToTimer(currentduration));

        seekBar.setMax(duration);
        seekBar2.setMax(duration);

        starttime.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(duration))) );
        starttimes.setText(String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS. toMinutes(duration))) );
        if (restore) {
            seekBar.setProgress(mPlayerAdapter.getPlayerPosition());
            seekBar2.setProgress(mPlayerAdapter.getPlayerPosition());
            updatePlayingStatus();


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //stop foreground if coming from pause state
                    if (mMusicService.isRestoredFromPause()) {
                        mMusicService.stopForeground(false);
                        mMusicService.getMusicNotificationManager().getNotificationManager()
                                .notify(MusicNotificationManager.NOTIFICATION_ID,
                                        mMusicService.getMusicNotificationManager().getNotificationBuilder().build());
                        mMusicService.setRestoredFromPause(false);
                    }
                }
            }, 250);


        }
        hdlr.postDelayed(UpdateSongTime, 50);
    }
    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            if(mPlayerAdapter.getMediaPlayer()!=null) {
                duration = mPlayerAdapter.getMediaPlayer().getCurrentPosition();
                starttime.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
                starttimes.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))));
                seekBar.setProgress((int) duration);
                seekBar2.setProgress((int) duration);
                hdlr.postDelayed(this, 50);
            }
        }

    };

    private void updatePlayingStatus() {
        final int drawable = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ?
                R.drawable.ic_pause : R.drawable.ic_play;
        final int drawableone = mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED ?
                R.drawable.ic_pause_one : R.drawable.ic_play_one;
        playPause.post(new Runnable() {
            @Override
            public void run() {
                playPause.setImageResource(drawable);
            }
        });
        playPause2.post(new Runnable() {
            @Override
            public void run() {
                playPause2.setImageResource(drawableone);
            }
        });
    }

    private void restorePlayerStatus() {
        seekBar.setEnabled(mPlayerAdapter.isMediaPlayer());
        seekBar2.setEnabled(mPlayerAdapter.isMediaPlayer());
        //if we are playing and the activity was restarted
        //update the controls panel
        if (mPlayerAdapter != null && mPlayerAdapter.isMediaPlayer()) {

            mPlayerAdapter.onResumeActivity();
            updatePlayingInfo(true, false);
        }
    }


    private void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        getActivity().bindService(new Intent(getActivity(),
                MusicService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;

//        final Intent startNotStickyIntent = new Intent(getActivity(), MusicService.class);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            getActivity().startForegroundService(startNotStickyIntent);
//        } else {
//            getActivity().startService(startNotStickyIntent);
//        }
    }

    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            getActivity().unbindService(mConnection);
            mIsBound = false;
        }
    }

    public void onSongSelected(@NonNull final Song song, @NonNull final List<Song> songs) {
        if (mLayout != null) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        mSelectedSong = song;
        track_image.setImageBitmap(Utils.songArt1(song.path, getContext()));
        track_images.setImageBitmap(Utils.songArt1(song.path, getContext()));
        if (!seekBar.isEnabled()) {
            seekBar.setEnabled(true);
        }
        if (!seekBar2.isEnabled()) {
            seekBar2.setEnabled(true);
        }
        try {
            mPlayerAdapter.setCurrentSong(song, songs);
            mPlayerAdapter.initMediaPlayer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void skipNext() {
        if (checkIsPlayer()) {
            mPlayerAdapter.skip(true);
        }
    }
    public void skipPrev() {
        if (checkIsPlayer()) {
            mPlayerAdapter.instantReset();
        }
    }

    public void resumeOrPause() {
        if (checkIsPlayer()) {
            mPlayerAdapter.resumeOrPause();
        }
    }
    private boolean checkIsPlayer() {

        boolean isPlayer = mPlayerAdapter.isMediaPlayer();
        if (!isPlayer) {

                Toast.makeText(getActivity(), "Play a Song first", Toast.LENGTH_SHORT).show();

        }
        return isPlayer;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case (R.id.buttonPlayPause): {
                resumeOrPause();
                break;

            }
            case (R.id.buttonNext): {
                skipNext();
                break;
            }
            case (R.id.buttonPrevious): {
                skipPrev();
                break;
            }

            case (R.id.buttonPlayPause2): {
                resumeOrPause();
                break;

            }
            case (R.id.buttonNext2): {
                skipNext();
                break;
            }
            case (R.id.buttonPrevious2): {
                skipPrev();
                break;
            }
            case (R.id.btn_suffel): {
                v.setSelected(v.isSelected() ? false : true);
                MediaPlayerHolder.shuffleMusic = v.isSelected() ? true : false;
                SongProvider.shuffleList(mSelectedArtistSongs);
                MyUtils.setShuffel(getContext()
                        , (v.isSelected() ? true : false));
                MyUtils.changeColorSet(getContext(), (ImageView) v, v.isSelected());
              //  skipPrev();
                break;
            }
            case (R.id.btn_toggle): {
                v.setSelected(v.isSelected() ? false : true);
               MediaPlayerHolder.repeatMode = v.isSelected() ? 1 : 0;
                MyUtils.setRepeat(getContext(), (v.isSelected() ? 1 : 0));
                MyUtils.changeColorSet(getContext(), (ImageView) v, v.isSelected());

                break;


            }
        }
    }


    private void initializeSeekBar() {
        seekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        if (fromUser) {
                            duration = progress;

                        }

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        if (mUserIsSeeking) {

                        }
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(duration);

                    }

                });

        seekBar2.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {


                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                        if (fromUser) {
                            duration = progress;

                        }

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                        if (mUserIsSeeking) {

                        }
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(duration);

                    }
                });

    }


    @Override
    public void onSongClicked(Song song) {
       // Toast.makeText(getActivity(),song.albumName,Toast.LENGTH_LONG).show();
  onSongSelected(song, mSelectedArtistSongs);
    }


    class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                seekBar.setProgress(position);
                seekBar2.setProgress(position);
            }
        }

        @Override
        public void onStateChanged(@State int state) {

            updatePlayingStatus();
            if (mPlayerAdapter.getState() != State.RESUMED && mPlayerAdapter.getState() != PlaybackInfoListener.State.PAUSED) {
                updatePlayingInfo(false, true);
            }
        }

        @Override
        public void onPlaybackCompleted() {


        }


    }


}
