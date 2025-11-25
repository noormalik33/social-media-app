package blogtalk.com.apiservices;

import blogtalk.com.items.ItemComments;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RespPostComment {

    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemComments> arrayListPostComment;

    @SerializedName("success")
    String success;

    public String getSuccess() {
        return success;
    }

    public ArrayList<ItemComments> getCommentDetail() {
        return arrayListPostComment;
    }
}