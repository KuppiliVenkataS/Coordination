package project.ResponseTimeSimulation.Cache_DataServerSimulation;

/**
 * Created by santhilata on 11/5/15.
 */
public class InterWANDataTransfers_State5 {
    int state5_Starttime;
    int state5_EndTime;
    String status = "";
    double timeInState5;
    double remainingTime;


    public InterWANDataTransfers_State5(){}

    public int getState5_Starttime() {
        return state5_Starttime;
    }

    public double getTimeInState5() {
        return timeInState5;
    }

    public void setTimeInState5(double timeInState5) {
        this.timeInState5 = timeInState5;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setState5_Starttime(int state5_Starttime) {
        this.state5_Starttime = state5_Starttime;
    }

    public int getState5_EndTime() {
        return state5_EndTime;
    }

    public void setState5_EndTime(int state5_EndTime) {
        this.state5_EndTime = state5_EndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
