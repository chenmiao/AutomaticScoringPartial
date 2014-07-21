package utilities;

import java.util.HashMap;
import java.util.Iterator;


/***
 * Given a document, find number of merged synset in this document
 * merged synset means a synset subsuming multiple words, e.g. synset1 subsumes {chance, opportunity}
 * 
 * @author miaochen
 *
 */

public class CheckSynsetNumberInDoc {
	
	/***
	 * this checks whehter singlewords are just because of lower/upper case of a same word
	 * e.g. She,she is actually 1 dimension in BOW, so it's the same with wn1st, and need not to be counted
	 * 
	 * @param singlewords
	 * @return
	 */
	static boolean isLowerUpperCases (String[] singlewords) {
		
		boolean islowerupppercase=false;
		
		if (singlewords.length == 2) {
			
			if (singlewords[0].equalsIgnoreCase(singlewords[1])) {
				islowerupppercase=true;
			}
			
		}
		
		return islowerupppercase;
		
	}
	
	
	public static void main (String[] args) {
		
		String vecDir="" +
				"/Users/miaochen/Documents/diss-experiment/proc-corpus/wn1st-vec-sorted/098/test/test0/7619622-VB531098.txt";  //param 1
		String synwordsDir="/Users/miaochen/Documents/diss-experiment/notes/wn1st/098-syn_words.txt";  //param2
		String outDir="/Users/miaochen/Documents/diss-experiment/notes/wn1st/7619622-VB531098-mergedsyn.txt";  //param3
		
		String merged="";
		String unknown="";
		int c1=0;
		int c2=0;  //counter for known concepts
		int c3=0;  //counter for known, merged synsets
		int c4=0; //counter for unknown synsets
		int c5=0; //counter for known, merged, and not because of the upper/lowercase synset
		
		HashMap<String,Double> syn_w=FileIO.readFileToHash(vecDir, 0);
		HashMap<String,String> syn_words=FileIO.readFileToHash(synwordsDir, 2, 2);
		
		for (Iterator iter=syn_w.keySet().iterator(); iter.hasNext(); ) {
			String syn=iter.next().toString();
			if (syn_words.containsKey(syn)) {
				System.out.println("Synset "+syn+" is in the training vec");
				String words=syn_words.get(syn);
				String[] singlewords=words.split(",");
				if (singlewords.length > 1) {  //means this is a merged synset
					merged+=syn+"\t"+words+"\t"+syn_w.get(syn)+"\n";
					c3+=1;
					
					if (!isLowerUpperCases(singlewords)) {
						c5+=1;
					}
				}
				c2+=1;
			}else {
				System.out.println("Synset "+syn+" is not in the training vec");
				unknown+="Unknown: "+syn+"\t"+syn_w.get(syn)+"\n";
				c4+=1;
			}
			c1+=1;
		}
		
		String title="Total synstes is "+c1+"\n"+"Known synsets is "+c2+"\n"
				+"Unknown synstes is "+c4+"\n"+"Known and merged synstes is "+c3+"\n"
				+"Known, merged, and not uppper/lower case synstes is "+c5;
		
		FileIO.writeFile(outDir, title+"\n\n"+merged+"\n\n"+unknown);
		System.out.println("Total synstes is "+title);

	}

}
