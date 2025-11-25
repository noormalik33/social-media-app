package blogtalk.com.interfaces;

import blogtalk.com.items.ItemComments;
import blogtalk.com.items.ItemLanguage;

import java.util.ArrayList;

public interface CommentListListener {
    void onDataReceived(String success, ArrayList<ItemComments> arrayListComments);
}
