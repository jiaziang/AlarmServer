package com.jiaziang8.alarm.Servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.PushPayload.Builder;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.Notification;

import com.google.gson.Gson;
import com.jiaziang8.alarmServer.object.AddFriendMessage;
import com.jiaziang8.alarmServer.object.PushMessage;
import com.jiaziang8.alarmServer.util.Constants;

@WebServlet("/DownLoadServlet")
public class DownLoadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	private Gson gson;

	public DownLoadServlet() {
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

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String filename = request.getParameter("filename");
		String idString = request.getParameter("id");
		con = null;
		statement = null;
		
		System.out.println("filename"+filename+"  id:"+idString);
		int id = Integer.parseInt(idString);
		File file = new File(filename);
		if (file.exists()) {
			String filenameString = URLEncoder.encode(file.getName(), "utf-8");
			response.reset();
			response.setContentType("application/x-msdownload");
			response.addHeader("Content-Disposition", "attachment; filename=\""
					+ filenameString + "\"");
			int fileLength = (int) file.length();
			response.setContentLength(fileLength);
			if (fileLength != 0) {
				/* 创建输入流 */
				InputStream inStream = new FileInputStream(file);
				byte[] buf = new byte[4096];
				/* 创建输出流 */
				ServletOutputStream servletOS = response.getOutputStream();
				int readLength;
				while (((readLength = inStream.read(buf)) != -1)) {
					servletOS.write(buf, 0, readLength);
				}
				inStream.close();
				servletOS.flush();
				servletOS.close();
			}
		}
		try {
			getConnection();
			String chagesql = "update savedAlarm set isAccept=1 where id="+id;
			statement.executeUpdate(chagesql);
			String findUserSql = "select *from savedAlarm where id="+id;
			ResultSet rSet = statement.executeQuery(findUserSql);
			rSet.beforeFirst();
			if(rSet.next()){
				String account = rSet.getString("account");
				String friend = rSet.getString("friend");
				acceptAlarmNotification(account,friend);
			}
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
	
	private void acceptAlarmNotification(String account,String friend){
		AddFriendMessage addFriendMessage = new AddFriendMessage(account, friend);
		String messageString = gson.toJson(addFriendMessage);
		PushMessage pushMessage = new PushMessage("accept_alarm", messageString);
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
				.setAlert("好友接受了你的闹钟，点击查看详情").build();
		Builder builder = new Builder().setAudience(audience)
				.setPlatform(platform).setNotification(notification);
		PushPayload pushPayload = builder.build();
		PushResult pushResult = jPushClient.sendPush(pushPayload);
		int errorcode = pushResult.getErrorCode();
		System.out.println("acceptPush pushResultCode is :" + errorcode);
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
