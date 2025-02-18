package tools;

import java.sql.SQLException;
import java.sql.ResultSet;

import bean.BreakthroughMechanism;
import bean.ItemInfo;
import client.inventory.*;
import gui.LtMS;
import handling.channel.MapleGuildRanking;
import server.*;
import server.maps.MapleDragon;
import client.MapleBeans;
import client.MapleBeans.BeansType;
import server.maps.MapleNodes.MaplePlatform;
import server.maps.MapleNodes.MapleNodeInfo;
import server.life.MapleMonster;
import server.shops.MaplePlayerShopItem;
import server.shops.HiredMerchant;
import constants.ServerConstants;

import java.util.*;

import client.MapleStat.Temp;
import server.events.MapleSnowball.MapleSnowballs;
import client.SkillMacro;
import handling.channel.MapleGuildRanking.GuildRankingInfo;
import handling.channel.MapleGuildRanking.levelRankingInfo;
import handling.channel.MapleGuildRanking.mesoRankingInfo;
import handling.world.guild.MapleBBSThread.MapleBBSReply;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleGuildCharacter;
import server.maps.MapleReactor;
import client.BuddyEntry;
import server.maps.MapleMist;
import handling.world.PartyOperation;
import handling.world.MaplePartyCharacter;
import handling.world.MapleParty;
import client.MapleKeyLayout;
import client.MapleDisease;
import handling.world.guild.MapleGuildAlliance;
import handling.world.World.Alliance;
import client.MapleQuestStatus;
import client.inventory.IEquip.ScrollResult;
import server.life.SummonAttackEntry;
import server.movement.LifeMovementFragment;
import handling.world.guild.MapleGuild;
import constants.GameConstants;

import handling.world.World.Guild;
import server.maps.MapleMapItem;
import client.MapleBuffStat;
import server.life.PlayerNPC;
import server.life.MapleNPC;
import server.maps.MapleSummon;
import java.awt.Point;
import server.maps.MapleMap;

import java.util.Map.Entry;

import client.MapleStat;
import tools.packet.PacketHelper;
import client.MapleCharacter;
import constants.ServerConfig;
import handling.SendPacketOpcode;
import tools.data.MaplePacketLittleEndianWriter;
import client.MapleClient;
import util.GetRedisDataUtil;
import util.ListUtil;

public class MaplePacketCreator
{
    public static int[] SecondaryStatRemote;
    
    public static byte[] getServerIP(final MapleClient c, final int port, final int clientId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVER_IP.getValue());
        mplew.writeShort(0);
        if (ServerConfig.TESPIA) {
            mplew.write(ServerConfig.Gateway_IP2);
        }
        else {
            mplew.write(ServerConfig.Gateway_IP);
        }
        mplew.writeShort(port);
        mplew.writeInt(clientId);
        mplew.write(new byte[] { 1, 0, 0, 0, 0 });
        return mplew.getPacket();
    }
    
    public static byte[] getChannelChange(final MapleClient c, final int port) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CHANGE_CHANNEL.getValue());
        mplew.write(1);
        if (ServerConfig.TESPIA) {
            mplew.write(ServerConfig.Gateway_IP2);
        }
        else {
            mplew.write(ServerConfig.Gateway_IP);
        }
        mplew.writeShort(port);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] getCharInfo(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.writeShort(0);
        chr.CRand().connectData(mplew);
        PacketHelper.addCharacterInfo(mplew, chr, false);
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }
    public static byte[] clearInventoryItem(final MapleInventoryType type, final short slot, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write((int)(fromDrop ? 1 : 0));
        mplew.write(HexTool.getByteArrayFromHexString("01 03"));
        mplew.write(type.getType());
        mplew.writeShort((int)slot);
        return mplew.getPacket();
    }
    public static byte[] enableActions() {
        return updatePlayerStats((Map<MapleStat, Integer>)new EnumMap<MapleStat, Integer>(MapleStat.class), true, null);
    }
    
    public static byte[] updatePlayerStats(final Map<MapleStat, Integer> stats, MapleCharacter chr) {
        return updatePlayerStats(stats, false, chr);
    }

    //强制变更属性1
    public static byte[] updatePlayerStats(final Map<MapleStat, Integer> mystats, final boolean itemReaction, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write((int)(itemReaction ? 1 : 0));
        int updateMask = 0;
        for (final MapleStat statupdate : mystats.keySet()) {
            updateMask |= statupdate.getValue();
        }
        mplew.writeInt(updateMask);
        for (final Entry<MapleStat, Integer> statupdate2 : mystats.entrySet()) {
            switch ((MapleStat)statupdate2.getKey()) {
                case SKIN:
                case LEVEL: {
                    mplew.write(Integer.valueOf(statupdate2.getValue()).byteValue());
                    continue;
                }
                case JOB:
                case STR:
                case DEX:
                case INT:
                case LUK:
                case HP:
                case MAXHP:
                case MP:
                case MAXMP:
                case AVAILABLEAP:
                case FAME: {
                    mplew.writeShort((int)Integer.valueOf(statupdate2.getValue()).shortValue());
                    continue;
                }
                case AVAILABLESP: {
                    mplew.writeShort(chr.getRemainingSp(0));
                    continue;
                }
                case EXP:
                case FACE:
                case HAIR:
                case MESO: {
                    mplew.writeInt((int)Integer.valueOf(statupdate2.getValue()));
                    continue;
                }
                case PET: {
                    mplew.writeLong((long)Integer.valueOf(statupdate2.getValue()));
                    mplew.writeLong((long)Integer.valueOf(statupdate2.getValue()));
                    mplew.writeLong((long)Integer.valueOf(statupdate2.getValue()));
                    continue;
                }
                default: {
                    mplew.writeInt((int)Integer.valueOf(statupdate2.getValue()));
                    continue;
                }
            }
        }
        if ((long)updateMask == 0L && !itemReaction) {
            mplew.write(1);
        }
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] updateSp(MapleCharacter chr, final boolean itemReaction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write((int)(itemReaction ? 1 : 0));
        mplew.writeInt(MapleStat.AVAILABLESP.getValue());
        mplew.writeShort(chr.getRemainingSp());
        return mplew.getPacket();
    }
    
    public static byte[] updateAp(MapleCharacter chr, final boolean itemReaction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_STATS.getValue());
        mplew.write((int)(itemReaction ? 1 : 0));
        mplew.writeInt(MapleStat.AVAILABLEAP.getValue());
        mplew.writeShort((int)chr.getRemainingAp());
        return mplew.getPacket();
    }

    /**
     * getWarpToMap 传送到地图
     * @param to          目标地图
     * @param spawnPoint  地图传送点
     * @param chr         角色
     * @return
     */
    public static byte[] getWarpToMap(final MapleMap to, final int spawnPoint, MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SET_FIELD.getValue());
        mplew.writeInt(chr.getClient().getChannel() - 1);
        mplew.write(0);
        mplew.write(3);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.writeInt(to.getId());
        mplew.write(spawnPoint);
        mplew.writeShort((int)chr.getStat().getHp());
        mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
        return mplew.getPacket();
    }

    /**
     * 传送
     * @param portal
     * @return
     */
    public static byte[] instantMapWarp(final byte portal) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CURRENT_MAP_WARP.getValue());
        mplew.write(0);
        mplew.write(portal);
        return mplew.getPacket();
    }
    
    public static byte[] spawnPortal(final int townId, final int targetId, final int skillId, final Point pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_PORTAL.getValue());
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        if (townId != 999999999 && targetId != 999999999) {
            mplew.writePos(pos);
        }
        return mplew.getPacket();
    }

    /**
     *  产生范围door
     * @param oid
     * @param pos
     * @param town
     * @return
     */
    public static byte[] spawnDoor(final int oid, final Point pos, final boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_DOOR.getValue());
        mplew.write((int)(town ? 0 : 1));
        mplew.writeInt(oid);
        mplew.writePos(pos);
        return mplew.getPacket();
    }
    
    public static byte[] removeDoor(final int oid, final boolean town) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_DOOR.getValue());
        mplew.write((int)(town ? 0 : 1));
        mplew.writeInt(oid);
        return mplew.getPacket();
    }
    
    public static byte[] spawnSummon(final MapleSummon summon, final boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.writeInt(summon.getSkill());
        mplew.write(summon.getOwnerLevel() - 1);
        mplew.write(summon.getSkillLevel());
        mplew.writePos(summon.getPosition());
        mplew.write((summon.getSkill() == 32111006 || summon.getSkill() == 33101005) ? 5 : 4);
        if (summon.getSkill() == 35121003 && summon.getOwner().getMap() != null) {
            mplew.writeShort(summon.getOwner().getMap().getFootholds().findBelow(summon.getPosition()).getId());
        }
        else {
            mplew.writeShort(0);
        }
        mplew.write(summon.getMovementType().getValue());
        mplew.write(summon.getSummonType());
        mplew.write((int)(animated ? 1 : 0));
        mplew.writeZeroBytes(8);
        return mplew.getPacket();
    }
    
    public static byte[] removeSummon(final MapleSummon summon, final boolean animated) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_SUMMON.getValue());
        mplew.writeInt(summon.getOwnerId());
        mplew.writeInt(summon.getObjectId());
        mplew.write(animated ? 4 : 1);
        return mplew.getPacket();
    }
    
    public static byte[] getRelogResponse() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter(3);
        mplew.writeShort((int)SendPacketOpcode.RELOG_RESPONSE.getValue());
        mplew.write(1);
        return mplew.getPacket();
    }
    
    public static byte[] serverBlocked(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVER_BLOCKED.getValue());
        mplew.write(type);
        return mplew.getPacket();
    }
    
    public static byte[] getItemNotice(final String message) {
        return getItemNotice(message, 0);
    }
    
    public static byte[] getItemNotice(final String message, final int itemId) {
        return serverMessage(6, 0, message, false);
    }
    
    public static byte[] getPopupMsg(final String message) {
        return serverMessage(1, 0, message, false);
    }
    
    public static byte[] serverMessage(final String message) {
        return serverMessage(4, 0, message, false);
    }
    
    public static byte[] getErrorNotice(final String message) {
        return broadcastMessage(5, 0, new String[] { message }, true, null);
    }
    
    public static byte[] serverNotice(final int type, final String message) {
        return serverMessage(type, 0, message, false);
    }
    
    public static byte[] serverNotice(final int type, final int channel, final String message) {
        return serverMessage(type, channel, message, false);
    }
    
    public static byte[] serverNotice(final int type, final int channel, final String message, final boolean smegaEar) {
        return serverMessage(type, channel, message, smegaEar);
    }
    
    private static byte[] serverMessage(final int type, final int channel, final String message, final boolean megaEar) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(1);
        }
        mplew.writeMapleAsciiString(message);
        switch (type) {
            case 3:
            case 9:
            case 10:
            case 11:
            case 12: {
                mplew.write(channel - 1);
                mplew.write((int)(megaEar ? 1 : 0));
                break;
            }
            case 6:
            case 18: {
                mplew.writeInt((channel >= 1000000 && channel < 6000000) ? channel : 0);
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] getGachaponMega(final String name, final String message, final IItem item, final byte rareness) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(16);
        mplew.writeMapleAsciiString(name + message);
        mplew.writeInt(0);
        PacketHelper.addItemInfo(mplew, item, true, true);
        return mplew.getPacket();
    }

    public static byte[] getGachaponMega(String name, String message, byte type, IItem item, byte rareness, int Channel) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        mplew.writeMapleAsciiString(name + message);
        mplew.writeInt(Channel - 1);
        PacketHelper.addItemInfo(mplew, item, true, true);
        return mplew.getPacket();
    }


    public static byte[] getGachaponMega(final String name, final String message, final IItem item, final byte rareness, final int Channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(14);
        mplew.writeMapleAsciiString(name + message);
        mplew.writeInt(Channel - 1);
        PacketHelper.addItemInfo(mplew, item, true, true);
        return mplew.getPacket();
    }
    
    private static byte[] broadcastMessage(final int type, final int channel, final String[] message, final boolean bool, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(type);
        if (type == 4) {
            mplew.write(bool);
        }
        if (type != 4 || bool) {
            mplew.writeMapleAsciiString((message == null || message.length < 1) ? "" : message[0]);
            switch (type) {
                case 3:
                case 11:
                case 12: {
                    mplew.write(channel - 1);
                    mplew.write((int)(bool ? 1 : 0));
                    break;
                }
                case 8: {
                    mplew.write(channel - 1);
                    mplew.write((int)(bool ? 1 : 0));
                    mplew.write(item != null);
                    if (item != null) {
                        PacketHelper.addItemInfo(mplew, item, true, true);
                        break;
                    }
                    break;
                }
                case 9: {
                    mplew.write(channel - 1);
                    break;
                }
                case 10: {
                    final int lines = (message == null) ? 0 : message.length;
                    mplew.write(lines);
                    if (lines > 1) {
                        mplew.writeMapleAsciiString((message == null || message.length < 2) ? "" : message[1]);
                    }
                    if (lines > 2) {
                        mplew.writeMapleAsciiString((message == null || message.length < 3) ? "" : message[2]);
                    }
                    mplew.write(channel - 1);
                    mplew.write((int)(bool ? 1 : 0));
                    break;
                }
                case 13: {
                    mplew.writeInt(channel - 1);
                    PacketHelper.addItemInfo(mplew, item, true, true);
                    break;
                }
            }
        }
        switch (type) {
            case 0: {}
            case 1: {}
            case 2: {}
            case 3:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12: {}
            case 13: {}
            case 4: {}
            case 6: {
                mplew.writeInt((channel >= 1000000 && channel < 6000000) ? channel : 0);
                break;
            }
            case 7: {
                mplew.writeInt(-1);
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] tripleSmega(final List<String> message, final boolean ear, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(10);
        if (message.get(0) != null) {
            mplew.writeMapleAsciiString((String)message.get(0));
        }
        mplew.write(message.size());
        for (int i = 1; i < message.size(); ++i) {
            if (message.get(i) != null) {
                mplew.writeMapleAsciiString((String)message.get(i));
            }
        }
        mplew.write(channel - 1);
        mplew.write((int)(ear ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] HeartSmega(final List<String> message, final boolean ear, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(11);
        mplew.writeMapleAsciiString((String)message.get(0));
        mplew.write(channel - 1);
        mplew.write((int)(ear ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] SkullSmega(final List<String> message, final boolean ear, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(12);
        mplew.writeMapleAsciiString((String)message.get(0));
        mplew.write(channel - 1);
        mplew.write((int)(ear ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] getAvatarMega(MapleCharacter chr, final int channel, final int itemId, final String message, final boolean ear) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.AVATAR_MEGA.getValue());
        mplew.writeInt(itemId);
        mplew.writeMapleAsciiString(chr.getName());
        mplew.writeMapleAsciiString(message);
        mplew.writeInt(channel - 1);
        mplew.write((int)(ear ? 1 : 0));
        PacketHelper.addCharLook(mplew, chr, true);
        return mplew.getPacket();
    }
    
    public static byte[] itemMegaphone(final String msg, final boolean whisper, final int channel, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SERVERMESSAGE.getValue());
        mplew.write(8);
        mplew.writeMapleAsciiString(msg);
        mplew.write(channel - 1);
        mplew.write((int)(whisper ? 1 : 0));
        if (item == null) {
            mplew.write(0);
        }
        else {
            PacketHelper.addItemInfo(mplew, item, false, false);
        }
        return mplew.getPacket();
    }
    
    public static byte[] spawnNPC(final MapleNPC life, final boolean show) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_NPC.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write((int)((life.getF() != 1) ? 1 : 0));
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write((int)(show ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] removeNPC(final int objectid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_NPC.getValue());
        mplew.writeInt(objectid);
        return mplew.getPacket();
    }
    
    public static byte[] removeNPCController(final int objectid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(0);
        mplew.writeInt(objectid);
        return mplew.getPacket();
    }
    
    public static byte[] spawnNPCRequestController(final MapleNPC life, final boolean MiniMap) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_NPC_REQUEST_CONTROLLER.getValue());
        mplew.write(1);
        mplew.writeInt(life.getObjectId());
        mplew.writeInt(life.getId());
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getCy());
        mplew.write((int)((life.getF() != 1) ? 1 : 0));
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getRx0());
        mplew.writeShort(life.getRx1());
        mplew.write((int)(MiniMap ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] spawnPlayerNPC(final PlayerNPC npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.IMITATED_NPC_DATA.getValue());
        mplew.write((int)((npc.getF() != 1) ? 1 : 0));
        mplew.writeInt(npc.getId());
        mplew.writeMapleAsciiString(npc.getName());
        mplew.write(npc.getGender());
        mplew.write(npc.getSkin());
        mplew.writeInt(npc.getFace());
        mplew.write(0);
        mplew.writeInt(npc.getHair());
        final Map<Byte, Integer> equip = npc.getEquips();
        final Map<Byte, Integer> myEquip = new LinkedHashMap<Byte, Integer>();
        final Map<Byte, Integer> maskedEquip = new LinkedHashMap<Byte, Integer>();
        for (final Entry<Byte, Integer> position : equip.entrySet()) {
            byte pos = (byte)((byte)Byte.valueOf(position.getKey()) * -1);
            if (pos < 100 && myEquip.get((Object)Byte.valueOf(pos)) == null) {
                myEquip.put(Byte.valueOf(pos), position.getValue());
            }
            else if ((pos > 100 || pos == -128) && pos != 111) {
                pos = (byte)((pos == -128) ? 28 : (pos - 100));
                if (myEquip.get((Object)Byte.valueOf(pos)) != null) {
                    maskedEquip.put(Byte.valueOf(pos), myEquip.get((Object)Byte.valueOf(pos)));
                }
                myEquip.put(Byte.valueOf(pos), position.getValue());
            }
            else {
                if (myEquip.get((Object)Byte.valueOf(pos)) == null) {
                    continue;
                }
                maskedEquip.put(Byte.valueOf(pos), position.getValue());
            }
        }
        for (final Entry<Byte, Integer> entry : myEquip.entrySet()) {
            mplew.write((byte)Byte.valueOf(entry.getKey()));
            mplew.writeInt((int)Integer.valueOf(entry.getValue()));
        }
        mplew.write(255);
        for (final Entry<Byte, Integer> entry : maskedEquip.entrySet()) {
            mplew.write((byte)Byte.valueOf(entry.getKey()));
            mplew.writeInt((int)Integer.valueOf(entry.getValue()));
        }
        mplew.write(255);
        final Integer cWeapon = equip.get((Object)Byte.valueOf((byte)(-111)));
        if (cWeapon != null) {
            mplew.writeInt((int)cWeapon);
        }
        else {
            mplew.writeInt(0);
        }
        for (int i = 0; i < 3; ++i) {
            mplew.writeInt(npc.getPet(i));
        }
        return mplew.getPacket();
    }
    
    public static byte[] getChatText(final int cidfrom, final String text, final boolean whiteBG, final int show) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CHATTEXT.getValue());
        mplew.writeInt(cidfrom);
        mplew.write((int)(whiteBG ? 1 : 0));
        mplew.writeMapleAsciiString(text);
        mplew.write(show);
        return mplew.getPacket();
    }
    
    public static byte[] GameMaster_Func(final int value) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GM_EFFECT.getValue());
        mplew.write(value);
        mplew.writeZeroBytes(17);
        return mplew.getPacket();
    }
    
    public static byte[] addComboBuff(final int combo) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        mplew.writeLong((long)MapleBuffStat.ARAN_COMBO.getValue());
        mplew.writeLong(0L);
        mplew.writeShort(combo);
        mplew.writeInt(21000000);
        mplew.writeInt(256);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] updateCombo(final int value) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ARAN_COMBO.getValue());
        mplew.writeInt(value);
        return mplew.getPacket();
    }
    
    public static byte[] getPacketFromHexString(final String hex) {
        return HexTool.getByteArrayFromHexString(hex);
    }



    public static final byte[] GainEXP_Monster(int gain, boolean white, int partyinc, int Class_Bonus_EXP, int Equipment_Bonus_EXP, int Premium_Bonus_EXP) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.write(white ? 1 : 0);
        mplew.writeInt(gain);
        byte v2 = 0;
        mplew.write(v2);
        mplew.writeInt(0);
        byte v6 = 0;
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        if (ServerConfig.version == 85) {
            mplew.writeInt(0);
        }

        if (v6 > 0) {
            mplew.write(0);
        }

        if (v2 > 0) {
            byte v2a = 0;
            mplew.write(v2a);
            if (v2a > 0) {
                byte v2aa = 0;
                mplew.write(v2aa);
            }
        }

        mplew.write(0);
        mplew.writeInt(partyinc);
        mplew.writeInt(Equipment_Bonus_EXP);
        mplew.writeInt(Premium_Bonus_EXP);
        if (ServerConfig.version == 85) {
            mplew.writeInt(0);
        }

        return mplew.getPacket();
    }


    public static byte[] GainEXP_Monster(final int gain, final boolean white, final int partyinc, final int Class_Bonus_EXP, final int Equipment_Bonus_EXP, final int Premium_Bonus_EXP, final int H_EXP) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.write((int)(white ? 1 : 0));
        mplew.writeInt(gain);
        final byte v2 = 0;
        mplew.write(v2);
        mplew.writeInt(0);
        final byte v3 = 0;
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        if (v3 > 0) {
            mplew.write(0);
        }
        if (v2 > 0) {
            final byte v2a = 0;
            mplew.write(v2a);
            if (v2a > 0) {
                final byte v2aa = 0;
                mplew.write(v2aa);
            }
        }
        mplew.write(0);
        mplew.writeInt(partyinc);
        mplew.writeInt(Equipment_Bonus_EXP);
        mplew.writeInt(H_EXP);
        return mplew.getPacket();
    }
    
    public static byte[] GainEXP_Others(final int gain, final boolean inChat, final boolean white) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(3);
        mplew.write((int)(white ? 1 : 0));
        mplew.writeInt(gain);
        final byte v2 = 0;
        mplew.write(v2);
        mplew.writeInt(0);
        final byte v3 = 0;
        mplew.write(0);
        mplew.write(0);
        mplew.writeInt(0);
        if (v3 > 0) {
            mplew.write(0);
        }
        if (v2 > 0) {
            final byte v2a = 0;
            mplew.write(v2a);
            if (v2a > 0) {
                final byte v2aa = 0;
                mplew.write(v2aa);
            }
        }
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] getShowFameGain(final int gain) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(4);
        mplew.writeInt(gain);
        return mplew.getPacket();
    }
    
    public static byte[] showMesoGain(final int gain, final boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        if (!inChat) {
            mplew.write(0);
            mplew.write(1);
            mplew.write(0);
            mplew.writeInt(gain);
            mplew.writeShort(0);
        }
        else {
            mplew.write(5);
            mplew.writeInt(gain);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getShowItemGain(final int itemId, final short quantity) {
        return getShowItemGain(itemId, quantity, false);
    }
    
    public static byte[] getShowItemGain(final int itemId, final short quantity, final boolean inChat) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (inChat) {
            mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
            mplew.write(3);
            mplew.write(1);
            mplew.writeInt(itemId);
            mplew.writeInt((int)quantity);
        }
        else {
            mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
            mplew.writeShort(0);
            mplew.writeInt(itemId);
            mplew.writeInt((int)quantity);
        }
        return mplew.getPacket();
    }
    
    public static byte[] showRewardItemAnimation(final int itemId, final String effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(16);
        mplew.writeInt(itemId);
        mplew.write((int)((effect != null && effect.length() > 0) ? 1 : 0));
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }
        return mplew.getPacket();
    }

    /**
     * 显示奖励画面
     * @param itemId     道具id
     * @param effect     效果
     * @param from_playerid 来自哪个玩家
     * @return
     */
    public static byte[] showRewardItemAnimation(final int itemId, final String effect, final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(16);
        mplew.writeInt(itemId);
        mplew.write((int)((effect != null && effect.length() > 0) ? 1 : 0));
        if (effect != null && effect.length() > 0) {
            mplew.writeMapleAsciiString(effect);
        }
        return mplew.getPacket();
    }
    
    public static byte[] dropItemFromMapObject( MapleMapItem drop, final Point dropfrom, final Point dropto, final byte mod) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DROP_ITEM_FROM_MAPOBJECT.getValue());
        mplew.write(mod);
        mplew.writeInt(drop.getObjectId());
        mplew.write((int)((drop.getMeso() > 0) ? 1 : 0));
        mplew.writeInt(drop.getItemId());
        mplew.writeInt(drop.getOwner());
        mplew.write(drop.getDropType());
        mplew.writePos(dropto);
        mplew.writeInt((drop.getDropType() == 0) ? drop.getOwner() : 0);
        if (mod != 2) {
            mplew.writePos(dropfrom);
            mplew.writeShort(0);
        }
        if (drop.getMeso() == 0) {
            PacketHelper.addExpirationTime(mplew,drop.getItem().getExpiration());
        }
            mplew.writeShort((int) (drop.isPlayerDrop() ? 0 : 1));// 玩家丢弃是 0 怪物掉落是 1
            return mplew.getPacket();
        }
    
    public static void writeBuffMask(final MaplePacketLittleEndianWriter mplew, final Collection<MapleBuffStat> statups) {
        final int[] mask = { 0, 0, -262144, 0 };
        for (final MapleBuffStat buf : statups) {
            final int[] array = mask;
            final int position = buf.getPosition();
            array[position] |= buf.getValue();
        }
        for (int i = 0; i < mask.length; ++i) {
            mplew.writeInt(mask[i]);
        }
    }
    
    public static byte[] spawnPlayerMapobject(MapleCharacter chr) {

        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_PLAYER.getValue());
        mplew.writeInt(chr.getId());
        mplew.write((int)chr.getLevel());
        mplew.writeMapleAsciiString(chr.getName());
        boolean showPower = (Integer)LtMS.ConfigValuesMap.get("角色名战斗力显示") > 0;
        boolean 家族名称显示 = Objects.nonNull(LtMS.ConfigValuesMap.get("家族名称显示")) && LtMS.ConfigValuesMap.get("家族名称显示") > 0;
        boolean BOSS伤害显示 = Objects.nonNull(LtMS.ConfigValuesMap.get("BOSS伤害显示")) && LtMS.ConfigValuesMap.get("BOSS伤害显示") > 0;

        if (chr.getGuildId() <= 0) {
            if (showPower) {
                if(BOSS伤害显示) {
                    mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(chr.getPower()) + "』"+"|BOSS伤:"+(int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(chr.get套装伤害加成()*100))+"%"+(chr.开启巅峰等级>0 ? "|巅峰:"+chr.开启巅峰等级+"级" : ""));
                }else{
                    mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(chr.getPower()) + "』"+(chr.开启巅峰等级>0 ? "|巅峰:"+chr.开启巅峰等级+"级" : ""));
                }
            } else {
                if(BOSS伤害显示) {
                    mplew.writeMapleAsciiString("BOSS伤:"+((int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(chr.get套装伤害加成()*100)))+"%"+(chr.开启巅峰等级>0 ? "|巅峰:"+chr.开启巅峰等级+"级" : ""));
                }else{
                    mplew.writeMapleAsciiString("");
                }
            }

            mplew.writeZeroBytes(6);
        } else {
            MapleGuild gs = Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                if (showPower) {
                    if(家族名称显示){
                        if(BOSS伤害显示) {
                            mplew.writeMapleAsciiString(gs.getName() + "『战力:" + formatNumberWithUnit(chr.getPower()) + "』"+"|BOSS伤:"+(int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(chr.get套装伤害加成()*100))+"%");
                        }else{
                            mplew.writeMapleAsciiString(gs.getName() + "『战力:" + formatNumberWithUnit(chr.getPower()) + "』");
                        }
                    }else{
                        if(BOSS伤害显示) {
                            mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(chr.getPower()) + "』"+"|BOSS伤:"+(int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(chr.get套装伤害加成()*100))+"%");
                        }else{
                            mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(chr.getPower()) + "』");
                        }
                    }
                } else {
                    if(BOSS伤害显示) {
                        mplew.writeMapleAsciiString(gs.getName()+"|BOSS伤:"+(int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(chr.get套装伤害加成()*100))+"%");
                    }else{
                        mplew.writeMapleAsciiString(gs.getName());
                    }
                }

                mplew.writeShort(gs.getLogoBG());
                mplew.write(gs.getLogoBGColor());
                mplew.writeShort(gs.getLogo());
                mplew.write(gs.getLogoColor());
            } else {
                if (showPower) {
                    if(BOSS伤害显示) {
                        mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(chr.getPower()) + "』"+"|BOSS伤:"+(int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(chr.get套装伤害加成()*100))+"%");
                    }else{
                        mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(chr.getPower()) + "』");
                    }
                } else {
                    mplew.writeMapleAsciiString("");
                }

                mplew.writeZeroBytes(6);
            }
        }

//        mplew.writeMapleAsciiString(sb.toString());
//        mplew.writeZeroBytes(6);
        final List<Pair<Integer, Integer>> buffvalue = new ArrayList<Pair<Integer, Integer>>();
        final List<Pair<Integer, Integer>> buffvaluenew = new ArrayList<Pair<Integer, Integer>>();
        final int[] mask = (int[])MaplePacketCreator.SecondaryStatRemote.clone();
        if (chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null && !chr.isHidden()) {
            final int[] array = mask;
            final int position = MapleBuffStat.DARKSIGHT.getPosition();
            array[position] |= MapleBuffStat.DARKSIGHT.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.COMBO) != null) {
            final int[] array2 = mask;
            final int position2 = MapleBuffStat.COMBO.getPosition();
            array2[position2] |= MapleBuffStat.COMBO.getValue();
            buffvalue.add(new Pair<Integer, Integer>(chr.getBuffedValue(MapleBuffStat.COMBO), Integer.valueOf(1)));
        }
        if (chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) {
            final int[] array3 = mask;
            final int position3 = MapleBuffStat.SHADOWPARTNER.getPosition();
            array3[position3] |= MapleBuffStat.SHADOWPARTNER.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.SOULARROW) != null) {
            final int[] array4 = mask;
            final int position4 = MapleBuffStat.SOULARROW.getPosition();
            array4[position4] |= MapleBuffStat.SOULARROW.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.DIVINE_BODY) != null) {
            final int[] array5 = mask;
            final int position5 = MapleBuffStat.DIVINE_BODY.getPosition();
            array5[position5] |= MapleBuffStat.DIVINE_BODY.getValue();
            buffvalue.add(new Pair<Integer, Integer>(chr.getBuffedValue(MapleBuffStat.DIVINE_BODY), Integer.valueOf(3)));
        }
        if (chr.getBuffedValue(MapleBuffStat.BERSERK_FURY) != null) {
            final int[] array6 = mask;
            final int position6 = MapleBuffStat.BERSERK_FURY.getPosition();
            array6[position6] |= MapleBuffStat.BERSERK_FURY.getValue();
        }
        if (chr.getBuffedValue(MapleBuffStat.MORPH) != null) {
            final int[] array7 = mask;
            final int position7 = MapleBuffStat.MORPH.getPosition();
            array7[position7] |= MapleBuffStat.MORPH.getValue();
            buffvalue.add(new Pair<Integer, Integer>(chr.getBuffedValue(MapleBuffStat.MORPH), Integer.valueOf(2)));
        }
        for (int i = 0; i < mask.length; ++i) {
            mplew.writeInt(mask[i]);
        }
        for (final Pair j : buffvalue) {
            if ((int)(Integer)j.right == 3) {
                mplew.writeInt((int)(Integer)j.left);
            }
            else if ((int)(Integer)j.right == 2) {
                mplew.writeShort((int)((Integer)j.left).shortValue());
            }
            else {
                if ((int)(Integer)j.right != 1) {
                    continue;
                }
                mplew.write(((Integer)j.left).byteValue());
            }
        }
        mplew.write(0);
        mplew.write(0);
        final int CHAR_MAGIC_SPAWN = Randomizer.nextInt();
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeShort(0);
        mplew.write(0);
        final int buffSrc = chr.getBuffSource(MapleBuffStat.MONSTER_RIDING);
        if (buffSrc > 0) {
            final IItem c_mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-118));
            final IItem mount = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-18));
            if (GameConstants.getMountItem(buffSrc) == 0 && c_mount != null) {
                mplew.writeInt(c_mount.getItemId());
            }
            else if (GameConstants.getMountItem(buffSrc) == 0 && mount != null) {
                mplew.writeInt(mount.getItemId());
            }
            else {
                mplew.writeInt(GameConstants.getMountItem(buffSrc));
            }
            mplew.writeInt(buffSrc);
            mplew.writeInt(19275520);
            mplew.write(0);
        }
        else {
            mplew.writeInt(CHAR_MAGIC_SPAWN);
            mplew.writeLong(0L);
            mplew.write(0);
        }
        mplew.writeLong(0L);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.write(1);
        mplew.write(65);
        mplew.write(154);
        mplew.write(112);
        mplew.write(7);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeInt(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.writeLong(0L);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(CHAR_MAGIC_SPAWN);
        mplew.write(0);
        mplew.writeShort((int)chr.getJob());
        PacketHelper.addCharLook(mplew, chr, false);
        mplew.writeInt(Math.min(250, chr.getInventory(MapleInventoryType.CASH).countById(5110000)));
        mplew.writeInt(chr.getItemEffect());
        mplew.writeMapleAsciiString("");
        mplew.writeMapleAsciiString("");
        mplew.writeShort(-1);
        mplew.writeShort(-1);
        mplew.writeInt((GameConstants.getInventoryType(chr.getChair()) == MapleInventoryType.SETUP) ? chr.getChair() : 0);
        mplew.writePos(chr.getTruePosition());
        mplew.write(chr.getStance());
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt((int)chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt((int)chr.getMount().getFatigue());
        PacketHelper.addAnnounceBox(mplew, chr);
        mplew.write((int)((chr.getChalkboard() != null && chr.getChalkboard().length() > 0) ? 1 : 0));
        if (chr.getChalkboard() != null && chr.getChalkboard().length() > 0) {
            mplew.writeMapleAsciiString(chr.getChalkboard());
        }
        final Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        final List<MapleRing> allrings = (List<MapleRing>)rings.getLeft();
        allrings.addAll((Collection<? extends MapleRing>)(List<MapleRing>)rings.getRight());
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        addMarriageRingLook(mplew, chr);
        mplew.writeShort(0);
        if (chr.getCarnivalParty() != null) {
            mplew.write(chr.getCarnivalParty().getTeam());
        }
        else if (chr.getMapId() == 109080000 || chr.getMapId() == 109080010) {
            mplew.write(1);
        }
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    private static void addMarriageRingLook(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.write((byte)(byte)((chr.getMarriageRing(false) != null) ? 1 : 0));
        if (chr.getMarriageRing(false) != null) {
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing(false).getPartnerChrId());
        }
    }
    
    public static byte[] removePlayerFromMap(final int cid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_PLAYER_FROM_MAP.getValue());
        mplew.writeInt(cid);
        return mplew.getPacket();
    }
    
    public static byte[] facialExpression(final MapleCharacter from, final int expression) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FACIAL_EXPRESSION.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(expression);
        return mplew.getPacket();
    }

    /**
     * 移动角色
     * @param cid
     * @param moves
     * @param startPos
     * @return
     */
    public static byte[] movePlayer(final int cid, final List<LifeMovementFragment> moves, final Point startPos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(0);
        PacketHelper.serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    /**
     * 移动召唤兽
     * @param cid   角色id
     * @param oid   召唤兽id
     * @param startPos   起始位置
     * @param moves  移动路径
     * @return
     */
    public static byte[] moveSummon(final int cid, final int oid, final Point startPos, final List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(oid);
        mplew.writePos(startPos);
        PacketHelper.serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }

    /**
     * 召唤兽攻击
     * @param cid    角色id
     * @param summonSkillId 召唤技能id
     * @param animation 动画效果
     * @param allDamage 伤害
     * @param level 召唤者等级
     * @return
     */
    public static byte[] summonAttack(final int cid, final int summonSkillId, final byte animation, final List<SummonAttackEntry> allDamage, final int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SUMMON_ATTACK.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(level - 1);
        mplew.write(animation);
        mplew.write(allDamage.size());
        for (final SummonAttackEntry attackEntry : allDamage) {
            mplew.writeInt(attackEntry.getMonster().getObjectId());
            mplew.write(7);
            mplew.writeInt(attackEntry.getDamage());
        }
        return mplew.getPacket();
    }

    //近距离攻击封包发送
    /**
     * 构建近战攻击的数据包
     *
     * @param cid 角色ID
     * @param tbyte 时间字节
     * @param skill 技能ID
     * @param level 技能等级
     * @param display 显示类型
     * @param animation 动画类型
     * @param speed 攻击速度
     * @param damage 攻击伤害列表
     * @param energy 是否使用能量攻击
     * @param lvl 技能等级
     * @param mastery 熟练度
     * @param unk 未知字节
     * @param charge 充能值
     * @return 返回构建的近战攻击数据包
     */
    public static byte[] closeRangeAttack(final int cid, final int tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final List<AttackPair> damage, final boolean energy, final int lvl, final byte mastery, final byte unk, final int charge) {
        // 创建数据包写入器
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入攻击类型（根据是否使用能量攻击决定）
        mplew.writeShort((int)(energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue()));

        // 写入角色ID
        mplew.writeInt(cid);

        // 写入时间字节
        mplew.write(tbyte);

        // 写入技能等级
        mplew.write(lvl);

        // 如果技能ID大于0，则写入技能等级和技能ID
        if (skill > 0) {
            mplew.write(level);
            mplew.writeInt(skill);
        }
        else {
            // 否则写入0
            mplew.write(0);
        }

        // 写入未知字节
        mplew.write(unk);

        // 写入显示类型
        mplew.write(display);

        // 写入动画类型
        mplew.write(animation);

        // 写入攻击速度
        mplew.write(speed);

        // 写入熟练度
        mplew.write(mastery);

        // 写入固定值0
        mplew.writeInt(0);

        // 如果技能ID为特定值，则按照特殊格式写入攻击伤害
        if (skill == 4211006) {
            for (final AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(7);
                    mplew.write(oned.attack.size());
                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
                        mplew.writeInt((int)Integer.valueOf(eachd.left));
                    }
                }
            }
        }
        else {
            // 否则按照普通格式写入攻击伤害
            for (final AttackPair oned : damage) {
                if (oned.attack != null) {
                    mplew.writeInt(oned.objectid);
                    mplew.write(7);
                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
                        if ((boolean)Boolean.valueOf(eachd.right)) {
                            mplew.writeInt((int)Integer.valueOf(eachd.left) + Integer.MIN_VALUE);
                        }
                        else {
                            mplew.writeInt((int)Integer.valueOf(eachd.left));
                        }
                    }
                }
            }
        }

        // 返回构建的数据包
        return mplew.getPacket();
    }


//    public static byte[] closeRangeAttack(final int cid, final int tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final List<AttackPair> damage, final boolean energy, final int lvl, final byte mastery, final byte unk, final int charge) {
//        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//        mplew.writeShort((int)(energy ? SendPacketOpcode.ENERGY_ATTACK.getValue() : SendPacketOpcode.CLOSE_RANGE_ATTACK.getValue()));
//        mplew.writeInt(cid);
//        mplew.write(tbyte);
//        mplew.write(lvl);
//        if (skill > 0) {
//            mplew.write(level);
//            mplew.writeInt(skill);
//        }
//        else {
//            mplew.write(0);
//        }
//        mplew.write(unk);
//        mplew.write(display);
//        mplew.write(animation);
//        mplew.write(speed);
//        mplew.write(mastery);
//        mplew.writeInt(0);
//        if (skill == 4211006) {
//            for (final AttackPair oned : damage) {
//                if (oned.attack != null) {
//                    mplew.writeInt(oned.objectid);
//                    mplew.write(7);
//                    mplew.write(oned.attack.size());
//                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
//                        mplew.writeInt((int)Integer.valueOf(eachd.left));
//                    }
//                }
//            }
//        }
//        else {
//            for (final AttackPair oned : damage) {
//                if (oned.attack != null) {
//                    mplew.writeInt(oned.objectid);
//                    mplew.write(7);
//                    for (final Pair<Integer, Boolean> eachd : oned.attack) {
//                        if ((boolean)Boolean.valueOf(eachd.right)) {
//                            mplew.writeInt((int)Integer.valueOf(eachd.left) + Integer.MIN_VALUE);
//                        }
//                        else {
//                            mplew.writeInt((int)Integer.valueOf(eachd.left));
//                        }
//                    }
//                }
//            }
//        }
//        return mplew.getPacket();
//    }
    //远程攻击封包发送


    /**
     * 远程攻击封包发送
     * @param cid   角色id
     * @param tbyte 攻击类型
     * @param skill 技能id
     * @param level 攻击等级
     * @param display   显示效果
     * @param animation 动画效果
     * @param speed   速度
     * @param itemid     道具id
     * @param damage     伤害
     * @param pos        位置
     * @param lvl     角色等级
     * @param mastery    精通度
     * @param unk     未知
     * @return
     */
    public static byte[] rangedAttack(final int cid, final byte tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final int itemid, final List<AttackPair> damage, final Point pos, final int lvl, final byte mastery, final byte unk) {
        // 创建一个MaplePacketLittleEndianWriter实例用于写入数据
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入远程攻击封包的标识
        mplew.writeShort((int)SendPacketOpcode.RANGED_ATTACK.getValue());

        // 写入角色id
        mplew.writeInt(cid);

        // 写入攻击类型
        mplew.write(tbyte);

        // 写入角色等级
        mplew.write(lvl);

        // 如果技能id大于0，说明使用了技能，写入技能的相关信息
        if (skill > 0) {
            // 写入技能等级
            mplew.write(level);

            // 写入技能id
            mplew.writeInt(skill);
        }
        else {
            // 如果没有使用技能，写入0
            mplew.write(0);
        }

        // 写入未知数据
        mplew.write(unk);

        // 写入显示效果
        mplew.write(display);

        // 写入动画效果
        mplew.write(animation);

        // 写入速度
        mplew.write(speed);

        // 写入精通度
        mplew.write(mastery);

        // 写入道具id
        mplew.writeInt(itemid);

        // 遍历伤害列表，写入每个攻击对象的伤害信息
        for (final AttackPair oned : damage) {
            if (oned.attack != null) {
                // 写入攻击对象的id
                mplew.writeInt(oned.objectid);

                // 写入固定值7，可能是攻击类型或者其他含义
                mplew.write(7);

                // 遍历每个攻击对象的伤害，写入伤害信息
                for (final Pair<Integer, Boolean> eachd : oned.attack) {
                    if ((boolean)Boolean.valueOf(eachd.right)) {
                        // 如果伤害是真实的，写入伤害值加上Integer.MIN_VALUE
                        mplew.writeInt((int)Integer.valueOf(eachd.left) + Integer.MIN_VALUE);
                    }
                    else {
                        // 如果伤害是虚假的，直接写入伤害值
                        mplew.writeInt((int)Integer.valueOf(eachd.left));
                    }
                }
            }
        }

        // 写入位置信息
        mplew.writePos(pos);

        // 返回封包数据
        return mplew.getPacket();
    }

//    /**
//     * 远程攻击封包发送
//     * @param cid   角色id
//     * @param tbyte 攻击类型
//     * @param skill 技能id
//     * @param level 攻击等级
//     * @param display   显示效果
//     * @param animation 动画效果
//     * @param speed   速度
//     * @param itemid     道具id
//     * @param damage     伤害
//     * @param pos        位置
//     * @param lvl     角色等级
//     * @param mastery    精通度
//     * @param unk     未知
//     * @return
//     */
//    public static byte[] rangedAttack(final int cid, final byte tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final int itemid, final List<AttackPair> damage, final Point pos, final int lvl, final byte mastery, final byte unk) {
//        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//        mplew.writeShort((int)SendPacketOpcode.RANGED_ATTACK.getValue());
//        mplew.writeInt(cid);
//        mplew.write(tbyte);
//        mplew.write(lvl);
//        if (skill > 0) {
//            mplew.write(level);
//            mplew.writeInt(skill);
//        }
//        else {
//            mplew.write(0);
//        }
//        mplew.write(unk);
//        mplew.write(display);
//        mplew.write(animation);
//        mplew.write(speed);
//        mplew.write(mastery);
//        mplew.writeInt(itemid);
//        for (final AttackPair oned : damage) {
//            if (oned.attack != null) {
//                mplew.writeInt(oned.objectid);
//                mplew.write(7);
//                for (final Pair<Integer, Boolean> eachd : oned.attack) {
//                    if ((boolean)Boolean.valueOf(eachd.right)) {
//                        mplew.writeInt((int)Integer.valueOf(eachd.left) + Integer.MIN_VALUE);
//                    }
//                    else {
//                        mplew.writeInt((int)Integer.valueOf(eachd.left));
//                    }
//                }
//            }
//        }
//        mplew.writePos(pos);
//        return mplew.getPacket();
//    }

    //魔法攻击封包发送

    /**
     *  魔法攻击封包发送
     * @param cid     角色id
     * @param tbyte   攻击类型
     * @param skill   技能id
     * @param level   攻击等级
     * @param display   显示效果
     * @param animation 动画效果
     * @param speed   速度
     * @param damage     伤害
     * @param charge    充能
     * @param lvl     角色等级
     * @param unk     未知
     * @return
     */
    public static byte[] magicAttack(final int cid, final int tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final List<AttackPair> damage, final int charge, final int lvl, final byte unk) {
        // 创建一个MaplePacketLittleEndianWriter对象用于写入数据
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入封包标识
        mplew.writeShort((int)SendPacketOpcode.MAGIC_ATTACK.getValue());
        // 写入角色id
        mplew.writeInt(cid);
        // 写入攻击类型
        mplew.write(tbyte);
        // 写入角色等级
        mplew.write(lvl);
        // 写入攻击等级
        mplew.write(level);
        // 写入技能id
        mplew.writeInt(skill);
        // 写入未知参数
        mplew.write(unk);
        // 写入显示效果
        mplew.write(display);
        // 写入动画效果
        mplew.write(animation);
        // 写入速度
        mplew.write(speed);
        // 保留字段，目前未知用途
        mplew.write(0);
        // 保留字段，目前未知用途
        mplew.writeInt(0);
        // 遍历伤害列表
        for (final AttackPair oned : damage) {
            // 如果伤害对象不为空
            if (oned.attack != null) {
                // 写入对象id
                mplew.writeInt(oned.objectid);
                // 写入攻击类型标识
                mplew.write(-1);
                // 遍历每个伤害值
                for (final Pair<Integer, Boolean> eachd : oned.attack) {
                    // 如果伤害值带有特殊标识
                    if ((boolean)Boolean.valueOf(eachd.right)) {
                        // 写入经过特殊处理的伤害值
                        mplew.writeInt((int)Integer.valueOf(eachd.left) + Integer.MIN_VALUE);
                    }
                    else {
                        // 写入普通伤害值
                        mplew.writeInt((int)Integer.valueOf(eachd.left));
                    }
                }
            }
        }
        // 如果充能值大于0
        if (charge > 0) {
            // 写入充能值
            mplew.writeInt(charge);
        }
        // 返回封包数据
        return mplew.getPacket();
    }
//    public static byte[] magicAttack(final int cid, final int tbyte, final int skill, final int level, final byte display, final byte animation, final byte speed, final List<AttackPair> damage, final int charge, final int lvl, final byte unk) {
//        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
//        mplew.writeShort((int)SendPacketOpcode.MAGIC_ATTACK.getValue());
//        mplew.writeInt(cid);
//        mplew.write(tbyte);
//        mplew.write(lvl);
//        mplew.write(level);
//        mplew.writeInt(skill);
//        mplew.write(unk);
//        mplew.write(display);
//        mplew.write(animation);
//        mplew.write(speed);
//        mplew.write(0);
//        mplew.writeInt(0);
//        for (final AttackPair oned : damage) {
//            if (oned.attack != null) {
//                mplew.writeInt(oned.objectid);
//                mplew.write(-1);
//                for (final Pair<Integer, Boolean> eachd : oned.attack) {
//                    if ((boolean)Boolean.valueOf(eachd.right)) {
//                        mplew.writeInt((int)Integer.valueOf(eachd.left) + Integer.MIN_VALUE);
//                    }
//                    else {
//                        mplew.writeInt((int)Integer.valueOf(eachd.left));
//                    }
//                }
//            }
//        }
//        if (charge > 0) {
//            mplew.writeInt(charge);
//        }
//        return mplew.getPacket();
//    }
    
    public static byte[] getNPCShop(final MapleClient c, final int sid, final List<MapleShopItem> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.writeShort((int)SendPacketOpcode.OPEN_NPC_SHOP.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(items.size());
        for (final MapleShopItem item : items) {
            mplew.writeInt(item.getItemId());
            mplew.writeInt(item.getPrice());
            if (!GameConstants.isThrowingStar(item.getItemId()) && !GameConstants.isBullet(item.getItemId())) {
                mplew.writeShort(1);
                mplew.writeShort((int)item.getBuyable());
            }
            else {
                mplew.writeZeroBytes(6);
                mplew.writeShort(BitTools.doubleToShortBits(ii.getPrice(item.getItemId())));
                mplew.writeShort((int)ii.getSlotMax(c, item.getItemId()));
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] confirmShopTransaction(final byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CONFIRM_SHOP_TRANSACTION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }
    
    public static byte[] modifyInventory(final boolean updateTick, final ModifyInventory mod) {
        return modifyInventory(updateTick, Collections.singletonList(mod));
    }
    /**
     * 修改库存物品的方法
     *
     * @param updateTick 是否更新Tick，用于指示是否在游戏中的一个特定时间点进行更新
     * @param mods 一个包含修改库存操作的列表，每个操作描述了库存的一个更改
     * @return 返回一个包含修改库存指令的字节数组
     *
     * 该方法主要用于构建一个网络包，用于在游戏服务器和客户端之间传输库存修改信息
     * 它根据提供的修改操作列表和更新Tick标志来构造一个特定格式的字节流
     */
    public static byte[] modifyInventory(final boolean updateTick, final List<ModifyInventory> mods) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write((int)(updateTick ? 1 : 0));
        mplew.write(mods.size());
        int addMovement = -1;
        for (final ModifyInventory mod : mods) {
            mplew.write(mod.getMode());
            mplew.write(mod.getInventoryType());
            mplew.writeShort((int)((mod.getMode() == 2) ? mod.getOldPosition() : mod.getPosition()));
            switch (mod.getMode()) {
                case 0: {
                    PacketHelper.addItemInfo(mplew, mod.getItem(), true, false);
                    break;
                }
                case 1: {
                    mplew.writeShort((int)mod.getQuantity());
                    break;
                }
                case 2: {
                    mplew.writeShort((int)mod.getPosition());
                    if (mod.getPosition() < 0 || mod.getOldPosition() < 0) {
                        addMovement = ((mod.getOldPosition() < 0) ? 1 : 2);
                        break;
                    }
                    break;
                }
                case 3: {
                    if (mod.getPosition() < 0) {
                        addMovement = 2;
                        break;
                    }
                    break;
                }
            }
            mod.clear();
        }
        if (addMovement > -1) {
            mplew.write(addMovement);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getScrollEffect(final int chr, final ScrollResult scrollSuccess, final boolean legendarySpirit) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_SCROLL_EFFECT.getValue());
        mplew.writeInt(chr);
        switch (scrollSuccess) {
            case SUCCESS: {
                mplew.writeShort(1);
                mplew.writeShort((int)(legendarySpirit ? 1 : 0));
                break;
            }
            case FAIL: {
                mplew.writeShort(0);
                mplew.writeShort((int)(legendarySpirit ? 1 : 0));
                break;
            }
            case CURSE: {
                mplew.write(0);
                mplew.write(1);
                mplew.writeShort((int)(legendarySpirit ? 1 : 0));
                break;
            }
        }
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] getPotentialEffect(final int chr, final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_POTENTIAL_EFFECT.getValue());
        mplew.writeInt(chr);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }
    
    public static byte[] getPotentialReset(final int chr, final short pos) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_POTENTIAL_RESET.getValue());
        mplew.writeInt(chr);
        mplew.writeShort((int)pos);
        return mplew.getPacket();
    }
    
    public static byte[] ItemMaker_Success() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(17);
        mplew.writeZeroBytes(4);
        return mplew.getPacket();
    }
    
    public static byte[] ItemMaker_Success_3rdParty(final int from_playerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(from_playerid);
        mplew.write(17);
        mplew.writeZeroBytes(4);
        return mplew.getPacket();
    }
    
    public static byte[] explodeDrop(final int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(4);
        mplew.writeInt(oid);
        mplew.writeShort(655);
        return mplew.getPacket();
    }
    
    public static byte[] removeItemFromMap( int oid,  int animation,  int cid) {
        return removeItemFromMap(oid, animation, cid, 0);
    }
    /**
     * 从地图上移除物品的函数
     *
     * @param oid 物品的唯一标识符
     * @param animation 移除物品时的动画效果类型
     * @param cid 角色的标识符，用于确认移除物品的角色
     * @param slot 物品所在的槽位，仅当动画效果类型为5时需要
     * @return 返回包含移除物品指令的字节数组
     *
     * 此函数根据提供的参数构建一个移除地图上物品的指令包不同类型的动画效果需要不同的参数配置
     */
    public static byte[] removeItemFromMap(final int oid, final int animation, final int cid, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_ITEM_FROM_MAP.getValue());
        mplew.write(animation);
        mplew.writeInt(oid);
        if (animation >= 2) {
            mplew.writeInt(cid);
            if (animation == 5) {
                mplew.write(slot);
            }
        }
        return mplew.getPacket();
    }
    /**
     * 更新角色外观信息
     * 该方法用于生成更新角色外观的数据包，包括角色的装备、戒指等信息
     *
     * @param chr 需要更新外观的角色对象
     * @return 包含更新角色外观信息的数据包
     */
    public static byte[] updateCharLook(MapleCharacter chr) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        // 添加角色外观信息到数据包
        PacketHelper.addCharLook(mplew, chr, false);
        // 获取角色的戒指信息
        Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        List<MapleRing> allrings = (List)rings.getLeft();
        allrings.addAll((Collection)rings.getRight());
        // 添加戒指信息到数据包
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        // 添加婚姻戒指外观信息到数据包
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    /**
     * 更新角色外观信息，可选择是否显示装备
     * 该方法用于生成更新角色外观的数据包，包括角色的装备、戒指等信息，并允许选择是否显示装备
     *
     * @param chr 需要更新外观的角色对象
     * @param isShowEquip 是否显示装备，true显示，false不显示
     * @return 包含更新角色外观信息的数据包
     */
    public static byte[] updateCharLook(MapleCharacter chr, boolean isShowEquip) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.UPDATE_CHAR_LOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        PacketHelper.addCharLook(mplew, chr, false, isShowEquip);
        Pair<List<MapleRing>, List<MapleRing>> rings = chr.getRings(false);
        List<MapleRing> allrings = (List)rings.getLeft();
        allrings.addAll((Collection)rings.getRight());
        addRingInfo(mplew, allrings);
        addRingInfo(mplew, allrings);
        addMarriageRingLook(mplew, chr);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     *   增加戒指信息
     * @param mplew
     * @param rings
     */
    public static void addRingInfo(final MaplePacketLittleEndianWriter mplew, final List<MapleRing> rings) {
        mplew.write((int)((rings.size() > 0) ? 1 : 0));
        if (rings.size() > 0) {
            mplew.writeInt(rings.size());
            for (final MapleRing ring : rings) {
                mplew.writeLong((long)ring.getRingId());
                mplew.writeLong((long)ring.getPartnerRingId());
                mplew.writeInt(ring.getItemId());
            }
        }
    }
    
    public static byte[] dropInventoryItem(final MapleInventoryType type, final short src) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 03"));
        mplew.write(type.getType());
        mplew.writeShort((int)src);
        if (src < 0) {
            mplew.write(1);
        }
        return mplew.getPacket();
    }
    
    public static byte[] dropInventoryItemUpdate(final MapleInventoryType type, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("01 01 01"));
        mplew.write(type.getType());
        mplew.writeShort((int)item.getPosition());
        mplew.writeShort((int)item.getQuantity());
        return mplew.getPacket();
    }

    /**
     * 伤害角色
     * @param skill 技能id
     * @param monsteridfrom  攻击者id
     * @param cid            被攻击者id
     * @param damage         伤害值
     * @param fake           假伤害
     * @param direction      方向
     * @param reflect        反弹伤害
     * @param is_pg          是否是超能力
     * @param oid            伤害对象id
     * @param pos_x          伤害位置x
     * @param pos_y          伤害位置y
     * @return
     */
    public static byte[] damagePlayer(final int skill, final int monsteridfrom, final int cid, final int damage, final int fake, final byte direction, final int reflect, final boolean is_pg, final int oid, final int pos_x, final int pos_y) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DAMAGE_PLAYER.getValue());
        mplew.writeInt(cid);
        mplew.write(skill);
        mplew.writeInt(damage);
        mplew.writeInt(monsteridfrom);
        mplew.write(direction);
        if (reflect > 0) {
            mplew.write(reflect);
            mplew.write((int)(is_pg ? 1 : 0));
            mplew.writeInt(oid);
            mplew.write(6);
            mplew.writeShort(pos_x);
            mplew.writeShort(pos_y);
            mplew.write(0);
        }
        else {
            mplew.writeShort(0);
        }
        mplew.writeInt(damage);
        if (fake > 0) {
            mplew.writeInt(fake);
        }
        return mplew.getPacket();
    }
    
    public static byte[] updateQuest(final MapleQuestStatus quest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(quest.getQuest().getId());
        mplew.write(quest.getStatus());
        switch (quest.getStatus()) {
            case 0: {
                mplew.writeZeroBytes(10);
                break;
            }
            case 1: {
                mplew.writeMapleAsciiString((quest.getCustomData() != null) ? quest.getCustomData() : "");
                break;
            }
            case 2: {
                mplew.writeLong(PacketHelper.getTime(System.currentTimeMillis()));
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] updateInfoQuest(final int quest, final String data) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(10);
        mplew.writeShort(quest);
        mplew.writeMapleAsciiString(data);
        return mplew.getPacket();
    }
    
    public static byte[] updateQuestInfo(final MapleCharacter c, final int quest, final int npc, final byte progress) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(progress);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] updateQuestFinish(final int quest, final int npc, final int nextquest) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_QUEST_INFO.getValue());
        mplew.write(8);
        mplew.writeShort(quest);
        mplew.writeInt(npc);
        mplew.writeInt(nextquest);
        return mplew.getPacket();
    }
    
    public static byte[] charInfo(MapleCharacter chr, final boolean isSelf) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CHAR_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write((int)chr.getLevel());
        mplew.writeShort((int)chr.getJob());
        mplew.writeShort((int)chr.getFame());
        mplew.write((int)((chr.getMarriageId() > 0) ? 1 : 0));
        final String Prefix = chr.getPrefix();
        if (chr.getGuildId() <= 0) {
            if (chr.getPrefix().equals((Object)"")) {
                mplew.writeMapleAsciiString("尚未加入公会");
                mplew.writeMapleAsciiString("尚未加入联盟");
            }
            else {
                mplew.writeMapleAsciiString(Prefix);
                mplew.writeMapleAsciiString("尚未加入联盟");
            }
        }
        else {
            final MapleGuild gs = Guild.getGuild(chr.getGuildId());
            if (gs != null) {
                mplew.writeMapleAsciiString(gs.getName());
                if (gs.getAllianceId() > 0) {
                    final MapleGuildAlliance allianceName = Alliance.getAlliance(gs.getAllianceId());
                    if (allianceName != null) {
                        mplew.writeMapleAsciiString(allianceName.getName());
                    }
                    else if (chr.getPrefix().equals((Object)"")) {
                        mplew.writeMapleAsciiString("尚未加入联盟");
                    }
                    else {
                        mplew.writeMapleAsciiString(Prefix);
                    }
                }
                else if (chr.getPrefix().equals((Object)"")) {
                    mplew.writeMapleAsciiString("尚未加入联盟");
                }
                else {
                    mplew.writeMapleAsciiString(Prefix);
                }
            }
            else if (chr.getPrefix().equals((Object)"")) {
                mplew.writeMapleAsciiString("尚未加入公会");
                mplew.writeMapleAsciiString("尚未加入联盟");
            }
            else {
                mplew.writeMapleAsciiString(Prefix);
                mplew.writeMapleAsciiString("尚未加入联盟");
            }
        }
        byte index = 1;
        for (final MaplePet pet : chr.getSummonedPets()) {
            if (pet.getSummoned()) {
                mplew.write(pet.getSummonedValue());
                mplew.writeInt(pet.getPetItemId());
                mplew.writeMapleAsciiString(pet.getName());
                mplew.write(pet.getLevel());
                mplew.writeShort((int)pet.getCloseness());
                mplew.write(pet.getFullness());
                mplew.writeShort((int)pet.getFlags());
                final IItem inv = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(byte)((index == 2) ? -122 : ((index == 1) ? -114 : -138)));
                mplew.writeInt((inv == null) ? 0 : inv.getItemId());
                ++index;
            }
        }
        mplew.write(0);
        if (chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-18)) != null) {
            final int itemid = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-18)).getItemId();
            final MapleMount mount = chr.getMount();
            final boolean canwear = MapleItemInformationProvider.getInstance().getReqLevel(itemid) <= chr.getLevel();
            mplew.write((int)(canwear ? 1 : 0));
            if (canwear) {
                mplew.writeInt((int)mount.getLevel());
                mplew.writeInt(mount.getExp());
                mplew.writeInt((int)mount.getFatigue());
            }
        }
        else {
            mplew.write(0);
        }
        final int wishlistSize = chr.getWishlistSize();
        mplew.write(wishlistSize);
        if (wishlistSize > 0) {
            final int[] wishlist = chr.getWishlist();
            for (int x = 0; x < wishlistSize; ++x) {
                mplew.writeInt(wishlist[x]);
            }
        }
        chr.getMonsterBook().addCharInfoPacket(chr.getMonsterBookCover(), mplew);
        final IItem medal = chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-49));
        mplew.writeInt((medal == null) ? 0 : medal.getItemId());
        final List<Integer> medalQuests = new ArrayList<Integer>();
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        for (final MapleQuestStatus q : completed) {
            if (q.getQuest().getMedalItem() > 0 && GameConstants.getInventoryType(q.getQuest().getMedalItem()) == MapleInventoryType.EQUIP) {
                medalQuests.add(Integer.valueOf(q.getQuest().getId()));
            }
        }
        mplew.writeShort(medalQuests.size());
        final Iterator<Integer> iterator3 = medalQuests.iterator();
        while (iterator3.hasNext()) {
            final int x2 = (int)Integer.valueOf(iterator3.next());
            mplew.writeShort(x2);
        }
        final MapleInventory iv = chr.getInventory(MapleInventoryType.SETUP);
        final List<Item> chairItems = new ArrayList<Item>();
        for (final IItem item : iv.list()) {
            if (item.getItemId() >= 3010000 && item.getItemId() <= 3020001) {
                chairItems.add((Item)item);
            }
        }
        mplew.writeInt(chairItems.size());
        for (final IItem item : chairItems) {
            mplew.writeInt(item.getItemId());
        }
        final MapleInventory medals = chr.getInventory(MapleInventoryType.EQUIP);
        final List<Item> medalsItems = new ArrayList<Item>();
        for (final IItem item2 : medals.list()) {
            if (item2.getItemId() >= 1142000 && item2.getItemId() <= 1142999) {
                medalsItems.add((Item)item2);
            }
        }
        mplew.writeInt(medalsItems.size());
        for (final IItem item2 : medalsItems) {
            mplew.writeInt(item2.getItemId());
        }
        return mplew.getPacket();
    }
    
    private static void writeLongDiseaseMask(final MaplePacketLittleEndianWriter mplew, final List<Pair<MapleDisease, Integer>> statups) {
        long firstmask = 0L;
        long secondmask = 0L;
        for (final Pair<MapleDisease, Integer> statup : statups) {
            if (((MapleDisease)statup.getLeft()).isFirst()) {
                firstmask |= ((MapleDisease)statup.getLeft()).getValue();
            }
            else {
                secondmask |= ((MapleDisease)statup.getLeft()).getValue();
            }
        }
        mplew.writeLong(firstmask);
        mplew.writeLong(secondmask);
    }
    
    private static void writeBuffState(final MaplePacketLittleEndianWriter mplew, final List<MapleBuffStat> statups) {
        final int[] mask = new int[4];
        for (final MapleBuffStat statup : statups) {
            final int[] array = mask;
            final int position = statup.getPosition();
            array[position] |= statup.getValue();
        }
        for (int i = 0; i < mask.length; ++i) {
            mplew.writeInt(mask[i]);
        }
    }
    
    private static void writeBuffState2(final MaplePacketLittleEndianWriter mplew, final List<Pair<MapleBuffStat, Integer>> statups) {
        final int[] mask = new int[4];
        for (final Pair<MapleBuffStat, Integer> statup : statups) {
            final int[] array = mask;
            final int position = ((MapleBuffStat)statup.getLeft()).getPosition();
            array[position] |= ((MapleBuffStat)statup.getLeft()).getValue();
        }
        for (int i = 0; i < mask.length; ++i) {
            mplew.writeInt(mask[i]);
        }
    }
    
    public static byte[] giveMount(final int buffid, final int skillid, final List<Pair<MapleBuffStat, Integer>> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        writeBuffState2(mplew, statups);
        mplew.writeShort(0);
        mplew.writeInt(buffid);
        mplew.writeInt(skillid);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(2);
        return mplew.getPacket();
    }
    
    public static byte[] givePirate(final List<Pair<MapleBuffStat, Integer>> statups, final int duration, final int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        writeBuffState2(mplew, statups);
        mplew.writeShort(0);
        for (final Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt((int)Integer.valueOf(stat.getRight()));
            mplew.writeLong((long)skillid);
            mplew.writeZeroBytes(infusion ? 6 : 1);
            mplew.writeShort(duration);
        }
        mplew.writeShort(infusion ? 600 : 0);
        if (!infusion) {
            mplew.write(1);
        }
        return mplew.getPacket();
    }
    
    public static byte[] giveForeignPirate(final List<Pair<MapleBuffStat, Integer>> statups, final int duration, final int cid, final int skillid) {
        final boolean infusion = skillid == 5121009 || skillid == 15111005;
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeBuffState2(mplew, statups);
        mplew.writeShort(0);
        for (final Pair<MapleBuffStat, Integer> stat : statups) {
            mplew.writeInt((int)Integer.valueOf(stat.getRight()));
            mplew.writeLong((long)skillid);
            mplew.writeZeroBytes(infusion ? 7 : 1);
            mplew.writeShort(duration);
        }
        mplew.writeShort(infusion ? 600 : 0);
        return mplew.getPacket();
    }
    
    public static byte[] giveHoming(final int skillid, final int mobid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        final List<MapleBuffStat> st = new ArrayList<MapleBuffStat>();
        st.add(MapleBuffStat.HOMING_BEACON);
        writeBuffState(mplew, st);
        mplew.writeShort(0);
        mplew.writeInt(1);
        mplew.writeLong((long)skillid);
        mplew.write(0);
        mplew.writeInt(mobid);
        mplew.writeShort(0);
        return mplew.getPacket();
    }
    
    public static byte[] giveEnergyChargeTest(final int bar, final int bufflength) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        final List<MapleBuffStat> st = new ArrayList<MapleBuffStat>();
        st.add(MapleBuffStat.ENERGY_CHARGE);
        writeBuffState(mplew, st);
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000));
        mplew.writeLong(0L);
        mplew.write(0);
        mplew.writeInt((bar >= 10000) ? bufflength : 0);
        return mplew.getPacket();
    }
    
    public static byte[] giveEnergyChargeTest(final int cid, final int bar, final int bufflength) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        final List<MapleBuffStat> st = new ArrayList<MapleBuffStat>();
        st.add(MapleBuffStat.ENERGY_CHARGE);
        writeBuffState(mplew, st);
        mplew.writeShort(0);
        mplew.writeInt(Math.min(bar, 10000));
        mplew.writeLong(0L);
        mplew.write(0);
        mplew.writeInt((bar >= 10000) ? bufflength : 0);
        return mplew.getPacket();
    }

    /**
     * 给物品buff  潜能buff用的这个
     * @param buffid
     * @param bufflength
     * @param statups
     * @param effect
     * @return
     */
    public static byte[] giveBuff(final int buffid, final int bufflength, final List<Pair<MapleBuffStat, Integer>> statups, final MapleStatEffect effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        writeBuffState2(mplew, statups);
        for (final Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort((int)Integer.valueOf(statup.getRight()).shortValue());
            mplew.writeInt(buffid);
            mplew.writeInt(bufflength);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        if (effect == null || (!effect.isCombo() && !effect.isFinalAttack())) {
            mplew.write(0);
        }
        return mplew.getPacket();
    }
    
    public static byte[] hiredMerchantBox() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ENTRUSTED_SHOP_CHECK_RESULT.getValue());
        mplew.write(7);
        return mplew.getPacket();
    }
    
    public static byte[] giveDebuff(final List<Pair<MapleDisease, Integer>> statups, final int skillid, final int level, final int duration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_BUFF.getValue());
        writeLongDiseaseMask(mplew, statups);
        for (final Pair<MapleDisease, Integer> statup : statups) {
            mplew.writeShort((int)Integer.valueOf(statup.getRight()).shortValue());
            mplew.writeShort(skillid);
            mplew.writeShort(level);
            mplew.writeInt(duration);
        }
        mplew.writeShort(0);
        mplew.writeShort(900);
        mplew.write(1);
        return mplew.getPacket();
    }

    /**
     * 给异常BUFF
     * @param cid
     * @param statups
     * @param skillid
     * @param level
     * @return
     */
    public static byte[] giveForeignDebuff(final int cid, final List<Pair<MapleDisease, Integer>> statups, final int skillid, final int level) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeLongDiseaseMask(mplew, statups);
        if (skillid == 125) {
            mplew.writeShort(0);
        }
        mplew.writeShort(skillid);
        mplew.writeShort(level);
        mplew.writeShort(0);
        mplew.writeShort(900);
        return mplew.getPacket();
    }
    
    public static byte[] cancelForeignDebuff(final int cid, final long mask, final boolean first) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        mplew.writeLong(first ? mask : 0L);
        mplew.writeLong(first ? 0L : mask);
        return mplew.getPacket();
    }
    
    public static byte[] showMonsterRiding(final int cid, final List<Pair<MapleBuffStat, Integer>> statups, final int itemId, final int skillId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeBuffState2(mplew, statups);
        mplew.writeShort(0);
        mplew.writeInt(itemId);
        mplew.writeInt(skillId);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    //给变身Buff
    public static byte[] giveForeignBuff(final int cid, final List<Pair<MapleBuffStat, Integer>> statups, final MapleStatEffect effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GIVE_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeBuffState2(mplew, statups);
        for (final Pair<MapleBuffStat, Integer> statup : statups) {
            mplew.writeShort((int)Integer.valueOf(statup.getRight()).shortValue());
        }
        mplew.writeShort(0);// same as give_buff
        if (effect.isMorph() && (!effect.isPirateMorph())) {//判断是道具还是技能
            mplew.write(0);
        }
        mplew.write(0);
        mplew.write(0); 
        return mplew.getPacket();
    }
    
    public static byte[] cancelForeignBuff(final int cid, final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_FOREIGN_BUFF.getValue());
        mplew.writeInt(cid);
        writeBuffState(mplew, statups);
        return mplew.getPacket();
    }

    /**
     * 取消buff
     * @param statups
     * @return
     */
    public static byte[] cancelBuff(final List<MapleBuffStat> statups) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_BUFF.getValue());
        if (statups != null) {
            writeBuffState(mplew, statups);
            mplew.write(3);
        }
        else {
            mplew.writeLong(0L);
            mplew.writeInt(64);
            mplew.writeInt(4096);
        }
        return mplew.getPacket();
    }

    /**
     * 取消归零
     * @return
     */
    public static byte[] cancelHoming() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong((long)MapleBuffStat.HOMING_BEACON.getValue());
        mplew.writeLong(0L);
        return mplew.getPacket();
    }

    /**
     * 移除debuff
     * @param mask
     * @param first
     * @return
     */
    public static byte[] cancelDebuff(final long mask, final boolean first) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_BUFF.getValue());
        mplew.writeLong(first ? mask : 0L);
        mplew.writeLong(first ? 0L : mask);
        mplew.write(1);
        return mplew.getPacket();
    }
    
    public static byte[] updateMount(MapleCharacter chr, final boolean levelup) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SET_TAMING_MOB_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt((int)chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt((int)chr.getMount().getFatigue());
        mplew.write((int)(levelup ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] mountInfo(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SET_TAMING_MOB_INFO.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt((int)chr.getMount().getLevel());
        mplew.writeInt(chr.getMount().getExp());
        mplew.writeInt((int)chr.getMount().getFatigue());
        return mplew.getPacket();
    }
    
    public static byte[] getPlayerShopNewVisitor(final MapleCharacter c, final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("04 0" + slot));
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }
    
    public static byte[] getPlayerShopRemoveVisitor(final int slot) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(HexTool.getByteArrayFromHexString("0A 0" + slot));
        return mplew.getPacket();
    }

    /**
     * 获取对方的交易添加物品
     * @param c
     * @return
     */
    public static byte[] getTradePartnerAdd(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(4);
        mplew.write(1);
        PacketHelper.addCharLook(mplew, c, false);
        mplew.writeMapleAsciiString(c.getName());
        return mplew.getPacket();
    }

    /**
     * 发起交易
     * @param c
     * @return
     */
    public static byte[] getTradeInvite(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(2);
        mplew.write(3);
        mplew.writeMapleAsciiString(c.getName());
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * 交易金额设置
     * @param number
     * @param meso
     * @return
     */
    public static byte[] getTradeMesoSet(final byte number, final int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(15);
        mplew.write(number);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    /**
     * 交易物品添加
     * @param number
     * @param item
     * @return
     */
    public static byte[] getTradeItemAdd(final byte number, final IItem item) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(14);
        mplew.write(number);
        PacketHelper.addItemInfo(mplew, item, false, false);
        return mplew.getPacket();
    }

    /**
     * 交易开始
     * @param c
     * @param trade
     * @param number
     * @return
     */
    public static byte[] getTradeStart(final MapleClient c, final MapleTrade trade, final byte number) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(5);
        mplew.write(3);
        mplew.write(2);
        mplew.write(number);
        if (number == 1) {
            mplew.write(0);
            PacketHelper.addCharLook(mplew, trade.getPartner().getChr(), false);
            mplew.writeMapleAsciiString(trade.getPartner().getChr().getName());
        }
        mplew.write(number);
        PacketHelper.addCharLook(mplew, c.getPlayer(), false);
        mplew.writeMapleAsciiString(c.getPlayer().getName());
        mplew.write(255);
        return mplew.getPacket();
    }

    /**
     * 获取交易确认
     * @return
     */
    public static byte[] getTradeConfirmation() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(16);
        return mplew.getPacket();
    }

    /**
     * 交易消息
     * @param UserSlot
     * @param message
     * @return
     */
    public static byte[] TradeMessage(final byte UserSlot, final byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(UserSlot);
        mplew.write(message);
        return mplew.getPacket();
    }
    
    public static byte[] getTradeCancel(final byte UserSlot, final int unsuccessful) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PLAYER_INTERACTION.getValue());
        mplew.write(10);
        mplew.write(UserSlot);
        mplew.write((unsuccessful == 0) ? 2 : ((unsuccessful == 1) ? 9 : 10));
        return mplew.getPacket();
    }
    
    public static byte[] getNPCTalk(final int npc, final byte msgType, final String talk, final String endBytes, final byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.write(msgType);
        mplew.write(type);
        mplew.writeMapleAsciiString(talk);
        mplew.write(HexTool.getByteArrayFromHexString(endBytes));
        return mplew.getPacket();
    }

    /**
     * 地图选择
     * @param npcid
     * @param sel
     * @return
     */
    public static byte[] getMapSelection(final int npcid, final String sel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npcid);
        mplew.writeShort(13);
        mplew.writeInt(0);
        mplew.writeInt(5);
        mplew.writeMapleAsciiString(sel);
        return mplew.getPacket();
    }
    
    public static byte[] getNPCTalkStyle(final int npc, final String talk, final int... args) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(7);
        mplew.writeMapleAsciiString(talk);
        mplew.write(args.length);
        for (int i = 0; i < args.length; ++i) {
            mplew.writeInt(args[i]);
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    public static byte[] getNPCTalkStyle(int npc, String talk, int a, int... args) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        if (ServerConfig.version == 79) {
            mplew.writeShort(7);
        } else if (ServerConfig.version == 85) {
            mplew.writeShort(8);
        }

        mplew.writeMapleAsciiString(talk);
        mplew.write(args.length);

        for(int i = 0; i < args.length; ++i) {
            mplew.writeInt(args[i]);
        }

        mplew.writeInt(a);
        return mplew.getPacket();
    }

    public static byte[] getNPCTalkNum(final int npc, final String talk, final int def, final int min, final int max) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(3);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(def);
        mplew.writeInt(min);
        mplew.writeInt(max);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] getNPCTalkText(final int npc, final String talk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        mplew.writeShort(2);
        mplew.writeMapleAsciiString(talk);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    public static byte[] getNPCTalkText(int npc, String talk, String def) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.NPC_TALK.getValue());
        mplew.write(4);
        mplew.writeInt(npc);
        if (ServerConfig.version == 79) {
            mplew.writeShort(2);
        } else if (ServerConfig.version == 85) {
            mplew.writeShort(3);
        }

        mplew.writeMapleAsciiString(talk);
        mplew.writeMapleAsciiString(def);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    public static byte[] showForeignEffect(final int cid, final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        return mplew.getPacket();
    }
    
    public static byte[] showBuffeffect(final int cid, final int skillid, final int effectid, final int playerLevel, final int skillLevel) {
        return showBuffeffect(cid, skillid, effectid, playerLevel, skillLevel, (byte)3);
    }
    
    public static byte[] showBuffeffect(final int cid, final int skillid, final int effectid, final int playerLevel, final int skillLevel, final byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1);
        mplew.write(skillLevel);
        if (direction != 3) {
            mplew.write(direction);
        }
        mplew.writeZeroBytes(20);
        return mplew.getPacket();
    }
    
    public static byte[] showBuffeffect(final int cid, final int skillid, final int effectid) {
        return showBuffeffect(cid, skillid, effectid, (byte)3);
    }
    
    public static byte[] showBuffeffect(final int cid, final int skillid, final int effectid, final byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1);
        mplew.write(1);
        if (direction != 3) {
            mplew.write(direction);
        }
        return mplew.getPacket();
    }

    /**
     * 显示BUFF效果
     * @param skillid
     * @param effectid
     * @return
     */
    public static byte[] showOwnBuffEffect(final int skillid, final int effectid) {
        return showOwnBuffEffect(skillid, effectid, (byte)3);
    }

    /**
     * 显示BUFF效果
     * @param skillid
     * @param effectid
     * @param direction
     * @return
     */
    public static byte[] showOwnBuffEffect(final int skillid, final int effectid, final byte direction) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effectid);
        mplew.writeInt(skillid);
        mplew.write(1);
        mplew.write(1);
        if (direction != 3) {
            mplew.write(direction);
        }
        return mplew.getPacket();
    }

    /**
     * 显示物品升级效果
     * @return
     */
    public static byte[] showItemLevelupEffect() {
        return showSpecialEffect(17);
    }

    public static byte[] showForeignItemLevelupEffect(final int cid) {
        return showSpecialEffect(cid, 17);
    }

    /**
     * 显示特殊效果
     * @param effect
     * @return
     */
    public static byte[] showSpecialEffect(final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(effect);
        return mplew.getPacket();
    }

    /**
     * 显示特殊效果
     * @param cid
     * @param effect
     * @return
     */
    public static byte[] showSpecialEffect(final int cid, final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(effect);
        return mplew.getPacket();
    }
    
    public static byte[] updateSkill(final int skillid, final int level, final int masterlevel, final long expiration) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_SKILLS.getValue());
        mplew.write(1);
        mplew.writeShort(1);
        mplew.writeInt(skillid);
        mplew.writeInt(level);
        mplew.writeInt(masterlevel);
        PacketHelper.addExpirationTime(mplew, expiration);
        mplew.write(4);
        return mplew.getPacket();
    }
    
    public static byte[] updateQuestMobKills(final MapleQuestStatus status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(1);
        mplew.writeShort(status.getQuest().getId());
        mplew.write(1);
        final StringBuilder sb = new StringBuilder();
        final Iterator<Integer> iterator = status.getMobKills().values().iterator();
        while (iterator.hasNext()) {
            final int kills = (int)Integer.valueOf(iterator.next());
            sb.append(StringUtil.getLeftPaddedStr(String.valueOf(kills), '0', 3));
        }
        mplew.writeMapleAsciiString(sb.toString());
        mplew.writeZeroBytes(8);
        return mplew.getPacket();
    }

    /**
     * 显示任务完成信息
     * @param id
     * @return
     */
    public static byte[] getShowQuestCompletion(final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_QUEST_COMPLETION.getValue());
        mplew.writeShort(id);
        return mplew.getPacket();
    }

    /**
     * 获取键盘映射
     * @param layout
     * @return
     */
    public static byte[] getKeymap(final MapleKeyLayout layout) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.KEYMAP.getValue());
        mplew.write(0);
        layout.writeData(mplew);
        return mplew.getPacket();
    }

    /**
     * 宠物自动回复HP
     * @param itemId
     * @return
     */
    public static byte[] petAutoHP(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PET_AUTO_HP.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }

    /**
     * 宠物自动回复MP
     * @param itemId
     * @return
     */
    public static byte[] petAutoMP(final int itemId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PET_AUTO_MP.getValue());
        mplew.writeInt(itemId);
        return mplew.getPacket();
    }
    
    public static byte[] getWhisper(final String sender, final int channel, final String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.WHISPER.getValue());
        mplew.write(18);
        mplew.writeMapleAsciiString(sender);
        mplew.writeShort(channel - 1);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }
    
    public static byte[] getWhisperReply(final String target, final byte reply) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.WHISPER.getValue());
        mplew.write(10);
        mplew.writeMapleAsciiString(target);
        mplew.write(reply);
        return mplew.getPacket();
    }
    
    public static byte[] getFindReplyWithMap(final String target, final int mapid, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(1);
        mplew.writeInt(mapid);
        mplew.writeZeroBytes(8);
        return mplew.getPacket();
    }
    
    public static byte[] getFindReply(final String target, final int channel, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(3);
        mplew.writeInt(channel - 1);
        return mplew.getPacket();
    }
    
    public static byte[] getInventoryFull() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(1);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] getShowInventoryFull() {
        return getShowInventoryStatus(255);
    }
    
    public static byte[] showItemUnavailable() {
        return getShowInventoryStatus(254);
    }

    /**
     * 获取仓库状态
     * @param mode
     * @return
     */
    public static byte[] getShowInventoryStatus(final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(0);
        mplew.write(mode);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * 获取仓库物品
     * @param npcId
     * @param slots
     * @param items
     * @param meso
     * @return
     */
    public static byte[] getStorage(final int npcId, final byte slots, final Collection<IItem> items, final int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(22);
        mplew.writeInt(npcId);
        mplew.write(slots);
        mplew.writeShort(126);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        mplew.writeShort(0);
        mplew.write((byte)items.size());
        for (final IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        mplew.writeShort(0);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] getStorageFull() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(17);
        return mplew.getPacket();
    }

    /**
     * 存钱
     * @param slots
     * @param meso
     * @return
     */
    public static byte[] mesoStorage(final byte slots, final int meso) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(19);
        mplew.write(slots);
        mplew.writeShort(2);
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.writeInt(meso);
        return mplew.getPacket();
    }

    /**
     * 保存物品
     * @param slots
     * @param type
     * @param items
     * @return
     */
    public static byte[] storeStorage(final byte slots, final MapleInventoryType type, final Collection<IItem> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(13);
        mplew.write(slots);
        mplew.writeShort((int)type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (final IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        return mplew.getPacket();
    }

    /**
     * 取出物品
     * @param slots
     * @param type
     * @param items
     * @return
     */
    public static byte[] takeOutStorage(final byte slots, final MapleInventoryType type, final Collection<IItem> items) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(9);
        mplew.write(slots);
        mplew.writeShort((int)type.getBitfieldEncoding());
        mplew.writeShort(0);
        mplew.writeInt(0);
        mplew.write(items.size());
        for (final IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        return mplew.getPacket();
    }
    
    public static byte[] fairyPendantMessage(final int type, final int percent) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FAIRY_PEND_MSG.getValue());
        mplew.writeShort(21);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.writeShort(percent);
        mplew.writeShort(0);
        return mplew.getPacket();
    }
    
    public static byte[] giveFameResponse(final int mode, final String charname, final int newfame) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(charname);
        mplew.write(mode);
        mplew.writeShort(newfame);
        mplew.writeShort(0);
        return mplew.getPacket();
    }
    
    public static byte[] giveFameErrorResponse(final int status) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(status);
        return mplew.getPacket();
    }

    /**
     * 获得声望
     * @param mode
     * @param charnameFrom
     * @return
     */
    public static byte[] receiveFame(final int mode, final String charnameFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FAME_RESPONSE.getValue());
        mplew.write(5);
        mplew.writeMapleAsciiString(charnameFrom);
        mplew.write(mode);
        return mplew.getPacket();
    }

    /**
     * 创建队伍
     * @param partyid
     * @return
     */
    public static byte[] partyCreated(final int partyid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(8);
        mplew.writeInt(partyid);
        mplew.writeInt(999999999);
        mplew.writeInt(999999999);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * 组队邀请
     * @param from
     * @return
     */
    public static byte[] partyInvite(final MapleCharacter from) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(from.getParty().getId());
        mplew.writeMapleAsciiString(from.getName());
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 组队状态消息
     * @param message
     * @return
     */
    public static byte[] partyStatusMessage(final int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    /**
     * 组队状态消息-角色
     * @param message
     * @param charname
     * @return
     */
    public static byte[] partyStatusMessage(final int message, final String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(message);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }

    /**
     * 添加组队状态
     * @param forchannel
     * @param party
     * @param lew
     * @param leaving
     */
    private static void addPartyStatus(final int forchannel, final MapleParty party, final MaplePacketLittleEndianWriter lew, final boolean leaving) {
        final List<MaplePartyCharacter> partymembers = new ArrayList<MaplePartyCharacter>((Collection<? extends MaplePartyCharacter>)party.getMembers());
        while (partymembers.size() < 6) {
            partymembers.add(new MaplePartyCharacter());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getId());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeAsciiString(partychar.getName(), 13);
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getJobId());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            lew.writeInt(partychar.getLevel());
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            if (partychar.isOnline()) {
                lew.writeInt(partychar.getChannel() - 1);
            }
            else {
                lew.writeInt(-2);
            }
        }
        lew.writeInt(party.getLeader().getId());
        for (final MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel) {
                lew.writeInt(partychar.getMapid());
            }
            else {
                lew.writeInt(0);
            }
        }
        for (final MaplePartyCharacter partychar : partymembers) {
            if (partychar.getChannel() == forchannel && !leaving) {
                lew.writeInt(partychar.getDoorTown());
                lew.writeInt(partychar.getDoorTarget());
                lew.writeInt(partychar.getDoorSkill());
                lew.writeInt(partychar.getDoorPosition().x);
                lew.writeInt(partychar.getDoorPosition().y);
            }
            else {
                lew.writeInt(leaving ? 999999999 : 0);
                lew.writeLong(leaving ? 999999999L : 0L);
                lew.writeLong(leaving ? -1L : 0L);
            }
        }
    }

    /**
     * 修改组队
     * @param forChannel
     * @param party
     * @param op
     * @param target
     * @return
     */
    public static byte[] updateParty(final int forChannel, final MapleParty party, final PartyOperation op, final MaplePartyCharacter target) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PARTY_OPERATION.getValue());
        switch (op) {
            case DISBAND:
            case EXPEL:
            case LEAVE: {
                mplew.write(12);
                mplew.writeInt(party.getId());
                mplew.writeInt(target.getId());
                mplew.write((int)((op != PartyOperation.DISBAND) ? 1 : 0));
                if (op == PartyOperation.DISBAND) {
                    mplew.writeInt(target.getId());
                    break;
                }
                mplew.write((int)((op == PartyOperation.EXPEL) ? 1 : 0));
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, op == PartyOperation.LEAVE);
                break;
            }
            case JOIN: {
                mplew.write(15);
                mplew.writeInt(party.getId());
                mplew.writeMapleAsciiString(target.getName());
                addPartyStatus(forChannel, party, mplew, false);
                break;
            }
            case SILENT_UPDATE:
            case LOG_ONOFF: {
                mplew.write(7);
                mplew.writeInt(party.getId());
                addPartyStatus(forChannel, party, mplew, op == PartyOperation.LOG_ONOFF);
                break;
            }
            case CHANGE_LEADER:
            case CHANGE_LEADER_DC: {
                mplew.write(27);
                mplew.writeInt(target.getId());
                mplew.write((int)((op == PartyOperation.CHANGE_LEADER_DC) ? 1 : 0));
                break;
            }
        }
        return mplew.getPacket();
    }

    /**
     * 组队门户
     * @param townId
     * @param targetId
     * @param skillId
     * @param position
     * @param animation
     * @return
     */
    public static byte[] partyPortal(final int townId, final int targetId, final int skillId, final Point position, final boolean animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PARTY_OPERATION.getValue());
        mplew.write(36);
        mplew.write((int)(animation ? 0 : 1));
        mplew.writeInt(townId);
        mplew.writeInt(targetId);
        mplew.writePos(position);
        return mplew.getPacket();
    }

    /**
     * 更新组队成员HP
     * @param cid
     * @param curhp
     * @param maxhp
     * @return
     */
    public static byte[] updatePartyMemberHP(final int cid, final int curhp, final int maxhp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_PARTYMEMBER_HP.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(curhp);
        mplew.writeInt(maxhp);
        return mplew.getPacket();
    }

    /**
     * 聊天
     * @param name
     * @param chattext
     * @param mode
     * @return
     */
    public static byte[] multiChat(final String name, final String chattext, final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MULTICHAT.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(name);
        mplew.writeMapleAsciiString(chattext);
        return mplew.getPacket();
    }

    /**
     * 获取时钟
     * @param time
     * @return
     */
    public static byte[] getClock(final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CLOCK.getValue());
        mplew.write(2);
        mplew.writeInt(time);
        return mplew.getPacket();
    }

    /**
     * 获取时钟时间
     * @param hour
     * @param min
     * @param sec
     * @return
     */
    public static byte[] getClockTime(final int hour, final int min, final int sec) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CLOCK.getValue());
        mplew.write(1);
        mplew.write(hour);
        mplew.write(min);
        mplew.write(sec);
        return mplew.getPacket();
    }

    /**
     * 释放范围技能
     * @param mist
     * @return
     */
    public static byte[] spawnMist(final MapleMist mist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_MIST.getValue());
        mplew.writeInt(mist.getObjectId());
        mplew.writeInt(mist.isMobMist() ? 0 : ((mist.isPoisonMist() != 0) ? 1 : 2));
        mplew.writeInt(mist.getOwnerId());
        if (mist.getMobSkill() == null) {
            mplew.writeInt(mist.getSourceSkill().getId());
        }
        else {
            mplew.writeInt(mist.getMobSkill().getSkillId());
        }
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(LtMS.ConfigValuesMap.get("方向"));
        return mplew.getPacket();
    }

    /**
     * 召唤技能
     * @param mist
     * @param opCode
     * @return
     */
    public static byte[] spawnSkill(final MapleMist mist,int opCode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(opCode);
        mplew.writeInt(mist.getObjectId());
        mplew.writeInt(LtMS.ConfigValuesMap.get("技能类型"));
        mplew.writeInt(mist.getOwnerId());
        mplew.writeInt(mist.getSourceSkill().getId());
        mplew.write(mist.getSkillLevel());
        mplew.writeShort(mist.getSkillDelay());
        mplew.writeInt(mist.getBox().x);
        mplew.writeInt(mist.getBox().y);
        mplew.writeInt(mist.getBox().x + mist.getBox().width);
        mplew.writeInt(mist.getBox().y + mist.getBox().height);
        mplew.writeInt(0);
        return mplew.getPacket();
    }

    /**
     * 移除范围技能
     * @param oid
     * @param eruption
     * @return
     */
    public static byte[] removeMist(final int oid, final boolean eruption) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_MIST.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }

    /**
     * 伤害召唤
     * @param cid
     * @param summonSkillId
     * @param damage
     * @param unkByte
     * @param monsterIdFrom
     * @return
     */
    public static byte[] damageSummon(final int cid, final int summonSkillId, final int damage, final int unkByte, final int monsterIdFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DAMAGE_SUMMON.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(unkByte);
        mplew.writeInt(damage);
        mplew.writeInt(monsterIdFrom);
        mplew.write(0);
        return mplew.getPacket();
    }

    /**
     * 好友列表消息
     * @param message
     * @return
     */
    public static byte[] buddylistMessage(final byte message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(message);
        return mplew.getPacket();
    }

    /**
     * 更新好友列表
     * @param buddylist
     * @return
     */
    public static byte[] updateBuddylist(final Collection<BuddyEntry> buddylist) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(7);
        mplew.write(buddylist.size());
        for (final BuddyEntry buddy : buddylist) {
            mplew.writeInt(buddy.getCharacterId());
            mplew.writeAsciiString(buddy.getName(), 13);
            mplew.write(0);
            mplew.writeInt((buddy.getChannel() == -1 || !buddy.isVisible()) ? -1 : (buddy.getChannel() - 1));
            mplew.writeAsciiString(buddy.getGroup(), 17);
        }
        for (int x = 0; x < buddylist.size(); ++x) {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }
    
    public static byte[] requestBuddylistAdd(final int cidFrom, final String nameFrom, final int levelFrom, final int jobFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(9);
        mplew.writeInt(cidFrom);
        mplew.writeMapleAsciiString(nameFrom);
        mplew.writeInt(cidFrom);
        mplew.writeAsciiString(nameFrom, 13);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeAsciiString("其他", 16);
        mplew.writeShort(0);
        return mplew.getPacket();
    }

    /**
     * 更新好友频道
     * @param characterid
     * @param channel
     * @return
     */
    public static byte[] updateBuddyChannel(final int characterid, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(20);
        mplew.writeInt(characterid);
        mplew.write(0);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }

    /**
     * 物品效果
     * @param characterid
     * @param itemid
     * @return
     */
    public static byte[] itemEffect(final int characterid, final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_EFFECT.getValue());
        mplew.writeInt(characterid);
        mplew.writeMapleAsciiString("");
        mplew.writeMapleAsciiString("");
        mplew.writeShort(-1);
        mplew.writeShort(-1);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    /**
     * 更新好友
     * @param capacity
     * @return
     */
    public static byte[] updateBuddyCapacity(final int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BUDDYLIST.getValue());
        mplew.write(21);
        mplew.write(capacity);
        return mplew.getPacket();
    }

    /**
     * 显示椅子
     * @param characterid
     * @param itemid
     * @return
     */
    public static byte[] showChair(final int characterid, final int itemid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_CHAIR.getValue());
        mplew.writeInt(characterid);
        mplew.writeInt(itemid);
        return mplew.getPacket();
    }

    /**
     * 取消椅子
     * @param id
     * @return
     */
    public static byte[] cancelChair(final int id) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_CHAIR.getValue());
        if (id == -1) {
            mplew.write(0);
        }
        else {
            mplew.write(1);
            mplew.writeShort(id);
        }
        return mplew.getPacket();
    }

    /**
     * 召唤反应堆
     * @param reactor
     * @return
     */
    public static byte[] spawnReactor(final MapleReactor reactor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REACTOR_SPAWN.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.writeInt(reactor.getReactorId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.write(reactor.getFacingDirection());
        mplew.writeMapleAsciiString(reactor.getName());
        return mplew.getPacket();
    }

    /**
     * 触发反应堆
     * @param reactor
     * @param stance
     * @return
     */
    public static byte[] triggerReactor(final MapleReactor reactor, final int stance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REACTOR_HIT.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        mplew.writeShort(stance);
        mplew.write(0);
        mplew.write(4);
        return mplew.getPacket();
    }

    /**
     * 摧毁反应堆
     * @param reactor
     * @return
     */
    public static byte[] destroyReactor(final MapleReactor reactor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REACTOR_DESTROY.getValue());
        mplew.writeInt(reactor.getObjectId());
        mplew.write(reactor.getState());
        mplew.writePos(reactor.getPosition());
        return mplew.getPacket();
    }

    /**
     * 音乐变更
     * @param song
     * @return
     */
    public static byte[] musicChange(final String song) {
        return environmentChange(song, 6);
    }

    /**
     * 显示效果
     * @param effect
     * @return
     */
    public static byte[] showEffect(final String effect) {
        return environmentChange(effect, 3);
    }

    /**
     * 播放音效
     * @param sound
     * @return
     */
    public static byte[] playSound(final String sound) {
        return environmentChange(sound, 4);
    }

    /**
     * 场景变更
     * @param env
     * @param mode
     * @return
     */
    public static byte[] environmentChange(final String env, final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(env);
        return mplew.getPacket();
    }

    /**
     * 场景移动
     * @param env
     * @param mode
     * @return
     */
    public static byte[] environmentMove(final String env, final int mode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_ENV.getValue());
        mplew.writeMapleAsciiString(env);
        mplew.writeInt(mode);
        return mplew.getPacket();
    }
    
    public static byte[] startMapEffect(final String msg, final int itemid, final boolean active) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_ENV.getValue());
        mplew.write((int)(active ? 0 : 1));
        mplew.writeInt(itemid);
        if (active) {
            mplew.writeMapleAsciiString(msg);
        }
        return mplew.getPacket();
    }

    /**
     * 移除地图效果
     * @return
     */
    public static byte[] removeMapEffect() {
        return startMapEffect(null, 0, false);
    }
    
    public static byte[] fuckGuildInfo(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(26);
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("");
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] showGuildInfo(final MapleCharacter c) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(26);
        if (c == null || c.getMGC() == null) {
            mplew.write(0);
            return mplew.getPacket();
        }
        final MapleGuild g = Guild.getGuild(c.getGuildId());
        if (g == null) {
            mplew.write(0);
            return mplew.getPacket();
        }
        mplew.write(1);
        getGuildInfo(mplew, g, c);
        return mplew.getPacket();
    }
    
    public static void getGuildInfo(final MaplePacketLittleEndianWriter mplew, final MapleGuild guild) {
        getGuildInfo(mplew, guild, null);
    }
    
    public static void getGuildInfo(final MaplePacketLittleEndianWriter mplew, final MapleGuild guild, final MapleCharacter c) {

        mplew.writeInt(guild.getId());
        boolean BOSS伤害显示 = Objects.nonNull(LtMS.ConfigValuesMap.get("BOSS伤害显示")) && LtMS.ConfigValuesMap.get("BOSS伤害显示") > 0;
        boolean 家族名称显示 = Objects.nonNull(LtMS.ConfigValuesMap.get("家族名称显示")) && LtMS.ConfigValuesMap.get("家族名称显示") > 0;
        if (c != null && (Integer)LtMS.ConfigValuesMap.get("角色名战斗力显示") > 0) {
            if(家族名称显示){
                if(BOSS伤害显示) {
                    mplew.writeMapleAsciiString(guild.getName() + "『战力:" + formatNumberWithUnit(c.getPower()) + "』"+"|BOSS伤:"+(int)(c.getStat().bossdam_r+c.package_boss_damage_percent+c.getPotential(30)+(c.get套装伤害加成()*100))+"%"+(c.开启巅峰等级>0 ? "|巅峰:"+c.开启巅峰等级+"级" : ""));
                }else{
                    mplew.writeMapleAsciiString(guild.getName() + "『战力:" + formatNumberWithUnit(c.getPower()) + "』"+(c.开启巅峰等级>0 ? "|巅峰:"+c.开启巅峰等级+"级" : ""));
                }
            }else{
                if(BOSS伤害显示) {
                    mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(c.getPower()) + "』"+"|BOSS伤:"+(int)(c.getStat().bossdam_r+c.package_boss_damage_percent+c.getPotential(30)+(c.get套装伤害加成()*100))+"%"+(c.开启巅峰等级>0 ? "|巅峰:"+c.开启巅峰等级+"级" : ""));
                }else{
                    mplew.writeMapleAsciiString("『战力:" + formatNumberWithUnit(c.getPower()) + "』"+(c.开启巅峰等级>0 ? "|巅峰:"+c.开启巅峰等级+"级" : ""));
                }
            }
        } else {
            mplew.writeMapleAsciiString(guild.getName());
        }

        for (int i = 1; i <= 5; ++i) {
            mplew.writeMapleAsciiString(guild.getRankTitle(i));
        }
        guild.addMemberData(mplew);
        mplew.writeInt(guild.getCapacity());
        mplew.writeShort(guild.getLogoBG());
        mplew.write(guild.getLogoBGColor());
        mplew.writeShort(guild.getLogo());
        mplew.write(guild.getLogoColor());
        mplew.writeMapleAsciiString(guild.getNotice());
        mplew.writeInt(guild.getGP());
        mplew.writeInt((guild.getAllianceId() > 0) ? guild.getAllianceId() : 0);
    }
    public static String formatNumberWithUnit(long number) {
        if (number < 1000) {
            return String.valueOf(number);
        } else if (number < 10000) {
            return String.format("%.0f千", number / 1000.0);
        } else if (number < 100000) {
            return String.format("%.0f万", number / 10000.0).replace(".0万", "万");
        } else if (number < 1000000) {
            return String.format("%.0f万", number / 10000.0).replace(".00万", "万");
        } else if (number < 100000000) {
            return String.format("%.0f万", number / 10000.0).replace(".0万", "万");
        } else if (number < 1000000000) {
            return String.format("%.0f亿", number / 100000000.0).replace(".0亿", "亿");
        } else {
            return String.format("%.0f亿", number / 100000000.0).replace(".00亿", "亿");
        }
    }

    public static void main(String[] args) {
        System.out.println(formatNumberWithUnit(111111));
    }
    public static byte[] 勳章(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(26);
        if (chr == null) {
            mplew.write(0);
            return mplew.getPacket();
        }
        mplew.write(1);
        mplew.writeInt(0);
        mplew.writeMapleAsciiString("");
        for (int i = 1; i <= 5; ++i) {
            mplew.writeMapleAsciiString("");
        }
        mplew.write(0);
        mplew.writeInt(0);
        mplew.writeAsciiString("", 13);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.writeMapleAsciiString("");
        mplew.writeInt(0);
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] guildMemberOnline(final int gid, final int cid, final boolean bOnline) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(61);
        mplew.writeInt(gid);
        mplew.writeInt(cid);
        mplew.write((int)(bOnline ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] guildInvite(final int gid, final String charName, final int levelFrom, final int jobFrom) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(5);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(charName);
        return mplew.getPacket();
    }
    
    public static byte[] denyGuildInvitation(final String charname) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(55);
        mplew.writeMapleAsciiString(charname);
        return mplew.getPacket();
    }
    
    public static byte[] genericGuildMessage(final byte code) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(code);
        return mplew.getPacket();
    }
    
    public static byte[] newGuildMember(final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(39);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeAsciiString(mgc.getName(), 13);
        mplew.writeInt(mgc.getJobId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt((int)mgc.getGuildRank());
        mplew.writeInt((int)(mgc.isOnline() ? 1 : 0));
        mplew.writeInt(1);
        mplew.writeInt((int)mgc.getAllianceRank());
        return mplew.getPacket();
    }
    
    public static byte[] memberLeft(final MapleGuildCharacter mgc, final boolean bExpelled) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(bExpelled ? 47 : 44);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeMapleAsciiString(mgc.getName());
        return mplew.getPacket();
    }
    
    public static byte[] changeRank(final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(64);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.write(mgc.getGuildRank());
        return mplew.getPacket();
    }
    
    public static byte[] guildNotice(final int gid, final String notice) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(68);
        mplew.writeInt(gid);
        mplew.writeMapleAsciiString(notice);
        return mplew.getPacket();
    }
    
    public static byte[] guildMemberLevelJobUpdate(final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(60);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        return mplew.getPacket();
    }
    
    public static byte[] rankTitleChange(final int gid, final String[] ranks) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(62);
        mplew.writeInt(gid);
        for (final String r : ranks) {
            mplew.writeMapleAsciiString(r);
        }
        return mplew.getPacket();
    }
    
    public static byte[] guildDisband(final int gid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(50);
        mplew.writeInt(gid);
        mplew.write(1);
        return mplew.getPacket();
    }
    
    public static byte[] guildEmblemChange(final int gid, final short bg, final byte bgcolor, final short logo, final byte logocolor) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(66);
        mplew.writeInt(gid);
        mplew.writeShort((int)bg);
        mplew.write(bgcolor);
        mplew.writeShort((int)logo);
        mplew.write(logocolor);
        return mplew.getPacket();
    }
    
    public static byte[] guildCapacityChange(final int gid, final int capacity) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(58);
        mplew.writeInt(gid);
        mplew.write(capacity);
        return mplew.getPacket();
    }
    
    public static byte[] removeGuildFromAlliance(final MapleGuildAlliance alliance, final MapleGuild expelledGuild, final boolean expelled) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(16);
        addAllianceInfo(mplew, alliance);
        getGuildInfo(mplew, expelledGuild);
        mplew.write((int)(expelled ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] changeAlliance(final MapleGuildAlliance alliance, final boolean in) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(1);
        mplew.write((int)(in ? 1 : 0));
        mplew.writeInt(in ? alliance.getId() : 0);
        final int noGuilds = alliance.getNoGuilds();
        final MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < noGuilds; ++i) {
            g[i] = Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.write(noGuilds);
        for (int i = 0; i < noGuilds; ++i) {
            mplew.writeInt(g[i].getId());
            final Collection<MapleGuildCharacter> members = g[i].getMembers();
            mplew.writeInt(members.size());
            for (final MapleGuildCharacter mgc : members) {
                mplew.writeInt(mgc.getId());
                mplew.write((byte)(in ? mgc.getAllianceRank() : 0));
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] changeAllianceLeader(final int allianceid, final int newLeader, final int oldLeader) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(2);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        return mplew.getPacket();
    }
    
    public static byte[] updateAllianceLeader(final int allianceid, final int newLeader, final int oldLeader) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(25);
        mplew.writeInt(allianceid);
        mplew.writeInt(oldLeader);
        mplew.writeInt(newLeader);
        return mplew.getPacket();
    }
    
    public static byte[] sendAllianceInvite(final String allianceName, final MapleCharacter inviter) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(3);
        mplew.writeInt(inviter.getGuildId());
        mplew.writeMapleAsciiString(inviter.getName());
        mplew.writeMapleAsciiString(allianceName);
        return mplew.getPacket();
    }
    
    public static byte[] changeGuildInAlliance(final MapleGuildAlliance alliance, final MapleGuild guild, final boolean add) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(4);
        mplew.writeInt(add ? alliance.getId() : 0);
        mplew.writeInt(guild.getId());
        final Collection<MapleGuildCharacter> members = guild.getMembers();
        mplew.writeInt(members.size());
        for (final MapleGuildCharacter mgc : members) {
            mplew.writeInt(mgc.getId());
            mplew.write((byte)(add ? mgc.getAllianceRank() : 0));
        }
        return mplew.getPacket();
    }
    
    public static byte[] changeAllianceRank(final int allianceid, final MapleGuildCharacter player) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(5);
        mplew.writeInt(allianceid);
        mplew.writeInt(player.getId());
        mplew.writeInt((int)player.getAllianceRank());
        return mplew.getPacket();
    }
    
    public static byte[] createGuildAlliance(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(15);
        addAllianceInfo(mplew, alliance);
        final int noGuilds = alliance.getNoGuilds();
        final MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); ++i) {
            g[i] = Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        for (final MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getAllianceInfo(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(12);
        mplew.write((int)((alliance != null) ? 1 : 0));
        if (alliance != null) {
            addAllianceInfo(mplew, alliance);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getAllianceUpdate(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(23);
        addAllianceInfo(mplew, alliance);
        return mplew.getPacket();
    }
    
    public static byte[] getGuildAlliance(final MapleGuildAlliance alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(13);
        if (alliance == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        final int noGuilds = alliance.getNoGuilds();
        final MapleGuild[] g = new MapleGuild[noGuilds];
        for (int i = 0; i < alliance.getNoGuilds(); ++i) {
            g[i] = Guild.getGuild(alliance.getGuildId(i));
            if (g[i] == null) {
                return enableActions();
            }
        }
        mplew.writeInt(noGuilds);
        for (final MapleGuild gg : g) {
            getGuildInfo(mplew, gg);
        }
        return mplew.getPacket();
    }
    
    public static byte[] addGuildToAlliance(final MapleGuildAlliance alliance, final MapleGuild newGuild) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(18);
        addAllianceInfo(mplew, alliance);
        mplew.writeInt(newGuild.getId());
        getGuildInfo(mplew, newGuild);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    private static void addAllianceInfo(final MaplePacketLittleEndianWriter mplew, final MapleGuildAlliance alliance) {
        mplew.writeInt(alliance.getId());
        mplew.writeMapleAsciiString(alliance.getName());
        for (int i = 1; i <= 5; ++i) {
            mplew.writeMapleAsciiString(alliance.getRank(i));
        }
        mplew.write(alliance.getNoGuilds());
        for (int i = 0; i < alliance.getNoGuilds(); ++i) {
            mplew.writeInt(alliance.getGuildId(i));
        }
        mplew.writeInt(alliance.getCapacity());
        mplew.writeMapleAsciiString(alliance.getNotice());
    }
    
    public static byte[] allianceMemberOnline(final int alliance, final int gid, final int id, final boolean online) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(14);
        mplew.writeInt(alliance);
        mplew.writeInt(gid);
        mplew.writeInt(id);
        mplew.write((int)(online ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] updateAlliance(final MapleGuildCharacter mgc, final int allianceid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(24);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getGuildId());
        mplew.writeInt(mgc.getId());
        mplew.writeInt(mgc.getLevel());
        mplew.writeInt(mgc.getJobId());
        return mplew.getPacket();
    }
    
    public static byte[] updateAllianceRank(final int allianceid, final MapleGuildCharacter mgc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(27);
        mplew.writeInt(allianceid);
        mplew.writeInt(mgc.getId());
        mplew.writeInt((int)mgc.getAllianceRank());
        return mplew.getPacket();
    }
    
    public static byte[] disbandAlliance(final int alliance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ALLIANCE_OPERATION.getValue());
        mplew.write(29);
        mplew.writeInt(alliance);
        return mplew.getPacket();
    }
    
    public static byte[] BBSThreadList(final List<MapleBBSThread> bbs, int start) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(6);
        if (bbs == null) {
            mplew.write(0);
            mplew.writeLong(0L);
            return mplew.getPacket();
        }
        final int threadCount = bbs.size();
        MapleBBSThread notice = null;
        for (final MapleBBSThread b : bbs) {
            if (b.isNotice()) {
                notice = b;
                break;
            }
        }
        mplew.write((int)((notice != null) ? 1 : 0));
        if (notice != null) {
            addThread(mplew, notice);
        }
        if (threadCount < start) {
            start = 0;
        }
        mplew.writeInt(threadCount);
        final int pages = Math.min(10, threadCount - start);
        mplew.writeInt(pages);
        for (int i = 0; i < pages; ++i) {
            addThread(mplew, (MapleBBSThread)bbs.get(start + i));
        }
        return mplew.getPacket();
    }
    
    private static void addThread(final MaplePacketLittleEndianWriter mplew, final MapleBBSThread rs) {
        mplew.writeInt(rs.localthreadID);
        mplew.writeInt(rs.ownerID);
        mplew.writeMapleAsciiString(rs.name);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(rs.timestamp));
        mplew.writeInt(rs.icon);
        mplew.writeInt(rs.getReplyCount());
    }
    
    public static byte[] showThread(final MapleBBSThread thread) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BBS_OPERATION.getValue());
        mplew.write(7);
        mplew.writeInt(thread.localthreadID);
        mplew.writeInt(thread.ownerID);
        mplew.writeLong(PacketHelper.getKoreanTimestamp(thread.timestamp));
        mplew.writeMapleAsciiString(thread.name);
        mplew.writeMapleAsciiString(thread.text);
        mplew.writeInt(thread.icon);
        mplew.writeInt(thread.getReplyCount());
        for (final MapleBBSReply reply : thread.replies.values()) {
            mplew.writeInt(reply.replyid);
            mplew.writeInt(reply.ownerID);
            mplew.writeLong(PacketHelper.getKoreanTimestamp(reply.timestamp));
            mplew.writeMapleAsciiString(reply.content);
        }
        return mplew.getPacket();
    }
    
    public static byte[] showmesoRanks(final int npcid, final List<mesoRankingInfo> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final mesoRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(Long.valueOf(info.getMeso()).intValue());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    
    public static byte[] showlevelRanks(final int npcid, final List<levelRankingInfo> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final levelRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getLevel());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    
    public static byte[] showGuildRanks(final int npcid, final List<GuildRankingInfo> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final GuildRankingInfo info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getGP());
            mplew.writeInt(info.getLogo());
            mplew.writeInt(info.getLogoColor());
            mplew.writeInt(info.getLogoBg());
            mplew.writeInt(info.getLogoBgColor());
        }
        return mplew.getPacket();
    }

    public static byte[] showSponsorRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    public static byte[] showDefeatRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    public static byte[] showBossRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    public static byte[] showRedRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    public static byte[] showAccurateRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    public static byte[] showEnhancedRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    public static byte[] showDropRanks(final int npcid, final List<MapleGuildRanking.SponsorRank> all) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(all.size());
        for (final MapleGuildRanking.SponsorRank info : all) {
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getCounts());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }
        return mplew.getPacket();
    }
    
    public static byte[] updateGP(final int gid, final int GP) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(72);
        mplew.writeInt(gid);
        mplew.writeInt(GP);
        return mplew.getPacket();
    }
    
    public static byte[] skillEffect(final MapleCharacter from, final int skillId, final byte level, final byte flags, final byte speed, final byte unk) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        mplew.write(level);
        mplew.write(flags);
        mplew.write(speed);
        mplew.write(unk);
        return mplew.getPacket();
    }
    
    public static byte[] skillCancel(final MapleCharacter from, final int skillId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_SKILL_EFFECT.getValue());
        mplew.writeInt(from.getId());
        mplew.writeInt(skillId);
        return mplew.getPacket();
    }
    
    public static byte[] showMagnet(final int mobid, final byte success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_MAGNET.getValue());
        mplew.writeInt(mobid);
        mplew.write(success);
        return mplew.getPacket();
    }
    
    public static byte[] sendHint(final String hint, int width, int height) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        if (width < 1) {
            width = hint.length() * 10;
            if (width < 40) {
                width = 40;
            }
        }
        if (height < 5) {
            height = 5;
        }
        mplew.writeShort((int)SendPacketOpcode.PLAYER_HINT.getValue());
        mplew.writeMapleAsciiString(hint);
        mplew.writeShort(width);
        mplew.writeShort(height);
        mplew.write(1);
        return mplew.getPacket();
    }
    
    public static byte[] messengerInvite(final String from, final int messengerid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(3);
        mplew.writeMapleAsciiString(from);
        mplew.write(0);
        mplew.writeInt(messengerid);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] addMessengerPlayer(final String from, MapleCharacter chr, final int position, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(0);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);
        return mplew.getPacket();
    }
    
    public static byte[] removeMessengerPlayer(final int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(2);
        mplew.write(position);
        return mplew.getPacket();
    }
    
    public static byte[] updateMessengerPlayer(final String from, MapleCharacter chr, final int position, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(7);
        mplew.write(position);
        PacketHelper.addCharLook(mplew, chr, true);
        mplew.writeMapleAsciiString(from);
        mplew.writeShort(channel);
        return mplew.getPacket();
    }
    
    public static byte[] joinMessenger(final int position) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(1);
        mplew.write(position);
        return mplew.getPacket();
    }
    
    public static byte[] messengerChat(final String text) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(6);
        mplew.writeMapleAsciiString(text);
        return mplew.getPacket();
    }
    
    public static byte[] messengerNote(final String text, final int mode, final int mode2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MESSENGER.getValue());
        mplew.write(mode);
        mplew.writeMapleAsciiString(text);
        mplew.write(mode2);
        return mplew.getPacket();
    }
    
    public static byte[] getFindReplyWithCS(final String target, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(2);
        mplew.writeInt(-1);
        return mplew.getPacket();
    }
    
    public static byte[] getFindReplyWithMTS(final String target, final boolean buddy) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.WHISPER.getValue());
        mplew.write(buddy ? 72 : 9);
        mplew.writeMapleAsciiString(target);
        mplew.write(0);
        mplew.writeInt(-1);
        return mplew.getPacket();
    }
    
    public static byte[] showEquipEffect() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        return mplew.getPacket();
    }
    
    public static byte[] showEquipEffect(final int team) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_EQUIP_EFFECT.getValue());
        mplew.writeShort(team);
        return mplew.getPacket();
    }
    
    public static byte[] summonSkill(final int cid, final int summonSkillId, final int newStance) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SUMMON_SKILL.getValue());
        mplew.writeInt(cid);
        mplew.writeInt(summonSkillId);
        mplew.write(newStance);
        return mplew.getPacket();
    }
    
    public static byte[] skillCooldown(final int sid, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.COOLDOWN.getValue());
        mplew.writeInt(sid);
        mplew.writeShort(time);
        return mplew.getPacket();
    }
    
    public static byte[] useSkillBook(MapleCharacter chr, final int skillid, final int maxlevel, final boolean canuse, final boolean success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.USE_SKILL_BOOK.getValue());
        mplew.writeInt(chr.getId());
        mplew.write(1);
        mplew.writeInt(skillid);
        mplew.writeInt(maxlevel);
        mplew.write((int)(canuse ? 1 : 0));
        mplew.write((int)(success ? 1 : 0));
        return mplew.getPacket();
    }
    
    public static byte[] getMacros(final SkillMacro[] macros) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SKILL_MACRO.getValue());
        int count = 0;
        for (int i = 0; i < 5; ++i) {
            if (macros[i] != null) {
                ++count;
            }
        }
        mplew.write(count);
        for (int i = 0; i < 5; ++i) {
            final SkillMacro macro = macros[i];
            if (macro != null) {
                mplew.writeMapleAsciiString(macro.getName());
                mplew.write(macro.getShout());
                mplew.writeInt(macro.getSkill1());
                mplew.writeInt(macro.getSkill2());
                mplew.writeInt(macro.getSkill3());
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] updateAriantPQRanking(final String name, final int score, final boolean empty) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ARIANT_PQ_START.getValue());
        mplew.write((int)(empty ? 0 : 1));
        if (!empty) {
            mplew.writeMapleAsciiString(name);
            mplew.writeInt(score);
        }
        return mplew.getPacket();
    }
    
    public static byte[] catchMonster(final int mobid, final int itemid, final byte success) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CATCH_MONSTER.getValue());
        mplew.writeInt(mobid);
        mplew.writeInt(itemid);
        mplew.write(success);
        return mplew.getPacket();
    }
    
    public static byte[] showAriantScoreBoard() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ARIANT_SCOREBOARD.getValue());
        return mplew.getPacket();
    }
    
    public static byte[] boatEffect(final int effect) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(effect);
        return mplew.getPacket();
    }
    
    public static byte[] boatPacket( boolean type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BOAT_PACKET.getValue());
        mplew.write(type ? 1 : 2);
        mplew.write(0);
        return mplew.getPacket();
    }
    public static byte[] boatPacket(int effect) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();


        mplew.writeShort(SendPacketOpcode.BOAT_EFFECT.getValue());
        mplew.writeShort(effect);
        return mplew.getPacket();
    }
    public static byte[] removeItemFromDuey(final boolean remove, final int Package) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DUEY.getValue());
        mplew.write(24);
        mplew.writeInt(Package);
        mplew.write(remove ? 3 : 4);
        return mplew.getPacket();
    }
    
    public static byte[] sendDuey(final byte operation, final List<MapleDueyActions> packages) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DUEY.getValue());
        mplew.write(operation);
        switch (operation) {
            case 9: {
                mplew.write(1);
                break;
            }
            case 10: {
                mplew.write(0);
                mplew.write(packages.size());
                for (final MapleDueyActions dp : packages) {
                    mplew.writeInt(dp.getPackageId());
                    mplew.writeAsciiString(dp.getSender(), 13);
                    mplew.writeInt(dp.getMesos());
                    mplew.writeLong(KoreanDateUtil.getFileTimestamp(dp.getSentTime(), false));
                    mplew.writeZeroBytes(205);
                    if (dp.getItem() != null) {
                        mplew.write(1);
                        PacketHelper.addItemInfo(mplew, dp.getItem(), true, true);
                    }
                    else {
                        mplew.write(0);
                    }
                }
                mplew.write(0);
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] Mulung_DojoUp2() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(8);
        return mplew.getPacket();
    }
    
    public static byte[] showQuestMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_STATUS_INFO.getValue());
        mplew.write(9);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }
    
    public static byte[] Mulung_Pts(final int recv, final int total) {
        return showQuestMsg("你获得 " + recv + " 修煉點數, 目前累積了 " + total + " 點修煉點數");
    }
    
    public static byte[] showQuestMsgA(final String msg) {
        return serverNotice(5, msg);
    }
    
    public static byte[] showOXQuiz(final int questionSet, final int questionId, final boolean askQuestion) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OX_QUIZ.getValue());
        mplew.write((int)(askQuestion ? 1 : 0));
        mplew.write(questionSet);
        mplew.writeShort(questionId);
        return mplew.getPacket();
    }
    
    public static byte[] leftKnockBack() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LEFT_KNOCK_BACK.getValue());
        return mplew.getPacket();
    }
    
    public static byte[] rollSnowball(final int type, final MapleSnowballs ball1, final MapleSnowballs ball2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ROLL_SNOWBALL.getValue());
        mplew.write(type);
        mplew.writeInt((ball1 == null) ? 0 : (ball1.getSnowmanHP() / 75));
        mplew.writeInt((ball2 == null) ? 0 : (ball2.getSnowmanHP() / 75));
        mplew.writeShort((ball1 == null) ? 0 : ball1.getPosition());
        mplew.write(0);
        mplew.writeShort((ball2 == null) ? 0 : ball2.getPosition());
        mplew.writeZeroBytes(11);
        return mplew.getPacket();
    }
    
    public static byte[] enterSnowBall() {
        return rollSnowball(0, null, null);
    }
    
    public static byte[] hitSnowBall(final int team, final int damage, final int distance, final int delay) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.HIT_SNOWBALL.getValue());
        mplew.write(team);
        mplew.writeShort(damage);
        mplew.write(distance);
        mplew.write(delay);
        return mplew.getPacket();
    }
    
    public static byte[] snowballMessage(final int team, final int message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SNOWBALL_MESSAGE.getValue());
        mplew.write(team);
        mplew.writeInt(message);
        return mplew.getPacket();
    }
    
    public static byte[] finishedSort(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GATHER_ITEM_RESULT.getValue());
        mplew.write(1);
        mplew.write(type);
        return mplew.getPacket();
    }
    
    public static byte[] coconutScore(final int[] coconutscore) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.COCONUT_SCORE.getValue());
        mplew.writeShort(coconutscore[0]);
        mplew.writeShort(coconutscore[1]);
        return mplew.getPacket();
    }
    
    public static byte[] hitCoconut(final boolean spawn, final int id, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.HIT_COCONUT.getValue());
        if (spawn) {
            mplew.write(0);
            mplew.writeInt(128);
        }
        else {
            mplew.writeInt(id);
            mplew.write(type);
        }
        return mplew.getPacket();
    }
    
    public static byte[] finishedGather(final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SORT_ITEM_RESULT.getValue());
        mplew.write(1);
        mplew.write(type);
        return mplew.getPacket();
    }
    
    public static byte[] yellowChat(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SET_WEEK_EVENT_MESSAGE.getValue());
        mplew.write(-1);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }
    
    public static byte[] getPeanutResult(final int itemId, final short quantity, final int itemId2, final short quantity2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PIGMI_REWARD.getValue());
        mplew.writeInt(itemId);
        mplew.writeShort((int)quantity);
        mplew.writeInt(5060003);
        mplew.writeInt(itemId2);
        mplew.writeInt((int)quantity2);
        return mplew.getPacket();
    }
    
    public static byte[] sendLevelup(final boolean family, final int level, final String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LEVEL_UPDATE.getValue());
        mplew.write(family ? 1 : 2);
        mplew.writeInt(level);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }
    
    public static byte[] sendMarriage(final boolean family, final String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MARRIAGE_UPDATE.getValue());
        mplew.write((int)(family ? 1 : 0));
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }
    
    public static byte[] sendJobup(final boolean family, final int jobid, final String name) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.JOB_UPDATE.getValue());
        mplew.write((int)(family ? 1 : 0));
        mplew.writeInt(jobid);
        mplew.writeMapleAsciiString(name);
        return mplew.getPacket();
    }
    
    public static byte[] showZakumShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ZAKUM_SHRINE.getValue());
        mplew.write((int)(spawned ? 1 : 0));
        mplew.writeInt(time);
        return mplew.getPacket();
    }
    
    public static byte[] showHorntailShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.HORNTAIL_SHRINE.getValue());
        mplew.write((int)(spawned ? 1 : 0));
        mplew.writeInt(time);
        return mplew.getPacket();
    }
    
    public static byte[] showChaosZakumShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CHAOS_ZAKUM_SHRINE.getValue());
        mplew.write((int)(spawned ? 1 : 0));
        mplew.writeInt(time);
        return mplew.getPacket();
    }
    
    public static byte[] showChaosHorntailShrine(final boolean spawned, final int time) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CHAOS_HORNTAIL_SHRINE.getValue());
        mplew.write((int)(spawned ? 1 : 0));
        mplew.writeInt(time);
        return mplew.getPacket();
    }
    
    public static byte[] stopClock() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.STOP_CLOCK.getValue());
        return mplew.getPacket();
    }

    public static byte[] temporaryStats_Aran() {
        final List<Pair<Temp, Integer>> stats = new ArrayList<Pair<Temp, Integer>>();
        stats.add(new Pair<Temp, Integer>(Temp.STR, Integer.valueOf(999)));
        stats.add(new Pair<Temp, Integer>(Temp.DEX, Integer.valueOf(999)));
        stats.add(new Pair<Temp, Integer>(Temp.INT, Integer.valueOf(999)));
        stats.add(new Pair<Temp, Integer>(Temp.LUK, Integer.valueOf(999)));
        stats.add(new Pair<Temp, Integer>(Temp.WATK, Integer.valueOf(255)));
        stats.add(new Pair<Temp, Integer>(Temp.ACC, Integer.valueOf(999)));
        stats.add(new Pair<Temp, Integer>(Temp.AVOID, Integer.valueOf(999)));
        stats.add(new Pair<Temp, Integer>(Temp.SPEED, Integer.valueOf(140)));
        stats.add(new Pair<Temp, Integer>(Temp.JUMP, Integer.valueOf(120)));
        return temporaryStats(stats);
    }
    //强制修改属性2
    public static byte[] temporaryStats_Balrog(MapleCharacter chr) {
        final List<Pair<Temp, Integer>> stats = new ArrayList<Pair<Temp, Integer>>();
        final int offset = 1 + (chr.getLevel() - 90) / 20;
        stats.add(new Pair<Temp, Integer>(Temp.STR, Integer.valueOf(chr.getStat().getTotalStr() / offset)));
        stats.add(new Pair<Temp, Integer>(Temp.DEX, Integer.valueOf(chr.getStat().getTotalDex() / offset)));
        stats.add(new Pair<Temp, Integer>(Temp.INT, Integer.valueOf(chr.getStat().getTotalInt() / offset)));
        stats.add(new Pair<Temp, Integer>(Temp.LUK, Integer.valueOf(chr.getStat().getTotalLuk() / offset)));
        stats.add(new Pair<Temp, Integer>(Temp.WATK, Integer.valueOf(chr.getStat().getTotalWatk() / offset)));
        stats.add(new Pair<Temp, Integer>(Temp.MATK, Integer.valueOf(chr.getStat().getTotalMagic() / offset)));
        return temporaryStats(stats);
    }

    public static byte[] temporaryStats(final List<Pair<Temp, Integer>> stats) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.TEMP_STATS.getValue());
        int updateMask = 0;
        for (final Pair<Temp, Integer> statupdate : stats) {
            updateMask |= ((Temp)statupdate.getLeft()).getValue();
        }
        final List<Pair<Temp, Integer>> mystats = stats;
        if (mystats.size() > 1) {
            Collections.sort(mystats, (Comparator<? super Pair<Temp, Integer>>)new Comparator<Pair<Temp, Integer>>() {
                @Override
                public int compare(final Pair<Temp, Integer> o1, final Pair<Temp, Integer> o2) {
                    final int val1 = ((Temp)o1.getLeft()).getValue();
                    final int val2 = ((Temp)o2.getLeft()).getValue();
                    return (val1 < val2) ? -1 : ((val1 == val2) ? 0 : 1);
                }
            });
        }
        mplew.writeInt(updateMask);
        for (final Pair<Temp, Integer> statupdate2 : mystats) {
            final Integer value = Integer.valueOf(((Temp)statupdate2.getLeft()).getValue());
            if ((int)value >= 1) {
                if ((int)value <= 512) {
                    mplew.writeShort((int)Integer.valueOf(statupdate2.getRight()).shortValue());
                }
                else {
                    mplew.write(Integer.valueOf(statupdate2.getRight()).byteValue());
                }
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] temporaryStats_Reset() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.TEMP_STATS_RESET.getValue());
        return mplew.getPacket();
    }
    
    public static byte[] showHpHealed(final int cid, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_FOREIGN_EFFECT.getValue());
        mplew.writeInt(cid);
        mplew.write(7);
        mplew.writeInt(amount);
        return mplew.getPacket();
    }
    
    public static byte[] showOwnHpHealed(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_ITEM_GAIN_INCHAT.getValue());
        mplew.write(7);
        mplew.writeInt(amount);
        return mplew.getPacket();
    }
    
    public static byte[] sendRepairWindow(final int npc) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REPAIR_WINDOW.getValue());
        mplew.writeInt(34);
        mplew.writeInt(npc);
        return mplew.getPacket();
    }
    
    public static byte[] sendPyramidUpdate(final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.PYRAMID_UPDATE.getValue());
        mplew.writeInt(amount);
        return mplew.getPacket();
    }
    
    public static byte[] sendPyramidResult(final byte rank, final int amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(151);
        mplew.write(rank);
        mplew.writeInt(amount);
        return mplew.getPacket();
    }
    
    public static byte[] sendPyramidEnergy(final String type, final String amount) {
        return sendString(1, type, amount);
    }
    
    public static byte[] sendString(final int type, final String object, final String amount) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        switch (type) {
            case 1: {
                mplew.writeShort((int)SendPacketOpcode.SESSION_VALUE.getValue());
                break;
            }
            case 2: {
                mplew.writeShort((int)SendPacketOpcode.GHOST_POINT.getValue());
                break;
            }
            case 3: {
                mplew.writeShort((int)SendPacketOpcode.GHOST_STATUS.getValue());
                break;
            }
        }
        mplew.writeMapleAsciiString(object);
        mplew.writeMapleAsciiString(amount);
        return mplew.getPacket();
    }
    
    public static byte[] sendGhostPoint(final String type, final String amount) {
        return sendString(2, type, amount);
    }
    
    public static byte[] sendGhostStatus(final String type, final String amount) {
        return sendString(3, type, amount);
    }
    
    public static byte[] MulungEnergy(final int energy) {
        return sendPyramidEnergy("energy", String.valueOf(energy));
    }
    
    public static byte[] getPollQuestion() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GAME_POLL_QUESTION.getValue());
        mplew.writeInt(1);
        mplew.writeInt(14);
        mplew.writeMapleAsciiString("Are you mudkiz?");
        mplew.writeInt(ServerConstants.Poll_Answers.length);
        for (byte i = 0; i < ServerConstants.Poll_Answers.length; ++i) {
            mplew.writeMapleAsciiString(ServerConstants.Poll_Answers[i]);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getPollReply(final String message) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GAME_POLL_REPLY.getValue());
        mplew.writeMapleAsciiString(message);
        return mplew.getPacket();
    }
    
    public static byte[] getEvanTutorial(final String data) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.NPC_TALK.getValue());
        mplew.writeInt(8);
        mplew.write(0);
        mplew.write(1);
        mplew.write(1);
        mplew.write(1);
        mplew.writeMapleAsciiString(data);
        return mplew.getPacket();
    }
    
    public static byte[] showEventInstructions() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GMEVENT_INSTRUCTIONS.getValue());
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] getOwlOpen() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOP_SCANNER_RESULT.getValue());
        mplew.write(7);
        mplew.write(GameConstants.owlItems.length);
        for (final int i : GameConstants.owlItems) {
            mplew.writeInt(i);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getOwlSearched(final int itemSearch, final List<HiredMerchant> hms) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOP_SCANNER_RESULT.getValue());
        mplew.write(6);
        mplew.writeInt(0);
        mplew.writeInt(itemSearch);
        int size = 0;
        for (final HiredMerchant hm : hms) {
            size += hm.searchItem(itemSearch).size();
        }
        mplew.writeInt(size);
        for (final HiredMerchant hm : hms) {
            final List<MaplePlayerShopItem> items = hm.searchItem(itemSearch);
            for (final MaplePlayerShopItem item : items) {
                mplew.writeMapleAsciiString(hm.getOwnerName());
                mplew.writeInt(hm.getMap().getId());
                mplew.writeMapleAsciiString(hm.getDescription());
                mplew.writeInt((int)item.item.getQuantity());
                mplew.writeInt((int)item.bundles);
                mplew.writeInt(item.price);
                switch (2) {
                    case 0: {
                        mplew.writeInt(hm.getOwnerId());
                        break;
                    }
                    case 1: {
                        mplew.writeInt(hm.getStoreId());
                        break;
                    }
                    default: {
                        mplew.writeInt(hm.getObjectId());
                        break;
                    }
                }
                mplew.write((int)((hm.getFreeSlot() == -1) ? 1 : 0));
                mplew.write(GameConstants.getInventoryType(itemSearch).getType());
                if (GameConstants.getInventoryType(itemSearch) == MapleInventoryType.EQUIP) {
                    PacketHelper.addItemInfo(mplew, item.item, true, true);
                }
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] getRPSMode(final byte mode, final int mesos, final int selection, final int answer) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.RPS_GAME.getValue());
        mplew.write(mode);
        switch (mode) {
            case 6: {
                if (mesos != -1) {
                    mplew.writeInt(mesos);
                    break;
                }
                break;
            }
            case 8: {
                mplew.writeInt(9209002);
                break;
            }
            case 11: {
                mplew.write(selection);
                mplew.write(answer);
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] getSlotUpdate(final byte invType, final byte newSlots) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_INVENTORY_SLOT.getValue());
        mplew.write(invType);
        mplew.write(newSlots);
        return mplew.getPacket();
    }
    
    public static byte[] followRequest(final int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FOLLOW_REQUEST.getValue());
        mplew.writeInt(chrid);
        return mplew.getPacket();
    }
    
    public static byte[] followEffect(final int initiator, final int replier, final Point toMap) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FOLLOW_EFFECT.getValue());
        mplew.writeInt(initiator);
        mplew.writeInt(replier);
        if (replier == 0) {
            mplew.write((int)((toMap != null) ? 1 : 0));
            if (toMap != null) {
                mplew.writeInt(toMap.x);
                mplew.writeInt(toMap.y);
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] getFollowMsg(final int opcode) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FOLLOW_MSG.getValue());
        mplew.writeLong((long)opcode);
        return mplew.getPacket();
    }
    
    public static byte[] moveFollow(final Point otherStart, final Point myStart, final Point otherEnd, final List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FOLLOW_MOVE.getValue());
        mplew.writePos(otherStart);
        mplew.writePos(myStart);
        PacketHelper.serializeMovementList(mplew, moves);
        mplew.write(17);
        for (int i = 0; i < 8; ++i) {
            mplew.write(136);
        }
        mplew.write(8);
        mplew.writePos(otherEnd);
        mplew.writePos(otherStart);
        return mplew.getPacket();
    }
    
    public static byte[] getFollowMessage(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.FOLLOW_MESSAGE.getValue());
        mplew.writeShort(11);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }
    
    public static byte[] getNodeProperties(final MapleMonster objectid, final MapleMap map) {
        if (objectid.getNodePacket() != null) {
            return objectid.getNodePacket();
        }
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MONSTER_PROPERTIES.getValue());
        mplew.writeInt(objectid.getObjectId());
        mplew.writeInt(map.getNodes().size());
        mplew.writeInt(objectid.getPosition().x);
        mplew.writeInt(objectid.getPosition().y);
        for (final MapleNodeInfo mni : map.getNodes()) {
            mplew.writeInt(mni.x);
            mplew.writeInt(mni.y);
            mplew.writeInt(mni.attr);
            if (mni.attr == 2) {
                mplew.writeInt(500);
            }
        }
        mplew.writeZeroBytes(6);
        objectid.setNodePacket(mplew.getPacket());
        return objectid.getNodePacket();
    }
    
    public static byte[] getMovingPlatforms(final MapleMap map) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_PLATFORM.getValue());
        mplew.writeInt(map.getPlatforms().size());
        for (final MaplePlatform mp : map.getPlatforms()) {
            mplew.writeMapleAsciiString(mp.name);
            mplew.writeInt(mp.start);
            mplew.writeInt(mp.SN.size());
            for (int x = 0; x < mp.SN.size(); ++x) {
                mplew.writeInt((int)Integer.valueOf(mp.SN.get(x)));
            }
            mplew.writeInt(mp.speed);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.x2);
            mplew.writeInt(mp.y1);
            mplew.writeInt(mp.y2);
            mplew.writeInt(mp.x1);
            mplew.writeInt(mp.y1);
            mplew.writeShort(mp.r);
        }
        return mplew.getPacket();
    }
    
    public static byte[] getUpdateEnvironment(final MapleMap map) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_ENV.getValue());
        mplew.writeInt(map.getEnvironment().size());
        for (final Entry<String, Integer> mp : map.getEnvironment().entrySet()) {
            mplew.writeMapleAsciiString((String)mp.getKey());
            mplew.writeInt((int)Integer.valueOf(mp.getValue()));
        }
        return mplew.getPacket();
    }
    
    public static byte[] sendEngagementRequest(final String name, final int cid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MARRIAGE_REQUEST.getValue());
        mplew.write(0);
        mplew.writeMapleAsciiString(name);
        mplew.writeInt(cid);
        return mplew.getPacket();
    }
    
    public static byte[] trembleEffect(final int type, final int delay) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(1);
        mplew.write(type);
        mplew.writeInt(delay);
        return mplew.getPacket();
    }
    
    public static byte[] sendEngagement(final byte msg, final int item, final MapleCharacter male, final MapleCharacter female) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MARRIAGE_RESULT.getValue());
        mplew.write(msg);
        switch (msg) {
            case 11: {
                mplew.writeInt(0);
                mplew.writeInt(male.getId());
                mplew.writeInt(female.getId());
                mplew.writeShort(1);
                mplew.writeInt(item);
                mplew.writeInt(item);
                mplew.writeAsciiString(male.getName(), 13);
                mplew.writeAsciiString(female.getName(), 13);
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] englishQuizMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.ENGLISH_QUIZ.getValue());
        mplew.writeInt(20);
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }
    
    public static byte[] bombLieDetector(final boolean error, final int mapid, final int channel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(error ? 2 : 1);
        mplew.writeInt(mapid);
        mplew.writeInt(channel);
        return mplew.getPacket();
    }
    
    public static byte[] sendLieDetector(final byte[] image, final int attempt) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(6);
        mplew.write(4);
        mplew.write(2 - attempt);
        if (image == null) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(image.length);
        mplew.write(image);
        return mplew.getPacket();
    }
    
    public static byte[] LieDetectorResponse(final byte msg) {
        return LieDetectorResponse(msg, (byte)0);
    }
    
    public static byte[] LieDetectorResponse(final byte msg, final byte msg2) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(msg);
        mplew.write(msg2);
        return mplew.getPacket();
    }
    
    public static byte[] getLieDetector(final byte type, final String tester) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(type);
        switch (type) {
            case 4: {
                mplew.write(0);
                mplew.writeMapleAsciiString("");
                break;
            }
            case 5: {
                mplew.write(1);
                mplew.writeMapleAsciiString(tester);
                break;
            }
            case 6: {
                mplew.write(4);
                mplew.write(1);
                break;
            }
            case 7: {
                mplew.write(4);
                break;
            }
            case 9: {
                mplew.write(0);
                break;
            }
            case 8: {
                mplew.write(0);
                mplew.writeMapleAsciiString("");
                break;
            }
            case 10: {
                mplew.write(0);
                mplew.writeMapleAsciiString("");
                break;
            }
            default: {
                mplew.write(0);
                break;
            }
        }
        return mplew.getPacket();
    }
    
    public static byte[] lieDetector(final byte mode, final byte action, final byte[] image, final String str1, final String str2, final String str3) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.LIE_DETECTOR.getValue());
        mplew.write(mode);
        mplew.write(action);
        if (mode == 6) {
            mplew.write(1);
            PacketHelper.addImageInfo(mplew, image);
        }
        if (mode == 7 || mode == 9) {}
        if (mode == 4) {
            mplew.writeMapleAsciiString(str1);
        }
        if (mode != 5) {
            if (mode == 10) {
                mplew.writeMapleAsciiString(str2);
            }
            else {
                if (mode != 8) {}
                mplew.writeMapleAsciiString(str2);
            }
        }
        mplew.writeMapleAsciiString(str3);
        return mplew.getPacket();
    }
    
    public static byte[] arrangeStorage(final byte slots, final Collection<IItem> items, final boolean changed) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_STORAGE.getValue());
        mplew.write(15);
        mplew.write(slots);
        mplew.write(124);
        mplew.writeZeroBytes(10);
        mplew.write(items.size());
        for (final IItem item : items) {
            PacketHelper.addItemInfo(mplew, item, true, true);
        }
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] spawnKite(final int oid, final int itemid, final String name, final String msg, final Point pos, final int ft) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_KITE.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(itemid);
        mplew.writeMapleAsciiString(msg);
        mplew.writeMapleAsciiString(name);
        mplew.writeShort(pos.x);
        mplew.writeShort(ft);
        return mplew.getPacket();
    }
    
    public static byte[] destroyKite(final int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DESTROY_KITE.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }
    
    public static byte[] spawnKiteError() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_KITE_ERROR.getValue());
        return mplew.getPacket();
    }
    
    public static byte[] showSpecialAttack(final int chrId, final int pot_x, final int pot_y, final int display, final int skillId) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_SPECIAL_ATTACK.getValue());
        mplew.writeInt(chrId);
        mplew.writeInt(pot_x);
        mplew.writeInt(pot_y);
        mplew.writeInt(display);
        mplew.writeInt(skillId);
        return mplew.getPacket();
    }
    
    public static byte[] BeansGameMessage(final int cid, final int x, final String laba) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.TIP_BEANS.getValue());
        mplew.writeInt(cid);
        mplew.write(x);
        mplew.writeMapleAsciiString(laba);
        return mplew.getPacket();
    }
    
    public static byte[] updateBeans(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_BEANS.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getBeans());
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] 能量储存器(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.UPDATE_BEANS.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getBeans());
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] openBeans(final int beansCount, final int type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_BEANS.getValue());
        mplew.writeInt(beansCount);
        mplew.write(type);
        return mplew.getPacket();
    }
    
    public static byte[] BeansZJgeidd(final boolean type, final int a) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(type ? BeansType.獎勵豆豆效果.getType() : BeansType.獎勵豆豆效果B.getType());
        mplew.writeInt(a);
        mplew.write(5);
        return mplew.getPacket();
    }
    
    public static byte[] BeansZJgeiddB(final int a) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.獎勵豆豆效果B.getType());
        mplew.writeInt(a);
        mplew.write(0);
        return mplew.getPacket();
    }
    
    public static byte[] BeansHJG(final byte type) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.黃金狗.getType());
        mplew.write(type);
        return mplew.getPacket();
    }
    
    public static byte[] BeansJDCS(final int a, final int 加速旋转, final int 蓝, final int 绿, final int 红) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.顏色求進洞.getType());
        mplew.write(a);
        mplew.write(加速旋转);
        mplew.write(蓝);
        mplew.write(绿);
        mplew.write(红);
        return mplew.getPacket();
    }
    
    public static byte[] BeansJDXZ(final int a, final int 第一排, final int 第三排, final int 第二排, final int 启动打怪效果, final int 中奖率, final int 加速旋转, final boolean 关闭打击效果A, final boolean 关闭打击效果B) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.進洞旋轉.getType());
        mplew.write(a);
        mplew.write(第一排);
        mplew.write(第三排);
        mplew.write(第二排);
        mplew.write(启动打怪效果);
        if (启动打怪效果 > 0) {
            mplew.write(中奖率);
            mplew.writeInt(0);
        }
        mplew.write(加速旋转);
        mplew.writeBoolean(关闭打击效果A);
        mplew.writeBoolean(关闭打击效果B);
        return mplew.getPacket();
    }
    
    public static byte[] Beans_why() {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.未知效果.getType());
        return mplew.getPacket();
    }
    
    public static byte[] BeansUP(final int ITEM) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.領獎npc.getType());
        mplew.writeInt(ITEM);
        return mplew.getPacket();
    }
    
    public static byte[] showBeans(final List<MapleBeans> beansInfo) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOOT_BEANS.getValue());
        mplew.write(BeansType.開始打豆豆.getType());
        mplew.write(beansInfo.size());
        for (final MapleBeans bean : beansInfo) {
            mplew.writeShort(bean.getPos());
            mplew.write(bean.getType());
            mplew.writeInt(bean.getNumber());
        }
        return mplew.getPacket();
    }
    
    public static byte[] getTopMsg(final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.TOP_MSG.getValue());
        mplew.writeMapleAsciiString(msg);
        return mplew.getPacket();
    }
    
    public static byte[] showCharCash(MapleCharacter chr) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CHAR_CASH.getValue());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getCSPoints(2));
        return mplew.getPacket();
    }
    
    public static byte[] spawnDragon(final MapleDragon d) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DRAGON_SPAWN.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writeInt(d.getPosition().x);
        mplew.writeInt(d.getPosition().y);
        mplew.write(d.getStance());
        mplew.writeShort(0);
        mplew.writeShort(d.getJobId());
        return mplew.getPacket();
    }
    
    public static byte[] removeDragon(final int chrid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DRAGON_REMOVE.getValue());
        mplew.writeInt(chrid);
        return mplew.getPacket();
    }
    
    public static byte[] moveDragon(final MapleDragon d, final Point startPos, final List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DRAGON_MOVE.getValue());
        mplew.writeInt(d.getOwner());
        mplew.writePos(startPos);
        PacketHelper.serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }
    
    public static byte[] addInventorySlot(final MapleInventoryType type, final IItem item) {
        return addInventorySlot(type, item, false);
    }
    
    public static byte[] addInventorySlot(final MapleInventoryType type, final IItem item, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        if (fromDrop) {
            mplew.write(1);
        }
        else {
            mplew.write(0);
        }
        mplew.writeShort(1);
        mplew.write(type.getType());
        mplew.write((int)item.getPosition());
        PacketHelper.addItemInfo(mplew, item, true, false);
        return mplew.getPacket();
    }

    public static byte[] updateInventorySlot(final MapleInventoryType type, final IItem item, final boolean fromDrop) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write((int)(fromDrop ? 1 : 0));
        mplew.write(1);
        mplew.write(1);
        mplew.write(type.getType());
        mplew.writeShort((int)item.getPosition());
        mplew.writeShort((int)item.getQuantity());
        return mplew.getPacket();
    }
    public static byte[] addInventorySlot(MapleInventoryType type, Item item, boolean fromDrop) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.MODIFY_INVENTORY_ITEM.getValue());
        mplew.write(fromDrop ? 1 : 0);
        mplew.write(1);
        //mplew.write(0);
        mplew.write(GameConstants.isInBag(item.getPosition(), type.getType()) ? 9 : 0);
        mplew.write(type.getType());
        mplew.writeShort(item.getPosition());
        PacketHelper.addItemInfo(mplew, item);
        return mplew.getPacket();
    }
    public static byte[] sendHammer效果(boolean start, int hammered) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.VICIOUS_HAMMER.getValue());
        mplew.write(start ? 52 : 51);
        mplew.writeInt(0);
        if (start) {
            mplew.writeInt(hammered);
        } else {
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }
    public static byte[] openWeb(final String web) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.OPEN_WEB.getValue());
        mplew.writeMapleAsciiString(web);
        return mplew.getPacket();
    }
    
    public static byte[] showCustomRanks(final int npcid, final ResultSet rs) throws SQLException {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        }
        mplew.writeInt(rs.getRow());
        rs.beforeFirst();
        while (rs.next()) {
            mplew.writeMapleAsciiString(rs.getString("name"));
            mplew.writeInt(rs.getInt("data"));
            mplew.writeInt(rs.getInt("level"));
            mplew.writeInt(rs.getInt("meso"));
            mplew.writeInt(0);
            mplew.writeInt(0);
        }
        return mplew.getPacket();
    }
    
    static {
        SecondaryStatRemote = new int[4];
        final int[] secondaryStatRemote = MaplePacketCreator.SecondaryStatRemote;
        final int position = MapleBuffStat.召唤玩家1.getPosition();
        secondaryStatRemote[position] |= MapleBuffStat.召唤玩家1.getValue();
        final int[] secondaryStatRemote2 = MaplePacketCreator.SecondaryStatRemote;
        final int position2 = MapleBuffStat.召唤玩家2.getPosition();
        secondaryStatRemote2[position2] |= MapleBuffStat.召唤玩家2.getValue();
        final int[] secondaryStatRemote3 = MaplePacketCreator.SecondaryStatRemote;
        final int position3 = MapleBuffStat.召唤玩家3.getPosition();
        secondaryStatRemote3[position3] |= MapleBuffStat.召唤玩家3.getValue();
        final int[] secondaryStatRemote4 = MaplePacketCreator.SecondaryStatRemote;
        final int position4 = MapleBuffStat.召唤玩家4.getPosition();
        secondaryStatRemote4[position4] |= MapleBuffStat.召唤玩家4.getValue();
        final int[] secondaryStatRemote5 = MaplePacketCreator.SecondaryStatRemote;
        final int position5 = MapleBuffStat.召唤玩家5.getPosition();
        secondaryStatRemote5[position5] |= MapleBuffStat.召唤玩家5.getValue();
        final int[] secondaryStatRemote6 = MaplePacketCreator.SecondaryStatRemote;
        final int position6 = MapleBuffStat.召唤玩家6.getPosition();
        secondaryStatRemote6[position6] |= MapleBuffStat.召唤玩家6.getValue();
        final int[] secondaryStatRemote7 = MaplePacketCreator.SecondaryStatRemote;
        final int position7 = MapleBuffStat.召唤玩家7.getPosition();
        secondaryStatRemote7[position7] |= MapleBuffStat.召唤玩家7.getValue();
        final int[] secondaryStatRemote8 = MaplePacketCreator.SecondaryStatRemote;
        final int position8 = MapleBuffStat.召唤玩家8.getPosition();
        secondaryStatRemote8[position8] |= MapleBuffStat.召唤玩家8.getValue();
    }



    public static byte[] giveMount(MapleCharacter c, int buffid, int skillid, List<Pair<MapleBuffStat, Integer>> statups) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GIVE_BUFF.getValue());
        writeBuffState2(mplew, statups);
        mplew.writeShort(0);
        mplew.writeInt(buffid);
        mplew.writeInt(skillid);
        mplew.writeInt(0);
        mplew.writeShort(0);
        mplew.write(0);
        mplew.write(2);
        int a = giveBuff(buffid);
        if (a > 0) {
            mplew.write(a);
        }

        return mplew.getPacket();
    }

    public static int giveBuff(int buffid) {
        int a = 0;
        switch (buffid) {
            case -2022458:
            case 33121006:
                a = 6;
                break;
            case 1002:
            case 8000:
            case 1121000:
            case 1221000:
            case 1321000:
            case 2121000:
            case 2221000:
            case 2321000:
            case 3121000:
            case 3221000:
            case 4101004:
            case 4121000:
            case 4201003:
            case 4221000:
            case 4341000:
            case 5101007:
            case 5121000:
            case 5221000:
            case 5321005:
            case 9001001:
            case 10001002:
            case 10008000:
            case 14101003:
            case 20001002:
            case 20008000:
            case 20018000:
            case 21121000:
            case 22171000:
            case 23121005:
            case 30008000:
            case 31121004:
            case 32121007:
            case 33121007:
            case 35121007:
                a = 5;
                break;
            case 5111005:
            case 5121003:
            case 13111005:
            case 15111002:
                a = 7;
                break;
            case 5301003:
                a = 3;
                break;
            case 32101003:
                a = 29;
        }

        return a;
    }

    public static byte[] 显示战斗力排行_snail(int npcid, ArrayList<MapleGuild.战斗力信息> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        ArrayList<MapleGuild.战斗力信息> new_all = new ArrayList(all);
        int n = new_all.size();
        if (n > 100) {
            for(int i = 100; i < n; ++i) {
                new_all.remove(i);
                --i;
                --n;
            }
        }

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(new_all.size());
        Iterator var7 = new_all.iterator();

        while(var7.hasNext()) {
            MapleGuild.战斗力信息 info = (MapleGuild.战斗力信息)var7.next();
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt((int)info.getForce());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }

        return mplew.getPacket();
    }

    public static byte[] 显示力量排行_snail(int npcid, ArrayList<MapleGuild.战斗力信息> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        ArrayList<MapleGuild.战斗力信息> new_all = new ArrayList(all);
        int n = new_all.size();
        if (n > 100) {
            for(int i = 100; i < n; ++i) {
                new_all.remove(i);
                --i;
                --n;
            }
        }

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(new_all.size());
        Iterator var7 = new_all.iterator();

        while(var7.hasNext()) {
            MapleGuild.战斗力信息 info = (MapleGuild.战斗力信息)var7.next();
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getStr() + info.getE_str());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }

        return mplew.getPacket();
    }

    public static byte[] 显示敏捷排行_snail(int npcid, ArrayList<MapleGuild.战斗力信息> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        ArrayList<MapleGuild.战斗力信息> new_all = new ArrayList(all);
        int n = new_all.size();
        if (n > 100) {
            for(int i = 100; i < n; ++i) {
                new_all.remove(i);
                --i;
                --n;
            }
        }

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(new_all.size());
        Iterator var7 = new_all.iterator();

        while(var7.hasNext()) {
            MapleGuild.战斗力信息 info = (MapleGuild.战斗力信息)var7.next();
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getDex() + info.getE_dex());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }

        return mplew.getPacket();
    }

    public static byte[] 显示智力排行_snail(int npcid, ArrayList<MapleGuild.战斗力信息> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        ArrayList<MapleGuild.战斗力信息> new_all = new ArrayList(all);
        int n = new_all.size();
        if (n > 100) {
            for(int i = 100; i < n; ++i) {
                new_all.remove(i);
                --i;
                --n;
            }
        }

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(new_all.size());
        Iterator var7 = new_all.iterator();

        while(var7.hasNext()) {
            MapleGuild.战斗力信息 info = (MapleGuild.战斗力信息)var7.next();
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getInt() + info.getE_int());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }

        return mplew.getPacket();
    }

    public static byte[] 显示运气排行_snail(int npcid, ArrayList<MapleGuild.战斗力信息> all) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        ArrayList<MapleGuild.战斗力信息> new_all = new ArrayList(all);
        int n = new_all.size();
        if (n > 100) {
            for(int i = 100; i < n; ++i) {
                new_all.remove(i);
                --i;
                --n;
            }
        }

        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        mplew.writeInt(new_all.size());
        Iterator var7 = new_all.iterator();

        while(var7.hasNext()) {
            MapleGuild.战斗力信息 info = (MapleGuild.战斗力信息)var7.next();
            mplew.writeMapleAsciiString(info.getName());
            mplew.writeInt(info.getLuk() + info.getE_luk());
            mplew.writeInt(info.getStr());
            mplew.writeInt(info.getDex());
            mplew.writeInt(info.getInt());
            mplew.writeInt(info.getLuk());
        }

        return mplew.getPacket();
    }

    public static byte[] MapleMSpvpdeaths(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        } else {
            mplew.writeInt(rs.getRow());
            rs.beforeFirst();

            while(rs.next()) {
                mplew.writeMapleAsciiString(rs.getString("name"));
                mplew.writeInt(rs.getInt("pvpdeaths"));
                mplew.writeInt(rs.getInt("str"));
                mplew.writeInt(rs.getInt("dex"));
                mplew.writeInt(rs.getInt("int"));
                mplew.writeInt(rs.getInt("luk"));
            }

            return mplew.getPacket();
        }
    }
    public static byte[] MapleMSpvpkills(int npcid, ResultSet rs) throws SQLException {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendPacketOpcode.GUILD_OPERATION.getValue());
        mplew.write(73);
        mplew.writeInt(npcid);
        if (!rs.last()) {
            mplew.writeInt(0);
            return mplew.getPacket();
        } else {
            mplew.writeInt(rs.getRow());
            rs.beforeFirst();

            while(rs.next()) {
                mplew.writeMapleAsciiString(rs.getString("name"));
                mplew.writeInt(rs.getInt("pvpkills"));
                mplew.writeInt(rs.getInt("str"));
                mplew.writeInt(rs.getInt("dex"));
                mplew.writeInt(rs.getInt("int"));
                mplew.writeInt(rs.getInt("luk"));
            }

            return mplew.getPacket();
        }
    }
}
