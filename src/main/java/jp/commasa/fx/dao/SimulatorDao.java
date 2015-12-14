package jp.commasa.fx.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import jp.commasa.fx.dto.Condition;

@Deprecated
public class SimulatorDao extends AbstractDao {

	private PreparedStatement ps = null;
	private PreparedStatement psRatio = null;

	@Override
	public void open() {
		try {
			super.init();
			ps = getPreparedStatement("INSERT INTO model(symbol,premise,result,total) VALUES(?,?,?,1) ON DUPLICATE KEY UPDATE total=total+1");
			psRatio = getPreparedStatement("select m1.result, m2.total total, m1.total/m2.total ratio from model m1 "
					+ "inner join (select sum(total) total from model where symbol=? and premise=?) m2 "
					+ "where m1.symbol=? and m1.premise=?");
		} catch (Exception e) {
			log.error("open error: ", e);
		}
	}

	@Override
	public void close() {
		try {
			ps.close();
			psRatio.close();
			super.closeConnection();
		} catch (Exception e) {
			log.warn("close error: ", e);
		}
	}

	public void executeUpdate(String symbol, String premise, String result) throws SQLException {
		ps.clearParameters();
		ps.setString(1, symbol);
		ps.setString(2, premise);
		ps.setString(3, result);
		log.trace("executeUpdate(psModel): symbol="+symbol+",premise="+premise+",result="+result);
		super.executeUpdate(ps);
	}

	public Map<String, Condition> executeQuery(String symbol, String premise) throws SQLException {
		psRatio.clearParameters();
		psRatio.setString(1, symbol);
		psRatio.setString(2, premise);
		psRatio.setString(3, symbol);
		psRatio.setString(4, premise);
		ResultSet rs = psRatio.executeQuery();
		Map<String, Condition> result = new HashMap<String, Condition>();
		while (rs.next()) {
			Condition c = new Condition();
			c.setResult(rs.getString(1));
			c.setTotal(rs.getDouble(2));
			c.setRatio(rs.getDouble(3));
			result.put(c.getResult(), c);
		}
		rs.close();
		return result;
	}

}
