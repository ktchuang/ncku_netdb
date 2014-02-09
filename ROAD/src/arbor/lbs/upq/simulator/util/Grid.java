package arbor.lbs.upq.simulator.util;

import java.util.HashMap;
import java.util.HashSet;

import arbor.lbs.uqp.graph.util.Graph;
import arbor.lbs.uqp.graph.util.ShortCutSet;

public class Grid {
	int gID;
    int xID;
    int yID;
    int rightGID;
    int downGID;
    int cornerGID;
    HashSet<Integer> vertexIDSet = null;
    int clusterID;

    public Grid(int gID, int xID, int yID, int rGID, int dGID, int cGID, HashSet<Integer> vertexSet) {
    	this.gID = gID;
    	this.xID = xID;
    	this.yID = yID;
    	this.rightGID = rGID;
    	this.downGID = dGID;
    	this.cornerGID = cGID;
    	this.vertexIDSet = vertexSet;
    }
    
    public int getGID() {
    	return this.gID;
    }
    public int getxID() {
    	return this.xID;
    }
    public int getyID() {
    	return this.yID;
    }
    public int getRightGID() {
    	return this.rightGID;
    }
    public int getDownGID() {
    	return this.downGID;
    }
    public int getCornetGID() {
    	return this.cornerGID;
    }
    public HashSet<Integer> getAllVertex() {
    	return this.vertexIDSet;
    }
    public void setClusterID(int ID) {
    	this.clusterID = ID;
    }
}