package project.MiddlewareEnvironment.CacheMaintenance.SampleEA;

/**
 * Created by santhilata on 18/07/16.
 */

import project.MainClasses.CacheProperties;
import project.SupportSystem.MapUtil;

import java.util.*;

/**
 * The following class contains data about a query for one time window (uLoc_frequency)
 * But contains information
 */
public class QueryData implements CacheProperties{
    String qId;
    String cLoc =""; // current cache location
    String[] cLocRecommend ;
    int queryComplexity;
    double querySize; // query data volume in GB
    String refresh;
    HashMap<String,Integer> uLoc_frequency; // frequency from all user locations per time window
    int totalFreq=0; // this total freq is used for frequency based threshold calculation for this window
    int freq_5windows; // frequency over last five windows
    HashMap<String, Integer> cLoc_time; // time last requested at cache locations
    int timeRecent = 9999;// this is the recent epoch this query was used overall
    boolean foundInCache;
    HashMap<String,Integer> queryAssociation;

    public QueryData(){ }

    public QueryData(String qid){
        this.qId = qid;
        this.queryComplexity = new Random().nextInt(upperQueryComplexity)+1;// to avoid zero
        this.querySize = new Random().nextDouble()*upperDataSize;
        this.refresh = Refresh[new Random().nextInt(2)]; // gets a value either rare or often
      //  this.cLocRecommend = new String[featureStr.length()];
        this.cLocRecommend = new String[cacheUnits];
        Arrays.fill(this.cLocRecommend,"");
        uLoc_frequency = new HashMap<>();
        for (int i = 0; i < numUserContainers; i++) {
            int freq = new Random().nextInt(upperLimit_Freq);
            if (freq >= upperLimit_Freq/2) uLoc_frequency.put((""+(i+1)),0);
            else uLoc_frequency.put((""+(i+1)),freq);
            totalFreq += freq;
        }

        freq_5windows = totalFreq;// for a new query
        this.cLoc_time = new HashMap<>(upperTime_WindowLimit);
        this.foundInCache = false;
        this.queryAssociation =  new HashMap<>();
    }

    public QueryData (QueryData qd_original) {

        this.qId = qd_original.getqId();
        this.queryComplexity = qd_original.getQueryComplexity();
        this.cLoc = qd_original.cLoc;
       // this.cLocRecommend = new String[featureStr.length()];
       // for (int i = 0; i < featureStr.length() ; i++) {
        this.cLocRecommend = new String[cacheUnits];
        for (int i = 0; i < cacheUnits ; i++) {

            this.cLocRecommend[i] = qd_original.getcLocRecommend()[i];
        }

        this.querySize = qd_original.getQuerySize();
        this.refresh =  qd_original.getRefresh();
        this.uLoc_frequency = new HashMap<>();
        MapUtil.copyHashMap(qd_original.getuLoc_frequency(), this.uLoc_frequency);
        this.totalFreq = qd_original.getTotalFreq();
        this.freq_5windows = qd_original.getFreq_5windows();
        this.cLoc_time = new HashMap<>();
        MapUtil.copyHashMap(qd_original.cLoc_time,this.cLoc_time);
        this.timeRecent = qd_original.timeRecent;
        this.foundInCache = qd_original.foundInCache;
        MapUtil.copyHashMap(qd_original.queryAssociation,this.queryAssociation);
        //copy HashTable
    }

    public String getqId() {
        return qId;
    }

    public void setqId(String qId) {
        this.qId = qId;
    }

    public String getRefresh() {
        return refresh;
    }

    public void setRefresh(String refresh) {
        this.refresh = refresh;
    }

    public void setTotalFreq(int totalFreq) {
        this.totalFreq = totalFreq;
    }

    public void setcLoc_time(HashMap<String, Integer> cLoc_time) {
        MapUtil.copyHashMap(cLoc_time,this.cLoc_time);
    }

    public void setTimeRecent(int timeRecent) {
        this.timeRecent = timeRecent;
    }

    public int getTotalFreq() {
        return totalFreq;
    }

    public double getQuerySize() {
        return querySize;
    }

    public String getQid(){
        return qId;
    }

    public String getcLoc() {
        return cLoc;
    }

    public void setcLoc(String cLoc) {
        this.cLoc = cLoc;
        //this.cLoc_time.put(cLoc,0);
    }

    public int getTimeRecent() {
        return timeRecent;
    }

    public void setOverallTimeRecent() {

        Iterator<String> iterKeys = this.cLoc_time.keySet().iterator();
        int min = 9999;
        while(iterKeys.hasNext()){
            int time = this.cLoc_time.get(iterKeys.next());
            if (time < min){
                min = time;
            }
        }
        this.timeRecent = min;
    }
    public void setQueryAssociation(HashMap<String,Integer> queryAssociation){
        MapUtil.copyHashMap(queryAssociation,this.queryAssociation);
    }

    public HashMap<String,Integer> getQueryAssociation(){
        return this.queryAssociation;
    }

    public void setuLoc_frequency(HashMap<String, Integer> uLoc_frequency) {

        MapUtil.copyHashMap( uLoc_frequency,this.uLoc_frequency);
    }

    public HashMap<String, Integer> getuLoc_frequency() {
        return uLoc_frequency;
    }

    public boolean isFoundInCache() {
        return foundInCache;
    }

    public void setFoundInCache(boolean foundInCache) {
        this.foundInCache = foundInCache;
    }

    public void setcLocRecommend(String[] cLocRecommend) {

        this.cLocRecommend = cLocRecommend.clone();
    }

    public String[] getcLocRecommend() {
        return cLocRecommend;
    }

    public int getQueryComplexity() {
        return queryComplexity;
    }

    public void setQueryComplexity(int queryComplexity) {
        this.queryComplexity = queryComplexity;
    }

    /**
     * it is possible to have some locations and time as 9999.
     * But a query can be within cache for continuous time windows
     * @param cacheLoc
     * @param time
     */
    public void setCloc_time(String cacheLoc, int time){
        this.cLoc_time.put(cacheLoc,time);
    }

    public HashMap<String, Integer> getcLoc_time() {
        return cLoc_time;
    }

    public int getFreq_5windows() {
        return freq_5windows;
    }

    public void setFreq_5windows(int freq_5windows) {
        this.freq_5windows = freq_5windows;
    }

    public void addToFreq_5windows(int freq_5windows){
        this.freq_5windows += freq_5windows;
    }

    public void setTotalFreq(){
        Iterator<String> itr = uLoc_frequency.keySet().iterator();
        int total = 0;
        while(itr.hasNext()){
            total += uLoc_frequency.get(itr.next());
        }

        totalFreq = total;
    }

    public void setQuerySize(double querySize) {
        this.querySize = querySize;
    }

}