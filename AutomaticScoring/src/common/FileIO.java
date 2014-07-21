package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class FileIO {
	
	static String readText(String path) {
		StringBuffer sb = new StringBuffer("");
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));
			String strLine;
			while ((strLine = br.readLine()) != null) {
				sb.append(strLine);
				sb.append("\r\n");
			}
			br.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
		return sb.toString();
	}

	static void writeText(String outDir, String content) {
		BufferedWriter wr = null;
		try {// deal with attributes part
			wr = new BufferedWriter(new FileWriter(new File(outDir)));
			wr.write(content);
			wr.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String path = "C:\\Projects\\BreastCancer\\742241.chunk";
		System.out.println(FileIO.readText(path));
	}

}
