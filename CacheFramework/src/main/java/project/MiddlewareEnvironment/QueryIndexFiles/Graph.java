package project.MiddlewareEnvironment.QueryIndexFiles;

/**
 * Created by santhilata on 27/04/15.
 */

import project.MainClasses.CacheProperties;
import project.MiddlewareEnvironment.Cache;
import project.MainClasses.PropertiesLoaderImplBase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;



import project.QueryEnvironment.ExprTreeNode;
import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.Query_Subj_Predicate;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by santhilata on 19/03/15.
 * There are atleast 20 bags in a graph.( 20 is an arbitrary number )
 * In a graph, top 10 bags are for frequent queries and
 * bottom ten bags are for infrequent queries
 * So, when inserting queries, they are inserted from the bottom.
 * When searched for a new query, they are searched from the top.
 */
public class Graph implements CacheProperties {


    private  Bag[] addressArray;
    private QueryIndex myQueryIndex;

    static final int no_of_bags=8;
    ArrayList<ExpressionTree> insertIntoGraphList = new ArrayList<>();

    private  static int number_addedQueries=0;
    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static Lock readLock = rwl.readLock();
    private static Lock writeLock = rwl.writeLock();

    int number_cacheHits =0;
    int number_cacheAccesses=0;

    // constructor
    public Graph(){
        addressArray = new Bag[no_of_bags];
        for (int i=0; i<no_of_bags; i++){
            addressArray[i] = new Bag();
        }
    }

    public QueryIndex getMyQueryIndex() {
        return myQueryIndex;
    }

    public void setMyQueryIndex(QueryIndex myQueryIndex) {
        this.myQueryIndex = myQueryIndex;
    }

    //get and set methods
    public Bag[] getAddressArray(){
        return this.addressArray;
    }

    /**
     * Gets bag at a particular level
     * @param level
     * @return
     */
    public Bag getBag(int level) {
        Bag bag = this.addressArray[level];
        return bag;
    }

    public int getNumber_addedQueries_atMaintenance() {
        int addedQueries= number_addedQueries;
        number_addedQueries =0;
        return addedQueries;
    }

    public static void setNumber_addedQueries(int number_addedQueries) {
        Graph.number_addedQueries = number_addedQueries;
    }

    public Cache_Reply searchGraph(ExpressionTree expressionTree){
        Cache_Reply cacheReply =null ;

        int query_size = expressionTree.getNo_of_Nodes();

        //first search in the full containment
        for(int bagSize=(query_size-1); bagSize < no_of_bags; bagSize++){
            Bag bag = addressArray[bagSize];

            if (!bag.isBagEmpty()){

                cacheReply = bag.searchBag(new IndexedQuery(expressionTree));
                number_cacheAccesses++;
                /**
                 * This is the case when searched query is a part of a bigger query
                 */
                if (cacheReply.getReplyStatus().equals(FULLY_FOUND))  {

                    number_cacheHits++;
                    if (bagSize >= expressionTree.getNo_of_Nodes()){// this is correct - dont change anything

                        if(!insertIntoGraphList.contains(expressionTree))
                            insertIntoGraphList.add(expressionTree);
                    }

                    break;
                }
            }
        }//for loop

        if (cacheReply== null){ // first query- all higher bags are empty case

            if(!insertIntoGraphList.contains(expressionTree)) {
                insertIntoGraphList.add(expressionTree);
            }
            boolean allbagsEmpty= true;
            for (int i = 0; i < no_of_bags ; i++) {
                if (!addressArray[i].isBagEmpty())  {
                    allbagsEmpty = false;
                    break;
                }
            }

            if (allbagsEmpty)
                cacheReply = new Cache_Reply(expressionTree.getExpressionTreeID(),NOT_FOUND,expressionTree,0); // the first Query

            else if (!searchCriteria.equals(FULL_QUERY_SEARCH_ONLY))
                cacheReply = searchForPartialQuery(expressionTree);
        }

        /**
         * Not Found - The following code checks whether any smaller sub parts of the query is found in the
         * cache
         * Partially Found - This query is part of some other bigger query.
         */
        else if(cacheReply.getReplyStatus().equals(NOT_FOUND)||cacheReply.getReplyStatus().equals(PARTIALLY_FOUND)) {

            if(!insertIntoGraphList.contains(expressionTree))
                insertIntoGraphList.add(expressionTree);

            if (!searchCriteria.equals(FULL_QUERY_SEARCH_ONLY))

                cacheReply = searchForPartialQuery(expressionTree);
        }


        for(ExpressionTree et: insertIntoGraphList){

            insertIntoGraph(et);
        }
        insertIntoGraphList.clear();

        return  cacheReply;
    }

    /**
     * search for the right cache first and then,
     * insert into bags that contains trees of same size
     * @param expressionTree
     *
     * find_a_cache(...) function searches for a suitable cache according to the given criteria
     * attached to the query index. this function is implemented in the class QueryIndex.
     */
    public void insertIntoGraph(ExpressionTree expressionTree){
        int j = expressionTree.getNo_of_Nodes();

        // only search for a local empty cache
        if (selectedCacheStoreCriteria.equals(NEARBY_CRITERIA) || selectedCacheStoreCriteria.equals
         (DATASIZE_CRITERIA)) {
            Cache cache = getMyQueryIndex().find_A_Cache(expressionTree);
            if (cache == null) {
                System.out.println("All cache units are full. So discarding data / saving in the unsaved queries");
            } else {
                for (int i = j - 1; i < no_of_bags; i++) {
                    Bag bag = addressArray[i];

                    if (!bag.isFull()) {
                        bag.bag_Insert(new Pennant(new IndexedQuery(expressionTree, cache)));
                        number_addedQueries++;
                        break;
                    }

                }
            }
        }// search criteria
    }

    /**
     * The following method creates an Arraylist of all intermediate nodes of the tree
     * Each node is a combination of sub-queries as they appear in sequences.
     * Leaf nodes being the atomic sub-queries that cannot be sub divided further.
     * All these nodes are set with the time-last-queried same as the parent query
     * @param expressionTree
     * @return
     */
    public Cache_Reply  searchForPartialQuery(ExpressionTree expressionTree){
        ArrayList<ExprTreeNode> arrayTree = expressionTree.packTreeInBFS_array();
        int time_to_set_for_LastAccessed = expressionTree.getTime_queried();
        Cache_Reply cacheReply = null;



        boolean partiallyFound = false;
        int no_ofNodesFound = 0;

        for (int i = 0; i < arrayTree.size() ; i++) {

            if (arrayTree.get(i).getParent() != null){
                if (arrayTree.get(i).getParent().isNodeFoundInCache()){
                    arrayTree.get(i).setNodeFoundInCache(true);
                }
            }

            if(!arrayTree.get(i).isNodeFoundInCache()){
                Query_Subj_Predicate throwingError =  arrayTree.get(i).getValue();

                ExpressionTree et = new ExpressionTree(throwingError);
                et.setTime_queried(time_to_set_for_LastAccessed);
                et.setQspLocation(expressionTree.getQspLocation());// setting query location for each of the segments

                IndexedQuery iq = new IndexedQuery(et);// creating an indexed query to search in the cache
                int bagSize = et.getNo_of_Nodes();

                for (int j = bagSize-1; j < no_of_bags; j++) {
                    Bag bag = addressArray[j];

                    if (!bag.isBagEmpty()) {
                        cacheReply = bag.searchBag(iq);
                        number_cacheAccesses++;
                        if (cacheReply.getReplyStatus().equals(FULLY_FOUND)){

                            arrayTree.get(i).setNodeFoundInCache(true);
                            no_ofNodesFound++;
                            partiallyFound = true;
                            number_cacheHits++;
                            //including frequent query fragment to graph

                            if(j >= bagSize)
                                insertIntoGraphList.add(et);

                        }

                    }

                }
            }
        }

        expressionTree.setFoundTree();
        //   System.out.println(" from search partial "+expressionTree.getExpressionTreeID());
        if(cacheReply==null){
            cacheReply = new Cache_Reply();
            cacheReply.setReplyStatus(NOT_FOUND);
        }

        cacheReply.setPartialExpressionTree(expressionTree);
        cacheReply.setQueryID(expressionTree.getExpressionTreeID());

        if (partiallyFound) {
            cacheReply.setReplyStatus(PARTIALLY_FOUND);

        }

        return cacheReply;
    }

    /**
     * The following code just rearranges childnodes of the root according to the descending order.
     * This is to ensure that biggest possible chunk is found
     * bubble sort
     * @param expressionTree
     */
    private  ExpressionTree[] createDescendingChildList(ExpressionTree expressionTree){
        ExprTreeNode[] childList = expressionTree.getRoot().getChildren();
        ExpressionTree[] treeListDesc = new ExpressionTree[childList.length];
        for (int i = 0; i < treeListDesc.length && childList[i]!=null; i++) {
            treeListDesc[i] = new ExpressionTree(childList[i].getValue());
        }
        // now the biggest child first

        while (true ) {
            boolean swapped = false;
            for (int i = 1; i < treeListDesc.length && treeListDesc[i]!= null; i++) {
                if (treeListDesc[i].getNo_of_Nodes() > treeListDesc[i-1].getNo_of_Nodes()){

                    ExpressionTree tempTree = treeListDesc[i];
                    treeListDesc[i]=treeListDesc[i-1];
                    treeListDesc[i-1] = tempTree;

                }
                swapped = true;

            }
            if(swapped) break;
        }

        return treeListDesc;
    }

    /**
     * counting number of queries in the graph
     */
    public int countNumberOfQueries()    {
        Bag bag;
        int totalQueries=0;
        for (int i=0; i<addressArray.length; i++){

            bag = this.getBag(i);

            if (!bag.isBagEmpty()) {
                totalQueries += bag.bagSize();
            }
        }
        return  totalQueries;
    }// end of the method countNumberOfQueries()

    /**
     * Printing the graph. Gets bag at each level and prints the pennants.
     */
    public void printGraph()    {
        Bag bag;
        for (int i=0; i<addressArray.length; i++) {
            bag = this.getBag(i);

            if (!bag.isBagEmpty()) {

                System.out.println(" printing the bag at level: "+i);
                System.out.println("No.of queries in the Bag= "+bag.bagSize());
                bag.printBag();
            }
            else{
                System.out.println("bag empty at level "+i);
            }
        }
    }// end of the method printGraph

    /**
     * This method is to delete old query segments after a given time threshold
     * @param time_threshold
     */
    public UpdatedData deleteExpiredQueries(int time_threshold,int ticks) {
        Bag bag;
        double deletedDatasize =0; //from cache
        double dataSizeInAllCaches =0; // aggregated cache needs in All caches

        UpdatedData tempDataReceived = null;
        UpdatedData dataReceived = new UpdatedData();

        int totalDeletedQueries = 0;
        ArrayList<IndexedQuery> deletedQueries = new ArrayList<>();

        dataReceived.setQueriesInTheGraphBefore(this.countNumberOfQueries());
        for (int i=0; i<addressArray.length; i++)
        {
            //     System.out.println(" deleting the clusters in the bag at level: "+i);
            bag = this.getBag(i);
            tempDataReceived = bag.deleteOldQueries(time_threshold, ticks);
            totalDeletedQueries += tempDataReceived.getNumber_deletedQueries();

            if(tempDataReceived!= null && tempDataReceived.getDeletedQueries()!= null)
                deletedQueries.addAll(tempDataReceived.getDeletedQueries());


            // delete data  from bag and cache
            if(tempDataReceived.deletedQueries !=null) {
                for (IndexedQuery cl : tempDataReceived.deletedQueries) {
                    double tempDatadeleted = cl.getDataSize();
                    cl.getCacheAddress().deleteDataFromCache(tempDatadeleted);
                    deletedDatasize += tempDatadeleted;
                }
            }
        }

        dataReceived.setNumber_deletedQueries(totalDeletedQueries);
        dataReceived.setDeletedQueries(deletedQueries);
        dataReceived.setQueriesInTheGraphAfter(this.countNumberOfQueries());
        dataReceived.setDeletedDataSize(deletedDatasize);

        for (Cache cache: myQueryIndex.getCache()) {
            dataSizeInAllCaches += cache.getOccupiedData();
        }

        dataReceived.setDataSizeInCache(dataSizeInAllCaches);
        dataReceived.setCacheHits(number_cacheHits);
        dataReceived.setCacheAccesses(number_cacheAccesses);
        number_cacheAccesses=0;
        number_cacheHits=0;



        return dataReceived;
    }

    /**
     * This method is to delete old query segments after a given frequency threshold
     * @param frequency_threshold
     */
    public UpdatedData deleteLessFrequentQueries(int frequency_threshold, int ticks){
        Bag bag;
        double deletedDatasize =0; //from cache
        double dataSizeInAllCaches =0; // aggregated cache needs in All caches

        UpdatedData tempDataReceived ;
        UpdatedData dataReceived = new UpdatedData();

        int totalDeletedQueries = 0;
        ArrayList<IndexedQuery> deletedQueries = new ArrayList<>();

        dataReceived.setQueriesInTheGraphBefore(this.countNumberOfQueries());

        for (int i=0; i<addressArray.length; i++)
        {
            //     System.out.println(" deleting the clusters in the bag at level: "+i);
            bag = this.getBag(i);
            tempDataReceived = bag.deleteLowFrequentQueries(frequency_threshold,ticks);
            totalDeletedQueries += tempDataReceived.getNumber_deletedQueries();

            if(tempDataReceived!= null && tempDataReceived.getDeletedQueries()!= null)
                deletedQueries.addAll(tempDataReceived.getDeletedQueries());

            // delete data  from bag and cache
            if(tempDataReceived.deletedQueries !=null) {
                for (IndexedQuery cl : tempDataReceived.deletedQueries) {
                    double tempDatadeleted = cl.getDataSize();
                    cl.getCacheAddress().deleteDataFromCache(tempDatadeleted);
                    deletedDatasize += tempDatadeleted;
                }
            }
        }
        dataReceived.setNumber_deletedQueries(totalDeletedQueries);
        dataReceived.setDeletedQueries(deletedQueries);
        dataReceived.setQueriesInTheGraphAfter(this.countNumberOfQueries());
        dataReceived.setDeletedDataSize(deletedDatasize);

        for (Cache cache: myQueryIndex.getCache()) {
            dataSizeInAllCaches += cache.getOccupiedData();
        }

        dataReceived.setDataSizeInCache(dataSizeInAllCaches);
        dataReceived.setCacheHits(number_cacheHits);
        dataReceived.setCacheAccesses(number_cacheAccesses);
        number_cacheAccesses=0;
        number_cacheHits=0;

        return dataReceived;
    }

    public static void main(String[] args) {
        int totalQueries=10;
        Bag bag = new Bag();
        Query_Subj_Predicate[] qsps = new Query_Subj_Predicate[totalQueries];
        ExpressionTree[] ets = new ExpressionTree[totalQueries];
        IndexedQuery[] iqs = new IndexedQuery[totalQueries];
        Graph graph = new Graph();


        qsps[0] = new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)");
        qsps[1] = new Query_Subj_Predicate("(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)_(<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>)");

        qsps[2] = new Query_Subj_Predicate("(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)_(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");
        qsps[3] = new Query_Subj_Predicate("(<atb24:tab4:db3><at4:tab34:db3><slr23:tab43:db3>#<san4:tab24:db3,atb64:tab4:db3,200to300>)");
        qsps[4]= new Query_Subj_Predicate("(<xlr62:tab42:db3><at2:tab32:db3>#<sand82:tab22:db2,gt222>)_(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");
        qsps[5] = new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)");
        qsps[6] = new Query_Subj_Predicate("(<qry32:tab22:db2><axt412:tab12:db2><jkat22:tab32:db2>#<qry34:tab24:db4," +
                "gt345>)&(<jkat25:tab35:db5>#<qry33:tab23:db3,gt345>)");
        qsps[7] = new Query_Subj_Predicate("(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4," +
                "eq33>)_(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");
        qsps[8] = new Query_Subj_Predicate("(<atb24:tab4:db3><at4:tab34:db3><slr23:tab43:db3>#<san4:tab24:db3," +
                "atb64:tab4:db3,200to300>)");
        qsps[9]= new Query_Subj_Predicate("(<xlr62:tab42:db3><at2:tab32:db3>#<sand82:tab22:db2,gt222>)_" +
                "(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)");

        for (int i = 0; i <10; i++) {
            // System.out.println("queries are = "+qsps[i].getQueryID()+" "+qsps[i].getQueryExpression());
            ets[i] = new ExpressionTree(qsps[i]);
            iqs[i] = new IndexedQuery(ets[i]);
            //Pennant p = new Pennant(iqs[i]);
            //bag.bag_Insert(p);

            graph.insertIntoGraph(ets[i]);
        }
      /*  ExpressionTree[] aList = graph.createDescendingChildList(ets[1]);
        for (int i = 0; i <aList.length && aList[i] != null; i++) {
            System.out.println(aList[i].getRoot().getValue().getQueryExpression());
            System.out.println(aList[i].getNo_of_Nodes());
        }*/

        ExpressionTree et = new ExpressionTree(new Query_Subj_Predicate("((<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)_((<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)_" +
                "(<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>)))&(<jkat23335:tab35:db5>#<qry378:tab23:db3,gt345>)"));




        Cache_Reply testTree = graph.searchForPartialQuery(et);


        ArrayList<Query_Subj_Predicate> subQueries = testTree.getPartialExpressionTree().getSubQueries();
        Iterator<Query_Subj_Predicate> itr = subQueries.iterator();

        while(itr.hasNext()){
            System.out.println(itr.next().getQueryExpression());
        }

        subQueries = testTree.getPartialExpressionTree().set_GetQueriesNotFoundTree();
        itr = subQueries.iterator();

        while(itr.hasNext()){
            System.out.println(itr.next().getQueryExpression());
        }

    }

}
