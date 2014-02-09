package arbor.lbs.uqp.graph.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

public class Path implements Serializable, Cloneable, Comparable {
    public LinkedList<Integer> pathList;
    public double cost;    
    public int currentVisitedRNetLevel;
    public Path() {
    	
    }
    public Path clone() {
    	Path p = new Path();
    	p.cost = cost;
    	p.pathList = new LinkedList<Integer>();
    	for (int i=0;i<pathList.size();i++)
    		p.pathList.add(pathList.get(i));    	
    	return p;
    }
    public LinkedList<Integer> getReversedPath() {
    	LinkedList<Integer> r = new LinkedList<Integer>();
    	for (int i=pathList.size()-1;i>=0;i--) {
    		r.add(pathList.get(i));
    	}
    	if (r.size()!=pathList.size()) {
    		System.err.println("ERROR");
    		System.exit(0);
    	}
    	return r;
    }
    public int getTailVID() {
    	return pathList.getLast();
    }
    public void join(Path tailPath) {
    	if (pathList.getLast() != tailPath.pathList.getFirst()) {
    		System.err.println("is not a linkable path");
    		System.exit(0);
    	}
    	for (int i = 1;i<tailPath.pathList.size();i++) {
    		pathList.add(tailPath.pathList.get(i));
    	}
    	cost += tailPath.cost;
    }
    public int getSourceVID() {
    	return pathList.getFirst();
    }
    public int getDestinationVID() {
    	return pathList.getLast();
    }
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("(");
    	for (int i = 0;i<pathList.size()-1;i++) {
    		buf.append(pathList.get(i)+" ");    		
    	}
    	buf.append(pathList.get(pathList.size()-1)+")");
    	buf.append(":"+cost);
    	return buf.toString();
    }
    public int compareTo(Object o) {
    	Path ano = (Path)o;
    	if (this.cost < ano.cost) {
    		return -1;
    	}
    	if (this.cost > ano.cost) {
    		return 1;
    	}
    	else {
    		return 0;
    	}
    }
}
