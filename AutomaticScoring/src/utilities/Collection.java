package utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import edu.mit.jwi.item.ISynset;


/***
 * it has some operations on text document collection (a folder of files)
 * 
 * @author miaochen
 *
 */
public class Collection {
	
	String cDir;
	int totalDoc;
	
	Collection (){
		
	}
	
	Collection (String cDir){
		this.cDir=cDir;
//		then count number of non-system files in the dir
		String[] filenames=new File(cDir).list();
		totalDoc=filenames.length;
//		see if the file is a system file, if so, reduce the totalDoc by 1
		for (String filename : filenames) {
			if (filename.startsWith(".")) {
				totalDoc--;				
			}
		}
	}
	
	/***
	 * compute word idf values from the collection files
	 * 
	 * @return
	 */
	HashMap getWordIDF(SentenceSplitter splitter, SentenceTokenizer tokenizer){
		HashMap<String,Double> word_idf=new HashMap<String,Double>();
//		this stores (key=word, value=number of documents containing the word)
		HashMap<String,Integer> word_n=new HashMap<String,Integer>();
		
		String[] filenames=new File(cDir).list();
		
		for (String filename : filenames) {
			if(isSystemFile(filename)){
				continue;
			}
			System.out.println("For idf, processing "+filename);
			Document doc = new Document (new File (cDir+File.separator+filename));
			doc.toWordVectorRawFrequency(splitter, tokenizer);
			for (Iterator<String> iter=doc.word_rawfreq.keySet().iterator();iter.hasNext();){
				String token=(String) iter.next();
				if(word_n.containsKey(token)){
					int n=((Integer) word_n.get(token)).intValue();
					word_n.put(token, new Integer(n+1));
				}else{
					word_n.put(token, new Integer(1));
				}
			}
		}
		
//		idf=log(N/(n+1))
		for (Iterator iter=word_n.keySet().iterator(); iter.hasNext();){
			String token=(String) iter.next();
			int n=word_n.get(token).intValue();
			word_idf.put(token, new Double(Math.log10((double)totalDoc/(double)(1.0+n))));
		}
		
		return word_idf;
	}
	
	/***
	 * given a folder, if a word doesn't appear in the folder then estimate
	 * its default IDF value
	 * idf=log(N/(1+n))=log(N)
	 * 
	 * @return
	 */
	double getDefaultIDFForUnseenWord (){
		
		double idf=0;
//		System.out.println(totalDoc);
		if(totalDoc > 0){
			idf=Math.log10((double)totalDoc);
		}
		return idf;
		
	}
	
	/***
	 * compute Wiki concept idf for the collection
	 * 
	 * @return
	 */
	HashMap getWikiConIDF (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, int ngram){
		HashMap con_idf=new HashMap();
		HashMap con_n=new HashMap();
		
		String[] filenames=new File(cDir).list();
		for (String filename : filenames){
			if(isSystemFile(filename)){
				continue;
			}
			System.out.println("For idf, processing "+filename);
			Document doc = new Document(new File(cDir+File.separator+filename));
			doc.toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
//			put the Wiki hash to the con_n hash, key=pageid, value=number of docs that this pageid/concept appears
			for (Iterator iter=doc.con_rawfreq.keySet().iterator(); iter.hasNext(); ){
				Integer pageid=(Integer) iter.next();
				if (con_n.containsKey(pageid)){
					Integer n=(Integer) con_n.get(pageid);
					con_n.put(pageid, new Integer(n.intValue()+1));
				}else{
					con_n.put(pageid, new Integer(1));
				}
			}
		}
		
		for (Iterator iter=con_n.keySet().iterator(); iter.hasNext(); ){
			Integer pageid=(Integer) iter.next();
			Integer num=(Integer) con_n.get(pageid);
			con_idf.put(pageid, new Double(Math.log10((double)totalDoc/(double)(1.0+num.intValue()))));
		}
		return con_idf;
	}
	
	/***
	 * given a folder, if a wiki concept doesn't appear in the folder then estimate
	 * its default IDF value
	 * idf=log(N/(1+n))=log(N)
	 * 
	 * @return
	 */
	double getDefaultIDFForUnseenWikiCon (){
		
		double idf=0;
//		System.out.println(totalDoc);
		if(totalDoc > 0){
			idf=Math.log10((double)totalDoc);
		}
		return idf;
		
	}
	
	void computeFolderWikiVectors (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, HashMap con_idf, String outFolder, int ngram) {
		String[] filenames = new File (cDir).list();
		for (String filename : filenames) {
			if (isSystemFile (filename))
				continue;
			System.out.println("Computing vector weights of "+filename);
			Document doc=new Document (new File(cDir+File.separator+filename));
			doc.toWikiVectorTfIdf(wtype, splitter, tokenizer, dbcon, con_idf, ngram);
//			Then write HashMap to output file
			String outDir=outFolder+File.separator+filename;
			FileIO.writeHashMapToFile(doc.con_tfidf, outDir);
		}
	}
	
	/***
	 * compute Synset concept idf for the collection
	 * 
	 * @return
	 */
	HashMap getSynsetIDF (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, 
			WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger){
		HashMap syn_idf=new HashMap();
		HashMap syn_n=new HashMap();
		
		String[] filenames=new File(cDir).list();
		for (String filename : filenames){
//			see if this is a text file, need to remove unuseful files like ".DS_Store" on mac computers
			if(isSystemFile(filename)){
				continue;
			}
			System.out.println("For idf, processing "+filename);
			Document doc = new Document(new File(cDir+File.separator+filename));
			doc.toSynVectorRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
//			put the Synset hash to the syn_n hash, key=synset, value=number of docs that this synset/concept appears
			for (Iterator iter=doc.syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
				ISynset syn=(ISynset) iter.next();
				if (syn_n.containsKey(syn)){
					Integer n=(Integer) syn_n.get(syn);
					syn_n.put(syn, new Integer(n.intValue()+1));
				}else{
					syn_n.put(syn, new Integer(1));
				}
			}
		}
		
		for (Iterator iter=syn_n.keySet().iterator(); iter.hasNext(); ){
			ISynset syn=(ISynset) iter.next();
			Integer num=(Integer) syn_n.get(syn);
			syn_idf.put(syn, new Double(Math.log10((double)totalDoc/(double)(1.0+num.intValue()))));
		}
		return syn_idf;
	}
	
	double getDefaultIDFForUnseenSynset () {
		double idf=0;
//		System.out.println(totalDoc);
		if(totalDoc > 0){
			idf=Math.log10((double)totalDoc);
		}
		return idf;
	}
 	
	
	/***
	 * hms.get(0) is word_idf
	 * hms.get(1) is syn_idf
	 * 
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 * @param wnmatch
	 * @param doPOS
	 * @param tagger
	 * @return
	 */
	ArrayList<HashMap> getSynsetReplacingWordIDF (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, 
			WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger) {
		ArrayList<HashMap> hms=new ArrayList<HashMap> ();
		
		HashMap word_idf=new HashMap();
		HashMap word_n=new HashMap();
		HashMap syn_idf=new HashMap();
		HashMap syn_n=new HashMap();
		
		String[] filenames=new File(cDir).list();
		for (String filename : filenames){
//			see if this is a text file, need to remove unuseful files like ".DS_Store" on mac computers
			if(isSystemFile(filename)){
				continue;
			}
			System.out.println("For idf, processing "+filename);
			Document doc = new Document(new File(cDir+File.separator+filename));
			doc.toSynReplacingWordRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			
			for (Iterator iter=doc.word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				if (word_n.containsKey(token)) {
					Integer n=(Integer) word_n.get(token);
					word_n.put(token, new Integer(n.intValue()+1));
				}else{
					word_n.put(token, new Integer(1));
				}
			}			
//			put the Synset hash to the syn_n hash, key=synset, value=number of docs that this synset/concept appears
			for (Iterator iter=doc.syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
				ISynset syn=(ISynset) iter.next();
				if (syn_n.containsKey(syn)){
					Integer n=(Integer) syn_n.get(syn);
					syn_n.put(syn, new Integer(n.intValue()+1));
				}else{
					syn_n.put(syn, new Integer(1));
				}
			}
		}
		
		for (Iterator iter=word_n.keySet().iterator(); iter.hasNext(); ) {
			String token=(String) iter.next();
			Integer num=(Integer) word_n.get(token);
			word_idf.put(token, new Double (Math.log10((double)totalDoc/(double)(1.0+num.intValue()))));
		}
		
		for (Iterator iter=syn_n.keySet().iterator(); iter.hasNext(); ){
			ISynset syn=(ISynset) iter.next();
			Integer num=(Integer) syn_n.get(syn);
			syn_idf.put(syn, new Double(Math.log10((double)totalDoc/(double)(1.0+num.intValue()))));
		}
		
		hms.add(word_idf);
		hms.add(syn_idf);
		return hms;
	}
	
	/**
	 * this is a simplied version of the above method, only getting word_idf, because syn_idf is already written
	 * in previous experiments
	 * 
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 * @param wnmatch
	 * @param doPOS
	 * @param tagger
	 * @return
	 */
	HashMap getSynsetReplacingWordIDFReturnWordOnly (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, 
			WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger) {
		ArrayList<HashMap> hms=new ArrayList<HashMap> ();
		
		HashMap word_idf=new HashMap();
		HashMap word_n=new HashMap();
		
		String[] filenames=new File(cDir).list();
		for (String filename : filenames){
//			see if this is a text file, need to remove unuseful files like ".DS_Store" on mac computers
			if(isSystemFile(filename)){
				continue;
			}
			System.out.println("For idf, processing "+filename);
			Document doc = new Document(new File(cDir+File.separator+filename));
			doc.toSynReplacingWordRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
//			put to the word_n hash
			for (Iterator iter=doc.word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				if (word_n.containsKey(token)) {
					Integer n=(Integer) word_n.get(token);
					word_n.put(token, new Integer(n.intValue()+1));
				}else{
					word_n.put(token, new Integer(1));
				}
			}			
		}
		
		for (Iterator iter=word_n.keySet().iterator(); iter.hasNext(); ) {
			String token=(String) iter.next();
			Integer num=(Integer) word_n.get(token);
			word_idf.put(token, new Double (Math.log10((double)totalDoc/(double)(1.0+num.intValue()))));
		}
		return word_idf;
	}
	
/***
 * given a folder, an IDF file, compute the tfidf weighting for each file in the folder
 * and write each hash to output files
 * 
 * @param wtype
 * @param splitter
 * @param tokenizer
 * @param dbcon
 * @param wnmatch
 * @param doPOS
 * @param syn_idf
 * @param outFolder
 */
	void computeFolderSynVectors (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, HashMap syn_idf, String outFolder,
			SentenceTagger tagger) {
		int outputSynsetFormat=Params.outputSynsetFormat;
		String[] filenames = new File (cDir).list();
		for (String filename : filenames) {
			if (isSystemFile (filename))
				continue;
			System.out.println("Computing vector weights of "+filename);
			Document doc=new Document (new File(cDir+File.separator+filename));
			doc.toSynVectorTfIdfNormalized(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, syn_idf, tagger);
//			Then write HashMap to output file
			String outDir=outFolder+File.separator+filename;
			if (outputSynsetFormat==0) {
				FileIO.writeHashMapOrderByValueForSynset(doc.syn_tfidf, outDir);
			}else if (outputSynsetFormat==1) {
				FileIO.writeHashMapOrderByValue(doc.syn_tfidf, outDir);
			}						
		}
	}
	
//	void computeFolderSynReplacingWordVecs (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
//			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, HashMap word_idf, HashMap syn_idf, 
//			String outBowFolder, String outWnFolder, SentenceTagger tagger) {
//		String[] filenames = new File (cDir).list();
//		for (String filename : filenames) {
//			if (isSystemFile (filename))
//				continue;
//			System.out.println("Computing vector weights of "+filename);
//			Document doc=new Document (new File(cDir+File.separator+filename));
//			doc.toSynReplacingWordTfIdf(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, word_idf, syn_idf, tagger);
//			
////			Then write HashMap to output file
//			String outBowDir=outBowFolder+File.separator+filename;
//			String outWnDir=outWnFolder+File.separator+filename;
//			FileIO.writeHashMapToFile(doc.word_tfidf, outBowDir);
//			FileIO.writeHashMapToFile(doc.syn_tfidf, outWnDir);
//		}
//	}
	
	/***
	 * this is a simplified version for the above method,
	 * only for returning word vecs, no synset is returned
	 * 
	 * @param wtype
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 * @param wnmatch
	 * @param doPOS
	 * @param word_idf
	 * @param syn_idf
	 * @param outBowFolder
	 * @param outWnFolder
	 * @param tagger
	 */
	void computeFolderSynReplacingWordVecs (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, HashMap word_idf,String outBowFolder, 
			SentenceTagger tagger) {
		String[] filenames = new File (cDir).list();
		for (String filename : filenames) {
			if (isSystemFile (filename))
				continue;
			System.out.println("Computing vector weights of "+filename);
			Document doc=new Document (new File(cDir+File.separator+filename));
			doc.toSynReplacingWordTfIdf(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, word_idf, tagger);
			
//			Then write HashMap to output file
			String outBowDir=outBowFolder+File.separator+filename;
			FileIO.writeHashMapToFile(doc.word_tfidf, outBowDir);
		}
	}
	
	void computeFolderBowVec (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			HashMap word_idf, String outFolder) {
		String[] filenames = new File (cDir).list();
		for (String filename : filenames) {
			if (isSystemFile (filename))
				continue;
			System.out.println("Computing vector weights of "+filename);
			Document doc=new Document (new File(cDir+File.separator+filename));
			doc.toWordTfIdf(wtype, splitter, tokenizer, word_idf);
						
//			Then write HashMap to output file
			FileIO.writeHashMapOrderByValue(doc.word_tfidf, outFolder+File.separator+filename);
		}
	}
	
	
//	List<Document> folderToDocList () {
//		List<Document> docs=new ArrayList<Document> ();
//		
//		String[] filenames = new File (cDir).list();
//		for (String filename : filenames) {
//			if (isSystemFile (filename))
//				continue;
//			System.out.println("Computing vector weights of "+filename);
//			Document doc=new Document (new File(cDir+File.separator+filename));
//			docs.add(doc);
//		}
//		
//		return docs;
//	}
	
	boolean isSystemFile (String filename) {
		return filename.startsWith(".");
	}
	
	void annotateFolderByWordNet (SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger, String outFolder) {
		String[] filenames = new File (cDir).list();
		for (String filename : filenames) {
			if (isSystemFile (filename))
				continue;
			System.out.println("Annotate "+filename+" with WordNet");
			AnnotatedDoc annodoc=new AnnotatedDoc (new File(cDir+File.separator+filename));
			annodoc.annotateTextWithWN(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
//			write the annotation to output file
			FileIO.writeFile(outFolder+File.separator+filename+".html", annodoc.wnAnnoText);
		}
	}
	
	
	public static void main (String[] args) {
		/***
		 * get idf for the bow approach
		 */
		String folder="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds/098/train/train0";
		Toolbox toolbox=new Toolbox(true,true,false,true,true,false,false);
		toolbox.enableTools();
		Collection trainCol=new Collection(folder);
//		HashMap word_idf=trainCol.getWordIDF(toolbox.splitter, toolbox.tokenizer);
//		FileIO.writeHashMapOrderByValue(word_idf, "/Users/miaochen/Documents/diss-experiment/results/wntest/idf-098-train0.txt");
		
		HashMap syn_idf=trainCol.getSynsetIDF(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, 
				toolbox.wnmatch, Params.doPOS, toolbox.tagger);
		FileIO.writeHashMapOrderByValue(syn_idf, "/Users/miaochen/Documents/diss-experiment/results/wntest/idf-wn1st-098-train0.txt");
		
		
//		int wtype=5;
//		int ngram=5;
//		
//		String cDir="/Users/miaochen/Documents/diss-experiment/proc-corpus/test";
//		Collection col=new Collection(cDir);
//		
////		System.out.println("unseen word idf: "+col.getDefaultIDFForUnseenWord());
////		
////		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
////		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
////		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
////		SentenceSplitter splitter = new SentenceSplitter(spliterModel);
////		HashMap<String,Double> idf=col.getWordIDF(splitter, tokenizer);
////		for (Iterator iter=idf.keySet().iterator(); iter.hasNext(); ){
////			String token=(String) iter.next();
////			System.out.println(token+","+idf.get(token));
////		}
//		
//        System.out.println("unseen concept idf: "+col.getDefaultIDFForUnseenWikiCon());
//		
//		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
//		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
//		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
//		SentenceSplitter splitter = new SentenceSplitter(spliterModel);
//		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
//		String dbClass = "com.mysql.jdbc.Driver";
//		String usr="miao";		
//		String pwd=PasswordField.readPassword("Enter password: ");
//		System.out.println("Finished entering password");
//		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
//		
//		HashMap<Integer, Double> idf=col.getWikiConIDF(splitter, tokenizer, dbcon, ngram);
//		for (Iterator iter=idf.keySet().iterator(); iter.hasNext(); ){
//			Integer token=(Integer) iter.next();
//			System.out.println(token+","+idf.get(token).toString());
//		}
	}

}
