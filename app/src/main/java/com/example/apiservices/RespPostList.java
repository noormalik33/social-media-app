package blogtalk.com.apiservices;

import blogtalk.com.items.ItemPost;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class RespPostList implements Serializable {

    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemPost> arrayListPost;

    @SerializedName("total_records")
    int totalRecords;

    public ArrayList<ItemPost> getArrayListPost() {
        return arrayListPost;
    }

    public int getTotalRecords() {
        return totalRecords;
    }
}