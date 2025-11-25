package com.example.eventbus;

import com.example.items.ItemPost;

import java.io.Serializable;

public class EventCalling implements Serializable{

	String callType;

	public EventCalling(String callType) {
		this.callType = callType;
	}

	public String getCallType() {
		return callType;
	}
}
