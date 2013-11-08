/**
 * ADK - Labb 3
 * 
 * By: Christoffer Rödöö, 2013-11-07
 * 
 * 
 * - Läs indata för matchningsproblemet från standard input.
 * - Översätt matchningsinstansen till en flödesinstans.
 * - Skriv indata för flödesproblemet till standard output (se till att utdata flushas).
 * - Den svarta lådan löser flödesproblemet.
 * - Läs utdata för flödesproblemet från standard input.
 * - Översätt lösningen på flödesproblemet till en lösning på matchningsproblemet.
 * - Skriv utdata för matchningsproblemet till standard output.
 */
import java.util.Scanner;

public class BipRedFlow {
	Kattio io;	// in out handler
	Graph bip, bipMatched;
	
	public static void main(String args[]) {
		new BipRedFlow();
	}

	public BipRedFlow() {
		io = new Kattio(System.in, System.out);

		//readBipGraph();
		
		//writeFlowGraph();
		
		readMaxFlowSolution();
		
		writeBipMatchSolution();

		// debugutskrift
		System.err.println("Bipred avslutar\n");

		// Kom ihåg att stänga ner Kattio-klassen
		io.close();

	}
	
	private void readBipGraph() {
		int x = io.getInt();
		int y = io.getInt();
		int e = io.getInt();
		
		bip = new Graph(x, y);
		bipMatched = new Graph(x, y);
		
		for(int i=0; i < e; i++) {
			bip.addEdge(io.getInt(), io.getInt());
		}
		System.err.println("Läst bipartit graf");
	}
	
	private void writeFlowGraph() {
		System.err.println("WriteFlowGraph START");
		int s = bip.x + bip.y + 1;			// Start
		int t = s + 1;						// Slut
		io.println((bip.x + bip.y + 2));	// Antal noder, alla X + alla Y + start och slut			
		io.println(s + " " + t);			// Start till slut
		io.println(bip.e + bip.x + bip.y);	// Antal kanter, givna + de nya till start / slut
		
		// Skriv ut alla vanliga kanter
		for(int i=1; i <= bip.x; i++) {
			if(bip.getEdge(i) != 0)
				io.println(i + " " + bip.getEdge(i) + " 1");
		}
		// Alla kanter start till X
		for(int i=1; i<= bip.x; i++)
			io.println(s + " " + i + " 1");
		// Alla kanter i Y till slut
		for(int i=bip.x+1; i < s; i++)
			io.println(i + " " + t + " 1");
		
		// Var noggrann med att flusha utdata när flödesgrafen skrivits ut!
		io.flush();
		
		// Debugutskrift
		System.err.println("Skickade iväg flödesgrafen");
		System.err.println("WriteFlowGraph END");
			
	}
	
	private void readMaxFlowSolution() {
		// Läs in antal hörn, kanter, källa, sänka, och totalt flöde
		// (Antal hörn, källa och sänka borde vara samma som vi i grafen vi
		// skickade iväg)
		System.err.println("readMaxFlowSolution START");
		int v = io.getInt();
		int s = io.getInt();
		int t = io.getInt();
		int totflow = io.getInt();
		int e = io.getInt();
		
		for (int i = 0; i < e; ++i) {
			// Flöde f från a till b
		    int a = io.getInt();
		    int b = io.getInt();
		    int f = io.getInt();
		    System.err.println("HAJ");
		    if( (a != s) && (a != t) && (b != s) && (b != t) ) {
		    	bipMatched.addEdge(a, b);
		    }
		}
		
		// Debugutskrift
		System.err.println("Läst flödesgrafen");
		System.err.println("readMaxFlowSolution END");
	}

	private void writeBipMatchSolution() {
		io.println(bipMatched.x + " " + bipMatched.y);
		io.println(bipMatched.e);
		// Skriv ut alla vanliga kanter
		for(int i=0; i < bip.y; i++) {
			if(bip.getEdge(i) != 0)
				io.println(i + " " + bip.getEdge(i));
		}
		System.err.println("Skrivit matchad bipartit graf");
	}
	
		
}
