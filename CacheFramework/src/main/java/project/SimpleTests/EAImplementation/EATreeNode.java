package project.SimpleTests.EAImplementation;

/**
 * Created by santhilata on 13/05/16.
 * This class is to implement EA tree node
 *
 * EATree Node
 */
public class EATreeNode {
    String classifier;
    boolean isLeaf;
    Link[] links;

    public EATreeNode(String classifier){
        this.classifier = classifier;
        this.isLeaf = true;
        this.links = new Link[3];
    }

    public EATreeNode(String classifier, boolean isLeaf, Link[] links) {
        this.classifier = classifier;
        this.isLeaf = isLeaf;
        for (Link link:links ) {
           if (link.getCondition()!= null){
               link.setFromNode(this);
           }
        }
        this.links = links;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public Link[] getLinks() {
        return links;
    }

    public void setLinks(Link[] links) {for (Link link:links ) {
        if (link.getCondition()!= null){
            link.setFromNode(this);
        }
    }
        this.links = links;
    }

    public String toString(){
        String str = this.classifier+":";
        for (int i = 0; i < this.getLinks().length; i++) {
            str = str+this.getLinks()[i].getCondition()+" ";
        }
        return str;
    }
}
