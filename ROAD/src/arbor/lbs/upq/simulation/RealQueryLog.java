package arbor.lbs.upq.simulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import arbor.lbs.networkprocess.MsgVertexAssociator;
import arbor.lbs.uqp.graph.util.PointRTree;

public class RealQueryLog {
	PointRTree tree;

	public RealQueryLog(String vertexFn, String queryFn, String newQueryFn) throws IOException {
		// TODO Auto-generated constructor stub
		tree = new PointRTree();
		
		BufferedReader reader1 = new BufferedReader(new FileReader(vertexFn));
		String line;
		while ((line=reader1.readLine())!=null) {
			StringTokenizer token = new StringTokenizer(line, " ");
			int vID = Integer.parseInt(token.nextToken());
			double x = Double.parseDouble(token.nextToken());
			double y = Double.parseDouble(token.nextToken());
			vID += 1;
			tree.insertData(vID, x, y);
		}
		reader1.close();
			
		BufferedReader reader2 = new BufferedReader(new FileReader(queryFn));
		BufferedWriter writer1 = new BufferedWriter(new FileWriter(newQueryFn));
		while ((line=reader2.readLine())!=null) {
			StringTokenizer token = new StringTokenizer(line, ",");
			System.out.println(line);
			double x = Double.parseDouble(token.nextToken());
			double y = Double.parseDouble(token.nextToken());			
			int vID = tree.getNearestVID(x,y);
			writer1.write(vID+"\n");
		}
		reader2.close();
		writer1.flush();
		writer1.close();	
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		RealQueryLog associator = new RealQueryLog(args[0],args[1],args[2]);
    	System.out.println("Query Log Transform successfully");

	}

}
