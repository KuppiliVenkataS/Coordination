package project.SimpleTests.ID3;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by santhilata on 2/5/16.
 */
public class DecisionTree {
    /**
     * Contains the set of available attributes.
     */
    private LinkedHashSet<String> attributes;

    /**
     * Maps a attribute name to a set of possible decisions for that attribute.
     */
    private Map<String, Set<String>> decisions;
    private boolean decisionsSpecified;

    /**
     * Contains the top-most attribute of the decision tree.
     *
     * For a tree where the decision requires no attributes,
     * the rootAttribute yields a boolean classification.
     *
     */
    private DecisionTreeNode rootDecisionNode;
    private Algorithm algorithm;

}
