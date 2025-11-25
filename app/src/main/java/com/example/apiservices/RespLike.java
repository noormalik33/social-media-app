package blogtalk.com.apiservices;

import blogtalk.com.items.ItemSuccess;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RespLike {

    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemSuccess> arrayListSuccess;

    public ArrayList<ItemSuccess> getArrayListSuccess() {
        return arrayListSuccess;
    }

    public void setArrayListSuccess(ArrayList<ItemSuccess> arrayListSuccess) {
        this.arrayListSuccess = arrayListSuccess;
    }
}