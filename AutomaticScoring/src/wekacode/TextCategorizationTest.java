package wekacode;

import weka.core.*;
import weka.core.converters.*;
import weka.classifiers.trees.*;
import weka.filters.*;
import weka.filters.unsupervised.attribute.*;

import java.io.*;

/**
 * Example class that converts HTML files stored in a directory structure into 
 * and ARFF file using the TextDirectoryLoader converter. It then applies the
 * StringToWordVector to the data and feeds a J48 classifier with it.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class TextCategorizationTest {

  /**
   * Expects the first parameter to point to the directory with the text files.
   * In that directory, each sub-directory represents a class and the text
   * files in these sub-directories will be labeled as such.
   *
   * @param args        the commandline arguments
   * @throws Exception  if something goes wrong
   */
  public static void main(String[] args) throws Exception {
    // convert the directory into a dataset
//    TextDirectoryLoader loader = new TextDirectoryLoader();
//    loader.setDirectory(new File(args[0]));
//    Instances dataRaw = loader.getDataSet();
    
    //String out1="C:\\Users\\Miao\\Downloads\\dataraw.arff";
    //WekaIO.writeInstancesToArff(dataRaw, out1);
    
    //write Instances to Arff
//    ArffSaver saver = new ArffSaver();
//    saver.setInstances(dataRaw);
//    saver.setFile(new File("C:\\Users\\Miao\\Downloads\\test.arff"));
//    saver.writeBatch();
    
    //System.out.println("\n\nImported data:\n\n" + dataRaw);

    // apply the StringToWordVector
    // (see the source code of setOptions(String[]) method of the filter
    // if you want to know which command-line option corresponds to which
    // bean property)
	  
	//read arff as Instances
	String inPath="C:\\Users\\Miao\\Documents\\My Dropbox\\Projects\\insitu-weka\\sesWekaData.arff";
	Instances dataRaw=WekaIO.readArffToInstances(inPath);	
	  
    StringToWordVector filter = new StringToWordVector();
    filter.setInputFormat(dataRaw);
    filter.setOptions(Utils.splitOptions("-R 8 -I -stemmer weka.core.stemmers.SnowballStemmer -L -stopwords \"C:\\Users\\Miao\\Documents\\My Dropbox\\Projects\\insitu-weka\\stopword.txt\" -P attr8-"));
    //filter.setOptions(Utils.splitOptions("-R 1"));
    
    Instances dataFiltered1 = Filter.useFilter(dataRaw, filter);
    //System.out.println("\n\nFiltered data:\n\n" + dataFiltered);
    
    int index=dataFiltered1.attribute("objectmetavalue4").index();
    //System.out.println(index);
    
    filter.setInputFormat(dataFiltered1);
    filter.setOptions(Utils.splitOptions("-R "+Integer.toString(index+1)+" -I -stemmer weka.core.stemmers.SnowballStemmer -L -stopwords \"C:\\Users\\Miao\\Documents\\My Dropbox\\Projects\\insitu-weka\\stopword.txt\" -P attr9-"));

    Instances dataFiltered2=Filter.useFilter(dataFiltered1, filter);
    
    String outPath="C:\\Users\\Miao\\Downloads\\sesFiltered9.arff";
    WekaIO.writeInstancesToArff(dataFiltered2, outPath);

    // train J48 and output model
//    J48 classifier = new J48();
//    classifier.buildClassifier(dataFiltered);
    //System.out.println("\n\nClassifier model:\n\n" + classifier);
    
    //print out the options of StringToWordVector
    String[] opts=filter.getOptions();
    for(String opt:opts){
    	System.out.println(opt);
    }
    
  }
}

