package arbor.lbs.upq.simulation;

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
import arbor.lbs.uqp.algorithm.VisitedObject;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.MsgObj;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class NetExpTurnAroundPTAT {
	//static int num_k = 5;
	ArrayList<Integer> queryList;
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		int num_k = Integer.valueOf(args[4]);
		double ranValue = Double.valueOf(args[5]);
		int factor = Integer.valueOf(args[7]);
    	int winSize = Integer.valueOf(args[8]);
    	int intputSize = Integer.valueOf(args[9]);
		NetExpTurnAroundPTAT netExp = new NetExpTurnAroundPTAT(args[0],args[1],args[2],args[3]);
		
		BufferedReader in = new BufferedReader(new FileReader(args[6]));
		String str;
		double[] arrivalT = new double[110000];
		int idx=0;
		while ((str = in.readLine()) != null) {
			double value = Double.valueOf(str);
			//arrivalT[idx] = value/factor;
			arrivalT[idx] = value;
			idx++;
		}
    	in.close();
    	
    	double currT_1 = 0;
    	double currT_2 = 0;
    	double currT_3 = 0;
    	double currT_4 = 0;
    	double totalExecT = 0;
		double turnAroundT_1 = 0;
		double turnAroundT_2 = 0;
		double turnAroundT_3 = 0;
		double turnAroundT_4 = 0;
		int cases = 0;
    	
    	double maxTAT = 0, minTAT = 100;
    	ExecTimer timer = new ExecTimer();
    	int querySize = netExp.queryList.size();
	   	for (int l=0; l<intputSize; l++) {
        	timer.setNanoStartTime("Query Performance");
    		ArrayList<VisitedObject> results = netExp.getKNN(netExp.queryList.get(l), num_k);
    		double execTime = timer.setNanoEndTime().getMillisSeconds();
    		if (l==10000) {
    			totalExecT = 0;
    			turnAroundT_1 = 0;
    			turnAroundT_2 = 0;
    			turnAroundT_3 = 0;
    			turnAroundT_4 = 0;
    		}
    		//System.out.println(currT+" "+arrivalT[l]+" "+execTime);
    		totalExecT += execTime;
    		//System.out.println(currT+" "+arrivalT[l]+" "+execTime);
    		double aT_1 = arrivalT[l]/500;
    		double aT_2 = arrivalT[l]/1000;
    		double aT_3 = arrivalT[l]/5000;
    		double aT_4 = arrivalT[l]/10000;
    		if (aT_1>=currT_1) {
    			turnAroundT_1 += execTime;
    			currT_1 = aT_1+execTime;
    			if (execTime>maxTAT)
    				maxTAT = execTime;
    			if (execTime<minTAT)
    				minTAT = execTime;
    		}
    		else {
    			double waitingT = currT_1-aT_1;
    			turnAroundT_1 += waitingT;
    			turnAroundT_1 += execTime;
    			currT_1 = currT_1+execTime;
    			if (waitingT+execTime>maxTAT)
    				maxTAT = waitingT+execTime;
    			if (waitingT+execTime<minTAT)
    				minTAT = waitingT+execTime;
    		}
    		if (aT_2>=currT_2) {
    			turnAroundT_2 += execTime;
    			currT_2 = aT_2+execTime;
    		}
    		else {
    			double waitingT = currT_2-aT_2;
    			turnAroundT_2 += waitingT;
    			turnAroundT_2 += execTime;
    			currT_2 = currT_2+execTime;
    		}
    		if (aT_3>=currT_3) {
    			turnAroundT_3 += execTime;
    			currT_3 = aT_3+execTime;
    		}
    		else {
    			double waitingT = currT_3-aT_3;
    			turnAroundT_3 += waitingT;
    			turnAroundT_3 += execTime;
    			currT_3 = currT_3+execTime;
    		}
    		if (aT_4>=currT_4) {
    			turnAroundT_4 += execTime;
    			currT_4 = aT_4+execTime;
    		}
    		else {
    			double waitingT = currT_4-aT_4;
    			turnAroundT_4 += waitingT;
    			turnAroundT_4 += execTime;
    			currT_4 = currT_4+execTime;
    		}
    		if (results.size()!=num_k) 
    			cases++;
    		/*
    		if (l%10000==9999) {
    			System.out.println("NetExp Turn Around Time:"+turnAroundT);
    	    	System.out.println("NetExp Total Exec Time:"+totalExecT);
    		}
    		*/
    	}
	   	
    	System.out.println("NetExp Turn Around Time1:"+turnAroundT_1);
    	System.out.println("NetExp Turn Around Time2:"+turnAroundT_2);
    	System.out.println("NetExp Turn Around Time3:"+turnAroundT_3);
    	System.out.println("NetExp Turn Around Time4:"+turnAroundT_4);
    	System.out.println("NetExp Total Exec Time:"+totalExecT);
    	System.out.println(cases);
    	System.out.println("KNN Search Complete");
    	System.out.println("MAX Turn Around Time:"+maxTAT);
    	System.out.println("MIN Turn Around Time:"+minTAT);
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
	
	public NetExpTurnAroundPTAT(String networkFn, String hierFn, String msgFn, String queryFn) throws IOException {
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
		in.close();
	}
    public ArrayList<VisitedObject> getKNN(int srcVID, int k) throws IOException {
    	TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
    	ArrayList<VisitedObject> kNNs = new ArrayList<VisitedObject>();
    	HashSet<Integer> visitedN = new HashSet<Integer>();
    	HashSet<Integer> visitedO = new HashSet<Integer>();
    	
    	int expNodeCnt = 0;
    	
    	VisitedObject obj = new VisitedObject(srcVID,-1,0); 
    	queue.add(obj);
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
    		}
    		*/
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
