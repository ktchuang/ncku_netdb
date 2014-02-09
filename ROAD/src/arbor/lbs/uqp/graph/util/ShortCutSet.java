package arbor.lbs.uqp.graph.util;

import java.io.Serializable;
import java.util.HashMap;

/**
 * SP between borders
 * @author ktchuang
 *
 */
public class ShortCutSet implements Serializable {  
  int srcVID;
  HashMap<Integer, Path> scMap;
    
  public ShortCutSet(int srcVertexID) {
	  this.srcVID = srcVertexID;
	  scMap = new HashMap<Integer, Path>();
  }
  public void insertShortCut(Path path, int destV) {
	  scMap.put(destV, path);
  }
  public Path getShortCut(int destV) {
	  if (scMap.containsKey(destV)) {
		  return scMap.get(destV);  
	  }
	  else {
		  return null;
	  }	  
  }
  public HashMap<Integer, Path> getSCMap() {
	  return this.scMap;
  }
  
}
