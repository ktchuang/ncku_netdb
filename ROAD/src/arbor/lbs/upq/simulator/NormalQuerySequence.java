package arbor.lbs.upq.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import arbor.lbs.uqp.algorithm.VisitedObject;

public class NormalQuerySequence {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String input = args[0];
		String output = args[1];
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		BufferedReader in = new BufferedReader(new FileReader(input));
		
		String str;
		String[] tmp = null;
		while ((str = in.readLine()) != null) {
			tmp = str.split(",");
		}
		in.close();
		
		int binSize = tmp.length-1;
		double interval = (double)259200/binSize;
		System.out.println(binSize+" "+interval);
		
		//System.out.println(interval);
		int sum = 0;
		double timeStamp = 0;
		for (int i=0; i<binSize; i++) {
			int count = Integer.valueOf(tmp[i]);
			timeStamp = interval*i;
			for (int j=0; j<count; j++){
				writer.write(timeStamp+"\n");
			}
			System.out.println(timeStamp+" "+count);
			sum += count;
		}
		writer.flush();
		writer.close();
		System.out.println(sum);
	}
	
}
