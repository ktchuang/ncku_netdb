package arbor.lbs.upq.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.Vertex;

public class RoadAnalyzer {

	public RoadAnalyzer(String networkFn, int xBinCnt, int yBinCnt) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(networkFn);
		
		double minX, minY, maxX, maxY;
		
		List<Vertex> vertexLst = GraphPool.getSignleton().getAllVertexes();
		minX = vertexLst.get(0).getloc().getCoord(0)*1000;
		maxX = minX;
		minY = vertexLst.get(0).getloc().getCoord(1)*1000;
		maxY = minY;
		for (int i=1; i<vertexLst.size(); i++) {
			Vertex v = vertexLst.get(i);
			double x = v.getloc().getCoord(0)*1000;
			double y = v.getloc().getCoord(1)*1000;
			if (x>maxX)
				maxX = x;
			if (x<minX)
				minX = x;
			if (y>maxY)
				maxY = y;
			if (y<minY)
				minY = y;
		}
		
		int xBin = (int) ((maxX-minX)/xBinCnt);
		HashMap<Integer,Integer> xBinCount = new HashMap<Integer,Integer>();
		int yBin = (int) ((maxY-minY)/yBinCnt);
		HashMap<Integer,Integer> yBinCount = new HashMap<Integer,Integer>();
		
		for (int j=0; j<vertexLst.size(); j++) {
			Vertex v = vertexLst.get(j);
			double x = v.getloc().getCoord(0)*1000;
			double y = v.getloc().getCoord(1)*1000;
			int xKey = (int) ((x - minX)/xBin);
			int nCnt = v.getNeighbors().size();
			if (xBinCount.containsKey(xKey)) {
				int num = xBinCount.get(xKey);
				num += 1;
				xBinCount.put(xKey, num);
			}
			else {
				xBinCount.put(xKey, 1);
			}
			int yKey = (int) ((y - minY)/yBin);
			if (yBinCount.containsKey(yKey)) {
				int num = yBinCount.get(yKey);
				num += 1;
				yBinCount.put(yKey, num);
			}
			else {
				yBinCount.put(yKey, 1);
			}
		}
		
		String output = networkFn+".x.csv";
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		
		int size = xBinCount.keySet().size();
		for(int j=0;j<size;j++) {
			if (xBinCount.containsKey(j)) {
				int count = xBinCount.get(j);
				writer.write(j+","+count+"\n");
			}
			else {
				writer.write(j+",0\n");
			}
		}
		writer.flush();
		writer.close();
		
		output = networkFn+".y.csv";
		writer = new BufferedWriter(new FileWriter(output));
		
		size = yBinCount.keySet().size();
		for(int j=0;j<size;j++) {
			if (yBinCount.containsKey(j)) {
				int count = yBinCount.get(j);
				writer.write(j+","+count+"\n");
			}
			else {
				writer.write(j+",0\n");
			}
		}
		writer.flush();
		writer.close();
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		RoadAnalyzer road = new RoadAnalyzer(args[0],Integer.valueOf(args[1]),Integer.valueOf(args[2]));
	}

}
