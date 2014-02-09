package arbor.lbs.upq.simulation;

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
import arbor.lbs.uqp.algorithm.ObjectUpdate;
import arbor.lbs.uqp.algorithm.VisitedObject;
import arbor.lbs.uqp.graph.dijkstra.Dijkstra;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.MsgObj;
import arbor.lbs.uqp.graph.util.MsgObjPath;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.PointRTree;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.ShortCutSet;
import arbor.lbs.uqp.graph.util.Vertex;

public class UPQCompactLazyUpdateTurnAround {
	//static int num_k = 5;
	ArrayList<Integer> queryList;
	ArrayList<Integer> updateList;
	static int maxBID = 0;
	static double maxKDist = 0;
	PointRTree tree = new PointRTree();
	static HashSet<Integer> checkBorders = new HashSet<Integer>();
	int recCnt = 0;
	
	
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
		int updateFreq = Integer.valueOf(args[6]);
		UPQCompactLazyUpdateTurnAround upq = new UPQCompactLazyUpdateTurnAround(args[0],args[1],args[2],args[3],args[4],args[8]);
		ExecTimer timer = new ExecTimer();
		timer.setStartTime("Index Performance");
		//int maxBID = upq.getBorderKNN(num_k);
		upq.compactHierarchy();
		System.out.println(timer.setEndTime());
		System.out.println("Index Complete");
		
		BufferedReader in = new BufferedReader(new FileReader(args[7]));
		String str;
		double[] arrivalT = new double[50000];
		int idx=0;
		while ((str = in.readLine()) != null) {
			double value = Double.valueOf(str);
			arrivalT[idx] = value/1000;
			idx++;
		}
		in.close();
		
    	int cases = 0,count = 0;
    	double currT = 0;
		double turnAroundT = 0;
		double totalExecT = 0;
		
		Random r = new Random();
		double maxTAT = 0, minTAT = 100;
		int querySize = upq.queryList.size();
		int cnt=0;
		for (int l=0; l<querySize; l++) {
    		timer.setNanoStartTime("Query Performance");
    		ArrayList<VisitedObject> results = upq.getKNN(upq.queryList.get(l),num_k);
    		if (results.size()!=num_k) 
        		cases++;
    		//***insert one object start
    		if ((l<3000)||(l>6000)) {
    			if (l%updateFreq==0) {
    				int vID = upq.updateList.get(cnt);
    				Vertex v = GraphPool.getSignleton().getVertex(vID);
    				upq.updateObject(vID);
    				double[] xy = new double[2];
    				xy[0] = v.getloc().getCoord(0);
    				xy[1] = v.getloc().getCoord(1);
    				Point insertObj = new Point(xy);
    				HashSet<Integer> updatedBorders = upq.GetUpdatedBorder(insertObj, maxKDist, maxBID);
    				checkBorders.addAll(updatedBorders);
    				cnt++;
    			}
    		}
    		else {
    			if (l%1==0) {
    				int vID = upq.updateList.get(cnt);
    				Vertex v = GraphPool.getSignleton().getVertex(vID);
    				upq.updateObject(vID);
    				double[] xy = new double[2];
    				xy[0] = v.getloc().getCoord(0);
    				xy[1] = v.getloc().getCoord(1);
    				Point insertObj = new Point(xy);
    				HashSet<Integer> updatedBorders = upq.GetUpdatedBorder(insertObj, maxKDist, maxBID);
    				checkBorders.addAll(updatedBorders);
    				cnt++;
    			}
    			
    		}
    		//***insert one object end
    		double execTime = timer.setNanoEndTime().getMillisSeconds();
    		
    		//System.out.println(currT+" "+arrivalT[l]+" "+execTime);
    		totalExecT += execTime;
    		if (arrivalT[l]>=currT) {
    			turnAroundT += execTime;
    			currT = arrivalT[l]+execTime;
    			if (execTime>maxTAT)
    				maxTAT = execTime;
    			if (execTime<minTAT)
    				minTAT = execTime;
    		}
    		else {
    			double waitingT = currT-arrivalT[l];
    			turnAroundT += waitingT;
    			turnAroundT += execTime;
    			currT = currT+execTime;
    			if (waitingT+execTime>maxTAT)
    				maxTAT = waitingT+execTime;
    			if (waitingT+execTime<minTAT)
    				minTAT = waitingT+execTime;
    		}
    		System.out.println(currT+" "+turnAroundT+" "+cnt);
    	}
		System.out.println(cnt);
		System.out.println(upq.recCnt);
		System.out.println("UPQ Compact Lazy Update Turn Around Time:"+turnAroundT);
    	System.out.println("UPQ Total Exec Time:"+totalExecT);
    	System.out.println(cases);
    	System.out.println("KNN Search Complete");
    	System.out.println("MAX Turn Around Time:"+maxTAT);
    	System.out.println("MIN Turn Around Time:"+minTAT);
    }
	
	private void compactHierarchy() {
		// TODO Auto-generated method stub
		int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		LinkedList<Integer> rnetLst = RNetHierarchy.getSignleton().getLevelRNet(maxLevel);
		
		for (int i=0; i<rnetLst.size(); i++) {
			RNet rnet = RNetHierarchy.getSignleton().getRNet(rnetLst.get(i));
			LinkedList<Integer> bLst = rnet.getBorderList();
			for (int j=0; j<bLst.size(); j++) {
				int bID = bLst.get(j);
				Vertex v = GraphPool.getSignleton().getVertex(bID);
				int tmpLevel = v.getHighestBorderLevel();
				if (tmpLevel==maxLevel) {
					v.setTopSearchLevel(maxLevel);
				}
				else {
					int rID = v.getInsideRNetID(tmpLevel);
					RNet tmpRNet = RNetHierarchy.getSignleton().getRNet(rID);
					if (tmpRNet.hasMsgObjInside()) {						
						int compact = tmpLevel;
						for (int x=tmpLevel; x<=maxLevel; x++) {
							int rnetID = v.getInsideRNetID(x);
							RNet currRNet = RNetHierarchy.getSignleton().getRNet(rnetID);
							compact = x;
							if (!currRNet.hasMsgObjInside()) {								
								break;
							}
						}
						v.setTopSearchLevel(compact);
					}
					else {
						v.setTopSearchLevel(tmpLevel);
					}
				}
			}
		}
	}
	
	private void updateObject(int vID) {
		// TODO Auto-generated method stub
		/*
		int size = GraphPool.getSignleton().getAllVertexes().size();
		Random r = new Random();
		int vID = r.nextInt(size);
		Vertex v = GraphPool.getSignleton().getVertex(vID);
		*/
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
	
	public HashSet<Integer> GetUpdatedBorder(Point insertObj, double deltaLen, int maxBID) {
		HashSet<Integer> updatedBorder = new HashSet<Integer>();
		LinkedList<Integer> candidates = new LinkedList<Integer>();
		double realDeltaLen = (deltaLen+1)/1000;  //in Road2GraphFile.java each edge is multipled by 1000
		candidates = tree.getDataInsideBBox(insertObj.getCoord(0), insertObj.getCoord(1), realDeltaLen, realDeltaLen);
		maxBID = -1;
		int index = -1;
		double preMax = deltaLen;
		while (candidates.size()>0) {
			/*
			if (candidates.contains(maxBID)) {
				updatedBorder.add(maxBID);
				candidates.remove(maxBID);
			}*/
			double newKDist = 0;
			index = -1;
			for (int i=0; i<candidates.size(); i++) {
				Vertex v = GraphPool.getSignleton().getVertex(candidates.get(i));
				int hqNetID = v.getHQNetID();
				if (hqNetID != -1) {
					HQNet hNet = HQNetPool.getSignleton().getHQNet(hqNetID);
					HashMap<Integer, ArrayList<VisitedObject>> knnMap = hNet.getBorderKNNMap();
					ArrayList<VisitedObject> preKNN = knnMap.get(v.getId());
					int nnNum = preKNN.size();
					VisitedObject kthObj = preKNN.get(nnNum-1);
					if (kthObj.getDist()>=preMax) {
						updatedBorder.add(v.getId());
						candidates.remove(i);
					}
					else if (kthObj.getDist()>newKDist) {
						newKDist = kthObj.getDist();
						maxBID = v.getId();
						index = i;
					}
				}
			}
			double realNewKDist=0;
			if (newKDist!=0) { 
				realNewKDist = (newKDist+1)/1000;  //in Road2GraphFile.java each edge is multipled by 1000
			}
			candidates = tree.getDataInsideBBox(insertObj.getCoord(0), insertObj.getCoord(1), realNewKDist, realNewKDist);
			preMax = newKDist;
			//remove the set of updateBorders from candidates
			candidates.removeAll(updatedBorder);
		}
		return updatedBorder;
	}
	
	public UPQCompactLazyUpdateTurnAround(String networkFn, String hierFn, String msgFn,
			String hqnetFn, String queryFn, String updateFn) throws IOException {
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
		in.close();
		
		updateList = new ArrayList<Integer>();
		in = new BufferedReader(new FileReader(updateFn));
		while ((str = in.readLine()) != null) {
			Integer vID = Integer.valueOf(str);
			updateList.add(vID);
		}
		in.close();
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
    	HashSet<Integer> bSet = hnet.getBorderSet();
    	
    	if (bKNNMap.containsKey(srcVID)) {
    		if (checkBorders.contains(srcVID)) {
    			ArrayList<VisitedObject> knns = getRoadKNN(srcVID, k);
        		hnet.insertBorderKnn(srcVID, knns);
        		int size = knns.size();
    			VisitedObject kthObj = knns.get(size-1);
    			if (kthObj.getDist()>maxKDist) {
    				maxKDist = kthObj.getDist();
    				maxBID = kthObj.getVID();
    			}
    			checkBorders.remove(srcVID);
    			recCnt++;
    		}
			ArrayList<VisitedObject> knns = bKNNMap.get(srcVID);
			NNs.addAll(knns);
			return NNs;
    	}
    	
    	if (bSet.contains(srcVID)) {
    		ArrayList<VisitedObject> knns = getRoadKNN(srcVID, k);
    		hnet.insertBorderKnn(srcVID, knns);
    		NNs.addAll(knns);
    		Vertex v = GraphPool.getSignleton().getVertex(srcVID);
    		Point loc = v.getloc();
			tree.insertData(srcVID, loc.getCoord(0), loc.getCoord(1));
			int size = knns.size();
			VisitedObject kthObj = knns.get(size-1);
			if (kthObj.getDist()>maxKDist) {
				maxKDist = kthObj.getDist();
				maxBID = kthObj.getVID();
			}
			return NNs;
    	}
    	
    	NNs = getSearchRoadKNN(srcVID,k);
    	/*
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
    	*/

		return NNs;
	}
    private void calculateBKNN(HQNet hnet, int k) {
		// TODO Auto-generated method stub
    	hnet.initBuildHQNet(false);
		Iterator<Integer> borderIter = hnet.getBorderSet().iterator();
		while (borderIter.hasNext()) {
			Integer bID = (Integer)borderIter.next();
			ArrayList<VisitedObject> knns = getRoadKNN(bID, k);
			hnet.insertBorderKnn(bID, knns);
			/*
			double kValue = 0;
			if (knns.size()>0) {
				kValue = knns.get(knns.size()-1).getDist();
			}
			if (kValue>maxK) {
				maxK = kValue;
				maxBID = bID;
			}
			*/
		}
		/*hnet.insertMaxKDist(maxK);
		if (maxK>globalMax) {
			globalMax = maxK;
			globalMaxBID = maxBID;
		}
		*/
		
	}

	public ArrayList<VisitedObject> getSearchRoadKNN(int srcVID, int k) {
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
    			ChooseSearchPath(queue,popVertex,popObj.getDist(),visitedN,visitedO,k);
    			visitedN.add(popObj.getVID());
    		}
    		else { //pop up object is a msgObj
    			kNNs.add(popObj);
    			visitedO.add(popObj.getMsgID());
    		}
    	}
    	
		return kNNs;
    }
    private void ChooseSearchPath(TreeSet<VisitedObject> queue, Vertex popVertex,
			double dist, HashSet<Integer> visitedN, HashSet<Integer> visitedO,int k) {
		// TODO Auto-generated method stub
    	int hqNetID = popVertex.getHQNetID();
    	if (hqNetID!=-1) {
    		HQNet hnet = HQNetPool.getSignleton().getHQNet(hqNetID);
    		HashSet<Integer> borderSet = hnet.getBorderSet();
    		int vID = popVertex.getId();
    		if (borderSet.contains(vID)) {
    			HashMap<Integer, ArrayList<VisitedObject>> bKNNMap = hnet.getBorderKNNMap();
            	if (!bKNNMap.containsKey(vID)) {
            		ArrayList<VisitedObject> knns = getRoadKNN(vID, k);
            		hnet.insertBorderKnn(vID, knns);
            		for (int m=0; m<knns.size(); m++) {
        				knns.get(m).addDist(dist);
        				queue.add(knns.get(m));
        			}
        			Vertex v = GraphPool.getSignleton().getVertex(vID);
            		Point loc = v.getloc();
        			tree.insertData(vID, loc.getCoord(0), loc.getCoord(1));
        			int size = knns.size();
        			VisitedObject kthObj = knns.get(size-1);
        			if (kthObj.getDist()>maxKDist) {
        				maxKDist = kthObj.getDist();
        				maxBID = kthObj.getVID();
        			}
            	}
            	else {
            		if (checkBorders.contains(vID)) {
            			ArrayList<VisitedObject> knns = getRoadKNN(vID, k);
                		hnet.insertBorderKnn(vID, knns);
                		int size = knns.size();
            			VisitedObject kthObj = knns.get(size-1);
            			if (kthObj.getDist()>maxKDist) {
            				maxKDist = kthObj.getDist();
            				maxBID = kthObj.getVID();
            			}
            			checkBorders.remove(vID);
            			recCnt++;
            		}
            		ArrayList<VisitedObject> knns = bKNNMap.get(vID);
        			for (int m=0; m<knns.size(); m++) {
        				knns.get(m).addDist(dist);
        				queue.add(knns.get(m));
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
    		int highestLevel = popVertex.getTopSearchLevel();
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
