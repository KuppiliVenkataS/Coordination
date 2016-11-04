package project.QueryEnvironment.Subj_PredicateSpecificQueries;


import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import project.QueryEnvironment.*;

import java.io.*;
import java.util.Random;


/**
 * Created by k1224068 on 04/08/14.
 *
 * In this class the method 'generateQueries' is for the simulated input having subject and predicate.
 * Here, Subject refers to the 'SELECT' part of the query and predicate is to the 'WHERE' part of the query
 * Whereas. for the real queries from  test inputs, directly use 'readInputQuery_Subj_Pred ' method to convert queries into
 * 'Attributes(subject attributes and predicate attributes class/Table class/Database class'.
 *
 * This class generates queries input of given number in various distributions - (for example, 'random' , 'poisson' are implemented till now)
 * command to generate queries is: File generatedFile = generateQueries(inputPredicateFile,"Poisson",50);
 * 'generatedFile' in this same package contains queries.
 *
 * The method 'readInputQuery_Subj_Pred ' converts strings from generatedFile into
 * 'Attributes (with condition=null for subject and condition set for predicate ) class/Table class/Database class'
 *  //List<Query> queries = readInputQuery_Subj_Pred(generatedFile);
 *
 *  The inputPredicateFile is the representation of databases.
 * The input file format specified in the queryFile should be strictly followed
 *
 * At the moment, no of segments in a query is restricted to a hardcoded  number. Query length can be randomised
 * Similarly, the no of segments generated (basic queries) is hard coded to 25. This number can be adjusted as per requirement in the method - 'makeSegmentFile'.
 * The poisson mean is hardcoded to 12. To be changed later.
 *
 * Intermediate files:
 * inputFile => possible subject attrobutes
 * inputPredicatefile => possible predicate attributes and conditions
 * segmentFile => This file stores required number of subject segments (separated by " ") and # and required number of subject segments (separated by " ")
 * queryFile => This file creates queries from segments in the required format (these are basic unique queries, from which we can generate distributions)
 * generatedFile =>
 *
 */

public class QueryGenerator_SubjPred {
    /**
     *
     * @param inputFile
     * @param inputPredicateFile
     * @return segmentFile (file contains querySegments a query is composed of )
     * @throws IOException
     */

    public File makeSegmentFile(File inputFile, File inputPredicateFile ) throws IOException {
        File segmentFile = new File(".//src//main//java//project//QueryEnvironment//Subj_PredicateSpecificQueries//segmentFile");
        BufferedWriter outSegments = new BufferedWriter(new FileWriter(segmentFile));// intermediate file to output segments
        BufferedReader subjects = new BufferedReader(new FileReader(inputFile));
        BufferedReader predicates = new BufferedReader(new FileReader(inputPredicateFile));

        Random randomGenerator = new Random();
        int Subj_queryLength =3; //number of subject segments
        int pred_queryLength =3; //number of conditions
        /*
        // the above number can be randomized as follows
        int queryLength = randomGenerator.nextInt(10);
         */

        /** numberOfLines represents no of basic queries that will be generated. A distribution will choose queries from this basic list.
         * At the moment, this number is hard coded to 25. Change as per requirement.
         */
        int  numberOfLines = 15; // the no.of segments to be generated

        List inputSubj = new ArrayList();//subjects read from the file - inputFile

        while(true){
            String line = subjects.readLine();
            if (line == null) break;
            inputSubj.add(line);
        }

        List inputPred = new ArrayList();// predicates read from the file - inputPredicateFile

        while(true){
            String line = predicates.readLine();
            if(line == null) break;
            inputPred.add(line);
        }

        for (int i=0; i<numberOfLines; i++)
        {
            for (int idx = 0; idx < Subj_queryLength; ++idx)
            {
                int randomInt = randomGenerator.nextInt(inputSubj.size());
                outSegments.write(inputSubj.get(randomInt) + " ");
            }
            outSegments.write("#");
            for (int idx = 0; idx < pred_queryLength; ++idx)
            {
                int randomInt = randomGenerator.nextInt(inputPred.size());
                outSegments.write(inputPred.get(randomInt) + " ");
            }
            outSegments.write("\n");

        }//for

        // System.out.println("Done");
        outSegments.close();

        return segmentFile;
    }

    /**
     * inputFile contains attribute:table:database combination.
     * This method generates an intermediate file containing query segments. (makeSegmentFile)
     * This method creates queries from the given segmentFile     *
     * @return - the newly created file of queries - queryFile
     */
    public  File generateQueries(File inputFile, File inputPredicateFile, String distribution, int noOfQueries) throws IOException {

        //queryFile contains
        File queryFile = new File(".//src//main//java//project//QueryEnvironment//Subj_PredicateSpecificQueries//queryFile");

        BufferedWriter outQueries = new BufferedWriter(new FileWriter(queryFile));

          File segmentFile = makeSegmentFile(inputFile,inputPredicateFile); //generates new set of queries every time
       // File segmentFile = new File(".//src//main//java//project//QueryEnvironment//Subj_PredicateSpecificQueries" + "//segmentFile");// static file for debugging purposes

        /** Read segments from the segmentFile.
         */
        BufferedReader in = new BufferedReader(new FileReader(segmentFile));
        int lineNo=1; // for debugging purpose only
        while (true) {
            String line = in.readLine();
            if (line == null) break;
            String[] subj_pred = line.split("#"); //splits subjects and predicates


            //testing
            String str = "";
           // Subject query segments to add
            int subjIndex =0; // subjIndex=0 is for subjects and 1 for predicates
                String[] subj = subj_pred[subjIndex].split(" ");
                String[][] arrayOfBits = new String[subj.length][3]; // read into two dimensional String array to compare and combine


                // System.out.println("Line number: "+ lineNo); // for debugging the segmentFile
                lineNo++;

                for (int i = 0; i < subj.length; i++) {

                    String bits[] = subj[i].split(":");
                    for (int j = 0; j < bits.length; j++) {
                        arrayOfBits[i][j] = bits[j];
                    }
                }
                /**
                 * put all attributes having from same table and database together.
                 */

                for (int i = 0; i < subj.length - 1; i++) {
                    String a = arrayOfBits[i][2];//database name
                    String b = arrayOfBits[i][1];//Table name
                    String c = arrayOfBits[i][0];//attribute name
                    for (int j = i + 1; j < subj.length; j++) {
                        if (!arrayOfBits[j][2].equals(" ") && (!a.equals(" ")) && (a.equals(arrayOfBits[j][2])) && (b.equals(arrayOfBits[j][1]))) {
                            if ((c.equals(arrayOfBits[j][0]))) {
                                //  System.out.println("duplicate");
                                for (int k = 0; k < 3; k++)
                                    arrayOfBits[j][k] = " ";
                            } else {// from the same table and same database
                                //  System.out.println(c + "," + arrayOfBits[j][0] + " are from the same table");
                                arrayOfBits[j][0] = c + ";" + arrayOfBits[j][0]; //';' is the delimiter for attributes from the same table
                                for (int k = 0; k < 3; k++)
                                    arrayOfBits[i][k] = " ";
                            }
                        }
                    }
                }//for i=0

                for (int i = 0; i < subj.length; i++) {
                    if (arrayOfBits[i][0].equals(" "))
                        continue;
                    else {
                        str = str+"<" + arrayOfBits[i][0] + ":" + arrayOfBits[i][1] + ":" + arrayOfBits[i][2]+">";//'< >' are the delimiters for segments
                    }

                }
                if(subjIndex == 0)
                    str = str+"#";//$' is the delimiter for subject attributes and predicate attributes
                // else str = str+">";


            // Start adding predicate query segments

            subjIndex=1; // subjIndex = 1 is to add predicate query segments
            String[] pred = subj_pred[subjIndex].split(" ");
            for (int i = 0; i < pred.length ; i++) {
                str = str+"<"+pred[i]+">";
            }


            // System.out.println("QueryFileSegments"+str);


            outQueries.write(str);
            outQueries.write("\n");


        }//while (true)
        outQueries.close();

        //final file that generates queries according to a statistical distribution
        File generatedFile;
        generatedFile = generateDistributions(queryFile, distribution, noOfQueries);

        return generatedFile;
    }

    /**
     *
     * @param distribution specifies the statistical distribution of queries
     * @return the generated file
     * At present random distribution and poisson distribution are implemented
     */
    public  File  generateDistributions(File queryFile, String distribution, int noOfQueries) throws IOException {
        File generatedFile = new File(".//src//main//java//project//QueryEnvironment//Subj_PredicateSpecificQueries//generatedFile");

        BufferedReader in = new BufferedReader(new FileReader(queryFile));
        BufferedWriter out = new BufferedWriter(new FileWriter(generatedFile));

        ArrayList queryList = new ArrayList();

        while(true){
            String query = in.readLine();
            if(query == null) break;

            queryList.add(query);

        }
        int query_type = 0;

        if (distribution.equals("Random")) query_type = 1;
        else if (distribution.equals("Poisson")) query_type = 2;
        else  if (distribution.equals("Uniform")) query_type = 3;

        //similarly one can generate queries for any type of distribution (Exponential / Pareto / etc

        switch (query_type)
        {
            case 1:{

                Random randomGenerator = new Random();

                for (int i=0; i<noOfQueries; i++)
                {
                    int randomInt = randomGenerator.nextInt(noOfQueries-1);
                    out.write((String)queryList.get(randomInt));
                    out.write("\n");
                }
                out.close();
                break;
            }
            case 2:{
                    /*
                     * select a suitable value for mean
                     * Here 34.5 is the mean around which poisson function will generate queries.
                     */
               /* System.out.println("Give a value for poisson mean ");
                Scanner sc = new Scanner(System.in);
                double mean = sc.nextDouble();
                */
                double mean = 3;// is hardcoded at the moment, but can be changed later

                for (int i=0; i<noOfQueries; i++)
                {
                   // int poisson_next = poisson(mean);
                    PoissonDistribution p = new PoissonDistribution(mean);

                    // System.out.println("poisson_next = "+poisson_next);
                    out.write((String)queryList.get(p.sample()))   ;
                    out.write("\n");
                }
                out.close();
                break;

            }
            case 3:{
                    /*
                     * select a suitable value for lower and upper boundaries
                     * Here 2, 6  which uniform function will generate queries.
                     */
               /* System.out.println("Give a value for uniform mean ");
                Scanner sc = new Scanner(System.in);
                double mean = sc.nextDouble();
                */
                double mean = 3;// is hardcoded at the moment, but can be changed later

                for (int i=0; i<noOfQueries; i++)
                {
                    // int poisson_next = poisson(mean);
                    UniformIntegerDistribution u = new UniformIntegerDistribution(2,6);// 2 and 6 are lower and upper range
                    out.write((String)queryList.get(u.sample()))   ;
                    out.write("\n");
                }
                out.close();
                break;

            }
            default : break;
        }
        return  generatedFile;
    }// end of method generateDistributions

    /**
     * poisson distribution
     * @param mean
     * @return
     */
    private  int poisson(double mean) {
        int r = 0;

        Random random = new Random();
        double a = random.nextDouble();
        double p = Math.exp(-mean);

        while (a > p) {
            r++;
            a = a - p;
            p = p * mean / r;
        }
        return r;
    }	//end poisson

    /**
     * This method reads queries from the given queryFile
     */
    public  List readInputQuery_Subj_Pred (File file)throws IOException{
        List queries = new ArrayList();
        BufferedReader in = new BufferedReader(new FileReader(file));

        while(true){
            String line = in.readLine();
            if (line == null ) break;
            Query_Subj_Predicate qsp = createQuery(line);

            queries.add(qsp);
        }

        return queries;
    }

    /**
     * This method is to create a query from list of sqs and pqs
     * * At present, attributes are set to text type only. Integer and object type of attributes will have to be implemented in similar manner
     * The input file format specified in the queryFile with in this package should be strictly followed.
     * @param line
     * @return a Query_Subj_Pred object
     */
    public Query_Subj_Predicate createQuery(String line){

        java.util.ArrayList lsqs = new java.util.ArrayList();
        java.util.ArrayList lpqs = new java.util.ArrayList();

        String[] sub_pred = line.split("#");

        int sp = sub_pred.length;

        // subject query segments

        String[] segment = sub_pred[0].split(">");
        int noOfSegments = segment.length;

        while (noOfSegments > 0) {

            java.util.ArrayList subjectAttributes = new java.util.ArrayList();
            String table;
            String database;

            String[] tokens= segment[noOfSegments-1].split(":");

            //read attributes names
            String str = tokens[0];
            str = str.substring(1, str.length()); // skip the first character '<'
            String[] attr = str.split(";"); //';' is the delimiter for attributes from the same table

            for(int i=0; i<attr.length; i++){
                String atr = attr[i];
                subjectAttributes.add(atr);
            }

            //Reading Table name
            table = tokens[1];
            //Reading database name
            database = tokens[2];
            // add subjects to subjectQuerySegment list
            SubjectQuerySegment qs = new SubjectQuerySegment(subjectAttributes, table, database);
            lsqs.add(qs);

            noOfSegments--;
        }     // while (noOfSegments >0)

        if (sp > 1) { // now do the same with predicate query segments
            segment = sub_pred[1].split(">");
            noOfSegments = segment.length;

            while (noOfSegments > 0) {

                java.util.ArrayList predicateAttributes1 = new java.util.ArrayList();
                java.util.ArrayList predicateAttributes2 = new java.util.ArrayList();
                String table1, table2;
                String database1,database2;

                String[] tokens= segment[noOfSegments-1].split(",");
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
                        // System.out.println(str.length+"*****"+str[0]);
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

                lpqs.add(pqs);

                noOfSegments--;
            }     // while (noOfSegments >0)

        }//  if (sp > 1) -- now do the same with predicate query segments

        Query_Subj_Predicate qsp = new Query_Subj_Predicate(lsqs,lpqs);

        return qsp;
    }

    public  List readQueryExpressions (File QueryExpressionFile) throws IOException {
        List queries = new ArrayList();

        BufferedReader in = new BufferedReader(new FileReader(QueryExpressionFile));
        while(true){
            String str = in.readLine();
            if(str == null) break;
            Query_Subj_Predicate query = new Query_Subj_Predicate(str);

            queries.add(query);
        }

        return queries;
    }

    /**
     * main
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        /**
         * The input file format specified in the queryFile should be strictly followed
         */
        QueryGenerator_SubjPred qsp = new QueryGenerator_SubjPred();
        File inputFile = new File(".//src//main//java//project//QueryEnvironment//Subj_PredicateSpecificQueries//inputFile");
        File inputPredicateFile = new File(".//src//main//java//project//QueryEnvironment//Subj_PredicateSpecificQueries//inputPredicateFile");

        /* System.out.println("Random or Poisson?");
        Scanner sc = new Scanner(System.in);
        String str = sc.next();
        System.out.println("How many queries");
        int i = sc.nextInt();
        */
        File generatedFile = qsp.generateQueries(inputFile,inputPredicateFile,"Poisson",50);
        List queries = qsp.readInputQuery_Subj_Pred(generatedFile);
        Iterator itr = queries.iterator();
        while (itr.hasNext()){
            Query_Subj_Predicate q = (Query_Subj_Predicate)itr.next();
            q.printQuery();
            System.out.println();
        }

    } //end of main
}
