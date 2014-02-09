package arbor.lbs.uqp.graph.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Graph implements Serializable{
	private HashSet<Integer> vertexes;
	private HashSet<Long> edges;

	
	public Graph(List<Vertex> vertexes, List<Edge> edges) {
		this.vertexes = new HashSet<Integer>();
		for (int i =0;i<vertexes.size();i++) {
			Vertex v = vertexes.get(i);			
			this.vertexes.add(v.getId());
		}		
		this.edges = new HashSet<Long>();
		for (int i=0;i<edges.size();i++) {
			Edge e = edges.get(i);			
			this.edges.add(e.getId());
		}				
		
	}	
	public Graph() {
		this.vertexes = new HashSet<Integer>();
		this.edges = new HashSet<Long>();
	}
	
	public boolean containVertex(int vID) {
		if (vertexes.contains(vID)) {
			return true;
		}
		else 
			return false;
	}
	public int getNumEdges() {
		return edges.size();
	}
	public int getNumVertexes() {
		return vertexes.size();
	}
	public boolean containEdge(long eID) {
		if (edges.contains(eID)) {
			return true;
		}
		else 
			return false;
	}
	public void insertVertex(int vID) {
		vertexes.add(vID);
	}
	public void insertEdge(long eID) {
		edges.add(eID);
	}
	public List<Integer> getVertexes() {		
		ArrayList<Integer> vlist = new ArrayList<Integer>();
		Object[] val = this.vertexes.toArray();
		for (int i = 0;i < val.length;i++) {
		  vlist.add((Integer)val[i]);
		}
		return vlist;
	}

	public List<Long> getEdges() {
		ArrayList<Long> elist = new ArrayList<Long>();
		Object[] val = this.edges.toArray();
		for (int i = 0;i < val.length;i++) {
		  elist.add((Long)val[i]);
		}
		return elist;
	}
	
	public Vertex getVertex(int vID) {
		return GraphPool.getSignleton().getVertex(vID);
	}
	
	public Edge getEdge(int vertex1, int vertex2) {
		return GraphPool.getSignleton().getEdge(vertex1,vertex2);
	}
	
	
	public static Graph doGraphJoin(Graph agraph, Graph bgraph) {
		List<Long> ealist = agraph.getEdges();
		List<Integer> valist = agraph.getVertexes();	
		List<Long> eblist = bgraph.getEdges();
		List<Integer> vblist = bgraph.getVertexes();

		Graph newg = new Graph();
		for (int i=0;i<ealist.size();i++) {
			newg.insertEdge(ealist.get(i));
		}
		for (int i=0;i<valist.size();i++) {
			newg.insertVertex(valist.get(i));
		}		
		for (int i=0;i<eblist.size();i++) {
			newg.insertEdge(eblist.get(i));
		}
		for (int i=0;i<vblist.size();i++) {
			newg.insertVertex(vblist.get(i));
		}
		return newg;
	}
}