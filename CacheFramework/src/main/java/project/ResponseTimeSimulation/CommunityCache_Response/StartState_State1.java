package project.ResponseTimeSimulation.CommunityCache_Response;

import project.MiddlewareEnvironment.QueryIndexFiles.IndexedQuery;
import project.QueryEnvironment.ExpressionTree;

/**
 * Created by santhilata on 25/03/15.
 */
public class StartState_State1 {
    //STATE 1 : start state
    int startTime; // Time at state 1
    ExpressionTree expressionTree;
    int response; // to record the response time
    int state1EndTime;
    String status = "State1";

    public  StartState_State1(){}

    public StartState_State1(int startTime) {

        this.startTime = startTime;
        System.out.println(status);
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }


    public int getState1EndTime() {
        return state1EndTime;
    }

    public void setState1EndTime(int state1EndTime) {
        this.state1EndTime = state1EndTime;
    }



    public ExpressionTree getExpressionTree() {
        return expressionTree;
    }

    public void setExpressionTree(ExpressionTree expressionTree) {
        this.expressionTree = expressionTree;
    }

    public IndexedQuery convertExpressionTreeToIndexedQuery(){
        return (new IndexedQuery(expressionTree));
    }

    public int getResponse() {
        return response;
    }

    public void setResponse(int response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int timeSpentInState1(){
        return (state1EndTime - startTime +1);
    }
}
