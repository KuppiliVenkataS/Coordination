package project.SimpleTests.EAImplementation;



import project.SupportSystem.MapUtil;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by santhilata on 16/05/16.
 * EATree is to generate decision tree with given rules
 *
 * Create classfier table - denoting various decisions to be taken
 * Target classes- correctly placed, near correctly placed and not placed,
 * create a EATreeNode with the help of classifier table
 * test the out come.
 * choose the best classifier that achieves maximum positive values  (correctly placed)
 *
 */
public class EATree {

    ArrayList<String> classifiersUsed = new ArrayList<>();
    EATreeNode root;


    public void genericEATree(TreeMap treeMap, ClassifierTable ct){
        ArrayList<String> classifiers = ct.getClassifierNames();
        HashMap<String,ArrayList<String>> map = (HashMap<String, ArrayList<String>>) ct.getClassifiers();


    }



    public ArrayList<EATreeNode> createRandomTreeNodes(ClassifierTable ct){
        ArrayList<String> classifiers = ct.getClassifierNames();
        HashMap<String,ArrayList<String>> map = (HashMap<String, ArrayList<String>>) ct.getClassifiers();

        ArrayList<String> chosenList = new ArrayList<>();

        ArrayList<EATreeNode> eat = new ArrayList<>();

        while (chosenList.size()< classifiers.size()){
            String classifier = classifiers.get(new Random().nextInt(classifiers.size()));

            while (chosenList.contains(classifier)) { // to check that classifiers do not repeat
                classifier = classifiers.get(new Random().nextInt(classifiers.size()));
            }

            EATreeNode eATreeNode = new EATreeNode(classifier);
            chosenList.add(classifier);


            ArrayList<String> linkList = map.get(classifier);
            Link[] linkSet = new Link[linkList.size()];
            int i=0;
            Iterator<String> itr = linkList.iterator();
            while (itr.hasNext()){
                Link link = new Link(itr.next());
                linkSet[i++]= link;
            }

            eATreeNode.setLinks(linkSet);
            eat.add(eATreeNode);
        }

        return eat;
    }

    public ArrayList<EATreeNode> createTreeNodes( ClassifierTable ct){

        //creating a node
        ArrayList<String> classifiers = ct.getClassifierNames();
        HashMap<String,ArrayList<String>> map = (HashMap<String, ArrayList<String>>) ct.getClassifiers();
        ArrayList<EATreeNode> eat = new ArrayList<>();

        Iterator<String> itr = classifiers.iterator();
        while(itr.hasNext()){
            String classifier = itr.next();
            EATreeNode eATreeNode = new EATreeNode(classifier);

            ArrayList<String> linkList = map.get(classifier);
            Link[] linkSet = new Link[linkList.size()];
            int i=0;
            Iterator<String> itrll = linkList.iterator();
            while (itrll.hasNext()){
                Link link = new Link(itrll.next());
                linkSet[i++]= link;
            }
            eATreeNode.setLinks(linkSet);
            eat.add(eATreeNode);


        }

        return eat;

    }

    public void trainWithInputData(ClassifierTable ct) throws IOException {

        TreeMap map = calculateEntropy(new File("input.csv"),ct.getClassifierNames());// to get list classifiers according to the entropy

        Iterator<String> itr = map.keySet().iterator();
        while(itr.hasNext()){
            String key = itr.next();
            System.out.println("key "+key +" entropy = "+map.get(key));
        }

        ArrayList<EATreeNode> classifierTreeNodes = createTreeNodes(ct);
        //create tree with the given nodes






    }

    public TreeMap calculateEntropy(File inputDataFile,ArrayList<String> classifierNames ) throws IOException {

        HashMap entropyValues = new HashMap();
        String line=null;
        Iterator<String> itr = classifierNames.iterator();

        while (itr.hasNext()){
            String classifier = itr.next();

            int positiveClass =0;
            int negativeClass = 0;
            int mayBeClass = 0;
            double lines = 0;
            BufferedReader br = new BufferedReader(new FileReader(inputDataFile));
            while( (line = br.readLine()) != null) {

                lines += 1;
                String[] tokens = line.split(Pattern.quote(","));
                String result = tokens[6];

                switch(classifier){
                    case "cLoc" :{
                        String cLoc = tokens[2].replaceAll("[^0-9]", "");
                        String uLoc = tokens[1].replaceAll("[^0-9]", "");

                        if (Integer.parseInt(cLoc) == Integer.parseInt(uLoc) && result.equals("no"))
                            positiveClass++;
                        else  if (Integer.parseInt(cLoc) != Integer.parseInt(uLoc) && result.equals("yes"))
                            positiveClass++;

                        else negativeClass++;
                    }
                    break;

                    case "time": {

                        if (Integer.parseInt(tokens[4]) < 2 && result.equals("no"))
                            positiveClass++;
                        else if (Integer.parseInt(tokens[4]) >= 2 && result.equals("yes"))
                            positiveClass++;
                        else negativeClass++;
                    }
                    break;

                    case "freq":{

                        if (Integer.parseInt(tokens[3]) > 10 && result.equals("no"))
                            positiveClass++;
                        else if (Integer.parseInt(tokens[3]) <= 10 && result.equals("no"))
                            positiveClass++;
                        else negativeClass++;
                    }
                    break;

                    case "size":{
                        if (Integer.parseInt(tokens[5]) > 10 && result.equals("no"))
                            positiveClass++;
                        else if (Integer.parseInt(tokens[5]) <= 10 && result.equals("no"))
                            positiveClass++;
                        else negativeClass++;
                    }
                    break;

                    default:
                        break;
                }

            }
            double a =   (positiveClass/lines);
            double b =    (negativeClass/lines);

            double entropy = (-1)*( a*Math.log(a) + b*Math.log(b)) ;
          //  System.out.println("classifier "+classifier+" "+positiveClass+" "+negativeClass+" "+lines +" entropy "+entropy);

            entropyValues.put(classifier,entropy);
        }

        entropyValues = (HashMap) MapUtil.sortByValue(entropyValues);

        TreeMap treeMap = new TreeMap(entropyValues);
        System.out.println(treeMap.size());

        return treeMap;
    }

    public static void main(String[] args) throws IOException {
        DecimalFormat df = new DecimalFormat("#.#####");

        int[] cacheSize = {100,259,321,30,15};
        EATree eATree = new EATree();
        toTrainCreateData(cacheSize);
        ClassifierTable ct = createSampleClassifierTable();
        ArrayList<String> classifiers = ct.getClassifierNames();
        toTestCreateData();

       // eATree.trainWithInputData(ct);
    }

    /**
     * this function is to just a temporary one to test this class
     * does not have any significance in the original algorithm
     * @param cacheSize
     * @return
     * @throws IOException
     */
    public static File toTrainCreateData(int[] cacheSize) throws IOException {

        File inputFile = new File("input.csv");
        FileWriter fw = new FileWriter(inputFile);
        fw.flush();
        fw.write("id,uLoc,cLoc,size,time,freq,Class\n");
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

            if (uLoc == cLoc)  fw.write("no"+"\n");           // => 6
            else {
                if (timeWindows >=2){
                    if (freq >10 ){
                        
                        if (dataSize < cacheSize[uLoc-1] ){
                            fw.write("yes"+"\n");
                            cacheSize[uLoc-1] -= dataSize;
                        }
                        else fw.write("Maybe"+"\n");
                    }
                    else fw.write("no"+"\n");
                }
                else{

                    if (dataSize < cacheSize[uLoc-1] ){
                        fw.write("yes"+"\n");
                        cacheSize[uLoc-1] -= dataSize;
                    }
                    else fw.write("Maybe"+"\n");
                }
            }

        }
        fw.close();

        return inputFile;
    }//to test create data

    /**
     * This function is to test this class only
     * Doesnot have any significance in the algorithm
     * @return
     * @throws IOException
     */
    public static File toTestCreateData() throws IOException {
        File testDataFile = new File("testData.csv");
        FileWriter fw = new FileWriter(testDataFile);
        fw.flush();
        fw.write("id,uLoc,cLoc,size,time,freq,Class\n");
        for (int qid = 0; qid < 100; qid++) { // 100 queries
            fw.write(qid + ","); // enter qid
            int uLoc = new Random().nextInt(6);//user location
            while(uLoc == 0) uLoc = new Random().nextInt(6);
            fw.write("uLoc_" + uLoc + ",");

            int cLoc = new Random().nextInt(6); // cache location
            while(cLoc == 0) cLoc = new Random().nextInt(6);
            fw.write("cLoc_" + cLoc + ",");

            int freq = new Random().nextInt(20); // frequency of a query
            while(freq ==0)  freq = new Random().nextInt(20);
            fw.write(freq + ",");

            int timeWindows = new Random().nextInt(4);
            while (timeWindows == 0) timeWindows = new Random().nextInt(4);
            fw.write(timeWindows + ",");

            int dataSize = new Random().nextInt(20);
            while(dataSize ==0) dataSize = new Random().nextInt(20);
            fw.write(dataSize + ",");

            String[] result = {"yes","no","Maybe"};
            fw.write(result[new Random().nextInt(3)]);
            fw.write("\n");

        }
        fw.close();

        return testDataFile;
    }

    public static ClassifierTable createSampleClassifierTable(){
        String classifiers[] = {"cLoc","size","time","freq"};
        String[][] classifier1 = {{"cLoc","yes","no"},{"size","yes","no"},{"time","yes","no"},{"freq","yes","no"}};

        HashMap<String,ArrayList<String>> map = new HashMap<>();

        for (int i = 0; i < classifiers.length ; i++) {
            ArrayList<String> arrayList = new ArrayList<>();
            for (int j = 0; j < classifier1.length; j++) {
                if (classifiers[i].equals(classifier1[j][0])){

                    for (int k = 1; k < classifier1[j].length; k++) {
                        arrayList.add(classifier1[j][k]);
                    }
                    break;
                }
            }
            map.put(classifiers[i],arrayList);
        }

        return new ClassifierTable(map);
    }

}
