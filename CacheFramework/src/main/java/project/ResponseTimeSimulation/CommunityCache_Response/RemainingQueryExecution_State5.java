package project.ResponseTimeSimulation.CommunityCache_Response;

import project.QueryEnvironment.*;
import project.UserEnvironment.QuerySegment_ExecutionTime;

import java.util.ArrayList;

/**
 * Created by santhilata on 25/03/15.
 */
public class RemainingQueryExecution_State5 {
    //STATE 5: Reply Cache on LAN

    private int startTimeState5;
    private int endTimeState5;
    private ArrayList<QuerySegment_ExecutionTime> remainingQuerySegments ;
    private ExpressionTree remainingTree;
    private double remainingDataNeeded;
    private String status;

    public RemainingQueryExecution_State5() {
        remainingQuerySegments = new ArrayList<>();
    }

    public int getStartTimeState5() {
        return startTimeState5;
    }

    public void setStartTimeState5(int startTimeState5) {
        this.startTimeState5 = startTimeState5;
    }

    public int getEndTimeState5() {
        return endTimeState5;
    }

    public void setEndTimeState5(int endTimeState5) {
        this.endTimeState5 = endTimeState5;
    }

    public String getStatus() {
        return status;
    }

    public ExpressionTree getRemainingTree() {
        return remainingTree;
    }

    public void setRemainingTree(ExpressionTree remainingTree) {
        this.remainingTree = remainingTree;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRemainingDataNeeded() {
        return remainingDataNeeded;
    }

    public void setRemainingDataNeeded(double remainingDataNeeded) {
        this.remainingDataNeeded = remainingDataNeeded;
    }

    public ExpressionTree createRemainingTree(ExpressionTree originalExpressionTree){
        remainingTree = new ExpressionTree();
        //TODO: set remaining data needed here
        return remainingTree;
    }




}
