package project.QueryEnvironment;

import jade.content.Concept;

import java.io.Serializable;

/**
 * Created by k1224068 on 02/09/14.
 */
public class Query_Attribute_condition implements Concept,Serializable{
    private String attribute1;
    private String attribute2;
    private String condition;

    public Query_Attribute_condition()
    {//null constructor
        this.attribute1 = null;
        this.attribute2 = null;
        this.condition = null;
     }

    public Query_Attribute_condition(String attribute1, String condition) {
        this.attribute1 = attribute1;
        this.condition = condition;
    }

    public Query_Attribute_condition(String attribute1, String attribute2, String condition) {
        this.attribute1 = attribute1;
        this.attribute2 = attribute2;
        this.condition = condition;
    }


    public String getAttribute1() {
        return attribute1;
    }

    public void setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
    }

    public String getAttribute2() {
        return attribute2;
    }

    public void setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Query_Attribute_condition)) return false;

        Query_Attribute_condition that = (Query_Attribute_condition) o;

        if (!attribute1.equals(that.attribute1)) return false;
        if (!attribute2.equals(that.attribute2)) return false;
        if (!condition.equals(that.condition)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attribute1.hashCode();
        result = 31 * result + attribute2.hashCode();
        result = 31 * result + condition.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Query_Attribute_condition{" +
                "attribute1 ='" + attribute1 + '\'' +
                "attribute2 ='" + attribute2 + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }
}