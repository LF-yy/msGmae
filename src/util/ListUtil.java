package util;

import com.sun.istack.internal.Nullable;

import java.util.Collection;

public class ListUtil {

    public static boolean isNotEmpty(@Nullable Collection<?> list){
        return list !=null && !list.isEmpty();
    }
    public static boolean isEmpty(@Nullable Collection<?> list){
        return list ==null || list.isEmpty();
    }
}
