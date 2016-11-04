package project.MiddlewareEnvironment.QueryIndexFiles;

import java.util.ArrayList;

import project.MainClasses.CacheProperties;
import project.MiddlewareEnvironment.Container;
import project.MiddlewareEnvironment.Cache;
import project.QueryEnvironment.ExpressionTree;

import project.QueryEnvironment.Query_Subj_Predicate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Last edited on 30/03/2015
 * This class creates a query pattern store to check for the thresholds (frequency and time)
 * This acts as an index to cache unit.
 * Query index stores the every query segment.
 * Still to be developed
 */
public  class QueryIndex implements CacheProperties{

    static Graph queryIndexGraph ;

    HashSet<Cache> cacheSet ;
    private int no_ofThreads;
    private ThreadPool threadPool;
    private Container residentContainer;
    Container[] neighbouringContainers; // all containers list  - not using now
    ArrayList<ExpressionTree> unSavedQueries = new ArrayList<>(); // interested but unsaved queries

    public QueryIndex(){
        this.queryIndexGraph  = new Graph();
        this.getQueryIndexGraph().setMyQueryIndex(this);
        no_ofThreads = 10;
        threadPool =   new ThreadPool(no_ofThreads);

    }

    public QueryIndex(HashSet<Cache> cache){
        this.queryIndexGraph  = new Graph();
        this.getQueryIndexGraph().setMyQueryIndex(this);

        this.cacheSet = cache;
        no_ofThreads = 10;
        threadPool =   new ThreadPool(no_ofThreads);
       // threadPool.addTask(new ThreadService());

    }

    public QueryIndex(Graph queryIndexGraph,Cache cache){
        this.cacheSet.add(cache);
        this.queryIndexGraph = queryIndexGraph;
        this.getQueryIndexGraph().setMyQueryIndex(this);
    }

    public Graph getQueryIndexGraph() {
        return queryIndexGraph;
    }

    public void setQueryIndexGraph(Graph queryIndexGraph) {
        this.queryIndexGraph = queryIndexGraph;
    }

    public Container getResidentContainer() {
        return residentContainer;
    }

    public void setResidentContainer(Container residentContainer) {
        this.residentContainer = residentContainer;
    }

    public HashSet<Cache> getCache() {
        return cacheSet;
    }

    public void setCache(HashSet<Cache> cache) {
        this.cacheSet = cache;
    }

    public ArrayList<ExpressionTree> getUnSavedQueries() {
        return unSavedQueries;
    }

    public void setUnSavedQueries(ArrayList<ExpressionTree> unSavedQueries) {
        this.unSavedQueries = unSavedQueries;
    }

    public Container[] getNeighbouringContainers(){
        return neighbouringContainers;
    }
    public void setNeighbouringContainers(Container[] containers){
        this.neighbouringContainers = containers;
    }

    /**
     * Searches the Query index
     * and adds the new queries to query index
     * and also the partial queries from the part-matched queries
     * query_cl is the query to be searched in the query index
     */
   // public void searchQueryIndex(Query_Subj_Predicate qsp,String criteria){
    public void searchQueryIndex(ExpressionTree et){

       // ExpressionTree et = new ExpressionTree(qsp);

        entryQueue.add(et);
        threadPool.addTask(new ThreadService());
    }

    /**
     * to send the querylist back
     * @return
     */
    public synchronized ArrayList<Cache_Reply> sendQueryBack(){
        ArrayList<Cache_Reply> returnList = new ArrayList<>(returnQueue.size());
        returnList.addAll(returnQueue);

       return  returnList;
    }

    public synchronized void removeQueryFromReturnQueue(Cache_Reply cr){
        returnQueue.remove(cr);
    }

    //========================================TODO: FOLLOWING CODE IS FOR CACHE MAINTENANCE ===========================

    // the following two cache maintenance functions add or delete queries periodically after every cache maintenance period
    public UpdatedData updateQueryIndex_TimeLimit(int time_threshold,int ticks){
        UpdatedData  dataReceived =  getQueryIndexGraph().deleteExpiredQueries(time_threshold,ticks);
        return dataReceived;
    }

    public UpdatedData updateQueryIndex_frequencyLimit(int frequency_threshold,int ticks){
        UpdatedData  dataReceived ;
        dataReceived = getQueryIndexGraph().deleteLessFrequentQueries(frequency_threshold, ticks);
        return dataReceived;
    }

    //Following two cache maintenance functions add or delete queries as and when query appears and no place in the cache
    //TODO

    public int getNumber_addedQueries(){
        int number = getQueryIndexGraph().getNumber_addedQueries_atMaintenance();
        return number;
    }

    /**
     * The following function returns a cache from the cache set attached to this query Index
     * A new query will be inserted in that cache
     * Distance criteria , data size criteria and a combination are written at  the moment (16/09/2015).
     * Other cases may be added later
     *
     * @return
     */
    public Cache find_A_Cache( ExpressionTree expressionTree){
        Cache cache = null;
        int requiredDataSize = expressionTree.getNo_of_Nodes() * standard_Data_Unit_Size;
        String queryLocation = expressionTree.getQspLocation();

        int timeQueried = expressionTree.getTime_queried();
        
        switch (selectedCacheStoreCriteria){
            /**
             * NEARBY_CRITERIA is the greedy approach - Follow the leader
             * The simplest learning rule to try is to select (at the current step) the hypothesis that has the least loss over all past rounds. This algorithm is called Follow the leader, and is simply given by:

             In round t , set

             w_t = \operatorname*{arg\,min}_{w \in S} \sum_{i=1}^{t-1} v_i(w)

             Here, ties are broken arbitrarily. This method can thus be looked as a greedy algorithm.
             For the case of online quadratic optimization (where the loss function is v_t(w) = || w - x_t ||_2^2 ),
             one can show a regret bound that grows as \log(T) .[4] However, similar bounds cannot be obtained for
             the FTL algorithm for other important families of models like online linear optimization etc.
             To do so, one modifies FTL by adding regularisation.

             source: https://en.wikipedia.org/wiki/Online_machine_learning
             */
            case NEARBY_CRITERIA:{
                // just gets a cache within the container and size availability within the cache
                boolean cacheFound = false;
                char userLocation = queryLocation.charAt(4);
                for(Cache cache1: cacheSet){
                   if (userLocation == cache1.getCacheAddress().charAt(10)) {
                       if (cache1.getDataSizeAvailable() > requiredDataSize) {
                           cacheFound = true;
                           cache = cache1;
                           break;
                       }
                   }
                }
                if (!cacheFound){
                    System.out.println("No cache has free space within the same container. Hence data is discarded.");
                    unSavedQueries.add(expressionTree);
                }

                break;
            }
            /**
             * Simplest criteria. It places data on the first available cache within the container
             */
            case DATASIZE_CRITERIA: {
                boolean cacheFound = false;
                for (Cache cache1: cacheSet){
                    if (cache1.getDataSizeAvailable() > requiredDataSize){
                        cache = cache1;
                        break;
                    }
                }
                if (!cacheFound){
                    System.out.println("No cache has free space within the same container. Hence data is discarded.");
                    unSavedQueries.add(expressionTree);
                }
                break;
            }

            /**
             * This is a greedy method extended to other containers
             *  caches first within the container and size availability onto other nearby containers too
             *  nearby containers have the increased  visibility by one step at any given time
             */
              /*
            case GLOBAL_DATA_CRITERIA:{

                boolean cacheFound = false;
                char userLocation = queryLocation.charAt(4);
                for(Cache cache1: getCache()){
                    if (userLocation == cache1.getCacheAddress().charAt(10)) {
                        if (cache1.getDataSizeAvailable() > requiredDataSize) {
                            cacheFound = true;
                            cache = cache1;
                            break;
                        }
                    }
                }
                if (!cacheFound){
                    int uloc = Character.getNumericValue(userLocation);
                    getNeighbours(uloc,uloc+1,numContainers);
                    for (int i = 0; i < neighbourList.size() ; i++) {
                        Container container = getContainerByName(i);
                        for(Cache cache1: container.getCacheSet()){
                            if (cache1.getDataSizeAvailable() > requiredDataSize) {
                                cacheFound = true;
                                cache = cache1;

                                break;
                            }
                        }
                    }
                    if(!cacheFound){
                        System.out.println("No cache has free space in any container. Hence data is discarded.");
                    }
                }

                unSavedQueries.add(expressionTree);

                break;
            }
             */

            default: {
                unSavedQueries.add(expressionTree);
                break;
            }
        }

        return cache;
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

        for (int j = 0; j < neighbouringContainers.length; j++) {
            String name = neighbouringContainers[j].getContainer_name();
            if (i == name.charAt(10)){
                container = neighbouringContainers[j];
                break;
            }
        }
            return container;
    }

    private Cache getCacheByName(int i){
        Cache cache = null;
        Iterator<Cache> itr = cacheSet.iterator();
        while(itr.hasNext()){
            cache = itr.next();
            if (Character.getNumericValue(cache.getCacheName().charAt(6)) == i)
                return cache;
        }

        return cache;
    }

    //=============================== FOLLOWING CODE IS TO CREATE A MULTI-THREADED SERVICE AT CACHE ===============

    private BlockingQueue<ExpressionTree> entryQueue = new PriorityBlockingQueue<>() ;
  //  private BlockingQueue<Cache_Reply> returnQueue =  new PriorityBlockingQueue<>() ;
   private Vector<Cache_Reply> returnQueue =  new Vector<>() ;

    public BlockingQueue<ExpressionTree> getEntryQueue() {
        return entryQueue;
    }

    public void setEntryQueue(BlockingQueue<ExpressionTree> entryQueue) {
        this.entryQueue = entryQueue;
    }

   /**
    public BlockingQueue<Cache_Reply> getReturnQueue() {
        return returnQueue;
    }

    public void setReturnQueue(BlockingQueue<Cache_Reply> returnQueue) {
        this.returnQueue = returnQueue;
    }
     */
    public Vector<Cache_Reply> getReturnQueue() {
        return returnQueue;
    }

    public void setReturnQueue(Vector<Cache_Reply> returnQueue) {
        this.returnQueue = returnQueue;
    }

    private class ThreadPool {

        // A Priority blocking queue is maintained for the queue of queries instead of simple queue
        // This is just for the purpose of setting up some priority in future
        // An ordinary Priority queue implementation does not allow synchronisation on a single queue instance.
        // We need to  access a single queue instance from the database to insert queries from one end and --
        // -- concurrently retrieving the queries from the other end (or wherever)

        // private BlockingQueue<Query_ExecutionTime> entryQueue;
        // private BlockingQueue<Query_ExecutionTime> returnQueue;
        //  private int[] bitVector;

        private final int THREAD_ALLOCATION_TIME = 1;
        private final BlockingQueue<Runnable> workerQueue;
        private final Thread[] workerThreads;


        public ThreadPool(int numThreads) {
            workerQueue = new LinkedBlockingQueue<Runnable>();
            workerThreads = new Thread[numThreads];

            int i = 0;
            for (Thread t : workerThreads) {
                i++;
                t = new Worker("Pool Thread "+i);
                t.start();
            }
        }

        public void addTask(Runnable r){
            try {
                workerQueue.put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private class Worker extends Thread {

            public Worker(String name){
                super(name);
            }

            public void run() {
                while (true) {

                    try {
                        Runnable r = workerQueue.take();
                        r.run();


                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }  //class ThreadPool

    private class ThreadService implements Runnable{//, PropertiesLoaderImplBase{

        private  ExpressionTree expressionTree;
        private boolean queryDone = false;
        private double queryInterArrivalTime;
      //  private String criteria;

        public ThreadService(){}
        /*
        public ThreadService(String criteria ){
            this.criteria = criteria;
        }
        */

        //getter and setter methods
        public ExpressionTree getExpressionTree() {
            return expressionTree;
        }

        public void setExpressionTree(ExpressionTree expressionTree) {
            this.expressionTree = expressionTree;
        }

        public boolean isQueryDone() {
            return queryDone;
        }

        @Override
        public void run() {

            try {
                expressionTree = entryQueue.take();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            Cache_Reply cr = queryIndexGraph.searchGraph(expressionTree);

            queryDone = true;
            try {
                addToReturnQueue(cr);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        synchronized void addToReturnQueue(Cache_Reply cr) throws InterruptedException {

            returnQueue.add(cr);
        }

    }//private class ThreadService

    public static void main(String[] args) throws InterruptedException {
        HashSet<Cache> caches = new HashSet<>();
        for (int i = 0; i < 5 ; i++) {
           Cache cache = new Cache("Cache_"+i);
            caches.add(cache);
        }

        int totalQueries=10;
        QueryIndex qi = new QueryIndex(caches);
        Query_Subj_Predicate[] qsps = new Query_Subj_Predicate[totalQueries+10];
        ArrayList<String> testList = new ArrayList<>(totalQueries);

        qsps[0] = new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)");
        qsps[1] = new Query_Subj_Predicate("(<qry32:tab22:db2><axt412:tab12:db2><jkat22:tab32:db2>#<qry34:tab24:db4,gt345>)&(<jkat25:tab35:db5>#<qry33:tab23:db3,gt345>)");
        qsps[2] = new Query_Subj_Predicate("(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)_(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");
        qsps[3] = new Query_Subj_Predicate("(<atb24:tab4:db3><at4:tab34:db3><slr23:tab43:db3>#<san4:tab24:db3,atb64:tab4:db3,200to300>)");
        qsps[4]= new Query_Subj_Predicate("(<xlr62:tab42:db3><at2:tab32:db3>#<sand82:tab22:db2,gt222>)_(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");
        qsps[5] = new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)");
        qsps[6] = new Query_Subj_Predicate("(<qry32:tab22:db2><axt412:tab12:db2><jkat22:tab32:db2>#<qry34:tab24:db4," +
                "gt345>)&(<jkat25:tab35:db5>#<qry33:tab23:db3,gt345>)");
        qsps[7] = new Query_Subj_Predicate("(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4," +
                "eq33>)_(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");
        qsps[8] = new Query_Subj_Predicate("(<atb24:tab4:db3><at4:tab34:db3><slr23:tab43:db3>#<san4:tab24:db3," +
                "atb64:tab4:db3,200to300>)");
        qsps[9]= new Query_Subj_Predicate("(<xlr62:tab42:db3><at2:tab32:db3>#<sand82:tab22:db2,gt222>)_" +
                "(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");


        int returnQueries=0;


        for (int i = 0; i <totalQueries ; i++) {
            System.out.println("queries sent = "+qsps[i].getQueryID()+" "+qsps[i].getQueryExpression());
            testList.add(qsps[i].getQueryID());
            qsps[i+10]= new Query_Subj_Predicate(qsps[i].getQueryExpression());
            testList.add(qsps[i+10].getQueryID());
        }


        for (int i = 0; i < 5; i++) {
          //  qi.searchQueryIndex(qsps[i]);

        }

        Thread.sleep(10);
        System.out.println("***********************************************************************");
        qi.getQueryIndexGraph().printGraph();
        System.out.println("***********************************************************************");


        for (int i = 5; i < 10; i++) {
         //   qi.searchQueryIndex(qsps[i]);

        }

        for(int i=10; i <20;i++){
         //   qi.searchQueryIndex(qsps[i]);
        }


        //sendqueryback
        while (returnQueries != totalQueries){

            Thread.sleep(100);
            ArrayList<Cache_Reply> receivedList = qi.sendQueryBack();
            for (int i = 0; i < receivedList.size(); i++) {
                if(receivedList.get(i)!= null) {
                    System.out.println("From main - returned query ID: " + receivedList.get(i).getQueryID() + " with status " + receivedList.get(i).getReplyStatus());
                    String str = receivedList.get(i).getQueryID();
                    if (testList.contains(str)){
                        testList.remove(str);
                        returnQueries++;
                    }
                }
            }


           // System.out.println("returned queries are: "+returnQueries);
        }

        Thread.sleep(100);
        System.out.println("printing graph from queryIndex main");
        qi.getQueryIndexGraph().printGraph();

    }

}//class