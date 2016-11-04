package project.MainClasses;

/**
 * Created by Santhilata.
 * Last edited 26/02/2015
 */
public interface PropertiesLoaderImplBase {
    public static final String LOCAL_HOST = "127.0.0.1";
    public static final String JADE_PORT = "1099";
    public static  final int no_of_DataServers = 6;
    public static final int no_of_userContainers = 10;
    public static final  int no_of_UsersPerCache = 10; // each user may ask many questions
    public static final int no_of_cacheUnitsPerContainer = 4;


    // Query sample files
    // QueryExpressions1 - contains hand written mixed queries
    //QueryExpressions2/3/4/5 - contains queries generated as per number of nodes and repeated segments with known distributions
    public static final String INPUT_FILE= ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions3";
    public static final String BACKUP_FILE = ".//src//main//java//project//QueryEnvironment//QueryInput//QueryGenerator_output";
    public static final String GENERATED_FILE = ".//src//main//java//project//QueryEnvironment//QueryInput//generatedFile";

    //below are the details about query arrivals
    public static final String QUERY_INTERARRIVAL_DISTRIBUTION = "Fixed"; // choose from Poisson /Random / Uniform /Fixed /Exponential distributions
    public static final String QUERY_REPETITION_DISTRIBUTION = "Uniform"; // choose from Poisson /Random / Uniform /Fixed /Exponential distributions

    //SETTINGS FOR QUERY INTER ARRIVAL TIME
    public static final double POISSON_MEAN_TIME = 380;
    public static final int UNIFORM_UPPER_TIME = 1000;
    public static final int UNIFORM_LOWER_TIME = 20;
    public static final double EXPONENTIAL_MEAN_TIME = 250;

    //settings for query repetition
    public static final int MAXIMUM_OBSERVATION_TIME_UNIT = 7200; // One time epoch of observation is taken to be one  hour.
    public static final int NUMBER_OF_QUERIES = 10;
    public static final int NUMBER_OF_QUERIES_IN_FILE = 10000;
    public static final double POISSON_MEAN_QUERY = 3700;
    public static final int UNIFORM_UPPER_QUERY =7200;
    public static final int UNIFORM_LOWER_QUERY = 20;
    public static final double EXPONENTIAL_MEAN_QUERY = 478;

    public static   int standard_Data_Unit_Size = 10;



    //settings for data size
    public static final int DATABASETABLE_CARDINALITY =1; // cardinality to be multiplied by 1000

}
