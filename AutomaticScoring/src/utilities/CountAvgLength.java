package utilities;

import java.io.File;

public class CountAvgLength {
	
	/***
	 * given tokenized text, a b c, something like that, go throught the string list, and 
	 * substract number of punctuations to get the real number of words
	 * 
	 * @param text
	 */
	static int countRealWords (String text) {
		String[] tokens=text.split("\\s+");
		int numtokens=tokens.length;
		
		for (int i=0; i<numtokens; i++) {
			if (tokens[i].equals(".") || tokens[i].equals(",") || tokens[i].equals("?") || tokens[i].equals("!")) {
				numtokens--;
			}
		}
		return numtokens;
	}
	
	
	public static void main (String[] args) {
		
		String infolder="/Users/miaochen/Documents/diss-experiment/orig-corpus/all-1237files";
		String[] filenames=new File(infolder).list();
		Toolbox toolbox=new Toolbox(true,false,false,false,false,false,false);
		toolbox.enableTools();
		
		int numDoc=0;
		int numTokens=0;
		
		for (String filename : filenames) {
			numDoc++;
			String fdir=infolder+File.separator+filename;
			String text=FileIO.getFileAsString(fdir);
			String tokened=toolbox.tokenizer.tokenize(text);
			int realtokens=countRealWords(tokened);
			numTokens+=realtokens;
		}
		
		System.out.println("number of doc: "+numDoc);
		System.out.println("number of tokens: "+numTokens);
		System.out.println("avg number of tokens per doc: "+((double)numTokens/(double)numDoc));
	}
	

}
