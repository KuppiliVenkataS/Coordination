package project.SimpleTests.RuleLearning;

import project.SupportSystem.MapUtil;

import java.util.*;

/**
 * Created by santhilata on 22/05/16.
 */
public class Classifier {
    String classifierName;
    String type;
    double step;
    int numClasses;

    public Classifier(String classifierName){
        this.classifierName = classifierName;
    }

    public String getClassifierName() {
        return classifierName;
    }

    public void setClassifierName(String classifierName) {
        this.classifierName = classifierName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public int getNumClasses() {
        return numClasses;
    }

    public void setNumClasses(int numClasses) {
        this.numClasses = numClasses;
    }

    /**
     * Following methods work only for numeric type of classifiers
     * @param data
     * @return
     */
    public double getMax(ArrayList data){
        double max = 0.0;
        if (this.type.equals("Integer") || this.type.equals("Double" )){
            Collections.sort(data, Collections.reverseOrder()); // to sort in descending order
            max = (double) data.get(0);
        }

        return max;
    }

    public double getMin(ArrayList data){
        double min = 0.0;
        if (this.type.equals("Integer") || this.type.equals("Double" )){
            Collections.sort(data);
            min = (double) data.get(0);
        }

        return min;
    }

    public double getMean(ArrayList data){
        Iterator<String> itr = data.iterator();
        double sum =  0.0;
        double dataItem =0.0;
        while(itr.hasNext()){
           if (type.equals("Integer"))
               sum += Integer.parseInt(itr.next());
            else if (type.equals("Double")){
               sum += Double.parseDouble(itr.next());
           }
        }

        double mean = sum / data.size();

        return mean;
    }



    /**
     * This returns num of class intervals from the numeric type data
     * returns double, double values in ascending order
     * @param data
     * @return
     */
    public TreeMap getDistinctClassesForNumericData(ArrayList data){

        LinkedHashMap  mapClassIntervals = new LinkedHashMap();
        double min = getMin(data);
        double max = getMax(data);
        double lowerLimit = min;
        double upperLimit = min;

        this.step = (int)(Math.abs(max - min)/ numClasses); // set numClasses
        for (int i = 0; i < this.numClasses && upperLimit<max ; i++) {
            upperLimit = lowerLimit + (int)step;
            mapClassIntervals.put(lowerLimit,upperLimit);
            lowerLimit = upperLimit;
        }

        return  new TreeMap(MapUtil.sortByValueAscending(mapClassIntervals));
    }



    /**
     * Following methods are for String type data attribute
     */
    public ArrayList<String> getDistinctValuesForStringData(ArrayList data){
        ArrayList<String> distinctValues = null;
        if (this.type.equals("String")){
            distinctValues = new ArrayList<>();
            Iterator<String> itr = data.iterator();
            while(itr.hasNext()){
                String str = (String)itr.next();
                if (!distinctValues.contains(str))
                    distinctValues.add(str);
            }
        }
        this.numClasses = distinctValues.size();
        return distinctValues;
    }






}
