package utilities;

import java.util.HashMap;

import edu.mit.jwi.item.POS;

public class Constants {

	public static final HashMap<String, POS> penn_wnpos = new HashMap<String, POS> ();
	
	static {
		penn_wnpos.put("NN", POS.NOUN);
		penn_wnpos.put("NNS", POS.NOUN);
		penn_wnpos.put("NNP", POS.NOUN);
		penn_wnpos.put("NNPS", POS.NOUN);
		penn_wnpos.put("VB", POS.VERB);
		penn_wnpos.put("VBD", POS.VERB);
		penn_wnpos.put("VBG", POS.VERB);
		penn_wnpos.put("VBN", POS.VERB);
		penn_wnpos.put("VBP", POS.VERB);
		penn_wnpos.put("VBZ", POS.VERB);
		penn_wnpos.put("JJ", POS.ADJECTIVE);
		penn_wnpos.put("JJR", POS.ADJECTIVE);
		penn_wnpos.put("JJS", POS.ADJECTIVE);
		penn_wnpos.put("RB", POS.ADVERB);
		penn_wnpos.put("RBR", POS.ADVERB);
		penn_wnpos.put("RBS", POS.ADVERB);
		penn_wnpos.put("WRB", POS.ADVERB);
	}
	
	
	public static void main (String[] args) {
		
		System.out.println(penn_wnpos);
		
	}

}
