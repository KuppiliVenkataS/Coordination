package project.MiddlewareEnvironment.CacheMaintenance.SampleEA;

import project.MainClasses.CacheProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Created by santhilata on 18/07/16.
 * This class has optimization method which follows Genetic algorithm.
 *
 */
public class DataUnitOptimisation implements CacheProperties {
    public static ArrayList<QueryData>[] testData = new ArrayList[upperTime_WindowLimit]; // this list is used to store 5 windows of incoming data
    ArrayList<QueryData> trainData ;// all queries sent to query analyser per one epoch
    static ArrayList<CacheUnit> communityCache = new ArrayList(cacheUnits);// List of cache units within the community cache



    // to read Data files
    public void createInput() throws IOException {

        for (int i = 0; i < upperTime_WindowLimit; i++) {
            testData[i] = readDataFromFile(new File("/home/santhilata/Desktop/Input/input" + i + ".csv"));
        }

    }

    public ArrayList<QueryData> readDataFromFile(File file) throws IOException {
        ArrayList<QueryData> traindata = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while((line = br.readLine())!= null){
            String[] tokens = line.split(Pattern.quote(","));
            int i =0;
            QueryData queryData = new QueryData();
            queryData.setqId(tokens[i++]);
            queryData.setQueryComplexity(Integer.parseInt(tokens[i++]));
            queryData.setcLoc(tokens[i++]);


            queryData.cLocRecommend = new String[cacheUnits];
            for (int j= 0; j < cacheUnits ; j++) {
                queryData.cLocRecommend[j] = tokens[i++];
            }
            queryData.setQuerySize(Double.parseDouble(tokens[i++]));
            queryData.setRefresh(tokens[i++]);
            queryData.uLoc_frequency = new HashMap<>();

            for (int j = 0; j < numUserContainers; j++) {
                String str = tokens[i++];
                queryData.uLoc_frequency.put(str,Integer.parseInt(tokens[i++]));

            }


            queryData.totalFreq = Integer.parseInt(tokens[i++]);
            //   System.out.println(queryData.totalFreq);
            queryData.setFreq_5windows(Integer.parseInt(tokens[i++]));
            //  System.out.println(queryData.getFreq_5windows());
            queryData.cLoc_time = new HashMap<>();

            queryData.setTimeRecent(Integer.parseInt(tokens[i++]));
            if (tokens[i].equals("true")) queryData.setFoundInCache(true);
            else queryData.setFoundInCache(false);

            // System.out.println(queryData.isFoundInCache());

            traindata.add(queryData);
        }

        br.close();
        return traindata;
    }

    /**
     * Set up cache units
     */
    public void setupCacheUnits(){
        communityCache.clear();
        for (int i = 0; i < cacheUnits; i++) {
            CacheUnit cu = new CacheUnit(""+(i+1));
            communityCache.add(cu);
        }
    }

    private CacheUnit getCacheUnit(String name){
        for (int i = 0; i < communityCache.size(); i++) {
            if (name.equals(communityCache.get(i).getcName()))
                return communityCache.get(i);
        }

        return null;
    }

    private boolean isQueryInCommunityCache(QueryData qd){

        Iterator<CacheUnit> cuItr = communityCache.iterator();
        while (cuItr.hasNext()){
            CacheUnit cu = cuItr.next();
            if (cu.isCacheContainsQuery(qd))
                return  true;
        }

        return false;
    }

    public void dataOptimize(){
        for (int twi = 0; twi < upperTime_WindowLimit; twi++) {
            trainData = testData[twi];


        }
    }

    public static void main(String[] args) throws IOException {
        DataUnitOptimisation duo = new DataUnitOptimisation() ;
        duo.createInput();



    }
}
