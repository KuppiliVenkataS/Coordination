package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by santhilata on 06/11/16.
 */
public class Input {
    ArrayList<Query_Coord> queries ;

    public Input(int numQueries, int seed, int numLoc){ // n
        queries =  new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {

            String loc = ""+new Random().nextInt(numLoc);
            /*
            int seedValue =  new PoissonDistribution(seed/2).sample();
            System.out.println(seedValue);
            while (seedValue >= seed ){
                seedValue =  new PoissonDistribution(seed/2).sample();

            }
            */

            int seedValue = new Random().nextInt(seed);
            while (seedValue >= seed ){
                seedValue =  new PoissonDistribution(seed/2).sample();

            }


            String query = ""+seedValue;
            Query_Coord qctemp = new Query_Coord(i,loc,query);

            queries.add(qctemp);
        }

    }

    public ArrayList<Query_Coord> getQueries() {
        return queries;
    }
}


class Query_Coord{ // specific type of query used only for this MAS coordination strategies
    int qID;
    String loc;
    String query;

    public Query_Coord() {
    }

    public Query_Coord(int qID, String loc, String query) {
        this.qID = qID;
        this.loc = loc;
        this.query = query;
    }

    public int getqID() {
        return qID;
    }

    public void setqID(int qID) {
        this.qID = qID;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public  String toString(){
        return qID+","+loc+","+query;
    }

    public void  printQuery(){
        System.out.println(toString());
    }
}
