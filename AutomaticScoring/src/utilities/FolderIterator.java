package utilities;

import java.io.File;
import java.util.ArrayList;

/***
 * this is specifically designed for prompt + fold style
 * combine these 2 options, to get folders
 * 
 * @author miaochen
 *
 */
public class FolderIterator {
	
	String folderPrefix;
	String[] prompts={"098","099","100","101"};
	int numFolds=3;
	
	public FolderIterator (String folderPrefix) {
		this.folderPrefix=folderPrefix;
	}
	
	ArrayList<ArrayList<String>> getFoldersInList () {
//		ArrayList(0)=>super folder, ArrayList(1) => train folder, ArrayList(2) => test folder
		ArrayList<ArrayList<String>> folders=new ArrayList<ArrayList<String>> ();
		ArrayList<String> superfolders=new ArrayList<String>();
		ArrayList<String> trainfolders=new ArrayList<String>();
		ArrayList<String> testfolders=new ArrayList<String>();
		
		for (String prompt : prompts) {
			for (int i=0; i<numFolds; i++) {
				String superFolder=folderPrefix+File.separator+prompt+File.separator+"super"+File.separator+"super"+i;
				String trainFolder=folderPrefix+File.separator+prompt+File.separator+"train"+File.separator+"train"+i;
				String testFolder=folderPrefix+File.separator+prompt+File.separator+"test"+File.separator+"test"+i;
				
				superfolders.add(superFolder);
				trainfolders.add(trainFolder);
				testfolders.add(testFolder);
			}
		}
		
		folders.add(superfolders);
		folders.add(trainfolders);
		folders.add(testfolders);
		
		return folders;
	}
	
	
	public static void main (String[] args) {
		
		String fprefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/datafolds";
		
		FolderIterator fi=new FolderIterator(fprefix);
		ArrayList<ArrayList<String>> folders=fi.getFoldersInList();
		
		
		for (ArrayList<String> f : folders) {
			for (String s : f) {
				System.out.println(s);
			}
		}
		
	}

}
