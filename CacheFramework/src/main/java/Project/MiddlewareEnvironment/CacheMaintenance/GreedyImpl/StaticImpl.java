package project.MiddlewareEnvironment.CacheMaintenance.GreedyImpl;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by santhilata on 13/05/16.
 *
 * Simple implementation of rules. Basic tree
 */
public class StaticImpl {

    public void implementStaticGreedy() throws IOException {
        File inputFile = new File("/home/santhilata/Desktop/inputSample.csv");
        File outFile = new File("/home/santhilata/Desktop/outputSample.csv");
        FileWriter fw = new FileWriter(outFile);
        fw.flush();

        int threshold = 15;
        int[] cacheSize = {25,25,25,25,25};
        int largeWindow = 5;
        int mediumwindow = 3;
        int smallWindow = 1;
        BufferedReader in = new BufferedReader(new FileReader(inputFile));
        String curLine;

        ArrayList userLoc = new ArrayList(Arrays.asList(1,2,3,4,5));


        while ((curLine = in.readLine()) != null) {
            String[] tokens = curLine.split(Pattern.quote(","));
            String qId = tokens[0];
            int uLoc = Integer.parseInt(tokens[1]);
            int cLoc = Integer.parseInt(tokens[2]);
            int freq = Integer.parseInt(tokens[3]);
            int timeWindows = Integer.parseInt(tokens[4]);
            int dataSize = Integer.parseInt(tokens[5]);
            boolean locationFound= false;

            ArrayList copyULoc = (ArrayList) userLoc.clone();

            while (!locationFound && copyULoc.size() >0) {

                fw.write(qId);
                if (uLoc == cLoc) {
                    fw.write("," + cLoc);
                    fw.write(", no change in location\n");
                    locationFound = true;
                    System.out.println(qId + " - No change in Location as uloc==cloc");
                } else {//if uloc and cloc are different
                    if (timeWindows <= smallWindow) {
                        System.out.println("Time recent");
                        if (dataSize <= cacheSize[uLoc - 1]) {
                            cacheSize[uLoc - 1] -= dataSize;
                            fw.write("," + uLoc + "," + "change in location\n");
                            locationFound = true;
                            System.out.println(qId + "-" + "change in location to " + uLoc + " time windows<2, datasize <sizeleft");
                        } else {

                            System.out.println(qId + "-" + "finding alternate locations small window");
                        }

                    } else {//time not recent
                        if (freq >= threshold) {

                            if (dataSize <= cacheSize[uLoc - 1]) {
                                cacheSize[uLoc - 1] -= dataSize;
                                fw.write("," + uLoc + "," + "change in location\n");
                                locationFound = true;
                                System.out.println(qId + "-" + "change in location to " + uLoc + " time windows>2,freq>thre,datasize <sizeleft");
                            } else {
                                System.out.println(qId + "-" + "Finding alternate locations medium/large window");
                            }
                        } else {
                            locationFound = true;
                            fw.write("," + cLoc + ", no change in location\n");
                            System.out.println(qId + "- " + cLoc + "- no change in location freq <threshold, but timewindows > 2");
                        }

                    }

                }

                copyULoc.remove(uLoc-1);
                uLoc = (int)copyULoc.get(0);

            }// an infinite loop  till it finds a location
        }
        fw.close();

    }

    public static void main(String[] args) throws IOException {
        StaticImpl si = new StaticImpl();
        si.implementStaticGreedy();
    }
}
