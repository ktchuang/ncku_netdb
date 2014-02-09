package arbor.lbs.networkprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import arbor.foundation.time.ExecTimer;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.Vertex;


public class Road2GraphFile {
	/**
	 * @param args
	 */	
	public static void main(String[] args) throws Exception  {
		// TODO Auto-generated method stub
		Road2GraphFile me = new Road2GraphFile();
		String nodeFile = null, edgeFile = null, output = null;
		if (args.length==3){
			nodeFile = args[0];
			edgeFile = args[1];
			output = args[2];
		}
		else {
			System.out.println("3 input parameters: NodeFileName EdgeFileName OutputFileName");
			return;
		}
		ExecTimer timer = new ExecTimer();
		timer.setStartTime("Index Performance");
        me.formatTransform(nodeFile, edgeFile, output);
        System.out.println(timer.setEndTime());
        System.out.println("GraphPool Complete");
	}
	
	private void formatTransform(String nodeFile, String edgeFile, String output) throws IOException {
		HashMap<Integer, HashSet<Integer>> node = new HashMap<Integer,HashSet<Integer>>();
		int count=0;
		
		
        BufferedReader in = new BufferedReader(new FileReader(nodeFile));
        String str;
        
        while ((str = in.readLine()) != null) { 
            String[] tmp=str.split(" ");
            int nodeID = Integer.valueOf(tmp[0]).intValue()+1;
            double locX = Double.valueOf(tmp[1]).doubleValue();
            double locY = Double.valueOf(tmp[2]).doubleValue();
            Vertex v = new Vertex(nodeID, locX, locY);
            GraphPool.getSignleton().insertVertex(v);
        }
        in.close();
        
        int c = GraphPool.getSignleton().getAllVertexes().size();
        System.out.println(c);
        
        in = new BufferedReader(new FileReader(edgeFile));
        String edgeFileName = output+".edge";
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(edgeFileName)));
    
        // reading line by line from file  
        while ((str = in.readLine()) != null) { 
            String[] tmp=str.split(" ");
            Integer edgeID = Integer.valueOf(tmp[0]);
            Integer nodeID1 = Integer.valueOf(tmp[1])+1;
            Integer nodeID2 = Integer.valueOf(tmp[2])+1;
            Double dist = Double.valueOf(tmp[3])*1000;
            Edge e= new Edge(nodeID1.intValue(),nodeID2.intValue(),dist.doubleValue());
            GraphPool.getSignleton().insertEdge(e);
            if (node.get(nodeID1)!=null){
              	node.get(nodeID1).add(nodeID2);
            }
            else {
               	HashSet<Integer> neighbors = new HashSet<Integer>();
               	neighbors.add(nodeID2);
               	node.put(nodeID1,neighbors);
            }
            if (node.get(nodeID2)!=null){
               	node.get(nodeID2).add(nodeID1);
            }
            else {
               	HashSet<Integer> neighbors = new HashSet<Integer>();
               	neighbors.add(nodeID1);
               	node.put(nodeID2,neighbors);
            }
            count += 1;
            StringBuffer buf = new StringBuffer();
            Integer oriValue = Integer.valueOf(tmp[1]);
            String strValue = String.valueOf(oriValue.intValue()+1);
            buf.append(strValue+" ");
            oriValue = Integer.valueOf(tmp[2]);
            strValue = String.valueOf(oriValue.intValue()+1);
            buf.append(strValue+" "+tmp[3]);
            buf.append("\n");
			out.write(buf.toString());
        }
        in.close();
        out.flush();
		out.close();
		
		 c = GraphPool.getSignleton().getAllEdges().size();
		 System.out.println(c);
		 System.out.println(count);
		 
		String graphPoolName = output+".gp";
		GraphPool.getSignleton().saveGraph(graphPoolName);
	
		
		out = new BufferedWriter(new FileWriter(new File(output)));
		int nodeNum = node.keySet().size();
		out.write(nodeNum+" "+count+"\n");
		for (int i=1; i<=nodeNum; i++) {
			StringBuffer buf = new StringBuffer();
			int newID = i+1;
			//buf.append(newID);
			HashSet<Integer> nodeSet = node.get(i);
			Iterator<Integer> iter = nodeSet.iterator();
			while (iter.hasNext()){
				Integer tmp = (Integer)iter.next();
				newID = tmp.intValue()+1;
				buf.append(newID+" ");					
			}
			buf.append("\n");
			out.write(buf.toString());
		}
		out.flush();
		out.close();

		
	}
}
