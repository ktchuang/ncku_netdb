package arbor.lbs.random.datagenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.HQNet;
import arbor.lbs.uqp.graph.util.HQNetPool;
import arbor.lbs.uqp.graph.util.Vertex;

public class QueryGenerator {

	public QueryGenerator(String graphFn, String hqnetFn, String queryFn, String similarity, int number) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(graphFn);
		HQNetPool.loadHQNets(hqnetFn);
		
		double simDouble = Double.valueOf(similarity);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(queryFn)));
		
		ArrayList<Integer> hVertexList = new ArrayList<Integer>();
		HashMap<Integer,HQNet> hMap = HQNetPool.getSignleton().getHQNet();
		Iterator<Integer> iter = hMap.keySet().iterator();
		while (iter.hasNext()){
			Integer hID = (Integer)iter.next();
			HQNet hqNet = hMap.get(hID);
			List<Integer> vList = hqNet.getAllVertexes();
			hVertexList.addAll(vList);
			//HashSet<Integer> bSet = hqNet.getBorderSet();
			//hVertexList.addAll(bSet);
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
		Random r = new Random();
		Random hRan = new Random();
		Random nRan = new Random();
		int count=0;
		for (int l=0; l<number; l++) {
    		double ranDouble = r.nextDouble();
    		if (ranDouble<=simDouble) {
    			int ran = hRan.nextInt(hNum);
    			int ranVID = hVertexList.get(ran);
    			int hNetId = GraphPool.getSignleton().getVertex(ranVID).getHQNetID();
    			//System.out.println(hNetId);
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
		out.flush();
		out.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		QueryGenerator generator = new QueryGenerator(args[0],args[1],args[2],args[3],Integer.valueOf(args[4]));
		System.out.println("Create Query Sequence successfully");
	}

}
