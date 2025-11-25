package blogtalk.com.eventbus;

import blogtalk.com.items.ItemPost;

import java.io.Serializable;

public class EventUpdatePost implements Serializable{

	ItemPost itemPost;

	public EventUpdatePost(ItemPost itemPost) {
		this.itemPost = itemPost;
	}

	public ItemPost getItemPost() {
		return itemPost;
	}
}
