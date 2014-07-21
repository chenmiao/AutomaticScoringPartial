package utilities;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/***
 * it should be a sub-class of XmlProcessor, but since I'm not acquainted with Inheritance in Java,
 * I just made it as an independent class here.
 * It processes Wiki anno xml files resulted from Wikification
 * 
 * note: for Wiki concept processing, the input is .full.xml
 * for word replaced by Wiki processing, the input is .flat.html
 * 
 * @author miaochen
 *
 */

public class WikiXmlDocument {
	
    Document doc=null; //the doc is about xml doc (.full.xml), from wikification results
    File htmlFile=null; //is about html doc (.flat.html), from wikification results
    HashMap<Integer, Integer> wiki_rawfreq;
    HashMap<Integer, Integer> wiki_boolean;
    HashMap<Integer, Double> wiki_normfreq;
    HashMap<Integer, Double> wiki_tfidf;
    
    HashMap<String, Integer> word_rawfreq;
    HashMap<String, Double> word_normfreq;
    HashMap<String, Double> word_tfidf;
	
	WikiXmlDocument (Document doc){
		this.doc=doc;
	}
	
	WikiXmlDocument (String xmlPath){
		
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
	
	WikiXmlDocument (String xmlPath, String encoding) {
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
	
	WikiXmlDocument (File htmlFile) {
		this.htmlFile=htmlFile;
	}
	
	/***
	 * extract all the identified top wiki ids from the wiki annotation xml file
	 * <TopDisambiguation>
	 * ...
	 *    <WikititleID>
	 *    ...
	 *    </WikititleID>
	 * </TopDisambiguation>
	 * 
	 * @return
	 */
	List<Integer> getWikiIdsfromWikiAnno () {
		List<Integer> wikiIds=new ArrayList<Integer> ();
		
		NodeList nodes=doc.getElementsByTagName("TopDisambiguation");
		if (nodes.getLength() > 0) {
			for (int i=0; i<nodes.getLength(); i++) {
				Node aNode=nodes.item(i);
				
				if (aNode instanceof Element) {
					Element eNode=(Element) aNode;
					
					NodeList childs=eNode.getElementsByTagName("WikiTitleID");
				    //get the text content of the <WikiTitleID> in xml
				    String id=childs.item(0).getTextContent();
//				    make sure the id string is not empty or null
				    if ((!id.isEmpty()) && (id!=null)) {
				    	Integer wikiId=Integer.valueOf(id);
				    	wikiIds.add(wikiId);
//				    	System.out.println("added wiki id "+wikiId);
				    }				    
				}
				
			}
		}
		return wikiIds;
	}
	
	/***
	 * this works on the .flat.html file, so make sure use the correct constructor for the class!!!
	 * 
	 * @param wtype
	 * weighting type: 
	 * 0=RawTf*idf, 
	 * 1=(RawTf/DocLen)*idf
	 * 2=(RawTf/maximum term frequency)*idf
	 * 3=(RawTf/EucLen)*idf
	 * 4=boolean
	 * 5=(LogarithmTf)*idf
	 * @param word_idf
	 */
	void toWordReplacedByWikiTfidf (int wtype, HashMap word_idf, SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		word_tfidf=new HashMap<String, Double> ();
		
		switch (wtype) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			String text=removeWordsHavingMatchingInWiki();
			utilities.Document doc=new utilities.Document(text);
			doc.toWordTfIdf(wtype, splitter, tokenizer, word_idf);
			word_tfidf=doc.word_tfidf;
			break;
		case 4:
			break;
		case 5:
			break;
		case 6:
			toWordReplacedByWikiFrequencyNormalizedByEuclideanLength(splitter, tokenizer);
			word_tfidf=word_normfreq;
			wiki_tfidf=wiki_normfreq;
		default:
			System.out.println("There is something wrong with the code!");
		}
	}
	
	void toWordReplacedByWikiRawFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
//		String text=removeWordsHavingMatchingInWiki(FileIO.getFileObjectAsString(htmlFile));
		String text=removeWordsHavingMatchingInWiki();
		utilities.Document doc=new utilities.Document(text);
		doc.toWordVectorRawFrequency(splitter, tokenizer);
		word_rawfreq=doc.word_rawfreq;
	}
	
//	void toWordReplacedByWikiRawFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
//		String text=removeWordsHavingMatchingInWiki(FileIO.getFileObjectAsString(htmlFile));
//		utilities.Document doc=new utilities.Document(text);
//		doc.toWordVectorRawFrequency(splitter, tokenizer);
//		word_rawfreq=doc.word_rawfreq;
//	}
	
	void toWordReplacedByWikiFrequencyNormalizedByEuclideanLength (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		String text=removeWordsHavingMatchingInWiki(FileIO.getFileObjectAsString(htmlFile));
		utilities.Document doc=new utilities.Document(text);
		doc.toWordVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer);
		word_normfreq=doc.word_normfreq;      
	}
	
	/***
	 * given a text file, remove the words that have a match in Wiki
	 * 
	 * @param text
	 * @return
	 */
	String removeWordsHavingMatchingInWiki (String text) {
//		Pattern p=Pattern.compile("<a href(.*?)</a>");
//		Matcher m=p.matcher(text);
		return text.replaceAll("<a href(.*?)</a>", " ");
	}
	
    /***
     * this works on .xml file, find the matched wiki concepts, and remove them from text
     * it converts text into array, [t] [u] [i] t i o n f e e such things
     * and put them into hash according to the position in text, idx => true/false (true means keep in text,
     * false means replaced by Wiki concepts)
     * 
     */
	String removeWordsHavingMatchingInWiki () {
		String out="";
		
		String text=getTextFromXML();
		char[] chars=text.toCharArray();
		HashMap<Integer,Boolean> idx_keep=new HashMap<Integer,Boolean> ();
		
		for (int n=0; n<chars.length; n++) {
			idx_keep.put(new Integer(n), true);
		}
		
		NodeList nodes=doc.getElementsByTagName("Entity");
		if (nodes.getLength() > 0) { //make sure there is such result
			for (int i=0; i<nodes.getLength(); i++) {
				Node aNode=nodes.item(i);
				
				if (aNode instanceof Element) {
					Element eNode=(Element) aNode;					
					NodeList childs1=eNode.getElementsByTagName("EntityTextStart");
				    //get the text content of the <WikiTitleID> in xml
//				    String id=childs.item(0).getTextContent();
				    int startidx=Integer.parseInt(childs1.item(0).getTextContent());
				    NodeList childs2=eNode.getElementsByTagName("EntityTextEnd");
				    int endidx=Integer.parseInt(childs2.item(0).getTextContent());
				    
				    for (int k=startidx; k<=endidx; k++) {
				    	idx_keep.put(new Integer(k), false);
				    }				    				    
				}
				
			}
		}
		 
		for (int n=0; n<chars.length; n++) {
			if (idx_keep.get(new Integer(n)).booleanValue()){
				out+=chars[n];
			}
		}
		return out;
	}
	
	String getTextFromXML () {
		String text="";
		NodeList nodes=doc.getElementsByTagName("InputText");
		if (nodes.getLength() > 0) { //make sure there is such result
			text=nodes.item(0).getTextContent();			
		}
		return text;
	}
	
	
	/***
	 * compute tf*idf values for vector of Wiki concepts
	 * 
	 * weighting type: 
	 * 0=RawTf*idf, 
	 * 1=(RawTf/DocLen)*idf
	 * 2=(RawTf/maximum term frequency)*idf
	 * 3=(RawTf/EucLen)*idf
	 * 4=boolean
	 * 5=(LogarithmTf)*idf
	 * 
	 * NOTE: for a test set, unseen concept in test set can be assigned a default idf here
	 * 
	 * @param wtype
	 * @param wiki_idf
	 */
	void toWikiVectorTfidfNormalized (int wtype, HashMap wiki_idf) {
		wiki_tfidf=new HashMap<Integer, Double> ();
		
		switch (wtype) {
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			toWikiVectorRawFrequency();
			double norm=0;
			for (Iterator iter=wiki_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				Integer wikiid=(Integer) iter.next();
				int tf=wiki_rawfreq.get(wikiid).intValue();
				double idf=getIdfGivenKeyAndHash(wikiid,wiki_idf).doubleValue();
				double tfidf=(double)tf*idf;
				wiki_tfidf.put(wikiid, new Double(tfidf));
				norm+=tfidf*tfidf;
			}
			double sqrt=Math.sqrt(norm);
			for (Iterator iter=wiki_tfidf.keySet().iterator(); iter.hasNext(); ) {
				Integer wikiid=(Integer) iter.next();
				double tfidf=wiki_tfidf.get(wikiid).doubleValue();
				if (sqrt==0) {
					wiki_tfidf.put(wikiid, new Double(0.0));
				}else {
					wiki_tfidf.put(wikiid, new Double(tfidf/sqrt));
				}				
			}
			break;
		case 4:
			break;
		case 5:
			break;
		case 6: 
			toWikiVectorFrequencyNormalizedByEuclideanLength();
			wiki_tfidf=wiki_normfreq;
		default:
			System.out.println("There is something wrong with the code!");
		}
	}
	
	void toWikiVectorTfidf (int wtype, HashMap wiki_idf) {
		wiki_tfidf=new HashMap<Integer, Double> ();
		
		switch (wtype) {
		case 3:
			toWikiVectorRawFrequency();
			for (Iterator iter=wiki_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				Integer wikiid=(Integer) iter.next();
				int tf=wiki_rawfreq.get(wikiid).intValue();
				double idf=getIdfGivenKeyAndHash(wikiid,wiki_idf).doubleValue();
				double tfidf=(double)tf*idf;
				wiki_tfidf.put(wikiid, new Double(tfidf));
			}
			break;
		default:
			System.out.println("There is something wrong with the code!");
		}
	}
	
	
	
	void toWikiVectorRawFrequency () {
        wiki_rawfreq=new HashMap<Integer, Integer> ();
		
		NodeList nodes=doc.getElementsByTagName("TopDisambiguation");
		if (nodes.getLength() > 0) {
			for (int i=0; i<nodes.getLength(); i++) {
				Node aNode=nodes.item(i);
				
				if (aNode instanceof Element) {
					Element eNode=(Element) aNode;					
					NodeList childs=eNode.getElementsByTagName("WikiTitleID");
				    //get the text content of the <WikiTitleID> in xml
				    String id=childs.item(0).getTextContent();
//				    make sure the id string is not empty or null
				    if ((!id.isEmpty()) && (id!=null)) {
				    	Integer wikiId=Integer.valueOf(id);
				    	if (wiki_rawfreq.containsKey(wikiId)){
				    		int freq=wiki_rawfreq.get(wikiId).intValue();
				    		wiki_rawfreq.put(wikiId, new Integer (freq+1));
				    	}else{
				    		wiki_rawfreq.put(wikiId, new Integer (1));
				    	}				    	
				    }				    
				}
				
			}
		}
	}
	
	void toWikiVectorFrequencyNormalizedByEuclideanLength () {
		wiki_normfreq=new HashMap<Integer, Double> ();
		toWikiVectorRawFrequency();
		
		int norm=0;
		for (Iterator iter=wiki_rawfreq.keySet().iterator(); iter.hasNext(); ) {
			Integer wikiId=(Integer) iter.next();
			int freq=wiki_rawfreq.get(wikiId).intValue();
			norm+=freq*freq;
			wiki_normfreq.put(wikiId, new Double ((double) freq));
		}
		double sqrtnorm=Math.sqrt((double)norm);
		
		for (Iterator iter=wiki_normfreq.keySet().iterator(); iter.hasNext(); ) {
			Integer wikiId=(Integer) iter.next();
			double freq=wiki_normfreq.get(wikiId).doubleValue();
			wiki_normfreq.put(wikiId, new Double ((double)freq/(double)sqrtnorm));
		}
		
	}
	
	
	/***
	 * given a hashmap idf, find if a key occurs in it;
	 * if so, return the idf value; if not, return 0;
	 * 
	 * @param key
	 * @param idf
	 * @return
	 */
	Double getIdfGivenKeyAndHash (Object key, HashMap idf){
		if (idf.containsKey(key))
			return (Double) idf.get(key);
		else
			return (Double) 0.0;
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
	
	
	public static void main (String[] args) {
		
//		String xmlPath="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml/098/train0/" +
//				"7508663-VB531098.txt.wikification.tagged.full.xml";
		String xmlPath="/Users/miaochen/Documents/diss-experiment/proc-corpus/test2.xml";
		WikiXmlDocument xproc=new WikiXmlDocument(xmlPath,"utf-8");
//		List<Integer> ids=xproc.getWikiIdsfromWikiAnno();
//		for (Integer id : ids)
//			System.out.println(id);
		System.out.println(xproc.removeWordsHavingMatchingInWiki());
		
	}

}
