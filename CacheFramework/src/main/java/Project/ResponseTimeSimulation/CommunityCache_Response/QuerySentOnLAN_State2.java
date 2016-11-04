package project.ResponseTimeSimulation.CommunityCache_Response;

/**
 * Created by santhilata on 25/03/15.
 */
public class QuerySentOnLAN_State2 {
    // STATE2 : LAN state
    int startTimeState2;
    double timeRequiredAtLAN;
    String status = " Sending query to Cache";
    double remainingTime;
    int endTimeState2;

    public QuerySentOnLAN_State2() {

    }

    public int getStartTimeState2() {
        return startTimeState2;
    }

    public void setStartTimeState2(int startTimeState2) {
        this.startTimeState2 = startTimeState2;
    }

    public double getTimeRequiredAtLAN() {
        return timeRequiredAtLAN;
    }

    public void setTimeRequiredAtLAN(double timeRequiredAtLAN) {
        this.timeRequiredAtLAN = timeRequiredAtLAN;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getStatus(){
        return status;
    }

    public int getEndTimeState2() {
        return endTimeState2;
    }

    public void setEndTimeState2(int endTimeState2) {
        this.endTimeState2 = endTimeState2;
    }

    public double timeSpentInState2(){
        return (endTimeState2-startTimeState2 + timeRequiredAtLAN);
    }
}
