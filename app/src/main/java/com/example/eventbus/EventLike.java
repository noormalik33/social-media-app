package blogtalk.com.eventbus;

import blogtalk.com.items.ItemPost;

import java.io.Serializable;

public class EventLike implements Serializable{

	int position;
	ItemPost itemPost;
	boolean isLike = false;

	public EventLike(int position, ItemPost itemPost, boolean isLike) {
		this.position = position;
		this.itemPost = itemPost;
		this.isLike = isLike;
	}

	public EventLike(ItemPost itemPost, boolean isLike) {
		this.itemPost = itemPost;
		this.isLike = isLike;
	}

	public int getPosition() {
		return position;
	}

	public ItemPost getItemPost() {
		return itemPost;
	}

	public boolean isLike() {
		return isLike;
	}
}
