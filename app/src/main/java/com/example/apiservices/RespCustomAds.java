package blogtalk.com.apiservices;

import blogtalk.com.items.ItemCustomAds;
import blogtalk.com.items.ItemPost;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class RespCustomAds implements Serializable {
    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemCustomAds> arrayListCustomAds;

    @SerializedName("success")
    String success;

    public ArrayList<ItemCustomAds> getArrayListCustomAds() {
        return arrayListCustomAds;
    }

    public String getSuccess() {
        return success;
    }
}