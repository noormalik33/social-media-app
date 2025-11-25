package com.example.interfaces;

import com.example.items.ItemComments;
import com.example.items.ItemLanguage;

import java.util.ArrayList;

public interface CommentListListener {
    void onDataReceived(String success, ArrayList<ItemComments> arrayListComments);
}
