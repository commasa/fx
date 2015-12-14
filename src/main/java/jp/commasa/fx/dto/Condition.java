package jp.commasa.fx.dto;

@Deprecated
public class Condition {

	private String result;
	private double total;
	private double ratio;
	private double amount;

	public Condition() {
		result = "";
		ratio = 0;
		amount = 0;
	}

	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public double getTotal() {
		return total;
	}
	public void setTotal(double total) {
		this.total = total;
	}
	public double getRatio() {
		return ratio;
	}
	public void setRatio(double ratio) {
		this.ratio = ratio;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "Condition [result=" + result + ", total=" + total + ", ratio="
				+ ratio + ", amount=" + amount + "]";
	}

}
