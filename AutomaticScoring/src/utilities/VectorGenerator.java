package utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/***
 * it produces vectors and write to output folder
 * 
 * @author miaochen
 *
 *
 */
public class VectorGenerator {
	
	
	VectorGenerator () {
		
	}
	
	void generateWikiVector () {
		
	}

	
	public static void main (String[] args) {
//		!!!!!don't forget to set up the log file in RUN CONFIGURATION !!!!!
		
//		generating bow vec (no IDF involved in weights)
		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-noidf-vec";
		int wtype=Params.wType;//this is tf/EucLen* idf
		
		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
		SentenceSplitter splitter = new SentenceSplitter(spliterModel);		

		String[] prompts=Params.prompts;
		String[] folds=Params.folds;
		for (String prompt : prompts) {
			for (String fold : folds) {
				System.out.println("Processing prompt "+prompt+" fold "+fold);
//				write train/test/super folder of files to Wiki vectors
				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
//				output folder dir
				String trainOutFolder=outPrefix+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
				String testOutFolder=outPrefix+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
				String superOutFolder=outPrefix+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
					
//				get tfidf weights for files in training folder
//				given a folder and and idf hash, compute and write (to an output folder) 
//				vectors of files in this folder
				Collection trainCol=new Collection(trainFolderDir);
				trainCol.computeFolderBowVec(wtype, splitter, tokenizer, null, trainOutFolder);
////				get test folder vectors
				Collection testCol=new Collection(testFolderDir);
				testCol.computeFolderBowVec(wtype, splitter, tokenizer, null, testOutFolder);
////				get super folder vectors
				Collection superCol=new Collection(superFolderDir);
				superCol.computeFolderBowVec(wtype, splitter, tokenizer, null, superOutFolder);
			}
			
		}
		
		
//		generating word replaced by wordnet vectors (wn=1st sense)
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
//		String outBowPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-1st-replace-word/bow";
//		String outWnPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-1st-replace-word/wn";
//		
//		SentenceTokenizer tokenizer = new SentenceTokenizer(Params.tokenizerModel);
//		SentenceSplitter splitter = new SentenceSplitter(Params.spliterModel);
//		SentenceTagger tagger = new SentenceTagger(Params.taggerModel, Params.taggerDict);
//		String pwd=PasswordField.readPassword("Enter password: ");
//		System.out.println("Finished entering password");
//		DBConnection dbcon=new DBConnection(Params.dbUrl,Params.dbClass, Params.usr,pwd);
//		WordNetMatching wnmatch=new WordNetMatching(Params.wnpath);
//		
//		for (String prompt : Params.prompts) {
//			for (String fold : Params.folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);
////				write train/test/super folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				output folder dir (for bow)
//				String trainBowOutFolder=outBowPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testBowOutFolder=outBowPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superBowOutFolder=outBowPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				out folder dir (for wn)
//				String trainWnOutFolder=outWnPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testWnOutFolder=outWnPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superWnOutFolder=outWnPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
//				
////				get WordNet synset & word idfs
//				Collection trainCol=new Collection(trainFolderDir);
//				ArrayList<HashMap> hms=trainCol.getSynsetReplacingWordIDF(splitter, tokenizer, dbcon, wnmatch, Params.doPOS, tagger);
//				HashMap word_idf=hms.get(0);
//				HashMap syn_idf=hms.get(1);
//				
////				get train folder tfidf vecs
//				trainCol.computeFolderSynReplacingWordVecs(Params.wType, splitter, tokenizer, dbcon, wnmatch, Params.doPOS, word_idf, syn_idf, trainBowOutFolder, trainWnOutFolder, tagger);
////				get test folder tfidf vectors
//				Collection testCol=new Collection(testFolderDir);
//				testCol.computeFolderSynReplacingWordVecs(Params.wType, splitter, tokenizer, dbcon, wnmatch, Params.doPOS, word_idf, syn_idf, testBowOutFolder, testWnOutFolder, tagger);
////				get super folder tfidf vectors
//				Collection superCol=new Collection(superFolderDir);
//				superCol.computeFolderSynReplacingWordVecs(Params.wType, splitter, tokenizer, dbcon, wnmatch, Params.doPOS, word_idf, syn_idf, superBowOutFolder, superWnOutFolder, tagger);
//			}
//		}
//		dbcon.close();
		
//		*****generating word replaced by wiki vectors, from Wikification results*******
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikia-replace-word/html";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikia-replace-word/bow";
//		int wtype=3;//this is tf/EucLen* idf
//		SentenceTokenizer tokenizer = new SentenceTokenizer(Params.tokenizerModel);
//		SentenceSplitter splitter = new SentenceSplitter(Params.spliterModel);
//				
//		for (String prompt : Params.prompts) {
//			for (String fold : Params.folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);				
//				
////				write train/test/super folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				output folder dir
//				String trainOutFolder=outPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testOutFolder=outPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superOutFolder=outPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
//				
////				get Wiki idf
//				WikiXmlCollection trainCol=new WikiXmlCollection(trainFolderDir);
//				HashMap word_idf=trainCol.getWordReplacedByWikiIdf(splitter, tokenizer);
//								
////				get tfidf weights for files in training folder
////				given a folder and and idf hash, compute and write (to an output folder) 
////				vectors of files in this folder 
//				trainCol.computeFolderWordReplacedByWiki(wtype, word_idf, trainOutFolder, splitter, tokenizer);				
////				get test folder vectors
//				WikiXmlCollection testCol=new WikiXmlCollection(testFolderDir);
//				testCol.computeFolderWordReplacedByWiki(wtype, word_idf, testOutFolder, splitter, tokenizer);				
////				get super folder vectors
//				WikiXmlCollection superCol=new WikiXmlCollection(superFolderDir);
//				superCol.computeFolderWordReplacedByWiki(wtype, word_idf, superOutFolder, splitter, tokenizer);
//			}
//			
//		}
		
//		*****generating Wiki vectors, from Wikification results*********
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/vec";
//		int wtype=3;//this is tf/EucLen* idf
//				
//		for (String prompt : Params.prompts) {
//			for (String fold : Params.folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);				
//				
////				write train/test/super folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				output folder dir
//				String trainOutFolder=outPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testOutFolder=outPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superOutFolder=outPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
//				
////				get Wiki idf
//				WikiXmlCollection trainCol=new WikiXmlCollection(trainFolderDir);
//				HashMap wiki_idf=trainCol.getWikiIDF();
//								
////				get tfidf weights for files in training folder
////				given a folder and and idf hash, compute and write (to an output folder) 
////				vectors of files in this folder 
//				trainCol.computeFolderWikiVectors(wtype, wiki_idf, trainOutFolder);
////				get test folder vectors
//				WikiXmlCollection testCol=new WikiXmlCollection(testFolderDir);
//				testCol.computeFolderWikiVectors(wtype, wiki_idf, testOutFolder);
////				get super folder vectors
//				WikiXmlCollection superCol=new WikiXmlCollection(superFolderDir);
//				superCol.computeFolderWikiVectors(wtype, wiki_idf, superOutFolder);
//			}
//			
//		}

//		*****generating WordNet synset vectors, pos based*********
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-vec-pos";
//		int wtype=3;//this is tf/EucLen* idf
//		
//		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
//		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
//		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
//		SentenceSplitter splitter = new SentenceSplitter(spliterModel);
//		SentenceTagger tagger = new SentenceTagger(
//				"/Users/miaochen/Documents/Software/OpenNLP/models/tag.bin.gz",
//				"/Users/miaochen/Documents/Software/OpenNLP/models/tagdict");
//		
//		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
//		String dbClass = "com.mysql.jdbc.Driver";
//		String usr="miao";		
//		String pwd=PasswordField.readPassword("Enter password: ");
//		System.out.println("Finished entering password");
//		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
//		
//		String wnhome = "/Users/miaochen/Documents/Software/WordNet/3.0";
//		String wnpath = wnhome + File.separator + "dict";
//		boolean doPOS=true;
//		WordNetMatching wnmatch=new WordNetMatching(wnpath);		
//		
//		String[] prompts={"098","099","100","101"};
//		String[] folds={"0","1","2"};
//		for (String prompt : prompts) {
//			for (String fold : folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);
////				write train/test/super folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				output folder dir
//				String trainOutFolder=outPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testOutFolder=outPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superOutFolder=outPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
//				
////				get WordNet synset idf
//				Collection trainCol=new Collection(trainFolderDir);
//				HashMap syn_idf=trainCol.getSynsetIDF(splitter, tokenizer, dbcon, wnmatch, doPOS, tagger); 
//				
////				get tfidf weights for files in training folder
////				given a folder and and idf hash, compute and write (to an output folder) 
////				vectors of files in this folder 
//				trainCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//						syn_idf, trainOutFolder, tagger);
////				get test folder vectors
//				Collection testCol=new Collection(testFolderDir);
//				testCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//						syn_idf, testOutFolder, tagger);
////				get super folder vectors
//				Collection superCol=new Collection(superFolderDir);
//				superCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//						syn_idf, superOutFolder, tagger);
//			}
//			
//		}
//		
//		dbcon.close();
		
//		*****generating WordNet synset vectors, 1st sense*********
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-vec-1st";
//		int wtype=3;//this is tf/EucLen* idf
//		
//		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
//		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
//		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
//		SentenceSplitter splitter = new SentenceSplitter(spliterModel);		
//		
//		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
//		String dbClass = "com.mysql.jdbc.Driver";
//		String usr="miao";		
//		String pwd=PasswordField.readPassword("Enter password: ");
//		System.out.println("Finished entering password");
//		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
//		
//		String wnhome = "/Users/miaochen/Documents/Software/WordNet/3.0";
//		String wnpath = wnhome + File.separator + "dict";
//		boolean doPOS=false;
//		WordNetMatching wnmatch=new WordNetMatching(wnpath);		
//		
//		String[] prompts={"098","099","100","101"};
//		String[] folds={"0","1","2"};
//		for (String prompt : prompts) {
//			for (String fold : folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);
////				write train/test/super folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				output folder dir
//				String trainOutFolder=outPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testOutFolder=outPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superOutFolder=outPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
//				
////				get WordNet synset idf
//				Collection trainCol=new Collection(trainFolderDir);
//				HashMap syn_idf=trainCol.getSynsetIDF(splitter, tokenizer, dbcon, wnmatch, doPOS); 
//				
////				get tfidf weights for files in training folder
////				given a folder and and idf hash, compute and write (to an output folder) 
////				vectors of files in this folder 
//				trainCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//						syn_idf, trainOutFolder);
////				get test folder vectors
//				Collection testCol=new Collection(testFolderDir);
//				testCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//						syn_idf, testOutFolder);
////				get super folder vectors
//				Collection superCol=new Collection(superFolderDir);
//				superCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//						syn_idf, superOutFolder);
//			}
//			
//		}
//		
//		dbcon.close();
		
//      *********generating Wiki vectors*************		
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wiki-vec";
//		int wtype=5;//this is logarithm tf* idf
////		the ngram (maximum string length) for matching Wikipedia concepts
//		int ngram=5;
//		
//		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
//		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
//		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
//		SentenceSplitter splitter = new SentenceSplitter(spliterModel);		
//		
//		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
//		String dbClass = "com.mysql.jdbc.Driver";
//		String usr="miao";		
//		String pwd=PasswordField.readPassword("Enter password: ");
//		System.out.println("Finished entering password");
//		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
//		
//		String[] prompts={"098","099","100","101"};
//		String[] folds={"0","1","2"};
//		for (String prompt : prompts) {
//			for (String fold : folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);
////				write train/test/super folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
////				output folder dir
//				String trainOutFolder=outPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testOutFolder=outPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superOutFolder=outPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;
//				
////				get Wiki concept idf
//				Collection trainCol=new Collection(trainFolderDir);
//				HashMap con_idf=trainCol.getWikiConIDF(splitter, tokenizer, dbcon, ngram); 
//				
////				get tfidf weights for files in training folder
////				given a folder and and idf hash, compute and write (to an output folder) 
////				vectors of files in this folder 
//				trainCol.computeFolderWikiVectors(wtype, splitter, tokenizer, dbcon, con_idf, 
//						trainOutFolder, ngram);
////				get test folder vectors
//				Collection testCol=new Collection(testFolderDir);
//				testCol.computeFolderWikiVectors(wtype, splitter, tokenizer, dbcon, con_idf, testOutFolder, ngram);
////				get super folder vectors
//				Collection superCol=new Collection(superFolderDir);
//				superCol.computeFolderWikiVectors(wtype, splitter, tokenizer, dbcon, con_idf, superOutFolder, ngram);
//			}
//			
//		}
//		
//		dbcon.close();
	}

}
