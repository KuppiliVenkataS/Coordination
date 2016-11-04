
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;


/**
 * Created by santhilata on 14/10/15.
 */
public class MethodOverloading {
    int t1;
    int t2;
    Rectangle2D.Double r2d = new Rectangle2D.Double(5,6,10,30);
    java.awt.geom.Arc2D.Double roomba = new java.awt.geom.Arc2D.Double(2,3,20,20,0,360, Arc2D.PIE);
    Object[] os = new Object[7];

    public void swap(Object p, Object q){
        Object temp =p;
        p=q;
        q=temp;
    }

    public boolean meth1(int i, int j){
        if (i==j) return  true;
        else return false;
    }

    public int meth1(double d, double e){
        if (d==e) return 1;
        else return  2;
    }

    public int meth1(String st1, String st2){
        if (st1.equals(st2)) return 1;
        else return 2;
    }

    public boolean inCollision(){
        if(roomba.intersects(r2d)) return true;
        else return  false;
    }



    public static void main(String[] args) {
        MethodOverloading m = new MethodOverloading();
        System.out.println(m.meth1("santhi","santhi"));
        System.out.println(m.meth1(2.3, 4.5));
        System.out.println(m.meth1(3,8));

        for (int i = 0; i < 7; i++) {
            m.os[i] = i;
        }

        System.out.println(m.inCollision());
    }
}
