package jp.commasa.fx.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import jp.commasa.fx.dto.IntervalPriceResult;

public class IntervalPriceResultDao extends AbstractDao {

	private PreparedStatement ps = null;

	@Override
	public void open() {
		try {
			super.init();
			ps = getPreparedStatement("INSERT INTO interval_model(tradedate,symbol,last,model,volatility,buypl,sellpl,basebein,baseend) "
					+ "VALUES(?,?,?,?,?,?,?,?,?)");
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

	public void executeUpdate(String tradeDate, IntervalPriceResult result) throws SQLException {
		ps.clearParameters();
		ps.setString(1, tradeDate);
		ps.setString(2, result.getSymbol());
		ps.setTimestamp(3, new java.sql.Timestamp(result.getLast().getTime()));
		ps.setString(4, result.getModel());
		ps.setDouble(5, result.getVolatility());
		ps.setDouble(6, result.getBuyPL());
		ps.setDouble(7, result.getSellPL());
		ps.setTimestamp(8, new java.sql.Timestamp(result.getBaseBegin().getTime()));
		ps.setTimestamp(9, new java.sql.Timestamp(result.getBaseEnd().getTime()));
		log.trace("executeUpdate(ipModel): "+result.toString());
		super.executeUpdate(ps);
	}

}
