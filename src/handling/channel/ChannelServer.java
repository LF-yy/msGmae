package handling.channel;

import java.sql.*;

import database.DatabaseConnection;
import gui.LtMS;
import handling.cashshop.CashShopServer;
import client.MapleClient;
import server.events.*;
import tools.*;
import handling.world.CheaterData;

import java.util.LinkedList;
import java.util.List;
import server.maps.MapleMapObject;
import java.util.Collections;
import constants.WorldConstants;
import client.MapleCharacter;
import java.util.Iterator;
import java.util.Map.Entry;
import handling.login.LoginServer;
import constants.ServerConfig;
import server.ServerProperties;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.EnumMap;
import java.util.HashMap;

import abc.离线人偶;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import server.life.PlayerNPC;
import server.shops.MaplePlayerShop;
import server.shops.HiredMerchant;
import server.MapleSquad;
import server.MapleSquad.MapleSquadType;
import java.util.Map;
import scripting.EventScriptManager;
import server.maps.MapleMapFactory;
import handling.mina.ServerConnection;
import configs.Config;

import java.io.Serializable;

public class ChannelServer implements Serializable
{
    // 表示玩家是否处于离线挂机状态
    public static boolean 离线挂机;
    // 服务器开始运行的时间戳
    public static long serverStartTime;
    // 配置对象，用于服务器配置管理
    private static Config cf;
    // 服务器端口
    private short port;
    // 经验率，玩家获得经验的倍数
    private float expRate = 1.0F;
    // 金币率，玩家获得金币的倍数
    private float mesoRate = 1.0F;
    // 掉落率，怪物掉落物品概率的倍数
    private float dropRate = 1.0F;
    // 默认服务器端口
    private static final short DEFAULT_PORT;
    // 通道号，标识服务器的频道
    public int channel;
    // 正在运行的商人ID
    private int running_MerchantID;
    // 正在运行的玩家商店ID
    private int running_PlayerShopID;
    // 套接字信息，用于网络连接
    private String socket;
    // 关闭服务器的状态
    private boolean shutdown;
    // 服务器关闭完成的状态
    private boolean finishedShutdown;
    // 喇叭静音状态
    private boolean MegaphoneMuteState;
    // 玩家存储，用于管理在线玩家
    private PlayerStorage players;
    // 服务器连接对象，用于处理客户端连接
    private ServerConnection acceptor;
    // 地图工厂，用于创建和管理地图实例
    private final MapleMapFactory mapFactory;
    // 事件脚本管理器，用于管理游戏内事件
    private EventScriptManager eventSM;
    // 通道服务器实例映射，用于管理所有频道服务器实例
    private static final Map<Integer, ChannelServer> instances;
    // 组队副本映射，用于管理所有类型的组队副本
    private final Map<MapleSquadType, MapleSquad> mapleSquads;
    // 商人映射，用于管理所有雇佣商人
    private final Map<Integer, HiredMerchant> merchants;
    // 玩家商店映射，用于管理所有玩家商店
    private final Map<Integer, MaplePlayerShop> playershops;
    // 玩家NPC映射，用于管理所有玩家创建的NPC
    private final Map<Integer, PlayerNPC> playerNPCs;
    // 商人锁，用于同步商人操作
    private final ReentrantReadWriteLock merchLock;
    // 组队副本锁，用于同步组队副本操作
    private final ReentrantReadWriteLock squadLock;
    // 事件地图ID，标识当前激活的事件地图
    private int eventmap;
    // 事件映射，用于管理所有类型的事件
    private final Map<MapleEventType, MapleEvent> events;
    // 离线人偶列表，用于管理所有离线玩家的人偶
    public static ArrayList<离线人偶> clones;
    // 实例ID，用于唯一标识每个服务器实例
    private int instanceId = 0;

    /**
     * 获取当前实例的ID
     *
     * @return 实例ID
     */
    public int getInstanceId() {
        return this.instanceId;
    }

    public void addInstanceId() {
        ++this.instanceId;
    }
    private ChannelServer(final int channel) {
        this.port = (short)Integer.parseInt(ChannelServer.cf.getConfig("LtMS.channel.port1"));
        this.running_MerchantID = 0;
        this.running_PlayerShopID = 0;
        this.shutdown = false;
        this.finishedShutdown = false;
        this.MegaphoneMuteState = false;
        this.mapleSquads = new ConcurrentEnumMap<MapleSquadType, MapleSquad>(MapleSquadType.class);
        this.merchants = new HashMap<Integer, HiredMerchant>();
        this.playershops = new HashMap<Integer, MaplePlayerShop>();
        this.playerNPCs = new HashMap<Integer, PlayerNPC>();
        this.merchLock = new ReentrantReadWriteLock();
        this.squadLock = new ReentrantReadWriteLock();
        this.eventmap = -1;
        this.events = new EnumMap<MapleEventType, MapleEvent>(MapleEventType.class);
        this.channel = channel;
        (this.mapFactory = new MapleMapFactory()).setChannel(channel);
        this.expRate = WorldConstants.EXP_RATE;
        this.mesoRate = WorldConstants.MESO_RATE;
        this.dropRate = WorldConstants.DROP_RATE;
    }

    public static Set<Integer> getAllChannels() {
        return new HashSet<Integer>((Collection<? extends Integer>)ChannelServer.instances.keySet());
    }
    public void loadEvents() {
        if (this.events.isEmpty()) {
            this.events.put(MapleEventType.打瓶盖比赛, new MapleCoconut(this.channel, MapleEventType.打瓶盖比赛.mapids));
            this.events.put(MapleEventType.打椰子比赛, new MapleCoconut(this.channel, MapleEventType.打椰子比赛.mapids));
            this.events.put(MapleEventType.上楼上楼, new MapleOla(this.channel, MapleEventType.上楼上楼.mapids));
            this.events.put(MapleEventType.OX答题比赛, new MapleOxQuiz(this.channel, MapleEventType.OX答题比赛.mapids));
            this.events.put(MapleEventType.推雪球比赛, new MapleSnowball(this.channel, MapleEventType.推雪球比赛.mapids));
            this.events.put(MapleEventType.寻宝, new MapleJewel(this.channel, MapleEventType.寻宝.mapids));
            this.events.put(MapleEventType.向高地比赛, new MapleFitness(this.channel, MapleEventType.向高地比赛.mapids));
            this.events.put(MapleEventType.家族对抗赛, new MapleGuildMatch(this.channel, MapleEventType.家族对抗赛.mapids));
            this.events.put(MapleEventType.家族野外BOSS赛, new MapleGuildOutsideBoss(this.channel, MapleEventType.家族野外BOSS赛.mapids));
            this.events.put(MapleEventType.怪物攻城, new MonsterComming(this.channel, MapleEventType.怪物攻城.mapids));
        }
    }
    public static void reloadExpRate() {
        Iterator var0 = getAllInstances().iterator();

        while(var0.hasNext()) {
            ChannelServer cs = (ChannelServer)var0.next();
            cs.setExpRate(WorldConstants.EXP_RATE);
        }

    }
    public void setup() {
        this.setChannel(this.channel);
        try {
            this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("LtMS.events").split(","));
            this.port = (short)(ServerProperties.getProperty("LtMS.channel.port", ChannelServer.DEFAULT_PORT) + this.channel - 1);
        }
        catch (Exception e) {
            throw new RuntimeException((Throwable)e);
        }
        this.socket = ServerConfig.IP + ":" + (int)this.port;
        this.players = new PlayerStorage(this.channel);
        this.loadEvents();
        (this.acceptor = new ServerConnection((int)this.port, 0, this.channel)).run();
        System.out.println("[正在启动] 频道" + this.getChannel() + "端口:" + (int)this.port );
        this.eventSM.init();
    }
    public float getMesoRateSpecial() {
        return ServerConfig.getMyChannelMesoRate(this.channel);
    }
    public float getExpRateSpecial() {
        return ServerConfig.getMyChannelExpRate(this.channel);
    }

    public void shutdown() {
        if (this.finishedShutdown) {
            return;
        }
        this.broadcastPacket(MaplePacketCreator.serverNotice(0, "[频道" + this.getChannel() + "] 频道正在关闭"));
        this.shutdown = true;
        System.out.println("[频道" + this.getChannel() + "] 保存角色资料");
        System.out.println("[频道" + this.getChannel() + "] 解除端口绑定中");
        try {
            if (this.acceptor != null) {
                this.acceptor.close();
                System.out.println("[频道" + this.getChannel() + "] 解除端口成功");
            }
        }
        catch (Exception e) {
            System.out.println("[频道" + this.getChannel() + "] 解除端口失败");
        }
        ChannelServer.instances.remove((Object)Integer.valueOf(this.channel));
        LoginServer.removeChannel(this.channel);
        this.setFinishShutdown();
    }
    
    /**
     * 关闭所有商家的店铺
     *
     * 此方法遍历所有雇佣的商家，并要求它们关闭店铺然后从商家列表中移除这些商家
     * 使用写锁确保在关闭和移除商家的过程中其他线程不会读取或修改商家列表
     */
    public void closeAllMerchants() {
        // 初始化返回值，用于记录关闭的商家数量
        int ret = 0;
        // 记录开始时间，用于计算操作耗时
        final long Start = System.currentTimeMillis();
        // 获取写锁，确保在操作过程中其他线程不会访问商家列表
        this.merchLock.writeLock().lock();
        try {
            // 遍历商家列表的迭代器
            final Iterator<Map.Entry<Integer, HiredMerchant>> hmit = this.merchants.entrySet().iterator();
            while (hmit.hasNext()) {
                // 调用每个商家的关闭店铺方法，并移除商家列表中的该商家
                ((HiredMerchant)(hmit.next()).getValue()).closeShop(true, false);
                hmit.remove();
                // 增加关闭的商家数量计数
                ++ret;
            }
        }
        catch (Exception e) {
            // 异常处理：输出关闭店铺过程中发生的异常信息
            System.out.println("关闭雇佣商店出现错误" + (Object)e);
        }
        finally {
            // 释放写锁
            this.merchLock.writeLock().unlock();
        }
        // 输出关闭店铺操作的总结信息，包括频道号、关闭的商家数量和操作耗时
        System.out.println("频道 " + this.channel + " 共保存雇佣商店: " + ret + " | 耗时: " + (System.currentTimeMillis() - Start) + " 毫秒");
    }

    public final boolean hasFinishedShutdown() {
        return this.finishedShutdown;
    }
    
    public final MapleMapFactory getMapFactory() {
        return this.mapFactory;
    }
    
    public void addPlayer(MapleCharacter chr) {
        this.getPlayerStorage().registerPlayer(chr);
        if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"滚动公告开关")) >= 1) {
            chr.getClient().sendPacket(MaplePacketCreator.serverMessage(this.getServerMessage()));
        }
    }
    
    public final PlayerStorage getPlayerStorage() {
        if (this.players == null) {
            this.players = new PlayerStorage(this.channel);
        }
        return this.players;
    }
    
    public void removePlayer(MapleCharacter chr) {
        this.getPlayerStorage().deregisterPlayer(chr);
    }
    
    public void removePlayer(final int idz, final String namez) {
        this.getPlayerStorage().deregisterPlayer(idz, namez);
    }
    
    public final String getServerMessage() {
        return WorldConstants.SCROLL_MESSAGE;
    }
    
    public void setServerMessage(final String newMessage) {
        WorldConstants.SCROLL_MESSAGE = newMessage;
    }
    
    public void broadcastPacket(final byte[] data) {
        this.getPlayerStorage().broadcastPacket(data);
    }
    
    public void broadcastSmegaPacket(final byte[] data) {
        this.getPlayerStorage().broadcastSmegaPacket(data);
    }
    
    public void broadcastGashponmegaPacket( byte[] data) {
        this.getPlayerStorage().broadcastGashponmegaPacket(data);
    }
    
    public void broadcastGMPacket( byte[] data) {
        this.getPlayerStorage().broadcastGMPacket(data);
    }
    
    public void broadcastGMPacket( byte[] data,  boolean 吸怪) {
        this.getPlayerStorage().broadcastGMPacket(data, 吸怪);
    }
    
    public  float getExpRate() {
        return WorldConstants.EXP_RATE;
    }
    
    public void setExpRate(final float expRate) {
        WorldConstants.EXP_RATE = expRate;
    }
    public static void reloadDropRate() {
        Iterator var0 = getAllInstances().iterator();

        while(var0.hasNext()) {
            ChannelServer cs = (ChannelServer)var0.next();
            cs.setDropRate(WorldConstants.DROP_RATE);
        }

    }

    public static void reloadMesoRate() {
        Iterator var0 = getAllInstances().iterator();

        while(var0.hasNext()) {
            ChannelServer cs = (ChannelServer)var0.next();
            cs.setMesoRate(WorldConstants.MESO_RATE);
        }

    }

    public static boolean isOnlineByAccId(MapleClient client, int accid) {
        boolean online = false;
        Iterator var3 = getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer ch = (ChannelServer)var3.next();
            Collection<MapleCharacter> chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
            Iterator var6 = chrs.iterator();

            while(var6.hasNext()) {
                MapleCharacter c = (MapleCharacter)var6.next();
                if (c.getAccountID() == accid) {
                    try {
                        if (c.getClient() != null && c.getClient() != client) {
                            c.saveToDB(false, false);
                            online = true;
                        }
                    } catch (Exception var11) {
                        return online;
                    }

                    chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
                    if (chrs.contains(c)) {
                        c.saveToDB(false, false);
                        online = true;
                    }
                }
            }
        }

        try {
            Collection<MapleCharacter> chrs = CashShopServer.getPlayerStorage().getAllCharactersThreadSafe();
            Iterator var13 = chrs.iterator();

            while(var13.hasNext()) {
                MapleCharacter c = (MapleCharacter)var13.next();
                if (c.getAccountID() == accid) {
                    try {
                        if (c.getClient() != null && c.getClient() != client) {
                            c.saveToDB(false, true);
                            online = true;
                        }
                    } catch (Exception var9) {
                        return online;
                    }
                }
            }

            return online;
        } catch (Exception var10) {
            return online;
        }
    }
    public  float getMesoRate() {
        return WorldConstants.MESO_RATE;
    }
    
    public void setMesoRate( float mesoRate) {
        WorldConstants.MESO_RATE = mesoRate;
    }
    
    public  float getDropRate() {
        return WorldConstants.DROP_RATE;
    }
    
    public void setDropRate( float dropRate) {
        WorldConstants.DROP_RATE = dropRate;
    }
    
    public  int getChannel() {
        return this.channel;
    }
    
    public void setChannel( int channel) {
        ChannelServer.instances.put(Integer.valueOf(channel), this);
        LoginServer.addChannel(channel);
    }
    
    /**
     * 获取所有实例的不可修改集合
     *
     * 此方法提供了一个安全的方式来访问所有ChannelServer的实例它返回一个不可修改的集合，
     * 防止外部代码意外修改实例集合
     *
     * @return Collection<ChannelServer> 所有ChannelServer实例的不可修改集合
     */
    public static Collection<ChannelServer> getAllInstances() {
        return Collections.unmodifiableCollection((Collection<? extends ChannelServer>)ChannelServer.instances.values());
    }
    
    public  String getSocket() {
        return this.socket;
    }
    
    public  boolean isShutdown() {
        return this.shutdown;
    }
    
    public  int getLoadedMaps() {
        return this.mapFactory.getLoadedMaps();
    }
    
    public  EventScriptManager getEventSM() {
        return this.eventSM;
    }

    public void reloadEvents() {
        this.eventSM.cancel();
        this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("LtMS.events").split(","));
        this.eventSM.init();
    }
    
    public Map<MapleSquadType, MapleSquad> getAllSquads() {
        return Collections.unmodifiableMap((Map<? extends MapleSquadType, ? extends MapleSquad>)this.mapleSquads);
    }
    
    public  MapleSquad getMapleSquad(final String type) {
        return this.getMapleSquad(MapleSquadType.valueOf(type.toLowerCase()));
    }
    
    public  MapleSquad getMapleSquad(final MapleSquadType type) {
        return (MapleSquad)this.mapleSquads.get((Object)type);
    }
    
    public  boolean addMapleSquad( MapleSquad squad,  String type) {
         MapleSquadType types = MapleSquadType.valueOf(type.toLowerCase());
        if (types != null && !this.mapleSquads.containsKey((Object)types)) {
            this.mapleSquads.put(types, squad);
            squad.scheduleRemoval();
            return true;
        }
        return false;
    }
    
    public  boolean removeMapleSquad( MapleSquadType types) {
        if (types != null && this.mapleSquads.containsKey((Object)types)) {
            this.mapleSquads.remove((Object)types);
            return true;
        }
        return false;
    }
    
    public final int closeAllPlayerShop() {
        int ret = 0;
        this.merchLock.writeLock().lock();
        try {
            final Iterator<Entry<Integer, MaplePlayerShop>> playershops_ = this.playershops.entrySet().iterator();
            while (playershops_.hasNext()) {
                final MaplePlayerShop hm = (MaplePlayerShop)((Entry<Integer, MaplePlayerShop>)playershops_.next()).getValue();
                hm.closeShop(true, false);
                hm.getMap().removeMapObject((MapleMapObject)hm);
                playershops_.remove();
                ++ret;
            }
        }
        finally {
            this.merchLock.writeLock().unlock();
        }
        return ret;
    }
    
    /**
     * 关闭所有商家的店铺
     * 此方法用于关闭当前管理的所有雇佣商家的店铺，并将它们从所在的地图中移除
     * 使用写锁确保在关闭店铺期间，其他操作不会对商家数据进行修改
     *
     * @return 成功关闭的店铺数量
     */
    public final int closeAllMerchant() {
        int ret = 0;
        // 加锁，确保线程安全
        this.merchLock.writeLock().lock();
        try {
            // 遍历当前管理的所有商家
            final Iterator<Entry<Integer, HiredMerchant>> merchants_ = this.merchants.entrySet().iterator();
            while (merchants_.hasNext()) {
                // 获取当前遍历到的商家
                final HiredMerchant hm = (HiredMerchant)((Entry<Integer, HiredMerchant>)merchants_.next()).getValue();
                // 关闭商家的店铺
                hm.closeShop(true, false);
                // 从商家所在地图中移除
                hm.getMap().removeMapObject((MapleMapObject)hm);
                // 从管理列表中移除
                merchants_.remove();
                // 计数增加
                ++ret;
            }
        }
        // 无论如何都要释放锁
        finally {
            this.merchLock.writeLock().unlock();
        }
        // 遍历特定范围的商家地图ID，关闭并移除所有雇佣商家
        for (int i = 910000001; i <= 910000022; ++i) {
            for (final MapleMapObject mmo : this.mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                // 关闭商家的店铺
                ((HiredMerchant)mmo).closeShop(true, false);
                // 计数增加
                ++ret;
            }
        }
        // 返回成功关闭的店铺数量
        return ret;
    }
    
    public final int addPlayerShop(final MaplePlayerShop PlayerShop) {
        this.merchLock.writeLock().lock();
        int runningmer = 0;
        try {
            runningmer = this.running_PlayerShopID;
            this.playershops.put(Integer.valueOf(this.running_PlayerShopID), PlayerShop);
            ++this.running_PlayerShopID;
        }
        finally {
            this.merchLock.writeLock().unlock();
        }
        return runningmer;
    }
    
    /**
     * 添加商家
     *
     * 此方法用于向系统中添加一个新的商家对象在执行此操作之前，它会获取写锁以确保线程安全
     * 在尝试添加商家时，如果发生异常，此方法将释放写锁以避免死锁
     *
     * @param hMerchant 要添加的商家对象，不能为空
     * @return 返回分配给商家的ID，表示商家已成功添加到系统中
     */
    public final int addMerchant(final HiredMerchant hMerchant) {
        // 获取写锁，确保在添加商家时线程安全
        this.merchLock.writeLock().lock();
        int runningmer = 0;
        try {
            // 获取当前的商家ID，并将新的商家添加到商家集合中
            runningmer = this.running_MerchantID;
            this.merchants.put(Integer.valueOf(this.running_MerchantID), hMerchant);
            // 更新下一个商家ID
            ++this.running_MerchantID;
        }
        finally {
            // 无论是否发生异常，都释放写锁
            this.merchLock.writeLock().unlock();
        }
        // 返回分配给商家的ID
        return runningmer;
    }
    
    /**
     * 移除商户
     *
     * 此方法用于从系统中移除一个已雇佣的商户在执行此操作时，需要持有写锁
     * 以确保数据一致性，防止在移除操作过程中其他操作对商户数据进行修改
     *
     * @param hMerchant 要移除的商户对象，包含商户的相关信息，特别是商户的商店ID
     */
    public void removeMerchant(final HiredMerchant hMerchant) {
        // 获取写锁，确保在移除商户的过程中其他操作不会对商户数据进行修改
        this.merchLock.writeLock().lock();
        try {
            // 从商户集合中移除指定商店ID对应的商户对象
            this.merchants.remove((Object)Integer.valueOf(hMerchant.getStoreId()));
        }
        finally {
            // 释放写锁，确保资源可用性，避免死锁和资源泄露
            this.merchLock.writeLock().unlock();
        }
    }
    /**
     * 检查是否存在指定账户ID的商人
     * 此方法使用读锁来防止数据在检查期间被修改，确保数据一致性
     *
     * @param accid 要检查的商人账户ID
     * @return 如果存在具有指定账户ID的商人，则返回true；否则返回false
     */
    public final boolean containsMerchant(final int accid) {
        boolean contains = false;
        // 获取读锁以确保数据一致性
        this.merchLock.readLock().lock();
        try {
            final Iterator itr = this.merchants.values().iterator();
            while (itr.hasNext()) {
                if (((HiredMerchant)itr.next()).getOwnerAccId() == accid) {
                    contains = true;
                    break;
                }
            }
        }
        // 释放读锁
        finally {
            this.merchLock.readLock().unlock();
        }
        return contains;
    }

    /**
     * 搜索拥有特定物品的商人
     * 此方法遍历所有商人，查找其是否有匹配的物品，如果有，则将商人添加到结果列表中
     * 使用读锁来防止数据在搜索期间被修改，确保搜索结果的准确性
     *
     * @param itemSearch 要搜索的物品ID
     * @return 包含匹配物品的商人列表
     */
    public final List<HiredMerchant> searchMerchant(final int itemSearch) {
        final List<HiredMerchant> list = new LinkedList<HiredMerchant>();
        // 获取读锁以确保数据一致性
        this.merchLock.readLock().lock();
        try {
            for (final HiredMerchant hm : this.merchants.values()) {
                if (hm.searchItem(itemSearch).size() > 0) {
                    list.add(hm);
                }
            }
        }
        // 释放读锁
        finally {
            this.merchLock.readLock().unlock();
        }
        return list;
    }
    
    public void toggleMegaphoneMuteState() {
        this.MegaphoneMuteState = !this.MegaphoneMuteState;
    }
    
    public final boolean getMegaphoneMuteState() {
        return this.MegaphoneMuteState;
    }
    
    public int getEvent() {
        return this.eventmap;
    }
    
    public void setEvent(final int ze) {
        this.eventmap = ze;
    }
    
    public MapleEvent getEvent(final MapleEventType t) {
        return (MapleEvent)this.events.get((Object)t);
    }
    
    public final Collection<PlayerNPC> getAllPlayerNPC() {
        return this.playerNPCs.values();
    }
    
    public final PlayerNPC getPlayerNPC(final int id) {
        return (PlayerNPC)this.playerNPCs.get((Object)Integer.valueOf(id));
    }
    
    public void addPlayerNPC(final PlayerNPC npc) {
        if (this.playerNPCs.containsKey((Object)Integer.valueOf(npc.getId()))) {
            this.removePlayerNPC(npc);
        }
        this.playerNPCs.put(Integer.valueOf(npc.getId()), npc);
        this.getMapFactory().getMap(npc.getMapId()).addMapObject((MapleMapObject)npc);
    }
    
    public void removePlayerNPC(final PlayerNPC npc) {
        if (this.playerNPCs.containsKey((Object)Integer.valueOf(npc.getId()))) {
            this.playerNPCs.remove((Object)Integer.valueOf(npc.getId()));
            this.getMapFactory().getMap(npc.getMapId()).removeMapObject((MapleMapObject)npc);
        }
    }
    
    public final String getServerName() {
        return ServerConfig.SERVERNAME;
    }
    
    public void setServerName(final String sn) {
        ServerConfig.SERVERNAME = sn;
    }
    
    public final int getPort() {
        return this.port;
    }
    
    public void setPrepareShutdown() {
        this.shutdown = true;
        System.out.println("[頻道" + this.getChannel() + "] 准备关闭");
    }
    
    public void setFinishShutdown() {
        this.finishedShutdown = true;
        System.out.println("[頻道" + this.getChannel() + "] 已经关闭完成.");
    }
    
    public final boolean isAdminOnly() {
        return WorldConstants.ADMIN_ONLY;
    }
    
    public static Map<Integer, Integer> getChannelLoad() {
        final Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        for (final ChannelServer cs : ChannelServer.instances.values()) {
            ret.put(Integer.valueOf(cs.getChannel()), Integer.valueOf(cs.getConnectedClients()));
        }
        return ret;
    }
    
    public int getConnectedClients() {
        final double bfb = (double)LoginServer.getRSGS() / 100.0 * (double)this.getPlayerStorage().getConnectedClients();
        return this.getPlayerStorage().getConnectedClients() + (int)Math.ceil(bfb);
    }
    
    public List<CheaterData> getCheaters() {
        final List<CheaterData> cheaters = this.getPlayerStorage().getCheaters();
        Collections.sort(cheaters);
        return CollectionUtil.copyFirst(cheaters, 20);
    }
    
    public void broadcastMessage(final byte[] message) {
        this.broadcastPacket(message);
    }
    
    public void broadcastSmega(final byte[] message) {
        this.broadcastSmegaPacket(message);
    }
    
    public void broadcastGashponmega(final byte[] message) {
        this.broadcastGashponmegaPacket(message);
    }
    
    public void broadcastGMMessage(final byte[] message, final boolean 吸怪) {
        this.broadcastGMPacket(message, 吸怪);
    }
    
    public void broadcastGMMessage(final byte[] message) {
        this.broadcastGMPacket(message);
    }
    
    public void saveAll() {
        int ppl = 0;
        int lastnumber = 0;
        final List<MapleCharacter> all = this.players.getAllCharactersThreadSafe();
        for (MapleCharacter chr : all) {
            try {
                final int res = chr.saveToDB(false, false);
                if (ChannelServer.离线挂机) {
                    lastnumber = this.getLastOfflineTime2();
                }
                if (res == 1) {
                    ++ppl;
                }
                else {
                    System.out.println("[自动存档] 角色:" + chr.getName() + " 储存失败");
                }
            }
            catch (Exception e) {
                FileoutputUtil.logToFile("logs/saveAll存檔保存数据异常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + chr.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + chr.getClient().getAccountName() + " 账号ID " + chr.getClient().getAccID() + " 角色名 " + chr.getName() + " 角色ID " + chr.getId());
                FileoutputUtil.outError("logs/saveAll存檔保存数据异常.txt", (Throwable)e);
            }
        }
    }
    
    public boolean CanGMItem() {
        return WorldConstants.GMITEMS;
    }
    
    public final int getMerchantMap(MapleCharacter chr) {
        final int ret = -1;
        for (int i = 910000001; i <= 910000022; ++i) {
            for (final MapleMapObject mmo : this.mapFactory.getMap(i).getAllHiredMerchantsThreadsafe()) {
                if (((HiredMerchant)mmo).getOwnerId() == chr.getId()) {
                    return this.mapFactory.getMap(i).getId();
                }
            }
        }
        return ret;
    }
    
    public static int getChannelCount() {
        return ChannelServer.instances.size();
    }
    
    public static void forceRemovePlayerByAccId(final MapleClient client, final int accid) {
        for (final ChannelServer ch : getAllInstances()) {
            Collection<MapleCharacter> chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
            for (final MapleCharacter c : chrs) {
                if (c.getAccountID() == accid) {
                    try {
                        if (c.getClient() != null && c.getClient() != client) {
                            c.getClient().unLockDisconnect();
                        }
                    }
                    catch (Exception ex) {}
                    chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
                    if (!chrs.contains((Object)c)) {
                        continue;
                    }
                    ch.removePlayer(c);
                }
            }
        }
        try {
            final Collection<MapleCharacter> chrs2 = CashShopServer.getPlayerStorage().getAllCharactersThreadSafe();
            for (final MapleCharacter c2 : chrs2) {
                if (c2.getAccountID() == accid) {
                    try {
                        if (c2.getClient() == null || c2.getClient() == client) {
                            continue;
                        }
                        c2.getClient().unLockDisconnect();
                    }
                    catch (Exception ex2) {}
                }
            }
        }
        catch (Exception ex3) {}
    }
    
    public static Set<Integer> getChannels() {
        return new HashSet<Integer>((Collection<? extends Integer>)ChannelServer.instances.keySet());
    }
    
    public static ChannelServer newInstance(final int channel) {
        return new ChannelServer(channel);
    }
    
    public static ChannelServer getInstance(final int channel) {
        return (ChannelServer)ChannelServer.instances.get((Object)Integer.valueOf(channel));
    }
    
    public static void startAllChannels() {
        ChannelServer.serverStartTime = System.currentTimeMillis();
        for (int channelCount = WorldConstants.CHANNEL_COUNT, i = 1; i <= Math.min(20, (channelCount > 0) ? channelCount : 1); ++i) {
            newInstance(i).setup();
        }
    }
    
    public static void startChannel(final int channel) {
        ChannelServer.serverStartTime = System.currentTimeMillis();
        if (channel <= WorldConstants.CHANNEL_COUNT) {
            newInstance(channel).setup();
        }
    }
    
    public static void forceRemovePlayerByCharName(final MapleClient client, final String Name) {
        for (final ChannelServer ch : getAllInstances()) {
            Collection<MapleCharacter> chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
            for (final MapleCharacter c : chrs) {
                if (c.getName().equalsIgnoreCase(Name)) {
                    try {
                        if (c.getClient() != null && c.getClient() != client) {
                            c.getClient().unLockDisconnect();
                        }
                    }
                    catch (Exception ex) {}
                    chrs = ch.getPlayerStorage().getAllCharactersThreadSafe();
                    if (chrs.contains((Object)c)) {
                        ch.removePlayer(c);
                    }
                    c.getMap().removePlayer(c);
                }
            }
        }
    }
    
    public static void forceRemovePlayerByCharNameFromDataBase(final MapleClient client, final List<String> Name) {
        for (final ChannelServer ch : getAllInstances()) {
            for (final String name : Name) {
                if (ch.getPlayerStorage().getCharacterByName(name) != null) {
                    final MapleCharacter c = ch.getPlayerStorage().getCharacterByName(name);
                    try {
                        if (c.getClient() != null && c.getClient() != client) {
                            c.getClient().unLockDisconnect();
                        }
                    }
                    catch (Exception ex) {}
                    if (ch.getPlayerStorage().getAllCharactersThreadSafe().contains((Object)c)) {
                        ch.removePlayer(c);
                    }
                    c.getMap().removePlayer(c);
                }
            }
        }
        for (final String name2 : Name) {
            if (CashShopServer.getPlayerStorage().getCharacterByName(name2) != null) {
                final MapleCharacter c2 = CashShopServer.getPlayerStorage().getCharacterByName(name2);
                try {
                    if (c2.getClient() == null || c2.getClient() == client) {
                        continue;
                    }
                    c2.getClient().unLockDisconnect();
                }
                catch (Exception ex2) {}
            }
        }
    }
    
    public int getLastOfflineTime2() {
        int retnumber = -1;
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            final PreparedStatement ps = conn.prepareStatement("TRUNCATE TABLE lefttime");
            ps.executeUpdate();
            final ArrayList<离线人偶> clone = ChannelServer.clones;
            for (final 离线人偶 jr : clone) {
                final PreparedStatement psu = conn.prepareStatement("insert into lefttime (accid,charid,x,y,chairid,lefttime,channel) values (?,?,?,?,?,?,?)");
                psu.setInt(1, jr.AccId);
                psu.setInt(2, jr.charId);
                psu.setInt(3, jr.x);
                psu.setInt(4, jr.y);
                psu.setInt(5, jr.chairId);
                psu.setLong(6, jr.liftTime);
                psu.setInt(7, jr.channel);
                psu.executeUpdate();
                psu.close();
            }
            ps.close();
            retnumber = 1;
        }
        catch (Exception Ex) {
            System.out.println("离线挂机数据保存异常" + (Object)Ex);
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception Ex2) {
                System.out.println("离线挂机数据保存自动关闭数据库异常" + (Object)Ex2);
            }
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception Ex2) {
                System.out.println("离线挂机数据保存自动关闭数据库异常" + (Object)Ex2);
            }
        }
        return retnumber;
    }
    
    static {
        ChannelServer.离线挂机 = Boolean.parseBoolean(ServerProperties.getProperty("LtMS.离线挂机"));
        ChannelServer.cf = new Config();
        DEFAULT_PORT = (short)Integer.parseInt(ChannelServer.cf.getConfig("LtMS.channel.port1"));
        instances = new HashMap<Integer, ChannelServer>();
        ChannelServer.clones = new ArrayList<离线人偶>();
    }

    public void guaiReloadEvents() {
        this.eventSM.cancel();
        (this.eventSM = new EventScriptManager(this, ServerProperties.getProperty("LtMS.events").split(","))).init();
    }
    private EventScriptManager eventSMA;
    private EventScriptManager eventSMB;
    private EventScriptManager eventSMC;
    public final EventScriptManager getEventSMA() {
        return this.eventSMA;
    }

    public final EventScriptManager getEventSMB() {
        return this.eventSMB;
    }

    public final EventScriptManager getEventSMC() {
        return this.eventSMC;
    }

    public void reloadEventsa() {
        this.eventSMA.cancel();
        (this.eventSMA = new EventScriptManager(this, ServerProperties.getProperty("LtMS.活动事件脚本").split(","))).init();
    }

    //重载指定事件
    public final boolean reloadEvent(String script) {
        boolean success = false;
        this.eventSM.cancel(script);
        if (this.eventSM.loadEntry(this, script) && this.eventSM.init(script)) {
            success = true;
        }

        return success;
    }
    public void reloadEventsb() {
        this.eventSMB.cancel();
        (this.eventSMB = new EventScriptManager(this, ServerProperties.getProperty("LtMS.BOSS事件脚本").split(","))).init();
    }

    public void reloadEventsc() {
        this.eventSMC.cancel();
        (this.eventSMC = new EventScriptManager(this, ServerProperties.getProperty("LtMS.自定义事件脚本").split(","))).init();
    }



}
