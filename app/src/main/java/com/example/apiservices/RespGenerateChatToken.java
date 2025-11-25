package blogtalk.com.apiservices;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RespGenerateChatToken {

    @SerializedName("VIDEO_STATUS_APP")
    ChatToken chatToken;

    public ChatToken getItemChatToken() {
        return chatToken;
    }

    public static class ChatToken implements Serializable {

        @SerializedName("user_token")
        String userToken="";

        @SerializedName("app_token")
        String authToken="";

        public String getUserToken() {
            return userToken;
        }

        public String getAuthToken() {
            return authToken;
        }
    }
}