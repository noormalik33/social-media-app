package blogtalk.com.interfaces;

import blogtalk.com.apiservices.RespUserList;
import blogtalk.com.items.ItemComments;
import blogtalk.com.items.ItemUser;

import java.util.ArrayList;

public interface FunctionListener {
    void getUserDetails(String success, ItemUser itemUsers);
}
