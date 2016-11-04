package project.QueryEnvironment;


import java.util.ArrayList;
import jade.content.Concept;
//import jade.util.leap.ArrayList;

import java.io.Serializable;
import java.text.CollationElementIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by santhilata on 11/02/15
 */

public class SubjectQuerySegment  implements Concept, Serializable,Comparable {
    private String queryID;
    private ArrayList attributes ;
    private String table ;
    private String database ;

    public SubjectQuerySegment(){
        //null constructor
    }
    public SubjectQuerySegment(ArrayList at, String t, String db){
        this.attributes =  new ArrayList();
        for (int i=0; i< at.size();i++) {
            String qat = (String)at.get(i);
            this.attributes.add(i,qat);

        }

        //  this.attributes = at;
        this.table = t;
        this.database = db;
    }

    public SubjectQuerySegment(String queryID, ArrayList at, String table, String database) {
        this.queryID = queryID;
        this.attributes =  new ArrayList();
        for (int i=0; i< at.size();i++) {
            String qat = (String)at.get(i);
            this.attributes.add(i,qat);
        }
        this.table = table;
        this.database = database;
    }

    public String getQueryID() {
        return queryID;
    }

    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }


    public ArrayList getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList attributes) {
        this.attributes = attributes;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectQuerySegment)) {
         //   System.out.println("From SQS: not instance of SQS");
            return false;
        }

        SubjectQuerySegment that = (SubjectQuerySegment) o;

        if (! checkEqualityOfArrayLists(this.getAttributes(), that.getAttributes())) {
         //   System.out.println("From SQS:  attribute lists are not same");
            return  false;
        }
        if (!database.equals(that.database)){
        //    System.out.println("From SQS: database name is not equal");
            return false;
        }
        if (!table.equals(that.table)) {
         //   System.out.println("From SQS: table name is not equal");
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;// = attributes.hashCode();
        Iterator<String> itr = attributes.iterator();
        while(itr.hasNext()){
            result = 31*result+itr.next().hashCode();
        }
        result = 31 * result + table.hashCode();
        result = 31 * result + database.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SubjectQuerySegment{" +
                "attributes=" + attributes +
                ", table='" + table + '\'' +
                ", database='" + database + '\'' +
                '}';
    }

    /**
     * internal method -- support code
     * @param list1
     * @param list2
     * @return
     */
    boolean checkEqualityOfArrayLists(ArrayList list1, ArrayList list2){

        if (list1 == null && list2 == null){
            return true;
        }
        if ((list1 == null && list2 != null) || (list1 != null && list2 == null) ||(list1.size() != list2.size())){
            return false;
        }

        List one = (List) new java.util.ArrayList<String>();
        List two = (List) new java.util.ArrayList<String>();

        for (int i = 0; i < list1.size(); i++) {
            one.add(list1.get(i));
            two.add(list2.get(i));
        }
        Collections.sort(one);
        Collections.sort(two);

       /* Iterator<String> itr = one.iterator();
        while(itr.hasNext()){
          //  System.out.println(itr.next());
        }
        itr = two.iterator();
        while(itr.hasNext()){
           // System.out.println(" two "+itr.next());
        }
        */

        if (!one.equals((two)))
            return  false;
        else return  true;
    }

    /**
     * for greater than and less than for equi length arraylists
     * @param o
     * @return
     */

    int arrayListGTLTEQ(Object o){
        int value = 0;

        SubjectQuerySegment that = (SubjectQuerySegment) o;

        List one = (List) new java.util.ArrayList<String>();
        List two = (List) new java.util.ArrayList<String>();

        for (int i = 0; i < this.getAttributes().size(); i++) {
            one.add(this.getAttributes().get(i));
            two.add(that.getAttributes().get(i));
        }

        Collections.sort(one);
        Collections.sort(two);

        Iterator<String> itr1 = one.iterator();
        Iterator<String> itr2 = two.iterator();

        while (itr1.hasNext()){
            String str1 = itr1.next();
            String str2 = itr2.next();
          //  System.out.println("from arrayListGTLTEQ of SubjectQuerySegment class "+ str1+" "+str2);

           if (str1.compareToIgnoreCase(str2)< 0) return -1;
           else if (str1.compareToIgnoreCase(str2)> 0)return 1;
          // else  continue;
        }

        return value;
    }

    /**
     * To compare on database first and then table and then the length of attributes and then string comparison of each of the attributes
     * @param o
     * @return
     * returns '0' when both are equal, -1 if this is smaller than that and 1 if this is larger than that
     */
    @Override
    public int compareTo(Object o) {

        SubjectQuerySegment that = (SubjectQuerySegment) o;
      //  System.out.println(this.getDatabase()+" "+that.getDatabase());
        if (this.getDatabase().compareToIgnoreCase(that.getDatabase()) == 0){
            if (this.getTable().compareToIgnoreCase(that.getTable()) == 0){
                if (this.getAttributes().size() == that.getAttributes().size()) {

                    if (this.arrayListGTLTEQ(that) == 0) {
                      //  System.out.println("In subjectQuerySegmnet class : compareTo method: this.arrayListGTLTEQ(that) == 0");
                        return 0;
                    }
                    else if (this.arrayListGTLTEQ(that) == 1) return 1;
                    else return -1;
                }
                else if  (this.getAttributes().size() > that.getAttributes().size())   return 1;
                else return -1;
            }
            else if (this.getTable().compareToIgnoreCase(that.getTable())>0) return 1;
            else  return -1;
        }
        else if (this.getDatabase().compareToIgnoreCase(that.getDatabase()) >0) return 1;
        else return -1;
    }


    /**
     * Testing
     * @param args
     */
    public static void main(String[] args) {
        ArrayList list1 = new ArrayList();
        ArrayList list2 = new ArrayList();

        for (int i = 0; i < 3; i++) {
           // list1.add(i+" ");
            list2.add(i+" ");
        }
        list1.add("asdf1");
        list1.add("adef");
        list1.add("asdf2");

        SubjectQuerySegment sqs1 = new SubjectQuerySegment(list2,"t1","d1");
        SubjectQuerySegment sqs2 = new SubjectQuerySegment(list1,"t1","d1");
     //   System.out.println("hash code sqs1"+sqs1.hashCode());
      //  System.out.println("hash code sqs2"+sqs2.hashCode());

      //  System.out.println("asdf".compareToIgnoreCase("asdf"));
      //  System.out.println(sqs1.equals(sqs2));
        System.out.println(sqs1.compareTo(sqs2));

    }
}