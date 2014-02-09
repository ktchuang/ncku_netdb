package arbor.lbs.uqp.graph.util;

import java.util.LinkedList;

import arbor.lbs.rtree.RTree;
import arbor.lbs.rtree.spatialindex.IData;
import arbor.lbs.rtree.spatialindex.IEntry;
import arbor.lbs.rtree.spatialindex.INode;
import arbor.lbs.rtree.spatialindex.IQueryStrategy;
import arbor.lbs.rtree.spatialindex.IShape;
import arbor.lbs.rtree.spatialindex.IVisitor;
import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.rtree.spatialindex.Region;
import arbor.lbs.rtree.spatialindex.SpatialIndex;
import arbor.lbs.rtree.storagemanager.IBuffer;
import arbor.lbs.rtree.storagemanager.IStorageManager;
import arbor.lbs.rtree.storagemanager.PropertySet;
import arbor.lbs.rtree.storagemanager.RandomEvictionsBuffer;

public class PointRTree {
	RTree tree;
	public PointRTree() {
		tree = createRTree();
	}
	public int getNearestVID(double x, double y) {
		double[] xy = new double[2];
		MyKNNQueryVisitor vis = new MyKNNQueryVisitor();

		xy[0] = x;
		xy[1] = y;
		Point p = new Point(xy);
		tree.nearestNeighborQuery(1, p, vis);
		return vis.vid;
	}
	public LinkedList<Integer> getDataInsideBBox(double x, double y, double dx, double dy) {		
		double[] pLow = new double[2];
		double[] pHigh = new double[2];
		
		MyRangeQueryVisitor vis = new MyRangeQueryVisitor();

		pLow[0] = x - dx;
		pLow[1] = y - dy;
		
		pHigh[0] = x + dx;
		pHigh[1] = y + dy;
				
		Region r = new Region(pLow, pHigh);
		tree.intersectionQuery(r, vis);
		
		return vis.vlist;		
	}
	public void insertData(int id, double x, double y) {
		double[] xy = new double[2];
		xy[0] = x;
		xy[1] = y;	
		Point p = new Point(xy);
		tree.insertData(null, p, id);
	}
	public void deleteData(int id, double x, double y) {
		double[] xy = new double[2];
		xy[0] = x;
		xy[1] = y;	
		Point p = new Point(xy);
		tree.deleteData(p, id);
	}
	public Region getTreeBBOX() {
		MyQueryStrategy2 qs = new MyQueryStrategy2();
		tree.queryStrategy(qs);
		return qs.m_indexedSpace;		
	}
	public RTree createRTree() {
		// Create a disk based storage manager.
		PropertySet ps = new PropertySet();

		Boolean b = new Boolean(true);
		ps.setProperty("Overwrite", b);
			//overwrite the file if it exists.

		//ps.setProperty("FileName", args[1]);
			// .idx and .dat extensions will be added.

		Integer i = new Integer(4096);
		ps.setProperty("PageSize", i);
			// specify the page size. Since the index may also contain user defined data
			// there is no way to know how big a single node may become. The storage manager
			// will use multiple pages per node if needed. Off course this will slow down performance.

		IStorageManager diskfile = SpatialIndex.createMemoryStorageManager(ps);

		IBuffer file = new RandomEvictionsBuffer(diskfile, 10, false);
			// applies a main memory random buffer on top of the persistent storage manager
			// (LRU buffer, etc can be created the same way).

		// Create a new, empty, RTree with dimensionality 2, minimum load 70%, using "file" as
		// the StorageManager and the RSTAR splitting policy.
		PropertySet ps2 = new PropertySet();

		Double f = new Double(0.7);
		ps2.setProperty("FillFactor", f);

		i = new Integer(20);
		ps2.setProperty("IndexCapacity", i);
		ps2.setProperty("LeafCapacity", i);
			// Index capacity and leaf capacity may be different.

		i = new Integer(2);
		ps2.setProperty("Dimension", i);

		RTree rtree = new RTree(ps2, file);
        return rtree;
	}
	// example of a Visitor pattern.
	// see RTreeQuery for a more elaborate example.
	private class MyKNNQueryVisitor implements IVisitor
	{
		int vid;
		public void visitNode(final INode n) {}

		public void visitData(final IData d)
		{
			this.vid = d.getIdentifier();
			//System.out.println(d.getIdentifier());
				// the ID of this data entry is an answer to the query. I will just print it to stdout.
		}
	}
	private class MyRangeQueryVisitor implements IVisitor 
	{
		
		LinkedList<Integer> vlist = new LinkedList<Integer>();
		public void visitNode(final INode n) {}

		public void visitData(final IData d)
		{
			this.vlist.add(d.getIdentifier());
		}	
	}
	// example of a Strategy pattern.
	// find the total indexed space managed by the index (the MBR of the root).
	private class MyQueryStrategy2 implements IQueryStrategy
	{
		public Region m_indexedSpace;

		public void getNextEntry(IEntry entry, int[] nextEntry, boolean[] hasNext)
		{
			// the first time we are called, entry points to the root.
			IShape s = entry.getShape();
			m_indexedSpace = s.getMBR();

			// stop after the root.
			hasNext[0] = false;
		}
	}
}
