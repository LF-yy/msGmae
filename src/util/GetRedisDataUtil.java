package util;

import bean.ItemInfo;
import bean.LtDiabloEquipments;
import com.alibaba.fastjson.JSONObject;
import server.Start;
import tools.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * redis数据获取工具类
 */
public class GetRedisDataUtil {

    public static List<Pair<Integer, Pair<String, Pair<String, Integer>>>> getRedisData(String key) {
        List<Pair<Integer, Pair<String, Pair<String, Integer>>>> ddd = new ArrayList<>();
        String hget = RedisUtil.hget(key, key);
        if (hget != null) {
            List<Pair> pairs = JSONObject.parseArray(hget, Pair.class);
            if (ListUtil.isNotEmpty(pairs)) {
                for (Pair pair : pairs) {
                    ddd.add((Pair<Integer, Pair<String, Pair<String, Integer>>>) pair);
                }
            }
        }
        return ddd;
    }
    public static List<LtDiabloEquipments> getLtDiabloEquipments() {
        if (Start.ltDiabloEquipments.size() >0){
            return Start.ltDiabloEquipments;
        }
        String hget = RedisUtil.hget(RedisUtil.KEYNAMES.SET_LT_DIABLO_EQUIPMENTS.getKeyName(), RedisUtil.KEYNAMES.SET_LT_DIABLO_EQUIPMENTS.getKeyName());
        if (hget != null) {
            return JSONObject.parseArray(hget, LtDiabloEquipments.class);
        }
        return null;
    }

    public static Map<Integer,List<ItemInfo>> getItemInfo() {
        if (Start.itemInfo.keySet().size() >0){
            return Start.itemInfo;
        }
        String hget1 = RedisUtil.hget(RedisUtil.KEYNAMES.ITEM_INFO.getKeyName(), RedisUtil.KEYNAMES.ITEM_INFO.getKeyName());
            if (hget1 != null) {
                return JSONObject.parseObject(hget1, Map.class);
            }
        return null;
    }
}
