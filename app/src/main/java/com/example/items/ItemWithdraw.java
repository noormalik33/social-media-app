package blogtalk.com.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;


public class ItemWithdraw implements Serializable {

	@SerializedName("id")
	String id;
	@SerializedName("points")
	int points;
	@SerializedName("one_points")
	int onePoints;
	@SerializedName("one_money")
	int oneMoney;
	@SerializedName("amount")
	String amount;
	@SerializedName("request_date")
	String requestDate;
	@SerializedName("payout_date")
	String payoutDate;
	@SerializedName("payout_reference")
	String payoutReference;
	@SerializedName("status")
	int status;

	public String getId() {
		return id;
	}

	public int getPoints() {
		return points;
	}

	public int getOnePoints() {
		return onePoints;
	}

	public int getOneMoney() {
		return oneMoney;
	}

	public String getAmount() {
		return amount;
	}

	public String getRequestDate() {
		return requestDate;
	}

	public String getPayoutDate() {
		return payoutDate;
	}

	public String getPayoutReference() {
		return payoutReference;
	}

	public boolean isStatusApproved() {
		return status==1;
	}
}