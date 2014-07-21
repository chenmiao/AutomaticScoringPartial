package utilities;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;


/**
 * given a folder a document (in hash),
 * count total num of unique terms / num documents
 * 
 * @author miaochen
 *
 */
public class CountAvgTemNumberPerDoc {
	
	/**
	 * good keys mean meaningful words/terms, not "",or empty string
	 * @param hm
	 */
	static int countGoodKeysInHash (HashMap hm) {
		int n=0;
		for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ) {
			String k=(String) iter.next();
			if (!k.isEmpty()) {
				n+=1;
			}
		}
		return n;
	}
	
	public static void main (String[] args) {
		
		String inFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wiki-vec-sorted"; //param1
		int numTerms=0;
		int numFile=0;
		
		for (String prompt : Params.prompts) {
			String fold="0"; //just the 1st fold is enough for computing this
			String trainFolder=inFolder+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
			String testFolder=inFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
			
			
			String[] trainfiles=new File(trainFolder).list();
			for (String trainfile : trainfiles) {
				if (!trainfile.startsWith(".")) {
					System.out.println("Processing file "+trainfile);
					numFile+=1;
					
					String trainfileDir=trainFolder+File.separator+trainfile;
					HashMap hm=FileIO.readFileToHash(trainfileDir, 0);
					numTerms+=countGoodKeysInHash(hm);
				}								
			}
			
			String[] testfiles=new File(testFolder).list();
			for (String testfile : testfiles) {
				if (!testfile.startsWith(".")) {
					System.out.println("Processing file "+testfile);
					numFile+=1;
					
					String testfileDir=testFolder+File.separator+testfile;
					HashMap hm=FileIO.readFileToHash(testfileDir, 0);
					numTerms+=countGoodKeysInHash(hm);
				}
				
			}
		}
		
		System.out.println("Total num of files is "+numFile);
		System.out.println("Total num of terms is "+numTerms);
		System.out.println("Num of distinct terms per doc is "+ ((double)numTerms/(double)numFile));
	}

}
