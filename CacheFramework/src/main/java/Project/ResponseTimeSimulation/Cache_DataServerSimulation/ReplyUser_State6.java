package project.ResponseTimeSimulation.Cache_DataServerSimulation;

/**
 * Created by santhilata on 11/5/15.
 */
public class ReplyUser_State6 {
    int state6_Starttime;
    int state6_EndTime;
    String status = "";

    double timeSpentInState6;
    double remainingTime;

    public double getTimeSpentInState6() {
        return timeSpentInState6;
    }

    public void setTimeSpentInState6(double timeSpentInState6) {
        this.timeSpentInState6 = timeSpentInState6;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getState6_Starttime() {
        return state6_Starttime;
    }

    public void setState6_Starttime(int state6_Starttime) {
        this.state6_Starttime = state6_Starttime;
    }

    public int getState6_EndTime() {
        return state6_EndTime;
    }

    public void setState6_EndTime(int state6_EndTime) {
        this.state6_EndTime = state6_EndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
