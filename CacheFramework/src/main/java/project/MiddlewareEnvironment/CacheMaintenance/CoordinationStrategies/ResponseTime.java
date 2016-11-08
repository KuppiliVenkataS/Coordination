package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by santhilata on 04/11/16.
 * This class is to calculate the response time for various coordination strategies
 */
public class ResponseTime implements InputParameters{

    Input[] trainInput; // inputs for training
    ArrayList<Query_Coord>[] uloc_queries ;// queries originated from each of the user locations
    ArrayList<Query_Coord>[] cloc_queries;// queries at each of the cache locations
    ArrayList<Query_Coord> currentSeedQueries;
    
    int[][] uloc_query_freq = new int[numLoc][seed]; // this contains the frequency of a query repeated ata given location

    Input[] testInputs = new Input[numTests]; // inputs for testing

    public ResponseTime(){
        this.trainInput = new Input[3]; // 3 is the minimum number to have historical information
        // After every input, input[0] is deleted and other inputs are shifted one place to left.
        //one testinput is added to trainInput

        for (int i = 0; i < 3; i++) {
            this.trainInput[i] = new  Input(numQueries,seed,numLoc);
        }

        this.uloc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc ; i++) {
            this.uloc_queries[i] = new ArrayList<>();
        }

        this.cloc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc ; i++) {
            this.cloc_queries[i] = new ArrayList<>();
        }

        //create test inputs
        for (int i = 0; i < numTests; i++) {
            testInputs[i] = createInput(numQueries,seed,numLoc);
        }
    }


    public Input createInput(int numQueries, int seed, int numLoc){

          return  new Input(numQueries,seed,numLoc);


    }

    /**
     * This method essentially generates uloc_query_freq
     */
    public void generateStats(int window){


        ArrayList<Query_Coord> tempQ = trainInput[window].getQueries();

        // location-wise query lists
        for (Query_Coord qc :
                tempQ) {
            int i = Integer.parseInt(qc.getLoc());
            uloc_queries[i].add(qc);
        }

        //get frequency of queries at each uloc
        for (int i = 0; i < numLoc; i++) {
            for (Query_Coord qc :
                    uloc_queries[i]) {
                    int qNum = (int) Double.parseDouble(qc.getQuery());
                    uloc_query_freq[i][qNum] +=1;

            }
            
        }


    }

    public int[][] getUloc_query_freq(){
        return  this.uloc_query_freq;
    }

    //master-slave algorithm

    /**
     * Master selects a place for slave
     * create input for ten times.
     * Consider frequency for last three times and select that place for query
     * create new input -
     *
     */
    public void master_slave(){

        freeUpCloc_queriesList();// to clear caches
        currentSeedQueries = trainInput[0].getSeedQueries();

        // add queries to cache units one per place
       // for (int i = 0; i < numtrain ; i++) {

            int queryNo = 0;
            while (queryNo < seed) {
                for (int j = 0; j < numLoc; j++, queryNo++) {
                    if (queryNo < seed)
                        cloc_queries[j].add(getQueryObject( queryNo));// Only once added
                }

            }

       // }

        for (int i = 0; i < numTests; i++) {
        System.out.println("*********************************************************");
        System.out.println("loc      query from Master-slave");

        /*
        for (int i = 0; i < numLoc ; i++) {
            for (Query_Coord qc :
                    cloc_queries[i]) {
                System.out.println(i+"th cloc contains "+qc.getQuery());
            }
        }
        */

        //test master-slave with new query input

            ArrayList<Query_Coord> testQueries = testInputs[i].getQueries();
            int responseTime = 0;

            for (Query_Coord qtemp :
                    testQueries) {

                int queryNum = Integer.parseInt(qtemp.getQuery());
                String cloc = getCloc_querynum(queryNum);

                if (qtemp.getLoc().equals(cloc)) {
                    responseTime += 10;
                   // System.out.println(qtemp.getqID()+" "+qtemp.getLoc()+" "+cloc+" "+responseTime);
                } else {
                    responseTime += 100;
                  //  System.out.println(qtemp.getqID()+" "+qtemp.getLoc()+" "+cloc+" "+responseTime);
                }
            }

            System.out.println(i+"the test -  Response time Master-slave -- " + responseTime * (1.0) / numQueries);
        }

    }


    /**
     * This method is to free up cloc_queries  to enable new caching techniques everytime
     */
    public void freeUpCloc_queriesList(){
        for (int i = 0; i < numLoc ; i++) {
            this.cloc_queries[i] = new ArrayList<>();
        }
    }


    /**
     * The following method returns cache location where the query is stored
     * @param qc
     * @return
     */
    public String getCloc_querynum(int qc){
        String cloc = null;
        Query_Coord qtemp = getQueryObject(qc);
        for (int i = 0; i < numLoc ; i++) {
            if (cloc_queries[i].contains(qtemp)){
                cloc = ""+i;
                break;
            }
        }

        return cloc;
    }

    /**
     * to get the query from original input
     * @param queryNum
     * @return
     */
    public Query_Coord getQueryObject( int queryNum ){

        Query_Coord qtemp = null ;
        for (Query_Coord qc :
                currentSeedQueries) {
            int queryVal = Integer.parseInt(qc.getQuery());
            if (queryVal == queryNum) {
                qtemp = qc;
                break;
            }
        }

        return  qtemp;
    }

    public void cacheRefresh_LRU(){

    }


    public static void main(String[] args) {

        ResponseTime rt = new ResponseTime();

        rt.generateStats(0);

        int[][] ulocQ_F = rt.getUloc_query_freq();
/*
        System.out.println("uloc  query  freq");
        for (int i = 0; i < ulocQ_F.length; i++) {
            for (int j = 0; j < ulocQ_F[i].length; j++) {
                System.out.println(i +"     "+j+"      "+ulocQ_F[i][j]);
            }
        }
        */

        rt.master_slave();
        //rt.voting();

    }
}
