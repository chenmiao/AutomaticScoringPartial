package utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.lang.english.Tokenizer;

public class SentenceTokenizer {
	
	private Tokenizer tokenizer = null;

	public SentenceTokenizer(String modelPath) {
		try {
			tokenizer = new Tokenizer(modelPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/****
	 * It tokenizes a sentence, and output in a string delimited by whitespace
	 * Note: it doesn't count punctuations
	 * 
	 * @param sentence
	 * @return
	 */
	public String tokenize(String sentence) {
		StringBuffer tokenizeResult = new StringBuffer("");
		if (tokenizer != null) {
			String[] tokens = tokenizer.tokenize(sentence);
			if (tokens.length > 0) {
				tokenizeResult.append(tokens[0]);
			}
			for (int ti = 1, tn = tokens.length; ti < tn; ti++) {
				tokenizeResult.append(" " + tokens[ti]);
			}
		}
		return tokenizeResult.toString();
	}
	
	public List<String> tokenizeToList(String sentence){
		
		List<String> tokenList=new ArrayList<String>();
		
		if (tokenizer != null) {
			String[] tokens = tokenizer.tokenize(sentence);
			if (tokens.length > 0) {
				for (int ti=0; ti<tokens.length; ti++){
					tokenList.add(tokens[ti]);
				}
							
			}

		}
		
		return tokenList;
		
	}
	

	public static void main(String[] args) throws IOException {
		String modelPath = "/Users/miaochen/Software/javalibs/models/EnglishTok.bin.gz";
		SentenceTokenizer stoken = new SentenceTokenizer(modelPath);
		String sentence = "There are four people in the meeting... I would like to talk about opennlp today. I think it's a good package";
//		String sentence="2.1.4 Half cell SOFCs ";
		String tokenizeResult = stoken.tokenize(sentence);
		System.out.println(tokenizeResult);
		
//		List<String> tlist=stoken.tokenizeToList(sentence);
//		for (String t: tlist){
//			System.out.println(t);
//		}
		
	}
	

}
