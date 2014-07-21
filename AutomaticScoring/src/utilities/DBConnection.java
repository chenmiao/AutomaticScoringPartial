package utilities;


import java.sql.*;
//import javax.sql.*;

import edu.mit.jwi.item.POS;


public class DBConnection {
	
	Connection con=null;
	
	DBConnection (){
		
	}
	
	DBConnection (String dbUrl, String dbClass, String usr, String pwd) {
		
		try {
			Class.forName(dbClass);
			con = DriverManager.getConnection (dbUrl, usr, pwd);
			
			if (con==null){
				System.out.println("Failed to connect to the database!");
			}else{
				System.out.println("Successfully connected to the database!");
			}
		}catch (Exception e){
			System.out.println("Failed to connect to the database!");
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	void close (){
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/***
	 * given a string, find if it appears in Wikipedia and return its page ID
	 * 
	 * @param inString
	 */
	int findWikiConceptID (String inString) {
		
		int pageID=-1;
		
		try {
			Statement Stmt = con.createStatement();		
			ResultSet RS = Stmt.executeQuery("SELECT pageId FROM `wikiData`.`Page` " +
					"where isDisambiguation=0 and name=\'"+processStringForMysqlWiki(inString)+"\'");
			
			while (RS.next()) {
				pageID=RS.getInt(1);	
           }
			RS.close();
			Stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return pageID;
		
		
	}
	
    WikiPage findWikiConcept (String inString) {
		
		int pageID=-1;
		
		try {
			Statement Stmt = con.createStatement();		
			ResultSet RS = Stmt.executeQuery("SELECT pageId FROM `wikiData`.`Page` " +
					"where isDisambiguation=0 and name=\'"+processStringForMysqlWiki(inString)+"\'");
			
			while (RS.next()) {
				pageID=RS.getInt(1);
           }
			
			RS.close();
			Stmt.close();			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new WikiPage(pageID,inString);
				
	}
    
    String getWikiPageContentGivenID (int id) {
    	String content="";
    	
    	try {   		
    		Statement Stmt = con.createStatement();	
    		ResultSet RS = Stmt.executeQuery("SELECT text FROM `wikiData`.`Page` where pageid="+id);
    		while (RS.next()) {
    			content=RS.getString(1);
    		}
    		RS.close();
			Stmt.close();  		
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	
    	return content;
    }
    
    
    POS findPosOfFirstSenseInWordnetGivenAWord (String word) {
    	POS pos=null;
    	String posString=null;
    	
    	try {
    		Statement Stmt = con.createStatement();		
    		ResultSet RS=Stmt.executeQuery("select pos from `wordnet`.`senses`, `wordnet`.`synsets`, `wordnet`.`words` " +
    				"where synsets.synsetid = senses.synsetid and lemma = \'"+processStringForMysql(word)+"\' " +
    						"and senses.wordid = words.wordid order by tagcount desc");
//    		get the first result row, which is the most frequently used sense for this word
    		while (RS.next()&&RS.isFirst()) {
    			posString=RS.getString(1);
    		}
    		
    		RS.close();
    		Stmt.close();
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if (posString==null) {
//    		there is no match from sql query
    		return null;
    	}
    	
    	if (posString.equalsIgnoreCase("n")){
    		pos=POS.NOUN;
    	}else if (posString.equalsIgnoreCase("v")){
    		pos=POS.VERB;
    	}else if (posString.equalsIgnoreCase("a")||posString.equalsIgnoreCase("s")){
    		pos=POS.ADJECTIVE;
    	}else if (posString.equalsIgnoreCase("r")) {
    		pos=POS.ADVERB;
    	}
    	
    	return pos;
    }
	
	/***
	 * it process an input string due to the mysql and wikipedia convention:
	 * 1) escape ' as \'
	 * 2) replace " " as "_"
	 * for example, O'Reilly Media is processed to O\'Reilly_Media
	 * 
	 * @param in
	 * @return
	 */
	String processStringForMysqlWiki (String in){
		
		return in.replaceAll("'", "\\\\'").replaceAll("\\s+", "_");		
		
	}
	
	/***
	 * it process an input string due to the mysql convention
	 * 1) escape ' as \'
	 * 
	 * @param in
	 * @return
	 */
	String processStringForMysql (String in){
		
		return in.replaceAll("'", "\\\\'");		
		
	}	
	
	
	public static void main (String[] args){
		
//		String dbUrl = "jdbc:mysql://localhost:3306/test";
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbCon=new DBConnection(dbUrl,dbClass,usr,pwd);
		System.out.println(dbCon.con!=null);

//		System.out.println(dbCon.findWikiConcept("went").id);
		System.out.println(dbCon.findPosOfFirstSenseInWordnetGivenAWord("student"));
		
		dbCon.close();

		
	}

}
