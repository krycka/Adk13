import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * 
 * @author elincl
 * Att kontrollera innan inlämning:
 * - Städa
 */

public class Konkordans {
	private static File korpus;
	private static File tokfile;
	private static File bucket;
	private static File wordIndex;
	private static StopWatch stopWatch;
	private static int resultsLimit = 25;

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
		stopWatch = new StopWatch();
		stopWatch.start();
		
		// Hämta position att söka på i wordIndex från bucket
		RandomAccessFile raf = new RandomAccessFile(bucket, "r");
		raf.seek(getBucketOffset(searchWord));
		long wordIndexPos = raf.readLong();		
		long wordIndexNextPos = 0;
		
		// Kontrollera att vi fẗt en giltig position, annars finns ej ordet.
		if(wordIndexPos == 0) {
			wordIndexNextPos = -1;
			//			System.out.println("Ordet finns ej..");
			//			System.exit(1);
		} else {
			// Fick en giltig pos, hämta nästa ord åxå.
			try {
				wordIndexNextPos = raf.readLong();
				for(int i=0; i<100 && wordIndexNextPos == 0; i++) 
					wordIndexNextPos = raf.readLong();
			} catch(EOFException e) {
				wordIndexNextPos = wordIndex.length();
			}
		}
		raf.close();

		// Hämta offset för ordet i tokfile från wordIndex
		raf = new RandomAccessFile(wordIndex, "r");
		raf.seek(wordIndexPos);
		long tokfilePos = 0;
//		long tokfileNextPos = 0;
		int wordCount = 0;
		boolean found = false;
		
		for(long i=wordIndexPos; i<wordIndexNextPos && !found; i=raf.getFilePointer()) {
			String line[] = raf.readLine().split(" ");
			if(line[0].equals(searchWord)) {
				// Hämta offset i tokfile
				tokfilePos = Long.parseLong(line[1]);
				wordCount = Integer.parseInt(line[2]);
				found = true; // break; istallet?
			}
		}
		raf.close();
		
		System.out.println();
		System.out.println("Hittade "+wordCount+" förekomster av ordet: \""+searchWord+"\". Sökningen tog "+(float)stopWatch.stop()/1000+"s.");
		if(wordCount > resultsLimit) {
			System.out.println("Visa alla? [j/n]");
			Scanner in = new Scanner(System.in);
			if(in.nextLine().equals("j")) 
				resultsLimit = wordCount;
			in.close();
		} else {
			resultsLimit = wordCount;
		}
		System.out.println();
		stopWatch.start();
		
		// Skriv ut från korpus!
		RandomAccessFile rafTokfile = new RandomAccessFile(tokfile, "r");
		rafTokfile.seek(tokfilePos);
		RandomAccessFile rafKorpus = new RandomAccessFile(korpus, "r");
		byte[] b = new byte[30+searchWord.length()+30];
		
		for(int i=0; i<resultsLimit; i++) {
			String line[] = rafTokfile.readLine().split(" ");
			rafKorpus.seek(Long.parseLong(line[1])-30);
			rafKorpus.read(b);
			
			System.out.println(i+1+":\t "+new String(trimOutput(b), "ISO-8859-1"));
		}
		System.out.println();
		System.out.println("Utskrift av resultatet tog: "+(float)stopWatch.stop()/1000+"s");
		raf.close();
		System.exit(0);
	}

	private static boolean checkIndexes() {
		if(tokfile.exists() && bucket.exists() && wordIndex.exists())
			return true;
		return false;
	}


	private static boolean createIndexes() throws IOException, InterruptedException{
		createTokfile();
		createWordIndex();
		createBucket();
		
		return true;

	}
	
	private static boolean createTokfile() throws IOException, InterruptedException{
//		System.out.println("Creating tokfile...");
		if(tokfile.exists())
			return true;
		System.out.println("Missing tokfile, run the following commands in the terminal:");
		System.out.println();
		System.out.println("export LC_COLLATE=C");
		System.out.println("/info/adk13/labb1/tokenizer < /info/adk13/labb1/korpus | sort > /var/tmp/tokfile\n");
		System.exit(1);
		
		return false;
//		Process p;
//		p= Runtime.getRuntime().exec("/info/adk13/labb1/tokenizer < /info/adk13/labb1/korpus | sort > /var/tmp/tokfile");
//		p.waitFor();
//		return true;
	}
	
	private static boolean createBucket() throws IOException{
		System.out.println("Creating bucket index...");
		Scanner in = new Scanner(wordIndex, "ISO-8859-1");

		RandomAccessFile out = new RandomAccessFile(bucket, "rw");
		out.setLength((30*30*30+1)*8); //30*30*30 * 8 + 1*8. För att a=1, därför används inte index 0. 

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
				System.out.println("word: "+newWord+" seek: "+getBucketOffset(newWord)+" pos: "+pos+" count:"+lineSplit[2]);
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
		
		String[] line = in.nextLine().split(" ");
		String lastWord = line[0], newWord = "";
		
		long pos = 0, prevPos=0;
		int count = 0;
		
		while(in.hasNextLine()) {
			line = in.nextLine().split(" ");
			newWord = line[0];
			
			count++;
			pos += (line[0].length()+line[1].length()+2); // +2 för space mellan ord och offsett samt newline på slutet
			
			if(!newWord.equals(lastWord)) { //om senast skrivna ordet inte är lika med det aktuella ordet, skriv det till filen.
				writer.println(lastWord + " " + prevPos + " " + count);
				lastWord = newWord;
				count = 0;
				prevPos = pos;
			}
			
		}
		// Skriv ut sista ordet, plus ett då vi inte kommer att räkna med den sista raden.
		writer.println(newWord + " " + prevPos + " " + (count+1));
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
				pos += ((int)letters[i]-96)*Math.pow(30, 2-i);
			else if((int)letters[i] == 229)
				pos += 27*Math.pow(30, 2-i); // å Ascii 134 UTF-8 229
			else if((int)letters[i] == 228)
				pos += 28*Math.pow(30, 2-i); // ä Ascii 132 UTF-8 228
			else if((int)letters[i] == 246)
				pos += 29*Math.pow(30, 2-i); // ö Ascii 148 UTF-8 246
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
