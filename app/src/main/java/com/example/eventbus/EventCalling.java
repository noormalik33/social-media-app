package blogtalk.com.eventbus;

import blogtalk.com.items.ItemPost;

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
