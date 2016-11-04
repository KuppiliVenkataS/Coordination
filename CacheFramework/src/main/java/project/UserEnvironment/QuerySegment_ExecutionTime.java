package project.UserEnvironment;

import jade.content.Concept;
import jade.content.Predicate;
import project.QueryEnvironment.QuerySegment;
import project.QueryEnvironment.SubjectQuerySegment;

/**
 * Created by K.V.Santhilata on 11/02/2015.
 * This is a class to facilitate coding simplicity needed by JADE.
 * This class is useful in identifying individual execution time needed by each segment.
 */
public class QuerySegment_ExecutionTime implements Concept,Comparable{

    private SubjectQuerySegment querySegment;
    private double executionTime;

    public QuerySegment_ExecutionTime(SubjectQuerySegment querySegment) {
        this.querySegment = querySegment;
        this.executionTime = 0;
    }

    public SubjectQuerySegment getQuerySegment() {
        return querySegment;
    }

    public void setQuerySegment(SubjectQuerySegment querySegment) {
        this.querySegment = querySegment;
    }

    public QuerySegment_ExecutionTime() {
        super();
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(double executionTime) {
        this.executionTime = executionTime;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QuerySegment_ExecutionTime)) return false;

        QuerySegment_ExecutionTime that = (QuerySegment_ExecutionTime) o;

      /*  if (Double.compare(that.executionTime, executionTime) != 0) {
            System.out.println("From QSET equals: Execution time differs ");
            return false;
        }*/
        if (!querySegment.equals(that.querySegment)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = querySegment.hashCode();
        temp = Double.doubleToLongBits(executionTime);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public int compareTo(Object o) {
        return 0;
    }
}
