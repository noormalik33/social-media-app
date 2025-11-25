package com.example.items;

import com.google.gson.annotations.SerializedName;

public class ItemSuccess {

    @SerializedName("success")
    String success;

    @SerializedName("msg")
    String message;

    @SerializedName("total_rate")
    int totalRate;

    @SerializedName("total_likes")
    int totalLikes;

    @SerializedName("views")
    String totalViews;

    @SerializedName("download")
    String totalDownloads;

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getTotalRate() {
        return totalRate;
    }

    public String getTotalDownloads() {
        return totalDownloads;
    }

    public int getTotalLikes() {
        return totalLikes;
    }

    public String getTotalViews() {
        return totalViews;
    }
}