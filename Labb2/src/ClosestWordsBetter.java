/* Labb 2 i DD1352 Algoritmer, datastrukturer och komplexitet    */
/* Se labbanvisning under kurssidan http://www.csc.kth.se/DD1352 */
/* Ursprunglig författare: Viggo Kann KTH viggo@nada.kth.se      */
import java.util.LinkedList;
import java.util.List;

public class ClosestWordsBetter {
	LinkedList<String> closestWords = null;
	int m[][] = new int[41][41];
	char[] lastWord = {' '};
	int closestDistance = -1;
	
	//Chararrays istallet for strings?
	int partDist(char[] w1, char[] w2, int w1len, int w2len) {
		int c2 = 1;
		
		for(int i = 0; i <= Math.min(w2len, lastWord.length)-1; i++){
			if (w2[i] == lastWord[i])
				c2++;
			else
				break;
		}
	
		for(int r=1; r<=w1len; r++){
			int lowestRowDistance = 100;
			for(int c=c2; c<=w2len; c++){
				int t=0;
				// Add one if different letters
				if(w1[r-1] != w2[c-1]) t++;
				// Get the least value from neighbors. Left, Over
				m[r][c] = Math.min(Math.min(m[r-1][c]+1, m[r][c-1]+1), m[r-1][c-1]+t);
				
				if(m[r][c] < lowestRowDistance) 
					lowestRowDistance = m[r][c];
			}
			if(lowestRowDistance > closestDistance && closestDistance != -1 ) {
				m[w1len][w2len] = 100;
				break;
			}
		}
		lastWord = w2;
		return m[w1len][w2len];
	}

	int Distance(String w1, String w2) {
		return partDist(w1.toCharArray(), w2.toCharArray(), w1.length(), w2.length());
	}

	public ClosestWordsBetter(String w, List<String> wordList) {
		init();
		for (String s : wordList) {
			int dist = Distance(w, s);
			// System.out.println("d(" + w + "," + s + ")=" + dist);
			if (dist < closestDistance || closestDistance == -1) {
				closestDistance = dist;
				closestWords = new LinkedList<String>();
				closestWords.add(s);
			}
			else if (dist == closestDistance)
				closestWords.add(s);
//			System.out.println("W: "+w+" S:"+s+" Distance: "+dist);
		}
	}

	int getMinDistance() {
		return closestDistance;
	}

	List<String> getClosestWords() {
		return closestWords;
	}
	
	private void init() {
		for(int r=0; r<=40; r++)	m[r][0] = r;
		for(int c=0; c<=40; c++)	m[0][c] = c;
	}
}
