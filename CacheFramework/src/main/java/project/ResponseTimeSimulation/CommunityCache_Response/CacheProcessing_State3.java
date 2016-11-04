package project.ResponseTimeSimulation.CommunityCache_Response;

import project.QueryEnvironment.ExpressionTree;

/**
 * Created by santhilata on 25/03/15.
 */
public class CacheProcessing_State3 {
    //STATE 3: Reached at Cache

    int startTimeState3;
    int endTimeState3;

    int cacheLocationTime;

    double dataFound;  // calculate dataFound in GB
    ExpressionTree remainingQuery;
    double remainingTime;

    public CacheProcessing_State3(int ticks){
        this.startTimeState3 = ticks;
        this.cacheLocationTime = 1;// time needed to locate relevant cache
    }

    int status;

    public int getStartTimeState3() {
        return startTimeState3;
    }

    public void setStartTimeState3(int startTimeState3) {
        this.startTimeState3 = startTimeState3;
    }

    public int getEndTimeState3() {
        return endTimeState3;
    }

    public void setEndTimeState3(int endTimeState3) {
        this.endTimeState3 = endTimeState3;
    }

    public int getCacheLocationTime() {
        return cacheLocationTime;
    }

    public void setCacheLocationTime(int cacheLocationTime) {
        this.cacheLocationTime = cacheLocationTime;
    }

    public double getDataFound() {
        return dataFound;
    }

    public void setDataFound(double dataFound) {
        this.dataFound = dataFound;
    }

    public ExpressionTree getRemainingQuery() {
        return remainingQuery;
    }

    public void setRemainingQuery(ExpressionTree remainingQuery) {
        this.remainingQuery = remainingQuery;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int proportionDataFound(){
        int dataFound = 0;

        return dataFound;
    }

    public double timeSpentInState3(double queryInterArrivalTime){
        double timeSpent = waitingTimeInQueue(queryInterArrivalTime)+QueryIndexSearchTime()+cacheServiceTime()+cacheLocationTime;
        return (timeSpent);
    }


    /// FOLLOWING METHODS CALCULATE CACHE SERVICE AND WAITING TIME NEEDED IN STATE 3
    /**
     * This method should take some ordering of queue
     * and calculate the average waiting time in the queue
     * @return
     */
    public double waitingTimeInQueue(double queryInterArrivalTime){

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

        double utilization = cacheServiceTime()/queryInterArrivalTime;
        double activityTime = cacheServiceTime();
        int CV_a = 1;
        int CV_p = 1;
        int m = 10; //no of parallel processors

        double waitingTime=0;
        if (m==1)
        waitingTime = (activityTime*(utilization /(1-utilization)) * (CV_a*CV_a + CV_p*CV_p) / 2);
        else waitingTime = (activityTime / m)*(Math.pow(utilization,(Math.sqrt(2*(m+1)-1)))/(1-utilization))*((CV_a*CV_a +CV_p*CV_p)/2) ;


        return waitingTime;


    }

    /**
     * Calculates cache service time
     * Cache service time is the time needed for query index search plus cache data location
     *  @return
     */
    public double cacheServiceTime(){

        /**
         * Assumptions:
         * Cache service time is assumed to  be the same as query index search
         * So total service time =  QueryIndexSearchTime + Cache service time;
         */
        return (2*QueryIndexSearchTime());
    }

    /**
     * To calculate query index
     * @return
     */
    public double QueryIndexSearchTime(){
        /**
         * Assumptions:
         * Average number of queries in the index= 1000
         * Average no of branches per query = 3

         * Average I/O instructions per branch 4;

         * CPU time = I * CPI * T

         * I = number of instructions in program
         * CPI = average cycles per instruction = 3.6
         * (source: http://homepage.divms.uiowa.edu/~ghosh/2-2-06.pdf - page 1)
         * T = clock cycle time (1/3.45GHZ)
         * Data structure complexity is assumed to be negligible with sequential access of all the elements in store
         */
        int no_ofQueriesInIndex=10000;
        int no_ofBranchesPerQuery = 3; //average number of branches per query
        int no_ofI_OInstructionsPerBranch = 25;
        int no_ofMicroInstructions = 10;

        double CPU_Time = no_ofQueriesInIndex*(no_ofBranchesPerQuery+no_ofI_OInstructionsPerBranch*no_ofMicroInstructions)*3.6*3.3E-09;
        return CPU_Time;
    }

}
