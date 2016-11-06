package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by santhilata on 04/11/16.
 * This class is to calculate the response time for various
 */
public class ResponseTime {

    Input input;
    ArrayList<Query_Coord>[] uloc_queries ;
    ArrayList<Query_Coord>[] cloc_queries;
    
    
    
    static int numQueries = 1000;//{1000,2000,5000,7000,10000};
    static   int seed = 25; //{25, 50, 100, 150, 200 };

    static int numLoc = 6;
    static int cloc_size = 100; //GB
    
    int[][] uloc_query_freq = new int[numLoc][seed];


    public void createInput(){

            this.input = new Input(numQueries,seed,numLoc);
    }

    public void generateStats(){
        this.createInput();
        this.uloc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc ; i++) {
            this.uloc_queries[i] = new ArrayList<>();
        }
        ArrayList<Query_Coord> tempQ = input.getQueries();

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

    //master-slave algorithm

    /**
     * Master selects a place for slave
     * create input for ten times.
     * Consider frequency for last three times and select that place for query
     * create new input -
     *
     */

    public void master_slave(){
        this.cloc_queries = new ArrayList[numLoc];
        for (int i = 0; i < numLoc ; i++) {
            this.cloc_queries[i] = new ArrayList<>();
        }

        int queryNo = 0;
        while (queryNo < seed) {
            for (int i = 0; i < numLoc; i++, queryNo++) {
                if (queryNo < seed)
                    cloc_queries[i].add(getQuery(queryNo));
            }
        }

    }

    public Query_Coord getQuery(int queryNum){

        Query_Coord qtemp = null ;
        for (Query_Coord qc :
                input.getQueries()) {
            if (qc.getqID() == queryNum) {
                qtemp = qc;
                break;
            }
        }

        return  qtemp;
    }


    public static void main(String[] args) {

        ResponseTime rt = new ResponseTime();

        rt.generateStats();

        int[][] ulocQ_F = rt.getUloc_query_freq();

        System.out.println("uloc  query  freq");
        for (int i = 0; i < ulocQ_F.length; i++) {
            for (int j = 0; j < ulocQ_F[i].length; j++) {
                System.out.println(i +"     "+j+"      "+ulocQ_F[i][j]);
            }
        }
    }
}
