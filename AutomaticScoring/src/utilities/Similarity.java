package utilities;

import java.util.HashMap;
import java.util.Iterator;

/***
 * it computes similarity between 2 documents
 * 
 * @author miaochen
 *
 */

public class Similarity {
	
	Similarity (){
		
	}
	
	/***
	 * this similarity is for tf weighting (raw count), using dot product
	 * this can be used on vectors of words or vectors of Wiki concepts
	 * 
	 * this is for id/token => integer frequency
	 * 
	 * @param hm1
	 * @param hm2
	 * @return
	 */
	int computeDotProductOnRawFreq (HashMap hm1, HashMap hm2){
		int sim=0;
		
		for (Iterator iterator = hm1.keySet().iterator(); iterator.hasNext();)
	    {
			Object token = iterator.next();
			if (hm2.containsKey(token)){
				Integer freq1=(Integer) hm1.get(token);
				Integer freq2=(Integer) hm2.get(token);
				sim+=freq1.intValue()*freq2.intValue();				
			}
	    }
		
		return sim;		
	}
	
	double computeCosSimilarityOnRawFreq (HashMap hm1, HashMap hm2) {
		
		double sim=0;
		
		int dot=computeDotProductOnRawFreq(hm1, hm2);
		
		int sqrt1=0;
		for (Iterator iter=hm1.keySet().iterator(); iter.hasNext(); ) {
			Object k=iter.next();
			Integer v=(Integer) hm1.get(k);
			sqrt1+=v.intValue()*v.intValue();
		}
		int sqrt2=0;
		for (Iterator iter=hm2.keySet().iterator(); iter.hasNext(); ) {
			Object k=iter.next();
			Integer v=(Integer) hm2.get(k);
			sqrt2+=v.intValue()*v.intValue();
		}
		if ((sqrt1==0) || (sqrt2==0)) {  //make sure there is no 0
			return sim;
		}else {
			sim=dot/(Math.sqrt(sqrt1*sqrt2));
			return sim;
		}
		
	}
	
	
	
	/***
	 * given two hashmaps as doc vectors(each is like token=>weight), compute the dot product of them
	 * 
	 * this is for token/id => (double) weight
	 * 
	 * @param hm1
	 * @param hm2
	 * @return
	 */
	double computeDotProductOnNormFreqBetweenHashMaps (HashMap hm1, HashMap hm2) {
		double sim=0;
//		deal with hm==null case
		if (hm1==null || hm2==null)
			return 0;

		for (Iterator iter = hm1.keySet().iterator(); iter.hasNext();){
			Object token=(Object) iter.next();
			if (hm2.containsKey(token)){
				Double w1=(Double) hm1.get(token);
				Double w2=(Double) hm2.get(token);
				sim+=w1.doubleValue()*w2.doubleValue();				
			}
		}
		return sim;
	}
	
	/***
	 * cos similarity=a*b/|a||b|
	 * the input is token => weight, and weight has to be Double type
	 * 
	 * @param hm1
	 * @param hm2
	 * @return
	 */
	double computeCosineSimilarityBetweenHashMaps (HashMap hm1, HashMap hm2) {		
		double dotProduct=computeDotProductOnNormFreqBetweenHashMaps(hm1,hm2);
		
		double eucLen1=getEuclideanLength(hm1);
		double eucLen2=getEuclideanLength(hm2);
		
		return dotProduct/(eucLen1*eucLen2);
	}
	
	/***
	 * Given a vector, reutrn its Euclidean Length, will be used for computing cosine similarity
	 * 
	 * @param hm
	 * @return
	 */
	double getEuclideanLength (HashMap hm){
		double len=0;
		
		for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ){
			Object token=(Object) iter.next();
			Double w=(Double) hm.get(token);
			len+=w.doubleValue()*w.doubleValue();
		}
		
		return Math.sqrt(len);
	}
	
	/***
	 * 
	 * @param c1
	 * @param c2
	 * @param cType  the combination type: 0=add, 1=replace
	 * @return
	 */
	public double computeCosSimBetweenCombinedDocs(CombinedDocument c1,CombinedDocument c2) {
//		first count number of non-null vectors (bowVec, wnVec, wikiVec), for convenience of computing Euclidean length
//		I'm assuming the bowVec/wnVec/wikiVec has been normalized by Euclidean length, so their
//		Euclidean length is 1 (if the vec is not null)
		int numVec=0;
		double sim=0.0;
		int wType=Params.wType; //!!!note this comes from wType in params!!!		
		double wBow=Params.wBow;  //weight of bow similarity
		double wWn=Params.wWn;  //weight of wordnet similarity
		double wWiki=Params.wWiki; //weight of wiki similarity
		double wEsa=Params.wEsa; //weight of esa similarity
		boolean sameSimWeighting=Params.sameSimWeighting;  //whetehr if all the similarity types are of the same weighting 
		double sumw=0; //check whether all w is summed to 1, just a security purpose check
		
		boolean bowVecNormalized=Params.bowVecNormalized; //get info from Params to see if these vecs are normalized
		boolean wnVecNormalized=Params.wnVecNormalized;
		boolean wikiVecNormalized=Params.wikiVecNormalized;
		boolean esaVecNoramlized=Params.esaVecNormalized;
		
//		don't forget to load vectors for CombinedDocument before computing similarity!!!
//		it seems to be more efficient, not sure though...
		c1.loadVectors();
		c2.loadVectors();
		
		double bowSim=0;
		double wnSim=0;
		double wikiSim=0;
		double esaSim=0;
		
		if (c1.hasBow) {
			numVec++;
			
			if (bowVecNormalized) {
				bowSim=computeDotProductOnNormFreqBetweenHashMaps(c1.token_w, c2.token_w);
			}else {
				bowSim=sim+=computeCosineSimilarityBetweenHashMaps(c1.token_w, c2.token_w);
			}
			
		}
		if (c1.hasWn) {
			numVec++;
			
			if (wnVecNormalized) {
				wnSim=computeDotProductOnNormFreqBetweenHashMaps(c1.wn_w, c2.wn_w);
			}else {
				wnSim=computeCosineSimilarityBetweenHashMaps(c1.wn_w, c2.wn_w);
			}
		}
		if (c1.hasWiki) {
			numVec++;
			
			if (wikiVecNormalized) {
				wikiSim=computeDotProductOnNormFreqBetweenHashMaps(c1.wiki_w, c2.wiki_w);
			}else {
				wikiSim=computeCosineSimilarityBetweenHashMaps(c1.wiki_w, c2.wiki_w);
			}
		}
		if (c1.hasEsa) {
			numVec++;
			
			if (esaVecNoramlized) {
				esaSim=computeDotProductOnNormFreqBetweenHashMaps(c1.esa_w, c2.esa_w);
			}else {
				esaSim=computeCosineSimilarityBetweenHashMaps(c1.esa_w, c2.esa_w);
			}
		}
		
		if (sameSimWeighting) {  //this is for assigning weights to similarity from different vecs
			sim=bowSim + wnSim + wikiSim + esaSim;
		}else {
			sim=wBow*bowSim + wWn*wnSim + wWiki*wikiSim + wEsa*esaSim;
		}
		
		return sim;
		
//		if (sameSimWeighting) {
//			if (wType==4) { //this is the boolean weight case
//				if (c1.hasBow) { 
//					numVec++;
//					sim+=computeCosineSimilarityBetweenHashMaps(c1.token_w, c2.token_w);
//				}
//				if (c1.hasWn) {
//					numVec++;
//					sim+=computeCosineSimilarityBetweenHashMaps(c1.wn_w, c2.wn_w);
//				}
//				if (c1.hasWiki) {
//					numVec++;
//					sim+=computeCosineSimilarityBetweenHashMaps(c1.wiki_w, c2.wiki_w);
//				}
//				if (c1.hasEsa) {
//					numVec++;
//					sim+=computeCosineSimilarityBetweenHashMaps(c1.esa_w, c2.esa_w);
//				}
//			}else {  //this is the non-boolean weight case, e.g. tfidf
//				if (c1.hasBow) {
//					numVec++;
//					sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.token_w, c2.token_w);
//				}		
//				if (c1.hasWn) {
//					numVec++;
//					sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.wn_w, c2.wn_w);
//				}
//					
//				if (c1.hasWiki) {
//					numVec++;
//					sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.wiki_w, c2.wiki_w);
//				}
//				
//				if (c1.hasEsa) {
//					numVec++;
//					sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.esa_w, c2.esa_w);
//				}
//			}
//			return sim/(double)(numVec);
//		}else {
//			if (wType==4) { //this is the boolean weight case
//				if (c1.hasBow) { 
//					numVec++;
//					sim+=wBow*computeCosineSimilarityBetweenHashMaps(c1.token_w, c2.token_w);
//					sumw+=wBow;
//				}
//				if (c1.hasWn) {
//					numVec++;
//					sim+=wWn*computeCosineSimilarityBetweenHashMaps(c1.wn_w, c2.wn_w);
//					sumw+=wWn;
//				}
//				if (c1.hasWiki) {
//					numVec++;
//					sim+=wWiki*computeCosineSimilarityBetweenHashMaps(c1.wiki_w, c2.wiki_w);
//					sumw+=wWiki;
//				}
//				if (c1.hasEsa) {
//					numVec++;
//					sim+=wEsa*computeCosineSimilarityBetweenHashMaps(c1.esa_w, c2.esa_w);
//					sumw+=wEsa;
//				}
//			}else {  //this is the non-boolean weight case, e.g. tfidf
//				if (c1.hasBow) {
//					numVec++;
//					sim+=wBow*computeDotProductOnNormFreqBetweenHashMaps(c1.token_w, c2.token_w);
//					sumw+=wBow;
//				}		
//				if (c1.hasWn) {
//					numVec++;
//					sim+=wWn*computeDotProductOnNormFreqBetweenHashMaps(c1.wn_w, c2.wn_w);
//					sumw+=wWn;
//				}
//					
//				if (c1.hasWiki) {
//					numVec++;
//					sim+=wWiki*computeDotProductOnNormFreqBetweenHashMaps(c1.wiki_w, c2.wiki_w);
//					sumw+=wWiki;
//				}
//				
//				if (c1.hasEsa) {
//					numVec++;
//					sim+=wEsa*computeDotProductOnNormFreqBetweenHashMaps(c1.esa_w, c2.esa_w);
//					sumw+=wEsa;
//				}
////				if (sumw!=1.0) { //the numbers do not always add to 1.0, weird
////					System.out.println("sumw="+sumw);
//////					System.out.println("There is error in assinging weights to the similarities!");
////				}
//					
//					
//			}
//			return sim;
//		}

	}
	
	/***
	 * 
	 * @param c1
	 * @param c2
	 * @param cType  the combination type: 0=add, 1=replace
	 * @return
	 */
//	public double computeCosSimBetweenCombinedDocsWithEsa (CombinedDocument c1,CombinedDocument c2, double esaSim) {
////		first count number of non-null vectors (bowVec, wnVec, wikiVec), for convenience of computing Euclidean length
////		I'm assuing the bowVec/wnVec/wikiVec has been normalized by Euclidean length, so their
////		Euclidean length is 1 (if the vec is not null)
////		the totalSim=esaSim+cossimBow+cosSimWn+...
//		
//		int numVec=1; //it starts from 1 because the esa vec is always there
//		double sim=esaSim;
//		
////		don't forget to load vectors for CombinedDocument before computing similarity!!!
////		it seems to be more efficient, not sure though...
//		c1.loadVectors();
//		c2.loadVectors();
//		
//		if (c1.hasBow) {
//			numVec++;
//			sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.token_w, c2.token_w);
//		}		
//		if (c1.hasWn) {
//			numVec++;
//			sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.wn_w, c2.wn_w);
//		}
//			
//		if (c1.hasWiki) {
//			numVec++;
//			sim+=computeDotProductOnNormFreqBetweenHashMaps(c1.wiki_w, c2.wiki_w);
//		}
//	
//		return sim/(double)(numVec);
//	}
	
	/***
	 * given 2 files/hashmap, compute their similarity and also output the middle product
	 * 
	 * @return
	 */
	String checkSimilarity (HashMap hm1, HashMap hm2) {
		
		double sim=0;
		int c=0;
		String out="";
		
//		deal with hm==null case
		if (hm1==null || hm2==null)
			out+="Both hashmaps are null!";

		for (Iterator iter = hm1.keySet().iterator(); iter.hasNext();){
			Object token=(Object) iter.next();
			if (hm2.containsKey(token)){
				Double w1=(Double) hm1.get(token);
				Double w2=(Double) hm2.get(token);
				double thissim=w1.doubleValue()*w2.doubleValue();
				sim+=thissim;
				
				c+=1;
				out+=thissim+" "+token.toString()+":"+w1+","+w2+"\n";
			}
		}
		
		out+="Total similarity is "+sim+"\n";
		out+="Total matched terms are "+c;
		return out;
	}
	
	
	
	public static void main (String[] args){
		
		String file1=
				"/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-vec-stopped-sorted/098/test/test0/7545576-VB531098.txt";
		String file2="/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-vec-stopped-sorted/098/super/super0/2";
//		String file1=
//				"/Users/miaochen/Documents/diss-experiment/proc-corpus/wn1st-vec-sorted/098/test/test0/7545576-VB531098.txt";
//		String file2="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn1st-vec-sorted/098/super/super0/3";
		HashMap hm1=FileIO.readFileToHash(file1, 0);
		HashMap hm2=FileIO.readFileToHash(file2, 0);
		
		Similarity sim=new Similarity();
		String out=sim.checkSimilarity(hm1, hm2);
		System.out.println(out);
		
	}

	

}
