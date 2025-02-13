package handling.world;

import client.*;
import constants.MapConstants;
import constants.ServerConfig;
import constants.WorldConstants;
import gui.LtMS;

import scripting.ReactorScriptManager;
import server.*;
import server.life.MapleLifeFactory;
import server.life.MapleMonsterInformationProvider;
import handling.world.family.MapleFamilyCharacter;
import handling.world.family.MapleFamily;
import handling.world.guild.MapleGuildAlliance;

import java.awt.*;
import java.util.*;

import handling.world.guild.MapleGuildSummary;
import handling.world.guild.MapleBBSThread;
import handling.world.guild.MapleGuildCharacter;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import handling.world.guild.MapleGuild;
import client.BuddyList.BuddyAddResult;
import client.BuddyList.BuddyOperation;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

import server.maps.MapleMapObject;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import tools.*;
import database.DBConPool;
import java.util.concurrent.atomic.AtomicInteger;

import server.Timer.EventTimer;
import tools.packet.PetPacket;
import client.inventory.MapleInventoryType;
import client.inventory.PetDataFactory;
import client.inventory.MaplePet;
import client.status.MonsterStatusEffect;
import server.life.MapleMonster;
import server.maps.MapleMapItem;
import server.maps.MapleMap;
import server.Timer.WorldTimer;
import handling.cashshop.CashShopServer;
import handling.channel.PlayerStorage;

import java.rmi.RemoteException;

import handling.channel.ChannelServer;
import tools.packet.PlayerShopPacket;

public class World
{
    public static boolean isShutDown;
    public static boolean isShopShutDown;

    private static long lastSuccessTime = 0L;
    private static long lastSuccessTimeI = 0L;
    private static long oldTime = 0L;
    public static ArrayList<Pair<MapleMonster, MapleMap>> bossAndMapRecord = new ArrayList<>();
    private static ArrayList<Pair<Integer, Integer>> outsideBoss = new ArrayList<>();
    private static ArrayList<Integer> outsideMap = new ArrayList<>();

    private static ArrayList<MapleMonster> durationMonsterList = new ArrayList<>();

    public static ArrayList<Integer> getOutsideMap() {
        return outsideMap;
    }
    public static void init() {
        Find.findChannel(0);
        Guild.lock.toString();
        Alliance.lock.toString();
        Family.lock.toString();
        Messenger.getMessenger(0);
        Party.getParty(0);
    }
    
    public static String getStatus() throws RemoteException {
        final StringBuilder ret = new StringBuilder();
        int totalUsers = 0;
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            for (MapleMap mapleMap : cs.getMapFactory().getAllMapThreadSafe()) {
                mapleMap.地图回收();
            }
            ret.append("頻道 ");
            ret.append(cs.getChannel());
            ret.append(": ");
            final int channelUsers = cs.getConnectedClients();
            totalUsers += channelUsers;
            ret.append(channelUsers);
            ret.append(" 個玩家\n");
        }
        ret.append("總共線上人數: ");
        ret.append(totalUsers);
        ret.append("\n");
        return ret.toString();
    }
    public static int 在线人数() {
        int count = 0;

        ChannelServer chl;
        for(Iterator var1 = ChannelServer.getAllInstances().iterator(); var1.hasNext(); count += chl.getPlayerStorage().getAllCharacters().size()) {
            chl = (ChannelServer)var1.next();
        }

        return count;
    }
    public static Map<Integer, Integer> getConnected() {
        final Map<Integer, Integer> ret = new HashMap<Integer, Integer>();
        int total = 0;
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            final int curConnected = cs.getConnectedClients();
            ret.put(Integer.valueOf(cs.getChannel()), Integer.valueOf(curConnected));
            total += curConnected;
        }
        ret.put(Integer.valueOf(0), Integer.valueOf(total));
        return ret;
    }
    public static void addDurationMonster(MapleMonster mob) {
        if (mob != null && mob.getDuration() > 0L) {
            durationMonsterList.add(mob);
        }

    }
    public static void clearDurationMonsterList() {
        durationMonsterList.clear();
    }

    public static ArrayList<MapleMonster> getDurationMonsterList() {
        return durationMonsterList;
    }

    public static void monitorDurationMonster(int sec) {
        EventTimer.getInstance().register(new Runnable() {
            public void run() {
                try {
                    if (!World.durationMonsterList.isEmpty()) {
                        for(int i = 0; i < World.durationMonsterList.size(); ++i) {
                            MapleMonster mob = (MapleMonster)World.durationMonsterList.get(i);
                            if (mob == null) {
                                World.durationMonsterList.remove(i);
                                --i;
                            } else if (mob.getLastDuration() <= 0L) {
                                MapleMap map = mob.getMap();
                                mob.setHp(0L);
                                if (map != null) {
                                    mob.getMap().killMonster(mob, true);
                                    if (!map.haveMonster(9900000) && !map.haveMonster(9900001) && !map.haveMonster(9900002)) {
                                        map.setHaveStone(false);
                                        map.setStoneLevel(0);
                                    }
                                }

                                World.durationMonsterList.remove(i);
                                --i;
                            }
                        }
                    }
                } catch (Exception var4) {
                    //服务端输出信息.println_err("【错误】monitorDurationMonster执行错误，错误原因：" + var4);
                    var4.printStackTrace();
                }

            }
        }, (long)(sec * 1000), (long)(sec * 1000));
    }
    public static List<CheaterData> getCheaters() {
        final List<CheaterData> allCheaters = new ArrayList<CheaterData>();
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            allCheaters.addAll((Collection<? extends CheaterData>)cs.getCheaters());
        }
        Collections.sort(allCheaters);
        return CollectionUtil.copyFirst(allCheaters, 10);
    }
    public static ArrayList<Integer> getOutsideMapSQL() {
        outsideMap.clear();
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT  * FROM snail_outside_map");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                outsideMap.add(rs.getInt("mapid"));
            }
        } catch (SQLException var3) {
            //服务端输出信息.println_err("getOutsideMap 错误，错误原因：" + var3);
        }

        return outsideMap;
    }
    public static boolean isConnected(final String charName) {
        return Find.findChannel(charName) > 0;
    }
    
    public static void toggleMegaphoneMuteState() {
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            cs.toggleMegaphoneMuteState();
        }
    }
    
    public static void channelChangeData(final CharacterTransfer Data, final int characterid, final int toChannel) {
        getStorage(toChannel).registerPendingPlayer(Data, characterid);
    }
    
    public static boolean isCharacterListConnected(final List<String> charName) {
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            for (final String c : charName) {
                if (cs.getPlayerStorage().getCharacterByName(c) != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean hasMerchant(final int accountID) {
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            if (cs.containsMerchant(accountID)) {
                return true;
            }
        }
        return false;
    }
    
    public static PlayerStorage getStorage(final int channel) {
        if (channel == -20) {
            return CashShopServer.getPlayerStorageMTS();
        }
        if (channel == -10) {
            return CashShopServer.getPlayerStorage();
        }
        return ChannelServer.getInstance(channel).getPlayerStorage();
    }
    
    public static int getPendingCharacterSize() {
        int ret = CashShopServer.getPlayerStorage().pendingCharacterSize();
        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
            ret += cserv.getPlayerStorage().pendingCharacterSize();
        }
        return ret;
    }
    
    public static void registerRespawn() {
        WorldTimer.getInstance().register((Runnable)new Respawn(),  (long)((Integer) LtMS.ConfigValuesMap.get("怪物刷新频率设定")));
    }
    public static void registerRespawn1() {//备用刷怪线程
        WorldTimer.getInstance().register((Runnable)new Respawn1(), LtMS.ConfigValuesMap.get("地图刷新频率"));
    }
    public static void handleMap(final MapleMap map, final int numTimes, final int size) {
        if (map.getItemsSize() > 0) {
            for ( MapleMapItem item : map.getAllItemsThreadsafe()) {
                if (item.shouldExpire()) {
                    item.expire(map);
                }
                else {
                    if (!item.shouldFFA()) {
                        continue;
                    }
                    item.setDropType((byte)2);
                }
            }
        }
        //刷怪判斷輪迴 轮回石碑
        if (map.characterSize() > 0) {
            if (map.canSpawn()) {
                map.respawn(false);
            }
             boolean hurt = map.canHurt();
            for (MapleCharacter chr : map.getCharactersThreadsafe()) {
                handleCooldowns(chr, numTimes, hurt);
            }
            if (map.getMobsSize() > 0) {
                for ( MapleMonster mons : map.getAllMonstersThreadsafe()) {
                    if (mons.isAlive() && mons.getStatiSize() > 0) {
                        for ( MonsterStatusEffect mse : mons.getAllBuffs()) {
                            try {
                                if (mse.shouldCancel()) {
                                    mons.cancelSingleStatus(mse);
                                }
                            } catch (Exception e) {
                                ////e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void scheduleRateDelay(final String type, final long delay) {
        WorldTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                final String rate = type;
                if (rate.equals((Object)"经验")) {
                    for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
                        cservs.setExpRate(Integer.parseInt(ServerProperties.getProperty("LtMS.expRate")));
                        cservs.broadcastPacket(MaplePacketCreator.serverNotice(6, "[系统公告]：经验倍率活动已经结束，已经恢复正常值。"));
                        ServerProperties.setProperty("expRate","0");
                    }
                }
                else if (rate.equals((Object)"爆率")) {
                    for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
                        cservs.setDropRate(Integer.parseInt(ServerProperties.getProperty("LtMS.dropRate")));
                        cservs.broadcastPacket(MaplePacketCreator.serverNotice(6, "[系统公告]：爆物倍率活动已经结束，已经恢复正常值。"));
                        ServerProperties.setProperty("dropRate","0");
                    }
                }
                else if (rate.equals((Object)"金币")) {
                    for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
                        cservs.setMesoRate(Integer.parseInt(ServerProperties.getProperty("LtMS.mesoRate")));
                        cservs.broadcastPacket(MaplePacketCreator.serverNotice(6, "[系统公告]：金币倍率活动已经结束，已经恢复正常值。"));
                        ServerProperties.setProperty("mesoRate","0");
                    }
                }
                else if (rate.equals((Object)"宠物经验")) {}
                for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
                    cservs.broadcastPacket(MaplePacketCreator.serverNotice(6, " 系统双倍活动已经结束。系统已成功自动切换为正常游戏模式！"));
                }
            }
        }, delay * 1000L);
    }
    
    public static void handleCooldowns(MapleCharacter chr, final int numTimes, final boolean hurt) {
        final long now = System.currentTimeMillis();
        try {
            for (final MapleCoolDownValueHolder m : chr.getCooldowns()) {
                if (m.startTime + m.length < now) {
                    final int skil = m.skillId;
                    chr.removeCooldown(skil);
                    chr.getClient().sendPacket(MaplePacketCreator.skillCooldown(skil, 0));
                }
            }
        } catch (Exception e) {
            if (LtMS.ConfigValuesMap.get("开启负数数组检测")>1) {
                chr.getClient().disconnect(true, false);
                chr.getClient().getSession().close();
                return;
            }
        }
        if (chr.getDiseaseSize() > 0) {
            for (final MapleDiseaseValueHolder i : chr.getAllDiseases()) {
                if (i != null && i.startTime + i.length < now) {
                    chr.dispelDebuff(i.disease);
                }
            }
        }
        for (final MapleDiseaseValueHolder i : chr.getAllDiseases()) {
            if (i.startTime + i.length < now) {
                chr.dispelDebuff(i.disease);
            }
        }
        if (numTimes % 100 == 0) {
            for (final MaplePet pet : chr.getSummonedPets()) {
                if (pet.getSummoned()) {
                    if (pet.getPetItemId() == 5000054 && pet.getSecondsLeft() > 0) {
                        pet.setLimitedLife(pet.getSecondsLeft() - 1);
                        if (pet.getSecondsLeft() <= 0) {
                            chr.unequipPet(pet, true);
                            return;
                        }
                    }
                    final int newFullness = pet.getFullness() - PetDataFactory.getHunger(pet.getPetItemId());
                    if (newFullness <= 5) {
                        pet.setFullness(15);
                        chr.unequipPet(pet, true);
                    }
                    else {
                        pet.setFullness(newFullness);
                        chr.getClient().sendPacket(PetPacket.updatePet(pet, chr.getInventory(MapleInventoryType.CASH).getItem(pet.getInventoryPosition())));
                    }
                }
            }
        }
        if (chr.isAlive()) {
            if (chr.canRecover(now)) {
                chr.doRecovery();
            }
            if (hurt && chr.getInventory(MapleInventoryType.EQUIPPED).findById(chr.getMap().getHPDecProtect()) == null) {
                if (chr.getMapId() == 749040100 && chr.getInventory(MapleInventoryType.CASH).findById(5451000) == null) {
                    chr.addHP(-chr.getMap().getHPDec());
                }
                else if (chr.getMapId() != 749040100) {
                    chr.addHP(-(chr.getMap().getHPDec() - ((chr.getBuffedValue(MapleBuffStat.HP_LOSS_GUARD) == null) ? 0 : ((int)chr.getBuffedValue(MapleBuffStat.HP_LOSS_GUARD)))));
                }
            }
        }
    }
    
    public static void AutoClean(final int mapid) {
        EventTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                    final MapleMap map = cserv.getMapFactory().getMap(mapid);
                    map.killAllMonsters(false);
                    map.removeDrops();
                }
            }
        }, 600000L, 600000L);
    }
    
    public static void GainGash(final int min) {
        EventTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                final int quantity = Randomizer.rand(10, 30);
                for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : cs.getPlayerStorage().getAllCharactersThreadSafe()) {
                        if (chr == null) {
                            break;
                        }
                        if (!chr.isAlive()) {
                            break;
                        }
                        final int gain = quantity;
                        chr.modifyCSPoints(1, gain, true);
                    }
                }
            }
        }, (long)(min * 60 * 1000), (long)(min * 60 * 1000));
    }
    
    public static void GainNX(final int min) {
        EventTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                final Map<MapleCharacter, Integer> GiveList = new HashMap<MapleCharacter, Integer>();
                final int quantity = Randomizer.rand(15, 35);
                for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : cs.getPlayerStorage().getAllCharactersThreadSafe()) {
                        if (chr == null) {
                            break;
                        }
                        if (!chr.isAlive()) {
                            break;
                        }
                        final int gain = quantity;
                        GiveList.put(chr, Integer.valueOf(gain));
                    }
                }
                if (!GiveList.isEmpty()) {
                    MapleCharacter.setMP(GiveList, true);
                }
            }
        }, (long)(min * 60 * 1000), (long)(min * 60 * 1000));
    }
    
    public static void clearChannelChangeDataByAccountId(final int accountid) {
        try {
            for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                getStorage(cs.getChannel()).deregisterPendingPlayerByAccountId(accountid);
            }
            getStorage(-20).deregisterPendingPlayerByAccountId(accountid);
            getStorage(-10).deregisterPendingPlayerByAccountId(accountid);
        }
        catch (Exception ex) {}
    }
    
    static {
        World.isShutDown = false;
        World.isShopShutDown = false;
    }
    
    public static class Party
    {
        private static Map<Integer, MapleParty> parties;
        private static final AtomicInteger runningPartyId;
        
        public static void partyChat(final int partyid, final String chattext, final String namefrom) {
            final MapleParty party = getParty(partyid);
            if (party == null) {
                throw new IllegalArgumentException("no party with the specified partyid exists");
            }
            for (final MaplePartyCharacter partychar : party.getMembers()) {
                final int ch = Find.findChannel(partychar.getName());
                if (ch > 0) {
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (chr == null || chr.getName().equalsIgnoreCase(namefrom)) {
                        continue;
                    }
                    chr.getClient().sendPacket(MaplePacketCreator.multiChat(namefrom, chattext, 1));
                }
            }
        }
        
        public static void updateParty(final int partyid, final PartyOperation operation, final MaplePartyCharacter target) {
            final MapleParty party = getParty(partyid);
            if (party == null) {
                return;
            }
            switch (operation) {
                case JOIN: {
                    party.addMember(target);
                    break;
                }
                case EXPEL:
                case LEAVE: {
                    party.removeMember(target);
                    break;
                }
                case DISBAND: {
                    disbandParty(partyid);
                    break;
                }
                case SILENT_UPDATE:
                case LOG_ONOFF: {
                    party.updateMember(target);
                    break;
                }
                case CHANGE_LEADER:
                case CHANGE_LEADER_DC: {
                    party.setLeader(target);
                    break;
                }
                default: {
                    throw new RuntimeException("Unhandeled updateParty operation " + operation.name());
                }
            }
            for (final MaplePartyCharacter partychar : party.getMembers()) {
                final int ch = Find.findChannel(partychar.getName());
                if (ch > 0) {
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(partychar.getName());
                    if (chr == null) {
                        continue;
                    }
                    if (operation == PartyOperation.DISBAND) {
                        chr.setParty(null);
                    }
                    else {
                        chr.setParty(party);
                    }
                    chr.getClient().sendPacket(MaplePacketCreator.updateParty(chr.getClient().getChannel(), party, operation, target));
                }
            }
            switch (operation) {
                case EXPEL:
                case LEAVE: {
                    final int ch2 = Find.findChannel(target.getName());
                    if (ch2 <= 0) {
                        break;
                    }
                    MapleCharacter chr2 = ChannelServer.getInstance(ch2).getPlayerStorage().getCharacterByName(target.getName());
                    if (chr2 != null) {
                        chr2.getClient().sendPacket(MaplePacketCreator.updateParty(chr2.getClient().getChannel(), party, operation, target));
                        chr2.setParty(null);
                        break;
                    }
                    break;
                }
            }
        }
        
        public static MapleParty createParty(final MaplePartyCharacter chrfor) {
            final int partyid = Party.runningPartyId.getAndIncrement();
            final MapleParty party = new MapleParty(partyid, chrfor);
            Party.parties.put(Integer.valueOf(party.getId()), party);
            return party;
        }
        
        public static MapleParty getParty(final int partyid) {
            return (MapleParty)Party.parties.get((Object)Integer.valueOf(partyid));
        }
        
        public static MapleParty disbandParty(final int partyid) {
            return (MapleParty)Party.parties.remove((Object)Integer.valueOf(partyid));
        }
        
        static {
            Party.parties = new HashMap<Integer, MapleParty>();
            runningPartyId = new AtomicInteger();
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final PreparedStatement ps = con.prepareStatement("SELECT MAX(party)+2 FROM characters");
                try (final ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    Party.runningPartyId.set(rs.getInt(1));
                }
                ps.close();
            }
            catch (SQLException e) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            }
        }
    }
    
    public static class Buddy
    {
        public static void buddyChat(final int[] recipientCharacterIds, final int cidFrom, final String nameFrom, final String chattext) {
            for (final int characterId : recipientCharacterIds) {
                final int ch = Find.findChannel(characterId);
                if (ch > 0) {
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(characterId);
                    if (chr != null && chr.getBuddylist().containsVisible(cidFrom)) {
                        chr.getClient().sendPacket(MaplePacketCreator.multiChat(nameFrom, chattext, 0));
                    }
                }
            }
        }
        
        private static void updateBuddies(final int characterId, final int channel, final Collection<Integer> buddies, final boolean offline, final int gmLevel, final boolean isHidden) {
            for (final Integer buddy : buddies) {
                final int ch = Find.findChannel((int)buddy);
                if (ch > 0) {
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById((int)buddy);
                    if (chr == null) {
                        continue;
                    }
                    final BuddyEntry ble = chr.getBuddylist().get(characterId);
                    if (ble == null || !ble.isVisible()) {
                        continue;
                    }
                    int mcChannel;
                    if (offline || (isHidden && chr.getGMLevel() < gmLevel)) {
                        ble.setChannel(-1);
                        mcChannel = -1;
                    }
                    else {
                        ble.setChannel(channel);
                        mcChannel = channel - 1;
                    }
                    chr.getBuddylist().put(ble);
                    chr.getClient().sendPacket(MaplePacketCreator.updateBuddyChannel(ble.getCharacterId(), mcChannel));
                }
            }
        }
        
        public static void buddyChanged(final int cid, final int cidFrom, final String name, final int channel, final BuddyOperation operation, final int level, final int job, final String group) {
            final int ch = Find.findChannel(cid);
            if (ch > 0) {
                final MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(cid);
                if (addChar != null) {
                    final BuddyList buddylist = addChar.getBuddylist();
                    switch (operation) {
                        case ADDED: {
                            if (buddylist.contains(cidFrom)) {
                                buddylist.put(new BuddyEntry(name, cidFrom, group, channel, true, level, job));
                                addChar.getClient().sendPacket(MaplePacketCreator.updateBuddyChannel(cidFrom, channel - 1));
                                break;
                            }
                            break;
                        }
                        case DELETED: {
                            if (buddylist.contains(cidFrom)) {
                                buddylist.put(new BuddyEntry(name, cidFrom, group, -1, buddylist.get(cidFrom).isVisible(), level, job));
                                addChar.getClient().sendPacket(MaplePacketCreator.updateBuddyChannel(cidFrom, -1));
                                break;
                            }
                            break;
                        }
                    }
                }
            }
        }
        
        public static BuddyAddResult requestBuddyAdd(final String addName, final int channelFrom, final int cidFrom, final String nameFrom, final int levelFrom, final int jobFrom) {
            final int ch = Find.findChannel(addName);
            if (ch > 0) {
                final MapleCharacter addChar = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(addName);
                if (addChar != null) {
                    final BuddyList buddylist = addChar.getBuddylist();
                    if (buddylist.isFull()) {
                        return BuddyAddResult.BUDDYLIST_FULL;
                    }
                    if (!buddylist.contains(cidFrom)) {
                        buddylist.addBuddyRequest(addChar.getClient(), cidFrom, nameFrom, channelFrom, levelFrom, jobFrom);
                    }
                    else if (buddylist.containsVisible(cidFrom)) {
                        return BuddyAddResult.ALREADY_ON_LIST;
                    }
                }
            }
            return BuddyAddResult.OK;
        }
        
        public static void loggedOn(final String name, final int characterId, final int channel, final Collection<Integer> buddies, final int gmLevel, final boolean isHidden) {
            updateBuddies(characterId, channel, buddies, false, gmLevel, isHidden);
        }
        
        public static void loggedOff(final String name, final int characterId, final int channel, final Collection<Integer> buddies, final int gmLevel, final boolean isHidden) {
            updateBuddies(characterId, channel, buddies, true, gmLevel, isHidden);
        }
    }
    
    public static class Messenger
    {
        private static final Map<Integer, MapleMessenger> messengers;
        private static final AtomicInteger runningMessengerId;
        
        public static MapleMessenger createMessenger(final MapleMessengerCharacter chrfor) {
            final int messengerid = Messenger.runningMessengerId.getAndIncrement();
            final MapleMessenger messenger = new MapleMessenger(messengerid, chrfor);
            Messenger.messengers.put(Integer.valueOf(messenger.getId()), messenger);
            return messenger;
        }
        
        public static void declineChat(final String target, final String namefrom) {
            final int ch = Find.findChannel(target);
            if (ch > 0) {
                final ChannelServer cs = ChannelServer.getInstance(ch);
                MapleCharacter chr = cs.getPlayerStorage().getCharacterByName(target);
                if (chr != null) {
                    final MapleMessenger messenger = chr.getMessenger();
                    if (messenger != null) {
                        chr.getClient().sendPacket(MaplePacketCreator.messengerNote(namefrom, 5, 0));
                    }
                }
            }
        }
        
        public static MapleMessenger getMessenger(final int messengerid) {
            return (MapleMessenger)Messenger.messengers.get((Object)Integer.valueOf(messengerid));
        }
        
        public static void leaveMessenger(final int messengerid, final MapleMessengerCharacter target) {
            final MapleMessenger messenger = getMessenger(messengerid);
            if (messenger == null) {
                throw new IllegalArgumentException("No messenger with the specified messengerid exists");
            }
            final int position = messenger.getPositionByName(target.getName());
            messenger.removeMember(target);
            for (final MapleMessengerCharacter mmc : messenger.getMembers()) {
                if (mmc != null) {
                    final int ch = Find.findChannel(mmc.getId());
                    if (ch <= 0) {
                        continue;
                    }
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(mmc.getName());
                    if (chr == null) {
                        continue;
                    }
                    chr.getClient().sendPacket(MaplePacketCreator.removeMessengerPlayer(position));
                }
            }
        }
        
        public static void silentLeaveMessenger(final int messengerid, final MapleMessengerCharacter target) {
            final MapleMessenger messenger = getMessenger(messengerid);
            if (messenger == null) {
                throw new IllegalArgumentException("No messenger with the specified messengerid exists");
            }
            messenger.silentRemoveMember(target);
        }
        
        public static void silentJoinMessenger(final int messengerid, final MapleMessengerCharacter target) {
            final MapleMessenger messenger = getMessenger(messengerid);
            if (messenger == null) {
                throw new IllegalArgumentException("No messenger with the specified messengerid exists");
            }
            messenger.silentAddMember(target);
        }
        
        public static void updateMessenger(final int messengerid, final String namefrom, final int fromchannel) {
            final MapleMessenger messenger = getMessenger(messengerid);
            final int position = messenger.getPositionByName(namefrom);
            for (final MapleMessengerCharacter messengerchar : messenger.getMembers()) {
                if (messengerchar != null && !messengerchar.getName().equals((Object)namefrom)) {
                    final int ch = Find.findChannel(messengerchar.getName());
                    if (ch <= 0) {
                        continue;
                    }
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                    if (chr == null) {
                        continue;
                    }
                    final MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(namefrom);
                    chr.getClient().sendPacket(MaplePacketCreator.updateMessengerPlayer(namefrom, from, position, fromchannel - 1));
                }
            }
        }
        
        public static void joinMessenger(final int messengerid, final MapleMessengerCharacter target, final String from, final int fromchannel) {
            final MapleMessenger messenger = getMessenger(messengerid);
            if (messenger == null) {
                throw new IllegalArgumentException("No messenger with the specified messengerid exists");
            }
            messenger.addMember(target);
            final int position = messenger.getPositionByName(target.getName());
            for (final MapleMessengerCharacter messengerchar : messenger.getMembers()) {
                if (messengerchar != null) {
                    final int mposition = messenger.getPositionByName(messengerchar.getName());
                    final int ch = Find.findChannel(messengerchar.getName());
                    if (ch <= 0) {
                        continue;
                    }
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                    if (chr == null) {
                        continue;
                    }
                    if (!messengerchar.getName().equals((Object)from)) {
                        final MapleCharacter fromCh = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(from);
                        chr.getClient().sendPacket(MaplePacketCreator.addMessengerPlayer(from, fromCh, position, fromchannel - 1));
                        fromCh.getClient().sendPacket(MaplePacketCreator.addMessengerPlayer(chr.getName(), chr, mposition, messengerchar.getChannel() - 1));
                    }
                    else {
                        chr.getClient().sendPacket(MaplePacketCreator.joinMessenger(mposition));
                    }
                }
            }
        }
        
        public static void messengerChat(final int messengerid, final String chattext, final String namefrom) {
            final MapleMessenger messenger = getMessenger(messengerid);
            if (messenger == null) {
                throw new IllegalArgumentException("No messenger with the specified messengerid exists");
            }
            for (final MapleMessengerCharacter messengerchar : messenger.getMembers()) {
                if (messengerchar != null && !messengerchar.getName().equals((Object)namefrom)) {
                    final int ch = Find.findChannel(messengerchar.getName());
                    if (ch <= 0) {
                        continue;
                    }
                    MapleCharacter chr = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                    if (chr == null) {
                        continue;
                    }
                    chr.getClient().sendPacket(MaplePacketCreator.messengerChat(chattext));
                }
                else {
                    if (messengerchar == null) {
                        continue;
                    }
                    final int ch = Find.findChannel(messengerchar.getName());
                    if (ch <= 0) {
                        continue;
                    }
                    ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(messengerchar.getName());
                }
            }
        }
        
        public static void messengerInvite(final String sender, final int messengerid, final String target, final int fromchannel, final boolean gm) {
            if (World.isConnected(target)) {
                final int ch = Find.findChannel(target);
                if (ch > 0) {
                    final MapleCharacter from = ChannelServer.getInstance(fromchannel).getPlayerStorage().getCharacterByName(sender);
                    final MapleCharacter targeter = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterByName(target);
                    if (from != null) {
                        if (targeter != null && targeter.getMessenger() == null) {
                            if (!targeter.isGM() || gm) {
                                targeter.getClient().sendPacket(MaplePacketCreator.messengerInvite(sender, messengerid));
                                from.getClient().sendPacket(MaplePacketCreator.messengerNote(target, 4, 1));
                            }
                            else {
                                from.getClient().sendPacket(MaplePacketCreator.messengerNote(target, 4, 0));
                            }
                        }
                        else {
                            from.getClient().sendPacket(MaplePacketCreator.messengerChat(sender + " : " + target + " is already using Maple Messenger"));
                        }
                    }
                }
            }
        }
        
        static {
            messengers = new HashMap<Integer, MapleMessenger>();
            (runningMessengerId = new AtomicInteger()).set(1);
        }
    }
    
    public static class Guild
    {
        private static final Map<Integer, MapleGuild> guilds;
        private static final ReentrantReadWriteLock lock;
        
        public static int createGuild(final int leaderId, final String name) {
            return MapleGuild.createGuild(leaderId, name);
        }
        
        public static MapleGuild getGuild(final int id) {
            MapleGuild ret = null;
            Guild.lock.readLock().lock();
            try {
                ret = (MapleGuild)Guild.guilds.get((Object)Integer.valueOf(id));
            }
            finally {
                Guild.lock.readLock().unlock();
            }
            if (ret == null) {
                Guild.lock.writeLock().lock();
                try {
                    ret = new MapleGuild(id);
                    if (ret == null || ret.getId() <= 0 || !ret.isProper()) {
                        return null;
                    }
                    Guild.guilds.put(Integer.valueOf(id), ret);
                }
                finally {
                    Guild.lock.writeLock().unlock();
                }
            }
            return ret;
        }
        
        public static MapleGuild getGuildByName(final String guildName) {
            Guild.lock.readLock().lock();
            try {
                for (final MapleGuild g : Guild.guilds.values()) {
                    if (g.getName().equalsIgnoreCase(guildName)) {
                        return g;
                    }
                }
                return null;
            }
            finally {
                Guild.lock.readLock().unlock();
            }
        }
        
        public static MapleGuild getGuild(final MapleCharacter mc) {
            return getGuild(mc.getGuildId());
        }
        
        public static void setGuildMemberOnline(final MapleGuildCharacter mc, final boolean bOnline, final int channel) {
            final MapleGuild g = getGuild(mc.getGuildId());
            if (g != null) {
                g.setOnline(mc.getId(), bOnline, channel);
            }
        }
        
        public static void guildPacket(final int gid, final byte[] message) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.broadcast(message);
            }
        }
        
        public static int addGuildMember(final MapleGuildCharacter mc) {
            return addGuildMember(mc, true);
        }
        
        public static int addGuildMember(final MapleGuildCharacter mc, final boolean show) {
            final MapleGuild g = getGuild(mc.getGuildId());
            if (g != null) {
                return g.addGuildMember(mc, show);
            }
            return 0;
        }
        
        public static void leaveGuild(final MapleGuildCharacter mc) {
            final MapleGuild g = getGuild(mc.getGuildId());
            if (g != null) {
                g.leaveGuild(mc);
            }
        }
        
        public static void guildChat(final int gid, final String name, final int cid, final String msg) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.guildChat(name, cid, msg);
            }
        }
        
        public static void changeRank(final int gid, final int cid, final int newRank) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.changeRank(cid, newRank);
            }
        }
        
        public static void expelMember(final MapleGuildCharacter initiator, final String name, final int cid) {
            final MapleGuild g = getGuild(initiator.getGuildId());
            if (g != null) {
                g.expelMember(initiator, name, cid);
            }
        }
        
        public static void setGuildNotice(final int gid, final String notice) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.setGuildNotice(notice);
            }
        }
        
        public static void memberLevelJobUpdate(final MapleGuildCharacter mc) {
            final MapleGuild g = getGuild(mc.getGuildId());
            if (g != null) {
                g.memberLevelJobUpdate(mc);
            }
        }
        
        public static void changeRankTitle(final int gid, final String[] ranks) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.changeRankTitle(ranks);
            }
        }
        
        public static void setGuildEmblem(final int gid, final short bg, final byte bgcolor, final short logo, final byte logocolor) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.setGuildEmblem(bg, bgcolor, logo, logocolor);
            }
        }
        
        public static void disbandGuild(final int gid) {
            final MapleGuild g = getGuild(gid);
            Guild.lock.writeLock().lock();
            try {
                if (g != null) {
                    g.disbandGuild();
                    Guild.guilds.remove((Object)Integer.valueOf(gid));
                }
            }
            finally {
                Guild.lock.writeLock().unlock();
            }
        }
        
        public static void deleteGuildCharacter(final int guildid, final int charid) {
            final MapleGuild g = getGuild(guildid);
            if (g != null) {
                final MapleGuildCharacter mc = g.getMGC(charid);
                if (mc != null) {
                    if (mc.getGuildRank() > 1) {
                        g.leaveGuild(mc);
                    }
                    else {
                        g.disbandGuild();
                    }
                }
            }
        }
        
        public static boolean increaseGuildCapacity(final int gid) {
            final MapleGuild g = getGuild(gid);
            return g != null && g.increaseCapacity();
        }
        
        public static void gainGP(final int gid, final int amount) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.gainGP(amount);
            }
        }
        
        public static int getGP(final int gid) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                return g.getGP();
            }
            return 0;
        }
        
        public static int getInvitedId(final int gid) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                return g.getInvitedId();
            }
            return 0;
        }
        
        public static void setInvitedId(final int gid, final int inviteid) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                g.setInvitedId(inviteid);
            }
        }
        
        public static int getGuildLeader(final String guildName) {
            final MapleGuild mga = getGuildByName(guildName);
            if (mga != null) {
                return mga.getLeaderId();
            }
            return 0;
        }
        
        public static void save() {
            System.out.println("储存公会资料中");
            Guild.lock.writeLock().lock();
            try {
                for (final MapleGuild a : Guild.guilds.values()) {
                    a.writeToDB(false);
                }
            }
            finally {
                Guild.lock.writeLock().unlock();
            }
        }
        
        public static List<MapleBBSThread> getBBS(final int gid) {
            final MapleGuild g = getGuild(gid);
            if (g != null) {
                return g.getBBS();
            }
            return null;
        }
        
        public static int addBBSThread(final int guildid, final String title, final String text, final int icon, final boolean bNotice, final int posterID) {
            final MapleGuild g = getGuild(guildid);
            if (g != null) {
                return g.addBBSThread(title, text, icon, bNotice, posterID);
            }
            return -1;
        }
        
        public static void editBBSThread(final int guildid, final int localthreadid, final String title, final String text, final int icon, final int posterID, final int guildRank) {
            final MapleGuild g = getGuild(guildid);
            if (g != null) {
                g.editBBSThread(localthreadid, title, text, icon, posterID, guildRank);
            }
        }
        
        public static void deleteBBSThread(final int guildid, final int localthreadid, final int posterID, final int guildRank) {
            final MapleGuild g = getGuild(guildid);
            if (g != null) {
                g.deleteBBSThread(localthreadid, posterID, guildRank);
            }
        }
        
        public static void addBBSReply(final int guildid, final int localthreadid, final String text, final int posterID) {
            final MapleGuild g = getGuild(guildid);
            if (g != null) {
                g.addBBSReply(localthreadid, text, posterID);
            }
        }
        
        public static void deleteBBSReply(final int guildid, final int localthreadid, final int replyid, final int posterID, final int guildRank) {
            final MapleGuild g = getGuild(guildid);
            if (g != null) {
                g.deleteBBSReply(localthreadid, replyid, posterID, guildRank);
            }
        }
        
        public static void changeEmblem(final int gid, final int affectedPlayers, final MapleGuildSummary mgs) {
            Broadcast.sendGuildPacket(affectedPlayers, MaplePacketCreator.guildEmblemChange(gid, mgs.getLogoBG(), mgs.getLogoBGColor(), mgs.getLogo(), mgs.getLogoColor()), -1, gid);
            setGuildAndRank(affectedPlayers, -1, -1, -1);
        }
        
        public static void setGuildAndRank(final int cid, final int guildid, final int rank, final int alliancerank) {
            final int ch = Find.findChannel(cid);
            if (ch == -1) {
                return;
            }
            final MapleCharacter mc = World.getStorage(ch).getCharacterById(cid);
            if (mc == null) {
                return;
            }
            boolean bDifferentGuild;
            if (guildid == -1 && rank == -1) {
                bDifferentGuild = true;
            }
            else {
                bDifferentGuild = (guildid != mc.getGuildId());
                mc.setGuildId(guildid);
                mc.setGuildRank((byte)rank);
                mc.setAllianceRank((byte)alliancerank);
                mc.saveGuildStatus();
            }
            if (bDifferentGuild && ch > 0) {
                mc.getMap().broadcastMessage(mc, MaplePacketCreator.removePlayerFromMap(cid), false);
                mc.getMap().broadcastMessage(mc, MaplePacketCreator.spawnPlayerMapobject(mc), false);
            }
        }
        
        static {
            guilds = new LinkedHashMap<Integer, MapleGuild>();
            lock = new ReentrantReadWriteLock();
            System.out.println("[正在加载] -> 游戏家族公会系统");
            final Collection<MapleGuild> allGuilds = MapleGuild.loadAll();
            for (final MapleGuild g : allGuilds) {
                if (g.isProper()) {
                    Guild.guilds.put(Integer.valueOf(g.getId()), g);
                }
            }
        }
    }
    
    public static class Broadcast
    {
        public static void broadcastSmega(int world, byte[] message) {
            Iterator var2 = ChannelServer.getAllInstances().iterator();

            while(true) {
                ChannelServer cs;
                do {
                    if (!var2.hasNext()) {
                        return;
                    }

                    cs = (ChannelServer)var2.next();
                } while(world != -1 && cs.channel != world);

                cs.broadcastSmega(message);
            }
        }

        public static void broadcastSmega(final byte[] message) {
            for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastSmega(message);
            }
        }
        
        public static void broadcastGashponmega(final byte[] message) {
            for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastGashponmega(message);
            }
        }
        
        public static void broadcastGMMessage(final byte[] message, final boolean 吸怪) {
            for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastGMMessage(message, 吸怪);
            }
        }
        
        public static void broadcastGMMessage(final byte[] message) {
            for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastGMMessage(message);
            }
        }
        
        public static void broadcastMessage(final byte[] message) {
            for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                cs.broadcastMessage(message);
            }
        }
        
        public static void sendPacket(final List<Integer> targetIds, final byte[] packet, final int exception) {
            final Iterator<Integer> iterator = targetIds.iterator();
            while (iterator.hasNext()) {
                final int i = (int)Integer.valueOf(iterator.next());
                if (i == exception) {
                    continue;
                }
                final int ch = Find.findChannel(i);
                if (ch < 0) {
                    continue;
                }
                final MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(i);
                if (c == null) {
                    continue;
                }
                c.getClient().sendPacket(packet);
            }
        }
        
        public static void sendGuildPacket(final int targetIds, final byte[] packet, final int exception, final int guildid) {
            if (targetIds == exception) {
                return;
            }
            final int ch = Find.findChannel(targetIds);
            if (ch < 0) {
                return;
            }
            final MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetIds);
            if (c != null && c.getGuildId() == guildid) {
                c.getClient().sendPacket(packet);
            }
        }
        
        public static void sendFamilyPacket(final int targetIds, final byte[] packet, final int exception, final int guildid) {
            if (targetIds == exception) {
                return;
            }
            final int ch = Find.findChannel(targetIds);
            if (ch < 0) {
                return;
            }
            final MapleCharacter c = ChannelServer.getInstance(ch).getPlayerStorage().getCharacterById(targetIds);
            if (c != null && c.getFamilyId() == guildid) {
                c.getClient().sendPacket(packet);
            }
        }
    }
    
    public static class Find
    {
        private static final ReentrantReadWriteLock lock;
        private static final HashMap<Integer, Integer> idToChannel;
        
        public static void register(final int id, final String name, final int channel) {
            Find.lock.writeLock().lock();
            try {
                Find.idToChannel.put(Integer.valueOf(id), Integer.valueOf(channel));
            }
            finally {
                Find.lock.writeLock().unlock();
            }
        }
        
        public static void forceDeregister(final int id) {
            Find.lock.writeLock().lock();
            try {
                Find.idToChannel.remove((Object)Integer.valueOf(id));
            }
            finally {
                Find.lock.writeLock().unlock();
            }
        }
        
        public static void forceDeregister(final String id) {
            Find.lock.writeLock().lock();
            Find.lock.writeLock().unlock();
        }
        
        public static void forceDeregister(final int id, final String name) {
            Find.lock.writeLock().lock();
            try {
                Find.idToChannel.remove((Object)Integer.valueOf(id));
            }
            finally {
                Find.lock.writeLock().unlock();
            }
        }
        
        public static int findChannel(final int id) {
            Find.lock.readLock().lock();
            Integer ret;
            try {
                ret = Find.idToChannel.get((Object)Integer.valueOf(id));
            }
            finally {
                Find.lock.readLock().unlock();
            }
            if (ret == null) {
                return -1;
            }
            if ((int)ret != -10 && (int)ret != -20 && ChannelServer.getInstance((int)ret) == null) {
                forceDeregister(id);
                return -1;
            }
            return (int)ret;
        }
        
        public static int findChannel(final String st) {
            Integer ret = null;
            Find.lock.readLock().lock();
            try {
                MapleCharacter target = null;
                for (final ChannelServer ch : ChannelServer.getAllInstances()) {
                    target = ch.getPlayerStorage().getCharacterByName(st);
                    if (target != null) {
                        ret = Integer.valueOf(ch.getChannel());
                    }
                }
            }
            finally {
                Find.lock.readLock().unlock();
            }
            if (ret == null) {
                return -1;
            }
            if ((int)ret != -10 && (int)ret != -20 && ChannelServer.getInstance((int)ret) == null) {
                forceDeregister(st);
                return -1;
            }
            return (int)ret;
        }
        
        public static CharacterIdChannelPair[] multiBuddyFind(final int charIdFrom, final Collection<Integer> characterIds) {
            final List<CharacterIdChannelPair> foundsChars = new ArrayList<CharacterIdChannelPair>(characterIds.size());
            for (final Integer i : characterIds) {
                final Integer channel = Integer.valueOf(findChannel((int)i));
                if ((int)channel > 0) {
                    foundsChars.add(new CharacterIdChannelPair((int)i, (int)channel));
                }
            }
            Collections.sort(foundsChars);
            return (CharacterIdChannelPair[])foundsChars.toArray(new CharacterIdChannelPair[foundsChars.size()]);
        }
        
        static {
            lock = new ReentrantReadWriteLock();
            idToChannel = new HashMap<Integer, Integer>();
        }
    }
    
    public static class Alliance
    {
        private static final Map<Integer, MapleGuildAlliance> alliances;
        private static final ReentrantReadWriteLock lock;
        
        public static MapleGuildAlliance getAlliance(final int allianceid) {
            MapleGuildAlliance ret = null;
            Alliance.lock.readLock().lock();
            try {
                ret = (MapleGuildAlliance)Alliance.alliances.get((Object)Integer.valueOf(allianceid));
            }
            finally {
                Alliance.lock.readLock().unlock();
            }
            if (ret == null) {
                Alliance.lock.writeLock().lock();
                try {
                    ret = new MapleGuildAlliance(allianceid);
                    if (ret.getId() <= 0) {
                        return null;
                    }
                    Alliance.alliances.put(Integer.valueOf(allianceid), ret);
                }
                finally {
                    Alliance.lock.writeLock().unlock();
                }
            }
            return ret;
        }
        
        public static int getAllianceLeader(final int allianceid) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            if (mga != null) {
                return mga.getLeaderId();
            }
            return 0;
        }
        
        public static void updateAllianceRanks(final int allianceid, final String[] ranks) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            if (mga != null) {
                mga.setRank(ranks);
            }
        }
        
        public static void updateAllianceNotice(final int allianceid, final String notice) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            if (mga != null) {
                mga.setNotice(notice);
            }
        }
        
        public static boolean canInvite(final int allianceid) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.getCapacity() > mga.getNoGuilds();
        }
        
        public static boolean changeAllianceLeader(final int allianceid, final int cid) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.setLeaderId(cid);
        }
        
        public static boolean changeAllianceRank(final int allianceid, final int cid, final int change) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.changeAllianceRank(cid, change);
        }
        
        public static boolean changeAllianceCapacity(final int allianceid) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.setCapacity();
        }
        
        public static boolean disbandAlliance(final int allianceid) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.disband();
        }
        
        public static boolean addGuildToAlliance(final int allianceid, final int gid) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.addGuild(gid);
        }
        
        public static boolean removeGuildFromAlliance(final int allianceid, final int gid, final boolean expelled) {
            final MapleGuildAlliance mga = getAlliance(allianceid);
            return mga != null && mga.removeGuild(gid, expelled);
        }
        
        public static void sendGuild(final int allianceid) {
            final MapleGuildAlliance alliance = getAlliance(allianceid);
            if (alliance != null) {
                sendGuild(MaplePacketCreator.getAllianceUpdate(alliance), -1, allianceid);
                sendGuild(MaplePacketCreator.getGuildAlliance(alliance), -1, allianceid);
            }
        }
        
        public static void sendGuild(final byte[] packet, final int exceptionId, final int allianceid) {
            final MapleGuildAlliance alliance = getAlliance(allianceid);
            if (alliance != null) {
                for (int i = 0; i < alliance.getNoGuilds(); ++i) {
                    final int gid = alliance.getGuildId(i);
                    if (gid > 0 && gid != exceptionId) {
                        Guild.guildPacket(gid, packet);
                    }
                }
            }
        }
        
        public static boolean createAlliance(final String alliancename, final int cid, final int cid2, final int gid, final int gid2) {
            final int allianceid = MapleGuildAlliance.createToDb(cid, alliancename, gid, gid2);
            if (allianceid <= 0) {
                return false;
            }
            final MapleGuild g = Guild.getGuild(gid);
            final MapleGuild g_ = Guild.getGuild(gid2);
            g.setAllianceId(allianceid);
            g_.setAllianceId(allianceid);
            g.changeARank(true);
            g_.changeARank(false);
            final MapleGuildAlliance alliance = getAlliance(allianceid);
            sendGuild(MaplePacketCreator.createGuildAlliance(alliance), -1, allianceid);
            sendGuild(MaplePacketCreator.getAllianceInfo(alliance), -1, allianceid);
            sendGuild(MaplePacketCreator.getGuildAlliance(alliance), -1, allianceid);
            sendGuild(MaplePacketCreator.changeAlliance(alliance, true), -1, allianceid);
            return true;
        }
        
        public static void allianceChat(final int gid, final String name, final int cid, final String msg) {
            final MapleGuild g = Guild.getGuild(gid);
            if (g != null) {
                final MapleGuildAlliance ga = getAlliance(g.getAllianceId());
                if (ga != null) {
                    for (int i = 0; i < ga.getNoGuilds(); ++i) {
                        final MapleGuild g_ = Guild.getGuild(ga.getGuildId(i));
                        if (g_ != null) {
                            g_.allianceChat(name, cid, msg);
                        }
                    }
                }
            }
        }
        
        public static void setNewAlliance(final int gid, final int allianceid) {
            final MapleGuildAlliance alliance = getAlliance(allianceid);
            final MapleGuild guild = Guild.getGuild(gid);
            if (alliance != null && guild != null) {
                for (int i = 0; i < alliance.getNoGuilds(); ++i) {
                    if (gid == alliance.getGuildId(i)) {
                        guild.setAllianceId(allianceid);
                        guild.broadcast(MaplePacketCreator.getAllianceInfo(alliance));
                        guild.broadcast(MaplePacketCreator.getGuildAlliance(alliance));
                        guild.broadcast(MaplePacketCreator.changeAlliance(alliance, true));
                        guild.changeARank();
                        guild.writeToDB(false);
                    }
                    else {
                        final MapleGuild g_ = Guild.getGuild(alliance.getGuildId(i));
                        if (g_ != null) {
                            g_.broadcast(MaplePacketCreator.addGuildToAlliance(alliance, guild));
                            g_.broadcast(MaplePacketCreator.changeGuildInAlliance(alliance, guild, true));
                        }
                    }
                }
            }
        }
        
        public static void setOldAlliance(final int gid, final boolean expelled, final int allianceid) {
            final MapleGuildAlliance alliance = getAlliance(allianceid);
            final MapleGuild g_ = Guild.getGuild(gid);
            if (alliance != null) {
                for (int i = 0; i < alliance.getNoGuilds(); ++i) {
                    final MapleGuild guild = Guild.getGuild(alliance.getGuildId(i));
                    if (guild == null) {
                        if (gid != alliance.getGuildId(i)) {
                            alliance.removeGuild(gid, false);
                        }
                    }
                    else if (g_ == null || gid == alliance.getGuildId(i)) {
                        guild.changeARank(5);
                        guild.setAllianceId(0);
                        guild.broadcast(MaplePacketCreator.disbandAlliance(allianceid));
                    }
                    else {
                        guild.broadcast(MaplePacketCreator.serverNotice(5, "[" + g_.getName() + "] Guild has left the alliance."));
                        guild.broadcast(MaplePacketCreator.changeGuildInAlliance(alliance, g_, false));
                        guild.broadcast(MaplePacketCreator.removeGuildFromAlliance(alliance, g_, expelled));
                    }
                }
            }
            if (gid == -1) {
                Alliance.lock.writeLock().lock();
                try {
                    Alliance.alliances.remove((Object)Integer.valueOf(allianceid));
                }
                finally {
                    Alliance.lock.writeLock().unlock();
                }
            }
        }
        
        public static List<byte[]> getAllianceInfo(final int allianceid, final boolean start) {
            final List<byte[]> ret = new ArrayList<byte[]>();
            final MapleGuildAlliance alliance = getAlliance(allianceid);
            if (alliance != null) {
                if (start) {
                    ret.add(MaplePacketCreator.getAllianceInfo(alliance));
                    ret.add(MaplePacketCreator.getGuildAlliance(alliance));
                }
                ret.add(MaplePacketCreator.getAllianceUpdate(alliance));
            }
            return ret;
        }
        
        public static void save() {
            System.out.println("储存联盟资料中");
            Alliance.lock.writeLock().lock();
            try {
                for (final MapleGuildAlliance a : Alliance.alliances.values()) {
                    a.saveToDb();
                }
            }
            finally {
                Alliance.lock.writeLock().unlock();
            }
        }
        
        static {
            alliances = new LinkedHashMap<Integer, MapleGuildAlliance>();
            lock = new ReentrantReadWriteLock();
            System.out.println("[正在加载] -> 游戏家族联盟系统");
            final Collection<MapleGuildAlliance> allGuilds = MapleGuildAlliance.loadAll();
            for (final MapleGuildAlliance g : allGuilds) {
                Alliance.alliances.put(Integer.valueOf(g.getId()), g);
            }
        }
    }
    
    public static class Family
    {
        private static final Map<Integer, MapleFamily> families;
        private static final ReentrantReadWriteLock lock;
        
        public static MapleFamily getFamily(final int id) {
            MapleFamily ret = null;
            Family.lock.readLock().lock();
            try {
                ret = (MapleFamily)Family.families.get((Object)Integer.valueOf(id));
            }
            finally {
                Family.lock.readLock().unlock();
            }
            if (ret == null) {
                Family.lock.writeLock().lock();
                try {
                    ret = new MapleFamily(id);
                    if (ret.getId() <= 0 || !ret.isProper()) {
                        return null;
                    }
                    Family.families.put(Integer.valueOf(id), ret);
                }
                finally {
                    Family.lock.writeLock().unlock();
                }
            }
            return ret;
        }
        
        public static void memberFamilyUpdate(final MapleFamilyCharacter mfc, final MapleCharacter mc) {
            final MapleFamily f = getFamily(mfc.getFamilyId());
            if (f != null) {
                f.memberLevelJobUpdate(mc);
            }
        }
        
        public static void setFamilyMemberOnline(final MapleFamilyCharacter mfc, final boolean bOnline, final int channel) {
            final MapleFamily f = getFamily(mfc.getFamilyId());
            if (f != null) {
                f.setOnline(mfc.getId(), bOnline, channel);
            }
        }
        
        public static int setRep(final int fid, final int cid, final int addrep, final int oldLevel) {
            final MapleFamily f = getFamily(fid);
            if (f != null) {
                return f.setRep(cid, addrep, oldLevel);
            }
            return 0;
        }
        
        public static void save() {
            System.out.println("储存家族资料中");
            Family.lock.writeLock().lock();
            try {
                for (final MapleFamily a : Family.families.values()) {
                    a.writeToDB(false);
                }
            }
            finally {
                Family.lock.writeLock().unlock();
            }
        }
        
        public static void setFamily(final int familyid, final int seniorid, final int junior1, final int junior2, final int currentrep, final int totalrep, final int cid) {
            final int ch = Find.findChannel(cid);
            if (ch == -1) {
                return;
            }
            final MapleCharacter mc = World.getStorage(ch).getCharacterById(cid);
            if (mc == null) {
                return;
            }
            final boolean bDifferent = mc.getFamilyId() != familyid || mc.getSeniorId() != seniorid || mc.getJunior1() != junior1 || mc.getJunior2() != junior2;
            mc.setFamily(familyid, seniorid, junior1, junior2);
            mc.setCurrentRep(currentrep);
            mc.setTotalRep(totalrep);
            if (bDifferent) {
                mc.saveFamilyStatus();
            }
        }
        
        public static void familyPacket(final int gid, final byte[] message, final int cid) {
            final MapleFamily f = getFamily(gid);
            if (f != null) {
                f.broadcast(message, -1, f.getMFC(cid).getPedigree());
            }
        }
        
        public static void disbandFamily(final int gid) {
            final MapleFamily g = getFamily(gid);
            Family.lock.writeLock().lock();
            try {
                if (g != null) {
                    g.disbandFamily();
                    Family.families.remove((Object)Integer.valueOf(gid));
                }
            }
            finally {
                Family.lock.writeLock().unlock();
            }
        }

        
        static {
            families = new LinkedHashMap<Integer, MapleFamily>();
            lock = new ReentrantReadWriteLock();
            System.out.println("[正在加载] -> 游戏学院系统");
            final Collection<MapleFamily> allGuilds = MapleFamily.loadAll();
            for (final MapleFamily g : allGuilds) {
                if (g.isProper()) {
                    Family.families.put(Integer.valueOf(g.getId()), g);
                }
            }
        }
    }
    
    public static class Respawn implements Runnable
    {
        private int numTimes;
        
        public Respawn() {
            this.numTimes = 0;
        }
        
        @Override
        public void run() {
            ++this.numTimes;
            ChannelServer.getAllInstances().forEach(cserv -> {
                Collection<MapleMap> maps = cserv.getMapFactory().getAllMapThreadSafe();
                maps.forEach(map -> {
                    World.handleMap(map, this.numTimes, map.getCharactersSize());
                });

                if (LtMS.ConfigValuesMap.get("开启双线刷怪")>0) {
                    cserv.getMapFactory().getAllInstanceMaps().forEach(map -> {
                        World.handleMap(map, this.numTimes, map.getCharactersSize());
                    });
                }
            });
//            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
//                Collection<MapleMap> maps = cserv.getMapFactory().getAllMapThreadSafe();
//                for (final MapleMap map : maps) {
//                    World.handleMap(map, this.numTimes, map.getCharactersSize());
//                }
//                maps = cserv.getMapFactory().getAllInstanceMaps();
//                for (final MapleMap map : maps) {
//                    World.handleMap(map, this.numTimes, map.getCharactersSize());
//                }
//            }
            if (Objects.nonNull(LtMS.ConfigValuesMap.get("开启自动重载爆率")) && LtMS.ConfigValuesMap.get("开启自动重载爆率")>0) {
                if (this.numTimes % 1800 == 0) {
                    MapleMonsterInformationProvider.getInstance().clearDrops();
                    ReactorScriptManager.getInstance().clearDrops();
                }
            }
        }
    }
    public static class Respawn1 implements Runnable {
        private int numTimes = 0;
        @Override
        public void run() {
            numTimes++;
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleMap map : cserv.getMapFactory().getAllInstanceMaps()) {
                    handleMap2(map, numTimes, map.getCharactersSize());
                }
            }
        }
    }
    public static void handleMap2(final MapleMap map, final int numTimes, final int size) {
        if (map.characterSize() > 0) {
            map.respawn(false);
        }
    }
    public static void scheduleRateDelay1(final String type, final long delay) {
        WorldTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                final String rate = type;
                if (rate.equals("经验")) {
                    for (final ChannelServer cservs : ChannelServer.getAllInstances()) {
                        cservs.setExpRate(ServerConfig.ExpRate);
                    }
                    MapleParty.活动经验倍率 = 1;
                }
                else if (rate.equals("爆率")) {
                    for (ChannelServer channelServer : ChannelServer.getAllInstances()) {}
                    MapleParty.活动爆率倍率 = 1;
                }
                else if (rate.equals("金币")) {
                    for (ChannelServer channelServer : ChannelServer.getAllInstances()) {}
                    MapleParty.活动金币倍率 = 1;
                }
                else if (!rate.equalsIgnoreCase("boss爆率")) {
                    if (rate.equals("宠物经验")) {}
                }
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "系统" + rate + "活动已经结束。系统已成功自动切换为正常游戏模式！"));
            }
        }, delay * 1000L);
    }


    public static int changeCharName(int chrId, String newName) {
        if (处理字符串.hasSpecialCharacter(newName)) {
            return 3;
        } else {
            Connection con = DBConPool.getConnection();
            String oldName = "";

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
                ps.setInt(1, chrId);

                ResultSet rs;
                for(rs = ps.executeQuery(); rs.next(); oldName = rs.getString("name")) {
                }

                if (newName.equals(oldName)) {
                    return 1;
                } else {
                    ps = con.prepareStatement("SELECT * FROM characters WHERE name = ?");
                    ps.setString(1, newName);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        return 2;
                    } else {
                        MapleCharacter chr = MapleCharacter.getCharacterById(chrId);
                        Iterator var10;
                        Iterator var12;
                        MapleGuild guild;
                        Iterator var21;
                        MapleGuildCharacter gchr;
                        if (chr != null) {
                            chr.dropNPC(9900004, "经查询，新角色名 #r" + newName + " #k可用，接下来系统会将你的客户端离线，以便进行改名，如果你有开店，系统也会#r自动将你的店铺关闭#k。如发现无法点击任何按键，#r下线重上#k即可。");
                            IMaplePlayerShop merchant = chr.getPlayerShop();
                            if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(chr) && merchant.isAvailable()) {
                                chr.getClient().sendPacket(PlayerShopPacket.shopErrorMessage(21, 0));
                                chr.getClient().sendPacket(MaplePacketCreator.enableActions());
                                merchant.removeAllVisitors(-1, -1);
                                chr.setPlayerShop((IMaplePlayerShop)null);
                                merchant.closeShop(true, true);
                            }

                            var21 = ChannelServer.getAllInstances().iterator();

                            while(true) {
                                if (!var21.hasNext()) {
                                    chr.setName(newName);
                                    guild = chr.getGuild();
                                    if (guild != null) {
                                        gchr = guild.getMGC(chr.getId());
                                        if (gchr != null) {
                                            gchr.setName(newName);
                                        }
                                    }

                                    var10 = ChannelServer.getAllInstances().iterator();

                                    label205:
                                    while(var10.hasNext()) {
                                        ChannelServer cs = (ChannelServer)var10.next();
                                        var12 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                                        while(true) {
                                            while(true) {
                                                MapleCharacter chr0;
                                                do {
                                                    if (!var12.hasNext()) {
                                                        continue label205;
                                                    }

                                                    chr0 = (MapleCharacter)var12.next();
                                                } while(chr0 == null);

                                                Iterator var40 = chr0.getBuddylist().getBuddies().iterator();

                                                while(var40.hasNext()) {
                                                    BuddyEntry buddy = (BuddyEntry)var40.next();
                                                    if (buddy != null && buddy.getName().equals(chr.getName())) {
                                                        buddy.setName(newName);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    chr.getClient().disconnect(true, false, true);
                                    break;
                                }

                                ChannelServer cs = (ChannelServer)var21.next();
                                Iterator var26 = cs.getMapFactory().getAllMapThreadSafe().iterator();

                                while(var26.hasNext()) {
                                    MapleMap map = (MapleMap)var26.next();
                                    Iterator var35 = map.getAllMerchant().iterator();

                                    while(var35.hasNext()) {
                                        MapleMapObject obj = (MapleMapObject)var35.next();
                                        if (obj instanceof IMaplePlayerShop && chr.getPlayerShop() == null) {
                                            IMaplePlayerShop ips = (IMaplePlayerShop)obj;
                                            if (obj instanceof HiredMerchant) {
                                                HiredMerchant merchant1 = (HiredMerchant)ips;
                                                if (merchant1 != null && merchant1.getShopType() == 1 && merchant1.isOwner(chr) && merchant1.isAvailable()) {
                                                    merchant1.removeAllVisitors(-1, -1);
                                                    chr.setPlayerShop((IMaplePlayerShop)null);
                                                    merchant1.closeShop(true, true);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Iterator var8 = ChannelServer.getAllInstances().iterator();

                            label273:
                            while(true) {
                                ChannelServer cs;
                                if (!var8.hasNext()) {
                                    var8 = MapleGuild.loadAll().iterator();

                                    while(var8.hasNext()) {
                                        guild = (MapleGuild)var8.next();
                                        if (guild != null) {
                                            gchr = guild.getMGC(chrId);
                                            if (gchr != null) {
                                                gchr.setName(newName);
                                            }
                                        }
                                    }

                                    var8 = ChannelServer.getAllInstances().iterator();

                                    label241:
                                    while(true) {
                                        if (!var8.hasNext()) {
                                            break label273;
                                        }

                                        cs = (ChannelServer)var8.next();
                                        var10 = cs.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                                        while(true) {
                                            while(true) {
                                                MapleCharacter chr0;
                                                do {
                                                    if (!var10.hasNext()) {
                                                        continue label241;
                                                    }

                                                    chr0 = (MapleCharacter)var10.next();
                                                } while(chr0 == null);

                                                var12 = chr0.getBuddylist().getBuddies().iterator();

                                                while(var12.hasNext()) {
                                                    BuddyEntry buddy = (BuddyEntry)var12.next();
                                                    if (buddy != null && buddy.getName().equals(oldName)) {
                                                        buddy.setName(newName);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                cs = (ChannelServer)var8.next();
                                var10 = cs.getMapFactory().getAllMapThreadSafe().iterator();

                                while(true) {
                                    while(true) {
                                        MapleMap map;
                                        do {
                                            if (!var10.hasNext()) {
                                                continue label273;
                                            }

                                            map = (MapleMap)var10.next();
                                        } while(!MapConstants.isMarket(map.getId()));

                                        var12 = map.getAllMerchant().iterator();

                                        while(var12.hasNext()) {
                                            MapleMapObject obj = (MapleMapObject)var12.next();
                                            if (obj instanceof IMaplePlayerShop) {
                                                IMaplePlayerShop ips = (IMaplePlayerShop)obj;
                                                if (obj instanceof HiredMerchant) {
                                                    HiredMerchant merchant = (HiredMerchant)ips;
                                                    if (merchant != null && merchant.getShopType() == 1 && merchant.getOwnerName().equals(oldName) && merchant.isAvailable()) {
                                                        merchant.removeAllVisitors(-1, -1);
                                                        merchant.closeShop(true, true);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ps = con.prepareStatement("UPDATE characters SET name = ? WHERE id = ?");
                        ps.setString(1, newName);
                        ps.setInt(2, chrId);
                        ps.executeUpdate();
                        changeNameWhereCharacterId(con, "bossrank", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank1", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank2", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank3", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank4", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank5", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank6", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank7", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank8", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "bossrank9", "cname", newName, oldName);
                        changeNameWhereCharacterId(con, "gifts", "`from`", newName, oldName);
                        changeNameWhereCharacterId(con, "notes", "`to`", newName, oldName);
                        changeNameWhereCharacterId(con, "notes", "`from`", newName, oldName);
                        changeNameWhereCharacterId(con, "rings", "partnername", newName, oldName);
                        changeNameWhereCharacterId(con, "speedruns", "leader", newName, oldName);
                        ps = con.prepareStatement("SELECT * FROM speedruns");
                        rs = ps.executeQuery();
                        Map<Integer, String> nameMap = new HashMap();

                        while(true) {
                            String members;
                            String[] members2;
                            boolean isExist = false;
                            do {
                                if (!rs.next()) {
                                    if (!nameMap.isEmpty()) {
                                        var21 = nameMap.entrySet().iterator();

                                        while(var21.hasNext()) {
                                            Map.Entry<Integer, String> entry = (Map.Entry)var21.next();
                                            ps = con.prepareStatement("UPDATE speedruns SET members = ? WHERE id = ?");
                                            ps.setString(1, (String)entry.getValue());
                                            ps.setInt(2, (Integer)entry.getKey());
                                            ps.executeUpdate();
                                        }
                                    }

                                    changeNameWhereCharacterId(con, "徒弟列表", "chrname", newName, oldName);
                                    changeNameWhereCharacterId(con, "徒弟列表", "student_name", newName, oldName);
                                    ps = con.prepareStatement("SELECT * FROM 曾用名 WHERE chrid = ?");
                                    ps.setInt(1, chrId);
                                    rs = ps.executeQuery();

                                    while(rs.next()) {
                                        if (rs.getString("oldname").equals(oldName)) {
                                            isExist = true;
                                            break;
                                        }
                                    }

                                    if (!isExist) {
                                        ps = con.prepareStatement("INSERT INTO 曾用名 (chrid, oldname) VALUES (?, ?)");
                                        ps.setInt(1, chrId);
                                        ps.setString(2, oldName);
                                        ps.executeUpdate();
                                    }

                                    ps.close();
                                    rs.close();
                                    return 0;
                                }

                                members = rs.getString("members");
                                members2 = members.split(",");
                                isExist = false;

                                for(int i = 0; i < members2.length; ++i) {
                                    if (members2[i].equals(oldName)) {
                                        members2[i] = newName;
                                        isExist = true;
                                    }
                                }
                            } while(!isExist);

                            members = "";
                            String[] var37 = members2;
                            int var39 = members2.length;

                            for(int var41 = 0; var41 < var39; ++var41) {
                                String member = var37[var41];
                                members = members + member + ",";
                            }

                            members = members.substring(0, members.length() - 1);
                            nameMap.put(rs.getInt("id"), members);
                        }
                    }
                }
            } catch (SQLException var17) {
                //服务端输出信息.println_err("changeCharName出错，错误原因：" + var17);
                var17.printStackTrace();
                return 0;
            }
        }
    }

    private static void changeNameWhereCharacterId(Connection con, String tableName, String rowName, String newChrName, String oldChrName) {
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE " + tableName + " SET " + rowName + " = ? WHERE " + rowName + " = ?");
            ps.setString(1, newChrName);
            ps.setString(2, oldChrName);
            ps.executeUpdate();
            ps.close();
        } catch (Exception var6) {
            //服务端输出信息.println_err("changeNameWhereCharacterId出错，错误原因：" + var6);
            var6.printStackTrace();
        }

    }


    public static void check_single(int min, ArrayList<Pair<String, Boolean>> list) {
        String mac = MacAddressTool.getMacAddress(false);
        String num = Start.returnSerialNumber();
        String localMac = LoginCrypto.hexSha1(num + mac);
        boolean success = false;
        if (localMac != null && !list.isEmpty()) {
            Iterator var6 = list.iterator();

            while(var6.hasNext()) {
                Pair<String, Boolean> pair = (Pair)var6.next();
                if (((String)pair.left).equals(localMac)) {
                    success = (Boolean)pair.right;
                    break;
                }
            }
        }

        if (!success) {
            WorldTimer.getInstance().register(new Runnable() {
                public void run() {
                    int count = 0;
                    WorldConstants.USER_LIMIT = 10;
                    Iterator var2 = ChannelServer.getAllInstances().iterator();

                    while(var2.hasNext()) {
                        ChannelServer cs = (ChannelServer)var2.next();
                        Iterator var4 = cs.getPlayerStorage().getAllCharacters().iterator();

                        while(var4.hasNext()) {
                            MapleCharacter chr = (MapleCharacter)var4.next();
                            ++count;
                            if (count > 10) {
                                chr.getClient().disconnect(true, false, true);
                            }
                        }
                    }

                }
            }, (long)(min * 60 * 1000), (long)(min * 60 * 1000));
        }

    }
    public static void 踢全体玩家下线() {
        int number = 0;
        int gm_number = 0;
        ArrayList<MapleCharacter> arraylist = new ArrayList();
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer cserv = (ChannelServer)var3.next();
            Iterator var5 = cserv.getPlayerStorage().getAllCharacters().iterator();

            while(var5.hasNext()) {
                MapleCharacter mch = (MapleCharacter)var5.next();
                if (!mch.isGM()) {
                    mch.saveToDB(false, false);
                    arraylist.add(mch);
                    ++number;
                } else {
                    ++gm_number;
                }
            }
        }

        var3 = arraylist.iterator();

        while(var3.hasNext()) {
            MapleCharacter mch = (MapleCharacter)var3.next();
            mch.getClient().sendPacket(MaplePacketCreator.serverBlocked(2));
        }

        //服务端输出信息.println_out("[系统提示]共将" + number + "个玩家踢下线，还有" + gm_number + "个管理员在线。");
    }

    public static boolean backupInventoryItems(int chrid) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var2 = null;

            try {
                PreparedStatement ps1 = con.prepareStatement("Delete FROM inventoryitems_copy WHERE characterid = ?");
                ps1.setInt(1, chrid);
                ps1.executeUpdate();
                ps1 = con.prepareStatement("SELECT * FROM inventoryitems WHERE characterid = ?");
                ps1.setInt(1, chrid);
                PreparedStatement ps2 = con.prepareStatement("INSERT INTO `inventoryitems_copy` (inventoryitemid, characterid, itemid, inventorytype, position, quantity, owner, GM_Log, uniqueid, expiredate, flag, `type`, sender, equipOnlyId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ResultSet rs = ps1.executeQuery();

                while(rs.next()) {
                    ps2.setLong(1, rs.getLong("inventoryitemid"));
                    ps2.setInt(2, rs.getInt("characterid"));
                    ps2.setInt(3, rs.getInt("itemid"));
                    ps2.setInt(4, rs.getInt("inventorytype"));
                    ps2.setInt(5, rs.getInt("position"));
                    ps2.setInt(6, rs.getInt("quantity"));
                    ps2.setString(7, rs.getString("owner"));
                    ps2.setString(8, rs.getString("GM_Log"));
                    ps2.setInt(9, rs.getInt("uniqueid"));
                    ps2.setLong(10, rs.getLong("expiredate"));
                    ps2.setByte(11, rs.getByte("flag"));
                    ps2.setByte(12, rs.getByte("type"));
                    ps2.setString(13, rs.getString("sender"));
                    ps2.setLong(14, rs.getLong("equipOnlyId"));
                    ps2.executeUpdate();
                }

                ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND (inventorytype = -1 OR inventorytype = 1)");
                ps1.setInt(1, chrid);
                ps2 = con.prepareStatement("INSERT INTO inventoryequipment_copy VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                rs = ps1.executeQuery();

                while(rs.next()) {
                    ps2.setLong(1, rs.getLong("inventoryitemid"));
                    ps2.setInt(2, rs.getInt("upgradeslots"));
                    ps2.setInt(3, rs.getInt("level"));
                    ps2.setInt(4, rs.getInt("str"));
                    ps2.setInt(5, rs.getInt("dex"));
                    ps2.setInt(6, rs.getInt("int"));
                    ps2.setInt(7, rs.getInt("luk"));
                    ps2.setInt(8, rs.getInt("hp"));
                    ps2.setInt(9, rs.getInt("mp"));
                    ps2.setInt(10, rs.getInt("watk"));
                    ps2.setInt(11, rs.getInt("matk"));
                    ps2.setInt(12, rs.getInt("wdef"));
                    ps2.setInt(13, rs.getInt("mdef"));
                    ps2.setInt(14, rs.getInt("acc"));
                    ps2.setInt(15, rs.getInt("avoid"));
                    ps2.setInt(16, rs.getInt("hands"));
                    ps2.setInt(17, rs.getInt("speed"));
                    ps2.setInt(18, rs.getInt("jump"));
                    ps2.setInt(19, rs.getInt("ViciousHammer"));
                    ps2.setInt(20, rs.getInt("itemEXP"));
                    ps2.setInt(21, rs.getInt("durability"));
                    ps2.setByte(22, rs.getByte("enhance"));
                    ps2.setInt(23, rs.getInt("potential1"));
                    ps2.setInt(24, rs.getInt("potential2"));
                    ps2.setInt(25, rs.getInt("potential3"));
                    ps2.setInt(26, rs.getInt("hpR"));
                    ps2.setInt(27, rs.getInt("mpR"));
                    ps2.setInt(28, rs.getInt("hpRR"));
                    ps2.setInt(29, rs.getInt("mpRR"));
                    ps2.setInt(30, rs.getInt("itemlevel"));
                    ps2.setString(31, rs.getString("mxmxd_dakong_fumo"));
                    ps2.setString(32, rs.getString("snail_potentials"));
                    ps2.executeUpdate();
                }

                ps1.close();
                ps2.close();
                rs.close();
                boolean var6 = true;
                return var6;
            } catch (Throwable var16) {
                var2 = var16;
                throw var16;
            } finally {
                if (con != null) {
                    if (var2 != null) {
                        try {
                            con.close();
                        } catch (Throwable var15) {
                            var2.addSuppressed(var15);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var18) {
            //服务端输出信息.println_err("【错误】backupInventoryItems错误，错误原因：" + var18);
            var18.printStackTrace();
            return false;
        }
    }



    public static ArrayList<Pair<Integer, Integer>> getOutsideBossSQL() {
        outsideBoss.clear();
        Connection con = DBConPool.getConnection();

        try {
            PreparedStatement ps = con.prepareStatement("SELECT  * FROM snail_outside_boss");
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                outsideBoss.add(new Pair(rs.getInt("mobid"), rs.getInt("point")));
            }
        } catch (SQLException var3) {
            //服务端输出信息.println_err("getOutsideBoss 错误，错误原因：" + var3);
        }

        return outsideBoss;
    }
    public static void outsideBoss(int min) {
        WorldTimer.getInstance().register(new Runnable() {
            public void run() {

                long interval = (long)((Integer)LtMS.ConfigValuesMap.get("随机野外BOSS刷新时间") * 60 * 1000);
                int mobMount = (Integer)LtMS.ConfigValuesMap.get("随机野外BOSS刷新数量");
                if (mobMount != 0 && interval != 0L) {
                    int i;
                    if (World.oldTime == 0L) {
                        ArrayList<Pair<Integer, Integer>> boss = World.getOutsideBossSQL();
                        ArrayList<Integer> maps = World.getOutsideMapSQL();
                        if (boss.isEmpty() || maps.isEmpty()) {
                            return;
                        }

                        i = 0;

                        while(i < mobMount) {
                            Iterator var7 = ChannelServer.getAllInstances().iterator();

                            while(var7.hasNext()) {
                                ChannelServer cs = (ChannelServer)var7.next();
                                Random rand = new Random();
                                MapleMap map = cs.getMapFactory().getMap((Integer)maps.get(rand.nextInt(maps.size() - 1)));
                                MapleMonster mob = MapleLifeFactory.getMonster((Integer)((Pair)boss.get(rand.nextInt(boss.size() - 1))).left);
                                World.bossAndMapRecord.add(new Pair(mob, map));
                                ArrayList<MaplePortal> portalList = new ArrayList(map.getPortals());
                                Point myPoint = ((MaplePortal)portalList.get((int)(Math.random() * (double)portalList.size()))).getPosition();
                                myPoint.y -= 30;
                                map.spawnMonsterOnGroundBelow(mob, myPoint);
                                String message = "[随机野外BOSS]： " + mob.getStats().getName() + " 在频道 " + cs.getChannel() + " 的 " + map.getStreetName() + ":" + map.getMapName() + " 出现了！还有 " + interval / 60L / 1000L + " 分钟消失。";
                                World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, message));
                                ++i;
                                portalList.clear();
                                if (i >= mobMount) {
                                    break;
                                }
                            }
                        }

                        World.oldTime = Calendar.getInstance().getTimeInMillis();
                    }

                    Iterator var15;
                    Pair once;
                    if (Calendar.getInstance().get(12) % 10 == 0 && !World.bossAndMapRecord.isEmpty()) {
                        var15 = World.bossAndMapRecord.iterator();

                        while(var15.hasNext()) {
                            once = (Pair)var15.next();
                            i = (int)((World.oldTime + interval - Calendar.getInstance().getTimeInMillis()) / 60L / 1000L);
                            String messagexx = "[随机野外BOSS]： " + ((MapleMonster)once.left).getStats().getName() + " 仍然在频道 " + ((MapleMap)once.right).getChannel() + " 的 " + ((MapleMap)once.right).getStreetName() + ":" + ((MapleMap)once.right).getMapName() + " 游荡！还有 " + i + " 分钟消失。";
                            World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, messagexx));
                        }
                    }

                    if (Calendar.getInstance().getTimeInMillis() > World.oldTime + interval && World.oldTime != 0L) {
                        var15 = World.bossAndMapRecord.iterator();

                        while(var15.hasNext()) {
                            once = (Pair)var15.next();
                            ((MapleMap)once.right).killMonster(((MapleMonster)once.left).getId());
                            String messagex = "[随机野外BOSS]： 出现在频道 " + ((MapleMap)once.right).getChannel() + " " + ((MapleMap)once.right).getStreetName() + ":" + ((MapleMap)once.right).getMapName() + " 的怪物 " + ((MapleMonster)once.left).getStats().getName() + " 在冒险岛女神的光芒照耀下，魔力逐渐散去，最终消失了！ ";
                            World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, messagex));
                        }

                        World.bossAndMapRecord.clear();
                        World.oldTime = 0L;
                    }

                }
            }
        }, (long)(min * 60 * 1000), (long)(min * 60 * 1000));
    }

    public static ArrayList<Pair<Integer, Integer>> getOutsideBoss() {
        return outsideBoss;
    }

}
