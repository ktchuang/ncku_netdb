package arbor.lbs.upq.simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
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
import arbor.lbs.uqp.graph.util.Graph;
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

public class UPQTurnAroundPNew {
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
		int num_k = Integer.valueOf(args[4]);
		double ranValue = Double.valueOf(args[5]);
		int factor = Integer.valueOf(args[8]);
    	int winSize = Integer.valueOf(args[9]);
    	int intputSize = Integer.valueOf(args[10]);
		UPQTurnAroundPNew upq = new UPQTurnAroundPNew(args[0],args[1],args[2],args[3]);
		ExecTimer timer = new ExecTimer();
		timer.setStartTime("Index Performance");
		//int maxBID = upq.getBorderKNN(num_k);
		//upq.compactHierarchy();
		System.out.println(timer.setEndTime());
		System.out.println("Index Complete");
		
		BufferedReader in = new BufferedReader(new FileReader(args[6]));
		String str;
		double[] arrivalT = new double[105000];
		int idx=0;
		while ((str = in.readLine()) != null) {
			double value = Double.valueOf(str);
			arrivalT[idx] = value/factor;
			idx++;
		}
		in.close();
		
    	int cases = 0,count = 0;
    	double currT = 0;
		double turnAroundT = 0;
		double totalExecT = 0;
		int hnetNum = 0;
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(args[7])));
		
		HashMap<Integer, Integer> hqNetCnt = new HashMap<Integer, Integer>();
		HashSet<Integer> prevRIDSet = new HashSet<Integer>();
		
		HashMap<Integer,Integer> TATMap = new HashMap<Integer,Integer>();
		
		double maxTAT = 0, minTAT = 100;
		int querySize = upq.queryList.size();
		double prevArrT = 0;
		double averageTAT = 0;
		int averageCnt = 0;
		double tmpMaxTAT = 0;
		double tmpMinTAT = 1;
		for (int l=0; l<intputSize; l++) {
    		timer.setNanoStartTime("Query Performance");
    		ArrayList<VisitedObject> results = upq.getKNN(upq.queryList.get(l),num_k);
    		double execTime = timer.setNanoEndTime().getMillisSeconds();
    		if (l==5000) {
    			totalExecT = 0;
    			turnAroundT = 0;
    		}
    		//execTime /= 1000;
    		//System.out.println(currT+" "+arrivalT[l]+" "+execTime);
    		totalExecT += execTime;
    		int oneTAT = 0;
    		//execTime = 0.0003;//temp
    		double printTAT = 0;
    		//System.out.println(currT+" "+arrivalT[l]+" "+execTime);
    		if (arrivalT[l]>=currT) {
    			turnAroundT += execTime;
    			currT = arrivalT[l]+execTime;
    			if (execTime>maxTAT)
    				maxTAT = execTime;
    			if (execTime<minTAT)
    				minTAT = execTime;
    			oneTAT = (int) (execTime*10000);
    			printTAT = execTime;
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
    			double tmp = waitingT+execTime;
    			oneTAT = (int) (tmp*10000);
    			printTAT =  waitingT+execTime;
    		}
    		
    		if (!TATMap.containsKey(oneTAT)) {
    			TATMap.put(oneTAT, 1);
			}
			else {
				int cCount = TATMap.get(oneTAT);
				cCount++;
				TATMap.put(oneTAT, cCount);
			}
    		
    		if (results.size()!=num_k) 
    			cases++;
 
    		int srcVID = upq.queryList.get(l);
    		Vertex v = GraphPool.getSignleton().getVertex(srcVID);
    		int leafRID = v.getLeafRnetID();
    		if (hqNetCnt.containsKey(leafRID)) {
    			int cnt = hqNetCnt.get(leafRID);
    			cnt++;
    			hqNetCnt.put(leafRID, cnt);
    		}
    		else {
    			hqNetCnt.put(leafRID, 1);
    		}
    		
    		if (l%1000==999) {
    			out.write("UPQ Turn Around Time:"+turnAroundT+"\n");
    			//out.write("UPQ Compact Exec Time:"+totalExecT+"\n");
    			//System.out.println("UPQ Total Exec Time:"+totalExecT);
    		}
    		
    		if (l==(winSize-1)) {
    			
    			// HQNet border size vs. HQNet node size
    			int size = 0, totalSize = 0;
    			Iterator<Integer> hqNetMapIter = HQNetPool.getSignleton().getHQNet().keySet().iterator();
    			while (hqNetMapIter.hasNext()) {
    				int hqID = (Integer)hqNetMapIter.next();
    				HQNet hqnet= HQNetPool.getSignleton().getHQNet(hqID);
    				size += hqnet.getBorderKNNMap().size();
    				totalSize += hqnet.getAllVertexes().size();
    			}
    			//System.out.println(HQNetPool.getSignleton().getHQNet().size());
    			//System.out.println(size);
    			//System.out.println(totalSize);
    			
    			
    			//System.out.println("UPQ Compact Turn Around Time:"+turnAroundT);
    	    	//System.out.println("UPQ Total Exec Time:"+totalExecT);
    	    	HashSet<Integer> rIDSet = new HashSet<Integer>();
    	    	
    	    	Iterator<Integer> hqNetCntIter= hqNetCnt.keySet().iterator();
    	    	int threshold = winSize/hqNetCnt.size();
    	    	//System.out.println(threshold);
    	    	while (hqNetCntIter.hasNext()) {
    	    		int rID = (Integer)hqNetCntIter.next();
    	    		int num = hqNetCnt.get(rID);
    	    		if (num>=threshold) { // frequent query
    	    			rIDSet.add(rID);
    	    		}
    	    	}
    	    	//System.out.println(hqNetCnt.size());
    	    	//System.out.println(rIDSet.size());
    	    	HashSet<Integer> addRIDSet = new HashSet<Integer>();
    	    	HashSet<Integer> removeRIDSet = new HashSet<Integer>();
    	    	HashSet<Integer> sameRIDSet = new HashSet<Integer>();
    	    	if (!prevRIDSet.isEmpty()) {
    	    		addRIDSet.addAll(rIDSet);
    	    		addRIDSet.removeAll(prevRIDSet);
    	    		removeRIDSet.addAll(prevRIDSet);
    	    		removeRIDSet.removeAll(rIDSet);
    	    	}
    	    	else {
    	    		addRIDSet.addAll(rIDSet);
    	    	}
    	    	sameRIDSet.addAll(rIDSet);
    	    	sameRIDSet.retainAll(prevRIDSet);
    	    	System.out.println(rIDSet.size()+" "+sameRIDSet.size());
    	    	//System.out.println(addRIDSet.size()+" "+removeRIDSet.size());
    	    	
    	    	Iterator<Integer> rIDIter = addRIDSet.iterator();
    	    	while (rIDIter.hasNext()) {
    	    		int rID = rIDIter.next();
    	    		//insert nodes to subgraph
    	    		Graph subgraph = new Graph();
    	    		List<Integer> allVertex = RNetHierarchy.getSignleton().getRNet(rID).getAllVertexes();
    	    		Iterator<Integer> allVIter = allVertex.iterator();
    	    		while(allVIter.hasNext()) {
    	    			Integer vertexID = (Integer)allVIter.next();
    	    			subgraph.insertVertex(vertexID);
    	    		}
    	    		List<Integer> subgraphNodes = subgraph.getVertexes();
    				for (int j=0; j<subgraphNodes.size(); j++) {
    					v = GraphPool.getSignleton().getVertex(subgraphNodes.get(j));
    					List<Integer> neighbors = v.getNeighbors();
    					for (int k=0; k<neighbors.size(); k++) {
    						Edge e = GraphPool.getSignleton().getEdge(v.getId(), neighbors.get(k).intValue());
    						subgraph.insertEdge(e.getId());
    					}
    				}
    				HQNet hnet = new HQNet(hnetNum, rID, subgraph);
    				boolean containObj = false;
    				allVertex = subgraph.getVertexes();
    				for (int p=0; p<allVertex.size(); p++) {
    					Vertex tmp = GraphPool.getSignleton().getVertex(allVertex.get(p));
    					tmp.setHQNetID(hnetNum);
    					if (tmp.hasAssocMsgObj()) {
    						containObj = true;
    					}
    				}
    				hnet.setContainObj(containObj);
    				hnetNum++;
    				//insert to HQNetPool
    				HQNetPool.getSignleton().insertHQNet(hnet);
    	    	}
    	    	Iterator<Integer> rrIDIter = removeRIDSet.iterator();
    	    	while (rrIDIter.hasNext()) {
    	    		int rID = rrIDIter.next();
    	    		HashMap<Integer, HQNet> hqNetMap = HQNetPool.getSignleton().getHQNet();
    	    		Iterator<Integer> hqNetIter = hqNetMap.keySet().iterator();
    	    		while (hqNetIter.hasNext()) {
    	    			int hqID = (Integer)hqNetIter.next();
    	    			HQNet tmpHNet= hqNetMap.get(hqID);
    	    			if (tmpHNet.getParent() ==rID) {
    	    				HQNet removeHQNet = hqNetMap.get(hqID);
    	    				List<Integer> allV = removeHQNet.getAllVertexes();
    	    				for (int p=0; p<allV.size(); p++) {
    	    					Vertex tmp = GraphPool.getSignleton().getVertex(allV.get(p));
    	    					tmp.setHQNetID(-1);
    	    				}
    	    				hqNetMap.remove(hqID);
    	    				break;
    	    			}
    	    		}
    	    	}    	    	
    	    	prevRIDSet.clear();
    	    	prevRIDSet.addAll(rIDSet);
    	    	hqNetCnt.clear();
    	    	
    	    	Iterator<Integer> itr = HQNetPool.getSignleton().getHQNet().keySet().iterator();
    			while (itr.hasNext()) {
    				int hqID = itr.next();
    				HQNet hqNet = HQNetPool.getSignleton().getHQNet(hqID);
    				int vID = hqNet.getAllVertexes().get(0);
    				v = GraphPool.getSignleton().getVertex(vID);
    				int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
    				for (int i=0; i<=maxLevel; i++) {
    					int rID = v.getInsideRNetID(i);
    					RNet rnet = RNetHierarchy.getSignleton().getRNet(rID);
    					rnet.setContainHQNet(true);  
    				}
    			}
    			
    			upq.getBorderKNN(num_k);
    		}
    		
    	}
		out.close();
		System.out.println("UPQ Turn Around Time:"+turnAroundT);
    	System.out.println("UPQ Total Exec Time:"+totalExecT);
    	System.out.println(cases);
    	System.out.println("KNN Search Complete");
    	System.out.println("MAX Turn Around Time:"+maxTAT);
    	System.out.println("MIN Turn Around Time:"+minTAT);
    	/*
    	Iterator<Integer> iter = TATMap.keySet().iterator();
		while (iter.hasNext()) {
			int TATID = (Integer)iter.next();
			int cnt = TATMap.get(TATID);
			System.out.println(TATID+" "+cnt);
		}*/
    	for (int i=0; i<11500; i++) {
    		int cnt = 0;
    		if (TATMap.containsKey(i))
    			cnt = TATMap.get(i);
    		else
    			cnt = 0;
    		//System.out.println(i+" "+cnt);
    	}
    	System.gc();
    }
	
	public int getBorderKNN(int k) throws IOException {
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
	public UPQTurnAroundPNew(String networkFn, String hierFn, String msgFn, String queryFn) throws IOException {
		// TODO Auto-generated constructor stub
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
	/*
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
	}*/
	
    public ArrayList<VisitedObject> getKNN(int srcVID, int k) throws IOException {
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
    
    private ArrayList<VisitedObject> getUPKNN(int srcVID, int k, int hqNetID) throws IOException {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> NNs = new ArrayList<VisitedObject>();
    	//ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	HQNet hnet = HQNetPool.getSignleton().getHQNet(hqNetID);
    	if (hnet==null) 
    		System.out.println(hqNetID);
    	HashMap<Integer, ArrayList<VisitedObject>> bKNNMap = hnet.getBorderKNNMap();
    	
    	if (bKNNMap.containsKey(srcVID)) {
			ArrayList<VisitedObject> knns = bKNNMap.get(srcVID);
			NNs.addAll(knns);
			return NNs;
    	}
    	
    	
    	HashSet<Integer> borderSet = hnet.getBorderSet();
    	/*
    	if (bKNNMap.size()==borderSet.size()) {
    		NNs = getSearchRoadKNN(srcVID,k);
    	}
    	else {
    		NNs = getRoadKNN(srcVID,k);
    	}
    	*/
    	NNs = getSearchRoadKNN(srcVID,k);
    	if (borderSet.contains(srcVID)) {
    		hnet.insertBorderKnn(srcVID, NNs);
    	}
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
   /* 
    private void calculateBKNN(HQNet hnet, int k) {
		// TODO Auto-generated method stub
    	hnet.initBuildHQNet(false);
		Iterator<Integer> borderIter = hnet.getBorderSet().iterator();
		while (borderIter.hasNext()) {
			Integer bID = (Integer)borderIter.next();
			ArrayList<VisitedObject> knns = getRoadKNN(bID, k);
			hnet.insertBorderKnn(bID, knns);
			
		}
	}*/

	public ArrayList<VisitedObject> getSearchRoadKNN(int srcVID, int k) throws IOException {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	int expNodeCnt = 0;
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
    	int popsize=0;
    	while ((queue.size()>0)&&(kNNs.size()<k)) {
    		/*
    		if (expNodeCnt%100==0) { //simulate reading data from page
    			String input = "test.txt";
    			BufferedReader in = new BufferedReader(new FileReader(input));
    			String str;
    			while ((str = in.readLine()) != null) {
    				int lng = str.length();
    				if (lng==0)
    					System.out.println("The string is empty.");
    				
    			}
    			in.close();
    		}*/
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
    			ChooseSearchPathNew(queue,popVertex,popObj.getDist(),visitedN,visitedO,k);
    			expNodeCnt++;
    			visitedN.add(popObj.getVID());
    		}
    		else { //pop up object is a msgObj
    			kNNs.add(popObj);
    			visitedO.add(popObj.getMsgID());
    		}
    	}
    	
		return kNNs;
    }
	
	private void ChooseSearchPathNew(TreeSet<VisitedObject> queue, Vertex popVertex,
			double dist, HashSet<Integer> visitedN, HashSet<Integer> visitedO,int k) {
		// TODO Auto-generated method stub	
    	int hqNetID = popVertex.getHQNetID();
    	if (hqNetID!=-1) {
    		HQNet hnet = HQNetPool.getSignleton().getHQNet(hqNetID);
    		int vID = popVertex.getId();
    		HashMap<Integer, ArrayList<VisitedObject>> bKNNMap = hnet.getBorderKNNMap();
            if (bKNNMap.containsKey(vID)) {
            	ArrayList<VisitedObject> knns = bKNNMap.get(vID);
        		for (int m=0; m<knns.size(); m++) {
        			knns.get(m).addDist(dist);
        			queue.add(knns.get(m));
        		}
        		return ;
    		}
    	}
    	ChoosePath(queue,popVertex,dist,visitedN,visitedO);
	}
	
    private void ChooseSearchPath(TreeSet<VisitedObject> queue, Vertex popVertex,
			double dist, HashSet<Integer> visitedN, HashSet<Integer> visitedO,int k) {
		// TODO Auto-generated method stub	
    	int hqNetID = popVertex.getHQNetID();
    	if (hqNetID!=-1) {
    		HQNet hnet = HQNetPool.getSignleton().getHQNet(hqNetID);
    		int vID = popVertex.getId();
    		HashMap<Integer, ArrayList<VisitedObject>> bKNNMap = hnet.getBorderKNNMap();
            if (!bKNNMap.containsKey(vID)) {
            	//precomputed knn of a border if the query pass through the border
            	/*
            	ArrayList<VisitedObject> knns = getRoadKNN(vID, k);
            	hnet.insertBorderKnn(vID, knns);
            	for (int m=0; m<knns.size(); m++) {
        			knns.get(m).addDist(dist);
        			queue.add(knns.get(m));
        		}*/
            		
            	List<Integer> neighbors = popVertex.getNeighbors();
            	for (int i = 0; i<neighbors.size(); i++) {
            		int id = neighbors.get(i).intValue();
            		Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), id);
            		double newDist = dist+e.getWeight();
            		VisitedObject oneObj = new VisitedObject(id,-1,newDist); 
        			queue.add(oneObj);
            	}
        			
            }
            else {
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
    
    public ArrayList<VisitedObject> getRoadKNN(int srcVID, int k) throws IOException {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	int expNodeCnt = 0;
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
    	int popsize=0;
    	while ((queue.size()>0)&&(kNNs.size()<k)) {
    		/*
    		if (expNodeCnt%100==0) { //simulate reading data from page
    			String input = "test.txt";
    			BufferedReader in = new BufferedReader(new FileReader(input));
    			String str;
    			while ((str = in.readLine()) != null) {
    				int lng = str.length();
    				if (lng==0)
    					System.out.println("The string is empty.");
    				
    			}
    			in.close();
    		}*/
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
    			expNodeCnt++;
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
    		//int highestLevel = popVertex.getTopSearchLevel();
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
    	    	else if (tmpRNet.hasHQNetInside()) {
    	    		if (tmpRNet.isLeafRNet()) {
    	    			int vID = popVertex.getId();
    	    			int hqNetID = popVertex.getHQNetID();
    	    			HQNet hnet = HQNetPool.getSignleton().getHQNet(hqNetID);
    	    			HashMap<Integer, ArrayList<VisitedObject>> bKNNMap = hnet.getBorderKNNMap();
    	                if (bKNNMap.containsKey(vID)) {
    	                	ArrayList<VisitedObject> knns = bKNNMap.get(vID);
    	            		for (int m=0; m<knns.size(); m++) {
    	            			knns.get(m).addDist(dist);
    	            			queue.add(knns.get(m));
    	            		}
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
