package arbor.lbs.upq.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TurnAroundWithNum {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputName = args[0];
		String outputName = args[1];
		int timeUnit = Integer.valueOf(args[2]);
		
		BufferedReader in = new BufferedReader(new FileReader(inputName));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputName));
		
		String str;
		int threshold = timeUnit;
		double taTime=0;
		int num=0;
		while ((str = in.readLine()) != null) {
			String[] tmp = str.split(" ");
			if (Double.valueOf(tmp[0])>threshold) {
				writer.write(threshold+","+taTime+","+num+"\n");
				threshold += timeUnit;
			}
			taTime = Double.valueOf(tmp[1]);
			num = Integer.valueOf(tmp[2]);
		}
		in.close();
		writer.flush();
		writer.close();

	}

}
