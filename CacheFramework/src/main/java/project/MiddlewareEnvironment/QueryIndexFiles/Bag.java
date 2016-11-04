package project.MiddlewareEnvironment.QueryIndexFiles;


import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.Query_Subj_Predicate;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by santhilata on 19/03/15.
 *
 */
public class Bag {
    static final int no_of_pennant_roots = 14;// At the moment the number of cells in a bit vector is set to 14.
    // This number can be changed according to the use

    public static final String FULLY_FOUND = "FULLY FOUND";
    public static final String PARTIALLY_FOUND = "PARTIALLY FOUND";
    public static final String NOT_FOUND = "NOT FOUND";
    static int noOfQueriesDeleted=0;

    private int[] bitVector = new int[no_of_pennant_roots];// contains 1 or 0 to give the information
    private Pennant[] pennant_root_list = new Pennant[no_of_pennant_roots]; // root pennants in the bag's bitVector
    private UpdatedData data = new UpdatedData();

    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private static Lock readLock = rwl.readLock();
    private Lock writeLock = rwl.writeLock();
    /**
     * Default constructor
     */
    public Bag()  {
        for (int i=0; i < no_of_pennant_roots; i++) {
            bitVector[i] = 0;
            pennant_root_list[i] = null;
        }
    }//constructor

    /**
     * This constructor creates a bag of given size - i
     * @param i
     * @return
     */
    public int getBitVector(int i) {
        return bitVector[i];
    }

    public void setBitVectorValue(int position, int value){
        bitVector[position] = value;
    }

    /**
     * This method sets a pennant 'pennant' at kth position of the pennant list
     * @param k
     * @param pennant
     */
    public void setPennant_root_list(int k, Pennant pennant) {
        this.pennant_root_list[k] = pennant;
    }

    public Pennant[] getPennant_root_list() {
        return pennant_root_list;
    }

    /**
     * insert pennants into bag
     */
    public  void bag_Insert( Pennant p){
        int k =0;

        writeLock.lock();
        //  System.out.println("inside Bag class inserting new pennant"+p.getRoot().queryIndexID);

        while (bitVector[k] != 0) {

            p = pennant_root_list[k].pennant_Union(p);
            bitVector[k] = 0;
            pennant_root_list[k++] = null;
        }

        pennant_root_list[k] = p;
        bitVector[k] = 1;
        writeLock.unlock();

    }//bag_Insert

    /**
     * returns true if the bag is empty
     */
    public boolean isBagEmpty(){
        boolean empty = true;
        int i=0;
        while (i<no_of_pennant_roots) {
            if ( bitVector[i++] == 0)
                empty = true;
            else {
                empty= false;
                break;
            }
        }

        return empty;
    }

    public boolean isFull(){
        boolean full = false;
        if (bagSize() >= (Math.pow(2,(no_of_pennant_roots-1))+1)) full= true;

        return full;
    }

    //==========FOLLOWING CODE IS TO MAINTAIN BAGS AS PART OF CACHE MAINTENANCE==================
    /**
     * returns the number of queries in a bag --for counting purpose
     */
    public int bagSize() {
        int size = 0;
        for(int i=0; i<no_of_pennant_roots; i++)

            size += this.getBitVector(i) * Math.pow(2, i);

        return size;
    }

    /**
     * Deleting a particular query
     */
    public void deleteQueryFromBag(IndexedQuery cl) {
        Pennant p;
        Pennant x = null;
        Pennant y;


        Bag bag = new Bag(); // this is a temporary bag is to hold the deleted pennant.  Later  this bag will perform union with the original bag
        int k;
        // to search for the pennant root in which the query to be deleted currently is in
        noOfQueriesDeleted++;

        //original queries

        for( k=0; k<no_of_pennant_roots; k++)
        {
            if (((p = this.pennant_root_list[k]) != null) && isQueryInPennant(p, cl))
            {
                x = this.pennant_root_list[k]; // take out the pennant from the bag

                //   System.out.println("Cluster to be deleted is found in Pennant "+k);
                this.pennant_root_list[k] = null;
                this.bitVector[k] = 0;// make the bit 0
                break;
            }

        }//for loop

        // This is to set left and right subtrees of the pennant_to_be_deleted
        while ((x.getLeftSubTree() !=null ) && (k !=0))  {
            y = x.pennant_Split();
            k--;

            if (!(isQueryInPennant(y, cl)))  {
                bag.setPennant_root_list(k, y);// in the new bag set kth cell with 'y'
            }
            else    {
                bag.setPennant_root_list(k, x);
                x = y;
            }
            bag.bitVector[k]=1;

        }//while

        x = null;
        this.bag_Union(bag);// add this bag to the original bag



    }// end of the method deleteQueryFromBag

    /**
     * Returns a boolean value. This is a private method to search for a cluster within the pennant.
     */
    public Boolean isQueryInPennant( Pennant p, IndexedQuery cl) {
        boolean present = false;


    /*    present = ((p != null) && ( (p.getRoot().equals(cl))
                ||(p.getRightSubTree() != null && p.getRightSubTree().isQueryInPennant(cl))) || (p
                .getLeftSubTree()!= null && p.getLeftSubTree().isQueryInPennant(cl))
        ); */

        return ((p != null) && ( (p.getRoot().equals(cl))
                ||(isQueryInPennant(p.getRightSubTree(),cl)) || (isQueryInPennant(p.getLeftSubTree(),cl))   ));



/*
        if(this != null && present == false){
            System.out.println(cl.queryIndexID +" in pennant isQueryInPennant"+ " is comparing with "+this.getRoot().getQueryID());
            if(this.getRoot().equals(cl)){
                present = true;

                this.getRoot().addFrequency(1);
                return true;
            }
           if(this.getLeftSubTree() != null && this.getLeftSubTree().isQueryInPennant(cl)){
                present = true;
                return true;
            }
            else if (this.getRightSubTree()!= null && this.getRightSubTree().isQueryInPennant(cl)){
                present = true;
                return true;
            }
        }

       // System.out.println(cl.queryIndexID +" in pennant isQueryInPennant"+ " is comparing with "+this.getRoot().getQueryID()+" "+present);
*/
        //  return present;
    }

    /**
     * Adding two bags. It needs a fullAdder function to store the carry bit and a special inner class SubBag structure.
     */
    public void bag_Union(Bag bag) {
        SubBag sb ;
        Pennant y = null; //carry bit
        for (int k=0; k<no_of_pennant_roots; k++)  {
            sb =  fullAdder(this.pennant_root_list[k],bag.pennant_root_list[k],y);// union of two pennants
            this.pennant_root_list[k] = sb.getS();
            y = sb.getC();

            // set bitvector to 1 to have the count of pennants

            if (this.pennant_root_list[k] != null) this.bitVector[k] = 1;
            else this.bitVector[k] = 0;
        }
    }

    private SubBag fullAdder(Pennant x, Pennant y, Pennant z)  {
        Pennant s;
        Pennant c;
        SubBag sb = new SubBag();

        if (z ==null)   {
            if (x == null)  {
                if (y == null) {// 000
                    s = null;
                    c = null;
                }
                else   { //010
                    s = y;
                    c = null;
                }
            }
            else  {// x != null
                if (y == null) { //100
                    s = x;
                    c = null;
                }
                else  { // 110
                    s = null;
                    c = x.pennant_Union(y);
                }
            }
        }
        else { //z!=null
            if (x == null) {
                if (y == null) { //001
                    s = z;
                    c = null;
                }
                else  { // 011
                    s = null;
                    c = y.pennant_Union(z);
                }

            }

            else { // x != null
                if (y == null) { // 101
                    s = null;
                    c = x.pennant_Union(z);
                }

                else { //111
                    s = x;
                    c = y.pennant_Union(z);
                }
            }

        }

        sb.setC(c);
        sb.setS(s);

        return sb;
    } // end of method fullAdder

    /**
     * This method is to remove the old clusters from the bag level
     * This is the part of updating queryBase
     */
    public UpdatedData deleteOldQueries(int time_threshold, int ticks) {
        Pennant p;

        UpdatedData tempData ;
        noOfQueriesDeleted = 0;
        for(int k=0; k< no_of_pennant_roots; k++) {
            if ((p = this.pennant_root_list[k]) != null) {
                //  System.out.println("k= "+k);
                p = delete(p, time_threshold, ticks);
            }
        }
        tempData = new UpdatedData(data);
        data.refreshData();
        return tempData;
    }//end of the method deleteOldQueries

    /**
     * This method is a private recursion for deleting time elapsed queries
     */
    private Pennant delete(Pennant p, int time_threshold,int ticks) {
        if(p != null) {

            delete(p.getLeftSubTree(),time_threshold, ticks);

            if ((ticks-p.getRoot().getLastAccessed()) > time_threshold) {
              //  System.out.println("Line 310 in bag.java ; Last accessed  time "+ p.getRoot().getLastAccessed()+"     ticks-p.getRoot().getLastAccessed() = "+(ticks-p.getRoot().getLastAccessed())+"time threshold ="+time_threshold);
                data.addDeletedQueries(p.getRoot());
                this.deleteQueryFromBag(p.getRoot());
            }

            delete(p.getRightSubTree(),time_threshold,ticks);

        }
        return p;
    }// end of the private method delete


    /**
     * This method is to remove the less frequent queries from the bag level
     * This is the part of updating queryIndex
     */
    public UpdatedData deleteLowFrequentQueries(int frequency_threshold, int ticks) {
        Pennant p;

        UpdatedData tempData ;
        noOfQueriesDeleted = 0;
        for(int k=0; k<no_of_pennant_roots; k++) {
            if ((p = this.pennant_root_list[k]) != null)
                //  System.out.println("k= "+k);
                p = delete_LowFrequent(p, frequency_threshold,ticks);
        }
        tempData = new UpdatedData(data);
        data.refreshData();
        return tempData;
    }//end of the method deleteOldQueries

    /**
     * This method is a private recursion for deleting time elapsed queries
     */
    private Pennant delete_LowFrequent(Pennant p, int frequency_threshold, int ticks) {
        if(p != null) {

            delete_LowFrequent(p.getLeftSubTree(),frequency_threshold,ticks);

            if (p.getRoot().getFrequency() > frequency_threshold) {

                data.addDeletedQueries(p.getRoot());
                this.deleteQueryFromBag(p.getRoot());
            }

            delete_LowFrequent(p.getRightSubTree(),frequency_threshold,ticks);

        }
        return p;
    }// end of the private method delete



    /**
     *  Printing the bag
     */
    public void printBag() {
        Pennant p;

        for(int k=0; k<no_of_pennant_roots; k++) {
            if ((p = this.pennant_root_list[k]) != null)

                p = printData(p);
        }
    }

    private Pennant printData(Pennant p) {

        if(p != null) {
            printData(p.getLeftSubTree());
            System.out.println(p.getRoot().toString() + "; ");
            printData(p.getRightSubTree());
        }
        return p;
    }


    static  boolean found = false;
    static boolean partiallyFound = false;

    /**
     * This method is incomplete
     * This searches for queries whether fully found / partially found or not found
     * @param ind_qu
     * @return
     */
    public Cache_Reply searchBag(IndexedQuery ind_qu){
        Cache_Reply cr =  null;
        Pennant p;

        readLock.lock();
        for(int k=0; k<no_of_pennant_roots; k++) {
            if ((p = this.pennant_root_list[k]) != null) {

                cr = search(p, ind_qu);
//                System.out.println(" ** From bag class ** "+ind_qu.getQueryExpressionTree()
//                        .getRoot().getValue().getQueryExpression());
                if (found) {

                    break;
                }
            }
        }
        readLock.unlock();



        if (cr==null ){

            cr = new Cache_Reply();
            cr.setQueryID(ind_qu.getQueryID());
            cr.setReplyStatus(NOT_FOUND);
            cr.setDataSizeFound(0);
            cr.setPartialExpressionTree(ind_qu.getQueryExpressionTree());

        }
        else if (cr.getReplyStatus().equals(FULLY_FOUND+"Partial")){  //for partial cases only
//            System.out.println("from Bag class Fully found Partial"+cr.getPartialExpressionTree().getRoot().getValue()
//                    .getQueryExpression());
            cr.setQueryID(ind_qu.getQueryID());
          //  cr.setReplyStatus(PARTIALLY_FOUND);     // this is part of some query
            cr.setReplyStatus(FULLY_FOUND);

        }

        found = false;

        return cr;
    }

    private Cache_Reply search(Pennant p, IndexedQuery ind_qu){

        Cache_Reply cacheReply=null;


        if (p != null && found==false ) {
            cacheReply = search(p.getLeftSubTree(), ind_qu);
        }
        if (p != null && found==false ) {

            //searching in the same sized tree
            if ((p.getRoot().getQueryExpressionTree().getNo_of_Nodes() == ind_qu.getQueryExpressionTree().getNo_of_Nodes()) && (p.getRoot().equals(ind_qu))) {

                cacheReply = new Cache_Reply();
                cacheReply.setDataSizeFound(ind_qu.getDataSize());

                cacheReply.setPartialExpressionTree(ind_qu.getQueryExpressionTree());
                cacheReply.setQueryID(ind_qu.getQueryExpressionTree().getExpressionTreeID());
              //  System.out.println("In bag Fully found "+ind_qu.getQueryExpressionTree().getRoot().getValue()
               //         .getQueryExpression());
                cacheReply.setReplyStatus(FULLY_FOUND);

                // Following is to update the time last accessed and frequency
                p.getRoot().setLastAccessed(ind_qu.getQueryExpressionTree().getTime_queried());
                p.getRoot().addFrequency(1);
                found = true;

                return cacheReply;
            }
            //found = false;
            // if query is a part of higher order bags

            if (p.getRoot().getQueryExpressionTree().getNo_of_Nodes() > ind_qu.getQueryExpressionTree().getNo_of_Nodes()) {


                ExpressionTree p_exprTree = p.getRoot().getQueryExpressionTree();
                ExpressionTree to_be_searched_query = ind_qu.getQueryExpressionTree();


               if (p_exprTree.getRoot().getValue().getQueryExpression().contains(to_be_searched_query.getRoot().getValue().getQueryExpression())) {


                    cacheReply = new Cache_Reply();
                   // cacheReply.setDataSizeFound(ind_qu.getDataSize());
                    cacheReply.setPartialExpressionTree(ind_qu.getQueryExpressionTree());
                    cacheReply.setQueryID(ind_qu.getQueryExpressionTree().getExpressionTreeID());
                    cacheReply.setReplyStatus(FULLY_FOUND + "Partial");
//                  cacheReply.setReplyStatus(FULLY_FOUND);
                   p.getRoot().setLastAccessed(ind_qu.getQueryExpressionTree().getTime_queried());
                   p.getRoot().addFrequency(1);

                    found = true;

                    return cacheReply;
                }
            }// to_be_searched_query is smaller

        }

        if (p != null && found==false ) {

            cacheReply = search(p.getRightSubTree(), ind_qu);

        }
        return cacheReply;
    }



    /**
     * ============================================================================================================================
     * A private class within the bag. This structure is needed to return two values.
     */
    class SubBag  {
        Pennant s;
        Pennant c;

        public SubBag() {
            s = new Pennant();
            c = new Pennant();
        }

        private Pennant getS() {
            return s;
        }

        private Pennant getC() {
            return c;
        }

        private void setS(Pennant x) {
            this.s = x;
        }

        private void setC(Pennant x) {
            this.c = x;
        }

    }// sub class SubBag

    public static void main(String[] args) {
        int totalQueries=10;
        Bag bag = new Bag();
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
            Pennant p = new Pennant(iqs[i]);
            bag.bag_Insert(p);

        }
        bag.printBag();



        //

        for (int i = 5; i < 10 ; i++) {

            Cache_Reply cr = bag.searchBag(iqs[i]);
            // Cache_Reply cr = bag.searchBag(new ExpressionTree(qsps[i]));
            System.out.println("From main "+cr.getQueryID()+cr.getReplyStatus());

        }

        System.out.println("*********************************");
        bag.printBag();
        System.out.println("*********************************");

    }
}
