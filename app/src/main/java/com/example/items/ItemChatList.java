package com.example.items;

import java.io.Serializable;

public class ItemChatList implements Serializable{

	String id;
	String name;
	String image;
	boolean isBlocked, isMessageSynced, isUserDeleted, isUserAccountVerified;

	public ItemChatList(String id, String name, String image, boolean isBlocked, boolean isMessageSynced, boolean isUserDeleted, boolean isUserAccountVerified) {
		this.id = id;
		this.name = name;
		this.image = image;
		this.isBlocked = isBlocked;
		this.isMessageSynced = isMessageSynced;
		this.isUserDeleted = isUserDeleted;
		this.isUserAccountVerified = isUserAccountVerified;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public boolean isBlocked() {
		return isBlocked;
	}

	public boolean isMessageSynced() {
		return isMessageSynced;
	}

	public boolean isUserDeleted() {
		return isUserDeleted;
	}

	public void setUserDeleted(boolean userDeleted) {
		isUserDeleted = userDeleted;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public boolean getIsUserAccountVerified() {
		return isUserAccountVerified;
	}
}