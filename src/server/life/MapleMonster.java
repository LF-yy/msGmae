package server.life;

import bean.HideAttribute;
import fumo.FumoSkill;
import gui.LtMS;
import handling.world.MaplePartyCharacter;
import client.inventory.IItem;
import client.inventory.Item;
import handling.world.World;
import server.MapleItemInformationProvider;
import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import server.Randomizer;

import java.io.UnsupportedEncodingException;
import java.util.*;

import server.Start;
import snail.DamageManage;
import snail.MonsterKillQuest;
import tools.Pair;
import server.Timer.MobTimer;
import client.ISkill;
import constants.GameConstants;
import client.SkillFactory;
import server.MapleStatEffect;

import java.util.List;
import java.util.Map.Entry;
import server.maps.MapleMapObjectType;
import server.maps.MapScriptMethods;
import client.MapleClient;
import tools.MaplePacketCreator;
import server.maps.MapleMapObject;
import handling.channel.ChannelServer;
import client.MapleDisease;
import handling.world.MapleParty;
import client.MapleBuffStat;
import tools.packet.MobPacket;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import client.status.MonsterStatusEffect;
import client.status.MonsterStatus;
import scripting.EventInstanceManager;
import client.MapleCharacter;
import java.lang.ref.WeakReference;
import server.maps.MapleMap;

public class MapleMonster extends AbstractLoadedMapleLife
{
    MapleMonsterStats stats;
    private OverrideMonsterStats ostats;
    private long hp;
    private long nextKill;
    private int mp;
    private byte venom_counter;
    private byte carnivalTeam;
    private MapleMap map;
    private WeakReference<MapleMonster> sponge;
    private int linkoid;
    private int lastNode;
    private int lastNodeController;
    private int highestDamageChar;
    private WeakReference<MapleCharacter> controller;
    private boolean fake;
    private boolean dropsDisabled;
    private boolean controllerHasAggro;
    private boolean controllerKnowsAboutAggro;
    private final Collection<AttackerEntry> attackers;
    private EventInstanceManager eventInstance;
    private MonsterListener listener;
    private byte[] reflectpack;
    private byte[] nodepack;
    private final EnumMap<MonsterStatus, MonsterStatusEffect> stati;
    private final LinkedList<MonsterStatusEffect> poisons;
    private final ReentrantReadWriteLock poisonsLock;
    private Map<Integer, Long> usedSkills;
    private int stolen;
    private ScheduledFuture<?> dropItemSchedule;
    private boolean shouldDropItem;
    private long lastAbsorbMP;
    private int owner;
    private long duration;
    private DamageManage.MobDamageData mobDamageData;
    private long exp;
    private boolean monitor;
    public boolean isMonitor() {
        return this.monitor;
    }
    private String name;
    public long getDuration() {
        return duration;
    }

    private long spawnTime = 0L;
    public long getLastDuration() {
        long nowTime = Calendar.getInstance().getTimeInMillis();
        long lastDuration = this.duration - (nowTime - this.spawnTime);
        if (lastDuration < 0L) {
            lastDuration = 0L;
        }

        return lastDuration;
    }
    public void setDuration(long duration) {
        this.spawnTime = Calendar.getInstance().getTimeInMillis();
        this.duration = duration;
        World.addDurationMonster(this);

    }
    public final long getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = (long)exp;
    }

    public int getOwner() {
        return this.owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
    public DamageManage.MobDamageData getMobDamageData() {
        return this.mobDamageData;
    }

    public void setMobDamageData(DamageManage.MobDamageData mobDamageData) {
        this.mobDamageData = mobDamageData;
    }

    public MapleMonster(final int id, final MapleMonsterStats stats) {
        super(id);
        this.ostats = null;
        this.sponge = new WeakReference<MapleMonster>(null);
        this.linkoid = 0;
        this.lastNode = -1;
        this.lastNodeController = -1;
        this.highestDamageChar = 0;
        this.controller = new WeakReference<MapleCharacter>(null);
        this.attackers = new LinkedList<AttackerEntry>();
        this.listener = null;
        this.reflectpack = null;
        this.nodepack = null;
        this.stati = new EnumMap<MonsterStatus, MonsterStatusEffect>(MonsterStatus.class);
        this.poisons = new LinkedList<MonsterStatusEffect>();
        this.poisonsLock = new ReentrantReadWriteLock();
        this.stolen = -1;
        this.shouldDropItem = false;
        this.mobDamageData = new DamageManage.MobDamageData();

        this.initWithStats(stats);
    }
    public String getName() {
        if (this.name == null || this.name.equals("")) {
            this.name = MapleLifeFactory.getName(this.getId());
        }

        return this.name;
    }
    public MapleMonster(final MapleMonster monster) {
        super((AbstractLoadedMapleLife)monster);
        this.ostats = null;
        this.sponge = new WeakReference<MapleMonster>(null);
        this.linkoid = 0;
        this.lastNode = -1;
        this.lastNodeController = -1;
        this.highestDamageChar = 0;
        this.controller = new WeakReference<MapleCharacter>(null);
        this.attackers = new LinkedList<AttackerEntry>();
        this.listener = null;
        this.reflectpack = null;
        this.nodepack = null;
        this.stati = new EnumMap<MonsterStatus, MonsterStatusEffect>(MonsterStatus.class);
        this.poisons = new LinkedList<MonsterStatusEffect>();
        this.poisonsLock = new ReentrantReadWriteLock();
        this.mobDamageData = new DamageManage.MobDamageData();

        this.stolen = -1;
        this.shouldDropItem = false;
        this.initWithStats(monster.stats);
    }
    
    public final MapleMonsterStats getStats() {
        return this.stats;
    }
    
    private void initWithStats(final MapleMonsterStats stats) {
        this.setStance(5);
        this.stats = stats;
        this.hp = stats.getHp();
        this.mp = stats.getMp();
        this.exp = (long)stats.getExp();
        this.venom_counter = 0;
        this.carnivalTeam = -1;
        this.fake = false;
        this.dropsDisabled = false;
        if (stats.getNoSkills() > 0) {
            this.usedSkills = new HashMap<Integer, Long>();
        }
    }
    public void setMonitor(boolean monitor) {
        this.monitor = monitor;
    }
    public void disableDrops() {
        this.dropsDisabled = true;
    }
    
    public final boolean dropsDisabled() {
        return this.dropsDisabled;
    }
    
    public void setMap(final MapleMap map) {
        this.map = map;
        this.startDropItemSchedule();
    }
    
    public final MapleMap getMap() {
        return this.map;
    }
    
    public void setSponge(final MapleMonster mob) {
        this.sponge = new WeakReference<MapleMonster>(mob);
    }
    
    public final MapleMonster getSponge() {
        return (MapleMonster)this.sponge.get();
    }
    
    public void setHp(final long hp) {
        this.hp = hp;
    }
    
    public final long getHp() {
        return this.hp;
    }
    
    public final long getMobMaxHp() {
        if (this.ostats != null) {
            return this.ostats.getHp();
        }
        return this.stats.getHp();
    }
    
    public void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }
    
    public  int getMp() {
        return this.mp;
    }
    
    public  int getMobMaxMp() {
        if (this.ostats != null) {
            return this.ostats.getMp();
        }
        return this.stats.getMp();
    }
    
    public  int getMobExp() {
        if (this.ostats != null) {
            return this.ostats.getExp();
        }
        return this.stats.getExp();
    }
    
    public  int getMobLevel() {
        if (this.ostats != null) {
            return this.ostats.getlevel();
        }
        return this.stats.getLevel();
    }
    
    public void setOverrideStats(final OverrideMonsterStats ostats) {
        this.ostats = ostats;
        this.hp = ostats.getHp();
        this.mp = ostats.getMp();
    }
    
    public  byte getVenomMulti() {
        return this.venom_counter;
    }
    
    public void setVenomMulti(final byte venom_counter) {
        this.venom_counter = venom_counter;
    }
    
    public void absorbMP(final int amount) {
        if (!this.canAbsorbMP()) {
            return;
        }
        if (this.getMp() >= amount) {
            this.setMp(this.getMp() - amount);
        }
        else {
            this.setMp(0);
        }
        this.lastAbsorbMP = System.currentTimeMillis();
    }
    
    public  long getLastAbsorbMP() {
        return this.lastAbsorbMP;
    }
    
    public  boolean canAbsorbMP() {
        return System.currentTimeMillis() - this.lastAbsorbMP > 10000L;
    }
    
    public void damage(final MapleCharacter from, final long damage, final boolean updateAttackTime) {
        this.damage(from, damage, updateAttackTime, 0);
    }
    
    public void damage( MapleCharacter from,  long damage,  boolean updateAttackTime,  int lastSkill) {
        if (from == null || damage <= 0L || !this.isAlive()) {
            return;
        }
        AttackerEntry attacker = (from.getParty() != null) ? new PartyAttackerEntry(from.getParty().getId(), this.map.getChannel()) : new SingleAttackerEntry(from, this.map.getChannel());
        boolean replaced = false;
        for ( AttackerEntry aentry : this.attackers) {
            if (aentry.equals((Object)attacker)) {
                attacker = aentry;
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            this.attackers.add(attacker);
        }
        if (this.monitor && this.mobDamageData != null) {
            this.mobDamageData.addDamage(from, damage);
        }
         long rightDamage = Math.max(0L, Math.min(damage, this.hp));
        attacker.addDamage(from, rightDamage, updateAttackTime);
        if (this.getStats().getSelfD() != -1) {
             long newHp = this.getHp() - rightDamage;
            this.setHp(newHp);
            if (this.getHp() > 0L) {
                if (this.getHp() < (long)this.getStats().getSelfDHp()) {
                    this.getMap().killMonster(this, from, false, false, this.getStats().getSelfD(), lastSkill);
                } else {
                    for ( AttackerEntry mattacker : this.attackers) {
                        for ( AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
                            if (cattacker != null && cattacker.getAttacker().getMap() == from.getMap() && cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L) {
                                cattacker.getAttacker().getClient().sendPacket(MobPacket.showMonsterHP(this.getObjectId(), (int)Math.ceil((double)this.hp * 100.0 / (double)this.getMobMaxHp())));
                            }
                        }
                    }
                }
            } else {
                this.getMap().killMonster(this, from, true, false, (byte)1, lastSkill);
            }
        } else {
            if (this.getSponge() != null && this.getSponge().getHp() > 0L) {
                 long newHp = this.getSponge().getHp() - rightDamage;
                this.getSponge().setHp(newHp);
                if (this.getSponge().getHp() <= 0L) {
                    this.getMap().killMonster((MapleMonster)this.sponge.get(), from, true, false, (byte)1, lastSkill);
                }else {
                    this.getMap().broadcastMessage(MobPacket.showBossHP((MapleMonster)this.sponge.get()));
                }
            }
            if (this.getHp() > 0L) {
                 long newHp = this.getHp() - rightDamage;
                this.setHp(newHp);
                if (this.eventInstance != null) {
                    this.eventInstance.monsterDamaged(from, this, (int)rightDamage);
                } else {
                     EventInstanceManager em = from.getEventInstance();
                    if (em != null) {
                        em.monsterDamaged(from, this, (int)rightDamage);
                    }
                }
                if (this.getSponge() == null && this.hp > 0L) {
                    switch (this.getStats().getHPDisplayType()) {
                        case 0: {
                            this.getMap().broadcastMessage(MobPacket.showBossHP(this), this.getPosition());
                            break;
                        }
                        case 1: {
                            this.getMap().broadcastMessage(MobPacket.damageFriendlyMob(this, damage, true));
                            break;
                        }
                        case -1:
                        case 2: {
                            this.getMap().broadcastMessage(MobPacket.showMonsterHP(this.getObjectId(), (int)Math.ceil((double)this.hp * 100.0 / (double)this.getMobMaxHp())));
                            from.mulungEnergyModify(true);
                            break;
                        }
                        case 3: {
                            try {
                                for (final AttackerEntry mattacker : this.attackers) {
                                    for (final AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
                                        if (cattacker != null && cattacker.getAttacker().getMap() == from.getMap() && cattacker.getLastAttackTime() >= System.currentTimeMillis() - 4000L) {
                                            cattacker.getAttacker().getClient().sendPacket(MobPacket.showMonsterHP(this.getObjectId(), (int)Math.ceil((double)this.hp * 100.0 / (double)this.getMobMaxHp())));
                                        }
                                    }
                                }
                            } catch (Exception e) {

                            }
                            break;
                        }
                    }
                }
                if (this.getHp() <= 0L) {
                    if (this.monitor && this.mobDamageData != null && this.mobDamageData.getMainMobId() == this.getId()) {
                        List<Integer> toSpawn = this.stats.getRevives();
                        boolean canCalculate = true;
                        Iterator<Integer> iterator = toSpawn.iterator();

                        while(iterator.hasNext()) {
                            int spawnId = (Integer)iterator.next();
                            if (!GameConstants.isFakeRevive(spawnId)) {
                                canCalculate = false;
                                break;
                            }
                        }

                        if (canCalculate) {
                            try {
                                this.mobDamageData.calculate();
                            } catch (UnsupportedEncodingException e) {

                            }
                        }
                    }
                    if (from.getParty() != null) {
                        for (MaplePartyCharacter pChr : from.getParty().getMembers()) {
                            int chrId = pChr.getId();
                            MapleCharacter chr = from.getMap().getCharacterById(chrId);
                            if (chr != null) {
                                updateMonsterKillQuest(chr, this.getId(), this.getName());
                            }
                        }
                    } else {
                        updateMonsterKillQuest(from, this.getId(), this.getName());
                    }



                    if (this.getStats().getHPDisplayType() == -1) {
                        this.getMap().broadcastMessage(MobPacket.showMonsterHP(this.getObjectId(), (int)Math.ceil((double)this.hp * 100.0 / (double)this.getMobMaxHp())));
                    }
                    this.getMap().killMonster(this, from, true, false, (byte)1, lastSkill);
                }
            }
        }
        this.startDropItemSchedule();
    }
    private void updateMonsterKillQuest(MapleCharacter character, int monsterId, String monsterName) {
        List<MonsterKillQuest> monsterKillQuestList = character.getMonsterKillQuestByMonsterId(monsterId);
        for (MonsterKillQuest monsterKillQuest : monsterKillQuestList) {
            if (monsterKillQuest != null && !monsterKillQuest.isFinished(monsterId)) {
                monsterKillQuest.addCount(monsterId, 1);
                character.dropMessage(-1, "击杀 " + monsterName + "[" + monsterKillQuest.getNowQuantity(monsterId) + "/" + monsterKillQuest.getFinishQuantity(monsterId) + "]");
            }
        }
    }
    public void heal(final int hp, final int mp, final boolean broadcast) {
        long totalHP = this.getHp() + (long)hp;
        int totalMP = this.getMp() + mp;
        totalHP = ((totalHP > this.getMobMaxHp()) ? this.getMobMaxHp() : totalHP);
        totalMP = ((totalMP > this.getMobMaxMp()) ? this.getMobMaxMp() : totalMP);
        this.setHp(totalHP);
        this.setMp(totalMP);
        if (broadcast) {
            this.getMap().broadcastMessage(MobPacket.healMonster(this.getObjectId(), hp));
        }
        if (this.getSponge() != null) {
            totalHP = this.getSponge().getHp() + (long)hp;
            totalMP = this.getSponge().getMp() + mp;
            totalHP = ((totalHP > this.getSponge().getMobMaxHp()) ? this.getSponge().getMobMaxHp() : totalHP);
            totalMP = ((totalMP > this.getSponge().getMobMaxMp()) ? this.getSponge().getMobMaxMp() : totalMP);
            this.getSponge().setHp(totalHP);
            this.getSponge().setMp(totalMP);
        }
    }
    public void healLong( long hp, final int mp, final boolean broadcast) {
        long totalHP = this.getHp() + (long)hp;
        int totalMP = this.getMp() + mp;
        totalHP = ((totalHP > this.getMobMaxHp()) ? this.getMobMaxHp() : totalHP);
        totalMP = ((totalMP > this.getMobMaxMp()) ? this.getMobMaxMp() : totalMP);
        this.setHp(totalHP);
        this.setMp(totalMP);
        if (broadcast) {
            this.getMap().broadcastMessage(MobPacket.healMonster(this.getObjectId(), hp>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)hp));
        }
        if (this.getSponge() != null) {
            totalHP = this.getSponge().getHp() + (long)hp;
            totalMP = this.getSponge().getMp() + mp;
            totalHP = ((totalHP > this.getSponge().getMobMaxHp()) ? this.getSponge().getMobMaxHp() : totalHP);
            totalMP = ((totalMP > this.getSponge().getMobMaxMp()) ? this.getSponge().getMobMaxMp() : totalMP);
            this.getSponge().setHp(totalHP);
            this.getSponge().setMp(totalMP);
        }
    }
    //击杀怪物获取经验
    private void giveExpToCharacter(final MapleCharacter attacker, int exp, final boolean highestDamage, final int numExpSharers, final byte pty, final byte classBounsExpPercent, final byte Premium_Bonus_EXP_PERCENT, final int lastskillID) {

        if (LtMS.ConfigValuesMap.get("修正经验") >0 && attacker.getLevel() >= 200) {
            exp = (int)Math.round(exp * ((double)LtMS.ConfigValuesMap.get("修正经验比例") / 100.0));
        }
        HideAttribute hideAttribute = Start.hideAttributeMap.get(attacker.getId());

        if (hideAttribute.totalExpRate>0){
            exp += (int)Math.round(exp * (hideAttribute.totalExpRate / 100.0));
        }
        exp += hideAttribute.totalExpRateCount;
        if(exp <0 ){
            exp = 0;
        }
        if (highestDamage) {
            if (this.eventInstance != null) {
                this.eventInstance.monsterKilled(attacker, this);
            }
            else {
                final EventInstanceManager em = attacker.getEventInstance();
                if (em != null) {
                    em.monsterKilled(attacker, this);
                }
            }
            this.highestDamageChar = attacker.getId();
        }
        final double 怪物坐标X = this.getPosition().getX();
        final double 怪物坐标Y = this.getPosition().getY();
        final double X坐标误差 = attacker.getPosition().getX() - 怪物坐标X;
        final double Y坐标误差 = attacker.getPosition().getY() - 怪物坐标Y;
        final int 记录地图 = attacker.getMapId();
        if (attacker.打怪地图 == 0) {
            attacker.打怪地图 = 记录地图;
        }
        else if (记录地图 != attacker.打怪地图) {
            attacker.打怪地图 = 0;
            attacker.打怪数量 = 0;
            attacker.X坐标误差 = 0.0;
            attacker.Y坐标误差 = 0.0;
        }
        if (attacker.打怪地图 > 0) {
            if (X坐标误差 > attacker.X坐标误差) {
                attacker.X坐标误差 = X坐标误差;
            }
            if (Y坐标误差 > attacker.Y坐标误差) {
                attacker.Y坐标误差 = Y坐标误差;
            }
        }
        if (exp > 0) {
            if ((Integer) LtMS.ConfigValuesMap.get("越级打怪开关") >0 ) {
                int 怪物 = this.getMobLevel();
                int 玩家 = attacker.getLevel();
                if (玩家 < 怪物) {
                    int 相差 = 怪物 - 玩家;
                    if (相差 >= (Integer)LtMS.ConfigValuesMap.get("越级打怪经验减半判定等级差") && 相差 < (Integer)LtMS.ConfigValuesMap.get("越级打怪经验全扣判定等级差")) {
                        exp = (int)((double)exp * 0.5);
                    } else if (相差 >= (Integer)LtMS.ConfigValuesMap.get("越级打怪经验全扣判定等级差")) {
                        exp *= 0;
                    }
                }
            }
            if (exp > 0) {
                final MonsterStatusEffect mse = (MonsterStatusEffect)this.stati.get((Object)MonsterStatus.SHOWDOWN);
                if (mse != null) {
                    exp += (int)((double)exp * ((double)(int)mse.getX() / 100.0));
                }
                final Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                if (holySymbol != null) {
                    if (numExpSharers == 1) {
                        exp = (int)((double)exp * (1.0 + (double)holySymbol / 500.0));
                    }
                    else {
                        exp = (int)((double)exp * (1.0 + (double)holySymbol / 100.0));
                    }
                    if (attacker.getEquippedFuMoMap().get(FumoSkill.FM("神圣祈祷ex")) != null) {
                        attacker.gainExp(exp / 100 * (Integer)attacker.getEquippedFuMoMap().get(FumoSkill.FM("神圣祈祷ex")), true, false, false);
                    }
                }
                final int 职业 = attacker.getJob();
                final int 职业2 = MapleParty.幸运职业;
                if (职业 == 职业2 || 职业 - 职业2 == 1 || 职业2 - 职业 == -1) {
                    exp = (int)((double)exp + (double)exp * 0.5);
                }
                if (attacker.hasDisease(MapleDisease.CURSE)) {
                    if (attacker.getEquippedFuMoMap().get(FumoSkill.FM("苦中作乐")) != null) {
                        exp *= 5;
                    } else {
                        exp /= 2;
                    }
                }

                double lastexp = attacker.getStat().realExpBuff - 100.0 <= 0.0 ? 100.0 : attacker.getStat().realExpBuff - 100.0;
                exp = (int)((double)exp * attacker.getEXPMod() * (double)attacker.getExpRateChr() * (double)((int)(lastexp / 100.0)));
//                    if (attacker.getLevel() < 10) {
//                        exp = (int)(1 * exp * ChannelServer.getInstance(this.map.getChannel()).getExpRate());
//                    }
//                    else if (attacker.getLevel() >= 10 && attacker.getLevel() < 30) {
//                        exp = (int)(1 * exp * ChannelServer.getInstance(this.map.getChannel()).getExpRate());
//                    }
//                    else if (attacker.getLevel() >= 30 && attacker.getLevel() < 60) {
//                        exp = (int)(1 * exp * ChannelServer.getInstance(this.map.getChannel()).getExpRate());
//                    }
//                    else if (attacker.getLevel() >= 60 && attacker.getLevel() < 90) {
//                        exp = (int)(1 * exp * ChannelServer.getInstance(this.map.getChannel()).getExpRate());
//                    }
//                    else if (attacker.getLevel() >= 90 && attacker.getLevel() < 120) {
//                        exp = (int)(1 * exp * ChannelServer.getInstance(this.map.getChannel()).getExpRate());
//                    }
//                    else {
                        exp *= ChannelServer.getInstance(this.map.getChannel()).getExpRate();
//                    }

                int classBonusExp;
                if (attacker.getExpm() > 1.0) {
                    classBonusExp = 0;
                    classBonusExp += (int)((double)exp * (attacker.getExpm() - 1.0));
                    attacker.gainExp(classBonusExp, true, false, false);
                }
                exp = (int)Math.min(2.14748365E9F, (float)exp * (attacker.getLevel() < 10 ? (float)GameConstants.getExpRate_Below10(attacker.getJob()) : ChannelServer.getInstance(this.map.getChannel()).getExpRate() * ChannelServer.getInstance(this.map.getChannel()).getExpRateSpecial()));
                classBonusExp = 0;
                if (classBounsExpPercent > 0) {
                    classBonusExp = (int)((double)exp / 100.0 * (double)classBounsExpPercent);
                }
                int premiumBonusExp = 0;
                int equpBonusExp;
                if ((Integer)LtMS.ConfigValuesMap.get("网吧经验加成") != 0) {
                    equpBonusExp = (Integer)LtMS.ConfigValuesMap.get("网吧经验加成");
                    premiumBonusExp += (int)((double)exp / 100.0 * (double)equpBonusExp);
                }

                equpBonusExp = (int)((double)exp / 100.0 * (double)attacker.getStat().equipmentBonusExp);
                if (attacker.getStat().equippedFairy) {
                    equpBonusExp = (int)((1.0 + (double)((float)attacker.getFairyExp())) / 100.0 * (double)((float)exp));
                }


                int wedding_EXP = 0;
                if (attacker.getMarriageId() > 0 && attacker.getMap().getCharacterById_InMap(attacker.getMarriageId()) != null && (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"结婚经验加成")) == 1) {
                    final int 结婚经验加成 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"结婚经验加成"));
                    wedding_EXP = (int)((double)wedding_EXP + (double)exp / 100.0 * (double)结婚经验加成);
                }
                if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"人气经验加成")) > 0 && attacker.getFame() > 0) {
                    attacker.人气经验加成();
                }

                int EXP;
                if (attacker.是否开店()) {
                    EXP = (Integer)LtMS.ConfigValuesMap.get("开店经验加成");
                    EXP = (int)((double)exp / 100.0 * (double)EXP);
                    attacker.gainExp(EXP, true, false, false);
                }
                if (attacker.getEquippedFuMoMap().get(FumoSkill.FM("幸运狩猎")) != null) {
                    EXP = (int)((double)exp / 100.0 * (double)(Integer)attacker.getEquippedFuMoMap().get(FumoSkill.FM("幸运狩猎")));
                    attacker.gainExp(EXP, true, false, false);
                }
                exp *= attacker.getExpRateChr();
                attacker.gainExpMonster(Math.min(exp, Integer.MAX_VALUE), true, highestDamage, pty, classBonusExp, equpBonusExp, premiumBonusExp);

            }
            attacker.mobKilled(this.getId(), lastskillID);
        }
    }
    
    public final int killBy(final MapleCharacter killer, final int lastSkill) {
        final int totalBaseExp = this.getMobExp();
        AttackerEntry highest = null;
        long highdamage = 0L;
        for (final AttackerEntry attackEntry : this.attackers) {
            if (attackEntry.getDamage() > highdamage) {
                highest = attackEntry;
                highdamage = attackEntry.getDamage();
            }
        }
        for (final AttackerEntry attackEntry : this.attackers) {
            final int baseExp = (int)Math.ceil((double)totalBaseExp * ((double)attackEntry.getDamage() / (double)this.getMobMaxHp()));
            attackEntry.killedMob(this.getMap(), baseExp, attackEntry == highest, lastSkill);
        }
        final MapleCharacter controll = this.getController();
        if (controll != null) {
            controll.getClient().sendPacket(MobPacket.stopControllingMonster(this.getObjectId()));
            controll.stopControllingMonster(this);
        }
        switch (this.getId()) {
            default: {
                this.spawnRevives(this.getMap());
                if (this.eventInstance != null) {
                    this.eventInstance.unregisterMonster(this);
                    this.eventInstance = null;
                }
                if (killer != null && killer.getPyramidSubway() != null) {
                    killer.getPyramidSubway().onKill(killer);
                }
                final MapleMonster oldSponge = this.getSponge();
                this.sponge = new WeakReference<MapleMonster>(null);
                if (oldSponge != null && oldSponge.isAlive()) {
                    boolean set = true;
                    for (final MapleMapObject mon : this.map.getAllMonstersThreadsafe()) {
                        final MapleMonster mons = (MapleMonster)mon;
                        if (mons.getObjectId() != oldSponge.getObjectId() && mons.getObjectId() != this.getObjectId() && (mons.getSponge() == oldSponge || mons.getLinkOid() == oldSponge.getObjectId())) {
                            set = false;
                            break;
                        }
                    }
                    if (set) {
                        this.map.killMonster(oldSponge, killer, true, false, (byte)1);
                    }
                }
                this.nodepack = null;
                this.reflectpack = null;
                this.stati.clear();
                this.cancelDropItem();
                if (this.listener != null) {
                    this.listener.monsterKilled();
                }
                final int v1 = this.highestDamageChar;
                this.highestDamageChar = 0;
                return v1;
            }
        }
    }
    
//    public void spawnRevives(final MapleMap map) {
//        final List<Integer> toSpawn = this.stats.getRevives();
//        if (toSpawn == null) {
//            return;
//        }
//        MapleMonster spongy = null;
//        switch (this.getId()) {
//            case 8810118:
//            case 8810119:
//            case 8810120:
//            case 8810121: {
//                final Iterator<Integer> iterator = toSpawn.iterator();
//                while (iterator.hasNext()) {
//                    final int i = (int)Integer.valueOf(iterator.next());
//                    final MapleMonster mob = MapleLifeFactory.getMonster(i);
//                    mob.setPosition(this.getPosition());
//                    if (this.eventInstance != null) {
//                        this.eventInstance.registerMonster(mob);
//                    }
//                    if (this.dropsDisabled()) {
//                        mob.disableDrops();
//                    }
//                    switch (mob.getId()) {
//                        case 8810119:
//                        case 8810120:
//                        case 8810121:
//                        case 8810122: {
//                            spongy = mob;
//                            continue;
//                        }
//                    }
//                }
//                if (spongy != null) {
//                    map.spawnRevives(spongy, this.getObjectId());
//                    for (final MapleMapObject mon : map.getAllMonstersThreadsafe()) {
//                        final MapleMonster mons = (MapleMonster)mon;
//                        if (mons.getObjectId() != spongy.getObjectId() && (mons.getSponge() == this || mons.getLinkOid() == this.getObjectId())) {
//                            mons.setSponge(spongy);
//                            mons.setLinkOid(spongy.getObjectId());
//                        }
//                    }
//                    break;
//                }
//                break;
//            }
//            case 8810026:
//            case 8810130:
//            case 8820008:
//            case 8820009:
//            case 8820010:
//            case 8820011:
//            case 8820012:
//            case 8820013: {
//                final List<MapleMonster> mobs = new ArrayList<MapleMonster>();
//                final Iterator<Integer> iterator3 = toSpawn.iterator();
//                while (iterator3.hasNext()) {
//                    final int j = (int)Integer.valueOf(iterator3.next());
//                    final MapleMonster mob2 = MapleLifeFactory.getMonster(j);
//                    mob2.setPosition(this.getPosition());
//                    if (this.eventInstance != null) {
//                        this.eventInstance.registerMonster(mob2);
//                    }
//                    if (this.dropsDisabled()) {
//                        mob2.disableDrops();
//                    }
//                    switch (mob2.getId()) {
//                        case 8810018:
//                        case 8810118:
//                        case 8820009:
//                        case 8820010:
//                        case 8820011:
//                        case 8820012:
//                        case 8820013:
//                        case 8820014: {
//                            spongy = mob2;
//                            continue;
//                        }
//                        default: {
//                            mobs.add(mob2);
//                            continue;
//                        }
//                    }
//                }
//                if (spongy != null) {
//                    map.spawnRevives(spongy, this.getObjectId());
//                    for (final MapleMonster k : mobs) {
//                        k.setSponge(spongy);
//                        map.spawnRevives(k, this.getObjectId());
//                    }
//                    break;
//                }
//                break;
//            }
//            default: {
//                final Iterator<Integer> iterator5 = toSpawn.iterator();
//                while (iterator5.hasNext()) {
//                    final int i = (int)Integer.valueOf(iterator5.next());
//                    final MapleMonster mob = MapleLifeFactory.getMonster(i);
//                    if (this.eventInstance != null) {
//                        this.eventInstance.registerMonster(mob);
//                    }
//                    mob.setPosition(this.getPosition());
//                    if (this.dropsDisabled()) {
//                        mob.disableDrops();
//                    }
//                    if (this.monitor && this.mobDamageData != null && !GameConstants.isFakeRevive(mob.getId())) {
//                        if (this.getId() != this.mobDamageData.getMainMobId() && mob.getId() != 8820001) {
//                            this.mobDamageData.addMonster(mob, false);
//                        } else {
//                            this.mobDamageData.addMonster(mob, true);
//                        }
//                    }
//                    map.spawnRevives(mob, this.getObjectId());
//
//                    if (this.eventInstance == null && !mob.getStats().isBoss() && (this.getMap().haveMonster(9900000) || this.getMap().haveMonster(9900001) && this.getMap().haveMonster(9900002))) {
//                        int rateByStone;
//                        if (this.getMap().getStoneLevel() == 1) {
//                            rateByStone = (Integer) LtMS.ConfigValuesMap.get("1级轮回碑石怪物倍数");
//                        } else if (this.getMap().getStoneLevel() >= 2) {
//                            rateByStone = (Integer)LtMS.ConfigValuesMap.get("2级轮回碑石怪物倍数");
//                        } else {
//                            rateByStone = (Integer)LtMS.ConfigValuesMap.get("轮回碑石怪物倍数");
//                        }
//
//                        for(int j = 0; j < rateByStone; ++j) {
//                            MapleMonster mob2 = MapleLifeFactory.getMonster(i);
//                            mob2.setPosition(this.getPosition());
//                            if (this.dropsDisabled()) {
//                                mob2.disableDrops();
//                            }
//
//                            map.spawnRevives(mob2, this.getObjectId());
//                        }
//                    }
//
//                    if (mob.getId() == 9300216) {
//                        map.broadcastMessage(MaplePacketCreator.environmentChange("Dojang/clear", 4));
//                        map.broadcastMessage(MaplePacketCreator.environmentChange("dojang/end/clear", 3));
//                    }
//                    if (map.getId() == 280030002) {
//                        MapleLifeFactory.deleteStats(i);
//                    }
//                }
//                break;
//            }
//        }
//    }
    
    public void setCarnivalTeam(final byte team) {
        this.carnivalTeam = team;
    }
    
    public final byte getCarnivalTeam() {
        return this.carnivalTeam;
    }
    
    public final MapleCharacter getController() {
        return (MapleCharacter)this.controller.get();
    }
    
    public void setController(final MapleCharacter controller) {
        this.controller = new WeakReference<MapleCharacter>(controller);
    }
    
    public void switchController(final MapleCharacter newController, final boolean immediateAggro) {
        final MapleCharacter controllers = this.getController();
        if (controllers == newController) {
            return;
        }
        if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().sendPacket(MobPacket.stopControllingMonster(this.getObjectId()));
            this.sendStatus(controllers.getClient());
        }
        newController.controlMonster(this, immediateAggro);
        this.setController(newController);
        if (immediateAggro) {
            this.setControllerHasAggro(true);
        }
        this.setControllerKnowsAboutAggro(false);
        if (this.getId() == 9300275 && this.map.getId() >= 921120100 && this.map.getId() < 921120500) {
            if (this.lastNodeController != -1 && this.lastNodeController != newController.getId()) {
                this.resetShammos(newController.getClient());
            }
            else {
                this.setLastNodeController(newController.getId());
            }
        }
    }
    
    public void resetShammos(final MapleClient c) {
        this.map.killAllMonsters(true);
        this.map.broadcastMessage(MaplePacketCreator.serverNotice(5, "A player has moved too far from Shammos. Shammos is going back to the start."));
        for (MapleCharacter chr : this.map.getCharactersThreadsafe()) {
            chr.changeMap(chr.getMap(), chr.getMap().getPortal(0));
        }
        MapScriptMethods.startScript_FirstUser(c, "shammos_Fenter");
    }
    
    public void setListener(final MonsterListener listener) {
        this.listener = listener;
    }
    
    public final boolean isControllerHasAggro() {
        return this.controllerHasAggro;
    }
    
    public void setControllerHasAggro(final boolean controllerHasAggro) {
        this.controllerHasAggro = controllerHasAggro;
    }
    
    public final boolean isControllerKnowsAboutAggro() {
        return this.controllerKnowsAboutAggro;
    }
    
    public void setControllerKnowsAboutAggro(final boolean controllerKnowsAboutAggro) {
        this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
    }
    
    public void sendStatus(final MapleClient client) {
        if (this.reflectpack != null) {
            client.getSession().writeAndFlush((Object)this.reflectpack);
        }
        if (this.poisons.size() > 0) {
            this.poisonsLock.readLock().lock();
            try {
                client.getSession().writeAndFlush((Object)MobPacket.applyMonsterStatus(this, (List<MonsterStatusEffect>)this.poisons));
            }
            finally {
                this.poisonsLock.readLock().unlock();
            }
        }
    }
    
    @Override
    public void sendSpawnData(final MapleClient client) {
        if (!this.isAlive()) {
            return;
        }
        client.sendPacket(MobPacket.spawnMonster(this, (this.lastNode >= 0) ? -2 : -1, this.fake ? 252 : ((this.lastNode >= 0) ? 12 : 0), 0));
        this.sendStatus(client);
        if (this.lastNode >= 0) {
            client.sendPacket(MaplePacketCreator.getNodeProperties(this, this.map));
            if (this.getId() == 9300275 && this.map.getId() >= 921120100 && this.map.getId() < 921120500) {
                if (this.lastNodeController != -1) {
                    this.resetShammos(client);
                }
                else {
                    this.setLastNodeController(client.getPlayer().getId());
                }
            }
        }
    }
    
    @Override
    public void sendDestroyData(final MapleClient client) {
        if (this.lastNode == -1) {
            client.sendPacket(MobPacket.killMonster(this.getObjectId(), 0));
        }
        if (this.getId() == 9300275 && this.map.getId() >= 921120100 && this.map.getId() < 921120500) {
            this.resetShammos(client);
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.stats.getName());
        sb.append("(");
        sb.append(this.getId());
        sb.append(") (等級 ");
        sb.append((int)this.stats.getLevel());
        sb.append(") 在 (X");
        sb.append(this.getPosition().x);
        sb.append("/ Y");
        sb.append(this.getPosition().y);
        sb.append(") 座標 ");
        sb.append(this.getHp());
        sb.append("/ ");
        sb.append(this.getMobMaxHp()>10000 ? this.getMobMaxHp()>100000000 ? (this.getMobMaxHp()/100000000)>100000000 ? this.getMobMaxHp()/100000000/100000000 + "兆" : this.getMobMaxHp()/100000000 + "亿" : this.getMobMaxHp()/10000+"万" : this.getMobMaxHp());
        sb.append("血量, ");
        sb.append(this.getMp());
        sb.append("/ ");
        sb.append(this.getMobMaxMp());
        sb.append(" 魔力, MobOID: ");
        sb.append(this.getObjectId());
        sb.append(" || 仇恨目標 : ");
        MapleCharacter chr = (MapleCharacter)this.controller.get();
        sb.append((chr != null) ? chr.getName() : "無");
        return sb.toString();
    }
    
    @Override
    public final MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }
    
    public void setEventInstance(final EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }
    
    public final EventInstanceManager getEventInstance() {
        return this.eventInstance;
    }
    
    public final int getStatusSourceID(final MonsterStatus status) {
        final MonsterStatusEffect effect = (MonsterStatusEffect)this.stati.get((Object)status);
        if (effect != null) {
            return effect.getSkill();
        }
        return -1;
    }
    
    public final ElementalEffectiveness getEffectiveness(final Element e) {
        if (this.stati.size() > 0 && this.stati.get((Object)MonsterStatus.DOOM) != null) {
            return ElementalEffectiveness.NORMAL;
        }
        return this.stats.getEffectiveness(e);
    }
    
    public void applyMonsterBuff(final Map<MonsterStatus, Integer> effect, final int x, final int skillId, final long duration, final MobSkill skill, final List<Integer> reflection) {
        final MapleCharacter con = this.getController();
        for (final Entry<MonsterStatus, Integer> z : effect.entrySet()) {
            if (this.stati.containsKey((Object)z.getKey())) {
                this.cancelStatus((MonsterStatus)z.getKey());
            }
            final MonsterStatusEffect effectz = new MonsterStatusEffect((MonsterStatus)z.getKey(), z.getValue(), 0, skill, true, reflection.size() > 0);
            effectz.setCancelTask(duration);
            this.stati.put((MonsterStatus)z.getKey(), effectz);
        }
        if (reflection.size() > 0) {
            final List<MonsterStatusEffect> mse = new ArrayList<MonsterStatusEffect>();
            for (final Entry<MonsterStatus, Integer> z2 : effect.entrySet()) {
                mse.add(new MonsterStatusEffect((MonsterStatus)z2.getKey(), z2.getValue(), 0, skill, true, true));
            }
            this.reflectpack = MobPacket.applyMonsterStatus(this, mse);
            if (con != null) {
                this.map.broadcastMessage(con, this.reflectpack, this.getTruePosition());
                con.getClient().getSession().writeAndFlush((Object)this.reflectpack);
            }else {
                this.map.broadcastMessage(this.reflectpack, this.getTruePosition());
            }
        } else {
            for (final Entry<MonsterStatus, Integer> z : effect.entrySet()) {
                final MonsterStatusEffect effectz = new MonsterStatusEffect((MonsterStatus)z.getKey(), z.getValue(), 0, skill, true, reflection.size() > 0);
                if (con != null) {
                    this.map.broadcastMessage(con, MobPacket.applyMonsterStatus(this, effectz), this.getTruePosition());
                    con.getClient().getSession().writeAndFlush((Object)MobPacket.applyMonsterStatus(this, effectz));
                }
                else {
                    this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, effectz), this.getTruePosition());
                }
            }
        }
    }
    
    public void applyStatus(final MapleCharacter from, final MonsterStatusEffect status, final boolean poison, long duration, final boolean checkboss, final MapleStatEffect eff) {
        if (!this.isAlive()) {
            return;
        }
        if ( LtMS.ConfigValuesMap.get((Object)"怪物状态开关") >0 && from.hasGmLevel(5)) {
            String 状态 = "";
            if (status.getStatus() != null) {
                final String name = status.getStati().name();
                int n = -1;
                switch (name.hashCode()) {
                    case 2556090: {
                        if (name.equals((Object)"STUN")) {
                            n = 0;
                            break;
                        }
                        break;
                    }
                    case -1929420024: {
                        if (name.equals((Object)"POISON")) {
                            n = 1;
                            break;
                        }
                        break;
                    }
                    case 79104039: {
                        if (name.equals((Object)"SPEED")) {
                            n = 2;
                            break;
                        }
                        break;
                    }
                    case 2104233: {
                        if (name.equals((Object)"DOOM")) {
                            n = 3;
                            break;
                        }
                        break;
                    }
                    case 2541053: {
                        if (name.equals((Object)"SEAL")) {
                            n = 4;
                            break;
                        }
                        break;
                    }
                    case -534226027: {
                        if (name.equals((Object)"SHADOW_WEB")) {
                            n = 5;
                            break;
                        }
                        break;
                    }
                    case 444279071: {
                        if (name.equals((Object)"SHOWDOWN")) {
                            n = 6;
                            break;
                        }
                        break;
                    }
                    case 2361464: {
                        if (name.equals((Object)"MDEF")) {
                            n = 7;
                            break;
                        }
                        break;
                    }
                    case 2659374: {
                        if (name.equals((Object)"WDEF")) {
                            n = 8;
                            break;
                        }
                        break;
                    }
                }
                switch (n) {
                    case 0: {
                        状态 = "怪物无法移动,[昏迷]，[冰冻]";
                        break;
                    }
                    case 1: {
                        状态 = "怪物持续掉血,[中毒]，[灼烧]";
                        break;
                    }
                    case 2: {
                        状态 = "怪物减少移动速度,[缓速]，[束缚]";
                        break;
                    }
                    case 3: {
                        状态 = "怪物改变外观,[巫毒]，[变身]";
                        break;
                    }
                    case 4: {
                        状态 = "怪物无法使用技能,[封印]，[沉默]";
                        break;
                    }
                    case 5: {
                        状态 = "怪物定身，无法移动,[束缚]，[昏迷]，[定身]";
                        break;
                    }
                    case 6: {
                        状态 = "怪物被激怒,[挑衅]，[诱导]";
                        break;
                    }
                    case 7: {
                        状态 = "怪物防御发生变化,[魔防]";
                        break;
                    }
                    case 8: {
                        状态 = "怪物防御发生变化,[物防]";
                        break;
                    }
                    default: {
                        from.dropMessage(5, "怪物状态: " + status.getStati().name() + "");
                        break;
                    }
                }
            }
            from.dropMessage(5, "怪物状态: " + 状态 + "");
        }
        final ISkill skilz = SkillFactory.getSkill(status.getSkill());
        if (skilz != null) {
            switch (this.stats.getEffectiveness(skilz.getElement())) {
                case IMMUNE:
                case STRONG: {
                    return;
                }
                case NORMAL:
                case WEAK: {
                    break;
                }
                default: {
                    return;
                }
            }
        }
        final int statusSkill = status.getSkill();
        Label_0757: {
            switch (statusSkill) {
                //火毒合击
                case 2111006: {
                    switch (this.stats.getEffectiveness(Element.POISON)) {
                        case IMMUNE:
                        case STRONG: {
                            return;
                        }
                        default: {
                            break Label_0757;
                        }
                    }
                }
                //冰雷合击
                case 2211006: {
                    switch (this.stats.getEffectiveness(Element.ICE)) {
                        case IMMUNE:
                        case STRONG: {
                            return;
                        }
                        default: {
                            break Label_0757;
                        }
                    }
                }
               // 武器用毒液
                case 4120005:
                case 4220005:
                case 14110004: {
                    switch (this.stats.getEffectiveness(Element.POISON)) {
                        case WEAK: {
                            return;
                        }
                        default: {
                            break Label_0757;
                        }
                    }
                }
            }
        }
        if (duration >= 2000000000L) {
            duration = 5000L;
        }
        final MonsterStatus stat = status.getStatus();
        if (this.getId() == 5100002 && stat == MonsterStatus.POISON) {

            return;
        }
        if (this.stats.isNoDoom() && stat == MonsterStatus.DOOM) {

            return;
        }
        if (stat == MonsterStatus.FREEZE) {
            switch (this.getId()) {
                case 9400253:
                case 9400254: {
                    return;
                }
            }
        }
        if (this.stats.isBoss()) {
            //怪物持续掉血,[中毒]，[灼烧]
            if (stat == MonsterStatus.POISON) {
                return;
            }
            //怪物无法移动,[昏迷]，[冰冻]
            if (stat == MonsterStatus.STUN) {
                return;
            }
            //怪物减少移动速度,[缓速]，[束缚]，怪物被忍者伏击,[忍者伏击]   物理攻击
            if (stat != MonsterStatus.SPEED && stat != MonsterStatus.NINJA_AMBUSH && stat != MonsterStatus.WATK) {
                return;
            }
            //怪物魔法防御崩坏,[魔防崩坏]
            if (this.getId() == 8850011 && stat == MonsterStatus.MAGIC_CRASH) {
                return;
            }
            //怪物被冻结,[冻结]
            if (stat == MonsterStatus.FREEZE) {
                return;
            }
        }
        if ((this.stats.isFriendly() || this.isFake()) && (stat == MonsterStatus.STUN || stat == MonsterStatus.SPEED || stat == MonsterStatus.POISON || stat == MonsterStatus.VENOMOUS_WEAPON)) {

            return;
        }
        if ((stat == MonsterStatus.VENOMOUS_WEAPON || stat == MonsterStatus.POISON) && eff == null) {

            return;
        }
        if (this.stati.containsKey((Object)stat)) {

            return;
        }
        if (stat == MonsterStatus.POISON || stat == MonsterStatus.VENOMOUS_WEAPON) {
            this.poisonsLock.readLock().lock();
            try {
                for (final MonsterStatusEffect mse : this.poisons) {
                    if (mse != null && (mse.getSkill() == eff.getSourceId() || mse.getSkill() == GameConstants.getLinkedAttackSkill(eff.getSourceId()) || GameConstants.getLinkedAttackSkill(mse.getSkill()) == eff.getSourceId())) {
                        return;
                    }
                }
            }
            finally {
                this.poisonsLock.readLock().unlock();
            }
        }
        if (poison && this.getHp() > 1L && eff != null) {
            duration = Math.max(duration, (long)(eff.getDOTTime() * 1000));
        }
        final long aniTime;
        duration = (aniTime = duration + (long)(from.getStat().dotTime * 1000));
        status.setCancelTask(aniTime);
        if (poison && this.getHp() > 1L) {
            if (status.getchr() != null) {
                return;
            }
            status.setDotTime(duration);
            final int dam = (int)Math.min(32767L, (long)((double)this.getMobMaxHp() / (70.0 - (double)from.getSkillLevel(status.getSkill())) + 0.999));
            if (from.hasGmLevel(5)) {
                from.dropMessage(6, "[持续伤害] 开始处理效果 - 技能ID：" + eff.getSourceId());
            }
            status.setValue(status.getStatus(), Integer.valueOf(dam));
            status.setPoisonDamage(dam, from);
            final int poisonDamage = (int)(aniTime / 1000L * (long)(int)status.getX());
            if (from.hasGmLevel(5)) {
                from.dropMessage(6, "[持续伤害] 持续伤害： " + (this.getHp() > (long)poisonDamage ? (long)poisonDamage : this.getHp() - 1L) + " 持续时间：" + duration + " 持续掉血：" + status.getX());
            }
        }
        else if (statusSkill == 5211004 && this.getHp() > 1L) {
            if (status.getchr() != null) {
                return;
            }
            status.setDotTime(duration);
            final int dam = (int)Math.min(32767L, (long)((double)this.getMobMaxHp() / (70.0 - (double)from.getSkillLevel(status.getSkill())) + 0.999));
            if (from.isAdmin()) {
                from.dropMessage(6, "[持续伤害] 开始处理效果 - 技能ID：" + eff.getSourceId());
            }
            status.setValue(status.getStatus(), dam);
            status.setPoisonDamage(dam, from);
            final int poisonDamage = (int)(aniTime / 1000L * (long)(int)status.getX());
            if (from.isAdmin()) {
                from.dropMessage(6, "[持续伤害] 持续伤害： " + (this.getHp() > (long)poisonDamage ? (long)poisonDamage : this.getHp() - 1L) + " 持续时间：" + duration + " 持续掉血：" + status.getX());
            }
        }
        else if (statusSkill == 4111003 || statusSkill == 14111001) {
            status.setValue(status.getStatus(), (int) ((double) this.getMobMaxHp() / 50.0 + 0.999));
            status.setPoisonDamage((int)status.getX(), from);
        }
        else if (statusSkill == 4341003) {
            status.setPoisonDamage((int)((double)((float)eff.getDamage() * from.getStat().getCurrentMaxBaseDamage()) / 100.0), from);
        }
        else if (statusSkill == 4121004 || statusSkill == 4221004) {
            status.setValue(status.getStatus(), Math.min(32767, (int) ((double) ((float) eff.getDamage() * from.getStat().getCurrentMaxBaseDamage()) / 100.0)));
            int dam = (int)(aniTime / 1000L * (long)(int)status.getX() / 2L);
            status.setPoisonDamage(dam, from);
            if (dam > 0) {
                if ((long)dam >= this.hp) {
                    dam = (int)(this.hp - 1L);
                }
                this.damage(from, (long)dam, false);
            }
        }
        final MapleCharacter con = this.getController();
        if (stat == MonsterStatus.POISON || stat == MonsterStatus.VENOMOUS_WEAPON) {
            this.poisonsLock.writeLock().lock();
            try {
                this.poisons.add(status);
                status.scheduledoPoison(this);
            }
            finally {
                this.poisonsLock.writeLock().unlock();
            }
        }
        else {
            this.stati.put(stat, status);
        }
        if (con != null) {
            this.map.broadcastMessage(con, MobPacket.applyMonsterStatus(this, status), this.getTruePosition());
            con.getClient().sendPacket(MobPacket.applyMonsterStatus(this, status));
        }
        else {
            this.map.broadcastMessage(MobPacket.applyMonsterStatus(this, status), this.getTruePosition());
        }
        if (from.getDebugMessage()) {
            from.dropMessage(6, "开始 => 給予怪物状态: 持续时间[" + duration + "] 状态效果[" + status.getStatus().name() + "] 开始时间[" + System.currentTimeMillis() + "]");
        }
    }
    
    public void dispelSkill(final MobSkill skillId) {
        final List<MonsterStatus> toCancel = new ArrayList<MonsterStatus>();
        for (final Entry<MonsterStatus, MonsterStatusEffect> effects : this.stati.entrySet()) {
            final MonsterStatusEffect mse = (MonsterStatusEffect)effects.getValue();
            if (mse != null && mse.getMobSkill() != null && mse.getMobSkill().getSkillId() == skillId.getSkillId()) {
                toCancel.add(effects.getKey());
            }
        }
        for (final MonsterStatus stat : toCancel) {
            this.cancelStatus(stat);
        }
    }
    
    public void cancelStatus(final MonsterStatus stat) {
        if (stat == MonsterStatus.EMPTY || stat == MonsterStatus.SUMMON) {
            return;
        }
        final MonsterStatusEffect mse = (MonsterStatusEffect)this.stati.get((Object)stat);
        if (mse == null || !this.isAlive()) {
            return;
        }
        if (mse.isReflect()) {
            this.reflectpack = null;
        }
        mse.cancelPoisonSchedule(this);
        final MapleCharacter con = this.getController();
        if (con != null) {
            this.map.broadcastMessage(con, MobPacket.cancelMonsterStatus(this, mse), this.getTruePosition());
            con.getClient().sendPacket(MobPacket.cancelMonsterStatus(this, mse));
        }
        else {
            this.map.broadcastMessage(MobPacket.cancelMonsterStatus(this, mse), this.getTruePosition());
        }
        this.stati.remove((Object)stat);
    }
    
    public void cancelSingleStatus(final MonsterStatusEffect stat) {
        if (stat == null || stat.getStatus() == MonsterStatus.EMPTY || stat.getStatus() == MonsterStatus.SUMMON || !this.isAlive()) {
            return;
        }
        if (stat.getStatus() != MonsterStatus.POISON && stat.getStatus() != MonsterStatus.VENOMOUS_WEAPON) {
            this.cancelStatus(stat.getStatus());
            return;
        }
        this.poisonsLock.writeLock().lock();
        try {
            if (!this.poisons.contains((Object)stat)) {
                return;
            }
            this.poisons.remove((Object)stat);
            if (stat.isReflect()) {
                this.reflectpack = null;
            }
            stat.cancelPoisonSchedule(this);
            final MapleCharacter con = this.getController();


            if (con != null) {
                this.map.broadcastMessage(con, MobPacket.cancelMonsterStatus(this, stat), this.getTruePosition());
                con.getClient().getSession().writeAndFlush((Object)MobPacket.cancelMonsterStatus(this, stat));
            }
            else {
                this.map.broadcastMessage(MobPacket.cancelMonsterStatus(this, stat), this.getTruePosition());//this.getTruePosition()
            }
        }
        finally {
            this.poisonsLock.writeLock().unlock();
        }
    }
    
    public void doPoison(final MonsterStatusEffect status, final WeakReference<MapleCharacter> weakChr) {
        if ((status.getStatus() == MonsterStatus.VENOMOUS_WEAPON || status.getStatus() == MonsterStatus.POISON || status.getStatus() == MonsterStatus.NEUTRALISE) && this.poisons.size() <= 0) {
            return;
        }
        if (status.getStatus() != MonsterStatus.VENOMOUS_WEAPON && status.getStatus() != MonsterStatus.POISON && status.getStatus() == MonsterStatus.NEUTRALISE && !this.stati.containsKey((Object)status.getStatus())) {
            return;
        }
        if (weakChr == null) {
            return;
        }
        int damage = status.getPoisonDamage();
        final boolean shadowWeb = status.getSkill() == 4111003 || status.getSkill() == 14111001;
        MapleCharacter chr = (MapleCharacter)weakChr.get();
        boolean cancel = damage <= 0 || chr == null || chr.getMapId() != this.map.getId();
        if ((long)damage >= this.hp) {
            damage = (int)this.hp - 1;
            cancel = (!shadowWeb || cancel);
        }
        if (!cancel) {
            this.damage(chr, (long)damage, false);
            if (shadowWeb) {
                this.map.broadcastMessage(MobPacket.damageMonster(this.getObjectId(), (long)damage), this.getTruePosition());
            }
        }
    }
    
    public void setTempEffectiveness(final Element e, final long milli) {
        this.stats.setEffectiveness(e, ElementalEffectiveness.WEAK);
        MobTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                stats.removeEffectiveness(e);
            }
        }, milli);
    }
    
    public final boolean isBuffed(final MonsterStatus status) {
        return this.stati.containsKey((Object)status);
    }
    
    public final MonsterStatusEffect getBuff(final MonsterStatus status) {
        return (MonsterStatusEffect)this.stati.get((Object)status);
    }
    
    public final int getStatiSize() {
        return this.stati.size() + ((this.poisons.size() > 0) ? 1 : 0);
    }
    
    public final ArrayList<MonsterStatusEffect> getAllBuffs() {
        final ArrayList<MonsterStatusEffect> ret = new ArrayList<MonsterStatusEffect>();
        for (final MonsterStatusEffect e : this.stati.values()) {
            ret.add(e);
        }
        this.poisonsLock.readLock().lock();
        try {
            for (final MonsterStatusEffect e : this.poisons) {
                ret.add(e);
            }
        }
        finally {
            this.poisonsLock.readLock().unlock();
        }
        return ret;
    }
    
    public void setFake(final boolean fake) {
        this.fake = fake;
    }
    
    public final boolean isFake() {
        return this.fake;
    }
    
    public final boolean isAlive() {
        return this.hp > 0L;
    }
    
    public boolean isAttackedBy(MapleCharacter chr) {
        for (final AttackerEntry aentry : this.attackers) {
            if (aentry.contains(chr)) {
                return true;
            }
        }
        return false;
    }
    
    public final boolean isFirstAttack() {
        return this.stats.isFirstAttack();
    }
    
    public final List<Pair<Integer, Integer>> getSkills() {
        return this.stats.getSkills();
    }
    
    public final boolean hasSkill(final int skillId, final int level) {
        return this.stats.hasSkill(skillId, level);
    }
    
    public final long getLastSkillUsed(final int skillId) {
        if (this.usedSkills ==null){
            this.usedSkills = new HashMap<Integer, Long>();
            return 0L;
        }
        if (this.usedSkills.containsKey( skillId)) {
            return this.usedSkills.get(skillId);
        }
        return 0L;
    }
    
    public void setLastSkillUsed(final int skillId, final long now, final long cooltime) {
        switch (skillId) {
            case 140: {
                this.usedSkills.put(skillId, now + cooltime * 2L);
                this.usedSkills.put(141, now);
                break;
            }
            case 141: {
                this.usedSkills.put(skillId, now + cooltime * 2L);
                this.usedSkills.put(140, now + cooltime);
                break;
            }
            default: {
                this.usedSkills.put(skillId, now + cooltime);
                break;
            }
        }
    }
    
    public final byte getNoSkills() {
        return this.stats.getNoSkills();
    }
    
    public final int getBuffToGive() {
        return this.stats.getBuffToGive();
    }
    
    public int getLevel() {
        return this.stats.getLevel();
    }
    
    public int getLinkOid() {
        return this.linkoid;
    }
    
    public void setLinkOid(final int lo) {
        this.linkoid = lo;
    }
    
    public final Map<MonsterStatus, MonsterStatusEffect> getStati() {
        return this.stati;
    }
    
    public void addEmpty() {
        for (final MonsterStatus stat : MonsterStatus.values()) {
            if (stat.isDefault()) {
                this.stati.put(stat, new MonsterStatusEffect(stat, Integer.valueOf(0), 0, null, false));
            }
        }
    }
    
    public final int getStolen() {
        return this.stolen;
    }
    
    public void setStolen(final int s) {
        this.stolen = s;
    }
    
    public void handleSteal(MapleCharacter chr) {
        double showdown = 100.0;
        final MonsterStatusEffect mse = this.getBuff(MonsterStatus.SHOWDOWN);
        if (mse != null) {
            showdown += (double)(int)mse.getX();
        }
         ISkill steal = SkillFactory.getSkill(4201004);
         int level = chr.getSkillLevel(steal);
         double chServerrate = ChannelServer.getInstance(chr.getClient().getChannel()).getDropRate();
        if (level > 0 && !this.getStats().isBoss() && this.stolen == -1 && steal.getEffect(level).makeChanceResult()) {
            final MapleMonsterInformationProvider mi = MapleMonsterInformationProvider.getInstance();
            final List<MonsterDropEntry> de = mi.retrieveDrop(this.getId());
            if (de == null) {
                this.stolen = 0;
                return;
            }
            final List<MonsterDropEntry> dropEntry = new ArrayList<MonsterDropEntry>((Collection<? extends MonsterDropEntry>)de);
            Collections.shuffle(dropEntry);
            for (final MonsterDropEntry d : dropEntry) {
                if (d.itemId > 0 && d.questid == 0 && d.itemId / 10000 != 238 && Randomizer.nextInt(999999) < (int)((double)(10 * d.chance * chServerrate * chr.getDropMod()) * chr.getDropm() * ((double)chr.getVipExpRate() / 100.0 + 1.0) * (chr.getStat().dropBuff / 100.0) * (showdown / 100.0))) {
                    IItem idrop;
                    if (GameConstants.getInventoryType(d.itemId) == MapleInventoryType.EQUIP) {
                        final Equip eq = (Equip)MapleItemInformationProvider.getInstance().getEquipById(d.itemId);
                        idrop = MapleItemInformationProvider.getInstance().randomizeStats(eq);
                    }
                    else {
                        idrop = new Item(d.itemId, (short)0, (short)((d.Maximum != 1) ? (Randomizer.nextInt(d.Maximum - d.Minimum) + d.Minimum) : 1), (byte)0);
                    }
                    this.stolen = d.itemId;
                    this.map.spawnMobDrop(idrop, this.map.calcDropPos(this.getPosition(), this.getTruePosition()), this, chr, (byte)0, (short)0);
                    break;
                }
            }
        }
        else {
            this.stolen = 0;
        }
    }
    
    public void setLastNode(final int lastNode) {
        this.lastNode = lastNode;
    }
    
    public final int getLastNode() {
        return this.lastNode;
    }
    
    public void setLastNodeController(final int lastNode) {
        this.lastNodeController = lastNode;
    }
    
    public final int getLastNodeController() {
        return this.lastNodeController;
    }
    
    public void cancelDropItem() {
        if (this.dropItemSchedule != null) {
            this.dropItemSchedule.cancel(false);
            this.dropItemSchedule = null;
        }
    }
    
    public void startDropItemSchedule() {
        this.cancelDropItem();
        if (this.stats.getDropItemPeriod() <= 0 || !this.isAlive()) {
            return;
        }
        int itemId = 0;
        switch (this.getId()) {
            case 9300061: {
                itemId = 4001101;
                break;
            }
            case 9300102: {
                itemId = 4031507;
                break;
            }
            default: {
                return;
            }
        }
        final  int itemId2 = itemId;
        this.shouldDropItem = false;
        this.dropItemSchedule = MobTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (MapleMonster.this.isAlive() && map != null) {
                    if (shouldDropItem) {
                        map.spawnAutoDrop(itemId2, MapleMonster.this.getPosition());
                    }
                    else {
                        shouldDropItem = true;
                    }
                }
            }
        }, (long)(this.stats.getDropItemPeriod() * 1000));
    }
    
    public byte[] getNodePacket() {
        return this.nodepack;
    }
    
    public void setNodePacket(final byte[] np) {
        this.nodepack = np;
    }
    
    public void killed() {
        if (this.listener != null) {
            this.listener.monsterKilled();
        }
        this.listener = null;
    }

    private final class PoisonTask implements Runnable
    {
        private final int poisonDamage;
        private MapleCharacter chr;
        private final MonsterStatusEffect status;
        private final Runnable cancelTask;
        private final boolean shadowWeb;
        private final MapleMap map;
        
        private PoisonTask(final int poisonDamage, MapleCharacter chr, final MonsterStatusEffect status, final Runnable cancelTask, final boolean shadowWeb) {
            this.poisonDamage = poisonDamage;
            this.chr = chr;
            this.status = status;
            this.cancelTask = cancelTask;
            this.shadowWeb = shadowWeb;
            this.map = chr.getMap();
        }
        
        @Override
        public void run() {
            long damage = (long)this.poisonDamage;
            if (damage >= hp) {
                damage = hp - 1L;
                if (!this.shadowWeb) {
                    this.cancelTask.run();
                    this.status.cancelTask();
                }
            }
            if (hp > 1L && damage > 0L) {
                MapleMonster.this.damage(this.chr, damage, false);
                if (this.shadowWeb) {
                    this.map.broadcastMessage(MobPacket.damageMonster(MapleMonster.this.getObjectId(), damage), MapleMonster.this.getPosition());
                }
            }
        }
    }
    
    private static class AttackingMapleCharacter
    {
        private final WeakReference<MapleCharacter> attacker;
        private long lastAttackTime;
        
        public AttackingMapleCharacter(final MapleCharacter attacker, final long lastAttackTime) {
            this.attacker = new WeakReference<MapleCharacter>(attacker);
            this.lastAttackTime = lastAttackTime;
        }
        
        public final long getLastAttackTime() {
            return this.lastAttackTime;
        }
        
        public void setLastAttackTime(final long lastAttackTime) {
            this.lastAttackTime = lastAttackTime;
        }
        
        public final MapleCharacter getAttacker() {
            return (MapleCharacter)this.attacker.get();
        }
    }
    
    private final class SingleAttackerEntry implements AttackerEntry
    {
        private long damage;
        private final int chrid;
        private long lastAttackTime;
        private final int channel;
        
        public SingleAttackerEntry(final MapleCharacter from, final int cserv) {
            this.damage = 0L;
            this.chrid = from.getId();
            this.channel = cserv;
        }
        
        @Override
        public void addDamage(final MapleCharacter from, final long damage, final boolean updateAttackTime) {
            if (this.chrid == from.getId()) {
                this.damage += damage;
                if (updateAttackTime) {
                    this.lastAttackTime = System.currentTimeMillis();
                }
            }
        }
        
        @Override
        public final List<AttackingMapleCharacter> getAttackers() {
            MapleCharacter chr = map.getCharacterById(this.chrid);
            if (chr != null) {
                return Collections.singletonList(new AttackingMapleCharacter(chr, this.lastAttackTime));
            }
            return Collections.emptyList();
        }
        
        @Override
        public boolean contains(MapleCharacter chr) {
            return this.chrid == chr.getId();
        }
        
        @Override
        public long getDamage() {
            return this.damage;
        }
        
        @Override
        public void killedMob(final MapleMap map, final int baseExp, final boolean mostDamage, final int lastSkill) {
            MapleCharacter chr = map.getCharacterById(this.chrid);
            if (chr != null && chr.isAlive()) {
                MapleMonster.this.giveExpToCharacter(chr, baseExp, mostDamage, 1, (byte)0, (byte)0, (byte)0, lastSkill);
            }
        }
        
        @Override
        public int hashCode() {
            return this.chrid;
        }
        
        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final SingleAttackerEntry other = (SingleAttackerEntry)obj;
            return this.chrid == other.chrid;
        }
    }
    
    private static final class ExpMap
    {
        public final int exp;
        public final byte ptysize;
        public final byte Class_Bonus_EXP;
        public final byte Premium_Bonus_EXP;
        
        public ExpMap(final int exp, final byte ptysize, final byte Class_Bonus_EXP, final byte Premium_Bonus_EXP) {
            this.exp = exp;
            this.ptysize = ptysize;
            this.Class_Bonus_EXP = Class_Bonus_EXP;
            this.Premium_Bonus_EXP = Premium_Bonus_EXP;
        }
    }
    
    private static final class OnePartyAttacker
    {
        public MapleParty lastKnownParty;
        public long damage;
        public long lastAttackTime;
        
        public OnePartyAttacker(final MapleParty lastKnownParty, final long damage) {
            this.lastKnownParty = lastKnownParty;
            this.damage = damage;
            this.lastAttackTime = System.currentTimeMillis();
        }
    }
    
    private class PartyAttackerEntry implements AttackerEntry
    {
        private long totDamage;
        private final Map<Integer, OnePartyAttacker> attackers;
        private final int partyid;
        private final int channel;
        
        public PartyAttackerEntry(final int partyid, final int cserv) {
            this.attackers = new HashMap<Integer, OnePartyAttacker>(6);
            this.partyid = partyid;
            this.channel = cserv;
        }
        
        @Override
        public List<AttackingMapleCharacter> getAttackers() {
            final List<AttackingMapleCharacter> ret = new ArrayList<AttackingMapleCharacter>(this.attackers.size());
            for (final Entry<Integer, OnePartyAttacker> entry : this.attackers.entrySet()) {
                MapleCharacter chr = map.getCharacterById((int)Integer.valueOf(entry.getKey()));
                if (chr != null) {
                    ret.add(new AttackingMapleCharacter(chr, ((OnePartyAttacker)entry.getValue()).lastAttackTime));
                }
            }
            return ret;
        }
        
        private Map<MapleCharacter, OnePartyAttacker> resolveAttackers() {
            final Map<MapleCharacter, OnePartyAttacker> ret = new HashMap<MapleCharacter, OnePartyAttacker>(this.attackers.size());
            for (final Entry<Integer, OnePartyAttacker> aentry : this.attackers.entrySet()) {
                MapleCharacter chr = map.getCharacterById((int)Integer.valueOf(aentry.getKey()));
                if (chr != null) {
                    ret.put(chr, aentry.getValue());
                }
            }
            return ret;
        }
        
        @Override
        public final boolean contains(MapleCharacter chr) {
            return this.attackers.containsKey((Object)Integer.valueOf(chr.getId()));
        }
        
        @Override
        public final long getDamage() {
            return this.totDamage;
        }
        
        @Override
        public void addDamage(final MapleCharacter from, final long damage, final boolean updateAttackTime) {
            final OnePartyAttacker oldPartyAttacker = (OnePartyAttacker)this.attackers.get((Object)Integer.valueOf(from.getId()));
            if (oldPartyAttacker != null) {
                final OnePartyAttacker onePartyAttacker2 = oldPartyAttacker;
                onePartyAttacker2.damage += damage;
                oldPartyAttacker.lastKnownParty = from.getParty();
                if (updateAttackTime) {
                    oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
                }
            }
            else {
                final OnePartyAttacker onePartyAttacker = new OnePartyAttacker(from.getParty(), damage);
                this.attackers.put(Integer.valueOf(from.getId()), onePartyAttacker);
                if (!updateAttackTime) {
                    onePartyAttacker.lastAttackTime = 0L;
                }
            }
            this.totDamage += damage;
        }
        
        @Override
        public void killedMob(final MapleMap map,  int baseExp, final boolean mostDamage, final int lastSkill) {
            MapleCharacter highest = null;
            long highestDamage = 0L;
            final Map<MapleCharacter, ExpMap> expMap = new HashMap<MapleCharacter, ExpMap>(6);
            byte added_partyinc = 0;
            for (final Entry<MapleCharacter, OnePartyAttacker> attacker : this.resolveAttackers().entrySet()) {
                final MapleParty party = ((OnePartyAttacker)attacker.getValue()).lastKnownParty;
                double averagePartyLevel = 0.0;
                byte Class_Bonus_EXP = 0;
                byte Premium_Bonus_EXP = 0;
                final List<MapleCharacter> expApplicable = new ArrayList<MapleCharacter>();
                for (final MaplePartyCharacter partychar : party.getMembers()) {
                    if (((MapleCharacter)attacker.getKey()).getLevel() - partychar.getLevel() <= 5 || stats.getLevel() - partychar.getLevel() <= 5) {
                        final MapleCharacter pchr = map.getCharacterById(partychar.getId());
                        if (pchr == null || !pchr.isAlive() || pchr.getMap() != map) {
                            continue;
                        }
                        expApplicable.add(pchr);
                        averagePartyLevel += (double)pchr.getLevel();
                        if (Class_Bonus_EXP == 0) {}
                        if (pchr.getStat().equippedWelcomeBackRing && Premium_Bonus_EXP == 0) {
                            Premium_Bonus_EXP = 80;
                        }
                        if (!pchr.getStat().hasPartyBonus || added_partyinc >= 4) {
                            continue;
                        }
                        ++added_partyinc;
                    }
                }
                if (expApplicable.size() > 1) {
                    averagePartyLevel /= (double)expApplicable.size();
                }
                else {
                    Class_Bonus_EXP = 0;
                }
                final long iDamage = ((OnePartyAttacker)attacker.getValue()).damage;
                if (iDamage > highestDamage) {
                    highest = (MapleCharacter)attacker.getKey();
                    highestDamage = iDamage;
                }
                final double innerBaseExp = (double)baseExp * ((double)iDamage / (double)this.totDamage);
                final double expFraction = innerBaseExp / (double)expApplicable.size();
                for (final MapleCharacter expReceiver : expApplicable) {
                    //2024-1-25修改,修正经验
                    int iexp = (expMap.get(expReceiver) == null) ? 0 : ((ExpMap)expMap.get(expReceiver)).exp;
                    final double expWeight = (expReceiver == attacker.getKey()) ? ((double)(LtMS.ConfigValuesMap.get("修正组队分配经验"))) : ((double)(LtMS.ConfigValuesMap.get("修正队员分配经验")));
                    double levelMod = (double)expReceiver.getLevel() / averagePartyLevel;
                    if (levelMod > 1.0 || this.attackers.containsKey(expReceiver.getId())) {
                        levelMod = 1.0;
                    }

                     iexp += (int)Math.round(expFraction * expWeight * levelMod / 100.0);
                    expMap.put(expReceiver, new ExpMap(iexp, (byte)(expApplicable.size() + added_partyinc), Class_Bonus_EXP, Premium_Bonus_EXP));

                }
            }
            for (final Entry<MapleCharacter, ExpMap> expReceiver2 : expMap.entrySet()) {
                final ExpMap expmap = (ExpMap)expReceiver2.getValue();
                try {
                    MapleMonster.this.giveExpToCharacter((MapleCharacter)expReceiver2.getKey(), expmap.exp, mostDamage && expReceiver2.getKey() == highest, expMap.size(), expmap.ptysize, expmap.Class_Bonus_EXP, expmap.Premium_Bonus_EXP, lastSkill);
                } catch (Exception e) {
                   //e.printStackTrace();
                }
            }
        }
        
        @Override
        public final int hashCode() {
            final int prime = 31;
            int result = 1;
            result = 31 * result + this.partyid;
            return result;
        }
        
        @Override
        public final boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            final PartyAttackerEntry other = (PartyAttackerEntry)obj;
            return this.partyid == other.partyid;
        }
    }
    
    private interface AttackerEntry
    {
        List<AttackingMapleCharacter> getAttackers();
        
        void addDamage(final MapleCharacter p0, final long p1, final boolean p2);
        
        long getDamage();
        
        boolean contains(final MapleCharacter p0);
        
        void killedMob(final MapleMap p0, final int p1, final boolean p2, final int p3);
    }
    
    public final int getSpawnChrid(){
         return stats.getSpawnChrId();
}


    public void damagefj(int hp) {
        map.broadcastMessage(MobPacket.healMonster(getObjectId(), hp));
    }
    public final void sendBlueDamage(long hp, boolean trueDamage) {
        if (hp > 2147483647L) {
            this.map.broadcastMessage(MobPacket.healMonster(this.getObjectId(), Integer.MAX_VALUE));
        } else {
            this.map.broadcastMessage(MobPacket.healMonster(this.getObjectId(), (int)hp));
        }

        if (trueDamage) {
            if (hp >= this.getHp()) {
                this.getMap().killMonster(this, true);
            } else {
                this.setHp(this.getHp() - hp);
            }
        }

    }

    public final void spawnRevives(MapleMap map) {
        List<Integer> toSpawn = this.stats.getRevives();
        if (toSpawn != null) {
            MapleMonster spongy = null;
            Iterator var4;
            int i;
            MapleMonster mob;
            switch (this.getId()) {
                case 8810026:
                case 8810130:
                case 8820008:
                case 8820009:
                case 8820010:
                case 8820011:
                case 8820012:
                case 8820013:
                    List<MapleMonster> mobs = new ArrayList();
                    Iterator var12 = toSpawn.iterator();

                    while(var12.hasNext()) {
                        i = (Integer)var12.next();
                        mob = MapleLifeFactory.getMonster(i);
                        if (this.isMonitor()) {
                            if (mob.getId() != 8810018 && mob.getId() != 8820001) {
                                this.mobDamageData.addMonster(mob, false);
                            } else {
                                this.mobDamageData.addMonster(mob, true);
                            }
                        }

                        mob.setPosition(this.getPosition());
                        if (this.eventInstance != null) {
                            this.eventInstance.registerMonster(mob);
                        }

                        if (this.dropsDisabled()) {
                            mob.disableDrops();
                        }

                        switch (mob.getId()) {
                            case 8810018:
                            case 8810118:
                            case 8820009:
                            case 8820010:
                            case 8820011:
                            case 8820012:
                            case 8820013:
                            case 8820014:
                                spongy = mob;
                                break;
                            default:
                                mobs.add(mob);
                        }
                    }

                    if (spongy != null) {
                        map.spawnRevives(spongy, this.getObjectId());
                        var12 = mobs.iterator();

                        while(var12.hasNext()) {
                            mob = (MapleMonster)var12.next();
                            mob.setSponge(spongy);
                            map.spawnRevives(mob, this.getObjectId());
                        }
                    }
                    break;
                case 8810118:
                case 8810119:
                case 8810120:
                case 8810121:
                    var4 = toSpawn.iterator();

                    while(var4.hasNext()) {
                        i = (Integer)var4.next();
                        mob = MapleLifeFactory.getMonster(i);
                        mob.setPosition(this.getPosition());
                        if (this.eventInstance != null) {
                            this.eventInstance.registerMonster(mob);
                        }

                        if (this.dropsDisabled()) {
                            mob.disableDrops();
                        }

                        switch (mob.getId()) {
                            case 8810119:
                            case 8810120:
                            case 8810121:
                            case 8810122:
                                spongy = mob;
                        }
                    }

                    if (spongy != null) {
                        map.spawnRevives(spongy, this.getObjectId());
                        var4 = map.getAllMonstersThreadsafe().iterator();

                        while(true) {
                            do {
                                do {
                                    if (!var4.hasNext()) {
                                        return;
                                    }

                                    MapleMapObject mon = (MapleMapObject)var4.next();
                                    mob = (MapleMonster)mon;
                                } while(mob.getObjectId() == spongy.getObjectId());
                            } while(mob.getSponge() != this && mob.getLinkOid() != this.getObjectId());

                            mob.setSponge(spongy);
                            mob.setLinkOid(spongy.getObjectId());
                        }
                    }
                    break;
                default:
                    if (map.getId() == 280030002) {
                        var4 = toSpawn.iterator();

                        while(var4.hasNext()) {
                            i = (Integer)var4.next();
                            MapleLifeFactory.deleteStats(i);
                            mob = MapleLifeFactory.getMonster(i);
                            switch (i) {
                                case 8800000:
                                    mob.getStats().setName("进阶扎昆1");
                                    mob.getStats().addSkill(145, 2);
                                    mob.getStats().addSkill(123, 12);
                                    mob.getStats().addSkill(127, 4);
                                    mob.getStats().addSkill(128, 10);
                                    mob.getStats().addSkill(132, 2);
                                    break;
                                case 8800001:
                                    mob.getStats().setName("进阶扎昆2");
                                    mob.getStats().addSkill(145, 2);
                                    mob.getStats().addSkill(145, 2);
                                    mob.getStats().addSkill(123, 12);
                                    mob.getStats().addSkill(123, 19);
                                    mob.getStats().addSkill(127, 4);
                                    mob.getStats().addSkill(128, 10);
                                    mob.getStats().addSkill(132, 2);
                                    break;
                                case 8800002:
                                    mob.getStats().setName("进阶扎昆");
                                    mob.getStats().addSkill(145, 2);
                                    mob.getStats().addSkill(145, 2);
                                    mob.getStats().addSkill(145, 2);
                                    mob.getStats().addSkill(123, 12);
                                    mob.getStats().addSkill(123, 19);
                                    mob.getStats().addSkill(123, 19);
                                    mob.getStats().addSkill(127, 4);
                                    mob.getStats().addSkill(128, 10);
                                    mob.getStats().addSkill(132, 2);
                                    mob.getStats().addSkill(128, 10);
                            }

                            mob.getStats().setLevel((short)180);
                            mob.getStats().setExp((int)(mob.getExp() * 5L));
                            mob.getStats().setHp(mob.getHp() * (long)(Integer)LtMS.ConfigValuesMap.get("进阶扎昆血量倍数"));
                            mob.getStats().setMp(mob.getMp() * 15);
                            mob.getStats().setEva((short)(mob.getStats().getEva() * 10));
                            mob.getStats().setPhysicalDefense((short)(mob.getStats().getPhysicalDefense() * 10));
                            mob.getStats().setMagicDefense((short)(mob.getStats().getMagicDefense() * 10));
                            mob.getStats().setFixedDamage(mob.getStats().getFixedDamage() * 10);
                            MapleLifeFactory.addStats(i, mob.getStats());
                        }
                    }

                    var4 = toSpawn.iterator();

                    while(var4.hasNext()) {
                        i = (Integer)var4.next();
                        mob = MapleLifeFactory.getMonster(i);
                        if (this.eventInstance != null) {
                            this.eventInstance.registerMonster(mob);
                        }

                        mob.setPosition(this.getPosition());
                        if (this.dropsDisabled()) {
                            mob.disableDrops();
                        }

                        if (this.monitor && this.mobDamageData != null && !GameConstants.isFakeRevive(mob.getId())) {
                            if (this.getId() != this.mobDamageData.getMainMobId() && mob.getId() != 8820001) {
                                this.mobDamageData.addMonster(mob, false);
                            } else {
                                this.mobDamageData.addMonster(mob, true);
                            }
                        }

                        map.spawnRevives(mob, this.getObjectId());
//                        if (this.eventInstance == null && !mob.getStats().isBoss() && (this.getMap().haveMonster(9900000) || this.getMap().haveMonster(9900001) && this.getMap().haveMonster(9900002))) {
//                            int rateByStone;
//                            if (this.getMap().getStoneLevel() == 1) {
//                                rateByStone = (Integer)LtMS.ConfigValuesMap.get("1级轮回碑石怪物倍数");
//                            } else if (this.getMap().getStoneLevel() >= 2) {
//                                rateByStone = (Integer)LtMS.ConfigValuesMap.get("2级轮回碑石怪物倍数");
//                            } else {
//                                rateByStone = (Integer)LtMS.ConfigValuesMap.get("轮回碑石怪物倍数");
//                            }
//
//                            for(int j = 0; j < rateByStone; ++j) {
//                                MapleMonster mob2 = MapleLifeFactory.getMonster(i);
//                                mob2.setPosition(this.getPosition());
//                                if (this.dropsDisabled()) {
//                                    mob2.disableDrops();
//                                }
//
//                                map.spawnRevives(mob2, this.getObjectId());
//                            }
//                        }

                        if (mob.getId() == 9300216) {
                            map.broadcastMessage(MaplePacketCreator.environmentChange("Dojang/clear", 4));
                            map.broadcastMessage(MaplePacketCreator.environmentChange("dojang/end/clear", 3));
                        }

                        if (map.getId() == 280030002) {
                            MapleLifeFactory.deleteStats(i);
                        }
                    }
            }

        }
    }

    public final void sendYellowDamage(long hp, MapleCharacter chr) {
        if (hp > 2147483647L) {
            this.map.broadcastMessage(MobPacket.damageMonster(this.getObjectId(), Integer.MAX_VALUE));
        } else {
            this.map.broadcastMessage(MobPacket.damageMonster(this.getObjectId(), hp));
        }

        this.damage(chr, hp, false);
    }

    public final void sendBlueDamage(long hp, MapleCharacter chr) {
        if (hp > 2147483647L) {
            this.map.broadcastMessage(MobPacket.healMonster(this.getObjectId(), Integer.MAX_VALUE));
        } else {
            this.map.broadcastMessage(MobPacket.healMonster(this.getObjectId(), (int)hp));
        }

        this.damage(chr, hp, false);
    }

    public final void sendYellowDamage(long hp, boolean trueDamage) {
        if (hp > 2147483647L) {
            this.map.broadcastMessage(MobPacket.damageMonster(this.getObjectId(), Integer.MAX_VALUE));
        } else {
            this.map.broadcastMessage(MobPacket.damageMonster(this.getObjectId(), hp));
        }

        if (trueDamage) {
            if (hp >= this.getHp()) {
                this.getMap().killMonster(this, true);
            } else {
                this.setHp(this.getHp() - hp);
            }
        }

    }


    public void sendAttack(MapleCharacter chr, int skill, int damage, int fake, byte direction, int reflect, boolean is_pg, int pos_x, int pos_y, boolean repeatToSource) {
        chr.getMap().broadcastMessage(chr, MaplePacketCreator.damagePlayer(skill, this.getId(), chr.getId(), damage, fake, direction, reflect, is_pg, this.getObjectId(), pos_x, pos_y), repeatToSource);
    }

    public void sendSkill(MapleCharacter chr, int skill, int unk2) {
        chr.getMap().broadcastMessage(chr, MobPacket.moveMonster(true, skill, unk2, this.getObjectId(), this.getPosition(), this.getPosition(), this.getController().getLastRes()), this.getPosition());
    }

    public void sendSkillResPose(MapleCharacter chr, short moveId, int skillId, int skillLevel, boolean isControllerHasAggro, boolean repeatToSource) {
        this.getMap().broadcastMessage(chr, MobPacket.moveMonsterResponse(this.getObjectId(), moveId, this.getMp(), isControllerHasAggro, skillId, skillLevel), repeatToSource);
        this.setControllerHasAggro(true);
    }



}
