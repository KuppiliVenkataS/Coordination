package project.MiddlewareEnvironment;


import project.MiddlewareEnvironment.QueryIndexFiles.Graph;
import project.MiddlewareEnvironment.QueryIndexFiles.IndexedQuery;
import project.MiddlewareEnvironment.QueryIndexFiles.QueryIndex;

import java.util.ArrayList;

/**
 * Created by k1224068 on 17/06/14.
 * Last edited on 16/09/2015.
 * This class abstracts cache of frequent query segments.
 * A special data structure is created to store the frequent query segments.
 * TODO: Calculate delay of cache
 *
 */
public class Cache {
    private String cacheName;
    private String cacheAddress; // cache location the container
    private ArrayList<IndexedQuery> queryList; // query fragments stored in the cache unit
    private double maxCacheSize = 1000000000.0;
    private double occupiedData =0;
    private double dataSizeAvailable = maxCacheSize;
    private QueryIndex selfIndex;


    public Cache(String cacheName) {
        this.cacheName = cacheName;
        queryList = new ArrayList<>(); //place holder for queries in sequential order
    }

    public Cache(String cacheName, String cacheAddress) {
        this.cacheName = cacheName;
        this.cacheAddress = cacheAddress;
        queryList = new ArrayList<>();
    }

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public String getCacheAddress() {
        return cacheAddress;
    }

    public void setCacheAddress(String cacheAddress) {
        this.cacheAddress = cacheAddress;
    }

    public ArrayList<IndexedQuery> getQueryList() {
        return queryList;
    }

    public void setQueryList(ArrayList<IndexedQuery> queryList) {
        this.queryList = queryList;
    }

    public void addQueryToList(IndexedQuery cl){
        this.getQueryList().add(cl);
    }

    public double getMaxCacheSize() {
        return maxCacheSize;
    }

    public void setMaxCacheSize(double maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    public double getOccupiedData() {
        return occupiedData;
    }

    public void setOccupiedData(double occupiedData) {
        this.occupiedData = occupiedData;
    }

    public double getDataSizeAvailable() {
        return dataSizeAvailable;
    }

    private void modifyDataSizeAvailable() {
        this.dataSizeAvailable = maxCacheSize - this.occupiedData;

    }

    public void addDataToCache (double extraData){
        this.occupiedData += extraData;
        modifyDataSizeAvailable();
    }

    public void deleteDataFromCache(double uselessData){
        this.occupiedData -= uselessData;
        modifyDataSizeAvailable();
    }

    public void setSelfIndex (QueryIndex queryIndex){
        this.selfIndex = queryIndex;
    }

    /**
     * Though the searching in the query index and in a bag may be more recommendable,#
     * nevertheless the following may be useful sometimes
     * @param cl - Indexedquery
     * @return
     */
    public boolean isQueryPresentInCache(IndexedQuery cl){
       for (IndexedQuery query: queryList){
           if (query.equals(cl))
               return true;
        }
        return false;
    }

    public void refreshCacheData(){
        this.dataSizeAvailable = maxCacheSize;
        this.queryList = new ArrayList<>();
        this.occupiedData = 0.0;
    }


    /**
     * This method is to calculate the cache latency
     * @return time to access data within cache
     */
    public int calculateTimeToAccess(){
        int timeToAccess = 0;
        return timeToAccess;
    }
}
