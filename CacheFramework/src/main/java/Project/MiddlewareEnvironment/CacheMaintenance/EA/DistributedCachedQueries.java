package project.MiddlewareEnvironment.CacheMaintenance.EA;

import project.MainClasses.CacheProperties;
import project.MiddlewareEnvironment.QueryIndexFiles.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO: THIS FILE NEEDS TO BE CHANGED
 * Created by santhilata on 28/04/16.
 * This class to get statistics of cached data
 * This class is to collect meta-data
 * Time, frequency and
 * This Distributed queries is referred from SimpleMain class
 */
public class DistributedCachedQueries implements CacheProperties{

    private int numGloballyaddedQueries;
    private int numGloballydeletedQueries;
    private int queriesBefore;
    private int queriesAfter;

    ArrayList<IndexedQuery> addedQueries =  new ArrayList<>();
    ArrayList<IndexedQuery> deletedQueries = new ArrayList<>();
    Map<IndexedQuery,From_To> globalMobileQueries = new HashMap<>();

    private int globalCacheHits;
    private int globalCacheAccesses;

    private double deletedDataSize=0;
    private double dataSizeInCache=0;

    private class From_To{
        String from;
        String to;

        public From_To(String from, String to){
            this.from = from;
            this.to = to;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }
    }

    File statisticsFile ; // File containing statistics (meta-data) for each epoch


    public DistributedCachedQueries(){


    }

    public File getStatisticsFile() {
        return statisticsFile;
    }

    public void setStatisticsFile(File file) {
        this.statisticsFile = file;
    }

    public void collectTimeObsoleteQueries(){

    }

    public void refreshAllData(){
        this.globalCacheHits = 0;
        this.globalCacheAccesses = 0;
        this.deletedDataSize = 0;
        this.dataSizeInCache = 0;
        this.addedQueries = new ArrayList<>();
        this.deletedQueries = new ArrayList<>();
        this.globalMobileQueries = new HashMap<>();
        this.numGloballyaddedQueries = 0;
        this.numGloballydeletedQueries =0;

    }

    /**
     *  Following function is to get statistics for number of epochs
     *  This method stores meta data about queries per Epoch in different files
     */
    public void generateMetaDataStatisticsPerEpoch(QueryIndex[] queryIndices) throws IOException {
        //for each time epoch
        //collect statistics about frequency. time last accessed, query location list and cache location
        // store them in .csv  epochwise


        FileWriter fw = new FileWriter(this.statisticsFile);
        fw.flush();

        fw.append(" QueryID, Frequency, First Accessed, Last Accessed, Caches, data occupied, UserLocations");
        for (QueryIndex queryIndex: queryIndices) {
            Graph graph = queryIndex.getQueryIndexGraph();
            Bag bag;
            for (int i=0; i<graph.getAddressArray().length; i++) {
                bag = graph.getBag(i);

                if (!bag.isBagEmpty()) {

                    int no_of_pennant_roots = bag.getPennant_root_list().length;

                    Pennant p;

                    for(int k=0; k<no_of_pennant_roots; k++) {
                        if ((p = bag.getPennant_root_list()[k]) != null)

                            p = writeDataIntoFile(p,fw);
                    }
                }
                else{
                    System.out.println("bag is empty at level "+i);
                }
            }
        }


        fw.close();

    }

    private Pennant writeDataIntoFile(Pennant p, FileWriter fw) throws IOException {

        if(p != null) {
            writeDataIntoFile(p.getLeftSubTree(),fw);

            System.out.println(p.getRoot().toString() + "; ");
            IndexedQuery iq = p.getRoot();
            fw.append(iq.getQueryID()+","+iq.getFrequency()+","+iq.getLastAccessed()+","+iq.getCache().getCacheName()+","+iq.getDataSize()+",");

            Iterator<String> itr = iq.getQueryLocation().iterator();
            String str = "";
            while(itr.hasNext()){
                str += itr.next();
            }
            fw.append(str.substring(0,str.length()-1));// to remove last character
            fw.append("\n");

            writeDataIntoFile(p.getRightSubTree(),fw);
        }
        return p;
    }
}
