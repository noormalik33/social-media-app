package blogtalk.com.apiservices;

import com.google.gson.annotations.SerializedName;

public class RespRegister {

    @SerializedName("success")
    String success;

    @SerializedName("VIDEO_STATUS_APP")
    UserDetail userDetail;

    @SerializedName("msg")
    String message;

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static class UserDetail {
        @SerializedName("user_id")
        String id;
        @SerializedName("verify_email_on_off")
        String isVerifyEmail;

        public String getUserId() {
            return id;
        }

        public String isVerifyEmail() {
            return isVerifyEmail;
        }
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }
}