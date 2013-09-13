
public class Time {
	private long startTime;
	private long stopTime;
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	public long stop(){
		stopTime = System.currentTimeMillis();
		return stopTime-startTime;
	}
	
	public long res(){
		return stopTime-startTime;
	}
}
