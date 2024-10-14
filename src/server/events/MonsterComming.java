//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server.events;

import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DBConPool;
import gui.服务端输出信息;
import server.MapleItemInformationProvider;
import server.MaplePortal;
import server.Timer.EventTimer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.MaplePacketCreator;
import tools.Pair;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class MonsterComming extends MapleEvent {
    private long time = 1200000L;
    private long preparationTime = 180000L;
    private long timeStarted = 0L;
    private int count = 1;
    private int nowStage = 0;
    private long begainRegisterTime = 0L;
    private boolean begain = false;
    private ArrayList<Integer> rewardedChr = new ArrayList();
    private ArrayList<MapleMap> spawnMapList = new ArrayList();
    private ArrayList<MapleMap> spawnedMapList = new ArrayList();
    private ScheduledFuture<?> startGameSchedule;
    private ScheduledFuture<?> msgSchedule;
    private ScheduledFuture<?> prepareGameSchedule;
    private Map<Integer, Monsters> monsterMap = new HashMap();

    public MonsterComming(int channel, int[] mapid) {
        super(channel, mapid);
    }

    public void finished(MapleCharacter chr) {
        if (!this.isRunning) {
            chr.dropMessage(1, "活动并未开始！");
        } else {
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
        this.WorldMessage(2, "【怪物攻城】 : 怪物攻城还有" + this.preparationTime / 1000L / 60L + "分钟在" + this.channel + "频道开始，请大家做好准备！");
        this.WorldEffMessage(5120009, "【怪物攻城】 : 怪物攻城还有" + this.preparationTime / 1000L / 60L + "分钟在" + this.channel + "频道开始，请大家做好准备！");
        this.broadcast(MaplePacketCreator.getClock((int)(this.preparationTime / 1000L)));
        this.startGameSchedule = EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
                MonsterComming.this.nextStage();
            }
        }, this.preparationTime);
    }

    public void nextStage() {
        if (this.isRunning) {
            Monsters mobs;
            MapleMap map;
            Pair ret;
            ArrayList portals;
            Iterator var17;
            if (this.nowStage > 0 && this.nowStage <= this.monsterMap.size()) {
                mobs = (Monsters)this.monsterMap.get(this.nowStage);
                map = this.getChannelServer().getMapFactory().getMap(mobs.getMapId());
                if (map == null) {
                    return;
                }

                boolean exist = false;
                ArrayList<Pair<Integer, Integer>> mobList = mobs.getMobList();
                Iterator var5 = mobList.iterator();

                while(var5.hasNext()) {
                    Pair<Integer, Integer> pair = (Pair)var5.next();
                    if (map.haveMonster((Integer)pair.left)) {
                        exist = true;
                        break;
                    }
                }

                if (exist) {
                    if (this.nowStage == this.monsterMap.size()) {
                        this.WorldMessage(2, "【怪物攻城】 : 很遗憾，没能在时间结束前消灭第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物！");
                        this.WorldEffMessage(5120009, "很遗憾，没能在时间结束前消灭第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物！");
                    } else {
                        this.WorldMessage(2, "【怪物攻城】 : 很遗憾，没能在时间结束前消灭第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物，下一波要守住啊！");
                        this.WorldEffMessage(5120009, "很遗憾，没能在时间结束前消灭第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物，下一波要守住啊！");
                    }

                    map.killAllMonsters(true);
                } else {
                    portals = mobs.getRewardList();
                    var17 = portals.iterator();

                    while(true) {
                        if (!var17.hasNext()) {
                            this.WorldMessage(2, "【怪物攻城】 : 恭喜你们击退了第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物，赶快捡取奖励吧！");
                            this.WorldEffMessage(5120009, "恭喜你们击退了第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物，赶快捡取奖励吧！");
                            break;
                        }

                        ret = (Pair)var17.next();
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        Random random = new Random();
                        MapleCharacter chr = MapleCharacter.getCharacterById(1);
                        chr.setMap(map.getId());
                        chr.setPosition(new Point((map.getLeft() + map.getRight()) / 2, map.getTop()));

                        for(int i = 0; i < (Integer)ret.right; ++i) {
                            Object toDrop;
                            if (GameConstants.getInventoryType((Integer)ret.left) == MapleInventoryType.EQUIP) {
                                toDrop = ii.randomizeStats((Equip)ii.getEquipById((Integer)ret.left));
                            } else {
                                toDrop = new Item((Integer)ret.left, (short)0, (short) 1, (byte)0);
                            }

                            map.spawnItemDrop(chr, chr, (IItem)toDrop, new Point(random.nextInt(map.getRight() - map.getLeft()) + map.getLeft(), map.getTop() + 50), true, true);
                        }
                    }
                }

                try {
                    map.broadcastMessage(MaplePacketCreator.getClock(30));
                    Thread.sleep(30000L);
                } catch (Exception var13) {
                    服务端输出信息.println_err("【错误】nextStage 线程休眠错误，错误原因：" + var13);
                    var13.printStackTrace();
                }
            }

            ++this.nowStage;
            if (this.nowStage <= this.monsterMap.size()) {
                mobs = (Monsters)this.monsterMap.get(this.nowStage);
                if (mobs == null) {
                    return;
                }

                map = this.getChannelServer().getMapFactory().getMap(mobs.getMapId());
                if (map != null) {
                    this.WorldMessage(2, "【怪物攻城】 : 第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物来袭，地点在" + this.channel + "频道[" + map.getStreetName() + ":" + map.getMapName() + "]！");
                    this.WorldEffMessage(5120009, "第" + this.nowStage + "/" + this.monsterMap.size() + "波怪物来袭，地点在" + this.channel + "频道[" + map.getStreetName() + ":" + map.getMapName() + "]！");
                    ArrayList<Pair<Integer, Integer>> mobList = mobs.getMobList();
                    Random random = new Random();
                    portals = new ArrayList(map.getPortals());
                    var17 = mobList.iterator();

                    while(true) {
                        if (!var17.hasNext()) {
                            map.broadcastMessage(MaplePacketCreator.getClock(mobs.getSecond()));
                            break;
                        }

                        ret = (Pair)var17.next();

                        for(int i = 0; i < (Integer)ret.right; ++i) {
                            MapleMonster mob = MapleLifeFactory.getMonster((Integer)ret.left);
                            if (mob == null) {
                                break;
                            }

                            Point point = new Point(((MaplePortal)portals.get(random.nextInt(portals.size()))).getPosition());
                            point.y -= 10;
                            mob.setPosition(point);
                            map.spawnMonster(mob, -2);
                        }
                    }
                }

                this.startGameSchedule = EventTimer.getInstance().schedule(new Runnable() {
                    public void run() {
                        MonsterComming.this.nextStage();
                    }
                }, (long)(mobs.getSecond() * 1000));
            } else {
                this.WorldMessage(2, "【怪物攻城】 : 怪物没有再出现了，冒险岛世界又恢复了平静，感谢大家的付出！");
                this.WorldEffMessage(5120009, "怪物没有再出现了，冒险岛世界又恢复了平静，感谢大家的付出！");
            }

        }
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
        this.loadFromDB();
        this.nowStage = 0;
    }

    public void loadFromDB() {
        this.monsterMap.clear();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_monster_comming");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    String[] mobStringList = rs.getString("monsters").split(",");
                    ArrayList<Pair<Integer, Integer>> mobList = new ArrayList();
                    String[] rewardStringList = mobStringList;
                    int var8 = mobStringList.length;

                    for(int var9 = 0; var9 < var8; ++var9) {
                        String mobString = rewardStringList[var9];
                        if (mobString != null && mobString.indexOf(":") > 0) {
                            String[] ret = mobString.split(":");
                            if (ret.length >= 2) {
                                Pair<Integer, Integer> mobPair = new Pair(Integer.parseInt(ret[0]), Integer.parseInt(ret[1]));
                                mobList.add(mobPair);
                            }
                        }
                    }

                    rewardStringList = rs.getString("reward").split(",");
                    ArrayList<Pair<Integer, Integer>> rewardList = new ArrayList();
                    String[] var26 = rewardStringList;
                    int var28 = rewardStringList.length;

                    for(int var29 = 0; var29 < var28; ++var29) {
                        String rewardString = var26[var29];
                        if (rewardString != null && rewardString.indexOf(":") > 0) {
                            String[] ret = rewardString.split(":");
                            if (ret.length >= 2) {
                                rewardList.add(new Pair(Integer.parseInt(ret[0]), Integer.parseInt(ret[1])));
                            }
                        }
                    }

                    Monsters mobs = new Monsters(rs.getInt("stage"), mobList, rs.getInt("map_id"), rewardList, rs.getInt("second"));
                    this.monsterMap.put(rs.getInt("stage"), mobs);
                }

                ps.close();
                rs.close();
            } catch (Throwable var22) {
                var2 = var22;
                throw var22;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var21) {
                            var2.addSuppressed(var21);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var24) {
            服务端输出信息.println_err("【错误】怪物攻城loadFromDB错误，错误原因：" + var24);
            var24.printStackTrace();
        }

    }

    public void reset() {
        try {
            super.reset();
            this.resetSchedule();
            this.rewardedChr.clear();
            this.spawnMapList.clear();
            this.spawnedMapList.clear();
            this.count = 1;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】MonsterComming事件reset错误，错误原因：" + var2);
            var2.printStackTrace();
        }

    }

    public void unreset() {
        super.unreset();
        this.WorldMessage(2, "【怪物攻城】 : 活动结束~！");
        this.count = 1;
    }

    public long getTimeLeft() {
        return this.time - (System.currentTimeMillis() - this.timeStarted);
    }

    private class Monsters {
        int stage;
        int mapId;
        int second;
        ArrayList<Pair<Integer, Integer>> mobList = new ArrayList();
        ArrayList<Pair<Integer, Integer>> rewardList = new ArrayList();

        public Monsters(int stage, ArrayList<Pair<Integer, Integer>> mobList, int mapId, ArrayList<Pair<Integer, Integer>> rewardList, int second) {
            this.stage = stage;
            this.mobList = mobList;
            this.mapId = mapId;
            this.rewardList = rewardList;
            this.second = second;
        }

        public int getSecond() {
            return this.second;
        }

        public void setSecond(int second) {
            this.second = second;
        }

        public ArrayList<Pair<Integer, Integer>> getRewardList() {
            return this.rewardList;
        }

        public void setRewardList(ArrayList<Pair<Integer, Integer>> rewardList) {
            this.rewardList = rewardList;
        }

        public int getStage() {
            return this.stage;
        }

        public void setStage(int stage) {
            this.stage = stage;
        }

        public int getMapId() {
            return this.mapId;
        }

        public void setMapId(int mapId) {
            this.mapId = mapId;
        }

        public ArrayList<Pair<Integer, Integer>> getMobList() {
            return this.mobList;
        }

        public void setMobList(ArrayList<Pair<Integer, Integer>> mobList) {
            this.mobList = mobList;
        }
    }
}
