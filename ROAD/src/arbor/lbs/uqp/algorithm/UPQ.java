package arbor.lbs.uqp.algorithm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import arbor.foundation.time.ExecTimer;
import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.uqp.graph.dijkstra.Dijkstra;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.MsgObj;
import arbor.lbs.uqp.graph.util.MsgObjPath;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.ShortCutSet;
import arbor.lbs.uqp.graph.util.Vertex;

public class UPQ {
	//static int num_k = 5;
	ArrayList<Integer> queryList;
	
	public static void main(String[] args) throws IOException {
    	/**
    	 * Code sequence:
    	 * 1. load graph pool (serialize)
    	 * 2. load rnet hierarchy (serialize)
    	 * 3. load object pool
    	 * 4. load hot query net pool (serialie) and calculate knn of borders in hot query net
    	 * 5. check knn sequentially
    	 */
		int num_k = Integer.valueOf(args[5]);
		double ranValue = Double.valueOf(args[6]);
		UPQ upq = new UPQ(args[0],args[1],args[2],args[3],args[4]);
		ExecTimer timer = new ExecTimer();
		timer.setStartTime("Index Performance");
		int maxBID = upq.getBorderKNN(num_k);
		//***object update filtering start
		ObjectUpdate objUpdate = new ObjectUpdate();
		double returnMax = objUpdate.InsertBorder2RTree();
		Vertex v = GraphPool.getSignleton().getVertex(upq.queryList.get(0));
		double[] xy = new double[2];
		xy[0] = v.getloc().getCoord(0);
		xy[1] = v.getloc().getCoord(1);
		Point insertObj = new Point(xy);
		HashSet<Integer> candidates = objUpdate.GetUpdatedBorder(insertObj, returnMax, maxBID);
		//***object update filtering end
		System.out.println(timer.setEndTime());
		System.out.println("Index Complete");
    	int cases = 0,count = 0;
    	timer.setStartTime("UPQ Query Performance");
    	Random r = new Random();
    	for (int l=0; l<upq.queryList.size(); l++) {
    		ArrayList<VisitedObject> results = upq.getKNN(upq.queryList.get(l),num_k);
    		//Vertex v = GraphPool.getSignleton().getVertex(upq.queryList.get(l));
        	//int hqNetID = v.getHQNetID();
        	//if (hqNetID!=-1)
        	//	count++;
    		if (results.size()!=num_k) 
    			cases++;
    		/*
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
    	System.out.println(count);
    	System.out.println(cases);
    	System.out.println("KNN Search Complete");
    	/*
    	int num = (int) (ranValue*1000);
		timer.setStartTime("UPQ Update");
		for (int p=0; p<num; p++) {
			upq.updateObject();
			double ranDouble = r.nextDouble();
			if (ranDouble<0.1)
				upq.getRoadKNN(1, num_k);
		}
		System.out.println(timer.setEndTime());
		*/
    	//HQNetPool.getSignleton().saveHQNets(args[3]);
		/*
		while (iter.hasNext()){
			Integer hID = (Integer)iter.next();
			HQNet hqNet = hMap.get(hID);
			if (hqNet.hasMsgObjInside()) {
				List<Integer> vertexList = hqNet.getAllVertexes();
				for (int m=0; m<vertexList.size(); m++) {
					ArrayList<VisitedObject> results = upq.getKNN(vertexList.get(m),5);
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
		    	}
			}
		}*/
		/*
		HQNet hnet = HQNetPool.getSignleton().getHQNet(1);
		HashSet<Integer> bSet = hnet.getBorderSet();
		Iterator<Integer> it = bSet.iterator();
		Integer tmp = (Integer)it.next();
		//List<Integer> vertexList = hnet.getAllVertexes();
		//for (int m=0; m<vertexList.size(); m++) {
			ArrayList<VisitedObject> results = upq.getKNN(tmp,5);
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
    	//}*/
    }
	private Vertex updateObject() {
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
		return v;
	}
	public UPQ(String networkFn, String hierFn, String msgFn, String hqnetFn, String queryFn) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(networkFn);
		RNetHierarchy.loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		HQNetPool.loadHQNets(hqnetFn);
		
		queryList = new ArrayList<Integer>();
		BufferedReader in = new BufferedReader(new FileReader(queryFn));
		String str;
		while ((str = in.readLine()) != null) {
			Integer vID = Integer.valueOf(str);
			queryList.add(vID);
		}
	}
	public int getBorderKNN(int k) {
		int borderCnt = 0;
		HashMap<Integer, HQNet> hMap = HQNetPool.getSignleton().getHQNet();
		Iterator <Integer> hMapIter = hMap.keySet().iterator();
		double globalMax = 0;
		int globalMaxBID = 0;
		while (hMapIter.hasNext()) {
			double maxK = 0;
			int maxBID = 0;
			Integer hID = (Integer)hMapIter.next();
			HQNet hNet = hMap.get(hID);
			hNet.initBuildHQNet(false);
			Iterator<Integer> borderIter = hNet.getBorderSet().iterator();
			while (borderIter.hasNext()) {
				borderCnt++;
				Integer bID = (Integer)borderIter.next();
				ArrayList<VisitedObject> knns = getRoadKNN(bID, k);
				hNet.insertBorderKnn(bID, knns);
				double kValue = 0;
				if (knns.size()>0) {
					kValue = knns.get(knns.size()-1).getDist();
				}
				if (kValue>maxK) {
					maxK = kValue;
					maxBID = bID;
				}
			}
			hNet.insertMaxKDist(maxK);
			if (maxK>globalMax) {
				globalMax = maxK;
				globalMaxBID = maxBID;
			}
		}
		System.out.println(borderCnt);
		return globalMaxBID;
	}
	
    public ArrayList<VisitedObject> getKNN(int srcVID, int k) {
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	
    	Vertex v = GraphPool.getSignleton().getVertex(srcVID);
    	int hqNetID = v.getHQNetID();
    	if (hqNetID!=-1) {
    		kNNs = getUPKNN(srcVID, k, hqNetID);
    	}
    	else {
    		kNNs = getRoadKNN(srcVID, k);
    	}
		return kNNs;
    }
    
    private ArrayList<VisitedObject> getUPKNN(int srcVID, int k, int hqNetID) {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> NNs = new ArrayList<VisitedObject>();
    	//ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	HQNet hnet = HQNetPool.getSignleton().getHQNet(hqNetID);
    	HashMap<Integer, ArrayList<VisitedObject>> bKNNMap = hnet.getBorderKNNMap();
    	
    	if (bKNNMap.containsKey(srcVID)) {
			ArrayList<VisitedObject> knns = bKNNMap.get(srcVID);
			NNs.addAll(knns);
			return NNs;
    	}
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
    	int popsize = 0;
    	while ((queue.size()>0)&&(NNs.size()<k)) {
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
    			
    			List<Integer> neighbors = popVertex.getNeighbors();
        		for (int j = 0; j<neighbors.size(); j++) {
        			int id = neighbors.get(j).intValue();
        			if (hnet.containVertex(id)) {
        				Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
        				double newDist = popObj.getDist()+e.getWeight();
        				VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
        				queue.add(oneObj);
        			}
        		}
        		if (bKNNMap.containsKey(popVertex.getId())) {
        			ArrayList<VisitedObject> knns = bKNNMap.get(popVertex.getId());
        			for (int m=0; m<knns.size(); m++) {
        				knns.get(m).addDist(popObj.getDist());
        				queue.add(knns.get(m));
        			}
        		}
    			visitedN.add(popObj.getVID());
    		}
    		else { //pop up object is a msgObj
    			NNs.add(popObj);
    			visitedO.add(popObj.getMsgID());
    		}
    	}
    	//System.out.println(popsize);
    	/*
    	int nnCount = 0;
    	if(NNs.size()>= k) {
    		nnCount = k;
    	}
    	else {
    		nnCount = NNs.size();
    	}
    	for (int n=0; n<nnCount; n++) {
    		kNNs.add(NNs.get(n));
    	}
    	*/
		return NNs;
	}
    public ArrayList<VisitedObject> getRoadKNN(int srcVID, int k) {
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
