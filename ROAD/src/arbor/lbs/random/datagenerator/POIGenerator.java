package arbor.lbs.random.datagenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import arbor.lbs.rtree.spatialindex.Point;

public class POIGenerator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		String str;
		int idx=0;
		HashMap<Integer,HashMap<Integer,Point>> poiMap = new HashMap<Integer,HashMap<Integer,Point>>();
		while ((str = in.readLine()) != null) {
			String[] tmp=str.split(" ");
            double locX = Double.valueOf(tmp[0]);
            double locY = Double.valueOf(tmp[1]);
			int catID = Integer.valueOf(tmp[2]);
			double[] loc = new double[2];
			loc[0]= locX; loc[1]= locY;
			Point pLoc = new Point(loc);
			if (poiMap.containsKey(catID)) {
				HashMap<Integer,Point> poiData = poiMap.get(catID);
				poiData.put(idx, pLoc);
			}
			else {
				HashMap<Integer,Point> poiData = new HashMap<Integer,Point>();
				poiData.put(idx, pLoc);
				poiMap.put(catID, poiData);
			}
			idx++;
		}
		in.close();

		Iterator<Integer> poiMapIter = poiMap.keySet().iterator();
		while (poiMapIter.hasNext()) {
			Integer cID = (Integer)poiMapIter.next();
			HashMap<Integer,Point> poiSet = poiMap.get(cID);
			System.out.println(cID+" "+poiSet.size());
		}
		
		BufferedWriter writer1 = new BufferedWriter(new FileWriter(args[1]));
		BufferedWriter writer2 = new BufferedWriter(new FileWriter(args[2]));
		HashMap<Integer,Point> poiSet  = poiMap.get(16);
		Iterator<Integer> poiSetIter = poiSet.keySet().iterator();
		writer1.write("x,y\n");
		int d=0;
		while (poiSetIter.hasNext()) {
			Integer pID = (Integer)poiSetIter.next();
			Point pit = poiSet.get(pID);
			writer1.write(pit.m_pCoords[0]+","+pit.m_pCoords[1]+"\n");
			writer2.write(d+","+pit.m_pCoords[0]+","+pit.m_pCoords[1]+"\n");
			d++;
		}
		writer1.close();
		writer2.close();
		
	}

}
