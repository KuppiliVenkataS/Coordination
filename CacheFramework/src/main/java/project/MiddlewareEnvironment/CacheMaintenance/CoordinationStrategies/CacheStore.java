package project.MiddlewareEnvironment.CacheMaintenance.CoordinationStrategies;

import java.util.ArrayList;

/**
 * Created by santhilata on 07/11/16.
 */
public class CacheStore {
    String cLoc;
    ArrayList<Query_Coord> homeQueries;
    ArrayList<String> heuristicPrefList ;

    public CacheStore(String cLoc){
        this.cLoc = cLoc;
        homeQueries = new ArrayList<>();
        heuristicPrefList = new ArrayList<>();
    }

    public String getcLoc() {
        return cLoc;
    }

    public void setcLoc(String cLoc) {
        this.cLoc = cLoc;
    }

    public ArrayList<Query_Coord> getHomeQueries() {
        return homeQueries;
    }

    public void setHomeQueries(ArrayList<Query_Coord> homeQueries) {
        this.homeQueries = homeQueries;
    }

    public ArrayList<String> getHeuristicPrefList() {
        return heuristicPrefList;
    }

    public void setHeuristicPrefList(ArrayList<String> heuristicPrefList) {
        this.heuristicPrefList = heuristicPrefList;
    }


    //TODO: Complete the following
    public ArrayList<Query_Coord> selectQueriesForMA_Planning(){
        ArrayList<Query_Coord> selected_Queries = new ArrayList<>();

        return selected_Queries;
    }
}
