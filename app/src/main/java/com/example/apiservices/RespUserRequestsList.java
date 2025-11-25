package blogtalk.com.apiservices;

import blogtalk.com.items.ItemUser;
import blogtalk.com.items.ItemUserRequests;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class RespUserRequestsList implements Serializable {


    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemUserRequests> arrayListUserRequests;

    @SerializedName("msg")
    String message;

    @SerializedName("success")
    String success;

    public ArrayList<ItemUserRequests> getArrayListUserRequests() {
        return arrayListUserRequests;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }
}