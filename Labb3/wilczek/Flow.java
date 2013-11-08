import java.util.LinkedList;

public class Flow {
	Kattio io;
	int v, s, t, e, maxflow;
	LinkedList2[] kanter;

	public Flow() throws Exception {
		io = new Kattio(System.in, System.out);
		// System.err.println("HERE WE GOOOOO!!!!");
		readFlow();
		// System.err.println("NU HAR VI LÄST, NU SKA VI TÄNKA");
		findMaxFlow();
		// System.err.println("NU SKA HÄR SKRIVAS MASSSSSOR");
		writeMaxFlow();
		io.close();
		// System.err.println("HEJDÅÅÅÅ!!!!!");
	}

	private void readFlow() {
		// io.flush();
		v = io.getInt();
		s = io.getInt();
		t = io.getInt();
		e = io.getInt();
		kanter = new LinkedList2[v + 1];
		for (int i = 0; i <= v; i++)
			kanter[i] = new LinkedList2(); // Inga null, tack!!!
		for (int i = 0; i < e; i++) {
			int a = io.getInt(); // Från 3
			int b = io.getInt(); // Till 2
			int cap = io.getInt(); // Kapacitet
			Kant x = new Kant(a, b, cap, 0);
			Kant y = new Kant(b, a, 0, 0);
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
			} else {
				kanter[a].add(x);
				kanter[b].add(y);
				x.invers = y;
				y.invers = x;
			}
		}
	}

	private void findMaxFlow() throws Exception {
		Kant x;
		maxflow = 0;
		while ((x = BFS()) != null) {
			int r = Integer.MAX_VALUE;
			LinkedList<Kant> p = new LinkedList<Kant>();
			while (x != null) {
				p.add(x);
				r = Math.min(r, x.cap - x.flow);
				x = x.father;
			}
			for (Kant e : p) {
				e.flow += r;
				e.invers.flow = -e.flow;
			}
			maxflow += r;
		}
	}

	private void writeMaxFlow() throws Exception {
		io.println(v); // TODO vet inte om v eller v-kanter-som-inte-är-med
		io.println(s + " " + t + " " + maxflow);
		LinkedList<String> y = new LinkedList<String>();
		Queue q = new Queue();
		boolean[] visited = new boolean[v + 1];
		q.Put(s);
		while (!q.IsEmpty()) {
			int at = (Integer) q.Get();
			if (!visited[at]) {
				visited[at] = true;
				Kant le = kanter[at].first;
				while (le != null) {
					if (le.flow > 0) {
						y.add(at + " " + le.to + " " + le.flow);
					}
					q.Put(le.to);
					le = le.next;
				}
			}

		}
		io.println(y.size());
		for (String s : y) {
			io.println(s);
		}
		io.flush();

	}

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
		new Flow();
	}

	public class Kant {
		public int to;
		public int from;
		public int flow;
		public int cap;
		public Kant invers;
		public Kant father;
		public Kant next;

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

}
