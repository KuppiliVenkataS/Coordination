package project.MiddlewareEnvironment.QueryIndexFiles;

import java.util.ArrayList;

/**
 * Created by santhilata on 11/09/15.
 * This class contains all information related to cache maintenance during one cache maintenance  period
 * This updatedData stores data for a single queryIndex graph
 */
public class UpdatedData {
    private int number_addedQueries;
    private int number_deletedQueries;
    private int queriesInTheGraphBefore;
    private int queriesInTheGraphAfter;

    ArrayList<IndexedQuery> addedQueries =  new ArrayList<>();
    ArrayList<IndexedQuery> deletedQueries = new ArrayList<>();


    private int cacheHits;
    private int cacheAccesses;

    private double deletedDataSize=0;
    private double dataSizeInCache=0;

    public UpdatedData(){
    }

    public UpdatedData(UpdatedData another){
        this.number_deletedQueries = another.number_deletedQueries;
        this.queriesInTheGraphAfter = another.queriesInTheGraphAfter;
        this.queriesInTheGraphBefore = another.queriesInTheGraphBefore;

        if(another.deletedQueries!= null)
        this.deletedQueries = another.deletedQueries;

        if(another.addedQueries != null)
            this.addedQueries = another.addedQueries;

        this.cacheHits = another.cacheHits;
        this.cacheAccesses = another.cacheAccesses;

    }

    public int getNumber_deletedQueries() {
        return number_deletedQueries;
    }

    public void setNumber_deletedQueries(int number_deletedQueries) {
        this.number_deletedQueries = number_deletedQueries;
    }

    public int getQueriesInTheGraphBefore() {
        return queriesInTheGraphBefore;
    }

    public void setQueriesInTheGraphBefore(int queriesInTheGraphBefore) {
        this.queriesInTheGraphBefore = queriesInTheGraphBefore;
    }

    public int getQueriesInTheGraphAfter() {
        return queriesInTheGraphAfter;
    }

    public void setQueriesInTheGraphAfter(int queriesInTheGraphAfter) {
        this.queriesInTheGraphAfter = queriesInTheGraphAfter;
    }

    public ArrayList<IndexedQuery> getDeletedQueries() {
        return deletedQueries;
    }

    public void setDeletedQueries(ArrayList<IndexedQuery> deletedQueries) {
        this.deletedQueries = deletedQueries;
    }

    public int getCacheHits() {
        return cacheHits;
    }

    public void setCacheHits(int cacheHits) {
        this.cacheHits = cacheHits;
    }

    public void addCacheHits(int cachehits){
        this.cacheHits+=cachehits;
    }

    public int getCacheAccesses() {
        return cacheAccesses;
    }

    public void setCacheAccesses(int cacheAccesses) {
        this.cacheAccesses = cacheAccesses;
    }

    public void addCacheAccesses(int cacheAccesses){
        this.cacheAccesses += cacheAccesses;
    }

    public void addDeletedQueries(IndexedQuery cl){
        this.number_deletedQueries++;
        this.deletedQueries.add(cl);
    }

    public void addToAddedQueries(IndexedQuery cl){
        this.number_addedQueries++;
        this.addedQueries.add(cl);
    }

    public int getNumber_addedQueries() {
        return number_addedQueries;
    }

    public void setNumber_addedQueries(int number_addedQueries) {
        this.number_addedQueries = number_addedQueries;
    }

    public ArrayList<IndexedQuery> getAddedQueries() {
        return addedQueries;
    }

    public void setAddedQueries(ArrayList<IndexedQuery> addedQueries) {
        this.addedQueries = addedQueries;
    }

    public double getDeletedDataSize() {
        return deletedDataSize;
    }

    public void setDeletedDataSize(double deletedDataSize) {
        this.deletedDataSize = deletedDataSize;
    }

    public double getDataSizeInCache() {
        return dataSizeInCache;
    }

    public void setDataSizeInCache(double dataSizeInCache) {
        this.dataSizeInCache = dataSizeInCache;
    }

    public void refreshData(){
        this.setQueriesInTheGraphAfter(0);
        this.setNumber_deletedQueries(0);
        this.setQueriesInTheGraphBefore(0);
        this.deletedQueries = new ArrayList<>();

    }



}
