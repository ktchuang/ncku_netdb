package arbor.lbs.random.datagenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class ClusterObjectGenerator {

	public ClusterObjectGenerator(String graphFn, String hierFn, String objFn, int clusterNum, int number) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(graphFn);
		RNetHierarchy.loadHierarchy(hierFn);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(objFn)));
		
		int leafLevel = RNetHierarchy.getSignleton().getMaxLevel();
		LinkedList<Integer> leafRNetSet = RNetHierarchy.getSignleton().getLevelRNet(leafLevel);
		int leafRNetNum = leafRNetSet.size();
		
		ArrayList<Integer> chosenSet = new ArrayList<Integer>();
		Random r = new Random();
		
		int pickNum = 0;
		
		while (pickNum<clusterNum) {
			int ranVar = r.nextInt(leafRNetNum);
			int rnetID = leafRNetSet.get(ranVar);
			if (chosenSet.contains(rnetID))
				continue;
			for (int i=0; i<1; i++) {
				chosenSet.add(rnetID++);
				pickNum++;
			}
			
		}
		
		ArrayList<Integer> hVertexList = new ArrayList<Integer>();
		Iterator<Integer> iter = chosenSet.iterator();
		while (iter.hasNext()){
			Integer rID = (Integer)iter.next();
			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
			List<Integer> vList = rNet.getAllVertexes();
			hVertexList.addAll(vList);
		}
		
		r = new Random();
		int count=0;
		int vertexSize = hVertexList.size();
		for (int l=0; l<number; l++) {
			out.write(String.valueOf(l));
			out.write(",");
			
    		int ran = r.nextInt(vertexSize);
    		int ranVID = hVertexList.get(ran);
    		Point p = GraphPool.getSignleton().getVertex(ranVID).getloc();
    		double x = p.getCoord(0);
    		out.write(String.valueOf(x));
    		out.write(",");
    		double y = p.getCoord(1);
    		out.write(String.valueOf(y));
    		out.write(",");
    		out.write(String.valueOf(ranVID));
        	out.write("\n");
		}
		System.out.println(vertexSize);
		
		out.flush();
		out.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ClusterObjectGenerator generator = new ClusterObjectGenerator(args[0],args[1],args[2],Integer.valueOf(args[3]),Integer.valueOf(args[4]));
		System.out.println("Create Query Sequence successfully");
	}

}
