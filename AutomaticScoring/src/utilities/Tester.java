package utilities;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Tester {
	
	public static void main (String[] args){
		
		String dbUrl = "jdbc:mysql://rdc04.uits.iu.edu:3264/test";
		String dbClass = "com.mysql.jdbc.Driver";
		String usr="miao";		
		String pwd=PasswordField.readPassword("Enter password: ");
		System.out.println("Finished entering password");
		DBConnection dbCon=new DBConnection(dbUrl,dbClass,usr,pwd);
		System.out.println(dbCon.con!=null);
		
		int sum=0;
		int n=0; //counter for all records
		int m=0; //counter for wiki title longer than 5 words
		
		try {
			Statement Stmt = dbCon.con.createStatement();	
//			first count number of records for wiki pages
			int numRecords=0;
			ResultSet RS1 = Stmt.executeQuery("SELECT count(*) FROM `wikiData`.`Page` " +
					"where isDisambiguation=0");
			while (RS1.next()){
				numRecords=RS1.getInt(1);
			}
			RS1.close();
			
//			fetch 50000 records in each round
			int getNum=50000;
			for (int k=0; k<numRecords/getNum+1; k++){
				ResultSet RS2 = Stmt.executeQuery("SELECT name FROM `wikiData`.`Page` " +
						"where isDisambiguation=0 LIMIT "+k*getNum+","+getNum);
				while (RS2.next()){
					System.out.println(n++);
					String name=RS2.getString(1);
					int len=name.split("_").length;
					if (len > 5){
						m++;
					}
					sum+=name.split("_").length;
				}
				RS2.close();				
			}
			
			Stmt.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		System.out.println("The average title length is "+ (float)sum/(float)n);
		System.out.println("The total number of wiki concepts longer than 5 words is "+m);
		System.out.println("Sum of length is "+sum);
				
	}

}
