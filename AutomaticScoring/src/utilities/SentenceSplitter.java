package utilities;

import java.io.File;
import java.io.IOException;

import opennlp.maxent.io.SuffixSensitiveGISModelReader;
import opennlp.tools.sentdetect.SentenceDetectorME;

/***
 * given a document, it splits it into sentences
 * 
 * @author miaochen
 *
 */

public class SentenceSplitter {
	
	private SentenceDetectorME sdetector = null;

	public SentenceSplitter(String modelPath) {
		try {
			sdetector = new SentenceDetectorME(
					(new SuffixSensitiveGISModelReader(new File(modelPath)))
							.getModel());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String[] split(String doc) {
		String[] sentences = null;
		if (sdetector != null) {
			sentences = sdetector.sentDetect(doc);
		}
		return sentences;
	}
	
	public double[] getProb(){
		return sdetector.getSentenceProbabilities(); 
	}

	public static void main(String[] args) {
		String modelPath = "/Users/miaochen/Software/javalibs/EnglishSD.bin.gz";
		SentenceSplitter sp = new SentenceSplitter(modelPath);
		String doc=FileIO.getFileAsString("/Users/miaochen/Documents/diss-experiment/proc-corpus/test/1.txt");
		doc=doc.replaceAll("\\s+", " ");
		System.out.println(doc);
		
		String[] sentences = sp.split(doc);
		if (sentences != null) {
			for (String sentence : sentences)
				System.out.println(sentence);
		}
		
//		for(String doctest:docs){
//			System.out.println("a new line*****");
//			String[] sentences = sp.split(doctest);
//			double[] probs=sp.sdetector.getSentenceProbabilities();
//			System.out.println("The size of array is "+probs.length);
//			for(double prob:probs){
//				System.out.println(prob);
//			}
//			if (sentences != null) {
//				for (String sentence : sentences)
//					System.out.println(sentence);
//			}
//		}
		
	}

}
