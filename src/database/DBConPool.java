package database;

import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;

import com.alibaba.druid.pool.DruidPooledConnection;
import gui.服务端输出信息;
import server.ServerProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.druid.pool.DruidDataSource;

public class DBConPool
{
    private static DruidDataSource dataSource;
    public static int RETURN_GENERATED_KEYS = 1;
    public static String dbUser;
    public static String dbPass;
    public static String dbIp;
    public static String dbName;
    public static int dbport;
    private static Properties dbProps;
    private static String dbHost;
    private static String dbPort;
    private static final ReentrantLock lock = new ReentrantLock();

    private static HashMap<Integer, ArrayList<Connection>> connectionsMap = new HashMap();
    private static final HashMap<Integer, ConWrapper> connections = new HashMap();

    private static Connection con = null;
    private static int max_conections = ServerProperties.getProperty("server.settings.db.maxConectionsSingleThread", 10);

    public static void InitDB() {
        DBConPool.dbName = ServerProperties.getProperty("LtMS.db.name", DBConPool.dbName);
        DBConPool.dbIp = ServerProperties.getProperty("LtMS.db.ip", DBConPool.dbIp);
        DBConPool.dbport = ServerProperties.getProperty("LtMS.db.port", DBConPool.dbport);
        DBConPool.dbUser = ServerProperties.getProperty("LtMS.db.user", DBConPool.dbUser);
        DBConPool.dbPass = ServerProperties.getProperty("LtMS.db.password", DBConPool.dbPass);
    }
    public static void close(PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException var2) {
                服务端输出信息.println_err(var2);
            }
        }

    }
    public static DBConPool getInstance() {
        return InstanceHolder.instance;
    }
    
    private DBConPool() {
    }
    
    public DruidDataSource getDataSource() {
        if (DBConPool.dataSource == null) {
            this.InitDBConPool();
        }
        return DBConPool.dataSource;
    }
    
    private void InitDBConPool() {
        try {
            final FileReader fR = new FileReader("配置.ini");
            DBConPool.dbProps.load((Reader)fR);
            fR.close();
        }
        catch (IOException ex) {
            System.err.println("加载数据库配置出错，请检查" + (Object)ex);
        }
        DBConPool.dbName = DBConPool.dbProps.getProperty("LtMS.db.name");
        DBConPool.dbHost = DBConPool.dbProps.getProperty("LtMS.db.host");
        DBConPool.dbPort = DBConPool.dbProps.getProperty("LtMS.db.port");
        DBConPool.dbUser = DBConPool.dbProps.getProperty("LtMS.db.user");
        DBConPool.dbPass = DBConPool.dbProps.getProperty("LtMS.db.password");
        (DBConPool.dataSource = new DruidDataSource()).setName("mysql_pool");
        DBConPool.dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        DBConPool.dataSource.setUrl("jdbc:mysql://" + DBConPool.dbHost + ":" + DBConPool.dbport + "/" + DBConPool.dbName + "?useUnicode=true&characterEncoding=UTF8");
        DBConPool.dataSource.setUsername(DBConPool.dbUser);
        DBConPool.dataSource.setPassword(DBConPool.dbPass);
        DBConPool.dataSource.setInitialSize(300);
        DBConPool.dataSource.setMinIdle(200);
        DBConPool.dataSource.setMaxActive(3000);
        DBConPool.dataSource.setTimeBetweenEvictionRunsMillis(60000L);
        DBConPool.dataSource.setMinEvictableIdleTimeMillis(300000L);
        DBConPool.dataSource.setValidationQuery("SELECT * FROM characters WHERE gm < 1 LIMIT 1");
        DBConPool.dataSource.setTestOnBorrow(false);
        DBConPool.dataSource.setTestOnReturn(false);
        DBConPool.dataSource.setTestWhileIdle(true);
        DBConPool.dataSource.setMaxWait(30000L);
        DBConPool.dataSource.setUseUnfairLock(true);
        DBConPool.dataSource.setRemoveAbandoned(true);
        DBConPool.dataSource.setRemoveAbandonedTimeout(60);
        InitConnections();
    }
    private static Connection InitConnections() {
        try {
            con = getInstance().getDataSource().getConnection();
        } catch (SQLException var1) {
            服务端输出信息.println_err("【错误】初始化连接数据库失败，代码位置：DBConPool.InitConnections()，原因：" + var1);
            return null;
        }

        return con;
    }
    static {
        DBConPool.dataSource = null;
        DBConPool.dbUser = "";
        DBConPool.dbPass = "root";
        DBConPool.dbIp = "localhost";
        DBConPool.dbName = "v079";
        DBConPool.dbport = 3306;
        DBConPool.dbProps = new Properties();
        InitDB();
        try {
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (ClassNotFoundException e) {
            System.out.println("[数据库信息] 找不到JDBC驱动.");
            System.exit(0);
        }
    }


    public static void close(Connection conn) {
        try {
            Thread cThread = Thread.currentThread();
            Integer threadID = (int)cThread.getId();
            ConWrapper ret = (ConWrapper)connections.get(threadID);
            if (ret != null) {
                Connection c = ret.getConnection();
                if (!c.isClosed() && c == conn) {
                    c.close();
                }

                lock.lock();

                try {
                    connections.remove(threadID);
                } finally {
                    lock.unlock();
                }
            }
        } catch (SQLException var9) {
            服务端输出信息.println_err("【错误】关闭数据库连接失败，代码位置：DBConPool.close()，原因：" + var9);
        }

    }
    private static class InstanceHolder
    {
        public static DBConPool instance;
        
        static {
            instance = new DBConPool();
        }
    }
    public static void cleanUP(final ResultSet rs, final PreparedStatement ps, Connection con) {
        if (rs != null) {
            try {
                rs.close();
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (ps != null) {
            try {
                ps.close();
            }catch (SQLException ex) {
            }
        }
        try {
            if (con != null) {
                con.close();
            }
        }catch (SQLException ex2) {
        }
    }

    public static Connection getNewConnection() {
        Thread cThread = Thread.currentThread();
        Integer threadID = (int)cThread.getId();
        ArrayList<Connection> con_list = new ArrayList();
        DruidPooledConnection con;
        if (connectionsMap.containsKey(threadID)) {
            con_list = (ArrayList)connectionsMap.get(threadID);

            try {
                con = getInstance().getDataSource().getConnection();
            } catch (SQLException var5) {
                服务端输出信息.println_err("【错误】getNewConnection失败，代码位置：DBConPool.getConnection()，原因：" + var5);
                var5.printStackTrace();
                con = null;
            }

            con_list.add(con);
            connectionsMap.put(threadID, con_list);
            return con;
        } else {
            try {
                con = getInstance().getDataSource().getConnection();
            } catch (SQLException var6) {
                服务端输出信息.println_err("【错误】getNewConnection失败，代码位置：DBConPool.getConnection()，原因：" + var6);
                var6.printStackTrace();
                con = null;
            }

            con_list.add(con);
            connectionsMap.put(threadID, con_list);
            return con;
        }
    }
    public static Connection getConnection() {
        Thread cThread = Thread.currentThread();
        Integer threadID = (int)cThread.getId();
        ArrayList<Connection> con_list = new ArrayList();
        DruidPooledConnection con;
        if (!connectionsMap.containsKey(threadID)) {
            try {
                con = getInstance().getDataSource().getConnection();
            } catch (SQLException var8) {
                服务端输出信息.println_err("【错误】连接数据库失败，代码位置：DBConPool.getConnection()，原因：" + var8);
                con = null;
            }

            if (con == null) {
                return con;
            } else {
                con_list.add(con);
                connectionsMap.put(threadID, con_list);
                return con;
            }
        } else {
            con_list = (ArrayList)connectionsMap.get(threadID);
            int con_mount = con_list.size();

            for(int i = 0; i < con_mount; ++i) {
                try {
                    if (con_list.get(i) != null && !((Connection)con_list.get(i)).isClosed()) {
                        if (!((Connection)con_list.get(i)).isValid(30)) {
                            con_list.remove(i);
                            --con_mount;
                            --i;
                        }
                    } else {
                        con_list.remove(i);
                        --con_mount;
                        --i;
                    }
                } catch (SQLException var9) {
                    服务端输出信息.println_err("【错误】判断连接是否关闭失败，代码位置：DBConPool.getConnection()，原因：" + var9);
                }
            }

            if (con_list.size() > max_conections) {
                return (Connection)con_list.get(con_list.size() - 1);
            } else {
                try {
                    con = getInstance().getDataSource().getConnection();
                    con_list.add(con);
                } catch (SQLException var7) {
                    服务端输出信息.println_err("【错误】连接数据库失败，代码位置：DBConPool.getConnection()，原因：" + var7);
                    con = null;
                }

                return con;
            }
        }
    }
    static class ConWrapper {
        private final int tid;
        private long lastAccessTime;
        private Connection connection;

        public ConWrapper(int tid, Connection con) {
            this.tid = tid;
            this.lastAccessTime = System.currentTimeMillis();
            this.connection = con;
        }

        public boolean close() {
            boolean ret = false;
            if (this.connection == null) {
                ret = false;
            } else {
                try {
                    DBConPool.lock.lock();

                    try {
                        if (this.expiredConnection() || this.connection.isValid(10)) {
                            try {
                                this.connection.close();
                                ret = true;
                            } catch (SQLException var7) {
                                ret = false;
                            }
                        }

                        DBConPool.connections.remove(this.tid);
                    } finally {
                        DBConPool.lock.unlock();
                    }
                } catch (SQLException var9) {
                    ret = false;
                }
            }

            return ret;
        }

        public Connection getConnection() {
            if (this.expiredConnection()) {
                try {
                    this.connection.close();
                    this.connection = DBConPool.getInstance().getDataSource().getConnection();
                } catch (SQLException var2) {
                }
            }

            this.lastAccessTime = System.currentTimeMillis();
            return this.connection;
        }

        public boolean expiredConnection() {
            return System.currentTimeMillis() - this.lastAccessTime >= 1800000L;
        }
    }


}
