package jp.commasa.fx.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import jp.commasa.fx.dto.Reports;

public class ReportsDao extends AbstractDao {

	private PreparedStatement ps = null;

	@Override
	public void open() {
		try {
			super.init();
			ps = getPreparedStatement("INSERT INTO reports(orderid, origclordid, clordid, execid, exectype, ordstatus, ordrejreason, account, symbol, side, orderqty, ordtype, price, stoppx, timeinforce, expiretime, lastqty, lastpx, leavesqty, cumqty, avgpx, transacttime, cxlrejresponseto, cxlrejreason, text) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		} catch (Exception e) {
			log.error("open error: ", e);
		}
	}

	@Override
	public void close() {
		try {
			ps.close();
			super.closeConnection();
		} catch (Exception e) {
			log.warn("close error: ", e);
		}
	}

	public void execute(Reports reports) throws SQLException {
		ps.clearParameters();
		ps.setString( 1, reports.getOrderID());
		ps.setString( 2, reports.getOrigClOrdID());
		ps.setString( 3, reports.getClOrdID());
		ps.setString( 4, reports.getExecID());
		ps.setString( 5, reports.getExecType());
		ps.setString( 6, reports.getOrdStatus());
		ps.setInt( 7, reports.getOrdRejReason());
		ps.setString( 8, reports.getAccount());
		ps.setString( 9, reports.getSymbol());
		ps.setString(10, reports.getSide());
		ps.setDouble(11, reports.getOrderQty());
		ps.setString(12, reports.getOrdType());
		ps.setDouble(13, reports.getPrice());
		ps.setDouble(14, reports.getStopPx());
		ps.setString(15, reports.getTimeInForce());
		ps.setDate(16, reports.getExpireTime());
		ps.setDouble(17, reports.getLastQty());
		ps.setDouble(18, reports.getLastPx());
		ps.setDouble(19, reports.getLeavesQty());
		ps.setDouble(20, reports.getCumQty());
		ps.setDouble(21, reports.getAvgPx());
		ps.setDate(22, reports.getTransactTime());
		ps.setString(23, reports.getCxlRejResponseTo());
		ps.setInt(24, reports.getCxlRejReason());
		ps.setString(25, reports.getText());
		log.trace("executeUpdate: reports="+reports.toString());
		super.executeUpdate(ps);
	}

}
