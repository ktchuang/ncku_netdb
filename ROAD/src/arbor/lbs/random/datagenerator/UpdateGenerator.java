package arbor.lbs.random.datagenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import arbor.lbs.uqp.graph.util.GraphPool;

public class UpdateGenerator {

	public UpdateGenerator(String graphFn, String updateFn, String distri, String num) throws IOException {
		// TODO Auto-generated constructor stub
		GraphPool.loadGraph(graphFn);
		
		int count = Integer.valueOf(num);
		
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(updateFn)));
		if (updateFn.contains("uniform")) {
			System.out.println("uniform");
			int size = GraphPool.getSignleton().getAllVertexes().size();
			Random r = new Random();
			for (int i=0; i<count; i++) {
				int vID = r.nextInt(size)+1;
				out.write(String.valueOf(vID)+"\n");
			}
			out.flush();
			out.close();
		}
		else {
			System.out.println("POI distribution");
			BufferedReader in = new BufferedReader(new FileReader(distri));
			String str;
			ArrayList<Integer> pointSet = new ArrayList<Integer>();
			while ((str = in.readLine()) != null) {
				String[] tmp = str.split(",");
				pointSet.add(Integer.valueOf(tmp[3]));
			}
			in.close();
			
			int size = pointSet.size();
			Random r = new Random();
			for (int i=0; i<count; i++) {
				int idx = r.nextInt(size);
				int id = pointSet.get(idx);
				out.write(String.valueOf(id)+"\n");
			}
			out.flush();
			out.close();
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		UpdateGenerator generator = new UpdateGenerator(args[0],args[1],args[2],args[3]);
		System.out.println("Create Update Sequence successfully");

	}

}
