package utilities;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.POS;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;

/***
 * given a test doc, an array of super files, compute the 2 erater features for the test file
 * also has param of whether expanding/reasoning weight of unseen words in test doc
 * 
 * @author miaochen
 *
 */

public class EraterFeatureProcessor {
	
	Document testdoc=null;
	ArrayList<Document> superdocs=null; //this is an arraylist
	
	WikiXmlDocument wtestdoc=null; //for WikiXmlDocument
	ArrayList<WikiXmlDocument> wsuperdocs=null;
	
	CombinedDocument ctestdoc=null; //for combined docs
	ArrayList<CombinedDocument> csuperdocs=null;
	
	int maxcos=0;
	double cosw4=0;
//	these are obtained from the Params.java code
//	int vType=Params.vType;
	int wType=Params.wType;
//	boolean expand=Params.expand;
	Toolbox toolbox=null;
//	Path path=null;
//	Lin lin=null;
	double sim2=0;  //similarity between the test essay and score level 2
	double sim3=0;  //similarity between the test essay and score level 3
	double sim4=0;  //similarity between the test essay and score level 4
	
	public EraterFeatureProcessor (Document testdoc, ArrayList<Document> superdocs) {
		this.testdoc=testdoc;
		this.superdocs=superdocs;
	}
	
	public EraterFeatureProcessor (Document testdoc, ArrayList<Document> superdocs, Toolbox toolbox) {
		this.testdoc=testdoc;
		this.superdocs=superdocs;
		this.toolbox=toolbox;
	}
	
	public EraterFeatureProcessor (CombinedDocument ctestdoc, ArrayList<CombinedDocument> csuperdocs) {
		this.ctestdoc=ctestdoc;
		this.csuperdocs=csuperdocs;
	}
	
	public EraterFeatureProcessor (WikiXmlDocument wtestdoc, ArrayList<WikiXmlDocument> wsuperdocs) {
		this.wtestdoc=wtestdoc;
		this.wsuperdocs=wsuperdocs;
	}
	
	
	public void computeFeatures () {
		if (Params.vType==0) { //Document object, bow
			getFeaturesWord();
		}else if (Params.vType==1) {			
			getFeaturesWn ();
		}else if (Params.vType==2) {
			getFeaturesWiki ();
		}else if (Params.vType==3) {
			
		}else {
			System.out.println("Wrong vector type!!");
		}
		
	}
	
	
	void getFeaturesWord () {
//		first check if document class is correct
		if (testdoc==null || superdocs==null) {
			System.err.println("Error! Document type error!");
			System.exit(2);
		}
		
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value
		
		HashMap testvec=getDocumentVector(testdoc);
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			HashMap supervec=getDocumentVector(superdocs.get(score-Params.minScore));
			double cossim;
			if (wType==3 || wType==6) {
				cossim=similarity.computeDotProductOnNormFreqBetweenHashMaps(testvec, supervec);
			}else {
				cossim = similarity.computeCosineSimilarityBetweenHashMaps(testvec, supervec);
			}
//			for cosmax feature
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
//			for cosw4 feature
			if (score==Params.maxScore)
				cosw4=cossim;
		}
		
		maxcos=maxPosit;
	}
	
	void getFeaturesWn () {
		
//		first check if document class is correct
		if (testdoc==null || superdocs==null) {
			System.err.println("Error! Document type error!");
			System.exit(3);
		}
		
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value
		
		HashMap testvec=getDocumentVector(testdoc);
		normalizeVec (testvec);
							
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			HashMap supervec=getDocumentVector(superdocs.get(score-Params.minScore));
			double cossim;
			
			normalizeVec (supervec);
			if (wType==3 || wType==6) {				
				cossim=similarity.computeDotProductOnNormFreqBetweenHashMaps(testvec, supervec);
			}else {
				cossim = similarity.computeCosineSimilarityBetweenHashMaps(testvec, supervec);
			}
//			for cosmax feature
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
//			for cosw4 feature
			if (score==Params.maxScore)
				cosw4=cossim;
		}
		
		maxcos=maxPosit;
		System.out.println("Max cos sim is "+maxSim);
		System.out.println("Maxcos feature is "+maxcos);
	}
	
	void getFeaturesWnReasoning (HashMap syn_idf, double dftIDF) {
//		first check if document class is correct
		if (testdoc==null || superdocs==null) {
			System.err.println("Error! Document type error!");
			System.exit(3);
		}
		
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value, maybe need to set to 2 for avoid errors???		
		
//		HashMap testvec=getDocumentVector(testdoc); 
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			System.out.println("Matching supervec "+score+" with the test file");
			 //it's in the loop because we need to change this testvec according to different supervecs
//			HashMap supervec=getDocumentVector(superdocs.get(score-Params.minScore));
			Document superdoc=superdocs.get(score-Params.minScore);
			double cossim;
			
			HashMap expTestVec=expandWnTestVec (syn_idf, dftIDF, toolbox.path, toolbox.lin, Params.wnSimType);
			HashMap expSuperVec=expandWnSuperVec (superdoc, toolbox.path, toolbox.lin, Params.wnSimType);
			normalizeVec (expTestVec);
			normalizeVec (expSuperVec);
			
//			System.out.println("expanded test vec is" +expTestVec);
			
			if (wType==3 || wType==6) {				
				cossim=similarity.computeDotProductOnNormFreqBetweenHashMaps(expTestVec, expSuperVec);
				System.out.println("The cos sim between them is "+cossim);
			}else {
				cossim = similarity.computeCosineSimilarityBetweenHashMaps(expTestVec, expSuperVec);
			}
//			for cosmax feature
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
//			for cosw4 feature
			if (score==Params.maxScore)
				cosw4=cossim;
			
			if (score==2) {
				sim2=cossim;
			}else if (score==3) {
				sim3=cossim;
			}else if (score==4) {
				sim4=cossim;
			}
				
			
		}
		
		maxcos=maxPosit;
		System.out.println("Max cos sim is "+maxSim);
		System.out.println("Maxcos feature is "+maxcos);
	}
	
	void getFeaturesWiki () {
//		first check if document class is correct
		if (wtestdoc==null || wsuperdocs==null) {
			System.err.println("Error! Document type error!");
			System.exit(4);
		}
		
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value
		
		HashMap testvec=getDocumentVector(wtestdoc);
		normalizeVec(testvec);
		
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			HashMap supervec=getDocumentVector(wsuperdocs.get(score-Params.minScore));
			double cossim;
			normalizeVec(supervec);
			
			if (wType==3 || wType==6) {
				cossim=similarity.computeDotProductOnNormFreqBetweenHashMaps(testvec, supervec);
			}else {
				cossim = similarity.computeCosineSimilarityBetweenHashMaps(testvec, supervec);
			}
//			for cosmax feature
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
//			for cosw4 feature
			if (score==Params.maxScore)
				cosw4=cossim;
		}
		maxcos=maxPosit;
	}
	
	void getFeaturesWikiReasoning (HashMap wiki_idf, double dftIDF, Toolbox toolbox) {
//		first check if document class is correct
		if (wtestdoc==null || wsuperdocs==null) {
			System.err.println("Error! Document type error!");
			System.exit(4);
		}
		
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value
		
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			WikiXmlDocument superdoc=wsuperdocs.get(score-Params.minScore);
			double cossim;
			
			HashMap expTestVec=expandWikiTestVec (wiki_idf, dftIDF, Params.wikiSimType, toolbox);
			HashMap expSuperVec=expandWikiSuperVec (superdoc, Params.wikiSimType, toolbox);
			normalizeVec (expTestVec); //normalize the test and super vecs
			normalizeVec (expSuperVec);
			
			if (wType==3 || wType==6) {				
				cossim=similarity.computeDotProductOnNormFreqBetweenHashMaps(expTestVec, expSuperVec);
				System.out.println("The cos sim between them is "+cossim);
			}else {
				cossim = similarity.computeCosineSimilarityBetweenHashMaps(expTestVec, expSuperVec);
			}
//			for cosmax feature
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
//			for cosw4 feature
			if (score==Params.maxScore)
				cosw4=cossim;
			
			if (score==2) {
				sim2=cossim;
			}else if (score==3) {
				sim3=cossim;
			}else if (score==4) {
				sim4=cossim;
			}
		}
		
		maxcos=maxPosit;
		System.out.println("Max cos sim is "+maxSim);
		System.out.println("Maxcos feature is "+maxcos);
	}
	
	
	HashMap getDocumentVector (Object doc) {
		if (Params.vType==0) {
			Document thedoc=(Document) doc;
			return thedoc.word_tfidf;
		} else if (Params.vType==1) {
			Document thedoc=(Document) doc;
			return thedoc.syn_tfidf;
		} else if (Params.vType==2) {
			WikiXmlDocument thedoc=(WikiXmlDocument) doc;
			return thedoc.wiki_tfidf;
		} else if (Params.vType==3) {
			//do something for combined doc here
			return null;
		} else {
			System.out.println("Error in getting document vector!");
			return null;
		}
			
	}
	
	/***
	 * we only compute one type of similarity, either path or lin similarity
	 * 
	 * @param testWn_w
	 * @param trainWn_w
	 * @param path
	 * @param lin
	 * @param wnSimType
	 */
	HashMap expandWnSuperVec (Document superdoc, Path path, Lin lin, int wnSimType) {
		HashMap <ISynset, Double> expSuperVec=new HashMap <ISynset, Double> ();
		double sumw=0;
		for (Iterator iter=superdoc.syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
			ISynset supersyn=(ISynset) iter.next();
			double w=superdoc.syn_tfidf.get(supersyn).doubleValue();
			expSuperVec.put(supersyn, new Double(w));
			sumw+=w;
		}		
//		find which words in testWn don't appear in trainWn, the "unknown" word
		ArrayList<Object> unseens=findUnknownKeys (testdoc.syn_tfidf, superdoc.syn_tfidf);
//		there are 2 cases: unSyn occurs in whole training set but not this super vec, unsyn occurs not in whole training set. now treated in the same way!
//		by reasoning the weight based on similar concepts
		for (Object un : unseens) {
//			System.out.println(un==null);
			ISynset unSyn=(ISynset) un;
			System.out.println("The unseen synset, in super vec: "+unSyn.toString());
			double wUnSyn=0; //this is the weight of the unseen synset, in the trainig super vec
			
			if (wnSimType==0) {
//				build a hashmap of trainSynset => its similarity with the unseen word
				HashMap<ISynset,Double> unTrain_sim=new HashMap<ISynset,Double> (); //stored similarity between unseen synset and training synsets (in supervec)
				for (Iterator iter=superdoc.syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
					ISynset supersyn=(ISynset) iter.next();
//					check if the POS of the two synsets are the same, doesn't need to compare if they have different pos
					if ((unSyn.getPOS() == POS.NOUN && supersyn.getPOS()==POS.NOUN) || (unSyn.getPOS() == POS.VERB && supersyn.getPOS()==POS.VERB)) {
						double sim=0; 
						try {
							sim=path.path(unSyn, supersyn);
//							System.out.println(sim+" : Similarity with "+supersyn.toString());
						}catch (StackOverflowError e) {
							sim=0;
						}
						unTrain_sim.put(supersyn, new Double(sim));
					}
				}
				if (unTrain_sim.isEmpty()) { //check if the hash is empty, is so, then we just skip the unseen word
					continue;
				}
				ArrayList<ISynset> simlarSyns=getNMostSimilarSynsets(unTrain_sim, Params.nTrainSyn);
				double sumweight=0;
				for (ISynset similarSyn : simlarSyns) {
					sumweight+=superdoc.syn_tfidf.get(similarSyn).doubleValue();
				}
				wUnSyn=sumweight/((double) simlarSyns.size()); //was divided by Params.nTrainSyn before, seems wrong...
			}else if (wnSimType==1) {
//				build a hashmap of trainSynset => its similarity with the unseen word
				HashMap<ISynset,Double> unTrain_sim=new HashMap<ISynset,Double> (); //stored similarity between unseen synset and training synsets (in supervec)
				for (Iterator iter=superdoc.syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
					ISynset supersyn=(ISynset) iter.next();
                    if (unSyn.getPOS() == supersyn.getPOS()) {
                    	double sim=0; 
                    	try {
							sim=lin.lin(unSyn, supersyn);
						}catch (StackOverflowError e) {
							sim=0;
						}
                    	unTrain_sim.put(supersyn, new Double(sim));
					}
				}
				if (unTrain_sim.isEmpty()) { //check if the hash is empty, is so, then we just skip the unseen word
					continue;
				}
				ArrayList<ISynset> simlarSyns=getNMostSimilarSynsets(unTrain_sim, Params.nTrainSyn);
				double sumweight=0;
				for (ISynset similarSyn : simlarSyns) {
					sumweight+=superdoc.syn_tfidf.get(similarSyn).doubleValue();				
				}
				wUnSyn=sumweight/((double) simlarSyns.size());
			}else if (wnSimType==2) {
				wUnSyn=sumw/(double)superdoc.syn_tfidf.size();
			}
			expSuperVec.put(unSyn, new Double (wUnSyn));			
		}
		
		return expSuperVec;
	}
	
	HashMap expandWnTestVec (HashMap syn_idf, double dftIDF, Path path, Lin lin, int wnSimType) {
		HashMap<ISynset, Double> expTestVec=new HashMap <ISynset, Double>();
		for (Iterator iter=testdoc.syn_tfidf.keySet().iterator(); iter.hasNext(); ) {
			ISynset testsyn=(ISynset) iter.next();
			if (syn_idf.containsKey(testsyn)) {
				Double w=(Double) testdoc.syn_tfidf.get(testsyn);
				expTestVec.put(testsyn, w);
			}else { //this is an unseen synset then
				System.out.println("Unknown synset, in test vec weight:"+testsyn.toString());
				int freq=(Integer) testdoc.syn_rawfreq.get(testsyn).intValue();
				double idf=0; //this is the idf for the unseen word
				if (wnSimType==0) {  //path similarity
					HashMap<ISynset, Double> unsynVoc_sim=new HashMap<ISynset,Double> ();
					System.out.println("syn_idf length is "+syn_idf.size());
					for (Iterator idfiter=syn_idf.keySet().iterator(); idfiter.hasNext(); ) {
						ISynset idfsyn=(ISynset) idfiter.next();
						if ((testsyn.getPOS()==POS.NOUN && idfsyn.getPOS()==POS.NOUN) || (testsyn.getPOS()==POS.VERB && idfsyn.getPOS()==POS.VERB)) {
							double sim=0;
							try {
								sim=path.path(testsyn, idfsyn);
//								System.out.println(sim+" : Similarity with "+idfsyn.toString());
							}catch (StackOverflowError e) {
								sim=0;
							}
							unsynVoc_sim.put(idfsyn, new Double (sim));
						}
					}
					if (unsynVoc_sim.isEmpty()) { //make sure it's not an empty hashmap
						System.out.println("It's an empty unsynVoc_sim hash! breaking the loop...");
						continue;
					}
					ArrayList<ISynset> similarSyns=getNMostSimilarSynsets(unsynVoc_sim, Params.nTrainSyn);
					double sumidf=0;
					for (ISynset similarSyn : similarSyns) {
						Double wSimilarSyn=(Double) syn_idf.get(similarSyn);
						sumidf+=wSimilarSyn.doubleValue();
					}
					idf=sumidf/(double)similarSyns.size();					
				}else if (wnSimType==1) {  //lin similarity
					HashMap<ISynset, Double> unsynVoc_sim=new HashMap<ISynset,Double> ();
					for (Iterator idfiter=syn_idf.keySet().iterator(); idfiter.hasNext(); ) {
						ISynset idfsyn=(ISynset) idfiter.next();
						if (testsyn.getPOS() == idfsyn.getPOS()) {
							double sim=0;
							try {
								sim=lin.lin(testsyn, idfsyn);
//								System.out.println(sim+" : Similarity with "+idfsyn.toString());
							}catch (StackOverflowError e) {
								sim=0;
							}
							unsynVoc_sim.put(idfsyn, sim);
						}
					}
					if (unsynVoc_sim.isEmpty()) { //make sure it's not an empty hashmap
						System.out.println("It's an empty unsynVoc_sim hash! breaking the loop...");
						continue;
					}
					ArrayList<ISynset> similarSyns=getNMostSimilarSynsets(unsynVoc_sim, Params.nTrainSyn);
					double sumidf=0;
					for (ISynset similarSyn : similarSyns) { //get avg idf of similar concepts
						Double wSimilarSyn=(Double) syn_idf.get(similarSyn);
						sumidf+=wSimilarSyn.doubleValue();
					}
					idf=sumidf/(double)similarSyns.size();
				}else if (wnSimType==2) {  //dft similarity
					idf=dftIDF;
				}
				expTestVec.put(testsyn, new Double ((double)freq*idf));
			}			
			
		}		
		return expTestVec;
	}
	
	HashMap expandWikiSuperVec (WikiXmlDocument superdoc, int wikiSimType, Toolbox toolbox) {
		HashMap <Integer, Double> expSuperVec=new HashMap <Integer, Double> ();
		double sumw=0;
		for (Iterator iter=superdoc.wiki_tfidf.keySet().iterator(); iter.hasNext();) {
			Integer sueprWikiId=(Integer) iter.next();
			Double w=superdoc.wiki_tfidf.get(sueprWikiId);
			expSuperVec.put(sueprWikiId, w);
			sumw+=w;
		}
//		find which words in testWiki don't appear in trainWiki, the "unknown" wiki id
		ArrayList<Object> unseens=findUnknownKeys(wtestdoc.wiki_tfidf,superdoc.wiki_tfidf);
		
		for (Object un: unseens) {
			Integer unWikiId=(Integer) un;
			System.out.println("The unseen WikiID, for supervec, is: "+unWikiId.toString());
			double wUnWiki=0;  //this is the weight of the unseen wiki id, in the super vec
			
			if (wikiSimType==0) {
//				build a hashmap of trainWikiIds => its similarity with the unseen wiki id
				HashMap <Integer, Double> unTrain_sim=new HashMap <Integer, Double> ();
//				store similarity between unseen wiki id and training wiki ids (in supervec)
				for (Iterator iter=superdoc.wiki_tfidf.keySet().iterator(); iter.hasNext(); ) {
					Integer superWikiId=(Integer) iter.next();
					WikiSimilarity wikiSimilarity=new WikiSimilarity(unWikiId.intValue(), superWikiId.intValue());
					double sim=wikiSimilarity.getLinkSimilarity();
					unTrain_sim.put(superWikiId, new Double(sim));
				}
				if (unTrain_sim.isEmpty())
					continue;
				ArrayList<Integer> similarWikiIds=getNMostSimilarWikiIds(unTrain_sim, Params.wikiSimType);
				double sumweight=0;
				for (Integer similarWikiId : similarWikiIds) {
					sumweight+=superdoc.wiki_tfidf.get(similarWikiId).doubleValue();
				}
				wUnWiki=sumweight/(double)similarWikiIds.size();
			}else if (wikiSimType==1) {
//				build a hashmap of trainWikiIds => its similarity with the unseen wiki id
				HashMap <Integer, Double> unTrain_sim=new HashMap <Integer, Double> ();
//				store similarity between unseen wiki id and training wiki ids (in supervec)
				for (Iterator iter=superdoc.wiki_tfidf.keySet().iterator(); iter.hasNext(); ) {
					Integer superWikiId=(Integer) iter.next();
					WikiSimilarity wikiSimilarity=new WikiSimilarity(unWikiId.intValue(), superWikiId.intValue());
					double sim=wikiSimilarity.getContentSimilarity(toolbox);
					unTrain_sim.put(superWikiId, new Double(sim));
				}
				if (unTrain_sim.isEmpty())
					continue;
				ArrayList<Integer> similarWikiIds=getNMostSimilarWikiIds(unTrain_sim, Params.wikiSimType);
				double sumweight=0;
				for (Integer similarWikiId : similarWikiIds) {
					sumweight+=superdoc.wiki_tfidf.get(similarWikiId).doubleValue();
				}
				wUnWiki=sumweight/(double)similarWikiIds.size();
			}else if (wikiSimType==2) {
				wUnWiki=sumw/(double) superdoc.wiki_tfidf.size();
			}
			expSuperVec.put(unWikiId, new Double(wUnWiki));
		}
		return expSuperVec;
	}
	
	
	HashMap expandWikiTestVec (HashMap wiki_idf, double dftIDF, int wikiSimType, Toolbox toolbox) {
		HashMap<Integer, Double> expTestVec=new HashMap <Integer, Double>();		
		
		for (Iterator iter=wtestdoc.wiki_tfidf.keySet().iterator(); iter.hasNext(); ) {
			Integer testWikiId=(Integer) iter.next();
			if (wiki_idf.containsKey(testWikiId)) {
				Double w=(Double) wtestdoc.wiki_tfidf.get(testWikiId);
				expTestVec.put(testWikiId, w);
			}else { //this is an unseen wiki id then
				System.out.println("It's an unknown concept: "+testWikiId);
				int freq=(Integer) wtestdoc.wiki_rawfreq.get(testWikiId).intValue();
				double idf=0; //this is the idf for the unseen wiki id
				if (wikiSimType==0) {  //for link similarity
					HashMap <Integer, Double> unseenVoc_sim=new HashMap <Integer, Double> ();
					for (Iterator idfiter=wiki_idf.keySet().iterator(); idfiter.hasNext(); ) {
						Integer idfWikiId=(Integer) idfiter.next();
						WikiSimilarity wikiSimilarity=new WikiSimilarity (testWikiId.intValue(), idfWikiId.intValue());
						double sim=wikiSimilarity.getLinkSimilarity();
						unseenVoc_sim.put(idfWikiId, new Double (sim));						
					}
					if (unseenVoc_sim.isEmpty()) {
						System.out.println("It's an empty unseenVoc_sim hash! breaking the loop...");
						continue;
					}
					ArrayList<Integer> similarWikiIds=getNMostSimilarWikiIds (unseenVoc_sim,Params.nTrainSyn);
					double sumidf=0;
					for (Integer similarWikiId : similarWikiIds) {
						Double wSimilarWikiId=(Double) wiki_idf.get(similarWikiId);
						sumidf+=wSimilarWikiId.doubleValue();
					}
					idf=sumidf/(double)similarWikiIds.size();
				}else if (wikiSimType==1) { //1=content similarity
					HashMap <Integer, Double> unseenVoc_sim=new HashMap <Integer, Double> ();
					for (Iterator idfiter=wiki_idf.keySet().iterator(); idfiter.hasNext(); ) {
						Integer idfWikiId=(Integer) idfiter.next();
						WikiSimilarity wikiSimilarity=new WikiSimilarity (testWikiId.intValue(), idfWikiId.intValue());
						double sim=wikiSimilarity.getContentSimilarity(toolbox);
						unseenVoc_sim.put(idfWikiId, new Double (sim));						
					}
					if (unseenVoc_sim.isEmpty()) {
						System.out.println("It's an empty unseenVoc_sim hash! breaking the loop...");
						continue;
					}
					ArrayList<Integer> similarWikiIds=getNMostSimilarWikiIds (unseenVoc_sim,Params.nTrainSyn);
					double sumidf=0;
					for (Integer similarWikiId : similarWikiIds) {
						Double wSimilarWikiId=(Double) wiki_idf.get(similarWikiId);
						sumidf+=wSimilarWikiId.doubleValue();
					}
					idf=sumidf/(double)similarWikiIds.size();
				}else if (wikiSimType==2) { //for default similarity, by averaging all idf values (then *tf)
					idf=dftIDF;
				}
				expTestVec.put(testWikiId, new Double ((double)freq*idf));
			}
		}
		
		return expTestVec;
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
//	double getNMostSimilarTrainSynsets (HashMap hm, int n) {
//		
//		if (hm==null) //make sure the input hash is not null
//			return 0;
//		
////		first, we need to sort hash by value
//		Map sorted=sortByValues(hm);
//		int c=0;
//	    double sumSim=0;
//	    
//		for (Iterator iter=sorted.keySet().iterator(); iter.hasNext() && c<n; ) {
//			c++;
//			ISynset syn=(ISynset) iter.next();
//			Double sim=(Double)hm.get(syn);
//			sumSim+=sim;
//			System.out.println("most similar synset: "+syn.toString()+","+sim);
//		}		
//		return (double)sumSim/(double)n;
//	}
	
       ArrayList<ISynset> getNMostSimilarSynsets (HashMap hm, int n) {
		
		  if (hm==null) //make sure the input hash is not null
			return null;
		
		  ArrayList<ISynset> similarSyns=new ArrayList<ISynset> ();
		
//		  first, we need to sort hash by value
		  Map sorted=sortByValues(hm);
		  int c=0;
	    
		  for (Iterator iter=sorted.keySet().iterator(); iter.hasNext() && c<n; ) {
			  c++;
			  ISynset syn=(ISynset) iter.next();
			  similarSyns.add(syn);
			  System.out.println("most similar synset: "+syn.toString()+", their sim value is "+hm.get(syn));
		  }		
		  return similarSyns;
	}
	
        ArrayList<Integer> getNMostSimilarWikiIds (HashMap hm, int n) {
        	
        	if (hm==null) //make sure the input hash is not null
        		return null;
        	
        	ArrayList<Integer> similarWikiIds=new ArrayList<Integer> ();
        	
        	Map sorted=sortByValues(hm);
        	int c=0;
        	
        	for (Iterator iter=sorted.keySet().iterator(); iter.hasNext() && c<n; ) {
        		c++;
        		Integer wikiId=(Integer) iter.next();
        		similarWikiIds.add(wikiId);
        		System.out.println("most similar synset: "+wikiId.toString());
        	}
        	
        	return similarWikiIds;
        }
	
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
	
	void normalizeVec (HashMap hm) {
		double norm=0;
		for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ) {
			Object k=(Object) iter.next();
			Double w=(Double) hm.get(k);
			norm+=w.doubleValue()*w.doubleValue();
		}
		double sqrt=Math.sqrt(norm);
		if (sqrt==0) { //quit if sqrt=0
			return;
		}
		for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ) {
			Object k=(Object) iter.next();
			Double w=(Double) hm.get(k);
			hm.put(k, new Double(w.doubleValue()/sqrt));
		}
	}
	
	
	public static void main (String[] args) {
		
	}

}
