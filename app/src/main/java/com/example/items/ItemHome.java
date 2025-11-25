package blogtalk.com.items;

import java.io.Serializable;
import java.util.ArrayList;

public class ItemHome implements Serializable{

	String type;
	ArrayList<ItemPost> arrayListStatus;

	public ItemHome(String type, ArrayList<ItemPost> arrayListStatus) {
		this.type = type;
		this.arrayListStatus = arrayListStatus;
	}

	public String getType() {
		return type;
	}

	public ArrayList<ItemPost> getArrayListStatus() {
		return arrayListStatus;
	}
}
