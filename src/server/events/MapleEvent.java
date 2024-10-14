package server.events;

import database.DBConPool;
import gui.服务端输出信息;
import handling.world.World.Broadcast;
import server.Timer.EventTimer;
import tools.MaplePacketCreator;
import server.maps.SavedLocationType;
import server.MapleItemInformationProvider;
import server.MapleInventoryManipulator;
import server.Randomizer;
import server.RandomRewards;
import client.MapleCharacter;
import handling.channel.ChannelServer;
import server.maps.MapleMap;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class MapleEvent
{
    protected int[] mapid;
    protected int channel;
    protected boolean isRunning;
    
    public MapleEvent(final int channel, final int[] mapid) {
        this.isRunning = false;
        this.channel = channel;
        this.mapid = mapid;
    }
    
    public boolean isRunning() {
        return this.isRunning;
    }
    
    public MapleMap getMap(final int i) {
        return this.getChannelServer().getMapFactory().getMap(this.mapid[i]);
    }
    
    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }
    
    public void broadcast(final byte[] packet) {
        for (int i = 0; i < this.mapid.length; ++i) {
            this.getMap(i).broadcastMessage(packet);
        }
    }
    
    public void givePrize(MapleCharacter chr) {
        final int reward = RandomRewards.getInstance().getEventReward();
        if (reward == 0) {
            chr.gainMeso(66666, true, false, false);
            chr.dropMessage(5, "你获得 66666 金币");
        }
        else if (reward == 1) {
            chr.gainMeso(399999, true, false, false);
            chr.dropMessage(5, "你获得 399999 金币");
        }
        else if (reward == 2) {
            chr.gainMeso(666666, true, false, false);
            chr.dropMessage(5, "你获得 666666 金币");
        }
        else if (reward == 3) {
            chr.addFame(10);
            chr.dropMessage(5, "你获得 10 名聲");
        }
        else {
            int max_quantity = 1;
            switch (reward) {
                case 5062000: {
                    max_quantity = 3;
                    break;
                }
                case 5220000: {
                    max_quantity = 25;
                    break;
                }
                case 4031307:
                case 5050000: {
                    max_quantity = 5;
                    break;
                }
                case 2022121: {
                    max_quantity = 10;
                    break;
                }
            }
            final int quantity = ((max_quantity > 1) ? Randomizer.nextInt(max_quantity) : 0) + 1;
            if (MapleInventoryManipulator.checkSpace(chr.getClient(), reward, quantity, "")) {
                MapleInventoryManipulator.addById(chr.getClient(), reward, (short)quantity);
                chr.dropMessage(5, "恭喜获得" + MapleItemInformationProvider.getInstance().getName(reward));
            }
            else {
                chr.gainMeso(100000, true, false, false);
                chr.dropMessage(5, "參加獎 100000 金币");
            }
        }
    }
    
    public void finished(MapleCharacter chr) {
    }
    
    public void onMapLoad(MapleCharacter chr) {
    }
    
    public void startEvent() {
    }
    
    public void warpBack(MapleCharacter chr) {
        int map = chr.getSavedLocation(SavedLocationType.EVENT);
        if (map <= -1) {
            map = 104000000;
        }
        final MapleMap mapp = chr.getClient().getChannelServer().getMapFactory().getMap(map);
        chr.changeMap(mapp, mapp.getPortal(0));
    }
    
    public void reset() {
        this.isRunning = true;
    }
    
    public void unreset() {
        this.isRunning = false;
    }
    
    public static void setEvent(final ChannelServer cserv, final boolean auto) {
        if (auto) {
            for (final MapleEventType t : MapleEventType.values()) {
                final MapleEvent e = cserv.getEvent(t);
                if (e.isRunning) {
                    for (final int i : e.mapid) {
                        if (cserv.getEvent() == i) {
                            e.broadcast(MaplePacketCreator.serverNotice(0, "距離活動開始只剩一分鐘!"));
                            e.broadcast(MaplePacketCreator.getClock(60));
                            EventTimer.getInstance().schedule((Runnable)new Runnable() {
                                @Override
                                public void run() {
                                    e.startEvent();
                                }
                            }, 60000L);
                            break;
                        }
                    }
                }
            }
        }
        cserv.setEvent(-1);
    }
    
    public static void mapLoad(MapleCharacter chr, final int channel) {
        if (chr == null) {
            return;
        }
        for (final MapleEventType t : MapleEventType.values()) {
            final MapleEvent e = ChannelServer.getInstance(channel).getEvent(t);
            if (e.isRunning) {
                if (chr.getMapId() == 109050000) {
                    e.finished(chr);
                }
                for (final int i : e.mapid) {
                    if (chr.getMapId() == i) {
                        e.onMapLoad(chr);
                    }
                }
            }
        }
    }
    
    public static void onStartEvent(MapleCharacter chr) {
        for (final MapleEventType t : MapleEventType.values()) {
            final MapleEvent e = chr.getClient().getChannelServer().getEvent(t);
            if (e.isRunning) {
                for (final int i : e.mapid) {
                    if (chr.getMapId() == i) {
                        e.startEvent();
                        chr.getMap().broadcastMessage(MaplePacketCreator.serverNotice(5, String.valueOf((Object)t) + " 活動開始。"));
                    }
                }
            }
        }
    }
    
    public static String scheduleEvent(final MapleEventType event, final ChannelServer cserv) {
        if (cserv.getEvent() != -1 || cserv.getEvent(event) == null) {
            return "該活動已經被禁止安排了.";
        }
        for (final int i : cserv.getEvent(event).mapid) {
            if (cserv.getMapFactory().getMap(i).getCharactersSize() > 0) {
                return "該活動已經在執行中.";
            }
        }
        cserv.setEvent(cserv.getEvent(event).mapid[0]);
        cserv.getEvent(event).reset();
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, "活動 " + String.valueOf((Object)event) + " 即將在頻道 " + cserv.getChannel() + " 舉行 , 參加指令@event 要參加的玩家請到頻道 " + cserv.getChannel()));
        return "";
    }


    public void WorldMessage(int type, String message) {
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer cs = (ChannelServer)var3.next();
            Iterator var5 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

            while(var5.hasNext()) {
                MapleCharacter chr = (MapleCharacter)var5.next();
                if (chr != null) {
                    chr.dropMessage(type, message);
                }
            }
        }

    }

    public void WorldEffMessage(int itemId, String message) {
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer cs = (ChannelServer)var3.next();
            Iterator var5 = cs.getMapFactory().getAllMapThreadSafe().iterator();

            while(var5.hasNext()) {
                MapleMap map = (MapleMap)var5.next();
                if (map != null) {
                    map.startMapEffect(message, itemId);
                }
            }
        }

    }
    protected Map<Integer, ArrayList<Pair<Integer, Integer>>> rewardsMap = new HashMap();

    private Map<Integer, ArrayList<Pair<Integer, Integer>>> getRewards(MapleEventType eventType) {
        if (this.rewardsMap.isEmpty()) {
            this.loadRewardsFromDB(eventType);
        }

        return this.rewardsMap;
    }
    public void dropEffectMessageInMaps(int itemId, String message) {
        Iterator var3 = this.getAllMaps().iterator();

        while(var3.hasNext()) {
            MapleMap map = (MapleMap)var3.next();
            if (map != null) {
                map.startMapEffect(message, itemId);
            }
        }

    }
    public void loadRewardsFromDB(MapleEventType eventType) {
        this.rewardsMap.clear();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_event_rewards WHERE eventtype = ?");
                ps.setString(1, eventType.command);
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    int rewardsType = rs.getInt("rewardstype");
                    int itemId = rs.getInt("itemid");
                    int mount = rs.getInt("mount");
                    if (this.rewardsMap.containsKey(rewardsType)) {
                        ((ArrayList)this.rewardsMap.get(rewardsType)).add(new Pair(itemId, mount));
                    } else {
                        this.rewardsMap.put(rewardsType, new ArrayList(Arrays.asList(new Pair(itemId, mount))));
                    }
                }

                ps.close();
                rs.close();
            } catch (Throwable var17) {
                var3 = var17;
                throw var17;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var16) {
                            var3.addSuppressed(var16);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var19) {
            服务端输出信息.println_err("【错误】loadRewards读取数据库错误，错误原因：" + var19);
            var19.printStackTrace();
        }

    }
    public void givePrizeS(MapleCharacter chr, MapleEventType eventType, int rewardType) {
        try {
            this.getRewards(eventType);
            if (!((ArrayList)this.rewardsMap.get(rewardType)).isEmpty()) {
                chr.dropMessage(1, "恭喜你获得了 " + rewardType + " 等奖。");
                Iterator var4 = ((ArrayList)this.rewardsMap.get(rewardType)).iterator();

                while(var4.hasNext()) {
                    Pair<Integer, Integer> pair = (Pair)var4.next();
                    switch ((Integer)pair.left) {
                        case 0:
                            chr.gainExp((Integer)pair.right, true, true, false);
                            break;
                        case 1:
                            chr.modifyCSPoints(1, (Integer)pair.right, true);
                            break;
                        case 2:
                            chr.modifyCSPoints(2, (Integer)pair.right, true);
                            break;
                        case 3:
                            chr.gainMeso((Integer)pair.right, true);
                            break;
                        case 4:
                            chr.增加里程_数据库((Integer)pair.right);
                            break;
                        case 5:
                            chr.setMoney((Integer)pair.right);
                            chr.dropMessage(5, "赞助余额（元宝）增加 " + pair.right);
                            break;
                        case 6:
                            chr.setMoneyAll((Integer)pair.right);
                            chr.dropMessage(5, "累计赞助增加 " + pair.right);
                            break;
                        case 7:
                            chr.setGuildPoints(chr.getGuildPoints() + (Integer)pair.right);
                            chr.dropMessage(5, "家族活跃点增加 " + pair.right);
                            break;
                        default:
                            chr.gainItem((Integer)pair.left, (Integer)pair.right);
                            chr.dropMessage(5, "你获得了 " + MapleItemInformationProvider.getInstance().getName((Integer)pair.left) + " x" + pair.right);
                    }
                }
            }
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】givePrizeS错误，错误原因：" + var6);
            var6.printStackTrace();
        }

    }

    public ArrayList<MapleMap> getAllMaps() {
        ArrayList<MapleMap> mapList = new ArrayList();

        for(int i = 0; i < this.mapid.length; ++i) {
            mapList.add(this.getChannelServer().getMapFactory().getMap(this.mapid[i]));
        }

        return mapList;
    }
    public ArrayList<MapleCharacter> getAllCharacters() {
        ArrayList<MapleCharacter> chrList = new ArrayList();
        ArrayList<MapleMap> mapList = this.getAllMaps();
        Iterator var3 = mapList.iterator();

        while(true) {
            MapleMap map;
            do {
                if (!var3.hasNext()) {
                    return chrList;
                }

                map = (MapleMap)var3.next();
            } while(map == null);

            Iterator var5 = map.getCharacters().iterator();

            while(var5.hasNext()) {
                MapleCharacter chr = (MapleCharacter)var5.next();
                if (chr != null) {
                    chrList.add(chr);
                }
            }
        }
    }
}
