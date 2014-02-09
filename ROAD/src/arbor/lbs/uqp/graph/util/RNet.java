package arbor.lbs.uqp.graph.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import arbor.lbs.uqp.graph.dijkstra.Dijkstra;

public class RNet implements Serializable {
	int rID;
    private Graph graph;
    int level;
    int parentRID; 
    HashSet<Integer> childRID;
    int objCnt = 0;
    boolean containObj = false;
    boolean containHQNet = false;
    HashMap<Integer, ShortCutSet> borderMap = null;
    
    public RNet(int rID, int level, int parentRID, Graph graph) {
    	this.rID = rID;
    	this.level = level;
    	this.parentRID = parentRID;    		
    	this.graph = graph;
    	borderMap = new HashMap<Integer, ShortCutSet>();
    	childRID = new HashSet<Integer>();
    	if (graph.getNumEdges() > 0)
    	  initBuildRNet(false);
    	else
          initBuildRNet(true);
    	
    	//buildShortCut();    	
    }
    public LinkedList<Integer> getVertexesContainingObject() {
    	if (hasMsgObjInside()) {
    		LinkedList<Integer> ret = new LinkedList<Integer>();
    		List<Integer> vlist = getAllVertexes();
    		for (int i=0;i<vlist.size();i++) {
    			Vertex v = GraphPool.getSignleton().getVertex(vlist.get(i));
    			if (v.hasAssocMsgObj()) {
    				ret.add(v.getId());
    			}    			                         
    		}
    		return ret;
    	}
    	else {
    		return null;
    	}
    }
    public Graph getGraph() {
    	return this.graph;
    }
    public int getRNetID() {
    	return this.rID;    	
    }
    public int getObjCnt() {
    	return this.objCnt;    	
    }
    public boolean hasMsgObjInside() {
    	if (this.objCnt==0)
    		return false;
    	else
    		return true;
    }
    public boolean hasHQNetInside() {
    	return containHQNet;
    }
    public int getLevel() {
    	return this.level;
    }
    public int getParent() {
    	return this.parentRID;
    }
    public ShortCutSet getShortCutSet(int vID) {
    	return this.borderMap.get(vID);
    }
    public boolean isRootRNet() {

    	return (parentRID<0) ? true : false;
    }
    public boolean isLeafRNet() {
    	if (childRID.size() == 0)
    		return true;
    	else
    		return false;
    }
    public void insertChildRNet(int childRID) {
    	this.childRID.add(childRID);
    }
    public void insertEdge(long eID) {
    	graph.insertEdge(eID);
    }
    public void setContainObj(boolean flag) {
    	this.containObj = flag;
    }
    public void increaseObjCnt(int num) {
    	this.objCnt += num;
    }
    public void decreaseObjCnt(int num) {
    	if (this.objCnt>0)
    		this.objCnt -= num;
    }
    public void resetSubgraph() {
    	this.graph = null;
    }
    public void setContainHQNet(boolean flag) {
    	this.containHQNet = flag;
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
    
    public List<Integer> getChildRNetList() {
    	LinkedList<Integer> list = new LinkedList<Integer>();
    	Object[] val = childRID.toArray();
    	for (int i=0;i<val.length;i++) {
    		list.add((Integer)val[i]);
    	}
    	return list;
    }
    /**
     * We build:
     * 1. test if objects are inside.
     * 2. identify borders.
     * 3. set vertex's level/RNetID
     * 4. if graph's edge is empty, collect!
     */
    public void initBuildRNet(boolean buildEdgeSet) {
        List<Integer> vlist = graph.getVertexes();
        for (int i=0;i<vlist.size();i++) {
        	Vertex v = GraphPool.getSignleton().getVertex(vlist.get(i));
        	v.setRNet(this.level, this.rID);
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
        		  v.setBorder(this.level);
        		  ShortCutSet sc = new ShortCutSet(v.getId());
        		  borderMap.put(v.getId(), sc);
        		  break;
        	  }
        	}        	 
        }
    }
    public LinkedList<Integer> getBorderList() {
    	LinkedList<Integer> blist = new LinkedList<Integer>();
    	Object[] key = borderMap.keySet().toArray();
    	for (int i=0;i<key.length;i++) {
    		blist.add((Integer)key[i]);
    	}
    	return blist;
    }
    public void buildShortCut() {
    	/*TODO: to check the shortcut between two borders, we need to check
    	 *all vertexes rather than vertexes in the rnet.*/
    	/**
    	 * TODO: path is bi-directed. We did not care the direction now.
    	 */
    	Object[] border = borderMap.keySet().toArray();
    	for (int i=0;i<borderMap.size();i++) {
    		Dijkstra dijkstra = new Dijkstra(graph);
    		dijkstra.execute((Integer)border[i]);
            ShortCutSet scA = borderMap.get(border[i]);
    		for (int j=i+1;j<borderMap.size();j++) {
    	      ShortCutSet scB = borderMap.get(border[j]);
    		  
    	      Path p = dijkstra.getPath((Integer)border[j]);
    	      
    	      if (p != null) {
    		    scA.insertShortCut(p, (Integer)border[j]);
    		    Path revP = new Path();
    		    revP.cost = p.cost;
    		    revP.pathList = p.getReversedPath();
    		    scB.insertShortCut(revP, (Integer)border[i]);
    	      }
    		}
    	}
    }
    
}
