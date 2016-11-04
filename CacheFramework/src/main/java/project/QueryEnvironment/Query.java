package project.QueryEnvironment;

        import jade.util.leap.ArrayList;
        import jade.util.leap.Iterator;
        import java.util.Random;

/**
 * Created by k1224068 on 02/09/14.
 * This is the user query
 *
 * DO NOT TOUCH THIS CLASS FOR CODING OPTIMIZATION
 */
public class Query {
    protected String queryID; // unique queryID which helps in aggregating smaller segments when user sends them
  //  private ArrayList querySegments = new ArrayList(); // List of query segments are the subject as well as predicates segments
    protected String queryExpression;


    public Query(){
        Random randomGenerator = new Random();
        queryID = System.currentTimeMillis()+randomGenerator.nextInt()+"";
    }

    public Query(String queryExpression) {
        Random randomGenerator = new Random();
        this.queryID = System.currentTimeMillis()+randomGenerator.nextInt()+"";
        this.queryExpression = queryExpression;
    }



    public String getQueryExpression() {
        return queryExpression;
    }

    public void setQueryExpression(String queryExpression) {
        this.queryExpression = queryExpression;
    }

    public String getQueryID() {
        return this.queryID;
    }

    public void setQueryID(String queryID) {
        this.queryID = queryID;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Query)) return false;

        Query query = (Query) o;

      //  if (queryID != null ? !queryID.equals(query.queryID) : query.queryID != null) return false;

        // following is the case for query that has only expression and neither SQS nor pQS present
        if (!(this.getQueryExpression().equals(query.getQueryExpression()))) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = queryID != null ? queryID.hashCode() : 0;
       // result = 31 * result + (querySegments != null ? querySegments.hashCode() : 0);
        return result;
    }
/*
    public void setQuerySegments(ArrayList querySegments) {
        this.querySegments = querySegments;
    }


    public void printQuery(){
        System.out.print(i +". Query ID: "+this.queryID+" ");
        i++;

        Iterator itr = querySegments.iterator();
        while (itr.hasNext()){
            QuerySegment queryS = (QuerySegment)itr.next();

            System.out.println(queryS.toString());
        }
    }
    */
}