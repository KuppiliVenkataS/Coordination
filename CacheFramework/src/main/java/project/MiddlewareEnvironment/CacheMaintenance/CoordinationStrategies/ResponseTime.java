package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import project.SupportSystem.MapUtil;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
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
    DecimalFormat df = new DecimalFormat("#.00");


    int[][] uloc_query_freq ; // this contains the frequency of a query repeated ata given location

    int numLoc;
    int seed;
    int numQueries;
    String inputDistribution;


    public ResponseTime(String repeat, int numQ, int numCache, int numseed) throws IOException {
        uloc_query_freq =  new int[numLoc][seed];
        this.numLoc = numCache;
        this.inputDistribution = repeat;
        this.numQueries = numQ;
        this.seed = numseed;

        createInput(); // creates new input files everytime. If you do not want to change input, just put this line in comments


        this.trainInput = new Input[numtrain]; // 3 is the minimum number to have historical information
        for (int i = 0; i < numtrain; i++) {
            this.trainInput[i] = new Input();
            trainInput[i].setInput_queries(readQueriesFromFile(inputFile[i]));
            trainInput[i].setSeedQueries( readQueriesFromFile(new File("_seed.csv")));
            currentSeedQueries = readQueriesFromFile(new File("_seed.csv"));
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
            //inputFile[i] = new File(inputFolder+"_"+i+".csv");
            inputFile[i] = new File("_"+i+".csv");
            createInputFile(inputFile[i]);
        }

        //create seed file
       // File  seedFile = new File (inputFolder+"_seed.csv");
        File  seedFile = new File ("_seed.csv");
        createSeedFile(seedFile);



    }

    public File createInputFile(File file) throws IOException{

        FileWriter fw = new FileWriter(file);
       // System.out.println(" from responsetime - 128 "+numQueries+" "+seed+" "+numLoc+" "+inputDistribution);
        Input input = new Input(numQueries,seed,numLoc,inputDistribution);
        for (Query_Coord qc :
                input.getQueries()) {
            fw.write(qc.getqID()+","+qc.getLoc()+","+qc.getQuery()+"\n");
        }

        fw.close();
        return file;

    }

    public File createSeedFile(File file) throws IOException{

        FileWriter fw = new FileWriter(file);
        Input input = new Input(numQueries,seed,numLoc,inputDistribution);
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
    public Result master_slave(){

        double res = 0.0;
       // HashMap<String,Double> responseTime_query  = new HashMap<>();
        int messages = 0;

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
            messages = numLoc;

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
                  //  responseTime +=100;
                    if (cloc != null)
                        responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                    else {
                        responseTime +=100;
                        // responseTime += numLoc * 10;
                    }
                   // responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
                  //  System.out.println(qtemp.getqID()+" "+qtemp.getLoc()+" "+cloc+" "+responseTime);
                }
            }

             res += responseTime * (1.0) / numQueries;
            System.out.println(i+"the test -  Response time Master/slave -- " + responseTime * (1.0) / numQueries);

           // System.out.println("res = "+responseTime);
        }

        res = res/numTests;



        return  new Result(res,messages);
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
    public Result voting(){

       // HashMap<String,Double> responseTime_query = new HashMap<>();
        double res = 0.0;
        int messages = 0;

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

        messages = numLoc;

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
               // responseTime +=100;
                if (cloc != null)
                    responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                else {
                 //   responseTime +=100;
                    if (cloc != null)
                        responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                    else {
                        responseTime +=100;
                        // responseTime += numLoc * 10;
                    }
                    // responseTime += numLoc * 10;
                }
               // responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
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
        messages += numLoc ; // receive votes

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


                messages += numLoc;
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
                     //   responseTime +=100;
                        if (cloc != null)
                            responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                        else {
                            responseTime +=100;
                            // responseTime += numLoc * 10;
                        }
                       // responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
                        // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                    }
                }

                 res += responseTime * (1.0) / numQueries;
                System.out.println(i+"the test -  Response time Voting -- " + responseTime * (1.0) / numQueries);
               // responseTime_query.put("Voting",res);

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

        return new Result(res/numTests,messages);
    }

    /**
     * Each cache sends a list based on some local threshold based on time and frequency and storage
     * Initial set up is with voting (frequency based)
     *
     */
    public Result multi_agentPlanning(){

        //HashMap<String,Double> responseTime_query = new HashMap<>();
        double res= 0.0;
        int messages = 0;

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
                /*

                for (int k = 0; k < numLoc; k++) {
                    for (int j = 0; j <cloc_queries[k].size() ; j++) {

                        System.out.println(k +" --***- "+cloc_queries[k].get(j).getQuery());
                    }
                }
                */

            }

            int responseTime = 0;

            ArrayList<Query_Coord> testQueries = testInputs[testNo].getQueries();
            int[] cacheHits = new int[numLoc];

            for (Query_Coord qtemp :
                    testQueries) {
                String cloc = getCloc_querynum(Integer.parseInt(qtemp.getQuery()));
               // System.out.println(qtemp.getQuery()+ "    "+cloc);
                if (qtemp.getLoc().equals(cloc)) {
                    responseTime += 10;
                    cacheHits[Integer.parseInt(cloc)] += 1;
                    //  System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                } else {
                    if (cloc!= null)
                        responseTime += Math.abs(Integer.parseInt(qtemp.getLoc())-Integer.parseInt(cloc))*10;
                    else{
                      //  responseTime +=100;
                        if (cloc != null)
                            responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                        else {
                            responseTime +=100;
                            // responseTime += numLoc * 10;
                        }
                       // responseTime += numLoc*10;
                    }
                    // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                }
            }

             res += responseTime * (1.0) / numQueries;
            System.out.println(testNo+"the test -  Response time Multi-agent -- " + responseTime * (1.0) / numQueries);
            //responseTime_query.put("Multi-agent",res);

        }

        return  new Result(res/numTests,2*numLoc);

    }

    /**
     * This method is supposed to place remaining queries according to frequency
     * and then least recently used
     * @param remainingQueries
     * @param i - is the window number (epoch)
     */
    public void placeRemainingQueries(ArrayList remainingQueries, int i){

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
                int remainQuery = (int)remainingQueries.get(j);
                for (int k = 0; k < numLoc; k++) {
                    if (uLoc_query_freq[k][remainQuery] > max) {
                        max = uLoc_query_freq[k][remainQuery];

                        caloc = k;

                    }

                }

                cloc_queries[caloc].add(getQueryObject(remainQuery));
            }
        //}
    }


    /**
     * This method finds data associations among seed queries - frequency of togetherness
     * Each cache gets highly associated query lists
     * contention is resolved by number of queries a cache can have
     * and associations at lower levels
     */
    public Result negotiation(){

        //HashMap<String,Double> responseTime_query = new HashMap<>();
        double res = 0.0;
        int messages = 0;
        System.out.println("************** NEGOTIATION *********************");
        //create association matrix

        int[][] association_matrix = new int[seed][seed];
        ArrayList<Query_Coord>[] tempCacheLists = new ArrayList[numLoc];
        for (int i = 0; i < trainInput.length; i++) {
            ArrayList<Query_Coord> testQueries = trainInput[i].getQueries();

            for (int j = 0; j < testQueries.size(); j++) {
                for (int k = j+1; k < testQueries.size(); k++) {
                    int j1 = Integer.parseInt(testQueries.get(j).getQuery());
                    int k1 = Integer.parseInt(testQueries.get(k).getQuery());
                    if(j1 != k1){
                        association_matrix[j1][k1] +=1;
                        association_matrix[k1][j1] = association_matrix[j1][k1];
                    }

                }
            }

        }
//===============================================
    /*    freeUpCloc_queriesList(); // free up caches
        // first allocate queries to caches according to master/slave
        int queryNo = 0;
        while (queryNo < seed) {
            for (int j = 0; j < numLoc; j++, queryNo++) {
                if (queryNo < seed)
                    cloc_queries[j].add(getQueryObject( queryNo));// Only once added
            }

        }
        */
        //===============================================================

        //******************************************************************
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
            int max = -9999;
            int cloc = -8888;

            if (temp_query_lists[i].size() == 0) {
                remainingQueries.add(i);
            } else {
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
        //******************************************************************

        messages += 2*numLoc;
        //for each cache, check associations and create contention lists
        int max_queriesAtaCache = numQueries/numLoc;


        Map<Integer,Long>[] tempCacheRequirementMaps = new HashMap[numLoc];
        ArrayList frequentQueries = new ArrayList();

        //check cache associations among themselves
        for (int i = 0; i < numLoc; i++) {

             tempCacheRequirementMaps[i] = new HashMap<>();
            // determine maximum value
            for (Query_Coord qtemp :
                    cloc_queries[i]) {

                int j = Integer.parseInt(qtemp.getQuery());

                int max = 0;
                for (int k = j + 1; k < seed; k++) {
                    if (association_matrix[j][k] > max) {
                        max = association_matrix[j][k];

                    }
                }



                // add all querys that have  associations more than max/2 for each of them &&  combine all lists
                for (int k = j; k < seed; k++) {
                    if ((association_matrix[j][k]*100.0)/max > 50){
                        if (!frequentQueries.contains(k)) frequentQueries.add(k);

                        if (!tempCacheRequirementMaps[i].containsKey(k)) {
                            tempCacheRequirementMaps[i].put(k, (long)association_matrix[j][k]);

                           // System.out.println(i + " " + j + " " + k + " " + association_matrix[j][k] + " " + max);
                        }
                        else {
                            tempCacheRequirementMaps[i].put(k, (tempCacheRequirementMaps[i].get(k)+association_matrix[j][k]));
                        }
                    }


                }



            }
            tempCacheRequirementMaps[i] = MapUtil.sortByValue(tempCacheRequirementMaps[i]);

        }

        //Now re-assign queries to caches
        //freeUpCloc_queriesList(); //free up all memory



        for (int j = 0; j < seed; j++) {

            if (frequentQueries.contains(j)){

                long max = 0L; int loc = -999;
                for (int i = 0; i < numLoc; i++) {

                    if (tempCacheRequirementMaps[i].containsKey(j)){
                        if (tempCacheRequirementMaps[i].get(j) > max){
                            max = tempCacheRequirementMaps[i].get(j);
                            loc = i;
                        }
                    }
                }
                if (!cloc_queries[loc].contains(getQueryObject(j))) {
                    cloc_queries[loc].add(getQueryObject(j));
                    messages +=1;

                   // System.out.println("adding " + j + " at " + loc);
                }

                for (int i = 0; i < numLoc; i++) {
                    if (i != loc && cloc_queries[i].contains(getQueryObject(j))){
                        cloc_queries[i].remove(getQueryObject(j));
                        messages += 1;
                    }
                }

            }

        }


        //calculate response time
        for (int testNo = 0; testNo < numTests; testNo++) {


            int responseTime = 0;

            ArrayList<Query_Coord> testQueries = testInputs[testNo].getQueries();
            int[] cacheHits = new int[numLoc];

            for (Query_Coord qtemp :
                    testQueries) {
                String cloc = getCloc_querynum(Integer.parseInt(qtemp.getQuery()));
                // System.out.println(qtemp.getQuery()+ "    "+cloc);
                if (qtemp.getLoc().equals(cloc)) {
                    responseTime += 10;
                    cacheHits[Integer.parseInt(cloc)] += 1;
                    //  System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                } else {
                    if (cloc != null)
                        responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                    else {
                      //  responseTime +=100;
                        if (cloc != null)
                            responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                        else {
                            responseTime +=100;
                            // responseTime += numLoc * 10;
                        }
                       // responseTime += numLoc * 10;
                    }
                    // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                }
            }

             res += responseTime * (1.0) / numQueries;
            System.out.println(testNo+"the test -  Response time feedback -- " + responseTime * (1.0) / numQueries);
           // responseTime_query.put("Feedback",res);

        }

        return new Result(res/numTests,messages);
    }

    public ArrayList createOneItemList(Input input){
        int[] query_freq = new int[seed];


        ArrayList tempList = new ArrayList();
        ArrayList<Query_Coord> testQueries = input.getQueries();

       // for (int largeList = 0; largeList < numQueries/numLoc; largeList++) {

            for (int j = 0; j < testQueries.size(); j++) {
                int id = Integer.parseInt(testQueries.get(j).getQuery());
                query_freq[j] += 1;
            }
            for (int i = 0; i < seed; i++) {
                if (query_freq[i] > freq_threshold) {
                    tempList.add(query_freq[i]);
                }
            }
            if (tempList.size() > 0) query_freq = new int[tempList.size()];
       // }

        return  tempList;
    }



    public Result feedback(){

       // HashMap<String,Double> responseTime_query = new HashMap<>();
        double res= 0.0;
        int messages = 0;

        System.out.println("************** FEEDBACK NEGOTIATION *********************");
        //create association matrix

        int[][] association_matrix = new int[seed][seed];
        ArrayList<Query_Coord>[] tempCacheLists = new ArrayList[numLoc];
        for (int i = 0; i < trainInput.length; i++) {
            ArrayList<Query_Coord> testQueries = trainInput[i].getQueries();

            for (int j = 0; j < testQueries.size(); j++) {
                for (int k = j+1; k < testQueries.size(); k++) {
                    int j1 = Integer.parseInt(testQueries.get(j).getQuery());
                    int k1 = Integer.parseInt(testQueries.get(k).getQuery());
                    if(j1 != k1){
                        association_matrix[j1][k1] +=1;
                        association_matrix[k1][j1] = association_matrix[j1][k1];
                    }

                }
            }

        }
//===============================================
/*    freeUpCloc_queriesList(); // free up caches
    // first allocate queries to caches according to master/slave
    int queryNo = 0;
    while (queryNo < seed) {
        for (int j = 0; j < numLoc; j++, queryNo++) {
            if (queryNo < seed)
                cloc_queries[j].add(getQueryObject( queryNo));// Only once added
        }

    }
    */
        //===============================================================

        //******************************************************************
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
            int max = -9999;
            int cloc = -8888;

            if (temp_query_lists[i].size() == 0) {
                remainingQueries.add(i);
            } else {
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

        messages += 2;
        //******************************************************************

        //for each cache, check associations and create contention lists
        int max_queriesAtaCache = numQueries/numLoc;


        Map<Integer,Long>[] tempCacheRequirementMaps = new HashMap[numLoc];
        ArrayList frequentQueries = new ArrayList();

        //check cache associations among themselves
        for (int i = 0; i < numLoc; i++) {

            tempCacheRequirementMaps[i] = new HashMap<>();
            // determine maximum value
            for (Query_Coord qtemp :
                    cloc_queries[i]) {

                int j = Integer.parseInt(qtemp.getQuery());

                int max = 0;
                for (int k = j + 1; k < seed; k++) {
                    if (association_matrix[j][k] > max) {
                        max = association_matrix[j][k];

                    }
                }

                // max = max/2;

                // add all querys that have  associations more than max/2 for each of them &&  combine all lists
                for (int k = j; k < seed; k++) {
                    if ((association_matrix[j][k]*100.0)/max > 20){
                        if (!frequentQueries.contains(k)) frequentQueries.add(k);

                        if (!tempCacheRequirementMaps[i].containsKey(k)) {
                            tempCacheRequirementMaps[i].put(k, (long)association_matrix[j][k]);
                            // System.out.println(i + " " + j + " " + k + " " + association_matrix[j][k] + " " + max);
                        }
                        else {
                            tempCacheRequirementMaps[i].put(k, (tempCacheRequirementMaps[i].get(k)+association_matrix[j][k]));
                        }
                    }


                }



            }
            tempCacheRequirementMaps[i] = MapUtil.sortByValue(tempCacheRequirementMaps[i]);

        }

    /*
    ArrayList remainingQueries = new ArrayList();
    for (int i = 0; i < seed; i++) {
        if (!frequentQueries.contains(i)) remainingQueries.add(i);
    }

*/
        //No re assign queries to caches
        //   freeUpCloc_queriesList(); //free up all memory



        for (int j = 0; j < seed; j++) {

            if (frequentQueries.contains(j)){

                long max = 0L; int loc = -999;
                for (int i = 0; i < numLoc; i++) {

                    if (tempCacheRequirementMaps[i].containsKey(j)){
                        if (tempCacheRequirementMaps[i].get(j) > max){
                            max = tempCacheRequirementMaps[i].get(j);
                            loc = i;
                        }
                    }
                }
                if (!cloc_queries[loc].contains(getQueryObject(j))) {
                    cloc_queries[loc].add(getQueryObject(j));
                    messages += 1;
                    // System.out.println("adding " + j + " at " + loc);
                }

                for (int i = 0; i < numLoc; i++) {
                    if (i != loc && cloc_queries[i].contains(getQueryObject(j))){
                        cloc_queries[i].remove(getQueryObject(j));
                        messages +=1;
                    }
                }

            }

        }

        //calculate response time
        for (int testNo = 0; testNo < numTests; testNo++) {


            int responseTime = 0;

            ArrayList<Query_Coord> testQueries = testInputs[testNo].getQueries();
            int[] cacheHits = new int[numLoc];

            for (Query_Coord qtemp :
                    testQueries) {
                String cloc = getCloc_querynum(Integer.parseInt(qtemp.getQuery()));
                // System.out.println(qtemp.getQuery()+ "    "+cloc);
                if (qtemp.getLoc().equals(cloc)) {
                    responseTime += 10;
                    cacheHits[Integer.parseInt(cloc)] += 1;
                    //  System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                } else {
                    if (cloc != null)
                        responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                    else {
                        //  responseTime +=100;
                        if (cloc != null)
                            responseTime += Math.abs(Integer.parseInt(qtemp.getLoc()) - Integer.parseInt(cloc)) * 10;
                        else {
                            responseTime +=100;
                            // responseTime += numLoc * 10;
                        }
                        // responseTime += numLoc * 10;
                    }
                    // System.out.println(qtemp.getqID() + " " + qtemp.getLoc() + " " + cloc + " " + responseTime);
                }
            }

            res += responseTime * (1.0) / numQueries;
            System.out.println(testNo+"the test -  Response time feedback -- " + responseTime * (1.0) / numQueries);
            //responseTime_query.put("Feedback",res);

        }
        return  new Result(res/numTests, messages);

    }



    public void cacheRefresh_LRU(){

    }

    class Result{
        double res;
        int messages;

        public Result(double res, int messages) {
            this.res = res;
            this.messages = messages;
        }

        public double getRes() {
            return res;
        }

        public void setRes(double res) {
            this.res = res;
        }

        public int getMessages() {
            return messages;
        }

        public void setMessages(int messages) {
            this.messages = messages;
        }
    }

    public static void main(String[] args) throws IOException {


        File outputFile = new File("output_temp.csv");
        FileWriter fw = new FileWriter(outputFile);
        fw.write("Repeatability,Strategy,NumQueries,numCache,numSeeds,ResponseTime,numMessages\n");

        String[] repeatability = {"Poisson","Random","Uniform","Exponential"};
        String[] Strategy = {"Master/slave","Voting","Multi-agent","Negotiation","Feedback"};
        int[] numQ = {20000,25000 ,30000, 35000};
        int[] numcache = {35,40,50,55};
        int[] numSeeds ={175, 200,250};

        DecimalFormat df = new DecimalFormat("#0.00");
       //double responseTime_queryMain = 0.0;
        Result result;
/*
        for (int i = 0; i < repeatability.length; i++) {
            for (int j = 0; j < numQ.length; j++) {
                for (int k = 0; k < numcache.length; k++) {
                    for (int l = 0; l < numSeeds.length; l++) {
                        String repeat = repeatability[i];
                        int numQuery = numQ[j];
                        int numcaches = numcache[k];
                        int numSeed = numSeeds[l];
                        ResponseTime rt = new ResponseTime(repeat,numQuery,numcaches,numSeed);
                        result = rt.master_slave();
                        System.out.println(repeat+",Master/slave,"+numQuery+","+numcaches+","+numSeed+df.format(result.getRes())+","+result.getMessages()+"\n");
                        fw.append(repeat+",Master/slave,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");

                        result = rt.voting();
                        System.out.println(repeat+",voting,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");
                        fw.append(repeat+",Voting,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");
                        result = rt.multi_agentPlanning();
                        System.out.println(repeat+",Multi-agent,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");
                        fw.append(repeat+",Multi-agent,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");

                        result = rt.negotiation();
                        System.out.println(repeat+",negotiation,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");
                        fw.append(repeat+",Negotiation,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");
                        result = rt.feedback();
                        System.out.println(repeat+",feedback,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");
                        fw.append(repeat+",feedback,"+numQuery+","+numcaches+","+numSeed+","+df.format(result.getRes())+","+result.getMessages()+"\n");


                    }
                }
            }
        }
*/

        for (int i = 0; i < repeatability.length; i++) {


            for (int numQuery = 1000; numQuery < 5000; numQuery += 1000) {
                 String repeat = repeatability[i];

                int numcaches = 6;
                int numSeed = 25;
                ResponseTime rt = new ResponseTime(repeat, numQuery, 6, 25);
                result = rt.master_slave();
                System.out.println(repeat + ",Master/slave," + numQuery + "," + numcaches + "," + numSeed + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                fw.append(repeat + ",Master/slave," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");

                result = rt.voting();
                System.out.println(repeat + ",voting," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                fw.append(repeat + ",Voting," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                result = rt.multi_agentPlanning();
                System.out.println(repeat + ",Multi-agent," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                fw.append(repeat + ",Multi-agent," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");

                result = rt.negotiation();
                System.out.println(repeat + ",negotiation," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                fw.append(repeat + ",Negotiation," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                result = rt.feedback();
                System.out.println(repeat + ",feedback," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");
                fw.append(repeat + ",feedback," + numQuery + "," + numcaches + "," + numSeed + "," + df.format(result.getRes()) + "," + result.getMessages() + "\n");

            }
        }

        System.out.println("Done");
        fw.close();
    }
}
