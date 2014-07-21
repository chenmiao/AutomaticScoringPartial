package wekacode;

import java.io.File;

import weka.core.Instances;

public class Tester {
	
	public static void main (String[] args){
		
		//merge instances (merging their attributes)
//		String arff1="C:\\Users\\Miao\\Downloads\\sesFiltered5.arff";
//		String arff2="C:\\Users\\Miao\\Downloads\\sesFiltered7.arff";
//		String outPath="C:\\Users\\Miao\\Downloads\\mergedtest.arff";
//		
//		Instances inst1=WekaIO.readArffToInstances(arff1);
//		Instances inst2=WekaIO.readArffToInstances(arff2);
//		
//		Instances mergedInst=Instances.mergeInstances(inst1, inst2);
//		WekaIO.writeInstancesToArff(mergedInst, outPath);
		
		//Instances inst=WekaIO.readArffToInstances("C:\\Users\\Miao\\Downloads\\test.arff");
		
//		String inPath="";
//		WekaIO.readArffToInstances(inPath);
		int correctIns=1;
		int totalIns=3;
		double accu=(totalIns==0) ? 0 : (double)correctIns/(double)totalIns;
		System.out.println(accu);
		
	}

}
