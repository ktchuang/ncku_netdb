package arbor.lbs.networkprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import arbor.foundation.time.ExecTimer;
import arbor.foundation.time.MemUsage;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.Graph;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.ShortCutSet;
import arbor.lbs.uqp.graph.util.Vertex;

public class MetisPartitioning {
	int rNetID=0;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		MetisPartitioning me = new MetisPartitioning();
		String input = null, execFile = null;
		int partitionFactor = 0, level = 0;
		
		if (args.length==4){
			input = args[0];
			execFile = args[1];
			partitionFactor = Integer.valueOf(args[2]);
			level = Integer.valueOf(args[3]);
		}
		else {
			System.out.println("4 input parameters: InputFileName ExecFile PartitionFactor Level");
			return;
		}
		ExecTimer timer = new ExecTimer();
		MemUsage mu = new MemUsage();
    	timer.setStartTime("Index Construction Time");
        me.HierarchyPartition(input, execFile, partitionFactor, level);
        long mem = mu.getCurrentMemorySize();
        float memSize = mem/1024f;
    	System.out.println("Memory Size (kbytes) = "+memSize);
        System.out.println(timer.setEndTime());
        String rnetHierarchyName = input+".rh";
		RNetHierarchy.getSignleton().saveHierarchy(rnetHierarchyName);
		String graphPoolName = input+".gp";
		GraphPool.getSignleton().saveGraph(graphPoolName);
		System.out.println("RNet Hierarchy Complete");

	}

	private void HierarchyPartition(String input, String execFile, int partitionFactor, int level) throws IOException {
		// TODO Auto-generated method stub
		String graphPoolName = input+".gp";
		GraphPool.getSignleton().loadGraph(graphPoolName);
		
		List<Vertex> nodes = GraphPool.getSignleton().getAllVertexes();
		List<Edge> edges = GraphPool.getSignleton().getAllEdges();
		ArrayList<Integer> nodeInt = new ArrayList<Integer>();
		for (int v = 0; v<nodes.size(); v++) {
			nodeInt.add(nodes.get(v).getId());
			/*if ((v%1000)==0) {
				System.out.print(v);
				System.out.print(" ");
			}*/
		}
		//System.out.print("\n");
		String inputName = input+"."+String.valueOf(0)+"."+String.valueOf(0);
		HashMap<Integer,Integer> nodeMapping = CreatePartitionFile(inputName,nodeInt);
		//root Rnet
		Graph graph = new Graph(nodes,edges);
		RNet rnet = new RNet(rNetID,0,-1,graph);
		RNetHierarchy.getSignleton().insertRNet(rnet);
		rNetID++;
		
		int curLevel = 1;
		Partitioning(inputName,execFile,partitionFactor);
		InsertPart2RNet(inputName,partitionFactor,curLevel,0,nodeMapping);  // parent Rnet ID == -1 represents root
		for (int i=1; i<level; i++) {
			curLevel++;
			List<Integer> rnetList = RNetHierarchy.getSignleton().getLevelRNet(i);
			for (int j=0; j<rnetList.size(); j++) {
				rnet = RNetHierarchy.getSignleton().getRNet(rnetList.get(j));
				nodeInt = (ArrayList<Integer>) rnet.getAllVertexes();
				inputName = input+"."+String.valueOf(i)+"."+String.valueOf(j);
				nodeMapping = CreatePartitionFile(inputName,nodeInt);
				Partitioning(inputName,execFile,partitionFactor);
				InsertPart2RNet(inputName,partitionFactor,curLevel,rnet.getRNetID(),nodeMapping);
				//temp for calculate memory usage 
				//rnet.resetSubgraph();
			}
		}
		RNetHierarchy.getSignleton().SetShortCut();
		//OutputShortCutInfo();
		/*
		String rnetHierarchyName = input+".rh";
		RNetHierarchy.getSignleton().saveHierarchy(rnetHierarchyName);
		GraphPool.getSignleton().saveGraph(graphPoolName);
		*/
		return;
	}
	
	private void OutputShortCutInfo() {
		// TODO Auto-generated method stub
		int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		for (int i=1; i<maxLevel; i++) {
			LinkedList<Integer> rnetList = RNetHierarchy.getSignleton().getLevelRNet(i);
			for (int j=0; j<rnetList.size(); j++) {
				RNet r = RNetHierarchy.getSignleton().getRNet(rnetList.get(j));
				System.out.println(r.getRNetID());
				LinkedList<Integer> borderList = r.getBorderList();
				for (int m=0; m<borderList.size(); m++) {
					System.out.println(borderList.get(m));
					ShortCutSet scSet = r.getShortCutSet(borderList.get(m));
					Iterator<Integer> destIter=  scSet.getSCMap().keySet().iterator();
					while (destIter.hasNext()) {
						int des = (Integer)destIter.next();
						Path p = scSet.getShortCut(des);
						System.out.print(des);
						System.out.print(" ");
						System.out.print(p.cost);
						System.out.println();
					}
				}
			}
		}
	}

	private HashMap<Integer,Integer> CreatePartitionFile(String input, List<Integer> nodes) throws IOException {
		// TODO Auto-generated method stub
		String output = input;
		HashMap<Integer,HashSet<Integer>> nodeMap = new HashMap<Integer,HashSet<Integer>>();
		HashMap<Integer,Integer> nodeMapping1 = new HashMap<Integer,Integer>(); //(original ID, new ID)
		HashMap<Integer,Integer> nodeMapping2 = new HashMap<Integer,Integer>(); //(new ID, original ID)
		int count= 0;
		int nodeSize = nodes.size();
		//System.out.print("create partition file 1");
		for (int i=0; i<nodeSize; i++) {
			//System.out.print("i=");  System.out.println(i);
			int id = nodes.get(i).intValue();
			Vertex v = GraphPool.getSignleton().getVertex(id);
			List<Integer> neighbors= v.getNeighbors();
			HashSet<Integer> connectedNodes = new HashSet<Integer>();
			for (int k=0; k<neighbors.size(); k++) {
				//System.out.print("k="); System.out.println(k);
				v = GraphPool.getSignleton().getVertex(neighbors.get(k).intValue());
				if (nodes.contains(v.getId())) {
					connectedNodes.add(neighbors.get(k));
				}
			}
			count += connectedNodes.size();
			nodeMap.put(id, connectedNodes);
			/*if ((i%1000)==0) {
				System.out.print(i);
				System.out.print(" ");
			}*/
		}
		//System.out.print("\n");
		//System.out.print("create partition file 2");
		Iterator<Integer> nodeIter = nodeMap.keySet().iterator();
		int index=1;
		FileWriter out = new FileWriter(new File(output));
		//System.out.println(nodeSize);
		out.write(String.valueOf(nodeSize));
		out.write(" ");
		count /=2;
		out.write(String.valueOf(count));
		out.write("\n");
		while (nodeIter.hasNext()) {
			Integer nodeID = (Integer)nodeIter.next();
			nodeMapping1.put(nodeID, index);
			nodeMapping2.put(index, nodeID);
			index++;
			/*if ((index%1000)==0) {
				System.out.print(index);
				System.out.print(" ");
			}*/
		}
		//System.out.print("\n");
		//System.out.print("create partition file 3");
		//System.out.println(nodeSize);
		nodeIter = nodeMap.keySet().iterator();
		for (int j=1; j<=nodeSize; j++) {
			Integer nodeID = nodeMapping2.get(j);
			Iterator<Integer> others = nodeMap.get(nodeID).iterator();
			while (others.hasNext()) {
				Integer tmpID1 = (Integer)others.next();
				Integer tmpID2 = nodeMapping1.get(tmpID1);
				int intID2 = tmpID2.intValue();
				out.write(String.valueOf(intID2));
				out.write(" ");
			}
			out.write("\n");
			/*if ((j%1000)==0) {
				System.out.print(j);
				System.out.print(" ");
			}*/
		}
		//System.out.print("\n");
		/*
		while (nodeIter.hasNext()) {
			Integer nodeID = (Integer)nodeIter.next();
			Iterator<Integer> others = nodeMap.get(nodeID).iterator();
			while (others.hasNext()) {
				Integer tmpID1 = (Integer)others.next();
				Integer tmpID2 = nodeMapping1.get(tmpID1);
				int intID2 = tmpID2.intValue();
				out.write(String.valueOf(intID2));
				out.write(" ");
			}
			out.write("\n");
		}*/
		//System.out.println(nodeMap.keySet().size());
		out.flush();
		out.close();
		return nodeMapping2;
	}

	private void Partitioning(String input, String execFile, int partitionFactor) throws IOException {
		
		String s = null;
		String command = "utility\\graph_part\\"+execFile+" ";
		command = command + input + " " + String.valueOf(partitionFactor);
		
		Process p = Runtime.getRuntime().exec(command);
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	    // read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println(s);
        }
        // read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println(s);
        }
	}
	
	private void InsertPart2RNet(String input, int partitionFactor, int level, int parentID, HashMap<Integer,Integer> mapping) throws IOException {
		String parFileName = input+".part."+String.valueOf(partitionFactor);
		BufferedReader in = new BufferedReader(new FileReader(parFileName));
		String str;
		int num = 1;
		//(partID,(nodeID, newNodeID))
		HashMap<Integer, HashMap<Integer, Integer>> partSet = new HashMap<Integer, HashMap<Integer, Integer>>();
		//(nodeID,partID)
		HashMap<Integer, Integer> partInfo = new HashMap<Integer,Integer>();
			
		while ((str = in.readLine()) != null) {
			Integer partID = Integer.valueOf(str);
			if (partSet.get(partID)!=null){
				partSet.get(partID).put(Integer.valueOf(num),partSet.get(partID).size()+1);
            }
            else {
               	HashMap<Integer, Integer> setMembers = new HashMap<Integer, Integer>();
               	setMembers.put(Integer.valueOf(num), 1);
               	partSet.put(partID,setMembers);
            }
			partInfo.put(num, partID);
			num++;
			/*if ((num%1000)==0) {
				System.out.print(num);
				System.out.print(" ");
			}*/
		}
		//System.out.print("\n");
		in.close();
		
		Iterator<Integer> iter = partSet.keySet().iterator();
		while (iter.hasNext()){
			int ttt=0;
			Integer pID = (Integer)iter.next();
			Graph subgraph = new Graph();
			Iterator<Integer> idIter = partSet.get(pID).keySet().iterator();
			while (idIter.hasNext()) {
				Integer id = (Integer)idIter.next();
				int originalID = mapping.get(id).intValue();
				subgraph.insertVertex(originalID);  //node ID starts from 1
				Vertex v = GraphPool.getSignleton().getVertex(originalID);
				List<Integer> neighbors= v.getNeighbors();
				for (int k=0; k<neighbors.size(); k++) {
					Edge e = GraphPool.getSignleton().getEdge(originalID, neighbors.get(k).intValue());
					subgraph.insertEdge(e.getId());
				}
				ttt++;
				/*if ((ttt%1000)==0) {
					System.out.print(ttt);
					System.out.print(" ");
				}*/
			}
			//System.out.print(rNetID);
			//System.out.print("\n");
			RNet rnet = new RNet(rNetID,level,parentID,subgraph);
			RNetHierarchy.getSignleton().insertRNet(rnet);
			rNetID++;
		}
		System.out.print("\n");
	}
	
	private void ttt(String input, int partitionFactor) throws IOException {
		
		// Opening partition result file 
        String parFileName = input+".part."+String.valueOf(partitionFactor);
		BufferedReader in = new BufferedReader(new FileReader(parFileName));
		String str;
		int num = 1;
		//(partID,(nodeID, newNodeID))
		HashMap<Integer, HashMap<Integer, Integer>> partSet = new HashMap<Integer, HashMap<Integer, Integer>>();
		//(nodeID,partID)
		HashMap<Integer, Integer> partInfo = new HashMap<Integer,Integer>();
			
		while ((str = in.readLine()) != null) {
			Integer partID = Integer.valueOf(str);
			if (partSet.get(partID)!=null){
				partSet.get(partID).put(Integer.valueOf(num),partSet.get(partID).size()+1);
            }
            else {
               	HashMap<Integer, Integer> setMembers = new HashMap<Integer, Integer>();
               	setMembers.put(Integer.valueOf(num), 1);
               	partSet.put(partID,setMembers);
            }
			partInfo.put(num, partID);
			num++;
		}
		in.close();
		int index = input.indexOf('.');
		String edgeFileName = input.substring(0, index)+".edge";
		HashMap<Integer, HashSet<Integer[]>> newEdge = new HashMap<Integer, HashSet<Integer[]>>();
		int cutNum=0;
		
		in = new BufferedReader(new FileReader(edgeFileName));
		while ((str = in.readLine()) != null) {
			String[] tmp=str.split(" ");
            Integer nodeID1 = Integer.valueOf(tmp[0]);
            Integer nodeID2 = Integer.valueOf(tmp[1]);
            if (partInfo.get(nodeID1).intValue()==partInfo.get(nodeID2).intValue()) {
            	Integer[] nodePair = {nodeID1, nodeID2};
            	if (newEdge.get(partInfo.get(nodeID1))!=null){
            		newEdge.get(partInfo.get(nodeID1)).add(nodePair);
                }
            	else {
            		HashSet<Integer[]> newPairSet = new HashSet<Integer[]>();
            		newPairSet.add(nodePair);
            		newEdge.put(partInfo.get(nodeID1), newPairSet);
            	}
            }
            else {
            	cutNum++;
            }
		}
		System.out.println(cutNum);
		in.close();
		
		//(original nodeID, new nodeID)
		String nodeMapping = input+".nodemapping";
		FileWriter out = new FileWriter(new File(nodeMapping));
		for (int i=0; i<partSet.size(); i++){
			HashMap<Integer, Integer> nodes = partSet.get(i);
			Iterator<Integer> iter = nodes.keySet().iterator();
			while (iter.hasNext()){
				Integer tmp = (Integer)iter.next();
				out.write(tmp.toString()+" "+nodes.get(tmp).toString()+"\n");
			}
			out.write("==="+"\n");
		}
		out.flush();
		out.close();
		
		
		for(int i=0; i<newEdge.size(); i++){
			HashMap<Integer, HashSet<Integer>> node = new HashMap<Integer,HashSet<Integer>>();
			int count=0;
			
			String partGraph = input+"."+String.valueOf(i);
			out = new FileWriter(new File(partGraph));
			HashSet<Integer[]> edges = newEdge.get(i);
			Iterator<Integer[]> iter = edges.iterator();
			while (iter.hasNext()){
				Integer[] tmp = (Integer[])iter.next();
				Integer nID1 = partSet.get(i).get(tmp[0]);
				Integer nID2 = partSet.get(i).get(tmp[1]);
				if (node.get(nID1)!=null){
	              	node.get(nID1).add(nID2);  
	            }
	            else {
	               	HashSet<Integer> neighbors = new HashSet<Integer>();
	               	neighbors.add(nID2);
	               	node.put(nID1,neighbors);
	            }
	            if (node.get(nID2)!=null){
	               	node.get(nID2).add(nID1);
	            }
	            else {
	               	HashSet<Integer> neighbors = new HashSet<Integer>();
	               	neighbors.add(nID1);
	               	node.put(nID2,neighbors);
	            }
	            count += 1;
			}
			int nodeNum = node.keySet().size();
			out.write(nodeNum+" "+count+"\n");
			for (int j=1; j<=nodeNum; j++) {
				HashSet<Integer> nodeSet = node.get(j);
				if (nodeSet!=null) {
					Iterator<Integer> iter1 = nodeSet.iterator();
					while (iter1.hasNext()){
						Integer tmp = (Integer)iter1.next();
						out.write(tmp+" ");
					}
					out.write("\n");
				}
				else {
					out.write("\n");
				}
			}
			out.flush();
			out.close();
		}
	}

}
