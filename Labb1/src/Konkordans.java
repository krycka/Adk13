import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * 
 * @author elincl
 * Att kontrollera innan inlämning:
 * - Att den klarar av det sista ordet i varje index
 * - sista ordet i korpus
 * - Binärsökning?
 * - Städa
 * - Åtgärdat? Lägg in antal träffar
 * - Åtgärdat? åäö-stöd
 * - kontrollera case insensitive
 * - lägg in om det är många resultat för en sökning (Visa alla?)
 * - spara res mot korpus i länkad lista
 * - Två longs per bokstavskomb i bucket? dvs från offset, till offset.
 * 
 */


public class Konkordans {
	private static File korpus;
	private static File tokfile;
	private static File bucket;
	private static File wordIndex;
	private static Time searchTime;
	private static int resultsLimit = 25;
	private static final boolean DBG = true;

	public static void main(String[] args) throws IOException, InterruptedException {
		// Kontrollera att vi har fått ett ord att söka efter
		if(args.length == 0) {	System.out.println("Du måste skriva ett ord för att söka."); 	System.exit(-1); }
		if(args.length > 1) {	System.out.println("Du kan bara söka efter ett ord åt gången");	System.exit(-1); }

		String searchWord = args[0].toLowerCase();
		
		// Initiera filerna vi behöver
		korpus = new File("/info/adk13/labb1/korpus");
		tokfile = new File("/var/tmp/tokfile");
		bucket = new File("/var/tmp/bucket");
		wordIndex = new File("/var/tmp/wordIndex");
		
		// Kontrollera att indexfilerna, skapa dom om någon saknas.
		if(!checkIndexes()) {
			System.out.println("En eller fler filer saknas, genererar nya filer...");
			createIndexes();
		}		
		// Nu är vi garanterade att ha de filer vi behöver
		searchTime = new Time();
		searchTime.start();
		
		// Hämta position att söka på i wordIndex från bucket
		RandomAccessFile raf = new RandomAccessFile(bucket, "r");
		raf.seek(getBucketOffset(searchWord));
		long wordIndexPos = raf.readLong();
		long wordIndexNextPos = raf.readLong();
		for(int i=0; i<100 && wordIndexNextPos == 0; i++) 
			wordIndexNextPos = raf.readLong();
		raf.close();
		
		// Hämta offset för ordet i tokfile från wordIndex
		raf = new RandomAccessFile(wordIndex, "r");
		raf.seek(wordIndexPos);
		long tokfilePos = 0;
		long tokfileNextPos = 0;
		int wordCount = 0;
		boolean found = false;
		
		for(long i=wordIndexPos; i<wordIndexNextPos && !found; i=raf.getFilePointer()) {
			String line[] = raf.readLine().split(" ");
			if(line[0].equals(searchWord)) {
				// Hämta offset i tokfile
				tokfilePos = Long.parseLong(line[1]);
				wordCount = Integer.parseInt(line[2]);
				System.out.println(wordCount);
				// Hämta offset till nästa ord
				line = raf.readLine().split(" ");
				tokfileNextPos = Long.parseLong(line[1]);
				found = true;
			}
		}
		raf.close();
		
		// Hämta alla förekomster av ordet från tokfile och spara deras offsets
		LinkedList<Long> results = new LinkedList<Long>();
		raf = new RandomAccessFile(tokfile, "r");
		raf.seek(tokfilePos);
		for(long i=tokfilePos; i<tokfileNextPos; i=raf.getFilePointer()) {
			String line[] = raf.readLine().split(" ");
			results.add(Long.parseLong(line[1]));
		}
		raf.close();
		
		System.out.println();
		System.out.println("Hittade "+results.size()+" förekomster av ordet: \""+searchWord+"\". Sökningen tog "+(float)searchTime.stop()/1000+"s.");
		if(results.size() > resultsLimit) {
			System.out.println("Visa alla? [j/n]");
			Scanner in = new Scanner(System.in);
			if(in.nextLine().equals("j")) 
				resultsLimit = results.size();
			in.close();
		} else {
			resultsLimit = results.size();
		}
		System.out.println();
		
		// Skriv ut från korpus!
		raf = new RandomAccessFile(korpus, "r");
		byte[] b = new byte[30+searchWord.length()+30];
		
		for(int i=0; i<resultsLimit; i++) {
			raf.seek(results.poll()-30);
			raf.read(b);
			
			System.out.println(i+1+": "+new String(trimOutput(b), "ISO-8859-1"));
		}
		System.out.println();
		raf.close();
	}

	private static boolean checkIndexes() {
		if(tokfile.exists() && bucket.exists() && wordIndex.exists())
			return true;
		return false;

	}


	private static boolean createIndexes() throws IOException, InterruptedException{
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
	
	private static boolean createBucket() throws IOException{
		System.out.println("Creating bucket index...");
		Scanner in = new Scanner(wordIndex, "ISO-8859-1");

		RandomAccessFile out = new RandomAccessFile(bucket, "rw");
		out.setLength(195120); //29*29*29 * 8 + 1*8. För att a=1, därför används inte index 0. 

		String lastWord = "";
		String line = "";
		long pos = 0;
		while(in.hasNextLine()){
			line = in.nextLine();
			String[] lineSplit = line.split(" ");
			String newWord = lineSplit[0];

			if(lineSplit[0].length() > 3)
				newWord = newWord.substring(0, 3);

			if(!newWord.equals(lastWord)){				
				out.seek(getBucketOffset(newWord));
				out.writeLong(pos);
				System.out.println("word: "+newWord+" seek: "+getBucketOffset(newWord)+" pos: "+pos);
				lastWord = newWord;
			}
			pos += line.length()+1;
		}
		out.close();
		in.close();
		return true;
	}

	private static boolean createWordIndex() throws FileNotFoundException, UnsupportedEncodingException{
		System.out.println("Creating wordIndex...");
		
		Scanner in = new Scanner(tokfile, "ISO-8859-1");
		PrintWriter writer = new PrintWriter(wordIndex, "ISO-8859-1");
		
		String lastWord = "";
		String line = "";
		long pos = 0;
		int count = 0;
		
		while(in.hasNextLine()) {
			line = in.nextLine();
			String[] shortWord = line.split(" ");
			String newWord = shortWord[0];
			if(!newWord.equals(lastWord)){ //om senast skrivna ordet inte är lika med det aktuella ordet, skriv det till filen. 
				String a = newWord + " " + pos + " " + count;
				System.out.println(a);
				writer.println(a);
				lastWord = newWord;
				count = 0;
			}
			count++;
			pos += line.length()+1;
		}
		writer.close();
		in.close();

		return true;
	}
	

	private static long getBucketOffset(String word){
		long pos = 0;
		if(word.length() > 3) word = word.substring(0, 3);
		char[] letters = word.toLowerCase().toCharArray();

		for(int i=0; i < letters.length; i++){
			if((int)letters[i] >= 97 && (int)letters[i] <= 122) 
				pos += ((int)letters[i]-96)*Math.pow(29, 2-i);
			else if((int)letters[i] == 229)
				pos += 27*Math.pow(29, 2-i); // å Ascii 134 UTF-8 229
			else if((int)letters[i] == 228)
				pos += 28*Math.pow(29, 2-i); // ä Ascii 132 UTF-8 228
			else if((int)letters[i] == 246)
				pos += 29*Math.pow(29, 2-i); // ö Ascii 148 UTF-8 246
		}
		return pos*8;
	}
	/**
	 * Removes newlines, carriage returns and tabs and replaces them with a space.
	 * @param b
	 * @return b 
	 */
	private static byte[] trimOutput(byte[] b) {
		for(int i=0; i<b.length; i++) {
			if(b[i]==10 || b[i]==13 || b[i] == 9)
				b[i]=32;
		}
		return b;
	}

}
