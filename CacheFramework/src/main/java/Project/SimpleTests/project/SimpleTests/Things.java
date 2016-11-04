package project.SimpleTests;

import java.util.ArrayList;

/**
 * Created by santhilata on 18/11/15.
 */
public class Things {
    public static void main(String[] args) {
        ArrayList<Thing> things = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            things.add(new Thing(i));
        }
        for (int i = 0; i < things.size() ; i++) {
            System.out.println(things.get(i));
        }

        Thing t = things.get(1);
        System.out.println("Is"+t+"there: "+things.contains(t));
        t = new Thing(3);
        System.out.println("Is"+t+"there: "+things.contains(t));
    }

    ArrayList<Integer> integers = new ArrayList<>();
}
