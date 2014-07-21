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

import com.esa.search.ESASearcher;

public class EraterFeaturesForCombinedDoc {
	
	String textFolder; //folder storing the original text
	String bowFolder;
	String wnFolder;
	String wikiFolder;
	String esaFolder;
	String scoreList;
//	the type of input folder: 0=regular hash file; 1=esa, text file;
//	int inType; //it's not useful in current situation, because esa vec has been computed 
	int cType; //the type of combination, add=0, replace=1
	
	int numFolds = 3;// means each prompt is split into 3 folds, for cross validation
//	int numFolds = 10;
	int maxScore = 4;
	int minScore = 2;
	int numClasses = maxScore - minScore + 1;
	String[] classValues = { "2", "3", "4" };
	List<String> cValues = Arrays.asList(classValues);
	int wtype = 3; // tf idf type, it's only applicable to inType=0; that means, wtype=null when inType=1 (esa case).
	String[] prompts = { "098", "099", "100", "101" };
	
	EraterFeaturesForCombinedDoc () {
		
	}
	
	EraterFeaturesForCombinedDoc (String textFolder, String bowFolder, String wnFolder, String wikiFolder
			, String scoreList, int cType) {
		this (textFolder, bowFolder, wnFolder, wikiFolder, null, scoreList, cType);
	}
	
	EraterFeaturesForCombinedDoc (String textFolder, String bowFolder, String wnFolder, String wikiFolder, String esaFolder
			, String scoreList, int cType) {
		this.textFolder=textFolder;
		this.bowFolder=bowFolder;
		this.wnFolder=wnFolder;
		this.wikiFolder=wikiFolder;
		this.esaFolder=esaFolder;
		this.scoreList=scoreList;
//		this.inType=inType;
		this.cType=cType;
	}
	
	public void getEvaluation (String outFolder, ESASearcher searcher) {
		getEvaluationFromFiles (outFolder);
//		if (inType==0) {
//			getEvaluationFromFiles (outFolder);
//		}
//			else if(inType==1){
//			//actually ESA input are text files, and it needs ESA Searcher class for similarity computation
////			getEvaluationFromEsaFiles (outFolder, searcher);
////			use the pre-saved similarity file (sim between super files and test files), together
////			with vec files (bow-vec, wn-vec, etc.) to determine the total similarity
////			between super and test files
//			getEvaluationFromEsaSimFile (outFolder, Params.esaSimFolder);
//		}
	}
	
	public void getEvaluationFromFiles (String outFolder) {
		HashMap<String, Integer> file_score = filescorelistToHash();
//		 this is the result output string, will be a .csv table
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
//				the super folders of different representations
				String superFolderText= textFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i;
				String superFolderBow=(bowFolder!=null) ? bowFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i : null;
				String superFolderWn=(wnFolder!=null) ? wnFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i : null;
				String superFolderWiki=(wikiFolder!=null) ? wikiFolder+File.separator+prompt+File.separator+"super"+File.separator
				+"super"+i: null;
				String superFolderEsa= (esaFolder!=null) ? esaFolder+File.separator+prompt+File.separator+"super"+File.separator+"super"+i : null;
//				the test folders of different representations
				String testFolderText=textFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i;
				String testFolderBow=(bowFolder!=null) ? (bowFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderWn=(wnFolder!=null) ? (wnFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderWiki=(wikiFolder!=null) ? (wikiFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderEsa=(esaFolder!=null) ? (esaFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				
//				get the super CombinedDocument
				ArrayList<CombinedDocument> cSuperDocList=getSuperCombinedDocsInList(superFolderText, superFolderBow
						, superFolderWn, superFolderWiki, superFolderEsa);
//				get test file names from the textFolder
				String[] filenames=new File (testFolderText).list();
				for (String filename : filenames) {
					System.out.println("Processing " + filename);
					int score = getScoreGivenFilename(file_score, filename);
//					this is the combined test document
					CombinedDocument cTestDoc=new CombinedDocument(testFolderText+File.separator+filename
							, getFilePathGivenFolderAndFilename(testFolderBow,filename), getFilePathGivenFolderAndFilename(testFolderWn,filename)
							,getFilePathGivenFolderAndFilename(testFolderWiki,filename), getFilePathGivenFolderAndFilename(testFolderEsa, filename));
					int f1 = getMaxCosFeature(cTestDoc, cSuperDocList);
					double f2 = getCosW4Feature(cTestDoc, cSuperDocList);
					
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
//			this is for computing avarage f1 etc. (averaged on prompts)
			sumMaxcos+=corr1;
			sumCosw4+=corr2;
			sumf+=totalF;

			result+=prompt+","+formatDecimal(corr1)+","+formatDecimal(corr2)+","+formatDecimal(totalF)+"\n";
//			this is writing to the output file of values of feature1, feature2, and actual score
			String outDir=outFolder+File.separator+"prom-"+prompt+"-all.csv";
			FileIO.writeFile(outDir, featsOut);
			
			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
		}
		result+="average,"+formatDecimal(sumMaxcos/(double)4)+","+formatDecimal(sumCosw4/(double)4)+","+formatDecimal(sumf/(double)4)+"\n";
//		write all the correlation results to one file
		FileIO.writeFile(resultDir, result);
		FileIO.writeFile(matrixDir, matrixOut);
	}
	
	public void getEvaluationFromFilesWithSimDetails (String outFolder) {
		
		HashMap<String, Integer> file_score = filescorelistToHash();
//		 this is the result output string, will be a .csv table
		String result = ",maxcos,cosw4,aFmeasure,waFmeasure,accuracy,f2,f3,f4,p2,p3,p4,r2,r3,r4\n";
//		output for confusion matrices
		String matrixOut="";
		String resultDir=outFolder+File.separator+"erater-results.csv";
		String matrixDir=outFolder+File.separator+"erater-confusionmatrix.txt";
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
		
		for (String prompt : prompts) {
			String featsOut = "Filename,maxcos,cosw4,score,simw2,simw3,simw4\n";
			// this is the array/list for F1, F2, actual score (for the whole
			// prompt)
			ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // max.cos
			ArrayList<Double> arrayF2 = new ArrayList<Double>(); // cosw4
			ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list			
			int[][] confusionMatrix = new int[numClasses][numClasses];
			
			for (int i = 0; i < numFolds; i++) {
				System.out.println("Start processing " + prompt + " data fold "+ i);
//				the super folders of different representations
				String superFolderText= textFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i;
				String superFolderBow=(bowFolder!=null) ? bowFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i : null;
				String superFolderWn=(wnFolder!=null) ? wnFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i : null;
				String superFolderWiki=(wikiFolder!=null) ? wikiFolder+File.separator+prompt+File.separator+"super"+File.separator
				+"super"+i: null;
				String superFolderEsa= (esaFolder!=null) ? esaFolder+File.separator+prompt+File.separator+"super"+File.separator+"super"+i : null;
//				the test folders of different representations
				String testFolderText=textFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i;
				String testFolderBow=(bowFolder!=null) ? (bowFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderWn=(wnFolder!=null) ? (wnFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderWiki=(wikiFolder!=null) ? (wikiFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderEsa=(esaFolder!=null) ? (esaFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				
//				get the super CombinedDocument
				ArrayList<CombinedDocument> cSuperDocList=getSuperCombinedDocsInList(superFolderText, superFolderBow
						, superFolderWn, superFolderWiki, superFolderEsa);
//				get test file names from the textFolder
				String[] filenames=new File (testFolderText).list();
				for (String filename : filenames) {
					System.out.println("Processing " + filename);
					int score = getScoreGivenFilename(file_score, filename);
//					this is the combined test document
					CombinedDocument cTestDoc=new CombinedDocument(testFolderText+File.separator+filename
							, getFilePathGivenFolderAndFilename(testFolderBow,filename), getFilePathGivenFolderAndFilename(testFolderWn,filename)
							,getFilePathGivenFolderAndFilename(testFolderWiki,filename), getFilePathGivenFolderAndFilename(testFolderEsa, filename));
					double[] f1sims=getMaxCosSimilarityWithEachScoreLevel(cTestDoc, cSuperDocList);
					int f1=getMaxCosFeatureFromSims(f1sims);
					double f2 = getCosW4Feature(cTestDoc, cSuperDocList);
					
					arrayF1.add(new Integer(f1));
					arrayF2.add(new Double(f2));
					arrayScore.add(score);
					// add the predicted result to the confusion matrix
					int predIndex = getClassIndex(Integer.toString(f1));
					int actualIndex = getClassIndex(Integer.toString(score));
					confusionMatrix[actualIndex][predIndex] += 1;
					
					featsOut+=filename+","+f1+","+formatDecimal(f2)+","+score;
					for (int k=0; k<f1sims.length; k++) {
						featsOut+=","+formatDecimal(f1sims[k]);
					}
					featsOut+="\n";
				}
				System.out.println("Finished prompt " + prompt + " data fold "+ i);
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
			String outDir=outFolder+File.separator+"erater-"+prompt+"-feats-withsim.csv";
			FileIO.writeFile(outDir, featsOut);
			
			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
		}
		result+="average,"+formatDecimal(sumMaxcos/(double)4)+","+formatDecimal(sumCosw4/(double)4)+","+formatDecimal(sumavgf/(double)4)+
				","+formatDecimal(sumf/(double)4)+","+formatDecimal(sumaccu/(double)4)+","+formatDecimal(sumf2/(double)4)+","+formatDecimal(sumf3/(double)4)+","+formatDecimal(sumf4/(double)4)
				+","+formatDecimal(sump2/(double)4)+","+formatDecimal(sump3/(double)4)+","+formatDecimal(sump4/(double)4)
				+","+formatDecimal(sumr2/(double)4)+","+formatDecimal(sumr3/(double)4)+","+formatDecimal(sumr4/(double)4)+"\n";
//		write all the correlation results to one file
		FileIO.writeFile(resultDir, result);
		FileIO.writeFile(matrixDir, matrixOut);
	}
	
	/**
	 * it runs and print out results, a quick and dirty way to test similarity weights
	 */
	public String getEvaluationFromFilesQuick () { 
		
		HashMap<String, Integer> file_score = filescorelistToHash();
//		 this is the result output string, will be a .csv table
		String result = ",maxcos,cosw4,aFmeasure,waFmeasure,accuracy,f2,f3,f4,p2,p3,p4,r2,r3,r4\n";
//		output for confusion matrices
//		String matrixOut="";
//		String resultDir=outFolder+File.separator+"erater-results.csv";
//		String matrixDir=outFolder+File.separator+"erater-confusionmatrix.txt";
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
		
		for (String prompt : prompts) {
//			String featsOut = "Filename,maxcos,cosw4,score,simw2,simw3,simw4\n";
			// this is the array/list for F1, F2, actual score (for the whole
			// prompt)
			ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // max.cos
			ArrayList<Double> arrayF2 = new ArrayList<Double>(); // cosw4
			ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list			
			int[][] confusionMatrix = new int[numClasses][numClasses];
			
			for (int i = 0; i < numFolds; i++) {
				System.out.println("Start processing " + prompt + " data fold "+ i);
//				the super folders of different representations
				String superFolderText= textFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i;
				String superFolderBow=(bowFolder!=null) ? bowFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i : null;
				String superFolderWn=(wnFolder!=null) ? wnFolder+File.separator+prompt+File.separator+"super"+File.separator
						+"super"+i : null;
				String superFolderWiki=(wikiFolder!=null) ? wikiFolder+File.separator+prompt+File.separator+"super"+File.separator
				+"super"+i: null;
				String superFolderEsa= (esaFolder!=null) ? esaFolder+File.separator+prompt+File.separator+"super"+File.separator+"super"+i : null;
//				the test folders of different representations
				String testFolderText=textFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i;
				String testFolderBow=(bowFolder!=null) ? (bowFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderWn=(wnFolder!=null) ? (wnFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderWiki=(wikiFolder!=null) ? (wikiFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				String testFolderEsa=(esaFolder!=null) ? (esaFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+i) : null;
				
//				get the super CombinedDocument
				ArrayList<CombinedDocument> cSuperDocList=getSuperCombinedDocsInList(superFolderText, superFolderBow
						, superFolderWn, superFolderWiki, superFolderEsa);
//				get test file names from the textFolder
				String[] filenames=new File (testFolderText).list();
				for (String filename : filenames) {
					System.out.println("Processing " + filename);
					int score = getScoreGivenFilename(file_score, filename);
//					this is the combined test document
					CombinedDocument cTestDoc=new CombinedDocument(testFolderText+File.separator+filename
							, getFilePathGivenFolderAndFilename(testFolderBow,filename), getFilePathGivenFolderAndFilename(testFolderWn,filename)
							,getFilePathGivenFolderAndFilename(testFolderWiki,filename), getFilePathGivenFolderAndFilename(testFolderEsa, filename));
					double[] f1sims=getMaxCosSimilarityWithEachScoreLevel(cTestDoc, cSuperDocList);
					int f1=getMaxCosFeatureFromSims(f1sims);
					double f2 = getCosW4Feature(cTestDoc, cSuperDocList);
					
					arrayF1.add(new Integer(f1));
					arrayF2.add(new Double(f2));
					arrayScore.add(score);
					// add the predicted result to the confusion matrix
					int predIndex = getClassIndex(Integer.toString(f1));
					int actualIndex = getClassIndex(Integer.toString(score));
					confusionMatrix[actualIndex][predIndex] += 1;
					
//					featsOut+=filename+","+f1+","+formatDecimal(f2)+","+score;
//					for (int k=0; k<f1sims.length; k++) {
//						featsOut+=","+formatDecimal(f1sims[k]);
//					}
//					featsOut+="\n";
				}
				System.out.println("Finished prompt " + prompt + " data fold "+ i);
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
//			String outDir=outFolder+File.separator+"erater-"+prompt+"-feats-withsim.csv";
//			FileIO.writeFile(outDir, featsOut);
			
//			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
		}
		result+="average,"+formatDecimal(sumMaxcos/(double)4)+","+formatDecimal(sumCosw4/(double)4)+","+formatDecimal(sumavgf/(double)4)+
				","+formatDecimal(sumf/(double)4)+","+formatDecimal(sumaccu/(double)4)+","+formatDecimal(sumf2/(double)4)+","+formatDecimal(sumf3/(double)4)+","+formatDecimal(sumf4/(double)4)
				+","+formatDecimal(sump2/(double)4)+","+formatDecimal(sump3/(double)4)+","+formatDecimal(sump4/(double)4)
				+","+formatDecimal(sumr2/(double)4)+","+formatDecimal(sumr3/(double)4)+","+formatDecimal(sumr4/(double)4)+"\n";
//		write all the correlation results to one file
//		FileIO.writeFile(resultDir, result);
//		FileIO.writeFile(matrixDir, matrixOut);
		return result;
	}
	
	
//	public void getEvaluationFromEsaSimFile (String outFolder, String esaSimFolder) {
//		HashMap<String, Integer> file_score = filescorelistToHash();
////		 this is the result output string, will be a .csv table
//		String result = ",TotalFmeasure,maxcos,cosw4\n";
////		output for confusion matrices
//		String matrixOut="";
//		String resultDir=outFolder+File.separator+"erater-results.csv";
//		String matrixDir=outFolder+File.separator+"erater-confusionmatrix.txt";
//		
//		for (String prompt : prompts) {
//			String esaSimFile=Params.esaSimFolder+File.separator+"sim-"+prompt+".txt";
////			the key=>value means: key is the combination string of "testfile,superfile", and value is their cos similarity
//			HashMap<String,Double> testsuper_sim=FileIO.readFileToHash(esaSimFile, 0);
//			
//			String featsOut = "Filename,maxcos,cosw4,score\n";
//			// this is the array/list for F1, F2, actual score (for the whole
//			// prompt)
//			ArrayList<Integer> arrayF1 = new ArrayList<Integer>(); // max.cos
//			ArrayList<Double> arrayF2 = new ArrayList<Double>(); // cosw4
//			ArrayList<Integer> arrayScore = new ArrayList<Integer>(); // the actual score list			
//			int[][] confusionMatrix = new int[numClasses][numClasses];
//			
//			for (int i = 0; i < numFolds; i++) {
//				System.out.println("Start processing " + prompt + " data fold "+ i);
//				
//				String superFolderText= textFolder+File.separator+prompt+File.separator+"super"+File.separator
//						+"train"+i;
//				String superFolderBow=(bowFolder!=null) ? bowFolder+File.separator+prompt+File.separator+"super"+File.separator
//						+"train"+i : null;
//				String superFolderWn=(wnFolder!=null) ? wnFolder+File.separator+prompt+File.separator+"super"+File.separator
//						+"train"+i : null;
//				String superFolderWiki=(wikiFolder!=null) ? wikiFolder+File.separator+prompt+File.separator+"super"+File.separator
//				+"train"+i: null;
//				String testFolderText=textFolder+File.pathSeparator+prompt+File.separator+"test"+i;
//				String testFolderBow=(bowFolder!=null) ? (bowFolder+File.separator+prompt+File.separator+"test"+i) : null;
//				String testFolderWn=(wnFolder!=null) ? (wnFolder+File.separator+prompt+File.separator+"test"+i) : null;
//				String testFolderWiki=(wikiFolder!=null) ? (wikiFolder+File.separator+prompt+File.separator+"test"+i) : null;
//				
////				get the super CombinedDocument
//				ArrayList<CombinedDocument> cSuperDocList=getSuperCombinedDocsInList(superFolderText, superFolderBow
//						, superFolderWn, superFolderWiki);
////				get test file names from the textFolder
//				String[] filenames=new File (textFolder+File.separator+prompt+File.separator+"test"+i).list();				
//				for (String filename : filenames) {
//					System.out.println("Processing " + filename);
//					int score = getScoreGivenFilename(file_score, filename);
//					
////					this is the combined test document, but doens't include the ESA representation in the CombinedDocument
//					CombinedDocument cTestDoc=new CombinedDocument(testFolderText+File.separator+filename
//							, getFilePathGivenFolderAndFilename(testFolderBow,filename), getFilePathGivenFolderAndFilename(testFolderWn,filename)
//							,getFilePathGivenFolderAndFilename(testFolderWiki,filename), cType);
//					
//					int f1 = getMaxCosFeature(cTestDoc, cSuperDocList, testsuper_sim, filename);
//					double f2 = getCosW4Feature(cTestDoc, cSuperDocList, testsuper_sim, filename);
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
//			result+=prompt+","+formatDecimal(totalF)+","+formatDecimal(corr1)+","+formatDecimal(corr2)+"\n";
////			this is writing to the output file of values of feature1, feature2, and actual score
//			String outDir=outFolder+File.separator+"prom-"+prompt+"-all.csv";
//			FileIO.writeFile(outDir, featsOut);
//			
//			matrixOut+=toMatrixString("Confusion Matrix of prompt"+prompt, confusionMatrix)+"\n";
//		}
////		write all the correlation results to one file
//		FileIO.writeFile(resultDir, result);
//		FileIO.writeFile(matrixDir, matrixOut);
//	}
	
	int getMaxCosFeature (CombinedDocument cTestDoc, ArrayList<CombinedDocument> cSuperDocList) {
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value, like score "2" or so; it's an array because maybe two score
					     // categories share the same simialrity with the essay (although this is very low probability)
//		the max numVec is 3: bowVec, wnVec, and wikiVec

		for (int score = minScore; score < maxScore + 1; score++) {
			CombinedDocument cSuperDoc=cSuperDocList.get(score-minScore);			
//			an important presupposition: the bow vec is normalized by Euclidean length, so is WN and Wiki vector
//			that makes the cossim computation much easier, just get the (sum of dotproduct)/num of not null vec
			double cossim=similarity.computeCosSimBetweenCombinedDocs (cTestDoc, cSuperDoc);
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = score;
			}
		}
		return maxPosit;
	}
	
	int getMaxCosFeatureFromSims (double[] sims) {
		int maxPosit = 0;
		double maxSim=-1.0;
		
		for (int i=0; i<sims.length; i++) {
			double cossim=sims[i];
			if (cossim > maxSim) {
				maxSim = cossim;
				maxPosit = i;
			}
		}
		
		return maxPosit+minScore;
	}
	
	double[] getMaxCosSimilarityWithEachScoreLevel (CombinedDocument cTestDoc, ArrayList<CombinedDocument> cSuperDocList) {
		double[] sims=new double[3];
		
		Similarity similarity = new Similarity();
		double maxSim = -1.0; // the maximum cosine similarity value
		int maxPosit = 0; // position of the maximum value, like score "2" or so; it's an array because maybe two score
					     // categories share the same simialrity with the essay (although this is very low probability)
//		the max numVec is 3: bowVec, wnVec, and wikiVec

		for (int score = minScore; score < maxScore + 1; score++) {
			CombinedDocument cSuperDoc=cSuperDocList.get(score-minScore);			
//			an important presupposition: the bow vec is normalized by Euclidean length, so is WN and Wiki vector
//			that makes the cossim computation much easier, just get the (sum of dotproduct)/num of not null vec
			double cossim=similarity.computeCosSimBetweenCombinedDocs (cTestDoc, cSuperDoc);
			sims[score-minScore]=cossim;
		}
		
		return sims;
	}
	
	
	/***
	 * this is for the esa case
	 * 
	 * @param cTestDoc
	 * @param cSuperDocList
	 * @param testsuper_sim
	 * @param filename
	 * @return
	 */
//	int getMaxCosFeature (CombinedDocument cTestDoc, ArrayList<CombinedDocument> cSuperDocList, HashMap<String,Double> testsuper_sim, String filename) {
//		Similarity similarity = new Similarity();
//		double maxSim = -1.0; // the maximum cosine similarity value
//		int maxPosit = 0; // position of the maximum value, like score "2" or so; it's an array because maybe two score
//					     // categories share the same simialrity with the essay (although this is very low probability)
////		the max numVec is 3: bowVec, wnVec, and wikiVec
//
//		for (int score = minScore; score < maxScore + 1; score++) {
//			CombinedDocument cSuperDoc=cSuperDocList.get(score-minScore);
////			get the ESA similarity between the test and super files
//			double esaSim=getEsaSimGivenTestAndSuper(filename, score, testsuper_sim);
////			an important presupposition: the bow vec is normalized by Euclidean length, so is WN and Wiki vector
////			that makes the cossim computation much easier, just get the (sum of dotproduct)/num of not null vec
//			double cossim=similarity.computeCosSimBetweenCombinedDocsWithEsa (cTestDoc, cSuperDoc, esaSim);
//			if (cossim > maxSim) {
//				maxSim = cossim;
//				maxPosit = score;
//			}
//		}
//		return maxPosit;
//	}
	
	double getCosW4Feature (CombinedDocument cTestDoc, ArrayList<CombinedDocument> cSuperDocList) {
		Similarity similarity = new Similarity();
		return similarity.computeCosSimBetweenCombinedDocs(cTestDoc, cSuperDocList.get(maxScore-minScore));
	}
	
	/***
	 * this is for the esa case
	 * 
	 * @param cTestDoc
	 * @param cSuperDocList
	 * @return
	 */
//	double getCosW4Feature (CombinedDocument cTestDoc, ArrayList<CombinedDocument> cSuperDocList, HashMap<String,Double> testsuper_sim, String filename) {
//		Similarity similarity = new Similarity();
//		CombinedDocument cSuperDoc=cSuperDocList.get(maxScore-minScore);
//		double esaSim=getEsaSimGivenTestAndSuper(filename, 4, testsuper_sim); //the esa sim with score level 4 (super file)
//		return similarity.computeCosSimBetweenCombinedDocsWithEsa (cTestDoc, cSuperDoc, esaSim);
//	}
	
//	double getEsaSimGivenTestAndSuper (String test, int score, HashMap<String,Double> testsuper_sim) {
//		double esasim=0;
//		if (testsuper_sim.containsKey(test+","+Integer.toString(score))) {
//			esasim=testsuper_sim.get(test+","+Integer.toString(score)).doubleValue();
//		}
//		return esasim;
//	}
	
	/***
	 * count number of not null vectors (see if bowFolder, wnFolder, and wikiFolder are null)
	 * 
	 */
//	int countNumOfNotNullVectorsInCombinedDoc () {
//		int num=0;
//		if (bowFolder != null)
//			num++;
//		if (wnFolder != null)
//			num++;
//		if (wikiFolder != null)
//			num++;
//		return num;
//	}
	
	/***
	 * if the folder is not null, then return folder/filename
	 * if folder is null, then return null as file path
	 * 
	 * @param folder
	 * @param filename
	 * @return
	 */
	String getFilePathGivenFolderAndFilename (String folder, String filename) {
		return (folder != null) ? (folder+File.separator+filename) : null;
	}
	
	ArrayList<CombinedDocument> getSuperCombinedDocsInList (String superFolderText, String superFolderBow, String superFolderWn, String superFolderWiki, String superFolderEsa) {
		ArrayList<CombinedDocument> cSuperDocList=new ArrayList<CombinedDocument> ();
		for (int score = minScore; score < maxScore + 1; score++) {
			// #this superFile name is customized and is subject to change
			CombinedDocument cSuperDoc=new CombinedDocument (superFolderText+File.separator+Integer.toString(score)
					, getFilePathGivenFolderAndFilename(superFolderBow,Integer.toString(score))
					, getFilePathGivenFolderAndFilename(superFolderWn, Integer.toString(score))
					, getFilePathGivenFolderAndFilename(superFolderWiki, Integer.toString(score))
					, getFilePathGivenFolderAndFilename(superFolderEsa, Integer.toString(score)));
			cSuperDocList.add(cSuperDoc);
		}
		return cSuperDocList;
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
	
	int getScoreGivenFilename(HashMap<String, Integer> file_score, String filename) {
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
	
	double averageFMeasure (int[][] confusionMatrix) {
		double totalF = 0;
		
		for (int i = 0; i < numClasses; i++) {
			double temp = computeFmeasure(confusionMatrix, i);
//			System.out.println("F measure for class index "+i+" is "+temp);
			totalF += temp;
		}
		
		return totalF/(double)numClasses;
	}
	
	double accuracyRate(int[][] confusionMatrix) {
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
	
	
	public static void main (String[] args) {
		
//		*********code for inType=0*******
////		try bow + wn vectors
//		String textFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
////		String bowFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-vec";
//		String bowFolder=null;
//		String wnFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn-vec-pos";
////		String wnFolder=null;
//		String wikiFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/vec";
////		String wikiFolder=null;
//		String scoreList="/Users/miaochen/Documents/diss-experiment/info/1239files-mergedscore.txt";
//		int inType=0;
//		int cType=0; //combined type is add
//		String outFolder="/Users/miaochen/Documents/diss-experiment/results/test";
//		
//		EraterFeaturesForCombinedDoc erater=new EraterFeaturesForCombinedDoc(textFolder, bowFolder
//				,wnFolder, wikiFolder, scoreList, inType, cType);
//		erater.getEvaluation(outFolder, null);
		
		
//		********code for inType=0 (e.g. wn + bow vec), cType=1****
//		it seems cType is not a useful param here
		
		
		//****remember to change numFolds of this class!!//
		String textFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
//		String bowFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-stem-nonstop";  //param 1
		String bowFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/lsa-vec-200";  //param 1, we can also use LSA vec for erater computation here (virtually treat them as bow vec)
//		String bowFolder=null;
//		String wnFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wn1st-stop"; //param 2
		String wnFolder=null;
//		String wikiFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/wiki-vec-sorted"; //param 3
		String wikiFolder=null;
//		String esaFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/esa/vec";  //param 4
		String esaFolder=null;
		String scoreList="/Users/miaochen/Documents/diss-experiment/info/1237files-mergedscore.txt";
//		int inType=0;
		int cType=0; //combined type is replace
		String outFolder="/Users/miaochen/Documents/diss-experiment/results/wn1st-stop";  //param 5
		
		EraterFeaturesForCombinedDoc erater=new EraterFeaturesForCombinedDoc(textFolder, bowFolder
				,wnFolder, wikiFolder, esaFolder, scoreList, cType);
//		erater.getEvaluation(outFolder, null);
		erater.getEvaluationFromFilesWithSimDetails(outFolder);
		System.out.println(erater.getEvaluationFromFilesQuick());
	}

}
