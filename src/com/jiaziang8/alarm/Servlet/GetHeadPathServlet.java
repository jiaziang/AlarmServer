package com.jiaziang8.alarm.Servlet;

import java.io.IOException;
import java.io.OutputStream;
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

import com.jiaziang8.alarmServer.util.Constants;

@WebServlet("/GetHeadPathServlet")
public class GetHeadPathServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con;
	private Statement statement;
       
    public GetHeadPathServlet() {
        super();
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} 
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String account = request.getParameter("account");
		String resultString = "";
		con = null;
		statement = null;
		ResultSet rSet = null;
		try {
			getConnection();
			rSet = statement.executeQuery("select *from user where account='"+account+"'");
			rSet.beforeFirst();
			if(rSet.next()){
				resultString = rSet.getString("headpath");
			}
			OutputStream outputStream = response.getOutputStream();
			outputStream.write(resultString.getBytes("utf-8"));
			outputStream.flush();
			outputStream.close();
			response.setStatus(200);
			rSet.close();
			rSet = null;
			statement.close();
			statement = null;
			con.close();
			con = null;
			
		} catch (Exception e) {
			e.printStackTrace();
		}	finally{
			if(rSet!=null){
				try {
					rSet.close();
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
