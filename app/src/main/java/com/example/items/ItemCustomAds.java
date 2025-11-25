package blogtalk.com.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class ItemCustomAds implements Serializable {

	public long id;

	@SerializedName("ad_id")
	String adID;

	@SerializedName("title")
	String title;

	@SerializedName("image")
	String image;

	@SerializedName("url")
	String url;

	@SerializedName("display_on")
	String displayOn;

	@SerializedName("ad_position")
	int adPosition;

	@SerializedName("status")
	String status;

	public ItemCustomAds(long id, String adID, String title, String image, String url, String displayOn, int adPosition, String status) {
		this.id = id;
		this.adID = adID;
		this.title = title;
		this.image = image;
		this.url = url;
		this.displayOn = displayOn;
		this.adPosition = adPosition;
		this.status = status;
	}

	public long getId() {
		return id;
	}

	public String getAdID() {
		return adID;
	}

	public String getTitle() {
		return title;
	}

	public String getImage() {
		return image;
	}

	public String getUrl() {
		return url;
	}

	public String getDisplayOn() {
		return displayOn;
	}

	public int getAdPosition() {
		return adPosition;
	}

	public String getStatus() {
		return status;
	}
}