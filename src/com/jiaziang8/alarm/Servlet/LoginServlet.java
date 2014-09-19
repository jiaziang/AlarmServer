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

import com.jiaziang8.alarmServer.util.Constants;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String CONNECT = "jdbc:mysql://localhost:3306/alarm?useUnicode=true&characterEncoding=utf8";
	static String user = Constants.MYSQL_ACCOUNT;
	static String password = Constants.MYSQL_PASSWORD;
	private Connection con ;
	private Statement statement ;

	public LoginServlet() {
		super();
		System.out.println("Servlet started!!!!!");
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
		String username = request.getParameter("username");
		String userpwd = request.getParameter("userpwd");
		con = null;
		statement = null;
		ResultSet rs = null;

		try {
			getConnection();
			rs = statement
					.executeQuery("select * from user where account='"
							+ username + "'and password='" + userpwd + "'");
			rs.beforeFirst();

			if (rs.next()) {
				response.setStatus(201);
			} else {
				response.setStatus(202);
			}
			rs.close();
			rs = null;
			statement.close();
			statement = null;
			con.close();
			con = null;

		} catch (SQLException e) {
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

	@Override
	public void destroy() {
		super.destroy();
	}

}
