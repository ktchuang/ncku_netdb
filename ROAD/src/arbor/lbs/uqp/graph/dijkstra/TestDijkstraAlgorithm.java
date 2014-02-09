package arbor.lbs.uqp.graph.dijkstra;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import arbor.lbs.uqp.graph.util.Edge;
import arbor.lbs.uqp.graph.util.Graph;
import arbor.lbs.uqp.graph.util.GraphPool;
import arbor.lbs.uqp.graph.util.Path;
import arbor.lbs.uqp.graph.util.Vertex;




public class TestDijkstraAlgorithm {
	TestDijkstraAlgorithm() {
		test1();
		test2();
	}
	void test1() {
		
		for (int i = 0; i < 11; i++) {
			GraphPool.getSignleton().insertVertex(new Vertex(i));
		}

		addLane(0, 1, 85);
		addLane(0, 2, 217);
		addLane(0, 4, 173);
		addLane(2, 6, 186);
		addLane(2, 7, 103);
		addLane(3, 7, 183);
		addLane(5, 8, 250);
		addLane(8, 9, 84);
		addLane(7, 9, 167);
		addLane(4, 9, 502);
		addLane(9, 10, 40);
		addLane(1, 10, 600);

		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(GraphPool.getSignleton().getAllVertexes(), GraphPool.getSignleton().getAllEdges());
		Dijkstra dijkstra = new Dijkstra(graph);
		dijkstra.execute(0);
		Path path = dijkstra.getPath(10);
		System.out.println(path.toString());
		
		Dijkstra dijkstra1 = new Dijkstra(graph);
		path = dijkstra1.execute(0,10);
		System.out.println(path.toString());
		
	}

	
	void test2() {
		int num_vertexes = 10000;
		int num_edges = 20000;
		Random r = new Random();
		for (int i = 0; i < num_vertexes; i++) {
			GraphPool.getSignleton().insertVertex(new Vertex(i));
		}

		for (int i = 0; i < num_vertexes; i++) {
			addLane((int)(r.nextDouble() * num_vertexes), (int)(r.nextDouble() * num_vertexes), (int)(r.nextDouble() * 50));			
		}
		addLane(0, 50, 200000);

		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(GraphPool.getSignleton().getAllVertexes(), GraphPool.getSignleton().getAllEdges());
		Dijkstra dijkstra = new Dijkstra(graph);
		dijkstra.execute(0);
		Path path = dijkstra.getPath(50);
		System.out.println(path.toString());
		
		Dijkstra dijkstra1 = new Dijkstra(graph);
		path = dijkstra1.execute(0, 50);		
		System.out.println(path.toString());
	}	
	private void addLane(int sourceLocNo, int destLocNo,
			double duration) {
		Edge lane = new Edge(sourceLocNo, destLocNo, duration);
		GraphPool.getSignleton().insertEdge(lane);
	}
	public static void main(String[] args) {
		new TestDijkstraAlgorithm();
	}

}