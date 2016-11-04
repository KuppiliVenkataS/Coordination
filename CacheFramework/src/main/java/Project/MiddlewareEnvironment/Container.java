package project.MiddlewareEnvironment;

import project.DatabaseInfo.DatabaseServer;
import project.MiddlewareEnvironment.QueryIndexFiles.QueryIndex;
import project.ResponseTimeSimulation.CommunityCache_Response.Query_Response;
import project.UserEnvironment.User;



import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by santhilata on 6/4/15.
 */
public class Container {//implements PropertiesLoaderImplBase {
    private String container_name;
    private int container_number;
    private DatabaseServer dataServers;
    private QueryIndex myQueryIndex;
    HashSet<Cache> cacheSet ;
    private ArrayList<Query_Response>  queryResponses = new ArrayList<>();// queries sent from this container
    private ArrayList<User>   userArrayList = new ArrayList<>();


    public  Container(String container_name){
        this.container_name = container_name;
    }


    public Container(String container_name, int no_of_cacheUnitsPerContainer,QueryIndex QI) {
        this.container_name = container_name;
        cacheSet = QI.getCache();
        for (int i = 0; i < no_of_cacheUnitsPerContainer; i++) {
            Cache cache = new Cache("Cache_"+(i+1), container_name);
            cache.setSelfIndex(myQueryIndex);
            cacheSet.add(cache);
        }
        QI.setCache(cacheSet);
        myQueryIndex = QI;
    }



    public String getContainer_name() {
        return container_name;
    }

    public void setContainer_name(String container_name) {
        this.container_name = container_name;
    }

    public int getContainer_number() {
        return container_number;
    }

    public void setContainer_number(int container_number) {
        this.container_number = container_number;
    }

    public DatabaseServer getDataServers() {
        return dataServers;
    }

    public void setDataServers(DatabaseServer dataServers) {
        this.dataServers = dataServers;
    }

    public QueryIndex getQueryIndex() {
        return myQueryIndex;
    }

    public void setQueryIndex(QueryIndex queryIndexGraph) {
        this.myQueryIndex = queryIndexGraph;
    }

    public ArrayList<Query_Response> getQueryResponses() {
        return queryResponses;
    }

    public void setQueryResponses(ArrayList<Query_Response> queryResponses) {
        this.queryResponses = queryResponses;
    }

    public HashSet<Cache> getCacheSet() {
        return cacheSet;
    }

    public void setCacheSet(HashSet<Cache> cacheSet) {
        this.cacheSet = cacheSet;
    }

    public void refreshQueryIndex(){
        for (Cache cache: this.cacheSet){
            cache.getQueryList().clear();
        }
        myQueryIndex = new QueryIndex(cacheSet);
    }



    //start cache units


}
