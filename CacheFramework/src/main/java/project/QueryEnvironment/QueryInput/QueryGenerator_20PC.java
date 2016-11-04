package project.QueryEnvironment.QueryInput;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by santhilata on 06/05/15.
 * cardinality of one data table is a random number(1-15) multiplied by a factor 1000
 * creating synthetic queries with 20Pc overlap
 */
public class QueryGenerator_20PC {

    private static  final String[] conditionArray = {"eq","gt","lt","gte","lte","true","false","other",
            "between"};

    private static final int databases = 2; //6
    private static final int tables = 5;
    private static final int attributes = 10; //15
    private int noOfSQS = (databases-1)*(tables-1)*(attributes+4);
    private static final int noOfPQSS = 50;

    //SETTINGS FOR QUERY INTER ARRIVAL TIME
    public static final double POISSON_MEAN_TIME = 15;
    public static final int UNIFORM_UPPER_TIME = 10;
    public static final int UNIFORM_LOWER_TIME = 2;
    public static final double EXPONENTIAL_MEAN_TIME = 15;

    //settings for query repetition
    public static final int MAXIMUM_OBSERVATION_TIME_UNIT = 3600; // One time epoch of observation is taken to be one hour.
    public static final int NUMBER_OF_QUERIES = 10000;
    public static final int NUMBER_OF_QUERY_SEGMENTS= 2000;
    public static final double POISSON_MEAN_QUERY = 18;
    public static final int UNIFORM_UPPER_QUERY =15;
    public static final int UNIFORM_LOWER_QUERY = 1;
    public  static final double EXPONENTIAL_MEAN_QUERY = 4;

    ArrayList<String> sqss = new ArrayList<>();
    ArrayList<String> pqss = new ArrayList<>(noOfPQSS);

    int sample=0;

    int height = 3;
    int childHeight =1;
    String distribution="Fixed"; //"Random";
    int noofQSPs=5;
    ArrayList<String> queries = createQuerySegments(NUMBER_OF_QUERY_SEGMENTS);
    //    public static final String BACKUP_FILE = "" +
//            ".//src//main//java//project//QueryEnvironment//QueryInput//QueryGenerator_output";
    public static final String BACKUP_FILE = "" +
            ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions2";


    public QueryGenerator_20PC(){

    }

    /**
     * sqss are the subject query segments
     */
    private void createSQSS(){
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

                    Set checkRandom = new HashSet<Integer>(5);
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
     * This method creates query segments. Each query segment consists of one sub-query
     * @param numberOfQuerySegments
     * @return
     */
    private ArrayList<String> createQuerySegments(int numberOfQuerySegments){
        int querySegmentSample = 0;
        String tempSTR ="";
        ArrayList<String> queries = new ArrayList<>();
        createSQSS();
        createPQSS();

        for (int i = 0; i < numberOfQuerySegments ; i++) {
            String query = "";

            ArrayList<String> tempListSQS = new ArrayList<>();

            int noOfSQSInQuery = 0; // This variable is to select more than one sqss. This is to ensure we get data from multiple databases
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
                    //   System.out.println(tempSTR +"at 178");
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
                // System.out.println(attr2 +" "+attr1);
                PoissonDistribution poisson_PQS = new PoissonDistribution(pqss.size()/seed);
                int poisson_sample = poisson_PQS.sample();

                while (poisson_sample>= pqss.size())  {
                    poisson_sample = poisson_PQS.sample();
                }

                attr2 = pqss.get(poisson_sample);
                //  System.out.println("new attr2 "+attr2);
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
        return  queries;
    }

    private int getSampleFromDistribution(String distribution){


        switch (distribution){
            case "Poisson":{
                PoissonDistribution poisson = new PoissonDistribution(NUMBER_OF_QUERY_SEGMENTS/2.5);
                sample = poisson.sample();

                break;
            }
            case "Uniform":{
                UniformIntegerDistribution uniform = new UniformIntegerDistribution(1,NUMBER_OF_QUERY_SEGMENTS-1);
                sample = uniform.sample();

                break;
            }
            case "Random":{
                Random random = new Random();
                sample = random.nextInt(NUMBER_OF_QUERIES-1);

                break;
            }
            case "Fixed":{
                sample += 1;
                if (sample >= NUMBER_OF_QUERY_SEGMENTS) sample = 1;
                break;
            }
            case "Exponential":{
                ExponentialDistribution exponential = new ExponentialDistribution(NUMBER_OF_QUERY_SEGMENTS/3);
                sample = (int)exponential.sample();
                while (sample >=NUMBER_OF_QUERY_SEGMENTS){
                    sample = (int) exponential.sample();
                }
                break;
            }
            default:break;
        }


        return sample;

    }

    private String getRoot(){
        String[] seq_parallel = {"&","_"};
        String root = seq_parallel[new Random().nextInt(2)];
        return root;
    }

    private int childnodeCounter=0;
    private String getQueryExpression(String queryExpression){

        int children = new Random().nextInt(4);
        childnodeCounter = childnodeCounter+children;

        if( childnodeCounter> noofQSPs) { children = noofQSPs - (childnodeCounter-children)  ; }


        childHeight++;
        System.out.println("child height " + childHeight);
        if ((childHeight > height) && (queryExpression.equals(""))) {
            return "(" + queries.get(getSampleFromDistribution(distribution)) + ")";
        }

        if (children == 1 || children == 0) {
            queryExpression = queryExpression + "(" + queries.get(getSampleFromDistribution(distribution)) + ")";
            return queryExpression;
        } else {
            String root = getRoot();

            if (children == 2) {
                return "(" + getQueryExpression(queryExpression) + root + getQueryExpression(queryExpression) + ")";
            } else if (children == 3) {
                return ("(" + getQueryExpression(queryExpression) + root + getQueryExpression(queryExpression)
                        + root + getQueryExpression(queryExpression) + ")");
            }
        }


        return queryExpression + "(" + queries.get(getSampleFromDistribution(distribution)) + ")";

    }

    /**
     * following function is to generate one node query expressions
     * @return
     */
    private String getQueryExpression_oneNode(){
        int sample_value=    getSampleFromDistribution(distribution);
        while (sample_value >= NUMBER_OF_QUERY_SEGMENTS)     {
            sample_value=    getSampleFromDistribution(distribution);
        }
        return  "" + "(" + queries.get(sample_value) + ")";
    }

    /**
     * The following function generates queries with selected single segments repeated 40% of the time
     * One particular query is shuffled through the query input to get repeated 40% of the time
     */
    private void getQueryExpressions_oneNode_40PCOverlap(){
        int fixed_segment1 = 0;
        int number_of_queries =10000;

        ArrayList<String> queryList = new ArrayList<>();

        try{
            BufferedWriter out1 = new BufferedWriter(new FileWriter( ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions1_40PCOverlap_10000"));
            int sample_value1=0;
            fixed_segment1 = getSampleFromDistribution(distribution);
            while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS )     {
                fixed_segment1=    getSampleFromDistribution(distribution);
            }

            for (int i = 0; i < 0.6*number_of_queries; i++) {
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                String q = "("+queries.get(sample_value1)+")";
                queryList.add(q);
            }

            for (int i = 0; i < 0.4*number_of_queries ; i++) {
                String q = "("+queries.get(fixed_segment1)+")";
                queryList.add(q);
            }

            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The following function generates queries with selected single segments repeated 60% of the time
     * One particular query is shuffled through the query input to get repeated 40% of the time
     */
    private void getQueryExpressions_oneNode_60PCOverlap(){
        int fixed_segment1 = 0;
        int number_of_queries =10000;
//        int number_of_queries =1000;
        ArrayList<String> queryList = new ArrayList<>();

        try{
            BufferedWriter out1 = new BufferedWriter(new FileWriter( ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions1_60PCOverlap_10000"));
            int sample_value1=0;
            fixed_segment1 = getSampleFromDistribution(distribution);
            while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS )     {
                fixed_segment1 =    getSampleFromDistribution(distribution);
            }

            for (int i = 0; i < 0.4*number_of_queries; i++) {
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                String q = "("+queries.get(sample_value1)+")";
                queryList.add(q);
            }

            for (int i = 0; i < 0.6*number_of_queries ; i++) {
                String q = "("+queries.get(fixed_segment1)+")";
                queryList.add(q);
            }

            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Following function is to generate query expressions two node of random type
     * @return
     */
    private String getQueryExpressions_twoNode(){
        int sample_value1=    getSampleFromDistribution(distribution);
        while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS)     {
            sample_value1=    getSampleFromDistribution(distribution);
        }

        int sample_value2=    getSampleFromDistribution(distribution);
        while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2)     {
            sample_value2=    getSampleFromDistribution(distribution);
        }

        // System.out.println("sample value 1= "+sample_value1+ " sample value2 = "+sample_value2);

        return  "(" + queries.get(sample_value1)+")" +getRoot()+ "("+
                queries.get(sample_value2)+")";
    }

    /**
     * The following function generates queries with selected 2 query segments repeated 40% of the time
     * First 10% + 10% of the queries contain either of the segments alone, 50% of the queries contain neither
     * and 30% contain both- (full query overlap = 30% and 20%)
     * @return
     */
    private void getQueryExpressions_twoNode_40PCOverlap(){
        int fixed_segment1 = 0;
        int fixed_segment2 = 0;
        int number_of_queries =10000;

        ArrayList<String> queryList = new ArrayList<>();

        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter( "" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions2_40PCOverlap_10000"));

            int sample_value1=0;
            int sample_value2=0;

            fixed_segment1 = getSampleFromDistribution(distribution);
            while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS )     {
                fixed_segment1 =    getSampleFromDistribution(distribution);
            }
            fixed_segment2 = getSampleFromDistribution(distribution);
            while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment2 == fixed_segment1)     {
                fixed_segment2 =    getSampleFromDistribution(distribution);
            }

            String q = "";

            for (int i=0; i<0.2*number_of_queries; i++){
                sample_value2=    getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == fixed_segment1 || sample_value2==fixed_segment2 || sample_value1==sample_value2 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                q = "(" + queries.get(sample_value2)+")" +getRoot()+ "("+
                        queries.get(fixed_segment1)+")";

                queryList.add(q);
            }

            for (int i=0; i<0.2*number_of_queries; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1>= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2 ||sample_value1 ==fixed_segment1 || sample_value1==fixed_segment2)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                q = "(" + queries.get(fixed_segment2)+")" +getRoot()+ "("+
                        queries.get(sample_value1)+")";
                queryList.add(q);
            }

            for (int i=0; i<0.2*number_of_queries; i++){
                q = "(" + queries.get(fixed_segment1)+")" +"&"+ "("+
                        queries.get(fixed_segment2)+")";
                queryList.add(q);
            }
            for (int i=0; i<0.2*number_of_queries; i++){
                q = "(" + queries.get(fixed_segment2)+")" +"&"+ "("+
                        queries.get(fixed_segment1)+")";
                queryList.add(q);
            }

            for (int i=0; i<0.4*number_of_queries; i++){

                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 ==fixed_segment1 || sample_value1==fixed_segment2)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2 ||sample_value2 ==fixed_segment1 || sample_value2==fixed_segment2)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                q = "(" + queries.get(sample_value1)+")" +getRoot()+ "("+
                        queries.get(sample_value2)+")";
                queryList.add(q);
            }

            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * foloowing piece of code is to check how many distinct queries oare present in the list
         * It should present only single query in this case. Count should be 3000
         */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);


    }

    /**
     * The following function generates queries with selected 2 query segments repeated 60% of the time
     * 30% + 30% of the queries contain either of the segments alone, 10% of the queries contain neither
     * and 30% contain both - full query overlap= 30% and 20%
     * @return
     */
    private void getQueryExpressions_twoNode_60PCOverlap(){
        int fixed_segment1 = 0;
        int fixed_segment2 = 0;
        int number_of_queries =10000;

        ArrayList<String> queryList = new ArrayList<>();
        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter( "" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions2_60PCOverlap_10000"));
            int sample_value1=0;
            int sample_value2=0;
            String q = "";

            fixed_segment1 = getSampleFromDistribution(distribution);
            while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS )     {
                fixed_segment1=    getSampleFromDistribution(distribution);
            }

            fixed_segment2 = getSampleFromDistribution(distribution);
            while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment2 == fixed_segment1 )     {
                fixed_segment2=    getSampleFromDistribution(distribution);
            }

            for (int i=0; i<0.4*number_of_queries; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2 || sample_value1 == fixed_segment1 || sample_value1==fixed_segment2)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                q = "(" + queries.get(sample_value1)+")" +getRoot()+ "("+
                        queries.get(fixed_segment1)+")";

                queryList.add(q);
            }

            for (int i=0; i<0.4*number_of_queries; i++){
                sample_value2=    getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2 ||sample_value2 ==fixed_segment1 || sample_value2==fixed_segment2)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                q = "(" + queries.get(fixed_segment2)+")" +getRoot()+ "("+
                        queries.get(sample_value2)+")";
                queryList.add(q);
            }

            for (int i=0; i<0.1*number_of_queries; i++){
                q = "(" + queries.get(fixed_segment1)+")" +"&"+ "("+
                        queries.get(fixed_segment2)+")";
                queryList.add(q);
            }
            for (int i=0; i<0.1*number_of_queries; i++){
                q = "(" + queries.get(fixed_segment2)+")" +"&"+ "("+
                        queries.get(fixed_segment1)+")";
                queryList.add(q);
            }

            for (int i=0; i<0.0*number_of_queries; i++){

                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 ==fixed_segment1 || sample_value1==fixed_segment2)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2 ||sample_value2 ==fixed_segment1 || sample_value2==fixed_segment2)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                q = "(" + queries.get(sample_value1)+")" +getRoot()+ "("+
                        queries.get(sample_value2)+")";
                queryList.add(q);
            }

            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * foloowing piece of code is to check how many distinct queries oare present in the list
         * It should present only single query in this case. Count should be 3000
         */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);


    }

    /**
     * The following method generates three nodes random segments
     * @return
     */
    private String getQueryExpressions_threeNode(){

        String queryExpression="";
        String root = getRoot();
        int treeType = new Random().nextInt(4);

        int sample_value1=    getSampleFromDistribution(distribution);
        while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS )     {
            sample_value1=    getSampleFromDistribution(distribution);
        }

        int sample_value2=    getSampleFromDistribution(distribution);
        // sample_value2 = 2520;
        while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==sample_value1 )     {
            sample_value2=    getSampleFromDistribution(distribution);
        }

        int sample_value3=    getSampleFromDistribution(distribution);
        // sample_value3 = 1720;
        while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==sample_value2 || sample_value3==sample_value1 )     {
            sample_value3=    getSampleFromDistribution(distribution);
        }

        switch (treeType){
            case 1:{
                queryExpression = queryExpression + "(" + queries.get(sample_value1)+")"
                        +root+  "("+queries.get(sample_value2)+")" + root +"("+ queries.get
                        (sample_value3) + ")";
                break;
            }
            case 2:{ // two levels. right subtree has children

                String subRoot = getRoot();
                queryExpression = "(" + queries.get(sample_value1) + ")"+root;
                queryExpression = queryExpression + "((" + queries.get(sample_value2)+")"
                        +subRoot+"("+
                        queries.get(sample_value3)+"))";
                break;
            }

            case 3:{    // two levels, left subtree has children
                queryExpression = "";
                String subRoot = getRoot();
                queryExpression = queryExpression + "((" + queries.get(sample_value1)+")"
                        +subRoot+"("+
                        queries.get(sample_value2)+"))";

                queryExpression = queryExpression +root+"(" + queries.get(sample_value3
                ) + ")" ;
                break;
            }
            default:{
                queryExpression = queryExpression + "(" + queries.get(sample_value1)+")"
                        +root+  "("+queries.get(sample_value2)+")" + root +"("+ queries.get
                        (sample_value3) + ")";
                break;
            }
        }

        return queryExpression;

    }

    /**
     * The following function generates 10000 queries with each of the segment appear in 40% of the time
     * full query overlap = 30% and 20%
     */
    private void getQueryExpressions_threeNode_40PCOverlap(){

        String queryExpression="";
        String root = getRoot();

        int totalQ = 0;

        int fixed_segment1 = getSampleFromDistribution(distribution);
        while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS  )     {
            fixed_segment1=    getSampleFromDistribution(distribution);
        }
        int fixed_segment2 = getSampleFromDistribution(distribution);
        while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment2 == fixed_segment1 )     {
            fixed_segment2=    getSampleFromDistribution(distribution);
        }
        int fixed_segment3 = getSampleFromDistribution(distribution);
        while (fixed_segment3 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment3 == fixed_segment1 || fixed_segment3 == fixed_segment2)     {
            fixed_segment3=    getSampleFromDistribution(distribution);
        }

        int number_of_queries =10000;
        ArrayList<String> queryList = new ArrayList<>();

        try {
//            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
//                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
//                    "//QueryExpressions3_40PCOverlap_10000"));
            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions3_40PCOverlap_10000"));

            int querySegment[][] = new int[10000][3];

            int sample_value1 = 0;
            int sample_value2 = 0;
            int sample_value3 =0;
            String q = "";

            int i = 0;
            //    double limit = 0.0333*number_of_queries;
            double limit = 0.067*number_of_queries;
            System.out.println("limit 1 = "+limit);

            for(; i < limit; i++){
                sample_value1 = fixed_segment1;
                sample_value3 = fixed_segment3;

                sample_value2=    getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;

            }


//            limit = 0.0333*number_of_queries+ i;
            limit = 0.067*number_of_queries+i;
            System.out.println("limit 2 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }



//            limit = 0.0333*number_of_queries+ i; // for the 5% of queries
            limit = 0.067*number_of_queries+i;
            System.out.println("limit 3 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }




//            limit = 0.0333*number_of_queries+ i;
            limit = 0.067*number_of_queries+i;
            System.out.println("limit 4 ="+limit);


            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2==fixed_segment3 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3==fixed_segment3)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }




//            limit = 0.0333*number_of_queries+ i; // for the next hundred queries
            limit = 0.067*number_of_queries+i;
            System.out.println("limit 5 ="+limit);


            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment1 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }



//            limit = 0.0333*number_of_queries+ i; // for the next hundred queries
            limit = 0.067*number_of_queries+i;
            System.out.println("limit 6 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment1 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }


//            limit = number_of_queries-(3000) ;
            limit = number_of_queries-(2000) ;
            System.out.println("limit 7 ="+limit);

            // for the rest of the queries
            for(; i <limit; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3=    getSampleFromDistribution(distribution);

                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }

            System.out.println("final limit "+ limit);


            for (int j = 0; j < i; j++) {
                int treeType = new Random().nextInt(4);
                queryExpression = "";
                switch (treeType){
                    case 1:{
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +root+  "("+queries.get(querySegment[j][1])+")" + root +"("+ queries.get
                                (querySegment[j][2]) + ")";
                        break;
                    }
                    case 2:{ // two levels. right subtree has children

                        String subRoot = getRoot();
                        queryExpression = "(" + queries.get(querySegment[j][0]) + ")"+root;
                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][1])+")"
                                +subRoot+"("+
                                queries.get(querySegment[j][2])+"))";
                        break;
                    }

                    case 3:{    // two levels, left subtree has children
                        queryExpression = "";
                        String subRoot = getRoot();
                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][0])+")"
                                +subRoot+"("+
                                queries.get(querySegment[j][1])+"))";

                        queryExpression = queryExpression +root+"(" + queries.get(querySegment[j][2]
                        ) + ")" ;
                        break;
                    }
                    default:{
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +root+  "("+queries.get(querySegment[j][1])+")" + root +"("+ queries.get
                                (querySegment[j][2]) + ")";
                        break;
                    }
                }



                queryList.add(queryExpression);

                // System.out.println(j + " " + treeType +"____"+queryExpression);

            }

            // All the 30% fixed queries

//            limit = 0.3*number_of_queries+ i;
            limit = 0.2*number_of_queries+ i;
            System.out.println("limit fixed queries ="+limit);

            for(; i < limit; i++){

                querySegment[i][0] = fixed_segment1;
                querySegment[i][1] = fixed_segment2;
                querySegment[i][2] = fixed_segment3;

                root = "&";
                queryExpression = "";
                String subRoot = "&";
                queryExpression = queryExpression + "((" + queries.get(querySegment[i][0])+")"
                        +subRoot+"("+
                        queries.get(querySegment[i][1])+"))";

                queryExpression = queryExpression +root+"(" + queries.get(querySegment[i][2]
                ) + ")" ;

                queryList.add(queryExpression);

            }


            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

/**
 * foloowing piece of code is to check how many distinct queries oare present in the list
 * It should present only single query in this case. Count should be 3000
 */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);


        // return queryExpression;

    }

    /**
     * The following function generates 10000 queries with each of the segment appear in 60% of queries.
     * full query overlap = 30% and 20%
     */
    private void getQueryExpressions_threeNode_60PCOverlap(){

        String queryExpression="";
        String root = getRoot();

        int fixed_segment1 = getSampleFromDistribution(distribution);
        while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS  )     {
            fixed_segment1=    getSampleFromDistribution(distribution);
        }
        int fixed_segment2 = getSampleFromDistribution(distribution);
        while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment2 == fixed_segment1 )     {
            fixed_segment2=    getSampleFromDistribution(distribution);
        }
        int fixed_segment3 = getSampleFromDistribution(distribution);
        while (fixed_segment3 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment3 == fixed_segment1 || fixed_segment3 == fixed_segment2)     {
            fixed_segment3=    getSampleFromDistribution(distribution);
        }

        int number_of_queries =10000;
        ArrayList<String> queryList = new ArrayList<>();

        try {
//            BufferedWriter out1 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions3_60PCOverlap_10000"));
            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions3_60PCOverlap_10000"));


            int querySegment[][] = new int[10000][3];


            int sample_value1 = 0;
            int sample_value2 = 0;
            int sample_value3 =0;
            String q = "";
            int i = 0;
//            double limit = 0.1*number_of_queries+ i;
            double limit = 0.128*number_of_queries+ i;
            System.out.println("limit 1 ="+limit);

            for(; i < limit; i++){
                sample_value1 = fixed_segment1;
                sample_value3 = fixed_segment3;

                sample_value2=    getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;

            }


//            limit = 0.1*number_of_queries+ i;
            limit = 0.128*number_of_queries+ i;
            System.out.println("limit 2 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1==fixed_segment1) {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0] = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }



//            limit = 0.1*number_of_queries+ i; // for the next 10% of queries
            limit = 0.128*number_of_queries+ i;
            System.out.println("limit 3 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;

                querySegment[i][0] = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }


//            limit = 0.1*number_of_queries+ i; // for the 10% queries
            limit = 0.128*number_of_queries+ i;
            System.out.println("limit 4 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2==fixed_segment3 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3==fixed_segment3)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }




//            limit = 0.1*number_of_queries+ i; // for the 10% queries
            limit = 0.128*number_of_queries+ i;
            System.out.println("limit 5 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment1 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }



//            limit = 0.1*number_of_queries+ i; // for the next 10% queries
            limit = 0.128*number_of_queries+ i;
            System.out.println("limit 6 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment1 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
            }


//            limit = 0.1*number_of_queries+ i; // for the next 10% queries - totally random queries
            limit = 0.0320*number_of_queries+ i;
            System.out.println("limit 7 = "+limit);

            for (; i <limit; i++) {// random queries

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment1 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment1 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;

            }

            System.out.println("final limit = "+i);

            for (int j = 0; j < i; j++) { // remaining queries
                int treeType = new Random().nextInt(4);
                queryExpression="";
                switch (treeType){
                    case 1:{
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +root+  "("+queries.get(querySegment[j][1])+")" + root +"("+ queries.get
                                (querySegment[j][2]) + ")";
                        break;
                    }
                    case 2:{ // two levels. right subtree has children

                        String subRoot = getRoot();
                        queryExpression = "(" + queries.get(querySegment[j][0]) + ")"+root;
                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][1])+")"
                                +subRoot+"("+
                                queries.get(querySegment[j][2])+"))";
                        break;
                    }

                    case 3:{    // two levels, left subtree has children
                        queryExpression = "";
                        String subRoot = getRoot();
                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][0])+")"
                                +subRoot+"("+
                                queries.get(querySegment[j][1])+"))";

                        queryExpression = queryExpression +root+"(" + queries.get(querySegment[j][2]
                        ) + ")" ;
                        break;
                    }
                    default:{
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +root+  "("+queries.get(querySegment[j][1])+")" + root +"("+ queries.get
                                (querySegment[j][2]) + ")";
                        break;
                    }
                }

                queryList.add(queryExpression);

            }

//            limit = 0.3*number_of_queries+ i; // for the next 30% queries
            limit = 0.2*number_of_queries+ i;
            System.out.println("limit fixed queries ="+limit);

            for(; i < limit; i++){

                querySegment[i][0]  = fixed_segment1;
                querySegment[i][1] = fixed_segment2;
                querySegment[i][2] = fixed_segment3;

                root = "&";
                queryExpression = "";
                String subRoot = "&";
                queryExpression = queryExpression + "((" + queries.get(querySegment[i][0])+")"
                        +subRoot+"("+
                        queries.get(querySegment[i][1])+"))";

                queryExpression = queryExpression +root+"(" + queries.get(querySegment[i][2]
                ) + ")" ;

                queryList.add(queryExpression);
            }


            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * following piece of code is to check how many distinct queries oare present in the list
         * It should present only single query in this case. Count should be 3000
         */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);
        // return queryExpression;
    }




    /**
     * Following function generates four node queries with random segments
     * @return
     */
    private String getQueryExpressions_fourNode(){

        String queryExpression="";
        String root = getRoot();
        int treeType = new Random().nextInt(5);

        int sample_value1=    getSampleFromDistribution(distribution);
        while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS )     {
            sample_value1=    getSampleFromDistribution(distribution);
        }

        int sample_value2=    getSampleFromDistribution(distribution);
        while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==sample_value1 )     {
            sample_value2=    getSampleFromDistribution(distribution);
        }

        int sample_value3=    getSampleFromDistribution(distribution);
        while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==sample_value1 || sample_value3==sample_value2 )     {
            sample_value3=    getSampleFromDistribution(distribution);
        }

        int sample_value4=    getSampleFromDistribution(distribution);
        while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS|| sample_value4==sample_value1 || sample_value4==sample_value2 || sample_value4==sample_value3)     {
            sample_value4=    getSampleFromDistribution(distribution);
        }

        switch (treeType){
            case 1:{
                // complete binary tree , three levels
                queryExpression= "";
                String subRoot1 = getRoot();
                String subRoot2 = getRoot();
                queryExpression = queryExpression + "(" + queries.get(sample_value1)+")"
                        +subRoot1+ "("+
                        queries.get(sample_value2)+")";
                queryExpression = queryExpression +root;

                queryExpression = queryExpression+ "(" + queries.get(sample_value3)+")"
                        +subRoot1+ "("+
                        queries.get(sample_value4)+")";


                break;
            }
            case 2:{
                //root node has three children. middle child has two children
                queryExpression = "";
                String subRoot = getRoot();
                queryExpression = queryExpression+"(" + queries.get(sample_value1) + ")"+root;

                queryExpression = queryExpression + "((" + queries.get(sample_value2)+")"
                        +subRoot+"("+
                        queries.get(sample_value3)+"))";

                queryExpression = queryExpression +root+"(" + queries.get(sample_value4
                ) + ")" ;
                break;
            }

            case 3:{
                //three levels. root has two children. Right child has three children
                queryExpression = "";
                String subRoot = getRoot();

                queryExpression = queryExpression+"(" + queries.get(sample_value1) + ")"+root;
                queryExpression = queryExpression + "((" + queries.get(sample_value2)+")"
                        +subRoot+  "("+queries.get(sample_value3)+")" + subRoot +"("+ queries.get
                        (sample_value4) + "))";

                break;
            }

            case 4: {
                //three levels. root has two children. Left child has three children
                queryExpression = "";
                String subRoot = getRoot();

                queryExpression = queryExpression + "((" + queries.get(sample_value1)+")"
                        +subRoot+  "("+queries.get(sample_value2)+")" + subRoot +"("+ queries.get
                        (sample_value3) + "))";

                queryExpression = queryExpression+root+"(" + queries.get(sample_value4) +
                        ")";

                break;
            }
            default: {
                String subRoot1 = getRoot();
                String subRoot2 = getRoot();
                queryExpression= "";
                queryExpression = queryExpression + "(" + queries.get(sample_value1)+")"
                        +subRoot1+ "("+
                        queries.get(sample_value2)+")";
                queryExpression = queryExpression +root;

                queryExpression = queryExpression+ "(" + queries.get(sample_value3)+")"
                        +subRoot1+ "("+
                        queries.get(sample_value4)+")";

                break;
            }
        }

        return queryExpression;

    }

    /**
     * The following function generates 10000 queries with each of the segment appear in 40% of the time
     * full query overlap = 30%    and 20%
     */
    private void getQueryExpressions_fourNode_40PCOverlap(){

        String queryExpression="";
        String root = getRoot();

        int fixed_segment1 = getSampleFromDistribution(distribution);
        while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS  )     {
            fixed_segment1 = getSampleFromDistribution(distribution);
        }

        int fixed_segment2 = getSampleFromDistribution(distribution);
        while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment2 == fixed_segment1 )     {
            fixed_segment2 = getSampleFromDistribution(distribution);
        }

        int fixed_segment3 = getSampleFromDistribution(distribution);
        while (fixed_segment3 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment3 == fixed_segment1 || fixed_segment3 == fixed_segment2)     {
            fixed_segment3 = getSampleFromDistribution(distribution);
        }

        int fixed_segment4 = getSampleFromDistribution(distribution);
        while (fixed_segment4 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment4 == fixed_segment1 || fixed_segment4 == fixed_segment2 || fixed_segment4== fixed_segment3)     {
            fixed_segment4 = getSampleFromDistribution(distribution);
        }


        int number_of_queries =10000;
        ArrayList<String> queryList = new ArrayList<>();

        try {
//            BufferedWriter out1 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions4_40PCOverlap_10000"));
            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions4_40PCOverlap_10000"));
            int querySegment[][] = new int[10000][4];

            int sample_value1 = 0;
            int sample_value2 = 0;
            int sample_value3 =0;
            int sample_value4 = 0;

            String q = "";
            int i = 0;
            double limit = 0;

            /**
             * following four for loops keep one segment constant for 143 queries each
             */
//            limit = 0.0143*number_of_queries+i;
            limit = 0.0286*number_of_queries+i;
            System.out.println("limit 1 = "+limit);

            for(; i < limit; i++){
                sample_value1 = fixed_segment1;
                sample_value2 = getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

//            limit = 0.0143*number_of_queries +i;
            limit = 0.0286*number_of_queries +i;
            //  System.out.println("limit 2 = "+limit);

            for(; i < limit; i++){
                sample_value2 = fixed_segment2;
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);

                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1== fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;

            }

//            limit = 0.0143*number_of_queries +i;
            limit = 0.0286*number_of_queries +i;
            //   System.out.println("limit 3 = "+limit);

            for(; i < limit; i++){
                sample_value3 = fixed_segment3;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;

            }

//            limit = 0.0143*number_of_queries +i;
            limit = 0.0286*number_of_queries +i;
            //   System.out.println("limit 4 = "+limit);

            for(; i < limit; i++){
                sample_value4 = fixed_segment4;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;

            }

            /**
             * following six for loops keep two segments constant
             */
//            limit = 0.0143*number_of_queries+ i; // for 2 &3
            limit = 0.0286*number_of_queries +i;
            //   System.out.println("limit 5 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1== fixed_segment1 ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4== fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }



//            limit = 0.0143*number_of_queries+ i; // for 1 & 2
            limit = 0.0286*number_of_queries +i;
            //     System.out.println("limit 6 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment3 || sample_value3== fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

//            limit = 0.0143*number_of_queries+ i; // for 3 & 4
            limit = 0.0286*number_of_queries +i;
            //    System.out.println("limit 7 = "+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }


//            limit = 0.0143*number_of_queries+ i; // for 1 & 4
            limit = 0.0286*number_of_queries +i;
            //     System.out.println("limit 8 = "+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

//            limit = 0.0143*number_of_queries+ i; // for 1 & 3
            limit = 0.0286*number_of_queries +i;
            //     System.out.println("limit 9 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);

                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4== fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

//            limit = 0.0143*number_of_queries+ i;
            limit = 0.0286*number_of_queries +i; // 2 & 4
            //   System.out.println("limit 10 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }



            /**
             * Following four for loops keep three segments constant
             */
//            limit = 0.0143*number_of_queries+ i; // for 1, 2 ,3
            limit = 0.0286*number_of_queries +i;
            //      System.out.println("limit 11 ="+limit);

            for(; i < limit; i++){
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;


                querySegment[i][0] = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

//            limit = 0.0143*number_of_queries+ i; // for 2,3,4
            limit = 0.0286*number_of_queries +i;
            //      System.out.println("limit 12 = "+limit);

            for(; i < limit; i++){

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1==fixed_segment3 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }


//            limit = 0.0143*number_of_queries+ i; // for 1,3,4
            limit = 0.0286*number_of_queries +i;
            //     System.out.println("limit 13 = "+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2==fixed_segment3 || sample_value2 == fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }


                sample_value1= fixed_segment1;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }


//            limit = 0.0143*number_of_queries+ i; // for 1,2,4
            limit = 0.0286*number_of_queries +i;
            //      System.out.println("limit 14 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3== fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }



//            limit = number_of_queries - 3000;
            limit = number_of_queries - 2000;
            //      System.out.println("limit 15 ="+limit);

            // for the rest of the queries
            for(; i <limit; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3=    getSampleFromDistribution(distribution);

                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }


                sample_value4=    getSampleFromDistribution(distribution);

                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4 == fixed_segment2 || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            //       System.out.println("final limit i "+i);


            for (int j = 0; j < i; j++) {
                int treeType = new Random().nextInt(5);
                queryExpression = "";
                //   System.out.println("at 1823 ---"+j +" *** "+sample_value1+" ** "+sample_value2+" ** "+sample_value3+ " ** "+sample_value4);

                switch (treeType){
                    case 1:{
                        // complete binary tree , three levels
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][1])+")";
                        queryExpression = queryExpression +root;

                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][2])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][3])+")";


                        break;
                    }
                    case 2:{
                        //root node has three children. middle child has two children
                        queryExpression = "";
                        String subRoot = getRoot();
                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][0]) + ")"+root;

                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][1])+")"
                                +subRoot+"("+
                                queries.get(querySegment[j][2])+"))";

                        queryExpression = queryExpression +root+"(" + queries.get(querySegment[j][3]
                        ) + ")" ;
                        break;
                    }

                    case 3:{
                        //three levels. root has two children. Right child has three children
                        queryExpression = "";
                        String subRoot = getRoot();

                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][0]) + ")"+root;
                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][1])+")"
                                +subRoot+  "("+queries.get(querySegment[j][2])+")" + subRoot +"("+ queries.get
                                (querySegment[j][3]) + "))";

                        break;
                    }

                    case 4: {
                        //three levels. root has two children. Left child has three children
                        queryExpression = "";
                        String subRoot = getRoot();


                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][0])+")"
                                +subRoot+  "("+queries.get(querySegment[j][1])+")" + subRoot +"("+ queries.get
                                (querySegment[j][2]) + "))";

                        queryExpression = queryExpression+root+"(" + queries.get(querySegment[j][3]) +
                                ")";

                        break;
                    }
                    default: {
                        queryExpression= "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][1])+")";
                        queryExpression = queryExpression +root;

                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][2])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][3])+")";

                        break;
                    }
                }

                queryList.add(queryExpression);

                // System.out.println(j + " " + treeType +"____"+queryExpression);

            }

            // All the 30% fixed queries

//            limit = 0.3*number_of_queries+ i;
            limit = 0.2*number_of_queries+ i;
            //      System.out.println("limit fixed queries ="+limit);

            for(; i < limit; i++){

                querySegment[i][0] = fixed_segment1;
                querySegment[i][1] = fixed_segment2;
                querySegment[i][2] = fixed_segment3;
                querySegment[i][3] = fixed_segment4;

                root = "&";
                queryExpression = "";
                String subRoot = "&";
                String subRoot2 = "_";
                queryExpression = queryExpression + "((" + queries.get(querySegment[i][0])+")"
                        +subRoot+"("+
                        queries.get(querySegment[i][1])+"))";

                queryExpression = queryExpression +root+"((" + queries.get(querySegment[i][2]
                ) + ")" +subRoot2+"("+
                        queries.get(querySegment[i][3])+"))";

                queryList.add(queryExpression);

            }


            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

/**
 * foloowing piece of code is to check how many distinct queries oare present in the list
 * It should present only single query in this case. Count should be 3000
 */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);

        // return queryExpression;

    }

    /**
     * The following function generates 10000 queries with each of the segment appear in 60% of the time
     * full query overlap = 30%    and 20%
     */
    private void getQueryExpressions_fourNode_60PCOverlap(){

        String queryExpression="";
        String root = getRoot();

        int fixed_segment1 = getSampleFromDistribution(distribution);
        while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS  )     {
            fixed_segment1 = getSampleFromDistribution(distribution);
        }

        int fixed_segment2 = getSampleFromDistribution(distribution);
        while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment2 == fixed_segment1 )     {
            fixed_segment2 = getSampleFromDistribution(distribution);
        }

        int fixed_segment3 = getSampleFromDistribution(distribution);
        while (fixed_segment3 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment3 == fixed_segment1 || fixed_segment3 == fixed_segment2)     {
            fixed_segment3 = getSampleFromDistribution(distribution);
        }

        int fixed_segment4 = getSampleFromDistribution(distribution);
        while (fixed_segment4 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment4 == fixed_segment1 || fixed_segment4 == fixed_segment2 || fixed_segment4== fixed_segment3)     {
            fixed_segment4 = getSampleFromDistribution(distribution);
        }


        int number_of_queries =10000;
        ArrayList<String> queryList = new ArrayList<>();

        try {
//            BufferedWriter out1 = new BufferedWriter(new FileWriter(".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions4_60PCOverlap_10000"));
            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions4_60PCOverlap_10000"));
            int querySegment[][] = new int[10000][4];

            int sample_value1 = 0;
            int sample_value2 = 0;
            int sample_value3 =0;
            int sample_value4 = 0;

            String q = "";
            int i = 0;
            double limit = 0;

            /**
             * following four for loops keep one segment constant for 143 queries each
             */
            // limit = 0.043*number_of_queries+i; //1
            limit = 0.0571*number_of_queries+i;
            //System.out.println("limit 1 = "+limit);

            for(; i < limit; i++){
                sample_value1 = fixed_segment1;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            // limit = 0.043*number_of_queries +i; //2
            limit = 0.0571*number_of_queries+i;
            // System.out.println("limit 2 = "+limit);

            for(; i < limit; i++){
                sample_value2 = fixed_segment2;
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);

                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1== fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;

            }

            // limit = 0.043*number_of_queries +i; // 3
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 3 = "+limit);

            for(; i < limit; i++){
                sample_value3 = fixed_segment3;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;

            }

            //  limit = 0.043*number_of_queries +i; // 4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 4 = "+limit);

            for(; i < limit; i++){
                sample_value4 = fixed_segment4;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;

            }

            /**
             * following six for loops keep two segments constant
             */
            //   limit = 0.043*number_of_queries+ i; // for 2,3
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 5 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1== fixed_segment1 ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4== fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }



            //      limit = 0.043*number_of_queries+ i; //1,2
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 6 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment3 || sample_value3== fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            //    limit = 0.043*number_of_queries+ i; //3,4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 7 = "+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }


            //  limit = 0.043*number_of_queries+ i; //1,4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 8 = "+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            //  limit = 0.043*number_of_queries+ i; //1,3
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 9 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);

                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4== fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            //    limit = 0.043*number_of_queries+ i; //2,4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 10 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }



            /**
             * Following four for loops keep three segments constant
             */
            //    limit = 0.043*number_of_queries+ i; // 1,2,3
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 11 ="+limit);

            for(; i < limit; i++){
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;


                querySegment[i][0] = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            //    limit = 0.043*number_of_queries+ i;//2,3,4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 12 = "+limit);

            for(; i < limit; i++){

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1==fixed_segment3 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }


            //     limit = 0.043*number_of_queries+ i;//1,3,4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 13 = "+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2==fixed_segment3 || sample_value2 == fixed_segment4)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }


                sample_value1= fixed_segment1;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }


            //    limit = 0.043*number_of_queries+ i; // 1,2,4
            limit = 0.0571*number_of_queries+i;
//            System.out.println("limit 14 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3== fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }



            //   limit = number_of_queries - 3000;
            limit = number_of_queries - 2000;
            System.out.println("limit 15 line number 2558="+limit);
            System.out.println("At line number 2559 i="+i);

            // for the rest of the queries
            for(; i <limit; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 )     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3=    getSampleFromDistribution(distribution);

                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3== fixed_segment4)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }


                sample_value4=    getSampleFromDistribution(distribution);

                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4 == fixed_segment2 || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
            }

            System.out.println("final limit i "+i);


            for (int j = 0; j < i; j++) {
                int treeType = new Random().nextInt(5);
                queryExpression = "";


                switch (treeType){
                    case 1:{
                        // complete binary tree , three levels
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][1])+")";
                        queryExpression = queryExpression +root;

                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][2])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][3])+")";


                        break;
                    }
                    case 2:{
                        //root node has three children. middle child has two children
                        queryExpression = "";
                        String subRoot = getRoot();
                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][0]) + ")"+root;

                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][1])+")"
                                +subRoot+"("+
                                queries.get(querySegment[j][2])+"))";

                        queryExpression = queryExpression +root+"(" + queries.get(querySegment[j][3]
                        ) + ")" ;
                        break;
                    }

                    case 3:{
                        //three levels. root has two children. Right child has three children
                        queryExpression = "";
                        String subRoot = getRoot();

                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][0]) + ")"+root;
                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][1])+")"
                                +subRoot+  "("+queries.get(querySegment[j][2])+")" + subRoot +"("+ queries.get
                                (querySegment[j][3]) + "))";

                        break;
                    }

                    case 4: {
                        //three levels. root has two children. Left child has three children
                        queryExpression = "";
                        String subRoot = getRoot();


                        queryExpression = queryExpression + "((" + queries.get(querySegment[j][0])+")"
                                +subRoot+  "("+queries.get(querySegment[j][1])+")" + subRoot +"("+ queries.get
                                (querySegment[j][2]) + "))";

                        queryExpression = queryExpression+root+"(" + queries.get(querySegment[j][3]) +
                                ")";

                        break;
                    }
                    default: {
                        queryExpression= "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression + "(" + queries.get(querySegment[j][0])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][1])+")";
                        queryExpression = queryExpression +root;

                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][2])+")"
                                +subRoot1+ "("+
                                queries.get(querySegment[j][3])+")";

                        break;
                    }
                }
                queryList.add(queryExpression);

                // System.out.println(j + " " + treeType +"____"+queryExpression);

            }

            // All the 30% fixed queries

            //    limit = 0.3*number_of_queries+ i;

            limit = 0.2*number_of_queries+i;

            for(; i < limit; i++){

                querySegment[i][0] = fixed_segment1;
                querySegment[i][1] = fixed_segment2;
                querySegment[i][2] = fixed_segment3;
                querySegment[i][3] = fixed_segment4;

                root = "&";
                queryExpression = "";
                String subRoot = "&";
                String subRoot2 = "_";
                queryExpression = queryExpression + "((" + queries.get(querySegment[i][0])+")"
                        +subRoot+"("+
                        queries.get(querySegment[i][1])+"))";

                queryExpression = queryExpression +root+"((" + queries.get(querySegment[i][2]
                ) + ")" +subRoot2+"("+
                        queries.get(querySegment[i][3])+"))";

                queryList.add(queryExpression);

            }


            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
/**
 * foloowing piece of code is to check how many distinct queries oare present in the list
 * It should present only single query in this case. Count should be 3000
 */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);



        // return queryExpression;

    }

    /**
     * Overlap values:
     *
     * sample_value5 == 720
     * sample_value4 == 1120
     * sample_value3 == 1720
     * sample_value2 == 2520
     * @return
     */
    private String getQueryExpressions_fiveNode() {

        String queryExpression = "";
        String root = getRoot();
        int treeType = new Random().nextInt(5);

        int sample_value1=    getSampleFromDistribution(distribution);
        while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS )     {
            sample_value1=    getSampleFromDistribution(distribution);
        }

        int sample_value2=    getSampleFromDistribution(distribution);
        while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value2 )     {
            sample_value2=    getSampleFromDistribution(distribution);
        }

        int sample_value3=    getSampleFromDistribution(distribution);
        while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS||  sample_value2==sample_value3 || sample_value3==sample_value1 )     {
            sample_value3=    getSampleFromDistribution(distribution);
        }

        int sample_value4=    getSampleFromDistribution(distribution);
        while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value4 || sample_value2==sample_value4 || sample_value3==sample_value4 )     {
            sample_value4=    getSampleFromDistribution(distribution);
        }

        int sample_value5=    getSampleFromDistribution(distribution);
        while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==sample_value5 ||sample_value2==sample_value5 ||sample_value3== sample_value5 ||sample_value4==sample_value5)     {
            sample_value5=    getSampleFromDistribution(distribution);
        }



        //   System.out.println("Five node index is "+treeType);

        switch (treeType){
            case 1: {
                //four levels. root has two children. Left child has three children. one of the children have two
                // children.
                queryExpression="";
                String subRoot1 = getRoot();
                String subRoot2 = getRoot();
                queryExpression = queryExpression+"((" + queries.get(sample_value1) +
                        ")"+subRoot1;

                queryExpression = queryExpression+"(" + queries.get(sample_value2) +
                        ")"+subRoot1;

                queryExpression = queryExpression+"(("+queries.get(sample_value3)+")" +
                        subRoot2 +"("+ queries.get
                        (sample_value4) + ")))" ;

                queryExpression = queryExpression+root+ "(" + queries.get(sample_value5) +
                        ")";

                break;
            }

            case 2:{
                // root has two children. left child has two children and right child has three.
                // three level tree
                String subRoot1 = getRoot();
                String subRoot2 = getRoot();

                queryExpression = "";

                queryExpression =  queryExpression+"(("+queries.get(sample_value1)+")" +
                        subRoot1 +"("+ queries.get
                        (sample_value2) + "))";

                queryExpression = queryExpression+root;

                queryExpression = queryExpression+  "((" + queries.get(sample_value3)+")"
                        +subRoot2+  "("+queries.get(sample_value4)+")" + subRoot2 +"("+ queries.get
                        (sample_value5) + "))";

                break;
            }

            case 3: {
                queryExpression = "";
                String subRoot1 = getRoot();
                queryExpression = queryExpression+"(" + queries.get(sample_value1) +
                        ")"+root;
                queryExpression = queryExpression+  "((" + queries.get(sample_value2)+")"
                        +subRoot1+  "("+queries.get(sample_value3)+")" + subRoot1 +"("+
                        queries.get
                                (sample_value4) + "))" ;

                queryExpression = queryExpression+root+"("+ queries.get(sample_value5) +
                        ")" ;

                break;
            }

            case 4 : {

                queryExpression = "";
                String subRoot1 = getRoot();
                String subRoot2 = getRoot();

                queryExpression = queryExpression+ "(" + queries.get(sample_value1) +
                        ")"+root;
                queryExpression = queryExpression+"(("+ queries.get(sample_value2)+")" +
                        ""+subRoot1+"(("+queries.get(sample_value3)+")"+subRoot2+"" +
                        "("+queries.get(sample_value4)+"))"+subRoot1+"("+queries.get
                        (sample_value5)+"))";

                break;
            }
            default: {
                queryExpression="";
                String subRoot1 = getRoot();
                String subRoot2 = getRoot();
                queryExpression = queryExpression+"((" + queries.get(sample_value1) +
                        ")"+subRoot1;

                queryExpression = queryExpression+"(" + queries.get(sample_value2) +
                        ")"+subRoot1;

                queryExpression = queryExpression+"(("+queries.get(sample_value3)+")" +
                        subRoot2 +"("+ queries.get
                        (sample_value4) + ")))" ;

                queryExpression = queryExpression+root+ "(" + queries.get(sample_value5) +
                        ")";
                break;
            }
        }



        return queryExpression;
    }

    /**
     * The following code is for  five Node 40% overlap. Full queries are repeated 30% and 20%of the time
     */
    private void getQueryExpressions_fiveNode_40PCOverlap() {

        String queryExpression="";
        String root = getRoot();

        int fixed_segment1 = getSampleFromDistribution(distribution);
        while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS  )     {
            fixed_segment1 = getSampleFromDistribution(distribution);
        }

        int fixed_segment2 = getSampleFromDistribution(distribution);
        while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment2 == fixed_segment1 )     {
            fixed_segment2 = getSampleFromDistribution(distribution);
        }

        int fixed_segment3 = getSampleFromDistribution(distribution);
        while (fixed_segment3 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment3 == fixed_segment1 || fixed_segment3 == fixed_segment2)     {
            fixed_segment3 = getSampleFromDistribution(distribution);
        }

        int fixed_segment4 = getSampleFromDistribution(distribution);
        while (fixed_segment4 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment4 == fixed_segment1 || fixed_segment4 == fixed_segment2 || fixed_segment4== fixed_segment3)     {
            fixed_segment4 = getSampleFromDistribution(distribution);
        }


        int fixed_segment5 = getSampleFromDistribution(distribution);
        while (fixed_segment5 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment5 == fixed_segment1 || fixed_segment5 == fixed_segment2 || fixed_segment5 == fixed_segment3 || fixed_segment5 == fixed_segment4)     {
            fixed_segment5 = getSampleFromDistribution(distribution);
        }

        int number_of_queries =10000;
        ArrayList<String> queryList = new ArrayList<>();

        try {
            //  BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
//                    ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions5_40PCOverlap_10000"));
            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions5_40PCOverlap_10000"));
            int querySegment[][] = new int[10000][5];

            int sample_value1 = 0;
            int sample_value2 = 0;
            int sample_value3 =0;
            int sample_value4 = 0;
            int sample_value5 = 0;

            String q = "";
            int i = 0;
            double limit = 0;

            /**
             * following five for loops keep one segment constant
             */
            //  limit = 0.0067*number_of_queries+i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 1 = "+limit);

            for(; i < limit; i++){


                sample_value1 = fixed_segment1;

                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }


                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment2 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }




                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //limit = 0.0067*number_of_queries +i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 2 = "+limit);

            for(; i < limit; i++){
                sample_value2 = fixed_segment2;
                sample_value1=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries +i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 3 = "+limit);

            for(; i < limit; i++){
                sample_value3 = fixed_segment3;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            //  limit = 0.0067*number_of_queries +i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 4 = "+limit);

            for(; i < limit; i++){
                sample_value4 = fixed_segment4;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);                }

                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            //  limit = 0.0067*number_of_queries+ i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 5 ="+limit);

            for(; i < limit; i++){
                sample_value5 = fixed_segment5;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            /**
             * Following ten for  loops keep two segments fixed
             */

            // limit = 0.0067*number_of_queries+ i; //1,2
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 6 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; // 2 &3
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 7 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1>= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            // limit = 0.0067*number_of_queries+ i;//3,4
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 8 = "+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1 ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3  || sample_value1== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; //4 &5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 9 = "+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1 ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3  || sample_value1== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 = getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; //1 &5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 10 = "+limit);

            for(; i < limit; i++){

                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 = getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4== fixed_segment5 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value5 = fixed_segment5;
                sample_value1 = fixed_segment1;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; //1 & 3
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 11 ="+limit);

            for(; i < limit; i++){


                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //    limit = 0.0067*number_of_queries+ i; //1 &4
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 12 = "+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //  limit = 0.0067*number_of_queries+ i; // 2 & 4
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 13 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0067*number_of_queries+ i; // 2 & 5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 14 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4== fixed_segment5 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0067*number_of_queries+ i; // 3 & 5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 15 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4== fixed_segment5 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            /**
             * Following ten for loops keep three segments constant
             */
            //  limit = 0.0067*number_of_queries+ i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 16 ="+limit);

            for(; i < limit; i++){
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 ||  sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0] = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //  limit = 0.0067*number_of_queries+ i;
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 17 = "+limit);

            for(; i < limit; i++){

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1==fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; //3 &4&5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 18 = "+limit);

            for(; i < limit; i++){

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1==fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5 )     {
                    sample_value2 =    getSampleFromDistribution(distribution);
                }

                sample_value5 = fixed_segment5;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }




            //  limit = 0.0067*number_of_queries+ i; // 1& 4& 5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 19 = "+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2==fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3== fixed_segment2 || sample_value3==fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }


                sample_value1= fixed_segment1;
                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            //  limit = 0.0067*number_of_queries+ i; // 1 & 2&5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 20 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3== fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4== fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }


                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0067*number_of_queries+ i; // 1 & 2&4
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 21 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3== fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value5 =  getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5== fixed_segment1 || sample_value5 == fixed_segment2 || sample_value5== fixed_segment3 || sample_value5 == fixed_segment4 || sample_value5 == fixed_segment5)     {
                    sample_value5 =    getSampleFromDistribution(distribution);
                }


                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0067*number_of_queries+ i; // 2 & 3 & 5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 22 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1== fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4 =    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0067*number_of_queries+ i; // 1&3&4
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 23 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value5 =  getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment4 || sample_value5 == fixed_segment5)     {
                    sample_value5 =    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; // 2 &4 & 5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 24 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1== fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; // 1&3&5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 25 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4 =    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            /**
             * following five for loops keep four segments fixed
             */
            //   limit = 0.0067*number_of_queries+ i; // 1,2,3,4
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 26 ="+limit);

            for(; i < limit; i++){
                sample_value5 =  getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5== fixed_segment1 || sample_value5 == fixed_segment2 || sample_value5  == fixed_segment3 || sample_value5 == fixed_segment4 || sample_value5 == fixed_segment5)     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0067*number_of_queries+ i; // 1,3,4,5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 27 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; // 1,2,4,5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 28 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value1 = fixed_segment1;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0067*number_of_queries+ i; // 2,3,4,5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 29 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0067*number_of_queries+ i; // 1, 2,3,5
            limit = 0.0133*number_of_queries+i;
//            System.out.println("limit 30 ="+limit);

            for(; i < limit; i++){
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;
                sample_value1 = fixed_segment1;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }






            //  limit = number_of_queries - 3000;
            limit = number_of_queries - 2000;
//            System.out.println("limit 31 ="+limit);

            // for the rest of the queries
            for(; i <limit; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 || sample_value1==fixed_segment4 ||sample_value1==fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3=    getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4|| sample_value3== fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value4=    getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4 == fixed_segment2 || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 ||sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value5=    getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment4 ||sample_value5==fixed_segment4|| sample_value5 == fixed_segment5)     {
                    sample_value5 =    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

//            System.out.println("final limit i "+i);


            for (int j = 0; j < i; j++) {
                int treeType = new Random().nextInt(5);

                switch (treeType){
                    case 1: {
                        //four levels. root has two children. Left child has three children. one of the children have two
                        // children.
                        queryExpression="";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression+"((" + queries.get(querySegment[j][0]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][1]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(("+queries.get(querySegment[j][2])+")" +
                                subRoot2 +"("+ queries.get
                                (querySegment[j][3]) + ")))" ;

                        queryExpression = queryExpression+root+ "(" + queries.get(querySegment[j][4]) +
                                ")";

                        break;
                    }

                    case 2:{
                        // root has two children. left child has two children and right child has three.
                        // three level tree
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();

                        queryExpression = "";

                        queryExpression =  queryExpression+"(("+queries.get(querySegment[j][0])+")" +
                                subRoot1 +"("+ queries.get
                                (querySegment[j][1]) + "))";

                        queryExpression = queryExpression+root;

                        queryExpression = queryExpression+  "((" + queries.get(querySegment[j][2])+")"
                                +subRoot2+  "("+queries.get(querySegment[j][3])+")" + subRoot2 +"("+ queries.get
                                (querySegment[j][4]) + "))";

                        break;
                    }

                    case 3: {
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][0]) +
                                ")"+root;
                        queryExpression = queryExpression+  "((" + queries.get(querySegment[j][1])+")"
                                +subRoot1+  "("+queries.get(querySegment[j][2])+")" + subRoot1 +"("+
                                queries.get
                                        (querySegment[j][3]) + "))" ;

                        queryExpression = queryExpression+root+"("+ queries.get(querySegment[j][4]) +
                                ")" ;

                        break;
                    }

                    case 4 : {

                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = "";
                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][0]) +
                                ")"+root;
                        queryExpression = queryExpression+"(("+ queries.get(querySegment[j][1])+")" +
                                ""+subRoot1+"(("+queries.get(querySegment[j][2])+")"+subRoot2+"" +
                                "("+queries.get(querySegment[j][3])+"))"+subRoot1+"("+queries.get
                                (querySegment[j][4])+"))";

                        break;
                    }
                    default: {
                        queryExpression="";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression+"((" + queries.get(querySegment[j][0]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][1]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(("+queries.get(querySegment[j][2])+")" +
                                subRoot2 +"("+ queries.get
                                (querySegment[j][3]) + ")))" ;

                        queryExpression = queryExpression+root+ "(" + queries.get(querySegment[j][4]) +
                                ")";
                        break;
                    }
                }

                queryList.add(queryExpression);
// System.out.println(j + " " + treeType +"____"+queryExpression);

            }

            // All the 30% & 20% fixed queries

            //  limit = 0.3*number_of_queries+ i;
            limit = 0.2*number_of_queries+ i;
//            System.out.println("limit fixed queries ="+limit);

            for(; i < limit; i++){

                querySegment[i][0] = fixed_segment1;
                querySegment[i][1] = fixed_segment2;
                querySegment[i][2] = fixed_segment3;
                querySegment[i][3] = fixed_segment4;
                querySegment[i][4] = fixed_segment5;

                root = "&";
                queryExpression = "";
                String subRoot = "&";

                queryExpression = queryExpression+"((" + queries.get(fixed_segment1) +
                        ")"+subRoot;

                queryExpression = queryExpression+"(" + queries.get(fixed_segment2) +
                        ")"+subRoot;

                queryExpression = queryExpression+"(("+queries.get(fixed_segment3)+")" +
                        subRoot +"("+ queries.get
                        (fixed_segment4) + ")))" ;

                queryExpression = queryExpression+root+ "(" + queries.get( fixed_segment5) +
                        ")";

                queryList.add(queryExpression);

            }


            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * foloowing piece of code is to check how many distinct queries oare present in the list
         * It should present only single query in this case. Count should be 3000
         */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);


    }

    /**
     * The following code is for  five Node 60% overlap. Full queries are repeated 30% and 20% of the time
     */
    private void getQueryExpressions_fiveNode_60PCOverlap() {

        String queryExpression="";
        String root = getRoot();

        int fixed_segment1 = getSampleFromDistribution(distribution);
        while (fixed_segment1 >= NUMBER_OF_QUERY_SEGMENTS  )     {
            fixed_segment1 = getSampleFromDistribution(distribution);
        }

        int fixed_segment2 = getSampleFromDistribution(distribution);
        while (fixed_segment2 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment2 == fixed_segment1 )     {
            fixed_segment2 = getSampleFromDistribution(distribution);
        }

        int fixed_segment3 = getSampleFromDistribution(distribution);
        while (fixed_segment3 >= NUMBER_OF_QUERY_SEGMENTS  || fixed_segment3 == fixed_segment1 || fixed_segment3 == fixed_segment2)     {
            fixed_segment3 = getSampleFromDistribution(distribution);
        }

        int fixed_segment4 = getSampleFromDistribution(distribution);
        while (fixed_segment4 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment4 == fixed_segment1 || fixed_segment4 == fixed_segment2 || fixed_segment4== fixed_segment3)     {
            fixed_segment4 = getSampleFromDistribution(distribution);
        }


        int fixed_segment5 = getSampleFromDistribution(distribution);
        while (fixed_segment5 >= NUMBER_OF_QUERY_SEGMENTS || fixed_segment5 == fixed_segment1 || fixed_segment5 == fixed_segment2 || fixed_segment5 == fixed_segment3 || fixed_segment5 == fixed_segment4)     {
            fixed_segment5 = getSampleFromDistribution(distribution);
        }

        int number_of_queries =10000;
        ArrayList<String> queryList = new ArrayList<>();

        try {
            //BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
            //      ".//src//main//java//project//QueryEnvironment//QueryInput//QueryExpressions5_60PCOverlap_10000
            // "));
            BufferedWriter out1 = new BufferedWriter(new FileWriter("" +
                    ".//src//main//java//project//QueryEnvironment//QueryInput" +
                    "//20PC_QueryExpressions5_60PCOverlap_10000"));
            int querySegment[][] = new int[10000][5];

            int sample_value1 = 0;
            int sample_value2 = 0;
            int sample_value3 =0;
            int sample_value4 = 0;
            int sample_value5 = 0;

            String q = "";
            int i = 0;
            double limit = 0;

            /**
             * following five for loops keep one segment constant
             */
            // limit = 0.0201*number_of_queries+i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 1 = "+limit);

            for(; i < limit; i++){


                sample_value1 = fixed_segment1;

                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }


                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment2 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }




                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0201*number_of_queries +i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 2 = "+limit);

            for(; i < limit; i++){
                sample_value2 = fixed_segment2;
                sample_value1=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            // limit = 0.0201*number_of_queries +i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 3 = "+limit);

            for(; i < limit; i++){
                sample_value3 = fixed_segment3;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            // limit = 0.0201*number_of_queries +i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 4 = "+limit);

            for(; i < limit; i++){
                sample_value4 = fixed_segment4;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);
                sample_value5 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);                }

                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5==fixed_segment1|| sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            //  limit = 0.0201*number_of_queries+ i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 5 ="+limit);

            for(; i < limit; i++){
                sample_value5 = fixed_segment5;
                sample_value2=  getSampleFromDistribution(distribution);
                sample_value3 = getSampleFromDistribution(distribution);
                sample_value4 = getSampleFromDistribution(distribution);
                sample_value1 = getSampleFromDistribution(distribution);

                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2==fixed_segment1|| sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3==fixed_segment1|| sample_value3 == fixed_segment3 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1|| sample_value4 == fixed_segment3 || sample_value4== fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1==fixed_segment1|| sample_value1 == fixed_segment3 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5 )     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }


                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            /**
             * Following ten for  loops keep two segments fixed
             */

            //  limit = 0.0201*number_of_queries+ i; //1,2
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 6 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0201*number_of_queries+ i; // 2 &3
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 7 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1>= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1== fixed_segment2 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);                }

                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //  limit = 0.0201*number_of_queries+ i;//3,4
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 8 = "+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1 ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3  || sample_value1== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0201*number_of_queries+ i; //4 &5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 9 = "+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1 ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3  || sample_value1== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 = getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0201*number_of_queries+ i; //1 &5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 10 = "+limit);

            for(; i < limit; i++){

                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value3 = getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5 )     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4== fixed_segment5 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value5 = fixed_segment5;
                sample_value1 = fixed_segment1;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0201*number_of_queries+ i; //1 & 3
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 11 ="+limit);

            for(; i < limit; i++){


                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value3 = fixed_segment3;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //  limit = 0.0201*number_of_queries+ i; //1 &4
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 12 = "+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment4 || sample_value1==fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //    limit = 0.0201*number_of_queries+ i; // 2 & 4
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 13 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0201*number_of_queries+ i; // 2 & 5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 14 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3== fixed_segment2 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4== fixed_segment5 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //  limit = 0.0201*number_of_queries+ i; // 3 & 5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 15 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment4 || sample_value4== fixed_segment5 )     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            /**
             * Following ten for loops keep three segments constant
             */
            //    limit = 0.0201*number_of_queries+ i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 16 ="+limit);

            for(; i < limit; i++){
                sample_value4 = getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4==fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4== fixed_segment4 ||  sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;

                querySegment[i][0] = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }



            //    limit = 0.0201*number_of_queries+ i;
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 17 = "+limit);

            for(; i < limit; i++){

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1==fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value5 = getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment4 || sample_value5== fixed_segment5 )     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }

                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; //3 &4&5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 18 = "+limit);

            for(; i < limit; i++){

                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment1 || sample_value1== fixed_segment2 || sample_value1==fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value2 = getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 || sample_value2== fixed_segment2 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5 )     {
                    sample_value2 =    getSampleFromDistribution(distribution);
                }

                sample_value5 = fixed_segment5;
                sample_value3 = fixed_segment3;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }




            //   limit = 0.0201*number_of_queries+ i; // 1& 4& 5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 19 = "+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2==fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment1 || sample_value3== fixed_segment2 || sample_value3==fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }


                sample_value1= fixed_segment1;
                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            //   limit = 0.0201*number_of_queries+ i; // 1 & 2&5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 20 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3== fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4== fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }


                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 1 & 2&4
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 21 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3== fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3== fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }
                sample_value5 =  getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5== fixed_segment1 || sample_value5 == fixed_segment2 || sample_value5== fixed_segment3 || sample_value5 == fixed_segment4 || sample_value5 == fixed_segment5)     {
                    sample_value5 =    getSampleFromDistribution(distribution);
                }


                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 2 & 3 & 5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 22 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1== fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1== fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4 =    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 1&3&4
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 23 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value5 =  getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment2 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment4 || sample_value5 == fixed_segment5)     {
                    sample_value5 =    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //   limit = 0.0201*number_of_queries+ i; // 2 &4 & 5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 24 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1== fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 1&3&5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 25 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4 =    getSampleFromDistribution(distribution);
                }

                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value5 = fixed_segment5;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            /**
             * following five for loops keep four segments fixed
             */
            //     limit = 0.0201*number_of_queries+ i; // 1,2,3,4
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 26 ="+limit);

            for(; i < limit; i++){
                sample_value5 =  getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5== fixed_segment1 || sample_value5 == fixed_segment2 || sample_value5  == fixed_segment3 || sample_value5 == fixed_segment4 || sample_value5 == fixed_segment5)     {
                    sample_value5=    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value2 = fixed_segment2;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 1,3,4,5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 27 ="+limit);

            for(; i < limit; i++){
                sample_value2 =  getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS || sample_value2== fixed_segment1 || sample_value2 == fixed_segment2 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }


                sample_value3 = fixed_segment3;
                sample_value1 = fixed_segment1;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 1,2,4,5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 28 ="+limit);

            for(; i < limit; i++){
                sample_value3 =  getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment2 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4 || sample_value3 == fixed_segment5)     {
                    sample_value3 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value1 = fixed_segment1;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 2,3,4,5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 29 ="+limit);

            for(; i < limit; i++){
                sample_value1 =  getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment2 || sample_value1 == fixed_segment3 || sample_value1 == fixed_segment4 || sample_value1 == fixed_segment5)     {
                    sample_value1 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;
                sample_value4 = fixed_segment4;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

            //    limit = 0.0201*number_of_queries+ i; // 1, 2,3,5
            limit = 0.0266*number_of_queries+i;
//            System.out.println("limit 30 ="+limit);

            for(; i < limit; i++){
                sample_value4 =  getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment2 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 || sample_value4 == fixed_segment5)     {
                    sample_value4 =    getSampleFromDistribution(distribution);
                }


                sample_value2 = fixed_segment2;
                sample_value3 = fixed_segment3;
                sample_value5 = fixed_segment5;
                sample_value1 = fixed_segment1;

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }


            //limit = number_of_queries - 3000;
            limit = number_of_queries - 2000;
//            System.out.println("limit 31 ="+limit);

            // for the rest of the queries
            for(; i <limit; i++){
                sample_value1=    getSampleFromDistribution(distribution);
                while (sample_value1 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value1 == fixed_segment2 || sample_value1 == fixed_segment1 || sample_value1 == fixed_segment3 || sample_value1==fixed_segment4 ||sample_value1==fixed_segment5)     {
                    sample_value1=    getSampleFromDistribution(distribution);
                }

                sample_value2=    getSampleFromDistribution(distribution);
                while (sample_value2 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value2 == fixed_segment2 || sample_value2 == fixed_segment1 || sample_value2 == fixed_segment3 || sample_value2 == fixed_segment4 || sample_value2 == fixed_segment5)     {
                    sample_value2=    getSampleFromDistribution(distribution);
                }

                sample_value3=    getSampleFromDistribution(distribution);
                while (sample_value3 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value3 == fixed_segment2 || sample_value3 == fixed_segment1 || sample_value3 == fixed_segment3 || sample_value3 == fixed_segment4|| sample_value3== fixed_segment5)     {
                    sample_value3=    getSampleFromDistribution(distribution);
                }

                sample_value4=    getSampleFromDistribution(distribution);
                while (sample_value4 >= NUMBER_OF_QUERY_SEGMENTS ||sample_value4 == fixed_segment2 || sample_value4 == fixed_segment1 || sample_value4 == fixed_segment3 || sample_value4 == fixed_segment4 ||sample_value4 == fixed_segment5)     {
                    sample_value4=    getSampleFromDistribution(distribution);
                }

                sample_value5=    getSampleFromDistribution(distribution);
                while (sample_value5 >= NUMBER_OF_QUERY_SEGMENTS || sample_value5 == fixed_segment1 || sample_value5 == fixed_segment3 || sample_value5 == fixed_segment4 ||sample_value5==fixed_segment4|| sample_value5 == fixed_segment5)     {
                    sample_value5 =    getSampleFromDistribution(distribution);
                }

                querySegment[i][0]  = sample_value1;
                querySegment[i][1] = sample_value2;
                querySegment[i][2] = sample_value3;
                querySegment[i][3] = sample_value4;
                querySegment[i][4] = sample_value5;
            }

//            System.out.println("final limit i "+i);


            for (int j = 0; j < i; j++) {
                int treeType = new Random().nextInt(5);

                switch (treeType){
                    case 1: {
                        //four levels. root has two children. Left child has three children. one of the children have two
                        // children.
                        queryExpression="";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression+"((" + queries.get(querySegment[j][0]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][1]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(("+queries.get(querySegment[j][2])+")" +
                                subRoot2 +"("+ queries.get
                                (querySegment[j][3]) + ")))" ;

                        queryExpression = queryExpression+root+ "(" + queries.get(querySegment[j][4]) +
                                ")";

                        break;
                    }

                    case 2:{
                        // root has two children. left child has two children and right child has three.
                        // three level tree
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();

                        queryExpression = "";

                        queryExpression =  queryExpression+"(("+queries.get(querySegment[j][0])+")" +
                                subRoot1 +"("+ queries.get
                                (querySegment[j][1]) + "))";

                        queryExpression = queryExpression+root;

                        queryExpression = queryExpression+  "((" + queries.get(querySegment[j][2])+")"
                                +subRoot2+  "("+queries.get(querySegment[j][3])+")" + subRoot2 +"("+ queries.get
                                (querySegment[j][4]) + "))";

                        break;
                    }

                    case 3: {
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][0]) +
                                ")"+root;
                        queryExpression = queryExpression+  "((" + queries.get(querySegment[j][1])+")"
                                +subRoot1+  "("+queries.get(querySegment[j][2])+")" + subRoot1 +"("+
                                queries.get
                                        (querySegment[j][3]) + "))" ;

                        queryExpression = queryExpression+root+"("+ queries.get(querySegment[j][4]) +
                                ")" ;

                        break;
                    }

                    case 4 : {

                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = "";
                        queryExpression = queryExpression+ "(" + queries.get(querySegment[j][0]) +
                                ")"+root;
                        queryExpression = queryExpression+"(("+ queries.get(querySegment[j][1])+")" +
                                ""+subRoot1+"(("+queries.get(querySegment[j][2])+")"+subRoot2+"" +
                                "("+queries.get(querySegment[j][3])+"))"+subRoot1+"("+queries.get
                                (querySegment[j][4])+"))";

                        break;
                    }
                    default: {
                        queryExpression="";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression+"((" + queries.get(querySegment[j][0]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(" + queries.get(querySegment[j][1]) +
                                ")"+subRoot1;

                        queryExpression = queryExpression+"(("+queries.get(querySegment[j][2])+")" +
                                subRoot2 +"("+ queries.get
                                (querySegment[j][3]) + ")))" ;

                        queryExpression = queryExpression+root+ "(" + queries.get(querySegment[j][4]) +
                                ")";
                        break;
                    }
                }
                queryList.add(queryExpression);
// System.out.println(j + " " + treeType +"____"+queryExpression);

            }

            // All the 30% fixed queries

            //     limit = 0.3*number_of_queries+ i;
            limit = 0.2*number_of_queries+ i;
//            System.out.println("limit fixed queries ="+limit);

            for(; i < limit; i++){

                querySegment[i][0] = fixed_segment1;
                querySegment[i][1] = fixed_segment2;
                querySegment[i][2] = fixed_segment3;
                querySegment[i][3] = fixed_segment4;
                querySegment[i][4] = fixed_segment5;

                root = "&";
                queryExpression = "";
                String subRoot = "&";

                queryExpression = queryExpression+"((" + queries.get(fixed_segment1) +
                        ")"+subRoot;

                queryExpression = queryExpression+"(" + queries.get(fixed_segment2) +
                        ")"+subRoot;

                queryExpression = queryExpression+"(("+queries.get(fixed_segment3)+")" +
                        subRoot +"("+ queries.get
                        (fixed_segment4) + ")))" ;

                queryExpression = queryExpression+root+ "(" + queries.get( fixed_segment5) +
                        ")";

                queryList.add(queryExpression);

            }


            Collections.shuffle(queryList);

            for(String query:queryList){
                out1.write(query);
                out1.write("\n");
            }

            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * foloowing piece of code is to check how many distinct queries oare present in the list
         * It should present only single query in this case. Count should be 3000
         */
        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];

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
                distinctQueries[independentQuery] = new Query_Count(query);

            }
        }

        int total = 0;
        for (int i = 0; i < independentQuery ; i++) {

            if (distinctQueries[i].getCount()>10) {
                System.out.println(distinctQueries[i].getQuery() + " " + distinctQueries[i].getCount());
                total += distinctQueries[i].getCount();
            }

        }
        System.out.println("total queries "+ total);


    }

    /**
     * This method is to create uniform number of node trees or trees of any height
     * height=0 means, the query expressions array list will contain a mixed bag
     * @param
     * @return
     */
    public ArrayList<String> createQueryExpressions() {
        //  int number_of_queries=10000;
        ArrayList<String> queryList = new ArrayList<>();


        try {
            BufferedWriter out1 = new BufferedWriter(new FileWriter(BACKUP_FILE));


            for (int i = 0; i < NUMBER_OF_QUERIES ; i++) {
               /* if(height ==0)
                    height = new Random().nextInt(4);
                childHeight=0;     */
                //queryList.add(getQueryExpression(""));

                String q = getQueryExpressions_twoNode();


                out1.write(q);
                out1.write("\n");
                queryList.add(q);

            }
            out1.close();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return queryList;
    }

    class Query_Count{
        String query;
        int count=0;

        public Query_Count(String query) {
            this.query = query;
            count = 1;
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

        public void addCount(int i){
            this.count += i;
        }
    }

    public static void main(String[] args) {
        QueryGenerator_20PC qg = new QueryGenerator_20PC();
        //   ArrayList<String> test = qg.createQueryExpressions();


        // qg.getQueryExpressions_oneNode_40PCOverlap();
        // qg.getQueryExpressions_oneNode_60PCOverlap();
        qg.getQueryExpressions_twoNode_40PCOverlap();//3000
        System.out.println("done 2_40");
        qg.getQueryExpressions_twoNode_60PCOverlap();//3000
        System.out.println("done 2_60");
        qg.getQueryExpressions_threeNode_40PCOverlap(); //3000
        System.out.println("done 3_40");
        qg.getQueryExpressions_threeNode_60PCOverlap();//3000
        System.out.println("done 3_60");
        qg.getQueryExpressions_fourNode_40PCOverlap();//3144
        System.out.println("done 4_40");
        qg.getQueryExpressions_fourNode_60PCOverlap(); //3431
        System.out.println("done 4_60");
        qg.getQueryExpressions_fiveNode_40PCOverlap(); //3000
        System.out.println("done 5_40");
        qg.getQueryExpressions_fiveNode_60PCOverlap(); //3000
        System.out.println("done 5_60");
//
        //System.out.println("from main");

       /*
       Iterator<String> itr = test.iterator();
        while(itr.hasNext()){
            System.out.println(itr.next());
        }
        */

    }
}
