package utilities;

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/***
 * it cleans Wiki text by using the Jython package
 * It makes use of the WikiExtractor package
 * 
 * @author miaochen
 *
 */


public class WikiTextCleaner {
	
	String txt;
	
	WikiTextCleaner (String txt) {
		this.txt=txt;
	}
	
	String clean (PythonInterpreter interp) {
		//here we use the WikiExtractor.py
		interp.set("txt", txt); 
		interp.exec("import sys");
//		this coulbe be a param
	    interp.exec("sys.path.append(\"/Users/miaochen/Documents/diss-experiment/python-code/\")");  //a potential param
	    interp.exec("import WikiExtractor");
	    interp.exec("import generalProc");
	    interp.exec("clnTxt=WikiExtractor.clean(txt)");
	    PyObject namespace = interp.getLocals();
	    String cleaned=namespace.__finditem__("clnTxt").__tojava__(String.class).toString();
//	    System.out.println(cleaned);
	    //then we remove \n, *, \', [...] in text
	    cleaned=cleaned.replaceAll("\\[.*?\\\\n", "");
	    cleaned=cleaned.replaceAll("\\\\n", "");
	    cleaned=cleaned.replaceAll("\\\\'", "");
	    cleaned=cleaned.replaceAll("\\*", "");
	    cleaned=cleaned.replaceAll("\\\\", "");	
	    cleaned=cleaned.replaceAll("=+", ""); //remove = 
	    cleaned=cleaned.replaceAll("\\(.*?\\)", "");  // (...)
	    System.out.println(cleaned.length());
//	    trail the start and end '
	    if (cleaned.length()>3 && cleaned.startsWith("'") && cleaned.endsWith("'")) {
	    	cleaned=cleaned.substring(1, cleaned.length()-2); //make sure the cleaned string is long enough
	    }
	    
	    return cleaned;
	}
	
	
	public static void main (String[] args) {
		
		Toolbox toolbox=new Toolbox(false,false,false,true,false,false,false,true);
		toolbox.enableTools();
		String t=FileIO.getFileByEncoding("/Users/miaochen/Documents/diss-experiment/proc-corpus/wikitext.txt", "utf-8");
		WikiTextCleaner cleaner=new WikiTextCleaner(t);
		System.out.println(cleaner.clean(toolbox.interp));
		
	}

}
