package utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/***
 * this collection deals with system files, by removing them from file list (type: ArrayList)
 * 
 */

/***
 * this is about operations on Wiki xml files, which are well-formed files for the
 * Wikification xml output
 * 
 * @author miaochen
 *
 */

public class WikiXmlCollection {
	
    String cDir;
    int totalDoc;
    List<String> fnames=new ArrayList<String> ();
	
	WikiXmlCollection (){
		
	}
	
	WikiXmlCollection (String cDir){
		this.cDir=cDir;
		
//		then count number of non-system files in the dir, and get list of fnames (non-system files)
		String[] filenames=new File(cDir).list();
		totalDoc=filenames.length;
//		see if the file is a system file, if so, reduce the totalDoc by 1
		for (String filename : filenames) {
			if (filename.startsWith(".")) {
				totalDoc--;				
			}else {
				fnames.add(filename);
			}
		}
	}
	
	/***
	 * compute word idf values from the collection xml annotated files
	 * 
	 * @return
	 */
	HashMap getWikiIDF () {
		HashMap<Integer, Double> wiki_idf=new HashMap<Integer, Double> ();
		HashMap<Integer, Integer> wiki_n=new HashMap<Integer, Integer> ();
		
		for (String filename : fnames){
			System.out.println("For idf, processing "+filename);
			
			WikiXmlDocument xdoc=new WikiXmlDocument (cDir+File.separator+filename,"utf-8");
			xdoc.toWikiVectorRawFrequency();
//			put the Wiki hash to the wiki_n hash, key=pageid, value=number of docs that this pageid/concept appears
			for (Iterator iter=xdoc.wiki_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				Integer wikiId=(Integer) iter.next();
				if (wiki_n.containsKey(wikiId)) {
					Integer n=(Integer) wiki_n.get(wikiId);
					wiki_n.put(wikiId, new Integer (n+1));
				}else{
					wiki_n.put(wikiId, new Integer (1));
				}
			}
		}
//		idf=log(N/(n+1))
		for (Iterator iter=wiki_n.keySet().iterator(); iter.hasNext(); ) {
			Integer wikiId=(Integer) iter.next();
			int n=wiki_n.get(wikiId).intValue();
			wiki_idf.put(wikiId, new Double (Math.log10((double)totalDoc/(double)(1.0+n))));
		}
		return wiki_idf;
		
	}
	
	double getDefaultIDFForUnseenWikiCon () {
		double idf=0;
		if(totalDoc > 0){
			idf=Math.log10((double)totalDoc);
		}
		return idf;
	}
	
	void computeFolderWikiVectors (int wtype, HashMap wiki_idf, String outFolder) {
		for (String filename : fnames) {
			System.out.println("Computing vector weights of "+filename);
			WikiXmlDocument xdoc=new WikiXmlDocument(cDir+File.separator+filename,"utf-8");
			xdoc.toWikiVectorTfidfNormalized(wtype, wiki_idf);
//			Then write HashMap to output file
			String outDir=outFolder+File.separator+removeWikificationFileExtension(filename);
//			we process the output filename a bit here, from "afile.tagged.full.xml" to "afile"
//			FileIO.writeHashMapToFile(xdoc.wiki_tfidf, outDir);
			FileIO.writeHashMapOrderByValue(xdoc.wiki_tfidf, outDir);
		}
	}
	
	/*
	 * given a file "filea.txt", the prefix name means "filea"
	 * 
	 */
	String getFilePrefixName (String filename) {
		String pre=filename;
		String[] parts=filename.split("\\.");
		if (parts.length > 0) {
			pre=parts[0];
		}
		return pre;
	}
	
	/***
	 * given a Wikification output file, convert it back to original file name
	 * e.g. afile.txt.tagged.full.xml => afile.txt
	 * 
	 * @param filename
	 * @return
	 */
	String removeWikificationFileExtension (String filename) {
		String orig=filename;
		if (filename.endsWith(".wikification.tagged.full.xml")) {
			orig=filename.replaceAll("\\.wikification\\.tagged\\.full\\.xml", "");
		}
		return orig;
	}
	
	String removeHtmlFileExtension (String filename) {
		String orig=filename;
		if (filename.endsWith(".wikification.tagged.flat.html"))
			orig=filename.replaceAll("\\.wikification\\.tagged\\.flat\\.html", "");
		return orig;
	}
	
	/***
	 * in the case of words replaced by matched Wiki concept, compute the idf values for the words
	 */
	HashMap getWordReplacedByWikiIdf (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		HashMap<String, Double> word_idf=new HashMap<String, Double> ();
		HashMap<String, Integer> word_n=new HashMap<String, Integer> ();
		
		for (String filename : fnames){
			System.out.println("For idf, processing "+filename);
			WikiXmlDocument xdoc=new WikiXmlDocument(cDir+File.separator+filename, "utf-8");
			xdoc.toWordReplacedByWikiRawFrequency(splitter, tokenizer);
			
			for (Iterator iter=xdoc.word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				if (word_n.containsKey(token)) {
					int n=(Integer)word_n.get(token).intValue();
					word_n.put(token, new Integer(n+1));
				}else
					word_n.put(token, new Integer(1));
			}
		}
//		idf=log(N/(n+1))
		for (Iterator iter=word_n.keySet().iterator(); iter.hasNext(); ) {
			String token=(String) iter.next();
			int n=word_n.get(token).intValue();
			word_idf.put(token, new Double(Math.log10((double)totalDoc)/((double)(1.0+n))));
		}
		return word_idf;
	}
	
//	HashMap getWordReplacedByWikiIdf (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
//		HashMap<String, Double> word_idf=new HashMap<String, Double> ();
//		HashMap<String, Integer> word_n=new HashMap<String, Integer> ();
//		
//		for (String filename : fnames){
//			System.out.println("For idf, processing "+filename);
//			WikiXmlDocument xdoc=new WikiXmlDocument(new File(cDir+File.separator+filename));
//			xdoc.toWordReplacedByWikiRawFrequency(splitter, tokenizer);
//			
//			for (Iterator iter=xdoc.word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
//				String token=(String) iter.next();
//				if (word_n.containsKey(token)) {
//					int n=(Integer)word_n.get(token).intValue();
//					word_n.put(token, new Integer(n+1));
//				}else
//					word_n.put(token, new Integer(1));
//			}
//		}
////		idf=log(N/(n+1))
//		for (Iterator iter=word_n.keySet().iterator(); iter.hasNext(); ) {
//			String token=(String) iter.next();
//			int n=word_n.get(token).intValue();
//			word_idf.put(token, new Double(Math.log10((double)totalDoc)/((double)(1.0+n))));
//		}
//		return word_idf;
//	}
	
	String getOriginalTranscriptName (String alteredname) {
		return alteredname.substring(0,20);
	}
	
	/***
	 * this computes tf*idf for word vectors, no need to compute tfidf for wiki vectors, because it always stays the same
	 * it's the words that are replaced by (wiki)
	 * 
	 * @param wtype
	 * @param word_idf
	 * @param outFolder
	 * @param splitter
	 * @param tokenizer
	 */
	void computeFolderWordReplacedByWiki (int wtype, HashMap word_idf, String outFolder, SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		for (String filename : fnames) {
			System.out.println("Computing vector weights of "+filename);
			WikiXmlDocument xdoc=new WikiXmlDocument(cDir+File.separator+filename, "utf-8");
			xdoc.toWordReplacedByWikiTfidf(wtype, word_idf, splitter, tokenizer);
//			Then write HashMap to output file
			String outDir=outFolder+File.separator+getOriginalTranscriptName(filename);
//			we process the output filename a bit here, from "afile.tagged.full.xml" to "afile"
			FileIO.writeHashMapToFile(xdoc.word_tfidf, outDir);
		}
	}
	
	public static void main (String[] args) {
		
		String s="7543008-VB531098.txt.wikification.tagged.full.xml";
		System.out.println(s.substring(0,20));
		
	}

	
}
