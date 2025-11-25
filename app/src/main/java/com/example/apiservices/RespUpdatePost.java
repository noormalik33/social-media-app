package blogtalk.com.apiservices;

import blogtalk.com.items.ItemPost;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RespUpdatePost {

    @SerializedName("VIDEO_STATUS_APP")
    ItemUpdatePost itemUpdatePost;
    @SerializedName("success")
    String success;

    @SerializedName("msg")
    String message;

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static class ItemUpdatePost {
        @SerializedName("post_image_url")
        String imageUrl = "";

        public String getImageUrl() {
            return imageUrl;
        }
    }

    public ItemUpdatePost getItemUpdatePost() {
        return itemUpdatePost;
    }
}