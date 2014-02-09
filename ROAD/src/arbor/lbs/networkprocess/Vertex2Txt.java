package arbor.lbs.networkprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import arbor.lbs.rtree.spatialindex.Point;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.Vertex;

public class Vertex2Txt {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Vertex2Txt me = new Vertex2Txt();
		String nodeFile = null, nodeMapping = null;
		nodeFile = args[0];
		nodeMapping = args[1];
		me.transform(nodeFile, nodeMapping);
	}

	private void transform(String nodeFile, String nodeMapping) throws IOException {
		// TODO Auto-generated method stub
		
		BufferedReader in = new BufferedReader(new FileReader(nodeFile));
        String str;
        HashMap<Integer, Point> node = new HashMap<Integer,Point>();
        
        while ((str = in.readLine()) != null) { 
            String[] tmp=str.split(" ");
            int nodeID = Integer.valueOf(tmp[0]).intValue()+1;
            double locX = Double.valueOf(tmp[1]).doubleValue();
            double locY = Double.valueOf(tmp[2]).doubleValue();
            double[] xy = new double[2];
    		xy[0] = locX;
    		xy[1] = locY;
    		Point loc = new Point(xy);
            node.put(nodeID, loc);
        }
        in.close();
        
		in = new BufferedReader(new FileReader(nodeMapping));
		String fileName = nodeFile+".1";
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(fileName)));
		out.write("x,y"+"\n");
		int count = 1;
		String separate = new String("===");
        
        while ((str = in.readLine()) != null) {
        	String[] tmp=str.split(" ");
        	if (tmp[0].equals(separate)){
        		out.flush();
        		out.close();
        		count++;
        		fileName = nodeFile+"."+String.valueOf(count);
        		out = new BufferedWriter(new FileWriter(new File(fileName)));
        		out.write("x,y"+"\n");
        	}
        	else {
        		double[] loc = node.get(Integer.valueOf(tmp[0])).m_pCoords;
        		out.write(String.valueOf(loc[0]));
        		out.write(",");
        		out.write(String.valueOf(loc[1]));
        		out.write("\n");
        	}
        }
        out.flush();
		out.close();
            
		
	}

}
