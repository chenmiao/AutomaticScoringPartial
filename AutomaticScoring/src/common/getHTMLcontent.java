//package common;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.Reader;
//import java.io.Writer;
//import java.net.URL;
//import java.net.URLConnection;
//import java.sql.ResultSet;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import org.htmlparser.Node;
//import org.htmlparser.Parser;
//import org.htmlparser.tags.TableTag;
//import java.io.IOException;
//import java.io.FileReader;
//import java.io.Reader;
//import java.util.List;
//import java.util.ArrayList;
//import javax.swing.text.html.parser.ParserDelegator;
//import javax.swing.text.html.HTMLEditorKit.ParserCallback;
//import javax.swing.text.html.HTML.Tag;
//import javax.swing.text.MutableAttributeSet;
//
//public class getHTMLcontent {
//	
//	static public String getHTMLcontent (String urlstring) throws Exception {
//        StringBuffer sb = new StringBuffer();
//        String temp = "";
//        URL url = new URL(urlstring);
//        //HttpConnection conn = null;
//	    URLConnection myurlcon = url.openConnection();
//        BufferedReader in = new BufferedReader(new InputStreamReader(myurlcon.getInputStream(), "utf-8"));   //
//        while ((temp = in.readLine()) != null) {
//            sb.append(temp + " ");
//        }
//        in.close();
//        return sb.toString();
//	}
//	
//	  public static List<String> extractText(Reader reader) throws IOException {
//		    final ArrayList<String> list = new ArrayList<String>();
//		    
//		    ParserDelegator parserDelegator = new ParserDelegator();
//		    ParserCallback parserCallback = new ParserCallback() {
//		      public void handleText(final char[] data, final int pos) { 
//		        list.add(new String(data));
//		      }
//		      public void handleStartTag(Tag tag, MutableAttributeSet attribute, int pos) { }
//		      public void handleEndTag(Tag t, final int pos) {  }
//		      public void handleSimpleTag(Tag t, MutableAttributeSet a, final int pos) { }
//		      public void handleComment(final char[] data, final int pos) { }
//		      public void handleError(final java.lang.String errMsg, final int pos) { }
//		    };
//		    parserDelegator.parse(reader, parserCallback, true);
//		    return list;
//		  }
//		  
//		  public static String getHTMLwithoutTag (String path) throws Exception {
//			    String text = "";
//			    FileReader reader = new FileReader(path);
//			    List<String> lines = extractText(reader);
//			    for (String line : lines) {
//			    	if (line.length() > 30)
//			    		text = text + line + "\r\n";
//			    		//System.out.println(line);
//			    }
//			    return text;
//		  }
//
//    public String getOneHtml(String htmlurl) throws IOException{
//        URL url;
//        String temp;
//        StringBuffer sb = new StringBuffer();
//        try {
//            url = new URL(htmlurl);
//		    URLConnection myurlcon = url.openConnection();
//		    myurlcon.setConnectTimeout(1000);
//		    myurlcon.setReadTimeout(1500);
//            BufferedReader in = new BufferedReader(new InputStreamReader(myurlcon.getInputStream(), "utf-8"));
//            while ((temp = in.readLine()) != null) {
//                sb.append(temp);
//            }
//            in.close();
//        }catch (Exception e) {
//        }
//        return sb.toString();
//    }
//
//    public String getTitle(String s) {
//        String regex;
//        String title = "";
//        List<String> list = new ArrayList<String>();
//        regex = "<title>.*?</title>";
//        Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
//        Matcher ma = pa.matcher(s);
//        while (ma.find()) {
//            list.add(ma.group());
//        }
//        for (int i = 0; i < list.size(); i++) {
//            title = title + list.get(i);
//        }
//        return outTag(title);
//    }
//    
//    public String getPosting(String s) {
//        String regex;
//        String title = "";
//        List<String> list = new ArrayList<String>();
//        regex = "<p class=\"blogContent\">.*?<p class=\"blogContentInfo\">";
//        Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);
//        Matcher ma = pa.matcher(s);
//        while (ma.find()) {
//            list.add(ma.group());
//        }
//        for (int i = 0; i < list.size(); i++) {
//            title = title + list.get(i);
//        }
//        return title;
//    }
//
//    public List<String> getComments(String s) {
//        String regex;
//        List<String> list = new ArrayList<String>();
//        regex = "<td class=\"blogComments\">.*?</tr>";
//        Pattern pa = Pattern.compile(regex, Pattern.DOTALL);
//        Matcher ma = pa.matcher(s);
//        while (ma.find()) {
//            list.add(ma.group());
//        }
//        return list;
//    }
//    
//    static public String getSnippetBasedContentfromHTML (String HTML,String snippet) {
//		boolean flag = false;
//		int upperboundry = 0, lowerboundry = 0, testlocation = 0;
//		//System.out.println("Here---------------");
//		if (HTML.indexOf(snippet.substring(0, 15))!=-1) {                                 //Find the location of Snippet in HTML
//			testlocation = HTML.indexOf(snippet.substring(0, 15));     //exact match
//			flag = true;
//		}
//		else {                                                         //fuzzy match   --- 3 tokens
//			if (HTML.indexOf(snippet.substring(15, 30))!=-1) {                                 //Find the location of Snippet in HTML
//				testlocation = HTML.indexOf(snippet.substring(15, 30));     //exact match
//				flag = true;
//			}
//			else {
//				FuzzyDistance fd = new FuzzyDistance();
//				int distance = 6, tempdistance = 0, numtokens = 5;
//				String tokensinsnippet [] = snippet.split(" ");
//				String tokensinHTML [] = HTML.split(" ");      //\\s+
//				String tempsnippet = "";
//				for (int j = 0; j < numtokens; j++)
//					tempsnippet = tempsnippet + tokensinsnippet[j] + " ";
//				tempsnippet = tempsnippet.substring(0, tempsnippet.length() - 1);
//				for (int i = 0; i < tokensinHTML.length-numtokens; i++) {
//					String tempHTML = "";
//					for (int j = 0; j < numtokens; j++)
//						tempHTML = tempHTML + tokensinHTML[i + j] + " ";
//					tempHTML = tempHTML.substring(0, tempHTML.length() - 1);
//					tempdistance = fd.LD(tempsnippet, tempHTML);
//					if (tempdistance < distance) {
//						flag = true;
//						distance = tempdistance;
//						testlocation = HTML.indexOf(tempHTML); 
//						System.out.println("Find one: " + tempHTML + "   ------   " + tempsnippet + "    :      " +distance);
//					}
//				}
//			}
//		}
//		while (HTML.indexOf("      ")!=-1 && testlocation > HTML.indexOf("      ")) {                     //Find the upperboundry
//			upperboundry = HTML.indexOf("      ") + 6;
//			HTML = HTML.substring(upperboundry);
//			testlocation = testlocation - upperboundry;
//		}
//		upperboundry = upperboundry - 6;
//		while (HTML.indexOf("\t\t")!=-1 && testlocation > HTML.indexOf("\t\t")) {                     
//			upperboundry = HTML.indexOf("\t\t") + 2;
//			HTML = HTML.substring(upperboundry);
//			testlocation = testlocation - upperboundry;
//		}
//		upperboundry = upperboundry - 2;
//		upperboundry = 0;
//		if ((HTML.indexOf("      ", upperboundry) < HTML.indexOf("\t\t", upperboundry)) || (HTML.indexOf("\t\t", upperboundry)==-1))
//			lowerboundry = HTML.indexOf("      ", upperboundry);                                     //Find the lowerboundry
//		else
//			lowerboundry = HTML.indexOf("\t\t", upperboundry); 
//		HTML = HTML.substring(upperboundry, lowerboundry);
//		if (flag==true || HTML.length()>200) {
//			Pattern p=Pattern.compile("[&][a-z]{1,5}[;]"); 
//			String content = HTML.replaceAll("'"," ").replaceAll("\"","");
//			Matcher m = p.matcher(content); 
//			content = m.replaceAll(" "); 
//			System.out.println("Suc! ");
//		}
//		return HTML;
//    }
//
//    public static String outTag(String s) {
//        return s.replaceAll("<.*?>", "   ");
//    }
//
//	public static void main(String[] args) {
//		
//	}
//
//}
