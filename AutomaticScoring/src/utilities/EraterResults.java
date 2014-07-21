package utilities;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;

import weka.core.Utils;

public class EraterResults {
	
	/***
	 * the EraterResults is for a single prompt result (3 folds results aggregated together)
	 * 
	 */
	
	int numClasses;
	List<String> cValues;
	ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // array for max.cos
	ArrayList<Double> arrayF2 = new ArrayList<Double>(); // array for cosw4
	ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list
	int[][] confusionMatrix;
	String featsOut;
//	below are evaluation metrics
	double totalF;
	double corr1; //correlation between maxcos and score
	double corr2; //correlation between cosw4 and score
	
	EraterResults () {
		this.numClasses=Params.numClasses;
		this.cValues=Arrays.asList(Params.classValues);
		confusionMatrix=new int[numClasses][numClasses];
		featsOut="Filename,maxcos,cosw4,score\n";
	}
	
	void addFeatureResults (int maxcos, double cosw4, int score) {
		arrayF1.add(new Integer(maxcos));
		arrayF2.add(new Double(cosw4));
		arrayScore.add(new Integer(score));
	}
	
	void addToConfusionMatrix (int maxcos, int score) {
		int predIndex = getClassIndex(Integer.toString(maxcos));
		int actualIndex = getClassIndex(Integer.toString(score));
		confusionMatrix[actualIndex][predIndex] += 1;
	}
	
	void addToFeatsOut (String filename, int maxcos, double cosw4, int score) {
		featsOut+=filename+","+maxcos+","+cosw4+","+score+"\n";
	}
	
	void computeEvaluationMeasures () {
		totalF = weightedFMeasure(confusionMatrix);
		corr1 = pcorrelation(convertIntegerArrayToDouble(arrayF1), convertIntegerArrayToDouble(arrayScore));
		corr2 = pcorrelation(arrayF2, convertIntegerArrayToDouble(arrayScore));
	}
	
	/***
	 * find the index of class label in the string array classValues
	 * 
	 * @param label
	 * @return
	 */
	int getClassIndex(String label) {
		return cValues.indexOf(label);
	}
	
	double weightedFMeasure(int[][] confusionMatrix) {
		int[] classCounts = new int[numClasses];
		int classSum = 0;
		double totalF = 0;

		for (int i = 0; i < numClasses; i++) {
			// this computes number of instances in a particular class
			for (int j = 0; j < numClasses; j++) {
				// the count is the actual num of instances for each class
				classCounts[i] = confusionMatrix[i][j];
			}
			classSum += classCounts[i];
		}

		for (int i = 0; i < numClasses; i++) {
			double temp = computeFmeasure(confusionMatrix, i);
//			System.out.println("F measure for class index "+i+" is "+temp);
			totalF += classCounts[i] * temp;
		}
		return totalF / (double) classSum;
	}
	
	double computeFmeasure(int[][] confusionMatrix, int i) {
		double p = precision(confusionMatrix, i);
		double r = recall(confusionMatrix, i);

		if (p + r == 0)
			return 0;

		return 2 * p * r / (p + r);
	}
	
	double precision(int[][] confusionMatrix, int classIndex) {
		int correct = 0;
		int total = 0;

		for (int i = 0; i < numClasses; i++) {
			if (i == classIndex) {
				correct = confusionMatrix[i][classIndex];
			}
			total += confusionMatrix[i][classIndex];
		}

		if (total == 0)
			return 0;
		return (double) correct / (double) total;
	}

	double recall(int[][] confusionMatrix, int classIndex) {
		int correct = 0;
		int total = 0;

		for (int j = 0; j < numClasses; j++) {
			if (j == classIndex) {
				correct = confusionMatrix[classIndex][j];
			}
			total += confusionMatrix[classIndex][j];
		}

		if (total == 0)
			return 0;
		return (double) correct / (double) total;
	}
	
	/***
	 * given an Integer arraylist, convert it to a Double arraylist
	 * 
	 * @param li
	 * @return
	 */
	ArrayList<Double> convertIntegerArrayToDouble(ArrayList<Integer> li) {
		ArrayList<Double> doubleli = new ArrayList<Double>();
		for (Integer elem : li) {
			double doubledElem = (double) elem.intValue();
			doubleli.add(new Double(doubledElem));
		}
		return doubleli;
	}
	
	/***
	 * this computes Pearson correlation between 2 arrays
	 * 
	 * @return
	 */
	double pcorrelation(ArrayList<Double> a1, ArrayList<Double> a2) {
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
	
	/**
	 * Outputs the performance statistics as a classification confusion matrix.
	 * For each class value, shows the distribution of predicted class values.
	 * 
	 * @param title
	 *            the title for the confusion matrix
	 * @return the confusion matrix as a String
	 * @throws Exception
	 *             if the class is numeric
	 */
	public String toMatrixString(String title) {

		StringBuffer text = new StringBuffer();
		char[] IDChars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
				'w', 'x', 'y', 'z' };
		int IDWidth;
		boolean fractional = false;

		// Find the maximum value in the matrix
		// and check for fractional display requirement
		double maxval = 0;
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				double current = confusionMatrix[i][j];
				if (current < 0) {
					current *= -10;
				}
				if (current > maxval) {
					maxval = current;
				}
				double fract = current - Math.rint(current);
				if (!fractional && ((Math.log(fract) / Math.log(10)) >= -2)) {
					fractional = true;
				}
			}
		}

		IDWidth = 1 + Math.max(
				(int) (Math.log(maxval) / Math.log(10) + (fractional ? 3 : 0)),
				(int) (Math.log(numClasses) / Math.log(IDChars.length)));
		text.append(title).append("\n");
		for (int i = 0; i < numClasses; i++) {
			if (fractional) {
				text.append(" ").append(num2ShortID(i, IDChars, IDWidth - 3))
						.append("   ");
			} else {
				text.append(" ").append(num2ShortID(i, IDChars, IDWidth));
			}
		}
		text.append("   <-- classified as\n");
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				text.append(" ").append(
						Utils.doubleToString(confusionMatrix[i][j], IDWidth,
								(fractional ? 2 : 0)));
			}
			text.append(" | ").append(num2ShortID(i, IDChars, IDWidth))
					.append(" = ").append(cValues.get(i)).append("\n");
		}
		return text.toString();
	}
	
	/**
	 * Method for generating indices for the confusion matrix.
	 * 
	 * @param num
	 *            integer to format
	 * @param IDChars
	 *            the characters to use
	 * @param IDWidth
	 *            the width of the entry
	 * @return the formatted integer as a string
	 */
	protected String num2ShortID(int num, char[] IDChars, int IDWidth) {

		char ID[] = new char[IDWidth];
		int i;

		for (i = IDWidth - 1; i >= 0; i--) {
			ID[i] = IDChars[num % IDChars.length];
			num = num / IDChars.length - 1;
			if (num < 0) {
				break;
			}
		}
		for (i--; i >= 0; i--) {
			ID[i] = ' ';
		}

		return new String(ID);
	}
	
	static String formatDecimal (double d) {
		DecimalFormat f=new DecimalFormat("#.0000");
		return f.format(d);
	}
	
	
	
	public static void main (String[] args) {
		
		//this class is mainly used for reasoning case now
//		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml";
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/results/wiki-reason-dft";
		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
		String outPrefix="/Users/miaochen/Documents/diss-experiment/results/wn1st-reason-path";
		String evalOut=",maxcos,cosw4,TotalFmeasure\n";
		
		double sumMaxcos=0;
		double sumCosw4=0;
		double sumF=0;
		
		Toolbox toolbox=new Toolbox(true,true,true,true,true,true,false);
		toolbox.enableTools();
		
		for (String prompt : Params.prompts) {
			EraterResults eresults=new EraterResults();
			for (String fold : Params.folds) {
				System.out.println("Processing prompt "+prompt+", fold "+fold);
				String trainFolder=inPrefix+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
				String testFolder=inPrefix+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
				String superFolder=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
//				***wordnet
//				EraterEvaluator eEval=new EraterEvaluator (trainFolder, testFolder, superFolder, eresults);
//				eEval.getEvaluationForWn(toolbox);
//				***bow
//				EraterEvaluator eEval=new EraterEvaluator (trainFolder, testFolder, superFolder, eresults);
//				eEval.getEvaluatioForBow(toolbox);
//				wiki
				EraterEvaluator eEval=new EraterEvaluator(null, null, null, 
						trainFolder, testFolder, superFolder, 
						null, null, null, eresults);
				eEval.getEvaluationForWiki(toolbox);
			}
			eresults.computeEvaluationMeasures();
			evalOut+=prompt+","+formatDecimal(eresults.corr1)+","+formatDecimal(eresults.corr2)+","+formatDecimal(eresults.totalF)+"\n";
			sumMaxcos+=eresults.corr1;
			sumCosw4+=eresults.corr2;
			sumF+=eresults.totalF;
			FileIO.writeFile(outPrefix+File.separator+"erater-"+prompt+"-confusionmatrix.txt", eresults.toMatrixString("prompt "+prompt));
			FileIO.writeFile(outPrefix+File.separator+"erater-"+prompt+"-feats.csv", eresults.featsOut);
		}
		evalOut+="average,"+formatDecimal(sumMaxcos/(double)4)+","+formatDecimal(sumCosw4/(double)4)+","+formatDecimal(sumF/(double)4)+"\n";
		FileIO.writeFile(outPrefix+File.separator+"erater-results.csv", evalOut);
		
	}

}
