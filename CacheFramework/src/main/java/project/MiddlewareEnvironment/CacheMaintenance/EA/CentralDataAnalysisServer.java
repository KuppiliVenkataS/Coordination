package project.MiddlewareEnvironment.CacheMaintenance.EA;

import project.MainClasses.CacheProperties;
import project.MiddlewareEnvironment.Cache;
import project.MiddlewareEnvironment.Container;
import project.MiddlewareEnvironment.QueryIndexFiles.Cache_Reply;
import project.MiddlewareEnvironment.QueryIndexFiles.IndexedQuery;
import project.MiddlewareEnvironment.QueryIndexFiles.QueryIndex;
import project.QueryEnvironment.ExpressionTree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by santhilata on 29/04/16.
 *
 * This class is mainly created to reduce  load on local query indexes
 * This class is superior class to all containers.
 * It runs continuously . This class is created to implement the hierarchy of query indexes in the architecture
 * QueryIndexes of each of the containers are added to this class
 * This class is responsible for
 * 1. gathering data from individual query indexes
 * 2. deciding upon a data location for a data fragment that cannot find place in the local container
 * 3. Running online algorithm for data placement
 * 4. running periodical mobility algorithms
 * 5. Running cache maintenance algorithms
 */
public class CentralDataAnalysisServer implements CacheProperties{

    private int no_ofThreads;
    private ThreadPool threadPool;
    private BlockingQueue<Object> entryQueue = new ArrayBlockingQueue(15) ;
    private Vector<Object> returnQueue =  new Vector<>() ;
    ArrayList<QueryIndex> queryIndices = new ArrayList<>();
    Container[] neighbouringContainers; // all containers list
    ArrayList<ExpressionTree> interestedQueries = new ArrayList<>(); // this list contains all queries that were
    //queried but could not find place. Interest is noted.

    public CentralDataAnalysisServer(){
        this.no_ofThreads = 10;
        this.threadPool = new ThreadPool(no_ofThreads);
    }

    public int getNo_ofThreads() {
        return no_ofThreads;
    }

    public void setNo_ofThreads(int no_ofThreads) {
        this.no_ofThreads = no_ofThreads;
    }

    public BlockingQueue<Object> getEntryQueue() {
        return entryQueue;
    }

    public void setEntryQueue(BlockingQueue<Object> entryQueue) {
        this.entryQueue = entryQueue;
    }

    public Vector<Object> getReturnQueue() {
        return returnQueue;
    }

    public void setReturnQueue(Vector<Object> returnQueue) {
        this.returnQueue = returnQueue;
    }

    public ArrayList<QueryIndex> getQueryIndices() {
        return queryIndices;
    }

    public void setQueryIndices(ArrayList<QueryIndex> queryIndices) {
        this.queryIndices = queryIndices;
    }

    public ArrayList<ExpressionTree> getInterestedQueries() {
        return interestedQueries;
    }

    public void setInterestedQueries(ArrayList<ExpressionTree> interestedQueries) {
        this.interestedQueries = interestedQueries;
    }

    public Container[] getNeighbouringContainers() {
        return neighbouringContainers;
    }

    public void setNeighbouringContainers(Container[] neighbouringContainers) {
        this.neighbouringContainers = neighbouringContainers;
    }

    public void enterObjectsIntoQueue(Object obj){
        entryQueue.add(obj);
        threadPool.addTask(new ThreadService());
    }

    /**
     * to get the objects returned from thread service
     * @return
     */
    public synchronized ArrayList<Object> fillReturnQueue(){
        ArrayList<Object> returnList = new ArrayList<>(returnQueue.size());
        returnList.addAll(returnQueue);

        return  returnList;
    }

    /**
     * Finds an empty cache globally
     * @param QI
     * @param searchCacheCriteria
     * @param expressionTree
     * @return
     */
    public Cache find_A_Cache_Globally(QueryIndex QI, String searchCacheCriteria, ExpressionTree expressionTree){
        Cache cache = null;
        int requiredDataSize = expressionTree.getNo_of_Nodes() * standard_Data_Unit_Size;
        String queryLocation = expressionTree.getQspLocation();

        int timeQueried = expressionTree.getTime_queried();

        switch (searchCacheCriteria){
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
            case GLOBAL_NEARBY_CRITERIA:{

                boolean cacheFound = false;
                char userLocation = queryLocation.charAt(4);
                for(Cache cache1: QI.getCache()){
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


                break;
            }
            /**
             * Simplest criteria. It places data on the first available cache of the first containers
             */
            case GLOBAL_DATA_CRITERIA: {

                for (int i = 0; i < numContainers; i++) {
                    Container container = getContainerByName(i);
                    QueryIndex qi = container.getQueryIndex();
                    for (Cache cache1: qi.getCache()){
                        if (cache1.getDataSizeAvailable() > requiredDataSize){
                            cache = cache1;
                            break;
                        }
                    }

                }

                break;
            }

            /**
             * This is a randomized method. Randomly picks a container and places there.
             *  caches random container and size availability onto other nearby containers too
             *  nearby containers have the increased  visibility by one step at any given time
             */
            case GLOBAL_RANDOMIZED: {

                Random random = new Random();
                boolean cacheFound = false;
                Container container = getContainerByName(random.nextInt(numContainers));
                QueryIndex qi = container.getQueryIndex();
                for (Cache cache1: qi.getCache()){
                    if (cache1.getDataSizeAvailable() > requiredDataSize){
                        cache = cache1;
                        break;
                    }
                }
                if (!cacheFound)
                    System.out.println("No place in the container. Hence data discarded");
                break;
            }

            default: {
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

    //-------------------------------------Time Related options--------------------------------------------------------


    //-------------------------------------Frequency related options---------------------------------------------------------



    //---------------------------------------------------------------------------------------------------------------------

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

    private class ThreadService implements Runnable{

        private Object entry;
        private Object reply;

        public ThreadService(){}

        @Override
        public void run() {

            try {
                entry = entryQueue.take();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (entry instanceof QueryIndex) {

                reply = new QueryIndex();
            }
            try {
                addToReturnQueue(reply);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        synchronized void addToReturnQueue(Object cr) throws InterruptedException {
            //do some thing here
            //1. can search for a suitable data size location
            returnQueue.add(cr);
        }


    }//private class ThreadService


}
