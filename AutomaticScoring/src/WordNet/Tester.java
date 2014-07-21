package WordNet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.*;
import edu.mit.jwi.item.*;

public class Tester {
	
	IDictionary dict=null;
	
	Tester(){
		//construct the URL to the WordNet dictionary directory
		String wnhome="/Users/miaochen/Documents/Software/WordNet/3.0";
		String path=wnhome+File.separator+"dict";
		URL url;
		try {
			url = new URL("file",null,path);

			//construcct the dictionary object and open it
			dict=new Dictionary(url);
			dict.open();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testDictionary() throws IOException{
		//look up first sense of the word "dog"
		IIndexWord idxWord=dict.getIndexWord("cats",POS.NOUN);
		IWordID wordID=idxWord.getWordIDs().get(0);
		IWord word=dict.getWord(wordID);
		System.out.println("Id = "+wordID);
		System.out.println("Lemma = "+word.getLemma());
		System.out.println("Gloss = "+word.getSynset().getGloss());
	}
	
	public void getSynonyms (){
		//look up first sense of the word "dog" as a noun
		IIndexWord idxWord=dict.getIndexWord("dog",POS.NOUN);
		IWordID wordID=idxWord.getWordIDs().get(0);//this is getting the first sense
		IWord word=dict.getWord(wordID);
		ISynset synset=word.getSynset();
		
		//iterate over words associated with the synset
		for(IWord w : synset.getWords()){
			System.out.println(w.getLemma());
		}
	}
	
	public void getHypernyms(){
		//get teh synset
		IIndexWord idxWord=dict.getIndexWord("cat",POS.NOUN);
		IWordID wordID=idxWord.getWordIDs().get(0);//1st meaning
		IWord word=dict.getWord(wordID);
		ISynset synset=word.getSynset();
		
		//get the hypernyms
		List<ISynsetID> hypernyms=synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		//print out each hypernym's id and synonyms
		List<IWord> words;
		for(ISynsetID sid : hypernyms){
			words=dict.getSynset(sid).getWords();
			System.out.print(sid+" {");
			for(Iterator<IWord> i=words.iterator();i.hasNext();){
				System.out.print(i.next().getLemma());
				if(i.hasNext())
					System.out.print(", ");
			}
			System.out.println("}");
		}
	}
	
	public static void main(String[] args) throws IOException{
		Tester tester=new Tester();
		tester.testDictionary();
//		tester.getSynonyms();
//		tester.getHypernyms();
	}

}
