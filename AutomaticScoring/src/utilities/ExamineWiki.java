package utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;


public class ExamineWiki {
	
	static HashMap readEsaFileToHash (String filePath, int vType, int numLines) {
		HashMap k_v=null;
		int c=0; 
		
		switch (vType) {
		case 0: 
			k_v=new HashMap<String,Double>();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
				String strLine;
				while((strLine=br.readLine())!=null && c<numLines){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 3) {
						k_v.put(units[0], Double.valueOf(units[2]));
					}
					c++;
				}				
				br.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			break;
		case 1:
			k_v=new HashMap<Integer, Double> ();
			try {
				BufferedReader br=new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(filePath))));
//				System.out.println("this file pointer is null "+br==null);
				String strLine;
				while((strLine=br.readLine())!=null && c<numLines){
					String[] units=strLine.trim().split("\\t");
					if (units.length == 3) {
						k_v.put(Integer.parseInt(units[0]), Double.valueOf(units[2]));
					}
					c++;
				}				
				br.close();
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			System.out.println("There is something wrong with the code!");
		}
		return k_v;	
	}
	
	public static void main (String[] args) {
//		String wikifDir="/Users/miaochen/Documents/diss-experiment/proc-corpus/wikianno/vec/101/super/super2/3";
//		String esaDir="/Users/miaochen/Documents/diss-experiment/proc-corpus/esa/vec/101/super/super2/3";
//		
//		HashMap wikifmap=FileIO.readFileToHash(wikifDir, 1);
//		HashMap esamap=readEsaFileToHash(esaDir, 1, 1000);
//		
//		for (Iterator iter=wikifmap.keySet().iterator(); iter.hasNext(); ) {
//			Integer wikiid=(Integer) iter.next();
//			if (esamap.containsKey(wikiid)) {
//				System.out.println(wikiid.toString());
//				System.out.println("Wikification score is "+wikifmap.get(wikiid));
//				System.out.println("esa score is "+esamap.get(wikiid));
//			}
//		}
		
		String esa1="/Users/miaochen/Documents/diss-experiment/proc-corpus/esa/vec/101/super/super2/2";
		String esa2="/Users/miaochen/Documents/diss-experiment/proc-corpus/esa/vec/101/super/super2/4";
		
		HashMap map1=readEsaFileToHash(esa1, 1, 1000);
		HashMap map2=readEsaFileToHash(esa2, 1, 1000);
		int c=0;
		
		for (Iterator iter=map1.keySet().iterator(); iter.hasNext(); ) {
			Integer wikiid=(Integer) iter.next();
			if (map1.containsKey(wikiid)) {
				System.out.println(wikiid.toString());
				System.out.println("w in super3 is "+map1.get(wikiid));
				System.out.println("w in super4 is "+map2.get(wikiid));
				c++;
			}
		}
		System.out.println("the overlap num is "+c);
		
	}

}
