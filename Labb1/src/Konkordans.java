import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;


public class Konkordans {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

	}
	
	public static boolean createIndex() throws FileNotFoundException, UnsupportedEncodingException{

	}
		public static booelan createHash(){
			
		
		Scanner in = new Scanner(new File("/var/tmp/elincl_ut"), "ISO-8859-1");
		PrintWriter writer = new PrintWriter("A.txt", "UTF-8");
		String lastWord = "";
		String line = "";
		while(in.hasNextLine()){
			line = in.nextLine();
			String[] shortWord = line.split(" ");
			String newWord;
			if(shortWord[0].length() < 3)
				newWord = line.substring(0, shortWord[0].length());
			else
				newWord = line.substring(0, 3);
			if(!newWord.equals(lastWord)){
				String[] lineNr = line.split(" ");
				String a = newWord + " " + lineNr[1];
				writer.println(a);
				lastWord = newWord;
			}
		}
		writer.close();
		return true;
		}


}

