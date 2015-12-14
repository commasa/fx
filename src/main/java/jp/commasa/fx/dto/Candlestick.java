package jp.commasa.fx.dto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Deprecated
public class Candlestick {
	private final int MAX_SIZE_05 = 72;
	private final int MAX_SIZE_15 = 24;
	private final int MAX_SIZE_30 = 12;
	private final int SKIP_SIZE_05 = 12;
	private final int SKIP_SIZE_15 = 4;
	private final int SKIP_SIZE_30 = 2;
	private final int MODEL_SIZE_05 = 3;
	private final int MODEL_SIZE_15 = 2;
	private final int MODEL_SIZE_30 = 1;
	private int scale = 1000;
	private int round = 2;
	private String symbol;
	private List<CandlestickPrice> candlestick05 = new ArrayList<CandlestickPrice>();
	private List<CandlestickPrice> candlestick15 = new ArrayList<CandlestickPrice>();
	private List<CandlestickPrice> candlestick30 = new ArrayList<CandlestickPrice>();

	public Candlestick(String symbol) {
		this.symbol = symbol;
		if (symbol.endsWith("/USD")) scale = 100000;
	}

	public String getSymbol() {
		return symbol;
	}

	private void createCandlestickString(StringBuffer sb, CandlestickPrice cp) {
		Double diff = (cp.getClosePrice() - cp.getOpenPrice()) * scale / round;
		Double ueHige;
		Double shitaHige;
		int hige = 0;
		int iDiff = diff.intValue();
		int absDiff = Math.abs(iDiff);
		if (iDiff < 0) {
			sb.append("-");
			if (iDiff < -9) sb.append("A"); else sb.append(String.format("%1d", absDiff));
			ueHige = (cp.getMaxPrice() - cp.getOpenPrice()) * scale / round;
			shitaHige = (cp.getClosePrice() - cp.getMinPrice()) * scale / round;
		} else {
			sb.append("+");
			if (iDiff > 9) sb.append("A"); else sb.append(String.format("%1d", absDiff));
			ueHige = (cp.getMaxPrice() - cp.getOpenPrice()) * scale / round;
			shitaHige = (cp.getClosePrice() - cp.getMinPrice()) * scale / round;
		}
		if (ueHige.intValue() > absDiff) hige += 1;
		if (shitaHige.intValue() > absDiff) hige += 2;
		sb.append(String.format("%1d", hige));
	}

	public String getCandlestick05() {
		StringBuffer sb = new StringBuffer();
		for (CandlestickPrice cp : candlestick05) {
			createCandlestickString(sb, cp);
		}
		return sb.toString();
	}
	public String getCandlestick15() {
		StringBuffer sb = new StringBuffer();
		for (CandlestickPrice cp : candlestick15) {
			createCandlestickString(sb, cp);
		}
		return sb.toString();
	}
	public String getCandlestick30() {
		StringBuffer sb = new StringBuffer();
		for (CandlestickPrice cp : candlestick30) {
			createCandlestickString(sb, cp);
		}
		return sb.toString();
	}

	public String getModel(StringBuffer sb) {
		if (candlestick05.size() < SKIP_SIZE_05 + MODEL_SIZE_05
				|| candlestick15.size() < SKIP_SIZE_15 + MODEL_SIZE_15
				|| candlestick30.size() < SKIP_SIZE_30 + MODEL_SIZE_30) {
			return null;
		}
		int start05 = candlestick05.size() - (SKIP_SIZE_05 + MODEL_SIZE_05);
		int start15 = candlestick15.size() - (SKIP_SIZE_15 + MODEL_SIZE_15);
		int start30 = candlestick30.size() - (SKIP_SIZE_30 + MODEL_SIZE_30);
		for ( int i=start05; i<(start05+MODEL_SIZE_05); i++ ) {
			createCandlestickString(sb, candlestick05.get(i));
		}
		sb.append(",");
		for ( int i=start15; i<(start15+MODEL_SIZE_15); i++ ) {
			createCandlestickString(sb, candlestick15.get(i));
		}
		sb.append(",");
		for ( int i=start30; i<(start30+MODEL_SIZE_30); i++ ) {
			createCandlestickString(sb, candlestick30.get(i));
		}
		double bid = candlestick05.get(candlestick05.size()-1).getBid();
		double ask = candlestick05.get(candlestick05.size()-1).getAsk();
		double prevBid = candlestick05.get(candlestick05.size()-SKIP_SIZE_05-1).getBid();
		double prevAsk = candlestick05.get(candlestick05.size()-SKIP_SIZE_05-1).getAsk();
		///////////////////////////////////////////////////////////////////
		// BID > prev.ASK 0001
		// BID < prev.BID 0010
		// ASK > prev.ASK 0100
		// ASK < prev.BID 1000
		///////////////////////////////////////////////////////////////////
		int val = 0;
		if ( bid > prevAsk ) val += 1;
		if ( bid < prevBid ) val += 2;
		if ( ask > prevAsk ) val += 4;
		if ( ask < prevBid ) val += 8;
		return Integer.toHexString(val);
	}

	public String newModel() {
		StringBuffer sb = new StringBuffer();
		if (candlestick05.size() < MODEL_SIZE_05
				|| candlestick15.size() < MODEL_SIZE_15
				|| candlestick30.size() < MODEL_SIZE_30) {
			return null;
		}
		int start05 = candlestick05.size() - MODEL_SIZE_05;
		int start15 = candlestick15.size() - MODEL_SIZE_15;
		int start30 = candlestick30.size() - MODEL_SIZE_30;
		for ( int i=start05; i<(start05+MODEL_SIZE_05); i++ ) {
			createCandlestickString(sb, candlestick05.get(i));
		}
		sb.append(",");
		for ( int i=start15; i<(start15+MODEL_SIZE_15); i++ ) {
			createCandlestickString(sb, candlestick15.get(i));
		}
		sb.append(",");
		for ( int i=start30; i<(start30+MODEL_SIZE_30); i++ ) {
			createCandlestickString(sb, candlestick30.get(i));
		}
		return sb.toString();
	}

	public void setPrice(Date date, double bid, double ask) {
		Calendar cal = Calendar.getInstance();
		Calendar cal05 = Calendar.getInstance();
		Calendar cal15 = Calendar.getInstance();
		Calendar cal30 = Calendar.getInstance();
		cal.setTime(date);
		cal05.setTime(date);
		cal15.setTime(date);
		cal30.setTime(date);
		int sec = cal.get(Calendar.SECOND);
		if ( sec >= 0 && sec < 5 ) {
			cal05.set(Calendar.SECOND, 0);
			cal15.set(Calendar.SECOND, 0);
			cal30.set(Calendar.SECOND, 0);
		} else if ( sec >= 5 && sec < 10 ) {
			cal05.set(Calendar.SECOND, 5);
			cal15.set(Calendar.SECOND, 0);
			cal30.set(Calendar.SECOND, 0);
		} else if ( sec >= 10 && sec < 15 ) {
			cal05.set(Calendar.SECOND, 10);
			cal15.set(Calendar.SECOND, 0);
			cal30.set(Calendar.SECOND, 0);
		} else if ( sec >= 15 && sec < 20 ) {
			cal05.set(Calendar.SECOND, 15);
			cal15.set(Calendar.SECOND, 15);
			cal30.set(Calendar.SECOND, 0);
		} else if ( sec >= 20 && sec < 25 ) {
			cal05.set(Calendar.SECOND, 20);
			cal15.set(Calendar.SECOND, 15);
			cal30.set(Calendar.SECOND, 0);
		} else if ( sec >= 25 && sec < 30 ) {
			cal05.set(Calendar.SECOND, 25);
			cal15.set(Calendar.SECOND, 15);
			cal30.set(Calendar.SECOND, 0);
		} else if ( sec >= 30 && sec < 35 ) {
			cal05.set(Calendar.SECOND, 30);
			cal15.set(Calendar.SECOND, 30);
			cal30.set(Calendar.SECOND, 30);
		} else if ( sec >= 35 && sec < 40 ) {
			cal05.set(Calendar.SECOND, 35);
			cal15.set(Calendar.SECOND, 30);
			cal30.set(Calendar.SECOND, 30);
		} else if ( sec >= 40 && sec < 45 ) {
			cal05.set(Calendar.SECOND, 40);
			cal15.set(Calendar.SECOND, 30);
			cal30.set(Calendar.SECOND, 30);
		} else if ( sec >= 45 && sec < 50 ) {
			cal05.set(Calendar.SECOND, 45);
			cal15.set(Calendar.SECOND, 45);
			cal30.set(Calendar.SECOND, 30);
		} else if ( sec >= 50 && sec < 55 ) {
			cal05.set(Calendar.SECOND, 50);
			cal15.set(Calendar.SECOND, 45);
			cal30.set(Calendar.SECOND, 30);
		} else if ( sec >= 55 ) {
			cal05.set(Calendar.SECOND, 55);
			cal15.set(Calendar.SECOND, 45);
			cal30.set(Calendar.SECOND, 30);
		}
		CandlestickPrice candlestickPrice05 = new CandlestickPrice(cal05.getTime());
		CandlestickPrice candlestickPrice15 = new CandlestickPrice(cal15.getTime());
		CandlestickPrice candlestickPrice30 = new CandlestickPrice(cal30.getTime());
		if ( candlestick05.size() > 0 && candlestick05.get(candlestick05.size()-1).getDate().equals(cal05.getTime()) ) {
			candlestickPrice05 = candlestick05.get(candlestick05.size()-1);
		} else {
			candlestick05.add(candlestickPrice05);
		}
		if ( candlestick15.size() > 0 && candlestick15.get(candlestick15.size()-1).getDate().equals(cal15.getTime()) ) {
			candlestickPrice15 = candlestick15.get(candlestick15.size()-1);
		} else {
			candlestick15.add(candlestickPrice15);
		}
		if ( candlestick30.size() > 0 && candlestick30.get(candlestick30.size()-1).getDate().equals(cal30.getTime()) ) {
			candlestickPrice30 = candlestick30.get(candlestick30.size()-1);
		} else {
			candlestick30.add(candlestickPrice30);
		}
		candlestickPrice05.setPrice(bid, ask);
		candlestickPrice15.setPrice(bid, ask);
		candlestickPrice30.setPrice(bid, ask);
		// 過去分の削除
		int delcnt05 = candlestick05.size() - MAX_SIZE_05;
		for (int i=0; i<delcnt05; i++) candlestick05.remove(0);
		int delcnt15 = candlestick15.size() - MAX_SIZE_15;
		for (int i=0; i<delcnt15; i++) candlestick15.remove(0);
		int delcnt30 = candlestick30.size() - MAX_SIZE_30;
		for (int i=0; i<delcnt30; i++) candlestick30.remove(0);
	}

}
