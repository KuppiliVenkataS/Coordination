package project.ResponseTimeSimulation.Cache_DataServerSimulation;

import project.QueryEnvironment.Query_Subj_Predicate;

/**
 * Created by santhilata on 11/5/15.
 */
public class Query_Response_DataServer implements  Comparable {
    private int startTime; // Time at state 1
    private int queryNumber; // query number in the supplied list of queries
    private  int endTime;

    private String status="State1";

    private Query_Subj_Predicate QSP;
    private UserInput_State1 state1;
    private QueryTransferOnWAN_State2 state2;
    private Cache_State3 state3;
    private LANQuerytransfer_State4 state4;
    private InterWANDataTransfers_State5 state5;
    private ReplyUser_State6 state6;



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public void setQueryNumber(int queryNumber) {
        this.queryNumber = queryNumber;
    }

    public Query_Subj_Predicate getQSP() {
        return QSP;
    }

    public void setQSP(Query_Subj_Predicate QSP) {
        this.QSP = QSP;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public UserInput_State1 getState1() {
        return state1;
    }

    public void setState1(UserInput_State1 state1) {
        this.state1 = state1;
    }

    public QueryTransferOnWAN_State2 getState2() {
        return state2;
    }

    public void setState2(QueryTransferOnWAN_State2 state2) {
        this.state2 = state2;
    }

    public Cache_State3 getState3() {
        return state3;
    }

    public void setState3(Cache_State3 state3) {
        this.state3 = state3;
    }

    public LANQuerytransfer_State4 getState4() {
        return state4;
    }

    public void setState4(LANQuerytransfer_State4 state4) {
        this.state4 = state4;
    }

    public InterWANDataTransfers_State5 getState5() {
        return state5;
    }

    public void setState5(InterWANDataTransfers_State5 state5) {
        this.state5 = state5;
    }

    public ReplyUser_State6 getState6() {
        return state6;
    }

    public void setState6(ReplyUser_State6 state6) {
        this.state6 = state6;
    }

    @Override
    public int compareTo(Object o) {
        Query_Response_DataServer that = (Query_Response_DataServer)o;

        if (this.startTime > that.startTime) return 1;
        if (this.startTime <that.startTime) return -1;
        return 0;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Query_Response_DataServer that = (Query_Response_DataServer) o;

        return startTime == that.startTime;

    }

    @Override
    public int hashCode() {
        return startTime;
    }
}
