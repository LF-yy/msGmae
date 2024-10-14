package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import abc.离线人偶;
import bean.FieldSkills;
import bean.SuperSkills;
import client.*;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.MapConstants;
import constants.tzjc;
import database.DBConPool;
import database.DatabaseConnection;
import gui.LtMS;
import gui.服务端输出信息;
import gui.活动野外通缉;
import gui.进阶BOSS.进阶BOSS线程;
import handling.channel.ChannelServer;
import handling.channel.handler.InventoryHandler;
import handling.world.MapleParty;
import handling.world.MaplePartyCharacter;
import handling.world.PartyOperation;
import handling.world.World.Broadcast;
import handling.world.World.Find;
import io.netty.channel.Channel;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import scripting.EventManager;
import server.*;
import server.MapleCarnivalFactory.MCSkill;
import server.MapleSquad.MapleSquadType;
import server.Timer.MapTimer;
import server.Timer.WorldTimer;
import server.custom.bossrank.BossRankManager;
import server.events.MapleEvent;
import server.life.*;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import server.maps.MapleNodes.MonsterPoint;
import server.movement.LifeMovementFragment;
import tools.*;
import tools.packet.MobPacket;
import tools.packet.PetPacket;
import util.ListUtil;
import util.NumberUtils;

public class MapleMap
{
    
    private final Map<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> mapObjects;
    private final Map<MapleMapObjectType, ReentrantReadWriteLock> mapObjectLocks;
    private final List<MapleCharacter> characters;
    private final ReentrantReadWriteLock charactersLock;
    private boolean hasCheckIn = false;
    private int runningOid;
    private final Lock runningOidLock;
    private final Map<String, Integer> environment;
    private final List<Spawns> monsterSpawn;//存放野外boss刷新信息的变量
    private final AtomicInteger spawnedMonstersOnMap;
    private final Map<Integer, MaplePortal> portals;
    private final List<Integer> disconnectedClients;
    //private static final Map<Integer, HashMap<String, Integer>> PointsGained;
    private final byte channel;
    private boolean respawnOnePoint = false;
    private int mapid;
    private final float monsterRate;
    private float recoveryRate;
    private MapleFootholdTree footholds;
    private MapleMapEffect mapEffect;
    private short decHP;
    private short createMobInterval;
    private int consumeItemCoolTime;
    private int protectItem;
    private int decHPInterval;
    private int returnMapId;
    private int timeLimit;
    private int fieldLimit;
    private int maxRegularSpawn;
    private int fixedMob;
    private int forcedReturnMap;
    private int lvForceMove;
    private int lvLimit;
    private int permanentWeather;
    private boolean town;
    private boolean personalShop;
    private boolean everlast;
    private boolean dropsDisabled;
    private boolean gDropsDisabled;
    private boolean soaring;
    private boolean squadTimer;
    private boolean isSpawns;
    private String mapName;
    private String streetName;
    private String onUserEnter;
    private String onFirstUserEnter;
    private String speedRunLeader;
    private ScheduledFuture<?> squadSchedule;
    private ScheduledFuture<?> MulungDojoLeaveTask;
    private long speedRunStart;
    private long lastSpawnTime;
    private long lastHurtTime;
    private MapleNodes nodes;
    private MapleSquadType squad;
    private boolean clock;
    private boolean boat;
    private boolean docked;
    private boolean PapfightStart;
    private static boolean 特殊宠物吸取开关;
    private boolean hasSpawned = false;
    private static boolean 特殊宠物吸物开关;
    private static boolean 特殊宠物吸金开关;
    private static boolean 特殊宠物吸物无法使用地图开关;
    private static String[] 特殊宠物吸物无法使用地图;
    private static int 持有物道具;
    private short top;
    private short bottom;
    private short left;
    private short right;
    private boolean haveStone = false;
    private int stoneLevel = 0;
    private long clock_s_startTime = 0L;
    private int clock_s_duration = 0;
    private boolean clock_s;
    private ArrayList<Integer> ownerList = new ArrayList();
    public static boolean canSpawnForCPU = true;
    private final Map<Thread, Boolean> clock_s_thread_map = new LinkedHashMap<Thread, Boolean>() {
        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(Map.Entry<Thread, Boolean> pEldest) {
            return this.size() > MapleMap.this.maximumSize;
        }
    };
    int maximumSize = 1000000;
    public void setRespawnOnePoint(boolean respawnOnePoint) {
        this.respawnOnePoint = respawnOnePoint;
    }
    public int getStoneLevel() {
        return this.stoneLevel;
    }

    public void setStoneLevel(int stoneLevel) {
        this.stoneLevel = stoneLevel;
    }
    public boolean isHaveStone() {
        return this.haveStone;
    }
    public void setHaveStone(boolean haveStone) {
        this.haveStone = haveStone;
    }
    public ScheduledFuture<?> MobVacSchedule = null;
    public MapleMap(int mapid, int channel, int returnMapId, final float monsterRate) {
        this.characters = new LinkedList<MapleCharacter>();
        this.charactersLock = new ReentrantReadWriteLock();
        this.runningOid = 100000;
        this.runningOidLock = new ReentrantLock();
        this.environment = new LinkedHashMap<String, Integer>();
        this.monsterSpawn = new ArrayList<Spawns>();
        this.spawnedMonstersOnMap = new AtomicInteger(0);
        this.portals = new HashMap<Integer, MaplePortal>();
        this.disconnectedClients = new ArrayList<Integer>();
        this.footholds = null;
        this.decHP = 0;
        this.createMobInterval = 1000;
        this.consumeItemCoolTime = 0;
        this.protectItem = 0;
        this.decHPInterval = 10000;
        this.maxRegularSpawn = 0;
        this.forcedReturnMap = 999999999;
        this.lvForceMove = 0;
        this.lvLimit = 0;
        this.permanentWeather = 0;
        this.everlast = false;
        this.dropsDisabled = false;
        this.gDropsDisabled = false;
        this.soaring = false;
        this.squadTimer = false;
        this.isSpawns = true;
        this.speedRunLeader = "";
        this.squadSchedule = null;
        this.MulungDojoLeaveTask = null;
        this.speedRunStart = 0L;
        this.lastSpawnTime = 0L;
        this.lastHurtTime = 0L;
        this.PapfightStart = false;
        this.top = 0;
        this.bottom = 0;
        this.left = 0;
        this.right = 0;
        this.mapid = mapid;
        this.channel = (byte)channel;
        this.returnMapId = returnMapId;
        if (this.returnMapId == 999999999) {
            this.returnMapId = mapid;
        }
        if (GameConstants.isNotToMap(mapid)) {
            this.returnMapId = 211060000;
        }
        this.monsterRate = monsterRate;
        this.createMobInterval = Short.parseShort(String.valueOf(LtMS.ConfigValuesMap.get("地图刷新频率")));
        final EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>> objsMap = new EnumMap<MapleMapObjectType, LinkedHashMap<Integer, MapleMapObject>>(MapleMapObjectType.class);
        final EnumMap<MapleMapObjectType, ReentrantReadWriteLock> objlockmap = new EnumMap<MapleMapObjectType, ReentrantReadWriteLock>(MapleMapObjectType.class);
        for ( MapleMapObjectType type : MapleMapObjectType.values()) {
            objsMap.put(type, new LinkedHashMap<Integer, MapleMapObject>());
            objlockmap.put(type, new ReentrantReadWriteLock());
        }
        this.mapObjects = Collections.unmodifiableMap((Map<? extends MapleMapObjectType, ? extends LinkedHashMap<Integer, MapleMapObject>>)objsMap);
        this.mapObjectLocks = Collections.unmodifiableMap((Map<? extends MapleMapObjectType, ? extends ReentrantReadWriteLock>)objlockmap);
    }
    public Point getRespawnPoint() {
        return this.respawnPoint;
    }

    public void setRespawnPoint(Point respawnPoint) {
        this.respawnPoint = respawnPoint;
    }

    public final void startMapEffect_S(String msg, int itemId, int duration) {
        boolean jukebox = false;
        if (this.mapEffect == null) {
            this.mapEffect = new MapleMapEffect(msg, itemId);
            this.mapEffect.setJukebox(jukebox);
            this.broadcastMessage(this.mapEffect.makeStartData());
            MapTimer.getInstance().schedule(new Runnable() {
                public void run() {
                    MapleMap.this.broadcastMessage(MapleMap.this.mapEffect.makeDestroyData());
                    MapleMap.this.mapEffect = null;
                }
            }, jukebox ? 300000L : (long)duration);
        }
    }
    public void 地图回收() {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleParty.神秘商人时间 == 1) {
                    if (MapleMap.this.getAllMonstersThreadsafe().size() > 0 && MapleMap.this.getCharactersSize() == 0) {
                        for ( ChannelServer cserv : ChannelServer.getAllInstances()) {
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 系统正在回收地图 √ " + MapleMap.this.getId());
                            cserv.getMapFactory().destroyMap(MapleMap.this.getId(), true);
                            cserv.getMapFactory().HealMap(MapleMap.this.getId());
                        }
                    }
                    System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 系统正在回收地图 √");
                }
            }
        }, 30000L);
    }
    
    public void 妖僧地图回收() {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getId() == 702060000 && MapleMap.this.getAllMonstersThreadsafe().size() > 0 && MapleMap.this.getCharactersSize() == 0) {
                    MapleMap.this.清怪();
                    Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, ";妖僧副本已被重置。"));
                }
            }
        }, 60000L);
    }
    
    public void 定时召唤蜗牛王(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getMonsterById(2220000) == null) {
                    try {
                        final MapleMonster mob1 = MapleLifeFactory.getMonster(2220000);
                        MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(439, 185));
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤普通扎昆(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getAllMonstersThreadsafe().isEmpty()) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            MapleMap.this.spawnZakum(-10, -215);
                            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, ";扎昆在祭台出现了。"));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤暗黑龙王(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getAllMonstersThreadsafe().isEmpty()) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(8810026);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(71, 260));
                            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, ";暗黑龙王出现了。"));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤时间宠儿(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getId() == 270050100 && MapleMap.this.getAllMonstersThreadsafe().isEmpty() && MapleMap.this.getMonsterById(8820008) == null) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(8820008);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(2, -42));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤蝙蝠怪(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getId() == 105100300 && MapleMap.this.getMonsterById(8830000) == null && MapleMap.this.getMonsterById(8830001) == null && MapleMap.this.getMonsterById(8830002) == null) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(8830000);
                            final MapleMonster mob2 = MapleLifeFactory.getMonster(8830001);
                            final MapleMonster mob3 = MapleLifeFactory.getMonster(8830002);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(483, 258));
                            MapleMap.this.spawnMonsterOnGroundBelow(mob2, new Point(483, 258));
                            MapleMap.this.spawnMonsterOnGroundBelow(mob3, new Point(483, 258));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤混沌女王(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getMonsterById(8920102) == null) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(8920000);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(-2118, 86));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤月妙巨兔(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                    if (MapleMap.this.getChannel() != 1) {
                        final MapleMonster mob1 = MapleLifeFactory.getMonster(9500006);
                        MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(475, 35));
                    }
                }
                catch (Exception ex) {}
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤雷昂(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getAllMonstersThreadsafe().size() == 0) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(8840000);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(-570, 102));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤石像塔(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getMonsterById(2500360) == null) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(2500360);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(1927, 2205));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void 定时召唤守护塔(int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMap.this.getMonsterById(2500402) == null) {
                    try {
                        if (MapleMap.this.getChannel() != 1) {
                            final MapleMonster mob1 = MapleLifeFactory.getMonster(2500400);
                            MapleMap.this.spawnMonsterOnGroundBelow(mob1, new Point(821, 195));
                        }
                    }
                    catch (Exception ex) {}
                }
            }
        }, (long)(60000 * time));
    }
    
    public void setSpawns(final boolean fm) {
        this.isSpawns = fm;
    }
    
    public boolean getSpawns() {
        return this.isSpawns;
    }
    
    public void setFixedMob(int fm) {
        this.fixedMob = fm;
    }
    
    public void setForceMove(int fm) {
        this.lvForceMove = fm;
    }
    
    public int getForceMove() {
        return this.lvForceMove;
    }
    
    public void setLevelLimit(int fm) {
        this.lvLimit = fm;
    }
    
    public int getLevelLimit() {
        return this.lvLimit;
    }
    
    public void setReturnMapId(int rmi) {
        this.returnMapId = rmi;
    }
    
    public void setSoaring(final boolean b) {
        this.soaring = b;
    }
    
    public boolean canSoar() {
        return this.soaring;
    }
    
    public void toggleDrops() {
        this.dropsDisabled = !this.dropsDisabled;
    }
    
    public void setDrops(final boolean b) {
        this.dropsDisabled = b;
    }
    
    public void toggleGDrops() {
        this.gDropsDisabled = !this.gDropsDisabled;
    }
    
    public int getId() {
        return this.mapid;
    }
    
    public MapleMap getReturnMap() {
        return ChannelServer.getInstance((int)this.channel).getMapFactory().getMap(this.returnMapId);
    }
    
    public int getReturnMapId() {
        return this.returnMapId;
    }
    
    public int getForcedReturnId() {
        return this.forcedReturnMap;
    }
    
    public MapleMap getForcedReturnMap() {
        return ChannelServer.getInstance((int)this.channel).getMapFactory().getMap(this.forcedReturnMap);
    }
    
    public void setForcedReturnMap(int map) {
        this.forcedReturnMap = map;
    }
    
    public float getRecoveryRate() {
        return this.recoveryRate;
    }
    
    public void setRecoveryRate(final float recoveryRate) {
        this.recoveryRate = recoveryRate;
    }
    
    public int getFieldLimit() {
        return this.fieldLimit;
    }
    
    public void setFieldLimit(int fieldLimit) {
        this.fieldLimit = fieldLimit;
    }
    
    public void setCreateMobInterval(final short createMobInterval) {
        this.createMobInterval = createMobInterval;
    }
    
    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public void setMapName(final String mapName) {
        this.mapName = mapName;
    }
    
    public String getMapName() {
        return this.mapName;
    }
    
    public String getStreetName() {
        return this.streetName;
    }
    
    public void setFirstUserEnter(final String onFirstUserEnter) {
        this.onFirstUserEnter = onFirstUserEnter;
    }
    
    public void setUserEnter(final String onUserEnter) {
        this.onUserEnter = onUserEnter;
    }
    
    public boolean hasClock() {
        return this.clock;
    }
    
    public void setClock(final boolean hasClock) {
        this.clock = hasClock;
    }
    
    private int hasBoat() {
        return this.docked ? 2 : (this.boat ? 1 : 0);
    }
    
    public void setBoat(final boolean hasBoat) {
        this.boat = hasBoat;
    }
    
    public void setDocked(final boolean isDocked) {
        this.docked = isDocked;
    }
    
    public boolean isTown() {
        return this.town;
    }
    
    public void setTown(final boolean town) {
        this.town = town;
    }
    
    public boolean allowPersonalShop() {
        return this.personalShop;
    }
    
    public void setPersonalShop(final boolean personalShop) {
        this.personalShop = personalShop;
    }
    
    public void setStreetName(final String streetName) {
        this.streetName = streetName;
    }
    
    public void setEverlast(final boolean everlast) {
        this.everlast = everlast;
    }
    
    public boolean getEverlast() {
        return this.everlast;
    }
    
    public int getHPDec() {
        return this.decHP;
    }
    
    public void setHPDec(int delta) {
        if (delta > 0 || this.mapid == 749040100) {
            this.lastHurtTime = System.currentTimeMillis();
        }
        this.decHP = (short)delta;
    }
    
    public int getHPDecInterval() {
        return this.decHPInterval;
    }
    
    public void setHPDecInterval(int delta) {
        this.decHPInterval = delta;
    }
    
    public int getHPDecProtect() {
        return this.protectItem;
    }
    
    public void setHPDecProtect(int delta) {
        this.protectItem = delta;
    }
    
    public int getCurrentPartyId() {
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter chr : this.characters) {
                if (chr.getPartyId() != -1) {
                    return chr.getPartyId();
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return -1;
    }
    
    public void addMapObject(final MapleMapObject mapobject) {
        this.runningOidLock.lock();
        int newOid;
        try {
            newOid = ++this.runningOid;
        }
        finally {
            this.runningOidLock.unlock();
        }
        mapobject.setObjectId(newOid);
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)mapobject.getType())).writeLock().lock();
        try {
            ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)mapobject.getType())).put(Integer.valueOf(newOid), mapobject);
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)mapobject.getType())).writeLock().unlock();
        }
    }
    
    private void spawnAndAddRangedMapObject(final MapleMapObject mapobject, final DelayedPacketCreation packetbakery, final SpawnCondition condition) {
        this.addMapObject(mapobject);
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter chr : this.characters) {
                if ((condition == null || condition.canSpawn(chr)) && !chr.isClone() && chr.getPosition().distanceSq((Point2D)mapobject.getPosition()) <= (double)GameConstants.maxViewRangeSq()) {
                    packetbakery.sendPackets(chr.getClient());
                    chr.addVisibleMapObject(mapobject);
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
    }
    
    public void removeMapObject(final MapleMapObject obj) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)obj.getType())).writeLock().lock();
        try {
            ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)obj.getType())).remove((Object)Integer.valueOf(obj.getObjectId()));
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)obj.getType())).writeLock().unlock();
        }
    }
    
    public Point calcPointBelow(final Point initial) {
        final MapleFoothold fh = this.footholds.findBelow(initial);
        if (fh == null) {
            return null;
        }
        int dropY = fh.getY1();
        int dropX = (initial.x < this.left + 30) ? (this.left + 30) : ((initial.x > this.right - 30) ? (this.right - 30) : initial.x);
        if (!fh.isWall() && fh.getY1() != fh.getY2()) {
            final double s1 = (double)Math.abs(fh.getY2() - fh.getY1());
            final double s2 = (double)Math.abs(fh.getX2() - fh.getX1());
            if (fh.getY2() < fh.getY1()) {
                dropY = fh.getY1() - (int)(Math.cos(Math.atan(s2 / s1)) * ((double)Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            }
            else {
                dropY = fh.getY1() + (int)(Math.cos(Math.atan(s2 / s1)) * ((double)Math.abs(initial.x - fh.getX1()) / Math.cos(Math.atan(s1 / s2))));
            }
        }
        return new Point(dropX, dropY);
    }
    
    public Point calcDropPos(final Point initial, final Point fallback) {
        final Point ret = this.calcPointBelow(new Point(initial.x, initial.y - 50));
        if (ret == null) {
            return fallback;
        }
        return ret;
    }
    
    private void dropFromMonster(MapleCharacter chr, final MapleMonster mob) {
        if (mob == null || chr == null || ChannelServer.getInstance((int)this.channel) == null || this.dropsDisabled || mob.dropsDisabled() || chr.getPyramidSubway() != null) {
            return;
        }
        if (mapObjects.get(MapleMapObjectType.ITEM).size() >= LtMS.ConfigValuesMap.get("地图物品上限"))
        {
            removeDrops();
            chr.dropMessage(6, "[系统提示] : 当前地图物品数量已经达到限制，现在已被清除。");
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();


        final byte droptype = (byte)(mob.getStats().isExplosiveReward() ? 3 : (mob.getStats().isFfaLoot() ? 2 : ((chr.getParty() != null) ? 1 : 0)));
        
        int mobpos = mob.getPosition().x;
        int cmServerrate = ChannelServer.getInstance((int)this.channel).getMesoRate();
        int chServerrate = ChannelServer.getInstance((int)this.channel).getDropRate();
        byte d = 1;
        final Point pos = new Point(0, mob.getPosition().y);
        double showdown = 100.0;
        final MonsterStatusEffect mse = mob.getBuff(MonsterStatus.SHOWDOWN);
        if (mse != null) {
            showdown += (double)(int)mse.getX();
        }
        final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
       // final List<MonsterDropEntry> dropEntry = mi.retrieveDrop(mob.getId());
        List<MonsterDropEntry> dropEntry = Start.dropsMap.get(mob.getId());
        if (dropEntry == null) {
            return;
        }
        Collections.shuffle(dropEntry);
        boolean mesoDropped = false;
        boolean rand = false;
        int coefficient = 1;
        //4人组队爆率加成机制
        int cMap = chr.getMapId();
        try {
            if (Objects.nonNull(chr.getClient().getPlayer().getParty()) && chr.getClient().getPlayer().getParty().getMembers().size()>=4) {
                for ( MaplePartyCharacter cc : chr.getClient().getPlayer().getParty().getMembers()) {
                    if(cc==null){
                        continue;
                    }
                    if (cMap == cc.getMapid()) {
                        rand = true;
                    } else {
                        rand = false;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if (rand){
            coefficient = 2;
        }
        double jiac = 1  ;
        if (LtMS.ConfigValuesMap.get("开启破功爆率加成")>0) {
            //破功爆率加成机制
            int 获得破功 = chr.getClient().getPlayer().取破攻等级();
            jiac = 获得破功 >= 100 ? (获得破功 / LtMS.ConfigValuesMap.get("破功爆率加成计算")) * 0.01 + 1 : 1;
        }
        //修改指定地图爆率
        double dropCoefficient = 1.0;
        if (Start.dropCoefficientMap.get(chr.getMapId())!=null){
            dropCoefficient = Start.dropCoefficientMap.get(chr.getMapId())/100.0;
        }
        //动态爆率调整
        if (chr.get地图缓存1() >0 && chr.get地图缓存2() == chr.getMapId()) {
            dropCoefficient+= chr.get地图缓存1()/100.0;
        }

        for ( MonsterDropEntry de2 : dropEntry) {
            if (de2.itemId == mob.getStolen()) {
                continue;
            }
            int itemDropm = chr.getItemDropm()/100;
            final double lastDrop = (chr.getStat().dropBuff - 100.0 <= 0.0) ? 100.0 : chr.getStat().dropBuff ;
           int drop =  (int)(de2.chance * (chServerrate *  chr.getDropMod() * coefficient * jiac
                                        * chr.getDropm()
                                        * (showdown/100)
                                        * lastDrop / 100.0
                                        + itemDropm ));// *  lastDrop / 100.0 * (showdown / 100.0) * ((double)(chr.getVipExpRate() / 100) + 1.0)

            if (Randomizer.nextInt(999999) >= ((de2.itemId == 1012168 || de2.itemId == 1012169 || de2.itemId == 1012170 || de2.itemId == 1012171) ? de2.chance : drop * (LtMS.ConfigValuesMap.get("砍爆率")/100.0) ) * dropCoefficient) {
                continue;
            }
            if (mesoDropped && droptype != 3 && de2.itemId == 0) {
                continue;
            }
            if (de2.questid > 0 && chr.getQuestStatus((int)de2.questid) != 1) {
                continue;
            }
            if (de2.itemId / 10000 == 238 && !mob.getStats().isBoss() && chr.getMonsterBook().getLevelByCard(ii.getCardMobId(de2.itemId)) >= 2) {
                continue;
            }
            if (droptype == 3) {
                pos.x = mobpos + ((d % 2 == 0) ? (40 * (d + 1) / 2) : (-(40 * (d / 2))));
            }
            else {
                pos.x = mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : (-(25 * (d / 2))));
            }
            if (de2.itemId == 0) {
                //金币掉落
                int mesos = Randomizer.nextInt(1 + Math.abs(de2.Maximum - de2.Minimum)) + de2.Minimum;
                if (mesos > 0) {
                    this.spawnMobMesoDrop((int)((double)mesos * (chr.getStat().mesoBuff / 100.0) * (double)chr.getDropMod() * (double)cmServerrate), this.calcDropPos(pos, mob.getTruePosition()), (MapleMapObject)mob, chr, false, droptype);
                    mesoDropped = true;
                }
            }
            else {
                //物品掉落
                IItem idrop;
                if (GameConstants.getInventoryType(de2.itemId) == MapleInventoryType.EQUIP) {
                    idrop = ii.randomizeStats((Equip)ii.getEquipById(de2.itemId));
                }
                else {
                    int range = Math.abs(de2.Maximum - de2.Minimum);
                    idrop = new Item(de2.itemId, (short)0, (short)((de2.Maximum != 1) ? (Randomizer.nextInt((range <= 0) ? 1 : range) + de2.Minimum) : 1), (byte)0);
                }
                this.spawnMobDrop(idrop, this.calcDropPos(pos, mob.getPosition()), mob, chr, droptype, de2.questid);
            }
            ++d;
        }
        final List<MonsterGlobalDropEntry> globalEntry = new ArrayList<MonsterGlobalDropEntry>((Collection<? extends MonsterGlobalDropEntry>)mi.getGlobalDrop());
        Collections.shuffle(globalEntry);
        int cashz = (mob.getStats().isBoss() && mob.getStats().getHPDisplayType() == 0) ? 20 : 1;
        int cashModifier = (int)(mob.getStats().isBoss() ? 0L : ((long)(mob.getMobExp() / 1000) + mob.getMobMaxHp() / 10000L));
        //全局掉落
        for ( MonsterGlobalDropEntry de3 : globalEntry) {
            if (Randomizer.nextInt(999999) < de3.chance && (de3.continent < 0 || (de3.continent < 10 && this.mapid / 100000000 == de3.continent) || (de3.continent < 100 && this.mapid / 10000000 == de3.continent) || (de3.continent < 1000 && this.mapid / 1000000 == de3.continent))) {
                if (droptype == 3) {
                    pos.x = mobpos + ((d % 2 == 0) ? (40 * (d + 1) / 2) : (-(40 * (d / 2))));
                }
                else {
                    pos.x = mobpos + ((d % 2 == 0) ? (25 * (d + 1) / 2) : (-(25 * (d / 2))));
                }
                if (de3.itemId == 0) {
                    continue;
                }
                if (this.gDropsDisabled) {
                    continue;
                }
                IItem idrop;
                if (GameConstants.getInventoryType(de3.itemId) == MapleInventoryType.EQUIP) {
                    idrop = ii.randomizeStats((Equip)ii.getEquipById(de3.itemId));
                }
                else {
                    idrop = new Item(de3.itemId, (short)0, (short)((de3.Maximum != 1) ? (Randomizer.nextInt(de3.Maximum - de3.Minimum) + de3.Minimum) : 1), (byte)0);
                }
                this.spawnMobDrop(idrop, this.calcDropPos(pos, mob.getPosition()), mob, chr, (byte)(de3.onlySelf ? 0 : droptype), de3.questid);
                ++d;
            }
        }
        
    }
    
    public void removeMonster(final MapleMonster monster) {
        if (monster == null) {
            return;
        }
        this.spawnedMonstersOnMap.decrementAndGet();
        this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 0));
        this.removeMapObject((MapleMapObject)monster);
        monster.killed();
    }
    
    private void killMonster(final MapleMonster monster) {
        this.spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0L);
        monster.spawnRevives(this);
        this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 1));
        this.removeMapObject((MapleMapObject)monster);
    }
    
    public void killMonster(final MapleMonster monster, MapleCharacter chr, final boolean withDrops, final boolean second, final byte animation) {
        this.killMonster(monster, chr, withDrops, second, animation, 0);
    }
    
    public void 地图杀怪(final MapleMonster monster, MapleCharacter chr) {
        int mobid = monster.getId();
        //击杀BOSS
        if (monster.getStats().isBoss()){
            //chr.BOSS记录累计.put(mobid,Objects.isNull(chr.BOSS记录累计.get(mobid)) ? 1:chr.BOSS记录累计.get(mobid)+1);
            chr.setBossLog(mobid+"",1,1);
           // chr.BOSS记录.put(mobid,Objects.isNull(chr.BOSS记录.get(mobid)) ? 1:chr.BOSS记录.get(mobid)+1);
            chr.setBossLog(mobid+"",0,1);

            chr.打Boss数量++;
        }else{
            chr.打怪数量++;
        }
        if (mobid == MapleParty.通缉BOSS && this.mapid == MapleParty.通缉地图) {
            MapleParty.通缉BOSS = 0;
            MapleParty.通缉地图 = 0;
            final String 信息 = "[野外通缉] : " + chr.getName() + " 完成了此次通缉令，下一次通缉令将在 1 小时后发布。";
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, 信息));
            chr.击杀野外BOSS特效2();
            chr.打开奖励();
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3600000L);
                        活动野外通缉.随机通缉();
                    }
                    catch (InterruptedException ex) {}
                }
            }.start();
        }
        else if (mobid == 9500337 && this.mapid == 104000400) {
            进阶BOSS线程.关闭进阶BOSS线程();
        }
        else if (mobid == 8810018 ) {
            chr.setBossLog("击杀黑龙",1,1);
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "击杀黑龙", (byte)2, 1);
        }
        else if (mobid == 2220000 && this.mapid == 104000400) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[红蜗牛王屠杀令]: " + chr.getName() + " 在海岸草丛III击杀了红蜗牛王"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("每日击杀红蜗牛王");
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀红蜗牛王", (byte)2, 1);
        }
        else if (mobid == 3220000 && this.mapid == 101030404) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[树妖王屠杀令]: " + chr.getName() + " 在东部岩山Ⅴ击杀了树妖王"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀树妖王");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀树妖王", (byte)2, 1);
        }
        else if (mobid == 8520000 && this.mapid == 230040420) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[鱼王屠杀令]: " + chr.getName() + " 水下世界 皮亚努斯洞穴击杀了鱼王左"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀鱼王");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀鱼王左", (byte)2, 1);
        }
        else if (mobid == 8510000 && this.mapid == 230040420) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[鱼王屠杀令]: " + chr.getName() + " 水下世界 皮亚努斯洞穴击杀了鱼王右"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀鱼王");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀鱼王右", (byte)2, 1);
        }
        else if (mobid == 5220001 && this.mapid == 110040000) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[巨居蟹屠杀令]: " + chr.getName() + " 在阳光沙滩击杀了巨居蟹"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀巨居蟹");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀巨居蟹", (byte)2, 1);
        }
        else if (mobid == 7220000 && this.mapid == 250010304) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[肯德熊屠杀令]: " + chr.getName() + " 在流浪熊的地盘击杀了肯德熊"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀肯德熊");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀肯德熊", (byte)2, 1);
        }
        else if (mobid == 8220000 && this.mapid == 200010300) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[艾利杰屠杀令]: " + chr.getName() + " 在天空楼梯Ⅱ击杀了艾利杰"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀艾利杰");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀艾利杰", (byte)2, 1);
        }
        else if (mobid == 7220002 && this.mapid == 250010503) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[妖怪禅师屠杀令]: " + chr.getName() + " 在妖怪森林击杀了妖怪禅师"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀妖怪禅师");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀妖怪禅师", (byte)2, 1);
        }
        else if (mobid == 7220001 && this.mapid == 222010310) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[九尾狐屠杀令]: " + chr.getName() + " 在月岭击杀了九尾狐"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀九尾狐");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀九尾狐", (byte)2, 1);
        }
        else if (mobid == 6220000 && this.mapid == 107000300) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[多尔屠杀令]: " + chr.getName() + " 在鳄鱼潭Ⅰ击杀了多尔"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀多尔");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀多尔", (byte)2, 1);
        }
        else if (mobid == 5220002 && this.mapid == 100040105) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[浮士德屠杀令]: " + chr.getName() + " 在巫婆森林Ⅰ击杀了浮士德"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀浮士德");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀浮士德", (byte)2, 1);
        }
        else if (mobid == 5220003 && this.mapid == 220050100) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[提莫屠杀令]: " + chr.getName() + " 在时间漩涡击杀了提莫"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀提莫");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀提莫", (byte)2, 1);
        }
        else if (mobid == 6220001 && this.mapid == 221040301) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[朱诺屠杀令]: " + chr.getName() + " 在哥雷草原击杀了朱诺"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀朱诺");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀朱诺", (byte)2, 1);
        }
        else if (mobid == 8220003 && this.mapid == 240040401) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[大海兽屠杀令]: " + chr.getName() + " 在大海兽 峡谷击杀了大海兽"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀大海兽");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀大海兽", (byte)2, 1);
        }
        else if (mobid == 3220001 && this.mapid == 260010201) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[大宇屠杀令]: " + chr.getName() + " 在仙人掌爸爸沙漠击杀了大宇"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀大宇");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀大宇", (byte)2, 1);
        }
        else if (mobid == 8220002 && this.mapid == 261030000) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[吉米拉屠杀令]: " + chr.getName() + " 在研究所地下秘密通道击杀了吉米拉"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀吉米拉");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀吉米拉", (byte)2, 1);
        }
        else if (mobid == 4220000 && this.mapid == 230020100) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[歇尔夫屠杀令]: " + chr.getName() + " 在海草之塔击杀了歇尔夫"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀歇尔夫");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀歇尔夫", (byte)2, 1);
        }
        else if (mobid == 6130101 && this.mapid == 100000005) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[蘑菇王屠杀令]: " + chr.getName() + " 在铁甲猪公园3击杀了蘑菇王"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀蘑菇王");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀蘑菇王", (byte)2, 1);
        }
        else if (mobid == 6300005 && this.mapid == 105070002) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[僵尸蘑菇王屠杀令]: " + chr.getName() + " 在蘑菇王之墓击杀了僵尸蘑菇王"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀僵尸蘑菇王");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀僵尸蘑菇王", (byte)2, 1);
        }
        else if (mobid == 8130100 && this.mapid == 105090900) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[蝙蝠怪屠杀令]: " + chr.getName() + " 在被诅咒的寺院击杀了蝙蝠怪"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀蝙蝠怪");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀蝙蝠怪", (byte)2, 1);
        }
        else if (mobid == 9400205 && this.mapid == 800010100) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[蓝蘑菇王屠杀令]: " + chr.getName() + " 在天皇殿堂击杀了蓝蘑菇王"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀蓝蘑菇王");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀蓝蘑菇王", (byte)2, 1);
        }
        else if (mobid == 9400120 && this.mapid == 801030000) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[老板屠杀令]: " + chr.getName() + " 在昭和内部街道3击杀了老板"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀老板");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀老板", (byte)2, 1);
        }
        else if (mobid == 8220001 && this.mapid == 211040101) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[驮狼雪人屠杀令]: " + chr.getName() + " 在雪人谷击杀了驮狼雪人"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀驮狼雪人");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀驮狼雪人", (byte)2, 1);
        }
        else if (mobid == 8180000 && this.mapid == 240020401) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) >= 1) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[火焰龙屠杀令]: " + chr.getName() + " 在喷火龙栖息地击杀了火焰龙"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀火焰龙");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀火焰龙", (byte)2, 1);
        }
        else if (mobid == 8180001 && this.mapid == 240020101) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) <= 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[天鹰屠杀令]: " + chr.getName() + " 在格瑞芬多森林击杀了天鹰"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀天鹰");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀天鹰", (byte)2, 1);
        }
        else if (mobid == 8220006 && this.mapid == 270030500) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) <= 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[雷卡屠杀令]: " + chr.getName() + " 在忘却之路5击杀了雷卡"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀雷卡");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀雷卡", (byte)2, 1);
        }
        else if (mobid == 8220005 && this.mapid == 270020500) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) <= 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[玄冰独角兽屠杀令]: " + chr.getName() + " 在后悔之路5击杀了玄冰独角兽"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀玄冰独角兽");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀玄冰独角兽", (byte)2, 1);
        }
        else if (mobid == 8220004 && this.mapid == 270010500) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) <= 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[多多屠杀令]: " + chr.getName() + " 在追忆之路5击杀了多多"));
            }
            chr.击杀野外BOSS特效();
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀多多");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀多多", (byte)2, 1);
        }
        else if (mobid == 8220004) {
            chr.setBossLog("蜈蚣");
            chr.击杀野外BOSS特效();
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "蜈蚣", (byte)2, 1);
        }
        else if (mobid == 8500002 && this.mapid == 220080001) {
            if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"屠令广播开关")) <= 0) {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(4, "[帕普拉图斯屠杀令]: " + chr.getName() + " 在时间塔的本源击杀了帕普拉图斯"));
            }
            chr.setBossLog("击杀高级怪物");
            chr.setBossLog("每日击杀帕普拉图斯");
            chr.setBossLog("活跃度");
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "每日挑战帕普拉图斯", (byte)2, 1);
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "个人击杀帕普拉图斯", (byte)2, 1);
        }
        else if (mobid == 9300003 && this.mapid == 103000804) {
            BossRankManager.getInstance().setLog(chr.getId(), chr.getName(), "废弃副本BOOS击杀次数", (byte)2, 1);
        }
        else if (mobid == 8830000 && this.mapid == 105100300) {
            final MapleMap map = chr.getMap();
            final boolean drop = false;
            final double range = Double.POSITIVE_INFINITY;
            final List<MapleMapObject> monsters = map.getMapObjectsInRange(chr.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
            for ( MapleMapObject monstermo : map.getMapObjectsInRange(chr.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
                final MapleMonster mob = (MapleMonster)monstermo;
                map.killMonster(mob, chr, drop, false, (byte)1);
            }
            for ( MapleMapObject monstermo : map.getMapObjectsInRange(chr.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
                final MapleMonster mob = (MapleMonster)monstermo;
                map.killMonster(mob, chr, drop, false, (byte)1);
            }
        }
        else if (mobid == 8800002 && this.mapid == 280030000) {
            final MapleMap map = chr.getMap();
            final boolean drop = false;
            final double range = Double.POSITIVE_INFINITY;
            final List<MapleMapObject> monsters = map.getMapObjectsInRange(chr.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER));
            for ( MapleMapObject monstermo : map.getMapObjectsInRange(chr.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
                final MapleMonster mob = (MapleMonster)monstermo;
                map.killMonster(mob, chr, drop, false, (byte)1);
            }
            for ( MapleMapObject monstermo : map.getMapObjectsInRange(chr.getPosition(), range, Arrays.asList(MapleMapObjectType.MONSTER))) {
                final MapleMonster mob = (MapleMonster)monstermo;
                map.killMonster(mob, chr, drop, false, (byte)1);
            }
        }
    }
    
    public final void killMonster(final MapleMonster monster, MapleCharacter chr, final boolean withDrops, final boolean second, byte animation, int lastSkill) {

        if (monster.getId() == 9900000 || monster.getId() == 9900001 || monster.getId() == 9900002) {
            this.haveStone = false;
            this.setStoneLevel(0);
        }
        if ((monster.getId() == 8810122 || monster.getId() == 8810018) && !second) {

            if (monster.getId() == 8810018 && monster.isMonitor() && monster.getMobDamageData() != null && monster.getMobDamageData().getMainMobId() == monster.getId()) {
                List<Integer> toSpawn = monster.getStats().getRevives();
                if (toSpawn.isEmpty()) {
                    try {
                        monster.getMobDamageData().calculate();
                    } catch (UnsupportedEncodingException e) {
                    }
                }
            }
            MapTimer.getInstance().schedule((Runnable)new Runnable() {
                @Override
                public void run() {
                    MapleMap.this.killMonster(monster, chr, true, true, (byte)1);
                    MapleMap.this.killAllMonsters(true);
                }
            }, 3000L);
            return;
        }
        if (monster.getId() == 8150000) {
            if (MapleParty.蝙蝠魔A部队 > 0) {
                if (this.mapid == 106010100 || this.mapid == 106010000 || this.mapid == 100000000) {
                    --MapleParty.蝙蝠魔A部队;
                }
            }
            else if (MapleParty.蝙蝠魔B部队 > 0) {
                if (this.mapid == 107000400 || this.mapid == 107000300 || this.mapid == 107000200 || this.mapid == 107000100 || this.mapid == 107000000 || this.mapid == 103000000) {
                    --MapleParty.蝙蝠魔B部队;
                }
            }
            else if (MapleParty.蝙蝠魔C部队 > 0) {
                if (this.mapid == 101010103 || this.mapid == 101010102 || this.mapid == 101010101 || this.mapid == 101010100 || this.mapid == 101010000 || this.mapid == 101000000) {
                    --MapleParty.蝙蝠魔C部队;
                }
            }
            else if (MapleParty.蝙蝠魔D部队 > 0 && (this.mapid == 106000300 || this.mapid == 106000200 || this.mapid == 106000100 || this.mapid == 106000000 || this.mapid == 102000000)) {
                --MapleParty.蝙蝠魔D部队;
            }
        }
        if (monster.getId() == 8820014) {
            this.killMonster(8820000);
        }
        else if (monster.getId() == 9300166) {
            animation = 2;
        }
        else if (this.getId() == 910320100) {}
        this.spawnedMonstersOnMap.decrementAndGet();
        this.removeMapObject((MapleMapObject)monster);
        int dropOwner = monster.killBy(chr, lastSkill);
        this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), (int)animation));
        if (monster.getBuffToGive() > -1) {
            int buffid = monster.getBuffToGive();
            final MapleStatEffect buff = MapleItemInformationProvider.getInstance().getItemEffect(buffid);
            this.charactersLock.readLock().lock();
            try {
                for ( MapleCharacter mc : this.characters) {
                    if (mc.isAlive()) {
                        if (buff!=null){
                            buff.applyTo(mc);
                        }
                        switch (monster.getId()) {
                            case 8810018:
                            case 8810122:
                            case 8820001: {
                                mc.getClient().sendPacket(MaplePacketCreator.showOwnBuffEffect(buffid, 11));
                                this.broadcastMessage(mc, MaplePacketCreator.showBuffeffect(mc.getId(), buffid, 11), false);
                                continue;
                            }
                        }
                    }
                }
            }
            finally {
                this.charactersLock.readLock().unlock();
            }
        }
        int mobid = monster.getId();
        SpeedRunType type = SpeedRunType.NULL;
        final MapleSquad sqd = this.getSquadByMap();
        this.地图杀怪(monster, chr);
        if (this.mapid == 702060000 && monster.getId() == 9600025) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("妖僧经验限制");
            }
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:少林妖僧被 " + 挑战者 + "击败了。"));
            chr.setBossLog("每日击杀妖僧");
            chr.setBossLog("活跃度");
        }
        else if (this.mapid == 541020800 && monster.getId() == 9420521) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("树精经验限制");
            }
            chr.setBossLog("每日击杀树精");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:克雷塞尔被 " + 挑战者 + "击败了。"));
        }
        else if (this.mapid == LtMS.ConfigValuesMap.get("世界BOSS地图") && monster.getId() == LtMS.ConfigValuesMap.get("世界BOSS")) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("击杀世界BOSS");
            }
            chr.setBossLog("每日击杀世界BOSS");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[活动]:世界BOSS被 " + 挑战者 + "击败了。"));
        }
        else if (Objects.nonNull(LtMS.ConfigValuesMap.get("塔BOSS"+monster.getId())) && Objects.nonNull(LtMS.ConfigValuesMap.get("塔BOSS地图"+this.mapid) ) && this.mapid == LtMS.ConfigValuesMap.get("塔BOSS地图"+this.mapid) && monster.getId() == LtMS.ConfigValuesMap.get("塔BOSS"+monster.getId())) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("击杀塔BOSS");
            }
            chr.setBossLog("每日击杀塔BOSS");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[宗师塔]:宗师塔BOSS被 " + 挑战者 + "击败了。"));
        }
        else if (mobid == 8810018 && this.mapid == 240060200) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("黑龙经验限制");
            }
            chr.setBossLog("每日击杀黑龙");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:黑暗龙王斯被 " + 挑战者 + "击败了。"));
            if (this.speedRunStart > 0L) {
                type = SpeedRunType.Horntail;
            }
            if (sqd != null) {
                this.doShrine(true);
            }
        }
        else if (mobid == 8500002 && this.mapid == 220080001) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("闹钟经验限制");
            }
            chr.setBossLog("每日击杀闹钟");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:帕普拉图斯被 " + 挑战者 + "击败了。"));
            if (this.speedRunStart > 0L) {
                type = SpeedRunType.Papulatus;
            }
        }
        else if ((mobid == 9420549 || mobid == 9420544) && this.mapid == 551030200) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
            }
            chr.setBossLog("每日击杀心疤熊");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:心疤狮王被 " + 挑战者 + "击败了。"));
            if (this.speedRunStart > 0L) {
                if (mobid == 9420549) {
                    type = SpeedRunType.Scarlion;
                }
                else {
                    type = SpeedRunType.Targa;
                }
            }
        }
        else if (mobid == 8820001 && this.mapid == 270050100) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 += player.getName();
            }
            chr.setBossLog("每日击杀品克缤");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:品克缤被" + 挑战者 + "击败了。"));
            if (this.speedRunStart > 0L) {
                type = SpeedRunType.Pink_Bean;
            }
            if (sqd != null) {
                this.doShrine(true);
            }
        }
        else if (mobid >= 8800003 && mobid <= 8800010) {
            boolean makeZakReal = true;
            final Collection<MapleMonster> monsters = this.getAllMonstersThreadsafe();
            for ( MapleMonster mons : monsters) {
                if (mons.getId() >= 8800003 && mons.getId() <= 8800010) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for ( MapleMapObject object : monsters) {
                    final MapleMonster mons2 = (MapleMonster)object;
                    if (mons2.getId() == 8800000) {
                        final Point pos = mons2.getPosition();
                        this.killAllMonsters(true);
                        this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800000), pos);
                        break;
                    }
                }
            }
        }
        else if (mobid == 8800002 && this.mapid == 280030000) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("扎昆经验限制");
            }
            chr.setBossLog("每日击杀扎昆");
            chr.setBossLog("活跃度");
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:扎昆被 " + 挑战者 + "击败了。"));
            if (this.speedRunStart > 0L) {
                type = SpeedRunType.Zakum;
            }
            if (sqd != null) {
                this.doShrine(true);
            }
            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characterz SET Point = 0 WHERE channel = 500")) {
                ps.executeUpdate();
            }
            catch (SQLException ex2) {}
        }
        if (mobid >= 8800103 && mobid <= 8800110) {
            boolean makeZakReal = true;
            final Collection<MapleMonster> monsters = this.getAllMonstersThreadsafe();
            for ( MapleMonster mons : monsters) {
                if (mons.getId() >= 8800103 && mons.getId() <= 8800110) {
                    makeZakReal = false;
                    break;
                }
            }
            if (makeZakReal) {
                for ( MapleMapObject object : monsters) {
                    final MapleMonster mons2 = (MapleMonster)object;
                    if (mons2.getId() == 8800100) {
                        final Point pos = mons2.getPosition();
                        this.killAllMonsters(true);
                        this.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8800100), pos);
                        break;
                    }
                }
            }
        }
        else if (mobid == 8800102 && this.mapid == 280030000) {
            String 挑战者 = "";
            for ( MapleCharacter player : this.getCharacters()) {
                挑战者 = 挑战者 + player.getName() + " ";
                player.setBossLog("扎昆经验限制");
            }
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[挑战]:进阶扎昆被 " + 挑战者 + "击败了。"));
            if (this.speedRunStart > 0L) {
                type = SpeedRunType.JjZakum;
            }
            if (sqd != null) {
                this.doShrine(true);
            }
            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characterz SET Point = 0 WHERE channel = 510")) {
                ps.executeUpdate();
            }
            catch (SQLException ex3) {}
        }
        if (mobid == 8820008) {
            for ( MapleMapObject mmo : this.getAllMonstersThreadsafe()) {
                final MapleMonster mons3 = (MapleMonster)mmo;
                if (mons3.getLinkOid() != monster.getObjectId()) {
                    this.killMonster(mons3, chr, false, false, animation);
                }
            }
        }
        else if (mobid >= 8820010 && mobid <= 8820014) {
            for ( MapleMapObject mmo : this.getAllMonstersThreadsafe()) {
                final MapleMonster mons3 = (MapleMonster)mmo;
                if (mons3.getId() != 8820000 && mons3.getObjectId() != monster.getObjectId() && mons3.getLinkOid() != monster.getObjectId()) {
                    this.killMonster(mons3, chr, false, false, animation);
                }
            }
        }
        if (withDrops) {
            MapleCharacter drop = null;
            if (dropOwner <= 0) {
                drop = chr;
            }
            else {
                drop = this.getCharacterById(dropOwner);
                if (drop == null) {
                    drop = chr;
                }
            }
            this.dropFromMonster(drop, monster);
        }
    }
    
    public List<MapleReactor> getAllReactor() {
        return this.getAllReactorsThreadsafe();
    }
    
    public List<MapleReactor> getAllReactorsThreadsafe() {
        final ArrayList<MapleReactor> ret = new ArrayList<MapleReactor>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                ret.add((MapleReactor)mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        return ret;
    }
    
    public List<MapleMapObject> getAllDoor() {
        return this.getAllDoorsThreadsafe();
    }
    
    public List<MapleMapObject> getAllDoorsThreadsafe() {
        final ArrayList<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.DOOR)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.DOOR)).values()) {
                ret.add(mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.DOOR)).readLock().unlock();
        }
        return ret;
    }
    
    public List<MapleMapObject> getAllMerchant() {
        return this.getAllHiredMerchantsThreadsafe();
    }
    
    public List<MapleMapObject> getAllHiredMerchantsThreadsafe() {
        final ArrayList<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.HIRED_MERCHANT)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.HIRED_MERCHANT)).values()) {
                ret.add(mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.HIRED_MERCHANT)).readLock().unlock();
        }
        return ret;
    }
    
    public List<MapleMonster> getAllMonster() {
        return this.getAllMonstersThreadsafe();
    }
    
    public List<MapleMonster> getAllMonstersThreadsafe() {
        final ArrayList<MapleMonster> ret = new ArrayList<MapleMonster>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MONSTER)).values()) {
                ret.add((MapleMonster)mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().unlock();
        }
        return ret;
    }
    
    public List<Integer> getAllUniqueMonsters() {
        final ArrayList<Integer> ret = new ArrayList<Integer>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MONSTER)).values()) {
                int theId = ((MapleMonster)mmo).getId();
                if (!ret.contains((Object)Integer.valueOf(theId))) {
                    ret.add(Integer.valueOf(theId));
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().unlock();
        }
        return ret;
    }
    
    public Collection<MapleCharacter> getNearestPvpChar(final Point attacker, final double maxRange, final double maxHeight, final Collection<MapleCharacter> chr) {
        final Collection<MapleCharacter> character = new LinkedList<MapleCharacter>();
        for ( MapleCharacter a : this.characters) {
            if (chr.contains((Object)a.getClient().getPlayer())) {
                final Point attackedPlayer = a.getPosition();
                final MaplePortal Port = a.getMap().findClosestSpawnpoint(a.getPosition());
                final Point nearestPort = Port.getPosition();
                final double safeDis = attackedPlayer.distance((Point2D)nearestPort);
                final double distanceX = attacker.distance(attackedPlayer.getX(), attackedPlayer.getY());
                if (MaplePvp.isLeft && attacker.x > attackedPlayer.x && distanceX < maxRange && distanceX > 2.0 && (double)attackedPlayer.y >= (double)attacker.y - maxHeight && (double)attackedPlayer.y <= (double)attacker.y + maxHeight && safeDis > 2.0) {
                    character.add(a);
                }
                if (!MaplePvp.isRight || attacker.x >= attackedPlayer.x || distanceX >= maxRange || distanceX <= 2.0 || (double)attackedPlayer.y < (double)attacker.y - maxHeight || (double)attackedPlayer.y > (double)attacker.y + maxHeight || safeDis <= 2.0) {
                    continue;
                }
                character.add(a);
            }
        }
        return character;
    }
    
    public void KillFk(final boolean animate) {
        final List<MapleMapObject> monsters = this.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for ( MapleMapObject monstermo : monsters) {
            final MapleMonster monster = (MapleMonster)monstermo;
            if (monster.getId() == 3230300 || monster.getId() == 3230301) {
                this.spawnedMonstersOnMap.decrementAndGet();
                monster.setHp(0L);
                this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), (int)(animate ? 1 : 0)));
                this.removeMapObject((MapleMapObject)monster);
                monster.killed();
            }
        }
        this.broadcastMessage(MaplePacketCreator.serverNotice(6, "由於受詛咒的岩石被摧殘，然而被詛咒的蝴蝶精消失了。"));
    }

    /**
     * 杀死地图所有怪物
     * @param animate
     */
    public void killAllMonsters(final boolean animate) {
        for ( MapleMapObject monstermo : this.getAllMonstersThreadsafe()) {
            final MapleMonster monster = (MapleMonster)monstermo;
            this.spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0L);
            this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), (int)(animate ? 1 : 0)));
            this.removeMapObject((MapleMapObject)monster);
            monster.killed();
        }
    }
    public final void killAllMonsters(boolean animate, boolean includeBoss) {
        Iterator var3 = this.getAllMonstersThreadsafe().iterator();

        while(true) {
            MapleMonster monster;
            do {
                do {
                    if (!var3.hasNext()) {
                        return;
                    }

                    MapleMapObject monstermo = (MapleMapObject)var3.next();
                    monster = (MapleMonster)monstermo;
                } while(monster == null);
            } while(monster.getStats().isBoss() && !includeBoss);

            this.spawnedMonstersOnMap.decrementAndGet();
            monster.setHp(0L);
            this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
            this.removeMapObject(monster);
            monster.killed();
        }
    }
    public void killMonster(int monsId) {
        for ( MapleMapObject mmo : this.getAllMonstersThreadsafe()) {
            if (((MapleMonster)mmo).getId() == monsId) {
                this.spawnedMonstersOnMap.decrementAndGet();
                this.removeMapObject(mmo);
                this.broadcastMessage(MobPacket.killMonster(mmo.getObjectId(), 1));
                break;
            }
        }
    }
    
    private String MapDebug_Log() {
        final StringBuilder sb = new StringBuilder("擊敗时间 : ");
        sb.append(FilePrinter.getLocalDateString());
        sb.append(" | 地图代码 : ").append(this.mapid);
        this.charactersLock.readLock().lock();
        try {
            sb.append(" 玩家 [").append(this.characters.size()).append("] | ");
            for ( MapleCharacter mc : this.characters) {
                sb.append(mc.getName()).append(", ");
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return sb.toString();
    }
    
    public void limitReactor(int rid, int num) {
        final List<MapleReactor> toDestroy = new ArrayList<MapleReactor>();
        final Map<Integer, Integer> contained = new LinkedHashMap<Integer, Integer>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor mr = (MapleReactor)obj;
                if (contained.containsKey((Object)Integer.valueOf(mr.getReactorId()))) {
                    if ((int)Integer.valueOf(contained.get((Object)Integer.valueOf(mr.getReactorId()))) >= num) {
                        toDestroy.add(mr);
                    }
                    else {
                        contained.put(Integer.valueOf(mr.getReactorId()), Integer.valueOf((int)Integer.valueOf(contained.get((Object)Integer.valueOf(mr.getReactorId()))) + 1));
                    }
                }
                else {
                    contained.put(Integer.valueOf(mr.getReactorId()), Integer.valueOf(1));
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for ( MapleReactor mr2 : toDestroy) {
            this.destroyReactor(mr2.getObjectId());
        }
    }
    
    public void destroyReactors(int first, int last) {
        final List<MapleReactor> toDestroy = new ArrayList<MapleReactor>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor mr = (MapleReactor)obj;
                if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                    toDestroy.add(mr);
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for ( MapleReactor mr2 : toDestroy) {
            this.destroyReactor(mr2.getObjectId());
        }
    }
    
    public void destroyReactor(int oid) {
        final MapleReactor reactor = this.getReactorByOid(oid);
        this.broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
        reactor.setAlive(false);
        this.removeMapObject((MapleMapObject)reactor);
        reactor.setTimerActive(false);
        if (reactor.getDelay() > 0) {
            try {
                MapTimer.getInstance().schedule((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        MapleMap.this.respawnReactor(reactor);
                    }
                }, (long)reactor.getDelay());
            }
            catch (RejectedExecutionException ex) {}
        }
    }
    
    public void reloadReactors() {
        final List<MapleReactor> toSpawn = new ArrayList<MapleReactor>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor reactor = (MapleReactor)obj;
                this.broadcastMessage(MaplePacketCreator.destroyReactor(reactor));
                reactor.setAlive(false);
                reactor.setTimerActive(false);
                toSpawn.add(reactor);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        for ( MapleReactor r : toSpawn) {
            this.removeMapObject((MapleMapObject)r);
            if (r.getReactorId() != 9980000 && r.getReactorId() != 9980001) {
                this.respawnReactor(r);
            }
        }
    }
    
    public void resetReactors() {
        this.setReactorState((byte)0);
    }
    
    public void setReactorState() {
        this.setReactorState((byte)1);
    }
    
    public void setReactorState(final byte state) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                ((MapleReactor)obj).forceHitReactor(state);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }
    
    public void shuffleReactors() {
        this.shuffleReactors(0, 9999999);
    }
    
    public void shuffleReactors(int first, int last) {
        final List<Point> points = new ArrayList<Point>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor mr = (MapleReactor)obj;
                if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                    points.add(mr.getPosition());
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
        Collections.shuffle(points);
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor mr = (MapleReactor)obj;
                if (mr.getReactorId() >= first && mr.getReactorId() <= last) {
                    mr.setPosition((Point)points.remove(points.size() - 1));
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }
    
    public void updateMonsterController(final MapleMonster monster) {
        if (!monster.isAlive()) {
            return;
        }
        if (monster.getController() != null) {
            if (monster.getController().getMap() == this) {
                return;
            }
            monster.getController().stopControllingMonster(monster);
        }
        int mincontrolled = -1;
        MapleCharacter newController = null;
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter chr : this.characters) {
                if (!chr.isHidden() && !chr.isClone() && (chr.getControlledSize() < mincontrolled || mincontrolled == -1)) {
                    mincontrolled = chr.getControlledSize();
                    newController = chr;
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        if (newController != null) {
            if (monster.isFirstAttack()) {
                newController.controlMonster(monster, true);
                monster.setControllerHasAggro(true);
                monster.setControllerKnowsAboutAggro(true);
            }
            else {
                newController.controlMonster(monster, false);
            }
        }
    }
    
    public MapleMapObject getMapObject(int oid, final MapleMapObjectType type) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().lock();
        try {
            return (MapleMapObject)((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)type)).get((Object)Integer.valueOf(oid));
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().unlock();
        }
    }
    
    public boolean containsNPC(int npcid) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).readLock().lock();
        try {
            for ( MapleNPC n : (java.util.Collection<MapleNPC>)(java.util.Collection)((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.NPC)).values()) {
                if (n.getId() == npcid) {
                    return true;
                }
            }
            return false;
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).readLock().unlock();
        }
    }
    
    public MapleNPC getNPCById(int id) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).readLock().lock();
        try {
            for ( MapleNPC n : (java.util.Collection<MapleNPC>)(java.util.Collection)((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.NPC)).values()) {
                if (n.getId() == id) {
                    return n;
                }
            }
            return null;
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).readLock().unlock();
        }
    }
    
    public MapleMonster getMonsterById(int id) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            MapleMonster ret = null;
            for ( MapleMonster n : (java.util.Collection<MapleMonster>)(java.util.Collection)((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MONSTER)).values()) {
                if (n.getId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }
    
    public int countMonsterById(int id) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            int ret = 0;
            for ( MapleMonster n : (java.util.Collection<MapleMonster>)(java.util.Collection)((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MONSTER)).values()) {
                if (n.getId() == id) {
                    ++ret;
                }
            }
            return ret;
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }
    
    public MapleReactor getReactorById(int id) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            MapleReactor ret = null;
            for ( MapleReactor n : (java.util.Collection<MapleReactor>)(java.util.Collection)(((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values())) {
                if (n.getReactorId() == id) {
                    ret = n;
                    break;
                }
            }
            return ret;
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }
    
    public MapleMonster getMonsterByOid(int oid) {
        final MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.MONSTER);
        if (mmo == null) {
            return null;
        }
        return (MapleMonster)mmo;
    }
    
    public MapleNPC getNPCByOid(int oid) {
        final MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.NPC);
        if (mmo == null) {
            return null;
        }
        return (MapleNPC)mmo;
    }
    
    public MapleReactor getReactorByOid(int oid) {
        final MapleMapObject mmo = this.getMapObject(oid, MapleMapObjectType.REACTOR);
        if (mmo == null) {
            return null;
        }
        return (MapleReactor)mmo;
    }
    
    public MapleReactor getReactorByName(final String name) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject obj : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor mr = (MapleReactor)obj;
                if (mr.getName().equalsIgnoreCase(name)) {
                    return mr;
                }
            }
            return null;
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }
    
    public void spawnNpc(int id, final Point pos) {
        final MapleNPC npc = MapleLifeFactory.getNPC(id);
        npc.setPosition(pos);
        npc.setCy(pos.y);
        npc.setRx0(pos.x + 50);
        npc.setRx1(pos.x - 50);
        npc.setFh(this.getFootholds().findBelow(pos).getId());
        npc.setCustom(true);
        this.addMapObject((MapleMapObject)npc);
        this.broadcastMessage(MaplePacketCreator.spawnNPC(npc, true));
    }
    
    public void removeNpc_(int npcid) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).writeLock().lock();
        try {
            final Iterator<MapleMapObject> itr = ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.NPC)).values().iterator();
            while (itr.hasNext()) {
                final MapleNPC npc = (MapleNPC)itr.next();
                if (npcid == -1 || npc.getId() == npcid) {
                    this.broadcastMessage(MaplePacketCreator.removeNPCController(npc.getObjectId()));
                    this.broadcastMessage(MaplePacketCreator.removeNPC(npc.getObjectId()));
                    itr.remove();
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).writeLock().unlock();
        }
    }
    
    public void removeNpc(int npcid) {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).writeLock().lock();
        try {
            final Iterator<MapleMapObject> itr = ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.NPC)).values().iterator();
            while (itr.hasNext()) {
                final MapleNPC npc = (MapleNPC)itr.next();
                if (npc.isCustom() && (npcid == -1 || npc.getId() == npcid)) {
                    this.broadcastMessage(MaplePacketCreator.removeNPCController(npc.getObjectId()));
                    this.broadcastMessage(MaplePacketCreator.removeNPC(npc.getObjectId()));
                    itr.remove();
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).writeLock().unlock();
        }
    }
    
    public void spawnMonster_sSack(final MapleMonster mob, final Point pos, int spawnType) {
        final Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
        mob.setPosition(spos);
        this.spawnMonster(mob, spawnType);
    }
   public void spawnMonster_sSack(final MapleMonster mob, final Point pos, int spawnType, final long hp) {
     final Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
     mob.setPosition(spos);
     mob.setOverrideStats(new OverrideMonsterStats(hp, mob.getMobMaxMp(), mob.getMobExp()));
     this.spawnMonster(mob, spawnType);
   }

    //刷怪物
    public void spawnMonsterOnGroundBelow(final MapleMonster mob, final Point pos) {
        this.spawnMonster_sSack(mob, pos, -2);
    }
    public void spawnMonsterOnGroundBelow(MapleMonster mob, Point pos, long hp) 
    {
    this.spawnMonster_sSack(mob, pos, -2, hp);
    }
    public int spawnMonsterWithEffectBelow(final MapleMonster mob, final Point pos, int effect) {
        final Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
        return this.spawnMonsterWithEffect(mob, effect, spos);
    }
    
    public void spawnZakum( int x,  int y) {
         Point pos = new Point(x, y);
         MapleMonster mainb = MapleLifeFactory.getMonster(8800000);
         Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
        mainb.setPosition(spos);
        mainb.setFake(true);
        this.spawnFakeMonster(mainb);
         int[] array;
         int[] zakpart = array = new int[] { 8800003, 8800004, 8800005, 8800006, 8800007, 8800008, 8800009, 8800010 };
        for ( int i : array) {
            final MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);
            this.spawnMonster(part, -2);
        }
        if (this.squadSchedule != null) {
            this.cancelSquadSchedule();
            this.broadcastMessage(MaplePacketCreator.stopClock());
        }
    }
    
    public  void spawnChaosZakum(int x, int y) {
         Point pos = new Point(x, y);
         MapleMonster mainb = MapleLifeFactory.getMonster(8800100);
         Point spos = this.calcPointBelow(new Point(pos.x, pos.y - 1));
         int[] array;
         int[] zakpart = array = new int[] { 8800103, 8800104, 8800105, 8800106, 8800107, 8800108, 8800109, 8800110 };
        for ( int i : array) {
             MapleMonster part = MapleLifeFactory.getMonster(i);
            part.setPosition(spos);
            this.spawnMonster(part, -2);
        }
        mainb.setPosition(spos);
        mainb.setFake(true);
        this.spawnFakeMonster(mainb);
        if (this.squadSchedule != null) {
            this.cancelSquadSchedule();
        }
    }
    
    public List<MapleMist> getAllMistsThreadsafe() {
         ArrayList<MapleMist> ret = new ArrayList<MapleMist>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MIST)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MIST)).values()) {
                ret.add((MapleMist)mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MIST)).readLock().unlock();
        }
        return ret;
    }
    
    public void spawnFakeMonsterOnGroundBelow( MapleMonster mob,  Point pos) {
         Point calcPointBelow;
         Point spos = calcPointBelow = this.calcPointBelow(new Point(pos.x, pos.y - 1));
        --calcPointBelow.y;
        mob.setPosition(spos);
        this.spawnFakeMonster(mob);
    }
    
    private void checkRemoveAfter( MapleMonster monster) {
         int ra = monster.getStats().getRemoveAfter();
        if (ra > 0) {
            MapTimer.getInstance().schedule((Runnable)new Runnable() {
                @Override
                public void run() {
                    if (monster != null && monster == MapleMap.this.getMapObject(monster.getObjectId(), monster.getType())) {
                        MapleMap.this.killMonster(monster);
                    }
                }
            }, (long)(ra * 1000));
        }
    }
    
    public  void spawnRevives( MapleMonster monster,  int oid) {
        monster.setMap(this);
        this.checkRemoveAfter(monster);
        monster.setLinkOid(oid);
        this.spawnAndAddRangedMapObject((MapleMapObject)monster, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                c.sendPacket(MobPacket.spawnMonster(monster, -2, 0, oid));
            }
        }, null);
        this.updateMonsterController(monster);
        this.spawnedMonstersOnMap.incrementAndGet();
    }
    
    public void spawnMonster( MapleMonster monster,  int spawnType) {
        monster.setMap(this);
        this.checkRemoveAfter(monster);
        if (monster.getId() == 9300166) {
            MapTimer.getInstance().schedule((Runnable)new Runnable() {
                @Override
                public void run() {
                    MapleMap.this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 2));
                }
            }, (long)new Random().nextInt(5000));
        }
        this.spawnAndAddRangedMapObject((MapleMapObject)monster, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                c.sendPacket(MobPacket.spawnMonster(monster, spawnType, 0, 0));
            }
        }, null);
        this.updateMonsterController(monster);
        this.spawnedMonstersOnMap.incrementAndGet();
    }
    
    public int spawnMonsterWithEffect( MapleMonster monster,  int effect,  Point pos) {
        try {
            monster.setMap(this);
            monster.setPosition(pos);
            this.spawnAndAddRangedMapObject((MapleMapObject)monster, (DelayedPacketCreation)new DelayedPacketCreation() {
                @Override
                public void sendPackets( MapleClient c) {
                    c.sendPacket(MobPacket.spawnMonster(monster, -2, effect, 0));
                }
            }, null);
            this.updateMonsterController(monster);
            this.spawnedMonstersOnMap.incrementAndGet();
            return monster.getObjectId();
        }
        catch (Exception e) {
            return -1;
        }
    }
    
    public void spawnFakeMonster( MapleMonster monster) {
        monster.setMap(this);
        monster.setFake(true);
        this.spawnAndAddRangedMapObject((MapleMapObject)monster, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                c.sendPacket(MobPacket.spawnMonster(monster, -2, 252, 0));
            }
        }, null);
        this.updateMonsterController(monster);
        this.spawnedMonstersOnMap.incrementAndGet();
    }
    
    public void spawnReactor( MapleReactor reactor) {
        reactor.setMap(this);
        this.spawnAndAddRangedMapObject((MapleMapObject)reactor, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                c.sendPacket(MaplePacketCreator.spawnReactor(reactor));
            }
        }, null);
    }
    
    private void respawnReactor( MapleReactor reactor) {
        reactor.setState((byte)0);
        reactor.setAlive(true);
        this.spawnReactor(reactor);
    }
    
    public void spawnDoor( MapleDoor door) {
        this.spawnAndAddRangedMapObject((MapleMapObject)door, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                door.sendSpawnData(c);
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        }, (SpawnCondition)new SpawnCondition() {
            @Override
            public boolean canSpawn( MapleCharacter chr) {
                return door.getTarget().getId() == chr.getMapId() || door.getOwnerId() == chr.getId() || (door.getOwner() != null && door.getOwner().getParty() != null && door.getOwner().getParty().getMemberById(chr.getId()) != null);
            }
        });
    }
    
    public void spawnSummon( MapleSummon summon) {
        summon.updateMap(this);
        this.spawnAndAddRangedMapObject((MapleMapObject)summon, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                if (c != null && c.getPlayer() != null && summon != null && (!summon.isChangedMap() || summon.getOwnerId() == c.getPlayer().getId())) {
                    c.sendPacket(MaplePacketCreator.spawnSummon(summon, true));
                }
            }
        }, null);
    }
    
    public  void spawnMist( MapleMist mist,  int duration,  boolean fake) {

    //生成并添加范围映射对象
        this.spawnAndAddRangedMapObject((MapleMapObject)mist, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                //发送召唤数据
                mist.sendSpawnData(c);
            }
        }, null);
        //映射计时器
         MapTimer tMan = MapTimer.getInstance();
        ScheduledFuture<?> poisonSchedule = null;
        switch (mist.isPoisonMist()) {
            case 1: {
                final MapleCharacter owner = this.getCharacterById(mist.getOwnerId());
                poisonSchedule = tMan.register((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        for ( MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                           if (mist.makeChanceResult() && !((MapleMonster)mo).isBuffed(MonsterStatus.POISON)) {//
                               ((MapleMonster)mo).applyStatus(owner, new MonsterStatusEffect(MonsterStatus.POISON, Integer.valueOf(1), mist.getSourceSkill().getId(), null, false), true, (long) duration, ((MapleMonster)mo).getStats().isBoss(), mist.getSource());
                            }
                        }
                    }
                }, 2000L, 2500L);
                break;
            }
            case 2: {
                poisonSchedule = tMan.register((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        for ( MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.PLAYER))) {
                            if (mist.makeChanceResult()) {
                                MapleCharacter chr = (MapleCharacter)mo;
                                chr.addMP((int)((double)mist.getSource().getX() * ((double)chr.getStat().getMaxMp() / 100.0)));
                            }
                        }
                    }
                }, 2000L,2500L);
                break;
            }

            default: {
                poisonSchedule = null;
                break;
            }
        }
        try {
        	 ScheduledFuture<?> poisonSchedule2 = poisonSchedule;
            tMan.schedule((Runnable)new Runnable() {
                @Override
                public void run() {
                    //移除毒雾
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeMist(mist.getObjectId(), false));
                    //移除地图对象
                    MapleMap.this.removeMapObject((MapleMapObject)mist);
                    if (poisonSchedule2 != null) {
                        poisonSchedule2.cancel(false);
                    }
                }
            }, (long)duration);//存在时间
        }
        catch (RejectedExecutionException ex) {}
    }

    /**
     * 超级技能释放
     * @param applyfrom
     * @param facingLeft
     * @param effect
     * @param bounds
     */
    public void spawnSkill( MapleCharacter applyfrom,  boolean facingLeft,  MapleStatEffect effect, Rectangle bounds, SuperSkills superSkills) {
//        Point position = applyfrom.getPosition();
//        AtomicReference<Integer> x = new  AtomicReference<>(0);
//        AtomicReference<Integer> y = new  AtomicReference<>(0);
//        AtomicReference<Integer> index1 = new  AtomicReference<>(0);
//        if (facingLeft){
//            x.set(position.x-LtMS.ConfigValuesMap.get("超级技能补偿坐标X"));
//            y.set(position.y+LtMS.ConfigValuesMap.get("超级技能补偿坐标Y"));
//        }else{
//            x.set(position.x+LtMS.ConfigValuesMap.get("超级技能补偿坐标X"));
//            y.set(position.y+LtMS.ConfigValuesMap.get("超级技能补偿坐标Y"));
//        }
//        List<MapleMist> list = new ArrayList<>();
//
//        for (int i = 0; i < superSkills.getStackingDistance(); i++) {
//            if (facingLeft){
//                x.set(x.get()-superSkills.getSkillCount());
//                bounds = MapleStatEffect.calculateBoundingBox(new Point(x.get(),y.get()), facingLeft, new Point(superSkills.getSkillLX(),superSkills.getSkillLY()), new Point(superSkills.getSkillRX(),superSkills.getSkillRY()),superSkills.getRange());
//            }else{
//                x.set(x.get()+superSkills.getSkillCount());
//                bounds = MapleStatEffect.calculateBoundingBox(new Point(x.get(),y.get()), facingLeft,new Point(superSkills.getSkillRX(),superSkills.getSkillRY()) , new Point(superSkills.getSkillLX(),superSkills.getSkillLY()),superSkills.getRange());
//            }
//            //设置对象
//            MapleMist mist = new MapleMist(bounds, applyfrom, effect);
//            list.add(mist);
//        }
//            //生成并添加范围映射对象
//
//             MapTimer.getInstance().register((Runnable)new Runnable() {
//                @Override
//                public void run() {
//
//                    if (index1.get()>=superSkills.getStackingDistance()){
//                        for (MapleMist mapleMist : list) {
//                            //移除技能
//                            MapleMap.this.broadcastMessage(MaplePacketCreator.removeMist(mapleMist.getObjectId(), false));
//                            //移除地图对象
//                            MapleMap.this.removeMapObject((MapleMapObject) mapleMist);
//                        }
//                        return;
//                    }else{
//                        spawnAndAddRangedMapObject((MapleMapObject) list.get(index1.get()), (DelayedPacketCreation)new DelayedPacketCreation() {
//                            @Override
//                            public void sendPackets(final MapleClient c) {
//                                //发送召唤数据
//                                list.get(index1.get()).sendSpawnData(c);
//                            }
//                        }, null);
//                    }
//                    int index = 1;
//                    for ( MapleMapObject mo : applyfrom.getClient().getPlayer().getMap().getMapObjectsInRect(list.get(index1.get()).getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
//                        if (Objects.nonNull(applyfrom.getMap()) && index1.get()>=LtMS.ConfigValuesMap.get("删除起点")){
//                            if(index >= 10 ) {break;}
//                            long l = (long) (superSkills.getSkill_leve() * applyfrom.getStat().damage * superSkills.getHarm() + 1);
//                            l = l>Integer.MAX_VALUE ? Integer.MAX_VALUE : l;
//                            applyfrom.getMap().broadcastMessage(MobPacket.damageMonster(((MapleMonster)mo).getObjectId(),l));
//                            ((MapleMonster)mo).damage(applyfrom, l, true, list.get(index1.get()).getSourceSkill().getId());
//                            // System.out.println("产生伤害");
//                        }
//                        index++;
//                    }
//                    if (index1.get()>=LtMS.ConfigValuesMap.get("删除起点")){
//                      //移除技能
//                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeMist(list.get(index1.get()-LtMS.ConfigValuesMap.get("删除起点")).getObjectId(), false));
//                    //移除地图对象
//                    MapleMap.this.removeMapObject((MapleMapObject) list.get(index1.get()-LtMS.ConfigValuesMap.get("删除起点")));
//                    }
//
//                    index1.set(index1.get()+1);
//                }
//            }, superSkills.getInjuryinterval());
//生成并添加范围映射对象
            //设置对象
            MapleMist mist = new MapleMist(bounds, applyfrom, effect);
        this.spawnAndAddRangedMapObject((MapleMapObject)mist, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets( MapleClient c) {
                //发送召唤数据
                mist.sendSpawnData(c);
            }
        }, null);
        //映射计时器
        MapTimer tMan = MapTimer.getInstance();
        tzjc t = new tzjc();
        double totDamageToOneMonster = superSkills.getHarm()/superSkills.getSkillCount();
        totDamageToOneMonster = totDamageToOneMonster >1 ? 1.0 : totDamageToOneMonster;
       // final MapleCharacter owner = this.getCharacterById(mist.getOwnerId());
        double finalTotDamageToOneMonster = totDamageToOneMonster;
        ScheduledFuture<?> poisonSchedule = tMan.register((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        int index = 1;
                        for ( MapleMapObject mo : MapleMap.this.getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                            if(index >= 10 ) {break;}
                            if ( Objects.nonNull(applyfrom.getMap()) &&  ((MapleMonster)mo).getId()!=9300061){
                                long l = (long) ( applyfrom.getStat().damage * superSkills.getHarm() + 1);
                                l = l>Integer.MAX_VALUE ? Integer.MAX_VALUE : l;
                                applyfrom.getMap().broadcastMessage(MobPacket.damageMonster(((MapleMonster)mo).getObjectId(),l));
                                //((MapleMonster)mo).damage(applyfrom, l, true, mist.getSourceSkill().getId());
                                //((MapleMonster)mo).applyStatus(applyfrom, new MonsterStatusEffect(MonsterStatus.NEUTRALISE, 1, effect.getSourceId(), null, false), false, (effect.getX() * 2L), true, effect);
                                ((MapleMonster)mo).damage(applyfrom, t.damage(applyfrom, (long)(l*finalTotDamageToOneMonster),0.0 ), true, mist.getSourceSkill().getId());
                            }
                            index++;
                        }
                    }
                }, superSkills.getInjuryinterval(),superSkills.getInjurydelaytime());

        try {
            tMan.schedule((Runnable)new Runnable() {
                @Override
                public void run() {
                    //移除技能
                    MapleMap.this.broadcastMessage(MaplePacketCreator.removeMist(mist.getObjectId(), false));
                    //移除地图对象
                    MapleMap.this.removeMapObject((MapleMapObject)mist);
                    if (poisonSchedule != null) {
                        poisonSchedule.cancel(false);
                    }
                }
            }, superSkills.getDamagedestructiontime());//存在时间
        }
        catch (RejectedExecutionException ex) {}
    }

    /**
     * 光环伤害释放
     * @param applyfrom
     * @param facingLeft
     * @param effect
     * @param bounds
     */
    public void spawnCoronaSkill( MapleCharacter applyfrom,  boolean facingLeft, MapleStatEffect effect, Rectangle bounds,FieldSkills fieldSkills) {
        ScheduledFuture<?> poisonSchedule = null;
        try {
        if (applyfrom.getCorona()>LtMS.ConfigValuesMap.get("光环上限") && applyfrom.getCoronaMap() == applyfrom.getMapId()){
            return;
        }
        applyfrom.setCoronaJa(1);
        applyfrom.setCoronaMap(applyfrom.getMapId());
            //设置对象
         MapleMist[] mist = {new MapleMist(bounds, applyfrom, effect)};
            //生成并添加范围映射对象
            MapTimer tMan = MapTimer.getInstance();

//            spawnAndAddRangedMapObject((MapleMapObject) mist[0], (DelayedPacketCreation)new DelayedPacketCreation() {
//                @Override
//                public void sendPackets(final MapleClient c) {
//                    //发送召唤数据
//                    mist[0].sendSpawnData(c);
//                }
//            }, null);
        tzjc t = new tzjc();
       final double totDamageToOneMonster = fieldSkills.getHarm()/fieldSkills.getInjurydelaytime();
            poisonSchedule = tMan.register((Runnable)new Runnable() {
                @Override
                public void run() {
                    try {
                        int index = 1;
                        long l = applyfrom.getJob() >= 200 && applyfrom.getJob()<=232 ? (long) ((applyfrom.getStat().damage + applyfrom.getStat().localint_ * 25L)  + 1) :  (long) (applyfrom.getStat().damage);

                        for ( MapleMapObject mo : applyfrom.getClient().getPlayer().getMap().getMapObjectsInRect(mist[0].getBox(), Collections.singletonList(MapleMapObjectType.MONSTER))) {
                            if(index > fieldSkills.getDjCount() ) {break;}
                            if ( Objects.nonNull(applyfrom.getMap()) &&  ((MapleMonster)mo).getId()!=9300061){
                                l = l>Integer.MAX_VALUE ? Integer.MAX_VALUE : l;
                                long damage = t.damage(applyfrom, (long)(l*totDamageToOneMonster), 0.0);
                                if(LtMS.ConfigValuesMap.get("世界BOSS")==((MapleMonster)mo).getId()){
                                    damage = 999;
                                }
                                if ((LtMS.ConfigValuesMap.get("领域伤害屏蔽")) > 0) {
                                    applyfrom.getMap().broadcastMessage(MobPacket.damageMonster(((MapleMonster)mo).getObjectId(),damage));
                                }
                                ((MapleMonster)mo).damage(applyfrom, damage*fieldSkills.getDjSection(), true, mist[0].getSourceSkill().getId());
                                if ((LtMS.ConfigValuesMap.get("领域伤害气泡显示")) > 0) {
                                    applyfrom.showInstruction("【领域伤害 → " + NumberUtils.amountConversion(new BigDecimal(damage*fieldSkills.getDjSection())) + "#k】", 240, 10);
                                }
                                if ((LtMS.ConfigValuesMap.get("领域伤害黄字喇叭显示")) > 0) {
                                    applyfrom.dropMessage(-1, "【领域伤害 →" + NumberUtils.amountConversion(new BigDecimal(damage*fieldSkills.getDjSection())) + "】");
                                }
                            }
                            index++;
                        }
                        Rectangle bounds = MapleStatEffect.calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft(), new Point(fieldSkills.getSkillLX(),fieldSkills.getSkillLY()), new Point(fieldSkills.getSkillRX(),fieldSkills.getSkillRY()),fieldSkills.getRange());
                        mist[0] = new MapleMist(bounds, applyfrom, effect);
                    } catch (Exception e) {}
                }
            }, fieldSkills.getInjuryinterval());

            //消除技能

                 ScheduledFuture<?> poisonSchedule2 = poisonSchedule;
                tMan.schedule((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        if(applyfrom.getCorona()>0){
                            applyfrom.setCoronaJan(1);
                        }
                        if(applyfrom.getCorona()<0){
                            applyfrom.setCorona(0);
                        }
                        if (poisonSchedule2 != null) {
                            poisonSchedule2.cancel(false);
                        }
                    }
                }, (long)fieldSkills.getDamagedestructiontime());//存在时间

            }catch (RejectedExecutionException ex) {
                ex.printStackTrace();
            if (poisonSchedule != null) {
                poisonSchedule.cancel(false);
            }
            }

    }
    public void cancelMorphs(final MapleStatEffect effect,final MapleCharacter applyfrom) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)applyfrom.getEffects().values());
        for ( MapleBuffStatValueHolder mbsvh : allBuffs) {
           if(mbsvh.effect.getSourceId() == effect.getSourceId()) {
                    applyfrom.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    return;
                }
            }
    }
public void sendSkill(MapleCharacter chr,final MapleStatEffect effect){
    int skillId = effect.getSourceId();
    final byte level = effect.getLevel();
    final byte flags = 1;
    final byte speed = 100;
    final byte unk = 1;
    final ISkill skill = SkillFactory.getSkill(skillId);
    if (chr == null) {
        return;
    }
    int skilllevel_serv = chr.getSkillLevel(skill);
    if (skilllevel_serv > 0 && skilllevel_serv == level && skill.isChargeSkill()) {
        chr.setKeyDownSkill_Time(System.currentTimeMillis());
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.skillEffect(chr, skillId, level, flags, speed, unk), false);
    }
}

    public void disappearingItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos) {
        final Point droppos = this.calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte)1, false);
        this.broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte)3), drop.getPosition());
    }
    
    public void spawnMesoDrop(int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {
        final Point droppos = this.calcDropPos(position, position);
        final MapleMapItem mdrop = new MapleMapItem(meso, droppos, dropper, owner, droptype, playerDrop);
        this.spawnAndAddRangedMapObject((MapleMapObject)mdrop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), droppos, (byte)1));
            }
        }, null);
        if (!this.everlast) {
            mdrop.registerExpire(LtMS.ConfigValuesMap.get("物品掉落持续时间")*1000L);
            if (droptype == 0 || droptype == 1) {
                mdrop.registerFFA(30000L);
            }
        }
    }
    
    public void spawnMobMesoDrop(int meso, final Point position, final MapleMapObject dropper, final MapleCharacter owner, final boolean playerDrop, final byte droptype) {

    final MapleMapItem mdrop = new MapleMapItem(meso, position, dropper,owner, droptype, playerDrop);
        this.spawnAndAddRangedMapObject((MapleMapObject)mdrop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, dropper.getPosition(), position, (byte)1));
            }
        }, null);
        if (LtMS.ConfigValuesMap.get("特殊宠物吸物开关")>=1 && LtMS.ConfigValuesMap.get("特殊宠物吸金开关")>=1 && owner.getEventInstance() == null) {
            宠物吸物吸金(owner, mdrop);
        }
        mdrop.registerExpire(LtMS.ConfigValuesMap.get("物品掉落持续时间")*1000L);
        if (droptype == 0 || droptype == 1) {
            mdrop.registerFFA(30000L);
        }
    }

    //独立掉落
    public final void spawnMobDrop(IItem idrop, final Point dropPos, final MapleMonster mob, MapleCharacter chr, byte droptype, final short questid, final int onlySeeId) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, mob, chr, droptype, false, questid);
        if (onlySeeId >= 0) {
            mdrop.setOnlySeeChrId(onlySeeId);
        }

        List<MapleMapItem> items = this.getAllItemsThreadsafe();
        int mountToClear = 0;
        int maxDrops = (Integer)LtMS.ConfigValuesMap.get("地图掉物数量上限");
        if (items.size() > maxDrops) {
            mountToClear = items.size() - maxDrops;
        }

        if (mountToClear > 0) {
            for(int i = 0; i < mountToClear; ++i) {
                ((MapleMapItem)items.get(i)).expire(this);
            }
        }
        this.spawnAndAddRangedMapObject(mdrop, new DelayedPacketCreation() {
            public void sendPackets(MapleClient c) {
                if (questid <= 0 || c.getPlayer().getQuestStatus(questid) == 1) {
                    if (mdrop.getItem() == null) {
                        if (mdrop.getMeso() > 0) {
                            if (!c.getPlayer().isDropItemFilter(0)) {
                                if (onlySeeId >= 0) {
                                    if (c.getPlayer().getId() == onlySeeId) {
                                        c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));
                                    }
                                } else {
                                    c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));
                                }
                            }
                        } else if (onlySeeId >= 0) {
                            if (c.getPlayer().getId() == onlySeeId) {
                                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));
                            }
                        } else {
                            c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));
                        }
                    } else if (c != null && c.getPlayer() != null && !c.getPlayer().isDropItemFilter(mdrop.getItem().getItemId())) {
                        if (onlySeeId >= 0) {
                            if (c.getPlayer().getId() == onlySeeId) {
                                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));
                            }
                        } else {
                            c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));
                        }
                    }
                }

            }
        }, (SpawnCondition)null);

        if (LtMS.ConfigValuesMap.get("特殊宠物吸物开关")>=1 && LtMS.ConfigValuesMap.get("特殊宠物吸金开关")>=1 && chr.getEventInstance() == null) {
            宠物吸物吸金(chr, mdrop);
        }
        mdrop.registerExpire((long)(1000 * (Integer)LtMS.ConfigValuesMap.get("地图掉物保留秒数")));
        if (droptype == 0 || droptype == 1) {
            mdrop.registerFFA(30000L);
        }

        this.activateItemReactors(mdrop, chr.getClient());
    }
    public void spawnMobDrop(final IItem idrop, final Point dropPos, final MapleMonster mob, MapleCharacter chr, final byte droptype, final short questid) {
        final MapleMapItem mdrop = new MapleMapItem(idrop, dropPos, (MapleMapObject)mob, chr, droptype, false, (int)questid);
        this.spawnAndAddRangedMapObject((MapleMapObject)mdrop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                if (questid <= 0 || c.getPlayer().getQuestStatus((int)questid) == 1) {
                    c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, mob.getPosition(), dropPos, (byte)1));//0为开启,1为关闭
                }
            }
        }, null);
        if (Objects.isNull(LtMS.ConfigValuesMap.get("吸物排除"+mob.getId())) && LtMS.ConfigValuesMap.get("特殊宠物吸取开关")>=1 && chr.getEventInstance() == null){
            宠物吸物(chr, mdrop);
        }
        mdrop.registerExpire(LtMS.ConfigValuesMap.get("物品掉落持续时间")*1000L);
        if (droptype == 0 || droptype == 1) {
            mdrop.registerFFA(30000L);
        }
        this.activateItemReactors(mdrop, chr.getClient());
    }

    private static void 宠物吸物吸金(MapleCharacter owner, MapleMapItem mdrop) {
        boolean 吸物状态 = false;
        int 宠物数据库ID = 0;
        if (owner.getId() == mdrop.character_ownerid) {
            //判断是否带有宠物,并是否持有宠吸凭证
            for ( MaplePet pet : owner.getSummonedPets()) {
                if (owner.getItemQuantity(LtMS.ConfigValuesMap.get("宠吸道具"), false) > 0 && pet.getPetItemId() != 0) {
                    宠物数据库ID = pet.getUniqueId();
                    吸物状态 = true;
                    break;
                }
            }
            if (吸物状态 && mdrop.getMeso() > 0) {
                if (owner.getParty() != null && mdrop.getOwner() == owner.getId()) {
                    final List<MapleCharacter> toGive = new LinkedList<MapleCharacter>();
                    int splitMeso = mdrop.getMeso() * 40 / 100;
                    for ( MaplePartyCharacter z : owner.getParty().getMembers()) {
                        final MapleCharacter m = owner.getMap().getCharacterById(z.getId());
                        if (m != null && m.getId() != owner.getId()) {
                            toGive.add(m);
                        }
                    }
                    for ( MapleCharacter i : toGive) {
                        i.gainMeso(splitMeso / toGive.size() + (i.getStat().hasPartyBonus ? ((int)((double) mdrop.getMeso() / 20.0)) : 0), true);
                    }
                    owner.gainMeso(mdrop.getMeso() - splitMeso, true);
                }
                else {
                    owner.gainMeso(mdrop.getMeso(), true);
                }
                //final byte petz = owner.getPetIndex(宠物数据库ID);
                InventoryHandler.removeItemPet(owner, mdrop, 宠物数据库ID);
            }
        }
    }

    private void 宠物吸物(MapleCharacter chr, MapleMapItem mdrop) {
        boolean 吸物状态 = false;
        int 宠物数据库ID = 0;
        if (chr.getId() == mdrop.character_ownerid) {
            for ( MaplePet pet : chr.getSummonedPets()) {
                if (chr.getItemQuantity(MapleMap.持有物道具, false) > 0 && pet.getPetItemId() != 0) {
                    宠物数据库ID = pet.getUniqueId();
                    吸物状态 = true;
                    break;
                }
            }
            for ( MaplePet pet : chr.getSummonedPets()) {
                final List excluded = pet.getExcluded();
                if (excluded.size() > 0) {
                    for ( Object excluded2 : excluded) {
                        if ((int)(Integer)excluded2 == mdrop.getItemId()) {
                            吸物状态 = false;
                            break;
                        }
                    }
                }
            }
            if (吸物状态 && MapleMap.特殊宠物吸物无法使用地图开关) {
                for (int i = 0; i < MapleMap.特殊宠物吸物无法使用地图.length; ++i) {
                    if (this.mapid == Integer.parseInt(MapleMap.特殊宠物吸物无法使用地图[i])) {
                        吸物状态 = false;
                        break;
                    }
                }
            }
            if (吸物状态 && mdrop.getItem().getItemId() != 0 && MapleInventoryManipulator.checkSpace(chr.getClient(), mdrop.getItemId(), (int) mdrop.getItem().getQuantity(), mdrop.getItem().getOwner())) {
                final byte petz = chr.getPetIndex(宠物数据库ID);
                InventoryHandler.removeItemPet(chr, mdrop, (int)petz);
                MapleInventoryManipulator.addFromDrop(chr.getClient(), mdrop.getItem(), true, mdrop.getDropper() instanceof MapleMonster, true);
            }
        }
    }

    public void spawnRandDrop() {
        if (this.mapid != 910000000 || this.channel != 1) {
            return;
        }
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.ITEM)).readLock().lock();
        try {
            for ( MapleMapObject o : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.ITEM)).values()) {
                if (((MapleMapItem)o).isRandDrop()) {
                    return;
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.ITEM)).readLock().unlock();
        }
        MapTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                final Point pos = new Point(Randomizer.nextInt(800) + 531, -806);
                int theItem = Randomizer.nextInt(1000);
                int itemid = 0;
                if (theItem < 950) {
                    itemid = GameConstants.normalDrops[Randomizer.nextInt(GameConstants.normalDrops.length)];
                }
                else if (theItem < 990) {
                    itemid = GameConstants.rareDrops[Randomizer.nextInt(GameConstants.rareDrops.length)];
                }
                else {
                    itemid = GameConstants.superDrops[Randomizer.nextInt(GameConstants.superDrops.length)];
                }
                MapleMap.this.spawnAutoDrop(itemid, pos);
            }
        }, 20000L);
    }
    
    public void spawnAutoDrop(int itemid, final Point pos) {
        IItem idrop = null;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
            idrop = ii.randomizeStats((Equip)ii.getEquipById(itemid));
        }
        else {
            idrop = new Item(itemid, (short)0, (short)1, (byte)0);
        }
        final MapleMapItem mdrop = new MapleMapItem(pos, idrop);
        this.spawnAndAddRangedMapObject((MapleMapObject)mdrop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, pos, pos, (byte)1));
            }
        }, null);
        this.broadcastMessage(MaplePacketCreator.dropItemFromMapObject(mdrop, pos, pos, (byte)0));
        mdrop.registerExpire(LtMS.ConfigValuesMap.get("物品掉落持续时间")*1000L);
    }
    
    public void spawnAutoDrop2(int itemid, final Point pos) {
        IItem idrop = null;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (GameConstants.getInventoryType(itemid) == MapleInventoryType.EQUIP) {
            idrop = ii.randomizeStats((Equip)ii.getEquipById(itemid));
        }
        else {
            idrop = new Item(itemid, (short)0, (short)1, (byte)0);
        }
        final MapleMapItem mdrop = new MapleMapItem(pos, idrop);
        this.spawnAndAddRangedMapObject((MapleMapObject)mdrop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                if ((boolean)c.getPlayer().isCheating) {
                    return;
                }
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(mdrop, pos, pos, (byte)1));
            }
        }, null);
        this.broadcastMessage(MaplePacketCreator.dropItemFromMapObject(mdrop, pos, pos, (byte)0));
        mdrop.registerExpire(10000L);
    }
    
    public void 物品掉落(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos, final boolean ffaDrop, final boolean playerDrop) {
        final Point droppos = this.calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte)2, playerDrop);
        this.spawnAndAddRangedMapObject((MapleMapObject)drop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                if ((boolean)owner.isCheating) {
                    return;
                }
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte)1));
            }
        }, null);
        if ((boolean)owner.isCheating) {
            return;
        }
        this.broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte)0));
        if (!this.everlast) {
            drop.registerExpire(LtMS.ConfigValuesMap.get("物品掉落持续时间")*1000L);
            this.activateItemReactors(drop, owner.getClient());
        }
    }
    
    public void spawnItemDrop(final MapleMapObject dropper, final MapleCharacter owner, final IItem item, final Point pos, final boolean ffaDrop, final boolean playerDrop) {
        final Point droppos = this.calcDropPos(pos, pos);
        final MapleMapItem drop = new MapleMapItem(item, droppos, dropper, owner, (byte)2, playerDrop);
        this.spawnAndAddRangedMapObject((MapleMapObject)drop, (DelayedPacketCreation)new DelayedPacketCreation() {
            @Override
            public void sendPackets(final MapleClient c) {
                c.sendPacket(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte)1));
            }
        }, null);
        this.broadcastMessage(MaplePacketCreator.dropItemFromMapObject(drop, dropper.getPosition(), droppos, (byte)0));
        if (!this.everlast) {
            drop.registerExpire(LtMS.ConfigValuesMap.get("物品掉落持续时间")*1000L);
            this.activateItemReactors(drop, owner.getClient());
        }
    }
    
    private void activateItemReactors(final MapleMapItem drop, final MapleClient c) {
        final IItem item = drop.getItem();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().lock();
        try {
            for ( MapleMapObject o : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.REACTOR)).values()) {
                final MapleReactor react = (MapleReactor)o;
                if (react.getReactorType() == 100 && GameConstants.isCustomReactItem(react.getReactorId(), item.getItemId(), (int)Integer.valueOf(react.getReactItem().getLeft())) && (int)Integer.valueOf(react.getReactItem().getRight()) == item.getQuantity() && react.getArea().contains(drop.getPosition()) && !react.isTimerActive()) {
                    MapTimer.getInstance().schedule((Runnable)new ActivateItemReactor(drop, react, c), 5000L);
                    react.setTimerActive(true);
                    break;
                }
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.REACTOR)).readLock().unlock();
        }
    }
    
    public int getItemsSize() {
        return ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.ITEM)).size();
    }
    
    public int getMobsSize() {
        return ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MONSTER)).size();
    }
    
    public List<MapleMapItem> getAllItems() {
        return this.getAllItemsThreadsafe();
    }
    
    public List<MapleMapItem> getAllItemsThreadsafe() {
        final ArrayList<MapleMapItem> ret = new ArrayList<MapleMapItem>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.ITEM)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.ITEM)).values()) {
                ret.add((MapleMapItem)mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.ITEM)).readLock().unlock();
        }
        return ret;
    }
    
    public void returnEverLastItem(MapleCharacter chr) {
        for ( MapleMapObject o : this.getAllItemsThreadsafe()) {
            final MapleMapItem item = (MapleMapItem)o;
            if (item.getOwner() == chr.getId()) {
                item.setPickedUp(true);
                this.broadcastMessage(MaplePacketCreator.removeItemFromMap(item.getObjectId(), 2, chr.getId()), item.getPosition());
                if (item.getMeso() > 0) {
                    chr.gainMeso(item.getMeso(), false);
                }
                else {
                    MapleInventoryManipulator.addFromDrop(chr.getClient(), item.getItem(), false);
                }
                this.removeMapObject((MapleMapObject)item);
            }
        }
        this.spawnRandDrop();
    }
    
    public void talkMonster(final String msg, int itemId, int objectid) {
        if (itemId > 0) {
            this.startMapEffect(msg, itemId, false);
        }
        this.broadcastMessage(MobPacket.talkMonster(objectid, itemId, msg));
        this.broadcastMessage(MobPacket.removeTalkMonster(objectid));
    }
    
    public void startMapEffect(final String msg, int itemId) {
        this.startMapEffect(msg, itemId, false);
    }
    
    public void startMapEffect(final String msg, int itemId, final boolean jukebox) {
        if (this.mapEffect != null) {
            return;
        }
        (this.mapEffect = new MapleMapEffect(msg, itemId)).setJukebox(jukebox);
        this.broadcastMessage(this.mapEffect.makeStartData());
        MapTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                MapleMap.this.broadcastMessage(mapEffect.makeDestroyData());
                mapEffect = null;
            }
        }, jukebox ? 300000L : 30000L);
    }
    
    public void startExtendedMapEffect(final String msg, int itemId) {
        this.broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, true));
        MapTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                MapleMap.this.broadcastMessage(MaplePacketCreator.removeMapEffect());
                MapleMap.this.broadcastMessage(MaplePacketCreator.startMapEffect(msg, itemId, false));
            }
        }, 60000L);
    }
    
    public void startJukebox(final String msg, int itemId) {
        this.startMapEffect(msg, itemId, true);
    }
    
    public void addPlayer(MapleCharacter chr) {
        final List<MapleCharacter> players = this.getAllPlayersThreadsafe();
        for ( MapleCharacter c : players) {
            if (c.getId() == chr.getId()) {
                this.removePlayer(c);
            }
        }

        if (chr.getMapId() == 999999999) {
            chr.dropMessage(1, "卡地图解救");
            chr.changeMap(100000000, 0);
        }

        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.PLAYER)).writeLock().lock();
        try {
            ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.PLAYER)).put(Integer.valueOf(chr.getObjectId()), chr);
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.PLAYER)).writeLock().unlock();
        }
        this.charactersLock.writeLock().lock();
        try {
            this.characters.add(chr);
        }
        finally {
            this.charactersLock.writeLock().unlock();
        }
        chr.setChangeTime(true);
        if (this.mapid == 109080000 || this.mapid == 109080001 || this.mapid == 109080002 || this.mapid == 109080003 || this.mapid == 109080010 || this.mapid == 109080011 || this.mapid == 109080012) {
            chr.setCoconutTeam((int)(this.getAndSwitchTeam() ? 0 : 1));
        }
        final byte[] packet = MaplePacketCreator.spawnPlayerMapobject(chr);
        if (!chr.isHidden()) {
            this.broadcastMessage(chr, packet, false);
            if (chr.isGM() && this.speedRunStart > 0L) {
                this.endSpeedRun();
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "The speed run has ended."));
            }
        }
        else {
            this.broadcastGMMessage(chr, packet, false);
        }
        if (!chr.isClone()) {
            if (!this.onFirstUserEnter.equals((Object)"") && this.getCharactersSize() == 1) {
                MapScriptMethods.startScript_FirstUser(chr.getClient(), this.onFirstUserEnter);
            }
            this.sendObjectPlacement(chr);
            chr.getClient().sendPacket(MaplePacketCreator.spawnPlayerMapobject(chr));
            if (!this.onUserEnter.equals((Object)"")) {
                MapScriptMethods.startScript_User(chr.getClient(), this.onUserEnter);
            }
            switch (this.mapid) {
                case 109030001:
                case 109040000:
                case 109060001:
                case 109080000:
                case 109080010: {
                    chr.getClient().sendPacket(MaplePacketCreator.showEventInstructions());
                    break;
                }
                case 809000101:
                case 809000201: {
                    chr.getClient().sendPacket(MaplePacketCreator.showEquipEffect());
                    break;
                }
                case 910000000: {
                    MapleCharacter victim = null;
                    chr.getClient().getChannelServer();
                    if (!ChannelServer.clones.isEmpty()) {
                        chr.getClient().getChannelServer();
                        ArrayList<离线人偶> clone = ChannelServer.clones;
                        if (!chr.isGM()) {
                            for ( 离线人偶 jr : clone) {
                                if (chr.getClient().getAccID() == jr.AccId) {
                                    chr.getClient().getChannelServer();
                                    ChannelServer.clones.remove((Object)jr);
                                    break;
                                }
                            }
                        }
                        int nowchannel = chr.getClient().getChannelServer().getChannel();
                        chr.getClient().getChannelServer();
                        clone = ChannelServer.clones;
                        for ( 离线人偶 jr2 : clone) {
                            if (jr2.channel == nowchannel) {
                                victim = MapleCharacter.loadCharFromDB(jr2.charId, new MapleClient(null, null, (Channel)new MockIOSession()), true);
                                if (victim == null) {
                                    continue;
                                }
                                int ch = Find.findChannel(victim.getName());
                                if (ch != -1) {
                                    continue;
                                }
                                if (jr2.chairId > 0) {
                                    victim.setChair(jr2.chairId);
                                }
                                victim.setPosition(new Point(jr2.x, jr2.y));
                                victim.setStance(Randomizer.rand(4, 5));
                                chr.getClient().getSession().write((Object)MaplePacketCreator.spawnPlayerMapobject(victim));
                            }
                        }
                        break;
                    }
                    break;
                }
            }
        }
        for ( MaplePet pet : chr.getSummonedPets()) {
            if (pet.getSummoned()) {
                this.broadcastMessage(chr, PetPacket.showPet(chr, pet, false, false), false);
                chr.getClient().sendPacket(PetPacket.showPet(chr, pet, false, false));
                chr.getClient().sendPacket(PetPacket.petStatUpdate(chr));
                chr.getClient().sendPacket(PetPacket.loadExceptionList(chr, pet));
            }
        }
        if (chr.getParty() != null && !chr.isClone()) {
            chr.silentPartyUpdate();
            chr.getClient().sendPacket(MaplePacketCreator.updateParty(chr.getClient().getChannel(), chr.getParty(), PartyOperation.SILENT_UPDATE, null));
            chr.updatePartyMemberHP();
            chr.receivePartyMemberHP();
        }
        if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"地图名称开关")) > 0) {
            chr.startMapEffect(chr.getMap().getMapName(), 5120023, 5000);
        }
        chr.removeAll(1472063);
        chr.removeAll(2060006);
        chr.removeAll(4001101);
        switch (this.mapid) {
            case 103000800:
                chr.removeAll(4001008);
                chr.removeAll(4001007);
                chr.startMapEffect("解决问题并收集通行证的数量，即可进入下一关！", 5120017);
                chr.Gaincharacterz("" + chr.getId() + "", 499, 1);
                break;
            case 103000801:
                chr.startMapEffect("团队合作，爬上绳索揭开正确的组合吧!", 5120017);
                break;
            case 103000802:
                chr.startMapEffect("团队合作，在平台上揭开正确的组合吧!", 5120017);
                break;
            case 103000803:
                chr.startMapEffect("团队合作，在木桶上揭开正确的组合吧!", 5120017);
                break;
            case 103000804:
                chr.startMapEffect("打败绿水灵王和它的小水灵吧！！!", 5120017);
                break;
            case 103000805:
                chr.startMapEffect("恭喜你通关了，领取你的奖励吧！", 5120017);
                break;
            case 180000001:
                chr.startMapEffect("拉 桑 特 监 狱", 5120018);
                break;
            case 209080000:
                if (this.getMonsterById(9400714) == null) {
                    MapleMonster mob1 = MapleLifeFactory.getMonster(9400714);
                    this.spawnMonsterOnGroundBelow(mob1, new Point(1450, 140));
                }
                break;
            case 910010000:
                chr.startMapEffect("快种上种子，保护月妙兔兔生产年糕吧！", 5120016);
                break;
            case 920010000:
                chr.startMapEffect("打碎白色的云朵，收集云片！", 5120019);
                break;
            case 920010200:
                chr.startMapEffect("收集第一个小碎片吧！", 5120019);
                break;
            case 920010300:
                chr.startMapEffect("收集第二个小碎片吧！", 5120019);
                break;
            case 920010400:
                chr.startMapEffect("这美妙的音乐，真是让人心旷神怡！", 5120019);
                break;
            case 920010500:
                chr.startMapEffect("想一想，该如何通关呢！", 5120019);
                break;
            case 920010600:
                chr.startMapEffect("想一想，该如何通关呢！", 5120019);
                break;
            case 920010700:
                chr.startMapEffect("想一想，该如何通关呢！", 5120019);
                break;
            case 922010100:
                chr.removeAll(4001022);
                chr.removeAll(4001023);
                chr.startMapEffect("击杀老鼠，收集通行证就可以进入下一关！", 5120018);
                break;
            case 922010200:
                chr.startMapEffect("打碎箱子，收集通行证就可以进入下一关!", 5120018);
                break;
            case 922010300:
                chr.startMapEffect("击杀怪物，收集通行证就可以进入下一关!", 5120018);
                break;
            case 922010400:
                chr.startMapEffect("击杀黑暗中的怪物，收集通行证!", 5120018);
                break;
            case 922010500:
                chr.startMapEffect("赶快动起来，收集通行证!", 5120018);
                break;
            case 922010600:
                chr.startMapEffect("这里只有一条正确的路！", 5120018);
                break;
            case 922010700:
                chr.startMapEffect("击杀怪物，收集通行证就可以进入下一关！", 5120018);
                break;
            case 922010800:
                chr.startMapEffect("跳上箱子，和队友推算是正确的组合吧！", 5120018);
                break;
            case 922010900:
                chr.startMapEffect("击杀BOSS，拿到钥匙！", 5120018);
                break;
            case 925100000:
                chr.removeAll(4001117);
                chr.removeAll(4001120);
                chr.removeAll(4001121);
                chr.removeAll(4001122);
                chr.startMapEffect("打碎宝箱，收集钥匙", 5120020);
                break;
            case 925100100:
                chr.startMapEffect("击杀海盗，收集海盗证明！", 5120020);
                break;
            case 925100300:
                chr.startMapEffect("消灭这里的守卫，快消灭他们！", 5120020);
                break;
            case 925100500:
                chr.startMapEffect("消灭老海盗！", 5120020);
                break;
            case 926100000:
                chr.startMapEffect("请找到隐藏的门，通过调查实验室！", 5120021);
                break;
            case 926100001:
                chr.startMapEffect("找到你的方式通过这黑暗！", 5120021);
                break;
            case 926100100:
                chr.startMapEffect("充满能量的烧杯！", 5120021);
                break;
            case 926100200:
                chr.startMapEffect("获取实验的文件通过每个门!", 5120021);
                break;
            case 926100203:
                chr.startMapEffect("请打败所有的怪物！!", 5120021);
                break;
            case 926100300:
                chr.startMapEffect("找到你的方法通过实验室！", 5120021);
                break;
            case 926100401:
                chr.startMapEffect("请保护我的爱人！", 5120021);
                break;
            case 930000000:
                chr.startMapEffect("进入传送点，我要对你们施放变身魔法了！", 5120023);
                break;
            case 930000100:
                chr.startMapEffect("消灭这里的怪物，将怪物数量净化到低于20只就可以通关了！", 5120023);
                break;
            case 930000200:
                chr.startMapEffect("需要稀释毒液，然后释放在荆棘上面，就可以打破阻拦！", 5120023);
                break;
            case 930000300:
                chr.startMapEffect("找到出去的路，别迷失在这里!", 5120023);
                break;
            case 930000500:
                chr.startMapEffect("赶快去上方寻找紫色魔力石！!", 5120023);
                break;
            case 930000600:
                chr.startMapEffect("将魔力石放在祭坛上！", 5120023);
        }






        final MapleStatEffect stat = chr.getStatForBuff(MapleBuffStat.SUMMON);
        if (stat != null && !chr.isClone()) {
            final MapleSummon summon = (MapleSummon)chr.getSummons().get((Object)Integer.valueOf(stat.getSourceId()));
            summon.setPosition(chr.getPosition());
            try {
                summon.setFh(this.getFootholds().findBelow(chr.getPosition()).getId());
            }
            catch (NullPointerException e) {
                summon.setFh(0);
            }
            chr.addVisibleMapObject((MapleMapObject)summon);
            this.spawnSummon(summon);
        }
        if (this.mapEffect != null) {
            this.mapEffect.sendStartData(chr.getClient());
        }
        if (this.timeLimit > 0 && this.getForcedReturnMap() != null && !chr.isClone()) {
            chr.startMapTimeLimitTask(this.timeLimit, this.getForcedReturnMap());
        }
        if (chr.getBuffedValue(MapleBuffStat.MONSTER_RIDING) != null && FieldLimitType.Mount.check(this.fieldLimit)) {
            chr.cancelBuffStats(MapleBuffStat.MONSTER_RIDING);
        }
        if (this.hasBoat() == 2) {
            chr.getClient().sendPacket(MaplePacketCreator.boatPacket(true));
        }
        else if (this.hasBoat() == 1 && (chr.getMapId() != 200090000 || chr.getMapId() != 200090010)) {
            chr.getClient().sendPacket(MaplePacketCreator.boatPacket(false));
        }
        if (!chr.isClone()) {
            if (chr.getEventInstance() != null && chr.getEventInstance().isTimerStarted() && !chr.isClone()) {
                chr.getClient().sendPacket(MaplePacketCreator.getClock((int)(chr.getEventInstance().getTimeLeft() / 1000L)));
            }
            if (this.hasClock()) {
                final Calendar cal = Calendar.getInstance();
                chr.getClient().sendPacket(MaplePacketCreator.getClockTime(cal.get(11), cal.get(12), cal.get(13)));
            }
            if (chr.getCarnivalParty() != null && chr.getEventInstance() != null) {
                chr.getEventInstance().onMapLoad(chr);
            }
            MapleEvent.mapLoad(chr, (int)this.channel);
            if (this.getSquadBegin() != null && this.getSquadBegin().getTimeLeft() > 0L && this.getSquadBegin().getStatus() == 1) {
                chr.getClient().sendPacket(MaplePacketCreator.getClock((int)(this.getSquadBegin().getTimeLeft() / 1000L)));
            }
            if (this.mapid != 280030000 && this.mapid != 240060000 && this.mapid != 240060100 && this.mapid != 240060200 && this.mapid != 270050100 && this.mapid != 551030200 && this.mapid / 1000 != 105100 && this.mapid / 100 != 8020003 && this.mapid / 100 != 8020008) {
                final MapleSquad sqd = this.getSquadByMap();
                if (!this.squadTimer && sqd != null && chr.getName().equals((Object)sqd.getLeaderName()) && !chr.isClone()) {
                    this.doShrine(false);
                    this.squadTimer = true;
                }
            }
            for ( WeakReference<MapleCharacter> chrz : chr.getClones()) {
                if (chrz.get() != null) {
                    ((MapleCharacter)chrz.get()).setPosition(new Point(chr.getPosition()));
                    ((MapleCharacter)chrz.get()).setMap(this);
                    this.addPlayer((MapleCharacter)chrz.get());
                }
            }
            if (this.mapid == 914000000) {
                chr.getClient().sendPacket(MaplePacketCreator.temporaryStats_Aran());
            }
            else if (this.mapid == 105100300 && chr.getLevel() >= 91) {
                chr.getClient().sendPacket(MaplePacketCreator.temporaryStats_Balrog(chr));
            }
            else if (this.mapid == 140090000 || this.mapid == 105100301 || this.mapid == 105100100) {
                chr.getClient().sendPacket(MaplePacketCreator.temporaryStats_Reset());
            }
        }
        if (this.permanentWeather > 0) {
            chr.getClient().sendPacket(MaplePacketCreator.startMapEffect("", this.permanentWeather, false));
        }
        if (this.getPlatforms().size() > 0) {
            chr.getClient().sendPacket(MaplePacketCreator.getMovingPlatforms(this));
        }
        if (this.environment.size() > 0) {
            chr.getClient().sendPacket(MaplePacketCreator.getUpdateEnvironment(this));
        }
        if ((Integer)LtMS.ConfigValuesMap.get("过图存档开关") > 0) {
            chr.saveToDB(false, false);
        }

        if ((Integer)LtMS.ConfigValuesMap.get("下雪天开关") > 0 || (Integer)LtMS.ConfigValuesMap.get("下红花开关") > 0 || (Integer)LtMS.ConfigValuesMap.get("下气泡开关") > 0 || (Integer)LtMS.ConfigValuesMap.get("下雪花开关") > 0 || (Integer)LtMS.ConfigValuesMap.get("下枫叶开关") > 0) {
            if ((Integer)LtMS.ConfigValuesMap.get("下雪天开关") > 0) {
                chr.getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", 5120000, false));
            } else if ((Integer)LtMS.ConfigValuesMap.get("下红花开关") > 0) {
                chr.getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", 5120001, false));
            } else if ((Integer)LtMS.ConfigValuesMap.get("下气泡开关") > 0) {
                chr.getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", 5120002, false));
            } else if ((Integer)LtMS.ConfigValuesMap.get("下雪花开关") > 0) {
                chr.getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", 5120003, false));
            } else if ((Integer)LtMS.ConfigValuesMap.get("下枫叶开关") > 0) {
                chr.getMap().broadcastMessage(MaplePacketCreator.startMapEffect("", 5120008, false));
            }
        }

        this.hasCheckIn = true;
    }
    
    public int getNumItems() {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.ITEM)).readLock().lock();
        try {
            return ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.ITEM)).size();
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.ITEM)).readLock().unlock();
        }
    }
    
    public int getNumMonsters() {
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().lock();
        try {
            return ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.MONSTER)).size();
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.MONSTER)).readLock().unlock();
        }
    }
    
    public void doShrine(final boolean spawned) {
        if (this.squadSchedule != null) {
            this.cancelSquadSchedule();
        }
        int mode = (this.mapid == 280030000) ? 1 : ((this.mapid == 280030001) ? 2 : ((this.mapid == 240060200 || this.mapid == 240060201) ? 3 : 0));
        final MapleSquad sqd = this.getSquadByMap();
        final EventManager em = this.getEMByMap();
        if (sqd != null && em != null && this.getCharactersSize() > 0) {
            final String leaderName = sqd.getLeaderName();
            final String state = em.getProperty("state");
            MapleMap returnMapa = this.getForcedReturnMap();
            if (returnMapa == null || returnMapa.getId() == this.mapid) {
                returnMapa = this.getReturnMap();
            }
            if (mode == 1) {
                this.broadcastMessage(MaplePacketCreator.showZakumShrine(spawned, 5));
            }
            else if (mode == 2) {
                this.broadcastMessage(MaplePacketCreator.showChaosZakumShrine(spawned, 5));
            }
            else if (mode == 3) {
                this.broadcastMessage(MaplePacketCreator.showChaosHorntailShrine(spawned, 5));
            }
            else {
                this.broadcastMessage(MaplePacketCreator.showHorntailShrine(spawned, 5));
            }
            if (mode == 1 || spawned) {
                this.broadcastMessage(MaplePacketCreator.getClock(300));
            }
            final MapleMap returnMapz = returnMapa;
            Runnable run;
            if (!spawned) {
                final List<MapleMonster> monsterz = this.getAllMonstersThreadsafe();
                final List<Integer> monsteridz = new ArrayList<Integer>();
                for ( MapleMapObject m : monsterz) {
                    monsteridz.add(Integer.valueOf(m.getObjectId()));
                }
                run = new Runnable() {
                    @Override
                    public void run() {
                        final MapleSquad sqnow = MapleMap.this.getSquadByMap();
                        if (MapleMap.this.getCharactersSize() > 0 && MapleMap.this.getNumMonsters() == monsterz.size() && sqnow != null && sqnow.getStatus() == 2 && sqnow.getLeaderName().equals((Object)leaderName) && MapleMap.this.getEMByMap().getProperty("state").equals((Object)state)) {
                            boolean passed = monsterz.isEmpty();
                            for ( MapleMapObject m : MapleMap.this.getAllMonstersThreadsafe()) {
                                final Iterator<Integer> iterator2 = monsteridz.iterator();
                                while (iterator2.hasNext()) {
                                    int i = (int)Integer.valueOf(iterator2.next());
                                    if (m.getObjectId() == i) {
                                        passed = true;
                                        break;
                                    }
                                }
                                if (passed) {
                                    break;
                                }
                            }
                            if (passed) {
                                byte[] packet;
                                if (mode == 1) {
                                    packet = MaplePacketCreator.showZakumShrine(spawned, 0);
                                }
                                else if (mode == 2) {
                                    packet = MaplePacketCreator.showChaosZakumShrine(spawned, 0);
                                }
                                else {
                                    packet = MaplePacketCreator.showHorntailShrine(spawned, 0);
                                }
                                for ( MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) {
                                    chr.getClient().sendPacket(packet);
                                    chr.changeMap(returnMapz, returnMapz.getPortal(0));
                                }
                                MapleMap.this.checkStates("");
                                MapleMap.this.resetFully();
                            }
                        }
                    }
                };
            }
            else {
                run = new Runnable() {
                    @Override
                    public void run() {
                        final MapleSquad sqnow = MapleMap.this.getSquadByMap();
                        if (MapleMap.this.getCharactersSize() > 0 && sqnow != null && sqnow.getStatus() == 2 && sqnow.getLeaderName().equals((Object)leaderName) && MapleMap.this.getEMByMap().getProperty("state").equals((Object)state)) {
                            byte[] packet;
                            if (mode == 1) {
                                packet = MaplePacketCreator.showZakumShrine(spawned, 0);
                            }
                            else if (mode == 2) {
                                packet = MaplePacketCreator.showChaosZakumShrine(spawned, 0);
                            }
                            else {
                                packet = MaplePacketCreator.showHorntailShrine(spawned, 0);
                            }
                            for ( MapleCharacter chr : MapleMap.this.getCharactersThreadsafe()) {
                                chr.getClient().sendPacket(packet);
                                chr.changeMap(returnMapz, returnMapz.getPortal(0));
                            }
                            MapleMap.this.checkStates("");
                            MapleMap.this.resetFully();
                        }
                    }
                };
            }
            this.squadSchedule = MapTimer.getInstance().schedule(run, 300000L);
        }
    }
    
    public MapleSquad getSquadByMap() {
        MapleSquadType zz = null;
        switch (this.mapid) {
            case 105100300: {
                zz = MapleSquadType.bossbalrog;
                break;
            }
            case 280030000: {
                zz = MapleSquadType.zak;
                break;
            }
            case 280030001: {
                zz = MapleSquadType.chaoszak;
                break;
            }
            case 240060000:
            case 240060100:
            case 240060200: {
                zz = MapleSquadType.horntail;
                break;
            }
            case 240060201: {
                zz = MapleSquadType.chaosht;
                break;
            }
            case 270050100: {
                zz = MapleSquadType.pinkbean;
                break;
            }
            case 802000111: {
                zz = MapleSquadType.nmm_squad;
                break;
            }
            case 802000211: {
                zz = MapleSquadType.vergamot;
                break;
            }
            case 802000411: {
                zz = MapleSquadType.dunas;
                break;
            }
            case 802000611: {
                zz = MapleSquadType.nibergen_squad;
                break;
            }
            case 802000711: {
                zz = MapleSquadType.dunas2;
                break;
            }
            case 802000801:
            case 802000802:
            case 802000803: {
                zz = MapleSquadType.core_blaze;
                break;
            }
            case 802000821:
            case 802000823: {
                zz = MapleSquadType.aufheben;
                break;
            }
            case 211070100:
            case 211070101:
            case 211070110: {
                zz = MapleSquadType.vonleon;
                break;
            }
            case 551030200: {
                zz = MapleSquadType.scartar;
                break;
            }
            case 271040100: {
                zz = MapleSquadType.cygnus;
                break;
            }
            default: {
                return null;
            }
        }
        return ChannelServer.getInstance((int)this.channel).getMapleSquad(zz);
    }
    
    public MapleSquad getSquadBegin() {
        if (this.squad != null) {
            return ChannelServer.getInstance((int)this.channel).getMapleSquad(this.squad);
        }
        return null;
    }
    
    public EventManager getEMByMap() {
        String em = null;
        switch (this.mapid) {
            case 105100300: {
                em = "BossBalrog";
                break;
            }
            case 280030000: {
                em = "ZakumBattle";
                break;
            }
            case 240060000:
            case 240060100:
            case 240060200: {
                em = "HorntailBattle";
                break;
            }
            case 280030001: {
                em = "ChaosZakum";
                break;
            }
            case 240060201: {
                em = "ChaosHorntail";
                break;
            }
            case 270050100: {
                em = "PinkBeanBattle";
                break;
            }
            case 802000111: {
                em = "NamelessMagicMonster";
                break;
            }
            case 802000211: {
                em = "Vergamot";
                break;
            }
            case 802000311: {
                em = "tokyo_2095";
                break;
            }
            case 802000411: {
                em = "Dunas";
                break;
            }
            case 802000611: {
                em = "Nibergen";
                break;
            }
            case 802000711: {
                em = "Dunas2";
                break;
            }
            case 802000801:
            case 802000802:
            case 802000803: {
                em = "CoreBlaze";
                break;
            }
            case 802000821:
            case 802000823: {
                em = "Aufhaven";
                break;
            }
            case 211070100:
            case 211070101:
            case 211070110: {
                em = "VonLeonBattle";
                break;
            }
            case 551030200: {
                em = "ScarTarBattle";
                break;
            }
            case 271040100: {
                em = "CygnusBattle";
                break;
            }
            case 262030300: {
                em = "HillaBattle";
                break;
            }
            case 262031300: {
                em = "DarkHillaBattle";
                break;
            }
            case 272020110:
            case 272030400: {
                em = "ArkariumBattle";
                break;
            }
            case 955000100:
            case 955000200:
            case 955000300: {
                em = "AswanOffSeason";
                break;
            }
            case 280030100: {
                em = "ZakumBattle";
                break;
            }
            case 272020200: {
                em = "Akayile";
                break;
            }
            case 689013000: {
                em = "PinkZakum";
                break;
            }
            case 703200400: {
                em = "0AllBoss";
                break;
            }
            default: {
                return null;
            }
        }
        return ChannelServer.getInstance((int)this.channel).getEventSM().getEventManager(em);
    }
    
    public void broadcastNONGMMessage(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        this.broadcastNONGMMessage(repeatToSource ? null : source, packet);
    }
    
    private void broadcastNONGMMessage(final MapleCharacter source, final byte[] packet) {
        this.charactersLock.readLock().lock();
        try {
            if (source == null) {
                for ( MapleCharacter chr : this.characters) {
                    if (!chr.isStaff()) {
                        chr.getClient().getSession().writeAndFlush((Object)packet);
                    }
                }
            }
            else {
                for ( MapleCharacter chr : this.characters) {
                    if (chr != source && chr.getGMLevel() < 3) {
                        chr.getClient().getSession().writeAndFlush((Object)packet);
                    }
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
    }
    
    public void broadcastMessage(final byte[] packet) {
        this.broadcastMessage(null, packet, Double.POSITIVE_INFINITY, null);
    }
    
    public void broadcastMessage(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        this.broadcastMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }
    
    public int playerCount() {
        final List<MapleMapObject> players = this.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.PLAYER));
        return players.size();
    }
    
    public int mobCount() {
        final List<MapleMapObject> mobsCount = this.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        return mobsCount.size();
    }
    
    public void broadcastMessage(final byte[] packet, final Point rangedFrom) {
        this.broadcastMessage(null, packet, (double)GameConstants.maxViewRangeSq(), rangedFrom);
    }
    
    public void broadcastMessage(final MapleCharacter source, final byte[] packet, final Point rangedFrom) {
        this.broadcastMessage(source, packet, (double)GameConstants.maxViewRangeSq(), rangedFrom);
    }
    
    private void broadcastMessage(final MapleCharacter source, final byte[] packet, final double rangeSq, final Point rangedFrom) {
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter chr : this.characters) {
                if (chr != source) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq((Point2D)chr.getPosition()) > rangeSq) {
                            continue;
                        }
                        chr.getClient().sendPacket(packet);
                    }
                    else {
                        chr.getClient().sendPacket(packet);
                    }
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
    }
    
    private void sendObjectPlacement(final MapleCharacter c) {
        if (c == null || c.isClone()) {
            return;
        }
        for ( MapleMapObject o : this.getAllMonstersThreadsafe()) {
            this.updateMonsterController((MapleMonster)o);
        }
        for ( MapleMapObject o : this.getMapObjectsInRange(c.getPosition(), (double)GameConstants.maxViewRangeSq(), GameConstants.rangedMapobjectTypes)) {
            if (o.getType() == MapleMapObjectType.REACTOR && !((MapleReactor)o).isAlive()) {
                continue;
            }
            o.sendSpawnData(c.getClient());
            c.addVisibleMapObject(o);
        }
    }
    
    public List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq) {
        final List<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        for ( MapleMapObjectType type : MapleMapObjectType.values()) {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().lock();
            try {
                for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)type)).values()) {
                    if (from.distanceSq((Point2D)mmo.getPosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            }
            finally {
                ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().unlock();
            }
        }
        return ret;
    }
    
    public List<MapleMapObject> getItemsInRange(final Point from, final double rangeSq) {
        return this.getMapObjectsInRange(from, rangeSq, Arrays.asList(MapleMapObjectType.ITEM));
    }
    
    public List<MapleMapObject> getMapObjectsInRange(final Point from, final double rangeSq, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        for ( MapleMapObjectType type : MapObject_types) {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().lock();
            try {
                for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)type)).values()) {
                    if (from.distanceSq((Point2D)mmo.getPosition()) <= rangeSq) {
                        ret.add(mmo);
                    }
                }
            }
            finally {
                ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().unlock();
            }
        }
        return ret;
    }
    
    public List<MapleMapObject> getMapObjectsInRect(final Rectangle box, final List<MapleMapObjectType> MapObject_types) {
        final List<MapleMapObject> ret = new ArrayList<MapleMapObject>();
        for ( MapleMapObjectType type : MapObject_types) {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().lock();
            try {
                for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)type)).values()) {
                    if (box.contains(mmo.getPosition())) {
                        ret.add(mmo);
                    }
                }
            }
            finally {
                ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)type)).readLock().unlock();
            }
        }
        return ret;
    }
    
    public List<MapleCharacter> getAllPlayersThreadsafe() {
        final List<MapleCharacter> ret = new LinkedList<MapleCharacter>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.PLAYER)).readLock().lock();
        try {
            for ( MapleMapObject chr : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.PLAYER)).values()) {
                ret.add((MapleCharacter)chr);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.PLAYER)).readLock().unlock();
        }
        return ret;
    }
    
    public List<MapleCharacter> getPlayersInRectThreadsafe(final Rectangle box, final List<MapleCharacter> chrList) {
        final List<MapleCharacter> character = new LinkedList<MapleCharacter>();
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter a : this.characters) {
                if (chrList.contains((Object)a) && box.contains(a.getPosition())) {
                    character.add(a);
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return character;
    }
    
    public void addPortal(final MaplePortal myPortal) {
        this.portals.put(Integer.valueOf(myPortal.getId()), myPortal);
    }
    
    public MaplePortal getPortal(final String portalname) {
        for ( MaplePortal port : this.portals.values()) {
            if (port.getName().equals((Object)portalname)) {
                return port;
            }
        }
        return null;
    }
    
    public MaplePortal getPortal(int portalid) {
        return (MaplePortal)this.portals.get((Object)Integer.valueOf(portalid));
    }
    
    public void resetPortals() {
        for ( MaplePortal port : this.portals.values()) {
            port.setPortalState(true);
        }
    }
    
    public void setFootholds(final MapleFootholdTree footholds) {
        this.footholds = footholds;
    }
    
    public MapleFootholdTree getFootholds() {
        return this.footholds;
    }
    
    public void loadMonsterRate(final boolean first) {
        int spawnSize = this.monsterSpawn.size();
        this.maxRegularSpawn = Math.round((float)spawnSize * this.monsterRate);
        if (this.maxRegularSpawn < 2) {
            this.maxRegularSpawn = 2;
        }
        else if (this.maxRegularSpawn > spawnSize) {
            this.maxRegularSpawn = spawnSize - spawnSize / 15;
        }
        if (this.fixedMob > 0) {
            this.maxRegularSpawn = this.fixedMob;
        }
        final Collection<Spawns> newSpawn = new LinkedList<Spawns>();
        final Collection<Spawns> newBossSpawn = new LinkedList<Spawns>();
        for ( Spawns s : this.monsterSpawn) {
            if (s.getCarnivalTeam() >= 2) {
                continue;
            }
            if (s.getMonster().getStats().isBoss()) {
                newBossSpawn.add(s);
            }
            else {
                newSpawn.add(s);
            }
        }
        this.monsterSpawn.clear();
        this.monsterSpawn.addAll((Collection<? extends Spawns>)newBossSpawn);
        this.monsterSpawn.addAll((Collection<? extends Spawns>)newSpawn);
        if (first && spawnSize > 0) {
            this.lastSpawnTime = System.currentTimeMillis();
            if (GameConstants.isForceRespawn(this.mapid)) {
                this.createMobInterval = 1000;
            }
        }
    }
    
    public SpawnPoint addMonsterSpawn(final MapleMonster monster, int mobTime, final byte carnivalTeam, final String msg) {
        final Point calcPointBelow;
        final Point newpos = calcPointBelow = this.calcPointBelow(monster.getPosition());
        --calcPointBelow.y;
        final SpawnPoint sp = new SpawnPoint(monster, newpos, mobTime, carnivalTeam, msg);
        if (carnivalTeam > -1) {
            this.monsterSpawn.add(0, sp);
        }
        else {
            this.monsterSpawn.add(sp);
        }
        return sp;
    }
    
    public void addAreaMonsterSpawn(final MapleMonster monster, Point pos1, Point pos2, Point pos3, int mobTime, final String msg) {
        pos1 = this.calcPointBelow(pos1);
        pos2 = this.calcPointBelow(pos2);
        pos3 = this.calcPointBelow(pos3);
        if (pos1 != null) {
            final Point point = pos1;
            --point.y;
        }
        if (pos2 != null) {
            final Point point2 = pos2;
            --point2.y;
        }
        if (pos3 != null) {
            final Point point3 = pos3;
            --point3.y;
        }
        if (pos1 == null && pos2 == null && pos3 == null) {
            System.err.println("警告: 地图 " + this.mapid + ", 怪物代码 " + monster.getId() + " 召喚失敗. (pos1 == null && pos2 == null && pos3 == null)");
            return;
        }
        if (pos1 != null) {
            if (pos2 == null) {
                pos2 = new Point(pos1);
            }
            if (pos3 == null) {
                pos3 = new Point(pos1);
            }
        }
        else if (pos2 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos2);
            }
            if (pos3 == null) {
                pos3 = new Point(pos2);
            }
        }
        else if (pos3 != null) {
            if (pos1 == null) {
                pos1 = new Point(pos3);
            }
            if (pos2 == null) {
                pos2 = new Point(pos3);
            }
        }
        this.monsterSpawn.add(new SpawnPointAreaBoss(monster, pos1, pos2, pos3, mobTime, msg));
    }
    
    public List<MapleCharacter> getCharacters() {
        return this.getCharactersThreadsafe();
    }
    
    public List<MapleCharacter> getCharactersThreadsafe() {
        final List<MapleCharacter> chars = new ArrayList<MapleCharacter>();
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter mc : this.characters) {
                chars.add(mc);
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return chars;
    }
    
    public MapleCharacter getCharacterByName(final String id) {
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter mc : this.characters) {
                if (mc.getName().equalsIgnoreCase(id)) {
                    final MapleCharacter localMapleCharacter1 = mc;
                    return localMapleCharacter1;
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return null;
    }
    
    public MapleCharacter getCharacterById_InMap(int id) {
        return this.getCharacterById(id);
    }
    
    public MapleCharacter getCharacterById(int id) {
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter mc : this.characters) {
                if (mc.getId() == id) {
                    return mc;
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return null;
    }
    
    public void updateMapObjectVisibility(MapleCharacter chr, final MapleMapObject mo) {
        if (chr == null || chr.isClone()) {
            return;
        }
        if (!chr.isMapObjectVisible(mo)) {
            if (mo.getType() == MapleMapObjectType.SUMMON || mo.getPosition().distanceSq((Point2D)chr.getPosition()) <= (double)GameConstants.maxViewRangeSq()) {
                chr.addVisibleMapObject(mo);
                mo.sendSpawnData(chr.getClient());
            }
        }
        else if (mo.getType() != MapleMapObjectType.SUMMON && mo.getPosition().distanceSq((Point2D)chr.getPosition()) > (double)GameConstants.maxViewRangeSq()) {
            chr.removeVisibleMapObject(mo);
            mo.sendDestroyData(chr.getClient());
        }
    }
    
    public void moveMonster(final MapleMonster monster, final Point reportedPos) {
        monster.setPosition(reportedPos);
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter mc : this.characters) {
                this.updateMapObjectVisibility(mc, (MapleMapObject)monster);
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
    }
    
    public void movePlayer(final MapleCharacter player, final Point newPosition) {
        player.setPosition(newPosition);
        if (!player.isClone()) {
            try {
                final Collection<MapleMapObject> visibleObjects = player.getAndWriteLockVisibleMapObjects();
                final ArrayList<MapleMapObject> copy = new ArrayList<MapleMapObject>((Collection<? extends MapleMapObject>)visibleObjects);
                for ( MapleMapObject mo : copy) {
                    if (mo != null && this.getMapObject(mo.getObjectId(), mo.getType()) == mo) {
                        this.updateMapObjectVisibility(player, mo);
                    }
                    else {
                        if (mo == null) {
                            continue;
                        }
                        visibleObjects.remove((Object)mo);
                    }
                }
                for ( MapleMapObject mo2 : this.getMapObjectsInRange(player.getPosition(), (double)GameConstants.maxViewRangeSq())) {
                    if (mo2 != null && !player.isMapObjectVisible(mo2)) {
                        mo2.sendSpawnData(player.getClient());
                        visibleObjects.add(mo2);
                    }
                }
            }
            finally {
                player.unlockWriteVisibleMapObjects();
            }
        }
    }
    
    public MaplePortal findClosestSpawnpoint(final Point from) {
        MaplePortal closest = null;
        double shortestDistance = Double.POSITIVE_INFINITY;
        for ( MaplePortal portal : this.portals.values()) {
            final double distance = portal.getPosition().distanceSq((Point2D)from);
            if (portal.getType() >= 0 && portal.getType() <= 2 && distance < shortestDistance && portal.getTargetMapId() == 999999999) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }
    
    public MaplePortal findClosestPortal(final Point from) {
        MaplePortal closest = this.getPortal(0);
        double shortestDistance = Double.POSITIVE_INFINITY;
        for ( MaplePortal portal : this.portals.values()) {
            final double distance = portal.getPosition().distanceSq((Point2D)from);
            if (distance < shortestDistance) {
                closest = portal;
                shortestDistance = distance;
            }
        }
        return closest;
    }
    
    public String spawnDebug() {
        final StringBuilder sb = new StringBuilder("Mapobjects in map : ");
        sb.append(this.getMapObjectSize());
        sb.append(" spawnedMonstersOnMap: ");
        sb.append((Object)this.spawnedMonstersOnMap);
        sb.append(" spawnpoints: ");
        sb.append(this.monsterSpawn.size());
        sb.append(" maxRegularSpawn: ");
        sb.append(this.maxRegularSpawn);
        sb.append(" actual monsters: ");
        sb.append(this.getNumMonsters());
        return sb.toString();
    }
    
    public int characterSize() {
        return this.characters.size();
    }
    
    public int getMapObjectSize() {
        return this.mapObjects.size() + this.getCharactersSize() - this.characters.size();
    }
    
    public int getCharactersSize() {
        int ret = 0;
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter chr : this.characters) {
                if (!chr.isClone()) {
                    ++ret;
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }
    
    public Collection<MaplePortal> getPortals() {
        return Collections.unmodifiableCollection((Collection<? extends MaplePortal>)this.portals.values());
    }
    
    public int getSpawnedMonstersOnMap() {
        return this.spawnedMonstersOnMap.get();
    }
    
    public void spawnKite(final MapleKite Kite) {
        this.addMapObject((MapleMapObject)Kite);
        this.broadcastMessage(Kite.makeSpawnData());
        MapTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                MapleMap.this.broadcastMessage(Kite.makeDestroyData());
                MapleMap.this.removeMapObject((MapleMapObject)Kite);
            }
        }, 3600000L);
    }
    
    public void respawn(final boolean force) {
        Integer integer = LtMS.ConfigValuesMap.get((Object) "多倍怪倍数");
        if (integer < 1) {
            integer = 1;
        }
        boolean MultipleSpawn = (Integer)LtMS.ConfigValuesMap.get("多倍怪物开关") > 0;
        if (GameConstants.isBanMultiMobRateMap(this.getId())) {
            MultipleSpawn = false;
        }
        int rateByStone;
        if (this.getStoneLevel() == 1) {
            rateByStone = (Integer)LtMS.ConfigValuesMap.get("1级轮回碑石怪物倍数");
        } else if (this.getStoneLevel() >= 2) {
            rateByStone = (Integer)LtMS.ConfigValuesMap.get("2级轮回碑石怪物倍数");
        } else {
            rateByStone = (Integer)LtMS.ConfigValuesMap.get("轮回碑石怪物倍数");
        }

        if (rateByStone < 1) {
            rateByStone = 1;
        }
        this.lastSpawnTime = System.currentTimeMillis();
//        if (force) {
//            int numShouldSpawn = (this.monsterSpawn.size() * MapConstants.isMonsterSpawn(this) - this.spawnedMonstersOnMap.get());
//            if (numShouldSpawn > 0) {
//                int spawned = 0;
//                for ( Spawns spawnPoint : this.monsterSpawn) {
//                    spawnPoint.spawnMonster(this);
//                    if (++spawned >= numShouldSpawn) {
//                        break;
//                    }
//                }
//            }
//        }
//        else {
//            int defaultNum = (GameConstants.isForceRespawn(this.mapid) ? this.monsterSpawn.size() : this.maxRegularSpawn) - this.spawnedMonstersOnMap.get();
//            int numShouldSpawn2 = integer > 0 ? Math.max(defaultNum, integer) : defaultNum;
//            int spawned = 0;
////                final List<Spawns> randomSpawn = new ArrayList<Spawns>((Collection<? extends Spawns>)this.monsterSpawn);
//            final List<Spawns> randomSpawn = new ArrayList<Spawns>((Collection<? extends Spawns>)this.monsterSpawn);
//            Collections.shuffle(randomSpawn);
//            if (this.mapid == 925100100){
//                for ( Spawns spawnPoint2 : randomSpawn) {
//                    if (spawnPoint2.shouldSpawn() || MapConstants.isForceRespawn(this.mapid)) {
//                        spawnPoint2.spawnMonster(this);
//                        ++spawned;
//                    }
//                    if (spawned >= defaultNum && !GameConstants.isCarnivalMaps(this.mapid)) {
//                        break;
//                    }
//                }
//            }else{
//                if(integer>0) {
//                    for (int i = 0; i < numShouldSpawn2; i++) {
//                        for ( Spawns spawnPoint2 : randomSpawn) {
//                            if (spawnPoint2.shouldSpawn() || MapConstants.isForceRespawn(this.mapid)) {
//                                spawnPoint2.spawnMonster(this);
//                                ++spawned;
//                            }
//                            if (spawned >= numShouldSpawn2 && !GameConstants.isCarnivalMaps(this.mapid)) {
//                                break;
//                            }
//                        }
//                        i += spawned;
//                        if (spawned >= numShouldSpawn2) {
//                            break;
//                        }
//                    }
//                }else{
//                    for ( Spawns spawnPoint2 : randomSpawn) {
//                        if (spawnPoint2.shouldSpawn() || MapConstants.isForceRespawn(this.mapid)) {
//                            spawnPoint2.spawnMonster(this);
//                            ++spawned;
//                        }
//                        if (spawned >= defaultNum && !GameConstants.isCarnivalMaps(this.mapid)) {
//                            break;
//                        }
//                    }
//                }
//            }
//        }

        List<Spawns> spawns = new ArrayList();
        spawns.addAll(this.monsterSpawn);
        int finalCount;
        int count;
        Iterator var8;
        Spawns spawnPoint;
        if (MultipleSpawn) {
            if (this.haveStone && rateByStone > 1) {
                integer *= rateByStone;
            }

            finalCount = integer * this.monsterSpawn.size();


            finalCount -= this.monsterSpawn.size();
            count = 0;

            label206:
            while(count < finalCount) {
                var8 = this.monsterSpawn.iterator();

                while(true) {
                    while(true) {
                        if (!var8.hasNext()) {
                            continue label206;
                        }

                        spawnPoint = (Spawns)var8.next();
                        if (!spawnPoint.getMonster().getStats().isBoss() && !GameConstants.isNoDoubleMap(this.getId())) {
                            if (spawnPoint.shouldSpawn()) {
                                ++count;
                                spawns.add(new SpawnPoint(spawnPoint.getMonster(), spawnPoint.getPosition(), spawnPoint.getMobTime(), spawnPoint.getCarnivalTeam(), spawnPoint.getMessage()));
                            } else {
                                ++count;
                                spawns.add(spawnPoint);
                            }

                            if (count >= finalCount) {
                                continue label206;
                            }
                        } else {
                            ++count;
                        }
                    }
                }
            }
        } else if (this.haveStone && rateByStone > 1 && !GameConstants.isNoDoubleMap(this.getId())) {
            for(finalCount = 1; finalCount < rateByStone; ++finalCount) {
                Iterator var15 = this.monsterSpawn.iterator();

                while(var15.hasNext()) {
                    Spawns point = (Spawns)var15.next();
                    if (!point.getMonster().getStats().isBoss()) {
                        if (point.shouldSpawn()) {
                            spawns.add(new SpawnPoint(point.getMonster(), point.getPosition(), point.getMobTime(), point.getCarnivalTeam(), point.getMessage()));
                        } else {
                            spawns.add(point);
                        }
                    }
                }
            }
        }

        if (force) {
            finalCount = spawns.size() * MapConstants.isMonsterSpawn(this) - this.spawnedMonstersOnMap.get();
            if (finalCount > 0) {
                count = 0;
                var8 = spawns.iterator();

                while(var8.hasNext()) {
                    spawnPoint = (Spawns)var8.next();
                    spawnPoint.spawnMonster(this);
                    ++count;
                    if (count >= finalCount) {
                        break;
                    }
                }
            }
        } else if ((Integer)LtMS.ConfigValuesMap.get("怪物平均分配开关") > 0) {
            finalCount = spawns.size() * MapConstants.isMonsterSpawn(this) - this.spawnedMonstersOnMap.get();
            if (finalCount > 0) {
                count = 0;
                List<Spawns> randomSpawn = new ArrayList(spawns);
                Collections.shuffle(randomSpawn);
                Iterator var16 = randomSpawn.iterator();

                while(var16.hasNext()) {
                    spawnPoint = (Spawns)var16.next();
                    if (this.isRespawnOnePoint() && this.respawnOnePoint && this.respawnPoint != null && !GameConstants.isBossMap(this.getId()) && !spawnPoint.getMonster().getStats().isBoss() && spawnPoint.getMonster().getId() != 9900000 && spawnPoint.getMonster().getId() != 9900001 && spawnPoint.getMonster().getId() != 9900002) {
                        spawnPoint.setPosition(this.respawnPoint);
                        spawnPoint.getMonster().setPosition(this.respawnPoint);
                    }

                    if (spawnPoint.shouldSpawn() || MapConstants.isForceRespawn(this.mapid)) {
                        spawnPoint.spawnMonster(this);
                        ++count;
                    }

                    if (count >= finalCount && !GameConstants.isCarnivalMaps(this.mapid)) {
                        break;
                    }
                }
            }
        } else {
            finalCount = this.maxRegularSpawn - this.spawnedMonstersOnMap.get();
            if (finalCount > 0 && spawns.size() > 0) {
                List<Spawns> randomSpawn = new ArrayList(spawns);
                Collections.shuffle(randomSpawn);
                var8 = randomSpawn.iterator();

                label148:
                while(true) {
                    do {
                        if (!var8.hasNext()) {
                            break label148;
                        }

                        spawnPoint = (Spawns)var8.next();
                        if (this.isRespawnOnePoint() && this.respawnOnePoint && this.respawnPoint != null && !GameConstants.isBossMap(this.getId()) && !spawnPoint.getMonster().getStats().isBoss() && spawnPoint.getMonster().getId() != 9900000 && spawnPoint.getMonster().getId() != 9900001 && spawnPoint.getMonster().getId() != 9900002) {
                            spawnPoint.setPosition(this.respawnPoint);
                            spawnPoint.getMonster().setPosition(this.respawnPoint);
                        }
                    } while(!spawnPoint.shouldSpawn() && !MapConstants.isForceRespawn(this.mapid));

                    spawnPoint.spawnMonster(this);
                }
            }
        }

        spawns.clear();
    }
    
    public String getSnowballPortal() {
        int[] teamss = new int[2];
        for ( MapleCharacter chr : this.getCharactersThreadsafe()) {
            if (chr.getPosition().y > -80) {
                int[] array = teamss;
                int n = 0;
                ++array[n];
            }
            else {
                int[] array2 = teamss;
                int n2 = 1;
                ++array2[n2];
            }
        }
        if (teamss[0] > teamss[1]) {
            return "st01";
        }
        return "st00";
    }
    
    public boolean isDisconnected(int id) {
        return this.disconnectedClients.contains((Object)Integer.valueOf(id));
    }
    
    public void addDisconnected(int id) {
        this.disconnectedClients.add(Integer.valueOf(id));
    }
    
    public void resetDisconnected() {
        this.disconnectedClients.clear();
    }
    
    public void startSpeedRun() {
        final MapleSquad squad = this.getSquadByMap();
        if (squad != null) {
            for ( MapleCharacter chr : this.getCharactersThreadsafe()) {
                if (chr.getName().equals((Object)squad.getLeaderName())) {
                    this.startSpeedRun(chr.getName());
                }
            }
        }
    }
    
    public void startSpeedRun(final String leader) {
        this.speedRunStart = System.currentTimeMillis();
        this.speedRunLeader = leader;
    }
    
    public void endSpeedRun() {
        this.speedRunStart = 0L;
        this.speedRunLeader = "";
    }
    
    public boolean getPapfight() {
        return this.PapfightStart;
    }
    
    public void Papfight() {
        this.PapfightStart = true;
    }
    
    public void EndPapfight() {
        this.PapfightStart = false;
    }
    
    public static int getMerchantMap(MapleCharacter chr) {
        for ( ChannelServer cs : ChannelServer.getAllInstances()) {
            int map = cs.getMerchantMap(chr);
            if (map != -1) {
                return map;
            }
        }
        return -1;
    }
    
    public static int getMerchantChannel(MapleCharacter chr) {
        for ( ChannelServer cs : ChannelServer.getAllInstances()) {
            int map = cs.getMerchantMap(chr);
            if (map != -1) {
                return cs.getChannel();
            }
        }
        return -1;
    }
    
    public void getRankAndAdd(final String leader, final String time, final SpeedRunType type, final long timz, final Collection<String> squad) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final StringBuilder rett = new StringBuilder();
            if (squad != null) {
                for ( String chr : squad) {
                    rett.append(chr);
                    rett.append(",");
                }
            }
            String z = rett.toString();
            if (squad != null) {
                z = z.substring(0, z.length() - 1);
            }
            final PreparedStatement ps = con.prepareStatement("INSERT INTO speedruns(`type`, `leader`, `timestring`, `time`, `members`) VALUES (?,?,?,?,?)");
            ps.setString(1, type.name());
            ps.setString(2, leader);
            ps.setString(3, time);
            ps.setLong(4, timz);
            ps.setString(5, z);
            ps.executeUpdate();
            ps.close();
            if (SpeedRunner.getInstance().getSpeedRunData(type) == null) {
                SpeedRunner.getInstance().addSpeedRunData(type, SpeedRunner.getInstance().addSpeedRunData(new StringBuilder("#rThese are the speedrun times for " + (Object)type + ".#k\r\n\r\n"), (Map<Integer, String>)new HashMap<Integer, String>(), z, leader, 1, time));
            }
            else {
                SpeedRunner.getInstance().removeSpeedRunData(type);
                SpeedRunner.getInstance().loadSpeedRunData(type);
            }
        }
        catch (Exception e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            e.printStackTrace();
        }
    }
    
    public long getSpeedRunStart() {
        return this.speedRunStart;
    }
    
    public void disconnectAll(MapleCharacter chr) {
        for ( MapleCharacter chrs : this.getCharactersThreadsafe()) {
            if (chrs.getGMLevel() < chr.getGMLevel()) {
                chrs.getClient().disconnect(true, false);
                chrs.getClient().getSession().close();
            }
        }
    }
    
    public void disconnectAll() {
        for ( MapleCharacter chr : this.getCharactersThreadsafe()) {
            if (!chr.isGM()) {
                chr.getClient().disconnect(true, false);
                chr.getClient().getSession().close();
            }
        }
    }
    
    public List<MapleNPC> getAllNPCs() {
        return this.getAllNPCsThreadsafe();
    }
    
    public List<MapleNPC> getAllNPCsThreadsafe() {
        final ArrayList<MapleNPC> ret = new ArrayList<MapleNPC>();
        ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).readLock().lock();
        try {
            for ( MapleMapObject mmo : ((LinkedHashMap<Integer, MapleMapObject>)this.mapObjects.get((Object)MapleMapObjectType.NPC)).values()) {
                ret.add((MapleNPC)mmo);
            }
        }
        finally {
            ((ReentrantReadWriteLock)this.mapObjectLocks.get((Object)MapleMapObjectType.NPC)).readLock().unlock();
        }
        return ret;
    }
    
    public void resetNPCs() {
        this.removeNpc(-1);
    }
    
    public void resetFully() {
        this.resetFully(true);
    }
    
    public void resetFully(final boolean respawn) {
        this.killAllMonsters(false);
        this.reloadReactors();
        this.removeDrops();
        this.resetNPCs();
        this.resetSpawns();
        this.resetDisconnected();
        this.endSpeedRun();
        this.cancelSquadSchedule();
        this.resetPortals();
        this.environment.clear();
        if (this.MulungDojoLeaveTask != null && !this.MulungDojoLeaveTask.isCancelled()) {
            this.MulungDojoLeaveTask.cancel(true);
            this.MulungDojoLeaveTask = null;
        }
        if (respawn) {
            this.respawn(true);
        }
    }
    
    public void setMulungDojoLeaveTask(final ScheduledFuture<?> task) {
        this.MulungDojoLeaveTask = task;
    }
    
    public void cancelSquadSchedule() {
        this.squadTimer = false;
        if (this.squadSchedule != null) {
            this.squadSchedule.cancel(false);
            this.squadSchedule = null;
        }
    }
    
    public void removeDrops() {
        final List<MapleMapItem> items = this.getAllItemsThreadsafe();
        for ( MapleMapItem i : items) {
            i.expire(this);
        }
    }
    
    public void resetAllSpawnPoint(int mobid, int mobTime) {
        final Collection<Spawns> sss = new LinkedList<Spawns>((Collection<? extends Spawns>)this.monsterSpawn);
        this.resetFully();
        this.monsterSpawn.clear();
        for ( Spawns s : sss) {
            final MapleMonster newMons = MapleLifeFactory.getMonster(mobid);
            final MapleMonster oldMons = s.getMonster();
            newMons.setCy(oldMons.getCy());
            newMons.setF(oldMons.getF());
            newMons.setFh(oldMons.getFh());
            newMons.setRx0(oldMons.getRx0());
            newMons.setRx1(oldMons.getRx1());
            newMons.setPosition(new Point(oldMons.getPosition()));
            newMons.setHide(oldMons.isHidden());
            this.addMonsterSpawn(newMons, mobTime, (byte)(-1), null);
        }
        this.loadMonsterRate(true);
    }
    
    public void resetSpawns() {
        boolean changed = false;
        final Iterator<Spawns> sss = this.monsterSpawn.iterator();
        while (sss.hasNext()) {
            if (((Spawns)sss.next()).getCarnivalId() > -1) {
                sss.remove();
                changed = true;
            }
        }
        this.setSpawns(true);
        if (changed) {
            this.loadMonsterRate(true);
        }
    }
    
    public boolean makeCarnivalSpawn(int team, final MapleMonster newMons, int num) {
        MonsterPoint ret = null;
        for ( MonsterPoint mp : this.nodes.getMonsterPoints()) {
            if (mp.team == team || mp.team == -1) {
                final Point calcPointBelow;
                final Point newpos = calcPointBelow = this.calcPointBelow(new Point(mp.x, mp.y));
                --calcPointBelow.y;
                boolean found = false;
                for ( Spawns s : this.monsterSpawn) {
                    if (s.getCarnivalId() > -1 && (mp.team == -1 || s.getCarnivalTeam() == mp.team) && s.getPosition().x == newpos.x && s.getPosition().y == newpos.y) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ret = mp;
                    break;
                }
                continue;
            }
        }
        if (ret != null) {
            newMons.setCy(ret.cy);
            newMons.setF(0);
            newMons.setFh(ret.fh);
            newMons.setRx0(ret.x + 50);
            newMons.setRx1(ret.x - 50);
            newMons.setPosition(new Point(ret.x, ret.y));
            newMons.setHide(false);
            newMons.setCarnivalTeam((byte)team);
            final SpawnPoint sp = this.addMonsterSpawn(newMons, 1, (byte)team, null);
            sp.setCarnival(num);
        }
        return ret != null;
    }
    
    public boolean makeCarnivalReactor(int team, int num) {
        final MapleReactor old = this.getReactorByName(team + "" + num);
        if (old != null && old.getState() < 5) {
            return false;
        }
        Point guardz = null;
        final List<MapleReactor> react = this.getAllReactorsThreadsafe();
        for ( Pair<Point, Integer> guard : this.nodes.getGuardians()) {
            if ((int)Integer.valueOf(guard.right) == team || (int)Integer.valueOf(guard.right) == -1) {
                boolean found = false;
                for ( MapleReactor r : react) {
                    if (r.getPosition().x == ((Point)guard.left).x && r.getPosition().y == ((Point)guard.left).y && r.getState() < 5) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    guardz = (Point)guard.left;
                    break;
                }
                continue;
            }
        }
        if (guardz != null) {
            final MapleReactorStats stats = MapleReactorFactory.getReactor(9980000 + team);
            final MapleReactor my = new MapleReactor(stats, 9980000 + team);
            stats.setFacingDirection((byte)0);
            my.setPosition(guardz);
            my.setState((byte)1);
            my.setDelay(0);
            my.setName(team + "" + num);
            this.spawnReactor(my);
            final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
            if (skil != null && skil.getMobSkill() != null) {
                for ( MapleMonster mons : this.getAllMonstersThreadsafe()) {
                    if (mons.getCarnivalTeam() == team) {
                        skil.getMobSkill().applyEffect(null, mons, false);
                    }
                }
            }
        }
        return guardz != null;
    }
    
    public void blockAllPortal() {
        for ( MaplePortal p : this.portals.values()) {
            p.setPortalState(false);
        }
    }
    
    public boolean getAndSwitchTeam() {
        return this.getCharactersSize() % 2 != 0;
    }
    
    public void setSquad(final MapleSquadType s) {
        this.squad = s;
    }
    
    public int getChannel() {
        return this.channel;
    }
    
    public int getConsumeItemCoolTime() {
        return this.consumeItemCoolTime;
    }
    
    public void setConsumeItemCoolTime(int ciit) {
        this.consumeItemCoolTime = ciit;
    }
    
    public void setPermanentWeather(int pw) {
        this.permanentWeather = pw;
    }
    
    public int getPermanentWeather() {
        return this.permanentWeather;
    }
    
    public void checkStates(final String chr) {
        final MapleSquad sqd = this.getSquadByMap();
        final EventManager em = this.getEMByMap();
        int size = this.getCharactersSize();
        if (sqd != null) {
            sqd.removeMember(chr);
            if (em != null) {
                if (sqd.getLeaderName().equals((Object)chr)) {
                    em.setProperty("leader", "false");
                }
                if (chr.equals((Object)"") || size == 0) {
                    sqd.clear();
                    em.setProperty("state", "0");
                    em.setProperty("leader", "true");
                    this.cancelSquadSchedule();
                }
            }
        }
        if (em != null && em.getProperty("state") != null && size == 0) {
            em.setProperty("state", "0");
            if (em.getProperty("leader") != null) {
                em.setProperty("leader", "true");
            }
        }
        if (this.speedRunStart > 0L && this.speedRunLeader.equalsIgnoreCase(chr)) {
            if (size > 0) {
                this.broadcastMessage(MaplePacketCreator.serverNotice(5, "由於遠征队队長離開了，所以遠征队任務失敗。"));
            }
            this.endSpeedRun();
        }
    }
    
    public void setNodes(final MapleNodes mn) {
        this.nodes = mn;
    }
    
    public List<MaplePlatform> getPlatforms() {
        return this.nodes.getPlatforms();
    }
    
    public Collection<MapleNodeInfo> getNodes() {
        return this.nodes.getNodes();
    }
    
    public MapleNodeInfo getNode(int index) {
        return this.nodes.getNode(index);
    }
    
    public List<Rectangle> getAreas() {
        return this.nodes.getAreas();
    }
    
    public Rectangle getArea(int index) {
        return this.nodes.getArea(index);
    }
    
    public void changeEnvironment(final String ms, int type) {
        this.broadcastMessage(MaplePacketCreator.environmentChange(ms, type));
    }
    
    public void toggleEnvironment(final String ms) {
        if (this.environment.containsKey((Object)ms)) {
            this.moveEnvironment(ms, ((int)Integer.valueOf(this.environment.get((Object)ms)) == 1) ? 2 : 1);
        }
        else {
            this.moveEnvironment(ms, 1);
        }
    }
    
    public void moveEnvironment(final String ms, int type) {
        this.broadcastMessage(MaplePacketCreator.environmentMove(ms, type));
        this.environment.put(ms, Integer.valueOf(type));
    }
    
    public Map<String, Integer> getEnvironment() {
        return this.environment;
    }
    
    public int getNumPlayersInArea(int index) {
        int ret = 0;
        this.charactersLock.readLock().lock();
        try {
            final Iterator<MapleCharacter> ltr = this.characters.iterator();
            while (ltr.hasNext()) {
                if (this.getArea(index).contains(((MapleCharacter)ltr.next()).getPosition())) {
                    ++ret;
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
        return ret;
    }
    
    public void broadcastGMMessage(final MapleCharacter source, final byte[] packet, final boolean repeatToSource) {
        this.broadcastGMMessage(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, (source == null) ? new Point(0, 0) : source.getPosition(), (source == null) ? 1 : source.getGMLevel());
    }
    
    private void broadcastGMMessage(final MapleCharacter source, final byte[] packet, final double rangeSq, final Point rangedFrom, int lowestLevel) {
        this.charactersLock.readLock().lock();
        try {
            for ( MapleCharacter chr : this.characters) {
                if (chr != source && chr.getGMLevel() >= lowestLevel) {
                    chr.getClient().sendPacket(packet);
                }
            }
        }
        finally {
            this.charactersLock.readLock().unlock();
        }
    }
    
    public void Killdpm(final boolean animate) {
        final List<MapleMapObject> monsters = this.getMapObjectsInRange(new Point(0, 0), Double.POSITIVE_INFINITY, Arrays.asList(MapleMapObjectType.MONSTER));
        for ( MapleMapObject monstermo : monsters) {
            final MapleMonster monster = (MapleMonster)monstermo;
            if (monster.getId() == 9001007) {
                this.spawnedMonstersOnMap.decrementAndGet();
                monster.setHp(0L);
                this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), (int)(animate ? 1 : 0)));
                this.removeMapObject((MapleMapObject)monster);
                monster.killed();
            }
        }
    }
    
    public List<Pair<Integer, Integer>> getMobsToSpawn() {
        return this.nodes.getMobsToSpawn();
    }
    
    public List<Integer> getSkillIds() {
        return this.nodes.getSkillIds();
    }
    
    public boolean canSpawn() {
    //刷新怪物的时间
//        createMobInterval = (short) (MobConstants.isSpawnSpeed(this) ? 0 : LtMS.ConfigValuesMap.get("地图刷新频率")); // 轮回有輪迴時怪物重生時間間隔為 0 轮回石碑
//        return lastSpawnTime > 0 && isSpawns && lastSpawnTime + createMobInterval < System.currentTimeMillis();
        if (!canSpawnForCPU) {
            return false;
        } else if (!this.hasSpawned && this.hasCheckIn) {
            this.hasSpawned = true;
            return this.lastSpawnTime > 0L && this.isSpawns;
        } else if (this.haveStone) {
            if (this.getStoneLevel() == 1) {
                return this.lastSpawnTime > 0L && this.isSpawns && this.lastSpawnTime + (long)(this.createMobInterval / 6) < System.currentTimeMillis();
            } else if (this.getStoneLevel() >= 2) {
                return this.lastSpawnTime > 0L && this.isSpawns && this.lastSpawnTime + (long)(this.createMobInterval / 10) < System.currentTimeMillis();
            } else {
                return this.lastSpawnTime > 0L && this.isSpawns && this.lastSpawnTime + (long)(this.createMobInterval / 3) < System.currentTimeMillis();
            }
        } else {
            return this.lastSpawnTime > 0L && this.isSpawns && this.lastSpawnTime + (long)this.createMobInterval < System.currentTimeMillis();
        }
    // return true;
    }
    
    public boolean canHurt() {
        if (this.lastHurtTime > 0L && this.lastHurtTime + (long)this.decHPInterval < System.currentTimeMillis()) {
            this.lastHurtTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    
    public short getTop() {
        return this.top;
    }
    
    public short getBottom() {
        return this.bottom;
    }
    
    public short getLeft() {
        return this.left;
    }
    
    public short getRight() {
        return this.right;
    }
    
    public void setTop(int ii) {
        this.top = (short)ii;
    }
    
    public void setBottom(int ii) {
        this.bottom = (short)ii;
    }
    
    public void setLeft(int ii) {
        this.left = (short)ii;
    }
    
    public void setRight(int ii) {
        this.right = (short)ii;
    }
    
    public void 清怪() {
        this.killAllMonsters(true);
    }
    
    public void removePlayer2(MapleCharacter chr) {
        if (this.everlast) {
            this.returnEverLastItem(chr);
        }
        this.charactersLock.writeLock().lock();
        try {
            this.characters.remove((Object)chr);
        }
        finally {
            this.charactersLock.writeLock().unlock();
        }
        int nowChannel = chr.getClient().getChannelServer().getChannel();
        chr.getClient().getChannelServer();
        ChannelServer.clones.add(new 离线人偶(chr.getClient().getAccID(), chr.getId(), chr.getPosition().x, chr.getPosition().y, (chr.getChair() > 0) ? chr.getChair() : 0, nowChannel));
        this.removeMapObject((MapleMapObject)chr);
        chr.checkFollow();
        this.broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        if (!chr.isClone()) {
            final List<MapleMonster> update = new ArrayList<MapleMonster>();
            final Iterator<MapleMonster> controlled = chr.getControlled().iterator();
            while (controlled.hasNext()) {
                final MapleMonster monster = (MapleMonster)controlled.next();
                if (monster != null) {
                    monster.setController(null);
                    monster.setControllerHasAggro(false);
                    monster.setControllerKnowsAboutAggro(false);
                    controlled.remove();
                    update.add(monster);
                }
            }
            for ( MapleMonster mons : update) {
                this.updateMonsterController(mons);
            }
            chr.leaveMap();
            this.checkStates(chr.getName());
            if (this.mapid == 109020001) {
                chr.canTalk(true);
            }
            for ( WeakReference<MapleCharacter> chrz : chr.getClones()) {
                if (chrz.get() != null) {
                    this.removePlayer((MapleCharacter)chrz.get());
                }
            }
        }
        chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        boolean cancelSummons = false;
        for ( MapleSummon summon : chr.getSummons().values()) {
            if (summon.getMovementType() == SummonMovementType.STATIONARY || summon.getMovementType() == SummonMovementType.CIRCLE_STATIONARY || summon.getMovementType() == SummonMovementType.WALK_STATIONARY) {
                cancelSummons = true;
            }
            else {
                summon.setChangedMap(true);
                this.removeMapObject((MapleMapObject)summon);
            }
        }
        if (cancelSummons) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        }
    }
    
    public void removePlayer3(MapleCharacter chr) {
        this.removeMapObject((MapleMapObject)chr);
        this.broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
    }
    
    public void removePlayer(MapleCharacter chr) {
        if (this.everlast) {
            this.returnEverLastItem(chr);
        }
        this.charactersLock.writeLock().lock();
        try {
            this.characters.remove((Object)chr);
        }
        catch (Exception ex) {
            System.err.println("移除CHR失敗" + (Object)ex);
            FileoutputUtil.outputFileError("logs/移除CHR失敗.txt", (Throwable)ex);
        }
        finally {
            this.charactersLock.writeLock().unlock();
        }
        this.removeMapObject((MapleMapObject)chr);
        chr.checkFollow();
        if (chr.getMapId() == 220080001 && chr.getMap().playerCount() <= 0) {
            final MapleMap map = chr.getClient().getChannelServer().getMapFactory().getMap(220080000);
            map.EndPapfight();
            map.resetReactors();
        }
        this.broadcastMessage(MaplePacketCreator.removePlayerFromMap(chr.getId()));
        if (!chr.isClone()) {
            chr.leaveMap();
            this.checkStates(chr.getName());
            if (this.mapid == 109020001) {
                chr.canTalk(true);
            }
            for ( WeakReference<MapleCharacter> chrz : chr.getClones()) {
                if (chrz.get() != null) {
                    this.removePlayer((MapleCharacter)chrz.get());
                }
            }
        }
        chr.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        boolean cancelSummons = false;
        for ( MapleSummon summon : chr.getSummons().values()) {
            if (summon.getMovementType() == SummonMovementType.STATIONARY || summon.getMovementType() == SummonMovementType.CIRCLE_STATIONARY || summon.getMovementType() == SummonMovementType.WALK_STATIONARY) {
                cancelSummons = true;
            }
            else {
                summon.setChangedMap(true);
                this.removeMapObject((MapleMapObject)summon);
            }
        }
        if (cancelSummons) {
            chr.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        }
    }

    
    static {
        //PointsGained = new HashMap<Integer, HashMap<String, Integer>>();
        MapleMap.特殊宠物吸取开关 = Boolean.parseBoolean(ServerProperties.getProperty("LtMS.特殊宠物吸取开关"));
        MapleMap.特殊宠物吸物开关 = Boolean.parseBoolean(ServerProperties.getProperty("LtMS.特殊宠物吸物开关"));
        MapleMap.特殊宠物吸金开关 = Boolean.parseBoolean(ServerProperties.getProperty("LtMS.特殊宠物吸金开关"));
        MapleMap.特殊宠物吸物无法使用地图开关 = Boolean.parseBoolean(ServerProperties.getProperty("LtMS.特殊宠物吸物无法使用地图开关"));
        MapleMap.特殊宠物吸物无法使用地图 = ServerProperties.getProperty("LtMS.特殊宠物吸物无法使用地图").split(",");
        MapleMap.持有物道具 = (int)Integer.valueOf(ServerProperties.getProperty("LtMS.持有物道具"));
    }
    
    private class ActivateItemReactor implements Runnable
    {
        private MapleMapItem mapitem;
        private MapleReactor reactor;
        private MapleClient c;
        
        public ActivateItemReactor(final MapleMapItem mapitem, final MapleReactor reactor, final MapleClient c) {
            this.mapitem = mapitem;
            this.reactor = reactor;
            this.c = c;
        }
        
        @Override
        public void run() {
            if (this.mapitem != null && this.mapitem == MapleMap.this.getMapObject(this.mapitem.getObjectId(), this.mapitem.getType())) {
                if (this.mapitem.isPickedUp()) {
                    this.reactor.setTimerActive(false);
                    return;
                }
                this.mapitem.expire(MapleMap.this);
                this.reactor.hitReactor(this.c);
                this.reactor.setTimerActive(false);
                if (this.reactor.getDelay() > 0) {
                    MapTimer.getInstance().schedule((Runnable)new Runnable() {
                        @Override
                        public void run() {
                            reactor.forceHitReactor((byte)0);
                        }
                    }, (long)this.reactor.getDelay());
                }
            }
            else {
                this.reactor.setTimerActive(false);
            }
        }
        

    }
    
    private interface SpawnCondition
    {
        boolean canSpawn(final MapleCharacter p0);
    }
    
    private interface DelayedPacketCreation
    {
        void sendPackets(final MapleClient p0);
    }
    public boolean haveMonster(int monsterId) {
        for (MapleMonster mapleMonster : this.getAllMonstersThreadsafe()) {
            if (mapleMonster.getId() == monsterId) {
                return true;
            }
        }
        return false;
    }
    private Point respawnPoint;
    private List<LifeMovementFragment> respawnOnePointMoves = new ArrayList();
    public boolean isRespawnOnePoint() {
        return this.respawnOnePoint;
    }
    public List<LifeMovementFragment> getRespawnOnePointMoves() {
        return this.respawnOnePointMoves;
    }

    public void setRespawnOnePointMoves(List<LifeMovementFragment> moves) {
        if (moves != null) {
            this.respawnOnePointMoves.clear();
            this.respawnOnePointMoves = new ArrayList(moves);
        }

    }
    public void 给时钟(final int minutes, boolean refresh, final boolean kickOut) {
        if (refresh) {
            this.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
            this.clock_s = true;
            this.clock_s_startTime = Calendar.getInstance().getTimeInMillis();
            this.clock_s_duration = minutes * 60 * 1000;
        } else {
            if (this.clock_s) {
                long exist_time = this.获得剩余时钟时间();
                if (exist_time != 0L) {
                    this.broadcastMessage(MaplePacketCreator.getClock((int)(exist_time / 1000L)));
                }

                return;
            }

            this.broadcastMessage(MaplePacketCreator.getClock(minutes * 60));
            this.clock_s = true;
            this.clock_s_startTime = Calendar.getInstance().getTimeInMillis();
            this.clock_s_duration = minutes * 60 * 1000;
        }

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                try {
                    MapleMap.this.刷新时钟倒计时线程();
                    Thread cThread = Thread.currentThread();
                    MapleMap.this.clock_s_thread_map.put(cThread, true);
                    Thread.sleep((long)(minutes * 1000 * 60));
                    if (kickOut && (Boolean)MapleMap.this.clock_s_thread_map.get(cThread)) {
                        Iterator var2 = MapleMap.this.getCharacters().iterator();

                        while(var2.hasNext()) {
                            MapleCharacter chr = (MapleCharacter)var2.next();
                            chr.changeMap(MapleMap.this.returnMapId);
                        }
                    }

                    MapleMap.this.broadcastMessage(MaplePacketCreator.stopClock());
                    MapleMap.this.clock_s = false;
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                    服务端输出信息.println_err("执行 给时钟 错误，错误原因： " + var4);
                }

            }
        });
    }

    public long 获得剩余时钟时间() {
        int exsit_time = 0;
        if (this.clock_s) {
            exsit_time = this.clock_s_duration - (int)(Calendar.getInstance().getTimeInMillis() - this.clock_s_startTime);
        }

        return (long)exsit_time;
    }

    private void 刷新时钟倒计时线程() {
        if (!this.clock_s_thread_map.isEmpty()) {
            Iterator<Map.Entry<Thread, Boolean>> iterator = this.clock_s_thread_map.entrySet().iterator();

            while(iterator.hasNext()) {
                Map.Entry<Thread, Boolean> entry = (Map.Entry)iterator.next();
                if (!((Thread)entry.getKey()).isAlive()) {
                    this.clock_s_thread_map.remove(entry.getKey(), entry.getValue());
                    iterator = this.clock_s_thread_map.entrySet().iterator();
                } else {
                    entry.setValue(false);
                }
            }

        }
    }
    public void 给时钟2(final int seconds, boolean refresh, final boolean kickOut) {
        if (refresh) {
            this.broadcastMessage(MaplePacketCreator.getClock(seconds));
            this.clock_s = true;
            this.clock_s_startTime = Calendar.getInstance().getTimeInMillis();
            this.clock_s_duration = seconds * 1000;
        } else {
            if (this.clock_s) {
                long exist_time = this.获得剩余时钟时间();
                if (exist_time != 0L) {
                    this.broadcastMessage(MaplePacketCreator.getClock((int)(exist_time / 1000L)));
                }

                return;
            }

            this.broadcastMessage(MaplePacketCreator.getClock(seconds));
            this.clock_s = true;
            this.clock_s_startTime = Calendar.getInstance().getTimeInMillis();
            this.clock_s_duration = seconds * 1000;
        }

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            public void run() {
                try {
                    MapleMap.this.刷新时钟倒计时线程();
                    Thread cThread = Thread.currentThread();
                    MapleMap.this.clock_s_thread_map.put(cThread, true);

                    for(int i = 0; i < seconds; ++i) {
                        Thread.sleep(1000L);
                    }

                    if (kickOut && (Boolean)MapleMap.this.clock_s_thread_map.get(cThread)) {
                        for (MapleCharacter chr : MapleMap.this.getCharacters()) {
                            chr.changeMap(MapleMap.this.returnMapId);
                        }

                    }

                    MapleMap.this.broadcastMessage(MaplePacketCreator.stopClock());
                    MapleMap.this.clock_s = false;
                } catch (InterruptedException var4) {
                    var4.printStackTrace();
                    服务端输出信息.println_err("执行 给时钟 错误，错误原因： " + var4);
                }

            }
        });
    }
    public final void broadcastMessageSkill(MapleCharacter source, byte[] packet, boolean repeatToSource) {
        this.broadcastMessageSkill(repeatToSource ? null : source, packet, Double.POSITIVE_INFINITY, source.getPosition());
    }
    private void broadcastMessageSkill(MapleCharacter source, byte[] packet, double rangeSq, Point rangedFrom) {
        this.charactersLock.readLock().lock();

        try {
            for (MapleCharacter chr : this.characters) {
                if (chr != source && chr.isShowSkill()) {
                    if (rangeSq < Double.POSITIVE_INFINITY) {
                        if (rangedFrom.distanceSq(chr.getPosition()) <= rangeSq) {
                            chr.getClient().sendPacket(packet);
                        }
                    } else {
                        chr.getClient().sendPacket(packet);
                    }
                }
            }

        } finally {
            this.charactersLock.readLock().unlock();
        }

    }
    public final void killMonsterAll2(int monsId) {
        for (MapleMonster monster : this.getAllMonstersThreadsafe()) {
            if (monster.getId() == monsId) {
                this.spawnedMonstersOnMap.decrementAndGet();
                monster.setHp(0L);
                this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), 1));
                this.removeMapObject(monster);
                monster.killed();
            }
        }

    }
    public final void killMonster(MapleMonster monster, boolean animate) {
        this.spawnedMonstersOnMap.decrementAndGet();
        monster.setHp(0L);
        this.broadcastMessage(MobPacket.killMonster(monster.getObjectId(), animate ? 1 : 0));
        this.removeMapObject(monster);
        monster.killed();
    }


    public ArrayList<Integer> getOwnerList() {
        return this.ownerList;
    }

    public void clearOwnerList() {
        this.ownerList.clear();
    }

    public void addOwner(int chrId) {
        if (!this.ownerList.contains(chrId)) {
            this.ownerList.add(chrId);
        }

    }
    public boolean removeOwner(int chrId) {
        if (this.ownerList.contains(chrId)) {
            this.ownerList.remove(chrId);
            return true;
        } else {
            return false;
        }
    }
    public void setOwnerList(ArrayList<Integer> ownerList) {
        this.ownerList.clear();
        this.ownerList = new ArrayList(ownerList);
    }

    public boolean isOwner(int chrId) {
        return this.ownerList.contains(chrId);
    }

    public void reLoadMonsterSpawn() {
        this.monsterSpawn.clear();
        MapleDataProvider source = MapleDataProviderFactory.getDataProvider("Map.wz");
        MapleData mapData = source.getData(MapleMapFactory.getMapPath(this.mapid));
        int bossid = -1;
        String msg = null;
        if (mapData.getChildByPath("info/timeMob") != null) {
            bossid = MapleDataTool.getInt(mapData.getChildByPath("info/timeMob/id"), 0);
            msg = MapleDataTool.getString(mapData.getChildByPath("info/timeMob/message"), (String)null);
        }

        Iterator var7 = mapData.getChildByPath("life").iterator();

        while(var7.hasNext()) {
            MapleData life = (MapleData)var7.next();
            String type = MapleDataTool.getString(life.getChildByPath("type"));
            if (!type.equals("n")) {
                AbstractLoadedMapleLife myLife = MapleMapFactory.reLoadLife(life, MapleDataTool.getString(life.getChildByPath("id")), type);
                if (myLife instanceof MapleMonster) {
                    MapleMonster mob = (MapleMonster)myLife;
                    this.addMonsterSpawn(mob, MapleDataTool.getInt("mobTime", life, 0), (byte)MapleDataTool.getInt("team", life, -1), mob.getId() == bossid ? msg : null);
                }
            }
        }

    }
}
