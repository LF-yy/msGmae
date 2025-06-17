package client.MyMonth;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import server.MapleItemInformationProvider;
import server.maps.MapleMapObject;
import server.shops.*;
import tools.MaplePacketCreator;

import java.util.UUID;

public class CreatePlayerInteraction {

    public static void CreatePlayerInteraction(MapleClient c, MapleCharacter chr) {
        // 店铺标题描述
        String desc = "某某某的小店";
        // 处理雇佣商人或玩家商店的创建
            IItem shop = c.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short)(byte)1);
            HiredMerchant merch = new HiredMerchant(chr, shop.getItemId(), desc);
            chr.setPlayerShop((IMaplePlayerShop)merch);
            chr.getMap().addMapObject((MapleMapObject)merch);
            merch.setCanShop(true);
            //c.sendPacket(PlayerShopPacket.getHiredMerch(chr, merch, true));
            chr.getStat().recalcLocalStats();

            // 读取库存类型和槽位信息
            MapleInventoryType type2 = MapleInventoryType.getByType((byte) 1);
            //槽位信息
            byte slot = 12;
            // 读取捆绑销售的相关信息
            //捆绑数量
            short bundles = 1;
            //每捆数量
            short perBundle = 30000;
            //设置的价格
            int price = 111;
            // 检查价格、捆绑数量和每捆数量是否有效
            if (price <= 0 || bundles <= 0 || perBundle <= 0) {
                return;
            }
            // 获取玩家商店实例
            IMaplePlayerShop shop3 = chr.getPlayerShop();
            // 检查商店是否存在、玩家是否为店主以及是否为迷你游戏
            if (shop3 == null || !shop3.isOwner(chr) || shop3 instanceof MapleMiniGame) {
                return;
            }
            // 获取指定槽位的物品
            IItem ivItem = chr.getInventory(type2).getItem((short) slot);
            MapleItemInformationProvider ii2 = MapleItemInformationProvider.getInstance();

            short bundles_perbundle = (short) (bundles * perBundle);
            // 检查物品数量是否足够
            if (ivItem.getQuantity() >= bundles_perbundle) {
                final byte flag = ivItem.getFlag();
                // 检查物品是否不可交易或锁定
                if (ItemFlag.UNTRADEABLE.check((int) flag) || ItemFlag.LOCK.check((int) flag)) {
                    c.sendPacket(MaplePacketCreator.enableActions());
                    return;
                }
                // 处理投掷星和子弹类物品
                if (GameConstants.isThrowingStar(ivItem.getItemId()) || GameConstants.isBullet(ivItem.getItemId())) {
                    final IItem sellItem = ivItem.copyWithQuantity(ivItem.getQuantity());
                    shop3.addItem(new MaplePlayerShopItem(sellItem, (short) 1, price, UUID.randomUUID().toString().replaceAll("-", "") ));
//                    MapleInventoryManipulator.removeFromSlot(c, type2, (short) slot, ivItem.getQuantity(), true);
//                    FileoutputUtil.logToFile("logs/Data/精灵商人放入道具.txt", FileoutputUtil.NowTime() + "账号角色名字:" + c.getAccountName() + " " + c.getPlayer().getName() + " 道具： " + ivItem.getItemId() + " 數量:  " + (int) bundles + "\r\n");
                } else {
                    // 处理其他物品
//                    MapleInventoryManipulator.removeFromSlot(c, type2, (short) slot, bundles_perbundle, true);
                    IItem sellItem = ivItem.copy();
                    sellItem.setQuantity(perBundle);
                    shop3.addItem(new MaplePlayerShopItem(sellItem, bundles, price,UUID.randomUUID().toString().replaceAll("-", "")));
//                    FileoutputUtil.logToFile("logs/Data/精灵商人放入道具.txt", FileoutputUtil.NowTime() + "账号角色名字:" + c.getAccountName() + " " + c.getPlayer().getName() + " 道具： " + ivItem.getItemId() + " 數量:  " + (int) bundles + "\r\n");
                }
                // 更新商店物品信息
//                c.sendPacket(PlayerShopPacket.shopItemUpdate(shop3));

            }
            //开启商店并将商店对象赋值给线路服务器商店集合
            shop3.setOpen(true);
            shop3.setAvailable(true);
            shop3.setCanShop(true);
            shop3.update();
            MaplePlayerShop playershop = (MaplePlayerShop) shop3;
            c.getChannelServer().addPlayerShop(playershop);

        }
}
