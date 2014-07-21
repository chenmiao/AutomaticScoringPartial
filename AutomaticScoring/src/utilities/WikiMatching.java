package utilities;

import java.util.ArrayList;
import java.util.List;

/***
 * Given a sentence, return the matched Wikipedia concepts,
 * the match follows the maximum length criteria
 * 
 * @author miaochen
 *
 */

public class WikiMatching {
	
	String sentence=null;
	List<String> tokenList=new ArrayList<String> ();
	DBConnection dbcon;
	
	WikiMatching (String sentence, SentenceTokenizer tokenizer, DBConnection dbcon){
		
		this.sentence=sentence;
		tokenList=tokenizer.tokenizeToList(sentence);
		this.dbcon=dbcon;
		
	}
	
	/***
	 * 
	 * The way it works if by finding the maximum matched string in the sentence
	 * starting from beginning of the sentence, get the string from beginning to the end and see if there 
	 * is a match; if not, then move forward a token to see if there is a match; if there is a match, then
	 * skip this match and start searching the next match
	 * 
	 * @return
	 */
	List<Integer> match (int ngram) {
		
		List<Integer> matchedWiki=new ArrayList<Integer> ();
				
		for (int currentPointer=0; currentPointer<tokenList.size();){
			//see if length of (the currentPointer to the end string) < ngram
			if ((tokenList.size()-currentPointer) < ngram){
				ngram=tokenList.size()-currentPointer;
			}
			
			//set a pointer at the back part of nGramString
			int sBackPointer=0;
			while (sBackPointer < ngram){
//				this is part of the n-gram string starting from the currentPointer
				String partNgram=getStringGivenStartIndex(currentPointer,ngram-sBackPointer);
				WikiPage wpage=findStringInWiki(partNgram);
				
				if (wpage.id != -1){//that means there is a match
					System.out.println("Found match: "+partNgram);
					matchedWiki.add(new Integer(wpage.id));
					sBackPointer++;//before break, increase the sBackPointer for purpose of calculating currentPointer
					break;
				}else{
					sBackPointer++;
//					System.out.println("Didn't find match");
				}
			}
			//set the current pointer to a new place
			currentPointer=currentPointer+(ngram-sBackPointer)+1;
		}
		
		return matchedWiki;
	}
	
	/***
	 * Given a token index in the sentence, return the sentence part including and after this word 
	 * 
	 * @param idx
	 * @return
	 */
	String remainingSentString(int idx){
		
		String remSentence="";
		
		for (int i=idx; i<tokenList.size(); i++){
			
			remSentence+=tokenList.get(i)+" ";
			
		}
		
		return remSentence.trim();
		
	}
	
	//given start index, get the string within its ngram length, or the string reaching the end of the sentence
	String getStringGivenStartIndex(int currentPointer, int ngram){
		String out="";
		for (int i=0; i<ngram && (currentPointer+i)<tokenList.size(); i++){
			out+=tokenList.get(currentPointer+i)+" ";
			
		}
		return out.trim();
	}
	
	WikiPage findStringInWiki (String inString){
		
//		need to work on this with the Wiki database
		return dbcon.findWikiConcept(inString);
//		return new WikiPage (-1,null);
				
	}
	
	public static void main (String[] args){
		
		String modelPath = "/Users/miaochen/Documents/Software/OpenNLP/models/EnglishTok.bin.gz";
		SentenceTokenizer stoken = new SentenceTokenizer(modelPath);
		String sentence = "Los Angeles Lakers visited Washington State University yesterday.";
		
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/wikiData";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbCon=new DBConnection(dbUrl,dbClass,usr,pwd);
		
		WikiMatching wmatch=new WikiMatching(sentence, stoken, dbCon);
		wmatch.match(5);
		
		dbCon.close();
	}

}
