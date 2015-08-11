package at.micsti.mymusic;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DatabaseMetaData;

public class DbConnector {
	
	private static String driverName = "org.sqlite.JDBC";
	private static String jdbc = "jdbc:sqlite";
	
	// database file path
	private String path;
	
	// database url
	private String dbUrl;
	
	// database connection
	private Connection connection;


	public DbConnector(String dbPath) {
		path = dbPath;
		
		dbUrl = jdbc + ":" + path;
	}
	
	public void createConnection() {
		try {
			connection = DriverManager.getConnection(dbUrl);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
