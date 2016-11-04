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

import org.apache.commons.math3.distribution.UniformIntegerDistribution;

import java.util.*;

public class DistributedEnvironment {
    private DatabaseDetails[] databaseSchema;
    private int numbLANs;
    private int userGroupsPerLAN;
    private int cacheUnitsPerLAN;


    public DistributedEnvironment(){
        this.databaseSchema = createArtificialDatabaseDetails();
        this.numbLANs = 5;
        this.userGroupsPerLAN = 1;
        this.cacheUnitsPerLAN = 1;
    }

    public DistributedEnvironment(DatabaseDetails[] databaseSchema, int numbLANs, int userGroupsPerLAN, int cacheUnitsPerLAN) {
        this.databaseSchema = databaseSchema;
        this.numbLANs = numbLANs;
        this.userGroupsPerLAN = userGroupsPerLAN;
        this.cacheUnitsPerLAN = cacheUnitsPerLAN;
    }

    public DatabaseDetails[] getDatabaseSchema() {
        return databaseSchema;
    }

    public void setDatabaseSchema(DatabaseDetails[] databaseSchema) {
        this.databaseSchema = databaseSchema;
    }

    public int getNumbLANs() {
        return numbLANs;
    }

    public void setNumbLANs(int numbLANs) {
        this.numbLANs = numbLANs;
    }

    public int getUserGroupsPerLAN() {
        return userGroupsPerLAN;
    }

    public void setUserGroupsPerLAN(int userGroupsPerLAN) {
        this.userGroupsPerLAN = userGroupsPerLAN;
    }

    public int getCacheUnitsPerLAN() {
        return cacheUnitsPerLAN;
    }

    public void setCacheUnitsPerLAN(int cacheUnitsPerLAN) {
        this.cacheUnitsPerLAN = cacheUnitsPerLAN;
    }


    /**
     * =============================================================================================================================================
     * All the following methods are to create artificial attributes
     * =============================================================================================================================================
     */
    public DatabaseDetails[] createArtificialDatabaseDetails(){
        DatabaseDetails[] databaseDetails = new DatabaseDetails[10];

        for(int i=1; i <11; i++){
            databaseDetails[i-1] = new DatabaseDetails();
            databaseDetails[i-1].setDatabaseName("d"+i);
            String databaseName = databaseDetails[i-1].getDatabaseName();

            String[] tableNames = new String[10];
            String[][] attributeNames = new String[10][10];
            for (int j = 1; j <11; j++){
                //tableNames[j-1] = databaseName+"t"+j;
                tableNames[j-1] = "t"+j+i;
                String tabName = tableNames[j-1];

                for (int k=1; k <11; k++){
                   // attributeNames[j-1][k-1] = tabName+"at"+k;
                    attributeNames[j-1][k-1] = "at"+k+j+i;
                }
            }
            databaseDetails[i-1].setTables(tableNames);
            databaseDetails[i-1].setAttributes(attributeNames);
        }

        return databaseDetails;
    }



    /**
     * sqss are the subject query segments
     */
    ArrayList<String> sqss = new ArrayList<>();
    ArrayList<String> pqss = new ArrayList<>(50);
    ArrayList<String> sub_queries = new ArrayList<>();// single sub-queries
    /*
    private void  createSQSS(){
        String tempSQS = "";
        ArrayList<String> temp_sqss = new ArrayList<>();
        int databases = databaseSchema.length;


        for(int database=1; database < databases; database++) { // for all 6 databases
            int tables = databaseSchema[database].getTables().length;
            for(int table = 1; table < tables; table++){ // for 5 tables
                int attributes = databaseSchema[database].getAttributes().length;
                for (int attribute =1; attribute<attributes; attribute++){ // for all 15 attributes in each table
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
    */

    /**
     * pqss are predicate query segments that decide the number of rows
     */
    int noOfPQSS = 50;
    final String[] conditionArray = {"eq","gt","lt","gte","lte","true","false","other","between"};
    /*
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
*/
    /**
     * This method creates query segments.
     * This method returns Arraylist of queries.
     * @param
     * @return
     */
    /*
    private ArrayList<String> createQuerySegments(){
        // add subquery sequences to sub-queries.
        //shuffle
        ArrayList<String> subQueries = null;
        return subQueries;

    }// end of query segments and hence query
    */
    @Override
    public String toString(){
        String s="";

        s = s+ " Database Schema: "+databaseSchema.toString();
        s = s+ " No.of LANS= " + numbLANs;
        s = s+ " User groups per LAN: "+userGroupsPerLAN;
        s = s+ " Cache units per LAN: "+cacheUnitsPerLAN;


        return s;
    }
}
