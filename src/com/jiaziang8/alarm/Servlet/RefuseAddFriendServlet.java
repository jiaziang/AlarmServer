package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.jiaziang8.alarmServer.util.Constants;

@WebServlet("/RefuseAddFriendServlet")
public class RefuseAddFriendServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	private Gson gson;

	public RefuseAddFriendServlet() {
		super();
		gson = new Gson();
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String user = request.getParameter("user");
		String friend = request.getParameter("friend");
		con = null;
		statement = null;
		if (friend.indexOf("+86") == 0) {
			friend = friend.substring(3);
		}
		try {
			getConnection();
			String deleteAddRecord = "delete from savedAlarm where account=? and friend=? and mDays=-1";
			java.sql.PreparedStatement pStatement1 = con
					.prepareStatement(deleteAddRecord);
			pStatement1.setString(1, user);
			pStatement1.setString(2, friend);
			pStatement1.executeUpdate();
			
			statement.close();
			statement = null;
			con.close();
			con = null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
			if(statement!=null){
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
		}

	}
	
	public void getConnection(){
		try {
			con = DriverManager.getConnection(CONNECT, user, password);
			statement = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	

}
