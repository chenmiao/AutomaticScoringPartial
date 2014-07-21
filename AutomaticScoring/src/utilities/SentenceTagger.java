package utilities;

import java.io.IOException;
import java.util.ArrayList;


import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSTaggerME;

public class SentenceTagger {
	
	private POSTaggerME tagger = null;
	
	public SentenceTagger(String modelPath, String tagdict) {
		try {
			tagger = new PosTagger(modelPath, new POSDictionary(tagdict, true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String tag(String tokenizeResult) {
		String tagResult = "";
		if (tagger != null) {
			tagResult = tagger.tag(tokenizeResult);
		}
		return tagResult;
	}
	
	/***
	 * the output is list of list, which is a list containing 2 lists, looks like this:
	 * There are four people ...
	 * EX    VBP  CD   NNS
	 * 
	 * @param tokenizeResult
	 * @return
	 */
	public ArrayList<ArrayList<String>> getTokensAndTagsInSeparateLists (String tokenizeResult) {
		ArrayList<ArrayList<String>> lists=new ArrayList<ArrayList<String>> ();
		ArrayList<String> tokens=new ArrayList<String> ();
		ArrayList<String> tags=new ArrayList<String> ();
		
		String tagResult = null;
		if (tagger != null) {
			tagResult=tagger.tag(tokenizeResult);
//			get word/tag pairs
			String[] pairs=tagResult.split("\\s+");
//			make sure there is something in the tagging result
			if (pairs.length > 0) {
				for (String pair : pairs) {
					String[] units=pair.split("/");
					if (units.length==2) {
						tokens.add(units[0]);
						tags.add(units[1]);
					}
				}
			}
		}
		lists.add(tokens);
		lists.add(tags);
		return lists;
	}
	
	/***
	 * the output is list of list, which is a list containing 2 lists, looks like this:
	 * There are four people ...
	 * EX    VBP  CD   NNS
	 * 
	 * it also handles punctuation, by excluding them from the lists of tokens/tags
	 * 
	 * @param tokenizeResult
	 * @return
	 */
	public ArrayList<ArrayList<String>> getTokensAndTagsInSeparateListsNoPunctuation (String tokenizeResult) {
		ArrayList<ArrayList<String>> lists=new ArrayList<ArrayList<String>> ();
		ArrayList<String> tokens=new ArrayList<String> ();
		ArrayList<String> tags=new ArrayList<String> ();
		
		String tagResult = null;
		if (tagger != null) {
			tagResult=tagger.tag(tokenizeResult);
//			get word/tag pairs
			String[] pairs=tagResult.split("\\s+");
//			make sure there is something in the tagging result
			if (pairs.length > 0) {
				for (String pair : pairs) {
					String[] units=pair.split("/");
					if (units.length==2) {
//						excluding punctuations from the list of tokens/tags
						if (!isPunctuation(units[0])) {
							tokens.add(units[0]);
							tags.add(units[1]);
						}
						
					}
				}
			}
		}
		lists.add(tokens);
		lists.add(tags);
		return lists;
	}
	
	boolean isPunctuation (String token){
		return token.equals(".")||token.equals("!")||token.equals("?")||token.equals(",");
	}
	
	
	public static void main (String[] args) {
		
		SentenceTagger tagger = new SentenceTagger(
				"/Users/miaochen/Documents/Software/OpenNLP/models/tag.bin.gz",
				"/Users/miaochen/Documents/Software/OpenNLP/models/tagdict");
		String tokenizeResult = "There are four people in the meeting ...";
		String tagResult = tagger.tag(tokenizeResult);
		System.out.println(tagResult);
		ArrayList<ArrayList<String>> lists=tagger.getTokensAndTagsInSeparateListsNoPunctuation(tokenizeResult);
		System.out.println(lists);
		
	}

}
