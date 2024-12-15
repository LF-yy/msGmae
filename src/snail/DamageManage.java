//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import client.MapleCharacter;
import constants.GameConstants;
import database.DBConPool;
import gui.服务端输出信息;
import handling.channel.ChannelServer;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.*;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class DamageManage {
    private ArrayList<MobDamageData> mobDamageDataList = new ArrayList();
    private static DamageManage instance = new DamageManage();

    public DamageManage() {
    }

    public static DamageManage getInstance() {
        return instance;
    }

    public MobDamageData getMobDamageDataByMob(MapleMonster mob) {
        if (mob == null) {
            return null;
        } else {
            Iterator var2 = this.mobDamageDataList.iterator();

            MobDamageData mdd;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                mdd = (MobDamageData)var2.next();
            } while(mdd == null || !mdd.isMonitorMob(mob));

            return mdd;
        }
    }

    public ArrayList<MobDamageData> getMobDamageDataListByMobId(int mobId) {
        ArrayList<MobDamageData> mobDamageDataList0 = new ArrayList();
        Iterator var3 = this.mobDamageDataList.iterator();

        while(var3.hasNext()) {
            MobDamageData mdd = (MobDamageData)var3.next();
            if (mdd != null && mdd.getMainMobId() == mobId) {
                mobDamageDataList0.add(mdd);
            }
        }

        return mobDamageDataList0;
    }

    public ArrayList<MobDamageData> getMobDamageDataList() {
        return this.mobDamageDataList;
    }

    public ArrayList<MobDamageData> getMobDamageDataListByChr(MapleCharacter chr) {
        if (chr == null) {
            return null;
        } else {
            ArrayList<MobDamageData> mobDamageDataList0 = new ArrayList();
            Iterator var3 = this.mobDamageDataList.iterator();

            while(var3.hasNext()) {
                MobDamageData mdd = (MobDamageData)var3.next();
                if (mdd != null && mdd.containsChrId(chr.getId())) {
                    mobDamageDataList0.add(mdd);
                }
            }

            return mobDamageDataList0;
        }
    }

    public boolean deleteAllMobDamageDataByChr(int chrId) {
        int count = this.mobDamageDataList.size();

        for(int i = 0; i < count; ++i) {
            MobDamageData mdd = (MobDamageData)this.mobDamageDataList.get(i);
            if (mdd != null && mdd.containsChrId(chrId)) {
                mdd.getDamageMap().remove(chrId);
            }
        }

        try {
            Connection con = DBConPool.getConnection();
            Throwable var20 = null;

            boolean var6;
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monster_damage WHERE characterid = ?");
                ps.setInt(1, chrId);
                ps.executeUpdate();
                ps.close();
                var6 = true;
            } catch (Throwable var16) {
                var20 = var16;
                throw var16;
            } finally {
                if (con != null) {
                    if (var20 != null) {
                        try {
                            con.close();
                        } catch (Throwable var15) {
                            var20.addSuppressed(var15);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var6;
        } catch (Exception var18) {
            服务端输出信息.println_err("【错误】deleteAllMobDamageDataByChr 删除数据库错误，错误原因：" + var18);
            var18.printStackTrace();
            return false;
        }
    }

    public boolean deleteMobDamageDataByChr(int chrId, int mobId) {
        int count = this.mobDamageDataList.size();

        for(int i = 0; i < count; ++i) {
            MobDamageData mdd = (MobDamageData)this.mobDamageDataList.get(i);
            if (mdd != null && mdd.getMainMobId() == mobId && mdd.containsChrId(chrId)) {
                mdd.getDamageMap().remove(chrId);
            }
        }

        try {
            Connection con = DBConPool.getConnection();
            Throwable var21 = null;

            boolean var7;
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monster_damage WHERE characterid = ? AND mobid = ?");
                ps.setInt(1, chrId);
                ps.setInt(2, mobId);
                ps.executeUpdate();
                ps.close();
                var7 = true;
            } catch (Throwable var17) {
                var21 = var17;
                throw var17;
            } finally {
                if (con != null) {
                    if (var21 != null) {
                        try {
                            con.close();
                        } catch (Throwable var16) {
                            var21.addSuppressed(var16);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var7;
        } catch (Exception var19) {
            服务端输出信息.println_err("【错误】deleteMobDamageDataByChr 删除数据库错误，错误原因：" + var19);
            var19.printStackTrace();
            return false;
        }
    }

    public boolean deleteAllMobDamageDataByMob(int mobId) {
        int count = this.mobDamageDataList.size();

        for(int i = 0; i < count; ++i) {
            MobDamageData mdd = (MobDamageData)this.mobDamageDataList.get(i);
            if (mdd != null && mdd.getMainMobId() == mobId) {
                this.mobDamageDataList.remove(i);
                --i;
                --count;
            }
        }

        try {
            Connection con = DBConPool.getConnection();
            Throwable var20 = null;

            boolean var6;
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monster_damage WHERE mobId = ?");
                ps.setInt(1, mobId);
                ps.executeUpdate();
                ps.close();
                var6 = true;
            } catch (Throwable var16) {
                var20 = var16;
                throw var16;
            } finally {
                if (con != null) {
                    if (var20 != null) {
                        try {
                            con.close();
                        } catch (Throwable var15) {
                            var20.addSuppressed(var15);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var6;
        } catch (Exception var18) {
            服务端输出信息.println_err("【错误】deleteAllMobDamageDataByMob 删除数据库错误，错误原因：" + var18);
            var18.printStackTrace();
            return false;
        }
    }

    public MobDamageData getMobDamageDataByMap(MapleMap map) {
        if (map == null) {
            return null;
        } else {
            Iterator var2 = this.mobDamageDataList.iterator();

            MobDamageData mdd;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                mdd = (MobDamageData)var2.next();
            } while(mdd == null || !mdd.getMapList().contains(map));

            return mdd;
        }
    }

    public boolean loadMobDamageDataListFromDB() {
        this.mobDamageDataList.clear();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_monster_damage");
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    MobDamageData mobDamageData = new MobDamageData();
                    mobDamageData.addChrInfo(rs.getInt("characterid"), rs.getShort("level"), rs.getShort("job"), rs.getShort("gm"), (long)rs.getInt("force"));
                    mobDamageData.setMainMobId(rs.getInt("mobid"));
                    mobDamageData.setMainMobName(rs.getString("mobname"));
                    mobDamageData.setDamage(rs.getInt("characterid"), rs.getLong("damage"));
                    mobDamageData.setSeconds(rs.getInt("seconds"));
                    mobDamageData.setStopTime(rs.getLong("recordtime"));
                    mobDamageData.setStartTime(mobDamageData.getStopTime() - (long)(mobDamageData.getSeconds() * 1000));
                    this.mobDamageDataList.add(mobDamageData);
                }

                ps.close();
                rs.close();
                boolean var18 = true;
                return var18;
            } catch (Throwable var15) {
                var2 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var2.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var17) {
            服务端输出信息.println_err("【错误】loadMobDamageDataListFromDB错误，错误原因：" + var17);
            var17.printStackTrace();
            return false;
        }
    }

    public ArrayList<Pair<Integer, OutputChrDamageData>> getDamageRankListByMobId(int mobId) {
        ArrayList<Pair<Integer, OutputChrDamageData>> damageRankList = new ArrayList();
        Iterator var3 = this.mobDamageDataList.iterator();

        while(true) {
            MobDamageData mobDamageData;
            long dps2;
            do {
                do {
                    do {
                        if (!var3.hasNext()) {
                            int n = damageRankList.size();
                            if (n > 1) {
                                for(int i = 0; i < n; ++i) {
                                    boolean flag = false;

                                    for(int j = 0; j < n - i - 1; ++j) {
                                        long dps1 = ((OutputChrDamageData)((Pair)damageRankList.get(j)).right).getDps();
                                        dps2 = ((OutputChrDamageData)((Pair)damageRankList.get(j + 1)).right).getDps();
                                        if (dps1 < dps2) {
                                            Collections.swap(damageRankList, j, j + 1);
                                            flag = true;
                                        }
                                    }

                                    if (!flag) {
                                        break;
                                    }
                                }
                            }

                            return damageRankList;
                        }

                        mobDamageData = (MobDamageData)var3.next();
                    } while(mobDamageData == null);
                } while(mobDamageData.getMainMobId() != mobId);
            } while(mobDamageData.stopTime <= 0L);

            Iterator var5 = mobDamageData.getDamageMap().entrySet().iterator();

            while(var5.hasNext()) {
                Map.Entry<Integer, Long> entry = (Map.Entry)var5.next();
                int chrId = (Integer)entry.getKey();
                MobDamageData.ChrInfo info = mobDamageData.getChrInfo((Integer)entry.getKey());
                dps2 = mobDamageData.getDamage(chrId);
                int seconds = mobDamageData.getSeconds();
                long dps = dps2 / (long)seconds;
                OutputChrDamageData outputChrDamageData = new OutputChrDamageData(chrId, info.getJob(), info.getLevel(), info.getGm(), info.getForce(), mobDamageData.getMainMobId(), mobDamageData.getMainMobName(), dps2, dps, seconds, mobDamageData.getStartTime(), mobDamageData.getStopTime());
                damageRankList.add(new Pair(entry.getKey(), outputChrDamageData));
            }
        }
    }

    public ArrayList<Pair<Integer, OutputChrDamageData>> getDamageRankListByMobId(int mobId, int chrId0) {
        ArrayList<Pair<Integer, OutputChrDamageData>> damageRankList = new ArrayList();
        Iterator var4 = this.mobDamageDataList.iterator();

        while(true) {
            MobDamageData mobDamageData;
            long damage;
            do {
                do {
                    do {
                        if (!var4.hasNext()) {
                            int n = damageRankList.size();
                            if (n > 1) {
                                for(int i = 0; i < n; ++i) {
                                    boolean flag = false;

                                    for(int j = 0; j < n - i - 1; ++j) {
                                        long dps1 = ((OutputChrDamageData)((Pair)damageRankList.get(j)).right).getDps();
                                        damage = ((OutputChrDamageData)((Pair)damageRankList.get(j + 1)).right).getDps();
                                        if (dps1 < damage) {
                                            Collections.swap(damageRankList, j, j + 1);
                                            flag = true;
                                        }
                                    }

                                    if (!flag) {
                                        break;
                                    }
                                }
                            }

                            return damageRankList;
                        }

                        mobDamageData = (MobDamageData)var4.next();
                    } while(mobDamageData == null);
                } while(mobDamageData.getMainMobId() != mobId);
            } while(mobDamageData.stopTime <= 0L);

            Iterator var6 = mobDamageData.getDamageMap().entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<Integer, Long> entry = (Map.Entry)var6.next();
                int chrId = (Integer)entry.getKey();
                if (chrId == chrId0) {
                    MobDamageData.ChrInfo info = mobDamageData.getChrInfo((Integer)entry.getKey());
                    damage = mobDamageData.getDamage(chrId);
                    int seconds = mobDamageData.getSeconds();
                    long dps = damage / (long)seconds;
                    OutputChrDamageData outputChrDamageData = new OutputChrDamageData(chrId, info.getJob(), info.getLevel(), info.getGm(), info.getForce(), mobDamageData.getMainMobId(), mobDamageData.getMainMobName(), damage, dps, seconds, mobDamageData.getStartTime(), mobDamageData.getStopTime());
                    damageRankList.add(new Pair(entry.getKey(), outputChrDamageData));
                }
            }
        }
    }

    public ArrayList<Pair<Integer, OutputChrDamageData>> solveDamageRankListToWeek(ArrayList<Pair<Integer, OutputChrDamageData>> damageRankList) {
        if (damageRankList == null) {
            return null;
        } else if (damageRankList.size() < 1) {
            return damageRankList;
        } else {
            int count = damageRankList.size();
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(7);
            int hour = calendar.get(11);
            int minute = calendar.get(12);
            int second = calendar.get(13);
            long time = calendar.getTimeInMillis();
            long stopTime = 0L;
            long startTime = 0L;
            if (day > 1) {
                stopTime = time - (long)((day - 2) * 1000 * 60 * 60 * 24 + hour * 60 * 60 * 1000 + minute * 1000 * 60 + second * 1000);
                startTime = stopTime - 604800000L;
            } else {
                stopTime = time - (long)(518400000 + hour * 60 * 60 * 1000 + minute * 1000 * 60 + second * 1000);
                startTime = stopTime - 604800000L;
            }

            for(int i = 0; i < count; ++i) {
                if (((OutputChrDamageData)((Pair)damageRankList.get(i)).right).stopTime < startTime || ((OutputChrDamageData)((Pair)damageRankList.get(i)).right).stopTime > stopTime) {
                    damageRankList.remove(i);
                    --i;
                    --count;
                }
            }

            return damageRankList;
        }
    }

    public ArrayList<Pair<Integer, OutputChrDamageData>> solveDamageRankListToDay(ArrayList<Pair<Integer, OutputChrDamageData>> damageRankList) {
        if (damageRankList == null) {
            return null;
        } else if (damageRankList.size() < 1) {
            return damageRankList;
        } else {
            int count = damageRankList.size();
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(7);
            int hour = calendar.get(11);
            int minute = calendar.get(12);
            int second = calendar.get(13);
            long time = calendar.getTimeInMillis();
            long stopTime = 0L;
            long startTime = 0L;
            stopTime = time - (long)(hour * 60 * 60 * 1000 + minute * 1000 * 60 + second * 1000);
            startTime = stopTime - 86400000L;

            for(int i = 0; i < count; ++i) {
                if (((OutputChrDamageData)((Pair)damageRankList.get(i)).right).stopTime < startTime || ((OutputChrDamageData)((Pair)damageRankList.get(i)).right).stopTime > stopTime) {
                    damageRankList.remove(i);
                    --i;
                    --count;
                }
            }

            return damageRankList;
        }
    }

    public MobDamageData newDamageData() {
        MobDamageData mobDamageData = new MobDamageData();
        this.mobDamageDataList.add(mobDamageData);
        return mobDamageData;
    }

    public boolean addMonster(MobDamageData mobDamageData, MapleMonster mob, boolean mainMob) {
        if (mobDamageData != null && mob != null) {
            mobDamageData.addMonster(mob, mainMob);
            return true;
        } else {
            return false;
        }
    }

    public class OutputChrDamageData {
        private int chrId;
        private int mobId;
        private int seconds;
        private short job;
        private short level;
        private short gm;
        private long damage;
        private long startTime;
        private long stopTime;
        private long dps;
        private long force;
        private String mobName;

        public long getStartTime() {
            return this.startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getStopTime() {
            return this.stopTime;
        }

        public void setStopTime(long stopTime) {
            this.stopTime = stopTime;
        }

        public short getGm() {
            return this.gm;
        }

        public void setGm(short gm) {
            this.gm = gm;
        }

        public OutputChrDamageData(int chrId, short job, short level, short gm, long force, int mobId, String mobName, long damage, long dps, int seconds, long startTime, long stopTime) {
            this.chrId = chrId;
            this.job = job;
            this.level = level;
            this.gm = gm;
            this.force = force;
            this.mobId = mobId;
            this.mobName = mobName;
            this.damage = damage;
            this.dps = dps;
            this.seconds = seconds;
            this.startTime = startTime;
            this.stopTime = stopTime;
        }

        public int getChrId() {
            return this.chrId;
        }

        public void setChrId(int chrId) {
            this.chrId = chrId;
        }

        public long getForce() {
            return this.force;
        }

        public void setForce(int force) {
            this.force = (long)force;
        }

        public int getMobId() {
            return this.mobId;
        }

        public void setMobId(int mobId) {
            this.mobId = mobId;
        }

        public String getMobName() {
            return this.mobName;
        }

        public void setMobName(String mobName) {
            this.mobName = mobName;
        }

        public long getDps() {
            return this.dps;
        }

        public void setDps(long dps) {
            this.dps = dps;
        }

        public int getSeconds() {
            return this.seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        public short getJob() {
            return this.job;
        }

        public void setJob(short job) {
            this.job = job;
        }

        public short getLevel() {
            return this.level;
        }

        public void setLevel(short level) {
            this.level = level;
        }

        public long getDamage() {
            return this.damage;
        }

        public void setDamage(long damage) {
            this.damage = damage;
        }
    }

    public static class MobDamageData {
        private Map<Integer, Long> damageMap = new HashMap();
        private ArrayList<MapleMonster> mobList = new ArrayList();
        private ArrayList<MapleMap> mapList = new ArrayList();
        private long startTime;
        private long stopTime;
        private long recordTime;
        private int mainMobId;
        private String mainMobName;
        private long totalDamage;
        private Map<Integer, ChrInfo> chrInfoMap = new HashMap();
        private int seconds;

        public ArrayList<MapleMap> getMapList() {
            return this.mapList;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getStopTime() {
            return this.stopTime;
        }

        public void setStopTime(long stopTime) {
            this.stopTime = stopTime;
        }

        public long getRecordTime() {
            return this.recordTime;
        }

        public void setRecordTime(long recordTime) {
            this.recordTime = recordTime;
        }

        public int getSeconds() {
            if (this.seconds <= 0) {
                this.seconds = 1;
            }

            return this.seconds;
        }

        public void setSeconds(int seconds) {
            this.seconds = seconds;
        }

        public long getTotalDamage() {
            return this.totalDamage;
        }

        public void setTotalDamage(long totalDamage) {
            this.totalDamage = totalDamage;
        }

        public Map<Integer, ChrInfo> getChrInfoMap() {
            return this.chrInfoMap;
        }

        public ChrInfo getChrInfo(int chrId) {
            return (ChrInfo)this.chrInfoMap.get(chrId);
        }

        public void clearChrInfoMap() {
            this.chrInfoMap.clear();
        }

        public void addChrInfo(int chrId, short level, short job, short gm, long force) {
            ChrInfo chrInfo = new ChrInfo(chrId, level, job, gm, force);
            this.chrInfoMap.put(chrId, chrInfo);
        }

        public boolean removeChrInfo(int chrId) {
            if (this.chrInfoMap.containsKey(chrId)) {
                this.chrInfoMap.remove(chrId);
                return true;
            } else {
                return false;
            }
        }

        public MobDamageData(ArrayList<MapleMonster> mobList) {
            this.mobList = mobList;
            Iterator var2 = mobList.iterator();

            while(var2.hasNext()) {
                MapleMonster mob = (MapleMonster)var2.next();
                if (mob != null && !this.mapList.contains(mob.getMap())) {
                    this.mapList.add(mob.getMap());
                }
            }

        }

        public MobDamageData() {
        }

        public Map<Integer, Long> getDamageMap() {
            return this.damageMap;
        }

        public long getDamage(int chrId) {
            return this.damageMap.get(chrId) == null ? 0L : (Long)this.damageMap.get(chrId);
        }

        public ArrayList<MapleMonster> getMobList() {
            return this.mobList;
        }

        public int getMainMobId() {
            return this.mainMobId;
        }

        public void setMainMobId(int mainMobId) {
            this.mainMobId = mainMobId;
        }

        public String getMainMobName() {
            return this.mainMobName;
        }

        public void setMainMobName(String mainMobName) {
            this.mainMobName = mainMobName;
        }

        public void addMonster(MapleMonster mob, boolean mainMob) {
            if (mob != null) {
                this.mobList.add(mob);
                if (!this.mapList.contains(mob.getMap())) {
                    this.mapList.add(mob.getMap());
                }

                if (mainMob) {
                    this.mainMobId = mob.getId();
                    this.mainMobName = mob.getStats().getName();
                }

                mob.setMonitor(true);
                mob.setMobDamageData(this);
            }

        }

        public void addMonster(MapleMonster mob) {
            this.addMonster(mob, false);
        }

        public void startMonitor() {
            this.startTime = System.currentTimeMillis();
        }

        public void stopMonitor() {
            this.stopTime = System.currentTimeMillis();
        }

        public boolean isMonitorMob(MapleMonster mob) {
            return this.mobList.contains(mob);
        }

        public void addDamage(MapleCharacter chr, Long damage) {
            if (chr != null && this.stopTime <= 0L && this.startTime > 0L) {
                int chrId = chr.getId();
                long oldDamage = 0L;
                if (this.damageMap.containsKey(chrId)) {
                    oldDamage = (Long)this.damageMap.get(chrId);
                }

                this.damageMap.put(chrId, oldDamage + damage);
                this.totalDamage += damage;
                this.addChrInfo(chr.getId(), chr.getLevel(), chr.getJob(), (short)chr.getGMLevel(), chr.getPower());
            }
        }

        public void setDamage(int chrId, Long damage) {
            this.damageMap.put(chrId, damage);
        }

        public void calculate() throws UnsupportedEncodingException {
            if (!this.damageMap.isEmpty() && !GameConstants.isFakeRevive(this.mainMobId) && !this.mapList.isEmpty()) {
                Iterator var1 = this.mobList.iterator();

                while(var1.hasNext()) {
                    MapleMonster mob = (MapleMonster)var1.next();
                    if (mob != null && mob.getId() == this.mainMobId && mob.isAlive() && mob.getId() != 8810018) {
                        return;
                    }
                }

                if (this.stopTime <= 0L) {
                    this.stopMonitor();
                    this.seconds = (int)(this.stopTime - this.startTime) / 1000;
                    this.damageMap = sortTool.sortDescend(this.damageMap);
                    ArrayList<MapleCharacter> chrList = new ArrayList();
                    Iterator var24 = this.damageMap.entrySet().iterator();

                    label322:
                    while(var24.hasNext()) {
                        Map.Entry<Integer, Long> entry = (Map.Entry)var24.next();
                        Iterator var4 = this.mapList.iterator();

                        while(true) {
                            MapleMap map;
                            do {
                                if (!var4.hasNext()) {
                                    continue label322;
                                }

                                map = (MapleMap)var4.next();
                            } while(map == null);

                            Iterator var6 = map.getCharactersThreadsafe().iterator();

                            while(var6.hasNext()) {
                                MapleCharacter chr = (MapleCharacter)var6.next();
                                if (chr != null && chr.getId() == (Integer)entry.getKey()) {
                                    chrList.add(chr);
                                }
                            }
                        }
                    }

                    if (!chrList.isEmpty()) {
                        var24 = chrList.iterator();

                        while(true) {
                            MapleCharacter chr;
                            Iterator var35;
                            do {
                                if (!var24.hasNext()) {
                                    MapleCharacter chr0 = (MapleCharacter)chrList.get(0);
                                    int mapId = 0;
                                    if (chr0 != null) {
                                        mapId = chr0.getMapId();
                                        String chrName = chr0.getName();
                                        String durationString = TimeUtil.formatTime(this.stopTime - this.startTime);
                                        String damageString = StringUtil.formatNum(this.damageMap.get(chr0.getId()) + "", false);
                                        String dpsString = StringUtil.formatNum((Long)this.damageMap.get(chr0.getId()) / ((this.stopTime - this.startTime) / 1000L) + "", false);
                                        Iterator var38 = ChannelServer.getAllInstances().iterator();

                                        while(var38.hasNext()) {
                                            ChannelServer cs = (ChannelServer)var38.next();
                                            Iterator var42 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                                            while(var42.hasNext()) {
                                                 chr = (MapleCharacter)var42.next();
                                                if (chr != null) {
                                                    chr.dropMessage(6, "[系统公告] " + chrList.size() + "人小队耗时 " + durationString + " 战胜了强大的怪物 [" + this.getMainMobName() + "]，恭喜 [" + chrName + "] 以总伤害(" + damageString + ") 每秒伤害(" + dpsString + ") 成为全场MVP！");
                                                }
                                            }
                                        }
                                    }

                                    if (mapId == 280030002 && this.mainMobId == 8800002) {
                                        this.mainMobId = 8800102;
                                    }

                                    try {
                                        Connection con = DBConPool.getConnection();
                                        Throwable var32 = null;

                                        try {
                                            var35 = this.damageMap.entrySet().iterator();

                                            while(var35.hasNext()) {
                                                Map.Entry<Integer, Long> entry = (Map.Entry)var35.next();
                                                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monster_damage WHERE recordtime < ?");
                                                ps.setLong(1, System.currentTimeMillis() - 864000000L);
                                                ps.executeUpdate();
                                                ps = con.prepareStatement("INSERT INTO snail_monster_damage (characterid,`level`,`job`,`gm`,`force`,`mobid`,mobname,damage,`dps`,seconds,recordtime,timeoftext) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP())");
                                                ps.setInt(1, (Integer)entry.getKey());
                                                ChrInfo info = (ChrInfo)this.chrInfoMap.get(entry.getKey());
                                                if (info != null) {
                                                    ps.setShort(2, info.getLevel());
                                                    ps.setShort(3, info.getJob());
                                                    ps.setShort(4, info.getGm());
                                                    ps.setLong(5, info.getForce());
                                                }

                                                ps.setInt(6, this.mainMobId);
                                                ps.setString(7, this.mainMobName);
                                                ps.setLong(8, (Long)entry.getValue());
                                                long duration = (this.stopTime - this.startTime) / 1000L;
                                                ps.setLong(9, (Long)entry.getValue() / duration);
                                                ps.setInt(10, (int)duration);
                                                ps.setLong(11, this.stopTime);
                                                ps.executeUpdate();
                                            }
                                        } catch (Throwable var20) {
                                            var32 = var20;
                                            throw var20;
                                        } finally {
                                            if (con != null) {
                                                if (var32 != null) {
                                                    try {
                                                        con.close();
                                                    } catch (Throwable var19) {
                                                        var32.addSuppressed(var19);
                                                    }
                                                } else {
                                                    con.close();
                                                }
                                            }

                                        }
                                    } catch (SQLException var22) {
                                        服务端输出信息.println_err("【错误】calculate()写数据库错误，错误原因：" + var22);
                                        var22.printStackTrace();
                                    }

                                    return;
                                }

                                chr = (MapleCharacter)var24.next();
                            } while(chr == null);

                            long duration = this.stopTime - this.startTime;
                            chr.dropMessage(6, "--------------------------------------------------------------------------------------");
                            chr.dropMessage(6, "\t\t\t\t\t\t\t\t\t  伤害统计\t\t\t\t\t\t\t\t\t  ");
                            chr.dropMessage(6, "             全队伤害：" + 处理字符串.formatString(10, ' ', StringUtil.formatNum(this.totalDamage + "", false)) + "                    总耗时：" + TimeUtil.formatTime(duration));
                            chr.dropMessage(6, "--------------------------------------------------------------------------------------");
                            chr.dropMessage(6, "          排名              角色名              总伤害              每秒伤害");
                            chr.dropMessage(6, ".");
                            int count = 1;
                            var35 = chrList.iterator();

                            while(var35.hasNext()) {
                                MapleCharacter chr0 = (MapleCharacter)var35.next();
                                if (chr0 != null) {
                                    String damageString = StringUtil.formatNum(this.damageMap.get(chr0.getId()) + "", false);
                                    String dpsString = StringUtil.formatNum((Long)this.damageMap.get(chr0.getId()) / (duration / 1000L) + "", false);
                                    chr.dropMessage(6, "          " + 处理字符串.formatString2(3, ' ', count + "") + "            " + 处理字符串.formatString2(12, ' ', chr0.getName()) + "         " + 处理字符串.formatString2(10, ' ', damageString + "") + "           " + 处理字符串.formatString2(10, ' ', dpsString + ""));
                                    ++count;
                                }
                            }

                            chr.dropMessage(6, "--------------------------------------------------------------------------------------");
                        }
                    }
                }
            }
        }

        public ArrayList<Integer> getChrIdList() {
            ArrayList<Integer> chrIdList = new ArrayList();
            Iterator var2 = this.damageMap.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<Integer, Long> entry = (Map.Entry)var2.next();
                chrIdList.add(entry.getKey());
            }

            return chrIdList;
        }

        public boolean containsChrId(int chrId) {
            return this.getChrIdList().contains(chrId);
        }

        private class ChrInfo {
            private int chrId;
            private long force;
            private short level;
            private short job;
            private short gm;

            public short getGm() {
                return this.gm;
            }

            public void setGm(short gm) {
                this.gm = gm;
            }

            public ChrInfo(int chrId, short level, short job, short gm, long force) {
                this.chrId = chrId;
                this.level = level;
                this.job = job;
                this.gm = gm;
                this.force = force;
            }

            public int getChrId() {
                return this.chrId;
            }

            public void setChrId(int chrId) {
                this.chrId = chrId;
            }

            public long getForce() {
                return this.force;
            }

            public void setForce(int force) {
                this.force = (long)force;
            }

            public short getLevel() {
                return this.level;
            }

            public void setLevel(short level) {
                this.level = level;
            }

            public short getJob() {
                return this.job;
            }

            public void setJob(short job) {
                this.job = job;
            }
        }
    }
}
