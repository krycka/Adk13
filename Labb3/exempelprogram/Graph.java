/**
 * @author rodoo
 *
 */

public class Graph {
	public int x;
	public int y;
	public int e;
	
	int edges[];
	int bipGraph[][];
	
	int c[][];
	int f[][];
	int cf[][];

	
	public Graph(int x, int y) {
		this.x = x;
		this.y = y;
		this.e = 0;
		
		edges = new int[x+y+1]; // plus ett då vi inte använder index 0
		
//		c = new int[x][y];
//		f = new int[x][y];
//		cf = new int[x][y];
	}
	
	public void addEdge(int a, int b) {
		edges[a] = b;
		//edges[b] = a;
		e++;
	}
	
	public int getEdge(int a) {
		return edges[a];
	}
	
	
	
/*
	Ford-Fulkersons algoritm i pseudokod

	c[u,v] är kapaciteten från u till v, f[u,v] är flödet, cf[u,v] är restkapaciteten.

	for varje kant (u,v) i grafen do 
	    f[u,v]:=0; f[v,u]:=0 
	    cf[u,v]:=c[u,v]; cf[v,u]:=c[v,u]
	    
	while det finns en stig p från s till t i restflödesgrafen do 
	    r:=min(cf[u,v]: (u,v) ingår i p) 
	    for varje kant (u,v) i p do 
	         f[u,v]:=f[u,v]+r; f[v,u]:= -f[u,v] 
	         cf[u,v]:=c[u,v] - f[u,v]; cf[v,u]:=c[v,u] - f[v,u]
*/
	
	public void fordFulkersons() {
		for(int u=0; u<x; u++) {
			for(int v=0; v<y; v++) {
				f[u][v] = 0;
				f[v][u] = 0;
				cf[u][v] = c[u][v];
				cf[v][u] = c[v][u];				
			}			
		}
		
		
	}
	
	
}
