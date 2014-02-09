package arbor.foundation.time;

public class MemUsage {
	long startMemoryUse;
	long endMemoryUse;
    public MemUsage() {
    	this.startMemoryUse = getMemoryUse();    	
    }
    public long getCurrentMemorySize() {
    	this.endMemoryUse = getMemoryUse();
    	/*
        float approximateSize = ( endMemoryUse - startMemoryUse ) /100f;
        long result = Math.round( approximateSize );
        */
    	long result = endMemoryUse - startMemoryUse;
        return result;
    }
    
    private static long getMemoryUse(){
        putOutTheGarbage();
        long totalMemory = Runtime.getRuntime().totalMemory();

        putOutTheGarbage();
        long freeMemory = Runtime.getRuntime().freeMemory();

        return (totalMemory - freeMemory);
      }

      private static void putOutTheGarbage() {
        collectGarbage();
        collectGarbage();
      }
      // PRIVATE //
      private static int fSAMPLE_SIZE = 100;
      private static long fSLEEP_INTERVAL = 100;

      private static void collectGarbage() {
        try {
          System.gc();
          Thread.currentThread().sleep(fSLEEP_INTERVAL);
          System.runFinalization();
          Thread.currentThread().sleep(fSLEEP_INTERVAL);
        }
        catch (InterruptedException ex){
          ex.printStackTrace();
        }
      }
}
