package netdb.spatial.trajdb.index;
import java.util.*;
public class LineString {
  public LinkedList<double[]> pntList;
  public LineString() {
	  pntList=new LinkedList<double[]>();
  }
  void insertPnt(double[] pnt) {
	 double[] newP =  Arrays.copyOf(pnt, 2);
	 pntList.add(newP);
  }

}
