package blogtalk.com.apiservices;

import blogtalk.com.items.ItemUser;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class RespFollowUserList implements Serializable {


    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemUser> arrayListUser;

    @SerializedName("msg")
    String message;

    @SerializedName("success")
    String success;

    @SerializedName("total_records")
    int totalRecords;

    public ArrayList<ItemUser> getArrayListUser() {
        return arrayListUser;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }

    public int getTotalRecords() {
        return totalRecords;
    }
}