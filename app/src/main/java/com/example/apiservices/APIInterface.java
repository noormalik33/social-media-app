package blogtalk.com.apiservices;

import blogtalk.com.utils.Constants;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface APIInterface {

    @FormUrlEncoded
    @POST(Constants.URL_APP_DETAILS)
    Call<RespAppDetails> getAppDetails(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_LOGIN)
    Call<RespUserList> getLogin(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_SOCIAL_LOGIN)
    Call<RespUserList> getSocialLogin(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_REGISTRATION)
    Call<RespRegister> getRegistration(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_SEND_VERIFY_OTP)
    Call<RespSuccess> getSendVerifyOTP(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_VERIFY_OTP)
    Call<RespSuccess> getVerifyOTP(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_FORGOT_PASSWORD)
    Call<RespSuccess> getForgotPassword(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_PROFILE)
    Call<RespUserList> getProfile(@Field("data") String data);

    @POST(Constants.URL_PROFILE_UPDATE)
    Call<RespUserList> getProfileUpdate(@Body RequestBody imageFile);

    @FormUrlEncoded
    @POST(Constants.URL_USER_LINKS_UPDATE)
    Call<RespSuccess> getLinksUpdate(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_PRIVACY_UPDATE)
    Call<RespSuccess> getUserPrivacyUpdate(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_CHANGE_PASSWORD)
    Call<RespSuccess> getChangePassword(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_CHECK_USERNAME)
    Call<RespSuccess> getCheckUsername(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_VALID_INVALID)
    Call<RespSuccess> getUserValidInvalid(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_FOLLOWING)
    Call<RespFollowUserList> getUserFollowing(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_FOLLOWED_BY_OTHERS)
    Call<RespFollowUserList> getUserFollowedByOthers(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_REQUESTED)
    Call<RespUserRequestsList> getUserRequestedList(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_BY_POST_LIKE)
    Call<RespFollowUserList> getUserListByPostLike(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_HOME)
    Call<RespHomeList> getHome(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_LATEST)
    Call<RespPostList> getLatest(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_RELATED_POST)
    Call<RespPostList> getRelatedPost(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_POST)
    Call<RespPostList> getUserPost(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_FAV_POST)
    Call<RespPostList> getUserFavPost(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_SEARCH)
    Call<RespPostList> getSearch(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_SEARCH_USERS)
    Call<RespFollowUserList> getSearchUsers(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_SEARCH_TAG)
    Call<RespPostList> getSearchTag(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_FOLLOW_UNFOLLOW)
    Call<RespSuccess> getFollowUnFollow(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_REQUESTED_ACCEPT)
    Call<RespSuccess> getUserRequestAccept(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_USER_REQUESTED_DECLINE)
    Call<RespSuccess> getUserRequestDecline(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_DO_FAV)
    Call<RespSuccess> getDoFavourite(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_DO_LIKE)
    Call<RespLike> getDoLike(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_VIEW_POST)
    Call<RespView> getDoView(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_POST_DETAILS)
    Call<RespPostDetails> getPostDetails(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_POST_COMMENTS)
    Call<RespPostComment> getDoPostComments(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_REPORT)
    Call<RespSuccess> getDoReport(@Field("data") String data);

    @POST(Constants.URL_ADD_POST)
    Call<RespSuccess> getDoUploadPost(@Body RequestBody requestBody);

    @POST(Constants.URL_EDIT_POST)
    Call<RespUpdatePost> getDoEditPost(@Body RequestBody requestBody);

    @FormUrlEncoded
    @POST(Constants.URL_TOTAL_POST)
    Call<RespTotalPost> getTotalPost(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_DELETE_POST)
    Call<RespSuccess> getDeletePost(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_DELETE_COMMENT)
    Call<RespSuccess> getDeleteComment(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_DELETE_ACCOUNT)
    Call<RespSuccess> getDeleteAccount(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_CONTACT_US)
    Call<RespSuccess> getContactUs(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_GENERATE_CHAT_TOKEN)
    Call<RespGenerateChatToken> getGenerateChatToken(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_GENERATE_RTM_TOKEN)
    Call<RespGenerateChatToken> getGenerateRtmToken(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_GENERATE_RTC_TOKEN)
    Call<RespGenerateChatToken> getGenerateRtcToken(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_CUSTOM_ADS)
    Call<RespCustomAds> getCustomAds(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_STORY_LIST)
    Call<RespStories> getStories(@Field("data") String data);

    @POST(Constants.URL_STORY_UPLOAD)
    Call<RespSuccess> getUploadStory(@Body RequestBody requestBody);

    @FormUrlEncoded
    @POST(Constants.URL_STORY_VIEW)
    Call<RespView> getStoryView(@Field("data") String data);

    @POST(Constants.URL_ACCOUNT_VERIFY_REQUEST)
    Call<RespSuccess> getAccountVerifyRequest(@Body RequestBody imageFile);

    @FormUrlEncoded
    @POST(Constants.URL_WITHDRAW_REQUEST)
    Call<RespSuccess> getWithdrawRequest(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_WITHDRAW_HISTORY)
    Call<RespWithdrawList> getWithdrawHistory(@Query("page") int page, @Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_UPDATE_PAYMENT_INFO)
    Call<RespSuccess> getUpdatePaymentInfo(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_STORY_USER_VIEW_LIST)
    Call<RespFollowUserList> getStoryUserViewList(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.URL_STORY_DELETE)
    Call<RespSuccess> getStoryDelete(@Field("data") String data);
}