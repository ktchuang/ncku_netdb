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

public class AdaptiveLeafQueryGeneratorNew {

	public AdaptiveLeafQueryGeneratorNew(String graphFn, String hierFn, String queryFn, int leafNum, int number) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(graphFn);
		RNetHierarchy.loadHierarchy(hierFn);
		
		
		String file1 = queryFn+"_0.txt";
		BufferedWriter out1 = new BufferedWriter(new FileWriter(new File(file1)));
		String file2 = queryFn+"_0.2.txt";
		BufferedWriter out2 = new BufferedWriter(new FileWriter(new File(file2)));
		String file3 = queryFn+"_0.4.txt";
		BufferedWriter out3 = new BufferedWriter(new FileWriter(new File(file3)));
		String file4 = queryFn+"_0.6.txt";
		BufferedWriter out4 = new BufferedWriter(new FileWriter(new File(file4)));
		String file5 = queryFn+"_0.8.txt";
		BufferedWriter out5 = new BufferedWriter(new FileWriter(new File(file5)));
		String file6 = queryFn+"_1.txt";
		BufferedWriter out6 = new BufferedWriter(new FileWriter(new File(file6)));
		
		
		int leafLevel = RNetHierarchy.getSignleton().getMaxLevel();
		LinkedList<Integer> leafRNetSet = RNetHierarchy.getSignleton().getLevelRNet(leafLevel);
		int leafRNetNum = leafRNetSet.size();
		
		ArrayList<Integer> chosenSet = new ArrayList<Integer>();
		Random r = new Random();
		
		int pickNum = 0;
		
		while (pickNum<leafNum) {
			int ranVar = r.nextInt(leafRNetNum);
			int rnetID = leafRNetSet.get(ranVar);
			if (chosenSet.contains(rnetID))
				continue;
			for (int i=0; i<10; i++) {
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
		List<Vertex> vertexList = GraphPool.getSignleton().getAllVertexes();
		ArrayList<Integer> norVList = new ArrayList<Integer>();
		for (int i=0; i<vertexList.size(); i++) {
			norVList.add(vertexList.get(i).getId());
		}
		norVList.removeAll(hVertexList);
			
		r = new Random();
		int nNum = norVList.size();
		Random hRan = new Random();
		Random nRan = new Random();
		Random vRan = new Random();
		int count1=0,count2=0,count3=0,count4=0,count5=0,count6=0;
		for (int l=0; l<number; l++) {
    		double ranDouble = r.nextDouble();
    		int hran = hRan.nextInt(leafNum);
    		int nran = nRan.nextInt(nNum);
    		if (ranDouble<=1) {
    			int rID = chosenSet.get(hran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			int ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran);
    			out6.write(String.valueOf(ranVID));
        		out6.write("\n");
        		count6++;
    		}
    		else {
    			int ranVID = norVList.get(nran);
    			out6.write(String.valueOf(ranVID));
        		out6.write("\n");
    		}
    		if (ranDouble<=0.8) {
    			int rID = chosenSet.get(hran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			int ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran);
    			out5.write(String.valueOf(ranVID));
        		out5.write("\n");
        		count5++;
    		}
    		else {
    			int ranVID = norVList.get(nran);
    			out5.write(String.valueOf(ranVID));
        		out5.write("\n");
    		}
    		if (ranDouble<=0.6) {
    			int rID = chosenSet.get(hran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			int ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran)	;
    			out4.write(String.valueOf(ranVID));
        		out4.write("\n");
        		count4++;
    		}
    		else {
    			int ranVID = norVList.get(nran);
    			out4.write(String.valueOf(ranVID));
        		out4.write("\n");
    		}
    		if (ranDouble<=0.4) {
    			int rID = chosenSet.get(hran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			int ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran);
    			out3.write(String.valueOf(ranVID));
        		out3.write("\n");
        		count3++;
    		}
    		else {
    			int ranVID = norVList.get(nran);
    			out3.write(String.valueOf(ranVID));
        		out3.write("\n");
    		}
    		if (ranDouble<=0.2) {
    			int rID = chosenSet.get(hran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			int ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran);
    			out2.write(String.valueOf(ranVID));
        		out2.write("\n");
        		count2++;
    		}
    		else {
    			int ranVID = norVList.get(nran);
    			out2.write(String.valueOf(ranVID));
        		out2.write("\n");
    		}
    		if (ranDouble<=0) {
    			int rID = chosenSet.get(hran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			int ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran);
    			out1.write(String.valueOf(ranVID));
        		out1.write("\n");
        		count1++;
    		}
    		else {
    			int ranVID = norVList.get(nran);
    			out1.write(String.valueOf(ranVID));
        		out1.write("\n");
    		}
		}
		System.out.println(count1);
		System.out.println(count2);
		System.out.println(count3);
		System.out.println(count4);
		System.out.println(count5);
		System.out.println(count6);
		/*
		chosenSet = new ArrayList<Integer>();
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
		
		
		r = new Random();
		nNum = norVList.size();
		hRan = new Random();
		nRan = new Random();
		vRan = new Random();
		count=0;
		for (int l=0; l<number; l++) {
    		double ranDouble = r.nextDouble();
    		if (ranDouble<=simDouble) {
    			int ran = hRan.nextInt(leafNum);
    			int rID = chosenSet.get(ran);
    			RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
    			List<Integer> vList = rNet.getAllVertexes();
    			int rSize = vList.size();
    			ran = vRan.nextInt(rSize);
    			int ranVID = vList.get(ran);
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
		out1.flush();
		out1.close();
		out2.flush();
		out2.close();
		out3.flush();
		out3.close();
		out4.flush();
		out4.close();
		out5.flush();
		out5.close();
		out6.flush();
		out6.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		AdaptiveLeafQueryGeneratorNew generator = new AdaptiveLeafQueryGeneratorNew(args[0],args[1],args[2],Integer.valueOf(args[3]),Integer.valueOf(args[4]));
		System.out.println("Create Query Sequence successfully");
	}

}
