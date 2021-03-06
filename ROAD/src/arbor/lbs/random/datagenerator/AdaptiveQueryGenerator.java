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

import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class AdaptiveQueryGenerator {

	public AdaptiveQueryGenerator(String graphFn, String hierFn, String queryFn, String similarity, int leafNum, int number) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(graphFn);
		RNetHierarchy.loadHierarchy(hierFn);
		
		double simDouble = Double.valueOf(similarity);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(queryFn)));
		
		int leafLevel = RNetHierarchy.getSignleton().getMaxLevel();
		LinkedList<Integer> leafRNetSet = RNetHierarchy.getSignleton().getLevelRNet(leafLevel);
		int leafRNetNum = leafRNetSet.size();
		
		HashSet<Integer> chosenSet = new HashSet<Integer>();
		Random r = new Random();
		
		int pickNum = 0;
		
		while (pickNum<leafNum) {
			int ranVar = r.nextInt(leafRNetNum);
			if (chosenSet.contains(ranVar))
				continue;
			int rnetID = leafRNetSet.get(ranVar);
			chosenSet.add(rnetID);
			pickNum++;
		}
		
		ArrayList<Integer> hVertexList = new ArrayList<Integer>();
		Iterator<Integer> iter = chosenSet.iterator();
		while (iter.hasNext()){
			Integer rID = (Integer)iter.next();
			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
			List<Integer> vList = rNet.getAllVertexes();
			hVertexList.addAll(vList);
		}
		List<Vertex> vertexList = GraphPool.getSignleton().getAllVertexes();
		ArrayList<Integer> norVList = new ArrayList<Integer>();
		for (int i=0; i<vertexList.size(); i++) {
			norVList.add(vertexList.get(i).getId());
		}
		norVList.removeAll(hVertexList);
		
		int hNum = hVertexList.size();
		int nNum = norVList.size();
		System.out.println(hNum);
		System.out.println(nNum);
		r = new Random();
		Random hRan = new Random();
		Random nRan = new Random();
		int count=0;
		for (int l=0; l<number; l++) {
    		double ranDouble = r.nextDouble();
    		if (ranDouble<=simDouble) {
    			int ran = hRan.nextInt(hNum);
    			int ranVID = hVertexList.get(ran);
    			out.write(String.valueOf(ranVID));
        		out.write("\n");
        		count++;
    		}
    		else {
    			int ran = nRan.nextInt(nNum);
    			int ranVID = norVList.get(ran);
    			out.write(String.valueOf(ranVID));
        		out.write("\n");	
    		}
		}
		System.out.println(count);
		
		/*
		chosenSet = new HashSet<Integer>();
		r = new Random();
		
		pickNum = 0;
		
		while (pickNum<leafNum) {
			int ranVar = r.nextInt(leafRNetNum);
			if (chosenSet.contains(ranVar))
				continue;
			int rnetID = leafRNetSet.get(ranVar);
			chosenSet.add(rnetID);
			pickNum++;
		}
		
		hVertexList = new ArrayList<Integer>();
		iter = chosenSet.iterator();
		while (iter.hasNext()){
			Integer rID = (Integer)iter.next();
			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
			List<Integer> vList = rNet.getAllVertexes();
			hVertexList.addAll(vList);
		}
		vertexList = GraphPool.getSignleton().getAllVertexes();
		norVList = new ArrayList<Integer>();
		for (int i=0; i<vertexList.size(); i++) {
			norVList.add(vertexList.get(i).getId());
		}
		norVList.removeAll(hVertexList);
		
		hNum = hVertexList.size();
		nNum = norVList.size();
		System.out.println(hNum);
		System.out.println(nNum);
		r = new Random();
		hRan = new Random();
		nRan = new Random();
		count=0;
		for (int l=0; l<number; l++) {
    		double ranDouble = r.nextDouble();
    		if (ranDouble<=simDouble) {
    			int ran = hRan.nextInt(hNum);
    			int ranVID = hVertexList.get(ran);
    			out.write(String.valueOf(ranVID));
        		out.write("\n");
        		count++;
    		}
    		else {
    			int ran = nRan.nextInt(nNum);
    			int ranVID = norVList.get(ran);
    			out.write(String.valueOf(ranVID));
        		out.write("\n");	
    		}
		}
		System.out.println(count);
		*/
		
		out.flush();
		out.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		AdaptiveQueryGenerator generator = new AdaptiveQueryGenerator(args[0],args[1],args[2],args[3],Integer.valueOf(args[4]),Integer.valueOf(args[5]));
		System.out.println("Create Query Sequence successfully");
	}

}
