package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class SingleEssayRater {
	
	String textFolder; //folder storing the original text
	String bowFolder;
	String wnFolder;
	String wikiFolder;
	String esaFolder;	
	String scoreList;
	int prompt;
	int fold;
	
	int maxScore = 4;
	int minScore = 2;
	
	
	public SingleEssayRater (String textFolder, String bowFolder, String wnFolder, String wikiFolder, String esaFolder, String scoreList, 
			int prompt, int fold) {
		this.textFolder=textFolder;
		this.bowFolder=bowFolder;
		this.wnFolder=wnFolder;
		this.wikiFolder=wikiFolder;
		this.esaFolder=esaFolder;
		this.scoreList=scoreList;
		this.prompt=prompt;
		this.fold=fold;
	}
	
	void getEraterResults (String filename) {
		HashMap<String, Integer> file_score = filescorelistToHash();
//		get the super CombinedDocument
		String superFolderText= textFolder+File.separator+prompt+File.separator+"super"+File.separator
				+"super"+fold;
		String superFolderBow=(bowFolder!=null) ? bowFolder+File.separator+prompt+File.separator+"super"+File.separator
				+"super"+fold : null;
		String superFolderWn=(wnFolder!=null) ? wnFolder+File.separator+prompt+File.separator+"super"+File.separator
				+"super"+fold : null;
		String superFolderWiki=(wikiFolder!=null) ? wikiFolder+File.separator+prompt+File.separator+"super"+File.separator
		+"super"+fold: null;
		String superFolderEsa= (esaFolder!=null) ? esaFolder+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold : null;
		
//		the test folders of different representations
		String testFolderText=textFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
		String testFolderBow=(bowFolder!=null) ? (bowFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold) : null;
		String testFolderWn=(wnFolder!=null) ? (wnFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold) : null;
		String testFolderWiki=(wikiFolder!=null) ? (wikiFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold) : null;
		String testFolderEsa=(esaFolder!=null) ? (esaFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold) : null;
		
//		get the super CombinedDocument
		ArrayList<CombinedDocument> cSuperDocList=getSuperCombinedDocsInList(superFolderText, superFolderBow
				, superFolderWn, superFolderWiki, superFolderEsa);
		int score = getScoreGivenFilename(file_score, filename);
		System.out.println("The actual score is "+score);
//		this is the combined test document
		CombinedDocument cTestDoc=new CombinedDocument(testFolderText+File.separator+filename
				, getFilePathGivenFolderAndFilename(testFolderBow,filename), getFilePathGivenFolderAndFilename(testFolderWn,filename)
				,getFilePathGivenFolderAndFilename(testFolderWiki,filename), getFilePathGivenFolderAndFilename(testFolderEsa, filename));
		int f1=getMaxCosFeature(cTestDoc, cSuperDocList);
		System.out.println("The predicted score is "+score);		
	}
	
	int getMaxCosFeature (CombinedDocument cTestDoc, ArrayList<CombinedDocument> cSuperDocList) {
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value, like score "2" or so; it's an array because maybe two score
					     // categories share the same simialrity with the essay (although this is very low probability)
//		the max numVec is 3: bowVec, wnVec, and wikiVec

		for (int score = minScore; score < maxScore + 1; score++) {
			CombinedDocument cSuperDoc=cSuperDocList.get(score-minScore);			
//			an important presupposition: the bow vec is normalized by Euclidean length, so is WN and Wiki vector
//			that makes the cossim computation much easier, just get the (sum of dotproduct)/num of not null vec
			double cossim=similarity.computeCosSimBetweenCombinedDocs (cTestDoc, cSuperDoc);
			System.out.println("Its similarity with "+score+" is "+cossim);
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
		}
		return maxPosit;
	}
	
	ArrayList<CombinedDocument> getSuperCombinedDocsInList (String superFolderText, String superFolderBow, String superFolderWn, String superFolderWiki, String superFolderEsa) {
		ArrayList<CombinedDocument> cSuperDocList=new ArrayList<CombinedDocument> ();
		for (int score = minScore; score < maxScore + 1; score++) {
			// #this superFile name is customized and is subject to change
			CombinedDocument cSuperDoc=new CombinedDocument (superFolderText+File.separator+Integer.toString(score)
					, getFilePathGivenFolderAndFilename(superFolderBow,Integer.toString(score))
					, getFilePathGivenFolderAndFilename(superFolderWn, Integer.toString(score))
					, getFilePathGivenFolderAndFilename(superFolderWiki, Integer.toString(score))
					, getFilePathGivenFolderAndFilename(superFolderEsa, Integer.toString(score)));
			cSuperDocList.add(cSuperDoc);
		}
		return cSuperDocList;
	}
	
	/***
	 * converts the scoreList file to a hashmap
	 * 
	 * @return
	 */
	HashMap<String, Integer> filescorelistToHash() {
		HashMap<String, Integer> f_s = new HashMap<String, Integer>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(scoreList))));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] units = strLine.trim().split(",");
				if (units.length == 2) {
					f_s.put(units[0], Integer.valueOf(units[1]));
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return f_s;
	}
	
	/***
	 * if the folder is not null, then return folder/filename
	 * if folder is null, then return null as file path
	 * 
	 * @param folder
	 * @param filename
	 * @return
	 */
	String getFilePathGivenFolderAndFilename (String folder, String filename) {
		return (folder != null) ? (folder+File.separator+filename) : null;
	}
	
	int getScoreGivenFilename(HashMap<String, Integer> file_score, String filename) {
		int score = -1;
		String f = filename.substring(0, filename.length() - 4);

		if (file_score.containsKey(f)) {
			score = file_score.get(f);
		}
		return score;
	}
	
	
	public static void main (String[] args) {
		
	}
	

}
