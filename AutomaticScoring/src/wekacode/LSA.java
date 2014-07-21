package wekacode;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.LatentSemanticAnalysis;
import weka.attributeSelection.Ranker;
import weka.core.Instances;


public class LSA {
	
	public static void main (String[] args){
		
		// assume Instances inputData is your dataset which has already been loaded and set up 
		AttributeSelection selecter = new AttributeSelection();
		LatentSemanticAnalysis lsa = new LatentSemanticAnalysis(); 
		lsa.setMaximumAttributeNames(-1);
		Ranker rank = new Ranker(); // The default parameters for Ranker and AttributeSelection are appropriate for LSA. You can adjust the LSA options as desired.

		selecter.setEvaluator(lsa);
		selecter.setSearch(rank);

		// the methods in the next two lines can throw exceptions, so you have to deal with those appropriately
		//**********Need to get data from text folder...
		String inPath="C:\\Users\\Miao\\Downloads\\test.arff";
		Instances dataIns=WekaIO.readArffToInstances(inPath);
		dataIns.setClassIndex(0);
		//dataIns.deleteAttributeAt(0);
		
		try {
			selecter.SelectAttributes(dataIns);
			Instances outputData = selecter.reduceDimensionality(dataIns);
			
			// the transformed data is now in the outputData object
			String outPath="C:\\Users\\Miao\\Downloads\\may28out2.arff";
			WekaIO.writeInstancesToArff(outputData, outPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		

		
		
	}
	

}
