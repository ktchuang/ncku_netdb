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
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class RandomLeafHQNetCreator {

	public RandomLeafHQNetCreator(String graphFn, String hierFn, String msgFn, String outputFn,
			String hqnetFn, String hqnetGraphFn, int hqNum, int factor ) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.getSignleton().loadGraph(graphFn);
		RNetHierarchy.getSignleton().loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFn)));
		out.write("x,y"+"\n");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(hqnetGraphFn)));

		int hnetNum = 0;
		
		int leafLevel = RNetHierarchy.getSignleton().getMaxLevel();
		LinkedList<Integer> leafRNetSet = RNetHierarchy.getSignleton().getLevelRNet(leafLevel);
		int leafRNetNum = leafRNetSet.size();
		HashSet<Integer> chosenSet = new HashSet<Integer>();
		Random r = new Random();
		
		
		while (hnetNum<hqNum) {
			int ranVar = r.nextInt(leafRNetNum);
			if (chosenSet.contains(ranVar))
				continue;
			int rnetID = leafRNetSet.get(ranVar);
			//System.out.println(rnetID);
			//insert nodes to subgraph
			Graph subgraph = new Graph();
			List<Integer> allVertex = RNetHierarchy.getSignleton().getRNet(rnetID).getAllVertexes();
			Iterator<Integer> allVIter = allVertex.iterator();
			while(allVIter.hasNext()) {
				Integer vertexID = (Integer)allVIter.next();
				writer.write(String.valueOf(vertexID)+" ");
				subgraph.insertVertex(vertexID);
			}
			writer.write("\n");
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
			HQNet hnet = new HQNet(hnetNum, rnetID, subgraph);
			boolean containObj = false;
			allVertex = subgraph.getVertexes();
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
		writer.flush();
		writer.close();
	}
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ExecTimer timer = new ExecTimer();
    	timer.setStartTime("HQNet Construction Time");
		RandomLeafHQNetCreator me = new RandomLeafHQNetCreator(args[0],args[1], args[2], args[3], args[4],
				 args[5],Integer.valueOf(args[6]), Integer.valueOf(args[7]));
		System.out.println(timer.setEndTime());
		System.out.println("Create HQNetPool successfully");

	}

}
