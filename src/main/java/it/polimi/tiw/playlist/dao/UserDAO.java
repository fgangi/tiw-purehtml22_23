package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
	
	private Connection con;
	
	public UserDAO(Connection c) {
		this.con = c;
	}
	
	// method that verifies if the given userName is already taken
	private boolean taken(String userName) throws SQLException {
		boolean result = false;
		String query = "SELECT * FROM USER WHERE UserName = ?";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1 , userName);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) result = true;
		}catch(SQLException e) {
			throw new SQLException(e);
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	//method that returns the User(if exist) after the authentication
	public boolean authentication(String userName, String password) throws SQLException {
		boolean result = false;
		String query ="SELECT * FROM USER WHERE UserName = ? AND Password = ?";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try{			
			pStatement = con.prepareStatement(query);
			pStatement.setString(1 , userName);
			pStatement.setString(2 , password);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) {
				result = true;
			}	
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
	
	// method that creates a new User after registration
	public boolean registration(String userName, String password) throws SQLException {
		boolean result = false;
		String query = "INSERT into USER (UserName,Password) VALUES(?,?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1 , userName);
			pStatement.setString(2 , password);
			if(!this.taken(userName)) {
				pStatement.executeUpdate();
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException(e);
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return result;
	}
	
}