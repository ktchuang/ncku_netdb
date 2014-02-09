package arbor.lbs.uqp.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.PointRTree;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class ObjectUpdate {
	PointRTree tree; 
	
	public ObjectUpdate(String networkFn, String hierFn, String msgFn, String hqnetFn) {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(networkFn);
		RNetHierarchy.loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		HQNetPool.loadHQNets(hqnetFn);
	}
	public ObjectUpdate(){
		
	}

	public double InsertBorder2RTree() {
		double maxK = 0;
		tree = new PointRTree();
		HashMap<Integer, HQNet> hMap = HQNetPool.getSignleton().getHQNet();
		Iterator<Integer> hMapIter = hMap.keySet().iterator();
		while (hMapIter.hasNext()) {
			HQNet hqNet = hMap.get((Integer)hMapIter.next());
			double kDist = hqNet.getMaxKDist();
			if (kDist>maxK) {
				maxK = kDist;
			}
			HashSet<Integer> bSet = hqNet.getBorderSet();
			Iterator<Integer> iter = bSet.iterator();
			while (iter.hasNext()) {
				Integer bID = (Integer)iter.next();
				Vertex v = GraphPool.getSignleton().getVertex(bID);
				Point loc = v.getloc();
				tree.insertData(bID, loc.getCoord(0), loc.getCoord(1));
			}
		}
		return maxK;
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
			// has bug below
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ObjectUpdate objUpdate = new ObjectUpdate(args[0],args[1],args[2],args[3]);
		double maxK = objUpdate.InsertBorder2RTree();
		System.out.println(maxK);
		

	}

}
