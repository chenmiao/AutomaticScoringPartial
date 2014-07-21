package wekacode;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import utilities.FileIO;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.NaiveBayesSimple;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.classifiers.Evaluation;

public class Evaluator {
	
	/**
	 * Calculates the weighted (by class size) F-Measure.
	 * 
	 * @return the weighted F-Measure.
	 */
	public static double weightedFMeasure(int numClasses,
			int[][] confusionMatrix) {

		double[] classCounts = new double[numClasses];
		double classCountSum = 0;

		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				classCounts[i] += confusionMatrix[i][j];
			}
			classCountSum += classCounts[i];
		}

		double fMeasureTotal = 0;
		for (int i = 0; i < numClasses; i++) {
			// compute f-measure of class i, for preparing computing the final
			// f-measure (macro-averaged)
			double temp = fMeasure(i, numClasses, confusionMatrix);
			fMeasureTotal += (temp * classCounts[i]);
		}

		return fMeasureTotal / classCountSum;
	}
	
	public static double avgFMeasure(int numClasses,
			int[][] confusionMatrix) {

		double fMeasureTotal = 0;
		for (int i = 0; i < numClasses; i++) {
			// compute f-measure of class i, for preparing computing the final
			// f-measure (macro-averaged)
			double temp = fMeasure(i, numClasses, confusionMatrix);
			fMeasureTotal += temp;
		}

		return fMeasureTotal / numClasses;
	}
	

	/**
	 * Calculate the F-Measure with respect to a particular class. This is
	 * defined as
	 * <p/>
	 * 
	 * <pre>
	 * 2 * recall * precision
	 * ----------------------
	 *   recall + precision
	 * </pre>
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return the F-Measure
	 */
	public static double fMeasure(int classIndex, int numClasses,
			int[][] confusionMatrix) {

		double precision = precision(classIndex, numClasses, confusionMatrix);
		double recall = recall(classIndex, numClasses, confusionMatrix);
		if ((precision + recall) == 0) {
			return 0;
		}
		return 2 * precision * recall / (precision + recall);
	}

	/**
	 * Calculate the precision with respect to a particular class. This is
	 * defined as
	 * <p/>
	 * 
	 * <pre>
	 * correctly classified positives
	 * ------------------------------
	 *  total predicted as positive
	 * </pre>
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return the precision
	 */
	public static double precision(int classIndex, int numClasses,
			int[][] confusionMatrix) {

		double correct = 0, total = 0;
		for (int i = 0; i < numClasses; i++) {
			if (i == classIndex) {
				correct += confusionMatrix[i][classIndex];
			}
			total += confusionMatrix[i][classIndex];
		}
		if (total == 0) {
			return 0;
		}
		return correct / total;
	}
	
	public static double accuracyRate (int numClasses, int[][] confusionMatrix) {
		int totalIns=0;
		int correctIns=0;
		
		for (int i=0; i < numClasses; i++) {
			correctIns+=confusionMatrix[i][i];
			for (int j=0; j < numClasses; j++) {
				totalIns+=confusionMatrix[i][j];
			}			
		}
		
		double accu=(totalIns==0) ? 0 : (double)correctIns/(double)totalIns;
		return accu;
	}
	

	/**
	 * Calculate the true positive rate with respect to a particular class. This
	 * is defined as
	 * <p/>
	 * 
	 * <pre>
	 * correctly classified positives
	 * ------------------------------
	 *       total positives
	 * </pre>
	 * 
	 * @param classIndex
	 *            the index of the class to consider as "positive"
	 * @return the true positive rate
	 */
	public static double recall(int classIndex, int numClasses,
			int[][] confusionMatrix) {

		double correct = 0, total = 0;
		for (int j = 0; j < numClasses; j++) {
			if (j == classIndex) {
				correct += confusionMatrix[classIndex][j];
			}
			total += confusionMatrix[classIndex][j];
		}
		if (total == 0) {
			return 0;
		}
		return correct / total;
	}
	
	/***
	 * this computes Pearson correlation between 2 arrays
	 * 
	 * @return
	 */
	static double pcorrelation(ArrayList<Double> a1, ArrayList<Double> a2) {
		double r = 0;
		// make sure the two lists have the same size
		if (a1.size() == a2.size()) {
			double[] array1 = new double[a1.size()];
			double[] array2 = new double[a1.size()];
			// copy the 2 arrays to double[] respectively
			for (int i = 0; i < a1.size(); i++) {
				array1[i] = a1.get(i).doubleValue();
				array2[i] = a2.get(i).doubleValue();
			}
			PearsonsCorrelation corr = new PearsonsCorrelation();
			r = corr.correlation(array1, array2);
		}
		return r;
	}
	
	/***
	 * given an Integer arraylist, convert it to a Double arraylist
	 * 
	 * @param li
	 * @return
	 */
	static ArrayList<Double> convertIntegerArrayToDouble(ArrayList<Integer> li) {
		ArrayList<Double> doubleli = new ArrayList<Double>();
		for (Integer elem : li) {
			double doubledElem = (double) elem.intValue();
			doubleli.add(new Double(doubledElem));
		}
		return doubleli;
	}
	
	static void addNumberNTimesToArray (ArrayList<Integer> ary, int n, int theNumber) {
		
		for (int i=0; i<n; i++) {
			ary.add(new Integer(theNumber));
		}
		
	}
	
	static String writeStringNTimes (int n, String s) {
		
		String w="";
		for (int i=0; i<n; i++) {
			w+=s;
		}
		
		return w;
	}
	
	
	
	public static void main (String[] args){
		
		String inFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn1st-vec-sorted";  //param1
		String outDir="/Users/miaochen/Documents/diss-experiment/results/wn1st/nb/wekaNB-fmeasure.txt";  //param2
		String csvFolder="/Users/miaochen/Documents/diss-experiment/results/wn1st/nb";  //param3
		String out="";		
		
		int numFolds=3;
		int numClasses=3;				
//		String prompt="098";
		String[] prompts={"098","099","100","101"};
		
		double sumWF=0;
		double sumAF=0;
		double sumAccu=0;
		double sumCorr=0;
		
		int[] scores={2,3,4};
		
		for (String prompt : prompts){
			
			String csvout="pred,actual\n"; //this for recording pred and actual scores (for convenience of computing kappa)
			
//			System.out.println("*********************************\nProcessing prompt "+prompt);
			out+="*********************************\nProcessing prompt "+prompt+"\n";
			
			int[][] confusionMatrix=new int[numClasses][numClasses];
			ArrayList<Integer> arrayActual=new ArrayList<Integer> ();
			ArrayList<Integer> arrayPred=new ArrayList<Integer> ();
			
			for(int k=0;k<numFolds;k++){
				
//				System.out.println("Processing data fold "+k);
				out+="Processing data fold "+k+"\n";
				
				String trainArff=inFolder+File.separator+prompt+"-train"+(new Integer(k)).toString()+"-score.arff";
				String testArff=inFolder+File.separator+prompt+"-test"+(new Integer(k)).toString()+"-score.arff";
				
				Instances trainIns=WekaIO.readArffToInstances(trainArff);
				Instances testIns=WekaIO.readArffToInstances(testArff);
				
				trainIns.setClassIndex(trainIns.numAttributes()-1);
				testIns.setClassIndex(testIns.numAttributes()-1);
				
				Classifier NBcls=new NaiveBayesMultinomial();
//				Classifier NBcls=new LibSVM();
				try {
					NBcls.buildClassifier(trainIns);
					Evaluation eval=new Evaluation(trainIns);
					eval.evaluateModel(NBcls, testIns);
					
					double[][] subConfusionMatrix=eval.confusionMatrix();
				    for (int i=0; i<numClasses; i++){
				    	for (int j=0; j<numClasses; j++){
				    		confusionMatrix[i][j]+=subConfusionMatrix[i][j];
				    		int actual=scores[i];
				    		int pred=scores[j];
				    		int count=(int) subConfusionMatrix[i][j];
				    		
				    		addNumberNTimesToArray (arrayActual, count, actual);
				    		addNumberNTimesToArray (arrayPred, count, pred);
				    		
				    		
				    		
				    		csvout+=writeStringNTimes (count, Integer.toString(pred)+","+Integer.toString(actual)+"\n");
//				    		csvout+=pred+","+actual+"\n";
				    	}
				    }
				    //this fmeasure has nothing to do with the final f-measure!!!
//				    System.out.println("Weighted F-measure for fold "+k+" is "+weightedFMeasure(numClasses,subConfusionMatrix));
//				    out+="Weighted F-measure for fold "+k+" is "+weightedFMeasure(numClasses,subConfusionMatrix)+"\n";
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
//			compute correlation (or max.cos)
			double corr=pcorrelation(convertIntegerArrayToDouble(arrayPred), convertIntegerArrayToDouble(arrayActual));
			System.out.println(arrayActual);
			
//			System.out.println("Printing confusion matrix:");
			for (int i=0; i<numClasses; i++){
		    	for (int j=0; j<numClasses; j++){
//		    		  System.out.print(Utils.doubleToString(confusionMatrix[i][j], 7, 0));
		    		  out+=Utils.doubleToString(confusionMatrix[i][j], 7, 0);
		    	  }
//		    	System.out.println();
		    	out+="\n";
		    }
			
			double wFmeasure=weightedFMeasure(numClasses, confusionMatrix);
			double aFmeasure=avgFMeasure(numClasses, confusionMatrix);
			double accu=accuracyRate(numClasses, confusionMatrix);
			sumWF+=wFmeasure;
			sumAF+=aFmeasure;
			sumAccu+=accu;
			sumCorr+=corr;
			
//			System.out.println("The total F-measure is "+weightedFMeasure(numClasses, confusionMatrix));
			out+="The total weighted F-measure for prompt "+prompt+" is "+wFmeasure+"\n\n";
			out+="The total avg F-measure for prompt "+prompt+" is "+aFmeasure+"\n\n";
			out+="The accuracy rate for prompt  "+prompt+" is "+accu+"\n";
			out+="The correlation for prompt "+prompt+" is "+corr+"\n";
			
			String csvDir=csvFolder+File.separator+prompt+"-NB-pred.csv";
			FileIO.writeFile(csvDir, csvout);
		}
		
		out+="Average weighted F measure is "+sumWF/(double)4+"\n";
		out+="Average F measure is "+sumAF/(double)4+"\n";
		out+="Average accuracy is "+sumAccu/(double)4+"\n";
		out+="Average correlation is "+sumCorr/(double)4+"\n";
		
		WekaIO.writeText(outDir, out);
		System.out.println(out);
		
	}

}
