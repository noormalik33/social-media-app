package com.example.utils;

import android.content.Context;

import com.wortise.ads.interstitial.InterstitialAd;

public class AdManagerInterWortise {
    static InterstitialAd interAd;
    private final Context ctx;

    public AdManagerInterWortise(Context ctx) {
        this.ctx = ctx;
    }

    public void createAd() {
        interAd = new InterstitialAd((ctx), Constants.interstitialAdID);
        interAd.loadAd();
    }

    public InterstitialAd getAd() {
        return interAd;
    }

    public static void setAd(InterstitialAd interstitialAd) {
        interAd = interstitialAd;
    }
}