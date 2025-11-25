package com.example.socialmedia;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.database.StandaloneDatabaseProvider;
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor;
import androidx.media3.datasource.cache.SimpleCache;

import com.example.chat.ChatHelper;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.example.utils.StatusBarUtil;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.onesignal.OneSignal;

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    SharedPref sharedPref;
    private static boolean isVoiceCallActivityRunning = false;
    @UnstableApi
    private static androidx.media3.datasource.cache.SimpleCache simpleCache;

    @OptIn(markerClass = UnstableApi.class) @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        this.registerActivityLifecycleCallbacks(this);
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseAnalytics.getInstance(getApplicationContext());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        try {
            try (DBHelper dbHelper = new DBHelper(getApplicationContext())) {
                dbHelper.onCreate(dbHelper.getWritableDatabase());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        OneSignal.initWithContext(this, getString(R.string.onesignal_app_id));

        sharedPref = new SharedPref(this);

        String mode = sharedPref.getDarkMode();
        switch (mode) {
            case Constants.DARK_MODE_SYSTEM ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            case Constants.DARK_MODE_OFF ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            case Constants.DARK_MODE_ON ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        sharedPref.getAdDetails();
        new Methods(getApplicationContext()).initializeAds();

        simpleCache = new SimpleCache(getApplicationContext().getCacheDir(), new LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024), new StandaloneDatabaseProvider(getApplicationContext()));

        Constants.screenWidth = new Methods(getApplicationContext()).getScreenWidth();
        Constants.photoHeight = (int) (new Methods(getApplicationContext()).getScreenHeight()*0.65);

        if(sharedPref.isLoginShown() && sharedPref.getIsChatOn()) {
            ChatHelper.getInstance().initSDK(this);
        }

//        System.loadLibrary("agora-rtc-sdk");

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                if (activity instanceof VoiceCallActivity) {
                    isVoiceCallActivityRunning = true;
                }
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {}

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                if (activity instanceof VoiceCallActivity) {
                    isVoiceCallActivityRunning = false;
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {}
        });
    }

    public static boolean isVoiceCallActivityRunning() {
        return isVoiceCallActivityRunning;
    }

    @OptIn(markerClass = UnstableApi.class)
    public static androidx.media3.datasource.cache.SimpleCache getSimpleCache() {
        return simpleCache;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        MultiDex.install(this);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        if(!(activity instanceof MainActivity) && !(activity instanceof PostDetailActivity)) {
            StatusBarUtil.setStatusBar(activity);
        } else if(!(activity instanceof MainActivity)) {
            StatusBarUtil.setStatusBarDarkActivity(activity);
        } else {
            StatusBarUtil.setStatusBarMainActivity(activity);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}