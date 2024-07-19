/*
 * 輪迴碑石 轮回石碑
 */
package server.maps;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.life.MapleMonster;
import constants.MapConstants;
public class MobConstants {
    public static int[] REINCARNATION_MOB = new int[]{ 9300329, 2 }; // 輪迴怪物代碼, 生怪數量倍率 轮回石碑
    public static int isMonsterSpawn(MapleMap map) {
        int addition = 1;
        if (MapConstants.isBossMap(map.getId()) || MapConstants.isEventMap(map.getId())) { // 判斷是否為特殊地圖, 輪迴不會在特殊地圖生效 轮回石碑
            return 1;
        }
        for (MapleMapObject obj : map.getAllMonstersThreadsafe()) { // 判斷地圖有boss, 回傳倍率1
            final MapleMonster mob = (MapleMonster) obj;
            if (mob.getStats().isBoss() && !isReincarnationMob(mob.getId())) {
                return 1;
            }
        }
        if (map.getMonsterById(REINCARNATION_MOB[0]) != null) { // 判斷是否有輪迴 轮回石碑
            addition *= REINCARNATION_MOB[1]; // 乘以倍率
        }
        return addition;
    }
    public static boolean isReincarnationMob(int mobid) { // 判斷是否為輪迴怪物 轮回石碑
        if (REINCARNATION_MOB[0] == mobid) {
            return true;
        }
        return false;
    }
    public static boolean isSpawnSpeed(MapleMap map) {
        if (map.getMonsterById(REINCARNATION_MOB[0]) != null) {
            return true;
        }
        return false;
    }
}