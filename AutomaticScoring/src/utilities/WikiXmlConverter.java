package utilities;

import java.io.ByteArrayInputStream;
import java.io.File;


/***
 * it converts the xml files generated from Wikification to "well-informed" xml files
 * because it does not contain an overall tag, i.e. <Result>...</Result> to wrap the whole file
 * also write it to "utf-encoding"
 * 
 * one functionality: given an xml folder, convert it to good xml files
 * 
 * @author miaochen
 *
 */

public class WikiXmlConverter {


	WikiXmlConverter() {
		
	}

	/***
	 * 
	 * @param inPath  input folder path
	 * @param outPath  output folder path
	 */
	void convertXmlFolder (String inPath, String outPath, String encoding) {
		String[] files=new File(inPath).list();
		for (String afile: files) {
//			first check if this is the .xml extension that we need
			if (isXmlFile (afile)) {
				String xmlpath=inPath+File.separator+afile;
				String text=FileIO.getFileByEncoding(xmlpath, encoding);
//				wrap the xml body with an overall <Result>...</Result> tag
				text="<Result>\n"+text+"</Result>\n";
				
				String outXml=outPath+File.separator+afile;
				FileIO.writeFile(outXml, text);
			}			
			
		}
		
	}
	
	boolean isXmlFile (String afile) {
		return afile.endsWith(".xml");
	}



	public static void main(String[] args) {
		
		WikiXmlConverter xConverter=new WikiXmlConverter();
		
		String[] sets={"train","test"};
		String[] prompts={"098","099","100","101"};
		String[] folds={"0","1","2"};
		String inPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/rawxml/";
		String outPrefix="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/xml/";
		String encoding="utf-8";
		
		for (String prompt : prompts) {
			
			for (String fold : folds) {
				System.out.println("Processing prompt "+prompt+", fold "+fold);
				
				String trainSuffix=prompt+File.separator+"train"+fold;
				String testSuffix=prompt+File.separator+"test"+fold;
				String superSuffix=prompt+File.separator+"super"+File.separator+"train"+fold;
				String[] suffixes={trainSuffix, testSuffix, superSuffix};
				
				for (String suffix : suffixes) {
					System.out.println("Processing "+suffix);
					String inPath=inPrefix+suffix;
					String outPath=outPrefix+suffix;
					xConverter.convertXmlFolder(inPath, outPath, encoding);
				}
			}
			
		}


	}

}
