package arbor.lbs.uqp.graph.util;

import java.util.LinkedList;

public class MsgObjPath extends Path {
    public int msgID;
    /*the vertex associated with the MsgObj*/
    public int assocVID;
    public int currentVisitedRNetLevel;
    public MsgObjPath() {
    	super();
    	msgID = -1;
    	assocVID = -1;
    	currentVisitedRNetLevel = RNetHierarchy.getSignleton().getMaxLevel();
    }
    public MsgObjPath clone() {
    	MsgObjPath p = new MsgObjPath();
    	p.cost = cost;
    	p.pathList = new LinkedList<Integer>();
    	for (int i=0;i<pathList.size();i++)
    		p.pathList.add(pathList.get(i));   
    	p.msgID = msgID;
    	p.assocVID = assocVID;
    	p.currentVisitedRNetLevel = currentVisitedRNetLevel;
    	return p;
    } 
    public boolean hasTouchObj() {
    	if (msgID < 0) {
    		return false;
    	}
    	else {
    		return true;
    	}
    }
    public int compareTo(Object o) {
    	MsgObjPath ano = (MsgObjPath)o;
    	if (this.cost < ano.cost) {
    		return -1;
    	}
    	if (this.cost > ano.cost) {
    		return 1;
    	}
    	else {  
    		if (hasTouchObj()) {
    			return -1;
    		}
    		else if (ano.hasTouchObj()) {
    			return 1;
    		}
    		else {
    		  if (msgID < ano.msgID)
    			return -1;
    		  else
    			return 1;
    		}
    	}
    }
}
