package jp.commasa.fx.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jp.commasa.fx.dto.Orders;

public class OrdersDao extends AbstractDao {

	private PreparedStatement ps = null;
	private PreparedStatement psOpenPos = null;
	private PreparedStatement psCloseOrder = null;
	private PreparedStatement psCloseAllOrder = null;
	private PreparedStatement psOpenOrder = null;
	private PreparedStatement psClosePos = null;

	@Override
	public void open() {
		try {
			super.init();
			ps = getPreparedStatement("INSERT INTO orders(clordid, account, symbol, side, transacttime, orderqty, ordtype, price, stoppx, timeinforce, expiredate, expiretime, tickno, openclordid) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			psOpenPos = getPreparedStatement("INSERT INTO openposition(clordid, symbol, amount, tickno) VALUES(?,?,?,?)");
			psCloseOrder = getPreparedStatement("SELECT o.clordid, o.account, o.symbol, o.side, o.orderqty FROM openposition p INNER JOIN orders o ON p.clordid = o.clordid WHERE p.symbol = ? AND p.insts < NOW() - INTERVAL 1 MINUTE");
			psCloseAllOrder = getPreparedStatement("SELECT o.clordid, o.account, o.symbol, o.side, o.orderqty FROM openposition p INNER JOIN orders o ON p.clordid = o.clordid");
			psClosePos = getPreparedStatement("DELETE FROM openposition WHERE clordid = ?");
			psOpenOrder = getPreparedStatement("SELECT SUM(amount) amount FROM openposition WHERE symbol = ?");
		} catch (Exception e) {
			log.error("open error: ", e);
		}
	}

	@Override
	public void close() {
		try {
			ps.close();
			psOpenPos.close();
			psCloseOrder.close();
			psCloseAllOrder.close();
			psClosePos.close();
			psOpenOrder.close();
			super.closeConnection();
		} catch (Exception e) {
			log.warn("close error: ", e);
		}
	}

	public void execute(Orders orders, String ClOrdID) throws SQLException {
		ps.clearParameters();
		ps.setString(1, orders.getClOrdID());
		ps.setString(2, orders.getAccount());
		ps.setString(3, orders.getSymbol());
		ps.setString(4, orders.getSide());
		ps.setDate(5, orders.getTransactTime());
		if (orders.getOrderQty() == null) ps.setNull(6, java.sql.Types.DOUBLE); else ps.setDouble(6, orders.getOrderQty());
		ps.setString(7, orders.getOrdType());
		if (orders.getPrice() == null) ps.setNull(8, java.sql.Types.DOUBLE); else ps.setDouble(8, orders.getPrice());
		if (orders.getStopPx() == null) ps.setNull(9, java.sql.Types.DOUBLE); else ps.setDouble(9, orders.getStopPx());
		ps.setString(10, orders.getTimeInForce());
		ps.setDate(11, orders.getExpireDate());
		ps.setDate(12, orders.getExpireTime());
		ps.setBigDecimal(13, orders.getTickNo());
		ps.setString(14, ClOrdID);
		log.trace("executeUpdate: orders="+orders.toString());
		super.executeUpdate(ps);
		if (ClOrdID==null) {
			double side = 1;
			if ("2".equals(orders.getSide())) side = -1;
			psOpenPos.clearParameters();
			psOpenPos.setString(1, orders.getClOrdID());
			psOpenPos.setString(2, orders.getSymbol());
			if (orders.getOrderQty() == null) psOpenPos.setDouble(3, 0); else psOpenPos.setDouble(3, orders.getOrderQty() * side);
			psOpenPos.setBigDecimal(4, orders.getTickNo());
			super.executeUpdate(psOpenPos);
		} else {
			psClosePos.clearParameters();
			psClosePos.setString(1, ClOrdID);
			super.executeUpdate(psClosePos);
		}
	}

	public List<Orders> getCloseOrder(String symbol) throws SQLException {
		List<Orders> result = new ArrayList<Orders>();
		psCloseOrder.clearParameters();
		psCloseOrder.setString(1, symbol);
		ResultSet rs = psCloseOrder.executeQuery();
		while (rs.next()) {
			Orders orders = new Orders();
			orders.setClOrdID(rs.getString(1));
			orders.setAccount(rs.getString(2));
			orders.setSymbol(rs.getString(3));
			orders.setSide(rs.getString(4));
			orders.setOrderQty(rs.getDouble(5));
			result.add(orders);
		}
		return result;
	}

	public List<Orders> getCloseAllOrder() throws SQLException {
		List<Orders> result = new ArrayList<Orders>();
		ResultSet rs = psCloseAllOrder.executeQuery();
		while (rs.next()) {
			Orders orders = new Orders();
			orders.setClOrdID(rs.getString(1));
			orders.setAccount(rs.getString(2));
			orders.setSymbol(rs.getString(3));
			orders.setSide(rs.getString(4));
			orders.setOrderQty(rs.getDouble(5));
			result.add(orders);
		}
		return result;
	}

	public double getOpenOrder(String symbol) throws SQLException {
		double result = 0;
		psOpenOrder.clearParameters();
		psOpenOrder.setString(1, symbol);
		ResultSet rs = psOpenOrder.executeQuery();
		while (rs.next()) {
			result = rs.getDouble(1);
		}
		return result;
	}

}
