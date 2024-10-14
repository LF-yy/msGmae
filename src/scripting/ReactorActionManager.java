package scripting;

import handling.world.World;
import server.MapleCarnivalFactory.MCSkill;
import server.MapleCarnivalFactory;
import server.life.MapleMonster;
import server.life.MapleLifeFactory;
import client.inventory.IItem;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import server.maps.MapleMapObject;
import handling.channel.ChannelServer;
import server.Randomizer;
import server.MapleItemInformationProvider;
import server.maps.ReactorDropEntry;
import java.util.LinkedList;
import client.MapleClient;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;

public class ReactorActionManager extends AbstractPlayerInteraction
{

    private final MapleReactor reactor;

    public ReactorActionManager(MapleClient c, MapleReactor reactor) {
        super(c);
        this.reactor = reactor;
    }

    public void dropItems() {
        this.dropItems(false, 0, 0, 0, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso) {
        this.dropItems(meso, mesoChance, minMeso, maxMeso, 0);
    }

    public void dropItems(boolean meso, int mesoChance, int minMeso, int maxMeso, int minItems) {
        List<ReactorDropEntry> chances = ReactorScriptManager.getInstance().getDrops(this.reactor.getReactorId());
        List<ReactorDropEntry> items = new LinkedList();
        if (meso && Math.random() < 1.0 / (double)mesoChance) {
            items.add(new ReactorDropEntry(0, mesoChance, -1));
        }

        int numItems = 0;
        Iterator<ReactorDropEntry> iter = chances.iterator();

        while(true) {
            ReactorDropEntry d;
            do {
                do {
                    if (!iter.hasNext()) {
                        while(items.size() < minItems) {
                            items.add(new ReactorDropEntry(0, mesoChance, -1));
                            ++numItems;
                        }

                        Point dropPos = this.reactor.getPosition();
                        dropPos.x -= 12 * numItems;
                        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

                        for(Iterator var14 = items.iterator(); var14.hasNext(); dropPos.x += 25) {
                             d = (ReactorDropEntry)var14.next();
                            if (d.itemId == 0) {
                                int range = maxMeso - minMeso;
                                int mesoDrop = (int)((float)Randomizer.nextInt(range) + (float)minMeso * ChannelServer.getInstance(this.getClient().getChannel()).getMesoRate() * ChannelServer.getInstance(this.getClient().getChannel()).getMesoRateSpecial());
                                this.reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, this.reactor, this.getPlayer(), false, (byte)0);
                            } else {
                                Object drop;
                                if (GameConstants.getInventoryType(d.itemId) != MapleInventoryType.EQUIP) {
                                    drop = new Item(d.itemId, (short)0, (short) 1, (byte)0);
                                } else {
                                    drop = ii.randomizeStats((Equip)ii.getEquipById(d.itemId));
                                }

                                this.reactor.getMap().spawnItemDrop(this.reactor, this.getPlayer(), (IItem)drop, dropPos, false, false);
                            }
                        }

                        return;
                    }

                    d = (ReactorDropEntry)iter.next();
                } while(!(Math.random() * 100.0 <= (double)d.chance / 10000.0));
            } while(d.questid > 0 && this.getPlayer().getQuestStatus(d.questid) != 1);

            ++numItems;
            items.add(d);
        }
    }

    public void spawnNpc(int npcId) {
        this.spawnNpc(npcId, this.getPosition());
    }

    public Point getPosition() {
        Point pos = this.reactor.getPosition();
        pos.y -= 10;
        return pos;
    }

    public MapleReactor getReactor() {
        return this.reactor;
    }

    public void spawnZakum() {
        this.reactor.getMap().spawnZakum(this.getPosition().x, this.getPosition().y);
    }

    public void spawnFakeMonster(int id) {
        this.spawnFakeMonster(id, 1, this.getPosition());
    }

    public void spawnFakeMonster(int id, int x, int y) {
        this.spawnFakeMonster(id, 1, new Point(x, y));
    }

    public void spawnFakeMonster(int id, int qty) {
        this.spawnFakeMonster(id, qty, this.getPosition());
    }

    public void spawnFakeMonster(int id, int qty, int x, int y) {
        this.spawnFakeMonster(id, qty, new Point(x, y));
    }

    private void spawnFakeMonster(int id, int qty, Point pos) {
        for(int i = 0; i < qty; ++i) {
            this.reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }

    }

    public MapleMonster getMonster(int id) {
        return MapleLifeFactory.getMonster(id);
    }

    public void killAll() {
        this.reactor.getMap().killAllMonsters(true);
    }

    public void killMonster(int monsId) {
        this.reactor.getMap().killMonster(monsId);
    }

    public void spawnMonster(int id) {
        this.spawnMonster(id, 1, this.getPosition());
    }

    public void spawnMonster(int id, int qty) {
        this.spawnMonster(id, qty, this.getPosition());
    }

    public void dispelAllMonsters(int num) {
        MapleCarnivalFactory.MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
        if (skil != null) {
            Iterator var3 = this.getMap().getAllMonstersThreadsafe().iterator();

            while(var3.hasNext()) {
                MapleMonster mons = (MapleMonster)var3.next();
                mons.dispelSkill(skil.getMobSkill());
            }
        }

    }

    public void 全服公告(String text) {
        World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, text));
    }
//    private final MapleReactor reactor;
//
//    public ReactorActionManager(final MapleClient c, final MapleReactor reactor) {
//        super(c);
//        this.reactor = reactor;
//    }
//
//    public void dropItems() {
//        this.dropItems(false, 0, 0, 0, 0);
//    }
//
//    public void dropItems(final boolean meso, final int mesoChance, final int minMeso, final int maxMeso) {
//        this.dropItems(meso, mesoChance, minMeso, maxMeso, 0);
//    }
//
//    public void dropItems(final boolean meso, final int mesoChance, final int minMeso, final int maxMeso, final int minItems) {
//        final List<ReactorDropEntry> chances = ReactorScriptManager.getInstance().getDrops(this.reactor.getReactorId());
//        final List<ReactorDropEntry> items = new LinkedList<ReactorDropEntry>();
//        if (meso && Math.random() < 1.0 / (double)mesoChance) {
//            items.add(new ReactorDropEntry(0, mesoChance, -1));
//        }
//        int numItems = 0;
//        for (final ReactorDropEntry d : chances) {
//            if (Math.random() < 1.0 / (double)d.chance && (d.questid <= 0 || this.getPlayer().getQuestStatus(d.questid) == 1)) {
//                ++numItems;
//                items.add(d);
//            }
//        }
//        while (items.size() < minItems) {
//            items.add(new ReactorDropEntry(0, mesoChance, -1));
//            ++numItems;
//        }
//        final Point position;
//        final Point dropPos = position = this.reactor.getPosition();
//        position.x -= 12 * numItems;
//        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
//        for (final ReactorDropEntry d2 : items) {
//            if (d2.itemId == 0) {
//                final int range = maxMeso - minMeso;
//                final int mesoDrop = Randomizer.nextInt(range) + minMeso * ChannelServer.getInstance(this.getClient().getChannel()).getMesoRate();
//                this.reactor.getMap().spawnMesoDrop(mesoDrop, dropPos, (MapleMapObject)this.reactor, this.getPlayer(), false, (byte)0);
//            }
//            else {
//                IItem drop;
//                if (GameConstants.getInventoryType(d2.itemId) != MapleInventoryType.EQUIP) {
//                    drop = new Item(d2.itemId, (short)0, (short)1, (byte)0);
//                }
//                else {
//                    drop = ii.randomizeStats((Equip)ii.getEquipById(d2.itemId));
//                }
//                this.reactor.getMap().spawnItemDrop((MapleMapObject)this.reactor, this.getPlayer(), drop, dropPos, false, false);
//            }
//            final Point point = dropPos;
//            point.x += 25;
//        }
//    }
//
//    @Override
//    public void spawnNpc(final int npcId) {
//        this.spawnNpc(npcId, this.getPosition());
//    }
//
//    public Point getPosition() {
//        final Point position;
//        final Point pos = position = this.reactor.getPosition();
//        position.y -= 10;
//        return pos;
//    }
//
//    public MapleReactor getReactor() {
//        return this.reactor;
//    }
//
//    public void spawnZakum() {
//        this.reactor.getMap().spawnZakum(this.getPosition().x, this.getPosition().y);
//    }
//
//    public void spawnFakeMonster(final int id) {
//        this.spawnFakeMonster(id, 1, this.getPosition());
//    }
//
//    public void spawnFakeMonster(final int id, final int x, final int y) {
//        this.spawnFakeMonster(id, 1, new Point(x, y));
//    }
//
//    public void spawnFakeMonster(final int id, final int qty) {
//        this.spawnFakeMonster(id, qty, this.getPosition());
//    }
//
//    public void spawnFakeMonster(final int id, final int qty, final int x, final int y) {
//        this.spawnFakeMonster(id, qty, new Point(x, y));
//    }
//
//    private void spawnFakeMonster(final int id, final int qty, final Point pos) {
//        for (int i = 0; i < qty; ++i) {
//            this.reactor.getMap().spawnFakeMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
//        }
//    }
//
//    public MapleMonster getMonster(final int id) {
//        return MapleLifeFactory.getMonster(id);
//    }
//
//    public void killAll() {
//        this.reactor.getMap().killAllMonsters(true);
//    }
//
//    public void killMonster(final int monsId) {
//        this.reactor.getMap().killMonster(monsId);
//    }
//
//    @Override
//    public void spawnMonster(final int id) {
//        this.spawnMonster(id, 1, this.getPosition());
//    }
//
//    @Override
//    public void spawnMonster(final int id, final int qty) {
//        this.spawnMonster(id, qty, this.getPosition());
//    }
//
//    public void dispelAllMonsters(final int num) {
//        final MCSkill skil = MapleCarnivalFactory.getInstance().getGuardian(num);
//        if (skil != null) {
//            for (final MapleMonster mons : this.getMap().getAllMonstersThreadsafe()) {
//                mons.dispelSkill(skil.getMobSkill());
//            }
//        }
//    }
}
