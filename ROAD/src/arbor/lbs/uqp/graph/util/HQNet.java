package arbor.lbs.uqp.graph.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import arbor.lbs.uqp.algorithm.VisitedObject;

public class HQNet implements Serializable { 
	int hID;
    private Graph graph;
    int parentRID; 
    boolean containObj = false;
    double maxKDist = 0;
    HashSet<Integer> border = null;
    HashMap<Integer, ArrayList<VisitedObject>> borderKnnMap = null;
    
    public HQNet(int hID, int parentRID, Graph graph) {
    	this.hID = hID;
    	this.parentRID = parentRID;    		
    	this.graph = graph;
    	border = new HashSet<Integer>();
    	borderKnnMap = new HashMap<Integer, ArrayList<VisitedObject>>();
    	if (graph.getNumEdges() > 0)
    	  initBuildHQNet(false);
    	else
          initBuildHQNet(true);    	
    }
    public LinkedList<Integer> getVertexesContainingObject() {
    	if (hasMsgObjInside()) {
    		LinkedList<Integer> het = new LinkedList<Integer>();
    		List<Integer> vlist = getAllVertexes();
    		for (int i=0;i<vlist.size();i++) {
    			Vertex v = GraphPool.getSignleton().getVertex(vlist.get(i));
    			if (v.hasAssocMsgObj()) {
    				het.add(v.getId());
    			}    			                         
    		}
    		return het;
    	}
    	else {
    		return null;
    	}
    }
    
    /**
     * We build:
     * 1. test if objects are inside.
     * 2. identify borders.
     * 3. if graph's edge is empty, collect!
     */
    public void initBuildHQNet(boolean buildEdgeSet) {
        List<Integer> vlist = graph.getVertexes();
        for (int i=0;i<vlist.size();i++) {
        	Vertex v = GraphPool.getSignleton().getVertex(vlist.get(i));
        	if (!containObj && v.hasAssocMsgObj()) {
        		this.containObj = true;
        	}
        	List<Integer> neighbor = v.getNeighbors();
        	for (int j=0;j<neighbor.size();j++) {
              if (buildEdgeSet) {
            	  Edge a = GraphPool.getSignleton().getEdge(v.getId(), neighbor.get(j));
            	  if (a == null) {
            		  System.out.println("bug!");            		  
            		  System.exit(0);
            	  }
            	  this.insertEdge(a.getId());
              }
        	  if (!graph.containVertex(neighbor.get(j))) {
        		  border.add(v.getId());
        		  break;
        	  }
        	}        	 
        }
    }
    public int getHQNetID() {
    	return this.hID;    	
    }
    public boolean hasMsgObjInside() {
    	return containObj;
    }
    public int getParent() {
    	return this.parentRID;
    }
    public HashSet<Integer> getBorderSet() {
    	return this.border;
    }
    public HashMap<Integer, ArrayList<VisitedObject>> getBorderKNNMap() {
    	return this.borderKnnMap;
    }
    public boolean containVertex(int vID) {
    	return graph.containVertex(vID);
    }
    public List<Integer> getAllVertexes() {
    	return graph.getVertexes();
    }
    public List<Long> getAllEdges() {
    	return graph.getEdges();
    }
    public double getMaxKDist() {
    	return this.maxKDist;
    }
    public void insertEdge(long eID) {
    	graph.insertEdge(eID);
    }
    public void insertBorderKnn(int borderID, ArrayList<VisitedObject> kNN) {
    	this.borderKnnMap.put(borderID, kNN);
    }
    public void insertMaxKDist(double dist) {
    	this.maxKDist = dist;
    }
    public void setContainObj(boolean flag) {
    	this.containObj = flag;
    }

	public Graph getGraph() {
		return this.graph;
	}

}
