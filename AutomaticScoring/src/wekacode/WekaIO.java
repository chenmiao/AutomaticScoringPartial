package wekacode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.BufferedReader;
import java.io.FileReader;

public class WekaIO {
	
	static Instances readArffToInstances(String inPath){
		
		BufferedReader reader;
		Instances data=null;
		
		try {
			reader = new BufferedReader(new FileReader(
					inPath));
			data = new Instances(reader);
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return data;
		
	}

	static void writeInstancesToArff(Instances dataInstances, String outPath) {

		// write Instances to Arff
		ArffSaver saver = new ArffSaver();
		saver.setInstances(dataInstances);

		try {
			saver.setFile(new File(outPath));
			saver.writeBatch();
			
			System.out.println("Finished writing to "+outPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	static void writeText(String outDir, String content) {
		BufferedWriter wr = null;
		try {// deal with attributes part
			wr = new BufferedWriter(new FileWriter(new File(outDir)));
			wr.write(content);
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		
		String inPath="C:\\Users\\Miao\\Downloads\\sesWekaData.arff";
		System.out.println(readArffToInstances(inPath));

	}

}
