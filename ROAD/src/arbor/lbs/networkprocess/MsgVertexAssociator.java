package arbor.lbs.networkprocess;
import java.io.*;
import java.util.*;

import arbor.lbs.uqp.graph.util.PointRTree;
import arbor.lbs.uqp.graph.util.Vertex;
public class MsgVertexAssociator {
	String msgfn;
	String newmsgfn;
	String vertexfn;
	PointRTree tree;
	
	public MsgVertexAssociator(String vertexfn, String msgfn, String newmsgfn) {
		this.vertexfn = vertexfn;
		this.newmsgfn = newmsgfn;
		this.msgfn = msgfn;
		tree = new PointRTree();
		try {
			BufferedReader reader1 = new BufferedReader(new FileReader(vertexfn));
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
			
			BufferedReader reader2 = new BufferedReader(new FileReader(msgfn));
			BufferedWriter writer1 = new BufferedWriter(new FileWriter(newmsgfn));
			while ((line=reader2.readLine())!=null) {
				StringTokenizer token = new StringTokenizer(line, ",");
				int msgID = Integer.parseInt(token.nextToken());
				
				double x = Double.parseDouble(token.nextToken());
				double y = Double.parseDouble(token.nextToken());	
				
				int vID = tree.getNearestVID(x,y);
				writer1.write(msgID+","+x+","+y+","+vID+"\n");
			}
			reader2.close();
			writer1.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static void main(String[] args) {
    	MsgVertexAssociator associator = new MsgVertexAssociator(args[0],args[1],args[2]);
    	System.out.println("Transform successfully");
    }
}
