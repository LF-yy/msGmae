package server.shops;

import client.inventory.*;
import database.DBConPool;
import server.MerchItemPackage;
import server.maps.MapleMapObjectType;
import server.maps.MapleMapObject;
import tools.Pair;
import tools.packet.PlayerShopPacket;
import handling.channel.ChannelServer;
import tools.FileoutputUtil;
import constants.ServerConfig;
import server.MapleItemInformationProvider;
import constants.GameConstants;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import client.MapleClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import server.Timer.EtcTimer;
import client.MapleCharacter;

import java.util.concurrent.ScheduledFuture;

public class HiredMerchant extends AbstractPlayerStore
{
    public ScheduledFuture<?> schedule;
    private final List<String> blacklist;
    private int storeid;
    private final long start;
    
    public HiredMerchant(final MapleCharacter owner, final int itemId, final String desc) {
        super(owner, itemId, desc, "", 3);
        this.start = System.currentTimeMillis();
        this.blacklist = new LinkedList<String>();
        this.schedule = EtcTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                HiredMerchant.this.removeAllVisitors(-1, -1);
                HiredMerchant.this.closeShop(true, true);
            }
        }, 259200000L);
    }
    
    @Override
    public byte getShopType() {
        return 1;
    }
    
    public void setStoreId(final int storeid) {
        this.storeid = storeid;
    }
    
    public List<MaplePlayerShopItem> searchItem(final int itemSearch) {
        final List<MaplePlayerShopItem> itemz = new LinkedList<MaplePlayerShopItem>();
        for (final MaplePlayerShopItem item : this.items) {
            if (item.item.getItemId() == itemSearch && item.bundles > 0) {
                itemz.add(item);
            }
        }
        return itemz;
    }
    
    /**
     * 重写购买方法，处理玩家在精灵商人购买商品的逻辑
     *
     * @param c 代表客户端，用于与服务器交互
     * @param item 商品ID，标识玩家想要购买的商品
     * @param quantity 购买数量，表示玩家想要购买的商品数量
     */
    @Override
    public void buy(final MapleClient c, final int item, final short quantity) {
        // 根据商品ID获取精灵商人中的商品信息
        final MaplePlayerShopItem pItem = (MaplePlayerShopItem)this.items.get(item);
        // 获取商品的物品信息
        final IItem shopItem = pItem.item;
        // 复制商品信息，用于创建新的物品实例
        final IItem newItem = shopItem.copy();
        // 获取单个商品的数量
        final short perbundle = newItem.getQuantity();
        // 计算购买这些商品所需的金币总额
        final int theQuantity = pItem.price * quantity;

        // 设置新物品的数量为购买数量乘以单个商品的数量
        newItem.setQuantity((short)(quantity * perbundle));

        // 检查商品库存是否合理
        if (pItem.bundles <= 0 || pItem.bundles >= 60000) {
            c.getPlayer().dropMessage(1, "系统繁忙，请稍后再試！");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }

        // 获取物品的标志
        final byte flag = newItem.getFlag();
        // 根据物品标志处理物品属性
        if (ItemFlag.KARMA_EQ.check((int)flag)) {
            newItem.setFlag((byte)(flag - ItemFlag.KARMA_EQ.getValue()));
        }
        else if (ItemFlag.KARMA_USE.check((int)flag)) {
            newItem.setFlag((byte)(flag - ItemFlag.KARMA_USE.getValue()));
        }

        // 检查玩家是否有足够的背包空间
        if (!c.getPlayer().canHold(newItem.getItemId())) {
            c.getPlayer().dropMessage(1, "您的背包满了");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }

        // 再次检查商品库存是否合理
        if (pItem.bundles <= 0 || pItem.bundles >= 60000) {
            c.getPlayer().dropMessage(1, "系统繁忙，请稍后再試！");
            c.sendPacket(MaplePacketCreator.enableActions());
            return;
        }

        // 将商品添加到玩家的背包中
        if (MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
            // 更新商品库存
            final MaplePlayerShopItem maplePlayerShopItem = pItem;
            maplePlayerShopItem.bundles -= quantity;

            // 计算并更新商人获得的金币
            final int gainmeso = this.getMeso() + pItem.price * quantity - GameConstants.EntrustedStoreTax(pItem.price * quantity);
            this.setMeso(gainmeso);

            // 扣除玩家的金币
            c.getPlayer().gainMeso(-pItem.price * quantity, false);

            // 购买完成后移除数据库对应物品
            //同步移除数据库物品
            buyItemsShop( shopItem,c,quantity,pItem.uuid);
            // 通知商人商品已被购买
            final MapleCharacter Owner = this.getMCOwnerWorld();
            if (Owner != null) {
                Owner.dropMessage(5, "道具 " + MapleItemInformationProvider.getInstance().getName(newItem.getItemId()) + " (" + (int)perbundle + ") × " + (int)quantity + " 已被其他玩家购买，還剩下：" + (int)pItem.bundles + " 个");
            }

            // 设置物品的GM日志信息
            newItem.setGMLog(c.getPlayer().getName() + " Buy from  " + this.getOwnerName() + "'s Merchant " + newItem.getItemId() + "x" + (int)quantity + " Prize : " + pItem.price);

            // 记录日志，如果配置开启
            if (ServerConfig.LOG_MRECHANT) {
                FileoutputUtil.logToFile("logs/Data/精灵商人.txt", "\r\n 时间\u3000[" + FileoutputUtil.NowTime() + "] IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 玩家 " + c.getAccountName() + " " + c.getPlayer().getName() + " 從  " + this.getOwnerName() + " 的精灵商人购买了" + MapleItemInformationProvider.getInstance().getName(newItem.getItemId()) + " (" + newItem.getItemId() + ") x" + (int)quantity + " 单个价钱为 : " + pItem.price);
            }

            // 构建购买信息字符串，通知控制精灵商人的玩家
            final StringBuilder sb = new StringBuilder("玩家 " + c.getPlayer().getName() + " 從  " + this.getOwnerName() + " 的精灵商人购买了 " + MapleItemInformationProvider.getInstance().getName(newItem.getItemId()) + "(" + newItem.getItemId() + ") x" + (int)quantity + " 单个价钱为 : " + pItem.price);
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharactersThreadSafe()) {
                    if (chr.get_control_精灵商人()) {
                        chr.dropMessage(sb.toString());
                    }
                }
            }
        }
        else {
            // 如果背包已满，提示玩家
            c.getPlayer().dropMessage(1, "您的背包满了，请檢查您的背包！");
            c.sendPacket(MaplePacketCreator.enableActions());
        }
    }
    
    @Override
    public void closeShop(final boolean saveItems, final boolean remove) {
        try {
            if (this.schedule != null) {
                this.schedule.cancel(false);
            }
            if (saveItems) {
                this.saveItemsNew();
                this.items.clear();
            }
            if (remove) {
                ChannelServer.getInstance(this.channel).removeMerchant(this);
                this.getMap().broadcastMessage(PlayerShopPacket.destroyHiredMerchant(this.getOwnerId()));
            }
            this.setCanShop(false);
            this.getMap().removeMapObject((MapleMapObject)this);
            this.schedule = null;
        }
        catch (Exception se) {
            FileoutputUtil.outError("logs/精灵商人关闭异常.txt", (Throwable)se);
        }
    }
    
    public int getTimeLeft() {
        return (int)((System.currentTimeMillis() - this.start) / 1000L);
    }
    
    public final int getStoreId() {
        return this.storeid;
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.HIRED_MERCHANT;
    }
    
    @Override
    public void sendDestroyData(final MapleClient client) {
        if (this.isAvailable()) {
            client.sendPacket(PlayerShopPacket.destroyHiredMerchant(this.getOwnerId()));
        }
    }
    
    @Override
    public void sendSpawnData(final MapleClient client) {
        if (this.isAvailable()) {
            client.sendPacket(PlayerShopPacket.spawnHiredMerchant(this));
        }
    }
    
    public final boolean isInBlackList(final String bl) {
        return this.blacklist.contains((Object)bl);
    }
    
    public void addBlackList(final String bl) {
        this.blacklist.add(bl);
    }
    
    public void removeBlackList(final String bl) {
        this.blacklist.remove((Object)bl);
    }
    
    public void sendBlackList(final MapleClient c) {
        c.sendPacket(PlayerShopPacket.MerchantBlackListView(this.blacklist));
    }
    
    public void sendVisitor(final MapleClient c) {
        c.sendPacket(PlayerShopPacket.MerchantVisitorView(this.visitors));
    }




    public synchronized void buyItemsShop(IItem items, MapleClient c,int quantity,String uuid)   {
        System.out.println("HiredMerchant购买物品");
        long index = 0;
        try (Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()){
            MapleInventoryType mit =  GameConstants.getInventoryType(items.getItemId());
            if (mit.equals((Object)MapleInventoryType.EQUIP) || mit.equals((Object)MapleInventoryType.EQUIPPED)) {
                System.out.println(quantity + "  " + items.getItemId()+ "  " + items.getInventoryId()+  "  " + this.ownerAccount+ "  " + this.ownerId);

                PreparedStatement ps1 = con.prepareStatement("delete from hiredmerchitems where  accountid = ? and itemid = ? and uuid = ?");
            ps1.setInt(1, this.ownerAccount);
            ps1.setInt(2, items.getItemId());
            ps1.setString(3, uuid);
            ps1.executeUpdate();
            ps1.close();

                PreparedStatement ps2 = con.prepareStatement("delete from hiredmerchequipment where  uuid = ?");
                ps2.setString(1, uuid);
                ps2.executeUpdate();
                ps2.close();

                PreparedStatement ps3 = con.prepareStatement("update hiredmerch set Mesos = ? where characterid = ? and  accountid = ?", 1);
                ps3.setInt(1, this.meso.get());
                ps3.setInt(2, this.ownerId);
                ps3.setInt(3, this.ownerAccount);
                ps3.executeUpdate();
                ps3.close();
            }else{
                System.out.println(quantity + "  " + items.getItemId()+ "  " + items.getInventoryId()+  "  " + this.ownerAccount+ "  " + this.ownerId);
                PreparedStatement ps4 = con.prepareStatement("update hiredmerchitems set quantity = quantity - ?  where  accountid = ? and itemid = ? and uuid = ?");
                ps4.setInt(1, quantity);
                ps4.setInt(2, this.ownerAccount);
                ps4.setInt(3, items.getItemId());
                ps4.setString(4, uuid);
                ps4.executeUpdate();
                ps4.close();

                PreparedStatement ps5 = con.prepareStatement("update hiredmerch set Mesos = ? where characterid = ? and  accountid = ?", 1);
                ps5.setInt(1, this.meso.get());
                ps5.setInt(2, this.ownerId);
                ps5.setInt(3, this.ownerAccount);
                ps5.executeUpdate();
                ps5.close();
            }
        }
        catch (SQLException ex) {
            //System.out.println((Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
            FileoutputUtil.log("logs/雇佣商店数据库移除异常.txt", "错误的编码:"+this.ownerAccount+"----"+items.getItemId());
        }
    }

    public synchronized void deleteItemsShop(int item, MapleClient c,String uuid)   {
        MaplePlayerShopItem items = (MaplePlayerShopItem)this.items.get(item);
        long index = 0;
        try (Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()){

            PreparedStatement ps = con.prepareStatement("delete from hiredmerchitems where  accountid = ? and itemid = ? and uuid = ?");
            ps.setInt(1, this.ownerAccount);
            ps.setInt(2, items.item.getItemId());
            ps.setString(3, uuid);
            ps.executeUpdate();
            ps.close();

            MapleInventoryType mit =  GameConstants.getInventoryType(items.item.getItemId());

            if (mit.equals((Object)MapleInventoryType.EQUIP) || mit.equals((Object)MapleInventoryType.EQUIPPED)) {
                PreparedStatement pss = con.prepareStatement("delete from hiredmerchequipment where  uuid = ?");
                pss.setString(1, uuid);
                pss.executeUpdate();
                pss.close();
            }

            PreparedStatement ps1 = con.prepareStatement("update hiredmerch set Mesos = ? where characterid = ? and  accountid = ?", 1);
            ps1.setInt(1, this.meso.get());
            ps1.setInt(2, this.ownerId);
            ps1.setInt(3, this.ownerAccount);
            ps1.executeUpdate();
            ps1.close();
        }
        catch (SQLException ex) {
            //System.out.println((Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
            FileoutputUtil.log("logs/雇佣商店数据库移除异常.txt", "错误的编码:"+c.getAccountName()+"----"+items.item.getItemId());
        }
    }

    public void addItemsShop(IItem items, MapleClient c,String cleanKey)   {
        System.out.println("添加物品到数据库");
        int pid = loadPackageId(c.getPlayer().getId(), c.getAccID());
        long indexId = 0;
        try(Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO  hiredmerchitems (characterid,accountid,packageid,itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender, equipOnlyId,uuid) VALUES (?,?,?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)", 1);
            PreparedStatement pse = con.prepareStatement("INSERT INTO hiredmerchequipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)", 1);
            MapleInventoryType mit =  GameConstants.getInventoryType(items.getItemId());
            items.setUUID(cleanKey);
            ps.setInt(1 ,c.getPlayer().getId());

            ps.setInt(2, c.getAccID());
            ps.setInt(3,pid);

            ps.setInt(4, items.getItemId());
            ps.setInt(5, (int)mit.getType());
            ps.setInt(6, (int)items.getPosition());
            ps.setInt(7, (int)items.getQuantity());
            ps.setString(8, items.getOwner());
            ps.setString(9, items.getGMLog());
            ps.setInt(10, items.getUniqueId());
            ps.setLong(11, items.getExpiration());
            ps.setByte(12, items.getFlag());
            ps.setByte(13, (byte)5);
            ps.setString(14, items.getGiftFrom());
            ps.setInt(15, (int)items.getEquipOnlyId());
            ps.setString(16, cleanKey);
            ps.executeUpdate();
            try (final ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    items.setInventoryId(rs.getLong(1));
                    indexId = rs.getLong(1);
                }
            }
            if (mit.equals((Object)MapleInventoryType.EQUIP) || mit.equals((Object)MapleInventoryType.EQUIPPED)) {

                final IEquip equip = (IEquip)items;
                pse.setLong(1, indexId);
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
                pse.setString(33,cleanKey);
                pse.executeUpdate();
            }
            pse.close();
            ps.close();
        }
        catch (SQLException ex) {
            //System.out.println((Object)ex);
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)ex);
            FileoutputUtil.log("logs/物品保存异常.txt", "错误的编码:"+itemId);
        }
    }

    private static Integer loadPackageId(final int charid, final int accountid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * from hiredmerch where characterid = ? OR accountid = ?")) {
            ps.setInt(1, charid);
            ps.setInt(2, accountid);
            final ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                ps.close();
                rs.close();
                return null;
            }
            int packageId = rs.getInt("PackageId");
            ps.close();
            rs.close();
            return packageId;
        }
        catch (SQLException e) {
            //e.printStackTrace();
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)e);
            return null;
        }
    }

}
