package project.MiddlewareEnvironment.CacheMaintenance.SampleEA;

/**
 * Created by santhilata on 18/07/16.
 */

import project.MainClasses.CacheProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * The following class is a private supplement class to create cache unit
 */
public class CacheUnit implements CacheProperties{
    String cName; // name of the cache unit. For the time being it is only a (number+"")
    double availableSize; // size available still
    ArrayList<QueryData> residentQueries; // Query list within each cache unit



    public CacheUnit(String name){
        this.cName = name;
        this.availableSize = (new Random().nextDouble()*maxCache_size > maxCache_size/2)?maxCache_size:maxCache_size/3;
      //  this.availableSize = maxCache_size;
        this.residentQueries = new ArrayList<>();
    }

    public void addQuery(QueryData qd){
        this.residentQueries.add(qd);
        this.availableSize = this.availableSize-qd.getQuerySize();
    }

    public QueryData searchQuery(QueryData qd){

        Iterator<QueryData> itr = residentQueries.iterator();
        while (itr.hasNext()){
            QueryData qdt = itr.next();
            if (qdt.getQid().equals(qd.getQid()))
                return qdt;
        }
        return  null;
    }

    public boolean deleteQuery(QueryData qd){
        QueryData qdt = searchQuery(qd);

        if (qdt != null) {
            this.residentQueries.remove(qdt);
            this.availableSize += qdt.getQuerySize();
            return true;
        }
        return  false;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public double getAvailableSize() {
        return availableSize;
    }

    public ArrayList<QueryData> getResidentQueries() {
        return residentQueries;
    }

    public boolean isCacheContainsQuery(QueryData qd){

        Iterator<QueryData> itr = residentQueries.iterator();

        while (itr.hasNext()){
            QueryData resQ = itr.next();
            if (resQ.getQid().equals(qd.getQid())){
                return true;
            }
        }

        return false;
    }
}
