package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by santhilata on 04/11/16.
 * This class is to calculate the response time for various coordination strategies
 *
 */
public class ResponseTime implements InputParameters{

    File[] inputFile = new File[8]; //inputFile[0] - inputfile[2] are for training. remaining 5 are for testing


    Input[] trainInput; // inputs for training
    Input[] testInputs ; // inputs for testing

    ArrayList<Query_Coord>[] uloc_queries ;// queries originated from each of the user locations
    ArrayList<Query_Coord>[] cloc_queries;// queries at each of the cache locations
    ArrayList<Query_Coord> currentSeedQueries;
    
    int[][] uloc_query_freq = new int[numLoc][seed]; // this contains the frequency of a query repeated ata given location



    public ResponseTime() throws IOException {
        createInput(); // creates new input files everytime. If you do not want to change input, just put this line in comments


        this.trainInput = new Input[numtrain]; // 3 is the minimum number to have historical information
        for (int i = 0; i < numtrain; i++) {
            this.trainInput[i] = new Input();
            trainInput[i].setInput_queries(readQueriesFromFile(inputFile[i]));
            trainInput[i].setSeedQueries( readQueriesFromFile(new File(inputFolder+"_seed.csv")));
            currentSeedQueries = readQueriesFromFile(new File(inputFolder+"_seed.csv"));
        }

        testInputs = new Input[numTests];
        for (int i = 0; i < numTests; i++) {
            this.testInputs[i] = new Input();
            testInputs[i].setInput_queries(readQueriesFromFile(inputFile[i+3]));
        }


        this.uloc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc ; i++) {
            this.uloc_queries[i] = new ArrayList<>();
        }

        this.cloc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc ; i++) {
            this.cloc_queries[i] = new ArrayList<>();
        }

        /*
        for (int i = 0; i < numtrain; i++) {
            System.out.println("train input created "+i);
            Input input_test = trainInput[i];
            for (Query_Coord qc :
                    input_test.getQueries()) {
                System.out.println(qc.toString());
            }
        }
        */


    }


    public ArrayList<Query_Coord> readQueriesFromFile (File file)throws  IOException {

        ArrayList<Query_Coord> tempQueries = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine())!= null){
            String[] tokens = line.split(Pattern.quote(","));
            Query_Coord qc = new Query_Coord();
            qc.setqID(Integer.parseInt(tokens[0]));
            qc.setLoc(tokens[1]);
            qc.setQuery(tokens[2]);

            tempQueries.add(qc);
        }

        return tempQueries;

    }

    // the following is called only once
    public void createInput() throws IOException {
        int totalInput = numtrain+numTests; // all input files for training and testing
        for (int i = 0; i < totalInput; i++) {
            inputFile[i] = new File(inputFolder+"_"+i+".csv");
            createInputFile(inputFile[i]);
        }

        //create seed file
        File  seedFile = new File (inputFolder+"_seed.csv");
        createSeedFile(seedFile);



    }

    public File createInputFile(File file) throws IOException{

        FileWriter fw = new FileWriter(file);
        Input input = new Input(numQueries,seed,numLoc);
        for (Query_Coord qc :
                input.getQueries()) {
            fw.write(qc.getqID()+","+qc.getLoc()+","+qc.getQuery()+"\n");
        }

        fw.close();
        return file;

    }

    public File createSeedFile(File file) throws IOException{

        FileWriter fw = new FileWriter(file);
        Input input = new Input(numQueries,seed,numLoc);
        for (Query_Coord qc :
                input.seedQueries) {
            fw.write(qc.getqID()+","+qc.getLoc()+","+qc.getQuery()+"\n");
        }

        fw.close();

        return file;
    }

    /**
     * This method essentially generates uloc_query_freq
     */
    public void generateStats(int fileNum){


        ArrayList<Query_Coord> tempQ = trainInput[fileNum].getQueries();

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



    public void voting(){

    }

    public void multi_agentPlanning(){

    }

    public void negotiation(){

    }

    public void feedback(){

    }

    public void cacheRefresh_LRU(){

    }

    public static void main(String[] args) throws IOException {

        ResponseTime rt = new ResponseTime();
        rt.master_slave();


    }
}
