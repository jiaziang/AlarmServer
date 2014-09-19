package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
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

@WebServlet("/ReviewGetItemsServlet")
public class ReviewGetItemsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	Gson gson;
	private static int ITEMSCOUNT = 10;

    public ReviewGetItemsServlet() {
        super();
        System.out.println("Review啊混蛋～～～～～～～～`");
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
		String startString = request.getParameter("start");
		String accoutString = request.getParameter("account");
		con = null;
		statement = null;
		ResultSet rs = null;
		int start = Integer.parseInt(startString);
		String resultString;
		int returnCount = 0;
		
		try {
			getConnection();
			rs = statement
					.executeQuery("select *from savedAlarm where (friend='"
							+ accoutString + "' or account='"+accoutString+"') and isAccept=1 and mDays<>-1");
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
			long alarmtime;
			
			List<savedAlarmObject> notificationList = new ArrayList<savedAlarmObject>();
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			
			while(rs.next()){
					id = rs.getInt(1);
					account = rs.getString(2);
					friend = rs.getString(3);
					mDays = rs.getInt(4);
					mHour = rs.getInt(5);
					mMinute = rs.getInt(6);
					filename = rs.getString(7);
					wordsToSay = rs.getString(8);
					timestamp = rs.getTimestamp(9);
					isAccept = rs.getInt(10);
					alarmtime = (long)rs.getObject(11);
					time = format.format(timestamp);
					if(accoutString.equals(friend)&&System.currentTimeMillis()<alarmtime){
						continue;
					}

					savedAlarmObject savedAlarmObject = new savedAlarmObject(
							account, friend, mDays, mHour, mMinute, filename,
							wordsToSay, time,id,alarmtime);
					notificationList.add(savedAlarmObject);					
					System.out.println("id~~~~~~~~~~~~"+id+"  and start is:"+start);
			}
			if(notificationList.size()>start){
				if(notificationList.size()>start+ITEMSCOUNT){
					notificationList = notificationList.subList(start, start+ITEMSCOUNT);
				}else{
					notificationList = notificationList.subList(start, notificationList.size());
				}				
				System.out.println("size is :"+notificationList.size());
				resultString = gson.toJson(notificationList);
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(resultString.getBytes("utf-8"));
				outputStream.flush();
				outputStream.close();
				response.setStatus(200);
							
			}else{
				response.setStatus(201);
			}
				
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			con.close();
			con = null;
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(202);
		}	finally{
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
