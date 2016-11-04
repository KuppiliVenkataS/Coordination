package project.MiddlewareEnvironment.CacheMaintenance.SampleEA;

import project.MainClasses.CacheProperties;
import project.SupportSystem.MapUtil;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by santhilata on 22/06/16.
 *
 * There are three phases.
 * 1. Active cache: query request meta-data is collected at this stage
 * 2. Cache eviction: All obsolete queries are marked
 * 3. Cached data relocation: find a place for the newly cached data and existing data for relocation(if any)
 *
 * Active cache activities: data collection, data cleaning
 * data collection: During data collection, collect query Id, cache Loc, present in the cache or not, user location,
 * query complexity, and other query related information
 * Data Cleaning: generate uloc-frequency, timewindow-frequency, totalFrequency, overallRecentWindow for each query
 *
 */

public class HeuristicApproach implements CacheProperties {
    private static final Objectives objective = Objectives.RESPONSE_TIME;

    public static ArrayList<QueryData>[] testData = new ArrayList[upperTime_WindowLimit]; // this list is used to store 5 windows of incoming data

    static ArrayList<CacheUnit> communityCache = new ArrayList(cacheUnits);// List of cache units within the community cache

    ArrayList<QueryData> trainData ;// all queries sent to query analyser per one epoch
    ArrayList<String> totalPopulations = new ArrayList<>(); // total population
    ArrayList<QueryData> fitPopulation= new ArrayList<>(); // fit population
    String[] features = {"freq","time","size","loc","complex"};

    /**
     * internal class for returning two lists
     */
    class Delete_StayQueryLists{
        ArrayList<QueryData> toDeleteList;
        ArrayList<QueryData> toStayList;

        public ArrayList<QueryData> getToDeleteList() {
            return toDeleteList;
        }

        public void setToDeleteList(ArrayList<QueryData> toDeleteList) {
            if (this.toDeleteList != null) this.toDeleteList.clear();
            this.toDeleteList = toDeleteList;
        }

        public ArrayList<QueryData> getToStayList() {

            return toStayList;
        }

        public void setToStayList(ArrayList<QueryData> toStayList) {
            if (this.toStayList != null) this.toStayList.clear();
            this.toStayList = toStayList;
        }
    }

    /**
     * Multiple Output stores multiple values
     * This is only a supporting class
     */
    class MultipleOutput{
        String inputFile = ""; // inputFile
        String outputFile = ""; //output File

        double dataDeletedPerWindow;
        double dataDeletedPerFeatureString;
        double dataDeletedPerObservation; // overall average for upper window limit

        int numDeletedQueriesPerWindow ;
        int numDeletedQueriesPerFeatureString;
        int numDeletedQueriesPerObservation;

        int numReOrderingsPerWindow;
        int numReOrderingsPerFeatureString;
        int numReOrderingsPerObservation;

        int numQueriesAnsweredPerWindow;
        int numQueriesAnsweredPerFeatureString;
        int numQueriesAnsweredPerObservation;

        int numQueriesSentDBPerWindow;
        int numQueriesSentDBPerFeatureString;
        int numQueriesSentDBPerObservation;

        int numQueriesNotPlacedPerWindow;
        int numQueriesNotPlacedPerFeatureString;
        int numQueriesNotPlacedPerObservation;

        public String getInputFile() {
            return inputFile;
        }

        public void setInputFile(String inputFile) {
            this.inputFile = inputFile;
        }

        public String getOutputFile() {
            return outputFile;
        }

        public void setOutputFile(String outputFile) {
            this.outputFile = outputFile;
        }

        public double getDataDeletedPerWindow() {
            return dataDeletedPerWindow;
        }

        public void setDataDeletedPerWindow(double dataDeletedPerWindow) {
            this.dataDeletedPerWindow = dataDeletedPerWindow;
        }

        public double getDataDeletedPerFeatureString() {
            return dataDeletedPerFeatureString;
        }

        public void setDataDeletedPerFeatureString(double dataDeletedPerFeatureString) {
            this.dataDeletedPerFeatureString = dataDeletedPerFeatureString;
        }

        public double getDataDeletedPerObservation() {
            return dataDeletedPerObservation;
        }

        public void setDataDeletedPerObservation(double dataDeletedPerObservation) {
            this.dataDeletedPerObservation = dataDeletedPerObservation;
        }

        public int getNumDeletedQueriesPerWindow() {
            return numDeletedQueriesPerWindow;
        }

        public void setNumDeletedQueriesPerWindow(int numDeletedQueriesPerWindow) {
            this.numDeletedQueriesPerWindow = numDeletedQueriesPerWindow;
        }

        public int getNumDeletedQueriesPerFeatureString() {
            return numDeletedQueriesPerFeatureString;
        }

        public void setNumDeletedQueriesPerFeatureString(int numDeletedQueriesPerFeatureString) {
            this.numDeletedQueriesPerFeatureString = numDeletedQueriesPerFeatureString;
        }

        public int getNumDeletedQueriesPerObservation() {
            return numDeletedQueriesPerObservation;
        }

        public void setNumDeletedQueriesPerObservation(int numDeletedQueriesPerObservation) {
            this.numDeletedQueriesPerObservation = numDeletedQueriesPerObservation;
        }

        public int getNumReOrderingsPerWindow() {
            return numReOrderingsPerWindow;
        }

        public void setNumReOrderingsPerWindow(int numReOrderingsPerWindow) {
            this.numReOrderingsPerWindow = numReOrderingsPerWindow;
        }

        public int getNumReOrderingsPerFeatureString() {
            return numReOrderingsPerFeatureString;
        }

        public void setNumReOrderingsPerFeatureString(int numReOrderingsPerFeatureString) {
            this.numReOrderingsPerFeatureString = numReOrderingsPerFeatureString;
        }

        public int getNumReOrderingsPerObservation() {
            return numReOrderingsPerObservation;
        }

        public void setNumReOrderingsPerObservation(int numReOrderingsPerObservation) {
            this.numReOrderingsPerObservation = numReOrderingsPerObservation;
        }

        public int getNumQueriesAnsweredPerWindow() {
            return numQueriesAnsweredPerWindow;
        }

        public void setNumQueriesAnsweredPerWindow(int numQueriesAnsweredPerWindow) {
            this.numQueriesAnsweredPerWindow = numQueriesAnsweredPerWindow;
        }

        public int getNumQueriesAnsweredPerFeatureString() {
            return numQueriesAnsweredPerFeatureString;
        }

        public void setNumQueriesAnsweredPerFeatureString(int numQueriesAnsweredPerFeatureString) {
            this.numQueriesAnsweredPerFeatureString = numQueriesAnsweredPerFeatureString;
        }

        public int getNumQueriesAnsweredPerObservation() {
            return numQueriesAnsweredPerObservation;
        }

        public void setNumQueriesAnsweredPerObservation(int numQueriesAnsweredPerObservation) {
            this.numQueriesAnsweredPerObservation = numQueriesAnsweredPerObservation;
        }

        public int getNumQueriesSentDBPerWindow() {
            return numQueriesSentDBPerWindow;
        }

        public void setNumQueriesSentDBPerWindow(int numQueriesSentDBPerWindow) {
            this.numQueriesSentDBPerWindow = numQueriesSentDBPerWindow;
        }

        public int getNumQueriesSentDBPerFeatureString() {
            return numQueriesSentDBPerFeatureString;
        }

        public void setNumQueriesSentDBPerFeatureString(int numQueriesSentDBPerFeatureString) {
            this.numQueriesSentDBPerFeatureString = numQueriesSentDBPerFeatureString;
        }

        public int getNumQueriesSentDBPerObservation() {
            return numQueriesSentDBPerObservation;
        }

        public void setNumQueriesSentDBPerObservation(int numQueriesSentDBPerObservation) {
            this.numQueriesSentDBPerObservation = numQueriesSentDBPerObservation;
        }

        public int getNumQueriesNotPlacedPerWindow() {
            return numQueriesNotPlacedPerWindow;
        }

        public void setNumQueriesNotPlacedPerWindow(int numQueriesNotPlacedPerWindow) {
            this.numQueriesNotPlacedPerWindow = numQueriesNotPlacedPerWindow;
        }

        public int getNumQueriesNotPlacedPerFeatureString() {
            return numQueriesNotPlacedPerFeatureString;
        }

        public void setNumQueriesNotPlacedPerFeatureString(int numQueriesNotPlacedPerFeatureString) {
            this.numQueriesNotPlacedPerFeatureString = numQueriesNotPlacedPerFeatureString;
        }

        public int getNumQueriesNotPlacedPerObservation() {
            return numQueriesNotPlacedPerObservation;
        }

        public void setNumQueriesNotPlacedPerObservation(int numQueriesNotPlacedPerObservation) {
            this.numQueriesNotPlacedPerObservation = numQueriesNotPlacedPerObservation;
        }
    }

    //____________________________DATA COLLECTION: ACTIVE CACHE_____________________________

    /**
     * Following test data is generated for five epochs
     * During active cache phase, only meta-data about the query workload during that time period is collected
     * @param numTimeWindows
     */
    public ArrayList<QueryData>[] createEpochWiseTestData(int numTimeWindows){
        for (int twi = 0; twi < numTimeWindows; twi++) {

            ArrayList<QueryData> queryArr = new ArrayList<>();
            for (int qid = 0; qid < num_Queries; qid++) {
                QueryData qf = new QueryData(""+qid);
                //qf.setOverallTimeRecent(twi);
                queryArr.add(qf);
            }
            testData[twi] = queryArr;
        }

        return testData;
    }

    /**
     * Creates standard input files
     * @throws IOException
     */
    public void writeQueryFiles() throws IOException {

        testData = createEpochWiseTestData(upperTime_WindowLimit);
        File[] inputFiles = new File[upperTime_WindowLimit];
        for (int twi = 0; twi < upperTime_WindowLimit; twi++) {
            File input = new File("/home/santhilata/Desktop/Input/input" + twi + ".csv");
            FileWriter fw = new FileWriter(input);
            ArrayList<QueryData> queryArr = (ArrayList<QueryData>) testData[twi].clone();

            Iterator<QueryData> itrQd = queryArr.iterator();
            while (itrQd.hasNext()){
                QueryData qf = itrQd.next();

                fw.write(qf.getQid()+","+qf.getQueryComplexity()+","+qf.cLoc+",");
                for (int i = 0; i < qf.getcLocRecommend().length; i++) {
                    fw.write(qf.getcLocRecommend()[i]+",");
                }
                fw.write(qf.getQuerySize()+","+qf.getRefresh()+",");;

                Set<String> keySet = qf.getuLoc_frequency().keySet();
                Iterator<String> iter1 = keySet.iterator();
                while (iter1.hasNext()){
                    String item1 = iter1.next();

                    fw.write(item1+","+qf.getuLoc_frequency().get(item1)+",");
                }

                fw.write(qf.getTotalFreq()+","+qf.getFreq_5windows()+",");

                Set<String> keySet1 = qf.getcLoc_time().keySet();

                Iterator<String> iter11 = keySet1.iterator();
                while (iter11.hasNext()){
                    String item1 = iter11.next();
                    System.out.println(item1+","+qf.getcLoc_time().get(item1));
                    fw.write(item1+","+qf.getcLoc_time().get(item1)+",");
                }

                fw.write(qf.timeRecent+","+qf.foundInCache+"\n");
            }

            fw.close();

        }
    }

    public ArrayList<QueryData> readDataFromFile(File file) throws IOException {
        ArrayList<QueryData> traindata = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while((line = br.readLine())!= null){
            String[] tokens = line.split(Pattern.quote(","));
            int i =0;
            QueryData queryData = new QueryData();
            queryData.setqId(tokens[i++]);
            queryData.setQueryComplexity(Integer.parseInt(tokens[i++]));
            queryData.setcLoc(tokens[i++]);


            queryData.cLocRecommend = new String[cacheUnits];
            for (int j= 0; j < cacheUnits ; j++) {
                queryData.cLocRecommend[j] = tokens[i++];
            }
            queryData.setQuerySize(Double.parseDouble(tokens[i++]));
            queryData.setRefresh(tokens[i++]);
            queryData.uLoc_frequency = new HashMap<>();

            for (int j = 0; j < numUserContainers; j++) {
                String str = tokens[i++];
                queryData.uLoc_frequency.put(str,Integer.parseInt(tokens[i++]));

            }


            queryData.totalFreq = Integer.parseInt(tokens[i++]);
         //   System.out.println(queryData.totalFreq);
            queryData.setFreq_5windows(Integer.parseInt(tokens[i++]));
          //  System.out.println(queryData.getFreq_5windows());
            queryData.cLoc_time = new HashMap<>();

            queryData.setTimeRecent(Integer.parseInt(tokens[i++]));
            if (tokens[i].equals("true")) queryData.setFoundInCache(true);
            else queryData.setFoundInCache(false);

           // System.out.println(queryData.isFoundInCache());

            traindata.add(queryData);
        }

        br.close();
        return traindata;
    }

    //TODO:incomplete
    public void printInput(){
        System.out.println("     Query input      ");
        System.out.println("----------------------");
        for (int twi = 0; twi < upperTime_WindowLimit; twi++) {
            System.out.println("   Time window - "+twi);
            System.out.println("----------------------");
            ArrayList<QueryData> queryArr = (ArrayList<QueryData>)testData[twi].clone();

            Iterator<QueryData> itrQd = queryArr.iterator();
            while (itrQd.hasNext()){
                QueryData qf = itrQd.next();
                System.out.println("qid = "+qf.getQid()+"  "+qf.getuLoc_frequency()+" totalFreq="+qf.totalFreq);

            }
        }

    }

    /**
     * Set up cache units
     */
    public void setupCacheUnits(){
        communityCache.clear();
        for (int i = 0; i < cacheUnits; i++) {
            CacheUnit cu = new CacheUnit(""+(i+1));
            communityCache.add(cu);
        }
    }

    private CacheUnit getCacheUnit(String name){
        for (int i = 0; i < communityCache.size(); i++) {
            if (name.equals(communityCache.get(i).getcName()))
                return communityCache.get(i);
        }

        return null;
    }

    private boolean isQueryInCommunityCache(QueryData qd){

        Iterator<CacheUnit> cuItr = communityCache.iterator();
        while (cuItr.hasNext()){
            CacheUnit cu = cuItr.next();
            if (cu.isCacheContainsQuery(qd))
                return  true;
        }

        return false;
    }

    public void printCacheInstance(){
        for (CacheUnit cu: communityCache) {

            Iterator<QueryData> qdItr = cu.residentQueries.iterator();
            while (qdItr.hasNext()){
                QueryData qd = qdItr.next();
                System.out.println(qd.getQid()+" "+qd.getcLoc());
            }
        }
    }

    /**
     * ______________CACHE MAINTENANCE: CACHE EVICTION ___________________________________________
     *
     * During this phase, obsolete queries are removed
     * and,
     * all queries epoch-wise selected, tested for suitability for caching from this window
     */

    /**
     * The following function only marks queries and adds to the toDeletedQuery list
     * But not deletes from each of the cache unit.
     * Complete deletion has to be done from the application method
     * checks with overall frequency and then time recently used
     *
     * Provision is made to check how many windows of data to be considered for the marking
     * Total frequency for each query will be considered for marking
     *
     */
    private Delete_StayQueryLists mark_delete_ObsoleteQueries(int numWindows){
        Delete_StayQueryLists dsl = new Delete_StayQueryLists();
        int freq_5windowsThreshold = 4*numWindows; // frequency threshold for the past 5 windows
        ArrayList<QueryData> toDeleteQuery = new ArrayList<>();
        ArrayList<QueryData> toStayQuery = new ArrayList<>();//all queries that stay

        for (int cn = 0; cn < communityCache.size(); cn++) {
            ArrayList<QueryData> qd = communityCache.get(cn).getResidentQueries();

            Iterator<QueryData> itr = qd.iterator(); // for each query sent to the query analyser

            while (itr.hasNext()) {

                QueryData qf = itr.next();

                if ( (qf.getFreq_5windows() < freq_5windowsThreshold) ||
                   // if(
                            (qf.timeRecent > time_threshold)  ) { // obsolete ones
                    //System.out.println("from delete obsolete queries "+qf);
                    toDeleteQuery.add(qf);
                }
                else{// non-obsolete queries

                    qf.setCloc_time(qf.getcLoc(), qf.cLoc_time.get(qf.getcLoc()) + 1);
                    qf.setOverallTimeRecent();
                    qf.setTotalFreq(); // why this?

                    toStayQuery.add(qf);
                }

                //TODO : query complexity
            }

        }//for each community cache unit

        dsl.setToDeleteList(toDeleteQuery);
        dsl.setToStayList(toStayQuery);

        return dsl;
    }

    /**
     * This function takes care of the incrementing frequency
     * for queries that were not frequent in this window
     * @param window
     */
    private ArrayList<QueryData> increaseFreq(int window, ArrayList<QueryData> toStayQuery){

        for (QueryData qTemp:toStayQuery ) {

            for (QueryData qd:trainData ) {

                if (qTemp.getQid().equals(qd.getQid())){
                  //  System.out.println("from increase freq "+qd+" stay query "+qTemp);
                    HashMap<String,Integer> toStayUloc_freq = qTemp.getuLoc_frequency();
                    HashMap<String,Integer> trainDataULoc_freq = qd.getuLoc_frequency();

                    Set<String> keySet = toStayUloc_freq.keySet();
                    Iterator<String> itrKey = keySet.iterator();

                    while (itrKey.hasNext()){
                        String key = itrKey.next();
                        toStayUloc_freq.put(key,toStayUloc_freq.get(key) + trainDataULoc_freq.get(key));

                    }
                    qTemp.setTotalFreq();
                    break;
                }
            }
        }

        return toStayQuery;

    }

    /**
     * This method is to set the query found status
     * If the query is found in the cache, query becomes the resident query.
     * So all meta characteristics such as frequency etc are set to resident query's
     */
    private void setQueryFound(int window){

      //  ArrayList<QueryData>trainData = (ArrayList<QueryData>)testData[window].clone(); // train data contains the given window data
        Iterator<QueryData> itr = trainData.iterator(); // for each query sent to the query analyser

        while(itr.hasNext()) {
            QueryData qf = itr.next();// new query
            //System.out.println(" from set query found "+qf);
            boolean flag = false;
            Iterator<CacheUnit> iterCache = communityCache.iterator();
            while(iterCache.hasNext()){
                ArrayList<QueryData> residentQueries = iterCache.next().residentQueries;

                for (int i = 0; i < residentQueries.size() ; i++) {
                    QueryData tempQuery = residentQueries.get(i); //for each resident query

                    if (qf.getQid().equals(tempQuery.getQid())){
                        //System.out.println("from set query found "+tempQuery);
                        qf.setFoundInCache(true);

                        //tempQuery.setuLoc_frequency( qf.getuLoc_frequency());//why?

                        qf.setcLoc(tempQuery.getcLoc());
                        qf.setcLocRecommend(tempQuery.getcLocRecommend());
                        qf.cLoc_time =tempQuery.getcLoc_time();
                        tempQuery.setTimeRecent(1);
                        qf.timeRecent = tempQuery.timeRecent;

                        qf.addToFreq_5windows(tempQuery.getFreq_5windows());
                        qf.setQuerySize(tempQuery.getQuerySize());
                        qf.setQueryComplexity(tempQuery.getQueryComplexity());
                        flag = true;
                        break;
                    }
                }
                if (flag) break;
            }
        }
    }

    /**
     * The following function identifies  queries to be cached & adds to the toAddQuery arraylist from the current window
     * At the moment algorithm is to add only from current window if frequency > threshold).
     * (Future addition: Observing over past few windows and if frequency is near threshold, add them)
     *
     * But does not add them to the cache unit here.
     * Adding will be done after finding an appropriate place
     *
     * @param window
     */
    public ArrayList<QueryData> mark_queriesForCaching(int window){

        ArrayList<QueryData> toAddQuery = new ArrayList<>();
        Iterator<QueryData> itr = trainData.iterator(); // for each query sent to the query analyser
        while(itr.hasNext()) {

            QueryData qf = itr.next();

            if (qf.totalFreq >= freq_threshold) {
                QueryData qf1 =  new QueryData(qf);
                toAddQuery.add(qf1);
            }
        }

        return  toAddQuery;
    }

    /**_____________CACHE MAINTENANCE: DATA PLACEMENT FOR NEW QUERIES AND CACHED QUERIES________________________________
     *
     * This phase has several functions
     * 1.dataPlacement - to find a place to store data
     *
     * 2. private functions- to obtain populations
     *
     */
    public void placeData() throws IOException {

        MultipleOutput multipleOutput = new MultipleOutput();

        String inputFile = "";
        String outputFileString ="/home/santhilata/Desktop/Output/ft_tf_EA1.csv";

        File outputfile = new File(outputFileString);
        FileWriter fw = new FileWriter(outputfile);
        fw.flush();

        population(featureStr);



        double[][] av_total_cost_5windows = new double[totalPopulations.size()][5]; //average cost for all 5 windows for each feature string
        double[][] av_total_cost_1window = new double[totalPopulations.size()][upperTime_WindowLimit]; // av cost of current window for each of the feature strings

        for (int run = 0; run<5; run++)
        {

            //Step 1A:  set epochwise data
           // createEpochWiseTestData(upperTime_WindowLimit); // create 5 windows worth data

            int countNoPlaceQueries = 0;
            int populationInt =  0;

            for (int i = 0; i <totalPopulations.size(); i++) {
                String featureString = totalPopulations.get(i);
                av_total_cost_5windows[populationInt][run] = 0.0;
                //Step 1B: Set up environment
                setupCacheUnits();//set up cache units freshly for each string
                int countNoplaceFeature=0;

                double dataDeleted = 0.0;

                for (int twi = 0; twi < upperTime_WindowLimit; twi++) {

                    av_total_cost_1window[populationInt][twi] = 0.0;
                    int queries = 0;

                   // trainData = (ArrayList<QueryData>) testData[twi].clone();
                    trainData = readDataFromFile(new File("/home/santhilata/Desktop/Input/input" +twi + ".csv"));

                    for (int j = 0; j < trainData.size() ; j++) {
                        queries += trainData.get(j).getTotalFreq();

                    }

                    //Step 2: Delete obsolete queries to make space
                    Delete_StayQueryLists dsl = mark_delete_ObsoleteQueries(twi);
                    ArrayList<QueryData> toDeleteQuery = dsl.getToDeleteList();

                    for (QueryData qTemp : toDeleteQuery) {
                        getCacheUnit(qTemp.getcLoc()).deleteQuery(qTemp);//delete from the cache unit and add size
                        dataDeleted += qTemp.getQuerySize();
                    }

                    //Step2a: increase frequency to all queries that were not crossed frequency limit but in cache
                    ArrayList<QueryData> toStayQuery = dsl.getToStayList();
                    toStayQuery = increaseFreq(twi,toStayQuery);


                    //Step2b: Set query found flag if query is stored in cache
                    setQueryFound(twi);

                    //Step3 : Get all queries > threshold
                    //creates a new query from train data and adds to residential queries
                    ArrayList<QueryData> toAddQuery = mark_queriesForCaching(twi);

                    /**
                     * Step 4: whether found in cache, if found which cache loc?
                     * where to locate existing / first time cache data
                     *
                     * if a query is a candidate for first location;
                     * static algorithm: Size (user location - frequency) in the descending order                     *
                     * if a query is a candidate for relocation:
                     * multiple sequences
                     */

                    int firstTimeQueries =0;
                    int relocateQueries = 0;
                    int toAddqueries = 0;


                    Iterator<QueryData> iter = toAddQuery.iterator();

                    while (iter.hasNext()) { // for each query to be added in toAddQuery list
                        double Query_totalcost = 0.0;

                        QueryData qdTemp = iter.next();
                        toAddqueries += qdTemp.totalFreq;

                        // FIRST PLACEMENT OF QUERIES
                        if (!qdTemp.isFoundInCache()) {// not found in any of the cache units
                            //first time adding to any cache unit - this is a standard cost

                            Query_totalcost += cost_per_query;

                            // candidate for first location
                            String location = findFirstPlacement_ResponseTime(qdTemp); // gets the location recommendation

                            try {
                                CacheUnit cuTemp = getCacheUnit(location);

                                firstTimeQueries += qdTemp.totalFreq;
                                qdTemp.setcLoc(location); // set this query's location as the recommended place
                                qdTemp.setCloc_time(location, 1);
                                qdTemp.setOverallTimeRecent();

                                qdTemp.setFreq_5windows(qdTemp.totalFreq);
                                getCacheUnit(location).addQuery(qdTemp); // add to community cache and adjust available size

                                Iterator<String> ulocKeyiter = qdTemp.getuLoc_frequency().keySet().iterator();

                                while (ulocKeyiter.hasNext()) {
                                    String key = ulocKeyiter.next();

                                    if (!location.equals(key)) {
                                        Query_totalcost += qdTemp.getuLoc_frequency().get(key) * cache_To_cacheDistance;// all cache units at unit distance(10.0) to each other
                                    }
                                }
                               // System.out.println("first location  "+location+" "+qdTemp);
                            }
                            catch (NullPointerException ne){
                                countNoPlaceQueries++;
                                countNoplaceFeature+= qdTemp.totalFreq;
                                System.out.println("quitting due to no place in this world");
                            }
                        }

                        else {  // candidate for relocation

                            findRelocationPlace_ResponseTime(qdTemp, featureString);
                            String existLoc = qdTemp.getcLoc();

                            boolean dataSizeRestriction = true;

                            // For each of the recommendations given by the feature string,
                            // select a place where there is enough space is available
                            for (int j = 0; j < featureString.length(); j++) {
                                String relocate = qdTemp.cLocRecommend[j];

                                if (qdTemp.getQuerySize() <= getCacheUnit(relocate).getAvailableSize()) {

                                    relocateQueries += qdTemp.totalFreq;
                                    if (!relocate.equals(qdTemp.getcLoc())) { // some location other than the current one

                                        boolean done = getCacheUnit(existLoc).deleteQuery(qdTemp);

                                        qdTemp.setcLoc(relocate);
                                        qdTemp.setCloc_time(relocate, 1);
                                        qdTemp.setOverallTimeRecent();

                                        qdTemp.setFreq_5windows(qdTemp.getFreq_5windows() + qdTemp.totalFreq);
                                        getCacheUnit(relocate).addQuery(qdTemp);

                                    }
                                    dataSizeRestriction  = false;
                                    break; // ensures the order of feature string
                                }

                            }

                            if (dataSizeRestriction == false){
                                // to calculate the data transfer costs for all this query
                                Iterator<String> ulocKeyiter = qdTemp.getuLoc_frequency().keySet().iterator();
                                while (ulocKeyiter.hasNext()) {
                                    String key = ulocKeyiter.next();

                                    if (!existLoc.equals(key)) {
                                        Query_totalcost += qdTemp.getuLoc_frequency().get(key) * cache_To_cacheDistance;// all cache units at unit distance(10.0) to each other
                                    }
                                }

                            }

                            else{

                                countNoPlaceQueries++;
                                countNoplaceFeature+= qdTemp.totalFreq;
                            }
                        }
                        av_total_cost_1window[populationInt][twi] += Query_totalcost;
                      //  System.out.println(av_total_cost_1window[populationInt][twi]);

                    }

                    av_total_cost_5windows[populationInt][run]+=  av_total_cost_1window[populationInt][twi]/queries;
                   // System.out.println(";;;;;;;;;;;;;;;;;;;;;;;"+twi+";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;");

                }//for each window

                dataDeleted = 0.0;
                populationInt++;

                System.out.println("*****************"+featureString+"******************");
            }// for each feature String


            System.out.println("================="+run+"======================");

        }//for each variation of num_Queries

        fw.write("Feature string,");
        for (int run = 0; run < 5; run++) {
            fw.write(run+",");
        }
        fw.write("\n");
        for (int j = 0; j <totalPopulations.size();j++) {
            fw.write(totalPopulations.get(j)+",");
            double avOfAv = 0.0;
            for(int run = 0; run < 5; run++) {
              //  System.out.println("YYYYYYYYYYYYYYYY   "+av_total_cost_5windows[j][run]);
                avOfAv += av_total_cost_5windows[j][run];
                fw.write((av_total_cost_5windows[j][run])+",");
            }
            fw.write(avOfAv/5+"\n");
           // fw.write("\n");
        }



        fw.close();
    }

    /**
     * When query to be allocated at a place for the first time
     * it should be done according to the criteria : global nearby, data size, frequency descending order
     * @param qd
     * @return cacheLocation
     */
    private String findFirstPlacement_ResponseTime(QueryData qd){
        String cacheLoc = "";

        Map<String, Integer> userFreq = MapUtil.sortByValue(qd.getuLoc_frequency());

        //System.out.println("userFreq "+userFreq.size()+" "+qd.getuLoc_frequency().size());
        Object[] uLocArray =  userFreq.keySet().toArray();// these objects are strings


        for (int i = 0; i < uLocArray.length; i++) {
            String uLoc = (String)uLocArray[i];
           // System.out.println( uLocArray.length+" available size  "+ getCacheUnit(uLoc).availableSize + " qd.getQuerySize "+qd.getQuerySize());
            if (getCacheUnit(uLoc).availableSize >= qd.getQuerySize()) {
                return uLoc; // cloc == uloc
            }

        }

        return cacheLoc;
    }

    private void findRelocationPlace_ResponseTime(QueryData qf, String featureString){

        char[] chAr = featureString.toCharArray();
        RuleTree rt = new RuleTree(chAr); // RuleTree is a class
        Node curr = rt.getRoot();

        int recommendPos = 0; // position in the locRecommend array

        /**
         * Each of the rule recommends a location for the query data segment at recommendPos.
         * This is the implementation of the decision tree.
         * Maximum information gain is obtained by the best feature is chosen first
         */

        while (true){
            boolean flag = false;
            String ch = curr.getRule();
            switch (ch) {
            /* Rules according to frequency */
                case "f" : {
                    Map tMap = MapUtil.sortByValue(qf.getuLoc_frequency());//descending frequencies
                    qf.cLocRecommend[recommendPos] = (String) tMap.keySet().toArray()[0];
                    flag = true;
                }
                break;

            /* Rules according to time windows */
                case "t" : {
                    Map tMap = MapUtil.sortByValueAscending(qf.cLoc_time);
                    qf.cLocRecommend[recommendPos] = (String) tMap.keySet().toArray()[0];
                    flag = true;
                }
                break;

                /**
                 *  Rules according to query complexity
                 *  TODO: complexity should be in line with other queries having higher associations
                 */
                case "c" : {
                    if (qf.getQueryComplexity() >= 2) {// not changing as query is heavy

                        qf.cLocRecommend[recommendPos] = qf.cLoc;
                    }
                    else {

                        double smallest = maxCache_size;
                        String smallName = "";
                        for (CacheUnit cu :
                                communityCache) {
                            if ( cu.getAvailableSize() < smallest) {
                                smallest = cu.getAvailableSize();
                                smallName = cu.getcName();
                            }

                        }
                        qf.cLocRecommend[recommendPos] = smallName;
                    }
                    flag = true;
                }
                break;

             /* Rules according to data size */
                case "s": {
                    if (qf.getQuerySize() < (upperDataSize-50)){// go by uloc - frequency
                        Map tMap = MapUtil.sortByValue(qf.getuLoc_frequency());//descending frequencies
                        qf.cLocRecommend[recommendPos] = (String) tMap.keySet().toArray()[0];

                    }
                    else{
                        qf.cLocRecommend[recommendPos] = qf.cLoc; //no change for larger queries
                    }
                    flag = true;
                }
                break;

            /* Rules according to cache placement */
                case "l" : {


                }
                break;

                /* cache unit size */
                case "q" : {
                    double maxLimit = 0.0;
                    CacheUnit max =null ;
                    Iterator<CacheUnit> iterCache = communityCache.iterator();
                    while(iterCache.hasNext()){
                        CacheUnit cu = iterCache.next();
                        if (cu.getAvailableSize() > maxLimit){
                            maxLimit = cu.getAvailableSize();
                            max = cu;
                        }
                    }

                    qf.cLocRecommend[recommendPos] = max.getcName();

                    flag = true;
                }
                break;

                /* Data usability of the current query with other queries*/
                case "u" :{
                    double usability = (freq_threshold - qf.totalFreq);

                }
                break;

                /* MTCD */
                case "m" :{
                    double minLimit = 99999.0;
                    CacheUnit min =null ;
                    Iterator<CacheUnit> iterCache = communityCache.iterator();
                    while(iterCache.hasNext()){
                        CacheUnit cu = iterCache.next();
                        if (cu.getAvailableSize() < minLimit){
                            minLimit = cu.getAvailableSize();
                            min = cu;
                        }
                    }

                    qf.cLocRecommend[recommendPos] = min.getcName();

                    flag = true;
                }
                break;

                default: {
                    qf.cLocRecommend[recommendPos] = qf.cLoc; // no change

                    flag = true;
                }
                break;
            }

            if (flag) curr = rt.getNextNode(curr);
            recommendPos++;
            if (curr != null ) { //System.out.println();
            }
            else break;
        }
    }

    /**
     * Following two methods are to generate multiple permutations of feature list
     * the following list generates population list
     * @param str
     */
    private  void population(String str) {
        population("", str);
    }
    private  void population(String prefix, String str) {
        int n = str.length();
        if (n == 0) {
            totalPopulations.add(prefix);
            //System.out.println(prefix);
        }
        else {
            for (int i = 0; i < n; i++)
                population(prefix + str.charAt(i), str.substring(0, i) + str.substring(i+1, n));
        }
    }

    /**
     * This takes the population list and removes all combinations that does not cross
     * fitness criteria
     */
    private void extractFitPopulation(){
        Iterator<String> itr = totalPopulations.iterator();

        while(itr.hasNext()){
            char[] featureOrder = itr.next().toCharArray();
            for (int i = 0; i < featureOrder.length ; i++) {
                //build tree
            }

        }
    }

    /**
     * cost function for minimum response time
     */
    private double costFunction(){
        double totalCost = 0;
        switch (objective){
            case RESPONSE_TIME:{
                totalCost = AVERAGE_CACHE_LATENCY + average_DataServer_ProcTime;

                for (int i = 0; i < num_Queries ; i++) {

                }

            }
            break;

            case DATA_TRANSFER:{

            }
            break;
/*
            case OVERALL_COSTS:{
                for (int i = 0; i < num_Queries ; i++) {

                    QueryData tempQF = getQuery(i+"",);
                    double distance = (tempQF == null) ? cacheUnitDistance:dataStoreDistance;
                    totalCost += calculateTransferTime(distance, tempQF.getQuerySize());
                }

            }
            break;
            */
        }
        return  totalCost;
    }

    /**
     * This method finds the cost of finding cached data using different feature strings
     * @param window
     */
    public void costCompare(int window){

    }

    /**
     * @param distance = distance from user location to data store / cache unit
     * @param dataSize = size of the query needed to be transfered
     * @return
     */
    private double calculateTransferTime(double distance, double dataSize){
        return  distance*dataSize*networkCostPerGB;
    }

    private QueryData getQuery(String qid, ArrayList<QueryData>trainData){
        QueryData qf = null;

        for (int i = 0; i < trainData.size() ; i++) {
            if (qid.equals(trainData.get(i).getQid())){
                qf = trainData.get(i);
                break;
            }
        }

        return qf;
    }

    private ArrayList<QueryData> copyArrayList(ArrayList<QueryData> original)  {
        ArrayList<QueryData> newList = new ArrayList<>();

        for (int i = 0; i < original.size(); i++) {
            QueryData qd_new = new QueryData(original.get(i));
            newList.add(qd_new);
        }

        return newList;
    }


    //____________________________ MAIN METHOD ___________________________________________________

    public static void main(String[] args) throws IOException {
        HeuristicApproach ea = new HeuristicApproach();
         ea.createEpochWiseTestData(upperTime_WindowLimit); // create 5 windows worth data
        // ea.setupCacheUnits();
        // ea.printInput();

        ea.writeQueryFiles();
        ea.placeData();




    }
}
