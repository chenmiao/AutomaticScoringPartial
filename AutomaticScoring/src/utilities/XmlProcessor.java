package utilities;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/***
 * some operations on the Document (xml) class
 * it processes the wiki anno file and extracts wiki concepts from xml
 * 
 * @author Miao
 *
 */
public class XmlProcessor {
	
	Document doc=null;
	
	XmlProcessor (Document doc){
		this.doc=doc;
	}
	
	XmlProcessor (String xmlPath){
		
		File fXmlFile = new File(xmlPath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(fXmlFile);
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	XmlProcessor (String xmlPath, String encoding) {
		String text=FileIO.getFileByEncoding(xmlPath, encoding);
		InputStream is=new ByteArrayInputStream(text.getBytes());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			doc=dBuilder.parse(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	NodeList getNodesGivenTagName (String tag){
		return doc.getElementsByTagName(tag);
	}
	
	NodeList getChildNodesGivenANode (Node node){
		return node.getChildNodes();
	}
	
	//given a xml tag name, return number of elements with this tag name
	int countNumOfElementGivenTagName (String tag){
		
		return getNodesGivenTagName(tag).getLength();

	}
	
	//this is for elements only occurring once in the xml file, such as <pubdate>
	String getTextValueGivenTagName(String tag){
		
		if(getNodesGivenTagName(tag).getLength()==0){
			return null;
		}else if(getNodesGivenTagName(tag).getLength()>1){
			System.err.println("Warning!! There is more than one "+tag+" element in the xml file!");
		}
		
		return getNodesGivenTagName(tag).item(0).getTextContent();
		
	}
	
	/***
	 * The method is specifically for the "<attr>" elements in the xml file
	 * Given a text content, e.g. 
	 * 
	 * @param text
	 * @return
	 */
//	NodeList getAttriNodeListGivenTextContent(String text){
//		
//		
//		
//	}
	
	public static void main (String[] args) {
		
//		String xmlPath="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml/098/train0/" +
//				"7508663-VB531098.txt.wikification.tagged.full.xml";
		String xmlPath="/Users/miaochen/Documents/diss-experiment/proc-corpus/test2.xml";
		XmlProcessor xproc=new XmlProcessor(xmlPath,"utf-8");
		
		NodeList attriNodes=xproc.getNodesGivenTagName("TopDisambiguation");
		//loop for each <attr> node
		for (int i=0;i<attriNodes.getLength();i++){
			
			Node attriNode=attriNodes.item(i);
			Element eNode=(Element) attriNode;
		    NodeList attriChilds=eNode.getElementsByTagName("WikiTitleID");
		    //get the text content of the <attrilabl> in xml
		    String attriLable=attriChilds.item(0).getTextContent();
			System.out.println(attriLable);
						
		}
		
		
		
		
	}

}

