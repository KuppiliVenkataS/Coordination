package project.ResponseTimeSimulation.Cache_DataServerSimulation;


import project.QueryEnvironment.SubjectQuerySegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by santhilata on 11/5/15.
 * This cache implementation simply stores the query segments that belong to a particular database.
 */
public class Cache_DataServer {
    List<SubjectQuerySegment> cacheIndex;
    String dataServer;
    public Cache_DataServer(String dataServer){
        cacheIndex = new ArrayList<>();
        this.dataServer = dataServer;
    }

    public List<SubjectQuerySegment> getCacheIndex() {
        return cacheIndex;
    }

    public void setCacheIndex(List<SubjectQuerySegment> cacheIndex) {
        this.cacheIndex = cacheIndex;
    }

    public String getDataServer() {
        return dataServer;
    }

    public void setDataServer(String dataServer) {
        this.dataServer = dataServer;
    }

    public void addNodetoCache(SubjectQuerySegment sqs){
        if(!cacheIndex.contains(sqs)) {

            String table = sqs.getTable();
            ArrayList<String> attributes = sqs.getAttributes();
            Collections.sort(cacheIndex);
            boolean tableFound = false;

            for (int i = 0; i < cacheIndex.size(); i++) {
                if(cacheIndex.get(i).getTable().equals(table)) {
                    cacheIndex.add(i+1,sqs);
                    tableFound = true;
                    break;
                }

            }
            if (!tableFound)  // first entry of this kind
            cacheIndex.add(sqs);

            Collections.sort(cacheIndex);
        }
    }

    public boolean isSegmentInCache(SubjectQuerySegment sqs){
        if (cacheIndex.contains(sqs))
            return true;
        else return false;
    }


    public boolean deleteNodeFromCache(SubjectQuerySegment sqs){
        if(cacheIndex.contains(sqs)){
            cacheIndex.remove(sqs);
            return true;
        }
        else {
            System.out.println(" segment not present in cache");
            return  false;
        }
    }

}
