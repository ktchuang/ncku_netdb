package arbor.lbs.uqp.graph.util;

import java.io.Serializable;
import java.util.*;
import arbor.lbs.rtree.spatialindex.Point;

public class Vertex implements Serializable{
	private int vID;
	private TreeSet<Integer> borderLevelMap = null;
	private Point loc;
	private String name = null;
	private HashSet<Integer> neighbor = null;
	private HashSet<Integer> assocObj = null;
	private TreeMap<Integer,Integer> rnetMap = null;
	private int hqNetID = -1;
	private int topSearchLevel = -1;
	public Vertex(int id) {
		this.vID = id;
		this.name = null;
		neighbor = new HashSet<Integer>();
		rnetMap = new TreeMap<Integer,Integer>();
	}
	public Vertex(int id, double lat, double lon) {
		this.vID = id;
		this.name = null;
		neighbor = new HashSet<Integer>();
		double[] xy = new double[2];
		xy[0] = lat;
		xy[1] = lon;
		loc = new Point(xy);
		rnetMap = new TreeMap<Integer,Integer>();
	}	
	public Vertex(int id, String name) {
		this.vID = id;
		this.name = name;
		neighbor = new HashSet<Integer>();
		rnetMap = new TreeMap<Integer,Integer>();
	}	
	public int getId() {
		return vID;
	}
	public int getHQNetID() {
		return this.hqNetID;
	}
	public void setRNet(int level, int rID) {
		rnetMap.put(level, rID);
	}
	public void setHQNetID(int hID) {
		hqNetID = hID;
	}
	public int getInsideRNetID(int level) {
		return rnetMap.get(level);
	}
	public int getLeafRnetID() {
		if (rnetMap.lastKey() <rnetMap.firstKey()) {
			System.err.println("error");
			System.exit(0);
		}
		return rnetMap.get(rnetMap.lastKey());
		
	}
	
	public void insertAssocMsgObj(int msgID) {
		if (this.assocObj == null)
			this.assocObj = new HashSet<Integer>();
		this.assocObj.add(msgID);
	}
	public int deleteOneAssocMsgObj() {
		if (assocObj.size()>0) {
			Object[] val = assocObj.toArray();
			assocObj.remove(val[0]);
			return (Integer)val[0];
		}
		else {
			return -1;
		}
	}
	
	public LinkedList<Integer> getAssocMsgObj() {
		LinkedList<Integer> list = new LinkedList<Integer>();
		if (assocObj!=null) {
			Object[] val = assocObj.toArray();
			for (int i=0;i<val.length;i++)
				list.add((Integer)val[i]);
		}
		return list;
	}
	public boolean hasAssocMsgObj() {
		return (this.assocObj==null) ? false : true;
	}
	
	public String getName() {
		return name;
	}
	public void insertNeighbor(int neighborID) {
		neighbor.add(neighborID);
	}
	public void removeNeighbor(int neighborID) {
		if (neighbor.contains(neighborID)) {
			neighbor.remove(neighborID);
		}
	}
	public boolean isNeighbor(int vID){
		return neighbor.contains(vID);
	}
	public List<Integer> getNeighbors() {
		ArrayList<Integer> list = new ArrayList<Integer>(); 
		Object[] obj = neighbor.toArray();
		for (int i=0;i<obj.length;i++){
			list.add(((Integer)obj[i]).intValue());
		}
		return list;
	}
	
	public void setBorder(int level) {
		if (this.borderLevelMap ==  null) {
			this.borderLevelMap = new TreeSet<Integer>();
		}
		this.borderLevelMap.add(level);
	}
	public int getHighestBorderLevel() {
		if (borderLevelMap == null) {
			return -1;
		}
		else {
		  if (borderLevelMap.first() >  borderLevelMap.last()) {
			System.err.println("err");
			System.exit(0);
		  }
		  return borderLevelMap.first();
		}		
		
	}
	
	public void setTopSearchLevel(int level) {
		this.topSearchLevel = level;
	}
	
	public int getTopSearchLevel() {
		return topSearchLevel;
	}
	
	public Point getloc(){
		return this.loc;
	}
	public boolean isBorderNode(int level) {
		if (this.borderLevelMap ==  null)
			return false;
		else {			
		  if (this.borderLevelMap.contains(level)) {
			  return true;
		  }
		  else
			  return false;
		}
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + Integer.toString(vID).hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		Vertex other = (Vertex) obj;
		if (other.getId() == this.vID)
			return true;
		else 
			return false;
	}

	@Override
	public String toString() {
		return "Vertex_" + Integer.toString(vID);		
	}
	
}
