package utilities;

import java.io.File;
import java.net.URL;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

/***
 * Given a word, find the matching WordNet synsets
 * 
 * @author miaochen
 * 
 */
public class WordNetMatching {

	Dictionary dict;
	WordnetStemmer stemmer;

	WordNetMatching() {
		
	}

	WordNetMatching(String wnPath) {
		URL url;
		try {
			url = new URL("file", null, wnPath);
			// construcct the dictionary object and open it
			dict = new Dictionary(url);
			dict.open();
			
			stemmer=new WordnetStemmer (dict);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	this returns SynsetID
	ISynsetID getFirstSynsetIDInWordnetGivenAWord (String wordString, DBConnection dbcon) {
		ISynsetID synid=null;
//		we first find the stem for the word, i.e. running => run
		String wordStem=getdWordStem (wordString);
		
//		this is a failure in getting stem of a word, which also means the word is not in WordNet
		if (wordStem == null) {
			return null;
		}
		
//		this is the place where we use the mysql WordNet database
		POS pos=dbcon.findPosOfFirstSenseInWordnetGivenAWord(wordStem);
//	    means we get something back from the WordNet database
		if (pos != null) {
			IIndexWord idxWord = dict.getIndexWord(wordStem, pos);
//			make sure we can find this word + pos combination in WordNet (flatfile)
			if (idxWord != null) {
//				get its 1st sense in the corresponding POS tag
				IWordID wordID = idxWord.getWordIDs().get(0);
				IWord word = dict.getWord(wordID);
				synid=word.getSynset().getID();
			} else {
//				this also indicates inconsistency in records of flat file and database WordNet
				System.out.println(wordStem+" isn't in WordNet!");
			}
			
		}
		
		return synid;
	}
	
//	this returns Synset
	ISynset getFirstSynsetInWordnetGivenAWord (String wordString, DBConnection dbcon) {
		ISynset syn=null;
//		we first find the stem for the word, i.e. running => run
		String wordStem=getdWordStem (wordString);
		
//		this is a failure in getting stem of a word, which also means the word is not in WordNet		
		if (wordStem == null) {
			return null;
		}
		
//		this is the place where we use the mysql WordNet database
		POS pos=dbcon.findPosOfFirstSenseInWordnetGivenAWord(wordStem);
//	    means we get something back from the WordNet database
		if (pos != null) {
			IIndexWord idxWord = dict.getIndexWord(wordStem, pos);
//			make sure we can find this word + pos combination in WordNet (flatfile)
			if (idxWord != null) {
//				get its 1st sense in the corresponding POS tag
				IWordID wordID = idxWord.getWordIDs().get(0);
				IWord word = dict.getWord(wordID);
				syn=word.getSynset();
			} else {
//				this also indicates inconsistency in records of flat file and database WordNet
				System.out.println(wordStem+" isn't in WordNet!");
			}
			
		}
		
		return syn;
	}
	
//	this returns SynsetID
	ISynsetID getSynsetIDInWordnetGivenAWordAndPos (String wordString, DBConnection dbcon, String pennpos) {
		ISynsetID synid=null;
		
//		map penn tree pos to wordnet-style pos
		POS pos=matchPenntreePosToWnPos(pennpos);
//		we first find the stem for the word, i.e. running => run
		String wordStem=getdWordStem (wordString, pos);
		
//		this is a failure in getting stem of a word, which also means the word is not in WordNet
		if (wordStem == null) {
			return null;
		}		

//	    means we get something back from the WordNet database
		if (pos != null) {
			IIndexWord idxWord = dict.getIndexWord(wordStem, pos);
//			make sure we can find this word + pos combination in WordNet (flatfile)
			if (idxWord != null) {
//				get its 1st sense in the corresponding POS tag
//				if a word + pos combination has more than 1 result, then get the first returned result, 
//				e.g. "change" as a noun has multiple results, then we get the 1st one
				IWordID wordID = idxWord.getWordIDs().get(0);
				IWord word = dict.getWord(wordID);
				synid=word.getSynset().getID();
			} else {
//				this also indicates inconsistency in records of flat file and database WordNet
//				or there is no such word+pos combination in WordNet db, especially for the pos=true case
				System.out.println(wordStem+" isn't in WordNet!");
			}
			
		}
		
		return synid;
	}
	
//	this returns Synset
	ISynset getSynsetInWordnetGivenAWordAndPos (String wordString, DBConnection dbcon, String pennpos) {
		ISynset syn=null;
		
//		map penn tree pos to wordnet-style pos
		POS pos=matchPenntreePosToWnPos(pennpos);
//		we first find the stem for the word, i.e. running => run
		String wordStem=getdWordStem (wordString, pos);
		
//		this is a failure in getting stem of a word, which also means the word is not in WordNet
		if (wordStem == null) {
			return null;
		}		

//	    means we get something back from the WordNet database
		if (pos != null) {
			IIndexWord idxWord = dict.getIndexWord(wordStem, pos);
//			make sure we can find this word + pos combination in WordNet (flatfile)
			if (idxWord != null) {
//				get its 1st sense in the corresponding POS tag
//				if a word + pos combination has more than 1 result, then get the first returned result, 
//				e.g. "change" as a noun has multiple results, then we get the 1st one
				IWordID wordID = idxWord.getWordIDs().get(0);
				IWord word = dict.getWord(wordID);
				syn=word.getSynset();
			} else {
//				this also indicates inconsistency in records of flat file and database WordNet
//				or there is no such word+pos combination in WordNet db, especially for the pos=true case
				System.out.println(wordStem+" isn't in WordNet!");
			}
			
		}
		
		return syn;
	}
	
	String getdWordStem (String wordString) {
//		NOTE: this function returns a null if the word doesn't exist in Wordnet database
//		i.e. stem("she") => null 
//		we don't care about POS of the word here
		List<String> stems=stemmer.findStems(wordString, null); 
		if (stems.size() > 0) 
			return stems.get(0);
		else
			return null;
	}
	
	String getdWordStem (String wordString, POS pos) {
//		NOTE: this function returns a null if the word doesn't exist in Wordnet database
//		i.e. stem("she") => null 
//		we do care about POS of the word here
		List<String> stems=stemmer.findStems(wordString, pos); 
		if (stems.size() > 0) 
			return stems.get(0);
		else
			return null;
	}
	
	/***
	 * it matches penn tree bank pos tags to word net style pos
	 * 
	 * @return
	 */
	POS matchPenntreePosToWnPos (String pennPos) {
		if (Constants.penn_wnpos.containsKey(pennPos) )
			return Constants.penn_wnpos.get(pennPos);
		else 
			return null;
		
	}

	public static void main(String[] args) {

		// construct the URL to the WordNet dictionary directory
		String wnhome = "/Users/miaochen/Documents/Software/WordNet/3.0";
		String wnpath = wnhome + File.separator + "dict";
		
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);

		// look up first sense of a word
		String wordString="one";
//		need to first stem this word to its original format!!!
		
		WordNetMatching wnmatch=new WordNetMatching (wnpath);
//		System.out.println(wnmatch.getFirstSynsetInWordnetGivenAWord(wordString, dbcon).toString());
		System.out.println(wnmatch.getSynsetInWordnetGivenAWordAndPos(wordString, dbcon, "NN"));
//		String wordstem=wnmatch.getdWordStem(wordString);
//		System.out.println(wordstem);
//		
//		POS pos=dbcon.findPosOfFirstSenseInWordnetGivenAWord(wordstem);
//		System.out.println(pos.toString());
//		System.out.println(wnmatch.getdWordStem(wordstem, pos));
		
		
//		ISynset syn=wnmatch.getFirstSynsetInWordnetGivenAWord(wordString, dbcon);
//		System.out.println(syn.toString());
//		System.out.println(syn.getGloss());
		
//		for (IWord word : syn.getWords()) {
//			System.out.println(word.toString());
//		}

	}

}
