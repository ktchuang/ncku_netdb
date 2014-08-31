package netdb.spatial.trajdb.index;

public class TimeStamp {
  private static long incTP = 0;
  long timestamp;
  public static long getNextTimeStamp() {
	  return incTP++;
  }
  
  public TimeStamp() {
	  
  }
  public void setTP(long time) {
	  this.timestamp = time;
  }
  public long getTP() {
	  return timestamp;
  }
}
