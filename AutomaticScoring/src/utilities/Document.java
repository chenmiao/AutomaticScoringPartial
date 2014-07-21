package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import common.Porter;

import edu.mit.jwi.item.ISynset;

/****
 * Document class is a speech transcript here
 * there is a series of operations on this document class
 * 
 * @author miaochen
 *
 */
public class Document {
	
	String text;
//	the hashmap for bag of words, word => frequency
	public HashMap<String,Integer> word_rawfreq;
	HashMap<String, Integer> word_boolean;
	HashMap<String,Double> word_normfreq;
	HashMap<String,Double> word_tfidf;
//	the hashmap for wikiConcept id=> frequency
	HashMap<Integer,Integer> con_rawfreq;
	HashMap<Integer, Integer> con_boolean;
	HashMap<Integer,Double> con_normfreq;
	HashMap<Integer,Double> con_tfidf;
//	the hashmap for WordNet synsets syn=>frequency
	HashMap<ISynset,Integer> syn_rawfreq;
	HashMap<ISynset,Integer> syn_boolean;
	HashMap<ISynset,Double> syn_normfreq;
	HashMap<ISynset,Double> syn_tfidf;
	
	
	Document (){
		
	}
	
	public Document (String text){
//		NOTE: here we replace spaces/tab/line break with a single " "
		this.text=text.replaceAll("\\s+", " ");		
	}
	
	Document (File afile){
//		NOTE: here we replace spaces/tab/line break with a single " "
		this.text=FileIO.getFileObjectAsString(afile).replaceAll("\\s+", " ");
//		System.out.println(text);
	}
	
	/***
	 * compute tf*idf values for vector of word concepts
	 * 
	 * @param wtype
	 * 0=RawTf*idf, 
	 * 1=(RawTf/DocLen)*idf
	 * 2=(RawTf/maximum term frequency)*idf
	 * 3=(RawTf/EucLen)*idf
	 * 4=boolean
	 * 5=(LogarithmTf)*idf
	 * 6=RawTf/EucLen (no idf involved)
	 * 
	 * NOTE: for a test set, unseen word in test set can be assigned a default idf here
	 * 
	 * @param splitter
	 * @param tokenizer
	 */
	void toWordTfIdf (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, HashMap word_idf) {
		word_tfidf=new HashMap<String,Double> ();
		
		switch (wtype) {
		case 0:
			toWordVectorRawFrequency(splitter, tokenizer);
			for(Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ){
				String token=(String) iter.next();
				int rawtf=word_rawfreq.get(token).intValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				word_tfidf.put(token, new Double(rawtf*idf));
			}
			break;
		case 1:
			toWordVectorFrequencyNormalizedByDocLength(splitter, tokenizer);
			for (Iterator iter=word_normfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				double normtf=word_normfreq.get(token).doubleValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				word_tfidf.put(token, new Double (normtf*idf));
			}
			break;
		case 2:
			break;
		case 3:
//			we also consider if there is stoplist in this case, stoplist option needs to be added for other cases
			toWordVectorRawFrequency(splitter, tokenizer);
			double norm=0;
			for(Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ){
				String token=(String) iter.next();
				int rawtf=word_rawfreq.get(token).intValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				word_tfidf.put(token, new Double(rawtf*idf));
				norm+=rawtf*idf*rawtf*idf;
			}
			double sqrt=Math.sqrt(norm);
			for (Iterator iter=word_tfidf.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				double w=word_tfidf.get(token).doubleValue();
				if (w==0) {
					word_tfidf.put(token, new Double(0.0));
				}else {
					word_tfidf.put(token, new Double(w/sqrt));
				}				
			}
			break;
		case 4:
//			the word_tfidf is filled like "word => 1.000"
			toWordVectorRawFrequency(splitter, tokenizer);
			for(Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				word_tfidf.put(token, new Double (1.0));
			}
			break;
		case 5:
			toWordVectorLogarithmFrequency(splitter, tokenizer);
			for (Iterator iter=word_normfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				double normtf=word_normfreq.get(token).doubleValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				word_tfidf.put(token, new Double (normtf*idf));
			}
			break;
		case 6:
			toWordVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer);
			word_tfidf=word_normfreq;
			break;
		default:
			System.out.println("There is something wrong with the code!");
		}
	}
	
	/***
	 * compute tf*idf values for vector of Wiki concepts
	 * 
	 * @param wtype
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
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 */
	void toWikiVectorTfIdf (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, HashMap con_idf, int ngram) {
		con_tfidf=new HashMap <Integer,Double> ();
		
		switch (wtype) {
		case 0:
			toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
			for (Iterator iter=con_rawfreq.keySet().iterator(); iter.hasNext(); ){
				Integer pageid=(Integer) iter.next();
				int rawtf=(Integer) con_rawfreq.get(pageid).intValue();
				double idf=getIdfGivenKeyAndHash(pageid,con_idf).doubleValue();
				con_tfidf.put(pageid, new Double(rawtf*idf));
			}
			break;
		case 1:
			toWikiVectorFrequencyNormalizedByDocLength(splitter, tokenizer, dbcon, ngram);
			for (Iterator iter=con_normfreq.keySet().iterator(); iter.hasNext(); ){
				Integer pageid=(Integer) iter.next();
				double normtf=(Double) con_normfreq.get(pageid).doubleValue();
				double idf=getIdfGivenKeyAndHash(pageid,con_idf).doubleValue();
				con_tfidf.put(pageid, new Double(normtf*idf));
			}
			break;
		case 2:
//			tf/maximum tf, not implemented yet
			break;
		case 3:
			toWikiVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer, dbcon, ngram);
			for (Iterator iter=con_normfreq.keySet().iterator(); iter.hasNext(); ){
				Integer pageid=(Integer) iter.next();
				double normtf=(Double) con_normfreq.get(pageid).doubleValue();
				double idf=getIdfGivenKeyAndHash(pageid,con_idf).doubleValue();;
				con_tfidf.put(pageid, new Double(normtf*idf));
			}
			break;
		case 4:
			toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
			for (Iterator iter=con_rawfreq.keySet().iterator(); iter.hasNext(); ){
				Integer pageid=(Integer) iter.next();
				con_tfidf.put(pageid, new Double(1.0));
			}
			break;
		case 5:
			toWikiVectorLogarithmFrequency(splitter, tokenizer, dbcon, ngram);
			for (Iterator iter=con_normfreq.keySet().iterator(); iter.hasNext(); ){
				Integer pageid=(Integer) iter.next();
				double normtf=(Double) con_normfreq.get(pageid).doubleValue();
				double idf=getIdfGivenKeyAndHash(pageid,con_idf).doubleValue();
				con_tfidf.put(pageid, new Double(normtf*idf));
			}
			break;
		default:
			System.out.println("There is something wrong with the code!");
		}
	}
	
	/***
	 * compute tf*idf values for vector of Wiki concepts
	 * 
	 * @param wtype
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
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 */
	void toSynVectorTfIdfNormalized (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, HashMap syn_idf, SentenceTagger tagger) {
		syn_tfidf=new HashMap <ISynset,Double> ();
		
		switch (wtype) {
		case 0:
			toSynVectorRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			for (Iterator iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
				ISynset syn=(ISynset) iter.next();
				int rawtf=(Integer) syn_rawfreq.get(syn).intValue();
				double idf=getIdfGivenKeyAndHash(syn,syn_idf).doubleValue();
				syn_tfidf.put(syn, new Double(rawtf*idf));
			}
			break;
		case 1:
//			need to implement
			break;
		case 2:
//			tf/maximum tf, not implemented yet
			break;
		case 3:
			toSynVectorRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			double norm=0;
			for (Iterator iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
				ISynset syn=(ISynset) iter.next();
				int rawtf=(Integer) syn_rawfreq.get(syn).intValue();
				double idf=getIdfGivenKeyAndHash(syn,syn_idf).doubleValue();
				syn_tfidf.put(syn, new Double(rawtf*idf));
				norm+=rawtf*idf*rawtf*idf;
			}
			double sqrt=Math.sqrt(norm);
			for (Iterator iter=syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
				ISynset syn=(ISynset) iter.next();
				double w=(Double) syn_tfidf.get(syn).doubleValue();
				if (sqrt==0) {
					syn_tfidf.put(syn, new Double(0.0));
				}else{
					syn_tfidf.put(syn, new Double(w/sqrt));
				}				
			}
			break;
		case 4:
			toSynVectorRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			for (Iterator iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
				ISynset syn=(ISynset) iter.next();
				syn_tfidf.put(syn, new Double(1.0));
			}
			break;
		case 5:
//			not implemented yet
			break;
		case 6: 
			toSynVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			syn_tfidf=syn_normfreq;
		    break;
		default:
			System.out.println("There is something wrong with the code!");
		}
	}
	
	void toSynVectorTfIdf (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, HashMap syn_idf, SentenceTagger tagger) {
		
        syn_tfidf=new HashMap <ISynset,Double> ();
		
		switch (wtype) {
		case 3:
			toSynVectorRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			double norm=0;
			for (Iterator iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
				ISynset syn=(ISynset) iter.next();
				int rawtf=(Integer) syn_rawfreq.get(syn).intValue();
				double idf=getIdfGivenKeyAndHash(syn,syn_idf).doubleValue();
				syn_tfidf.put(syn, new Double(rawtf*idf));
				norm+=rawtf*idf*rawtf*idf;
			}
			break;
		default:
			System.out.println("There is something wrong with the code!");
		}
			
	}
	
	/**
	 * it normalizes word_tfidf, syn_tfidf, when they're not null
	 * 
	 */
	void normalizeTfIdf () {
		if (word_tfidf != null) {
			
		}
		
		if (syn_tfidf != null) {
			double norm=0;
			for (Iterator iter=syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
				ISynset syn=(ISynset) iter.next();
				double w=syn_tfidf.get(syn).doubleValue();
				norm+=w*w;
			}
			double sqrt=Math.sqrt(norm);
			for (Iterator iter=syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
				ISynset syn=(ISynset) iter.next();
				double w=syn_tfidf.get(syn).doubleValue();
				if (sqrt==0) {
					syn_tfidf.put(syn, new Double(0.0));
				}else {
					syn_tfidf.put(syn, new Double(w/sqrt));
				}
			}
		}
		
	}
	
	
	/***
	 * if a word has a match in WordNet then use this WN synset in the vector,
	 * else use the original word in the vector
	 * 
	 * I now decide not to change syn_tfidf here, seems unnecessary, because it's already calculated in wn1st and wnpos
	 * 
	 */
	void toSynReplacingWordTfIdf (int wtype, SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, HashMap word_idf, SentenceTagger tagger) {
		word_tfidf=new HashMap <String, Double> ();
//		syn_tfidf=new HashMap <ISynsetID,Double> ();
		
		switch (wtype) {
		case 0:
			toSynReplacingWordRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			for (Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				int rawtf=(Integer) word_rawfreq.get(token).intValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				word_tfidf.put(token, new Double((double)rawtf*idf));
			}
//			for (Iterator iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
//				ISynsetID syn=(ISynsetID) iter.next();
//				int rawtf=(Integer) syn_rawfreq.get(syn).intValue();
//				double idf=getIdfGivenKeyAndHash(syn,syn_idf).doubleValue();
//				syn_tfidf.put(syn, new Double(rawtf*idf));
//			}
			break;
		case 1:
//			need to implement
			break;
		case 2:
//			tf/maximum tf, not implemented yet
			break;
		case 3:
			toSynReplacingWordRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
			double norm=0;
			for (Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				int rawtf=(Integer) word_rawfreq.get(token).intValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				norm+=(double)rawtf*idf*(double)rawtf*idf;
			}
			double sqrt=Math.sqrt(norm);
			for (Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
				String token=(String) iter.next();
				int rawtf=(Integer) word_rawfreq.get(token).intValue();
				double idf=getIdfGivenKeyAndHash(token,word_idf).doubleValue();
				if (sqrt==0) {
					word_tfidf.put(token, new Double (0.0));
				}else {
					word_tfidf.put(token, new Double ((double)rawtf*idf/sqrt));
				}				
			}
			break;
		case 4:
			toSynReplacingWordRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
//			need to revise to adapt to bow+wn vectors
//			for (Iterator iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ){
//				ISynsetID syn=(ISynsetID) iter.next();
//				syn_tfidf.put(syn, new Double(1.0));
//			}
			break;
		case 5:
//			not implemented yet
			break;
		default:
			System.out.println("There is something wrong with the code!");
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
	
	/***
	 * the weighting schema = raw tf
	 * 
	 * @param splitter
	 * @param tokenizer
	 */
	public void toWordVectorRawFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		boolean stemming=Params.stemming;
		Porter porter = new Porter();  //could make this more efficient, e.g. in Toolbox
		
		word_rawfreq=new HashMap<String,Integer>();
		if (Params.useStoplistOnBow) {
			ArrayList<String> stopwords=new ArrayList<String> ();
			try{
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(Params.stoplist))));			
				String strLine;
				while((strLine=br.readLine())!=null){
					stopwords.add(strLine.trim());
				}				
				br.close();
			}catch (Exception e){
				System.err.println("Error: "+e.getMessage());
			}
			
			String[] sentences=splitter.split(text);
			for (String sentence : sentences) {
				String[] tokens=tokenizer.tokenize(sentence).split("\\s+");
				
				for (String token : tokens){
//					we first lowercase the token
					token=token.toLowerCase();
//					examine if this token is a punctuation, also skip stopwords
					if(isPunctuation(token) || stopwords.contains(token)){
						continue;
					}
					if (stemming) {  //do stemming here
						token=porter.stripAffixes(token);
					}
					
					if (word_rawfreq.containsKey(token)){
						int freq=word_rawfreq.get(token).intValue();
						word_rawfreq.put(token, new Integer(freq+1));
					}else{
						word_rawfreq.put(token, new Integer(1));
					}
				}
			}
			
		} else {
			String[] sentences=splitter.split(text);
			for (String sentence : sentences) {
				String[] tokens=tokenizer.tokenize(sentence).split("\\s+");
				
				for (String token : tokens){
//					examine if this token is a punctuation
					if(isPunctuation(token)){
						continue;
					}
//					here we lowercase the token
					token=token.toLowerCase();
					if (stemming) {  //do stemming here
						token=porter.stripAffixes(token);
					}				
					
					if (word_rawfreq.containsKey(token)){
						int freq=word_rawfreq.get(token).intValue();
						word_rawfreq.put(token, new Integer(freq+1));
					}else{
						word_rawfreq.put(token, new Integer(1));
					}
				}
			}
		}		

	}
	
	void toWordVectorFrequencyNormalizedByEuclideanLength (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		word_normfreq=new HashMap<String,Double> ();
		toWordVectorRawFrequency(splitter, tokenizer);
		
		int norm=0;
		for (Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ){
			String token=(String) iter.next();
			int rawfreq=word_rawfreq.get(token).intValue();
			norm+=rawfreq*rawfreq;
			word_normfreq.put(token, new Double ((double) rawfreq));
		}
		double sqrtnorm=Math.sqrt((double)norm);
		
		for (Iterator iter=word_normfreq.keySet().iterator(); iter.hasNext(); ){
			String token=(String) iter.next();
			double rawfreq=word_normfreq.get(token).doubleValue();
			word_normfreq.put(token, new Double (rawfreq/sqrtnorm));
		}
	}
	
	void toWordVectorFrequencyNormalizedByDocLength (SentenceSplitter splitter, SentenceTokenizer tokenizer){
		word_normfreq=new HashMap<String,Double> ();
		toWordVectorRawFrequency(splitter, tokenizer);
		int docLen=getDocLength(splitter, tokenizer);
		
		for (Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ){
			String token=(String) iter.next();
			int rawfreq=word_rawfreq.get(token).intValue();
			word_normfreq.put(token, new Double ((double)rawfreq/(double)docLen));
		}
		
	}
	
	void toWordVectorLogarithmFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		word_normfreq=new HashMap<String,Double> ();
		toWordVectorRawFrequency(splitter, tokenizer);
		
		for (Iterator iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ){
			String token=(String) iter.next();
			int rawfreq=word_rawfreq.get(token).intValue();
			word_normfreq.put(token, new Double (1.0+Math.log10((double)rawfreq)));
		}
	}
	
	boolean isPunctuation (String token){
		return token.equals(".")||token.equals("!")||token.equals("?")||token.equals(",");
	}
	
	/***
	 * the weighting schema = raw tf
	 * 
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 */
	void toWikiVectorRawFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, int ngram) {
		con_rawfreq=new HashMap<Integer,Integer>();
		
		String[] sentences = splitter.split(text);
		for (String sentence : sentences){
			WikiMatching wmatch = new WikiMatching (sentence, tokenizer, dbcon);
			List<Integer> pageids=wmatch.match(ngram);
//			put the matched ids in hash and count frequency (there might be duplicated id in the List)
			if (pageids != null){
				for (Integer pageid : pageids){
					if (con_rawfreq.containsKey(pageid)){
						Integer freq=(Integer) con_rawfreq.get(pageid);
						con_rawfreq.put(pageid, new Integer (freq.intValue()+1));
					}else{
						con_rawfreq.put(pageid, new Integer (1));
					}
				}
				
			}
			
		}
		
	}
	
	/***
	 * weight=tf/doc vector Euclidean length
	 * 
	 */
	void toWikiVectorFrequencyNormalizedByEuclideanLength (SentenceSplitter splitter, 
			SentenceTokenizer tokenizer, DBConnection dbcon, int ngram){
		con_normfreq=new HashMap<Integer,Double>();
//		first convert the text to raw tf, saved in the con_rawfreq hashmap
		toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
		
		int norm=0;
		for (Iterator<Integer> iter=con_rawfreq.keySet().iterator(); iter.hasNext();){
			Integer con=iter.next();
			int rawfreq=con_rawfreq.get(con).intValue();
			norm+=rawfreq*rawfreq;
			
			con_normfreq.put(con, new Double((double)rawfreq));
		}
		double sqrtNorm=Math.sqrt((double)norm);
		
		for (Iterator<Integer> iter=con_normfreq.keySet().iterator(); iter.hasNext();){
			Integer con=iter.next();
			double w=con_normfreq.get(con).doubleValue();
			con_normfreq.put(con, new Double((double)w/sqrtNorm));
		}
	}
	
	/***
	 * weight=tf/doc vector document length
	 * 
	 */
	void toWikiVectorFrequencyNormalizedByDocLength (SentenceSplitter splitter, 
			SentenceTokenizer tokenizer, DBConnection dbcon, int ngram){
		con_normfreq=new HashMap<Integer,Double>();
//		first convert the text to raw tf, saved in the con_rawfreq hashmap
		toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
		int docLen=getDocLength(splitter, tokenizer);
		
		if (docLen > 0) {
			for (Iterator<Integer> iter=con_rawfreq.keySet().iterator(); iter.hasNext();){
				Integer con=iter.next();
				int rawfreq=con_rawfreq.get(con).intValue();
				
				con_normfreq.put(con, new Double((double)rawfreq/(double)docLen));
			}
		}		
	}
	
	/***
	 * weight=1+log(tf), logarithm tf
	 * it basically takes results of toVectorVectorRawFrequency() and log the values
	 * 
	 */
	void toWikiVectorLogarithmFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer, 
			DBConnection dbcon, int ngram){
		con_normfreq=new HashMap<Integer,Double>();
//		first convert the text to raw tf, saved in the con_rawfreq hashmap
		toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
		
		for (Iterator<Integer> iter=con_rawfreq.keySet().iterator(); iter.hasNext();){
			Integer con=iter.next();
			int rawfreq=con_rawfreq.get(con).intValue();
			
			con_normfreq.put(con, new Double(1.0+Math.log10((double)rawfreq)));
		}		
	}
	
	/***
	 * get the frequencies of wordnet synsets, the Hash key is changed to <ISynset>, previously was <ISynsetID>
	 * 
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 * @param ngram
	 * @param doPOS it's about whether do POS on sentence before matching to WordNet
	 *        true=do POS and then match to WN senses
	 *        false=no pos, just get the 1st sense in WN
	 * 
	 */
	void toSynVectorRawFrequency (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, 
			WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger) {
		boolean useStoplist=Params.useStoplistOnBow;
		
		syn_rawfreq=new HashMap<ISynset,Integer>();		
		String[] sentences = splitter.split(text);
		
		ArrayList<String> stopwords=null;
		if (useStoplist) {
			stopwords=new ArrayList<String> ();
			try{
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(Params.stoplist))));			
				String strLine;
				while((strLine=br.readLine())!=null){
					stopwords.add(strLine.trim());
				}				
				br.close();
			}catch (Exception e){
				System.err.println("Error: "+e.getMessage());
			}
		}
		
//		get the first sense
		if (doPOS == false) {
			for (String sentence : sentences){
				List<String> tokens=tokenizer.tokenizeToList(sentence);
				for (String token : tokens) {
					if (useStoplist && stopwords.contains(token)) {
						continue;
					}
					
					ISynset syn=wnmatch.getFirstSynsetInWordnetGivenAWord(token, dbcon);
//					didn't find this word in WordNet
					if (syn == null)
						continue;
					
					if (syn_rawfreq.containsKey(syn)) {
						Integer freq=syn_rawfreq.get(syn);
						syn_rawfreq.put(syn, new Integer(freq.intValue()+1));
					}else {
						syn_rawfreq.put(syn, new Integer(1));
					}
				}
			}
		} else if (doPOS == true) {//first do POS
			for (String sentence : sentences) {
//				put tokens in a list, and corresponding tags in another list
				ArrayList<ArrayList<String>> lists=tagger.getTokensAndTagsInSeparateListsNoPunctuation(
						tokenizer.tokenize(sentence));
				ArrayList<String> tokens=lists.get(0);
				ArrayList<String> tags=lists.get(1);
				int len=tokens.size(); //size of a token or tag list
				for (int i=0; i< len; i++) {
					ISynset syn=wnmatch.getSynsetInWordnetGivenAWordAndPos(
							tokens.get(i), dbcon, tags.get(i).toUpperCase());
//					if there is no such word+pos in WordNet
					if (syn == null) 
						continue;
					
					if (syn_rawfreq.containsKey(syn)) {
						Integer freq=syn_rawfreq.get(syn);
						syn_rawfreq.put(syn, new Integer(freq.intValue()+1));
					}else {
						syn_rawfreq.put(syn, new Integer(1));
					}
				}
			}
		}		
		
	}
	
	/***
	 * replacing words with matched WN concept (if there is a match)
	 * it changes 2 things: word_rawfreq, syn_rawfreq
	 * 
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 * @param wnmatch
	 * @param doPOS
	 * @param tagger
	 */
	void toSynReplacingWordRawFrequency(SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, 
			WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger) {
		word_rawfreq=new HashMap<String, Integer> ();
		syn_rawfreq=new HashMap<ISynset,Integer>();		
		String[] sentences = splitter.split(text);
		
//		get the first sense
		if (doPOS == false) {
			for (String sentence : sentences){
				List<String> tokens=tokenizer.tokenizeToList(sentence);
				for (String token : tokens) {
					if(isPunctuation(token)){
						continue;
					}
//					here we lowercase the token
					token=token.toLowerCase();	
					ISynset syn=wnmatch.getFirstSynsetInWordnetGivenAWord(token, dbcon);
//					didn't find this word in WordNet, also add this word to BOW vector
					if (syn == null) {
						if (word_rawfreq.containsKey(token)) {
							Integer freq=word_rawfreq.get(token);
							word_rawfreq.put(token, new Integer(freq.intValue()+1));
						}else
							word_rawfreq.put(token, new Integer(1));
						continue;
					}						
//					found this word in WordNet, then add this matching to WN vector, but not adding to the bow vec
					if (syn_rawfreq.containsKey(syn)) {
						Integer freq=syn_rawfreq.get(syn);
						syn_rawfreq.put(syn, new Integer(freq.intValue()+1));
					}else {
						syn_rawfreq.put(syn, new Integer(1));
					}
				}
			}
		} else if (doPOS == true) {//first do POS
			for (String sentence : sentences) {
//				put tokens in a list, and corresponding tags in another list
				ArrayList<ArrayList<String>> lists=tagger.getTokensAndTagsInSeparateListsNoPunctuation(
						tokenizer.tokenize(sentence));
				ArrayList<String> tokens=lists.get(0);
				ArrayList<String> tags=lists.get(1);
				int len=tokens.size(); //size of a token or tag list
				for (int i=0; i< len; i++) {
					ISynset syn=wnmatch.getSynsetInWordnetGivenAWordAndPos(
							tokens.get(i), dbcon, tags.get(i).toUpperCase());
//					if there is no such word+pos in WordNet, then add the word to the bow vec
					if (syn == null) {
						String token=tokens.get(i);
						if (word_rawfreq.containsKey(token)) {
							Integer freq=word_rawfreq.get(token);
							word_rawfreq.put(token, new Integer(freq.intValue()+1));
						}else
							word_rawfreq.put(token, new Integer(1));
						continue;
					}						
//					if there is such word+pos combination in WordNet, then add the syn to wn vec
					if (syn_rawfreq.containsKey(syn)) {
						Integer freq=syn_rawfreq.get(syn);
						syn_rawfreq.put(syn, new Integer(freq.intValue()+1));
					}else {
						syn_rawfreq.put(syn, new Integer(1));
					}
				}
			}
		}
	}
	
	/***
	 * weight=tf/doc vector Euclidean length
	 * 
	 */
	void toSynVectorFrequencyNormalizedByEuclideanLength (SentenceSplitter splitter, 
			SentenceTokenizer tokenizer, DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger){
		syn_normfreq=new HashMap<ISynset, Double> ();
		toSynVectorRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
		
		int norm=0;
		for (Iterator<ISynset> iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ) {
			ISynset syn=iter.next();
			int rawfreq=syn_rawfreq.get(syn).intValue();
			norm+=rawfreq*rawfreq;
			
			syn_normfreq.put(syn, new Double((double) rawfreq));
		}
		double sqrtnorm=Math.sqrt(norm);
		
		for (Iterator<ISynset> iter=syn_normfreq.keySet().iterator(); iter.hasNext(); ) {
			ISynset syn=iter.next();
			double w=syn_normfreq.get(syn).doubleValue();
			syn_normfreq.put(syn, new Double ((double)w/sqrtnorm));
		}
	}
	
	/***
	 * the wordnet replacing word case
	 * 
	 * @param splitter
	 * @param tokenizer
	 * @param dbcon
	 * @param wnmatch
	 * @param doPOS
	 * @param tagger
	 */
	void toSynReplacingWordFrequencyNormalizedByEuclideanLength(SentenceSplitter splitter, 
			SentenceTokenizer tokenizer, DBConnection dbcon, WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger) {
		word_normfreq=new HashMap<String, Double> ();
		syn_normfreq=new HashMap<ISynset, Double> ();
		toSynReplacingWordRawFrequency(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger);
		
		int normW=0;
		for (Iterator<String> iter=word_rawfreq.keySet().iterator(); iter.hasNext(); ) {
			String token=iter.next();
			int rawfreq=word_rawfreq.get(token).intValue();
			normW+=rawfreq*rawfreq;
			word_normfreq.put(token, new Double((double) rawfreq));
		}
		double sqrtNormW=Math.sqrt(normW);
		for (Iterator<String> iter=word_normfreq.keySet().iterator(); iter.hasNext(); ) {
			String token=iter.next();
			double w=word_normfreq.get(token).doubleValue();
			word_normfreq.put(token, new Double ((double)w/sqrtNormW));
		}
		
		int normWn=0;
		for (Iterator<ISynset> iter=syn_rawfreq.keySet().iterator(); iter.hasNext(); ) {
			ISynset syn=iter.next();
			int rawfreq=syn_rawfreq.get(syn).intValue();
			normWn+=rawfreq*rawfreq;
			
			syn_normfreq.put(syn, new Double((double) rawfreq));
		}
		double sqrtnormWn=Math.sqrt(normWn);		
		for (Iterator<ISynset> iter=syn_normfreq.keySet().iterator(); iter.hasNext(); ) {
			ISynset syn=iter.next();
			double w=syn_normfreq.get(syn).doubleValue();
			syn_normfreq.put(syn, new Double ((double)w/sqrtnormWn));
		}
	}
	
	int getDocLength (SentenceSplitter splitter, SentenceTokenizer tokenizer) {
		String[] sentences = splitter.split(text);
		String tokened="";
		
		for (String sentence : sentences){
			tokened+=tokenizer.tokenize(sentence);
		}
		
		if (!tokened.equals("")){
			return tokened.trim().split(" ").length;
		}else{
			return 0;
		}
				
	}
	
	public static void main (String[] args){
		
		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
		SentenceSplitter splitter = new SentenceSplitter(spliterModel);
		int ngram=5;
		
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
		
//		String text="I like like chocolate. Chocolate is my favorite food. Chocolate is so sweet".toLowerCase();
		Document doc=new Document(new File("/Users/miaochen/Documents/diss-experiment/proc-corpus/test/4.txt"));
		doc.toWikiVectorRawFrequency(splitter, tokenizer, dbcon, ngram);
//		doc.toWordVectorRawFrequency(splitter, tokenizer);
//		doc.toVectorRawFrequency(splitter, tokenizer, dbCon);
//		doc.toWikiVectorLogarithmFrequency(splitter, tokenizer, dbCon);
//		doc.toVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer, dbCon);
//		doc.toVectorFrequencyNormalizedByDocLength(splitter, tokenizer, dbCon);
		
		System.out.println(doc.con_rawfreq.size());
		for (Iterator iter=doc.con_rawfreq.keySet().iterator(); iter.hasNext(); ){
			Integer pageid=(Integer) iter.next();
			System.out.println(pageid.toString()+","+doc.con_rawfreq.get(pageid).toString());
		}
		
//		System.out.println(doc.word_rawfreq.size());
//		for (Iterator iter=doc.word_rawfreq.keySet().iterator(); iter.hasNext(); ){
//			String token=(String) iter.next();
//			System.out.println(token+","+doc.word_rawfreq.get(token).toString());
//		}
		
	}

}
