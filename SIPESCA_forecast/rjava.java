/*
http://stackoverflow.com/questions/8844451/calling-r-script-from-java

javac rjava.java
java rjava

*/

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class rjava {
 static String localPath = "/Users/pedro/java/rjava/";

 public static void main(String[] args) {

    try {
    
		String rScriptFileName = localPath+"miscript.R";
        Runtime.getRuntime().exec("/usr/bin/Rscript " + rScriptFileName);

        try {Thread.sleep(10000);}//make this thread sleep for 30 seconds while R creates the needed file
        catch (InterruptedException e) {e.printStackTrace();}
        
        
        //Step Four: Import data from R and put it into myDataArray's empty last column
        String matchFileName = localPath+"resultado.csv";
        BufferedReader br3 = new BufferedReader(new FileReader(matchFileName));
        String thisRow;
        int rowIndex = 0;
        while ((thisRow = br3.readLine()) != null) {
        	System.out.println( "Lin." + rowIndex + " -> " + "[" + thisRow + "]" );
            rowIndex += 1;
        }
        br3.close();
        
    }
    catch (FileNotFoundException e) {e.printStackTrace();}
    catch (IOException ie){ie.printStackTrace();}
 }

}
