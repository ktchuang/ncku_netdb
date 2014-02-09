package arbor.foundation.time;

public class TimePair {
	long startTime;
	long endTime;
	String name;

	public TimePair(String name) {
		this.name = name;
	}
	public double getSeconds() {
		return (double) ((double) (endTime - startTime) / 1000);
	}
	public double getMillisSeconds() {
		return (double) ((double) (endTime - startTime) / 1000000);
	}
	public double getPeakSeconds() {
		return (double) ((double) (endTime - startTime));
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("<"+this.name+"> uses "+getSeconds() + " seconds");
		return buf.toString();
	}
}
