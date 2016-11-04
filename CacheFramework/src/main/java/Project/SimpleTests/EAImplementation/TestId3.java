package project.SimpleTests.EAImplementation;

import weka.classifiers.trees.Id3;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 * Created by santhilata on 19/05/16.
 */
public class TestId3 {
    public static void main(String[] args) throws Exception {

        String fileName = "input.csv";
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(fileName);
        Instances data = source.getDataSet();

        Id3 testAlgo =  new Id3();


        testAlgo.buildClassifier(data);

    }
}
