import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;


public class Konkordans {
	private static File tokfile;
	private static File bucket;
	private static File wordIndex;
	private static final boolean DBG = true;


	public static void main(String[] args) throws IOException {
		if(args.length == 0) System.out.println("Du måste skriva ett ord för att söka.");
		if(args.length > 1) System.out.println("Du kan bara söka efter ett ord åt gången");

		if(!checkIndexes()) {
			createIndexes();
		}
		
		//search
		

	}

	private static boolean checkIndexes() {
		tokfile = new File("/var/tmp/elincl_ut");
		bucket = new File("bucket");
		wordIndex = new File("wordIndex");

		if(tokfile.exists() && bucket.exists() && wordIndex.exists())
			return true;
		return false;

	}


	public static boolean createIndexes() throws IOException{
		//		createBucket();
		createWordIndex();
		createBucket();
		return true;

	}
	public static boolean createBucket() throws IOException{
		System.out.println("Creating bucket index...");
		Scanner in = new Scanner(wordIndex, "UTF-8");

		RandomAccessFile out = new RandomAccessFile(bucket, "rw");
		out.setLength(195120); //29*29*29 * 8 + 1*8. För att a=1, därför används inte index 0. 

		String lastWord = "";
		String line = "";
		Long pos = new Long(0);
		while(in.hasNextLine()){
			line = in.nextLine();
			pos += line.length()+1;
			String[] lineSplit = line.split(" ");
			String newWord = "";

			//			if(lineSplit[0].length() < 3)
			//				newWord = line.substring(0, lineSplit[0].length());
			if(lineSplit[0].length() > 3)
				newWord = line.substring(0, 3);

			if(!newWord.equals(lastWord)){				
				out.seek(getBucketPosition(newWord));
				out.writeLong(pos);
				lastWord = newWord;
			}
		}
		out.close();
		return true;
	}

	public static boolean createWordIndex() throws FileNotFoundException, UnsupportedEncodingException{
		System.out.println("Creating wordIndex...");
		Scanner in = new Scanner(new File("/var/tmp/elincl_ut"), "ISO-8859-1");
		PrintWriter writer = new PrintWriter(wordIndex, "UTF-8");
		String lastWord = "";
		String line = "";
		long pos = 0;
		while(in.hasNextLine()){
			line = in.nextLine();
			pos += line.length()+1;
			String[] shortWord = line.split(" ");
			String newWord = shortWord[0];
			if(!newWord.equals(lastWord)){ //om senast skrivna ordet inte är lika med det aktuella ordet, skriv det till filen. 
				String a = newWord + " " + pos;
				writer.println(a);
				lastWord = newWord;
			}
		}
		writer.close();

		return true;
	}

	private static long getBucketPosition(String word){
		long off = 0;
		char[] letters = word.toLowerCase().toCharArray();

		/*
		 * å = 134
		 * ä = 132
		 * ö = 148
		 */

		//97-122 = a-z
		for(int i = 0; i < letters.length; i++){
			
			if((int)letters[i] >= 97 && (int)letters[i] <= 122) 
				off += ((int)letters[0]-96)*Math.pow(29, i);

			else if((int)letters[i] == 134)
				off += ((int)letters[i]-(108))*Math.pow(29, i); //134-27+1
			else if((int)letters[i] == 132)
				off += ((int)letters[i]-(105))*Math.pow(29, i); //132-28+1
			else if((int)letters[i] == 148)
				off += ((int)letters[i]-(120))*Math.pow(29, i); //148-29+1

		}

		return off;

	}

}
