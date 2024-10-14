//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tools;

import java.util.Iterator;
import java.util.WeakHashMap;

public class CacheMap<K, V> extends WeakHashMap<K, V> {
    public CacheMap() {
    }

    public Object getKeyByValue(Object value) {
        Object returnValue = null;
        Object key = null;
        Iterator iter = this.keySet().iterator();

        while(iter.hasNext()) {
            key = iter.next();
            if (this.get(key).equals(value)) {
                returnValue = key;
                break;
            }
        }

        return returnValue;
    }
}
