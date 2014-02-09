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

public class QuerySequence {

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

		    
		    public BinCnt(int rID, int count) {
		    	this.rID = rID;
		    	this.count = count;
		    }
		    public int getRID() {
		    	return this.rID;
		    }
		    public int getCount() {
		    	return this.count;
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
		
		Random r = new Random();
		TreeSet<RandomValue> queueSet = new TreeSet<RandomValue>();
		double mean = 0, variance = 0.2; 
		
		for (int i=0; i<number; i++) {
			double ranDouble = mean + r.nextGaussian()*variance;
			//double ranDouble = r.nextDouble();
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
			BinCnt ran = new BinCnt(binID,count); 
			binSet.add(ran);
		}
		
		//int binSize = binSet.size();
		//double interval = (double)86400/binSize;
		int high = binSet.last().getRID();
		int low = binSet.first().getRID();
		double interval = (double)86400/(high-low);
		
		//System.out.println(interval);
		double timeStamp = 0;
		while (binSet.size()>0) {
			
			BinCnt popObj = binSet.pollFirst();
			int rID = popObj.getRID();
			int tmp = popObj.getCount();
			timeStamp = (double)(rID-low)*interval;
			
			for (int i=0; i<tmp; i++){
				writer.write(timeStamp+"\n");
			}
			/*
			if ((binSet.size()<90)&&(binSet.size()>80)) {
				for (int i=0; i<tmp*5; i++){
					writer.write(timeStamp+"\n");
				}
			}
			else if ((binSet.size()<=80)&&(binSet.size()>70)) {
				for (int i=0; i<tmp*3; i++){
					writer.write(timeStamp+"\n");
				}
			}
			else if ((binSet.size()<=70)&&(binSet.size()>60)) {
				for (int i=0; i<tmp; i++){
					writer.write(timeStamp+"\n");
				}
			}
			else if ((binSet.size()>=90)||((binSet.size()<=60)&&(binSet.size()>=0))) {
				for (int i=0; i<(tmp/10); i++){
					writer.write(timeStamp+"\n");
				}
			}*/
			/*
			if ((binSet.size()<90)&&(binSet.size()>70)) {
				for (int i=0; i<5000; i++){
					writer.write(timeStamp+"\n");
				}
			}
			else if ((binSet.size()>=90)||((binSet.size()<70)&&(binSet.size()>40))){
				for (int i=0; i<(tmp/10); i++){
					writer.write(timeStamp+"\n");
				}
			}
			*/
			System.out.println(timeStamp+" "+tmp);
			//timeStamp += interval;
		}
		writer.flush();
		writer.close();
	}
	
}
