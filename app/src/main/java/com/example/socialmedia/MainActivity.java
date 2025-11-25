package blogtalk.com.socialmedia;

import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespAppDetails;
import blogtalk.com.apiservices.RespCustomAds;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.fragments.FragmentHome;
import blogtalk.com.fragments.FragmentProfile;
import blogtalk.com.fragments.FragmentSearch;
import blogtalk.com.fragments.FragmentVideos;
import blogtalk.com.utils.AdManagerInterAdmob;
import blogtalk.com.utils.AdManagerInterApplovin;
import blogtalk.com.utils.AdManagerInterStartApp;
import blogtalk.com.utils.AdManagerInterWortise;
import blogtalk.com.utils.Constants;
import blogtalk.com.utils.DBHelper;
import blogtalk.com.utils.GDPRChecker;
import blogtalk.com.utils.Methods;
import blogtalk.com.utils.SharedPref;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.onesignal.OneSignal;

import java.util.Objects;

import io.agora.chat.ChatClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    DBHelper dbHelper;
    MaterialToolbar toolbar;
    Methods methods;
    SharedPref sharedPref;
    public static BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    ConstraintLayout cl_notification, cl_chat;
    ImageView iv_settings;
    View view_dot_noti, view_dot_chat;
    GDPRChecker gdprChecker;
    AppBarLayout myAppBar;
    AppBarLayout.LayoutParams params;
    CoordinatorLayout.LayoutParams appBarLayoutParams;
    private static final int TIME_INTERVAL = 2000; // Time interval for the exit message in milliseconds
    private long backPressedTime;
    boolean isFromUploadNoti =false;
    public ImageView iv_account_verified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bnv_main);

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frameLayout_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.coordinator_layout), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });

        // if clicked from upload notification
        isFromUploadNoti = getIntent().getBooleanExtra("isfromuploadnoti", false);

        dbHelper = new DBHelper(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();
        sharedPref = new SharedPref(this);

        fm = getSupportFragmentManager();
        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        myAppBar = findViewById(R.id.myAppBar);
        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        appBarLayoutParams = (CoordinatorLayout.LayoutParams) myAppBar.getLayoutParams();

        bottomNavigationView.setOnItemSelectedListener(onItemSelectedListener);

        iv_settings = findViewById(R.id.iv_main_settings);
        cl_chat = findViewById(R.id.cl_main_chat);
        cl_notification = findViewById(R.id.cl_main_notification);
        view_dot_noti = findViewById(R.id.view_dot_noti);
        view_dot_chat = findViewById(R.id.view_dot_chat);
        iv_account_verified = findViewById(R.id.iv_prof_account_verify);

        setDotOnIcons();

        if(!isFromUploadNoti) {
            loadFrag(new FragmentHome(), getString(R.string.home));
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_bottom_profile);
        }

        gdprChecker = new GDPRChecker(MainActivity.this);
        gdprChecker.check();

        getAppDetails();

        iv_settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        cl_notification.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        cl_chat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
            startActivity(intent);
        });

        if (!methods.getPerNotificationStatus()) {
            methods.permissionDialog();
        }

        getCustomAds();

        cl_chat.setVisibility((sharedPref.getIsChatOn() && sharedPref.isLogged()) ? View.VISIBLE : View.GONE);

        if (!sharedPref.getIsVoicePermissionAsked() && !checkPermissions() && sharedPref.getIsChatOn() && sharedPref.getIsVoiceChatOn()) {
            sharedPref.setIsVoicePermissionAsked(true);
            ActivityCompat.requestPermissions(this, getRequiredCallPermissions(), 11);
        }
    }

    NavigationBarView.OnItemSelectedListener onItemSelectedListener = item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.nav_bottom_home) {
            toolbar.setVisibility(View.VISIBLE);
            loadFrag(new FragmentHome(), getString(R.string.home));
            return true;
        } else if (itemId == R.id.nav_bottom_search) {
            loadFrag(new FragmentSearch(), getString(R.string.search));
            return true;
        } else if (itemId == R.id.nav_bottom_video) {
            loadFrag(new FragmentVideos(), getString(R.string.video));
            return true;
        } else if (itemId == R.id.nav_bottom_add_post) {
            if (methods.isLoggedAndVerified(false)) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
            return false;
        } else if (itemId == R.id.nav_bottom_profile) {
            if (new SharedPref(MainActivity.this).isLogged()) {
                loadFrag(new FragmentProfile(), getString(R.string.profile));
                return true;
            } else {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.putExtra("from", "app");
                startActivity(intent);
                return false;
            }
        }
        return false;
    };

    public void loadFrag(Fragment f1, String name) {
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//        if (!name.equals(getString(R.string.home))) {
//            ft.hide(fm.getFragments().get(fm.getBackStackEntryCount()));
//            ft.add(R.id.frameLayout_main, f1, name);
//            ft.addToBackStack(name);
//        } else {
        ft.replace(R.id.frameLayout_main, f1, name);
//        }
        ft.commitAllowingStateLoss();

        if (!name.equals(getString(R.string.video))) {
            toolbar.setVisibility(View.VISIBLE);
            if(name.equals(getString(R.string.profile))) {
                if(sharedPref.getUserName().isEmpty() || sharedPref.getUserName().equals("null")) {
                    getSupportActionBar().setTitle(name);
                } else {
                    getSupportActionBar().setTitle(sharedPref.getUserName());
                }
            } else {
                getSupportActionBar().setTitle(name);
            }
        } else {
            toolbar.setVisibility(View.GONE);
        }
        if (name.equals(getString(R.string.profile))) {
            iv_settings.setVisibility(View.GONE);
            cl_notification.setVisibility(View.GONE);
            cl_chat.setVisibility(View.GONE);
        } else {
            iv_account_verified.setVisibility(View.GONE);

            iv_settings.setVisibility(View.VISIBLE);
            cl_notification.setVisibility(View.VISIBLE);
            cl_chat.setVisibility((sharedPref.getIsChatOn() && sharedPref.isLogged()) ? View.VISIBLE : View.GONE);
        }
        if (name.equals(getString(R.string.profile))) {
            params.setScrollFlags(0);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
            myAppBar.setLayoutParams(appBarLayoutParams);
        }
    }

    private void getAppDetails() {
        if (methods.isNetworkAvailable()) {
            Call<RespAppDetails> call = APIClient.getClient().create(APIInterface.class).getAppDetails(methods.getAPIRequest(Constants.URL_APP_DETAILS, "", "", "", "", "", "", "", "", "", "", "", ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespAppDetails> call, @NonNull Response<RespAppDetails> response) {
                    if (response.body() != null && response.body().getItemAbout() != null) {
                        Constants.itemAbout = response.body().getItemAbout();

                        Constants.showUpdateDialog = response.body().getItemAbout().isShowAppUpdate();
                        Constants.appVersion = response.body().getItemAbout().getAppUpdateVersion();
                        Constants.appUpdateMsg = response.body().getItemAbout().getAppUpdateMessage();
                        Constants.appUpdateURL = response.body().getItemAbout().getAppUpdateLink();
                        Constants.appUpdateCancel = response.body().getItemAbout().isAppUpdateCancel();

//                        Constants.appUpdateCancel = c.getBoolean("google_play_link");
                        Constants.urlYoutube = response.body().getItemAbout().getYoutubeLink();
                        Constants.urlInstagram = response.body().getItemAbout().getInstagramLink();
                        Constants.urlTwitter = response.body().getItemAbout().getTwitterLink();
                        Constants.urlFacebook = response.body().getItemAbout().getFacebookLink();

                        Constants.videoUploadDuration = response.body().getItemAbout().getVideoUploadTime();
                        Constants.videoUploadSize = response.body().getItemAbout().getVideoUploadSize();

                        Constants.minPost = response.body().getItemAbout().getMinimumPost();
                        Constants.minFollowers = response.body().getItemAbout().getMinimumFollowers();
                        Constants.verifiedDocName = response.body().getItemAbout().getVerificationDocName();
                        Constants.onePoint = response.body().getItemAbout().getOnePoints();
                        Constants.oneMoney = response.body().getItemAbout().getOneMoney();

                        sharedPref.setIsChatOn(response.body().getItemAbout().isChatOn());
                        sharedPref.setIsVoiceChatOn(response.body().getItemAbout().isVoiceCallOn());
                        sharedPref.setIsPointsOn(response.body().getItemAbout().getPointsSystemOnOff());
                        sharedPref.setIsAccountVerifyOn(response.body().getItemAbout().getIsAccountVerifyOn());
                        sharedPref.setMinWithdrawPoints(response.body().getItemAbout().getMinWithdrawPoints());
                        sharedPref.setUploadPostType(response.body().getItemAbout().getUploadPostType());

                        sharedPref.setCurrencyCode(response.body().getItemAbout().getCurrencyCode());

                        if (response.body().getItemAbout().getItemAds() != null && response.body().getItemAbout().getItemAds().getAdType() != null) {
                            switch (response.body().getItemAbout().getItemAds().getAdType()) {
                                case "Admob", "Facebook" ->
                                        Constants.publisherAdID = response.body().getItemAbout().getItemAds().getItemAdsDetails().getPublisherId();
                                case "StartApp" ->
                                        Constants.startappAppId = response.body().getItemAbout().getItemAds().getItemAdsDetails().getPublisherId();
                                case "Wortise" ->
                                        Constants.wortiseAppId = response.body().getItemAbout().getItemAds().getItemAdsDetails().getPublisherId();
                            }
                            Constants.bannerAdID = response.body().getItemAbout().getItemAds().getItemAdsDetails().getBannerID();
                            Constants.interstitialAdID = response.body().getItemAbout().getItemAds().getItemAdsDetails().getInterstitialID();
                            Constants.nativeAdID = response.body().getItemAbout().getItemAds().getItemAdsDetails().getNativeID();

                            Constants.isBannerAd = response.body().getItemAbout().getItemAds().getItemAdsDetails().getIsBannerOn().equals("1");
                            Constants.isInterAd = response.body().getItemAbout().getItemAds().getItemAdsDetails().getIsInterstitialOn().equals("1");
                            Constants.isNativeAd = response.body().getItemAbout().getItemAds().getItemAdsDetails().getIsNativeOn().equals("1");

                            Constants.interstitialAdShow = Integer.parseInt(response.body().getItemAbout().getItemAds().getItemAdsDetails().getInterAdsClick());
                            Constants.nativeAdShow = Integer.parseInt(response.body().getItemAbout().getItemAds().getItemAdsDetails().getNativeAdsPos());

                            Constants.bannerAdType = response.body().getItemAbout().getItemAds().getAdType();
                            Constants.interstitialAdType = response.body().getItemAbout().getItemAds().getAdType();
                            Constants.nativeAdType = response.body().getItemAbout().getItemAds().getAdType();
                        } else {
                            Constants.isBannerAd = false;
                            Constants.isInterAd = false;
                            Constants.isNativeAd = false;
                        }

                        if (response.body().getItemAbout().getArrayListPages() != null && !response.body().getItemAbout().getArrayListPages().isEmpty()) {
                            Constants.arrayListPages.clear();
                            for (int i = 0; i < response.body().getItemAbout().getArrayListPages().size(); i++) {
                                if (!response.body().getItemAbout().getArrayListPages().get(i).getId().equals("1")) {
                                    Constants.arrayListPages.add(response.body().getItemAbout().getArrayListPages().get(i));
                                } else {
                                    Constants.itemAbout.setAppDesc(response.body().getItemAbout().getArrayListPages().get(i).getContent());
                                }
                            }
                        }

                        cl_chat.setVisibility((sharedPref.getIsChatOn() && sharedPref.isLogged()) ? View.VISIBLE : View.GONE);
                    }

                    String version = "";
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        version = String.valueOf(pInfo.versionCode);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (Constants.showUpdateDialog && !Constants.appVersion.equals(version)) {
                        methods.showUpdateAlert(Constants.appUpdateMsg, false);
                    } else {

//                        adConsent.checkForConsent();

                        methods.initializeAds();

                        sharedPref.setAdDetails(Constants.isBannerAd, Constants.isInterAd, Constants.isNativeAd, Constants.bannerAdType,
                                Constants.interstitialAdType, Constants.nativeAdType, Constants.bannerAdID, Constants.interstitialAdID, Constants.nativeAdID, Constants.startappAppId, Constants.interstitialAdShow, Constants.nativeAdShow);
                        sharedPref.setSocialDetails();
                        dbHelper.addToAbout();

                        if (Constants.isInterAd) {
                            switch (Constants.interstitialAdType) {
                                case Constants.AD_TYPE_ADMOB, Constants.AD_TYPE_FACEBOOK -> {
                                    AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(getApplicationContext());
                                    adManagerInterAdmob.createAd();
                                }
                                case Constants.AD_TYPE_STARTAPP -> {
                                    AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(getApplicationContext());
                                    adManagerInterStartApp.createAd();
                                }
                                case Constants.AD_TYPE_APPLOVIN -> {
                                    AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(MainActivity.this);
                                    adManagerInterApplovin.createAd();
                                }
                                case Constants.AD_TYPE_WORTISE -> {
                                    AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(MainActivity.this);
                                    adManagerInterWortise.createAd();
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespAppDetails> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        } else {
//            adConsent.checkForConsent();
            dbHelper.getAbout();
        }
    }

    private void getCustomAds() {
        if (Constants.arrayListCustomAds.isEmpty() && methods.isNetworkAvailable()) {

            Call<RespCustomAds> call = APIClient.getClient().create(APIInterface.class).getCustomAds(methods.getAPIRequest(Constants.URL_CUSTOM_ADS, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespCustomAds> call, @NonNull Response<RespCustomAds> response) {
                    if (!isFinishing()) {
                        if (response.body() != null) {
                            if (response.body().getArrayListCustomAds() != null && !response.body().getArrayListCustomAds().isEmpty()) {
                                Constants.arrayListCustomAds.addAll(response.body().getArrayListCustomAds());
                                for (int i = 0; i < Constants.arrayListCustomAds.size(); i++) {
                                    switch (Constants.arrayListCustomAds.get(i).getDisplayOn()) {
                                        case Constants.TAG_FROM_HOME: {
                                            Constants.isCustomAdsHome = true;
                                            Constants.customAdHomePos = Constants.arrayListCustomAds.get(i).getAdPosition();
                                            break;
                                        }
                                        case Constants.TAG_FROM_SEARCH: {
                                            Constants.isCustomAdsSearch = true;
                                            Constants.customAdSearchPos = Constants.arrayListCustomAds.get(i).getAdPosition();
                                            break;
                                        }
                                        case Constants.TAG_FROM_TAG: {
                                            Constants.isCustomAdsTags = true;
                                            Constants.customAdTagPos = Constants.arrayListCustomAds.get(i).getAdPosition();
                                            break;
                                        }
                                        case Constants.TAG_FROM_OTHER: {
                                            Constants.isCustomAdsOther = true;
                                            Constants.customAdOthersPos = Constants.arrayListCustomAds.get(i).getAdPosition();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespCustomAds> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void setDotOnIcons() {
        if(sharedPref.getIsChatOn()) {
            if (sharedPref.isLogged()) {
                cl_chat.setVisibility(View.VISIBLE);
            } else {
                cl_chat.setVisibility(View.GONE);
            }
            if (sharedPref.isNewNotification()) {
                view_dot_noti.setVisibility(View.VISIBLE);
            } else {
                view_dot_noti.setVisibility(View.GONE);
            }

            try {
                if (ChatClient.getInstance().chatManager().getUnreadMessageCount() > 0) {
                    view_dot_chat.setVisibility(View.VISIBLE);
                } else {
                    view_dot_chat.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            cl_chat.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        if (Constants.isNewPostAdded) {
            Constants.isNewPostAdded = false;
            bottomNavigationView.setSelectedItemId(R.id.nav_bottom_profile);
        }
        setDotOnIcons();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(fm.getBackStackEntryCount() > 0 && Objects.requireNonNull(fm.getBackStackEntryAt(0).getName()).equalsIgnoreCase("story")) {
            fm.popBackStack();
        } else {
            if (bottomNavigationView.getSelectedItemId() == R.id.nav_bottom_home) {
                if (System.currentTimeMillis() - backPressedTime < TIME_INTERVAL) {
                    super.onBackPressed();
                } else {
                    methods.showToast(getString(R.string.press_back_again));
                    backPressedTime = System.currentTimeMillis();
                }
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_bottom_home);
            }
        }
    }

    private String[] getRequiredCallPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    RECORD_AUDIO, // Record audio permission
                    READ_PHONE_STATE, // Read phone state permission
                    BLUETOOTH_CONNECT // Bluetooth connection permission
            };
        } else {
            return new String[]{
                    RECORD_AUDIO,
            };
        }
    }

    private boolean checkPermissions() {
        for (String permission : getRequiredCallPermissions()) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}