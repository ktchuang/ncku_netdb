package arbor.lbs.uqp.graph.util;

import java.io.Serializable;

public class Edge implements Serializable{
	private long eID;
	private int v1;
	private int v2;
	private double weight; 
	
	public Edge(int v1, int v2, double weight) {
		
		this.v1 = v1 < v2 ? v1 : v2;
		this.v2 = v1 < v2 ? v2 : v1;
		this.weight = weight;
		eID = hashkey();
	}

	public long getId() {
		return eID;
	}
	
	public int getV1() {
		return v1;
	}

	public int getV2() {
		return v2;
	}
	public double getWeight() {
		return weight;
	}
	public long hashkey() {
		return getEdgeHashCode(v1,v2);
	}
	public static long getEdgeHashCode(int vexter1, int vexter2) {
		long base = 500001;
		long tmp1 = vexter1 < vexter2 ? vexter1 : vexter2;
		long tmp2 = vexter1 < vexter2 ? vexter2 : vexter1;
		
		long result = 1;
		
		result = base * tmp1 + tmp2;
		return result;		
	}
	@Override
	public String toString() {
		return  "edge "+"("+v1 + "-" + v2+")";
	}
	
	
}