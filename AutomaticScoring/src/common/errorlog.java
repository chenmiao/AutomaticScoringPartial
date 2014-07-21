package common;

import java.io.*;

public class errorlog {
	final static String path = config.configmap.get("ERROR_LOG").toString();
	static PrintWriter error = null; 
	
	static public PrintWriter geterrorstream () throws Exception {
		error = new PrintWriter(new FileWriter(path,true));
		return error;
	}
	
	static public void clearerrorlog () {
		BufferedWriter bw = null;
		try {
		     bw = new BufferedWriter(new FileWriter(path));
			 bw.write("");
			 bw.flush();
		}
		catch (IOException ioe) {
			 ioe.printStackTrace();
		} 
		finally {                       
			 if (bw != null) try {
			    bw.close();
			 }
			 catch (IOException ioe2) {
			    // ignore it
			 }
		 }
	}
	
	static public void errorclose () {
	    error.flush();
	    error.close();
	}
}
