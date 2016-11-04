package project.MainClasses;


import SimpleGui.ConfigurationClass;
import org.apache.log4j.Logger;

import project.DatabaseInfo.DatabaseServer;
import project.MiddlewareEnvironment.Cache;
import project.MiddlewareEnvironment.Container;
import project.MiddlewareEnvironment.QueryIndexFiles.*;
import project.MiddlewareEnvironment.CacheMaintenance.EA.SuperQueryIndex;
import project.Network.NetworkConstants;
import project.Network.NetworkTopography;
import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.SubjectQuerySegment;
import project.ResponseTimeSimulation.CommunityCache_Response.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import project.UserEnvironment.QuerySegment_ExecutionTime;

/**
 * Created by santhilata on 07/04/16.
 * SimpleMain creates distributed environment architecture first
 *
 */
public class SimpleMain implements CacheProperties, NetworkConstants{
    protected final Logger log = Logger.getLogger(SimpleMain.class);
    private  ConfigurationClass cfgClass ;
    private int numbLANs;
    private int numbDatabases;
    private int cacheUnitsPerLAN;
    private int userGroupsPerLAN;

    private Container[] containers;
    private QueryIndex[] queryIndex; // each container has one query index at present



    ArrayList<Query_Response> query_responseTime ;
    static NetworkTopography networkTopography = new NetworkTopography();
    DatabaseServer[] dataServers ;
    static  int queriesDone=0;
    String status;
    int cacheMaintenanceStartTime;
    int cacheMaintenanceEndTime;
    int noOfQueriesPresentOnNetwork;
    int noOfRepeatedQueries=0;

    //temporary variables


    double totalDataFound=0;
    double totalDataNotFound=0;
    double averageDataFound=0;
    double averageDataNotFound=0.0;
    int number_addedQueries = 0;
    int number_deletedQueries = 0;
    int number_cacheAccesses = 0;
    int number_cacheHits = 0;

    SuperQueryIndex sqda;


    public int getCacheMaintenanceStartTime() {
        return cacheMaintenanceStartTime;
    }

    public void setCacheMaintenanceStartTime(int cacheMaintenanceStartTime) {
        this.cacheMaintenanceStartTime = cacheMaintenanceStartTime;
    }

    public int getCacheMaintenanceEndTime() {
        return cacheMaintenanceEndTime;
    }

    public void setCacheMaintenanceEndTime(int cacheMaintenanceEndTime) {
        this.cacheMaintenanceEndTime = cacheMaintenanceEndTime;
    }

    public QueryIndex[] getQueryIndex() {
        return queryIndex;
    }

    public void setQueryIndex(QueryIndex[] queryIndex) {
        this.queryIndex = queryIndex;
    }

    public Container[] getContainers() {
        return containers;
    }

    /**
     * Architecture contains several containers (LANs).
     * Each LAN has a query index
     * Queryindexes are implemented as peer to peer
     */
    public SimpleMain(){
        //setting architecture
        XStream xstream = new XStream(new StaxDriver());
        this.cfgClass = (ConfigurationClass) xstream.fromXML(configFile);
        this.numbLANs = this.cfgClass.getDistributedEnvironment().getNumbLANs();
        this.numbDatabases = this.cfgClass.getDistributedEnvironment().getDatabaseSchema().length;
        this.cacheUnitsPerLAN = this.cfgClass.getDistributedEnvironment().getCacheUnitsPerLAN();
        this.userGroupsPerLAN = this.cfgClass.getDistributedEnvironment().getUserGroupsPerLAN();

        this.sqda = new SuperQueryIndex();

        //create queryIndex[] and add caches to query Index
        for (int i = 0; i < numbLANs; i++) {
            queryIndex[i] = new QueryIndex();
            HashSet<Cache> caches = new HashSet<>();
            for (int j = 0; j < cacheUnitsPerLAN; j++) {
                Cache cache = new Cache("Cache_"+(j+1));
                cache.setSelfIndex(queryIndex[i]);
                caches.add(cache);
            }
            queryIndex[i].setCache(caches);
        }

        //setting up containers and their query indexes
        containers = new Container[numbLANs];
        int numULoc = numbLANs *userGroupsPerLAN;
        String[] containerNames = new String[numbLANs];

        for (int i = 0; i < numContainers; i++) {
                containerNames[i] = "Container_"+(i+1);
            containers[i] = new Container(containerNames[i]);
            containers[i].setQueryIndex(queryIndex[i]);
            queryIndex[i].setResidentContainer(containers[i]);
            containers[i].setCacheSet( queryIndex[i].getCache());

            //set address to cache units
            Iterator<Cache> itr = containers[i].getCacheSet().iterator();
            while (itr.hasNext()){
                Cache cache = itr.next();
                cache.setCacheAddress(containers[i].getContainer_name());
            }
        }

        //for the peer to peer visibility, each queryindex can see other containers
        for (int i = 0; i < queryIndex.length ; i++) {
            queryIndex[i].setNeighbouringContainers(containers);
            sqda.getQueryIndices().add(queryIndex[i]);//
        }

        sqda.setNeighbouringContainers(containers);

        //setting and starting remote data servers in a container other than user containers
        dataServers  = new DatabaseServer[numbDatabases];
        for (int i = 0; i < numbDatabases ; i++) {
            dataServers[i] = new DatabaseServer("db" + (i + 1));
            dataServers[i].setAddress_container("Container_" + (i + 999));
        }


    }

    /**
     * For smaller input files with less number of queries
     * @param QueryExpressionFile
     * @return
     * @throws IOException
     */
    private  void readQueriesFromFile(File QueryExpressionFile) throws IOException{
        ArrayList<String> queries = new ArrayList<>();
        query_responseTime = new ArrayList<>();

        BufferedReader in = new BufferedReader(new FileReader(QueryExpressionFile));
        while(true){
            String str = in.readLine();
            if(str == null) break;

            String[] queryFragments = str.split(Pattern.quote("@"));
            Query_Response qr = new Query_Response(Integer.parseInt(queryFragments[0]),queryFragments[2],
                    queryFragments[1]);

            query_responseTime.add(qr);
        }

        return ;
    }

    /**
     *   Nine states below are the establishment of 9 states within a cache architecture
     *   State-1 : read query
     *   State-2: carry query to local query index
     *   State-3: Query Index fragments query, checks with local cache
     *   State-4: Send results back to user
     *   State-5: Checks how much data has been found and what is remaining
     *   State-6: Send data on WAN to data server
     *   State-7: Finding at Data server
     *   State-8: results back on WAN
     *   State-9: aggregate and reply user
     */
    public  void checkState1(int ticks)  {
        int noOfQueries = query_responseTime.size();

        for(int queryCounter = 0; queryCounter < noOfQueries; queryCounter++) {
            Query_Response nextQuery = query_responseTime.get(queryCounter);
           // nextQuery.setQueryLocation("QueryLocation_"+(new Random().nextInt(no_of_cacheUnitsPerContainer+1)));

            if (nextQuery.getStatus().equals("State1")) {

                int nextQueryTime = query_responseTime.get(queryCounter).getStartTime();
                int check_cacheMaintenance= nextQueryTime%CACHE_MAINTENANCE_PERIOD;

                if (nextQueryTime == ticks || (nextQueryTime>= getCacheMaintenanceStartTime() && nextQueryTime <=
                        getCacheMaintenanceEndTime())) {

                    //open the state 1 of query to be entered

                    StartState_State1 state1 = new StartState_State1();

                    state1.setExpressionTree(new ExpressionTree(nextQuery.getQSP()));
                    state1.getExpressionTree().setTime_queried(ticks);
                    state1.getExpressionTree().setQspLocation(nextQuery.getQueryLocation());// setting the query location drom where it is generated


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
    public void checkState3(int ticks){
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

                    int userLocation=0;
                    if (selectedCacheStoreCriteria.equals(NEARBY_CRITERIA) || selectedCacheStoreCriteria.equals(DATASIZE_CRITERIA)) {
                        userLocation = Character.getNumericValue(nextQuery.getQueryLocation().charAt(4));
                        Container container = getContainerByName(userLocation);

                        QueryIndex queryIndex = container.getQueryIndex();

                        queryIndex.searchQueryIndex(nextQuery.getState1().getExpressionTree());


                        boolean queryReturned = false;

                        while (!queryReturned) {
                            ArrayList<Cache_Reply> receivedList = queryIndex.sendQueryBack();
                            Cache_Reply tempCacheReply = null;
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

                                if (tempCacheReply.getReplyStatus().equals(FULLY_FOUND)) {
                                    nextQuery.setFoundInCache(true);
                                    log.info("  from clm class data size found fully found " + tempCacheReply.getDataSizeFound());
                                    nextQuery.setRemainingQueryFull(tempCacheReply.getPartialExpressionTree());

                                    log.info(queryCounter + ") " + nextQuery.getDataSizeFound() + " " + nextQuery.getRemaingDataNeeded());
                                } else if (tempCacheReply.getReplyStatus().equals(NOT_FOUND)) {

                                    log.info(queryCounter + ") in not found case");
                                    nextQuery.setRemainingQueryPartial(tempCacheReply.getPartialExpressionTree());
                                    nextQuery.setFoundInCache(false);
                                } else if (tempCacheReply.getReplyStatus().equals(PARTIALLY_FOUND)) {
                                    log.info(queryCounter + ") in Partial found case");
                                    log.info("  from clm class data size found " + tempCacheReply.getDataSizeFound
                                            ());
                                    nextQuery.setFoundInCache(false);

                                    nextQuery.setRemainingQueryPartial(tempCacheReply.getPartialExpressionTree());

                                    log.info(queryCounter + ") " + nextQuery.getDataSizeFound() + " " + nextQuery.getRemaingDataNeeded());
                                }
                            }

                        }
                    }
                    else if (selectedCacheStoreCriteria.equals(GLOBAL_DATA_CRITERIA)){
                        //search query globally
                        userLocation = Character.getNumericValue(nextQuery.getQueryLocation().charAt(4));
                        getNeighbours(userLocation,userLocation+1,containers.length);
                        for (int i = 0; i < neighbourList.size(); i++) {
                            Container container = getContainerByName(i);
                            QueryIndex queryIndex = container.getQueryIndex();

                            queryIndex.searchQueryIndex(nextQuery.getState1().getExpressionTree());


                            boolean queryReturned = false;

                            while (!queryReturned) {
                                ArrayList<Cache_Reply> receivedList = queryIndex.sendQueryBack();
                                Cache_Reply tempCacheReply = null;
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

                                    if (tempCacheReply.getReplyStatus().equals(FULLY_FOUND)) {
                                        nextQuery.setFoundInCache(true);
                                        log.info("  from clm class data size found fully found " + tempCacheReply.getDataSizeFound());
                                        nextQuery.setRemainingQueryFull(tempCacheReply.getPartialExpressionTree());

                                        log.info(queryCounter + ") " + nextQuery.getDataSizeFound() + " " + nextQuery.getRemaingDataNeeded());
                                    } else if (tempCacheReply.getReplyStatus().equals(NOT_FOUND)) {

                                        log.info(queryCounter + ") in not found case");
                                        nextQuery.setRemainingQueryPartial(tempCacheReply.getPartialExpressionTree());
                                        nextQuery.setFoundInCache(false);
                                    } else if (tempCacheReply.getReplyStatus().equals(PARTIALLY_FOUND)) {
                                        log.info(queryCounter + ") in Partial found case");
                                        log.info("  from clm class data size found " + tempCacheReply.getDataSizeFound
                                                ());
                                        nextQuery.setFoundInCache(false);

                                        nextQuery.setRemainingQueryPartial(tempCacheReply.getPartialExpressionTree());

                                        log.info(queryCounter + ") " + nextQuery.getDataSizeFound() + " " + nextQuery.getRemaingDataNeeded());
                                    }
                                }

                                if (queryReturned){
                                    //find a cache to store unsaved queries
                                    String reply = tempCacheReply.getReplyStatus();
                                    if(reply.equals(NOT_FOUND) || reply.equals(PARTIALLY_FOUND)) {
                                        //for first time queries
                                        if (queryIndex.getUnSavedQueries().size() != 0)
                                            sqda.getInterestedQueries().addAll(queryIndex.getUnSavedQueries());
                                    }

                                    break;// query is found in one of the containers
                                }


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

    /**
     * Following is an important function to run all above 9 states and note down details
     * @param NUMBER_OF_QUERIES
     * @param
     * @param maintenanceCriteria
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public double runQueriesThroughStates(int NUMBER_OF_QUERIES, String maintenanceCriteria) throws InterruptedException, IOException {

        double total_responseTime=0;
        String fileOut = null;

        File outFile11 = null;
        FileWriter writer1 = null;

        // File handling
        switch(maintenanceCriteria){
            case TIME_THRESHOLD_ONLY: {
                fileOut = "//home//santhilata//Dropbox//CachelearningResults//Cache_stability_TIME_"+TIME_THRESHOLD
                        +"_"+NUMBER_OF_QUERIES+"_"+searchCriteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries, Time Threshold, no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            case FREQUENCY_THRESHOLD_ONLY :{
                fileOut = "//home//santhilata//Dropbox//CachelearningResults//Cache_stability_FREQUENCY_"+FREQUENCY_THRESHOLD+"_"+NUMBER_OF_QUERIES+"_"+searchCriteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries, Frequency Threshold, no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            case COMBINED_ALGO_MAINTENANCE: {
                fileOut = "//home//santhilata//Dropbox//CachelearningResults//Cache_stability_COMBINED_"+TIME_THRESHOLD+"_"+FREQUENCY_THRESHOLD+"_"+NUMBER_OF_QUERIES+"_"+searchCriteria+".csv";

                outFile11 = new File(fileOut);
                writer1 = new FileWriter(outFile11);

                writer1.flush();
                writer1.append("Ticks, Queries, Time Threshold, Frequency Threshold, no.of Added Queries, no.of deleted queries, cache hits, cache accesses,cache data size, cache deleted data");
                writer1.append('\n');
                break;
            }
            default: break;
        }

        for (int ticks = 1; ;ticks++) {

            if (ticks % CACHE_MAINTENANCE_PERIOD==0){

                this.setCacheMaintenanceStartTime(ticks);
                this.setCacheMaintenanceEndTime(ticks+CACHE_MAINTENANCE_DURATION);

                QueryIndex queryIndex  = this.containers[0].getQueryIndex();

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
                    default: break;
                }

                ticks +=CACHE_MAINTENANCE_DURATION;
            }

            //run all 9 states for each query
            checkState1(ticks);
            checkState2(ticks);
            checkState3(ticks);
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
     * the following private method is to expand visibility of neighbours by one on either side
     */
    ArrayList neighbourList = new ArrayList();
    private void getNeighbours(int curr,int next, int Max){

        if  (curr<0  || next>Max )
            return;
        neighbourList.add(curr);
        neighbourList.add(next);
        // System.out.println(curr+" "+next);
        getNeighbours(curr-1,next+1,Max);

    }
    private Container getContainerByName(int i){
        Container container = null;

        for (int j = 0; j < containers.length; j++) {
            String name = containers[j].getContainer_name();
            if (i == name.charAt(10)){
                container =containers[j];
                break;
            }
        }
        return container;
    }


    public void collectDataIntoFile(File metadataFile) throws IOException {
        sqda.setStatisticsFile(metadataFile);
        sqda.generateMetaDataStatisticsPerEpoch();

    }



    /**
     * The following function is to do with overall cache maintenance
     */
    public void cacheMaintenance(){
        //cache maintenance could be in varied time intervals.
        //A very small interval leads to online maintenance
        //
        //1.take unsaved queries list and find_A_cache globally from sqda and insert them
        //2.
    }



    public static void main(String[] args) throws IOException {
        System.out.println("How many epochs you want to scan?");
        int numFiles = (new Scanner(System.in)).nextInt();

        for (int i = 0; i < numFiles; i++) {
            File inputFile = new File(INPUT_FILE_Part1+i+INPUT_FILE_Part2);

            SimpleMain simpleMain = new SimpleMain();//sets Architecture
            simpleMain.readQueriesFromFile(inputFile);   // reading queries, arrival times and user locations

            // To collect meta data
            File metadataFile = new File(METADATA_FILE_Part1+i+METADATA_FILE_Part2);
            //DistributedCachedQueries dcq = new DistributedCachedQueries();
            //dcq.setStatisticsFile(metadataFile);
            //dcq.generateMetaDataStatisticsPerEpoch(simpleMain.getQueryIndex());
            simpleMain.collectDataIntoFile(metadataFile);


        }





        // Set queries


    }
}
