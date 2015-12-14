package jp.commasa.fx.logic;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.JUnitCore;

import jp.commasa.fx.dto.IntervalPriceResult;
import jp.commasa.fx.dto.Price;
import jp.commasa.fx.logic.IntervalPrices;

public class IntervalPricesTest {

	public static void main(String[] args) {
		JUnitCore.main(IntervalPricesTest.class.getName());
	}
	
	private static BigDecimal tickNo = BigDecimal.ZERO;
	private static Price getInstance(String datetime, String symbol, double bid, double ask) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss");
		Price price = new Price();
		price.setSymbol(symbol);
		price.setBid(bid);
		price.setAsk(ask);
		try {
			price.setDate(sdf.parse(datetime));
		} catch (ParseException e) {
			price.setDate(null);
		}
		tickNo = tickNo.add(BigDecimal.ONE);
		price.setTickNo(tickNo);
		return price;
	}
	
	@Test
	public void testPrice() {
		IntervalPrices ip = new IntervalPrices(10);
		assertThat(ip.tradeSignal(getInstance("20151206 065959", "USD/JPY", 112.654, 112.657)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070001", "USD/JPY", 112.654, 112.657)), is(Boolean.TRUE));
		IntervalPriceResult result = ip.result("USD/JPY", 12, 6);
		assertThat(result, nullValue());
		assertThat(ip.tradeSignal(getInstance("20151206 070006", "USD/JPY", 112.653, 112.656)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070011", "USD/JPY", 112.652, 112.655)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070016", "USD/JPY", 112.651, 112.654)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070021", "USD/JPY", 112.652, 112.655)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070026", "USD/JPY", 112.653, 112.656)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070031", "USD/JPY", 112.654, 112.657)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070036", "USD/JPY", 112.655, 112.658)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070041", "USD/JPY", 112.656, 112.659)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070046", "USD/JPY", 112.657, 112.660)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070051", "USD/JPY", 112.658, 112.661)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070056", "USD/JPY", 112.659, 112.662)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070101", "USD/JPY", 112.660, 112.663)), is(Boolean.TRUE));
		result = ip.result("USD/JPY", 12, 6);
		assertThat(result, nullValue());
		assertThat(ip.tradeSignal(getInstance("20151206 070106", "USD/JPY", 112.661, 112.664)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070111", "USD/JPY", 112.662, 112.665)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070116", "USD/JPY", 112.663, 112.666)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070121", "USD/JPY", 112.664, 112.667)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070126", "USD/JPY", 112.663, 112.666)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070131", "USD/JPY", 112.662, 112.665)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070156", "USD/JPY", 112.661, 112.664)), is(Boolean.FALSE));
		assertThat(ip.tradeSignal(getInstance("20151206 070201", "USD/JPY", 112.660, 112.663)), is(Boolean.TRUE));
		result = ip.result("USD/JPY", 12, 6);
		assertThat(result, notNullValue());
		System.out.println(result.toString());
	}
	
	@Test
	public void testGetTradeYMD() throws ParseException {
		Calendar cal = Calendar.getInstance();
		IntervalPrices ip = new IntervalPrices(10);
		System.out.println("getTradeYMD: " + ip.getTradeYMD());
		// 夏時間
		cal.set(2015, 10, 1, 5, 59);
		assertThat(ip.getTradeYMD(cal), is("20151031"));
		cal.set(2015, 10, 1, 6, 0);
		assertThat(ip.getTradeYMD(cal), is("20151101"));
		cal.set(2015, 10, 1, 6, 59);
		assertThat(ip.getTradeYMD(cal), is("20151101"));
		cal.set(2015, 10, 1, 7, 00);
		assertThat(ip.getTradeYMD(cal), is("20151101"));
		// 標準時間（冬時間）
		cal.set(2015, 10, 2, 5, 59);
		assertThat(ip.getTradeYMD(cal), is("20151101"));
		cal.set(2015, 10, 2, 6, 0);
		assertThat(ip.getTradeYMD(cal), is("20151101"));
		cal.set(2015, 10, 2, 6, 59);
		assertThat(ip.getTradeYMD(cal), is("20151101"));
		cal.set(2015, 10, 2, 7, 00);
		assertThat(ip.getTradeYMD(cal), is("20151102"));
		// 13時〜23時の確認 hhとHHのミス確認
		cal.set(2015, 10, 2, 15, 00);
		assertThat(ip.getTradeYMD(cal), is("20151102"));
 	}

	@Test
	public void testIsOpen() throws ParseException {
		Calendar cal = Calendar.getInstance();
		IntervalPrices ip = new IntervalPrices(10);
		System.out.println("isOpen: " + ip.isOpen());
		// 夏時間
		cal.set(2015, 10, 1, 8, 0);
		assertThat(ip.isOpen(cal), is(Boolean.FALSE));
		cal.set(2015, 10, 1, 9, 0);
		assertThat(ip.isOpen(cal), is(Boolean.TRUE));
		cal.set(2015, 10, 2, 3, 00);
		assertThat(ip.isOpen(cal), is(Boolean.FALSE));
		// 標準時間（冬時間）
		cal.set(2015, 10, 2, 8, 0);
		assertThat(ip.isOpen(cal), is(Boolean.FALSE));
		cal.set(2015, 10, 2, 9, 0);
		assertThat(ip.isOpen(cal), is(Boolean.TRUE));
		cal.set(2015, 10, 2, 3, 00);
		assertThat(ip.isOpen(cal), is(Boolean.FALSE));
		// 13時〜23時の確認 hhとHHのミス確認
		cal.set(2015, 10, 2, 15, 00);
		assertThat(ip.isOpen(cal), is(Boolean.TRUE));
 	}

}
