package blogtalk.com.utils;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.READ_MEDIA_VIDEO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.CLIPBOARD_SERVICE;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkInitializationConfiguration;
import blogtalk.com.adapters.AdapterComments;
import blogtalk.com.apiservices.APIClient;
import blogtalk.com.apiservices.APIInterface;
import blogtalk.com.apiservices.RespLike;
import blogtalk.com.apiservices.RespPostComment;
import blogtalk.com.apiservices.RespPostDetails;
import blogtalk.com.apiservices.RespSuccess;
import blogtalk.com.apiservices.RespUserList;
import blogtalk.com.eventbus.EventRequested;
import blogtalk.com.eventbus.GlobalBus;
import blogtalk.com.interfaces.ActionDoneListener;
import blogtalk.com.interfaces.CommentAddListener;
import blogtalk.com.interfaces.CommentListListener;
import blogtalk.com.interfaces.EditCommentListener;
import blogtalk.com.interfaces.FunctionListener;
import blogtalk.com.interfaces.InterAdListener;
import blogtalk.com.interfaces.MoreOptionListener;
import blogtalk.com.items.ItemComments;
import blogtalk.com.items.ItemPost;
import blogtalk.com.socialmedia.EditPostActivity;
import blogtalk.com.socialmedia.EmailVerificationActivity;
import blogtalk.com.socialmedia.LoginActivity;
import blogtalk.com.socialmedia.MainActivity;
import blogtalk.com.socialmedia.PostByTagActivity;
import blogtalk.com.socialmedia.R;
import blogtalk.com.socialmedia.UserListByPostLikeActivity;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookMediationAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.wortise.ads.AdError;
import com.wortise.ads.RevenueData;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.banner.BannerAd;
import com.wortise.ads.interstitial.InterstitialAd;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.agora.chat.ChatClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Methods {

    Context context;
    int editCommentPos = 0;
    AdapterComments adapterComments;
    InterAdListener interAdListener;

    public Methods(Context context) {
        this.context = context;
    }

    // constructor
    public Methods(Context context, InterAdListener interAdListener) {
        this.context = context;
        this.interAdListener = interAdListener;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void forceRTLIfSupported() {
        if (context.getResources().getString(R.string.isRTL).equals("true")) {
            ((Activity) context).getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public int getColumnWidth(int column, int grid_padding) {
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, grid_padding, r.getDisplayMetrics());
        return (int) ((getScreenWidth() - ((column + 1) * padding)) / column);
    }

    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnWidth = point.x;
        return columnWidth;
    }

    public int getScreenHeight() {
        int columnHeight;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();

        point.x = display.getWidth();
        point.y = display.getHeight();

        columnHeight = point.y;
        return columnHeight;
    }

    public void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void setStatusColor(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(context.getResources().getColor(R.color.colorPrimaryDark));
        if (!isDarkMode()) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public void setStatusColorDark(Window window, int color) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(context.getResources().getColor(color));
//        if(!isDarkMode()) {
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }
    }

    public boolean isDarkMode() {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                return true;
            default:
                return true;
        }
    }

    public String getDarkMode() {
        SharedPref sharedPref = new SharedPref(context);
        return sharedPref.getDarkMode();
    }

    public String formatNumber(String totalNumber) {

        Number number;
        if (!totalNumber.isEmpty() && !totalNumber.equals("0")) {
            number = Double.parseDouble(totalNumber);

            char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
            long numValue = number.longValue();
            int value = (int) Math.floor(Math.log10(numValue));
            int base = value / 3;
            if (value >= 3 && base < suffix.length) {
                return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
            } else {
                return new DecimalFormat("#,##0").format(numValue);
            }
        } else {
            return "0";
        }
    }

    public void clickLogin() {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.isLogged()) {
            logout((Activity) context, sharedPref);
            Toast.makeText(context, context.getString(R.string.logout_success), Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.putExtra("from", "app");
            context.startActivity(intent);
        }
    }

    public void logout(Activity activity, SharedPref sharedPref) {
        setOnesignalIDs("", "");
        sharedPref.setNewNotification(false);
        try (DBHelper db = new DBHelper(context)) {
            db.clearNotifications();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sharedPref.getLoginType().equals(Constants.LOGIN_TYPE_GOOGLE)) {
            FirebaseAuth.getInstance().signOut();
        }

        try {
            if (ChatClient.getInstance().isLoggedIn() || ChatClient.getInstance().isLoggedInBefore()) {
                ChatClient.getInstance().logout(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sharedPref.setIsChatRegistered(false);
        sharedPref.setIsAutoLogin(false);
        sharedPref.setIsLogged(false);
        sharedPref.setUserID("");
//        sharedPref.setLoginDetails("", "", "", "", "", "", false, "", Constants.LOGIN_TYPE_NORMAL, "No");
        Intent intent1 = new Intent(context, LoginActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra("from", "");
        context.startActivity(intent1);
        activity.finish();
    }

    public void setOnesignalIDs(String playerID, String userID) {
        OneSignal.getUser().addTag("user_id", userID);
        OneSignal.getUser().addTag("player_id", playerID);
    }

    @SuppressLint("Range")
    public String getPathImage(Uri uri) {
        try {
            String filePath = "";
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Images.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
//            } else {
//                return getTempUploadPath(uri, true);
//            }
            return filePath;
        } catch (Exception e) {
            if (uri == null) {
                return null;
            }
            File tempFile = new File(context.getCacheDir(), System.currentTimeMillis() + ".jpg");
            try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                 OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException ignore) {
                return null;
            }
            return tempFile.getAbsolutePath();
        }
    }

    @SuppressLint("Range")
    public String getPathVideo(Uri uri) {
        try {
            String filePath = "";
            String wholeID = DocumentsContract.getDocumentId(uri);

            // Split at colon, use second item in the array
            String id = wholeID.split(":")[1];

            String[] column = {MediaStore.Video.Media.DATA};

            // where id is equal to
            String sel = MediaStore.Video.Media._ID + "=?";

            Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, column, sel, new String[]{id}, null);

            int columnIndex = cursor.getColumnIndex(column[0]);

            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex);
            }

            cursor.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            if (uri == null) {
                return null;
            }
            try {
                return getTempUploadPath(uri, false);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            // this is our fallback here
            return uri.getPath();
        }
    }

    public String getTempUploadPath(Uri uri, boolean isImage) {
        File root = context.getExternalCacheDir().getAbsoluteFile();
        try {
            String filePath = "";
            if (isImage) {
                filePath = root.getPath() + File.separator + System.currentTimeMillis() + ".jpg";

                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                Bitmap bm = BitmapFactory.decodeStream(inputStream);

                if (saveBitMap(root, bm, filePath)) {
                    return filePath;
                } else {
                    return "";
                }
            } else {
                String destinationFilename = context.getExternalCacheDir().getAbsolutePath() + File.separatorChar + getFileNameFromUri(uri);
                if (savefile(destinationFilename, uri)) {
                    return destinationFilename;
                } else {
                    return "";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean saveBitMap(File root, Bitmap Final_bitmap, String filePath) {
        if (!root.exists()) {
            boolean isDirectoryCreated = root.mkdirs();
            if (!isDirectoryCreated)
                return false;
        }
        File pictureFile = new File(filePath);
        try {
            pictureFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(pictureFile);
            Final_bitmap.compress(Bitmap.CompressFormat.PNG, 18, oStream);
            oStream.flush();
            oStream.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean savefile(String destinationFilename, Uri sourceUri) {
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            File fileNew = new File(destinationFilename);
            fileNew.createNewFile();

            inputStream = context.getContentResolver().openInputStream(sourceUri);

            outputStream = new FileOutputStream(destinationFilename, false);
            byte[] buf = new byte[1024];
            inputStream.read(buf);
            do {
                outputStream.write(buf);
            } while (inputStream.read(buf) != -1);

        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public String getFileNameFromUri(Uri uri) {
        Cursor returnCursor =
                context.getContentResolver().query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }

    public String getDeviceID() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void expand(final View v, View tags) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else {
                    v.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
                }
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public void collapse(final View v, View tags) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    v.requestLayout();
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public String getCurrencySymbol(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return currency.getSymbol(Locale.getDefault());
    }

    public boolean isAdmobFBAds() {
        return Constants.bannerAdType.equals(Constants.AD_TYPE_ADMOB) ||
                Constants.interstitialAdType.equals(Constants.AD_TYPE_ADMOB) ||
                Constants.nativeAdType.equals(Constants.AD_TYPE_ADMOB) ||
                Constants.bannerAdType.equals(Constants.AD_TYPE_FACEBOOK) ||
                Constants.interstitialAdType.equals(Constants.AD_TYPE_FACEBOOK) ||
                Constants.nativeAdType.equals(Constants.AD_TYPE_FACEBOOK);
    }

    public boolean isStartAppAds() {
        return Constants.bannerAdType.equals(Constants.AD_TYPE_STARTAPP) ||
                Constants.interstitialAdType.equals(Constants.AD_TYPE_STARTAPP) ||
                Constants.nativeAdType.equals(Constants.AD_TYPE_STARTAPP);
    }

    public boolean isApplovinAds() {
        return Constants.bannerAdType.equals(Constants.AD_TYPE_APPLOVIN) ||
                Constants.interstitialAdType.equals(Constants.AD_TYPE_APPLOVIN) ||
                Constants.nativeAdType.equals(Constants.AD_TYPE_APPLOVIN);
    }

    public boolean isWortiseAds() {
        return Constants.bannerAdType.equals(Constants.AD_TYPE_WORTISE) ||
                Constants.interstitialAdType.equals(Constants.AD_TYPE_WORTISE) ||
                Constants.nativeAdType.equals(Constants.AD_TYPE_WORTISE);
    }

    public void initializeAds() {
        try {
            if (isAdmobFBAds()) {
                MobileAds.initialize(context, new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });
            }

            if (isStartAppAds()) {
                if (!Constants.startappAppId.isEmpty()) {
                    StartAppSDK.init(context, Constants.startappAppId, false);
                    StartAppAd.disableSplash();
                }
            }

            if (isApplovinAds()) {
                if (!AppLovinSdk.getInstance(context).isInitialized()) {
                    AppLovinSdkInitializationConfiguration initConfig = AppLovinSdkInitializationConfiguration.builder(context.getString(R.string.applovin_id))
                            .setMediationProvider(AppLovinMediationProvider.MAX)
                            .build();

                    AppLovinSdk.getInstance(context).initialize( initConfig, sdkConfig -> { });
                }
            }

            if (isWortiseAds() && !WortiseSdk.isInitialized()) {
                WortiseSdk.initialize(context, Constants.wortiseAppId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showBannerAd(LinearLayout linearLayout) {
        if (isNetworkAvailable() && Constants.isBannerAd) {
            switch (Constants.bannerAdType) {
                case Constants.AD_TYPE_ADMOB, Constants.AD_TYPE_FACEBOOK -> {
                    AdView adViewBanner = new AdView(context);
                    AdRequest adRequest;
                    if (Constants.interstitialAdType.equals(Constants.AD_TYPE_ADMOB)) {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                                .build();
                    } else {
                        adRequest = new AdRequest.Builder()
                                .addNetworkExtrasBundle(AdMobAdapter.class, new Bundle())
                                .addNetworkExtrasBundle(FacebookMediationAdapter.class, new Bundle())
                                .build();
                    }
                    adViewBanner.setAdUnitId(Constants.bannerAdID);
                    adViewBanner.setAdSize(AdSize.BANNER);
                    linearLayout.addView(adViewBanner);
                    adViewBanner.loadAd(adRequest);
                }
                case Constants.AD_TYPE_STARTAPP -> {
                    linearLayout.setVisibility(View.GONE);
                    Banner startAppBanner = new Banner(context);
                    startAppBanner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(startAppBanner);
                    startAppBanner.setBannerListener(new BannerListener() {
                        @Override
                        public void onReceiveAd(View view) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailedToReceiveAd(View view) {

                        }

                        @Override
                        public void onImpression(View view) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onClick(View view) {

                        }
                    });
                    startAppBanner.loadAd();
                }
                case Constants.AD_TYPE_APPLOVIN -> {
                    linearLayout.setVisibility(View.GONE);
                    MaxAdView adView = new MaxAdView(Constants.bannerAdID, context);
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx = context.getResources().getDimensionPixelSize(R.dimen.banner_height);
                    adView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    linearLayout.addView(adView);
                    adView.setListener(new MaxAdViewAdListener() {
                        @Override
                        public void onAdExpanded(MaxAd maxAd) {

                        }

                        @Override
                        public void onAdCollapsed(MaxAd maxAd) {

                        }

                        @Override
                        public void onAdLoaded(MaxAd maxAd) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdDisplayed(MaxAd maxAd) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdHidden(MaxAd maxAd) {

                        }

                        @Override
                        public void onAdClicked(MaxAd maxAd) {

                        }

                        @Override
                        public void onAdLoadFailed(String s, MaxError maxError) {

                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd maxAd, MaxError maxError) {

                        }
                    });
                    adView.loadAd();
                }
                case Constants.AD_TYPE_WORTISE -> {
                    linearLayout.setVisibility(View.GONE);
                    BannerAd mBannerAd = new BannerAd(context);
                    mBannerAd.setAdSize(com.wortise.ads.AdSize.HEIGHT_50);
                    mBannerAd.setAdUnitId(Constants.bannerAdID);
                    linearLayout.addView(mBannerAd);
                    mBannerAd.setListener(new BannerAd.Listener() {
                        @Override
                        public void onBannerRevenuePaid(@NonNull BannerAd bannerAd, @NonNull RevenueData revenueData) {

                        }

                        @Override
                        public void onBannerClicked(@NonNull BannerAd bannerAd) {

                        }

                        @Override
                        public void onBannerFailedToLoad(@NonNull BannerAd bannerAd, @NonNull AdError adError) {

                        }

                        @Override
                        public void onBannerImpression(@NonNull BannerAd bannerAd) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onBannerLoaded(@NonNull BannerAd bannerAd) {
                            linearLayout.setVisibility(View.VISIBLE);
                        }
                    });
                    mBannerAd.loadAd();
                }
            }
        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    public void showInter(final int pos, final String type) {
        if (Constants.isInterAd) {
            Constants.adCount = Constants.adCount + 1;
            if (Constants.adCount % Constants.interstitialAdShow == 0) {
                switch (Constants.interstitialAdType) {
                    case Constants.AD_TYPE_ADMOB:
                    case Constants.AD_TYPE_FACEBOOK:
                        final AdManagerInterAdmob adManagerInterAdmob = new AdManagerInterAdmob(context);
                        if (adManagerInterAdmob.getAd() != null) {
                            adManagerInterAdmob.getAd().setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    AdManagerInterAdmob.setAd(null);
                                    adManagerInterAdmob.createAd();
                                    interAdListener.onClick(pos, type);
                                    super.onAdDismissedFullScreenContent();
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull @NotNull com.google.android.gms.ads.AdError adError) {
                                    AdManagerInterAdmob.setAd(null);
                                    adManagerInterAdmob.createAd();
                                    interAdListener.onClick(pos, type);
                                    super.onAdFailedToShowFullScreenContent(adError);
                                }
                            });
                            adManagerInterAdmob.getAd().show((Activity) context);
                        } else {
                            AdManagerInterAdmob.setAd(null);
                            adManagerInterAdmob.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;
                    case Constants.AD_TYPE_STARTAPP:
                        final AdManagerInterStartApp adManagerInterStartApp = new AdManagerInterStartApp(context);
                        if (adManagerInterStartApp.getAd() != null && adManagerInterStartApp.getAd().isReady()) {
                            adManagerInterStartApp.getAd().showAd(new AdDisplayListener() {
                                @Override
                                public void adHidden(Ad ad) {
                                    AdManagerInterStartApp.setAd(null);
                                    adManagerInterStartApp.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void adDisplayed(Ad ad) {

                                }

                                @Override
                                public void adClicked(Ad ad) {

                                }

                                @Override
                                public void adNotDisplayed(Ad ad) {
                                    AdManagerInterStartApp.setAd(null);
                                    adManagerInterStartApp.createAd();
                                    interAdListener.onClick(pos, type);
                                }
                            });
                        } else {
                            AdManagerInterStartApp.setAd(null);
                            adManagerInterStartApp.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;
                    case Constants.AD_TYPE_APPLOVIN:
                        final AdManagerInterApplovin adManagerInterApplovin = new AdManagerInterApplovin(context);
                        if (adManagerInterApplovin.getAd() != null && adManagerInterApplovin.getAd().isReady()) {
                            adManagerInterApplovin.getAd().setListener(new MaxAdListener() {
                                @Override
                                public void onAdLoaded(MaxAd ad) {

                                }

                                @Override
                                public void onAdDisplayed(MaxAd ad) {

                                }

                                @Override
                                public void onAdHidden(MaxAd ad) {
                                    AdManagerInterApplovin.setAd(null);
                                    adManagerInterApplovin.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onAdClicked(MaxAd ad) {

                                }

                                @Override
                                public void onAdLoadFailed(String adUnitId, MaxError error) {
                                    AdManagerInterApplovin.setAd(null);
                                    adManagerInterApplovin.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                    AdManagerInterApplovin.setAd(null);
                                    adManagerInterApplovin.createAd();
                                    interAdListener.onClick(pos, type);
                                }
                            });
                            adManagerInterApplovin.getAd().showAd();
                        } else {
                            AdManagerInterStartApp.setAd(null);
                            adManagerInterApplovin.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;

                    case Constants.AD_TYPE_WORTISE:
                        final AdManagerInterWortise adManagerInterWortise = new AdManagerInterWortise(context);
                        if (adManagerInterWortise.getAd() != null && adManagerInterWortise.getAd().isAvailable()) {
                            adManagerInterWortise.getAd().setListener(new InterstitialAd.Listener() {

                                @Override
                                public void onInterstitialRevenuePaid(@NonNull InterstitialAd interstitialAd, @NonNull RevenueData revenueData) {

                                }

                                @Override
                                public void onInterstitialClicked(@NonNull InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialDismissed(@NonNull InterstitialAd interstitialAd) {
                                    AdManagerInterWortise.setAd(null);
                                    adManagerInterWortise.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialFailedToLoad(@NonNull InterstitialAd interstitialAd, @NonNull AdError adError) {
                                    AdManagerInterWortise.setAd(null);
                                    adManagerInterWortise.createAd();
                                    interAdListener.onClick(pos, type);
                                }

                                @Override
                                public void onInterstitialImpression(@NonNull InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialFailedToShow(@NonNull InterstitialAd interstitialAd, @NonNull AdError adError) {

                                }

                                @Override
                                public void onInterstitialLoaded(@NonNull InterstitialAd interstitialAd) {

                                }

                                @Override
                                public void onInterstitialShown(@NonNull InterstitialAd interstitialAd) {

                                }
                            });
                            adManagerInterWortise.getAd().showAd();
                        } else {
                            AdManagerInterWortise.setAd(null);
                            adManagerInterWortise.createAd();
                            interAdListener.onClick(pos, type);
                        }
                        break;
                }
            } else {
                interAdListener.onClick(pos, type);
            }
        } else {
            interAdListener.onClick(pos, type);
        }
    }

    public Boolean checkPer() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if ((ContextCompat.checkSelfPermission(context, READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(context, READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)) {
                ((Activity) context).requestPermissions(new String[]{READ_MEDIA_VIDEO, READ_MEDIA_IMAGES}, 22);
                return false;
            } else {
                return true;
            }
        } else if (android.os.Build.VERSION.SDK_INT >= 29) {
            if ((ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                ((Activity) context).requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, 22);
                return false;
            } else {
                return true;
            }
        } else {
            if ((ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

                ((Activity) context).requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, 22);
                return false;
            }
            return true;
        }
    }

    public Boolean checkPerLocation() {
        if ((ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ((Activity) context).requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 103);
            return false;
        } else {
            return true;
        }
    }

    public void checkPerNotification() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if ((ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                ((Activity) context).requestPermissions(new String[]{POST_NOTIFICATIONS}, 103);
            }
        }
    }

    public Boolean getPerNotificationStatus() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void permissionDialog() {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_permission, null);

            BottomSheetDialog dialog_per = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
            dialog_per.setContentView(view);
            dialog_per.show();

            MaterialButton button = dialog_per.findViewById(R.id.button_permission);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkPerNotification();
                    dialog_per.dismiss();
                }
            });
        }
    }

    public void openNotiPermissionDialog() {
        View view = ((Activity)context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_theme = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_theme.setContentView(view);
        dialog_theme.show();

        MaterialButton btn_cancel = dialog_theme.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_allow = dialog_theme.findViewById(R.id.btn_del_ac_delete);
        TextView tv1 = dialog_theme.findViewById(R.id.tv1);
        TextView tv2 = dialog_theme.findViewById(R.id.tv2);

        btn_allow.setText(context.getString(R.string.allow));

        tv1.setText(context.getString(R.string.noti_permissions));
        tv2.setText(context.getString(R.string.allow_noti_background_download));

        btn_cancel.setOnClickListener(v -> dialog_theme.dismiss());

        btn_allow.setOnClickListener(view1 -> {
            dialog_theme.dismiss();
            checkPerNotification();
        });
    }

    public boolean isLoggedAndVerified(boolean showToast) {
        if (new SharedPref(context).isLogged()) {
            if (new SharedPref(context).getIsEmailVerified()) {
                return true;
            } else {
                if (showToast) {
                    showToast(context.getString(R.string.err_verify_email));
                } else {
                    openEmailVerifyDialog();
                }
                return false;
            }
        } else {
            showToast(context.getString(R.string.err_login_to_continue));
            return false;
        }
    }

    public SpannableString createSpannableStringBio(TextView tv_bio, String text) {
        SpannableStringBuilder spannableString = new SpannableStringBuilder(text);

        // Handle hashtags
        int hashtagStartIndex = text.indexOf("#");
        while (hashtagStartIndex != -1) {
            int hashtagEndIndex = text.indexOf(" ", hashtagStartIndex);
            if (hashtagEndIndex == -1) {
                hashtagEndIndex = text.length();
            }
            String word = text.substring(hashtagStartIndex, hashtagEndIndex);
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {

                    Intent intent = new Intent(context, PostByTagActivity.class);
                    intent.putExtra("tag", word.replace("#",""));
                    context.startActivity(intent);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };

            spannableString.setSpan(clickableSpan, hashtagStartIndex, hashtagEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.primary)), hashtagStartIndex, hashtagEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            hashtagStartIndex = text.indexOf("#", hashtagEndIndex);
        }

        // Handle emails using a regular expression
        String emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile(emailRegex);
        java.util.regex.Matcher emailMatcher = emailPattern.matcher(text);

        while (emailMatcher.find()) {
            int emailStartIndex = emailMatcher.start();
            int emailEndIndex = emailMatcher.end();

            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    // Handle email click
                    // You can open an email-related activity or perform any desired action
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            };

            spannableString.setSpan(clickableSpan, emailStartIndex, emailEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableString.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.red)), emailStartIndex, emailEndIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // Handle URLs
        Linkify.addLinks(spannableString, Linkify.WEB_URLS);
        tv_bio.setLinkTextColor(context.getResources().getColor(R.color.black)); // Change color for links

        return new SpannableString(spannableString);
    }

    public void openMoreDialog(ItemPost itemPost, MoreOptionListener moreOptionListener) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_more, null);

        BottomSheetDialog dialog_more = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_more.setContentView(view);
        dialog_more.show();

//        ImageView ll_share = dialog_more.findViewById(R.id.ll_more_share);
        LinearLayout ll_fav = dialog_more.findViewById(R.id.ll_more_fav);
        ImageView iv_fav = dialog_more.findViewById(R.id.iv_more_fav);
        LinearLayout ll_download = dialog_more.findViewById(R.id.ll_more_download);
        LinearLayout ll_report = dialog_more.findViewById(R.id.ll_more_report);
        LinearLayout ll_delete = dialog_more.findViewById(R.id.ll_more_delete);
        LinearLayout ll_edit = dialog_more.findViewById(R.id.ll_more_edit);
        LinearLayout ll_copy = dialog_more.findViewById(R.id.ll_more_copy);

        if (!itemPost.isFavourite()) {
            iv_fav.setImageResource(R.drawable.ic_fav_circle);
        } else {
            iv_fav.setImageResource(R.drawable.ic_fav_circle_hover);
        }

        if (!itemPost.getPostType().equalsIgnoreCase("text")) {
            ll_download.setVisibility(View.VISIBLE);
            ll_copy.setVisibility(View.GONE);
        } else {
            ll_download.setVisibility(View.GONE);
            ll_copy.setVisibility(View.VISIBLE);
        }

        if (!itemPost.getUserId().equals(new SharedPref(context).getUserId())) {
            ll_report.setVisibility(View.VISIBLE);
            ll_delete.setVisibility(View.GONE);
            ll_edit.setVisibility(View.GONE);

            ll_report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isLoggedAndVerified(true)) {
                        dialog_more.dismiss();
                        openReportDialog(itemPost);
                    }
                }
            });
        } else {
            ll_delete.setVisibility(View.VISIBLE);
            ll_edit.setVisibility(View.VISIBLE);
            ll_report.setVisibility(View.GONE);

            ll_delete.setOnClickListener(v -> {
                dialog_more.dismiss();
                moreOptionListener.onUserPostDelete();
            });
        }

        ll_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkAvailable()) {
                    if (isLoggedAndVerified(true)) {
                        getDoFav(itemPost.getPostID(), iv_fav, moreOptionListener);
                    }
                } else {
                    showToast(context.getString(R.string.err_internet_not_connected));
                }
            }
        });

//        ll_share.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                sharePost(itemPost.getPostImage());
//            }
//        });

        ll_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadManager.Request request;
                if (itemPost.getPostType().equalsIgnoreCase("image")) {
                    request = new DownloadManager.Request(Uri.parse(itemPost.getPostImage()))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, context.getString(R.string.app_name) + File.separator + FilenameUtils.getName(itemPost.getPostImage()))
                            .setTitle(itemPost.getCaptions())
                            .setDescription(context.getString(R.string.image))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                } else {
                    request = new DownloadManager.Request(Uri.parse(itemPost.getVideoUrl()))
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, context.getString(R.string.app_name) + File.separator + FilenameUtils.getName(itemPost.getVideoUrl()))
                            .setTitle(itemPost.getCaptions())// Title of the Download Notification
                            .setDescription(context.getString(R.string.video))
                            .setAllowedOverMetered(true)
                            .setAllowedOverRoaming(true);
                }
                DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                long downloadID = downloadManager.enqueue(request);

                showToast(context.getString(R.string.starting_download));

                dialog_more.dismiss();
            }
        });

        ll_copy.setOnClickListener(view1 -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(context.getString(R.string.app_name), itemPost.getCaptions());
            clipboard.setPrimaryClip(clip);

            dialog_more.dismiss();

            showToast(context.getString(R.string.text_copied));
        });

        ll_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_more.dismiss();
                Intent intent = new Intent(context, EditPostActivity.class);
                intent.putExtra("item", itemPost);
                context.startActivity(intent);
            }
        });
    }

    public void sharePost(String imageUrl, String shareUrl, boolean isText) {
        if(isText) {
            new ShareCompat.IntentBuilder(context)
                    .setType("text/plain")
                    .setText(shareUrl + "\n" + context.getString(R.string.share_messages) + "\n" + context.getString(R.string.app_name) + " - " + "https://play.google.com/store/apps/details?id=" + context.getPackageName())
                    .setChooserTitle(context.getString(R.string.share))
                    .startChooser();
        } else {
            new LoadShare().execute(imageUrl, URLUtil.guessFileName(imageUrl, null, null), shareUrl);
        }
    }

    public void getPublicProfile(String userID, FunctionListener functionListener) {
        if (isNetworkAvailable()) {

            Call<RespUserList> call = APIClient.getClient().create(APIInterface.class).getProfile(getAPIRequest(Constants.URL_PUBLIC_PROFILE, new SharedPref(context).getUserId(), "", "", "", "", "", "", "", "", "", userID, ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespUserList> call, @NonNull Response<RespUserList> response) {
                    if (context != null) {
                        if (response.body() != null) {
                            if (response.body().getSuccess().equals("1")) {
                                functionListener.getUserDetails("1", response.body().getUserDetail());
                            } else {
                                functionListener.getUserDetails("0", null);
                            }
                        } else {
                            showToast(context.getString(R.string.err_server_error));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespUserList> call, @NonNull Throwable t) {
                    call.cancel();
                    functionListener.getUserDetails("0", null);
                }
            });
        }
    }

    public void getFollowUnFollow(String postUserID, MaterialButton buttonFollow, MaterialButton buttonParentFollow, ActionDoneListener actionDoneListener) {
        if (isLoggedAndVerified(true)) {
            if (isNetworkAvailable()) {

                Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getFollowUnFollow(getAPIRequest(Constants.URL_FOLLOW_UNFOLLOW, postUserID, "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        if (response.body() != null) {
                            if (response.body().getSuccess() != null) {
                                Constants.isUserFollowingChanged = true;
                                if (response.body().getSuccess().equals("true")) {
                                    // if user profile is private it will request otherwise it will be directly followed
                                    if(response.body().getType().equals("request")) {
                                        buttonFollow.setText(context.getString(R.string.requested));
                                    } else {
                                        buttonFollow.setText(context.getString(R.string.unfollow));
                                    }
                                } else {
                                    buttonFollow.setText(context.getString(R.string.follow));
                                }

                                if (buttonParentFollow != null) {
                                    Constants.isUserFollowingChanged = true; //Notify follow change in profile fragment
                                    buttonParentFollow.setText(buttonFollow.getText());
                                }

                                if (actionDoneListener != null) {
                                    actionDoneListener.onWorkDone("1", response.body().getSuccess().equals("true"), 0);
                                }

                                GlobalBus.getBus().postSticky(new EventRequested(postUserID, response.body().getSuccess().equals("true"), response.body().getType()));
                            } else {
                                actionDoneListener.onWorkDone("0", false, 0);
                                showToast(context.getString(R.string.err_server_error));
                            }
                        } else {
                            actionDoneListener.onWorkDone("0", false, 0);
                            showToast(context.getString(R.string.err_server_error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                        call.cancel();
                    }
                });
            } else {
                showToast(context.getString(R.string.err_internet_not_connected));
            }
        }
    }

    public void openFollowUnFollowAlert(String postUserID, MaterialButton buttonFollow, MaterialButton buttonParentFollow, ActionDoneListener actionDoneListener) {
        if(!buttonFollow.getText().toString().equals(context.getString(R.string.unfollow))) {
            getFollowUnFollow(postUserID, buttonFollow, buttonParentFollow, actionDoneListener);
        } else {
            View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

            BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
            dialog.setContentView(view);
            dialog.show();

            MaterialButton btn_cancel = dialog.findViewById(R.id.btn_del_ac_cancel);
            MaterialButton btn_delete = dialog.findViewById(R.id.btn_del_ac_delete);
            btn_delete.getBackground().setTint(ContextCompat.getColor(context, R.color.delete));
            TextView tv1 = dialog.findViewById(R.id.tv1);
            TextView tv2 = dialog.findViewById(R.id.tv2);

            tv1.setText(context.getString(R.string.unfollow));
            tv2.setText(context.getString(R.string.sure_unfollow));
            btn_delete.setText(context.getString(R.string.unfollow));

            btn_cancel.setOnClickListener(v -> dialog.dismiss());

            btn_delete.setOnClickListener(view1 -> {
                dialog.dismiss();
                getFollowUnFollow(postUserID, buttonFollow, buttonParentFollow, actionDoneListener);
            });
        }
    }

    public void getAcceptDeclineRequest(String requestID, String postUserID, boolean isAccept, ActionDoneListener actionDoneListener) {
        if (isLoggedAndVerified(true)) {
            if (isNetworkAvailable()) {

                ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(context.getString(R.string.loading));
                progressDialog.show();

                Call<RespSuccess> call;
                if (isAccept) {
                    call = APIClient.getClient().create(APIInterface.class).getUserRequestAccept(getAPIRequest(Constants.URL_USER_REQUESTED_ACCEPT, "", "", "", "", requestID, "", "", "", "", "", "", ""));
                } else {
                    call = APIClient.getClient().create(APIInterface.class).getUserRequestDecline(getAPIRequest(Constants.URL_USER_REQUESTED_DECLINE, "", "", "", "", requestID, "", "", "", "", "", "", ""));
                }
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                        progressDialog.dismiss();
                        if (response.body() != null) {
                            if (response.body().getSuccess() != null) {
                                Constants.isUserFollowingChanged = true;

                                if (actionDoneListener != null) {
                                    actionDoneListener.onWorkDone("1", response.body().getSuccess().equals("true"), 0);
                                }

                                if (isAccept) {
                                    GlobalBus.getBus().postSticky(new EventRequested(postUserID, response.body().getSuccess().equals("true"), ""));
                                }
                            } else {
                                actionDoneListener.onWorkDone("0", false, 0);
                                showToast(context.getString(R.string.err_server_error));
                            }
                        } else {
                            actionDoneListener.onWorkDone("0", false, 0);
                            showToast(context.getString(R.string.err_server_error));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                        progressDialog.dismiss();
                        call.cancel();
                    }
                });
            } else {
                showToast(context.getString(R.string.err_internet_not_connected));
            }
        }
    }

    public void getDoLike(String postID, MoreOptionListener moreOptionListener) {
        Call<RespLike> call = APIClient.getClient().create(APIInterface.class).getDoLike(getAPIRequest(Constants.URL_DO_LIKE, postID, "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

        call.enqueue(new Callback<RespLike>() {
            @Override
            public void onResponse(@NonNull Call<RespLike> call, @NonNull Response<RespLike> response) {
                if (response.body() != null && response.body().getArrayListSuccess() != null && response.body().getArrayListSuccess().size() > 0) {
                    moreOptionListener.onFavDone("1", response.body().getArrayListSuccess().get(0).getMessage().equals("Like"), response.body().getArrayListSuccess().get(0).getTotalLikes());
//                        showToast(response.body().getArrayListSuccess().get(0).getMessage());
                } else {
                    showToast(context.getString(R.string.err_server_error));
                    moreOptionListener.onFavDone("0", false, 0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespLike> call, @NonNull Throwable t) {
                call.cancel();
                showToast(context.getString(R.string.err_server_error));
                moreOptionListener.onFavDone("0", false, 0);
            }
        });
    }

    public void getDoFav(String postID, ImageView iv_fav, MoreOptionListener moreOptionListener) {
        Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDoFavourite(getAPIRequest(Constants.URL_DO_FAV, postID, "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

        call.enqueue(new Callback<RespSuccess>() {
            @Override
            public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                if (response.body() != null && response.body().getSuccess() != null) {
                    if (response.body().getSuccess().equals("true")) {
                        if (iv_fav != null) {
                            iv_fav.setImageResource(R.drawable.ic_fav_circle_hover);
                        }
                        moreOptionListener.onFavDone("1", true, 0);
                    } else {
                        if (iv_fav != null) {
                            iv_fav.setImageResource(R.drawable.ic_fav_circle);
                        }
                        moreOptionListener.onFavDone("1", false, 0);
                    }
//                        showToast(response.body().getMessage());
                } else {
                    moreOptionListener.onFavDone("0", false, 0);
                    showToast(context.getString(R.string.err_server_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                call.cancel();
                showToast(context.getString(R.string.err_server_error));
            }
        });
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

//        private PowerManager.WakeLock mWakeLock;
        String videoUrl, shareUrl, fileName;
        ProgressDialog mProgressDialog;
        File file;

        public DownloadTask(String videoUrl, String shareUrl) {
            this.videoUrl = videoUrl;
            this.shareUrl = shareUrl;
            fileName = URLUtil.guessFileName(videoUrl, null, null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("A message");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);

            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            String filePath = context.getExternalCacheDir().getAbsoluteFile().getAbsolutePath() + File.separator + fileName;

            file = new File(filePath);
            if (!file.exists()) {
                InputStream input = null;
                OutputStream output = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(videoUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    // expect HTTP 200 OK, so we don't mistakenly save error report
                    // instead of the file
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        return "Server returned HTTP " + connection.getResponseCode()
                                + " " + connection.getResponseMessage();
                    }

                    // this will be useful to display download percentage
                    // might be -1: server did not report the length
                    int fileLength = connection.getContentLength();

                    input = connection.getInputStream();
                    output = new FileOutputStream(filePath);

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            return null;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                } catch (Exception e) {
                    return e.toString();
                } finally {
                    try {
                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();
                    } catch (IOException ignored) {
                    }

                    if (connection != null)
                        connection.disconnect();
                }
                return null;
            } else {
                return "true";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
//            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null) {
                showToast(context.getString(R.string.err_server_error));
            } else {
                Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName().concat(".fileprovider"), file);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
                share.putExtra(Intent.EXTRA_TEXT, shareUrl + "\n" + context.getString(R.string.share_messages) + "\n" + context.getString(R.string.app_name) + " - " + "https://play.google.com/store/apps/details?id=" + context.getPackageName());
                share.putExtra(Intent.EXTRA_STREAM, contentUri);
                context.startActivity(Intent.createChooser(share, context.getString(R.string.share)));
            }
        }

    }

    public class LoadShare extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        String filePath, shareUrl;
        File file;

        LoadShare() {

        }

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context, android.app.AlertDialog.THEME_HOLO_LIGHT);
            pDialog.setMessage(context.getResources().getString(R.string.loading));
            pDialog.setIndeterminate(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String name = strings[1];
            shareUrl = strings[2];
            try {
                filePath = context.getExternalCacheDir().getAbsoluteFile().getAbsolutePath() + File.separator + name;
                file = new File(filePath);
                if (!file.exists()) {
                    URL url = new URL(strings[0]);

                    InputStream inputStream;

                    if (strings[0].contains("https://")) {
                        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Accept", "*/*");
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();
                        inputStream = urlConnection.getInputStream();
                    } else {
                        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Accept", "*/*");
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();
                        inputStream = urlConnection.getInputStream();
                    }

                    if (file.createNewFile()) {
                        file.createNewFile();
                    }

                    FileOutputStream fileOutput = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int bufferLength = 0;
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                    }
                    fileOutput.close();
                    return "1";
                } else {
                    return "2";
                }
            } catch (MalformedURLException e) {
                return "0";
            } catch (IOException e) {
                return "0";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            pDialog.dismiss();

            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName().concat(".fileprovider"), file);

//            Intent share = new Intent(Intent.ACTION_SEND);
//            share.setDataAndType(contentUri, context.getContentResolver().getType(contentUri));
//            share.putExtra(Intent.EXTRA_TEXT, shareUrl + "\n" + context.getString(R.string.share_messages) + "\n" + context.getString(R.string.app_name) + " - " + "https://play.google.com/store/apps/details?id=" + context.getPackageName());
//            share.putExtra(Intent.EXTRA_STREAM, contentUri);
//            context.startActivity(Intent.createChooser(share, context.getString(R.string.share)));

            new ShareCompat.IntentBuilder(context)
                    .setType(context.getContentResolver().getType(contentUri))
                    .setText(shareUrl + "\n" + context.getString(R.string.share_messages) + "\n" + context.getString(R.string.app_name) + " - " + "https://play.google.com/store/apps/details?id=" + context.getPackageName())
                    .setStream(contentUri)
                    .setChooserTitle(context.getString(R.string.share))
                    .startChooser();

            super.onPostExecute(s);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void openCommentDialog(ItemPost itemPost) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_comments, null);

        BottomSheetDialog dialog_comment = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_comment.setContentView(view);

        dialog_comment.show();

        Constants.isEditComment = false;

        adapterComments = null;
        ArrayList<ItemComments> arrayList_comments = new ArrayList<>();

        RecyclerView rv_comments = dialog_comment.findViewById(R.id.rv_comments);
        ImageView iv_close = dialog_comment.findViewById(R.id.iv_comments_close);
        ImageView iv_user = dialog_comment.findViewById(R.id.ic_comments_user);
        TextInputEditText et_comment = dialog_comment.findViewById(R.id.et_comment);
        TextInputLayout textInputLayout = dialog_comment.findViewById(R.id.til_email);
        TextView tv_comments_total = dialog_comment.findViewById(R.id.tv_comments_total);
        LinearLayout ll_likes = dialog_comment.findViewById(R.id.ll_comment_likes);
        TextView tv_likes = dialog_comment.findViewById(R.id.tv_comment_total_likes);
        CircularProgressBar pb_comment = dialog_comment.findViewById(R.id.pb_comment);
        ConstraintLayout cl_empty = dialog_comment.findViewById(R.id.cl_empty);
        TextView tv_empty = dialog_comment.findViewById(R.id.tv_empty);
        tv_empty.setText(context.getString(R.string.err_no_comments_found));

        tv_comments_total.setText(itemPost.getTotalComments());
        tv_likes.setText(formatNumber(itemPost.getTotalLikes()));

        rv_comments.setLayoutManager(new LinearLayoutManager(context));

        Picasso.get().load(new SharedPref(context).getUserImage()).placeholder(R.drawable.placeholder).into(iv_user);

        ll_likes.setOnClickListener(view1 -> {
            openPostLikesUsersList(itemPost.getPostID());
        });

        getPostDetails(itemPost.getPostID(), new CommentListListener() {
            @Override
            public void onDataReceived(String success, ArrayList<ItemComments> arrayListComments) {
                pb_comment.setVisibility(View.GONE);
                if(success.equals("1") && arrayListComments != null) {
                    arrayList_comments.addAll(arrayListComments);
                }
                adapterComments = new AdapterComments(context, arrayList_comments, new SharedPref(context).getUserId(), new EditCommentListener() {
                    @Override
                    public void onEdit(int pos) {
                        try {
                            et_comment.setText(arrayList_comments.get(pos).getCommentText());
                            et_comment.setSelection(et_comment.getText().length());
                            Constants.isEditComment = true;
                            editCommentPos = pos;

                            et_comment.setFocusable(true);
                            et_comment.requestFocus();
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        } catch (Exception ignore){}
                    }

                    @Override
                    public void onDelete() {
                        if (arrayList_comments.isEmpty()) {
                            rv_comments.setVisibility(View.GONE);
                            cl_empty.setVisibility(View.VISIBLE);
                        }
                        tv_comments_total.setText(String.valueOf(arrayList_comments.size()));
                    }
                });
                rv_comments.setAdapter(adapterComments);

                if (!arrayList_comments.isEmpty()) {
                    rv_comments.setVisibility(View.VISIBLE);
                    cl_empty.setVisibility(View.GONE);
                } else {
                    rv_comments.setVisibility(View.GONE);
                    cl_empty.setVisibility(View.VISIBLE);
                }
            }
        });


        iv_close.setOnClickListener(v -> {
            dialog_comment.dismiss();
        });

        et_comment.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                et_comment.setFocusableInTouchMode(true);
                return false;
            }
        });

        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    textInputLayout.getEndIconDrawable().setColorFilter(ContextCompat.getColor(context, R.color.primary), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    textInputLayout.getEndIconDrawable().setColorFilter(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_comment.setFocusable(false);
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_comment.getWindowToken(), 0);

                if (isLoggedAndVerified(true)) {
                    if (!et_comment.getText().toString().trim().isEmpty()) {
                        getPostComment(itemPost.getPostID(), et_comment.getText().toString(), !Constants.isEditComment ? "" : arrayList_comments.get(editCommentPos).getId(), dialog_comment, new CommentAddListener() {
                            @Override
                            public void onDataReceived(String success, ItemComments itemComments) {
                                if (!Constants.isEditComment) {
                                    arrayList_comments.add(0, itemComments);
                                    adapterComments.notifyItemInserted(0);

                                    tv_comments_total.setText(arrayList_comments.get(0).getTotalComments());

                                    if (cl_empty.getVisibility() == View.VISIBLE) {
                                        rv_comments.setVisibility(View.VISIBLE);
                                        cl_empty.setVisibility(View.GONE);
                                    }
                                } else {
                                    arrayList_comments.get(editCommentPos).setCommentText(et_comment.getText().toString());
                                    adapterComments.notifyItemChanged(editCommentPos);
                                }
                                et_comment.setText("");
                                Constants.isEditComment = false;
                            }
                        });
                    }
                }
            }
        });
    }

    private void getPostComment(String postID, String commentText, String commentID, BottomSheetDialog dialog_comment, CommentAddListener commentAddListener) {
        if (isNetworkAvailable()) {

            Call<RespPostComment> call = APIClient.getClient().create(APIInterface.class).getDoPostComments(getAPIRequest(Constants.URL_POST_COMMENTS, postID, "", "", commentText, commentID, "", "", "", "", "", new SharedPref(context).getUserId(), ""));

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostComment> call, @NonNull Response<RespPostComment> response) {
                    if (response.body() != null && response.body().getSuccess().equals("1") && response.body().getCommentDetail() != null && response.body().getCommentDetail().size() > 0) {
                        showToast(response.body().getCommentDetail().get(0).getMessage());

                        commentAddListener.onDataReceived(response.body().getSuccess(), response.body().getCommentDetail().get(0));
                    } else {
                        showToast(context.getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostComment> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private void getPostDetails(String postID, CommentListListener commentListListener) {
        if (isNetworkAvailable()) {

            Call<RespPostDetails> call = APIClient.getClient().create(APIInterface.class).getPostDetails(getAPIRequest(Constants.URL_POST_DETAILS, postID, "", "", "", "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespPostDetails> call, @NonNull Response<RespPostDetails> response) {
                    if (response.body() != null && response.body().getStatusCode().equals("200") && response.body().getItemPost() != null && response.body().getItemPost().getArrayListComments() != null) {
                        commentListListener.onDataReceived("1", response.body().getItemPost().getArrayListComments());
                    } else {
                        commentListListener.onDataReceived("0", null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespPostDetails> call, @NonNull Throwable t) {
                    commentListListener.onDataReceived("0", null);
                    call.cancel();
                }
            });
        }
    }

    public void openPostLikesUsersList(String postID) {
        Intent intent = new Intent(context, UserListByPostLikeActivity.class);
        intent.putExtra("id", postID);
        context.startActivity(intent);
    }

    private void openReportDialog(ItemPost itemPost) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_report, null);

        BottomSheetDialog dialog_report = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_report.setContentView(view);
        dialog_report.show();

        MaterialButton btn_submit = dialog_report.findViewById(R.id.btn_report_submit);
        TextInputEditText et_report = dialog_report.findViewById(R.id.et_report);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et_report.getText().toString().trim().isEmpty()) {
                    getDoReport(et_report.getText().toString(), itemPost, dialog_report);
                } else {
                    showToast(context.getString(R.string.err_report_message_empty));
                }
            }
        });
    }

    private void getDoReport(String message, ItemPost itemPost, BottomSheetDialog dialog) {
        if (isNetworkAvailable()) {

            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading..");
            progressDialog.show();

            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getDoReport(getAPIRequest(Constants.URL_REPORT, itemPost.getPostID(), "", "", message, "", "", "", "", "", "", new SharedPref(context).getUserId(), ""));

            call.enqueue(new Callback<RespSuccess>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    progressDialog.dismiss();
                    if (response.body() != null && response.body().getSuccess() != null) {
                        if (response.body().getSuccess().equals("1")) {
                            dialog.dismiss();
                        }
                        showToast(response.body().getMessage());
                    } else {
                        showToast(context.getString(R.string.err_server_error));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                    progressDialog.dismiss();
                    call.cancel();
                    showToast(context.getString(R.string.err_server_error));
                }
            });
        } else {
            showToast(context.getString(R.string.err_internet_not_connected));
        }
    }

    public void openLogoutDialog() {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_theme = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_theme.setContentView(view);
        dialog_theme.show();

        MaterialButton btn_cancel = dialog_theme.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_logout = dialog_theme.findViewById(R.id.btn_del_ac_delete);
        TextView tv1 = dialog_theme.findViewById(R.id.tv1);
        TextView tv2 = dialog_theme.findViewById(R.id.tv2);

        btn_logout.setText(context.getString(R.string.logout));
        tv1.setText(context.getString(R.string.logout));
        tv2.setText(context.getString(R.string.sure_logout));

        btn_cancel.setOnClickListener(v -> dialog_theme.dismiss());

        btn_logout.setOnClickListener(view1 -> {
            dialog_theme.dismiss();
            logout((Activity) context, new SharedPref(context));
        });
    }

    public void openEmailVerifyDialog() {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.layout_bottom_delete_ac, null);

        BottomSheetDialog dialog_theme = new BottomSheetDialog(context, R.style.BottomSheetDialogStyle);
        dialog_theme.setContentView(view);
        dialog_theme.show();

        MaterialButton btn_cancel = dialog_theme.findViewById(R.id.btn_del_ac_cancel);
        MaterialButton btn_delete = dialog_theme.findViewById(R.id.btn_del_ac_delete);
        TextView tv1 = dialog_theme.findViewById(R.id.tv1);
        TextView tv2 = dialog_theme.findViewById(R.id.tv2);

        btn_delete.setText(context.getString(R.string.verify));
        tv1.setText(context.getString(R.string.verify_email));
        tv2.setText(context.getString(R.string.need_verify_email));

        btn_cancel.setOnClickListener(v -> dialog_theme.dismiss());

        btn_delete.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, EmailVerificationActivity.class);
            intent.putExtra("user_id", new SharedPref(context).getUserId());
            intent.putExtra("from", true);
            context.startActivity(intent);
            dialog_theme.dismiss();
        });
    }

    public void showUpdateAlert(String message, boolean isFromSplash) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.ThemeDialog);
        alertDialog.setTitle(context.getString(R.string.update));
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(context.getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String url = Constants.appUpdateURL;
                if (url.isEmpty()) {
                    url = "http://play.google.com/store/apps/details?id=" + context.getPackageName();
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);

                ((Activity) context).finish();
            }
        });
        if (Constants.appUpdateCancel) {
            alertDialog.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (isFromSplash) {
                        new SharedPref(context).setIsLoginShown(true);
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        ((Activity) context).finish();
                    }
                }
            });
        } else {
            alertDialog.setNegativeButton(context.getString(R.string.exit), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) context).finish();
                }
            });
        }
        alertDialog.show();
    }

    public void getUserValidInvalid(SharedPref sharedPref) {
        if (isNetworkAvailable() && sharedPref.isLogged()) {
            Call<RespSuccess> call = APIClient.getClient().create(APIInterface.class).getUserValidInvalid(getAPIRequest(Constants.URL_USER_VALID_INVALID, "", "", "", "", "", "", "", "", "", "", sharedPref.getUserId(), ""));
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<RespSuccess> call, @NonNull Response<RespSuccess> response) {
                    if (response.body() != null && response.body().getSuccess() != null) {
                        sharedPref.setUserValidCheckDate();
                        if (response.body().getSuccess().equals("0")) {
                            showUserInvalidDialog(context.getString(R.string.err_invalid_user), response.body().getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<RespSuccess> call, @NonNull Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    public void getVerifyDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.ThemeDialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                finish();
            }
        });
        alertDialog.show();
    }

    public void showUserInvalidDialog(String title, String message) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.ThemeDialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout((Activity) context, new SharedPref(context));
            }
        });
        alertDialog.show();
    }

    public String getAPIRequest(String method, String objectID, String type, String userBio, String searchText, String itemID, String rate_date, String name, String email, String password, String phone, String userID, String privacy) {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", method);
        jsObj.addProperty("package_name", context.getPackageName());

        switch (method) {
            case Constants.URL_LOGIN -> {
                jsObj.addProperty("email", email);
                jsObj.addProperty("password", password);
            }
            case Constants.URL_SOCIAL_LOGIN -> {
                jsObj.addProperty("name", name);
                jsObj.addProperty("email", email);
                jsObj.addProperty("login_type", type);
                jsObj.addProperty("social_id", objectID);
            }
            case Constants.URL_REGISTRATION -> {
                jsObj.addProperty("name", name);
                jsObj.addProperty("email", email);
                jsObj.addProperty("password", password);
                jsObj.addProperty("phone", phone);
                jsObj.addProperty("date_of_birth", rate_date);
            }
            case Constants.URL_SEND_VERIFY_OTP -> jsObj.addProperty("user_id", userID);
            case Constants.URL_VERIFY_OTP -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("verify_code", searchText);
            }
            case Constants.URL_FORGOT_PASSWORD -> jsObj.addProperty("email", email);
            case Constants.URL_PROFILE, Constants.URL_PUBLIC_PROFILE -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("logged_user_id", objectID);
            }
            case Constants.URL_CHANGE_PASSWORD -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("password", password);
            }
            case Constants.URL_USER_PRIVACY_UPDATE -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("privacy_settings", privacy);
            }
            case Constants.URL_USER_VALID_INVALID -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_FOLLOW_UNFOLLOW -> {
                jsObj.addProperty("following_user_id", userID);
                jsObj.addProperty("followed_user_id", objectID);
            }
            case Constants.URL_HOME, Constants.URL_USER_FOLLOWING, Constants.URL_USER_FOLLOWED_BY_OTHERS, Constants.URL_USER_REQUESTED -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_USER_BY_POST_LIKE -> {
                jsObj.addProperty("post_id", objectID);
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_USER_REQUESTED_ACCEPT, Constants.
                    URL_USER_REQUESTED_DECLINE -> {
                jsObj.addProperty("request_id", itemID);
            }
            case Constants.URL_LATEST -> {
                jsObj.addProperty("user_id", userID);
                if (!type.isEmpty()) {
                    jsObj.addProperty("filter_type", type);
                }
            }
            case Constants.URL_RELATED_POST -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", objectID);
                jsObj.addProperty("post_type", type);
                jsObj.addProperty("post_tags", searchText);
            }
            case Constants.URL_USER_POST, Constants.URL_USER_FAV_POST -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("logged_user_id", objectID);
            }
            case Constants.URL_SEARCH, Constants.URL_SEARCH_USERS -> jsObj.addProperty("search_text", searchText);
            case Constants.URL_SEARCH_TAG -> jsObj.addProperty("tag", searchText);
            case Constants.URL_POST_DETAILS, Constants.URL_VIEW_POST, Constants.URL_DO_FAV, Constants.URL_DO_LIKE -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", objectID);
                jsObj.addProperty("post_type", "Post");
                jsObj.addProperty("device_id", getDeviceID());
            }
            case Constants.URL_REPORT -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", objectID);
                jsObj.addProperty("post_type", "Post");
                jsObj.addProperty("message", searchText);
            }
            case Constants.URL_POST_COMMENTS -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", objectID);
                jsObj.addProperty("comment_text", searchText);
                if (!itemID.isEmpty()) {
                    jsObj.addProperty("comment_id", itemID);
                }
            }
            case Constants.URL_ADD_POST, Constants.URL_STORY_UPLOAD -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_type", type);
                jsObj.addProperty("caption", searchText);
            }
            case Constants.URL_EDIT_POST -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", itemID);
                jsObj.addProperty("post_type", type);
                jsObj.addProperty("caption", searchText);
            }
            case Constants.URL_TOTAL_POST, Constants.URL_DELETE_ACCOUNT ->
                    jsObj.addProperty("user_id", userID);
            case Constants.URL_DELETE_POST -> jsObj.addProperty("post_id", objectID);
            case Constants.URL_DELETE_COMMENT -> jsObj.addProperty("comment_id", itemID);
            case Constants.URL_CONTACT_US -> {
                jsObj.addProperty("name", name);
                jsObj.addProperty("email", email);
                jsObj.addProperty("phone", phone);
                jsObj.addProperty("subject", type);
                jsObj.addProperty("message", searchText);
            }
            case Constants.URL_GENERATE_CHAT_TOKEN -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_GENERATE_RTM_TOKEN -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_GENERATE_RTC_TOKEN -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("channel_name", searchText);
            }
            case Constants.URL_CUSTOM_ADS -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_STORY_LIST -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_STORY_VIEW -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", objectID);
            }
            case Constants.URL_STORY_USER_VIEW_LIST -> {
                jsObj.addProperty("post_id", objectID);
            }
            case Constants.URL_ACCOUNT_VERIFY_REQUEST -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_WITHDRAW_HISTORY -> {
                jsObj.addProperty("user_id", userID);
            }
            case Constants.URL_WITHDRAW_REQUEST -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("points", searchText);
            }
            case Constants.URL_UPDATE_PAYMENT_INFO -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("payment_info", searchText);
            }
            case Constants.URL_STORY_DELETE -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("post_id", objectID);
            }
        }

//        Log.e("aaa", jsObj.toString());
        return API.toBase64(jsObj.toString());
    }

    public String getAPIRequestProfile(String method, String gender, String userBio, String address, String lat, String longitude, String date, String name, String email, String phone, String username, String linkTitle_1, String link_1, String linkTitle_2, String link_2, String linkTitle_3, String link_3, String linkTitle_4, String link_4, String linkTitle_5, String link_5, String userID) {
        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", method);
        jsObj.addProperty("package_name", context.getPackageName());

        switch (method) {
            case Constants.URL_PROFILE_UPDATE -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("username", username);
                jsObj.addProperty("name", name);
                jsObj.addProperty("email", email);
                jsObj.addProperty("phone", phone);
                jsObj.addProperty("date_of_birth", date);
                jsObj.addProperty("gender", gender);
                jsObj.addProperty("address", address);
                jsObj.addProperty("user_lat", lat);
                jsObj.addProperty("user_long", longitude);
                jsObj.addProperty("user_bio", userBio);
            }
            case Constants.URL_USER_LINKS_UPDATE -> {
                jsObj.addProperty("user_id", userID);
                jsObj.addProperty("link1_title", linkTitle_1);
                jsObj.addProperty("link1", link_1);
                jsObj.addProperty("link2_title", linkTitle_2);
                jsObj.addProperty("link2", link_2);
                jsObj.addProperty("link3_title", linkTitle_3);
                jsObj.addProperty("link3", link_3);
                jsObj.addProperty("link4_title", linkTitle_4);
                jsObj.addProperty("link4", link_4);
                jsObj.addProperty("link5_title", linkTitle_5);
                jsObj.addProperty("link5", link_5);
            }
            case Constants.URL_CHECK_USERNAME -> jsObj.addProperty("username", username);
        }

//        Log.e("aaa", jsObj.toString());
        return API.toBase64(jsObj.toString());
    }

    public void animateHeartButton(final View v) {
        final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
        final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
        final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(v, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(v, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }

    public static class CustomLinkMovementMethod extends ScrollingMovementMethod {
        @Override
        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                Layout layout = widget.getLayout();
                if (layout != null) {
                    int line = layout.getLineForVertical(widget.getScrollY() + (int) event.getY());
                    int off = layout.getOffsetForHorizontal(line, event.getX());
                    ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

                    if (link.length != 0) {
                        link[0].onClick(widget);
                        return true;
                    }
                }
            }
            return Touch.onTouchEvent(widget, buffer, event);
        }
    }

    public SpannableString highlightHashtagsAndMentions(String text, int textColor, int hashTagColor) {
        SpannableString spannableString = new SpannableString(text);

        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, textColor)),
                0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Regex for hashtags and mentions
        Pattern pattern = Pattern.compile("(#\\w+|@\\w+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String match = matcher.group();
            int start = matcher.start();
            int end = matcher.end();

            // Add clickable span
            spannableString.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    if(match.startsWith("#")) {
                        Intent intent = new Intent(context, PostByTagActivity.class);
                        intent.putExtra("tag", match.replace("#", ""));
                        context.startActivity(intent);
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Add color span
            if (match.startsWith("#")) {
                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, hashTagColor)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else if (match.startsWith("@")) {
                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannableString;
    }

//    public SpannableString highlightHashtagsAndMentions(TextView textView, String text, String type, int textColor, int hashTagColor, boolean isLong) {
//        if(isLong && !text.contains("...See Less")) {
//            text = text.concat("...See Less");
//        }
//        SpannableString spannableString = new SpannableString(text);
//
//        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, textColor)),
//                0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        // Regex for hashtags and mentions
//        Pattern pattern = Pattern.compile("(#\\w+|@\\w+|\\.\\.\\.See Less)");
//        Matcher matcher = pattern.matcher(text);
//
//        while (matcher.find()) {
//            String match = matcher.group();
//            int start = matcher.start();
//            int end = matcher.end();
//
//            // Add clickable span
//            String finalText = text;
//            spannableString.setSpan(new ClickableSpan() {
//                @Override
//                public void onClick(@NonNull View widget) {
//                    if(match.startsWith("#")) {
//                        Intent intent = new Intent(context, PostByTagActivity.class);
//                        intent.putExtra("tag", match.replace("#", ""));
//                        context.startActivity(intent);
//                    } else if(match.equals("...See Less")) {
//                        addSeeMoreFunctionality(textView, finalText, type, type.equalsIgnoreCase("text") ? 5 : 2, textColor, hashTagColor);
//                    }
//                }
//
//                @Override
//                public void updateDrawState(@NonNull TextPaint ds) {
//                    super.updateDrawState(ds);
//                    ds.setUnderlineText(false);
//                }
//            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//            // Add color span
//            if (match.startsWith("#")) {
//                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, hashTagColor)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            } else if (match.startsWith("@")) {
//                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            } else if (match.startsWith("...See Less")) {
//                spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.text_see_more)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//            }
//        }
//
//        return spannableString;
//    }
//
//    public void addSeeMoreFunctionality(final TextView textView, final String fullText, String type, final int maxLines, int textColor, int hashTagColor) {
//
//        Spannable highlightedText = highlightHashtagsAndMentions(textView, fullText, type, textColor, hashTagColor, false);
//        textView.setText(highlightedText);
//        textView.setMovementMethod(new Methods.CustomLinkMovementMethod());
//
//        textView.post(() -> {
//            Layout layout = textView.getLayout();
//            if (layout != null && layout.getLineCount() > maxLines) {
//                // Find the end of the visible text for truncation
//                int endOfVisibleText = layout.getLineEnd(maxLines - 1);
//
//                // Ensure the end index is within bounds
//                if (endOfVisibleText > fullText.length()) {
//                    endOfVisibleText = fullText.length();
//                }
//
//                // Append " ...See More"
//                String seeMoreText = context.getString(R.string.see_more);
//
//                Paint paint = textView.getPaint();
//                float seeMoreWidth = paint.measureText(seeMoreText);
//                int seeMoreChars = (int) (seeMoreWidth / paint.measureText(" "));
//
//                endOfVisibleText = Math.max(0, endOfVisibleText - seeMoreChars);
//
//                // Truncate the visible text
//                String visibleText = fullText.substring(0, endOfVisibleText).trim();
//
//                SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(visibleText + seeMoreText);
//
//                // Add "See More" clickable span
//                int seeMoreStart = visibleText.length();
//                spannableBuilder.setSpan(new ClickableSpan() {
//                    @Override
//                    public void onClick(@NonNull View widget) {
//                        // Expand text on click
//
//                        Spannable highlightedTextSeeLess = highlightHashtagsAndMentions(textView, fullText, type, textColor, hashTagColor, true);
//                        textView.setText(highlightedTextSeeLess);
//                        textView.setMaxLines(Integer.MAX_VALUE); // Remove line limit
//                        textView.setMovementMethod(LinkMovementMethod.getInstance()); // Re-enable spans
//                    }
//
//                    @Override
//                    public void updateDrawState(@NonNull TextPaint ds) {
//                        super.updateDrawState(ds);
//                        ds.setUnderlineText(false);
//                        ds.setColor(ContextCompat.getColor(textView.getContext(), R.color.text_see_more)); // Color for "See More"
//                    }
//                }, seeMoreStart, spannableBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//                // Copy spans for hashtags/mentions
//                Object[] spans = highlightedText.getSpans(0, endOfVisibleText, Object.class);
//                for (Object span : spans) {
//                    int spanStart = highlightedText.getSpanStart(span);
//                    int spanEnd = highlightedText.getSpanEnd(span);
//
//                    // Ensure span indices are within bounds and do not overlap with "See More"
//                    if (spanStart < seeMoreStart) {
//                        spannableBuilder.setSpan(span, spanStart, Math.min(spanEnd, seeMoreStart), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//                    }
//                }
//
//                // Set the truncated text with "See More"
//                textView.setText(spannableBuilder);
//                textView.setMaxLines(maxLines); // Ensure maxLines is applied
//                textView.setMovementMethod(LinkMovementMethod.getInstance()); // Enable clickable spans
//            } else {
//
//            }
//        });
//    }
}
