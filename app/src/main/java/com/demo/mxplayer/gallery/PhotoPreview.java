package com.demo.mxplayer.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.demo.mxplayer.gallery.helpercomponent.GestureImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import com.demo.mxplayer.R;
import com.demo.mxplayer.gallery.PhoneMediaControl.PhotoEntry;


public class PhotoPreview extends LinearLayout implements OnClickListener {

	private ProgressBar pbLoading;
	private GestureImageView ivContent;
	private OnClickListener l;
View view;
	public PhotoPreview(Context context) {
		super(context);
		try {
			view=LayoutInflater.from(context).inflate(R.layout.view_photopreview, this, true);

			pbLoading =view. findViewById(R.id.pb_loading_vpp);
		    ivContent = view.findViewById(R.id.iv_content_vpp);
			ivContent.setOnClickListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public PhotoPreview(Context context, AttributeSet attrs, int defStyle) {
		this(context);
	}

	public PhotoPreview(Context context, AttributeSet attrs) {
		this(context);
	}

	public void loadImage(PhotoEntry mPhotoEntry) {
		loadImage("file://" + mPhotoEntry.path);
	}




	private void loadImage(String path) {


		try {
			ImageLoader.getInstance().loadImage(path, new SimpleImageLoadingListener() {
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

					ivContent.setImageBitmap(loadedImage);

					pbLoading.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
					ivContent.setImageDrawable(getResources().getDrawable(R.drawable.nophotos));
					pbLoading.setVisibility(View.GONE);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@Override
	public void setOnClickListener(OnClickListener l) {
		this.l = l;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.iv_content_vpp && l != null)
			l.onClick(ivContent);
	};


}
