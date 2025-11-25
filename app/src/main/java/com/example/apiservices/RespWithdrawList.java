package com.example.apiservices;

import com.example.items.ItemUser;
import com.example.items.ItemWithdraw;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class RespWithdrawList implements Serializable {


    @SerializedName("VIDEO_STATUS_APP")
    ArrayList<ItemWithdraw> arrayListWithdraw;
    @SerializedName("success")
    String success;

    public ArrayList<ItemWithdraw> getArrayListWithdraw() {
        return arrayListWithdraw;
    }

    public String getSuccess() {
        return success;
    }
}