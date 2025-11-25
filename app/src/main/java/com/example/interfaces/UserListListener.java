package blogtalk.com.interfaces;

import blogtalk.com.items.ItemComments;
import blogtalk.com.items.ItemUser;

import java.util.ArrayList;

public interface UserListListener {
    void onDataReceived(String success, ArrayList<ItemUser> arrayListUser, int totalRecords);
}
