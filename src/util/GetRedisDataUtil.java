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
        return ddd;
    }
    public static List<LtDiabloEquipments> getLtDiabloEquipments() {
        if (Start.ltDiabloEquipments.size() >0){
            return Start.ltDiabloEquipments;
        }
        return null;
    }

    public static Map<Integer,List<ItemInfo>> getItemInfo() {
        if (Start.itemInfo.keySet().size() >0){
            return Start.itemInfo;
        }
        return null;
    }
}
