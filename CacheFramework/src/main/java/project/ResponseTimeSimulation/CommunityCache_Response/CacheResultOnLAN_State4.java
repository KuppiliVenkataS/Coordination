package project.ResponseTimeSimulation.CommunityCache_Response;

/**
 * Created by santhilata on 25/03/15.
 */
public class CacheResultOnLAN_State4 {
    private int startTimeState4;
    private int endTimeState4;
    private double dataTransferTimeLAN;
    private double remainingTime;


    private String status;

    public CacheResultOnLAN_State4() {
        status = "State4: Done";
    }


    public int getStartTimeState4() {
        return startTimeState4;
    }

    public void setStartTimeState4(int startTimeState4) {
        this.startTimeState4 = startTimeState4;
    }

    public int getEndTimeState4() {
        return endTimeState4;
    }

    public void setEndTimeState4(int endTimeState4) {
        this.endTimeState4 = endTimeState4;
    }

    public double getDataTransferTimeLAN() {
        return dataTransferTimeLAN;
    }

    public void setDataTransferTimeLAN(double datasizeInGB) {
        double dataTransferTimeLAN   =0;

        this.dataTransferTimeLAN = dataTransferTimeLAN;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }
}
