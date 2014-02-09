package arbor.lbs.upq.simulator.util;

import java.util.HashSet;

public class GridGroup {
	HashSet<Integer> gridSet = null;
	
	public GridGroup(HashSet<Integer> gridSet) {
		this.gridSet = gridSet;
	}
	
	public HashSet<Integer> getGridSet() {
		return this.gridSet;
	}
	public void insertGrid(int gID){
		gridSet.add(gID);
	}

}
