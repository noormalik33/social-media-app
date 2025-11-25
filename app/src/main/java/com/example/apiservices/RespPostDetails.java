package blogtalk.com.apiservices;

import blogtalk.com.items.ItemPost;
import com.google.gson.annotations.SerializedName;

public class RespPostDetails {

    @SerializedName("VIDEO_STATUS_APP")
    ItemPost itemPost;

    @SerializedName("status_code")
    String statusCode;

    public ItemPost getItemPost() {
        return itemPost;
    }

    public String getStatusCode() {
        return statusCode;
    }
}