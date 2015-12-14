package jp.commasa.fx.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Orders {

	protected static final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");

	private String orderID;
	private String origClOrdID;
	private String clOrdID;
	private String account;
	private String symbol;
	private String side;
	private Date transactTime;
	private Double orderQty;
	private String ordType;
	private Double price;
	private Double stopPx;
	private String timeInForce;
	private Date expireDate;
	private Date expireTime;
	private BigDecimal tickNo;
	private String openClOrdID;

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
	public Date getTransactTime() {
		return transactTime;
	}
	public void setTransactTime(Date transactTime) {
		this.transactTime = transactTime;
	}
	public Double getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(Double orderQty) {
		this.orderQty = orderQty;
	}
	public String getOrdType() {
		return ordType;
	}
	public void setOrdType(String ordType) {
		this.ordType = ordType;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Double getStopPx() {
		return stopPx;
	}
	public void setStopPx(Double stopPx) {
		this.stopPx = stopPx;
	}
	public String getTimeInForce() {
		return timeInForce;
	}
	public void setTimeInForce(String timeInForce) {
		this.timeInForce = timeInForce;
	}
	public Date getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}
	public Date getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}
	public BigDecimal getTickNo() {
		return tickNo;
	}
	public void setTickNo(BigDecimal tickNo) {
		this.tickNo = tickNo;
	}
	public String getOpenClOrdID() {
		return openClOrdID;
	}
	public void setOpenClOrdID(String openClOrdID) {
		this.openClOrdID = openClOrdID;
	}

	public void setExpireDate(String expireDate) {
		try {
			this.expireDate = new java.sql.Date(sdfDate.parse(expireDate).getTime());
		} catch (ParseException e) {}
	}

	@Override
	public String toString() {
		return "Orders [orderID=" + orderID + ", origClOrdID=" + origClOrdID
				+ ", clOrdID=" + clOrdID + ", account=" + account + ", symbol="
				+ symbol + ", side=" + side + ", transactTime=" + transactTime
				+ ", orderQty=" + orderQty + ", ordType=" + ordType
				+ ", price=" + price + ", stopPx=" + stopPx + ", timeInForce="
				+ timeInForce + ", expireDate=" + expireDate + ", expireTime="
				+ expireTime + ", tickNo=" + tickNo + ", openClOrdID=" + openClOrdID + "]";
	}

}
