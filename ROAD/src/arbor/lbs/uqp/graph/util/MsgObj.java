package arbor.lbs.uqp.graph.util;

import java.io.Serializable;

import arbor.lbs.rtree.spatialindex.Point;

public class MsgObj implements Serializable {
    int msgID;
	String name;
	Point loc;
	int assocVID;
	public MsgObj(int msgID, Point location) {
		this.msgID = msgID;
		this.loc = (Point) location.clone();
	}
	public MsgObj(int msgID, double lon, double lat) {
		double[] xy = new double[2];
		xy[0] = lon;
		xy[1] = lat;
		loc = new Point(xy);
		this.msgID = msgID;
	}
	public void setAssocVertex(int vID) {
		this.assocVID = vID;
	}
	public int getAssocVertex() {
		return this.assocVID;
	}
}
