package blogtalk.com.apiservices;

import blogtalk.com.items.ItemSuccess;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RespView {

    @SerializedName("VIDEO_STATUS_APP")
    ItemSuccess itemSuccess;
    @SerializedName("success")
    String success;

    public ItemSuccess getItemSuccess() {
        return itemSuccess;
    }

    public String getSuccess() {
        return success;
    }
}