package jp.commasa.fx;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import jp.commasa.fx.dao.OrdersDao;
import jp.commasa.fx.dao.SimulatorDao;
import jp.commasa.fx.dto.Candlestick;
import jp.commasa.fx.dto.Condition;
import jp.commasa.fx.dto.Orders;
import jp.commasa.fx.dto.Price;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import quickfix.FieldNotFound;
import quickfix.SessionNotFound;
import quickfix.field.Symbol;

@Deprecated
public class Simulator implements Runnable {

	private Logger log = LogManager.getLogger(this.getClass());
	private Map<String, Candlestick> candlestick = new HashMap<String, Candlestick>();
	private List<Price> pool = Collections.synchronizedList(new ArrayList<Price>());
	private boolean finish = false;

	private OandaRateApp application;
	private List<Condition> conditions = new ArrayList<Condition>();
	private SimulatorDao simulatorDao = null;
	private OrdersDao ordersDao = null;
	private Map<String, Long> lastOrder = new HashMap<String, Long>();

	public void init(ResourceBundle bundle, List<Symbol> symbols, OandaRateApp application) {
		this.application = application;
		for (Symbol s: symbols) {
			candlestick.put(s.getValue(), new Candlestick(s.getValue()));
			lastOrder.put(s.getValue(), new Long(0));
		}
		int i = 0;
		while ( i < 100 ) {
			try {
				String value = bundle.getString("condition." + String.valueOf(i));
				String[] e = value.split(",");
				Condition c = new Condition();
				c.setResult(e[0]);
				c.setTotal(Double.valueOf(e[1]));
				c.setRatio(Double.valueOf(e[2]));
				c.setAmount(Double.valueOf(e[3]));
				conditions.add(c);
			} catch(Exception e) {}
			i++;
		}
		simulatorDao = new SimulatorDao();
		simulatorDao.open();
		ordersDao = new OrdersDao();
		ordersDao.open();
	}

	public void simulate(Map<String, Price> pMap) {
		for(Map.Entry<String, Price> entry: pMap.entrySet()) {
			if (entry.getValue().getSymbol()!=null && entry.getValue().getBid()!=0 && entry.getValue().getAsk()!=0) {
				pool.add(entry.getValue());
			}
		}
		synchronized (this) {
			notify();
		}
	}

	public void simulate(Price p) {
		pool.add(p);
		synchronized (this) {
			notify();
		}
	}

	public void close () {
		try {
			closeAllOrder();
		} catch (SQLException | SessionNotFound | FieldNotFound e) {
			log.error("closeAllOrder error: ", e);
		}
		finish = true;
		simulatorDao.close();
		ordersDao.close();
	}

	@Override
	public void run() {
		while (!finish) {
			// モデル登録
			Price curr = null;
			synchronized (pool) {
				if (pool.size() > 0) curr = new Price(pool.get(0));
			}
			if (curr != null) {
				Candlestick c = candlestick.get(curr.getSymbol());
				c.setPrice(curr.getDate(), curr.getBid(), curr.getAsk());
				StringBuffer sb = new StringBuffer();
				String result = c.getModel(sb);
				if ( result != null ) {
					try {
						simulatorDao.executeUpdate(curr.getSymbol(), sb.toString(), result);
					} catch (SQLException e) {
						log.error("INSERT/UPDATE ERROR", e);
					}
				}
				synchronized (pool) {
					pool.remove(0);
				}

				// 売買条件検証
				String premise = c.newModel();
				if ( premise != null ) {
					String symbol = curr.getSymbol();
					BigDecimal tickNo = curr.getTickNo();
					try {
						// 浮きポジションのうち、期限切れをクローズする
						try {
							closeOrder(symbol, tickNo);
						} catch (SessionNotFound | FieldNotFound e) {
							log.error("ORDER CLOSE ERROR symbol=" + symbol + ", tickNo=" + tickNo.toPlainString(), e);
						}
						// 新規注文
						Map<String, Condition> summary = simulatorDao.executeQuery(symbol, premise);
						for (Condition condition: conditions) {
							Condition sum = summary.get(condition.getResult());
							if (sum != null) {
								if ( sum.getTotal() >= condition.getTotal() && sum.getRatio() >= condition.getRatio() ) {
									Long prevOrderTime = lastOrder.get(symbol);
									if ( System.currentTimeMillis() - prevOrderTime > 60000 ) {
										log.info("<< ORDER >> " + symbol + " " + sum.toString());
										try {
											newOrder(symbol, condition.getAmount(), tickNo);
											lastOrder.put(symbol, System.currentTimeMillis());
										} catch (SessionNotFound | FieldNotFound e) {
											log.error("ORDER ERROR symbol=" + symbol + ", amount=" + condition.getAmount() + ", tickNo=" + tickNo.toPlainString(), e);
										}
									} else {
										log.info("<SKIPORDER> " + symbol + " " + sum.toString());
									}
								} else {
									log.debug("<NOT ORDER> " + symbol + " " + sum.toString());
								}
							}
						}
					} catch (SQLException e) {
						log.error("SELECT ERROR", e);
					}
				}

			} else {
				// プールの残りが無くなったら待機
				try {
					synchronized (this) {
						wait();
					}
				} catch (InterruptedException e) {
					log.error("wait error: ", e);
					break;
				}
			}

		}

	}

	private void closeOrder(String symbol, BigDecimal tickNo) throws SQLException, SessionNotFound, FieldNotFound {
		// ポジションクローズ処理
		List<Orders> closeList = ordersDao.getCloseOrder(symbol);
		try {
			Map<String, Orders> newOrderList = application.closeOrder(closeList, tickNo);
			for ( Map.Entry<String, Orders> entry : newOrderList.entrySet() ) {
				log.info("<< CLOSE >> " + entry.getValue().getSymbol() + " " + entry.getValue().getSide() + " " + entry.getValue().getOrderQty());
				ordersDao.execute(entry.getValue(), entry.getKey());
			}
		} catch (SessionNotFound | FieldNotFound e) {
			log.error("ORDER ERROR", e);
		}
	}

	public void closeAllOrder() throws SQLException, SessionNotFound, FieldNotFound {
		// ポジションクローズ処理
		List<Orders> closeList = ordersDao.getCloseAllOrder();
		try {
			Map<String, Orders> newOrderList = application.closeOrder(closeList, null);
			for ( Map.Entry<String, Orders> entry : newOrderList.entrySet() ) {
				log.info("<< CLOSE >> " + entry.getValue().getSymbol() + " " + entry.getValue().getSide() + " " + entry.getValue().getOrderQty());
				ordersDao.execute(entry.getValue(), entry.getKey());
			}
		} catch (SessionNotFound | FieldNotFound e) {
			log.error("ORDER ERROR", e);
		}
	}

	private void newOrder(String symbol, double amount, BigDecimal tickNo) throws SQLException, SessionNotFound, FieldNotFound {
		// 傾きチェック
		double pos = ordersDao.getOpenOrder(symbol);
		char posSide = '1';
		if ( pos < 0 ) posSide = '2';
		char orderSide = '1';
		double orderAmount = amount;
		if ( amount < 0 ) {
			orderSide = '2';
			orderAmount = amount * -1;
		}
		if ( pos == 0 || posSide == orderSide ) {
			Orders orders = application.newOrder(symbol, orderSide, orderAmount, tickNo);
			ordersDao.execute(orders, null);
		} else {
			log.info("< CANCEL >  " + symbol + " Position=" + pos);
		}
	}

}
