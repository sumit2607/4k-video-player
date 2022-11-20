package com.demo.mxplayer.fragment;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.demo.mxplayer.R;
import com.demo.mxplayer.gallery.PhoneMediaControl;
import com.demo.mxplayer.gallery.PhotoPreview;

import java.util.List;

public class Gallery_Photo_Preview extends Fragment implements ViewPager.OnPageChangeListener{

    private ViewPager mViewPager;
    protected List<PhoneMediaControl.PhotoEntry> photos;
    protected int current,folderPosition;

    protected Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_gallerphotopreview, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        context=this.getActivity();

        folderPosition= getArguments().getInt("Key_FolderID");
        current= getArguments().getInt("Key_ID");

        photos=GalleryFragment.albumsSorted.get(folderPosition).photos;

        mViewPager = (ViewPager) view.findViewById(R.id.vp_base_app);
        mViewPager.setOnPageChangeListener(this);
        getActivity().overridePendingTransition(R.anim.activity_alpha_action_in, 0);
        bindData();

    }

    protected void bindData() {
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(current);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle((current + 1) + "/" + photos.size());
    }

    private PagerAdapter mPagerAdapter = new PagerAdapter() {

        @Override
        public int getCount() {
            if (photos == null) {
                return 0;
            } else {
                return photos.size();
            }
        }

        @Override
        public View instantiateItem(final ViewGroup container, final int position) {
            PhotoPreview photoPreview = new PhotoPreview(context);
            ((ViewPager) container).addView(photoPreview);
            photoPreview.loadImage(photos.get(position));
            return photoPreview;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    };

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        current = arg0;
        updatePercent();
    }

    protected void updatePercent() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle((current + 1) + "/" + photos.size());
    }

}
