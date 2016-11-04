package project.MiddlewareEnvironment.CacheMaintenance.EA;

import project.MainClasses.CacheProperties;
import project.MiddlewareEnvironment.Cache;
import project.MiddlewareEnvironment.Container;
import project.MiddlewareEnvironment.QueryIndexFiles.*;
import project.QueryEnvironment.ExpressionTree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by santhilata on 29/04/16.
 *
 * This class is mainly created to reduce  load on local query indexes
 * This class is superior class to all containers.
 * It runs continuously . This class is created to implement the hierarchy of query indexes in the architecture
 * QueryIndexes of each of the containers are added to this class
 * This class is responsible for
 * 1. gathering data from individual query indexes
 * 2. deciding upon a data location for a data fragment that cannot find place in the local container
 * 3. Running online algorithm for data placement
 * 4. running periodical mobility algorithms
 * 5. Running cache maintenance algorithms
 */
public class SuperQueryIndex implements CacheProperties{


    ArrayList<QueryIndex> queryIndices = new ArrayList<>();
    Container[] neighbouringContainers; // all containers list
    ArrayList<ExpressionTree> interestedQueries = new ArrayList<>(); // this list contains all queries that were
    //queried but could not find place. Interest is noted.


    File statisticsFile ; // File containing statistics (meta-data) for each epoch

    public SuperQueryIndex(){

    }

    public ArrayList<QueryIndex> getQueryIndices() {
        return queryIndices;
    }

    public void setQueryIndices(ArrayList<QueryIndex> queryIndices) {
        this.queryIndices = queryIndices;
    }

    public ArrayList<ExpressionTree> getInterestedQueries() {
        return interestedQueries;
    }

    public void setInterestedQueries(ArrayList<ExpressionTree> interestedQueries) {
        this.interestedQueries = interestedQueries;
    }

    public Container[] getNeighbouringContainers() {
        return neighbouringContainers;
    }

    public void setNeighbouringContainers(Container[] neighbouringContainers) {
        this.neighbouringContainers = neighbouringContainers;
    }

    public File getStatisticsFile() {
        return statisticsFile;
    }

    public void setStatisticsFile(File file) {
        this.statisticsFile = file;
    }


    /**
     * Finds an empty cache globally
     * @param QI
     * @param searchCacheCriteria
     * @param expressionTree
     * @return
     */
    public Cache find_A_Cache_Globally(QueryIndex QI, String searchCacheCriteria, ExpressionTree expressionTree){
        Cache cache = null;
        int requiredDataSize = expressionTree.getNo_of_Nodes() * standard_Data_Unit_Size;
        String queryLocation = expressionTree.getQspLocation();

        int timeQueried = expressionTree.getTime_queried();

        switch (searchCacheCriteria){
            /**
             * NEARBY_CRITERIA is the greedy approach - Follow the leader
             * The simplest learning rule to try is to select (at the current step) the hypothesis that has the least loss over all past rounds.
             * This algorithm is called Follow the leader, and is simply given by:

             In round t , set

             w_t = \operatorname*{arg\,min}_{w \in S} \sum_{i=1}^{t-1} v_i(w)

             Here, ties are broken arbitrarily. This method can thus be looked as a greedy algorithm.
             For the case of online quadratic optimization (where the loss function is v_t(w) = || w - x_t ||_2^2 ),
             one can show a regret bound that grows as \log(T) .[4] However, similar bounds cannot be obtained for
             the FTL algorithm for other important families of models like online linear optimization etc.
             To do so, one modifies FTL by adding regularisation.

             source: https://en.wikipedia.org/wiki/Online_machine_learning
             */
            case GLOBAL_NEARBY_CRITERIA:{

                boolean cacheFound = false;
                char userLocation = queryLocation.charAt(4);
                for(Cache cache1: QI.getCache()){
                    if (userLocation == cache1.getCacheAddress().charAt(10)) {
                        if (cache1.getDataSizeAvailable() > requiredDataSize) {
                            cacheFound = true;
                            cache = cache1;
                            break;
                        }
                    }
                }
                if (!cacheFound){
                    int uloc = Character.getNumericValue(userLocation);
                    getNeighbours(uloc,uloc+1,numContainers);
                    for (int i = 0; i < neighbourList.size() ; i++) {
                        Container container = getContainerByName(i);
                        for(Cache cache1: container.getCacheSet()){
                            if (cache1.getDataSizeAvailable() > requiredDataSize) {
                                cacheFound = true;
                                cache = cache1;

                                break;
                            }
                        }
                    }
                    if(!cacheFound){
                        System.out.println("No cache has free space in any container. Hence data is discarded.");
                    }
                }


                break;
            }
            /**
             * Simplest criteria. It places data on the first available cache of the first containers
             */
            case GLOBAL_DATA_CRITERIA: {

                for (int i = 0; i < numContainers; i++) {
                    Container container = getContainerByName(i);
                    QueryIndex qi = container.getQueryIndex();
                    for (Cache cache1: qi.getCache()){
                        if (cache1.getDataSizeAvailable() > requiredDataSize){
                            cache = cache1;
                            break;
                        }
                    }

                }

                break;
            }

            /**
             * This is a randomized method. Randomly picks a container and places there.
             *  caches random container and size availability onto other nearby containers too
             *  nearby containers have the increased  visibility by one step at any given time
             */
            case GLOBAL_RANDOMIZED: {

                Random random = new Random();
                boolean cacheFound = false;
                Container container = getContainerByName(random.nextInt(numContainers));
                QueryIndex qi = container.getQueryIndex();
                for (Cache cache1: qi.getCache()){
                    if (cache1.getDataSizeAvailable() > requiredDataSize){
                        cache = cache1;
                        break;
                    }
                }
                if (!cacheFound)
                    System.out.println("No place in the container. Hence data discarded");


                break;
            }

            default: {
                break;
            }
        }

        return cache;
    }

    /**
     * the following private method is to expand visibility of neighbours by one on either side
     */
    ArrayList neighbourList = new ArrayList();
    private void getNeighbours(int curr,int next, int Max){

        if  (curr<0  || next>Max )
            return;
        neighbourList.add(curr);
        neighbourList.add(next);
        // System.out.println(curr+" "+next);
        getNeighbours(curr-1,next+1,Max);

    }
    private Container getContainerByName(int i){
        Container container = null;

        for (int j = 0; j < neighbouringContainers.length; j++) {
            String name = neighbouringContainers[j].getContainer_name();
            if (i == name.charAt(10)){
                container = neighbouringContainers[j];
                break;
            }
        }
        return container;
    }
  //-----------------------------------------------------------------------------------------------------------------------


    //---------------------------------------------------------------------------------------------------------------------



    //---------------------------------------------------------------------------------------------------------------------

    /**
     *  Following function is to get statistics for number of epochs
     *  This method stores meta data about queries per Epoch in different files
     */
    public void generateMetaDataStatisticsPerEpoch() throws IOException {
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


