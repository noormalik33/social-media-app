package com.example.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class ItemPost implements Serializable {

	public long id;

	@SerializedName("post_id")
	String postID;

	@SerializedName("post_title")
	String captions;

	@SerializedName("post_type")
	String postType;

	@SerializedName("post_image")
	String postImage;

	@SerializedName("video_file")
	String videoUrl;

	@SerializedName("post_date")
	String date;

	@SerializedName("post_tags")
	String tags;

	@SerializedName("total_views")
	String totalViews;

	@SerializedName("total_likes")
	String totalLikes;

	@SerializedName("total_comments")
	String totalComments;

	@SerializedName("favourite")
	boolean isFavourite;

	@SerializedName("check_like")
	boolean isLiked;

	@SerializedName("user_id")
	String userId;

	@SerializedName("user_name")
	String userName;

	@SerializedName("user_image")
	String userImage;

	@SerializedName("account_verified")
	String isUserAccVerified;

	@SerializedName("user_follow_or_not")
	boolean isUserFollowed;

	@SerializedName("user_request_or_not")
	boolean isUserRequested;

	@SerializedName("share_url")
	String shareUrl;

	@SerializedName("image_gallery")
	ArrayList<ItemImageGallery> arrayListImageGallery;

	@SerializedName("comment_list")
	ArrayList<ItemComments> arrayListComments;

	boolean isViewed;

	public ItemPost(String postID, String captions, String postImage, String videoUrl, String date, String totalLikes, boolean isFavourite, boolean isLiked, String userId, String userName, String userImage) {
		this.postID = postID;
		this.captions = captions;
		this.postImage = postImage;
		this.videoUrl = videoUrl;
		this.date = date;
		this.totalLikes = totalLikes;
		this.isFavourite = isFavourite;
		this.isLiked = isLiked;
		this.userId = userId;
		this.userName = userName;
		this.userImage = userImage;
	}

	public String getPostID() {
		return postID;
	}

	public String getCaptions() {
		return captions.replace("&nbsp;", " ");
	}

	public String getPostType() {
		return postType;
	}

	public String getPostImage() {
		return postImage;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public String getDate() {
		return date;
	}

	public String getTags() {
		return tags;
	}

	public String getTotalViews() {
		return totalViews;
	}

	public String getTotalLikes() {
		return totalLikes;
	}

	public String getTotalComments() {
		return totalComments;
	}

	public boolean isFavourite() {
		return isFavourite;
	}

	public void setFavourite(boolean favourite) {
		isFavourite = favourite;
	}

	public boolean isLiked() {
		return isLiked;
	}

	public void setLiked(boolean liked) {
		isLiked = liked;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserImage() {
		return userImage;
	}

	public boolean isViewed() {
		return isViewed;
	}

	public void setViewed(boolean viewed) {
		isViewed = viewed;
	}

	public void setTotalViews(String totalViews) {
		this.totalViews = totalViews;
	}

	public void setTotalLikes(String totalLikes) {
		this.totalLikes = totalLikes;
	}

	public void setTotalComments(String totalComments) {
		this.totalComments = totalComments;
	}

	public boolean isUserFollowed() {
		return isUserFollowed;
	}

	public void setUserFollowed(boolean userFollowed) {
		isUserFollowed = userFollowed;
	}

	public boolean isUserRequested() {
		return isUserRequested;
	}

	public void setUserRequested(boolean userRequested) {
		isUserRequested = userRequested;
	}

	public ArrayList<ItemComments> getArrayListComments() {
		return arrayListComments;
	}

	public void setCaptions(String captions) {
		this.captions = captions;
	}

	public void setPostImage(String postImage) {
		this.postImage = postImage;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public boolean getIsUserAccVerified() {
		return isUserAccVerified != null && isUserAccVerified.equalsIgnoreCase("yes");
	}

	public ArrayList<ItemImageGallery> getArrayListImageGallery() {
		return arrayListImageGallery;
	}
}