package project.ResponseTimeSimulation.Cache_DataServerSimulation;

import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.Query_Subj_Predicate;

/**
 * Created by santhilata on 11/5/15.
 * Takes user input, fragments the query according to the database
 *
 */
public class UserInput_State1 {

    int state1_Starttime;
    int state1_EndTime;
    int remainingTime;
    String status = "State1";

   // Query_Subj_Predicate QSP ;
    ExpressionTree expressionTree;

    public UserInput_State1(){}
    public UserInput_State1(int startTime){
        this.state1_Starttime = startTime;
        System.out.println(status);
    }

    public int getState1_Starttime() {
        return state1_Starttime;
    }

    public void setState1_Starttime(int state1_Starttime) {
        this.state1_Starttime = state1_Starttime;
    }

    public int getState1_EndTime() {
        return state1_EndTime;
    }

    public void setState1_EndTime(int state1_EndTime) {
        this.state1_EndTime = state1_EndTime;
    }

    public String getStatus() {
        return status;
    }

    public ExpressionTree getExpressionTree() {
        return expressionTree;
    }

    public void setExpressionTree(ExpressionTree expressionTree) {
        this.expressionTree = expressionTree;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }
}
