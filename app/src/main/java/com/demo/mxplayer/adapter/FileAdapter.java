package com.demo.mxplayer.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.demo.mxplayer.R;
import com.demo.mxplayer.models.SingleFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileAdapter extends ArrayAdapter<SingleFile> {
    public static final String TAG = "FileAdapter";
    LayoutInflater inflater;
    Context mContext;
    ArrayList<SingleFile> records;
    int resId;
    SimpleDateFormat sdf = new SimpleDateFormat("d MMM, y");

    public FileAdapter(Context context, int i, List<SingleFile> list) {
        super(context, i, list);
        this.records = (ArrayList) list;
        this.resId = i;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.inflater.inflate(this.resId, null);
        }
        SingleFile singleFile = (SingleFile) this.records.get(i);
        if (singleFile.isSelected) {
            view.setBackgroundColor(Color.parseColor("#EAEAEA"));
        } else {
            view.setBackgroundColor(0);
        }
        String substring = singleFile.title.substring(singleFile.title.lastIndexOf(46) + 1);
        if (substring.length() > 4) {
            substring = substring.substring(0, 4);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".");
        stringBuilder.append(substring);
        ((TextView) view.findViewById(R.id.lbl_file_ext)).setText(stringBuilder.toString());
        ((TextView) view.findViewById(R.id.lbl_file_title)).setText(singleFile.title);
        ((TextView) view.findViewById(R.id.lbl_file_size)).setText(String.valueOf(singleFile.sizeString));
        ((TextView) view.findViewById(R.id.lbl_file_created)).setText(this.sdf.format(singleFile.created));
        return view;
    }

    public void refreshRecords(ArrayList<SingleFile> arrayList) {
        this.records = arrayList;
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.records.size();
    }

    public SingleFile getItem(int i) {
        return (SingleFile) this.records.get(i);
    }

    public long getItemId(int i) {
        return (long) ((SingleFile) this.records.get(i)).id;
    }

    public Bitmap getThumbnail(ContentResolver contentResolver, String str) {
        String str2 = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getThumbnail : ");
        stringBuilder.append(str);
        Log.i(str2, stringBuilder.toString());
        Bitmap createVideoThumbnail = ThumbnailUtils.createVideoThumbnail(str, 1);
        if (createVideoThumbnail != null) {
            return createVideoThumbnail;
        }
        return BitmapFactory.decodeResource(this.mContext.getResources(), R.mipmap.ic_launcher);
    }
}
