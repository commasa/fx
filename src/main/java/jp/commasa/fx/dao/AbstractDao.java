package jp.commasa.fx.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractDao {

	protected Logger log = LogManager.getLogger(this.getClass());
	private String url = "";
	private String user = "";
	private String pass = "";
	protected Connection conn = null;

	protected void init() throws ClassNotFoundException, SQLException {
		ResourceBundle bundle = ResourceBundle.getBundle("db");
		String url = bundle.getString("url");
		String user = bundle.getString("user");
		String pass = bundle.getString("pass");
		this.url = url;
		this.user = user;
		this.pass = pass;
	}

	protected void closeConnection() throws SQLException {
		if ( conn != null ) conn.close();
	}

	protected Connection getConnection() throws ClassNotFoundException, SQLException {
		if (conn == null || (conn != null && conn.isClosed())) {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(url, user, pass);
			log.debug("getConnection(url="+url+",user="+user+")");
			return conn;
		} else {
			return conn;
		}
	}

	protected PreparedStatement getPreparedStatement(String sql) throws ClassNotFoundException, SQLException {
		if (conn == null || conn.isClosed()) {
			conn = getConnection();
		}
		return conn.prepareStatement(sql);
	}

	protected void executeUpdate(PreparedStatement ps) throws SQLException {
		ps.executeUpdate();
		if (!conn.getAutoCommit()) conn.commit();
	}

	public abstract void open();

	public abstract void close();

}
