package gui;


import database.DBConPool;
import gui.服务端输出信息;
import java.awt.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMap;
import snail.TimeLogCenter;

public class WorldBoss {
    private static short nowStage;
    private static short maxStage;
    private static Map<Short, Boss> bossMap = new HashMap();

    public WorldBoss() {
    }

    public static void main(String[] args) throws ClassNotFoundException {
        loadFromDB();
    }

    public static boolean loadFromDB() {
        服务端输出信息.println_out("【世界BOSS】开始从数据库的读取BOSS信息。。。");
        bossMap.clear();
        maxStage = 0;
        nowStage = 1;

        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_world_boss ORDER BY stage");
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    loadBoss(rs.getShort("stage"), rs.getInt("mobid"), rs.getLong("mobhp"), rs.getLong("mobmaxhp"), rs.getInt("itemid"), rs.getInt("itemquantity"), rs.getInt("itemquantityneed"), rs.getShort("spawned") > 0, rs.getShort("killed") > 0, rs.getInt("killerid"), rs.getShort("finalrewarded") > 0);
                }

                Iterator var4 = bossMap.entrySet().iterator();

                while (true) {
                    if (var4.hasNext()) {
                        Map.Entry<Short, Boss> entry = (Map.Entry) var4.next();
                        if (((Boss) entry.getValue()).isSpawned() && !((Boss) entry.getValue()).isKilled()) {
                            nowStage = (Short) entry.getKey();
                        } else {
                            if (((Boss) entry.getValue()).isSpawned()) {
                                continue;
                            }

                            nowStage = (Short) entry.getKey();
                        }
                    }

                    服务端输出信息.println_out("【世界BOSS】读取成功");
                    boolean var17 = true;
                    return var17;
                }
            } catch (Throwable var14) {
                var1 = var14;
                throw var14;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var13) {
                            var1.addSuppressed(var13);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var16) {
            服务端输出信息.println_err("【错误】loadFromDB错误，错误原因：" + var16);
            var16.printStackTrace();
            return false;
        }
    }

    public static boolean saveToDB() {
        if (bossMap.isEmpty()) {
            return false;
        } else {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var1 = null;

                boolean var16;
                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM snail_world_boss");
                    ps.executeUpdate();
                    ps = con.prepareStatement("ALTER TABLE snail_world_boss auto_increment = 1");
                    ps.executeUpdate();
                    ps = con.prepareStatement("INSERT INTO snail_world_boss (stage, mobid, mobhp, mobmaxhp, itemid, itemquantity, itemquantityneed, spawned, killed, killerid, finalrewarded) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    for (Iterator var3 = bossMap.entrySet().iterator(); var3.hasNext(); ps.executeUpdate()) {
                        Map.Entry<Short, Boss> entry = (Map.Entry) var3.next();
                        ps.setShort(1, (Short) entry.getKey());
                        ps.setInt(2, ((Boss) entry.getValue()).getMobId());
                        ps.setLong(3, ((Boss) entry.getValue()).getMobHp());
                        ps.setLong(4, ((Boss) entry.getValue()).getMobMaxHp());
                        ps.setInt(5, ((Boss) entry.getValue()).getItemId());
                        ps.setInt(6, ((Boss) entry.getValue()).getItemQuantity());
                        ps.setInt(7, ((Boss) entry.getValue()).getItemQuantityNeed());
                        if (((Boss) entry.getValue()).isSpawned()) {
                            ps.setShort(8, (short) 1);
                        } else {
                            ps.setShort(8, (short) 0);
                        }

                        if (((Boss) entry.getValue()).isKilled()) {
                            ps.setShort(9, (short) 1);
                        } else {
                            ps.setShort(9, (short) 0);
                        }

                        ps.setInt(10, ((Boss) entry.getValue()).getKillerId());
                        if (((Boss) entry.getValue()).isFinalRewarded()) {
                            ps.setShort(11, (short) 1);
                        } else {
                            ps.setShort(11, (short) 0);
                        }
                    }

                    var16 = true;
                } catch (Throwable var13) {
                    var1 = var13;
                    throw var13;
                } finally {
                    if (con != null) {
                        if (var1 != null) {
                            try {
                                con.close();
                            } catch (Throwable var12) {
                                var1.addSuppressed(var12);
                            }
                        } else {
                            con.close();
                        }
                    }

                }

                return var16;
            } catch (SQLException var15) {
                服务端输出信息.println_err("【错误】saveToDB错误，错误原因：" + var15);
                var15.printStackTrace();
                return false;
            }
        }
    }

    public static void reset() {
        for (short i = 1; i <= bossMap.size(); ++i) {
            clearRewarded(i);
        }

        bossMap.clear();
        maxStage = 0;
        nowStage = 1;
    }

    public static boolean contains(MapleMonster mob) {
        if (bossMap.isEmpty()) {
            return false;
        } else {
            Iterator var1 = bossMap.entrySet().iterator();

            Map.Entry entry;
            do {
                if (!var1.hasNext()) {
                    return false;
                }

                entry = (Map.Entry) var1.next();
            } while (((Boss) entry.getValue()).getMonster() != mob);

            return true;
        }
    }

    public static boolean addBoss(int mobId, long mobHp, int itemId, int itemQuantityNeed) {
        try {
            bossMap.put((short) (maxStage + 1), new Boss((short) (maxStage + 1), mobId, mobHp, itemId, itemQuantityNeed));
            ++maxStage;
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】addBoss错误，错误原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    private static boolean loadBoss(short stage, int mobId, long mobHp, long mobMaxHp, int itemId, int itemQuantity, int itemQuantityNeed, boolean spawned, boolean killed, int killerId, boolean finalRewarded) {
        try {
            bossMap.put(stage, new Boss(stage, mobId, mobHp, mobMaxHp, itemId, itemQuantity, itemQuantityNeed, spawned, killed, killerId, finalRewarded));
            ++maxStage;
            return true;
        } catch (Exception var14) {
            服务端输出信息.println_err("【错误】loadBoss错误，错误原因：" + var14);
            var14.printStackTrace();
            return false;
        }
    }

    public static short getNowStage() {
        return nowStage;
    }

    public static short getMaxStage() {
        return maxStage;
    }

    public static Boss getNowBoss() {
        return !bossMap.isEmpty() && nowStage <= bossMap.size() ? (Boss) bossMap.get(nowStage) : null;
    }

    public static Boss getBoss(short stage) {
        return !bossMap.isEmpty() && stage <= bossMap.size() ? (Boss) bossMap.get(stage) : null;
    }

    public static long getMaxHp() {
        Boss boss = getNowBoss();
        return boss != null ? boss.getMobMaxHp() : 0L;
    }

    public static boolean addItem(int quantity) {
        Boss boss = getNowBoss();
        if (boss != null) {
            boss.setItemQuantity(boss.getItemQuantity() + quantity);
            return true;
        } else {
            return false;
        }
    }

    public static int getItemId() {
        Boss boss = getNowBoss();
        return boss != null ? boss.getItemId() : 0;
    }

    public static int getItemQuantity() {
        Boss boss = getNowBoss();
        return boss != null ? boss.getItemQuantity() : 0;
    }

    public static int getItemQuantityNeed() {
        Boss boss = getNowBoss();
        return boss != null ? boss.getItemQuantityNeed() : 0;
    }

    public static boolean canSpawn() {
        Boss boss = getNowBoss();
        return boss != null && !boss.isSpawned() && boss.getItemQuantity() >= boss.getItemQuantityNeed();
    }

    public static boolean isSpawned() {
        Boss boss = getNowBoss();
        return boss != null ? boss.isSpawned() : false;
    }

    public static boolean spawn(MapleMap map, int x, int y) {
        Boss boss = getNowBoss();
        if (boss != null && map != null) {
            map.spawnMonsterOnGroundBelow(boss.getMonster(), new Point(x, y));
            boss.setSpawned(true);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isKilled() {
        Boss boss = getNowBoss();
        return boss != null && boss.isKilled();
    }

    public static boolean isKilled(short stage) {
        Boss boss = getBoss(stage);
        return boss != null ? boss.isKilled() : false;
    }

    public static boolean recordKill(int killerId) {
        Boss boss = getNowBoss();
        if (boss != null && !boss.isKilled()) {
            boss.setKilled(true);
            boss.setSpawned(true);
            boss.setKillerId(killerId);
            if (nowStage < maxStage) {
                ++nowStage;
            }

            return true;
        } else {
            return false;
        }
    }

    public static int getKillerId(short stage) {
        return bossMap != null && stage <= bossMap.size() ? ((Boss) bossMap.get(stage)).getKillerId() : 0;
    }

    public static boolean isFinalRewarded() {
        Boss boss = getNowBoss();
        return boss != null && boss.isFinalRewarded();
    }

    public static boolean isFinalRewarded(short stage) {
        Boss boss = getBoss(stage);
        return boss != null && boss.isFinalRewarded();
    }

    public static boolean setFinalRewarded(boolean finalRewarded) {
        Boss boss = getNowBoss();
        if (boss != null) {
            boss.setFinalRewarded(finalRewarded);
            return true;
        } else {
            return false;
        }
    }

    public static boolean setFinalRewarded(boolean finalRewarded, short stage) {
        Boss boss = getBoss(stage);
        if (boss != null) {
            boss.setFinalRewarded(finalRewarded);
            return true;
        } else {
            return false;
        }
    }

    public static boolean isRewarded(int chrId, short stage) {
        Boss boss = getBoss(stage);
        if (boss != null && boss.isKilled()) {
            String log = "世界BOSS阶段" + stage + "奖励";
            int ret_count = TimeLogCenter.getInstance().getOneTimeLog(chrId, log);
            return ret_count > 0;
        } else {
            return false;
        }
    }

    public static boolean setRewarded(int chrId, short stage) {
        Boss boss = getBoss(stage);
        if (boss != null && boss.isKilled()) {
            String log = "世界BOSS阶段" + stage + "奖励";
            TimeLogCenter.getInstance().setOneTimeLog(chrId, log);
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteRewarded(int chrId, short stage) {
        String log = "世界BOSS阶段" + stage + "奖励";
        return TimeLogCenter.getInstance().deleteOneTimeLogAll(chrId, log);
    }

    public static boolean clearRewarded(short stage) {
        String log = "世界BOSS阶段" + stage + "奖励";
        TimeLogCenter.getInstance().deleteOneTimeLogaAll(log);
        return TimeLogCenter.getInstance().deleteOneTimeLogAll(log);
    }

    public static class Boss {
        private short stage;
        private int mobId;
        private int itemId;
        private int itemQuantity;
        private int itemQuantityNeed;
        private int killerId;
        private long maxHp;
        private boolean spawned;
        private boolean killed;
        private boolean finalRewarded;
        private MapleMonster mob;

        public Boss(short stage, int mobId, long mobHp, int itemId, int itemQuantityNeed) {
            this.stage = stage;
            this.mobId = mobId;
            this.itemId = itemId;
            this.itemQuantityNeed = itemQuantityNeed;
            this.maxHp = mobHp;
            this.mob = MapleLifeFactory.getMonster(mobId);
            this.mob.setHp(mobHp);
            this.spawned = false;
            this.killed = false;
            this.killerId = 0;
            this.itemQuantity = 0;
            this.finalRewarded = false;
        }

        public Boss(short stage, int mobId, long mobHp, long mobMaxHp, int itemId, int itemQuantity, int itemQuantityNeed, boolean spawned, boolean killed, int killerId, boolean finalRewarded) {
            this.stage = stage;
            this.mobId = mobId;
            this.maxHp = mobMaxHp;
            this.itemId = itemId;
            this.itemQuantity = itemQuantity;
            this.itemQuantityNeed = itemQuantityNeed;
            this.mob = MapleLifeFactory.getMonster(mobId);
            this.mob.setHp(mobHp);
            this.spawned = spawned;
            this.killed = killed;
            this.killerId = killerId;
            this.finalRewarded = finalRewarded;
        }

        public boolean isSpawned() {
            return this.spawned;
        }

        public boolean isKilled() {
            return this.killed;
        }

        public boolean isFinalRewarded() {
            return this.finalRewarded;
        }

        public int getMobId() {
            return this.mobId;
        }

        public long getMobHp() {
            return this.mob.getHp();
        }

        public long getMobMaxHp() {
            return this.maxHp;
        }

        public int getItemId() {
            return this.itemId;
        }

        public int getItemQuantity() {
            return this.itemQuantity;
        }

        public int getItemQuantityNeed() {
            return this.itemQuantityNeed;
        }

        public int getKillerId() {
            return this.killerId;
        }

        public short getStage() {
            return this.stage;
        }

        public MapleMonster getMonster() {
            return this.mob;
        }

        public void setKilled(boolean killed) {
            this.killed = killed;
        }

        public void setSpawned(boolean spawned) {
            this.spawned = spawned;
        }

        public void setFinalRewarded(boolean finalRewarded) {
            this.finalRewarded = finalRewarded;
        }

        public void setKillerId(int killerId) {
            this.killerId = killerId;
        }

        public void setItemQuantity(int itemQuantity) {
            this.itemQuantity = itemQuantity;
        }
    }

}
