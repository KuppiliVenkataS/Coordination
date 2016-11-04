package project.ResponseTimeSimulation.CommunityCache_Response;

/**
 * Created by santhilata on 25/03/15.
 */
public class ResultAggregation_State9 {
    //STATE 9: Waiting at DataServer

    private int startTimeState9;
    private int endTimeState9;
    private double dataAggregationTime;
    private double remainingTime;

    private String status;

    public ResultAggregation_State9() {
    }

    public int getStartTimeState9() {
        return startTimeState9;
    }

    public void setStartTimeState9(int startTimeState9) {
        this.startTimeState9 = startTimeState9;
    }

    public int getEndTimeState9() {
        return endTimeState9;
    }

    public void setEndTimeState9(int endTimeState9) {
        this.endTimeState9 = endTimeState9;
    }

    public double getDataAggregationTime() {
        return dataAggregationTime;
    }

    public void setDataAggregationTime(double dataAggregationTime) {
        this.dataAggregationTime = dataAggregationTime;
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
