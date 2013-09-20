import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 * Konkordans - Söker efter alla förekomster av ett ord i korpus.txt
 * @author Christoffer Rödöö & Elin Clemmedsson
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
			if(createIndexes())
				System.out.println("Generering klar!");
			else {
				System.out.println("Något gick fel vid genereringen av indexes...");
				System.exit(1);
			}
		}		
		// Nu är vi garanterade att ha de filer vi behöver
		stopWatch = new StopWatch();
		stopWatch.start();
		
		RandomAccessFile rafBucket = new RandomAccessFile(bucket, "r");
		RandomAccessFile rafWordIndex = new RandomAccessFile(wordIndex, "r");
		RandomAccessFile rafTokfile = new RandomAccessFile(tokfile, "r");
		RandomAccessFile rafKorpus = new RandomAccessFile(korpus, "r");
		
		// Hämta position att söka på i wordIndex från bucket
		rafBucket.seek(getBucketOffset(searchWord));
		long wordIndexPos = rafBucket.readLong();		
		long wordIndexNextPos = 0;
		
		// Kontrollera att vi fått en giltig position, annars finns ej ordet.
		if(wordIndexPos == 0) {
			String temp = rafWordIndex.readLine();
			if(!temp.startsWith(searchWord))
				wordIndexNextPos = -1;
		} else {// Fick en giltig pos, hämta nästa ord åxå.			
			try {
				wordIndexNextPos = rafBucket.readLong();
				for(int i=0; i<100 && wordIndexNextPos == 0; i++) 
					wordIndexNextPos = rafBucket.readLong();
			} catch(EOFException e) { // Vi nådde slutet av filen, sätt slutpos till slutet av filen.
				wordIndexNextPos = wordIndex.length();
			}
		}

		// Hämta offset för ordet i tokfile från wordIndex
		rafWordIndex.seek(wordIndexPos);
		long tokfilePos = 0;
		int wordCount = 0;
		boolean found = false;
		
		for(long i=wordIndexPos; i<=wordIndexNextPos && !found; i=rafWordIndex.getFilePointer()) {
			String line[] = rafWordIndex.readLine().split(" ");
			if(line[0].equals(searchWord)) {
				// Hämta offset i tokfile
				tokfilePos = Long.parseLong(line[1]);
				wordCount = Integer.parseInt(line[2]);
				found = true; // break; istallet?
			}
		}
		
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
		byte[] b = new byte[30+searchWord.length()+30];
		
		rafTokfile.seek(tokfilePos);
		
		for(int i=0; i<resultsLimit; i++) {
			String line[] = rafTokfile.readLine().split(" ");	// Hämta offset för nästa ord
			rafKorpus.seek(Long.parseLong(line[1])-30);			// Applicera offseten mot korpus
			rafKorpus.read(b);									// Hämta meningen	
			System.out.println(i+1+":\t "+new String(trimOutput(b), "ISO-8859-1")); // printa utan enter och dyl.
		}
		System.out.println();
		System.out.println("Utskrift av resultatet tog: "+(float)stopWatch.stop()/1000+"s");
		
		rafBucket.close();
		rafWordIndex.close();
		rafTokfile.close();
		rafKorpus.close();
		
		System.exit(0);
	}


	private static boolean checkIndexes() {
		if(tokfile.exists() && bucket.exists() && wordIndex.exists())
			return true;
		return false;
	}


	private static boolean createIndexes() throws IOException, InterruptedException{
		if(createTokfile() && createWordIndex() && createBucket())
			return true;
		return false;
	}
	
	
	private static boolean createTokfile() throws IOException, InterruptedException{
		if(tokfile.exists())
			return true;
		System.out.println("Saknar tokfile, kör följande kommandon i terminalen:");
		System.out.println();
		System.out.println("export LC_COLLATE=C");
		System.out.println("/info/adk13/labb1/tokenizer < /info/adk13/labb1/korpus | sort > /var/tmp/tokfile\n");
		System.exit(1);		
		return false;
	}
	
	/**
	 * Skapar en "latmanshashning", aka bucket. Varje offset till trebokstavskombinationer av orden i wordIndex lagras
	 * binärt på en uträknad plats beroende på sina bokstäver. 
	 * @return true om allt gått väl, annars false.
	 * @throws IOException
	 */
	private static boolean createBucket() throws IOException{
		System.out.print("Generar ny bucketfil... ");
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
//				System.out.println("word: "+newWord+" seek: "+getBucketOffset(newWord)+" pos: "+pos+" count:"+lineSplit[2]);
				lastWord = newWord;
			}
			pos += line.length()+1;
		}
		out.close();
		in.close();
		System.out.println("Klart!");
		return true;
	}

	/**
	 * Skapar wordIndexfilen som innehåller ett förekomst av varje ord och dess position i tokfile och antalet förekomster.
	 * @return	true om allt gått väl, annars false.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static boolean createWordIndex() throws FileNotFoundException, UnsupportedEncodingException{
		System.out.print("Generarar ny wordIndex... ");
		
		Scanner in = new Scanner(tokfile, "ISO-8859-1");
		PrintWriter writer = new PrintWriter(wordIndex, "ISO-8859-1");
		
		long pos = 0, prevPos=0;
		int count = 0;
		// Hämta första raden raden redan nu, annars kommer while satsen nedan strunta i det första ordet.
		String[] line = in.nextLine().split(" ");
		String lastWord = line[0], newWord = "";		
		
		// Gå igenom alla rader i tokfile och skriv in en förekomst av varje ord, dess placering i tokfile och hur många det är.
		while(in.hasNextLine()) {
			line = in.nextLine().split(" ");
			newWord = line[0];
			pos += (line[0].length()+line[1].length()+2); // +2 för space mellan ord och offsett samt newline på slutet
			count++;
			if(!newWord.equals(lastWord)) { //om senast skrivna ordet inte är lika med det aktuella ordet, skriv det till filen.
				writer.println(lastWord + " " + prevPos + " " + count);
				lastWord = newWord;
				count = 0;
				prevPos = pos;
			}
		}
		// Skriv ut sista ordet, plus ett då vi inte kommer att räkna med den sista raden i while-loopen.
		writer.println(newWord + " " + prevPos + " " + (count+1));
		writer.close();
		in.close();
		System.out.println("Klart!");
		return true;
	}
	
	/**
	 * Beräknar en offset till bucketfilen baserat på de tre första bokstäverna i ett ord.
	 * Skapar med andra ord en tredimensionell array i en fil.
	 * @param word ordet som skall få en offset
	 * @return offsetten för ordet.
	 */
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
	 * Tar bort radbyten, enter, och tabbar och ersätter dom med space.
	 * @param b	 bytearray med texten som skall trimmas
	 * @return b Bytearray utan de oönskade tecknen.
	 */
	private static byte[] trimOutput(byte[] b) {
		for(int i=0; i<b.length; i++) {
			if(b[i]==10 || b[i]==13 || b[i] == 9)
				b[i]=32;
		}
		return b;
	}

}
