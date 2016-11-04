package project.ResponseTimeSimulation.Cache_DataServerSimulation;

import project.QueryEnvironment.SubjectQuerySegment;

import java.util.ArrayList;

/**
 * Created by santhilata on 11/5/15.
 */
public class Cache_State3 {
    int state3_Starttime;
    int state3_EndTime;
    String status = "";
    double dataFound;
    int[] totalSegmentsNeeded = new int[6];
    ArrayList<SubjectQuerySegment>[] segmentsFound  = new ArrayList[6];
    ArrayList<SubjectQuerySegment>[] segmentsNeeded = new ArrayList[6];

    int[] state4_waiting = new int[6];

    public Cache_State3(){
        for (int i = 0; i < 6 ; i++) {
             totalSegmentsNeeded[i]=0;
            this.segmentsFound[i]  = new ArrayList<>();
            this.segmentsNeeded[i] = new ArrayList<>();

        }
    }

    public int getState4_waiting(int i) {
        return state4_waiting[i];
    }

    public void setState4_waiting(int state4_waiting, int dataServer) {
        this.state4_waiting[dataServer] = state4_waiting;
    }

    public int getTotalSegmentsNeeded(int i) {
        return totalSegmentsNeeded[i];
    }

    public void setTotalSegmentsNeeded(int totalSegmentsNeeded, int dataServer) {
        this.totalSegmentsNeeded[dataServer] = totalSegmentsNeeded;
    }

    public ArrayList<SubjectQuerySegment>[] getSegmentsFound() {
        return segmentsFound;
    }

    public void setSegmentsFound(ArrayList<SubjectQuerySegment>[] segmentsFound) {
        this.segmentsFound = segmentsFound;
    }

    public ArrayList<SubjectQuerySegment>[] getSegmentsNeeded() {
        return segmentsNeeded;
    }

    public void setSegmentsNeeded(ArrayList<SubjectQuerySegment>[] segmentsNeeded) {
        this.segmentsNeeded = segmentsNeeded;
    }

    public int getState3_Starttime() {
        return state3_Starttime;
    }

    public double getDataFound() {
        return dataFound;
    }

    public void setDataFound(double dataFound) {
        this.dataFound = dataFound;
    }



    public void setState3_Starttime(int state3_Starttime) {
        this.state3_Starttime = state3_Starttime;
    }

    public int getState3_EndTime() {
        return state3_EndTime;
    }

    public void setState3_EndTime(int state3_EndTime) {
        this.state3_EndTime = state3_EndTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
