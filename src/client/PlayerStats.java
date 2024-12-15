package client;

import abc.套装系统完善版;
import bean.SuitSystem;
import constants.ItemConstants;
import fumo.FumoSkill;
import gui.CongMS;
import gui.LtMS;
import gui.服务端输出信息;
import handling.world.guild.MapleGuild;
import server.*;
import tools.data.MaplePacketLittleEndianWriter;
import client.inventory.MapleWeaponType;
import client.inventory.ModifyInventory;
import tools.MaplePacketCreator;

import java.util.*;

import constants.ServerConfig;
import server.StructSetItem.SetItem;
import java.util.Map.Entry;

import client.inventory.IEquip;
import client.inventory.IItem;
import client.inventory.MapleInventoryType;
import constants.GameConstants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import client.inventory.Equip;

import java.lang.ref.WeakReference;
import java.io.Serializable;

public class PlayerStats implements Serializable
{
    private static final long serialVersionUID = -679541993413738569L;
    private final transient WeakReference<MapleCharacter> chr;
    private final Map<Integer, Integer> setHandling;
    private final List<Equip> durabilityHandling;
    private final List<Equip> equipLevelHandling;
    private transient float shouldHealHP;
    private transient float shouldHealMP;
    public short str;//力量
    public short dex;//敏捷
    public short luk;//运气
    public short int_;//智力
    public short limitStr;
    public short limitDex;
    public short limitLuk;
    public short limitInt;
    public short hp;
    public short maxhp;
    public short mp;
    public short maxmp;
    public transient short passive_sharpeye_percent; //爆击最大伤害倍率
    public transient short localmaxhp;//血量
    public transient short localmaxmp;//蓝量
    public transient byte passive_mastery; //武器熟练度
    public transient byte passive_sharpeye_rate;  //爆击概率
    public transient int localstr; //力量
    public transient int localdex; //敏捷
    public transient int localluk; //运气
    public transient int localint_;//智力

    public transient int magic; //魔法防御
    public transient int watk;  //物理防御
    public transient int hands;//手技
    public transient int accuracy; //命中
    public transient int avoid;
    public transient int wdef;
    public transient int mdef;
    public transient boolean equippedWelcomeBackRing; //以前是1112127 盛大修改 回归戒指 - 热烈欢迎玩家回归的特别戒指，附带特殊福利。佩戴本戒指时，在组队状态下，#c全队队员可享受额外80%的召回经验奖励#。归来的朋友，快去和其他玩家组队一起战斗吧！
    public transient boolean equippedFairy;
    public transient boolean hasMeso;
    public transient boolean hasItem;
    public transient boolean hasVac;
    public transient boolean hasClone;
    public transient boolean hasPartyBonus;
    public transient boolean Berserk;
    public transient boolean isRecalc;
    public transient boolean equippedRing;
    public transient int equipmentBonusExp;
    public transient double expMod;
    public transient int dropMod;
    public transient int cashMod;
    public transient int levelBonus;
    public transient int expMod_H;
    public transient double expBuff;
    public transient double dropBuff;
    public transient double mesoBuff;
    public transient double cashBuff;
    public transient double realExpBuff;
    public transient double realDropBuff;
    public transient double realMesoBuff;
    public transient double realCashBuff;
    public transient double dam_r; //伤害加成
    public transient double bossdam_r; //BOSS伤害加成
    public transient double dropm;
    public transient double expm;
    public transient int itemExpm;
    public transient int itemDropm;
    public transient int recoverHP;
    public transient int recoverMP;
    public transient int mpconReduce;
    public transient int incMesoProp;
    public transient int incRewardProp;
    public transient int DAMreflect;
    public transient int DAMreflect_rate;
    public transient int mpRestore;
    public transient int hpRecover;
    public transient int hpRecoverProp;
    public transient int mpRecover;
    public transient int mpRecoverProp;
    public transient int RecoveryUP;
    public transient int incAllskill;
    private transient float speedMod;
    private transient float jumpMod;
    public transient float localmaxbasedamage;
    public transient int def;
    public transient int element_ice;  //element_ice  冰
    public transient int element_fire; //element_fire 火
    public transient int element_light;//element_light  光
    public transient int element_psn;
    public static short maxStr = 999;
    public ReentrantLock lock;
    public short pickRate;
    public int defRange;
    public transient int dotTime;
    public transient boolean 精灵吊坠;
    private int TZ1, TZ2, TZ3, TZ4, TZ5, TZ6;
    boolean 套装1是否共存, 套装2是否共存, 套装3是否共存, 套装4是否共存, 套装5是否共存, 套装6是否共存;
    private boolean canRefresh = false;
    private long lastMessageTime;
    private long lastMessageTime2;
    private long lastMessageTime3;
    private long lastMessageTime4;
    private long lastMessageTime5;
    private long lastMessageTime6;
    public int pWatk;
    public int pMatk;
    public int pWdef;
    public int pMdef;
    public int pAcc;
    public int pAvoid;
    public int pMaxHp;
    public int pMaxMp;
    public int pSpeed;
    public int pJump;
    public int pWatkPercent;
    public int pMatkPercent;
    public int pWdefPercent;
    public int pMdefPercent;
    public int pAccPercent;
    public int pAvoidPercent;
    public int pMaxHpPercent;
    public int pMaxMpPercent;
    public transient int regularStr,regularDex,regularLuk,regularInt_;//潜能固定加成  力量,敏捷,运气,智力
    public transient int pgStr,pgDex,pgLuk,pgInt_;//潜能百分比加成  力量,敏捷,运气,智力
    //public transient Map<String, List<SuitSystem>> suitSys;//套装列表
    public transient long damage;//伤害
    public transient boolean equippedRing1;
    public transient boolean equippedRing2;

    public PlayerStats(MapleCharacter chr) {
        this.equipLevelHandling = new ArrayList<Equip>();
        this.Berserk = false;
        this.isRecalc = false;
        this.expMod_H = 0;
        this.lock = new ReentrantLock();
        this.setHandling = new HashMap<Integer, Integer>();
        this.durabilityHandling = new ArrayList<Equip>();
        this.chr = new WeakReference<MapleCharacter>(chr);
    }

    {
        itemExpm = 100;
        itemDropm = 0;
    }
    public void init() {
        this.recalcLocalStats();
        this.relocHeal();
    }
    
    public final short getStr() {
        return this.str;
    }
    
    public final short getDex() {
        return this.dex;
    }
    
    public final short getLuk() {
        return this.luk;
    }
    
    public final short getInt() {
        return this.int_;
    }
    
    public void setStr(final short str) {
        this.str = str;
        this.recalcLocalStats();
    }
    
    public void setDex(final short dex) {
        this.dex = dex;
        this.recalcLocalStats();
    }
    
    public void setLuk(final short luk) {
        this.luk = luk;
        this.recalcLocalStats();
    }
    
    public void setInt(final short int_) {
        this.int_ = int_;
        this.recalcLocalStats();
    }
    
    public final boolean setHp(final int newhp) {
        return this.setHp(newhp, false);
    }
    
    public final boolean setHp(final int newhp, final boolean silent) {
        final short oldHp = this.hp;
        int thp = newhp;
        if (thp < 0) {
            thp = 0;
        }
        if (thp > this.localmaxhp) {
            thp = this.localmaxhp;
        }
        this.hp = (short)thp;
        MapleCharacter chra = (MapleCharacter)this.chr.get();
        if (chra != null) {
            if (!silent) {
                chra.updatePartyMemberHP();
            }
            if (oldHp > this.hp && !chra.isAlive()) {
                chra.playerDead();
            }
        }
        return this.hp != oldHp;
    }
    
    public final boolean setMp(final int newmp) {
        final short oldMp = this.mp;
        int tmp = newmp;
        if (tmp < 0) {
            tmp = 0;
        }
        if (tmp > this.localmaxmp) {
            tmp = this.localmaxmp;
        }
        this.mp = (short)tmp;
        return this.mp != oldMp;
    }
    
    public void setMaxHp(final short hp) {
        this.maxhp = hp;
        this.recalcLocalStats();
    }
    
    public void setMaxMp(final short mp) {
        this.maxmp = mp;
        this.recalcLocalStats();
    }
    
    public final short getHp() {
        return this.hp;
    }
    
    public final short getMaxHp() {
        return this.maxhp;
    }
    
    public final short getMp() {
        return this.mp;
    }
    
    public final short getMaxMp() {
        return this.maxmp;
    }
    
    public final int getTotalDex() {
        return this.localdex;
    }
    
    public final int getTotalInt() {
        return this.localint_;
    }
    
    public final int getTotalStr() {
        return this.localstr;
    }
    
    public final int getTotalLuk() {
        return this.localluk;
    }
    
    public final int getTotalMagic() {
        return this.magic;
    }
    
    public final double getSpeedMod() {
        return (double)this.speedMod;
    }
    
    public final double getJumpMod() {
        return (double)this.jumpMod;
    }
    
    public final int getTotalWatk() {
        return this.watk;
    }
    
    public final short getCurrentMaxHp() {
        return this.localmaxhp;
    }
    
    public final short getCurrentMaxMp() {
        return this.localmaxmp;
    }
    
    public final int getHands() {
        return this.hands;
    }
    
    public final float getCurrentMaxBaseDamage() {
        return this.localmaxbasedamage;
    }
    
    public void recalcLocalStats() {
        this.recalcLocalStats(false);
    }


    public void recalcLocalStats(boolean first_login) {
//        if (LtMS.ConfigValuesMap.get("开启功能")>0){
//            first_login = true;
//        }
        MapleCharacter chra = (MapleCharacter)this.chr.get();
        if (chra != null) {
            chra.刷新身上装备镶嵌汇总数据();
            chra.reloadPotentialMap();
            chra.loadPackageList();
            chra.solvePackageStats();
            if (!this.isRecalc) {
                this.isRecalc = true;
                MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                int oldmaxhp = this.localmaxhp;
                int localmaxhp_ = this.getMaxHp();
                int localmaxmp_ = this.getMaxMp();
                this.localdex = this.getDex();
                this.localint_ = this.getInt();
                this.localstr = this.getStr();
                this.localluk = this.getLuk();
                this.limitStr = 0;
                this.limitDex = 0;
                this.limitLuk = 0;
                this.limitInt = 0;
                int speed = 100;
                int jump = 100;
                this.dotTime = 0;
                int percent_hp = 0;
                int percent_mp = 0;
                int percent_str = 0;
                int percent_dex = 0;
                int percent_int = 0;
                int percent_luk = 0;
                int percent_acc = 0;
                int percent_avoid = 0;
                int percent_atk = 0;
                int percent_matk = 0;
                int percent_wdef = 0;
                int percent_mdef = 0;
                int added_sharpeye_rate = 0;
                int added_sharpeye_dmg = 0;
                this.magic = this.localint_;
                this.watk = 0;
                this.wdef = 0;
                this.mdef = 0;
                this.accuracy = 0;
                this.avoid = 0;
                if (chra.getJob() != 500 && (chra.getJob() < 520 || chra.getJob() > 522)) {
                    if (chra.getJob() == 400 || chra.getJob() >= 410 && chra.getJob() <= 412 || chra.getJob() >= 1400 && chra.getJob() <= 1412) {
                        this.watk = 30;
                    }
                } else {
                    this.watk = 20;
                }

                this.dam_r = 100.0;
                this.bossdam_r = 100.0;
                this.realExpBuff = 100.0;
                this.realCashBuff = 100.0;
                this.realDropBuff = 100.0;
                this.realMesoBuff = 100.0;
                this.expBuff = 100.0;
                this.cashBuff = 100.0;
                this.dropBuff = 100.0;
                this.mesoBuff = 100.0;
                this.recoverHP = 0;
                this.recoverMP = 0;
                this.mpconReduce = 0;
                this.incMesoProp = 0;
                this.incRewardProp = 0;
                this.DAMreflect = 0;
                this.DAMreflect_rate = 0;
                this.hpRecover = 0;
                this.hpRecoverProp = 0;
                this.mpRecover = 0;
                this.mpRecoverProp = 0;
                this.mpRestore = 0;
                this.equippedWelcomeBackRing = false;
                this.equippedRing = false;
                this.equippedRing1 = false;
                this.equippedRing2 = false;
                this.equippedFairy = false;
                this.hasMeso = false;
                this.hasItem = false;
                this.hasPartyBonus = false;
                this.hasVac = false;
                this.hasClone = false;
                boolean canEquipLevel = chra.getLevel() >= 70;
                this.equipmentBonusExp = 0;
                this.RecoveryUP = 0;
                this.dropMod = 1;
                this.dropm = 1.0;
                this.expMod = 1.0;
                this.expm = 1.0;
                this.cashMod = 1;
                this.levelBonus = 0;
                this.incAllskill = 0;
                this.durabilityHandling.clear();
                this.equipLevelHandling.clear();
                this.setHandling.clear();
                this.element_fire = 100;
                this.element_ice = 100;
                this.element_light = 100;
                this.element_psn = 100;
                this.def = 100;
                this.defRange = 0;

                int package_luk;
                int percentStr;
                int eb_bonus;
                int percentInt;
                int i;
                int percentTotal;
                int p_luk;
                for (IItem iItemEquipped : chra.getInventory(MapleInventoryType.EQUIPPED)) {
                    IEquip equip = (IEquip)iItemEquipped;
                    if (equip.getPosition() == -11 && GameConstants.isMagicWeapon(equip.getItemId())) {
                        Map<String, Integer> eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());
                        this.element_fire = (Integer)eqstat.get("incRMAF");
                        this.element_ice = (Integer)eqstat.get("incRMAI");
                        this.element_light = (Integer)eqstat.get("incRMAL");
                        this.element_psn = (Integer)eqstat.get("incRMAS");
                        this.def = (Integer)eqstat.get("elemDefault");
                    }

                    this.accuracy += equip.getAcc();
                    this.wdef += equip.getWdef();
                    this.mdef += equip.getMdef();
                    this.avoid += equip.getAvoid();
                    localmaxhp_ += equip.getHp();
                    localmaxmp_ += equip.getMp();
                    this.localdex += equip.getDex();
                    this.localint_ += equip.getInt();
                    this.localstr += equip.getStr();
                    this.localluk += equip.getLuk();
                    this.magic += equip.getMatk() + equip.getInt();
                    this.watk += equip.getWatk();
                    speed += equip.getSpeed();
                    jump += equip.getJump();
                    switch (equip.getItemId()) {
                        case 1112127:
                            this.equippedWelcomeBackRing = true;
                            break;
                        case 1112427:
                            added_sharpeye_rate += 5;
                            added_sharpeye_dmg += 20;
                            break;
                        case 1112428:
                            added_sharpeye_rate += 10;
                            added_sharpeye_dmg += 10;
                            break;
                        case 1112429:
                            added_sharpeye_rate += 5;
                            added_sharpeye_dmg += 20;
                            break;
                        case 1114000:
                            this.equippedRing = true;
                            break;
                        case 1114317:
                            this.equippedRing1 = true;
                            break;
                        case 1114400:
                            this.equippedRing2 = true;
                            break;
                        case 1122017:
                        case 1122086:
                        case 1122155:
                        case 1122156:
                        case 1122207:
                        case 1122214:
                        case 1122215:
                        case 1122271:
                        case 1122316:
                            this.equippedFairy = true;
                            break;
                        case 1812000:
                            this.hasMeso = true;
                            break;
                        case 1812001:
                            this.hasItem = true;
                            break;
                        default:
                            int[] var68 = GameConstants.Equipments_Bonus;
                            package_luk = var68.length;

                            for(percentStr = 0; percentStr < package_luk; ++percentStr) {
                                eb_bonus = var68[percentStr];
                                if (equip.getItemId() == eb_bonus) {
                                    this.equipmentBonusExp += GameConstants.Equipment_Bonus_EXP(eb_bonus);
                                    break;
                                }
                            }
                    }

                    percent_hp += equip.getHpR();
                    percent_mp += equip.getMpR();
                    p_luk = ii.getSetItemID(equip.getItemId());
                    if (p_luk > 0) {
                        package_luk = 1;
                        if (this.setHandling.get(p_luk) != null) {
                            package_luk += (Integer)this.setHandling.get(p_luk);
                        }

                        this.setHandling.put(p_luk, package_luk);
                    }

                    if (equip.getState() > 1) {
                        int[] potentials = new int[]{equip.getPotential1(), equip.getPotential2(), equip.getPotential3()};
                        int[] var72 = potentials;
                        eb_bonus = potentials.length;

                        for(percentInt = 0; percentInt < eb_bonus; ++percentInt) {
                            i = var72[percentInt];
                            if (i > 0) {
                                percentTotal = ii.getReqLevel(equip.getItemId()) / 10 != 0 ? ii.getReqLevel(equip.getItemId()) / 10 - 1 : 0;
                                StructPotentialItem pot = (StructPotentialItem)ii.getPotentialInfo(i).get(percentTotal);
                                if (pot != null) {
                                    this.localstr += pot.incSTR;
                                    this.localdex += pot.incDEX;
                                    this.localint_ += pot.incINT;
                                    this.localluk += pot.incLUK;
                                    this.localmaxhp += pot.incMHP;
                                    this.localmaxmp += pot.incMMP;
                                    this.watk += pot.incPAD;
                                    this.magic += pot.incINT + pot.incMAD;
                                    speed += pot.incSpeed;
                                    jump += pot.incJump;
                                    this.accuracy += pot.incACC;
                                    this.avoid += pot.incEVA;
                                    this.wdef += pot.incPDD;
                                    this.mdef += pot.incMDD;
                                    this.incAllskill += pot.incAllskill;
                                    percent_hp += pot.incMHPr;
                                    percent_mp += pot.incMMPr;
                                    percent_str += pot.incSTRr;
                                    percent_dex += pot.incDEXr;
                                    percent_int += pot.incINTr;
                                    percent_luk += pot.incLUKr;
                                    percent_acc += pot.incACCr;
                                    percent_avoid += pot.incEVAr;
                                    percent_atk += pot.incPADr;
                                    percent_matk += pot.incMADr;
                                    percent_wdef += pot.incPDDr;
                                    percent_mdef += pot.incMDDr;
                                    added_sharpeye_rate += pot.incCr;
                                    added_sharpeye_dmg += pot.incCr;
                                    if (!pot.boss) {
                                        this.dam_r = Math.max((double)pot.incDAMr, this.dam_r);
                                    } else {
                                        this.bossdam_r = Math.max((double)pot.incDAMr, this.bossdam_r);
                                    }

                                    this.recoverHP += pot.RecoveryHP;
                                    this.recoverMP += pot.RecoveryMP;
                                    this.RecoveryUP += pot.RecoveryUP;
                                    if (pot.HP > 0) {
                                        this.hpRecover += pot.HP;
                                        this.hpRecoverProp += pot.prop;
                                    }

                                    if (pot.MP > 0) {
                                        this.mpRecover += pot.MP;
                                        this.mpRecoverProp += pot.prop;
                                    }

                                    this.mpconReduce += pot.mpconReduce;
                                    this.incMesoProp += pot.incMesoProp;
                                    this.incRewardProp += pot.incRewardProp;
                                    if (pot.DAMreflect > 0) {
                                        this.DAMreflect += pot.DAMreflect;
                                        this.DAMreflect_rate += pot.prop;
                                    }

                                    this.mpRestore += pot.mpRestore;
                                    if (!first_login && pot.skillID > 0) {
                                        chra.changeSkillLevel_Skip(SkillFactory.getSkill(this.getSkillByJob(pot.skillID, chra.getJob())), (byte)1, (byte)1);
                                    }
                                }
                            }
                        }
                    }

                    if (equip.getDurability() > 0) {
                        this.durabilityHandling.add((Equip)equip);
                    }

                    if (canEquipLevel && GameConstants.getMaxLevel(equip.getItemId()) > 0) {
                        if (GameConstants.getStatFromWeapon(equip.getItemId()) == null) {
                            if (equip.getEquipLevel() > GameConstants.getMaxLevel(equip.getItemId())) {
                                continue;
                            }
                        } else if (equip.getEquipLevel() >= GameConstants.getMaxLevel(equip.getItemId())) {
                            continue;
                        }

                        this.equipLevelHandling.add((Equip)equip);
                    }
                }


                int package_num;
                if (chra.F().get(FumoSkill.FM("强身健体")) != null) {
                    package_num = (Integer)chra.F().get(FumoSkill.FM("强身健体"));
                    if (package_num > 0) {
                        this.localdex += package_num;
                        this.localint_ += package_num;
                        this.localstr += package_num;
                        this.localluk += package_num;
                        this.limitStr = (short)(this.limitStr + package_num);
                        this.limitInt = (short)(this.limitInt + package_num);
                        this.limitDex = (short)(this.limitDex + package_num);
                        this.limitLuk = (short)(this.limitLuk + package_num);
                        if (first_login && System.currentTimeMillis() - this.lastMessageTime > 2000L) {
                            chra.dropMessage(5, "镶嵌效果：【强身健体】 全属性 + " + package_num + " 点");
                            this.lastMessageTime = System.currentTimeMillis();
                        }
                    }
                }

                int p_dex;
                int p_int;
                if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
                    package_num = chra.getPotential(1);
                    p_dex = chra.getPotential(2);
                    p_int = chra.getPotential(3);
                    p_luk = chra.getPotential(4);
                    package_luk = chra.getPotential(9);
                    percentStr = chra.getPotential(5);
                    eb_bonus = chra.getPotential(6);
                    percentInt = chra.getPotential(7);
                    i = chra.getPotential(8);
                    percentTotal = chra.getPotential(10);
                    package_num += package_luk;
                    p_dex += package_luk;
                    p_int += package_luk;
                    p_luk += package_luk;
                    if (percentStr > 0) {
                        package_num += chra.getStat().getStr() * percentStr / 100;
                    }

                    if (eb_bonus > 0) {
                        p_dex += chra.getStat().getDex() * eb_bonus / 100;
                    }

                    if (percentInt > 0) {
                        p_int += chra.getStat().getInt() * percentInt / 100;
                    }

                    if (i > 0) {
                        p_luk += chra.getStat().getLuk() * i / 100;
                    }

                    if (percentTotal > 0) {
                        package_num += chra.getStat().getStr() * percentTotal / 100;
                        p_dex += chra.getStat().getDex() * percentTotal / 100;
                        p_int += chra.getStat().getInt() * percentTotal / 100;
                        p_luk += chra.getStat().getLuk() * percentTotal / 100;
                    }

                    if (package_num > 0 || p_dex > 0 || p_int > 0 || p_luk > 0) {
                        String text = "【潜能加成】：";
                        if (package_num > 0) {
                            this.localstr += package_num;
                            this.limitStr = (short)(this.limitStr + package_num);
                            text = text + "力量+" + package_num + " ";
                        }

                        if (p_dex > 0) {
                            this.localdex += p_dex;
                            this.limitDex = (short)(this.limitDex + p_dex);
                            text = text + "敏捷+" + p_dex + " ";
                        }

                        if (p_int > 0) {
                            this.localint_ += p_int;
                            this.limitInt = (short)(this.limitInt + p_int);
                            text = text + "智力+" + p_int + " ";
                        }

                        if (p_luk > 0) {
                            this.localluk += p_luk;
                            this.limitLuk = (short)(this.limitLuk + p_luk);
                            text = text + "运气+" + p_luk + " ";
                        }

                        if (first_login && System.currentTimeMillis() - this.lastMessageTime3 > 2000L) {
                            chra.dropMessage(5, text);
                            this.lastMessageTime3 = System.currentTimeMillis();
                        }
                    }
                }
                //套装加成
                package_num = chra.getPackageList() == null ? 0 : chra.getPackageList().size();
                int package_str = chra.package_str;
                int package_dex = chra.package_dex;
                int package_int = chra.package_int;
                package_luk = chra.package_luk;
                int package_total = chra.package_all_ap;
                int package_percentStr = chra.package_str_percent;
                int package_percentDex = chra.package_dex_percent;
                int package_percentInt = chra.package_int_percent;
                int package_percentLuk = chra.package_luk_percent;
                int package_percentTotal = chra.package_all_ap_percent;
                p_dex = package_str + package_total;
                p_int = package_dex + package_total;
                p_luk = package_int + package_total;
                package_luk = package_luk + package_total;
                if (package_percentStr > 0) {
                    p_dex += chra.getStat().getStr() * package_percentStr / 100;
                }

                if (package_percentDex > 0) {
                    p_int += chra.getStat().getDex() * package_percentDex / 100;
                }

                if (package_percentInt > 0) {
                    p_luk += chra.getStat().getInt() * package_percentInt / 100;
                }

                if (package_percentLuk > 0) {
                    package_luk += chra.getStat().getLuk() * package_percentLuk / 100;
                }

                if (package_percentTotal > 0) {
                    p_dex += chra.getStat().getStr() * package_percentTotal / 100;
                    p_int += chra.getStat().getDex() * package_percentTotal / 100;
                    p_luk += chra.getStat().getInt() * package_percentTotal / 100;
                    package_luk += chra.getStat().getLuk() * package_percentTotal / 100;
                }

                if (p_dex > 0 || p_int > 0 || p_luk > 0 || package_luk > 0) {
                    String text = "【套装加成】：穿戴了" + package_num + "组套装，";
                    if (p_dex > 0) {
                        this.localstr += p_dex;
                        this.limitStr = (short)(this.limitStr + p_dex);
                        text = text + "力量+" + p_dex + " ";
                    }

                    if (p_int > 0) {
                        this.localdex += p_int;
                        this.limitDex = (short)(this.limitDex + p_int);
                        text = text + "敏捷+" + p_int + " ";
                    }

                    if (p_luk > 0) {
                        this.localint_ += p_luk;
                        this.limitInt = (short)(this.limitInt + p_luk);
                        text = text + "智力+" + p_luk + " ";
                    }

                    if (package_luk > 0) {
                        this.localluk += package_luk;
                        this.limitLuk = (short)(this.limitLuk + package_luk);
                        text = text + "运气+" + package_luk + " ";
                    }

                    if (first_login && System.currentTimeMillis() - this.lastMessageTime5 > 2000L) {
                        chra.dropMessage(5, text);
                        this.lastMessageTime5 = System.currentTimeMillis();
                    }
                }

                MapleGuild guild = chra.getGuild();
                int hour;
                if (guild != null) {
                    int skillVal = guild.getGuildSkillVal(1);
                    hour = guild.getGuildSkillLevel(1);
                    if (skillVal > 0) {
                        this.localdex += skillVal;
                        this.localint_ += skillVal;
                        this.localstr += skillVal;
                        this.localluk += skillVal;
                        this.limitStr = (short)(this.limitStr + skillVal);
                        this.limitInt = (short)(this.limitInt + skillVal);
                        this.limitDex = (short)(this.limitDex + skillVal);
                        this.limitLuk = (short)(this.limitLuk + skillVal);
                        if (first_login && System.currentTimeMillis() - this.lastMessageTime2 > 2000L) {
                            chra.dropMessage(5, "家族技能：【强壮】 等级：" + hour + "  效果：全属性 + " + skillVal + " 点");
                            this.lastMessageTime2 = System.currentTimeMillis();
                        }
                    }
                }

                if (this.canRefresh) {
                    this.refreshLimitAP();
                }

                Iterator<Map.Entry<Integer, Integer>> iter = this.setHandling.entrySet().iterator();
                StructSetItem set;
                Map.Entry entry;
                    do {
                        if (!iter.hasNext()) {
                            if (chra.getMarriageId() > 0) {
                                this.expm = 1.1;
                                this.dropm = 1.1;
                            }

                            int before_;
                            int singleDropm;
                            int pGainExpPercent;
                            if ((Integer)LtMS.ConfigValuesMap.get("星球椅子爆率加成开关") > 0) {
                                singleDropm = (Integer)LtMS.ConfigValuesMap.get("单个星球椅子爆率加成");
                                int singleDropm2 = (Integer)LtMS.ConfigValuesMap.get("太阳椅子爆率加成");
                                pGainExpPercent = 0;
                                boolean haveSun = false;

                                for(before_ = 3010824; before_ < 3010833; ++before_) {
                                    if (chra.haveItem(before_, 1, true, true)) {
                                        if (before_ == 3010832) {
                                            this.dropm += (double)singleDropm2 / 100.0;
                                            haveSun = true;
                                        } else {
                                            this.dropm += (double)singleDropm / 100.0;
                                        }

                                        ++pGainExpPercent;
                                    }
                                }

                                if (pGainExpPercent > 0 && first_login) {
                                    if (haveSun) {
                                        chra.dropMessage(5, "你身上拥有 " + pGainExpPercent + " 件不重复的星球椅子，共追加了 " + ((pGainExpPercent - 1) * singleDropm + singleDropm2) + "% 爆率");
                                    } else {
                                        chra.dropMessage(5, "你身上拥有 " + pGainExpPercent + " 件不重复的星球椅子，共追加了 " + pGainExpPercent * singleDropm + "% 爆率");
                                    }
                                }
                            }

                            if (chra.是否开店()) {
                                singleDropm = (Integer)LtMS.ConfigValuesMap.get("开店爆率加成");
                                if (singleDropm > 0) {
                                    this.dropm += (double)singleDropm / 100.0;
                                }
                            }

                            singleDropm = chra.getPotential(41);
                            if (singleDropm > 0 && (Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
                                this.dropm += (double)singleDropm / 100.0;
                                if (first_login) {
                                    chra.dropMessage(5, "【潜能系统】：狩猎物品掉落率增加" + singleDropm + "%。");
                                }
                            }


                            if (first_login && chra.haveItem(3700070)) {
                                chra.dropMessage(5, "检测到您持有《功能卡-无限弓镖弹》，使用技能将不消耗弓镖弹！");
                            }

                            if (first_login && chra.haveItem(3700071)) {
                                chra.dropMessage(5, "检测到您持有《功能卡-无限MP》，使用技能将不消耗MP！");
                            }

                            if (first_login && (Integer)LtMS.ConfigValuesMap.get("VIP无敌开关") > 0 && chra.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无敌道具ID"))) {
                                chra.dropMessage(5, "检测到您持有《VIP无敌道具》，将会免疫怪物和BOSS的一切伤害与技能！");
                            }

                            String msg = "";
                            if (first_login && chra.getExpRateChr() > 1.0F) {
                                msg = msg + "经验倍率：+" + Math.ceil(((double)chra.getExpRateChr() - 1.0) * 100.0) + "%   ";
                            }

                            if (first_login && chra.getMesoRateChr() > 1.0F) {
                                msg = msg + "金币倍率：+" + Math.ceil(((double)chra.getMesoRateChr() - 1.0) * 100.0) + "%   ";
                            }

                            if (first_login && chra.getDropRateChr() > 1.0F) {
                                msg = msg + "掉落倍率：+" + Math.ceil(((double)chra.getDropRateChr() - 1.0) * 100.0) + "%   ";
                            }

                            if (!msg.equals("")) {
                                chra.dropMessage(5, "【个人倍率加成】" + msg);
                            }

                            if (this.equippedRing1) {
                                this.expm += 0.1;
                            }

                            if (this.equippedRing2) {
                                this.expm += 0.2;
                            }

                            this.expMod = 1.0;
                            this.dropMod = 1;
                            for (IItem iItemCash : chra.getInventory(MapleInventoryType.CASH)) {
                                if (this.expMod < 3.0 && GameConstants.isTripleExpCard(iItemCash.getItemId())) {
                                    this.expMod = 3.0;
                                } else if (this.expMod < 2.0 && GameConstants.isDoubleExpCard(iItemCash.getItemId())) {
                                    this.expMod = 2.0;
                                }

                                if (this.dropMod == 1 && GameConstants.isDoubleDropCard(iItemCash.getItemId())) {
                                    this.dropMod = 2;
                                }

                                if (iItemCash.getItemId() == 5650000) {
                                    this.hasPartyBonus = true;
                                } else if ((int)((double)iItemCash.getItemId() / 10000.0) == 559) {
                                    before_ = MapleItemInformationProvider.getInstance().getIncLEV(iItemCash.getItemId());
                                    if (before_ > this.levelBonus) {
                                        this.levelBonus = before_;
                                    }
                                }
                            }

                            for (IItem iItem : chra.getInventory(MapleInventoryType.ETC)) {
                                if (this.expMod < 3.0 && GameConstants.isTripleExpCard(iItem.getItemId())) {
                                    this.expMod = 3.0;
                                } else if (this.expMod < 2.0 && GameConstants.isDoubleExpCard(iItem.getItemId())) {
                                    this.expMod = 2.0;
                                }

                                if (this.dropMod == 1 && GameConstants.isDoubleDropCard(iItem.getItemId())) {
                                    this.dropMod = 2;
                                }
                            }

                            if ((Integer)LtMS.ConfigValuesMap.get("新手多倍经验开关") > 0 && chra.getLevel() < (Integer)LtMS.ConfigValuesMap.get("新手多倍经验判定等级")) {
                                this.expMod *= (double)(Integer)LtMS.ConfigValuesMap.get("新手多倍经验倍率");
                            }

                            pGainExpPercent = chra.getPotential(39);
                            if (pGainExpPercent > 0 && (Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
                                this.expMod += this.expMod * (double)pGainExpPercent / 100.0;
                                if (first_login) {
                                    chra.dropMessage(5, "【潜能系统】：狩猎经验增加" + pGainExpPercent + "%。");
                                }
                            }

                            int pGainMesoPercent = chra.getPotential(40);
                            if (pGainMesoPercent > 0 && first_login && (Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
                                chra.dropMessage(5, "【潜能系统】：狩猎金币掉落率增加" + pGainMesoPercent + "%。");
                            }

                            for (IItem itemEtc : chra.getInventory(MapleInventoryType.ETC)) {
                                switch (itemEtc.getItemId()) {
                                    case 4030005:
                                        this.cashMod = 2;
                                        break;
                                    case 4101000:
                                    case 4101002:
                                        this.equippedFairy = true;
                                        chra.setFairyExp((byte)30);
                                }
                            }

                            if ((Integer)LtMS.ConfigValuesMap.get("宠吸开关") > 0 && chra.haveItem((Integer)LtMS.ConfigValuesMap.get("宠吸道具ID"), 1, true, true)) {
                                this.hasVac = true;
//                                if (first_login) {
//                                    chra.dropMessage(5, "【宠吸系统】：检测到您持有 [" + MapleItemInformationProvider.getInstance().getName((Integer)LtMS.ConfigValuesMap.get("宠吸道具ID")) + "]，已开启宠吸！");
//                                }
                            }

                            for (IItem setupItem : chra.getInventory(MapleInventoryType.SETUP)) {
                                switch (setupItem.getItemId()) {
                                    case 3020031:
                                    case 3700069:
                                }
                            }

                            this.magic += chra.getSkillLevel(SkillFactory.getSkill(22000000));
                            this.localstr = (int)((float)this.localstr + (float)(percent_str * this.localstr) / 100.0F);
                            this.localdex = (int)((float)this.localdex + (float)(percent_dex * this.localdex) / 100.0F);
                            before_ = this.localint_;
                            this.localint_ = (int)((float)this.localint_ + (float)(percent_int * this.localint_) / 100.0F);
                            this.magic += this.localint_ - before_;
                            this.localluk = (int)((float)this.localluk + (float)(percent_luk * this.localluk) / 100.0F);
                            this.accuracy = (int)((float)this.accuracy + (float)(percent_acc * this.accuracy) / 100.0F);
                            this.avoid = (int)((float)this.avoid + (float)(percent_avoid * this.avoid) / 100.0F);
                            this.watk = (int)((float)this.watk + (float)(percent_atk * this.watk) / 100.0F);
                            this.magic = (int)((float)this.magic + (float)(percent_matk * this.magic) / 100.0F);
                            this.wdef = (int)((float)this.wdef + (float)(percent_wdef * this.wdef) / 100.0F);
                            this.mdef = (int)((float)this.mdef + (float)(percent_mdef * this.mdef) / 100.0F);
                            localmaxhp_ = (int)((float)localmaxhp_ + (float)(percent_hp * localmaxhp_) / 100.0F);
                            localmaxmp_ = (int)((float)localmaxmp_ + (float)(percent_mp * localmaxmp_) / 100.0F);
                            this.magic = Math.min(this.magic, 1999);
                            if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
                                this.pWatk = chra.getPotential(11);
                                this.pMatk = chra.getPotential(12);
                                this.pWatkPercent = chra.getPotential(13);
                                this.pMatkPercent = chra.getPotential(14);
                                this.pWdef = chra.getPotential(15);
                                this.pWdefPercent = chra.getPotential(16);
                                this.pMdef = chra.getPotential(17);
                                this.pMdefPercent = chra.getPotential(18);
                                this.pAcc = chra.getPotential(19);
                                this.pAccPercent = chra.getPotential(20);
                                this.pAvoid = chra.getPotential(21);
                                this.pAvoidPercent = chra.getPotential(22);
                                this.pMaxHp = chra.getPotential(23);
                                this.pMaxHpPercent = chra.getPotential(24);
                                this.pMaxMp = chra.getPotential(25);
                                this.pMaxMpPercent = chra.getPotential(26);
                                this.pSpeed = chra.getPotential(27);
                                this.pJump = chra.getPotential(28);
                                if (this.pWatk > 0 || this.pMatk > 0 || this.pWatkPercent > 0 || this.pMatkPercent > 0 || this.pWdef > 0 || this.pWdefPercent > 0 || this.pMdef > 0 || this.pMdefPercent > 0 || this.pAcc > 0 || this.pAccPercent > 0 || this.pAvoid > 0 || this.pAvoidPercent > 0 || this.pMaxHp > 0 || this.pMaxHpPercent > 0 || this.pMaxMp > 0 || this.pMaxMpPercent > 0 || this.pSpeed > 0 || this.pJump > 0) {
                                    if (this.pWatkPercent > 0) {
                                        this.pWatk += this.watk * this.pWatkPercent / 100;
                                    }

                                    if (this.pMatkPercent > 0) {
                                        this.pMatk += this.magic * this.pMatkPercent / 100;
                                    }

                                    if (this.pWdefPercent > 0) {
                                        this.pWdef += this.wdef * this.pWdefPercent / 100;
                                    }

                                    if (this.pMdefPercent > 0) {
                                        this.pMdef += this.mdef * this.pMdefPercent / 100;
                                    }

                                    if (this.pAccPercent > 0) {
                                        this.pAcc += this.accuracy * this.pAccPercent / 100;
                                    }

                                    if (this.pAvoidPercent > 0) {
                                        this.pAvoid += this.avoid * this.pAvoidPercent / 100;
                                    }

                                    if (this.pMaxHp > 0) {
                                        this.pMaxHpPercent = (int)((double)this.pMaxHpPercent + Math.ceil((double)((float)(this.pMaxHp * 100) / (float)localmaxhp_)));
                                        this.pMaxHp += this.pMaxHpPercent * localmaxhp_ / 100;
                                    }

                                    if (this.pMaxMp > 0) {
                                        this.pMaxMpPercent = (int)((double)this.pMaxMpPercent + Math.ceil((double)((float)(this.pMaxMp * 100) / (float)localmaxmp_)));
                                        this.pMaxMp += this.pMaxMpPercent * localmaxmp_ / 100;
                                    }

                                    if (first_login && System.currentTimeMillis() - this.lastMessageTime4 > 2000L) {
                                        StringBuilder text = new StringBuilder();
                                        text.append("【潜能加成】：");
                                        if (this.pWatk > 0) {
                                            text.append("攻击力+");
                                            text.append(this.pWatk);
                                            text.append(" ");
                                        }

                                        if (this.pMatk > 0) {
                                            text.append("魔法力+");
                                            text.append(this.pMatk);
                                            text.append(" ");
                                        }

                                        if (this.pWdef > 0) {
                                            text.append("物防+");
                                            text.append(this.pWdef);
                                            text.append(" ");
                                        }

                                        if (this.pMdef > 0) {
                                            text.append("魔防+");
                                            text.append(this.pMdef);
                                            text.append(" ");
                                        }

                                        if (this.pAcc > 0) {
                                            text.append("命中+");
                                            text.append(this.pAcc);
                                            text.append(" ");
                                        }

                                        if (this.pAvoid > 0) {
                                            text.append("回避+");
                                            text.append(this.pAvoid);
                                            text.append(" ");
                                        }

                                        if (this.pSpeed > 0) {
                                            text.append("速度+");
                                            text.append(this.pSpeed);
                                            text.append(" ");
                                        }

                                        if (this.pJump > 0) {
                                            text.append("跳跃+");
                                            text.append(this.pJump);
                                            text.append(" ");
                                        }

                                        if (this.pMaxHp > 0) {
                                            text.append("maxHp+");
                                            text.append(this.pMaxHpPercent);
                                            text.append("% ");
                                        }

                                        if (this.pMaxMp > 0) {
                                            text.append("maxMp+");
                                            text.append(this.pMaxMpPercent);
                                            text.append("% ");
                                        }

                                        chra.dropMessage(5, text.toString());
                                        this.lastMessageTime4 = System.currentTimeMillis();
                                    }
                                }
                            }

                            int package_Watk = chra.package_watk;
                            int package_Matk = chra.package_matk;
                            int package_WatkPercent = chra.package_watk_percent;
                            int package_MatkPercent = chra.package_matk_percent;
                            int package_Wdef = chra.package_wdef;
                            int package_WdefPercent = chra.package_wdef_percent;
                            int package_Mdef = chra.package_mdef;
                            int package_MdefPercent = chra.package_mdef_percent;
                            int package_Acc = chra.package_acc;
                            int package_AccPercent = chra.package_acc_percent;
                            int package_Avoid = chra.package_avoid;
                            int package_AvoidPercent = chra.package_avoid_percent;
                            int package_MaxHp = chra.package_maxhp;
                            int package_MaxHpPercent = chra.package_maxhp_percent;
                            int package_MaxMp = chra.package_maxmp;
                            int package_MaxMpPercent = chra.package_maxmp_percent;
                            int package_Speed = chra.package_speed;
                            int package_Jump = chra.package_jump;
                            if (package_Watk > 0 || package_Matk > 0 || package_WatkPercent > 0 || package_MatkPercent > 0 || package_Wdef > 0 || package_WdefPercent > 0 || package_Mdef > 0 || package_MdefPercent > 0 || package_Acc > 0 || package_AccPercent > 0 || package_Avoid > 0 || package_AvoidPercent > 0 || package_MaxHp > 0 || package_MaxHpPercent > 0 || package_MaxMp > 0 || package_MaxMpPercent > 0 || package_Speed > 0 || package_Jump > 0) {
                                if (package_WatkPercent > 0) {
                                    package_Watk += this.watk * package_WatkPercent / 100;
                                }

                                if (package_MatkPercent > 0) {
                                    package_Matk += this.magic * package_MatkPercent / 100;
                                }

                                if (package_WdefPercent > 0) {
                                    package_Wdef += this.wdef * package_WdefPercent / 100;
                                }

                                if (package_MdefPercent > 0) {
                                    package_Mdef += this.mdef * package_MdefPercent / 100;
                                }

                                if (package_AccPercent > 0) {
                                    package_Acc += this.accuracy * package_AccPercent / 100;
                                }

                                if (package_AvoidPercent > 0) {
                                    package_Avoid += this.avoid * package_AvoidPercent / 100;
                                }

                                if (package_Watk > 0) {
                                    this.pWatk += package_Watk;
                                }

                                if (package_Matk > 0) {
                                    this.pMatk += package_Matk;
                                }

                                if (package_Wdef > 0) {
                                    this.pWdef += package_Wdef;
                                }

                                if (package_Mdef > 0) {
                                    this.pMdef += package_Mdef;
                                }

                                if (package_Acc > 0) {
                                    this.pAcc += package_Acc;
                                }

                                if (package_Avoid > 0) {
                                    this.pAvoid += package_Avoid;
                                }

                                if (package_MaxHp > 0) {
                                    package_MaxHpPercent += package_MaxHp * 100 / localmaxhp_;
                                    package_MaxHp += package_MaxHpPercent * localmaxhp_ / 100;
                                    this.pMaxHp += package_MaxHp;
                                }

                                if (package_MaxMp > 0) {
                                    package_MaxMpPercent += package_MaxMp * 100 / localmaxmp_;
                                    package_MaxMp += package_MaxMpPercent * localmaxmp_ / 100;
                                    this.pMaxMp += package_MaxMp;
                                }

                                if (first_login && System.currentTimeMillis() - this.lastMessageTime6 > 2000L) {
                                    StringBuilder text = new StringBuilder();
                                    text.append("【套装加成】：穿戴了" + package_num + "组套装，");
                                    if (package_Watk > 0) {
                                        text.append("攻击力+");
                                        text.append(package_Watk);
                                        text.append(" ");
                                    }

                                    if (package_Matk > 0) {
                                        text.append("魔法力+");
                                        text.append(package_Matk);
                                        text.append(" ");
                                    }

                                    if (package_Wdef > 0) {
                                        text.append("物防+");
                                        text.append(package_Wdef);
                                        text.append(" ");
                                    }

                                    if (package_Mdef > 0) {
                                        text.append("魔防+");
                                        text.append(package_Mdef);
                                        text.append(" ");
                                    }

                                    if (package_Acc > 0) {
                                        text.append("命中+");
                                        text.append(package_Acc);
                                        text.append(" ");
                                    }

                                    if (package_Avoid > 0) {
                                        text.append("回避+");
                                        text.append(package_Avoid);
                                        text.append(" ");
                                    }

                                    if (package_Speed > 0) {
                                        text.append("速度+");
                                        text.append(package_Speed);
                                        text.append(" ");
                                    }

                                    if (package_Jump > 0) {
                                        text.append("跳跃+");
                                        text.append(package_Jump);
                                        text.append(" ");
                                    }

                                    if (package_MaxHp > 0) {
                                        text.append("maxHp+");
                                        text.append(package_MaxHpPercent);
                                        text.append("% ");
                                    }

                                    if (package_MaxMp > 0) {
                                        text.append("maxMp+");
                                        text.append(package_MaxMpPercent);
                                        text.append("% ");
                                    }

                                    chra.dropMessage(5, text.toString());
                                    this.lastMessageTime6 = System.currentTimeMillis();
                                }
                            }

                            Integer buff = chra.getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
                            double d;
                            if (buff != null) {
                                d = buff.doubleValue() / 100.0;
                                this.localstr = (int)((double)this.localstr + d * (double)this.str);
                                this.localdex = (int)((double)this.localdex + d * (double)this.dex);
                                this.localluk = (int)((double)this.localluk + d * (double)this.luk);
                                package_MatkPercent = this.localint_;
                                this.localint_ = (int)((double)this.localint_ + d * (double)this.int_);
                                this.magic += this.localint_ - package_MatkPercent;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.ECHO_OF_HERO);
                            if (buff != null) {
                                d = buff.doubleValue() / 100.0;
                                this.watk += (int)((double)this.watk * d);
                                this.magic += (int)((double)this.magic * d);
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.ARAN_COMBO);
                            if (buff != null) {
                                this.watk += buff / 10;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MAXHP);
                            if (buff != null) {
                                localmaxhp_ = (int)((double)localmaxhp_ + buff.doubleValue() / 100.0 * (double)localmaxhp_);
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MAXMP);
                            if (buff != null) {
                                localmaxmp_ = (int)((double)localmaxmp_ + buff.doubleValue() / 100.0 * (double)localmaxmp_);
                            }

                            byte boflevel;
                            ISkill blessoffairy;
                            switch (chra.getJob()) {
                                case 211:
                                case 212:
                                    blessoffairy = SkillFactory.getSkill(2110001);
                                    boflevel = chra.getSkillLevel(blessoffairy);
                                    if (boflevel > 0) {
                                        this.dam_r *= (double)blessoffairy.getEffect(boflevel).getY() / 100.0;
                                        this.bossdam_r *= (double)blessoffairy.getEffect(boflevel).getY() / 100.0;
                                    }
                                    break;
                                case 221:
                                case 222:
                                    blessoffairy = SkillFactory.getSkill(2210001);
                                    boflevel = chra.getSkillLevel(blessoffairy);
                                    if (boflevel > 0) {
                                        this.dam_r *= (double)blessoffairy.getEffect(boflevel).getY() / 100.0;
                                        this.bossdam_r *= (double)blessoffairy.getEffect(boflevel).getY() / 100.0;
                                    }
                                    break;
                                case 312:
                                    blessoffairy = SkillFactory.getSkill(3120005);
                                    boflevel = chra.getSkillLevel(blessoffairy);
                                    if (boflevel > 0) {
                                        this.watk += blessoffairy.getEffect(boflevel).getX();
                                    }
                                    break;
                                case 322:
                                    blessoffairy = SkillFactory.getSkill(3220004);
                                    boflevel = chra.getSkillLevel(blessoffairy);
                                    if (boflevel > 0) {
                                        this.watk += blessoffairy.getEffect(boflevel).getX();
                                    }
                                    break;
                                case 1211:
                                case 1212:
                                    blessoffairy = SkillFactory.getSkill(12110001);
                                    boflevel = chra.getSkillLevel(blessoffairy);
                                    if (boflevel > 0) {
                                        this.dam_r *= (double)blessoffairy.getEffect(boflevel).getY() / 100.0;
                                        this.bossdam_r *= (double)blessoffairy.getEffect(boflevel).getY() / 100.0;
                                    }
                                    break;
                                case 2112:
                                    blessoffairy = SkillFactory.getSkill(21120001);
                                    boflevel = chra.getSkillLevel(blessoffairy);
                                    if (boflevel > 0) {
                                        this.watk += blessoffairy.getEffect(boflevel).getX();
                                    }
                            }

                            blessoffairy = SkillFactory.getSkill(GameConstants.getBofForJob(chra.getJob()));
                            boflevel = chra.getSkillLevel(blessoffairy);
                            if (boflevel > 0) {
                                this.watk += blessoffairy.getEffect(boflevel).getX();
                                this.magic += blessoffairy.getEffect(boflevel).getY();
                                this.accuracy += blessoffairy.getEffect(boflevel).getX();
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.EXPRATE);
                            if (buff != null) {
                                this.expBuff *= buff.doubleValue() / 100.0;
                                this.realExpBuff += buff.doubleValue();
                            }

                            if (chra.isBuffedValue(2382046)) {
                                this.realMesoBuff += 100.0;
                                this.mesoBuff *= 2.0;
                                this.realDropBuff += 200.0;
                                this.dropBuff *= 3.0;
                            } else if (chra.isBuffedValue(2382028)) {
                                this.realMesoBuff += 100.0;
                                this.mesoBuff *= 2.0;
                                this.realDropBuff += 200.0;
                                this.dropBuff *= 3.0;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.DROP_RATE);
                            if (buff != null) {
                                if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382028) {
                                    switch (chra.getMapId()) {
                                        case 100040101:
                                        case 100040102:
                                        case 100040103:
                                        case 100040104:
                                        case 107000401:
                                        case 107000402:
                                        case 107000403:
                                        case 191000000:
                                            this.realDropBuff += buff.doubleValue();
                                            this.dropBuff *= buff.doubleValue() / 100.0;
                                    }
                                } else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382028) {
                                    switch (chra.getMapId()) {
                                        case 222020100:
                                        case 222020200:
                                        case 222020300:
                                            this.realDropBuff += buff.doubleValue();
                                            this.dropBuff *= buff.doubleValue() / 100.0;
                                    }
                                } else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2022462) {
                                    this.realDropBuff += 50.0;
                                    this.dropBuff *= 1.5;
                                }  else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2022531) {
                                    this.realDropBuff += 100.0;
                                    this.dropBuff *= 2.0;
                                }  else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2022530) {
                                    this.realDropBuff += 100.0;
                                    this.dropBuff *= 2.0;
                                } else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382001) {
                                    this.realMesoBuff += 100.0;
                                    this.mesoBuff *= 2.0;
                                    this.realDropBuff += 100.0;
                                    this.dropBuff *= 3.0;
                                } else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382040) {
                                    this.realMesoBuff += 100.0;
                                    this.mesoBuff *= 2.0;
                                    this.realDropBuff += 100.0;
                                    this.dropBuff *= 3.0;
                                } else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2383003) {
                                    this.realMesoBuff += 100.0;
                                    this.mesoBuff *= 2.0;
                                    this.realDropBuff += 100.0;
                                    this.dropBuff *= 3.0;
                                } else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2383006) {
                                    this.realDropBuff += 100.0;
                                    this.dropBuff *= 4.0;
                                } else {
                                    this.realDropBuff += buff.doubleValue();
                                    this.dropBuff *= buff.doubleValue() / 100.0;
                                }
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.ACASH_RATE);
                            if (buff != null) {
                                this.realCashBuff += buff.doubleValue();
                                this.cashBuff *= buff.doubleValue() / 100.0;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MESO_RATE);
                            if (buff != null) {
                                if (chra.getBuffSource(MapleBuffStat.MESO_RATE) != 2382005 && chra.getBuffSource(MapleBuffStat.MESO_RATE) != 2382016) {
                                    if (chra.getBuffSource(MapleBuffStat.MESO_RATE) == 2022459) {
                                        this.realMesoBuff += 30.0;
                                        this.mesoBuff *= 1.3;
                                    } else if (chra.getBuffSource(MapleBuffStat.MESO_RATE) == 2022460) {
                                        this.realMesoBuff += 50.0;
                                        this.mesoBuff *= 1.5;
                                    } else {
                                        this.realMesoBuff += buff.doubleValue();
                                        this.mesoBuff *= buff.doubleValue() / 100.0;
                                    }
                                } else if (chra.getMapId() >= 221020000 && chra.getMapId() <= 221024400) {
                                    this.mesoBuff *= buff.doubleValue() / 100.0;
                                    this.realMesoBuff += buff.doubleValue();
                                }
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MESOUP);
                            if (buff != null) {
                                this.realMesoBuff += buff.doubleValue();
                                this.mesoBuff *= buff.doubleValue() / 100.0;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.ACC);
                            if (buff != null) {
                                this.accuracy += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.AVOID);
                            if (buff != null) {
                                this.avoid += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.WATK);
                            if (buff != null) {
                                this.watk += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MATK);
                            if (buff != null) {
                                this.magic += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.WDEF);
                            if (buff != null) {
                                this.wdef += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MDEF);
                            if (buff != null) {
                                this.mdef += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.SPEED);
                            if (buff != null) {
                                speed += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.JUMP);
                            if (buff != null) {
                                jump += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.DASH_SPEED);
                            if (buff != null) {
                                speed += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.DASH_JUMP);
                            if (buff != null) {
                                jump += buff;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.WIND_WALK);
                            if (buff != null) {
                                MapleStatEffect eff = chra.getStatForBuff(MapleBuffStat.WIND_WALK);
                                this.dam_r *= (double)eff.getDamage() / 100.0;
                                this.bossdam_r *= (double)eff.getDamage() / 100.0;
                            }

                            buff = chra.getBuffedSkill_Y(MapleBuffStat.OWL_SPIRIT);
                            if (buff != null) {
                                this.dam_r *= buff.doubleValue() / 100.0;
                                this.bossdam_r *= buff.doubleValue() / 100.0;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.BERSERK_FURY);
                            if (buff != null) {
                                this.dam_r *= 2.0;
                                this.bossdam_r *= 2.0;
                            }

                            ISkill bx = SkillFactory.getSkill(1320006);
                            if (chra.getSkillLevel(bx) > 0) {
                                this.dam_r *= (double)bx.getEffect(chra.getSkillLevel(bx)).getDamage() / 100.0;
                                this.bossdam_r *= (double)bx.getEffect(chra.getSkillLevel(bx)).getDamage() / 100.0;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.WK_CHARGE);
                            MapleStatEffect eff;
                            if (buff != null) {
                                eff = chra.getStatForBuff(MapleBuffStat.WK_CHARGE);
                                this.dam_r *= (double)eff.getDamage() / 100.0;
                                this.bossdam_r *= (double)eff.getDamage() / 100.0;
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
                            if (buff != null) {
                                eff = chra.getStatForBuff(MapleBuffStat.MONSTER_RIDING);
                                this.pickRate = eff.getProb();
                            }

                            buff = chra.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE);
                            if (buff != null) {
                                eff = chra.getStatForBuff(MapleBuffStat.LIGHTNING_CHARGE);
                                this.dam_r *= (double)eff.getDamage() / 100.0;
                                this.bossdam_r *= (double)eff.getDamage() / 100.0;
                            }

                            buff = chra.getBuffedSkill_X(MapleBuffStat.SHARP_EYES);
                            if (buff != null) {
                                added_sharpeye_rate += buff;
                            }

                            buff = chra.getBuffedSkill_Y(MapleBuffStat.SHARP_EYES);
                            if (buff != null) {
                                added_sharpeye_dmg += buff - 100;
                            }

                            if (speed > 140) {
                                speed = 140;
                            }

                            if (jump > 123) {
                                jump = 123;
                            }

                            this.speedMod = (float)speed / 100.0F;
                            this.jumpMod = (float)jump / 100.0F;
                            Integer mount = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
                            if (mount != null) {
                                this.jumpMod = 1.23F;
                                switch (mount) {
                                    case 1:
                                        this.speedMod = 1.5F;
                                        break;
                                    case 2:
                                        this.speedMod = 1.7F;
                                        break;
                                    case 3:
                                        this.speedMod = 1.8F;
                                        break;
                                    default:
                                        服务端输出信息.println_err("Unhandeled monster riding level, Speedmod = " + this.speedMod + "");
                                }
                            }

                            this.hands = this.localdex + this.localint_ + this.localluk;
                            this.localmaxhp = (short)Math.min(30000, Math.abs(Math.max(-30000, localmaxhp_)));
                            this.localmaxmp = (short)Math.min(30000, Math.abs(Math.max(-30000, localmaxmp_)));
                            this.CalcPassive_SharpEye(chra, added_sharpeye_rate, added_sharpeye_dmg);
                            this.CalcPassive_Mastery(chra);
                            this.CalcPassive_Range(chra);
                            if (first_login) {
                                chra.silentEnforceMaxHpMp();
                            } else {
                                chra.enforceMaxHpMp();
                            }

                            this.localmaxbasedamage = this.calculateMaxBaseDamage(this.magic, this.watk);
                            if (oldmaxhp != 0 && oldmaxhp != this.localmaxhp) {
                                chra.updatePartyMemberHP();
                            }

                            this.isRecalc = false;
                            this.canRefresh = true;
                            return;
                        }

                        entry = (Map.Entry)iter.next();
                        set = ii.getSetItem((Integer)entry.getKey());
                    } while(set == null);

                    for (final Entry<Integer, Integer> integerEntry : this.setHandling.entrySet()) {
                        final StructSetItem set2 = ii.getSetItem((int)Integer.valueOf(integerEntry.getKey()));
                        if (set2 != null) {
                            final Map<Integer, SetItem> itemz = set2.getItems();
                            for (final Entry<Integer, SetItem> ent : itemz.entrySet()) {
                                if ((int)Integer.valueOf(ent.getKey()) <= (int)Integer.valueOf(integerEntry.getValue())) {
                                    final SetItem se = (SetItem)ent.getValue();
                                    this.localstr += se.incSTR;
                                    this.localdex += se.incDEX;
                                    this.localint_ += se.incINT;
                                    this.localluk += se.incLUK;
                                    this.watk += se.incPAD;
                                    this.magic += se.incINT + se.incMAD;
                                    this.avoid += se.incAvoid;
                                    this.wdef += se.incPDD;
                                    this.mdef += se.incMDD;
                                    speed += se.incSpeed;
                                    this.accuracy += se.incACC;
                                    localmaxhp_ += se.incMHP;
                                    localmaxmp_ += se.incMMP;
                                }
                            }
                        }
                    }
                }
            }
    }

    //角色属性整合
    public void recalcLocalStats1(final boolean first_login) {
        MapleCharacter chra = (MapleCharacter)this.chr.get();
        if (chra == null) {
            return;
        }
        if (this.isRecalc) {
            return;
        }
        chra.刷新身上装备镶嵌汇总数据();
        chra.reloadPotentialMap();
        chra.loadPackageList();
        chra.solvePackageStats();



        this.isRecalc = true;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final int oldmaxhp = this.localmaxhp;
        int localmaxhp_ = this.getMaxHp();
        int localmaxmp_ = this.getMaxMp();
        this.localdex = this.getDex();
        this.localint_ = this.getInt();
        this.localstr = this.getStr();
        this.localluk = this.getLuk();
        this.limitStr = 0;
        this.limitDex = 0;
        this.limitLuk = 0;
        this.limitInt = 0;
        int speed = 100;
        int jump = 100;
        this.dotTime = 0;
        int percent_hp = 0;//Hp增加x%
        int percent_mp = 0;//Mp增加x%
        int percent_str = 0; //力量增加x%
        int percent_dex = 0;//敏捷增加x%
        int percent_int = 0;//智力增加x%
        int percent_luk = 0;//运气增加x%
        int percent_acc = 0; //命中增加x%
        int percent_atk = 0;//物理攻击力增加x%
        int percent_matk = 0;//魔法攻击力增加x%
        int percent_avoid = 0;
        int percent_wdef = 0;
        int percent_mdef = 0;
        int added_sharpeye_rate = 0;//添加暴击率
        int added_sharpeye_dmg = 0;//添加暴击伤害
        this.magic = this.localint_;
        this.watk = 0;
        this.wdef = 0;
        this.mdef = 0;
        this.accuracy = 0;
        this.avoid = 0;
        if (chra.getJob() == 500 || (chra.getJob() >= 520 && chra.getJob() <= 522)) {
            this.watk = 20;
        }
        else if (chra.getJob() == 400 || (chra.getJob() >= 410 && chra.getJob() <= 412) || (chra.getJob() >= 1400 && chra.getJob() <= 1412)) {
            this.watk = 30;
        }
//        suitSys = new Hashtable<>();
//        suitSys.putAll(Start.suitSystemsMap);

        this.dam_r = 100.0;//伤害
        this.bossdam_r = 100.0;//boss伤害
        this.realExpBuff = 100.0;
        this.realCashBuff = 100.0;
        this.realDropBuff = 100.0;
        this.realMesoBuff = 100.0;
        this.expBuff = 100.0;
        this.cashBuff = 100.0;
        this.dropBuff = 100.0;
        this.mesoBuff = 100.0;
        this.recoverHP = 0;
        this.recoverMP = 0;
        this.mpconReduce = 0;
        this.incMesoProp = 0;
        this.incRewardProp = 0;
        this.DAMreflect = 0;
        this.DAMreflect_rate = 0;
        this.hpRecover = 0;
        this.hpRecoverProp = 0;
        this.mpRecover = 0;
        this.mpRecoverProp = 0;
        this.mpRestore = 0;
        this.equippedWelcomeBackRing = false;
        this.equippedRing = false;
        this.equippedRing1 = false;
        this.equippedRing2 = false;
        this.equippedFairy = false;
        this.hasMeso = false;
        this.hasItem = false;
        this.hasPartyBonus = false;
        this.hasVac = false;
        this.hasClone = false;
        final boolean canEquipLevel = chra.getLevel() >= 120 && !GameConstants.isKOC((int)chra.getJob());
        this.equipmentBonusExp = 0;
        this.RecoveryUP = 0;
        this.dropMod = 1;
        this.dropm = 1.0;
        this.expMod = 1;
        this.expm = 1.0;
        this.cashMod = 1;
        this.精灵吊坠 = false;
        this.levelBonus = 0;
        this.incAllskill = 0;
        this.durabilityHandling.clear();
        this.equipLevelHandling.clear();
        this.setHandling.clear();
        this.element_fire = 100;
        this.element_ice = 100;
        this.element_light = 100;
        this.element_psn = 100;
        this.def = 100;
        this.defRange = 0;
        //装备属性合并
        for (final IItem item : chra.getInventory(MapleInventoryType.EQUIPPED)) {
            final IEquip equip = (IEquip)item;
            if (equip.getPosition() == -11 && GameConstants.isMagicWeapon(equip.getItemId())) {
                final Map<String, Integer> eqstat = MapleItemInformationProvider.getInstance().getEquipStats(equip.getItemId());
                this.element_fire = (int)Integer.valueOf(eqstat.get((Object)"incRMAF"));
                this.element_ice = (int)Integer.valueOf(eqstat.get((Object)"incRMAI"));
                this.element_light = (int)Integer.valueOf(eqstat.get((Object)"incRMAL"));
                this.element_psn = (int)Integer.valueOf(eqstat.get((Object)"incRMAS"));
                this.def = (int)Integer.valueOf(eqstat.get((Object)"elemDefault"));
            }
            this.accuracy += equip.getAcc();
            this.wdef += equip.getWdef();
            this.mdef += equip.getMdef();
            this.avoid += equip.getAvoid();
            localmaxhp_ += equip.getHp();
            localmaxmp_ += equip.getMp();
            this.localdex += equip.getDex();
            this.localint_ += equip.getInt();
            this.localstr += equip.getStr();
            this.localluk += equip.getLuk();
            this.magic += equip.getMatk() + equip.getInt();
            this.watk += equip.getWatk();
            speed += equip.getSpeed();
            jump += equip.getJump();


            switch (equip.getItemId()) {
                case 1122017: {
                    this.精灵吊坠 = true;
                    this.equippedFairy = true;
                    break;
                }
                case 1112427: {
                    added_sharpeye_rate += 5;
                    added_sharpeye_dmg += 20;
                    break;
                }
                case 1112428: {
                    added_sharpeye_rate += 10;
                    added_sharpeye_dmg += 10;
                    break;
                }
                case 1112429: {
                    added_sharpeye_rate += 5;
                    added_sharpeye_dmg += 20;
                    break;
                }
                case 1112127: {
                    this.equippedWelcomeBackRing = true;
                    break;
                }
                case 1114000: {
                    this.equippedRing = true;
                    break;
                }
                case 1114317:
                    this.equippedRing1 = true;
                    break;
                case 1114400:
                    this.equippedRing2 = true;
                    break;
                case 1122086:
                case 1122207:
                case 1122215: {
                    this.equippedFairy = true;
                    break;
                }
                case 1812000: {
                    this.hasMeso = true;
                    break;
                }
                case 1812001: {
                    this.hasItem = true;
                    break;
                }
                default: {
                    for (final int eb_bonus : GameConstants.Equipments_Bonus) {
                        if (equip.getItemId() == eb_bonus) {
                            this.equipmentBonusExp += GameConstants.Equipment_Bonus_EXP(eb_bonus);
                            break;
                        }
                    }
                    break;
                }
            }
            percent_hp += equip.getHpR();
            percent_mp += equip.getMpR();
            final int set = ii.getSetItemID(equip.getItemId());
            if (set > 0) {
                int value = 1;
                if (this.setHandling.get((Object)Integer.valueOf(set)) != null) {
                    value += (int)Integer.valueOf(this.setHandling.get((Object)Integer.valueOf(set)));
                }
                this.setHandling.put(Integer.valueOf(set), Integer.valueOf(value));
            }

            //潜能
            if (equip.getState() > 1) {
                final int[] array;
                final int[] potentials = array = new int[] { equip.getPotential1(), equip.getPotential2(), equip.getPotential3() };
                for (final int i : array) {
                    if (i > 0) {
                        final StructPotentialItem pot = (StructPotentialItem)ii.getPotentialInfo(i).get(ii.getReqLevel(equip.getItemId()) / 10);
                        if (pot != null) {
                            this.localstr += pot.incSTR;
                            this.localdex += pot.incDEX;
                            this.localint_ += pot.incINT;
                            this.localluk += pot.incLUK;
                            this.localmaxhp += pot.incMHP;
                            this.localmaxmp += pot.incMMP;
                            this.watk += pot.incPAD;
                            this.magic += pot.incINT + pot.incMAD;
                            speed += pot.incSpeed;
                            jump += pot.incJump;
                            this.accuracy += pot.incACC;
                            this.avoid += pot.incEVA;
                            this.wdef += pot.incPDD;
                            this.mdef += pot.incMDD;
                            this.incAllskill += pot.incAllskill;
                            percent_hp += pot.incMHPr;
                            percent_mp += pot.incMMPr;
                            percent_str += pot.incSTRr;
                            percent_dex += pot.incDEXr;
                            percent_int += pot.incINTr;
                            percent_luk += pot.incLUKr;
                            percent_acc += pot.incACCr;
                            percent_atk += pot.incPADr;
                            percent_matk += pot.incMADr;
                            added_sharpeye_rate += pot.incCr;
                            added_sharpeye_dmg += pot.incCr;
                            if (!pot.boss) {
                                this.dam_r = Math.max((double)pot.incDAMr, this.dam_r);
                            }
                            else {
                                this.bossdam_r = Math.max((double)pot.incDAMr, this.bossdam_r);
                            }
                            this.recoverHP += pot.RecoveryHP;
                            this.recoverMP += pot.RecoveryMP;
                            this.RecoveryUP += pot.RecoveryUP;
                            if (pot.HP > 0) {
                                this.hpRecover += pot.HP;
                                this.hpRecoverProp += pot.prop;
                            }
                            if (pot.MP > 0) {
                                this.mpRecover += pot.MP;
                                this.mpRecoverProp += pot.prop;
                            }
                            this.mpconReduce += pot.mpconReduce;
                            this.incMesoProp += pot.incMesoProp;
                            this.incRewardProp += pot.incRewardProp;
                            if (pot.DAMreflect > 0) {
                                this.DAMreflect += pot.DAMreflect;
                                this.DAMreflect_rate += pot.prop;
                            }
                            this.mpRestore += pot.mpRestore;
                            if (!first_login && pot.skillID > 0) {
                                chra.changeSkillLevel_Skip(SkillFactory.getSkill(this.getSkillByJob((int)pot.skillID, (int)chra.getJob())), (byte)1, (byte)1);
                            }
                        }
                    }
                }
            }

            if (equip.getDurability() > 0) {
                this.durabilityHandling.add((Equip)equip);
            }
            if (canEquipLevel && GameConstants.getMaxLevel(equip.getItemId()) > 0) {
                if (GameConstants.getStatFromWeapon(equip.getItemId()) == null) {
                    if (equip.getEquipLevel() > GameConstants.getMaxLevel(equip.getItemId())) {
                        continue;
                    }
                }
                else if (equip.getEquipLevel() >= GameConstants.getMaxLevel(equip.getItemId())) {
                    continue;
                }
                this.equipLevelHandling.add((Equip)equip);
            }

        }

        int package_num;
        if (chra.F().get(FumoSkill.FM("强身健体")) != null) {
            package_num = (Integer)chra.F().get(FumoSkill.FM("强身健体"));
            if (package_num > 0) {
                this.localdex += package_num;
                this.localint_ += package_num;
                this.localstr += package_num;
                this.localluk += package_num;
                this.limitStr = (short)(this.limitStr + package_num);
                this.limitInt = (short)(this.limitInt + package_num);
                this.limitDex = (short)(this.limitDex + package_num);
                this.limitLuk = (short)(this.limitLuk + package_num);
                if (first_login && System.currentTimeMillis() - this.lastMessageTime > 2000L) {
                    chra.dropMessage(5, "镶嵌效果：【强身健体】 全属性 + " + package_num + " 点");
                    this.lastMessageTime = System.currentTimeMillis();
                }
            }
        }

        int p_dex;
        int p_int;
        int p_luk;
        int package_luk;
        int percentStr;
        int eb_bonus;
        int percentInt;
        int i;
        int percentTotal;
        if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
            package_num = chra.getPotential(1);
            p_dex = chra.getPotential(2);
            p_int = chra.getPotential(3);
            p_luk = chra.getPotential(4);
            package_luk = chra.getPotential(9);
            percentStr = chra.getPotential(5);
            eb_bonus = chra.getPotential(6);
            percentInt = chra.getPotential(7);
            i = chra.getPotential(8);
            percentTotal = chra.getPotential(10);
            package_num += package_luk;
            p_dex += package_luk;
            p_int += package_luk;
            p_luk += package_luk;
            if (percentStr > 0) {
                package_num += chra.getStat().getStr() * percentStr / 100;
            }

            if (eb_bonus > 0) {
                p_dex += chra.getStat().getDex() * eb_bonus / 100;
            }

            if (percentInt > 0) {
                p_int += chra.getStat().getInt() * percentInt / 100;
            }

            if (i > 0) {
                p_luk += chra.getStat().getLuk() * i / 100;
            }

            if (percentTotal > 0) {
                package_num += chra.getStat().getStr() * percentTotal / 100;
                p_dex += chra.getStat().getDex() * percentTotal / 100;
                p_int += chra.getStat().getInt() * percentTotal / 100;
                p_luk += chra.getStat().getLuk() * percentTotal / 100;
            }

            if (package_num > 0 || p_dex > 0 || p_int > 0 || p_luk > 0) {
                String text = "【潜能加成】：";
                if (package_num > 0) {
                    this.localstr += package_num;
                    this.limitStr = (short)(this.limitStr + package_num);
                    text = text + "力量+" + package_num + " ";
                }

                if (p_dex > 0) {
                    this.localdex += p_dex;
                    this.limitDex = (short)(this.limitDex + p_dex);
                    text = text + "敏捷+" + p_dex + " ";
                }

                if (p_int > 0) {
                    this.localint_ += p_int;
                    this.limitInt = (short)(this.limitInt + p_int);
                    text = text + "智力+" + p_int + " ";
                }

                if (p_luk > 0) {
                    this.localluk += p_luk;
                    this.limitLuk = (short)(this.limitLuk + p_luk);
                    text = text + "运气+" + p_luk + " ";
                }

                if (first_login && System.currentTimeMillis() - this.lastMessageTime3 > 2000L) {
                    chra.dropMessage(5, text);
                    this.lastMessageTime3 = System.currentTimeMillis();
                }
            }
        }

        package_num = chra.getPackageList() == null ? 0 : chra.getPackageList().size();
        int package_str = chra.package_str;
        int package_dex = chra.package_dex;
        int package_int = chra.package_int;
         package_luk = chra.package_luk;
        int package_total = chra.package_all_ap;
        int package_percentStr = chra.package_str_percent;
        int package_percentDex = chra.package_dex_percent;
        int package_percentInt = chra.package_int_percent;
        int package_percentLuk = chra.package_luk_percent;
        int package_percentTotal = chra.package_all_ap_percent;
        p_dex = package_str + package_total;
        p_int = package_dex + package_total;
        p_luk = package_int + package_total;
        package_luk = package_luk + package_total;
        if (package_percentStr > 0) {
            p_dex += chra.getStat().getStr() * package_percentStr / 100;
        }

        if (package_percentDex > 0) {
            p_int += chra.getStat().getDex() * package_percentDex / 100;
        }

        if (package_percentInt > 0) {
            p_luk += chra.getStat().getInt() * package_percentInt / 100;
        }

        if (package_percentLuk > 0) {
            package_luk += chra.getStat().getLuk() * package_percentLuk / 100;
        }

        if (package_percentTotal > 0) {
            p_dex += chra.getStat().getStr() * package_percentTotal / 100;
            p_int += chra.getStat().getDex() * package_percentTotal / 100;
            p_luk += chra.getStat().getInt() * package_percentTotal / 100;
            package_luk += chra.getStat().getLuk() * package_percentTotal / 100;
        }

        if (p_dex > 0 || p_int > 0 || p_luk > 0 || package_luk > 0) {
            String text = "【套装加成】：穿戴了" + package_num + "组套装，";
            if (p_dex > 0) {
                this.localstr += p_dex;
                this.limitStr = (short)(this.limitStr + p_dex);
                text = text + "力量+" + p_dex + " ";
            }

            if (p_int > 0) {
                this.localdex += p_int;
                this.limitDex = (short)(this.limitDex + p_int);
                text = text + "敏捷+" + p_int + " ";
            }

            if (p_luk > 0) {
                this.localint_ += p_luk;
                this.limitInt = (short)(this.limitInt + p_luk);
                text = text + "智力+" + p_luk + " ";
            }

            if (package_luk > 0) {
                this.localluk += package_luk;
                this.limitLuk = (short)(this.limitLuk + package_luk);
                text = text + "运气+" + package_luk + " ";
            }

            if (first_login && System.currentTimeMillis() - this.lastMessageTime5 > 2000L) {
                chra.dropMessage(5, text);
                this.lastMessageTime5 = System.currentTimeMillis();
            }
        }


        for (final Entry<Integer, Integer> entry : this.setHandling.entrySet()) {
            final StructSetItem set2 = ii.getSetItem((int)Integer.valueOf(entry.getKey()));
            if (set2 != null) {
                final Map<Integer, SetItem> itemz = set2.getItems();
                for (final Entry<Integer, SetItem> ent : itemz.entrySet()) {
                    if ((int)Integer.valueOf(ent.getKey()) <= (int)Integer.valueOf(entry.getValue())) {
                        final SetItem se = (SetItem)ent.getValue();
                        this.localstr += se.incSTR;
                        this.localdex += se.incDEX;
                        this.localint_ += se.incINT;
                        this.localluk += se.incLUK;
                        this.watk += se.incPAD;
                        this.magic += se.incINT + se.incMAD;
                        this.avoid += se.incAvoid;
                        this.wdef += se.incPDD;
                        this.mdef += se.incMDD;
                        speed += se.incSpeed;
                        this.accuracy += se.incACC;
                        localmaxhp_ += se.incMHP;
                        localmaxmp_ += se.incMMP;
                    }
                }
            }
        }
        final int hour = Calendar.getInstance().get(11);
        final int weekDay = Calendar.getInstance().get(7);
        if (chra.getMarriageId() > 0) {
            this.expm = 1.1;
            this.dropm = 1.1;
        }

        this.expMod = 1;
        this.dropMod = 1;
        //背包物品经验卡
        for (final IItem item2 : chra.getInventory(MapleInventoryType.CASH)) {
            if (this.expMod < 3 && (item2.getItemId() == 5211060 || item2.getItemId() == 5211050 || item2.getItemId() == 5211051 || item2.getItemId() == 5211052 || item2.getItemId() == 5211053 || item2.getItemId() == 5211054)) {
                this.expMod = 3;
            }
            else if (this.expMod < 2 && (item2.getItemId() == 5211061 || item2.getItemId() == 5211000 || item2.getItemId() == 5211001 || item2.getItemId() == 5211002 || item2.getItemId() == 5211003 || item2.getItemId() == 5211046 || item2.getItemId() == 5211047 || item2.getItemId() == 5211048 || item2.getItemId() == 5211049)) {
                this.expMod = 2;
            }
            else if (this.expMod < 2 && (item2.getItemId() == 5210002 || item2.getItemId() == 5210003) && ((hour >= 6 && hour <= 18 && weekDay >= 2 && weekDay <= 6) || weekDay == 1 || weekDay == 7)) {
                this.expMod = 2;
            }
            else if (this.expMod < 2 && (item2.getItemId() == 5210004 || item2.getItemId() == 5210005 || item2.getItemId() == 521000) && (((hour >= 18 || hour <= 6) && weekDay >= 2 && weekDay <= 6) || weekDay == 1 || weekDay == 7)) {
                this.expMod = 2;
            }
            else if (this.expMod < 2 && (item2.getItemId() == 5210000 || item2.getItemId() == 5210001) && ((hour >= 10 && hour <= 22 && weekDay >= 2 && weekDay <= 6) || weekDay == 1 || weekDay == 7)) {
                this.expMod = 2;
            }
            if (this.dropMod == 1) {
                if (item2.getItemId() == 5360015) {//双倍爆率卡
                    this.dropMod = 2;
                }
                else if (item2.getItemId() == 5360000 && hour >= 0 && hour <= 6) {
                    this.dropMod = 2;
                }
                else if (item2.getItemId() == 5360001 && hour >= 6 && hour <= 12) {
                    this.dropMod = 2;
                }
                else if (item2.getItemId() == 5360002 && hour >= 12 && hour <= 18) {
                    this.dropMod = 2;
                }
                else if (item2.getItemId() == 5360003 && hour >= 18 && hour <= 24) {
                    this.dropMod = 2;
                }
                else if (item2.getItemId() == 5360017) {
                    this.dropMod = 5; //5倍卡
                }
                else if (item2.getItemId() == 5360018) {
                    this.dropMod = 8; //8倍卡
                }
            }
            if (item2.getItemId() == 5650000) {
                this.hasPartyBonus = true;
            }
            else if (item2.getItemId() == 5590001) {
                this.levelBonus = 10;
            }
            else {
                if (this.levelBonus != 0 || item2.getItemId() != 5590000) {
                    continue;
                }
                this.levelBonus = 5;
            }
        }
        if (chra.getHiredChannel() > 0) {
            this.expMod_H = 10;
        }
        //梯级经验设置
        if (chra.getLevel() >= 1 && chra.getLevel() <= 120) {
            this.expMod *= ServerConfig.BeiShu1;
        }
        else if (chra.getLevel() > 120 && chra.getLevel() <= 200) {
            this.expMod *= ServerConfig.BeiShu2;
        }
        else if (chra.getLevel() > 200 && chra.getLevel() <= 250) {
            this.expMod *= ServerConfig.BeiShu3;
        }
        for (final IItem item2 : chra.getInventory(MapleInventoryType.ETC)) {
            switch (item2.getItemId()) {
                case 5062000: {
                    this.hasVac = true;
                    continue;
                }
                case 4030004: {
                    this.hasClone = true;
                    continue;
                }
                case 4030005: {
                    this.cashMod = 2;
                    continue;
                }
                case 4101000:
                case 4101002: {
                    this.equippedFairy = true;
                    chra.setFairyExp((byte)30);
                    continue;
                }
            }
        }

        this.magic += chra.getSkillLevel(SkillFactory.getSkill(22000000));
        this.localstr = (int)((float)this.localstr + (float)(percent_str * this.localstr) / 100.0f);
        this.localdex = (int)((float)this.localdex + (float)(percent_dex * this.localdex) / 100.0f);
        final int before_ = this.localint_;
        this.localint_ = (int)((float)this.localint_ + (float)(percent_int * this.localint_) / 100.0f);
        this.magic += this.localint_ - before_;
        this.localluk = (int)((float)this.localluk + (float)(percent_luk * this.localluk) / 100.0f);
        this.accuracy = (int)((float)this.accuracy + (float)(percent_acc * this.accuracy) / 100.0f);
        this.watk = (int)((float)this.watk + (float)(percent_atk * this.watk) / 100.0f);
        this.magic = (int)((float)this.magic + (float)(percent_matk * this.magic) / 100.0f);
        this.avoid = (int)((float)this.avoid + (float)(percent_avoid * this.avoid) / 100.0F);
        this.wdef = (int)((float)this.wdef + (float)(percent_wdef * this.wdef) / 100.0F);
        this.mdef = (int)((float)this.mdef + (float)(percent_mdef * this.mdef) / 100.0F);
        localmaxhp_ = (int)((float)localmaxhp_ + (float)(percent_hp * localmaxhp_) / 100.0f);
        localmaxmp_ = (int)((float)localmaxmp_ + (float)(percent_mp * localmaxmp_) / 100.0f);
        this.magic = Math.min(this.magic, 1999);

        if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
            this.pWatk = chra.getPotential(11);
            this.pMatk = chra.getPotential(12);
            this.pWatkPercent = chra.getPotential(13);
            this.pMatkPercent = chra.getPotential(14);
            this.pWdef = chra.getPotential(15);
            this.pWdefPercent = chra.getPotential(16);
            this.pMdef = chra.getPotential(17);
            this.pMdefPercent = chra.getPotential(18);
            this.pAcc = chra.getPotential(19);
            this.pAccPercent = chra.getPotential(20);
            this.pAvoid = chra.getPotential(21);
            this.pAvoidPercent = chra.getPotential(22);
            this.pMaxHp = chra.getPotential(23);
            this.pMaxHpPercent = chra.getPotential(24);
            this.pMaxMp = chra.getPotential(25);
            this.pMaxMpPercent = chra.getPotential(26);
            this.pSpeed = chra.getPotential(27);
            this.pJump = chra.getPotential(28);
            if (this.pWatk > 0 || this.pMatk > 0 || this.pWatkPercent > 0 || this.pMatkPercent > 0 || this.pWdef > 0 || this.pWdefPercent > 0 || this.pMdef > 0 || this.pMdefPercent > 0 || this.pAcc > 0 || this.pAccPercent > 0 || this.pAvoid > 0 || this.pAvoidPercent > 0 || this.pMaxHp > 0 || this.pMaxHpPercent > 0 || this.pMaxMp > 0 || this.pMaxMpPercent > 0 || this.pSpeed > 0 || this.pJump > 0) {
                if (this.pWatkPercent > 0) {
                    this.pWatk += this.watk * this.pWatkPercent / 100;
                }

                if (this.pMatkPercent > 0) {
                    this.pMatk += this.magic * this.pMatkPercent / 100;
                }

                if (this.pWdefPercent > 0) {
                    this.pWdef += this.wdef * this.pWdefPercent / 100;
                }

                if (this.pMdefPercent > 0) {
                    this.pMdef += this.mdef * this.pMdefPercent / 100;
                }

                if (this.pAccPercent > 0) {
                    this.pAcc += this.accuracy * this.pAccPercent / 100;
                }

                if (this.pAvoidPercent > 0) {
                    this.pAvoid += this.avoid * this.pAvoidPercent / 100;
                }

                if (this.pMaxHp > 0) {
                    this.pMaxHpPercent = (int)((double)this.pMaxHpPercent + Math.ceil((double)((float)(this.pMaxHp * 100) / (float)localmaxhp_)));
                    this.pMaxHp += this.pMaxHpPercent * localmaxhp_ / 100;
                }

                if (this.pMaxMp > 0) {
                    this.pMaxMpPercent = (int)((double)this.pMaxMpPercent + Math.ceil((double)((float)(this.pMaxMp * 100) / (float)localmaxmp_)));
                    this.pMaxMp += this.pMaxMpPercent * localmaxmp_ / 100;
                }

                if (first_login && System.currentTimeMillis() - this.lastMessageTime4 > 2000L) {
                    StringBuilder text = new StringBuilder();
                    text.append("【潜能加成】：");
                    if (this.pWatk > 0) {
                        text.append("攻击力+");
                        text.append(this.pWatk);
                        text.append(" ");
                    }

                    if (this.pMatk > 0) {
                        text.append("魔法力+");
                        text.append(this.pMatk);
                        text.append(" ");
                    }

                    if (this.pWdef > 0) {
                        text.append("物防+");
                        text.append(this.pWdef);
                        text.append(" ");
                    }

                    if (this.pMdef > 0) {
                        text.append("魔防+");
                        text.append(this.pMdef);
                        text.append(" ");
                    }

                    if (this.pAcc > 0) {
                        text.append("命中+");
                        text.append(this.pAcc);
                        text.append(" ");
                    }

                    if (this.pAvoid > 0) {
                        text.append("回避+");
                        text.append(this.pAvoid);
                        text.append(" ");
                    }

                    if (this.pSpeed > 0) {
                        text.append("速度+");
                        text.append(this.pSpeed);
                        text.append(" ");
                    }

                    if (this.pJump > 0) {
                        text.append("跳跃+");
                        text.append(this.pJump);
                        text.append(" ");
                    }

                    if (this.pMaxHp > 0) {
                        text.append("maxHp+");
                        text.append(this.pMaxHpPercent);
                        text.append("% ");
                    }

                    if (this.pMaxMp > 0) {
                        text.append("maxMp+");
                        text.append(this.pMaxMpPercent);
                        text.append("% ");
                    }

                    chra.dropMessage(5, text.toString());
                    this.lastMessageTime4 = System.currentTimeMillis();
                }
            }
        }

        int package_Watk = chra.package_watk;
        int package_Matk = chra.package_matk;
        int package_WatkPercent = chra.package_watk_percent;
        int package_MatkPercent = chra.package_matk_percent;
        int package_Wdef = chra.package_wdef;
        int package_WdefPercent = chra.package_wdef_percent;
        int package_Mdef = chra.package_mdef;
        int package_MdefPercent = chra.package_mdef_percent;
        int package_Acc = chra.package_acc;
        int package_AccPercent = chra.package_acc_percent;
        int package_Avoid = chra.package_avoid;
        int package_AvoidPercent = chra.package_avoid_percent;
        int package_MaxHp = chra.package_maxhp;
        int package_MaxHpPercent = chra.package_maxhp_percent;
        int package_MaxMp = chra.package_maxmp;
        int package_MaxMpPercent = chra.package_maxmp_percent;
        int package_Speed = chra.package_speed;
        int package_Jump = chra.package_jump;
        if (package_Watk > 0 || package_Matk > 0 || package_WatkPercent > 0 || package_MatkPercent > 0 || package_Wdef > 0 || package_WdefPercent > 0 || package_Mdef > 0 || package_MdefPercent > 0 || package_Acc > 0 || package_AccPercent > 0 || package_Avoid > 0 || package_AvoidPercent > 0 || package_MaxHp > 0 || package_MaxHpPercent > 0 || package_MaxMp > 0 || package_MaxMpPercent > 0 || package_Speed > 0 || package_Jump > 0) {
            if (package_WatkPercent > 0) {
                package_Watk += this.watk * package_WatkPercent / 100;
            }

            if (package_MatkPercent > 0) {
                package_Matk += this.magic * package_MatkPercent / 100;
            }

            if (package_WdefPercent > 0) {
                package_Wdef += this.wdef * package_WdefPercent / 100;
            }

            if (package_MdefPercent > 0) {
                package_Mdef += this.mdef * package_MdefPercent / 100;
            }

            if (package_AccPercent > 0) {
                package_Acc += this.accuracy * package_AccPercent / 100;
            }

            if (package_AvoidPercent > 0) {
                package_Avoid += this.avoid * package_AvoidPercent / 100;
            }

            if (package_Watk > 0) {
                this.pWatk += package_Watk;
            }

            if (package_Matk > 0) {
                this.pMatk += package_Matk;
            }

            if (package_Wdef > 0) {
                this.pWdef += package_Wdef;
            }

            if (package_Mdef > 0) {
                this.pMdef += package_Mdef;
            }

            if (package_Acc > 0) {
                this.pAcc += package_Acc;
            }

            if (package_Avoid > 0) {
                this.pAvoid += package_Avoid;
            }

            if (package_MaxHp > 0) {
                package_MaxHpPercent += package_MaxHp * 100 / localmaxhp_;
                package_MaxHp += package_MaxHpPercent * localmaxhp_ / 100;
                this.pMaxHp += package_MaxHp;
            }

            if (package_MaxMp > 0) {
                package_MaxMpPercent += package_MaxMp * 100 / localmaxmp_;
                package_MaxMp += package_MaxMpPercent * localmaxmp_ / 100;
                this.pMaxMp += package_MaxMp;
            }

            if (first_login && System.currentTimeMillis() - this.lastMessageTime6 > 2000L) {
                StringBuilder text = new StringBuilder();
                text.append("【套装加成】：穿戴了" + package_num + "组套装，");
                if (package_Watk > 0) {
                    text.append("攻击力+");
                    text.append(package_Watk);
                    text.append(" ");
                }

                if (package_Matk > 0) {
                    text.append("魔法力+");
                    text.append(package_Matk);
                    text.append(" ");
                }

                if (package_Wdef > 0) {
                    text.append("物防+");
                    text.append(package_Wdef);
                    text.append(" ");
                }

                if (package_Mdef > 0) {
                    text.append("魔防+");
                    text.append(package_Mdef);
                    text.append(" ");
                }

                if (package_Acc > 0) {
                    text.append("命中+");
                    text.append(package_Acc);
                    text.append(" ");
                }

                if (package_Avoid > 0) {
                    text.append("回避+");
                    text.append(package_Avoid);
                    text.append(" ");
                }

                if (package_Speed > 0) {
                    text.append("速度+");
                    text.append(package_Speed);
                    text.append(" ");
                }

                if (package_Jump > 0) {
                    text.append("跳跃+");
                    text.append(package_Jump);
                    text.append(" ");
                }

                if (package_MaxHp > 0) {
                    text.append("maxHp+");
                    text.append(package_MaxHpPercent);
                    text.append("% ");
                }

                if (package_MaxMp > 0) {
                    text.append("maxMp+");
                    text.append(package_MaxMpPercent);
                    text.append("% ");
                }

                chra.dropMessage(5, text.toString());
                this.lastMessageTime6 = System.currentTimeMillis();
            }
        }
        if (this.equippedRing1) {
            this.expm += 0.1;
        }

        if (this.equippedRing2) {
            this.expm += 0.2;
        }
        Integer buff = chra.getBuffedValue(MapleBuffStat.MAPLE_WARRIOR);
        if (buff != null) {
            final double d = (double)buff / 100.0;
            this.localstr = (int)((double)this.localstr + d * (double)this.str);
            this.localdex = (int)((double)this.localdex + d * (double)this.dex);
            this.localluk = (int)((double)this.localluk + d * (double)this.luk);
            final int before = this.localint_;
            this.localint_ = (int)((double)this.localint_ + d * (double)this.int_);
            this.magic += this.localint_ - before;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ECHO_OF_HERO);
        if (buff != null) {
            final double d = (double)buff / 100.0;
            this.watk += (int)((double)this.watk * d);//物攻?
            this.magic += (int)((double)this.magic * d);//魔攻?
        }
        buff = chra.getBuffedValue(MapleBuffStat.ARAN_COMBO);
        if (buff != null) {
            this.watk += (int)buff / 10;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAXHP);
        if (buff != null) {
            localmaxhp_ = (int)((double)localmaxhp_ + (double)buff / 100.0 * (double)localmaxhp_);
        }
        buff = chra.getBuffedValue(MapleBuffStat.MAXMP);
        if (buff != null) {
            localmaxmp_ = (int)((double)localmaxmp_ + (double)buff / 100.0 * (double)localmaxmp_);
        }

        switch (chra.getJob()) {
            case 322: {
                final ISkill expert = SkillFactory.getSkill(3220004);
                final int boostLevel = chra.getSkillLevel(expert);
                if (boostLevel > 0) {
                    this.watk += expert.getEffect(boostLevel).getX();
                    break;
                }
                break;
            }
            case 312: {
                final ISkill expert = SkillFactory.getSkill(3120005);
                final int boostLevel = chra.getSkillLevel(expert);
                if (boostLevel > 0) {
                    this.watk += expert.getEffect(boostLevel).getX();
                    break;
                }
                break;
            }
            case 211:
            case 212: {
                final ISkill amp = SkillFactory.getSkill(2110001);
                final int level = chra.getSkillLevel(amp);
                if (level > 0) {
                    this.dam_r *= (double)amp.getEffect(level).getY() / 100.0;
                    this.bossdam_r *= (double)amp.getEffect(level).getY() / 100.0;
                    break;
                }
                break;
            }
            case 221:
            case 222: {
                final ISkill amp = SkillFactory.getSkill(2210001);
                final int level = chra.getSkillLevel(amp);
                if (level > 0) {
                    this.dam_r *= (double)amp.getEffect(level).getY() / 100.0;
                    this.bossdam_r *= (double)amp.getEffect(level).getY() / 100.0;
                    break;
                }
                break;
            }
            case 1211:
            case 1212: {
                final ISkill amp = SkillFactory.getSkill(12110001);
                final int level = chra.getSkillLevel(amp);
                if (level > 0) {
                    this.dam_r *= (double)amp.getEffect(level).getY() / 100.0;
                    this.bossdam_r *= (double)amp.getEffect(level).getY() / 100.0;
                    break;
                }
                break;
            }
            case 2112: {
                final ISkill expert = SkillFactory.getSkill(21120001);
                final int boostLevel = chra.getSkillLevel(expert);
                if (boostLevel > 0) {
                    this.watk += expert.getEffect(boostLevel).getX();
                    break;
                }
                break;
            }
        }
        final ISkill blessoffairy = SkillFactory.getSkill(GameConstants.getBofForJob((int)chra.getJob()));
        final int boflevel = chra.getSkillLevel(blessoffairy);
        if (boflevel > 0) {
            this.watk += blessoffairy.getEffect(boflevel).getX();
            this.magic += blessoffairy.getEffect(boflevel).getY();
            this.accuracy += blessoffairy.getEffect(boflevel).getX();
        }
        buff = chra.getBuffedValue(MapleBuffStat.EXPRATE);
        if (buff != null) {
            this.expBuff *= (double)buff / 100.0;
            this.realExpBuff += (double)buff;
        }
        if (chra.isBuffedValue(2382046)) {
            this.realMesoBuff += 100.0;
            this.mesoBuff *= 2.0;
            this.realDropBuff += 200.0;
            this.dropBuff *= 3.0;
        } else if (chra.isBuffedValue(2382028)) {
            this.realMesoBuff += 100.0;
            this.mesoBuff *= 2.0;
            this.realDropBuff += 200.0;
            this.dropBuff *= 3.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.DROP_RATE);
        if (buff != null) {
              if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2022462) {
                this.realDropBuff += 50.0;
                this.dropBuff *= 1.5;
            }
              else
            if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382028) {
                switch (chra.getMapId()) {
                    case 100040101:
                    case 100040102:
                    case 100040103:
                    case 100040104:
                    case 107000401:
                    case 107000402:
                    case 107000403:
                    case 191000000: {
                        this.realDropBuff += (double)buff;
                        this.dropBuff *= (double)buff / 100.0;
                        break;
                    }
                }
            }
            else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382028) {
                switch (chra.getMapId()) {
                    case 222020100:
                    case 222020200:
                    case 222020300: {
                        this.realDropBuff += (double)buff;
                        this.dropBuff *= (double)buff / 100.0;
                        break;
                    }
                }
            }
            else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382001) {
                this.realMesoBuff += 100.0;
                this.mesoBuff *= 2.0;
                this.realDropBuff += 200.0;
                this.dropBuff *= 3.0;
            }
            else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2382040) {
                this.realMesoBuff += 100.0;
                this.mesoBuff *= 2.0;
                this.realDropBuff += 200.0;
                this.dropBuff *= 3.0;
            }
            else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2383003) {
                this.realMesoBuff += 100.0;
                this.mesoBuff *= 2.0;
                this.realDropBuff += 200.0;
                this.dropBuff *= 3.0;
            }
            else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2383006) {
                this.realDropBuff += 300.0;
                this.dropBuff *= 4.0;
            }
            else if (chra.getBuffSource(MapleBuffStat.DROP_RATE) == 2383010) {
                this.realDropBuff += 300.0;
                this.dropBuff *= 4.0;
            }
            else {
                if (buff>200){
                    buff=200;
                }
                this.realDropBuff += (double)buff;
                this.dropBuff *= (double)buff / 100.0;
            }
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACASH_RATE);
        if (buff != null) {
            this.realCashBuff += (double)buff;
            this.cashBuff *= (double)buff / 100.0;
        }

        buff = chra.getBuffedValue(MapleBuffStat.MESO_RATE);
        if (buff != null) {
            if (chra.getBuffSource(MapleBuffStat.MESO_RATE) == 2382005 || chra.getBuffSource(MapleBuffStat.MESO_RATE) == 2382016) {
                if (chra.getMapId() >= 221020000 && chra.getMapId() <= 221024400) {
                    this.mesoBuff *= (double)buff / 100.0;
                    this.realMesoBuff += (double)buff;
                }
            }
            else if (chra.getBuffSource(MapleBuffStat.MESO_RATE) == 2022459) {
                this.realMesoBuff += 30.0;
                this.mesoBuff *= 1.3;
            }
            else if (chra.getBuffSource(MapleBuffStat.MESO_RATE) == 2022460) {
                this.realMesoBuff += 50.0;
                this.mesoBuff *= 1.5;
            }
            else {
                this.realMesoBuff += (double)buff;
                this.mesoBuff *= (double)buff / 100.0;
            }
        }
        buff = chra.getBuffedValue(MapleBuffStat.MESOUP);
        if (buff != null) {
            this.realMesoBuff += (double)buff;
            this.mesoBuff *= (double)buff / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.ACC);
        if (buff != null) {
            this.accuracy += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.AVOID);
        if (buff != null) {
            this.avoid += buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.WATK);
        if (buff != null) {
            this.watk += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MATK);
        if (buff != null) {
            this.magic += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.WDEF);
        if (buff != null) {
            this.wdef += buff;
        }

        buff = chra.getBuffedValue(MapleBuffStat.MDEF);
        if (buff != null) {
            this.mdef += buff;
        }

        buff = chra.getBuffedValue(MapleBuffStat.SPEED);
        if (buff != null) {
            speed += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.JUMP);
        if (buff != null) {
            jump += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_SPEED);
        if (buff != null) {
            speed += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.DASH_JUMP);
        if (buff != null) {
            jump += (int)buff;
        }
        buff = chra.getBuffedValue(MapleBuffStat.WIND_WALK);
        if (buff != null) {
            final MapleStatEffect eff = chra.getStatForBuff(MapleBuffStat.WIND_WALK);
            this.dam_r *= (double)eff.getDamage() / 100.0;
            this.bossdam_r *= (double)eff.getDamage() / 100.0;
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.OWL_SPIRIT);
        if (buff != null) {
            this.dam_r *= (double)buff / 100.0;
            this.bossdam_r *= (double)buff / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.BERSERK_FURY);
        if (buff != null) {
            this.dam_r *= 2.0;
            this.bossdam_r *= 2.0;
        }
        final ISkill bx = SkillFactory.getSkill(1320006);
        if (chra.getSkillLevel(bx) > 0) {
            this.dam_r *= (double)bx.getEffect((int)chra.getSkillLevel(bx)).getDamage() / 100.0;
            this.bossdam_r *= (double)bx.getEffect((int)chra.getSkillLevel(bx)).getDamage() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.WK_CHARGE);
        if (buff != null) {
            final MapleStatEffect eff2 = chra.getStatForBuff(MapleBuffStat.WK_CHARGE);
            this.dam_r *= (double)eff2.getDamage() / 100.0;
            this.bossdam_r *= (double)eff2.getDamage() / 100.0;
        }
        buff = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (buff != null) {
            final MapleStatEffect eff2 = chra.getStatForBuff(MapleBuffStat.MONSTER_RIDING);
            this.pickRate = eff2.getProb();
        }
        buff = chra.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE);
        if (buff != null) {
            final MapleStatEffect eff2 = chra.getStatForBuff(MapleBuffStat.LIGHTNING_CHARGE);
            this.dam_r *= (double)eff2.getDamage() / 100.0;
            this.bossdam_r *= (double)eff2.getDamage() / 100.0;
        }
        buff = chra.getBuffedSkill_X(MapleBuffStat.SHARP_EYES);
        if (buff != null) {
            added_sharpeye_rate += (int)buff;
        }
        buff = chra.getBuffedSkill_Y(MapleBuffStat.SHARP_EYES);
        if (buff != null) {
            added_sharpeye_dmg += (int)buff - 100;
        }
        if (speed > 140) {
            speed = 140;
        }
        if (jump > 123) {
            jump = 123;
        }
        this.speedMod = (float)speed / 100.0f;
        this.jumpMod = (float)jump / 100.0f;
        final Integer mount = chra.getBuffedValue(MapleBuffStat.MONSTER_RIDING);
        if (mount != null) {
            this.jumpMod = 1.23f;
            switch ((int)mount) {
                case 1: {
                    this.speedMod = 1.5f;
                    break;
                }
                case 2: {
                    this.speedMod = 1.7f;
                    break;
                }
                case 3: {
                    this.speedMod = 1.8f;
                    break;
                }
                default: {
                    System.err.println("Unhandeled monster riding level, Speedmod = " + this.speedMod + "");
                    break;
                }
            }
        }
        this.hands = this.localdex + this.localint_ + this.localluk;
        this.localmaxhp = (short)Math.min(30000, Math.abs(Math.max(-30000, localmaxhp_)));
        this.localmaxmp = (short)Math.min(30000, Math.abs(Math.max(-30000, localmaxmp_)));
        this.CalcPassive_SharpEye(chra, added_sharpeye_rate, added_sharpeye_dmg);
        this.CalcPassive_Mastery(chra);
        this.CalcPassive_Range(chra);
        if (first_login) {
            chra.silentEnforceMaxHpMp();
        }
        else {
            chra.enforceMaxHpMp();
        }
        this.localmaxbasedamage = this.calculateMaxBaseDamage(this.magic, this.watk);
        if (oldmaxhp != 0 && oldmaxhp != this.localmaxhp) {
            chra.updatePartyMemberHP();
        }
        this.isRecalc = false;
        if(this.str>=32766){
            this.str=32766;
        }
        if(this.dex>=32766){
            this.dex=32766;
        }
        if(this.luk>=32766){
            this.luk=32766;
        }
        if(this.int_>=32766){
            this.int_=32766;
        }
        if(this.limitStr>=32766){
            this.limitStr=32766;
        }
        if(this.limitDex>=32766){
            this.limitDex=32766;
        }
        if(this.limitLuk>=32766){
            this.limitLuk=32766;
        }
        if(this.limitInt>=32766){
            this.limitInt=32766;
        }
    }

    public void 计算最大伤害(MapleCharacter chra ){
        if(chra==null){
            return;
        }
        switch (chra.getJob()){
            //战士
            case 110:
            case 111:
            case 112:
            case 120:
            case 121:
            case 122:
            case 130:
            case 131:
            case 132:
                damage = (long) ((this.localstr*(LtMS.ConfigValuesMap.get("战士力量系数")/100.00)+this.localdex*0.1+this.localluk*0.1+this.localint_*0.1)*(this.watk*(LtMS.ConfigValuesMap.get("战士物理系数")/100.00)))+1L;
                break;
            case 2000:
            case 2100:
            case 2110:
            case 2111:
            case 2112:
                damage = (long) ((this.localstr*(LtMS.ConfigValuesMap.get("战神力量系数")/100.00)+this.localdex*0.1+this.localluk*0.1+this.localint_*0.1)*(this.watk*(LtMS.ConfigValuesMap.get("战神物理系数")/100.00)))+1L;
                break;
            //法师
            case 200:
            case 210:
            case 211:
            case 212:
            case 220:
            case 221:
            case 222:
            case 230:
            case 231:
            case 232:
                damage = (long) ((this.localstr*0.1+this.localdex*0.1+this.localluk*0.1+this.localint_*(LtMS.ConfigValuesMap.get("法师智力系数")/100.00))*(this.magic*(LtMS.ConfigValuesMap.get("法师魔力系数")/100.00)))+1L;
                break;
            //射手
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:

                damage = (long) ((this.localstr*0.1+this.localdex*(LtMS.ConfigValuesMap.get("射手敏捷系数")/100.00)+this.localluk*0.1+this.localint_*0.1)*(this.watk*(LtMS.ConfigValuesMap.get("射手物理系数")/100.00)))+1L;
                break;
            //飞侠
            case 400:
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422:

                damage = (long) ((this.localstr*0.1+this.localdex*0.1+this.localluk*(LtMS.ConfigValuesMap.get("飞侠运气系数")/100.00)+this.localint_*0.1)*(this.watk*(LtMS.ConfigValuesMap.get("飞侠物理系数")/100.00)))+1L;
                break;
            //海盗
            case 500:
            case 510:
            case 511:
            case 512:
                damage = (long) ((this.localstr*0.1+this.localdex*(LtMS.ConfigValuesMap.get("拳手敏捷系数")/100.00)+this.localluk*0.1+this.localint_*0.1)*(this.watk*(LtMS.ConfigValuesMap.get("海盗物理系数")/100.00)))+1L;
                break;
            case 520:
            case 521:
            case 522:
                damage = (long) ((this.localstr*0.1+this.localdex*0.1+this.localluk*(LtMS.ConfigValuesMap.get("船长运气系数")/100.00)+this.localint_*0.1)*(this.watk*(LtMS.ConfigValuesMap.get("海盗物理系数")/100.00)))+1L;
                break;
            default:
                damage = (long) ((this.localstr*0.3+this.localdex*0.3+this.localluk*0.3+this.localint_*0.3)*(this.watk*0.1))+1L;
                break;

        }
    }

    public boolean checkEquipLevels(MapleCharacter chr, final int gain) {
        boolean changed = false;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        final List<Equip> all = new ArrayList<Equip>((Collection<? extends Equip>)this.equipLevelHandling);
        for (Equip eq : all) {
            final int lvlz = eq.getEquipLevel();
            eq.setItemEXP(Math.min(eq.getItemEXP() + gain, Integer.MAX_VALUE));
            if (eq.getEquipLevel() > lvlz) {
                for (int i = eq.getEquipLevel() - lvlz; i > 0; --i) {
                    final Map<Integer, Map<String, Integer>> inc = ii.getEquipIncrements(eq.getItemId());
                    if (inc != null && inc.containsKey((Object)Integer.valueOf(lvlz + i))) {
                        eq = ii.levelUpEquip(eq, (Map<String, Integer>)inc.get((Object)Integer.valueOf(lvlz + i)));
                    }
                    if (GameConstants.getStatFromWeapon(eq.getItemId()) == null) {
                        final Map<Integer, List<Integer>> ins = ii.getEquipSkills(eq.getItemId());
                        if (ins != null && ins.containsKey((Object)Integer.valueOf(lvlz + i))) {
                            for (final Integer z : (List<Integer>)ins.get((Object)Integer.valueOf(lvlz + i))) {
                                if (Math.random() < 0.1) {
                                    final ISkill skil = SkillFactory.getSkill((int)z);
                                    if (skil == null || !skil.canBeLearnedBy((int)chr.getJob()) || chr.getSkillLevel(skil) >= chr.getMasterLevel(skil)) {
                                        continue;
                                    }
                                    chr.changeSkillLevel(skil, (byte)(chr.getSkillLevel(skil) + 1), chr.getMasterLevel(skil));
                                }
                            }
                        }
                    }
                }
                changed = true;
            }
            chr.forceReAddItem_Flag(eq.copy(), MapleInventoryType.EQUIPPED);
        }
        if (changed) {
            chr.equipChanged();
            chr.getClient().sendPacket(MaplePacketCreator.showItemLevelupEffect());
            chr.getMap().broadcastMessage(chr, MaplePacketCreator.showForeignItemLevelupEffect(chr.getId()), false);
        }
        return changed;
    }
    
    public boolean checkEquipDurabilitys(MapleCharacter chr, final int gain) {
        for (final Equip item : this.durabilityHandling) {
            item.setDurability(item.getDurability() + gain);
            if (item.getDurability() < 0) {
                item.setDurability(0);
            }
        }
        final List<Equip> all = new ArrayList<Equip>((Collection<? extends Equip>)this.durabilityHandling);
        for (final Equip eqq : all) {
            if (eqq.getDurability() == 0) {
                if (chr.getInventory(MapleInventoryType.EQUIP).isFull()) {
                    chr.getClient().sendPacket(MaplePacketCreator.getInventoryFull());
                    chr.getClient().sendPacket(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                this.durabilityHandling.remove((Object)eqq);
                final short pos = chr.getInventory(MapleInventoryType.EQUIP).getNextFreeSlot();
                MapleInventoryManipulator.unequip(chr.getClient(), eqq.getPosition(), pos);
                chr.getClient().sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(1, (IItem)eqq, pos)));
            }
            else {
                chr.forceReAddItem(eqq.copy(), MapleInventoryType.EQUIPPED);
            }
        }
        return true;
    }
    
    private void CalcPassive_Mastery(final MapleCharacter player) {
        if (player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-11)) == null) {
            this.passive_mastery = 0;
            return;
        }
        int skil = 0;
        switch (GameConstants.getWeaponType(player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-11)).getItemId())) {
            case 弓: {
                skil = 3100000;
                break;
            }
            case 拳套: {
                skil = 4100000;
                break;
            }
            case 双刀:
            case 短剑: {
                skil = ((player.getJob() >= 430 && player.getJob() <= 434) ? 4300000 : 4200000);
                break;
            }
            case 弩: {
                skil = 3200000;
                break;
            }
            case 单手斧:
            case 双手斧: {
                skil = 1100001;
                break;
            }
            case 单手剑:
            case 双手剑: {
                skil = (GameConstants.isKOC((int)player.getJob()) ? 11100000 : ((player.getJob() > 112) ? 1200000 : 1100000));
                break;
            }
            case 单手棍:
            case 双手棍: {
                skil = 1200001;
                break;
            }
            case 枪: {
                skil = (GameConstants.isAran((int)player.getJob()) ? 21100000 : 1300001);
                break;
            }
            case 矛: {
                skil = 1300000;
                break;
            }
            case 指虎: {
                skil = (GameConstants.isKOC((int)player.getJob()) ? 15100001 : 5100001);
                break;
            }
            case 火枪: {
                skil = 5200000;
                break;
            }
            case 短杖: {
                skil = 32100006;
                break;
            }
            default: {
                this.passive_mastery = 0;
                return;
            }
        }
        if (player.getSkillLevel(skil) <= 0) {
            this.passive_mastery = 0;
            return;
        }
        this.passive_mastery = (byte)(player.getSkillLevel(skil) / 2 + player.getSkillLevel(skil) % 2);
    }
    
    private void CalcPassive_SharpEye(final MapleCharacter player, final int added_sharpeye_rate, final int added_sharpeye_dmg) {
        switch (player.getJob()) {
            case 410:
            case 411:
            case 412: {
                final ISkill critSkill = SkillFactory.getSkill(4100001);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 1410:
            case 1411:
            case 1412: {
                final ISkill critSkill = SkillFactory.getSkill(14100001);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 511:
            case 512: {
                final ISkill critSkill = SkillFactory.getSkill(5110000);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 1511:
            case 1512: {
                final ISkill critSkill = SkillFactory.getSkill(15110000);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 2111:
            case 2112: {
                final ISkill critSkill = SkillFactory.getSkill(21110000);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getX() * critSkill.getEffect(critlevel).getDamage() + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getX() * critSkill.getEffect(critlevel).getY() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322: {
                final ISkill critSkill = SkillFactory.getSkill(3000001);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 1300:
            case 1310:
            case 1311:
            case 1312: {
                final ISkill critSkill = SkillFactory.getSkill(13000000);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
            case 2214:
            case 2215:
            case 2216:
            case 2217:
            case 2218: {
                final ISkill critSkill = SkillFactory.getSkill(22140000);
                final int critlevel = player.getSkillLevel(critSkill);
                if (critlevel > 0) {
                    this.passive_sharpeye_percent = (short)(critSkill.getEffect(critlevel).getDamage() - 100 + added_sharpeye_dmg);
                    this.passive_sharpeye_rate = (byte)(critSkill.getEffect(critlevel).getProb() + added_sharpeye_rate);
                    return;
                }
                break;
            }
        }
        this.passive_sharpeye_percent = (short)added_sharpeye_dmg;
        this.passive_sharpeye_rate = (byte)added_sharpeye_rate;
    }
    
    private void CalcPassive_Range(MapleCharacter chra) {
        switch (chra.getJob()) {
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322: {
                this.defRange = 100;
                final ISkill bx = SkillFactory.getSkill(3000002);
                final int bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    this.defRange += bx.getEffect(bof).getRange();
                    break;
                }
                break;
            }
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422: {
                this.defRange = 100;
                final ISkill bx = SkillFactory.getSkill(4000001);
                final int bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    this.defRange += bx.getEffect(bof).getRange();
                    break;
                }
                break;
            }
            case 520:
            case 521:
            case 522: {
                this.defRange = 100;
                break;
            }
            case 1300:
            case 1310:
            case 1311:
            case 1312: {
                this.defRange = 100;
                final ISkill bx = SkillFactory.getSkill(13000001);
                final int bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    this.defRange += bx.getEffect(bof).getRange();
                    break;
                }
                break;
            }
            case 1400:
            case 1410:
            case 1411:
            case 1412: {
                this.defRange = 100;
                final ISkill bx = SkillFactory.getSkill(14000001);
                final int bof = chra.getSkillLevel(bx);
                if (bof > 0) {
                    this.defRange += bx.getEffect(bof).getRange();
                    break;
                }
                break;
            }
            case 2100:
            case 2110:
            case 2111:
            case 2112: {
                this.defRange = 80;
                break;
            }
        }
    }
    
    public final short passive_sharpeye_percent() {
        return this.passive_sharpeye_percent;
    }
    
    public final byte passive_sharpeye_rate() {
        return this.passive_sharpeye_rate;
    }
    
    public final byte passive_mastery() {
        return this.passive_mastery;
    }
    
    public final float calculateMaxBaseDamage(final int matk, final int watk) {
        MapleCharacter chra = (MapleCharacter)this.chr.get();
        if (chra == null) {
            return 0.0f;
        }
        float maxbasedamage;
        if (watk == 0) {
            maxbasedamage = 1.0f;
        }
        else {
            final IItem weapon_item = chra.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-11));
            final int job = chra.getJob();
            final MapleWeaponType weapon = (weapon_item == null) ? MapleWeaponType.没有武器 : GameConstants.getWeaponType(weapon_item.getItemId());
            final boolean magican = (job >= 200 && job <= 232) || (job >= 1200 && job <= 1212);
            int mainstat = 0;
            int secondarystat = 0;
            switch (weapon) {
                case 矛: {
                    mainstat = (int)((double)this.localstr * 1.25);
                    secondarystat = (int)((double)this.localdex * 1.25);
                    break;
                }
                case 弓:
                case 弩: {
                    mainstat = this.localdex * 2;
                    secondarystat = this.localstr * 2;
                    break;
                }
                case 拳套:
                case 双刀:
                case 短剑: {
                    if ((job >= 400 && job <= 434) || (job >= 1400 && job <= 1412)) {
                        mainstat = this.localluk;
                        secondarystat = this.localdex + this.localstr;
                        break;
                    }
                    mainstat = this.localstr;
                    secondarystat = this.localdex;
                    break;
                }
                case 指虎: {
                    mainstat = this.localstr;
                    secondarystat = this.localdex;
                    break;
                }
                case 火枪: {
                    mainstat = this.localdex;
                    secondarystat = this.localstr;
                    break;
                }
                case 没有武器: {
                    if ((job >= 500 && job <= 522) || (job >= 1500 && job <= 1512)) {
                        mainstat = this.localstr;
                        secondarystat = this.localdex;
                        break;
                    }
                    mainstat = 0;
                    secondarystat = 0;
                    break;
                }
                default: {
                    if (magican) {
                        mainstat = this.localint_;
                        secondarystat = this.localluk;
                        break;
                    }
                    mainstat = this.localstr;
                    secondarystat = this.localdex;
                    break;
                }
            }
            maxbasedamage = (weapon.getMaxDamageMultiplier() * (float)mainstat + (float)secondarystat) * (float)(magican ? matk : watk) / 100.0f;
        }
        return maxbasedamage;
    }
    
    public final float getHealHP() {
        int shouldHealHP = 10;
        Skill bx = (Skill)SkillFactory.getSkill(1000000);
        int bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            final MapleStatEffect eff = bx.getEffect(bof);
            shouldHealHP += eff.getHp();
        }
        bx = (Skill)SkillFactory.getSkill(1320008);
        bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            final MapleStatEffect eff = bx.getEffect(bof);
            shouldHealHP += eff.getHp();
        }
        bx = (Skill)SkillFactory.getSkill(4100002);
        bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            final MapleStatEffect eff = bx.getEffect(bof);
            shouldHealHP += eff.getHp();
        }
        bx = (Skill)SkillFactory.getSkill(4200001);
        bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            final MapleStatEffect eff = bx.getEffect(bof);
            shouldHealHP += eff.getHp();
        }
        return (float)shouldHealHP;
    }
    
    public final float getHealMP() {
        int shouldHealMP = 3;
        Skill bx = (Skill)SkillFactory.getSkill(2000000);
        int bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            shouldHealMP += bof * 5;
        }
        bx = (Skill)SkillFactory.getSkill(4100002);
        bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            final MapleStatEffect eff = bx.getEffect(bof);
            shouldHealMP += eff.getMp();
        }
        bx = (Skill)SkillFactory.getSkill(4200001);
        bof = ((MapleCharacter)this.chr.get()).getSkillLevel((ISkill)bx);
        if (bof > 0) {
            final MapleStatEffect eff = bx.getEffect(bof);
            shouldHealMP += eff.getMp();
        }
        return (float)shouldHealMP;
    }
    
    public void relocHeal() {
        MapleCharacter chra = (MapleCharacter)this.chr.get();
        if (chra == null) {
            return;
        }
        final int playerjob = chra.getJob();
        this.shouldHealHP = (float)(10 + this.recoverHP);
        this.shouldHealMP = (float)(3 + this.mpRestore + this.recoverMP);
        if (GameConstants.isJobFamily(200, playerjob)) {
            this.shouldHealMP += (float)chra.getSkillLevel(SkillFactory.getSkill(2000000)) / 10.0f * (float)chra.getLevel();
        }
        else if (GameConstants.isJobFamily(111, playerjob)) {
            final ISkill effect = SkillFactory.getSkill(1110000);
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealMP += (float)effect.getEffect(lvl).getMp();
            }
        }
        else if (GameConstants.isJobFamily(121, playerjob)) {
            final ISkill effect = SkillFactory.getSkill(1210000);
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealMP += (float)effect.getEffect(lvl).getMp();
            }
        }
        else if (GameConstants.isJobFamily(1111, playerjob)) {
            final ISkill effect = SkillFactory.getSkill(11110000);
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealMP += (float)effect.getEffect(lvl).getMp();
            }
        }
        else if (GameConstants.isJobFamily(410, playerjob)) {
            final ISkill effect = SkillFactory.getSkill(4100002);
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealHP += (float)effect.getEffect(lvl).getHp();
                this.shouldHealMP += (float)effect.getEffect(lvl).getMp();
            }
        }
        else if (GameConstants.isJobFamily(420, playerjob)) {
            final ISkill effect = SkillFactory.getSkill(4200001);
            final int lvl = chra.getSkillLevel(effect);
            if (lvl > 0) {
                this.shouldHealHP += (float)effect.getEffect(lvl).getHp();
                this.shouldHealMP += (float)effect.getEffect(lvl).getMp();
            }
        }
        if (chra.isGM()) {
            this.shouldHealHP += 1000.0f;
            this.shouldHealMP += 1000.0f;
        }
        if (chra.getChair() != 0) {
            this.shouldHealHP += 99.0f;
            this.shouldHealMP += 99.0f;
        }
        else {
            final float recvRate = chra.getMap().getRecoveryRate();
            this.shouldHealHP *= recvRate;
            this.shouldHealMP *= recvRate;
        }
        this.shouldHealHP *= 2.0f;
        this.shouldHealMP *= 2.0f;
    }

    //封装四维属性
    public void connectData(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort((int)this.str);
        mplew.writeShort((int)this.dex);
        mplew.writeShort((int)this.int_);
        mplew.writeShort((int)this.luk);
        mplew.writeShort((int)this.hp);
        mplew.writeShort((int)this.maxhp);
        mplew.writeShort((int)this.mp);
        mplew.writeShort((int)this.maxmp);
    }
    
    public final int getSkillByJob(final int skillID, final int job) {
        if (GameConstants.isKOC(job)) {
            return skillID + 10000000;
        }
        if (GameConstants.isAran(job)) {
            return skillID + 20000000;
        }
        return skillID;
    }
    
    public int getExpModH() {
        return this.expMod_H;
    }

    public static boolean 自定义套装1(int a) {
        套装系统完善版 TZXT = new 套装系统完善版();
        String items2 = TZXT.get套装1();
        if (items2.contains("*" + a + "*")) {
            return true;
        }
        return false;
    }

    public static boolean 自定义套装2(int a) {
        套装系统完善版 TZXT = new 套装系统完善版();
        String items2 = TZXT.get套装2();
        if (items2.contains("*" + a + "*")) {
            return true;
        }
        return false;
    }

    public static boolean 自定义套装3(int a) {
        套装系统完善版 TZXT = new 套装系统完善版();
        String items2 = TZXT.get套装3();
        if (items2.contains("*" + a + "*")) {
            return true;
        }
        return false;
    }

    public static boolean 自定义套装4(int a) {
        套装系统完善版 TZXT = new 套装系统完善版();
        String items2 = TZXT.get套装4();
        if (items2.contains("*" + a + "*")) {
            return true;
        }
        return false;
    }

    public static boolean 自定义套装5(int a) {
        套装系统完善版 TZXT = new 套装系统完善版();
        String items2 = TZXT.get套装5();
        if (items2.contains("*" + a + "*")) {
            return true;
        }
        return false;
    }

    public static boolean 自定义套装6(int a) {
        套装系统完善版 TZXT = new 套装系统完善版();
        String items2 = TZXT.get套装6();
        if (items2.contains("*" + a + "*")) {
            return true;
        }
        return false;
    }

    public short getInt_() {
        return int_;
    }

    public void setInt_(short int_) {
        this.int_ = int_;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public short getMaxhp() {
        return maxhp;
    }

    public void setMaxhp(short maxhp) {
        this.maxhp = maxhp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public short getMaxmp() {
        return maxmp;
    }

    public void setMaxmp(short maxmp) {
        this.maxmp = maxmp;
    }

    public short getPassive_sharpeye_percent() {
        return passive_sharpeye_percent;
    }

    public void setPassive_sharpeye_percent(short passive_sharpeye_percent) {
        this.passive_sharpeye_percent = passive_sharpeye_percent;
    }

    public short getLocalmaxhp() {
        return localmaxhp;
    }

    public void setLocalmaxhp(short localmaxhp) {
        this.localmaxhp = localmaxhp;
    }

    public short getLocalmaxmp() {
        return localmaxmp;
    }

    public void setLocalmaxmp(short localmaxmp) {
        this.localmaxmp = localmaxmp;
    }

    public byte getPassive_mastery() {
        return passive_mastery;
    }

    public void setPassive_mastery(byte passive_mastery) {
        this.passive_mastery = passive_mastery;
    }

    public byte getPassive_sharpeye_rate() {
        return passive_sharpeye_rate;
    }

    public void setPassive_sharpeye_rate(byte passive_sharpeye_rate) {
        this.passive_sharpeye_rate = passive_sharpeye_rate;
    }

    public int getLocalstr() {
        return localstr;
    }

    public void setLocalstr(int localstr) {
        this.localstr = localstr;
    }

    public int getLocaldex() {
        return localdex;
    }

    public void setLocaldex(int localdex) {
        this.localdex = localdex;
    }

    public int getLocalluk() {
        return localluk;
    }

    public void setLocalluk(int localluk) {
        this.localluk = localluk;
    }

    public int getLocalint_() {
        return localint_;
    }

    public void setLocalint_(int localint_) {
        this.localint_ = localint_;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getWatk() {
        return watk;
    }

    public void setWatk(int watk) {
        this.watk = watk;
    }

    public void setHands(int hands) {
        this.hands = hands;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public boolean isEquippedWelcomeBackRing() {
        return equippedWelcomeBackRing;
    }

    public void setEquippedWelcomeBackRing(boolean equippedWelcomeBackRing) {
        this.equippedWelcomeBackRing = equippedWelcomeBackRing;
    }

    public boolean isEquippedFairy() {
        return equippedFairy;
    }

    public void setEquippedFairy(boolean equippedFairy) {
        this.equippedFairy = equippedFairy;
    }

    public boolean isHasMeso() {
        return hasMeso;
    }

    public void setHasMeso(boolean hasMeso) {
        this.hasMeso = hasMeso;
    }

    public boolean isHasItem() {
        return hasItem;
    }

    public void setHasItem(boolean hasItem) {
        this.hasItem = hasItem;
    }

    public boolean isHasVac() {
        return hasVac;
    }

    public void setHasVac(boolean hasVac) {
        this.hasVac = hasVac;
    }

    public boolean isHasClone() {
        return hasClone;
    }

    public void setHasClone(boolean hasClone) {
        this.hasClone = hasClone;
    }

    public boolean isHasPartyBonus() {
        return hasPartyBonus;
    }

    public void setHasPartyBonus(boolean hasPartyBonus) {
        this.hasPartyBonus = hasPartyBonus;
    }

    public boolean isBerserk() {
        return Berserk;
    }

    public void setBerserk(boolean berserk) {
        Berserk = berserk;
    }

    public boolean isRecalc() {
        return isRecalc;
    }

    public void setRecalc(boolean recalc) {
        isRecalc = recalc;
    }

    public boolean isEquippedRing() {
        return equippedRing;
    }

    public void setEquippedRing(boolean equippedRing) {
        this.equippedRing = equippedRing;
    }

    public int getEquipmentBonusExp() {
        return equipmentBonusExp;
    }

    public void setEquipmentBonusExp(int equipmentBonusExp) {
        this.equipmentBonusExp = equipmentBonusExp;
    }

    public double getExpMod() {
        return expMod;
    }

    public void setExpMod(int expMod) {
        this.expMod = expMod;
    }

    public int getDropMod() {
        return dropMod;
    }

    public void setDropMod(int dropMod) {
        this.dropMod = dropMod;
    }

    public int getCashMod() {
        return cashMod;
    }

    public void setCashMod(int cashMod) {
        this.cashMod = cashMod;
    }

    public int getLevelBonus() {
        return levelBonus;
    }

    public void setLevelBonus(int levelBonus) {
        this.levelBonus = levelBonus;
    }

    public int getExpMod_H() {
        return expMod_H;
    }

    public void setExpMod_H(int expMod_H) {
        this.expMod_H = expMod_H;
    }

    public double getExpBuff() {
        return expBuff;
    }

    public void setExpBuff(double expBuff) {
        this.expBuff = expBuff;
    }

    public double getDropBuff() {
        return dropBuff;
    }

    public void setDropBuff(double dropBuff) {
        this.dropBuff = dropBuff;
    }

    public double getMesoBuff() {
        return mesoBuff;
    }

    public void setMesoBuff(double mesoBuff) {
        this.mesoBuff = mesoBuff;
    }

    public double getCashBuff() {
        return cashBuff;
    }

    public void setCashBuff(double cashBuff) {
        this.cashBuff = cashBuff;
    }

    public double getRealExpBuff() {
        return realExpBuff;
    }

    public void setRealExpBuff(double realExpBuff) {
        this.realExpBuff = realExpBuff;
    }

    public double getRealDropBuff() {
        return realDropBuff;
    }

    public void setRealDropBuff(double realDropBuff) {
        this.realDropBuff = realDropBuff;
    }

    public double getRealMesoBuff() {
        return realMesoBuff;
    }

    public void setRealMesoBuff(double realMesoBuff) {
        this.realMesoBuff = realMesoBuff;
    }

    public double getRealCashBuff() {
        return realCashBuff;
    }

    public void setRealCashBuff(double realCashBuff) {
        this.realCashBuff = realCashBuff;
    }

    public double getDam_r() {
        return dam_r;
    }

    public void setDam_r(double dam_r) {
        this.dam_r = dam_r;
    }

    public double getBossdam_r() {
        return bossdam_r;
    }

    public void setBossdam_r(double bossdam_r) {
        this.bossdam_r = bossdam_r;
    }

    public double getDropm() {
        return dropm;
    }

    public void setDropm(double dropm) {
        this.dropm = dropm;
    }

    public double getExpm() {
        return expm;
    }

    public void setExpm(double expm) {
        this.expm = expm;
    }

    public int getItemExpm() {
        return itemExpm;
    }

    public void setItemExpm(int itemExpm) {
        this.itemExpm = itemExpm;
    }

    public int getItemDropm() {
        return itemDropm;
    }

    public void setItemDropm(int itemDropm) {
        this.itemDropm = itemDropm;
    }

    public int getRecoverHP() {
        return recoverHP;
    }

    public void setRecoverHP(int recoverHP) {
        this.recoverHP = recoverHP;
    }

    public int getRecoverMP() {
        return recoverMP;
    }

    public void setRecoverMP(int recoverMP) {
        this.recoverMP = recoverMP;
    }

    public int getMpconReduce() {
        return mpconReduce;
    }

    public void setMpconReduce(int mpconReduce) {
        this.mpconReduce = mpconReduce;
    }

    public int getIncMesoProp() {
        return incMesoProp;
    }

    public void setIncMesoProp(int incMesoProp) {
        this.incMesoProp = incMesoProp;
    }

    public int getIncRewardProp() {
        return incRewardProp;
    }

    public void setIncRewardProp(int incRewardProp) {
        this.incRewardProp = incRewardProp;
    }

    public int getDAMreflect() {
        return DAMreflect;
    }

    public void setDAMreflect(int DAMreflect) {
        this.DAMreflect = DAMreflect;
    }

    public int getDAMreflect_rate() {
        return DAMreflect_rate;
    }

    public void setDAMreflect_rate(int DAMreflect_rate) {
        this.DAMreflect_rate = DAMreflect_rate;
    }

    public int getMpRestore() {
        return mpRestore;
    }

    public void setMpRestore(int mpRestore) {
        this.mpRestore = mpRestore;
    }

    public int getHpRecover() {
        return hpRecover;
    }

    public void setHpRecover(int hpRecover) {
        this.hpRecover = hpRecover;
    }

    public int getHpRecoverProp() {
        return hpRecoverProp;
    }

    public void setHpRecoverProp(int hpRecoverProp) {
        this.hpRecoverProp = hpRecoverProp;
    }

    public int getMpRecover() {
        return mpRecover;
    }

    public void setMpRecover(int mpRecover) {
        this.mpRecover = mpRecover;
    }

    public int getMpRecoverProp() {
        return mpRecoverProp;
    }

    public void setMpRecoverProp(int mpRecoverProp) {
        this.mpRecoverProp = mpRecoverProp;
    }

    public int getRecoveryUP() {
        return RecoveryUP;
    }

    public void setRecoveryUP(int recoveryUP) {
        RecoveryUP = recoveryUP;
    }

    public int getIncAllskill() {
        return incAllskill;
    }

    public void setIncAllskill(int incAllskill) {
        this.incAllskill = incAllskill;
    }

    public void setSpeedMod(float speedMod) {
        this.speedMod = speedMod;
    }

    public void setJumpMod(float jumpMod) {
        this.jumpMod = jumpMod;
    }

    public float getLocalmaxbasedamage() {
        return localmaxbasedamage;
    }

    public void setLocalmaxbasedamage(float localmaxbasedamage) {
        this.localmaxbasedamage = localmaxbasedamage;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
    }

    public int getElement_ice() {
        return element_ice;
    }

    public void setElement_ice(int element_ice) {
        this.element_ice = element_ice;
    }

    public int getElement_fire() {
        return element_fire;
    }

    public void setElement_fire(int element_fire) {
        this.element_fire = element_fire;
    }

    public int getElement_light() {
        return element_light;
    }

    public void setElement_light(int element_light) {
        this.element_light = element_light;
    }

    public int getElement_psn() {
        return element_psn;
    }

    public void setElement_psn(int element_psn) {
        this.element_psn = element_psn;
    }

    public static short getMaxStr() {
        return maxStr;
    }

    public static void setMaxStr(short maxStr) {
        PlayerStats.maxStr = maxStr;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public void setLock(ReentrantLock lock) {
        this.lock = lock;
    }

    public short getPickRate() {
        return pickRate;
    }

    public void setPickRate(short pickRate) {
        this.pickRate = pickRate;
    }

    public int getDefRange() {
        return defRange;
    }

    public void setDefRange(int defRange) {
        this.defRange = defRange;
    }

    public int getDotTime() {
        return dotTime;
    }

    public void setDotTime(int dotTime) {
        this.dotTime = dotTime;
    }

    public boolean is精灵吊坠() {
        return 精灵吊坠;
    }

    public void set精灵吊坠(boolean 精灵吊坠) {
        this.精灵吊坠 = 精灵吊坠;
    }

    public int getTZ1() {
        return TZ1;
    }

    public void setTZ1(int TZ1) {
        this.TZ1 = TZ1;
    }

    public int getTZ2() {
        return TZ2;
    }

    public void setTZ2(int TZ2) {
        this.TZ2 = TZ2;
    }

    public int getTZ3() {
        return TZ3;
    }

    public void setTZ3(int TZ3) {
        this.TZ3 = TZ3;
    }

    public int getTZ4() {
        return TZ4;
    }

    public void setTZ4(int TZ4) {
        this.TZ4 = TZ4;
    }

    public int getTZ5() {
        return TZ5;
    }

    public void setTZ5(int TZ5) {
        this.TZ5 = TZ5;
    }

    public int getTZ6() {
        return TZ6;
    }

    public void setTZ6(int TZ6) {
        this.TZ6 = TZ6;
    }

    public boolean is套装1是否共存() {
        return 套装1是否共存;
    }

    public void set套装1是否共存(boolean 套装1是否共存) {
        this.套装1是否共存 = 套装1是否共存;
    }

    public boolean is套装2是否共存() {
        return 套装2是否共存;
    }

    public void set套装2是否共存(boolean 套装2是否共存) {
        this.套装2是否共存 = 套装2是否共存;
    }

    public boolean is套装3是否共存() {
        return 套装3是否共存;
    }

    public void set套装3是否共存(boolean 套装3是否共存) {
        this.套装3是否共存 = 套装3是否共存;
    }

    public boolean is套装4是否共存() {
        return 套装4是否共存;
    }

    public void set套装4是否共存(boolean 套装4是否共存) {
        this.套装4是否共存 = 套装4是否共存;
    }

    public boolean is套装5是否共存() {
        return 套装5是否共存;
    }

    public void set套装5是否共存(boolean 套装5是否共存) {
        this.套装5是否共存 = 套装5是否共存;
    }

    public boolean is套装6是否共存() {
        return 套装6是否共存;
    }

    public void set套装6是否共存(boolean 套装6是否共存) {
        this.套装6是否共存 = 套装6是否共存;
    }

    public boolean isCanRefresh() {
        return canRefresh;
    }

    public void setCanRefresh(boolean canRefresh) {
        this.canRefresh = canRefresh;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public long getLastMessageTime2() {
        return lastMessageTime2;
    }

    public void setLastMessageTime2(long lastMessageTime2) {
        this.lastMessageTime2 = lastMessageTime2;
    }

    public long getLastMessageTime3() {
        return lastMessageTime3;
    }

    public void setLastMessageTime3(long lastMessageTime3) {
        this.lastMessageTime3 = lastMessageTime3;
    }

    public long getLastMessageTime4() {
        return lastMessageTime4;
    }

    public void setLastMessageTime4(long lastMessageTime4) {
        this.lastMessageTime4 = lastMessageTime4;
    }

    public long getLastMessageTime5() {
        return lastMessageTime5;
    }

    public void setLastMessageTime5(long lastMessageTime5) {
        this.lastMessageTime5 = lastMessageTime5;
    }

    public long getLastMessageTime6() {
        return lastMessageTime6;
    }

    public void setLastMessageTime6(long lastMessageTime6) {
        this.lastMessageTime6 = lastMessageTime6;
    }

    public int getpWatk() {
        return pWatk;
    }

    public void setpWatk(int pWatk) {
        this.pWatk = pWatk;
    }

    public int getpMatk() {
        return pMatk;
    }

    public void setpMatk(int pMatk) {
        this.pMatk = pMatk;
    }

    public int getpWdef() {
        return pWdef;
    }

    public void setpWdef(int pWdef) {
        this.pWdef = pWdef;
    }

    public int getpMdef() {
        return pMdef;
    }

    public void setpMdef(int pMdef) {
        this.pMdef = pMdef;
    }

    public int getpAcc() {
        return pAcc;
    }

    public void setpAcc(int pAcc) {
        this.pAcc = pAcc;
    }

    public int getpAvoid() {
        return pAvoid;
    }

    public void setpAvoid(int pAvoid) {
        this.pAvoid = pAvoid;
    }

    public int getpMaxHp() {
        return pMaxHp;
    }

    public void setpMaxHp(int pMaxHp) {
        this.pMaxHp = pMaxHp;
    }

    public int getpMaxMp() {
        return pMaxMp;
    }

    public void setpMaxMp(int pMaxMp) {
        this.pMaxMp = pMaxMp;
    }

    public int getpSpeed() {
        return pSpeed;
    }

    public void setpSpeed(int pSpeed) {
        this.pSpeed = pSpeed;
    }

    public int getpJump() {
        return pJump;
    }

    public void setpJump(int pJump) {
        this.pJump = pJump;
    }

    public int getpWatkPercent() {
        return pWatkPercent;
    }

    public void setpWatkPercent(int pWatkPercent) {
        this.pWatkPercent = pWatkPercent;
    }

    public int getpMatkPercent() {
        return pMatkPercent;
    }

    public void setpMatkPercent(int pMatkPercent) {
        this.pMatkPercent = pMatkPercent;
    }

    public int getpWdefPercent() {
        return pWdefPercent;
    }

    public void setpWdefPercent(int pWdefPercent) {
        this.pWdefPercent = pWdefPercent;
    }

    public int getpMdefPercent() {
        return pMdefPercent;
    }

    public void setpMdefPercent(int pMdefPercent) {
        this.pMdefPercent = pMdefPercent;
    }

    public int getpAccPercent() {
        return pAccPercent;
    }

    public void setpAccPercent(int pAccPercent) {
        this.pAccPercent = pAccPercent;
    }

    public int getpAvoidPercent() {
        return pAvoidPercent;
    }

    public void setpAvoidPercent(int pAvoidPercent) {
        this.pAvoidPercent = pAvoidPercent;
    }

    public int getpMaxHpPercent() {
        return pMaxHpPercent;
    }

    public void setpMaxHpPercent(int pMaxHpPercent) {
        this.pMaxHpPercent = pMaxHpPercent;
    }

    public int getpMaxMpPercent() {
        return pMaxMpPercent;
    }

    public void setpMaxMpPercent(int pMaxMpPercent) {
        this.pMaxMpPercent = pMaxMpPercent;
    }

    public int getRegularStr() {
        return regularStr;
    }

    public void setRegularStr(int regularStr) {
        this.regularStr = regularStr;
    }

    public int getRegularDex() {
        return regularDex;
    }

    public void setRegularDex(int regularDex) {
        this.regularDex = regularDex;
    }

    public int getRegularLuk() {
        return regularLuk;
    }

    public void setRegularLuk(int regularLuk) {
        this.regularLuk = regularLuk;
    }

    public int getRegularInt_() {
        return regularInt_;
    }

    public void setRegularInt_(int regularInt_) {
        this.regularInt_ = regularInt_;
    }

    public int getPgStr() {
        return pgStr;
    }

    public void setPgStr(int pgStr) {
        this.pgStr = pgStr;
    }

    public int getPgDex() {
        return pgDex;
    }

    public void setPgDex(int pgDex) {
        this.pgDex = pgDex;
    }

    public int getPgLuk() {
        return pgLuk;
    }

    public void setPgLuk(int pgLuk) {
        this.pgLuk = pgLuk;
    }

    public int getPgInt_() {
        return pgInt_;
    }

    public void setPgInt_(int pgInt_) {
        this.pgInt_ = pgInt_;
    }

//    public Map<String, List<SuitSystem>> getSuitSys() {
//        return suitSys;
//    }
//
//    public void setSuitSys(Map<String, List<SuitSystem>> suitSys) {
//        this.suitSys = suitSys;
//    }

    public long getDamage() {
        return damage;
    }

    public void setDamage(long damage) {
        this.damage = damage;
    }

    public void refreshLimitAP() {
        MapleCharacter chra = (MapleCharacter)this.chr.get();
        Map<MapleStat, Integer> statupdate = new EnumMap(MapleStat.class);
        statupdate.put(MapleStat.STR, this.str + this.limitStr >= 32767 ? 32767 : this.str + this.limitStr);
        statupdate.put(MapleStat.DEX, this.dex + this.limitDex >= 32767 ? 32767 : this.dex + this.limitDex);
        statupdate.put(MapleStat.LUK, this.luk + this.limitLuk >= 32767 ? 32767 : this.luk + this.limitLuk);
        statupdate.put(MapleStat.INT, this.int_ + this.limitInt >= 32767 ? 32767 : this.int_ + this.limitInt);
        chra.getClient().sendPacket(MaplePacketCreator.updatePlayerStats(statupdate, true, chra));
    }

    public short getLimitStr() {
        return limitStr;
    }

    public void setLimitStr(short limitStr) {
        this.limitStr = limitStr;
    }

    public short getLimitDex() {
        return limitDex;
    }

    public void setLimitDex(short limitDex) {
        this.limitDex = limitDex;
    }

    public short getLimitLuk() {
        return limitLuk;
    }

    public void setLimitLuk(short limitLuk) {
        this.limitLuk = limitLuk;
    }

    public short getLimitInt() {
        return limitInt;
    }

    public void setLimitInt(short limitInt) {
        this.limitInt = limitInt;
    }

}
