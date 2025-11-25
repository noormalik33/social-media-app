package com.example.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemUserRequests implements Serializable {

    @SerializedName("request_id")
    String requestID;

    @SerializedName("user_id")
    String userID;

    @SerializedName("user_name")
    String userName;

    @SerializedName("user_image")
    String image;

    public String getRequestID() {
        return requestID;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getImage() {
        return image;
    }
}