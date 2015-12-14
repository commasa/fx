package jp.commasa.fx;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import jp.commasa.fx.dao.ReportsDao;
import jp.commasa.fx.dto.Orders;
import jp.commasa.fx.dto.Price;
import jp.commasa.fx.dto.Reports;
import jp.commasa.fx.logic.IntervalPrices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.StringField;
import quickfix.UnsupportedMessageType;
import quickfix.field.Account;
import quickfix.field.AvgPx;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecType;
import quickfix.field.ExpireDate;
import quickfix.field.ExpireTime;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LeavesQty;
import quickfix.field.LinesOfText;
import quickfix.field.MDEntryDate;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryTime;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.NoMDEntries;
import quickfix.field.OrdRejReason;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Password;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.field.TargetSubID;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.fix44.MarketDataRequest;
import quickfix.fix44.NewOrderSingle;

public class OandaRateApp extends MessageCracker implements Application {

	private Logger log = LogManager.getLogger(this.getClass());
	private SessionSettings settings;
	private final String RATES = "RATES";
	private SessionID rateSessionID = null;
	private String rateReqID;
	private boolean isRateLogOn = false;
	private final String ORDER = "ORDER";
	private SessionID orderSessionID = null;
	private String account;
	private boolean isOrderLogOn = false;

	private List<Symbol> symbols;
	private Map<String, Price> current = new HashMap<String, Price>();
	IntervalPrices intervalPrices = new IntervalPrices(10);

	private ReportsDao reportsDao = new ReportsDao();

	public OandaRateApp(SessionSettings settings, ResourceBundle bundle)
	{
		this.settings = settings;
		List<Symbol> symbolList = new ArrayList<Symbol>();
		String strSymbols = bundle.getString("symbols");
		if (strSymbols != null && !"".equals(strSymbols)) {
			String[] ss = strSymbols.split(",");
			for (String s : ss) {
				symbolList.add(new Symbol(s.trim()));
			}
		}
		this.symbols = symbolList;
		reportsDao.open();

	}

	@Override
	public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
		log.info("fromAdmin : SessionID=" + sessionId.toString() + " , Message=" + message.getClass().getName());
	}

	@Override
	public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		try {
			crack(message, sessionId);
		} catch (Exception e) {
			log.error("ERROR fromApp : SessionID=" + sessionId.toString() + " , Message=" + message.toString(), e);
		}
	}

	@Override
	public void onCreate(SessionID sessionID) {
		if (RATES.equalsIgnoreCase(sessionID.getTargetSubID())) rateSessionID = sessionID;
		if (ORDER.equalsIgnoreCase(sessionID.getTargetSubID())) orderSessionID = sessionID;
		log.info("onCreate : SessionID=" + sessionID.toString());
	}

	@Override
	public void onLogon(SessionID sessionID) {
		if (RATES.equalsIgnoreCase(sessionID.getTargetSubID())) {
			synchronized (this) { isRateLogOn = true; }
			try {
				requestMarketData();
			} catch (SessionNotFound e) {
				log.error("requestMarketData : SessionID=" + sessionID.toString(), e);
			}
		}
		if (ORDER.equalsIgnoreCase(sessionID.getTargetSubID())) synchronized (this) { isOrderLogOn = true; }
		log.info("onLogon : SessionID=" + sessionID.toString());
	}

	@Override
	public void onLogout(SessionID sessionID) {
		if (RATES.equalsIgnoreCase(sessionID.getTargetSubID())) synchronized (this) { isRateLogOn = false; }
		if (ORDER.equalsIgnoreCase(sessionID.getTargetSubID())) synchronized (this) { isOrderLogOn = false; }
		log.info("onLogout : SessionID=" + sessionID.toString());
	}

	@Override
	public void toAdmin(Message message, SessionID sessionID) {
		MsgType msgType = new MsgType();
		try {
			//連続ログインで攻撃とみなされないように待機
			try {
				Random rnd = new Random();
				int wait = rnd.nextInt(20);
				Thread.sleep(wait*100);
			} catch (InterruptedException e) {}
			try {
				account = settings.getString(sessionID, "Account");
			} catch(ConfigError e) { /* RATEのときは不要なタグのため、何もしない */ }
			StringField type = message.getHeader().getField(msgType);
			message.getHeader().setField(new TargetSubID(settings.getString(sessionID, "TargetSubID")));
			if (type.valueEquals(MsgType.LOGON)) {
				if (!message.isSetField(Password.FIELD)) {
					message.setField(new Password(settings.getString(sessionID, "Password")));
				}
				message.setField(new ResetSeqNumFlag(true));
			}
			log.info("toAdmin : SessionID=" + sessionID.toString() + " , Message=" + message.getClass().getName());
		} catch (Exception e) {
			log.error("ERROR toAdmin : ", e);
		}
	}

	@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
		log.info("toApp : SessionID=" + sessionId.toString() + " , Message=" + message.getClass().getName());
	}

	public void requestMarketData() throws SessionNotFound {
		int cnt = 0;
		while (!isRateLogOn) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new SessionNotFound();
			}
			if (cnt>60) throw new SessionNotFound(); else cnt++;
		}
		MDReqID mdReqID = new MDReqID();
		SubscriptionRequestType subType = new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES);
		MarketDepth marketDepth = new MarketDepth(1);
		MarketDataRequest message = new MarketDataRequest(mdReqID, subType, marketDepth);

		rateReqID = Long.toString(System.currentTimeMillis());
		message.setField(new MDReqID(rateReqID));
		message.setField(new MDUpdateType(MDUpdateType.INCREMENTAL_REFRESH));

		MarketDataRequest.NoMDEntryTypes marketDataEntry = new MarketDataRequest.NoMDEntryTypes();
		marketDataEntry.set(new MDEntryType(MDEntryType.BID));
		message.addGroup(marketDataEntry);
		marketDataEntry.set(new MDEntryType(MDEntryType.OFFER));
		message.addGroup(marketDataEntry);

		MarketDataRequest.NoRelatedSym symbol = new MarketDataRequest.NoRelatedSym();
		for (Symbol s: symbols) {
			symbol.set(s);
			message.addGroup(symbol);
			current.put(s.getValue(), new Price());
		}

		if (message != null) {
			Session.sendToTarget(message, rateSessionID);
			log.info("QueryMarketDataRequest : " + message.toString().replace('\u0001', '|'));
		}
	}

	public Orders newOrder(String symbol, char side, double qty, BigDecimal tickNo) throws SessionNotFound, FieldNotFound {
		Orders result = null;
		int cnt = 0;
		while (!isOrderLogOn) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new SessionNotFound();
			}
			if (cnt>60) throw new SessionNotFound(); else cnt++;
		}
		NewOrderSingle message = new NewOrderSingle();
		message.set(new ClOrdID(getOrderID()));
		message.set(new Account(account));
		message.set(new Symbol(symbol));
		message.set(new Side(side));
		message.set(new OrderQty(qty));
		message.set(new OrdType(OrdType.MARKET));
		message.set(new TransactTime(new Date()));
		if (message != null) {
			Session.sendToTarget(message, orderSessionID);
			log.info("newOrder : " + message.toString().replace('\u0001', '|'));
			result = new Orders();
			ClOrdID fClOrdID = new ClOrdID();
			Account fAccount = new Account();
			Symbol fSymbol = new Symbol();
			Side fSide = new Side();
			TransactTime fTransactTime = new TransactTime();
			OrderQty fOrderQty = new OrderQty();
			OrdType fOrdType = new OrdType();
			quickfix.field.Price fPrice = new quickfix.field.Price();
			StopPx fStopPx = new StopPx();
			TimeInForce fTimeInForce = new TimeInForce();
			ExpireDate fExpireDate = new ExpireDate();
			ExpireTime fExpireTime = new ExpireTime();
			message.get(fClOrdID);
			message.get(fAccount);
			message.get(fSymbol);
			message.get(fSide);
			message.get(fTransactTime);
			message.get(fOrderQty);
			message.get(fOrdType);
			try { message.get(fPrice); } catch(FieldNotFound e) {}
			try { message.get(fStopPx); } catch(FieldNotFound e) {}
			try { message.get(fTimeInForce); } catch(FieldNotFound e) {}
			try { message.get(fExpireDate); } catch(FieldNotFound e) {}
			try { message.get(fExpireTime); } catch(FieldNotFound e) {}
			result.setClOrdID(fClOrdID.getValue());
			result.setAccount(fAccount.getValue());
			result.setSymbol(fSymbol.getValue());
			result.setSide(String.valueOf(fSide.getValue()));
			result.setTransactTime(new java.sql.Date(fTransactTime.getValue().getTime()));
			result.setOrderQty(fOrderQty.getValue());
			result.setOrdType(String.valueOf(fOrdType.getValue()));
			result.setPrice(fPrice.getValue());
			result.setStopPx(fStopPx.getValue());
			result.setTimeInForce(String.valueOf(fTimeInForce.getValue()));
			result.setExpireDate(fExpireDate.getValue());
			result.setExpireTime(new java.sql.Date(fExpireTime.getValue().getTime()));
			result.setTickNo(tickNo);
		}
		return result;
	}

	public Map<String, Orders> closeOrder(List<Orders> openPositions, BigDecimal tickNo) throws SessionNotFound, FieldNotFound {
		Map<String, Orders> result = new HashMap<String, Orders>();
		for ( Orders orders : openPositions ) {
			if ("1".equals(orders.getSide())) {
				Orders newOrders = newOrder(orders.getSymbol(), '2', orders.getOrderQty(), tickNo);
				result.put(orders.getClOrdID(), newOrders);
			}
			if ("2".equals(orders.getSide())) {
				Orders newOrders = newOrder(orders.getSymbol(), '1', orders.getOrderQty(), tickNo);
				result.put(orders.getClOrdID(), newOrders);
			}
		}
		return result;
	}

	private String getOrderID() {
		try { Thread.sleep(1L); } catch (InterruptedException e) {}
		String ordID = Long.toString(System.currentTimeMillis());
		return ordID;
	}

	public void onMessage(quickfix.fix44.MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound ,UnsupportedMessageType ,IncorrectTagValue {
		MDReqID mdReqID = new MDReqID();
		message.get(mdReqID);
		if (mdReqID.valueEquals(rateReqID)) {
			MsgSeqNum seqNum = new MsgSeqNum();
			message.getHeader().getField(seqNum);
			Symbol symbol = new Symbol();
			message.get(symbol);
			NoMDEntries noMDEntries = new NoMDEntries();
			message.get(noMDEntries);
			quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries group = new quickfix.fix44.MarketDataSnapshotFullRefresh.NoMDEntries();
			MDEntryType mdEntryType = new MDEntryType();
			MDEntryPx mdEntryPx = new MDEntryPx();
			MDEntrySize mdEntrySize = new MDEntrySize();
			MDEntryDate mdEntryDate = new MDEntryDate();
			MDEntryTime mdEntryTime = new MDEntryTime();
			Text text = new Text();
			for (int i=1; i<=noMDEntries.getValue(); i++) {
				message.getGroup(i, group);
				group.get(mdEntryType);
				group.get(mdEntryPx);
				group.get(mdEntrySize);
				group.get(mdEntryDate);
				group.get(mdEntryTime);
				String strText = "";
				try { group.get(text); strText = text.getValue(); } catch (FieldNotFound e) {}
				if ( "".equals(strText) ) {
					Price p = current.get(symbol.getValue());
					if (p != null) {
						p.setSymbol(symbol.getValue());
						if (mdEntryType.getValue() == '0') p.setBid(mdEntryPx.getValue());
						if (mdEntryType.getValue() == '1') p.setAsk(mdEntryPx.getValue());
						p.setDate(new Date(mdEntryDate.getValue().getTime() + mdEntryTime.getValue().getTime()));
						p.setTickNo(BigDecimal.valueOf(seqNum.getValue()));
					}
				}
			}
			intervalPrices.main(current);
		}
	}

	public void onMessage(quickfix.fix44.MarketDataIncrementalRefresh message, SessionID sessionID) throws FieldNotFound ,UnsupportedMessageType ,IncorrectTagValue {
		MDReqID mdReqID = new MDReqID();
		message.get(mdReqID);
		if (mdReqID.valueEquals(rateReqID)) {
			MsgSeqNum seqNum = new MsgSeqNum();
			message.getHeader().getField(seqNum);
			NoMDEntries noMDEntries = new NoMDEntries();
			message.get(noMDEntries);
			quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries group = new quickfix.fix44.MarketDataIncrementalRefresh.NoMDEntries();
			Symbol symbol = new Symbol();
			MDEntryType mdEntryType = new MDEntryType();
			MDEntryPx mdEntryPx = new MDEntryPx();
			MDEntrySize mdEntrySize = new MDEntrySize();
			MDEntryDate mdEntryDate = new MDEntryDate();
			MDEntryTime mdEntryTime = new MDEntryTime();
			Text text = new Text();
			for (int i=1; i<=noMDEntries.getValue(); i++) {
				message.getGroup(i, group);
				group.get(symbol);
				group.get(mdEntryType);
				group.get(mdEntryPx);
				group.get(mdEntrySize);
				group.get(mdEntryDate);
				group.get(mdEntryTime);
				String strText = "";
				try { group.get(text); strText = text.getValue(); } catch (FieldNotFound e) {}
				if ( "".equals(strText) ) {
					Price p = current.get(symbol.getValue());
					if (p != null) {
						p.setSymbol(symbol.getValue());
						if (mdEntryType.getValue() == '0') p.setBid(mdEntryPx.getValue());
						if (mdEntryType.getValue() == '1') p.setAsk(mdEntryPx.getValue());
						p.setDate(new Date(mdEntryDate.getValue().getTime() + mdEntryTime.getValue().getTime()));
						p.setTickNo(BigDecimal.valueOf(seqNum.getValue()));
					}
				}
			}
			intervalPrices.main(current);
		}
	}

	public void onMessage(quickfix.fix44.News message, SessionID sessionID) throws FieldNotFound ,UnsupportedMessageType ,IncorrectTagValue {
		LinesOfText lines = new LinesOfText();
		message.get(lines);
		quickfix.fix44.News.LinesOfText group = new quickfix.fix44.News.LinesOfText();
		Text text = new Text();
		for (int i=1; i<=lines.getValue(); i++) {
			message.getGroup(i, group);
			group.get(text);
			log.info(text.getValue());
		}
	}

	public void onMessage(quickfix.fix44.ExecutionReport message, SessionID sessionID) throws FieldNotFound ,UnsupportedMessageType ,IncorrectTagValue {
		log.info("ExecutionReport : " + message.toString().replace('\u0001', '|'));
		OrderID orderID = new OrderID();
		OrigClOrdID origClOrdID = new OrigClOrdID();
		ClOrdID clOrdID = new ClOrdID();
		ExecID execID = new ExecID();
		ExecType execType = new ExecType();
		OrdStatus ordStatus = new OrdStatus();
		OrdRejReason ordRejReason = new OrdRejReason();
		Account account = new Account();
		Symbol symbol = new Symbol();
		Side side = new Side();
		OrderQty orderQty = new OrderQty();
		OrdType ordType = new OrdType();
		quickfix.field.Price price = new quickfix.field.Price();
		StopPx stopPx = new StopPx();
		TimeInForce timeInForce = new TimeInForce();
		ExpireTime expireTime = new ExpireTime();
		LastQty lastQty = new LastQty();
		LastPx lastPx = new LastPx();
		LeavesQty leavesQty = new LeavesQty();
		CumQty cumQty = new CumQty();
		AvgPx avgPx = new AvgPx();
		TransactTime transactTime = new TransactTime();
		Text text = new Text();
		try { message.get(orderID); } catch (FieldNotFound e) {}
		try { message.get(origClOrdID); } catch (FieldNotFound e) {}
		try { message.get(clOrdID); } catch (FieldNotFound e) {}
		try { message.get(execID); } catch (FieldNotFound e) {}
		try { message.get(execType); } catch (FieldNotFound e) {}
		try { message.get(ordStatus); } catch (FieldNotFound e) {}
		try { message.get(ordRejReason); } catch (FieldNotFound e) {}
		try { message.get(account); } catch (FieldNotFound e) {}
		try { message.get(symbol); } catch (FieldNotFound e) {}
		try { message.get(side); } catch (FieldNotFound e) {}
		try { message.get(orderQty); } catch (FieldNotFound e) {}
		try { message.get(ordType); } catch (FieldNotFound e) {}
		try { message.get(price); } catch (FieldNotFound e) {}
		try { message.get(stopPx); } catch (FieldNotFound e) {}
		try { message.get(timeInForce); } catch (FieldNotFound e) {}
		try { message.get(expireTime); } catch (FieldNotFound e) {}
		try { message.get(lastQty); } catch (FieldNotFound e) {}
		try { message.get(lastPx); } catch (FieldNotFound e) {}
		try { message.get(leavesQty); } catch (FieldNotFound e) {}
		try { message.get(cumQty); } catch (FieldNotFound e) {}
		try { message.get(avgPx); } catch (FieldNotFound e) {}
		try { message.get(transactTime); } catch (FieldNotFound e) {}
		try { message.get(text); } catch (FieldNotFound e) {}
		Reports reports = new Reports();
		reports.setOrderID(orderID.getValue());
		reports.setOrigClOrdID(origClOrdID.getValue());
		reports.setClOrdID(clOrdID.getValue());
		reports.setExecID(execID.getValue());
		reports.setExecType(execType.getValue());
		reports.setOrdStatus(ordStatus.getValue());
		reports.setOrdRejReason(ordRejReason.getValue());
		reports.setAccount(account.getValue());
		reports.setSymbol(symbol.getValue());
		reports.setSide(side.getValue());
		reports.setOrderQty(orderQty.getValue());
		reports.setOrdType(ordType.getValue());
		reports.setPrice(price.getValue());
		reports.setStopPx(stopPx.getValue());
		reports.setTimeInForce(timeInForce.getValue());
		reports.setExpireTime(expireTime.getValue());
		reports.setLastQty(lastQty.getValue());
		reports.setLastPx(lastPx.getValue());
		reports.setLeavesQty(leavesQty.getValue());
		reports.setCumQty(cumQty.getValue());
		reports.setAvgPx(avgPx.getValue());
		reports.setTransactTime(transactTime.getValue());
		reports.setText(text.getValue());
		try {
			reportsDao.execute(reports);
		} catch (SQLException e) {
			log.error("INSERT ERROR", e);
		}
	}

	@Override
	protected void onMessage(Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
		log.info(message.getClass().getSimpleName() + " : " + message.toString().replace('\u0001', '|'));
	}

	public void close () {
		intervalPrices.finish();
		reportsDao.close();
	}

}
