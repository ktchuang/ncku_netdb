package arbor.lbs.networkprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import arbor.foundation.time.ExecTimer;
import arbor.foundation.time.MemUsage;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.Graph;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class StaticHQNetCreator {

	public StaticHQNetCreator(String graphFn, String hierFn, String msgFn, String outputFn,String hqnetFn, String hqnetGraphFn ) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.getSignleton().loadGraph(graphFn);
		RNetHierarchy.getSignleton().loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFn)));
		out.write("x,y"+"\n");
		
		/*load HQNetGraph*/
		HashMap<Integer,HashSet<Integer>> hqnetGraph = new HashMap<Integer,HashSet<Integer>>();
		BufferedReader reader = new BufferedReader(new FileReader(hqnetGraphFn));
  	  	String line;
  	  	while ((line=reader.readLine()) != null) {
  	  		String[] tmp = line.split(" ");
  	  	HashSet<Integer> nodeSet = new HashSet<Integer>();
  	  		for (int i=1; i<tmp.length; i++) {
  	  			nodeSet.add(Integer.valueOf(tmp[i]));
  	  		}
  	  		hqnetGraph.put(Integer.valueOf(tmp[0]), nodeSet);
  	  	}
  	  	reader.close();
		
		int hnetNum = 0;
		List<Vertex> vertexList = GraphPool.getSignleton().getAllVertexes();
		int vertexNum = vertexList.size();
		int levelNum = RNetHierarchy.getSignleton().getMaxLevel();
		int leafRNetNum = RNetHierarchy.getSignleton().getLevelRNet(levelNum).size();
		
		Iterator<Integer> hqnetGIter = hqnetGraph.keySet().iterator();
		
		while (hqnetGIter.hasNext()) {
			//insert nodes to subgraph
			Graph subgraph = new Graph();
			Integer hIdx = (Integer)hqnetGIter.next();
			subgraph.insertVertex(hIdx);
			Iterator<Integer> nodeSetIter = hqnetGraph.get(hIdx).iterator();
			while(nodeSetIter.hasNext()) {
				Integer nodeID = (Integer)nodeSetIter.next();
				subgraph.insertVertex(nodeID);
			}
			//insert edges to subgraph
			List<Integer> subgraphNodes = subgraph.getVertexes();
			for (int j=0; j<subgraphNodes.size(); j++) {
				Vertex v = GraphPool.getSignleton().getVertex(subgraphNodes.get(j));
				List<Integer> neighbors = v.getNeighbors();
				for (int k=0; k<neighbors.size(); k++) {
					Edge e = GraphPool.getSignleton().getEdge(v.getId(), neighbors.get(k).intValue());
					subgraph.insertEdge(e.getId());
				}
			}
			Vertex v =  GraphPool.getSignleton().getVertex(hIdx);
			HQNet hnet = new HQNet(hnetNum, v.getLeafRnetID(), subgraph);
			boolean containObj = false;
			List<Integer> allVertex = subgraph.getVertexes();
			for (int p=0; p<allVertex.size(); p++) {
				Vertex tmp = GraphPool.getSignleton().getVertex(allVertex.get(p));
				tmp.setHQNetID(hnetNum);
				if (tmp.hasAssocMsgObj()) {
					containObj = true;
				}
				double[] xy = tmp.getloc().m_pCoords;
				out.write(String.valueOf(xy[0]));
				out.write(",");
				out.write(String.valueOf(xy[1]));
				out.write("\n");
			}
			hnet.setContainObj(containObj);
			hnetNum++;
			//insert to HQNetPool
			HQNetPool.getSignleton().insertHQNet(hnet);
		}
		HQNetPool.getSignleton().saveHQNets(hqnetFn);
		String queryGraphFn = graphFn+".query";
		GraphPool.getSignleton().saveGraph(queryGraphFn);
		out.flush();
		out.close();
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ExecTimer timer = new ExecTimer();
    	timer.setStartTime("HQNet Construction Time");
		StaticHQNetCreator me = new StaticHQNetCreator(args[0],args[1], args[2], args[3], args[4], args[5]);
		System.out.println(timer.setEndTime());
		System.out.println("Create HQNetPool successfully");

	}

}
