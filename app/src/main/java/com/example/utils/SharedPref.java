package blogtalk.com.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SharedPref {

    Context context;
    EncryptData encryptData;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String TAG_IS_INTRO= "is_intro", TAG_IS_LOGIN_SHOWN = "is_login_shown", TAG_UID = "uid", TAG_USERNAME = "username", TAG_NAME = "name",
            TAG_EMAIL = "email", TAG_MOBILE = "mobile", TAG_USER_IMAGE = "userImage", TAG_IS_EMAIL_VERIFIED = "email_verified", TAG_REMEMBER = "rem",
            TAG_PASSWORD = "pass", SHARED_PREF_AUTOLOGIN = "autologin", TAG_LOGIN_TYPE = "loginType", TAG_AUTH_ID = "auth_id",
            TAG_NIGHT_MODE = "nightmode", TAG_IS_LOGGED = "islogged", TAG_AD_IS_BANNER = "isbanner", TAG_AD_IS_INTER = "isinter",
            TAG_AD_IS_NATIVE = "isnative", TAG_AD_ID_BANNER = "id_banner", TAG_AD_ID_INTER = "id_inter", TAG_AD_ID_NATIVE = "id_native",
            TAG_AD_NATIVE_POS = "native_pos", TAG_AD_INTER_POS = "inter_pos", TAG_AD_TYPE_BANNER = "type_banner", TAG_AD_TYPE_INTER = "type_inter",
            TAG_AD_TYPE_NATIVE = "type_native", TAG_STARTAPP_ID = "startapp_id", TAG_COLOR_SAVE = "color_save",
            TAG_FB = "fb", TAG_INSTA = "insta", TAG_TWITTER = "twitter", TAG_YOUTUBE = "youtube", TAG_PROF_COMPLETE = "prof_complete",
            TAG_GENDER = "gender", TAG_ADDRESS="address", TAG_LATITUDE="latitude", TAG_LONGITUDE="longitude", TAG_SHARE_URL="share_url",
            TAG_USER_BIO = "user_bio", TAG_BIRTH_DATE="birthdate", TAG_PROFILE_SECURITY = "prof_sec", TAG_NEW_NOTIFICATION = "new_noti",
            TAG_LINK_TITLE_1="link_title_1", TAG_LINK_TITLE_2="link_title_2", TAG_LINK_TITLE_3="link_title_3", TAG_LINK_TITLE_4="link_title_4",
            TAG_LINK_TITLE_5="link_title_5", TAG_LINK_1="link_1", TAG_LINK_2="link_2", TAG_LINK_3="link_3", TAG_LINK_4="link_4", TAG_LINK_5="link_5",
            TAG_IS_CHAT_REGISTERED ="is_chat_reigs", TAG_IS_CHAT_LOAD_SERVER ="is_chat_load_server", TAG_USER_CHECK_DATE = "user_check_date",
            TAG_IS_CHAT_ON = "is_chat_on", TAG_IS_VOICE_CHAT_ON = "is_voice_chat_on", TAG_USER_ACCOUNT_VERIFY_ON = "is_user_account_verify_on",
            TAG_POINTS_ON = "is_points_on", TAG_MIN_POINTS_WITHDRAW = "min_points_withdraw", TAG_CURRENCY_CODE = "currency_code",
            TAG_CHECK_USER_VALID = "check_user_valid", TAG_UPLOAD_POST_TYPE = "upload_post_type", TAG_IS_VOICE_PERMISSION_ASKED = "is_voice_per";

    public SharedPref(Context context) {
        this.context = context;
        encryptData = new EncryptData(context);
        sharedPreferences = context.getSharedPreferences("status", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsNotification() {
        return sharedPreferences.getBoolean("noti", true);
    }

    public void setIsNotification(Boolean isNotification) {
        editor.putBoolean("noti", isNotification);
        editor.apply();
    }

    public void setIsLogged(Boolean isLogged) {
        editor.putBoolean(TAG_IS_LOGGED, isLogged);
        editor.apply();
    }

    public boolean isLogged() {
        return sharedPreferences.getBoolean(TAG_IS_LOGGED, false);
    }

    public Boolean isIntroShown() {
        return sharedPreferences.getBoolean(TAG_IS_INTRO, false);
    }

    public void setIsIntroShown(Boolean isIntroShown) {
        editor.putBoolean(TAG_IS_INTRO, isIntroShown);
        editor.apply();
    }

    public Boolean isLoginShown() {
        return sharedPreferences.getBoolean(TAG_IS_LOGIN_SHOWN, false);
    }

    public void setIsLoginShown(Boolean isLoginShown) {
        editor.putBoolean(TAG_IS_LOGIN_SHOWN, isLoginShown);
        editor.apply();
    }

    public void setLoginDetails(String id, String name, String mobile, String email, String userImage, String authID, Boolean isRemember, String password, String loginType, String isEmailVerified, int profileComplete) {
        editor.putBoolean(TAG_REMEMBER, isRemember);
        editor.putString(TAG_UID, id);
        editor.putString(TAG_NAME, encryptData.encrypt(name));
        editor.putString(TAG_MOBILE, encryptData.encrypt(mobile));
        editor.putString(TAG_EMAIL, encryptData.encrypt(email));
        editor.putString(TAG_USER_IMAGE, encryptData.encrypt(userImage));
        editor.putString(TAG_PASSWORD, encryptData.encrypt(password));
        editor.putString(TAG_LOGIN_TYPE, encryptData.encrypt(loginType));
        editor.putString(TAG_AUTH_ID, encryptData.encrypt(authID));
        editor.putBoolean(TAG_IS_EMAIL_VERIFIED, isEmailVerified.equals("Yes"));
        editor.putInt(TAG_PROF_COMPLETE, profileComplete);
        editor.apply();
    }

    public void setSocialDetails() {
        editor.putString(TAG_FB, Constants.urlFacebook);
        editor.putString(TAG_INSTA, Constants.urlInstagram);
        editor.putString(TAG_TWITTER, Constants.urlTwitter);
        editor.putString(TAG_YOUTUBE, Constants.urlYoutube);
        editor.apply();
    }

    public void setRemember(Boolean isRemember) {
        editor.putBoolean(TAG_REMEMBER, isRemember);
        editor.putString(TAG_PASSWORD, "");
        editor.apply();
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        editor.putBoolean(TAG_IS_EMAIL_VERIFIED, isEmailVerified);
        editor.apply();
    }

    public boolean getIsEmailVerified() {
        return sharedPreferences.getBoolean(TAG_IS_EMAIL_VERIFIED, false);
    }

    public String getUserId() {
        return sharedPreferences.getString(TAG_UID, "");
    }

    public String getEncryptedUserId() {
        return encryptData.encrypt(sharedPreferences.getString(TAG_UID, ""));
    }

    public void setUserID(String userID) {
        editor.putString(TAG_UID, userID);
        editor.apply();
    }

    public void setName(String name) {
        editor.putString(TAG_NAME, encryptData.encrypt(name));
        editor.apply();
    }

    public String getName() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_NAME, ""));
    }

    public void setUserName(String userName) {
        editor.putString(TAG_USERNAME, encryptData.encrypt(userName));
        editor.apply();
    }

    public String getUserName() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_USERNAME, ""));
    }

    public void setEmail(String email) {
        editor.putString(TAG_EMAIL, encryptData.encrypt(email));
        editor.apply();
    }

    public String getEmail() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_EMAIL, ""));
    }

    public void setUserMobile(String mobile) {
        editor.putString(TAG_MOBILE, encryptData.encrypt(mobile));
        editor.apply();
    }

    public String getUserPhone() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_MOBILE, ""));
    }

    public void setUserImage(String image) {
        editor.putString(TAG_USER_IMAGE, encryptData.encrypt(image));
        editor.apply();
    }

    public String getUserImage() {
        String image = encryptData.decrypt(sharedPreferences.getString(TAG_USER_IMAGE, "null"));
        return !image.isEmpty() ? image : "null";
    }

    public String getPassword() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_PASSWORD, ""));
    }

    public Boolean getIsRemember() {
        return sharedPreferences.getBoolean(TAG_REMEMBER, false);
    }

    public Boolean getIsAutoLogin() {
        return sharedPreferences.getBoolean(SHARED_PREF_AUTOLOGIN, false);
    }

    public void setIsAutoLogin(Boolean isAutoLogin) {
        editor.putBoolean(SHARED_PREF_AUTOLOGIN, isAutoLogin);
        editor.apply();
    }

    public String getLoginType() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_LOGIN_TYPE, ""));
    }

    public String getAuthID() {
        return encryptData.decrypt(sharedPreferences.getString(TAG_AUTH_ID, ""));
    }

    public String getDarkMode() {
        return sharedPreferences.getString(TAG_NIGHT_MODE, Constants.DARK_MODE_SYSTEM);
    }

    public void setDarkMode(String nightMode) {
        editor.putString(TAG_NIGHT_MODE, nightMode);
        editor.apply();
    }

    public String getGender() {
        return sharedPreferences.getString(TAG_GENDER, "");
    }

    public void setGender(String gender) {
        editor.putString(TAG_GENDER, gender);
        editor.apply();
    }

    public String getBirthdate() {
        return sharedPreferences.getString(TAG_BIRTH_DATE, "");
    }

    public void setBirthdate(String birthdate) {
        editor.putString(TAG_BIRTH_DATE, birthdate);
        editor.apply();
    }

    public String getAddress() {
        return sharedPreferences.getString(TAG_ADDRESS, "");
    }

    public void setAddress(String address) {
        editor.putString(TAG_ADDRESS, address);
        editor.apply();
    }

    public String getLatitude() {
        return sharedPreferences.getString(TAG_LATITUDE, "");
    }

    public void setLatitude(String latitude) {
        editor.putString(TAG_LATITUDE, latitude);
        editor.apply();
    }

    public String getLongitude() {
        return sharedPreferences.getString(TAG_LONGITUDE, "");
    }

    public void setLongitude(String longitude) {
        editor.putString(TAG_LONGITUDE, longitude);
        editor.apply();
    }

    public int getProfileComplete() {
        return sharedPreferences.getInt(TAG_PROF_COMPLETE, 20);
    }

    public String getProfileShareUrl() {
        return sharedPreferences.getString(TAG_SHARE_URL, "");
    }

    public void setProfileShareUrl(String latitude) {
        editor.putString(TAG_SHARE_URL, latitude);
        editor.apply();
    }

    public String getProfilePrivacy() {
        return sharedPreferences.getString(TAG_PROFILE_SECURITY, Constants.TAG_PROFILE_PUBLIC);
    }

    public void setProfilePrivacy(String privacy) {
        editor.putString(TAG_PROFILE_SECURITY, privacy);
        editor.apply();
    }

    public void setProfileComplete(int profileComplete) {
        editor.putInt(TAG_PROF_COMPLETE, profileComplete);
        editor.apply();
    }

    public void setUserBio(String userbio) {
        editor.putString(TAG_USER_BIO, userbio);
        editor.apply();
    }

    public String getUserBio() {
        return sharedPreferences.getString(TAG_USER_BIO, "");
    }

    public Boolean isNewNotification() {
        return sharedPreferences.getBoolean(TAG_NEW_NOTIFICATION, false);
    }

    public void setNewNotification(boolean isNewNoti) {
        editor.putBoolean(TAG_NEW_NOTIFICATION, isNewNoti);
        editor.apply();
    }

    public void setLink1Title(String linkTitle) {
        editor.putString(TAG_LINK_TITLE_1, linkTitle);
        editor.apply();
    }

    public String getLink1Title() {
        return sharedPreferences.getString(TAG_LINK_TITLE_1, "");
    }

    public void setLink1(String link) {
        editor.putString(TAG_LINK_1, link);
        editor.apply();
    }

    public String getLink1() {
        return sharedPreferences.getString(TAG_LINK_1, "");
    }

    public void setLink2Title(String linkTitle) {
        editor.putString(TAG_LINK_TITLE_2, linkTitle);
        editor.apply();
    }

    public String getLink2Title() {
        return sharedPreferences.getString(TAG_LINK_TITLE_2, "");
    }

    public void setLink2(String link) {
        editor.putString(TAG_LINK_2, link);
        editor.apply();
    }

    public String getLink2() {
        return sharedPreferences.getString(TAG_LINK_2, "");
    }

    public void setLink3Title(String linkTitle) {
        editor.putString(TAG_LINK_TITLE_3, linkTitle);
        editor.apply();
    }

    public String getLink3Title() {
        return sharedPreferences.getString(TAG_LINK_TITLE_3, "");
    }

    public void setLink3(String link) {
        editor.putString(TAG_LINK_3, link);
        editor.apply();
    }

    public String getLink3() {
        return sharedPreferences.getString(TAG_LINK_3, "");
    }

    public void setLink4Title(String linkTitle) {
        editor.putString(TAG_LINK_TITLE_4, linkTitle);
        editor.apply();
    }

    public String getLink4Title() {
        return sharedPreferences.getString(TAG_LINK_TITLE_4, "");
    }

    public void setLink4(String link) {
        editor.putString(TAG_LINK_4, link);
        editor.apply();
    }

    public String getLink4() {
        return sharedPreferences.getString(TAG_LINK_4, "");
    }

    public void setLink5Title(String linkTitle) {
        editor.putString(TAG_LINK_TITLE_5, linkTitle);
        editor.apply();
    }

    public String getLink5Title() {
        return sharedPreferences.getString(TAG_LINK_TITLE_5, "");
    }

    public void setLink5(String link) {
        editor.putString(TAG_LINK_5, link);
        editor.apply();
    }

    public Boolean isChatRegistered() {
        return sharedPreferences.getBoolean(TAG_IS_CHAT_REGISTERED, false);
    }

    public void setIsChatRegistered(Boolean isChatRegistered) {
        editor.putBoolean(TAG_IS_CHAT_REGISTERED, isChatRegistered);
        editor.apply();
    }

    public Boolean isChatLoadedFromServer() {
        return sharedPreferences.getBoolean(TAG_IS_CHAT_LOAD_SERVER, false);
    }

    public void setIsChatLoadedFromServer(Boolean isChatLoadedFromServer) {
        editor.putBoolean(TAG_IS_CHAT_LOAD_SERVER, isChatLoadedFromServer);
        editor.apply();
    }

    public String getLink5() {
        return sharedPreferences.getString(TAG_LINK_5, "");
    }

    public void setUserCheckDate(String userCheckDate) {
        editor.putString(TAG_USER_CHECK_DATE, userCheckDate);
        editor.apply();
    }

    public String getUserCheckDate() {
        return sharedPreferences.getString(TAG_USER_CHECK_DATE, "");
    }

    public void setUserValidCheckDate() {
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cDate);

        editor.putString(TAG_CHECK_USER_VALID, fDate);
        editor.apply();
    }

    public boolean getIsUserValidCheck() {
        Date cDate = new Date();
        String fDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cDate);

        return !sharedPreferences.getString(TAG_CHECK_USER_VALID, "").equals(fDate);
    }

    public void setIsChatOn(boolean isChatOn) {
        editor.putBoolean(TAG_IS_CHAT_ON, isChatOn);
        editor.apply();
    }

    public boolean getIsChatOn() {
        return sharedPreferences.getBoolean(TAG_IS_CHAT_ON, false);
    }

    public void setIsVoiceChatOn(boolean isVoiceChatOn) {
        editor.putBoolean(TAG_IS_VOICE_CHAT_ON, isVoiceChatOn);
        editor.apply();
    }

    public boolean getIsVoiceChatOn() {
        return sharedPreferences.getBoolean(TAG_IS_VOICE_CHAT_ON, false);
    }

    public void setIsAccountVerifyOn(boolean isAccountVerifyOn) {
        editor.putBoolean(TAG_USER_ACCOUNT_VERIFY_ON, isAccountVerifyOn);
        editor.apply();
    }

    public boolean getIsAccountVerifyOn() {
        return sharedPreferences.getBoolean(TAG_USER_ACCOUNT_VERIFY_ON, false);
    }

    public void setIsPointsOn(boolean isPointsOn) {
        editor.putBoolean(TAG_POINTS_ON, isPointsOn);
        editor.apply();
    }

    public boolean getIsPointsOn() {
        return sharedPreferences.getBoolean(TAG_POINTS_ON, false);
    }

    public void setCurrencyCode(String currencyCode) {
        editor.putString(TAG_CURRENCY_CODE, currencyCode);
        editor.apply();
    }

    public String getCurrencyCode() {
        return sharedPreferences.getString(TAG_CURRENCY_CODE, "INR");
    }

    public void setMinWithdrawPoints(int minWithdrawPoints) {
        editor.putInt(TAG_MIN_POINTS_WITHDRAW, minWithdrawPoints);
        editor.apply();
    }

    public String getUploadPostType() {
        return sharedPreferences.getString(TAG_UPLOAD_POST_TYPE, "Both");
    }

    public void setUploadPostType(String uploadPostType) {
        editor.putString(TAG_UPLOAD_POST_TYPE, uploadPostType);
        editor.apply();
    }

    public int getMinWithdrawPoints() {
        return sharedPreferences.getInt(TAG_MIN_POINTS_WITHDRAW, 100);
    }

    public Boolean getIsVoicePermissionAsked() {
        return sharedPreferences.getBoolean(TAG_IS_VOICE_PERMISSION_ASKED, false);
    }

    public void setIsVoicePermissionAsked(Boolean isVoicePer) {
        editor.putBoolean(TAG_IS_VOICE_PERMISSION_ASKED, isVoicePer);
        editor.apply();
    }

    public void setAdDetails(boolean isBanner, boolean isInter, boolean isNative, String typeBanner, String typeInter, String typeNative,
                             String idBanner, String idInter, String idNative, String startapp_id, int interPos, int nativePos) {
        editor.putBoolean(TAG_AD_IS_BANNER, isBanner);
        editor.putBoolean(TAG_AD_IS_INTER, isInter);
        editor.putBoolean(TAG_AD_IS_NATIVE, isNative);
        editor.putString(TAG_AD_TYPE_BANNER, encryptData.encrypt(typeBanner));
        editor.putString(TAG_AD_TYPE_INTER, encryptData.encrypt(typeInter));
        editor.putString(TAG_AD_TYPE_NATIVE, encryptData.encrypt(typeNative));
        editor.putString(TAG_AD_ID_BANNER, encryptData.encrypt(idBanner));
        editor.putString(TAG_AD_ID_INTER, encryptData.encrypt(idInter));
        editor.putString(TAG_AD_ID_NATIVE, encryptData.encrypt(idNative));
        editor.putString(TAG_STARTAPP_ID, encryptData.encrypt(startapp_id));
        editor.putInt(TAG_AD_NATIVE_POS, interPos);
        editor.putInt(TAG_AD_INTER_POS, nativePos);
        editor.apply();
    }

    public void getAdDetails() {
        Constants.bannerAdType = encryptData.decrypt(sharedPreferences.getString(TAG_AD_TYPE_BANNER, Constants.AD_TYPE_ADMOB));
        Constants.interstitialAdType = encryptData.decrypt(sharedPreferences.getString(TAG_AD_TYPE_INTER, Constants.AD_TYPE_ADMOB));
        Constants.nativeAdType = encryptData.decrypt(sharedPreferences.getString(TAG_AD_TYPE_NATIVE, Constants.AD_TYPE_ADMOB));

        Constants.bannerAdID = encryptData.decrypt(sharedPreferences.getString(TAG_AD_ID_BANNER, ""));
        Constants.interstitialAdID = encryptData.decrypt(sharedPreferences.getString(TAG_AD_ID_INTER, ""));
        Constants.nativeAdID = encryptData.decrypt(sharedPreferences.getString(TAG_AD_ID_NATIVE, ""));

        Constants.startappAppId = encryptData.decrypt(sharedPreferences.getString(TAG_STARTAPP_ID, ""));

        Constants.interstitialAdShow = sharedPreferences.getInt(TAG_AD_INTER_POS, 5);
        Constants.nativeAdShow = sharedPreferences.getInt(TAG_AD_NATIVE_POS, 9);
    }

    public String getFB() {
        return sharedPreferences.getString(TAG_FB,"");
    }

    public String getTwitter() {
        return sharedPreferences.getString(TAG_TWITTER,"");
    }

    public String getInsta() {
        return sharedPreferences.getString(TAG_INSTA,"");
    }

    public String getYoutube() {
        return sharedPreferences.getString(TAG_YOUTUBE,"");
    }
}