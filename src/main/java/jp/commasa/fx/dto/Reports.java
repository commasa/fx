package jp.commasa.fx.dto;

import java.sql.Date;

public class Reports {

	private String orderID;
	private String origClOrdID;
	private String clOrdID;
	private String execID;
	private String execType;
	private String ordStatus;
	private int ordRejReason;
	private String account;
	private String symbol;
	private String side;
	private double orderQty;
	private String ordType;
	private double price;
	private double stopPx;
	private String timeInForce;
	private Date expireTime;
	private double lastQty;
	private double lastPx;
	private double leavesQty;
	private double cumQty;
	private double avgPx;
	private Date transactTime;
	private String cxlRejResponseTo;
	private int cxlRejReason;
	private String text;

	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public String getOrigClOrdID() {
		return origClOrdID;
	}
	public void setOrigClOrdID(String origClOrdID) {
		this.origClOrdID = origClOrdID;
	}
	public String getClOrdID() {
		return clOrdID;
	}
	public void setClOrdID(String clOrdID) {
		this.clOrdID = clOrdID;
	}
	public String getExecID() {
		return execID;
	}
	public void setExecID(String execID) {
		this.execID = execID;
	}
	public String getExecType() {
		return execType;
	}
	public void setExecType(String execType) {
		this.execType = execType;
	}
	public String getOrdStatus() {
		return ordStatus;
	}
	public void setOrdStatus(String ordStatus) {
		this.ordStatus = ordStatus;
	}
	public int getOrdRejReason() {
		return ordRejReason;
	}
	public void setOrdRejReason(int ordRejReason) {
		this.ordRejReason = ordRejReason;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getSide() {
		return side;
	}
	public void setSide(String side) {
		this.side = side;
	}
	public double getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(double orderQty) {
		this.orderQty = orderQty;
	}
	public String getOrdType() {
		return ordType;
	}
	public void setOrdType(String ordType) {
		this.ordType = ordType;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public double getStopPx() {
		return stopPx;
	}
	public void setStopPx(double stopPx) {
		this.stopPx = stopPx;
	}
	public String getTimeInForce() {
		return timeInForce;
	}
	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}
	public Date getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	public double getLastQty() {
		return lastQty;
	}
	public void setLastQty(double lastQty) {
		this.lastQty = lastQty;
	}
	public double getLastPx() {
		return lastPx;
	}
	public void setLastPx(double lastPx) {
		this.lastPx = lastPx;
	}
	public double getLeavesQty() {
		return leavesQty;
	}
	public void setLeavesQty(double leavesQty) {
		this.leavesQty = leavesQty;
	}
	public double getCumQty() {
		return cumQty;
	}
	public void setCumQty(double cumQty) {
		this.cumQty = cumQty;
	}
	public double getAvgPx() {
		return avgPx;
	}
	public void setAvgPx(double avgPx) {
		this.avgPx = avgPx;
	}
	public Date getTransactTime() {
		return transactTime;
	}
	public void setTransactTime(Date transactTime) {
		this.transactTime = transactTime;
	}
	public String getCxlRejResponseTo() {
		return cxlRejResponseTo;
	}
	public void setCxlRejResponseTo(String cxlRejResponseTo) {
		this.cxlRejResponseTo = cxlRejResponseTo;
	}
	public int getCxlRejReason() {
		return cxlRejReason;
	}
	public void setCxlRejReason(int cxlRejReason) {
		this.cxlRejReason = cxlRejReason;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}

	public void setExecType(char execType) {
		this.execType = String.valueOf(execType);
	}
	public void setOrdStatus(char ordStatus) {
		this.ordStatus = String.valueOf(ordStatus);
	}
	public void setSide(char side) {
		this.side = String.valueOf(side);
	}
	public void setOrdType(char ordType) {
		this.ordType = String.valueOf(ordType);
	}
	public void setTimeInForce(char timeInForce) {
		this.timeInForce = String.valueOf(timeInForce);
	}
	public void setExpireTime(java.util.Date expireTime) {
		this.expireTime = new Date(expireTime.getTime());
	}
	public void setTransactTime(java.util.Date transactTime) {
		this.transactTime = new Date(transactTime.getTime());
	}

	@Override
	public String toString() {
		return "Reports [orderID=" + orderID + ", origClOrdID=" + origClOrdID
				+ ", clOrdID=" + clOrdID + ", execID=" + execID + ", execType="
				+ execType + ", ordStatus=" + ordStatus + ", ordRejReason="
				+ ordRejReason + ", account=" + account + ", symbol=" + symbol
				+ ", side=" + side + ", orderQty=" + orderQty + ", ordType="
				+ ordType + ", price=" + price + ", stopPx=" + stopPx
				+ ", timeInForce=" + timeInForce + ", expireTime=" + expireTime
				+ ", lastQty=" + lastQty + ", lastPx=" + lastPx
				+ ", leavesQty=" + leavesQty + ", cumQty=" + cumQty
				+ ", avgPx=" + avgPx + ", transactTime=" + transactTime
				+ ", cxlRejResponseTo=" + cxlRejResponseTo + ", cxlRejReason="
				+ cxlRejReason + ", text=" + text + "]";
	}

}
