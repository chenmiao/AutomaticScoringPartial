package utilities;

//File: SimpleEmbedded.java
import org.python.util.PythonInterpreter;
import org.python.core.*;

import java.io.*;

public class SimpleEmbedded
{
 public static void main(String[]args) throws PyException, IOException
 {
//     BufferedReader terminal;
     PythonInterpreter interp;
     interp = new PythonInterpreter();
//     terminal = new BufferedReader(new InputStreamReader(System.in));
//     System.out.println ("Hello");
     String txt=FileIO.getFileByEncoding("/Users/miaochen/Documents/diss-experiment/proc-corpus/wikitext.txt", "utf-8");
     interp.set("txt", txt);     
     
     interp.exec("import sys");
     interp.exec("sys.path.append(\"/Users/miaochen/Documents/diss-experiment/python-code/\")");
     interp.exec("import WikiExtractor");
     interp.exec("import generalProc");
//     interp.exec("text=generalProc.readFileToString(\"/Users/miaochen/Documents/diss-experiment/proc-corpus/wikitext.txt\")");
//     interp.exec("text=unicode(text,\"utf-8\")");
     interp.exec("clnTxt=WikiExtractor.clean(txt)");
//     interp.exec("print text");
     PyObject namespace = interp.getLocals();
     
//     PyObject obj = namespace.__finditem__("text");
//     System.out.println(obj.__tojava__(String.class));
//     String text=obj.__tojava__(String.class).toString();
     String cleaned=namespace.__finditem__("clnTxt").__tojava__(String.class).toString();
     System.out.println("Here is the cleaned text!");
     System.out.println(cleaned);
     
//     String text=FileIO.getFileByEncoding("/Users/miaochen/Documents/diss-experiment/proc-corpus/wikitext.txt", "utf-8");
     
     
//     interp.exec("print sys");
//     interp.set("a", new PyInteger(42));
//     interp.exec("print a");
//     interp.exec("x = 2+2");
//     PyObject x = interp.get("x");
//     System.out.println("x: " + x);
//     PyObject localvars = interp.getLocals();
//     interp.set("localvars", localvars);
//     String codeString = "";
//     String prompt = ">> ";
//     
//     while (true)
//     {
//         System.out.print (prompt);
//         try
//         {   
//             codeString = terminal.readLine();
//             if (codeString.equals("exit"))
//             {
//                 System.exit(0);
//                 break;
//             }
//             interp.exec(codeString);
//         }
//         catch (IOException e)
//         {
//             e.printStackTrace();
//         }
//     }
//     System.out.println("Goodbye");
 }
}
