package arbor.lbs.uqp.graph.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

public class HQNetPool  implements Serializable {
	private static HQNetPool me = null;

	HashMap<Integer, HQNet> hMap;
	
	public static HQNetPool getSignleton() {
		if (me == null) {
			me = new HQNetPool();
		}
		return me;
	}
	private HQNetPool() {
		hMap = new HashMap<Integer, HQNet>();
	}
	public void insertHQNet(HQNet hnet) {
		hMap.put(hnet.getHQNetID(), hnet);
	}
	public HashMap<Integer, HQNet> getHQNet() {
		return this.hMap;
	}
	public HQNet getHQNet(int hID) {
		return hMap.get(hID);
	}
	public void saveHQNets(String fn) {
		try {
			FileOutputStream fs = new FileOutputStream(fn);
			ObjectOutputStream os = new ObjectOutputStream(fs);
			os.writeObject(me);
			os.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public static void loadHQNets(String fn) {
		try {
			FileInputStream fs = new FileInputStream(fn);
			ObjectInputStream in = new ObjectInputStream(fs);
			HQNetPool inst = (HQNetPool) in.readObject();
			me = inst;
			in.close();
		} catch (IOException e) {
			System.out.println(e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
