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

@WebServlet("/ReallyAddFriendServlet")
public class ReallyAddFriendServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	private Gson gson;

	public ReallyAddFriendServlet() {
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
		ResultSet rs = null;
		//System.out.println("user is"+user+"and friend is"+friend);
		//去掉+86
		if (friend.indexOf("+86") == 0) {
			friend = friend.substring(3);
		}
		try {
			getConnection();
			rs = statement
					.executeQuery("select *from user where account=" + user);
			rs.beforeFirst();
			if (rs.next()) {
				//获取friends字段
				String friend_list = rs.getString("friends");
				//若原来有好友
				if (!friend_list.equals("")) {
					String new_friend = friend_list + " " + friend;
					String sql = "update user set friends=? where account=?";
					java.sql.PreparedStatement pStatement0 = con
							.prepareStatement(sql);
					pStatement0.setString(1, new_friend);
					pStatement0.setString(2, user);
					pStatement0.executeUpdate();
				} else {
					String sql2 = "update user set friends=? where account=?";
					java.sql.PreparedStatement pStatement = con
							.prepareStatement(sql2);
					//System.out.println("friends is"+friend+"and account ="+user);
					pStatement.setString(1, friend);
					pStatement.setString(2, user);
					pStatement.executeUpdate();
				}
			}
			//添加好友成功后删除SavedAlarm表内通知的数据
			String deleteAddRecord ="delete from savedAlarm where account=? and friend=? and mDays=-1";
			java.sql.PreparedStatement pStatement1 = con.prepareStatement(deleteAddRecord);
			pStatement1.setString(1, user);
			pStatement1.setString(2, friend);
			pStatement1.executeUpdate();
			//相互删除
			String deleteAddRecord2 ="delete from savedAlarm where friend=? and account=? and mDays=-1";
			java.sql.PreparedStatement pStatement2 = con.prepareStatement(deleteAddRecord2);
			pStatement2.setString(1, user);
			pStatement2.setString(2, friend);
			pStatement2.executeUpdate();
			
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			con.close();
			con = null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
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
