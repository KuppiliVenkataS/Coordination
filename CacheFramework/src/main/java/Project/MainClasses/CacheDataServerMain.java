package project.MainClasses;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.log4j.Logger;
import project.MiddlewareEnvironment.Container;
import project.Network.NetworkConstants;
import project.Network.NetworkTopography;
import project.QueryEnvironment.*;
import project.ResponseTimeSimulation.Cache_DataServerSimulation.*;
import project.UserEnvironment.QuerySegment_ExecutionTime;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

/**
 * Created by santhilata on 11/5/15.
 */
public class CacheDataServerMain implements PropertiesLoaderImplBase, NetworkConstants {
    protected static final Logger log = Logger.getLogger(CacheDataServerMain.class);
    static NetworkTopography networkTopography = new NetworkTopography();
    DataServer[] dataServers = new DataServer[no_of_DataServers];
    Cache_DataServer[] cache_dataServers = new Cache_DataServer[no_of_DataServers];
    ArrayList<Container> user_containers = new ArrayList<>();
    static ArrayList<Query_Response_DataServer> query_responseTime = new ArrayList<>();
    int noOfQueries;

    static  int queriesDone=0;
    public static final String FULLY_FOUND = "FULLY FOUND";
    public static final String PARTIALLY_FOUND = "PARTIALLY FOUND";
    public static final String NOT_FOUND = "NOT FOUND";
    ArrayList<SubjectQuerySegment>[][] q = new ArrayList[NUMBER_OF_QUERIES][6];


    public CacheDataServerMain(int noOfQueries){
        this.noOfQueries = noOfQueries;
        //set user containers
        for (int i = 0; i < no_of_userContainers; i++) {
            String container_name = "Container_"+(i+1) ;
            user_containers.add(new Container(container_name));

        }

        //start data servers

        for (int i = 0; i < no_of_DataServers ; i++) {
            dataServers[i] = new DataServer("db"+(i+1));
            dataServers[i].setAddress_container("Container_"+(i+999));

            cache_dataServers[i] = new Cache_DataServer(dataServers[i].getDatabaseName());

            /*int randomInt = new Random().nextInt(user_containers.size());
            // try to set one data server per container only
            ArrayList<String> usedContainers = new ArrayList<>();
            String testString = "Container_"+randomInt;
            if (!usedContainers.contains(testString))
            dataServers[i].setAddress_container(testString);
            */
        }

        for (int i = 0; i < NUMBER_OF_QUERIES; i++) {
            for (int j = 0; j <6 ; j++) {
               q[i][j] = new ArrayList<>();
            }
        }

    }

    private static ArrayList<String> readQueryExpressions1(File QueryExpressionFile) throws IOException {
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
    private  void  startTime_Query_Distributions(int noOfQueries, String timeDistribution,String queryDistribution) throws IOException {

        ArrayList<String> queries = null;
        ArrayList queryList = new ArrayList(noOfQueries);
        query_responseTime = new ArrayList<>();

        // queries are sent as an expression from query agents
//        File queryExpressionFile = new File(
//                ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions1");
        File queryExpressionFile = new File( INPUT_FILE);
        BufferedWriter out1 = new BufferedWriter(new FileWriter(BACKUP_FILE));
        BufferedWriter out = new BufferedWriter(new FileWriter(GENERATED_FILE));

        try {
            queries = readQueryExpressions1(queryExpressionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        int time_type = 0;

        if (timeDistribution.equals("Random")) time_type = 1;
        else if (timeDistribution.equals("Poisson")) time_type = 2;
        else  if (timeDistribution.equals("Uniform")) time_type = 3;
        else if (timeDistribution.equals("Exponential")) time_type = 4;
        else if (timeDistribution.equals("Fixed")) time_type = 5;

        switch (time_type){
            case 1:{

                Random randomGenerator = new Random();

                for (int i=0; i<noOfQueries; i++)
                {
                    Query_Response_DataServer qr = new Query_Response_DataServer();
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
                    Query_Response_DataServer qr = new Query_Response_DataServer();
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
                    Query_Response_DataServer qr = new Query_Response_DataServer();
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
                    Query_Response_DataServer qr = new Query_Response_DataServer();
                    qr.setStartTime(new Double(randomDouble).intValue()+1);//to avoid random value zero
                    query_responseTime.add(qr);

                }


                break;
            }

            case 5: { // for standard queries and testing purpose

                for (int i = 0; i < noOfQueries; i++) {
                    Query_Response_DataServer qr = new Query_Response_DataServer();
                    qr.setStartTime(i+1);//to avoid random value zero
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

        if (queryDistribution.equals("Random")) query_type = 1;
        else if (queryDistribution.equals("Poisson")) query_type = 2;
        else  if (queryDistribution.equals("Uniform")) query_type = 3;
        else  if (queryDistribution.equals("Exponential")) query_type = 4;
        else if (queryDistribution.equals("Fixed")) query_type = 5;

        switch (query_type)
        {
            case 1:{

                Random randomGenerator = new Random();

                for (int i=0; i<noOfQueries; i++)
                {
                    int randomInt = randomGenerator.nextInt(NUMBER_OF_QUERIES_IN_FILE-1);
                    query_responseTime.get(i).setQueryNumber(randomInt);

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
                    query_responseTime.get(i).setQueryNumber(randomInt);
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
                    query_responseTime.get(i).setQueryNumber(randomInt);
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
                    query_responseTime.get(i).setQueryNumber(randomInt);
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
                    query_responseTime.get(i).setQueryNumber(i);
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
                System.out.println("not matching with any selection");
                break;
            }
        }

        Collections.sort(query_responseTime);

    }// End of method

    private void check_State1(int ticks){
        int noOfQueries = query_responseTime.size();

        for(int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);

            if (nextQuery.getStatus().equals("State1")) {

                int nextQueryTime = query_responseTime.get(queryCounter).getStartTime();

                if ((nextQueryTime == ticks)) {
                    //open the state 1 of query to be entered
                    UserInput_State1 state1 = new UserInput_State1();
                    state1.setState1_Starttime(ticks);
                    state1.setExpressionTree(new ExpressionTree(nextQuery.getQSP()));

                    ArrayList<Query_Subj_Predicate> queries = state1.getExpressionTree().getSubQueries();




                    for (int i = 0; i < queries.size() ; i++) {

                        Query_Subj_Predicate qsp = queries.get(i);

                        ArrayList<PredicateQuerySegment> preds = qsp.getPredicateQuerySegments();
                        for (int j = 0; j < preds.size(); j++) {
                            PredicateQuerySegment pred = preds.get(j);

                            int type = pred.getAttributes();
                            SubjectQuerySegment attribute1 = null;
                            SubjectQuerySegment attribute2 = null;
                            String condition="";

                            switch (type){
                                case 0: {
                                     condition = pred.getCondition();
                                    break;
                                }
                                case 1: {
                                     attribute1 = pred.getAttribute1();
                                     condition = pred.getCondition();
                                    break;
                                }
                                case 2: {
                                    attribute1 = pred.getAttribute1();
                                    attribute2 = pred.getAttribute2();
                                    condition = pred.getCondition();
                                }
                            }
                            if(attribute1 != null) {
                                String database = attribute1.getDatabase();
                                int databaseNo = Integer.parseInt(String.valueOf(database.charAt(database.length()-1)));
                                q[queryCounter][databaseNo - 1].add(attribute1);

                                if (attribute2 != null) {
                                    database = attribute2.getDatabase();
                                    databaseNo = Integer.parseInt(String.valueOf(database.charAt(database.length() - 1)));
                                    q[queryCounter][databaseNo - 1].add(attribute2);
                                }
                            }
                        }

                    }
                   /*
                    for (int i = 0; i < 6; i++) {
                        for (int j = 0; j < q[i].size(); j++) {
                            System.out.println(q[i].get(j).toString());
                        }
                    }
                    */

                    state1.setRemainingTime(2);

                    nextQuery.setState1(state1);
                    nextQuery.setStartTime(state1.getState1_Starttime());

                    nextQuery.getState1().setRemainingTime(state1.getRemainingTime()-1);
                    nextQuery.setStatus("Query Processing");
                }
            }

           else if (nextQuery.getStatus().equals("Query Processing")) {
                if (nextQuery.getState1().getRemainingTime() == 1) {
                    nextQuery.getState1().setRemainingTime(0);
                    nextQuery.setStatus("State1: done");
                    nextQuery.getState1().setState1_EndTime(ticks);

//                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 1 start time " +
//                    nextQuery.getState1().getState1_Starttime() + " end time " + nextQuery.getState1()
//                            .getState1_EndTime());
                    System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 1 start time " +
                            nextQuery.getState1().getState1_Starttime() + " end time " + nextQuery.getState1()
                            .getState1_EndTime());

                }
                else  nextQuery.getState1().setRemainingTime(nextQuery.getState1().getRemainingTime() - 1);

            }
        }
    }

    private void check_State2(int ticks){
        int noOfQueries = query_responseTime.size();

        for(int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);

            if (nextQuery.getStatus().equals("State1: done")||(nextQuery.getStatus().startsWith("State2:"))) {
                if (nextQuery.getStatus().equals("State1: done") && (nextQuery.getState1().getState1_EndTime() < ticks
                )) {
                    nextQuery.setState2(new QueryTransferOnWAN_State2());
                    nextQuery.setStatus(nextQuery.getState2().getStatus()); // sets with the status of state 2
                    double loadFactor;

                    if (noOfQueries > WAN_QUERY_LOAD_LIMIT) loadFactor = WAN_HIGH_LOADFACTOR;
                    else loadFactor =  WAN_LOW_LOADFACTOR;
                    double time = networkTopography.getWAN_Delay(loadFactor, 0.001);

                    double no_ticks_needed = Math.ceil(time / 1000);
                    nextQuery.getState2().setTimeRequiredAtWAN(no_ticks_needed);
                    nextQuery.getState2().setState2_Starttime(ticks);
                    nextQuery.getState2().setRemainingTime(no_ticks_needed);
                    nextQuery.setStatus("State2: WAN Transit");

                    nextQuery.getState2().setRemainingTime(nextQuery.getState2().getRemainingTime() - 1);

                } else if (nextQuery.getStatus().equals("State2: WAN Transit")) {
//                     System.out.println(queryCounter+")---in WAN state2  "+nextQuery.getState2().getRemainingTime());
                    if (nextQuery.getState2().getRemainingTime() == 1) {
                        nextQuery.getState2().setRemainingTime(0);
                        nextQuery.setStatus("State2: done");
                        nextQuery.getState2().setState2_EndTime(ticks);

//                        log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 2 start time " +
//                        nextQuery.getState2().getState2_Starttime() + " end time " + nextQuery.getState2()
//                                .getState2_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 2 start time " +
                                nextQuery.getState2().getState2_Starttime() + " end time " + nextQuery.getState2()
                                .getState2_EndTime());

                    }
                    else  nextQuery.getState2().setRemainingTime(nextQuery.getState2().getRemainingTime() - 1);
                }
            }
        }

    }

    private void check_State3(int ticks){
        int noOfQueries = query_responseTime.size();
        double queryInterArrivalTime = query_responseTime.get(noOfQueries-1).getStartTime()/(double)noOfQueries; //average arrival time


        for (int queryCounter =0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);
            ArrayList<SubjectQuerySegment> toAddToCache = new ArrayList<>();

            if ((nextQuery.getStatus().equals("State2: done")) || (nextQuery.getStatus().startsWith("State3:"))) {

                if (nextQuery.getStatus().equals("State2: done") && (nextQuery.getState2().getState2_EndTime() < ticks)) {
                    Cache_State3 state3 = new Cache_State3();
                    state3.setState3_Starttime(ticks);
                    int noOfDatabasesNeeded = 0;
                    int[] segmentsFound= new int[6];
                    nextQuery.setState3(state3);

                    for (int i = 0; i < no_of_DataServers; i++) {

                        int noOfSegments =  q[queryCounter][i].size();
                        nextQuery.getState3().setTotalSegmentsNeeded(noOfSegments,i);

                        if (noOfSegments >0) noOfDatabasesNeeded++;
                        for (int j = 0; j < noOfSegments; j++) {

                            if (cache_dataServers[i].isSegmentInCache(q[queryCounter][i].get(j))) {
                                segmentsFound[i]++;
                                nextQuery.getState3().getSegmentsFound()[i].add(q[queryCounter][i].get(j));
                              //  System.out.println("Segment is found in cache " + q[queryCounter][i].get(j));

                            }
                            else {
                               // System.out.println(nextQuery.getState3().getSegmentsNeeded()[i].size());
                                nextQuery.getState3().getSegmentsNeeded()[i].add(q[queryCounter][i].get(j));
                                toAddToCache.add(q[queryCounter][i].get(j)) ;
                              //  cache_dataServers[i].addNodetoCache(q[queryCounter][i].get(j));
                            }
                        }
                        for (int j = 0; j < toAddToCache.size() ; j++) {
                         //   System.out.println("Adding node to cache " + toAddToCache.get(j));
                            cache_dataServers[i].addNodetoCache(toAddToCache.get(j));
                        }
                        toAddToCache.clear();


                      /*  if (nextQuery.getState3().getSegmentsNeeded()[i].size() > 0) {

                            nextQuery.setStatus("State3: waiting for Database Reply");
                            nextQuery.getState3().setState4_waiting(ticks,i);
                        }  */

                    }
                   // if(!nextQuery.getStatus().equals("State3: waiting for Database Reply"))
                    nextQuery.setStatus("State3: processing");

                }
                else if (nextQuery.getStatus().equals("State3: processing"))  {
                    nextQuery.setStatus("State3: done");


                    nextQuery.getState3().setState3_EndTime(ticks);
//                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 3 " +
//                    "start time " +
//                            nextQuery.getState3().getState3_Starttime() + " end time " + nextQuery.getState3()
//                            .getState3_EndTime());
                    System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 3 " +
                            "start time " +
                            nextQuery.getState3().getState3_Starttime() + " end time " + nextQuery.getState3()
                            .getState3_EndTime());


                }

            }
        }

    }

    private void check_State4(int ticks)  {
        int noOfQueries = query_responseTime.size();

        for (int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);

            if ((nextQuery.getStatus().equals("State3: done")) || (nextQuery.getStatus().startsWith("State4:"))) {


              //calculate  query transfer on LAN time + database server time +data transfer back time
                int totalSegments = 0;

                for (int i = 0; i < 6; i++) {
                   totalSegments += nextQuery.getState3().getTotalSegmentsNeeded(i);
                }
                double dataSizePerSegment = nextQuery.getState1().getExpressionTree().getSubQueries().size()*  10.0/
                        totalSegments;


                if(nextQuery.getStatus().equals("State3: done")&& (nextQuery.getState3().getState3_EndTime() < ticks)) {

                    LANQuerytransfer_State4 state4 = new LANQuerytransfer_State4();
                    state4.setState4_Starttime(ticks);

                    for (int i = 0; i < no_of_DataServers; i++) {

                        //   if (nextQuery.getState3().getState4_waiting(i) <ticks) {

                        nextQuery.setState4(state4);
                        //calculate  query transfer on LAN time
                        double loadFactor;

                        if (noOfQueries > LAN_QUERY_LOAD_LIMIT) loadFactor = LAN_HIGH_LOADFACTOR;
                        else loadFactor = LAN_LOW_LOADFACTOR;
                        double time = networkTopography.getLAN_Delay(loadFactor, 0.001);

                        double LANTransferTime = Math.ceil(time / 1000);

                        // database server time

                        dataServers[i].setEntryQueue(nextQuery.getState3().getSegmentsNeeded()[i]);

                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        BlockingQueue<QuerySegment_ExecutionTime> returnQueue = dataServers[i].getReturnQueue();

                        double[] max_time = new double[6];
                        for (int j = 0; j < 6; j++) {
                            max_time[j] = 0;
                        }
                        for (int j = 0; j < returnQueue.size(); j++) {
                            QuerySegment_ExecutionTime qet = null;
                            try {
                                qet = returnQueue.take();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if (max_time[i] < qet.getExecutionTime()) {
                                max_time[i] = qet.getExecutionTime();
                            }
                        }

                        //data transfer back time

                        double datasizeInDB = dataSizePerSegment * (nextQuery.getState3().getSegmentsNeeded()[i].size());

                        //System.out.println(queryCounter+ " Here in state4 1 "+datasizeInDB);
                        if ((noOfQueries > LAN_QUERY_LOAD_LIMIT) || (datasizeInDB >= LAN_HEAVYLOAD))
                            loadFactor = LAN_HIGH_LOADFACTOR;
                        else loadFactor = LAN_LOW_LOADFACTOR;

                        double LANtimeToBackcache = (int) Math.ceil(networkTopography.getLAN_Delay(loadFactor,
                                datasizeInDB) / 1000);

                        double totalTicks = LANTransferTime + max_time[i] + LANtimeToBackcache;
                      //  System.out.println("total ticks "+totalTicks);

                        nextQuery.getState4().setTimeNeededPerDataServer(totalTicks, i);
                        // data obtained from each dataserver
                        nextQuery.getState4().setDataSizeInDataServer(nextQuery.getState3().getTotalSegmentsNeeded(i)
                                * dataSizePerSegment, i);
                    }
                    double totaltimeNeeded = 0;
                    for (int i = 0; i < 6; i++) {
                        totaltimeNeeded += nextQuery.getState4().getTimeNeededPerDataServer(i);
                    }

                    nextQuery.getState4().setTimeSpentInState4(totaltimeNeeded);
                    nextQuery.getState4().setRemainingTime(totaltimeNeeded - 1);
                    nextQuery.setStatus("State4: Started transferring Data from cache");

                    if (nextQuery.getState4().getRemainingTime() == 0) {
                        nextQuery.getState4().setState4_EndTime(ticks);


                        nextQuery.setStatus("State4: done");

 //                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 4
                        // start time " +nextQuery.getState4().getState4_Starttime() + " end time " + nextQuery.getState4()
//                                .getState4_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 4 " +
                                "start time " +
                                nextQuery.getState4().getState4_Starttime() + " end time " + nextQuery.getState4()
                                .getState4_EndTime());
                    }

                    else nextQuery.setStatus("State4: processing");
                }

               else if(nextQuery.getStatus().equals("State4: processing")){
                   // System.out.println("*******"+nextQuery.getState4().getRemainingTime());
                    nextQuery.getState4().setRemainingTime(nextQuery.getState4().getRemainingTime() - 1);
                    if (nextQuery.getState4().getRemainingTime() == 0) {

                        nextQuery.getState4().setState4_EndTime(ticks);

                        nextQuery.setStatus("State4: done");

                        //                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 4
                        // start time " +
//                        nextQuery.getState4().getState4_Starttime() + " end time " + nextQuery.getState4()
//                                .getState4_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 4 " +
                                "start time " +
                                nextQuery.getState4().getState4_Starttime() + " end time " + nextQuery.getState4()
                                .getState4_EndTime());
                    }
                }

            }
        }
    }

    private void check_State5(int ticks){
        int noOfQueries = query_responseTime.size();

        for (int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);

            if ((nextQuery.getStatus().equals("State4: done")) || (nextQuery.getStatus().startsWith("State5:"))) {

                if(nextQuery.getStatus().equals("State4: done")&&(nextQuery.getState4().getState4_EndTime() < ticks)) {

                    InterWANDataTransfers_State5 state5 = new InterWANDataTransfers_State5();
                    state5.setState5_Starttime(ticks);
                    nextQuery.setState5(state5);

                    double interWANTransferTime = 0;

                    for (int i = 0; i < no_of_DataServers - 1; i++) {
                        double datasizeInDB = nextQuery.getState4().getDataSizeInDataServer(i);


                        double loadFactor;

                        if ((noOfQueries > WAN_QUERY_LOAD_LIMIT) || (datasizeInDB > WAN_HEAVYLOAD))
                            loadFactor = WAN_HIGH_LOADFACTOR;
                        else loadFactor = WAN_LOW_LOADFACTOR;

                        double noOfTicksNeeded = (int) Math.ceil(networkTopography.getWAN_Delay(loadFactor,
                                datasizeInDB) / 1000);



                        interWANTransferTime += noOfTicksNeeded;
                        System.out.println("from state 5 at data server no "+(i+1)+" " +
                                ""+datasizeInDB+"-"+noOfTicksNeeded+"--"+interWANTransferTime);
                    }

                    nextQuery.getState5().setTimeInState5(interWANTransferTime);
                    nextQuery.getState5().setRemainingTime(interWANTransferTime-1);

                    if (nextQuery.getState5().getRemainingTime() == 0){

                        nextQuery.getState5().setState5_EndTime(ticks);


                        nextQuery.setStatus("State5: done");

 //                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 5 " +
//                        "start time " +
//                                nextQuery.getState5().getState5_Starttime() + " end time " + nextQuery.getState5()
//                                .getState5_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 5 " +
                                "start time " +
                                nextQuery.getState5().getState5_Starttime() + " end time " + nextQuery.getState5()
                                .getState5_EndTime());
                    }

                    else nextQuery.setStatus("State5: processing");
                }

                else if(nextQuery.getStatus().equals("State5: processing")){
                    // System.out.println("*******"+nextQuery.getState5().getRemainingTime());
                    nextQuery.getState5().setRemainingTime(nextQuery.getState5().getRemainingTime() - 1);
                    if (nextQuery.getState5().getRemainingTime() == 0) {

                        nextQuery.getState5().setState5_EndTime(ticks);

                        nextQuery.setStatus("State5: done");

 //                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 5 " +
//                        "start time " +nextQuery.getState5().getState5_Starttime() + " end time " + nextQuery.getState5()
//                                .getState5_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 5 " +
                                "start time " +
                                nextQuery.getState5().getState5_Starttime() + " end time " + nextQuery.getState5()
                                .getState5_EndTime());
                    }

                }



            }
        }
    }

    private void check_State6(int ticks){
        int noOfQueries = query_responseTime.size();

        for (int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);

            if ((nextQuery.getStatus().equals("State5: done")) || (nextQuery.getStatus().startsWith("State6:"))) {

                if (nextQuery.getStatus().equals("State5: done") && (nextQuery.getState5().getState5_EndTime() < ticks)) {

                    ReplyUser_State6 state6 = new ReplyUser_State6();
                    state6.setState6_Starttime(ticks);
                    nextQuery.setState6(state6);

                    double datasizeInDB = nextQuery.getState1().getExpressionTree().getSubQueries().size()*10;

                    double loadFactor;

                    if ((noOfQueries > WAN_QUERY_LOAD_LIMIT) || (datasizeInDB > WAN_HEAVYLOAD))
                        loadFactor = WAN_HIGH_LOADFACTOR;
                    else loadFactor = WAN_LOW_LOADFACTOR;


                    double noOfTicksNeeded = (int) Math.ceil(networkTopography.getWAN_Delay(loadFactor,
                            datasizeInDB) / 1000);

                 //   System.out.println("**"+noOfTicksNeeded);

                    state6.setTimeSpentInState6(noOfTicksNeeded);
                    state6.setRemainingTime(noOfTicksNeeded-1);
                    if (nextQuery.getState6().getRemainingTime() == 0){

                        nextQuery.getState6().setState6_EndTime(ticks);


                        nextQuery.setStatus("Query completed");
                        queriesDone++;
                        //                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + "
                        // STATE 6 " +
//                        "start time " +
//                                nextQuery.getState6().getState6_Starttime() + " end time " + nextQuery.getState6()
//                                .getState6_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 6 " +
                                "start time " +
                                nextQuery.getState6().getState6_Starttime() + " end time " + nextQuery.getState6()
                                .getState6_EndTime());

                        nextQuery.setEndTime(nextQuery.getState6().getState6_EndTime());
                    }

                    else nextQuery.setStatus("State6: processing");


                }
                else if(nextQuery.getStatus().equals("State6: processing")){
                    // System.out.println("*******"+nextQuery.getState6().getRemainingTime());
                    nextQuery.getState6().setRemainingTime(nextQuery.getState6().getRemainingTime() - 1);
                    if (nextQuery.getState6().getRemainingTime() == 0) {

                        nextQuery.getState6().setState6_EndTime(ticks);

                        nextQuery.setStatus("Query completed");
                        queriesDone++;
                        //                    log.info(queryCounter + ") " + nextQuery.getQSP().getQueryID() + "
                        // STATE 6 " + "start time " +nextQuery.getState6().getState6_Starttime() + " end time " + nextQuery.getState6()
//                                .getState6_EndTime());
                        System.out.println(queryCounter + ") " + nextQuery.getQSP().getQueryID() + " STATE 6" +
                                "start time " +
                                nextQuery.getState6().getState6_Starttime() + " end time " + nextQuery.getState6()
                                .getState6_EndTime());
                        nextQuery.setEndTime(nextQuery.getState6().getState6_EndTime());
                    }

                }


            }
        }
    }

    public void runThroughStates()  {

        for (int ticks = 0;  ; ticks++) {

            check_State1(ticks);
            check_State2(ticks);
            check_State3(ticks);
            check_State4(ticks);
            check_State5(ticks);
           check_State6(ticks);


            if(queriesDone == NUMBER_OF_QUERIES) break;

        }


        for (int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);

            if (nextQuery.getStatus().equals("Query completed")) {

                if (nextQuery.getState6() == null){
                    System.out.println(queryCounter + ") repeated query");
                }
                System.out.println(queryCounter + ") QUERY ID: " + nextQuery.getQSP().getQueryID() + " Start time :" + nextQuery
                        .getStartTime() + " EndTime: " + nextQuery.getEndTime());



            }
        }

        try {

            String sFileName = "//home//santhilata//Desktop//Output//outFile_dataServer.csv";
            File outFile1 = new File(sFileName);
            FileWriter writer = new FileWriter(outFile1);
            //to append into an existing file use the following
            // FileWriter writer = new FileWriter(outFile1, true);
            // FileWriter writer = new FileWriter(sFileName, true);
            writer.flush();
            writer.append("QueryID");
            writer.append(',');

            //title
            for (int i = 1; i < 10 ; i++) {

                writer.append("S"+i+"_Start");
                writer.append(',');
                writer.append("S"+i+"_End");
                writer.append(',');
            }

            writer.append("Query_Start");
            writer.append(',');
            writer.append("Query_End");

            writer.append('\n');


            for (int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
                Query_Response_DataServer nextQuery = query_responseTime.get(queryCounter);
                writer.append(nextQuery.getQSP().getQueryID());

                writer.append(',');

                writer.append(nextQuery.getState1().getState1_EndTime() + "");
                writer.append(',');

                writer.append(nextQuery.getState1().getState1_EndTime() + "");
                writer.append(',');

                writer.append(nextQuery.getState2().getState2_Starttime() + "");
                writer.append(',');
                writer.append(nextQuery.getState2().getState2_EndTime() + "");
                writer.append(',');

                writer.append(nextQuery.getState3().getState3_Starttime() + "");
                writer.append(',');
                writer.append(nextQuery.getState3().getState3_EndTime() + "");
                writer.append(',');

               writer.append(nextQuery.getState4().getState4_Starttime() + "");
                writer.append(',');
                writer.append(nextQuery.getState4().getState4_EndTime() + "");
                writer.append(',');

                writer.append(nextQuery.getState5().getState5_Starttime() + "");
                writer.append(',');
                writer.append(nextQuery.getState5().getState5_EndTime() + "");
                writer.append(',');


                if(nextQuery.getState6()!= null) {
                    writer.append(nextQuery.getState6().getState6_Starttime() + "");
                    writer.append(',');
                    writer.append(nextQuery.getState6().getState6_EndTime() + "");
                    writer.append(',');

                }
                else{
                    for (int i = 0; i < 4; i++) {


                        writer.append("");
                        writer.append(',');
                        writer.append("");
                        writer.append(',');

                    }
                }
                writer.append(nextQuery.getStartTime() + "");
                writer.append(',');
                writer.append(nextQuery.getEndTime() + "");

                writer.append('\n');


                query_responseTime.remove(queryCounter);
                queryCounter--;
                noOfQueries = query_responseTime.size();

            }

            writer.flush();
            writer.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {

        CacheDataServerMain cdm = new CacheDataServerMain(NUMBER_OF_QUERIES);
        cdm.startTime_Query_Distributions(NUMBER_OF_QUERIES,QUERY_INTERARRIVAL_DISTRIBUTION,
                QUERY_REPETITION_DISTRIBUTION);
        double queryInterArrivalTime = query_responseTime.get(NUMBER_OF_QUERIES-1).getStartTime()/(double)NUMBER_OF_QUERIES; //average arrival time at data servers

        for (int i = 0; i < no_of_DataServers; i++) {
            cdm.dataServers[i].setQueryInterArrivalTime(queryInterArrivalTime);
        }

        for (int i = 0; i < query_responseTime.size(); i++) {
            //log.info(i+") "+query_responseTime.get(i).getQueryNumber()+" *** "+query_responseTime.get(i).getQSP().getQueryExpression());
            System.out.println(i+") "+query_responseTime.get(i).getQueryNumber()+" *** "+query_responseTime.get(i).getQSP().getQueryExpression());

        }

        cdm.runThroughStates();
    }
}
