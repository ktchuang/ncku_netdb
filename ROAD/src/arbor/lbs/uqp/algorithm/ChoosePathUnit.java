package arbor.lbs.uqp.algorithm;

import arbor.lbs.uqp.graph.util.MsgObjPath;
import arbor.lbs.uqp.graph.util.Path;

public class ChoosePathUnit {
  public int rnet;
  public MsgObjPath path;
  
  public int compareTo(Object o) {
	  ChoosePathUnit ano = (ChoosePathUnit)o;
  	if (this.path.cost < ano.path.cost) {
  		return -1;
  	}
  	if (this.path.cost > ano.path.cost) {
  		return 1;
  	}
  	else {
  		return 0;
  	}
  }
}
