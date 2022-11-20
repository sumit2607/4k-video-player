package com.demo.mxplayer.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.demo.mxplayer.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public  class Utils {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Bitmap songArt (String path, Context context){
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        InputStream inputStream;
        retriever.setDataSource(path);
        if (retriever.getEmbeddedPicture()!=null){
            inputStream= new ByteArrayInputStream(retriever.getEmbeddedPicture());
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            retriever.release();
            return bitmap;
        }else {
            return getLargeIcon(context);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static Bitmap getLargeIcon(Context context) {

        final VectorDrawable vectorDrawable = (VectorDrawable)context.getDrawable(R.drawable.music_notification);

        final int largeIconSize = context.getResources().getDimensionPixelSize(R.dimen.notification_large_dim);
        final Bitmap bitmap = Bitmap.createBitmap(largeIconSize, largeIconSize, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);

        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.setAlpha(100);
            vectorDrawable.draw(canvas);
        }

        return bitmap;
    }




    public static Bitmap songArt1 (String path,Context context){
        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        InputStream inputStream;
        retriever.setDataSource(path);
        if (retriever.getEmbeddedPicture()!=null){
            inputStream= new ByteArrayInputStream(retriever.getEmbeddedPicture());
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            retriever.release();
            return bitmap;
        }else {
            return getLargeIcon1(context);
        }
    }

    private static Bitmap getLargeIcon1(Context context) {

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.track_image);
        return bitmap;
    }

}
