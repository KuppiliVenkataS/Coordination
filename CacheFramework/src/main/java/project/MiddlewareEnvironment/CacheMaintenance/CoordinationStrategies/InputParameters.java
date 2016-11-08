package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import java.io.File;

/**
 * Created by santhilata on 07/11/16.
 */
public interface InputParameters {
    public static int numQueries = 1000;//{1000,2000,5000,7000,10000};
    public static   int seed = 25; //{25, 50, 100, 150, 200 };
    public static int numLoc = 6;
    public static int cloc_size = 100; //GB
    public static int numTests = 5;
    public static int numtrain =3;
    String inputDistribution = "Poisson"; // {"Random","Uniform"}

    int freq_threshold = 6;

    public static  String inputFile = "//home//santhillata//Dropbox//";
}
