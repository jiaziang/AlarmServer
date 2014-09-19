package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.jiaziang8.alarmServer.object.savedAlarmObject;
import com.jiaziang8.alarmServer.util.Constants;

@WebServlet("/NotificationServlet")
public class NotificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	Gson gson;

	public NotificationServlet() {
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
		String accoutString = request.getParameter("account");
		String resultString;
		con = null;
		statement = null;
		ResultSet rs = null;
		try {
			getConnection();
			rs = statement
					.executeQuery("select *from savedAlarm where friend='"
							+ accoutString + "'");
			rs.beforeFirst();
			int id;
			String account;
			String friend;
			int mDays;
			int mHour;
			int mMinute;
			String filename;
			String wordsToSay;
			String time;
			Timestamp timestamp;
			int isAccept;

			List<savedAlarmObject> notificationList = new ArrayList<savedAlarmObject>();
			DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:MM");
			while (rs.next()) {
				isAccept = rs.getInt(10);
				if (isAccept == 0) {
					id = rs.getInt(1);
					account = rs.getString(2);
					friend = rs.getString(3);
					mDays = rs.getInt(4);
					mHour = rs.getInt(5);
					mMinute = rs.getInt(6);
					filename = rs.getString(7);
					wordsToSay = rs.getString(8);
					timestamp = rs.getTimestamp(9);
					time = format.format(timestamp);
					Statement statement2 = con.createStatement();
					String path = "";
					ResultSet rSet2 = statement2.executeQuery("select *from user where account='"+account+"'");
					rSet2.beforeFirst();
					if(rSet2.next()){
						path = rSet2.getString("headpath");
					}
					

					savedAlarmObject savedAlarmObject = new savedAlarmObject(
							account, friend, mDays, mHour, mMinute, filename,
							wordsToSay, time,id,path);
					notificationList.add(savedAlarmObject);
					rSet2.close();
				}
			}
			
			resultString = gson.toJson(notificationList);
			OutputStream outputStream = response.getOutputStream();
			outputStream.write(resultString.getBytes("utf-8"));
			outputStream.flush();
			outputStream.close();
			response.setStatus(200);
			
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			con.close();
			con = null;

		} catch (Exception e) {
			response.setStatus(201);
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
