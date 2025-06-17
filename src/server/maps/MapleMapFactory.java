package server.maps;

import java.awt.Point;
import java.awt.Rectangle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import bean.LtMobSpawnBoss;
import database.DBConPool;
import provider.MapleData;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.PortalFactory;
import server.Start;
import server.life.AbstractLoadedMapleLife;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleNPC;
import server.maps.MapleNodes.MapleNodeInfo;
import server.maps.MapleNodes.MaplePlatform;
import tools.FileoutputUtil;
import tools.StringUtil;

public class MapleMapFactory
{
    private static final MapleDataProvider source;
    private static final MapleData nameData;
    private final Map<Integer, MapleMap> maps;
    private final Map<Integer, MapleMap> instanceMap;
    private static final Map<Integer, MapleNodes> mapInfos;
    private final ReentrantLock lock;
    private static final Map<Integer, List<AbstractLoadedMapleLife>> customLife;
    private static final Map<Integer, List<AbstractLoadedMapleLife>> RemovecustomLife;
    private final Map<Integer, Integer> DeStorymaps;
    private int channel;
    private int 怪物刷新时间;
    private static boolean changed;
    private static Map<Integer, ArrayList<Integer>> mobInMapId = new ConcurrentHashMap();
    private static Map<Integer, String> mapnames = new ConcurrentHashMap();

    public Map<Integer, MapleMap> getMaps() {
        return maps;
    }

    private String getMapRealName(int mapid) {
        String mapRealName = (String)mapnames.get(mapid);
        return mapRealName;
    }

    public static String getMapFullName(int mapid) {
        String mapRealName = (String)mapnames.get(mapid);
        return mapRealName;
    }
    public MapleMapFactory() {
        this.maps = new ConcurrentHashMap<>();
        this.instanceMap = new ConcurrentHashMap<Integer, MapleMap>();
        this.lock = new ReentrantLock(true);
        this.DeStorymaps = new ConcurrentHashMap<Integer, Integer>();
        this.怪物刷新时间 = 1;
    }
    public static boolean addMobInMapId(int mobId, int mapId) {
        if (mapId <= 0) {
            return false;
        } else if (mobInMapId.containsKey(mobId)) {
            if (((ArrayList)mobInMapId.get(mobId)).contains(mapId)) {
                return false;
            } else {
                ((ArrayList)mobInMapId.get(mobId)).add(mapId);
                return true;
            }
        } else {
            ArrayList<Integer> mapIdList = new ArrayList();
            mapIdList.add(mapId);
            mobInMapId.put(mobId, mapIdList);
            return true;
        }
    }
    public static void clearMobInMapId() {
        mobInMapId.clear();
    }

    public static Map<Integer, ArrayList<Integer>> getMobInMapIdMap() {
        return mobInMapId;
    }

    public static ArrayList<Integer> getMobInMapIdList(int mobId) {
        return (ArrayList)mobInMapId.get(mobId);
    }
    public final MapleMap getMap(final int mapid) {
        return this.getMap(mapid, true, true, true);
    }
    
    public final MapleMap getMap(final int mapid, final boolean respawns, final boolean npcs) {
        return this.getMap(mapid, respawns, npcs, true);
    }
    public static String getMapPath(int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        StringBuilder builder = new StringBuilder("Map/Map");
        builder.append(mapid / 100000000);
        builder.append("/");
        builder.append(mapName);
        builder.append(".img");
        mapName = builder.toString();
        return mapName;
    }

    public static AbstractLoadedMapleLife reLoadLife(MapleData life, String id, String type) {
        if (id.equals("")) {
            return null;
        } else {
            AbstractLoadedMapleLife myLife = null;
            try {
                myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type);
            } catch (NumberFormatException e) {
                myLife = MapleLifeFactory.getLife(910000000, type);
            }
            if (myLife == null) {
                return null;
            } else {
                myLife.setCy(MapleDataTool.getInt(life.getChildByPath("cy")));
                MapleData dF = life.getChildByPath("f");
                if (dF != null) {
                    myLife.setF(MapleDataTool.getInt(dF));
                }

                myLife.setFh(MapleDataTool.getInt(life.getChildByPath("fh")));
                myLife.setRx0(MapleDataTool.getInt(life.getChildByPath("rx0")));
                myLife.setRx1(MapleDataTool.getInt(life.getChildByPath("rx1")));
                myLife.setPosition(new Point(MapleDataTool.getInt(life.getChildByPath("x")), MapleDataTool.getInt(life.getChildByPath("y"))));
                if (MapleDataTool.getInt("hide", life, 0) == 1 && myLife instanceof MapleNPC) {
                    myLife.setHide(true);
                }

                return myLife;
            }
        }
    }
    public final MapleMap getMap( int mapid, final boolean respawns, final boolean npcs, final boolean reactors) {
        if (mapid == 98){
            mapid = 910000000;
        }
        final Integer omapid = mapid;
        MapleMap map = (MapleMap)this.maps.get((Object)omapid);
        if (map == null) {
            this.lock.lock();
            try {
                if (this.DeStorymaps.get((Object)omapid) != null) {
                    return null;
                }
                map = (MapleMap)this.maps.get((Object)omapid);
                if (map != null) {
                    return map;
                }
                MapleData mapData = MapleMapFactory.source.getData(this.getMapName(mapid));
                final MapleData link = mapData.getChildByPath("info/link");
                if (link != null) {
                    mapData = MapleMapFactory.source.getData(this.getMapName(MapleDataTool.getIntConvert("info/link", mapData)));
                }
                float monsterRate = 0.0f;
                if (respawns) {
                    final MapleData mobRate = mapData.getChildByPath("info/mobRate");
                    if (mobRate != null) {
                        monsterRate = (float)(Float)mobRate.getData();
                    }
                }
                map = new MapleMap(mapid, this.channel, MapleDataTool.getInt("info/returnMap", mapData), monsterRate);
                final PortalFactory portalFactory = new PortalFactory();
                for (final MapleData portal : mapData.getChildByPath("portal")) {
                    map.addPortal(portalFactory.makePortal(MapleDataTool.getInt(portal.getChildByPath("pt")), portal));
                }
                map.setTop(MapleDataTool.getInt(mapData.getChildByPath("info/VRTop"), 0));
                map.setLeft(MapleDataTool.getInt(mapData.getChildByPath("info/VRLeft"), 0));
                map.setBottom(MapleDataTool.getInt(mapData.getChildByPath("info/VRBottom"), 0));
                map.setRight(MapleDataTool.getInt(mapData.getChildByPath("info/VRRight"), 0));
                final List<MapleFoothold> allFootholds = new LinkedList<MapleFoothold>();
                final Point lBound = new Point();
                final Point uBound = new Point();
                for (final MapleData footRoot : mapData.getChildByPath("foothold")) {
                    for (final MapleData footCat : footRoot) {
                        for (final MapleData footHold : footCat) {
                            final MapleFoothold fh = new MapleFoothold(new Point(MapleDataTool.getInt(footHold.getChildByPath("x1")), MapleDataTool.getInt(footHold.getChildByPath("y1"))), new Point(MapleDataTool.getInt(footHold.getChildByPath("x2")), MapleDataTool.getInt(footHold.getChildByPath("y2"))), Integer.parseInt(footHold.getName()));
                            fh.setPrev((short)MapleDataTool.getInt(footHold.getChildByPath("prev")));
                            fh.setNext((short)MapleDataTool.getInt(footHold.getChildByPath("next")));
                            if (fh.getX1() < lBound.x) {
                                lBound.x = fh.getX1();
                            }
                            if (fh.getX2() > uBound.x) {
                                uBound.x = fh.getX2();
                            }
                            if (fh.getY1() < lBound.y) {
                                lBound.y = fh.getY1();
                            }
                            if (fh.getY2() > uBound.y) {
                                uBound.y = fh.getY2();
                            }
                            allFootholds.add(fh);
                        }
                    }
                }
                final MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
                for (final MapleFoothold foothold : allFootholds) {
                    fTree.insert(foothold);
                }
                map.setFootholds(fTree);
                if (map.getTop() == 0) {
                    map.setTop(lBound.y);
                }
                if (map.getBottom() == 0) {
                    map.setBottom(uBound.y);
                }
                if (map.getLeft() == 0) {
                    map.setLeft(lBound.x);
                }
                if (map.getRight() == 0) {
                    map.setRight(uBound.x);
                }
                int bossid = -1;
                String msg = null;
                if (mapData.getChildByPath("info/timeMob") != null) {
                    bossid = MapleDataTool.getInt(mapData.getChildByPath("info/timeMob/id"), 0);
                    msg = MapleDataTool.getString(mapData.getChildByPath("info/timeMob/message"), null);
                }
                for (final MapleData life : mapData.getChildByPath("life")) {
                    final String type = MapleDataTool.getString(life.getChildByPath("type"));
                    if (npcs || !type.equals((Object)"n")) {
                        final AbstractLoadedMapleLife myLife = this.loadLife(life, MapleDataTool.getString(life.getChildByPath("id")), type);
                        if (myLife instanceof MapleMonster) {
                            final MapleMonster mob = (MapleMonster)myLife;
                            map.addMonsterSpawn(mob, MapleDataTool.getInt("mobTime", life, 0), (byte)MapleDataTool.getInt("team", life, -1), (mob.getId() == bossid) ? msg : null);
                            if (map.getId() > 0) {
                                addMobInMapId(mob.getId(), map.getId());
                            }
                        }
                        else {
                            if (myLife == null) {
                                continue;
                            }
                            map.addMapObject((MapleMapObject)myLife);
                        }
                    }
                }
                final List<AbstractLoadedMapleLife> custom = (List<AbstractLoadedMapleLife>)MapleMapFactory.customLife.get((Object)Integer.valueOf(mapid));
                if (custom != null) {
                    for (final AbstractLoadedMapleLife n : custom) {
                        final String cType = n.getCType();
                        int n3 = -1;
                        switch (cType.hashCode()) {
                            case 110: {
                                if (cType.equals((Object)"n")) {
                                    n3 = 0;
                                    break;
                                }
                                break;
                            }
                        }
                        switch (n3) {
                            case 0: {
                                map.addMapObject((MapleMapObject)n);
                                continue;
                            }
                        }
                    }
                }
                this.addAreaBossSpawn(map);
                map.setCreateMobInterval((short)MapleDataTool.getInt(mapData.getChildByPath("info/createMobInterval"), this.怪物刷新时间 * 1));
                map.loadMonsterRate(true);
                map.setNodes(this.loadNodes(mapid, mapData));
                if (reactors && mapData.getChildByPath("reactor") != null) {
                    for (final MapleData reactor : mapData.getChildByPath("reactor")) {
                        final String id = MapleDataTool.getString(reactor.getChildByPath("id"));
                        if (id != null) {
                            map.spawnReactor(this.loadReactor(reactor, id, (byte)MapleDataTool.getInt(reactor.getChildByPath("f"), 0)));
                        }
                    }
                }
                try {
                    map.setMapName(MapleDataTool.getString("mapName", MapleMapFactory.nameData.getChildByPath(this.getMapStringName((int)omapid)), ""));
                    map.setStreetName(MapleDataTool.getString("streetName", MapleMapFactory.nameData.getChildByPath(this.getMapStringName((int)omapid)), ""));
                }
                catch (Exception e) {
                    map.setMapName("");
                    map.setStreetName("");
                }
                map.setClock(mapData.getChildByPath("clock") != null);
                map.setEverlast(MapleDataTool.getInt(mapData.getChildByPath("info/everlast"), 0) > 0);
                map.setTown(MapleDataTool.getInt(mapData.getChildByPath("info/town"), 0) > 0);
                map.setSoaring(MapleDataTool.getInt(mapData.getChildByPath("info/needSkillForFly"), 0) > 0);
                map.setPersonalShop(MapleDataTool.getInt(mapData.getChildByPath("info/personalShop"), 0) > 0);
                map.setForceMove(MapleDataTool.getInt(mapData.getChildByPath("info/lvForceMove"), 0));
                map.setHPDec(MapleDataTool.getInt(mapData.getChildByPath("info/decHP"), 0));
                map.setHPDecInterval(MapleDataTool.getInt(mapData.getChildByPath("info/decHPInterval"), 10000));
                map.setHPDecProtect(MapleDataTool.getInt(mapData.getChildByPath("info/protectItem"), 0));
                map.setBoat(mapData.getChildByPath("shipObj") != null);
                map.setForcedReturnMap(MapleDataTool.getInt(mapData.getChildByPath("info/forcedReturn"), 999999999));
                map.setTimeLimit(MapleDataTool.getInt(mapData.getChildByPath("info/timeLimit"), -1));
                map.setFieldLimit(MapleDataTool.getInt(mapData.getChildByPath("info/fieldLimit"), 0));
                map.setFirstUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onFirstUserEnter"), ""));
                map.setUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onUserEnter"), ""));
                map.setRecoveryRate(MapleDataTool.getFloat(mapData.getChildByPath("info/recovery"), 1.0f));
                map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
                map.setConsumeItemCoolTime(MapleDataTool.getInt(mapData.getChildByPath("info/consumeItemCoolTime"), 0));
                this.maps.put(omapid, map);
            }
            finally {
                this.lock.unlock();
            }
        }
        else if (MapleMapFactory.changed) {
            final List<AbstractLoadedMapleLife> custom2 = (List<AbstractLoadedMapleLife>)MapleMapFactory.customLife.get((Object)Integer.valueOf(mapid));
            if (custom2 != null) {
                for (final AbstractLoadedMapleLife n2 : custom2) {
                    final String cType2 = n2.getCType();
                    int n4 = -1;
                    switch (cType2.hashCode()) {
                        case 110: {
                            if (cType2.equals((Object)"n")) {
                                n4 = 0;
                                break;
                            }
                            break;
                        }
                    }
                    switch (n4) {
                        case 0: {
                            map.addMapObject((MapleMapObject)n2);
                            continue;
                        }
                    }
                }
            }
        }
        return map;
    }
    
    public MapleMap getInstanceMap(final int instanceid) {
        return (MapleMap)this.instanceMap.get((Object)Integer.valueOf(instanceid));
    }
    
    public void removeInstanceMap(final int instanceid) {
        if (this.isInstanceMapLoaded(instanceid)) {
            this.getInstanceMap(instanceid).checkStates("");
            this.instanceMap.remove((Object)Integer.valueOf(instanceid));
        }
    }
    
    public void removeMap(final int instanceid) {
        if (this.isMapLoaded(instanceid)) {
            this.getMap(instanceid).checkStates("");
            this.maps.remove((Object)Integer.valueOf(instanceid));
        }
    }
    
    public MapleMap CreateInstanceMap(final int mapid, final boolean respawns, final boolean npcs, final boolean reactors, final int instanceid) {
        if (this.isInstanceMapLoaded(instanceid)) {
            return this.getInstanceMap(instanceid);
        }
        MapleData mapData = MapleMapFactory.source.getData(this.getMapName(mapid));
        final MapleData link = mapData.getChildByPath("info/link");
        if (link != null) {
            mapData = MapleMapFactory.source.getData(this.getMapName(MapleDataTool.getIntConvert("info/link", mapData)));
        }
        float monsterRate = 0.0f;
        if (respawns) {
            final MapleData mobRate = mapData.getChildByPath("info/mobRate");
            if (mobRate != null) {
                monsterRate = (float)(Float)mobRate.getData();
            }
        }
        final MapleMap map = new MapleMap(mapid, this.channel, MapleDataTool.getInt("info/returnMap", mapData), monsterRate);
        final PortalFactory portalFactory = new PortalFactory();
        for (final MapleData portal : mapData.getChildByPath("portal")) {
            map.addPortal(portalFactory.makePortal(MapleDataTool.getInt(portal.getChildByPath("pt")), portal));
        }
        map.setTop(MapleDataTool.getInt(mapData.getChildByPath("info/VRTop"), 0));
        map.setLeft(MapleDataTool.getInt(mapData.getChildByPath("info/VRLeft"), 0));
        map.setBottom(MapleDataTool.getInt(mapData.getChildByPath("info/VRBottom"), 0));
        map.setRight(MapleDataTool.getInt(mapData.getChildByPath("info/VRRight"), 0));
        final List<MapleFoothold> allFootholds = new LinkedList<MapleFoothold>();
        final Point lBound = new Point();
        final Point uBound = new Point();
        for (final MapleData footRoot : mapData.getChildByPath("foothold")) {
            for (final MapleData footCat : footRoot) {
                for (final MapleData footHold : footCat) {
                    final MapleFoothold fh = new MapleFoothold(new Point(MapleDataTool.getInt(footHold.getChildByPath("x1")), MapleDataTool.getInt(footHold.getChildByPath("y1"))), new Point(MapleDataTool.getInt(footHold.getChildByPath("x2")), MapleDataTool.getInt(footHold.getChildByPath("y2"))), Integer.parseInt(footHold.getName()));
                    fh.setPrev((short)MapleDataTool.getInt(footHold.getChildByPath("prev")));
                    fh.setNext((short)MapleDataTool.getInt(footHold.getChildByPath("next")));
                    if (fh.getX1() < lBound.x) {
                        lBound.x = fh.getX1();
                    }
                    if (fh.getX2() > uBound.x) {
                        uBound.x = fh.getX2();
                    }
                    if (fh.getY1() < lBound.y) {
                        lBound.y = fh.getY1();
                    }
                    if (fh.getY2() > uBound.y) {
                        uBound.y = fh.getY2();
                    }
                    allFootholds.add(fh);
                }
            }
        }
        final MapleFootholdTree fTree = new MapleFootholdTree(lBound, uBound);
        for (final MapleFoothold fh2 : allFootholds) {
            fTree.insert(fh2);
        }
        map.setFootholds(fTree);
        int bossid = -1;
        String msg = null;
        if (mapData.getChildByPath("info/timeMob") != null) {
            bossid = MapleDataTool.getInt(mapData.getChildByPath("info/timeMob/id"), 0);
            msg = MapleDataTool.getString(mapData.getChildByPath("info/timeMob/message"), null);
        }
        for (final MapleData life : mapData.getChildByPath("life")) {
            final String type = MapleDataTool.getString(life.getChildByPath("type"));
            if (npcs || !type.equals((Object)"n")) {
                final AbstractLoadedMapleLife myLife = this.loadLife(life, MapleDataTool.getString(life.getChildByPath("id")), type);
                if (myLife instanceof MapleMonster) {
                    final MapleMonster mob = (MapleMonster)myLife;
                    map.addMonsterSpawn(mob, MapleDataTool.getInt("mobTime", life, 0), (byte)MapleDataTool.getInt("team", life, -1), (mob.getId() == bossid) ? msg : null);
                    if (map.getId() > 0) {
                        addMobInMapId(mob.getId(), map.getId());
                    }
                }
                else {
                    map.addMapObject((MapleMapObject)myLife);
                }
            }
        }
        this.addAreaBossSpawn(map);
        map.setCreateMobInterval((short)MapleDataTool.getInt(mapData.getChildByPath("info/createMobInterval"), this.怪物刷新时间 * 1));
        map.loadMonsterRate(true);
        map.setNodes(this.loadNodes(mapid, mapData));
        if (reactors && mapData.getChildByPath("reactor") != null) {
            for (final MapleData reactor : mapData.getChildByPath("reactor")) {
                final String id = MapleDataTool.getString(reactor.getChildByPath("id"));
                if (id != null) {
                    map.spawnReactor(this.loadReactor(reactor, id, (byte)MapleDataTool.getInt(reactor.getChildByPath("f"), 0)));
                }
            }
        }
        try {
            map.setMapName(MapleDataTool.getString("mapName", MapleMapFactory.nameData.getChildByPath(this.getMapStringName(mapid)), ""));
            map.setStreetName(MapleDataTool.getString("streetName", MapleMapFactory.nameData.getChildByPath(this.getMapStringName(mapid)), ""));
        }
        catch (Exception e) {
            map.setMapName("");
            map.setStreetName("");
        }
        map.setClock(MapleDataTool.getInt(mapData.getChildByPath("info/clock"), 0) > 0);
        map.setEverlast(MapleDataTool.getInt(mapData.getChildByPath("info/everlast"), 0) > 0);
        map.setTown(MapleDataTool.getInt(mapData.getChildByPath("info/town"), 0) > 0);
        map.setSoaring(MapleDataTool.getInt(mapData.getChildByPath("info/needSkillForFly"), 0) > 0);
        map.setForceMove(MapleDataTool.getInt(mapData.getChildByPath("info/lvForceMove"), 0));
        map.setHPDec(MapleDataTool.getInt(mapData.getChildByPath("info/decHP"), 0));
        map.setHPDecInterval(MapleDataTool.getInt(mapData.getChildByPath("info/decHPInterval"), 10000));
        map.setHPDecProtect(MapleDataTool.getInt(mapData.getChildByPath("info/protectItem"), 0));
        map.setForcedReturnMap(MapleDataTool.getInt(mapData.getChildByPath("info/forcedReturn"), 999999999));
        map.setTimeLimit(MapleDataTool.getInt(mapData.getChildByPath("info/timeLimit"), -1));
        map.setFieldLimit(MapleDataTool.getInt(mapData.getChildByPath("info/fieldLimit"), 0));
        map.setFirstUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onFirstUserEnter"), ""));
        map.setUserEnter(MapleDataTool.getString(mapData.getChildByPath("info/onUserEnter"), ""));
        map.setRecoveryRate(MapleDataTool.getFloat(mapData.getChildByPath("info/recovery"), 1.0f));
        map.setFixedMob(MapleDataTool.getInt(mapData.getChildByPath("info/fixedMobCapacity"), 0));
        map.setConsumeItemCoolTime(MapleDataTool.getInt(mapData.getChildByPath("info/consumeItemCoolTime"), 0));
        this.instanceMap.put(instanceid, map);
        return map;
    }
    
    public int getLoadedMaps() {
        return this.maps.size();
    }
    
    public boolean isMapLoaded(final int mapId) {
        return this.maps.containsKey((Object) mapId);
    }
    
    public boolean isInstanceMapLoaded(final int instanceid) {
        return this.instanceMap.containsKey((Object) instanceid);
    }
    
    public void clearLoadedMap() {
        this.maps.clear();
    }
    
    public Collection<MapleMap> getAllMaps() {
        return this.maps.values();
    }
    
    public Collection<MapleMap> getAllMapThreadSafe() {
        return new ArrayList<MapleMap>((Collection<? extends MapleMap>) this.maps.values());
    }
    
    public Collection<MapleMap> getAllInstanceMaps() {
        return this.instanceMap.values();
    }
    
    private AbstractLoadedMapleLife loadLife(final MapleData life, final String id, final String type) {
        AbstractLoadedMapleLife myLife = null;
        try {
            myLife = MapleLifeFactory.getLife(Integer.parseInt(id), type);
        } catch (NumberFormatException e) {
            myLife = MapleLifeFactory.getLife(910000000, type);
        }
        if (myLife == null) {
            return null;
        }
        myLife.setCy(MapleDataTool.getInt(life.getChildByPath("cy")));
        final MapleData dF = life.getChildByPath("f");
        if (dF != null) {
            myLife.setF(MapleDataTool.getInt(dF));
        }
        myLife.setFh(MapleDataTool.getInt(life.getChildByPath("fh")));
        myLife.setRx0(MapleDataTool.getInt(life.getChildByPath("rx0")));
        myLife.setRx1(MapleDataTool.getInt(life.getChildByPath("rx1")));
        //吸怪设置怪物初始位置   获取角色位置   设置怪物状态
        myLife.setPosition(new Point(MapleDataTool.getInt(life.getChildByPath("x")), MapleDataTool.getInt(life.getChildByPath("y"))));
        if (MapleDataTool.getInt("hide", life, 0) == 1 && myLife instanceof MapleNPC) {
            myLife.setHide(true);
        }
        return myLife;
    }
    
    private MapleReactor loadReactor(final MapleData reactor, final String id, final byte FacingDirection) {
        final MapleReactorStats stats = MapleReactorFactory.getReactor(Integer.parseInt(id));
        final MapleReactor myReactor = new MapleReactor(stats, Integer.parseInt(id));
        stats.setFacingDirection(FacingDirection);
        myReactor.setPosition(new Point(MapleDataTool.getInt(reactor.getChildByPath("x")), MapleDataTool.getInt(reactor.getChildByPath("y"))));
        myReactor.setDelay(MapleDataTool.getInt(reactor.getChildByPath("reactorTime")) * 1000);
        myReactor.setState((byte)0);
        myReactor.setName(MapleDataTool.getString(reactor.getChildByPath("name"), ""));
        return myReactor;
    }
    
    private String getMapName(final int mapid) {
        String mapName = StringUtil.getLeftPaddedStr(Integer.toString(mapid), '0', 9);
        final StringBuilder builder = new StringBuilder("Map/Map");
        builder.append(mapid / 100000000);
        builder.append("/");
        builder.append(mapName);
        builder.append(".img");
        mapName = builder.toString();
        return mapName;
    }
    
    private String getMapStringName(final int mapid) {
        final StringBuilder builder = new StringBuilder();
        if (mapid < 100000000) {
            builder.append("maple");
        }
        else if ((mapid >= 100000000 && mapid < 200000000) || mapid / 100000 == 5540) {
            builder.append("victoria");
        }
        else if (mapid >= 200000000 && mapid < 300000000) {
            builder.append("ossyria");
        }
        else if (mapid >= 300000000 && mapid < 400000000) {
            builder.append("elin");
        }
        else if (mapid >= 500000000 && mapid < 510000000) {
            builder.append("thai");
        }
        else if (mapid >= 540000000 && mapid < 600000000) {
            builder.append("SG");
        }
        else if (mapid >= 600000000 && mapid < 620000000) {
            builder.append("MasteriaGL");
        }
        else if ((mapid >= 670000000 && mapid < 677000000) || (mapid >= 678000000 && mapid < 682000000)) {
            builder.append("global");
        }
        else if (mapid >= 677000000 && mapid < 678000000) {
            builder.append("Episode1GL");
        }
        else if (mapid >= 682000000 && mapid < 683000000) {
            builder.append("HalloweenGL");
        }
        else if (mapid >= 683000000 && mapid < 684000000) {
            builder.append("event");
        }
        else if (mapid >= 684000000 && mapid < 685000000) {
            builder.append("event_5th");
        }
        else if (mapid >= 700000000 && mapid < 700000300) {
            builder.append("wedding");
        }
        else if (mapid >= 800000000 && mapid < 900000000) {
            builder.append("jp");
        }
        else if (mapid >= 700000000 && mapid < 782000002) {
            builder.append("chinese");
        }
        else {
            builder.append("etc");
        }
        builder.append("/");
        builder.append(mapid);
        return builder.toString();
    }
    
    public void setChannel(final int channel) {
        this.channel = channel;
    }

    // 添加野外BOSS刷新
    public void addAreaBossSpawn(MapleMap map) {
        // 检查 Start.ltMobSpawnBoss 是否为空
        if (Start.ltMobSpawnBoss == null || !Start.ltMobSpawnBoss.containsKey(map.getId())) {
            return;
        }

        List<LtMobSpawnBoss> ltMobSpawnBosses = Start.ltMobSpawnBoss.get(map.getId());
        if (ltMobSpawnBosses == null || ltMobSpawnBosses.isEmpty()) {
            return;
        }

        Point point = new Point(); // 复用 Point 对象
        for (LtMobSpawnBoss ltMobSpawnBoss : ltMobSpawnBosses) {
            int monsterid = ltMobSpawnBoss.getMobid();
            int mobtime = ltMobSpawnBoss.getTime();
            String msg = ltMobSpawnBoss.getName();

            // 验证 monsterid 和 mobtime 是否有效
            if (monsterid <= 0 || mobtime <= 0) {
                continue; // 跳过无效的 BOSS 数据
            }

            // 获取怪物对象
            MapleMonster monster = MapleLifeFactory.getMonster(monsterid);
            if (monster == null) {
                System.err.println("Failed to load monster with ID: " + monsterid);
                continue; // 跳过加载失败的怪物
            }

            // 设置位置点
            if (setPoint(point, ltMobSpawnBoss.getX(), ltMobSpawnBoss.getY()) &&
                    setPoint(point, ltMobSpawnBoss.getX1(), ltMobSpawnBoss.getY1()) &&
                    setPoint(point, ltMobSpawnBoss.getX2(), ltMobSpawnBoss.getY2())) {
                map.addAreaMonsterSpawn(monster, point, point, point, mobtime, msg);
            }
        }
    }
    // 辅助方法：设置 Point 并验证坐标有效性
    private boolean setPoint(Point point, Integer x, Integer y) {
        if (x == null || y == null) {
            return false; // 坐标无效
        }
        point.setLocation(x, y);
        return true;
    }
    private MapleNodes loadNodes(final int mapid, final MapleData mapData) {
        MapleNodes nodeInfo = (MapleNodes)MapleMapFactory.mapInfos.get((Object)Integer.valueOf(mapid));
        if (nodeInfo == null) {
            nodeInfo = new MapleNodes(mapid);
            if (mapData.getChildByPath("nodeInfo") != null) {
                for (final MapleData node : mapData.getChildByPath("nodeInfo")) {
                    try {
                        final String name2 = node.getName();
                        int n = -1;
                        switch (name2.hashCode()) {
                            case 109757538: {
                                if (name2.equals((Object)"start")) {
                                    n = 0;
                                    break;
                                }
                                break;
                            }
                            case 100571: {
                                if (name2.equals((Object)"end")) {
                                    n = 1;
                                    break;
                                }
                                break;
                            }
                        }
                        switch (n) {
                            case 0: {
                                nodeInfo.setNodeStart(MapleDataTool.getInt(node, 0));
                                continue;
                            }
                            case 1: {
                                nodeInfo.setNodeEnd(MapleDataTool.getInt(node, 0));
                                continue;
                            }
                            default: {
                                final List<Integer> edges = new ArrayList<Integer>();
                                if (node.getChildByPath("edge") != null) {
                                    for (final MapleData edge : node.getChildByPath("edge")) {
                                        edges.add(Integer.valueOf(MapleDataTool.getInt(edge, -1)));
                                    }
                                }
                                final MapleNodeInfo mni = new MapleNodeInfo(Integer.parseInt(node.getName()), MapleDataTool.getIntConvert("key", node, 0), MapleDataTool.getIntConvert("x", node, 0), MapleDataTool.getIntConvert("y", node, 0), MapleDataTool.getIntConvert("attr", node, 0), edges);
                                nodeInfo.addNode(mni);
                                continue;
                            }
                        }
                    }
                    catch (NumberFormatException ex) {}
                }
                nodeInfo.sortNodes();
            }
            for (int i = 1; i <= 7; ++i) {
                if (mapData.getChildByPath(String.valueOf(i)) != null && mapData.getChildByPath(i + "/obj") != null) {
                    for (final MapleData node2 : mapData.getChildByPath(i + "/obj")) {
                        final int sn_count = MapleDataTool.getIntConvert("SN_count", node2, 0);
                        final String name = MapleDataTool.getString("name", node2, "");
                        final int speed = MapleDataTool.getIntConvert("speed", node2, 0);
                        if (sn_count > 0 && speed > 0) {
                            if (name.equals((Object)"")) {
                                continue;
                            }
                            final List<Integer> SN = new ArrayList<Integer>();
                            for (int x = 0; x < sn_count; ++x) {
                                SN.add(Integer.valueOf(MapleDataTool.getIntConvert("SN" + x, node2, 0)));
                            }
                            final MaplePlatform mni2 = new MaplePlatform(name, MapleDataTool.getIntConvert("start", node2, 2), speed, MapleDataTool.getIntConvert("x1", node2, 0), MapleDataTool.getIntConvert("y1", node2, 0), MapleDataTool.getIntConvert("x2", node2, 0), MapleDataTool.getIntConvert("y2", node2, 0), MapleDataTool.getIntConvert("r", node2, 0), SN);
                            nodeInfo.addPlatform(mni2);
                        }
                    }
                }
            }
            if (mapData.getChildByPath("area") != null) {
                for (final MapleData area : mapData.getChildByPath("area")) {
                    final int x2 = MapleDataTool.getInt(area.getChildByPath("x1"));
                    final int y1 = MapleDataTool.getInt(area.getChildByPath("y1"));
                    final int x3 = MapleDataTool.getInt(area.getChildByPath("x2"));
                    final int y2 = MapleDataTool.getInt(area.getChildByPath("y2"));
                    final Rectangle mapArea = new Rectangle(x2, y1, x3 - x2, y2 - y1);
                    nodeInfo.addMapleArea(mapArea);
                }
            }
            if (mapData.getChildByPath("monsterCarnival") != null) {
                final MapleData mc = mapData.getChildByPath("monsterCarnival");
                if (mc.getChildByPath("mobGenPos") != null) {
                    for (final MapleData area2 : mc.getChildByPath("mobGenPos")) {
                        nodeInfo.addMonsterPoint(MapleDataTool.getInt(area2.getChildByPath("x")), MapleDataTool.getInt(area2.getChildByPath("y")), MapleDataTool.getInt(area2.getChildByPath("fh")), MapleDataTool.getInt(area2.getChildByPath("cy")), MapleDataTool.getInt("team", area2, -1));
                    }
                }
                if (mc.getChildByPath("mob") != null) {
                    for (final MapleData area2 : mc.getChildByPath("mob")) {
                        nodeInfo.addMobSpawn(MapleDataTool.getInt(area2.getChildByPath("id")), MapleDataTool.getInt(area2.getChildByPath("spendCP")));
                    }
                }
                if (mc.getChildByPath("guardianGenPos") != null) {
                    for (final MapleData area2 : mc.getChildByPath("guardianGenPos")) {
                        nodeInfo.addGuardianSpawn(new Point(MapleDataTool.getInt(area2.getChildByPath("x")), MapleDataTool.getInt(area2.getChildByPath("y"))), MapleDataTool.getInt("team", area2, -1));
                    }
                }
                if (mc.getChildByPath("skill") != null) {
                    for (final MapleData area2 : mc.getChildByPath("skill")) {
                        nodeInfo.addSkillId(MapleDataTool.getInt(area2));
                    }
                }
            }
            MapleMapFactory.mapInfos.put(Integer.valueOf(mapid), nodeInfo);
        }
        return nodeInfo;
    }
    
    public static int loadCustomLife() {
        return loadCustomLife(false, null);
    }
    
    public static int loadCustomLife(final boolean reload, final MapleMap map) {
        if (reload) {
            final int mid = map.getId();
            final List<AbstractLoadedMapleLife> custom = (List<AbstractLoadedMapleLife>)MapleMapFactory.RemovecustomLife.get((Object)Integer.valueOf(mid));
            if (custom != null) {
                for (final AbstractLoadedMapleLife n : custom) {
                    final String cType = n.getCType();
                    int n2 = -1;
                    switch (cType.hashCode()) {
                        case 110: {
                            if (cType.equals((Object)"n")) {
                                n2 = 0;
                                break;
                            }
                            break;
                        }
                    }
                    switch (n2) {
                        case 0: {
                            map.removeNpc_(n.getId());
                            continue;
                        }
                    }
                }
            }
            MapleMapFactory.customLife.clear();
            MapleMapFactory.changed = true;
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            try (final PreparedStatement ps = con.prepareStatement("SELECT * FROM `wz_customlife`");
                 final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final int mapid = rs.getInt("mid");
                    final AbstractLoadedMapleLife myLife = loadLife(rs.getInt("dataid"), rs.getInt("f"), rs.getByte("hide") > 0, rs.getInt("fh"), rs.getInt("cy"), rs.getInt("rx0"), rs.getInt("rx1"), rs.getInt("x"), rs.getInt("y"), rs.getString("type"), rs.getInt("mobtime"));
                    if (myLife == null) {
                        continue;
                    }
                    final List<AbstractLoadedMapleLife> entries = (List<AbstractLoadedMapleLife>)MapleMapFactory.customLife.get((Object)Integer.valueOf(mapid));
                    final List<AbstractLoadedMapleLife> collections = new ArrayList<AbstractLoadedMapleLife>();
                    if (entries == null) {
                        collections.add(myLife);
                        MapleMapFactory.RemovecustomLife.put(Integer.valueOf(mapid), collections);
                        MapleMapFactory.customLife.put(Integer.valueOf(mapid), collections);
                    }
                    else {
                        collections.addAll((Collection<? extends AbstractLoadedMapleLife>)entries);
                        collections.add(myLife);
                        MapleMapFactory.RemovecustomLife.put(Integer.valueOf(mapid), collections);
                        MapleMapFactory.customLife.put(Integer.valueOf(mapid), collections);
                    }
                }
            }
            return MapleMapFactory.customLife.size();
        }
        catch (SQLException e) {
            System.err.println("Error loading custom life..." + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return -1;
        }
    }
    
    private static AbstractLoadedMapleLife loadLife(final int id, final int f, final boolean hide, final int fh, final int cy, final int rx0, final int rx1, final int x, final int y, final String type, final int mtime) {
        final AbstractLoadedMapleLife myLife = MapleLifeFactory.getLife(id, type);
        if (myLife == null) {
            System.err.println("自訂 npc " + id + " 异常...");
            return null;
        }
        myLife.setCy(cy);
        myLife.setF(f);
        myLife.setFh(fh);
        myLife.setRx0(rx0);
        myLife.setRx1(rx1);
        myLife.setPosition(new Point(x, y));
        myLife.setHide(hide);
        myLife.setMTime(mtime);
        myLife.setCType(type);
        return myLife;
    }
    
    public boolean destroyMap(final int mapid) {
        return this.destroyMap(mapid, false);
    }
    
    public boolean destroyMap(final int mapid, final boolean Remove) {
        synchronized (this.maps) {
            if (this.maps.containsKey((Object)Integer.valueOf(mapid))) {
                if (Remove) {
                    this.DeStorymaps.put(Integer.valueOf(mapid), Integer.valueOf(0));
                    this.maps.remove((Object)Integer.valueOf(mapid));
                }
                return this.maps.remove((Object)Integer.valueOf(mapid)) != null;
            }
        }
        return false;
    }
    
    public void HealMap(final int mapid) {
        synchronized (this.maps) {
            if (this.DeStorymaps.containsKey((Object)Integer.valueOf(mapid))) {
                this.DeStorymaps.remove((Object)Integer.valueOf(mapid));
            }
        }
    }
    public static void 加载地图名称() {
        mapnames.clear();
        MapleData data = MapleDataProviderFactory.getDataProvider("String.wz").getData("Map.img");
        Iterator var1 = data.getChildren().iterator();

        while(var1.hasNext()) {
            MapleData mapAreaData = (MapleData)var1.next();
            Iterator var3 = mapAreaData.getChildren().iterator();

            while(var3.hasNext()) {
                MapleData mapIdData = (MapleData)var3.next();
                mapnames.put(Integer.parseInt(mapIdData.getName()), "'" + MapleDataTool.getString(mapIdData.getChildByPath("streetName"), "无名称") + " : " + MapleDataTool.getString(mapIdData.getChildByPath("mapName"), "无名称") + "'");
            }
        }

    }
    static {
        source = MapleDataProviderFactory.getDataProvider("Map.wz");
        nameData = MapleDataProviderFactory.getDataProvider("String.wz").getData("Map.img");
        mapInfos = new HashMap<Integer, MapleNodes>();
        customLife = new HashMap<Integer, List<AbstractLoadedMapleLife>>();
        RemovecustomLife = new HashMap<Integer, List<AbstractLoadedMapleLife>>();
        MapleMapFactory.changed = false;
    }
}
