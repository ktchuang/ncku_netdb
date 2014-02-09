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

public class LeafHQNetCreator {

	public LeafHQNetCreator(String graphFn, String hierFn, String msgFn, String outputFn,
			String hqnetFn, String hqnetGraphFn, int hqNum, int factor ) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.getSignleton().loadGraph(graphFn);
		RNetHierarchy.getSignleton().loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFn)));
		out.write("x,y"+"\n");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(hqnetGraphFn)));

		//get leaf Rnet with top-k neighbors
		TreeMap<Integer,HashSet<Integer>> analysisData = new TreeMap<Integer, HashSet<Integer>>();
		int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		LinkedList<Integer> allLeaf = RNetHierarchy.getSignleton().getLevelRNet(maxLevel);
		Iterator<Integer> allLeafIter = allLeaf.iterator();
		while (allLeafIter.hasNext()) {
			int rID = allLeafIter.next();
			RNet rnet = RNetHierarchy.getSignleton().getRNet(rID);
			List<Integer> vertexs = rnet.getAllVertexes();
			int allCnt = 0;
			for (int i=0; i<vertexs.size(); i++) {
				int vID = vertexs.get(i);
				allCnt += GraphPool.getSignleton().getVertex(vID).getNeighbors().size();
			}
			if (analysisData.containsKey(allCnt)) {
				HashSet<Integer> rSet = analysisData.get(allCnt);
				rSet.add(rID);
				analysisData.put(allCnt, rSet);
			}
			else {
				HashSet<Integer> rSet = new HashSet<Integer>();
				rSet.add(rID);
				analysisData.put(allCnt, rSet);
			}
		}
		HashSet<Integer> hqSet = new HashSet<Integer>();
		Iterator<Integer> keyIter = analysisData.descendingKeySet().iterator();
		int num = 0;
		while (keyIter.hasNext()&&(num<hqNum)) {
			int keyValue = keyIter.next();
			HashSet<Integer> rSet = analysisData.get(keyValue);
			if ((num+rSet.size())<hqNum) {
				hqSet.addAll(rSet);
				num += rSet.size();
			}
			else {
				Iterator<Integer> iter = rSet.iterator();
				for (int j=0; j<(hqNum-num); j++) {
					int id = iter.next();
					hqSet.add(id);
					num += 1;
				}
			}
		}
		// end

		
		int hnetNum = 0;
		//List<Vertex> vertexList = GraphPool.getSignleton().getAllVertexes();
		//int vertexNum = vertexList.size();
		//int levelNum = RNetHierarchy.getSignleton().getMaxLevel();
		//int leafRNetNum = RNetHierarchy.getSignleton().getLevelRNet(levelNum).size();
		
		
		Iterator<Integer> hqSetIter = hqSet.iterator();
		while (hqSetIter.hasNext()) {
			//insert nodes to subgraph
			Graph subgraph = new Graph();
			Integer hIdx = (Integer)hqSetIter.next();
			List<Integer> allVertex = RNetHierarchy.getSignleton().getRNet(hIdx).getAllVertexes();
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
			HQNet hnet = new HQNet(hnetNum, hIdx, subgraph);
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
		LeafHQNetCreator me = new LeafHQNetCreator(args[0],args[1], args[2], args[3], args[4],
				 args[5],Integer.valueOf(args[6]), Integer.valueOf(args[7]));
		System.out.println(timer.setEndTime());
		System.out.println("Create HQNetPool successfully");

	}

}
