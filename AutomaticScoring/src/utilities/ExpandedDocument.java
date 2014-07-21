package utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.mit.jwi.item.ISynset;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;


public class ExpandedDocument {
		
	
	/***
	 * the keys are ISynset, values are weight
	 * 
	 * @param testWn_w
	 * @param trainWn_w
	 */
	void expandWnVec (HashMap testWn_w, HashMap trainWn_w, Path path, Lin lin, int wnSimType) {
//		find which words in testWn don't appear in trainWn, the "unknown" word
		ArrayList<Object> unseens=findUnknownKeys (testWn_w, trainWn_w);
		for (Object un : unseens) {
			ISynset unSyn=(ISynset) un;
			System.out.println("The unseen synset is: "+unSyn.toString());
//			build a hashmap of trainSynset => its similarity with the unseen word
			HashMap<ISynset,Double> trainSyn_sim=new HashMap<ISynset,Double> ();
			for (Iterator iter=trainWn_w.keySet().iterator(); iter.hasNext(); ) {
				ISynset trainSyn=(ISynset) iter.next();
//				check if the POS of the two synsets are the same, doesn't need to compare if they have different pos
				if (unSyn.getPOS() == trainSyn.getPOS()) {
					double sim=0; 
					if (wnSimType==0) {
						sim=path.path(unSyn, trainSyn);
					} else if (wnSimType==1){
						sim=lin.lin(unSyn, trainSyn);
					}				
					trainSyn_sim.put(trainSyn, new Double(sim));
				}
				
			}
//			get the first n=5 trainSyn that is most similar to unseen synset
			double avgSim=getNMostSimilarTrainSynsets(trainSyn_sim, Params.nTrainSyn);  //need to add something here if we want to know the exact train synsets
			trainWn_w.put(unSyn, new Double(avgSim));			
		}
	}
	
	/***
	 * given 2 hashmaps, test and train
	 * find keys of test that don't appear in train
	 * 
	 * @param testhm
	 * @param trainhm
	 * @return
	 */
	ArrayList<Object> findUnknownKeys (HashMap testhm, HashMap trainhm) {
		ArrayList<Object> unseens=new ArrayList<Object> ();
		for (Iterator iter=testhm.keySet().iterator(); iter.hasNext(); ) {
			Object k=iter.next();
			if (!trainhm.containsKey(k))
				unseens.add(k);
		}
		return unseens;
	}
	
	/***
	 * 
	 * @param n  number of most simmilar training concepts to be found
	 * @return
	 */
	double getNMostSimilarTrainSynsets (HashMap hm, int n) {
//		first, we need to sort hash by value
		Map sorted=sortByValues(hm);
		int c=0;
	    double sumSim=0;
	    
		for (Iterator iter=sorted.keySet().iterator(); iter.hasNext() && c<n; ) {
			ISynset syn=(ISynset) iter.next();
			Double sim=(Double)hm.get(syn);
			sumSim+=sim;
			System.out.println("most similar synset: "+syn.toString()+","+sim);
		}		
		return (double)sumSim/(double)n;
	}
	
	/***
	 * sort a hashmap by value
	 * 
	 * @param map
	 * @return
	 */
	public <K,V extends Comparable<V>> Map<K,V> sortByValues (final Map<K,V> map) {
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
//		code for generating expanded WordNet vector (wn=1st sense)
		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-1st-exp";
//		int wtype=3;//this is tf/EucLen* idf
//		
		SentenceTokenizer tokenizer = new SentenceTokenizer(Params.tokenizerModel);
		SentenceSplitter splitter = new SentenceSplitter(Params.spliterModel);		
		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbcon=new DBConnection(Params.dbUrl,Params.dbClass, Params.usr,pwd);
		WordNetMatching wnmatch=new WordNetMatching(Params.wnpath);		
		
//		String[] prompts={"098","099","100","101"};
//		String[] folds={"0","1","2"};
//		for (String prompt : Params.prompts) {
//			for (String fold : Params.folds) {
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
//				HashMap syn_idf=trainCol.getSynsetIDF(splitter, tokenizer, dbcon, wnmatch, Params.doPOS); 
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
		
		dbcon.close();
		
		
	}

}
