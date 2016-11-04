package project.QueryEnvironment;

import jade.content.Concept;
import jade.content.ContentElement;

import jade.content.Predicate;
//import jade.util.leap.ArrayList;
import java.util.ArrayList;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


/**
 * Created by santhilata on 11/02/15.
 */
public class PredicateQuerySegment  implements Concept, Serializable,Comparable {
    private SubjectQuerySegment attribute1;
    private SubjectQuerySegment attribute2;
    private String condition;

    public PredicateQuerySegment() {

    }

    public PredicateQuerySegment(SubjectQuerySegment attribute1, String condition) {
        this.attribute1 = attribute1;
        this.condition = condition;
    }

    public PredicateQuerySegment(SubjectQuerySegment attribute1, SubjectQuerySegment getAttribute2, String condition) {
        this.attribute1 = attribute1;
        this.attribute2 = getAttribute2;
        this.condition = condition;
    }

    public SubjectQuerySegment getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(SubjectQuerySegment attribute1) {
        this.attribute1 = attribute1;
    }

    public SubjectQuerySegment getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(SubjectQuerySegment attribute2) {
        this.attribute2 = attribute2;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getAttributes(){
        int i=0;
        if (attribute1== null && attribute2 == null)
            i = 0;
        else if (attribute1 != null && attribute2 == null)
            i=1;
        else if (attribute1 != null && attribute2 != null)
            i = 2;

        return i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PredicateQuerySegment)) return false;

        PredicateQuerySegment that = (PredicateQuerySegment) o;

        if (!attribute1.equals(that.attribute1)) return false;
        if (attribute2 != null ? !attribute2.equals(that.attribute2) : that.attribute2 != null) return false;
        if (!condition.equals(that.condition)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attribute1.hashCode();
        result = 31 * result + (attribute2 != null ? attribute2.hashCode() : 0);
        result = 31 * result + condition.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PredicateQuerySegment{" +
                "attribute1=" + attribute1 +
                ", attribute2=" + attribute2 +
                ", condition='" + condition + '\'' +
                '}';
    }

    /**
     * before using compareTo, one must check how many attributes the predicate has.
     *  whether there is a predicate present at all
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        PredicateQuerySegment that = (PredicateQuerySegment) o;
        int value = 0;

        if(this.equals(that)) return 0;

        // checking with condition
        if ((this.condition == null) && (that.condition != null)) return -1;
        else if ((this.condition != null) && (that.condition == null)) {
            System.out.println("testing in pred que seg6");
            return 1;
        }

        if ((this.getAttribute1() == null) && (this.getAttribute2())== null) {
            if (this.getCondition().compareTo(that.getCondition()) < 0) return -1;
            else if (this.getCondition().compareTo(that.getCondition()) > 0) {
               // System.out.println("testing in pred que seg5");
                return 1;
            }

        }
        else if ((this.getAttribute1() == null) && (that.getAttribute1())!= null) return -1;
        else if ((this.getAttribute1() != null) && (that.getAttribute1())== null) return 1;

        if ((this.getAttribute2() == null) && (that.getAttribute2())!= null) return -1;
        else if ((this.getAttribute2() != null) && (that.getAttribute2())== null) return 1;

        // comparing with the number of attributes
        if (this.getAttributes() < that.getAttributes()) return -1;
        else if (this.getAttributes() > that.getAttributes()) {
           // System.out.println("testing in pred que seg4");
            return 1;
        }

        if ((this.getAttribute2() != null) &&(that.getAttribute2() != null)) {
            SubjectQuerySegment this_attr1 = this.getAttribute1();
            SubjectQuerySegment this_attr2 = this.getAttribute2();
            SubjectQuerySegment temp;
            //      System.out.println(this_attr2.getDatabase());

            //sorting attributes of this PredicateQuerySegment
            if (this_attr1.compareTo(this_attr2) >0) {
                temp = this_attr1;
                this_attr1 = this_attr2;
                this_attr2 = temp;

            }

            //sorting attributes of that PredicateQuerySegment (of SQS type)
            SubjectQuerySegment that_attr1 = that.getAttribute1();
            SubjectQuerySegment that_attr2 = that.getAttribute2();

            if (that_attr1.compareTo(that_attr2) >0) {
                temp = that_attr1;
                that_attr1 = that_attr2;
                that_attr2 = temp;
            }

            // now, comparison

            if ((this_attr1.compareTo(that_attr1)) >0) {
                // System.out.println("testing in pred que seg1");
                return 1;
            } else if ((this_attr1.compareTo(that_attr1)) < 0) return -1;

            if ((this_attr2.compareTo(that_attr2)) >0) {
                // System.out.println("testing in pred que seg2");
                return 1;
            } else if ((this_attr2.compareTo(that_attr2)) < 0) return -1;

            //checks the query condition last
            if (this.getCondition().compareTo(that.getCondition()) < 0) return -1;
            else if (this.getCondition().compareTo(that.getCondition()) >0) {
                //  System.out.println("testing in pred que seg3");
                return 1;
            }
        }

        if ((this.getAttribute2() == null)&&(that.getAttribute2() == null)){
            if ((this.getAttribute1().compareTo(that.getAttribute1())) >0) {
                // System.out.println("testing in pred que seg1");
                return 1;
            } else if ((this.getAttribute1().compareTo(that.getAttribute1())) < 0) return -1;

            if (this.getCondition().compareTo(that.getCondition()) < 0) return -1;
            else if (this.getCondition().compareTo(that.getCondition()) >0) {
                //  System.out.println("testing in pred que seg3");
                return 1;
            }

        }

       // System.out.println(" value is"+ value);
        return value;
    }

    public static void main(String[] args) {

        ArrayList list1 = new ArrayList();
        ArrayList list2 = new ArrayList();

        for (int i = 0; i < 3; i++) {
            // list1.add(i+" ");
            list2.add(i+" ");
        }
        list1.add("asdf1");
        list1.add("adef");
        //  list1.add("asdf2");

        SubjectQuerySegment sqs1 = new SubjectQuerySegment(list1,"t1","d1");
        SubjectQuerySegment sqs2 = new SubjectQuerySegment(list2,"t1","d1");

      //  System.out.println(sqs1.getDatabase());


        PredicateQuerySegment pqs1 = new PredicateQuerySegment(sqs1,sqs2,"cond1");
        PredicateQuerySegment pqs2 = new PredicateQuerySegment(sqs2,sqs1,"cond1");

        System.out.println("asdf".compareTo("1"));

        System.out.println(pqs1.compareTo(pqs2));
    }
}
