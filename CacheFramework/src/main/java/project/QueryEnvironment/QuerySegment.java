package project.QueryEnvironment;

import jade.content.Concept;
import jade.content.ContentElement;

import java.io.Serializable;
//import java.util.ArrayList;
import jade.content.Predicate;
import jade.util.leap.ArrayList;
/**
 * Created by k1224068 on 02/09/14.
 */
public class QuerySegment implements Concept,  Serializable {

    private String queryID;
    private ArrayList attributes ;
    private String table ;
    private String database ;

    public QuerySegment(){

    }

    public QuerySegment(ArrayList at, String t, String db){
       this.attributes =  new ArrayList();
        for (int i=0; i< at.size();i++) {
            Query_Attribute_condition qat = (Query_Attribute_condition)at.get(i);
            this.attributes.add(qat);
            }

      //  this.attributes = at;
        this.table = t;
        this.database = db;
    }

  // constructor with QueryID
    public QuerySegment(String queryID, ArrayList at, String t, String db){
        this.queryID = queryID;
        this.attributes =  new ArrayList();
        for (int i=0; i< at.size();i++) {
            Query_Attribute_condition qat = (Query_Attribute_condition)at.get(i);
            this.attributes.add(qat);
        }
        //  this.attributes = at;
        this.table = t;
        this.database = db;
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
        if (!(o instanceof QuerySegment)) return false;

        QuerySegment that = (QuerySegment) o;

        if (!attributes.equals(that.attributes)) return false;
        if (!database.equals(that.database)) return false;
        if (!queryID.equals(that.queryID)) return false;
        if (!table.equals(that.table)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryID.hashCode();
        result = 31 * result + attributes.hashCode();
        result = 31 * result + table.hashCode();
        result = 31 * result + database.hashCode();
        return result;
    }



    @Override
    public String toString() {
        return "QuerySegment{" +
                "QueryId="+getQueryID()+
                "attributes=" + attributes +
                ", table='" + table + '\'' +
                ", database='" + database + '\'' +
                '}';
    }
}