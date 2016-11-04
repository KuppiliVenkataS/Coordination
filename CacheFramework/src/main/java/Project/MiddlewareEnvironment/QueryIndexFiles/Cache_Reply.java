package project.MiddlewareEnvironment.QueryIndexFiles;

import project.MiddlewareEnvironment.Cache;
import project.QueryEnvironment.ExpressionTree;

/**
 * Created by santhilata on 31/03/15.
 */
public class Cache_Reply {
    private String queryID;
    private String replyStatus;
    private ExpressionTree partialExpressionTree;
    private double dataSizeFound;
    private ExpressionTree remainingQuery;



    public Cache_Reply() {
    }

    public Cache_Reply(String queryID, String replyStatus, ExpressionTree partialExpressionTree, double dataSizeFound) {
        this.queryID = queryID;
        this.replyStatus = replyStatus;
        this.partialExpressionTree = partialExpressionTree;
        this.dataSizeFound = dataSizeFound;
    }

    public String getQueryID() {
        return queryID;
    }

    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }

    public String getReplyStatus() {
        return replyStatus;
    }

    public void setReplyStatus(String replyStatus) {
        this.replyStatus = replyStatus;
    }

    public ExpressionTree getPartialExpressionTree() {
        return partialExpressionTree;
    }

    public void setPartialExpressionTree(ExpressionTree partialExpressionTree) {
        this.partialExpressionTree = partialExpressionTree;
    }



    public double getDataSizeFound() {
        return dataSizeFound;
    }

    public void setDataSizeFound(double dataSizeFound) {
        this.dataSizeFound = dataSizeFound;
    }

   /* public ExpressionTree getRemainingQuery(){

    }*/


}
