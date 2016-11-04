package project.ResponseTimeSimulation.CommunityCache_Response;

/**
 * Created by santhilata on 25/03/15.
 */
public class QueryTransferOnWAN_State6 {
    //STATE 6: create execution Agents
    private int startTimeState6;
    private int endTimeState6;

    private double queryTransferTimeWAN;
    private double remainingTime;

    private String status;

    public QueryTransferOnWAN_State6() {
    }

    public int getStartTimeState6() {
        return startTimeState6;
    }

    public void setStartTimeState6(int startTimeState6) {
        this.startTimeState6 = startTimeState6;
    }

    public int getEndTimeState6() {
        return endTimeState6;
    }

    public void setEndTimeState6(int endTimeState6) {
        this.endTimeState6 = endTimeState6;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getQueryTransferTimeWAN() {
        return queryTransferTimeWAN;
    }

    public void setQueryTransferTimeWAN(double queryTransferTimeWAN) {
        this.queryTransferTimeWAN = queryTransferTimeWAN;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }
}
