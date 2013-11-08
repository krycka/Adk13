public class SuperTotal {
	int s, t, maxflow, v, x, y, e;
	public static int r = 1; // Eftersom vi analyserer bipartita grafer kommer
								// flödet alltid vara 1, alltså maxflödet kommer
								// också alltid vara 1
	LinkedList2[] kanter; // Kanterna från en viss nod.
	Kattio io;

	public SuperTotal() throws Exception {
		io = new Kattio(System.in, System.out);
		read();
		findMaxFlow();
		write();
		io.close();
	}

	private void read() {
		// Läser in från System.in
		x = io.getInt();
		y = io.getInt();
		e = io.getInt();
		// Räknar ut s och t, antalet kanter v får vi genom att kolla på den
		// sista kanten (t).
		s = x + y + 1;
		t = s + 1;
		v = t;
		// Vi använder oss av grannmatriser för att få bättre tidskomplexitet
		// O(v³) ist för O(v⁴)
		kanter = new LinkedList2[v + 1];
		// Inga null, tack!!!
		for (int i = 0; i <= v; i++)
			kanter[i] = new LinkedList2();
		// Läs in kanterna
		for (int i = 0; i < e; ++i) {
			int a = io.getInt();
			int b = io.getInt();
			addkant(a, b);
		}
		// Lägger till kanter från källan till alla element i X
		for (int i = 0; i < x; i++) {
			addkant(s, (i + 1));
		}
		// Lägger till kanter från alla element i Y till sänkan.
		for (int i = 0; i < y; i++) {
			addkant(x + 1 + i, t);
		}

	}

	private void addkant(int a, int b) {
		Kant x = new Kant(a, b, 1, 0);
		Kant y = new Kant(b, a, 0, 0);
		// Specialfall från uppgift 2.
		if (kanter[a].contains(x)) {
			Kant y2 = null;
			Kant le = kanter[a].first;
			while (le != null) {
				if (le.equals(x)) {
					y2 = le.invers;
					break;
				}
				le = le.next;
			}
			kanter[a].remove(x);
			kanter[a].add(x);
			x.invers = y2;
			y2.invers = x;
		}
		// Egentligen det som körs i uppgift 3.
		else {
			kanter[a].add(x);
			kanter[b].add(y);
			x.invers = y;
			y.invers = x;
		}
	}

	private void write() {
		io.println(x + " " + y);
		io.println(maxflow);
		for (int i = 1; i < kanter.length - 2; i++) {
			Kant le = kanter[i].first;
			while (le != null) {
				if (le.to != t && le.flow > 0) {
					io.println(i + " " + le.to);
				}
				le = le.next;
			}
		}
		io.flush(); //Glöm inte att spola kröken
	}

	/**
	 * Som pseudo-koden i labblydelsen 
	 */
	private void findMaxFlow() throws Exception {
		Kant x;
		maxflow = 0;
		//BFS reurnerar sista kanten i en stig, minska flöde med r för alla i stigen. (För alla stigar)
		while ((x = BFS()) != null) {
			while (x != null) {
				x.flow += r;
				x.invers.flow = -x.flow;
				x = x.father; //Vi använder inte restflow utan räknar cap - flow
			}
			maxflow += r;
		}
	}
	/**
	 * trad. bfs
	 * @return
	 * @throws Exception
	 */
	private Kant BFS() throws Exception {
		boolean[] visited = new boolean[v + 1];
		Queue q = new Queue();
		Kant le = kanter[s].first;
		while (le != null) {
			if ((le.cap - le.flow) > 0) {
				q.Put(le);
				le.father = null;
			}
			le = le.next;
		}
		visited[s] = true;
		while (!q.IsEmpty()) {
			Kant at = (Kant) q.Get();
			if (!visited[at.to]) {
				visited[at.to] = true;
				if (at.to == t) {
					return at;
				}
				le = kanter[at.to].first;
				while (le != null) {
					if (!visited[le.to] && (le.cap - le.flow) > 0) {
						le.father = at;
						q.Put(le);
					}
					le = le.next;
				}
			}
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		new SuperTotal();
	}

	public class LinkedList2 {
		public Kant first;
		public Kant last;

		public LinkedList2() {

		}

		public void add(Kant ge) {
			if (first == null) {
				first = ge;
				last = ge;
			} else {
				last.next = ge;
				last = ge;
			}
		}

		public void clear() {
			first = null;
			last = null;
		}

		public boolean contains(Kant ge) {
			Kant g = first;
			while (g != null) {
				if (g == ge)
					return true;
				g = g.next;
			}
			return false;
		}

		public void remove(Kant ge) {
			Kant prev = null;
			Kant g = first;
			while (g != null) {
				if (g == ge) {
					if (prev == null) {
						first = g.next;
					} else {
						prev.next = g.next;
					}
					return;
				}
				prev = g;
				g = g.next;
			}
		}

	}

	public class Kant {
		public int to;
		public int from;
		public int flow;
		public int cap;
		public Kant invers; // För flödesminskning
		public Kant father; // För BFS
		public Kant next; // För Lista

		public Kant(int from, int to, int cap, int flow) {
			this.from = from;
			this.to = to;
			this.cap = cap;
			this.flow = flow;
		}

		@Override
		public boolean equals(Object o) {
			Kant t = (Kant) o;
			if (this.to == t.to)
				return true;
			return false;
		}
	}
}
