package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
        //to test
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
     * CONSIDER FOR DELETION OF THIS METHOD
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

    /**
     * The following method is create uloc_query frequency
     * @param inputs
     * @return
     */
    public int[][] generateUloc_Query_Freq(Input[] inputs){

        ArrayList<Query_Coord> queryList = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            queryList.addAll(inputs[i].getQueries());
        }

        int[][] uloc_query_freq = new int[numLoc][seed];

        ArrayList<Query_Coord>[] uLoc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc; i++) {
            uLoc_queries[i] = new ArrayList<>();

        }

        for (Query_Coord qc :
                queryList) {
            int loc = Integer.parseInt(qc.getLoc());
            uLoc_queries[loc].add(qc);
        }

        //get frequency stats
        for (int i = 0; i < numLoc; i++) {
            for (Query_Coord qc :
                    uLoc_queries[i]) {
                int qNum = Integer.parseInt(qc.getQuery());
                uloc_query_freq[i][qNum] +=1;
            }

        }

        return uloc_query_freq;

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
        System.out.println("*************************MASTER-SLAVE********************************");


        for (int i = 0; i < numTests; i++) {

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
                    responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
                  //  System.out.println(qtemp.getqID()+" "+qtemp.getLoc()+" "+cloc+" "+responseTime);
                }
            }

            System.out.println(i+"the test -  Response time Master-slave -- " + responseTime * (1.0) / numQueries);
        }

    }


    /**
     * Algorithm for this strategy is to create a basic plan such as master-slave.
     * for each test input,
     * All cache agents vote yes or no .
     * If cache hits are > numQueries/20 per cache site, yes
     * else no.
     * if yes_vote, keep the cache plan.
     * else no_vote, add frequency preferences and poll again
     */
    public void voting(){

        System.out.println("*************************VOTING********************************");
        int yes_vote = numLoc/2+1;
        //System.out.println(yes_vote); // 4 for numLoc = 6
        // now check basic cache arrangement  with testinputs
        for (int i = 0; i < numTests; i++) {

        freeUpCloc_queriesList();// to clear caches
        currentSeedQueries = trainInput[0].getSeedQueries();

        //create basic master-slave plan
        int queryNo = 0;
        while (queryNo < seed) {
            for (int j = 0; j < numLoc; j++, queryNo++) {
                if (queryNo < seed)
                    cloc_queries[j].add(getQueryObject( queryNo));// Only once added
            }

        }





            int responseTime = 0;

            ArrayList<Query_Coord> testQueries = testInputs[i].getQueries();
            int[] cacheHits = new int[numLoc];

            for (Query_Coord qtemp :
                    testQueries) {
                String cloc = getCloc_querynum(Integer.parseInt(qtemp.getQuery()));
                if (qtemp.getLoc().equals(cloc)) {
                    responseTime += 10;
                    cacheHits[Integer.parseInt(cloc)]++;
                   //  System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                } else {
                    responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
                    // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                }
            }

           // System.out.println(i + " test -  Response time basic Voting -- " + responseTime * (1.0) / numQueries);

            int numVotes = 0;
            for (int j = 0; j < numLoc; j++) {
                if (cacheHits[j]>= (numQueries*0.03)) {
                  //  System.out.println(j+" inside cache hits "+cacheHits[j]);

                    numVotes++;
                }
            }

           /* if (numVotes >= yes_vote) {
                System.out.println(" &&&& "+numVotes+ " basic cache set is fine");
                System.out.println("******************************************************************************");
            }

            else
            */

            { // Arrange another cache arrangement
                    // System.out.println("*************************************************************************");
                freeUpCloc_queriesList();// to clear caches
                String[] query_cacheLoc = new String[seed];
                int[][] uloc_query_freq = generateUloc_Query_Freq(trainInput);

                //cache allocation based on the highest bidder
                for (int ii = 0; ii < seed; ii++) {
                    int max = -999; int cLoc = 0;
                    for (int jj = 0; jj < uloc_query_freq.length; jj++) {
                        if (uloc_query_freq[jj][ii]> max){
                            max = uloc_query_freq[jj][ii];
                            cLoc = jj;
                        }

                    }
                    cloc_queries[cLoc].add(getQueryObject(ii)); // add iith query at jjth location

                }



                responseTime = 0;

                Arrays.fill(cacheHits,0);

                for (Query_Coord qtemp :
                        testQueries) {
                    String cloc = getCloc_querynum(Integer.parseInt(qtemp.getQuery()));
                    if (qtemp.getLoc().equals(cloc)) {
                        responseTime += 10;
                        cacheHits[Integer.parseInt(cloc)]++;
                        // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                    } else {
                        responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
                        // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                    }
                }

                System.out.println(i + " test -  Response time after II round Voting -- " + responseTime * (1.0) / numQueries);

                numVotes = 0;
                for (int j = 0; j < numLoc; j++) {
                    if (cacheHits[j]>= (numQueries*0.02)) {
                       // System.out.println("inside cache hits "+cacheHits[j]);

                        numVotes++;
                    }
                }


                /*
                if (numVotes >= yes_vote) {
                    System.out.println(numVotes+" second level cache set is fine");
                }
                else {
                    System.out.println(" &&& "+numVotes+" Second level is also failed");
                }
                */

            }


        }
    }

    /**
     * Each cache sends a list based on some local threshold based on time and frequency and storage
     * Initial set up is with voting (frequency based)
     *
     */
    public void multi_agentPlanning(){

        System.out.println("************** MULTI AGENT PLANNING *********************");
        for (int testNo = 0; testNo < numTests; testNo++) {

            freeUpCloc_queriesList(); //  free up all caches

            int[][] uloc_query_freq = generateUloc_Query_Freq(trainInput); // uloc_query_freq is => [loc][seed]

            //cache selection lists
            ArrayList[] temp_query_lists = new ArrayList[seed];
            for (int i = 0; i < seed; i++) {
                temp_query_lists[i] = new ArrayList<>();
            }

            // the following lines add all locations for each query
            for (int i = 0; i < seed; i++) {
                for (int j = 0; j < numLoc; j++) {
                    if (uloc_query_freq[j][i]> freq_threshold  ){
                        temp_query_lists[i].add(j);
                    }
                }
            }

            //for each query resolve contention

            ArrayList remainingQueries = new ArrayList();

           // System.out.println("------------- Selected queries ----------");
            for (int i = 0; i < seed; i++) {
                int max = -9999; int cloc = -8888;

                if (temp_query_lists[i].size()==0){
                    remainingQueries.add(i);
                }
                else {
                   // System.out.print(i +" ");
                    for (int j = 0; j < temp_query_lists[i].size(); j++) {
                        if (uloc_query_freq[j][i] > max) {
                            max = uloc_query_freq[j][i];
                            cloc = j;
                        }
                    }
                    cloc_queries[cloc].add(getQueryObject(i)); // add the query to cache location that has highest bidding of frequency
                }

            }
            /*

            System.out.println();


            System.out.println("___________Remaining queries_____________");
            for (int i = 0; i < remainingQueries.size(); i++) {
                System.out.print(remainingQueries.get(i)+" ");
            }
            System.out.println();
            */

            if (option.equals("FCFP")) {
                //determine which cache has freespace       and fill it up with queries
                // queries that are not contended are allocated to first come first serve by adding queries in the space
                int maxQueries = seed / numLoc;
                Iterator itr = remainingQueries.iterator();

                for (int j = 0; j < numLoc; j++) {
                    int freespace = 0;
                    if (cloc_queries[j].size() < maxQueries) {
                        freespace = maxQueries - cloc_queries[j].size();
                    }

                    for (int ii = 0; ii < freespace && itr.hasNext(); ii++) {
                        cloc_queries[j].add(getQueryObject((int) itr.next()));
                    }

                }
            }

            else if (option.equals("PRU")) {
                placeRemainingQueries(remainingQueries,testNo);


                for (int k = 0; k < numLoc; k++) {
                    for (int j = 0; j <cloc_queries[k].size() ; j++) {
                        System.out.println(k +" --- "+cloc_queries[k].get(j));
                    }
                }

            }





        }

    }

    public void placeRemainingQueries(ArrayList remainingQueries, int i){


       // for (int i = 0; i < numTests; i++) {

            int[][] uLoc_query_freq = new int[numLoc][seed];

            ArrayList<Query_Coord> tempQueries = null;
            if (i > 0) {
                //find uloc_query_freq for a single testinput

                tempQueries = testInputs[i - 1].getQueries(); // use historical information
                for (Query_Coord qtemp :
                        tempQueries) {
                    int seedValue = Integer.parseInt(qtemp.getQuery());
                    int uLoc = Integer.parseInt(qtemp.getLoc());
                    uLoc_query_freq[uLoc][seedValue] += 1;

                }
            } else { // for i=0, use train input data
                uLoc_query_freq = generateUloc_Query_Freq(trainInput);
            }



            for (int j = 0; j < remainingQueries.size(); j++) {
                int caloc = -999; int max = -99999;
                for (int k = 0; k < numLoc; k++) {
                    if (uLoc_query_freq[k][j] > max) {
                        max = uLoc_query_freq[k][j];

                        caloc = k;

                    }

                }
              //  System.out.println(remainingQueries.size()+" DIRTY ANSWERS        "+caloc);
                cloc_queries[caloc].add(getQueryObject(j));
            }
        //}
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
        rt.voting();
        rt.multi_agentPlanning();


    }
}
