package common;

import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class readsubprotag {
	String path;
	static public ArrayList protagIDarray = new ArrayList();
	
	static public ArrayList load_subprotagfile (String path) throws Exception {
		try {
	    	String subprotag;
	        FileInputStream fin =  new FileInputStream(path);
	        BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
	        while ((subprotag = myInput.readLine()) != null) {  
	        	int subprotagID = getsubprotagID(subprotag);
	        	if (subprotagID!=-1) 
	        		protagIDarray.add(subprotagID);
	        }
	    }
	    catch (Exception e) {
	    	e.printStackTrace();
	        e.printStackTrace(errorlog.geterrorstream());
	    }
	    if (protagIDarray.size() == 0) System.out.println("No legal protagonist in the list file" + 
	    		config.configmap.get("PROTAG_FILE").toString() + ", please check...");
	    return protagIDarray;
	}
	
	static public int getsubprotagID (String subprotag) throws Exception {
		if (subprotag.length() <= 2) return -1;
		int subprotagID = 0;
		Statement statement = config.connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT * FROM subprotag where sub_protag = '" + subprotag + "'"); 
		if (rs.next()) {
			subprotagID = rs.getInt("ID");
			rs.close();
			statement.close();
			return subprotagID;
		}
		statement.executeUpdate("insert into subprotag (sub_protag) values ('" + subprotag + "')");
		rs = statement.executeQuery("SELECT * FROM subprotag where sub_protag = '" + subprotag + "'"); 
		rs.next();
		subprotagID = rs.getInt("ID");
		rs.close();
		statement.close();
		return subprotagID;
	}
}
