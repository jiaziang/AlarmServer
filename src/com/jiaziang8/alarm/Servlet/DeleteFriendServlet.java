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

@WebServlet("/DeleteFriendServlet")
public class DeleteFriendServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	private Gson gson;

       
    public DeleteFriendServlet() {
        super();
		gson = new Gson();
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String user = request.getParameter("user");
		String friend = request.getParameter("friend");
		System.out.println(user+"要删"+friend+"啦啦啦");
		con = null;
		statement = null;
		ResultSet rs = null;
		
		if (friend.indexOf("+86") == 0) {
			friend = friend.substring(3);
		}
		try {
			getConnection();
			rs = statement.executeQuery("select *from user where account='"+user+"'");
			rs.beforeFirst();
			if(rs.next()){
				//原来的friends字段
				String friend_list = rs.getString("friends");
				int friendlength = friend.length();
				int startIndex = friend_list.indexOf(friend);
				//存在要删除的好友
				if(startIndex!=-1){
					if(friend_list.equals(friend)){
						friend_list = "";
					}
					else if(startIndex >0){
						friend_list = friend_list.substring(0, startIndex-1) + friend_list.substring(startIndex+friendlength);
						System.out.println("friend_list"+friend_list+"aaaa");
					}
					else{
						friend_list = friend_list.substring(friendlength+1);
						System.out.println("friend_list"+friend_list+"bbbb");
					}
					//friend_list = friend_list.substring(0, startIndex)+friend_list.substring(startIndex+friendlength);
					String sql3 = "update user set friends=? where account=?";
					java.sql.PreparedStatement pStatement = con
							.prepareStatement(sql3);
					pStatement.setString(1, friend_list);
					pStatement.setString(2, user);
					pStatement.executeUpdate();
				}				
			}
			
			rs = statement.executeQuery("select *from user where account='"+friend+"'");
			rs.beforeFirst();
			if(rs.next()){
				String friend_list = rs.getString("friends");
				int friendlength = user.length();
				int startIndex = friend_list.indexOf(user);
				if(startIndex!=-1){
					if(friend_list.equals(user)){
						friend_list = "";
					}
					else if(startIndex >0){
						friend_list = friend_list.substring(0, startIndex-1) + friend_list.substring(startIndex+friendlength);
						System.out.println("friend_list"+friend_list+"aaaa");
					}
					else{
						friend_list = friend_list.substring(friendlength+1);
						System.out.println("friend_list"+friend_list+"bbbb");
					}
					//friend_list = friend_list.substring(0, startIndex)+friend_list.substring(startIndex+friendlength);
					String sql3 = "update user set friends=? where account=?";
					java.sql.PreparedStatement pStatement = con
							.prepareStatement(sql3);
					pStatement.setString(1, friend_list);
					pStatement.setString(2, friend);
					pStatement.executeUpdate();
				}	
			}		
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
			statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
}
