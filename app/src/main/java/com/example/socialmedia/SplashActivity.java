package com.example.socialmedia;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespAppDetails;
import com.example.apiservices.RespPostDetails;
import com.example.chat.ChatHelper;
import com.example.items.ItemUser;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    Methods methods;
    SharedPref sharedPref;
    Uri data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPref = new SharedPref(this);

        methods = new Methods(this);

        data = getIntent().getData();
        if (data != null) {
            String url = data.toString();
            if (url.contains("/post/share")) {
                Constants.pushType = "post";
                String[] strings = data.toString().split("/post/share/");
                Constants.pushPostID = strings[strings.length - 1];
            } else if (url.contains("/profile/share")) {
                Constants.pushType = "profile";
                String[] strings = data.toString().split("/profile/share/");
                Constants.pushPostID = strings[strings.length - 1];
            }
        }

        if (!sharedPref.isIntroShown()) {
            getAppDetails();
        } else {
            new Handler().postDelayed(() -> {
                if (!sharedPref.getIsAutoLogin()) {
                    openActivity();
                } else {
                    if (sharedPref.getLoginType().equals(Constants.LOGIN_TYPE_GOOGLE)) {
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser == null) {
                            sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
                            sharedPref.setIsAutoLogin(false);
                        }
                        openActivity();
                    } else {
                        openActivity();
                    }
                }
            }, 1500);

        }
    }

//    private void loadLogin() {
//        if (methods.isNetworkAvailable()) {
//
//            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getLogin(methods.getAPIRequest(Constants.URL_LOGIN, "", "", "", "", "", "", "", sharedPref.getEmail(), sharedPref.getPassword(), "", "", ""));
//            call.enqueue(new Callback<>() {
//                @Override
//                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
//                    if (response.body() != null) {
//                        if (response.body().getSuccess().equals("1") && response.body().getUserDetail() != null) {
//                            methods.setOnesignalIDs(OneSignal.getUser().getPushSubscription().getId(), response.body().getUserDetail().getId());
//
//                            sharedPref.setLoginDetails(response.body().getUserDetail().getId(), response.body().getUserDetail().getName(), response.body().getUserDetail().getMobile(), response.body().getUserDetail().getEmail(), response.body().getUserDetail().getImage(), "", sharedPref.getIsRemember(), sharedPref.getPassword(), Constants.LOGIN_TYPE_NORMAL, response.body().getUserDetail().getIsEmailVerified(), response.body().getUserDetail().getProfileCompleted());
//                            sharedPref.setIsLogged(true);
//                            sharedPref.setIsAutoLogin(true);
//                        } else {
//                            sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//                            sharedPref.setIsLogged(false);
//                        }
//                    } else {
//                        sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//                        sharedPref.setIsLogged(false);
//                    }
//                    openActivity();
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
//                    call.cancel();
//                    sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//                    sharedPref.setIsLogged(false);
//                    openActivity();
//                }
//            });
//        } else {
//            methods.showToast(getString(R.string.err_internet_not_connected));
//            sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//            sharedPref.setIsLogged(false);
//            openActivity();
//        }
//    }

//    private void loadLoginSocial(final String loginType, final String name, String email, final String authId) {
//        if (methods.isNetworkAvailable()) {
//
//
//            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getSocialLogin(methods.getAPIRequest(Constants.URL_SOCIAL_LOGIN, authId, loginType, "", "", "", "", name, email, "", "", "", ""));
//            call.enqueue(new Callback<RespUserList>() {
//                @Override
//                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
//                    if (response.body() != null) {
//                        if (response.body().getSuccess().equals("1") && response.body().getUserDetail() != null) {
//                            sharedPref.setLoginDetails(response.body().getUserDetail().getId(), response.body().getUserDetail().getName(), response.body().getUserDetail().getMobile(), email, response.body().getUserDetail().getImage(), authId, sharedPref.getIsRemember(), "", loginType, response.body().getUserDetail().getIsEmailVerified(), response.body().getUserDetail().getProfileCompleted());
//                            sharedPref.setIsLogged(true);
//                            sharedPref.setIsAutoLogin(true);
//
//                            methods.setOnesignalIDs(OneSignal.getUser().getPushSubscription().getId(), response.body().getUserDetail().getId());
//                        } else {
//                            sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//                            sharedPref.setIsLogged(false);
//                        }
//                    } else {
//                        sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//                        sharedPref.setIsLogged(false);
//                    }
//                    openActivity();
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
//                    call.cancel();
//                    sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//                    sharedPref.setIsLogged(false);
//                    openActivity();
//                }
//            });
//        } else {
//            methods.showToast(getString(R.string.err_internet_not_connected));
//            sharedPref.setLoginDetails("0", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "false", 0);
//            sharedPref.setIsLogged(false);
//            openActivity();
//        }
//    }

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

                        if (response.body().getItemAbout().getArrayListPages() != null && response.body().getItemAbout().getArrayListPages().size() > 0) {
                            Constants.arrayListPages.clear();
                            for (int i = 0; i < response.body().getItemAbout().getArrayListPages().size(); i++) {
                                if (!response.body().getItemAbout().getArrayListPages().get(i).getId().equals("1")) {
                                    Constants.arrayListPages.add(response.body().getItemAbout().getArrayListPages().get(i));
                                } else {
                                    Constants.itemAbout.setAppDesc(response.body().getItemAbout().getArrayListPages().get(i).getContent());
                                }
                            }
                        }

                        if(!sharedPref.isLoginShown() && sharedPref.getIsChatOn()) {
                            ChatHelper.getInstance().initSDK(SplashActivity.this);
                        }
                    }

                    String version = "";
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        version = String.valueOf(pInfo.versionCode);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (Constants.showUpdateDialog && !Constants.appVersion.equals(version)) {
                        methods.showUpdateAlert(Constants.appUpdateMsg, true);
                    } else {
                        sharedPref.setAdDetails(Constants.isBannerAd, Constants.isInterAd, Constants.isNativeAd, Constants.bannerAdType,
                                Constants.interstitialAdType, Constants.nativeAdType, Constants.bannerAdID, Constants.interstitialAdID, Constants.nativeAdID, Constants.startappAppId, Constants.interstitialAdShow, Constants.nativeAdShow);
                        sharedPref.setSocialDetails();

                        try (DBHelper db = new DBHelper(SplashActivity.this)) {
                            db.onCreate(db.getWritableDatabase());
                            db.addToAbout();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        openActivity();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespAppDetails> call, @NonNull Throwable t) {
                    errorDialog(getString(R.string.err_server_error), getString(R.string.err_server_no_conn));
                    call.cancel();
                }
            });
        } else {
            errorDialog(getString(R.string.err_internet_not_connected), getString(R.string.err_connect_net_tryagain));
        }
    }

    private void errorDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(SplashActivity.this, R.style.ThemeDialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);

        if (title.equals(getString(R.string.err_internet_not_connected)) || title.equals(getString(R.string.err_server_error))) {
            alertDialog.setNegativeButton(getString(R.string.try_again), (dialog, which) -> getAppDetails());
        }

        alertDialog.setPositiveButton(getString(R.string.exit), (dialog, which) -> finish());
        alertDialog.show();
    }

    private void openActivity() {
        Intent intent;
        if (!sharedPref.isIntroShown()) {
            intent = new Intent(SplashActivity.this, IntroActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else if (Constants.pushType.equals("post")) {
//            methods.showToast(Constants.pushType + " - " + Constants.pushPostID);
            getPostDetails(Constants.pushPostID);
        } else if (Constants.pushType.equals("profile")) {
            intent = new Intent(SplashActivity.this, ProfileActivity.class);
            intent.putExtra("item_user", new ItemUser(Constants.pushPostID, "", "null"));
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void getPostDetails(String postID) {
        if (methods.isNetworkAvailable()) {

            Call<RespPostDetails> call = APIClient.getClient().create(APIInterface.class).getPostDetails(methods.getAPIRequest(Constants.URL_POST_DETAILS, postID, "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostDetails> call, @NonNull Response<RespPostDetails> response) {
                    if (response.body() != null && response.body().getStatusCode().equals("200") && response.body().getItemPost() != null) {
                        Intent intent;
                        if(!response.body().getItemPost().getPostType().equalsIgnoreCase("text")) {
                            Constants.arrayListPosts.clear();
                            Constants.arrayListPosts.add(response.body().getItemPost());
                            intent = new Intent(SplashActivity.this, PostDetailActivity.class);
                            intent.putExtra("isuser", false);
                            intent.putExtra("pos", 0);
                        } else {
                            intent = new Intent(SplashActivity.this, TextPostDetailActivity.class);
                            intent.putExtra("item", response.body().getItemPost());
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        Constants.pushType = "";
                        openActivity();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostDetails> call, @NonNull Throwable t) {
                    Constants.pushType = "";
                    openActivity();
                    call.cancel();
                }
            });
        }
    }
}