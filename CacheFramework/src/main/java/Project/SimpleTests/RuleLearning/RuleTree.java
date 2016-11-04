package project.SimpleTests.RuleLearning;


import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by santhilata on 21/05/16.
 * Algorithm is to develop rules
 * incremental rule learning
 */
public class RuleTree {

    private LinkedHashMap attributeList ;

    /**
     * Following methods are for training data
     * @param inputFile
     * @throws IOException
     */
    public void readData(File inputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        String line = br.readLine(); // first line is always the names of attributes
        String[] classifiers = line.split(Pattern.quote(",")); // classifiers contains array of attribute names
        // by default always the last name is "Class" for the train data

        for (int i = 0; i < classifiers.length-1; i++) {
            Classifier classifier_new = new Classifier(classifiers[i]);

        }


        attributeList = new LinkedHashMap();
        ArrayList<String>[] attributeData = new ArrayList[classifiers.length-1];



        for (int i = 0; i < classifiers.length-1; i++) {
            ArrayList<String> temp = new ArrayList<>();
            attributeData[i] = temp;
        }

        while ((line = br.readLine()) != null){
            String[] attributes = line.split(Pattern.quote(","));
            for (int i = 0; i < classifiers.length-1; i++) {
                attributeData[i].add(attributes[i]);
            }
        }

        // setting attribute name and data(arraylist) in the linked hash map
        for (int i = 0; i < classifiers.length-1; i++) {
            attributeList.put(classifiers[i],attributeData[i]);
        }
    }

    public LinkedHashMap getAttributeList(){
        return attributeList;
    }


    /**
     * The following is a sample raw data
     * @return
     * @throws IOException
     */
    public static File toTrainCreateData() throws IOException {

        File inputFile = new File("raw_data_collected.csv");
        FileWriter fw = new FileWriter(inputFile);
        fw.flush();
        fw.write("id,uLoc,cLoc,size,time,freq\n");
        for (int qid = 0; qid < 100; qid++) { // 100 queries       => 0
            fw.write(qid + ","); // enter qid
            int uLoc = new Random().nextInt(6);//user location       => 1
            while(uLoc == 0) uLoc = new Random().nextInt(6);
            fw.write("uLoc_" + uLoc + ",");

            int cLoc = new Random().nextInt(6); // cache location    =>2
            while(cLoc == 0) cLoc = new Random().nextInt(6);
            fw.write("cLoc_" + cLoc + ",");

            int freq = new Random().nextInt(20); // frequency of a query   =>3
            while(freq ==0)  freq = new Random().nextInt(20);
            fw.write(freq + ",");

            int timeWindows = new Random().nextInt(4);                // =>4
            while (timeWindows == 0) timeWindows = new Random().nextInt(4);
            fw.write(timeWindows + ",");

            int dataSize = new Random().nextInt(20);             // =>5
            while(dataSize ==0) dataSize = new Random().nextInt(20);
            fw.write(dataSize + ",");

            fw.write("\n");

        }
        fw.close();

        return inputFile;
    }//to test create data


    public void clean_rawData(int[] cacheSize) throws IOException{

        File cleanedFile = new File("cleaned_data.csv");
        FileWriter fwClean = new FileWriter(cleanedFile);
        fwClean.flush();
        //write classifier as the first line and outcome of the rule
    }
    /**
     * The following method is to create list of classifiers
     */
    public ArrayList<Classifier> createClassifiersList(){
        ArrayList<Classifier> classifiersList = new ArrayList<>();

        Iterator<String> itr = attributeList.keySet().iterator();
        while (itr.hasNext()){
            String classifierName = itr.next();
            ArrayList<String> classifierValue = (ArrayList<String>)attributeList.get(classifierName);


            // create a new classifier with each attribute in the list
            Classifier classifier = new Classifier(classifierName);
            try {
                String someString = classifierValue.get(0);
                double d = Double.valueOf(someString);
                if (someString.matches("\\-?\\d+")){//optional minus and at least one digit
                    classifier.setType("Integer");
                    System.out.println("integer" + d);
                } else {
                    classifier.setType("Double");
                    System.out.println("double" + d);
                }
            } catch (Exception e) {
                classifier.setType("String");
                System.out.println("not number");
            }

            //finding ideal number of classes
            for (int numClasses = 1; numClasses < 5; numClasses++) {
                // to something here
            }

        }


        return  classifiersList;
    }




    public static void main(String[] args) throws IOException {
        File inputFile =  new File("input.csv");
        RuleTree rt = new RuleTree();
        rt.readData(inputFile);
    }


}
