package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/***
 * code sample from http://java.sun.com/developer/technicalArticles/Security/pwordmask/
 * 
 * @author miaochen
 *
 */

public class PasswordField {

	   /**
	    *@param prompt The prompt to display to the user
	    *@return The password as entered by the user
	    */
	   public static String readPassword (String prompt) {
	      EraserThread et = new EraserThread(prompt);
	      Thread mask = new Thread(et);
	      mask.start();

	      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	      String password = "";

	      try {
	         password = in.readLine();
	      } catch (IOException ioe) {
	        ioe.printStackTrace();
	      }
	      // stop masking
	      et.stopMasking();
	      // return the password entered by the user
	      return password;
	   }
	   
	   public static void main (String[] args){
		   
		   String password = PasswordField.readPassword("Enter password: ");
		   System.out.println("The password entered is: "+password);
		   
	   }
	   
	}
