package project.ResponseTimeSimulation.Cache_DataServerSimulation;

import project.QueryEnvironment.SubjectQuerySegment;
import project.UserEnvironment.QuerySegment_ExecutionTime;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by santhilata on 14/5/15.
 */
public class DataServer {
    private String databaseName;
    private static final int READ_SERVICE_TIME=50; // seek and I/O time used for read and write  queries
    private static final int WRITE_SERVICE_TIME=0; // seek and I/O time used for write queries only

    private QuerySegment_ExecutionTime querySegment;
    private int no_ofOperations = 0;
    private int pauseTime = 10;
    private boolean queryDone = false;
    private String address_container;

    private static  double queryInterArrivalTime;

    private BlockingQueue<QuerySegment_ExecutionTime> entryQueue = new PriorityBlockingQueue<QuerySegment_ExecutionTime>() ;
    private BlockingQueue<QuerySegment_ExecutionTime> returnQueue =  new PriorityBlockingQueue<QuerySegment_ExecutionTime>() ;


    public DataServer(String name){
        this.databaseName = name;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getAddress_container() {
        return address_container;
    }

    public void setAddress_container(String address_container) {
        this.address_container = address_container;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public static double getQueryInterArrivalTime() {
        return queryInterArrivalTime;
    }

    public static void setQueryInterArrivalTime(double queryInterArrivalTime) {
        DataServer.queryInterArrivalTime = queryInterArrivalTime;
    }

    public BlockingQueue<QuerySegment_ExecutionTime> getEntryQueue() {
        return entryQueue;
    }

    public void setEntryQueue(ArrayList<SubjectQuerySegment> inList) {
        for (int i = 0; i < inList.size(); i++) {
           QuerySegment_ExecutionTime qet = new QuerySegment_ExecutionTime(inList.get(i));
            this.entryQueue.add(qet);
        }

    }

    public BlockingQueue<QuerySegment_ExecutionTime> getReturnQueue() {
        return returnQueue;
    }

    public void setReturnQueue(BlockingQueue<QuerySegment_ExecutionTime> returnQueue) {
        this.returnQueue = returnQueue;
    }

    public void setNo_ofOperations(int no_ofOperations) {
        this.no_ofOperations = no_ofOperations;
    }
    public int calculate_No_ofOps(){

        ArrayList attributes = querySegment.getQuerySegment().getAttributes();
        // Here number of attributes is multiplied by a constant value
        // to indicate the time needed for search a particular attribute
        int no_ofOperations = attributes.size()*500;

        // read and write operations are not specified
        return no_ofOperations;
    }

    /**
     * This method should take some ordering of queue
     * and calculate the average waiting time in the queue
     * @return
     */
    public double calculateWaitInQueueTime(){
        double waitingTime=0;

        /**
         * Assumptions and formula for waiting time:
         * a= Average inter arrival time; (last query arrived / no of queries) ;
         * CV_a = std_dev of inter arrival times / a; (assume to be 1)
         * CV_p = Coefficient of variation of times ; (assume to be 1)
         * Utilization u = flowrate / capacity; (flowrate = 1/a, capacity = 1/cache service time)
         * cache service time = index search time + service time
         * Activity time = service time factor = average cache service time
         *
         * waiting time = activity time *(utilization /(1-utilization))*(sqr(CV_a)+sqr(CV_p) / 2);
         *
         * source: https://www.utdallas.edu/~metin/Or6302/Folios/omqueue.pdf - page 20
         *
         * for parallel processors
         * Approximate Waiting Time Formula for Multiple ( m) Servers:
         * utilization = p / (a*m) = flowrate / capacity
         * time in queue = (activity time / m)*((utilization ^ (Math.sqrt(2*(m+1)-1))/(1-utilization))*(CV_a*CV_a +CV_p*CV_p)/2 ;
         */


        double utilization = calculateServiceTime()/queryInterArrivalTime;

        double activityTime = calculateServiceTime()*10000;//no of queries data server can service at a moment
        int CV_a = 1;
        int CV_p = 1;
        int m = 10; //no of parallel processors


        if (m==1)
            waitingTime = (activityTime*(utilization /(1-utilization)) * (CV_a*CV_a + CV_p*CV_p) / 2);
        else waitingTime = (activityTime / m)*(Math.pow(utilization,(Math.sqrt(2*(m+1)-1)))/(1-utilization))*((CV_a*CV_a +CV_p*CV_p)/2) ;


        return waitingTime;


    }

    /**
     * This method is some simulated way of executing a query.
     * Every operation is accounted for read and write seek and I/O time
     * Should also add the time spent in queue
     * Service time is the CPU time
     */
    public double calculateServiceTime(){
        Random rand = new Random();
        double serviceTime = 0 ;
        /**
         * Assumption: There are loops within the operations.
         * Loops within the operations pauses execution of operations
         * Thus introducing a delay.
         *
         * At the moment these are random values generated from the system.
         * However, in the later versions, the following parameters have to be set differently.
         */

        int loops = rand.nextInt(no_ofOperations+1);// to avoid zero number of operations
        int delay = loops*pauseTime;
        serviceTime = (no_ofOperations*(READ_SERVICE_TIME + WRITE_SERVICE_TIME) + delay)*3.6*3.45E-09 ;// Server processor speed is assumed to be 3.45 GHZ

        return serviceTime;
    }

    public void QueryServiceTime() {

        try {
            querySegment = entryQueue.take();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setNo_ofOperations(calculate_No_ofOps());
        double time = calculateServiceTime()+calculateWaitInQueueTime( );

        querySegment.setExecutionTime(time);// this execution time is for each query segment
        queryDone = true;

        try {
            addToReturnQueue(querySegment);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    synchronized void addToReturnQueue(QuerySegment_ExecutionTime querySegment) throws InterruptedException {

        returnQueue.add(querySegment);
    }



}
