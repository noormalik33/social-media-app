package com.example.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.apiservices.APIClient;
import com.example.apiservices.APIInterface;
import com.example.apiservices.RespAppDetails;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.Methods;
import com.example.utils.SharedPref;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AboutActivity extends AppCompatActivity {

    DBHelper dbHelper;
    SharedPref sharedPref;
    WebView webView;
    TextView tv_appname, tv_email, tv_website, tv_company, tv_contact, tv_version;
    ImageView iv_logo;
    MaterialCardView cv_email, cv_website, cv_company, cv_contact;
    String website, email, desc, applogo, appname, appversion, appauthor, appcontact;
    ProgressDialog pbar;
    Methods methods;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        dbHelper = new DBHelper(this);
        sharedPref = new SharedPref(this);
        methods = new Methods(this);
        methods.forceRTLIfSupported();

        pbar = new ProgressDialog(this);
        pbar.setMessage(getResources().getString(R.string.loading));
        pbar.setCancelable(false);

        webView = findViewById(R.id.wb_about);
        tv_appname = findViewById(R.id.tv_about_appname);
        tv_email = findViewById(R.id.tv_about_email);
        tv_website = findViewById(R.id.tv_about_website);
        tv_company = findViewById(R.id.tv_about_company);
        tv_contact = findViewById(R.id.tv_about_contact);
        tv_version = findViewById(R.id.tv_about_version);
        iv_logo = findViewById(R.id.iv_about_logo);

        cv_email = findViewById(R.id.cv_about_email);
        cv_website = findViewById(R.id.cv_about_website);
        cv_contact = findViewById(R.id.cv_about_contact);
        cv_company = findViewById(R.id.cv_about_company );

        findViewById(R.id.iv_about_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        tv_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + tv_contact.getText().toString()));
                startActivity(intent);
            }
        });

        tv_website.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = tv_website.getText().toString();
                if(!url.contains("http://") || !url.contains("https://")) {
                    url = "https://" + url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        tv_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + tv_email.getText().toString()));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        getAppDetails();

        LinearLayout ll_adView = findViewById(R.id.ll_adView);
        methods.showBannerAd(ll_adView);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void getAppDetails() {
        if (methods.isNetworkAvailable()) {
            pbar.show();

            Call<RespAppDetails> call = APIClient.getClient().create(APIInterface.class).getAppDetails(methods.getAPIRequest(Constants.URL_APP_DETAILS, "", "", "", "", "", "", "", "", "", "", "", ""));
            call.enqueue(new Callback<RespAppDetails>() {
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
                    }


                    sharedPref.setAdDetails(Constants.isBannerAd, Constants.isInterAd, Constants.isNativeAd, Constants.bannerAdType,
                            Constants.interstitialAdType, Constants.nativeAdType, Constants.bannerAdID, Constants.interstitialAdID, Constants.nativeAdID, Constants.startappAppId, Constants.interstitialAdShow, Constants.nativeAdShow);
                    sharedPref.setSocialDetails();
                    setVariables();
                    dbHelper.addToAbout();

                    if (pbar.isShowing()) {
                        pbar.dismiss();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespAppDetails> call, @NonNull Throwable t) {
                    if (pbar.isShowing()) {
                        pbar.dismiss();
                    }
                    call.cancel();
                }
            });
        } else {
            if (dbHelper.getAbout()) {
                setVariables();
            }
        }
    }

    public void setVariables() {

        appname = Constants.itemAbout.getAppName();
        applogo = Constants.itemAbout.getAppLogo();
        desc = Constants.itemAbout.getAppDesc();
        appversion = Constants.itemAbout.getAppVersion();
        appauthor = Constants.itemAbout.getAuthor();
        appcontact = Constants.itemAbout.getContact();
        email = Constants.itemAbout.getEmail();
        website = Constants.itemAbout.getWebsite();

        tv_appname.setText(appname);
        if (!email.trim().isEmpty()) {
            cv_email.setVisibility(View.VISIBLE);
            tv_email.setText(email);
        }

        if (!website.trim().isEmpty()) {
            cv_website.setVisibility(View.VISIBLE);
            tv_website.setText(website);
        }

        if (!appauthor.trim().isEmpty()) {
            cv_company.setVisibility(View.VISIBLE);
            tv_company.setText(appauthor);
        }

        if (!appcontact.trim().isEmpty()) {
            cv_contact.setVisibility(View.VISIBLE);
            tv_contact.setText(appcontact);
        }

        if (!appversion.trim().isEmpty()) {
            tv_version.setText(appversion);
        }

        if (applogo.trim().isEmpty()) {
            iv_logo.setVisibility(View.GONE);
        } else {
            Picasso
                    .get()
                    .load(applogo)
                    .into(iv_logo);
        }

        String mimeType = "text/html";
        String encoding = "utf-8";

        String text;
        if (methods.isDarkMode()) {
            text = "<html><head>"
                    + "<style> @font-face { font-family: 'custom'; src: url(\"file:///android_res/font/outfit_medium.ttf\")"
                    + "} body {color:#fff !important;text-align:left; font-family: 'custom'; font-size:13px;}"
                    + "</style></head>"
                    + "<body>"
                    + desc
                    + "</body></html>";
        } else {
            text = "<html><head>"
                    + "<style> @font-face { font-family: 'custom'; src: url(\"file:///android_res/font/outfit_medium.ttf\")"
                    + "} body {color:#65637B !important;text-align:left; font-family: 'custom'; font-size:13px;}"
                    + "</style></head>"
                    + "<body>"
                    + desc
                    + "</body></html>";
        }

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadDataWithBaseURL("blarg://ignored", text, mimeType, encoding, "");
    }
}