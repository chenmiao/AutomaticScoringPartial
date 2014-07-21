package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyEsaOut {
	
	static void copyfile (String sourceDir, String targetDir) {
		try {
			
			InputStream in=new FileInputStream(sourceDir);
			OutputStream out=new FileOutputStream(targetDir);
			
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
	
	/***
	 * it removes the .out extension for the filenames
	 * 
	 */
	static String removeOutExtension (String fname) {
		if (fname.endsWith(".output"))
			return fname.substring(0, fname.length()-7);
		else
			return fname;
	}
	
	
	public static void main (String[] args) {
		
		String rawFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/esa/rawout";
		String outFolder="/Users/miaochen/Documents/diss-experiment/proc-corpus/esa/vec";
		
		for (String prompt : Params.prompts) {
			for (String fold : Params.folds) {
				System.out.println("Processing prompt "+prompt+" fold "+fold);
				String[] subsets={"train","test","super"};
				
				for (String subset : subsets) {
					String sourceFolder=rawFolder+File.separator+prompt+File.separator+subset+File.separator+subset+fold+File.separator+"output";
					String targetFolder=outFolder+File.separator+prompt+File.separator+subset+File.separator+subset+fold;
					System.out.println(sourceFolder);
					String[] filenames=new File(sourceFolder).list();
					for (String filename : filenames) {
						String sourceDir=sourceFolder+File.separator+filename;
						String targetDir=targetFolder+File.separator+removeOutExtension(filename);
						System.out.println("Copying "+sourceDir);
						copyfile(sourceDir, targetDir);
					}
				}
				
			}
		}
		
	}

}
