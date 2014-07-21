package common;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class fileoperator {
	static public String readfile (String path) {
	    File file = new File(path);
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    String content = "";
	    try {
	      fis = new FileInputStream(file);
	      // Here BufferedInputStream is added for fast reading.
	      bis = new BufferedInputStream(fis);
	      dis = new DataInputStream(bis);
	      // dis.available() returns 0 if the file does not have more lines.
	      while (dis.available() != 0) {
	    	  content = content + dis.readLine();
	      }
	      fis.close();
	      bis.close();
	      dis.close();
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  return content;
	}
	
	static public String readfilewithchangeline (String path) {
	    File file = new File(path);
	    FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	    String content = "";
	    try {
	      fis = new FileInputStream(file);
	      // Here BufferedInputStream is added for fast reading.
	      bis = new BufferedInputStream(fis);
	      dis = new DataInputStream(bis);
	      // dis.available() returns 0 if the file does not have more lines.
	      while (dis.available() != 0) {
	    	  content = content + dis.readLine() + "\r\n";
	      }
	      fis.close();
	      bis.close();
	      dis.close();
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	  return content;
	}
}
