package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.RandomEngine;
import org.apache.commons.math3.distribution.PoissonDistribution;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by santhilata on 06/11/16.
 */
public class Input implements InputParameters{
    ArrayList<Query_Coord> input_queries ;
    ArrayList<Query_Coord> seedQueries;

    public Input(){

    }
    public Input(int numQueries, int seed, int numLoc){ // n
        input_queries =  new ArrayList<>();
        for (int i = 0; i < numQueries; i++) {


          int loc=0;
            int seedValue=0;


            String input_dist = inputDistribution;
            switch (inputDistribution){
                case "Poisson":{
                    /*
                    loc = getPoisson(numLoc/2,2,numLoc);
                    seedValue = getPoisson(seed/2, (int)(seed/3),seed);
                    System.out.println(loc + " "+seedValue);
                    */

                     loc = new Random().nextInt(numLoc);
                        loc = new PoissonDistribution(numLoc/2).sample();
                    while (loc>= numLoc){
                        loc = new PoissonDistribution(numLoc/2).sample();
                    }
                     seedValue =  new PoissonDistribution(seed/2).sample();
                    //System.out.println(seedValue);
                    while (seedValue >= seed ){
                        seedValue =  new PoissonDistribution(seed/2).sample();
                    }

                }
                break;

                case "Random":{
                    loc = new Random().nextInt(numLoc); // arbitrary location


                     seedValue = new Random().nextInt(seed); // one of the queries from the given set
                    while (seedValue >= seed ){
                        seedValue =  new Random().nextInt(seed);
                    }
                }
                break;

                default: {
                    loc = new Random().nextInt(numLoc); // arbitrary location
                    seedValue = new Random().nextInt(seed); // one of the queries from the given set
                    while (seedValue >= seed ){
                        seedValue =  new Random().nextInt(seed);
                    }
            }
            break;
            }


            String query = ""+seedValue;
            Query_Coord qctemp = new Query_Coord(i,loc+"",query); // here i is the qId, which runs from 0 to numQueries

            input_queries.add(qctemp);
        }

        seedQueries = new ArrayList<>();
        for (int i = 0; i < seed ; i++) {
            Query_Coord qTemp = new Query_Coord(i,""+i,""+i);
            seedQueries.add(qTemp);

        }

    }


    public int getPoisson(int mean, int variance, int upperBoundary){
        RandomEngine engine = new DRand();
        Poisson poisson = new Poisson(mean, engine);
        int poissonObs = poisson.nextInt();
        Normal normal = new Normal(mean, variance, engine);


        double normalObs = normal.nextDouble();
        int sample = (int) Math.abs(normalObs);

        while (sample >= upperBoundary) {
            normalObs = normal.nextDouble();
            sample = (int) Math.abs(normalObs);
        }
        return sample;

    }

    public ArrayList<Query_Coord> getQueries() {
        return input_queries;
    }
    public ArrayList<Query_Coord> getSeedQueries(){
        return seedQueries; }

    public void setInput_queries(ArrayList<Query_Coord> input_queries) {
        this.input_queries = input_queries;
    }

    public void setSeedQueries(ArrayList<Query_Coord> seedQueries) {
        this.seedQueries = seedQueries;
    }
}


class Query_Coord{ // specific type of query used only for this MAS coordination strategies
    int qID; // is for identification purpose - out of numQueries
    String loc; // user location from where it is originated
    String query; //the query  indicates which query from the stock queries - out of seed queries

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
