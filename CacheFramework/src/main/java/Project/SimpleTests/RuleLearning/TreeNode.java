package project.SimpleTests.RuleLearning;

import java.util.ArrayList;

/**
 * Created by santhilata on 21/05/16.
 *
 *
 */
public class TreeNode {
    ArrayList<Rule> rules;
    Object condition;
    Object value;

    boolean isLeaf;
    Object decision;

    ArrayList<TreeNode> otherClassifiers;

    public TreeNode(boolean leaf){
        this.isLeaf = leaf;

        if (this.isLeaf){
            decision = new Object();
        }

    }



    public Object getDecision() {
        return decision;
    }

    public void setDecision(Object decision) {
        this.decision = decision;
    }


    /**
     * to test
     * @param args
     */
    public static void main(String[] args) {

        String str = "class1";
        TreeNode tn = new TreeNode(true);
        tn.setDecision(str);

        System.out.println(tn.getDecision());
        System.out.println(tn.getDecision().getClass());
    }
}
