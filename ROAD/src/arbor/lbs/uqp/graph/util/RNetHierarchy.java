package arbor.lbs.uqp.graph.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import arbor.lbs.uqp.algorithm.VisitedObject;

public class RNetHierarchy implements Serializable {
	private static RNetHierarchy me = null;

	int maxLevel;
	HashMap<Integer, RNet> rMap;
	HashMap<Integer, LinkedList<Integer>> levelMap;

	public static RNetHierarchy getSignleton() {
		if (me == null) {
			me = new RNetHierarchy();
		}
		return me;
	}

	private RNetHierarchy() {
		rMap = new HashMap<Integer, RNet>();
		levelMap = new HashMap<Integer, LinkedList<Integer>>();
	}

	public LinkedList<Integer> getLevelRNet(int level) {
		return levelMap.get(level);
	}
	/**
	 * Please insert from root!
	 * 
	 * @param rnet
	 */
	public void insertRNet(RNet rnet) {
		rMap.put(rnet.getRNetID(), rnet);
		checkMaxLevel(rnet.getLevel());
		if (!rnet.isRootRNet()) {
		  RNet par = rMap.get(rnet.getParent());		
		  par.insertChildRNet(rnet.getRNetID());
		}
		if (levelMap.containsKey(rnet.getLevel())) {
			LinkedList<Integer> l = levelMap.get(rnet.getLevel());
			l.add(rnet.getRNetID());
		} else {
			LinkedList<Integer> l = new LinkedList<Integer>();
			l.add(rnet.getRNetID());
			levelMap.put(rnet.getLevel(), l);
		}
	}
	public RNet getRNet(int rID) {
		return rMap.get(rID);
	}

	public int getMaxLevel() {
		return this.maxLevel;
	}
	public void checkMaxLevel(int level) {
		if (level > maxLevel)
			maxLevel = level;
	}
	public void SetShortCut() {
		LinkedList<Integer> leafRNetID= getLevelRNet(maxLevel);
		for (int i=0; i<leafRNetID.size(); i++) {
			RNet rnet = rMap.get(leafRNetID.get(i));
			rnet.buildShortCut();
		}
		for (int j=maxLevel-1; j>0; j--) {
			LinkedList<Integer> rnetList= getLevelRNet(j);
			for (int m=0; m<rnetList.size(); m++) {
				RNet rnet = rMap.get(rnetList.get(m));
				LinkedList<Integer> bSet = rnet.getBorderList();
				for (int n=0; n<bSet.size(); n++) {
					TreeSet<VisitedObject> queue = new TreeSet<VisitedObject>();
			    	HashSet<Integer> visitedN = new HashSet<Integer>();
			    	VisitedObject obj = new VisitedObject(bSet.get(n),-1,0); 
			    	queue.add(obj);
			    	while (queue.size()>0) {
			    		VisitedObject popObj = queue.pollFirst();
			    		if (visitedN.contains(popObj.getVID())) {
			    			continue;
			    		}
			    		if  ((bSet.contains(popObj.getVID()))&&(popObj.getVID()!=bSet.get(n))) {
			    			ShortCutSet scSet = rnet.getShortCutSet(bSet.get(n));
			    			Path p = new Path();
			    			p.cost = popObj.getDist();
			    			scSet.insertShortCut(p, popObj.getVID());
			    		}
			    		Vertex popVertex = GraphPool.getSignleton().getVertex(popObj.getVID());
			    		List<Integer> neighbors = popVertex.getNeighbors();
    	        		for (int q = 0; q<neighbors.size(); q++) {
    	        			Vertex neiVertex = GraphPool.getSignleton().getVertex(neighbors.get(q));
    	        			if (neiVertex.isBorderNode(j+1)) {
    	        				Edge e = GraphPool.getSignleton().getEdge(popVertex.getId(), neiVertex.getId());
    	            			double newDist = popObj.getDist()+e.getWeight();
    	            			VisitedObject oneObj = new VisitedObject(neiVertex.getId(),-1,newDist); 
    	        				queue.add(oneObj);
    	        			}
    	        			
    	        		}
    	        		Integer popBRID = popVertex.getInsideRNetID(j+1);
    	        		RNet popRNet = RNetHierarchy.getSignleton().getRNet(popBRID);
			    		ShortCutSet scSet = popRNet.getShortCutSet(popVertex.getId());
	    	    		if (scSet==null) {
	    	    			visitedN.add(popObj.getVID());
	    	    			continue;
	    	    		}
	    	    		HashMap<Integer,Path> scMap = scSet.getSCMap();
	    	    		Iterator<Integer> scMIter = scMap.keySet().iterator();
	    	    		while (scMIter.hasNext()) {
	    	    			Integer destID = (Integer)scMIter.next();
	    	    			double newDist = popObj.getDist()+scMap.get(destID).cost;
	    	    			VisitedObject oneObj = new VisitedObject(destID.intValue(),-1,newDist); 
	    	    			queue.add(oneObj);
	    	    		}			    		
			    		visitedN.add(popObj.getVID());
			    	}
				}
				
				 
			}
		}
	}

	public void saveHierarchy(String fn) {
		try {
			FileOutputStream fs = new FileOutputStream(fn);
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(me);
			os.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void loadHierarchy(String fn) {
		try {
			FileInputStream fs = new FileInputStream(fn);
			ObjectInputStream in = new ObjectInputStream(fs);
			RNetHierarchy inst = (RNetHierarchy) in.readObject();
			me = inst;
			in.close();
		} catch (IOException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
