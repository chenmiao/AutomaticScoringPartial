package utilities;

import java.io.File;
import java.util.ArrayList;

public class TextPreprocessor {
	
	
	public static void main (String[] args) {
		
        String inprefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml-mar13";
        String outprefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml";
		
		FolderIterator fi=new FolderIterator(inprefix);
		ArrayList<ArrayList<String>> infolders=fi.getFoldersInList();
				
		for (ArrayList<String> f : infolders) {
			for (String aFolder : f) {
				
				String[] filenames=new File(aFolder).list();
				
				for (String filename : filenames) {
					
					System.out.println("Processing "+filename);
					
					String fDir=aFolder+File.separator+filename;
					String text=FileIO.getFileByEncoding(fDir, "utf-8");
//					String text=FileIO.getFileAsString(fDir);
//					text=text.replaceAll("’", "'");
					text=text.replaceAll("\\?", "'");
					
					String outDir=fDir.replaceFirst(inprefix, outprefix);
					FileIO.writeFile(outDir, text);
				}
				
			}
		}
		
	}

}
