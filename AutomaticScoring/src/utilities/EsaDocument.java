package utilities;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import com.common.contants.ESAConstants;
import com.esa.api.concept.IConceptVector;
import com.esa.search.ESASearcher;


public class EsaDocument {
	
	/***
	 * the document is actually a text document, and will be changed to ESA vectors
	 * maybe, this class should be combined with the Document class, but for now,
	 * I separete them to have two relatively smaller class
	 * 
	 * @param args
	 */
	
	String text;
//	the hashmap for bag of words, word => frequency
	HashMap<String,Integer> wiki_esa;
	
	
	EsaDocument (){
		
	}
	
	EsaDocument (String text){
//		NOTE: here we replace spaces/tab/line break with a single " "
		this.text=text.replaceAll("\\s+", " ");		
	}
	
	EsaDocument (File afile){
//		NOTE: here we replace spaces/tab/line break with a single " "
		this.text=FileIO.getFileObjectAsString(afile).replaceAll("\\s+", " ");
//		System.out.println(text);
	}
	
	IConceptVector toEsaVector (ESASearcher searcher) {
		IConceptVector esaVec=null;
		
		try {
			esaVec = searcher.getConceptVectorFromText(text);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return esaVec;
	}
	
	public static void main (String[] args) throws SQLException, IOException {
		String fpath="somepath";
		EsaDocument esadoc=new EsaDocument(new File(fpath));
		
		ESASearcher searcher = new ESASearcher(ESAConstants.page_searcher);
		IConceptVector cvBase = searcher.getConceptVectorFromText(esadoc.text);
		
		searcher.getRelatednessBetweenFile(esadoc.text, esadoc.text);
	}

}
