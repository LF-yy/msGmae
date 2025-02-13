//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import client.MapleCharacter;
import client.SkillFactory;
import client.inventory.MaplePet;
import constants.GameConstants;
import constants.ItemConstants;
import constants.MapConstants;
import gui.LtMS;

import handling.channel.ChannelServer;
import server.Timer.WorldTimer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Pair;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class Marathon {
    private static boolean isOpen = false;
    private static boolean isBegain = false;
    private static boolean reward = false;
    private static int itemId;
    private static int begainMapId;
    private static int finishMapId;
    private static int duration;
    private static int morphItemId;
    private static int buffItemId;
    private static long startTime;
    private static ScheduledFuture<?> gameSchedule;
    private static Map<Integer, Integer> chrMap;
    private static ArrayList<Pair<Integer, Integer>> chrList;
    private static Map<Integer, Boolean> chrReward;
    private static Map<Integer, ArrayList<Pair<Integer, Pair<Byte, Byte>>>> chrBanSkillInfo;
    private static Map<Integer, Boolean> chrChangeMapInfo;
    private static ArrayList<Integer> morphIds;

    public Marathon() {
    }

    public static void setBegainMapId(int mapId) {
        begainMapId = mapId;
    }

    public static int getBegainMapId() {
        return begainMapId;
    }

    public static void setFinishMapId(int mapId) {
        finishMapId = mapId;
    }

    public static int getFinishMapId() {
        return finishMapId;
    }

    public static boolean open() {
        if (isOpen) {
            return false;
        } else {
            chrMap.clear();
            chrList.clear();
            chrReward.clear();
            chrBanSkillInfo.clear();
            chrChangeMapInfo.clear();
            isOpen = true;
            itemId = (Integer)LtMS.ConfigValuesMap.get("马拉松比赛道具ID");
            startTime = 0L;
            reward = false;
            isBegain = false;
            Iterator var0 = ChannelServer.getAllInstances().iterator();

            ChannelServer cserv1;
            Iterator var2;
            while(var0.hasNext()) {
                cserv1 = (ChannelServer)var0.next();
                var2 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                while(var2.hasNext()) {
                    MapleCharacter mch = (MapleCharacter)var2.next();
                    mch.dropMessage(6, "[马拉松比赛]: 马拉松比赛活动开启，现在是报名时间，请大家尽快去任意主城《送货员 杜宜》处报名。");
                }
            }

            var0 = ChannelServer.getAllInstances().iterator();

            while(var0.hasNext()) {
                cserv1 = (ChannelServer)var0.next();
                var2 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                while(var2.hasNext()) {
                    MapleMap map = (MapleMap)var2.next();
                    map.startMapEffect("马拉松比赛活动开启，现在是报名时间，请大家尽快去任意主城《送货员 杜宜》处报名。", 5121015);
                }
            }

            return true;
        }
    }

    public static boolean setDuration(int duration) {
        if (duration <= 0) {
            return false;
        } else {
            Marathon.duration = duration;
            return true;
        }
    }

    public static int getDuration() {
        return duration;
    }

    public static boolean isOpen() {
        return isOpen;
    }

    public static boolean isBegain() {
        return isBegain;
    }

    public static boolean hasJoined(MapleCharacter chr) {
        return chrMap.containsKey(chr.getId());
    }

    public static int getItemId() {
        return itemId;
    }

    public static boolean joinIn(MapleCharacter chr) {
        if (chr != null && !isBegain) {
            if (!chr.canHold(itemId)) {
                return false;
            } else {
                if (!chr.haveItem(itemId)) {
                    chr.gainItem(itemId, 1);
                    chr.dropMessage(5, "你获得了 " + ItemConstants.getItemNameById(itemId) + " x" + 1);
                }

                if (!chrMap.containsKey(chr.getId())) {
                    chrMap.put(chr.getId(), 0);
                    chrChangeMapInfo.put(chr.getId(), false);
                    Iterator var1 = ChannelServer.getAllInstances().iterator();

                    while(var1.hasNext()) {
                        ChannelServer cserv1 = (ChannelServer)var1.next();
                        Iterator var3 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                        while(var3.hasNext()) {
                            MapleCharacter mch = (MapleCharacter)var3.next();
                            mch.dropMessage(6, "[马拉松比赛]: " + chr.getName() + " 报名了比赛。");
                        }
                    }
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public static ArrayList<Integer> getChrIdList() {
        if (chrMap.isEmpty()) {
            return null;
        } else {
            ArrayList<Integer> chrIds = new ArrayList();
            Iterator var1 = chrMap.entrySet().iterator();

            while(var1.hasNext()) {
                Map.Entry<Integer, Integer> entry = (Map.Entry)var1.next();
                if (entry.getKey() != null) {
                    chrIds.add(entry.getKey());
                }
            }

            if (chrIds.isEmpty()) {
                return null;
            } else {
                return chrIds;
            }
        }
    }

    public static boolean begain() {
        if (!chrMap.isEmpty() && finishMapId != 0 && begainMapId != 0 && duration > 0 && !isBegain && isOpen) {
            isBegain = true;
            startTime = Calendar.getInstance().getTimeInMillis();
            Iterator var0 = chrMap.entrySet().iterator();

            while(true) {
                MapleCharacter chr;
                do {
                    Map.Entry entry;
                    do {
                        if (!var0.hasNext()) {
                            marathonThread();
                            var0 = ChannelServer.getAllInstances().iterator();

                            label65:
                            while(var0.hasNext()) {
                                ChannelServer cserv1 = (ChannelServer)var0.next();
                                Iterator var8 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                                while(true) {
                                    MapleMap map;
                                    do {
                                        if (!var8.hasNext()) {
                                            continue label65;
                                        }

                                        map = (MapleMap)var8.next();
                                    } while(map == null);

                                    map.startMapEffect("马拉松比赛正式开始，各位参赛者请抓紧时间跑到目的地:" + MapConstants.getMapNameById(finishMapId) + "。", 5121015);
                                    Iterator var10 = map.getAllPlayersThreadsafe().iterator();

                                    while(var10.hasNext()) {
                                        MapleCharacter mch = (MapleCharacter)var10.next();
                                        if (mch != null) {
                                            mch.dropMessage(6, "[马拉松比赛]: 马拉松比赛正式开始，各位参赛者请抓紧时间跑到目的地:" + MapConstants.getMapNameById(finishMapId) + "。");
                                        }
                                    }
                                }
                            }

                            return true;
                        }

                        entry = (Map.Entry)var0.next();
                    } while((Integer)entry.getKey() <= 0);

                    chr = MapleCharacter.getCharacterById((Integer)entry.getKey());
                } while(chr == null);

                Iterator var3 = chr.getSummonedPets().iterator();

                while(var3.hasNext()) {
                    MaplePet pet = (MaplePet)var3.next();
                    if (pet != null) {
                        chr.unequipPet(pet, false);
                    }
                }

                chr.changeMap(begainMapId);
                if (chr.getClient().isLoggedIn()) {
                    chrChangeMapInfo.put(chr.getId(), true);
                }

                banSkill(chr, 4001003);
                banSkill(chr, 14001003);
                banSkill(chr, 4120002);
                banSkill(chr, 4220002);
                banSkill(chr, 4111006);
                banSkill(chr, 4211002);
                banSkill(chr, 2201002);
                banSkill(chr, 2301001);
                banSkill(chr, 2101002);
                banSkill(chr, 5101007);
                banSkill(chr, 12101003);
                banSkill(chr, 14101004);
                setMorph(chr);

                try {
                    Thread.sleep(50L);
                } catch (InterruptedException var6) {
                    var6.printStackTrace();
                }
            }
        } else {
            return false;
        }
    }

    private static void banSkill(MapleCharacter chr, int skillId) {
        try {
            if (chr == null) {
                return;
            }

            if (chr.getSkillLevel(skillId) >= 1) {
                Pair<Integer, Pair<Byte, Byte>> pair0 = new Pair(skillId, new Pair(chr.getSkillLevel(skillId), chr.getMasterLevel(skillId)));
                boolean isContain = false;
                if (!chrBanSkillInfo.isEmpty() && chrBanSkillInfo.containsKey(chr.getId())) {
                    Iterator var4 = ((ArrayList)chrBanSkillInfo.get(chr.getId())).iterator();

                    while(var4.hasNext()) {
                        Pair<Integer, Pair<Byte, Byte>> pair1 = (Pair)var4.next();
                        if (((Integer)pair1.left).equals(pair0.left)) {
                            isContain = true;
                            break;
                        }
                    }
                }

                if (!isContain) {
                    if (chrBanSkillInfo.containsKey(chr.getId())) {
                        ((ArrayList)chrBanSkillInfo.get(chr.getId())).add(pair0);
                    } else {
                        ArrayList<Pair<Integer, Pair<Byte, Byte>>> pairList0 = new ArrayList();
                        pairList0.add(pair0);
                        chrBanSkillInfo.put(chr.getId(), pairList0);
                    }
                }

                int skillbook = GameConstants.getSkillBookForSkill(skillId);
                chr.setRemainingSp(chr.getRemainingSp(skillbook) + chr.getSkillLevel(skillId), skillbook);
                chr.getClient().sendPacket(MaplePacketCreator.updateSp(chr, false));
                chr.changeSkillLevel(SkillFactory.getSkill(skillId), (byte)0, chr.getMasterLevel(skillId));
            }
        } catch (Exception var6) {
            //服务端输出信息.println_err("【错误】banSkill错误，错误原因：" + var6);
            var6.printStackTrace();
        }

    }

    public static boolean returnSkills(MapleCharacter chr) {
        if (chr == null) {
            return false;
        } else if ((!isBegain() || !chr.haveItem(getItemId())) && chrBanSkillInfo.containsKey(chr.getId())) {
            Iterator var1 = ((ArrayList)chrBanSkillInfo.get(chr.getId())).iterator();

            while(var1.hasNext()) {
                Pair<Integer, Pair<Byte, Byte>> pair = (Pair)var1.next();
                int skillbook = GameConstants.getSkillBookForSkill((Integer)pair.left);
                int remainSp = chr.getRemainingSp(skillbook) - Byte.parseByte(((Pair)pair.right).left + "");
                if (remainSp < 0) {
                    remainSp = 0;
                }

                chr.setRemainingSp(remainSp, skillbook);
                chr.getClient().sendPacket(MaplePacketCreator.updateSp(chr, false));
                chr.changeSkillLevel(SkillFactory.getSkill((Integer)pair.left), Byte.parseByte(((Pair)pair.right).left + ""), Byte.parseByte(((Pair)pair.right).right + ""));
            }

            chrBanSkillInfo.remove(chr.getId());
            chr.dropMessage(1, "马拉松比赛禁用的技能已归还");
            return true;
        } else {
            return false;
        }
    }

    public static boolean arrive(MapleCharacter chr) {
        try {
            if (chr != null && chrMap.containsKey(chr.getId()) && chr.getMapId() == finishMapId && isBegain && chr.haveItem(itemId) && chr.getMorphState() != -1 && (Boolean)chrChangeMapInfo.get(chr.getId())) {
                long nowTime = Calendar.getInstance().getTimeInMillis();
                chrMap.put(chr.getId(), (int)(nowTime - startTime));
                chrList.add(new Pair(chr.getId(), (int)(nowTime - startTime)));
                chrReward.put(chr.getId(), false);
                chr.removeAll(itemId);
                chr.forceCancelAllBuffs();
                returnSkills(chr);
                Iterator var3 = ChannelServer.getAllInstances().iterator();

                while(var3.hasNext()) {
                    ChannelServer cserv1 = (ChannelServer)var3.next();
                    Iterator var5 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var5.hasNext()) {
                        MapleCharacter mch = (MapleCharacter)var5.next();
                        mch.dropMessage(6, "[马拉松比赛]: 恭喜 " + chr.getName() + " 到达了目的地，他是第 " + chrList.size() + " 名到达的玩家。");
                    }
                }

                return true;
            } else {
                return false;
            }
        } catch (Exception var7) {
            //服务端输出信息.println_err("【错误】arrive错误，错误原因：" + var7);
            var7.printStackTrace();
            return false;
        }
    }

    public static ArrayList<Integer> getFinishedChrList() {
        if (chrList.isEmpty()) {
            return null;
        } else {
            ArrayList<Integer> chrIds = new ArrayList();
            Iterator var1 = chrList.iterator();

            while(var1.hasNext()) {
                Pair<Integer, Integer> pair = (Pair)var1.next();
                if ((Integer)pair.left > 0 && (Integer)pair.right > 0) {
                    chrIds.add(pair.left);
                }
            }

            if (chrIds.isEmpty()) {
                return null;
            } else {
                return chrIds;
            }
        }
    }

    public static ArrayList<Pair<Integer, Integer>> getChrList() {
        return chrList;
    }

    public static boolean isFinished(MapleCharacter chr) {
        ArrayList<Integer> chrIds = getFinishedChrList();
        if (chrIds == null) {
            return false;
        } else {
            return chrIds.contains(chr.getId());
        }
    }

    public static boolean setReward(MapleCharacter chr, boolean reward) {
        if (chrReward != null && chr != null) {
            chrReward.put(chr.getId(), reward);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isRewarded(MapleCharacter chr) {
        return !chrReward.isEmpty() && chrReward.containsKey(chr.getId()) ? (Boolean)chrReward.get(chr.getId()) : false;
    }

    private static void marathonThread() {
        final long stopTime = startTime + (long)duration;
        gameSchedule = WorldTimer.getInstance().register(new Runnable() {
            public void run() {
                int min = Calendar.getInstance().get(12);
                long nowTime = Calendar.getInstance().getTimeInMillis();
                long leftTime = stopTime - nowTime;
                Iterator var6;
                ChannelServer cserv1;
                Iterator var8;
                MapleCharacter mch;
                if (min % 3 == 0 && leftTime >= 180000L) {
                    var6 = ChannelServer.getAllInstances().iterator();

                    while(var6.hasNext()) {
                        cserv1 = (ChannelServer)var6.next();
                        var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                        while(var8.hasNext()) {
                            mch = (MapleCharacter)var8.next();
                            mch.dropMessage(6, "[马拉松比赛]: 比赛正在紧张进行中，还有 " + leftTime / 1000L / 60L + " 分钟结束，各位选手加油！");
                        }
                    }
                } else if (leftTime < 180000L && leftTime > 0L) {
                    var6 = ChannelServer.getAllInstances().iterator();

                    while(var6.hasNext()) {
                        cserv1 = (ChannelServer)var6.next();
                        var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                        while(var8.hasNext()) {
                            mch = (MapleCharacter)var8.next();
                            mch.dropMessage(6, "[马拉松比赛]: 比赛即将结束，剩余 " + leftTime / 1000L / 60L + " 分钟，仍未完成的选手请加快速度冲刺起来！");
                        }
                    }
                } else if (leftTime <= 0L || !Marathon.isOpen) {
                    Marathon.isBegain = false;
                    Marathon.isOpen = false;
                    Marathon.reward = true;
                    var6 = ChannelServer.getAllInstances().iterator();

                    while(var6.hasNext()) {
                        cserv1 = (ChannelServer)var6.next();

                        for(var8 = cserv1.getPlayerStorage().getAllCharacters().iterator(); var8.hasNext(); mch.dropMessage(6, "[马拉松比赛]: 比赛已结束，感谢大家的支持，请各位参赛选手通过比赛NPC领取奖品！")) {
                            mch = (MapleCharacter)var8.next();
                            if (mch.getMorphState() >= 0) {
                                mch.forceCancelAllBuffs();
                            }

                            if (mch.haveItem(Marathon.itemId)) {
                                mch.removeAll(Marathon.itemId);
                            }

                            if (Marathon.chrBanSkillInfo.containsKey(mch.getId())) {
                                Iterator var10 = ((ArrayList)Marathon.chrBanSkillInfo.get(mch.getId())).iterator();

                                while(var10.hasNext()) {
                                    Pair<Integer, Pair<Byte, Byte>> pair = (Pair)var10.next();
                                    int remainSp = mch.getRemainingSp(0) - Byte.parseByte(((Pair)pair.right).left + "");
                                    if (remainSp < 0) {
                                        remainSp = 0;
                                    }

                                    mch.setRemainingSp(remainSp);
                                    mch.changeSkillLevel(SkillFactory.getSkill((Integer)pair.left), Byte.parseByte(((Pair)pair.right).left + ""), Byte.parseByte(((Pair)pair.right).right + ""));
                                }

                                Marathon.chrBanSkillInfo.remove(mch.getId());
                            }
                        }
                    }

                    var6 = ChannelServer.getAllInstances().iterator();

                    while(var6.hasNext()) {
                        cserv1 = (ChannelServer)var6.next();
                        var8 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                        while(var8.hasNext()) {
                            MapleMap map = (MapleMap)var8.next();
                            map.startMapEffect("比赛已结束，感谢大家的支持，请各位参赛选手通过比赛NPC领取奖品！", 5121015);
                        }
                    }

                    Marathon.gameSchedule.cancel(true);
                }

                var6 = ChannelServer.getAllInstances().iterator();

                label102:
                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(true) {
                        do {
                            while(true) {
                                do {
                                    if (!var8.hasNext()) {
                                        continue label102;
                                    }

                                    mch = (MapleCharacter)var8.next();
                                } while(!Marathon.chrMap.containsKey(mch.getId()));

                                if (mch.haveItem(Marathon.itemId)) {
                                    Marathon.banSkill(mch, 4001003);
                                    Marathon.banSkill(mch, 14001003);
                                    Marathon.banSkill(mch, 4120002);
                                    Marathon.banSkill(mch, 4220002);
                                    Marathon.banSkill(mch, 4111006);
                                    Marathon.banSkill(mch, 4211002);
                                    Marathon.banSkill(mch, 2201002);
                                    Marathon.banSkill(mch, 2301001);
                                    Marathon.banSkill(mch, 2101002);
                                    Marathon.banSkill(mch, 5101007);
                                    Marathon.banSkill(mch, 12101003);
                                    Marathon.banSkill(mch, 14101004);
                                    if (!(Boolean)Marathon.chrChangeMapInfo.get(mch.getId())) {
                                        if (!mch.haveItem(Marathon.itemId)) {
                                            mch.gainItem(Marathon.itemId, 1);
                                            mch.dropMessage(1, "给你补上了马拉松比赛道具，继续比赛吧。");
                                        }

                                        mch.changeMap(Marathon.begainMapId);
                                        if (mch.getClient().isLoggedIn()) {
                                            Marathon.chrChangeMapInfo.put(mch.getId(), true);
                                        }
                                    }
                                    break;
                                }

                                Marathon.returnSkills(mch);
                                mch.forceCancelAllBuffs();
                            }
                        } while(mch.getMorphState() != -1 && mch.getMorphState() != 5121003 && mch.getMorphState() != 5111005 && mch.getMorphState() != 15111002);

                        Marathon.setMorph(mch);
                    }
                }

            }
        }, 30000L);
    }

    public static boolean setMorph(MapleCharacter chr) {
        if (chr == null) {
            return false;
        } else {
            chr.forceCancelAllBuffs();
            Random ra = new Random();
            chr.setMorph(morphItemId, (Integer)morphIds.get(ra.nextInt(morphIds.size())), 3600000, false);
            chr.giveBuff(buffItemId, (short)30000, (short)10000, (short)-9999, (short)-9999, (short)9999, (short)9999, (short)-9999, (short)-9999, (short)60, (short)60, 3600000, false);
            return true;
        }
    }

    public static ArrayList<Pair<Integer, Integer>> getRankList() {
        if (chrList.isEmpty()) {
            return null;
        } else {
            ArrayList<Pair<Integer, Integer>> list = new ArrayList();
            Iterator var1 = chrList.iterator();

            while(var1.hasNext()) {
                Pair<Integer, Integer> pair = (Pair)var1.next();
                if ((Integer)pair.left > 0 && (Integer)pair.right > 0) {
                    list.add(pair);
                }
            }

            if (list.isEmpty()) {
                return null;
            } else {
                return list;
            }
        }
    }

    public static int getRank(MapleCharacter chr) {
        if (chrList.isEmpty()) {
            return 0;
        } else {
            for(int i = 1; i <= chrList.size(); ++i) {
                if ((Integer)((Pair)chrList.get(i - 1)).left == chr.getId()) {
                    return i;
                }
            }

            return 0;
        }
    }

    public static boolean close() {
        if (!isOpen) {
            return false;
        } else {
            isOpen = false;
            isBegain = false;
            chrMap.clear();
            gameSchedule.cancel(true);
            Iterator var0 = ChannelServer.getAllInstances().iterator();

            ChannelServer cserv1;
            Iterator var2;
            while(var0.hasNext()) {
                cserv1 = (ChannelServer)var0.next();
                var2 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                while(var2.hasNext()) {
                    MapleCharacter mch = (MapleCharacter)var2.next();
                    if (mch.getMorphState() >= 0) {
                        mch.forceCancelAllBuffs();
                    }

                    returnSkills(mch);
                    mch.dropMessage(6, "[马拉松比赛]: 马拉松比赛活动已被管理员关闭！");
                }
            }

            var0 = ChannelServer.getAllInstances().iterator();

            while(var0.hasNext()) {
                cserv1 = (ChannelServer)var0.next();
                var2 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                while(var2.hasNext()) {
                    MapleMap map = (MapleMap)var2.next();
                    map.startMapEffect("比赛已结束，感谢大家的支持，请各位参赛选手通过比赛NPC领取奖品！", 5121015);
                }
            }

            return true;
        }
    }

    static {
        itemId = (Integer) LtMS.ConfigValuesMap.get("马拉松比赛道具ID");
        begainMapId = 100000000;
        finishMapId = 102000000;
        duration = 3600000;
        morphItemId = (Integer)LtMS.ConfigValuesMap.get("马拉松比赛变身道具ID") > 0 ? (Integer)LtMS.ConfigValuesMap.get("马拉松比赛变身道具ID") : 2614103;
        buffItemId = (Integer)LtMS.ConfigValuesMap.get("马拉松比赛buff道具ID") > 0 ? (Integer)LtMS.ConfigValuesMap.get("马拉松比赛buff道具ID") : 2438001;
        startTime = 0L;
        gameSchedule = null;
        chrMap = new HashMap();
        chrList = new ArrayList();
        chrReward = new HashMap();
        chrBanSkillInfo = new HashMap();
        chrChangeMapInfo = new HashMap();
        morphIds = new ArrayList(Arrays.asList(145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 74, 75, 87, 95, 97, 100, 103, 104, 105, 110, 111, 114, 115, 116, 125, 128, 130, 131, 139, 140, 143, 144, 160, 161, 162, 178, 196, 197, 198, 199, 207, 208, 209, 212, 213, 221));
    }
}
