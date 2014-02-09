package arbor.lbs.uqp.graph.util;

import java.io.*;
import java.util.HashMap;
import java.util.StringTokenizer;


public class MsgObjPool implements Serializable {
	private static MsgObjPool me = null;
	private HashMap<Integer, MsgObj> msgMap = null;
	
    private MsgObjPool() {
    	msgMap = new HashMap<Integer, MsgObj>();
    }
    public static MsgObjPool getSignleton() {
    	if (me == null) {
    		me = new MsgObjPool();
    	}
    	return me;
    }
    public int getMsgSize() {
    	return this.msgMap.size();
    }
    public MsgObj getMsg(int msgID) {
    	if (msgMap.containsKey(msgID)) {
    		return msgMap.get(msgID);
    	}
    	else {
    		return null;
    	}
    }
    public void insertMsg(MsgObj obj) {
    	msgMap.put(obj.msgID, obj);    	
    }
    public boolean removeMsg(int msgID) {
    	if (msgMap.containsKey(msgID)) {
    		msgMap.remove(msgID);
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    /**
     * Please call this function after GraphPool and RNetHierarchy are ready.
     * Msg file format:
     * ID, x, y, assocVertexID
     * @param fn
     */
    public static void loadMsgPool(String fn) {
    	try {
    	  BufferedReader reader = new BufferedReader(new FileReader(fn));
    	  String line;
    	  while ((line=reader.readLine()) != null) {
    		  StringTokenizer token = new StringTokenizer(line,",");
    		  int msgID = Integer.parseInt(token.nextToken());
    		  double x = Double.parseDouble(token.nextToken());
    		  double y = Double.parseDouble(token.nextToken());
    		  int vID = Integer.parseInt(token.nextToken());
    		  MsgObj obj = new MsgObj(msgID, x, y);
    		  obj.setAssocVertex(vID);
    		  MsgObjPool.getSignleton().insertMsg(obj);
    		  Vertex v = GraphPool.getSignleton().getVertex(vID);
    		  v.insertAssocMsgObj(msgID);
    		  
    		  int maxLevel = RNetHierarchy.getSignleton().getMaxLevel();
    		  for (int i=0; i<=maxLevel; i++) {
    			  int rID = v.getInsideRNetID(i);
    			  RNet rnet = RNetHierarchy.getSignleton().getRNet(rID);
    			  //rnet.setContainObj(true);  
    			  rnet.increaseObjCnt(1);
    		  }
    		  
    	  }
    	  reader.close();
    	}catch (IOException e) {
    		System.out.println(e);
    	}
    	
    }
}
