package com.example.apiservices;

import com.example.items.ItemStories;
import com.example.items.ItemSuccess;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RespStories {

    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemStories> arrayListStories;

    @SerializedName("user_own_story")
    ArrayList<ItemStories.ItemStoryPost> arrayListOwnStories;

    public ArrayList<ItemStories> getArrayListStories() {
        return arrayListStories;
    }

    public ArrayList<ItemStories.ItemStoryPost> getArrayListOwnStories() {
        return arrayListOwnStories;
    }
}