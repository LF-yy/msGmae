package server.shops;

import server.maps.MapleMap;
import handling.channel.ChannelServer;
import handling.world.World.Find;
import server.maps.MapleMapObjectType;
import client.MapleClient;
import tools.packet.PlayerShopPacket;

import java.sql.*;
import java.util.Iterator;

import tools.FileoutputUtil;
import tools.FilePrinter;
import client.inventory.ItemLoader;
import constants.GameConstants;
import client.inventory.MapleInventoryType;
import client.inventory.IItem;
import java.util.ArrayList;
import database.DBConPool;
import java.util.LinkedList;
import tools.Pair;
import java.util.List;
import client.MapleCharacter;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import server.maps.AbstractMapleMapObject;

public abstract class AbstractPlayerStore extends AbstractMapleMapObject implements IMaplePlayerShop {
/**
 * 表示商店是否已开启
 */
protected boolean isOpened;
/**
 * 表示商店是否可用
 */
protected boolean available;
/**
 * 表示是否可以购物
 */
protected boolean canShop;
/**
 * 商店所有者的名称
 */
protected String ownerName;
/**
 * 商店的描述信息
 */
protected String des;
/**
 * 商店的密码
 */
protected String pass;
/**
 * 商店所有者的ID
 */
protected int ownerId;
/**
 * 商店所有者的账号ID
 */
protected int ownerAccount;
/**
 * 商店物品的ID
 */
protected int itemId;
/**
 * 商店的频道ID
 */
protected int channel;
/**
 * 商店所在地图的ID
 */
protected int map;
/**
 * 商店中meso（游戏货币）的数量，使用AtomicInteger保证线程安全
 */
protected AtomicInteger meso;
/**
 * 商店中角色的弱引用数组，用于节省内存
 */
protected WeakReference<MapleCharacter>[] chrs;
/**
 * 记录访问过商店的玩家名称列表
 */
protected List<String> visitors;
/**
 * 记录购买的物品列表
 */
protected List<BoughtItem> bought;
/**
 * 商店中出售的物品列表
 */
protected List<MaplePlayerShopItem> items;
/**
 * 商店内玩家发送的消息列表，存储为字符串和字节对
 */
protected List<Pair<String, Byte>> messages;

    
 /**
 * AbstractPlayerStore类代表一个抽象的玩家商店，提供了基本的商店管理和交互功能
 * 该类继承了MaplePlayerShop类，并实现了Serializable接口以支持对象的序列化
 *
 * @param owner 商店的拥有者
 * @param itemId 商店物品的ID
 * @param desc 商店的描述信息
 * @param pass 商店的密码
 * @param slots 商店的槽位数量
 */
public AbstractPlayerStore(final MapleCharacter owner, final int itemId, final String desc, final String pass, final int slots) {
    // 初始化商店状态为未开启、不可用、不可购物
    this.isOpened = false;
    this.available = false;
    this.canShop = false;

    // 初始化商店的meso数量为0
    this.meso = new AtomicInteger(0);

    // 初始化商店的访客列表、已售商品列表、商品列表和消息列表
    this.visitors = new LinkedList<String>();
    this.bought = new LinkedList<BoughtItem>();
    this.items = new LinkedList<MaplePlayerShopItem>();
    this.messages = new LinkedList<Pair<String, Byte>>();

    // 设置商店的位置为拥有者的当前位置
    this.setPosition(owner.getPosition());

    // 初始化商店的拥有者信息
    this.ownerName = owner.getName();
    this.ownerId = owner.getId();
    this.ownerAccount = owner.getAccountID();

    // 初始化商店物品的ID、描述信息和密码
    this.itemId = itemId;
    this.des = desc;
    this.pass = pass;

    // 获取拥有者所在地图和频道的信息
    this.map = owner.getMapId();
    this.channel = owner.getClient().getChannel();

    // 初始化商店的槽位数组
    this.chrs = (WeakReference<MapleCharacter>[])new WeakReference[slots];
    for (int i = 0; i < this.chrs.length; ++i) {
        this.chrs[i] = new WeakReference<MapleCharacter>(null);
    }
}

    
    @Override
    public int getMaxSize() {
        return this.chrs.length + 1;
    }
    
    @Override
    public int getSize() {
        return (this.getFreeSlot() == -1) ? this.getMaxSize() : this.getFreeSlot();
    }
    
    @Override
    public void broadcastToVisitors(final byte[] packet) {
        this.broadcastToVisitors(packet, true);
    }
    
    public void broadcastToVisitors(final byte[] packet, final boolean owner) {
        for (final WeakReference<MapleCharacter> chr : this.chrs) {
            if (chr != null && chr.get() != null) {
                ((MapleCharacter)chr.get()).getClient().sendPacket(packet);
            }
        }
        if (this.getShopType() != 1 && owner && this.getMCOwner() != null) {
            this.getMCOwner().getClient().sendPacket(packet);
        }
    }
    
    public void broadcastToVisitors(final byte[] packet, final int exception) {
        for (final WeakReference<MapleCharacter> chr : this.chrs) {
            if (chr != null && chr.get() != null && this.getVisitorSlot((MapleCharacter)chr.get()) != exception) {
                ((MapleCharacter)chr.get()).getClient().sendPacket(packet);
            }
        }
        if (this.getShopType() != 1 && this.getShopType() != 2 && this.getMCOwner() != null) {
            this.getMCOwner().getClient().sendPacket(packet);
        }
        else if (this.getShopType() == 2 && this.getMCOwner() != null) {
            this.getMCOwner().getClient().sendPacket(packet);
        }
    }
    
    @Override
    public int getMeso() {
        return this.meso.get();
    }
    
    @Override
    public void setMeso(final int meso) {
        this.meso.set(meso);
    }
    
    @Override
    public void setOpen(final boolean open) {
        this.isOpened = open;
    }
    
    @Override
    public boolean isOpen() {
        return this.isOpened;
    }
    
    public boolean saveItems() {
        if (this.getShopType() != 1) {
            return false;
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM hiredmerch WHERE accountid = ? OR characterid = ?");
            ps.setInt(1, this.ownerAccount);
            ps.setInt(2, this.ownerId);
            ps.execute();
            ps.close();
            ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", 1);
            ps.setInt(1, this.ownerId);
            ps.setInt(2, this.ownerAccount);
            ps.setInt(3, this.meso.get());
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
            final ResultSet rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                rs.close();
                ps.close();
                System.out.println("[SaveItems] 保存精灵商店出錯 - 1");
                throw new RuntimeException("Error, adding merchant to DB");
            }
            final int packageid = rs.getInt(1);
            rs.close();
            ps.close();
            final List<Pair<IItem, MapleInventoryType>> iters = new ArrayList<Pair<IItem, MapleInventoryType>>();
            for (final MaplePlayerShopItem pItems : this.items) {
                if (pItems.item != null) {
                    if (pItems.bundles <= 0) {
                        continue;
                    }
                    if (pItems.item.getQuantity() <= 0 && !GameConstants.isRechargable(pItems.item.getItemId())) {
                        continue;
                    }
                    final IItem item = pItems.item.copy();
                    item.setQuantity((short)(item.getQuantity() * pItems.bundles));
                    iters.add(new Pair<IItem, MapleInventoryType>(item, GameConstants.getInventoryType(item.getItemId())));
                }
            }
            ItemLoader.HIRED_MERCHANT.saveItems(iters, packageid, this.ownerAccount);
            return true;
        }
        catch (SQLException se) {
            System.out.println("[SaveItems] 保存精灵商店出錯 - 2");
            FilePrinter.printError("AbstractPlayerStore.txt", (Throwable)se, "saveItems");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)se);
            return false;
        }
    }

    public boolean saveItemsNew() {
        System.out.println("saveItemsNew");
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM hiredmerch WHERE accountid = ? OR characterid = ?");
            ps.setInt(1, this.ownerAccount);
            ps.setInt(2, this.ownerId);
            ps.execute();
            ps.close();
            ps = con.prepareStatement("INSERT INTO hiredmerch (characterid, accountid, Mesos, time) VALUES (?, ?, ?, ?)", 1);
            ps.setInt(1, this.ownerId);
            ps.setInt(2, this.ownerAccount);
            ps.setInt(3, this.meso.get());
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
             ResultSet rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                rs.close();
                ps.close();
                System.out.println("[SaveItems] 保存精灵商店出錯 - 1");
                throw new RuntimeException("Error, adding merchant to DB");
            }
             int packageid = rs.getInt(1);
            rs.close();
            ps.close();
            //记录雇佣商店
            ps = con.prepareStatement("DELETE FROM hiredmerchantshop WHERE accid = ? OR charid = ?");
            ps.setInt(1, this.ownerAccount);
            ps.setInt(2, this.ownerId);
            ps.execute();
            ps.close();
            ps = con.prepareStatement("INSERT INTO hiredmerchantshop VALUES (DEFAULT,?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);//(id,channelid, mapid, charid,itemid,desc, x,y,opened,accid,createdate)
            ps.setInt(1, this.channel);
            ps.setInt(2, this.map);
            ps.setInt(3, this.ownerId);
            ps.setInt(4, this.itemId);
            ps.setString(5, this.des);
            ps.setInt(6, this.getPosition().x);
            ps.setInt(7, this.getPosition().y);
            ps.setInt(8, 1);
            ps.setTimestamp(9,  new Timestamp(System.currentTimeMillis()));
            ps.setInt(10, this.ownerAccount);
            ps.executeUpdate();
            rs.close();
            ps.close();

             List<Pair<IItem, MapleInventoryType>> iters = new ArrayList<Pair<IItem, MapleInventoryType>>();
            for ( MaplePlayerShopItem pItems : this.items) {
                if (pItems.item != null) {
                    if (pItems.bundles <= 0) {
                        continue;
                    }
                    if (pItems.item.getQuantity() <= 0 && !GameConstants.isRechargable(pItems.item.getItemId())) {
                        continue;
                    }
                     IItem item = pItems.item.copy();
                    item.setQuantity((short)(item.getQuantity() * pItems.bundles));
                    iters.add(new Pair<IItem, MapleInventoryType>(item, GameConstants.getInventoryType(item.getItemId())));
                }
            }
            ItemLoader.HIRED_MERCHANT.saveItemsShop(iters, packageid, this.ownerAccount);
            return true;
        }
        catch (SQLException se) {
            System.out.println("[SaveItems] 保存精灵商店出錯 - 2");
            FilePrinter.printError("AbstractPlayerStore.txt", (Throwable)se, "saveItems");
            FileoutputUtil.outError("logs/资料库异常.txt", (Throwable)se);
            return false;
        }
    }


    public MapleCharacter getVisitor(final int num) {
        return (MapleCharacter)this.chrs[num].get();
    }
    
    @Override
    public void update() {
        if (this.isAvailable()) {
            if (this.getShopType() == 1) {
                this.getMap().broadcastMessage(PlayerShopPacket.updateHiredMerchant((HiredMerchant)this));
            }
            else if (this.getMCOwner() != null) {
                this.getMap().broadcastMessage(PlayerShopPacket.sendPlayerShopBox(this.getMCOwner()));
            }
        }
    }
    
    @Override
    public void addVisitor(final MapleCharacter visitor) {
        final int i = this.getFreeSlot();
        if (i > 0) {
            if (this.getShopType() >= 3) {
                this.broadcastToVisitors(PlayerShopPacket.getMiniGameNewVisitor(visitor, i, (MapleMiniGame)this));
            }
            else {
                this.broadcastToVisitors(PlayerShopPacket.shopVisitorAdd(visitor, i));
            }
            this.chrs[i - 1] = new WeakReference<MapleCharacter>(visitor);
            if (!this.isOwner(visitor)) {
                this.visitors.add(visitor.getName());
            }
            if (i == 3) {
                this.update();
            }
        }
    }
    
    @Override
    public void removeVisitor(final MapleCharacter visitor) {
        final byte slot = this.getVisitorSlot(visitor);
        final boolean shouldUpdate = this.getFreeSlot() == -1;
        if (slot > 0) {
            this.broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(slot), (int)slot);
            this.chrs[slot - 1] = new WeakReference<MapleCharacter>(null);
            if (shouldUpdate) {
                this.update();
            }
        }
    }
    
    @Override
    public byte getVisitorSlot(final MapleCharacter visitor) {
        for (byte i = 0; i < this.chrs.length; ++i) {
            if (this.chrs[i] != null && this.chrs[i].get() != null && ((MapleCharacter)this.chrs[i].get()).getId() == visitor.getId()) {
                return (byte)(i + 1);
            }
        }
        if (visitor.getId() == this.ownerId) {
            return 0;
        }
        return -1;
    }
    
    @Override
    public void removeAllVisitors(final int error, int type) {
        for (int i = 0; i < this.chrs.length; ++i) {
            final MapleCharacter visitor = this.getVisitor(i);
            if (visitor != null) {
                if (type != -1) {
                    visitor.getClient().sendPacket(PlayerShopPacket.shopErrorMessage(error, type));
                }
                this.broadcastToVisitors(PlayerShopPacket.shopVisitorLeave(this.getVisitorSlot(visitor)), (int)this.getVisitorSlot(visitor));
                visitor.setPlayerShop(null);
                this.chrs[i] = new WeakReference<MapleCharacter>(null);
                ++type;
            }
        }
        this.update();
    }
    
    @Override
    public String getOwnerName() {
        return this.ownerName;
    }
    
    @Override
    public int getOwnerId() {
        return this.ownerId;
    }
    
    @Override
    public int getOwnerAccId() {
        return this.ownerAccount;
    }
    
    @Override
    public String getDescription() {
        if (this.des == null) {
            return "";
        }
        return this.des;
    }
    
    @Override
    public List<Pair<Byte, MapleCharacter>> getVisitors() {
        final List<Pair<Byte, MapleCharacter>> chrz = new LinkedList<Pair<Byte, MapleCharacter>>();
        for (byte i = 0; i < this.chrs.length; ++i) {
            if (this.chrs[i] != null && this.chrs[i].get() != null) {
                chrz.add(new Pair<Byte, MapleCharacter>(Byte.valueOf((byte)(i + 1)), this.chrs[i].get()));
            }
        }
        return chrz;
    }
    
    @Override
    public List<MaplePlayerShopItem> getItems() {
        return this.items;
    }
    
    @Override
    public void addItem(final MaplePlayerShopItem item) {
        this.items.add(item);
    }
    
    @Override
    public boolean removeItem(final int item) {
        return false;
    }
    
    @Override
    public void removeFromSlot(final int slot) {
        this.items.remove(slot);
    }
    
    @Override
    public byte getFreeSlot() {
        for (byte i = 0; i < this.chrs.length; ++i) {
            if (this.chrs[i] == null || this.chrs[i].get() == null) {
                return (byte)(i + 1);
            }
        }
        return -1;
    }
    
    @Override
    public int getItemId() {
        return this.itemId;
    }
    
    @Override
    public boolean isOwner(MapleCharacter chr) {
        return chr.getId() == this.ownerId && chr.getName().equals((Object)this.ownerName);
    }
    
    @Override
    public String getPassword() {
        if (this.pass == null) {
            return "";
        }
        return this.pass;
    }
    
    @Override
    public void sendDestroyData(final MapleClient client) {
    }
    
    @Override
    public void sendSpawnData(final MapleClient client) {
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.SHOP;
    }
    
    public MapleCharacter getMCOwner() {
        return this.getMap().getCharacterById(this.ownerId);
    }
    
    public MapleCharacter getMCOwnerWorld() {
        final int ourChannel = Find.findChannel(this.ownerId);
        if (ourChannel <= 0) {
            return null;
        }
        return ChannelServer.getInstance(ourChannel).getPlayerStorage().getCharacterById(this.ownerId);
    }
    
    public MapleMap getMap() {
        return ChannelServer.getInstance(this.channel).getMapFactory().getMap(this.map);
    }
    
    @Override
    public int getGameType() {
        if (this.getShopType() == 1) {
            return 5;
        }
        if (this.getShopType() == 2) {
            return 4;
        }
        if (this.getShopType() == 3) {
            return 1;
        }
        if (this.getShopType() == 4) {
            return 2;
        }
        return 0;
    }
    
    @Override
    public boolean isAvailable() {
        return this.available;
    }
    
    @Override
    public void setAvailable(final boolean b) {
        this.available = b;
    }
    
    @Override
    public List<BoughtItem> getBoughtItems() {
        return this.bought;
    }
    
    @Override
    public boolean getCanShop() {
        return this.canShop;
    }
    
    @Override
    public void setCanShop(final boolean CanShop) {
        this.canShop = CanShop;
    }
    
    @Override
    public final List<Pair<String, Byte>> getMessages() {
        return this.messages;
    }
    
    public static class BoughtItem
    {
        public int id;
        public int quantity;
        public int totalPrice;
        public String buyer;
        
        public BoughtItem(final int id, final int quantity, final int totalPrice, final String buyer) {
            this.id = id;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
            this.buyer = buyer;
        }
    }
}
