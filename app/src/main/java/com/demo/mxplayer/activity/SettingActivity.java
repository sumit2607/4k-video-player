package com.demo.mxplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;



import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.demo.mxplayer.R;
import com.demo.mxplayer.pinlib.CustomPinActivity;
import com.demo.mxplayer.utils.ColorChooserDialog;
import com.demo.mxplayer.utils.MyUtils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private int theme;
    private Context context;
    TextView securitytext,themename;
    LinearLayout changepintext;
    Switch sw,resume,fingerprintbutton,nightmode,autonext,subtitleswitch;
    LinearLayout fingerlayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context=SettingActivity.this;
        getWindow().setFlags(1024, 1024);
        theme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sharedPreferences = getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        ((LinearLayout) findViewById(R.id.relativeLayoutChooseTheme)).setOnClickListener(this);
         sw = (Switch) findViewById(R.id.securitybutton);
        resume = (Switch) findViewById(R.id.resumebutton);
        nightmode = (Switch) findViewById(R.id.daynightmode);
        autonext = (Switch) findViewById(R.id.autoplayswitch);
        subtitleswitch = (Switch) findViewById(R.id.subtitleswitch);
        fingerprintbutton = (Switch) findViewById(R.id.fingerprintbutton);
        securitytext = (TextView) findViewById(R.id.securitybuttontext);
        themename = (TextView) findViewById(R.id.themename);
        changepintext = (LinearLayout) findViewById(R.id.changepintext);
        fingerlayout = (LinearLayout) findViewById(R.id.fingerlayout);
     //   sharedPreferences.edit().putString("THEMENAME", (String)v.getTag()).apply();
        themename.setText(sharedPreferences.getString("THEMENAME","Blue"));
        themename.setOnClickListener(this);
        if(sharedPreferences.getBoolean("is_pin_enable", false)){
            securitytext.setText("Disable Lock");
            sw.setChecked(true);

            changepintext.setVisibility(View.VISIBLE);
            fingerlayout.setVisibility(View.VISIBLE);

        }else{
            securitytext.setText("Enable Lock");
            sw.setChecked(false);

            changepintext.setVisibility(View.GONE);
            fingerlayout.setVisibility(View.GONE);

        }

        if(sharedPreferences.getBoolean("is_resume", false)) {
            resume.setChecked(true);
        }else{
            resume.setChecked(false);

        }
        if(sharedPreferences.getBoolean("is_autoplay", false)) {
            autonext.setChecked(true);
        }else{
            autonext.setChecked(false);

        }

        if(sharedPreferences.getInt("THEME", 0)!=111) {
            nightmode.setChecked(true);
        }else{
            nightmode.setChecked(false);

        }


        if(sharedPreferences.getBoolean("is_finger_enable", false)) {
            fingerprintbutton.setChecked(true);
        }else{
            fingerprintbutton.setChecked(false);

        }
        if(sharedPreferences.getBoolean("is_subtitle_show", false)) {
            subtitleswitch.setChecked(true);
        }else{
            subtitleswitch.setChecked(false);

        }


        changepintext.setOnClickListener(this);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent;
                if (sharedPreferences.getBoolean("is_pin_enable", false)) {
                    intent = new Intent(getApplicationContext(), CustomPinActivity.class);
                    intent.putExtra("type", 4);
                    startActivityForResult(intent, 123);


                } else {
                    intent = new Intent(getApplicationContext(), CustomPinActivity.class);
                    intent.putExtra("type", 0);
                    startActivityForResult(intent, 11);


                }


            }
        });

        nightmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (sharedPreferences.getBoolean("NightMode", false)) {
                    editor.putBoolean("NightMode",false).apply();
                    editor.putInt("THEME", sharedPreferences.getInt("LASTTHEME", 0)).apply();
                    MyUtils.settingTheme(context,  sharedPreferences.getInt("LASTTHEME", 0));



                } else {

                    editor.putInt("LASTTHEME", theme).apply();
                    editor.putBoolean("NightMode",true).apply();
                    editor.putInt("THEME", 111).apply();
                    MyUtils.settingTheme(context, 111);


                }
                finish();
                startActivity(getIntent());


            }
        });
        resume.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (sharedPreferences.getBoolean("is_resume", false)) {
                    editor.putBoolean("is_resume",false).apply();


                } else {
                    editor.putBoolean("is_resume",true).apply();


                }


            }
        });
        subtitleswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (sharedPreferences.getBoolean("is_subtitle_show", false)) {
                    editor.putBoolean("is_subtitle_show",false).apply();


                } else {
                    editor.putBoolean("is_subtitle_show",true).apply();


                }


            }
        });

        autonext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (sharedPreferences.getBoolean("is_autoplay", false)) {
                    editor.putBoolean("is_autoplay",false).apply();


                } else {
                    editor.putBoolean("is_autoplay",true).apply();


                }


            }
        });

        fingerprintbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (sharedPreferences.getBoolean("is_finger_enable", false)) {
                    editor.putBoolean("is_finger_enable",false).apply();


                } else {
                    editor.putBoolean("is_finger_enable",true).apply();


                }


            }
        });



    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
           onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
    public void theme() {
        sharedPreferences = getSharedPreferences(MyUtils.pref_key, Context.MODE_PRIVATE);
        theme = sharedPreferences.getInt("THEME", 0);

        MyUtils.settingTheme(context, theme);
    }
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 11) {
            Toast.makeText(getApplicationContext(), "PassCode enabled.", Toast.LENGTH_LONG).show();
            securitytext.setText("Disable Lock");
            sw.setChecked(true);
            fingerlayout.setVisibility(View.VISIBLE);
            changepintext.setVisibility(View.VISIBLE);
            editor.putBoolean("is_pin_enable",true).apply();

        } else if (requestCode == 123 && resultCode == -1) {
            editor.putBoolean("is_pin_enable",false).apply();
            securitytext.setText("Enable Lock");
            sw.setChecked(false);
            fingerlayout.setVisibility(View.GONE);
            changepintext.setVisibility(View.GONE);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relativeLayoutChooseTheme:
                FragmentManager fragmentManager = getSupportFragmentManager();
                ColorChooserDialog dialog = new ColorChooserDialog();
                dialog.setOnItemChoose(new ColorChooserDialog.OnItemChoose() {
                    @Override
                    public void onClick(int position) {
                        setThemeFragment(position);
                    }

                    @Override
                    public void onSaveChange() {
                        startActivity(new Intent(SettingActivity.this, MainActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
                dialog.show(fragmentManager, "fragment_color_chooser");
                break;

         case R.id.changepintext:
             Intent intent;
             intent = new Intent(getApplicationContext(), CustomPinActivity.class);
             intent.putExtra("type", 2);
             startActivity(intent);
        break;

            case R.id.themename:
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                ColorChooserDialog dialog1 = new ColorChooserDialog();
                dialog1.setOnItemChoose(new ColorChooserDialog.OnItemChoose() {
                    @Override
                    public void onClick(int position) {
                        setThemeFragment(position);
                    }

                    @Override
                    public void onSaveChange() {
                        startActivity(new Intent(SettingActivity.this, MainActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                    }
                });
                dialog1.show(fragmentManager1, "fragment_color_chooser");
                break;
    }
    }

    public void setThemeFragment(int theme) {
        switch (theme) {
            case 1:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 1).apply();
                break;
            case 2:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 2).apply();
                break;
            case 3:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 3).apply();
                break;
            case 4:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 4).apply();
                break;
            case 5:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 5).apply();
                break;
            case 6:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 6).apply();
                break;
            case 7:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 7).apply();
                break;
            case 8:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 8).apply();
                break;
            case 9:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 9).apply();
                break;
            case 10:
                editor = sharedPreferences.edit();
                editor.putInt("THEME", 10).apply();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(SettingActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(0, 0);
        super.onBackPressed();
    }
}
