package common;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class config {
	
	//Return DB conntction and config HashMap
	static public Connection connection;
	static public Statement statement;
	static public HashMap configmap = new HashMap();
	
	static public void loadconfig (String path) throws Exception {
		// PASS LOCATION OF CONFIG FILE AS A PARAMETER FROM MAIN()
		String content = fileoperator.readfile(path);
		Matcher ParameternMatcher = Pattern.compile("<Parameter name=[^>]*></Parameter>").matcher(content);
		while (ParameternMatcher.find()) {
			//	<Parameter name="DB_USER" value="mysql"></Parameter>
			String parameter = ParameternMatcher.group();
			//System.out.println(parameter);
			String name = parameter.substring(parameter.indexOf("name=")+ 6, parameter.indexOf(" value=") - 1);
			String value = parameter.substring(parameter.indexOf("value=")+ 7, parameter.indexOf("></Parameter>")-1);
			System.out.println("Name: " + name + " \t Value: " + value);
			configmap.put(name, value);
		}
		connection = new DBconnect(configmap.get("DB_CONNECTION").toString(), configmap.get("DB_USER").toString(), configmap.get("DB_PASSWORD").toString()).connection;
		statement = connection.createStatement();
	}
}


/*
			static public DBconnect db = null;
			static public String DBconnection = "";
			static public String DBuser = "";
			static public String DBpassword = "";
			static public int k;
			static public double alpha;
			static public String subprotagfile;
			static public Connection connection;
			static public int inferencedays;    
		
		String content = fileoperator.readfile("C:\\config.xml");
		System.out.println(content);
		Matcher DBconnectionMatcher = Pattern.compile("<DBconnection>.*</DBconnection>").matcher(content);
		DBconnectionMatcher.find();
		DBconnection = DBconnectionMatcher.group().substring(
				DBconnectionMatcher.group().indexOf(">")+1, DBconnectionMatcher.group().indexOf("</")-1);
		Matcher DBuserMatcher = Pattern.compile("<DBuser>.*</DBuser>").matcher(content);
		DBuserMatcher.find();
		DBuser = DBuserMatcher.group().substring(
				DBuserMatcher.group().indexOf(">")+1, DBuserMatcher.group().indexOf("</")-1);
		Matcher DBpasswordMatcher = Pattern.compile("<DBpassword>.*</DBpassword>").matcher(content);
		DBpasswordMatcher.find();
		DBpassword = DBpasswordMatcher.group().substring(
				DBpasswordMatcher.group().indexOf(">")+1, DBpasswordMatcher.group().indexOf("</")-1);
		Matcher kMatcher = Pattern.compile("<LDA-k>.*</LDA-k>").matcher(content);
		kMatcher.find();
		k = new Integer(kMatcher.group().substring(
				kMatcher.group().indexOf(">")+1, kMatcher.group().indexOf("</")-1)).intValue();
		Matcher alphaMatcher = Pattern.compile("<LDA-alpha>.*</LDA-alpha>").matcher(content);
		alphaMatcher.find();
		alpha = new Double(alphaMatcher.group().substring(
				alphaMatcher.group().indexOf(">")+1, alphaMatcher.group().indexOf("</")-1)).doubleValue();
		Matcher subprotagfileMatcher = Pattern.compile("<subprotag-file>.*</subprotag-file>").matcher(content);
		subprotagfileMatcher.find();
		subprotagfile = subprotagfileMatcher.group().substring(
				subprotagfileMatcher.group().indexOf(">")+1, subprotagfileMatcher.group().indexOf("</")-1);
		Matcher inferencedaysMatcher = Pattern.compile("<inferencedays>.*</inferencedays>").matcher(content);
		inferencedaysMatcher.find();
		inferencedays = new Integer(inferencedaysMatcher.group().substring(
				inferencedaysMatcher.group().indexOf(">")+1, inferencedaysMatcher.group().indexOf("</")-1)).intValue();
		connection = new DBconnect(DBconnection, DBuser, DBpassword).connection;

*/