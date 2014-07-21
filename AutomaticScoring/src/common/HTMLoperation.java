//package common;
//
//import java.io.BufferedReader;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.InetSocketAddress;
//import java.net.Proxy;
//import java.net.SocketAddress;
//import java.net.URL;
//import java.net.URLConnection;
//import java.nio.ByteBuffer;
//
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.methods.PostMethod;
//
//public class HTMLoperation {
//	
//	static public String getHTMLcontent (String urlstring) throws Exception {
//        StringBuffer sb = new StringBuffer();
//        String temp = "";
//        URL url = new URL(urlstring);
//        //HttpConnection conn = null;
//	    URLConnection myurlcon = url.openConnection();
//        BufferedReader in = new BufferedReader(new InputStreamReader(myurlcon.getInputStream(), "utf-8"));
//        while ((temp = in.readLine()) != null) {
//            sb.append(temp + "\r\n");
//        }
//        in.close();
//        return sb.toString();
//	}
//	
//	static public String getHTMLcontent_post (String urlstring, String [] parameters, String [] values) {
//		   try {
//			    HttpClient client = new HttpClient();
//			    PostMethod method = new PostMethod( urlstring );
//
//				 // Configure the form parameters
//			    for (int i = 0; i < parameters.length; i++)
//			    	method.addParameter(parameters[i], values[i]);
//				 // Execute the POST method
//			    int statusCode = client.executeMethod( method );
//			    if( statusCode != -1 ) {
//			      //String contents = method.getResponseBodyAsString();
//			      InputStream s =method.getResponseBodyAsStream();
//			      String contents = convertStreamToString(s);
//			      method.releaseConnection();
//			      //System.out.println( contents );
//			      return contents;
//			    }
//			   }
//			   catch( Exception e ) {
//			    e.printStackTrace();
//			   }
//			   return "";
//	}
//	
//	static public String getHTMLcontent_proxy (String urlstring) throws Exception {
//		SocketAddress addr = new InetSocketAddress("socks.yahoo.com", 1080);
//		Proxy proxy = new Proxy(Proxy.Type.SOCKS, addr);
//        StringBuffer sb = new StringBuffer();
//        String temp = "";
//        URL url = new URL(urlstring);
//	    URLConnection myurlcon = url.openConnection(proxy);
//        BufferedReader in = new BufferedReader(new InputStreamReader(myurlcon.getInputStream(), "utf-8"));
//        while ((temp = in.readLine()) != null) {
//            sb.append(temp + "\r\n");
//        }
//        in.close();
//        return sb.toString();
//	}
//	
//	static public String convertURL (String rawURL) {
//		String url = rawURL.replaceAll("%3a", ":").replaceAll("%3A", ":").replaceAll("%3f", "?")
//			.replaceAll("%3F", "?").replaceAll("%3D", "=").replaceAll("%3d", "=")
//			.replaceAll("%26", "&");
//		return url;
//	}
//	
//    static private String convertStreamToString(InputStream is) {
//        /*
//         * To convert the InputStream to String we use the BufferedReader.readLine()
//         * method. We iterate until the BufferedReader return null which means
//         * there's no more data to read. Each line will appended to a StringBuilder
//         * and returned as String.
//         */
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
// 
//        String line = null;
//        try {
//            while ((line = reader.readLine()) != null) {
//                sb.append(line + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                is.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
// 
//        return sb.toString();
//    }
//    
//    public static ByteBuffer getAsByteArray(URL url) throws IOException {
//        URLConnection connection = url.openConnection();
//        // Since you get a URLConnection, use it to get the InputStream
//        InputStream in = connection.getInputStream();
//        // Now that the InputStream is open, get the content length
//        int contentLength = connection.getContentLength();
//
//        // To avoid having to resize the array over and over and over as
//        // bytes are written to the array, provide an accurate estimate of
//        // the ultimate size of the byte array
//        ByteArrayOutputStream tmpOut;
//        if (contentLength != -1) {
//            tmpOut = new ByteArrayOutputStream(contentLength);
//        } else {
//            tmpOut = new ByteArrayOutputStream(16384); // Pick some appropriate size
//        }
//
//        byte[] buf = new byte[512];
//        while (true) {
//            int len = in.read(buf);
//            if (len == -1) {
//                break;
//            }
//            tmpOut.write(buf, 0, len);
//        }
//        in.close();
//        tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive
//
//        byte[] array = tmpOut.toByteArray();
//
//        //Lines below used to test if file is corrupt
//        //FileOutputStream fos = new FileOutputStream("C:\\abc.pdf");
//        //fos.write(array);
//        //fos.close();
//        for (int i = 0; i < array.length; i++){
//        	//System.out.print(array[i]);
//        	System.out.print((char)array[i]);
//        }
//        return ByteBuffer.wrap(array);
//    }
//    
//    public static void main(String[] args) throws Exception {
//    	//System.out.println(HTMLoperation.getHTMLcontent("http://www.asis.org/asist2010/proceedings/proceedings/ASIST_AM10/submissions/145_Final_Submission.pdf"));
//    	URL url = new URL("http://www.asis.org/asist2010/proceedings/proceedings/ASIST_AM10/submissions/145_Final_Submission.pdf");
//    	getAsByteArray(url);
//    }
//}
