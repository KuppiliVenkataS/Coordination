package project.QueryEnvironment;



//import jade.util.leap.ArrayList;
//import jade.util.leap.Iterator;

import javax.sql.rowset.Predicate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;



/**
 * Created by santhilata on 01/08/14.
 * Edited last on: 11/02/2015.
 * Query_Subj_Predicate contains all subject segments that define  columns and predicate segments that define the  rows
 * In all, Query_Subj_Predicate defines an optimal grain in the query index and data block that should be stored in the cache.
 * A normal query assumed to be given to the query planner (outside our work) and receives list of  Query_Subj_Predicate(s).
 */

public class Query_Subj_Predicate extends Query implements Comparable {

   //  private String queryID;

    private ArrayList<SubjectQuerySegment> subjectQuerySegments ;
    private ArrayList<PredicateQuerySegment> predicateQuerySegments ;
    private int datasize = 0;

    static int i=1;


    public Query_Subj_Predicate (){

        super();
        subjectQuerySegments = new ArrayList();
        for(SubjectQuerySegment s: subjectQuerySegments){
            s.setQueryID(this.getQueryID());
        }
        predicateQuerySegments = new ArrayList();
        for(PredicateQuerySegment p: predicateQuerySegments){
            p.getAttribute1().setQueryID(this.getQueryID());
            p.getAttribute2().setQueryID(this.getQueryID());
        }

        datasize = subjectQuerySegments.size()*10;

    }

    public Query_Subj_Predicate(String string){
        super(string); //sets query expression

        subjectQuerySegments = new ArrayList();
        predicateQuerySegments = new ArrayList();

        for(SubjectQuerySegment s: subjectQuerySegments){
            s.setQueryID(this.getQueryID());
        }

        for(PredicateQuerySegment p: predicateQuerySegments){
            if (p.getAttribute1() != null)
            p.getAttribute1().setQueryID(this.getQueryID());
            if (p.getAttribute2() != null)
            p.getAttribute2().setQueryID(this.getQueryID());
        }
        datasize = subjectQuerySegments.size()*10;

    }

    public Query_Subj_Predicate (ArrayList<SubjectQuerySegment> subjectQuerySegments, ArrayList<PredicateQuerySegment> predicateQuerySegments) {

        super();
      //  System.out.println("XXXXXXXXXXXXXXXXXXX from Query Subj Pred class "+getQueryExpression());
        this.subjectQuerySegments = subjectQuerySegments;
        for(SubjectQuerySegment s: this.subjectQuerySegments){
            s.setQueryID(this.getQueryID());
        }
        this.predicateQuerySegments = predicateQuerySegments;
        for(PredicateQuerySegment p: this.predicateQuerySegments){
            if (p.getAttribute1()!= null) p.getAttribute1().setQueryID(this.getQueryID());
            if (p.getAttribute2()!= null) p.getAttribute2().setQueryID(this.getQueryID());
        }
        datasize = subjectQuerySegments.size()*10;
        setQueryExpression(createExpressionFromQuery());  //sets query expression

    }

    public ArrayList getSubjectQuerySegments() {
        return subjectQuerySegments;
    }
    public void setSubjectQuerySegments(ArrayList subjectQuerySegments) {
        this.subjectQuerySegments = subjectQuerySegments;
    }

    public ArrayList getPredicateQuerySegments() {
        return predicateQuerySegments;
    }

    public void setPredicateQuerySegments(ArrayList predicateQuerySegments) {
        this.predicateQuerySegments = predicateQuerySegments;
    }

    public void createQuerySegmentFromExpression(String line){

        this.setQueryExpression(line);
        String[] sub_pred = line.split("#"); //sub_pred[0]=subjectQuerysegments abd sub_pred[1]= pred segments

        int sp = sub_pred.length;
        // subject query segments
        String[] segment = sub_pred[0].split(">");
        int noOfSegments = segment.length;

        int index = 0;

        while (index <noOfSegments){

           ArrayList subjectAttributes = new ArrayList();
            String table;
            String database;

            String[] tokens= segment[index].split(":");
            //read attributes names
            String str = tokens[0];
            if (str.charAt(0) == '(') str = str.substring(2,str.length()); // skip '(' and '<' characters
            else {
                str = str.substring(1, str.length()); // skip the first character '<'
            }
            String[] attr = str.split(";"); //';' is the delimiter for attributes from the same table

            for(int i=0; i<attr.length; i++){
                String atr = attr[i];
                subjectAttributes.add(i,atr);
            }

            //Reading Table name
            table = tokens[1];
            //Reading database name
            database = tokens[2];
            // add subjects to subjectQuerySegment list
            SubjectQuerySegment qs = new SubjectQuerySegment(subjectAttributes, table, database);

            subjectQuerySegments.add(index,qs);

            index++;

        }     // while (noOfSegments >0)

        if (sp > 1) { // now do the same with predicate query segments
            if (sub_pred[1].charAt(sub_pred[1].length()-1)== ')')
                sub_pred[1] = sub_pred[1].substring(0,sub_pred[1].length()-1);
                segment = sub_pred[1].split(">");

            noOfSegments = segment.length;

            index =0;

            while (index <noOfSegments){   // for more number of predicates
                ArrayList predicateAttributes1 = new ArrayList();
                ArrayList predicateAttributes2 = new ArrayList();
                String table1, table2;
                String database1,database2;

              //  String[] tokens= segment[noOfSegments-1].split(",");

                String[] tokens= segment[index].split(",");
                PredicateQuerySegment pqs = null;

                switch (tokens.length) {

                    case 0: {// no predicate at all
                        pqs = new PredicateQuerySegment();
                        break;
                    }
                    case 1: {
                        break;// this condition is never possible
                    }
                    case 2: {

                        String[] str = tokens[0].split(":");
                        str[0] = str[0].substring(1, str[0].length()); // skip the first character '<'
                        String attr = str[0];
                        predicateAttributes1.add(attr);

                        table1 = str[1];
                        database1 = str[2];

                        SubjectQuerySegment sqs1 = new SubjectQuerySegment(predicateAttributes1, table1, database1); // first attribute

                        String condition = tokens[1];

                        pqs = new PredicateQuerySegment(sqs1, condition);
                        break;
                    }

                    case 3: {
                        String[] str = tokens[0].split(":");
                        str[0] = str[0].substring(1, str[0].length()); // skip the first character '<'
                        String attr = str[0];
                        predicateAttributes1.add(attr);

                        table1 = str[1];
                        database1 = str[2];

                        SubjectQuerySegment sqs1 = new SubjectQuerySegment(predicateAttributes1, table1, database1); // first attribute
                        // for second segment

                        str = tokens[1].split(":");

                        attr = str[0];
                        predicateAttributes2.add(attr);

                        table2 = str[1];
                        database2 = str[2];

                        SubjectQuerySegment sqs2 = new SubjectQuerySegment(predicateAttributes2, table2, database2); // first attribute

                        String condition = tokens[2];
                        pqs = new PredicateQuerySegment(sqs1, sqs2, condition);
                        break;

                    }
                    default:
                        break;
                } //switch-case

                predicateQuerySegments.add(index,pqs);

                index++;

             //   noOfSegments--;
            }     // while (noOfSegments >0)

        }//  if (sp > 1) -- now do the same with predicate query segments

    }

    /**
     * creating a expression for ExpressionTree node
     * @return
     */
    public String createExpressionFromQuery(){

            String expression = "";

            //add all SubjectQuerySegments
//
            for (int j = 0; j < this.getSubjectQuerySegments().size(); j++) {

                SubjectQuerySegment sqs = (SubjectQuerySegment) getSubjectQuerySegments().get(j);
                expression = expression + "<";
                for (int k = 0; k < sqs.getAttributes().size(); k++) {
                    expression = expression + sqs.getAttributes().get(k);
                    if (k < sqs.getAttributes().size() - 1)
                       expression = expression+";";
                }
                expression = expression + ":";
                expression = expression + sqs.getTable() + ":";
                expression = expression + sqs.getDatabase() + ">";
            }
            expression = expression + "#";

            //Now add predicate query segments
            for (int j = 0; j < this.getPredicateQuerySegments().size(); j++) {
                PredicateQuerySegment pqs = (PredicateQuerySegment) getPredicateQuerySegments().get(j);
                expression = expression + "<";

                if (pqs.getAttribute1() != null) {
                    SubjectQuerySegment attribute1 = pqs.getAttribute1();
                    for (int k = 0; k < attribute1.getAttributes().size(); k++) {
                        expression = expression + attribute1.getAttributes().get(k);
                        if (k < attribute1.getAttributes().size() - 1)
                            expression = expression + ",";
                    }
                    expression = expression + ":";
                    expression = expression + attribute1.getTable() + ":";
                    expression = expression + attribute1.getDatabase();
                }

                if (pqs.getAttribute2() != null) {
                    expression = expression + ",";
                    SubjectQuerySegment attribute2 = pqs.getAttribute2();
                    for (int k = 0; k < attribute2.getAttributes().size(); k++) {
                        expression = expression + attribute2.getAttributes().get(k);
                        if (k < attribute2.getAttributes().size() - 1)
                            expression = expression + ",";
                    }
                    expression = expression + ":";
                    expression = expression + attribute2.getTable() + ":";
                    expression = expression + attribute2.getDatabase();
                }
                expression = expression + ",";
                expression = expression + pqs.getCondition();
                expression = expression + ">";

            }
            expression = "(" + expression + ")";

        return expression;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Query_Subj_Predicate)) {
            return false;
        }


        Query_Subj_Predicate that = (Query_Subj_Predicate) o;

        // following is the case for query that has only expression and neither SQS nor pQS present
        if ((this.getQueryExpression()!= "") &&(that.getQueryExpression() != ""))
        if (!(this.getQueryExpression().equals(that.getQueryExpression()))){
            return false;
        }


        if (! (checkEqualityOfArrayLists(this.getSubjectQuerySegments(),that.getSubjectQuerySegments()) &&
                checkEqualityOfArrayLists(this.getPredicateQuerySegments(),that.getPredicateQuerySegments()))) {

            return false;
        }


        return true;
    }

    @Override
    public int hashCode() {
        int result = 0; //super.hashCode();
        java.util.Iterator<SubjectQuerySegment> itr = subjectQuerySegments.iterator();
        while(itr.hasNext()){
            result = 31*result+itr.next().hashCode();
        }
        if (predicateQuerySegments!= null) {
            java.util.Iterator<PredicateQuerySegment> itr1 = predicateQuerySegments.iterator();
            while (itr1.hasNext()) {
                result = 31 * result + itr1.next().hashCode();
            }
        }

        return result;
    }

    /**
    * The following method is only for arrayList equality checking
    */
    boolean checkEqualityOfArrayLists(ArrayList list1, ArrayList list2){

        if (list1 == null && list2 == null){
            return true;
        }
        if ((list1 == null && list2 != null) || (list1 != null && list2 == null) || (list1.size() != list2.size())){
            return false;
        }

        List one = (List) new java.util.ArrayList<>();
        List two = (List) new java.util.ArrayList<>();

        for (int i = 0; i < list1.size(); i++) {
            if (list1.get(i) != null)
            one.add(list1.get(i));
            if (list2.get(i) != null)
            two.add(list2.get(i));

        }

        Collections.sort(one);
        Collections.sort(two);

        if (!one.equals((two)))
            return  false;
        else return  true;
    }

    /**
     * to compare subject query segments
     * @param sqs1
     * @param sqs2
     * @return
     */
    int compareSubjectQuerySegments(SubjectQuerySegment sqs1, SubjectQuerySegment sqs2){
      return sqs1.compareTo(sqs2);
    }

    int comparePredicateQuerySegments(PredicateQuerySegment pqs1, PredicateQuerySegment pqs2){
        return pqs1.compareTo(pqs2);
    }

     /**
     * This method is helpful in sorting queries of type Query_Subj_Predicate
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        Query_Subj_Predicate that = (Query_Subj_Predicate) o;

        if ((this.getSubjectQuerySegments() == null) && (that.getSubjectQuerySegments() == null))  return  0;
        // this is possible with some nodes in the Expression Tree without proper queries
        if ((this.getPredicateQuerySegments() == null) && (that.getPredicateQuerySegments() == null)) return 0;

        if ((this.getSubjectQuerySegments() == null) && (that.getSubjectQuerySegments() != null))  return  -1;
        if ((this.getSubjectQuerySegments() != null) && (that.getSubjectQuerySegments() == null))  return  1;

        if ((this.getPredicateQuerySegments() == null) && (that.getPredicateQuerySegments() != null)) return -1;
        if ((this.getPredicateQuerySegments() != null) && (that.getPredicateQuerySegments() == null)) return 1;

        //size checking
        if (this.getSubjectQuerySegments().size() < that.getSubjectQuerySegments().size()) return -1;
        else if (this.getSubjectQuerySegments().size() > that.getSubjectQuerySegments().size()) return 1;

        if (this.getPredicateQuerySegments().size() < that.getPredicateQuerySegments().size()) return -1;
        else if (this.getPredicateQuerySegments().size() > that.getPredicateQuerySegments().size())  return 1;


        List one = (List) new java.util.ArrayList<SubjectQuerySegment>();
        List two = (List) new java.util.ArrayList<SubjectQuerySegment>();

        int length = this.getSubjectQuerySegments().size();

        for (int j = 0; j < length; j++) {
            one.add(this.getSubjectQuerySegments().get(j));
            two.add(that.getSubjectQuerySegments().get(j));
        }
        Collections.sort(one);
        Collections.sort(two);

        for (int j = 0; j < length; j++) {
            if (compareSubjectQuerySegments((SubjectQuerySegment)one.get(j), (SubjectQuerySegment)two.get(j)) == -1) return -1;
            else if (compareSubjectQuerySegments((SubjectQuerySegment)one.get(j), (SubjectQuerySegment)two.get(j)) == 1)   return 1;

        }

        List onep = (List) new java.util.ArrayList<PredicateQuerySegment>();
        List twop = (List) new java.util.ArrayList<PredicateQuerySegment>();

        length = this.getPredicateQuerySegments().size();

        for (int j = 0; j < length; j++) {
            onep.add(this.getPredicateQuerySegments().get(j));
            twop.add(that.getPredicateQuerySegments().get(j));
        }

        Collections.sort(onep);
        Collections.sort(twop);

        for (int j = 0; j < length; j++) {
            if (comparePredicateQuerySegments((PredicateQuerySegment)onep.get(j), (PredicateQuerySegment)twop.get(j)) == -1) return -1;
            else if (comparePredicateQuerySegments((PredicateQuerySegment)onep.get(j), (PredicateQuerySegment)twop.get(j)) == 1) {

                return 1;
            }
        }


        return 0;
    }

    /**
     * Print query
     */
    public void printQuery(){
        System.out.println(i+". Query ID: "+this.queryID+" ");
        i++;

        System.out.println("Subject attributes: ");
        java.util.Iterator itr = subjectQuerySegments.iterator();
        while (itr.hasNext()){
            SubjectQuerySegment queryS = (SubjectQuerySegment)itr.next();
            System.out.println(queryS.toString());
        }

        System.out.println("Predicate attributes: ");
        java.util.Iterator itr1 = predicateQuerySegments.iterator();
        System.out.println("Predicate segments "+predicateQuerySegments.size());
        while (itr1.hasNext()){
            PredicateQuerySegment queryS = (PredicateQuerySegment)itr1.next();
            System.out.println(queryS.toString());
        }
    }


/**
 * TODO:
 * 1. Write a method to add (Union) one Query_Subj_Predicate to another
 * 2. Delete a part Query_Subj_Predicate from the first
 * 3. Add rows
 * 4. Add columns
 * 5. Delete rows
 * 6. Delete columns
 * 7. get intersection of a Query_Subj_Predicate1 with Query_Subj_Predicate2 which results a Query_Subj_Predicate_common
 */
public static void main(String[] args) {
    String expression8 = "<sand83:tab23:db5>#<asd13:tab3:db5,lt25>";


    Query_Subj_Predicate qsp1 = new Query_Subj_Predicate();
  //  qsp1.createQuerySegmentFromExpression(expression8);

    String expression81 = "<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>";
    String expression82 = "(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)";
    String testString = "(<jkat22:tab32:db2><slr23;xlr63:tab43:db3>#<qry31:tab21:db1,gt345><usr13:tab43:db3,at2:tab2:db1,eq67><at32:tab32:db2,eqHarry>)";
    Query_Subj_Predicate qsp2 = new Query_Subj_Predicate();
    qsp2.createQuerySegmentFromExpression(expression82);
   qsp2.printQuery();

    Query_Subj_Predicate qsp3 = new Query_Subj_Predicate("(<jkat22:tab32:db2><slr23;xlr63:tab43:db3>#<qry31:tab21:db1,gt345><usr13:tab43:db3,at2:tab2:db1,eq67><at32:tab32:db2,eqHarry>)");
   // System.out.println(qsp3.getQueryExpression());
  //  qsp3.createQuerySegmentFromExpression(qsp3.getQueryExpression());
  //  qsp3.printQuery();

  //  System.out.println(qsp2.createExpressionFromQuery());




  //  System.out.println(qsp3.createExpressionFromQuery());

//    System.out.println(qsp1.compareTo(qsp2));

}


}