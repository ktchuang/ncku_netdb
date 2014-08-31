package netdb.spatial.trajdb.index;

import java.util.Arrays;

import netdb.spatial.rtree.spatialindex.Region;

public class Trajectory extends LineString {
  private Region bbox;
  public Trajectory() {
	  super();
	  bbox = new Region();
  }
  void insertPnt(double[] pnt) {
	 double[] newP =  Arrays.copyOf(pnt, 2);
	 pntList.add(newP);
	 updateBBOX(newP);
  }
  private void updateBBOX(double[] pnt) {
	  if (pnt[0] < bbox.m_pLow[0])
		  bbox.m_pLow[0] = pnt[0];
	  if (pnt[1] < bbox.m_pLow[1])
		  bbox.m_pLow[0] = pnt[1];
	  
	  if (pnt[0] > bbox.m_pHigh[0])
		  bbox.m_pHigh[0] = pnt[0];
	  if (pnt[1] > bbox.m_pHigh[1])
		  bbox.m_pHigh[1] = pnt[1];	  
	  
  }
  /**
   * Convert a LineString to Trajectory object
   * @param line
   * @return
   */
  public static Trajectory lineString2Trajectory(LineString line) {
	  Trajectory traj = new Trajectory();
	  for (int i=0;i<line.pntList.size();i++) {
		  double[] pnt = line.pntList.get(i);
		  traj.insertPnt(pnt);
	  }
	  return traj;
  }
}
