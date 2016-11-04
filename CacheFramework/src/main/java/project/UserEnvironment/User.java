package project.UserEnvironment;

import project.MiddlewareEnvironment.Container;
import project.ResponseTimeSimulation.CommunityCache_Response.Query_Response;

/**
 * Created by santhilata on 6/4/15.
 */
public class User {
    private String userID;
    private Container myContainer;
    private Query_Response myQuery;

    private static int id=1;

    public User() {
        this.userID ="User"+id;
        id++;
    }

    public User( Container myContainer,Query_Response userQuery) {
        this.userID ="User"+id;
        id++;
        this.myContainer = myContainer;
        this.myQuery =  userQuery;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        userID = userID;
    }

    public Query_Response getUserQuery() {
        return myQuery;
    }

    public void setUserQuery(Query_Response userQuery) {
        this.myQuery = userQuery;
    }
}
