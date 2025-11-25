package com.example.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class ItemStories implements Serializable {

	public long id;
	@SerializedName("user_id")
	String userID;
	@SerializedName("user_name")
	String userName;
	@SerializedName("user_image")
	String userImage;

	@SerializedName("account_verified")
	String isUserAccVerified;
	@SerializedName("user_story")
	ArrayList<ItemStoryPost> arrayListStoryPost;
	int currentStoryPos = 0;

	public ItemStories(String postID, String userID, String userName, String userImage, ArrayList<ItemStoryPost> arrayListStoryPost) {
		this.userID = userID;
		this.userName = userName;
		this.userImage = userImage;
		this.arrayListStoryPost = arrayListStoryPost;
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

	public ArrayList<ItemStoryPost> getArrayListStoryPost() {
		return arrayListStoryPost;
	}

	public int getCurrentStoryPos() {
		return currentStoryPos;
	}

	public void setCurrentStoryPos(int currentStoryPos) {
		this.currentStoryPos = currentStoryPos;
	}

	public boolean getIsUserAccVerified() {
		return isUserAccVerified != null && isUserAccVerified.equalsIgnoreCase("yes");
	}

	public static class ItemStoryPost implements Serializable {

		public long id;

		@SerializedName("story_id")
		String storyID;

		@SerializedName("story_type")
		String postType;
		@SerializedName("story_date")
		String date;
		@SerializedName("story_media")
		String postImage;
		@SerializedName("story_caption")
		String caption;
		@SerializedName("story_views")
		String totalViews;

		public ItemStoryPost(String storyID, String postImage, String date) {
			this.storyID = storyID;
			this.postImage = postImage;
			this.date = date;
		}

		public long getId() {
			return id;
		}

		public String getStoryID() {
			return storyID;
		}

		public String getPostImage() {
			return postImage;
		}

		public String getDate() {
			return date;
		}

		public String getPostType() {
			return postType;
		}

		public String getCaption() {
			return caption;
		}

		public String getTotalViews() {
			return totalViews != null ? totalViews : "0";
		}

		public void setTotalViews(String totalViews) {
			this.totalViews = totalViews;
		}
	}
}