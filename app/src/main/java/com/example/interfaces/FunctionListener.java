package com.example.interfaces;

import com.example.apiservices.RespUserList;
import com.example.items.ItemComments;
import com.example.items.ItemUser;

import java.util.ArrayList;

public interface FunctionListener {
    void getUserDetails(String success, ItemUser itemUsers);
}
