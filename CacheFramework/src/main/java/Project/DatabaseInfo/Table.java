package project.DatabaseInfo;

import jade.content.Concept;
import project.DatabaseInfo.Attribute;

import java.io.Serializable;

import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;


/**
 * Created by k1224068 on 12/06/14.
 * This class is an abstraction for database tables in structured databases
 * This class stores the schematic information metadata. Fields (list of attributes) of each table, available indexes
 * is available at present. This class can be modified or extended for semi-structured / unstructured databases
 * Also, for other information such as the vertical fragmentation and the pattern of data storage etc.
 */

public class Table implements Concept, Serializable {
    private String tableName;
    private ArrayList attributes;
    private ArrayList indexInfo; // contains information on all attributes that are used for indexing

    //constructors
    public Table() {
        this.attributes = new ArrayList();
    }

    public Table(String name){
        this.tableName = name;
        this.attributes = new ArrayList();
    }

    public Table(String name, ArrayList al) {
        this.tableName = name;
        this.attributes = new ArrayList();
        this.attributes = al;
    }

    //methods

    public void setTableName(String name){
        this.tableName = name;
    }

    public String getTableName(){
        return tableName;
    }

    public void setAttributes(ArrayList at){
        for (int i=0; i<at.size(); i++) {
            this.attributes.add(at.get(i));
        }
    }

    public ArrayList getAttributes(){
        return this.attributes;
    }

    public void addSingleAttribute(Attribute at){
        this.attributes.add(at);
    }

    //adds index information of single attribute
    public void setIndexInfo(Attribute at){
        if (indexInfo == null)
            new ArrayList();
        indexInfo.add(at);

    }

    //This method returns list of all indexes
    public ArrayList getIndexInfo(){
        ArrayList at = new ArrayList();
        if (indexInfo == null){
            System.out.println("Table has no indexes");
            return null;
        }

        else {
            return indexInfo;
        }
    }

    // This method returns whether index is present on a specific attribute in a given table
    public boolean isIndexPresent(Attribute at){

        Iterator litr  = indexInfo.iterator();
        while(litr.hasNext()) {
            Attribute a = (Attribute)litr.next();
            if (a.equals(at))
                return true;

        }
        return false;
    }

}

