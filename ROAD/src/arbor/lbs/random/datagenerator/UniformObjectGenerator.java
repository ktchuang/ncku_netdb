package arbor.lbs.random.datagenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

import arbor.foundation.time.ExecTimer;
import arbor.foundation.time.MemUsage;
import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.rtree.spatialindex.Region;
import arbor.lbs.uqp.graph.util.PointRTree;

public class UniformObjectGenerator {
	PointRTree tree;
    public UniformObjectGenerator(String vertexfn) {
    	tree = new PointRTree();
		try {
			BufferedReader reader1 = new BufferedReader(new FileReader(vertexfn));
			String line;
			while ((line=reader1.readLine())!=null) {
				StringTokenizer token = new StringTokenizer(line, " ");
				int vID = Integer.parseInt(token.nextToken());
				double x = Double.parseDouble(token.nextToken());
				double y = Double.parseDouble(token.nextToken());
				
				tree.insertData(vID, x, y);
			}
			reader1.close();
		}catch (IOException e) {
			System.out.println(e);
		}
    }
    public void generateRandomObject(String fn, int numP) {
    	int count = 0;
    	Random randx = new Random();
    	Random randy = new Random();
    	Region bbox = tree.getTreeBBOX();
    	double xsize = bbox.m_pHigh[0]-bbox.m_pLow[0];
    	double ysize = bbox.m_pHigh[1]-bbox.m_pLow[1];
    	double minx = bbox.m_pLow[0];
    	double miny = bbox.m_pLow[1];
    	try {
    		BufferedWriter writer = new BufferedWriter(new FileWriter(fn));
    		while (count < numP) {
    			double px = minx+(randx.nextDouble()*xsize);
    			double py = miny+(randy.nextDouble()*ysize);
    			writer.write(count+","+px+","+py+"\n");
    			count++;
    		}
    		writer.close();
    	} catch (IOException e) {
    		System.out.println(e);
    	}
    	
    }
    public static void main(String args[]) {
    	ExecTimer timer = new ExecTimer();
    	MemUsage mu = new MemUsage();
    	timer.setStartTime("uniform Object Generate");
    	UniformObjectGenerator generator = new UniformObjectGenerator(args[0]);    	
    	generator.generateRandomObject(args[1], Integer.parseInt(args[2]));
    	long mem = mu.getCurrentMemorySize();
    	System.out.println("Memory Size (bytes) = "+mem);
    	System.out.println(timer.setEndTime());
    	System.out.println("Generated!");
    }
}
