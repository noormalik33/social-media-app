package com.example.items;

import java.io.Serializable;

public class ItemNotification implements Serializable{

	String title;
	String message;
	String image;
	String url;
	String notificationType;
	String userID;
	String userName;
	String userImage;
	String postID;
	String postImage;

	public ItemNotification(String title, String message, String image, String url, String notificationType, String userID, String userName, String userImage, String postID, String postImage) {
		this.title = title;
		this.message = message;
		this.image = image;
		this.url = url;
		this.notificationType = notificationType;
		this.userID = userID;
		this.userName = userName;
		this.userImage = userImage;
		this.postID = postID;
		this.postImage = postImage;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getImage() {
		return image;
	}

	public String getUrl() {
		return url;
	}

	public String getNotificationType() {
		return notificationType;
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

	public String getPostID() {
		return postID;
	}

	public String getPostImage() {
		return postImage;
	}
}
