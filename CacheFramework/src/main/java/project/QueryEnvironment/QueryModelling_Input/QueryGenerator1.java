package project.QueryEnvironment.QueryModelling_Input;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;


import java.io.*;
import java.util.*;

/**
 * Created by santhilata on 14/12/15.
 * Code for query arrival patterns is not written here as this code is only to generate queries
 * Query arrival patterns is written in the CommunityCacheMain.java
 */
public class QueryGenerator1 {

    static int NUMBER_OF_QUERY_SEGMENTS;
    static int noOfPQSS;
    int NUMBER_OF_QUERIES =0;
    private String distribution ="";
    private int databases;
    private int tables;
    private int attributes;

    private static int noOfSQS = 0; //
    ArrayList<String> sqss = new ArrayList<>();
    ArrayList<String> pqss = new ArrayList<>(50);
    ArrayList<String> sub_queries = new ArrayList<>();// single sub-queries
    ArrayList<String> TwoSubQuery_Sequences = new ArrayList<>(); // combination of two sub-queries
    ArrayList<String> ThreeSubQuery_Sequences = new ArrayList<>(); // combination of three sub-queries

    int sample=0;
    int height = 3;
    int childHeight =1;

    int noofQSPs=5;


    final String[] conditionArray = {"eq","gt","lt","gte","lte","true","false","other","between"};

    public static final String BACKUP_FILE = "" +
            ".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryExpressions_BACKUP";


   public QueryGenerator1(){
       this.NUMBER_OF_QUERY_SEGMENTS= 2000;
       this.noOfPQSS = 50;
   }

    public QueryGenerator1(int databases, int tables, int attributes, String distribution){
        this.NUMBER_OF_QUERY_SEGMENTS= 2000;
        this.noOfPQSS = 50;

        this.attributes = attributes;
        this.tables = tables;
        this.databases = databases;
        this.noOfSQS = (databases-1)*(tables-1)*(attributes+4);
        this.distribution = distribution;

    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public int getDatabases() {
        return databases;
    }

    public void setDatabases(int databases) {
        this.databases = databases;
    }

    public int getTables() {
        return tables;
    }

    public void setTables(int tables) {
        this.tables = tables;
    }

    public int getAttributes() {
        return attributes;
    }

    public void setAttributes(int attributes) {
        this.attributes = attributes;
    }



    public int getNUMBER_OF_QUERIES() {
        return NUMBER_OF_QUERIES;
    }

    public void setNUMBER_OF_QUERIES(int NUMBER_OF_QUERIES) {
        this.NUMBER_OF_QUERIES = NUMBER_OF_QUERIES;
    }

    /**
     * sqss are the subject query segments
     */
    private void  createSQSS(){
        String tempSQS = "";
        ArrayList<String> temp_sqss = new ArrayList<>();

        for(int database=1; database < databases+1; database++) { // for all 6 databases
            for(int table = 1; table < tables+1; table++){ // for 5 tables
                for (int attribute =1; attribute<attributes+1; attribute++){ // for all 15 attributes in each table
                    tempSQS = "at"+attribute+table+database+":t"+table+database+":d"+database+"";

                    if (!sqss.contains(tempSQS)) {
                        sqss.add(tempSQS);
                    }

                }

                // the following is to create multiple attributes
                tempSQS = "";

                for (int attribute =1; attribute<attributes+1; attribute++) { // for all 15 attributes in each table
                    Random randomIndex = new Random();
                    // Random noOfAttr = new Random();

                    Set checkRandom = new HashSet(5);
                    int tempRandomIndexSQS = randomIndex.nextInt(attributes);
                    int noOfAttr = new Random().nextInt(4);

                    //  checking for duplicates
                    for(int i=0; i < noOfAttr; i++){
                        while (checkRandom.contains(tempRandomIndexSQS)){
                            tempRandomIndexSQS = randomIndex.nextInt(attributes);
                        }

                        checkRandom.add(tempRandomIndexSQS);
                        tempSQS = tempSQS+"at"+ tempRandomIndexSQS+table+database+";";
                    }

                    tempRandomIndexSQS = randomIndex.nextInt(attributes);
                    while (checkRandom.contains(tempRandomIndexSQS)){
                        tempRandomIndexSQS = randomIndex.nextInt(attributes);
                    }

                    tempSQS = tempSQS+"at"+tempRandomIndexSQS+table+database+":t"+table+database+":d"+database;

                    if (!sqss.contains(tempSQS)) {
                        sqss.add(tempSQS);
                    }
                    tempSQS = "";
                }


            }//no of tables
        }//no of databases


        Collections.shuffle(sqss);


    }

    /**
     * pqss are predicate query segments that decide the number of rows
     */
    private void createPQSS( ){

        for (int p = 0; p < noOfPQSS ; p++) {

            String tempPQS = "";

            UniformIntegerDistribution uniform = new UniformIntegerDistribution(1, sqss.size()-1);

            String attr2 = "";
            String condition = "";

            int tempAttr = uniform.sample();

            while ((tempAttr == 0)) {
                tempAttr = uniform.sample();
            }

            attr2 = sqss.get(tempAttr);

            Random shouldHaveAttr2 = new Random();
            int OKattr2 = shouldHaveAttr2.nextInt(8);

            if (OKattr2 >= 2) {
                tempPQS = tempPQS + "," + attr2;
                condition = conditionArray[new Random().nextInt(8)];

                //cardinality is a random number generated to give a number
                long  cardinality = Math.round(Math.abs(new Random().nextGaussian() * 1000));


//                System.out.println("cardinality is "+cardinality);
                tempPQS = tempPQS + "," + condition+"-"+cardinality;
            }
            else{
                condition = conditionArray[new Random().nextInt(8)];
                long  cardinality = Math.round(Math.abs(new Random().nextGaussian()*1000));
                tempPQS = ","+ condition+"-"+cardinality;
            }


            pqss.add(tempPQS);// + ">");
        }

    }

    /**
     * This method creates query segments.
     * This method returns Arraylist of queries.
     * @param
     * @return
     */
    private ArrayList<String> createQuerySegments(){
            // add subquery sequences to sub-queries.
        //shuffle
        ArrayList<String> subQueries = null;
        return subQueries;

    }// end of query segments

    /**
     * The following method is a crucial one in selecting one sub-query from the distribution
     * @param
     * @return
     */
    private int getSampleFromDistribution(int segments){

       // System.out.println("segments  = "+segments);
        switch (distribution){
            case "Poisson":{
                PoissonDistribution poisson = new PoissonDistribution(segments/2);
             //   System.out.println("segments Poisson = "+segments);
                sample = poisson.sample();

                break;
            }
            case "Uniform":{
                UniformIntegerDistribution uniform = new UniformIntegerDistribution(1,segments-1);
             //  System.out.println("segments Uniform = "+segments);
                sample = uniform.sample();
                break;
            }
            case "Random":{
               // System.out.println("segments Random = "+segments);
                Random random = new Random();
                sample = random.nextInt(segments-1);

                break;
            }
            case "Fixed":{
                sample += 1;
                if (sample >= segments) sample = 1;
                break;
            }
            case "Exponential":{
                ExponentialDistribution exponential = new ExponentialDistribution(segments/4);
            //   System.out.println("segments Exponential = "+segments);
                sample = (int) exponential.sample();
                while (sample >= segments){
                    sample = (int) exponential.sample();
                }

                break;
            }
            default:break;
        }


        return sample;

    }

    /**
     * Following method is to return an execution operator
     * @return
     */
    private String getRoot(){
        String[] seq_parallel = {"&","_"};
        String root = seq_parallel[new Random().nextInt(2)];
        return root;
        }

    /**
     * Creating sub-queries (one sub-query)
     */
    public void createSubQueries()  {

        int querySegmentSample = 0;
        String tempSTR ="";
        ArrayList<String> queries = new ArrayList<>();
        createSQSS();
        createPQSS();

        for (int i = 0; i < NUMBER_OF_QUERY_SEGMENTS ; i++) {
            String query = "";

            ArrayList<String> tempListSQS = new ArrayList<>();

            int noOfSQSInQuery = new Random().nextInt(databases+1)+1; // This variable is to select more than one sqss.
            // This is to ensure we get data from multiple databases
            while (noOfSQSInQuery == 0) {
                noOfSQSInQuery = new Random().nextInt(databases+1)+1;    // upto no.of databases
            }

            int seed = 0;
            while (seed== 0){
                seed = new Random().nextInt(8);
            }

            PoissonDistribution poisson = new PoissonDistribution(noOfSQS / seed);
            UniformIntegerDistribution uid = new UniformIntegerDistribution(noOfSQS/(seed+1),noOfSQS/(seed));

            //adding SQS in the query
            for (int j = 0; j < noOfSQSInQuery; j++) {
                //  String tempSTR = sqss.get(poisson.sample());
                if (querySegmentSample> sqss.size()-1)
                    querySegmentSample = 0;
                else {
                    tempSTR = sqss.get(querySegmentSample);
                    querySegmentSample++;
                }
                if( !tempListSQS.contains(tempSTR))
                    tempListSQS.add(tempSTR);
                else j--;
            }


            for (int j = 0; j < noOfSQSInQuery; j++) {
                query = query+"<"+tempListSQS.get(j)+">";

            }
            //till here added sqs in a query
            int tempAttr = new Random().nextInt(noOfSQSInQuery);
            //to ensure that first attribute is taken from the chosen subject query segments
            while ((tempAttr == 0)) {
                if (noOfSQSInQuery == 1) tempAttr = 1;
                else tempAttr = new Random().nextInt(noOfSQSInQuery);
            }

            String attr1 = "";
            if (tempAttr == 1) attr1 = tempListSQS.get(0);
            else attr1 = tempListSQS.get(tempAttr);

            String attr2 =   attr1+"*";
            while(attr2.contains(attr1)) {    //this is to avoid repetition of attributes in the predicates

                PoissonDistribution poisson_PQS = new PoissonDistribution(pqss.size()/seed);
                int poisson_sample = poisson_PQS.sample();

                while (poisson_sample>= pqss.size())  {
                    poisson_sample = poisson_PQS.sample();
                }

                attr2 = pqss.get(poisson_sample);
            }

            if (attr2 == null){
                attr1 = attr1+","+conditionArray[new Random().nextInt(conditionArray.length)];
                long  cardinality = Math.round(Math.abs(new Random().nextGaussian()*1000));
                attr1 = attr1+","+conditionArray[new Random().nextInt(conditionArray.length)]+"-"+cardinality;

            }

            String str = "<" + attr1 + attr2+">";

            query = query+"#"+ str;

            queries.add(query);

        }
        this.sub_queries =  queries;
    } // end of query segments and hence query

    /**
     * Creating 2- sub-query sequences by creating subQueries
     */
    public void create2_SubQuerySequences(){

        //create 2-sub-query sequences
        //sample from subQueries

        for (int i = 0; i < 20; i++) {
            String subQuery1 = sub_queries.get(new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1));
            String subQuery2 = sub_queries.get(new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS -1));

            while(subQuery1.equals(subQuery2)){
                subQuery2 = sub_queries.get(new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1));
            }

            String subQuerySequence = "("+subQuery1+")" + getRoot()+"("+subQuery2 +")";
            if (!TwoSubQuery_Sequences.contains(subQuerySequence)) {
                TwoSubQuery_Sequences.add(subQuerySequence);
            }
            else i--;
        }

    }

    /**
     * Create 3- sub-query sequences
     */
    public void create3_SubQuerySequences(){
        //ArrayList<String> queries = new ArrayList<>(100);

        for(int i=1; i<20; i++) {

            String queryExpression = "";
            String root = getRoot();
            int treeType = new Random().nextInt(4);

            int sample_value1 = new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1);
            while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS) {
                sample_value1 = new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1);
            }

            int sample_value2 = new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1);
            // sample_value2 = 2520;
            while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == sample_value1) {
                sample_value2 = new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1);
            }

            int sample_value3 = new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1);
            // sample_value3 = 1720;
            while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == sample_value2 || sample_value3 == sample_value1) {
                sample_value3 = new Random().nextInt(NUMBER_OF_QUERY_SEGMENTS - 1);
            }

            switch (treeType) {
                case 1: {
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")"
                            + root + "(" + sub_queries.get(sample_value2) + ")" + root + "(" + sub_queries.get
                            (sample_value3) + ")";
                    break;
                }
                case 2: { // two levels. right subtree has children

                    String subRoot = getRoot();
                    queryExpression = "(" + sub_queries.get(sample_value1) + ")" + root;
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value2) + ")"
                            + subRoot + "(" +
                            sub_queries.get(sample_value3) + "))";
                    break;
                }

                case 3: {    // two levels, left subtree has children
                    queryExpression = "";
                    String subRoot = getRoot();
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value1) + ")"
                            + subRoot + "(" +
                            sub_queries.get(sample_value2) + "))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value3
                    ) + ")";
                    break;
                }
                default: {
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")"
                            + root + "(" + sub_queries.get(sample_value2) + ")" + root + "(" + sub_queries.get
                            (sample_value3) + ")";
                    break;
                }
            }

            ThreeSubQuery_Sequences.add( queryExpression);

        }
    }

    /**
     * following function is to generate 100 unique one node queries
     * @return
     */
    private ArrayList<String> getQueryExpression_oneNode(){
        int sample_value=   0;
        ArrayList<String> queries = new ArrayList<>(100);

        for(int i =1; i < 101; i++){
           // System.out.println("NUMBER_OF_QUERY_SEGMENTS ="+ NUMBER_OF_QUERY_SEGMENTS);
            sample_value = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);

            while (sample_value >= NUMBER_OF_QUERY_SEGMENTS)     {
                sample_value=    getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }
            String query =   "(" + sub_queries.get(sample_value) + ")";

            queries.add(query);
        }
        return queries;
    }

    /**
     * Following function is to generate query expressions two node of random type
     * @return
     */
    private ArrayList<String> getQueryExpressions_twoNode(){

        ArrayList<String> queries = new ArrayList<>(100);

        for(int i =1; i < 101; i++) {
            int sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS) {
                sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == sample_value2) {
                sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            queries.add("(" + sub_queries.get(sample_value1) + ")" + getRoot() + "(" +
                    sub_queries.get(sample_value2) + ")");
        }

        return queries;
    }

    /**
     * The following method generates three nodes random segments
     * @return
     */
    private ArrayList<String> getQueryExpressions_threeNode(){

        ArrayList<String> queries = new ArrayList<>(100);

        for(int i=1; i<101; i++) {

            String queryExpression = "";
            String root = getRoot();
            int treeType = new Random().nextInt(4);

            int sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS) {
                sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            // sample_value2 = 2520;
            while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == sample_value1) {
                sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value3 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            // sample_value3 = 1720;
            while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == sample_value2 || sample_value3 == sample_value1) {
                sample_value3 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            switch (treeType) {
                case 1: {
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")"
                            + root + "(" + sub_queries.get(sample_value2) + ")" + root + "(" + sub_queries.get
                            (sample_value3) + ")";
                    break;
                }
                case 2: { // two levels. right subtree has children

                    String subRoot = getRoot();
                    queryExpression = "(" + sub_queries.get(sample_value1) + ")" + root;
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value2) + ")"
                            + subRoot + "(" +
                            sub_queries.get(sample_value3) + "))";
                    break;
                }

                case 3: {    // two levels, left subtree has children
                    queryExpression = "";
                    String subRoot = getRoot();
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value1) + ")"
                            + subRoot + "(" +
                            sub_queries.get(sample_value2) + "))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value3
                    ) + ")";
                    break;
                }
                default: {
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")"
                            + root + "(" + sub_queries.get(sample_value2) + ")" + root + "(" + sub_queries.get
                            (sample_value3) + ")";
                    break;
                }
            }

            queries.add( queryExpression);

        }

        return queries;
    }

    /**
     * Following function generates four node queries with random segments
     * @return
     */
    private ArrayList<String>getQueryExpressions_fourNode(){

        ArrayList<String> queries = new ArrayList<>(100);

        for (int i = 1; i < 101; i++) {


            String queryExpression = "";
            String root = getRoot();
            int treeType = new Random().nextInt(5);

            int sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS) {
                sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == sample_value1) {
                sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value3 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == sample_value1 || sample_value3 == sample_value2) {
                sample_value3 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value4 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == sample_value1 || sample_value4 == sample_value2 || sample_value4 == sample_value3) {
                sample_value4 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            switch (treeType) {
                case 1: {
                    // complete binary tree , three levels
                    queryExpression = "";
                    String subRoot1 = getRoot();
                    String subRoot2 = getRoot();
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")"
                            + subRoot1 + "(" +
                            sub_queries.get(sample_value2) + ")";
                    queryExpression = queryExpression + root;

                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value3) + ")"
                            + subRoot1 + "(" +
                            sub_queries.get(sample_value4) + ")";


                    break;
                }
                case 2: {
                    //root node has three children. middle child has two children
                    queryExpression = "";
                    String subRoot = getRoot();
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")" + root;

                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value2) + ")"
                            + subRoot + "(" +
                            sub_queries.get(sample_value3) + "))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value4
                    ) + ")";
                    break;
                }

                case 3: {
                    //three levels. root has two children. Right child has three children
                    queryExpression = "";
                    String subRoot = getRoot();

                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")" + root;
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value2) + ")"
                            + subRoot + "(" + sub_queries.get(sample_value3) + ")" + subRoot + "(" + sub_queries.get
                            (sample_value4) + "))";

                    break;
                }

                case 4: {
                    //three levels. root has two children. Left child has three children
                    queryExpression = "";
                    String subRoot = getRoot();

                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value1) + ")"
                            + subRoot + "(" + sub_queries.get(sample_value2) + ")" + subRoot + "(" + sub_queries.get
                            (sample_value3) + "))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value4) +
                            ")";

                    break;
                }
                default: {
                    String subRoot1 = getRoot();
                    String subRoot2 = getRoot();
                    queryExpression = "";
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) + ")"
                            + subRoot1 + "(" +
                            sub_queries.get(sample_value2) + ")";
                    queryExpression = queryExpression + root;

                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value3) + ")"
                            + subRoot1 + "(" +
                            sub_queries.get(sample_value4) + ")";

                    break;
                }
            }

            queries.add(queryExpression);
        }
        return queries;

    }

    /**
     * The following method is to generate 100 unique five node queries
     * @return
     */
    private ArrayList<String> getQueryExpressions_fiveNode() {

        ArrayList<String> queries = new ArrayList<>(100);

        for (int i = 1; i < 101 ; i++) {


            String queryExpression = "";
            String root = getRoot();
            int treeType = new Random().nextInt(5);

            int sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS) {
                sample_value1 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == sample_value2) {
                sample_value2 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value3 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == sample_value3 || sample_value3 == sample_value1) {
                sample_value3 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value4 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == sample_value4 || sample_value2 == sample_value4 || sample_value3 == sample_value4) {
                sample_value4 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }

            int sample_value5 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == sample_value5 || sample_value2 == sample_value5 || sample_value3 == sample_value5 || sample_value4 == sample_value5) {
                sample_value5 = getSampleFromDistribution(NUMBER_OF_QUERY_SEGMENTS);
            }


            //   System.out.println("Five node index is "+treeType);

            switch (treeType) {
                case 1: {
                    //four levels. root has two children. Left child has three children. one of the children have two
                    // children.
                    queryExpression = "";
                    String subRoot1 = getRoot();
                    String subRoot2 = getRoot();
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value1) +
                            ")" + subRoot1;

                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value2) +
                            ")" + subRoot1;

                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value3) + ")" +
                            subRoot2 + "(" + sub_queries.get
                            (sample_value4) + ")))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value5) +
                            ")";

                    break;
                }

                case 2: {
                    // root has two children. left child has two children and right child has three.
                    // three level tree
                    String subRoot1 = getRoot();
                    String subRoot2 = getRoot();

                    queryExpression = "";

                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value1) + ")" +
                            subRoot1 + "(" + sub_queries.get
                            (sample_value2) + "))";

                    queryExpression = queryExpression + root;

                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value3) + ")"
                            + subRoot2 + "(" + sub_queries.get(sample_value4) + ")" + subRoot2 + "(" + sub_queries.get
                            (sample_value5) + "))";

                    break;
                }

                case 3: {
                    queryExpression = "";
                    String subRoot1 = getRoot();
                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) +
                            ")" + root;
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value2) + ")"
                            + subRoot1 + "(" + sub_queries.get(sample_value3) + ")" + subRoot1 + "(" +
                            sub_queries.get
                                    (sample_value4) + "))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value5) +
                            ")";

                    break;
                }

                case 4: {

                    queryExpression = "";
                    String subRoot1 = getRoot();
                    String subRoot2 = getRoot();

                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value1) +
                            ")" + root;
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value2) + ")" +
                            "" + subRoot1 + "((" + sub_queries.get(sample_value3) + ")" + subRoot2 + "" +
                            "(" + sub_queries.get(sample_value4) + "))" + subRoot1 + "(" + sub_queries.get
                            (sample_value5) + "))";

                    break;
                }
                default: {
                    queryExpression = "";
                    String subRoot1 = getRoot();
                    String subRoot2 = getRoot();
                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value1) +
                            ")" + subRoot1;

                    queryExpression = queryExpression + "(" + sub_queries.get(sample_value2) +
                            ")" + subRoot1;

                    queryExpression = queryExpression + "((" + sub_queries.get(sample_value3) + ")" +
                            subRoot2 + "(" + sub_queries.get
                            (sample_value4) + ")))";

                    queryExpression = queryExpression + root + "(" + sub_queries.get(sample_value5) +
                            ")";
                    break;
                }
            }

            queries.add(queryExpression);
        }
        return queries;
    }

    /**
     * This is to generate normal queries
     * six databases, each database has 5 to 9 tables, each table has 8 to 16 attributes
     * @throws IOException
     */
    public void generateQueryRepetitions() throws IOException {

        final String[] distributionArray = {"Exponential","Fixed","Poisson","Random","Uniform"};
        //Distributed Environment settings
        int databases =2; // upto 6
        int tables = 5; //upto 10
        int attributes = 8; //upto 15

        int queryNo=0;

        ArrayList<String> unique_100_one_Node_querylist ;
        ArrayList<String> unique_100_two_Node_querylist ;
        ArrayList<String> unique_100_three_Node_querylist;
        ArrayList<String> unique_100_four_Node_querylist ;
        ArrayList<String> unique_100_five_Node_querylist ;


        for (databases=2; databases <6; databases+=3) {
            for (tables = 5; tables < 10; tables += 3) {
                for (attributes = 8; attributes < 17; attributes += 8) {

                    for( String distribution:distributionArray) {

                        this.setDatabases(databases);
                        this.setTables(tables);
                        this.setAttributes(attributes);
                        this.setDistribution(distribution);
                        this.noOfSQS = (databases-1)*(tables-1)*(attributes+4);

                        createQuerySegments();
                        create2_SubQuerySequences();
                        create3_SubQuerySequences();

                        unique_100_one_Node_querylist = getQueryExpression_oneNode();
                        unique_100_two_Node_querylist = getQueryExpressions_twoNode();
                        unique_100_three_Node_querylist = getQueryExpressions_threeNode();
                        unique_100_four_Node_querylist = getQueryExpressions_fourNode();
                        unique_100_five_Node_querylist = getQueryExpressions_fiveNode();

                            for (int numberOfQueries = 2500; numberOfQueries < 20001; numberOfQueries += 2500) {


                                BufferedWriter out1 = null;
                                int[][] queryNumbers_distinct = new int[101][1];
                                BufferedWriter out_write = null;

                                try {
                                    out1 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryInput_"+numberOfQueries+"//one_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution));

                                    for (int i = 0; i < numberOfQueries; i++) {
                                        queryNo = getSampleFromDistribution(100);
                                        String sampleQuery = unique_100_one_Node_querylist.get(queryNo);
                                        int j=1;
                                        for (; j < 101; j++) {
                                            if ( queryNo == j){
                                                queryNumbers_distinct[j][0]++;
                                                break;
                                            }
                                        }
                                        out1.write(sampleQuery);
                                        out1.write("\n");


                                    }// for loop for each list of queries
                                    out1.close();
                                }catch (IOException e) {
                                        e.printStackTrace();
                                }


                            out_write = new BufferedWriter(new FileWriter("//home//santhilata//Desktop//QueryOutput//QueryOutput_"+numberOfQueries+"//one_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution+".csv"));
                            out_write.append("QueryNumber,repetitions\n");
                            for (int i = 1; i < 101; i++) {
                                //System.out.println(i +" "+queryNumbers_distinct[i][0]);
                                out_write.append(i+","+queryNumbers_distinct[i][0]+"\n");

                            }

/*
                                BufferedWriter out2 = null;
                                try {
                                    out2 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryInput_"+numberOfQueries+"//two_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution));

                                    for (int i = 0; i < numberOfQueries; i++) {
                                        queryNo = getSampleFromDistribution(100);
                                        String sampleQuery = unique_100_two_Node_querylist.get(queryNo);

                                        int j=1;
                                        for (; j < 101; j++) {
                                            if ( queryNo == j){
                                                queryNumbers_distinct[j][0]++;
                                                break;
                                            }
                                        }
                                        out2.write(sampleQuery);
                                        out2.write("\n");



                                    }// for loop for each list of queries
                                    out2.close();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                                out_write = new BufferedWriter(new FileWriter("//home//santhilata//Desktop//QueryOutput//QueryOutput_"+numberOfQueries+"//two_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution+".csv"));
                            out_write.append("QueryNumber,repetitions\n");
                            for (int i = 1; i < 101; i++) {
                                //System.out.println(i +" "+queryNumbers_distinct[i][0]);
                                out_write.append(i+","+queryNumbers_distinct[i][0]+"\n");

                            }


                                BufferedWriter out3 = null;
                                try {
                                    out3 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryInput_"+numberOfQueries+"//three_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution));

                                    for (int i = 0; i < numberOfQueries; i++) {
                                        queryNo = getSampleFromDistribution(100);
                                        String sampleQuery = unique_100_three_Node_querylist.get(queryNo);

                                        int j=1;
                                        for (; j < 101; j++) {
                                            if ( queryNo == j){
                                                queryNumbers_distinct[j][0]++;
                                                break;
                                            }
                                        }

                                        out3.write(sampleQuery);
                                        out3.write("\n");


                                    }// for loop for each list of queries
                                    out3.close();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }

                                 out_write = new BufferedWriter(new FileWriter("//home//santhilata//Desktop//QueryOutput//QueryOutput_"+numberOfQueries+"//three_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution+".csv"));
                            out_write.append("QueryNumber,repetitions\n");
                            for (int i = 1; i < 101; i++) {
                                //System.out.println(i +" "+queryNumbers_distinct[i][0]);
                                out_write.append(i+","+queryNumbers_distinct[i][0]+"\n");

                            }


                                BufferedWriter out4 = null;
                                try {
                                    out4 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryInput_"+numberOfQueries+"//four_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution));

                                    for (int i = 0; i < numberOfQueries; i++) {
                                        queryNo = getSampleFromDistribution(100);
                                        String sampleQuery = unique_100_four_Node_querylist.get(queryNo);

                                        int j=1;
                                        for (; j < 101; j++) {
                                            if ( queryNo == j){
                                                queryNumbers_distinct[j][0]++;
                                                break;
                                            }
                                        }

                                        out4.write(sampleQuery);
                                        out4.write("\n");

                                    }// for loop for each list of queries
                                    out4.close();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }


                            out_write = new BufferedWriter(new FileWriter("//home//santhilata//Desktop//QueryOutput//QueryOutput_"+numberOfQueries+"//four_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution+".csv"));
                            out_write.append("QueryNumber,repetitions\n");
                            for (int i = 1; i < 101; i++) {
                                //System.out.println(i +" "+queryNumbers_distinct[i][0]);
                                out_write.append(i+","+queryNumbers_distinct[i][0]+"\n");

                            }

                                BufferedWriter out5 = null;
                                try {
                                    out5 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryInput_"+numberOfQueries+"//five_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution));

                                    for (int i = 0; i < numberOfQueries; i++) {
                                        queryNo = getSampleFromDistribution(100);
                                        String sampleQuery = unique_100_five_Node_querylist.get(queryNo);

                                        int j=1;
                                        for (; j < 101; j++) {
                                            if ( queryNo == j){
                                                queryNumbers_distinct[j][0]++;
                                                break;
                                            }
                                        }

                                        out5.write(sampleQuery);
                                        out5.write("\n");


                                    }// for loop for each list of queries
                                    out5.close();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }

                                out_write = new BufferedWriter(new FileWriter("//home//santhilata//Desktop//QueryOutput//QueryOutput_"+numberOfQueries+"//five_node_d" +databases+"_t"+tables+"_a"+attributes+"_"+ numberOfQueries + "_" + distribution+".csv"));
                                out_write.append("QueryNumber,repetitions\n");
                                for (int i = 1; i < 101; i++) {
                                    //System.out.println(i +" "+queryNumbers_distinct[i][0]);
                                    out_write.append(i+","+queryNumbers_distinct[i][0]+"\n");

                                }

*/
                                out_write.close();

                            }// number of queries


                    }// distribution

                }//attributes
            }//tables
        }//databases

    }//method

    /**
     * Distribution of sub-queries within queryset
     */
    public void generateSubQueryRepetitions(){

    }

    /**
     * generate specific query patterns
     * @param traceRepetition - This is the distribution for repeating queries
     * @param pc_Repetition - percentage repetition of the sequence
     * @param sequence - # of sub-query sequence you want (2 or 3)
     * @param noOfQueries
     */
    public void generateSpecificQuerySets( String traceRepetition, int pc_Repetition, int sequence, int noOfQueries){

    }

    /**
     * Generating slow changing query traces
     */
    public void generateSlowChangingQuerySets(int  pc_Repetition, int sequence, int noOfQueries){
        // take 100 distinct queries.
        // generate adjacent queries.
        //

    }

    /**
     * following piece of code is to check how many distinct queries that are present in the list
     * It should present only single query in this case. Count should be 3000
     */
    public void countQueries() throws IOException {

        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];
        ArrayList<String> queryList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(".//src//main//java//project//QueryEnvironment//QueryModelling_Input//QueryInput_20000//one_node_d2_t8_a16_20000_Exponential"))){

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                queryList.add(sCurrentLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String query:queryList){
            boolean present=false;
            int z =0;
            while (z <= independentQuery){
                if (query.equals(distinctQueries[z].getQuery())) {
                    distinctQueries[z].addCount(1);
                    present = true;
                    break;
                }
                else {
                    z++;
                }
                // System.out.println("z = "+z);
            }
            if (!present){
                independentQuery++;
                distinctQueries[independentQuery] = new Query_Count(query,queryList.indexOf(query));

            }
        }

        String fileOut = "//home//santhilata//Desktop//QueryOutput//CountQueries//one_node_d2_t8_a16_20000_Exponential.csv";


        File outFile11 = new File(fileOut);
        FileWriter writer1 = new FileWriter(outFile11);

        writer1.flush();
        writer1.append("QueryNumber,Repetitions");
        writer1.append('\n');

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>0) {
                System.out.println(distinctQueries[i].getIndex()+" "+distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                writer1.append(distinctQueries[i].getIndex()+","+ distinctQueries[i].getCount()+"\n");
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);
        writer1.close();



    }

    /**
     * The following class is an internal class,
     * used for counting unique queries in the distribution.
     */
    class Query_Count{
        String query;
        int count=0;
       int index =0;

        public Query_Count(String query, int index) {
            this.query = query;
            count = 1;
            this.index = index;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void addCount(int i){
            this.count += i;
        }
    }


    /**

     //Settings for query inter arrival time
     double POISSON_MEAN_TIME = 15;
     int UNIFORM_UPPER_TIME = 10;
     int UNIFORM_LOWER_TIME = 2;
     double EXPONENTIAL_MEAN_TIME = 15;
     //settings for query repetition
     int MAXIMUM_OBSERVATION_TIME_UNIT = 3600; // One time epoch of observation is taken to be one hour.

     double POISSON_MEAN_QUERY = 18;
     int UNIFORM_LOWER_QUERY = 1;
     double EXPONENTIAL_MEAN_QUERY = 4;

     */


    public void myPoisson(){
       /* init:
        Let L ← exp(−λ), k ← 0 and p ← 1.
        do:
        k ← k + 1.
        Generate uniform random number u in [0,1] and let p ← p × u.
        while p > L.
        return k − 1.
        */

        for (int lambda = 0; lambda < 30; lambda++) {
            double L = Math.exp((-1)*lambda);
            int k=0;
            double p = 1.0;

            while (p> L){
                k = k+1;
                int u = new UniformIntegerDistribution(0,1).sample();
                p = p*u;
            }
            System.out.println(k-1);
        }
    }
    public static void main(String[] args) throws IOException {

        QueryGenerator1 qg = new QueryGenerator1();
     //  qg.generateQueryRepetitions();// for full query repetitions
     //  qg.countQueries();



        System.out.println("done");

        int[] values = new int[2000];
        int[][] repetitions = new int[20][1];
        PoissonDistribution pd = new PoissonDistribution(100.0,20);

/*

        for (int i = 0; i < 2000; i++) {
           // values[i] = new PoissonDistribution(50.0).sample();
            values[i] = pd.sample();
           // values[i] = (int) new ExponentialDistribution(50).sample();
        }

        for (int i = 0; i < 2000 ; i++) {
            if ((values[i]>0) && (values[i]<=10)) repetitions[0][0]++;
            if ((values[i]>10) && (values[i]<=20)) repetitions[1][0]++;
            if ((values[i]>20) && (values[i]<=30)) repetitions[2][0]++;
            if ((values[i]>30) && (values[i]<=40)) repetitions[3][0]++;
            if ((values[i]>40) && (values[i]<=50)) repetitions[4][0]++;
            if ((values[i]>50) && (values[i]<=60)) repetitions[5][0]++;
            if ((values[i]>60) && (values[i]<=70)) repetitions[6][0]++;
            if ((values[i]>70) && (values[i]<=80)) repetitions[7][0]++;
            if ((values[i]>80) && (values[i]<=90)) repetitions[8][0]++;
            if ((values[i]>90) && (values[i]<=100)) repetitions[9][0]++;
            if ((values[i]>100) && (values[i]<=110)) repetitions[10][0]++;
            if ((values[i]>110) && (values[i]<=120)) repetitions[11][0]++;
            if ((values[i]>120) && (values[i]<=130)) repetitions[12][0]++;
            if ((values[i]>130) && (values[i]<=140)) repetitions[13][0]++;
            if ((values[i]>140) && (values[i]<=150)) repetitions[14][0]++;
            if ((values[i]>150) && (values[i]<=160)) repetitions[15][0]++;
            if ((values[i]>160) && (values[i]<=170)) repetitions[16][0]++;
            if ((values[i]>170) && (values[i]<=180)) repetitions[17][0]++;
            if ((values[i]>180) && (values[i]<=190)) repetitions[18][0]++;
            if ((values[i]>190) && (values[i]<=200)) repetitions[19][0]++;

        }

        for (int i = 0; i < 20; i++) {
            System.out.println(i*10 + " "+repetitions[i][0]);
        }

        System.out.println("Numerical Variance = "+pd.getNumericalVariance()+" "+pd.getNumericalMean()+" "+ pd.getMean());

*/

        qg.myPoisson();

    }// main method


} // class
