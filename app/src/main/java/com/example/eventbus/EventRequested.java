package com.example.eventbus;

import java.io.Serializable;

public class EventRequested implements Serializable{

	String userID, type;
	boolean isRequested = false;

	public EventRequested(String userID, boolean isRequested, String type) {
		this.userID = userID;
		this.isRequested = isRequested;
		this.type = type;
	}

	public String getUserID() {
		return userID;
	}

	public boolean isRequested() {
		return isRequested;
	}

	public String getType() {
		return type;
	}
}
