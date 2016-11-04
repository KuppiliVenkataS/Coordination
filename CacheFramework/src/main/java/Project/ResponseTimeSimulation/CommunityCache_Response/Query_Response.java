package project.ResponseTimeSimulation.CommunityCache_Response;

import project.QueryEnvironment.ExprTreeNode;
import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.Query_Subj_Predicate;

import java.util.ArrayList;

/**
 * Created by santhilata on 25/03/15.
 */
public class Query_Response implements  Comparable{
    //STATE 1 : start state
    private int startTime; // Time at state 1
    private int queryNumber; // query number in the supplied list of queries

    private ExpressionTree remainingQuery;
    private double dataSizeFound;
    private double remaingDataNeeded;
    private Query_Subj_Predicate QSP;
    private StartState_State1 state1;
    private QuerySentOnLAN_State2 state2;
    private CacheProcessing_State3 state3;
    private CacheResultOnLAN_State4 state4;
    private RemainingQueryExecution_State5 state5;
    private QueryTransferOnWAN_State6 state6;
    private DataServer_State7 state7;
    private DataTransferOnWAN_State8 state8;
    private ResultAggregation_State9 state9;
    private int endTime;// Time ends for a query
    private boolean foundInCache;
    private String queryLocation; // user location

    String status;


    public Query_Response(){
        this.state1 = new StartState_State1();
        this.status = state1.status;

    }
    public Query_Response(int startTime){
        this.startTime = startTime;
        this.state1 = new StartState_State1(startTime);
        this.status = state1.status;
    }

    public Query_Response(int startTime, int query){
        this.startTime = startTime;
        this.queryNumber = query;
        this.state1 = new StartState_State1(startTime);
        this.status = state1.status;
    }

    //the following constructor is used only in Simple main
    public Query_Response(int startTime, String query, String queryLocation){
        this.startTime = startTime;
        this.QSP = new Query_Subj_Predicate(query);
        this.state1 = new StartState_State1(startTime);
        this.status = state1.status;
        this.queryLocation = queryLocation;
    }


    public String getQueryLocation() {
        return queryLocation;
    }

    public void setQueryLocation(String queryLocation) {
        this.queryLocation = queryLocation;
    }

    public Query_Subj_Predicate getQSP() {
        return QSP;
    }

    public boolean isFoundInCache() {
        return foundInCache;
    }

    public void setFoundInCache(boolean foundInCache) {
        this.foundInCache = foundInCache;
    }

    public void setQSP(Query_Subj_Predicate QSP) {
        this.QSP = QSP;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getQueryNumber() {
        return queryNumber;
    }

    public void setQuery(int query) {
        this.queryNumber = query;
    }

    public StartState_State1 getState1() {
        return state1;
    }

    public void setState1(StartState_State1 state1) {
        this.state1 = state1;
    }

    public QuerySentOnLAN_State2 getState2() {
        return state2;
    }

    public void setState2(QuerySentOnLAN_State2 state2) {
        this.state2 = state2;
    }

    public CacheProcessing_State3 getState3() {
        return state3;
    }

    public void setState3(CacheProcessing_State3 state3) {
        this.state3 = state3;
    }

    public CacheResultOnLAN_State4 getState4() {
        return state4;
    }

    public void setState4(CacheResultOnLAN_State4 state4) {
        this.state4 = state4;
    }

    public RemainingQueryExecution_State5 getState5() {
        return state5;
    }

    public void setState5(RemainingQueryExecution_State5 state5) {
        this.state5 = state5;
    }

    public QueryTransferOnWAN_State6 getState6() {
        return state6;
    }

    public void setState6(QueryTransferOnWAN_State6 state6) {
        this.state6 = state6;
    }

    public DataServer_State7 getState7() {
        return state7;
    }

    public void setState7(DataServer_State7 state7) {
        this.state7 = state7;
    }

    public DataTransferOnWAN_State8 getState8() {
        return state8;
    }

    public void setState8(DataTransferOnWAN_State8 state8) {
        this.state8 = state8;
    }

    public ResultAggregation_State9 getState9() {
        return state9;
    }

    public void setState9(ResultAggregation_State9 state9) {
        this.state9 = state9;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public ExpressionTree getRemainingQuery() {
        return remainingQuery;
    }

    public void setRemainingQueryPartial(ExpressionTree remainingQuery) {
        this.remainingQuery = remainingQuery;


        this.setDataSizeFound(remainingQuery.calculateDataSizeFoundTree());

        this.setRemaingDataNeeded(remainingQuery.calculateTotalDataSizeNeededTree()-this.getDataSizeFound());
    }

    public double getRemaingDataNeeded() {
        return remaingDataNeeded;
    }

    private void setRemaingDataNeeded(double remaingDataNeeded) {
        this.remaingDataNeeded = remaingDataNeeded;
    }

    public double getDataSizeFound() {
        return dataSizeFound;
    }

    public void setRemainingQueryFull(ExpressionTree remainingQuery) {
        this.remainingQuery = remainingQuery;
        ArrayList<ExprTreeNode> someList = remainingQuery.packTreeInBFS_array();
        for(ExprTreeNode etn: someList){
            if (etn.isNodeFoundInCache()) System.out.println("From Query response full "+etn.getValue()
                    .getQueryExpression());
        }
        this.setDataSizeFound(remainingQuery.calculateTotalDataSizeNeededTree());

        this.setRemaingDataNeeded(0);
    }


    private void setDataSizeFound(double dataSizeFound) {
        this.dataSizeFound = dataSizeFound;
    }

    @Override
    public int compareTo(Object o) {
        Query_Response that = (Query_Response)o;

        if (this.startTime > that.startTime) return 1;
        if (this.startTime <that.startTime) return -1;
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Query_Response)) return false;

        Query_Response that = (Query_Response) o;

        if (startTime != that.startTime) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return startTime;
    }


}