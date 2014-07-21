package common;

import java.sql.Connection;
import java.sql.DriverManager;


public class DBconnect {
	public Connection connection;

	public DBconnect (String url, String username, String password) throws Exception {
		Class.forName("org.gjt.mm.mysql.Driver");
        //String url = "jdbc:mysql://lwd0.search:3306/uiv2";
        //String username = "mysql";
        //String password = "";    
        connection = DriverManager.getConnection (url, username, password);
	}
}