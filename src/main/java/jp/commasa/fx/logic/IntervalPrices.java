package jp.commasa.fx.logic;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.commasa.fx.dao.IntervalPriceResultDao;
import jp.commasa.fx.dto.IntervalPriceResult;
import jp.commasa.fx.dto.Price;

public class IntervalPrices {
	private Logger log = LogManager.getLogger(this.getClass());
	private int interval;
	private Date nextDate;
	private Map<String, List<Price>> buffer;
	private static int BUFFER_SIZE = 33;
	private Date oldTime;
	private TimeZone TZ_NY = TimeZone.getTimeZone("America/New_York");
	private SimpleDateFormat sdfNY = new SimpleDateFormat("yyyyMMdd");
	private Calendar calNY = Calendar.getInstance();
	private IntervalPriceResultDao intervalPriceResultDao = new IntervalPriceResultDao();

	public IntervalPrices(int interval) {
		this.interval = interval;
		buffer = new HashMap<String, List<Price>>();
		nextDate = null;
		calNY.setTimeZone(TZ_NY);
		sdfNY.setTimeZone(TZ_NY);
	}
	
	protected boolean tradeSignal(Price price) {
		boolean result = false;
		if (price == null || price.getSymbol() == null || price.getDate() == null) { return result; }
		Calendar cal = Calendar.getInstance();
		cal.setTime(price.getDate());
		cal.set(Calendar.SECOND, 0);
		Date baseDate = cal.getTime();
		List<Price> buf = buffer.get(price.getSymbol());
		if (buf == null) {
			buf = new ArrayList<Price>(BUFFER_SIZE);
			buffer.put(price.getSymbol(), buf);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		if (nextDate == null || buf.size() < 1) {
			buf.add(price);
			nextDate = new Date(baseDate.getTime() + interval*1000);
			while (nextDate.compareTo(price.getDate()) < 0) {
				nextDate = new Date(nextDate.getTime() + interval*1000);
			}
			log.trace("    buf.add["+buf.size()+"]: " + price.toString() + " , nextDate: " + sdf.format(nextDate));
		} else {
			if (nextDate.compareTo(price.getDate()) <= 0) {
				Price lastPrice = null;
				if (buf.size() > 0) { lastPrice = buf.get(buf.size()-1); }
				nextDate = new Date(nextDate.getTime() + interval*1000);
				while (nextDate.compareTo(price.getDate()) <= 0) {
					if (lastPrice != null) {
						buf.add(lastPrice);
						log.trace("    buf["+buf.size()+"].add->" + lastPrice.toString() + " , nextDate: " + sdf.format(nextDate));
					}
					nextDate = new Date(nextDate.getTime() + interval*1000);
				}
				buf.add(price);
				log.trace("    buf["+buf.size()+"].add: " + price.toString() + " , nextDate: " + sdf.format(nextDate));
				while (buf.size() > BUFFER_SIZE) {
					buf.remove(0);
				}
			} else {
				buf.set(buf.size()-1, price);
				log.trace("    buf["+buf.size()+"].set: " + price.toString() + " , nextDate: " + sdf.format(nextDate));
			}
		}
		if ( oldTime != null && oldTime.compareTo(baseDate) != 0) {
			result = true;
		}
		oldTime = baseDate;
		return result;
	}
	
	protected IntervalPriceResult result(String symbol, int base, int range) {
		if (symbol == null) { return null; }
		List<Price> buf = buffer.get(symbol);
		if (buf == null) { return null; }
		IntervalPriceResult result = new IntervalPriceResult();
		result.setSymbol(symbol);
		int start = buf.size() - base - 2;
		int i = start;
		if (i < 0) { return null; }
		Price basePrice = buf.get(i);
		result.setBaseBegin(basePrice.getDate());
		log.trace("    base["+i+"]: " + basePrice.toString());
		i++;
		String model = "";
		double volatility = 0;
		int end = buf.size() - 1;
		while ( i < start + range && i <= end ) {
			log.trace("        ["+i+"]: " + buf.get(i).toString());
			double v = buf.get(i).getMid() - basePrice.getMid(); 
			volatility = volatility + Math.abs(v);
			if ( v > 0 ) {
				model = model + "H";
			} else if (v < 0) {
				model = model + "L";
			} else {
				model = model + "-";
			}
			result.setBaseEnd(buf.get(i).getDate());
			i++;
		}
		result.setLast(buf.get(end).getDate());
		result.setBuyPL(buf.get(end).getBid() - basePrice.getAsk());
		result.setSellPL(basePrice.getBid() - buf.get(end).getAsk());
		result.setModel(model);
		result.setVolatility(volatility);
		return result;
	}

	public String getTradeYMD() {
		return getTradeYMD(Calendar.getInstance());
	}
	public String getTradeYMD(Calendar cal) {
		calNY.setTime(cal.getTime());
		if (calNY.get(Calendar.HOUR_OF_DAY) >= 17) {
			calNY.add(Calendar.DATE, 1);
		}
		return sdfNY.format(calNY.getTime());
	}

	public boolean isOpen() {
		return isOpen(Calendar.getInstance());
	}
	public boolean isOpen(Calendar cal) {
		int h = cal.get(Calendar.HOUR_OF_DAY);
		if ( h >= 7 && h < 9 ) {
			return false;
		} else {
			return true;
		}
	}

	public void main(Map<String, Price> priceMap) {
		String tradeYMD = getTradeYMD();
		if ( isOpen() ) {
			for (Price p : priceMap.values()) {
				if (tradeSignal(p)) {
					IntervalPriceResult result = result(p.getSymbol(), (int)120/interval, (int)60/interval);
					if (result != null) {
						System.out.println(result.toString());
//						try {
//							intervalPriceResultDao.executeUpdate(tradeYMD, result);
//						} catch (SQLException e) {
//							log.error("INSERT IntervalPriceResult", e);
//						}
						//TODO モデルから注文条件を取得
						//TODO 注文呼び出し
					}
				}
			}
		} else {
			//TODO 時間外の時は、浮きポジションがないかチェックし、あればスクエアにする。
		}
	}
	public void finish() {
		intervalPriceResultDao.close();
	}

}
