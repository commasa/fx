package jp.commasa.fx.dto;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Price {
	private String symbol;
	private double bid;
	private double ask;
	private Date date;
	private String flg = "";
	private BigDecimal tickNo;

	public Price() {};

	public Price(Price p) {
		this.symbol = ( p.symbol == null ? "" : new String(p.symbol) );
		this.bid = p.bid;
		this.ask = p.ask;
		this.date = ( p.date == null ? null : new Date(p.date.getTime()) );
		this.flg = new String(p.flg);
		this.tickNo = new BigDecimal(p.tickNo.toPlainString());
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public double getAsk() {
		return ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getFlg() {
		return flg;
	}

	public void setFlg(String flg) {
		this.flg = flg;
	}

	public BigDecimal getTickNo() {
		return tickNo;
	}

	public void setTickNo(BigDecimal tickNo) {
		this.tickNo = tickNo;
	}

	public double getMid() {
		return (ask + bid) / 2;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return "Price [symbol=" + symbol + ", bid=" + bid + ", ask=" + ask 
				+ ", date=" + (date == null ? "" : sdf.format(date)) 
				+ ", flg=" + flg + ", tickNo=" + tickNo + "]";
	}

}
