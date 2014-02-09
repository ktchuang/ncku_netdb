package arbor.lbs.uqp.graph.dijkstra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.Graph;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.MsgObjPath;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.Vertex;

public class Dijkstra {
	HashMap<Integer, Double> dist;
	HashMap<Integer, Integer> previous;
	Graph graph;
	public Dijkstra(Graph graph) {
		dist = new HashMap<Integer, Double>();
		previous = new HashMap<Integer, Integer>();
		this.graph = graph;
		List<Integer> vertexes = graph.getVertexes();
		for (int i = 0;i<vertexes.size();i++) {
			dist.put(vertexes.get(i), Double.MAX_VALUE);
			previous.put(vertexes.get(i), Integer.MIN_VALUE);
		}
	}
	public Path execute(int srcVertexID, int destVertexID) {
		dist.put(srcVertexID,0.0);
		HashSet<Integer> queue = new HashSet<Integer>();
		List<Integer> vertexes = graph.getVertexes();
		boolean finish = false;
		for (int i =0;i<vertexes.size();i++) {
			queue.add(vertexes.get(i));
		}
		
		while (!queue.isEmpty() && !finish) {
			Object[] obj = queue.toArray();
			double min = Double.MAX_VALUE;
			int minV = -1;
			for (int i=0;i<obj.length;i++) {
				Double cost = dist.get((Integer)obj[i]);
				if (cost<min) {
					min = cost;
					minV = (Integer)obj[i];
				}					
			}
			if (minV == -1) {
				/*No accessible to other nodes, break!*/
				finish = true;
			}
			else {
				if (minV == destVertexID) {
				  finish = true;
				}
				else {
				  queue.remove(minV);
				  Vertex vertex = graph.getVertex(minV);
				  List<Integer> neighbor = vertex.getNeighbors();
				  for (int i=0;i<neighbor.size();i++) {
					int vertex2 = neighbor.get(i);
					if (queue.contains(vertex2)) { /*where v2 has not yet been removed from queue.*/
						double cost = dist.get(minV);
						cost += graph.getEdge(minV, vertex2).getWeight();
						double distv2 = dist.get(vertex2);
						if (cost < distv2) {
							dist.put(vertex2, cost);
							previous.put(vertex2, minV);
						}
					}
				  }
				}
			}
		}
		Path p = getPath(destVertexID);
		return p;
	}
	/**
	 * Use when searching the source query to Borders. exec this after call execute()
	 * @param srcVertexID
	 * @return
	 */
	public TreeSet<MsgObjPath> getPath2Borders(RNet rnet) {
		List<Integer> borderList = rnet.getBorderList();	
		TreeSet<MsgObjPath> pathSet = new TreeSet<MsgObjPath>();
	    
		for (int i=0;i<borderList.size();i++) {
			Path p = getPath(borderList.get(i));
			MsgObjPath op = new MsgObjPath();
			op.pathList = p.pathList;
			op.cost = p.cost;
			op.currentVisitedRNetLevel = rnet.getLevel();
			pathSet.add(op);
		}
		return pathSet;		
	}
	/**
	 * Use when searching the source query to object if the rnet contains object.
	 * @param srcVertexID
	 * @return
	 */
	public TreeSet<MsgObjPath> getPath2Objects(RNet rnet) {
		LinkedList<Integer> vContainObjects = rnet.getVertexesContainingObject();
		TreeSet<MsgObjPath> pathSet = new TreeSet<MsgObjPath>();
			    
		for (int i=0;i<vContainObjects.size();i++) {
			Vertex v= GraphPool.getSignleton().getVertex(vContainObjects.get(i));
			Path p = getPath(vContainObjects.get(i));
			if (p!=null) {
			  LinkedList<Integer> oList = v.getAssocMsgObj();
			  for (int j=0;j<oList.size();j++) {
				MsgObjPath mpath = new MsgObjPath();
				mpath.msgID =oList.get(j);
				mpath.assocVID = v.getId();
				Path op = p.clone();
				mpath.pathList = op.pathList;
				mpath.cost = op.cost;
				mpath.currentVisitedRNetLevel = rnet.getLevel();
			    pathSet.add(mpath);
			  }
			}
		}
		return pathSet;
		
	}
	/**
	 * Use when searching SPs bettween borders and objects in the RNet
	 * @param srcVertexID
	 * @param numMaxList
	 * @return
	 */
	public TreeSet<MsgObjPath> executeDijkstra2Objects(int srcVertexID, int numMaxList) {
		LinkedList<MsgObjPath> pathSet = new LinkedList<MsgObjPath>();
		dist.put(srcVertexID,0.0);
		HashSet<Integer> queue = new HashSet<Integer>();
		List<Integer> vertexes = graph.getVertexes();
		boolean finish = false;
		for (int i =0;i<vertexes.size();i++) {
			queue.add(vertexes.get(i));
		}
		
		while (!queue.isEmpty() && !finish) {
			Object[] obj = queue.toArray();
			double min = Double.MAX_VALUE;
			int minV = -1;
			for (int i=0;i<obj.length;i++) {
				double cost = dist.get((Integer)obj[i]);
				if (cost<min) {
					min = cost;
					minV = (Integer)obj[i];
				}					
			}
			if (minV == -1) {
				/*No accessible for other nodes, break!*/
				finish = true;
			}
			else {
				if (graph.getVertex(minV).hasAssocMsgObj()) {
					  LinkedList<Integer> msgList = graph.getVertex(minV).getAssocMsgObj();
					  for (int i=0;i<msgList.size();i++) {
					    MsgObjPath o = new MsgObjPath();
					    o.assocVID = minV;
					    o.msgID = msgList.get(i);
					    pathSet.add(o);
					  }	
					  if (pathSet.size() > numMaxList) { 
						 finish = true;
						 continue;
					  }
				}
				queue.remove(minV);
				Vertex vertex = graph.getVertex(minV);
				List<Integer> neighbor = vertex.getNeighbors();
				for (int i=0;i<neighbor.size();i++) {
					int vertex2 = neighbor.get(i);
					if (queue.contains(vertex2)) { /*where v2 has not yet been removed from queue.*/
						double cost = dist.get(minV);
						cost += graph.getEdge(minV, vertex2).getWeight();
						double distv2 = dist.get(vertex2);
						if (cost < distv2) {
							dist.put(vertex2, cost);
							previous.put(vertex2, minV);
						}
					}
				}
			}
		}
		
		TreeSet<MsgObjPath> ret = new TreeSet<MsgObjPath>();
		for (int i=0;i<pathSet.size();i++) {	
		    MsgObjPath op = pathSet.get(i);
			Path p = getPath(op.assocVID);
			if (p!=null) {
			  op.pathList = p.pathList;
			  op.cost = p.cost;
			  ret.add(op);
			}
		}
		return ret;
				
	}
	public void execute(int srcVertexID) {
		dist.put(srcVertexID,0.0);
		HashSet<Integer> queue = new HashSet<Integer>();
		List<Integer> vertexes = graph.getVertexes();
		boolean finish = false;
		for (int i =0;i<vertexes.size();i++) {
			queue.add(vertexes.get(i));
		}
		
		while (!queue.isEmpty() && !finish) {
			Object[] obj = queue.toArray();
			double min = Double.MAX_VALUE;
			int minV = -1;
			for (int i=0;i<obj.length;i++) {
				double cost = dist.get((Integer)obj[i]);
				if (cost<min) {
					min = cost;
					minV = (Integer)obj[i];
				}					
			}
			if (minV == -1) {
				/*No accessible for other nodes, break!*/
				finish = true;
			}
			else {
				queue.remove(minV);
				Vertex vertex = graph.getVertex(minV);
				List<Integer> neighbor = vertex.getNeighbors();
				for (int i=0;i<neighbor.size();i++) {
					int vertex2 = neighbor.get(i);
					if (queue.contains(vertex2)) { /*where v2 has not yet been removed from queue.*/
						double cost = dist.get(minV);
						cost += graph.getEdge(minV, vertex2).getWeight();
						double distv2 = dist.get(vertex2);
						if (cost < distv2) {
							dist.put(vertex2, cost);
							previous.put(vertex2, minV);
						}
					}
				}
			}
		}
		
	}
	private double getShortestDistance(int destVertexID) {
		Double d = dist.get(destVertexID);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
	 * This method returns the path from the source to the selected target and
	 * NULL if no path exists
	 */
	public Path getPath(int destID) {
		LinkedList<Integer> path = new LinkedList<Integer>();
		int step = destID;
		// Check if a path exists
		if (previous.get(step) == Integer.MIN_VALUE) {
			return null;
		}
		path.add(step);
		while (previous.get(step) != Integer.MIN_VALUE) {
			step = previous.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		Path p = new Path();
		p.pathList = path;
		p.cost = getShortestDistance(destID);
		return p;
	}	
}
