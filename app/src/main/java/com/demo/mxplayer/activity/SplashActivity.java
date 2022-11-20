package com.demo.mxplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.FirebaseApp;
import com.demo.mxplayer.R;
import com.demo.mxplayer.utils.MyUtils;


import java.util.Arrays;




public class SplashActivity extends AppCompatActivity {

    com.google.android.gms.ads.interstitial.InterstitialAd mInterstitialAdMob;
    public void loadMob() {


        AdRequest adRequest = new AdRequest.Builder().build();
        RequestConfiguration configuration = new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("5568E99994901F4C1E938022EB5334EC","3E577FA421751B6DB467B936D6E01D6B")).build();
        MobileAds.setRequestConfiguration(configuration);
        com.google.android.gms.ads.interstitial.InterstitialAd.load(this, getResources().getString(R.string.admob_interstitial), adRequest, new InterstitialAdLoadCallback() {
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






    private void ShowInterstitalAds() {
   if (this.mInterstitialAdMob != null) {
            this.mInterstitialAdMob.show(this);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_splash);

        FirebaseApp.initializeApp(this);

        loadMob();

        AdSettings.addTestDevice("d29428d4-b705-4f1c-9dea-5f3d05bb4dde");
        AudienceNetworkAds.initialize(this);

        new Handler().postDelayed(new Runnable() {
            public void run() {


                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                ShowInterstitalAds();
                finish();
            }

        }, 7000);
    }
}