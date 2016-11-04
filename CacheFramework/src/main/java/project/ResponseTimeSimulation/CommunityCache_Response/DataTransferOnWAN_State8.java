package project.ResponseTimeSimulation.CommunityCache_Response;

/**
 * Created by santhilata on 25/03/15.
 */
public class DataTransferOnWAN_State8 {
    //STATE 8: reached DataServer

    private int startTimeState8;
    private int endTimeState8;
    private double dataTransferTimeWAN;
    private double remainingTime;

    private String status;

    public DataTransferOnWAN_State8() {
    }

    public int getStartTimeState8() {
        return startTimeState8;
    }

    public void setStartTimeState8(int startTimeState8) {
        this.startTimeState8 = startTimeState8;
    }

    public int getEndTimeState8() {
        return endTimeState8;
    }

    public void setEndTimeState8(int endTimeState8) {
        this.endTimeState8 = endTimeState8;
    }

    public double getDataTransferTimeWAN() {
        return dataTransferTimeWAN;
    }

    public void setDataTransferTimeWAN(double dataTransferTimeWAN) {
        this.dataTransferTimeWAN = dataTransferTimeWAN;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
