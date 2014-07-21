package utilities;

import java.io.File;
import java.util.TreeMap;

import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Path;

/***
 * it computes the similarity between 2 wordnet synsets
 * 
 * @author miaochen
 *
 */

public class SynsetSimilarity {
	
	
	public static void main (String[] args) {
		
		// construct the URL to the WordNet dictionary directory
		String wnhome = "/Users/miaochen/Documents/Software/WordNet/3.0";
		String wnpath = wnhome + File.separator + "dict";		
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbcon=new DBConnection(dbUrl,dbClass,usr,pwd);
		WordNetMatching wnmatch=new WordNetMatching (wnpath);
		
		String dir = "/Users/miaochen/Documents/Software/WordNet";
		JWS	ws = new JWS(dir, "3.0");
		Path path=ws.getPath();
		System.out.println("Path\n");
//		TreeMap<String,Double> scores3=path.path("apple", "banana", "n");//all senses
//		for (String s : scores3.keySet()){
////			System.out.println(s+"\t"+scores3.get(s));
//		}
		double spec=path.path("apple", 1, "banana",1,"n");
		System.out.println(spec);
		
		ISynset syn1=wnmatch.getSynsetInWordnetGivenAWordAndPos("apple", dbcon, "NN");
		ISynset syn2=wnmatch.getSynsetInWordnetGivenAWordAndPos("banana", dbcon, "NN");
		double s2=path.path(syn1, syn2);
		System.out.println(s2);
	}

}
