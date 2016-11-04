/**
 Copyright (C) <2016>  <Santhilata Kuppili Venkata>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package SimpleGui;

/**
 * Created by santhilata on 12/02/16.
 */
/*
 * To change this license header, choose License Headers in project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.engine.DRand;
import cern.jet.random.engine.RandomEngine;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by santhilata on 6/2/16.
 */
public class QueryGenerator {

    private ConfigurationClass configuration;
    private int noEpochs;
    private String[] distributions;
    private int[] numbQs;

    private String[] percentQueryRepetition;
    private int[] numbSubQs;
    private String[] percentSubqueryRepetition;
    private double[] meanValues ;
    private double[] varianceValues;
    private double[] meanExponentialValues;

    private int[] uniformLowerLimit ;
    private int[] uniformUpperLimit;
    private double[] slopeValue;
    ArrayList<String> sub_queries = new ArrayList<>();

   // String[] userLocations = new String[20];
    ArrayList<String> userLocations = new ArrayList<>();

    int noOfPQSS = 50;
    int NUMBER_OF_QUERY_SEGMENTS = 4000; // limiting no of query segments geb=nerated to some value
    final String[] conditionArray = {"eq","gt","lt","gte","lte","true","false","other","between"};
    private static int noOfSQS = 0; //
    ArrayList<String> sqss = new ArrayList<>();
    ArrayList<String> pqss = new ArrayList<>(50);

    public QueryGenerator(){ }

    public int getNoEpochs() {
        return noEpochs;
    }

    public void setNoEpochs(int noEpochs) {
        this.noEpochs = noEpochs;
    }

    public String[] getDistributions() {
        return distributions;
    }

    public void setDistributions(String[] distributions) {
        this.distributions = distributions;
    }

    public int[] getNumbQs() {
        return numbQs;
    }

    public void setNumbQs(int[] numbQs) {
        this.numbQs = numbQs;
    }

    public String[] getPercentQueryRepetition() {
        return percentQueryRepetition;
    }

    public void setPercentQueryRepetition(String[] percentQueryRepetition) {
        this.percentQueryRepetition = percentQueryRepetition;
    }

    public int[] getNumbSubQs() {
        return numbSubQs;
    }

    public void setNumbSubQs(int[] numbSubQs) {
        this.numbSubQs = numbSubQs;
    }

    public String[] getPercentSubqueryRepetition() {
        return percentSubqueryRepetition;
    }

    public void setPercentSubqueryRepetition(String[] percentSubqueryRepetition) {
        this.percentSubqueryRepetition = percentSubqueryRepetition;
    }

    public double[] getMeanValues() {
        return meanValues;
    }

    public void setMeanValues(double[] meanValues) {
        this.meanValues = meanValues;
    }

    public double[] getMeanExponentialValues() {
        return meanExponentialValues;
    }

    public void setMeanExponentialValues(double[] meanExponentialValues) {
        this.meanExponentialValues = meanExponentialValues;
    }

    public double[] getVarianceValues() {
        return varianceValues;
    }

    public void setVarianceValues(double[] varianceValues) {
        this.varianceValues = varianceValues;
    }

    public int[] getUniformLowerLimit() {
        return uniformLowerLimit;
    }

    public void setUniformLowerLimit(int[] uniformLowerLimit) {
        this.uniformLowerLimit = uniformLowerLimit;
    }

    public int[] getUniformUpperLimit() {
        return uniformUpperLimit;
    }

    public void setUniformUpperLimit(int[] uniformUpperLimit) {
        this.uniformUpperLimit = uniformUpperLimit;
    }

    public double[] getSlopeValue() {
        return slopeValue;
    }

    public void setSlopeValue(double[] slopeValue) {
        this.slopeValue = slopeValue;
    }

    public ConfigurationClass getConfiguration() {
        if (this.configuration == null){
            this.configuration = new ConfigurationClass();
        }
        return configuration;
    }

    public ArrayList<String> getUserLocations() {
        return userLocations;
    }

    public void setUserLocations() {
      //  int numULoc = this.configuration.getDistributedEnvironment().getDatabaseSchema().length;

        int numbLANs= this.configuration.getDistributedEnvironment().getNumbLANs();
        int numbU_LANs = this.configuration.getDistributedEnvironment().getUserGroupsPerLAN();
        int numULoc = numbLANs *numbU_LANs;
        System.out.println("num u loc"+numULoc);
        for (int i = 0; i < numbLANs ; i++) {
            for (int j = 0; j < numbU_LANs; j++) {
                this.userLocations.add( "ULoc"+i+"_"+j);
            }
        }
    }

    public void setConfiguration(ConfigurationClass configuration) {
        this.configuration = configuration;
    }

    /**
     * The following code will generate code and saves it in a comma separated file (.csv)
     * File contains two columns of values.
     * First column: arrival time of the query (as described in the distribution)
     * Second column: query itself
     */
    public ArrayList<File> generateQuerySet() throws IOException {
        //1.Decide on number of single sub-queries (upto 5)
        //2. Generate all permutations of sub-queries from 1 to given number (for the given number of
        // sub-queries) into an arraylist
        //3. Generate values with given sub-query distribution
        //4. Pick  a particular sub-query sequence and if it is shorter than the required length, add some
        // arbitrary sub-queries.
        //5. Save in an appropriate file with proper file name


        //=====================================================================
        ArrayList<File> inputFiles = new ArrayList<>();
        File sqFile = null;
        FileWriter sqWriter = null;

        File qFile = null;
        FileWriter qWriter = null;

        // get distributions of user locations
        setUserLocations();

        //generate query set for a specified number of epochs
        for (int epoch =1; epoch <= this.getNoEpochs(); epoch++) {
            String distribution = this.distributions[epoch - 1];
            int numQs = numbQs[epoch - 1];
            String pcQueryRepeat = percentQueryRepetition[epoch - 1];
            int numSubqs = numbSubQs[epoch - 1];
            String pcSubQRepeat = percentSubqueryRepetition[epoch - 1];
            int u_upper = uniformUpperLimit[epoch - 1];
            int u_lower = uniformLowerLimit[epoch - 1];
            double slope = slopeValue[epoch-1];

            // get distributions of  arrival times
            int[] arrivalTimes = getDistributionValues(distribution, numQs, meanValues[epoch - 1],
                    varianceValues[epoch - 1], meanExponentialValues[epoch - 1],  u_lower,u_upper,slope,numQs);
            Arrays.sort(arrivalTimes);

            System.out.println(getUserLocations().size());
            int[] userLocationDetails = getDistributionValues("Uniform",numQs,getUserLocations().size()/2,
                    getUserLocations
                    ().size()/3,2,0,getUserLocations().size()-1,1,getUserLocations().size());


            // The following if condition is to specify whether we would want only sub-query repeatition or only query repetition
            //if we want only sub-query repetition, then query repetition is chose to be "None" and vice versa.
            if (pcQueryRepeat.equals("None")) { //if you want only sub query repetition
                //Following code is to create queries with sub-query repeat
                sqFile = new File("//home//santhilata//Dropbox//CacheLearning//QGen//src//main//java//QueryInput//" + epoch + "_subqueryRepeat_10.csv");

                sqWriter = new FileWriter(sqFile);

                sqWriter.flush();
                //sqWriter.append("Arrival time,Query");
                //sqWriter.append('\n');


                //considering 10 uniq queries at the moment. So variance is not much
                double variance = 0;
                double slopeSubQ = 0;
                if (pcSubQRepeat.equals("Poisson"))
                    variance = 3;
                else if (pcSubQRepeat.equals("Grading"))
                    slopeSubQ = 1.5;

                createSubQuerySequences(numSubqs); //creates two lists: uniqQs and uniqSubQs
                ArrayList<String> sub_queryList = new ArrayList<>();
                ArrayList<String> inputQueryList = null;

                int[] sampleSQValues = getDistributionValues(pcSubQRepeat, numQs, (double)uniqSubQs.size()/ 2.0, variance,
                        (double)uniqSubQs.size()/ 3.0, 0, uniqSubQs.size() - 1,slopeSubQ,uniqSubQs.size());

                for (int j = 0; j < numQs; j++) {
                    String str = uniqSubQs.get(sampleSQValues[j]);
                    String[] numSubQueries = str.split(Pattern.quote(")"));
                    int remaining = numSubqs - numSubQueries.length;

                    if (remaining > 0) { // if chosen sequences are less than required number of subqueries
                        for (int i = 0; i < remaining; i++) {
                            str = str +uniqSubQs.get(new Random().nextInt(uniqSubQs.size()));
                        }
                    }
                    sub_queryList.add(str);

                }
                if (numSubqs == 1) inputQueryList = getQueryExpression_oneNode(sub_queryList);
                if (numSubqs == 2) inputQueryList = getQueryExpression_twoNode(sub_queryList);
                if (numSubqs == 3) inputQueryList = getQueryExpression_threeNode(sub_queryList);
                if (numSubqs == 4) inputQueryList = getQueryExpression_fourNode(sub_queryList);
                if (numSubqs == 5) inputQueryList = getQueryExpression_fiveNode(sub_queryList);

                //now write them back to a file
                for (int queries = 0; queries < numQs; queries++) {

                    sqWriter.append(arrivalTimes[queries] + ",");   // add arrival times as the first column
                    // File sub_querySamples = new File
                    //          ("//home//santhilata//dropbox//CacheLearning//QGen//src//main//java//QueryInput//SubQueries" +
                    //                ".txt");
                    sqWriter.append(userLocationDetails[queries]+",") ;
                    sqWriter.append(inputQueryList.get(queries) + "\n");
                }

                sqWriter.close();

            } // if you want only sub query repetition
            else{
                //Following code is to create queries with sub-query repeat
                qFile = new File("//home//santhilata//Dropbox//CacheLearning//QGen//src//main//java//QueryInput//" +
                        epoch + "_queryRepeat_10.csv");
                qWriter = new FileWriter(qFile);

                qWriter.flush();
                //qWriter.append("Arrival time,Query");
                //qWriter.append('\n');

                //considering 10 uniq queries at the moment. So variance is not much
                double variance = 0;
                double slopeQ = 0;

               // if (pcQueryRepeat.equals("Poisson"))  variance = 3; // variance for 20 - 6, 50-12, 100-24
                if (pcQueryRepeat.equals("Grading"))
                    slopeQ = 1.5;

                createSubQuerySequences(numSubqs);
                ArrayList<String> queryList1=null;
                //create unique query lists
                if (numSubqs == 1) queryList1 = getQueryExpression_oneNode(uniqQs);
                else if (numSubqs == 2) queryList1 = getQueryExpression_twoNode(uniqQs);
                else if (numSubqs == 3) queryList1 = getQueryExpression_threeNode(uniqQs);
                else if (numSubqs == 4) queryList1 = getQueryExpression_fourNode(uniqQs);
                else if (numSubqs == 5) queryList1 = getQueryExpression_fiveNode(uniqQs);



                int[] sampleQValues = getDistributionValues(pcQueryRepeat, numQs, (double)queryList1.size()/2.0,
                        (double)queryList1.size()/4.0,
                        (double)queryList1.size()/3.0, 0,
                        queryList1.size()-1,slopeQ,queryList1.size());

                for (int i = 0; i < numQs; i++) {
                    qWriter.append(arrivalTimes[i] + ",");
                    qWriter.append(userLocationDetails[i]+",") ;
                    qWriter.append(queryList1.get(sampleQValues[i]) + "\n");
                }
                qWriter.close();
            }

            countQueries(epoch); // to count unique queries. This step is for debugging purposes only
            File file;
            if (sqFile != null) file = sqFile;
            else file = qFile;

            inputFiles.add(file);

        }// for epoch loop
       // generateSubQuerySamples(4000);




        return inputFiles;
    }

    /**
     * sqss are the subject query segments
     */
    private void  createSQSS(){

        DatabaseDetails[] ddetails = this.getConfiguration().getDistributedEnvironment().getDatabaseSchema();
        String tempSQS = "";
        ArrayList<String> temp_sqss = new ArrayList<>();


        for(int database=1; database < ddetails.length; database++) { // for all  databases
            for(int table = 1; table < ddetails[database].tables.length; table++){ // for  tables within each database
                for (int attribute =1; attribute<ddetails[database].attributes[table].length; attribute++){ // for all
                    // attributes
                    noOfSQS++;
                   // tempSQS = "at"+attribute+table+database+":t"+table+database+":d"+database+"";
                    tempSQS = ddetails[database].getAttributes()[table][attribute]+":"+ddetails[database].getTables()
                            [table]+":"+ddetails[database].getDatabaseName();

                    if (!sqss.contains(tempSQS)) {
                        sqss.add(tempSQS);
                    }

                }

                // the following is to create multiple attributes
                tempSQS = "";
                     int attrLength = ddetails[database].attributes[table].length;
                for (int attribute =1; attribute<attrLength; attribute++) { // for all attributes in each table
                    Random randomIndex = new Random();
                    // Random noOfAttr = new Random();

                    Set checkRandom = new HashSet(5);   // upto 5 attributes only
                    int tempRandomIndexSQS = randomIndex.nextInt(attrLength); //to decide  the attribute to pickup
                    int noOfAttr = new Random().nextInt(4);    // to decide number of attributes in SQS

                    //  checking for duplicates
                    for(int i=0; i < noOfAttr; i++){
                        while (checkRandom.contains(tempRandomIndexSQS)){
                            tempRandomIndexSQS = randomIndex.nextInt(attrLength);
                        }
                        //this piece of tempSQS is to make sure that there is atleast one attribute in SQS
                        checkRandom.add(tempRandomIndexSQS);
                        tempSQS = tempSQS+"at"+ tempRandomIndexSQS+table+database+";";
                    }

                    tempRandomIndexSQS = randomIndex.nextInt(attrLength);
                    while (checkRandom.contains(tempRandomIndexSQS)){
                        tempRandomIndexSQS = randomIndex.nextInt(attrLength);
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

            while ((tempAttr == 0)) {  // to avoid 0
                tempAttr = uniform.sample();
            }

            attr2 = sqss.get(tempAttr);  // get a random value from sqss
            Random shouldHaveAttr2 = new Random();
            int OKattr2 = shouldHaveAttr2.nextInt(4);//4 is an arbitrarily chosen to limit number of attributes

            if (OKattr2 >= 2) {   // to have
                tempPQS = tempPQS + ";" + attr2;
                condition = conditionArray[new Random().nextInt(8)]; // a value from condition

                //cardinality is a random number generated to give a number for size of the query
                long  cardinality = Math.round(Math.abs(new Random().nextGaussian() * 1000));
//                System.out.println("cardinality is "+cardinality);
                tempPQS = tempPQS + ";" + condition+"-"+cardinality;
            }
            else{
                condition = conditionArray[new Random().nextInt(8)];
                long  cardinality = Math.round(Math.abs(new Random().nextGaussian()*1000));
                tempPQS = ";"+ condition+"-"+cardinality;
            }

            pqss.add(tempPQS);// + ">");
        }
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
     * Creating specified number (numOfQuerySegments) of sub-queries
     * This should be first time to keep sub-queries set fixed for all inputs
     */
    public ArrayList<String> generateSubQuerySamples( int numOfQuerySegments) throws IOException {
       /* File file = new File("//home//santhilata//Dropbox//CacheLearning//QGen//src//main//java//QueryInput" +
                "//SubQueries.txt");


        FileWriter fw = new FileWriter(file);

        fw.flush();
        */
        DatabaseDetails[] ddetails = this.getConfiguration().getDistributedEnvironment().getDatabaseSchema();

        int querySegmentSample = 0;
        String tempSTR ="";
        ArrayList<String> queries = new ArrayList<>(numOfQuerySegments);
        createSQSS();
        createPQSS();

        for (int i = 0; i < numOfQuerySegments ; i++) {
            String query = "";

            ArrayList<String> tempListSQS = new ArrayList<>();

            int noOfSQSInQuery = 0; // This variable is to select more than one sqss. This is to ensure we get data from multiple databases
            while (noOfSQSInQuery == 0) {
                noOfSQSInQuery = new Random().nextInt(ddetails.length)+1;    // upto no.of databases
            }

            int seed = new Random().nextInt(8);
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


            String attr2 =   attr1+"*"; // what is this star for?
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
                attr1 = attr1+";"+conditionArray[new Random().nextInt(conditionArray.length)];
                long  cardinality = Math.round(Math.abs(new Random().nextGaussian()*1000));
                attr1 = attr1+";"+conditionArray[new Random().nextInt(conditionArray.length)]+"-"+cardinality;

            }

            String str = "<" + attr1 + attr2+">";

            query = query+"#"+ str;

            queries.add(query);

        }
        /*
        for (String query: queries
             ) {
             fw.append(query+"\n");
        }
        fw.close();
        */
        this.sub_queries =  queries;

        return queries;
    } // end of query segments and hence query

    /**
     * Creating  sub-query sequences
     * Arraylist -> query_2sequences_10 contains 10 queries with 2 sub-query sequences
     */
    public void createSubQuerySequences(int sequenceLength) throws IOException {
       // int[] sub_QueryArray = {10,20, 50, 100};
        int sub_Queries = 10;
        ArrayList<String> singleSequences = generateSubQuerySamples(sub_Queries);  //create single sub-queries
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<String> temp1 = new ArrayList<>();
        String str = "";

        for (int i = 0; i < singleSequences.size(); i++) {
            temp.add("("+singleSequences.get(i)+")");
            uniqSubQs.add("("+singleSequences.get(i)+")") ;
        }
        int mark =1;
        int markTemp=1;
        int seqNo = 0;

        for (int seq=2; seq <= sequenceLength; seq++) {

            for (int k = 0; k < temp.size(); k++) {
                for ( seqNo = mark; seqNo < singleSequences.size(); seqNo++) {
                    String sample = getlastElement(temp.get(k));

                    if (!sample.equals(singleSequences.get(seqNo))) {
                        str = temp.get(k) + "(" + singleSequences.get(seqNo) + ")";
                        if (isAscendingOrder(str,singleSequences)) {

                            temp1.add(str);
                            uniqSubQs.add(str);
                            if (seq == sequenceLength)
                                uniqQs.add(str);
                        }
                    }
                }//for seqNo

                mark++;
                if(mark > singleSequences.size() && temp.size()>0 ) {
                    mark = markTemp+1;
                    if (!getlastElement(temp.get(k)).equals(singleSequences.get(singleSequences.size()-1))){
                        k--;
                    }
                }
            }//for k

            temp.clear();
            temp.addAll(temp1);
            temp1.clear();
            markTemp++;
            mark=markTemp;
        }// for seq



        /*
        if (sub_Queries == 10 ){
            if (sequence ==2) query_2sequences_10 = sequenceList;
            if (sequence == 3) query_3sequences_10 = sequenceList;
            if (sequence ==4 )query_4sequences_10 = sequenceList;
            if (sequence== 5) query_5sequences_10 = sequenceList;
        }
        if (sub_Queries == 20){
            if (sequence ==2) query_2sequences_20 = sequenceList;
            if (sequence == 3) query_3sequences_20 = sequenceList;
            if (sequence ==4 )query_4sequences_20 = sequenceList;
            if (sequence== 5) query_5sequences_20 = sequenceList;
        }
        if (sub_Queries == 50){
            if (sequence ==2) query_2sequences_50 = sequenceList;
            if (sequence == 3) query_3sequences_50 = sequenceList;
            if (sequence ==4 )query_4sequences_50 = sequenceList;
            if (sequence== 5) query_5sequences_50 = sequenceList;
        }
        if (sub_Queries == 100){
            if (sequence ==2) query_2sequences_100 = sequenceList;
            if (sequence == 3) query_3sequences_100 = sequenceList;
            if (sequence ==4 )query_4sequences_100 = sequenceList;
            if (sequence== 5) query_5sequences_100 = sequenceList;
        }
        */



        Collections.shuffle(uniqSubQs);
        Collections.shuffle(uniqQs);

    }

    /**
     * Internal private function to be used in create subquerysequences- that creates in all ascending combinations
     * @param str
     * @return
     */
    private  String getlastElement(String str){
        String[] sampleStr = str.split(Pattern.quote(")"));
        String sample = sampleStr[sampleStr.length-1];
        sample = sample.substring(1,sample.length());
        return  sample;
    }

    /**
     * Internal private function to be used in create subquery sequences- that creates in all ascending combinations
     * @param str
     * @param singleSequencesList
     * @return
     */
    private  boolean isAscendingOrder(String str, ArrayList<String> singleSequencesList){
        String[] sampleStr = str.split(Pattern.quote(")"));
        for (int i = 0; i < sampleStr.length-1; i++) {
            for (int j = i; j < sampleStr.length; j++) {
                String sample1 = sampleStr[i];
                sample1 = sample1.substring(1,sample1.length());
                String sample2 = sampleStr[j];
                sample2 = sample2.substring(1,sample2.length());

                if (singleSequencesList.indexOf(sample1)> singleSequencesList.indexOf(sample2))
                    return false;

            }

        }
        return true;
    }

    /**
     * following piece of code is to check how many distinct queries that are present in the list
     * It should present only single query in this case. Count should be 3000
     */
    public void countQueries(int epoch) throws IOException {

        int independentQuery=-1;
        Query_Count[] distinctQueries = new Query_Count[10000];
        ArrayList<String> queryList = new ArrayList<>();
        ArrayList arrivalTimes = new ArrayList();
        ArrayList userLocations = new ArrayList();

        try (BufferedReader br = new BufferedReader(new FileReader("//home//santhilata//Dropbox//CacheLearning//QGen//src//main//java//QueryInput//"+epoch+"_queryRepeat_10.csv"))){

            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                //String[] query = sCurrentLine.split(Pattern.quote("@("));
                String[] query = sCurrentLine.split(Pattern.quote(",("));
                queryList.add("("+query[1]);
                //each line contain <arrivalTime, userLocation, query> ==<int, int, string>
                arrivalTimes.add(query[0].split(Pattern.quote(","))[0]);
                userLocations.add(query[0].split(Pattern.quote(","))[1]);
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
        String fileOut = "//home//santhilata//Desktop//QueryOutput//CountQueries//test.csv";


        File outFile11 = new File(fileOut);
        FileWriter writer1 = new FileWriter(outFile11);

        writer1.flush();
        writer1.append("QueryNumber,QRepetitions");
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

        String arrivalTime = "//home//santhilata//Desktop//QueryOutput//CountQueries//arrivalTimes.csv";
        File aTFile = new File(arrivalTime);
        FileWriter Atwriter = new FileWriter(aTFile);
        Atwriter.flush();
        Atwriter.append("ArrivalTimes,noOfTimes"+"\n");
        int noOfTimes = 1;
        Collections.sort(arrivalTimes);
        for (int i =0; i< arrivalTimes.size(); i++){

            if (i>0) {

                if ( Integer.parseInt((String)arrivalTimes.get(i)) == Integer.parseInt((String)arrivalTimes.get(i - 1))) {
                    noOfTimes++;

                } else {
                    Atwriter.append(arrivalTimes.get(i - 1) + "," + noOfTimes + "\n");
                    noOfTimes = 1;
                }
            }
        }

        for (int i = 0; i < queryList.size(); i++) {
            if (! arrivalTimes.contains(i+"")){
                Atwriter.append(i+","+0+"\n");
            }
        }

        Atwriter.close();


    }

    /**
     * following function is to generate  one node queries
     * @return
     */
    private ArrayList<String> getQueryExpression_oneNode(ArrayList<String> newSubQuery){
        ArrayList<String> queryList = new ArrayList<>();

        if (newSubQuery != null)
            for (int i=0; i < newSubQuery.size(); i++) {
                String str = newSubQuery.get(0) +")";
                queryList.add(str);
            }
        return queryList;
    }

    /**
     * Following function generates two node queries separated by root value
     *      ||          &
     *     / \         / \
     *    O  O        O  O
     * @param uniqQs
     * @return
     */
    private ArrayList<String> getQueryExpression_twoNode(ArrayList<String> uniqQs){
        ArrayList<String> queryList = new ArrayList<>();

        if(uniqQs != null)
            for (int i=0; i < uniqQs.size(); i++) {
                String[] queryArray = uniqQs.get(i).split(Pattern.quote(")"));
                String query = "";
                query = queryArray[0] + ")" + getRoot() + queryArray[1]+ ")";
                queryList.add(query);
            }

        return queryList;
    }

    /**
     * Following function generates three node queries
     * 1.    R               2.  R        3.    R
     *     / | \                / \            / \
     *    O  O  O              O   SR         SR  O
     *                             / \       / \
     *                            O  O      O  O
     * @param uniqQs
     * @return
     */
    private ArrayList<String> getQueryExpression_threeNode(ArrayList<String> uniqQs){

        ArrayList<String> queryList =new ArrayList<>();
        String queryExpression = "";

        if (uniqQs != null)
        for (int i = 0; i < uniqQs.size() ; i++) {
            String[] newSubQuery = uniqQs.get(i).split(Pattern.quote(")"));

            int treeType = new Random().nextInt(4);
            String root = getRoot();
            String subRoot = getRoot();

            switch (treeType) {
                case 1: {
                    queryExpression = newSubQuery[0] + ")"
                            + root + newSubQuery[1] + ")" + root + newSubQuery[2] + ")";
                    break;
                }
                case 2: { // two levels. right subtree has children

                    queryExpression = newSubQuery[0] + ")" + root;
                    queryExpression = queryExpression + "(" + newSubQuery[1] + ")"
                            + subRoot + newSubQuery[2] + "))";
                    break;
                }

                case 3: {    // two levels, left subtree has children

                    queryExpression = "(" + newSubQuery[0] + ")"
                            + subRoot + newSubQuery[1] + "))";

                    queryExpression = queryExpression + root + newSubQuery[2] + ")";
                    break;
                }
                default: {
                    queryExpression = newSubQuery[0] + ")"
                            + root + newSubQuery[1] + ")" + root + newSubQuery[2] + ")";
                    break;
                }

            }

            queryList.add(queryExpression);

        }// for loop

        return queryList;
    }

    /**
     * Following function generates four node queries
     * redraw following
     * 1.    R               2.  R        3.    R
     *     / | \                / \            / \
     *    O  O  O              O   SR         SR  O
     *                             / \       / \
     *                            O  O      O  O
     * @param uniqQs
     * @return
     */
    private ArrayList<String> getQueryExpression_fourNode(ArrayList<String> uniqQs){
        ArrayList<String> queryList = new ArrayList<>();
        String queryExpression = "";
        String root = getRoot();
        String subRoot1 = getRoot();
        String subRoot2 = getRoot();
        int treeType = new Random().nextInt(5);

        if (uniqQs != null){
            for (int i = 0; i < uniqQs.size(); i++) {
                String[] newSubQuery = uniqQs.get(i).split(Pattern.quote(")"));

                switch (treeType) {
                    case 1: {
                        // complete binary tree , three levels

                        queryExpression = newSubQuery[0] + ")"
                                + subRoot1 + newSubQuery[1] + ")";
                        queryExpression = queryExpression + root;
                        queryExpression = queryExpression + newSubQuery[2] + ")"
                                + subRoot1 + newSubQuery[3] + ")";

                        break;
                    }
                    case 2: {
                        //root node has three children. middle child has two children

                        queryExpression = newSubQuery[0] + ")" + root;
                        queryExpression = queryExpression + "(" + newSubQuery[1] + ")" + subRoot1 +
                                newSubQuery[2] + "))";
                        queryExpression = queryExpression + root + newSubQuery[3] + ")";
                        break;
                    }

                    case 3: {
                        //three levels. root has two children. Right child has three children

                        queryExpression = newSubQuery[0] + ")" + root;
                        queryExpression = queryExpression + "(" + newSubQuery[1] + ")"
                                + subRoot1 + newSubQuery[2] + ")" + subRoot1 + newSubQuery[3] + "))";
                        break;
                    }

                    case 4: {
                        //three levels. root has two children. Left child has three children

                        queryExpression = "(" + newSubQuery[0] + ")"
                                + subRoot1 + newSubQuery[1] + ")" + subRoot1 + newSubQuery[2] + "))";

                        queryExpression = queryExpression + root + newSubQuery[3] + ")";

                        break;
                    }
                    default: {

                        queryExpression = newSubQuery[0] + ")" + subRoot1 +
                                newSubQuery[1] + ")";
                        queryExpression = queryExpression + root;

                        queryExpression = queryExpression + newSubQuery[2] + ")"
                                + subRoot1 + newSubQuery[3] + ")";

                        break;
                    }
                }

                queryList.add(queryExpression);
            }// for loop
        }

        return queryList;
    }

    /**
     * This function is to generate ive node query expressions
     * @param uniqQs
     * @return
     */
    private ArrayList<String> getQueryExpression_fiveNode(ArrayList<String> uniqQs){
        ArrayList<String> queryList = new ArrayList<>();
        String queryExpression = "";
        String root = getRoot();
        int treeType = new Random().nextInt(5);
        if (uniqQs != null) {
            for (int i = 0; i < uniqQs.size(); i++) {
                String[] newSubQuery = uniqQs.get(i).split(Pattern.quote(")"));

                switch (treeType) {
                    case 1: {
                        //four levels. root has two children. Left child has three children. one of the children have two
                        // children.
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression + "(" + newSubQuery[0] +
                                ")" + subRoot1;

                        queryExpression = queryExpression + newSubQuery[1] +
                                ")" + subRoot1;

                        queryExpression = queryExpression + "(" + newSubQuery[2] + ")" +
                                subRoot2 + newSubQuery[3] + ")))";

                        queryExpression = queryExpression + root + newSubQuery[4] + ")";

                        break;
                    }

                    case 2: {
                        // root has two children. left child has two children and right child has three.
                        // three level tree
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();

                        queryExpression = "";

                        queryExpression = queryExpression + "(" + newSubQuery[0] + ")" +
                                subRoot1 + newSubQuery[1] + "))";

                        queryExpression = queryExpression + root;

                        queryExpression = queryExpression + "(" + newSubQuery[2] + ")"
                                + subRoot2 + newSubQuery[3] + ")" + subRoot2 + newSubQuery[4] + "))";

                        break;
                    }

                    case 3: {
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        queryExpression = queryExpression + newSubQuery[0]+
                                ")" + root;
                        queryExpression = queryExpression + "(" + newSubQuery[1] + ")"
                                + subRoot1 + newSubQuery[2] + ")" + subRoot1 +
                                newSubQuery[3] + "))";

                        queryExpression = queryExpression + root + newSubQuery[4] +
                                ")";

                        break;
                    }

                    case 4: {

                        queryExpression = "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();

                        queryExpression = queryExpression + newSubQuery[0] +
                                ")" + root;
                        queryExpression = queryExpression + "(" + newSubQuery[1] + ")" +
                                "" + subRoot1 + "(" + newSubQuery[2] + ")" + subRoot2 + "" +
                                newSubQuery[3] + "))" + subRoot1 + newSubQuery[4]+ "))";

                        break;
                    }
                    default: {
                        queryExpression = "";
                        String subRoot1 = getRoot();
                        String subRoot2 = getRoot();
                        queryExpression = queryExpression + "(" + newSubQuery[0] +
                                ")" + subRoot1;

                        queryExpression = queryExpression + newSubQuery[1] +
                                ")" + subRoot1;

                        queryExpression = queryExpression + "(" + newSubQuery[2] + ")" +
                                subRoot2 + newSubQuery[3]+ ")))";

                        queryExpression = queryExpression + root + newSubQuery[4] +
                                ")";
                        break;
                    }
                }

                queryList.add(queryExpression);
            }//for loop
        }

        return queryList;
    }

    /**
     * Returns array of values in a given distribution
     * @param distribution
     * @param numQs
     * @param mean
     * @param variance
     * @param exponentialMean
     * @param u_lower
     * @param u_upper
     * @return
     */
    public int[] getDistributionValues(String distribution, int numQs,double mean, double variance, double
            exponentialMean, int u_lower, int u_upper, double slope,int upperBoundary){

        int[] values = new int[numQs];

        switch (distribution){
            case "Poisson":{
                RandomEngine engine = new DRand();
                Poisson poisson = new Poisson(mean, engine);
                int poissonObs = poisson.nextInt();
                Normal normal = new Normal(mean, variance, engine);

                for (int i = 0; i < numQs; i++) {
                    double normalObs = normal.nextDouble();
                    int sample = (int)Math.abs(normalObs) ;

                    while ( sample >=  upperBoundary) {
                        normalObs = normal.nextDouble();
                        sample = (int) Math.abs(normalObs);
                    }

                    values[i] = sample;
                    //System.out.println(values[i]+"from poisson");
                //=============================================
                    PoissonDistribution p = new PoissonDistribution(mean);
                    int randomInt = p.sample();
                   // values[i] = randomInt;
                }
               // Arrays.sort(values);

                break;
            }
            case "Random":{

                Random randomGenerator = new Random();
                for (int i = 0; i < numQs; i++) {
                    int randomInt = randomGenerator.nextInt(numQs - 1);
                    values[i] = randomInt + 1; // to avoid '0'
                }


                break;
            }
            case "Uniform":{
                for (int i = 0; i < numQs; i++) {
                    UniformIntegerDistribution u = new UniformIntegerDistribution(u_lower, u_upper);
                    int randomInt = u.sample();
                    while (randomInt >= upperBoundary){
                        randomInt = u.sample();
                    }
                    if(randomInt ==0)
                        values[i] = randomInt + 1; // to avoid random '0'
                    else values[i] = randomInt;

                }
                break;
            }

            case "Exponential":{
                for (int i = 0; i < numQs; i++) {
                    ExponentialDistribution e = new ExponentialDistribution(exponentialMean);
                    double exponential = e.sample();
                    while (exponential >= upperBoundary-1){
                        exponential = e.sample();
                    }
                    values[i] = new Double(exponential).intValue() + 1;  //to avoid random value '0'
                }

                break;
            }

            case "Grading":{ // y=mx+c. increment and then decrement for positive slopes.
                int constant = 0;
                for (int i = 0; i < numQs; i++) {
                    constant += slope;
                    int nextVal = new Double(constant).intValue();
                    System.out.println(upperBoundary);
                    if (nextVal >= upperBoundary || nextVal < 0) {
                        slope = -1*slope;
                        i--;
                    }
                    else values[i]=nextVal;
                }
                    break;
            }
        }
       // Arrays.sort(values);

        return values;
    }

    /**
     * Need to add percent query and sub-queries
     * @return
     */
    @Override
    public String toString(){
        String s="";

        s = s+ "No.of Epochs: "+noEpochs;
        for (int i = 0; i < noEpochs; i++) {
            s = s+ "epoch["+i+"]: ";
            s = s+  " Query Arrival Distribution: "+ distributions[i];
            s=  s+ " No.of Queries: "+ numbQs[i];
            s = s+ " Query repetition: "+percentQueryRepetition[i];
            s = s+ " sub-query repetition: "+percentSubqueryRepetition[i];
        }

        return s;
    }

    ArrayList<String> uniqSubQs= new ArrayList<>();// to keep uniq subQueries standard for a given list
    ArrayList<String> uniqQs = new ArrayList<>();

    ArrayList<String>  query_2sequences_20 ;
    ArrayList<String>  query_2sequences_50 ;
    ArrayList<String>  query_2sequences_10 ;
    ArrayList<String>  query_2sequences_100 ;

    ArrayList<String>  query_3sequences_20;
    ArrayList<String>  query_3sequences_50 ;
    ArrayList<String>  query_3sequences_10 ;
    ArrayList<String>  query_3sequences_100;

    ArrayList<String>  query_4sequences_20;
    ArrayList<String>  query_4sequences_50 ;
    ArrayList<String>  query_4sequences_10 ;
    ArrayList<String>  query_4sequences_100 ;

    ArrayList<String>  query_5sequences_20;
    ArrayList<String>  query_5sequences_50;
    ArrayList<String>  query_5sequences_10 ;
    ArrayList<String>  query_5sequences_100 ;

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


}

