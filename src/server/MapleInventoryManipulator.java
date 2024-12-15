package server;

import abc.Game;
import client.*;
import constants.ServerConfig;
import constants.tzjc;
import gui.LtMS;
import gui.服务端输出信息;
import snail.EquipFieldEnhancement;
import snail.Potential;
import server.maps.MapleMapObject;
import java.awt.Point;
import constants.WorldConstants;

import java.util.Collections;
import handling.world.World.Broadcast;
import tools.FileoutputUtil;

import java.util.ArrayList;
import client.inventory.Equip;
import client.inventory.ItemFlag;
import java.util.Iterator;
import java.util.List;
import client.inventory.InventoryException;
import client.inventory.Item;
import client.inventory.MapleInventoryIdentifier;
import client.inventory.MaplePet;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import tools.MaplePacketCreator;
import constants.GameConstants;
import client.inventory.IItem;
import tools.packet.MTSCSPacket;

public class MapleInventoryManipulator
{
    public static void addRing(MapleCharacter chr, final int itemId, final int ringId, final int sn) {
        final CashItemInfo csi = CashItemFactory.getInstance().getItem(sn);
        if (csi == null) {
            return;
        }
        final IItem ring = chr.getCashInventory().toItem(csi, ringId);
        if (ring == null || ring.getUniqueId() != ringId || ring.getUniqueId() <= 0 || ring.getItemId() != itemId) {
            return;
        }
        chr.getCashInventory().addToInventory(ring);
        chr.getClient().sendPacket(MTSCSPacket.showBoughtCashItem(ring, sn, chr.getClient().getAccID()));
    }
    
    public static boolean addbyItem(final MapleClient c, final IItem item) {
        return addbyItem(c, item, false) >= 0;
    }
    
    public static short addbyItem(final MapleClient c, final IItem item, final boolean fromcs) {
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
        final short newSlot = c.getPlayer().getInventory(type).addItem(item);
        if (newSlot == -1) {
            if (!fromcs) {
                c.sendPacket(MaplePacketCreator.getInventoryFull());
                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
            }
            return newSlot;
        }
        if (item.hasSetOnlyId()) {
            item.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
        }
        if (!fromcs) {
            c.sendPacket(MaplePacketCreator.modifyInventory(true, new ModifyInventory(0, item)));
        }
        c.getPlayer().havePartyQuest(item.getItemId());
        //处理复制物品
        if (!fromcs && type.equals(MapleInventoryType.EQUIP)) {
            c.getPlayer().checkCopyItems();
        }
        return newSlot;
    }
    
    public static int getUniqueId(final int itemId, final MaplePet pet) {
        int uniqueid = -1;
        if (GameConstants.isPet(itemId)) {
            if (pet != null) {
                uniqueid = pet.getUniqueId();
            }
            else {
                uniqueid = MapleInventoryIdentifier.getInstance();
            }
        }
        else if (GameConstants.getInventoryType(itemId) == MapleInventoryType.CASH || MapleItemInformationProvider.getInstance().isCash(itemId)) {
            uniqueid = MapleInventoryIdentifier.getInstance();
        }
        return uniqueid;
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity) {
        return addById(c, itemId, quantity, null, null, 0L);
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity, final String owner) {
        return addById(c, itemId, quantity, owner, null, 0L);
    }
    
    public static byte addId(final MapleClient c, final int itemId, final short quantity, final String owner) {
        return addId(c, itemId, quantity, owner, null, 0L);
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity, final String owner, final MaplePet pet) {
        return addById(c, itemId, quantity, owner, pet, 0L);
    }
    
    public static boolean addById( MapleClient c,int itemId,short quantity,  String owner,  MaplePet pet,  long period) {
        return addId(c, itemId, quantity, owner, pet, period) >= 0;
    }

    public static byte addId(final MapleClient c, final int itemId, short quantity, final String owner, final MaplePet pet, final long period) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            c.sendPacket(MaplePacketCreator.showItemUnavailable());
            return -1;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);
        final int uniqueid = getUniqueId(itemId, pet);
        short newSlot = -1;
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, itemId);
            final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!GameConstants.isRechargable(itemId)) {
                if (existing.size() > 0) {
                    final Iterator<IItem> i = existing.iterator();
                    while (quantity > 0 && i.hasNext()) {
                        final Item eItem = (Item)i.next();
                        final short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && (eItem.getOwner().equals((Object)owner) || owner == null) && eItem.getExpiration() == -1L) {
                            final short newQ = (short)Math.min(oldQ + quantity, (int)slotMax);
                            quantity -= (short)(newQ - oldQ);
                            eItem.setQuantity(newQ);
                            c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(1, (IItem)eItem)));
                        }
                    }
                }
                while (quantity > 0) {
                    final short newQ2 = (short)Math.min((int)quantity, (int)slotMax);
                    if (newQ2 == 0) {
                        c.getPlayer().havePartyQuest(itemId);
                        c.sendPacket(MaplePacketCreator.enableActions());
                        return (byte)newSlot;
                    }
                    quantity -= newQ2;
                    final Item nItem = new Item(itemId, (short)0, newQ2, (byte)0, uniqueid);
                    newSlot = c.getPlayer().getInventory(type).addItem((IItem)nItem);
                    if (newSlot == -1) {
                        c.sendPacket(MaplePacketCreator.getInventoryFull());
                        c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                        return -1;
                    }
                    if (owner != null) {
                        nItem.setOwner(owner);
                    }
                    if (period > 0L) {
                        nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    }
                    if (pet != null) {
                        nItem.setPet(pet);
                        pet.setInventoryPosition(newSlot);
                        c.getPlayer().addPet(pet);
                    }
                    c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, (IItem)nItem)));
                    if (GameConstants.isRechargable(itemId) && quantity == 0) {
                        break;
                    }
                }
            }
            else {
                final Item nItem = new Item(itemId, (short)0, quantity, (byte)0, uniqueid);
                newSlot = c.getPlayer().getInventory(type).addItem((IItem)nItem);
                if (newSlot == -1) {
                    c.sendPacket(MaplePacketCreator.getInventoryFull());
                    c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                    return -1;
                }
                if (period > 0L) {
                    nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, (IItem)nItem)));
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        }
        else {
            if (quantity != 1) {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
            final IItem nEquip = ii.getEquipById(itemId);
            if (owner != null) {
                nEquip.setOwner(owner);
            }
            nEquip.setUniqueId(uniqueid);
            if (period > 0L) {
                nEquip.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }
            if (nEquip.hasSetOnlyId()) {
                nEquip.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
            }
            newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
            if (newSlot == -1) {
                c.sendPacket(MaplePacketCreator.getInventoryFull());
                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                return -1;
            }
            c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, nEquip)));
        }
        c.getPlayer().havePartyQuest(itemId);
        return (byte)newSlot;
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity, final byte Flag) {
        return addById(c, itemId, quantity, null, null, 0L, Flag);
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity, final String owner, final byte Flag) {
        return addById(c, itemId, quantity, owner, null, 0L, Flag);
    }
    
    public static byte addId(final MapleClient c, final int itemId, final short quantity, final String owner, final byte Flag) {
        return addId(c, itemId, quantity, owner, null, 0L, Flag);
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity, final String owner, final MaplePet pet, final byte Flag) {
        return addById(c, itemId, quantity, owner, pet, 0L, Flag);
    }
    
    public static boolean addById(final MapleClient c, final int itemId, final short quantity, final String owner, final MaplePet pet, final long period, final byte Flag) {
        return addId(c, itemId, quantity, owner, pet, period, Flag) >= 0;
    }
    
    public static byte addId(final MapleClient c, final int itemId, short quantity, final String owner, final MaplePet pet, final long period, final byte Flag) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) {
            c.getSession().write((Object)MaplePacketCreator.getInventoryFull());
            c.getSession().write((Object)MaplePacketCreator.showItemUnavailable());
            return -1;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);
        final int uniqueid = getUniqueId(itemId, pet);
        short newSlot = -1;
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, itemId);
            final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!GameConstants.isRechargable(itemId)) {
                if (existing.size() > 0) {
                    final Iterator<IItem> i = existing.iterator();
                    while (quantity > 0 && i.hasNext()) {
                        final Item eItem = (Item)i.next();
                        final short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && (eItem.getOwner().equals((Object)owner) || owner == null) && eItem.getExpiration() == -1L) {
                            final short newQ = (short)Math.min(oldQ + quantity, (int)slotMax);
                            quantity -= (short)(newQ - oldQ);
                            eItem.setQuantity(newQ);
                            c.getSession().write((Object)MaplePacketCreator.updateInventorySlot(type, (IItem)eItem, false));
                        }
                    }
                }
                while (quantity > 0) {
                    final short newQ2 = (short)Math.min((int)quantity, (int)slotMax);
                    if (newQ2 == 0) {
                        c.getPlayer().havePartyQuest(itemId);
                        c.getSession().write((Object)MaplePacketCreator.enableActions());
                        return (byte)newSlot;
                    }
                    quantity -= newQ2;
                    final Item nItem = new Item(itemId, (short)0, newQ2, (byte)0, uniqueid);
                    newSlot = c.getPlayer().getInventory(type).addItem((IItem)nItem);
                    if (newSlot == -1) {
                        c.getSession().write((Object)MaplePacketCreator.getInventoryFull());
                        c.getSession().write((Object)MaplePacketCreator.getShowInventoryFull());
                        return -1;
                    }
                    if (owner != null) {
                        nItem.setOwner(owner);
                    }
                    if (Flag > 0 && ii.isCash(nItem.getItemId())) {
                        byte flag = nItem.getFlag();
                        flag |= (byte)ItemFlag.KARMA_EQ.getValue();
                        nItem.setFlag(flag);
                    }
                    if (period > 0L) {
                        nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    }
                    if (pet != null) {
                        nItem.setPet(pet);
                        pet.setInventoryPosition(newSlot);
                        c.getPlayer().addPet(pet);
                    }
                    c.getSession().write((Object)MaplePacketCreator.addInventorySlot(type, (IItem)nItem));
                    if (GameConstants.isRechargable(itemId) && quantity == 0) {
                        break;
                    }
                }
            }
            else {
                final Item nItem = new Item(itemId, (short)0, quantity, (byte)0, uniqueid);
                newSlot = c.getPlayer().getInventory(type).addItem((IItem)nItem);
                if (newSlot == -1) {
                    c.getSession().write((Object)MaplePacketCreator.getInventoryFull());
                    c.getSession().write((Object)MaplePacketCreator.getShowInventoryFull());
                    return -1;
                }
                if (period > 0L) {
                    nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                c.getSession().write((Object)MaplePacketCreator.addInventorySlot(type, (IItem)nItem));
                c.getSession().write((Object)MaplePacketCreator.enableActions());
            }
        }
        else {
            if (quantity != 1) {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
            final IItem nEquip = ii.getEquipById(itemId);
            if (owner != null) {
                nEquip.setOwner(owner);
            }
            nEquip.setUniqueId(uniqueid);
            if (Flag > 0 && ii.isCash(nEquip.getItemId())) {
                byte flag2 = nEquip.getFlag();
                flag2 |= (byte)ItemFlag.KARMA_USE.getValue();
                nEquip.setFlag(flag2);
            }
            if (period > 0L) {
                nEquip.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }
            newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
            if (newSlot == -1) {
                c.getSession().write((Object)MaplePacketCreator.getInventoryFull());
                c.getSession().write((Object)MaplePacketCreator.getShowInventoryFull());
                return -1;
            }
            c.getSession().write((Object)MaplePacketCreator.addInventorySlot(type, nEquip));
        }
        c.getPlayer().havePartyQuest(itemId);
        return (byte)newSlot;
    }
    
    public static IItem addbyId_Gachapon(final MapleClient c, final int itemId, short quantity) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1) {
            return null;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            c.sendPacket(MaplePacketCreator.showItemUnavailable());
            return null;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, itemId);
            final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!GameConstants.isRechargable(itemId)) {
                IItem nItem = null;
                boolean recieved = false;
                if (existing.size() > 0) {
                    final Iterator<IItem> i = existing.iterator();
                    while (quantity > 0 && i.hasNext()) {
                        nItem = (Item)i.next();
                        final short oldQ = nItem.getQuantity();
                        if (oldQ < slotMax) {
                            recieved = true;
                            final short newQ = (short)Math.min(oldQ + quantity, (int)slotMax);
                            quantity -= (short)(newQ - oldQ);
                            nItem.setQuantity(newQ);
                            c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(1, nItem)));
                        }
                    }
                }
                while (quantity > 0) {
                    final short newQ2 = (short)Math.min((int)quantity, (int)slotMax);
                    if (newQ2 == 0) {
                        break;
                    }
                    quantity -= newQ2;
                    nItem = new Item(itemId, (short)0, newQ2, (byte)0);
                    final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1 && recieved) {
                        return nItem;
                    }
                    if (newSlot == -1) {
                        return null;
                    }
                    recieved = true;
                    c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, nItem)));
                    if (GameConstants.isRechargable(itemId) && quantity == 0) {
                        break;
                    }
                }
                if (recieved) {
                    c.getPlayer().havePartyQuest(nItem.getItemId());
                    return nItem;
                }
                return null;
            }
            else {
                final Item nItem2 = new Item(itemId, (short)0, quantity, (byte)0);
                final short newSlot2 = c.getPlayer().getInventory(type).addItem((IItem)nItem2);
                if (newSlot2 == -1) {
                    return null;
                }
                c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, (IItem)nItem2)));
                c.getPlayer().havePartyQuest(nItem2.getItemId());
                return nItem2;
            }
        }else {
            if (quantity != 1) {
                quantity = 1;
              //  throw new InventoryException("Trying to create equip with non-one quantity");
            }
            IItem item = null;
            switch (itemId) {
                case 1112413: {
                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                    break;
                }
                case 1112414: {
                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                    break;
                }
                case 1112405: {
                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                    break;
                }
                default: {
                    item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                    break;
                }
            }
            final short newSlot3 = c.getPlayer().getInventory(type).addItem(item);
            if (newSlot3 == -1) {
                return null;
            }
            if (item.hasSetOnlyId()) {
                item.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
            }
            c.sendPacket(MaplePacketCreator.modifyInventory(true, new ModifyInventory(0, item)));
            c.getPlayer().havePartyQuest(item.getItemId());
            return item;
        }
    }
    
    public static IItem addbyId_GachaponGM(final MapleClient c, final int itemId, short quantity) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1) {
            return null;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            c.sendPacket(MaplePacketCreator.showItemUnavailable());
            return null;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, itemId);
            if (GameConstants.isRechargable(itemId)) {
                final Item nItem = new Item(itemId, (short)0, quantity, (byte)0);
                return nItem;
            }
            IItem nItem2 = null;
            boolean recieved = false;
            while (quantity > 0) {
                final short newQ = (short)Math.min((int)quantity, (int)slotMax);
                if (newQ == 0) {
                    break;
                }
                quantity -= newQ;
                nItem2 = new Item(itemId, (short)0, newQ, (byte)0);
                recieved = true;
                if (GameConstants.isRechargable(itemId) && quantity == 0) {
                    break;
                }
            }
            if (recieved) {
                return nItem2;
            }
            return null;
        }
        else {
            if (quantity == 1) {
                final IItem item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                return item;
            }
            throw new InventoryException("Trying to create equip with non-one quantity");
        }
    }
    
    public static IItem addbyId_GachaponTime(final MapleClient c, final int itemId, short quantity, final long period) {
        if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.USE).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.ETC).getNextFreeSlot() == -1 || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNextFreeSlot() == -1) {
            return null;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.isPickupRestricted(itemId) && c.getPlayer().haveItem(itemId, 1, true, false)) {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            c.sendPacket(MaplePacketCreator.showItemUnavailable());
            return null;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemId);
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, itemId);
            final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!GameConstants.isRechargable(itemId)) {
                IItem nItem = null;
                boolean recieved = false;
                if (existing.size() > 0) {
                    final Iterator<IItem> i = existing.iterator();
                    while (quantity > 0 && i.hasNext()) {
                        nItem = (Item)i.next();
                        final short oldQ = nItem.getQuantity();
                        if (oldQ < slotMax) {
                            recieved = true;
                            final short newQ = (short)Math.min(oldQ + quantity, (int)slotMax);
                            quantity -= (short)(newQ - oldQ);
                            nItem.setQuantity(newQ);
                            c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(1, nItem)));
                        }
                    }
                }
                while (quantity > 0) {
                    final short newQ2 = (short)Math.min((int)quantity, (int)slotMax);
                    if (newQ2 == 0) {
                        break;
                    }
                    quantity -= newQ2;
                    nItem = new Item(itemId, (short)0, newQ2, (byte)0);
                    final short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1 && recieved) {
                        return nItem;
                    }
                    if (newSlot == -1) {
                        return null;
                    }
                    if (period > 0L) {
                        nItem.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                    }
                    recieved = true;
                    c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, nItem)));
                    if (GameConstants.isRechargable(itemId) && quantity == 0) {
                        break;
                    }
                }
                if (recieved) {
                    c.getPlayer().havePartyQuest(nItem.getItemId());
                    return nItem;
                }
                return null;
            }
            else {
                final Item nItem2 = new Item(itemId, (short)0, quantity, (byte)0);
                final short newSlot2 = c.getPlayer().getInventory(type).addItem((IItem)nItem2);
                if (newSlot2 == -1) {
                    return null;
                }
                if (period > 0L) {
                    nItem2.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, (IItem)nItem2)));
                c.getPlayer().havePartyQuest(nItem2.getItemId());
                return nItem2;
            }
        }
        else {
            if (quantity != 1) {
                throw new InventoryException("Trying to create equip with non-one quantity");
            }
            final IItem item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
            final short newSlot3 = c.getPlayer().getInventory(type).addItem(item);
            if (newSlot3 == -1) {
                return null;
            }
            if (period > 0L) {
                item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
            }
            if (item.hasSetOnlyId()) {
                item.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
            }
            c.sendPacket(MaplePacketCreator.modifyInventory(true, new ModifyInventory(0, item)));
            c.getPlayer().havePartyQuest(item.getItemId());
            return item;
        }
    }
    
    public static boolean addFromDrop(final MapleClient c, final IItem item, final boolean show) {
        return addFromDrop(c, item, show, false, false);
    }

    //拾取到的物品
    public static boolean addFromDrop(final MapleClient c, IItem item, final boolean show, final boolean enhance, final boolean isPetPickup) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItem(item.getItemId(), 1, true, false)) {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            c.sendPacket(MaplePacketCreator.showItemUnavailable());
            return false;
        }
        //限时道具时间赋予
        long expiration = MapleItemInformationProvider.getTimeLimit(item.getItemId());
        if (expiration >= 0L) {
            item.setExpiration(expiration);
        }
        //限时道具时间赋予
        final int before = c.getPlayer().itemQuantity(item.getItemId());
        short quantity = item.getQuantity();
        final MapleInventoryType type = GameConstants.getInventoryType(item.getItemId());
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, item.getItemId());
            final List<IItem> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!GameConstants.isRechargable(item.getItemId())) {
                if (quantity <= 0) {
                    c.sendPacket(MaplePacketCreator.getInventoryFull());
                    c.sendPacket(MaplePacketCreator.showItemUnavailable());
                    return false;
                }
                if (existing.size() > 0) {
                    final Iterator<IItem> i = existing.iterator();
                    while (quantity > 0 && i.hasNext()) {
                        final Item eItem = (Item)i.next();
                        final short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && item.getOwner().equals((Object)eItem.getOwner()) && item.getExpiration() == eItem.getExpiration()) {
                            final short newQ = (short)Math.min(oldQ + quantity, (int)slotMax);
                            quantity -= (short)(newQ - oldQ);
                            eItem.setQuantity(newQ);
                            c.sendPacket(MaplePacketCreator.modifyInventory(!isPetPickup, new ModifyInventory(1, (IItem)eItem)));
                        }
                    }
                }
                while (quantity > 0) {
                    final short newQ2 = (short)Math.min((int)quantity, (int)slotMax);
                    quantity -= newQ2;
                    final Item nItem = new Item(item.getItemId(), (short)0, newQ2, item.getFlag());
                    nItem.setExpiration(item.getExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setPet(item.getPet());
                    final short newSlot = c.getPlayer().getInventory(type).addItem((IItem)nItem);
                    if (newSlot == -1) {
                        c.sendPacket(MaplePacketCreator.getInventoryFull());
                        c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                        item.setQuantity((short)(quantity + newQ2));
                        return false;
                    }
                    c.sendPacket(MaplePacketCreator.modifyInventory(!isPetPickup, new ModifyInventory(0, (IItem)nItem)));
                }
            }
            else {
                final Item nItem2 = new Item(item.getItemId(), (short)0, quantity, item.getFlag());
                nItem2.setExpiration(item.getExpiration());
                nItem2.setOwner(item.getOwner());
                nItem2.setPet(item.getPet());
                final short newSlot2 = c.getPlayer().getInventory(type).addItem((IItem)nItem2);
                if (newSlot2 == -1) {
                    c.sendPacket(MaplePacketCreator.getInventoryFull());
                    c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, (IItem)nItem2)));
                c.sendPacket(MaplePacketCreator.enableActions());
            }
        }
        else {
            if (quantity != 1) {
                return false;
               // throw new RuntimeException("Trying to create equip with non-one quantity");
            }
            if (item.hasSetOnlyId()) {
                item.setEquipOnlyId(MapleEquipOnlyId.getInstance().getNextEquipOnlyId());
            }
            if (enhance) {
                item = checkEnhanced(item, c.getPlayer());
            }
            final short newSlot3 = c.getPlayer().getInventory(type).addItem(item);
            if (newSlot3 == -1) {
                c.sendPacket(MaplePacketCreator.getInventoryFull());
                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                return false;
            }
            c.sendPacket(MaplePacketCreator.modifyInventory(!isPetPickup, new ModifyInventory(0, item)));
        }
        if (item.getQuantity() < 50 || GameConstants.isUpgradeScroll(item.getItemId())) {}
        if (before == 0) {
            switch (item.getItemId()) {
                case 4000516: {
                    c.getPlayer().dropMessage(5, "你已經获得了一個 香爐， 可以到不夜城尋找龍山寺師父對話。");
                    break;
                }
                case 4031875: {
                    c.getPlayer().dropMessage(5, "You have gained a Powder Keg, you can give this in to Aramia of Henesys.");
                    break;
                }
                case 4001246: {
                    c.getPlayer().dropMessage(5, "You have gained a Warm Sun, you can give this in to Maple Tree Hill through @joyce.");
                    break;
                }
                case 4001473: {
                    c.getPlayer().dropMessage(5, "You have gained a Tree Decoration, you can give this in to White Christmas Hill through @joyce.");
                    break;
                }
            }
        }
        c.getPlayer().havePartyQuest(item.getItemId());
        if (show) {
            c.sendPacket(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
        }
        return true;
    }
    
    private static final IItem checkEnhanced(final IItem before, MapleCharacter chr) {
        if (before instanceof Equip) {
            final Equip eq = (Equip)before;
            if (eq.getState() == 0 && (eq.getUpgradeSlots() >= 1 || eq.getLevel() >= 1) && Randomizer.nextInt(100) > 80) {
                eq.resetPotential();
            }
        }
        return before;
    }
    
    private static int rand(final int min, final int max) {
        return Math.abs(Randomizer.rand(min, max));
    }
    
    public static boolean checkSpace(final MapleClient c, final int itemid, int quantity, final String owner) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (c.getPlayer() == null || (ii.isPickupRestricted(itemid) && c.getPlayer().haveItem(itemid, 1, true, false)) || !ii.itemExists(itemid)) {
            c.getSession().writeAndFlush((Object)MaplePacketCreator.enableActions());
            return false;
        }
        if (quantity <= 0 && !GameConstants.isRechargable(itemid)) {
            return false;
        }
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        if (c == null || c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return false;
        }
        if (!type.equals((Object)MapleInventoryType.EQUIP)) {
            final short slotMax = ii.getSlotMax(c, itemid);
            final List<IItem> existing = c.getPlayer().getInventory(type).listById(itemid);
            if (!GameConstants.isRechargable(itemid) && existing.size() > 0) {
                for (final IItem eItem : existing) {
                    final short oldQ = eItem.getQuantity();
                    if (oldQ < slotMax && owner != null && owner.equals((Object)eItem.getOwner())) {
                        final short newQ = (short)Math.min(oldQ + quantity, (int)slotMax);
                        quantity -= newQ - oldQ;
                    }
                    if (quantity <= 0) {
                        break;
                    }
                }
            }
            int numSlotsNeeded;
            if (slotMax > 0 && !GameConstants.isRechargable(itemid)) {
                numSlotsNeeded = (int)Math.ceil((double)quantity / (double)slotMax);
            }
            else {
                numSlotsNeeded = 1;
            }
            return !c.getPlayer().getInventory(type).isFull(numSlotsNeeded - 1);
        }
        return !c.getPlayer().getInventory(type).isFull();
    }
    
    public static void removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, final short quantity, final boolean fromDrop) {
        removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }
    public static void removeFromSlot(MapleClient c, MapleInventoryType type, short slot, long quantity, boolean fromDrop) {
        removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }
    public static void removeFromSlot(MapleClient c, MapleInventoryType type, short slot, long quantity, boolean fromDrop, boolean consume) {
        if (c.getPlayer() != null && c.getPlayer().getInventory(type) != null) {
            IItem item = c.getPlayer().getInventory(type).getItem(slot);
            if (item != null) {
                boolean allowZero = consume && GameConstants.isRechargable(item.getItemId());
                c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
                if (item.getQuantity() == 0L && !allowZero) {
                    c.sendPacket(MaplePacketCreator.modifyInventory(fromDrop, new ModifyInventory(3, item)));
                } else {
                    c.sendPacket(MaplePacketCreator.modifyInventory(fromDrop, new ModifyInventory(1, item)));
                }
            }

        }
    }
    public static boolean removeFromSlot(MapleClient c, MapleInventoryType type, short slot, long quantity, boolean fromDrop, boolean consume, int a) {
        if (c.getPlayer() != null && c.getPlayer().getInventory(type) != null) {
            IItem item = c.getPlayer().getInventory(type).getItem(slot);
            if (item == null) {
                return false;
            } else {
                boolean allowZero = consume && GameConstants.isRechargable(item.getItemId());
                c.getPlayer().getInventory(type).removeItem(slot, (short) quantity, allowZero);
                if (item.getQuantity() == 0L && !allowZero) {
                    c.sendPacket(MaplePacketCreator.modifyInventory(fromDrop, new ModifyInventory(3, item)));
                } else {
                    c.sendPacket(MaplePacketCreator.modifyInventory(fromDrop, new ModifyInventory(1, item)));
                }

                return true;
            }
        } else {
            return false;
        }
    }
    public static void removeFromSlot(final MapleClient c, final MapleInventoryType type, final short slot, final short quantity, final boolean fromDrop, final boolean consume) {
        if (c.getPlayer() == null || c.getPlayer().getInventory(type) == null) {
            return;
        }
        final IItem item = c.getPlayer().getInventory(type).getItem(slot);
        if (item != null) {
            final boolean allowZero = consume && GameConstants.isRechargable(item.getItemId());
            c.getPlayer().getInventory(type).removeItem(slot, quantity, allowZero);
            if (item.getQuantity() == 0 && !allowZero) {
                c.sendPacket(MaplePacketCreator.modifyInventory(fromDrop, new ModifyInventory(3, item)));
            }
            else {
                c.sendPacket(MaplePacketCreator.modifyInventory(fromDrop, new ModifyInventory(1, item)));
            }
        }
    }
    
    public static boolean removeById(final MapleClient c, final MapleInventoryType type, final int itemId, final int quantity, final boolean fromDrop, final boolean consume) {
        int remremove = quantity;
        for (final IItem item : c.getPlayer().getInventory(type).listById(itemId)) {
            if (remremove <= item.getQuantity()) {
                removeFromSlot(c, type, item.getPosition(), (short)remremove, fromDrop, consume);
                remremove = 0;
                break;
            }
            remremove -= item.getQuantity();
            removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume);
        }
        return remremove <= 0;
    }
    
    public static void move(final MapleClient c, final MapleInventoryType type, final short src, final short dst) {
        if (src < 0 || dst < 0) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final IItem source = c.getPlayer().getInventory(type).getItem(src);
        final IItem initialTarget = c.getPlayer().getInventory(type).getItem(dst);
        if (source == null) {
            return;
        }
        if (c.getPlayer().getGMLevel() > 0) {
            c.getPlayer().dropMessage("移动物品代码ID：" + source.getItemId());
        }
        if (LtMS.ConfigValuesMap.get("显示物品编码")==1){
            c.getPlayer().dropMessage("移动物品代码ID：" + source.getItemId());
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        final short oldsrcQ = source.getQuantity();
        final short slotMax = ii.getSlotMax(c, source.getItemId());
        c.getPlayer().getInventory(type).move(src, dst, slotMax);
        final List<ModifyInventory> mods = new ArrayList<ModifyInventory>();
        if (!type.equals((Object)MapleInventoryType.EQUIP) && !type.equals((Object)MapleInventoryType.CASH) && initialTarget != null && initialTarget.getItemId() == source.getItemId() && !GameConstants.isRechargable(source.getItemId())) {
            if (olddstQ + oldsrcQ > slotMax) {
                mods.add(new ModifyInventory(1, source));
                mods.add(new ModifyInventory(1, initialTarget));
            }
            else {
                mods.add(new ModifyInventory(3, source));
                mods.add(new ModifyInventory(1, initialTarget));
            }
        }
        else {
            mods.add(new ModifyInventory(2, source, src));
        }
        c.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
    }
    //更换装备刷新属性
    public static void equip( MapleClient c,  short src,  short dst) {
        Equip source = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
        MapleCharacter chr = c.getPlayer();
        if (source == null) {
            c.sendPacket(MaplePacketCreator.enableActions());
        } else {
            if (source.getItemId() / 10000 == 160) {
                if (MapleItemInformationProvider.getInstance().isCash(source.getItemId())) {
                    dst = -120;
                } else {
                    dst = -20;
                }
            }

            int checkOnlyId = GameConstants.checkMultiOnlyEquip(c.getPlayer(), source.getItemId());
            if (checkOnlyId > 0) {
                c.getPlayer().dropMessage(5, "检测到你身上已经穿戴了联结固有装备 [" + MapleItemInformationProvider.getInstance().getName(checkOnlyId) + "] ，无法继续穿戴这件装备！");
                c.sendPacket(MaplePacketCreator.enableActions());
            } else {
                if (source.getItemId() == 1602008 || source.getItemId() == 1602009 || source.getItemId() == 1602010) {
                    switch (source.getItemId()) {
                        case 1602008:
                            c.getPlayer().setOneTimeLog("轮回等级", -c.getPlayer().getOneTimeLog("轮回等级"));
                            break;
                        case 1602009:
                            c.getPlayer().setOneTimeLog("轮回等级", -c.getPlayer().getOneTimeLog("轮回等级") + 1);
                            break;
                        case 1602010:
                            c.getPlayer().setOneTimeLog("轮回等级", -c.getPlayer().getOneTimeLog("轮回等级") + 2);
                    }

                    if (ServerConfig.version == 85) {
                        c.getPlayer().changeSkillLevel(SkillFactory.getSkill(1025), (byte)1, (byte)1);
                    } else {
                        c.getPlayer().changeSkillLevel(SkillFactory.getSkill(1013), (byte)1, (byte)1);
                    }

                    c.getPlayer().dropMessage(5, "你学会了 “轮回” 技能。");
                }

                int reqlv;
                int dex;
                int _int;
                int luk;
                int hp;
                int mp;
                int watk;
                int matk;
                int wdef;
                int mdef;
                int acc;
                int avoid;
                if ((Integer)LtMS.ConfigValuesMap.get("装备栏强化系统开关") > 0) {
                    EquipFieldEnhancement.EquipField equipField = c.getPlayer().getEquipField(dst);
                    if (equipField != null) {
                        int str = source.getStr() + equipField.getStr();
                        if (str > 32767) {
                            str = 32767;
                        }

                        source.setStr((short)str);
                         dex = source.getDex() + equipField.getDex();
                        if (dex > 32767) {
                            dex = 32767;
                        }
                        source.setDex((short)dex);

                         reqlv = source.getInt() + equipField.getInt();
                        if (reqlv > 32767) {
                            reqlv = 32767;
                        }
                        source.setInt((short)reqlv);

                         luk = source.getLuk() + equipField.getLuk();
                        if (luk > 32767) {
                            luk = 32767;
                        }
                        source.setLuk((short)luk);

                         hp = source.getHp() + equipField.getHp();
                        if (hp > 32767) {
                            hp = 32767;
                        }
                        source.setHp((short)hp);

                         mp = source.getMp() + equipField.getMp();
                        if (mp > 32767) {
                            mp = 32767;
                        }
                        source.setMp((short)mp);

                         watk = source.getWatk() + equipField.getWatk();
                        if (watk > 32767) {
                            watk = 32767;
                        }
                        source.setWatk((short)watk);

                         matk = source.getMatk() + equipField.getMatk();
                        if (matk > 32767) {
                            matk = 32767;
                        }
                        source.setMatk((short)matk);

                         wdef = source.getWdef() + equipField.getWdef();
                        if (wdef > 32767) {
                            wdef = 32767;
                        }
                        source.setWdef((short)wdef);

                         mdef = source.getMdef() + equipField.getMdef();
                        if (mdef > 32767) {
                            mdef = 32767;
                        }
                        source.setMdef((short)mdef);

                         acc = source.getAcc() + equipField.getAcc();
                        if (acc > 32767) {
                            acc = 32767;
                        }
                        source.setAcc((short)acc);

                         avoid = source.getAvoid() + equipField.getAvoid();
                        if (avoid > 32767) {
                            avoid = 32767;
                        }
                        source.setAvoid((short)avoid);

                        int speed = source.getSpeed() + equipField.getSpeed();
                        if (speed > 32767) {
                            speed = 32767;
                        }
                        source.setSpeed((short)speed);

                        int jump = source.getJump() + equipField.getJump();
                        if (jump > 32767) {
                            jump = 32767;
                        }
                        source.setJump((short)jump);

                        if (equipField.getTotalDamage() > 0) {
                            Potential.setPotential(source, (short)11, 29, equipField.getTotalDamage());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了总伤害+" + equipField.getTotalDamage() + "%");
                        }

                        if (equipField.getBossDamage() > 0) {
                            Potential.setPotential(source, (short)12, 30, equipField.getBossDamage());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了BOSS伤害+" + equipField.getBossDamage() + "%");
                        }

                        if (equipField.getNormalDamage() > 0) {
                            Potential.setPotential(source, (short)13, 31, equipField.getNormalDamage());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了小怪伤害+" + equipField.getNormalDamage() + "%");
                        }

                        if (equipField.getStr_p() > 0) {
                            Potential.setPotential(source, (short)14, 5, equipField.getStr_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了力量+" + equipField.getStr_p() + "%");
                        }

                        if (equipField.getDex_p() > 0) {
                            Potential.setPotential(source, (short)15, 6, equipField.getDex_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了敏捷+" + equipField.getDex_p() + "%");
                        }

                        if (equipField.getInt_p() > 0) {
                            Potential.setPotential(source, (short)16, 7, equipField.getInt_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了智力+" + equipField.getInt_p() + "%");
                        }

                        if (equipField.getLuk_p() > 0) {
                            Potential.setPotential(source, (short)17, 8, equipField.getLuk_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了运气+" + equipField.getLuk_p() + "%");
                        }

                        if (equipField.getHp_p() > 0) {
                            Potential.setPotential(source, (short)18, 24, equipField.getHp_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了maxHp+" + equipField.getHp_p() + "%");
                        }

                        if (equipField.getMp_p() > 0) {
                            Potential.setPotential(source, (short)19, 26, equipField.getMp_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了maxMp+" + equipField.getMp_p() + "%");
                        }

                        if (equipField.getWatk_p() > 0) {
                            Potential.setPotential(source, (short)20, 13, equipField.getWatk_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了攻击力+" + equipField.getWatk_p() + "%");
                        }

                        if (equipField.getMatk_p() > 0) {
                            Potential.setPotential(source, (short)21, 14, equipField.getWatk_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了魔法力+" + equipField.getWatk_p() + "%");
                        }

                        if (equipField.getWdef_p() > 0) {
                            Potential.setPotential(source, (short)22, 16, equipField.getWdef_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了物理防御力+" + equipField.getWdef_p() + "%");
                        }

                        if (equipField.getMdef_p() > 0) {
                            Potential.setPotential(source, (short)23, 18, equipField.getMdef_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了魔法防御力+" + equipField.getMdef_p() + "%");
                        }

                        if (equipField.getAcc_p() > 0) {
                            Potential.setPotential(source, (short)24, 20, equipField.getAcc_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了命中率+" + equipField.getAcc_p() + "%");
                        }

                        if (equipField.getAvoid_p() > 0) {
                            Potential.setPotential(source, (short)25, 22, equipField.getAvoid_p());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了回避率+" + equipField.getAvoid_p() + "%");
                        }

                        if (equipField.getMustKill() > 0) {
                            Potential.setPotential(source, (short)26, 32, equipField.getMustKill());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了普通怪物必杀概率+" + equipField.getMustKill() + "%");
                        }

                        if (equipField.getInvincible() > 0) {
                            Potential.setPotential(source, (short)27, 33, equipField.getInvincible());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了无敌概率+" + equipField.getInvincible() + "%");
                        }

                        if (equipField.getStrong() > 0) {
                            Potential.setPotential(source, (short)28, 34, equipField.getStrong());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了坚韧+" + equipField.getStrong() + "%（受到的伤害减少）");
                        }

                        if (equipField.getSuckHp() > 0) {
                            Potential.setPotential(source, (short)29, 35, equipField.getSuckHp());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了吸血+" + equipField.getSuckHp() + "%（攻击时恢复HP上限10%的概率）");
                        }

                        if (equipField.getSuckMp() > 0) {
                            Potential.setPotential(source, (short)30, 36, equipField.getSuckMp());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了吸魔+" + equipField.getSuckMp() + "%（攻击时恢复MP上限10%的概率）");
                        }

                        if (equipField.getGrowableHp() > 0) {
                            Potential.setPotential(source, (short)31, 37, equipField.getGrowableHp());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了茁壮生命+" + equipField.getGrowableHp() + "（升级时额外获得HP上限数量）");
                        }

                        if (equipField.getGrowableMp() > 0) {
                            Potential.setPotential(source, (short)32, 38, equipField.getGrowableMp());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了茁壮魔力+" + equipField.getGrowableMp() + "（升级时额外获得MP上限数量）");
                        }

                        if (equipField.getMoreExp() > 0) {
                            Potential.setPotential(source, (short)33, 39, equipField.getMoreExp());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了慧根+" + equipField.getMoreExp() + "%（狩猎经验倍率增加）");
                        }

                        if (equipField.getMoreMeso() > 0) {
                            Potential.setPotential(source, (short)34, 40, equipField.getMoreMeso());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了财运+" + equipField.getMoreMeso() + "%（狩猎金币倍率增加）");
                        }

                        if (equipField.getMoreDrop() > 0) {
                            Potential.setPotential(source, (short)35, 41, equipField.getMoreDrop());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了财运+" + equipField.getMoreDrop() + "%（狩猎掉落倍率增加）");
                        }

                        if (equipField.getRevive() > 0) {
                            Potential.setPotential(source, (short)36, 42, equipField.getRevive());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了涅槃+" + equipField.getRevive() + "%（死亡时原地满血复活的概率）");
                        }

                        if (equipField.getSummonMob() > 0) {
                            Potential.setPotential(source, (short)37, 43, equipField.getSummonMob());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了怨念+" + equipField.getSummonMob() + "%（击杀普通怪物时招来怪物伙伴的概率）");
                        }

                        if (equipField.getConsumeRecover() > 0) {
                            Potential.setPotential(source, (short)38, 44, equipField.getConsumeRecover());
                            c.getPlayer().dropMessage(5, "该装备栏位激活了药灵+" + equipField.getConsumeRecover() + "%（恢复类药水的恢复值增加）");
                        }

                        c.getPlayer().reFreshItem(source);
                    }
                }

                boolean itemChanged = false;
                if (MapleItemInformationProvider.getInstance().isUntradeableOnEquip(source.getItemId())) {
                    if (!ItemFlag.UNTRADEABLE.check(source.getFlag())) {
                        source.setFlag((byte)(source.getFlag() + ItemFlag.UNTRADEABLE.getValue()));
                    }

                    itemChanged = true;
                }

                if (GameConstants.isGMEquip(source.getItemId()) && !c.getPlayer().isGM() && !c.getChannelServer().CanGMItem()) {
                    c.getPlayer().dropMessage(1, "只有管理员能装备这件道具。");
                    c.getPlayer().removeAll(source.getItemId(), true);
                    c.sendPacket(MaplePacketCreator.enableActions());
                } else {
                    if (c.getPlayer().getDebugMessage()) {
                        c.getPlayer().dropMessage("穿装备: src : " + src + " dst : " + dst + " 代码：" + source.getItemId());
                    }

                    IItem shield;
                    Equip target;
                    if (dst == -6) {
                        shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-5);
                        if (shield != null && isOverall(shield.getItemId())) {
                            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                                c.sendPacket(MaplePacketCreator.getInventoryFull());
                                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                                return;
                            }

                            unequip(c, (short)-5, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                        }
                    } else if (dst == -5) {
                        shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-6);
                        if (shield != null && isOverall(source.getItemId())) {
                            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                                c.sendPacket(MaplePacketCreator.getInventoryFull());
                                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                                return;
                            }

                            unequip(c, (short)-6, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                        }
                    } else if (dst == -10) {
                        target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-11);
                        if (target != null && MapleItemInformationProvider.getInstance().isTwoHanded(target.getItemId())) {
                            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                                c.sendPacket(MaplePacketCreator.getInventoryFull());
                                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                                return;
                            }

                            unequip(c, (short)-11, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                        }
                    } else if (dst == -11) {
                        shield = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-10);
                        if (shield != null && MapleItemInformationProvider.getInstance().isTwoHanded(source.getItemId())) {
                            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).isFull()) {
                                c.sendPacket(MaplePacketCreator.getInventoryFull());
                                c.sendPacket(MaplePacketCreator.getShowInventoryFull());
                                return;
                            }

                            unequip(c, (short)-10, c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNextFreeSlot());
                        }
                    }

                    if (dst == -18 && c.getPlayer().getMount() != null) {
                        c.getPlayer().getMount().setItemId(source.getItemId());
                    }

                    if (source.getItemId() == 1122017 || source.getItemId() == 1122086 || source.getItemId() == 1122207 || source.getItemId() == 1122215) {
                        c.getPlayer().startFairySchedule(true, true);
                    }

                    source = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(src);
                    target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(dst);
                    c.getPlayer().脱装备防滑检测(target);
                    c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(src);
                    if (target != null) {
                        if ((Integer)LtMS.ConfigValuesMap.get("装备栏强化系统开关") > 0) {
                            EquipFieldEnhancement.EquipField equipField = c.getPlayer().getEquipField(dst);
                            if (equipField != null) {
                                reqlv = target.getStr() - equipField.getStr();
                                if (reqlv < 0) {
                                    reqlv = 0;
                                }

                                target.setStr((short)reqlv);
                                dex = target.getDex() - equipField.getDex();
                                if (dex < 0) {
                                    dex = 0;
                                }

                                target.setDex((short)dex);
                                _int = target.getInt() - equipField.getInt();
                                if (_int < 0) {
                                    _int = 0;
                                }

                                target.setInt((short)_int);
                                luk = target.getLuk() - equipField.getLuk();
                                if (luk < 0) {
                                    luk = 0;
                                }

                                target.setLuk((short)luk);
                                hp = target.getHp() - equipField.getHp();
                                if (hp < 0) {
                                    hp = 0;
                                }

                                target.setHp((short)hp);
                                mp = target.getMp() - equipField.getMp();
                                if (mp < 0) {
                                    mp = 0;
                                }

                                target.setMp((short)mp);
                                watk = target.getWatk() - equipField.getWatk();
                                if (watk < 0) {
                                    watk = 0;
                                }

                                target.setWatk((short)watk);
                                matk = target.getMatk() - equipField.getMatk();
                                if (matk < 0) {
                                    matk = 0;
                                }

                                target.setMatk((short)matk);
                                wdef = target.getWdef() - equipField.getWdef();
                                if (wdef < 0) {
                                    wdef = 0;
                                }

                                target.setWdef((short)wdef);
                                mdef = target.getMdef() - equipField.getMdef();
                                if (mdef < 0) {
                                    mdef = 0;
                                }

                                target.setMdef((short)mdef);
                                acc = target.getAcc() - equipField.getAcc();
                                if (acc < 0) {
                                    acc = 0;
                                }

                                target.setAcc((short)acc);
                                avoid = target.getAvoid() - equipField.getAvoid();
                                if (avoid < 0) {
                                    avoid = 0;
                                }

                                target.setAvoid((short)avoid);
                                int speed = target.getSpeed() - equipField.getSpeed();
                                if (speed < 0) {
                                    speed = 0;
                                }

                                target.setSpeed((short)speed);
                                int jump = target.getJump() - equipField.getJump();
                                if (jump < 0) {
                                    jump = 0;
                                }

                                target.setJump((short)jump);
                                if (equipField.getTotalDamage() > 0) {
                                    Potential.setPotential(target, (short)11, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，总伤害-" + equipField.getTotalDamage() + "%");
                                }

                                if (equipField.getBossDamage() > 0) {
                                    Potential.setPotential(target, (short)12, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，BOSS伤害-" + equipField.getBossDamage() + "%");
                                }

                                if (equipField.getNormalDamage() > 0) {
                                    Potential.setPotential(target, (short)13, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，小怪伤害-" + equipField.getNormalDamage() + "%");
                                }

                                if (equipField.getStr_p() > 0) {
                                    Potential.setPotential(target, (short)14, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，力量-" + equipField.getStr_p() + "%");
                                }

                                if (equipField.getDex_p() > 0) {
                                    Potential.setPotential(target, (short)15, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，敏捷-" + equipField.getDex_p() + "%");
                                }

                                if (equipField.getInt_p() > 0) {
                                    Potential.setPotential(target, (short)16, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，智力-" + equipField.getInt_p() + "%");
                                }

                                if (equipField.getLuk_p() > 0) {
                                    Potential.setPotential(target, (short)17, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，运气-" + equipField.getLuk_p() + "%");
                                }

                                if (equipField.getHp_p() > 0) {
                                    Potential.setPotential(target, (short)18, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxHp-" + equipField.getHp_p() + "%");
                                }

                                if (equipField.getMp_p() > 0) {
                                    Potential.setPotential(target, (short)19, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxMp-" + equipField.getMp_p() + "%");
                                }

                                if (equipField.getWatk_p() > 0) {
                                    Potential.setPotential(target, (short)20, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，攻击力-" + equipField.getWatk_p() + "%");
                                }

                                if (equipField.getMatk_p() > 0) {
                                    Potential.setPotential(target, (short)21, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法力-" + equipField.getWatk_p() + "%");
                                }

                                if (equipField.getWdef_p() > 0) {
                                    Potential.setPotential(target, (short)22, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，物理防御力-" + equipField.getWdef_p() + "%");
                                }

                                if (equipField.getMdef_p() > 0) {
                                    Potential.setPotential(target, (short)23, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法防御力-" + equipField.getMdef_p() + "%");
                                }

                                if (equipField.getAcc_p() > 0) {
                                    Potential.setPotential(target, (short)24, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，命中率-" + equipField.getAcc_p() + "%");
                                }

                                if (equipField.getAvoid_p() > 0) {
                                    Potential.setPotential(target, (short)25, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，回避率-" + equipField.getAvoid_p() + "%");
                                }

                                if (equipField.getMustKill() > 0) {
                                    Potential.setPotential(target, (short)26, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，普通怪物必杀概率-" + equipField.getMustKill() + "%");
                                }

                                if (equipField.getInvincible() > 0) {
                                    Potential.setPotential(target, (short)27, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，无敌概率-" + equipField.getInvincible() + "%");
                                }

                                if (equipField.getStrong() > 0) {
                                    Potential.setPotential(target, (short)28, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，坚韧-" + equipField.getStrong() + "%（受到的伤害减少）");
                                }

                                if (equipField.getSuckHp() > 0) {
                                    Potential.setPotential(target, (short)29, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，吸血-" + equipField.getSuckHp() + "%（攻击时恢复HP上限10%的概率）");
                                }

                                if (equipField.getSuckMp() > 0) {
                                    Potential.setPotential(target, (short)30, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，吸魔-" + equipField.getSuckMp() + "%（攻击时恢复MP上限10%的概率）");
                                }

                                if (equipField.getGrowableHp() > 0) {
                                    Potential.setPotential(target, (short)31, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，茁壮生命-" + equipField.getGrowableHp() + "（升级时额外获得HP上限数量）");
                                }

                                if (equipField.getGrowableMp() > 0) {
                                    Potential.setPotential(target, (short)32, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，茁壮魔力-" + equipField.getGrowableMp() + "（升级时额外获得MP上限数量）");
                                }

                                if (equipField.getMoreExp() > 0) {
                                    Potential.setPotential(target, (short)33, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，慧根-" + equipField.getMoreExp() + "%（狩猎经验倍率增加）");
                                }

                                if (equipField.getMoreMeso() > 0) {
                                    Potential.setPotential(target, (short)34, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，财运-" + equipField.getMoreMeso() + "%（狩猎金币倍率增加）");
                                }

                                if (equipField.getMoreDrop() > 0) {
                                    Potential.setPotential(target, (short)35, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，财运-" + equipField.getMoreDrop() + "%（狩猎掉落倍率增加）");
                                }

                                if (equipField.getRevive() > 0) {
                                    Potential.setPotential(target, (short)36, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，涅槃-" + equipField.getRevive() + "%（死亡时原地满血复活的概率）");
                                }

                                if (equipField.getSummonMob() > 0) {
                                    Potential.setPotential(target, (short)37, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，怨念-" + equipField.getSummonMob() + "%（击杀普通怪物时招来怪物伙伴的概率）");
                                }

                                if (equipField.getConsumeRecover() > 0) {
                                    Potential.setPotential(target, (short)38, 0, 0);
                                    c.getPlayer().dropMessage(5, "该装备栏位取消装备，药灵-" + equipField.getConsumeRecover() + "%（恢复类药水的恢复值增加）");
                                }

                                c.getPlayer().reFreshItem(target);
                            }
                        }

                        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(dst);
                    }

                    List<ModifyInventory> mods = new ArrayList();
                    if (itemChanged) {
                        mods.add(new ModifyInventory(3, source));
                        mods.add(new ModifyInventory(0, source.copy()));
                    }

                    source.setPosition(dst);
                    c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(source);
                    if (target != null) {
                        target.setPosition(src);
                        c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(target);
                    }

                    if (c.getPlayer().getBuffedValue(MapleBuffStat.BOOSTER) != null && isWeapon(source.getItemId())) {
                        c.getPlayer().cancelBuffStats(new MapleBuffStat[]{MapleBuffStat.BOOSTER});
                    }
                ///chr.set套装伤害加成(tzjc.check_tz(chr));
                mods.add(new ModifyInventory(2, source, src));
                c.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
                reqlv = MapleItemInformationProvider.getInstance().getReqLevel(source.getItemId());
                if (reqlv > c.getPlayer().getLevel() + c.getPlayer().getStat().levelBonus && !c.getPlayer().isGM()) {
                    FileoutputUtil.logToFile("logs/Hack/Ban/修改封包.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ") <等级: " + c.getPlayer().getLevel() + " > 修改装备(" + source.getItemId() + ")封包，穿上装备时封锁。 该装备需求等级: " + reqlv);
                    Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁系统] " + c.getPlayer().getName() + " 因为修改封包而被管理员永久停权。"));
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密语]  " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ") <等级: " + c.getPlayer().getLevel() + " > 修改装备(" + source.getItemId() + ")封包，穿上装备时封锁。 该装备需求等级: " + reqlv));
                    if (Game.自动封挂) {
                        c.getPlayer().ban("修改封包", true, true, false);
                    }
                    c.getSession().close();
                } else {
                    c.getPlayer().equipChanged();
                    if ((Integer) LtMS.ConfigValuesMap.get("潜能系统开关") > 0 && (Potential.isPotentialExist(source) || Potential.getPotentialQuantity(target) > 0)) {
                        c.getPlayer().getStat().recalcLocalStats();
                        c.getPlayer().givePotentialBuff(Potential.buffItemId, Potential.duration, true);
                    }

                    c.getPlayer().刷新防滑状态();
                    if ((Integer) LtMS.ConfigValuesMap.get("穿脱装备存档开关") > 0) {
                        c.getPlayer().道具存档();
                        c.getPlayer().setPower(0L);
                        c.getPlayer().getPower();
                    }

                }
                chr.set套装伤害加成(tzjc.check_tz(chr));
            }
        }
        }

        c.getPlayer().equipChanged();
    }
    
    private static boolean isOverall(final int itemId) {
        return itemId / 10000 == 105;
    }
    
    private static boolean isWeapon(final int itemId) {
        return itemId >= 1302000 && itemId < 1492024;
    }

    public static void unequip(final MapleClient c, final short src, final short dst) {
        Equip source = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(src);
        Equip target = (Equip)c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem(dst);
        c.getPlayer().脱装备防滑检测(source);
        if (source.getItemId() == 1602008 || source.getItemId() == 1602009 || source.getItemId() == 1602010) {
            if (ServerConfig.version == 85) {
                c.getPlayer().changeSkillLevel(SkillFactory.getSkill(1025), (byte)0, (byte)0);
            } else {
                c.getPlayer().changeSkillLevel(SkillFactory.getSkill(1013), (byte)0, (byte)0);
            }

            c.getPlayer().deleteOneTimeLog("轮回等级");
            c.getPlayer().dropMessage(5, "你遗忘了 “轮回” 技能。");
        }

        if (dst >= 0) {
            if (source != null) {
                if (target != null && src <= 0) {
                    c.sendPacket(MaplePacketCreator.getInventoryFull());
                } else {
                    if (c.getPlayer().getDebugMessage()) {
                        c.getPlayer().dropMessage("脱装备: src : " + src + " dst : " + dst + " 代码：" + source.getItemId());
                    }

                    if (source.getItemId() == 1122017 || source.getItemId() == 1122086 || source.getItemId() == 1122207 || source.getItemId() == 1122215) {
                        c.getPlayer().cancelFairySchedule(true);
                    }

                    if ((Integer)LtMS.ConfigValuesMap.get("装备栏强化系统开关") > 0) {
                        EquipFieldEnhancement.EquipField equipField = c.getPlayer().getEquipField(src);
                        if (equipField != null) {
                            int str = source.getStr() - equipField.getStr();
                            if (str < 0) {
                                str = 0;
                            }

                            source.setStr((short)str);
                            int dex = source.getDex() - equipField.getDex();
                            if (dex < 0) {
                                dex = 0;
                            }

                            source.setDex((short)dex);
                            int _int = source.getInt() - equipField.getInt();
                            if (_int < 0) {
                                _int = 0;
                            }

                            source.setInt((short)_int);
                            int luk = source.getLuk() - equipField.getLuk();
                            if (luk < 0) {
                                luk = 0;
                            }

                            source.setLuk((short)luk);
                            int hp = source.getHp() - equipField.getHp();
                            if (hp < 0) {
                                hp = 0;
                            }

                            source.setHp((short)hp);
                            int mp = source.getMp() - equipField.getMp();
                            if (mp < 0) {
                                mp = 0;
                            }

                            source.setMp((short)mp);
                            int watk = source.getWatk() - equipField.getWatk();
                            if (watk < 0) {
                                watk = 0;
                            }

                            source.setWatk((short)watk);
                            int matk = source.getMatk() - equipField.getMatk();
                            if (matk < 0) {
                                matk = 0;
                            }

                            source.setMatk((short)matk);
                            int wdef = source.getWdef() - equipField.getWdef();
                            if (wdef < 0) {
                                wdef = 0;
                            }

                            source.setWdef((short)wdef);
                            int mdef = source.getMdef() - equipField.getMdef();
                            if (mdef < 0) {
                                mdef = 0;
                            }

                            source.setMdef((short)mdef);
                            int acc = source.getAcc() - equipField.getAcc();
                            if (acc < 0) {
                                acc = 0;
                            }

                            source.setAcc((short)acc);
                            int avoid = source.getAvoid() - equipField.getAvoid();
                            if (avoid < 0) {
                                avoid = 0;
                            }

                            source.setAvoid((short)avoid);
                            int speed = source.getSpeed() - equipField.getSpeed();
                            if (speed < 0) {
                                speed = 0;
                            }

                            source.setSpeed((short)speed);
                            int jump = source.getJump() - equipField.getJump();
                            if (jump < 0) {
                                jump = 0;
                            }

                            source.setJump((short)jump);
                            if (equipField.getTotalDamage() > 0) {
                                Potential.setPotential(source, (short)11, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，总伤害-" + equipField.getTotalDamage() + "%");
                            }

                            if (equipField.getBossDamage() > 0) {
                                Potential.setPotential(source, (short)12, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，BOSS伤害-" + equipField.getBossDamage() + "%");
                            }

                            if (equipField.getNormalDamage() > 0) {
                                Potential.setPotential(source, (short)13, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，小怪伤害-" + equipField.getNormalDamage() + "%");
                            }

                            if (equipField.getStr_p() > 0) {
                                Potential.setPotential(source, (short)14, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，力量-" + equipField.getStr_p() + "%");
                            }

                            if (equipField.getDex_p() > 0) {
                                Potential.setPotential(source, (short)15, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，敏捷-" + equipField.getDex_p() + "%");
                            }

                            if (equipField.getInt_p() > 0) {
                                Potential.setPotential(source, (short)16, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，智力-" + equipField.getInt_p() + "%");
                            }

                            if (equipField.getLuk_p() > 0) {
                                Potential.setPotential(source, (short)17, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，运气-" + equipField.getLuk_p() + "%");
                            }

                            if (equipField.getHp_p() > 0) {
                                Potential.setPotential(source, (short)18, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxHp-" + equipField.getHp_p() + "%");
                            }

                            if (equipField.getMp_p() > 0) {
                                Potential.setPotential(source, (short)19, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxMp-" + equipField.getMp_p() + "%");
                            }

                            if (equipField.getWatk_p() > 0) {
                                Potential.setPotential(source, (short)20, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，攻击力-" + equipField.getWatk_p() + "%");
                            }

                            if (equipField.getMatk_p() > 0) {
                                Potential.setPotential(source, (short)21, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法力-" + equipField.getWatk_p() + "%");
                            }

                            if (equipField.getWdef_p() > 0) {
                                Potential.setPotential(source, (short)22, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，物理防御力-" + equipField.getWdef_p() + "%");
                            }

                            if (equipField.getMdef_p() > 0) {
                                Potential.setPotential(source, (short)23, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法防御力-" + equipField.getMdef_p() + "%");
                            }

                            if (equipField.getAcc_p() > 0) {
                                Potential.setPotential(source, (short)24, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，命中率-" + equipField.getAcc_p() + "%");
                            }

                            if (equipField.getAvoid_p() > 0) {
                                Potential.setPotential(source, (short)25, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，回避率-" + equipField.getAvoid_p() + "%");
                            }

                            if (equipField.getMustKill() > 0) {
                                Potential.setPotential(source, (short)26, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，普通怪物必杀概率-" + equipField.getMustKill() + "%");
                            }

                            if (equipField.getInvincible() > 0) {
                                Potential.setPotential(source, (short)27, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，无敌概率-" + equipField.getInvincible() + "%");
                            }

                            if (equipField.getStrong() > 0) {
                                Potential.setPotential(source, (short)28, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，坚韧-" + equipField.getStrong() + "%（受到的伤害减少）");
                            }

                            if (equipField.getSuckHp() > 0) {
                                Potential.setPotential(source, (short)29, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，吸血-" + equipField.getSuckHp() + "%（攻击时恢复HP上限10%的概率）");
                            }

                            if (equipField.getSuckMp() > 0) {
                                Potential.setPotential(source, (short)30, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，吸魔-" + equipField.getSuckMp() + "%（攻击时恢复MP上限10%的概率）");
                            }

                            if (equipField.getGrowableHp() > 0) {
                                Potential.setPotential(source, (short)31, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，茁壮生命-" + equipField.getGrowableHp() + "（升级时额外获得HP上限数量）");
                            }

                            if (equipField.getGrowableMp() > 0) {
                                Potential.setPotential(source, (short)32, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，茁壮魔力-" + equipField.getGrowableMp() + "（升级时额外获得MP上限数量）");
                            }

                            if (equipField.getMoreExp() > 0) {
                                Potential.setPotential(source, (short)33, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，慧根-" + equipField.getMoreExp() + "%（狩猎经验倍率增加）");
                            }

                            if (equipField.getMoreMeso() > 0) {
                                Potential.setPotential(source, (short)34, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，财运-" + equipField.getMoreMeso() + "%（狩猎金币倍率增加）");
                            }

                            if (equipField.getMoreDrop() > 0) {
                                Potential.setPotential(source, (short)35, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，财运-" + equipField.getMoreDrop() + "%（狩猎掉落倍率增加）");
                            }

                            if (equipField.getRevive() > 0) {
                                Potential.setPotential(source, (short)36, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，涅槃-" + equipField.getRevive() + "%（死亡时原地满血复活的概率）");
                            }

                            if (equipField.getSummonMob() > 0) {
                                Potential.setPotential(source, (short)37, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，怨念-" + equipField.getSummonMob() + "%（击杀普通怪物时招来怪物伙伴的概率）");
                            }

                            if (equipField.getConsumeRecover() > 0) {
                                Potential.setPotential(source, (short)38, 0, 0);
                                c.getPlayer().dropMessage(5, "该装备栏位取消装备，药灵-" + equipField.getConsumeRecover() + "%（恢复类药水的恢复值增加）");
                            }

                            c.getPlayer().reFreshItem(source);
                        }
                    }

                    c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).removeSlot(src);
                    if (target != null) {
                        c.getPlayer().getInventory(MapleInventoryType.EQUIP).removeSlot(dst);
                    }

                    source.setPosition(dst);
                    c.getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(source);
                    if (target != null) {
                        target.setPosition(src);
                        c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).addFromDB(target);
                    }

                    c.sendPacket(MaplePacketCreator.modifyInventory(true, Collections.singletonList(new ModifyInventory(2, source, src))));
                    int reqlv = MapleItemInformationProvider.getInstance().getReqLevel(source.getItemId());
                    if (reqlv > c.getPlayer().getLevel() + c.getPlayer().getStat().levelBonus && !c.getPlayer().isGM()) {
                        FileoutputUtil.logToFile("logs/Hack/Ban/修改封包.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家：" + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ") <等级: " + c.getPlayer().getLevel() + " > 修改装备(" + source.getItemId() + ")封包，脱除装备时封锁。 该装备需求等级: " + reqlv);
                        if (Game.自动封挂) {
                            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁系统] " + c.getPlayer().getName() + " 因为修改封包而被管理员永久停权。"));
                            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密语]  " + c.getPlayer().getName() + "(" + c.getPlayer().getId() + ") <等级: " + c.getPlayer().getLevel() + " > 修改装备(" + source.getItemId() + ")封包，脱除装备时封锁。 该装备需求等级: " + reqlv));
                            c.getPlayer().ban("修改封包", true, true, false);
                            c.getSession().close();
                            return;
                        }
                    }

                    c.getPlayer().equipChanged();
                    if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0 && Potential.isPotentialExist(source)) {
                        c.getPlayer().getStat().recalcLocalStats();
                        c.getPlayer().givePotentialBuff(Potential.buffItemId, Potential.duration, true);
                    }

                    c.getPlayer().刷新防滑状态();
                    if ((Integer)LtMS.ConfigValuesMap.get("穿脱装备存档开关") > 0) {
                        c.getPlayer().道具存档();
                        c.getPlayer().setPower(0L);
                        c.getPlayer().getPower();
                    }

                }
            }
        }
    }
    
    public static boolean dropCs(final MapleClient c, final MapleInventoryType type, final short src, final short quantity) {
        return drop(c, type, src, quantity, false, true);
    }
    
    public static boolean drop( MapleClient c,  MapleInventoryType type,  short src,  short quantity) {
        return drop(c, type, src, quantity, false);
    }
    
    public static boolean drop( MapleClient c,  MapleInventoryType type,  short src,  short quantity,  boolean npcInduced) {
        return drop(c, type, src, quantity, npcInduced, false);
    }
    
    public static boolean drop( MapleClient c, MapleInventoryType type,  short src,  short quantity,  boolean npcInduced,  boolean cs) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (quantity < 0) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
        final int 丢出物品开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"丢出物品开关"));
        if (丢出物品开关 == 0) {
            c.getPlayer().dropMessage(1, "管理员从后台关闭了物品丢出功能。");
            c.sendPacket(MaplePacketCreator.enableActions());
            return false;
        }
        IItem source = c.getPlayer().getInventory(type).getItem(src);

        try {
            if (src == -7) {
                c.getPlayer().脱装备防滑检测(source);
            }
        } catch (Exception e) {
            服务端输出信息.println_err("【错误】丢出道具异常，原因：" + e);

        }

        try {
            if (src < 0) {
                type = MapleInventoryType.EQUIPPED;
            }
            if (c.getPlayer() == null) {
                return false;
            }
            if (!cs && ii.isCash(source.getItemId())) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
            //检测是否丢出超过数量的物品
            if(!c.getPlayer().haveItem(source.getItemId(),quantity)){
                c.getPlayer().dropMessage(1, "丢弃失败,请解卡或大退游戏再试");
                Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 强丢物品 账号 " + c.getAccountName() + " 账号ID " + c.getAccID() + " 角色名 " + c.getPlayer().getName() + " 角色ID " + c.getPlayer().getId() + " 類型 " + (Object)type + " src " + (int)src + (int)quantity + " 物品 " + ii.getName(source.getItemId()) + " (" + source.getItemId() + ") x" + (int)quantity));
                //Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[全服公告] 账号 " + c.getAccountName() + " 账号ID " + c.getAccID() + " 角色名 " + c.getPlayer().getName() + " 角色ID " + c.getPlayer().getId() +"丢弃物品异常 " + ii.getName(source.getItemId()) + " (" + source.getItemId() + ") x" + (int)quantity+"警告一次,再来就封了。"));
                FileoutputUtil.logToFile("logs/Data/强丢物品.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + c.getAccountName() + " 账号ID " + c.getAccID() + " 角色名 " + c.getPlayer().getName() + " 角色ID " + c.getPlayer().getId() + " 類型 " + (Object)type + " src " + (int)src + (int)quantity + " 物品 " + ii.getName(source.getItemId()) + " (" + source.getItemId() + ") x" + (int)quantity);
                return false;
            }

            if (source.getItemId() == 4110010) {
                c.getPlayer().dropMessage(1, "無法丟落該物品。");
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
            if (source.getItemId() == 2340000 || source.getItemId() == 2049100) {
                if (WorldConstants.DropItem) {
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 危險貴重物品 账号 " + c.getAccountName() + " 账号ID " + c.getAccID() + " 角色名 " + c.getPlayer().getName() + " 角色ID " + c.getPlayer().getId() + " 類型 " + (Object)type + " src " + (int)src + (int)quantity + " 物品 " + ii.getName(source.getItemId()) + " (" + source.getItemId() + ") x" + (int)quantity));
                }
                FileoutputUtil.logToFile("logs/Data/丟棄貴重物品.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + c.getAccountName() + " 账号ID " + c.getAccID() + " 角色名 " + c.getPlayer().getName() + " 角色ID " + c.getPlayer().getId() + " 類型 " + (Object)type + " src " + (int)src + (int)quantity + " 物品 " + ii.getName(source.getItemId()) + " (" + source.getItemId() + ") x" + (int)quantity);
            }

            if (quantity < 0L || source == null || !npcInduced && GameConstants.isPet(source.getItemId()) || quantity == 0L && !GameConstants.isRechargable(source.getItemId())) {
                c.sendPacket(MaplePacketCreator.enableActions());
                if (quantity < 0L && Game.自动封挂 && !c.getPlayer().isGM()) {
                    c.getPlayer().ban(c.getPlayer().getName() + "复制物品", true, true, false);
                    c.getPlayer().getClient().getSession().close();
                }

                return false;
            }
            if (source == null || (!npcInduced && GameConstants.isPet(source.getItemId()))) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
            if (!cs && ii.isCash(source.getItemId())) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
            final byte flag = source.getFlag();
            if (quantity > source.getQuantity()) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
            if (ItemFlag.LOCK.check((int)flag) || (quantity != 1 && type == MapleInventoryType.EQUIP)) {
                c.sendPacket(MaplePacketCreator.enableActions());
                return false;
            }
            final Point dropPos = new Point(c.getPlayer().getPosition());
            c.getPlayer().getCheatTracker().checkDrop();
            if (quantity < source.getQuantity() && !GameConstants.isRechargable(source.getItemId())) {
                final IItem target = source.copy();
                target.setQuantity(quantity);
                source.setQuantity((short)(source.getQuantity() - quantity));
                c.sendPacket(MaplePacketCreator.dropInventoryItemUpdate(type, source));
                if (ii.isDropRestricted(target.getItemId()) || ii.isAccountShared(target.getItemId())) {
                    if (ItemFlag.KARMA_EQ.check((int)flag)) {
                        target.setFlag((byte)(flag - ItemFlag.KARMA_EQ.getValue()));
                        c.getPlayer().getMap().spawnItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                    }
                    else if (ItemFlag.KARMA_USE.check((int)flag)) {
                        target.setFlag((byte)(flag - ItemFlag.KARMA_USE.getValue()));
                        c.getPlayer().getMap().spawnItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                    }
                    else {
                        c.getPlayer().getMap().disappearingItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), target, dropPos);
                    }
                }
                else if (GameConstants.isPet(source.getItemId()) || ItemFlag.UNTRADEABLE.check((int)flag)) {
                    c.getPlayer().getMap().disappearingItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), target, dropPos);
                }
                else {
                    c.getPlayer().getMap().spawnItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), target, dropPos, true, true);
                }
            } else {

                if (type.getType() == MapleInventoryType.EQUIPPED.getType() && (Integer)LtMS.ConfigValuesMap.get("装备栏强化系统开关") > 0) {
                    Equip equip = (Equip)source;
                    EquipFieldEnhancement.EquipField equipField = c.getPlayer().getEquipField(src);
                    if (equipField != null) {
                        int str = equip.getStr() - equipField.getStr();
                        if (str < 0) {
                            str = 0;
                        }

                        equip.setStr((short)str);
                        int dex = equip.getDex() - equipField.getDex();
                        if (dex < 0) {
                            dex = 0;
                        }

                        equip.setDex((short)dex);
                        int _int = equip.getInt() - equipField.getInt();
                        if (_int < 0) {
                            _int = 0;
                        }

                        equip.setInt((short)_int);
                        int luk = equip.getLuk() - equipField.getLuk();
                        if (luk < 0) {
                            luk = 0;
                        }

                        equip.setLuk((short)luk);
                        int hp = equip.getHp() - equipField.getHp();
                        if (hp < 0) {
                            hp = 0;
                        }

                        equip.setHp((short)hp);
                        int mp = equip.getMp() - equipField.getMp();
                        if (mp < 0) {
                            mp = 0;
                        }

                        equip.setMp((short)mp);
                        int watk = equip.getWatk() - equipField.getWatk();
                        if (watk < 0) {
                            watk = 0;
                        }

                        equip.setWatk((short)watk);
                        int matk = equip.getMatk() - equipField.getMatk();
                        if (matk < 0) {
                            matk = 0;
                        }

                        equip.setMatk((short)matk);
                        int wdef = equip.getWdef() - equipField.getWdef();
                        if (wdef < 0) {
                            wdef = 0;
                        }

                        equip.setWdef((short)wdef);
                        int mdef = equip.getMdef() - equipField.getMdef();
                        if (mdef < 0) {
                            mdef = 0;
                        }

                        equip.setMdef((short)mdef);
                        int acc = equip.getAcc() - equipField.getAcc();
                        if (acc < 0) {
                            acc = 0;
                        }

                        equip.setAcc((short)acc);
                        int avoid = equip.getAvoid() - equipField.getAvoid();
                        if (avoid < 0) {
                            avoid = 0;
                        }

                        equip.setAvoid((short)avoid);
                        int speed = equip.getSpeed() - equipField.getSpeed();
                        if (speed < 0) {
                            speed = 0;
                        }

                        equip.setSpeed((short)speed);
                        int jump = equip.getJump() - equipField.getJump();
                        if (jump < 0) {
                            jump = 0;
                        }

                        equip.setJump((short)jump);
                        if (equipField.getTotalDamage() > 0) {
                            Potential.setPotential(equip, (short)11, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，总伤害-" + equipField.getTotalDamage() + "%");
                        }

                        if (equipField.getBossDamage() > 0) {
                            Potential.setPotential(equip, (short)12, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，BOSS伤害-" + equipField.getBossDamage() + "%");
                        }

                        if (equipField.getNormalDamage() > 0) {
                            Potential.setPotential(equip, (short)13, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，小怪伤害-" + equipField.getNormalDamage() + "%");
                        }

                        if (equipField.getStr_p() > 0) {
                            Potential.setPotential(equip, (short)14, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，力量-" + equipField.getStr_p() + "%");
                        }

                        if (equipField.getDex_p() > 0) {
                            Potential.setPotential(equip, (short)15, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，敏捷-" + equipField.getDex_p() + "%");
                        }

                        if (equipField.getInt_p() > 0) {
                            Potential.setPotential(equip, (short)16, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，智力-" + equipField.getInt_p() + "%");
                        }

                        if (equipField.getLuk_p() > 0) {
                            Potential.setPotential(equip, (short)17, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，运气-" + equipField.getLuk_p() + "%");
                        }

                        if (equipField.getHp_p() > 0) {
                            Potential.setPotential(equip, (short)18, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxHp-" + equipField.getHp_p() + "%");
                        }

                        if (equipField.getMp_p() > 0) {
                            Potential.setPotential(equip, (short)19, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxMp-" + equipField.getMp_p() + "%");
                        }

                        if (equipField.getWatk_p() > 0) {
                            Potential.setPotential(equip, (short)20, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，攻击力-" + equipField.getWatk_p() + "%");
                        }

                        if (equipField.getMatk_p() > 0) {
                            Potential.setPotential(equip, (short)21, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法力-" + equipField.getWatk_p() + "%");
                        }

                        if (equipField.getWdef_p() > 0) {
                            Potential.setPotential(equip, (short)22, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，物理防御力-" + equipField.getWdef_p() + "%");
                        }

                        if (equipField.getMdef_p() > 0) {
                            Potential.setPotential(equip, (short)23, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法防御力-" + equipField.getMdef_p() + "%");
                        }

                        if (equipField.getAcc_p() > 0) {
                            Potential.setPotential(equip, (short)24, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，命中率-" + equipField.getAcc_p() + "%");
                        }

                        if (equipField.getAvoid_p() > 0) {
                            Potential.setPotential(equip, (short)25, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，回避率-" + equipField.getAvoid_p() + "%");
                        }

                        if (equipField.getMustKill() > 0) {
                            Potential.setPotential(source, (short)26, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，普通怪物必杀概率-" + equipField.getMustKill() + "%");
                        }

                        if (equipField.getInvincible() > 0) {
                            Potential.setPotential(source, (short)27, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，无敌概率-" + equipField.getInvincible() + "%");
                        }

                        if (equipField.getStrong() > 0) {
                            Potential.setPotential(source, (short)28, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，坚韧-" + equipField.getStrong() + "%（受到的伤害减少）");
                        }

                        if (equipField.getSuckHp() > 0) {
                            Potential.setPotential(source, (short)29, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，吸血-" + equipField.getSuckHp() + "%（攻击时恢复HP上限10%的概率）");
                        }

                        if (equipField.getSuckMp() > 0) {
                            Potential.setPotential(source, (short)30, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，吸魔-" + equipField.getSuckMp() + "%（攻击时恢复MP上限10%的概率）");
                        }

                        if (equipField.getGrowableHp() > 0) {
                            Potential.setPotential(source, (short)31, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，茁壮生命-" + equipField.getGrowableHp() + "（升级时额外获得HP上限数量）");
                        }

                        if (equipField.getGrowableMp() > 0) {
                            Potential.setPotential(source, (short)32, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，茁壮魔力-" + equipField.getGrowableMp() + "（升级时额外获得MP上限数量）");
                        }

                        if (equipField.getMoreExp() > 0) {
                            Potential.setPotential(source, (short)33, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，慧根-" + equipField.getMoreExp() + "%（狩猎经验倍率增加）");
                        }

                        if (equipField.getMoreMeso() > 0) {
                            Potential.setPotential(source, (short)34, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，财运-" + equipField.getMoreMeso() + "%（狩猎金币倍率增加）");
                        }

                        if (equipField.getMoreDrop() > 0) {
                            Potential.setPotential(source, (short)35, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，财运-" + equipField.getMoreDrop() + "%（狩猎掉落倍率增加）");
                        }

                        if (equipField.getRevive() > 0) {
                            Potential.setPotential(source, (short)36, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，涅槃-" + equipField.getRevive() + "%（死亡时原地满血复活的概率）");
                        }

                        if (equipField.getSummonMob() > 0) {
                            Potential.setPotential(source, (short)37, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，怨念-" + equipField.getSummonMob() + "%（击杀普通怪物时招来怪物伙伴的概率）");
                        }

                        if (equipField.getConsumeRecover() > 0) {
                            Potential.setPotential(source, (short)38, 0, 0);
                            c.getPlayer().dropMessage(5, "该装备栏位取消装备，药灵-" + equipField.getConsumeRecover() + "%（恢复类药水的恢复值增加）");
                        }

                        c.getPlayer().reFreshItem(equip);
                    }
                }

                if ((Integer)LtMS.ConfigValuesMap.get("丢道具存档开关") > 0) {
                    c.getPlayer().道具存档();
                    c.getPlayer().setPower(0L);
                    c.getPlayer().getPower();
                }
                c.getPlayer().getInventory(type).removeSlot(src);
                c.sendPacket(MaplePacketCreator.dropInventoryItem((src < 0) ? MapleInventoryType.EQUIP : type, src));
                if (src < 0) {
                    c.getPlayer().equipChanged();
                }
                if (ii.isDropRestricted(source.getItemId()) || ii.isAccountShared(source.getItemId())) {
                    if (ItemFlag.KARMA_EQ.check((int)flag)) {
                        source.setFlag((byte)(flag - ItemFlag.KARMA_EQ.getValue()));
                        c.getPlayer().getMap().spawnItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                    }
                    else if (ItemFlag.KARMA_USE.check((int)flag)) {
                        source.setFlag((byte)(flag - ItemFlag.KARMA_USE.getValue()));
                        c.getPlayer().getMap().spawnItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                    }
                    else {
                        c.getPlayer().getMap().disappearingItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), source, dropPos);
                    }
                }
                else if (GameConstants.isPet(source.getItemId()) || ItemFlag.UNTRADEABLE.check((int)flag)) {
                    c.getPlayer().getMap().disappearingItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), source, dropPos);
                }
                else {
                    c.getPlayer().getMap().spawnItemDrop((MapleMapObject)c.getPlayer(), c.getPlayer(), source, dropPos, true, true);
                }
            }
        }
        catch (Exception ex) {
            FileoutputUtil.outError("logs/丟棄道具異常.txt", (Throwable)ex);
        }
        return true;
    }
    
    public static void removeAllByEquipOnlyId(final MapleClient c, final long inventoryitemid) {
        if (c.getPlayer() == null) {
            return;
        }
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final IItem copyEquipItems = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItemByInventoryItemId(inventoryitemid);
        if (copyEquipItems != null) {
            removeFromSlot(c, MapleInventoryType.EQUIP, copyEquipItems.getPosition(), copyEquipItems.getQuantity(), true, false);
            final String msgtext = "玩家" + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級" + (int)c.getPlayer().getLevel() + ") 地图: " + c.getPlayer().getMapId() + " 在玩家背包中發現复制装备[" + ii.getName(copyEquipItems.getItemId()) + "]已經將其刪除。";
            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密语] " + msgtext));
            FileoutputUtil.log("Hack/复制装备_已刪除.txt", msgtext + " 道具唯一ID: " + copyEquipItems.getEquipOnlyId());
        }
        final IItem copyEquipedItems = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItemByInventoryItemId(inventoryitemid);
        if (copyEquipedItems != null) {
            removeFromSlot(c, MapleInventoryType.EQUIPPED, copyEquipedItems.getPosition(), copyEquipedItems.getQuantity(), true, false);
            final String msgtext2 = "玩家" + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級" + (int)c.getPlayer().getLevel() + ") 地图: " + c.getPlayer().getMapId() + " 在玩家穿戴中發現复制装备[" + ii.getName(copyEquipedItems.getItemId()) + "]已經將其刪除。";
            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密语] " + msgtext2));
            FileoutputUtil.logToFile("Hack/复制装备_已刪除.txt", msgtext2 + " 道具唯一ID: " + copyEquipedItems.getEquipOnlyId());
        }
        for (final IItem copyStorageItem : c.getPlayer().getStorage().getItems()) {
            if (copyStorageItem != null && c.getPlayer().getStorage().removeItemByInventoryItemId(inventoryitemid)) {
                final String msgtext3 = "玩家" + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等級" + (int)c.getPlayer().getLevel() + ") 地图: " + c.getPlayer().getMapId() + " 在玩家穿戴中發現复制装备[" + ii.getName(copyEquipedItems.getItemId()) + "]已經將其刪除。";
                Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM密语] " + msgtext3));
                FileoutputUtil.logToFile("Hack/复制装备_已刪除.txt", msgtext3 + " 道具唯一ID: " + copyStorageItem.getEquipOnlyId() + "\r\n");
            }
        }
        final List<IItem> copyEquipList = c.getPlayer().getInventory(MapleInventoryType.EQUIP).listByEquipOnlyId((int)inventoryitemid);
        boolean locked = false;
        for (final IItem item : copyEquipList) {
            if (item != null) {
                if (!locked) {
                    short flag = item.getFlag();
                    flag |= (short)ItemFlag.LOCK.getValue();
                    flag |= (short)ItemFlag.UNTRADEABLE.getValue();
                    item.setFlag((byte) flag);
                    item.setOwner("复制装备");
                    c.getPlayer().forceUpdateItem(item);
                    c.getPlayer().dropMessage(-11, "在背包中发现复制装备[" + ii.getName(item.getItemId()) + "]已经将其锁定。");
                    final String msgtext = "玩家 " + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等级 " + c.getPlayer().getLevel() + ") 地图: " + c.getPlayer().getMapId() + " 在玩家背包中发现复制装备[" + ii.getName(item.getItemId()) + "]已经将其锁定。";
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM Message] " + msgtext));
                    FileoutputUtil.log("log\\复制装备.log", msgtext + " 道具唯一ID: " + item.getEquipOnlyId());
                    locked = true;
                }
                else {
                    removeFromSlot(c, MapleInventoryType.EQUIP, item.getPosition(), item.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, "在背包中发现复制装备[" + ii.getName(item.getItemId()) + "]已经将其删除。");
                }
            }
        }
        final List<IItem> copyEquipedList= c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).listByEquipOnlyId((int)inventoryitemid);
        for (final IItem item2 : copyEquipedList) {
            if (item2 != null) {
                if (!locked) {
                    short flag2 = item2.getFlag();
                    flag2 |= (short)ItemFlag.LOCK.getValue();
                    flag2 |= (short)ItemFlag.UNTRADEABLE.getValue();
                    item2.setFlag((byte) flag2);
                    item2.setOwner("复制装备");
                    c.getPlayer().forceUpdateItem(item2);
                    c.getPlayer().dropMessage(-11, "在穿戴中发现复制装备[" + ii.getName(item2.getItemId()) + "]已经将其锁定。");
                    final String msgtext2 = "玩家 " + c.getPlayer().getName() + " ID: " + c.getPlayer().getId() + " (等级 " + c.getPlayer().getLevel() + ") 地图: " + c.getPlayer().getMapId() + " 在玩家穿戴中发现复制装备[" + ii.getName(item2.getItemId()) + "]已经将其锁定。";
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM Message] " + msgtext2));
                    FileoutputUtil.log("log\\复制装备.log", msgtext2 + " 道具唯一ID: " + item2.getEquipOnlyId());
                    locked = true;
                }
                else {
                    removeFromSlot(c, MapleInventoryType.EQUIPPED, item2.getPosition(), item2.getQuantity(), true, false);
                    c.getPlayer().dropMessage(-11, "在穿戴中发现复制装备[" + ii.getName(item2.getItemId()) + "]已经将其删除。");
                    c.getPlayer().equipChanged();
                }
            }
        }

    }
}
