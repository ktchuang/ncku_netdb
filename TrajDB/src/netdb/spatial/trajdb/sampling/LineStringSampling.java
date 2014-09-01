package netdb.spatial.trajdb.sampling;

import netdb.spatial.trajdb.unit.LineString;

public interface LineStringSampling {
  public void lineInput(LineString line);
  public void execSampling();
  public LineString lineOutput();
}
