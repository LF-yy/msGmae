package tools.packet;

import constants.ServerConfig;
import handling.SendPacketOpcode;
import server.shops.AbstractPlayerStore;
import server.shops.IMaplePlayerShop;
import server.movement.LifeMovementFragment;
import client.inventory.MaplePet;
import client.inventory.Equip;
import constants.GameConstants;
import server.MapleItemInformationProvider;
import java.util.LinkedHashMap;
import java.util.Collection;
import client.inventory.MapleInventory;
import java.util.Collections;
import client.inventory.IItem;
import client.inventory.Item;
import java.util.ArrayList;
import client.inventory.MapleInventoryType;
import tools.Pair;
import client.inventory.MapleRing;
import client.MapleCoolDownValueHolder;
import java.util.Map;
import client.SkillEntry;
import client.ISkill;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.List;
import tools.KoreanDateUtil;
import client.MapleQuestStatus;
import client.MapleCharacter;
import tools.data.MaplePacketLittleEndianWriter;
import java.util.Date;
import java.util.TimeZone;

public class PacketHelper
{
    private static final long FT_UT_OFFSET = 116444592000000000L;
    public static long MAX_TIME = 150842304000000000L;
    public static byte[] unk1;
    public static byte[] unk2;
    
    public static long getKoreanTimestamp(final long realTimestamp) {
        if (realTimestamp == -1L) {
            return 150842304000000000L;
        }
        final long time = realTimestamp / 1000L / 60L;
        return time * 600000000L + 116444592000000000L;
    }
    
    public static long getTime(final long realTimestamp) {
        if (realTimestamp == -1L) {
            return 150842304000000000L;
        }
        final long time = realTimestamp / 1000L;
        return time * 10000000L + 116444592000000000L;
    }
    
    public static long getFileTimestamp(long timeStampinMillis, final boolean roundToMinutes) {
        if (TimeZone.getDefault().inDaylightTime(new Date())) {
            timeStampinMillis -= 3600000L;
        }
        long time;
        if (roundToMinutes) {
            time = timeStampinMillis / 1000L / 60L * 600000000L;
        }
        else {
            time = timeStampinMillis * 10000L;
        }
        return time + 116444592000000000L;
    }
    
    public static void addImageInfo(final MaplePacketLittleEndianWriter mplew, final byte[] image) {
        mplew.writeInt(image.length);
        mplew.write(image);
    }
    
    public static void addQuestInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<MapleQuestStatus> started = chr.getStartedQuests();
        mplew.writeShort(started.size());
        for (final MapleQuestStatus q : started) {
            mplew.writeShort(q.getQuest().getId());
            mplew.writeMapleAsciiString((q.getCustomData() != null) ? q.getCustomData() : "");
        }
        final List<MapleQuestStatus> completed = chr.getCompletedQuests();
        mplew.writeShort(completed.size());
        for (final MapleQuestStatus q2 : completed) {
            mplew.writeShort(q2.getQuest().getId());
            final int time = KoreanDateUtil.getQuestTimestamp(q2.getCompletionTime());
            mplew.writeInt(time);
            mplew.writeInt(time);
        }
    }
    
    public static void addSkillInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final Map<ISkill, SkillEntry> skills = chr.getSkills();
        mplew.writeShort(skills.size());
        for (final Entry<ISkill, SkillEntry> skill : skills.entrySet()) {
            mplew.writeInt(((ISkill)skill.getKey()).getId());
            mplew.writeInt((int)((SkillEntry)skill.getValue()).skillevel);
            if (((ISkill)skill.getKey()).isFourthJob()) {
                mplew.writeInt((int)((SkillEntry)skill.getValue()).masterlevel);
            }
        }
    }
    
    public static void addCoolDownInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final List<MapleCoolDownValueHolder> cd = chr.getCooldowns();
        mplew.writeShort(cd.size());
        for (final MapleCoolDownValueHolder cooling : cd) {
            mplew.writeInt(cooling.skillId);
            mplew.writeShort((int)(cooling.length + cooling.startTime - System.currentTimeMillis()) / 1000);
        }
    }
    
    public static void addRocksInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        final int[] mapz = chr.getRegRocks();
        for (int i = 0; i < 5; ++i) {
            mplew.writeInt(mapz[i]);
        }
        final int[] map = chr.getRocks();
        for (int j = 0; j < 10; ++j) {
            mplew.writeInt(map[j]);
        }
    }
    
    public static void addMonsterBookInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMonsterBookCover());
        mplew.write(0);
        chr.getMonsterBook().addCardPacket(mplew);
    }
    
    public static void addRingInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeShort(0);
        final Pair<List<MapleRing>, List<MapleRing>> aRing = chr.getRings(true);
        final List<MapleRing> cRing = (List<MapleRing>)aRing.getLeft();
        mplew.writeShort(cRing.size());
        for (final MapleRing ring : cRing) {
            mplew.writeInt(ring.getPartnerChrId());
            mplew.writeAsciiString(ring.getPartnerName(), 13);
            mplew.writeLong((long)ring.getRingId());
            mplew.writeLong((long)ring.getPartnerRingId());
        }
        final List<MapleRing> fRing = (List<MapleRing>)aRing.getRight();
        mplew.writeShort(fRing.size());
        for (final MapleRing ring2 : fRing) {
            mplew.writeInt(ring2.getPartnerChrId());
            mplew.writeAsciiString(ring2.getPartnerName(), 13);
            mplew.writeLong((long)ring2.getRingId());
            mplew.writeLong((long)ring2.getPartnerRingId());
            mplew.writeInt(ring2.getItemId());
        }
        mplew.writeShort((int)(short)(short)((chr.getMarriageRing(false) != null) ? 1 : 0));
        final int marriageId = 30000;
        if (chr.getMarriageRing(false) != null) {
            mplew.writeInt(0);
            mplew.writeAsciiString("", 13);
            mplew.writeInt(chr.getId());
            mplew.writeInt(chr.getMarriageRing(false).getPartnerRingId());
        }
    }
    
    public static void addInventoryInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getMeso());
        mplew.writeInt(chr.getId());
        mplew.writeInt(chr.getBeans());
        mplew.writeInt(0);
        mplew.write(chr.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.USE).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.SETUP).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.ETC).getSlotLimit());
        mplew.write(chr.getInventory(MapleInventoryType.CASH).getSlotLimit());
        mplew.writeLong(getTime(-2L));
        MapleInventory iv = chr.getInventory(MapleInventoryType.EQUIPPED);
        final Collection<IItem> equippedC = iv.list();
        final List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (final IItem item : equippedC) {
            equipped.add((Item)item);
        }
        Collections.sort(equipped);
        for (final Item item2 : equipped) {
            if (item2.getPosition() < 0 && item2.getPosition() > -100) {
                addItemInfo(mplew, (IItem)item2, false, false);
            }
        }
        mplew.write(0);
        for (final Item item2 : equipped) {
            if (item2.getPosition() <= -100 && item2.getPosition() > -1000) {
                addItemInfo(mplew, (IItem)item2, false, false);
            }
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.EQUIP);
        for (final IItem item : iv.list()) {
            addItemInfo(mplew, item, false, false);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.USE);
        for (final IItem item : iv.list()) {
            addItemInfo(mplew, item, false, false);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.SETUP);
        for (final IItem item : iv.list()) {
            addItemInfo(mplew, item, false, false);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.ETC);
        for (final IItem item : iv.list()) {
            addItemInfo(mplew, item, false, false);
        }
        mplew.write(0);
        iv = chr.getInventory(MapleInventoryType.CASH);
        for (final IItem item : iv.list()) {
            addItemInfo(mplew, item, false, false);
        }
        mplew.write(0);
    }
    
    public static void addCharStats(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        mplew.writeInt(chr.getId());
        mplew.writeAsciiString(chr.getName(), 13);
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.writeInt(chr.getHair());
        mplew.writeZeroBytes(24);
        mplew.write((int)chr.getLevel());
        mplew.writeShort((int)chr.getJob());
        chr.getStat().connectData(mplew);
        mplew.writeShort((int)chr.getRemainingAp());
        mplew.writeShort(chr.getRemainingSp());
        mplew.writeInt(chr.getExp());
        mplew.writeShort((int)chr.getFame());
        mplew.writeInt(0);
        mplew.writeLong(getTime(System.currentTimeMillis()));
        mplew.writeInt(chr.getMapId());
        mplew.write(chr.getInitialSpawnpoint());
    }
    /**
     * 向数据包中添加角色外观信息
     *
     * @param mplew 数据包写入器，用于将角色外观信息写入数据包
     * @param chr 角色对象，包含角色的外观和装备信息
     * @param mega 控制是否使用mega模式的标志，影响头发信息的写入方式
     */
    public static final void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair());
        // 创建装备和遮罩装备的映射，用于存储装备位置和对应的物品ID
        Map<Byte, Integer> myEquip = new LinkedHashMap();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap();
        // 获取角色装备的库存
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        Iterator var6 = equip.list().iterator();

        while(true) {
            while(true) {
                IItem item;
                do {
                    if (!var6.hasNext()) {
                        var6 = myEquip.entrySet().iterator();

                        Map.Entry entry;
                        while(var6.hasNext()) {
                            entry = (Map.Entry)var6.next();
                            mplew.write((Byte)entry.getKey());
                            mplew.writeInt((Integer)entry.getValue());
                        }

                        mplew.write(255);
                        var6 = maskedEquip.entrySet().iterator();

                        while(var6.hasNext()) {
                            entry = (Map.Entry)var6.next();
                            mplew.write((Byte)entry.getKey());
                            mplew.writeInt((Integer)entry.getValue());
                        }

                        mplew.write(255);
                        IItem cWeapon = equip.getItem((short)-111);
                        mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
                        // 写入默认的武器外观参数
                            mplew.writeInt(0);
                            mplew.writeLong(0L);
                        return;
                    }

                    item = (IItem)var6.next();
                } while(item.getPosition() < -128);

                byte pos = (byte)(item.getPosition() * -1);
                // 根据装备位置和ID更新myEquip和maskedEquip映射
                if (pos < 100 && myEquip.get(pos) == null) {
                    myEquip.put(pos, item.getItemId());
                } else if ((pos > 100 || pos == -128) && pos != 111) {
                    pos = (byte)(pos == -128 ? 28 : pos - 100);
                    if (myEquip.get(pos) != null) {
                        maskedEquip.put(pos, myEquip.get(pos));
                    }

                    myEquip.put(pos, item.getItemId());
                } else if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, item.getItemId());
                }
            }
        }
    }
    public static  void addCharLook(MaplePacketLittleEndianWriter mplew, MapleCharacter chr, boolean mega, boolean showEquip) {
        mplew.write(chr.getGender());
        mplew.write(chr.getSkinColor());
        mplew.writeInt(chr.getFace());
        mplew.write(mega ? 0 : 1);
        mplew.writeInt(chr.getHair());
        Map<Byte, Integer> myEquip = new LinkedHashMap();
        Map<Byte, Integer> maskedEquip = new LinkedHashMap();
        MapleInventory equip = chr.getInventory(MapleInventoryType.EQUIPPED);
        Iterator var7 = equip.list().iterator();

        while(true) {
            while(true) {
                IItem item;
                do {
                    if (!var7.hasNext()) {
                        var7 = myEquip.entrySet().iterator();

                        Map.Entry entry;
                        while(var7.hasNext()) {
                            entry = (Map.Entry)var7.next();
                            if (showEquip) {
                                mplew.write((Byte)entry.getKey());
                                mplew.writeInt((Integer)entry.getValue());
                            } else if ((Byte)entry.getKey() == 11) {
                                mplew.write((Byte)entry.getKey());
                                mplew.writeInt((Integer)entry.getValue());
                            }
                        }

                        mplew.write(255);
                        var7 = maskedEquip.entrySet().iterator();

                        while(var7.hasNext()) {
                            entry = (Map.Entry)var7.next();
                            mplew.write((Byte)entry.getKey());
                            mplew.writeInt((Integer)entry.getValue());
                        }

                        mplew.write(255);
                        if (showEquip) {
                            IItem cWeapon = equip.getItem((short)-111);
                            mplew.writeInt(cWeapon != null ? cWeapon.getItemId() : 0);
                        } else {
                            mplew.writeInt(0);
                        }


                            mplew.writeInt(0);
                            mplew.writeLong(0L);


                        return;
                    }

                    item = (IItem)var7.next();
                } while(item.getPosition() < -128);

                byte pos = (byte)(item.getPosition() * -1);
                if (pos < 100 && myEquip.get(pos) == null) {
                    myEquip.put(pos, item.getItemId());
                } else if ((pos > 100 || pos == -128) && pos != 111) {
                    pos = (byte)(pos == -128 ? 28 : pos - 100);
                    if (myEquip.get(pos) != null) {
                        maskedEquip.put(pos, myEquip.get(pos));
                    }

                    myEquip.put(pos, item.getItemId());
                } else if (myEquip.get(pos) != null) {
                    maskedEquip.put(pos, item.getItemId());
                }
            }
        }
    }
    
    public static void addExpirationTime(final MaplePacketLittleEndianWriter mplew, final long time) {
        mplew.writeLong(getTime(time));
    }
    
    public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition) {
        addItemInfo(mplew, item, zeroPosition);
    }
    public static void addItemInfo(MaplePacketLittleEndianWriter mplew, Item item) {
        addItemInfo(mplew, item, false);
    }
    /**
     * 添加物品信息到数据包
     *
     * 此方法负责将物品的详细信息写入到MaplePacketLittleEndianWriter中，包括但不限于物品的位置、类型、ID、是否是宠物或戒指等
     * 它还处理物品的唯一ID分配、宠物和装备的特殊属性、以及物品的过期时间等信息的写入
     *
     * @param mplew 用于写入物品信息的数据包写入器
     * @param item 要写入信息的物品对象
     * @param zeroPosition 是否将位置值写为0的标志，用于特定情况下的物品位置处理
     * @param leaveOut 指示是否跳过某些信息的写入，根据上下文决定是否使用
     */
    public static void addItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition, final boolean leaveOut) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final boolean isPet = item.getPet() != null && item.getPet().getUniqueId() > -1;
        boolean isRing = false;
        final boolean hasUniqueId = item.getUniqueId() > 0 && !GameConstants.isMarrigeRing(item.getItemId()) && item.getItemId() / 10000 != 166;
        Equip equip = null;
        short pos = item.getPosition();
        if (item.getType() == 1) {
            equip = (Equip)item;
            isRing = (equip.getRing() != null && equip.getRing().getRingId() > -1);
        }
        // 根据zeroPosition标志决定是否写入位置信息
        if (!zeroPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                mplew.write((pos > 100) ? (pos - 100) : pos);
            }
            else {
                mplew.write((int)pos);
            }
        }
        // 写入物品类型，宠物则写入3，否则写入实际类型
        mplew.write((byte)((item.getPet() != null) ? 3 : item.getType()));
        mplew.writeInt(item.getItemId());
        // 处理现金物品且非宠物的唯一ID分配
        if (ii.isCash(item.getItemId()) && !isPet && item.getUniqueId() < 0) {
            final int uniqueid = MapleItemInformationProvider.getUniqueId(item.getItemId(), null);
            item.setUniqueId(uniqueid);
        }
        // 写入是否有唯一ID的信息
        mplew.write((int)(hasUniqueId ? 1 : 0));
        if (hasUniqueId) {
            if (isPet) {
                mplew.writeLong((long)item.getPet().getUniqueId());
            }
            else if (isRing) {
                mplew.writeLong((long)item.getRing().getRingId());
            }
            else {
                mplew.writeLong((long)item.getUniqueId());
            }
        }
        // 如果是宠物，调用addPetItemInfo方法写入宠物特定信息
        if (item.getPet() != null) {
            addPetItemInfo(mplew, item, item.getPet());
        }
        else {
            // 写入物品的过期时间
            addExpirationTime(mplew, item.getExpiration());
            // 对装备类物品，写入装备的详细属性
            if (item.getType() == 1 && equip != null) {
                mplew.write(equip.getUpgradeSlots());
                mplew.write(equip.getLevel());
                mplew.writeShort((int)equip.getStr());
                mplew.writeShort((int)equip.getDex());
                mplew.writeShort((int)equip.getInt());
                mplew.writeShort((int)equip.getLuk());
                mplew.writeShort((int)equip.getHp());
                mplew.writeShort((int)equip.getMp());
                mplew.writeShort((int)equip.getWatk());
                mplew.writeShort((int)equip.getMatk());
                mplew.writeShort((int)equip.getWdef());
                mplew.writeShort((int)equip.getMdef());
                mplew.writeShort((int)equip.getAcc());
                mplew.writeShort((int)equip.getAvoid());
                mplew.writeShort((int)equip.getHands());
                mplew.writeShort((int)equip.getSpeed());
                mplew.writeShort((int)equip.getJump());
                mplew.writeMapleAsciiString(equip.getOwner());
                mplew.writeShort((int)equip.getFlag());
                mplew.write(0);
                mplew.write(Math.max(equip.getBaseLevel(), equip.getEquipLevel()));
                mplew.writeInt(equip.getExpPercentage() * 100000);
                mplew.writeInt((int)equip.getViciousHammer());
                if (!hasUniqueId) {
                    mplew.writeLong((long)item.getUniqueId());
                }
                mplew.writeLong(getTime(-2L));
                mplew.writeInt(-1);
            } else {
                // 对非装备类物品，写入数量、所有者等信息
                mplew.writeShort((int)item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
                mplew.writeShort((int)item.getFlag());
                if (GameConstants.isExpChair(item.getItemId())) {
                    mplew.writeInt(0);
                    mplew.writeInt(0);
                }
                if (GameConstants.isRechargable(item.getItemId())) {
                    mplew.writeInt(2);
                    mplew.writeShort(84);
                    mplew.write(0);
                    mplew.write(52);
                }
            }
        }
    }
    
    public static void addItemInfo2(final MaplePacketLittleEndianWriter mplew, final IItem item, final boolean zeroPosition, final boolean leaveOut, final boolean trade) {
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final boolean isCash = ii.isCash(item.getItemId());
        final boolean isPet = item.getPet() != null && item.getPet().getUniqueId() > -1;
        boolean isRing = false;
        Equip equip = null;
        short pos = item.getPosition();
        if (item.getType() == 1) {
            equip = (Equip)item;
            isRing = (equip.getRing() != null && equip.getRing().getRingId() > -1);
        }
        if (!zeroPosition) {
            if (equip != null) {
                if (pos < 0) {
                    pos *= -1;
                }
                mplew.write((pos > 100) ? (pos - 100) : pos);
            }
            else {
                mplew.write((int)pos);
            }
        }
        mplew.write(item.getType());
        mplew.writeInt(item.getItemId());
        mplew.write((int)(isCash ? 1 : 0));
        if (isCash) {
            mplew.writeLong(isPet ? ((long)item.getPet().getUniqueId()) : (isRing ? ((long)equip.getRing().getRingId()) : ((long)item.getUniqueId())));
        }
        if (isPet) {
            addPetItemInfo(mplew, item, item.getPet());
        }
        else {
            addExpirationTime(mplew, item.getExpiration());
            if (equip == null) {
                mplew.writeShort((int)item.getQuantity());
                mplew.writeMapleAsciiString(item.getOwner());
                mplew.writeShort((int)item.getFlag());
                if (GameConstants.isRechargable(item.getItemId())) {
                    mplew.writeInt(2);
                    mplew.writeShort(84);
                    mplew.write(0);
                    mplew.write(52);
                }
                return;
            }
            mplew.write(equip.getUpgradeSlots());
            mplew.write(equip.getLevel());
            mplew.writeShort((int)equip.getStr());
            mplew.writeShort((int)equip.getDex());
            mplew.writeShort((int)equip.getInt());
            mplew.writeShort((int)equip.getLuk());
            mplew.writeShort((int)equip.getHp());
            mplew.writeShort((int)equip.getMp());
            mplew.writeShort((int)equip.getWatk());
            mplew.writeShort((int)equip.getMatk());
            mplew.writeShort((int)equip.getWdef());
            mplew.writeShort((int)equip.getMdef());
            mplew.writeShort((int)equip.getAcc());
            mplew.writeShort((int)equip.getAvoid());
            mplew.writeShort((int)equip.getHands());
            mplew.writeShort((int)equip.getSpeed());
            mplew.writeShort((int)equip.getJump());
            mplew.writeMapleAsciiString(equip.getOwner());
            mplew.writeShort((int)equip.getFlag());
            mplew.write(equip.getLevel());
            mplew.write(equip.getExpPercentage());
            mplew.writeInt(0);
            if (!isCash) {
                mplew.writeLong((long)item.getUniqueId());
            }
            mplew.writeLong(getTime(-2L));
            mplew.writeInt(-1);
        }
    }
    
    public static void serializeMovementList(final MaplePacketLittleEndianWriter lew, final List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (final LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }
    
    public static void addAnnounceBox(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr) {
        if (chr.getPlayerShop() != null && chr.getPlayerShop().isOwner(chr) && chr.getPlayerShop().getShopType() != 1 && chr.getPlayerShop().isAvailable()) {
            addInteraction(mplew, chr.getPlayerShop());
        }
        else {
            mplew.write(0);
        }
    }
    /**
     * 向数据包中添加与玩家商店的交互信息
     * 此方法主要用于将玩家商店的相关信息序列化到数据包中，以便在游戏客户端和服务器之间传输
     *
     * @param mplew 用于写入数据的MaplePacketLittleEndianWriter对象，负责将数据打包
     * @param shop 代表玩家商店的IMaplePlayerShop接口实现对象，包含商店信息
     */
    public static void addInteraction(final MaplePacketLittleEndianWriter mplew, final IMaplePlayerShop shop) {
        // 写入商店的游戏类型
        mplew.write(shop.getGameType());
        // 写入商店的对象ID
        mplew.writeInt(((AbstractPlayerStore)shop).getObjectId());
        // 写入商店的描述信息
        mplew.writeMapleAsciiString(shop.getDescription());
        // 如果商店类型不是1，则写入密码是否存在的情况
        if (shop.getShopType() != 1) {
            mplew.write((int)((shop.getPassword().length() > 0) ? 1 : 0));
        }
        // 写入商店项目的ID的最后一位数字
        mplew.write(shop.getItemId() % 10);
        // 写入商店的当前尺寸
        mplew.write(shop.getSize());
        // 写入商店的最大尺寸
        mplew.write(shop.getMaxSize());
        // 如果商店类型不是1，则写入商店是否开放的状态
        if (shop.getShopType() != 1) {
            mplew.write((int)(shop.isOpen() ? 0 : 1));
        }
    }
//    public static void addInteraction(final MaplePacketLittleEndianWriter mplew, final IMaplePlayerShop shop) {
//        mplew.write(shop.getGameType());
//        mplew.writeInt(((AbstractPlayerStore)shop).getObjectId());
//        mplew.writeMapleAsciiString(shop.getDescription());
//        if (shop.getShopType() != 1) {
//            mplew.write((int)((shop.getPassword().length() > 0) ? 1 : 0));
//        }
//        mplew.write(shop.getItemId() % 10);
//        mplew.write(shop.getSize());
//        mplew.write(shop.getMaxSize());
//        if (shop.getShopType() != 1) {
//            mplew.write((int)(shop.isOpen() ? 0 : 1));
//        }
//    }
    
    public static void addCharacterInfo(final MaplePacketLittleEndianWriter mplew, MapleCharacter chr, final boolean isCs) {
        mplew.writeLong(-1L);
        mplew.write(0);
        addCharStats(mplew, chr);
        mplew.write(chr.getBuddylist().getCapacity());
        if (!isCs) {
            if (chr.getBlessOfFairyOrigin() != null) {
                mplew.write(1);
                mplew.writeMapleAsciiString(chr.getBlessOfFairyOrigin());
            }
            else {
                mplew.write(0);
            }
        }
        else {
            mplew.write(0);
        }
        addInventoryInfo(mplew, chr);
        if (!isCs) {
            addSkillInfo(mplew, chr);
        }
        else {
            mplew.writeShort(0);
        }
        if (!isCs) {
            addCoolDownInfo(mplew, chr);
        }
        else {
            mplew.writeShort(0);
        }
        if (!isCs) {
            addQuestInfo(mplew, chr);
        }
        else {
            mplew.writeShort(0);
            mplew.writeShort(0);
        }
        addRingInfo(mplew, chr);
        addRocksInfo(mplew, chr);
        if (!isCs) {
            addMonsterBookInfo(mplew, chr);
        }
        else {
            mplew.writeInt(1);
            mplew.write(0);
            mplew.writeShort(0);
        }
        if (!isCs) {
            chr.QuestInfoPacket(mplew);
        }
        else {
            mplew.writeShort(0);
        }
        mplew.writeShort(0);
        mplew.writeShort(0);
        mplew.writeShort(0);
    }
    
    public static void addPetItemInfo(final MaplePacketLittleEndianWriter mplew, final IItem item, final MaplePet pet) {
        addExpirationTime(mplew, (item != null) ? item.getExpiration() : -1L);
        String petname = pet.getName();
        if (petname == null) {
            petname = "";
        }
        mplew.writeAsciiString(petname, 13);
        mplew.write(pet.getLevel());
        mplew.writeShort((int)pet.getCloseness());
        mplew.write(pet.getFullness());
        if (item == null) {
            mplew.writeLong(getKoreanTimestamp((long)((double)System.currentTimeMillis() * 1.5)));
        }
        else {
            addExpirationTime(mplew, (item.getExpiration() <= System.currentTimeMillis()) ? -1L : item.getExpiration());
        }
        mplew.writeShort(0);
        mplew.writeShort((int)pet.getFlags());
        mplew.writeShort(0);
        for (int i = 0; i < 4; ++i) {
            mplew.write(0);
        }
    }
    /*
     * 其他玩家更换伤害皮肤效果
     */
    public static byte[] showDamageSkin(int chrId, int skinId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        mplew.writeShort(SendPacketOpcode.SHOW_DAMAGE_SKIN.getValue());
        mplew.writeInt(chrId); //玩家ID
        mplew.writeInt(skinId); //更换的伤害皮肤ID

        return mplew.getPacket();
    }
    static {
        unk1 = new byte[] { 0, 64, -32, -3 };
        unk2 = new byte[] { 59, 55, 79, 1 };
    }
}
