package project.QueryEnvironment;

import java.util.Arrays;

/**
 * Created by santhilata on 04/03/15.
 */
public class ExprTreeNode implements Comparable {

    Query_Subj_Predicate value;
    String name;
    ExprTreeNode[] children;
    int childNodes ;
    ExprTreeNode parent;
    boolean isLeaf;

    // the following variables are for ExpressionTree generation
    int position;
    int no_evaluated; //if node is not a leaf, this returns the value of all evaluated children
    int toEvaluate;// no of child nodes for each node to be sent for evaluation
    int comparisonValue;
    int height;
    double dataSize; // useful for only for the partial trees
    boolean nodeFoundInCache=false;//useful for only for the partial trees

    public ExprTreeNode[] getChildren() {

        return children;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
// boolean nodeDone;

    public ExprTreeNode(){
        children = new ExprTreeNode[10];
        childNodes = 0;
        no_evaluated = 0;
        toEvaluate = 0;
        name = "999";
    }

    public ExprTreeNode(String str){
        children = new ExprTreeNode[10];
        childNodes = 0;
        no_evaluated = 0;
        toEvaluate = 0;
        value = new Query_Subj_Predicate(str);
        value.createQuerySegmentFromExpression(str);
        name = "999";
    }

    public ExprTreeNode (Query_Subj_Predicate qsp){
        children = new ExprTreeNode[10];
        childNodes = 0;
        no_evaluated = 0;
        toEvaluate = 0;
        value = qsp;
        //create expression from query
        name = "999";
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getComparisonValue() {
        return comparisonValue;
    }

    public void setComparisonValue(int comparisonValue) {
        this.comparisonValue = comparisonValue;
    }

    public Query_Subj_Predicate getValue() {
        return value;
    }

    public void setValue(Query_Subj_Predicate value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExprTreeNode getChildren(int i) {

        return children[i];
    }

    public boolean isNodeFoundInCache() {
        return nodeFoundInCache;
    }

    public void setNodeFoundInCache(boolean nodeFoundInCache) {
        this.nodeFoundInCache = nodeFoundInCache;
    }

    public void setChildren(ExprTreeNode[] children) {
        this.children = children;
    }

    public int getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(int childNodes) {
        this.childNodes = childNodes;
    }

    public ExprTreeNode getParent() {
        return parent;
    }

    public void setParent(ExprTreeNode parent) {
        this.parent = parent;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public int getNo_evaluated() {
        return no_evaluated;
    }

    public void setNo_evaluated(int no_evaluated) {
        this.no_evaluated = no_evaluated;
    }

    public void addNo_evaluated(int i){
        no_evaluated += i;
    }

    public int getToEvaluate() {
        return toEvaluate;
    }

    public void setToEvaluate(int toEvaluate) {
        this.toEvaluate = toEvaluate;
    }



    public void addChild(ExprTreeNode etn){
        for (int i=0; i < 10; i++){
            if (this.getChildren(i) == null){
                children[i] = etn;
            }
        }
    }

    public double getDataSize() {
        return dataSize;
    }

    public void setDataSize(double dataSize) {
        this.dataSize = dataSize;
    }

    /**
     * equals method only compares the query segment
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExprTreeNode)) return false;
        ExprTreeNode that = (ExprTreeNode) o;

     //   System.out.println("From Expr Tree node "+this.getName()+" "+that.getName());

        if (value != null ? !value.equals(that.value) : that.value != null) return false;


        return true;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    public int aggregate(){
        return no_evaluated;

    }


    @Override
    public int compareTo(Object o) {
        ExprTreeNode that = (ExprTreeNode) o;

        if ( (this.getValue() == null)&&(that.getValue()== null)) return 0;
        if ( (this.getValue() == null)&&(that.getValue()!= null)) return -1;
        else if ( (this.getValue() != null)&&(that.getValue() == null)) return 1;

        return (this.getValue().compareTo(that.getValue()));

       // return 0;
    }

    public static void main(String[] args) {
        String expression8 = "<sand83:tab23:db5>#<asd13:tab3:db5,lt25>";
        String expression9 = "<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>";

        Query_Subj_Predicate qsp = new Query_Subj_Predicate();
        qsp.createQuerySegmentFromExpression(expression9);

        ExprTreeNode  etn1 = new ExprTreeNode(qsp);
        ExprTreeNode etn2 = new ExprTreeNode(expression9);

        //System.out.println(etn1.compareTo(etn2));
        etn1.getValue().printQuery();
        System.out.println("22222222222222222");
        etn2.getValue().printQuery();

        System.out.println(etn1.equals(etn2));
    }
}
