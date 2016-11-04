package project.ResponseTimeSimulation.Cache_DataServerSimulation;

/**
 * Created by santhilata on 11/5/15.
 */
public class LANQuerytransfer_State4 {

    int state4_Starttime;
    int state4_EndTime;
    String status = "";
    double timeSpentInState4;
    double remainingTime;
    double[] dataSizeInDataServer = new double[6];

    public double getDataSizeInDataServer(int dataServer) {
        return dataSizeInDataServer[dataServer];
    }

    public void setDataSizeInDataServer(double dataSizeInDataServer, int dataServer) {
        this.dataSizeInDataServer[dataServer] = dataSizeInDataServer;
    }

    public double getTimeSpentInState4() {
        return timeSpentInState4;
    }

    public void setTimeSpentInState4(double timeSpentInState4) {
        this.timeSpentInState4 = timeSpentInState4;
    }

    public double getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(double remainingTime) {
        this.remainingTime = remainingTime;
    }

    double[] max_time = new double[6];
    double[] timeNeededPerDataServer = new double[6];

    public double getTimeNeededPerDataServer(int dataserver) {
        return timeNeededPerDataServer[dataserver];
    }

    public void setTimeNeededPerDataServer(double timeNeededPerDataServer, int dataServer) {
        this.timeNeededPerDataServer[dataServer] = timeNeededPerDataServer;
    }

    public int getState4_Starttime() {
        return state4_Starttime;
    }

    public void setState4_Starttime(int state4_Starttime) {
        this.state4_Starttime = state4_Starttime;
    }

    public double getMax_time(int i) {
        return max_time[i];
    }

    public void setMax_time(double max_time, int i) {
        this.max_time[i] = max_time;
    }

    public int getState4_EndTime() {
        return state4_EndTime;
    }

    public void setState4_EndTime(int state4_EndTime) {
        this.state4_EndTime = state4_EndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
