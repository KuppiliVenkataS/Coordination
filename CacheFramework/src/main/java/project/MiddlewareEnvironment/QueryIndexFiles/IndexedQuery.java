package project.MiddlewareEnvironment.QueryIndexFiles;

import project.MiddlewareEnvironment.Cache;
import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.Query_Subj_Predicate;

import java.util.ArrayList;
import java.util.Random;

/**
 * Indexed query is the query segment object that querybase stores.
 * Created by santhilata on 04/02/15.
 * Last edited 09/02/2015
 */
public class IndexedQuery {

    protected String queryIndexID; // this queryIndexID is not the same as the one in query from user. This is something for query index
    //private Query_Subj_Predicate querySegment;
    private ExpressionTree queryExpressionTree;
    private int firstAccessed; // query first entered into the cache - int ticks
    private int lastAccessed; // query last used for time based algorithms (such as LRU)- int ticks
    private int frequency; // number of times query is used
    private boolean presentInCache=false;
    private Cache cache;// name of the cache where this query is stored
    private ArrayList<String> queryLocation = new ArrayList<>(); // name of the location from where the query was sent
    private double dataSize; //data found in GB


    private static int i=1;
    double blockDataSize=10.0; //GB

    public IndexedQuery(){
        Random randomGenerator = new Random();
        queryIndexID = System.currentTimeMillis()+randomGenerator.nextInt()+"";
        this.queryExpressionTree = new ExpressionTree();
              this.frequency = 1;

    }

    public IndexedQuery(String queryID, ExpressionTree et,  int frequency, Cache cache) {
        this.queryIndexID = queryID;
        this.queryExpressionTree= et;
        this.setLastAccessed(et.getTime_queried());
        this.frequency = frequency;
        this.cache = cache;
        this.setDataSize(blockDataSize*et.getNo_of_Nodes());
    }

    /**
     * This constructor is called when the query is set in cache for the first time
     * Hence the attribute first accessed is set.
     * @param expressionTree
     * @param cache
     */
    public IndexedQuery(ExpressionTree expressionTree, Cache cache) {

        if(expressionTree.getExpressionTreeID()!=null)
            this.queryIndexID = expressionTree.getExpressionTreeID();
        else {
            Random randomGenerator = new Random();
            expressionTree.setExpressionTreeID( System.currentTimeMillis()+randomGenerator.nextInt()+"");
            queryIndexID =   expressionTree.getExpressionTreeID();
        }
        this.queryExpressionTree = expressionTree;
        this.setFirstAccessed(expressionTree.getTime_queried());
        this.setLastAccessed(expressionTree.getTime_queried());
        this.frequency = 1;
        double addedData = blockDataSize*expressionTree.getNo_of_Nodes();
        cache.addDataToCache(addedData);
        this.setDataSize(addedData);
        this.cache = cache;
        cache.addQueryToList(this);
    }

    /**
     * This constructor is called when queries are searched in the cache.
     * So no first accessed attribute and no cache location
     * @param expressionTree
     */
    public IndexedQuery(ExpressionTree expressionTree) {

       if(expressionTree.getExpressionTreeID()!=null)
        this.queryIndexID = expressionTree.getExpressionTreeID();
        else {
           Random randomGenerator = new Random();
           expressionTree.setExpressionTreeID( System.currentTimeMillis()+randomGenerator.nextInt()+"");
           queryIndexID =   expressionTree.getExpressionTreeID();
       }
        this.queryExpressionTree = expressionTree;

        this.setLastAccessed(expressionTree.getTime_queried());
        this.queryLocation.add(expressionTree.getQspLocation());
        this.frequency = 1;
        this.setDataSize(blockDataSize*expressionTree.getNo_of_Nodes());
    }

    public IndexedQuery(ExpressionTree expressionTree,int frequency){
        Random randomGenerator = new Random();
        queryIndexID = System.currentTimeMillis()+randomGenerator.nextInt()+"";
        this.queryExpressionTree = expressionTree;

        this.setLastAccessed(expressionTree.getTime_queried());
        this.frequency = frequency;
        this.setDataSize(blockDataSize*expressionTree.getNo_of_Nodes());
    }

    public boolean isPresentInCache(){

        if (this.cache != null) return true;

        else return presentInCache;
    }

    public int getFirstAccessed() {
        return firstAccessed;
    }

    public void setFirstAccessed(int firstAccessed) {
        this.firstAccessed = firstAccessed;
    }

    public int getLifeSpan(){
        return (this.lastAccessed - this.firstAccessed) ;
    }

    public double getDataSize() {
        return dataSize;
    }

    public void setDataSize(double dataSize) {
        this.dataSize = dataSize;
    }

    public Cache getCacheAddress() {
        return cache;
    }

    public void setCacheAddress(Cache cacheAddress) {
        this.cache = cacheAddress;
    }

    public String getQueryID() {
        return this.queryIndexID;
    }

    public void setQueryID(String queryIndexID) {
        this.queryIndexID = queryIndexID;
    }

    public int getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(int lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void incrementFrequency(){
        this.frequency++;
    }

    public void addFrequency(int freq){
        this.frequency += freq;
    }

    public ExpressionTree getQueryExpressionTree() {
        return queryExpressionTree;
    }

    public void setQueryExpressionTree(ExpressionTree queryExpressionTree) {
        this.queryExpressionTree = queryExpressionTree;
    }



    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public ArrayList<String> getQueryLocation() {
        return queryLocation;
    }

    public void setQueryLocation(ArrayList<String> queryLocation) {
        this.queryLocation = queryLocation;
    }

    public double getDataSize(ExpressionTree expressionTree){
       double size = 0;
       size = expressionTree.getNo_of_Nodes()*10;

       return size;
    }

    public String toString(){
        return "Query Index id: "+this.queryIndexID+", expressionTreeID: "+this.getQueryExpressionTree()
                .getExpressionTreeID()+", Query Expression: "+this.getQueryExpressionTree().getRoot().getValue()
                .getQueryExpression()+", " +
                "Frequency: " +this
                .getFrequency()+" " + "Last accessed: "+
                "Corresponding " +
                "Cache Address: "+this
                .getCacheAddress();
    }

    //===============FOLLOWING METHODS ARE FOR CACHE MAINTENANCE==================================

    /**
     * The following method is a part of queryIndex maintenance
     * @param threshold
     */
    public void addFrequentQueryToCache(int threshold){
        if ((this.isPresentInCache()==false) &&(this.frequency>= threshold) ) {
            this.presentInCache = true;
        }
    }

    /**
     * The following method creates a new indexed query by adding SQS of query1 to SQS of query2
     * query1.PQS = query2.PQS
     * but the condition of PQS1 is a subset of PQS2
     * @param query1
     * @param query2
     */
    public IndexedQuery  mergeIndexedQueries(IndexedQuery query1, IndexedQuery query2){
        IndexedQuery indexedQuery = new IndexedQuery();

       // Query_Subj_Predicate qsp = new Query_Subj_Predicate(query1.getQuerySegment().getSubjectQuerySegments(),query1.getQuerySegment().getPredicateQuerySegments());
        indexedQuery.setCacheAddress(query1.getCacheAddress());

        // frequency is set to maximum
        if (query1.getFrequency() > query2.getFrequency())
            indexedQuery.setFrequency(query1.getFrequency());
        else indexedQuery.setFrequency(query2.getFrequency());
        //TODO: finish this code here
        // it must move up or down in the querybase graph
        return indexedQuery;

    }

    /**
     * This is to delete some of the query segments
     * @param query1
     * @return
     */
    public IndexedQuery truncateQuery(IndexedQuery query1){
        IndexedQuery indexedQuery = new IndexedQuery();

        //TODO: delete query1's SQS from SQS of this query and adjust its position in the querybase graph

        return indexedQuery;
    }

    public StatusQuery matchQuery(Query_Subj_Predicate qsp){
        StatusQuery statusQuery = new StatusQuery();


        return statusQuery;
    }

    /**
     * to make search easier
     * @return the data size of the query
     */
    public int getQuerySize(){
        //TODO
        return 1;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexedQuery)) {
          //  System.out.println("o Not an instance of IndexedQuery class");
            return false;
        }

        IndexedQuery that = (IndexedQuery) o;

       // if (!queryExpressionTree.getRoot().getName().equals(that.queryExpressionTree.getRoot().getName())) {
        if (!queryExpressionTree.getRoot().getValue().getQueryExpression().equals(that.queryExpressionTree.getRoot().getValue().getQueryExpression())) {
          //  System.out.println("expression trees do not match  in the IndexedQuery class");
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        return queryExpressionTree.hashCode();
    }

    /**
     * To print indexed query
     */
    public void printQuery(){
        System.out.print(i +". Query ID: "+this.queryIndexID+" ");
        i++;
       // TODO:querySegment.printQuery();

        System.out.println("Last Accessed : "+getLastAccessed());
        System.out.println("Frequency : "+getFrequency());
        System.out.println("Cache Address : "+getCacheAddress());



        }

    public static void main(String[] args) {

        int totalQueries=10;

        Query_Subj_Predicate[] qsps = new Query_Subj_Predicate[totalQueries];
        IndexedQuery[] iqs = new IndexedQuery[totalQueries];

        qsps[0] = new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)");
        qsps[1] = new Query_Subj_Predicate("(<qry32:tab22:db2><axt412:tab12:db2><jkat22:tab32:db2>#<qry34:tab24:db4,gt345>)&(<jkat25:tab35:db5>#<qry33:tab23:db3,gt345>)");
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
            System.out.println("queries are = "+qsps[i].getQueryID()+" "+qsps[i].getQueryExpression());
            iqs[i] = new IndexedQuery(new ExpressionTree(qsps[i]));

        }

        for (int j = 0; j < 10; j++) {
            System.out.println(" testing "+iqs[i].getLastAccessed());
        }

/*
        int k=7;
        for (int j = 0; j < 10; j++) {

            if (iqs[k].equals(iqs[j])){
                System.out.println("Hashcode j="+ iqs[j].hashCode());
                System.out.println("Hashcode k="+ iqs[k].hashCode());
                System.out.println(j + "*** Equal to " + k);
            }
            else {
                System.out.println("Hashcode j="+ iqs[j].hashCode());
                System.out.println("Hashcode k="+ iqs[k].hashCode());
                System.out.println(j+"  NOT EQUAl to "+k);
            }
        }

       int j= k-5;

        if (iqs[j].equals(iqs[k])) {
            System.out.println("Hashcode j="+ iqs[j].hashCode());
            System.out.println("Hashcode k="+ iqs[k].hashCode());
            System.out.println(j + "*** Equal to " + k);
        }
        else {
            System.out.println("Hashcode j="+ iqs[j].hashCode());
            System.out.println("Hashcode k="+ iqs[k].hashCode());
            System.out.println(j+"  NOT EQUAl to "+k);
        }

        System.out.println("******************************");


        if (iqs[6].equals(iqs[1])) {
         //   System.out.println("Hashcode j="+ iqs[j].hashCode());
         //   System.out.println("Hashcode k="+ iqs[k].hashCode());
            System.out.println(1 + "*** Equal to " + 6);
        }
        else {
          //  System.out.println("Hashcode j="+ iqs[j].hashCode());
          //  System.out.println("Hashcode k="+ iqs[k].hashCode());
            System.out.println(1+"  NOT EQUAl to "+6);
        }
        */


    }
}

