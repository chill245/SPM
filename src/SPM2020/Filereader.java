package SPM2020;

import java.io.*;
import java.util.*;

public class Filereader {
	
	static String filename = "C:\\SPM\\kd100.csv";
	
	public static void main(String[] args) {
		File kdFile = new File(filename);
		String zeile;
		try {
			BufferedReader dataIn = new BufferedReader(new FileReader(kdFile));
			int lineNumber = 0;
			while((zeile = dataIn.readLine()) != null) {
				lineNumber++;
				System.out.println("Zeile " + lineNumber + ": "+zeile);
				String[] test = zeile.split(",");
				for(String s:test) {
					System.out.println(s);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
