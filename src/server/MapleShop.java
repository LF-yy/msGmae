package server;

import java.util.LinkedHashSet;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

import client.inventory.*;
import gui.LtMS;
import handling.world.World;
import tools.FileoutputUtil;
import java.util.Collection;
import java.util.ArrayList;
import database.DBConPool;

import client.SkillFactory;
import constants.PiPiConfig;
import constants.GameConstants;
import tools.MaplePacketCreator;
import client.MapleClient;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MapleShop
{
    private static final Set<Integer> rechargeableItems;
    private final int id;
    private final int npcId;
    private final List<MapleShopItem> items;
    
    private MapleShop(final int id, final int npcId) {
        this.id = id;
        this.npcId = npcId;
        this.items = new LinkedList<MapleShopItem>();
    }
    
    public void addItem(final MapleShopItem item) {
        this.items.add(item);
    }
    
    public void sendShop(final MapleClient c) {
        if (c != null && c.getPlayer() != null) {
            c.getPlayer().setShop(this);
            c.sendPacket(MaplePacketCreator.getNPCShop(c, this.getNpcId(), this.items));
        }
    }
    public void sendShop(MapleClient c, int customNpc) {
        if (c != null && c.getPlayer() != null) {
            c.getPlayer().setShop(this);
            c.sendPacket(MaplePacketCreator.getNPCShop(c, customNpc, this.items));
        }
    }

    public void buy(final MapleClient c, final int itemId, short quantity) {
        if (quantity <= 0) {
            AutobanManager.getInstance().addPoints(c, 1000, 0L, "Buying " + (int)quantity + " " + itemId);
            return;
        }
        if (!GameConstants.isMountItemAvailable(itemId, (int)c.getPlayer().getJob())) {
            c.getPlayer().dropMessage(1, "你不可以買这道具。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        final MapleShopItem item = this.findById(itemId);
        if (item != null && item.getPrice() > 0 && item.getReqItem() == 0) {
            final int price = GameConstants.isRechargable(itemId) ? item.getPrice() : (item.getPrice() * quantity);
            if (price >= 0 && c.getPlayer().getMeso() >= price) {
                if (MapleInventoryManipulator.checkSpace(c, itemId, (int)quantity, "")) {
                    c.getPlayer().gainMeso(-price, false);
                    if (GameConstants.isPet(itemId)) {
                        MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1L);
                    }
                    else {
                        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                        if (GameConstants.isRechargable(itemId)) {
                            quantity = ii.getSlotMax(c, item.getItemId());
                        }
                        MapleInventoryManipulator.addById(c, itemId, quantity);
                    }
                }
                else {
                    c.getPlayer().dropMessage(1, "你的道具栏满了。");
                }
                c.sendPacket(MaplePacketCreator.confirmShopTransaction((byte)0));
            }
        }
        else if (item != null && item.getReqItem() > 0 && quantity == 1 && c.getPlayer().haveItem(item.getReqItem(), item.getReqItemQ(), false, true)) {
            if (MapleInventoryManipulator.checkSpace(c, itemId, (int)quantity, "")) {
                MapleInventoryManipulator.removeById(c, GameConstants.getInventoryType(item.getReqItem()), item.getReqItem(), item.getReqItemQ(), false, false);
                if (GameConstants.isPet(itemId)) {
                    MapleInventoryManipulator.addById(c, itemId, quantity, "", MaplePet.createPet(itemId, MapleInventoryIdentifier.getInstance()), -1L);
                }
                else {
                    final MapleItemInformationProvider ii2 = MapleItemInformationProvider.getInstance();
                    if (GameConstants.isRechargable(itemId)) {
                        quantity = ii2.getSlotMax(c, item.getItemId());
                    }
                    MapleInventoryManipulator.addById(c, itemId, quantity);
                }
            }
            else {
                c.getPlayer().dropMessage(1, "你的道具栏满了。");
            }
            c.sendPacket(MaplePacketCreator.confirmShopTransaction((byte)0));
        }
    }
    
    public void sell(final MapleClient c, final MapleInventoryType type, final byte slot, short quantity) {
        if (quantity > 30000 || quantity == 0 || quantity<0) {
            return;
        }
        final IItem item = c.getPlayer().getInventory(type).getItem((short)slot);
        if (item == null) {
            return;
        }
        if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
            quantity = item.getQuantity();
        }
        short iQuant = item.getQuantity();
        if (iQuant > 30000) {
            iQuant = 1;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.cantSell(item.getItemId())) {
            return;
        }
        //校验
        try {
            MapleShopItem itemInfo = this.findById(item.getItemId());
            if (itemInfo != null) {
                long 付款金额 = GameConstants.isRechargable(item.getItemId()) ? itemInfo.getPrice() : (long) itemInfo.getPrice() * quantity;
                if (付款金额 <= 0 || 付款金额 >= Integer.MAX_VALUE) {
                    c.sendPacket(MaplePacketCreator.confirmShopTransaction((byte) 0));
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                int 付款前拥有金币 = c.getPlayer().getMeso();
                if (itemInfo != null && itemInfo.getPrice() > 0 && itemInfo.getReqItem() == 0) {
                    if (付款金额 > 0 && c.getPlayer().getMeso() >= 付款金额) {
                        if (MapleInventoryManipulator.checkSpace(c, itemInfo.getItemId(), (int) quantity, "")) {
                            if (quantity >= 1) {
                                if (付款金额 != (itemInfo.getPrice() * quantity)) {//付款价格不等于数据库价格
                                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(0, c.getPlayer().getName() + "尝试使用" + 付款金额 + "金币购买" + itemInfo.getPrice() * quantity + "金币的道具，被系统警告,超过三次自动封号。"));
                                    c.getPlayer().gainrwjf(1);
                                    if (c.getPlayer().getrwjf() >= 3) {
                                        //封号
                                        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + c.getPlayer().getName() + " 使用改数值外挂而被管理员永久停封。"));
                                        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + c.getPlayer().getName() + " 使用改数值外挂而被管理员永久停封。"));
                                        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + c.getPlayer().getName() + " 使用改数值外挂而被管理员永久停封。"));
                                        c.getPlayer().ban("使用数值外挂", true, true, true);
                                        return;
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            } catch(Exception e){
                System.out.println("Error while selling item: " + e);
            }



        if (quantity <= iQuant && (iQuant > 0 || GameConstants.isRechargable(item.getItemId()))) {
            MapleInventoryManipulator.removeFromSlot(c, type, (short)slot, quantity, false);
            double price;
            if (GameConstants.isThrowingStar(item.getItemId()) || GameConstants.isBullet(item.getItemId())) {
                price = (double)ii.getWholePrice(item.getItemId()) / (double)ii.getSlotMax(c, item.getItemId());
            }
            else {
                price = ii.getPrice(item.getItemId());
            }
            if (item.getItemId() == 2022195) {
                price = 1.0;
            }
            if (item.getItemId() == 4031348) {
                price = 1.0;
            }
            int recvMesos = (int)Math.max(Math.ceil(price * (double)quantity), 0.0);
            if (price != -1.0 && recvMesos > 0) {
                if (recvMesos > PiPiConfig.商店一次拍賣获得最大金币) {
                    return;
                }
                c.getPlayer().gainMeso(recvMesos, false);
            }
            c.sendPacket(MaplePacketCreator.confirmShopTransaction((byte)8));
        }
    }
    
    public void recharge(final MapleClient c, final byte slot) {
        final IItem item = c.getPlayer().getInventory(MapleInventoryType.USE).getItem((short)slot);
        if (item == null || (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId()))) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short slotMax = ii.getSlotMax(c, item.getItemId());
        final int skill = GameConstants.getMasterySkill((int)c.getPlayer().getJob());
        if (skill != 0) {
            slotMax += (short)(c.getPlayer().getSkillLevel(SkillFactory.getSkill(skill)) * 10);
        }
        if (item.getQuantity() < slotMax) {
            final int price = (int)Math.round(ii.getPrice(item.getItemId()) * (double)(slotMax - item.getQuantity()));
            if (c.getPlayer().getMeso() >= price) {
                item.setQuantity(slotMax);
                c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(1, item)));
                c.getPlayer().gainMeso(-price, false, true, false);
                c.sendPacket(MaplePacketCreator.confirmShopTransaction((byte)8));
            }
        }
    }
    
    protected MapleShopItem findById(final int itemId) {
        for (final MapleShopItem item : this.items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }
    
    public static MapleShop createFromDB(final int id, final boolean isShopId) {
        MapleShop ret = null;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement(isShopId ? "SELECT * FROM shops WHERE shopid = ?" : "SELECT * FROM shops WHERE npcid = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return null;
            }
            final int shopId = rs.getInt("shopid");
            ret = new MapleShop(shopId, rs.getInt("npcid"));
            rs.close();
            ps.close();
            ps = con.prepareStatement("SELECT * FROM shopitems WHERE shopid = ? ORDER BY position ASC");
            ps.setInt(1, shopId);
            rs = ps.executeQuery();
            final List<Integer> recharges = new ArrayList<Integer>((Collection<? extends Integer>)MapleShop.rechargeableItems);
            while (rs.next()) {
                if (GameConstants.isThrowingStar(rs.getInt("itemid")) || GameConstants.isBullet(rs.getInt("itemid"))) {
                    final MapleShopItem starItem = new MapleShopItem((short)1, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq"));
                    ret.addItem(starItem);
                    if (!MapleShop.rechargeableItems.contains((Object)Integer.valueOf(starItem.getItemId()))) {
                        continue;
                    }
                    recharges.remove((Object)Integer.valueOf(starItem.getItemId()));
                }
                else {
                    ret.addItem(new MapleShopItem((short)1000, rs.getInt("itemid"), rs.getInt("price"), rs.getInt("reqitem"), rs.getInt("reqitemq")));
                }
            }
            for (final Integer recharge : recharges) {
                ret.addItem(new MapleShopItem((short)1000, (int)recharge, 0, 0, 0));
            }
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("Could not load shop" + (Object)e);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
        }
        return ret;
    }
    
    public int getNpcId() {
        return this.npcId;
    }
    
    public int getId() {
        return this.id;
    }
    public static void 重载商店() {
        MapleShop.rechargeableItems.clear();
        for (int i = 2070000; i <= 2070013; ++i) {
            MapleShop.rechargeableItems.add(Integer.valueOf(i));
        }
        for (int i = 2330000; i <= 2330005; ++i) {
            MapleShop.rechargeableItems.add(Integer.valueOf(i));
        }
        if (((Integer) LtMS.ConfigValuesMap.get("子弹扩充开关")).intValue() > 0) {
            for (int a = 0; a < Start.子弹列表.size(); ++a) {
                MapleShop.rechargeableItems.add(Integer.valueOf(Integer.parseInt((String)Start.子弹列表.get(a))));
            }
        }
    }

    static {
        rechargeableItems = new LinkedHashSet<Integer>();
        for (int i = 2070000; i <= 2070026; ++i) {
            MapleShop.rechargeableItems.add(Integer.valueOf(i));
        }
        MapleShop.rechargeableItems.remove((Object)Integer.valueOf(2070014));
        //MapleShop.rechargeableItems.remove((Object)Integer.valueOf(2070015));
        //MapleShop.rechargeableItems.remove((Object)Integer.valueOf(2070016));
        MapleShop.rechargeableItems.remove((Object)Integer.valueOf(2070017));
        MapleShop.rechargeableItems.remove((Object)Integer.valueOf(2070018));
        MapleShop.rechargeableItems.remove((Object)Integer.valueOf(2070022));
        for (int i = 2330000; i <= 2330005; ++i) {
            MapleShop.rechargeableItems.add(Integer.valueOf(i));
        }
        MapleShop.rechargeableItems.add(Integer.valueOf(2331000));
        MapleShop.rechargeableItems.add(Integer.valueOf(2332000));
    }
}
