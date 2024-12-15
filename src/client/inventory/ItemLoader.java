package client.inventory;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.util.Iterator;
import java.sql.SQLException;

import client.MapleCharacter;
import gui.服务端输出信息;
import handling.channel.ChannelServer;
import tools.FileoutputUtil;
import constants.GameConstants;
import database.DBConPool;
import java.util.LinkedHashMap;
import tools.Pair;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

public enum ItemLoader
{
    INVENTORY("inventoryitems", "inventoryequipment", 0, new String[] { "characterid" }), 
    STORAGE("inventoryitems", "inventoryequipment", 1, new String[] { "accountid" }), 
    CASHSHOP_EXPLORER("csitems", "csequipment", 2, new String[] { "accountid" }), 
    CASHSHOP_CYGNUS("csitems", "csequipment", 3, new String[] { "accountid" }), 
    CASHSHOP_ARAN("csitems", "csequipment", 4, new String[] { "accountid" }), 
    HIRED_MERCHANT("hiredmerchitems", "hiredmerchequipment", 5, new String[] { "packageid", "accountid" }), 
    DUEY("dueyitems", "dueyequipment", 6, new String[] { "packageid" }), 
    CASHSHOP_EVAN("csitems", "csequipment", 7, new String[] { "accountid" }), 
    MTS("mtsitems", "mtsequipment", 8, new String[] { "packageid" }), 
    MTS_TRANSFER("mtstransfer", "mtstransferequipment", 9, new String[] { "characterid" }), 
    CASHSHOP_DB("csitems", "csequipment", 10, new String[] { "accountid" }), 
    CASHSHOP_RESIST("csitems", "csequipment", 11, new String[] { "accountid" });
    
    private final int value;
    private final String table;
    private final String table_equip;
    private List<String> arg;
    
    private ItemLoader(final String table, final String table_equip, final int value, final String[] arg) {
        this.table = table;
        this.table_equip = table_equip;
        this.value = value;
        this.arg = Arrays.asList(arg);
    }
    
    public int getValue() {
        return this.value;
    }
    
    public Map<Long, Pair<IItem, MapleInventoryType>> loadItems( boolean login, final Integer... id) throws SQLException {
        final List<Integer> lulz = Arrays.asList(id);
        final Map<Long, Pair<IItem, MapleInventoryType>> items = new LinkedHashMap<Long, Pair<IItem, MapleInventoryType>>();
        if (lulz.size() != this.arg.size()) {
            return items;
        }
        final StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `");
        query.append(this.table);
        query.append("` LEFT JOIN `");
        query.append(this.table_equip);
        query.append("` USING(`inventoryitemid`) WHERE `type` = ?");
        for (final String g : this.arg) {
            query.append(" AND `");
            query.append(g);
            query.append("` = ?");
        }
        if (login) {
            query.append(" AND `inventorytype` = ");
            query.append((int)MapleInventoryType.EQUIPPED.getType());
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, this.value);
            for (int i = 0; i < lulz.size(); ++i) {
                ps.setInt(i + 2, (int)Integer.valueOf(lulz.get(i)));
            }
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                if (mit.equals((Object)MapleInventoryType.EQUIP) || mit.equals((Object)MapleInventoryType.EQUIPPED)) {
                    final Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getByte("flag"));
                    if (!login) {
                        equip.setQuantity((short)1);
                        equip.setInventoryId(rs.getLong("inventoryitemid"));
                        equip.setOwner(rs.getString("owner"));
                        equip.setExpiration(rs.getLong("expiredate"));
                        equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                        equip.setLevel(rs.getByte("level"));
                        equip.setStr(rs.getShort("str"));
                        equip.setDex(rs.getShort("dex"));
                        equip.setInt(rs.getShort("int"));
                        equip.setLuk(rs.getShort("luk"));
                        equip.setHp(rs.getShort("hp"));
                        equip.setMp(rs.getShort("mp"));
                        equip.setWatk(rs.getShort("watk"));
                        equip.setMatk(rs.getShort("matk"));
                        equip.setWdef(rs.getShort("wdef"));
                        equip.setMdef(rs.getShort("mdef"));
                        equip.setAcc(rs.getShort("acc"));
                        equip.setAvoid(rs.getShort("avoid"));
                        equip.setHands(rs.getShort("hands"));
                        equip.setSpeed(rs.getShort("speed"));
                        equip.setJump(rs.getShort("jump"));
                        equip.setViciousHammer(rs.getByte("ViciousHammer"));
                        equip.setItemEXP(rs.getInt("itemEXP"));
                        equip.setGMLog(rs.getString("GM_Log"));
                        equip.setDurability(rs.getInt("durability"));
                        equip.setEnhance(rs.getByte("enhance"));
                        equip.setPotential1(rs.getShort("potential1"));
                        equip.setPotential2(rs.getShort("potential2"));
                        equip.setPotential3(rs.getShort("potential3"));
                        equip.setHpR(rs.getShort("hpR"));
                        equip.setMpR(rs.getShort("mpR"));
                        equip.setHpRR(rs.getShort("hpRR"));
                        equip.setMpRR(rs.getShort("mpRR"));
                        equip.setGiftFrom(rs.getString("sender"));
                        equip.setEquipLevel(rs.getByte("itemlevel"));
                        equip.setDaKongFuMo(rs.getString("mxmxd_dakong_fumo"));
                        equip.setPotentials(rs.getString("snail_potentials"));
                        if (equip.getUniqueId() > -1 && GameConstants.isEffectRing(rs.getInt("itemid"))) {
                            final MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals((Object)MapleInventoryType.EQUIPPED));
                            if (ring != null) {
                                equip.setRing(ring);
                            }
                        }
                    }
                    items.put(Long.valueOf(rs.getLong("inventoryitemid")), new Pair<IItem, MapleInventoryType>(equip.copy(), mit));
                }
                else {
                    final Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getByte("flag"));
                    item.setUniqueId(rs.getInt("uniqueid"));
                    item.setOwner(rs.getString("owner"));
                    item.setInventoryId(rs.getLong("inventoryitemid"));
                    item.setExpiration(rs.getLong("expiredate"));
                    item.setGMLog(rs.getString("GM_Log"));
                    item.setGiftFrom(rs.getString("sender"));
                    if (GameConstants.isPet(item.getItemId())) {
                        if (item.getUniqueId() > -1) {
                            final MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                            if (pet != null) {
                                item.setPet(pet);
                            }
                        }
                        else {
                            final int new_unique = MapleInventoryIdentifier.getInstance();
                            item.setUniqueId(new_unique);
                            item.setPet(MaplePet.createPet(item.getItemId(), new_unique));
                        }
                    }
                    items.put(Long.valueOf(rs.getLong("inventoryitemid")), new Pair<IItem, MapleInventoryType>(item.copy(), mit));
                }
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return items;
    }
    
    public void saveItems(final List<Pair<IItem, MapleInventoryType>> items, final Integer... id) throws SQLException {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            this.saveItems(items, con, id);
            con.close();
        }
        catch (SQLException ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
    }
    
    public void saveItems( List<Pair<IItem, MapleInventoryType>> items, Connection con,  Integer... id) throws SQLException {
        long index = 0;
        int itemId = 0;
        try {
            final List<Integer> lulz = Arrays.asList(id);
            if (lulz.size() != this.arg.size()) {
                return;
            }
            //itemDelete(con, id);

            final StringBuilder query = new StringBuilder();
            query.append("DELETE FROM `");
            query.append(this.table);
            query.append("` WHERE `type` = ? AND (`");
            query.append((String)this.arg.get(0));
            query.append("` = ?");
            if (this.arg.size() > 1) {
                for (int i = 1; i < this.arg.size(); ++i) {
                    query.append(" OR `");
                    query.append((String)this.arg.get(i));
                    query.append("` = ?");
                }
            }
            query.append(")");
            PreparedStatement ps = con.prepareStatement(query.toString());
            ps.setInt(1, this.value);
            for (int j = 0; j < lulz.size(); ++j) {
                ps.setInt(j + 2, (int)Integer.valueOf(lulz.get(j)));
            }
            ps.executeUpdate();
            ps.close();
            if (items == null) {
                return;
            }
            final StringBuilder query_2 = new StringBuilder("INSERT INTO `");
            query_2.append(this.table);
            query_2.append("` (");
            for (final String g : this.arg) {
                query_2.append(g);
                query_2.append(", ");
            }
            query_2.append("itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender, equipOnlyId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?");
            for (final String g : this.arg) {
                query_2.append(", ?");
            }
            query_2.append(")");
            ps = con.prepareStatement(query_2.toString(), 1);
             //PreparedStatement pse = con.prepareStatement("INSERT INTO " + this.table_equip + " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            PreparedStatement pse = con.prepareStatement("INSERT INTO " + this.table_equip + " VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);

            for (final Pair<IItem, MapleInventoryType> pair : items) {
                final IItem item = (IItem)pair.getLeft();
                final MapleInventoryType mit = (MapleInventoryType)pair.getRight();
                int k = 1;
                for (int x = 0; x < lulz.size(); ++x) {
                    ps.setInt(k, (int)Integer.valueOf(lulz.get(x)));
                    ++k;
                }
                ps.setInt(k, item.getItemId());
                ps.setInt(k + 1, (int)mit.getType());
                ps.setInt(k + 2, (int)item.getPosition());
                ps.setInt(k + 3, (int)item.getQuantity());
                ps.setString(k + 4, item.getOwner());
                ps.setString(k + 5, item.getGMLog());
                ps.setInt(k + 6, item.getUniqueId());
                ps.setLong(k + 7, item.getExpiration());
                ps.setByte(k + 8, item.getFlag());
                ps.setByte(k + 9, (byte)this.value);
                ps.setString(k + 10, item.getGiftFrom());
                ps.setLong(k + 11, item.getEquipOnlyId());
                ps.executeUpdate();
                if (mit.equals((Object)MapleInventoryType.EQUIP) || mit.equals((Object)MapleInventoryType.EQUIPPED)) {
                    try (final ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("物品数据插入失败");
                        }
                        pse.setLong(1, rs.getLong(1));
                        index = rs.getLong(1);
                        itemId = item.getItemId();
                    }
                    final IEquip equip = (IEquip)item;
                    pse.setInt(2, (int)equip.getUpgradeSlots());
                    pse.setInt(3, (int)equip.getLevel());
                    pse.setInt(4, (int)equip.getStr());
                    pse.setInt(5, (int)equip.getDex());
                    pse.setInt(6, (int)equip.getInt());
                    pse.setInt(7, (int)equip.getLuk());
                    pse.setInt(8, (int)equip.getHp());
                    pse.setInt(9, (int)equip.getMp());
                    pse.setInt(10, (int)equip.getWatk());
                    pse.setInt(11, (int)equip.getMatk());
                    pse.setInt(12, (int)equip.getWdef());
                    pse.setInt(13, (int)equip.getMdef());
                    pse.setInt(14, (int)equip.getAcc());
                    pse.setInt(15, (int)equip.getAvoid());
                    pse.setInt(16, (int)equip.getHands());
                    pse.setInt(17, (int)equip.getSpeed());
                    pse.setInt(18, (int)equip.getJump());
                    pse.setInt(19, (int)equip.getViciousHammer());
                    pse.setInt(20, equip.getItemEXP());
                    pse.setInt(21, equip.getDurability());
                    pse.setByte(22, equip.getEnhance());
                    pse.setInt(23, (int)equip.getPotential1());
                    pse.setInt(24, (int)equip.getPotential2());
                    pse.setInt(25, (int)equip.getPotential3());
                    pse.setInt(26, (int)equip.getHpR());
                    pse.setInt(27, (int)equip.getMpR());
                    pse.setInt(28, (int)equip.getHpRR());
                    pse.setInt(29, (int)equip.getMpRR());
                    pse.setInt(30, equip.getEquipLevel());
                    pse.setString(31, equip.getDaKongFuMo());
                    pse.setString(32, equip.getPotentials());
                    pse.executeUpdate();
                }
            }
            pse.close();
            ps.close();
        }
        catch (SQLException ex) {
            //System.out.println((Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            FileoutputUtil.log("logs/物品保存异常.txt", "错误的编码:"+index+"----"+itemId);
        }
    }

    public  void itemDelete( Connection con, Integer... id) {
        try {
            List<Integer> lulz = Arrays.asList(id);
            if (lulz.size() != this.arg.size()) {
                return;
            }
            final StringBuilder query = new StringBuilder();
            query.append("select inventoryitemid FROM `");
            query.append(this.table);
            query.append("` WHERE `type` = ? AND (`");
            query.append((String)this.arg.get(0));
            query.append("` = ?");
            if (this.arg.size() > 1) {
                for (int i = 1; i < this.arg.size(); ++i) {
                    query.append(" OR `");
                    query.append((String)this.arg.get(i));
                    query.append("` = ?");
                }
            }
            query.append(")");
            PreparedStatement ps = con.prepareStatement("delete from inventoryequipment where inventoryitemid in("+query.toString()+")");
            ps.setInt(1, this.value);
            for (int j = 0; j < lulz.size(); ++j) {
                ps.setInt(j + 2, (int)Integer.valueOf(lulz.get(j)));
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Error while deleting item");
        }
    }


    public static boolean isExistsByUniqueid(final int uniqueid) {
        for (final ItemLoader il : values()) {
            final StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `inventoryitems` WHERE `type` = ? AND uniqueid = ?");
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final PreparedStatement ps = con.prepareStatement(query.toString());
                ps.setInt(1, il.value);
                ps.setInt(2, uniqueid);
                final ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    ps.close();
                    rs.close();
                    return true;
                }
                ps.close();
                rs.close();
                con.close();
            }
            catch (SQLException ex) {
                Logger.getLogger(ItemLoader.class.getName()).log(Level.SEVERE, null, (Throwable)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
        }
        return false;
    }


    public Map<Integer, Pair<IItem, MapleInventoryType>> loadHiredItems(boolean login, int id) throws SQLException {
        Map<Integer, Pair<IItem, MapleInventoryType>> items = new LinkedHashMap();
        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM `");
        query.append(this.table);
        query.append("` LEFT JOIN `");
        query.append(this.table_equip);
        query.append("` USING (`inventoryitemid`) WHERE `type` = ?");
        query.append(" AND `");
        query.append((String)this.arg.get(0));
        query.append("` = ?");
        if (login) {
            query.append(" AND `inventorytype` = ");
            query.append(MapleInventoryType.EQUIPPED.getType());
        }

        PreparedStatement ps = DBConPool.getConnection().prepareStatement(query.toString());
        ps.setInt(1, this.value);
        ps.setInt(2, id);
        ResultSet rs = ps.executeQuery();

        while(true) {
            while(rs.next()) {
                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                if (!mit.equals(MapleInventoryType.EQUIP) && !mit.equals(MapleInventoryType.EQUIPPED)) {
                    Item item = new Item(rs.getInt("itemid"), rs.getShort("position"),rs.getShort("quantity"), rs.getByte("flag"));
                    item.setUniqueId(rs.getInt("uniqueid"));
                    item.setOwner(rs.getString("owner"));
                    item.setExpiration(rs.getLong("expiredate"));
                    item.setGMLog(rs.getString("GM_Log"));
                    item.setGiftFrom(rs.getString("sender"));
                    item.setPrice(rs.getInt("price"));
                    if (GameConstants.isPet(item.getItemId())) {
                        if (item.getUniqueId() > -1) {
                            MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                            if (pet != null) {
                                item.setPet(pet);
                            }
                        } else {
                            int new_unique = MapleInventoryIdentifier.getInstance();
                            item.setUniqueId(new_unique);
                            item.setPet(MaplePet.createPet(item.getItemId(), new_unique));
                        }
                    }

                    items.put(rs.getInt("inventoryitemid"), new Pair(item.copy(), mit));
                } else {
                    Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getByte("flag"));
                    if (!login) {
                        equip.setQuantity((short) 1);
                        equip.setOwner(rs.getString("owner"));
                        equip.setExpiration(rs.getLong("expiredate"));
                        equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                        equip.setLevel(rs.getByte("level"));
                        equip.setStr(rs.getShort("str"));
                        equip.setDex(rs.getShort("dex"));
                        equip.setInt(rs.getShort("int"));
                        equip.setLuk(rs.getShort("luk"));
                        equip.setHp(rs.getShort("hp"));
                        equip.setMp(rs.getShort("mp"));
                        equip.setWatk(rs.getShort("watk"));
                        equip.setMatk(rs.getShort("matk"));
                        equip.setWdef(rs.getShort("wdef"));
                        equip.setMdef(rs.getShort("mdef"));
                        equip.setAcc(rs.getShort("acc"));
                        equip.setAvoid(rs.getShort("avoid"));
                        equip.setHands(rs.getShort("hands"));
                        equip.setSpeed(rs.getShort("speed"));
                        equip.setJump(rs.getShort("jump"));
                        equip.setViciousHammer(rs.getByte("ViciousHammer"));
                        equip.setItemEXP(rs.getInt("itemEXP"));
                        equip.setGMLog(rs.getString("GM_Log"));
                        equip.setDurability(rs.getInt("durability"));
                        equip.setEnhance(rs.getByte("enhance"));
                        equip.setPotential1(rs.getShort("potential1"));
                        equip.setPotential2(rs.getShort("potential2"));
                        equip.setPotential3(rs.getShort("potential3"));
                        equip.setHpR(rs.getShort("hpR"));
                        equip.setMpR(rs.getShort("mpR"));
                        equip.setGiftFrom(rs.getString("sender"));
                        equip.setEquipLevel(rs.getByte("itemlevel"));
                        equip.setPrice(rs.getInt("price"));
                        equip.setDaKongFuMo(rs.getString("mxmxd_dakong_fumo"));
                        equip.setPotentials(rs.getString("snail_potentials"));
                        if (equip.getUniqueId() > -1 && GameConstants.isEffectRing(rs.getInt("itemid"))) {
                            MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                            if (ring != null) {
                                equip.setRing(ring);
                            }
                        }
                    }

                    items.put(rs.getInt("inventoryitemid"), new Pair(equip.copy(), mit));
                }
            }

            rs.close();
            ps.close();
            return items;
        }
    }


    public Map<Long, Pair<IItem, MapleInventoryType>> loadItems(Connection con, boolean login, Integer... id) throws SQLException {
        List<Integer> lulz = Arrays.asList(id);
        Map<Long, Pair<IItem, MapleInventoryType>> items = new LinkedHashMap();
        if (lulz.size() != this.arg.size()) {
            return items;
        } else {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM `");
            query.append(this.table);
            query.append("` LEFT JOIN `");
            query.append(this.table_equip);
            query.append("` USING(`inventoryitemid`) WHERE `type` = ?");
            Iterator var7 = this.arg.iterator();

            while(var7.hasNext()) {
                String g = (String)var7.next();
                query.append(" AND `");
                query.append(g);
                query.append("` = ?");
            }

            if (login) {
                query.append(" AND `inventorytype` = ");
                query.append(MapleInventoryType.EQUIPPED.getType());
            }

            try {
                PreparedStatement ps = con.prepareStatement(query.toString());
                ps.setInt(1, this.value);

                for(int i = 0; i < lulz.size(); ++i) {
                    ps.setInt(i + 2, (Integer)lulz.get(i));
                }

                ResultSet rs = ps.executeQuery();

                while(true) {
                    while(rs.next()) {
                        MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));
                        if (!mit.equals(MapleInventoryType.EQUIP) && !mit.equals(MapleInventoryType.EQUIPPED)) {
                            Item item = new Item(rs.getInt("itemid"), rs.getShort("position"), rs.getShort("quantity"), rs.getByte("flag"));
                            item.setUniqueId(rs.getInt("uniqueid"));
                            item.setOwner(rs.getString("owner"));
                            item.setInventoryId(rs.getLong("inventoryitemid"));
                            item.setExpiration(rs.getLong("expiredate"));
                            item.setGMLog(rs.getString("GM_Log"));
                            item.setGiftFrom(rs.getString("sender"));
                            if (GameConstants.isPet(item.getItemId())) {
                                if (item.getUniqueId() > -1) {
                                    MaplePet pet = MaplePet.loadFromDb(item.getItemId(), item.getUniqueId(), item.getPosition());
                                    if (pet != null) {
                                        item.setPet(pet);
                                    }
                                } else {
                                    int new_unique = MapleInventoryIdentifier.getInstance();
                                    item.setUniqueId(new_unique);
                                    item.setPet(MaplePet.createPet(item.getItemId(), new_unique));
                                }
                            }

                            items.put(rs.getLong("inventoryitemid"), new Pair(item.copy(), mit));
                        } else {
                            Equip equip = new Equip(rs.getInt("itemid"), rs.getShort("position"), rs.getInt("uniqueid"), rs.getByte("flag"));
                            if (!login) {
                                equip.setQuantity((short) 1);
                                equip.setInventoryId(rs.getLong("inventoryitemid"));
                                equip.setOwner(rs.getString("owner"));
                                equip.setExpiration(rs.getLong("expiredate"));
                                equip.setUpgradeSlots(rs.getByte("upgradeslots"));
                                equip.setLevel(rs.getByte("level"));
                                equip.setStr(rs.getShort("str"));
                                equip.setDex(rs.getShort("dex"));
                                equip.setInt(rs.getShort("int"));
                                equip.setLuk(rs.getShort("luk"));
                                equip.setHp(rs.getShort("hp"));
                                equip.setMp(rs.getShort("mp"));
                                equip.setWatk(rs.getShort("watk"));
                                equip.setMatk(rs.getShort("matk"));
                                equip.setWdef(rs.getShort("wdef"));
                                equip.setMdef(rs.getShort("mdef"));
                                equip.setAcc(rs.getShort("acc"));
                                equip.setAvoid(rs.getShort("avoid"));
                                equip.setHands(rs.getShort("hands"));
                                equip.setSpeed(rs.getShort("speed"));
                                equip.setJump(rs.getShort("jump"));
                                equip.setViciousHammer(rs.getByte("ViciousHammer"));
                                equip.setItemEXP(rs.getInt("itemEXP"));
                                equip.setGMLog(rs.getString("GM_Log"));
                                equip.setDurability(rs.getInt("durability"));
                                equip.setEnhance(rs.getByte("enhance"));
                                equip.setPotential1(rs.getShort("potential1"));
                                equip.setPotential2(rs.getShort("potential2"));
                                equip.setPotential3(rs.getShort("potential3"));
                                equip.setHpR(rs.getShort("hpR"));
                                equip.setMpR(rs.getShort("mpR"));
                                equip.setHpRR(rs.getShort("hpRR"));
                                equip.setMpRR(rs.getShort("mpRR"));
                                equip.setGiftFrom(rs.getString("sender"));
                                equip.setEquipLevel(rs.getByte("itemlevel"));
                                equip.setDaKongFuMo(rs.getString("mxmxd_dakong_fumo"));
                                equip.setPotentials(rs.getString("snail_potentials"));
                                if (equip.getUniqueId() > -1 && GameConstants.isEffectRing(rs.getInt("itemid"))) {
                                    MapleRing ring = MapleRing.loadFromDb(equip.getUniqueId(), mit.equals(MapleInventoryType.EQUIPPED));
                                    if (ring != null) {
                                        equip.setRing(ring);
                                    }
                                }
                            }

                            items.put(rs.getLong("inventoryitemid"), new Pair(equip.copy(), mit));
                        }
                    }

                    rs.close();
                    ps.close();
                    Iterator var17 = ChannelServer.getAllInstances().iterator();

                    while(var17.hasNext()) {
                        ChannelServer cs = (ChannelServer)var17.next();
                        Iterator var22 = cs.getPlayerStorage().getAllCharacters().iterator();

                        while(var22.hasNext()) {
                            MapleCharacter chr = (MapleCharacter)var22.next();
                            if (chr != null) {
                                chr.刷新防滑状态();
                            }
                        }
                    }

                    return items;
                }
            } catch (SQLException var13) {
                服务端输出信息.println_err("loadItems错误，角色ID：" + id + "，读" + this.table + "错误，" + var13);
                FileoutputUtil.outError("logs/资料库异常.txt", var13);
                return null;
            }
        }
    }
}
