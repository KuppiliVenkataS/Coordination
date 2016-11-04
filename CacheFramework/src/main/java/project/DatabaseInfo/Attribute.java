package project.DatabaseInfo;

/**
 * Created by k1224068 on 12/06/14.
 * This class is a abstraction of attributes or fields of a data table  in structured databases
 * Fields could have numeric, text or objects as their values.
 */
import jade.content.Concept;

import java.io.Serializable;


//public abstract class Attribute implements Concept,Serializable{
public  class Attribute implements Concept,Serializable{
    protected String attributeName; // name of the attribute
    protected String attributeType; // data type of the stored value will be defined here
//    int frequency;
   protected  String condition = null; //for predicates

//constructor
    public Attribute(String str, String type) {
        this.attributeName = str;
        this.attributeType = type;
        //     this.frequency =0;

    }

    public Attribute(String str){
        this.attributeName = str;
        this.attributeType = "";
        //this.frequency = 0;
    }

    protected Attribute(String attributeName, String attributeType, String condition) {
        this.attributeName = attributeName;
        this.attributeType = attributeType;
        this.condition = condition;
    }

    //set and get functions
    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setAttributeName(String str) {
        this.attributeName = str;
    }

    public String getAttributeName() {
        return this.attributeName;
    }
 /*
    public void setFrequency(int i) {
        this.frequency = i;
    }

    public int getFrequency() {
        return frequency;
    }

    public void addFrequency() {
        this.frequency++;
    }

*/
    // other methods
 /*
    public abstract boolean isIndex();
    public abstract String getAttributeType();
    public abstract void setAttributeType(String str);
    */
 public  boolean isIndex(){
     boolean index = true;

     return index;
 };
    public String getAttributeType(){
        return attributeType;
    };
    public  void setAttributeType(String str){};

  //  public abstract boolean equals(Object obj);


    @Override
    public boolean equals(Object obj) {
        Attribute at = (Attribute)obj;
        if (this.getAttributeName().equals(at.getAttributeName()) && (this.getAttributeType().equals(at.getAttributeType())))
            return true;
        else return false;
    }

    @Override
    public int hashCode() {
        int i=this.attributeName.hashCode()+ this.getAttributeType().hashCode();
        return i;
    }



}
