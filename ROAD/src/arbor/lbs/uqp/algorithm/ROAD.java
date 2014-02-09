package arbor.lbs.uqp.algorithm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import arbor.foundation.time.ExecTimer;
import arbor.lbs.uqp.graph.dijkstra.Dijkstra;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.MsgObj;
import arbor.lbs.uqp.graph.util.MsgObjPath;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.ShortCutSet;
import arbor.lbs.uqp.graph.util.Vertex;

public class ROAD {
	//static int num_k = 5;
	ArrayList<Integer> queryList;
	
    public static void main(String[] args) throws IOException {
    	/**
    	 * Code sequence:
    	 * 1. load graph pool (serialize)
    	 * 2. load rnet hierarchy (serialize)
    	 * 3. load object pool
    	 * 4. check knn sequentially
    	 */
    	int num_k = Integer.valueOf(args[4]);
    	double ranValue = Double.valueOf(args[5]);
    	ROAD road = new ROAD(args[0],args[1],args[2],args[3]);
    	/*
    	RNet rnet = RNetHierarchy.getSignleton().getRNet(12);
    	LinkedList<Integer> bSet = rnet.getBorderList();
    	for (int s=0; s<bSet.size(); s++) {
    		int bID = bSet.get(s);
    		ArrayList<VisitedObject> results = road.getKNN(bID, 5);
    		for (int i=0; i<results.size(); i++) {
    			VisitedObject o = results.get(i);
    			MsgObj msgO = MsgObjPool.getSignleton().getMsg(o.getMsgID());
    			System.out.print(msgO.getAssocVertex());
    			System.out.print(" ");
    			System.out.print(o.getMsgID());
    			System.out.print(" ");
    			System.out.println(o.getDist());
    		}
    		System.out.println("KNN Search Complete");
    	}*/
    	/*
    	int vertexNum = GraphPool.getSignleton().getAllVertexes().size();
    	Random r = new Random();
    	ExecTimer timer = new ExecTimer();
    	timer.setStartTime("Road Query Performance");
    	for (int l=0; l<1000; l++) {
    		int ranVID = r.nextInt(vertexNum)+1;
    		ArrayList<VisitedObject> results = road.getKNN(ranVID, 5);
    		
    		for (int i=0; i<results.size(); i++) {
    			VisitedObject o = results.get(i);
    			MsgObj msgO = MsgObjPool.getSignleton().getMsg(o.getMsgID());
    			System.out.print(msgO.getAssocVertex());
    			System.out.print(" ");
    			System.out.print(o.getMsgID());
    			System.out.print(" ");
    			System.out.println(o.getDist());
    		}
    	}*/
    	ExecTimer timer = new ExecTimer();
    	timer.setStartTime("Road Query Performance");
    	int cases = 0;
    	for (int l=0; l<road.queryList.size(); l++) {
    		ArrayList<VisitedObject> results = road.getKNN(road.queryList.get(l), num_k);
    		if (results.size()!=num_k) 
    			cases++;
    		/*System.out.println(results.size());
    		for (int i=0; i<results.size(); i++) {
    			VisitedObject o = results.get(i);
    			MsgObj msgO = MsgObjPool.getSignleton().getMsg(o.getMsgID());
    			System.out.print(msgO.getAssocVertex());
    			System.out.print(" ");
    			System.out.print(o.getMsgID());
    			System.out.print(" ");
    			System.out.println(o.getDist());
    		}*/
    	}
    	System.out.println(timer.setEndTime());
    	System.out.println(cases);
    	System.out.println("KNN Search Complete");
    	/*
    	int num = (int) (ranValue*1000);
		timer.setStartTime("ROAD Update");
		for (int p=0; p<num; p++) {
			road.updateObject();
		}
		System.out.println(timer.setEndTime());
		*/
    }
	
	private void updateObject() {
		// TODO Auto-generated method stub
		int size = GraphPool.getSignleton().getAllVertexes().size();
		Random r = new Random();
		int vID = r.nextInt(size);
		Vertex v = GraphPool.getSignleton().getVertex(vID);
		
		int msgSize = MsgObjPool.getSignleton().getMsgSize();
		
		MsgObj obj = new MsgObj(msgSize, v.getloc().getCoord(0), v.getloc().getCoord(1));
		obj.setAssocVertex(vID);
		MsgObjPool.getSignleton().insertMsg(obj);
		v.insertAssocMsgObj(msgSize);
		  
		  int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		  for (int i=0; i<=maxLevel; i++) {
			  int rID = v.getInsideRNetID(i);
			  RNet rnet = RNetHierarchy.getSignleton().getRNet(rID);
			  rnet.setContainObj(true);  
		  }
	}

	public ROAD(String networkFn, String hierFn, String msgFn, String queryFn) throws IOException {
		GraphPool.loadGraph(networkFn);
		RNetHierarchy.loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		
		queryList = new ArrayList<Integer>();
		BufferedReader in = new BufferedReader(new FileReader(queryFn));
		String str;
		while ((str = in.readLine()) != null) {
			Integer vID = Integer.valueOf(str);
			queryList.add(vID);
		}
	}
	public ROAD() {
		
	}
	private boolean doSettle(TreeSet<MsgObjPath> settleSet, TreeSet<MsgObjPath> unSettleSetTreeSet,
			              TreeSet<MsgObjPath> checkSet) {
		double minInUnsettle;
		
		Object[] key = checkSet.toArray();
		for (int i=0;i<key.length;i++) {
			MsgObjPath p = (MsgObjPath)key[i];
			unSettleSetTreeSet.add(p);
			
		}		

		minInUnsettle = unSettleSetTreeSet.first().cost;
		boolean finish = false;		
		while (!finish) {
			MsgObjPath p = unSettleSetTreeSet.first();
			if (p.hasTouchObj()) {
				unSettleSetTreeSet.pollFirst();
				settleSet.add(p);				
			}
			else {
				finish = true;
			}
		}
		int num_k=0;
		if (settleSet.size() >= num_k) {
			return true;
		}
		else {
			return false;
		}
				
	}
    public ArrayList<MsgObjPath> getRoadKNN(int srcVID) {
    	Vertex srcV = GraphPool.getSignleton().getVertex(srcVID);
    	RNet leafR = RNetHierarchy.getSignleton().getRNet(srcV.getLeafRnetID());
    	TreeSet<Path> queue = new TreeSet<Path>();
    	TreeSet<MsgObjPath> settleSet = new TreeSet<MsgObjPath>();
    	TreeSet<MsgObjPath> unSettleSet = new TreeSet<MsgObjPath>();
    	
    	boolean finish = false;
    	
    	if (leafR.hasMsgObjInside()) {    		
    		Dijkstra dijkstra = new Dijkstra(leafR.getGraph());
    		dijkstra.execute(srcVID);
    		TreeSet<MsgObjPath> msgset = dijkstra.getPath2Objects(leafR);
    		    		
    		finish=doSettle(settleSet, unSettleSet, msgset);
    		
    		if (!finish) {
    		  TreeSet<MsgObjPath> borset = dijkstra.getPath2Borders(leafR);
    		  finish = doSettle(settleSet, unSettleSet, borset);
    		}    		  
    	}
    	else { 
    		Dijkstra dijkstra = new Dijkstra(leafR.getGraph());
    		dijkstra.execute(srcVID);
    		TreeSet<MsgObjPath> borset = dijkstra.getPath2Borders(leafR);
    		finish = doSettle(settleSet, unSettleSet, borset);
    		
    	}
    	
    	TreeSet<ChoosePathUnit> pqueue = new TreeSet<ChoosePathUnit>();
    	while (!finish) {
    		MsgObjPath first = unSettleSet.pollFirst();
    		int visitedLevel = first.currentVisitedRNetLevel;
    		int vID = first.getTailVID();
    		Vertex v = GraphPool.getSignleton().getVertex(vID);
    		if (v.getHighestBorderLevel() < 0) {
    			System.out.println("v must be a border");
    			System.exit(0);
    		}
    		
    		RNet highR = RNetHierarchy.getSignleton().getRNet(v.getInsideRNetID(v.getHighestBorderLevel()));
    		    		
    		if (!highR.isLeafRNet()) {
    			ChoosePathUnit unit = new ChoosePathUnit();
        		unit.rnet = highR.getRNetID();
        		unit.path = first.clone();
        		pqueue.add(unit);
    		}
    		else {
    			/*leaf, search silbing*/
    			List<Integer> nlist = v.getNeighbors();
    			for (int i=0;i<nlist.size();i++) {
    				Vertex anoV = GraphPool.getSignleton().getVertex(nlist.get(i));
    				
    			}
    		}
    		
    	}
    	
    	return null;
    }
    public ArrayList<VisitedObject> getKNN(int srcVID, int k) {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
    	int popsize=0;
    	while ((queue.size()>0)&&(kNNs.size()<k)) {
    		VisitedObject popObj = queue.pollFirst();
    		//popsize++;
    		if ((visitedN.contains(popObj.getVID()))||(visitedO.contains(popObj.getMsgID()))) {
    			continue;
    		}
    		if (popObj.getVID()>=0) {  //pop up object is a node
    			Vertex popVertex = GraphPool.getSignleton().getVertex(popObj.getVID());
    			LinkedList<Integer> msgObj = popVertex.getAssocMsgObj();
    			for (int i=0; i<msgObj.size(); i++) {
    				VisitedObject oneObj = new VisitedObject(-1,msgObj.get(i),popObj.getDist()); 
    				queue.add(oneObj);
    			}
    			ChoosePath(queue,popVertex,popObj.getDist(),visitedN,visitedO);
    			visitedN.add(popObj.getVID());
    		}
    		else { //pop up object is a msgObj
    			kNNs.add(popObj);
    			visitedO.add(popObj.getMsgID());
    		}
    	}
    	
		return kNNs;
    }
    private void ChoosePath(TreeSet<VisitedObject> queue, Vertex popVertex,
			double dist, HashSet<Integer> visitedN, HashSet<Integer> visitedO) {
		// TODO Auto-generated method stub
    	int leafRID = popVertex.getLeafRnetID();
    	RNet leafRnet = RNetHierarchy.getSignleton().getRNet(leafRID);
    	int leafLevel = leafRnet.getLevel();
    	if (popVertex.isBorderNode(leafLevel)) {
    		int highestLevel = popVertex.getHighestBorderLevel();
    		for (int i=highestLevel; i<=leafLevel; i++) {
    			int tmpRID = popVertex.getInsideRNetID(i);
    	    	RNet tmpRNet = RNetHierarchy.getSignleton().getRNet(tmpRID);
    	    	if (tmpRNet.hasMsgObjInside()) {
    	    		if (tmpRNet.isLeafRNet()) {
    	    			List<Integer> neighbors = popVertex.getNeighbors();
    	        		for (int j = 0; j<neighbors.size(); j++) {
    	        			int id = neighbors.get(j).intValue();
    	        			Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
    	        			double newDist = dist+e.getWeight();
    	        			VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
    	    				queue.add(oneObj);
    	        		}
    	    		}
    	    		continue;
    	    	}
    	    	else {
    	    		ShortCutSet scSet = tmpRNet.getShortCutSet(popVertex.getId());
    	    		if (scSet==null)
    	    			break;
    	    		HashMap<Integer,Path> scMap = scSet.getSCMap();
    	    		Iterator<Integer> scMIter = scMap.keySet().iterator();
    	    		while (scMIter.hasNext()) {
    	    			Integer destID = (Integer)scMIter.next();
    	    			double newDist = dist+scMap.get(destID).cost;
    	    			VisitedObject oneObj = new VisitedObject(destID.intValue(),-1,newDist); 
    	    			queue.add(oneObj);
    	    		}
    	    		List<Integer> neighbors = popVertex.getNeighbors();
	        		for (int j = 0; j<neighbors.size(); j++) {
	        			int id = neighbors.get(j).intValue();
	        			Vertex neiVertex = GraphPool.getSignleton().getVertex(id);
	        			if (neiVertex.getInsideRNetID(i)!=popVertex.getInsideRNetID(i)) {
	        				Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
    	        			double newDist = dist+e.getWeight();
    	        			VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
    	    				queue.add(oneObj);
	        			}
	        		}
    	    		break;
    	    	}
    		}
    	}
    	else {
    		List<Integer> neighbors = popVertex.getNeighbors();
    		for (int i = 0; i<neighbors.size(); i++) {
    			int id = neighbors.get(i).intValue();
    			Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
    			double newDist = dist+e.getWeight();
    			VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
				queue.add(oneObj);
    		}
    	}
		
	}
}
