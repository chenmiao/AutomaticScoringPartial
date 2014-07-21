package wekacode;

import java.io.File;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSink;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AddClassification;
import weka.filters.unsupervised.instance.SparseToNonSparse;

public class Predictor {
	
public static void main (String[] args) throws Exception{
		
		String inFolder="C:\\Users\\Miao\\Projects\\diss-experiment\\proc-corpus\\lsi-vec";		
		int numFolds=3;
		String[] prompts={"098","099","100","101"};
		
		String classname="weka.classifiers.bayes.NaiveBayes";
		Classifier cls = (Classifier) Utils.forName(Classifier.class, classname, null);		
		
		for (String prompt : prompts){
			
			System.out.println("*********************************\nProcessing prompt "+prompt);
			Instances predictedData=null;
			String predDir="C:\\Users\\Miao\\Projects\\diss-experiment\\results\\lsi\\wekaNB-"+prompt+"-pred.arff";
			String predCsvDir="C:\\Users\\Miao\\Projects\\diss-experiment\\results\\lsi\\wekaNB-"+prompt+"-pred.csv";
						
			for(int k=0;k<numFolds;k++){
				
				System.out.println("Processing data fold "+k);
				
				String trainArff=inFolder+File.separator+prompt+"-train"+(new Integer(k)).toString()+"-score.arff";
				String testArff=inFolder+File.separator+prompt+"-test"+(new Integer(k)).toString()+"-score.arff";
				
				Instances trainIns=WekaIO.readArffToInstances(trainArff);
				Instances testIns=WekaIO.readArffToInstances(testArff);
				
				trainIns.setClassIndex(trainIns.numAttributes()-1);
				testIns.setClassIndex(testIns.numAttributes()-1);
				
				try {					
					// add predictions
				    AddClassification filter = new AddClassification();
				    filter.setClassifier(cls);
				    filter.setOutputClassification(true);
				    filter.setOutputDistribution(true);
				    filter.setOutputErrorFlag(true);
				    filter.setInputFormat(trainIns);
				    Filter.useFilter(trainIns, filter);  // trains the classifier
				    Instances pred = Filter.useFilter(testIns, filter);  // perform predictions on test set
				    
				    //delete the token attributes only with the classes/predicted classes etc. attributes left
				    //this is because token attribues are different for the 3 folds
				    //!!!this is probably not the best, it'd be better to deep copy the useful attributes (last 5 attributes)
				    for (int a=0; a<trainIns.numAttributes()-1; a++){
				    	//this is the tricky way to remove attributes, remove the idx=0 attribute in each loop (because the previous one is gone!)
				    	pred.deleteAttributeAt(0);
				    }
				    
				    if (predictedData == null)
				      predictedData = new Instances(pred, 0);
				    for (int j = 0; j < pred.numInstances(); j++)
				      predictedData.add(pred.instance(j));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
			//System.out.println("Is it null? "+predictedData==null);
			// output "enriched" dataset
		    try {
		    	SparseToNonSparse sparToNonSpar=new SparseToNonSparse();
		    	sparToNonSpar.setInputFormat(predictedData);
		    	Instances nonSparsePredictedData=Filter.useFilter(predictedData, sparToNonSpar);
	    	
				DataSink.write(predDir, nonSparsePredictedData);
				System.out.println("Finished writing to "+predDir);
				
				CSVSaver csvsaver=new CSVSaver();
		    	csvsaver.setInstances(nonSparsePredictedData);
		    	csvsaver.setFile(new File(predCsvDir));
//		    	csvsaver.setDestination(new File(predCsvDir));
		    	csvsaver.writeBatch();
		    	System.out.println("Finished writing to "+predCsvDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	

}
