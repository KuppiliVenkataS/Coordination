package project.MiddlewareEnvironment.QueryIndexFiles;

/**
 * Created by santhilata on 20/02/15.
 */
public class StatusQuery {
    private  int i;
    private IndexedQuery containQuery; // part of the new query present in some querySegment of IndexedQuery in the QueryBase
    private IndexedQuery remainQuery;  // part of the new query not present in some querySegment of IndexedQuery in the QueryBase

    public StatusQuery() {
        this.i = 999;
        this.containQuery = new IndexedQuery();
        this.remainQuery = new IndexedQuery();

    }

    public void setI(int i)    {
        this.i = i;
    }

    public int getI() {
        return this.i;
    }

    public void setContainQuery(IndexedQuery cl) {
        this.containQuery = cl;
    }

    public IndexedQuery getContainQuery() {
        return this.containQuery;
    }

    public void setRemainQuery(IndexedQuery cl) {
        this.remainQuery= cl;
    }

    public IndexedQuery getRemainQuery() {
        return this.remainQuery;
    }
}// end of the class
