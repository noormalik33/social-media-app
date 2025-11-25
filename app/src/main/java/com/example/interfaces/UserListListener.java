package com.example.interfaces;

import com.example.items.ItemComments;
import com.example.items.ItemUser;

import java.util.ArrayList;

public interface UserListListener {
    void onDataReceived(String success, ArrayList<ItemUser> arrayListUser, int totalRecords);
}
