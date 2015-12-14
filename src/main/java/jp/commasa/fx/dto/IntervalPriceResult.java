package jp.commasa.fx.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

public class IntervalPriceResult {
	private String symbol;
	private String model;
	private double volatility;
	private double buyPL;
	private double sellPL;
	private Date baseBegin;
	private Date baseEnd;
	private Date last;

	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public String getModel() {
		return model;
	}
	public void setModel(String model) {
		this.model = model;
	}
	public double getVolatility() {
		return volatility;
	}
	public void setVolatility(double volatility) {
		this.volatility = volatility;
	}
	public double getBuyPL() {
		return buyPL;
	}
	public void setBuyPL(double buyPL) {
		this.buyPL = buyPL;
	}
	public double getSellPL() {
		return sellPL;
	}
	public void setSellPL(double sellPL) {
		this.sellPL = sellPL;
	}	
	public Date getBaseBegin() {
		return baseBegin;
	}
	public void setBaseBegin(Date baseBegin) {
		this.baseBegin = baseBegin;
	}
	public Date getBaseEnd() {
		return baseEnd;
	}
	public void setBaseEnd(Date baseEnd) {
		this.baseEnd = baseEnd;
	}
	public Date getLast() {
		return last;
	}
	public void setLast(Date last) {
		this.last = last;
	}
	
	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd-hh:mm:ss");
		return "IntervalPriceResult [symbol=" + symbol + ", last=" + sdf.format(last) + ", model=" + model
				+ ", volatility=" + volatility + ", buyPL=" + buyPL + ", sellPL=" + sellPL 
				+ ", baseBegin=" + sdf.format(baseBegin) + ", baseEnd=" + sdf.format(baseEnd) + "]";
	}
	
}
