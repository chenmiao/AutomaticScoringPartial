package utilities;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import java.util.Iterator;

import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;

public class ResultAnalysis {
	
 	/***
	 * sort a hashmap by value
	 * 
	 * @param map
	 * @return
	 */
	static public <K,V extends Comparable<V>> Map<K,V> sortByValues (final Map<K,V> map) {
		Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}
	
	
	public static void main (String[] args) {
		
		SentenceTokenizer tokenizer = new SentenceTokenizer(Params.tokenizerModel);
		SentenceSplitter splitter = new SentenceSplitter(Params.spliterModel);
		SentenceTagger tagger = new SentenceTagger(Params.taggerModel, Params.taggerDict);
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbcon=new DBConnection(Params.dbUrl,Params.dbClass, Params.usr,pwd);
		WordNetMatching wnmatch=new WordNetMatching(Params.wnpath);
		
		Document doc=new Document(new File ("/Users/miaochen/Documents/diss-experiment/" +
				"proc-corpus/datafolds/098/train/train0/7508663-VB531098.txt"));
		doc.toWordVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer);
		doc.toSynVectorFrequencyNormalizedByEuclideanLength(splitter, tokenizer, dbcon, wnmatch, Params.doPOS, tagger);
		
		HashMap word_w=doc.word_normfreq;
		FileIO.writeHashMapOrderByValue(word_w, "/Users/miaochen/Documents/diss-experiment/results/test/bowhash");
		HashMap syn_w=doc.syn_normfreq;	
		FileIO.writeHashMapOrderByValue(syn_w, "/Users/miaochen/Documents/diss-experiment/results/test/synhash");
		
//		String bowout="";
//		Map sortedbow=sortByValues(word_w);
//		for (Iterator iter=sortedbow.keySet().iterator(); iter.hasNext(); ) {
//			String word=(String) iter.next();
//			bowout+=(String)word+"\t"+word_w.get(word)+"\n";
//		}
//		FileIO.writeFile("/Users/miaochen/Documents/diss-experiment/results/test/bowhash", bowout);
//		
//		HashMap syn_w=doc.syn_normfreq;	
//		FileIO.writeHashMapToFile(word_w, "/Users/miaochen/Documents/diss-experiment/results/test/bowhash");
//		
//		String synout="";
//		for (Iterator iter=syn_w.keySet().iterator(); iter.hasNext(); ) {
//			ISynsetID synid=(ISynsetID) iter.next();
//			synout+=synid.toString()+"\t"+ ((Double) syn_w.get(synid)).toString()+"\n";
//		}
//		FileIO.writeFile("/Users/miaochen/Documents/diss-experiment/results/test/synhash", synout);
	}

}
