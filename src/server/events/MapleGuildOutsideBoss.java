//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server.events;

import client.MapleCharacter;
import client.MapleStat;
import gui.LtMS;
import gui.服务端输出信息;
import handling.channel.ChannelServer;
import handling.world.World;
import handling.world.World.Guild;
import handling.world.guild.MapleGuild;
import server.MaplePortal;
import server.Start;
import server.Timer.EventTimer;
import server.Timer.WorldTimer;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.sortTool;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class MapleGuildOutsideBoss extends MapleEvent {
    private static final long serialVersionUID = 845748950824L;
    private long time = 1200000L;
    private long preparationTime = 300000L;
    private long timeStarted = 0L;
    private int count = 1;
    private int npcQuantity = 20;
    private int championGuildId = 0;
    private int npcId = 1510007;
    private long begainRegisterTime = 0L;
    private boolean begain = false;
    private ArrayList<Integer> rewardedChr = new ArrayList();
    private Map<Integer, ArrayList<Integer>> guildChrMap = new HashMap();
    private Map<Integer, Integer> guildPointsMap = new HashMap();
    private ArrayList<MapleMap> spawnMapList = new ArrayList();
    private ArrayList<MapleMap> spawnedMapList = new ArrayList();
    private ScheduledFuture<?> startGameSchedule;
    private ScheduledFuture<?> msgSchedule;
    private ScheduledFuture<?> prepareGameSchedule;

    public MapleGuildOutsideBoss(int channel, int[] mapid) {
        super(channel, mapid);
    }

    public void finished(MapleCharacter chr) {
        if (!this.isRunning) {
            chr.dropMessage(1, "活动并未开始！");
        } else if (this.rewardedChr.contains(chr.getId())) {
            chr.dropMessage(1, "你已经领取过奖励了！");
        } else {
            byte type;
            if (this.count <= 3) {
                this.givePrizeS(chr, MapleEventType.家族野外BOSS赛, 1);
                type = 1;
            } else if (this.count > 3 && this.count <= 10) {
                this.givePrizeS(chr, MapleEventType.家族野外BOSS赛, 2);
                type = 2;
            } else {
                this.givePrizeS(chr, MapleEventType.家族野外BOSS赛, 3);
                type = 3;
            }

            Iterator var3 = ChannelServer.getAllInstances().iterator();

            while(var3.hasNext()) {
                ChannelServer cs = (ChannelServer)var3.next();
                Iterator var5 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                while(var5.hasNext()) {
                    MapleCharacter chr0 = (MapleCharacter)var5.next();
                    chr0.dropMessage(6, "[家族野外BOSS争夺赛活动]恭喜 " + chr.getName() + " 归还了遗失的宝物，他是第 " + this.count + " 名完成的玩家，获得了 " + type + " 等奖。");
                }
            }

            this.rewardedChr.add(chr.getId());
            ++this.count;
        }
    }

    public void onMapLoad(MapleCharacter chr) {
        if (this.isTimerStarted()) {
            chr.getClient().sendPacket(MaplePacketCreator.getClock((int)(this.getTimeLeft() / 1000L)));
        }

    }

    public void startEvent() {
        this.begain = true;
        Random random = new Random();

        for(int i = 0; i < this.npcQuantity; ++i) {
            int index = random.nextInt(this.spawnMapList.size());
            MapleMap map = (MapleMap)this.spawnMapList.get(index);
            if (map != null) {
                map.spawnNpc(this.npcId, map.getPortal(0).getPosition());
                this.spawnedMapList.add(map);
                this.spawnMapList.remove(index);
                this.WorldMessage(2, "【家族野外BOSS争夺赛】 : " + (i + 1) + " 号入场NPC出现在了 1 频道：" + map.getStreetName() + "-" + map.getMapName());
            }
        }

        this.WorldEffMessage(5120009, "家族野外BOSS争夺赛开始了！请各位迅速与你的家族成员组队寻找入场NPC，挑战BOSS！");
        this.broadcast(MaplePacketCreator.getClock((int)(this.time / 1000L)));
        this.timeThread();
        this.timeStarted = System.currentTimeMillis();
        this.startGameSchedule = EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
                MapleGuildOutsideBoss.this.TimeOut();
                MapleGuildOutsideBoss.this.unreset();
            }
        }, this.time);
    }

    public boolean isBegain() {
        return this.begain;
    }

    public boolean isTimerStarted() {
        return this.timeStarted > 0L;
    }

    public long getTime() {
        return this.time;
    }

    public void resetSchedule() {
        this.timeStarted = 0L;
        this.begainRegisterTime = 0L;
        this.begain = false;
        if (this.startGameSchedule != null) {
            this.startGameSchedule.cancel(false);
        }

        this.startGameSchedule = null;
        if (this.msgSchedule != null) {
            this.msgSchedule.cancel(false);
        }

        this.msgSchedule = null;
        if (this.prepareGameSchedule != null) {
            this.prepareGameSchedule.cancel(false);
        }

        this.prepareGameSchedule = null;
        if ((Integer) LtMS.ConfigValuesMap.get("家族野外BOSS赛比赛时长") > 0) {
            this.time = (long)((Integer)LtMS.ConfigValuesMap.get("家族野外BOSS赛比赛时长") * 60 * 1000);
        }

        if ((Integer)LtMS.ConfigValuesMap.get("家族野外BOSS赛NPC数量") > 0) {
            this.npcQuantity = (Integer)LtMS.ConfigValuesMap.get("家族野外BOSS赛NPC数量");
        }

        if ((Integer)LtMS.ConfigValuesMap.get("家族野外BOSS赛NPCID") > 0) {
            this.npcId = (Integer)LtMS.ConfigValuesMap.get("家族野外BOSS赛NPCID");
        }

    }

    public void reset() {
        try {
            super.reset();
            this.resetSchedule();
            this.guildChrMap.clear();
            this.rewardedChr.clear();
            this.guildPointsMap.clear();
            this.spawnMapList.clear();
            Iterator var1 = World.getOutsideMapSQL().iterator();

            while(var1.hasNext()) {
                int mapId = (Integer)var1.next();
                MapleMap map = ChannelServer.getInstance(1).getMapFactory().getMap(mapId);
                if (map != null) {
                    this.spawnMapList.add(map);
                }
            }

            this.spawnedMapList.clear();
            this.count = 1;
        } catch (Exception var4) {
            服务端输出信息.println_err("【错误】MapleGuildOutsideBoss事件reset错误，错误原因：" + var4);
            //var4.printStackTrace();
        }

    }

    public void unreset() {
        super.unreset();
        this.resetSchedule();
        this.guildChrMap.clear();
        this.count = 1;
    }

    public long getTimeLeft() {
        return this.time - (System.currentTimeMillis() - this.timeStarted);
    }

    public long getRegisterTimeLeft() {
        return this.preparationTime - (System.currentTimeMillis() - this.begainRegisterTime);
    }
//
//    public int register(MapleCharacter chr) {
//        if (chr == null) {
//            return -1;
//        } else {
//            MapleGuild guild = chr.getGuild();
//            if (guild == null) {
//                return -2;
//            } else if (guild.getLeaderId() != chr.getId()) {
//                return -3;
//            } else if (this.guildChrMap.containsKey(guild.getId())) {
//                return -4;
//            } else if (this.isBegain()) {
//                return -5;
//            } else {
//                if (this.guildChrMap.isEmpty()) {
//                    this.rewardedChr.clear();
//                    this.guildPointsMap.clear();
//                }
//
//                ArrayList<Integer> chrList = new ArrayList();
//                chrList.add(chr.getId());
//                this.guildChrMap.put(guild.getId(), chrList);
//                this.guildPointsMap.put(guild.getId(), 0);
//                if (this.guildPointsMap.size() == 1) {
//                    this.reset();
//                    this.begainRegisterTime = System.currentTimeMillis();
//                    chr.getMap().给时钟((int)(this.preparationTime / 60L / 1000L), true, false);
//                    this.timeThread();
//                    this.prepareGameSchedule = EventTimer.getInstance().schedule(new Runnable() {
//                        public void run() {
//                            Iterator var1;
//                            MapleCharacter chr;
//                            if (MapleGuildOutsideBoss.this.guildPointsMap.size() >= 2) {
//                                var1 = MapleGuildOutsideBoss.this.getAllCharacters().iterator();
//
//                                while(var1.hasNext()) {
//                                    chr = (MapleCharacter)var1.next();
//                                    if (chr != null) {
//                                        MapleGuildOutsideBoss.this.warpBack(chr);
//                                    }
//                                }
//
//                                var1 = MapleGuildOutsideBoss.this.getAllMaps().iterator();
//
//                                while(var1.hasNext()) {
//                                    MapleMap map = (MapleMap)var1.next();
//                                    if (map != null) {
//                                        map.resetFully();
//                                    }
//                                }
//
//                                var1 = MapleGuildOutsideBoss.this.getAllJoinedChr().iterator();
//
//                                while(var1.hasNext()) {
//                                    chr = (MapleCharacter)var1.next();
//                                    if (chr != null) {
//                                        chr.changeMap(MapleGuildOutsideBoss.this.mapid[0], 0);
//                                        chr.setSkillSkinAll(2);
//                                    }
//                                }
//
//                                MapleGuildOutsideBoss.this.dropEffectMessageInMaps(5120009, "比赛正式开始~大家请抓紧时间击败怪物和对手！");
//                                MapleGuildOutsideBoss.this.startEvent();
//                            } else {
//                                var1 = MapleGuildOutsideBoss.this.getAllJoinedChr().iterator();
//
//                                while(var1.hasNext()) {
//                                    chr = (MapleCharacter)var1.next();
//                                    if (chr != null) {
//                                        chr.dropMessage(1, "由于报名家族不足2个，比赛取消！");
//                                    }
//                                }
//
//                                MapleGuildOutsideBoss.this.unreset();
//                            }
//
//                        }
//                    }, this.preparationTime);
//                }
//
//                return 1;
//            }
//        }
//    }

    public int join(MapleCharacter chr) {
        if (chr == null) {
            return -1;
        } else {
            MapleGuild guild = chr.getGuild();
            if (guild == null) {
                return -2;
            } else {
                if (!this.guildPointsMap.containsKey(guild.getId())) {
                    this.guildPointsMap.put(guild.getId(), 0);
                }

                if (!this.guildChrMap.containsKey(guild.getId())) {
                    ArrayList<Integer> chrList = new ArrayList();
                    chrList.add(chr.getId());
                    this.guildChrMap.put(guild.getId(), chrList);
                }

                if (!((ArrayList)this.guildChrMap.get(chr.getGuildId())).contains(chr.getId())) {
                    ((ArrayList)this.guildChrMap.get(guild.getId())).add(chr.getId());
                }

                return 1;
            }
        }
    }

    public int giveUp(MapleCharacter chr) {
        if (chr == null) {
            return -1;
        } else {
            MapleGuild guild = chr.getGuild();
            if (guild == null) {
                return -2;
            } else if (!this.guildChrMap.containsKey(guild.getId())) {
                return -3;
            } else {
                ArrayList<Integer> chrList = (ArrayList)this.guildChrMap.get(guild.getId());
                if (chrList != null && !chrList.isEmpty()) {
                    for(int i = 0; i < chrList.size(); ++i) {
                        if ((Integer)chrList.get(i) == chr.getId()) {
                            chrList.remove(i);
                            break;
                        }
                    }
                }

                return 1;
            }
        }
    }

    public boolean isJoined(int chrId) {
        Iterator var2 = this.guildChrMap.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, ArrayList<Integer>> entry = (Map.Entry)var2.next();
            Iterator var4 = ((ArrayList)entry.getValue()).iterator();

            while(var4.hasNext()) {
                int id = (Integer)var4.next();
                if (id == chrId) {
                    return true;
                }
            }
        }

        return false;
    }

//    public boolean addPoints(int guildId, int points) {
//        if (this.guildPointsMap.containsKey(guildId)) {
//            if ((Integer)this.guildPointsMap.get(guildId) + points < 0) {
//                points = -(Integer)this.guildPointsMap.get(guildId);
//            }
//
//            this.guildPointsMap.put(guildId, (Integer)this.guildPointsMap.get(guildId) + points);
//            ArrayList<MapleCharacter> chrList = this.getAllCharacters();
//            Iterator var4 = chrList.iterator();
//
//            while(var4.hasNext()) {
//                MapleCharacter chr = (MapleCharacter)var4.next();
//                if (chr != null && chr.getGuildId() == guildId) {
//                    if (points >= 0) {
//                        chr.dropMessage(5, "[家族野外BOSS争夺赛]你的家族增加了 " + points + " 点数，目前总点数为 " + this.getPoints(guildId) + "。");
//                    } else {
//                        chr.dropMessage(5, "[家族野外BOSS争夺赛]你的家族减少了 " + points + " 点数，目前总点数为 " + this.getPoints(guildId) + "。");
//                    }
//                }
//            }
//
//            return true;
//        } else {
//            return false;
//        }
//    }

    public int getPoints(int guildId) {
        return (Integer)this.guildPointsMap.get(guildId);
    }

    private void timeThread() {
        this.msgSchedule = WorldTimer.getInstance().register(new Runnable() {
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int minutes = calendar.get(12);
                int seconds = calendar.get(13);
                if (MapleGuildOutsideBoss.this.getTimeLeft() > 0L && minutes % 2 == 0) {
                    int a = 1;
                    Iterator var5 = MapleGuildOutsideBoss.this.spawnedMapList.iterator();

                    while(var5.hasNext()) {
                        MapleMap map = (MapleMap)var5.next();
                        if (map != null) {
                            MapleGuildOutsideBoss.this.WorldMessage(2, "【家族野外BOSS争夺赛】 : " + a + " 号入场NPC仍在 1 频道：" + map.getStreetName() + "-" + map.getMapName());
                            ++a;
                        }
                    }

                    MapleGuildOutsideBoss.this.WorldMessage(2, "[家族野外BOSS争夺赛] : 比赛正在进行中，还有 " + MapleGuildOutsideBoss.this.getTimeLeft() / 60L / 1000L + " 分 " + seconds + " 秒结束，大家加油！");
                    String text = "[家族野外BOSS争夺赛] : 各家族参赛点统计：";
                    int count = 1;
                    Iterator var7 = MapleGuildOutsideBoss.this.getGuildIdsOrderByPoints().entrySet().iterator();

                    while(var7.hasNext()) {
                        Map.Entry<Integer, Integer> entry = (Map.Entry)var7.next();
                        MapleGuild guild = Guild.getGuild((Integer)entry.getKey());
                        if (guild != null) {
                            text = text + "No." + count + " [" + guild.getName() + "]:" + entry.getValue() + "点 ";
                            ++count;
                        }
                    }

                    MapleGuildOutsideBoss.this.WorldMessage(2, text);
                }

            }
        }, 35000L);
    }

    public ArrayList<MapleGuild> getAllJoinedGuilds() {
        ArrayList<MapleGuild> guildList = new ArrayList();
        Iterator var2 = this.guildPointsMap.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry)var2.next();
            MapleGuild guild = Guild.getGuild((Integer)entry.getKey());
            if (guild != null) {
                guildList.add(guild);
            }
        }

        return guildList;
    }

    public ArrayList<MapleCharacter> getAllJoinedChr() {
        ArrayList<MapleCharacter> chrList = new ArrayList();
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            Iterator var4 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

            while(var4.hasNext()) {
                MapleCharacter chr = (MapleCharacter)var4.next();
                if (chr != null && this.isJoined(chr.getId())) {
                    chrList.add(chr);
                }
            }
        }

        return chrList;
    }

    public ArrayList<MapleCharacter> getChrsByGuildId(int guildId) {
        ArrayList<MapleCharacter> chrList = new ArrayList();
        if (this.guildChrMap.get(guildId) == null) {
            return chrList;
        } else {
            Iterator var3 = ((ArrayList)this.guildChrMap.get(guildId)).iterator();

            while(var3.hasNext()) {
                int chrId = (Integer)var3.next();
                Iterator var5 = ChannelServer.getAllInstances().iterator();

                while(var5.hasNext()) {
                    ChannelServer cs = (ChannelServer)var5.next();
                    Iterator var7 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                    while(var7.hasNext()) {
                        MapleCharacter chr = (MapleCharacter)var7.next();
                        if (chr.getId() == chrId) {
                            chrList.add(chr);
                        }
                    }
                }
            }

            return chrList;
        }
    }

    public Map<Integer, Integer> getGuildIdsOrderByPoints() {
        return sortTool.sortDescend(this.guildPointsMap);
    }

    private void distributeRewards() {
        服务端输出信息.println_out("[家族野外BOSS争夺赛] 玩家奖励发放中。。。");
        this.guildPointsMap = sortTool.sortDescend(this.guildPointsMap);
        Map<Integer, Integer> guildRewardsTypeMap = new HashMap();
        int i = 1;
        Iterator var3 = this.guildPointsMap.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry)var3.next();
            guildRewardsTypeMap.put(entry.getKey(), i);
            if (i == 1) {
                this.championGuildId = (Integer)entry.getKey();
                ++i;
            } else if (i == 2) {
                ++i;
            } else {
                i = 3;
            }
        }

        int count = 0;
        Iterator var10 = this.getAllJoinedChr().iterator();

        while(var10.hasNext()) {
            MapleCharacter chr = (MapleCharacter)var10.next();
            if (chr != null) {
                this.givePrizeS(chr, MapleEventType.家族野外BOSS赛, (Integer)guildRewardsTypeMap.get(chr.getGuildId()));
                ++count;
            }
        }

        guildRewardsTypeMap.clear();
        guildRewardsTypeMap = null;
        服务端输出信息.println_out("[家族野外BOSS争夺赛] 奖励玩家发放完毕。共完成" + count + "个玩家的奖励发放。");
        服务端输出信息.println_out("[家族野外BOSS争夺赛] 家族GP点数发放中。。。");
        int gp = 0;
        Iterator var12 = this.guildPointsMap.entrySet().iterator();

        while(var12.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry)var12.next();
            MapleGuild guild = Guild.getGuild((Integer)entry.getKey());
            if (guild != null) {
                int gp0 = (Integer)entry.getValue() / 10;
                if (gp0 > 0) {
                    guild.gainGP(gp0, true);
                }

                gp += gp0;
            }
        }

        服务端输出信息.println_out("[家族野外BOSS争夺赛] 家族GP点数发放完成，共计发放 " + gp + " 点GP值。");
    }

    private void TimeOut() {
        this.distributeRewards();
        Iterator var1 = ChannelServer.getAllInstances().iterator();

        while(var1.hasNext()) {
            ChannelServer cs = (ChannelServer)var1.next();
            Iterator var3 = cs.getMapFactory().getAllMaps().iterator();

            while(var3.hasNext()) {
                MapleMap map = (MapleMap)var3.next();
                if (map != null) {
                    String name = Guild.getGuild(this.championGuildId).getName();
                    map.startMapEffect("[家族野外BOSS争夺赛]比赛结束，<" + Guild.getGuild(this.championGuildId).getName() + ">家族以" + this.guildPointsMap.get(this.championGuildId) + "参赛点赢得了比赛，让我们恭喜他们！", 5120009);
                }
            }
        }

        var1 = this.spawnedMapList.iterator();

        while(var1.hasNext()) {
            MapleMap map = (MapleMap)var1.next();
            if (map != null) {
                map.removeNpc(this.npcId);
            }
        }

    }
    public ArrayList<MapleMap> getAllMaps() {
        ArrayList<MapleMap> mapList = new ArrayList();

        for(int i = 0; i < this.mapid.length; ++i) {
            mapList.add(this.getChannelServer().getMapFactory().getMap(this.mapid[i]));
        }

        return mapList;
    }
    public boolean revive(MapleCharacter chr) {
        if (chr == null) {
            return false;
        } else {
            Map<MapleStat, Integer> hpmpupdate = new EnumMap(MapleStat.class);
            chr.getStat().setHp(chr.getStat().getCurrentMaxHp());
            chr.getStat().setMp(chr.getStat().getCurrentMaxMp());
            hpmpupdate.put(MapleStat.HP, Integer.valueOf(chr.getStat().getHp()));
            hpmpupdate.put(MapleStat.MP, Integer.valueOf(chr.getStat().getMp()));
            chr.getClient().sendPacket(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, chr));
            hpmpupdate.clear();
            ArrayList<MapleMap> mapList = this.getAllMaps();
            Random rand = new Random();
            MapleMap map = this.getMap(rand.nextInt(mapList.size()));
            if (map != null) {
                MaplePortal portal = map.getPortal(rand.nextInt(map.getPortals().size()));
                if (portal != null) {
                    chr.changeMap(map, portal);
                    chr.getClient().sendPacket(MaplePacketCreator.enableActions());
                    return true;
                } else {
                    MapleMap map0 = this.getMap(0);
                    chr.changeMap(map0, map0.getPortal(0));
                    chr.getClient().sendPacket(MaplePacketCreator.enableActions());
                    return false;
                }
            } else {
                MapleMap map0 = this.getMap(0);
                chr.changeMap(map0, map0.getPortal(0));
                chr.getClient().sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
        }
    }

    public MapleMap getSpawnedMap() {
        Iterator var1 = this.spawnedMapList.iterator();

        MapleMap map;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            map = (MapleMap)var1.next();
        } while(map == null);

        return map;
    }

    public MapleMap getBossMap() {
        Iterator var1 = this.getAllMaps().iterator();

        MapleMap map;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            map = (MapleMap)var1.next();
        } while(map == null || map.getCharacters().size() > 0);

        return map;
    }

    public int getNpcId() {
        return this.npcId;
    }

    public boolean removeNpc(int mapId) {
        int index = 0;
        boolean success = false;

        for(Iterator var4 = this.spawnedMapList.iterator(); var4.hasNext(); ++index) {
            MapleMap map = (MapleMap)var4.next();
            if (map != null && map.getId() == mapId) {
                map.removeNpc(this.npcId);
                success = true;
                break;
            }
        }

        if (success) {
            this.spawnedMapList.remove(index);
        }

        return success;
    }
}
