package arbor.lbs.upq.simulator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import arbor.lbs.uqp.algorithm.VisitedObject;

public class QueryUpdateSequence {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String output = args[0];
		int number = Integer.valueOf(args[1]);
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		
		class RandomValue implements Comparable {
		    double value;
		    int rID;

		    
		    public RandomValue(int rID, double value) {
		    	this.rID = rID;
		    	this.value = value;
		    }
		    public int getRID() {
		    	return this.rID;
		    }
		    public double getValue() {
		    	return this.value;
		    }
			@Override
			public int compareTo(Object o) {
				// TODO Auto-generated method stub
				RandomValue ano = (RandomValue)o;
			   	if (this.value > ano.value) {
			   		return 1;
			   	}
			   	if (this.value < ano.value) {
			   		return -1;
			   	}
			   	else {
			   		return 0;
			   	}
			}
		}
		
		class BinCnt implements Comparable {
		    int count;
		    int rID;
		    int updateFlag;

		    
		    public BinCnt(int rID, int count, int updateFlag) {
		    	this.rID = rID;
		    	this.count = count;
		    	this.updateFlag = updateFlag;
		    }
		    public int getRID() {
		    	return this.rID;
		    }
		    public int getCount() {
		    	return this.count;
		    }
		    public int getUpdateFlag() {
		    	return this.updateFlag;
		    }
			@Override
			public int compareTo(Object o) {
				// TODO Auto-generated method stub
				BinCnt ano = (BinCnt)o;
			   	if (this.rID > ano.rID) {
			   		return 1;
			   	}
			   	if (this.rID < ano.rID) {
			   		return -1;
			   	}
			   	else {
			   		return 0;
			   	}
			}
		}
		
		//query sequence
		Random r = new Random();
		TreeSet<RandomValue> queueSet = new TreeSet<RandomValue>();
		
		for (int i=0; i<number; i++) {
			double ranDouble = r.nextGaussian();
			//System.out.println(ranDouble);
			RandomValue ran = new RandomValue(i,ranDouble); 
			queueSet.add(ran);
		}
		
		HashMap<Integer,Integer> binCnt = new HashMap<Integer,Integer>();
		while (queueSet.size()>0) {
			RandomValue popObj = queueSet.pollFirst();
			double tmp = popObj.getValue();
			tmp += 0.05;
			int binIdx = (int) (tmp*100);
			if (!binCnt.containsKey(binIdx)) {
				binCnt.put(binIdx, 1);
			}
			else {
				int cCount = binCnt.get(binIdx);
				cCount++;
				binCnt.put(binIdx, cCount);
			}
			
		}
		
		TreeSet<BinCnt> binSet = new TreeSet<BinCnt>();
		Iterator<Integer> iter = binCnt.keySet().iterator();
		while (iter.hasNext()) {
			int binID = (Integer)iter.next();
			int count = binCnt.get(binID);
			BinCnt ran = new BinCnt(binID,count,0); 
			binSet.add(ran);
		}
		
		//update sequence
		Random rUpdate = new Random();
		TreeSet<RandomValue> updateSet = new TreeSet<RandomValue>();
		
		for (int i=0; i<50000; i++) {
			double ranDouble = rUpdate.nextGaussian();
			//System.out.println(ranDouble);
			RandomValue ran = new RandomValue(i,ranDouble); 
			updateSet.add(ran);
		}
		
		HashMap<Integer,Integer> updateBinCnt = new HashMap<Integer,Integer>();
		while (updateSet.size()>0) {
			RandomValue popObj = updateSet.pollFirst();
			double tmp = popObj.getValue();
			tmp += 0.05;
			int binIdx = (int) (tmp*100);
			if (!updateBinCnt.containsKey(binIdx)) {
				updateBinCnt.put(binIdx, 1);
			}
			else {
				int cCount = updateBinCnt.get(binIdx);
				cCount++;
				updateBinCnt.put(binIdx, cCount);
			}
			
		}
		
		TreeSet<BinCnt> updateBinSet = new TreeSet<BinCnt>();
		iter = updateBinCnt.keySet().iterator();
		while (iter.hasNext()) {
			int binID = (Integer)iter.next();
			int count = updateBinCnt.get(binID);
			BinCnt ran = new BinCnt(binID,count,1); 
			updateBinSet.add(ran);
		}
		
		
		int high = binSet.last().getRID();
		int highU = updateBinSet.last().getRID();
		if (highU>high)
			high = highU;
		int low = binSet.first().getRID();
		int lowU = updateBinSet.first().getRID();
		if (lowU<low)
			low = lowU;
		double interval = (double)86400/(high-low);
		//System.out.println(interval);
		double timeStamp = 0;
		while ((binSet.size()>0)&&(updateBinSet.size()>0)) {
			BinCnt Obj1 = binSet.first();
			BinCnt Obj2 = updateBinSet.first();
			BinCnt  popObj;
			if  (Obj1.getRID()<Obj2.getRID()) {
				popObj = binSet.pollFirst();
			}
			else {
				popObj = updateBinSet.pollFirst();
			}
			int rID = popObj.getRID();
			int tmp = popObj.getCount();
			int updateFlag = popObj.getUpdateFlag();
			timeStamp = (double)(rID-low)*interval;
			for (int i=0; i<tmp; i++){
				if (updateFlag == 1) {
					writer.write("U "+timeStamp+"\n");
				}
				else {
					writer.write("Q "+timeStamp+"\n");
				}
			}
			if (updateFlag == 1) {
				System.out.println("U "+timeStamp+" "+tmp);
			}
			else {
				System.out.println("Q "+timeStamp+" "+tmp);
			}
			//timeStamp += interval;
		}
		while (binSet.size()>0) {
			BinCnt popObj = binSet.pollFirst();
			int rID = popObj.getRID();
			int tmp = popObj.getCount();
			int updateFlag = popObj.getUpdateFlag();
			timeStamp = (double)(rID-low)*interval;
			for (int i=0; i<tmp; i++){
				if (updateFlag == 1) {
					writer.write("U "+timeStamp+"\n");
				}
				else {
					writer.write("Q "+timeStamp+"\n");
				}
			}
			if (updateFlag == 1) {
				System.out.println("U "+timeStamp+" "+tmp);
			}
			else {
				System.out.println("Q "+timeStamp+" "+tmp);
			}
			//timeStamp += interval;
		}
		while (updateBinSet.size()>0) {
			BinCnt popObj = updateBinSet.pollFirst();
			int rID = popObj.getRID();
			int tmp = popObj.getCount();
			int updateFlag = popObj.getUpdateFlag();
			timeStamp = (double)(rID-low)*interval;
			for (int i=0; i<tmp; i++){
				if (updateFlag == 1) {
					writer.write("U "+timeStamp+"\n");
				}
				else {
					writer.write("Q "+timeStamp+"\n");
				}
			}
			if (updateFlag == 1) {
				System.out.println("U "+timeStamp+" "+tmp);
			}
			else {
				System.out.println("Q "+timeStamp+" "+tmp);
			}
			//timeStamp += interval;
		}
		
		writer.flush();
		writer.close();
	}
	
}
