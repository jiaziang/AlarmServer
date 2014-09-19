package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.jiaziang8.alarmServer.object.AddFriendMessage;
import com.jiaziang8.alarmServer.object.PushMessage;
import com.jiaziang8.alarmServer.util.Constants;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.Notification;

@WebServlet("/AddFriendServlet")
public class AddFriendServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	private Gson gson;

	public AddFriendServlet() {
		super();
		System.out.println("ADD~~~~~~~~~~~~~~~");
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
				//获取friends字段  friend_list
				String friend_list = rs.getString("friends");
				boolean is_exited = false;
				//原来有好友
				if (!friend_list.equals("")) {
					String[] friendStrings = friend_list.split(" ");
					// 判断是否已存在该好友
					for (String onefriend : friendStrings) {
						System.out.println(onefriend);
						if (onefriend.equals(friend)) {
							is_exited = true;
							break;
						}
					}
					if (!is_exited) {
						// 若该人不是好友，则添加好友
						pushAddFriend(user, friend);
						addToSavedAlarm(user, friend);
						response.setStatus(200);
					} else
						//该好友已存在
						response.setStatus(202);
				} 
				// 原来没有好友，添加好友
				else { 
					pushAddFriend(user, friend);
					addToSavedAlarm(user, friend);
					response.setStatus(200);
				}
			} else
				response.setStatus(201);
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			con.close();
			con = null;
		} catch (SQLException e) {
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

	//将好友请求添加进入SavesAlarm数据表
	private void addToSavedAlarm(String user, String friend) {
		try {
			String check_friend_notificationsql = "select *from savedAlarm where account=? and friend=? and mDays=?";
			PreparedStatement pStatement2 = con
					.prepareStatement(check_friend_notificationsql);
			pStatement2.setString(1, user);
			pStatement2.setString(2, friend);
			pStatement2.setInt(3, -1);
			ResultSet rs2 = pStatement2.executeQuery();
			rs2.beforeFirst();
			//如果之前不存在相同的未接受的请求
			if (!rs2.next()) {
				String addnotificationsql = "insert into savedAlarm(account,friend,mDays) values(?,?,?)";
				PreparedStatement pStatement = con
						.prepareStatement(addnotificationsql);
				pStatement.setString(1, user);
				pStatement.setString(2, friend);
				pStatement.setInt(3, -1);
				pStatement.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void pushAddFriend(String user, String friend) {

		AddFriendMessage addFriendMessage = new AddFriendMessage(user, friend);
		String messageString = gson.toJson(addFriendMessage);

		PushMessage pushMessage = new PushMessage("add_friend", messageString);
		String pushMessageString = gson.toJson(pushMessage);

		JPushClient jPushClient = new JPushClient(Constants.masterSecret,
				Constants.appKey, false, 86400);

		Audience audience = Audience.alias(friend);
		Platform platform = Platform.android();

		AndroidNotification androidNotification = AndroidNotification
				.newBuilder().addExtra("pushMessage", pushMessageString)
				.build();
		Notification notification = Notification.newBuilder()
				.addPlatformNotification(androidNotification)
				.setAlert("添加好友邀请").build();

		Builder builder = new Builder().setAudience(audience)
				.setPlatform(platform).setNotification(notification);
		PushPayload pushPayload = builder.build();

		PushResult pushResult = jPushClient.sendPush(pushPayload);
		int errorcode = pushResult.getErrorCode();
		System.out.println("pushResultCode is :" + errorcode);
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
