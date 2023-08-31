package it.polimi.tiw.playlist.utils;

import javax.servlet.*;
import java.sql.*;

public class ConnectionHandler {
	public static Connection getConnection(ServletContext context) throws UnavailableException {
		Connection result;
		String driver = context.getInitParameter("dbDriver");
		String url = context.getInitParameter("dbUrl");
		String user = context.getInitParameter("dbUser");
		String password = context.getInitParameter("dbPassword");
		try {
			Class.forName(driver);
			result = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load db driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't connect");
		}
		return result;
	}
	
	public static void destroy(Connection connection) {
	      try {
	        if (connection != null){
	            connection.close();
	        }
	      } catch (SQLException sqle) {}
  }
}

