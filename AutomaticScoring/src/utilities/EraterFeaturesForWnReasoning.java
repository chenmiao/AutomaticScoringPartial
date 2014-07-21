package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import weka.core.Utils;

public class EraterFeaturesForWnReasoning {
	
	static String[] classValues = { "2", "3", "4" };
	static List<String> cValues = Arrays.asList(classValues);
	static int numClasses=3;
	
	static HashMap<String, Integer> filescorelistToHash() {
		HashMap<String, Integer> f_s = new HashMap<String, Integer>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(Params.scoreList))));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				String[] units = strLine.trim().split(",");
				if (units.length == 2) {
					f_s.put(units[0], Integer.valueOf(units[1]));
				}
			}
			br.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return f_s;
	}
	
	static ArrayList<Document> computeWnSuperVecs (String superFolderText, SentenceSplitter splitter, SentenceTokenizer tokenizer, DBConnection dbcon, WordNetMatching wnmatch
			, SentenceTagger tagger, HashMap syn_idf) {
		ArrayList<Document> superdocs=new ArrayList<Document> ();
		for (int score=Params.minScore; score < Params.maxScore + 1; score++) {
			Document superdoc=new Document(new File(superFolderText+File.separator+Integer.toString(score)));
			superdoc.toSynVectorTfIdf(Params.wType, splitter, tokenizer, dbcon, wnmatch, Params.doPOS, syn_idf, tagger);
			superdocs.add(superdoc);
		}
		return superdocs;
	}
	
	static int getScoreGivenFilename(HashMap<String,Integer> file_score, String filename) {
		int score = -1;
		String f = filename.substring(0, filename.length() - 4);

		if (file_score.containsKey(f)) {
			score = file_score.get(f);
		}
		return score;
	}
	
	 /***
	 * find the index of class label in the string array classValues
	 * 
	 * @param label
	 * @return
	 */
	static int getClassIndex(String label) {
		return cValues.indexOf(label);
	}
	
	static String formatDecimal (double d) {
		DecimalFormat f=new DecimalFormat("#.0000");
		return f.format(d);
	}
	
	static double weightedFMeasure(int[][] confusionMatrix) {
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
	
	static double averageFMeasure (int[][] confusionMatrix) {
		double totalF = 0;
		
		for (int i = 0; i < numClasses; i++) {
			double temp = computeFmeasure(confusionMatrix, i);
//			System.out.println("F measure for class index "+i+" is "+temp);
			totalF += temp;
		}
		
		return totalF/(double)numClasses;
	}
	
	static double accuracyRate(int[][] confusionMatrix) {
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

	static double computeFmeasure(int[][] confusionMatrix, int i) {
		double p = precision(confusionMatrix, i);
		double r = recall(confusionMatrix, i);

		if (p + r == 0)
			return 0;

		return 2 * p * r / (p + r);
	}

	static double precision(int[][] confusionMatrix, int classIndex) {
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

	static double recall(int[][] confusionMatrix, int classIndex) {
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
	static public String toMatrixString(String title, int[][] confusionMatrix) {

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
					.append(" = ").append(classValues[i]).append("\n");
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
	static protected String num2ShortID(int num, char[] IDChars, int IDWidth) {

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
	
	
	public static void main (String[] args) {
//		this is for the WordNet reasoning case
		
		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds"; //param 1
//		String outPrefix="/Users/miaochen/Documents/diss-experiment/results/wnpos-reason-dft"; //param 2
		String result=",maxcos,cosw4,aFmeasure,waFmeasure,accuracy,f2,f3,f4,p2,p3,p4,r2,r3,r4\n";
		String matrixOut="";
//		String resultDir=outPrefix+File.separator+"erater-results.csv";
//		String matrixDir=outPrefix+File.separator+"erater-confusionmatrix.txt";
		HashMap<String, Integer> file_score = filescorelistToHash();  //param 3
		
		Toolbox toolbox=new Toolbox(true,true,false,true,true,false,true); //param 4
		toolbox.enableTools();
		
		String fileToCheck="7612835-VB531101.txt";  //param...1
		String thisprompt="101";  //param... 2
		
		double sumMaxcos=0;
		double sumCosw4=0;
		double sumavgf=0;
		double sumf=0;
		double sumaccu=0;
		double sumf2=0;
		double sumf3=0;
		double sumf4=0;
		double sump2=0;
		double sump3=0;
		double sump4=0;
		double sumr2=0;
		double sumr3=0;
		double sumr4=0;
		
		for (String prompt : Params.prompts) {
			
			if (!prompt.equalsIgnoreCase(thisprompt)) {
				continue;
			}
			
			String featsOut = "Filename,maxcos,cosw4,score,simw2,simw3,simw4\n";
			// this is the array/list for F1, F2, actual score (for the whole
			// prompt)
			ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // max.cos
			ArrayList<Double> arrayF2 = new ArrayList<Double>(); // cosw4
			ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list			
			int[][] confusionMatrix = new int[numClasses][numClasses];
			
			OUTMOST: for (String fold : Params.folds) {  //a temporary break all label here
				System.out.println("Start processing " + prompt + " data fold "+ fold);
				String trainFolderText=inPrefix+File.separator+prompt+File.separator+"train"+File.separator+"train"+fold;
				String testFolderText=inPrefix+File.separator+prompt+File.separator+"test"+File.separator+"test"+fold;
				String superFolderText=inPrefix+File.separator+prompt+File.separator+"super"+File.separator+"super"+fold;
				
//				get the idf from train collection
				Collection trainCol=new Collection(trainFolderText);
				HashMap syn_idf=trainCol.getSynsetIDF(toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, toolbox.tagger); 
				double dftIDF=trainCol.getDefaultIDFForUnseenSynset();
//				get wn vector for the super files
				ArrayList<Document> superdocs=computeWnSuperVecs(superFolderText, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, toolbox.tagger, syn_idf);
				String[] testfiles=new File(testFolderText).list();
				for (String testfile : testfiles) {
					
					//this part is for looking at mid product
					if (!testfile.equalsIgnoreCase(fileToCheck)) {
						continue;  //just ignore it					    
					}
					
					System.out.println("Test file: "+testfile);
//					get wn vector for the test file
					Document testdoc=new Document (new File (testFolderText+File.separator+testfile));
					testdoc.toSynVectorTfIdf(Params.wType, toolbox.splitter, toolbox.tokenizer, toolbox.dbcon, toolbox.wnmatch, Params.doPOS, syn_idf, toolbox.tagger);
					EraterFeatureProcessor eraterProc=new EraterFeatureProcessor (testdoc, superdocs, toolbox);
					if (Params.expand) {  //reasoning case
						eraterProc.getFeaturesWnReasoning(syn_idf,dftIDF);
					}else { //non-reasoning case
//						eraterProc.getFeaturesWn();
					}			
//					this is the actual score, and maxcos is the predicted score of the speech transcript
					int score=getScoreGivenFilename(file_score, testfile);
					
					arrayF1.add(new Integer(eraterProc.maxcos));
					arrayF2.add(new Double(eraterProc.cosw4));
					arrayScore.add(score);
					
					// add the predicted result to the confusion matrix
					int predIndex = getClassIndex(Integer.toString(eraterProc.maxcos));
					int actualIndex = getClassIndex(Integer.toString(score));
					confusionMatrix[actualIndex][predIndex] += 1;
					
					featsOut+=testfile+","+eraterProc.maxcos+","+formatDecimal(eraterProc.cosw4)+","+score;
					featsOut+=","+formatDecimal(eraterProc.sim2)+","+formatDecimal(eraterProc.sim3)+","+formatDecimal(eraterProc.sim4);
					featsOut+="\n";
					
					if (testfile.equalsIgnoreCase(fileToCheck)) {  //exit when finish looking at this file
						break OUTMOST;
					}

				}
				System.out.println("Finished prompt " + prompt + " data fold "+ fold);
			}
			double weightedAvgF = weightedFMeasure(confusionMatrix);
			double avgF=averageFMeasure(confusionMatrix);
			double f2=computeFmeasure(confusionMatrix, 0);
			double f3=computeFmeasure(confusionMatrix, 1);
			double f4=computeFmeasure(confusionMatrix, 2);
			double p2=precision(confusionMatrix, 0);
			double p3=precision(confusionMatrix, 1);
			double p4=precision(confusionMatrix, 2);
			double r2=recall(confusionMatrix, 0);
			double r3=recall(confusionMatrix, 1);
			double r4=recall(confusionMatrix, 2);
			double corr1 = pcorrelation(convertIntegerArrayToDouble(arrayF1), convertIntegerArrayToDouble(arrayScore));
			double corr2 = pcorrelation(arrayF2, convertIntegerArrayToDouble(arrayScore));
			double accu = accuracyRate(confusionMatrix);
//			this is for computing avarage f1 etc. (averaged on prompts)
			sumMaxcos+=corr1;
			sumCosw4+=corr2;
			sumavgf+=avgF;
			sumf+=weightedAvgF;
			sumaccu+=accu;
			sumf2+=f2;
			sumf3+=f3;
			sumf4+=f4;
			sump2+=p2;
			sump3+=p3;
			sump4+=p4;
			sumr2+=r2;
			sumr3+=r3;
			sumr4+=r4;

			result+=prompt+","+formatDecimal(corr1)+","+formatDecimal(corr2)+","+formatDecimal(avgF)+","+formatDecimal(weightedAvgF)+","+
			formatDecimal(accu)+","+formatDecimal(f2)+","+formatDecimal(f3)+","+formatDecimal(f4)
			+","+formatDecimal(p2)+","+formatDecimal(p3)+","+formatDecimal(p4)
			+","+formatDecimal(r2)+","+formatDecimal(r3)+","+formatDecimal(r4)+"\n";
//			this is writing to the output file of values of feature1, feature2, and actual score
//			String outDir=outPrefix+File.separator+"erater-"+prompt+"-feats-withsim.csv";
//			FileIO.writeFile(outDir, featsOut);
			
			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
		}
		result+="average,"+formatDecimal(sumMaxcos/(double)4)+","+formatDecimal(sumCosw4/(double)4)+","+formatDecimal(sumavgf/(double)4)+
				","+formatDecimal(sumf/(double)4)+","+formatDecimal(sumaccu/(double)4)+","+formatDecimal(sumf2/(double)4)+","+formatDecimal(sumf3/(double)4)+","+formatDecimal(sumf4/(double)4)
				+","+formatDecimal(sump2/(double)4)+","+formatDecimal(sump3/(double)4)+","+formatDecimal(sump4/(double)4)
				+","+formatDecimal(sumr2/(double)4)+","+formatDecimal(sumr3/(double)4)+","+formatDecimal(sumr4/(double)4)+"\n";
//		write all the correlation results to one file
//		FileIO.writeFile(resultDir, result);
//		FileIO.writeFile(matrixDir, matrixOut);
	}

}
