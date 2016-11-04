package project.MiddlewareEnvironment.CacheMaintenance.SampleEA;

/**
 * Created by santhilata on 16/06/16.
 */
public class Node{
    String rule;
    Node yesChild;
    Node noChild;
    Node parent;
    boolean isLeaf;

    public Node(String str){
        this.rule = str;
        this.yesChild = null;
        this.noChild = null;
        this.parent = null;
        this.isLeaf = true;
    }

    public Node getYesChild() {
        return yesChild;
    }

    public void setYesChild(Node yesChild) {
        this.yesChild = yesChild;
    }

    public Node getNoChild() {
        return noChild;
    }

    public void setNoChild(Node noChild) {
        this.noChild = noChild;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }
}
