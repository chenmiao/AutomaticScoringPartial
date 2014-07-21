package utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.mit.jwi.item.ISynset;


/***
 * this is for exmaining synset, which causes dimensionality reduction
 * e.g. make.v.01 and do.v.02 are under the same synset, we need to detect such merging cases from text
 * 
 * @author miaochen
 *
 */
public class SynsetCheck {
	
	/***
	 * the returned string doesn't contain extension such as ".txt"
	 * 
	 * @return
	 */
	static String getFilenameFromDir (String dir) {
		String fname=null;
		
		String[] items=dir.split(File.separator);
		if (items.length > 0) {
			String thename=items[items.length-1];
			fname=thename.substring(0,thename.length()-4);
		}
		
		return fname;
	}
	
	static void addSynAndWordsToHash (String filedir, HashMap<ISynset,String> syn_words, HashMap<String,String> sid_files, 
			boolean doPOS, Toolbox toolbox) {
		
		String text=FileIO.getFileAsString(filedir);
//		System.out.println(text);
		String filename=getFilenameFromDir(filedir);
        String[] sentences = toolbox.splitter.split(text);
		
//		get the first sense
		if (doPOS == false) { //1st sense
			for (String sentence : sentences){
				List<String> tokens=toolbox.tokenizer.tokenizeToList(sentence);
				for (String token : tokens) {
					ISynset syn=toolbox.wnmatch.getFirstSynsetInWordnetGivenAWord(token, toolbox.dbcon);
//					didn't find this word in WordNet
					if (syn == null)
						continue;
					
					if (syn_words.containsKey(syn)) { //for syn_words hashmap
						String words=syn_words.get(syn);
//						check if this word is already recorded, by checking sid_files
						if (!sid_files.containsKey(syn.getID().toString()+"-"+token)) {
							syn_words.put(syn, words+","+token);
						}						
					}else {
						syn_words.put(syn, token);
					}
					
					String sidword=syn.getID().toString()+"-"+token;
					if (!sid_files.containsKey(sidword)) { //for sid_files hashmap
						sid_files.put(sidword, filename);
					}else {
						String fnames=sid_files.get(sidword);
						sid_files.put(sidword, fnames+","+filename);
					}
				}
			}			
		}else { //pos case
			
			for (String sentence : sentences) {
//				put tokens in a list, and corresponding tags in another list
				ArrayList<ArrayList<String>> lists=toolbox.tagger.getTokensAndTagsInSeparateListsNoPunctuation(
						toolbox.tokenizer.tokenize(sentence));
				ArrayList<String> tokens=lists.get(0);
				ArrayList<String> tags=lists.get(1);
				int len=tokens.size(); //size of a token or tag list
				for (int i=0; i< len; i++) {
					ISynset syn=toolbox.wnmatch.getSynsetInWordnetGivenAWordAndPos(
							tokens.get(i), toolbox.dbcon, tags.get(i).toUpperCase());
//					if there is no such word+pos in WordNet
					if (syn == null) 
						continue;
					
					if (syn_words.containsKey(syn)) {
						String words=syn_words.get(syn);
						if (!sid_files.containsKey(syn.getID().toString()+"-"+tokens.get(i))) {
							syn_words.put(syn, words+","+tokens.get(i));
						}						
					}else {
						syn_words.put(syn, tokens.get(i));
					}
					
					String sidword=syn.getID().toString()+"-"+tokens.get(i);
					if (!sid_files.containsKey(sidword)) {
						sid_files.put(sidword, filename);
					}else {
						String fnames=sid_files.get(sidword);
						sid_files.put(sidword, fnames+","+filename);
					}
				}
			}
			
		}
		
	}
	
	
	public static void main (String[] args) {
		
//		param 1
		String trainFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds/098/train/train0"; 
		boolean doPOS=true; //param 2
		String outFolder="/Users/miaochen/Documents/diss-experiment/notes/wnpos";  //param 3
		
//		records synset => synonymous words appearing in the corpus. e.g. synsetx => {make, do}
		HashMap<ISynset, String> syn_words=new HashMap<ISynset, String> ();
//		sid-word => file than these words occur, e.g. sid-make => transcriptx, transcripty
		HashMap<String, String> sid_files=new HashMap<String, String> ();
		Toolbox toolbox=new Toolbox(true,true,true,true,true,false,false); //param 4
		toolbox.enableTools();
		
		String[] trainfiles=new File(trainFolder).list();
		for (String trainfile : trainfiles) {
			System.out.println("Processing "+trainfile);
			String trainfiledir=trainFolder+File.separator+trainfile;  //this is text file instead of synset hash file
			addSynAndWordsToHash (trainfiledir, syn_words, sid_files, doPOS, toolbox);			
		}
		
		FileIO.writeHashMapToFile(syn_words, outFolder+File.separator+"098-syn_words.txt");
		FileIO.writeHashMapToFile(sid_files, outFolder+File.separator+"098-sid_files.txt");		
		
	}

}
