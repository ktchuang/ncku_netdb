package arbor.lbs.upq.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

public class BinningAnalyzer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String input = args[0];
		String output = args[1];
		int binSize = Integer.valueOf(args[2]);
		
		HashMap<Integer,Integer> binCount = new HashMap<Integer,Integer>();
		
		BufferedReader in = new BufferedReader(new FileReader(input));
		String str;
		while ((str = in.readLine()) != null) {
			double value = Double.valueOf(str);
			int keyValue = (int) (value/binSize);
			if (!binCount.containsKey(keyValue)) {
				binCount.put(keyValue, 1);
			}
			else {
				int cCount = binCount.get(keyValue);
				cCount++;
				binCount.put(keyValue, cCount);
			}
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		
		int size = binCount.keySet().size();
		for(int j=0;j<size;j++) {
			if (binCount.containsKey(j)) {
				int count = binCount.get(j);
				writer.write(j+","+count+"\n");
			}
			else {
				writer.write(j+",0\n");
			}
		}
		writer.close();

	}

}
