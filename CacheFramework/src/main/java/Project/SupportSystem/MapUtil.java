package project.SupportSystem;

/**
 * Created by santhilata on 20/05/16.
 * This is just to sort a map based on the values
 */
import java.util.*;

public class MapUtil
{
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map ) {

        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o2.getValue()).compareTo( o1.getValue() );

            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValueAscending( Map<K, V> map ){

        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }


    /**
     * this works only for hashmaps having similar keysets
     * or
     * copying into new hashmap
     * @param map1
     * @param map2
     * @param <K>
     * @param <V>
     */
    public static <K,V> void copyHashMap(HashMap<K,V> map1,HashMap<K, V> map2){
        Set<K> keySet1 = map1.keySet();
        Set<K> keySet2 = map2.keySet();

        Iterator<K> iter1 = keySet1.iterator();

        if (map2.size() ==0){
            while(iter1.hasNext()) {
                K item1 = iter1.next();
                map2.put(item1,map1.get(item1));
            }
        }


        else {
            while(iter1.hasNext()) {
                K item1 = iter1.next();
                Iterator<K> iter2 = keySet2.iterator();
                while (iter2.hasNext()) {
                    K item2 = iter2.next();
                    if (item2.equals(item1)) {
                        map1.put(item1, map2.get(item1)); // copies values of map2 into map1
                        break;
                    }
                }

            }
        }



    }
}