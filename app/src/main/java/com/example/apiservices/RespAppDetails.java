package blogtalk.com.apiservices;

import blogtalk.com.items.ItemAbout;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RespAppDetails implements Serializable {

    @SerializedName("VIDEO_STATUS_APP")
    ItemAbout itemAbout;

    public ItemAbout getItemAbout() {
        return itemAbout;
    }
}