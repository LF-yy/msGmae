package server.shops;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;

import client.inventory.IEquip;
import client.inventory.MapleInventoryType;
import database.DBConPool;
import server.maps.MapleMapObject;
import client.inventory.IItem;
import tools.FileoutputUtil;
import tools.Pair;
import tools.packet.PlayerShopPacket;
import constants.GameConstants;
import server.MapleInventoryManipulator;
import tools.MaplePacketCreator;
import client.inventory.ItemFlag;
import client.MapleClient;
import java.util.ArrayList;
import client.MapleCharacter;
import java.util.List;

public class MaplePlayerShop extends AbstractPlayerStore {
    /**
 * 表示一个枫叶故事（MapleStory）玩家商店，管理购买数量和被封禁玩家等信息。
 */
private int boughtnumber; // 记录商店的购买次数
private final List<String> bannedList; // 存储被封禁玩家名称的列表

/**
 * 构造一个 MaplePlayerShop 对象。
 *
 * @param owner 商店的拥有者，为 MapleCharacter 类型对象。
 * @param itemId 商店的商品ID。
 * @param desc 商店的描述信息。
 *
 * 初始化商店，设置初始购买次数为0，并创建一个空的封禁列表。
 */
public MaplePlayerShop(final MapleCharacter owner, final int itemId, final String desc) {
    super(owner, itemId, desc, "", 3);
    this.boughtnumber = 0;
    this.bannedList = new ArrayList<String>();
}

    
    @Override
    public void buy(final MapleClient c, final int item, final short quantity) {
        final MaplePlayerShopItem pItem = (MaplePlayerShopItem)this.items.get(item);
        if (pItem.bundles > 0) {
            final IItem newItem = pItem.item.copy();
            newItem.setQuantity((short)(quantity * newItem.getQuantity()));
            final byte flag = newItem.getFlag();
            if (ItemFlag.KARMA_EQ.check((int)flag)) {
                newItem.setFlag((byte)(flag - ItemFlag.KARMA_EQ.getValue()));
            }
            else if (ItemFlag.KARMA_USE.check((int)flag)) {
                newItem.setFlag((byte)(flag - ItemFlag.KARMA_USE.getValue()));
            }
            final int gainmeso = pItem.price * quantity;
            if (c.getPlayer().getMeso() >= gainmeso) {
                if (!c.getPlayer().canHold(newItem.getItemId())) {
                    c.getPlayer().dropMessage(1, "您的背包满了.");
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                if (this.getMCOwner().getMeso() + gainmeso > 0 && MapleInventoryManipulator.checkSpace(c, newItem.getItemId(), (int)newItem.getQuantity(), newItem.getOwner()) && MapleInventoryManipulator.addFromDrop(c, newItem, false)) {
                    final MaplePlayerShopItem maplePlayerShopItem = pItem;
                    maplePlayerShopItem.bundles -= quantity;
                    this.bought.add(new BoughtItem(newItem.getItemId(), (int)quantity, gainmeso, c.getPlayer().getName()));
                    c.getPlayer().gainMeso(-gainmeso, false);
                    //同步移除数据库物品
                    buyItemsShop(item,c,quantity);
                    this.getMCOwner().gainMeso(gainmeso - GameConstants.EntrustedStoreTax(gainmeso), false);
                    if (pItem.bundles <= 0) {
                        ++this.boughtnumber;
                        if (this.boughtnumber == this.items.size()) {
                            this.closeShop(false, true);
                            return;
                        }
                    }

                }else {
                    c.getPlayer().dropMessage(1, "你的装备栏已经满了。");
                }
            }else {
                c.getPlayer().dropMessage(1, "你没有足夠的金币。");
            }
            this.getMCOwner().getClient().sendPacket(PlayerShopPacket.shopItemUpdate((IMaplePlayerShop)this));
        }
    }
    public synchronized void buyItemsShop(int item, MapleClient c,int quantity)   {
        System.out.println("MaplePlayerShop购买物品");
        MaplePlayerShopItem items = (MaplePlayerShopItem)this.items.get(item);
        long index = 0;
        try (Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()){
            MapleInventoryType mit =  GameConstants.getInventoryType(items.item.getItemId());
            if (mit.equals((Object)MapleInventoryType.EQUIP) || mit.equals((Object)MapleInventoryType.EQUIPPED)) {
                PreparedStatement ps1 = con.prepareStatement("delete from hiredmerchitems where   accountid = ? and itemid = ? and inventoryitemid = ?");
                ps1.setInt(1, this.ownerAccount);
                ps1.setInt(2, items.item.getItemId());
                ps1.setLong(3, items.item.getInventoryId());
                ps1.executeUpdate();
                ps1.close();

                PreparedStatement ps2 = con.prepareStatement("delete from hiredmerchequipment where inventoryitemid = ?");
                ps2.setLong(1, items.item.getInventoryId());
                ps2.executeUpdate();
                ps2.close();

                PreparedStatement ps3 = con.prepareStatement("update hiredmerch set Mesos = ? where characterid = ? and  accountid = ?", 1);
                ps3.setInt(1, this.meso.get());
                ps3.setInt(2, this.ownerId);
                ps3.setInt(3, this.ownerAccount);
                ps3.executeUpdate();
                ps3.close();
            }else{
                PreparedStatement ps4 = con.prepareStatement("update hiredmerchitems set quantity = quantity - ?  where  accountid = ? and itemid = ? and inventoryitemid = ?");
                ps4.setInt(1, quantity);
                ps4.setInt(2, this.ownerAccount);
                ps4.setInt(3, items.item.getItemId());
                ps4.setLong(4, items.item.getInventoryId());
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
            FileoutputUtil.log("logs/雇佣商店数据库移除异常.txt", "错误的编码:"+c.getAccountName()+"----"+items.item.getItemId());
        }
    }
    @Override
    public byte getShopType() {
        return 2;
    }

    //关闭商店保存物品
    @Override
    public void closeShop(final boolean saveItems, final boolean remove) {
        final MapleCharacter owner = this.getMCOwner();
        if (owner != null && owner.getClient() != null) {
            this.removeAllVisitors(3, 1);
            this.setCanShop(false);
            this.getMap().removeMapObject((MapleMapObject)this);
            for (final MaplePlayerShopItem items : this.getItems()) {
                if (items.bundles > 0) {
                    final IItem newItem = items.item.copy();
                    newItem.setQuantity((short)(items.bundles * newItem.getQuantity()));
                    if (!MapleInventoryManipulator.addFromDrop(owner.getClient(), newItem, false)) {
                        this.saveItemsNew();
                        break;
                    }
                    items.bundles = 0;
                }
            }
            owner.setPlayerShop(null);
            this.update();
        }
    }
    
    public void banPlayer(final String name) {
        if (!this.bannedList.contains((Object)name)) {
            this.bannedList.add(name);
        }
        for (int i = 0; i < 3; ++i) {
            MapleCharacter chr = this.getVisitor(i);
            if (chr.getName().equals((Object)name)) {
                chr.getClient().sendPacket(PlayerShopPacket.shopErrorMessage(5, 1));
                chr.setPlayerShop(null);
                this.removeVisitor(chr);
            }
        }
    }
    
    public boolean isBanned(final String name) {
        return this.bannedList.contains((Object)name);
    }


}
