package blogtalk.com.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemUser implements Serializable {

    @SerializedName("user_id")
    String id;

    @SerializedName(value = "name", alternate = {"user_name"})
    String name;

    @SerializedName("username")
    String username;

    @SerializedName("email")
    String email;

    @SerializedName("phone")
    String mobile;

    @SerializedName("user_image")
    String image;

    @SerializedName("email_verify")
    String isEmailVerified;

    @SerializedName("account_verified")
    String isAccountVerified;

    @SerializedName("date_of_birth")
    String dateOfBirth;

    @SerializedName("gender")
    String gender;

    @SerializedName("address")
    String address;

    @SerializedName("user_bio")
    String userBio;

    @SerializedName("user_lat")
    String latitude;

    @SerializedName("user_long")
    String longitude;

    @SerializedName("total_following")
    int totalFollowing;

    @SerializedName("total_followers")
    int totalFollowers;

    @SerializedName("total_posts")
    int totalPost;

    @SerializedName("user_follow_or_not")
    boolean isUserFollowed;

    @SerializedName("user_request_or_not")
    boolean isUserRequested;

    @SerializedName("profile_completed")
    int profileCompleted;

    @SerializedName("share_url")
    String shareUrl;

    @SerializedName("user_privacy")
    String userPrivacy;

    @SerializedName("link1_title")
    String link1Title;

    @SerializedName("link1")
    String link1;

    @SerializedName("link2_title")
    String link2Title;

    @SerializedName("link2")
    String link2;

    @SerializedName("link3_title")
    String link3Title;

    @SerializedName("link3")
    String link3;

    @SerializedName("link4_title")
    String link4Title;

    @SerializedName("link4")
    String link4;

    @SerializedName("link5_title")
    String link5Title;

    @SerializedName("link5")
    String link5;

    @SerializedName("total_points")
    int totalPoints;

    @SerializedName("total_earnings")
    String totalEarnings;

    @SerializedName("payment_info")
    String paymentInfo;

    @SerializedName("verification_request")
    int accountVerificationRequested;

    @SerializedName("user_status")
    int status;

    String authID;

    String loginType;

    public ItemUser(String id, String name, String email, String mobile, String authID, String loginType, String isEmailVerified) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.authID = authID;
        this.loginType = loginType;
        this.isEmailVerified = isEmailVerified;
    }

    public ItemUser(String id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getAddress() {
        return address;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getAuthID() {
        return authID;
    }

    public String getImage() {
        return image;
    }

    public String getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(String isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public boolean getIsAccountVerified() {
        return isAccountVerified != null && isAccountVerified.equalsIgnoreCase("yes");
    }

    public int getTotalFollowing() {
        return totalFollowing;
    }

    public int getTotalFollowers() {
        return totalFollowers;
    }

    public int getTotalPost() {
        return totalPost;
    }

    public boolean isUserFollowed() {
        return isUserFollowed;
    }

    public void setUserFollowed(boolean userFollowed) {
        isUserFollowed = userFollowed;
    }

    public boolean isUserRequested() {
        return isUserRequested;
    }

    public int getProfileCompleted() {
        return profileCompleted;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public String getUserBio() {
        return userBio;
    }

    public String getUserPrivacy() {
        return userPrivacy;
    }

    public String getUsername() {
        return username;
    }

    public String getLink1Title() {
        return link1Title;
    }

    public String getLink1() {
        return link1;
    }

    public String getLink2Title() {
        return link2Title;
    }

    public String getLink2() {
        return link2;
    }

    public String getLink3Title() {
        return link3Title;
    }

    public String getLink3() {
        return link3;
    }

    public String getLink4Title() {
        return link4Title;
    }

    public String getLink4() {
        return link4;
    }

    public String getLink5Title() {
        return link5Title;
    }

    public String getLink5() {
        return link5;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getTotalEarnings() {
        return totalEarnings;
    }

    public String getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(String paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public boolean getIsAccountVerificationRequested() {
        return accountVerificationRequested==1;
    }

    public int getStatus() {
        return status;
    }
}