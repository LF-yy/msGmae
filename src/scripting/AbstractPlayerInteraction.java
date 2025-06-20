package scripting;

import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

import bean.ItemInfo;
import bean.LtCopyMap;
import bean.LtCopyMapMonster;
import bean.LtMonsterPosition;
import client.inventory.*;
import database.DBConPool;
import database.DatabaseConnection;

import handling.SendPacketOpcode;
import handling.world.World;
import server.*;
import server.custom.bankitem2.BankItem2;
import server.custom.bankitem1.BankItem1;
import server.custom.bankitem.BankItem;
import server.custom.bankitem2.BankItemManager2;
import server.custom.bankitem1.BankItemManager1;
import server.custom.bankitem.BankItemManager;
import server.custom.bossrank10.BossRankManager10;
import server.custom.bossrank10.BossRankInfo10;
import server.custom.bossrank9.BossRankManager9;
import server.custom.bossrank9.BossRankInfo9;
import server.custom.bossrank8.BossRankManager8;
import server.custom.bossrank8.BossRankInfo8;
import server.custom.bossrank7.BossRankManager7;
import server.custom.bossrank7.BossRankInfo7;
import server.custom.bossrank6.BossRankManager6;
import server.custom.bossrank6.BossRankInfo6;
import server.custom.bossrank5.BossRankManager5;
import server.custom.bossrank5.BossRankInfo5;
import server.custom.bossrank4.BossRankManager4;
import server.custom.bossrank4.BossRankInfo4;
import server.custom.bossrank3.BossRankManager3;
import server.custom.bossrank3.BossRankInfo3;
import server.custom.bossrank2.BossRankManager2;
import server.custom.bossrank2.BossRankInfo2;
import server.custom.bossrank1.BossRankManager1;
import server.custom.bossrank1.BossRankInfo1;
import server.custom.bossrank.BossRankManager;
import server.custom.bossrank.BossRankInfo;

import java.util.*;

import constants.ServerConfig;
import client.MapleStat;
import server.events.MapleGuildMatch;
import server.events.MapleGuildOutsideBoss;
import snail.DamageManage;
import snail.GuiPlayerEntity;
import tools.FileoutputUtil;
import client.messages.CommandProcessor;
import constants.ServerConstants.CommandType;
import handling.channel.handler.InterServerHandler;
import server.maps.MapleMapFactory;
import server.maps.SavedLocationType;
import server.events.MapleEventType;
import server.events.MapleEvent;
import server.maps.Event_DojoAgent;
import client.ISkill;
import server.life.MapleMonster;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.GuiPacketCreator;
import tools.packet.PetPacket;
import tools.packet.UIPacket;
import client.SkillFactory;
import handling.world.MapleParty;
import handling.world.guild.MapleGuild;
import handling.world.World.Guild;
import handling.world.World.Broadcast;
import constants.ItemConstants.類型;
import server.maps.MapleMapObject;
import server.maps.MapleReactor;
import server.quest.MapleQuest;
import client.MapleQuestStatus;
import constants.GameConstants;
import server.life.MapleLifeFactory;
import java.awt.geom.Point2D;
import tools.MaplePacketCreator;
import java.awt.Point;

import server.maps.MapleMap;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import client.MapleCharacter;
import client.MapleClient;
import util.GetRedisDataUtil;
import util.ListUtil;

public abstract class AbstractPlayerInteraction
{
    private static final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

    protected MapleClient c;

    public String getPortalName(){
       return c.getPlayer().getMap().getPortalName();
    }
    public AbstractPlayerInteraction(final MapleClient c) {
        this.c = c;
    }

    public final MapleClient getClient() {
        return this.c;
    }

    public final MapleClient getC() {
        return this.c;
    }

    public MapleCharacter getChar() {
        return this.getClient().getPlayer();
    }

    public int getOneTimeLog(final String bossid) {
        return this.getPlayer().getOneTimeLog(bossid);
    }

    public void setOneTimeLog(final String bossid) {
        this.getPlayer().setOneTimeLog(bossid);
    }
    public void setOneTimeLog(String bossid, int count) {
        this.getPlayer().setOneTimeLog(bossid, count);
    }

    public void deleteOneTimeLog(final String bossid) {
        this.getPlayer().deleteOneTimeLog(bossid);
    }

    public int getAcLog(final String bossid) {
        return this.getPlayer().getAcLog(bossid);
    }

    public int getAcLogS(final String bossid) {
        return this.getPlayer().getAcLogS(bossid);
    }

    public void setAcLog(final String bossid) {
        this.getPlayer().setAcLog(bossid);
    }

    public int 获得破功() {
        return this.getPlayer().获得破功();
    }

    public void 添加破功(final int pg) {
        this.c.getPlayer().添加破功(pg);
    }
    public void setAccountidBossLog(String bossid) {
        this.getPlayer().setAccountidBossLog(bossid);
    }
    public void setAccountidLog(String bossid) {
        this.getPlayer().setAccountidLog(bossid);
    }

    public void setAccountidBossLog(String bossid, int number) {
        this.getPlayer().setAccountidBossLog(bossid, number);
    }

    public void setAccountidLog(String bossid, int number) {
        this.getPlayer().setAccountidLog(bossid, number);
    }
    public void givePartyAccountidBossLog(String bossid) {
        this.givePartyAccountidBossLog(bossid, 1);
    }
    public void givePartyAccountidLog(String bossid) {
        this.givePartyAccountidLog(bossid, 1);
    }

    public IItem getItemById(int itemId) {
        IItem item = null;
        MapleInventoryType type = GameConstants.getInventoryType(itemId);
        if (type == MapleInventoryType.UNDEFINED) {
            return null;
        } else {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            if (!type.equals(MapleInventoryType.EQUIP)) {
                item = new Item(itemId, (short)0, (short) 1, (byte)0);
            } else {
                switch (itemId) {
                    case 1112405:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    case 1112413:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    case 1112414:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId), itemId);
                        break;
                    default:
                        item = ii.randomizeStats((Equip)ii.getEquipById(itemId));
                }
            }

            return (IItem)item;
        }
    }
    public void givePartyAccountidBossLog(String bossid, int number) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.getPlayer().setAccountidBossLog(bossid, number);
            return;
        }
        for (MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.setAccountidBossLog(bossid, number);
            }
        }
    }
    public void givePartyAccountidLog(String bossid, int number) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.getPlayer().setAccountidLog(bossid, number);
            return;
        }
        for (MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.setAccountidLog(bossid, number);
            }
        }
    }
    public int getBossLog(final String bossid) {
        return this.c.getPlayer().getBossLog(bossid);
    }
    public int getBossLog1(final String bossid) {
        return this.c.getPlayer().getBossLog1(bossid);
    }

    public int getBossLog(final String bossid, final int type) {
        return this.c.getPlayer().getBossLog(bossid, type);
    }
        public int getBossLog1(final String bossid, final int type) {
        return this.c.getPlayer().getBossLog1(bossid, type);
    }

    public void setBossLog(final String bossid) {
        this.c.getPlayer().setBossLog(bossid);
    }

    public void setBossLog(final String bossid, final int type) {
        this.c.getPlayer().setBossLog(bossid, type);
    }

    public void setBossLog(final String bossid, final int type, final int count) {
        this.c.getPlayer().setBossLog(bossid, type, count);
    }
    public void setBossLog1(final String bossid) {
        this.c.getPlayer().setBossLog1(bossid);
    }
    public void setBossLog1(final String bossid, final int type, final int count) {
        this.c.getPlayer().setBossLog1(bossid, type, count);
    }

    public void resetBossLog(final String bossid) {
        this.c.getPlayer().resetBossLog(bossid);
    }

    public void resetBossLog(final String bossid, final int type) {
        this.c.getPlayer().resetBossLog(bossid, type);
    }

    public void setPartyBossLog(final String bossid) {
        this.setPartyBossLog(bossid, 0);
    }

    public void setPartyBossLog(final String bossid, final int type) {
        this.setPartyBossLog(bossid, type, 1);
    }

    public void setPartyBossLog(final String bossid, final int type, final int count) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.c.getPlayer().setBossLog(bossid, type, count);
            return;
        }
        final int cMap = this.getPlayer().getMapId();
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getPlayer().getMap().getCharacterById(chr.getId());
            if (curChar != null && curChar.getMapId() == cMap) {
                curChar.setBossLog(bossid, type, count);
            }
        }
    }

    public int getBossLogAcc(final String bossid) {
        return this.c.getPlayer().getBossLogAcc(bossid);
    }

    public void setBossLogAcc(final String bossid) {
        this.c.getPlayer().setBossLogAcc(bossid);
    }

    public void setBossLogAcc(final String bossid, final int bosscount) {
        this.c.getPlayer().setBossLogAcc(bossid, bosscount);
    }

    public final ChannelServer getChannelServer() {
        return this.getClient().getChannelServer();
    }

    public final MapleCharacter getPlayer() {
        return this.getClient().getPlayer();
    }

    public final EventManager getEventManager(final String event) {
        return this.getClient().getChannelServer().getEventSM().getEventManager(event);
    }

    public final EventInstanceManager getEventInstance() {
        return this.getClient().getPlayer().getEventInstance();
    }

    public void warp(final int map) {
        final MapleMap mapz = this.getWarpMap(map);
        try {
            this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
        }
        catch (Exception e) {
            this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public void warp_Instanced(final int map) {
        final MapleMap mapz = this.getMap_Instanced(map);
        try {
            this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(Randomizer.nextInt(mapz.getPortals().size())));
        }
        catch (Exception e) {
            this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(0));
        }
    }

    public void instantMapWarp(final int map, final int portal) {
        final MapleMap mapz = this.getWarpMap(map);
        if (portal != 0 && map == this.c.getPlayer().getMapId()) {
            final Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            this.c.getSession().writeAndFlush((Object)MaplePacketCreator.instantMapWarp((byte)portal));
            this.c.getPlayer().checkFollow();
            this.c.getPlayer().getMap().movePlayer(this.c.getPlayer(), portalPos);
        }
        else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public void warp(final int map, final int portal) {
        final MapleMap mapz = this.getWarpMap(map);
        if (portal != 0 && map == this.getClient().getPlayer().getMapId()) {
            final Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq((Point2D)this.getPlayer().getPosition()) < 90000.0) {
                this.getClient().sendPacket(MaplePacketCreator.instantMapWarp((byte)portal));
                this.getClient().getPlayer().checkFollow();
                this.getClient().getPlayer().getMap().movePlayer(this.c.getPlayer(), portalPos);
            }
            else {
                this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(portal));
            }
        }
        else {
            this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public void warpS(final int map, final int portal) {
        final MapleMap mapz = this.getWarpMap(map);
        this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(portal));
    }

    public void warp(final int map, String portal) {
        final MapleMap mapz = this.getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }
        if (map == this.getClient().getPlayer().getMapId()) {
            final Point portalPos = new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            if (portalPos.distanceSq((Point2D)this.getPlayer().getPosition()) < 90000.0) {
                this.getClient().getPlayer().checkFollow();
                this.getClient().sendPacket(MaplePacketCreator.instantMapWarp((byte)this.getClient().getPlayer().getMap().getPortal(portal).getId()));
                this.getClient().getPlayer().getMap().movePlayer(this.c.getPlayer(), new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition()));
            }
            else {
                this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(portal));
            }
        }
        else {
            this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }
    }

    public void warpS(final int map, String portal) {
        final MapleMap mapz = this.getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }
        this.getClient().getPlayer().changeMap(mapz, mapz.getPortal(portal));
    }

    public void warpMap(final int mapid, final String portal) {
        final MapleMap map = this.getMap(mapid);
        for (MapleCharacter chr : this.getClient().getPlayer().getMap().getCharactersThreadsafe()) {
            chr.changeMap(map, map.getPortal(portal));
        }
    }

    public void warpMap(final int mapid, final int portal) {
        final MapleMap map = this.getMap(mapid);
        for (MapleCharacter chr : this.getClient().getPlayer().getMap().getCharactersThreadsafe()) {
            chr.changeMap(map, map.getPortal(portal));
        }
    }

    public void playPortalSE() {
        this.getClient().sendPacket(MaplePacketCreator.showOwnBuffEffect(0, 8));
    }

    private final MapleMap getWarpMap(final int map) {
        return ChannelServer.getInstance(this.c.getChannel()).getMapFactory().getMap(map);
    }

    public final MapleMap getMap() {
        return this.getClient().getPlayer().getMap();
    }

    public final MapleMap getMap(final int map) {
        return this.getWarpMap(map);
    }

    public final MapleMap getMap_Instanced(final int map) {
        return (this.getClient().getPlayer().getEventInstance() == null) ? this.getMap(map) : this.getClient().getPlayer().getEventInstance().getMapInstance(map);
    }

    public void spawnMonster(final int id, final int qty) {
        this.spawnMob(id, qty, new Point(this.c.getPlayer().getPosition()));
    }

    public void spawnMobOnMap(final int id, final int qty, final int x, final int y, final int map) {
        for (int i = 0; i < qty; ++i) {
            this.getMap(map).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y));
        }
    }
    public void spawnMobOnMap(int id, int qty, int x, int y, int map, long hp) {
     for (int i = 0; i < qty; i++) {
       this.getMap(map).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), new Point(x, y), hp);
     }
   }

    public void spawnMob(final int id, final int qty, final int x, final int y) {
        this.spawnMob(id, qty, new Point(x, y));
    }

    public void spawnMob(final int id, final int x, final int y) {
        this.spawnMob(id, 1, new Point(x, y));
    }

    private void spawnMob(final int id, final int qty, final Point pos) {
        for (int i = 0; i < qty; ++i) {
            this.getClient().getPlayer().getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public void killMob(final int ids) {
        this.getClient().getPlayer().getMap().killMonster(ids);
    }

    public void killAllMob() {
        this.getClient().getPlayer().getMap().killAllMonsters(true);
    }

    public void addHP(final int delta) {
        this.getClient().getPlayer().addHP(delta);
    }

    public final int getPlayerStat(final String type) {
        if (type.equals((Object)"LVL")) {
            return this.getClient().getPlayer().getLevel();
        }
        if (type.equals((Object)"STR")) {
            return this.getClient().getPlayer().getStat().getStr();
        }
        if (type.equals((Object)"DEX")) {
            return this.getClient().getPlayer().getStat().getDex();
        }
        if (type.equals((Object)"INT")) {
            return this.getClient().getPlayer().getStat().getInt();
        }
        if (type.equals((Object)"LUK")) {
            return this.getClient().getPlayer().getStat().getLuk();
        }
        if (type.equals((Object)"HP")) {
            return this.getClient().getPlayer().getStat().getHp();
        }
        if (type.equals((Object)"MP")) {
            return this.getClient().getPlayer().getStat().getMp();
        }
        if (type.equals((Object)"MAXHP")) {
            return this.getClient().getPlayer().getStat().getMaxHp();
        }
        if (type.equals((Object)"MAXMP")) {
            return this.getClient().getPlayer().getStat().getMaxMp();
        }
        if (type.equals((Object)"RAP")) {
            return this.getClient().getPlayer().getRemainingAp();
        }
        if (type.equals((Object)"RSP")) {
            return this.getClient().getPlayer().getRemainingSp();
        }
        if (type.equals((Object)"GID")) {
            return this.getClient().getPlayer().getGuildId();
        }
        if (type.equals((Object)"GRANK")) {
            return this.getClient().getPlayer().getGuildRank();
        }
        if (type.equals((Object)"ARANK")) {
            return this.getClient().getPlayer().getAllianceRank();
        }
        if (type.equals((Object)"GM")) {
            return this.getClient().getPlayer().isGM() ? 1 : 0;
        }
        if (type.equals((Object)"ADMIN")) {
            return this.getClient().getPlayer().hasGmLevel(5) ? 1 : 0;
        }
        if (type.equals((Object)"GENDER")) {
            return this.getClient().getPlayer().getGender();
        }
        if (type.equals((Object)"FACE")) {
            return this.getClient().getPlayer().getFace();
        }
        if (type.equals((Object)"HAIR")) {
            return this.getClient().getPlayer().getHair();
        }
        return -1;
    }

    public final String getName() {
        return this.getClient().getPlayer().getName();
    }

    public final boolean haveItemTime(final int itemid) {
        if (this.haveItem(itemid)) {
            final MapleInventoryType type = GameConstants.getInventoryType(itemid);
            for (final IItem item : this.getChar().getInventory(type)) {
                if (item.getItemId() == itemid) {
                    return item.getExpiration() == -1L;
                }
            }
            return false;
        }
        return false;
    }

    public final boolean haveItemTimeNo(final int itemid) {
        if (this.haveItem(itemid)) {
            final MapleInventoryType type = GameConstants.getInventoryType(itemid);
            for (final IItem item : this.getChar().getInventory(type)) {
                if (item.getItemId() == itemid) {
                    return item.getExpiration() > 0L;
                }
            }
            return false;
        }
        return false;
    }

    public final boolean haveItem(final int itemid) {
        return this.haveItem(itemid, 1);
    }

    public final boolean haveItem(final int itemid, final int quantity) {
        return this.haveItem(itemid, quantity, false, true);
    }

    public final boolean haveItem(final int itemid, final int quantity, final boolean checkEquipped, final boolean greaterOrEquals) {
        return this.getClient().getPlayer().haveItem(itemid, quantity, checkEquipped, greaterOrEquals);
    }

    public final boolean canHold() {
        for (int i = 1; i <= 5; ++i) {
            if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).getNextFreeSlot() <= -1) {
                return false;
            }
        }
        return true;
    }

    public final boolean canHoldByType(final byte bytype, final int num) {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType(bytype)).getSlotLimit() - (this.c.getPlayer().getInventory(MapleInventoryType.getByType(bytype)).getNumSlotLimit() + 1) > num;
    }

    public final boolean canHoldByTypea(final byte bytype, final int num) {
        return this.c.getPlayer().getInventory(MapleInventoryType.getByType(bytype)).getSlotLimit() - (this.c.getPlayer().getInventory(MapleInventoryType.getByType(bytype)).getNextFreeSlot() - 1) > num;
    }

    public final boolean canHold(final int itemid) {
        return this.getClient().getPlayer().getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }

    public final boolean canHold(final int itemid, final int quantity) {
        return MapleInventoryManipulator.checkSpace(this.c, itemid, quantity, "");
    }

    public final MapleQuestStatus getQuestRecord(final int id) {
        return this.getClient().getPlayer().getQuestNAdd(MapleQuest.getInstance(id));
    }

    public final byte getQuestStatus(final int id) {
        return this.getClient().getPlayer().getQuestStatus(id);
    }

    public final boolean isQuestActive(final int id) {
        return this.getQuestStatus(id) == 1;
    }

    public final boolean isQuestFinished(final int id) {
        return this.getQuestStatus(id) == 2;
    }

    public void showQuestMsg(final String msg) {
        this.getClient().sendPacket(MaplePacketCreator.showQuestMsg(msg));
    }

    public void forceStartQuest(final int id, final String data) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, data);
    }

    public void forceStartQuest(final int id, final int data, final boolean filler) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, filler ? String.valueOf(data) : null);
    }

    public void forceStartQuest(final int id) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, null);
    }

    public void forceCompleteQuest(final int id) {
        MapleQuest.getInstance(id).forceComplete(this.getPlayer(), 0);
    }

    public void spawnNpc(final int npcId) {
        this.getClient().getPlayer().getMap().spawnNpc(npcId, this.getClient().getPlayer().getPosition());
    }

    public void spawnNpc(final int npcId, final int x, final int y) {
        this.getClient().getPlayer().getMap().spawnNpc(npcId, new Point(x, y));
    }

    public void spawnNpc(final int npcId, final Point pos) {
        this.getClient().getPlayer().getMap().spawnNpc(npcId, pos);
    }

    public void removeNpc(final int mapid, final int npcId) {
        this.getClient().getChannelServer().getMapFactory().getMap(mapid).removeNpc(npcId);
    }

    public void forceStartReactor(final int mapid, final int id) {
        final MapleMap map = this.getClient().getChannelServer().getMapFactory().getMap(mapid);
        for (final MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            final MapleReactor react = (MapleReactor)remo;
            if (react.getReactorId() == id) {
                react.forceStartReactor(this.c);
                break;
            }
        }
    }

    public void destroyReactor(final int mapid, final int id) {
        final MapleMap map = this.getClient().getChannelServer().getMapFactory().getMap(mapid);
        for (final MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            final MapleReactor react = (MapleReactor)remo;
            if (react.getReactorId() == id) {
                react.hitReactor(this.c);
                break;
            }
        }
    }

    public void hitReactor(final int mapid, final int id) {
        final MapleMap map = this.getClient().getChannelServer().getMapFactory().getMap(mapid);
        for (final MapleMapObject remo : map.getAllReactorsThreadsafe()) {
            final MapleReactor react = (MapleReactor)remo;
            if (react.getReactorId() == id) {
                react.hitReactor(this.c);
                break;
            }
        }
    }

    public final int getJob() {
        return this.getClient().getPlayer().getJob();
    }

    public void gainPotion(final int type, final int amount) {
        this.getClient().getPlayer().modifyCSPoints(type, amount, true);
    }

    public final int getPotion(final int type) {
        return this.getClient().getPlayer().getCSPoints(type);
    }

    public void gainNX(final int amount) {
        this.gainPotion(1, amount);
    }
    public void gainNXZ(final int amount) {
        if (amount <0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷点劵而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷点劵而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷点劵而被管理员永久停封。"));
            this.getPlayer().ban("刷点劵", true, true, false);
        }else {
            this.gainPotion(1, amount);
        }
    }
    public void gainNXF(final int amount) {
        if (amount > 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷点劵而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷点劵而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷点劵而被管理员永久停封。"));
            this.getPlayer().ban("刷点劵", true, true, false);
        }else {
            this.gainPotion(1, amount);
        }
    }

    public final int getNX() {
        return this.getPotion(1);
    }

    public void gainMaplePoint(final int amount) {
        this.gainPotion(2, amount);
    }

    public final int getMaplePoint() {
        return this.getPotion(2);
    }

    public void gainItemPeriod(final int id, final short quantity, final int period) {
        this.gainItem(id, quantity, false, (long)period, -1, "");
    }

    public void gainItemPeriod(final int id, final short quantity, final long period, final String owner) {
        this.gainItem(id, quantity, false, period, -1, owner);
    }

    public void gainItem( int id,  int quantity) {
        if (quantity>=30000){
            for (long i = 0; i < 999; i++) {
                if (quantity >=30000){
                    this.gainItem(id, (short) 30000, false, 0L, -1, "");
                    quantity=quantity-30000;
                }else {
                    this.gainItem(id, (short) quantity, false, 0L, -1, "");
                    return;
                }
                if (quantity <=0){
                    return;
                }
            }

        }else if (quantity<= -30000){
            for (long i = 0; i < 999; i++) {
                if (quantity <=-30000){
                    this.gainItem(id, (short) -30000, false, 0L, -1, "");
                    quantity=quantity+30000;
                }else {
                    this.gainItem(id, (short) quantity, false, 0L, -1, "");
                    return;
                }
                if (quantity >=0){
                    return;
                }
            }

        }else{
            this.gainItem(id, (short) quantity, false, 0L, -1, "");
        }
    }
    public void gainItemF( int id,  int quantity) {
        if (quantity >0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷物品而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷物品而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷物品而被管理员永久停封。"));
           this.getPlayer().ban("刷物品", true, true, false);
        }else {
            if (quantity<= -30000){
                for (long i = 0; i < 999; i++) {
                    if (quantity >=0){
                        return;
                    }
                    if (quantity <=-30000){
                        this.gainItem(id, (short) -30000, false, 0L, -1, "");
                        quantity=quantity+30000;
                    }else {
                        this.gainItem(id, (short) quantity, false, 0L, -1, "");
                        return;
                    }
                }
            }else{
                this.gainItem(id, (short) quantity, false, 0L, -1, "");
            }
        }
    }
    public void gainItemZ( int id,  int quantity) {
        if (quantity<=0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷物品而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷物品而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷物品而被管理员永久停封。"));
            this.getPlayer().ban("刷物品", true, true, false);
        }else {
            if (quantity >= 30000) {
                for (long i = 0; i < 999; i++) {
                    if (quantity <= 0) {
                        return;
                    }
                    if (quantity >= 30000) {
                        this.gainItem(id, (short) 30000, false, 0L, -1, "");
                        quantity = quantity - 30000;
                    } else {
                        this.gainItem(id, (short) quantity, false, 0L, -1, "");
                        return;
                    }
                }
            }else{
                this.gainItem(id, (short) quantity, false, 0L, -1, "");
            }
        }
    }

    public void gainItem(final int id, final short quantity, final boolean randomStats) {
        this.gainItem(id, quantity, randomStats, 0L, -1, "");
    }

    public void gainItem(final int id, final short quantity, final boolean randomStats, final int slots) {
        this.gainItem(id, quantity, randomStats, 0L, slots, "");
    }

    public void gainItem(final int id, final short quantity, final long period) {
        this.gainItem(id, quantity, false, period, -1, "");
    }

    public void gainItemTime(final int id, final short quantity, final long period) {
        if (MapleItemInformationProvider.getInstance().isCash(id)) {
            this.gainItem(id, quantity, false, period, -1, "");
        }
        else {
            this.gainItem(id, quantity, false, 0L, -1, "");
        }
    }

    public void gainItem(final int id, final short quantity, final boolean randomStats, final long period, final int slots) {
        this.gainItem(id, quantity, randomStats, period, slots, "");
    }

    public void gainItem(final int id, final short quantity, final boolean randomStats, final long period, final int slots, final String owner) {
        this.gainItem(id, quantity, randomStats, period, slots, owner, this.c);
    }

    public void gainItem(final int id, final short quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient cg) {
        if (quantity >= 0) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(id);
            if (!MapleInventoryManipulator.checkSpace(cg, id, (int)quantity, "")) {
                return;
            }
            if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                final Equip item = (Equip)(Equip)(randomStats ? ii.randomizeStats((Equip)ii.getEquipById(id)) : ii.getEquipById(id));
                if (period > 0L) {
                    item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                if (slots > 0) {
                    item.setUpgradeSlots((byte)(item.getUpgradeSlots() + slots));
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                final String name = ii.getName(id);
                if (id / 10000 == 114 && name != null && name.length() > 0) {
                    final String msg = "你已获得称号 <" + name + ">";
                    cg.getPlayer().dropMessage(-1, msg);
                    cg.getPlayer().dropMessage(5, msg);
                }
                //重构装备属性
                    Map<Integer, List<ItemInfo>> itemInfo = GetRedisDataUtil.getItemInfo();
                    if(Objects.nonNull(itemInfo) && ListUtil.isNotEmpty(itemInfo.get(item.getItemId())) ){
                        ItemInfo itemInfos = itemInfo.get(item.getItemId()).get(0);
                        item.setStr(itemInfos.getStr());
                        item.setDex(itemInfos.getDex());
                        item.setLuk(itemInfos.getLuk());
                        item.setInt(itemInfos.getIntValue());
                        item.setHp(itemInfos.getHp());
                        item.setMp(itemInfos.getMp());
                        item.setMatk(itemInfos.getMatk());
                        item.setWatk(itemInfos.getWatk());
                        item.setWdef(itemInfos.getWdef());
                        item.setMdef(itemInfos.getMdef());
                    }

                MapleInventoryManipulator.addbyItem(cg, item.copy());
            }
            else {
                MaplePet pet;
                if (類型.寵物(id)) {
                    pet = MaplePet.createPet(id, MapleInventoryIdentifier.getInstance());
                }
                else {
                    pet = null;
                }
                MapleInventoryManipulator.addById(cg, id, quantity, (owner == null) ? "" : owner, pet, period);
            }
        }
        else {
            MapleInventoryManipulator.removeById(cg, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        cg.sendPacket(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }

    public void gainItem(final int id, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final int time) {
        this.gainItemS(id, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, this.c, time);
    }

    public void gainItem(final int id, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final int time,final String ow) {
        this.gainItemS(id, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, this.c, time,ow);
    }

    public void gainItem(final int id, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd) {
        this.gainItemS(id, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, this.c, 0);
    }

    public void gainItemS(final int id, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final MapleClient cg, final int time) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleInventoryType type = GameConstants.getInventoryType(id);
        if (!MapleInventoryManipulator.checkSpace(cg, id, 1, "")) {
            return;
        }
        if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
            final Equip item = (Equip)ii.getEquipById(id);
            final String name = ii.getName(id);
            if (id / 10000 == 114 && name != null && name.length() > 0) {
                final String msg = "你已获得称号 <" + name + ">";
                cg.getPlayer().dropMessage(5, msg);
                cg.getPlayer().dropMessage(5, msg);
            }
            if (time > 0) {
                item.setExpiration(System.currentTimeMillis() + (long)(time * 60 * 60 * 1000));
            }
            if (str > 0) {
                item.setStr((short)str);
            }
            if (dex > 0) {
                item.setDex((short)dex);
            }
            if (luk > 0) {
                item.setLuk((short)luk);
            }
            if (Int > 0) {
                item.setInt((short)Int);
            }
            if (hp > 0) {
                item.setHp((short)hp);
            }
            if (mp > 0) {
                item.setMp((short)mp);
            }
            if (watk > 0) {
                item.setWatk((short)watk);
            }
            if (matk > 0) {
                item.setMatk((short)matk);
            }
            if (wdef > 0) {
                item.setWdef((short)wdef);
            }
            if (mdef > 0) {
                item.setMdef((short)mdef);
            }
            if (hb > 0) {
                item.setAvoid((short)hb);
            }
            if (mz > 0) {
                item.setAcc((short)mz);
            }
            if (ty > 0) {
                item.setJump((short)ty);
            }
            if (yd > 0) {
                item.setSpeed((short)yd);
            }
            MapleInventoryManipulator.addbyItem(cg, item.copy());
        }
        else {
            MapleInventoryManipulator.addById(cg, id, (short)1, "", (byte)0);
        }
        cg.getSession().write((Object)MaplePacketCreator.getShowItemGain(id, (short)1, true));
    }

    public void gainItemS(final int id, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final MapleClient cg, final int time,final String ow) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleInventoryType type = GameConstants.getInventoryType(id);
        if (!MapleInventoryManipulator.checkSpace(cg, id, 1, "")) {
            return;
        }
        if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
            final Equip item = (Equip)ii.getEquipById(id);
            final String name = ii.getName(id);
            if (id / 10000 == 114 && name != null && name.length() > 0) {
                final String msg = "你已获得称号 <" + name + ">";
                cg.getPlayer().dropMessage(5, msg);
                cg.getPlayer().dropMessage(5, msg);
            }
            if (time > 0) {
                item.setExpiration(System.currentTimeMillis() + (long)(time * 60 * 60 * 1000));
            }
            if (str > 0) {
                item.setStr((short)str);
            }
            if (dex > 0) {
                item.setDex((short)dex);
            }
            if (luk > 0) {
                item.setLuk((short)luk);
            }
            if (Int > 0) {
                item.setInt((short)Int);
            }
            if (hp > 0) {
                item.setHp((short)hp);
            }
            if (mp > 0) {
                item.setMp((short)mp);
            }
            if (watk > 0) {
                item.setWatk((short)watk);
            }
            if (matk > 0) {
                item.setMatk((short)matk);
            }
            if (wdef > 0) {
                item.setWdef((short)wdef);
            }
            if (mdef > 0) {
                item.setMdef((short)mdef);
            }
            if (hb > 0) {
                item.setAvoid((short)hb);
            }
            if (mz > 0) {
                item.setAcc((short)mz);
            }
            if (ty > 0) {
                item.setJump((short)ty);
            }
            if (yd > 0) {
                item.setSpeed((short)yd);
            }
            if (ow != null)
            {
                item.setOwner(ow);
            }
            MapleInventoryManipulator.addbyItem(cg, item.copy());
        }
        else {
            MapleInventoryManipulator.addById(cg, id, (short)1, "", (byte)0);
        }
        cg.getSession().write((Object)MaplePacketCreator.getShowItemGain(id, (short)1, true));
    }

    public void gainItemStatus(final int id, final short quantity) {
        this.gainItemStatus(id, quantity, false, 0L, -1, "");
    }

    public void gainItemStatus(final int id, final short quantity, final boolean randomStats, final long period, final int slots, final String owner) {
        this.gainItemStatus(id, quantity, randomStats, period, slots, owner, this.c);
    }

    public void gainItemStatus(final int id, final short quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient cg) {
        if (quantity >= 0) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(id);
            if (!MapleInventoryManipulator.checkSpace(cg, id, (int)quantity, "")) {
                return;
            }
            if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                final Equip item = (Equip)(Equip)(randomStats ? ii.randomizeStats((Equip)ii.getEquipById(id)) : ii.getEquipById(id));
                if (period > 0L) {
                    item.setExpiration(System.currentTimeMillis() + period * 24L * 60L * 60L * 1000L);
                }
                if (slots > 0) {
                    item.setUpgradeSlots((byte)(item.getUpgradeSlots() + slots));
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                item.setStr((short)1);
                item.setDex((short)1);
                item.setInt((short)1);
                item.setLuk((short)1);
                final String name = ii.getName(id);
                if (id / 10000 == 114 && name != null && name.length() > 0) {
                    final String msg = "你已获得称号 <" + name + ">";
                    cg.getPlayer().dropMessage(-1, msg);
                    cg.getPlayer().dropMessage(5, msg);
                }
                MapleInventoryManipulator.addbyItem(cg, item.copy());
            }
            else {
                MaplePet pet;
                if (類型.寵物(id)) {
                    pet = MaplePet.createPet(id, MapleInventoryIdentifier.getInstance());
                }
                else {
                    pet = null;
                }
                MapleInventoryManipulator.addById(cg, id, quantity, (owner == null) ? "" : owner, pet, period);
            }
        }
        else {
            MapleInventoryManipulator.removeById(cg, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        cg.sendPacket(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }

    public void 给限时道具(final int id, final short quantity,long period) {
        this.给限时道具1(id, quantity, false, period>0?period:0L, -1, "",this.c);
    }
    public void 给限时道具1(final int id, final short quantity, final boolean randomStats, final long period, final int slots, final String owner, final MapleClient cg) {
        if (quantity >= 0) {
            final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            final MapleInventoryType type = GameConstants.getInventoryType(id);
            if (!MapleInventoryManipulator.checkSpace(cg, id, (int)quantity, "")) {
                return;
            }
            if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
                final Equip item = (Equip)(Equip)(randomStats ? ii.randomizeStats((Equip)ii.getEquipById(id)) : ii.getEquipById(id));
                if (period > 0L) {
                    item.setExpiration(System.currentTimeMillis() + period * 60L * 60L * 1000L);
                }
                if (slots > 0) {
                    item.setUpgradeSlots((byte)(item.getUpgradeSlots() + slots));
                }
                if (owner != null) {
                    item.setOwner(owner);
                }
                item.setStr((short)1);
                item.setDex((short)1);
                item.setInt((short)1);
                item.setLuk((short)1);
                final String name = ii.getName(id);
                if (id / 10000 == 114 && name != null && name.length() > 0) {
                    final String msg = "你已获得称号 <" + name + ">";
                    cg.getPlayer().dropMessage(-1, msg);
                    cg.getPlayer().dropMessage(5, msg);
                }
                //重构装备属性
                Map<Integer, List<ItemInfo>> itemInfo = GetRedisDataUtil.getItemInfo();
                if(Objects.nonNull(itemInfo) && ListUtil.isNotEmpty(itemInfo.get(item.getItemId())) ){
                    ItemInfo itemInfos = itemInfo.get(item.getItemId()).get(0);
                    item.setStr(itemInfos.getStr());
                    item.setDex(itemInfos.getDex());
                    item.setLuk(itemInfos.getLuk());
                    item.setInt(itemInfos.getIntValue());
                    item.setHp(itemInfos.getHp());
                    item.setMp(itemInfos.getMp());
                    item.setMatk(itemInfos.getMatk());
                    item.setWatk(itemInfos.getWatk());
                    item.setWdef(itemInfos.getWdef());
                    item.setMdef(itemInfos.getMdef());
                }
                MapleInventoryManipulator.addbyItem(cg, item.copy());
            }
            else {
                MaplePet pet;
                if (類型.寵物(id)) {
                    pet = MaplePet.createPet(id, MapleInventoryIdentifier.getInstance());
                }
                else {
                    pet = null;
                }
                MapleInventoryManipulator.addByIdxians(cg, id, quantity, (owner == null) ? "" : owner, pet, period);
            }
        }
        else {
            MapleInventoryManipulator.removeById(cg, GameConstants.getInventoryType(id), id, -quantity, true, false);
        }
        cg.sendPacket(MaplePacketCreator.getShowItemGain(id, quantity, true));
    }
    public void changeMusic(final String songName) {
        this.getPlayer().getMap().broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public void worldMessage(final int type, final String message) {
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void playerMessage(final String message) {
        this.playerMessage(5, message);
    }

    public void mapMessage(final String message) {
        this.mapMessage(5, message);
    }

    public void guildMessage(final String message) {
        this.guildMessage(5, message);
    }

    public void playerMessage(final int type, final String message) {
        this.getClient().sendPacket(MaplePacketCreator.serverNotice(type, message));
    }

    public void mapMessage(final int type, final String message) {
        this.getClient().getPlayer().getMap().broadcastMessage(MaplePacketCreator.serverNotice(type, message));
    }

    public void guildMessage(final int type, final String message) {
        if (this.getPlayer().getGuildId() > 0) {
            Guild.guildPacket(this.getPlayer().getGuildId(), MaplePacketCreator.serverNotice(type, message));
        }
    }
    public void serverNotice(String Text) {
        this.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(6, Text));
    }
    public final MapleGuild getGuild() {
        return this.getGuild(this.getPlayer().getGuildId());
    }

    public final MapleGuild getGuild(final int guildid) {
        return Guild.getGuild(guildid);
    }

    public final MapleParty getParty() {
        return this.getClient().getPlayer().getParty();
    }

    public final int getCurrentPartyId(final int mapid) {
        return this.getMap(mapid).getCurrentPartyId();
    }

    public final boolean isLeader() {
        return this.getParty() != null && this.getParty().getLeader().getId() == this.getClient().getPlayer().getId();
    }

    public final boolean isAllPartyMembersAllowedJob(final int job) {
        if (this.c.getPlayer().getParty() == null) {
            return false;
        }
        for (final MaplePartyCharacter mem : this.getClient().getPlayer().getParty().getMembers()) {
            if (mem.getJobId() / 100 != job) {
                return false;
            }
        }
        return true;
    }

    public final boolean allMembersHere() {
        if (this.c.getPlayer().getParty() == null) {
            return false;
        }
        for (final MaplePartyCharacter mem : this.getClient().getPlayer().getParty().getMembers()) {
            MapleCharacter chr = this.getClient().getPlayer().getMap().getCharacterById(mem.getId());
            if (chr == null) {
                return false;
            }
        }
        return true;
    }

    public void warpParty(final int mapId) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.warp(mapId, 0);
            return;
        }
        final MapleMap target = this.getMap(mapId);
        final int cMap = this.getPlayer().getMapId();
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == this.getPlayer().getEventInstance())) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void warpParty(final int mapId, final int portal) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            if (portal < 0) {
                this.warp(mapId);
            }
            else {
                this.warp(mapId, portal);
            }
            return;
        }
        final boolean rand = portal < 0;
        final MapleMap target = this.getMap(mapId);
        final int cMap = this.getPlayer().getMapId();
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == this.getPlayer().getEventInstance())) {
                if (rand) {
                    try {
                        curChar.changeMap(target, target.getPortal(Randomizer.nextInt(target.getPortals().size())));
                    }
                    catch (Exception e) {
                        curChar.changeMap(target, target.getPortal(0));
                    }
                }
                else {
                    curChar.changeMap(target, target.getPortal(portal));
                }
            }
        }
    }

    public void warpParty_Instanced(final int mapId) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.warp_Instanced(mapId);
            return;
        }
        final MapleMap target = this.getMap_Instanced(mapId);
        final int cMap = this.getPlayer().getMapId();
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == this.getPlayer().getEventInstance())) {
                curChar.changeMap(target, target.getPortal(0));
            }
        }
    }

    public void gainMeso(final int gain) {
        if ( this.getClient()==null){
            return;
        }
        if ( this.getClient().getPlayer()==null){
            return;
        }
        this.getClient().getPlayer().gainMeso(gain, true, false, true);
    }
    public void gainMesoZ(final int gain) {
        if (gain < 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷金币而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷金币而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷金币而被管理员永久停封。"));
            this.getPlayer().ban("刷金币", true, true, false);
        }else {
            this.getClient().getPlayer().gainMeso(gain, true, false, true);
        }
    }
    public void gainMesoF(final int gain) {
        if (gain > 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷金币而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷金币而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷金币而被管理员永久停封。"));
            this.getPlayer().ban("刷金币", true, true, false);
        }else {
            this.getClient().getPlayer().gainMeso(gain, true, false, true);
        }
    }

    public void gainExp(final int gain) {
        this.getClient().getPlayer().gainExp(gain, true, true, true);
    }

    public void gainExpR(final int gain) {
        this.getClient().getPlayer().gainExp((int) (gain * this.getClient().getChannelServer().getExpRate()), true, true, true);
    }

    public void givePartyItems(final int id, final short quantity, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            if (quantity >= 0) {
                MapleInventoryManipulator.addById(chr.getClient(), id, quantity);
            }
            else {
                MapleInventoryManipulator.removeById(chr.getClient(), GameConstants.getInventoryType(id), id, -quantity, true, false);
            }
            chr.getClient().sendPacket(MaplePacketCreator.getShowItemGain(id, quantity, true));
        }
    }

    public void givePartyItems(final int id, final short quantity) {
        this.givePartyItems(id, quantity, false);
    }

    public final boolean canPartyHold() {
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                for (int i = 1; i <= 5; ++i) {
                    if (curChar.getInventory(MapleInventoryType.getByType((byte)i)).getNextFreeSlot() <= -1) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void givePartyItems(final int id, final short quantity, final boolean removeAll) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainItem(id, (short)(removeAll ? (-this.getPlayer().itemQuantity(id)) : quantity));
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                this.gainItem(id, (short)(removeAll ? (-curChar.itemQuantity(id)) : quantity), false, 0L, 0, "", curChar.getClient());
            }
        }
    }
    public void giveDarkMapList(int mapId,String eventStr,int channelId) {
        Start.giveDarkMapList(mapId,eventStr,channelId);
    }
    public void deleteDarkMapList(String eventStr,int channelId) {
        Start.deleteDarkMapList(eventStr,channelId);
    }

    public int giveDarkMap(int mapId,String eventStr,int channelId) {
        List<Integer> integers = Start.darkMap.get(channelId).get(eventStr);
        if (ListUtil.isNotEmpty(integers)){
            for (int i = 0; i < integers.size(); i++) {
                if (integers.get(i) == mapId && (i+1)<integers.size()){
                    return integers.get(i+1);
                }
            }
        }
        return 910000000;
    }
    /**
     * 查团队是否开启暗黑模式
     * @return
     */
    public boolean getPartyIsOpenEnableDarkMode() {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            return this.getPlayer().isOpenEnableDarkMode();
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null && !curChar.isOpenEnableDarkMode()) {
                return false;
            }
        }
        return true;
    }

    public void givePartyExp( int amount,  List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            if(amount * this.getClient().getChannelServer().getExpRate() >= Integer.MAX_VALUE){
                chr.gainExp(Integer.MAX_VALUE, true, true, true);
            }else{
                chr.gainExp((int)(amount * this.getClient().getChannelServer().getExpRate()), true,true, true);
            }
        }
    }

    public void givePartyExp(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainExp((int) (amount * this.getClient().getChannelServer().getExpRate()));
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.gainExp((int)(amount * this.getClient().getChannelServer().getExpRate()), true, true, true);
            }
        }
    }

    public void givePartyNX(final int amount, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.modifyCSPoints(1, amount, true);
        }
    }
    public void 给当前地图时钟(int minutes, boolean refresh, boolean kickOut) {
        this.c.getPlayer().getMap().给时钟(minutes, refresh, kickOut);
    }
    public void givePartyNX(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainNX(amount);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.modifyCSPoints(1, amount, true);
            }
        }
    }

    public void endPartyQuest(final int amount, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.endPartyQuest(amount);
        }
    }

    public void endPartyQuest(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.getPlayer().endPartyQuest(amount);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.endPartyQuest(amount);
            }
        }
    }

    public void removeFromParty(final int id, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            final int possesed = chr.getInventory(GameConstants.getInventoryType(id)).countById(id);
            if (possesed > 0) {
                MapleInventoryManipulator.removeById(this.c, GameConstants.getInventoryType(id), id, possesed, true, false);
                chr.getClient().sendPacket(MaplePacketCreator.getShowItemGain(id, (short)(-possesed), true));
            }
        }
    }

    public void removeFromParty(final int id) {
        this.givePartyItems(id, (short)0, true);
    }

    public void useSkill(final int skill, final int level) {
        if (level <= 0) {
            return;
        }
        SkillFactory.getSkill(skill).getEffect(level).applyTo(this.c.getPlayer());
    }

    public void useItem(final int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(this.c.getPlayer());
        this.getClient().sendPacket(UIPacket.getStatusMsg(id));
    }

    public void useItemEffect(final int id) {
        MapleItemInformationProvider.getInstance().getItemEffect(id).applyTo(this.c.getPlayer());
        this.getClient().sendPacket(MaplePacketCreator.enableActions());
    }

    public void cancelItem(final int id) {
        this.getClient().getPlayer().cancelEffect(MapleItemInformationProvider.getInstance().getItemEffect(id), false, -1L);
    }

    public final int getMorphState() {
        return this.getClient().getPlayer().getMorphState();
    }

    public void removeAll(final int id) {
        this.getClient().getPlayer().removeAll(id, true);
    }

    public void gainCloseness(final int closeness, final int index) {
        final MaplePet pet = this.getPlayer().getPet(index);
        if (pet != null) {
            pet.setCloseness(pet.getCloseness() + closeness);
            this.getClient().sendPacket(PetPacket.updatePet(pet, this.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short)(byte)pet.getInventoryPosition())));
        }
    }

    public void gainClosenessAll(final int closeness) {
        for (final MaplePet pet : this.getPlayer().getPets()) {
            if (pet != null) {
                pet.setCloseness(pet.getCloseness() + closeness);
                this.getClient().sendPacket(PetPacket.updatePet(pet, this.getPlayer().getInventory(MapleInventoryType.CASH).getItem((short)(byte)pet.getInventoryPosition())));
            }
        }
    }

    public void resetMap(final int mapid) {
        this.getMap(mapid).resetFully();
    }

    public void openNpc(final int id) {
       // NPCScriptManager.getInstance().dispose(this.c);
        this.openNpc(id, null);
    }

    public void openNpc(final int id, final int mode) {
      //  NPCScriptManager.getInstance().dispose(this.c);
        this.openNpc(this.getClient(), id, mode, null);
    }

    public void openNpc(final MapleClient cg, final int id) {
        NPCScriptManager.getInstance().dispose(cg);
        this.openNpc(cg, id, 0, null);
    }

    public void openNpc(final int id, final String script) {
       // NPCScriptManager.getInstance().dispose(this.c);
        this.openNpc(this.getClient(), id, script);
    }

    public void openNpc(final MapleClient cg, final int id, final String script) {
        this.openNpc(this.getClient(), id, 0, script);
    }

    public void openNpc(final MapleClient cg, final int id, final int mode, final String script) {
        NPCScriptManager.getInstance().dispose(cg);
        cg.removeClickedNPC();
        NPCScriptManager.getInstance().start(cg, id, mode, script);
    }

    public final int getMapId() {
        return this.getClient().getPlayer().getMapId();
    }

    public final boolean haveMonster(final int mobid) {
        for (final MapleMapObject obj : this.getClient().getPlayer().getMap().getAllMonstersThreadsafe()) {
            final MapleMonster mob = (MapleMonster)obj;
            if (mob.getId() == mobid) {
                return true;
            }
        }
        return false;
    }

    public final int getChannelNumber() {
        return this.getClient().getChannel();
    }

    public final int getMonsterCount(final int mapid) {
        return this.getClient().getChannelServer().getMapFactory().getMap(mapid).getNumMonsters();
    }

    public void teachSkill(final int id, final byte level, final byte masterlevel) {
        this.getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }
    public void 技能进阶(final int id,final byte masterlevel) {
        final ISkill skil = SkillFactory.getSkill(id);
        this.getPlayer().changeSkillLevel(skil, this.getPlayer().getSkillLevel(skil), masterlevel);
    }
    public void teachSkill(final int id, byte level) {
        final ISkill skil = SkillFactory.getSkill(id);
        if (this.getPlayer().getSkillLevel(skil) > level) {
            level = this.getPlayer().getSkillLevel(skil);
        }
        this.getPlayer().changeSkillLevel(skil, level, skil.getMaxLevel());
    }

    public final int getPlayerCount(final int mapid) {
        return this.getClient().getChannelServer().getMapFactory().getMap(mapid).getCharactersSize();
    }

    public void dojo_getUp() {

        this.getClient().sendPacket(MaplePacketCreator.updateInfoQuest(1207, "pt=1;min=4;belt=1;tuto=1"));
        this.getClient().sendPacket(MaplePacketCreator.Mulung_DojoUp2());
        this.getClient().sendPacket(MaplePacketCreator.instantMapWarp((byte)6));
    }

    public final boolean dojoAgent_NextMap(final boolean dojo, final boolean fromresting) {
        if (System.currentTimeMillis() - this.c.getPlayer().getWlTime() < 5000){
            return false;
        }
        this.c.getPlayer().setWlTime(System.currentTimeMillis());
        if (this.c.getPlayer().getMapId() == 925033800){
            this.c.getPlayer().setBossLog("累计武陵",1,1);
        }
        if (dojo) {
            return Event_DojoAgent.warpNextMap(this.c.getPlayer(), fromresting);
        }
        return Event_DojoAgent.warpNextMap_Agent(this.c.getPlayer(), fromresting);
    }

    public final int dojo_getPts() {
        return this.getClient().getPlayer().getDojo();
    }

    public final MapleEvent getEvent(final String loc) {
        return this.getClient().getChannelServer().getEvent(MapleEventType.valueOf(loc));
    }

    public final int getSavedLocation(final String loc) {
        final Integer ret = this.getClient().getPlayer().getSavedLocation(SavedLocationType.fromString(loc));
        if (ret == null || (int)ret == -1) {
            return 100000000;
        }
        return (int)ret;
    }

    public void saveLocation(final String loc) {
        this.getClient().getPlayer().saveLocation(SavedLocationType.fromString(loc));
    }

    public void saveReturnLocation(final String loc) {
        this.getClient().getPlayer().saveLocation(SavedLocationType.fromString(loc), this.getClient().getPlayer().getMap().getReturnMap().getId());
    }

    public void clearSavedLocation(final String loc) {
        this.getClient().getPlayer().clearSavedLocation(SavedLocationType.fromString(loc));
    }

    public void summonMsg(final String msg) {
        if (!this.c.getPlayer().hasSummon()) {
            this.playerSummonHint(true);
        }
        this.getClient().sendPacket(UIPacket.summonMessage(msg));
    }

    public void summonMsg(final int type) {
        if (!this.c.getPlayer().hasSummon()) {
            this.playerSummonHint(true);
        }
        this.getClient().sendPacket(UIPacket.summonMessage(type));
    }

    public void showInstruction(final String msg, final int width, final int height) {
        this.getClient().sendPacket(MaplePacketCreator.sendHint(msg, width, height));
    }

    public void playerSummonHint(final boolean summon) {
        this.getClient().getPlayer().setHasSummon(summon);
        this.getClient().sendPacket(UIPacket.summonHelper(summon));
    }

    public final String getInfoQuest(final int id) {
        return this.getClient().getPlayer().getInfoQuest(id);
    }

    public void updateInfoQuest(final int id, final String data) {
        this.getClient().getPlayer().updateInfoQuest(id, data);
    }

    public final boolean getEvanIntroState(final String data) {
        return this.getInfoQuest(22013).equals((Object)data);
    }

    public void updateEvanIntroState(final String data) {
        this.updateInfoQuest(22013, data);
    }

    public void Aran_Start() {
        this.getClient().sendPacket(UIPacket.Aran_Start());
    }

    public void evanTutorial(final String data, final int v1) {
        this.getClient().sendPacket(MaplePacketCreator.getEvanTutorial(data));
    }

    public void AranTutInstructionalBubble(final String data) {
        this.getClient().sendPacket(UIPacket.AranTutInstructionalBalloon(data));
    }

    public void ShowWZEffect(final String data) {
        this.getClient().sendPacket(UIPacket.AranTutInstructionalBalloon(data));
    }

    public void showWZEffect(final String data) {
        this.getClient().sendPacket(UIPacket.ShowWZEffect(data));
    }

    public void EarnTitleMsg(final String data) {
        this.getClient().sendPacket(UIPacket.EarnTitleMsg(data));
    }

    public void MovieClipIntroUI(final boolean enabled) {
        this.getClient().sendPacket(UIPacket.IntroDisableUI(enabled));
        this.getClient().sendPacket(UIPacket.IntroLock(enabled));
    }

    public MapleInventoryType getInvType(final int i) {
        return MapleInventoryType.getByType((byte)i);
    }

    public String getItemName( int id) {
        return MapleItemInformationProvider.getInstance().getName(id);
    }

    public void gainPet(final int id, final String name, final int level, final int closeness, final int fullness) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        this.gainPet(id, name, level, closeness, fullness, (long)ii.getPetLife(id), ii.getPetFlagInfo(id));
    }

    public void gainPet(final int id, final String name, final int level, final int closeness, final int fullness, final int period) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        this.gainPet(id, name, level, closeness, fullness, (long)period, ii.getPetFlagInfo(id));
    }

    public void gainPet(int id, final String name, int level, int closeness, int fullness, final long period, final short flags) {
        if (id > 5010000 || id < 5000000) {
            id = 5000000;
        }
        if (level > 30) {
            level = 30;
        }
        if (closeness > 30000) {
            closeness = 30000;
        }
        if (fullness > 100) {
            fullness = 100;
        }
        try {
            MapleInventoryManipulator.addById(this.c, id, (short)1, "", MaplePet.createPet(id, name, level, closeness, fullness, MapleInventoryIdentifier.getInstance(), (id == 5000054) ? ((int)period) : 0, flags), 45L);
        }
        catch (NullPointerException ex) {
            //Ex.printStackTrace();
        }
    }

    public void removeSlot(final int invType, final byte slot, final short quantity) {
        MapleInventoryManipulator.removeFromSlot(this.c, this.getInvType(invType), (short)slot, quantity, true);
    }

    public void gainGP(final int gp) {
        if (this.getPlayer().getGuildId() <= 0) {
            return;
        }
        Guild.gainGP(this.getPlayer().getGuildId(), gp);
    }

    public int getGP() {
        if (this.getPlayer().getGuildId() <= 0) {
            return 0;
        }
        return Guild.getGP(this.getPlayer().getGuildId());
    }

    public void showMapEffect(final String path) {
        this.getClient().sendPacket(UIPacket.MapEff(path));
    }

    public int itemQuantity(final int itemid) {
        return this.getPlayer().itemQuantity(itemid);
    }

    public EventInstanceManager getDisconnected(final String event) {
        final EventManager em = this.getEventManager(event);
        if (em == null) {
            return null;
        }
        for (final EventInstanceManager eim : em.getInstances()) {
            if (eim.isDisconnected(this.c.getPlayer()) && eim.getPlayerCount() > 0) {
                return eim;
            }
        }
        return null;
    }

    public boolean isAllReactorState(final int reactorId, final int state) {
        boolean ret = false;
        for (final MapleReactor r : this.getMap().getAllReactorsThreadsafe()) {
            if (r.getReactorId() == reactorId) {
                ret = (r.getState() == state);
            }
        }
        return ret;
    }

    public void 道具喇叭(int itemId, String msg) {
        Object item;
        if (GameConstants.getInventoryType(itemId) == MapleInventoryType.EQUIP) {
            item = (Equip)ii.getEquipById(itemId);
            ((IItem)item).setPosition((short)1);
        } else {
            item = new Item(itemId, (short)1, (short)1, (byte)0, -1);
        }

        this.道具喇叭((IItem)item, msg);
    }

    public void 道具喇叭(IItem item, String msg) {
        StringBuilder sb = new StringBuilder();
        IItem medal = this.c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem((short)-26);
        if (medal != null) {
            sb.append("<");
            sb.append(ii.getName(medal.getItemId()));
            sb.append("> ");
        }

        sb.append(this.c.getPlayer().getName());
        sb.append(" : ");
        sb.append(msg);
        Broadcast.broadcastSmega(this.c.getWorld(), MaplePacketCreator.itemMegaphone(sb.toString(), true, this.c.getChannel() - this.c.getWorld() * 10, item));
    }


    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public void spawnMonster(final int id) {
        this.spawnMonster(id, 1, new Point(this.getPlayer().getPosition()));
    }

    public void spawnMonster(final int id, final int x, final int y) {
        this.spawnMonster(id, 1, new Point(x, y));
    }

    public void spawnMonster(final int id, final int qty, final int x, final int y) {
        this.spawnMonster(id, qty, new Point(x, y));
    }

    public void spawnMonster(final int id, final int qty, final Point pos) {
        for (int i = 0; i < qty; ++i) {
            this.getMap().spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
        }
    }

    public void sendNPCText(final String text, final int npc) {
        this.getMap().broadcastMessage(MaplePacketCreator.getNPCTalk(npc, (byte)0, text, "00 00", (byte)0));
    }

    public void warpAllPlayer(final int from, final int to) {
        final MapleMap tomap = this.getMapFactory().getMap(to);
        final MapleMap frommap = this.getMapFactory().getMap(from);
        final List<MapleCharacter> list = frommap.getCharactersThreadsafe();
        if (tomap != null && frommap != null && list != null && frommap.getCharactersSize() > 0) {
            for (final MapleMapObject mmo : list) {
                ((MapleCharacter)mmo).changeMap(tomap, tomap.getPortal(0));
            }
        }
    }

    public MapleMapFactory getMapFactory() {
        return this.getChannelServer().getMapFactory();
    }

    public void enterMTS() {
        InterServerHandler.EnterCashShop(this.c, this.c.getPlayer(), true);
    }

    public int getChannelOnline() {
        return this.getClient().getChannelServer().getConnectedClients();
    }

    public int getTotalOnline() {
        return (int)Integer.valueOf(ChannelServer.getAllInstances().stream().map(cserv -> Integer.valueOf(cserv.getConnectedClients())).reduce(Integer.valueOf(0), Integer::sum));
    }

    public int getMP() {
        return this.getPlayer().getMP();
    }

    public void setMP(final int x) {
        this.getPlayer().setMP(x);
    }

    public int save(final boolean dc, final boolean fromcs) {
        try {
            return this.getPlayer().saveToDB(dc, fromcs,true);
        }
        catch (UnsupportedOperationException ex) {
            return 0;
        }
    }
    public DamageManage.MobDamageData newDamageData() {
        return DamageManage.getInstance().newDamageData();
    }

    public DamageManage getDamageManage() {
        return DamageManage.getInstance();
    }

    public void save() {
        this.save(false, false);
    }

    public boolean hasSquadByMap() {
        return this.getPlayer().getMap().getSquadByMap() != null;
    }

    public boolean hasEventInstance() {
        return this.getPlayer().getEventInstance() != null;
    }

    public boolean hasEMByMap() {
        return this.getPlayer().getMap().getEMByMap() != null;
    }

    public void processCommand(final String line) {
        CommandProcessor.processCommand(this.getClient(), line, CommandType.NORMAL);
    }

    public void warpPlayer(final int from, final int to) {
        final MapleMap mapto = this.c.getChannelServer().getMapFactory().getMap(to);
        final MapleMap mapfrom = this.c.getChannelServer().getMapFactory().getMap(from);
        for (MapleCharacter chr : mapfrom.getCharactersThreadsafe()) {
            chr.changeMap(mapto, mapto.getPortal(0));
        }
    }

    public void isVipMedalName() {
        if (this.getOneTimeLog("关闭VIP星星數顯示") < 1) {
            this.setOneTimeLog("关闭VIP星星數顯示");
            this.c.getPlayer().dropMessage(5, "关闭VIP星星數顯示。");
        }
        else {
            this.deleteOneTimeLog("关闭VIP星星數顯示");
            this.c.getPlayer().dropMessage(5, "开启VIP星星數顯示。");
        }
    }
    public void showDpsUi(boolean isOpen, MapleCharacter chr) {
        chr.getClient().sendPacket(GuiPacketCreator.ShowGui(isOpen));
    }

    public void showDpsPlayer(MapleCharacter chr) {
        List<GuiPlayerEntity> items1 = new ArrayList();
        items1.add(new GuiPlayerEntity(chr.getId(), chr.getName(), "0"));
        chr.getClient().sendPacket(GuiPacketCreator.ShowPlayer(items1));
    }

    public void updateDps(MapleCharacter chr, String text) {
        chr.getClient().sendPacket(GuiPacketCreator.UpdateDps(chr.getId(), text));
    }

    public void clearDps() {
        Broadcast.broadcastMessage(GuiPacketCreator.ClearDps());
    }
    public int getVip() {
        return this.getPlayer().getVip();
    }

    public void getItemLog(final String mob, final String itemmob) {
        FileoutputUtil.logToFile("logs/Data/" + mob + ".txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.c.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.c.getAccountName() + " 账号ID " + this.c.getAccID() + " 角色名 " + this.c.getPlayer().getName() + " 角色ID " + this.c.getPlayer().getId() + " " + itemmob);
    }

    public int getAccNewTime(final String time) {
        return this.getPlayer().getAccNewTime(time);
    }

    public int getQianDaoTime(final String time) {
        return this.getPlayer().getQianDaoTime(time);
    }

    public int getQianDaoAcLog(final String time) {
        return this.getPlayer().getQianDaoAcLog(time);
    }

    public void giveEventPrize() {
        final int reward = RandomRewards.getInstance().getEventReward();
        if (reward == 0) {
            this.getPlayer().gainMeso(66666, true, false, false);
            this.getPlayer().dropMessage(5, "你获得 66666 金币");
        }
        else if (reward == 1) {
            this.getPlayer().gainMeso(399999, true, false, false);
            this.getPlayer().dropMessage(5, "你获得 399999 金币");
        }
        else if (reward == 2) {
            this.getPlayer().gainMeso(666666, true, false, false);
            this.getPlayer().dropMessage(5, "你获得 666666 金币");
        }
        else if (reward == 3) {
            this.getPlayer().addFame(10);
            this.getPlayer().dropMessage(5, "你获得 10 名聲");
        }
        else {
            int max_quantity = 1;
            switch (reward) {
                case 5062000: {
                    max_quantity = 3;
                    break;
                }
                case 5220000: {
                    max_quantity = 25;
                    break;
                }
                case 4031307:
                case 5050000: {
                    max_quantity = 5;
                    break;
                }
                case 2022121: {
                    max_quantity = 10;
                    break;
                }
            }
            final int quantity = ((max_quantity > 1) ? Randomizer.nextInt(max_quantity) : 0) + 1;
            if (MapleInventoryManipulator.checkSpace(this.getPlayer().getClient(), reward, quantity, "")) {
                MapleInventoryManipulator.addById(this.getPlayer().getClient(), reward, (short)quantity);
                this.getPlayer().dropMessage(5, "恭喜获得" + MapleItemInformationProvider.getInstance().getName(reward));
            }
            else {
                this.getPlayer().gainMeso(100000, true, false, false);
                this.getPlayer().dropMessage(5, "參加獎 100000 金币");
            }
        }
    }

    public List<IItem> getMonsterRidinglist() {
        final MapleInventory Equip = this.c.getPlayer().getInventory(MapleInventoryType.EQUIP);
        final List<IItem> ret = new ArrayList<IItem>();
        for (final IItem tep : Equip) {
            if (tep.getItemId() >= 1930000 && tep.getItemId() <= 1992050) {
                ret.add(tep);
            }
        }
        return ret;
    }

    public String getCharacterNameById(final int id) {
        this.c.getPlayer();
        final String name = MapleCharacter.getCharacterNameById(id);
        return name;
    }

    public final int getCharacterIdByName(final String name) {
        this.c.getPlayer();
        final int id = MapleCharacter.getCharacterIdByName(name);
        return id;
    }

    public int getCharacterByNameLevel(final String name) {
        this.c.getPlayer();
        final int level = MapleCharacter.getCharacterByName(name).getLevel();
        return level;
    }

    public List<IItem> getCsEquipList() {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleInventory Equip = this.c.getPlayer().getInventory(MapleInventoryType.EQUIP);
        final List<IItem> ret = new ArrayList<IItem>();
        for (final IItem tep : Equip) {
            if (ii.isCash(tep.getItemId())) {
                ret.add(tep);
            }
        }
        return ret;
    }

    public Equip getEquipStat(final byte slot) {
        final Equip sel = (Equip)this.c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)slot);
        return sel;
    }

    public void dropCs(final byte type, final short src, final short quantity) {
        MapleInventoryManipulator.dropCs(this.c, MapleInventoryType.getByType(type), src, quantity);
    }

    public final boolean canwncs() {
        for (final int i : GameConstants.blockedMaps) {
            if (this.c.getPlayer().getMapId() == i) {
                this.c.getPlayer().dropMessage(5, "當前地图无法使用.");
                return false;
            }
        }
        if (this.c.getPlayer().getMapId() == 749060605 || this.c.getPlayer().getMapId() == 229010000 || this.c.getPlayer().getMapId() == 910000000) {
            this.c.getPlayer().dropMessage(5, "當前地图无法使用.");
            return false;
        }
        if (this.c.getPlayer().getLevel() < 10 && this.c.getPlayer().getJob() != 200) {
            this.c.getPlayer().dropMessage(5, "你的等級不足10級无法使用.");
            return false;
        }
        if (this.c.getPlayer().hasBlockedInventory(true) || this.c.getPlayer().getMap().getSquadByMap() != null || this.c.getPlayer().getEventInstance() != null || this.c.getPlayer().getMap().getEMByMap() != null || this.c.getPlayer().getMapId() >= 990000000) {
            this.c.getPlayer().dropMessage(5, "请稍后再試");
            return false;
        }
        if ((this.c.getPlayer().getMapId() >= 680000210 && this.c.getPlayer().getMapId() <= 680000502) || (this.c.getPlayer().getMapId() / 1000 == 980000 && this.c.getPlayer().getMapId() != 980000000) || this.c.getPlayer().getMapId() / 100 == 1030008 || this.c.getPlayer().getMapId() / 100 == 922010 || this.c.getPlayer().getMapId() / 10 == 13003000) {
            this.c.getPlayer().dropMessage(5, "请稍后再試.");
            return false;
        }
        return true;
    }

    public int getGamePoints() {
        return this.c.getPlayer().getGamePoints();
    }

    public void gainGamePoints(final int amount) {
        this.c.getPlayer().gainGamePoints(amount);
    }

    public void resetGamePoints() {
        this.c.getPlayer().updateGamePoints(0);
    }

    public int getGamePointsPD() {
        return this.c.getPlayer().get在线时间();
    }

    public void gainGamePointsPD(final int amount) {
        this.c.getPlayer().gainGamePointsPD(amount);
    }

    public void resetGamePointsPD() {
        this.c.getPlayer().resetGamePointsPD();
    }

    public int getEquipItemType(final int itemid) {
        if (類型.帽子(itemid)) {
            return 1;
        }
        if (類型.臉飾(itemid)) {
            return 2;
        }
        if (類型.眼飾(itemid)) {
            return 3;
        }
        if (類型.耳環(itemid)) {
            return 4;
        }
        if (類型.上衣(itemid)) {
            return 5;
        }
        if (類型.套服(itemid)) {
            return 6;
        }
        if (類型.褲裙(itemid)) {
            return 7;
        }
        if (類型.鞋子(itemid)) {
            return 8;
        }
        if (類型.手套(itemid)) {
            return 9;
        }
        if (類型.盾牌(itemid)) {
            return 9;
        }
        if (類型.披風(itemid)) {
            return 10;
        }
        if (類型.戒指(itemid)) {
            return 11;
        }
        if (類型.墜飾(itemid)) {
            return 12;
        }
        if (類型.腰帶(itemid)) {
            return 13;
        }
        if (類型.勳章(itemid)) {
            return 15;
        }
        if (類型.武器(itemid)) {
            return 16;
        }
        if (類型.副手(itemid)) {
            return 17;
        }
        return 0;
    }

    public void forceReAddItem(final Item item, final byte type) {
        this.c.getPlayer().forceReAddItem_Flag((IItem)item, MapleInventoryType.getByType(type));
        this.c.getPlayer().equipChanged();
    }

    public void StatsZs() {
        final Map<MapleStat, Integer> statups = new EnumMap<MapleStat, Integer>(MapleStat.class);
        this.c.getPlayer().setLevel((short)1);
        this.c.getPlayer().levelUp();
        if (this.c.getPlayer().getExp() < 0) {
            this.c.getPlayer().gainExp(-this.c.getPlayer().getExp(), false, false, true);
        }
        this.c.getPlayer().getStat().str = 4;
        this.c.getPlayer().getStat().dex = 4;
        this.c.getPlayer().getStat().int_ = 4;
        this.c.getPlayer().getStat().luk = 4;
        this.c.getPlayer().setHpMpApUsed((short)0);
        this.c.getPlayer().setRemainingAp((short)13);
        this.c.getPlayer().setRemainingSp(0);
        this.c.getSession().write((Object)MaplePacketCreator.updateSp(this.c.getPlayer(), false));
        statups.put(MapleStat.STR, Integer.valueOf((int)this.c.getPlayer().getStat().getStr()));
        statups.put(MapleStat.DEX, Integer.valueOf((int)this.c.getPlayer().getStat().getDex()));
        statups.put(MapleStat.LUK, Integer.valueOf((int)this.c.getPlayer().getStat().getLuk()));
        statups.put(MapleStat.INT, Integer.valueOf((int)this.c.getPlayer().getStat().getInt()));
        statups.put(MapleStat.HP, Integer.valueOf((int)this.c.getPlayer().getStat().getHp()));
        statups.put(MapleStat.MAXHP, Integer.valueOf((int)this.c.getPlayer().getStat().getMaxHp()));
        statups.put(MapleStat.MP, Integer.valueOf((int)this.c.getPlayer().getStat().getMp()));
        statups.put(MapleStat.MAXMP, Integer.valueOf((int)this.c.getPlayer().getStat().getMaxMp()));
        statups.put(MapleStat.AVAILABLEAP, Integer.valueOf((int)this.c.getPlayer().getRemainingAp()));
        this.c.getPlayer().getStat().recalcLocalStats();
        this.c.getSession().write((Object)MaplePacketCreator.updatePlayerStats(statups, this.c.getPlayer()));
        this.c.getPlayer().fakeRelog();
    }

    public void maxSkillsByJob() {
        this.c.getPlayer().maxSkillsByJob();
    }

    public String getServerName() {
        return ServerConfig.SERVERNAME;
    }

    public void gainDY(final int gain) {
        this.c.getPlayer().modifyCSPoints(2, gain, true);
    }

    public void worldMessage2(final int type, final String message) {
        switch (type) {
            case 1:
            case 2:
            case 3:
            case 5:
            case 6:
            case 9:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(type, this.c.getChannel(), message));
                break;
            }
            default: {
                Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(6, this.c.getChannel(), message));
                break;
            }
        }
    }

    public void 全服黄色喇叭(final String message) {
        Broadcast.broadcastSmega(MaplePacketCreator.serverNotice(9, this.c.getChannel(), message));
    }

    public boolean canHoldSlots(final int slot) {
        for (int i = 1; i <= 5; ++i) {
            if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).isFull(slot)) {
                return false;
            }
        }
        return true;
    }

    public int getItemQuantity(final int itemid) {
        return this.c.getPlayer().getItemQuantity(itemid);
    }

    public final int getNX(final int 类型) {
        return this.c.getPlayer().getCSPoints(类型);
    }

    public void gainD(final int amount) {
        this.c.getPlayer().modifyCSPoints(2, amount, true);
    }

    public final int 判断职业() {
        return this.c.getPlayer().getJob();
    }

    public void 判断组队() {
        this.c.getPlayer().getParty();
    }

    public void 判断频道() {
        this.getClient().getChannel();
    }

    public void 给抵用券(final int amount) {
        this.c.getPlayer().modifyCSPoints(2, amount, true);
    }

    public void 收抵用券(final int amount) {
        this.c.getPlayer().modifyCSPoints(2, -amount, true);
    }

    public void 给点券(final int amount) {
        this.c.getPlayer().modifyCSPoints(1, amount, true);
    }

    public void 收点券(final int amount) {
        this.c.getPlayer().modifyCSPoints(1, -amount, true);
    }

    public void 给物品(final int id, final short quantity) {
        this.gainItem(id, quantity, false, 0L, -1, "");
    }

    public void 物品兑换1(final int id1, final short shuliang1, final int id2, final int shuliang2) {
        if (!this.haveItem(id1, (int)shuliang1, true, true)) {
            this.c.getPlayer().dropMessage(1, "你没有足够的兑换物品。");
            return;
        }
        this.gainItem(id1, (short)(-shuliang1), false, 0L, -1, "");
        this.gainItem(id2, (short)shuliang2, false, 0L, -1, "");
        this.c.getPlayer().dropMessage(1, "兑换成功。");
    }

    public void 概率给物品(final int id, final short quantity, final double 概率2, final String a) {
        this.概率给物品(id, quantity, 概率2);
    }

    public void 概率给物品(final int id, final short quantity, double 概率2) {
        if (概率2 > 100.0) {
            概率2 = 100.0;
        }
        if (概率2 <= 0.0) {
            概率2 = 0.0;
        }
        final double 概率3 = Math.ceil(Math.random() * 100.0);
        if (概率2 > 0.0 && 概率3 <= 概率2) {
            this.gainItem(id, quantity, false, 0L, -1, "");
        }
    }

    public void 概率给物品2(final int id, final short quantity, final double 概率2, final String a) {
        this.概率给物品2(id, quantity, 概率2);
    }

    public void 概率给物品2(final int id, final short quantity, double 概率2) {
        if (概率2 > 100.0) {
            概率2 = 100.0;
        }
        if (概率2 <= 0.0) {
            概率2 = 0.0;
        }
        final double 概率3 = Math.ceil(Math.random() * 100.0);
        if (概率2 > 0.0 && 概率3 <= 概率2) {
            short 数量 = (short)(int)Math.ceil(Math.random() * (double)quantity);
            if (数量 == 0) {
                数量 = 1;
            }
            this.gainItem(id, 数量, false, 0L, -1, "");
        }
    }

    public void 收物品(final int id, final short quantity) {
        this.gainItem(id, (short)(-quantity), false, 0L, -1, "");
    }

    public void gainItemS(final String Owner, final int id, final int sj, final int Flag, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final MapleClient cg) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleInventoryType type = GameConstants.getInventoryType(id);
        if (!MapleInventoryManipulator.checkSpace(cg, id, 1, "")) {
            return;
        }
        if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
            final Equip item = (Equip)(Equip)ii.getEquipById(id);
            final String name = ii.getName(id);
            if (id / 10000 == 114 && name != null && name.length() > 0) {
                final String msg = "你已获得称号 <" + name + ">";
                cg.getPlayer().dropMessage(5, msg);
            }
            if (Owner != null) {
                item.setOwner(Owner);
            }
            if (sj > 0) {
                item.setUpgradeSlots((byte)(short)sj);
            }
            if (Flag > 0) {
                item.setFlag((byte)(short)Flag);
            }
            if (str > 0) {
                item.setStr((short)str);
            }
            if (dex > 0) {
                item.setDex((short)dex);
            }
            if (luk > 0) {
                item.setLuk((short)luk);
            }
            if (Int > 0) {
                item.setInt((short)Int);
            }
            if (hp > 0) {
                item.setHp((short)hp);
            }
            if (mp > 0) {
                item.setMp((short)mp);
            }
            if (watk > 0) {
                item.setWatk((short)watk);
            }
            if (matk > 0) {
                item.setMatk((short)matk);
            }
            if (wdef > 0) {
                item.setWdef((short)wdef);
            }
            if (mdef > 0) {
                item.setMdef((short)mdef);
            }
            if (hb > 0) {
                item.setAvoid((short)hb);
            }
            if (mz > 0) {
                item.setAcc((short)mz);
            }
            if (ty > 0) {
                item.setJump((short)ty);
            }
            if (yd > 0) {
                item.setSpeed((short)yd);
            }
            MapleInventoryManipulator.addbyItem(cg, item.copy());
        }
        else {
            MapleInventoryManipulator.addById(cg, id, (short)1, "", (byte)0);
        }
        cg.sendPacket(MaplePacketCreator.getShowItemGain(id, (short)1, true));
    }

    public void gainItem(final String Owner, final int id, final int sj, final int Flag, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd) {
        this.gainItemS(Owner, id, sj, Flag, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, this.c);
    }

    public void 给属性装备(final int id, final int sj, final int Flag, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd) {
        this.给属性装备(id, sj, Flag, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, 0L, this.c);
    }

    public void 给属性装备(final int id, final int sj, final int Flag, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final int 给予时间) {
        this.给属性装备(id, sj, Flag, str, dex, luk, Int, hp, mp, watk, matk, wdef, mdef, hb, mz, ty, yd, (long)给予时间, this.c);
    }

    public void 给属性装备(final int id, final int sj, final int Flag, final int str, final int dex, final int luk, final int Int, final int hp, final int mp, final int watk, final int matk, final int wdef, final int mdef, final int hb, final int mz, final int ty, final int yd, final long 给予时间, final MapleClient cg) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final MapleInventoryType type = GameConstants.getInventoryType(id);
        if (!MapleInventoryManipulator.checkSpace(cg, id, 1, "")) {
            return;
        }
        if (type.equals((Object)MapleInventoryType.EQUIP) && !GameConstants.isThrowingStar(id) && !GameConstants.isBullet(id)) {
            final Equip item = (Equip)(Equip)ii.getEquipById(id);
            final String name = ii.getName(id);
            if (id / 10000 == 114 && name != null && name.length() > 0) {
                final String msg = "你已获得称号 <" + name + ">";
                cg.getPlayer().dropMessage(5, msg);
            }
            if (sj > 0) {
                item.setUpgradeSlots((byte)(short)sj);
            }
            if (Flag > 0) {
                item.setFlag((byte)(short)Flag);
            }
            if (str > 0) {
                item.setStr((short)str);
            }
            if (dex > 0) {
                item.setDex((short)dex);
            }
            if (luk > 0) {
                item.setLuk((short)luk);
            }
            if (Int > 0) {
                item.setInt((short)Int);
            }
            if (hp > 0) {
                item.setHp((short)hp);
            }
            if (mp > 0) {
                item.setMp((short)mp);
            }
            if (watk > 0) {
                item.setWatk((short)watk);
            }
            if (matk > 0) {
                item.setMatk((short)matk);
            }
            if (wdef > 0) {
                item.setWdef((short)wdef);
            }
            if (mdef > 0) {
                item.setMdef((short)mdef);
            }
            if (hb > 0) {
                item.setAvoid((short)hb);
            }
            if (mz > 0) {
                item.setAcc((short)mz);
            }
            if (ty > 0) {
                item.setJump((short)ty);
            }
            if (yd > 0) {
                item.setSpeed((short)yd);
            }
            if (给予时间 > 0L) {
                item.setExpiration(System.currentTimeMillis() + 给予时间 * 60L * 60L * 1000L);
            }
            MapleInventoryManipulator.addbyItem(cg, item.copy());
        }
        else {
            MapleInventoryManipulator.addById(cg, id, (short)1, "", (byte)0);
        }
        cg.sendPacket(MaplePacketCreator.getShowItemGain(id, (short)1, true));
    }

    public int getHour() {
        return Calendar.getInstance().get(11);
    }
    public final void 个人公告(String message) {
        this.playerMessage(6, message);
    }
    public final void 地图公告(String message) {
        this.mapMessage(6, message);
    }

    public int 判断日() {
        return Calendar.getInstance().get(5);
    }

    public int 判断时() {
        return Calendar.getInstance().get(11);
    }

    public int getMin() {
        return Calendar.getInstance().get(12);
    }

    public int 判断分() {
        return Calendar.getInstance().get(12);
    }

    public int getSec() {
        return Calendar.getInstance().get(13);
    }

    public final boolean 是否队长() {
        return this.getParty() != null && this.getParty().getLeader().getId() == this.c.getPlayer().getId();
    }

    public final void 传送地图(int map, String portal) {
        MapleMap mapz = this.getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = mapz.getSnowballPortal();
        }

        if (map == this.c.getPlayer().getMapId()) {
            new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }

    }

    public final void 传送地图(int map, int portal) {
        MapleMap mapz = this.getWarpMap(map);
        if (map == 109060000 || map == 109060002 || map == 109060004) {
            portal = 0;
        }

        if (map == this.c.getPlayer().getMapId()) {
            new Point(this.c.getPlayer().getMap().getPortal(portal).getPosition());
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        } else {
            this.c.getPlayer().changeMap(mapz, mapz.getPortal(portal));
        }

    }
    public void 团队传送地图(final int mapId, final int portal) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            if (portal < 0) {
                this.warp(mapId);
            }
            else {
                this.warp(mapId, portal);
            }
            return;
        }

        final boolean rand = portal < 0;
        final MapleMap target = this.getMap(mapId);
        final int cMap = this.getPlayer().getMapId();
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
            if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == this.getPlayer().getEventInstance())) {
                if (rand) {
                    try {
                        curChar.changeMap(target, target.getPortal(Randomizer.nextInt(target.getPortals().size())));
                    }
                    catch (Exception e) {
                        curChar.changeMap(target, target.getPortal(0));
                    }
                }
                else {
                    curChar.changeMap(target, target.getPortal(portal));
                }
            }
        }
    }

    public void 给金币(final int gain) {
        this.c.getPlayer().gainMeso(gain, true, false, true);
    }

    public void 收金币(final int gain) {
        this.c.getPlayer().gainMeso(-gain, true, false, true);
    }

    public void 给经验(final int gain) {
        this.c.getPlayer().gainExp(gain, true, true, true);
    }

    public void 收经验(final int gain) {
        this.c.getPlayer().gainExp(-gain, true, true, true);
    }

    public void 给团队道具(final int id, final short quantity) {
        this.givePartyItems(id, quantity, false);
    }

    public void 收团队道具(final int id, final short quantity) {
        this.givePartyItems2(id, quantity, false);
    }

    public void givePartyItems2(final int id, final short quantity, final boolean removeAll) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainItem(id, (short)(removeAll ? (-this.getPlayer().itemQuantity(id)) : (-quantity)));
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                this.gainItem(id, (short)(removeAll ? (-curChar.itemQuantity(id)) : (-quantity)), false, 0L, 0, "", curChar.getClient());
            }
        }
    }

    public void 给团队经验(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainExp(amount);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.gainExp(amount, true, true, true);
            }
        }
    }

    public void 给团队点券(final int amount, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.modifyCSPoints(1, amount, true);
        }
    }

    public void 给团队抵用券(final int amount, final List<MapleCharacter> party) {
        for (MapleCharacter chr : party) {
            chr.modifyCSPoints(2, amount, true);
        }
    }

    public void givePartyDY(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainDY(amount);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.modifyCSPoints(2, amount, true);
            }
        }
    }

    public void givePartyMeso(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainMeso(amount);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.gainMeso(amount, true);
            }
        }
    }

    public void 给团队金币(final int amount) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.gainMeso(amount);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.gainMeso(amount, true);
            }
        }
    }

    public void 销毁物品(final int id) {
        this.c.getPlayer().removeAll(id);
    }

    public void 打开NPC(final int id, final int wh) {
        this.openNpc(this.getClient(), id, wh, (String)null);
    }

    public final int 判断地图() {
        return this.c.getPlayer().getMap().getId();
    }

    public final int 判断地图指定怪物数量(final int mobid) {
        int a = 0;
        for (final MapleMapObject obj : this.c.getPlayer().getMap().getAllMonstersThreadsafe()) {
            final MapleMonster mob = (MapleMonster)obj;
            if (mob.getId() == mobid) {
                ++a;
            }
        }
        return a;
    }

    public final boolean 判断当前地图指定怪物是否存在(final int mobid) {
        for (final MapleMapObject obj : this.c.getPlayer().getMap().getAllMonstersThreadsafe()) {
            final MapleMonster mob = (MapleMonster)obj;
            if (mob.getId() == mobid) {
                return true;
            }
        }
        return false;
    }

    public int 判断技能等级(final int id) {
        return this.getPlayer().getSkillLevel(id);
    }

    public void 给予技能(final int id, final byte level, final byte masterlevel) {
        this.getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }

    public void 给家族GP点(final int gp) {
        if (this.getPlayer().getGuildId() <= 0) {
            return;
        }
        Guild.gainGP(this.getPlayer().getGuildId(), gp);
    }

    public int 判断家族GP点() {
        if (this.getPlayer().getGuildId() <= 0) {
            return 0;
        }
        return Guild.getGP(this.getPlayer().getGuildId());
    }

    public final void givePartyBossLog(String bossid) {
        if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
            Iterator var2 = this.getPlayer().getParty().getMembers().iterator();

            while(var2.hasNext()) {
                MaplePartyCharacter chr = (MaplePartyCharacter)var2.next();
                MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
                if (curChar != null) {
                    curChar.setBossLog(bossid);
                }
            }

        } else {
            this.setBossLog(bossid);
        }
    }
    public void 给团队每日(final String bossid) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.setBossLog(bossid);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.setBossLog(bossid);
            }
        }
    }
    public void 给团队每日1(final String bossid) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.setBossLog1(bossid);
            return;
        }
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.setBossLog1(bossid);
            }
        }
    }
    public final void 给团队每日a(String bossid) {
        if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
            Iterator var2 = this.getPlayer().getParty().getMembers().iterator();

            while(var2.hasNext()) {
                MaplePartyCharacter chr = (MaplePartyCharacter)var2.next();
                MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
                if (curChar != null) {
                    curChar.setBossLoga(bossid);
                }
            }

        } else {
            this.setBossLoga(bossid);
        }
    }
    public void setBossLoga(String bossid) {
        this.getPlayer().setBossLoga(bossid);
    }

    public void setBossLoga(String bossid, int count) {
        this.getPlayer().setBossLoga(bossid, count);
    }

    public boolean 判断团队金币( Integer meso) {
        boolean a = false;
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                if (curChar.getMeso()<meso){
                    a = true;
                }
            }
        }
        return a;
    }

    public boolean 判断团队道具( int id, int quantity) {
        boolean a = false;
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                if (!curChar.haveItem(id,quantity)){
                    a = true;
                }
            }
        }
        return a;
    }
    public int 判断团队每日(final String bossid) {
        int a = 0;
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                a += curChar.getBossLog(bossid);
            }
        }
        return a;
    }

    public int 判断队友是否在场(final String bossid) {
        int a = 0;
        for (final MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                a += curChar.getBossLog(bossid);
            }
        }
        return a;
    }

    public int 判断星期() {
        return Calendar.getInstance().get(7);
    }

    public int 获取当前星期() {
        return Calendar.getInstance().get(7);
    }

    public List<BossRankInfo> getBossRankPointsTop(final String bossname) {
        return BossRankManager.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo1> getBossRankPointsTop1(final String bossname) {
        return BossRankManager1.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo2> getBossRankPointsTop2(final String bossname) {
        return BossRankManager2.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo3> getBossRankPointsTop3(final String bossname) {
        return BossRankManager3.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo4> getBossRankPointsTop4(final String bossname) {
        return BossRankManager4.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo5> getBossRankPointsTop5(final String bossname) {
        return BossRankManager5.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo6> getBossRankPointsTop6(final String bossname) {
        return BossRankManager6.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo7> getBossRankPointsTop7(final String bossname) {
        return BossRankManager7.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo8> getBossRankPointsTop8(final String bossname) {
        return BossRankManager8.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo9> getBossRankPointsTop9(final String bossname) {
        return BossRankManager9.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo10> getBossRankPointsTop10(final String bossname) {
        return BossRankManager10.getInstance().getRank(bossname, 1);
    }

    public List<BossRankInfo> getBossRankCountTop(final String bossname) {
        return BossRankManager.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo1> getBossRankCountTop1(final String bossname) {
        return BossRankManager1.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo2> getBossRankCountTop2(final String bossname) {
        return BossRankManager2.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo3> getBossRankCountTop3(final String bossname) {
        return BossRankManager3.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo4> getBossRankCountTop4(final String bossname) {
        return BossRankManager4.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo5> getBossRankCountTop5(final String bossname) {
        return BossRankManager5.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo6> getBossRankCountTop6(final String bossname) {
        return BossRankManager6.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo7> getBossRankCountTop7(final String bossname) {
        return BossRankManager7.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo8> getBossRankCountTop8(final String bossname) {
        return BossRankManager8.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo9> getBossRankCountTop9(final String bossname) {
        return BossRankManager9.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo10> getBossRankCountTop10(final String bossname) {
        return BossRankManager10.getInstance().getRank(bossname, 2);
    }

    public List<BossRankInfo> getBossRankTop(final String bossname, final byte type) {
        return BossRankManager.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo1> getBossRankTop1(final String bossname, final byte type) {
        return BossRankManager1.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo2> getBossRankTop2(final String bossname, final byte type) {
        return BossRankManager2.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo3> getBossRankTop3(final String bossname, final byte type) {
        return BossRankManager3.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo4> getBossRankTop4(final String bossname, final byte type) {
        return BossRankManager4.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo5> getBossRankTop5(final String bossname, final byte type) {
        return BossRankManager5.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo6> getBossRankTop6(final String bossname, final byte type) {
        return BossRankManager6.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo7> getBossRankTop7(final String bossname, final byte type) {
        return BossRankManager7.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo8> getBossRankTop8(final String bossname, final byte type) {
        return BossRankManager8.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo9> getBossRankTop9(final String bossname, final byte type) {
        return BossRankManager9.getInstance().getRank(bossname, (int)type);
    }

    public List<BossRankInfo10> getBossRankTop10(final String bossname, final byte type) {
        return BossRankManager10.getInstance().getRank(bossname, (int)type);
    }

    public int setBossRankPoints(final String bossname) {
        return this.setBossRank(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints1(final String bossname) {
        return this.setBossRank1(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints2(final String bossname) {
        return this.setBossRank2(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints3(final String bossname) {
        return this.setBossRank3(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints4(final String bossname) {
        return this.setBossRank4(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints5(final String bossname) {
        return this.setBossRank5(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints6(final String bossname) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints7(final String bossname) {
        return this.setBossRank7(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints8(final String bossname) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints9(final String bossname) {
        return this.setBossRank9(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankPoints10(final String bossname) {
        return this.setBossRank10(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, 1);
    }

    public int setBossRankCount(final String bossname) {
        return this.setBossRank(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount1(final String bossname) {
        return this.setBossRank1(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount2(final String bossname) {
        return this.setBossRank2(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount3(final String bossname) {
        return this.setBossRank3(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount4(final String bossname) {
        return this.setBossRank4(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount5(final String bossname) {
        return this.setBossRank5(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount6(final String bossname) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount7(final String bossname) {
        return this.setBossRank7(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount8(final String bossname) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount9(final String bossname) {
        return this.setBossRank9(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankCount10(final String bossname) {
        return this.setBossRank10(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, 1);
    }

    public int setBossRankPoints(final String bossname, final int add) {
        return this.setBossRank(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints1(final String bossname, final int add) {
        return this.setBossRank1(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints2(final String bossname, final int add) {
        return this.setBossRank2(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints3(final String bossname, final int add) {
        return this.setBossRank3(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints4(final String bossname, final int add) {
        return this.setBossRank4(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints5(final String bossname, final int add) {
        return this.setBossRank5(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints6(final String bossname, final int add) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints7(final String bossname, final int add) {
        return this.setBossRank7(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints8(final String bossname, final int add) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints9(final String bossname, final int add) {
        return this.setBossRank9(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankPoints10(final String bossname, final int add) {
        return this.setBossRank10(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)1, add);
    }

    public int setBossRankCount(final String bossname, final int add) {
        return this.setBossRank(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount1(final String bossname, final int add) {
        return this.setBossRank1(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount2(final String bossname, final int add) {
        return this.setBossRank2(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount3(final String bossname, final int add) {
        return this.setBossRank3(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount4(final String bossname, final int add) {
        return this.setBossRank4(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount5(final String bossname, final int add) {
        return this.setBossRank5(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount6(final String bossname, final int add) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount7(final String bossname, final int add) {
        return this.setBossRank7(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount8(final String bossname, final int add) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount9(final String bossname, final int add) {
        return this.setBossRank9(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int setBossRankCount10(final String bossname, final int add) {
        return this.setBossRank10(this.getPlayer().getId(), this.getPlayer().getName(), bossname, (byte)2, add);
    }

    public int 任务(final int sj, final int add) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), "赛季积分", (byte)sj, add);
    }

    public int 给赛季积分(final int sj, final int add) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), "赛季积分", (byte)sj, add);
    }

    public int 给SSP点(final int add) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), "超级技能点", (byte)2, add);
    }

    public int 收SSP点(final int add) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), "超级技能点", (byte)2, -add);
    }

    public int 给炼金经验(final int add) {
        return this.setBossRank5(this.getPlayer().getId(), this.getPlayer().getName(), "炼金经验", (byte)2, add);
    }

    public int 给唠叨经验(final int add) {
        return this.setBossRank4(this.getPlayer().getId(), this.getPlayer().getName(), "唠叨经验", (byte)2, add);
    }

    public int 给泡点经验(final int add) {
        return this.setBossRank3(this.getPlayer().getId(), this.getPlayer().getName(), "泡点经验", (byte)2, add);
    }

    public int 给挖矿经验(final int add) {
        return this.setBossRank2(this.getPlayer().getId(), this.getPlayer().getName(), "挖矿经验", (byte)2, add);
    }

    public int 给钓鱼经验(final int add) {
        return this.setBossRank1(this.getPlayer().getId(), this.getPlayer().getName(), "钓鱼经验", (byte)2, add);
    }

    public int setBossRank(final String bossname, final byte type, final int add) {
        return this.setBossRank(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank1(final String bossname, final byte type, final int add) {
        return this.setBossRank1(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank2(final String bossname, final byte type, final int add) {
        return this.setBossRank2(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank3(final String bossname, final byte type, final int add) {
        return this.setBossRank3(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank4(final String bossname, final byte type, final int add) {
        return this.setBossRank4(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank5(final String bossname, final byte type, final int add) {
        return this.setBossRank5(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank6(final String bossname, final byte type, final int add) {
        return this.setBossRank6(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank7(final String bossname, final byte type, final int add) {
        return this.setBossRank7(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank8(final String bossname, final byte type, final int add) {
        return this.setBossRank8(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank9(final String bossname, final byte type, final int add) {
        return this.setBossRank9(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank10(final String bossname, final byte type, final int add) {
        return this.setBossRank10(this.getPlayer().getId(), this.getPlayer().getName(), bossname, type, add);
    }

    public int setBossRank(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank1(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager1.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank2(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager2.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank3(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager3.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank4(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager4.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank5(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager5.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank6(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager6.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank7(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager7.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank8(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager8.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank9(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager9.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int setBossRank10(final int cid, final String cname, final String bossname, final byte type, final int add) {
        return BossRankManager10.getInstance().setLog(cid, cname, bossname, type, add);
    }

    public int getBossRankPoints(final String bossname) {
        return this.getBossRank(bossname, (byte)1);
    }

    public int getBossRankPoints1(final String bossname) {
        return this.getBossRank1(bossname, (byte)1);
    }

    public int getBossRankPoints2(final String bossname) {
        return this.getBossRank2(bossname, (byte)1);
    }

    public int getBossRankPoints3(final String bossname) {
        return this.getBossRank3(bossname, (byte)1);
    }

    public int getBossRankPoints4(final String bossname) {
        return this.getBossRank4(bossname, (byte)1);
    }

    public int getBossRankPoints5(final String bossname) {
        return this.getBossRank5(bossname, (byte)1);
    }

    public int getBossRankPoints6(final String bossname) {
        return this.getBossRank6(bossname, (byte)1);
    }

    public int getBossRankPoints7(final String bossname) {
        return this.getBossRank7(bossname, (byte)1);
    }

    public int getBossRankPoints8(final String bossname) {
        return this.getBossRank8(bossname, (byte)1);
    }

    public int getBossRankPoints9(final String bossname) {
        return this.getBossRank9(bossname, (byte)1);
    }

    public int getBossRankPoints10(final String bossname) {
        return this.getBossRank10(bossname, (byte)1);
    }

    public int getBossRankCount(final String bossname) {
        return this.getBossRank(bossname, (byte)2);
    }

    public int getBossRankCount1(final String bossname) {
        return this.getBossRank1(bossname, (byte)2);
    }

    public int getBossRankCount2(final String bossname) {
        return this.getBossRank2(bossname, (byte)2);
    }

    public int getBossRankCount3(final String bossname) {
        return this.getBossRank3(bossname, (byte)2);
    }

    public int getBossRankCount4(final String bossname) {
        return this.getBossRank4(bossname, (byte)2);
    }

    public int getBossRankCount5(final String bossname) {
        return this.getBossRank5(bossname, (byte)2);
    }

    public int getBossRankCount6(final String bossname) {
        return this.getBossRank6(bossname, (byte)2);
    }

    public int getBossRankCount7(final String bossname) {
        return this.getBossRank7(bossname, (byte)2);
    }

    public int getBossRankCount8(final String bossname) {
        return this.getBossRank8(bossname, (byte)2);
    }

    public int getBossRankCount9(final String bossname) {
        return this.getBossRank9(bossname, (byte)2);
    }

    public int getBossRankCount10(final String bossname) {
        return this.getBossRank10(bossname, (byte)2);
    }

    public int getBossRank(final String bossname, final byte type) {
        return this.getBossRank(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank1(final String bossname, final byte type) {
        return this.getBossRank1(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank2(final String bossname, final byte type) {
        return this.getBossRank2(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank3(final String bossname, final byte type) {
        return this.getBossRank3(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank4(final String bossname, final byte type) {
        return this.getBossRank4(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank5(final String bossname, final byte type) {
        return this.getBossRank5(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank6(final String bossname, final byte type) {
        return this.getBossRank6(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank7(final String bossname, final byte type) {
        return this.getBossRank7(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank8(final String bossname, final byte type) {
        return this.getBossRank8(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank9(final String bossname, final byte type) {
        return this.getBossRank9(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank10(final String bossname, final byte type) {
        return this.getBossRank10(this.getPlayer().getId(), bossname, type);
    }

    public int getBossRank(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo info = BossRankManager.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank1(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo1 info = BossRankManager1.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank2(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo2 info = BossRankManager2.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank3(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo3 info = BossRankManager3.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank4(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo4 info = BossRankManager4.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank5(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo5 info = BossRankManager5.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank6(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo6 info = BossRankManager6.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank7(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo7 info = BossRankManager7.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank8(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo8 info = BossRankManager8.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank9(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo9 info = BossRankManager9.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public int getBossRank10(final int cid, final String bossname, final byte type) {
        int ret = -1;
        final BossRankInfo10 info = BossRankManager10.getInstance().getInfo(cid, bossname);
        if (null == info) {
            return ret;
        }
        switch (type) {
            case 1: {
                ret = info.getPoints();
                break;
            }
            case 2: {
                ret = info.getCount();
                break;
            }
        }
        return ret;
    }

    public List<IItem> getItemsByType(final byte type) {
        final List<IItem> items = new ArrayList<IItem>();
        final MapleInventoryType itemtype = MapleInventoryType.getByType(type);
        final MapleInventory mi = this.getPlayer().getInventory(itemtype);
        if (mi != null) {
            for (final IItem item : mi.list()) {
                items.add(item);
            }
        }
        return items;
    }

    public List<IItem> getItemsByType1(final byte type) {
        final List<IItem> items = new ArrayList<IItem>();
        final MapleInventoryType itemtype = MapleInventoryType.getByType(type);
        final MapleInventory mi = this.getPlayer().getInventory(itemtype);
        if (mi != null) {
            for (final IItem item : mi.list()) {
                items.add(item);
            }
        }
        return items;
    }

    public List<IItem> getItemsByType2(final byte type) {
        final List<IItem> items = new ArrayList<IItem>();
        final MapleInventoryType itemtype = MapleInventoryType.getByType(type);
        final MapleInventory mi = this.getPlayer().getInventory(itemtype);
        if (mi != null) {
            for (final IItem item : mi.list()) {
                items.add(item);
            }
        }
        return items;
    }

    public int saveBankItem(final IItem item, final short count) {
        return BankItemManager.getInstance().saveItem(this.getPlayer(), item, count);
    }

    public int saveBankItem1(final IItem item, final short count) {
        return BankItemManager1.getInstance().saveItem(this.getPlayer(), item, count);
    }

    public int saveBankItem2(final IItem item, final short count, final short type) {
        return BankItemManager2.getInstance().saveItem(this.getPlayer(), item, count,type);
    }

    public List<BankItem> getBankItems() {
        return BankItemManager.getInstance().getItems(this.getPlayer().getId());
    }

    public List<BankItem1> getBankItems1() {
        return BankItemManager1.getInstance().getItems(this.getPlayer().getguildid());
    }

    public List<BankItem2> getBankItems2() {
        return BankItemManager2.getInstance().getItems(this.getPlayer().getId());
    }

    public int GetPiot(final String Name, final int Channale) {
        int ret = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM FullPoint WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            final ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {}
        return ret;
    }
    public void GainPiot(String Name, int Channale, int Piot) {
        int ret = this.GetPiot(Name, Channale);
        if (ret == -1) {
            ret = 0;
            PreparedStatement ps = null;

            try {
                Connection con = DBConPool.getConnection();
                Throwable var7 = null;

                try {
                    ps = con.prepareStatement("INSERT INTO FullPoint (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setString(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                } catch (Throwable var57) {
                    var7 = var57;
                    throw var57;
                } finally {
                    if (con != null) {
                        if (var7 != null) {
                            try {
                                con.close();
                            } catch (Throwable var56) {
                                var7.addSuppressed(var56);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var61) {
                //服务端输出信息.println_out("xxxxxxxx:" + var61);
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                } catch (SQLException var53) {
                    //服务端输出信息.println_out("xxxxxxxxzzzzzzz:" + var53);
                }

            }
        }

        ret += Piot;

        try {
            Connection con1 = DBConPool.getConnection();
            Throwable var64 = null;

            try {
                PreparedStatement ps = con1.prepareStatement("UPDATE FullPoint SET `Point` = ? WHERE Name = ? and channel = ?");
                ps.setInt(1, ret);
                ps.setString(2, Name);
                ps.setInt(3, Channale);
                ps.execute();
                ps.close();
            } catch (Throwable var55) {
                var64 = var55;
                throw var55;
            } finally {
                if (con1 != null) {
                    if (var64 != null) {
                        try {
                            con1.close();
                        } catch (Throwable var54) {
                            var64.addSuppressed(var54);
                        }
                    } else {
                        con1.close();
                    }
                }

            }
        } catch (SQLException var59) {
            //服务端输出信息.println_err("获取错误!!55" + var59);
        }

    }
    public int Getsaiji(final String Name, final int Channale) {
        int ret = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM saiji WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            final ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {}
        return ret;
    }

    public void Gainsaiji(final String Name, final int Channale, final int saiji) {
        try {
            int ret = this.Getsaiji(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;
                try {
                    ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO saiji (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setString(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                }
                catch (SQLException e) {
                    System.out.println("xxxxxxxx:" + (Object)e);
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    }
                    catch (SQLException e2) {
                        System.out.println("xxxxxxxxzzzzzzz:" + (Object)e2);
                    }
                }
                finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    }
                    catch (SQLException e2) {
                        System.out.println("xxxxxxxxzzzzzzz:" + (Object)e2);
                    }
                }
            }
            ret += saiji;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps2 = con.prepareStatement("UPDATE saiji SET `Point` = ? WHERE Name = ? and channel = ?");
            ps2.setInt(1, ret);
            ps2.setString(2, Name);
            ps2.setInt(3, Channale);
            ps2.execute();
            ps2.close();
            con.close();
        }
        catch (SQLException sql) {
            System.err.println("獲取錯誤!!55" + (Object)sql);
        }
    }

    public List<ArrayList> getSevenDayPayLog(final int day) {
        final List<Integer> ret = new ArrayList();
        for (int i = 0; i < day; ++i) {
            ret.add(Integer.valueOf(0));
        }
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM paylog WHERE account = ?");
            ps.setString(1, this.c.getAccountName());
            final ResultSet rs = ps.executeQuery();
            final Timestamp currtime = new Timestamp(System.currentTimeMillis());
            while (rs.next()) {
                final int rmb = rs.getInt("rmb");
                final Timestamp time = rs.getTimestamp("paytime");
                final int diffday = (int)((currtime.getTime() - time.getTime()) / 86400000L);
                if (diffday < day) {
                    ret.set(diffday, Integer.valueOf((int)Integer.valueOf(ret.get(diffday)) + rmb));
                }
            }
            ps.close();
            rs.close();
            con.close();
        }
        catch (SQLException e) {
            System.err.println("获取充值记录失败" + (Object)e);
        }
        return (List)ret;
    }

    public int 取破攻等级() {
        int 破功等级 = 0;
        try {
            final int cid = this.getPlayer().getId();
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM characters WHERE id=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                破功等级 = rs.getInt("PGSXDJ");
            }
            limitCheck.close();
            rs.close();
            con.close();
        }
        catch (SQLException ex) {}
        return 破功等级;
    }

    public void 给破攻等级(final int 等级) {
        try {
            final int cid = this.getPlayer().getId();
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE characters SET PGSXDJ =PGSXDJ+ " + 等级 + " WHERE id = " + cid + "");
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {}
    }

    public void 给破攻等级1(final int 等级) {
        this.c.getPlayer().gainPGSXDJ(等级);
    }

    public int getRMB() {
        return this.getPlayer().getRMB();
    }

    public void setRMB(final int rmb) {
        this.getPlayer().setRMB(rmb);
    }

    public int getjifen() {
        return this.getPlayer().getjifen();
    }

    public void setjifen(final int jifen) {
        this.getPlayer().setjifen(jifen);
    }

    public void gainRMB(final int rmb) {
        this.getPlayer().gainRMB(rmb);
    }

    public int getTotalRMB() {
        return this.getPlayer().getTotalRMB();
    }

    public MapleItemInformationProvider getItemInfo() {
        return MapleItemInformationProvider.getInstance();
    }

    public int getDaysPQLog(final String pqName, final int days) {
        return this.getPlayer().getDaysPQLog(pqName, 0, days);
    }

    public int getPQLog(final String pqName) {
        return this.getPlayer().getPQLog(pqName);
    }

    public int getPQLog(final String pqName, final int type) {
        return this.getPlayer().getPQLog(pqName, type);
    }

    public int getPQLog(final String pqName, final int type, final int days) {
        return this.getPlayer().getDaysPQLog(pqName, type, days);
    }

    public void setPQLog(final String pqName) {
        this.getPlayer().setPQLog(pqName);
    }

    public void setPQLog(final String pqName, final int type) {
        this.getPlayer().setPQLog(pqName, type);
    }

    public void setPQLog(final String pqName, final int type, final int count) {
        this.getPlayer().setPQLog(pqName, type, count);
    }

    public void resetPQLog(final String pqName) {
        this.getPlayer().resetPQLog(pqName);
    }

    public void resetPQLog(final String pqName, final int type) {
        this.getPlayer().resetPQLog(pqName, type);
    }

    public void setPartyPQLog(final String pqName) {
        this.setPartyPQLog(pqName, 0);
    }

    public void setPartyPQLog(final String pqName, final int type) {
        this.setPartyPQLog(pqName, type, 1);
    }

    public void setPartyPQLog(final String pqName, final int type, final int count) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.getPlayer().setPQLog(pqName, type, count);
            return;
        }
        final int n4 = this.getPlayer().getMapId();
        for (final MaplePartyCharacter partyCharacter : this.getPlayer().getParty().getMembers()) {
            final MapleCharacter player = this.getPlayer().getMap().getCharacterById(partyCharacter.getId());
            if (player != null) {
                if (player.getMapId() != n4) {
                    continue;
                }
                player.setPQLog(pqName, type, count);
            }
        }
    }

    public int getEventCount(final String eventId) {
        return this.c.getPlayer().getEventCount(eventId);
    }

    public int getEventCount(final String eventId, final int type) {
        return this.c.getPlayer().getEventCount(eventId, type);
    }

    public void setEventCount(final String eventId) {
        this.c.getPlayer().setEventCount(eventId);
    }

    public void setEventCount(final String eventId, final int type) {
        this.c.getPlayer().setEventCount(eventId, type);
    }

    public void setEventCount(final String eventId, final int type, final int count) {
        this.c.getPlayer().setEventCount(eventId, type, count);
    }

    public void resetEventCount(final String eventId) {
        this.c.getPlayer().resetEventCount(eventId);
    }

    public void resetEventCount(final String eventId, final int type) {
        this.c.getPlayer().resetEventCount(eventId, type);
    }

    public void setPartyEventCount(final String eventId) {
        this.setPartyEventCount(eventId, 0);
    }

    public void setPartyEventCount(final String eventId, final int type) {
        this.setPartyEventCount(eventId, type, 1);
    }

    public void setPartyEventCount(final String eventId, final int type, final int count) {
        if (this.getPlayer().getParty() == null || this.getPlayer().getParty().getMembers().size() == 1) {
            this.c.getPlayer().setEventCount(eventId, type, count);
            return;
        }
        final int checkMap = this.getPlayer().getMapId();
        for (final MaplePartyCharacter partyPlayer : this.getPlayer().getParty().getMembers()) {
            MapleCharacter chr = this.getPlayer().getMap().getCharacterById(partyPlayer.getId());
            if (chr != null && chr.getMapId() == checkMap) {
                chr.setEventCount(eventId, type, count);
            }
        }
    }

    public boolean checkPartyEventCount(final String eventId) {
        return this.checkPartyEventCount(eventId, 1);
    }

    public boolean checkPartyEventCount(final String eventId, final int checkcount) {
        final MapleParty party = this.c.getPlayer().getParty();
        if (party == null || party.getMembers().size() == 1) {
            final int count = this.getEventCount(eventId);
            return count >= 0 && count < checkcount;
        }
        int check = 0;
        final int partySize = party.getMembers().size();
        for (final MaplePartyCharacter partyPlayer : party.getMembers()) {
            MapleCharacter chr = this.getPlayer().getMap().getCharacterById(partyPlayer.getId());
            if (chr != null) {
                final int count = chr.getEventCount(eventId);
                if (count < 0 || count >= checkcount) {
                    continue;
                }
                ++check;
            }
        }
        return partySize == check;
    }

    public int getmoneyb() {
        int moneyb = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            final int cid = this.getPlayer().getAccountID();
            ResultSet rs;
            try (final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM accounts WHERE id=" + cid + "")) {
                rs = limitCheck.executeQuery();
                if (rs.next()) {
                    moneyb = rs.getInt("moneyb");
                }
            }
            rs.close();
        }
        catch (SQLException ex) {
            System.err.println("getmoneyb" + (Object)ex);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable)ex);
            ex.getStackTrace();
        }
        return moneyb;
    }

    public void setmoneyb(final int slot) {
        try (Connection con = DatabaseConnection.getConnection()) {
            final int cid = this.getPlayer().getAccountID();
            try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET moneyb =moneyb+ " + slot + " WHERE id = " + cid + "")) {
                ps.executeUpdate();
            }
        }
        catch (SQLException ex) {
            System.err.println("setmoneyb" + (Object)ex);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable)ex);
            ex.getStackTrace();
        }
    }

    public void setmoneybF(final int slot) {
        if (slot > 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            this.getPlayer().ban("刷元宝", true, true, false);
        }else {
            try (Connection con = DatabaseConnection.getConnection()) {
                final int cid = this.getPlayer().getAccountID();
                try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET moneyb =moneyb+ " + slot + " WHERE id = " + cid + "")) {
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                System.err.println("setmoneyb" + (Object) ex);
                FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable) ex);
                ex.getStackTrace();
            }
        }
    }
    public void setmoneybZ(final int slot) {
        if (slot < 0){
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + getPlayer().getName() + " 因为刷元宝而被管理员永久停封。"));
            this.getPlayer().ban("刷元宝", true, true, false);
        }else {
            try (Connection con = DatabaseConnection.getConnection()) {
                final int cid = this.getPlayer().getAccountID();
                try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET moneyb =moneyb+ " + slot + " WHERE id = " + cid + "")) {
                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                System.err.println("setmoneyb" + (Object) ex);
                FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable) ex);
                ex.getStackTrace();
            }
        }
    }

    public int getjbjf() {
        int jbjf = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            final int cid = this.getPlayer().getAccountID();
            ResultSet rs;
            try (final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM accounts WHERE id=" + cid + "")) {
                rs = limitCheck.executeQuery();
                if (rs.next()) {
                    jbjf = rs.getInt("jbjf");
                }
            }
            rs.close();
        }
        catch (SQLException ex) {
            System.err.println("getjbjf" + (Object)ex);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable)ex);
            ex.getStackTrace();
        }
        return jbjf;
    }

    public void setjbjf(final int zhi) {
        try (Connection con = DatabaseConnection.getConnection()) {
            final int cid = this.getPlayer().getAccountID();
            try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET jbjf =jbjf+ " + zhi + " WHERE id = " + cid + "")) {
                ps.executeUpdate();
            }
        }
        catch (SQLException ex) {
            System.err.println("setjbjf" + (Object)ex);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable)ex);
            ex.getStackTrace();
        }
    }

    public void openWeb(final String web) {
        this.c.getSession().write((Object)MaplePacketCreator.openWeb(web));
    }

    public final boolean checkNumSpace(final int type, final int space) {
        if (type <= 5 && type > 0) {
            return this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)type)).getNumFreeSlot() >= space;
        }
        for (int i = 1; i <= 5; ++i) {
            if (this.c.getPlayer().getInventory(MapleInventoryType.getByType((byte)i)).getNumFreeSlot() < space) {
                return false;
            }
        }
        return true;
    }

    public void refreshAllStats() {
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        this.c.getPlayer().getStat().recalcLocalStats();
        this.c.getSession().write((Object)MaplePacketCreator.updatePlayerStats(statup, this.c.getPlayer()));
    }

    public void refreshMaplePoints() {
        this.c.getPlayer().getStat().recalcLocalStats();
        this.c.sendPacket(MaplePacketCreator.showCharCash(this.c.getPlayer()));
    }

    public int getCombat() {
        return this.getPlayer().getCombat();
    }
    public boolean isAllPartyMembersCombat(int a) {
        if (this.getParty() != null) {
            for (MaplePartyCharacter partyCharacter : this.getParty().getMembers()) {
                MapleCharacter player = this.getChannelServer().getPlayerStorage().getCharacterById(partyCharacter.getId());
                if (player != null && player.getCombat() < a) {
                    return false;
                }
            }
        }
        return true;
    }
    public MaplePartyCharacter getNotAllMembersCombatName(int a) {
        if (this.getParty() == null) {
            return null;
        }
        for (MaplePartyCharacter partyCharacter : this.getParty().getMembers()) {
            MapleCharacter player = this.getChannelServer().getPlayerStorage().getCharacterById(partyCharacter.getId());
            if (player != null && player.getCombat() < a) {
                return partyCharacter;
            }
        }
        return null;
    }
    public String getNotAllPartyMembersCombatName(int a) {
        if (this.getNotAllMembersCombatName(a) != null) {
            return this.getNotAllMembersCombatName(a).getName();
        }
        return null;
    }

    //重置潜能
    public int UseCube(Item equip ,int flag) {


        boolean fail = false;
        int moba = 100;
        if (flag == 1) {

                final Item item = (Item) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) equip.getPosition());
                if (item != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    final Equip eq = (Equip) item;

                        //gainItemF(5062000, (short) -1);
                        eq.renewPotential();
                        //c.getSession().write(MaplePacketCreator.scrolledItem(scroll, item, false, true));
//                        c.getPlayer().marriage();
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        MapleInventoryManipulator.addById(c, 2430112, (short) 1, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());

                            FileoutputUtil.logToFile("logs/Data/使用方块.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSessionIPAddress() + " 账号: " + c.getAccountName() + " 玩家: " + c.getPlayer().getName() + " 使用了魔方道具: 5062000");

                            World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 聊天]『" + c.getPlayer().getName() + "』(" + c.getPlayer().getId() + ")地图『" + c.getPlayer().getMapId() + "』使用了魔方道具: 5062000"));
//
                        return 0;

                } else {
                    c.getPlayer().dropMessage(5, "请检查你的背包是否已满。");
                    fail = true;
                    moba = 5;
                }
               // c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getPotentialReset( c.getPlayer().getId(), scroll.getPosition()));
               // c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getPotentialReset(fail, c.getPlayer().getId(), scroll.getItemId()));
                return moba;


        } else if (flag ==2) {
            if (c.getPlayer().getLevel() < 70) {
                //c.getPlayer().dropMessage(1, "You may not use this until level 70.");
                return 12;
            } else {
                final Item item = (Item) c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((byte) equip.getPosition());
                if (item != null && c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() >= 1) {
                    final Equip eq = (Equip) item;

                        if (eq.getPotential3() <= 0){
                            eq.resetPotential();
                        }else{
                            eq.renewPotential();
                        }

                        //c.getSession().write(MaplePacketCreator.scrolledItem(scroll, item, false, true));
//                        c.getPlayer().marriage();
                        c.getPlayer().forceReAddItem_NoUpdate(item, MapleInventoryType.EQUIP);
                        MapleInventoryManipulator.addById(c, 2430112, (short) 5, "Cube" + " on " + FileoutputUtil.CurrentReadable_Date());

                            FileoutputUtil.logToFile("logs/Data/使用方块.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSessionIPAddress() + " 账号: " + c.getAccountName() + " 玩家: " + c.getPlayer().getName() + " 使用了魔方道具: 5062001");

                            World.Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 聊天]『" + c.getPlayer().getName() + "』(" + c.getPlayer().getId() + ")地图『" + c.getPlayer().getMapId() + "』使用了魔方道具: 5062001"));

                        return 0;

                } else {
                    c.getPlayer().dropMessage(5, "请检查你的背包是否已满。");
                    fail = true;
                    moba = 5;
                }
               // c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getPotentialReset( c.getPlayer().getId(), scroll.getPosition()));
                return moba;
            }
        }
        return 6;
    }
    //潜能鉴定
    public int UseMagnify(Item equip ) {
        c.getPlayer().updateTick(equip.getItemId());
        final IItem toReveal = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short)(byte)equip.getItemId());
        if ( toReveal == null) {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            return 0;
        }
        final Equip toScroll = (Equip)toReveal;

        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int reqLevel = ii.getReqLevel(toScroll.getItemId()) / 10;
        if (toScroll.getState() == 1 &&  reqLevel <= 12 ) {
            final List<List<StructPotentialItem>> pots = new LinkedList<List<StructPotentialItem>>((Collection<? extends List<StructPotentialItem>>)ii.getAllPotentialInfo().values());
            int new_state = Math.abs((int)toScroll.getPotential1());
            if (new_state > 7 || new_state < 5) {
                new_state = 5;
            }
            final int lines = (toScroll.getPotential2() != 0) ? 3 : 2;// 默认2条属性
            while (toScroll.getState() != new_state) {
                //31001 = 好用的轻功, 31002 = 好用的时空门, 31003 = 好用的火眼晶晶, 31004 = 好用的神圣之火, 41005 = 强化战斗命令, 41006 = 强化进阶祝福, 41007 = 强化极速领域
                for (int i = 0; i < lines; ++i) {//最小 2 条, 最大 3 条
                    for (boolean rewarded = false; !rewarded; rewarded = true) {
                        final StructPotentialItem pot = (StructPotentialItem)((List<StructPotentialItem>)pots.get(Randomizer.nextInt(pots.size()))).get(reqLevel);
                        if (pot != null && pot.reqLevel / 10 <= reqLevel && GameConstants.optionTypeFits(pot.optionType, toScroll.getItemId()) && GameConstants.potentialIDFits((int)pot.potentialID, new_state, i)) {
                            if (i == 0) {
                                toScroll.setPotential1(pot.potentialID);
                            }
                            else if (i == 1) {
                                toScroll.setPotential2(pot.potentialID);
                            }
                            else if (i == 2) {
                                toScroll.setPotential3(pot.potentialID);
                            }
                        }
                    }
                }
            }
            //c.sendPacket(MaplePacketCreator.modifyInventory(true, new ModifyInventory(2, magnify)));
            c.getPlayer().getMap().broadcastMessage(MaplePacketCreator.getPotentialReset(c.getPlayer().getId(), toScroll.getPosition()));
           // MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, magnify.getPosition(), (short)1, false);
            return 1;
        } else {
            c.sendPacket(MaplePacketCreator.getInventoryFull());
            return  0;
        }
    }


    public static void 雇佣写入(int Name, int Channale, int Piot) {
        try {
            int ret = 判断雇佣(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;

                try {
                    ps = DBConPool.getConnection().prepareStatement("INSERT INTO hirex (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setInt(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                } catch (SQLException var15) {
                    //服务端输出信息.println_out("雇佣写入1:" + var15);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var14) {
                        //服务端输出信息.println_out("雇佣写入2:" + var14);
                    }

                }
            }

            ret += Piot;
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE hirex SET `Point` = ? WHERE Name = ? and channel = ?");
            ps.setInt(1, ret);
            ps.setInt(2, Name);
            ps.setInt(3, Channale);
            ps.execute();
            ps.close();
        } catch (SQLException var17) {
            //服务端输出信息.println_err("雇佣写入3" + var17);
        }

    }

    public static int 判断雇佣(int Name, int Channale) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM hirex WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setInt(2, Name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
        } catch (SQLException var6) {
        }

        return ret;
    }

  public void 开启储备经验() {
      this.c.getPlayer().开启储备经验();
  }

  public void 关闭储备经验() {
      this.c.getPlayer().关闭储备经验();
  }

  public boolean 是否储备经验() {
      return this.c.getPlayer().是否储备经验;
  }

  public long 读取储备经验() {
      return this.c.getPlayer().读取经验储备();
  }

  public int 读取开店经验加成() {
      return this.c.getPlayer().读取开店经验加成();
  }

    public void 全服道具公告(String head, String message, Item item) {
        if (ServerConfig.version == 79) {
            this.全服道具公告(head, message, item, (byte)14);
        } else if (ServerConfig.version == 85) {
            this.全服道具公告(head, message, item, (byte)15);
        }

    }


    public final void 给团队积分(int mount) {
        if (this.getPlayer().getParty() != null && this.getPlayer().getParty().getMembers().size() != 1) {
            Iterator var2 = this.getPlayer().getParty().getMembers().iterator();

            while(var2.hasNext()) {
                MaplePartyCharacter chr = (MaplePartyCharacter)var2.next();
                MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
                if (curChar != null) {
                    if (mount > 0) {
                        this.增加积分(curChar, mount);
                    } else {
                        this.减少积分(curChar, mount);
                    }
                }
            }

        } else {
            if (mount > 0) {
                this.增加积分(this.getPlayer(), mount);
            } else {
                this.减少积分(this.getPlayer(), mount);
            }

        }
    }
    public boolean 读取变身无延迟() {
        return this.c.getPlayer().getSuperTransformation();
    }
    public void 变身无延迟(boolean active) {
        this.c.getPlayer().setSuperTransformation(active);
    }
    public final boolean 判断物品数量(int itemid, int quantity) {
        return this.haveItem(itemid, quantity, false, true);
    }

    public final long 判断物品数量(int itemid) {
        long a = this.c.getPlayer().判断物品数量(itemid);
        return a;
    }
    public boolean 增加积分(int mount) {
        return this.getPlayer().增加积分_数据库(mount);
    }

    public boolean 增加积分(MapleCharacter chr, int mount) {
        return chr.增加积分_数据库(mount);
    }

    public boolean 减少积分(int mount) {
        return this.getPlayer().减少积分_数据库(mount);
    }

    public boolean 减少积分(MapleCharacter chr, int mount) {
        return chr.减少积分_数据库(mount);
    }

    public void 全服道具公告(String head, String message, Item item, byte type) {
        if (item != null) {
            Broadcast.broadcastMessage(MaplePacketCreator.getGachaponMega(head, " : " + message, type, item, (byte)1, this.c.getChannel()));
        }

    }
    public int 获得积分() {
        Connection con = DBConPool.getConnection();
        int point = 0;

        try {
            PreparedStatement ps = con.prepareStatement("Select * FROM snail_boss_points WHERE accountid = ?");
            ps.setInt(1, this.getPlayer().getAccountID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                point = rs.getInt("points");
            }

            ps.close();
            rs.close();
        } catch (SQLException var5) {
            //服务端输出信息.println_err("获得积分，读取数据库错误，错误原因：" + var5);
            var5.printStackTrace();
        }

        return point;
    }
    public static byte[] 黄色喇叭(String text) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }


    public int getOneTimeLoga(String bossid) {
        return this.getPlayer().getOneTimeLoga(bossid);
    }

    public void setOneTimeLoga(String bossid) {
        this.getPlayer().setOneTimeLoga(bossid);
    }

    public void setOneTimeLoga(String bossid, int count) {
        this.getPlayer().setOneTimeLoga(bossid, count);
    }

    public void deleteOneTimeLoga(String bossid) {
        this.getPlayer().deleteOneTimeLoga(bossid);
    }


    public boolean deleteBossLog(String bossid) {
        return this.c.getPlayer().deleteBossLog(bossid);
    }

    public boolean deleteBossLog(String bossid, int count) {
        return this.c.getPlayer().deleteBossLog(bossid, count);
    }

    public int getBossLoga(String bossid) {
        return this.getPlayer().getBossLoga(bossid);
    }

    public boolean deleteBossLoga(String bossid) {
        return this.c.getPlayer().deleteBossLoga(bossid);
    }

    public boolean deleteBossLoga(String bossid, int count) {
        return this.c.getPlayer().deleteBossLoga(bossid, count);
    }

    public int 查询当日BOSS记录(String bossid) {
        return this.getPlayer().getBossLogC(bossid);
    }

    public final void 全服点歌(String songName) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cserv1 = (ChannelServer)var2.next();
            Iterator var4 = cserv1.getPlayerStorage().getAllCharacters().iterator();

            while(var4.hasNext()) {
                MapleCharacter mch = (MapleCharacter)var4.next();
                Broadcast.broadcastMessage(MaplePacketCreator.musicChange(songName));
            }
        }

    }

    public final void 个人点歌(String songName) {
        Broadcast.broadcastMessage(MaplePacketCreator.musicChange(songName));
    }

    public final String 判断玩家名字() {
        return this.c.getPlayer().getName();
    }

    public void 给家族GP点(int guildId, int gp) {
        if (this.getPlayer().getGuildId() > 0) {
            Guild.gainGP(guildId, gp);
        }
    }

    public int 判断家族GP点(int guildId) {
        return this.getPlayer().getGuildId() <= 0 ? 0 : Guild.getGP(guildId);
    }

    public int 判断每日值(String bossid) {
        return this.getPlayer().getBossLogD(bossid);
    }

    public int 判断每日(String bossid) {
        return this.getPlayer().getBossLogD(bossid);
    }

    public void 增加每日值(String bossid) {
        this.getPlayer().setBossLog(bossid);
    }

    public void 增加每日(String bossid) {
        this.getPlayer().setBossLog(bossid);
    }

    public void 给个人每日(String bossid) {
        this.getPlayer().setBossLog(bossid);
    }

    public int 判断团队每日(String bossid, int cou) {
        int a = 0;
        Iterator var5 = this.getPlayer().getParty().getMembers().iterator();

        while(var5.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var5.next();
            MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                int c = curChar.getBossLogD(bossid);
                if (c >= cou) {
                    a = 0;
                    return a;
                }

                a = 1;
            }
        }

        return a;
    }

    public int 判断团队每日a(String bossid, int cou) {
        int a = 0;
        Iterator var5 = this.getPlayer().getParty().getMembers().iterator();

        while(var5.hasNext()) {
            MaplePartyCharacter chr = (MaplePartyCharacter)var5.next();
            MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                int c = curChar.getBossLogDa(bossid);
                if (c >= cou) {
                    a = 0;
                    return a;
                }

                a = 1;
            }
        }

        return a;
    }

    public boolean 判断团队每日y(String bossid, int cou) {
        for (MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                int c = curChar.getBossLog1(bossid, 1);
                if (c >= cou) {
                    return false;
                }
            }
        }
        return true;
    }
    public boolean 给团队每日y(String bossid, int cou) {
        boolean falg = false;
        for (MaplePartyCharacter chr : this.getPlayer().getParty().getMembers()) {
            MapleCharacter curChar = this.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                falg = false;
                falg= curChar.setBossLog1y(bossid, 1,cou);
            }
        }
        return falg;
    }

    public final void 当前地图召唤怪物(int id, int qty, int x, int y) {
        this.spawnMob(id, qty, new Point(x, y));
    }

    public final void 指定地图召唤怪物(int id, int mapid, int x, int y) {
        this.spawnMob_map(id, mapid, new Point(x, y));
    }

    public final void spawnMob_map(int id, int mapid, Point pos) {
        this.c.getChannelServer().getMapFactory().getMap(mapid).spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(id), pos);
    }

    public void 给宠物(int id, String name, long period) {
        if (id > 5010000 || id < 5000000) {
            id = 5000000;
            name = this.getItemName(id);

            try {
                MapleInventoryManipulator.addById(this.c, id, (short) 1, "", MaplePet.createPet(id, name, 1, 1, 1, MapleInventoryIdentifier.getInstance(), id == 5000054 ? (int)period : 0, MapleItemInformationProvider.getInstance().getPetFlagInfo(id)), period);
            } catch (NullPointerException var6) {
                //服务端输出信息.println_err(var6);
            }
        }

    }

    public final void 清除当前地图怪物() {
        this.c.getPlayer().getMap().killAllMonsters(true);
    }

    public final byte 判断任务(int id) {
        return this.c.getPlayer().getQuestStatus(id);
    }

    public void 任务开始(int id) {
        MapleQuest.getInstance(id).forceStart(this.c.getPlayer(), 0, (String)null);
    }

    public void 任务完成(int id) {
        MapleQuest.getInstance(id).forceComplete(this.getPlayer(), 0);
    }

    public void 召唤NPC(int npcId) {
        this.c.getPlayer().getMap().spawnNpc(npcId, this.c.getPlayer().getPosition());
    }

    public final void 删除NPC(int mapid, int npcId) {
        this.c.getChannelServer().getMapFactory().getMap(mapid).removeNpc(npcId);
    }

    public void setWeekLog(String logId, int count) {
        this.c.getPlayer().setWeekLog(logId, count);
    }

    public void setWeekLog(String logId) {
        this.c.getPlayer().setWeekLog(logId, 1);
    }

    public int getWeekLog(String logId) {
        return this.c.getPlayer().getWeekLog(logId);
    }

    public void deleteWeekLog(String logId) {
        this.c.getPlayer().deleteWeekLog(logId);
    }

    public void setWeekLoga(String logId, int count) {
        this.c.getPlayer().setWeekLoga(logId, count);
    }

    public void setWeekLoga(String logId) {
        this.c.getPlayer().setWeekLoga(logId, 1);
    }

    public int getWeekLoga(String logId) {
        return this.c.getPlayer().getWeekLoga(logId);
    }

    public void deleteWeekLoga(String logId) {
        this.c.getPlayer().deleteWeekLoga(logId);
    }

    public void setMonthLog(String logId, int count) {
        this.c.getPlayer().setMonthLog(logId, count);
    }

    public void setMonthLog(String logId) {
        this.c.getPlayer().setMonthLog(logId, 1);
    }

    public int getMonthLog(String logId) {
        return this.c.getPlayer().getMonthLog(logId);
    }

    public void deleteMonthLog(String logId) {
        this.c.getPlayer().deleteMonthLog(logId);
    }

    public void setMonthLoga(String logId, int count) {
        this.c.getPlayer().setMonthLoga(logId, count);
    }

    public void setMonthLoga(String logId) {
        this.c.getPlayer().setMonthLoga(logId, 1);
    }

    public int getMonthLoga(String logId) {
        return this.c.getPlayer().getMonthLoga(logId);
    }

    public void deleteMonthLoga(String logId) {
        this.c.getPlayer().deleteMonthLoga(logId);
    }

    public boolean isBossMap(int mapId) {
        return GameConstants.isBossMap(mapId);
    }

    public boolean isTownMap(int mapId) {
        return GameConstants.isTownMap(mapId);
    }

    public boolean isActivityMap(int mapId) {
        return GameConstants.isActivityMap(mapId);
    }

    public boolean isMarketMap(int mapId) {
        return GameConstants.isMarket(mapId);
    }

    public boolean checkNumSpace(byte type, int quantity) {
        if (quantity <= 0) {
            quantity = 1;
        }

        return !this.c.getPlayer().getInventory(MapleInventoryType.getByType(type)).isFull((quantity - 1));
    }

    public long getItemQuantity(int itemId, boolean checkEquipped) {
        return this.c.getPlayer().getItemQuantity(itemId, checkEquipped);
    }
    public void youlog(String fileName, String message) {
        FileoutputUtil.logToFile("logs/" + fileName, FileoutputUtil.NowTime() + ": (账号:" + this.c.getAccountName() + " 角色名：" + this.c.getPlayer().getName() + ")" + message + "\r\n");
    }

    public String getOneTimeStringLog(String logName) {
        return this.c.getPlayer().getOneTimeStringLog(logName);
    }

    public void setOneTimeStringLog(String logName, String logVal) {
        this.c.getPlayer().setOneTimeStringLog(logName, logVal);
    }

    public int takeBankItem(BankItem bankItem, short count) {
        if (bankItem == null) {
            return -1;
        } else if ((long)count <= bankItem.getCount() && count >= 1) {
            if (!MapleInventoryManipulator.checkSpace(this.getClient(), bankItem.getItemid(), count, "")) {
                return -3;
            } else {
                int ret;
                if ((long)count < bankItem.getCount() && !GameConstants.isThrowingStar(bankItem.getItemid()) && !GameConstants.isBullet(bankItem.getItemid())) {
                    bankItem.setCount(bankItem.getCount() - count);
                    ret = BankItemManager.getInstance().update(bankItem);
                } else {
                    ret = BankItemManager.getInstance().delete(bankItem.getId());
                }

                if (ret > 0) {
                    this.gainItem(bankItem.getItemid(),count);
                }

                return ret;
            }
        } else {
            return -2;
        }
    }

    public int takeBankItem1(BankItem1 bankItem1, short count) {
        if (bankItem1 == null) {
            return -1;
        } else if ((long)count <= bankItem1.getCount() && count >= 1) {
            if (!MapleInventoryManipulator.checkSpace(this.getClient(), bankItem1.getItemid(), count, "")) {
                return -3;
            } else {
                int ret;
                if ((long)count < bankItem1.getCount() && !GameConstants.isThrowingStar(bankItem1.getItemid()) && !GameConstants.isBullet(bankItem1.getItemid())) {
                    bankItem1.setCount(bankItem1.getCount() - count);
                    ret = BankItemManager1.getInstance().update(bankItem1);
                } else {
                    ret = BankItemManager1.getInstance().delete(bankItem1.getId());
                }

                if (ret > 0) {
                    this.gainItem(bankItem1.getItemid(),count);
                }

                return ret;
            }
        } else {
            return -2;
        }
    }

    public int takeBankItem2(BankItem2 bankItem2, short count) {
        if (bankItem2 == null) {
            return -1;
        } else if ((long)count <= bankItem2.getCount() && count >= 1) {
            if (!MapleInventoryManipulator.checkSpace(this.getClient(), bankItem2.getItemid(), count, "")) {
                return -3;
            } else {
                int ret;
                if ((long)count < bankItem2.getCount() && !GameConstants.isThrowingStar(bankItem2.getItemid()) && !GameConstants.isBullet(bankItem2.getItemid())) {
                    bankItem2.setCount(bankItem2.getCount() - count);
                    ret = BankItemManager2.getInstance().update(bankItem2);
                } else {
                    ret = BankItemManager2.getInstance().delete(bankItem2.getId());
                }

                if (ret > 0) {
                    this.gainItem(bankItem2.getItemid(),count);
                }

                return ret;
            }
        } else {
            return -2;
        }
    }

    public void startOxQuiz(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(true) {
            ChannelServer cs;
            do {
                if (!var2.hasNext()) {
                    return;
                }

                cs = (ChannelServer)var2.next();
            } while(cs.getChannel() != channel);

            MapleEvent event = cs.getEvent(MapleEventType.OX答题比赛);
            event.unreset();
            event.reset();
            Iterator var5 = this.c.getPlayer().getMap().getCharactersThreadsafe().iterator();

            while(var5.hasNext()) {
                MapleCharacter chr = (MapleCharacter)var5.next();
                event.onMapLoad(chr);
            }

            event.startEvent();
        }
    }

    public void startCoconut(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.打椰子比赛);
                event.startEvent();
            }
        }

    }

    public void startSnowBall(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.推雪球比赛);
                event.reset();
                event.startEvent();
            }
        }

    }

    public void startOla(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.上楼上楼);
                event.startEvent();
            }
        }

    }

    public void startFitness(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.向高地比赛);
                event.startEvent();
            }
        }

    }

    public void startJewel(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.寻宝);
                event.startEvent();
            }
        }

    }

    public void startMonsterComming(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.怪物攻城);
                event.startEvent();
            }
        }

    }

    public void stopMonsterComming(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.怪物攻城);
                event.unreset();
            }
        }

    }

    public void guildMatch_start(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        while(var2.hasNext()) {
            ChannelServer cs = (ChannelServer)var2.next();
            if (cs.getChannel() == channel) {
                MapleEvent event = cs.getEvent(MapleEventType.家族对抗赛);
                event.startEvent();
            }
        }

    }

    public int guildMatch_register(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        ChannelServer cs;
        do {
            if (!var2.hasNext()) {
                return 0;
            }

            cs = (ChannelServer)var2.next();
        } while(cs.getChannel() != channel);

        MapleGuildMatch event = (MapleGuildMatch)cs.getEvent(MapleEventType.家族对抗赛);
        return event.register(this.c.getPlayer());
    }

    public int guildMatch_join(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        ChannelServer cs;
        do {
            if (!var2.hasNext()) {
                return 0;
            }

            cs = (ChannelServer)var2.next();
        } while(cs.getChannel() != channel);

        MapleGuildMatch event = (MapleGuildMatch)cs.getEvent(MapleEventType.家族对抗赛);
        return event.join(this.c.getPlayer());
    }

    public boolean guildMatch_isJoin(int channel) {
        Iterator var2 = ChannelServer.getAllInstances().iterator();

        ChannelServer cs;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            cs = (ChannelServer)var2.next();
        } while(cs.getChannel() != channel);

        MapleGuildMatch event = (MapleGuildMatch)cs.getEvent(MapleEventType.家族对抗赛);
        return event.isJoined(this.c.getPlayer().getId());
    }

    public MapleGuildMatch getMapleGuildMatch(int channel) {
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        ChannelServer cs;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            cs = (ChannelServer)var3.next();
        } while(cs.getChannel() != channel);

        MapleGuildMatch event = (MapleGuildMatch)cs.getEvent(MapleEventType.家族对抗赛);
        return event;
    }

    public MapleGuildOutsideBoss getMapleGuildOutsideBoss(int channel) {
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        ChannelServer cs;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            cs = (ChannelServer)var3.next();
        } while(cs.getChannel() != channel);

        MapleGuildOutsideBoss event = (MapleGuildOutsideBoss)cs.getEvent(MapleEventType.家族野外BOSS赛);
        return event;
    }

    public void sendBlueDamageToAllMobs(int hp, boolean trueDamage) {
        Iterator var3 = this.c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

        while(var3.hasNext()) {
            MapleMonster mob = (MapleMonster)var3.next();
            mob.sendBlueDamage((long)hp, trueDamage);
        }

    }

    public void sendBlueDamageToAllMobs(int hp, MapleCharacter chr) {
        Iterator var3 = this.c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

        while(var3.hasNext()) {
            MapleMonster mob = (MapleMonster)var3.next();
            mob.sendBlueDamage((long)hp, chr);
        }

    }

    public void sendYellowDamageToAllMobs(int hp, boolean trueDamage) {
        Iterator var3 = this.c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

        while(var3.hasNext()) {
            MapleMonster mob = (MapleMonster)var3.next();
            mob.sendYellowDamage((long)hp, trueDamage);
        }

    }

    public void sendYellowDamageToAllMobs(int hp, MapleCharacter chr) {
        Iterator var3 = this.c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

        while(var3.hasNext()) {
            MapleMonster mob = (MapleMonster)var3.next();
            mob.sendYellowDamage((long)hp, chr);
        }

    }
    public void 发送全服画面特效(int itemId, String ourMsg) {
        if (itemId / 10000 == 512) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Iterator var4 = ChannelServer.getAllInstances().iterator();

            while(var4.hasNext()) {
                ChannelServer cs = (ChannelServer)var4.next();
                Iterator var6 = cs.getMapFactory().getAllMaps().iterator();

                while(var6.hasNext()) {
                    MapleMap map = (MapleMap)var6.next();
                    map.startMapEffect(ourMsg, itemId, false);
                }
            }
        }

    }

    public void 发送全服画面特效(int itemId, String ourMsg, int duration) {
        if (itemId / 10000 == 512) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            Iterator var5 = ChannelServer.getAllInstances().iterator();

            while(var5.hasNext()) {
                ChannelServer cs = (ChannelServer)var5.next();
                Iterator var7 = cs.getMapFactory().getAllMaps().iterator();

                while(var7.hasNext()) {
                    MapleMap map = (MapleMap)var7.next();
                    map.startMapEffect_S(ourMsg, itemId, duration);
                }
            }
        }

    }

    public void 发送全服Buff(int itemId) {
        if (itemId / 10000 == 512) {
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            int buff = ii.getStateChangeItem(itemId);
            if (buff != 0) {
                Iterator var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    ChannelServer cs = (ChannelServer)var4.next();
                    Iterator var6 = cs.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        MapleCharacter mChar = (MapleCharacter)var6.next();
                        ii.getItemEffect(buff).applyTo(mChar);
                    }
                }
            }
        }

    }

    /**
     * 获取当前阶段地图信息
     * @return
     */
    public final List<LtCopyMap> getMapInfo(int index) {
        try {
            return Start.ltCopyMap.get(index);
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 获取地图可刷新怪物信息
     * @return 获取可以刷新的怪物集合信息
     */
    public final List<LtCopyMapMonster> getMapMonsterInfo(int index) {
        try {
            return Start.ltCopyMapMonster.get(index);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取怪物位置
     * @param index
     * @return
     */
    public final List<LtMonsterPosition> getMonsterPosition(int index) {
        try {
            return Start.ltMonsterPosition.get(index);
        } catch (Exception e) {
            return null;
        }
    }
}
