package project.MainClasses;

import jade.util.leap.List;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.log4j.Logger;
import project.DatabaseInfo.DatabaseServer;
import project.MiddlewareEnvironment.Cache;
import project.MiddlewareEnvironment.Container;
import project.MiddlewareEnvironment.QueryIndexFiles.Cache_Reply;
import project.MiddlewareEnvironment.QueryIndexFiles.IndexedQuery;
import project.MiddlewareEnvironment.QueryIndexFiles.QueryIndex;
import project.MiddlewareEnvironment.QueryIndexFiles.UpdatedData;
import project.Network.NetworkConstants;
import project.Network.NetworkTopography;
import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.QueryInput.Query_type;
import project.QueryEnvironment.QueryInput.Time_type;
import project.QueryEnvironment.Query_Subj_Predicate;
import project.QueryEnvironment.Querycriteria;
import project.QueryEnvironment.SubjectQuerySegment;
import project.ResponseTimeSimulation.CommunityCache_Response.*;
import project.UserEnvironment.QuerySegment_ExecutionTime;

import java.io.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

/**
 * Created by santhilata on 30/03/15.
 */
public class CommunityCacheMain_LRU implements PropertiesLoaderImplBase,NetworkConstants{

    protected static final Logger log = Logger.getLogger(CommunityCacheMain.class);
    public  String QUERY_INPUT_FILE= ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions";

    int noOfQueries;
    static ArrayList<Query_Response>  query_responseTime ;
    DatabaseServer[] dataServers = new DatabaseServer[no_of_DataServers];
    ArrayList<Container> user_containers = new ArrayList<>();

    int cacheMaintenanceStartTime;
    int getCacheMaintenanceEndTime;
    int noOfQueriesPresentOnNetwork;
    int noOfRepeatedQueries=0;

    String status;
    static NetworkTopography networkTopography = new NetworkTopography();
    static  int queriesDone=0;
    public static  final String FULLY_FOUND = "FULLY FOUND";
    public static final String PARTIALLY_FOUND = "PARTIALLY FOUND";
    public static final String NOT_FOUND = "NOT FOUND";
    public static final String FULL_QUERY_SEARCH_ONLY= "FullQuerySearch";
    public static  final String PARTIAL_QUERY_FRAGMENTS = "PartialQuerySearch";
    public static final String TIME_THRESHOLD_ONLY = "TimeThreshold";
    public static final String FREQUENCY_THRESHOLD_ONLY = "FrequencyThreshold";
    public static final String COMBINED_ALGO_MAINTENANCE ="CombinedAlgoMaintenance";
    public static final String LRU = "LRU";
    public static final String LFU = "LFU";
    public static final String NONE = "None";

    DecimalFormat df = new DecimalFormat("#.##");

    // variables for cache maintenance
    int CACHE_MAINTENANCE_PERIOD = 0;
    int FREQUENCY_THRESHOLD=20;
    int TIME_THRESHOLD = 2800;
    int CACHE_MAINTENANCE_DURATION = 15;
    int number_addedQueries = 0;
    int number_deletedQueries = 0;
    int number_cacheAccesses = 0;
    int number_cacheHits = 0;


    //temporary variables


    double totalDataFound=0;
    double totalDataNotFound=0;
    double averageDataFound=0;
    double averageDataNotFound=0.0;

    public CommunityCacheMain_LRU(){

        //set user containers
        for (int i = 0; i < no_of_userContainers; i++) {
            String container_name = "Container_"+(i+1) ;
            user_containers.add(new Container(container_name));

        }

        //start data servers

        for (int i = 0; i < no_of_DataServers ; i++) {
            dataServers[i] = new DatabaseServer("db"+(i+1));
            dataServers[i].setAddress_container("Container_"+(i+999));

            /*int randomInt = new Random().nextInt(user_containers.size());
            // try to set one data server per container only
            ArrayList<String> usedContainers = new ArrayList<>();
            String testString = "Container_"+randomInt;
            if (!usedContainers.contains(testString))
            dataServers[i].setAddress_container(testString);
            */
        }


    }
    public CommunityCacheMain_LRU(int noOfQueries) {
        this.noOfQueries = noOfQueries;

        //set user containers
        for (int i = 0; i < no_of_userContainers; i++) {
            String container_name = "Container_"+(i+1) ;
            user_containers.add(new Container(container_name));
        }

        //start data servers

        for (int i = 0; i < no_of_DataServers ; i++) {
            dataServers[i] = new DatabaseServer("db"+(i+1));
            dataServers[i].setAddress_container("Container_"+(i+999));
            /*int randomInt = new Random().nextInt(user_containers.size());
            // try to set one data server per container only
            ArrayList<String> usedContainers = new ArrayList<>();
            String testString = "Container_"+randomInt;
            if (!usedContainers.contains(testString))
            dataServers[i].setAddress_container(testString);
            */
        }
    }

    public int getCacheMaintenanceStartTime() {
        return cacheMaintenanceStartTime;
    }

    public void setCacheMaintenanceStartTime(int cacheMaintenanceStartTime) {
        this.cacheMaintenanceStartTime = cacheMaintenanceStartTime;
    }

    public int getGetCacheMaintenanceEndTime() {
        return getCacheMaintenanceEndTime;
    }

    public void setGetCacheMaintenanceEndTime(int getCacheMaintenanceEndTime) {
        this.getCacheMaintenanceEndTime = getCacheMaintenanceEndTime;
    }

    public int getNoOfQueriesPresentOnNetwork() {
        return noOfQueriesPresentOnNetwork;
    }

    public void setNoOfQueriesPresentOnNetwork(int noOfQueriesPresentOnNetwork) {
        this.noOfQueriesPresentOnNetwork = noOfQueriesPresentOnNetwork;
    }

    public int getNoOfQueries() {
        return noOfQueries;
    }

    public void setNoOfQueries(int noOfQueries) {
        this.noOfQueries = noOfQueries;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public  ArrayList<Query_Response> getQuery_responseTime() {
        return query_responseTime;
    }

    public void setQuery_responseTime(ArrayList<Query_Response> query_responseTime) {
        this.query_responseTime = query_responseTime;
    }

    public ArrayList<Container> getUser_containers() {
        return user_containers;
    }

    public void setUser_containers(ArrayList<Container> user_containers) {
        this.user_containers = user_containers;
    }

    /**
     * This method is a general method to read query expressions from the given input file and creates a query
     * This method is used by all other methods above
     * @param QueryExpressionFile
     * @return
     * @throws java.io.IOException
     */
    private static List readQueryExpressions (File QueryExpressionFile) throws IOException {
        List queries = new jade.util.leap.ArrayList();

        BufferedReader in = new BufferedReader(new FileReader(QueryExpressionFile));
        while(true){
            String str = in.readLine();
            if(str == null) break;
            Query_Subj_Predicate query = new Query_Subj_Predicate(str);
            queries.add(query);
        }

        return queries;
    }

    private static ArrayList<String> readQueryExpressions1(File QueryExpressionFile) throws IOException{
        ArrayList<String> queries = new ArrayList<>();

        BufferedReader in = new BufferedReader(new FileReader(QueryExpressionFile));
        while(true){
            String str = in.readLine();
            if(str == null) break;

            queries.add(str);
        }

        return queries;
    }

    /**
     * This method is to generate queries in distributions and create queries according to the distribution
     * No.of queries to be generated can be set here
     * This method creates QueryAgent_NoUAs in containers other than database servers
     * @param timeDistribution
     * @param queryDistribution
     * @throws IOException
     */
    private  void  startTime_Query_Distributions(int noOfQueries,String INPUT_FILE, String timeDistribution,String queryDistribution, int fixedTime_constant) throws IOException {

        ArrayList<String> queries = null;
        ArrayList queryList = new ArrayList(noOfQueries);
        query_responseTime = new ArrayList<>();

        // queries are sent as an expression from query agents
//        File queryExpressionFile = new File(
//                ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions1");
        File queryExpressionFile = new File( INPUT_FILE);

        BufferedWriter out = new BufferedWriter(new FileWriter(GENERATED_FILE));

        try {
            queries = readQueryExpressions1(queryExpressionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int time_type = 0;

        if (timeDistribution.equalsIgnoreCase("RanDom")) time_type = 1;
        else if (timeDistribution.equalsIgnoreCase("Poisson")) time_type = 2;
        else  if (timeDistribution.equalsIgnoreCase("Uniform")) time_type = 3;
        else if (timeDistribution.equalsIgnoreCase("Exponential")) time_type = 4;
        else if (timeDistribution.equalsIgnoreCase("Fixed")) time_type = 5;

        switch (time_type){
            case 1:{

                Random randomGenerator = new Random();

                for (int i=0; i<noOfQueries; i++)
                {
                    Query_Response qr = new Query_Response();
                    int randomInt = randomGenerator.nextInt(noOfQueries-1);
                    qr.setStartTime(randomInt+1); //to avoid random value zero
                    query_responseTime.add(qr);
                }

                break;
            }
            case 2:{
                    /*
                     * select a suitable value for mean
                     */
               /* System.out.println("Give a value for poisson mean ");
                Scanner sc = new Scanner(System.in);
                double mean = sc.nextDouble();
                */


                for (int i=0; i<noOfQueries; i++)
                {
                    // int poisson_next = poisson(mean);
                    PoissonDistribution p = new PoissonDistribution(POISSON_MEAN_TIME);

                    // System.out.println("poisson_next = "+poisson_next);
                    int randomInt = p.sample();
                    Query_Response qr = new Query_Response();
                    qr.setStartTime(randomInt+1);//to avoid random value zero
                    query_responseTime.add(qr);
                }

                break;

            }
            case 3:{
                    /*
                     * select a suitable value for lower and upper boundaries
                     * Here 2, 6  which uniform function will generate queries.
                     */
               /* System.out.println("Give a value for uniform mean ");
                Scanner sc = new Scanner(System.in);
                int lower = sc.nextInt();
                sc = new Scanner(System.in);
                int upper = sc.nextInt();

                */


                for (int i=0; i<noOfQueries; i++)  {
                    // int poisson_next = poisson(mean);
                    UniformIntegerDistribution u = new UniformIntegerDistribution(UNIFORM_LOWER_TIME, UNIFORM_UPPER_TIME);
                    int randomInt = u.sample();
                    Query_Response qr = new Query_Response();
                    qr.setStartTime(randomInt+1);//to avoid random value zero
                    query_responseTime.add(qr);

                }

                break;
            }

            case 4: {
                 /* System.out.println("Give a value for uniform mean ");
                Scanner sc = new Scanner(System.in);
                int lower = sc.nextInt();
                sc = new Scanner(System.in);
                int upper = sc.nextInt();

                */


                for (int i=0; i<noOfQueries; i++)  {
                    // int poisson_next = poisson(mean);
                    ExponentialDistribution e = new ExponentialDistribution(EXPONENTIAL_MEAN_TIME);
                    double randomDouble = e.sample();
                    Query_Response qr = new Query_Response();
                    qr.setStartTime(new Double(randomDouble).intValue()+1);//to avoid random value zero
                    query_responseTime.add(qr);

                }


                break;
            }

            case 5: { // for standard queries and testing purpose
                int t = 1;
                for (int i = 0; i < noOfQueries; i++, t +=fixedTime_constant) {
                    Query_Response qr = new Query_Response();
                    // qr.setStartTime(i+fixedTime_constant);//to avoid  zero

                    qr.setStartTime(t);
                    query_responseTime.add(qr);
                }

                break;
            }
            default : break;
        }
/**
 * below code is for the repetition of queries based on a distribution
 */
        int query_type = 0;

        if (queryDistribution.equalsIgnoreCase("RanDom")) query_type = 1;
        else if (queryDistribution.equalsIgnoreCase("Poisson")) query_type = 2;
        else  if (queryDistribution.equalsIgnoreCase("Uniform")) query_type = 3;
        else  if (queryDistribution.equalsIgnoreCase("Exponential")) query_type = 4;
        else if (queryDistribution.equalsIgnoreCase("Fixed")) query_type = 5;

        switch (query_type)
        {
            case 1:{

                Random randomGenerator = new Random();

                for (int i=0; i<noOfQueries; i++)
                {
                    int randomInt = randomGenerator.nextInt(NUMBER_OF_QUERIES_IN_FILE-1);
                    query_responseTime.get(i).setQuery(randomInt);

                    query_responseTime.get(i).setQSP(new Query_Subj_Predicate(queries.get(randomInt)));
                    queryList.add(queries.get(randomInt));
                    out.write(queries.get(randomInt));
                    out.write("\n");
                }
                out.close();
                break;
            }
            case 2:{
                    /*
                     * select a suitable value for mean
                     */
               /* System.out.println("Give a value for poisson mean ");
                Scanner sc = new Scanner(System.in);
                double mean = sc.nextDouble();
                */


                for (int i=0; i<noOfQueries; i++)
                {
                    // int poisson_next = poisson(mean);
                    PoissonDistribution p = new PoissonDistribution(POISSON_MEAN_QUERY,38.4);//(POISSON_MEAN_QUERY);

                    // System.out.println("poisson_next = "+poisson_next);
                    int randomInt = p.sample();
                    query_responseTime.get(i).setQuery(randomInt);
                    query_responseTime.get(i).setQSP(new Query_Subj_Predicate(queries.get(randomInt)));
                    queryList.add(queries.get(randomInt));
                    out.write(queries.get(randomInt));
                    out.write("\n");
                }

                out.close();
                break;

            }
            case 3:{

                /**
                 System.out.println("Give a value for uniform mean ");
                 Scanner sc = new Scanner(System.in);
                 int lower = sc.nextInt();
                 sc = new Scanner(System.in);
                 int upper = sc.nextInt();

                 */

                for (int i=0; i<noOfQueries; i++)
                {

                    UniformIntegerDistribution u = new UniformIntegerDistribution(UNIFORM_LOWER_QUERY,UNIFORM_UPPER_QUERY);
                    int randomInt = u.sample();
                    query_responseTime.get(i).setQuery(randomInt);
                    query_responseTime.get(i).setQSP(new Query_Subj_Predicate(queries.get(randomInt)));
                    queryList.add(queries.get(randomInt));
                    out.write(queries.get(randomInt));
                    out.write("\n");

                }
                out.close();
                break;
            }

            case 4:{

                /**
                 System.out.println("Give a value for uniform mean ");
                 Scanner sc = new Scanner(System.in);
                 int lower = sc.nextInt();
                 sc = new Scanner(System.in);
                 int upper = sc.nextInt();

                 */

                for (int i=0; i<noOfQueries; i++)
                {

                    ExponentialDistribution e = new ExponentialDistribution(EXPONENTIAL_MEAN_QUERY);
                    Double randomDouble = e.sample();
                    int randomInt = randomDouble.intValue();
                    query_responseTime.get(i).setQuery(randomInt);
                    query_responseTime.get(i).setQSP(new Query_Subj_Predicate(queries.get(randomInt)));
                    queryList.add(queries.get(randomInt));
                    out.write(queries.get(randomInt));
                    out.write("\n");

                }
                out.close();
                break;
            }

            case 5: { // for standard queries and testing purpose

                for (int i = 0; i < noOfQueries; i++) {
                    query_responseTime.get(i).setQuery(i);
                    int queryNo = i%NUMBER_OF_QUERIES_IN_FILE;
                    query_responseTime.get(i).setQSP(new Query_Subj_Predicate(queries.get(queryNo)));
                    queryList.add(queryNo);
                    out.write(queryNo);
                    out.write("\n");
                }
                out.close();
                break;
            }
            default : {
//                System.out.println("not matching with any selection");
                break;
            }
        }

        Collections.sort(query_responseTime);

    }// End of method

    // CHECKING STATES BELOW

    /**
     * Below are the states used in sub-query caching
     * @param ticks
     */
    public  void checkState1(int ticks)  {
        int noOfQueries = query_responseTime.size();
        // System.out.println(" from CLM no problem");

        for(int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);

            if (nextQuery.getStatus().equals("State1")) {

                int nextQueryTime = query_responseTime.get(queryCounter).getStartTime();
                int check_cacheMaintenance= nextQueryTime%CACHE_MAINTENANCE_PERIOD;


//                if ((nextQueryTime == ticks)||(check_cacheMaintenance>=0 && check_cacheMaintenance<=5)) {
                if (nextQueryTime == ticks || (nextQueryTime>= getCacheMaintenanceStartTime() && nextQueryTime <= getCacheMaintenanceEndTime)) {
                    //   System.out.println(queryCounter+") start time "+ ticks);
                    //open the state 1 of query to be entered

                    StartState_State1 state1 = new StartState_State1();

                    state1.setExpressionTree(new ExpressionTree(nextQuery.getQSP()));
                    state1.getExpressionTree().setTime_queried(ticks);
                    //   System.out.println(" Line 513 "+queryCounter+" "+ state1.getExpressionTree().getTime_queried());
                    state1.setStartTime(ticks);
                    nextQuery.setState1(state1); // setting the start time. this is  ideally equal to the start time
                    //converting the expression into expressionTree

                    nextQuery.setStatus("State1: done");
                    nextQuery.getState1().setState1EndTime(ticks);

                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 1 start time " +
                            nextQuery.getState1().getStartTime() + " end time " + nextQuery.getState1().getState1EndTime());
//                    System.out.println(queryCounter+") "+nextQuery.getQSP().getQueryID()+" STATE 1 start time "+
//                            nextQuery.getState1().getStartTime()+" end time "+ nextQuery.getState1().getState1EndTime());

                }
            }
        }  //for loop
    }   //end method

    /**
     * This method is to check State2
     * @param ticks
     */
    public  void checkState2(int ticks){
        int noOfQueries = query_responseTime.size();


        for (int queryCounter = 0;  queryCounter < noOfQueries ;  queryCounter++) {
            Query_Response nextQuery = query_responseTime.get( queryCounter);

            if ((nextQuery.getStatus().equals("State1: done")) ||(nextQuery.getStatus().startsWith("State2:"))) {


                //if query just entered into state2
                if (nextQuery.getStatus().equals("State1: done") && (nextQuery.getState1().getState1EndTime() < ticks
                )) {
                    nextQuery.setState2(new QuerySentOnLAN_State2());
                    nextQuery.setStatus(nextQuery.getState2().getStatus()); // sets with the status of state 2
                    double loadFactor;

                    if (noOfQueries > LAN_QUERY_LOAD_LIMIT) loadFactor = LAN_HIGH_LOADFACTOR;
                    else loadFactor =  LAN_LOW_LOADFACTOR;
                    double time = networkTopography.getLAN_Delay(loadFactor, 0.001);

                    double no_ticks_needed = Math.ceil(time / 1000);
                    nextQuery.getState2().setTimeRequiredAtLAN(no_ticks_needed); // 10 milli seconds is considered to be one tick
                    nextQuery.getState2().setStartTimeState2(ticks);
                    nextQuery.getState2().setRemainingTime(no_ticks_needed);
                    nextQuery.setStatus("State2: LAN Transit");

                    nextQuery.getState2().setRemainingTime(nextQuery.getState2().getRemainingTime() - 1);

                } else if (nextQuery.getStatus().equals("State2: LAN Transit")) {

                    if (nextQuery.getState2().getRemainingTime() == 1) {
                        nextQuery.getState2().setRemainingTime(0);
                        nextQuery.setStatus("State2: done");
                        nextQuery.getState2().setEndTimeState2(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 2 start time " +
                                nextQuery.getState2().getStartTimeState2() + " end time " + nextQuery.getState2().getEndTimeState2());

                    }
                    else  nextQuery.getState2().setRemainingTime(nextQuery.getState2().getRemainingTime() - 1);
                }
            }
        }// State2 ends
    }

    /**
     * This method is to process cache
     * 3.1 Searching Query index
     * 3.2 Cache data location
     * 3.3 Cache retrieval and send time
     * @param ticks
     */
    public void checkState3(int ticks, String criteria){
        int noOfQueries = query_responseTime.size();
        double queryInterArrivalTime = query_responseTime.get(noOfQueries-1).getStartTime()/(double)noOfQueries; //average arrival time

        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++){
            Query_Response nextQuery =  query_responseTime.get( queryCounter);


            if ((nextQuery.getStatus().equals("State2: done")) ||(nextQuery.getStatus().startsWith("State3:"))) {

                if (nextQuery.getStatus().equals("State2: done")&& (nextQuery.getState2().getEndTimeState2() < ticks)) {

                    CacheProcessing_State3 cps3 = new CacheProcessing_State3(ticks);
                    cps3.waitingTimeInQueue(queryInterArrivalTime);
                    double no_ticks_needed = cps3.timeSpentInState3(queryInterArrivalTime);
                    cps3.setRemainingTime(new Double(no_ticks_needed).intValue());
                    cps3.setStartTimeState3(ticks);
                    nextQuery.setState3(cps3);

                    // this step should be modified to get the address
                    QueryIndex queryIndex  = this.user_containers.get(1).getQueryIndex();

                    queryIndex.searchQueryIndex(nextQuery.getState1().getExpressionTree());

                    boolean queryReturned= false;

                    while(!queryReturned ) {
                        ArrayList<Cache_Reply> receivedList = queryIndex.sendQueryBack();
                        Cache_Reply tempCacheReply = null ;
                        //check whether the received list contains this query
                        for (Cache_Reply cr : receivedList) {

                            if (nextQuery.getQSP().getQueryID().equals(cr.getQueryID())) {
                                queryReturned = true;
                                tempCacheReply = cr;
                                queryIndex.removeQueryFromReturnQueue(cr);
                                break;
                            }
                        }

                        if (tempCacheReply != null) {

                            log.info(queryCounter + ") status " + tempCacheReply.getReplyStatus());

                            if (tempCacheReply.getReplyStatus().equals(FULLY_FOUND)){
                                nextQuery.setFoundInCache(true);
                                log.info("  from clm class data size found fully found " + tempCacheReply.getDataSizeFound());
                                nextQuery.setRemainingQueryFull(tempCacheReply.getPartialExpressionTree());

                                log.info(queryCounter + ") " + nextQuery.getDataSizeFound() + " " + nextQuery.getRemaingDataNeeded());
                            }
                            else if (tempCacheReply.getReplyStatus().equals(NOT_FOUND)){

                                log.info(queryCounter + ") in not found case");
                                nextQuery.setRemainingQueryPartial(tempCacheReply.getPartialExpressionTree());
                                nextQuery.setFoundInCache(false);
                            }
                            else if (tempCacheReply.getReplyStatus().equals(PARTIALLY_FOUND)) {
                                log.info(queryCounter + ") in Partial found case");
                                log.info("  from clm class data size found " + tempCacheReply.getDataSizeFound
                                        ());
                                nextQuery.setFoundInCache(false);

                                nextQuery.setRemainingQueryPartial(tempCacheReply.getPartialExpressionTree());

                                log.info(queryCounter + ") " + nextQuery.getDataSizeFound() + " " + nextQuery.getRemaingDataNeeded());
                            }
                        }

                    }
                    nextQuery.setStatus("State3: Cache Processing");
                    nextQuery.getState3().setRemainingTime(nextQuery.getState3().getRemainingTime() - 1);

                    if (nextQuery.getState3().getRemainingTime()  == 0) {

                        nextQuery.getState3().setDataFound(nextQuery.getDataSizeFound());
                        nextQuery.getState3().setRemainingTime(0);
                        nextQuery.setStatus("State3: done");
                        nextQuery.getState3().setEndTimeState3(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 3 start time " +
                                nextQuery.getState3().getStartTimeState3() + " end time " + nextQuery.getState3().getEndTimeState3());
                    }

                }
                else if (nextQuery.getStatus().equals("State3: Cache Processing")) {

                    if (nextQuery.getState3().getRemainingTime() == 1) {

                        nextQuery.getState3().setDataFound(nextQuery.getDataSizeFound());

                        nextQuery.getState2().setRemainingTime(0);
                        nextQuery.setStatus("State3: done");
                        nextQuery.getState3().setEndTimeState3(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 3 start time " +
                                nextQuery.getState3().getStartTimeState3() + " end time " + nextQuery.getState3().getEndTimeState3());

                    }
                }
                else if (nextQuery.getStatus().equals("State3: About to finish")) {

                    nextQuery.getState3().setDataFound(nextQuery.getDataSizeFound());
                    nextQuery.getState2().setRemainingTime(0);
                    nextQuery.setStatus("State3: done");
                    nextQuery.getState3().setEndTimeState3(ticks);

                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 3 start time " +
                            nextQuery.getState3().getStartTimeState3() + " end time " + nextQuery.getState3().getEndTimeState3());
                }

            }
        }//for loop

    } //State 3 ends

    /**
     * This method is to send Cache results on LAN
     * @param ticks
     */
    public void checkState4(int ticks){
        int noOfQueries = query_responseTime.size();
        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++){
            Query_Response nextQuery =  query_responseTime.get( queryCounter);

            if ((nextQuery.getStatus().equals("State3: done")) ||(nextQuery.getStatus().startsWith("State4:"))) {

                if (nextQuery.getStatus().equals("State3: done")&& (nextQuery.getState3().getEndTimeState3() < ticks)) {
                    nextQuery.setState4(new CacheResultOnLAN_State4());
                    // ready to send results back
                    nextQuery.getState4().setStartTimeState4(ticks);// state4 started
                    double loadFactor;

                    double datasizeInDB = nextQuery.getState3().getDataFound();
                    if ((noOfQueries > LAN_QUERY_LOAD_LIMIT) || (datasizeInDB >= LAN_HEAVYLOAD))
                        loadFactor = LAN_HIGH_LOADFACTOR;
                    else loadFactor = LAN_LOW_LOADFACTOR;

                    double noOfTicksNeeded = (int) Math.ceil(networkTopography.getLAN_Delay(loadFactor,
                            datasizeInDB) / 1000);

                    nextQuery.getState4().setDataTransferTimeLAN(noOfTicksNeeded);
                    nextQuery.getState4().setRemainingTime(noOfTicksNeeded);
                    nextQuery.setStatus("State4: Started transferring Data from cache");
                    nextQuery.getState4().setRemainingTime(nextQuery.getState4().getRemainingTime() - 1);

                    if (nextQuery.getState4().getRemainingTime()==0) {
                        nextQuery.getState4().setRemainingTime(0);
                        nextQuery.setStatus("State4: done");
                        nextQuery.getState4().setEndTimeState4(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 4 start time " +
                                nextQuery.getState4().getStartTimeState4() + " end time " + nextQuery.getState4().getEndTimeState4());
                    }

                }

                else if (nextQuery.getStatus().equals("State4: Started transferring Data from cache")) {

                    if (nextQuery.getState4().getRemainingTime()==1){
                        nextQuery.getState4().setRemainingTime(0);
                        nextQuery.setStatus("State4: done");
                        nextQuery.getState4().setEndTimeState4(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 4 start time " +
                                nextQuery.getState4().getStartTimeState4() + " end time " + nextQuery.getState4().getEndTimeState4());
                    }
                    else nextQuery.getState4().setRemainingTime(nextQuery.getState4().getRemainingTime() - 1);
                }
            }

        } //for

    }  //State 4 ends

    public void checkState5(int ticks){
        int noOfQueries = query_responseTime.size();
        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);

            /**
             * Checking the part of query answered
             * estimate the remaining part to be sent to DataServers
             * If, the entire query is answered at cache, remove the query.Query id "Done"
             */

            if ((nextQuery.getStatus().equals("State4: done")) ||(nextQuery.getStatus().startsWith("State5:"))) {

                if (nextQuery.getStatus().equals("State4: done") && (nextQuery.getState4().getEndTimeState4() < ticks)) {

                    nextQuery.setState5(new RemainingQueryExecution_State5());

                    nextQuery.getState5().setStartTimeState5(ticks);

                    if (!nextQuery.isFoundInCache()) {
                        nextQuery.getState5().setRemainingTree(nextQuery.getRemainingQuery());
                        nextQuery.getState5().setRemainingDataNeeded(nextQuery.getRemaingDataNeeded());
                        nextQuery.setStatus("State5: done");
                        nextQuery.getState5().setEndTimeState5(ticks);
                    }
                    else{
//                        System.out.println(queryCounter        + " Query completed");
                        log.info(queryCounter + ") Query completed")  ;
                        nextQuery.setStatus("Query completed");
                        nextQuery.getState5().setRemainingDataNeeded(0);
                        nextQuery.getState5().setEndTimeState5(ticks);
                        nextQuery.setEndTime(ticks);
                        queriesDone++;
                        log.info("no of queries done: " + queriesDone);
                        // System.out.println("Query done at state 5 only query counter "+queryCounter+"__"+ ticks);
                    }
                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 5 start time " +
                            nextQuery.getState5().getStartTimeState5() + " end time " + nextQuery.getState5().getEndTimeState5());
//                    System.out.println((queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 5 start time " + nextQuery.getState5().getStartTimeState5() + " end time " + nextQuery.getState5().getEndTimeState5()));
                }
            }

        } //for all queries

    }  //end of method state5

    public void checkState6(int ticks){
        int noOfQueries = query_responseTime.size();
        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);

            if ((nextQuery.getStatus().equals("State5: done")) ||(nextQuery.getStatus().startsWith("State6:"))) {

                if (nextQuery.getStatus().equals("State5: done") && (nextQuery.getState5().getEndTimeState5() < ticks)) {
                    nextQuery.setState6(new QueryTransferOnWAN_State6());
                    // ready to send results back
                    nextQuery.getState6().setStartTimeState6(ticks);// state4 started
                    double loadFactor;

                    double querySizeInDB = nextQuery.getState5().getRemainingTree().getNo_of_Nodes();
                    if ((noOfQueries > WAN_QUERY_LOAD_LIMIT) || (querySizeInDB> WAN_HEAVYLOAD))
                        loadFactor = WAN_HIGH_LOADFACTOR;
                    else loadFactor = WAN_LOW_LOADFACTOR;


                    double noOfTicksNeeded = (int) Math.ceil(networkTopography.getWAN_Delay(loadFactor,
                            querySizeInDB) / 1000);

                    nextQuery.getState6().setQueryTransferTimeWAN(noOfTicksNeeded);

                    nextQuery.getState6().setRemainingTime(noOfTicksNeeded);
                    nextQuery.setStatus("State6: Remaining query being sent to DataServers");
                    nextQuery.getState6().setRemainingTime(nextQuery.getState6().getRemainingTime() - 1);
                }

                else if (nextQuery.getStatus().equals("State6: Remaining query being sent to DataServers")) {

                    if (nextQuery.getState6().getRemainingTime() == 1) {

                        nextQuery.getState6().setRemainingTime(0);
                        nextQuery.setStatus("State6: done");
                        nextQuery.getState6().setEndTimeState6(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 6 start time " +
                                nextQuery.getState6().getStartTimeState6() + " end time " + nextQuery.getState6().getEndTimeState6());
//                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 6 start time " +
//                                nextQuery.getState6().getStartTimeState6() + " end time " + nextQuery.getState6().getEndTimeState6());
                    }
                    else nextQuery.getState6().setRemainingTime(nextQuery.getState6().getRemainingTime() - 1);

                }

            }
        }

    }

    public void checkState7(int ticks) throws InterruptedException {
        int noOfQueries = query_responseTime.size();


        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);

            // 7.1 data server waiting
            //7.2 data server processing
            if ((nextQuery.getStatus().equals("State6: done")) ||(nextQuery.getStatus().startsWith("State7:"))) {

                if (nextQuery.getStatus().equals("State6: done")&& (nextQuery.getState6().getEndTimeState6() < ticks)) {

                    DataServer_State7 dataServer_state7 = new DataServer_State7();
                    dataServer_state7.setStartTimeState7(ticks);

                    //  nextQuery.setRemainingQuery(nextQuery.getState5().getRemainingTree());

                    //following arraylist - querySegments is all querySegments from one query

                    ArrayList<SubjectQuerySegment> querySegments = dataServer_state7.getRemainingQuerySegments(nextQuery.getRemainingQuery());
                    ArrayList<QuerySegment_ExecutionTime> sentList = new ArrayList<>();
                    for(SubjectQuerySegment sqs: querySegments){
                        QuerySegment_ExecutionTime q = new QuerySegment_ExecutionTime(sqs);
                        sentList.add(q);
                    }

                    nextQuery.setState7(dataServer_state7);
                    nextQuery.setStatus("State7: Started");

                    for (int i = 0; i < querySegments.size(); i++) {

                        SubjectQuerySegment querySegment = querySegments.get(i);
                        querySegment.setQueryID(nextQuery.getQSP().getQueryID()); // sets the identifier for later identification
                        // All segments carry the same ID of the QSP

                        String databaseName = querySegment.getDatabase();
                        int databaseNumber = Integer.parseInt(databaseName.replaceAll("[a-z]",""));

                        DatabaseServer dataServer = dataServers[databaseNumber-1];


                        QuerySegment_ExecutionTime qset = new QuerySegment_ExecutionTime(querySegment);
                        dataServer.receiveQuery(qset);

                    }

                    Thread.sleep(10);

                    boolean queryDone= false;

                    int no_of_processed_segments = 0;
                    int sentListSize = sentList.size();
                    double maxTimeLimit =0;



                    while (no_of_processed_segments != sentListSize){ // this loop is the simulation to make sure all segments that are processed later are also covered

                        for (int i = 0; i < dataServers.length; i++) {

                            ArrayList<QuerySegment_ExecutionTime> toRemove = new ArrayList<>(); // array of elements to be removed
                            // from this data server's return list

                            ArrayList<QuerySegment_ExecutionTime> qsets = dataServers[i].sendQueryBack();

                            if(qsets!=null) {
                                for (QuerySegment_ExecutionTime q : qsets) {
                                    if (sentList.contains(q)) {

                                        no_of_processed_segments++;
                                        toRemove.add(q);
                                        if (q.getExecutionTime() > maxTimeLimit) {
                                            maxTimeLimit = q.getExecutionTime();
                                        }

                                        sentList.remove(q);// remove from the local list
                                    }
                                    if (no_of_processed_segments == sentListSize) break;
                                }
                                dataServers[i].removeQueriesFromReturnQueue(toRemove);
                            }

                            if (no_of_processed_segments == sentListSize) break;
                        }

                    }

                    nextQuery.getState7().setTimeSpentInState7(Math.ceil(maxTimeLimit));
                    nextQuery.getState7().setRemainingTimeState7(Math.ceil(maxTimeLimit));
                    //TODO: data size
                    nextQuery.getState7().setDataSize(nextQuery.getState5().getRemainingDataNeeded());

                    nextQuery.setStatus("State7: Data server(s) processing");
                    nextQuery.getState7().setRemainingTimeState7(nextQuery.getState7().getRemainingTimeState7()-1);

                    if (nextQuery.getState7().getRemainingTimeState7() <= 1) {
                        nextQuery.getState7().setRemainingTimeState7(0);
                        nextQuery.setStatus("State7: done");
                        nextQuery.getState7().setEndTimeState7(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 7 start time " +
                                nextQuery.getState7().getStartTimeState7() + " end time " + nextQuery.getState7().getEndTimeState7());
//                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 7 start time " +
//                                nextQuery.getState7().getStartTimeState7() + " end time " + nextQuery.getState7().getEndTimeState7());

                    }
                }

                else if (nextQuery.getStatus().equals("State7: Data server(s) processing")) {

                    if (nextQuery.getState7().getRemainingTimeState7() <=1) {
                        nextQuery.getState7().setRemainingTimeState7(0);
                        nextQuery.setStatus("State7: done");
                        nextQuery.getState7().setEndTimeState7(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 7 start time " +
                                nextQuery.getState7().getStartTimeState7() + " end time " + nextQuery.getState7().getEndTimeState7());
//                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 7 start time " +
//                                nextQuery.getState7().getStartTimeState7() + " end time " + nextQuery.getState7().getEndTimeState7());

                    }

                    else nextQuery.getState7().setRemainingTimeState7(nextQuery.getState7().getRemainingTimeState7() - 1);

                }

            }

        }

    }   //check status 7

    public void checkState8(int ticks){
        int noOfQueries = query_responseTime.size();
        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);

            if ((nextQuery.getStatus().equals("State7: done")) ||(nextQuery.getStatus().startsWith("State8:"))) {

                if (nextQuery.getStatus().equals("State7: done")&& (nextQuery.getState7().getEndTimeState7() < ticks)) {
                    nextQuery.setState8(new DataTransferOnWAN_State8());
                    // ready to send results back
                    nextQuery.getState8().setStartTimeState8(ticks);// state8 started
                    double loadFactor;

                    double datasizeInDB = nextQuery.getState7().getDataSize();
                    if ((noOfQueries > WAN_QUERY_LOAD_LIMIT) || (datasizeInDB > WAN_HEAVYLOAD))
                        loadFactor = WAN_HIGH_LOADFACTOR;
                    else loadFactor = WAN_LOW_LOADFACTOR;


                    double noOfTicksNeeded = (int) Math.ceil(networkTopography.getWAN_Delay(loadFactor,
                            datasizeInDB) / 1000);

                    nextQuery.getState8().setDataTransferTimeWAN(noOfTicksNeeded);
                    nextQuery.getState8().setRemainingTime(noOfTicksNeeded);
                    nextQuery.setStatus("State8: Started transferring Data from Data server");
                    nextQuery.getState8().setRemainingTime(nextQuery.getState8().getRemainingTime() - 1);

                    if (nextQuery.getState8().getRemainingTime() <= 1) {
                        nextQuery.getState8().setRemainingTime(0);
                        nextQuery.setStatus("State8: done");
                        nextQuery.getState8().setEndTimeState8(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 8 start time " +
                                nextQuery.getState8().getStartTimeState8() + " end time " + nextQuery.getState8().getEndTimeState8());
//                        System.out.println((queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 8 start time " +
//                                nextQuery.getState8().getStartTimeState8() + " end time " + nextQuery.getState8()
//                                .getEndTimeState8()));

                    }

                }
                else if (nextQuery.getStatus().equals("State8: Started transferring Data from Data server")) {

                    if (nextQuery.getState8().getRemainingTime() <= 1) {

                        nextQuery.getState8().setRemainingTime(0);
                        nextQuery.setStatus("State8: done");
                        nextQuery.getState8().setEndTimeState8(ticks);

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 8 start time " +
                                nextQuery.getState8().getStartTimeState8() + " end time " + nextQuery.getState8().getEndTimeState8());
//                        System.out.println((queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 8 start time " +
//                                nextQuery.getState8().getStartTimeState8() + " end time " + nextQuery.getState8()
//                                .getEndTimeState8()));
                    }

                    else nextQuery.getState8().setRemainingTime(nextQuery.getState8().getRemainingTime() - 1);

                }

            }
        }//for

    } //   check Status 8

    public void checkState9(int ticks){
        int noOfQueries = query_responseTime.size();
        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);


            if ((nextQuery.getStatus().equals("State8: done")) ||(nextQuery.getStatus().startsWith("State9:"))) {

                if (nextQuery.getStatus().equals("State8: done")&& (nextQuery.getState8().getEndTimeState8() < ticks)) {

                    nextQuery.setState9(new ResultAggregation_State9());
                    nextQuery.getState9().setStartTimeState9(ticks);
                    nextQuery.getState9().setDataAggregationTime(2); //  TODO: Calculate this
                    nextQuery.getState9().setRemainingTime(nextQuery.getState9().getDataAggregationTime());
                    nextQuery.setStatus("State9: Aggregating results");

                    nextQuery.getState9().setRemainingTime(nextQuery.getState9().getRemainingTime() - 1);

                    if (nextQuery.getState9().getRemainingTime() <= 1) {
                        nextQuery.getState9().setRemainingTime(0);
                        nextQuery.setStatus("State9: done");
                        nextQuery.getState9().setEndTimeState9(ticks);

                        queriesDone++;
                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 9 start time " +
                                nextQuery.getState9().getStartTimeState9() + " end time " + nextQuery.getState9().getEndTimeState9());
//                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 9 start time " +
//                                nextQuery.getState9().getStartTimeState9() + " end time " + nextQuery.getState9().getEndTimeState9());
                        //   System.out.println("queries done - query Counter " + queryCounter+" "+queriesDone);

                        nextQuery.setEndTime(ticks);
                        nextQuery.setStatus("Query completed");


                    }
                }
                else if (nextQuery.getStatus().equals("State9: Aggregating results")) {

                    if (nextQuery.getState9().getRemainingTime() <= 1) {
                        nextQuery.getState9().setRemainingTime(0);
                        nextQuery.setStatus("State9: done");
                        nextQuery.getState9().setEndTimeState9(ticks);

                        queriesDone++;

                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 9 start time " +
                                nextQuery.getState9().getStartTimeState9() + " end time " + nextQuery.getState9().getEndTimeState9());
//                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 9 start time " +
//                                nextQuery.getState9().getStartTimeState9() + " end time " + nextQuery.getState9().getEndTimeState9());
                        //   System.out.println("queries done - query Counter" + queriesDone);

                        nextQuery.setEndTime(ticks);
                        nextQuery.setStatus("Query completed");

                    }
                    else nextQuery.getState9().setRemainingTime(nextQuery.getState9().getRemainingTime() - 1);

                }

            }

        }//  for



    }  //end method State 9

    public double runQueriesThroughStates(int NUMBER_OF_QUERIES, String criteria, String maintenanceCriteria) throws InterruptedException, IOException {

        double total_responseTime=0;
        String fileOut = null;

        File outFile11 = null;
        FileWriter writer1 = null;

        // File handling
        switch(maintenanceCriteria){
            case TIME_THRESHOLD_ONLY: {
                fileOut = "//home//santhilata//Dropbox//ImprovedCachelearningResults//ADASS//Cache_stability_TIME_"+TIME_THRESHOLD+"_"+NUMBER_OF_QUERIES+"_"+criteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries, Time Threshold, no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            case FREQUENCY_THRESHOLD_ONLY :{
                fileOut = "//home//santhilata//Dropbox//ImprovedCachelearningResults//ADASS//Cache_stability_FREQUENCY_"+FREQUENCY_THRESHOLD+"_"+NUMBER_OF_QUERIES+"_"+criteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries, Frequency Threshold, no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            case COMBINED_ALGO_MAINTENANCE: {
                fileOut = "//home//santhilata//Dropbox//ImprovedCachelearningResults//ADASS//Cache_stability_COMBINED_"+TIME_THRESHOLD+"_"+FREQUENCY_THRESHOLD+"_"+NUMBER_OF_QUERIES+"_"+criteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries, Time Threshold, Frequency Threshold, no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            case LRU: {
                fileOut = "//home//santhilata//Dropbox//ImprovedCachelearningResults//ADASS//LRU"+NUMBER_OF_QUERIES+"_"+criteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries,  no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            default: break;
        }

        for (int ticks = 1; ;ticks++) {

            if (ticks % CACHE_MAINTENANCE_PERIOD==0){

                this.setCacheMaintenanceStartTime(ticks);
                this.setGetCacheMaintenanceEndTime(ticks+CACHE_MAINTENANCE_DURATION);

                QueryIndex queryIndex  = this.user_containers.get(1).getQueryIndex();

                switch (maintenanceCriteria){
                    case TIME_THRESHOLD_ONLY:{
                        UpdatedData dataReceivedTime = queryIndex.updateQueryIndex_TimeLimit(TIME_THRESHOLD, ticks);
                        int addq = queryIndex.getNumber_addedQueries();
                        int delq = dataReceivedTime.getNumber_deletedQueries();
                        int tempCachehits = dataReceivedTime.getCacheHits();
                        int tempCacheAccesses = dataReceivedTime.getCacheAccesses();

                        if (addq != 0 || delq != 0) {
                            writer1.append(ticks + "," + NUMBER_OF_QUERIES + "," + TIME_THRESHOLD + "," + addq + "," + delq+","+tempCachehits+","+tempCacheAccesses+","+dataReceivedTime.getDataSizeInCache()+","+dataReceivedTime.getDeletedDataSize());
                            writer1.append("\n");
                        }
                        number_deletedQueries += delq;
                        number_addedQueries += addq;
                        number_cacheAccesses += tempCacheAccesses;
                        number_cacheHits += tempCachehits;
                        break;
                    }
                    case FREQUENCY_THRESHOLD_ONLY:{
                        UpdatedData dataReceivedTime = queryIndex.updateQueryIndex_frequencyLimit(FREQUENCY_THRESHOLD, ticks);
                        int addq = queryIndex.getNumber_addedQueries();
                        int delq = dataReceivedTime.getNumber_deletedQueries();
                        int tempCachehits = dataReceivedTime.getCacheHits();
                        int tempCacheAccesses = dataReceivedTime.getCacheAccesses();

                        if (addq != 0 || delq != 0) {
                            writer1.append(ticks + "," + NUMBER_OF_QUERIES + "," + FREQUENCY_THRESHOLD + "," + addq + "," + delq+","+tempCachehits+","+tempCacheAccesses+","+dataReceivedTime.getDataSizeInCache()+","+dataReceivedTime.getDeletedDataSize());
                            writer1.append("\n");
                        }

                        number_deletedQueries += delq;
                        number_addedQueries += addq;
                        number_cacheAccesses += tempCacheAccesses;
                        number_cacheHits += tempCachehits;
                        break;
                    }
                    case COMBINED_ALGO_MAINTENANCE:{
                        UpdatedData dataReceivedTime1 = queryIndex.updateQueryIndex_TimeLimit(TIME_THRESHOLD, ticks);
                        UpdatedData dataReceivedTime = queryIndex.updateQueryIndex_frequencyLimit(FREQUENCY_THRESHOLD, ticks);

                        int addq = queryIndex.getNumber_addedQueries();
                        int delq = dataReceivedTime.getNumber_deletedQueries();
                        int tempCachehits = dataReceivedTime.getCacheHits();
                        int tempCacheAccesses = dataReceivedTime.getCacheAccesses();

                        if (addq != 0 || delq != 0) {
                            writer1.append(ticks + "," + NUMBER_OF_QUERIES + "," + FREQUENCY_THRESHOLD + "," + addq + "," + delq+","+tempCachehits+","+tempCacheAccesses+","+dataReceivedTime.getDataSizeInCache()+","+dataReceivedTime.getDeletedDataSize());
                            writer1.append("\n");
                        }

                        number_deletedQueries += delq;
                        number_addedQueries += addq;
                        number_cacheAccesses += tempCacheAccesses;
                        number_cacheHits += tempCachehits;
                        break;
                    }

                    case LRU: {

                        break;
                    }
                    default: break;
                }

                ticks +=CACHE_MAINTENANCE_DURATION;
            }

            //run all 9 states for each query
            checkState1(ticks);
            checkState2(ticks);
            checkState3(ticks,criteria);
            checkState4(ticks);
            checkState5(ticks);
            checkState6(ticks);
            checkState7(ticks);
            checkState8(ticks);
            checkState9(ticks);

            if(queriesDone == NUMBER_OF_QUERIES) break;
        }

        for (int queryCounter = 0; queryCounter < NUMBER_OF_QUERIES; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);

            if (nextQuery.getStatus().equals("Query completed")) {

                if (nextQuery.getState6() == null) {
                    noOfRepeatedQueries++;
                    log.info(queryCounter + ") repeated query")  ;
                }

                log.info(queryCounter + ") QUERY ID: " + nextQuery.getQSP().getQueryID() + " Start time :" + nextQuery
                        .getStartTime() + " EndTime: " + nextQuery.getEndTime());

                total_responseTime +=nextQuery.getEndTime()-nextQuery.getStartTime();
                totalDataFound += nextQuery.getDataSizeFound();
                totalDataNotFound += nextQuery.getRemaingDataNeeded();
            }
        }

        double average_responseTime = total_responseTime/(double)NUMBER_OF_QUERIES;
        System.out.println("from run through queries " + total_responseTime + " *** " + NUMBER_OF_QUERIES + " *** " + average_responseTime);
        averageDataFound = totalDataFound /(double)NUMBER_OF_QUERIES;
        averageDataNotFound = totalDataNotFound / (double)NUMBER_OF_QUERIES;
        System.out.println("average data found " + averageDataFound + ", **average data not found" + averageDataNotFound);

        writer1.close();
        return average_responseTime;

    }

    /**
     * runs lists of queries in loops
     * @throws IOException
     * @throws InterruptedException
     */
    public void runContinuously() throws IOException, InterruptedException {

        // File outputImprovedCacheLearning = new File(".//src//main//java//project//QueryEnvironment//QueryOutput//Cache_output_5subquery_overlap.csv");
        File outputImprovedCacheLearning = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_dataFound_details.csv");
        FileWriter writer = new FileWriter(outputImprovedCacheLearning);
        writer.flush();
        //the following line is used for general output
        //  writer.append("NoOfQueries,time type,query repetition,Average response time-1,Average response time-2,Average response time-3,Average response time-4,Average response time-5");

        writer.append("AVERAGE RESPONSE, TIME FOR ,OVERLAP, QUERIES\n"+"NoOfQueries,time type,query repetition, no_of_subqueries,Average response time-1_Overlap, Data Found, Data Not Found,Average response time-2_Overlap,Data Found, Data Not Found,Average response time-3_Overlap,Data Found, Data Not Found,Average response time-4_Overlap, Data Found, Data Not Found");
        writer.append('\n');



        for (int i = 1; i < 2; i++) { // this loop is for number of queries
            for (Time_type tt : Time_type.values()) {

                for (Query_type qt : Query_type.values()) {
                    int Max_No_queries = (i * 1000) ;
                    this.setNoOfQueries(Max_No_queries);
                    //  writer.append(Max_No_queries + "," + tt.toString() + "," + qt.toString() + ",");

                    for (int no_of_subQueries = 1; no_of_subQueries < 4; no_of_subQueries++) {

//                        System.out.println("<<<<<<< sub queries >>>>>>>>"+no_of_subQueries);
                        writer.append(Max_No_queries + "," + tt.toString() + "," + qt.toString() + ",");
                        writer.append("SubQueries_"+no_of_subQueries+",");

                        int overlap ;
                        for ( overlap = 1; overlap <no_of_subQueries+1; overlap++) {
                            String input_to_use = "";
                            if (no_of_subQueries == 1){
                                input_to_use = QUERY_INPUT_FILE + no_of_subQueries+"";
                            }
                            else  if (overlap == no_of_subQueries){
                                // input_to_use = QUERY_INPUT_FILE + no_of_subQueries+"_" +(overlap-1)+ "Overlap" ;
                                break;
                            }
                            else {
                                input_to_use = QUERY_INPUT_FILE + no_of_subQueries + "_" +overlap+ "Overlap";
                            }
//                            System.out.println("&&&&&>>>>>>  using input file <<<<<<<<<<<&&&&&"+input_to_use);
                            //experimenting with one container
                            Container container = getUser_containers().get(1);

                            startTime_Query_Distributions(Max_No_queries, input_to_use, tt.toString(), qt.toString(),1);

                            double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers

                            for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                                dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                            }

                            container.setQueryResponses(getQuery_responseTime());
                            System.out.println(Max_No_queries+"****************time type is: " + tt.toString() + " query_type " + qt.toString() + "**** no .of subqueries: " + no_of_subQueries+"************  "+input_to_use+"************");
                            for (int i2 = 0; i2 < query_responseTime.size(); i2++) {
//                                System.out.println(i2 + ") " + query_responseTime.get(i2).getQueryNumber() + " *** " + query_responseTime.get(i2).getQSP().getQueryExpression());

                            }


                            double averageResponseTime = runQueriesThroughStates(Max_No_queries,FULL_QUERY_SEARCH_ONLY,TIME_THRESHOLD_ONLY);
                            //                        System.out.println("AVERAGE RESPONSE TIME FOR " + no_of_subQueries + " *** " + Max_No_queries + " *** " + averageResponseTime);

                            //                        df.setMaximumFractionDigits(2);
                            //                        System.out.println(df.format(averageResponseTime));

                            writer.append(df.format(averageResponseTime) + "");
                            writer.append(df.format(averageDataFound)+","+df.format(averageDataNotFound));
                            if(overlap == no_of_subQueries) writer.append(","+"");
                            if (overlap != 5) writer.append(',');

                            queriesDone = 0;
                            query_responseTime.clear();
                            container.refreshQueryIndex(); // to start a new query index every time

                            totalDataFound=0.0;
                            totalDataNotFound=0.0;
                            averageDataFound=0.0;
                            averageDataNotFound=0.0;

                            // System.out.println("Leaving no_of subqueries"+no_of_subQueries);
                        }
//
                        writer.append('\n');
                    }

                    writer.append('\n');
                }


                writer.append('\n');
            }

            writer.append("\n");
        }



        writer.close();
    }

    /**
     * Just for non overlap queries only
     * @throws IOException
     * @throws InterruptedException
     */
    public void run_non_overlapQueries() throws IOException, InterruptedException {
        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_non_overlap.csv");
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        writer.append("NoOfQueries,query repetition, no_of_subqueries,ResponseTime");
        writer.append('\n');

        for (int i = 1; i < 11; i++) { // this loop is for number of queries

            int Max_No_queries = (i * 1000) ;
            this.setNoOfQueries(Max_No_queries);
            for (int no_of_subQueries = 1; no_of_subQueries < 6; no_of_subQueries++) {

                writer.append(Max_No_queries + "," + " Random (Non-Overlap) " + ",");
                writer.append("SubQueries_" + no_of_subQueries + ",");
                Container container = getUser_containers().get(1);

                startTime_Query_Distributions(Max_No_queries, QUERY_INPUT_FILE + no_of_subQueries, "Fixed", "RanDom",1);
                double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers

                for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                    dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                }

                container.setQueryResponses(getQuery_responseTime());
                double averageResponseTime = runQueriesThroughStates(Max_No_queries,FULL_QUERY_SEARCH_ONLY,TIME_THRESHOLD_ONLY);
                writer.append(df.format(averageResponseTime) + "");

                System.out.println("number of repeated queries = " + noOfRepeatedQueries);

                noOfRepeatedQueries=0;
                queriesDone = 0;
                query_responseTime.clear();
                writer.append('\n');
                container.refreshQueryIndex(); // to start a new query index every time
            }
            writer.append("," + '\n');
        }

        writer.close();
    }

    /**
     * The following method is to run queries of type 2 sub queries and 3 sub queries, with 40% and 60% overlap of each of the segment
     * with full query search only and partial query + full query search (there are always full query overlap is 30%)
     *
     * Input files are ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions"+2/3_40/60PCOverlap_10000
     * @throws IOException
     */
    public void run_40PC_60PC_OverlapQueries() throws IOException, InterruptedException {

        String inputFile= ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions";
        // File outputFile = new File(".//src//main//java//project//QueryEnvironment//QueryOutput//Cache_output_40_60_overlap.csv");
        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_dataFound_details.csv");
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found");
        writer.append('\n');
        for (int i = 1; i < 2; i++) { // this loop is for number of queries
            int Max_No_queries = (i * 10000) ; // change this val to 10000
            this.setNoOfQueries(Max_No_queries);

            // for (int no_of_subQueries = 2; no_of_subQueries < 4; no_of_subQueries++) {
            for (int no_of_subQueries = 2; no_of_subQueries < 6; no_of_subQueries++) {

                for (int overlapPercentage = 40; overlapPercentage < 62; overlapPercentage += 20)  {

                    for(Querycriteria qc:Querycriteria.values()) {// full query search and partial query search

                        writer.append(Max_No_queries + "," + qc.toString() + ",");
                        writer.append(overlapPercentage+",");
                        writer.append("SubQueries_" + no_of_subQueries + ",");

                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        System.out.println("Time of starting this iteration: " + dateFormat.format(date));

                        System.out.println(" INPUT FILE IS "+inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000 "+"_ criteria "+qc.toString());
                        // below 1 is added to the startTime_Query_Distributions(...) method signature to inform that fixed queries at 1 tick per query
                        startTime_Query_Distributions(Max_No_queries, inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000", "Fixed", "Fixed",1);

                        double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers
                        for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                            dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                        }

                        Container container = getUser_containers().get(1);
                        //CHECK THE FOLLOWING
                        //  container.getQueryIndex().getQueryIndexGraph().printGraph();

                        container.setQueryResponses(getQuery_responseTime());
                        double averageResponseTime = runQueriesThroughStates(Max_No_queries,qc.toString(),TIME_THRESHOLD_ONLY);

                        writer.append(df.format(averageResponseTime) + ",");
                        averageDataFound = totalDataFound/(double) Max_No_queries;
                        writer.append(df.format(averageDataFound) + ",");
                        averageDataNotFound = totalDataNotFound / (double)Max_No_queries;
                        writer.append(df.format(averageDataNotFound) + "");


                        writer.append('\n');

                        System.out.println("number of repeated queries = " + noOfRepeatedQueries);
                        System.out.println("Average data found " + averageDataFound);
                        System.out.println("Average data not found "+averageDataNotFound);

                        noOfRepeatedQueries=0;
                        queriesDone = 0;
                        query_responseTime.clear();
                        container.refreshQueryIndex(); // to start a new query index every time

                        totalDataFound=0.0;
                        totalDataNotFound=0.0;
                        averageDataFound=0.0;
                        averageDataNotFound=0.0;

                    }//overlap for loop

                    writer.append("," + '\n');
                }

                writer.append("," + '\n');
            }// no of sub queries  for loop
            writer.append("," + '\n');

        }// for loop for number of queries

        writer.close();


    }// method close

    /**
     * The following  piece of code is to run some sample query traces
     */
    public void runSampleQueryTraces() throws IOException, InterruptedException {

        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_time_threshold.csv");
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Time threshold");
        writer.append('\n');

        int[] cacheMaintenanceArray = {3600, 7200, 10800, 14400};
        int[] timeThreshold = {100, 200 }; //,300,400,500

        for (int i = 1; i < 2;  i++) { // this loop is for number of queries
            int Max_No_queries = (i * 2000) ; // change this val to 10000
            this.setNoOfQueries(Max_No_queries);


            for (int no_of_subQueries = 2; no_of_subQueries < 3; no_of_subQueries++) {

                for (int overlapPercentage = 40; overlapPercentage < 42; overlapPercentage += 20)  {

                    for(Querycriteria qc:Querycriteria.values()) {// full query search and partial query search

                      /*  for (int j = 0; j < 4; j++) {
                            CACHE_MAINTENANCE_PERIOD = cacheMaintenanceArray[j];
                        */
                        for (int j = 0; j < 1; j++) {


                            CACHE_MAINTENANCE_PERIOD= 200;
                            TIME_THRESHOLD = timeThreshold[j];

                            String inputFile = ".//src//main//java//project//QueryEnvironment//QueryInput//20PC_QueryExpressions";

                            writer.append(Max_No_queries + "," + qc.toString() + ",");
                            writer.append(overlapPercentage + ",");
                            writer.append("SubQueries_" + no_of_subQueries + ",");

                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            Date date = new Date();
                            System.out.println("Time of starting this iteration: " + dateFormat.format(date));

                            inputFile = inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000";

                            System.out.println(" INPUT FILE IS " + inputFile + "_ criteria " + qc.toString());
                            startTime_Query_Distributions(Max_No_queries, inputFile, "Fixed", "Fixed",2);

                            double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers
                            for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                                dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                            }

                            Container container = getUser_containers().get(1);
                            //CHECK THE FOLLOWING
                            //  container.getQueryIndex().getQueryIndexGraph().printGraph();

                            container.setQueryResponses(getQuery_responseTime());
                            double averageResponseTime = runQueriesThroughStates(Max_No_queries, qc.toString(),TIME_THRESHOLD_ONLY);

                            writer.append(df.format(averageResponseTime) + ",");
                            averageDataFound = totalDataFound / (double) Max_No_queries;
                            writer.append(df.format(averageDataFound) + ",");
                            averageDataNotFound = totalDataNotFound / (double) Max_No_queries;
                            writer.append(df.format(averageDataNotFound) + "");
                            writer.append("," + timeThreshold[j]);
                            writer.append('\n');

                            System.out.println("number of repeated queries = " + noOfRepeatedQueries);
                            System.out.println("Average data found " + averageDataFound);
                            System.out.println("Average data not found " + averageDataNotFound);

                            noOfRepeatedQueries = 0;
                            queriesDone = 0;
                            query_responseTime.clear();
                            container.refreshQueryIndex(); // to start a new query index every time

                            totalDataFound = 0.0;
                            totalDataNotFound = 0.0;
                            averageDataFound = 0.0;
                            averageDataNotFound = 0.0;

                            DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            Date date1 = new Date();
                            System.out.println("Time of Ending this iteration: " + dateFormat1.format(date1));
                            System.out.println("******************************************************************");

                        }// time threshold or cache maintenance period

                    }//overlap for loop

                    writer.append("," + '\n');
                }

                writer.append("," + '\n');
            }// no of sub queries  for loop
            writer.append("," + '\n');

        }// for loop for number of queries

        writer.close();


    }

    /**
     * The following method is to check cache stability for the slow changing inputs, random inputs, and inputs that appear in a distribution
     * variations time threshold, data size threshold and frequency threshold
     */
    public void checkCacheStability_TimeThreshold() throws IOException, InterruptedException {


        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_time_threshold.csv");
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Time threshold, number_queries Added, number_queries deleted, CacheHits, Cache Accesses,av_QueryLifeSpan");
        writer.append('\n');

        int[] cacheMaintenanceArray = {50,100,200, 500, 1000, 2000};
        int[] timeThreshold = {200,500,1000,1500,2000,2500,3000};

        int[] fixed_timeGap = {1,2,3,4,5};

        for (int i = 1; i < 2;  i++) { // this loop is for number of queries
            int Max_No_queries = (i * 10000) ; // change this val to 10000
            this.setNoOfQueries(Max_No_queries);


            for (int no_of_subQueries = 3; no_of_subQueries < 4; no_of_subQueries++) {

                for (int overlapPercentage = 40; overlapPercentage < 42; overlapPercentage += 20)  {

                    for(Querycriteria qc:Querycriteria.values()) {// full query search and partial query search

                        for (int j = 2; j < 3; j++) {
                            CACHE_MAINTENANCE_PERIOD = cacheMaintenanceArray[j];

                            for (int time_threshold = 0; time_threshold < 4; time_threshold++) { //upto 2500

                                for (int fixedTimeGap = 1; fixedTimeGap < 2; fixedTimeGap++) { // time gap for the query arrival is set to 2;

                                    //  CACHE_MAINTENANCE_PERIOD = 200;
                                    TIME_THRESHOLD = timeThreshold[time_threshold];
                                    FREQUENCY_THRESHOLD = 20;
                                    String inputFile = ".//src//main//java//project//QueryEnvironment//QueryInput//20PC_QueryExpressions";

                                    writer.append(Max_No_queries + "," + qc.toString() + ",");
                                    writer.append(overlapPercentage + ",");
                                    writer.append("SubQueries_" + no_of_subQueries + ",");

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date = new Date();
                                    System.out.println("Time of starting this iteration: " + dateFormat.format(date));

                                    inputFile = inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000";

                                    System.out.println(" INPUT FILE IS " + inputFile + "_ criteria " + qc.toString() + "  From time threshold " + timeThreshold[time_threshold]);
                                    startTime_Query_Distributions(Max_No_queries, inputFile, "Fixed", "Fixed", fixed_timeGap[fixedTimeGap]);

                                    double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers
                                    for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                                        dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                                    }

                                    Container container = getUser_containers().get(1);
                                    //CHECK THE FOLLOWING
                                    //  container.getQueryIndex().getQueryIndexGraph().printGraph();

                                    container.setQueryResponses(getQuery_responseTime());
                                    double averageResponseTime = runQueriesThroughStates(Max_No_queries, qc.toString(), TIME_THRESHOLD_ONLY);

                                    writer.append(df.format(averageResponseTime) + ",");
                                    averageDataFound = totalDataFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataFound) + ",");
                                    averageDataNotFound = totalDataNotFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataNotFound) + "");
                                    writer.append("," + timeThreshold[time_threshold]);
                                    writer.append("," + number_addedQueries);
                                    writer.append("," + number_deletedQueries);
                                    writer.append("," + number_cacheHits);
                                    writer.append("," + number_cacheAccesses);


                                    int totalLifeSpan = 0;
                                    int totalQueriesInList =0;
                                    for (Cache cache : container.getQueryIndex().getCache()) {
                                        // System.out.println("^^^^^^^^^^^no.of queries in list^^^^^^^^^ "+cache.getQueryList().size());
                                        for (IndexedQuery iq : cache.getQueryList()) {
                                            totalLifeSpan += iq.getLifeSpan();
                                        }
                                        totalQueriesInList += cache.getQueryList().size();
                                    }

                                    double average_Query_LifeSpan = totalLifeSpan / (double) totalQueriesInList;
                                    writer.append("," + average_Query_LifeSpan);

                                    writer.append('\n');

                                    System.out.println("number of repeated queries = " + noOfRepeatedQueries);
                                    System.out.println("Average data found " + averageDataFound + "Average data not found " + averageDataNotFound);
                                    System.out.println("Average life span = " + average_Query_LifeSpan +"-- Total Queries = "+ totalQueriesInList+"--"+totalLifeSpan);
                                    System.out.println();

                                    //refresh everything for the next iteration
                                    noOfRepeatedQueries = 0;
                                    queriesDone = 0;
                                    query_responseTime.clear();

                                    container.refreshQueryIndex(); // to start a new query index every time

                                    totalDataFound = 0.0;
                                    totalDataNotFound = 0.0;
                                    averageDataFound = 0.0;
                                    averageDataNotFound = 0.0;
                                    number_addedQueries = 0;
                                    number_deletedQueries = 0;
                                    number_cacheHits = 0;
                                    number_cacheAccesses = 0;


                                    DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date1 = new Date();
                                    System.out.println("Time of Ending this iteration: " + dateFormat1.format(date1));
                                    System.out.println("******************************************************************");
                                }//fixed time gap

                            }// time threshold or cache maintenance period
                        }//cache maintenance period

                    }//overlap for loop

                    writer.append("," + '\n');
                }

                writer.append("," + '\n');
            }// no of sub queries  for loop
            writer.append("," + '\n');

        }// for loop for number of queries

        writer.close();

    }

    /**
     * The following method is to check cache stability for the slow changing inputs, random inputs, and inputs that appear in a distribution
     * variations time threshold, data size threshold and frequency threshold
     */
    public void checkCacheStability_FrequencyThreshold() throws IOException, InterruptedException {

        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_frequency_threshold.csv");
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Frequency threshold, number_queries Added, number_queries deleted, CacheHits, Cache Accesses, av_lifeSpan");
        writer.append('\n');

        int[] cacheMaintenanceArray = {50,100,200, 500, 1000, 2000};
        int[] frequencyThreshold = {5,10,15,20,30,40,50};
        int[] fixed_timeGap = {1,2,3,4,5};

        for (int i = 1; i < 2;  i++) { // this loop is for number of queries
            int Max_No_queries = (i * 2500) ; // change this val to 10000
            this.setNoOfQueries(Max_No_queries);


            for (int no_of_subQueries = 3; no_of_subQueries < 4; no_of_subQueries++) {

                for (int overlapPercentage = 40; overlapPercentage < 42; overlapPercentage += 20)  {

                    for(Querycriteria qc:Querycriteria.values()) {// full query search and partial query search

                        for (int j = 2; j < 3; j++) {
                            CACHE_MAINTENANCE_PERIOD = cacheMaintenanceArray[j];

                            for (int frequency_threshold = 0; frequency_threshold < 4; frequency_threshold++) { //upto 2500

                                for (int fixedTimeGap = 1; fixedTimeGap < 2; fixedTimeGap++) { // time gap for the query arrival is set to 2;

                                    // CACHE_MAINTENANCE_PERIOD = 200;
                                    FREQUENCY_THRESHOLD = frequencyThreshold[frequency_threshold];

                                    TIME_THRESHOLD = 500;
                                    String inputFile = ".//src//main//java//project//QueryEnvironment//QueryInput//20PC_QueryExpressions";

                                    writer.append(Max_No_queries + "," + qc.toString() + ",");
                                    writer.append(overlapPercentage + ",");
                                    writer.append("SubQueries_" + no_of_subQueries + ",");

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date = new Date();
                                    System.out.println("Time of starting this iteration: " + dateFormat.format(date));

                                    inputFile = inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000";

                                    System.out.println(" INPUT FILE IS " + inputFile + "_ criteria " + qc.toString() + "  from frequency threshold " + frequencyThreshold[frequency_threshold]);
                                    startTime_Query_Distributions(Max_No_queries, inputFile, "Fixed", "Fixed", fixed_timeGap[fixedTimeGap]);

                                    double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers
                                    for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                                        dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                                    }

                                    Container container = getUser_containers().get(1);
                                    //CHECK THE FOLLOWING
                                    //  container.getQueryIndex().getQueryIndexGraph().printGraph();

                                    container.setQueryResponses(getQuery_responseTime());
                                    double averageResponseTime = runQueriesThroughStates(Max_No_queries, qc.toString(), FREQUENCY_THRESHOLD_ONLY);

                                    writer.append(df.format(averageResponseTime) + ",");
                                    averageDataFound = totalDataFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataFound) + ",");
                                    averageDataNotFound = totalDataNotFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataNotFound) + "");
                                    writer.append("," + frequencyThreshold[frequency_threshold]);
                                    writer.append("," + number_addedQueries);
                                    writer.append("," + number_deletedQueries);
                                    writer.append("," + number_cacheHits);
                                    writer.append("," + number_cacheAccesses);

                                    int totalLifeSpan = 0;
                                    int totalQueriesInList = 0;
                                    for (Cache cache : container.getQueryIndex().getCache()) {
                                        //System.out.println("^^^^^^^^^^^no.of queries in list^^^^^^^^^ " + cache.getQueryList().size());
                                        for (IndexedQuery iq : cache.getQueryList()) {
                                            totalLifeSpan += iq.getLifeSpan();
                                        }
                                        totalQueriesInList += cache.getQueryList().size();
                                    }

                                    double average_Query_LifeSpan = totalLifeSpan / (double) totalQueriesInList;
                                    writer.append("," + average_Query_LifeSpan);

                                    writer.append('\n');

                                    System.out.println("number of repeated queries = " + noOfRepeatedQueries);
                                    System.out.println("Average data found " + averageDataFound + "Average data not found " + averageDataNotFound);
                                    System.out.println("Average life span = " + average_Query_LifeSpan +"-- Total Queries = "+ totalQueriesInList+"--"+totalLifeSpan);
                                    System.out.println();

                                    //refresh everything for the next iteration
                                    noOfRepeatedQueries = 0;
                                    queriesDone = 0;
                                    query_responseTime.clear();
                                    container.refreshQueryIndex(); // to start a new query index every time

                                    totalDataFound = 0.0;
                                    totalDataNotFound = 0.0;
                                    averageDataFound = 0.0;
                                    averageDataNotFound = 0.0;
                                    number_addedQueries = 0;
                                    number_deletedQueries = 0;
                                    number_cacheHits = 0;
                                    number_cacheAccesses = 0;

                                    DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date1 = new Date();
                                    System.out.println("Time of Ending this iteration: " + dateFormat1.format(date1));
                                    System.out.println("******************************************************************");
                                }//fixed time gap

                            }// time threshold or cache maintenance period
                        }// cache maintenance array

                    }//overlap for loop

                    writer.append("," + '\n');
                }

                writer.append("," + '\n');
            }// no of sub queries  for loop
            writer.append("," + '\n');

        }// for loop for number of queries

        writer.close();


    }

    /**
     * TODO: COMPLETE THIS CODE
     * The following method is to check cache stability for the slow changing inputs, random inputs, and inputs that appear in a distribution
     * variations time threshold, data size threshold and frequency threshold
     */
    public void checkCacheStability_CacheMaintenance_Frequency() throws IOException, InterruptedException {


        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_CacheMaintenance.csv");

        //   File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_sample_testing.csv"); // for varied cache maintenance
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        //  writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Cache maintenance period");
        writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Frequency threshold, number_queries Added, number_queries deleted, CacheHits, Cache Accesses");
        writer.append('\n');

        int[] cacheMaintenanceArray = {200, 500, 1000, 2000};
        int[] frequencyThreshold = {5,10,20,30,40,50};
        int[] fixed_timeGap = {1,2,3,4,5};

        for (int i = 1; i < 2;  i++) { // this loop is for number of queries
            int Max_No_queries = (i * 2000) ; // change this val to 10000
            this.setNoOfQueries(Max_No_queries);


            for (int no_of_subQueries = 3; no_of_subQueries < 4; no_of_subQueries++) {

                for (int overlapPercentage = 40; overlapPercentage < 42; overlapPercentage += 20)  {

                    for(Querycriteria qc:Querycriteria.values()) {// full query search and partial query search

                        for (int j = 0; j < 4; j++) {
                            CACHE_MAINTENANCE_PERIOD = cacheMaintenanceArray[j];

                            for (int frequency_threshold = 0; frequency_threshold < 3; frequency_threshold++) { //upto 2500

                                for (int fixedTimeGap = 1; fixedTimeGap < 2; fixedTimeGap++) { // time gap for the query arrival is set to 2;

                                    //CACHE_MAINTENANCE_PERIOD = 200;
                                    FREQUENCY_THRESHOLD = frequencyThreshold[frequency_threshold];

                                    TIME_THRESHOLD = 500;
                                    String inputFile = ".//src//main//java//project//QueryEnvironment//QueryInput//20PC_QueryExpressions";

                                    writer.append(Max_No_queries + "," + qc.toString() + ",");
                                    writer.append(overlapPercentage + ",");
                                    writer.append("SubQueries_" + no_of_subQueries + ",");

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date = new Date();
                                    System.out.println("Time of starting this iteration: " + dateFormat.format(date));

                                    inputFile = inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000";

                                    System.out.println(" INPUT FILE IS " + inputFile + "_ criteria " + qc.toString() + "  from frequency threshold " + frequencyThreshold[frequency_threshold]);
                                    startTime_Query_Distributions(Max_No_queries, inputFile, "Fixed", "Fixed", fixed_timeGap[fixedTimeGap]);

                                    double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers
                                    for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                                        dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                                    }

                                    Container container = getUser_containers().get(1);
                                    //CHECK THE FOLLOWING
                                    //  container.getQueryIndex().getQueryIndexGraph().printGraph();

                                    container.setQueryResponses(getQuery_responseTime());
                                    double averageResponseTime = runQueriesThroughStates(Max_No_queries, qc.toString(), FREQUENCY_THRESHOLD_ONLY);

                                    writer.append(df.format(averageResponseTime) + ",");
                                    averageDataFound = totalDataFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataFound) + ",");
                                    averageDataNotFound = totalDataNotFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataNotFound) + "");
                                    writer.append("," + frequencyThreshold[frequency_threshold]);
                                    writer.append("," + number_addedQueries);
                                    writer.append("," + number_deletedQueries);
                                    writer.append("," + number_cacheHits);
                                    writer.append("," + number_cacheAccesses);
                                    writer.append('\n');

                                    System.out.println("number of repeated queries = " + noOfRepeatedQueries);
                                    System.out.println("Average data found " + averageDataFound + "Average data not found " + averageDataNotFound);
                                    System.out.println();

                                    //refresh everything for the next iteration
                                    noOfRepeatedQueries = 0;
                                    queriesDone = 0;
                                    query_responseTime.clear();
                                    container.refreshQueryIndex(); // to start a new query index every time

                                    totalDataFound = 0.0;
                                    totalDataNotFound = 0.0;
                                    averageDataFound = 0.0;
                                    averageDataNotFound = 0.0;
                                    number_addedQueries = 0;
                                    number_deletedQueries = 0;
                                    number_cacheHits = 0;
                                    number_cacheAccesses = 0;

                                    DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date1 = new Date();
                                    System.out.println("Time of Ending this iteration: " + dateFormat1.format(date1));
                                    System.out.println("******************************************************************");
                                }//fixed time gap

                            }// time threshold or cache maintenance period
                        }// CACHE MAINTENANCE

                    }//overlap for loop

                    writer.append("," + '\n');
                }

                writer.append("," + '\n');
            }// no of sub queries  for loop
            writer.append("," + '\n');

        }// for loop for number of queries

        writer.close();


    }

    //TODO STILL
    public void checkCacheStability_Time_Frequency() throws IOException, InterruptedException {


        File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_Time_frequency.csv");

        //   File outputFile = new File("//home//santhilata//Dropbox//ImprovedCachelearningResults//Cache_output_sample_testing.csv"); // for varied cache maintenance
        FileWriter writer = new FileWriter(outputFile);
        writer.flush();

        //  writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Cache maintenance period");
        writer.append("NoOfQueries,query search type, Overlap Percentage, no_of_subqueries,ResponseTime,Average Data Found, Average Data Not Found, Frequency threshold, number_queries Added, number_queries deleted, CacheHits, Cache Accesses");
        writer.append('\n');

        int[] cacheMaintenanceArray = {200, 500, 1000, 2000};
        int[] frequencyThreshold = {5,10,20,30,40,50};
        int[] fixed_timeGap = {1,2,3,4,5};

        for (int i = 1; i < 2;  i++) { // this loop is for number of queries
            int Max_No_queries = (i * 10000) ; // change this val to 10000
            this.setNoOfQueries(Max_No_queries);


            for (int no_of_subQueries = 3; no_of_subQueries < 4; no_of_subQueries++) {

                for (int overlapPercentage = 40; overlapPercentage < 42; overlapPercentage += 20)  {

                    for(Querycriteria qc:Querycriteria.values()) {// full query search and partial query search

                        for (int j = 0; j < 4; j++) {
                            CACHE_MAINTENANCE_PERIOD = cacheMaintenanceArray[j];

                            for (int frequency_threshold = 0; frequency_threshold < 3; frequency_threshold++) { //upto 2500

                                for (int fixedTimeGap = 1; fixedTimeGap < 2; fixedTimeGap++) { // time gap for the query arrival is set to 2;

                                    FREQUENCY_THRESHOLD = frequencyThreshold[frequency_threshold];

                                    TIME_THRESHOLD = 500;
                                    String inputFile = ".//src//main//java//project//QueryEnvironment//QueryInput//20PC_QueryExpressions";

                                    writer.append(Max_No_queries + "," + qc.toString() + ",");
                                    writer.append(overlapPercentage + ",");
                                    writer.append("SubQueries_" + no_of_subQueries + ",");

                                    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date = new Date();
                                    System.out.println("Time of starting this iteration: " + dateFormat.format(date));

                                    inputFile = inputFile + no_of_subQueries + "_" + overlapPercentage + "PCOverlap_10000";

                                    System.out.println(" INPUT FILE IS " + inputFile + "_ criteria " + qc.toString() + "  from frequency threshold " + frequencyThreshold[frequency_threshold]);
                                    startTime_Query_Distributions(Max_No_queries, inputFile, "Fixed", "Fixed", fixed_timeGap[fixedTimeGap]);

                                    double queryInterArrivalTime = query_responseTime.get(Max_No_queries - 1).getStartTime() / (double) Max_No_queries; //average arrival time at data servers
                                    for (int i1 = 0; i1 < no_of_DataServers; i1++) {
                                        dataServers[i1].setQueryInterArrivalTime(queryInterArrivalTime);
                                    }

                                    Container container = getUser_containers().get(1);
                                    //CHECK THE FOLLOWING
                                    //  container.getQueryIndex().getQueryIndexGraph().printGraph();

                                    container.setQueryResponses(getQuery_responseTime());
                                    double averageResponseTime = runQueriesThroughStates(Max_No_queries, qc.toString(), FREQUENCY_THRESHOLD_ONLY);

                                    writer.append(df.format(averageResponseTime) + ",");
                                    averageDataFound = totalDataFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataFound) + ",");
                                    averageDataNotFound = totalDataNotFound / (double) Max_No_queries;
                                    writer.append(df.format(averageDataNotFound) + "");
                                    writer.append("," + frequencyThreshold[frequency_threshold]);
                                    writer.append("," + number_addedQueries);
                                    writer.append("," + number_deletedQueries);
                                    writer.append("," + number_cacheHits);
                                    writer.append("," + number_cacheAccesses);
                                    writer.append('\n');

                                    System.out.println("number of repeated queries = " + noOfRepeatedQueries);
                                    System.out.println("Average data found " + averageDataFound + "Average data not found " + averageDataNotFound);
                                    System.out.println();

                                    //refresh everything for the next iteration
                                    noOfRepeatedQueries = 0;
                                    queriesDone = 0;
                                    query_responseTime.clear();
                                    container.refreshQueryIndex(); // to start a new query index every time

                                    totalDataFound = 0.0;
                                    totalDataNotFound = 0.0;
                                    averageDataFound = 0.0;
                                    averageDataNotFound = 0.0;
                                    number_addedQueries = 0;
                                    number_deletedQueries = 0;
                                    number_cacheHits = 0;
                                    number_cacheAccesses = 0;

                                    DateFormat dateFormat1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    Date date1 = new Date();
                                    System.out.println("Time of Ending this iteration: " + dateFormat1.format(date1));
                                    System.out.println("******************************************************************");
                                }//fixed time gap

                            }// time threshold or cache maintenance period
                        }// CACHE MAINTENANCE

                    }//overlap for loop

                    writer.append("," + '\n');
                }

                writer.append("," + '\n');
            }// no of sub queries  for loop
            writer.append("," + '\n');

        }// for loop for number of queries

        writer.close();
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        org.apache.log4j.PropertyConfigurator.configure("log4j.properties");

        CommunityCacheMain clm = new CommunityCacheMain();
    /*
    clm.setNoOfQueries(NUMBER_OF_QUERIES);

        Container container = clm.getUser_containers().get(1);
        clm.startTime_Query_Distributions(NUMBER_OF_QUERIES, INPUT_FILE, QUERY_INTERARRIVAL_DISTRIBUTION, QUERY_REPETITION_DISTRIBUTION);
        double queryInterArrivalTime = query_responseTime.get(NUMBER_OF_QUERIES-1).getStartTime()/(double)NUMBER_OF_QUERIES; //average arrival time at data servers

        for (int i = 0; i < no_of_DataServers; i++) {
            clm.dataServers[i].setQueryInterArrivalTime(queryInterArrivalTime);
        }
        container.setQueryResponses(clm.getQuery_responseTime());

        clm.runQueriesThroughStates(NUMBER_OF_QUERIES);
 */
        // clm.runContinuously();


        // clm.run_40PC_60PC_OverlapQueries();
        // clm.runSampleQueryTraces();
        // clm.checkCacheStability_TimeThreshold();
        clm.checkCacheStability_FrequencyThreshold();


    }

}
