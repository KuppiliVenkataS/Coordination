package project.SimpleTests;

/**
 * Created by santhilata on 24/4/15.
 */
public class States {
    public static void main(String[] args) {
       String a1 = "abcdefgh";
        String a2 = "bc";

        if (a1.contains(a2)){
            System.out.println("yes");
        }
        else System.out.println("No");
    }

    static class A{
        int a=8;
        public void printA(){
            System.out.println("A = "+a);;
        }
    }

    static class B{
        int b= 5;
        public void printB(){
            System.out.println("B = "+b);;
        }
    }
}
