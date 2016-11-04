package project.MiddlewareEnvironment.CacheMaintenance.EA;

/**
 * Created by santhilata on 07/06/16.
 */
public interface PerformanceParameters {
    public int numCacheLocations = 1000;

    //Location
    public String UserLocation = "ULOC_1";
    public String CacheLocation = "CLOC_1";

    /**
     * Assumptions about cache locations: all cache units at unit distance from each other.
     */

    //Frequency
    public int[] frequency_curWindow_Cloc = new int[3]; // for the last three windows
    // we store the data only for last three windows due to the dynamic change of query requirement
    public int[] frequency_curWindow_Uloc = new int[3];


    //data size
    public long[]  datasize = new long[numCacheLocations];


    //Time
    public int[] time_windowLastused = new int[numCacheLocations];


    //QueryData characteristics
    public boolean queryDataRefresh = false;


    //query repeatability at each user location
    public long[] userRepeatability = new long[numCacheLocations];


    // query complexity
    public int queryComplexity = 1;


    //MeanTime Cache down time
    public int[] MTCD = {2500, 5000, 7500, 10000}; //


    //Network resource utilization
    public double networkResourcesFactor = 0.1; // between 0 to 1


}
