package utilities;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/***
 * 
 * this class is for measuring similarity between 2 wiki concepts
 * 
 * @author miaochen
 *
 */
public class WikiSimilarity {
	
	
	int id1=0;
	int id2=0;
	
	public WikiSimilarity (int id1, int id2) {
		this.id1=id1;
		this.id2=id2;
	}
	
	double getLinkSimilarity () {

		if ((id1==0) || (id2==0)) //make sure this is not null id
			return 0;
		else {
			String urlstring="http://wikipedia-miner.cms.waikato.ac.nz/services/compare?" +
					"id1="+id1+"&id2="+id2;
			String xmlcontent=getContentGivenURL(urlstring);
			return getSimFromXml(xmlcontent);
		}
		
	}
	
	double getContentSimilarity (Toolbox toolbox) {
		double sim=0;
		
		String content1=toolbox.dbcon.getWikiPageContentGivenID(id1);
		String content2=toolbox.dbcon.getWikiPageContentGivenID(id2);
		
		WikiTextCleaner wikicleaner1=new WikiTextCleaner(content1);
		String cleaned1=wikicleaner1.clean(toolbox.interp);
		WikiTextCleaner wikicleaner2=new WikiTextCleaner(content2);
		String cleaned2=wikicleaner2.clean(toolbox.interp);
		
		Document doc1=new Document (cleaned1);
		Document doc2=new Document (cleaned2);
		doc1.toWordVectorRawFrequency(toolbox.splitter, toolbox.tokenizer);
		doc2.toWordVectorRawFrequency(toolbox.splitter, toolbox.tokenizer);
		Similarity similarity=new Similarity();
		sim=similarity.computeCosSimilarityOnRawFreq(doc1.word_rawfreq, doc2.word_rawfreq);
		
//		String content1=removeHtmlTag(toolbox.dbcon.getWikiPageContentGivenID(id1));
//		String content2=removeHtmlTag(toolbox.dbcon.getWikiPageContentGivenID(id2));
//		
//		Document doc1=new Document (content1);
//		Document doc2=new Document (content2);
//		doc1.toWordVectorRawFrequency(toolbox.splitter, toolbox.tokenizer);
//		doc2.toWordVectorRawFrequency(toolbox.splitter, toolbox.tokenizer);
//		
//		Similarity similarity=new Similarity();
//		sim=similarity.computeCosSimilarityOnRawFreq(doc1.word_rawfreq, doc2.word_rawfreq);
//		
		return sim;
	}
	
	
	double getSimFromXml (String xmlcontent) {
		
		double sim=0;
		
		InputStream is=new ByteArrayInputStream(xmlcontent.getBytes());
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;
		
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc=dBuilder.parse(is);
			NodeList nlist=doc.getElementsByTagName("message");
			
			if (nlist.getLength() == 0) {
				return sim;
			}else {
				Node anode=nlist.item(0);
//				get all attributes in a map (something like hashmap?)
				NamedNodeMap nmap=anode.getAttributes();
//				get the "relatedness" attribute as node
				Node attriNode=nmap.getNamedItem("relatedness");
				String sRel=attriNode.getTextContent();
				sim=Double.parseDouble(sRel);
			}
			
		}  catch (NumberFormatException e) {
			sim=0;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sim;
	}
	
	
	void writeURLContentToFile (String urlstring, String outpath) {
		String content=getContentGivenURL (urlstring);
		FileIO.writeFile(outpath, content);
	}
	
	
	String getContentGivenURL (String urlstring) {
		
		URL url=null;
		String content="";
		
		try {
			// get URL content
			url = new URL(urlstring);
			URLConnection conn = url.openConnection();
			// open the stream and put it into BufferedReader
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				content+=inputLine+"\n";
			}
		}catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return content;
	}
	
	String removeHtmlTag (String s) {

        return s.replaceAll("<.*?>", "   ");

    }
	
	
	public static void main (String[] args) {
		
		WikiSimilarity wikisim=new WikiSimilarity(17362, 711147);
//		String urlstring="http://wikipedia-miner.cms.waikato.ac.nz/services/compare?id1=17362&id2=89074";
//		String outpath="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikisim.xml";
////		System.out.println(wikisim.getLinkSimilarity());
//		System.out.println(wikisim.getContentGivenURL(urlstring));
//		wikisim.writeURLContentToFile(urlstring, outpath);
		
		System.out.println(wikisim.getContentGivenURL("http://data.semanticweb.org/person/nitish-aggarwal"));
		
	}
	

}
