package arbor.lbs.upq.simulation;

import java.util.LinkedList;
import java.util.List;

import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.MsgObjPool;
import arbor.lbs.uqp.graph.util.RNet;
import arbor.lbs.uqp.graph.util.RNetHierarchy;

public class RNetCompact {

	public RNetCompact(String graphFn, String hierFn, String msgFn) {
		// TODO Auto-generated constructor stub
		GraphPool.getSignleton().loadGraph(graphFn);
		RNetHierarchy.loadHierarchy(hierFn);
		MsgObjPool.loadMsgPool(msgFn);
		
		int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
		int count = 0;
		int total = 0;
		System.out.println(maxLevel);
		for (int i=1; i<maxLevel; i++) {
			LinkedList<Integer> rnetLst = RNetHierarchy.getSignleton().getLevelRNet(i);
			for (int j=0; j<rnetLst.size(); j++) {
				total++;
				RNet rnet = RNetHierarchy.getSignleton().getRNet(rnetLst.get(j));
				if (rnet.hasMsgObjInside()) {
					boolean allSubContain = true;
					List<Integer> childLst = rnet.getChildRNetList();
					for (int x=0; x<childLst.size(); x++) {
						RNet subRNet = RNetHierarchy.getSignleton().getRNet(childLst.get(x));
						if (!subRNet.hasMsgObjInside()) {
							allSubContain = false;
							break;
						}
					}
					if (allSubContain==true)
						count++;
				}
			}
		}
		System.out.println(total);
		System.out.println(count);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RNetCompact display = new RNetCompact(args[0],args[1],args[2]);
	}

}
