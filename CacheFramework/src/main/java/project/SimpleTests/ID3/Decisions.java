package project.SimpleTests.ID3;

/**
 * Created by santhilata on 2/5/16.
 */
import project.SimpleTests.EAImplementation.BadDecisionException;

import java.util.*;


class Decisions {
    private Map<String, DecisionTreeNode> decisions;

    public Decisions() {
        decisions = new HashMap<String, DecisionTreeNode>();
    }

    public Map<String, DecisionTreeNode> getMap() {
        return decisions;
    }

    public void put(String decision, DecisionTreeNode DecisionTreeNode) {
        decisions.put(decision, DecisionTreeNode);
    }

    public void clear() {
        decisions.clear();
    }

    /**
     * Returns the DecisionTreeNode based on the decision matching the provided value.
     *
     * Throws BadDecisionException if no decision matches.
     */
    public DecisionTreeNode apply(String value) throws BadDecisionException {
        DecisionTreeNode result = decisions.get(value);

        if ( result == null )
            throw new BadDecisionException();

        return result;
    }
}