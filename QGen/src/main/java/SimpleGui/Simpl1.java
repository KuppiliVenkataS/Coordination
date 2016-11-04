package SimpleGui;

import java.util.ArrayList;
import java.util.regex.Pattern;


/**
 * Created by santhilata on 06/04/16.
 */
public class Simpl1 {
    public static void main(String[] args) {
        String[] singleSequences = {"1", "2", "3", "4","5","6"};
        ArrayList<String> uniqQs = new ArrayList<>();
        ArrayList<String> uniqSubQs = new ArrayList<>();
        ArrayList<String> temp = new ArrayList<>();
        ArrayList<String> temp1 = new ArrayList<>();
        ArrayList<String> singleSequencesList = new ArrayList<>();

        int sequenceLength = 5;
        String str = "";
        for (int i = 0; i < singleSequences.length; i++) {
            singleSequencesList.add(singleSequences[i]);
            temp.add("("+singleSequences[i]+")");
            uniqSubQs.add("("+singleSequences[i]+")") ;
        }
        int mark =1;
        int markTemp=1;
        int seqNo = 0;


        for (int seq=2; seq <= sequenceLength; seq++) {

            for (int k = 0; k < temp.size(); k++) {
                for ( seqNo = mark; seqNo < singleSequences.length; seqNo++) {

                    String sample = getlastElement(temp.get(k));
                    System.out.println("sample ="+sample+"temp.get(k)  ="+temp.get(k));

                    if (!sample.equals(singleSequences[seqNo])) {
                        str = temp.get(k) + "(" + singleSequences[seqNo] + ")";
                        if (isCorrectOrder(str,singleSequencesList)) {
                            System.out.println(" Added String " + str);

                            temp1.add(str);
                            uniqSubQs.add(str);
                            if (seq == sequenceLength)
                                uniqQs.add(str);
                        }
                    }

                }
                mark++;
                if(mark > singleSequences.length && temp.size()>0 ) {
                    mark = markTemp+1;
                    if (!getlastElement(temp.get(k)).equals(singleSequences[singleSequences.length-1])){
                        k--;
                    }
                }

            }
            temp.clear();
            temp.addAll(temp1);
            temp1.clear();
            markTemp++;
            mark=markTemp;
        }
        System.out.println(("=============================="));

        for (int i = 0; i < uniqSubQs.size(); i++) {
            System.out.println(uniqSubQs.get(i));
        }

        for (int i = 0; i < uniqQs.size() ; i++) {
            System.out.println("uniqQs "+uniqQs.get(i));
        }



    }

    private static String getlastElement(String str){
        String[] sampleStr = str.split(Pattern.quote(")"));
        String sample = sampleStr[sampleStr.length-1];
        sample = sample.substring(1,sample.length());
        return  sample;
    }

    private static boolean isCorrectOrder(String str, ArrayList<String> singleSequencesList){
        String[] sampleStr = str.split(Pattern.quote(")"));
        for (int i = 0; i < sampleStr.length-1; i++) {
            for (int j = i; j < sampleStr.length; j++) {
                String sample1 = sampleStr[i];
                sample1 = sample1.substring(1,sample1.length());
                String sample2 = sampleStr[j];
                sample2 = sample2.substring(1,sample2.length());

                if (singleSequencesList.indexOf(sample1)> singleSequencesList.indexOf(sample2))
                    return false;

            }

        }
      return true;
    }

}
