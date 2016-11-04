package project.ResponseTimeSimulation.CommunityCache_Response;

import project.QueryEnvironment.ExpressionTree;
import project.QueryEnvironment.PredicateQuerySegment;
import project.QueryEnvironment.Query_Subj_Predicate;
import project.QueryEnvironment.SubjectQuerySegment;

import java.util.ArrayList;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by santhilata on 25/03/15.
 */
public class DataServer_State7 {
    //STATE 7: Data server
    private int startTimeState7;
    private int endTimeState7;
    private double timeSpentInState7;
    private double remainingTimeState7;
    double dataSize;


    private String status;

    public int getStartTimeState7() {
        return startTimeState7;
    }

    public void setStartTimeState7(int startTimeState7) {
        this.startTimeState7 = startTimeState7;
    }

    public int getEndTimeState7() {
        return endTimeState7;
    }

    public void setEndTimeState7(int endTimeState7) {
        this.endTimeState7 = endTimeState7;
    }

    public double getTimeSpentInState7() {
        return timeSpentInState7;
    }

    public void setTimeSpentInState7(double timeSpentInState7) {
        this.timeSpentInState7 = timeSpentInState7;
    }

    public double getDataSize() {
        return dataSize;
    }

    public void setDataSize(double dataSize) {
        this.dataSize = dataSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRemainingTimeState7() {
        return remainingTimeState7;
    }

    public void setRemainingTimeState7(double remainingTimeState7) {
        this.remainingTimeState7 = remainingTimeState7;
    }

    public ArrayList getRemainingQuerySegments(ExpressionTree remainingTree){
        ArrayList<Query_Subj_Predicate> subQueries = remainingTree.set_GetQueriesNotFoundTree();
        ArrayList querySegments = new ArrayList();
        for (int i = 0; i < subQueries.size(); i++) {

            Query_Subj_Predicate qsp = subQueries.get(i);
            for (int j = 0; j < qsp.getSubjectQuerySegments().size(); j++) {
                SubjectQuerySegment sqs = (SubjectQuerySegment) qsp.getSubjectQuerySegments().get(j);

                querySegments.add(sqs);
            }

            //adding predicate segments
            int number = qsp.getPredicateQuerySegments().size();

            for (int j = 0; j < number; j++) {

                PredicateQuerySegment predicateQuerySegment = (PredicateQuerySegment) qsp.getPredicateQuerySegments().get(j);
                if (predicateQuerySegment.getAttribute1() != null) {
                    querySegments.add(predicateQuerySegment.getAttribute1());
                }

                if (predicateQuerySegment.getAttribute2() != null) {
                    querySegments.add(predicateQuerySegment.getAttribute2());

                }
            }
        }

        return querySegments;
    }


    public static void main(String[] args) {
        PriorityBlockingQueue<String> queue = new PriorityBlockingQueue<>();
        queue.add("a");
        queue.add("b");
        queue.add("c");
        queue.add("d");
        queue.add("e");

        ArrayList<String> as = new ArrayList<>();
        as.addAll(queue);

        for(String a:as){
            System.out.println(a);
        }

        System.out.println(queue.size());

    }
}


