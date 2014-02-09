package arbor.lbs.uqp.algorithm;

import java.io.Serializable;

import arbor.lbs.uqp.graph.util.Graph;


public class VisitedObject implements Comparable, Serializable {
    double dist;
    int vID;
    int msgID;
    
    public VisitedObject(int vID, int msgID, double dist) {
    	this.vID = vID;
    	this.msgID = msgID;
    	this.dist = dist;
    }
    public int getVID() {
    	return this.vID;
    }
    public int getMsgID() {
    	return this.msgID;
    }
    public double getDist() {
    	return this.dist;
    }
    public void addDist(double distance) {
    	this.dist += distance;
    }
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		VisitedObject ano = (VisitedObject)o;
	   	if (this.dist > ano.dist) {
	   		return 1;
	   	}
	   	if (this.dist < ano.dist) {
	   		return -1;
	   	}
	   	else {
	   		return 0;
	   	}
	}

}
