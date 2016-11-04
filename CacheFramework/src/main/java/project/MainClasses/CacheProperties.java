package project.MainClasses;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by santhilata on 9/4/16.
 */
public interface CacheProperties {
    public static final File configFile =  new File("//home//santhilata//Dropbox//CL1//Coordination//QGen//src//main//java//SimpleGui//ConfigProperties.xml");
    public static final String INPUT_FILE_Part1 ="~/Dropbox//CL1//Coordination//QGen//src//main//java//QueryInput//";
    public static final String INPUT_FILE_Part2 ="_queryRepeat_10.csv";

    public static final String METADATA_FILE_Part1 = "~/Dropbox//CL1//Coordination//QGen//src//main//java//MetaDataFiles//Epoch_";
    public static final String METADATA_FILE_Part2 = ".csv";


    public static final int numContainers = 1;

    public static final String FULLY_FOUND = "FULLY FOUND";
    public static final String PARTIALLY_FOUND = "PARTIALLY FOUND";
    public static final String NOT_FOUND = "NOT FOUND";
    public static final String FULL_QUERY_SEARCH_ONLY= "FullQuerySearch";
    public static final String PARTIAL_QUERY_FRAGMENTS = "PartialQuerySearch";
    public static final String TIME_THRESHOLD_ONLY = "TimeThreshold";
    public static final String FREQUENCY_THRESHOLD_ONLY = "FrequencyThreshold";
    public static final String COMBINED_ALGO_MAINTENANCE ="CombinedAlgoMaintenance";
    public static final String NONE = "None";

    public static final String searchCriteria = FULL_QUERY_SEARCH_ONLY;

    public static final int cacheMaintenanceStart =10;
    public static   int standard_Data_Unit_Size = 10;

    int CACHE_MAINTENANCE_PERIOD = 200;
    int FREQUENCY_THRESHOLD=20;
    int TIME_THRESHOLD = 2800;
    int CACHE_MAINTENANCE_DURATION = 15;
    int AVERAGE_CACHE_LATENCY = 200;

    // criteria for placing data on cache as it arrives
    public static final String NEARBY_CRITERIA = "NearBy"; // greedy method  search and insert locally
    public static final String DATASIZE_CRITERIA = "DataSize"; // search and insert locally
    public static final String GLOBAL_NEARBY_CRITERIA = "Global_greedy"; // search and insert globally greedy manner
    public static final String GLOBAL_DATA_CRITERIA = "Global_data";  // search and insert globally
    public static final String GLOBAL_RANDOMIZED = "Random";  // search and insert globally
    public static final String GLOBAL_DYNAMIC_CRITERIA = "Dynamic";

    public static final String selectedCacheStoreCriteria = DATASIZE_CRITERIA;

    static int num_Queries = 20;
    static int upperLimit_Freq = 5;
    static int upperTime_WindowLimit = 5;// number time windows of cache location info
    static int trainLength = 1; // train length is used to find how many windows of data will be considered
    static int testLength = 1; // current window

    static int upperQueryComplexity = 5;
    static double upperDataSize = 300.0; // GB
    static double maxCache_size = 10000.0; //GB
    static double networkCostPerGB = 20.0; // network cost per GB per unit distance per sec
    static double average_DataServer_ProcTime = 5; // 5 milli sec
    static double avTime_QIModify = 5; //millisec
    static String[] Refresh = {"often","rare"};

    static double dataStoreDistance = 1000; // some distance units
    static double cache_To_cacheDistance = 100.0;// distance from cache to cache
    static double cost_per_query = 1000.0;// first time cost to add a query to cache units

    static int freq_threshold = 8; // frequency threshold
    static int time_threshold = 2; // # of time windows recently the query was used
    static double avResponseTime_ThresholdFitness = 10.0;
    static double dataSize_threshold = 300.0 ;//GB not to go above

    static int numUserContainers = 6;
    static int cachePerUserContainer = 1;// numUserContainers contain one cache unit each
    static int cacheUnits = numUserContainers * cachePerUserContainer;
    static String featureStr = "ftcsqm"; // 1
    // String featureStr = "cfst"; // 2
    //String featureStr = "stcf"; // 3
    //String featureStr = "tcsf"; // 4


}
