package server.shops;

import server.maps.MapleMapObjectType;
import server.maps.MapleMapObject;
import tools.packet.PlayerShopPacket;
import client.inventory.IItem;
import handling.channel.ChannelServer;
import tools.FileoutputUtil;
import constants.ServerConfig;
import server.MapleItemInformationProvider;
import constants.GameConstants;
import server.MapleInventoryManipulator;
import client.inventory.ItemFlag;
import tools.MaplePacketCreator;
import client.MapleClient;
import java.util.Iterator;
import server.Timer.EtcTimer;
import java.util.LinkedList;
import client.MapleCharacter;
import java.util.List;
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

            // TODO: 购买完成后移除数据库对应物品

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
                this.saveItems();
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
}
