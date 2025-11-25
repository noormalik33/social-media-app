package blogtalk.com.utils;

import android.graphics.Bitmap;
import android.net.Uri;

import blogtalk.com.items.ItemAbout;
import blogtalk.com.items.ItemCustomAds;
import blogtalk.com.items.ItemLinks;
import blogtalk.com.items.ItemPage;
import blogtalk.com.items.ItemPost;
import blogtalk.com.items.ItemStories;
import blogtalk.com.socialmedia.BuildConfig;

import java.io.Serializable;
import java.util.ArrayList;

public class Constants implements Serializable {

    private static final long serialVersionUID = 1L;

    //server url
    public static final String SERVER_URL = BuildConfig.SERVER_URL;

    public static final String URL_AGORA_SIGNUP = "username_check";
    public static final String URL_APP_DETAILS = "app_details";
    public static final String URL_LOGIN = "login";
    public static final String URL_SOCIAL_LOGIN = "social_login";
    public static final String URL_REGISTRATION = "signup";
    public static final String URL_SEND_VERIFY_OTP = "email_code_send";
    public static final String URL_VERIFY_OTP = "email_verify";
    public static final String URL_FORGOT_PASSWORD = "forgot_password";
    public static final String URL_PROFILE = "profile";
    public static final String URL_PUBLIC_PROFILE = "user_profile";
    public static final String URL_PROFILE_UPDATE = "profile_update";
    public static final String URL_CHANGE_PASSWORD = "password_change";
    public static final String URL_FOLLOW_UNFOLLOW = "post_following";
    public static final String URL_USER_FOLLOWING = "user_followers_list";
    public static final String URL_USER_FOLLOWED_BY_OTHERS = "user_followed_list";
    public static final String URL_USER_REQUESTED = "following_request_list";
    public static final String URL_USER_BY_POST_LIKE = "post_like_user_list";
    public static final String URL_USER_REQUESTED_ACCEPT = "following_request_accept";

    public static final String URL_USER_REQUESTED_DECLINE = "following_request_reject";
    public static final String URL_USER_LINKS_UPDATE = "user_links_update";
    public static final String URL_USER_PRIVACY_UPDATE = "user_privacy_update";
    public static final String URL_USER_VALID_INVALID = "user_valid_or_not";
    public static final String URL_CHECK_USERNAME = "username_check";

    public static final String URL_HOME = "home";
    public static final String URL_LATEST = "latest";
    public static final String URL_RELATED_POST = "related_post_list";
    public static final String URL_USER_POST = "user_post_list";
    public static final String URL_USER_FAV_POST = "user_favourite_post_list";
    public static final String URL_POST_DETAILS = "post_details";
    public static final String URL_SEARCH = "search";
    public static final String URL_SEARCH_USERS = "search_user";
    public static final String URL_SEARCH_TAG = "tags_search";
    public static final String URL_VIEW_POST = "post_view";
    public static final String URL_DO_FAV = "post_favourite";
    public static final String URL_DO_LIKE = "post_like";
    public static final String URL_POST_COMMENTS = "post_comment";
    public static final String URL_REPORT = "user_reports";
    public static final String URL_ADD_POST = "add_post";
    public static final String URL_EDIT_POST = "edit_post";
    public static final String URL_DELETE_POST = "delete_user_post";
    public static final String URL_DELETE_COMMENT = "delete_user_comment";
    public static final String URL_TOTAL_POST = "user_total_post";
    public static final String URL_DELETE_ACCOUNT = "delete_account";
    public static final String URL_CONTACT_US = "user_contact";
    public static final String URL_GENERATE_CHAT_TOKEN = "chat_token_get";
    public static final String URL_GENERATE_RTC_TOKEN = "rtc_token_get";
    public static final String URL_GENERATE_RTM_TOKEN = "rtm_token_get";
    public static final String URL_CUSTOM_ADS = "custom_ads";
    public static final String URL_STORY_LIST = "user_story_list";
    public static final String URL_STORY_UPLOAD = "add_story";
    public static final String URL_STORY_VIEW = "story_view_add";
    public static final String URL_STORY_USER_VIEW_LIST = "story_view_user_list";
    public static final String URL_STORY_DELETE = "delete_story";
    public static final String URL_ACCOUNT_VERIFY_REQUEST = "verification_request";
    public static final String URL_WITHDRAW_REQUEST = "user_withdrawal_request";
    public static final String URL_WITHDRAW_HISTORY = "user_withdrawal_history";
    public static final String URL_UPDATE_PAYMENT_INFO = "payment_info_update";

    public static final String LOGIN_TYPE_NORMAL = "normal";
    public static final String LOGIN_TYPE_GOOGLE = "google";

    public static final String DARK_MODE_ON = "on";
    public static final String DARK_MODE_OFF = "off";
    public static final String DARK_MODE_SYSTEM = "system";

    public static final String AD_TYPE_ADMOB = "Admob";
    public static final String AD_TYPE_FACEBOOK = "Facebook";
    public static final String AD_TYPE_STARTAPP = "StartApp";
    public static final String AD_TYPE_APPLOVIN = "AppLovins MAX";
    public static final String AD_TYPE_WORTISE = "Wortise";
    public static final String TAG_PROFILE_PUBLIC = "public";
    public static final String TAG_PROFILE_PRIVATE = "private";
    public static final String TAG_NOTI_TYPE_REQUEST = "request";
    public static final String TAG_NOTI_TYPE_ACCEPT = "accept";
    public static final String TAG_NOTI_TYPE_LIKE = "like_comment";
    public static final String TAG_FROM_HOME = "home";
    public static final String TAG_FROM_SEARCH = "search";
    public static final String TAG_FROM_TAG = "tag";
    public static final String TAG_FROM_OTHER = "other";

    public static ArrayList<ItemPage> arrayListPages = new ArrayList<>();
    public static ItemAbout itemAbout;
    public static ArrayList<ItemPost> arrayListPosts = new ArrayList<>();
    public static ArrayList<Uri> arrayListDownloads = new ArrayList<>();
    public static ArrayList<ItemLinks> arrayListLinks = new ArrayList<>();
    public static ArrayList<ItemCustomAds> arrayListCustomAds = new ArrayList<>();
    public static ArrayList<ItemStories> arrayListStories = new ArrayList<>();
    public static ArrayList<Uri> arrayListSelectedImagesUri = new ArrayList<>();
    public static ArrayList<String> arrayListSelectedImagesPath = new ArrayList<>();

    public static Boolean isProfileUpdate = false, isBannerAd = true, isInterAd = true, isNativeAd = false
            , showUpdateDialog = false, appUpdateCancel = true, isUsernameChanged = false, isLinkChanged = false, isPointsUpdated = false;
    public static String tempUsername = "", verifiedDocName = "";
    public static int userNameMaxChar = 20, minPost = 10, minFollowers = 15, onePoint = 7, oneMoney = 3;
    public static String bannerAdType = "admob", interstitialAdType = "admob", nativeAdType = "admob",
            publisherAdID = "", bannerAdID = "", interstitialAdID = "", nativeAdID = "", startappAppId = "",
            appVersion = "1", appUpdateMsg = "", appUpdateURL = "", wortiseAppId = "",
            urlYoutube = "", urlFacebook = "", urlInstagram = "", urlTwitter = "", pushType="", pushPostID="";

    public static int videoUploadDuration = 120, videoUploadSize = 50, adCount = 0, interstitialAdShow = 5, nativeAdShow = 12, screenWidth=50, photoHeight=100, customAdHomePos = 5, customAdSearchPos = 6, customAdTagPos = 5, customAdOthersPos = 5, galleryDetailPos = 0;

    public static Boolean isEditComment = true, isEmailVerificationChanged = false, isUserFollowingChanged = false, isUserPostDeleted = false,
            isNewPostAdded = false, isNewStoryAdded = false, isChatConversationDeleted = false, isCustomAdsHome = false, isCustomAdsSearch = false,
            isCustomAdsTags = false, isCustomAdsOther = false, isFromStories = false;
    public static Bitmap selectedImage;
    public static String CALL_CALLING = "calling";
    public static String CALL_RINGING = "ringing";
    public static String CALL_REJECTED = "rejected";

    public static final int MIN_BUFFER_DURATION = 2000;
    //Max Video you want to buffer during PlayBack
    public static final int MAX_BUFFER_DURATION = 5000;
    //Min Video you want to buffer before start Playing it
    public static final int MIN_PLAYBACK_START_BUFFER = 1500;
    //Min video You want to buffer when user resumes video
    public static final int MIN_PLAYBACK_RESUME_BUFFER = 2000;

    // milliseconds for story view time
    public static final long STORY_TIME = 4000L;

    public static final String AGORA_APP_ID = "72377928e9f94f058e4dc6e9dc754a1a";
}