package com.jiaziang8.alarm.Servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

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

@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Gson gson;

	public UploadServlet() {
		super();
		System.out.println("UploadServlet~");
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
		String account = "";
		String friend = "";
		con = null;
		int mDays = 0;
		int mHour = 0;
		int mMinute = 0;
		long alarmtime = 0;
		String wordToSay = "";
		System.out.println("Start to upload~~~~~");
		try {
			getConnection();
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=UTF-8");
			PrintWriter out = response.getWriter();

			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setHeaderEncoding("utf-8");
			java.util.List<FileItem> items = upload.parseRequest(request);

			// 音频储存路径
			String uploadPath = Constants.AUDIO_FILE_PATH;
			File file = new File(uploadPath);
			if (!file.exists()) { // 不存在此路径则新建文件夹
				file.mkdir();
			}
			String filename = "";
			InputStream is = null;
			for (FileItem item : items) {
				System.out.println("getFieldName:" + item.getFieldName()
						+ "  getName:" + item.getName() + " getString"
						+ item.getString());
				// getFieldName:fiel1 getName:文件名 :getString:文件内容
				if (item.isFormField()) {
					if (item.getFieldName().equals("file1")) { // 标志为file1
						if (!item.getString().equals(""))
							filename = item.getString("utf-8");
					}
				} else if (item.getName() != null // 有文件名且文件名不为空字符串
						&& !item.getName().equals("")) {
					String messageString = item.getName();
					System.out.println("messageString is" + messageString);
					String[] messageStrings = new String[8];
					messageStrings = messageString.split("_");

					account = messageStrings[1];
					friend = messageStrings[2];
					if (friend.indexOf("+86") == 0) {
						friend = friend.substring(3);
					}
					mDays = Integer.parseInt(messageStrings[3]);
					mHour = Integer.parseInt(messageStrings[4]);
					mMinute = Integer.parseInt(messageStrings[5]);
					wordToSay = messageStrings[6];
					alarmtime = Long.parseLong(messageStrings[7]);
					System.out.println("account" + account + "friend" + friend
							+ "mDays" + mDays + "mHour" + mHour + "mMinute"
							+ mMinute + "word:" + wordToSay);
					filename = messageStrings[0].substring( // 获取文件名
							item.getName().lastIndexOf("\\") + 1);
					is = item.getInputStream(); // 获取文件输入流
				}
			}
			filename = uploadPath + filename; // 文件最终储存路径
			if (new File(filename).exists()) { // 已经存在则删除原文件
				new File(filename).delete();
			}
			if (!filename.equals("")) {
				FileOutputStream fos = new FileOutputStream(filename);
				byte[] buffer = new byte[8192];
				int count = 0;
				// System.out.println();
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count); // 向服务端文件写入字节流
				}
				fos.close(); // 关闭FileOutputStream对象
				is.close(); // InputStream对象
				String insertAlarmString = "insert into savedAlarm(account,friend,mDays,mHour,mMinute,filename,wordsToSay,isAccept,alarmtime) values(?,?,?,?,?,?,?,?,?)";
				PreparedStatement preparedStatement = con
						.prepareStatement(insertAlarmString);
				preparedStatement.setString(1, account);
				preparedStatement.setString(2, friend);
				preparedStatement.setInt(3, mDays);
				preparedStatement.setInt(4, mHour);
				preparedStatement.setInt(5, mMinute);
				preparedStatement.setString(6, filename);
				preparedStatement.setString(7, wordToSay);
				preparedStatement.setInt(8, 0);
				preparedStatement.setLong(9, alarmtime);
				preparedStatement.executeUpdate();
				
				preparedStatement.close();

				pushAlarmMessage(user, friend);
				out.println("文件上传成功!");
			}
			con.close();
			con = null;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(con!=null){
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}			
		}
	}

	private void pushAlarmMessage(String user, String friend) {
		AddFriendMessage addFriendMessage = new AddFriendMessage(user, friend);
		String messageString = gson.toJson(addFriendMessage);
		PushMessage pushMessage = new PushMessage("add_alarm", messageString);
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
				.setAlert("你有新的闹钟啦").build();

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
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

}
