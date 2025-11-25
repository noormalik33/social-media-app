package com.example.apiservices;

import com.example.items.ItemPost;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RespTotalPost {

    @SerializedName("VIDEO_STATUS_APP")
    ItemTotalPost itemTotalPost;

    public ItemTotalPost getItemTotalPost() {
        return itemTotalPost;
    }

    public static class ItemTotalPost implements Serializable {

        @SerializedName("total_video_post")
        String videoPost="";

        @SerializedName("total_image_post")
        String imagePost="";

        public String getVideoPost() {
            return videoPost;
        }

        public String getImagePost() {
            return imagePost;
        }
    }
}