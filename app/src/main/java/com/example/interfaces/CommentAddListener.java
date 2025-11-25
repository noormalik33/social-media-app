package com.example.interfaces;

import com.example.items.ItemComments;

import java.util.ArrayList;

public interface CommentAddListener {
    void onDataReceived(String success, ItemComments itemComments);
}
