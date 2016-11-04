package project.SimpleTests.EAImplementation;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by santhilata on 16/05/16.
 */
public class ClassifierTable {
    Map<String,ArrayList<String>> classifiers;

    public ClassifierTable(){
        classifiers = new HashMap<>();
    }

    public ClassifierTable(HashMap<String,ArrayList<String>> classifiers){
        this.classifiers = classifiers;
    }

    public ArrayList<String> getClassifierNames(){
        ArrayList<String> classifierNames = new ArrayList<>();
        for (String key:classifiers.keySet()) {
            classifierNames.add(key);
        }
        return classifierNames;
    }

    public ArrayList<String> getClassifications(String classifier){
        return classifiers.get(classifier);
    }

    public void setClassfiers(String classifier, ArrayList<String>classifications){
        classifiers.put(classifier,classifications);
    }

    public Map<String, ArrayList<String>> getClassifiers() {
        return classifiers;
    }

    public String toString(){

        String bigString = "";

        for (String key:classifiers.keySet()     ) {
            String str="";
            ArrayList<String> values = classifiers.get(key);

            Iterator<String> itr = values.iterator();
            while(itr.hasNext()){
                str = str+","+itr.next();
            }

            bigString = bigString+key+" : "+str.substring(1,str.length())+"\n";

        }

        return bigString;
    }



    public static void main(String[] args) {
        ArrayList<String> classifications;
        ClassifierTable ct = new ClassifierTable();

        for (int i = 0; i < 10; i++) {
            String key = "Key"+i;
            ArrayList<String> values = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                values.add(key+"_Value_"+j);
            }
            ct.setClassfiers(key,values);

        }


       System.out.println(ct.toString());


    }
}
