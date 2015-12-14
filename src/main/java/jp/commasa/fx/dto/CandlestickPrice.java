package jp.commasa.fx.dto;

import java.util.Date;

@Deprecated
public class CandlestickPrice {
	private Date date;
	private double bid;
	private double ask;
	private Double openPrice;
	private Double closePrice;
	private Double maxPrice;
	private Double minPrice;

	public CandlestickPrice(Date date) {
		this.date = date;
		openPrice = null;
	}

	public void setPrice(double bid, double ask) {
		if (bid == 0 || ask == 0) return;
		this.bid = bid;
		this.ask = ask;
		double middle = (bid + ask) / 2;
		if (openPrice == null) openPrice = middle;
		closePrice = middle;
		if (maxPrice == null) maxPrice = middle;
		if (maxPrice < middle) maxPrice = middle;
		if (minPrice == null) minPrice = middle;
		if (minPrice > middle) minPrice = middle;
	}

	public Date getDate() {
		return date;
	}
	public double getBid() {
		return bid;
	}
	public double getAsk() {
		return ask;
	}
	public Double getOpenPrice() {
		return openPrice;
	}
	public Double getClosePrice() {
		return closePrice;
	}
	public Double getMaxPrice() {
		return maxPrice;
	}
	public Double getMinPrice() {
		return minPrice;
	}

	@Override
	public String toString() {
		return "CandlestickPrice [date=" + date + ", openPrice=" + openPrice + ", closePrice=" + closePrice
				+ ", maxPrice=" + maxPrice + ", minPrice=" + minPrice + "]";
	}

}
