import java.awt.Point;
 

public class BipRed {
        Kattio io;
        Point[] kanter;
        int x, y, e, totKant;
 
        void readBipartiteGraph() {
                // Läs antal hörn och kanter
                x = io.getInt();
                y = io.getInt();
                e = io.getInt();
                kanter = new Point[e];
 
                // Läs in kanterna
                for (int i = 0; i < e; ++i) {
                        int a = io.getInt();
                        int b = io.getInt();
                        kanter[i] = new Point(a, b);
                }
        }
 
        void writeFlowGraph() {
                int s = x + y + 1;
                int t = s + 1;
                // Skriv ut antal hörn och kanter samt källa och sänka
                io.println(x + y + 2); // Hörn i x och y + sänka och källa aka v
                io.println(s + " " + t);
                io.println(e + x + y); // Alla standardkanter + alla nya till s och t.
                for (int i = 0; i < e; ++i) {
                        io.println(kanter[i].x + " " + kanter[i].y + " 1");
                }
                for (int i = 0; i < x; i++) {
                        io.println(s + " " + (i + 1) + " 1"); // Alla kanter från källan
                }
                for (int i = 0; i < y; i++) {
                        io.println(x + 1 + i + " " + t + " 1"); // Alla kanter till sänkan
                }
                // Var noggrann med att flusha utdata när flödesgrafen skrivits ut!
                io.flush();
 
                // Debugutskrift
                System.err.println("Skickade iväg flödesgrafen");
        }
 
        void readMaxFlowSolution() {
                // Läs in antal hörn, kanter, källa, sänka, och totalt flöde
                // (Antal hörn, källa och sänka borde vara samma som vi i grafen vi
                // skickade iväg)
                int v = io.getInt();
                int s = io.getInt();
                int t = io.getInt();
                int totflow = io.getInt();
                int e = io.getInt();
                kanter = new Point[e];
                totKant = 0;
//              System.err.println("Läser lösning");
 
                for (int i = 0; i < e; ++i) {
                        // Flöde f från a till b
                        int a = io.getInt();
                        int b = io.getInt();
                        int f = io.getInt();
                        
                        if (!(a == s || a == t) && !(b == s || b == t) && f > 0) {
                                kanter[totKant] = new Point(a, b);
                                totKant++;
                        }
                }
//              System.err.println("Läsning klar");
        }
 
        void writeBipMatchSolution() {
 
                // Skriv ut antal hörn och storleken på matchningen
                io.println(x + " " + y);
                io.println(totKant);
 
                for (int i = 0; i < totKant; ++i) {
                        // Kant mellan a och b ingår i vår matchningslösning
                        io.println(kanter[i].x + " " + kanter[i].y);
                }
 
        }
 
        BipRed() {
                io = new Kattio(System.in, System.out);
//              System.err.println("START :DDD");
                readBipartiteGraph();
 
                writeFlowGraph();
 
                readMaxFlowSolution();
 
                writeBipMatchSolution();
 
                // debugutskrift
//              System.err.println("Bipred avslutar\n");
 
                // Kom ihåg att stänga ner Kattio-klassen
                io.close();
        }
 
        public static void main(String args[]) {
                new BipRed();
        }
}