//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package scripting;

import client.MapleClient;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import server.MapleInventoryManipulator;

import javax.script.Invocable;

public class ItemActionManager extends NPCConversationManager {
    private final IItem item;

    public ItemActionManager(MapleClient c, int npc, IItem item, Invocable iv) {
        super(c, npc, 0, 0, String.valueOf(item.getItemId()), (byte)-1, iv);
        this.item = item;
    }

    public IItem getItem() {
        return this.item;
    }

    public int getItemId() {
        return this.item.getItemId();
    }

    public int getPosition() {
        return this.item.getPosition();
    }

    public boolean used() {
        return this.used(1);
    }

    public boolean used(int q) {
        return MapleInventoryManipulator.removeFromSlot(this.c, MapleInventoryType.getByType(this.item.getType()), this.item.getPosition(), (long)((short)q), true, false, 0);
    }

    public boolean usedAll() {
        return MapleInventoryManipulator.removeFromSlot(this.c, MapleInventoryType.getByType(this.item.getType()), this.item.getPosition(), (long)((short)((int)this.item.getQuantity())), true, false, 0);
    }

    public void dispose(int remove) {
        if (remove == 0) {
            this.usedAll();
        } else if (remove > 0) {
            this.used(remove);
        }

        ItemScriptManager.getInstance().dispose(this, this.getClient());
    }

    public void dispose() {
        this.dispose(-1);
    }
}
