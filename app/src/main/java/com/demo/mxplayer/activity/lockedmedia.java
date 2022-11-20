package com.demo.mxplayer.activity;



import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.demo.mxplayer.R;
import com.demo.mxplayer.adapter.TabAdapter;
import com.demo.mxplayer.fragment.LockVideosList;
import com.demo.mxplayer.fragment.LockFolders;
import com.demo.mxplayer.utils.MyUtils;
import com.google.android.material.tabs.TabLayout;

public class lockedmedia extends AppCompatActivity {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int theme;
    private Context context;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=lockedmedia.this;
        getWindow().setFlags(1024, 1024);
        theme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockedmedia);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("Locked File");
        sharedPreferences = getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new LockFolders(), "Folder");
        adapter.addFragment(new LockVideosList(), "Video");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
    public void theme() {
        sharedPreferences = getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        theme = sharedPreferences.getInt("THEME", 0);

        MyUtils.settingTheme(context, theme);
    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(lockedmedia.this, MainActivity.class));
        finish();
        overridePendingTransition(0, 0);
        super.onBackPressed();
    }
}
