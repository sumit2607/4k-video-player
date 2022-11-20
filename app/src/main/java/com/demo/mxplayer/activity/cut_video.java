package com.demo.mxplayer.activity;



import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.demo.mxplayer.R;
import com.demo.mxplayer.adapter.VideoAdapter;
import com.demo.mxplayer.models.VideoModel;
import com.demo.mxplayer.player.Mediaplayer;
import com.demo.mxplayer.utils.MyUtils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.apache.commons.io.FilenameUtils;
import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class cut_video extends AppCompatActivity {

    private int theme;
    private Context context;
    private SharedPreferences sharedPreferences;
    private RangeSeekBar rangeSeekBar;
    private VideoView videoView;
    private FFmpeg ffmpeg;
    private ProgressDialog progressDialog;
    private Uri selectedVideoUri;
    private static final String FILEPATH = "filepath";
    private static final String TAG = "cut_video";
    private int duration;
    private int stopPosition;
    private TextView tvLeft, tvRight;
    private Runnable r;
    Button cutVideo;
    String realpath;
    String filename;
    String outputPath;
    private VideoAdapter adapter;
    public static List<VideoModel> videoModelList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=cut_video.this;
        theme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cut_video_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cut Video");
        setSupportActionBar(toolbar);

        loadFFMpegBinary();
        videoView = (VideoView) findViewById(R.id.videoView1);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        realpath=getIntent().getExtras().getString("videofilename");
        filename=realpath.substring(realpath.lastIndexOf("/")+1);
        selectedVideoUri=Uri.parse(realpath);
        videoView.setVideoURI(selectedVideoUri);
        videoModelList = new ArrayList<>();
        MediaController mediaController = new
                MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.start();
        tvLeft = (TextView) findViewById(R.id.left_pointer);
        tvRight = (TextView) findViewById(R.id.right_pointer);
        cutVideo = (Button) findViewById(R.id.trimbut);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO Auto-generated method stub
                duration = mp.getDuration() / 1000;
                tvLeft.setText("00:00:00");

                tvRight.setText(getTime(mp.getDuration() / 1000));
                mp.setLooping(true);
                rangeSeekBar.setRangeValues(0, duration);
                rangeSeekBar.setSelectedMinValue(0);
                rangeSeekBar.setSelectedMaxValue(duration);
                rangeSeekBar.setEnabled(true);
                progressDialog = new ProgressDialog(cut_video.this);
                progressDialog.setTitle(null);
                progressDialog.setCancelable(false);
                rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                    @Override
                    public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                        videoView.seekTo((int) minValue * 1000);

                        tvLeft.setText(getTime((int) bar.getSelectedMinValue()));

                        tvRight.setText(getTime((int) bar.getSelectedMaxValue()));

                    }
                });

                final Handler handler = new Handler();
                handler.postDelayed(r = new Runnable() {
                    @Override
                    public void run() {

                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        handler.postDelayed(r, 1000);
                    }
                }, 1000);

            }
        });
        cutVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);

            }
        });

    }
    public void theme() {
        sharedPreferences = getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        theme = sharedPreferences.getInt("THEME", 0);
        MyUtils.settingTheme(context, theme);
    }
    /**
     * Command for cutting video
     */
    private void executeCutVideoCommand(int startMs, int endMs) {
        String basename = FilenameUtils.getBaseName(realpath);
        String yourRealPath =realpath;
         outputPath = outputpath(this);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(basename+"_");
        stringBuilder.append(System.currentTimeMillis());
        stringBuilder.append(".mp4");
        String stringBuilder2 = stringBuilder.toString();
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append(outputPath);
        stringBuilder3.append(File.separator);
        stringBuilder3.append(stringBuilder2);
        outputPath = stringBuilder3.toString();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};
        String[] complexCommand = {"-ss", "" + startMs / 1000, "-y", "-i", yourRealPath, "-t", "" + (endMs - startMs) / 1000,"-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", outputPath};

        execFFmpegBinary(complexCommand);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : loading");
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.d(TAG, "ffmpeg :error : ");

                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {

        } catch (Exception e) {
            Log.d(TAG, "EXception no controlled : " + e);
        }
    }
    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    VideoAdapter.videoList.clear();
                    Log.d(TAG, "SUCCESS with output : " + s);
                    VideoModel video = new VideoModel(0, filename, outputPath, "", false,"","","","");
                    VideoAdapter.videoList.add(video);
                    Intent intent = new Intent(cut_video.this, Mediaplayer.class);
                    intent.setAction("video_list");
                    intent.putExtra("position", 0);
                    intent.putExtra("tag", "1");
                    intent.putExtra("destination", "cutvideo");
                    startActivity(intent);

                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "progress : " + s);
                }
                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {

        }
    }


    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopPosition = videoView.getCurrentPosition(); //stopPosition is an int
        videoView.pause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // videoView.seekTo(stopPosition);
       // videoView.start();
    }
    public static String outputpath(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory());
        stringBuilder.append(File.separator);
        stringBuilder.append(getAppName(context)+" "+"Cut Video");

        String stringBuilder2 = stringBuilder.toString();
        File file = new File(stringBuilder2);
        if (!file.exists()) {
            //  file.mkdir();
            if (file.mkdirs())
            {

            }
            else
            {

            }
        }
        return stringBuilder2;
    }
    public static String getAppName(Context context) {
        return context.getString(R.string.app_name);
    }
}
