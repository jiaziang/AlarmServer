package com.jiaziang8.alarm.Servlet;

import java.io.File;
import java.io.FileOutputStream;
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

import com.jiaziang8.alarmServer.util.Constants;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;

@WebServlet("/UploadImgServlet")
public class UploadImgServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;

	public UploadImgServlet() {
		super();
		System.out.println("UploadImg~~~~~~~~~");
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html");
		String photo = request.getParameter("photo");
		String account = request.getParameter("account");
		con = null;
		statement = null;
		try {
			// 对base64数据进行解码
			getConnection();
			byte[] decode = Base64.decode(photo);
			File savePath = new File(
					Constants.HEAD_IMAGE_PATH);
			if (!savePath.exists()) {
				savePath.mkdirs();
			}
			File file = File.createTempFile(account, ".jpg", savePath);
			String FinalSavePath = file.getAbsolutePath();
			FileOutputStream out = new FileOutputStream(file);
			out.write(decode);
			out.flush();
			out.close();
			
			ResultSet set = statement.executeQuery("select *from user where account='"+account+"'");
			set.beforeFirst();
			if(set.next()){
				String oldHeadPath = set.getString("headpath");
				File oldHeadFile = new File(oldHeadPath);
				if(oldHeadFile.exists()){
					oldHeadFile.delete();
				}
			}
			
			
			String updateSql = "update user set headpath=? where account=?";
			PreparedStatement pStatement = con.prepareStatement(updateSql);
			pStatement.setString(1, FinalSavePath);
			pStatement.setString(2,account);
			pStatement.executeUpdate();
			
			statement.close();
			statement = null;
			con.close();
			con = null;
		} catch (Base64DecodingException e) {
			e.printStackTrace();
		}catch (SQLException e) {
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

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);

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
