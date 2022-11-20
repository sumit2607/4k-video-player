package com.demo.mxplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.demo.mxplayer.App;
import com.demo.mxplayer.R;
import com.demo.mxplayer.adapter.BaseFragmentAdapter;
import com.demo.mxplayer.gallery.PhoneMediaControl;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

public class Gallery_Photo_list_Fragment extends Fragment {

    private Toolbar toolbar;
    private GridView mView;
    private Context context;
    public static ArrayList<PhoneMediaControl.AlbumEntry> albumsSorted = null;
    public static ArrayList<PhoneMediaControl.PhotoEntry> photos = new ArrayList<PhoneMediaControl.PhotoEntry>();

    private int itemWidth = 100;
    private ListAdapter listAdapter;
    private int AlbummID=0;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_photolist, parent, false);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initializeActionBar();

        initializeView(view);

    }
    private void initializeActionBar() {

        context=getActivity();
        String nameAlbum =  getArguments().getString("Key_Name");
        AlbummID =Integer.parseInt(getArguments().getString("Key_ID")) ;
        albumsSorted=GalleryFragment.albumsSorted;

        photos=albumsSorted.get(AlbummID).photos;


        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(nameAlbum+" ("+photos.size()+")");
    }
    private void initializeView(View view){
        mView=(GridView)view.findViewById(R.id.grid_view);
        mView.setAdapter(listAdapter = new ListAdapter(getActivity()));

        int position = mView.getFirstVisiblePosition();
        int columnsCount = 2;
        mView.setNumColumns(columnsCount);
        itemWidth = (App.displaySize.x - ((columnsCount + 1) * App.dp(4))) / columnsCount;
        mView.setColumnWidth(itemWidth);

        listAdapter.notifyDataSetChanged();
        mView.setSelection(position);
        mView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {


                Fragment myFragment = new Gallery_Photo_Preview();
                Bundle args = new Bundle();
                args.putInt("Key_FolderID", AlbummID);
                args.putInt("Key_ID",  position);
                myFragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, myFragment).addToBackStack(null).commit();
            }
        });

        LoadAllAlbum();
    }
    private void LoadAllAlbum(){
        if (mView != null && mView.getEmptyView() == null) {
            mView.setEmptyView(null);
        }
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }


    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;
        private LayoutInflater layoutInflater;
        private DisplayImageOptions options;
        private ImageLoader imageLoader = ImageLoader.getInstance();

        public ListAdapter(Context context) {
            this.mContext = context;
            this.layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.nophotos)
                    .showImageForEmptyUri(R.drawable.nophotos)
                    .showImageOnFail(R.drawable.nophotos).cacheInMemory(true)
                    .cacheOnDisc(true).considerExifParams(true).build();
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));

        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public int getCount() {
            return photos != null ? photos.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
         ListAdapter.viewHolder mHolder;
            if (view == null) {
                mHolder = new ListAdapter.viewHolder();
                view = layoutInflater.inflate(R.layout.album_image, viewGroup,false);
                mHolder.imageView = (ImageView) view.findViewById(R.id.album_image);
                ViewGroup.LayoutParams params = view.getLayoutParams();
                params.width = itemWidth;
                params.height = itemWidth;
                view.setLayoutParams(params);
                mHolder.imageView.setTag(i);

                view.setTag(mHolder);
            } else {
                mHolder = (ListAdapter.viewHolder) view.getTag();
            }
            PhoneMediaControl.PhotoEntry mPhotoEntry = photos.get(i);
            String path = mPhotoEntry.path;
            if (path != null && !path.equals("")) {
                ImageLoader.getInstance().displayImage("file://" + path, mHolder.imageView);
            }

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return albumsSorted == null || albumsSorted.isEmpty();
        }

        class viewHolder {
            public ImageView imageView;
        }

    }


}
