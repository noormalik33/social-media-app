package blogtalk.com.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class ItemImageGallery implements Serializable {

	@SerializedName("id")
	String id;
	@SerializedName("post_id")
	String postID;
	@SerializedName("image")
	String image;

	public ItemImageGallery(String postID, String image) {
		this.postID = postID;
		this.image = image;
	}

	public String getId() {
		return id;
	}

	public String getPostID() {
		return postID;
	}

	public String getImage() {
		return image;
	}
}