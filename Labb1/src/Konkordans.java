import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;


public class Konkordans {
	private static File tokfile;
	private static File bucket;
	private static File wordIndex;
	

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		if(args.length == 0) System.out.println("Du måste skriva ett ord för att söka.");
		if(args.length > 1) System.out.println("Du kan bara söka efter ett ord åt gången");
		
		if(!checkIndexes()) {
			createIndexes();
		}

	}
	
	private static boolean checkIndexes() {
		tokfile = new File("tokfile");
		bucket = new File("bucket");
		wordIndex = new File("wordIndex");
		
		if(tokfile.exists() && bucket.exists() && wordIndex.exists())
			return true;
		return false;
		
	}

	
	public static boolean createIndexes() throws FileNotFoundException, UnsupportedEncodingException{
//		createBucket();
		createWordIndex();
		return true;

	}
		public static boolean createBucket() throws FileNotFoundException, UnsupportedEncodingException{

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
		
		public static boolean createWordIndex() throws FileNotFoundException, UnsupportedEncodingException{
			
			Scanner in = new Scanner(new File("/var/tmp/elincl_ut"), "ISO-8859-1");
			PrintWriter writer = new PrintWriter("WordIndex.txt", "UTF-8");
			String lastWord = "";
			String line = "";
			int pos = 0;
			while(in.hasNextLine()){
				line = in.nextLine();
				pos += line.length()+1;
				String[] shortWord = line.split(" ");
				String newWord = "";
				if(!newWord.equals(lastWord)){
					String[] lineNr = line.split(" ");
					String a = newWord + " " + pos + "\n";
					writer.println(a);
					lastWord = newWord;
				}
			}
			writer.close();
			
			return true;
		}

	

}
