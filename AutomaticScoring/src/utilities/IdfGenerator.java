package utilities;

import java.io.File;
import java.util.HashMap;

public class IdfGenerator {
	
	public static void main (String[] args) {
//		*****this code generates idf files for Wordnet synsets*****
		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-vec-1st/idf";
		int wtype=3;//this is tf/EucLen* idf
		
		String tokenizerModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
		SentenceTokenizer tokenizer = new SentenceTokenizer(tokenizerModel);
		String spliterModel = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishSD.bin.gz";
		SentenceSplitter splitter = new SentenceSplitter(spliterModel);		
		
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
		
		String wnhome = "/Users/miaochen/Documents/Software/WordNet/3.0";
		String wnpath = wnhome + File.separator + "dict";
		boolean doPOS=false;
		WordNetMatching wnmatch=new WordNetMatching(wnpath);
		SentenceTagger tagger=null;
		
		String[] prompts={"098","099","100","101"};
		String[] folds={"0","1","2"};
		for (String prompt : prompts) {
			for (String fold : folds) {
				System.out.println("Processing prompt "+prompt+" fold "+fold);
//				write train/test/super folder of files to Wiki vectors
				String trainFolderDir=inPrefix+File.separator+prompt+File.separator+"train"+fold;
//				get WordNet synset idf
				Collection trainCol=new Collection(trainFolderDir);
				HashMap syn_idf=trainCol.getSynsetIDF(splitter, tokenizer, dbcon, wnmatch, doPOS,tagger); 
//				write the hash to output idf file
				String outIdf=outPrefix+"idf-"+prompt+"-train-"+fold;
				FileIO.writeHashMapToFile(syn_idf, outIdf);
			}
			
		}
		
		dbcon.close();
		
	}

}
