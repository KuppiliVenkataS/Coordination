package project.QueryEnvironment;

/**
 * Created by santhilata on 04/03/15.
 *
 * Equality / compareTo criteria :
 * both equal = 0
 * this (small) and that big = -1
 * this(big) and that small = 1
 */



import project.MainClasses.PropertiesLoaderImplBase;

import java.util.ArrayList;
import java.util.Stack;

public class ExpressionTree implements Comparable, PropertiesLoaderImplBase {
    String expressionTreeID;
    ExprTreeNode root;
    int no_of_Nodes=0;
    int height;   //depth of the tree including root level (=1)
    ArrayList<Query_Subj_Predicate> subQueries = new ArrayList<>();
    int time_queried ; // only used for indexed_query
    int standardDataSize = standard_Data_Unit_Size;

    private String qspLocation;

    /**
     * Tree can have many levels.
     * While checking for equality (in the equals method, each of these levels are checked
     * equalLevelFlag at each level is initialized to true
     */
    static boolean[] equalLevelFlag = new boolean[20];
    static int level;


    /**
     * Tree can have many levels.
     * While comparing two trees, nodes at each level are visited first (DFS is used)
     * compareLevelFlag at each level is initialized to 0
     */
    static int[] compareLevelFlag = new int[20];
    static int compareLevel;
    static boolean rootFound;
     boolean partialTreeFound = false;// used for partial trees
     boolean visited = false;// used for partial trees

    public static final String FULLY_FOUND = "FULLY FOUND";
    public static final String PARTIALLY_FOUND = "PARTIALLY FOUND";
    public static final String NOT_FOUND = "NOT FOUND";


    public ExpressionTree(){
   }
    /**
     * to construct from queries
     * @param qsp
     */
    public ExpressionTree(Query_Subj_Predicate qsp){
        create_setGraph(qsp.getQueryExpression());

        this.expressionTreeID = qsp.getQueryID();
        this.subQueries = traverseTree();

    }

    public String getQspLocation() {
        return qspLocation;
    }

    public void setQspLocation(String qspLocation) {
        this.qspLocation = qspLocation;
    }

    public int getTime_queried() {
        return time_queried;
    }

    public void setTime_queried(int time_queried) {
        this.time_queried = time_queried;
    }

    public String getExpressionTreeID() {
        return expressionTreeID;
    }

    public void setExpressionTreeID(String expressionTreeID) {
        this.expressionTreeID = expressionTreeID;
    }

    public  boolean isPartialTreeFound() {
        return partialTreeFound;
    }

    public  void setPartialTreeFound(boolean partialTreeFound) {
        this.partialTreeFound = partialTreeFound;
    }

    public ExprTreeNode getRoot() {
        return root;
    }

    public void setRoot(ExprTreeNode root) {
        this.root = root;
        setNodesGraph(root);
        addQueryExpressionsToLeaves();
        addQueryExpressionsToNonLeaves();
        countNodes();
        setHeightNode();
        setHeightTree();

    }

    public int getNo_of_Nodes() {

       // System.out.println(" PPPPPP "+no_of_Nodes +" "+getRoot().getValue().getQueryExpression());
        return no_of_Nodes;
    }

    public void setNo_of_Nodes(int no_of_Nodes) {
        this.no_of_Nodes = no_of_Nodes;
    }

    /**
     * This method is to combine createGraph and evaluate them
     * @param input
     * @return
     */
    public void create_setGraph (String input){
        ExprTreeNode root = createGraph(input);
        setNodesGraph(root);
        addQueryExpressionsToLeaves();
        addQueryExpressionsToNonLeaves();
        countNodes();
        setHeightNode();
        setHeightTree();
        // return root;
    }
///===========================FOLLOWING ARE EXPRESSION TREE CONSTRUCTION FUNCTIONS=====================================
    /**
     * This method generates a Query graph from the given  query expression of String type
     * Leaf nodes are the smallest query fragments and intermediate nodes have value equal to '&' or '_'
     * But even leaf nodes are simple String components
     * @param str
     * @return
     */
    public ExprTreeNode createGraph (String str){ //  parse

        ExprTreeNode root = new ExprTreeNode(); //root is always a dummy node

        root.value =  new Query_Subj_Predicate("Root");
        ExprTreeNode anotherNode = create(str, root);   // recursive call- let us see what this node is
        this.root = root;  //set root of the tree

        return root;
    }

    /**
     * This is the internal method used by createGraph method
     * @param str
     * @param root
     * @return
     */
    ExprTreeNode create(String str, ExprTreeNode root) {  // root is the overall parent

        ExprTreeNode tempSN = null;
        ExprTreeNode leftNode = null;

        int i = 0; int m = 0;
        int length = str.length();

        // first leftmost node
        if (str.charAt(i) == '(') {

            leftNode = findNextNode(str, i);
            String nodeStr = leftNode.value.getQueryExpression();
            leftNode.parent= root;
            root.childNodes++;
            root.children[root.childNodes-1]=leftNode;

            //  i = m;

            //====

            if (nodeStr.charAt(0)=='('){

                create(nodeStr,leftNode);
            }
            //===
            m = leftNode.position;

            //attach to root


            while (i< length) {    // not reached to the end
                if ((m+1) < length ) {

                    char ch = str.charAt(m + 1);

                    i = m+2;
                    tempSN =  findNextNode(str, i); // create second node
                    nodeStr = tempSN.value.getQueryExpression();

                    //====
                    if (nodeStr.charAt(0)=='('){
                        create(nodeStr,tempSN); //recursive call
                    }

                    //===
                    m = tempSN.position;

                    if (ch == '_') {
                       root.value.setQueryExpression("_");
                    }

                    else if (ch == '&') {
                        root.value.setQueryExpression("&");
                    }

                    leftNode.parent.childNodes++;
                    leftNode.parent.children[root.childNodes-1]=tempSN;
                    tempSN.parent = leftNode.parent;
                }

                i = m+2;

            }     //while


        }    // closing bracket for if (str.charAt(i) == '(')
        else {
            root.childNodes++;
            leftNode.parent = root;
            root.children[root.childNodes - 1]=leftNode;


        }

        return root;
    }    // createGraph closing

    /**
     * createGraph method creates String fragments separated by "()" and '&' or '_'
     * The following method converts those String fragments at every node into Query_Subj_Predicate objects.
     * To set names and values in the given graph
     * @param root
     */
    ExprTreeNode setNodesGraph(ExprTreeNode root){

        setNodes(root);
        return root;
    }

    /**
     * A private method used by setNodesGraph
     * @param root
     */
    void setNodes(ExprTreeNode root){

        if (root.childNodes == 0){//leaf node
            root.setLeaf(true);  // leaf nodes have query fragments

            if (root.parent.value.getQueryExpression().equals("&")) {

                if (root.parent.name == "999") {
                    root.parent.name = "(" + "(" + root.value.getQueryExpression() + ")";

                }
                else {
                    root.parent.name = root.parent.name + "&" +"("+root.value.getQueryExpression()+")";
                }

                //  System.out.println("Parent is "+root.parent.name);
            }
            else {
                if (root.parent.name == "999") {
                    root.parent.name = "(" + "(" + root.value.getQueryExpression() + ")";
                }
                else {
                    root.parent.name =root.parent.name +"_"+"("+root.value.getQueryExpression()+")";
                }

                //  System.out.println("Parent is "+root.parent.name);
            }
            String line = root.value.getQueryExpression();
            root.value = getQSP(line);

            root.value.setQueryExpression("");


        }

        else {
            int i =0;
            while (i < root.childNodes){

                setNodes(root.children[i]);
                if (root.children[i].childNodes!=0 ){
                    if (root.name!= null)
                        root.name =root.name+root.value.getQueryExpression()+root.children[i].name ;
                    else root.name = "("+root.children[i].name ;
                }

                i++;
            }

            root.name = root.name +")";

        }

    }

    ExprTreeNode findNextNode(String str, int i){

        int m = findMatchingRightParen(str, i );
        String expr = str.substring(i+1 , m );
      //  System.out.println("Expr is "+expr);

        ExprTreeNode tempSN = new ExprTreeNode(expr);
        tempSN.position = m;

        return  tempSN;
    }

    int findMatchingRightParen (String s, int leftPos) {
        Stack<Character> stack = new Stack<Character>();
        stack.push (s.charAt(leftPos));
        for (int i=leftPos+1; i<s.length(); i++) {
            char ch = s.charAt (i);
            if (ch == '(') {
                stack.push (ch);
            }
            else if (ch == ')') {
                stack.pop ();
                if ( stack.isEmpty() ) {
                    // This is the one.
                    return i;
                }
            }
        }
        // If we reach here, there's an error.
       // System.out.println ("ERROR: findRight: s=" + s + " left=" + leftPos);
        return -1;
    }

    int findMatchingLeftParen (String s, int rightPos) {
        Stack<Character> stack = new Stack<Character>();
        stack.push (s.charAt(rightPos));
        for (int i=rightPos-1; i>=0; i--) {
            char ch = s.charAt (i);
            if (ch == ')') {
                stack.push (ch);
            }
            else if (ch == '(') {
                stack.pop ();
                if ( stack.isEmpty() ) {
                    // This is the one.
                    return i;
                }
            }
        }
        // If we reach here, there's an error.
       // System.out.println ("ERROR: findLeft: s=" + s + " right=" + rightPos);
        return -1;
    }

    public void increaseDataSize(int dataSize){
        standardDataSize += dataSize;
    }

    public void decreaseDataSize(int dataSize){
        standardDataSize -= dataSize;
    }

    /**
     * This method is to create a qsp from
     * @param line
     * @return
     */
    public Query_Subj_Predicate getQSP (String line){


        ArrayList lsqs = new ArrayList();
        ArrayList lpqs = new ArrayList();

        String[] sub_pred = line.split("#");

        int sp = sub_pred.length;

        // subject query segments

        String[] segment = sub_pred[0].split(">");
        int noOfSegments = segment.length;
        int index = 0;
        while (index < noOfSegments) {

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
            lsqs.add(index,qs);

            index++;
          //  noOfSegments--;
        }     // while (noOfSegments >0)

        index=0;
        if (sp > 1) { // now do the same with predicate query segments
            if (sub_pred[1].charAt(sub_pred[1].length()-1)== ')')
                sub_pred[1] = sub_pred[1].substring(0,sub_pred[1].length()-1);
            segment = sub_pred[1].split(">");
            noOfSegments = segment.length;
          //  System.out.println("From Expression tree class "+ sub_pred[1]);
            while (index < noOfSegments) {

                ArrayList predicateAttributes1 = new ArrayList();
                ArrayList predicateAttributes2 = new ArrayList();
                String table1, table2;
                String database1,database2;

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

                lpqs.add(index,pqs);
                index++;

               // noOfSegments--;
            }     // while (noOfSegments >0)

        }//  if (sp > 1) -- now do the same with predicate query segments



        Query_Subj_Predicate qsp = new Query_Subj_Predicate(lsqs,lpqs);

        qsp.setQueryExpression(line);
       //  qsp.printQuery();
        return qsp;

    }

    /**
     * This method is to set query expressions to leaf nodes
     */
    public void addQueryExpressionsToNonLeaves(){
        addQueryExpressionsNonLeaf(this.getRoot());
    }
    void addQueryExpressionsNonLeaf(ExprTreeNode etn){

        if(!etn.isLeaf()) {
            String str =  etn.getValue().getQueryExpression();

            String str1 = "";
            for (int i = 0; i < etn.getChildNodes(); i++) {
               if (!etn.getChildren(i).isLeaf()) {
                   addQueryExpressionsNonLeaf(etn.getChildren(i));
                   str1 = str1+"("+etn.getChildren(i).getValue().getQueryExpression()+")"+str;
               }
                else  {
                   str1 = str1+etn.getChildren(i).getValue().getQueryExpression()+str;
               }
            }
            str1 = str1.substring(0,str1.length()-1);

            etn.getValue().setQueryExpression(str1);
        }
    }
    /**
     * This method is to set query expressions to leaf nodes
     */
    public void addQueryExpressionsToLeaves(){
        addQueryExpressions(this.getRoot());
    }
    void addQueryExpressions(ExprTreeNode etn){

        if(etn.getValue().getQueryExpression().equals("Root")){
            String s = etn.getValue().getQueryExpression();
            s = s.substring(0,s.length()-3);
            etn.getValue().setQueryExpression(s);
        }
        if(etn.getValue().getQueryExpression().equals("")) {
       // if(etn.isLeaf()) {
            etn.getValue().setQueryExpression(etn.getValue().createExpressionFromQuery());
        }
       if(etn.getName().equals("999")){
            etn.setName(etn.getValue().getQueryExpression());
        }

        else{

            for (int i = 0; i <etn.getChildNodes() ; i++) {
                addQueryExpressions(etn.getChildren(i));
            }
        }
    }

    /**
     * the following code sets height to each of the nodes
     */
    public void setHeightNode(){

        setHeightN(root);

    }
    void setHeightN(ExprTreeNode root){
        if (root.isLeaf()){
            //root.setHeight(root.parent.getHeight()+1);
            return;
        }

        for (int i = 0; i < root.childNodes; i++) {
            root.getChildren(i).setHeight(root.getHeight()+1);
            setHeightN(root.getChildren(i));
        }
    }

    /**
     * The following code sets height to the tree
     */
    public void setHeightTree(){

        if (root.childNodes == 1){
            height = 1;
            return ;
        }

        setHeightRecursiveTree(root);

    }
    void setHeightRecursiveTree(ExprTreeNode root){
        if (root.isLeaf()){
            if (height < root.getHeight()){
                height = root.getHeight();
            }
            return;
        }
        for (int i = 0; i < root.childNodes; i++) {
            setHeightRecursiveTree(root.getChildren(i));
        }

    }

    /**
     * This method is used in finding partial trees
     * sets the root node TRUE if all its child nodes are TRUE
     */
    public void setFoundTree(){
        if (root.childNodes == 1 && root.getChildren(0).isNodeFoundInCache()){
            root.setNodeFoundInCache(true);
            return ;
        }
        setFound(root);
    }
    private void setFound(ExprTreeNode root) throws NullPointerException{
        if (root.isLeaf() && !root.isNodeFoundInCache() && root.getParent()!=null){
            root.getParent().setNodeFoundInCache(false);
            return;
        }
       /* else if (root.isLeaf() && root.isNodeFoundInCache() && root.getParent()!=null){
            root.getParent().setNodeFoundInCache(true);
            return;

        }

        else if (!root.isLeaf() && root.isNodeFoundInCache() ){
            if(root.getParent()!= null)
            root.getParent().setNodeFoundInCache(true);
        }
        */
        else {
            if(root.getParent()!= null)
                root.getParent().setNodeFoundInCache(true);
        }

        for (int i = 0; i <root.getChildNodes() ; i++) {
                    setFound(root.getChildren(i));
        }


    }

    public double calculateDataSizeFoundTree(){
        if (root.childNodes == 1 && root.getChildren(0).isNodeFoundInCache()){
            root.setDataSize(standardDataSize);
            return root.getDataSize() ;
        }
       else {
            setDataSizeFound(root);

//            System.out.println("from get datasize " + root.getDataSize());
            return root.getDataSize();
        }
    }
    private void setDataSizeFound(ExprTreeNode root) throws NullPointerException{

        if(root.isNodeFoundInCache() && root.getParent()!=null) {

            if (root.isLeaf()) {
                root.setDataSize(standardDataSize);
//                System.out.println("from dataSizeFound Expression tree " + root.getValue().getQueryExpression());
//
//                System.out.println(root.getParent().getDataSize());
                return;
            }

        }


        for (int i = 0; i <root.getChildNodes() ; i++) {
            setDataSizeFound(root.getChildren(i));

          //  if(root.getChildren(i).isNodeFoundInCache() && !root.isLeaf()&& root != null){
            if(!root.isLeaf()&& root != null){
//                System.out.println("from dataSizeFound Expression tree non leaf before " + root.getValue()
//                        .getQueryExpression()+" "+root.getDataSize());
                root.setDataSize(root.getDataSize() + root.getChildren(i).getDataSize());
//                System.out.println("from dataSizeFound Expression tree non leaf after " + root.getValue()
//                        .getQueryExpression() + " " + root.getDataSize());

            }
        }


    }

    private ArrayList<Query_Subj_Predicate> queriesNotFound = new ArrayList<>();;

    public ArrayList<Query_Subj_Predicate> set_GetQueriesNotFoundTree(){

        if (root.childNodes == 1 && root.getChildren(0).isNodeFoundInCache()){

            return  queriesNotFound;
        }

        else if (root.childNodes == 1 && !root.getChildren(0).isNodeFoundInCache()){

            queriesNotFound.add(root.getValue());
            return queriesNotFound;
        }

        subQueriesNotFound(root);

        return queriesNotFound;
    }
    private void subQueriesNotFound(ExprTreeNode root) throws NullPointerException{

        if(!root.isNodeFoundInCache() ) {
            if (root.isLeaf()) {

                queriesNotFound.add(root.getValue());
                return;
            }
        }
        for (int i = 0; i <root.getChildNodes() ; i++) {
            subQueriesNotFound(root.getChildren(i));
        }
    }

    /**
     * This method calculates the total data size needed of the tree
     * @return
     */
    public double calculateTotalDataSizeNeededTree(){
        double dataSize =   this.getNo_of_Nodes()*standardDataSize;



//        System.out.println(this.getRoot().getValue().getQueryExpression()+" %%%%%%%%%%%%%%%%%%  expressiontree "+dataSize);
        return (dataSize);

    }

    /**
     * To count the number of nodes in the tree
     * @return
     */
    public int countNodes(){
        count(this.getRoot());

//        System.out.println("From count nodes " + no_of_Nodes + this.getRoot().getValue().getQueryExpression());
        return no_of_Nodes;
    }
    private void count (ExprTreeNode root){
        if (root.isLeaf()){
            this.no_of_Nodes++;
        }
        else {
            for (int i = 0; i < root.getChildNodes(); i++) {
                count(root.getChildren(i));
            }
        }
    }

    public int getHeight(){
        return height;
    }

//=====================================================================================================================

    /**
     * This practically adds a node to the tree
     * @param parent
     * @param exprTreeNode
     * @param operation
     */
    public void addNodeToTree(ExprTreeNode parent, ExprTreeNode exprTreeNode, String operation ){
        //TODO
    }

    /**
     * This method practically deletes a node from the tree
     * @param parent
     * @param exprTreeNode
     * @param operation
     */
    public void deleteNodeFromTree(ExprTreeNode parent, ExprTreeNode exprTreeNode, String operation ){
        //TODO
    }

    /**
     * to check whether a given querySegment is a part of the tree
     * gets the tree root node which is the String expression of the entire query
     * @param etn
     * @return
     */
    public boolean containsExpression(ExprTreeNode etn){
        String str = this.getRoot().getValue().getQueryExpression();
        Query_Subj_Predicate qsp = etn.getValue();
        String findExpression ="";
        if(etn.getValue().getSubjectQuerySegments() != null)
            findExpression = qsp.createExpressionFromQuery();

       else  findExpression = etn.getValue().getQueryExpression();


       /**
        * The following code will be useful when it is needed to find the position of the substring occurrence
        *
        */

       /*
       int lastIndex = 0;
       int count =0;

       while(lastIndex != -1){

           lastIndex = str.indexOf(findExpression,lastIndex);

           if( lastIndex != -1){
               count ++;
               lastIndex+=findExpression.length();
           }
       }
       */

        System.out.println("from expression tree contains " + findExpression + " " + str + "   " + etn.getValue().getQueryExpression());

        if (str.contains(findExpression))
        return true;

        else return false;
   }

    /**
     * This method traverses through the tree and checks whether the node is present
     * @param etn
     * @return
     */
    public boolean containsQueryNode(ExprTreeNode etn){
        rootFound = false;
        return  containsQuery(this.getRoot(),etn);
    }
    boolean containsQuery(ExprTreeNode root,ExprTreeNode etn){

        if (root.isLeaf() ) {

            if (root.equals(etn)) {
                rootFound = true;

                return rootFound;
            }
           //else part is not written

        }
        else {
            if (root.equals(etn)) {
                rootFound = true;
                return rootFound;
            }
            for (int i = 0; (i < root.childNodes) && !(rootFound); i++) {

               if(root.getChildren(i).getValue() != null){
              //     System.out.println(root.getChildren(i).getName()+" "+i);
                   //root.getChildren(i).getValue().printQuery();
               }
                 containsQuery(root.getChildren(i), etn);
            }
        }

       // System.out.println("Root found from containsQuery "+ rootFound);
        return rootFound;
    }

    @Override
    public boolean equals(Object o) {

        boolean equal1 = true;
        ExpressionTree that = (ExpressionTree) o;

        if (this == o) {
         //   System.out.println(" This Tree is equal to the Object o ");
            return true;
        }

        if (this.getNo_of_Nodes() != that.getNo_of_Nodes())  return false;

        if (root != null ? !root.equals(that.root) : that.root != null) return false;
        if (root != null ? !root.getName().equals(that.root.getName()) : that.root != null) return false;

        if (!(this.getRoot().equals(that.getRoot()))) {
         //   System.out.println(" Trees are not same as roots are not equal ");
            return false;
        }
        else {
            for (int j =0; j <20; j++){  //max no of levels is assumed to be 20
                equalLevelFlag[j] = true;
                level = 0;
            }
            equal1 =  equalTo(this.getRoot(),that.getRoot());
        }

      //  System.out.println(" Trees are equal "+equal1);

        return equal1;
    }
    private boolean equalTo(ExprTreeNode root1, ExprTreeNode root2){
       // System.out.println("Now comparing "+ root1.getName()+" "+root2.getName());

        //both are leaves
        if (root1.isLeaf() && root2.isLeaf()) {
            if (root1.getValue().equals(root2.getValue())) {
              //  System.out.println("Roots are leaves and they are equal "+root1.getName() +" "+root2.getName());
                return true;
            }
            else {
                equalLevelFlag[level] = false;
             //   System.out.println("Roots are leaves  but they are not equal "+root1.getName() +" "+root2.getName());
                return false;
            }
        }

        //one is a leaf and the other not
        if (((root1.isLeaf()) &&(!root2.isLeaf())) ||((!root1.isLeaf() ) &&(root2.isLeaf())))
        {

            equalLevelFlag[level] = false;
            return false;
        }

        //compare root nodes at every similar positions
        if(!(root1.equals(root2))) {

            equalLevelFlag[level] = false;
            return false;
        }
        if (root1.getChildNodes() != root2.getChildNodes()) {

            equalLevelFlag[level] = false;
            return false;
        }

        level++;  // increment level for children
        for (int i = 0; (i < root1.getChildNodes())&&(equalLevelFlag[level]==true) ; i++) {
          //  System.out.println("children "+root1.getChildNodes()+" "+root2.getChildNodes());

            equalTo(root1.getChildren(i), root2.getChildren(i));
        }

        for (int i = 0; i <20 ; i++) {
            if (equalLevelFlag[i] == false) {
                equalLevelFlag[0] = false;
                break;
            }
        }

        return equalLevelFlag[0];
       // return true;
    }

    @Override
    public int hashCode() {
        int result = root != null ? root.getName().hashCode() : 0;
        result = 31 * result + no_of_Nodes;

      //  if(root!=null)  System.out.println(root.getName());
      //  else System.out.println("root is null");
        return result;
    }

    @Override
    public int compareTo(Object o) {
        int value = 0;
        ExpressionTree that = (ExpressionTree) o;

        if (this.equals(that)) {
          //  System.out.println(" Both trees are same");
            return 0;
        }
        else {
           // System.out.println();
            compareLevel = 0;
            value =   compare(this.getRoot(), that.getRoot());
        }


        return value;
    }
    /**
     * // return value 1 stands for not equal and 0 stands for equal
     * @param root1
     * @param root2
     * @return a int value True if both trees are same and false if they are not
     */
    private int compare(ExprTreeNode root1, ExprTreeNode root2){

      //  System.out.println(compareLevel+" from compare to "+root1.getChildNodes()+" "+root2.childNodes+" "+root1.getName() +" "+root2.getName());
        //check for leaf nodes
        if ((root1.getChildNodes() == 0) && (root2.getChildNodes() > 0)) {
         //   System.out.println(compareLevel+" Compare: root1 children are less than root2 children "+root1.getName() +" "+root2.getName());

            compareLevelFlag[compareLevel] = -1;

            if (compareLevel >0) {
                if (compareLevelFlag[compareLevel] != 0) compareLevelFlag[compareLevel-1]= compareLevelFlag[compareLevel];
                compareLevel--;
            }
            return -1;
        }

        else if ((root1.getChildNodes() > 0) && (root2.getChildNodes() == 0)) {
          //  System.out.println(compareLevel+" Compare: root1 children are more than root2 children "+root1.getName() +" "+root2.getName());
            compareLevelFlag[compareLevel] = 1;
            if (compareLevel >0){
                if (compareLevelFlag[compareLevel] != 0) compareLevelFlag[compareLevel-1]= compareLevelFlag[compareLevel];
                compareLevel--;
            }
            return 1;
        }

        else if ((root1.isLeaf()) && (root2.isLeaf())){
           // System.out.println(compareLevel+" Compare: root1 has no children and  root2 has no children-- leaves "+root1.getName() +" "+root2.getName());
            compareLevelFlag[compareLevel] = root1.compareTo(root2);
          //  System.out.println(compareLevel+" from equal number of children "+compareLevelFlag[compareLevel]);
            if (compareLevel >0){
                if (compareLevelFlag[compareLevel] != 0) compareLevelFlag[compareLevel-1]= compareLevelFlag[compareLevel];
                compareLevel--;
            }
           // return  compareLevelFlag[compareLevel];
        }

       // if(compareLevelFlag[compareLevel] ==0) {

            // both roots have children
            if (root1.getChildNodes() < root2.getChildNodes()) {
                compareLevelFlag[compareLevel] = -1;
              //  System.out.println(compareLevel+" On same level, root1 has less number("+root1.childNodes + ") of children than root2 ("+root2.childNodes+") "+root1.getName() +" "+root2.getName());
                if (compareLevel >0) {
                    if (compareLevelFlag[compareLevel] != 0) compareLevelFlag[compareLevel-1]= compareLevelFlag[compareLevel];
                    compareLevel--;
                }
                return -1;
            }

            if (root1.getChildNodes() > root2.getChildNodes()){
                compareLevelFlag[level] = 1;
               // System.out.println(compareLevel+" On same level, root1 has more number("+root1.childNodes + ") of children than root2 ("+root2.childNodes+") "+root1.getName() +" "+root2.getName());
                if (compareLevel >0){
                    if (compareLevelFlag[compareLevel] != 0) compareLevelFlag[compareLevel-1]= compareLevelFlag[compareLevel];
                  //  System.out.println(" Nodes are not same");
                    compareLevel--;
                }
                return 1;
            }
            else {
              //  System.out.println(" Entering equal number of children");
                compareLevel++;
                for (int i = 0; i < root1.getChildNodes(); i++) {
                      // increment level for children
                    if(compareLevelFlag[compareLevel] ==0)
                        compare((ExprTreeNode) root1.getChildren(i), (ExprTreeNode) root2.getChildren(i));
                    else break;
                }
            }
       // }
    //    System.out.println(compareLevel+"  both are same? " +root1.getName() +" "+root2.getName()+" "+compareLevelFlag[compareLevel]);
        if (compareLevel >0){
            if (compareLevelFlag[compareLevel] != 0) compareLevelFlag[compareLevel-1]= compareLevelFlag[compareLevel];
            compareLevel--;

        }
        //System.out.println(" At the end "+compareLevel);


        return compareLevelFlag[compareLevel];

    }

    /**     *
     * Tree traversal  is used in counting number of subQueries in a tree.
     */
    ArrayList<Query_Subj_Predicate> traverseTree(){

        if (getRoot().getChildNodes() == 1){ //single node trees
            subQueries.add(getRoot().getChildren(0).getValue());
        }
        else{
            traverse(getRoot());
        }
        return  subQueries;
    }
    private ExprTreeNode traverse(ExprTreeNode root){

        if (root.isLeaf()) {

            subQueries.add(root.getValue());
            return root;
        }

        else {

            for (int i = 0; i < root.getChildNodes() ; i++) {
                traverse(root.getChildren(i));
            }
        }

        return root;
    }

    public ArrayList<Query_Subj_Predicate> getSubQueries() {
        return subQueries;
    }

    public void setSubQueries(ArrayList<Query_Subj_Predicate> subQueries) {
        this.subQueries = subQueries;
    }

    /**
     * To print a graph
     * @param
     */
    public void printGraph(){
        print(this.getRoot());
    }
    void print(ExprTreeNode root) throws NullPointerException{
        System.out.println(root.getName() + " number of children " + root.childNodes + " ");
        System.out.println(root.value.queryExpression + " number of children " + root.childNodes + " ");
        System.out.println("Height of this node is "+root.getHeight());
        System.out.println(root.isNodeFoundInCache());

        if (root.parent != null) {

              //   System.out.println("Root name is "+ root.getName());
              //   System.out.println("Root Query expression is " + root.value.getQueryExpression());

        }
        int  i= root.childNodes;
        System.out.println("Current node children "+ i);
        int j =0;

        while (j < i){
            System.out.println("checking children");
            print(root.children[j]) ;
            j++;
        }

    }

    /**
     * the following method's aim is to pack all nodes in the tree into an arraylist.
     * If a bigger node is found then need not go and search the lower nodes.
     * @return
     */
    public ArrayList<ExprTreeNode> packTreeInBFS_array(){

        ArrayList<ExprTreeNode> arrayNodes = new ArrayList<>();
        arrayNodes.add(root);
        ArrayList<ExprTreeNode> tempList = new ArrayList<>();

        int i = 0;

        while (arrayNodes.get(arrayNodes.size()-1).getHeight() < this.getHeight()) {

            for (; i < arrayNodes.size(); i++) {

                for (int j = 0; j < arrayNodes.get(i).getChildNodes(); j++) {
                    tempList.add(arrayNodes.get(i).getChildren(j));
                }
            }

            arrayNodes.addAll(tempList);
            tempList.clear();
        }

    /*    ArrayList<Query_Subj_Predicate> qsps = getSubQueries();
        for(Query_Subj_Predicate qsp:qsps) {
           // ExprTreeNode etn = new ExprTreeNode(qsp);
            arrayNodes.add(new ExprTreeNode(qsp));
        }
        */

        return arrayNodes;
    }

    public static void main(String[] args) {


        String expression7 = "(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)";
        String expression71 = "<sand83:tab23:db5>#<asd13:tab3:db5,lt25>";
        String expression8 = "((<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)_((<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>)_(<sand83:tab23:db5>#<asd13:tab3:db5," +
                "lt25>)))_(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)";
        String expression81 =  "(<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)_(<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>)";

        String expression9 = "(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)_((<slr23:tab43:db1>#<xcv35:tab5:db3,eq33>)_(<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>))";
        String expression101 = "(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)_((<slr23:tab43:db1>#<xcv35:tab5:db3,eq33>)_(<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>))";
        String expression10 = "(<sand83:tab23:db5>#<asd13:tab3:db5,lt25>)_((<slr23:tab43:db1><usr13:tab43:db2>#<xcv35:tab5:db3,xlr63:tab43:db4,eq33>)_" +
                "(<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>))";

        Query_Subj_Predicate[] qsps = new Query_Subj_Predicate[10];


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

/**
        ExpressionTree et1 = new ExpressionTree(qsps[1]);
        ExpressionTree et2 = new ExpressionTree(qsps[6]);



        IndexedQuery iq1 = new IndexedQuery(et1);
        IndexedQuery iq2 = new IndexedQuery( et2);

        String input = expression10;

        // System.out.println(input);
        ExpressionTree g = new ExpressionTree();
        g.create_setGraph(input);

        ExpressionTree et = new ExpressionTree(new Query_Subj_Predicate(expression8));
        et.printGraph();
        et.getNo_of_Nodes();
        ArrayList<Query_Subj_Predicate> testList = et.getSubQueries();

    *  System.out.println("size: "+testList.size());
        Iterator<Query_Subj_Predicate> itr = testList.iterator();
        while(itr.hasNext()){
            itr.next().printQuery();
        }

        ExpressionTree et7 = new ExpressionTree(new Query_Subj_Predicate(expression7));
        ExpressionTree et9 = new ExpressionTree(new Query_Subj_Predicate(expression9));



        if (et9.containsQueryNode(et7.getRoot().getChildren(0)))
            System.out.println("done");


        else System.out.println("Huh");

        System.out.println(et7.getNo_of_Nodes());

 *

        Cache_Reply cr = et9.searchTreeComplete(et7);
      //  System.out.println(cr.getReplyStatus());

        ExpressionTree et8 = new ExpressionTree(new Query_Subj_Predicate(expression8));
        et8.setHeightNode();
        et8.setHeightTree();
        System.out.println("Height = " + et8.getHeight());

       // et8.printGraph();

        ArrayList<ExprTreeNode> arrayNodes = et8.packTreeInBFS_array();

        for (int i = 0; i < arrayNodes.size(); i++) {
            System.out.println(arrayNodes.get(i).getName());
        }
    */
        System.out.println("*************");
        ExpressionTree ett2 = new ExpressionTree(new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)"));

        ExpressionTree ett1 = new ExpressionTree(new Query_Subj_Predicate("(<xlr63:tab43:db3><qry33:tab23:db3>#<qry31:tab21:db1,gt345>)&((<jkat22:tab32:db2>#<qry31:tab21:db1,gt345>)&(<Harry22:tab32:db2>#<Mouny31:tab21:db1,gt345>))"));
       ;

        System.out.println(ett1.calculateDataSizeFoundTree()); //0.0
        System.out.println(ett1.calculateTotalDataSizeNeededTree()); //30.0

        ett1.increaseDataSize(20);
        System.out.println("Total data size needed by ett1   " + ett1.calculateTotalDataSizeNeededTree()); // 90.0

        System.out.println("1 " +ett1.getNo_of_Nodes()); // 1 3

        ExpressionTree expressionTree = new ExpressionTree();
        ArrayList<ExprTreeNode> someList = ett1.packTreeInBFS_array();
        expressionTree.setRoot(someList.get(0));
        System.out.println("2 " + expressionTree.getNo_of_Nodes()); //2 3

        ExpressionTree expressionTree1 = new ExpressionTree(new Query_Subj_Predicate("" +
                "(<qry32:tab22:db2><axt412:tab12:db2><jkat22:tab32:db2>#<qry34:tab24:db4,gt345>)&(<jkat25:tab35:db5>#<qry33:tab23:db3,gt345>)"));
        System.out.println(expressionTree1.calculateDataSizeFoundTree()); //0.0
    }

}// end of class