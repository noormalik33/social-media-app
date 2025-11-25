package com.example.apiservices;

import com.example.items.ItemLanguage;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class RespLanguageList implements Serializable {

    @SerializedName("status")
    String status;
    @SerializedName("message")
    String message;
    @SerializedName("ANDROID_REWARDS_APP")
    ArrayList<ItemLanguage> arrayListAbout;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public ArrayList<ItemLanguage> getArrayListLanguage() {
        return arrayListAbout;
    }
}