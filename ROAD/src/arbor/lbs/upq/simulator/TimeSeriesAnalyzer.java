package arbor.lbs.upq.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TimeSeriesAnalyzer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String inputName = args[0];
		String outputName = args[1];
		
		BufferedReader in = new BufferedReader(new FileReader(inputName));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputName));
		
		String str;
		double curTime=0, prevTime=0, diff=0;
		while ((str = in.readLine()) != null) {
			String[] tmp = str.split(":");
			curTime = Double.valueOf(tmp[1]);
			diff = curTime-prevTime;
			prevTime = curTime;
			writer.write(diff+"\n");
		}
		in.close();
		writer.flush();
		writer.close();

	}

}
