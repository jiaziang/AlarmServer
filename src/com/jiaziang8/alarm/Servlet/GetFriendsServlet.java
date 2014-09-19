package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
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

@WebServlet("/GetFriendsServlet")
public class GetFriendsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
	Gson gson;
	
    public GetFriendsServlet() {
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
		String accountNumber = request.getParameter("account");
		con = null;
		statement = null;
		ResultSet rs = null;
		
		try {
			getConnection();
			rs = statement.executeQuery("select *from user where account='"+accountNumber+"'");
			rs.beforeFirst();
			if(rs.next()){
				response.setStatus(200);
				String friendString = rs.getString("friends");
				String reallyFriend ="";
				if(!friendString.equals("")){
					String[] friendStrings = friendString.split(" ");
					for(String friend:friendStrings){
						String path = "";
						Statement statement2 = con.createStatement();
						ResultSet rs2 = statement2.executeQuery("select *from user where account='"+friend+"'");
						rs2.beforeFirst();
						if(rs2.next()){
							path = rs2.getString("headpath");
						}
						reallyFriend+=friend+"@"+path+" ";
					}
					reallyFriend = reallyFriend.trim();
				}
				System.out.println("aaaaaaaaaaaa"+reallyFriend);
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(reallyFriend.getBytes("UTF-8"));
				outputStream.flush();
				outputStream.close();
				
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



