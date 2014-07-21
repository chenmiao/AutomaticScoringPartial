package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import edu.mit.jwi.item.ISynset;

public class FileIO {
	
	/**
	 * description of the static method
	 * 
	 * @param filePath
	 *            input file path
	 * @param encoding
	 *            encoding
	 * @return the string derived from input file
	 * @throws IOException
	 *             throw exception
	 * @throws FileNotFoundException
	 *             throw exception
	 */
	public static String getFileByEncoding(String filePath, String encoding) {
		InputStreamReader isr=null;
		try {
			isr = new InputStreamReader(new FileInputStream(
					filePath), encoding);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(isr);
		String s;
		try {
			while ((s = in.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public static String getFileAsString(String filePath) {
		StringBuffer sb=new StringBuffer();
		try{
			BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));			
			String strLine;
			while((strLine=br.readLine())!=null){
				sb.append(strLine);
				sb.append("\r\n");
			}				
			br.close();
		}catch (Exception e){
			System.err.println("Error: "+e.getMessage());
		}
		return sb.toString();
	}
	
	public static String getFileObjectAsString(File afile) {
		StringBuffer sb=new StringBuffer();
		try{
			BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(afile))));			
			String strLine;
			while((strLine=br.readLine())!=null){
				sb.append(strLine);
				sb.append("\r\n");
			}				
			br.close();
		}catch (Exception e){
			System.err.println("Error: "+e.getMessage());
		}
		return sb.toString();
	}
	
	/***
	 * 
	 * @param filePath
	 * @param keyType  value type of the hashmap (Double, String, Integer...), usually the key type is String
	 *        valueType is usually double
	 *        keytype: 0=Double
	 *               1=Integer
	 * @return
	 * @throws IOException 
	 */
	public static HashMap readFileToHash (String filePath, int keyType) {
		HashMap k_v=null;
		
		switch (keyType) { //key is Double type
		case 0: 
			k_v=new HashMap<String,Double>();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
				String strLine;
				while((strLine=br.readLine())!=null){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 2) {
//						we very simply check this is a decimal number
						if (!units[1].matches("^[0-9]*(\\.[0-9]+)?$")) {
							continue;
						}
						k_v.put(units[0], Double.valueOf(units[1]));
					}				
				}				
				br.close();
			} catch (NumberFormatException e) {
//				this exception is usually "String-like" values, so we just discard it
				e.printStackTrace();
			} catch (IOException e) {
//				this exception is usually "String-like" values, so we just discard it
				e.printStackTrace();
			}			
			break;
		case 1: //key is Integer type
			k_v=new HashMap<Integer, Double> ();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
				String strLine;
				while((strLine=br.readLine())!=null){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 2) {
//						we very simply check this is a decimal number
						if (!units[1].matches("^[0-9]*(\\.[0-9]+)?$")) {
							continue;
						}
						k_v.put(Integer.parseInt(units[0]), Double.valueOf(units[1]));
					}				
				}				
				br.close();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
//				this exception is usually "String-like" values, so we just discard it
				e.printStackTrace();
			}	
			break;
		case 2:  //value is string type
			k_v=new HashMap<String,String> ();
			
		
		default:
			System.out.println("There is something wrong with the code!");
		}
		return k_v;	
	}

	/***
	 * keytype, valuetype: 0=Double
	 *          1=Integer
	 *          2=String
	 * 
	 * @param filePath
	 * @param keyType
	 * @param valueType
	 * @return
	 */
	public static HashMap readFileToHash (String filePath, int keyType, int valueType) {
		
		HashMap k_v=null;
		
		if ((keyType==2) && (valueType==2)) {
			k_v=new HashMap<String,String> ();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
				String strLine;
				while((strLine=br.readLine())!=null){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 2) {
						k_v.put(units[0], units[1]);
					}
						
				}
				br.close();
			}catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
//				this exception is usually "String-like" values, so we just discard it
				e.printStackTrace();
			}			
		}
		
		return k_v;
	}
	
	
	public static void writeFile(String filePath,String content){
		try{
			FileWriter fw=new FileWriter(filePath);
			BufferedWriter bw=new BufferedWriter(fw);
			bw.write(content);
			bw.close();
			fw.close();
		}catch (Exception e){
			System.err.println("Error: "+e.getMessage());
		}
	}
	
	/***
	 * the output style is
	 * key\tvalue
	 * 
	 * @param hm
	 * @param outDir
	 */
	public static void writeHashMapToFile (HashMap hm, String outDir) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outDir));
//			first check if the hashmap is a null or not: if so, then write an empty string to the output file
			if (hm == null) {
				bw.write("");
				System.out.println("The file hash is empty");
				return;
			}
			for (Iterator iter=hm.keySet().iterator(); iter.hasNext(); ){
				Object key=(Object) iter.next();
				bw.write(key.toString()+"\t"+hm.get(key).toString()+"\r\n");
			}
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Errors in writing hashmap to output directory!");
		}
	}
	
	public static void writeHashMapOrderByValue (HashMap hm, String outDir) {
		String out="";
		if (hm != null) {
			Map sorted = sortByValues (hm);
			for (Iterator iter=sorted.keySet().iterator(); iter.hasNext(); ) {
				Object k=(Object) iter.next();
				out+=k.toString()+"\t"+hm.get(k).toString()+"\n";
			}
		}
		writeFile(outDir, out);
	}
	
	public static void writeHashMapOrderByValueForSynset (HashMap hm, String outDir) {
		String out="";
		if (hm != null) {
			Map sorted = sortByValues (hm);
			for (Iterator iter=sorted.keySet().iterator(); iter.hasNext(); ) {
				ISynset k=(ISynset) iter.next();
				out+=k.getID().toString()+"\t"+hm.get(k).toString()+"\n";
			}
		}
		writeFile(outDir, out);
	}
	
	
	public static int countFileLines(String filePath){
		int count=0;
		BufferedReader br;
		try {
			br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
			String strLine;
			while((strLine=br.readLine())!=null){
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}
	
	public static String readSpecificLine(String filePath,int lineNum){
		int count=1;
		BufferedReader br;
		String strLine="";
		try {
			br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
			while((strLine=br.readLine())!=null&&count<lineNum){
				count++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return strLine;
	}
	
	/***
	 * sort a hashmap by value
	 * 
	 * @param map
	 * @return
	 */
	static 	public <K,V extends Comparable<V>> Map<K,V> sortByValues (final Map<K,V> map) {
		Comparator<K> valueComparator =  new Comparator<K>() {
	        public int compare(K k1, K k2) {
	            int compare = map.get(k2).compareTo(map.get(k1));
	            if (compare == 0) return 1;
	            else return compare;
	        }
	    };
	    Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
	    sortedByValues.putAll(map);
	    return sortedByValues;
	}
	
	
	public static void main(String[] args) throws Exception{
//		System.out.println("this is the file content:");
//		String doc=getFileAsString("C:\\PoliticalData\\109-reps-matched\\109_ackerman_x_ny.txt");
//		writeFile("C:\\PoliticalData\\output\\109_ackerman_x_ny.txt",doc);
//		writeFile("C:\\writefile.txt","1 ");
//		try {
//			FileWriter fw=new FileWriter("C:\\writefile.txt",true);
//			BufferedWriter bw=new BufferedWriter(fw);
//			bw.write("2 3 ");
//			bw.close();
//			System.out.println("finished");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println(countFileLines("C:\\Projects\\TextProcessor\\output.arff"));
		File afile=new File("/Users/miaochen/Documents/diss-experiment/proc-corpus/bow-vec/098/super/train0/2");
		System.out.println(getFileObjectAsString(afile));
	}

}
