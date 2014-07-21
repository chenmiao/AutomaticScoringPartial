package utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

/***
 * 
 * given super files super0,super1,super2, find out vocabulary size of the corpus
 * by making a harsh from the 3 files
 * 
 * @author miaochen
 *
 */
public class CountCorpusVocabSize {
	
	public static void main (String[] args) {
		
		String inFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wiki-vec-sorted";  //param 1
		String out="";
		
		int allvocab=0;
	    String[] prompts=Params.prompts;
	    
	    for (String prompt : prompts) {
	    	HashMap<String,Double> vocab=new HashMap <String,Double>();
	    	
	    	String fold="0"; //just the 1st fold is enough for computing this
			String trainFolder=inFolder+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
			String testFolder=inFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
	    	
			String[] trainfiles=new File(trainFolder).list();
			for (String trainfile : trainfiles) {
				if (!trainfile.startsWith(".")) {
					System.out.println("Processing file "+trainfile);					
					String trainfileDir=trainFolder+File.separator+trainfile;
					HashMap hm=FileIO.readFileToHash(trainfileDir, 0);
					for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ){
						String term=(String) iter.next();
						if ((!term.isEmpty()) && (!vocab.containsKey(term))) {
							vocab.put(term, 0.0);
						}
					}
				}								
			}
			
			String[] testfiles=new File(testFolder).list();
			for (String testfile : testfiles) {
				if (!testfile.startsWith(".")) {
					System.out.println("Processing file "+testfile);
					String testfileDir=testFolder+File.separator+testfile;
					HashMap hm=FileIO.readFileToHash(testfileDir, 0);
					for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ) {
						String term=(String) iter.next();
						if ((!term.isEmpty()) && (!vocab.containsKey(term))) {
							vocab.put(term, 0.0);
						}
					}
					
				}								
			}
			out+="Total vocab size of prompt "+prompt+" is "+vocab.size()+"\n";
			allvocab+=vocab.size();
	    }
		
	    System.out.println(out);
		System.out.println("Average vocab size is "+ ((double)allvocab/(double)4));		
		
	}

}
