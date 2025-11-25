package blogtalk.com.eventbus;

import java.io.Serializable;

public class EventStoryUpload implements Serializable{

	boolean isStoryUploaded;

	public EventStoryUpload(boolean isStoryUploaded) {
		this.isStoryUploaded = isStoryUploaded;
	}

	public boolean isStoryUploaded() {
		return isStoryUploaded;
	}
}
