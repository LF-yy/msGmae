package server.life;

import java.util.*;

import bean.MobInfo;
import provider.MapleDataProviderFactory;
import server.Start;
import tools.Pair;
import provider.WzXML.MapleDataType;
import tools.StringUtil;
import provider.MapleDataFileEntry;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataTool;
import provider.MapleData;
import provider.MapleDataProvider;
import util.ListUtil;

public class MapleLifeFactory
{
    private static final MapleDataProvider data;
    private static final MapleDataProvider stringDataWZ;
    private static final MapleDataProvider etcDataWZ;
    private static final MapleData mobStringData;
    private static final MapleData npcStringData;
    private static final MapleData npclocData;
    private static final Map<Integer, String> npcNames;
    private static final Map<Integer, MapleMonsterStats> monsterStats;
    private static final Map<Integer, Integer> NPCLoc;
    private static final Map<Integer, List<Integer>> questCount;
    
    public static AbstractLoadedMapleLife getLife(final int id, final String type) {
        if (type.equalsIgnoreCase("n")) {
            return getNPC(id);
        }
        if (type.equalsIgnoreCase("m")) {
            return getMonster(id);
        }
        System.err.println("Unknown Life type: " + type + "");
        return null;
    }

    public static void deleteStats(int mobId) {
        if (monsterStats.containsKey(mobId)) {
            monsterStats.remove(mobId);
        }

    }

    public static void clearStats() {
        if (!monsterStats.isEmpty()) {
            monsterStats.clear();
        }

    }

    public static String getName(int mobId) {
        MapleMonsterStats stats = (MapleMonsterStats)monsterStats.get(mobId);
        if (stats == null) {
            MapleMonster mob = getMonster(mobId);
            return mob != null ? mob.getName() : "";
        } else {
            return stats.getName();
        }
    }
    public static int getNPCLocation(final int npcid) {
        if (MapleLifeFactory.NPCLoc.containsKey((Object)Integer.valueOf(npcid))) {
            return (int)Integer.valueOf(MapleLifeFactory.NPCLoc.get((Object)Integer.valueOf(npcid)));
        }
        final int map = MapleDataTool.getIntConvert(Integer.toString(npcid) + "/0", MapleLifeFactory.npclocData, -1);
        MapleLifeFactory.NPCLoc.put(Integer.valueOf(npcid), Integer.valueOf(map));
        return map;
    }
    
    public static void loadQuestCounts() {
        if (MapleLifeFactory.questCount.size() > 0) {
            return;
        }
        for (final MapleDataDirectoryEntry mapz : MapleLifeFactory.data.getRoot().getSubdirectories()) {
            if (mapz.getName().equals((Object)"QuestCountGroup")) {
                for (final MapleDataFileEntry entry : mapz.getFiles()) {
                    final int id = Integer.parseInt(entry.getName().substring(0, entry.getName().length() - 4));
                    final MapleData dat = MapleLifeFactory.data.getData("QuestCountGroup/" + entry.getName());
                    if (dat != null && dat.getChildByPath("info") != null) {
                        final List<Integer> z = new ArrayList<Integer>();
                        for (final MapleData da : dat.getChildByPath("info")) {
                            z.add(Integer.valueOf(MapleDataTool.getInt(da, 0)));
                        }
                        MapleLifeFactory.questCount.put(Integer.valueOf(id), z);
                    }
                    else {
                        System.out.println("null questcountgroup");
                    }
                }
            }
        }
    }
    public static void addStats(int mobId, MapleMonsterStats stats) {
        if (monsterStats.containsKey(mobId)) {
            monsterStats.remove(mobId);
        }

        monsterStats.put(mobId, stats);
    }
    public static List<Integer> getQuestCount(final int id) {
        return (List<Integer>)MapleLifeFactory.questCount.get((Object)Integer.valueOf(id));
    }
    
    public static MapleMonster getMonster(final int mid) {
        MapleMonsterStats stats = (MapleMonsterStats)MapleLifeFactory.monsterStats.get((Object)Integer.valueOf(mid));
        if (stats == null) {
            //System.out.println("没有这个怪物");
            MapleData monsterData = MapleLifeFactory.data.getData(StringUtil.getLeftPaddedStr(Integer.toString(mid) + ".img", '0', 11));
            if (monsterData == null) {
                //System.out.println("没有这个怪物缺少资源");
                return null;
            }
            final MapleData monsterInfoData = monsterData.getChildByPath("info");
            stats = new MapleMonsterStats();
            if(ListUtil.isNotEmpty(Start.mobInfoMap.get(mid))){
                MobInfo mobInfos = Start.mobInfoMap.get(mid).get(0);
                if(mobInfos.getHp()<0){
                    stats.setHp((long)MapleDataTool.getIntConvert("maxHP", monsterInfoData));
                }else {
                    stats.setHp(mobInfos.getHp());
                }

                stats.setMp(mobInfos.getMp());
                stats.setExp(mobInfos.getExp());
                if(mobInfos.getLevel()<0){
                    stats.setLevel((short)MapleDataTool.getIntConvert("level", monsterInfoData));
                }else {
                    stats.setLevel((short) (Math.min(mobInfos.getLevel(), 255)));
                }
                //stats.setEva((short)(Math.min(mobInfos.getEva(), 32767)));
                if (mobInfos.getDamage()>0) {
                    stats.setPhysicalDefense((short) (Math.min(mobInfos.getDamage(), 32767)));
                    stats.setMagicDefense((short) (Math.min(mobInfos.getDamage(), 32767)));
                }else{
                    stats.setPhysicalDefense((short)MapleDataTool.getIntConvert("PDDamage", monsterInfoData, 0));
                    stats.setMagicDefense((short)MapleDataTool.getIntConvert("MDDamage", monsterInfoData, 0));
                }
//                if (mobInfos.getPdd()>0) {
//                    stats.setFixedDamage(mobInfos.getPdd());//设置固定伤害
//                }else{
//                    stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));//设置固定伤害
//                }
            }else{
                stats.setHp((long)MapleDataTool.getIntConvert("maxHP", monsterInfoData));
                stats.setMp(MapleDataTool.getIntConvert("maxMP", monsterInfoData, 0));
                stats.setExp(MapleDataTool.getIntConvert("exp", monsterInfoData, 0));
                stats.setLevel((short)MapleDataTool.getIntConvert("level", monsterInfoData));
                stats.setPhysicalDefense((short)MapleDataTool.getIntConvert("PDDamage", monsterInfoData, 0));
                stats.setMagicDefense((short)MapleDataTool.getIntConvert("MDDamage", monsterInfoData, 0));
                stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));//设置固定伤害
            }

            stats.setEva((short)MapleDataTool.getIntConvert("eva", monsterInfoData, 0));
            stats.setRemoveAfter(MapleDataTool.getIntConvert("removeAfter", monsterInfoData, 0));
            stats.setrareItemDropLevel((byte)MapleDataTool.getIntConvert("rareItemDropLevel", monsterInfoData, 0));//稀有物品掉落级别
            stats.setOnlyNormalAttack(MapleDataTool.getIntConvert("onlyNormalAttack", monsterInfoData, 0) > 0);
            stats.setBoss(MapleDataTool.getIntConvert("boss", monsterInfoData, 0) > 0 || mid == 8810018 || mid == 9410066 || (mid >= 8810118 && mid <= 8810122));
            stats.setExplosiveReward(MapleDataTool.getIntConvert("explosiveReward", monsterInfoData, 0) > 0);//是否爆炸性奖励(爆物飞出)
            stats.setFfaLoot(MapleDataTool.getIntConvert("publicReward", monsterInfoData, 0) > 0);
            stats.setUndead(MapleDataTool.getIntConvert("undead", monsterInfoData, 0) > 0 || mid == 9700004 || mid == 9700009 || mid == 9700010);
            stats.setName(MapleDataTool.getString(mid + "/name", MapleLifeFactory.mobStringData, "MISSINGNO"));
            stats.setBuffToGive(MapleDataTool.getIntConvert("buff", monsterInfoData, -1));
            stats.setFixedDamage(MapleDataTool.getIntConvert("fixedDamage", monsterInfoData, -1));//设置固定伤害
            stats.setFriendly(MapleDataTool.getIntConvert("damagedByMob", monsterInfoData, 0) > 0);//友军怪物
            stats.setNoDoom(MapleDataTool.getIntConvert("noDoom", monsterInfoData, 0) > 0);
            stats.setCP((byte)MapleDataTool.getIntConvert("getCP", monsterInfoData, 0));
            stats.setPoint(MapleDataTool.getIntConvert("point", monsterInfoData, 0));
            stats.setDropItemPeriod(MapleDataTool.getIntConvert("dropItemPeriod", monsterInfoData, 0));
            final boolean hideHP = MapleDataTool.getIntConvert("HPgaugeHide", monsterInfoData, 0) > 0 || MapleDataTool.getIntConvert("hideHP", monsterInfoData, 0) > 0;
            final MapleData selfd = monsterInfoData.getChildByPath("selfDestruction");
            if (selfd != null) {
                stats.setSelfDHP(MapleDataTool.getIntConvert("hp", selfd, 0));
                stats.setSelfD((byte)MapleDataTool.getIntConvert("action", selfd, -1));
            }
            else {
                stats.setSelfD((byte)(-1));
            }
            final MapleData firstAttackData = monsterInfoData.getChildByPath("firstAttack");
            if (firstAttackData != null) {
                if (firstAttackData.getType() == MapleDataType.FLOAT) {
                    stats.setFirstAttack(Math.round(MapleDataTool.getFloat(firstAttackData)) > 0);
                }
                else {
                    stats.setFirstAttack(MapleDataTool.getInt(firstAttackData) > 0);
                }
            }
            if (stats.isBoss() || isDmgSponge(mid)) {
                if (hideHP || monsterInfoData.getChildByPath("hpTagColor") == null || monsterInfoData.getChildByPath("hpTagBgcolor") == null) {
                    stats.setTagColor(0);
                    stats.setTagBgColor(0);
                }
                else {
                    stats.setTagColor(MapleDataTool.getIntConvert("hpTagColor", monsterInfoData));
                    stats.setTagBgColor(MapleDataTool.getIntConvert("hpTagBgcolor", monsterInfoData));
                }
            }
            final MapleData banishData = monsterInfoData.getChildByPath("ban");
            if (banishData != null) {
                stats.setBanishInfo(new BanishInfo(MapleDataTool.getString("banMsg", banishData), MapleDataTool.getInt("banMap/0/field", banishData, -1), MapleDataTool.getString("banMap/0/portal", banishData, "sp")));
            }
            final MapleData reviveInfo = monsterInfoData.getChildByPath("revive");
            if (reviveInfo != null) {
                final List<Integer> revives = new LinkedList<Integer>();
                for (final MapleData bdata : reviveInfo) {
                    revives.add(Integer.valueOf(MapleDataTool.getInt(bdata)));
                }
                stats.setRevives(revives);
            }
            final MapleData monsterSkillData = monsterInfoData.getChildByPath("skill");
            if (monsterSkillData != null) {
                int i = 0;
                final List<Pair<Integer, Integer>> skills = new ArrayList<Pair<Integer, Integer>>();
                while (monsterSkillData.getChildByPath(Integer.toString(i)) != null) {
                    final boolean add = skills.add(new Pair<Integer, Integer>(Integer.valueOf(MapleDataTool.getInt(i + "/skill", monsterSkillData, 0)), Integer.valueOf(MapleDataTool.getInt(i + "/level", monsterSkillData, 0))));
                    ++i;
                }
                stats.setSkills(skills);
            }
            decodeElementalString(stats, MapleDataTool.getString("elemAttr", monsterInfoData, ""));
            final int link = MapleDataTool.getIntConvert("link", monsterInfoData, 0);
            if (link != 0) {
                monsterData = MapleLifeFactory.data.getData(StringUtil.getLeftPaddedStr(link + ".img", '0', 11));
            }
        Label_1377:
            for (final MapleData idata : monsterData) {
                final String name = idata.getName();
                int n = -1;
                switch (name.hashCode()) {
                    case 101491: {
                        if (name.equals((Object)"fly")) {
                            n = 0;
                            break;
                        }
                        break;
                    }
                    case 3357649: {
                        if (name.equals((Object)"move")) {
                            n = 1;
                            break;
                        }
                        break;
                    }
                }
                switch (n) {
                    case 0: {
                        stats.setFly(true);
                        stats.setMobile(true);
                        break Label_1377;
                    }
                    case 1: {
                        stats.setMobile(true);
                        continue;
                    }
                }
            }
            byte hpdisplaytype = -1;
            if (stats.getTagColor() > 0) {
                hpdisplaytype = 0;
            }
            else if (stats.isFriendly()) {
                hpdisplaytype = 1;
            }
            else if (mid >= 9300184 && mid <= 9300215) {
                hpdisplaytype = 2;
            }
            else if (!stats.isBoss() || mid == 9410066) {
                hpdisplaytype = 3;
            }
            stats.setHPDisplayType(hpdisplaytype);
            MapleLifeFactory.monsterStats.put(Integer.valueOf(mid), stats);
        }
        return new MapleMonster(mid, stats);
    }
    
    public static void decodeElementalString(final MapleMonsterStats stats, final String elemAttr) {
        for (int i = 0; i < elemAttr.length(); i += 2) {
            stats.setEffectiveness(Element.getFromChar(elemAttr.charAt(i)), ElementalEffectiveness.getByNumber((int)Integer.valueOf(String.valueOf(elemAttr.charAt(i + 1)))));
        }
    }
    
    private static boolean isDmgSponge(final int mid) {
        switch (mid) {
            case 8810018:
            case 8810118:
            case 8810119:
            case 8810120:
            case 8810121:
            case 8810122:
            case 8820009:
            case 8820010:
            case 8820011:
            case 8820012:
            case 8820013:
            case 8820014: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static MapleNPC getNPC(final int nid) {
        String name = (String)MapleLifeFactory.npcNames.get((Object)Integer.valueOf(nid));
        if (name == null) {
            name = MapleDataTool.getString(nid + "/name", MapleLifeFactory.npcStringData, "MISSINGNO");
            MapleLifeFactory.npcNames.put(Integer.valueOf(nid), name);
        }
        if (name.contains((CharSequence)"Maple TV")) {
            return null;
        }
        return new MapleNPC(nid, name);
    }
    
    public static Map<Integer, String> getNPCNames() {
        return MapleLifeFactory.npcNames;
    }

    public static Map<Integer, MapleMonsterStats> getMonsterStats() {
        return MapleLifeFactory.monsterStats;
    }
    public static MapleMonsterStats getMonsterStats(final int mid) {
        return MapleLifeFactory.monsterStats.get(mid);
    }

    static {
        data = MapleDataProviderFactory.getDataProvider("Mob.wz");
        stringDataWZ = MapleDataProviderFactory.getDataProvider("String.wz");
        etcDataWZ = MapleDataProviderFactory.getDataProvider("Etc.wz");
        mobStringData = MapleLifeFactory.stringDataWZ.getData("Mob.img");
        npcStringData = MapleLifeFactory.stringDataWZ.getData("Npc.img");
        npclocData = MapleLifeFactory.etcDataWZ.getData("NpcLocation.img");
        npcNames = new HashMap<Integer, String>();
        monsterStats = new HashMap<Integer, MapleMonsterStats>();
        NPCLoc = new HashMap<Integer, Integer>();
        questCount = new HashMap<Integer, List<Integer>>();
    }
}
