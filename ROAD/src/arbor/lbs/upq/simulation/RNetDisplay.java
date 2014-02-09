package arbor.lbs.upq.simulation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;
import arbor.lbs.uqp.graph.util.Vertex;

public class RNetDisplay {

	public RNetDisplay(String networkFn, String hierFn, int rID) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(networkFn);
		RNetHierarchy.loadHierarchy(hierFn);
		
		RNet rNet = RNetHierarchy.getSignleton().getRNet(rID);
		List<Integer> childRNetList = rNet.getChildRNetList();
		int number = childRNetList.size();
		for (int i=0; i<number; i++) {
			Integer childRNetID = childRNetList.get(i);
			RNet childRNet = RNetHierarchy.getSignleton().getRNet(childRNetID);
			List<Long> edge = childRNet.getAllEdges();
			int edgeCnt = edge.size();
			int vertexCnt = childRNet.getAllVertexes().size();
			System.out.println("RNetID:"+childRNetID);
			System.out.println(vertexCnt+" "+edgeCnt+" "+edgeCnt/vertexCnt);
			String outFn = networkFn+"_"+rID+"_"+i+".txt";
			FileWriter out = new FileWriter(new File(outFn));
			Iterator iterator = edge.iterator();
            int j=1;
            while(iterator.hasNext()) {
            	Long eID = (Long)iterator.next();
            	out.write(j+" ");
            	Edge e = GraphPool.getSignleton().getEdge(eID);
            	Vertex v1 = GraphPool.getSignleton().getVertex(e.getV1());
            	Vertex v2 = GraphPool.getSignleton().getVertex(e.getV2());
            	out.write(String.valueOf(v1.getloc().m_pCoords[0])+" "+String.valueOf(v1.getloc().m_pCoords[1])+"\n");
			    out.write(String.valueOf(v2.getloc().m_pCoords[0])+" "+String.valueOf(v2.getloc().m_pCoords[1])+"\n");
			    out.write("END"+"\n");
			    j++;
            }
            out.write("END");
            out.flush();
			out.close();
			Iterator<Integer> itr = childRNet.getAllVertexes().iterator();
			int fanOutC = 0;
			while (itr.hasNext()) {
				Integer vexID = (Integer)itr.next();
				Vertex v = GraphPool.getSignleton().getVertex(vexID);
				if (v.getNeighbors().size()>4)
					fanOutC++;
			}
			System.out.println("fanout count > 2: "+fanOutC);
			Iterator<Integer> bListIter = childRNet.getBorderList().iterator();
			double max_x=0, max_y=0, min_x=0, min_y=0; 
			if (bListIter.hasNext()){
				Integer bID = (Integer)bListIter.next();
				Point p = GraphPool.getSignleton().getVertex(bID).getloc();
				double x = p.m_pCoords[0];
				double y = p.m_pCoords[1];
				max_x=x; max_y=y; min_x=x; min_y=y;
			}
			while (bListIter.hasNext()) {
				Integer bID = (Integer)bListIter.next();
				Point p = GraphPool.getSignleton().getVertex(bID).getloc();
				double x = p.m_pCoords[0];
				double y = p.m_pCoords[1];
				if (x>max_x)
					max_x = x;
				if (x<min_x)
					min_x = x;
				if (y>max_y)
					max_y = y;
				if (y<min_y)
					min_y = y;
			}
			double square = (max_x-min_x)*(max_y-min_y);
			System.out.println(max_x+" "+min_x+" "+max_y+" "+min_y);
			System.out.println("square:"+square);
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		RNetDisplay display = new RNetDisplay(args[0],args[1],Integer.valueOf(args[2]));
	}

}
