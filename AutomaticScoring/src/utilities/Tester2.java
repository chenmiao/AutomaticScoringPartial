package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public class Tester2 {
	
	Tester2 () {
		
	}
	
	void sethm () {
		HashMap hm1=new HashMap ();
		hm1.put("this", 2.5);
		hm1.put("that", 3.5);
		HashMap hm2=hm1;
		expandVec(hm2);
		System.out.println(hm2);
	}
	
	void expandVec (HashMap hm) {
		hm.put("is", 4.5);
	}
	

	public static void main (String[] args) {
		
		double n=2;
		int m=(int) n;
		System.out.println(m);

		
//	    try {
//	        Runtime runtime = Runtime.getRuntime();
////	        InputStream input = runtime.exec("cmd \\c python '/Users/miaochen/Documents/diss-experiment/python-code/WikiExtractor.py'").getInputStream();
////	        InputStream input = runtime.exec("/Library/Frameworks/Python.framework/Versions/7.3/Resources/Python.app/Contents/MacOS/Python /Users/miaochen/Documents/diss-experiment/python-code/WikiExtractor.py").getInputStream();
//	        InputStream input = runtime.exec("/Library/Frameworks/Python.framework/Versions/7.3/Resources/Python.app/Contents/MacOS/Python /Users/miaochen/Documents/diss-experiment/python-code/cmd.py").getInputStream();
//	        BufferedInputStream buffer = new BufferedInputStream(input);
//	        BufferedReader commandResult = new BufferedReader(new InputStreamReader(buffer));
//	        String line = "";
//	        try {
//	            while ((line = commandResult.readLine()) != null) {
//	                s += line + "\n";
//	            }
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
//	        System.out.println(s);
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	    }
	    
	    
			
		
	}

}
