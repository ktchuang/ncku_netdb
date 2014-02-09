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
import java.util.List;
/**
 * The pool contains all vertexes and edges in the db
 * @author ktchuang
 *
 */
public class GraphPool implements Serializable {
	private static GraphPool me = null;
	private HashMap<Integer, Vertex> vertexes = null;
	/*The edge's key is the hashcode!*/
	private HashMap<Long, Edge> edges = null;
	
    private GraphPool() {    	
    	vertexes = new HashMap<Integer, Vertex>();
    	edges = new HashMap<Long, Edge>();    	
    }
    public static GraphPool getSignleton() {
    	if (me == null) {
    		me = new GraphPool();
    	}
    	return me;
    }
    public Vertex getVertex(int vID) {
    	if (vertexes.containsKey(vID)) {
    		return vertexes.get(vID);
    	}
    	else {
    		return null;
    	}
    }
    public Edge getEdge(int v1, int v2) {
    	long hashcode = Edge.getEdgeHashCode(v1,v2);
    	if (edges.containsKey(hashcode)) {
    		return edges.get(hashcode);
    	}
    	else {
    		return null;
    	}
    }
    public Edge getEdge(long eID) {
    	if (edges.containsKey(eID)) {
    		return edges.get(eID);
    	}
    	else {
    		return null;
    	}
    }
    public List<Vertex> getAllVertexes() {
    	ArrayList<Vertex> list = new ArrayList<Vertex>();
    	Object[] obj = this.vertexes.values().toArray();
    	for (int i=0;i<obj.length;i++)
    		list.add((Vertex)obj[i]);
    	return list;
    }
    public List<Edge> getAllEdges() {
    	ArrayList<Edge> list = new ArrayList<Edge>();
    	Object[] obj = this.edges.values().toArray();
    	for (int i=0;i<obj.length;i++)
    		list.add((Edge)obj[i]);
    	return list;
    }
    
	public void insertVertex(Vertex v) {
		if (!vertexes.containsKey(v.getId())) {
			vertexes.put(v.getId(), v);
		}
	}
	public void insertEdge(Edge e) {
		long hash = e.getId();
		if (!edges.containsKey(hash)) {
			edges.put(hash, e);
			linkNeighbor(e.getV1(),e.getV2());
		}
		else {
			Edge test = edges.get(hash);
			if (test.getV1()!=e.getV1() || test.getV2()!=e.getV2()) {
				System.err.println("edge hash conflict!!");
				System.out.println(test.hashkey());
				System.out.println(e.hashkey());
				//System.exit(0);
			}
		}
	}
	public boolean removeVertex(Vertex v) {
		if (!vertexes.containsKey(v.getId())) {
			return false;
		}
		else {
			vertexes.remove(v.getId());
			return true;
		}
	}
	public void linkNeighbor(int v1, int v2) {
		Vertex vex1 = vertexes.get(v1);
		Vertex vex2 = vertexes.get(v2);
		vex1.insertNeighbor(v2);
		vex2.insertNeighbor(v1);
	}
	public void saveGraph(String fn) {
		try {
		  FileOutputStream fs = new FileOutputStream(fn);
		  ObjectOutputStream os =  new ObjectOutputStream(fs);  
		  os.writeObject(me);
		  os.close();
		}catch (IOException e) {
			System.out.println(e);
		}
	}		
	public static void loadGraph(String fn) {
		try {
		  FileInputStream fs = new FileInputStream(fn);
		  ObjectInputStream in =  new ObjectInputStream(fs);  
		  GraphPool inst = (GraphPool)in.readObject();
		  me = inst;
		  in.close();
		}catch (IOException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
