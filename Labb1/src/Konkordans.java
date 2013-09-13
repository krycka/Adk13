import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * 
 * @author elincl
 * Att kontrollera innan inlämning:
 * - Att den klarar av det sista ordet i varje index
 * - sista ordet i korpus
 * - Binärsökning?
 * - Vaginalfantasi != vagina
 * - Städa
 * - Lägg in antal träffar
 * - åäö-stöd
 * - kontrollera case insensitive
 * - lägg in om det är många resultat för en sökning (Visa alla?)
 * - spara res mot korpus i länkad lista
 */


public class Konkordans {
	private static File korpus;
	private static File tokfile;
	private static File bucket;
	private static File wordIndex;
	private static final boolean DBG = true;
	private static Time searchTime;

	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length == 0) System.out.println("Du måste skriva ett ord för att söka.");
		if(args.length > 1) System.out.println("Du kan bara söka efter ett ord åt gången");

		if(!checkIndexes()) {
			System.out.println("En eller fler filer saknas, genererar nya filer...");
			createIndexes();
		}
		
		// Indexer finns garanterat nu! Fortsätt med sökningen.
		searchTime = new Time();
		searchTime.start();
		
		// Hämta position i wordIndex från bucket
		RandomAccessFile raf = new RandomAccessFile(bucket, "r");
		raf.seek(getBucketPosition(args[0]));
		long wordIndexPos = raf.readLong();
		long wordIndexNextPos = wordIndexPos+8;
		long tokfilePos = 0;
		long tokfileNextPos = 0;
		
		// Hämta offsett för ordet i tokfile
		raf = new RandomAccessFile(wordIndex, "r");
		boolean found = false;
		for(long i=wordIndexPos; i<wordIndexNextPos && !found; i=raf.getFilePointer()) {
			String line[] = raf.readLine().split(" ");
			if(line[0].equals(args[0])) {
				// Hämta offset i tokfile
				tokfilePos = Long.parseLong(line[1]);
				// Hämta offset till nästa ord
				line = raf.readLine().split(" ");
				tokfileNextPos = Long.parseLong(line[1]);
				found = true;
			}
		}
		
		System.out.println(searchTime.stop());
		// Hämta alla offsets för ett ord i tokfile till korpus
		raf = new RandomAccessFile(tokfile, "r");
		RandomAccessFile rafKorpus = new RandomAccessFile(korpus, "r");
		raf.seek(tokfilePos);
		
		for(long i=tokfilePos; i<tokfileNextPos; i=raf.getFilePointer()) {
			String line[] = raf.readLine().split(" ");
			Long posKorpus = Long.parseLong(line[1]);
			byte[] b = new byte[30+args[0].length()+30];
			rafKorpus.seek(posKorpus-30);
			rafKorpus.read(b);
			
			System.out.println(new String(b));
		}
		
		

	}

	private static boolean checkIndexes() {
		korpus = new File("/info/adk13/labb1/korpus");
		tokfile = new File("/var/tmp/tokfile");
		bucket = new File("/var/tmp/bucket");
		wordIndex = new File("/var/tmp/wordIndex");

		if(tokfile.exists() && bucket.exists() && wordIndex.exists())
			return true;
		return false;

	}


	public static boolean createIndexes() throws IOException, InterruptedException{
		//		createBucket();
//		createTokfile();
		createWordIndex();
		createBucket();
		
		return true;

	}
	
	private static boolean createTokfile() throws IOException, InterruptedException{
		System.out.println("Creating tokfile...");
		Process p;
		p= Runtime.getRuntime().exec("/info/adk13/labb1/tokenizer < /info/adk13/labb1/korpus | sort > /var/tmp/tokfile");
		p.waitFor();
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
		Scanner in = new Scanner(tokfile, "ISO-8859-1");
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
		if(word.length() > 3) word = word.substring(0, 3);
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
