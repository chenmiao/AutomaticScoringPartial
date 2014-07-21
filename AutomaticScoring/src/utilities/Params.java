package utilities;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/***
 * this class stores values of parameters
 * 
 * @author miaochen
 *
 */

public class Params {
	
//	universal params
	public static final String[] prompts={"098","099","100","101"};
	public static final String[] folds={"0","1","2"};
//	public static final String[] folds10={"0","1","2","3","4","5","6","7","8","9"};
	public static final int minScore=2;
	public static final int maxScore=4;
	
//	for use in EraterResults.java
	public static final int numClasses=3;
	public static final String[] classValues = { "2", "3", "4" };
	List<String> cValues = Arrays.asList(classValues);
	
//	public static final String scoreList="data/1239files-mergedscore.txt";
	public static final String scoreList="/Users/miaochen/Documents/diss-experiment/info/1237files-mergedscore.txt";
	
	public static final String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
	public static final String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
	public static final String taggerModel="/Users/miaochen/Documents/Software/OpenNLP/models/tag.bin.gz";
	public static final String taggerDict="/Users/miaochen/Documents/Software/OpenNLP/models/tagdict";
	
	public static final String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
	public static final String dbClass = "com.mysql.jdbc.Driver";
	public static final String usr="miao";	
	
	public static final String wndir="/Users/miaochen/Documents/Software/WordNet";
	public static final String wnversion="3.0";
	public static final String wnpath = wndir+File.separator+wnversion+ File.separator + "dict";
		
//	***params for bow
	public static final int vType=0;
	public static final boolean useStoplistOnBow=true; //this option is only for bow
// 	public static final String stoplist="data/XXstoplist.pp";
 	public static final String stoplist="/Users/miaochen/Software/javalibs/XXstoplist.pp";
 	public static final int wType=3; //tfidf type
 	public static final String outPrefixBow="/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-stem-stop";
// 	public static final boolean expand=false;  //do not alter, a dumb param
// 	public static final boolean doPOS=false;  //do not alter, a dumb param
// 	public static boolean sameSimWeighting=true;
 	public static boolean stemming=false;
	
//	***params for wordnet (pos=true/false)
// 	public static final int vType=1;
	public static final boolean doPOS=false;
//	public static final int wType=3; //tfidf type
	public static final boolean expand=false;
	public static final String outPrefixWn="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn1st-stop";
	public static final int outputSynsetFormat=1; //0 is synset id, 1 is Synset class (a very long string)
	public static final boolean useStoplist=true;
	
//	***params for wiki
//	public static final int vType=2;
//	public static final int wType=3; //tfidf type
//	public static final boolean expand=false; //false=not reasoning
	public static final String outPrefixWiki="/Users/miaochen/Documents/diss-experiment/proc-corpus/wiki-vec-sorted";
	
//	***params for esa
	public static final int esaNum=1000; //num of esa dimensions
	
//	***params for combined document
//	public static final int cType=0; //combine type, 0=combine, 1=replace
//	public static final int wType=3; //tfidf type
	
//	***params for wn replacing words
	public static final String outPrefixWnRepWords="/Users/miaochen/Documents/diss-experiment/proc-corpus/wnpos-replace-word";
//	public static final boolean doPOS=true;
//	public static final int wType=3; //tfidf type
//	public static final boolean expand=false;	
	
//	***params for wiki replacing words
	public static final String outPrefixWikiRepWords="/Users/miaochen/Documents/diss-experiment/proc-corpus/wiki-replace-word";
//	public static final int wType=3; //tfidf type
//	public static final boolean expand=false;	
	
//	***params for wordnet reasoning (sim=path or lin)
//	public static final int vType=1;
//	public static final int wType=3; //tfidf type
//	public static final boolean doPOS=false;
//	public static final boolean expand=true;
	public static final int wnSimType=1; //0=path similarity, 1=Lin similarity, 2=default assigned weight
	public static final int nTrainSyn=5; //get n most similar concepts

	
//	***params for Wiki reasoning
//	public static final int vType=2;
//	public static final int wType=3; //tfidf type
//	public static final boolean expand=true;
	public static final int wikiSimType=2; //0=link similarity, 1=content similarity, 2=default assigned weight
//	public static final int nTrainSyn=5; //get n most similar concepts
	
//  ***params for testing different weighting combinations for similarity
	public static boolean sameSimWeighting=false;
	public static double wBow=0.7;  //weight of bow similarity
	public static double wWn=0.1;  //weight of wordnet similarity
	public static double wWiki=0.2; //weight of wiki similarity
	public static double wEsa=0; 
//	public static final boolean expand=false;
	public static final boolean bowVecNormalized=true; //default
	public static final boolean wnVecNormalized=true;
	public static final boolean wikiVecNormalized=true;
	public static final boolean esaVecNormalized=false; 
	
	
//	public static final int wType=3; //tfidf type
////	public static final int inType=0;
//	public static final int cType=0; //combine type, 0=combine, 1=replace
//	public static final int esaNum=50; //num of esa dimensions
////	for similarity (expanding wordnet vec)
//	public static final int nTrainSyn=5;
//	public static final int wnSimType=0;  //0=path similarity, 1=Lin similarity
//	public static final int vType=2; //vector type: 0=bow, 1=wordnet, 2=wiki, 3=combined (containing esa case)
//	public static final boolean expand=false; //whether to expand/reason the vector or not
	

	
	public static void main (String[] args) {
		System.out.println(FileIO.getFileAsString(scoreList));
	}

}
