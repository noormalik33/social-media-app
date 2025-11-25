package com.example.utils;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManagerInterAdmob {
    static InterstitialAd interAd;
    private final Context ctx;

    public AdManagerInterAdmob(Context ctx) {
        this.ctx = ctx;
    }

    public void createAd() {
        AdRequest adRequest;
        Bundle extras = new Bundle();
        if (Constants.interstitialAdType.equals(Constants.AD_TYPE_ADMOB)) {
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                    .build();
        } else {
            adRequest = new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                    .addNetworkExtrasBundle(FacebookMediationAdapter.class, extras)
                    .build();
        }
        InterstitialAd.load(ctx, Constants.interstitialAdID, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                interAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            }
        });
    }

    public InterstitialAd getAd() {
        return interAd;
    }

    public static void setAd(InterstitialAd interstitialAd) {
        interAd = interstitialAd;
    }
}