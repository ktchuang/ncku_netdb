package arbor.lbs.upq.simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import arbor.lbs.upq.simulator.util.Grid;
import arbor.lbs.upq.simulator.util.GridGroup;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.Vertex;

public class RoadPartitioning {

	public RoadPartitioning(String networkFn, Integer level, Integer factor) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(networkFn);
		
		List<Vertex> vertexLst = GraphPool.getSignleton().getAllVertexes();
		double minX, minY, maxX, maxY;
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
		
		int binCnt = factor*factor*factor;
		int xBin = (int) ((maxX-minX)/binCnt);
		int yBin = (int) ((maxY-minY)/binCnt);
		HashMap<Integer,Grid> gridData = new HashMap<Integer,Grid>();
		HashMap<Integer,HashMap<Integer,HashSet<Integer>>> gridCnt = new HashMap<Integer,HashMap<Integer,HashSet<Integer>>>();
		int maxXBin = 0, maxYBin = 0;
		for (int j=0; j<vertexLst.size(); j++) {
			Vertex v = vertexLst.get(j);
			double x = v.getloc().getCoord(0)*1000;
			double y = v.getloc().getCoord(1)*1000;
			int xKey = (int) ((x - minX)/xBin);
			int yKey = (int) ((y - minY)/yBin);
			if (xKey>maxXBin)
				maxXBin = xKey;
			if (yKey>maxYBin)
				maxYBin = yKey;
			if (gridCnt.containsKey(xKey)) {
				HashMap<Integer,HashSet<Integer>> column = gridCnt.get(xKey);
				if (column.containsKey(yKey)) {
					HashSet<Integer> vertexSet = column.get(yKey);
					vertexSet.add(v.getId());
					column.put(yKey, vertexSet);
					gridCnt.put(xKey, column);
				}
				else {
					HashSet<Integer> vertexSet = new HashSet<Integer>();
					vertexSet.add(v.getId());
					column.put(yKey, vertexSet);
					gridCnt.put(xKey, column);
				}
			}
			else {
				HashSet<Integer> vertexSet = new HashSet<Integer>();
				vertexSet.add(v.getId());
				HashMap<Integer,HashSet<Integer>> column = new HashMap<Integer,HashSet<Integer>>();
				column.put(yKey, vertexSet);
				gridCnt.put(xKey, column);
			}
		}
		
		for (int i=0; i<=maxXBin; i++) {
			HashMap<Integer,HashSet<Integer>> column = gridCnt.get(i);
			for (int j=0; j<=maxYBin; j++) {
				if (!column.containsKey(j)) {
					column.put(j, null);
				}
			}
			gridCnt.put(i, column);
		}
		
		for (int i=0; i<=maxXBin; i++) {
			HashMap<Integer,HashSet<Integer>> column = gridCnt.get(i);
			for (int j=0; j<=maxYBin; j++) {
				int gIdx = (i*(maxYBin+1))+j;
				int down = -1;
				if (j!=maxYBin)
					down = gIdx+1;
				int right = -1, corner = -1;
				if (i!=maxXBin) {
					right = gIdx+maxYBin+1;
					if (j!=maxYBin)
						corner = right+1;
				}
				if ((right == 4290)||(down ==4290)||(corner==4290))
					System.out.println(gIdx+":"+i+":"+j);
				HashSet<Integer> set = column.get(j);
				Grid oneGrid = new Grid(gIdx, i, j, right,down, corner, set);
				gridData.put(gIdx, oneGrid);
			}
		}
		int times = 0;
		int allNum = GraphPool.getSignleton().getAllVertexes().size();
		int leafVNum = allNum/factor;
		HashMap<Integer,HashSet<Integer>> clusterRes = GridPartitioning(factor,0,gridData,maxXBin,maxYBin,leafVNum);
		/*
		HashMap<Integer,HashSet<Integer>> refineCluster = null;
		for (int p=0; p<times; p++) {
			refineCluster = Refine(factor,clusterRes,gridData,maxXBin,maxYBin);
			clusterRes = refineCluster;
		}
		*/
		Iterator<Integer> clusterIter = clusterRes.keySet().iterator();
		leafVNum /= factor;
		while (clusterIter.hasNext()) {
			int clusterID = clusterIter.next();
			Iterator<Integer> setIter = clusterRes.get(clusterID).iterator();
			HashMap<Integer,Grid> subGridData = new HashMap<Integer,Grid>();
			while (setIter.hasNext()) {
				int sID = setIter.next();
				Grid g = gridData.get(sID);
				subGridData.put(sID,g);
			}
			HashMap<Integer,HashSet<Integer>> res = GridPartitioning(factor,1,subGridData,maxXBin,maxYBin,leafVNum);
			OutputCluster(res,gridData,networkFn);
		}
		//OutputCluster(res,gridData,networkFn);
	}

	private void OutputCluster(HashMap<Integer, HashSet<Integer>> refineCluster,
			HashMap<Integer, Grid> gridData, String networkFn) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(refineCluster.size());
		Iterator<Integer> clusterIter = refineCluster.keySet().iterator();
		while (clusterIter.hasNext()) {
			int clusterID = clusterIter.next();
			HashSet<Integer> gridSet = refineCluster.get(clusterID);
			Iterator<Integer> gSetIter = gridSet.iterator();
			int count = 0;
			while (gSetIter.hasNext()) {
				int gIdx = (Integer)gSetIter.next();
				HashSet<Integer> vSet = gridData.get(gIdx).getAllVertex();
				if (vSet != null)
					count += vSet.size();
			}
			System.out.println(count);
		}
		
		clusterIter = refineCluster.keySet().iterator();
		while (clusterIter.hasNext()) {
			int clusterID = clusterIter.next();
			String outFn = networkFn+"_point_"+clusterID+".txt";
			FileWriter out = new FileWriter(new File(outFn));
			out.write("x,y\n"); 
			HashSet<Integer> gridSet = refineCluster.get(clusterID);
			Iterator<Integer> gSetIter = gridSet.iterator();
			int count = 0;
			while (gSetIter.hasNext()) {
				int gIdx = (Integer)gSetIter.next();
				HashSet<Integer> vSet = gridData.get(gIdx).getAllVertex();
				if (vSet!= null){
					Iterator<Integer> vSetIter = vSet.iterator();
					while (vSetIter.hasNext()) {
						int vID = (Integer)vSetIter.next();
						Vertex v = GraphPool.getSignleton().getVertex(vID);
						double x = v.getloc().getCoord(0);
						double y = v.getloc().getCoord(1);
						out.write(x+","+y+"\n");
					}
				}
			}
			out.flush();
			out.close();
		}
		
	}

	private HashMap<Integer, HashSet<Integer>> Refine(int factor,
			HashMap<Integer,HashSet<Integer>> clusterRes, HashMap<Integer, Grid> gridData,
			int maxXBin, int maxYBin) {
		// TODO Auto-generated method stub
		
		int vertexNum = GraphPool.getSignleton().getAllVertexes().size();
		int leafVNum = vertexNum/factor;
		
		HashMap<Integer,HashSet<Integer>> cluster = new HashMap<Integer,HashSet<Integer>>();
		HashMap<Integer,Integer> clusterCnt = new HashMap<Integer,Integer>();
		
		int gridSize = gridData.size();
		LinkedList<Integer> allElement = new LinkedList<Integer>();
		for (int i=0; i<gridSize; i++) {
			Grid g = gridData.get(i);
			allElement.add(g.getGID());
		}
		
		Iterator<Integer> iter = clusterRes.keySet().iterator();
		while (iter.hasNext()){
			int keyID = iter.next();
			HashSet<Integer> set = clusterRes.get(keyID);
			Iterator<Integer> setIter = set.iterator();
			int sumX=0;
			int sumY=0;
			while (setIter.hasNext()) {
				int elementID = setIter.next();
				Grid g = gridData.get(elementID);
				sumX += g.getxID();
				sumY += g.getyID();
			}
			int newX = sumX/set.size();
			int newY = sumY/set.size();
			int gIdx = (newX*(maxYBin+1))+newY;
			HashSet<Integer> gSet = new HashSet<Integer>();
			cluster.put(gIdx, gSet);
			clusterCnt.put(gIdx, 0);
			allElement.remove(gIdx);
		}
		
		Random rand = new Random();
		while (allElement.size()>0) {
			//for (int i=0; i<gridSize; i++) {
			int ranValue = (int) rand.nextFloat()*allElement.size();
			int gID = allElement.get(ranValue);
			allElement.remove(ranValue);
			Grid g = gridData.get(gID);
			int x = g.getxID();
			int y = g.getyID();
			iter = cluster.keySet().iterator();
			int minDist = maxXBin+maxYBin;
			int targetID = -1;
			while (iter.hasNext()){
				int seedID = iter.next();
				Grid seedGrid = gridData.get(seedID);
				int seedX = seedGrid.getxID();
				int seedY = seedGrid.getyID();
				int dist = Math.abs(x-seedX)+Math.abs(y-seedY);
				if ((dist<minDist)&&(clusterCnt.get(seedID)<leafVNum)) {
					minDist = dist;
					targetID = seedID;
				}
			}
			HashSet<Integer> group = cluster.get(targetID);
			group.add(gID);
			cluster.put(targetID,group);
			if (g.getAllVertex()!=null) {
				int tmpCnt = clusterCnt.get(targetID);
				tmpCnt += g.getAllVertex().size();
				clusterCnt.put(targetID, tmpCnt);
			}
			
		}
		return cluster;
	}

	private HashMap<Integer,HashSet<Integer>> GridPartitioning(Integer factor, int sub,
			HashMap<Integer, Grid> gridData, int maxXBin, int maxYBin, int leafVNum) throws IOException {
		// TODO Auto-generated method stub
		
		System.out.println(leafVNum);
		HashMap<Integer,HashSet<Integer>> cluster = new HashMap<Integer,HashSet<Integer>>();
		HashMap<Integer,Integer> clusterCnt = new HashMap<Integer,Integer>();
		//HashSet<Integer> selected = new HashSet<Integer>();

		int gridSize = gridData.size();
		LinkedList<Integer> allElement = new LinkedList<Integer>();
		Iterator<Integer> gridIter = gridData.keySet().iterator();
		while (gridIter.hasNext()) {
			int gID = gridIter.next();
			Grid g = gridData.get(gID);
			allElement.add(g.getGID());
		}
		
		Random rand = new Random();
		if (sub ==1) {
			for (int i=0; i<factor; i++) {
				int seed = (int) (rand.nextFloat()*allElement.size());
				int gID = allElement.get(seed);
				HashSet<Integer> gSet = new HashSet<Integer>();
				cluster.put(gID, gSet);
				clusterCnt.put(gID, 0);
				allElement.remove(seed);
			}
		}
		else {
			int[] seedArray = {1056, 1088, 3136,3168};
			for (int i=0; i<factor; i++) {
				int seed = seedArray[i];
				HashSet<Integer> gSet = new HashSet<Integer>();
				cluster.put(seed, gSet);
				clusterCnt.put(seed, 0);
				allElement.remove(seed);
			}
		}
		
		while (allElement.size()>0) {
		//for (int i=0; i<gridSize; i++) {
			int ranValue = (int) rand.nextFloat()*allElement.size();
			int gID = allElement.get(ranValue);
			allElement.remove(ranValue);
			Grid g = gridData.get(gID);
			int x = g.getxID();
			int y = g.getyID();
			Iterator<Integer> iter = cluster.keySet().iterator();
			int minDist = maxXBin+maxYBin;
			int targetID = -1;
			while (iter.hasNext()) {
				int seedID = iter.next();
				Grid seedGrid = gridData.get(seedID);
				int seedX = seedGrid.getxID();
				int seedY = seedGrid.getyID();
				int dist = Math.abs(x-seedX)+Math.abs(y-seedY);
				if ((dist<minDist)&&(clusterCnt.get(seedID)<leafVNum)) {
					minDist = dist;
					targetID = seedID;
				}
			}
			HashSet<Integer> group = cluster.get(targetID);
			group.add(gID);
			cluster.put(targetID,group);
			if (g.getAllVertex()!=null) {
				int tmpCnt = clusterCnt.get(targetID);
				tmpCnt += g.getAllVertex().size();
				clusterCnt.put(targetID, tmpCnt);
			}
			
		}
		
		/*
		int realCnt = 0;
		HashSet<Integer> group = new HashSet<Integer>();
		int groupID = 0;
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(0);
		HashSet<Integer> selectedGrid = new HashSet<Integer>();
		while (!queue.isEmpty()) {
			if (realCnt<leafVNum) {
				Integer gID = queue.pollFirst();
				selectedGrid.add(gID);
				Grid g = gridData.get(gID);
				//System.out.println(gID);
				if (g.getAllVertex()!=null){
					realCnt += g.getAllVertex().size();
				}
				group.add(g.getGID());
				int rID = g.getRightGID();
				if (rID!= -1)
					if ((!queue.contains(rID))&&(!selectedGrid.contains(rID)))
						queue.addLast(rID);
				int dID = g.getDownGID();
				if (dID!= -1)
					if ((!queue.contains(dID))&&(!selectedGrid.contains(dID)))
						queue.addLast(dID);
				int cID = g.getCornetGID();
				if (cID!= -1)
					if ((!queue.contains(cID))&&(!selectedGrid.contains(cID)))
						queue.addLast(cID);
			}
			else {
				GridGroup gGroup = new GridGroup(group);
				cluster.put(groupID, gGroup);
				groupID++;
				group = new HashSet<Integer>();
				realCnt = 0;
			}
		}
		if (group.size()>0) {
			GridGroup gGroup = new GridGroup(group);
			cluster.put(groupID, gGroup);
		}
		*/
		return cluster;
		
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		RoadPartitioning roadPart = new RoadPartitioning(args[0],Integer.valueOf(args[1]), Integer.valueOf(args[2]));
	}

}
