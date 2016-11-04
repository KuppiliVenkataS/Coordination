package project.MiddlewareEnvironment.CacheMaintenance.SampleEA;

public class RuleTree{

    Node root;

    public RuleTree(char[] chAr){
        this.root = new Node(chAr[0]+"");

        Node curr = root;
        for (int i = 1; i < chAr.length ; i++) {
            Node tempNode = new Node(chAr[i]+"");
            curr.setYesChild(tempNode);
            curr.isLeaf = false;
            tempNode.setParent(curr);
            curr = tempNode;

        }
    }

    public Node getNextNode(Node curr){
        return curr.yesChild;
    }

    public Node getRoot() {
        return root;
    }


    public static void main(String[] args) {
        System.out.println("Hello");
    }
}
