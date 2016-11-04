package project.MiddlewareEnvironment.QueryIndexFiles;

/**
 * Created by santhilata on 20/02/15.
 */
public class Pennant {
    private IndexedQuery root;
    private Pennant leftSubtree;
    private Pennant rightSubtree;

    public Pennant()
    {
        root = null;
        leftSubtree  = null;
        rightSubtree = null;
    }

    public Pennant(IndexedQuery x){
        root = x;
    }

    public IndexedQuery getRoot(){
        return this.root;
    }

    public void setRoot(IndexedQuery newRoot){
        this.root = newRoot;
    }

    public Pennant getLeftSubTree(){
        return this.leftSubtree;
    }

    public void setLeftSubTree(Pennant ps){
        this.leftSubtree = ps;
    }

    public Pennant getRightSubTree(){
        return this.rightSubtree;
    }

    public void setRightSubTree(Pennant ps){
        this.rightSubtree = ps;
    }


    public Pennant pennant_Split(){
        Pennant y = this.leftSubtree;
        this.leftSubtree = y.rightSubtree;
        y.rightSubtree = null;

        return y;
    }

    public Pennant pennant_Union( Pennant y){
        //union this with y
        y.rightSubtree = this.leftSubtree;
        this.leftSubtree = y;

        return this;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pennant pennant = (Pennant) o;

        if (!root.equals(pennant.root)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }
}
