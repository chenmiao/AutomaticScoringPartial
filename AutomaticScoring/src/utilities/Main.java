package utilities;

import java.io.File;
import java.util.HashMap;

public class Main {
	
	public static void main (String[] args){
		
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/test/in";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/test/out";
//		int wtype=3;
//		
//		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
//		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
//		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
//		SentenceSplitter splitter = new SentenceSplitter(spliterModel);		
//		
//		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264";
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
//		String trainFolderDir=inPrefix;
//		String trainOutFolder=outPrefix;
//		
//		Collection trainCol=new Collection(trainFolderDir);
//		HashMap syn_idf=trainCol.getSynsetIDF(splitter, tokenizer, dbcon, wnmatch, doPOS);
//		trainCol.computeFolderSynVectors(wtype, splitter, tokenizer, dbcon, wnmatch, doPOS, 
//				syn_idf, trainOutFolder);
		
		
//		String[] prompts={"098","099","100","101"};
//		String[] folds={"0","1","2"};
//		for (String prompt : prompts) {
//			for (String fold : folds) {
//				System.out.println("Processing prompt "+prompt+" fold "+fold);
////				write a folder of files to Wiki vectors
//				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				String testFolderDir=inPrefix+File.separator+prompt+File.separator+"test"+fold;
//				String superFolderDir=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"train"+fold;				
////				get Wiki concept idf
//				Collection trainCol=new Collection(trainFolderDir);
//				HashMap con_idf=trainCol.getWikiConIDF(splitter, tokenizer, dbcon, ngram); 
////				get tfidf weights for files in training folder
//				for (String trainFile : new File (trainFolderDir).list()){
//					System.out.println("Processing "+trainFile);
//					Document trainDoc=new Document (new File(trainFolderDir+File.separator+trainFile));
//					trainDoc.toWikiVectorTfIdf(wtype, splitter, tokenizer, dbcon, con_idf, ngram);
////					Then write hash to file...
//					String trainOutDir=outPrefix+File.separator+prompt+File.separator+"train"+fold;
//					FileIO.writeHashMapToFile(trainDoc.con_normfreq, trainOutDir);
//				}
//			}
//			
//		}
		
//		dbcon.close();
		
	}

}
