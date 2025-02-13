package server.life;

import java.util.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import tools.FileoutputUtil;
import database.DBConPool;
import provider.MapleDataProvider;
import provider.MapleDataTool;
import provider.MapleData;
import provider.MapleDataProviderFactory;
import java.util.concurrent.ConcurrentHashMap;

public class MapleMonsterInformationProvider
{
    private static final MapleMonsterInformationProvider instance = new MapleMonsterInformationProvider();
    private final ConcurrentHashMap<Integer, String> mobCache;
    private final ConcurrentHashMap<Integer, ArrayList<MonsterDropEntry>> drops;
    private final List<MonsterGlobalDropEntry> globaldrops;

    protected MapleMonsterInformationProvider() {
        this.mobCache =new ConcurrentHashMap<>();
        this.drops =new ConcurrentHashMap<>();
        this.globaldrops = new ArrayList<>();
        this.retrieveGlobal();
    }

    public static  MapleMonsterInformationProvider getInstance() {
        return MapleMonsterInformationProvider.instance;
    }

    public final List<MonsterGlobalDropEntry> getGlobalDrop() {
        return this.globaldrops;
    }

    public Map<Integer, String> getAllMonsters() {
        if (this.mobCache.isEmpty()) {
            final MapleDataProvider stringData = MapleDataProviderFactory.getDataProvider("String.wz");
            final MapleData mobsData = stringData.getData("Mob.img");
            for (final MapleData itemFolder : mobsData.getChildren()) {
                this.mobCache.put(Integer.parseInt(itemFolder.getName()), MapleDataTool.getString("name", itemFolder, "NO-NAME"));
            }
        }
        return this.mobCache;
    }

    private void  retrieveGlobal() {
        try (final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
             final ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                this.globaldrops.add(new MonsterGlobalDropEntry(
                        rs.getInt("itemid"),
                        rs.getInt("chance"),
                        rs.getInt("continent"),
                        rs.getByte("dropType"),
                        rs.getInt("minimum_quantity"),
                        rs.getInt("maximum_quantity"),
                        rs.getShort("questid")
                ));
            }

        } catch (SQLException e) {
            // 记录详细的异常信息，包括堆栈跟踪
            System.err.println("Error retrieving drop: " + e.getMessage());
            e.printStackTrace(); // 输出堆栈跟踪
            FileoutputUtil.outError("logs/资料库异常.txt", e); // 确保传递正确的异常对象
        }
    }

//    public final ArrayList<MonsterDropEntry> retrieveDrop(final int monsterId) {
//        if (this.drops.containsKey(monsterId)) {
//            return this.drops.get(monsterId);
//        }
//        final ArrayList<MonsterDropEntry> ret = new ArrayList<>();
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        Connection con = null;
//        try {
//            con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
//            ps = con.prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
//            ps.setInt(1, monsterId);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//                ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid")));
//            }
//            rs.close();
//            ps.close();
//            con.close();
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//                if (con != null) {
//                    con.close();
//                }
//            }
//            catch (SQLException ignore) {
//                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ignore);
//                return ret;
//            }
//        }
//        catch (SQLException e) {
//            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
//            return ret;
//        }
//        finally {
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//                if (con != null) {
//                    con.close();
//                }
//            }
//            catch (SQLException ignore2) {
//                FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ignore2);
//                return ret;
//            }
//        }
//        this.drops.put(monsterId, ret);
//        return ret;
//    }
public final ArrayList<MonsterDropEntry> retrieveDrop(final int monsterId) {
    return drops.computeIfAbsent(monsterId, id -> {
        final ArrayList<MonsterDropEntry> ret = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            con = DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM drop_data WHERE dropperid = ?");
            ps.setInt(1, id);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret.add(new MonsterDropEntry(rs.getInt("itemid"), rs.getInt("chance"), rs.getInt("minimum_quantity"), rs.getInt("maximum_quantity"), rs.getShort("questid")));
            }
        } catch (SQLException e) {
            FileoutputUtil.outError("logs/资料库异常.txt", e);
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } catch (SQLException ignore) {
                FileoutputUtil.outError("logs/资料库异常.txt", ignore);
            }
        }
        return ret;
    });
}

    public final void clearDrops() {
        synchronized (this) {
            if (this.drops != null) {
                this.drops.clear();
            }
            if (this.globaldrops != null) {
                this.globaldrops.clear();
            }
        }
        try {
            retrieveGlobal();
        } catch (Exception e) {
            // 记录日志或采取其他措施处理异常
            System.err.println("Error retrieving global drops: " + e.getMessage());
        }
    }
//    public final synchronized void clearDrops() {
//        this.drops.clear();
//        this.globaldrops.clear();
//        this.retrieveGlobal();
//    }

    public int getDropQuest(final int monsterId) {
        int quest = 0;
        try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT questid FROM drop_data where dropperid = ?");
            ps.setInt(1, monsterId);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                quest = rs.getInt("questid");
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            System.out.println("Error getDropQuest" + e);
            FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)e);
        }
        return quest;
    }


    public List<Integer> getMobByItem(final int itemId) {
        final Set<Integer> mobSet = new HashSet<>();

        try (final Connection con = DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * FROM drop_data WHERE itemid = ?");
             final ResultSet rs = executeQuery(ps, itemId)) {

            while (rs.next()) {
                final int mobid = rs.getInt("dropperid");
                mobSet.add(mobid);
            }

        } catch (SQLException e) {
            //logger.error("Error getMobByItem", e);
            FileoutputUtil.outputFileError("logs/资料库异常.txt", e);
        }

        return new ArrayList<>(mobSet);
    }

    private ResultSet executeQuery(PreparedStatement ps, int itemId) throws SQLException {
        ps.setInt(1, itemId);
        return ps.executeQuery();
    }

//    public List<Integer> getMobByItem(final int itemId) {
//        final List<Integer> mobs = new LinkedList<>();
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//        try {
//            try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
//                ps = con.prepareStatement("SELECT * FROM drop_data WHERE itemid = ?");
//                ps.setInt(1, itemId);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final int mobid = rs.getInt("dropperid");
//                    if (!mobs.contains(mobid)) {
//                        mobs.add(mobid);
//                    }
//                }
//                rs.close();
//                ps.close();
//                con.close();
//            }
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            }
//            catch (SQLException ignore) {
//                //System.out.println("Error getMobByItem" + ignore);
//                FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)ignore);
//                return null;
//            }
//        }
//        catch (SQLException e) {
//            ////e.printStackTrace();
//            //System.out.println("Error getMobByItem" + e);
//            FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)e);
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            }
//            catch (SQLException ignore) {
//                //System.out.println("Error getMobByItem" + ignore);
//                FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)ignore);
//                return null;
//            }
//        }
//        finally {
//            try {
//                if (ps != null) {
//                    ps.close();
//                }
//                if (rs != null) {
//                    rs.close();
//                }
//            }
//            catch (SQLException ignore2) {
//               // System.out.println("Error getMobByItem" + ignore2);
//                FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)ignore2);
//                return null;
//            }
//        }
//        return mobs;
//    }

    public int getDropChance(final int monsterId) {
        int chance = 0;
        try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT chance FROM drop_data where dropperid = ?");
            ps.setInt(1, monsterId);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                chance = rs.getInt("chance");
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException e) {
           // System.out.println("Error getDropChance" + e);
            FileoutputUtil.outputFileError("logs/资料库异常.txt", (Throwable)e);
        }
        return chance;
    }

    public void UpdateDropChance(final int chance, final int dropperid, final int itemid) {
        try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE drop_data SET chance = ? WHERE dropperid = ? AND itemid = ?")) {
            ps.setInt(1, chance);
            ps.setInt(2, dropperid);
            ps.setInt(3, itemid);
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            //System.out.println("Error UpdateDropChance" + ex);
            FileoutputUtil.outputFileError("logs/更新爆率几率异常.txt", (Throwable)ex);
        }
    }

    public void AddDropData(final int chance, final int dropperid, final int itemid, final int questid) {
        try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("INSERT INTO drop_data SET chance = ? , dropperid = ? , itemid = ?, questid = ?")) {
            ps.setInt(1, chance);
            ps.setInt(2, dropperid);
            ps.setInt(3, itemid);
            ps.setInt(4, questid);
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
           // System.out.println("Error AddDropData" + ex);
            FileoutputUtil.outputFileError("logs/添加爆率异常.txt", (Throwable)ex);
        }
    }

    public void DeleteDropData(final int dropperid, final int itemid) {
        try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("DELETE FROM drop_data WHERE dropperid = ? AND itemid = ?")) {
            ps.setInt(1, dropperid);
            ps.setInt(2, itemid);
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.out.println("Error DeleteDropData" + ex);
            FileoutputUtil.outputFileError("logs/删除爆率异常.txt", (Throwable)ex);
        }
    }
}
