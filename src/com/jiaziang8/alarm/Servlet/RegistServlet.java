package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.jiaziang8.alarmServer.util.Constants;


@WebServlet("/RegistServlet")
public class RegistServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	private Statement statement2;

	public RegistServlet() {
		super();
		System.out.println("Regist Start!!!!!!!!!!!!!!!!!~~~~");
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
		String username = request.getParameter("username");
		String userpwd = request.getParameter("userpwd");
		con = null;
		statement = null;
		statement2 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		if(username.indexOf("+86")==0){
			username = username.substring(3);
		}

		try {
			getConnection();
			rs = statement
					.executeQuery("select * from user where account='"
							+ username + "'");
			rs.beforeFirst();
			if (rs.next()) {
				response.setStatus(202);
			} else {
				rs2 = statement2
						.executeQuery("select *from checkuser where account="
								+ username);
				rs2.beforeFirst();
				if (rs2.next()) {
					int checknumber = getRandom();
					String sql2 = "update checkuser set checknumber=? where account=?";
					java.sql.PreparedStatement pStatement = con
							.prepareStatement(sql2);
					pStatement.setInt(1, checknumber);
					pStatement.setString(2, username);
					pStatement.executeUpdate();
					sendCheckNumber(checknumber,username);
				} else {
					int checknumber = getRandom();
					
					String sql3 = "insert into checkuser(account,password,checknumber) values('"
							+ username + "','" + userpwd + "'," + checknumber + ")";
					int count = statement.executeUpdate(sql3);
					sendCheckNumber(checknumber,username);
				}
				response.setStatus(201);
			}
			
			rs.close();
			rs = null;
			rs2.close();
			rs2 = null;
			statement.close();
			statement = null;
			statement2.close();
			statement2 = null;
			con.close();
			con = null;


		} catch (SQLException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(rs!=null){
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(rs2!=null){
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
			
			if(statement2!=null){
				try {
					statement2.close();
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

	private static void sendCheckNumber(int checknumber,String userphone) throws Exception {
		HttpClient client = new HttpClient();
		PostMethod post = new PostMethod("http://utf8.sms.webchinese.cn");
		post.addRequestHeader("Content-Type",
				"application/x-www-form-urlencoded;charset=utf8");// 在头文件中设置转码
		NameValuePair[] data = { new NameValuePair("Uid", "jiaziang8"),
				new NameValuePair("Key", "8b2bbcec425405517052"),
				new NameValuePair("smsMob", userphone),
				new NameValuePair("smsText", "您的注册验证码是" + checknumber + ".") };
		post.setRequestBody(data);

		client.executeMethod(post);
		Header[] headers = post.getResponseHeaders();
		int statusCode = post.getStatusCode();
		System.out.println("statusCode:" + statusCode);
		for (Header h : headers) {
			System.out.println(h.toString());
		}
		String result = new String(post.getResponseBodyAsString().getBytes(
				"utf8"));
		System.out.println("result" + result);

		post.releaseConnection();
	}

	public int getRandom() {
		Long seed = System.currentTimeMillis();// 获得系统时间，作为生成随机数的种子
		Random random = new Random(seed);// 调用种子生成随机数

		return random.nextInt(10000);
	}

	@Override
	public void destroy() {
		super.destroy();
	}
	
	public void getConnection(){
		try {
			con = DriverManager.getConnection(CONNECT, user, password);
			statement = con.createStatement();
			statement2 = con.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}

}
