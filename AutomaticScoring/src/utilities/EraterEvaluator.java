package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;


public class EraterEvaluator {
	
	String trainFolderText=null;
	String testFolderText=null;
	String superFolderText=null;
	
	String trainFolderWiki=null;  //folders storing wikification xml files, or flat.html files (this is for wiki replacing word case)
	String testFolderWiki=null;
	String superFolderWiki=null;
	
	String trainFolderEsa=null;  //folders storing esa vector files
	String testFolderEsa=null;
	String superFolderEsa=null;

	EraterResults eResults=null;
	HashMap<String,Integer> file_score=new HashMap<String, Integer>();
	int wType=Params.wType;
	
    EraterEvaluator (String trainFolderText, String testFolderText, String superFolderText, 
    		String trainFolderWiki, String testFolderWiki, String superFolderWiki,
    		String trainFolderEsa, String testFolderEsa, String superFolderEsa,
    		EraterResults eResults) {
    	this.trainFolderText=trainFolderText;
    	this.testFolderText=testFolderText;
		this.superFolderText=superFolderText;
		this.trainFolderWiki=trainFolderWiki;
		this.testFolderWiki=testFolderWiki;
		this.superFolderWiki=superFolderWiki;
		this.trainFolderEsa=trainFolderEsa;
    	this.testFolderEsa=testFolderEsa;
		this.superFolderEsa=superFolderEsa;
    	this.eResults=eResults;
    	try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(Params.scoreList))));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] units = strLine.trim().split(",");
				if (units.length == 2) {
					file_score.put(units[0], Integer.valueOf(units[1]));
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//    this constructor is still usable for now, but may need to be changed in the future...
	EraterEvaluator (String trainFolderText, String testFolderText, String superFolderText, EraterResults eResults) {
		
		this (trainFolderText, testFolderText, superFolderText, null, null, null, null, null, null, eResults);

	}
	

	
	void getEvaluatioForBow (Toolbox toolbox) {
//		get the idf from train collection
		Collection trainCol=new Collection(trainFolderText);
		HashMap word_idf=trainCol.getWordIDF(toolbox.splitter, toolbox.tokenizer);
		ArrayList<Document> superdocs=computeBowSuperVecs(toolbox.splitter, toolbox.tokenizer, word_idf);
		String[] testfiles=new File(testFolderText).list();
		for (String testfile : testfiles) {
			System.out.println("Test file: "+testfile);
			Document testdoc=new Document (new File (testFolderText+File.separator+testfile));
			testdoc.toWordTfIdf(wType, toolbox.splitter, toolbox.tokenizer, word_idf);
			
//			if (Params.writeVec) {
//				FileIO.writeHashMapOrderByValue(testdoc.word_tfidf, outDir);
//			}
			
			EraterFeatureProcessor eraterProc=new EraterFeatureProcessor (testdoc, superdocs);
			eraterProc.getFeaturesWord();
//			this is the actual score, and maxcos is the predicted score of the speech transcript
			int score=getScoreGivenFilename(testfile);
//			add the maxcos & cosw4 features to result record
			eResults.addFeatureResults(eraterProc.maxcos, eraterProc.cosw4, score);
//			add classification info to the confusion matrix
			eResults.addToConfusionMatrix(eraterProc.maxcos, score);
//			this is the csv output for storing 2 feature values
			eResults.addToFeatsOut(testfile, eraterProc.maxcos, eraterProc.cosw4, score);
		}
	}
	
	void getEvaluationForWn (Toolbox toolbox) {
//		get the idf from train collection
		Collection trainCol=new Collection(trainFolderText);
		HashMap syn_idf=trainCol.getSynsetIDF(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger); 
		double dftIDF=trainCol.getDefaultIDFForUnseenSynset();
//		get wn vector for the super files
		ArrayList<Document> superdocs=computeWnSuperVecs(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, toolbox.tagger, syn_idf);
		String[] testfiles=new File(testFolderText).list();
		for (String testfile : testfiles) {
			System.out.println("Test file: "+testfile);
//			get wn vector for the test file
			Document testdoc=new Document (new File (testFolderText+File.separator+testfile));
			testdoc.toSynVectorTfIdf(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, syn_idf, toolbox.tagger);
			EraterFeatureProcessor eraterProc=new EraterFeatureProcessor (testdoc, superdocs, toolbox);
			if (Params.expand) {  //reasoning case
				eraterProc.getFeaturesWnReasoning(syn_idf,dftIDF);
			}else { //non-reasoning case
				eraterProc.getFeaturesWn();
			}			
//			this is the actual score, and maxcos is the predicted score of the speech transcript
			int score=getScoreGivenFilename(testfile);
//			add the maxcos & cosw4 features to result record
			eResults.addFeatureResults(eraterProc.maxcos, eraterProc.cosw4, score);
//			add classification info to the confusion matrix
			eResults.addToConfusionMatrix(eraterProc.maxcos, score);
//			this is the csv output for storing 2 feature values
			eResults.addToFeatsOut(testfile, eraterProc.maxcos, eraterProc.cosw4, score);
		}
	}
	
	void getEvaluationForWiki (Toolbox toolbox) {
//		get idf from train collection
		WikiXmlCollection trainCol=new WikiXmlCollection(trainFolderWiki);
		HashMap wiki_idf=trainCol.getWikiIDF();
		double dftIDF=trainCol.getDefaultIDFForUnseenWikiCon();
//		get wiki vector from the super files
		ArrayList<WikiXmlDocument> superdocs=computeWikiSuperVecs(wiki_idf);
		String[] testfiles=new File(testFolderWiki).list(); //here may have problem of .ds_store document
		for (String testfile : testfiles) {
			System.out.println("Test file: "+testfile);
//			get wiki vector for the test file
			WikiXmlDocument testdoc=new WikiXmlDocument(testFolderWiki+File.separator+testfile,"utf-8");
			testdoc.toWikiVectorTfidf(Params.wType, wiki_idf);
			EraterFeatureProcessor eraterProc=new EraterFeatureProcessor(testdoc, superdocs);
			if (Params.expand) {
				eraterProc.getFeaturesWikiReasoning(wiki_idf, dftIDF, toolbox);
			}else {
				eraterProc.getFeaturesWiki(); 
			}			
			int score=getScoreGivenFilename(testfile.replaceAll("\\.wikification\\.tagged\\.full\\.xml", ""));
			eResults.addFeatureResults(eraterProc.maxcos,eraterProc.cosw4, score);
			eResults.addToConfusionMatrix(eraterProc.maxcos, score);
			eResults.addToFeatsOut(testfile, eraterProc.maxcos, eraterProc.cosw4, score);
		}
	}
	
	/***
	 * vecs="bow,wn,wiki" (for example), unfinished ...
	 * 
	 * @param vecs
	 */
	//****** this needs to be finished!!!
	void getEvaluationForCombined (Toolbox toolbox) {
//		need to consider if hasBow==null etc. case...
		Collection trainColText=new Collection(trainFolderText);
		HashMap word_idf=trainColText.getWordIDF(toolbox.splitter, toolbox.tokenizer);
		ArrayList<Document> superdocsBow=computeBowSuperVecs(toolbox.splitter, toolbox.tokenizer, word_idf);
		
		HashMap syn_idf=trainColText.getSynsetIDF(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger); 
//		get wn vector for the super files
		ArrayList<Document> superdocsWn=computeWnSuperVecs(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, toolbox.tagger, syn_idf);
		
		WikiXmlCollection trainColWiki=new WikiXmlCollection(trainFolderWiki);
		HashMap wiki_idf=trainColWiki.getWikiIDF();
//		get wiki vector from the super files
		ArrayList<WikiXmlDocument> superdocsWiki=computeWikiSuperVecs(wiki_idf);
		
//		***need to get super CombineDocument
		
		String[] testfiles=new File(testFolderText).list(); //here may have problem of .ds_store document
		for (String testfile : testfiles) {
			System.out.println("Test file: "+testfile);
			Document testdocBow=new Document (new File (testFolderText+File.separator+testfile));
			WikiXmlDocument testdocWiki=new WikiXmlDocument(testFolderWiki+File.separator+testfile,"utf-8");
			
			testdocBow.toWordTfIdf(wType, toolbox.splitter, toolbox.tokenizer, word_idf);
			testdocBow.toSynVectorTfIdfNormalized(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, syn_idf, toolbox.tagger);
			//****need to decide set which hashmap to null, according to Params.
		}
	}
	
	
	ArrayList<Document> computeBowSuperVecs (SentenceSplitter splitter, SentenceTokenizer tokenizer, HashMap word_idf) {
		ArrayList<Document> superdocs=new ArrayList<Document> ();
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			Document superdoc=new Document(new File(superFolderText+File.separator+Integer.toString(score)));
			superdoc.toWordTfIdf(wType, splitter, tokenizer, word_idf);
			superdocs.add(superdoc);
		}
		return superdocs;
	}
	
	
	ArrayList<Document> computeWnSuperVecs (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, WordNetMatching wnmatch
			, SentenceTagger tagger, HashMap syn_idf) {
		ArrayList<Document> superdocs=new ArrayList<Document> ();
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			Document superdoc=new Document(new File(superFolderText+File.separator+Integer.toString(score)));
			superdoc.toSynVectorTfIdf(Params.wType, splitter, tokenizer, dbcon, wnmatch, Params.doPOS, syn_idf, tagger);
			superdocs.add(superdoc);
		}
		return superdocs;
	}
	
	ArrayList<WikiXmlDocument> computeWikiSuperVecs (HashMap wiki_idf) {
		ArrayList<WikiXmlDocument> superdocs=new ArrayList<WikiXmlDocument> ();
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			WikiXmlDocument superdoc=new WikiXmlDocument(superFolderWiki+File.separator+Integer.toString(score)+".wikification.tagged.full.xml","utf-8");
			superdoc.toWikiVectorTfidf(Params.wType, wiki_idf);
			superdocs.add(superdoc);
		}
		return superdocs;
	}
	
	int getScoreGivenFilename(String filename) {
		int score = -1;
		String f = filename.substring(0, filename.length() - 4);

		if (file_score.containsKey(f)) {
			score = file_score.get(f);
		}
		return score;
	}
	
	
	
	public static void main (String[] args) {
		
	}

}
