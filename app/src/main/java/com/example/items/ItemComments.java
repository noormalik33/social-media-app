package com.example.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemComments implements Serializable {

    @SerializedName("comment_id")
    private String id;

    @SerializedName("comment_text")
    private String commentText;

    @SerializedName("comment_date")
    private String date;

    @SerializedName("user_id")
    private String userID;

    @SerializedName("user_name")
    private String userName;

    @SerializedName("user_image")
    private String userImage;

    @SerializedName("total_comments")
    String totalComments;

    @SerializedName("msg")
    String message;

    @SerializedName("account_verified")
    String isAccountVerified;

    public ItemComments(String id, String commentText, String date, String userID, String userName, String userImage) {
        this.id = id;
        this.commentText = commentText;
        this.date = date;
        this.userID = userID;
        this.userName = userName;
        this.userImage = userImage;
    }

    public String getId() {
        return id;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getDate() {
        return date;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getTotalComments() {
        return totalComments;
    }

    public String getMessage() {
        return message;
    }

    public boolean getIsUserAccVerified() {
        return isAccountVerified != null && isAccountVerified.equalsIgnoreCase("yes");
    }
}
