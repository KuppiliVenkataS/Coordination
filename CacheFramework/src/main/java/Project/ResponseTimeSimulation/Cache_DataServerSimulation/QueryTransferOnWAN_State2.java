package project.ResponseTimeSimulation.Cache_DataServerSimulation;

/**
 * Created by santhilata on 11/5/15.
 * Query reaching to different databases in parallel
 */
public class QueryTransferOnWAN_State2 {
    int state2_Starttime;
    int state2_EndTime;
    double timeRequiredAtWAN;

    double remainingTime;
    String status = "State2: Sending Query on WAN";

    public double getTimeRequiredAtWAN() {
        return timeRequiredAtWAN;
    }

    public void setTimeRequiredAtWAN(double timeRequiredAtWAN) {
        this.timeRequiredAtWAN = timeRequiredAtWAN;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getState2_Starttime() {
        return state2_Starttime;
    }

    public void setState2_Starttime(int state2_Starttime) {
        this.state2_Starttime = state2_Starttime;
    }

    public int getState2_EndTime() {
        return state2_EndTime;
    }

    public void setState2_EndTime(int state2_EndTime) {
        this.state2_EndTime = state2_EndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
