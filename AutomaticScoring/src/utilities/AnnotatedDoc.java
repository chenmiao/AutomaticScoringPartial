package utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.mit.jwi.item.ISynset;

/***
 * given a text doc, it's to annotate words in the doc with their WN concepts
 * 
 * @author miaochen
 *
 */

public class AnnotatedDoc {
	
	String text;
	String wnAnnoText=new String("");
	
	AnnotatedDoc () {
		
	}
	
	AnnotatedDoc (String text) {
//		NOTE: here we replace spaces/tab/line break with a single " "
		this.text=text.replaceAll("\\s+", " ");	
	}
	
	AnnotatedDoc (File afile){
//		NOTE: here we replace spaces/tab/line break with a single " "
		this.text=FileIO.getFileObjectAsString(afile).replaceAll("\\s+", " ");
//		System.out.println(text);
	}
	
	void annotateTextWithWN (SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, 
			WordNetMatching wnmatch, boolean doPOS, SentenceTagger tagger) {
		
		
		String anno1=new String(""); //this just gets the 1st word in the synset
		String anno2=new String(""); //this shows all words in the synset
		String[] sentences = splitter.split(text);
		
//		get the first sense
		if (doPOS == false) {
			for (String sentence : sentences){
				List<String> tokens=tokenizer.tokenizeToList(sentence);
				for (String token : tokens) {
					anno1+=token+" ";
					anno2+=token+" ";
					ISynset syn=wnmatch.getFirstSynsetInWordnetGivenAWord(token, dbcon);
//					didn't find this word in WordNet
					if (syn == null) {
						continue;
					}else {
						anno1+="<a href=\"\">["+syn.getWord(1).toString()+"]</a> ";
						anno2+="<a href=\"\">["+syn.toString()+"]</a> ";
					}
				}
			}
		} else if (doPOS == true) {//first do POS
			for (String sentence : sentences) {
//				put tokens in a list, and corresponding tags in another list
				ArrayList<ArrayList<String>> lists=tagger.getTokensAndTagsInSeparateListsNoPunctuation(
						tokenizer.tokenize(sentence));
				ArrayList<String> tokens=lists.get(0);
				ArrayList<String> tags=lists.get(1);
				int len=tokens.size(); //size of a token or tag list
				for (int i=0; i< len; i++) {
					anno1+=tokens.get(i)+" ["+tags.get(i)+"] ";
					anno2+=tokens.get(i)+" ["+tags.get(i)+"] ";
					ISynset syn=wnmatch.getSynsetInWordnetGivenAWordAndPos(
							tokens.get(i), dbcon, tags.get(i).toUpperCase());
//					if there is no such word+pos in WordNet
					if (syn == null) {
						continue;
					}else {
						anno1+="<a href=\"\">["+syn.getWord(1).toString()+"]</a> ";		
						anno2+="<a href=\"\">["+syn.toString()+"]</a> ";	
					}
				}
			}
		}
		
		wnAnnoText+=text+"<br>\n<br>\n"+anno1+"<br>\n<br>\n"+anno2+"<br>\n";
	}
	
	
	public static void main (String[] args) {
		
	}
	

}
