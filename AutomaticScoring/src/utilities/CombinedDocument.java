package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/***
 * given a document, it can be represented as BOW, WN concepts, Wiki concepts,
 * in this class, different representation vectors are combined to form a new vector for the document
 * 
 * @author miaochen
 *
 */

public class CombinedDocument {
	
	String textDir=null; //dir of the 
	String bowDir=null;
	String wnDir=null;
	String wikiDir=null; //dir of the direct Wiki vector (not ESA)
	String esaDir=null;
	boolean hasBow;
	boolean hasWn;
	boolean hasWiki;
	boolean hasEsa;
	HashMap token_w=null;
	HashMap wn_w=null;
	HashMap wiki_w=null;
	HashMap esa_w=null; //no replacing word (which is, cType=1) for esa vec
	
	/***
	 * 
	 * @param bowDir  dir of the bow vec file
	 * @param wnDir  dir of the wordnet vec file
	 * @param wikiDir dir of the wiki vec file
	 * there is no esa param here
	 * 
	 */
	public CombinedDocument (String textDir, String bowDir, String wnDir, String wikiDir) {		
		this (textDir, bowDir, wnDir, wikiDir, null);
	}
	
	public CombinedDocument (String textDir, String bowDir, String wnDir, String wikiDir, String esaDir) {
		this.textDir=textDir;
		this.bowDir=bowDir;
		this.wnDir=wnDir;
		this.wikiDir=wikiDir;
		this.esaDir=esaDir;
		this.hasBow=(bowDir == null) ? false : true;
		this.hasWn=(wnDir == null) ? false : true;
		this.hasWiki=(wikiDir == null) ? false: true;
		this.hasEsa=(esaDir == null) ? false: true;		
	}
	
	/***
	 * note: we cant' have esa and wiki at the same time!!!
	 * 
	 * @param token_w
	 * @param wn_w
	 * @param wiki_w
	 * @param esa_w
	 * @param cType
	 */
	public CombinedDocument (HashMap token_w, HashMap wn_w, HashMap wiki_w, HashMap esa_w) {
		this.token_w=token_w;
		this.wn_w=wn_w;
		this.wiki_w=wiki_w;
		this.esa_w=esa_w;
		this.hasBow=(token_w == null) ? false : true;
		this.hasWn=(wn_w == null) ? false : true;
		this.hasWiki=(wiki_w == null) ? false: true;
		this.hasEsa=(esa_w == null) ? false: true;
	}
	
	
	/***
	 *  0=aggregate all the vectors
	 *  1=replace bow dimension with concept dimension (if a word can be matched to wn or wiki)
	 */
	public void loadVectors () {
		if (hasBow == true) {
			token_w=FileIO.readFileToHash(bowDir, 0);
		}
		if (hasWn == true) {
			wn_w=FileIO.readFileToHash(wnDir, 0);
		}
		if (hasWiki == true) {
			wiki_w=FileIO.readFileToHash(wikiDir, 1);
		}
		if (hasEsa == true) {
//			if this esaDir does not exist, then we break
//			if (new File(esaDir).exists()) {
				esa_w=readEsaFileToHash(esaDir, 1, Params.esaNum);
//			}			
		}
	}
	
	/***
	 * the esa file is like "wikiid\twikititle\tweight"
	 * 
	 * @param filePath
	 * @param keyType  vType  value type of the hashmap (Double, String, Integer...), usually the key type is String
	 *        vtype: 0=Double
	 *               1=Integer
	 * @param numLines  this claims number of lines we need to read (e.g. can be used for reading ESA vecs)
	 * @return
	 */
	HashMap readEsaFileToHash (String filePath, int keyType, int numLines) {
		HashMap k_v=null;
		int c=0; 
		
		switch (keyType) {
		case 0: 
			k_v=new HashMap<String,Double>();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
				String strLine;
				while((strLine=br.readLine())!=null && c<numLines){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 3) {
						k_v.put(units[0], Double.valueOf(units[2]));
					}
					c++;
				}				
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			break;
		case 1:
			k_v=new HashMap<Integer, Double> ();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
//				System.out.println("this file pointer is null "+br==null);
				String strLine;
				while((strLine=br.readLine())!=null && c<numLines){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 3) {
						k_v.put(Integer.parseInt(units[0]), Double.valueOf(units[2]));
					}
					c++;
				}				
				br.close();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("There is something wrong with the code!");
		}
		return k_v;	
	}
	
	public static void main (String[] args) {
		
	}

}
