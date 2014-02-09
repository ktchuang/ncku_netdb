package arbor.lbs.networkprocess;

import java.io.BufferedWriter;
import java.io.File;
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

public class HQNetCreator {

	public HQNetCreator(String graphFn, String hierFn, String msgFn, String hNum,
			String size, String outputFn, String hqnetFn, String hqnetGraphFn ) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.getSignleton().loadGraph(graphFn);
		RNetHierarchy.getSignleton().loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(outputFn)));
		out.write("x,y"+"\n");
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(hqnetGraphFn)));
		
		int hnetNum = 0;
		List<Vertex> vertexList = GraphPool.getSignleton().getAllVertexes();
		int vertexNum = vertexList.size();
		int levelNum = RNetHierarchy.getSignleton().getMaxLevel();
		int leafRNetNum = RNetHierarchy.getSignleton().getLevelRNet(levelNum).size();
		//define HQNets/nodes ratios, HQNet size
		int graphSize = Integer.valueOf(size);
		//int hnetSeedNum = (int) (vertexNum*ratioValue);
		int hnetSeedNum = Integer.valueOf(hNum);
		HashSet<Integer> selectedNodeSet = new HashSet<Integer>();
		//get seed set randomly 
		while (hnetNum<hnetSeedNum) {
			Random r = new Random();
			int ranVar = r.nextInt(vertexNum)+1;
			if (!selectedNodeSet.contains(Integer.valueOf(ranVar))) {
				selectedNodeSet.add(ranVar);
				writer.write(String.valueOf(ranVar));
				List<Integer> conNodes = getConnectedNodes(ranVar,graphSize,selectedNodeSet);
				//insert nodes to subgraph
				Graph subgraph = new Graph();
				subgraph.insertVertex(ranVar);
				for(int i=0; i<conNodes.size(); i++) {
					selectedNodeSet.add(conNodes.get(i));
					subgraph.insertVertex(conNodes.get(i));
					writer.write(" "+conNodes.get(i));
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
				Vertex v =  GraphPool.getSignleton().getVertex(ranVar);
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
			
		}
		HQNetPool.getSignleton().saveHQNets(hqnetFn);
		String queryGraphFn = graphFn+".query";
		GraphPool.getSignleton().saveGraph(queryGraphFn);
		out.flush();
		out.close();
		writer.flush();
		writer.close();
	}
	private List<Integer> getConnectedNodes(int ranVer, int graphSize,
			HashSet<Integer> selectedNodeSet) {
		// TODO Auto-generated method stub
		ArrayList<Integer> nodes1 = new ArrayList<Integer>();
		ArrayList<Integer> nodes2 = new ArrayList<Integer>();
		ArrayList<Integer> graphNodes = new ArrayList<Integer>();
		nodes1.add(ranVer);
		Vertex seedV = GraphPool.getSignleton().getVertex(ranVer);
		int size=0;
		while (size<graphSize-1) {
			for (int i=0; i<nodes1.size(); i++) {
				Integer vID = nodes1.get(i);
				Vertex v = GraphPool.getSignleton().getVertex(vID);
				List<Integer> neigh = v.getNeighbors();
				for (int j=0; j<neigh.size(); j++) {
					if ((!selectedNodeSet.contains(neigh.get(j)))&&(size<graphSize)) {
						Vertex v1 = GraphPool.getSignleton().getVertex(neigh.get(j));
						if (v1.getLeafRnetID()==seedV.getLeafRnetID()) {
							nodes2.add(neigh.get(j));
							graphNodes.add(neigh.get(j));
							selectedNodeSet.add(neigh.get(j));
							size++;
						}
					}
					else if (size>=graphSize) {
						break;
					}
				}
				if (size>=graphSize) {
					break;
				}
			}
			if (nodes2.size()==0) {
				System.out.println("graph size is not as large as setting.");
				break;
			}
			nodes1.clear();
			nodes1.addAll(nodes2);
			nodes2.clear();
		}
		
		return graphNodes;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ExecTimer timer = new ExecTimer();
    	timer.setStartTime("HQNet Construction Time");
		HQNetCreator me = new HQNetCreator(args[0],args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
		System.out.println(timer.setEndTime());
		System.out.println("Create HQNetPool successfully");

	}

}
