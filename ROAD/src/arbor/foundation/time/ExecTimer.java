package arbor.foundation.time;

import java.util.Stack;

public class ExecTimer {

	
	Stack<TimePair> timeStack;
    public ExecTimer() {
    	timeStack = new Stack<TimePair>();
    }
    public void setStartTime(String description) {
    	TimePair pair = new TimePair(description);
    	pair.startTime = System.currentTimeMillis(); 
    	timeStack.push(pair);
    }
    public TimePair setEndTime() {
    	TimePair pair = timeStack.pop();
    	pair.endTime = System.currentTimeMillis();
    	return pair;
    }
    public void setNanoStartTime(String description) {
    	TimePair pair = new TimePair(description);
    	pair.startTime = System.nanoTime(); 
    	timeStack.push(pair);
    }
    public TimePair setNanoEndTime() {
    	TimePair pair = timeStack.pop();
    	pair.endTime = System.nanoTime();
    	return pair;
    }
}
