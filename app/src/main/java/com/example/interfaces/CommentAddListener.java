package blogtalk.com.interfaces;

import blogtalk.com.items.ItemComments;

import java.util.ArrayList;

public interface CommentAddListener {
    void onDataReceived(String success, ItemComments itemComments);
}
