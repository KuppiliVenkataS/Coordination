package project.UserEnvironment;

import project.QueryEnvironment.Query_Subj_Predicate;

/**
 * Created by Santhilata on 14/08/14.
 * Last edited 26/02/2015.
 * This class provides the time taken by entire grain
 */
public class Query_ExecutionTime implements Comparable{

    private Query_Subj_Predicate query;
    private long executionTime;



    public Query_ExecutionTime(Query_Subj_Predicate query){
        this.query = query;
        this.executionTime = 0;// at the start of the query
    }

    public Query_Subj_Predicate getQuery() {
        return query;
    }

    public void setQuery(Query_Subj_Predicate query) {
        this.query = query;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Query_ExecutionTime)) return false;

        Query_ExecutionTime that = (Query_ExecutionTime) o;

        if (query != null ? !query.equals(that.query) : that.query != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return query != null ? query.hashCode() : 0;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
