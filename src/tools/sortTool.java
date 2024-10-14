//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tools;

import java.util.*;

public class sortTool {
    public sortTool() {
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = ((Comparable)o1.getValue()).compareTo(o2.getValue());
                return -compare;
            }
        });
        Map<K, V> returnMap = new LinkedHashMap();
        Iterator var3 = list.iterator();

        while(var3.hasNext()) {
            Map.Entry<K, V> entry = (Map.Entry)var3.next();
            returnMap.put(entry.getKey(), entry.getValue());
        }

        return returnMap;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortAscend(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                int compare = ((Comparable)o1.getValue()).compareTo(o2.getValue());
                return compare;
            }
        });
        Map<K, V> returnMap = new LinkedHashMap();
        Iterator var3 = list.iterator();

        while(var3.hasNext()) {
            Map.Entry<K, V> entry = (Map.Entry)var3.next();
            returnMap.put(entry.getKey(), entry.getValue());
        }

        return returnMap;
    }
}
