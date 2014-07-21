package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

import com.common.contants.ESAConstants;
import com.esa.search.ESASearcher;

import weka.core.Utils;

/***
 * a big chunk of this code is adopted from the Weka Evaluation.java class
 * 
 * 
 * @author miaochen
 * 
 */

public class EraterFeatures {

	String superFolderPrefix;
	String testFolderPrefix;
	String scoreList;
//	the type of input folder: 0=regular hash file; 1=esa, text file;
//	int inType; 

	int numFolds = 3;// means each prompt is split into 3 folds, for cross validation
	int maxScore = 4;
	int minScore = 2;
	int numClasses = maxScore - minScore + 1;
	String[] classValues = { "2", "3", "4" };
	List<String> cValues = Arrays.asList(classValues);
	int wtype = 3; // tf idf type, it's only applicable to inType=0; that means, wtype=null when inType=1 (esa case).
	String[] prompts = { "098", "099", "100", "101" };

	EraterFeatures(String superFolderPrefix, String testFolderPrefix, String scoreList) {
		this.superFolderPrefix = superFolderPrefix;
		this.testFolderPrefix = testFolderPrefix;
		this.scoreList = scoreList;
//		this.inType=inType;
	}
	
	void getEvaluation (String outFolder, ESASearcher searcher) {
//		if (inType==0) {
			getEvaluationFromHashFiles (outFolder);
//		}
//		else if(inType==1){
//			//actually ESA input are text files, and it needs ESA Searcher class for similarity computation
//			getEvaluationFromEsaFiles (outFolder, searcher);
//		}
	}

//	this method is for regular hash style input file, which is like "key\tvalue"
	void getEvaluationFromHashFiles(String outFolder) {

		HashMap<String, Integer> file_score = filescorelistToHash();

		// this is the result output string, will be a .csv table
		String result = ",maxcos,cosw4,fmeasure\n";
//		output for confusion matrices
		String matrixOut="";
		String resultDir=outFolder+File.separator+"erater-results.csv";
		String matrixDir=outFolder+File.separator+"erater-confusionmatrix.txt";
		
		double sumMaxcos=0;
		double sumCosw4=0;
		double sumf=0;

		for (String prompt : prompts) {
			String featsOut = "Filename,maxcos,cosw4,score\n";
			// this is the array/list for F1, F2, actual score (for the whole
			// prompt)
			ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // max.cos
			ArrayList<Double> arrayF2 = new ArrayList<Double>(); // cosw4
			ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list

			int[][] confusionMatrix = new int[numClasses][numClasses];

			for (int i = 0; i < numFolds; i++) {
				System.out.println("Start processing " + prompt + " data fold "+ i);
				String superFolder = superFolderPrefix + File.separator+ prompt + File.separator + "super" + File.separator
						+ "super" + i;
				String testFolder = testFolderPrefix + File.separator + prompt + File.separator + "test" + File.separator
						+ "test" +i;

				ArrayList<HashMap<String, Double>> superWeightedVecList = getSuperVecListFromVecFolder(superFolder);

				String[] filenames = new File(testFolder).list();
				for (String filename : filenames) {
					System.out.println("Processing " + filename);
					int score = getScoreGivenFilename(file_score, filename);

					String testFile = testFolder + File.separator + filename;
					HashMap<String, Double> testWeightedVec = FileIO.readFileToHash(testFile, 0);

					int f1 = getMaxCosFeature(testWeightedVec, superWeightedVecList);
					double f2 = getCosW4Feature(testWeightedVec, superWeightedVecList);
					arrayF1.add(new Integer(f1));
					arrayF2.add(new Double(f2));
					arrayScore.add(score);
					// add the predicted result to the confusion matrix
					int predIndex = getClassIndex(Integer.toString(f1));
					int actualIndex = getClassIndex(Integer.toString(score));
					confusionMatrix[actualIndex][predIndex] += 1;
					
					featsOut+=filename+","+f1+","+f2+","+score+"\n";
				}
				System.out.println("Finished prompt " + prompt + " data fold "+ i);
			}
			double totalF = weightedFMeasure(confusionMatrix);
			double corr1 = pcorrelation(convertIntegerArrayToDouble(arrayF1), convertIntegerArrayToDouble(arrayScore));
			double corr2 = pcorrelation(arrayF2, convertIntegerArrayToDouble(arrayScore));
			sumMaxcos+=corr1;
			sumCosw4+=corr2;
			sumf+=totalF;
			
			result+=prompt+","+formatDecimal(corr1)+","+formatDecimal(corr2)+","+formatDecimal(totalF)+"\n";
//			this is writing to the output file of values of feature1, feature2, and actual score
			String outDir=outFolder+File.separator+"erater-"+prompt+"-feats.csv";
			FileIO.writeFile(outDir, featsOut);
			
			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
		}
		result+="average,"+formatDecimal(sumMaxcos/(double)4)+","+formatDecimal(sumCosw4/(double)4)+","+formatDecimal(sumf/(double)4)+"\n";
//		write all the correlation results to one file
		FileIO.writeFile(resultDir, result);
		FileIO.writeFile(matrixDir, matrixOut);
	}
	
	
//	the input files are plain text files for ESA processing
//	void getEvaluationFromEsaFiles (String outFolder, ESASearcher searcher) {
////		this is the indicator of whether record similarity to a file or not
//		boolean recordSim=Params.recordSim;		
//		
//		HashMap<String, Integer> file_score = filescorelistToHash();
//		
////		this is the result output string, will be a .csv table
//		String result = ",TotalFmeasure,maxcos,cosw4\n";
////		output for confusion matrices
//		String matrixOut="";
//		String resultDir=outFolder+File.separator+"erater-results.csv";
//		String matrixDir=outFolder+File.separator+"erater-confusionmatrix.txt";
//		
//		for (String prompt : prompts) {
//			String featsOut = "Filename,maxcos,cosw4,score\n";
//			String simRec=new String("");	//this string for similarity record
//			// this is the array/list for F1, F2, actual score (for the whole prompt)
//			ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // max.cos
//			ArrayList<Double> arrayF2 = new ArrayList<Double>(); // cosw4
//			ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list
//			
//			int[][] confusionMatrix = new int[numClasses][numClasses];
//			
//			for (int i = 0; i < numFolds; i++) {
//				System.out.println("Start processing " + prompt + " data fold "+ i);
//				String superFolder = superFolderPrefix + File.separator+ prompt + File.separator + "super" + File.separator
//						+ "train" + i;
//				String testFolder = testFolderPrefix + File.separator + prompt + File.separator + "test" + i;
//				
//				String[] filenames = new File(testFolder).list();
//				for (String filename : filenames) {
//					System.out.println("Processing " + filename);
//					int score = getScoreGivenFilename(file_score, filename);
//
//					String testFile = testFolder + File.separator + filename;
////					int f1 = getMaxCosFeature (testFile, superFolder, searcher);
////					double f2 = getCosW4Feature (testFile, superFolder, searcher);
////					gets all the cos sim scores in an arraylist, then sort for f1, get sim[lastIndex] for f2; it's done a bit differently 
////					than before because of the efficiency issue of ESA
//					ArrayList<Double> sims=computeSimBetweenTestFileAndEachSuperFile (testFile, superFolder, searcher);
//					int f1 = findScoreLevelWithMaxCos (sims);
//					double f2 = findCosW4 (sims);
//					if (recordSim==true){
////						record the similarities between test file with each super file
//						simRec+=printSimilaritiesForTestFile (filename,sims);
//					}	
//					
//					arrayF1.add(new Integer(f1));
//					arrayF2.add(new Double(f2));
//					arrayScore.add(score);
//					// add the predicted result to the confusion matrix
//					int predIndex = getClassIndex(Integer.toString(f1));
//					int actualIndex = getClassIndex(Integer.toString(score));
//					confusionMatrix[actualIndex][predIndex] += 1;
//					
//					featsOut+=filename+","+f1+","+f2+","+score+"\n";
//				}
//				System.out.println("Finished prompt " + prompt + " data fold "+ i);
//			}
//			double totalF = weightedFMeasure(confusionMatrix);
//			double corr1 = pcorrelation(convertIntegerArrayToDouble(arrayF1), convertIntegerArrayToDouble(arrayScore));
//			double corr2 = pcorrelation(arrayF2, convertIntegerArrayToDouble(arrayScore));
//			
//			result+=prompt+","+totalF+","+corr1+","+corr2+"\n";
////			this is writing to the output file of values of feature1, feature2, and actual score
//			String outDir=outFolder+File.separator+"prom-"+prompt+"-all.csv";
//			FileIO.writeFile(outDir, featsOut);
//			
//			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
//			
////			write the similarity records if it's set to true
//			if (recordSim) {
//				String simDir=outFolder+File.separator+"sim-"+prompt+".txt";
//				FileIO.writeFile(simDir, simRec);
//			}			
//		}
////		write all the correlation results to one file
//		FileIO.writeFile(resultDir, result);
//		FileIO.writeFile(matrixDir, matrixOut);
//	}
	

	ArrayList<HashMap<String, Double>> getSuperVecListFromVecFolder(String superFolder) {
		ArrayList<HashMap<String, Double>> superVecList = new ArrayList<HashMap<String, Double>>();

		for (int score = minScore; score < maxScore + 1; score++) {
			// #this superFile name is customized and is subject to change
			String superFile = superFolder + File.separator+score;
			HashMap<String, Double> superVec = FileIO.readFileToHash(superFile,0);
			superVecList.add(superVec);
		}
		return superVecList;
	}

	int getScoreGivenFilename(HashMap<String, Integer> file_score, String filename) {
		int score = -1;
		String f = filename.substring(0, filename.length() - 4);

		if (file_score.containsKey(f)) {
			score = file_score.get(f);
		}
		return score;
	}

	/***
	 * converts the scoreList file to a hashmap
	 * 
	 * @return
	 */
	HashMap<String, Integer> filescorelistToHash() {
		HashMap<String, Integer> f_s = new HashMap<String, Integer>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(scoreList))));
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
	
	/***
	 * the output format is : "filename,score(ofSuperFile)\tsimilarity\n"
	 * 
	 * @param fname
	 * @param sims
	 * @return
	 */
	String printSimilaritiesForTestFile (String fname, ArrayList<Double> sims) {
		String s="";
		for (int score = minScore; score < maxScore + 1; score++) {
			s+=fname+","+Integer.toString(score)+"\t"+sims.get(score-minScore).toString()+"\n";
		}
		return s;
	}
	
//	String removeFileSuffix (String fname) {
//		String removed;
//		if (fname.split("\\.").length==2) {
//			removed=fname.split("\\.")[0];
//		}else
//			removed=fname;
//		return removed;
//	}
	
	/***
	 * it computes esa similarity between the test file and each super file (2,3,4)
	 * return the (3) similarities in an ArrayList
	 * 
	 * @param testFile
	 * @param superFolder
	 * @param searcher
	 * @return
	 */
	ArrayList<Double> computeSimBetweenTestFileAndEachSuperFile (String testFile, String superFolder, ESASearcher searcher) {
		ArrayList<Double> sims=new ArrayList<Double> ();
		for (int score = minScore; score < maxScore + 1; score++) {
			// #this superFile name is customized and is subject to change
			String superFile = superFolder + File.separator+score;
			double esasim=0.0;
			try {
				esasim=searcher.getRelatednessBetweenText(FileIO.getFileAsString(testFile), FileIO.getFileAsString(superFile));
			}catch (Exception e) {
				e.printStackTrace();
			}
			sims.add(new Double(esasim));
		}
		return sims;
	}
	
	/****
	 * this finds the score level that has the highest cos sim with the test file (f1)
	 * 
	 * @return
	 */
	int findScoreLevelWithMaxCos (ArrayList<Double> sims) {
		double maxSim = -1.0;
		int maxPosit=0;  //position of the maximum score value (f1), like score "2" or so; 
		
		for (int score = minScore; score < maxScore + 1; score++) {
			double currentSim=sims.get(score-minScore).doubleValue();
			if (currentSim > maxSim) {
				maxSim=currentSim;
				maxPosit=score;
			}
		}
		
		return maxPosit;
	}
	
	/***
	 * this looks for f2 given the similarity ArrayList (with each super file)
	 * 
	 */
	double findCosW4 (ArrayList<Double> sims) {
		return sims.get(maxScore-minScore).doubleValue();
	}

	/***
	 * this computes f1 (the max.cos feature)
	 * 
	 * @param testWeightedVec
	 * @param superWeightedVecList
	 * @return
	 */
	int getMaxCosFeature(HashMap<String, Double> testWeightedVec,
			ArrayList<HashMap<String, Double>> superWeightedVecList) {
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value, like score "2" or so; it's an array because maybe two score
					     // categories share the same simialrity with the essay (although this is very low probability)

		for (int score = minScore; score < maxScore + 1; score++) {
			HashMap<String, Double> superWeightedVec = superWeightedVecList
					.get(score - minScore);
			double cossim;
			if (wtype == 3) {// the tf/normalizedByEuclideanLength*idf weighting
				cossim = similarity.computeDotProductOnNormFreqBetweenHashMaps(
						testWeightedVec, superWeightedVec);
			} else {
				cossim = similarity.computeCosineSimilarityBetweenHashMaps(testWeightedVec,
						superWeightedVec);
			}
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}

		}
		return maxPosit;
	}
	
	/***
	 * this is used for ESA document (text file, for ESA similarity computation)
	 * 
	 * @param testFile
	 * @param superFolder
	 * @param searcher
	 * @return
	 * @throws SQLException 
	 * @throws IOException 
	 */
	int getMaxCosFeature (String testFile, String superFolder, ESASearcher searcher) {
		double maxSim=-1.0;
		int maxPosit=0;
		for (int score = minScore; score < maxScore + 1; score++) {
			// #this superFile name is customized and is subject to change
			String superFile = superFolder + File.separator+score;
			double esasim=0.0;
			try {
				esasim=searcher.getRelatednessBetweenText(FileIO.getFileAsString(testFile), FileIO.getFileAsString(superFile));
			}catch (Exception e) {
				e.printStackTrace();
			}
			if (esasim > maxSim) {
				maxSim=esasim;
				maxPosit=score;
			}
		}
		return maxPosit;
	}

	double getCosW4Feature(HashMap<String, Double> testWeightedVec,
			ArrayList<HashMap<String, Double>> superWeightedVecList) {
		Similarity similarity = new Similarity();
		if (wtype == 3) {
			return similarity.computeDotProductOnNormFreqBetweenHashMaps(testWeightedVec,
					superWeightedVecList.get(maxScore - minScore));
		} else {
			return similarity.computeCosineSimilarityBetweenHashMaps(testWeightedVec,
					superWeightedVecList.get(maxScore - minScore));
		}
	}
	
	/***
	 * this is used for ESA document (text file, for ESA similarity computation)
	 * 
	 * @param testFile
	 * @param superFolder
	 * @param searcher
	 * @return
	 */
	double getCosW4Feature (String testFile, String superFolder, ESASearcher searcher) {
//		this is the super file with the highest score level
		String superFile4=superFolder+File.separator+Integer.toString(maxScore);
		double esasim=0.0;
		try {
			esasim=searcher.getRelatednessBetweenText(FileIO.getFileAsString(testFile), FileIO.getFileAsString(superFile4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return esasim;
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
	public String toMatrixString(String title, int[][] confusionMatrix) {

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

	public String printConfusionMatrix(int[][] matrix) {
		String out = "";
		for (int i = 0; i < numClasses; i++) {
			for (int j = 0; j < numClasses; j++) {
				out += matrix[i][j] + "      ";
			}
			out += "\n";
		}
		return out;
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
	
	String formatDecimal (double d) {
		DecimalFormat f=new DecimalFormat("#.0000");
		return f.format(d);
	}
	

	public static void main(String[] args) throws SQLException, IOException {
		
		String inPrefix="/home/miao/Documents/Projects/ETS2014/data/train-bow";
		String outFolder="/home/miao/Documents/Projects/ETS2014/data/test";
		String scoreList=Params.scoreList;
		
		EraterFeatures efeats=new EraterFeatures(inPrefix, inPrefix, scoreList);
		efeats.getEvaluationFromHashFiles(outFolder);
		
		

	}

}
