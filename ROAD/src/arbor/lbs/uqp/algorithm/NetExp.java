package arbor.lbs.uqp.algorithm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import arbor.foundation.time.ExecTimer;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.MsgObj;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class NetExp {
	//static int num_k = 5;
	ArrayList<Integer> queryList;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int num_k = Integer.valueOf(args[4]);
		double ranValue = Double.valueOf(args[5]);
		NetExp netExp = new NetExp(args[0],args[1],args[2],args[3]);
	   	ExecTimer timer = new ExecTimer();
    	timer.setStartTime("Network Expansion Performance");
    	int cases = 0;
    	for (int l=0; l<netExp.queryList.size(); l++) {
    		ArrayList<VisitedObject> results = netExp.getKNN(netExp.queryList.get(l), num_k);
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
    	System.out.println(cases);
		System.out.println("KNN Search Complete");
		/*
		int num = (int) (ranValue*1000);
		timer.setStartTime("Network Expansion Update");
		for (int p=0; p<num; p++) {
			netExp.updateObject();
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
	}
	
	public NetExp(String networkFn, String hierFn, String msgFn, String queryFn) throws IOException {
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
    public ArrayList<VisitedObject> getKNN(int srcVID, int k) {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
    	while ((queue.size()>0)&&(kNNs.size()<k)) {
    		VisitedObject popObj = queue.pollFirst();
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
    			ChoosePath(queue,popVertex,popObj.getDist());
    			visitedN.add(popObj.getVID());
    		}
    		else { //pop up object is a msgObj
    			kNNs.add(popObj);
    			visitedO.add(popObj.getMsgID());
    		}
    	}
    	
		return kNNs;
    }

	private void ChoosePath(TreeSet<VisitedObject> queue, Vertex popVertex, double dist) {
		// TODO Auto-generated method stub
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
