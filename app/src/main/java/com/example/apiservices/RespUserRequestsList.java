package com.example.apiservices;

import com.example.items.ItemUser;
import com.example.items.ItemUserRequests;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class RespUserRequestsList implements Serializable {


    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemUserRequests> arrayListUserRequests;

    @SerializedName("msg")
    String message;

    @SerializedName("success")
    String success;

    public ArrayList<ItemUserRequests> getArrayListUserRequests() {
        return arrayListUserRequests;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }
}