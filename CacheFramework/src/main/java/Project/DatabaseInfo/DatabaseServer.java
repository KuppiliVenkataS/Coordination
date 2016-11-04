package project.DatabaseInfo;

import jade.content.Concept;
import jade.util.leap.*;

import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.PredicateQuerySegment;
import project.QueryEnvironment.Query_Subj_Predicate;
import project.QueryEnvironment.Subj_PredicateSpecificQueries.QueryGenerator_SubjPred;
import project.QueryEnvironment.SubjectQuerySegment;

import project.UserEnvironment.QuerySegment_ExecutionTime;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import java.util.ArrayList;

/**
 * Edited on 30/03/15. *
 * This class receives query segments and serves them.
 */
public class DatabaseServer implements Concept,Serializable {
    private HashSet tables;
    private String databaseName;
    private HashMap schema;
    private String address_container;


    private static int no_ofReplicas = 10;
   // private Replica[] replicas = new Replica[no_ofReplicas];
    private int no_ofThreads ;
    private ThreadPool threadPool;

    private static int no_queries = 0;
    static  double queryInterArrivalTime;

    //BlockingQueue waits fro the queue to become non empty while retrieving an element
    // This class implements  two queues - entryQueue and returnQueue.
    //no_of_threads decide the capacity of the DatabaseServer
    //ThreadPool is a private class which makes ThreadService runnable
    //entryQueue adds new query ExpressionTrees into the queue that waits to be serviced.
    //returnQueue

    private BlockingQueue<QuerySegment_ExecutionTime> entryQueue = new PriorityBlockingQueue<QuerySegment_ExecutionTime>() ;
    private BlockingQueue<QuerySegment_ExecutionTime> returnQueue =  new PriorityBlockingQueue<QuerySegment_ExecutionTime>() ;

    public BlockingQueue<QuerySegment_ExecutionTime> getEntryQueue() {
        return entryQueue;
    }

    public void setEntryQueue(BlockingQueue<QuerySegment_ExecutionTime> entryQueue) {
        this.entryQueue = entryQueue;
    }

    public BlockingQueue<QuerySegment_ExecutionTime> getReturnQueue() {
        return returnQueue;
    }

    public void setReturnQueue(BlockingQueue<QuerySegment_ExecutionTime> returnQueue) {
        this.returnQueue = returnQueue;
    }

    //constructors
    public DatabaseServer() {
        // null constructor
        no_ofThreads = 10;
        threadPool =   new ThreadPool(no_ofThreads);
       // threadPool.addTask(new ThreadService(0));

    }

   /* public DatabaseServer(DatabaseAgent agent) {
        this.myAgent = agent;
    }*/
    public DatabaseServer(String name){
        this.databaseName = name;
        no_ofThreads = 10;
        threadPool =   new ThreadPool(no_ofThreads);
    }

    public DatabaseServer(HashSet tables, String databaseName) {
        this.tables = tables;
        this.databaseName = databaseName;
        no_ofThreads = 10;
        threadPool =   new ThreadPool(no_ofThreads);
       // threadPool.addTask(new ThreadService());
    }

    public DatabaseServer(String name, int capacity) {
        this.databaseName = name;
        no_ofThreads = capacity;
        threadPool =   new ThreadPool(no_ofThreads);
      //  threadPool.addTask(new ThreadService());
    }



    // get and set methods


    public static double getQueryInterArrivalTime() {
        return queryInterArrivalTime;
    }

    public static void setQueryInterArrivalTime(double queryInterArrivalTime) {
        DatabaseServer.queryInterArrivalTime = queryInterArrivalTime;
    }

    public String getAddress_container() {
        return address_container;
    }

    public void setAddress_container(String address_container) {
        this.address_container = address_container;
    }

    public HashSet getTables() {
        return tables;
    }

    public int getNo_ofThreads() {
        return no_ofThreads;
    }

    public void setNo_ofThreads(int no_ofThreads) {
        this.no_ofThreads = no_ofThreads;
    }

  /*  public DatabaseAgent getMyAgent() {
        return myAgent;
    }

    public void setMyAgent(DatabaseAgent myAgent) {
        this.myAgent = myAgent;
    }
    */

    public void setTables(HashSet tables) {

        this.tables = tables;

    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public void setSchema(HashMap schema) {
        this.schema = schema;
    }

    public Map getSchema() {
        return this.schema;
    }

    // schema is used by wrapper agents
    public void createSchema() {

        schema = new HashMap();

        Table[] tab = (Table[]) tables.toArray();
        for (int i=0; i<tab.length;i++){
            schema.put(tab[i], tab[i].getAttributes());
        }
    }

    public void addTables(Table t){

        tables.add(t);

    }

    public void addAllTables(HashSet t){
        tables.addAll(t);
    }

   /**
    *  public void createReplica(NetworkNode nn) {
        Replica newReplica = new Replica();
        newReplica.setReplicaID(this.databaseName);
        newReplica.setAddress(nn);

        int i = 0;
        for (; i < no_ofReplicas && this.replicas[i] != null; i++) ;
        replicas[i] = newReplica;

    }*/
    public void printSchema() {

        //   Iterator itr = schema.entrySet().iterator();
        Iterator itr = this.getTables().iterator();
        while (itr.hasNext()) {
            Table tab = (Table)itr.next();
            System.out.print("Table: " + tab.getTableName() + " - ");

            Iterator atr = tab.getAttributes().iterator();
            while (atr.hasNext()) {
                Attribute at = (Attribute)atr.next();
                System.out.print(at.getAttributeName() + ", ");
            }

            System.out.println();

        }//while itr
    }//printSchema

    public int capacity() {
        return no_ofThreads;
    }

    public void receiveQuery(QuerySegment_ExecutionTime querySegment) {
      //  System.out.println("From data server "+databaseName+" received segment "+ querySegment.getQuerySegment().getQueryID());
        entryQueue.add(querySegment);
        //The moment there is an entry in the entryQueue, threadPool adds a task .


        threadPool.addTask(new ThreadService());
        no_queries++;
    }

    public void readReturnQueue(){
        System.out.println("Return q size: "+returnQueue.size());
        for (int i = 0; i < returnQueue.size() ; i++) {

            System.out.println(( returnQueue.remove()).getQuerySegment().getDatabase());
        }
    }

    public synchronized ArrayList<QuerySegment_ExecutionTime> sendQueryBack() throws InterruptedException {
        if (returnQueue.isEmpty())    {
           // System.out.println("No more results. Queue is empty");

            return null;
        }


        ArrayList<QuerySegment_ExecutionTime> returnList = new ArrayList<>();
        returnList.addAll(returnQueue);
      //  System.out.println("Size of the return List in sendQueryBack"+returnList.size());

        return returnList;
    }

    public synchronized void removeQueriesFromReturnQueue(ArrayList<QuerySegment_ExecutionTime> qet){
      //  System.out.println("Size of the return queue "+returnQueue.size());
        returnQueue.removeAll(qet);
      //  System.out.println("removed queries from the function call");
      //  System.out.println("return queue size"+returnQueue.size());

    }

    public void calculateLoad() {
        //write code here
        //TODO
    }

    public void trafficSimulation() {
        // write code to simulate the way the queued queries should be serviced.
        //TODO
    }

    /**
     * Queries are lined up in the entryQueue. A thread service pool is opened to the incoming queries.
     * A constant read request service is implemented at the moment.
     * No_of threads a database can handle at a given time is hardcoded to 10. (To be changed later according to the ideal numbers)
     *  TODO: Read papers and obtain the ideal data to set parameters.
     * A BitVector is to be implemented to set the thread pool occupied
     */
    private class ThreadPool {

         // A Priority blocking queue is maintained for the queue of queries instead of simple queue
         // This is just for the purpose of setting up some priority in future
         // An ordinary Priority queue implementation does not allow synchronisation on a single queue instance.
         // We need to  access a single queue instance from the database to insert queries from one end and --
         // -- concurrently retrieve queries from the other end

        // private BlockingQueue<Query_ExecutionTime> entryQueue;
        // private BlockingQueue<Query_ExecutionTime> returnQueue;
        //  private int[] bitVector;

        private final int THREAD_ALLOCATION_TIME = 1;
        private final BlockingQueue<Runnable> workerQueue;
        private final Thread[] workerThreads;

        public ThreadPool(int numThreads) {


            workerQueue = new LinkedBlockingQueue<>();
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
                        r.run(); // running the threadService


                    } catch (InterruptedException e) {
                        e.printStackTrace();

                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }  //class ThreadPool

    //================================
    //This class's run method retrieves one element from the entry queue and
    // services and puts it in the return queue continuously.

    private class ThreadService implements Runnable{
        private static final int READ_SERVICE_TIME=50; // seek and I/O time used for read and write  queries
        private static final int WRITE_SERVICE_TIME=0; // seek and I/O time used for write queries only

        private QuerySegment_ExecutionTime querySegment;
        private int no_ofOperations = 0;
        private int pauseTime = 10;
        private boolean queryDone = false;

        public ThreadService(){

        }

        //getter and setter methods
        public QuerySegment_ExecutionTime getQuery() {
            return querySegment;
        }

        public void setQuery(QuerySegment_ExecutionTime query) {
            this.querySegment = query;
        }

        public int getNo_ofOperations() {
            return no_ofOperations;
        }

        public void setNo_ofOperations(int no_ofOperations) {
            this.no_ofOperations = no_ofOperations;
        }

        public int getPauseTime() {
            return pauseTime;
        }

        public void setPauseTime(int pauseTime) {
            this.pauseTime = pauseTime;
        }

        public boolean isQueryDone() {
            return queryDone;
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

        @Override
        public void run() {

            try {
                querySegment = entryQueue.take();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setNo_ofOperations(calculate_No_ofOps());
            double time = calculateServiceTime()+calculateWaitInQueueTime();

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

    }//private class ThreadService

    public static void main(String[] args) throws IOException, InterruptedException {
        DatabaseServer dbs = new DatabaseServer();

        //  PriorityBlockingQueue<Query_ExecutionTime> entryQueue = new PriorityBlockingQueue<Query_ExecutionTime>();
        //  PriorityBlockingQueue<Query_ExecutionTime> returnQueue = new PriorityBlockingQueue<Query_ExecutionTime>();

        int totalQueries=1;
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

        ExpressionTree[] ets = new ExpressionTree[totalQueries];

        ArrayList querySegments = new ArrayList();
        double queryInterArrivalTime = 3.6;

        dbs.setQueryInterArrivalTime(queryInterArrivalTime);

        for (int i = 0; i < totalQueries; i++) {

            ets[i] = new ExpressionTree(qsps[i]);
            ArrayList<Query_Subj_Predicate> subQueries = ets[i].getSubQueries();



            for (int j = 0; j < subQueries.size(); j++) {
               Query_Subj_Predicate qsp = subQueries.get(j);
                ArrayList<SubjectQuerySegment> listS = qsp.getSubjectQuerySegments();

                for (int k = 0; k < listS.size(); k++) {
                    querySegments.add(listS.get(k));
                }

                ArrayList<PredicateQuerySegment> listP = qsp.getPredicateQuerySegments();

                int number = listP.size();

                for (int k = 0; k < number; k++) {

                    PredicateQuerySegment predicateQuerySegment = (PredicateQuerySegment) qsp.getPredicateQuerySegments().get(k);
                    if (predicateQuerySegment.getAttribute1() != null) {
                        querySegments.add(predicateQuerySegment.getAttribute1());
                    }

                    if (predicateQuerySegment.getAttribute2() != null) {
                        querySegments.add(predicateQuerySegment.getAttribute2());

                    }
                }

            }

        }

       // System.out.println("Query segments size "+querySegments.size());

        for (int i = 0; i < querySegments.size(); i++) {
            SubjectQuerySegment qsmt = (SubjectQuerySegment) querySegments.get(i);

            QuerySegment_ExecutionTime qse = new QuerySegment_ExecutionTime(qsmt);
            dbs.receiveQuery(qse);
        }

        Thread.sleep(10);

        QuerySegment_ExecutionTime query = dbs.returnQueue.peek();
        if (query == null)
            System.out.println("nothing inside");
        else  System.out.println("Query Id "+query.getQuerySegment().toString());


int no_of_segments = 0;
        for (QuerySegment_ExecutionTime query1: dbs.returnQueue){
            System.out.println("querySegment in the return q "+query1.getQuerySegment().toString()+"exe time"+query1
                    .getExecutionTime());
            no_of_segments++;
        }

        try {
        ArrayList<QuerySegment_ExecutionTime> test1 = dbs.sendQueryBack();
        int test_size = 0;
        while(no_of_segments != test_size) {


            System.out.println("no of segments" + test1.size()+ "no_of_segments sent ="+no_of_segments);
            for (QuerySegment_ExecutionTime t1 : test1) {
                System.out.println(t1.getQuerySegment().getQueryID() + " " + t1.getExecutionTime());
            }

            dbs.removeQueriesFromReturnQueue(test1);

            test_size = test_size+test1.size();

            test1 = dbs.sendQueryBack();
        }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



    } //end main

}
