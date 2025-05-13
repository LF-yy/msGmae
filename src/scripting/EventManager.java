package scripting;

import database.DBConPool;
import gui.LtMS;

import handling.world.MaplePartyCharacter;
import handling.world.World;
import server.MapleItemInformationProvider;

import java.sql.*;
import java.sql.Date;
import java.util.*;

import constants.ServerConfig;
import handling.world.World.Broadcast;
import server.events.MapleEvent;
import server.Randomizer;
import server.events.MapleEventType;
import snail.DamageManage;
import tools.MaplePacketCreator;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.OverrideMonsterStats;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMap;
import handling.world.MapleParty;
import client.MapleCharacter;
import server.MapleSquad;
import tools.FileoutputUtil;
import server.Timer.EventTimer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import javax.script.ScriptException;
import tools.FilePrinter;
import handling.channel.ChannelServer;
import client.MapleClient;
import util.ListUtil;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;

public class EventManager
{
    private static int[] eventChannel;
    private Invocable iv;
    private int channel;
    private Map<String, EventInstanceManager> instances = new ConcurrentHashMap<>();
    private Properties props = new Properties();
    private String name;
    private MapleClient c;
    private static final String ERROR_LOG_FILE = "EventManager.txt";
    public EventManager(final ChannelServer cserv, final Invocable iv, final String name) {
        this.iv = iv;
        this.channel = cserv.getChannel();
        this.name = name;
    }

    /**
     * 取消事件调度的方法
     *
     * 本方法尝试调用一个名为"cancelSchedule"的函数来取消事件的调度，并清理所有已创建的事件实例
     * 如果调用"cancelSchedule"函数或清理事件实例时发生错误，则会捕获异常并打印错误信息
     */
    public void cancel() {
        if (this.instances == null) {
            logError("Instances map is null, skipping cleanup.");
            return;
        }

        try {
            // 调用取消调度函数
            this.iv.invokeFunction("cancelSchedule", new Object[] { null });

            // 遍历所有事件实例并清理
            for (Map.Entry<String, EventInstanceManager> entry : this.instances.entrySet()) {
                if (entry != null && entry.getValue() != null) {
                    try {
                        entry.getValue().dispose();
                    } catch (Exception e) {
                        logError("Failed to dispose event instance: " + entry.getKey(), e);
                    }
                }
            }
        } catch (ScriptException e) {
            logError("Script execution failed while calling 'cancelSchedule'", e);
        } catch (NoSuchMethodException e) {
            logError("Method 'cancelSchedule' not found", e);
        }
    }
    private void logError(String message, Exception e) {
        // 打印错误信息到标准错误输出
        System.err.println(message + "\n" + e.getMessage());
        // 记录堆栈信息到日志文件
        FilePrinter.printError(ERROR_LOG_FILE, message + "\n" + getStackTrace(e));
 
    }

    private void logError(String message) {
        System.err.println(message);
        FilePrinter.printError(ERROR_LOG_FILE, message);
    }
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    public ScheduledFuture<?> schedule( String methodName,  long delay) {
        return EventTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, new Object[] { null });
                }
                catch (ScriptException | NoSuchMethodException ex3) {
                    //服务端输出信息.println_err("Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                    FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                }
            }
        }, delay);
    }
    
    public ScheduledFuture<?> schedule(final String methodName, final long delay, final EventInstanceManager eim) {
        return EventTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, eim);
                }
                catch (ScriptException | NoSuchMethodException ex3) {
                    //服务端输出信息.println_err("Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                    FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                    FileoutputUtil.log("logs\\Log_Script_Except.txt", "Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                }
            }
        }, delay);
    }
    
    public ScheduledFuture<?> scheduleAtTimestamp(final String methodName, final long timestamp) {
        return EventTimer.getInstance().scheduleAtTimestamp((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                    iv.invokeFunction(methodName, new Object[] { null });
                }
                catch (ScriptException | NoSuchMethodException ex3) {
                    //服务端输出信息.println_err("Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                    FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                    FileoutputUtil.log("logs\\Log_Script_Except.txt", "Event name : " + name + ", method Name : " + methodName + ":\n" + (Object)ex3);
                }
            }
        }, timestamp);
    }
    
    public int getChannel() {
        return this.channel;
    }
    
    public ChannelServer getChannelServer() {
        return ChannelServer.getInstance(this.channel);
    }
    
    public EventInstanceManager getInstance(final String name) {
        return (EventInstanceManager)this.instances.get((Object)name);
    }
    
    public Collection<EventInstanceManager> getInstances() {
        return Collections.unmodifiableCollection((Collection<? extends EventInstanceManager>)this.instances.values());
    }
    
    public EventInstanceManager newInstance(final String name) {
        final EventInstanceManager ret = new EventInstanceManager(this, name, this.channel);
        if (name.equals("LudiPQ")) {
            int a = 0;
            ++a;
        }
        this.instances.put(name, ret);
        return ret;
    }
    
    public void disposeInstance(final String name) {
        this.instances.remove((Object)name);
        if (this.getProperty("state") != null && this.instances.isEmpty()) {
            this.setProperty("state", "0");
        }
        if (this.getProperty("leader") != null && this.instances.isEmpty() && this.getProperty("leader").equals((Object)"false")) {
            this.setProperty("leader", "true");
        }
        if (this.name.equals((Object)"CWKPQ")) {
            final MapleSquad squad = ChannelServer.getInstance(this.channel).getMapleSquad("CWKPQ");
            if (squad != null) {
                squad.clear();
            }
        }
    }
    
    public Invocable getIv() {
        return this.iv;
    }
    
    public void setProperty(final String key, final String value) {
        this.props.setProperty(key, value);
    }
    
    public String getProperty(final String key) {
        return this.props.getProperty(key);
    }
    
    public final Properties getProperties() {
        return this.props;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void startInstance() {
        try {
            this.iv.invokeFunction("setup", new Object[] { null });
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup:\n" + (Object)ex3);
        }
    }
    
    public void startInstance(final String mapid, MapleCharacter chr) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", mapid);
            eim.setDisposed(false);
            eim.registerCarnivalParty(chr, chr.getMap(), (byte)0);
            FilePrinter.printError("Event记录.txt", "Event name : " + this.name + ", method Name :"+chr.getName()+"开启事件,地图编码"+chr.getMapId() );
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup:\n" + (Object)ex3);
        }
    }
    
    public void startInstance_Party(final String mapid, MapleCharacter chr) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", mapid);
            eim.setDisposed(false);
            eim.registerParty(chr.getParty(), chr.getMap());
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup:\n" + (Object)ex3);
        }
    }
    
    public void startInstance(final MapleCharacter character, final String leader) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", new Object[] { null });
            eim.setDisposed(false);
            eim.registerPlayer(character);
            eim.setProperty("leader", leader);
            eim.setProperty("guildid", String.valueOf(character.getGuildId()));
            this.setProperty("guildid", String.valueOf(character.getGuildId()));
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-Guild:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-Guild:\n" + (Object)ex3);
        }
    }
    
    public void startInstance_CharID(final MapleCharacter character) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", Integer.valueOf(character.getId()));
            eim.setDisposed(false);
            eim.registerPlayer(character);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-CharID:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-CharID:\n" + (Object)ex3);
        }
    }
    
    public void startInstance(final MapleCharacter character) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", new Object[] { null });
            eim.setDisposed(false);
            eim.registerPlayer(character);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-character:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-character:\n" + (Object)ex3);
        }
    }
    
    public void startInstance(final MapleParty party, final MapleMap map) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", Integer.valueOf(party.getId()));
            eim.setDisposed(false);
            eim.registerParty(party, map);
        }
        catch (ScriptException ex) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-partyid:\n" + (Object)ex);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-partyid:\n" + (Object)ex);
        }
        catch (NoSuchMethodException ex2) {
            this.startInstance_NoID(party, map, (Exception)ex2);
        }
    }
    
    public void startInstance_NoID(final MapleParty party, final MapleMap map) {
        this.startInstance_NoID(party, map, null);
    }
    
    public void startInstance_NoID(final MapleParty party, final MapleMap map, final Exception old) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", new Object[] { null });
            eim.setDisposed(false);
            eim.registerParty(party, map);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-party:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-party:\n" + (Object)ex3 + "\n" + (Object)((old == null) ? "no old exception" : old));
        }
    }
    
    public void startInstance(final EventInstanceManager eim, final String leader) {
        try {
            this.iv.invokeFunction("setup", eim);
            eim.setProperty("leader", leader);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-leader:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-leader:\n" + (Object)ex3);
        }
    }
    
    public void startInstance(final MapleSquad squad, final MapleMap map) {
        this.startInstance(squad, map, -1);
    }
    
    public void startInstance(final MapleSquad squad, final MapleMap map, final int questID) {
        if (squad.getStatus() == 0) {
            return;
        }
        if (!squad.getLeader().isGM()) {
            if (World.在线人数() >= 10) {
                int count = squad.getType().i;
                if ((Integer) LtMS.ConfigValuesMap.get("远征队统一人数开关") > 0) {
                    count = (Integer)LtMS.ConfigValuesMap.get("远征队统一人数");
                }

                if (squad.getMembers().size() < count) {
                    squad.getLeader().dropMessage(5, "这个远征队至少要有 " + count + " 人以上才可以开战.");
                    return;
                }
            }
            if (!squad.getLeader().isGM() && this.name.equals((Object) "CWKPQ") && squad.getJobs().size() < 5) {
                squad.getLeader().dropMessage(5, "这个远征队要求至少有5种职业.");
                return;
            }
        }
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", squad.getLeaderName());
            eim.registerSquad(squad, map, questID);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-squad:\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-squad:\n" + (Object)ex3);
        }
    }
    
    public void warpAllPlayer(final int from, final int to) {
        final MapleMap tomap = this.getMapFactory().getMap(to);
        final MapleMap frommap = this.getMapFactory().getMap(from);
        final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
        if (tomap != null && list != null && frommap.getCharactersSize() > 0) {
            for (final MapleMapObject mmo : list) {
                ((MapleCharacter)mmo).changeMap(tomap, tomap.getPortal(0));
            }
        }
    }
    
    public MapleMapFactory getMapFactory() {
        return this.getChannelServer().getMapFactory();
    }
    
    public OverrideMonsterStats newMonsterStats() {
        return new OverrideMonsterStats();
    }
    
    public List<MapleCharacter> newCharList() {
        return new ArrayList<MapleCharacter>();
    }
    
    public MapleMonster getMonster(final int id) {
        return MapleLifeFactory.getMonster(id);
    }
    
    public void broadcastYellowMsg(final String msg) {
        this.getChannelServer().broadcastPacket(MaplePacketCreator.yellowChat(msg));
    }
    
    public void broadcastServerMsg(final String msg) {
        this.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, msg));
    }
    
    public void broadcastServerMsg(final int type, final String msg, final boolean weather) {
        if (!weather) {
            this.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(type, msg));
        }
        else {
            for (final MapleMap load : this.getMapFactory().getAllMaps()) {
                if (load.getCharactersSize() > 0) {
                    load.startMapEffect(msg, type);
                }
            }
        }
    }
    
    public boolean scheduleRandomEvent() {
        boolean omg = false;
        for (int i = 0; i < EventManager.eventChannel.length; ++i) {
            omg |= this.scheduleRandomEventInChannel(EventManager.eventChannel[i]);
        }
        return omg;
    }
    
    public boolean scheduleRandomEventInChannel(final int chz) {
        final ChannelServer cs = ChannelServer.getInstance(chz);
        if (cs == null || cs.getEvent() > -1) {
            return false;
        }
        MapleEventType t;
        MapleEventType x = null;
        for (t = null; t == null; t = x) {
            final MapleEventType[] values = MapleEventType.values();
            for (int length = values.length, i = 0; i < length; ++i) {
                x = values[i];
                if (Randomizer.nextInt(MapleEventType.values().length) == 0 && x != MapleEventType.上楼上楼) {
                    break;
                }
            }
        }
        final String msg = MapleEvent.scheduleEvent(t, cs);
        if (msg.length() > 0) {
            this.broadcastYellowMsg(msg);
            return false;
        }
        EventTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                if (cs.getEvent() >= 0) {
                    MapleEvent.setEvent(cs, true);
                }
            }
        }, 600000L);
        return true;
    }
    
    public void setWorldEvent() {
        for (int i = 0; i < EventManager.eventChannel.length; ++i) {
            EventManager.eventChannel[i] = Randomizer.nextInt(ChannelServer.getAllInstances().size()) + i;
        }
    }
    
    public void invokeFunctionMethod(final String methodName) {
        try {
            this.iv.invokeFunction(methodName, this);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            System.out.println("Event name" + this.getName() + ", Instance name : " + this.name + ", method Name : " + methodName + ":\n" + (Object)ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-squad:\n" + (Object)ex3);
        }
    }
    
    public void worldMessage(final int type, final String message) {
        if (type != 3 && type != 9 && type != 10) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(type, message));
        } else {
            Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(type, 1, message, true));
        }
    }
    public final void worldMapEffectMessage(String msg, int itemId, int duration) {
        Iterator var4 = ChannelServer.getAllInstances().iterator();

        while(var4.hasNext()) {
            ChannelServer cs = (ChannelServer)var4.next();
            Iterator var6 = cs.getMapFactory().getAllMaps().iterator();

            while(var6.hasNext()) {
                MapleMap map = (MapleMap)var6.next();
                if (map != null) {
                    map.startMapEffect_S(msg, itemId, duration);
                }
            }
        }

    }

    public final void worldMapEffectMessage(String msg, int itemId) {
        this.worldMapEffectMessage(msg, itemId, 15000);
    }

    public ScheduledFuture<?> schedule(final String methodName, final EventInstanceManager eim, long delay) {
        return EventTimer.getInstance().schedule(new Runnable() {
            public void run() {
                try {
                    EventManager.this.iv.invokeFunction(methodName, new Object[]{eim});
                } catch (Exception var2) {
                    //服务端输出信息.println_out("Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
                    FileoutputUtil.log("logs\\Log_Script_Except.txt", "Event name : " + EventManager.this.name + ", method Name : " + methodName + ":\n" + var2);
                }

            }
        }, delay);
    }
    public String getServerName() {
        return ServerConfig.SERVERNAME;
    }
    
    public int 获取当前星期() {
        return Calendar.getInstance().get(7);
    }
    
    public final MapleClient getClient() {
        return this.c;
    }
    
    public void openNpc(final int id) {
        this.openNpc(id, null);
    }
    
    public void openNpc(final int id, final int mode) {
        this.openNpc(this.getClient(), id, mode, null);
    }
    
    public void openNpc(final MapleClient cg, final int id) {
        this.openNpc(cg, id, 0, null);
    }
    
    public void openNpc(final int id, final String script) {
        this.openNpc(this.getClient(), id, script);
    }
    
    public void openNpc(final MapleClient cg, final int id, final String script) {
        this.openNpc(this.getClient(), id, 0, script);
    }
    
    public void openNpc(final MapleClient cg, final int id, final int mode, final String script) {
        NPCScriptManager.getInstance().dispose(cg);
        cg.removeClickedNPC();
        NPCScriptManager.getInstance().start(cg, id, mode, script);
    }
    
    public MapleItemInformationProvider getItemInfo() {
        return MapleItemInformationProvider.getInstance();
    }
    
    static {
        EventManager.eventChannel = new int[4];
    }
    public void startInstancea(final MapleCharacter character) {
        try {
            final EventInstanceManager eim = (EventInstanceManager)(EventInstanceManager)this.iv.invokeFunction("setup", null);
            eim.registerPlayer(character);
        }
        catch (ScriptException | NoSuchMethodException ex3) {
            //服务端输出信息.println_err("Event name : " + this.name + ", method Name : setup-character:\n" + ex3);
            FilePrinter.printError(ERROR_LOG_FILE, "Event name : " + this.name + ", method Name : setup-character:\n" + ex3);
        }
    }

    public void startInstance(MapleSquad squad, MapleMap map, String bossid) {
        if (squad.getStatus() != 0) {
            if (!squad.getLeader().isGM()) {
                int mapid = map.getId();
                int chrSize = 0;
                Iterator var6 = squad.getMembers().iterator();

                while(var6.hasNext()) {
                    String chr = (String)var6.next();
                    MapleCharacter player = squad.getChar(chr);
                    if (player != null && player.getMapId() == mapid) {
                        ++chrSize;
                    }
                }
            }

            try {
                EventInstanceManager eim = (EventInstanceManager)this.iv.invokeFunction("setup", new Object[]{squad.getLeaderName()});
                eim.registerSquad(squad, map, Integer.parseInt(bossid));
            } catch (Exception var9) {
                //服务端输出信息.println_out("Event name : " + this.name + ", method Name : setup-squad:\n" + var9);
                FileoutputUtil.log("log\\Script_Except.log", "Event name : " + this.name + ", method Name : setup-squad:\n" + var9);
            }

        }
    }
    public int online() {
        Connection con = DBConPool.getConnection();
        int count = 0;

        try {
            PreparedStatement ps = con.prepareStatement("SELECT count(*) as cc FROM accounts WHERE loggedin = 2");

            for(ResultSet re = ps.executeQuery(); re.next(); count = re.getInt("cc")) {
            }
        } catch (SQLException var6) {
            Logger.getLogger(EventInstanceManager.class.getName()).log(Level.SEVERE, (String)null, var6);
        }

        return count;
    }

    public void broadcastShip(int mapid, int effect) {
        this.getMapFactory().getMap(mapid).broadcastMessage(MaplePacketCreator.boatPacket(effect));
    }

    public void broadcastChangeMusic(int mapid) {
        this.getMapFactory().getMap(mapid).broadcastMessage(MaplePacketCreator.musicChange("Bgm04/ArabPirate"));
    }


    public void broadcastServerMsg2(String msg) {
        this.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, msg));
    }

    public void broadcastServerMsg2(int type, String msg, boolean weather) {
        if (!weather) {
            if (type != 3 && type != 9 && type != 10) {
                this.getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(type, msg));
            } else {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(type, 1, msg, true));
            }
        } else {
            Iterator var4 = this.getMapFactory().getAllMaps().iterator();

            while(var4.hasNext()) {
                MapleMap load = (MapleMap)var4.next();
                if (load.getCharactersSize() > 0) {
                    load.startMapEffect(msg, type);
                }
            }
        }

    }

    public boolean scheduleRandomEventInChannel(MapleCharacter chr, int chz, int A) {
        final ChannelServer cs = ChannelServer.getInstance(chz);
        if (cs != null && cs.getEvent() <= -1) {
            MapleEventType t = null;
            if (t == null) {
                if (A == 1) {
                    t = MapleEventType.打椰子比赛;
                } else if (A == 2) {
                    t = MapleEventType.打瓶盖比赛;
                } else if (A == 3) {
                    t = MapleEventType.向高地比赛;
                } else if (A == 4) {
                    t = MapleEventType.推雪球比赛;
                } else if (A == 5) {
                    t = MapleEventType.上楼上楼;
                } else if (A == 6) {
                    t = MapleEventType.OX答题比赛;
                } else {
                    if (A != 7) {
                        chr.dropMessage(6, "输入指令错误！");
                        return false;
                    }

                    t = MapleEventType.家族对抗赛;
                }
            }

            String msg = MapleEvent.scheduleEvent(t, cs);
            if (msg.length() > 0) {
                this.broadcastYellowMsg(msg);
                return false;
            } else {
                EventTimer.getInstance().schedule(new Runnable() {
                    public void run() {
                        if (cs.getEvent() >= 0) {
                            MapleEvent.setEvent(cs, true);
                        }

                    }
                }, 180000L);
                return true;
            }
        } else {
            return false;
        }
    }

    public int GetZfuben(String Name, int Channale) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM Zfuben WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
        } catch (SQLException var7) {
        }

        return ret;
    }

    public void GainZfuben(String Name, int Channale, int Piot) {
        try {
            int ret = this.GetZfuben(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;

                try {
                    ps = DBConPool.getConnection().prepareStatement("INSERT INTO Zfuben (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setString(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                } catch (SQLException var16) {
                    //服务端输出信息.println_out("xxxxxxxx:" + var16);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var15) {
                        //服务端输出信息.println_out("xxxxxxxxzzzzzzz:" + var15);
                    }

                }
            }

            ret += Piot;
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE Zfuben SET `Point` = ? WHERE Name = ? and channel = ?");
            ps.setInt(1, ret);
            ps.setString(2, Name);
            ps.setInt(3, Channale);
            ps.execute();
            ps.close();
        } catch (SQLException var18) {
            //服务端输出信息.println_err("获取错误!!55" + var18);
        }

    }


    public int GetPiot(String Name, int Channale) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM FullPoint WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
        } catch (SQLException var7) {
        }

        return ret;
    }

    public void GainPiot(String Name, int Channale, int Piot) {
        try {
            int ret = this.GetPiot(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;

                try {
                    ps = DBConPool.getConnection().prepareStatement("INSERT INTO FullPoint (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setString(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                } catch (SQLException var16) {
                    //服务端输出信息.println_out("xxxxxxxx:" + var16);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var15) {
                        //服务端输出信息.println_out("xxxxxxxxzzzzzzz:" + var15);
                    }

                }
            }

            ret += Piot;
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE FullPoint SET `Point` = ? WHERE Name = ? and channel = ?");
            ps.setInt(1, ret);
            ps.setString(2, Name);
            ps.setInt(3, Channale);
            ps.execute();
            ps.close();
        } catch (SQLException var18) {
            //服务端输出信息.println_err("获取错误!!55" + var18);
        }

    }


    public static int 获取最高玩家等级() {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT MAX(level) as DATA FROM characters WHERE gm = 0");
            ResultSet rs = ps.executeQuery();
            Throwable var4 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (rs != null) {
                    if (var4 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var16) {
            //服务端输出信息.println_err("获取最高玩家等级出错 - 数据库查询失败：" + var16);
        }

        return data;
    }

    public static String 获取最高等级玩家名字() {
        String name = "";
        String level = "";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `level` FROM characters WHERE gm = 0 ORDER BY `level` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("level");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            //服务端输出信息.println_err("获取家族名称出错 - 数据库查询失败：" + var17);
        }

        return String.format("%s", name);
    }

    public static int 获取最高玩家人气() {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT MAX(fame) as DATA FROM characters WHERE gm = 0");
            ResultSet rs = ps.executeQuery();
            Throwable var4 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (rs != null) {
                    if (var4 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var16) {
            //服务端输出信息.println_err("获取最高玩家等级出错 - 数据库查询失败：" + var16);
        }

        return data;
    }

    public static String 获取最高人气玩家名字() {
        String name = "";
        String level = "";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `fame` FROM characters WHERE gm = 0 ORDER BY `fame` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("fame");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            //服务端输出信息.println_err("获取家族名称出错 - 数据库查询失败：" + var17);
        }

        return String.format("%s", name);
    }

    public static int 获取最高玩家金币() {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT MAX(meso) as DATA FROM characters WHERE gm = 0");
            ResultSet rs = ps.executeQuery();
            Throwable var4 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (rs != null) {
                    if (var4 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var16) {
            //服务端输出信息.println_err("获取最高玩家等级出错 - 数据库查询失败：" + var16);
        }

        return data;
    }

    public static String 获取最高金币玩家名字() {
        String name = "";
        String level = "";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `meso` FROM characters WHERE gm = 0 ORDER BY `meso` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("meso");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            //服务端输出信息.println_err("获取家族名称出错 - 数据库查询失败：" + var17);
        }

        return String.format("%s", name);
    }

    public static int 获取最高玩家在线() {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT MAX(totalOnlineTime) as DATA FROM characters WHERE gm = 0");
            ResultSet rs = ps.executeQuery();
            Throwable var4 = null;

            try {
                if (rs.next()) {
                    data = rs.getInt("DATA");
                }
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (rs != null) {
                    if (var4 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var16) {
            //服务端输出信息.println_err("获取最高玩家等级出错 - 数据库查询失败：" + var16);
        }

        return data;
    }

    public static String 获取最高在线玩家名字() {
        String name = "";
        String level = "";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `totalOnlineTime` FROM characters WHERE gm = 0 ORDER BY `totalOnlineTime` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                if (rs.next()) {
                    name = rs.getString("name");
                    level = rs.getString("totalOnlineTime");
                }
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            ps.close();
        } catch (SQLException var17) {
            //服务端输出信息.println_err("获取最高在线玩家名字 - 数据库查询失败：" + var17);
        }

        return String.format("%s", name);
    }

    public DamageManage.MobDamageData newDamageData() {
        return DamageManage.getInstance().newDamageData();
    }

    public DamageManage getDamageManage() {
        return DamageManage.getInstance();
    }

    public boolean sqlUpdate(String text, Object... value) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement(text);
                if (value != null && value.length > 0) {
                    for(int i = 1; i <= value.length; ++i) {
                        if (value[i - 1] instanceof Integer) {
                            ps.setInt(i, (Integer)value[i - 1]);
                        } else if (value[i - 1] instanceof String) {
                            ps.setString(i, (String)value[i - 1]);
                        } else if (value[i - 1] instanceof Double) {
                            ps.setDouble(i, (Double)value[i - 1]);
                        } else if (value[i - 1] instanceof Float) {
                            ps.setFloat(i, (Float)value[i - 1]);
                        } else if (value[i - 1] instanceof Long) {
                            ps.setLong(i, (Long)value[i - 1]);
                        } else if (value[i - 1] instanceof Boolean) {
                            ps.setBoolean(i, (Boolean)value[i - 1]);
                        } else if (value[i - 1] instanceof Date) {
                            ps.setDate(i, (Date)value[i - 1]);
                        } else {
                            boolean var7;
                            if (value[i - 1] instanceof Integer[]) {
                                if (((Integer[])((Integer[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setInt(i, ((Integer[])((Integer[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof int[]) {
                                if (((int[])((int[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setInt(i, ((int[])((int[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof String[]) {
                                if (((String[])((String[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setString(i, ((String[])((String[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Double[]) {
                                if (((Double[])((Double[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setDouble(i, ((Double[])((Double[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Float[]) {
                                if (((Float[])((Float[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setFloat(i, ((Float[])((Float[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Long[]) {
                                if (((Long[])((Long[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setLong(i, ((Long[])((Long[])value[i - 1]))[0]);
                            } else if (value[i - 1] instanceof Boolean[]) {
                                if (((Boolean[])((Boolean[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setBoolean(i, ((Boolean[])((Boolean[])value[i - 1]))[0]);
                            } else {
                                if (!(value[i - 1] instanceof Date[])) {
                                    var7 = false;
                                    return var7;
                                }

                                if (((Date[])((Date[])value[i - 1])).length <= 0) {
                                    var7 = false;
                                    return var7;
                                }

                                ps.setDate(i, ((Date[])((Date[])value[i - 1]))[0]);
                            }
                        }
                    }
                }

                ps.executeUpdate();
                ps.close();
                boolean var31 = true;
                return var31;
            } catch (Throwable var27) {
                var4 = var27;
                throw var27;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var26) {
                            var4.addSuppressed(var26);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var29) {
            //服务端输出信息.println_err("【错误】sqlUpdate错误，原因：" + var29);
            var29.printStackTrace();
            return false;
        } catch (Exception var30) {
            //服务端输出信息.println_err("【错误】sqlUpdate错误，原因：" + var30);
            var30.printStackTrace();
            return false;
        }
    }

    public boolean sqlUpdate(String text) {
        return this.sqlUpdate(text, (Object[])null);
    }

    public boolean sqlInsert(String text, Object... value) {
        return this.sqlUpdate(text, value);
    }

    public boolean sqlInsert(String text) {
        return this.sqlUpdate(text);
    }

    public Object[] sqlSelect(String text, Object... value) {
        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement(text);
            if (value != null && value.length > 0) {
                for(int i = 1; i <= value.length; ++i) {
                    if (value[i - 1] instanceof Integer) {
                        ps.setInt(i, (Integer)value[i - 1]);
                    } else if (value[i - 1] instanceof String) {
                        ps.setString(i, (String)value[i - 1]);
                    } else if (value[i - 1] instanceof Double) {
                        ps.setDouble(i, (Double)value[i - 1]);
                    } else if (value[i - 1] instanceof Float) {
                        ps.setFloat(i, (Float)value[i - 1]);
                    } else if (value[i - 1] instanceof Long) {
                        ps.setLong(i, (Long)value[i - 1]);
                    } else if (value[i - 1] instanceof Boolean) {
                        ps.setBoolean(i, (Boolean)value[i - 1]);
                    } else if (value[i - 1] instanceof Date) {
                        ps.setDate(i, (Date)value[i - 1]);
                    } else if (value[i - 1] instanceof Integer[]) {
                        if (((Integer[])((Integer[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setInt(i, ((Integer[])((Integer[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof int[]) {
                        if (((int[])((int[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setInt(i, ((int[])((int[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof String[]) {
                        if (((String[])((String[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setString(i, ((String[])((String[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Double[]) {
                        if (((Double[])((Double[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setDouble(i, ((Double[])((Double[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Float[]) {
                        if (((Float[])((Float[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setFloat(i, ((Float[])((Float[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Long[]) {
                        if (((Long[])((Long[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setLong(i, ((Long[])((Long[])value[i - 1]))[0]);
                    } else if (value[i - 1] instanceof Boolean[]) {
                        if (((Boolean[])((Boolean[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setBoolean(i, ((Boolean[])((Boolean[])value[i - 1]))[0]);
                    } else {
                        if (!(value[i - 1] instanceof Date[])) {
                            return null;
                        }

                        if (((Date[])((Date[])value[i - 1])).length <= 0) {
                            return null;
                        }

                        ps.setDate(i, ((Date[])((Date[])value[i - 1]))[0]);
                    }
                }
            }

            ResultSet rs = ps.executeQuery();
            rs.last();
            Object[] ret = new Object[rs.getRow()];
            rs.beforeFirst();

            for(int count = 0; rs.next(); ++count) {
                int column = rs.getMetaData().getColumnCount();
                Map<String, Object> obj = new HashMap();

                for(int i = 1; i <= column; ++i) {
                    obj.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                }

                ret[count] = obj;
            }

            ps.close();
            rs.close();
            return ret;
        } catch (SQLException var11) {
            //服务端输出信息.println_err("【错误】sqlSelect错误，原因：" + var11);
            var11.printStackTrace();
            return null;
        }
    }

    public Object[] sqlSelect(String text) {
        return this.sqlSelect(text, (Object[])null);
    }

}
