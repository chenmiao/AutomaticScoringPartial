package wekacode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Random;

import weka.core.Instance;
import weka.core.Instances;

public class DataSplitter {
	
	/***
	 * The output file is in "filename\n" style
	 * 
	 * @param inst
	 * @param outPath
	 */

	static void writeInstancesToFile(Instances inst,String outPath){
		
		Enumeration enumInst=inst.enumerateInstances();
		String out="";
		
		while (enumInst.hasMoreElements()){
			
			Instance datum=(Instance) enumInst.nextElement();
			out+=datum.stringValue(0)+"\n";
			
		}
		
		WekaIO.writeText(outPath, out);
		
	}
	
	/***
	 * Given instances of filename/score, for each instance,
	 * copy its original file (origFolder) to the output folder (outFolder). 
	 * the ultimate purpose is to generate train/test folder pairs.
	 * 
	 * @param inst
	 * @param outFolder
	 */
	
	static void copyInstancesToFolder(Instances inst,String origFolder,String destFolder){
		
		Enumeration enumInst=inst.enumerateInstances();
		
		while (enumInst.hasMoreElements()){
			
			Instance datum=(Instance) enumInst.nextElement();
			String filename=datum.stringValue(0);
			
			copyFile(origFolder+File.separator+filename,destFolder+File.separator+filename);			
			
		}
		
	}
	
	/***
	 * it copies an original file to a destination file
	 * 
	 * @param origDir
	 * @param destDir
	 */
	static void copyFile(String origDir,String destDir){
		
		try {
			
			InputStream in=new FileInputStream(origDir);
			OutputStream out=new FileOutputStream(destDir);
			
			byte[] buf=new byte[1024];
			int len;
			
			while((len=in.read(buf))>0){
				out.write(buf,0,len);
			}
			
			in.close();
			out.close();			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String prompt="101"; //param 1

		int numFolds = 10;  //param 2
//		String inPath = "C:\\Users\\Miao\\Projects\\diss-experiment\\out\\merged\\"+prompt+"filescore.arff";
		String inPath = "/Users/miaochen/Documents/diss-experiment/out/merged/101filescore.arff";  //param3
		int seed = 1;// the seed for randomizing the data
		Instances inst = WekaIO.readArffToInstances(inPath);
		inst.setClassIndex(1);
//		String outFolder="C:\\Users\\Miao\\Projects\\diss-experiment\\proc-corpus\\datafolds";
		String outFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus-10fold/datafolds";
		
		// randomize data
		Random rand = new Random(seed);
		Instances randData = new Instances(inst);
		randData.randomize(rand);
		randData.stratify(numFolds);
		
//		String origFolder="C:\\Users\\Miao\\Projects\\diss-experiment\\orig-corpus\\all-1239files";
		String origFolder = "/Users/miaochen/Documents/diss-experiment/orig-corpus/all-1237files";

		// generate the folds
		for (int n = 0; n < numFolds; n++) {
			
			System.out.println("Splitting the "+n+"th fold...");
			
			Instances train = randData.trainCV(numFolds, n);
			Instances test = randData.testCV(numFolds, n);
			
			//writeInstancesToFile(train,outFolder+File.separator+"train"+Integer.toString(n)+".txt");
			//writeInstancesToFile(test,outFolder+File.separator+"test"+Integer.toString(n)+".txt");
			
			String trainDestFolder=outFolder+File.separator+prompt+File.separator+"train"+File.separator+"train"+Integer.toString(n);
			String testDestFolder=outFolder+File.separator+prompt+File.separator+"test"+File.separator+"test"+Integer.toString(n);
			
			copyInstancesToFolder(train,origFolder,trainDestFolder);
			copyInstancesToFolder(test,origFolder,testDestFolder);

		}

		// this is the previous code for using StratifiedRemoveFolds
		// StratifiedRemoveFolds straFolds=new StratifiedRemoveFolds();
		// straFolds.setNumFolds(numFolds);
		// Instances instances=WekaIO.readArffToInstances(inPath);
		// Instances splitInstances=Filter.useFilter(instances, straFolds);

	}

}
