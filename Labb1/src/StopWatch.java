
public class StopWatch {
	private long startTime;
	private long stopTime;
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	public long stop() {
		stopTime = System.currentTimeMillis();
		return stopTime-startTime;
	}
	
	public long result() {
		return stopTime-startTime;
	}
	public void clear() {
		
	}
}
