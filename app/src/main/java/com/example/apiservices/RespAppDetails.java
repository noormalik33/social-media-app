package com.example.apiservices;

import com.example.items.ItemAbout;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RespAppDetails implements Serializable {

    @SerializedName("VIDEO_STATUS_APP")
    ItemAbout itemAbout;

    public ItemAbout getItemAbout() {
        return itemAbout;
    }
}