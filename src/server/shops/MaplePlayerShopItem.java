package server.shops;

import client.inventory.IItem;

public class MaplePlayerShopItem
{
    public IItem item;
    public short bundles;
    public int price;
    public String uuid;

    public MaplePlayerShopItem(final IItem item, final short bundles, final int price,final String uuid) {
        this.item = item;
        item.setUUID(uuid);
        this.bundles = bundles;
        this.price = price;
        this.uuid = uuid;
    }
}
