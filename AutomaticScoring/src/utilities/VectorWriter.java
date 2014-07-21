package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/***
 * the class is something like "middle-ware" to output vectors to designated folders
 * 
 * !!! the esa folders are not in use now
 * 
 * @author miaochen
 *
 */

public class VectorWriter {
	
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
	
	VectorWriter (String trainFolderText, String testFolderText, String superFolderText, 
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
	
//    this constructor is for document text folders only
	VectorWriter (String trainFolderText, String testFolderText, String superFolderText, EraterResults eResults) {
		
		this (trainFolderText, testFolderText, superFolderText, null, null, null, null, null, null, eResults);

	}
	
	void getVecsForBow (Toolbox toolbox, String prompt, String fold) {
		
		String trainOutFolder=Params.outPrefixBow+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
		String testOutFolder=Params.outPrefixBow+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String superOutFolder=Params.outPrefixBow+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;		
		
//		get the idf from train collection
		Collection trainCol=new Collection(trainFolderText);
		HashMap word_idf=trainCol.getWordIDF(toolbox.splitter, toolbox.tokenizer);
//		compute vecs for train folder
		trainCol.computeFolderBowVec(Params.wType, toolbox.splitter, toolbox.tokenizer, word_idf, trainOutFolder);
//		compute vecs for test folder
		Collection testCol=new Collection(testFolderText);
		testCol.computeFolderBowVec(Params.wType, toolbox.splitter, toolbox.tokenizer, word_idf, testOutFolder);
//		compute vecs for super folder
		Collection superCol=new Collection (superFolderText);
		superCol.computeFolderBowVec(Params.wType, toolbox.splitter, toolbox.tokenizer, word_idf, superOutFolder);

	}
	

	void getVecsForWn (Toolbox toolbox, String prompt, String fold) {
		String trainOutFolder=Params.outPrefixWn+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
		String testOutFolder=Params.outPrefixWn+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String superOutFolder=Params.outPrefixWn+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;	
//		get the idf from train collection
		Collection trainCol=new Collection(trainFolderText);
		HashMap syn_idf=trainCol.getSynsetIDF(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger); 
//		compute vecs for train folder
		trainCol.computeFolderSynVectors(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, 
				Params.doPOS, syn_idf, trainOutFolder, toolbox.tagger);
//		compute vecs for test folder
		Collection testCol=new Collection(testFolderText);
		testCol.computeFolderSynVectors(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, 
				Params.doPOS, syn_idf, testOutFolder, toolbox.tagger);
//		compute vecs for super folder
		Collection superCol=new Collection (superFolderText);
		superCol.computeFolderSynVectors(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, 
				Params.doPOS, syn_idf, superOutFolder, toolbox.tagger);
	}
	
	void getAnnotatedOutputForWn (Toolbox toolbox, String prompt, String fold) {
		String trainOutFolder=Params.outPrefixWn+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
		String testOutFolder=Params.outPrefixWn+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String superOutFolder=Params.outPrefixWn+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;	
		
		Collection trainCol=new Collection(trainFolderText);
		trainCol.annotateFolderByWordNet(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger, trainOutFolder);
		Collection testCol=new Collection(testFolderText);
		testCol.annotateFolderByWordNet(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger, testOutFolder);
		Collection superCol=new Collection (superFolderText);
		superCol.annotateFolderByWordNet(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger, superOutFolder);
	}
	
	void getVecsForWiki (String prompt, String fold) {
		String trainOutFolder=Params.outPrefixWiki+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
		String testOutFolder=Params.outPrefixWiki+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String superOutFolder=Params.outPrefixWiki+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
//		get idf from train collection
		WikiXmlCollection trainCol=new WikiXmlCollection(trainFolderWiki);
		HashMap wiki_idf=trainCol.getWikiIDF();
//		compute vecs for train folder
		trainCol.computeFolderWikiVectors(Params.wType, wiki_idf, trainOutFolder);
//		compute vecs for test folder
		WikiXmlCollection testCol=new WikiXmlCollection(testFolderWiki);
		testCol.computeFolderWikiVectors(Params.wType, wiki_idf, testOutFolder);
//		compute vecs for super folder
		WikiXmlCollection superCol=new WikiXmlCollection(superFolderWiki);
		superCol.computeFolderWikiVectors(Params.wType, wiki_idf, superOutFolder);
	}
	
	/**
	 * this actually gets the remaining word vecs (which don't have synests in WordNet, because synset vecs was calculated in pure WordNet method.
	 * it's not necessary to return it again here
	 */
	void getVecsForWnReplacingWords (Toolbox toolbox, String prompt, String fold) {
		String trainOutFolder=Params.outPrefixWnRepWords+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
		String testOutFolder=Params.outPrefixWnRepWords+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String superOutFolder=Params.outPrefixWnRepWords+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
//		get the idf from train collection
		Collection trainCol=new Collection(trainFolderText);
		HashMap word_idf=trainCol.getSynsetReplacingWordIDFReturnWordOnly(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch
				, Params.doPOS, toolbox.tagger);
//		compute vecs for train folder
		trainCol.computeFolderSynReplacingWordVecs(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch
				, Params.doPOS, word_idf, trainOutFolder, toolbox.tagger);
//		compute vecs for test folder
		Collection testCol=new Collection(testFolderText);
		testCol.computeFolderSynReplacingWordVecs(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch
				, Params.doPOS, word_idf, testOutFolder, toolbox.tagger);
//		compute vecs for super folder
		Collection superCol=new Collection(superFolderText);
		superCol.computeFolderSynReplacingWordVecs(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch
				, Params.doPOS, word_idf, superOutFolder, toolbox.tagger);
	}
	
	void getVecsForWikiReplacingWords (Toolbox toolbox, String prompt, String fold) {
		
		String trainOutFolder=Params.outPrefixWikiRepWords+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
		String testOutFolder=Params.outPrefixWikiRepWords+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String superOutFolder=Params.outPrefixWikiRepWords+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
//		get idf from train collection
		WikiXmlCollection trainCol=new WikiXmlCollection(trainFolderWiki);
		HashMap word_idf=trainCol.getWordReplacedByWikiIdf(toolbox.splitter, toolbox.tokenizer);
//		get vecs for train folder
		trainCol.computeFolderWordReplacedByWiki(Params.wType, word_idf, trainOutFolder, toolbox.splitter, toolbox.tokenizer);
//		get vecs for test folder
		WikiXmlCollection testCol=new WikiXmlCollection(testFolderWiki);
		testCol.computeFolderWordReplacedByWiki(Params.wType, word_idf, testOutFolder, toolbox.splitter, toolbox.tokenizer);
//		get vecs for super folder
		WikiXmlCollection superCol=new WikiXmlCollection(superFolderWiki);
		superCol.computeFolderWordReplacedByWiki(Params.wType, word_idf, superOutFolder, toolbox.splitter, toolbox.tokenizer);
		
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
		
		//the esa folders are not in use now
		String inPrefixText="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds"; //param1
		String inPrefixWiki="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml"; //param2
//		output folder is setup in Params.outPrefixBow
		
		Toolbox toolbox=new Toolbox(true,true,false,true,true,false,false);
		toolbox.enableTools();
		
		for (String prompt : Params.prompts) {
			EraterResults eresults=null;
			for (String fold : Params.folds) {  //param3
				String trainFoldeText=inPrefixText+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
				String testFolderText=inPrefixText+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
				String superFolderText=inPrefixText+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
				
//				String trainFolderWiki=inPrefixWiki+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;  //folders storing wikification xml files, or flat.html files (this is for wiki replacing word case)
//				String testFolderWiki=inPrefixWiki+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
//				String superFolderWiki=inPrefixWiki+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
				String trainFolderWiki=null;
				String testFolderWiki=null;
				String superFolderWiki=null;
						
				VectorWriter vecwriter=new VectorWriter(trainFoldeText,testFolderText,superFolderText
						,trainFolderWiki,testFolderWiki,superFolderWiki
						,null,null,null,eresults);
//				System.out.println("Start processing bow vecs");
//				vecwriter.getVecsForBow(toolbox, prompt, fold);
				System.out.println("Start processing wordnet vecs");
				vecwriter.getVecsForWn(toolbox, prompt, fold);
//				System.out.println("Start annotating files by wordnet synsets");
//				vecwriter.getAnnotatedOutputForWn(toolbox, prompt, fold);
//				System.out.println("Start processing wiki vecs");
//				vecwriter.getVecsForWiki(prompt, fold);
//				System.out.println("Start processing wordnet replacing word vecs, only output word vecs:");
//				vecwriter.getVecsForWnReplacingWords(toolbox, prompt, fold);
//				System.out.println("Start processing wiki replacing word vecs, only output word vecs:");
//				vecwriter.getVecsForWikiReplacingWords(toolbox, prompt, fold);
			}
		}
				
		
	}

}
