//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import database.DBConPool;
import gui.服务端输出信息;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimeLogCenter {
    private static TimeLogCenter timeLogCenter = new TimeLogCenter();
    private static List<BossLog> bossLogList = Collections.synchronizedList(new ArrayList());
    private static List<BossLoga> bossLogaList = Collections.synchronizedList(new ArrayList());
    private static List<OneTimeLog> oneTimeLogList = Collections.synchronizedList(new ArrayList());
    private static List<OneTimeLoga> oneTimeLogaList = Collections.synchronizedList(new ArrayList());
    private static List<WeekLog> weekLogList = Collections.synchronizedList(new ArrayList());
    private static List<WeekLoga> weekLogaList = Collections.synchronizedList(new ArrayList());
    private static List<MonthLog> monthLogList = Collections.synchronizedList(new ArrayList());
    private static List<MonthLoga> monthLogaList = Collections.synchronizedList(new ArrayList());
    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public TimeLogCenter() {
    }

    public static TimeLogCenter getInstance() {
        return timeLogCenter;
    }

    public void saveAllLogs() {
        服务端输出信息.println_out("【开始保存Log】...");
        this.saveBossLogToDB();
        this.saveBossLogaToDB();
        this.saveOneTimeLogToDB();
        this.saveOneTimeLogaToDB();
        this.saveWeekLogToDB();
        this.saveWeekLogaToDB();
        服务端输出信息.println_out("【Log保存成功】");
    }

    public void loadBossLogFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from bosslog");
                ResultSet rs = ps.executeQuery();
                ArrayList<BossLog> bossLogList0 = new ArrayList();
                String logName = "";
                Boolean find = false;

                while(rs.next()) {
                    int chrId = rs.getInt("characterid");
                    int count = rs.getInt("count");
                    logName = rs.getString("bossid");
                    find = false;
                    Iterator var10 = bossLogList0.iterator();

                    while(var10.hasNext()) {
                        BossLog log = (BossLog)var10.next();
                        if (log != null && log.getChrId() == chrId && log.getBossId().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        bossLogList0.add(new BossLog(chrId, logName, count));
                    }
                }

                bossLogList.clear();
                bossLogList = bossLogList0;
                ps.close();
                rs.close();
            } catch (Throwable var20) {
                var2 = var20;
                throw var20;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var19) {
                            var2.addSuppressed(var19);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var22) {
            服务端输出信息.println_err("【错误】loadBossLogFromDB错误，错误原因：" + var22);
            var22.printStackTrace();
        }

    }

    public void saveBossLogToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM bosslog");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE bosslog auto_increment=1");
                ps.executeUpdate();
                ArrayList<BossLog> bossLogList0 = new ArrayList(bossLogList);
                Iterator var5 = bossLogList0.iterator();

                while(var5.hasNext()) {
                    BossLog bossLog = (BossLog)var5.next();
                    if (bossLog != null) {
                        ps = con.prepareStatement("INSERT INTO bosslog (characterid, bossid, count, lastattempt) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, bossLog.getChrId());
                        ps.setString(2, bossLog.getBossId());
                        ps.setInt(3, bossLog.getCount());
                        ps.setTimestamp(4, new Timestamp(bossLog.getDate().getTime()));
                        ps.executeUpdate();
                    }
                }

                bossLogList0.clear();
                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveBossLogToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<BossLog> getBossLogList() {
        return bossLogList;
    }

    public void clearBossLogList() {
        bossLogList.clear();
    }

    public int getBossLog(int chrId, String bossId) {
        int count = 0;
        ArrayList<BossLog> bossLogList0 = new ArrayList<>(bossLogList);
        Iterator var5 = bossLogList0.iterator();

        while(var5.hasNext()) {
            BossLog bossLog = (BossLog)var5.next();
            if (bossLog != null && bossLog.getChrId() == chrId && bossLog.getBossId().equals(bossId)) {
                count += bossLog.getCount();
            }
        }

        bossLogList0.clear();
        return count;
    }

    public void setBossLog(final int chrId, final String bossId, final int count) {
        boolean find = false;
        Iterator var5 = bossLogList.iterator();

        while(var5.hasNext()) {
            BossLog log = (BossLog)var5.next();
            if (log != null && log.getChrId() == chrId && log.getBossId().equals(bossId)) {
                log.setCount(log.getCount() + count);
                find = true;
                break;
            }
        }

        if (!find) {
            bossLogList.add(new BossLog(chrId, bossId, count));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM bosslog WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, bossId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE bosslog SET count = ? WHERE bosslogid = ?");
                            ps.setInt(1, TimeLogCenter.this.getBossLog(chrId, bossId));
                            ps.setInt(2, rs.getInt("bosslogid"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into bosslog (characterid, bossid, `count`) values (?,?,?)");
                            ps.setInt(1, chrId);
                            ps.setString(2, bossId);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setBossLog(final int chrId, final String bossId, int count)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteBossLogAll(final int chrId, final String bossId) {
        try {
            int size = bossLogList.size();

            for(int i = 0; i < size; ++i) {
                BossLog bossLog = (BossLog)bossLogList.get(i);
                if (bossLog != null && bossLog.getChrId() == chrId && bossLog.getBossId().equals(bossId)) {
                    bossLogList.remove(i);
                    --i;
                    size = bossLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bosslog WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, bossId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogAll(final int chrId, final String bossId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteBossLogAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteBossLogAll(final String bossId) {
        try {
            int size = bossLogList.size();

            for(int i = 0; i < size; ++i) {
                BossLog bossLog = (BossLog)bossLogList.get(i);
                if (bossLog != null && bossLog.getBossId().equals(bossId)) {
                    bossLogList.remove(i);
                    --i;
                    size = bossLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bosslog WHERE bossid = ?");
                        ps.setString(1, bossId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogAll(final String bossId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteBossLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteBossLogAll(final int chrId) {
        try {
            int size = bossLogList.size();

            for(int i = 0; i < size; ++i) {
                BossLog bossLog = (BossLog)bossLogList.get(i);
                if (bossLog != null && bossLog.getChrId() == chrId) {
                    bossLogList.remove(i);
                    --i;
                    size = bossLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bosslog WHERE characterid = ?");
                        ps.setInt(1, chrId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogAll(final int chrId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteBossLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteBossLogAll() {
        try {
            this.clearBossLogList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bosslog");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE bosslog auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteBossLogAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadBossLogaFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from bossloga");
                ResultSet rs = ps.executeQuery();
                ArrayList<BossLoga> bossLogaList0 = new ArrayList();
                String logName = "";
                Boolean find = false;

                while(rs.next()) {
                    int accountId = rs.getInt("accountid");
                    int count = rs.getInt("count");
                    logName = rs.getString("bossid");
                    find = false;
                    Iterator var10 = bossLogaList0.iterator();

                    while(var10.hasNext()) {
                        BossLoga log = (BossLoga)var10.next();
                        if (log != null && log.getAccountId() == accountId && log.getBossId().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        bossLogaList0.add(new BossLoga(accountId, logName, count));
                    }
                }

                bossLogaList.clear();
                bossLogaList = bossLogaList0;
                ps.close();
                rs.close();
            } catch (Throwable var20) {
                var2 = var20;
                throw var20;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var19) {
                            var2.addSuppressed(var19);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var22) {
            服务端输出信息.println_err("【错误】loadBossLogaFromDB错误，错误原因：" + var22);
            var22.printStackTrace();
        }

    }

    public void saveBossLogaToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM bossloga");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE bossloga auto_increment=1");
                ps.executeUpdate();
                ArrayList<BossLoga> bossLogaList0 = new ArrayList(bossLogaList);
                Iterator var5 = bossLogaList0.iterator();

                while(var5.hasNext()) {
                    BossLoga bossLoga = (BossLoga)var5.next();
                    if (bossLoga != null) {
                        ps = con.prepareStatement("INSERT INTO bossloga (accountid, bossid, lastattempt) VALUES (?, ?, ?)");
                        ps.setInt(1, bossLoga.getAccountId());
                        ps.setString(2, bossLoga.getBossId());
                        ps.setTimestamp(3, new Timestamp(bossLoga.getDate().getTime()));
                        ps.executeUpdate();
                    }
                }

                bossLogaList0.clear();
                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveBossLogaToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<BossLoga> getBossLogaList() {
        return bossLogaList;
    }

    public void clearBossLogaList() {
        bossLogaList.clear();
    }

    public int getBossLoga(int accountId, String bossId) {
        int count = 0;
        ArrayList<BossLoga> bossLogaList0 = new ArrayList(bossLogaList);
        Iterator var5 = bossLogaList0.iterator();

        while(var5.hasNext()) {
            BossLoga bossLoga = (BossLoga)var5.next();
            if (bossLoga != null && bossLoga.getAccountId() == accountId && bossLoga.getBossId().equals(bossId)) {
                count += bossLoga.getCount();
            }
        }

        bossLogaList0.clear();
        return count;
    }

    public void setBossLoga(final int accountId, final String bossId, final int count) {
        boolean find = false;
        Iterator var5 = bossLogaList.iterator();

        while(var5.hasNext()) {
            BossLoga log = (BossLoga)var5.next();
            if (log != null && log.getAccountId() == accountId && log.getBossId().equals(bossId)) {
                log.setCount(log.getCount() + count);
                find = true;
                break;
            }
        }

        if (!find) {
            bossLogaList.add(new BossLoga(accountId, bossId, count));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM bossloga WHERE accountid = ? AND bossid = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, bossId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE bossloga SET `count` = ? WHERE bosslogid = ?");
                            ps.setInt(1, TimeLogCenter.this.getBossLoga(accountId, bossId));
                            ps.setInt(2, rs.getInt("bosslogid"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into bossloga (accountid, bossid, `count`) values (?,?,?)");
                            ps.setInt(1, accountId);
                            ps.setString(2, bossId);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                        rs.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setBossLoga(final int accountId, final String bossId, int count)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteBossLogaAll(final int accountId, final String bossId) {
        try {
            int size = bossLogaList.size();

            for(int i = 0; i < size; ++i) {
                BossLoga bossLoga = (BossLoga)bossLogaList.get(i);
                if (bossLoga != null && bossLoga.getAccountId() == accountId && bossLoga.getBossId().equals(bossId)) {
                    bossLogaList.remove(i);
                    --i;
                    size = bossLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bossloga WHERE accountid = ? AND bossid = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, bossId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogaAll(final int accountId, final String bossId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteBossLogaAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteBossLogaAll(final String bossId) {
        try {
            int size = bossLogaList.size();

            for(int i = 0; i < size; ++i) {
                BossLoga bossLoga = (BossLoga)bossLogaList.get(i);
                if (bossLoga != null && bossLoga.getBossId().equals(bossId)) {
                    bossLogaList.remove(i);
                    --i;
                    size = bossLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bossloga WHERE bossid = ?");
                        ps.setString(1, bossId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogaAll(String bossId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteBossLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteBossLogaAll(final int accountId) {
        try {
            int size = bossLogaList.size();

            for(int i = 0; i < size; ++i) {
                BossLoga bossLoga = (BossLoga)bossLogaList.get(i);
                if (bossLoga != null && bossLoga.getAccountId() == accountId) {
                    bossLogaList.remove(i);
                    --i;
                    size = bossLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bossloga WHERE accountid = ?");
                        ps.setInt(1, accountId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogaAll(final int accountId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteBossLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteBossLogaAll() {
        try {
            this.clearBossLogaList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM bossloga");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE bossloga auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteBossLogaAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteBossLogaAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadOneTimeLogFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from onetimelog");
                ResultSet rs = ps.executeQuery();
                ArrayList<OneTimeLog> oneTimeLogList0 = new ArrayList();
                String logName = "";
                Boolean find = false;

                while(rs.next()) {
                    int chrId = rs.getInt("characterid");
                    int count = rs.getInt("count");
                    logName = rs.getString("log");
                    find = false;
                    Iterator var10 = oneTimeLogList0.iterator();

                    while(var10.hasNext()) {
                        OneTimeLog log = (OneTimeLog)var10.next();
                        if (log != null && log.getChrId() == chrId && log.getLogName().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        oneTimeLogList0.add(new OneTimeLog(chrId, logName, count));
                    }
                }

                oneTimeLogList.clear();
                oneTimeLogList = oneTimeLogList0;
                ps.close();
                rs.close();
            } catch (Throwable var20) {
                var2 = var20;
                throw var20;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var19) {
                            var2.addSuppressed(var19);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var22) {
            服务端输出信息.println_err("【错误】loadOneTimeLogFromDB错误，错误原因：" + var22);
            var22.printStackTrace();
        }

    }

    public void saveOneTimeLogToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM onetimelog");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE onetimelog auto_increment=1");
                ps.executeUpdate();
                ArrayList<OneTimeLog> oneTimeLogList0 = new ArrayList(oneTimeLogList);
                Iterator var5 = oneTimeLogList0.iterator();

                while(var5.hasNext()) {
                    OneTimeLog oneTimeLog = (OneTimeLog)var5.next();
                    if (oneTimeLog != null) {
                        ps = con.prepareStatement("INSERT INTO onetimelog (characterid, log, count) VALUES (?, ?, ?)");
                        ps.setInt(1, oneTimeLog.getChrId());
                        ps.setString(2, oneTimeLog.getLogName());
                        ps.setInt(3, oneTimeLog.getCount());
                        ps.executeUpdate();
                    }
                }

                oneTimeLogList0.clear();
                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveOneTimeLogToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<OneTimeLog> getOneTimeLogList() {
        return oneTimeLogList;
    }

    public void clearOneTimeLogList() {
        oneTimeLogList.clear();
    }

    public int getOneTimeLog(int chrId, String logName) {
        int count = 0;

        for (OneTimeLog oneTimeLog : oneTimeLogList) {
            if (oneTimeLog != null && oneTimeLog.getChrId() == chrId && oneTimeLog.getLogName().equals(logName)) {
                count = oneTimeLog.getCount();
                break;
            }
        }
        return count;
    }

    public void setOneTimeLog(int chrId, String logName) {
        this.setOneTimeLog(chrId, logName, 1);
    }

    public void setOneTimeLog(final int chrId, final String logName, final int count) {
        boolean find = false;
        for (OneTimeLog log : oneTimeLogList) {
            if (log != null && log.getChrId() == chrId && log.getLogName().equals(logName)) {
                log.setCount(log.getCount() + count);
                find = true;
                break;
            }
        }

        if (!find) {
            oneTimeLogList.add(new OneTimeLog(chrId, logName, count));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM onetimelog WHERE characterid = ? AND log = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, logName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE onetimelog SET count = ? WHERE id = ?");
                            ps.setInt(1, TimeLogCenter.this.getOneTimeLog(chrId, logName));
                            ps.setInt(2, rs.getInt("id"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into onetimelog (characterid, log, count) values (?,?,?)");
                            ps.setInt(1, chrId);
                            ps.setString(2, logName);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setOneTimeLog(final int chrId, final String logName, final int count)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteOneTimeLogAll(final int chrId, final String logName) {
        try {
            int size = oneTimeLogList.size();

            for(int i = 0; i < size; ++i) {
                OneTimeLog oneTimeLog = (OneTimeLog)oneTimeLogList.get(i);
                if (oneTimeLog != null && oneTimeLog.getChrId() == chrId && oneTimeLog.getLogName().equals(logName)) {
                    oneTimeLogList.remove(i);
                    --i;
                    size = oneTimeLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimelog WHERE characterid = ? AND log = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogAll(final int chrId, final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteOneTimeLogAll(final String logName) {
        try {
            int size = oneTimeLogList.size();

            for(int i = 0; i < size; ++i) {
                OneTimeLog oneTimeLog = (OneTimeLog)oneTimeLogList.get(i);
                if (oneTimeLog != null && oneTimeLog.getLogName().equals(logName)) {
                    oneTimeLogList.remove(i);
                    --i;
                    size = oneTimeLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimelog WHERE log = ?");
                        ps.setString(1, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogAll(final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteOneTimeLogAll(final int chrId) {
        try {
            int size = oneTimeLogList.size();

            for(int i = 0; i < size; ++i) {
                OneTimeLog oneTimeLog = (OneTimeLog)oneTimeLogList.get(i);
                if (oneTimeLog != null && oneTimeLog.getChrId() == chrId) {
                    oneTimeLogList.remove(i);
                    --i;
                    size = oneTimeLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimelog WHERE characterid = ?");
                        ps.setInt(1, chrId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogAll(final int chrId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteOneTimeLogAll() {
        try {
            this.clearOneTimeLogList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimelog");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE onetimelog auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadOneTimeLogaFromDB() {
        ArrayList<OneTimeLoga> oneTimeLogaList0 = new ArrayList();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from onetimeloga");
                ResultSet rs = ps.executeQuery();
                String logName = "";
                boolean find = false;

                while(rs.next()) {
                    int accountId = rs.getInt("accid");
                    int count = rs.getInt("count");
                    logName = rs.getString("log");
                    find = false;
                    Iterator var10 = oneTimeLogaList0.iterator();

                    while(var10.hasNext()) {
                        OneTimeLoga log = (OneTimeLoga)var10.next();
                        if (log != null && log.getAccountId() == accountId && log.getLogName().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        oneTimeLogaList0.add(new OneTimeLoga(accountId, logName, count));
                    }
                }

                oneTimeLogaList.clear();
                oneTimeLogaList = oneTimeLogaList0;
                ps.close();
                rs.close();
            } catch (Throwable var20) {
                var3 = var20;
                throw var20;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var19) {
                            var3.addSuppressed(var19);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var22) {
            服务端输出信息.println_err("【错误】loadOneTimeLogaFromDB错误，错误原因：" + var22);
            var22.printStackTrace();
        }

    }

    public void saveOneTimeLogaToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM OneTimeLoga");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE OneTimeLoga auto_increment=1");
                ps.executeUpdate();
                ArrayList<OneTimeLoga> oneTimeLogaList0 = new ArrayList(oneTimeLogaList);
                Iterator var5 = oneTimeLogaList0.iterator();

                while(var5.hasNext()) {
                    OneTimeLoga OneTimeLoga = (OneTimeLoga)var5.next();
                    if (OneTimeLoga != null) {
                        ps = con.prepareStatement("INSERT INTO OneTimeLoga (accid, log, count) VALUES (?, ?, ?)");
                        ps.setInt(1, OneTimeLoga.getAccountId());
                        ps.setString(2, OneTimeLoga.getLogName());
                        ps.setInt(3, OneTimeLoga.getCount());
                        ps.executeUpdate();
                    }
                }

                oneTimeLogaList0.clear();
                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveOneTimeLogaToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<OneTimeLoga> getOneTimeLogaList() {
        return oneTimeLogaList;
    }

    public void clearOneTimeLogaList() {
        oneTimeLogaList.clear();
    }

    public int getOneTimeLoga(int accountId, String logName) {
        int count = 0;
        ArrayList<OneTimeLoga> oneTimeLogaList0 = new ArrayList(oneTimeLogaList);
        Iterator var5 = oneTimeLogaList0.iterator();

        while(var5.hasNext()) {
            OneTimeLoga oneTimeLoga = (OneTimeLoga)var5.next();
            if (oneTimeLoga != null && oneTimeLoga.getAccountId() == accountId && oneTimeLoga.getLogName().equals(logName)) {
                count = oneTimeLoga.getCount();
                break;
            }
        }

        oneTimeLogaList0.clear();
        return count;
    }

    public void setOneTimeLoga(int accountId, String logName) {
        this.setOneTimeLoga(accountId, logName, 1);
    }

    public void setOneTimeLoga(final int accountId, final String logName, final int count) {
        boolean find = false;
        Iterator var5 = oneTimeLogaList.iterator();

        while(var5.hasNext()) {
            OneTimeLoga log = (OneTimeLoga)var5.next();
            if (log != null && log.getAccountId() == accountId && log.getLogName().equals(logName)) {
                log.setCount(log.getCount() + count);
                find = true;
                break;
            }
        }

        if (!find) {
            oneTimeLogaList.add(new OneTimeLoga(accountId, logName, count));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM onetimeloga WHERE accid = ? AND log = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, logName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE onetimeloga SET count = ? WHERE id = ?");
                            ps.setInt(1, TimeLogCenter.this.getOneTimeLoga(accountId, logName));
                            ps.setInt(2, rs.getInt("id"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into onetimeloga (accid, log, count) values (?,?,?)");
                            ps.setInt(1, accountId);
                            ps.setString(2, logName);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setOneTimeLoga(final int accountId, final String logName, final int count)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteOneTimeLogaAll(final int accountId, final String logName) {
        try {
            int size = oneTimeLogaList.size();

            for(int i = 0; i < size; ++i) {
                OneTimeLoga oneTimeLoga = (OneTimeLoga)oneTimeLogaList.get(i);
                if (oneTimeLoga != null && oneTimeLoga.getAccountId() == accountId && oneTimeLoga.getLogName().equals(logName)) {
                    oneTimeLogaList.remove(i);
                    --i;
                    size = oneTimeLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimeloga WHERE accid = ? AND log = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogaAll(final int accountId, final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogaAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteOneTimeLogaAll(final String logName) {
        try {
            int size = oneTimeLogaList.size();

            for(int i = 0; i < size; ++i) {
                OneTimeLoga oneTimeLoga = (OneTimeLoga)oneTimeLogaList.get(i);
                if (oneTimeLoga != null && oneTimeLoga.getLogName().equals(logName)) {
                    oneTimeLogaList.remove(i);
                    --i;
                    size = oneTimeLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimeloga WHERE log = ?");
                        ps.setString(1, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogaAll(final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteOneTimeLogaAll(final int accountId) {
        try {
            int size = oneTimeLogaList.size();

            for(int i = 0; i < size; ++i) {
                OneTimeLoga oneTimeLoga = (OneTimeLoga)oneTimeLogaList.get(i);
                if (oneTimeLoga != null && oneTimeLoga.getAccountId() == accountId) {
                    oneTimeLogaList.remove(i);
                    --i;
                    size = oneTimeLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimeloga WHERE accid = ?");
                        ps.setInt(1, accountId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogaAll(final int accountId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteOneTimeLogaAll() {
        try {
            this.clearOneTimeLogaList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM onetimeloga");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE onetimeloga auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteOneTimeLogaAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteOneTimeLogaAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadWeekLogFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from snail_weeklog");
                ResultSet rs = ps.executeQuery();
                ArrayList<WeekLog> weekLogList0 = new ArrayList();
                String logName = "";
                boolean find = false;

                while(rs.next()) {
                    find = false;
                    int chrId = rs.getInt("characterid");
                    logName = rs.getString("bossid");
                    int count = rs.getInt("count");
                    Date date = new Date(rs.getTimestamp("lastattempt").getTime());
                    Iterator var11 = weekLogList0.iterator();

                    while(var11.hasNext()) {
                        WeekLog log = (WeekLog)var11.next();
                        if (log != null && log.getChrId() == chrId && log.getLogName().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            log.setDate(date);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        weekLogList0.add(new WeekLog(chrId, logName, count, date));
                    }
                }

                weekLogList.clear();
                weekLogList = weekLogList0;
                ps.close();
                rs.close();
            } catch (Throwable var21) {
                var2 = var21;
                throw var21;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var20) {
                            var2.addSuppressed(var20);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var23) {
            服务端输出信息.println_err("【错误】loadWeekLogFromDB错误，错误原因：" + var23);
            var23.printStackTrace();
        }

    }

    public void saveWeekLogToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weeklog");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE snail_weeklog auto_increment=1");
                ps.executeUpdate();
                ArrayList<WeekLog> weekLogList0 = new ArrayList(weekLogList);
                Iterator var5 = weekLogList0.iterator();

                while(var5.hasNext()) {
                    WeekLog weekLog = (WeekLog)var5.next();
                    if (weekLog != null) {
                        ps = con.prepareStatement("INSERT INTO snail_weeklog (characterid, bossid, count, lastattempt) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, weekLog.getChrId());
                        ps.setString(2, weekLog.getLogName());
                        ps.setInt(3, weekLog.getCount());
                        ps.setTimestamp(4, new Timestamp(weekLog.getDate().getTime()));
                        ps.executeUpdate();
                    }
                }

                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveWeekLogToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<WeekLog> getWeekLogList() {
        return weekLogList;
    }

    public void clearWeekLogList() {
        weekLogList.clear();
    }

    public int getWeekLog(int chrId, String logName) {
        ArrayList<WeekLog> weekLogList0 = new ArrayList(weekLogList);
        Iterator var4 = weekLogList0.iterator();

        WeekLog weekLog;
        do {
            if (!var4.hasNext()) {
                weekLogList0.clear();
                return 0;
            }

            weekLog = (WeekLog)var4.next();
        } while(weekLog == null || weekLog.getChrId() != chrId || !weekLog.getLogName().equals(logName));

        return weekLog.getCount();
    }

    public void setWeekLog(int chrId, String logName) {
        this.setWeekLog(chrId, logName, 1);
    }

    public void setWeekLog(int chrId, String logName, int count) {
        this.setWeekLog(chrId, logName, count, new Date());
    }

    public void setWeekLog(final int chrId, final String logName, final int count, Date date) {
        boolean find = false;
        Iterator var6 = weekLogList.iterator();

        while(var6.hasNext()) {
            WeekLog log = (WeekLog)var6.next();
            if (log != null && log.getChrId() == chrId && log.getLogName().equals(logName)) {
                log.setCount(log.getCount() + count);
                log.setDate(date);
                find = true;
                break;
            }
        }

        if (!find) {
            weekLogList.add(new WeekLog(chrId, logName, count, date));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_weeklog WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, logName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE snail_weeklog SET count = ?  WHERE id = ?");
                            ps.setInt(1, TimeLogCenter.this.getWeekLog(chrId, logName));
                            ps.setInt(2, rs.getInt("id"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into snail_weeklog (characterid, bossid, count) values (?,?,?)");
                            ps.setInt(1, chrId);
                            ps.setString(2, logName);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setWeekLog(final int chrId, final String logName, final int count, final Date date)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteWeekLogAll(final int chrId, final String logName) {
        try {
            int size = weekLogList.size();

            for(int i = 0; i < size; ++i) {
                WeekLog weekLog = (WeekLog)weekLogList.get(i);
                if (weekLog != null && weekLog.getChrId() == chrId && weekLog.getLogName().equals(logName)) {
                    weekLogList.remove(i);
                    --i;
                    size = weekLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weeklog WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogAll(final int chrId, final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteWeekLogAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteWeekLogAll(final String logName) {
        try {
            int size = weekLogList.size();

            for(int i = 0; i < size; ++i) {
                WeekLog weekLog = (WeekLog)weekLogList.get(i);
                if (weekLog != null && weekLog.getLogName().equals(logName)) {
                    weekLogList.remove(i);
                    --i;
                    size = weekLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weeklog WHERE bossid = ?");
                        ps.setString(1, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogAll(final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteWeekLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteWeekLogAll(final int chrId) {
        try {
            int size = weekLogList.size();

            for(int i = 0; i < size; ++i) {
                WeekLog weekLog = (WeekLog)weekLogList.get(i);
                if (weekLog != null && weekLog.getChrId() == chrId) {
                    weekLogList.remove(i);
                    --i;
                    weekLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weeklog WHERE characterid = ?");
                        ps.setInt(1, chrId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogAll(final int chrId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteWeekLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteWeekLogAll() {
        try {
            this.clearWeekLogList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weeklog");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE snail_weeklog auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteWeekLogAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadWeekLogaFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from snail_weekloga");
                ResultSet rs = ps.executeQuery();
                ArrayList<WeekLoga> weekLogaList0 = new ArrayList();
                String logName = "";
                boolean find = false;

                while(rs.next()) {
                    int accountId = rs.getInt("accountid");
                    int count = rs.getInt("count");
                    logName = rs.getString("bossid");
                    Date date = new Date(rs.getTimestamp("lastattempt").getTime());
                    find = false;
                    Iterator var11 = weekLogaList0.iterator();

                    while(var11.hasNext()) {
                        WeekLoga log = (WeekLoga)var11.next();
                        if (log != null && log.getAccountId() == accountId && log.getLogName().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            log.setDate(date);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        weekLogaList0.add(new WeekLoga(accountId, logName, count, date));
                    }
                }

                weekLogaList.clear();
                weekLogaList = weekLogaList0;
                ps.close();
                rs.close();
            } catch (Throwable var21) {
                var2 = var21;
                throw var21;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var20) {
                            var2.addSuppressed(var20);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var23) {
            服务端输出信息.println_err("【错误】loadWeekLogaFromDB错误，错误原因：" + var23);
            var23.printStackTrace();
        }

    }

    public void saveWeekLogaToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weekloga");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE snail_weekloga auto_increment=1");
                ps.executeUpdate();
                ArrayList<WeekLoga> weekLogaList0 = new ArrayList(weekLogaList);
                Iterator var5 = weekLogaList0.iterator();

                while(var5.hasNext()) {
                    WeekLoga weekLoga = (WeekLoga)var5.next();
                    if (weekLoga != null) {
                        ps = con.prepareStatement("INSERT INTO snail_weekloga (accountid, bossid, count, lastattempt) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, weekLoga.getAccountId());
                        ps.setString(2, weekLoga.getLogName());
                        ps.setInt(3, weekLoga.getCount());
                        ps.setTimestamp(4, new Timestamp(weekLoga.getDate().getTime()));
                        ps.executeUpdate();
                    }
                }

                weekLogaList0.clear();
                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveWeekLogaToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<WeekLoga> getWeekLogaList() {
        return weekLogaList;
    }

    public void clearWeekLogaList() {
        weekLogaList.clear();
    }

    public int getWeekLoga(int accountId, String logName) {
        ArrayList<WeekLoga> weekLogaList0 = new ArrayList(weekLogaList);
        Iterator var4 = weekLogaList0.iterator();

        WeekLoga weekLoga;
        do {
            if (!var4.hasNext()) {
                weekLogaList0.clear();
                return 0;
            }

            weekLoga = (WeekLoga)var4.next();
        } while(weekLoga == null || weekLoga.getAccountId() != accountId || !weekLoga.getLogName().equals(logName));

        return weekLoga.getCount();
    }

    public void setWeekLoga(int accountId, String logName) {
        this.setWeekLoga(accountId, logName, 1);
    }

    public void setWeekLoga(int accountId, String logName, int count) {
        this.setWeekLoga(accountId, logName, count, new Date());
    }

    public void setWeekLoga(final int accountId, final String logName, final int count, Date date) {
        boolean find = false;
        Iterator var6 = weekLogaList.iterator();

        while(var6.hasNext()) {
            WeekLoga log = (WeekLoga)var6.next();
            if (log != null && log.getAccountId() == accountId && log.getLogName().equals(logName)) {
                log.setCount(log.getCount() + count);
                log.setDate(date);
                find = true;
                break;
            }
        }

        if (!find) {
            weekLogaList.add(new WeekLoga(accountId, logName, count, date));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_weekloga WHERE accountid = ? AND bossid = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, logName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE snail_weekloga SET count = ?  WHERE id = ?");
                            ps.setInt(1, TimeLogCenter.this.getWeekLoga(accountId, logName));
                            ps.setInt(2, rs.getInt("id"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into snail_weekloga (accountid, bossid, count) values (?,?,?)");
                            ps.setInt(1, accountId);
                            ps.setString(2, logName);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setWeekLoga(final int accountId, final String logName, final int count, final Date date)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteWeekLogaAll(final int accountId, final String logName) {
        try {
            int size = weekLogaList.size();

            for(int i = 0; i < size; ++i) {
                WeekLoga weekLoga = (WeekLoga)weekLogaList.get(i);
                if (weekLoga != null && weekLoga.getAccountId() == accountId && weekLoga.getLogName().equals(logName)) {
                    weekLogaList.remove(i);
                    --i;
                    size = weekLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weekloga WHERE accountid = ? AND bossid = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogaAll(final int accountId, final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteWeekLogaAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteWeekLogaAll(final String logName) {
        try {
            int size = weekLogaList.size();

            for(int i = 0; i < size; ++i) {
                WeekLoga weekLoga = (WeekLoga)weekLogaList.get(i);
                if (weekLoga != null && weekLoga.getLogName().equals(logName)) {
                    weekLogaList.remove(i);
                    --i;
                    size = weekLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weekloga WHERE bossid = ?");
                        ps.setString(1, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogaAll(final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteWeekLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteWeekLogaAll(final int accountId) {
        try {
            int size = weekLogaList.size();

            for(int i = 0; i < size; ++i) {
                WeekLoga weekLoga = (WeekLoga)weekLogaList.get(i);
                if (weekLoga != null && weekLoga.getAccountId() == accountId) {
                    weekLogaList.remove(i);
                    --i;
                    size = weekLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weekloga WHERE accountid = ?");
                        ps.setInt(1, accountId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogaAll(final int accountId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteWeekLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteWeekLogaAll() {
        try {
            this.clearWeekLogaList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_weekloga");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE snail_weekloga auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteWeekLogaAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteWeekLogaAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadMonthLogFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from snail_monthLog");
                ResultSet rs = ps.executeQuery();
                ArrayList<MonthLog> monthLogList0 = new ArrayList();
                String logName = "";
                boolean find = false;

                while(rs.next()) {
                    find = false;
                    int chrId = rs.getInt("characterid");
                    logName = rs.getString("bossid");
                    int count = rs.getInt("count");
                    Date date = new Date(rs.getTimestamp("lastattempt").getTime());
                    Iterator var11 = monthLogList0.iterator();

                    while(var11.hasNext()) {
                        MonthLog log = (MonthLog)var11.next();
                        if (log != null && log.getChrId() == chrId && log.getLogName().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            log.setDate(date);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        monthLogList0.add(new MonthLog(chrId, logName, count, date));
                    }
                }

                monthLogList.clear();
                monthLogList = monthLogList0;
                ps.close();
                rs.close();
            } catch (Throwable var21) {
                var2 = var21;
                throw var21;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var20) {
                            var2.addSuppressed(var20);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var23) {
            服务端输出信息.println_err("【错误】loadMonthLogFromDB错误，错误原因：" + var23);
            var23.printStackTrace();
        }

    }

    public void saveMonthLogToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthLog");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE snail_monthLog auto_increment=1");
                ps.executeUpdate();
                ArrayList<MonthLog> monthLogList0 = new ArrayList(monthLogList);
                Iterator var5 = monthLogList0.iterator();

                while(var5.hasNext()) {
                    MonthLog monthLog = (MonthLog)var5.next();
                    if (monthLog != null) {
                        ps = con.prepareStatement("INSERT INTO snail_monthLog (characterid, bossid, count, lastattempt) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, monthLog.getChrId());
                        ps.setString(2, monthLog.getLogName());
                        ps.setInt(3, monthLog.getCount());
                        ps.setTimestamp(4, new Timestamp(monthLog.getDate().getTime()));
                        ps.executeUpdate();
                    }
                }

                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveMonthLogToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<MonthLog> getMonthLogList() {
        return monthLogList;
    }

    public void clearMonthLogList() {
        monthLogList.clear();
    }

    public int getMonthLog(int chrId, String logName) {
        ArrayList<MonthLog> monthLogList0 = new ArrayList(monthLogList);
        Iterator var4 = monthLogList0.iterator();

        MonthLog monthLog;
        do {
            if (!var4.hasNext()) {
                monthLogList0.clear();
                return 0;
            }

            monthLog = (MonthLog)var4.next();
        } while(monthLog == null || monthLog.getChrId() != chrId || !monthLog.getLogName().equals(logName));

        return monthLog.getCount();
    }

    public void setMonthLog(int chrId, String logName) {
        this.setMonthLog(chrId, logName, 1);
    }

    public void setMonthLog(int chrId, String logName, int count) {
        this.setMonthLog(chrId, logName, count, new Date());
    }

    public void setMonthLog(final int chrId, final String logName, final int count, Date date) {
        boolean find = false;
        Iterator var6 = monthLogList.iterator();

        while(var6.hasNext()) {
            MonthLog log = (MonthLog)var6.next();
            if (log != null && log.getChrId() == chrId && log.getLogName().equals(logName)) {
                log.setCount(log.getCount() + count);
                log.setDate(date);
                find = true;
                break;
            }
        }

        if (!find) {
            monthLogList.add(new MonthLog(chrId, logName, count, date));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_monthLog WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, logName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE snail_monthLog SET count = ?  WHERE id = ?");
                            ps.setInt(1, TimeLogCenter.this.getMonthLog(chrId, logName));
                            ps.setInt(2, rs.getInt("id"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into snail_monthLog (characterid, bossid, count) values (?,?,?)");
                            ps.setInt(1, chrId);
                            ps.setString(2, logName);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setMonthLog(final int chrId, final String logName, final int count, final Date date)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteMonthLogAll(final int chrId, final String logName) {
        try {
            int size = monthLogList.size();

            for(int i = 0; i < size; ++i) {
                MonthLog monthLog = (MonthLog)monthLogList.get(i);
                if (monthLog != null && monthLog.getChrId() == chrId && monthLog.getLogName().equals(logName)) {
                    monthLogList.remove(i);
                    --i;
                    size = monthLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthLog WHERE characterid = ? AND bossid = ?");
                        ps.setInt(1, chrId);
                        ps.setString(2, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogAll(final int chrId, final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteMonthLogAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteMonthLogAll(final String logName) {
        try {
            int size = monthLogList.size();

            for(int i = 0; i < size; ++i) {
                MonthLog monthLog = (MonthLog)monthLogList.get(i);
                if (monthLog != null && monthLog.getLogName().equals(logName)) {
                    monthLogList.remove(i);
                    --i;
                    size = monthLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthLog WHERE bossid = ?");
                        ps.setString(1, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogAll(final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteMonthLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteMonthLogAll(final int chrId) {
        try {
            int size = monthLogList.size();

            for(int i = 0; i < size; ++i) {
                MonthLog monthLog = (MonthLog)monthLogList.get(i);
                if (monthLog != null && monthLog.getChrId() == chrId) {
                    monthLogList.remove(i);
                    --i;
                    monthLogList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthLog WHERE characterid = ?");
                        ps.setInt(1, chrId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogAll(final int chrId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteMonthLogAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteMonthLogAll() {
        try {
            this.clearMonthLogList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthLog");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE snail_monthLog auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteMonthLogAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public void loadMonthLogaFromDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps = con.prepareStatement("select * from snail_monthloga");
                ResultSet rs = ps.executeQuery();
                ArrayList<MonthLoga> monthLogaList0 = new ArrayList();
                String logName = "";
                boolean find = false;

                while(rs.next()) {
                    int accountId = rs.getInt("accountid");
                    int count = rs.getInt("count");
                    logName = rs.getString("bossid");
                    Date date = new Date(rs.getTimestamp("lastattempt").getTime());
                    find = false;
                    Iterator var11 = monthLogaList0.iterator();

                    while(var11.hasNext()) {
                        MonthLoga log = (MonthLoga)var11.next();
                        if (log != null && log.getAccountId() == accountId && log.getLogName().equals(logName)) {
                            log.setCount(log.getCount() + count);
                            log.setDate(date);
                            find = true;
                            break;
                        }
                    }

                    if (!find) {
                        monthLogaList0.add(new MonthLoga(accountId, logName, count, date));
                    }
                }

                monthLogaList.clear();
                monthLogaList = monthLogaList0;
                ps.close();
                rs.close();
            } catch (Throwable var21) {
                var2 = var21;
                throw var21;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var20) {
                            var2.addSuppressed(var20);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (Exception var23) {
            服务端输出信息.println_err("【错误】loadMonthLogaFromDB错误，错误原因：" + var23);
            var23.printStackTrace();
        }

    }

    public void saveMonthLogaToDB() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthloga");
                ps.executeUpdate();
                ps = con.prepareStatement("ALTER TABLE snail_monthloga auto_increment=1");
                ps.executeUpdate();
                ArrayList<MonthLoga> monthLogaList0 = new ArrayList(monthLogaList);
                Iterator var5 = monthLogaList0.iterator();

                while(var5.hasNext()) {
                    MonthLoga monthLoga = (MonthLoga)var5.next();
                    if (monthLoga != null) {
                        ps = con.prepareStatement("INSERT INTO snail_monthloga (accountid, bossid, count, lastattempt) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, monthLoga.getAccountId());
                        ps.setString(2, monthLoga.getLogName());
                        ps.setInt(3, monthLoga.getCount());
                        ps.setTimestamp(4, new Timestamp(monthLoga.getDate().getTime()));
                        ps.executeUpdate();
                    }
                }

                monthLogaList0.clear();
                ps.close();
                con.commit();
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
            服务端输出信息.println_err("【错误】saveMonthLogaToDB错误，错误原因：" + var17);
            var17.printStackTrace();
        }

    }

    public List<MonthLoga> getMonthLogaList() {
        return monthLogaList;
    }

    public void clearMonthLogaList() {
        monthLogaList.clear();
    }

    public int getMonthLoga(int accountId, String logName) {
        ArrayList<MonthLoga> monthLogaList0 = new ArrayList(monthLogaList);
        Iterator var4 = monthLogaList0.iterator();

        MonthLoga monthLoga;
        do {
            if (!var4.hasNext()) {
                monthLogaList0.clear();
                return 0;
            }

            monthLoga = (MonthLoga)var4.next();
        } while(monthLoga == null || monthLoga.getAccountId() != accountId || !monthLoga.getLogName().equals(logName));

        return monthLoga.getCount();
    }

    public void setMonthLoga(int accountId, String logName) {
        this.setMonthLoga(accountId, logName, 1);
    }

    public void setMonthLoga(int accountId, String logName, int count) {
        this.setMonthLoga(accountId, logName, count, new Date());
    }

    public void setMonthLoga(final int accountId, final String logName, final int count, Date date) {
        boolean find = false;
        Iterator var6 = monthLogaList.iterator();

        while(var6.hasNext()) {
            MonthLoga log = (MonthLoga)var6.next();
            if (log != null && log.getAccountId() == accountId && log.getLogName().equals(logName)) {
                log.setCount(log.getCount() + count);
                log.setDate(date);
                find = true;
                break;
            }
        }

        if (!find) {
            monthLogaList.add(new MonthLoga(accountId, logName, count, date));
        }

        cachedThreadPool.execute(new Runnable() {
            public void run() {
                try {
                    Connection con = DBConPool.getConnection();
                    Throwable var2 = null;

                    try {
                        PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_monthloga WHERE accountid = ? AND bossid = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, logName);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            ps = con.prepareStatement("UPDATE snail_monthloga SET count = ?  WHERE id = ?");
                            ps.setInt(1, TimeLogCenter.this.getMonthLoga(accountId, logName));
                            ps.setInt(2, rs.getInt("id"));
                            ps.executeUpdate();
                        } else {
                            ps = con.prepareStatement("insert into snail_monthloga (accountid, bossid, count) values (?,?,?)");
                            ps.setInt(1, accountId);
                            ps.setString(2, logName);
                            ps.setInt(3, count);
                            ps.executeUpdate();
                        }

                        ps.close();
                    } catch (Throwable var13) {
                        var2 = var13;
                        throw var13;
                    } finally {
                        if (con != null) {
                            if (var2 != null) {
                                try {
                                    con.close();
                                } catch (Throwable var12) {
                                    var2.addSuppressed(var12);
                                }
                            } else {
                                con.close();
                            }
                        }

                    }
                } catch (Exception var15) {
                    服务端输出信息.println_err("【错误】：setMonthLoga(final int accountId, final String logName, final int count, final Date date)写数据库错误，原因：" + var15);
                    var15.printStackTrace();
                }

            }
        });
    }

    public boolean deleteMonthLogaAll(final int accountId, final String logName) {
        try {
            int size = monthLogaList.size();

            for(int i = 0; i < size; ++i) {
                MonthLoga monthLoga = (MonthLoga)monthLogaList.get(i);
                if (monthLoga != null && monthLoga.getAccountId() == accountId && monthLoga.getLogName().equals(logName)) {
                    monthLogaList.remove(i);
                    --i;
                    size = monthLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthloga WHERE accountid = ? AND bossid = ?");
                        ps.setInt(1, accountId);
                        ps.setString(2, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogaAll(final int accountId, final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var6) {
            服务端输出信息.println_err("【错误】deleteMonthLogaAll错误，原因：" + var6);
            var6.printStackTrace();
            return false;
        }
    }

    public boolean deleteMonthLogaAll(final String logName) {
        try {
            int size = monthLogaList.size();

            for(int i = 0; i < size; ++i) {
                MonthLoga monthLoga = (MonthLoga)monthLogaList.get(i);
                if (monthLoga != null && monthLoga.getLogName().equals(logName)) {
                    monthLogaList.remove(i);
                    --i;
                    size = monthLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthloga WHERE bossid = ?");
                        ps.setString(1, logName);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogaAll(final String logName)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteMonthLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteMonthLogaAll(final int accountId) {
        try {
            int size = monthLogaList.size();

            for(int i = 0; i < size; ++i) {
                MonthLoga monthLoga = (MonthLoga)monthLogaList.get(i);
                if (monthLoga != null && monthLoga.getAccountId() == accountId) {
                    monthLogaList.remove(i);
                    --i;
                    size = monthLogaList.size();
                }
            }

            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthloga WHERE accountid = ?");
                        ps.setInt(1, accountId);
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogaAll(final int accountId)写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var5) {
            服务端输出信息.println_err("【错误】deleteMonthLogaAll错误，原因：" + var5);
            var5.printStackTrace();
            return false;
        }
    }

    public boolean deleteMonthLogaAll() {
        try {
            this.clearMonthLogaList();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    try {
                        Connection con = DBConPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monthloga");
                        ps.executeUpdate();
                        ps = con.prepareStatement("ALTER TABLE snail_monthloga auto_increment=1");
                        ps.executeUpdate();
                        ps.close();
                    } catch (Exception var3) {
                        服务端输出信息.println_err("【错误】：deleteMonthLogaAll写数据库错误，原因：" + var3);
                        var3.printStackTrace();
                    }

                }
            });
            return true;
        } catch (Exception var2) {
            服务端输出信息.println_err("【错误】deleteMonthLogaAll错误，原因：" + var2);
            var2.printStackTrace();
            return false;
        }
    }

    public class MonthLoga {
        private int accountId;
        private int count;
        private String logName;
        private Date date;

        public MonthLoga(int accountId, String logName) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = 0;
            this.date = new Date();
        }

        public MonthLoga(int accountId, String logName, int count) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = count;
            this.date = new Date();
        }

        public MonthLoga(int accountId, String logName, int count, Date date) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = count;
            this.date = date;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getAccountId() {
            return this.accountId;
        }

        public void setAccountId(int accountId) {
            this.accountId = accountId;
        }

        public String getLogName() {
            return this.logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }
    }

    public class MonthLog {
        private int chrId;
        private int count;
        private String logName;
        private Date date;

        public MonthLog(int chrId, String logName) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = 0;
            this.date = new Date();
        }

        public MonthLog(int chrId, String logName, int count) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = count;
            this.date = new Date();
        }

        public MonthLog(int chrId, String logName, int count, Date date) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = count;
            this.date = date;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getChrId() {
            return this.chrId;
        }

        public void setChrId(int chrId) {
            this.chrId = chrId;
        }

        public String getLogName() {
            return this.logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }
    }

    public class WeekLoga {
        private int accountId;
        private int count;
        private String logName;
        private Date date;

        public WeekLoga(int accountId, String logName) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = 0;
            this.date = new Date();
        }

        public WeekLoga(int accountId, String logName, int count) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = count;
            this.date = new Date();
        }

        public WeekLoga(int accountId, String logName, int count, Date date) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = count;
            this.date = date;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getAccountId() {
            return this.accountId;
        }

        public void setAccountId(int accountId) {
            this.accountId = accountId;
        }

        public String getLogName() {
            return this.logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }
    }

    public class WeekLog {
        private int chrId;
        private int count;
        private String logName;
        private Date date;

        public WeekLog(int chrId, String logName) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = 0;
            this.date = new Date();
        }

        public WeekLog(int chrId, String logName, int count) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = count;
            this.date = new Date();
        }

        public WeekLog(int chrId, String logName, int count, Date date) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = count;
            this.date = date;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getChrId() {
            return this.chrId;
        }

        public void setChrId(int chrId) {
            this.chrId = chrId;
        }

        public String getLogName() {
            return this.logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }
    }

    public class OneTimeLoga {
        private int accountId;
        private int count;
        private String logName;

        public OneTimeLoga(int accountId, String logName) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = 0;
        }

        public OneTimeLoga(int accountId, String logName, int count) {
            this.accountId = accountId;
            this.logName = logName;
            this.count = count;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getAccountId() {
            return this.accountId;
        }

        public void setAccountId(int accountId) {
            this.accountId = accountId;
        }

        public String getLogName() {
            return this.logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }
    }

    public class OneTimeLog {
        private int chrId;
        private int count;
        private String logName;

        public OneTimeLog(int chrId, String logName) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = 0;
        }

        public OneTimeLog(int chrId, String logName, int count) {
            this.chrId = chrId;
            this.logName = logName;
            this.count = count;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getChrId() {
            return this.chrId;
        }

        public void setChrId(int chrId) {
            this.chrId = chrId;
        }

        public String getLogName() {
            return this.logName;
        }

        public void setLogName(String logName) {
            this.logName = logName;
        }
    }

    public class BossLoga {
        private int logId;
        private int accountId;
        private int count;
        private String bossId;
        private Date date;

        public BossLoga(int accountId, String bossId, int count, Date date) {
            this.accountId = accountId;
            this.bossId = bossId;
            this.date = date;
            this.count = count;
        }

        public BossLoga(int accountId, String bossId, int count) {
            this.count = count;
            this.accountId = accountId;
            this.bossId = bossId;
            this.date = new Date();
        }

        public BossLoga(int accountId, String bossId) {
            this.accountId = accountId;
            this.bossId = bossId;
            this.date = new Date();
        }

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getLogId() {
            return this.logId;
        }

        public void setLogId(int logId) {
            this.logId = logId;
        }

        public int getAccountId() {
            return this.accountId;
        }

        public void setAccountId(int accountId) {
            this.accountId = accountId;
        }

        public String getBossId() {
            return this.bossId;
        }

        public void setBossId(String bossId) {
            this.bossId = bossId;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    public class BossLog {
        private int logId;
        private int chrId;
        private int count;
        private String bossId;
        private Date date;

        public BossLog(int chrId, String bossId, int count, Date date) {
            this.chrId = chrId;
            this.bossId = bossId;
            this.date = date;
            this.count = count;
        }

        public BossLog(int chrId, String bossId, int count) {
            this.chrId = chrId;
            this.bossId = bossId;
            this.date = new Date();
            this.count = count;
        }

        public BossLog(int chrId, String bossId) {
            this.chrId = chrId;
            this.bossId = bossId;
            this.date = new Date();
            this.count = 1;
        }

        public Date getDate() {
            return this.date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public int getLogId() {
            return this.logId;
        }

        public void setLogId(int logId) {
            this.logId = logId;
        }

        public int getChrId() {
            return this.chrId;
        }

        public void setChrId(int chrId) {
            this.chrId = chrId;
        }

        public String getBossId() {
            return this.bossId;
        }

        public void setBossId(String bossId) {
            this.bossId = bossId;
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
