package project.SimpleTests;

/**
 * Created by santhilata on 18/11/15.
 */
public class Thing {
    private int val;
    public Thing(int v){
        val = v;
    }
    public String toString(){
        return "I am thing "+val;
    }

    public boolean equals(Object o){
        if (o instanceof Thing){
            Thing t = (Thing) o;
            return val==t.val;
        }
        return  false;
    }
}
