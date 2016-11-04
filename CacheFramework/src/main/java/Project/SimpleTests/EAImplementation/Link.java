package project.SimpleTests.EAImplementation;

/**
 * Created by santhilata on 16/05/16.
 */
public class Link {
    String condition;
    EATreeNode fromNode;
    EATreeNode toNode;

    public Link(){}

    public Link(String condition){
        this.condition = condition;
    }

    public Link(String condition, EATreeNode fromNode, EATreeNode toNode) {
        this.condition = condition;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public EATreeNode getFromNode() {
        return fromNode;
    }

    public void setFromNode(EATreeNode fromNode) {
        this.fromNode = fromNode;
    }

    public EATreeNode getToNode() {
        return toNode;
    }

    public void setToNode(EATreeNode toNode) {
        this.toNode = toNode;
    }
}
