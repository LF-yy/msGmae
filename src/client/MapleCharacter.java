package client;

import abc.Game;
import abc.离线人偶;
import bean.LtDiabloEquipments;
import bean.UserAttraction;
import bean.UserLhAttraction;
import client.inventory.*;
import gui.LtMS;
import gui.服务端输出信息;
import org.apache.commons.lang.StringUtils;
import scripting.EventManager;
import server.*;
import server.Timer.EventTimer;
import server.life.*;
import server.maps.*;
import server.shops.HiredMerchant;
import snail.TimeLogCenter;
import handling.world.World.Find;
import constants.MapConstants;
import constants.ServerConfig;
import handling.world.family.MapleFamily;
import handling.world.family.MapleFamilyBuff;
import handling.world.family.MapleFamilyBuff.MapleFamilyBuffEntry;
import scripting.NPCScriptManager;
import constants.ServerConstants.PlayerGMRank;
import handling.cashshop.CashShopServer;
import handling.login.LoginServer;
import handling.world.World;
import handling.world.PlayerBuffStorage;
import handling.world.MapleMessengerCharacter;
import io.netty.channel.Channel;
import snail.*;
import tools.*;

import java.util.*;

import client.inventory.MapleRing.RingComparator;
import tools.packet.MonsterCarnivalPacket;
import tools.packet.PlayerShopPacket;
import database.DatabaseConnection;
import fumo.FumoSkill;
import handling.world.World.Guild;
import handling.world.guild.MapleGuild;

import java.sql.Statement;
import java.sql.Timestamp;

import scripting.NPCConversationManager;
import tools.packet.MobPacket;
import handling.world.PartyOperation;
import handling.world.World.Family;
import tools.packet.MTSCSPacket;
import handling.world.MaplePartyCharacter;
import handling.world.PlayerBuffValueHolder;
import tools.packet.PetPacket;

import java.util.concurrent.*;

import server.FishingRewardFactory.FishingReward;
import handling.world.World.Broadcast;
import tools.packet.UIPacket;
import server.Timer.EtcTimer;
import server.Timer.MapTimer;
import server.Timer.BuffTimer;

import tools.data.MaplePacketLittleEndianWriter;
import database.DatabaseException;

import constants.GameConstants;
import java.util.Map.Entry;
import handling.world.World.Party;
import handling.world.World.Messenger;
import handling.channel.ChannelServer;
import handling.world.CharacterTransfer;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;

import database.DBConPool;

import scripting.EventInstanceManager;
import handling.world.family.MapleFamilyCharacter;
import handling.world.guild.MapleGuildCharacter;
import handling.world.MapleParty;
import server.shops.IMaplePlayerShop;
import handling.world.MapleMessenger;
import client.anticheat.CheatTracker;

import server.quest.MapleQuest;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.lang.ref.WeakReference;

import server.movement.LifeMovementFragment;

import java.util.concurrent.atomic.AtomicInteger;
import java.awt.Point;
import java.io.Serializable;

public class MapleCharacter extends AbstractAnimatedMapleMapObject implements Serializable
{
    private static final long serialVersionUID = 845748950829L;
    public SkillSkin skillSkin = new SkillSkin(this, 0);
    public int id;
    private String name;
    private String chalktext;
    private String BlessOfFairy_Origin;
    private String charmessage;
    private String prefix;
    private String teleportname;
    private String nowmacs;
    private String loginkey;
    private String serverkey;
    private String clientkey;
    private String accountsecondPassword;
    private long lastCombo;
    private long lastfametime;
    private long keydown_skill;
    private long lastRecoveryTime;
    private long nextConsume;
    private long pqStartTime;
    private long lastHPTime;
    private long lastMPTime;
    private long lastMDTime;
    private long lastStorageTime;
    private long mapChangeTime;
    private long mrqdTime;
    private byte dojoRecord;
    private byte gmLevel;
    private byte gender;
    private byte initialSpawnPoint;
    private byte skinColor;
    private byte guildrank;
    private byte allianceRank;
    private byte world;
    private byte fairyExp;
    private byte numClones;
    private byte subcategory;
    private byte fairyHour;
    public short level;
    public short mulung_energy;
    public short availableCP;
    public short totalCP;
    public short fame;
    public short hpmpApUsed;
    public short job;
    public short remainingAp;
    private int accountid;

    private int meso;
    private int exp;
    private int hair;
    private int face;
    private int mapid;
    private int bookCover;
    private int dojo;
    private Map<Integer, Pair<Long, Integer>> noCancelBuffMap = new HashMap<>();
    private int guildid;
    private int fallcounter;
    private int maplepoints;
    private int chair;
    private int itemEffect;
    private int vpoints;
    private int rank;
    private int rankMove;
    private int jobRank;
    private int jobRankMove;
    private int marriageId;
    private int marriageItemId;
    private int currentrep;
    private int totalrep;
    private int linkMid;
    private int coconutteam;
    private int followid;
    private int battleshipHP;
    private int expression;
    private int constellation;
    private int blood;
    private int month;
    private int day;
    private int beans;
    private int beansNum;
    private int beansRange;
    private int PGSXDJ;
    private int gachexp;
    private int combo;
    private int MSG;
    private int 打怪;
    private int 吸怪;
    private int FLY_吸怪;
    private int vip;
    private int CsMod;
    private int 在线时间;
    private long 被驱散时间;
    private long 被诱导时间;
    private long 被封印时间;
    private long 物理无效时间;
    private long 魔法无效时间;
    private Point old;
    private boolean smega;
    private boolean gashponmega;
    private boolean hidden;
    private boolean hasSummon;
    private boolean 精灵商人购买开关;
    private boolean 玩家私聊开关;
    private boolean 玩家密语开关;
    private boolean 好友聊天开关;
    private boolean 队伍聊天开关;
    private boolean 公会聊天开关;
    private boolean 联盟聊天开关;
    private boolean GM吸怪信息开关;
    private boolean 开启自动回收;
    private boolean canSetBeansNum;
    private boolean Vip_Medal;
    private boolean auto吸怪;
    private boolean DebugMessage;
    private boolean itemVacs;
    private boolean mobVacs;
    private boolean mobLhVacs;
    private boolean mobMapVacs;
    private boolean beansStart;
    private int[] wishlist;
    private int[] rocks;
    private int[] savedLocations;
    private int[] regrocks;
    private int[] remainingSp;
    private int[] savedHairs;
    private int[] savedFaces;
    private transient AtomicInteger inst;
    private transient List<LifeMovementFragment> lastres;
    private transient List<LifeMovementFragment> lastresOld;
    private List<Integer> lastmonthfameids;
    private List<MapleDoor> doors;
    private List<MaplePet> pets;
    private transient WeakReference<MapleCharacter>[] clones;
    private transient Set<MapleMonster> controlled;
    private transient Set<MapleMapObject> visibleMapObjects;
    private transient ReentrantReadWriteLock visibleMapObjectsLock;
    private final Map<MapleQuest, MapleQuestStatus> quests;
    private Map<Integer, String> questinfo;
    private final Map<ISkill, SkillEntry> skills;
    private final transient Map<MapleBuffStat, MapleBuffStatValueHolder> effects;
    private final transient Map<Integer, MapleBuffStatValueHolder> skillID;
    private transient Map<Integer, MapleSummon> summons;
    private final transient Map<Integer, MapleCoolDownValueHolder> coolDowns;
    private final transient Map<MapleDisease, MapleDiseaseValueHolder> diseases;
    private CashShop cs;
    private transient Deque<MapleCarnivalChallenge> pendingCarnivalRequests;
    private transient MapleCarnivalParty carnivalParty;
    private BuddyList buddylist;
    private MonsterBook monsterbook;
    private transient CheatTracker anticheat;
    private transient MapleLieDetector antiMacro;
    private MapleClient client;
    private PlayerStats stats;
    private boolean isburnd = false;
    private transient PlayerRandomStream CRand;
    private transient MapleMap map;
    private transient MapleShop shop;
    private transient RockPaperScissors rps;
    private MapleStorage storage;
    private transient MapleTrade trade;
    private MapleMount mount;
    private final List<Integer> finishedAchievements;
    private MapleMessenger messenger;
    private byte[] petStore;
    private transient IMaplePlayerShop playerShop;
    private MapleParty party;
    private boolean invincible = false;
    private boolean canTalk = true;
    private boolean clone = false;
    private boolean followinitiator = false;
    private boolean followon = false;
    private MapleGuildCharacter mgc;
    private MapleFamilyCharacter mfc;
    private transient EventInstanceManager eventInstance;
    private MapleInventory[] inventory;
    private SkillMacro[] skillMacros;
    private MapleKeyLayout keylayout;
    private ItemVac ItemVac;
    private MobVac mobVac;
    private MobLhVac mobLhVac;
    private MobMapVac mobMapVac;
    private transient ScheduledFuture<?> beholderHealingSchedule;
    private transient ScheduledFuture<?> beholderBuffSchedule;
    private transient ScheduledFuture<?> BerserkSchedule;
    private transient ScheduledFuture<?> dragonBloodSchedule;
    private transient ScheduledFuture<?> fairySchedule;
    private transient ScheduledFuture<?> mapTimeLimitTask;
    private transient ScheduledFuture<?> fishing;
    private transient Event_PyramidSubway pyramidSubway;
    private transient List<Integer> pendingExpiration;
    private transient List<Integer> pendingSkills;
    private final transient Map<Integer, Integer> movedMobs;
    private int jf;
    private int zdjf;
    private int rwjf;
    private int cz;
    private int zs;
    private int dy;
    private int rmb;
    private int yb;
    private long lasttime;
    private long currenttime;
    private MapleCharacter chars;
    public int apprentice;
    public int master;
    public int ariantScore;
    private long 防止复制时间;
    private boolean isbanned;
    public static int 记录当前血量;
    public static int 角色无敌指数;
    public static int 地图记录;
    private static String[] ariantroomleader ;
    private static int[] ariantroomslot ;
    /**
     * 吸怪怪值
     */
    public  int attractValue;
    /**
     * 吸怪误检测次数
     */
    public  int noAttractValue;
    public static long 记录范围;
    public static int 记录技能;
    public static int 技能检测惩罚;
    public static int 登陆验证;
    public int 鱼来鱼往;
    public long 蓄力一击;
    public long 越战越勇;
    public long prevTimestamp1;
    public long 持续对话NPC;
    public long 宠物捡物冷却;
    public long 怪物移动冷却;
    public long 玩家捡物冷却;
    public long 上线提醒冷却;
    public long 整理背包冷却;
    public long 对话冷却;
    public long NPC对话冷却;
    public long 吸怪检测;
    public long 愤怒之火掉血;
    public long 神圣之火回血;
    public int 银行账号;
    public int 银行密码;
    public  long 最高伤害;
    public  long 追加伤害;
    public long 枫叶数量;
    public long 飞镖数量;
    public int 属性攻击累计伤害次数;
    public long 属性攻击累计伤害;
    public int 龙之献祭攻击次数;
    public long wlTime;
    public Map<Integer,Integer> BOSS记录累计;
    public double 记录坐标X;
    public double 记录坐标Y;
    public Map<Integer,Integer> BOSS记录;
    public int 打怪地图;
    public  int 打怪数量;
    public  int 打Boss数量;
    public int 被触碰次数;
    public long 累计掉血;
    public double X坐标误差;
    public double Y坐标误差;
    public int 误差次数;
    public int 过图;
    public long saveTime;

    //地图爆率设置
    public  int 地图缓存1;
    //地图爆率ID
    public  int 地图缓存2;
    public int 地图缓存3;
    public int 地图缓存4;
    public int 地图缓存5;
    public int 地图缓存6;
    public int 地图缓存7;
    public int 地图缓存8;
    public int 地图缓存9;
    public int 地图缓存10;
    public String 地图缓存时间1;
    public String 地图缓存时间2;
    public String 地图缓存时间3;
    public String 地图缓存时间4;
    public String 地图缓存时间5;
    public String 地图缓存时间6;
    public String 地图缓存时间7;
    public String 地图缓存时间8;
    public String 地图缓存时间9;
    public String 地图缓存时间10;
    public long 攻击加速;
    public int 攻击加速判断;
    public double 记录移动坐标X1;
    public double 记录移动坐标Y1;
    public double 记录移动坐标X2;
    public double 记录移动坐标Y2;
    public Boolean isCheating;
    private int points;
    private int playerPoints;
    private int playerEnergy;
    private int jf1;
    private int jf2;
    private int jf3;
    private int jf4;
    private int jf5;
    private int jf6;
    private int jf7;
    private int jf8;
    private int jf9;
    private int jf10;
    private int 等级上限;
    private  int corona;
    private int coronaMap;
    public volatile int saveData;
    public long buffTime;
    private boolean saveingToDB = false;
    private Map<Integer, Integer> _equippedFuMoMap;
    private transient List<LifeMovementFragment> 吸怪RES;
    private ArrayList<IItem> 临时防滑鞋子 = new ArrayList();

    public double 套装伤害加成;
    public int clonedamgerate;
    public boolean 是否开店 = false;
    public boolean 是否储备经验 = false;
    public boolean 是否防滑 = false;
    public boolean 屏蔽特效 = false;
    public long power = 0L;
    public long LastSaveTime = 0L;
    private final Map<Integer, Integer> potentialMap;


    private long lastSkillSkinTime = 0L;
    public ArrayList<Integer> mountList = new ArrayList<>();

    private int petHpItemId;
    private int petMpItemId;

    private int lastTakeDamageValue;
    private long lastPetHpTime;
    private long lastPetVacTime;
    private double petHpRecoveryPercent;
    private double petMpRecoveryPercent;
    public short package_str;
    public short package_dex;
    public short package_int;
    public short package_luk;
    public short package_all_ap;
    public short package_watk;
    public short package_matk;
    public short package_wdef;
    public short package_mdef;
    public short package_acc;
    public short package_avoid;
    public short package_maxhp;
    public short package_maxmp;
    public short package_speed;
    public short package_jump;
    public short package_str_percent;
    public short package_dex_percent;
    public short package_int_percent;
    public short package_luk_percent;
    public short package_all_ap_percent;
    public short package_watk_percent;
    public short package_matk_percent;
    public short package_wdef_percent;
    public short package_mdef_percent;
    public short package_acc_percent;
    public short package_avoid_percent;
    public short package_maxhp_percent;
    public short package_maxmp_percent;
    public short package_normal_damage_percent;
    public short package_boss_damage_percent;
    public short package_total_damage_percent;

    public short totalDropRate = 0;
    public short totalDropRateCount = 0;
    public short totalExpRate = 0;
    public short totalExpRateCount = 0;
    public short totalMesoRate = 0;
    public short totalMesoRateCount = 0;
    //抗性（可抵抗BOSS释放的debuff技能）
    public short totalResistance = 0;
    //闪避
    public short totalDodge = 0;
    private static ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    private static List<PackageOfEquipments.MyPackage> packageList = Collections.synchronizedList(new ArrayList());
    private ScheduledFuture<?> 仙人模式线程;
    private ScheduledFuture<?> 仙人模式BUFF线程;
    private ScheduledFuture<?> 物理攻击力线程;
    public int 修仙;
    private ScheduledFuture<?> 魔法攻击力线程;
    private ScheduledFuture<?> 硬化皮肤线程;
    private ScheduledFuture<?> 修炼BUFF增益线程;
    int a;
    public int b;
    int c;
    public int d;
    private int acash;
    public long lastGainHM;

    private ScheduledFuture<?> 保护线程;

    private boolean backupInventory = false;
    private ArrayList<Integer> dropItemFilterList = new ArrayList();
    public int extra_damage_id = 10016;
    public byte extra_damage_display = 10;
    public byte extra_damage_tbyte = 17;
    public byte extra_damage_animation = -128;
    public byte extra_damage_speed = 2;
    public byte extra_damage_unk = 0;
    public long lastItemSortTime = 0L;
    public long lastSuperTransformationTime = 0L;
    public long lastPetLootTime = 0L;
    public long loadDuration = 0L;
    public long lastComboTime = 0L;
    public int nextMapId = 0;
    public long max_damage;
    private boolean isShowChair = true;
    private boolean isShowEquip = true;
    public int todayOnlineTime;
    public int totalOnlineTime;
    public boolean superTransformation;
    public int cloneDamagePercentage = 50;
    public boolean fake;
    public int fakeOwnerId;
    public TreeMap<Integer, MonsterKillQuest> monsterKillQuestMap = new TreeMap<>();
    public ArrayList<Integer> sellWhenPickUpItemList = new ArrayList<>();
    private long exp_reserve;
    private boolean showSkill = true;
    private boolean dropOnMyFoot = false;
    private boolean dropOnMyBag = false;
    private int mountId = 1932003;
    private int tamingMobId = 0;
    private int tamingMobItemId = 0;
    private byte imprison = 0;
    private int[] cloneDamageRateList = new int[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
    private int guildPoints;
    private int stage;
    private int breakLevel;
    private float expRateChr = 1.0F;
    private float mesoRateChr = 1.0F;
    private float dropRateChr = 1.0F;
    private long qpStartTime = 0L;
    private long lyStartTime = 0L;
    private long bossStartTime = 0L;
    private boolean openEnableDarkMode = false;
    private transient MapleDragon dragon;
    private int cloneDamage = 0;
    public List<LtDiabloEquipments> LtDiabloEquipmentsList = new ArrayList<>();
    public Map<Integer, Long> usedSkills = new HashMap<Integer, Long>();

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
    public void setUsedSkills(final int skillId, final long now, final long cooltime) {
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
    public int getCloneDamage() {
        return cloneDamage;
    }

    public void setCloneDamageAdd(int cloneDamage) {
        this.cloneDamage += cloneDamage;
    }
    public void setCloneDamage(int cloneDamage) {
        this.cloneDamage = cloneDamage;
    }
    public boolean isOpenEnableDarkMode() {
        return openEnableDarkMode;
    }

    public void setOpenEnableDarkMode(boolean openEnableDarkMode) {
        this.openEnableDarkMode = openEnableDarkMode;
    }

    public long getLyStartTime() {
        return lyStartTime;
    }

    public void setLyStartTime(long lyStartTime) {
        this.lyStartTime = lyStartTime;
    }

    public long getQpStartTime() {
        return qpStartTime;
    }

    public void setQpStartTime(long qpStartTime) {
        this.qpStartTime = qpStartTime;
    }

    public long getBossStartTime() {
        return bossStartTime;
    }

    public void setBossStartTime(long bossStartTime) {
        this.bossStartTime = bossStartTime;
    }

    public boolean is开启自动回收() {
        return 开启自动回收;
    }

    public void set开启自动回收(boolean 开启自动回收) {
        this.开启自动回收 = 开启自动回收;
    }

    public void set开启or关闭自动回收() {
        if (getBossLog1("开启自动回收",1)>0){
            setBossLog1("开启自动回收",1,-getBossLog1("开启自动回收",1));
            this.开启自动回收 = false;
        }else{
            setBossLog1("开启自动回收",1);
            this.开启自动回收 = true;
        }

    }
    public ArrayList<Integer> getMountList() {
        if (this.mountList.isEmpty()) {
            this.loadMountListFromDB();
        }

        return this.mountList;
    }


    public int getMountId() {
        return this.mountId;
    }

    public void setMountId(int mountId) {
        this.mountId = mountId;
    }
    public int getPetHpItemId() {
        return this.petHpItemId;
    }

    public void setPetHpItemId(int petHpItemId) {
        this.petHpItemId = petHpItemId;
    }

    public int getPetMpItemId() {
        return this.petMpItemId;
    }

    public void setPetMpItemId(int petMpItemId) {
        this.petMpItemId = petMpItemId;
    }

    public double getPetHpRecoveryPercent() {
        return this.petHpRecoveryPercent;
    }

    public void setPetHpRecoveryPercent(double petHpRecoveryPercent) {
        this.petHpRecoveryPercent = petHpRecoveryPercent;
    }

    public double getPetMpRecoveryPercent() {
        return this.petMpRecoveryPercent;
    }

    public void setPetMpRecoveryPercent(double petMpRecoveryPercent) {
        this.petMpRecoveryPercent = petMpRecoveryPercent;
    }
    public List<SnailCharacterValueHolder> getSnailValues() {
        ArrayList<SnailCharacterValueHolder> values = new ArrayList();
        SnailCharacterValueHolder value = new SnailCharacterValueHolder(this.是否开店, this.是否储备经验, this.是否防滑, this.临时防滑鞋子);
        values.add(value);
        return values;
    }
    private MapleCharacter(final boolean ChannelServer) {
        this.clonedamgerate = 0;
        this.corona = 0;
        this.coronaMap = 0;
        this.teleportname = "";
        this.nowmacs = "";
        this.nextConsume = 0L;
        this.pqStartTime = 0L;
        this.guildrank = 5;
        this.allianceRank = 5;
        this.fairyExp = 30;
        this.fairyHour = 1;
        this.guildid = 0;
        this.fallcounter = 0;
        this.rank = 1;
        this.rankMove = 0;
        this.guildPoints = 0;
        this.jobRank = 1;
        this.jobRankMove = 0;
        this.marriageItemId = 0;
        this.linkMid = 0;
        this.coconutteam = 0;
        this.followid = 0;
        this.battleshipHP = 0;
        this.attractValue = 0;
        this.noAttractValue = 0;
        this.saveData = 0;
        this.buffTime = 0L;
        this.MSG = 0;
        this.打怪 = 0;
        this.吸怪 = 0;
        this.FLY_吸怪 = 0;
        this.CsMod = 0;
        this.在线时间 = 0;
        this.old = new Point(0, 0);
        this.smega = true;
        this.gashponmega = true;
        this.hasSummon = false;
        this.精灵商人购买开关 = false;
        this.玩家私聊开关 = false;
        this.玩家密语开关 = false;
        this.好友聊天开关 = false;
        this.队伍聊天开关 = false;
        this.公会聊天开关 = false;
        this.联盟聊天开关 = false;
        this.GM吸怪信息开关 = false;
        this.canSetBeansNum = false;
        this.Vip_Medal = true;
        this.auto吸怪 = false;
        this.DebugMessage = false;
        this.itemVacs = false;
        this.mobVacs = false;
        this.mobMapVacs = false;
        this.beansStart = false;
        this.remainingSp = new int[10];
        this.savedHairs = new int[6];
        this.savedFaces = new int[6];
        this.skills = new LinkedHashMap<ISkill, SkillEntry>();
        this.effects = new ConcurrentEnumMap<MapleBuffStat, MapleBuffStatValueHolder>(MapleBuffStat.class);
        this.skillID = new LinkedHashMap<Integer, MapleBuffStatValueHolder>();
        this.coolDowns = new LinkedHashMap<Integer, MapleCoolDownValueHolder>();
        this.diseases = new ConcurrentEnumMap<MapleDisease, MapleDiseaseValueHolder>(MapleDisease.class);
        this.finishedAchievements = new ArrayList<Integer>();
        this.invincible = false;
        this.canTalk = true;
        this.followinitiator = false;
        this.followon = false;
        this.skillMacros = new SkillMacro[5];
        this.pyramidSubway = null;
        this.pendingExpiration = null;
        this.pendingSkills = null;
        this.movedMobs = new HashMap<Integer, Integer>();
        this.lasttime = 0L;
        this.currenttime = 0L;
        this.apprentice = 0;
        this.master = 0;
        this.ariantScore = 0;
        this.防止复制时间 = 1000L;
        this.isbanned = false;
        this.鱼来鱼往 = 0;
        this.蓄力一击 = 0L;
        this.越战越勇 = 0L;
        this.prevTimestamp1 = 0L;
        this.持续对话NPC = 0L;
        this.宠物捡物冷却 = 0L;
        this.怪物移动冷却 = 0L;
        this.玩家捡物冷却 = 0L;
        this.上线提醒冷却 = 0L;
        this.整理背包冷却 = 0L;
        this.对话冷却 = 0L;
        this.NPC对话冷却 = 0L;
        this.吸怪检测 = 0L;
        this.愤怒之火掉血 = 0L;
        this.神圣之火回血 = 0L;
        this.银行账号 = 0;
        this.银行密码 = 0;
        this.最高伤害 = 0L;
        this.追加伤害 = 0L;
        this.枫叶数量 = 0L;
        this.飞镖数量 = 0L;
        this.属性攻击累计伤害次数 = 0;
        this.属性攻击累计伤害 = 0L;
        this.龙之献祭攻击次数 = 0;
        this.仙人模式线程 = null;
        this.仙人模式BUFF线程 = null;
        this.物理攻击力线程 = null;
        this.修仙 = 0;
        this.魔法攻击力线程 = null;
        this.硬化皮肤线程 = null;
        this.修炼BUFF增益线程 = null;
        this.a = 0;
        this.b = 0;
        this.c = 0;
        this.d = 0;
        this.保护线程 = null;
        this.wlTime = 0L;
        this.BOSS记录 = new Hashtable<>();
        this.BOSS记录累计 = new Hashtable<>();
        this.记录坐标X = 0.0;
        this.记录坐标Y = 0.0;
        this.打怪地图 = 0;
        this.打怪数量 = 0;
        this.被触碰次数 = 0;
        this.累计掉血 = 0L;
        this.X坐标误差 = 0.0;
        this.Y坐标误差 = 0.0;
        this.误差次数 = 0;
        this.过图 = 0;
        this.saveTime = 0L;
        this.地图缓存1 = 0;
        this.地图缓存2 = 0;
        this.地图缓存3 = 0;
        this.地图缓存4 = 0;
        this.地图缓存5 = 0;
        this.地图缓存6 = 0;
        this.地图缓存7 = 0;
        this.地图缓存8 = 0;
        this.地图缓存9 = 0;
        this.地图缓存10 = 0;
        this.地图缓存时间1 = "";
        this.地图缓存时间2 = "";
        this.地图缓存时间3 = "";
        this.地图缓存时间4 = "";
        this.地图缓存时间5 = "";
        this.地图缓存时间6 = "";
        this.地图缓存时间7 = "";
        this.地图缓存时间8 = "";
        this.地图缓存时间9 = "";
        this.地图缓存时间10 = "";
        this.攻击加速 = 0L;
        this.攻击加速判断 = 0;
        this.记录移动坐标X1 = 0.0;
        this.记录移动坐标Y1 = 0.0;
        this.记录移动坐标X2 = 0.0;
        this.记录移动坐标Y2 = 0.0;
        this.打怪数量 = 0;
        this.打Boss数量=0;
        this.等级上限 = 0;
        this.被驱散时间 = System.currentTimeMillis();
        this.物理无效时间 = System.currentTimeMillis();
        this.魔法无效时间 = System.currentTimeMillis();
        this.potentialMap = new ConcurrentHashMap<>();
        this.isCheating = Boolean.valueOf(false);
        this._equippedFuMoMap = new HashMap<Integer, Integer>();
        this.setStance(0);
        this.setPosition(new Point(0, 0));
        this.inventory = new MapleInventory[MapleInventoryType.values().length];
        for (final MapleInventoryType type : MapleInventoryType.values()) {
            this.inventory[type.ordinal()] = new MapleInventory(type);
        }
        this.quests = new LinkedHashMap<MapleQuest, MapleQuestStatus>();
        this.stats = new PlayerStats(this);
        for (int i = 0; i < this.remainingSp.length; ++i) {
            this.remainingSp[i] = 0;
        }
        for (int i = 0; i < this.savedHairs.length; ++i) {
            this.savedHairs[i] = -1;
        }
        for (int i = 0; i < this.savedFaces.length; ++i) {
            this.savedFaces[i] = -1;
        }
        if (ChannelServer) {
            this.lastCombo = 0L;
            this.mulung_energy = 0;
            this.combo = 0;
            this.keydown_skill = 0L;
            this.lastHPTime = 0L;
            this.lastMPTime = 0L;
            this.mapChangeTime = 0L;
            this.lastRecoveryTime = 0L;
            this.petStore = new byte[3];
            for (int i = 0; i < this.petStore.length; ++i) {
                this.petStore[i] = -1;
            }
            this.wishlist = new int[10];
            this.rocks = new int[10];
            this.regrocks = new int[5];
            this.clones = (WeakReference<MapleCharacter>[])new WeakReference[25];
            for (int i = 0; i < this.clones.length; ++i) {
                this.clones[i] = new WeakReference<MapleCharacter>(null);
            }
            (this.inst = new AtomicInteger()).set(0);
            this.keylayout = new MapleKeyLayout();
            this.doors = new ArrayList<MapleDoor>();
            this.controlled = new LinkedHashSet<MapleMonster>();
            this.summons = new LinkedHashMap<Integer, MapleSummon>();
            this.visibleMapObjects = new LinkedHashSet<MapleMapObject>();
            this.visibleMapObjectsLock = new ReentrantReadWriteLock();
            this.pendingCarnivalRequests = new LinkedList<MapleCarnivalChallenge>();
            this.savedLocations = new int[SavedLocationType.values().length];
            for (int i = 0; i < SavedLocationType.values().length; ++i) {
                this.savedLocations[i] = -1;
            }
            this.questinfo = new LinkedHashMap<Integer, String>();
            this.anticheat = new CheatTracker(this);
            this.pets = new ArrayList<MaplePet>();
            this.开启自动回收 = getBossLog1("开启自动回收",1)>0 ? true : false;
        }
    }
    public long getPower() {
        if (this.power <= 0L) {
            this.power = this.获取角色战斗力();
        }

        return this.power;
    }

    public long 获取角色战斗力() {
        MapleGuild.战斗力信息 player = new MapleGuild.战斗力信息();
        Connection con = DBConPool.getConnection();

        try {
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            PreparedStatement ps = con.prepareStatement("select * from characters WHERE `id` = ?");
            ps.setInt(1, this.getId());
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                int chrid = rs.getInt("id");
                PreparedStatement ps1 = con.prepareStatement("SELECT * FROM `inventoryitems` LEFT JOIN `inventoryequipment` USING(`inventoryitemid`) WHERE `characterid` = ? AND `position` < 0");
                ps1.setInt(1, chrid);
                ResultSet rs1 = ps1.executeQuery();
                player.setPotentials("");

                while(rs1.next()) {
                    player.setE_str(player.getE_str() + rs1.getShort("str"));
                    player.setE_dex(player.getE_dex() + rs1.getShort("dex"));
                    player.setE_int(player.getE_int() + rs1.getShort("int"));
                    player.setE_luk(player.getE_luk() + rs1.getShort("luk"));
                    player.setE_hp(player.getE_hp() + rs1.getShort("hp"));
                    player.setE_mp(player.getE_mp() + rs1.getShort("mp"));
                    player.setE_watk(player.getE_watk() + rs1.getShort("watk"));
                    player.setE_matk(player.getE_matk() + rs1.getShort("matk"));
                    player.setPotentials(player.getPotentials() + rs1.getString("snail_potentials"));
                }

                player.setChrid(chrid);
                player.setName(rs.getString("name"));
                player.setLevel(rs.getInt("level"));
                player.setJob(rs.getInt("job"));
                player.setStr(rs.getInt("str"));
                player.setDex(rs.getInt("dex"));
                player.setInt(rs.getInt("int"));
                player.setLuk(rs.getInt("luk"));
                player.setMaxhp(rs.getInt("maxhp"));
                player.setMaxmp(rs.getInt("maxmp"));
                player.setMax_damage(rs.getLong("max_damage"));
                player.solveForce();
            }
        } catch (SQLException var8) {
            服务端输出信息.println_err("获取角色战斗力出错！,错误位置：MapleCharacter.获取角色战斗力，原因：" + var8);
        }

        return player.getForce();
    }


    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTimes) {
        this.saveTime = saveTimes;
    }

    public long getWlTime() {
        return wlTime;
    }

    public void setWlTime(long wlTime) {
        this.wlTime = wlTime;
    }

    public int get地图缓存1() {
        return 地图缓存1;
    }

    public void set地图缓存1(int 地图缓存1) {
        this.地图缓存1 = 地图缓存1;
    }
    public int get地图缓存2() {
        return 地图缓存2;
    }

    public void set地图缓存2(int 地图缓存2) {
        this.地图缓存2 = 地图缓存2;
    }

    public long get被驱散时间() {
        return 被驱散时间;
    }

    public void set被驱散时间(long 被驱散时间) {
        this.被驱散时间 = 被驱散时间;
    }

    public long get被诱导时间() {
        return 被诱导时间;
    }

    public void set被诱导时间(long 被诱导时间) {
        this.被诱导时间 = 被诱导时间;
    }
    public long get被封印时间() {
        return 被封印时间;
    }

    public void set被封印时间(long 被封印时间) {
        this.被封印时间 = 被封印时间;
    }

    public long getNPC对话冷却() {
        return NPC对话冷却;
    }

    public void setNPC对话冷却(long NPC对话冷却) {
        this.NPC对话冷却 = NPC对话冷却;
    }

    public long get对话冷却() {
        return 对话冷却;
    }

    public void set对话冷却(long 对话冷却) {
        this.对话冷却 = 对话冷却;
    }
    public long get物理无效时间() {
        return 物理无效时间;
    }

    public void set物理无效时间(long 物理无效时间) {
        this.物理无效时间 = 物理无效时间;
    }
    public long get魔法无效时间() {
        return 魔法无效时间;
    }

    public void set魔法无效时间(long 魔法无效时间) {
        this.魔法无效时间 = 魔法无效时间;
    }

    public synchronized int getCorona() {
        return corona;
    }
    public synchronized void setCoronaJan(int corona) {
        this.corona -= corona;
    }
    public synchronized void setCoronaJa(int corona) {
        this.corona += corona;
    }
    public synchronized void setCorona(int corona) {
        this.corona = this.corona - corona;
    }
    public void set最高伤害(long corona) {
        this.最高伤害 = corona;
    }
    public long get最高伤害() {
        return this.最高伤害;
    }
    public void set追加伤害(long corona) {
        this.追加伤害 = corona;
    }
    public long get追加伤害() {
        return this.追加伤害;
    }
    public int getCoronaMap() {
        return coronaMap;
    }

    public void setCoronaMap(int coronaMap) {
        this.coronaMap = coronaMap;
    }

    public Map<MapleBuffStat, MapleBuffStatValueHolder> getEffects() {
        return effects;
    }

    public static MapleCharacter getDefault(final MapleClient client, final int type) {
        final MapleCharacter ret = new MapleCharacter(false);
        ret.client = client;
        ret.map = null;
        ret.exp = 0;
        ret.gmLevel = 0;
        ret.job = (short)((type == 1) ? 0 : ((type == 0) ? 1000 : ((type == 3) ? 2001 : ((type == 4) ? 3000 : 2000))));
        ret.beans = 0;
        ret.meso = 0;
        ret.level = 1;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = client.getAccID();
        ret.buddylist = new BuddyList((byte)20);
        ret.stats.str = 12;
        ret.stats.dex = 5;
        ret.stats.int_ = 4;
        ret.stats.luk = 4;
        ret.stats.maxhp = 50;
        ret.stats.hp = 50;
        ret.stats.maxmp = 50;
        ret.stats.mp = 50;
        ret.prefix = "";
        ret.PGSXDJ = 0;
        ret.gachexp = 0;
        ret.max_damage = 199999L;
        ret.guildPoints = 0;
        ret.exp_reserve = 0L;
        ret.breakLevel = 0;

        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT name, 2ndpassword, mPoints,  vpoints, VIP, loginkey, serverkey, clientkey FROM accounts WHERE id = ?");
            ps.setInt(1, ret.accountid);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret.client.setAccountName(rs.getString("name"));
                    ret.accountsecondPassword = rs.getString("2ndpassword");
                    ret.maplepoints = rs.getInt("mPoints");
                    ret.vpoints = rs.getInt("vpoints");
                    ret.vip = rs.getInt("VIP");
                    ret.loginkey = rs.getString("loginkey");
                    ret.serverkey = rs.getString("serverkey");
                    ret.clientkey = rs.getString("clientkey");
                }
            }
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            System.err.println("Error getting character default" + (Object)e);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
        return ret;
    }
    
    public static MapleCharacter ReconstructChr(final CharacterTransfer ct, final MapleClient client, final boolean isChannel) {
        final MapleCharacter ret = new MapleCharacter(true);
        ret.client = client;
        if (!isChannel) {
            ret.client.setChannel((int)ct.channel);
        }
        ret.nowmacs = ct.nowmacs;
        ret.canTalk = ct.canTalk;
        ret.DebugMessage = ct.DebugMessage;
        ret.auto吸怪 = ct.auto吸怪;
        ret.GM吸怪信息开关 = ct.GM吸怪信息开关;
        ret.Vip_Medal = ct.Vip_Medal;
       ret.精灵商人购买开关 = ct.精灵商人购买开关;
        ret.玩家私聊开关 = ct.玩家私聊开关;
        ret.玩家密语开关 = ct.玩家密语开关;
        ret.好友聊天开关 = ct.好友聊天开关;
        ret.队伍聊天开关 = ct.队伍聊天开关;
        ret.公会聊天开关 = ct.公会聊天开关;
        ret.联盟聊天开关 = ct.联盟聊天开关;
        ret.smega = ct.smega;
        ret.gashponmega = ct.gashponmega;
        ret.id = ct.characterid;
        ret.name = ct.name;
        ret.level = ct.level;
        ret.fame = ct.fame;
        ret.CRand = new PlayerRandomStream();
        ret.stats.str = ct.str;
        ret.stats.dex = ct.dex;
        ret.stats.int_ = ct.int_;
        ret.power = ct.power;
        ret.stats.luk = ct.luk;
        ret.stats.maxhp = ct.maxhp;
        ret.stats.maxmp = ct.maxmp;
        ret.stats.hp = ct.hp;
        ret.stats.mp = ct.mp;
        ret.chalktext = ct.chalkboard;
        ret.exp = ct.exp;
        ret.hpmpApUsed = ct.hpApUsed;
        ret.remainingSp = ct.remainingSp;
        ret.remainingAp = ct.remainingAp;
        ret.savedHairs = ct.savedHairs;
        ret.savedFaces = ct.savedFaces;
        ret.beans = ct.beans;
        ret.meso = ct.meso;
        ret.gmLevel = ct.gmLevel;
        ret.skinColor = ct.skinColor;
        ret.gender = ct.gender;
        ret.job = ct.job;
        ret.hair = ct.hair;
        ret.face = ct.face;
        ret.accountid = ct.accountid;
        ret.mapid = ct.mapid;
        ret.initialSpawnPoint = ct.initialSpawnPoint;
        ret.world = ct.world;
        ret.bookCover = ct.mBookCover;
        ret.dojo = ct.dojo;
        ret.dojoRecord = ct.dojoRecord;
        ret.guildid = ct.guildid;
        ret.guildrank = ct.guildrank;
        ret.allianceRank = ct.alliancerank;
        ret.CsMod = ct.CsMod;
        ret.vpoints = ct.vpoints;
        ret.vip = ct.vip;
        ret.mrqdTime = ct.mrqdTime;
        ret.fairyExp = ct.fairyExp;
        ret.marriageId = ct.marriageId;
        ret.currentrep = ct.currentrep;
        ret.totalrep = ct.totalrep;
        ret.charmessage = ct.charmessage;
        ret.expression = ct.expression;
        ret.constellation = ct.constellation;
        ret.blood = ct.blood;
        ret.month = ct.month;
        ret.jf = ct.jf;
        ret.zdjf = ct.zdjf;
        ret.rwjf = ct.rwjf;
        ret.cz = ct.cz;
        ret.zs = ct.zs;
        ret.dy = ct.dy;
        ret.rmb = ct.rmb;
        ret.yb = ct.yb;
        ret.playerPoints = ct.playerPoints;
        ret.playerEnergy = ct.playerEnergy;
        ret.jf1 = ct.jf1;
        ret.jf2 = ct.jf2;
        ret.jf3 = ct.jf3;
        ret.jf4 = ct.jf4;
        ret.jf5 = ct.jf5;
        ret.jf6 = ct.jf6;
        ret.jf7 = ct.jf7;
        ret.jf8 = ct.jf8;
        ret.jf9 = ct.jf9;
        ret.jf10 = ct.jf10;
        ret.day = ct.day;
        ret.gachexp = ct.gachexp;
        ret.makeMFC(ct.familyid, ct.seniorid, ct.junior1, ct.junior2);
        if (ret.guildid > 0) {
            ret.mgc = new MapleGuildCharacter(ret);
        }
        ret.backupInventory = ct.backupInventory;
        ret.buddylist = new BuddyList(ct.buddysize);
        ret.subcategory = ct.subcategory;
        ret.prefix = ct.prefix;
        ret.PGSXDJ = ct.PGSXDJ;
        ret.skillSkin = ct.skillSkin;
        ret.dropItemFilterList = ct.dropItemFilterList;
        ret.isShowChair = ct.isShowChair;
        ret.isShowEquip = ct.isShowEquip;
        ret.max_damage = ct.max_damage;
        ret.todayOnlineTime = ct.todayOnlineTime;
        ret.totalOnlineTime = ct.totalOnlineTime;
        ret.exp_reserve = ct.exp_reserve;
        ret.dropOnMyBag = ct.dropOnMyBag;
        ret.dropOnMyFoot = ct.dropOnMyFoot;
        ret.mountId = ct.mountId;
        ret.mountList = ct.mountList;
        ret.imprison = ct.imprison;
        ret.superTransformation = ct.superTransformation;
        ret.cloneDamagePercentage = ct.cloneDamagePercentage;
        ret.cloneDamageRateList = ct.cloneDamageRateList;
        ret.fake = ct.fake;
        ret.fakeOwnerId = ct.fakeOwnerId;
        ret.guildPoints = ct.guildPoints;
        ret.breakLevel = ct.breakLevel;
        ret.stage = ct.stage;
        ret.monsterKillQuestMap = ct.monsterKillQuestMap;
        ret.dropItemFilterList = ct.dropItemFilterList;
        ret.sellWhenPickUpItemList = ct.sellWhenPickUpItemList;
        ret.expRateChr = ct.expRateChr;
        ret.mesoRateChr = ct.mesoRateChr;
        ret.dropRateChr = ct.dropRateChr;

        if (isChannel) {
            final MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
            ret.map = mapFactory.getMap(ret.mapid);
            if (ret.map != null) {
                if (ret.mapid == 801000110 || ret.mapid == 801000210) {
                    ret.map = mapFactory.getMap(801000000);
                }
                if (ret.mapid >= 211060000 && ret.mapid <= 211070200) {
                    ret.map = mapFactory.getMap(211060000);
                }
            }
            if (ret.map == null) {
                ret.map = mapFactory.getMap(100000000);
            }
            else if (ret.map.getForcedReturnId() != 999999999) {
                ret.map = ret.map.getForcedReturnMap();
            }
            MaplePortal portal = ret.map.getPortal((int)ret.initialSpawnPoint);
            if (portal == null) {
                portal = ret.map.getPortal(0);
                ret.initialSpawnPoint = 0;
            }
            ret.setPosition(portal.getPosition());
            final int messengerid = ct.messengerid;
            if (messengerid > 0) {
                ret.messenger = Messenger.getMessenger(messengerid);
            }
        }
        else {
            ret.messenger = null;
        }
        final int partyid = ct.partyid;
        if (partyid >= 0) {
            final MapleParty party = Party.getParty(partyid);
            if (party != null && party.getMemberById(ret.id) != null) {
                ret.party = party;
            }
        }
        for (final Entry<Integer, Object> qs : ct.Quest.entrySet()) {
            final MapleQuest quest = MapleQuest.getInstance((int)Integer.valueOf(qs.getKey()));
            final MapleQuestStatus queststatus_from = (MapleQuestStatus)qs.getValue();
            final MapleQuestStatus queststatus = new MapleQuestStatus(quest, (int)queststatus_from.getStatus());
            queststatus.setForfeited(queststatus_from.getForfeited());
            queststatus.setCustomData(queststatus_from.getCustomData());
            queststatus.setCompletionTime(queststatus_from.getCompletionTime());
            if (queststatus_from.getMobKills() != null) {
                for (final Entry<Integer, Integer> mobkills : queststatus_from.getMobKills().entrySet()) {
                    queststatus.setMobKills((int)Integer.valueOf(mobkills.getKey()), (int)Integer.valueOf(mobkills.getValue()));
                }
            }
            ret.quests.put(quest, queststatus);
        }
        for (final Entry<Integer, SkillEntry> qs2 : ct.Skills.entrySet()) {
            ret.skills.put(SkillFactory.getSkill((int)Integer.valueOf(qs2.getKey())), qs2.getValue());
        }
        for (final Integer zz : ct.finishedAchievements) {
            ret.finishedAchievements.add(zz);
        }
        ret.monsterbook = new MonsterBook(ct.mbook);
        ret.inventory = (MapleInventory[])(MapleInventory[])ct.inventorys;
        ret.BlessOfFairy_Origin = ct.BlessOfFairy;
        ret.skillMacros = (SkillMacro[])(SkillMacro[])ct.skillmacro;
        ret.petStore = ct.petStore;
        ret.keylayout = new MapleKeyLayout(ct.keymap);
        ret.questinfo = ct.InfoQuest;
        ret.savedLocations = ct.savedlocation;
        ret.wishlist = ct.wishlist;
        ret.rocks = ct.rocks;
        ret.regrocks = ct.regrocks;
        ret.buddylist.loadFromTransfer(ct.buddies);
        ret.keydown_skill = 0L;
        ret.lastfametime = ct.lastfametime;
        ret.lastmonthfameids = ct.famedcharacters;
        ret.storage = (MapleStorage)ct.storage;
        ret.cs = (CashShop)ct.cs;
        client.setAccountName(ct.accountname);
        ret.maplepoints = ct.MaplePoints;
        ret.accountsecondPassword = ct.accountsecondPassword;
        ret.loginkey = ct.loginkey;
        ret.serverkey = ct.serverkey;
        ret.clientkey = ct.clientkey;
        ret.antiMacro = (MapleLieDetector)ct.antiMacro;
        ret.numClones = ct.clonez;
        ret.mount = new MapleMount(ret, ct.mount_itemid, GameConstants.isKOC((int)ret.job) ? 10001004 : (GameConstants.isAran((int)ret.job) ? 20001004 : 1004), ct.mount_Fatigue, ct.mount_level, ct.mount_exp);
        ret.stats.recalcLocalStats(true);
        return ret;
    }
    
    public static MapleCharacter loadCharFromDB(final int charid, final MapleClient client, final boolean channelserver) {
        final MapleCharacter ret = new MapleCharacter(channelserver);
        ret.client = client;
        ret.id = charid;
        long time0 = System.currentTimeMillis();
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
//                    System.out.println("Loading the Char Failed (char not found)==>查询角色ID: = "+charid );
//                    System.out.println("Loading the Char Failed (char not found)==>查询语句:"+ps.toString() );
//                    System.out.println("Loading the Char Failed (char not found)===>查询到的结果数:"+rs.getRow());
//                    System.out.println("Loading the Char Failed (char not found)===>异常点:loadCharFromDB");
                    return null;
                }
                ret.Vip_Medal = (rs.getShort("VipMedal") > 0);
                ret.name = rs.getString("name");
                ret.level = rs.getShort("level");
                ret.fame = rs.getShort("fame");
                ret.stats.str = rs.getShort("str");
                ret.stats.dex = rs.getShort("dex");
                ret.stats.int_ = rs.getShort("int");
                ret.stats.luk = rs.getShort("luk");
                ret.stats.maxhp = rs.getShort("maxhp");
                ret.stats.maxmp = rs.getShort("maxmp");
                ret.stats.hp = rs.getShort("hp");
                ret.stats.mp = rs.getShort("mp");
                ret.exp = rs.getInt("exp");
                ret.hpmpApUsed = rs.getShort("hpApUsed");
                final String[] sp = rs.getString("sp").split(",");
                for (int i = 0; i < ret.remainingSp.length; ++i) {
                    ret.remainingSp[i] = Integer.parseInt(sp[i]);
                }
                final String[] saves_faces = rs.getString("saved_faces").split(",");
                for (int j = 0; j < ret.savedFaces.length; ++j) {
                    ret.savedFaces[j] = Integer.parseInt(saves_faces[j]);
                }
                final String[] saves_hairs = rs.getString("saved_hairs").split(",");
                for (int k = 0; k < ret.savedHairs.length; ++k) {
                    ret.savedHairs[k] = Integer.parseInt(saves_hairs[k]);
                }
                ret.remainingAp = rs.getShort("ap");
                ret.beans = rs.getInt("beans");
                ret.meso = rs.getInt("meso");
                ret.gmLevel = rs.getByte("gm");
                ret.skinColor = rs.getByte("skincolor");
                ret.gender = rs.getByte("gender");
                ret.job = rs.getShort("job");
                ret.hair = rs.getInt("hair");
                ret.face = rs.getInt("face");
                ret.accountid = rs.getInt("accountid");
                ret.mapid = rs.getInt("map");
                ret.initialSpawnPoint = rs.getByte("spawnpoint");
                ret.world = rs.getByte("world");
                ret.guildid = rs.getInt("guildid");
                ret.guildrank = rs.getByte("guildrank");
                ret.allianceRank = rs.getByte("allianceRank");
                ret.currentrep = rs.getInt("currentrep");
                ret.totalrep = rs.getInt("totalrep");
                ret.makeMFC(rs.getInt("familyid"), rs.getInt("seniorid"), rs.getInt("junior1"), rs.getInt("junior2"));
                if (ret.guildid > 0) {
                    ret.mgc = new MapleGuildCharacter(ret);
                }
                ret.buddylist = new BuddyList(rs.getByte("buddyCapacity"));
                ret.subcategory = rs.getByte("subcategory");
                ret.mount = new MapleMount(ret, 0, (ret.job > 1000 && ret.job < 2000) ? 10001004 : ((ret.job >= 2000) ? ((ret.job == 2001 || (ret.job >= 2200 && ret.job <= 2218)) ? 20011004 : ((ret.job >= 3000) ? 30001004 : 20001004)) : 1004), (byte)0, (byte)1, 0);
                ret.rank = rs.getInt("rank");
                ret.rankMove = rs.getInt("rankMove");
                ret.jobRank = rs.getInt("jobRank");
                ret.jobRankMove = rs.getInt("jobRankMove");
                ret.marriageId = rs.getInt("marriageId");
                ret.charmessage = rs.getString("charmessage");
                ret.expression = rs.getInt("expression");
                ret.constellation = rs.getInt("constellation");
                ret.blood = rs.getInt("blood");
                ret.month = rs.getInt("month");
                ret.jf = rs.getInt("jf");
                ret.zdjf = rs.getInt("zdjf");
                ret.rwjf = rs.getInt("rwjf");
                ret.zs = rs.getInt("zs");
                ret.cz = rs.getInt("cz");
                ret.dy = rs.getInt("dy");
                ret.rmb = rs.getInt("rmb");
                ret.yb = rs.getInt("yb");
                ret.playerPoints = rs.getInt("playerPoints");
                ret.playerEnergy = rs.getInt("playerEnergy");
                ret.jf1 = rs.getInt("jf1");
                ret.jf2 = rs.getInt("jf2");
                ret.jf3 = rs.getInt("jf3");
                ret.jf4 = rs.getInt("jf4");
                ret.jf5 = rs.getInt("jf5");
                ret.jf6 = rs.getInt("jf6");
                ret.jf7 = rs.getInt("jf7");
                ret.jf8 = rs.getInt("jf8");
                ret.jf9 = rs.getInt("jf9");
                ret.jf10 = rs.getInt("jf10");
                ret.day = rs.getInt("day");
                ret.prefix = rs.getString("prefix");
                ret.gachexp = rs.getInt("gachexp");
                ret.PGSXDJ = rs.getInt("PGSXDJ");


                ret.max_damage = rs.getLong("max_damage");
                if (ret.max_damage < 199999L) {
                    ret.max_damage = 199999L;
                }
                ret.exp_reserve = rs.getLong("exp_reserve");
                ret.imprison = rs.getByte("imprison");
                ret.guildPoints = rs.getInt("guildpoints");
                ret.stage = rs.getInt("stage");
                ret.breakLevel = rs.getInt("break_level");
                ret.expRateChr = rs.getFloat("exp_rate");
                ret.mesoRateChr = rs.getFloat("meso_rate");
                ret.dropRateChr = rs.getFloat("drop_rate");

                if (channelserver) {
                    final MapleMapFactory mapFactory = ChannelServer.getInstance(client.getChannel()).getMapFactory();
                    ret.antiMacro = new MapleLieDetector(ret.id);
                    ret.map = mapFactory.getMap(ret.mapid);
                    if (ret.mapid == 801000210) {
                        ret.map = mapFactory.getMap(801000000);
                    }
                    if (ret.mapid == 801000110) {
                        ret.map = mapFactory.getMap(801000000);
                    }
                    if (ret.map == null) {
                        ret.map = mapFactory.getMap(100000000);
                    }
                    MaplePortal portal = ret.map.getPortal((int)ret.initialSpawnPoint);
                    if (portal == null) {
                        portal = ret.map.getPortal(0);
                        ret.initialSpawnPoint = 0;
                    }
                    ret.setPosition(portal.getPosition());
                    final int partyid = rs.getInt("party");
                    if (partyid >= 0) {
                        final MapleParty party = Party.getParty(partyid);
                        if (party != null && party.getMemberById(ret.id) != null) {
                            ret.party = party;
                        }
                    }
                    ret.bookCover = rs.getInt("monsterbookcover");
                    ret.dojo = rs.getInt("dojo_pts");
                    ret.dojoRecord = rs.getByte("dojoRecord");
                    final String[] pets = rs.getString("pets").split(",");
                    for (int l = 0; l < ret.petStore.length; ++l) {
                        ret.petStore[l] = Byte.parseByte(pets[l]);
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT achievementid FROM achievements WHERE accountid = ?");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.finishedAchievements.add(Integer.valueOf(rs.getInt("achievementid")));
                    }
                }
                rs.close();
                ps.close();
                boolean compensate_previousEvans = false;
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                final PreparedStatement pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
                while (rs.next()) {
                    final int id = rs.getInt("quest");
                    if (id == 170000) {
                        compensate_previousEvans = true;
                    }
                    final MapleQuest q = MapleQuest.getInstance(id);
                    final MapleQuestStatus status = new MapleQuestStatus(q, (int)rs.getByte("status"));
                    final long cTime = rs.getLong("time");
                    if (cTime > -1L) {
                        status.setCompletionTime(cTime * 1000L);
                    }
                    status.setForfeited(rs.getInt("forfeited"));
                    status.setCustomData(rs.getString("customData"));
                    ret.quests.put(q, status);
                    pse.setLong(1, rs.getLong("queststatusid"));
                    try (final ResultSet rsMobs = pse.executeQuery()) {
                        while (rsMobs.next()) {
                            status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                        }
                    }
                }
                rs.close();
                ps.close();
                pse.close();
                if (channelserver) {
                    ret.CRand = new PlayerRandomStream();
                    ret.monsterbook = MonsterBook.loadCards(charid);
                    ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
                    }
                    ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
                    ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
                    ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
                    ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
                    ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));
                    ps.close();
                    rs.close();
                    for (final Pair<IItem, MapleInventoryType> mit : ItemLoader.INVENTORY.loadItems(false, Integer.valueOf(charid)).values()) {
                        ret.getInventory((MapleInventoryType)mit.getRight()).addFromDB((IItem)mit.getLeft());
                        if (((IItem)mit.getLeft()).getPet() != null) {
                            ret.pets.add(((IItem)mit.getLeft()).getPet());
                        }
                    }
                    ps = con.prepareStatement("SELECT name, 2ndpassword, mPoints, vpoints, VIP, loginkey, serverkey, clientkey FROM accounts WHERE id = ?");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        ret.getClient().setAccountName(rs.getString("name"));
                        ret.accountsecondPassword = rs.getString("2ndpassword");
                        ret.maplepoints = rs.getInt("mPoints");
                        ret.vpoints = rs.getInt("vpoints");
                        ret.vip = rs.getInt("VIP");
                        ret.loginkey = rs.getString("loginkey");
                        ret.serverkey = rs.getString("serverkey");
                        ret.clientkey = rs.getString("clientkey");
                    }
                    else {
                        rs.close();
                    }
                    ps.close();
                    ps = con.prepareStatement("SELECT quest, customData FROM questinfo WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.questinfo.put(Integer.valueOf(rs.getInt("quest")), rs.getString("customData"));
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration FROM skills WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final ISkill skil = SkillFactory.getSkill(rs.getInt("skillid"));
                        if (skil != null && GameConstants.isApplicableSkill(rs.getInt("skillid")) && rs.getByte("skilllevel") >= 0) {
                            ret.skills.put(skil, new SkillEntry(rs.getByte("skilllevel"), rs.getByte("masterlevel"), rs.getLong("expiration")));
                        }
                        else {
                            if (skil != null) {
                                continue;
                            }
                            final int[] remainingSp = ret.remainingSp;
                            final int skillBookForSkill = GameConstants.getSkillBookForSkill(rs.getInt("skillid"));
                            remainingSp[skillBookForSkill] += rs.getByte("skilllevel");
                        }
                    }
                    rs.close();
                    ps.close();
                    ret.expirationTask(false);
                    ps = con.prepareStatement("SELECT id, name, level FROM characters WHERE accountid = ? ORDER BY level DESC");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    byte maxlevel_ = 0;
                    while (rs.next()) {
                        if (rs.getInt("id") != charid) {
                            byte maxlevel = (byte)(rs.getShort("level") / 10);
                            if (maxlevel > 20) {
                                maxlevel = 20;
                            }
                            if (maxlevel <= maxlevel_) {
                                continue;
                            }
                            maxlevel_ = maxlevel;
                            ret.BlessOfFairy_Origin = rs.getString("name");
                        }
                        else {
                            if (charid >= 17000 || compensate_previousEvans || ret.job < 2200 || ret.job > 2218) {
                                continue;
                            }
                            for (int m = 0; m <= GameConstants.getSkillBook((int)ret.job); ++m) {
                                final int[] remainingSp2 = ret.remainingSp;
                                final int n = m;
                                remainingSp2[n] += 3;//修改升级sp技能点
                            }
                            ret.setQuestAdd(MapleQuest.getInstance(170000), (byte)0, null);
                        }
                    }
                    ret.skills.put(SkillFactory.getSkill(GameConstants.getBofForJob((int)ret.job)), new SkillEntry(maxlevel_, (byte)0, -1L));
                    ps.close();
                    rs.close();
                    ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            final int position = rs.getInt("position");
                            final SkillMacro macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                            ret.skillMacros[position] = macro;
                            //System.out.println("读取技能宏");
                        }
                    } catch (SQLException e) {
                        int index = 0;
                        while (index<=2) {
                            final SkillMacro macro = new SkillMacro(0,0,0,"",0,index);
                            ret.skillMacros[index] = macro;
                            index = index + 1;
                        }
                    }



                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT `keye`,`type`,`action` FROM keymap WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    final Map<Integer, Pair<Byte, Integer>> keyb = ret.keylayout.Layout();
                    while (rs.next()) {
                        keyb.put(Integer.valueOf(rs.getInt("keye")), new Pair<Byte, Integer>(Byte.valueOf(rs.getByte("type")), Integer.valueOf(rs.getInt("action"))));
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map");
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    ret.lastfametime = 0L;
                    ret.lastmonthfameids = new ArrayList<Integer>(31);
                    while (rs.next()) {
                        ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                        ret.lastmonthfameids.add(Integer.valueOf(rs.getInt("characterid_to")));
                    }
                    rs.close();
                    ps.close();
                    ret.buddylist.loadFromDb(charid);
                    ret.storage = MapleStorage.loadStorage(ret.accountid);
                    ret.cs = new CashShop(ret.accountid, charid, (int)ret.getJob());
                    ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    int i2 = 0;
                    while (rs.next()) {
                        ret.wishlist[i2] = rs.getInt("sn");
                        ++i2;
                    }
                    while (i2 < 10) {
                        ret.wishlist[i2] = 0;
                        ++i2;
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    int r = 0;
                    while (rs.next()) {
                        ret.rocks[r] = rs.getInt("mapid");
                        ++r;
                    }
                    while (r < 10) {
                        ret.rocks[r] = 999999999;
                        ++r;
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT mapid FROM regrocklocations WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    r = 0;
                    while (rs.next()) {
                        ret.regrocks[r] = rs.getInt("mapid");
                        ++r;
                    }
                    while (r < 5) {
                        ret.regrocks[r] = 999999999;
                        ++r;
                    }
                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException("No mount data found on SQL column");
                    }
                    final IItem mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-18));

                    ret.mount = new MapleMount(ret, (mount != null) ? mount.getItemId() : 0, (ret.job > 1000 && ret.job < 2000) ? 10001004 : ((ret.job >= 2000) ? ((ret.job == 2001 || ret.job >= 2200) ? 20011004 : ((ret.job >= 3000) ? 30001004 : 20001004)) : 1004), rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                    ps.close();
                    rs.close();
                    ret.skillSkin.loadChrSkill();
                    ret.loadMountListFromDB();
                    EquipFieldEnhancement.getInstance().loadCharFromDB(charid, con);
                   ret.loadMonsterKillQuestFromDB(con);
                   ret.loadDropItemFilterListFromDB(con);
                   ret.loadSellWhenPickUpItemListFromDB(con);
                    ret.stats.recalcLocalStats(true);
                }
                else {
                    for (final Pair<IItem, MapleInventoryType> mit : ItemLoader.INVENTORY.loadItems(true, Integer.valueOf(charid)).values()) {
                        ret.getInventory((MapleInventoryType)mit.getRight()).addFromDB((IItem)mit.getLeft());
                    }
                }
            }
            catch (SQLException ess) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ess, "載入角色失敗..");
            }
            finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                }
                catch (SQLException ex) {}
            }
            con.close();
        }
        catch (SQLException exxx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)exxx);
        }
        System.out.println("角色載入完成用时:"+(System.currentTimeMillis()-time0)+"ms");
        return ret;
    }
    public void loadDropItemFilterListFromDB(Connection con) {
        try {
            if (con == null || con.isClosed()) {
                con = DBConPool.getConnection();
            }

            ArrayList<Integer> ret = new ArrayList();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_drop_item_filter WHERE character_id = ?");
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                ret.add(rs.getInt("item_id"));
            }

            LinkedHashSet<Integer> set = new LinkedHashSet(ret);
            ArrayList<Integer> ret0 = new ArrayList(set);
            rs.close();
            ps.close();
            ret.clear();
            set.clear();
            this.dropItemFilterList.clear();
            this.dropItemFilterList = ret0;
        } catch (SQLException var7) {
            服务端输出信息.println_err("【错误】loadDropItemFilterListFromDB错误，原因：" + var7);
            var7.printStackTrace();
        }

    }

    public TreeMap<Integer, MonsterKillQuest> getMonsterKillQuestMap() {
        return this.monsterKillQuestMap;
    }

    public void clearMonsterKillQuestMap() {
        Iterator var1 = this.monsterKillQuestMap.entrySet().iterator();

        while(var1.hasNext()) {
            Map.Entry<Integer, MonsterKillQuest> entry = (Map.Entry)var1.next();
            ((MonsterKillQuest)entry.getValue()).clearMonsterKillMap();
        }

        this.monsterKillQuestMap.clear();
    }
    public int getTotalOnlineTime() {
        return this.totalOnlineTime;
    }

    public void gainTotalOnlineTime(int s) {
        this.totalOnlineTime += s;
    }

    public void setTotalOnlineTime(int s) {
        this.totalOnlineTime = s;
    }

    public boolean isFake() {
        return this.fake;
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public int getFakeOwnerId() {
        return this.fakeOwnerId;
    }

    public void setFakeOwnerId(int fakeOwnerId) {
        this.fakeOwnerId = fakeOwnerId;
    }

    public boolean isQuestMonster(int monsterId) {
        boolean ret = false;
        Iterator var3 = this.monsterKillQuestMap.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Integer, MonsterKillQuest> entry = (Map.Entry)var3.next();
            if (((MonsterKillQuest)entry.getValue()).hasMonster(monsterId)) {
                ret = true;
                break;
            }
        }

        return ret;
    }

    public ArrayList<MonsterKillQuest> getMonsterKillQuestByMonsterId(int monsterId) {
        ArrayList<MonsterKillQuest> ret = new ArrayList();
        Iterator var3 = this.monsterKillQuestMap.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<Integer, MonsterKillQuest> entry = (Map.Entry)var3.next();
            if (((MonsterKillQuest)entry.getValue()).hasMonster(monsterId)) {
                ret.add(entry.getValue());
            }
        }

        return ret;
    }

    public void deleteMonsterKillQuest(int questId) {
        if (this.monsterKillQuestMap.containsKey(questId)) {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var3 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM snail_monster_kill_quest WHERE character_id =? AND quest_id = ?");
                    ps.setInt(1, this.id);
                    ps.setInt(2, questId);
                    ps.executeUpdate();
                    ps.close();
                    this.monsterKillQuestMap.remove(questId);
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if (con != null) {
                        if (var3 != null) {
                            try {
                                con.close();
                            } catch (Throwable var12) {
                                var3.addSuppressed(var12);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var15) {
                服务端输出信息.println_err("【错误】deleteMonsterKillQuest错误，原因：" + var15);
                var15.printStackTrace();
            }
        }

    }

    public MonsterKillQuest getMonsterKillQuestByQuestId(int questId) {
        return (MonsterKillQuest)this.monsterKillQuestMap.get(questId);
    }

    public MonsterKillQuest newMonsterKillQuest() {
        int questId = 1;
        Iterator var2 = this.monsterKillQuestMap.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<Integer, MonsterKillQuest> entry = (Map.Entry)var2.next();
            if (questId <= (Integer)entry.getKey()) {
                questId = (Integer)entry.getKey() + 1;
            }
        }

        while(this.monsterKillQuestMap.containsKey(questId)) {
            ++questId;
        }

        MonsterKillQuest monsterKillQuest = new MonsterKillQuest(this.getId(), questId);
        this.monsterKillQuestMap.put(questId, monsterKillQuest);
        return monsterKillQuest;
    }

    public void saveMonsterKillQuestToDB(Connection con) {
        try {
            boolean isNewConnection = false;
            if (con == null || con.isClosed()) {
                con = DBConPool.getNewConnection();
                isNewConnection = true;
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
            }

            PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_monster_kill_quest");
            PreparedStatement ps1 = con.prepareStatement("UPDATE snail_monster_kill_quest SET `count` = ?,finish_count = ?,`state` = ? WHERE id = ?");
            PreparedStatement ps2 = con.prepareStatement("INSERT INTO snail_monster_kill_quest (character_id, quest_id, monster_id, `count`, finish_count, `state`) VALUES (?,?,?,?,?,?)");
            PreparedStatement ps3 = con.prepareStatement("UPDATE snail_monster_kill_quest SET begin_time = ?, finish_time = ? WHERE character_id = ? AND quest_id = ?");
            int batchSize1 = 1000;
            int count1 = 0;
            int batchSize2 = 1000;
            int count2 = 0;
            int batchSize3 = 1000;
            int count3 = 0;
            ResultSet rs = ps.executeQuery();
            Iterator var14 = this.monsterKillQuestMap.entrySet().iterator();

            while(var14.hasNext()) {
                Map.Entry<Integer, MonsterKillQuest> entry = (Map.Entry)var14.next();
                HashMap<Integer, MonsterKillQuest.MonsterKill> monsterMap = ((MonsterKillQuest)entry.getValue()).getMonsterKillMap();
                Iterator var17 = monsterMap.entrySet().iterator();

                while(var17.hasNext()) {
                    Map.Entry<Integer, MonsterKillQuest.MonsterKill> entry2 = (Map.Entry)var17.next();
                    ps = con.prepareStatement("SELECT * FROM snail_monster_kill_quest WHERE character_id = ? AND quest_id = ? AND monster_id = ?");
                    ps.setInt(1, this.id);
                    ps.setInt(2, (Integer)entry.getKey());
                    ps.setInt(3, (Integer)entry2.getKey());
                    rs = ps.executeQuery();

                    boolean isExist;
                    for(isExist = false; rs.next(); isExist = true) {
                        ps1.setInt(1, ((MonsterKillQuest.MonsterKill)entry2.getValue()).getNowQuantity());
                        ps1.setInt(2, ((MonsterKillQuest.MonsterKill)entry2.getValue()).getFinishQuantity());
                        ps1.setByte(3, (byte)(((MonsterKillQuest.MonsterKill)entry2.getValue()).getNowQuantity() >= ((MonsterKillQuest.MonsterKill)entry2.getValue()).getFinishQuantity() ? 1 : 0));
                        ps1.setInt(4, rs.getInt("id"));
                        ps1.addBatch();
                        ++count1;
                        if (count1 % batchSize1 == 0) {
                            ps1.executeBatch();
                            ps1.clearBatch();
                        }
                    }

                    if (!isExist) {
                        ps2.setInt(1, this.id);
                        ps2.setInt(2, (Integer)entry.getKey());
                        ps2.setInt(3, (Integer)entry2.getKey());
                        ps2.setInt(4, ((MonsterKillQuest.MonsterKill)entry2.getValue()).getNowQuantity());
                        ps2.setInt(5, ((MonsterKillQuest.MonsterKill)entry2.getValue()).getFinishQuantity());
                        ps2.setByte(6, (byte)(((MonsterKillQuest.MonsterKill)entry2.getValue()).getNowQuantity() >= ((MonsterKillQuest.MonsterKill)entry2.getValue()).getFinishQuantity() ? 1 : 0));
                        ps2.addBatch();
                        ++count2;
                        if (count2 % batchSize2 == 0) {
                            ps2.executeBatch();
                            ps2.clearBatch();
                        }
                    }
                }

                ps3.setTimestamp(1, ((MonsterKillQuest)entry.getValue()).getBeginTime());
                ps3.setTimestamp(2, ((MonsterKillQuest)entry.getValue()).getFinishTime());
                ps3.setInt(3, this.id);
                ps3.setInt(4, (Integer)entry.getKey());
                ps3.addBatch();
                ++count3;
                if (count3 % batchSize3 == 0) {
                    ps3.executeBatch();
                    ps3.clearBatch();
                }
            }

            if (count1 % batchSize1 != 0) {
                ps1.executeBatch();
            }

            if (count2 % batchSize2 != 0) {
                ps2.executeBatch();
            }

            if (count3 % batchSize3 != 0) {
                ps3.executeBatch();
            }

            ps.close();
            rs.close();
            ps1.close();
            ps2.close();
            ps3.close();
            if (isNewConnection) {
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
                con.close();
            }
        } catch (SQLException var20) {
            服务端输出信息.println_err("【错误】 saveMonsterKillQuestToDB错误，原因：" + var20);
            var20.printStackTrace();
        }

    }

    public void loadMonsterKillQuestFromDB(Connection con) {
        try {
            if (con == null || con.isClosed()) {
                con = DBConPool.getConnection();
            }

            PreparedStatement ps = con.prepareStatement("SELECT DISTINCT quest_id FROM snail_monster_kill_quest WHERE character_id = ? ORDER BY quest_id");
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> questIdList = new ArrayList();

            while(rs.next()) {
                int questId = rs.getInt("quest_id");
                if (!questIdList.contains(questId)) {
                    questIdList.add(questId);
                }
            }

            this.clearMonsterKillQuestMap();
            Iterator var10 = questIdList.iterator();

            while(var10.hasNext()) {
                int questId = (Integer)var10.next();
                ps = con.prepareStatement("SELECT * FROM snail_monster_kill_quest WHERE character_id = ? AND quest_id = ?");
                ps.setInt(1, this.id);
                ps.setInt(2, questId);
                rs = ps.executeQuery();
                MonsterKillQuest quest = new MonsterKillQuest(this.id, questId);

                for(boolean setTime = true; rs.next(); quest.addMonsterKill(rs.getInt("monster_id"), rs.getInt("count"), rs.getInt("finish_count"))) {
                    if (setTime) {
                        quest.setBeginTime(rs.getTimestamp("begin_time"));
                        quest.setFinishTime(rs.getTimestamp("finish_time"));
                        setTime = false;
                    }
                }

                this.monsterKillQuestMap.put(questId, quest);
            }

            ps.close();
            rs.close();
        } catch (SQLException var9) {
            服务端输出信息.println_err("【错误】 loadMonsterKillQuestFromDB错误，原因：" + var9);
            var9.printStackTrace();
        }

    }
    public ArrayList<Integer> getDropItemFilterList() {
        return this.dropItemFilterList;
    }

    public void setDropItemFilterList(ArrayList<Integer> dropItemFilterList) {
        this.dropItemFilterList = dropItemFilterList;
    }

    public void loadDropItemFilterListFromDB() {
        this.loadDropItemFilterListFromDB((Connection)null);
    }

    public void saveDropItemFilterListToDB(Connection con) {
        try {
            if (con == null) {
                con = DBConPool.getConnection();
            }

            ArrayList<Integer> ret = new ArrayList(this.dropItemFilterList);
            PreparedStatement ps = con.prepareStatement("DELETE FROM snail_drop_item_filter WHERE character_id = ?");
            ps.setInt(1, this.id);
            ps.executeUpdate();
            ps = con.prepareStatement("INSERT INTO snail_drop_item_filter (character_id, item_id) VALUES (?, ?)");
            ps.setInt(1, this.id);
            Iterator var4 = ret.iterator();

            while(var4.hasNext()) {
                int itemId = (Integer)var4.next();
                ps.setInt(2, itemId);
                ps.executeUpdate();
            }

            ps.close();
            ret.clear();
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】saveDropItemFilterListToDB 错误，原因：" + var6);
            var6.printStackTrace();
        }

    }

    public void deleteDropItemFilter(int itemId) {
        if (this.dropItemFilterList.contains(itemId)) {
            Iterator iter = this.dropItemFilterList.iterator();

            while(iter.hasNext()) {
                if (iter.next().equals(itemId)) {
                    iter.remove();
                }
            }

            try {
                Connection con = DBConPool.getConnection();
                Throwable var4 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM snail_drop_item_filter WHERE character_id = ? and item_id = ?");
                    ps.setInt(1, this.id);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var14) {
                    var4 = var14;
                    throw var14;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var13) {
                                var4.addSuppressed(var13);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var16) {
                服务端输出信息.println_err("【错误】addDropItemFilter错误，原因：" + var16);
                var16.printStackTrace();
            }
        }

    }
    public void addDropItemFilter(int itemId) {
        if (!this.dropItemFilterList.contains(itemId)) {
            this.dropItemFilterList.add(itemId);

            try {
                Connection con = DBConPool.getConnection();
                Throwable var3 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO snail_drop_item_filter (character_id, item_id) VALUES (?, ?)");
                    ps.setInt(1, this.id);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if (con != null) {
                        if (var3 != null) {
                            try {
                                con.close();
                            } catch (Throwable var12) {
                                var3.addSuppressed(var12);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var15) {
                服务端输出信息.println_err("【错误】addDropItemFilter错误，原因：" + var15);
                var15.printStackTrace();
            }
        }

    }
    public boolean isDropItemFilter(int itemId) {
        return this.dropItemFilterList.contains(itemId);
    }

    public static void saveNewCharToDB(MapleCharacter chr, final int type, final boolean db) {
        Connection con = null;
        PreparedStatement ps = null;
        PreparedStatement pse = null;
        ResultSet rs = null;
        try {
            con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
            con.setTransactionIsolation(1);
            con.setAutoCommit(false);
            ps = con.prepareStatement("INSERT INTO characters (level, fame, str, dex, luk, `int`, exp, hp, mp, maxhp, maxmp, sp, ap, gm, skincolor, gender, job, hair, face, map, meso, hpApUsed, spawnpoint, party, buddyCapacity, monsterbookcover, dojo_pts, dojoRecord, pets, subcategory, marriageId, currentrep, totalrep, prefix, PGSXDJ, jf, zdjf, rwjf, zs, cz, dy, rmb, yb, playerPoints, playerEnergy, jf1, jf2, jf3, jf4, jf5, jf6, jf7, jf8, jf9, jf10,  accountid, name, world, VipMedal) VALUES (?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 1);
            ps.setInt(1, 1);
            ps.setShort(2, (short)0);
            final PlayerStats stat = chr.stats;
            ps.setShort(3, stat.getStr());
            ps.setShort(4, stat.getDex());
            ps.setShort(5, stat.getInt());
            ps.setShort(6, stat.getLuk());
            ps.setInt(7, 0);
            ps.setShort(8, stat.getHp());
            ps.setShort(9, stat.getMp());
            ps.setShort(10, stat.getMaxHp());
            ps.setShort(11, stat.getMaxMp());
            ps.setString(12, "0,0,0,0,0,0,0,0,0,0");
            ps.setShort(13, (short)0);
            ps.setByte(14, (byte)0);
            ps.setByte(15, chr.skinColor);
            ps.setByte(16, chr.gender);
            ps.setShort(17, chr.job);
            ps.setInt(18, chr.hair);
            ps.setInt(19, chr.face);
            ps.setInt(20, (type == 1) ? 0 : ((type == 0) ? 130030000 : ((type == 3) ? 900090000 : 914000000)));
            ps.setInt(21, chr.meso);
            ps.setShort(22, (short)0);
            ps.setByte(23, (byte)0);
            ps.setInt(24, -1);
            ps.setByte(25, chr.buddylist.getCapacity());
            ps.setInt(26, 0);
            ps.setInt(27, 0);
            ps.setInt(28, 0);
            ps.setString(29, "-1,-1,-1");
            ps.setInt(30, 0);
            ps.setInt(31, 0);
            ps.setInt(32, 0);
            ps.setInt(33, 0);
            ps.setString(34, chr.prefix);
            ps.setInt(35, chr.PGSXDJ);
            ps.setInt(36, chr.jf);
            ps.setInt(37, chr.zdjf);
            ps.setInt(38, chr.rwjf);
            ps.setInt(39, chr.zs);
            ps.setInt(40, chr.cz);
            ps.setInt(41, chr.dy);
            ps.setInt(42, chr.rmb);
            ps.setInt(43, chr.yb);
            ps.setInt(44, chr.playerPoints);
            ps.setInt(45, chr.playerEnergy);
            ps.setInt(46, chr.jf1);
            ps.setInt(47, chr.jf2);
            ps.setInt(48, chr.jf3);
            ps.setInt(49, chr.jf4);
            ps.setInt(50, chr.jf5);
            ps.setInt(51, chr.jf6);
            ps.setInt(52, chr.jf7);
            ps.setInt(53, chr.jf8);
            ps.setInt(54, chr.jf9);
            ps.setInt(55, chr.jf10);
            ps.setInt(56, chr.getAccountID());
            ps.setString(57, chr.name);
            ps.setByte(58, chr.world);
            ps.setInt(59, (int)(chr.Vip_Medal ? 1 : 0));
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
                throw new DatabaseException("Inserting char failed.");
            }
            chr.id = rs.getInt(1);
            ps.close();
            rs.close();
            ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
            pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (final MapleQuestStatus q : chr.quests.values()) {
                ps.setInt(2, q.getQuest().getId());
                ps.setInt(3, (int)q.getStatus());
                ps.setInt(4, (int)(q.getCompletionTime() / 1000L));
                ps.setInt(5, q.getForfeited());
                ps.setString(6, q.getCustomData());
                ps.executeUpdate();
                rs = ps.getGeneratedKeys();
                rs.next();
                if (q.hasMobKills()) {
                    final Iterator<Integer> iterator2 = q.getMobKills().keySet().iterator();
                    while (iterator2.hasNext()) {
                        final int mob = (int)Integer.valueOf(iterator2.next());
                        pse.setLong(1, rs.getLong(1));
                        pse.setInt(2, mob);
                        pse.setInt(3, q.getMobKills(mob));
                        pse.executeUpdate();
                    }
                }
                rs.close();
            }
            ps.close();
            pse.close();
            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte)32);
            ps.setByte(3, (byte)32);
            ps.setByte(4, (byte)32);
            ps.setByte(5, (byte)32);
            ps.setByte(6, (byte)60);
            ps.execute();
            ps.close();
            ps = con.prepareStatement("INSERT INTO mountdata (characterid, `Level`, `Exp`, `Fatigue`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            ps.setByte(2, (byte)1);
            ps.setInt(3, 0);
            ps.setByte(4, (byte)0);
            ps.execute();
            ps.close();
            final List<Pair<IItem, MapleInventoryType>> listing = new ArrayList<Pair<IItem, MapleInventoryType>>();
            for (final MapleInventory iv : chr.inventory) {
                for (final IItem item : iv.list()) {
                    listing.add(new Pair<IItem, MapleInventoryType>(item, iv.getType()));
                }
            }
            ItemLoader.INVENTORY.saveItems(listing, con, Integer.valueOf(chr.id));
            final int[] array1 = { 2, 3, 4, 5, 6, 7, 16, 17, 18, 19, 23, 25, 26, 27, 29, 31, 34, 35, 37, 38, 40, 41, 43, 44, 45, 46, 48, 50, 56, 57, 59, 60, 61, 62, 63, 64, 65 };
            final int[] array2 = { 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 4, 4, 4, 5, 5, 6, 6, 6, 6, 6, 6, 6 };
            final int[] array3 = { 10, 12, 13, 18, 24, 21, 8, 5, 0, 4, 1, 19, 14, 15, 52, 2, 17, 11, 3, 20, 16, 23, 9, 50, 51, 6, 22, 7, 53, 54, 100, 101, 102, 103, 104, 105, 106 };
            ps = con.prepareStatement("INSERT INTO keymap (characterid, `keye`, `type`, `action`) VALUES (?, ?, ?, ?)");
            ps.setInt(1, chr.id);
            for (int i = 0; i < array1.length; ++i) {
                ps.setInt(2, array1[i]);
                ps.setInt(3, array2[i]);
                ps.setInt(4, array3[i]);
                ps.execute();
            }
            ps.close();
        }
        catch (SQLException ex2) {}
        catch (DatabaseException e) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)e, "[角色存檔] 儲存角色資料失敗");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            try {
                con.rollback();
            }
            catch (SQLException ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "[角色存檔] 儲存失敗，繼續使用暫存檔不儲存資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
                if (con != null) {
                    con.close();
                }
            }
            catch (SQLException e2) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)e2, "[角色存檔] 錯誤自動返回儲存功能");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e2);
            }
        }
        finally {
            try {
                if (pse != null) {
                    pse.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
                con.setAutoCommit(true);
                con.setTransactionIsolation(4);
                if (con != null) {
                    con.close();
                }
            }
            catch (SQLException e3) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)e3, "[角色存檔] 錯誤自動返回儲存功能");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e3);
            }
        }
    }
    public int saveToDB(boolean dc, boolean fromcs) {
        return this.saveToDB(dc, fromcs, false);
    }
    public int saveToDB( boolean dc,  boolean fromcs, boolean newThread) {
        if (newThread) {
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    MapleCharacter.this.saveToDB(dc, fromcs, false);
                }
            });
            return 1;
        } else {
            if (this.isClone() || saveData > 0) {
                return -1;
            }
            if (!dc && this.isSaveingToDB()) {
                return -2;
            }
            this.saveData = 1;

            this.setBossLog1("打Boss数量", 1, this.打Boss数量);
            this.setBossLog1("打怪数量", 1, this.打怪数量);
//        this.setBossLog("打怪数量",1,this.打怪数量);
//        this.setBossLog("打怪数量",1,this.打怪数量);
//        this.setBossLog("打怪数量",1, (int) this.cunqianguan);
//        BOSS记录累计.forEach((a,b)->{
//            this.setBossLog(a+"",1,b);
//        });
//        BOSS记录.forEach((a,b)->{
//            this.setBossLog(a+"",0,b);
//        });
//        BOSS记录累计.clear();
//        BOSS记录.clear();
            this.追加伤害 = (this.打Boss数量 > 0 ? (long) Math.floor(this.打Boss数量 / LtMS.ConfigValuesMap.get("打BOSS追加伤害")) : 0L) + (this.打怪数量 > 0 ? (long) Math.floor(this.打怪数量 / LtMS.ConfigValuesMap.get("打小怪追加")) : 0L);
            this.打怪数量 = 0;
            this.打Boss数量 = 0;
            int retValue = 1;
            long time = System.currentTimeMillis();
            Connection con = null;
            PreparedStatement ps = null;
            PreparedStatement pse = null;
            ResultSet rs = null;
            try {
                con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
                con.setTransactionIsolation(1);
                con.setAutoCommit(false);
                ps = con.prepareStatement("UPDATE characters SET level = ?, fame = ?, str = ?, dex = ?, luk = ?, `int` = ?, exp = ?, hp = ?, mp = ?, maxhp = ?, maxmp = ?, sp = ?, ap = ?, gm = ?, skincolor = ?, gender = ?, job = ?, hair = ?, face = ?, map = ?, meso = ?, hpApUsed = ?, spawnpoint = ?, party = ?, buddyCapacity = ?, monsterbookcover = ?, dojo_pts = ?, dojoRecord = ?, pets = ?, subcategory = ?, marriageId = ?, currentrep = ?, totalrep = ?, charmessage = ?, expression = ?, constellation = ?, blood = ?, month = ?, jf = ?, zdjf = ?, rwjf = ?, zs = ?, cz = ?, dy = ?, rmb = ?, yb =?,  playerPoints = ?, playerEnergy = ?, jf1 = ?, jf2 = ?, jf3 = ?, jf4 = ?, jf5 = ?, jf6 = ?, jf7 = ?, jf8 = ?, jf9 = ?, jf10 = ?, day = ?, beans = ?, prefix = ?, PGSXDJ = ?,gachexp = ?, name = ?, VipMedal = ?, saved_faces = ?, saved_hairs = ? , max_damage = ? , exp_reserve = ?, imprison = ?, guildpoints = ?, stage = ?, break_level = ?, exp_rate = ?, meso_rate = ?, drop_rate = ? WHERE id = ?");
                ps.setInt(1, (int) this.level);
                ps.setShort(2, this.fame);
                ps.setShort(3, this.stats.getStr());
                ps.setShort(4, this.stats.getDex());
                ps.setShort(5, this.stats.getLuk());
                ps.setShort(6, this.stats.getInt());
                ps.setInt(7, this.exp);
                ps.setShort(8, (short) ((this.stats.getHp() < 1) ? 50 : this.stats.getHp() > this.stats.getMaxHp() ? this.stats.getMaxHp() : this.stats.getHp()));
                ps.setShort(9, this.stats.getMp());
                ps.setShort(10, this.stats.getMaxHp());
                ps.setShort(11, this.stats.getMaxMp());
                final StringBuilder sps = new StringBuilder();
                for (int i = 0; i < this.remainingSp.length; ++i) {
                    sps.append(this.remainingSp[i]);
                    sps.append(",");
                }
                final String sp = sps.toString();
                ps.setString(12, sp.substring(0, sp.length() - 1));
                ps.setShort(13, this.remainingAp);
                ps.setByte(14, this.gmLevel);
                ps.setByte(15, this.skinColor);
                ps.setByte(16, this.gender);
                ps.setShort(17, this.job);
                ps.setInt(18, this.hair);
                ps.setInt(19, this.face);
                if (!fromcs && this.map != null) {
                    if (this.map.getForcedReturnId() != 999999999) {
                        if (this.map.getId() == 220080001) {
                            ps.setInt(20, 910000000);
                        } else {
                            ps.setInt(20, this.map.getForcedReturnId());
                        }
                    } else {
                        ps.setInt(20, (this.stats.getHp() < 1) ? this.map.getReturnMapId() : this.map.getId());
                    }
                } else {
                    ps.setInt(20, this.mapid);
                }
                ps.setInt(21, this.meso);
                ps.setShort(22, this.hpmpApUsed);
                if (this.map == null) {
                    ps.setByte(23, (byte) 0);
                } else {
                    final MaplePortal closest = this.map.findClosestSpawnpoint(this.getPosition());
                    ps.setByte(23, (byte) ((closest != null) ? closest.getId() : 0));
                }
                ps.setInt(24, (this.party != null) ? this.party.getId() : -1);
                ps.setShort(25, (short) this.buddylist.getCapacity());
                ps.setInt(26, this.bookCover);
                ps.setInt(27, this.dojo);
                ps.setInt(28, (int) this.dojoRecord);
                final StringBuilder petz = new StringBuilder();
                int petLength = 0;
                for (final MaplePet pet : this.pets) {
                    pet.saveToDb();
                    if (pet.getSummoned()) {
                        petz.append((int) pet.getInventoryPosition());
                        petz.append(",");
                        ++petLength;
                    }
                }
                while (petLength < 3) {
                    petz.append("-1,");
                    ++petLength;
                }
                final String petstring = petz.toString();
                ps.setString(29, petstring.substring(0, petstring.length() - 1));
                ps.setByte(30, this.subcategory);
                ps.setInt(31, this.marriageId);
                ps.setInt(32, this.currentrep);
                ps.setInt(33, this.totalrep);
                ps.setString(34, this.charmessage);
                ps.setInt(35, this.expression);
                ps.setInt(36, this.constellation);
                ps.setInt(37, this.blood);
                ps.setInt(38, this.month);
                ps.setInt(39, this.jf);
                ps.setInt(40, this.zdjf);
                ps.setInt(41, this.rwjf);
                ps.setInt(42, this.zs);
                ps.setInt(43, this.cz);
                ps.setInt(44, this.dy);
                ps.setInt(45, this.rmb);
                ps.setInt(46, this.yb);
                ps.setInt(47, this.playerPoints);
                ps.setInt(48, this.playerEnergy);
                ps.setInt(49, this.jf1);
                ps.setInt(50, this.jf2);
                ps.setInt(51, this.jf3);
                ps.setInt(52, this.jf4);
                ps.setInt(53, this.jf5);
                ps.setInt(54, this.jf6);
                ps.setInt(55, this.jf7);
                ps.setInt(56, this.jf8);
                ps.setInt(57, this.jf9);
                ps.setInt(58, this.jf10);
                ps.setInt(59, this.day);
                ps.setInt(60, this.beans);
                ps.setString(61, this.prefix);
                ps.setInt(62, this.PGSXDJ);
                ps.setInt(63, this.gachexp);
                ps.setString(64, this.name);
                ps.setInt(65, (int) (this.Vip_Medal ? 1 : 0));
                final StringBuilder faces = new StringBuilder();
                for (int j = 0; j < this.savedFaces.length; ++j) {
                    faces.append(this.savedFaces[j]);
                    faces.append(",");
                }
                final String saved_faces = faces.toString();
                ps.setString(66, saved_faces.substring(0, saved_faces.length() - 1));
                final StringBuilder hairs = new StringBuilder();
                for (int k = 0; k < this.savedHairs.length; ++k) {
                    hairs.append(this.savedHairs[k]);
                    hairs.append(",");
                }
                final String saved_hairs = hairs.toString();
                ps.setString(67, saved_hairs.substring(0, saved_hairs.length() - 1));
//                ps.setInt(68, this.todayOnlineTime);
//                ps.setInt(69, this.totalOnlineTime);
                if (this.max_damage < 199999L) {
                    this.max_damage = 199999L;
                }

                ps.setLong(68, this.max_damage);
                ps.setLong(69, this.exp_reserve);
                ps.setByte(70, this.imprison);
                ps.setInt(71, this.guildPoints);
                ps.setInt(72, this.stage);
                ps.setInt(73, this.breakLevel);
                ps.setFloat(74, this.expRateChr);
                ps.setFloat(75, this.mesoRateChr);
                ps.setFloat(76, this.dropRateChr);
                ps.setInt(77, this.id);

                if (ps.executeUpdate() < 1) {
                    ps.close();
                    throw new DatabaseException("Character not in database (" + this.id + ")");
                }
                ps.close();
                this.deleteWhereCharacterId(con, "DELETE FROM skillmacros WHERE characterid = ?");
                //System.out.println("移除技能宏");
                for (int l = 0; l < 5; ++l) {
                    //System.out.println("新增技能宏");
                    final SkillMacro macro = this.skillMacros[l];
                    if (macro != null) {
                        ps = con.prepareStatement("INSERT INTO skillmacros (characterid, skill1, skill2, skill3, name, shout, position) VALUES (?, ?, ?, ?, ?, ?, ?)");
                        ps.setInt(1, this.id);
                        ps.setInt(2, macro.getSkill1());
                        ps.setInt(3, macro.getSkill2());
                        ps.setInt(4, macro.getSkill3());
                        ps.setString(5, macro.getName());
                        ps.setInt(6, macro.getShout());
                        ps.setInt(7, l);
                        ps.execute();
                        ps.close();
                    }
                }
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.2累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.3累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setInt(1, this.id);
                ps.setByte(2, this.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
                ps.setByte(3, this.getInventory(MapleInventoryType.USE).getSlotLimit());
                ps.setByte(4, this.getInventory(MapleInventoryType.SETUP).getSlotLimit());
                ps.setByte(5, this.getInventory(MapleInventoryType.ETC).getSlotLimit());
                ps.setByte(6, this.getInventory(MapleInventoryType.CASH).getSlotLimit());
                ps.execute();
                ps.close();
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.4累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                //保存装备数据
                this.saveInventory(con);
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.5累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.deleteWhereCharacterId(con, "DELETE FROM questinfo WHERE characterid = ?");
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.6累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                ps = con.prepareStatement("INSERT INTO questinfo (`characterid`, `quest`, `customData`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (final Entry<Integer, String> q : this.questinfo.entrySet()) {
                    ps.setInt(2, (int) Integer.valueOf(q.getKey()));
                    ps.setString(3, (String) q.getValue());
                    ps.execute();
                }
                ps.close();
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.7累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.deleteWhereCharacterId(con, "DELETE FROM queststatus WHERE characterid = ?");
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置1.8累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                ps = con.prepareStatement("INSERT INTO queststatus (`queststatusid`, `characterid`, `quest`, `status`, `time`, `forfeited`, `customData`) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)", 1);
                pse = con.prepareStatement("INSERT INTO queststatusmobs VALUES (DEFAULT, ?, ?, ?)", 1);
                ps.setInt(1, this.id);
                for (final MapleQuestStatus q2 : this.quests.values()) {
                    ps.setInt(2, q2.getQuest().getId());
                    ps.setInt(3, (int) q2.getStatus());
                    ps.setInt(4, (int) (q2.getCompletionTime() / 1000L));
                    ps.setInt(5, q2.getForfeited());
                    ps.setString(6, q2.getCustomData());
                    ps.executeUpdate();
                    rs = ps.getGeneratedKeys();
                    rs.next();
                    if (q2.hasMobKills()) {
                        final Iterator<Integer> iterator4 = q2.getMobKills().keySet().iterator();
                        while (iterator4.hasNext()) {
                            final int mob = (int) Integer.valueOf(iterator4.next());
                            pse.setLong(1, rs.getLong(1));
                            pse.setInt(2, mob);
                            pse.setInt(3, q2.getMobKills(mob));
                            pse.executeUpdate();
                        }
                    }
                    rs.close();
                }
                ps.close();
                pse.close();
                this.deleteWhereCharacterId(con, "DELETE FROM skills WHERE characterid = ?");
                ps = con.prepareStatement("INSERT INTO skills (characterid, skillid, skilllevel, masterlevel, expiration) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, this.id);
                for (final Entry<ISkill, SkillEntry> skill : this.skills.entrySet()) {
                    if (GameConstants.isApplicableSkill(((ISkill) skill.getKey()).getId())) {
                        ps.setInt(2, ((ISkill) skill.getKey()).getId());
                        ps.setByte(3, ((SkillEntry) skill.getValue()).skillevel);
                        ps.setByte(4, ((SkillEntry) skill.getValue()).masterlevel);
                        ps.setLong(5, ((SkillEntry) skill.getValue()).expiration);
                        ps.execute();
                    }
                }
                ps.close();

                //技能冷却
                try {
                    final List<MapleCoolDownValueHolder> cd = this.getCooldowns();
                    if (dc && cd.size() > 0) {
                        ps = con.prepareStatement("INSERT INTO skills_cooldowns (charid, SkillID, StartTime, length) VALUES (?, ?, ?, ?)");
                        ps.setInt(1, this.getId());
                        for (final MapleCoolDownValueHolder cooling : cd) {
                            ps.setInt(2, cooling.skillId);
                            ps.setLong(3, cooling.startTime);
                            ps.setLong(4, cooling.length);
                            ps.execute();
                        }
                        ps.close();
                    }
                } catch (SQLException e) {
                    System.out.println("Error saving cooldowns: " + e.getMessage());
                }

                try {
                    this.deleteWhereCharacterId(con, "DELETE FROM savedlocations WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO savedlocations (characterid, `locationtype`, `map`) VALUES (?, ?, ?)");
                    ps.setInt(1, this.id);
                    for (final SavedLocationType savedLocationType : SavedLocationType.values()) {
                        if (this.savedLocations[savedLocationType.getValue()] != -1) {
                            ps.setInt(2, savedLocationType.getValue());
                            ps.setInt(3, this.savedLocations[savedLocationType.getValue()]);
                            ps.execute();
                        }
                    }
                    ps.close();
                } catch (SQLException e) {
                    System.out.println("Error saving saved locations: " + e.getMessage());
                }
                ps = con.prepareStatement("DELETE FROM achievements WHERE accountid = ?");
                ps.setInt(1, this.accountid);
                ps.executeUpdate();
                ps.close();
                ps = con.prepareStatement("INSERT INTO achievements(charid, achievementid, accountid) VALUES(?, ?, ?)");
                for (final Integer achid : this.finishedAchievements) {
                    ps.setInt(1, this.id);
                    ps.setInt(2, (int) achid);
                    ps.setInt(3, this.accountid);
                    ps.executeUpdate();
                }
                ps.close();

                this.deleteWhereCharacterId(con, "DELETE FROM buddies WHERE characterid = ?");

                ps = con.prepareStatement("INSERT INTO buddies (characterid, `buddyid`, `pending`) VALUES (?, ?, ?)");
                ps.setInt(1, this.id);
                for (final BuddyEntry entry : this.buddylist.getBuddies()) {
                    if (entry != null) {
                        ps.setInt(2, entry.getCharacterId());
                        ps.setInt(3, (int) (entry.isVisible() ? 0 : 1));
                        ps.execute();
                    }
                }
                ps.close();
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置2.8累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                ps = con.prepareStatement("UPDATE accounts SET `mPoints` = ?, `vpoints` = ?, `VIP` = ? WHERE id = ?");
                ps.setInt(1, this.maplepoints);
                ps.setInt(2, this.vpoints);
                ps.setInt(3, this.vip);
                ps.setInt(4, this.client.getAccID());
                ps.execute();
                ps.close();
                if (this.storage != null) {
                    this.storage.saveToDB(con);
                }
                try {
                    if (this.cs != null) {
                        this.cs.save(con);
                    }
                } catch (SQLException e) {
                    FileoutputUtil.outError("logs/物品保存异常.txt", (Throwable) e);
                }
                PlayerNPC.updateByCharId(this, con);

                this.keylayout.saveKeys(this.id, con);

                this.mount.saveMount(this.id, con);

                if (this.monsterbook != null) {
                    this.monsterbook.saveCards(this.id, con);
                }

                this.deleteWhereCharacterId(con, "DELETE FROM wishlist WHERE characterid = ?");

                for (int m = 0; m < this.getWishlistSize(); ++m) {
                    ps = con.prepareStatement("INSERT INTO wishlist(characterid, sn) VALUES(?, ?) ");
                    ps.setInt(1, this.getId());
                    ps.setInt(2, this.wishlist[m]);
                    ps.execute();
                    ps.close();
                }
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置3.7累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.deleteWhereCharacterId(con, "DELETE FROM trocklocations WHERE characterid = ?");
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置3.8累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                for (int m = 0; m < this.rocks.length; ++m) {
                    if (this.rocks[m] != 999999999) {
                        ps = con.prepareStatement("INSERT INTO trocklocations(characterid, mapid) VALUES(?, ?) ");
                        ps.setInt(1, this.getId());
                        ps.setInt(2, this.rocks[m]);
                        ps.execute();
                        ps.close();
                    }
                }
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置3.9累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.deleteWhereCharacterId(con, "DELETE FROM regrocklocations WHERE characterid = ?");
                for (int m = 0; m < this.regrocks.length; ++m) {
                    if (this.regrocks[m] != 999999999) {
                        ps = con.prepareStatement("INSERT INTO regrocklocations(characterid, mapid) VALUES(?, ?) ");
                        ps.setInt(1, this.getId());
                        ps.setInt(2, this.regrocks[m]);
                        ps.execute();
                        ps.close();
                    }
                }
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置4.0累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.loadMountListFromDB();
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置4.1累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                EquipFieldEnhancement.getInstance().saveChrToDB(this.id, (Connection) null);
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置4.2累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.skillSkin.saveChrSkill();
                //                if (this.getGMLevel() > 0) {
                //                    this.dropMessage(5, "存档位置4.3累计耗时：" + (System.currentTimeMillis() - time));
                //                }
                this.saveMountListToDB(con);
                if (this.getGMLevel() > 0) {
                    this.dropMessage(5, "最终存档累计耗时：" + (System.currentTimeMillis() - time));
                }
                this.saveMonsterKillQuestToDB(con);

                con.commit();
            } catch (UnsupportedOperationException ex2) {
            } catch (SQLException ex3) {
            } catch (DatabaseException e) {
                retValue = 0;
                FileoutputUtil.logToFile("logs/保存角色数据出錯.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getClient().getAccountName() + " 账号ID " + this.getClient().getAccID() + " 角色名 " + this.getName() + " 角色ID " + this.getId());
                FilePrinter.printError("MapleCharacter.txt", (Throwable) e, "[角色存檔]儲存角色失敗");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable) e);
                FileoutputUtil.outError("logs/保存角色数据出錯.txt", (Throwable) e);
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    FileoutputUtil.logToFile("logs/保存角色数据出錯.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getClient().getAccountName() + " 账号ID " + this.getClient().getAccID() + " 角色名 " + this.getName() + " 角色ID " + this.getId());
                    FileoutputUtil.outError("logs/保存角色数据出錯.txt", (Throwable) ex);
                    FilePrinter.printError("MapleCharacter.txt", (Throwable) e, "[角色存檔] 儲存失敗，繼續使用暫存檔不儲存資料庫");
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable) ex);
                }
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    if (pse != null) {
                        pse.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                    con.setAutoCommit(true);
                    con.setTransactionIsolation(4);
                    if (con != null) {
                        con.close();
                    }
                } catch (SQLException es) {
                    retValue = 0;
                    FileoutputUtil.logToFile("logs/保存角色数据出錯.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getClient().getAccountName() + " 账号ID " + this.getClient().getAccID() + " 角色名 " + this.getName() + " 角色ID " + this.getId());
                    FilePrinter.printError("MapleCharacter.txt", (Throwable) es, "[角色存檔] 錯誤自動返回儲存功能");
                    FileoutputUtil.outError("logs/保存角色数据出錯.txt", (Throwable) es);
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable) es);
                }
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    if (pse != null) {
                        pse.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                    con.setAutoCommit(true);
                    con.setTransactionIsolation(4);
                    if (con != null) {
                        con.close();
                    }
                } catch (SQLException es2) {
                    retValue = 0;
                    FileoutputUtil.logToFile("logs/保存角色数据出錯.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getClient().getAccountName() + " 账号ID " + this.getClient().getAccID() + " 角色名 " + this.getName() + " 角色ID " + this.getId());
                    FilePrinter.printError("MapleCharacter.txt", (Throwable) es2, "[角色存檔] 錯誤自動返回儲存功能");
                    FileoutputUtil.outError("logs/保存角色数据出錯.txt", (Throwable) es2);
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable) es2);
                }
                this.saveData = 0;
            }
            return retValue;
        }
    }
    public SkillSkin getSkillSkin() {
        return this.skillSkin;
    }

    public boolean sendSkillSkin(int skillId) {
        long nowTime = System.currentTimeMillis();
        if (nowTime - this.lastSkillSkinTime < 200L) {
            return false;
        } else {
            this.lastSkillSkinTime = nowTime;
            if (this.skillSkin.getChrSkillType(skillId) > 0) {
                Pair<Integer, Integer> effSkill = this.skillSkin.getChrSkillEff(skillId);
                if (effSkill != null) {
                    this.sendSkillEffect((Integer)effSkill.left, (Integer)effSkill.right);
                    return true;
                } else {
                    return false;
                }
            } else if (skillId == 1211002) {
                for (PlayerBuffValueHolder buff : this.getAllBuffs()) {
                    switch (buff.effect.getSourceId()) {
                        case 1211004:
                        case 1211006:
                        case 1211008:
                        case 1221004:
                            Pair<Integer, Integer> effSkill = this.skillSkin.getChrSkillEffS(buff.effect.getSourceId());
                            if (effSkill != null) {
                                this.sendSkillEffect((Integer)effSkill.left, (Integer)effSkill.right);
                                return true;
                            }
                            return false;
                    }
                }
                return false;
            } else {
                return false;
            }
        }
    }

    public void setSkillSkinAll(int skillType) {
        this.skillSkin.setChrSkillTypeAll(skillType);
    }

    public boolean setSkillSkin(int skillId, int skillType) {
        return this.skillSkin.setChrSkillType(skillId, skillType);
    }
    private void deleteWhereCharacterId(Connection con, final String sql) throws SQLException {
        deleteWhereCharacterId(con, sql, this.id);
    }

    public static void deleteWhereCharacterId(Connection con, final String sql, final int id) {
        try {
            final PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception ex) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "[deleteWhereCharacterId]");
        }
    }
    
    public void saveInventory(Connection con) {
        final List<Pair<IItem, MapleInventoryType>> listing = new ArrayList<Pair<IItem, MapleInventoryType>>();
        for (final MapleInventory iv : this.inventory) {
            for (final IItem item : iv.list()) {
                listing.add(new Pair<IItem, MapleInventoryType>(item, iv.getType()));
            }
        }
        if (con != null) {
            try {
                ItemLoader.INVENTORY.saveItems(listing, con, Integer.valueOf(this.id));
            }
            catch (SQLException ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "[saveInventory]");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
        }
        else {
            try {
                ItemLoader.INVENTORY.saveItems(listing, Integer.valueOf(this.id));
            }
            catch (SQLException ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "[saveInventory]");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
        }
    }
    
    public final PlayerStats getStat() {
        return this.stats;
    }
    
    public final PlayerRandomStream CRand() {
        return this.CRand;
    }
    
    public void QuestInfoPacket(final MaplePacketLittleEndianWriter mplew) {
        mplew.writeShort(this.questinfo.size());
        for (final Entry<Integer, String> q : this.questinfo.entrySet()) {
            mplew.writeShort((int)Integer.valueOf(q.getKey()));
            mplew.writeMapleAsciiString((q.getValue() == null) ? "" : ((String)q.getValue()));
        }
    }
    
    public void updateInfoQuest(final int questid, final String data) {
        this.questinfo.put(Integer.valueOf(questid), data);
        this.client.sendPacket(MaplePacketCreator.updateInfoQuest(questid, data));
    }
    
    public final String getInfoQuest(final int questid) {
        if (this.questinfo.containsKey((Object)Integer.valueOf(questid))) {
            return (String)this.questinfo.get((Object)Integer.valueOf(questid));
        }
        return "";
    }
    
    public final int getNumQuest() {
        int i = 0;
        for (final MapleQuestStatus q : this.quests.values()) {
            if (q.getStatus() == 2 && !q.isCustom()) {
                ++i;
            }
        }
        return i;
    }
    
    public final byte getQuestStatus(final int quest) {
        return this.getQuest(MapleQuest.getInstance(quest)).getStatus();
    }
    
    public final MapleQuestStatus getQuest(final MapleQuest quest) {
        if (!this.quests.containsKey((Object)quest)) {
            return new MapleQuestStatus(quest, 0);
        }
        return (MapleQuestStatus)this.quests.get((Object)quest);
    }
    
    public void setQuestAdd(final MapleQuest quest, final byte status, final String customData) {
        if (!this.quests.containsKey((Object)quest)) {
            final MapleQuestStatus stat = new MapleQuestStatus(quest, (int)status);
            stat.setCustomData(customData);
            this.quests.put(quest, stat);
        }
    }
    
    public final MapleQuestStatus getQuestNAdd(final MapleQuest quest) {
        if (!this.quests.containsKey((Object)quest)) {
            final MapleQuestStatus status = new MapleQuestStatus(quest, 0);
            this.quests.put(quest, status);
            return status;
        }
        return (MapleQuestStatus)this.quests.get((Object)quest);
    }
    
    public final MapleQuestStatus getQuestNoAdd(final MapleQuest quest) {
        return (MapleQuestStatus)this.quests.get((Object)quest);
    }
    
    public final MapleQuestStatus getQuestRemove(final MapleQuest quest) {

        return (MapleQuestStatus)this.quests.remove((Object)quest);
    }
    
    public void updateQuest(final MapleQuestStatus quest) {
        this.updateQuest(quest, false);
    }
    
    public void updateQuest(final MapleQuestStatus quest, final boolean update) {
        this.quests.put(quest.getQuest(), quest);
        if (!quest.isCustom()) {
            this.client.sendPacket(MaplePacketCreator.updateQuest(quest));
            if (quest.getStatus() == 1 && !update) {
                this.client.sendPacket(MaplePacketCreator.updateQuestInfo(this, quest.getQuest().getId(), quest.getNpc(), (byte)8));
            }
        }
    }
    
    public final Map<Integer, String> getInfoQuest_Map() {
        return this.questinfo;
    }
    
    public final Map<MapleQuest, MapleQuestStatus> getQuest_Map() {
        return this.quests;
    }
    
    public boolean isActiveBuffedValue(final int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isBuffedValue(final int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            try {
                if (mbsvh.effect.getSourceId() == skillid) {
                    return true;
                }
            }
            catch (Exception e) {}
        }
        return false;
    }
    
    public Integer getBuffedValue(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)effect);
        return (mbsvh == null) ? null : Integer.valueOf(mbsvh.value);
    }
    
    public boolean hasBuffedValue(final MapleBuffStat effect) {
        return this.getBuffedValue(effect) != null;
    }
    
    public final Integer getBuffedSkill_X(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.effect.getX());
    }
    
    public final Integer getBuffedSkill_Y(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)effect);
        if (mbsvh == null) {
            return null;
        }
        return Integer.valueOf(mbsvh.effect.getY());
    }
    
    public boolean isBuffFrom(final MapleBuffStat stat, final ISkill skill) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)stat);
        return mbsvh != null && mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skill.getId();
    }
    
    public boolean changeFace(final short item, final int color) {
        int newFace = this.face / 1000 * 1000 + color + this.face % 10;
        if (!MapleItemInformationProvider.getInstance().faceExists(newFace)) {
            newFace = this.face;
            this.gainItem((int)item, 1);
        }
        else {
            this.face = newFace;
            this.updateSingleStat(MapleStat.FACE, newFace);
            this.equipChanged();
        }
        return MapleItemInformationProvider.faceLists.containsKey((Object)Integer.valueOf(color));
    }
    
    public int getBuffSource(final MapleBuffStat stat) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)stat);
        return (mbsvh == null) ? -1 : mbsvh.effect.getSourceId();
    }
    
    public int getItemQuantity(final int itemid, final boolean checkEquipped) {
        int possesed = this.inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        if (checkEquipped) {
            possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        return possesed;
    }
    public int itemQuantity(final int itemid) {
        return this.inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
    }
    public int itemQuantityF(final int itemid) {
        int possesed = this.inventory[GameConstants.getInventoryType(itemid).ordinal()].countById(itemid);
        possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        return possesed;
    }
    public void setBuffedValue(final MapleBuffStat effect, final int value) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)effect);
        if (mbsvh == null) {
            return;
        }
        mbsvh.value = value;
    }
    
    public Long getBuffedStarttime(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)effect);
        return (mbsvh == null) ? null : Long.valueOf(mbsvh.startTime);
    }
    
    public MapleStatEffect getStatForBuff(final MapleBuffStat effect) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.get((Object)effect);
        return (mbsvh == null) ? null : mbsvh.effect;
    }
    
    public void doRecovery() {
        final MapleStatEffect bloodEffect = this.getStatForBuff(MapleBuffStat.RECOVERY);
        if (bloodEffect != null) {
            this.prepareRecovery();
            if (this.stats.getHp() >= this.stats.getCurrentMaxHp()) {
                this.cancelEffectFromBuffStat(MapleBuffStat.RECOVERY);
            }
            else {
                this.healHP(bloodEffect.getX(), true);
            }
        }
    }
    
    public final boolean canRecover(final long now) {
        return this.lastRecoveryTime > 0L && this.lastRecoveryTime + 5000L < now;
    }
    
    private void prepareRecovery() {
        this.lastRecoveryTime = System.currentTimeMillis();
    }
    
    private void prepareDragonBlood(final MapleStatEffect bloodEffect) {
        if (this.dragonBloodSchedule != null) {
            this.dragonBloodSchedule.cancel(false);
        }
        this.dragonBloodSchedule = BuffTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (stats.getHp() - bloodEffect.getX() > 1) {
                    MapleCharacter.this.cancelBuffStats(MapleBuffStat.DRAGONBLOOD);
                }
                else {
                    MapleCharacter.this.addHP(-bloodEffect.getX());
                    client.sendPacket(MaplePacketCreator.showOwnBuffEffect(bloodEffect.getSourceId(), 5));
                    map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(MapleCharacter.this.getId(), bloodEffect.getSourceId(), 5), false);
                }
            }
        }, 4000L, 4000L);
    }
    
    public void startMapTimeLimitTask(int time, final MapleMap to) {
        this.client.sendPacket(MaplePacketCreator.getClock(time));
        time *= 1000;
        this.mapTimeLimitTask = MapTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                MapleCharacter.this.changeMap(to, to.getPortal(0));
            }
        }, (long)time, (long)time);
    }
    
    public void startFishingTask(final boolean VIP) {
        try {
            final int time = GameConstants.getFishingTime(VIP, this.isGM());
            this.dropMessage(5, "开始钓鱼：请注意检查背包空间");
            this.dropMessage(5, "钓鱼时间：" + time + "豪秒");
            this.cancelFishingTask();
            this.fishing = EtcTimer.getInstance().register((Runnable)new Runnable() {
                @Override
                public void run() {
                    final boolean expMulti = MapleCharacter.this.haveItem(2300001, 1, false, true);
                    if (!expMulti && !MapleCharacter.this.haveItem(2300000, 1, false, true)) {
                        MapleCharacter.this.cancelFishingTask();
                        return;
                    }
                    MapleInventoryManipulator.removeById(client, MapleInventoryType.USE, expMulti ? 2300001 : 2300000, 1, false, false);
                    final int rewardType = FishingRewardFactory.getInstance().getNextRewardType();
                    switch (rewardType) {
                        case 0: {
                            final int money = Randomizer.rand(expMulti ? 15 : 10, expMulti ? 2000 : 1000);
                            MapleCharacter.this.gainMeso(money, true);
                            client.sendPacket(UIPacket.fishingUpdate((byte)1, money));
                            break;
                        }
                        case 1: {
                            final int experi = Randomizer.nextInt(Math.abs(GameConstants.getExpNeededForLevel((int)level) / 200) + 1);
                            MapleCharacter.this.gainExp(expMulti ? (experi * 3 / 2) : experi, true, false, true);
                            client.sendPacket(UIPacket.fishingUpdate((byte)2, experi));
                            break;
                        }
                        default: {
                            final int gl = Randomizer.nextInt(2);
                            if (gl == 1) {
                                final FishingReward item = FishingRewardFactory.getInstance().getNextRewardItemId();
                                if (item != null) {
                                    if (!MapleInventoryManipulator.checkSpace(client, item.getItemId(), 1, MapleCharacter.this.getName())) {
                                        client.sendPacket(MaplePacketCreator.serverNotice(5, "温馨提示：要保持背包空位足够哦，满了是不能获得钓鱼物品的哦"));
                                        return;
                                    }
                                    MapleInventoryManipulator.addById(client, item.getItemId(), (short)1, GameConstants.isChair(item.getItemId()) ? MapleCharacter.this.getName() : null, null, (long)item.getExpiration());
                                    client.sendPacket(UIPacket.fishingUpdate((byte)0, item.getItemId()));
                                }
                                break;
                            }
                            final int moneya = Randomizer.rand(expMulti ? 15 : 10, expMulti ? 2000 : 1000);
                            MapleCharacter.this.gainMeso(moneya, true);
                            client.sendPacket(UIPacket.fishingUpdate((byte)1, moneya));
                            break;
                        }
                    }
                    map.broadcastMessage(UIPacket.fishingCaught(id));
                    final int tmp = Randomizer.nextInt(10000);
                    if (tmp == 5000) {
                        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8510000), new Point(1000, 385));
                        map.spawnMonsterOnGroundBelow(MapleLifeFactory.getMonster(8520000), new Point(0, 385));
                        MapleCharacter.this.dropMessage(5, "哇！鱼王出现了！快跑啊！");
                        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "哇！ " + MapleCharacter.this.getName() + "钓鱼钓出来鱼王了，大家快跑！！！"));
                    }
                }
            }, (long)time, (long)time);
        }
        catch (RejectedExecutionException ex) {}
    }
    
    public void cancelMapTimeLimitTask() {
        if (this.mapTimeLimitTask != null) {
            this.mapTimeLimitTask.cancel(false);
        }
    }
    
    public void cancelFishingTask() {
        if (this.fishing != null && !this.fishing.isCancelled()) {
            this.fishing.cancel(false);
        }
    }
    
    public void registerEffect(final MapleStatEffect effect, final long starttime, final ScheduledFuture<?> schedule, final int from) {
        this.registerEffect(effect, starttime, schedule, effect.getStatups(), false, effect.getDuration(), from);
    }
    
    public void registerEffect(final MapleStatEffect effect, final long starttime, final ScheduledFuture<?> schedule, final List<Pair<MapleBuffStat, Integer>> statups, final boolean silent, final int localDuration, final int cid) {
        if (effect.isHide() && this.isGM()) {
            this.hidden = true;
            this.map.broadcastNONGMMessage(this, MaplePacketCreator.removePlayerFromMap(this.getId()), false);
        }
        else if (effect.isDragonBlood()) {
            this.prepareDragonBlood(effect);
        }
        else if (effect.isBerserk()) {
            this.checkBerserk();
        }
        else if (effect.isMonsterRiding_()) {
            this.getMount().startSchedule();
        }
        else if (effect.isBeholder()) {
            //召唤技能
            this.prepareBeholderEffect(1321007);
        }
        else if (effect.isBeholder1()) {
            this.prepareBeholderEffect(1321011);
        }
        else if (effect.isRecovery()) {
            this.prepareRecovery();
        }
        else if (GameConstants.isAran((int)this.getJob())) {
            final int reduce = this.Aran_ReduceCombo(effect.getSourceId());
            if (reduce > 0) {
                this.setCombo(this.getCombo() - reduce);
            }
        }
        int clonez = 0;
        for (final Pair<MapleBuffStat, Integer> statup : statups) {
            if (statup.getLeft() == MapleBuffStat.ILLUSION) {
                clonez = (int)Integer.valueOf(statup.getRight());
            }
            final int value = (int)Integer.valueOf(statup.getRight());
            if (statup.getLeft() == MapleBuffStat.MONSTER_RIDING && effect.getSourceId() == 5221006 && this.battleshipHP <= 0) {
                this.battleshipHP = value;
            }
            final MapleBuffStatValueHolder mbsvh = new MapleBuffStatValueHolder(effect, starttime, schedule, value, localDuration, cid, effect.getSourceId());

            this.effects.put(statup.getLeft(), mbsvh);
            this.skillID.put(Integer.valueOf(effect.getSourceId()), mbsvh);
        }
        if (clonez > 0) {
            final int cloneSize = Math.max((int)this.getNumClones(), this.getCloneSize());
            if (clonez > cloneSize) {
                for (int i = 0; i < clonez - cloneSize; ++i) {
                    this.cloneLook();
                }
            }
        }
        this.stats.recalcLocalStats();
        if (this.getDebugMessage()) {
            for (final Pair<MapleBuffStat, Integer> buf : statups) {
                this.dropMessage(6, "[系統提示]\u0010" + ((MapleBuffStat)buf.getLeft()).toString() + "(0x" + HexTool.toString(((MapleBuffStat)buf.getLeft()).getValue()) + ")");
            }
        }
    }
    
    public List<MapleBuffStat> getBuffStats(final MapleStatEffect effect, final long startTime) {
        final List<MapleBuffStat> bstats = new ArrayList<MapleBuffStat>();
        final Map<MapleBuffStat, MapleBuffStatValueHolder> allBuffs = new EnumMap<MapleBuffStat, MapleBuffStatValueHolder>((Map<MapleBuffStat, ? extends MapleBuffStatValueHolder>)this.effects);
        for (final Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : allBuffs.entrySet()) {
            final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)stateffect.getValue();
            if (mbsvh.effect.sameSource(effect) && (startTime == -1L || startTime == mbsvh.startTime)) {
                bstats.add(stateffect.getKey());
            }
        }
        return bstats;
    }
    
    public List<MapleBuffStat> getBuffStatsFromStatEffect(final MapleStatEffect effect, final long startTime) {
        final List<MapleBuffStat> bstats = new ArrayList<MapleBuffStat>();
        final Map<MapleBuffStat, MapleBuffStatValueHolder> allBuffs = new EnumMap<MapleBuffStat, MapleBuffStatValueHolder>((Map<MapleBuffStat, ? extends MapleBuffStatValueHolder>)this.effects);
        for (final Entry<MapleBuffStat, MapleBuffStatValueHolder> stateffect : allBuffs.entrySet()) {
            final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)stateffect.getValue();
            if (mbsvh.effect.sameSource(effect) && (startTime == -1L || startTime == mbsvh.startTime)) {
                bstats.add(stateffect.getKey());
                this.skillID.put(Integer.valueOf(effect.getSourceId()), mbsvh);
            }
        }
        return bstats;
    }
    
    private boolean deregisterBuffStats(final List<MapleBuffStat> stats) {
        boolean clonez = false;
        final List<MapleBuffStatValueHolder> effectsToCancel = new ArrayList<MapleBuffStatValueHolder>(stats.size());
        for (final MapleBuffStat stat : stats) {
            final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.remove((Object)stat);
            if (mbsvh != null) {
                this.skillID.remove((Object)Integer.valueOf(mbsvh.effect.getSourceId()));
                boolean addMbsvh = true;
                for (final MapleBuffStatValueHolder contained : effectsToCancel) {
                    if (mbsvh.startTime == contained.startTime && contained.effect == mbsvh.effect) {
                        addMbsvh = false;
                    }
                }
                if (addMbsvh) {
                    effectsToCancel.add(mbsvh);
                }
                if (stat == MapleBuffStat.SUMMON || stat == MapleBuffStat.PUPPET) {
                    final int summonId = mbsvh.effect.getSourceId();
                    final MapleSummon summon = (MapleSummon)this.summons.get((Object)Integer.valueOf(summonId));
                    if (summon == null) {
                        continue;
                    }
                    this.map.broadcastMessage(MaplePacketCreator.removeSummon(summon, true));
                    this.map.removeMapObject((MapleMapObject)summon);
                    this.removeVisibleMapObject((MapleMapObject)summon);
                    this.summons.remove((Object)Integer.valueOf(summonId));
                    if (summon.getSkill() != 1321007) {
                        continue;
                    }
                    if (this.beholderHealingSchedule != null) {
                        this.beholderHealingSchedule.cancel(false);
                        this.beholderHealingSchedule = null;
                    }
                    if (this.beholderBuffSchedule == null) {
                        continue;
                    }
                    this.beholderBuffSchedule.cancel(false);
                    this.beholderBuffSchedule = null;
                }
                else if (stat == MapleBuffStat.DRAGONBLOOD) {
                    if (this.dragonBloodSchedule == null) {
                        continue;
                    }
                    this.dragonBloodSchedule.cancel(false);
                    this.dragonBloodSchedule = null;
                }
                else if (stat == MapleBuffStat.RECOVERY) {
                    this.lastRecoveryTime = 0L;
                }
                else {
                    if (stat != MapleBuffStat.ILLUSION) {
                        continue;
                    }
                    this.disposeClones();
                    clonez = true;
                }
            }
        }
        for (final MapleBuffStatValueHolder cancelEffectCancelTasks : effectsToCancel) {
            if (this.getBuffStatsFromStatEffect(cancelEffectCancelTasks.effect, cancelEffectCancelTasks.startTime).isEmpty() && cancelEffectCancelTasks.schedule != null) {
                cancelEffectCancelTasks.schedule.cancel(false);
            }
        }
        return clonez;
    }
    public void setMorph(int itemId, int morphId, int localDuration, boolean cancelable) {
        this.setMorph(itemId, morphId, localDuration, cancelable, false);
    }
    public void setMorph(int itemId, int morphId, int localDuration, boolean cancelable, boolean isSkill) {
        MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect_s(itemId, false);
        effect.setMorph(morphId);
        effect.setDuration(localDuration);
        effect.setCancelMorph(cancelable);
        if (!cancelable) {
            long nowTime = Calendar.getInstance().getTimeInMillis();
            this.noCancelBuffMap.put(effect.getSourceId(), new Pair(nowTime, effect.getDuration()));
        }

        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair(MapleBuffStat.MORPH, effect.getMorph(this)));
        this.getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this.getId(), stat, effect), false);
        ArrayList<Pair<MapleBuffStat, Integer>> Selfstat = new ArrayList();
        Selfstat.add(new Pair(MapleBuffStat.JUMP, 0));
        Selfstat.add(new Pair(MapleBuffStat.WDEF, 0));
        Selfstat.add(new Pair(MapleBuffStat.MDEF, 0));
        Selfstat.add(new Pair(MapleBuffStat.SPEED, 0));
        Selfstat.add(new Pair(MapleBuffStat.MORPH, morphId));
        this.cancelEffect(effect, true, -1L, Selfstat);
        if (isSkill) {
            this.getClient().sendPacket(MaplePacketCreator.giveBuff(itemId, localDuration, Selfstat, effect));
        } else {
            this.getClient().sendPacket(MaplePacketCreator.giveBuff(-itemId, localDuration, Selfstat, effect));
        }

        long starttime = System.currentTimeMillis();
        MapleStatEffect.CancelEffectAction cancelAction = new MapleStatEffect.CancelEffectAction(this, effect, starttime);

        try {
            ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, starttime + (long)localDuration - System.currentTimeMillis());
            this.registerEffect(effect, starttime, schedule, Selfstat, false, localDuration, this.getId());
        } catch (Exception var13) {
            服务端输出信息.println_err("MapleCharacter.setMorph 发生错误，错误原因：" + var13);
        }

    }
    public void setMorphForSendDamage(int itemId, int morphId, int localDuration, boolean cancelable) {
        MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect_s(itemId, false);
        effect.setMorph(morphId);
        effect.setDuration(localDuration);
        effect.setCancelMorph(cancelable);
        List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair(MapleBuffStat.MORPH, effect.getMorph(this)));
        this.getMap().broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this.getId(), stat, effect), false);
        ArrayList<Pair<MapleBuffStat, Integer>> Selfstat = new ArrayList();
        Selfstat.add(new Pair(MapleBuffStat.JUMP, 0));
        Selfstat.add(new Pair(MapleBuffStat.WDEF, 0));
        Selfstat.add(new Pair(MapleBuffStat.MDEF, 0));
        Selfstat.add(new Pair(MapleBuffStat.SPEED, 0));
        Selfstat.add(new Pair(MapleBuffStat.MORPH, morphId));
        this.cancelEffect(effect, true, -1L, Selfstat);
        this.getClient().sendPacket(MaplePacketCreator.giveBuff(-itemId, localDuration, Selfstat, effect));
        long starttime = System.currentTimeMillis();
        MapleStatEffect.CancelEffectAction cancelAction = new MapleStatEffect.CancelEffectAction(this, effect, starttime);

        try {
            ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, starttime + (long)localDuration - System.currentTimeMillis());
            this.registerEffect(effect, starttime, schedule, Selfstat, false, localDuration, this.getId());
        } catch (Exception var12) {
            服务端输出信息.println_err("MapleCharacter.setMorph 发生错误，错误原因：" + var12);
        }

    }
    public void cancelEffect( MapleStatEffect effect,  boolean overwrite,  long startTime) {
        if (this != null && effect != null) {
            this.cancelEffect(effect, overwrite, startTime, effect.getStatups());
        }
    }
    public void cancelEffect(MapleStatEffect effect, boolean overwrite, long startTime, List<Pair<MapleBuffStat, Integer>> statups, boolean forceCancel) {
        if (this.noCancelBuffMap.containsKey(effect.getSourceId()) && !forceCancel) {
            Pair<Long, Integer> timePair = (Pair)this.noCancelBuffMap.get(effect.getSourceId());
            long nowTime = Calendar.getInstance().getTimeInMillis();
            if (nowTime < (Long)timePair.left + (long)(Integer)timePair.right) {
                this.dropMessage(5, "该BUFF不能取消！");
                return;
            }

            this.noCancelBuffMap.remove(effect.getSourceId());
        }

        Object buffstats;
        if (!overwrite) {
            buffstats = this.getBuffStats(effect, startTime);
        } else {
            buffstats = new ArrayList(statups.size());
            Iterator var13 = statups.iterator();

            while(var13.hasNext()) {
                Pair<MapleBuffStat, Integer> statup = (Pair)var13.next();
                ((List)buffstats).add(statup.getLeft());
            }
        }

        if (((List)buffstats).size() > 0) {
            boolean clonez = this.deregisterBuffStats((List)buffstats);
            if (effect.isMagicDoor()) {
                if (!this.getDoors().isEmpty()) {
                    this.removeDoor();
                    this.silentPartyUpdate();
                }
            } else if (effect.isMonsterRiding_()) {
                this.getMount().cancelSchedule();
            } else if (effect.isAranCombo()) {
                this.combo = 0;
            }

            int var11;
            WeakReference chr;
            WeakReference[] var17;
            int var18;
            if (!overwrite) {
                this.cancelPlayerBuffs((List)buffstats);
                if (this.isGM() && effect.isHide() && this.client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null) {
                    this.hidden = false;
                    if (this.isGod()) {
                        this.map.broadcastGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                    } else {
                        this.map.broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                    }

                    Iterator var16 = this.pets.iterator();

                    while(var16.hasNext()) {
                        MaplePet pet = (MaplePet)var16.next();
                        if (pet.getSummoned()) {
                            this.map.broadcastMessage(this, PetPacket.showPet(this, pet, false, false), false);
                        }
                    }

                    var17 = this.clones;
                    var18 = var17.length;

                    for(var11 = 0; var11 < var18; ++var11) {
                        chr = var17[var11];
                        if (chr.get() != null) {
                            this.map.broadcastMessage((MapleCharacter)chr.get(), MaplePacketCreator.spawnPlayerMapobject((MapleCharacter)chr.get()), false);
                        }
                    }
                }
            }

            if (!clonez) {
                var17 = this.clones;
                var18 = var17.length;

                for(var11 = 0; var11 < var18; ++var11) {
                    chr = var17[var11];
                    if (chr.get() != null) {
                        ((MapleCharacter)chr.get()).cancelEffect(effect, overwrite, startTime);
                    }
                }
            }

            if (this.getDebugMessage()) {
                this.dropMessage("取消技能 - " + effect.getName() + "(" + effect.getSourceId() + ")");
            }

        }
    }
    //取消buff
    public void cancelEffect( MapleStatEffect effect,  boolean overwrite,  long startTime,  List<Pair<MapleBuffStat, Integer>> statups) {
        if (this.noCancelBuffMap.containsKey(effect.getSourceId())) {
            Pair<Long, Integer> timePair = (Pair)this.noCancelBuffMap.get(effect.getSourceId());
            long nowTime = Calendar.getInstance().getTimeInMillis();
            if (nowTime < (Long)timePair.left + (long)(Integer)timePair.right) {
                this.dropMessage(5, "该BUFF不能取消！");
                return;
            }

            this.noCancelBuffMap.remove(effect.getSourceId());
        }
        List<MapleBuffStat> buffstats;
        if (!overwrite) {
            buffstats = this.getBuffStats(effect, startTime);
        } else {
            buffstats = new ArrayList<MapleBuffStat>(statups.size());
            for (final Pair<MapleBuffStat, Integer> statup : statups) {
                buffstats.add(statup.getLeft());
            }
        }
        if (buffstats.size() <= 0) {
            return;
        }
        final boolean clonez = this.deregisterBuffStats(buffstats);
        if (effect.isMagicDoor()) {
            if (!this.getDoors().isEmpty()) {
                this.removeDoor();
                this.silentPartyUpdate();
            }
        }
        else if (effect.isMonsterRiding_()) {
            this.getMount().cancelSchedule();
        }
        else if (effect.isAranCombo()) {
            this.combo = 0;
        }
        if (!overwrite) {
            this.cancelPlayerBuffs(buffstats);
            if (this.isGM() && effect.isHide() && this.client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null) {
                this.hidden = false;
                if (this.isGod()) {
                    this.map.broadcastGMMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                }
                else {
                    this.map.broadcastMessage(this, MaplePacketCreator.spawnPlayerMapobject(this), false);
                }
                for (final MaplePet pet : this.pets) {
                    if (pet.getSummoned()) {
                        this.map.broadcastMessage(this, PetPacket.showPet(this, pet, false, false), false);
                    }
                }
                for (final WeakReference<MapleCharacter> chr : this.clones) {
                    if (chr.get() != null) {
                        this.map.broadcastMessage((MapleCharacter)chr.get(), MaplePacketCreator.spawnPlayerMapobject((MapleCharacter)chr.get()), false);
                    }
                }
            }
        }
        if (!clonez) {
            for (final WeakReference<MapleCharacter> chr : this.clones) {
                if (chr.get() != null) {
                    ((MapleCharacter)chr.get()).cancelEffect(effect, overwrite, startTime);
                }
            }
        }
        if (this.getDebugMessage()) {
            this.dropMessage("取消技能 - " + effect.getName() + "(" + effect.getSourceId() + ")");
        }

        if (effect.getSourceId() == 3020032) {
            boolean find = false;
            Iterator var23 = ChannelServer.getAllInstances().iterator();

            while(var23.hasNext()) {
                ChannelServer cs = (ChannelServer)var23.next();
                Iterator var25 = cs.getMapFactory().getAllMapThreadSafe().iterator();

                while(var25.hasNext()) {
                    MapleMap map = (MapleMap)var25.next();
                    if (map != null) {
                        Iterator var14 = map.getAllMonstersThreadsafe().iterator();

                        label112:
                        while(true) {
                            MapleMonster monster;
                            do {
                                do {
                                    if (!var14.hasNext()) {
                                        break label112;
                                    }

                                    monster = (MapleMonster)var14.next();
                                } while(monster == null);
                            } while(monster.getId() != 9900000 && monster.getId() != 9900001 && monster.getId() != 9900002);

                            if (monster.getOwner() == this.getId()) {
                                map.killMonster(monster, true);
                                map.setStoneLevel(0);
                                map.setHaveStone(false);
                                find = true;
                                break;
                            }
                        }
                    }

                    if (find) {
                        break;
                    }
                }

                if (find) {
                    break;
                }
            }
        }
    }
    
    public void cancelBuffStats(final MapleBuffStat... stat) {
        final List<MapleBuffStat> buffStatList = Arrays.asList(stat);
        this.deregisterBuffStats(buffStatList);
        this.cancelPlayerBuffs(buffStatList);
    }
    
    public void cancelEffectFromBuffStat(final MapleBuffStat stat) {
        if (this.effects.get((Object)stat) != null) {
            this.cancelEffect(((MapleBuffStatValueHolder)this.effects.get((Object)stat)).effect, false, -1L);
        }
    }
    private void recoverPotentialBuff(int itemId, int duration, boolean isCanCancel, List<MapleBuffStat> buffstats) {
        List<Pair<MapleBuffStat, Integer>> statups = new ArrayList();
        Iterator var6 = buffstats.iterator();

        while(var6.hasNext()) {
            MapleBuffStat stat = (MapleBuffStat)var6.next();
            if (stat != null) {
                Pair statup;
                if (stat.name().equals("WATK")) {
                    statup = new Pair(stat, this.getStat().pWatk);
                    statups.add(statup);
                } else if (stat.name().equals("MATK")) {
                    statup = new Pair(stat, this.getStat().pMatk);
                    statups.add(statup);
                } else if (stat.name().equals("WDEF")) {
                    statup = new Pair(stat, this.getStat().pWdef);
                    statups.add(statup);
                } else if (stat.name().equals("MDEF")) {
                    statup = new Pair(stat, this.getStat().pMdef);
                    statups.add(statup);
                } else if (stat.name().equals("ACC")) {
                    statup = new Pair(stat, this.getStat().pAcc);
                    statups.add(statup);
                } else if (stat.name().equals("AVOID")) {
                    statup = new Pair(stat, this.getStat().pAvoid);
                    statups.add(statup);
                } else if (stat.name().equals("SPEED")) {
                    statup = new Pair(stat, this.getStat().pSpeed);
                    statups.add(statup);
                } else if (stat.name().equals("JUMP")) {
                    statup = new Pair(stat, this.getStat().pJump);
                    statups.add(statup);
                } else if (stat.name().equals("MAXHP")) {
                    statup = new Pair(stat, this.getStat().pMaxHpPercent);
                    statups.add(statup);
                } else if (stat.name().equals("MAXMP")) {
                    statup = new Pair(stat, this.getStat().pMaxMpPercent);
                    statups.add(statup);
                }
            }
        }

        MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect_s(itemId, false);
        if (effect != null) {
            this.getClient().sendPacket(MaplePacketCreator.giveBuff(-itemId, duration, statups, effect));
        }

    }

    private void cancelPlayerBuffs( List<MapleBuffStat> buffstats) {
        final MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)this.effects.remove((Object)buffstats);
        final boolean write = this.client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null;
        if (buffstats.contains((Object)MapleBuffStat.HOMING_BEACON)) {
            if (write) {
                this.client.sendPacket(MaplePacketCreator.cancelHoming());
            }
        }
        else {
            if (write) {
                this.stats.recalcLocalStats();
            }
            this.client.sendPacket(MaplePacketCreator.cancelBuff(buffstats));
            this.map.broadcastMessage(this, MaplePacketCreator.cancelForeignBuff(this.getId(), buffstats), false);
            if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0) {
                this.recoverPotentialBuff(Potential.buffItemId, Potential.duration, true, buffstats);
            }
        }
    }
    
    public void dispel() {
        if (!this.isHidden()) {
            final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
            for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
                if (mbsvh.effect.isSkill() && mbsvh.schedule != null && !mbsvh.effect.isMorph() && !mbsvh.effect.isEnergyCharge()) {
                    this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                }
            }
        }
    }
    
    public void dispelSkill(final int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (skillid == 0) {
                if (mbsvh.effect.isSkill() && (mbsvh.effect.getSourceId() == 4331003 || mbsvh.effect.getSourceId() == 4331002 || mbsvh.effect.getSourceId() == 4341002 || mbsvh.effect.getSourceId() == 22131001 || mbsvh.effect.getSourceId() == 1321007  || mbsvh.effect.getSourceId() == 1321011 || mbsvh.effect.getSourceId() == 2121005 || mbsvh.effect.getSourceId() == 2221005 || mbsvh.effect.getSourceId() == 2311006 || mbsvh.effect.getSourceId() == 2321003 || mbsvh.effect.getSourceId() == 3111002 || mbsvh.effect.getSourceId() == 3111005 || mbsvh.effect.getSourceId() == 3211002 || mbsvh.effect.getSourceId() == 3211005 || mbsvh.effect.getSourceId() == 4111002)) {
                    this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    break;
                }
                continue;
            }
            else {
                if (mbsvh.effect.isSkill() && mbsvh.effect.getSourceId() == skillid) {
                    this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    break;
                }
                continue;
            }
        }
    }
    
    public void dispelBuff(final int skillid) {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.getSourceId() == skillid) {
                this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }
    
    public void cancelAllBuffs_() {
        this.effects.clear();
    }
    
    public void cancelAllSkillID() {
        this.skillID.clear();
    }
    
    public void cancelAllBuffs() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
        }
    }
    
    public void cancelMorphs() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.effect.getSourceId()) {
                case 5111005:
                case 5121003:
                case 13111005:
                case 15111002: {break;}
                default: {
                    if (!mbsvh.effect.isMorph()) {
                        continue;
                    }
//                    System.out.println("变身被取消:"+mbsvh.effect.getSourceId());
                    this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                    continue;
                }
            }
        }
    }
    public boolean is超人变身() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            switch (mbsvh.effect.getSourceId()) {
                case 5111005:
                case 5121003:
                case 13111005:
                case 15111002: {
                    return true;
                }
            }
        }
        return false;
    }
    public int getMorphState() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMorph()) {
                return mbsvh.effect.getSourceId();
            }
        }
        return -1;
    }
    
    public void silentGiveBuffs(final List<PlayerBuffValueHolder> buffs) {
        if (buffs == null) {
            return;
        }
        for (final PlayerBuffValueHolder mbsvh : buffs) {
            mbsvh.effect.silentApplyBuff(this, mbsvh.startTime, mbsvh.localDuration, mbsvh.statup, mbsvh.cid);
        }
    }
    
    public List<PlayerBuffValueHolder> getAllBuffs() {
        final List<PlayerBuffValueHolder> ret = new ArrayList<PlayerBuffValueHolder>();
        final Map<Pair<Integer, Byte>, Integer> alreadyDone = new HashMap<Pair<Integer, Byte>, Integer>();
        final LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>> allBuffs = new LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>>((Collection<? extends Entry<MapleBuffStat, MapleBuffStatValueHolder>>)this.effects.entrySet());
        for (final Entry<MapleBuffStat, MapleBuffStatValueHolder> mbsvh : allBuffs) {
            final Pair<Integer, Byte> key = new Pair<Integer, Byte>(Integer.valueOf(((MapleBuffStatValueHolder)mbsvh.getValue()).effect.getSourceId()), Byte.valueOf(((MapleBuffStatValueHolder)mbsvh.getValue()).effect.getLevel()));
            if (alreadyDone.containsKey((Object)key)) {
                ((PlayerBuffValueHolder)ret.get((int)Integer.valueOf(alreadyDone.get((Object)key)))).statup.add(new Pair<MapleBuffStat, Integer>(mbsvh.getKey(), Integer.valueOf(((MapleBuffStatValueHolder)mbsvh.getValue()).value)));
            }
            else {
                alreadyDone.put(key, Integer.valueOf(ret.size()));
                final ArrayList<Pair<MapleBuffStat, Integer>> list = new ArrayList<Pair<MapleBuffStat, Integer>>();
                list.add(new Pair<MapleBuffStat, Integer>(mbsvh.getKey(), Integer.valueOf(((MapleBuffStatValueHolder)mbsvh.getValue()).value)));
                ret.add(new PlayerBuffValueHolder(((MapleBuffStatValueHolder)mbsvh.getValue()).startTime, ((MapleBuffStatValueHolder)mbsvh.getValue()).effect, (List<Pair<MapleBuffStat, Integer>>)list, ((MapleBuffStatValueHolder)mbsvh.getValue()).localDuration, ((MapleBuffStatValueHolder)mbsvh.getValue()).cid));
            }
        }
        return ret;
    }
    
    public void cancelMagicDoor() {
        final LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList<MapleBuffStatValueHolder>((Collection<? extends MapleBuffStatValueHolder>)this.effects.values());
        for (final MapleBuffStatValueHolder mbsvh : allBuffs) {
            if (mbsvh.effect.isMagicDoor()) {
                this.cancelEffect(mbsvh.effect, false, mbsvh.startTime);
                break;
            }
        }
    }
    
    public int getSkillLevel2(final int skillid) {
        return this.getSkillLevel2(SkillFactory.getSkill(skillid));
    }
    
    public int getSkillLevel(final int skillid) {
        return this.getSkillLevel(SkillFactory.getSkill(skillid));
    }

    /**
     * 拳手能量获取
     * @param skillid
     * @param targets
     */
    public void handleEnergyCharge(final int skillid, final int targets) {
        final ISkill echskill = SkillFactory.getSkill(skillid);
        final byte skilllevel = this.getSkillLevel(echskill);
        if (skilllevel > 0) {
            final MapleStatEffect echeff = echskill.getEffect((int)skilllevel);
            if (targets > 0) {
                if (this.getBuffedValue(MapleBuffStat.ENERGY_CHARGE) == null) {
                    echeff.applyEnergyBuff(this, true);
                }
                else {
                    Integer energyLevel = this.getBuffedValue(MapleBuffStat.ENERGY_CHARGE);
                    if ((int)energyLevel < 10000) {
                        energyLevel = Integer.valueOf((int)energyLevel + echeff.getX() * targets);
                        this.client.sendPacket(MaplePacketCreator.showOwnBuffEffect(skillid, 2));
                        this.map.broadcastMessage(this, MaplePacketCreator.showBuffeffect(this.id, skillid, 2), false);
                        if ((int)energyLevel >= 10000) {
                            energyLevel = Integer.valueOf(10000);
                        }
                        this.client.sendPacket(MaplePacketCreator.giveEnergyChargeTest((int)energyLevel, echeff.getDuration() / 1000));
                        this.setBuffedValue(MapleBuffStat.ENERGY_CHARGE, (int)energyLevel);
                    }
                    else if ((int)energyLevel == 10000) {
                        echeff.applyEnergyBuff(this, false);
                        this.setBuffedValue(MapleBuffStat.ENERGY_CHARGE, 10001);
                    }
                }
            }
        }
    }
    
    public void handleBattleshipHP(final int damage) {
        if (this.isActiveBuffedValue(5221006)) {
            this.battleshipHP -= damage;
            if (this.battleshipHP <= 0) {
                this.battleshipHP = 0;
                final MapleStatEffect effect = this.getStatForBuff(MapleBuffStat.MONSTER_RIDING);
                this.client.sendPacket(MaplePacketCreator.skillCooldown(5221006, effect.getCooldown()));
                this.addCooldown(5221006, System.currentTimeMillis(), (long)(effect.getCooldown() * 1000L));
                this.dispelSkill(5221006);
            }
        }
    }
    
    public void handleOrbgain() {
        final int orbcount = (int)this.getBuffedValue(MapleBuffStat.COMBO);
        ISkill theCombol = null;
        ISkill advcombo = null;
        switch (this.getJob()) {
            case 1110:
            case 1111:
            case 1112: {
                theCombol = SkillFactory.getSkill(11111001);
                advcombo = SkillFactory.getSkill(11110005);
                break;
            }
            default: {
                theCombol = SkillFactory.getSkill(1111002);
                advcombo = SkillFactory.getSkill(1120003);
                break;
            }
        }
        final int advComboSkillLevel = this.getSkillLevel(advcombo);
        MapleStatEffect ceffect;
        if (advComboSkillLevel > 0) {
            ceffect = advcombo.getEffect(advComboSkillLevel);
        }
        else {
            if (this.getSkillLevel(theCombol) <= 0) {
                return;
            }
            ceffect = theCombol.getEffect((int)this.getSkillLevel(theCombol));
        }
        if (orbcount < ceffect.getX() + 1) {
            int neworbcount = orbcount + 1;
            if (advComboSkillLevel > 0 && ceffect.makeChanceResult() && neworbcount < ceffect.getX() + 1) {
                ++neworbcount;
            }
            final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, Integer.valueOf(neworbcount)));
            this.setBuffedValue(MapleBuffStat.COMBO, neworbcount);
            int duration = ceffect.getDuration();
            duration += (int)((long)this.getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis());
            this.client.sendPacket(MaplePacketCreator.giveBuff(theCombol.getId(), duration, stat, ceffect));
            this.map.broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this.getId(), stat, ceffect), false);
        }
    }
    
    public void handleOrbconsume() {
        ISkill theCombol = null;
        switch (this.getJob()) {
            case 1110:
            case 1111:
            case 1112: {
                theCombol = SkillFactory.getSkill(11111001);
                break;
            }
            default: {
                theCombol = SkillFactory.getSkill(1111002);
                break;
            }
        }
        if (this.getSkillLevel(theCombol) <= 0) {
            return;
        }
        final MapleStatEffect ceffect = this.getStatForBuff(MapleBuffStat.COMBO);
        if (ceffect == null) {
            return;
        }
        final List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<MapleBuffStat, Integer>(MapleBuffStat.COMBO, Integer.valueOf(1)));
        this.setBuffedValue(MapleBuffStat.COMBO, 1);
        int duration = ceffect.getDuration();
        duration += (int)((long)this.getBuffedStarttime(MapleBuffStat.COMBO) - System.currentTimeMillis());
        this.client.sendPacket(MaplePacketCreator.giveBuff(theCombol.getId(), duration, stat, ceffect));
        this.map.broadcastMessage(this, MaplePacketCreator.giveForeignBuff(this.getId(), stat, ceffect), false);
    }
    
    public void silentEnforceMaxHpMp() {
        this.stats.setMp((int)this.stats.getMp());
        this.stats.setHp((int)this.stats.getHp(), true);
    }
    
    public void enforceMaxHpMp() {
        final Map<MapleStat, Integer> statups = new EnumMap<MapleStat, Integer>(MapleStat.class);
        if (this.stats.getMp() > this.stats.getCurrentMaxMp()) {
            this.stats.setMp((int)this.stats.getMp());
            statups.put(MapleStat.MP, Integer.valueOf((int)this.stats.getMp()));
        }
        if (this.stats.getHp() > this.stats.getCurrentMaxHp()) {
            this.stats.setHp((int)this.stats.getHp());
            statups.put(MapleStat.HP, Integer.valueOf((int)this.stats.getHp()));
        }
        if (statups.size() > 0) {
            this.client.sendPacket(MaplePacketCreator.updatePlayerStats(statups, this));
        }
    }
    
    public MapleMap getMap() {
        return this.map;
    }
    
    public MonsterBook getMonsterBook() {
        return this.monsterbook;
    }
    
    public void setMap(final MapleMap newmap) {
        this.map = newmap;
    }
    
    public void setMap(final int PmapId) {
        this.mapid = PmapId;
    }
    
    public int getMapId() {
        if (this.map != null) {
            return this.map.getId();
        }
        return this.mapid;
    }
    public byte getInitialSpawnpoint() {
        return this.initialSpawnPoint;
    }
    
    public int getId() {
        return this.id;
    }
    
    public int getguildid() {
        return this.guildid;
    }
    
    public String getName() {
        return this.name;
    }
    
    public final boolean canHold(final int itemid) {
        return this.getInventory(GameConstants.getInventoryType(itemid)).getNextFreeSlot() > -1;
    }
    
    public final String getBlessOfFairyOrigin() {
        return this.BlessOfFairy_Origin;
    }
    
    public final short getLevel() {
        if (this.level < 1) {
            this.level = 1;
        }
        return this.level;
    }
    
    public final short getFame() {
        return this.fame;
    }
    
    public final int getDojo() {
        return this.dojo;
    }
    
    public final int getDojoRecord() {
        return this.dojoRecord;
    }
    
    public final int getFallCounter() {
        return this.fallcounter;
    }
    
    public final MapleClient getClient() {
        return this.client;
    }
    
    public void setClient(final MapleClient client) {
        this.client = client;
    }
    
    public int getExp() {
        return this.exp;
    }
    
    public short getRemainingAp() {
        return this.remainingAp;
    }
    
    public int getRemainingSp() {
        return this.remainingSp[GameConstants.getSkillBook((int)this.job)];
    }
    
    public int getRemainingSp(final int skillbook) {
        return this.remainingSp[skillbook];
    }
    
    public int[] getRemainingSps() {
        return this.remainingSp;
    }
    
    public int getRemainingSpSize() {
        int ret = 0;
        for (int i = 0; i < this.remainingSp.length; ++i) {
            if (this.remainingSp[i] > 0) {
                ++ret;
            }
        }
        return ret;
    }
    
    public short getHpMpApUsed() {
        return this.hpmpApUsed;
    }
    
    public boolean isHidden() {
        return this.hidden;
    }
    
    public void setHpMpApUsed(final short hpApUsed) {
        this.hpmpApUsed = hpApUsed;
    }
    
    public byte getSkinColor() {
        return this.skinColor;
    }
    
    public void setSkinColor(final byte skinColor) {
        this.skinColor = skinColor;
    }
    
    public short getJob() {
        return this.job;
    }
    
    public byte getGender() {
        return this.gender;
    }
    
    public int getHair() {
        return this.hair;
    }
    
    public int getFace() {
        return this.face;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setExp(final int exp) {
        this.exp = exp;
    }
    
    public void setHair(final int hair) {
        this.hair = hair;
    }
    
    public void setFace(final int face) {
        this.face = face;
    }
    
    public void setFame(final short fame) {
        this.fame = fame;
    }
    
    public void setDojo(final int dojo) {
        this.dojo = dojo;
    }
    
    public void setDojoRecord(final boolean reset) {
        if (reset) {
            this.dojo = 0;
            this.dojoRecord = 0;
        }
        else {
            ++this.dojoRecord;
        }
    }
    
    public void setFallCounter(final int fallcounter) {
        this.fallcounter = fallcounter;
    }
    
    public Point getOldPosition() {
        return this.old;
    }
    
    public void setOldPosition(final Point x) {
        this.old = x;
    }
    
    public void setRemainingAp(final short remainingAp) {
        this.remainingAp = remainingAp;
    }
    
    public void setRemainingSp(final int remainingSp) {
        this.remainingSp[GameConstants.getSkillBook((int)this.job)] = remainingSp;
    }
    
    public void setRemainingSp(final int remainingSp, final int skillbook) {
        this.remainingSp[skillbook] = remainingSp;
    }
    
    public void setGender(final byte gender) {
        this.gender = gender;
    }
    
    public void setInvincible(final boolean invinc) {
        this.invincible = invinc;
    }
    
    public boolean isInvincible() {
        return this.invincible;
    }
    
    public CheatTracker getCheatTracker() {
        return this.anticheat;
    }
    
    public MapleLieDetector getAntiMacro() {
        return this.antiMacro;
    }
    
    public void startLieDetector(final boolean isItem) {
        if ((this.getMapId() < 910000000 || this.getMapId() > 910000022) && this.getMapId() != 800040410 && !GameConstants.isFishingMap(this.getMapId()) && this.getMap().getReturnMapId() != this.getMapId()) {
            if (this.getAntiMacro().isPassed()) {
                this.getAntiMacro().setPassed(false);
            }
            if (!this.getAntiMacro().inProgress()) {
                this.getAntiMacro().startLieDetector(this.getName(), isItem, false);
            }
        }
    }
    
    public BuddyList getBuddylist() {
        return this.buddylist;
    }
    
    public void addFame(final int famechange) {
        this.fame += (short)famechange;
        this.updateFame();
    }
    
    public void changeMap(final int mapid) {
        final MapleMap target = this.client.getChannelServer().getMapFactory().getMap(mapid);
        this.changeMap(target, target.getPortal(0));
    }
    
    public void changeMap(final int map, final int portal) {
        final MapleMap warpMap = this.client.getChannelServer().getMapFactory().getMap(map);
        this.changeMap(warpMap, warpMap.getPortal(portal));
    }
    
    public void changeMapBanish(final int mapid, final String portal, final String msg) {
        this.dropMessage(5, msg);
        final MapleMap target = this.client.getChannelServer().getMapFactory().getMap(mapid);
        this.changeMap(target, target.getPortal(portal));
    }
    
    public void changeMap(final MapleMap to, final Point pos) {
        this.changeMapInternal(to, pos, MaplePacketCreator.getWarpToMap(to, 129, this), null);
    }
    
    public void changeMap(final MapleMap to, final MaplePortal pto) {
        this.changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), null);
    }
    
    public void changeMapPortal(final MapleMap to, final MaplePortal pto) {
        this.changeMapInternal(to, pto.getPosition(), MaplePacketCreator.getWarpToMap(to, pto.getId(), this), pto);
    }


    //切换地图
    private void changeMapInternal(final MapleMap to, final Point pos, final byte[] warpPacket, final MaplePortal pto) {
        if (to == null || this ==null || this.getAntiMacro() ==null) {
            return;
        }
//        if (pto != null && GameConstants.isNotTo(to.getId(), pto.getName())) {
//            if (this.getParty() == null || this.getParty().getMembers().size() == 1) {
//                this.changeMap(211060000);
//                return;
//            }
//            final int cMap = this.getMapId();
//            for (final MaplePartyCharacter chr : this.getParty().getMembers()) {
//                final MapleCharacter curChar = this.getClient().getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
//                if (curChar != null && (curChar.getMapId() == cMap || curChar.getEventInstance() == this.getEventInstance())) {
//                    curChar.changeMap(211060000);
//                }
//            }
//        }
//        else {
            if (this.getAntiMacro().inProgress()) {
                this.dropMessage(5, "被使用測謊儀時無法操作。");
                this.client.sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if (to.getId() != 105100300) {
                this.dispelBuff(2022536);
                this.dispelBuff(2022537);
            }
            if (to.getId() == 180000000 && this.getGMLevel() < 4) {
                this.changeMap(100000000);
                return;
            }
            final int nowmapid = this.map.getId();
            if (this.eventInstance != null) {
                this.eventInstance.changedMap(this, to.getId());
            }
            if (this.getTrade() != null) {
                if (this.getTrade().getPartner() != null) {
                    final MapleTrade local = this.getTrade();
                    final MapleTrade partners = local.getPartner();
                    if (local.isLocked() && partners.isLocked()) {
                        this.client.getSession().write((Object) MaplePacketCreator.enableActions());
                    } else {
                        MapleTrade.cancelTrade(this.getTrade(), this.getClient());
                    }
                } else {
                    MapleTrade.cancelTrade(this.getTrade(), this.client);
                }
            }
            final boolean pyramid = this.pyramidSubway != null;
            if (this.map.getId() == nowmapid) {
                this.client.sendPacket(warpPacket);
                this.map.removePlayer(this);
                if (!this.isClone() && this.client.getChannelServer().getPlayerStorage().getCharacterById(this.getId()) != null) {
                    this.map = to;
                    this.setPosition(pos);
                    to.addPlayer(this);
                    this.stats.relocHeal();
                }
            }
            if (pyramid && this.pyramidSubway != null) {
                this.pyramidSubway.onChangeMap(this, to.getId());
            }
//        }
    }
    
    public void leaveMap() {
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            for (final MapleMonster mons : this.controlled) {
                if (mons != null) {
                    mons.setController(null);
                    mons.setControllerHasAggro(false);
                    mons.setControllerKnowsAboutAggro(false);
                    this.map.updateMonsterController(mons);
                }
            }
            this.controlled.clear();
            this.visibleMapObjects.clear();
        }
        finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
        if (this.chair != 0) {
            this.cancelFishingTask();
            this.chair = 0;
        }
        if (this.getTrade() != null) {
            if (this.getTrade().getPartner() != null) {
                final MapleTrade local = this.getTrade();
                final MapleTrade partners = local.getPartner();
                if (local.isLocked() && partners.isLocked()) {
                    this.client.getSession().write((Object)MaplePacketCreator.enableActions());
                }
                else {
                    MapleTrade.cancelTrade(this.getTrade(), this.getClient());
                }
            }
            else {
                MapleTrade.cancelTrade(this.getTrade(), this.client);
            }
        }
        this.cancelMapTimeLimitTask();
    }
    
    public void changeJob(final int newJob) {
        try {
            boolean normal = true;
            switch (this.getLevel()) {
                case 8:
                case 10:
                case 30:
                case 70:
                case 120: {
                    normal = false;
                    break;
                }
            }
            if (normal) {
                this.dropMessage("由於您的轉職等級非普通轉職等級，技能點異常後果自負。");
            }
            this.job = (short)newJob;
            if (newJob == 200 && this.level > 8 && this.getOneTimeLog("九級補點數") < 1) {
                this.setOneTimeLog("九級補點數");
                final int[] remainingSp = this.remainingSp;
                final int skillBook = GameConstants.getSkillBook((int)this.job);
                remainingSp[skillBook] += 3;
            }
            if (newJob != 0 && newJob != 1000 && newJob != 2000 && newJob != 2001) {
                final int[] remainingSp2 = this.remainingSp;
                final int skillBook2 = GameConstants.getSkillBook(newJob);
                ++remainingSp2[skillBook2];
                if (newJob % 10 >= 2) {
                    final int[] remainingSp3 = this.remainingSp;
                    final int skillBook3 = GameConstants.getSkillBook(newJob);
                    remainingSp3[skillBook3] += 2;
                }
            }
            if (newJob > 0 && !this.isGM()) {
                this.resetStatsByJob(true);
            }
            this.client.sendPacket(MaplePacketCreator.updateSp(this, false));
            this.updateSingleStat(MapleStat.JOB, newJob);
            int maxhp = this.stats.getMaxHp();
            int maxmp = this.stats.getMaxMp();
            switch (this.job) {
                case 100:
                case 1100:
                case 2100: {
                    maxhp += Randomizer.rand(200, 250);
                    break;
                }
                case 200: {
                    maxmp += Randomizer.rand(100, 150);
                    break;
                }
                case 300:
                case 400:
                case 500: {
                    maxhp += Randomizer.rand(100, 150);
                    maxmp += Randomizer.rand(25, 50);
                    break;
                }
                case 110: {
                    maxhp += Randomizer.rand(300, 350);
                    break;
                }
                case 120:
                case 130:
                case 510:
                case 512:
                case 1110:
                case 2110: {
                    maxhp += Randomizer.rand(300, 350);
                    break;
                }
                case 210:
                case 220:
                case 230: {
                    maxmp += Randomizer.rand(400, 450);
                    break;
                }
                case 310:
                case 312:
                case 320:
                case 322:
                case 410:
                case 412:
                case 420:
                case 422:
                case 520:
                case 522:
                case 1310:
                case 1410: {
                    maxhp += Randomizer.rand(300, 350);
                    maxhp += Randomizer.rand(150, 200);
                    break;
                }
                case 800:
                case 900: {
                    maxhp += 30000;
                    maxhp += 30000;
                    break;
                }
            }
            if (maxhp >= 30000) {
                maxhp = 30000;
            }
            if (maxmp >= 30000) {
                maxmp = 30000;
            }
            this.stats.setMaxHp((short)maxhp);
            this.stats.setMaxMp((short)maxmp);
            this.stats.setHp((int)(short)maxhp);
            this.stats.setMp((int)(short)maxmp);
            final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
            statup.put(MapleStat.MAXHP, Integer.valueOf(maxhp));
            statup.put(MapleStat.MAXMP, Integer.valueOf(maxmp));
            statup.put(MapleStat.HP, Integer.valueOf(maxhp));
            statup.put(MapleStat.MP, Integer.valueOf(maxmp));
            this.stats.recalcLocalStats();
            this.client.sendPacket(MaplePacketCreator.updatePlayerStats(statup, this));
            this.map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(this.getId(), 9), false);
            this.silentPartyUpdate();
            this.guildUpdate();
            this.familyUpdate();
            this.baseSkills();
        }
        catch (Exception e) {
            //e.printStackTrace();
            FilePrinter.printError("MapleCharacter.txt", (Throwable)e);
        }
    }
    
    public void baseSkills() {
        if (GameConstants.getJobNumber((int)this.job) >= 3) {
            final List<Integer> baseSkills = SkillFactory.getSkillsByJob((int)this.job);
            if (baseSkills != null) {
                final Iterator<Integer> iterator = baseSkills.iterator();
                while (iterator.hasNext()) {
                    final int i = (int)Integer.valueOf(iterator.next());
                    final ISkill skil = SkillFactory.getSkill(i);
                    if (skil != null && !skil.isInvisible() && skil.isFourthJob() && this.getSkillLevel(skil) <= 0 && this.getMasterLevel(skil) <= 0 && skil.getMasterLevel() > 0) {
                        if (Objects.nonNull(Start.初始化技能等级.get(skil.getId()))){
                            this.changeSkillLevel(skil, (byte)0, (byte)(int)Start.初始化技能等级.get(skil.getId()));
                        }else{
                            this.changeSkillLevel(skil, (byte)0, (byte)skil.getMasterLevel());
                        }
                    }
                }
            }
        }
    }
    
    public void gainAp(final short ap) {
        this.remainingAp += ap;
        this.updateSingleStat(MapleStat.AVAILABLEAP, (int)this.remainingAp);
    }
    
    public void gainSP(final int sp) {
        final int[] remainingSp = this.remainingSp;
        final int skillBook = GameConstants.getSkillBook((int)this.job);
        remainingSp[skillBook] += sp;
        this.client.sendPacket(MaplePacketCreator.updateSp(this, false));
        this.client.sendPacket(UIPacket.getSPMsg((byte)sp, this.job));
    }
    
    public void gainSP(final int sp, final int skillbook) {
        final int[] remainingSp = this.remainingSp;
        remainingSp[skillbook] += sp;
        this.client.sendPacket(MaplePacketCreator.updateSp(this, false));
        this.client.sendPacket(UIPacket.getSPMsg((byte)sp, this.job));
    }
    
    public void resetAPSP() {
        for (int i = 0; i < this.remainingSp.length; ++i) {
            this.remainingSp[i] = 0;
        }
        this.client.sendPacket(MaplePacketCreator.updateSp(this, false));
        this.gainAp((short)(-this.remainingAp));
    }

    public void newOnKeyboard(final int key, final int type, final int action) {
        final ISkill skill = SkillFactory.getSkill(action);
        if (skill != null) {
            this.changeSkillLevel(skill,  skill.getMaxLevel(), skill.getMaxLevel());
            this.changeKeybinding(key, (byte) type, action);
            this.getClient().sendPacket(MaplePacketCreator.getKeymap(this.getKeyLayout()));
        }
    }

    public void changeSkillLevel(final ISkill skill, final byte newLevel, final byte newMasterlevel) {
        if (skill == null) {
            return;
        }
        this.changeSkillLevel(skill, newLevel, newMasterlevel, skill.isTimeLimited() ? (System.currentTimeMillis() + 2592000000L) : -1L);
    }
    
    public void changeSkillLevel(final ISkill skill, final byte newLevel, final byte newMasterlevel, final long expiration) {
        if (skill == null || (!GameConstants.isApplicableSkill(skill.getId()) && !GameConstants.isApplicableSkill_(skill.getId()))) {
            //System.out.println("直接返回了");
            return;
        }
        this.client.sendPacket(MaplePacketCreator.updateSkill(skill.getId(), (int)newLevel, (int)newMasterlevel, expiration));
        if (newLevel == 0 && newMasterlevel == 0) {
            if (!this.skills.containsKey((Object)skill)) {
                //System.out.println("可能没找到技能");
                return;
            }
            this.skills.remove((Object)skill);
            //System.out.println("删除了技能");
        }
        else {
            //System.out.println("新增技能了");
            this.skills.put(skill, new SkillEntry(newLevel, newMasterlevel, expiration));
        }
        if (GameConstants.isRecoveryIncSkill(skill.getId())) {
            //System.out.println("恢复技能");
            this.stats.relocHeal();
        }
        else if (GameConstants.isElementAmpSkill(skill.getId())) {
           // System.out.println("是元素技能");
            this.stats.recalcLocalStats();
        }
    }
    
    public void changeSkillLevel_Skip(final ISkill skill, final byte newLevel, final byte newMasterlevel) {
        if (skill == null) {
            return;
        }
        System.out.println("更改技能级别");
        this.client.sendPacket(MaplePacketCreator.updateSkill(skill.getId(), (int)newLevel, (int)newMasterlevel, -1L));
        if (newLevel == 0 && newMasterlevel == 0) {
            if (this.skills.containsKey((Object)skill)) {
                this.skills.remove((Object)skill);
            }
        }
        else {
            this.skills.put(skill, new SkillEntry(newLevel, newMasterlevel, -1L));
        }
    }
    
    public void playerDead() {
        if (this.getMapId() != 109020001) {
            final MapleStatEffect statss = this.getStatForBuff(MapleBuffStat.SOUL_STONE);
            if (statss != null) {
                this.dropMessage(5, "你已經透過靈魂之石復活了。");
                this.getStat().setHp(this.getStat().getMaxHp() / 100 * statss.getX());
                this.setStance(0);
                this.changeMap(this.getMap(), this.getMap().getPortal(0));
                return;
            }
        }
        if (this.getEventInstance() != null) {
            this.getEventInstance().playerKilled(this);
        }
        this.client.getSession().write((Object)MaplePacketCreator.enableActions());
        this.dispelSkill(0);
        this.cancelEffectFromBuffStat(MapleBuffStat.MORPH);
        this.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
        this.cancelEffectFromBuffStat(MapleBuffStat.SUMMON);
        this.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        this.checkFollow();
        if (this.getMapId() != 109020001) {
            if (this.job != 0 && this.job != 1000 && this.job != 2000 && this.job != 2001 && this.job != 3000) {
                int charms = this.getItemQuantity(5130000, false);
                if (charms > 0) {
                    MapleInventoryManipulator.removeById(this.client, MapleInventoryType.CASH, 5130000, 1, true, false);
                    if (--charms > 255) {
                        charms = 255;
                    }
                    this.client.sendPacket(MTSCSPacket.useCharm((byte)charms, (byte)0));
                }
                else {
                    final int expforlevel = GameConstants.getExpNeededForLevel((int)this.level);
                    float diepercentage;
                    if (this.map.isTown() || FieldLimitType.RegularExpLoss.check(this.map.getFieldLimit())) {
                        diepercentage = 0.01f;
                    }
                    else {
                        float v8;
                        if (this.job / 100 == 3) {
                            v8 = 0.08f;
                        }
                        else {
                            v8 = 0.2f;
                        }
                        diepercentage = (float)((double)(v8 / (float)this.stats.getLuk()) + 0.05);
                    }
                    int v9 = (int)((long)this.exp - (long)((double)expforlevel * (double)diepercentage));
                    if (v9 < 0) {
                        v9 = 0;
                    }
                    this.exp = v9;
                }
            }
            this.updateSingleStat(MapleStat.EXP, this.exp);
            this.client.getSession().write((Object)MaplePacketCreator.enableActions());
            if (!this.stats.checkEquipDurabilitys(this, -100)) {
                this.dropMessage(5, "該装备耐久度已經使用完畢，必須修理才可以繼續使用。");
            }
        }
        if (this.pyramidSubway != null) {
            this.stats.setHp(50);
            this.pyramidSubway.fail(this);
        }
        this.client.getSession().write((Object)MaplePacketCreator.enableActions());
    }
    
    public void updatePartyMemberHP() {
        if (this.party != null) {
            final int channel = this.client.getChannel();
            for (final MaplePartyCharacter partychar : this.party.getMembers()) {
                if (partychar.getMapid() == this.getMapId() && partychar.getChannel() == channel) {
                    final MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                    if (other == null) {
                        continue;
                    }
                    other.getClient().sendPacket(MaplePacketCreator.updatePartyMemberHP(this.getId(), (int)this.stats.getHp(), (int)this.stats.getCurrentMaxHp()));
                }
            }
        }
    }


    public MapleDragon getDragon() {
        return this.dragon;
    }
    public void receivePartyMemberHP() {
        if (this.party == null) {
            return;
        }
        final int channel = this.client.getChannel();
        for (final MaplePartyCharacter partychar : this.party.getMembers()) {
            if (partychar.getMapid() == this.getMapId() && partychar.getChannel() == channel) {
                final MapleCharacter other = this.client.getChannelServer().getPlayerStorage().getCharacterByName(partychar.getName());
                if (other == null) {
                    continue;
                }
                this.client.sendPacket(MaplePacketCreator.updatePartyMemberHP(other.getId(), (int)other.getStat().getHp(), (int)other.getStat().getCurrentMaxHp()));
            }
        }
    }
    
    public void healHP(final int delta) {
        this.healHP(delta, false);
    }
    
    public void healHP(final int delta, final boolean Show) {
        this.addHP(delta);
        if (Show) {
            this.client.sendPacket(MaplePacketCreator.showOwnHpHealed(delta));
            this.getMap().broadcastMessage(this, MaplePacketCreator.showHpHealed(this.getId(), delta), false);
        }
    }
    
    public void healMP(final int delta) {
        this.addMP(delta);
    }
    
    public void addHP(final int delta) {
        if (this.stats.setHp(this.stats.getHp() + delta)) {
            this.updateSingleStat(MapleStat.HP, (int)this.stats.getHp());
        }
    }
    
    public void addMP(final int delta) {
        if (this.stats.setMp(this.stats.getMp() + delta)) {
            this.updateSingleStat(MapleStat.MP, (int)this.stats.getMp());
        }
    }
    
    public void addMPHP(final int hpDiff, final int mpDiff) {
        final Map<MapleStat, Integer> statups = new EnumMap<MapleStat, Integer>(MapleStat.class);
        if (this.stats.setHp(this.stats.getHp() + hpDiff)) {
            statups.put(MapleStat.HP, Integer.valueOf((int)this.stats.getHp()));
        }
        if (this.stats.setMp(this.stats.getMp() + mpDiff)) {
            statups.put(MapleStat.MP, Integer.valueOf((int)this.stats.getMp()));
        }
        if (statups.size() > 0) {
            this.client.sendPacket(MaplePacketCreator.updatePlayerStats(statups, this));
        }
    }
    
    public void updateSingleStat(final MapleStat stat, final int newval) {
        this.updateSingleStat(stat, newval, false);
    }
    
    public void updateSingleStat(final MapleStat stat, final int newval, final boolean itemReaction) {
        if (stat == MapleStat.AVAILABLESP) {
            this.client.sendPacket(MaplePacketCreator.updateSp(this, itemReaction));
            return;
        }
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(stat, Integer.valueOf(newval));
        this.client.sendPacket(MaplePacketCreator.updatePlayerStats(statup, itemReaction, this));
    }
    
    public void gainExp( int total,  boolean show,  boolean inChat,  boolean white) {
        try {
            if(total>=Integer.MAX_VALUE){
                total = Integer.MAX_VALUE;
            }

            if (等级上限 == 0 ){
                等级上限 =  this.取限制等级1(getId());
            }
            if ( this.level >= 等级上限) {
                if(this.exp + total >= Integer.MAX_VALUE) {
               // this.经验入池(this.id,total);
                }
            }
            int prevexp = this.getExp();
            int needed = GameConstants.getExpNeededForLevel(this.level);
            int maxLevel;
            if ((Integer)LtMS.ConfigValuesMap.get("高等级经验限制开关") > 0 && this.getLevel() >= (Integer)LtMS.ConfigValuesMap.get("高等级经验限制判定等级")) {
                maxLevel = total - total / (Integer)LtMS.ConfigValuesMap.get("高等级经验减少倍数");
                this.增加经验储备((long)maxLevel);
                this.client.sendPacket(MaplePacketCreator.GainEXP_Others(maxLevel, false, false));
                total /= (Integer)LtMS.ConfigValuesMap.get("高等级经验减少倍数");
            }

            if (total > 0) {
                this.stats.checkEquipLevels(this, total);
            }

            maxLevel = ServerConfig.maxlevel;
            int kocMaxLevel = ServerConfig.kocmaxlevel;
            if ((Integer)LtMS.ConfigValuesMap.get("突破服务器等级上限开关") > 0) {
                maxLevel += this.getBreakLevel();
                if (maxLevel > 255) {
                    maxLevel = 255;
                }

                kocMaxLevel += this.getBreakLevel();
                if (kocMaxLevel > 255) {
                    kocMaxLevel = 255;
                }
            }

            if (this.是否储备经验) {
                this.增加经验储备((long)total);
            } else if (this.level >= maxLevel || GameConstants.isKOC(this.job) && this.level >= kocMaxLevel) {
                if (this.level <= maxLevel && (!GameConstants.isKOC(this.job) || this.level <= kocMaxLevel)) {
                    if (this.exp + total > needed) {
                        this.setExp(needed);
                        this.增加经验储备((long)total);
                    } else {
                        this.exp += total;
                    }
                } else {
                    this.增加经验储备((long)total);
                }
            } else {
                boolean leveled = false;
                if (this.exp + total >= needed) {
                    this.exp += total;
                    this.levelUp();
                    leveled = true;
                    needed = GameConstants.getExpNeededForLevel(this.level);
                    if (this.exp > needed) {
                        this.setExp(needed);
                    }
                } else {
                    this.exp += total;
                }

                if (total > 0) {
                    this.familyRep(prevexp, needed, leveled);
                }
            }

            if (total != 0) {
                if (this.exp < 0) {
                    if (total > 0) {
                        this.setExp(needed);
                    } else if (total < 0) {
                        this.setExp(0);
                    }
                }

                this.updateSingleStat(MapleStat.EXP, this.getExp());
                if (show) {
                    if(LtMS.ConfigValuesMap.get("经验显示开关") > 0){
                        this.client.sendPacket(MaplePacketCreator.GainEXP_Others(total, inChat, white));
                    }
                }
            }
        } catch (Exception var10) {
            服务端输出信息.println_err(var10);
            FilePrinter.printError("MapleCharacter.txt", var10);
        }
    }
    
    public void familyRep(final int prevexp, final int needed, final boolean leveled) {
        if (this.mfc != null) {
            final int onepercent = needed / 100;
            int percentrep = prevexp / onepercent + this.getExp() / onepercent;
            if (leveled) {
                percentrep = 100 - percentrep + this.level / 2;
            }
            if (percentrep > 0) {
                final int sensen = Family.setRep(this.mfc.getFamilyId(), this.mfc.getSeniorId(), percentrep, (int)this.level);
                if (sensen > 0) {
                    Family.setRep(this.mfc.getFamilyId(), sensen, percentrep / 2, (int)this.level);
                }
            }
        }
    }
    
    public boolean isShowInfo() {
        return this.isAdmin();
    }
    
    public boolean isShowErr() {
        return this.isShowInfo();
    }
    
    public void gainExpMonster(int gain, final boolean show, final boolean white, final byte pty, final int Class_Bonus_EXP, final int Equipment_Bonus_EXP, final int Premium_Bonus_EXP) {
        if (this != null) {
            this.expirationTask(true, false);
            if (this.getExpm() > 1.0) {
                gain = (int)((double)gain * this.getExpm());
            }

            if (this.isVip()) {
                gain = (int)((double)gain * (1.0 + (double)this.getVipExpRate() / 100.0));
            }

            if (this.getStat().equippedRing) {
                if (pty > 1) {
                    if (pty > 5) {
                        gain = (int)((double)gain * 1.3);
                    } else {
                        gain = (int)((double)gain * (1.0 + 0.1 + 0.05 * (double)(pty - 1)));
                    }
                } else {
                    gain = (int)((double)gain * 1.1);
                }
            }

            boolean merch = World.hasMerchant(this.getAccountID());
            if (merch) {
                gain = (int)((double)gain * 1.05);
            }

            int total = gain + Class_Bonus_EXP + Equipment_Bonus_EXP + Premium_Bonus_EXP;
            int partyinc = 0;
            int prevexp = this.getExp();
            if (pty > 1) {
                partyinc = (int)((float)((double)gain / 5.5) * (float)(pty + 1));
                total += partyinc;
            }

            if (gain > 0 && total < gain) {
                total = Integer.MAX_VALUE;
            }

            if (total > 0) {
                this.stats.checkEquipLevels(this, total);
            }

            int maxLevel = ServerConfig.maxlevel;
            int kocMaxLevel = ServerConfig.kocmaxlevel;
            if ((Integer)LtMS.ConfigValuesMap.get("突破服务器等级上限开关") > 0) {
                maxLevel += this.getBreakLevel();
                if (maxLevel > 255) {
                    maxLevel = 255;
                }

                kocMaxLevel += this.getBreakLevel();
                if (kocMaxLevel > 255) {
                    kocMaxLevel = 255;
                }
            }

            int needed = GameConstants.getExpNeededForLevel(this.level);
            if ((Integer)LtMS.ConfigValuesMap.get("高等级经验限制开关") > 0 && this.getLevel() >= (Integer)LtMS.ConfigValuesMap.get("高等级经验限制判定等级")) {
                int expToStore = total - total / (Integer)LtMS.ConfigValuesMap.get("高等级经验减少倍数");
                this.增加经验储备((long)expToStore);
                this.client.sendPacket(MaplePacketCreator.GainEXP_Others(expToStore, false, false));
                total /= (Integer)LtMS.ConfigValuesMap.get("高等级经验减少倍数");
                gain /= (Integer)LtMS.ConfigValuesMap.get("高等级经验减少倍数");
            }

            if (this.是否储备经验) {
                this.增加经验储备((long)total);
            } else if (GameConstants.isKOC(this.job) && this.level >= kocMaxLevel) {
                if (this.level > kocMaxLevel) {
                    this.增加经验储备((long)total);
                } else if (this.exp + total > needed) {
                    this.setExp(needed);
                    this.增加经验储备((long)total);
                } else {
                    this.exp += total;
                }
            } else if (this.level >= maxLevel) {
                if (this.level > maxLevel) {
                    this.增加经验储备((long)total);
                } else if (this.exp + total > needed) {
                    this.setExp(needed);
                    this.增加经验储备((long)total);
                } else {
                    this.exp += total;
                }
            } else {
                boolean leveled = false;
                if (this.exp + total < needed && this.exp < needed) {
                    this.exp += total;
                } else {
                    boolean levelUpTimesLimit = true;
                    long oexp = (long)this.exp;
                    this.exp += total;

                    while(oexp + (long)total > (long)needed) {
                        this.levelUp();
                        leveled = true;
                        needed = GameConstants.getExpNeededForLevel(this.level);
                        if (levelUpTimesLimit) {
                            break;
                        }
                    }

                    if (this.level >= maxLevel) {
                        this.setExp(0);
                    } else {
                        needed = GameConstants.getExpNeededForLevel(this.level);
                        if (this.exp >= needed) {
                            this.setExp(needed);
                        }
                    }
                }

                if (total > 0) {
                    this.familyRep(prevexp, needed, leveled);
                }
            }

            if (gain != 0) {
                if (this.exp < 0) {
                    if (gain > 0) {
                        this.setExp(GameConstants.getExpNeededForLevel(this.level));
                    } else if (gain < 0) {
                        this.setExp(0);
                    }
                }

                this.updateSingleStat(MapleStat.EXP, this.getExp());
                if (show) {
                    if(LtMS.ConfigValuesMap.get("经验显示开关") > 0) {
                        this.client.sendPacket(MaplePacketCreator.GainEXP_Monster(gain, white, partyinc, Class_Bonus_EXP, Equipment_Bonus_EXP, Premium_Bonus_EXP));
                    }
                }
            }

        }
    }
    
    public void reloadC() {
        this.client.getPlayer().getClient().sendPacket(MaplePacketCreator.getCharInfo(this.client.getPlayer()));
        this.client.getPlayer().getMap().removePlayer(this.client.getPlayer());
        this.client.getPlayer().getMap().addPlayer(this.client.getPlayer());
    }
    
    public void forceReAddItem_NoUpdate(final IItem item, final MapleInventoryType type) {
        this.getInventory(type).removeSlot(item.getPosition());
        this.getInventory(type).addFromDB(item);
    }
    
    public void forceReAddItem(final IItem item, final MapleInventoryType type) {
        this.forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(1, item)));
        }
    }
    public void forceReAddItem_NoUpdate1(Item item, MapleInventoryType type) {
        getInventory(type).removeSlot(item.getPosition());
        getInventory(type).addFromDB(item);
    }

    public void forceReAddItem1(Item item, MapleInventoryType type) {
        forceReAddItem_NoUpdate1(item, type);
        type = MapleInventoryType.EQUIP;
        if (type != MapleInventoryType.UNDEFINED) {
            client.sendPacket(MTSCSPacket.addInventorySlot(type, item,false));
        }
    }
    public void forceReAddItem_Flag(final IItem item, final MapleInventoryType type) {
        this.forceReAddItem_NoUpdate(item, type);
        if (type != MapleInventoryType.UNDEFINED) {
            this.client.sendPacket(MaplePacketCreator.modifyInventory(false, new ModifyInventory(0, item)));
        }
    }
    
    public void silentPartyUpdate() {
        if (this.party != null) {
            Party.updateParty(this.party.getId(), PartyOperation.SILENT_UPDATE, new MaplePartyCharacter(this));
        }
    }
    
    public boolean isGM() {
        return this.gmLevel > 0;
    }
    
    public boolean isAdmin() {
        return this.gmLevel >= 5;
    }
    
    public int getGMLevel() {
        return this.gmLevel;
    }
    
    public boolean isPlayer() {
        return this.gmLevel == 0;
    }
    
    public boolean hasGmLevel(final int level) {
        return this.gmLevel >= level;
    }
    
    public void setGmLevelHM(final byte level) {
        this.gmLevel = level;
    }
    
    public final MapleInventory getInventory(final MapleInventoryType type) {
        return this.inventory[type.ordinal()];
    }
    
    public final MapleInventory[] getInventorys() {
        return this.inventory;
    }
    
    public void expirationTask() {
        this.expirationTask(false);
    }
    
    public void expirationTask(final boolean pending) {
        this.expirationTask(false, pending);
    }

    public final void expirationTask(boolean packet, boolean pending) {
        try {
            if (pending) {
                Integer z;
                if (this.pendingExpiration != null) {
                    for (Integer integer : this.pendingExpiration) {
                        this.client.sendPacket(MTSCSPacket.itemExpired(integer));
                    }
                }
                this.pendingExpiration = null;
                if (this.pendingSkills != null) {
                    for (Integer pendingSkill : this.pendingSkills) {
                        this.client.sendPacket(MaplePacketCreator.updateSkill(pendingSkill, 0, 0, -1L));
                        this.client.sendPacket(MaplePacketCreator.serverNotice(5, "[" + SkillFactory.getSkillName(pendingSkill) + "] 技能已经过期，系统自动从技能栏位移除。"));
                    }
                }
                this.pendingSkills = null;
                return;
            }
            List<Integer> ret = new ArrayList<>();
            long currenttime = System.currentTimeMillis();
            List<Pair<MapleInventoryType, IItem>> toberemove = new ArrayList<>();
            List<IItem> tobeunlock = new ArrayList<>();
            MapleInventoryType[] mapleInventoryTypes = MapleInventoryType.values();

            for(int i = 0; i < mapleInventoryTypes.length; ++i) {
                MapleInventoryType inv = mapleInventoryTypes[i];
                for (IItem item : this.getInventory(inv)) {
                    long expiration = item.getExpiration();
                    if (expiration != -1L && !GameConstants.isPet(item.getItemId()) && currenttime > expiration) {
                        if (ItemFlag.LOCK.check(item.getFlag())) {
                            tobeunlock.add(item);
                        } else if (currenttime > expiration) {
                            toberemove.add(new Pair(inv, item));
                        }
                    } else if (item.getItemId() == 5000054 && item.getPet() != null && item.getPet().getSecondsLeft() <= 0) {
                        toberemove.add(new Pair(inv, item));
                    }
                }
            }
            toberemove.forEach(itemz-> {
                IItem item = (IItem)itemz.getRight();
                ret.add(item.getItemId());
                if (packet) {
                    this.getInventory((MapleInventoryType)itemz.getLeft()).removeItem(item.getPosition(), item.getQuantity(), false, this);
                } else {
                    this.getInventory((MapleInventoryType)itemz.getLeft()).removeItem(item.getPosition(), item.getQuantity(), false);
                }
            });
            tobeunlock.forEach(itemz->{
                itemz.setExpiration(-1L);
                itemz.setFlag((byte)(itemz.getFlag() - ItemFlag.LOCK.getValue()));
            });

            this.pendingExpiration = ret;
            List<Integer> skilz = new ArrayList<>();
            List<ISkill> toberem = new ArrayList<>();
            this.skills.forEach((key, value) -> {
                if (((SkillEntry) value).expiration != -1L && currenttime > ((SkillEntry) value).expiration) {
                    toberem.add(key);
                }
            });
            toberem.forEach(skil->{
                skilz.add(skil.getId());
                this.skills.remove(skil);
            });
            this.pendingSkills = skilz;
        } catch (Exception var16) {
            //服务端输出信息.println_err("【错误】expirationTask错误，错误原因：" + var16);
            // var16.printStackTrace();
        }
    }
    
    public MapleShop getShop() {
        return this.shop;
    }
    
    public void setShop(final MapleShop shop) {
        this.shop = shop;
    }
    
    public int getMeso() {
        return this.meso;
    }
    
    public final int[] getSavedLocations() {
        return this.savedLocations;
    }
    
    public int getSavedLocation(final SavedLocationType type) {
        return this.savedLocations[type.getValue()];
    }
    
    public void saveLocation(final SavedLocationType type) {
        this.savedLocations[type.getValue()] = this.getMapId();
    }
    
    public void saveLocation(final SavedLocationType type, final int mapz) {
        this.savedLocations[type.getValue()] = mapz;
    }
    
    public void clearSavedLocation(final SavedLocationType type) {
        this.savedLocations[type.getValue()] = -1;
    }
    
    public void gainMeso(final int gain, final boolean show) {
        this.gainMeso(gain, show, false, false);
    }
    
    public void gainMeso(final int gain, final boolean show, final boolean enableActions) {
        this.gainMeso(gain, show, enableActions, false);
    }
    
    public void gainMeso(final int gain, final boolean show, final boolean enableActions, final boolean inChat) {
        if (this.meso + gain < 0) {
            this.client.sendPacket(MaplePacketCreator.enableActions());
            return;
        }
        this.meso += gain;
        this.updateSingleStat(MapleStat.MESO, this.meso, enableActions);
        if (show) {
            this.client.sendPacket(MaplePacketCreator.showMesoGain(gain, inChat));
        }
    }
    
    public void controlMonster(final MapleMonster monster, final boolean aggro) {
        if (this.clone) {
            return;
        }
        monster.setController(this);
        this.controlled.add(monster);
        this.client.sendPacket(MobPacket.controlMonster(monster, false, aggro));
        monster.sendStatus(this.client);
    }
    
    public void stopControllingMonster(final MapleMonster monster) {
        if (this.clone) {
            return;
        }
        if (monster != null && this.controlled.contains((Object)monster)) {
            this.controlled.remove((Object)monster);
        }
    }
    
    public void checkMonsterAggro(final MapleMonster monster) {
        if (this.clone || monster == null) {
            return;
        }
        if (monster.getController() == this) {
            monster.setControllerHasAggro(true);
        }
        else {
            monster.switchController(this, true);
        }
    }
    
    public Set<MapleMonster> getControlled() {
        return this.controlled;
    }
    
    public int getControlledSize() {
        return this.controlled.size();
    }
    
    public int getAccountID() {
        return this.accountid;
    }
    
    public void mobKilled(final int id, final int skillID) {
        try {
            for (final MapleQuestStatus q : this.quests.values()) {
                if (q.getStatus() == 1) {
                    if (!q.hasMobKills()) {
                        continue;
                    }
                    if (!q.mobKilled(id, skillID)) {
                        continue;
                    }
                    this.client.sendPacket(MaplePacketCreator.updateQuestMobKills(q));
                    if (!q.getQuest().canComplete(this, null)) {
                        continue;
                    }
                    this.client.sendPacket(MaplePacketCreator.getShowQuestCompletion(q.getQuest().getId()));
                }
            }
        }
        catch (Exception ex) {
            //Ex.printStackTrace();
            FileoutputUtil.outError("logs/殺死怪物計次異常.txt", (Throwable)ex);
        }
    }
    
    public final List<MapleQuestStatus> getStartedQuests() {
        final List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (final MapleQuestStatus q : this.quests.values()) {
            if (q.getStatus() == 1 && !q.isCustom()) {
                ret.add(q);
            }
        }
        return ret;
    }
    
    public final List<MapleQuestStatus> getCompletedQuests() {
        final List<MapleQuestStatus> ret = new LinkedList<MapleQuestStatus>();
        for (final MapleQuestStatus q : this.quests.values()) {
            if (q.getStatus() == 2 && !q.isCustom()) {
                ret.add(q);
            }
        }
        return ret;
    }
    
    public Map<ISkill, SkillEntry> getSkills() {
        return Collections.unmodifiableMap((Map<? extends ISkill, ? extends SkillEntry>)this.skills);
    }
    
    public byte getSkillLevel2(final ISkill skill) {
        final SkillEntry ret = (SkillEntry)this.skills.get((Object)skill);
        if (ret == null || ret.skillevel < 0) {
            return -1;
        }
        return (byte)Math.min((int)skill.getMaxLevel(), ret.skillevel + (skill.isBeginnerSkill() ? 0 : this.stats.incAllskill));
    }
    
    public byte getSkillLevel(final ISkill skill) {
        final SkillEntry ret = (SkillEntry)this.skills.get((Object)skill);
        if (ret == null || ret.skillevel <= 0) {
            return 0;
        }
        return (byte)Math.min((int)skill.getMaxLevel(), ret.skillevel + (skill.isBeginnerSkill() ? 0 : this.stats.incAllskill));
    }
    
    public byte getMasterLevel(final int skill) {
        return this.getMasterLevel(SkillFactory.getSkill(skill));
    }
    
    public byte getMasterLevel(final ISkill skill) {
        final SkillEntry ret = (SkillEntry)this.skills.get((Object)skill);
        if (ret == null) {
            return 0;
        }
        return ret.masterlevel;
    }
    
    public void levelUp() {
        if (等级上限 == 0 ){
            等级上限 =  this.取限制等级1(getId());
        }
        int shijiedengji = this.取限制等级();
        if (shijiedengji > 等级上限){
            等级上限 = shijiedengji;
        }
        if ( this.level >= 等级上限) {
            //this.client.sendPacket(MaplePacketCreator.serverNotice(5, "当前等级已经超过世界限制等级，暂时无法升级，请突破世界等级之后才能继续升级！"));
                return;
        }
        if (GameConstants.isKOC((int)this.job)) {
            if (this.level <= 70) {
                this.remainingAp += 6;
            }
            else {
                this.remainingAp += 5;
            }
        }
        else {
            this.remainingAp += 5;
        }
        int maxhp = this.stats.getMaxHp();
        int maxmp = this.stats.getMaxMp();
        if (this.job == 0 || this.job == 1000 || this.job == 2000) {
            maxhp += Randomizer.rand(12, 16);
            maxmp += Randomizer.rand(10, 12);
        }
        else if (this.job >= 100 && this.job <= 132) {
            final ISkill improvingMaxHP = SkillFactory.getSkill(1000001);
            final int slevel = this.getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        }
        else if (this.job >= 200 && this.job <= 232) {
            final ISkill improvingMaxMP = SkillFactory.getSkill(2000001);
            final int slevel = this.getSkillLevel(improvingMaxMP);
            if (slevel > 0) {
                maxmp += improvingMaxMP.getEffect(slevel).getX() * 2;
            }
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        }
        else if ((this.job >= 300 && this.job <= 322) || (this.job >= 400 && this.job <= 422) || (this.job >= 1300 && this.job <= 1311) || (this.job >= 1400 && this.job <= 1411)) {
            maxhp += Randomizer.rand(20, 24);
            maxmp += Randomizer.rand(14, 16);
        }
        else if (this.job >= 500 && this.job <= 522) {
            final ISkill improvingMaxHP = SkillFactory.getSkill(5100000);
            final int slevel = this.getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(22, 26);
            maxmp += Randomizer.rand(18, 22);
        }
        else if (this.job >= 1100 && this.job <= 1111) {
            final ISkill improvingMaxHP = SkillFactory.getSkill(11000000);
            final int slevel = this.getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(24, 28);
            maxmp += Randomizer.rand(4, 6);
        }
        else if (this.job >= 1200 && this.job <= 1212) {
            final ISkill improvingMaxMP = SkillFactory.getSkill(12000000);
            final int slevel = this.getSkillLevel(improvingMaxMP);
            if (slevel > 0) {
                maxmp += improvingMaxMP.getEffect(slevel).getX() * 2;
            }
            maxhp += Randomizer.rand(10, 14);
            maxmp += Randomizer.rand(22, 24);
        }
        else if (this.job >= 1500 && this.job <= 1512) {
            final ISkill improvingMaxHP = SkillFactory.getSkill(15100000);
            final int slevel = this.getSkillLevel(improvingMaxHP);
            if (slevel > 0) {
                maxhp += improvingMaxHP.getEffect(slevel).getX();
            }
            maxhp += Randomizer.rand(22, 26);
            maxmp += Randomizer.rand(18, 22);
        }
        else if (this.job >= 2100 && this.job <= 2112) {
            maxhp += Randomizer.rand(50, 52);
            maxmp += Randomizer.rand(4, 6);
        }
        else {
            maxhp += Randomizer.rand(50, 100);
            maxmp += Randomizer.rand(50, 100);
        }
        maxmp += this.stats.getTotalInt() / 10;
        this.exp -= GameConstants.getExpNeededForLevel((int)this.level);
        if (this.exp < 0) {
            this.exp = 0;
        }
        ++this.level;
        maxhp = (short)Math.min(30000, Math.abs(maxhp));
        maxmp = (short)Math.min(30000, Math.abs(maxmp));
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        statup.put(MapleStat.MAXHP, Integer.valueOf(maxhp));
        statup.put(MapleStat.MAXMP, Integer.valueOf(maxmp));
        statup.put(MapleStat.HP, Integer.valueOf(maxhp));
        statup.put(MapleStat.MP, Integer.valueOf(maxmp));
        statup.put(MapleStat.EXP, Integer.valueOf(this.exp));
        statup.put(MapleStat.LEVEL, Integer.valueOf((int)this.level));
        if (this.isGM() || (this.job != 0 && this.job != 1000 && this.job != 2000 && this.job != 2001 && this.job != 3000) || this.level > 9) {
            final int[] remainingSp = this.remainingSp;
            final int skillBook = GameConstants.getSkillBook((int)this.job);
            remainingSp[skillBook] += 3;
            this.client.sendPacket(MaplePacketCreator.updateSp(this, false));
        }
        else if (this.level <= 10) {
            this.stats.setStr((short)(this.stats.getStr() + this.remainingAp));
            this.remainingAp = 0;
            statup.put(MapleStat.STR, Integer.valueOf((int)this.stats.getStr()));
        }
        statup.put(MapleStat.AVAILABLEAP, Integer.valueOf((int)this.remainingAp));
        this.stats.setMaxHp((short)maxhp);
        this.stats.setMaxMp((short)maxmp);
        this.stats.setHp((int)(short)maxhp+30000);
        this.stats.setMp((int)(short)maxmp+30000);
        this.client.sendPacket(MaplePacketCreator.updatePlayerStats(statup, this));
        this.map.broadcastMessage(this, MaplePacketCreator.showForeignEffect(this.getId(), 0), false);
        this.stats.recalcLocalStats();
        this.silentPartyUpdate();
        this.guildUpdate();
        this.familyUpdate();
        this.DoLevelMsg();
        if (this.level >= 20 && this.level <= 25 && !this.isGM()) {
            this.DoLevelMap();
        }
        if ((this.job == 1000 || (this.job >= 1100 && this.job <= 1111) || (this.job >= 1200 && this.job <= 1212) || (this.job >= 1300 && this.job <= 1312) || (this.job >= 1400 && this.job <= 1412) || (this.job >= 1500 && this.job <= 1512)) && this.level == 120) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[恭賀] 玩家" + this.getName() + " 皇家騎士團到達120級。"));
        }
        if (this.level == 200) {
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[恭賀] 玩家" + this.getName() + " 到達200級。"));
        }
        if (this.level == 100 && this.getStLog() >= 1) {
            final int stjfid = this.getStLogid(this.id);
            if (this.getStjfLog(stjfid) >= 1) {
                this.updateStjfLog(stjfid, this.getStjf(stjfid) + 1);
            }
            else {
                this.setStjfLog(stjfid, 1);
            }
        }
        FileoutputUtil.logToFile("logs/Data/升級日誌.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.getClient().getAccountName() + " 玩家: " + this.getName() + " 升級到" + (int)this.level);
    }
    
    public void DoLevelMsg() {
        final int 升级快讯 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"升级快讯开关"));
        if (升级快讯 > 0 && !this.isGM() && this.level >= 1) {
            final String 最高等级玩家 = NPCConversationManager.获取最高等级玩家名字();
            final StringBuilder sb = new StringBuilder("[升级快讯]: 恭喜玩家【");
            final IItem medal = this.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-26));
            if (this.guildid > 0) {}
            sb.append(this.getName());
            sb.append("】在地图 【 " + this.map.getMapName() + "】 目前等级达到 ").append((int)this.level).append(" 级 ");
            if (medal != null) {
                sb.append("<");
                sb.append(MapleItemInformationProvider.getInstance().getName(medal.getItemId()));
                sb.append(">");
            }
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, sb.toString()));
        }
        if (GameConstants.isAran((int)this.job)) {
            switch (this.level) {
                case 30: {
                    this.client.sendPacket(MaplePacketCreator.startMapEffect("恭喜达到30级请回金银岛二转吧。", 5120000, true));
                    break;
                }
                case 70: {
                    this.client.sendPacket(MaplePacketCreator.startMapEffect("恭喜达到70级请到冰原雪域长老公馆三转吧。", 5120000, true));
                    break;
                }
                case 120: {
                    this.client.sendPacket(MaplePacketCreator.startMapEffect("恭喜达到120级请回神木村祭司森林四转吧。", 5120000, true));
                    break;
                }
            }
        }
        if (GameConstants.isKOC((int)this.job) && this.level == 70) {
            this.client.sendPacket(MaplePacketCreator.startMapEffect("恭喜达到70等请到冰封雪域三转吧。", 5120000, true));
        }
    }
    
    public void DoLevelMap() {
        boolean warp = false;
        int Return_Map = 0;
        switch (this.getMapId()) {
            case 910060000: {
                warp = true;
                Return_Map = 100010000;
                break;
            }
            case 910120000: {
                warp = true;
                Return_Map = 100040000;
                break;
            }
            case 910220000: {
                warp = true;
                Return_Map = 101040000;
                break;
            }
            case 910310000: {
                warp = true;
                Return_Map = 103010000;
                break;
            }
            case 912030000: {
                warp = true;
                Return_Map = 120010000;
                break;
            }
        }
        if (warp) {
            final MapleMap warpMap = this.client.getChannelServer().getMapFactory().getMap(Return_Map);
            if (warpMap != null) {
                this.changeMap(warpMap, warpMap.getPortal(0));
                this.dropMessage("由於你的等級超過20，已經不符合新手需求，將把您傳出訓練場。");
            }
        }
    }
    
    public void changeKeybinding(final int key, final byte type, final int action) {
        if (type != 0) {
            this.keylayout.Layout().put(Integer.valueOf(key), new Pair<Byte, Integer>(Byte.valueOf(type), Integer.valueOf(action)));
        }
        else {
            this.keylayout.Layout().remove((Object)Integer.valueOf(key));
        }
    }
    
    public void sendMacros() {
        for (int i = 0; i < 5; ++i) {
            if (this.skillMacros[i] != null) {
                this.client.sendPacket(MaplePacketCreator.getMacros(this.skillMacros));
                break;
            }
        }
    }
    
    public void updateMacros(final int position, final SkillMacro updateMacro) {
        this.skillMacros[position] = updateMacro;
    }
    
    public final SkillMacro[] getMacros() {
        return this.skillMacros;
    }
    
    public void tempban(final String reason, final Calendar duration, final int greason, final boolean bandIp) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            FileoutputUtil.logToFile("logs/Hack/Ban/MySql_input.txt", "\r\n[tempBan] " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " MAC: " + (Object)this.getClient().getMacs() + " 理由: " + reason, false, false);
            PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans (ip) VALUES (?)");
            ps.setString(1, this.client.getSession().remoteAddress().toString().split(":")[0]);
            ps.executeUpdate();
            ps.close();
            ps = con.prepareStatement("UPDATE accounts SET tempban = ?, banreason = ?, greason = ? WHERE id = ?");
            final Timestamp TS = new Timestamp(duration.getTimeInMillis());
            ps.setTimestamp(1, TS);
            ps.setString(2, reason);
            ps.setInt(3, greason);
            ps.setInt(4, this.accountid);
            ps.execute();
            ps.close();
            this.client.disconnect(true, false);
        }
        catch (SQLException ex) {
            System.err.println("Error while tempbanning" + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
    }
    
    public static boolean ban(final String ip, final String id, final String reason, final boolean accountId, final int gmlevel, final boolean hellban) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            if (!isVpn(ip) && id.matches("/[0-9]{1,3}\\..*")) {
                FileoutputUtil.logToFile("logs/Hack/Ban/MySql_input.txt", "\r\n[Ban-1] " + FileoutputUtil.NowTime() + " IP: " + ip + " 理由: " + reason, false, false);
                final PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans (ip) VALUES (?)");
                ps.setString(1, id);
                ps.executeUpdate();
                ps.close();
                return true;
            }
            PreparedStatement ps;
            if (accountId) {
                ps = con.prepareStatement("SELECT id FROM accounts WHERE name = ?");
            }
            else {
                ps = con.prepareStatement("SELECT accountid FROM characters WHERE name = ?");
            }
            boolean ret = false;
            ps.setString(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    final int z = rs.getInt(1);
                    try (final PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ? AND gm < ?")) {
                        psb.setString(1, reason);
                        psb.setInt(2, z);
                        psb.setInt(3, gmlevel);
                        psb.execute();
                    }
                    if (gmlevel > 100) {
                        try (final PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
                            psa.setInt(1, z);
                            try (final ResultSet rsa = psa.executeQuery()) {
                                if (rsa.next()) {
                                    final String sessionIP = rsa.getString("sessionIP");
                                    if (sessionIP != null && sessionIP.matches("/[0-9]{1,3}\\..*")) {
                                        FileoutputUtil.logToFile("logs/Hack/Ban/MySql_input.txt", "\r\n[Ban-2] " + FileoutputUtil.NowTime() + " IP: " + ip + " 理由: " + reason, false, false);
                                    }
                                    final String macData = rsa.getString("macs");
                                    if (macData != null) {
                                        MapleClient.banMacs(macData);
                                    }
                                    if (hellban) {
                                        try (final PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE email = ?" + ((sessionIP == null) ? "" : " OR SessionIP = ?"))) {
                                            pss.setString(1, reason);
                                            pss.setString(2, rsa.getString("email"));
                                            if (sessionIP != null) {
                                                pss.setString(3, sessionIP);
                                            }
                                            pss.execute();
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ret = true;
                }
            }
            ps.close();
            return ret;
        }
        catch (SQLException ex) {
            System.err.println("Error while banning" + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            return false;
        }
    }
    
    public final boolean ban(final String reason, final boolean banIP, final boolean autoban, final boolean hellban) {
        if (this.lastmonthfameids == null) {
            throw new RuntimeException("Trying to ban a non-loaded character (testhack)");
        }
        final String ip = this.client.getSessionIPAddress();
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE id = ?");
            ps.setInt(1, autoban ? 2 : 1);
            ps.setString(2, reason);
            ps.setInt(3, this.accountid);
            ps.execute();
            ps.close();
            if (!isVpn(ip)) {
                FileoutputUtil.logToFile("logs/Hack/Ban/MySql_input.txt", "\r\n" + FileoutputUtil.NowTime() + " IP: " + ip + " MAC: " + (Object)this.getClient().getMacs() + " 理由: " + reason, false, false);
                ps = con.prepareStatement("INSERT INTO ipbans (ip) VALUES (?)");
                ps.setString(1, ip);
                ps.executeUpdate();
                ps.close();
                try {
                    for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                        for (MapleCharacter chr : cs.getPlayerStorage().getAllCharactersThreadSafe()) {
                            if (chr.getClient().getSessionIPAddress().equals((Object)this.client.getSessionIPAddress()) && !chr.getClient().isGm()) {
                                chr.getClient().disconnect(true, false);
                                chr.getClient().getSession().close();
                            }
                        }
                    }
                }
                catch (Exception ex3) {}
            }
            this.client.banMacs();
            if (hellban) {
                try (final PreparedStatement psa = con.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
                    psa.setInt(1, this.accountid);
                    try (final ResultSet rsa = psa.executeQuery()) {
                        if (rsa.next()) {
                            try (final PreparedStatement pss = con.prepareStatement("UPDATE accounts SET banned = ?, banreason = ? WHERE email = ? OR SessionIP = ?")) {
                                pss.setInt(1, autoban ? 2 : 1);
                                pss.setString(2, reason);
                                pss.setString(3, rsa.getString("email"));
                                pss.setString(4, ip);
                                pss.execute();
                            }
                        }
                    }
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("Error while banning" + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            return false;
        }
        try {
            this.client.disconnect(true, false);
        }
        catch (Exception ex2) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
        return true;
    }
    
    public boolean OfflineBanByName(final String name, final String reason) {
        int id = 0;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            final Statement stmt = con.createStatement();
            ps = con.prepareStatement("select id from characters where name = ?");
            ps.setString(1, name);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                id = rs.getInt("id");
            }
        }
        catch (Exception ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return id != 0 && this.OfflineBanById(id, reason);
    }
    
    public boolean OfflineBanById(final int id, final String reason) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final Statement stmt = con.createStatement();
            final int z = id;
            int acid = 0;
            String ip = "";
            String mac = "";
            ResultSet rs = stmt.executeQuery("select accountid from characters where id = " + id);
            while (rs.next()) {
                acid = rs.getInt("accountid");
            }
            if (acid == 0) {
                return false;
            }
            try (final PreparedStatement psb = con.prepareStatement("UPDATE accounts SET banned = 1, banreason = ? WHERE id = ?")) {
                psb.setString(1, reason);
                psb.setInt(2, acid);
                psb.execute();
                psb.close();
            }
            rs = stmt.executeQuery("select SessionIP, macs from accounts where id = " + acid);
            while (rs.next()) {
                ip = rs.getString("SessionIP");
                mac = rs.getString("macs");
            }
            if (!isVpn(ip)) {
                FileoutputUtil.logToFile("logs/Hack/Ban/MySql_input.txt", "\r\n[offlineBan] " + FileoutputUtil.NowTime() + " IP: " + ip + " MAC: " + (Object)this.getClient().getMacs() + " 理由: " + reason, false, false);
                final PreparedStatement ps = con.prepareStatement("INSERT INTO ipbans (ip) VALUES (?)");
                ps.setString(1, ip);
                ps.executeUpdate();
                ps.close();
                try {
                    for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                        for (MapleCharacter chr : cs.getPlayerStorage().getAllCharactersThreadSafe()) {
                            if (chr.getClient().getSessionIPAddress().equals((Object)ip) && !chr.getClient().isGm()) {
                                chr.getClient().disconnect(true, false);
                                chr.getClient().getSession().close();
                            }
                        }
                    }
                }
                catch (Exception ex2) {}
            }
            final MapleClient client = this.client;
            MapleClient.banMacs(mac);
            rs.close();
            stmt.close();
            return true;
        }
        catch (Exception ex) {
            System.err.println("封鎖出現錯誤 " + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            return false;
        }
    }
    
    @Override
    public int getObjectId() {
        return this.getId();
    }
    
    @Override
    public void setObjectId(final int id) {
        throw new UnsupportedOperationException();
    }
    
    public MapleStorage getStorage() {
        return this.storage;
    }
    
    public void addVisibleMapObject(final MapleMapObject mo) {
        if (this.clone) {
            return;
        }
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            this.visibleMapObjects.add(mo);
        }
        finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
    }
    
    public void removeVisibleMapObject(final MapleMapObject mo) {
        if (this.clone) {
            return;
        }
        this.visibleMapObjectsLock.writeLock().lock();
        try {
            this.visibleMapObjects.remove((Object)mo);
        }
        finally {
            this.visibleMapObjectsLock.writeLock().unlock();
        }
    }
    
    public boolean isMapObjectVisible(final MapleMapObject mo) {
        this.visibleMapObjectsLock.readLock().lock();
        try {
            return !this.clone && this.visibleMapObjects.contains((Object)mo);
        }
        finally {
            this.visibleMapObjectsLock.readLock().unlock();
        }
    }
    
    public Collection<MapleMapObject> getAndWriteLockVisibleMapObjects() {
        this.visibleMapObjectsLock.writeLock().lock();
        return this.visibleMapObjects;
    }
    
    public void unlockWriteVisibleMapObjects() {
        this.visibleMapObjectsLock.writeLock().unlock();
    }
    
    public boolean isAlive() {
        return this.stats.getHp() > 0;
    }
    
    @Override
    public void sendDestroyData(final MapleClient client) {
        client.sendPacket(MaplePacketCreator.removePlayerFromMap(this.getObjectId()));
        for (final WeakReference<MapleCharacter> chr : this.clones) {
            if (chr.get() != null) {
                ((MapleCharacter)chr.get()).sendDestroyData(client);
            }
        }
    }
    
    @Override
    public void sendSpawnData(final MapleClient client) {
        if (client.getPlayer().allowedToTarget(this)) {
            client.sendPacket(MaplePacketCreator.spawnPlayerMapobject(this));
            if (this.getParty() != null && !this.isClone()) {
                this.updatePartyMemberHP();
                this.receivePartyMemberHP();
            }
            for (final MaplePet pet : this.getSummonedPets()) {
                if (this.getId() != client.getPlayer().getId()) {
                    client.sendPacket(PetPacket.showPet(this, pet, false, false));
                }
            }
            for (final WeakReference<MapleCharacter> chr : this.clones) {
                if (chr.get() != null) {
                    ((MapleCharacter)chr.get()).sendSpawnData(client);
                }
            }
            if (this.summons != null) {
                for (final MapleSummon summon : this.summons.values()) {
                    client.sendPacket(MaplePacketCreator.spawnSummon(summon, false));
                }
            }
            if (this.followid > 0) {
                client.sendPacket(MaplePacketCreator.followEffect(this.followinitiator ? this.id : this.followid, this.followinitiator ? this.followid : this.id, null));
            }
        }
    }
    public EquipFieldEnhancement.EquipField getEquipField(int position) {
        return EquipFieldEnhancement.getInstance().getChrEquipField(this.id, position);
    }
    public Map<Integer, EquipFieldEnhancement.EquipField> getEquipFieldMap() {
        return EquipFieldEnhancement.getInstance().getChrEquipFieldMap(this.id);
    }
    public void equipChanged() {
        if (this.map == null) {
            return;
        }
        this.map.broadcastMessage(this, MaplePacketCreator.updateCharLook(this), false);
        this.stats.recalcLocalStats(true);
        if (this.getMessenger() != null) {
            Messenger.updateMessenger(this.getMessenger().getId(), this.getName(), this.client.getChannel());
        }
    }
    
    public final MaplePet getPet(final int index) {
        byte count = 0;
        for (final MaplePet pet : this.pets) {
            if (pet.getSummoned()) {
                if (count == index) {
                    return pet;
                }
                ++count;
            }
        }
        return null;
    }
    
    public void addPet(final MaplePet pet) {
        if (this.pets.contains((Object)pet)) {
            this.pets.remove((Object)pet);
        }
        this.pets.add(pet);
    }
    
    public void removePet(final MaplePet pet) {
        pet.setSummoned(0);
        this.pets.remove((Object)pet);
    }
    
    public final List<MaplePet> getSummonedPets() {
        final List<MaplePet> ret = new ArrayList<MaplePet>();
        for (int i = 0; i < 3; ++i) {
            ret.add(null);
        }
        for (int i = 0; i < 3; ++i) {
            for (final MaplePet pet : this.pets) {
                if (pet != null && pet.getSummoned()) {
                    final int index = pet.getSummonedValue() - 1;
                    if (index == i) {
                        ret.remove(index);
                        ret.add(index, pet);
                        break;
                    }
                    continue;
                }
            }
        }
        final List<Integer> nullArr = new ArrayList<Integer>();
        nullArr.add(null);
        ret.removeAll((Collection<?>)nullArr);
        return ret;
    }
    
    public final MaplePet getSummonedPet(final int index) {
        for (final MaplePet pet : this.getSummonedPets()) {
            if (pet.getSummonedValue() - 1 == index) {
                return pet;
            }
        }
        return null;
    }
    
    public void shiftPetsRight() {
        final List<MaplePet> petsz = this.getSummonedPets();
        if (petsz.size() >= 3 || petsz.size() < 1) {
            return;
        }
        final boolean[] indexBool = { false, false, false };
        for (int i = 0; i < 3; ++i) {
            for (final MaplePet p : petsz) {
                if (p.getSummonedValue() == i + 1) {
                    indexBool[i] = true;
                }
            }
        }
        if (petsz.size() > 1) {
            if (!indexBool[2]) {
                ((MaplePet)petsz.get(0)).setSummoned(2);
                ((MaplePet)petsz.get(1)).setSummoned(3);
            }
            else if (!indexBool[1]) {
                ((MaplePet)petsz.get(0)).setSummoned(2);
            }
        }
        else if (indexBool[0]) {
            ((MaplePet)petsz.get(0)).setSummoned(2);
        }
    }
    
    public final int getPetSlotNext() {
        final List<MaplePet> petsz = this.getSummonedPets();
        int index = 0;
        if (petsz.size() >= 3) {
            this.unequipPet(this.getSummonedPet(0), false);
        }
        else {
            final boolean[] indexBool = { false, false, false };
            for (int i = 0; i < 3; ++i) {
                for (final MaplePet p : petsz) {
                    if (p.getSummonedValue() == i + 1) {
                        indexBool[i] = true;
                        break;
                    }
                }
            }
            for (final boolean b : indexBool) {
                if (!b) {
                    break;
                }
                ++index;
            }
            index = Math.min(index, 2);
            for (final MaplePet p2 : petsz) {
                if (p2.getSummonedValue() == index + 1) {
                    this.unequipPet(p2, false);
                }
            }
        }
        return index;
    }
    
    public final byte getPetIndex(final MaplePet petz) {
        return (byte)Math.max(-1, petz.getSummonedValue() - 1);
    }
    
    public final byte getPetIndex(final int petId) {
        for (final MaplePet pet : this.getSummonedPets()) {
            if (pet.getUniqueId() == petId) {
                return (byte)Math.max(-1, pet.getSummonedValue() - 1);
            }
        }
        return -1;
    }
    
    public final List<MaplePet> getPets() {
        return this.pets;
    }
    
    public void unequipAllPets() {
        for (final MaplePet pet : this.getSummonedPets()) {
            this.unequipPet(pet, false);
        }
    }
    
    public void unequipPet(final MaplePet pet, final boolean hunger) {
        if (pet.getSummoned()) {
            pet.saveToDb();
            final List<MaplePet> summonedPets = this.getSummonedPets();
            if (summonedPets.contains((Object)pet)) {
                summonedPets.remove((Object)pet);
                int i = 1;
                for (final MaplePet p : summonedPets) {
                    if (p == null) {
                        continue;
                    }
                    p.setSummoned(i);
                    ++i;
                }
            }
            if (this.map != null) {
                this.map.broadcastMessage(this, PetPacket.showPet(this, pet, true, hunger), true);
            }
            pet.setSummoned(0);
            this.client.sendPacket(PetPacket.petStatUpdate(this));
            this.client.sendPacket(MaplePacketCreator.enableActions());
        }
    }
    
    public final long getLastFameTime() {
        return this.lastfametime;
    }
    
    public final List<Integer> getFamedCharacters() {
        return this.lastmonthfameids;
    }
    
    public FameStatus canGiveFame(final MapleCharacter from) {
        if (this.lastfametime >= System.currentTimeMillis() - 86400000L) {
            return FameStatus.NOT_TODAY;
        }
        if (from == null || this.lastmonthfameids == null || this.lastmonthfameids.contains((Object)Integer.valueOf(from.getId()))) {
            return FameStatus.NOT_THIS_MONTH;
        }
        return FameStatus.OK;
    }
    
    public void hasGivenFame(final MapleCharacter to) {
        this.lastfametime = System.currentTimeMillis();
        this.lastmonthfameids.add(Integer.valueOf(to.getId()));
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("INSERT INTO famelog (characterid, characterid_to) VALUES (?, ?)")) {
            ps.setInt(1, this.getId());
            ps.setInt(2, to.getId());
            ps.execute();
        }
        catch (SQLException e) {
            System.err.println("ERROR writing famelog for char " + this.getName() + " to " + to.getName() + (Object)e);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
    }
    
    public final MapleKeyLayout getKeyLayout() {
        return this.keylayout;
    }
    
    public MapleParty getParty() {
        return this.party;
    }
    
    public int getPartyId() {
        return (this.party != null) ? this.party.getId() : -1;
    }
    
    public byte getWorld() {
        return this.world;
    }
    
    public void setWorld(final byte world) {
        this.world = world;
    }
    
    public void setParty(final MapleParty party) {
        this.party = party;
    }
    
    public MapleTrade getTrade() {
        return this.trade;
    }
    
    public void setTrade(final MapleTrade trade) {
        this.trade = trade;
    }
    
    public EventInstanceManager getEventInstance() {
        return this.eventInstance;
    }
    
    public void setEventInstance(final EventInstanceManager eventInstance) {
        this.eventInstance = eventInstance;
    }
    
    public void addDoor(final MapleDoor door) {
        this.doors.add(door);
    }
    
    public void clearDoors() {
        this.doors.clear();
    }
    
    public List<MapleDoor> getDoors() {
        return new ArrayList<MapleDoor>((Collection<? extends MapleDoor>)this.doors);
    }
    
    public void setSmega() {
        if (this.smega) {
            this.smega = false;
            this.dropMessage(5, "由於您关闭了顯示廣播，所以您看不見任何的廣播，如果要打開請打@TSmega。");
        }
        else {
            this.smega = true;
            this.dropMessage(5, "目前已經打開顯示廣播，若要再次关闭請打@TSmega。");
        }
    }
    
    public boolean getSmega() {
        return this.smega;
    }
    
    public void setGashponmega() {
        if (this.gashponmega) {
            this.gashponmega = false;
            this.dropMessage(5, "由於您关闭了轉蛋廣播，所以您看不見任何的轉蛋廣播，如果要打開請打@Gashponmega。");
        }
        else {
            this.gashponmega = true;
            this.dropMessage(5, "目前已經打開顯示轉蛋廣播，若要再次关闭請打@Gashponmega。");
        }
    }
    
    public long getLasttime() {
        return this.lasttime;
    }
    
    public void setLasttime(final long lasttime) {
        this.lasttime = lasttime;
    }
    
    public long getCurrenttime() {
        return this.currenttime;
    }
    
    public void setCurrenttime(final long currenttime) {
        this.currenttime = currenttime;
    }
    
    public long get防止复制时间() {
        return this.防止复制时间;
    }
    
    public void set防止复制时间(final long 防止复制时间) {
        this.防止复制时间 = 防止复制时间;
    }
    
    public void startCheck() {
        if (!this.client.hasCheck(this.getAccountID())) {
            System.err.println("[作弊] 检测到玩家 " + this.getName() + " 登录器关闭，系统对其进行断开连接处理。");
            this.dropMessage(5, "检测到登录器关闭，游戏即将断开。");
        }
    }
    
    public boolean getGashponmega() {
        return this.gashponmega;
    }
    
    public Map<Integer, MapleSummon> getSummons() {
        return this.summons;
    }
    
    public int getChair() {
        return this.chair;
    }
    
    public int getItemEffect() {
        return this.itemEffect;
    }
    
    public void setChair(final int chair) {
        this.chair = chair;
        this.stats.relocHeal();
    }
    
    public void setItemEffect(final int itemEffect) {
        this.itemEffect = itemEffect;
    }
    
    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.PLAYER;
    }
    
    public int getFamilyId() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getFamilyId();
    }
    
    public int getSeniorId() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getSeniorId();
    }
    
    public int getJunior1() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getJunior1();
    }
    
    public int getJunior2() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getJunior2();
    }
    
    public int getCurrentRep() {
        return this.currentrep;
    }
    
    public int getTotalRep() {
        return this.totalrep;
    }
    
    public void setCurrentRep(final int _rank) {
        this.currentrep = _rank;
        if (this.mfc != null) {
            this.mfc.setCurrentRep(_rank);
        }
    }
    
    public void setTotalRep(final int _rank) {
        this.totalrep = _rank;
        if (this.mfc != null) {
            this.mfc.setTotalRep(_rank);
        }
    }
    
    public int getGuildId() {
        return this.guildid;
    }
    
    public byte getGuildRank() {
        return this.guildrank;
    }
    
    public void setGuildId(final int _id) {
        this.guildid = _id;
        if (this.guildid > 0) {
            if (this.mgc == null) {
                this.mgc = new MapleGuildCharacter(this);
            }
            else {
                this.mgc.setGuildId(this.guildid);
            }
        }
        else {
            this.mgc = null;
        }
    }
    
    public void setGuildRank(final byte _rank) {
        this.guildrank = _rank;
        if (this.mgc != null) {
            this.mgc.setGuildRank(_rank);
        }
    }
    
    public MapleGuildCharacter getMGC() {
        return this.mgc;
    }
    
    public void setAllianceRank(final byte rank) {
        this.allianceRank = rank;
        if (this.mgc != null) {
            this.mgc.setAllianceRank(rank);
        }
    }
    
    public byte getAllianceRank() {
        return this.allianceRank;
    }
    
    public MapleGuild getGuild() {
        if (this.getGuildId() <= 0) {
            return null;
        }
        return Guild.getGuild(this.getGuildId());
    }
    
    public void guildUpdate() {
        if (this.guildid <= 0) {
            return;
        }
        this.mgc.setLevel(this.level);
        this.mgc.setJobId((int)this.job);
        Guild.memberLevelJobUpdate(this.mgc);
    }
    
    public void saveGuildStatus() {
        MapleGuild.saveCharacterGuildInfo(this.guildid, this.guildrank, this.allianceRank, this.id);
    }
    
    public void familyUpdate() {
        if (this.mfc == null) {
            return;
        }
        Family.memberFamilyUpdate(this.mfc, this);
    }
    
    public void saveFamilyStatus() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE characters SET familyid = ?, seniorid = ?, junior1 = ?, junior2 = ? WHERE id = ?")) {
            if (this.mfc == null) {
                ps.setInt(1, 0);
                ps.setInt(2, 0);
                ps.setInt(3, 0);
                ps.setInt(4, 0);
            }
            else {
                ps.setInt(1, this.mfc.getFamilyId());
                ps.setInt(2, this.mfc.getSeniorId());
                ps.setInt(3, this.mfc.getJunior1());
                ps.setInt(4, this.mfc.getJunior2());
            }
            ps.setInt(5, this.id);
            ps.execute();
        }
        catch (SQLException se) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)se, "saveFamilyStatus");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)se);
        }
    }
    
    public void modifyCSPoints(final int type, final int quantity) {
        this.modifyCSPoints(type, quantity, false);
    }
    
    public void dropMessage(final String message) {
        this.dropMessage(6, message);
    }
    
    public void modifyCSPoints(final int type, final int quantity, final boolean show) {
        switch (type) {
            case 1: {
                final int Acash = this.getAcash();
                if (Acash + quantity < 0) {
                    if (show) {
                        this.dropMessage(-1, "目前点卷已滿，無法获得更多的点卷");
                    }
                    return;
                }
                this.setAcash(Acash + quantity);
                break;
            }
            case 2: {
                if (this.maplepoints + quantity < 0) {
                    if (show) {
                        this.dropMessage(-1, "目前抵用卷已滿，無法获得更多的抵用卷.");
                    }
                    return;
                }
                this.maplepoints += quantity;
                break;
            }
            case 3: {
                final int Points = this.getPoints();
                if (Points + quantity < 0) {
                    if (show) {
                        this.dropMessage(-1, "目前元宝已滿，無法获得更多的元宝");
                    }
                    return;
                }
                this.setPoints(Points + quantity);
                break;
            }
        }
        if (show && quantity != 0) {
            this.dropMessage(-1, "您已经 " + ((quantity > 0) ? "获得 " : "消费 ") + quantity + ((type == 1) ? " 点卷." : ((type == 2) ? " 抵用卷." : "元宝")));
        }
    }
    
    public int getCSPoints(final int type) {
        switch (type) {
            case 1: {
                return this.getAcash();
            }
            case 2: {
                return this.maplepoints;
            }
            case 3: {
                return this.getPoints();
            }
            default: {
                return 0;
            }
        }
    }
    
    public int getOfflineAcash(final MapleCharacter victim) {
        return this.getAcash(victim);
    }
    
    public final boolean hasEquipped(final int itemid) {
        Collection<IItem> list = this.inventory[MapleInventoryType.EQUIPPED.ordinal()].list();
        return this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid) >= 1;
    }

    public final Collection<IItem> getHasEquipped() {
        return this.inventory[MapleInventoryType.EQUIPPED.ordinal()].list();
    }
    
    public final boolean haveItem(final int itemid, final int quantity, final boolean checkEquipped, final boolean greaterOrEquals) {
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        int possesed = this.inventory[type.ordinal()].countById(itemid);
        if (checkEquipped && type == MapleInventoryType.EQUIP) {
            possesed += this.inventory[MapleInventoryType.EQUIPPED.ordinal()].countById(itemid);
        }
        if (greaterOrEquals) {
            return possesed >= quantity;
        }
        return possesed == quantity;
    }
    
    public final boolean haveItem(final int itemid, final int quantity) {
        return this.haveItem(itemid, quantity, true, true);
    }
    
    public final boolean haveItem(final int itemid) {
        return this.haveItem(itemid, 1, true, true);
    }
    
    public final short haveItemPos(final int itemid) {
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        final IItem findById = this.inventory[type.ordinal()].findById(itemid);
        short possesed;
        if (findById != null) {
            possesed = findById.getPosition();
        }
        else {
            possesed = 100;
        }
        return possesed;
    }
    
    public void dropNPC(final String message) {
        this.client.sendPacket(MaplePacketCreator.getNPCTalk(9010000, (byte)0, message, "00 00", (byte)0));
    }
    
    public void dropNPC(final int npc, final String message) {
        this.client.sendPacket(MaplePacketCreator.getNPCTalk(npc, (byte)0, message, "00 00", (byte)0));
    }
    
    public boolean getItemVac() {
        return this.itemVacs;
    }
    
    public void startItemVac() {
        (this.ItemVac = new ItemVac(this)).start();
        this.itemVacs = true;
    }
    public void startMobVac(UserAttraction userAttraction) {
        (this.mobVac = new MobVac(this,userAttraction)).start();
        this.mobVacs = true;
    }
    public void stopMobVac() {
        if (this.mobVacs) {
            this.mobVac.interrupt();
            this.mobVacs = false;
        }
    }

    /**
     * 开启轮回
     */
    public  void gainStartReincarnation( ){
        int mapId = this.getMapId();
        for ( MapleMonster monstermo : this.getMap().getAllMonster()) {
            if (monstermo.getPosition() != null && monstermo.getStats().isBoss()) {
               // this.dropMessage(1, "该地图有BOSS不允许开启轮回.");
                return;
            }
        }
        if (Start.特殊宠物吸物无法使用地图.stream().anyMatch(s-> {return mapId == Integer.parseInt(s);})){
          //  this.dropMessage(1, "该地图不允许轮回.");
        }else {


            boolean b = Start.轮回集合.entrySet().stream().anyMatch(ua -> {
                return ua.getValue().getPinDao() == this.getClient().getChannel() && ua.getValue().getMapId() == this.getMapId();
            });
            if (b) {
               // this.dropMessage(1, "该地图已经有人开启轮回了.");
            } else {
                //获取身边的怪物
                List<MapleMonster> list = new ArrayList<>();
                final MapleMap mapleMap = ChannelServer.getInstance(this.getClient().getChannel()).getMapFactory().getMap(this.getMapId());

                for (final MapleMonster monstermo : mapleMap.getAllMonster()) {
                    if (monstermo.isAlive() && !monstermo.getStats().isBoss()) {
                        list.add(monstermo);
                    }
                }

                Start.轮回怪物.put(this.getId(), list);
                UserLhAttraction userAttraction = new UserLhAttraction(this.getClient().getChannel(), this.getMapId(),this.getPosition());
                userAttraction.setMapMobCount(mapleMap.getAllMonster().size());
                Start.轮回集合.put(this.getId(), userAttraction);
                this.startMobLhVac(userAttraction);
                this.setLastResOld(this.getLastRes());
            }
        }
    }

    public void startMobLhVac(UserLhAttraction userAttraction) {
        (this.mobLhVac = new MobLhVac(this,userAttraction)).start();
        this.mobLhVacs = true;
    }
    public void startMobMapVac(UserAttraction userAttraction,int mobId,String mobName,String adress,String value, long hp) {
        (this.mobMapVac = new MobMapVac(this,userAttraction,mobId,mobName, adress, value,hp)).start();
        this.mobMapVacs = true;
    }
    public void stopItemVac() {
        if (this.itemVacs) {
            try {
                this.ItemVac.interrupt();
            } catch (Exception e) {
            }
            this.itemVacs = false;
        }
    }

    public void stopMobLhVac() {
        if (this.mobLhVacs) {
            this.mobLhVac.interrupt();
            this.mobLhVacs = false;
        }
    }
    public void stopMobMapVac() {
        if (this.mobMapVacs) {
            this.mobMapVac.interrupt();
            this.mobMapVacs = false;
        }
    }
    
    public final int getCombo() {
        return this.combo;
    }
    
    public void setCombo(final int combo) {
        this.combo = combo;
        this.lastCombo = System.currentTimeMillis();
        this.getClient().getSession().writeAndFlush((Object)MaplePacketCreator.updateCombo(combo));
        if (combo % 10 == 0 && combo >= 10 && combo <= 100) {
            if (this.getSkillLevel(21000000) < combo / 10) {
                return;
            }
            if (combo == 9 && this.getQuestStatus(10370) == 0) {
                this.giftMedal(1142134);
                MapleQuest.getInstance(10370).forceComplete(this, 0);
                this.dropMessage(5, "您剛才拿到了連續技高手勳章。");
            }
            if (combo == 4999 && this.getQuestStatus(10371) == 0) {
                this.giftMedal(1142135);
                MapleQuest.getInstance(10371).forceComplete(this, 0);
                this.dropMessage(5, "您剛才拿到了連續技達人勳章。");
            }
            if (combo == 14999 && this.getQuestStatus(10372) == 0) {
                this.giftMedal(1142136);
                MapleQuest.getInstance(10372).forceComplete(this, 0);
                this.dropMessage(5, "您剛才拿到了連續技之王勳章。");
            }
            SkillFactory.getSkill(21000000).getEffect(combo / 10).applyComboBuff(this, combo);
        }
        else if (combo < 10) {
            SkillFactory.getSkill(21000000).getEffect(combo / 10).applyComboBuff(this, 0);
        }
    }
    
    public final long getLastCombo() {
        return this.lastCombo;
    }
    
    public void setLastCombo(final long combo) {
        this.lastCombo = combo;
    }
    
    public void dropTopMsg(final String message) {
        this.client.sendPacket(UIPacket.getTopMsg(message));
    }
    
    public void dropTopMsg2(final String message) {
        this.client.sendPacket(UIPacket.getTopMsg1(message));
    }
    
    public byte getBuddyCapacity() {
        return this.buddylist.getCapacity();
    }
    
    public void setBuddyCapacity(final byte capacity) {
        this.buddylist.setCapacity(capacity);
        this.client.sendPacket(MaplePacketCreator.updateBuddyCapacity((int)capacity));
    }
    
    public MapleMessenger getMessenger() {
        return this.messenger;
    }
    
    public void setMessenger(final MapleMessenger messenger) {
        this.messenger = messenger;
    }
    
    public void addCooldown(final int skillId, final long startTime, final long length) {
        this.coolDowns.put(Integer.valueOf(skillId), new MapleCoolDownValueHolder(skillId, startTime, length));
    }
    
    public void removeCooldown(final int skillId) {
        if (this.coolDowns.containsKey((Object)Integer.valueOf(skillId))) {
            this.coolDowns.remove((Object)Integer.valueOf(skillId));
        }
    }
    
    public boolean skillisCooling(final int skillId) {
        return this.coolDowns.containsKey((Object)Integer.valueOf(skillId));
    }
    
    public void giveCoolDowns(final int skillid, final long starttime, final long length) {
        this.addCooldown(skillid, starttime, length);
    }
    
    public void giveCoolDowns(final List<MapleCoolDownValueHolder> cooldowns) {
        if (cooldowns != null && cooldowns.size() > 0) {
            for (final MapleCoolDownValueHolder cooldown : cooldowns) {
                this.coolDowns.put(Integer.valueOf(cooldown.skillId), cooldown);
                int scd = (int)((cooldown.length - ((System.currentTimeMillis() - cooldown.startTime > cooldown.length) ? 0L : (System.currentTimeMillis() - cooldown.startTime))) / 1000L);
                this.client.sendPacket(MaplePacketCreator.skillCooldown(cooldown.skillId,scd<=0 || scd == Integer.MAX_VALUE ? 0 : scd ));
            }
        }
        else {
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
                 final PreparedStatement ps = con.prepareStatement("SELECT SkillID,StartTime,length FROM skills_cooldowns WHERE charid = ?")) {
                ps.setInt(1, this.getId());
                final ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    if (rs.getLong("length") + rs.getLong("StartTime") - System.currentTimeMillis() <= 0L) {
                        continue;
                    }
                    this.giveCoolDowns(rs.getInt("SkillID"), rs.getLong("StartTime"), rs.getLong("length"));
                }
                rs.close();
                this.deleteWhereCharacterId(con, "DELETE FROM skills_cooldowns WHERE charid = ?");
            }
            catch (SQLException e) {
                FilePrinter.printError("MapleCharcter.txt", (Throwable)e, "Error while retriving cooldown from SQL storage");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            }
        }
    }
    
    public List<MapleCoolDownValueHolder> getCooldowns() {
        try {
            if (Objects.nonNull(this.coolDowns) && this.coolDowns.size() > 0) {
                return new ArrayList<MapleCoolDownValueHolder>((Collection<? extends MapleCoolDownValueHolder>) this.coolDowns.values());
            }
        } catch (Exception e) {
            System.out.println("Error while getting cooldowns");
        }
        return new ArrayList<MapleCoolDownValueHolder>();
    }
    
    public final List<MapleDiseaseValueHolder> getAllDiseases() {
        return new ArrayList<MapleDiseaseValueHolder>((Collection<? extends MapleDiseaseValueHolder>)this.diseases.values());
    }
    
    public final boolean hasDisease(final MapleDisease dis) {
        return this.diseases.keySet().contains((Object)dis);
    }
    
    public void giveDebuff(final MapleDisease disease, final MobSkill skill) {
        this.giveDebuff(disease, skill.getX(), skill.getDuration(), skill.getSkillId(), skill.getSkillLevel());
    }
    
    public void giveDebuff(final MapleDisease disease, final int x, final long duration, final int skillid, final int level) {
        final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(x)));
        if (!this.hasDisease(disease) && this.diseases.size() < 2) {
            if (disease != MapleDisease.SEDUCE && disease != MapleDisease.STUN && this.isActiveBuffedValue(2321005)) {
                return;
            }
            this.client.sendPacket(MaplePacketCreator.giveDebuff(debuff, skillid, level, (int)duration));
            this.map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, debuff, skillid, level), false);
            if (this.F().get((Object)Integer.valueOf(FumoSkill.FM("异常抗性"))) != null) {
                final double r1 = Math.ceil(Math.random() * 500.0);
                final int 免疫 = (int)Integer.valueOf(this.F().get((Object)Integer.valueOf(FumoSkill.FM("异常抗性"))));
                if (r1 <= (double)免疫) {
                    this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration / 100L));
                    this.dropMessage(5, "异常免疫");
                    return;
                }
            }
            if (this.F().get((Object)Integer.valueOf(FumoSkill.FM("异常抗性"))) != null) {
                int 抗性 = (int)Integer.valueOf(this.F().get((Object)Integer.valueOf(FumoSkill.FM("异常抗性"))));
                if (抗性 > 65) {
                    抗性 = 65;
                }
                this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration / 100L * (long)抗性));
            }
            else {
                this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration));
            }
        }
    }
    
    public void getDiseaseBuff(final MapleDisease disease, final MobSkill skill) {
        this.getDiseaseBuff(disease, skill.getX(), skill.getDuration(), skill.getSkillId(), skill.getSkillLevel());
    }
    
    public void getDiseaseBuff(final MapleDisease disease, final int x, final long duration, final int skillid, final int level) {
        final List<Pair<MapleDisease, Integer>> debuff = Collections.singletonList(new Pair<MapleDisease, Integer>(disease, Integer.valueOf(x)));
        if (!this.hasDisease(disease) && this.diseases.size() < 2) {
            if (disease != MapleDisease.SEDUCE && disease != MapleDisease.STUN && this.isActiveBuffedValue(2321005)) {
                return;
            }
            if (this.F().get(FumoSkill.FM("异常抗性")) != null) {
                double r1 = Math.ceil(Math.random() * 500.0);
                int 免疫 = (Integer)this.F().get(FumoSkill.FM("异常抗性"));
                if (r1 <= (double)免疫) {
                    this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration / 100L));
                    this.dropMessage(5, "异常免疫");
                    return;
                }
            }

            if (this.F().get(FumoSkill.FM("异常抗性")) != null) {
                int 抗性 = (Integer)this.F().get(FumoSkill.FM("异常抗性"));
                if (抗性 > 65) {
                    抗性 = 65;
                }

                this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration / 100L * (long)抗性));
            } else {
                this.diseases.put(disease, new MapleDiseaseValueHolder(disease, System.currentTimeMillis(), duration));
            }            this.client.sendPacket(MaplePacketCreator.giveDebuff(debuff, skillid, level, (int)duration));
            this.map.broadcastMessage(this, MaplePacketCreator.giveForeignDebuff(this.id, debuff, skillid, level), false);
        }
    }
    
    public void giveSilentDebuff(final List<MapleDiseaseValueHolder> ld) {
        if (ld != null) {
            for (final MapleDiseaseValueHolder disease : ld) {
                this.diseases.put(disease.disease, disease);
            }
        }
    }
    public final void giveSnailValues(List<SnailCharacterValueHolder> ld) {
        if (ld != null) {
            Iterator var2 = ld.iterator();

            while(var2.hasNext()) {
                SnailCharacterValueHolder value = (SnailCharacterValueHolder)var2.next();
                this.是否储备经验 = value.是否储备经验;
                this.是否开店 = value.是否开店;
                this.是否防滑 = value.是否防滑;
                this.临时防滑鞋子 = new ArrayList(value.临时防滑鞋子);
                MapleInventory inventory = this.getInventory(MapleInventoryType.EQUIPPED);
                Collection<IItem> items = inventory.list();
                Iterator var6 = items.iterator();

                while(var6.hasNext()) {
                    IItem item = (IItem)var6.next();
                    int 位置 = this.包含ID位置(this.临时防滑鞋子, item.getItemId());
                    if (位置 >= 0) {
                        item = ((IItem)this.临时防滑鞋子.get(位置)).copy();
                    }
                }
            }
        }

    }
    public void givePotentialBuff(int itemId, short watk, short matk, short wdef, short mdef, short acc, short avoid, short speed, short jump, short maxHpPercent, short maxMpPercent, int duration, boolean isCanCancel) {
        MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect_s(itemId, false);
        if (effect != null) {
            if (watk > 0) {
                effect.setWatk(watk);
            }

            if (matk > 0) {
                effect.setMatk(matk);
            }

            if (wdef > 0) {
                effect.setWdef(wdef);
            }

            if (mdef > 0) {
                effect.setMdef(mdef);
            }

            if (acc > 0) {
                effect.setAcc(acc);
            }

            if (avoid > 0) {
                effect.setAvoid(avoid);
            }

            if (speed > 0) {
                effect.setSpeed(speed);
            }

            if (jump > 0) {
                effect.setJump(jump);
            }

            if (duration < 0) {
                duration = 0;
            }

            effect.setDuration(duration);
            if (!isCanCancel) {
                long nowTime = Calendar.getInstance().getTimeInMillis();
                this.noCancelBuffMap.put(effect.getSourceId(), new Pair(nowTime, effect.getDuration()));
            }

            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair(MapleBuffStat.MORPH, effect.getMorph(this)));
            ArrayList<Pair<MapleBuffStat, Integer>> Selfstat = new ArrayList();
            Selfstat.add(new Pair(MapleBuffStat.WATK, Integer.valueOf(effect.getWatk())));
            Selfstat.add(new Pair(MapleBuffStat.MATK, Integer.valueOf(effect.getMatk())));
            Selfstat.add(new Pair(MapleBuffStat.WDEF, Integer.valueOf(effect.getWdef())));
            Selfstat.add(new Pair(MapleBuffStat.MDEF, Integer.valueOf(effect.getMdef())));
            Selfstat.add(new Pair(MapleBuffStat.ACC, Integer.valueOf(effect.getAcc())));
            Selfstat.add(new Pair(MapleBuffStat.AVOID, Integer.valueOf(effect.getAvoid())));
            Selfstat.add(new Pair(MapleBuffStat.SPEED, Integer.valueOf(effect.getSpeed())));
            Selfstat.add(new Pair(MapleBuffStat.JUMP, Integer.valueOf(effect.getJump())));
            Selfstat.add(new Pair(MapleBuffStat.MAXHP, Integer.valueOf(maxHpPercent)));
            Selfstat.add(new Pair(MapleBuffStat.MAXMP, Integer.valueOf(maxMpPercent)));
            this.cancelEffect(effect, true, -1L, Selfstat);
            this.getClient().sendPacket(MaplePacketCreator.giveBuff(-itemId, duration, Selfstat, effect));
            this.updateSingleStat(MapleStat.HP, this.getHp());
            this.updateSingleStat(MapleStat.MP, this.getMp());
            long starttime = System.currentTimeMillis();
            MapleStatEffect.CancelEffectAction cancelAction = new MapleStatEffect.CancelEffectAction(this, effect, starttime);

            try {
                ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, starttime + (long)duration - System.currentTimeMillis());
                this.registerEffect(effect, starttime, schedule, Selfstat, false, duration, this.getId());
            } catch (Exception var21) {
                服务端输出信息.println_err("MapleCharacter.givePotentialBuff 发生错误，错误原因：" + var21);
            }

        }
    }
    public void givePotentialBuff(int itemId, int duration, boolean isCanCancel) {
        if (this.getStat().pWatk <= 0 && this.getStat().pMatk <= 0 && this.getStat().pWdef <= 0 && this.getStat().pMdef <= 0 && this.getStat().pAcc <= 0 && this.getStat().pAvoid <= 0 && this.getStat().pSpeed <= 0 && this.getStat().pJump <= 0 && this.getStat().pMaxHpPercent <= 0 && this.getStat().pMaxMpPercent <= 0) {
            this.givePotentialBuff(itemId, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, (short)0, 1000, true);
        } else {
            this.givePotentialBuff(itemId, (short)(Math.min(this.getStat().pWatk, 32766)), (short)(Math.min(this.getStat().pMatk, 32766)), (short)(Math.min(this.getStat().pWdef, 32766)), (short)(Math.min(this.getStat().pMdef, 32766)), (short)(Math.min(this.getStat().pAcc, 32766)), (short)(Math.min(this.getStat().pAvoid, 32766)), (short)(Math.min(this.getStat().pSpeed, 32766)), (short)(Math.min(this.getStat().pJump, 32766)), (short)(Math.min(this.getStat().pMaxHpPercent, 30000)), (short)(Math.min(this.getStat().pMaxMpPercent, 30000)), duration, isCanCancel);
        }

    }

    public void giveBuff(int itemId, short hp, short mp, short watk, short matk, short wdef, short mdef, short acc, short avoid, short speed, short jump, int duration, boolean isCanCancel) {
        MapleStatEffect effect = MapleItemInformationProvider.getInstance().getItemEffect_s(itemId, false);
        if (effect != null) {
            effect.setHp(hp);
            effect.setMp(mp);
            effect.setWatk(watk);
            effect.setMatk(matk);
            effect.setWdef(wdef);
            effect.setMdef(mdef);
            effect.setAcc(acc);
            effect.setAvoid(avoid);
            effect.setSpeed(speed);
            effect.setJump(jump);
            if (duration > 0) {
                effect.setDuration(duration);
            }

            if (!isCanCancel) {
                long nowTime = Calendar.getInstance().getTimeInMillis();
                this.noCancelBuffMap.put(effect.getSourceId(), new Pair<>(nowTime, effect.getDuration()));
            }

            List<Pair<MapleBuffStat, Integer>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.MORPH, effect.getMorph(this)));
            ArrayList<Pair<MapleBuffStat, Integer>> Selfstat = new ArrayList<>();
            Selfstat.add(new Pair<>(MapleBuffStat.WATK, (int) effect.getWatk() + this.getStat().pWatk));
            Selfstat.add(new Pair<>(MapleBuffStat.MATK, (int) effect.getMatk() + this.getStat().pMatk));
            Selfstat.add(new Pair<>(MapleBuffStat.WDEF, (int) effect.getWdef() + this.getStat().pWdef));
            Selfstat.add(new Pair<>(MapleBuffStat.MDEF, (int) effect.getMdef() + this.getStat().pMdef));
            Selfstat.add(new Pair<>(MapleBuffStat.ACC, (int) effect.getAcc() + this.getStat().pAcc));
            Selfstat.add(new Pair<>(MapleBuffStat.AVOID, (int) effect.getAvoid() + this.getStat().pAvoid));
            Selfstat.add(new Pair<>(MapleBuffStat.SPEED, (int) effect.getSpeed() + this.getStat().pSpeed));
            Selfstat.add(new Pair<>(MapleBuffStat.JUMP, (int) effect.getJump() + this.getStat().pJump));
            this.cancelEffect(effect, true, -1L, Selfstat);
            this.getClient().sendPacket(MaplePacketCreator.giveBuff(-itemId, duration, Selfstat, effect));
            this.setHp(this.getHp() + hp);
            this.setMp(this.getMp() + mp);
            this.updateSingleStat(MapleStat.HP, this.getHp());
            this.updateSingleStat(MapleStat.MP, this.getMp());
            long starttime = System.currentTimeMillis();
            MapleStatEffect.CancelEffectAction cancelAction = new MapleStatEffect.CancelEffectAction(this, effect, starttime);

            try {
                ScheduledFuture<?> schedule = BuffTimer.getInstance().schedule(cancelAction, starttime + (long)duration - System.currentTimeMillis());
                this.registerEffect(effect, starttime, schedule, Selfstat, false, duration, this.getId());
            } catch (Exception var21) {
                服务端输出信息.println_err("MapleCharacter.giveBuff 发生错误，错误原因：" + var21);
            }

        }
    }

    public void 刷新防滑状态() {
        MapleInventory inventory = this.getInventory(MapleInventoryType.EQUIPPED);
        Collection<IItem> items = inventory.list();
        Iterator var3 = items.iterator();

        IItem item;
        Iterator var5;
        IItem item1;
        while(var3.hasNext()) {
            item = (IItem)var3.next();
            if (ItemFlag.SPIKES.check(item.getFlag()) && item.getPosition() < 0 && !this.临时防滑鞋子.contains(item)) {
                this.是否防滑 = true;
                var5 = items.iterator();

                do {
                    if (!var5.hasNext()) {
                        return;
                    }

                    item1 = (IItem)var5.next();
                } while(item1.getPosition() != -7);

                byte flag = (byte)ItemFlag.SPIKES.getValue();
                if (!ItemFlag.SPIKES.check(item1.getFlag())) {
                    item1.setFlag(flag);
                    List<ModifyInventory> mods = new ArrayList();
                    mods.add(new ModifyInventory(3, item1));
                    mods.add(new ModifyInventory(0, item1));
                    this.client.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
                    this.临时防滑鞋子.add(item1);
                }

                return;
            }
        }

        this.是否防滑 = false;
        if (!this.临时防滑鞋子.isEmpty()) {
            var3 = this.临时防滑鞋子.iterator();

            while(var3.hasNext()) {
                item = (IItem)var3.next();
                item.setFlag((byte)0);
                inventory = this.getInventory(MapleInventoryType.EQUIPPED);
                items = inventory.list();
                var5 = items.iterator();

                while(var5.hasNext()) {
                    item1 = (IItem)var5.next();
                    if (item1.getPosition() == -7 && item.getItemId() == item1.getItemId()) {
                        item1 = item.copy();
                        List<ModifyInventory> mods = new ArrayList();
                        mods.add(new ModifyInventory(3, item1));
                        mods.add(new ModifyInventory(0, item1));
                        this.client.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
                    }
                }
            }

            this.临时防滑鞋子.clear();
        }

    }
    public void setPower(long power) {
        this.power = power;
    }

    public int 包含ID位置(ArrayList<IItem> items, int itemid) {
        for(int i = 0; i < items.size(); ++i) {
            if (((IItem)items.get(i)).getItemId() == itemid) {
                return i;
            }
        }

        return -1;
    }

    public void 脱装备防滑检测(IItem source) {
        if (source != null) {
            int 位置 = this.包含ID位置(this.临时防滑鞋子, source.getItemId());
            if (位置 >= 0) {
                source.setFlag((byte)0);
                List<ModifyInventory> mods = new ArrayList();
                mods.add(new ModifyInventory(3, source));
                mods.add(new ModifyInventory(0, source));
                this.client.sendPacket(MaplePacketCreator.modifyInventory(true, mods));
                this.临时防滑鞋子.remove(位置);
            }

        }
    }
    public boolean isSaveingToDB() {
        return this.saveingToDB;
    }

    public void setSaveingToDB(boolean saveingToDB) {
        this.saveingToDB = saveingToDB;
    }

    public void 道具存档() {
        if (!this.isClone()) {
            if (!this.isSaveingToDB()) {
                cachedThreadPool.execute(new Runnable() {
                    public void run() {
                        PreparedStatement ps = null;
                        PreparedStatement pse = null;
                        ResultSet rs = null;
                        Connection con = null;

                        try {
                            MapleCharacter.this.setSaveingToDB(true);
                            con = DBConPool.getConnection();
                            con.setTransactionIsolation(1);
                            con.setAutoCommit(false);
                            MapleCharacter.this.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
                            ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
                            ps.setInt(1, MapleCharacter.this.id);
                            ps.setByte(2, MapleCharacter.this.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
                            ps.setByte(3, MapleCharacter.this.getInventory(MapleInventoryType.USE).getSlotLimit());
                            ps.setByte(4, MapleCharacter.this.getInventory(MapleInventoryType.SETUP).getSlotLimit());
                            ps.setByte(5, MapleCharacter.this.getInventory(MapleInventoryType.ETC).getSlotLimit());
                            ps.setByte(6, MapleCharacter.this.getInventory(MapleInventoryType.CASH).getSlotLimit());
                            ps.execute();
                            ps.close();
                            MapleCharacter.this.saveInventory(con);
                            MapleCharacter.this.saveMoneyToDB(con);
                        } catch (DatabaseException | UnsupportedOperationException | SQLException var17) {
                            FileoutputUtil.outputFileError("logs/Except/Log_Packet_Except.txt", var17);

                            try {
                                if (con != null) {
                                    con.rollback();
                                }
                            } catch (SQLException var16) {
                                FileoutputUtil.outputFileError("logs/Except/Log_Packet_Except.txt", var16);
                            }
                        } finally {
                            try {
                                if (ps != null) {
                                    ps.close();
                                }

                                if (pse != null) {
                                    ((PreparedStatement)pse).close();
                                }

                                if (rs != null) {
                                    ((ResultSet)rs).close();
                                }

                                con.setAutoCommit(true);
                                con.setTransactionIsolation(4);
                                MapleCharacter.this.setSaveingToDB(false);
                            } catch (SQLException var15) {
                                MapleCharacter.this.setSaveingToDB(false);
                                FileoutputUtil.outputFileError("logs/Except/Log_Packet_Except.txt", var15);
                            }

                        }

                    }
                });
            }
        }
    }

    public void 道具存档2() {
        if (!this.isClone()) {
            if (!this.isSaveingToDB()) {
                PreparedStatement ps = null;
                PreparedStatement pse = null;
                ResultSet rs = null;
                Connection con = null;

                try {
                    this.setSaveingToDB(true);
                    con = DBConPool.getConnection();
                    con.setTransactionIsolation(1);
                    con.setAutoCommit(false);
                    this.deleteWhereCharacterId(con, "DELETE FROM inventoryslot WHERE characterid = ?");
                    ps = con.prepareStatement("INSERT INTO inventoryslot (characterid, `equip`, `use`, `setup`, `etc`, `cash`) VALUES (?, ?, ?, ?, ?, ?)");
                    ps.setInt(1, this.id);
                    ps.setByte(2, this.getInventory(MapleInventoryType.EQUIP).getSlotLimit());
                    ps.setByte(3, this.getInventory(MapleInventoryType.USE).getSlotLimit());
                    ps.setByte(4, this.getInventory(MapleInventoryType.SETUP).getSlotLimit());
                    ps.setByte(5, this.getInventory(MapleInventoryType.ETC).getSlotLimit());
                    ps.setByte(6, this.getInventory(MapleInventoryType.CASH).getSlotLimit());
                    ps.execute();
                    ps.close();
                    this.saveInventory(con);
                    this.saveMoneyToDB(con);
                } catch (DatabaseException | UnsupportedOperationException | SQLException var17) {
                    FileoutputUtil.outputFileError("logs/Except/Log_Packet_Except.txt", var17);

                    try {
                        if (con != null) {
                            con.rollback();
                        }
                    } catch (SQLException var16) {
                        FileoutputUtil.outputFileError("logs/Except/Log_Packet_Except.txt", var16);
                    }
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }

                        if (pse != null) {
                            ((PreparedStatement)pse).close();
                        }

                        if (rs != null) {
                            ((ResultSet)rs).close();
                        }

                        con.setAutoCommit(true);
                        con.setTransactionIsolation(4);
                        this.setSaveingToDB(false);
                    } catch (SQLException var15) {
                        this.setSaveingToDB(false);
                        FileoutputUtil.outputFileError("logs/Except/Log_Packet_Except.txt", var15);
                    }

                }

            }
        }
    }

    public void saveMoneyToDB(Connection con) {
        try {
            boolean newCon = false;
            if (con == null || con.isClosed()) {
                newCon = true;
                con = DBConPool.getConnection();
            }

            PreparedStatement ps = con.prepareStatement("UPDATE characters SET meso = " + this.getMeso() + " WHERE id = " + this.getId());
            ps.executeUpdate();
            ps.close();
            if (newCon) {
                con.close();
            }
        } catch (Exception var4) {
            服务端输出信息.println_err("【错误】saveMoneyToDB错误，原因：" + var4);
            var4.printStackTrace();
        }

    }
    public void dispelDebuff(final MapleDisease debuff) {
        if (this.hasDisease(debuff)) {
            final long mask = debuff.getValue();
            final boolean first = debuff.isFirst();
            this.diseases.remove((Object)debuff);
            this.client.sendPacket(MaplePacketCreator.cancelDebuff(mask, first));
            this.map.broadcastMessage(this, MaplePacketCreator.cancelForeignDebuff(this.id, mask, first), false);
        }
    }

    //消除异常buff
    public void dispelDebuffs() {
        this.dispelDebuff(MapleDisease.CURSE);
        this.dispelDebuff(MapleDisease.DARKNESS);
        this.dispelDebuff(MapleDisease.POISON);
        this.dispelDebuff(MapleDisease.SEAL);
        this.dispelDebuff(MapleDisease.STUN);
        this.dispelDebuff(MapleDisease.WEAKEN);
    }
    
    public void cancelAllDebuffs() {
        this.diseases.clear();
    }
    
    public void setLevel(final short level) {
        this.level = level;
    }
    
    public void sendNote(final String to, final String msg) {
        this.sendNote(to, msg, 0);
    }
    
    public void sendNote(final String to, final String msg, final int fame) {
        MapleCharacterUtil.sendNote(to, this.getName(), msg, fame);
    }
    
    public void showNote() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * FROM notes WHERE `to`=?", 1005, 1008)) {
            ps.setString(1, this.getName());
            try (final ResultSet rs = ps.executeQuery()) {
                rs.last();
                final int count = rs.getRow();
                rs.first();
                this.client.sendPacket(MTSCSPacket.showNotes(rs, count));
            }
        }
        catch (SQLException e) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)e, "Unable to show note");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
    }
    
    public void deleteNote(final int id, final int fame) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT gift FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            final ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt("gift") == fame && fame > 0) {
                this.addFame(fame);
                this.updateSingleStat(MapleStat.FAME, (int)this.getFame());
                this.client.sendPacket(MaplePacketCreator.getShowFameGain(fame));
            }
            rs.close();
            ps.close();
            ps = con.prepareStatement("DELETE FROM notes WHERE `id`=?");
            ps.setInt(1, id);
            ps.execute();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("Unable to delete note" + (Object)e);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
    }
    
    public int getmulungEnergy() {
        return this.mulung_energy;
    }
    
    public void mulungEnergyModify(final boolean inc) {
        if (inc) {
            if (this.mulung_energy + 100 > 10000) {
                this.mulung_energy = 10000;
            }
            else {
                this.mulung_energy += 100;
            }
        }
        else {
            this.mulung_energy = 0;
        }
        if (this.isAdmin()) {
            this.mulung_energy = 10000;
        }
        this.client.sendPacket(MaplePacketCreator.MulungEnergy((int)this.mulung_energy));
    }
    
    public void writeMulungEnergy() {
        this.client.sendPacket(MaplePacketCreator.MulungEnergy((int)this.mulung_energy));
    }
    
    public void writeEnergy(final String type, final String inc) {
        this.client.sendPacket(MaplePacketCreator.sendPyramidEnergy(type, inc));
    }
    
    public void writeStatus(final String type, final String inc) {
        this.client.sendPacket(MaplePacketCreator.sendGhostStatus(type, inc));
    }
    
    public void writePoint(final String type, final String inc) {
        this.client.sendPacket(MaplePacketCreator.sendGhostPoint(type, inc));
    }
    
    public final long getKeyDownSkill_Time() {
        return this.keydown_skill;
    }
    
    public void setKeyDownSkill_Time(final long keydown_skill) {
        this.keydown_skill = keydown_skill;
    }
    
    public void checkBerserk() {
        if (this.BerserkSchedule != null) {
            this.BerserkSchedule.cancel(false);
            this.BerserkSchedule = null;
        }
        final ISkill BerserkX = SkillFactory.getSkill(1320006);
        final int skilllevel = this.getSkillLevel(BerserkX);
        if (skilllevel >= 1) {
            final MapleStatEffect ampStat = BerserkX.getEffect(skilllevel);
            this.stats.Berserk = (this.stats.getHp() * 100 / this.stats.getMaxHp() <= ampStat.getX());
            try {
                this.BerserkSchedule = BuffTimer.getInstance().schedule((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        MapleCharacter.this.checkBerserk();
                    }
                }, 10000L);
            }
            catch (RejectedExecutionException ex) {}
        }
    }
    
    public void prepareBeholderEffect(final int skillId) {
        if (this.beholderHealingSchedule != null) {
            this.beholderHealingSchedule.cancel(false);
        }
        if (this.beholderBuffSchedule != null) {
            this.beholderBuffSchedule.cancel(false);
        }
        final ISkill bHealing = SkillFactory.getSkill(1320008);
        final int bHealingLvl = this.getSkillLevel(bHealing);
        final int berserkLvl = this.getSkillLevel(SkillFactory.getSkill(1320006));
        if (bHealingLvl > 0) {
            final MapleStatEffect healEffect = bHealing.getEffect(bHealingLvl);
            final int healInterval = healEffect.getX() * 1000;
            this.beholderHealingSchedule = BuffTimer.getInstance().register((Runnable)new Runnable() {
                @Override
                public void run() {
                    final int remhppercentage = (int)Math.ceil((double)MapleCharacter.this.getStat().getHp() * 100.0 / (double)MapleCharacter.this.getStat().getMaxHp());
                    if (berserkLvl == 0 || remhppercentage >= berserkLvl + 10) {
                        MapleCharacter.this.addHP((int)healEffect.getHp());
                    }
                }
            }, (long)healInterval, (long)healInterval);
        }
        final ISkill bBuff = SkillFactory.getSkill(1320009);
        final int bBuffLvl = this.getSkillLevel(bBuff);
        if (bBuffLvl > 0) {
            final MapleStatEffect buffEffect = bBuff.getEffect(bBuffLvl);
            final int buffInterval = buffEffect.getX() * 1000;
            this.beholderBuffSchedule = BuffTimer.getInstance().register((Runnable)new Runnable() {
                @Override
                public void run() {
                    buffEffect.applyTo(MapleCharacter.this);
                    client.sendPacket(MaplePacketCreator.showOwnBuffEffect(skillId, 2));
                    map.broadcastMessage(MaplePacketCreator.summonSkill(MapleCharacter.this.getId(), skillId, Randomizer.nextInt(3) + 6));
                    map.broadcastMessage(MapleCharacter.this, MaplePacketCreator.showBuffeffect(MapleCharacter.this.getId(), skillId, 2), false);
                }
            }, (long)buffInterval, (long)buffInterval);
        }
    }
    
    public void setChalkboard(final String text) {
        this.chalktext = text;
        this.map.broadcastMessage(MTSCSPacket.useChalkboard(this.getId(), text));
    }
    
    public String getChalkboard() {
        return this.chalktext;
    }
    
    public MapleMount getMount() {
        return this.mount;
    }
    
    public int gmLevel() {
        return this.gmLevel;
    }
    
    public int[] getWishlist() {
        return this.wishlist;
    }
    
    public void clearWishlist() {
        for (int i = 0; i < 10; ++i) {
            this.wishlist[i] = 0;
        }
    }
    
    public int getWishlistSize() {
        int ret = 0;
        for (int i = 0; i < 10; ++i) {
            if (this.wishlist[i] > 0) {
                ++ret;
            }
        }
        return ret;
    }
    
    public void setWishlist(final int[] wl) {
        this.wishlist = wl;
    }
    
    public int[] getRocks() {
        return this.rocks;
    }
    
    public int getRockSize() {
        int ret = 0;
        for (int i = 0; i < 10; ++i) {
            if (this.rocks[i] != 999999999) {
                ++ret;
            }
        }
        return ret;
    }
    
    public void deleteFromRocks(final int map) {
        for (int i = 0; i < 10; ++i) {
            if (this.rocks[i] == map) {
                this.rocks[i] = 999999999;
                break;
            }
        }
    }
    
    public void addRockMap() {
        if (this.getRockSize() >= 10) {
            return;
        }
        this.rocks[this.getRockSize()] = this.getMapId();
    }
    
    public boolean isRockMap(final int id) {
        for (int i = 0; i < 10; ++i) {
            if (this.rocks[i] == id) {
                return true;
            }
        }
        return false;
    }
    
    public int[] getRegRocks() {
        return this.regrocks;
    }
    
    public int getRegRockSize() {
        int ret = 0;
        for (int i = 0; i < 5; ++i) {
            if (this.regrocks[i] != 999999999) {
                ++ret;
            }
        }
        return ret;
    }
    
    public void deleteFromRegRocks(final int map) {
        for (int i = 0; i < 5; ++i) {
            if (this.regrocks[i] == map) {
                this.regrocks[i] = 999999999;
                break;
            }
        }
    }
    
    public void addRegRockMap() {
        if (this.getRegRockSize() >= 5) {
            return;
        }
        this.regrocks[this.getRegRockSize()] = this.getMapId();
    }
    
    public boolean isRegRockMap(final int id) {
        for (int i = 0; i < 5; ++i) {
            if (this.regrocks[i] == id) {
                return true;
            }
        }
        return false;
    }
    
    public List<LifeMovementFragment> getLastRes() {
        return this.lastres;
    }
    
    public void setLastRes(final List<LifeMovementFragment> lastres) {
        this.lastres = lastres;
    }

    public List<LifeMovementFragment> getLastResOld() {
        return this.lastresOld;
    }

    public void setLastResOld(final List<LifeMovementFragment> lastres) {
        this.lastresOld = lastres;
    }

    public void setMonsterBookCover(final int bookCover) {
        this.bookCover = bookCover;
    }
    
    public int getMonsterBookCover() {
        return this.bookCover;
    }
    
    public String getAccountSecondPassword() {
        return this.accountsecondPassword;
    }
    
    public int 获得破功() {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM z_pg WHERE CharID = ? ");
            ps.setInt(1, this.accountid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(3);
            }
            else {
                this.创建破功();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            System.err.println("Error while read bosslog." + (Object)Ex);
            return 0;
        }
    }
    
    public int 创建破功() {
        Connection con = DatabaseConnection.getConnection();
        try {
            final int count = 0;
            final PreparedStatement ps = con.prepareStatement("Insert into z_pg(CharID,PG) values (?,?)");
            ps.setInt(1, this.accountid);
            ps.setInt(2, 0);
            ps.executeUpdate();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            System.err.println("Error while read bosslog." + (Object)Ex);
            return -1;
        }
    }
    public int getTodayOnlineTime() {
        return this.todayOnlineTime;
    }

    public void gainTodayOnlineTime(int s) {
        this.todayOnlineTime += s;
    }

    public void setTodayOnlineTime(int s) {
        this.todayOnlineTime = s;
    }

    public int getOneTimeLoga(String log) {
        return TimeLogCenter.getInstance().getOneTimeLoga(this.getAccountID(), log);
    }

    public int getBossLogDa(String bossid) {
        return TimeLogCenter.getInstance().getBossLoga(this.getAccountID(), bossid);
    }
    public void 添加破功(final int pg) {
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE z_pg SET PG = ? WHERE CharID = ?");
            ps.setInt(1, pg + this.获得破功());
            ps.setInt(2, this.accountid);
            ps.execute();
            ps.close();
        }
        catch (Exception Ex) {
            System.err.println("错误" + (Object)Ex);
        }
    }
    public int getCloneDamgeRate() {
        return this.clonedamgerate;
    }

    public void setCloneDamgeRate(int a) {
        this.clonedamgerate = a;
    }

    public void set套装伤害加成(double set) {
        this.套装伤害加成 = set;
    }

    public double get套装伤害加成() {
        return this.套装伤害加成;
    }
    
    public int getBossLog(final String boss) {
        return this.getBossLog(boss, 0);
    }
    public int getBossLog1(final String boss) {
        return this.getBossLog1(boss, 0);
    }
    public int getBossLog(final String boss, final int type) {
        try {
            int count = 0;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bosslog WHERE characterid = ? AND bossid = ? and type = ?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ps.setInt(3, type);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                rs.close();
                ps.close();
            }
            else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO bosslog (characterid, bossid, count, type) VALUES (?, ?, ?, ?)");
                psu.setInt(1, this.id);
                psu.setString(2, boss);
                psu.setInt(3, 0);
                psu.setInt(4, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    public int getBossLog1(final String boss, final int type) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bosslog2 WHERE characterid = ? AND bossid = ? and type = ?");
            ps.setInt(1, this.id);
            ps.setString(2, boss);
            ps.setInt(3, type);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                rs.close();
                ps.close();
            }
            else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO bosslog2 (characterid, bossid, count, type) VALUES (?, ?, ?, ?)");
                psu.setInt(1, this.id);
                psu.setString(2, boss);
                psu.setInt(3, 0);
                psu.setInt(4, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }finally {
            try {
                con.close();
            } catch (Exception e){}
        }
    }
    public void setBossLog(final String boss) {
        this.setBossLog(boss, 0);
    }
    public void setBossLog1(final String boss) {
        this.setBossLog1(boss, 0);
    }

    public void setBossLog(final String boss, final int type) {
        this.setBossLog(boss, type, 1);
    }
    public void setBossLog1(final String boss, final int type) {
        this.setBossLog1(boss, type, 1);
    }



    public void setBossLog(final String boss, final int type, final int count) {
        final int bossCount = this.getBossLog(boss, type);
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE bosslog SET count = ?,  time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ? and type = ?");
            ps.setInt(1, bossCount + count);
            ps.setInt(2, this.id);
            ps.setString(3, boss);
            ps.setInt(4, type);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }



    public void setBossLog1(final String boss, final int type, final int count) {
        final int bossCount = this.getBossLog1(boss, type);
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE bosslog2 SET count = ?,  time = CURRENT_TIMESTAMP() WHERE characterid = ? AND bossid = ? and type = ?");
            ps.setInt(1, bossCount + count);
            ps.setInt(2, this.id);
            ps.setString(3, boss);
            ps.setInt(4, type);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }

    public void setPublicRecord(final String boss, final int type, final int count) {
        final int bossCount = this.getPublicRecord(boss, type);
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE lt_public_record SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE record_name = ?");
            ps.setInt(1, bossCount + count);
            ps.setInt(2, type);
            ps.setString(3, boss);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    public int getPublicRecord(final String boss, final int type) {
        Connection con = DatabaseConnection.getConnection();
        try {
            int count = 0;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM lt_public_record WHERE  record_name = ? and type = ?");
            ps.setString(1, boss);
            ps.setInt(2, type);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                final Timestamp bossTime = rs.getTimestamp("time");
                rs.close();
                ps.close();
                if (type == 0) {
                    final Calendar sqlcal = Calendar.getInstance();
                    if (bossTime != null) {
                        sqlcal.setTimeInMillis(bossTime.getTime());
                    }
                    if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                        count = 0;
                        ps = con.prepareStatement("UPDATE lt_public_record SET count = 0, time = CURRENT_TIMESTAMP() WHERE  record_name = ? and type = ?");
                        ps.setString(1, boss);
                        ps.setInt(2, type);
                        ps.executeUpdate();
                    }
                }
            }
            else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO lt_public_record (record_name, count, type) VALUES (?, ?, ?)");
                psu.setString(1, boss);
                psu.setInt(2, 0);
                psu.setInt(3, type);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }finally {
            try {
                con.close();
            }catch (Exception e) {

            }
        }
    }
    
    public void resetBossLog(final String boss) {
        this.resetBossLog(boss, 0);
    }
    
    public void resetBossLog(final String boss, final int type) {
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("delete from  bosslog  WHERE type = ? and characterid = ? AND bossid = ?");
            ps.setInt(1, type);
            ps.setInt(2, this.id);
            ps.setString(3, boss);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    public void resetBossLog1(final String boss) {
        this.resetBossLog1(boss, 0);
    }

    public void resetBossLog1(final String boss, final int type) {
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("delete from  bosslog2  WHERE type = ? and characterid = ? AND bossid = ?");

            ps.setInt(1, type);
            ps.setInt(2, this.id);
            ps.setString(3, boss);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    public int getBossLogAcc(final String boss) {
        try {
            int count = 0;
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM bosslog WHERE accountid = ? AND bossid = ?");
            ps.setInt(1, this.accountid);
            ps.setString(2, boss);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                if (count < 0) {
                    return count;
                }
                final Timestamp bossTime = rs.getTimestamp("time");
                rs.close();
                ps.close();
                final Calendar sqlcal = Calendar.getInstance();
                if (bossTime != null) {
                    sqlcal.setTimeInMillis(bossTime.getTime());
                }
                if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                    count = 0;
                    ps = con.prepareStatement("UPDATE bosslog SET count = 0, time = CURRENT_TIMESTAMP() WHERE accountid = ? AND bossid = ?");
                    ps.setInt(1, this.accountid);
                    ps.setString(2, boss);
                    ps.executeUpdate();
                }
            }
            else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO bosslog (accountid, characterid, bossid, count) VALUES (?, ?, ?, ?)");
                psu.setInt(1, this.accountid);
                psu.setInt(2, 0);
                psu.setString(3, boss);
                psu.setInt(4, 0);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public void setBossLogAcc(final String bossid) {
        this.setBossLogAcc(bossid, 0);
    }
    
    public void setBossLogAcc(final String bossid, int bossCount) {
        bossCount += this.getBossLogAcc(bossid);
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE bosslog SET count = ?, characterid = ?, time = CURRENT_TIMESTAMP() WHERE accountid = ? AND bossid = ?");
            ps.setInt(1, bossCount + 1);
            ps.setInt(2, this.id);
            ps.setInt(3, this.accountid);
            ps.setString(4, bossid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    
    public int getAccNewTime(final String time) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from accounts where id = ? and createdat <= '" + time + "'");
            ps.setInt(1, this.accountid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getOneTimeLog(final String log) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int ret_count = 0;
            final PreparedStatement ps = con.prepareStatement("select sum(count) from OneTimelog where characterid = ? and log = ?");
            ps.setInt(1, this.id);
            ps.setString(2, log);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt(1);
            }
            else {
                ret_count = 0;
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
            return -1;
        }
    }
    
    public int getAddLog() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int money = 0;
            final PreparedStatement ps = con.prepareStatement("SELECT money FROM addlog WHERE accid = ?");
            ps.setInt(1, this.getClient().getAccID());
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                money += rs.getInt("money");
            }
            rs.close();
            ps.close();
            return money;
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            return -1;
        }
    }
    public void reFreshItem(IItem item) {
        List<ModifyInventory> mods = new ArrayList();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        this.getClient().sendPacket(MaplePacketCreator.modifyInventory(true, mods));
    }

    public void setOneTimeLog(String log, int count) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setOneTimeLog(this.getId(), log, count);
        }
    }
    public void setOneTimeLog(final String log) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into OneTimelog (characterid, log,count) values (?,?,?)");
            ps.setInt(1, this.id);
            ps.setString(2, log);
            ps.setInt(3, 1);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }

    public void deleteOneTimeLog(final String log) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("DELETE FROM onetimelog WHERE characterid = ? and log = ?");
            ps.setInt(1, this.id);
            ps.setString(2, log);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public void setPrizeLog(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into Prizelog (accid, bossid) values (?,?)");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public int getPrizeLog(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int ret_count = 0;
            final PreparedStatement ps = con.prepareStatement("select count(*) from Prizelog where accid = ? and bossid = ?");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt(1);
            }
            else {
                ret_count = 0;
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
            return -1;
        }
    }
    
    public void setAcLog(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into Aclog (accid, bossid) values (?,?)");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public void setAcLogS(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into Aclog (accid, bossid) values (?,?)");
            ps.setInt(1, this.getAccountID());
            ps.setString(2, bossid);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public int getAcLog(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int ret_count = 0;
            final PreparedStatement ps = con.prepareStatement("select count(*) from Aclog where accid = ? and bossid = ? and lastattempt >= subtime(current_timestamp, '1 0:0:0.0')");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt(1);
            }
            else {
                ret_count = 0;
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
            return -1;
        }
    }
    
    public int getAcLogS(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int ret_count = 0;
            final PreparedStatement ps = con.prepareStatement("select count(*) from Aclog where accid = ? and bossid = ?");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt(1);
            }
            else {
                ret_count = 0;
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
            return -1;
        }
    }

    public void dropMessage(final int type, final String message) {
        if (type == -1) {
            this.client.sendPacket(UIPacket.getTopMsg(message));
        }
        else if (type == -2) {
            this.client.sendPacket(PlayerShopPacket.shopChat(message, 0));
        }
        else if (type == -11) {
            this.client.getSession().writeAndFlush((Object)MaplePacketCreator.yellowChat(message));
        }
        else if (type == 6) {
            this.client.getSession().write(MaplePacketCreator.serverNotice(type, message));
        }
        else {
            this.client.sendPacket(MaplePacketCreator.serverNotice(type, message));
        }
    }
    
    public void showInfo(final String caption, final boolean pink, String msg) {
        final short type = (short)(pink ? 5 : 6);
        if (caption != null && !caption.isEmpty()) {
            msg = "[" + caption + "] " + msg;
        }
        this.dropMessage((int)type, msg);
        this.dropMessage(-1, msg);
    }
    
    public IMaplePlayerShop getPlayerShop() {
        return this.playerShop;
    }
    
    public void setPlayerShop(final IMaplePlayerShop playerShop) {
        this.playerShop = playerShop;
    }
    
    public int getConversation() {
        return this.inst.get();
    }
    
    public void setConversation(final int inst) {
        this.inst.set(inst);
    }
    
    public MapleCarnivalParty getCarnivalParty() {
        return this.carnivalParty;
    }
    
    public void setCarnivalParty(final MapleCarnivalParty party) {
        this.carnivalParty = party;
    }
    
    public void addCP(final int ammount) {
        this.totalCP += (short)ammount;
        this.availableCP += (short)ammount;
    }
    
    public void useCP(final int ammount) {
        if (this.availableCP >= ammount) {
            this.availableCP -= (short)ammount;
        }
    }
    
    public int getAvailableCP() {
        return this.availableCP;
    }
    
    public int getTotalCP() {
        return this.totalCP;
    }
    
    public void resetCP() {
        this.totalCP = 0;
        this.availableCP = 0;
    }
    
    public static int getIdByName(final String name) {
        try {
            int id;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
                 final PreparedStatement ps = con.prepareStatement("SELECT id FROM characters WHERE name = ?")) {
                ps.setString(1, name);
                try (final ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        rs.close();
                        ps.close();
                        return -1;
                    }
                    id = rs.getInt("id");
                }
            }
            return id;
        }
        catch (Exception e) {
            System.err.println("錯誤 'getIdByName' " + (Object)e);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            return -1;
        }
    }
    
    public void addCarnivalRequest(final MapleCarnivalChallenge request) {
        this.pendingCarnivalRequests.add(request);
    }
    
    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return (MapleCarnivalChallenge)this.pendingCarnivalRequests.pollLast();
    }
    
    public void clearCarnivalRequests() {
        this.pendingCarnivalRequests = new LinkedList<MapleCarnivalChallenge>();
    }
    
    public void startMonsterCarnival(final int enemyavailable, final int enemytotal) {
        this.client.sendPacket(MonsterCarnivalPacket.startMonsterCarnival(this, enemyavailable, enemytotal));
    }
    
    public void CPUpdate(final boolean party, final int available, final int total, final int team) {
        this.client.sendPacket(MonsterCarnivalPacket.CPUpdate(party, available, total, team));
    }
    
    public void playerDiedCPQ(final String name, final int lostCP, final int team) {
        this.client.sendPacket(MonsterCarnivalPacket.playerDiedMessage(name, lostCP, team));
    }
    
    public boolean getCanTalk() {
        return this.canTalk;
    }
    
    public void canTalk(final boolean talk) {
        this.canTalk = talk;
    }
    
    public double getEXPMod() {
        return this.stats.expMod;
    }
    
    public int getDropMod() {
        return this.stats.dropMod;
    }
    
        public double getDropm() {
        return this.stats.dropm;
    }
    public void setDropm(double drop) {
        this.stats.dropm = drop;
    }
    public int getItemDropm() {
        return this.stats.itemDropm;
    }
    public void setItemDropm(int itemDrop) {
        this.stats.itemDropm = itemDrop;
    }
    public int getItemExpm() {
        return this.stats.itemExpm;
    }
    public void setItemExpm(int itemExp) {
        this.stats.itemExpm = itemExp;
    }
    
    public double getExpm() {
        return this.stats.expm;
    }
    
    public int getCashMod() {
        return this.stats.cashMod;
    }
    
    public void setVPoints(final int p) {
        this.vpoints = p;
    }
    
    public int getVPoints() {
        return this.vpoints;
    }
    
    public CashShop getCashInventory() {
        return this.cs;
    }
    
    public void removeAll(final int id) {
        this.removeAll(id, false);
    }
    
    public void removeAll(final int id, final boolean show) {
        this.removeAll(id, false, false);
    }
    
    public void removeAll(final int id, final boolean show, final boolean equip) {
        MapleInventoryType type = GameConstants.getInventoryType(id);
        int possessed = this.getInventory(type).countById(id);
        if (possessed > 0) {
            MapleInventoryManipulator.removeById(this.getClient(), type, id, possessed, true, false);
            if (show) {
                this.getClient().sendPacket(MaplePacketCreator.getShowItemGain(id, (short)(-possessed), true));
            }
        }
        if (equip && type == MapleInventoryType.EQUIP) {
            type = MapleInventoryType.EQUIPPED;
            possessed = this.getInventory(type).countById(id);
            if (possessed > 0) {
                MapleInventoryManipulator.removeById(this.getClient(), type, id, possessed, true, false);
                this.getClient().sendPacket(MaplePacketCreator.getShowItemGain(id, (short)(-possessed), true));
            }
        }
    }
    
    public MapleRing getMarriageRing(final boolean incluedEquip) {
        MapleInventory iv = this.getInventory(MapleInventoryType.EQUIPPED);
        final Collection<IItem> equippedC = iv.list();
        final List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (final IItem item : equippedC) {
            equipped.add((Item)item);
        }
        for (final Item item2 : equipped) {
            if (item2.getRing() != null) {
                final MapleRing ring = item2.getRing();
                ring.setEquipped(true);
                if (GameConstants.isMarriageRing(item2.getItemId())) {
                    return ring;
                }
                continue;
            }
        }
        if (incluedEquip) {
            iv = this.getInventory(MapleInventoryType.EQUIP);
            for (final IItem item : iv.list()) {
                if (item.getRing() != null && GameConstants.isMarriageRing(item.getItemId())) {
                    final MapleRing ring = item.getRing();
                    ring.setEquipped(false);
                    return ring;
                }
            }
        }
        return null;
    }
    
    public Pair<List<MapleRing>, List<MapleRing>> getRings(final boolean equip) {
        MapleInventory iv = this.getInventory(MapleInventoryType.EQUIPPED);
        final Collection<IItem> equippedC = iv.list();
        final List<Item> equipped = new ArrayList<Item>(equippedC.size());
        for (final IItem item : equippedC) {
            equipped.add((Item)item);
        }
        Collections.sort(equipped);
        final List<MapleRing> crings = new ArrayList<MapleRing>();
        final List<MapleRing> frings = new ArrayList<MapleRing>();
        for (final Item item2 : equipped) {
            if (item2.getRing() != null) {
                final MapleRing ring = item2.getRing();
                ring.setEquipped(true);
                if (!GameConstants.isEffectRing(item2.getItemId())) {
                    continue;
                }
                if (equip) {
                    if (GameConstants.isCrushRing(item2.getItemId())) {
                        crings.add(ring);
                    }
                    else {
                        if (!GameConstants.isFriendshipRing(item2.getItemId())) {
                            continue;
                        }
                        frings.add(ring);
                    }
                }
                else if (crings.isEmpty() && GameConstants.isCrushRing(item2.getItemId())) {
                    crings.add(ring);
                }
                else {
                    if (!frings.isEmpty() || !GameConstants.isFriendshipRing(item2.getItemId())) {
                        continue;
                    }
                    frings.add(ring);
                }
            }
        }
        if (equip) {
            iv = this.getInventory(MapleInventoryType.EQUIP);
            for (final IItem item3 : iv.list()) {
                if (item3.getRing() != null && GameConstants.isEffectRing(item3.getItemId())) {
                    final MapleRing ring = item3.getRing();
                    ring.setEquipped(false);
                    if (GameConstants.isFriendshipRing(item3.getItemId())) {
                        frings.add(ring);
                    }
                    else {
                        if (!GameConstants.isCrushRing(item3.getItemId())) {
                            continue;
                        }
                        crings.add(ring);
                    }
                }
            }
        }
        Collections.sort(frings, (Comparator<? super MapleRing>)new RingComparator());
        Collections.sort(crings, (Comparator<? super MapleRing>)new RingComparator());
        return new Pair<List<MapleRing>, List<MapleRing>>(crings, frings);
    }
    
    public int getFH() {
        final MapleFoothold fh = this.getMap().getFootholds().findBelow(this.getPosition());
        if (fh != null) {
            return fh.getId();
        }
        return 0;
    }
    
    public void startFairySchedule(final boolean exp) {
        this.startFairySchedule(exp, false);
    }
    
    public void startFairySchedule(final boolean exp, final boolean equipped) {
        try {
            this.cancelFairySchedule(exp);
            if (this.fairyExp < 30 && this.stats.equippedFairy) {
                if (equipped) {
                    this.dropMessage(5, "您装备了精灵吊坠在1小时后经验获取将增加到 " + (this.fairyExp + 30) + "%");
                }
                this.fairySchedule = EtcTimer.getInstance().schedule((Runnable)new Runnable() {
                    @Override
                    public void run() {
                        if (fairyExp < 30 && stats.equippedFairy) {
                            fairyExp = (byte)30;
                            ++fairyHour;
                            MapleCharacter.this.dropMessage(5, "因装备精灵吊坠超过了" + (int)fairyHour + "小时，打怪時可以额外获得紅利经验值" + (int)fairyExp + "%.");
                            MapleCharacter.this.startFairySchedule(false, true);
                        }
                        else {
                            MapleCharacter.this.cancelFairySchedule(!stats.equippedFairy);
                        }
                    }
                }, 3600000L);
            }
            else {
                this.cancelFairySchedule(!this.stats.equippedFairy);
            }
        }
        catch (RejectedExecutionException ex) {}
    }
    
    public void cancelFairySchedule(final boolean exp) {
        if (this.fairySchedule != null) {
            this.fairySchedule.cancel(false);
            this.fairySchedule = null;
        }
        if (exp) {
            final int 精灵吊坠 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"精灵吊坠经验加成"));
            this.fairyExp = (byte)精灵吊坠;
        }
    }
    
    public void 人气经验加成() {
        final int 人气 = this.getFame() * (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"人气经验加成"));
        this.gainExp(人气, true, false, false);
    }
    
    public void 白银VIP经验加成(final boolean exp, final boolean equipped) {
        this.白银VIP经验加成(exp);
    }
    
    public void 白银VIP经验加成(final boolean exp) {
        if (exp) {
            this.fairyExp += 30;
        }
    }
    public void 经验戒指加成(final boolean exp) {
        if (exp) {
            this.fairyExp += 10;
        }
    }
    
    public byte getFairyExp() {
        return this.fairyExp;
    }
    
    public void setFairyExp(final byte Exp) {
        this.fairyExp = Exp;
    }
    
    public int getCoconutTeam() {
        return this.coconutteam;
    }
    
    public void setCoconutTeam(final int team) {
        this.coconutteam = team;
    }
    
    public void spawnPet(final byte slot) {
        this.spawnPet(slot, false, true);
    }
    
    public void spawnPet(final byte slot, final boolean lead) {
        this.spawnPet(slot, lead, true);
        final String[] 特殊宠物代码 = ServerProperties.getProperty("LtMS.petSpecial").split(",");
        boolean 特殊宠物开关 = false;
        if (LtMS.ConfigValuesMap.get("宠物buff开关") != null && LtMS.ConfigValuesMap.get("宠物buff开关")>0) {
            for (int i = 0; i < 特殊宠物代码.length; ++i) {
                if (this.getInventory(MapleInventoryType.CASH).getItem((short) slot).getItemId() == Integer.parseInt(特殊宠物代码[i])) {
                    特殊宠物开关 = true;
                }
            }
            if (LtMS.ConfigValuesMap.get("宠物特殊buff开关") != null && LtMS.ConfigValuesMap.get("宠物特殊buff开关")>0) {
                if (特殊宠物开关) {
                    MapleItemInformationProvider.getInstance().getItemEffect(LtMS.ConfigValuesMap.get("宠物特殊buff编码")).applyTo(this.client.getPlayer());
                }
            } else if (LtMS.ConfigValuesMap.get("宠物基础buff开关") != null && LtMS.ConfigValuesMap.get("宠物基础buff开关")>0) {
                MapleItemInformationProvider.getInstance().getItemEffect(LtMS.ConfigValuesMap.get("宠物普通buff编码")).applyTo(this.client.getPlayer());
            }
        }

    }
    
    public void spawnPet(final byte slot, final boolean lead, final boolean broadcast) {
        final IItem item = this.getInventory(MapleInventoryType.CASH).getItem((short)slot);
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (item == null || item.getItemId() > 5010000 || item.getItemId() < 5000000) {
            return;
        }
        switch (item.getItemId()) {
            case 5000028:
            case 5000047: {
                final MaplePet pet = MaplePet.createPet(item.getItemId() + 1, MapleInventoryIdentifier.getInstance());
                if (pet != null) {
                    MapleInventoryManipulator.addById(this.client, item.getItemId() + 1, (short)1, item.getOwner(), pet, 45L);
                    MapleInventoryManipulator.removeFromSlot(this.client, MapleInventoryType.CASH, (short)slot, (short)1, false);
                    break;
                }
                break;
            }
            default: {
                final MaplePet pet = item.getPet();
                if (pet == null || (item.getItemId() == 5000054 && pet.getSecondsLeft() <= 0) || (item.getExpiration() != -1L && item.getExpiration() <= System.currentTimeMillis())) {
                    break;
                }
                if (pet.getSummoned()) {
                    this.unequipPet(pet, false);
                    break;
                }
                int leadid = 8;
                if (GameConstants.isKOC((int)this.getJob())) {
                    leadid = 10000018;
                }
                else if (GameConstants.isAran((int)this.getJob())) {
                    leadid = 20000024;
                }
                if (this.getSkillLevel(SkillFactory.getSkill(leadid)) == 0 && this.getSummonedPet(0) != null) {
                    this.unequipPet(this.getSummonedPet(0), false);
                }
                else if (lead) {
                    this.shiftPetsRight();
                }
                final Point pos = this.getPosition();
                pet.setPos(pos);
                try {
                    pet.setFh(this.getMap().getFootholds().findBelow(pos).getId());
                }
                catch (NullPointerException e) {
                    pet.setFh(0);
                }
                pet.setStance(0);
                pet.setSummoned(this.getPetSlotNext() + 1);
                this.addPet(pet);
                if (this.getMap() != null) {
                    this.getMap().broadcastMessage(this, PetPacket.showPet(this, pet, false, false), true);
                    this.client.sendPacket(PetPacket.loadExceptionList(this, pet));
                    this.client.sendPacket(PetPacket.petStatUpdate(this));
                }
                break;
            }
        }
        this.client.sendPacket(PetPacket.emptyStatUpdate());
    }
    
    public void addMoveMob(final int mobid) {
        if (this.movedMobs.containsKey((Object)Integer.valueOf(mobid))) {
            this.movedMobs.put(Integer.valueOf(mobid), Integer.valueOf((int)Integer.valueOf(this.movedMobs.get((Object)Integer.valueOf(mobid))) + 1));
            if ((int)Integer.valueOf(this.movedMobs.get((Object)Integer.valueOf(mobid))) > 30) {
                for (MapleCharacter chr : this.getMap().getCharactersThreadsafe()) {
                    if (chr.getMoveMobs().containsKey((Object)Integer.valueOf(mobid))) {
                        chr.getClient().sendPacket(MobPacket.killMonster(mobid, 1));
                        chr.getMoveMobs().remove((Object)Integer.valueOf(mobid));
                    }
                }
            }
        }
        else {
            this.movedMobs.put(Integer.valueOf(mobid), Integer.valueOf(1));
        }
    }
    
    public Map<Integer, Integer> getMoveMobs() {
        return this.movedMobs;
    }
    
    public int getLinkMid() {
        return this.linkMid;
    }
    
    public void setLinkMid(final int lm) {
        this.linkMid = lm;
    }
    
    public boolean isClone() {
        return this.clone;
    }
    
    public void setClone(final boolean c) {
        this.clone = c;
    }
    
    public WeakReference<MapleCharacter>[] getClones() {
        return this.clones;
    }
    
    public MapleCharacter cloneLooks() {
        final MapleClient cloneClient = new MapleClient(null, null, (Channel)new MockIOSession());
        final int minus = this.getId() + Randomizer.nextInt(this.getId());
        final MapleCharacter ret = new MapleCharacter(true);
        ret.id = minus;
        ret.client = cloneClient;
        ret.exp = 0;
        ret.meso = 0;
        ret.beans = this.beans;
        ret.blood = this.blood;
        ret.month = this.month;
        ret.max_damage = this.max_damage;
        ret.exp_reserve = this.exp_reserve;
        ret.jf = this.jf;
        ret.zdjf = this.zdjf;
        ret.rwjf = this.rwjf;
        ret.zs = this.zs;
        ret.cz = this.cz;
        ret.dy = this.dy;
        ret.rmb = this.rmb;
        ret.yb = this.yb;
        ret.playerPoints = this.playerPoints;
        ret.playerEnergy = this.playerEnergy;
        ret.jf1 = this.jf1;
        ret.jf2 = this.jf2;
        ret.jf3 = this.jf3;
        ret.jf4 = this.jf4;
        ret.jf5 = this.jf5;
        ret.jf6 = this.jf6;
        ret.jf7 = this.jf7;
        ret.jf8 = this.jf8;
        ret.jf9 = this.jf9;
        ret.jf10 = this.jf10;
        ret.day = this.day;
        ret.charmessage = this.charmessage;
        ret.expression = this.expression;
        ret.constellation = this.constellation;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = this.name;
        ret.level = this.level;
        ret.fame = this.fame;
        ret.job = this.job;
        ret.hair = this.hair;
        ret.face = this.face;
        ret.skinColor = this.skinColor;
        ret.bookCover = this.bookCover;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = this.gmLevel;
        ret.gender = this.gender;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(this.getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = this.guildid;
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll((Map<? extends MapleBuffStat, ? extends MapleBuffStatValueHolder>)this.effects);
        if (ret.effects.get((Object)MapleBuffStat.ILLUSION) != null) {
            ret.effects.remove((Object)MapleBuffStat.ILLUSION);
        }
        if (ret.effects.get((Object)MapleBuffStat.SUMMON) != null) {
            ret.effects.remove((Object)MapleBuffStat.SUMMON);
        }
        if (ret.effects.get((Object)MapleBuffStat.PUPPET) != null) {
            ret.effects.remove((Object)MapleBuffStat.PUPPET);
        }
        ret.guildrank = this.guildrank;
        ret.allianceRank = this.allianceRank;
        ret.hidden = this.hidden;
        ret.setPosition(new Point(this.getPosition()));
        for (final IItem equip : this.getInventory(MapleInventoryType.EQUIPPED)) {
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }
        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.rocks = this.rocks;
        ret.regrocks = this.regrocks;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;
        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());
        while (this.map.getCharacterById(ret.id) != null || this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null) {
            final MapleCharacter mapleCharacter = ret;
            ++mapleCharacter.id;
        }
        ret.client.setPlayer(ret);
        return ret;
    }
    public void sendSkillEffect(int skillId, int effectType) {
        this.client.sendPacket(MaplePacketCreator.showOwnBuffEffect(skillId, effectType));
        this.map.broadcastMessageSkill(this, MaplePacketCreator.showBuffeffect(this.getId(), skillId, effectType), false);
    }
    public MapleCharacter cloneLooks(int cloneDamagePercentage) {
        MapleClient cloneClient = new MapleClient((MapleAESOFB)null, (MapleAESOFB)null, new MockIOSession());
        int minus = this.getId() + Randomizer.nextInt(this.getId());
        MapleCharacter ret = new MapleCharacter(true);
        ret.cloneDamagePercentage = cloneDamagePercentage;
        ret.id = minus;
        ret.client = cloneClient;
        ret.exp = 0;
        ret.meso = 0;
        ret.beans = this.beans;
        ret.max_damage = this.max_damage;
        ret.blood = this.blood;
        ret.month = this.month;
        ret.day = this.day;
        ret.charmessage = this.charmessage;
        ret.expression = this.expression;
        ret.constellation = this.constellation;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = this.name;
        ret.level = this.level;
        ret.fame = this.fame;
        ret.job = this.job;
        ret.hair = this.hair;
        ret.face = this.face;
        ret.skinColor = this.skinColor;
        ret.bookCover = this.bookCover;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = this.gmLevel;
        ret.gender = this.gender;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(this.getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = this.guildid;
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll(this.effects);
        if (ret.effects.get(MapleBuffStat.ILLUSION) != null) {
            ret.effects.remove(MapleBuffStat.ILLUSION);
        }

        if (ret.effects.get(MapleBuffStat.SUMMON) != null) {
            ret.effects.remove(MapleBuffStat.SUMMON);
        }

        if (ret.effects.get(MapleBuffStat.PUPPET) != null) {
            ret.effects.remove(MapleBuffStat.PUPPET);
        }

        ret.guildrank = this.guildrank;
        ret.allianceRank = this.allianceRank;
        ret.hidden = this.hidden;
        ret.setPosition(new Point(this.getPosition()));
        for (final IItem equip : this.getInventory(MapleInventoryType.EQUIPPED)) {
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }

        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.rocks = this.rocks;
        ret.regrocks = this.regrocks;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;
        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());
        ret.max_damage = this.max_damage;
        ret.exp_reserve = this.exp_reserve;
        ret.guildPoints = this.guildPoints;
        while (this.map.getCharacterById(ret.id) != null || this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null) {
            final MapleCharacter mapleCharacter = ret;
            ++mapleCharacter.id;
        }
        ret.client.setPlayer(ret);
        return ret;
    }
    public final void cloneLook() {
        if (this.cloneDamageRateList.length >0 && this.cloneDamageRateList[0]!=50) {
            this.cloneLook(this.cloneDamageRateList[0]);
        }else{
            this.cloneLook(50);
        }


    }

    public final void cloneLook(int damagePercentage) {
        if (this.clone) {
            return;
        }
            for(int i = 0; i < this.clones.length; ++i) {
                if (this.clones[i].get() == null) {
                    this.cloneDamageRateList[i] = damagePercentage;
                    MapleCharacter newp = this.cloneLooks(damagePercentage);
                    System.out.println(this.name+"克隆成功");
                    this.map.addPlayer(newp);
                    System.out.println(this.name+"克隆人载入地图成功");
                    this.map.broadcastMessage(MaplePacketCreator.updateCharLook(newp));
                    System.out.println(this.name+"克隆属性更新成功");
                    this.map.movePlayer(newp, this.getPosition());
                    System.out.println(this.name+"克隆人位置更新成功");
                    this.clones[i] = new WeakReference<MapleCharacter>(newp);
                    System.out.println(this.name+"克隆人装载成功");
                    return;
                }
            }
    }
    //20240622修复
    public void disposeClones() {
        this.numClones = 0;
        for (int i = 0; i < this.clones.length; ++i) {
            if (this.clones[i].get() != null) {
                this.map.removePlayer((MapleCharacter)this.clones[i].get());
                MapleCharacter mapleCharacter = this.clones[i].get();
                if (mapleCharacter==null){
                    continue;
                }
                mapleCharacter.getClient().disconnect(false, false);
                this.clones[i] = new WeakReference<MapleCharacter>(null);
                ++this.numClones;

            }
        }
    }
    
    public final int getCloneSize() {
        int z = 0;
        for (final WeakReference<MapleCharacter> clone1 : this.clones) {
            if (clone1.get() != null) {
                ++z;
            }
        }
        return z;
    }
    
    public void spawnClones() {
        if (this.numClones == 0 && this.stats.hasClone) {
            this.cloneLook();
        }
        for (int i = 0; i < this.numClones; ++i) {
            this.cloneLook();
        }
        this.numClones = 0;
    }
    
    public byte getNumClones() {
        return this.numClones;
    }
    
    public void spawnSavedPets() {
        for (int i = 0; i < this.petStore.length; ++i) {
            if (this.petStore[i] > -1) {
                this.spawnPet(this.petStore[i], false, false);
            }
        }
        this.client.sendPacket(PetPacket.petStatUpdate(this));
        this.petStore = new byte[] { -1, -1, -1 };
    }
    
    public final byte[] getPetStores() {
        return this.petStore;
    }
    
    public void resetStats(final int str, final int dex, final int int_, final int luk) {
        final Map<MapleStat, Integer> statup = new EnumMap<MapleStat, Integer>(MapleStat.class);
        int total = this.stats.getStr() + this.stats.getDex() + this.stats.getLuk() + this.stats.getInt() + this.getRemainingAp();
        total -= str;
        this.stats.setStr((short)str);
        total -= dex;
        this.stats.setDex((short)dex);
        total -= int_;
        this.stats.setInt((short)int_);
        total -= luk;
        this.stats.setLuk((short)luk);
        this.setRemainingAp((short)total);
        //测试改变属性
        statup.put(MapleStat.STR, Integer.valueOf(str) + LtMS.ConfigValuesMap.get("力量"));
        statup.put(MapleStat.DEX, Integer.valueOf(dex)+ LtMS.ConfigValuesMap.get("敏捷"));
        statup.put(MapleStat.INT, Integer.valueOf(int_)+ LtMS.ConfigValuesMap.get("智力"));
        statup.put(MapleStat.LUK, Integer.valueOf(luk)+ LtMS.ConfigValuesMap.get("运气"));
        statup.put(MapleStat.AVAILABLEAP, Integer.valueOf(total));
        this.client.sendPacket(MaplePacketCreator.updatePlayerStats(statup, false, this));
    }
    
    public Event_PyramidSubway getPyramidSubway() {
        return this.pyramidSubway;
    }
    
    public void setPyramidSubway(final Event_PyramidSubway ps) {
        this.pyramidSubway = ps;
    }
    
    public byte getSubcategory() {
        if (this.job >= 430 && this.job <= 434) {
            return 1;
        }
        return this.subcategory;
    }
    
//    public int itemQuantity(final int itemid) {
//        return this.getInventory(GameConstants.getInventoryType(itemid)).countById(itemid);
//    }
    
    public void setRPS(final RockPaperScissors rps) {
        this.rps = rps;
    }
    
    public RockPaperScissors getRPS() {
        return this.rps;
    }
    
    public long getNextConsume() {
        return this.nextConsume;
    }
    
    public void setNextConsume(final long nc) {
        this.nextConsume = nc;
    }
    
    public int getRank() {
        return this.rank;
    }
    
    public int getRankMove() {
        return this.rankMove;
    }
    
    public int getJobRank() {
        return this.jobRank;
    }
    
    public int getJobRankMove() {
        return this.jobRankMove;
    }
    
    public void dispelBuff() {
        final LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>> allBuffs = new LinkedList<Entry<MapleBuffStat, MapleBuffStatValueHolder>>((Collection<? extends Entry<MapleBuffStat, MapleBuffStatValueHolder>>)this.effects.entrySet());
        for (final Entry<MapleBuffStat, MapleBuffStatValueHolder> mbsvh : allBuffs) {
            final long startTime = ((MapleBuffStatValueHolder)mbsvh.getValue()).startTime;
            final long localDuration = (long)((MapleBuffStatValueHolder)mbsvh.getValue()).localDuration;
            final long nowtime = System.currentTimeMillis();
            if (startTime + localDuration - nowtime < 8000L) {
                this.dispelBuff(((MapleBuffStatValueHolder)mbsvh.getValue()).skillid);
            }
        }
    }
    
    public void ForcechangeChannel(final int channel) {
        final ChannelServer toch = ChannelServer.getInstance(channel);
        try {
            this.saveToDB(false, false,true);
        }
        catch (Exception ex) {
            FileoutputUtil.logToFile("logs/ForcechangeChannel保存数据異常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getClient().getAccountName() + " 账号ID " + this.getClient().getAccID() + " 角色名 " + this.getName() + " 角色ID " + this.getId());
            FileoutputUtil.outError("logs/ForcechangeChannel保存数据異常.txt", (Throwable)ex);
        }
        if (toch == null || toch.isShutdown()) {
            this.client.sendPacket(MaplePacketCreator.serverBlocked(1));
            return;
        }
        this.changeRemoval();
        this.dispelBuff();
        final ChannelServer ch = ChannelServer.getInstance(this.client.getChannel());
        if (this.getMessenger() != null) {
            Messenger.silentLeaveMessenger(this.getMessenger().getId(), new MapleMessengerCharacter(this));
        }
        PlayerBuffStorage.addBuffsToStorage(this.getId(), this.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(this.getId(), this.getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(this.getId(), this.getAllDiseases());
        World.channelChangeData(new CharacterTransfer(this), this.getId(), channel);
        ch.removePlayer(this);
        this.client.updateLoginState(6, this.client.getSessionIPAddress());
        this.client.sendPacket(MaplePacketCreator.getChannelChange(this.client, Integer.parseInt(toch.getSocket().split(":")[1])));
        this.getMap().removePlayer(this);
        this.client.setPlayer(null);
        this.client.setReceiving(false);
    }
    
    public void changeChannel(final int channel) {
        final ChannelServer toch = ChannelServer.getInstance(channel);
        try {
            this.saveToDB(false, false,true);
        }
        catch (Exception ex) {
            FileoutputUtil.logToFile("logs/更換頻道保存数据異常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.getClient().getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.getClient().getAccountName() + " 账号ID " + this.getClient().getAccID() + " 角色名 " + this.getName() + " 角色ID " + this.getId());
            FileoutputUtil.outError("logs/更換頻道保存数据異常.txt", (Throwable)ex);
        }
        if (channel == this.client.getChannel() || toch == null || toch.isShutdown()) {
            this.client.sendPacket(MaplePacketCreator.serverBlocked(1));
            return;
        }
        this.dispelBuff();
        this.changeRemoval();
        final ChannelServer ch = ChannelServer.getInstance(this.client.getChannel());
        if (this.getMessenger() != null) {
            Messenger.silentLeaveMessenger(this.getMessenger().getId(), new MapleMessengerCharacter(this));
        }
        PlayerBuffStorage.addBuffsToStorage(this.getId(), this.getAllBuffs());
        PlayerBuffStorage.addCooldownsToStorage(this.getId(), this.getCooldowns());
        PlayerBuffStorage.addDiseaseToStorage(this.getId(), this.getAllDiseases());
        PlayerBuffStorage.addSnailValuesToStorage(this.getId(), this.getSnailValues());
        World.channelChangeData(new CharacterTransfer(this), this.getId(), channel);
        if (ch != null) {
            ch.removePlayer(this);
        }
        this.client.updateLoginState(6, this.client.getSessionIPAddress());
        this.client.sendPacket(MaplePacketCreator.getChannelChange(this.client, Integer.parseInt(toch.getSocket().split(":")[1])));
        this.getMap().removePlayer(this);
        if (!LoginServer.CanLoginKey(this.getLoginKey(), this.getAccountID()) || (LoginServer.getLoginKey(this.getAccountID()) == null && !this.getLoginKey().isEmpty())) {
            FileoutputUtil.logToFile("logs/Data/客戶端登錄KEY異常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.client.getAccountName() + " 客戶端key：" + LoginServer.getLoginKey(this.getAccountID()) + " 伺服端key：" + this.getLoginKey() + " 更換頻道10");
            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
            this.client.getSession().close();
            return;
        }
        if (!LoginServer.CanServerKey(this.getServerKey(), this.getAccountID()) || (LoginServer.getServerKey(this.getAccountID()) == null && !this.getServerKey().isEmpty())) {
            FileoutputUtil.logToFile("logs/Data/客戶端頻道KEY異常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.client.getAccountName() + " 客戶端key：" + LoginServer.getServerKey(this.getAccountID()) + " 伺服端key：" + this.getServerKey() + " 更換頻道11");
            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
            this.client.getSession().close();
            return;
        }
        if (!LoginServer.CanClientKey(this.getClientKey(), this.getAccountID()) || (LoginServer.getClientKey(this.getAccountID()) == null && !this.getClientKey().isEmpty())) {
            FileoutputUtil.logToFile("logs/Data/客戶端進入KEY異常.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号: " + this.client.getAccountName() + " 客戶端key：" + LoginServer.getClientKey(this.getAccountID()) + " 伺服端key：" + this.getClientKey() + " 更換頻道12");
            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
            this.client.getSession().close();
            return;
        }
        final List<String> charNamesa = this.client.loadCharacterNamesByCharId(this.getId());
        for (final ChannelServer cs : ChannelServer.getAllInstances()) {
            for (final String name : charNamesa) {
                if (cs.getPlayerStorage().getCharacterByName(name) != null) {
                    FileoutputUtil.logToFile("logs/Data/非法登錄.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.client.getAccountName() + "更換頻道1");
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
                    this.client.getSession().close();
                    return;
                }
            }
        }
        for (final String name2 : charNamesa) {
            if (CashShopServer.getPlayerStorage().getCharacterByName(name2) != null) {
                FileoutputUtil.logToFile("logs/Data/非法登錄.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.client.getAccountName() + "更換頻道2");
                Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
                this.client.getSession().close();
                return;
            }
        }
        final List<String> charNames = this.client.loadCharacterNamesByCharId(this.getId());
        for (final ChannelServer cs2 : ChannelServer.getAllInstances()) {
            for (final String name3 : charNames) {
                final MapleCharacter character = cs2.getPlayerStorage().getCharacterByName(name3);
                if (character != null) {
                    FileoutputUtil.logToFile("logs/Data/非法登錄.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.client.getAccountName() + "更換頻道3");
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
                    this.client.getSession().close();
                    character.getClient().getSession().close();
                }
            }
        }
        for (final String name4 : charNames) {
            final MapleCharacter charactercs = CashShopServer.getPlayerStorage().getCharacterByName(name4);
            if (charactercs != null) {
                FileoutputUtil.logToFile("logs/Data/非法登錄.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + this.client.getSession().remoteAddress().toString().split(":")[0] + " 账号 " + this.client.getAccountName() + "更換頻道4");
                Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[GM 密语系統] 非法更換頻道 账号 " + this.client.getAccountName()));
                this.client.getSession().close();
                charactercs.getClient().getSession().close();
            }
        }
        this.client.setPlayer(null);
        this.client.setReceiving(false);
        this.等级上限 = this.取限制等级();
        this.expirationTask(true, false);

    }
    
    public void expandInventory(final byte type, final int amount) {
        final MapleInventory inv = this.getInventory(MapleInventoryType.getByType(type));
        inv.addSlot((byte)amount);
        this.client.sendPacket(MaplePacketCreator.getSlotUpdate(type, inv.getSlotLimit()));
    }
    
    public boolean allowedToTarget(final MapleCharacter other) {
        return other != null && (!other.isHidden() || this.getGMLevel() >= other.getGMLevel());
    }
    
    public int getFollowId() {
        return this.followid;
    }
    
    public void setFollowId(final int fi) {
        this.followid = fi;
        if (fi == 0) {
            this.followinitiator = false;
            this.followon = false;
        }
    }
    
    public void setFollowInitiator(final boolean fi) {
        this.followinitiator = fi;
    }
    
    public void setFollowOn(final boolean fi) {
        this.followon = fi;
    }
    
    public boolean isFollowOn() {
        return this.followon;
    }
    
    public boolean isFollowInitiator() {
        return this.followinitiator;
    }
    
    public void checkFollow() {
        if (this.followon) {
            this.map.broadcastMessage(MaplePacketCreator.followEffect(this.id, 0, null));
            this.map.broadcastMessage(MaplePacketCreator.followEffect(this.followid, 0, null));
            final MapleCharacter tt = this.map.getCharacterById(this.followid);
            this.client.sendPacket(MaplePacketCreator.getFollowMessage("Follow canceled."));
            if (tt != null) {
                tt.setFollowId(0);
                tt.getClient().sendPacket(MaplePacketCreator.getFollowMessage("Follow canceled."));
            }
            this.setFollowId(0);
        }
    }
    
    public int getMarriageId() {
        return this.marriageId;
    }
    
    public void setMarriageId(final int mi) {
        this.marriageId = mi;
    }
    
    public int getMarriageItemId() {
        return this.marriageItemId;
    }
    
    public void setMarriageItemId(final int mi) {
        this.marriageItemId = mi;
    }
    
    public boolean isStaff() {
        return this.gmLevel > PlayerGMRank.普通玩家.getLevel();
    }
    
    public boolean startPartyQuest(final int questid) {
        boolean ret = false;
        if (!this.quests.containsKey((Object)MapleQuest.getInstance(questid)) || !this.questinfo.containsKey((Object)Integer.valueOf(questid))) {
            final MapleQuestStatus status = this.getQuestNAdd(MapleQuest.getInstance(questid));
            status.setStatus((byte)1);
            this.updateQuest(status);
            switch (questid) {
                case 1300:
                case 1301:
                case 1302: {
                    this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0;gvup=0;vic=0;lose=0;draw=0");
                    break;
                }
                case 1204: {
                    this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;have2=0;have3=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                }
                case 1206: {
                    this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have0=0;have1=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                }
                default: {
                    this.updateInfoQuest(questid, "min=0;sec=0;date=0000-00-00;have=0;rank=F;try=0;cmp=0;CR=0;VR=0");
                    break;
                }
            }
            ret = true;
        }
        return ret;
    }
    
    public String getOneInfo(final int questid, final String key) {
        if (!this.questinfo.containsKey((Object)Integer.valueOf(questid)) || key == null) {
            return null;
        }
        final String[] split3;
        final String[] split = split3 = ((String)this.questinfo.get((Object)Integer.valueOf(questid))).split(";");
        for (final String x : split3) {
            final String[] split2 = x.split("=");
            if (split2.length == 2 && split2[0].equals((Object)key)) {
                return split2[1];
            }
        }
        return null;
    }
    
    public void updateOneInfo(final int questid, final String key, final String value) {
        if (!this.questinfo.containsKey((Object)Integer.valueOf(questid)) || key == null || value == null) {
            return;
        }
        final String[] split = ((String)this.questinfo.get((Object)Integer.valueOf(questid))).split(";");
        boolean changed = false;
        final StringBuilder newQuest = new StringBuilder();
        for (final String x : split) {
            final String[] split2 = x.split("=");
            if (split2.length == 2) {
                if (split2[0].equals((Object)key)) {
                    newQuest.append(key).append("=").append(value);
                }
                else {
                    newQuest.append(x);
                }
                newQuest.append(";");
                changed = true;
            }
        }
        this.updateInfoQuest(questid, changed ? newQuest.toString().substring(0, newQuest.toString().length() - 1) : newQuest.toString());
    }
    
    public void recalcPartyQuestRank(final int questid) {
        if (!this.startPartyQuest(questid)) {
            final String oldRank = this.getOneInfo(questid, "rank");
            if (oldRank == null || oldRank.equals((Object)"S")) {
                return;
            }
            final String s = oldRank;
            int n = -1;
            switch (s.hashCode()) {
                case 65: {
                    if (s.equals((Object)"A")) {
                        n = 0;
                        break;
                    }
                    break;
                }
                case 66: {
                    if (s.equals((Object)"B")) {
                        n = 1;
                        break;
                    }
                    break;
                }
                case 67: {
                    if (s.equals((Object)"C")) {
                        n = 2;
                        break;
                    }
                    break;
                }
                case 68: {
                    if (s.equals((Object)"D")) {
                        n = 3;
                        break;
                    }
                    break;
                }
                case 70: {
                    if (s.equals((Object)"F")) {
                        n = 4;
                        break;
                    }
                    break;
                }
            }
            String newRank = null;
            switch (n) {
                case 0: {
                    newRank = "S";
                    break;
                }
                case 1: {
                    newRank = "A";
                    break;
                }
                case 2: {
                    newRank = "B";
                    break;
                }
                case 3: {
                    newRank = "C";
                    break;
                }
                case 4: {
                    newRank = "D";
                    break;
                }
                default: {
                    return;
                }
            }
            final List<Pair<String, Pair<String, Integer>>> questInfo = MapleQuest.getInstance(questid).getInfoByRank(newRank);
            for (final Pair<String, Pair<String, Integer>> q : questInfo) {
                boolean found = false;
                final String val = this.getOneInfo(questid, (String)((Pair<String, Integer>)q.right).left);
                if (val == null) {
                    return;
                }
                int vall;
                try {
                    vall = Integer.parseInt(val);
                }
                catch (NumberFormatException e) {
                    return;
                }
                final String s2 = (String)q.left;
                int n2 = -1;
                switch (s2.hashCode()) {
                    case 3318169: {
                        if (s2.equals((Object)"less")) {
                            n2 = 0;
                            break;
                        }
                        break;
                    }
                    case 3357525: {
                        if (s2.equals((Object)"more")) {
                            n2 = 1;
                            break;
                        }
                        break;
                    }
                    case 96757556: {
                        if (s2.equals((Object)"equal")) {
                            n2 = 2;
                            break;
                        }
                        break;
                    }
                }
                switch (n2) {
                    case 0: {
                        found = (vall < (int)Integer.valueOf(((Pair<String, Integer>)q.right).right));
                        break;
                    }
                    case 1: {
                        found = (vall > (int)Integer.valueOf(((Pair<String, Integer>)q.right).right));
                        break;
                    }
                    case 2: {
                        found = (vall == (int)Integer.valueOf(((Pair<String, Integer>)q.right).right));
                        break;
                    }
                }
                if (!found) {
                    return;
                }
            }
            this.updateOneInfo(questid, "rank", newRank);
        }
    }
    
    public void tryPartyQuest(final int questid) {
        try {
            this.startPartyQuest(questid);
            this.pqStartTime = System.currentTimeMillis();
            this.updateOneInfo(questid, "try", String.valueOf(Integer.parseInt(this.getOneInfo(questid, "try")) + 1));
        }
        catch (Exception e) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)e, "tryPartyQuest error");
        }
    }
    
    public void cmpPartyQuest(final int questid, final int sl) {
        this.updateOneInfo(questid, "cmp", String.valueOf(Integer.parseInt(this.getOneInfo(questid, "cmp")) - sl));
    }
    
    public void endPartyQuest(final int questid) {
        try {
            this.startPartyQuest(questid);
            if (this.pqStartTime > 0L) {
                final long changeTime = System.currentTimeMillis() - this.pqStartTime;
                final int mins = (int)(changeTime / 1000L / 60L);
                final int secs = (int)(changeTime / 1000L % 60L);
                final int mins2 = Integer.parseInt(this.getOneInfo(questid, "min"));
                final int secs2 = Integer.parseInt(this.getOneInfo(questid, "sec"));
                if (mins2 <= 0 || mins < mins2) {
                    this.updateOneInfo(questid, "min", String.valueOf(mins));
                    this.updateOneInfo(questid, "sec", String.valueOf(secs));
                    this.updateOneInfo(questid, "date", FilePrinter.getLocalDateString());
                }
                final int newCmp = Integer.parseInt(this.getOneInfo(questid, "cmp")) + 1;
                this.updateOneInfo(questid, "cmp", String.valueOf(newCmp));
                this.updateOneInfo(questid, "CR", String.valueOf((int)Math.ceil((double)newCmp * 100.0 / (double)Integer.parseInt(this.getOneInfo(questid, "try")))));
                this.recalcPartyQuestRank(questid);
                this.pqStartTime = 0L;
            }
        }
        catch (Exception e) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)e, "endPartyQuest error");
        }
    }
    
    public void havePartyQuest(final int itemId) {
        int index = -1;
        int questid = 0;
        switch (itemId) {
            case 1002798: {
                questid = 1200;
                break;
            }
            case 1072369: {
                questid = 1201;
                break;
            }
            case 1022073: {
                questid = 1202;
                break;
            }
            case 1082232: {
                questid = 1203;
                break;
            }
            case 1002571:
            case 1002572:
            case 1002573:
            case 1002574: {
                questid = 1204;
                index = itemId - 1002571;
                break;
            }
            case 1122010: {
                questid = 1205;
                break;
            }
            case 1032060:
            case 1032061: {
                questid = 1206;
                index = itemId - 1032060;
                break;
            }
            case 3010018: {
                questid = 1300;
                break;
            }
            case 1122007: {
                questid = 1301;
                break;
            }
            case 1122058: {
                questid = 1302;
                break;
            }
            default: {
                return;
            }
        }
        this.startPartyQuest(questid);
        this.updateOneInfo(questid, "have" + (Object)((index == -1) ? "" : Integer.valueOf(index)), "1");
    }
    
    public void resetStatsByJob(final boolean beginnerJob) {
        final int baseJob = beginnerJob ? (this.job % 1000) : (this.job % 1000 / 100 * 100);
        if (baseJob == 100) {
            this.resetStats(25, 4, 4, 4);
        }
        else if (baseJob == 200) {
            this.resetStats(4, 4, 20, 4);
        }
        else if (baseJob == 300 || baseJob == 400) {
            this.resetStats(4, 25, 4, 4);
        }
        else if (baseJob == 500) {
            this.resetStats(4, 20, 4, 4);
        }
    }
    
    public boolean hasSummon() {
        return this.hasSummon;
    }
    
    public void setHasSummon(final boolean summ) {
        this.hasSummon = summ;
    }
    
    public void removeDoor() {
        final MapleDoor door = (MapleDoor)this.getDoors().iterator().next();
        for (MapleCharacter chr : door.getTarget().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (MapleCharacter chr : door.getTown().getCharactersThreadsafe()) {
            door.sendDestroyData(chr.getClient());
        }
        for (final MapleDoor destroyDoor : this.getDoors()) {
            door.getTarget().removeMapObject((MapleMapObject)destroyDoor);
            door.getTown().removeMapObject((MapleMapObject)destroyDoor);
        }
        this.clearDoors();
    }
    
    public void changeRemoval() {
        this.changeRemoval(false);
    }
    
    public void changeRemoval(final boolean dc) {
        if (this.getTrade() != null) {
            if (this.getTrade().getPartner() != null) {
                final MapleTrade local = this.getTrade();
                final MapleTrade partners = local.getPartner();
                if (local.isLocked() && partners.isLocked()) {
                    this.client.getSession().write((Object)MaplePacketCreator.enableActions());
                }
                else {
                    MapleTrade.cancelTrade(this.getTrade(), this.getClient());
                }
            }
            else {
                MapleTrade.cancelTrade(this.getTrade(), this.client);
            }
        }
        if (this.getCheatTracker() != null) {
            this.getCheatTracker().dispose();
        }
        if (!dc) {
            this.cancelEffectFromBuffStat(MapleBuffStat.MONSTER_RIDING);
            this.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
        }
        if (this.getPyramidSubway() != null) {
            this.getPyramidSubway().dispose(this);
        }
        if (this.playerShop != null && !dc) {
            this.playerShop.removeVisitor(this);
            if (this.playerShop.isOwner(this)) {
                this.playerShop.setOpen(true);
            }
        }
        if (!this.getDoors().isEmpty()) {
            this.removeDoor();
        }
        this.disposeClones();
        NPCScriptManager.getInstance().dispose(this.client);
    }
    
    public void updateTick(final int newTick) {
        if (this.anticheat != null) {
            this.anticheat.updateTick(newTick);
        }
    }
    
    public boolean canUseFamilyBuff(final MapleFamilyBuffEntry buff) {
        final MapleQuestStatus stat = this.getQuestNAdd(MapleQuest.getInstance(buff.questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Long.parseLong(stat.getCustomData()) + 86400000L < System.currentTimeMillis();
    }
    
    public void useFamilyBuff(final MapleFamilyBuffEntry buff) {
        final MapleQuestStatus stat = this.getQuestNAdd(MapleQuest.getInstance(buff.questID));
        stat.setCustomData(String.valueOf(System.currentTimeMillis()));
    }
    
    public List<Pair<Integer, Integer>> usedBuffs() {
        final List<Pair<Integer, Integer>> used = new ArrayList<Pair<Integer, Integer>>();
        for (final MapleFamilyBuffEntry buff : MapleFamilyBuff.getBuffEntry()) {
            if (!this.canUseFamilyBuff(buff)) {
                used.add(new Pair<Integer, Integer>(Integer.valueOf(buff.index), Integer.valueOf(buff.count)));
            }
        }
        return used;
    }
    
    public String getTeleportName() {
        return this.teleportname;
    }
    
    public void setTeleportName(final String tname) {
        this.teleportname = tname;
    }
    
    public int getNoJuniors() {
        if (this.mfc == null) {
            return 0;
        }
        return this.mfc.getNoJuniors();
    }
    
    public MapleFamilyCharacter getMFC() {
        return this.mfc;
    }
    
    public void makeMFC(final int familyid, final int seniorid, final int junior1, final int junior2) {
        if (familyid > 0) {
            final MapleFamily f = Family.getFamily(familyid);
            if (f == null) {
                this.mfc = null;
            }
            else {
                this.mfc = f.getMFC(this.id);
                if (this.mfc == null) {
                    this.mfc = f.addFamilyMemberInfo(this, seniorid, junior1, junior2);
                }
                if (this.mfc.getSeniorId() != seniorid) {
                    this.mfc.setSeniorId(seniorid);
                }
                if (this.mfc.getJunior1() != junior1) {
                    this.mfc.setJunior1(junior1);
                }
                if (this.mfc.getJunior2() != junior2) {
                    this.mfc.setJunior2(junior2);
                }
            }
        }
        else {
            this.mfc = null;
        }
    }
    
    public int 取破攻等级() {
        int 破功等级 = 0;
        try {
            final int cid = this.getId();
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM characters WHERE id=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                破功等级 = rs.getInt("PGSXDJ");
            }
            limitCheck.close();
            rs.close();
        }
        catch (SQLException ex) {}
        return 破功等级;
    }
    
    public void setFamily(final int newf, final int news, final int newj1, final int newj2) {
        if (this.mfc == null || newf != this.mfc.getFamilyId() || news != this.mfc.getSeniorId() || newj1 != this.mfc.getJunior1() || newj2 != this.mfc.getJunior2()) {
            this.makeMFC(newf, news, newj1, newj2);
        }
    }
    
    public int maxBattleshipHP(final int skillid) {
        return this.getSkillLevel(skillid) * 5000 + (this.getLevel() - 120) * 3000;
    }
    
    public int currentBattleshipHP() {
        return this.battleshipHP;
    }
    
    public int getGachExp() {
        return this.gachexp;
    }
    
    public void setGachExp(final int ge) {
        this.gachexp = ge;
    }
    
    public void sendEnglishQuiz(final String msg) {
        this.client.sendPacket(MaplePacketCreator.englishQuizMsg(msg));
    }
    
    public void fakeRelog() {
        final int chan = this.client.getChannel();
        this.client.sendPacket(MaplePacketCreator.getCharInfo(this));
        final MapleMap mapp = this.getMap();
        mapp.removePlayer(this);
        mapp.addPlayer(this);
        this.ForcechangeChannel(chan);
    }
    
    public String getcharmessage() {
        return this.charmessage;
    }
    
    public void setcharmessage(String s) {
        if (s.getBytes().length >= 24) {
            s = s.substring(0, 24);
        }
        this.charmessage = s;
    }
    
    public int getexpression() {
        return this.expression;
    }
    
    public void setexpression(final int s) {
        this.expression = s;
    }
    
    public int getconstellation() {
        return this.constellation;
    }
    
    public void setconstellation(final int s) {
        this.constellation = s;
    }
    
    public int getblood() {
        return this.blood;
    }
    
    public void setblood(final int s) {
        this.blood = s;
    }
    
    public int getmonth() {
        return this.month;
    }
    
    public void setmonth(final int s) {
        this.month = s;
    }
    
    public int getday() {
        return this.day;
    }
    
    public void setday(final int s) {
        this.day = s;
    }
    
    public int getTeam() {
        return this.coconutteam;
    }
    
    public int getBeans() {
        return this.beans;
    }
    
    public void gainBeans(final int s) {
        this.beans += s;
    }
    
    public void setBeans(final int s) {
        this.beans = s;
    }
    
    public int getBeansNum() {
        return this.beansNum;
    }
    
    public void setBeansNum(final int beansNum) {
        this.beansNum = beansNum;
    }
    
    public int getBeansRange() {
        return this.beansRange;
    }
    
    public void setBeansRange(final int beansRange) {
        this.beansRange = beansRange;
    }
    
    public boolean isCanSetBeansNum() {
        return this.canSetBeansNum;
    }
    
    public void setCanSetBeansNum(final boolean canSetBeansNum) {
        this.canSetBeansNum = canSetBeansNum;
    }
    
    public boolean getBeansStart() {
        return this.beansStart;
    }
    
    public void setBeansStart(final boolean beansStart) {
        this.beansStart = beansStart;
    }
    
    public boolean haveGM() {
        return this.gmLevel >= 2 && this.gmLevel <= 3;
    }
    
    public void setprefix(final String prefix) {
        this.prefix = prefix;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public void setPGSXDJ(final int PGSXDJ) {
        this.PGSXDJ = PGSXDJ;
    }
    
    public void gainPGSXDJ(final int PGSXDJ) {
        this.PGSXDJ += PGSXDJ;
    }
    
    public int getPGSXDJ() {
        return this.PGSXDJ;
    }

    public int getLimitBreak() {
        return (this.PGSXDJ * 10000 + 199999 ) ;
    }
    public int setLimitBreak( int limitBreak) {
        if(limitBreak<10000){
            limitBreak = 10000;
        }
        return PGSXDJ +=  Math.abs(limitBreak/10000) ;
    }
    public void gainItem(final int code, final int amount) {
        MapleInventoryManipulator.addById(this.client, code, (short)amount, null);
    }
    
    public void gainItem(final int code) {
        MapleInventoryManipulator.addById(this.client, code, (short)1, null);
    }
    
    public void giftMedal(final int id) {
        if (!this.getInventory(MapleInventoryType.EQUIP).isFull() && this.getInventory(MapleInventoryType.EQUIP).countById(id) == 0 && this.getInventory(MapleInventoryType.EQUIPPED).countById(id) == 0) {
            MapleInventoryManipulator.addById(this.client, id, (short)1);
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[恭喜]" + this.getName() + "剛才得到了 " + MapleItemInformationProvider.getInstance().getName(id) + "！"));
        }
        else if (this.getInventory(MapleInventoryType.EQUIP).countById(id) == 0 && this.getInventory(MapleInventoryType.EQUIPPED).countById(id) == 0) {
            MapleInventoryManipulator.drop(this.client, MapleInventoryType.EQUIP, (short)1, (short)1);
            MapleInventoryManipulator.addById(this.client, id, (short)1);
            Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[恭喜]" + this.getName() + "剛才得到了 " + MapleItemInformationProvider.getInstance().getName(id) + "！"));
        }
    }
    
    public void showInstruction(final String msg, final int width, final int height) {
        this.client.getSession().writeAndFlush((Object)MaplePacketCreator.sendHint(msg, width, height));
    }
    
    public String getVipName() {
        String name = "";
        if (this.getVip() > 0) {
            name = ServerConfig.getVipMedalName(this.getVip());
        }
        return name;
    }
    
    public String getNick() {
        String name = "";
        if (this.getOneTimeLog("关闭VIP星星數顯示") < 1 && this.getVipMedal() && this.getVip() > 0) {
            name += this.getVipName();
        }
        if (this.getGMLevel() > 0) {
            name += "";
        }
        return name;
    }
    
    public boolean getVipMedal() {
        return this.Vip_Medal;
    }
    
    public void setVipMedat(final boolean x) {
        this.Vip_Medal = x;
    }
    
    public void setVipMedal(final boolean x) {
        this.Vip_Medal = x;
    }
    
    public int getVipExpRate() {
        if (this.getVip() <= 5) {
            return (this.getVip() == 0) ? 0 : ((this.getVip() + 1) * 5);
        }
        return (this.getVip() == 0) ? 0 : ((this.getVip() + 1) * 5);
    }
    
    public void control_精灵商人(final boolean control) {
        this.精灵商人购买开关 = control;
    }
    
    public void control_玩家私聊(final boolean control) {
        this.玩家私聊开关 = control;
    }
    
    public void control_玩家密语(final boolean control) {
        this.玩家密语开关 = control;
    }
    
    public void control_好友聊天(final boolean control) {
        this.好友聊天开关 = control;
    }
    
    public void control_队伍聊天(final boolean control) {
        this.队伍聊天开关 = control;
    }
    
    public void control_公会聊天(final boolean control) {
        this.公会聊天开关 = control;
    }
    
    public void control_联盟聊天(final boolean control) {
        this.联盟聊天开关 = control;
    }
    
    public void control_吸怪信息(final boolean control) {
        this.GM吸怪信息开关 = control;
    }
    
    public boolean get_control_精灵商人() {
        return this.精灵商人购买开关;
    }
    
    public boolean get_control_玩家私聊() {
        return this.玩家私聊开关;
    }
    
    public boolean get_control_玩家密语() {
        return this.玩家密语开关;
    }
    
    public boolean get_control_好友聊天() {
        return this.好友聊天开关;
    }
    
    public boolean get_control_队伍聊天() {
        return this.队伍聊天开关;
    }
    
    public boolean get_control_公会聊天() {
        return this.公会聊天开关;
    }
    
    public boolean get_control_联盟聊天() {
        return this.联盟聊天开关;
    }
    
    public boolean get_control_吸怪信息() {
        return this.GM吸怪信息开关;
    }
    
    public int getDiseaseSize() {
        return this.diseases.size();
    }
    
    public int getMSG() {
        return this.MSG;
    }
    
    public void setMSG(final int x) {
        this.MSG = x;
    }
    
    public void addMSG() {
        ++this.MSG;
    }
    
    public void fly() {
    }
    
    public final boolean CanStorage() {
        //仓库操作时间间隔
        Integer time = LtMS.ConfigValuesMap.get("仓库操作时间间隔");
        if (this.lastStorageTime + (Objects.nonNull(time) ? time : 5000L) > System.currentTimeMillis()) {
            return false;
        }
        this.lastStorageTime = System.currentTimeMillis();
        return true;
    }
    
    public final boolean canHP() {
        if (this.lastHPTime + 5000L > System.currentTimeMillis()) {
            return false;
        }
        this.lastHPTime = System.currentTimeMillis();
        return true;
    }
    
    public final boolean canMP() {
        if (this.lastMPTime + 5000L > System.currentTimeMillis()) {
            return false;
        }
        this.lastMPTime = System.currentTimeMillis();
        return true;
    }
    
    public final boolean canUseMD() {
        if (this.lastMDTime + 5000L > System.currentTimeMillis()) {
            return false;
        }
        this.lastMDTime = System.currentTimeMillis();
        return true;
    }
    
    public void add打怪() {
        ++this.打怪;
    }
    
    public int get打怪() {
        return this.打怪;
    }
    
    public void add吸怪() {
        ++this.吸怪;
    }
    
    public int get吸怪() {
        return this.吸怪;
    }
    
    public void addFly_吸怪() {
        ++this.FLY_吸怪;
    }
    
    public int getFly_吸怪() {
        return this.FLY_吸怪;
    }
    
    public int Aran_ReduceCombo(final int skill) {
        int reduce = 0;
        switch (skill) {
            case 21100004:
            case 21100005: {
                reduce = 30;
                break;
            }
            case 21110004: {
                reduce = 100;
                break;
            }
            case 21120006:
            case 21120007: {
                reduce = 200;
                break;
            }
        }
        return reduce;
    }
    
    public int getAcash() {
        return this.getAcash(this);
    }
    
    public int getAcash(MapleCharacter chr) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select Acash from Accounts Where id = " + chr.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("Acash");
                    }
                    catch (Exception ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("Acash");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getAcash]無法連接資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "getAcash");
                System.err.println("[getAcash]" + (Object)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setAcash(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set Acash = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[Acash]無法連接資料庫");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "SetAcash");
            System.err.println("[setAcash]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public int getCZJF() {
        return this.getCZJF(this);
    }
    
    public int getCZJF(MapleCharacter chr) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select CZJF from Accounts Where id = " + chr.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("CZJF");
                    }
                    catch (Exception ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("CZJF");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getCZJF]無法連接資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "getCZJF");
                System.err.println("[getCZJF]" + (Object)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setCZJF(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set CZJF = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[CZJF]無法連接資料庫");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "SetCZJF");
            System.err.println("[setCZJF]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public int getTGJF() {
        return this.getTGJF(this);
    }
    
    public int getTGJF(MapleCharacter chr) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select TGJF from Accounts Where id = " + chr.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("TGJF");
                    }
                    catch (Exception ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("TGJF");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getTGJF]無法連接資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "getTGJF");
                System.err.println("[getTGJF]" + (Object)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setTGJF(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set TGJF = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[TGJF]無法連接資料庫");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "SetTGJFF");
            System.err.println("[setTGJF]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public int getTJJF() {
        return this.getTJJF(this);
    }
    
    public int getTJJF(MapleCharacter chr) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select TJJF from Accounts Where id = " + chr.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("TJJF");
                    }
                    catch (Exception ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("TJJF");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getTJJF]無法連接資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "getTJJF");
                System.err.println("[getTJJF]" + (Object)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setTJJF(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set TJJF = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[TJJF]無法連接資料庫");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "SetTJJFF");
            System.err.println("[setTJJF]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public void modifyJF(final int type, final int quantity) {
        switch (type) {
            case 1: {
                final int CZJF = this.getCZJF();
                if (CZJF + quantity < 0) {
                    return;
                }
                this.setCZJF(CZJF + quantity);
                break;
            }
            case 2: {
                final int TGJF = this.getTGJF();
                if (TGJF + quantity < 0) {
                    return;
                }
                this.setTGJF(TGJF + quantity);
                break;
            }
            case 3: {
                final int TJJF = this.getTJJF();
                if (TJJF + quantity < 0) {
                    return;
                }
                this.setTJJF(TJJF + quantity);
                break;
            }
            case 4: {
                final int DDJF = this.getDDJF();
                if (DDJF + quantity < 0) {
                    return;
                }
                this.setDDJF(DDJF + quantity);
                break;
            }
        }
    }
    
    public boolean getAuto吸怪() {
        return this.auto吸怪;
    }
    
    public void setAuto吸怪(final boolean x) {
        this.auto吸怪 = x;
    }
    
    public void warpAuto吸怪(MapleCharacter chr_) {
        MapleCharacter chr = this;
        try {
            if (chr.getMapId() != chr_.getMapId()) {
                chr.changeMap(chr_.getMapId());
                chr.changeMap(chr_.getMap(), chr_.getMap().findClosestSpawnpoint(chr_.getPosition()));
            }
            if (chr.getClient().getChannel() != chr_.getClient().getChannel()) {
                chr.changeChannel(chr_.getClient().getChannel());
            }
        }
        catch (Exception ex) {}
    }
    
    public void setDebugMessage(final boolean control) {
        this.DebugMessage = control;
    }
    
    public boolean getDebugMessage() {
        return this.DebugMessage;
    }
    
    public void RemoveHired() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("Delete From hiredmerch Where characterid = ?");
            ps.setInt(1, this.id);
            ps.execute();
            ps = con.prepareStatement("Delete From hiredmerchitems Where characterid = ?");
            ps.setInt(1, this.id);
            ps.execute();
            ps.close();
        }
        catch (Exception ex) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "RemoveHired");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
    }
    
    public void maxSkills() {
        for (final ISkill skil : SkillFactory.getAllSkills()) {
            this.changeSkillLevel(skil, skil.getMaxLevel(), skil.getMaxLevel());
        }
    }
    
    public void clearSkills() {
        for (final ISkill skil : SkillFactory.getAllSkills()) {
            this.changeSkillLevel(skil, (byte)0, (byte)0);
        }
    }
    
    public void LearnSameSkill(final MapleCharacter victim) {
        for (final ISkill skil : SkillFactory.getAllSkills()) {
            if (victim.getSkillLevel(skil) > 0) {
                this.changeSkillLevel(skil, victim.getSkillLevel(skil), victim.getMasterLevel(skil));
            }
        }
    }
    
    public int getStr() {
        return this.getStat().getStr();
    }
    
    public int getInt() {
        return this.getStat().getInt();
    }
    
    public int getLuk() {
        return this.getStat().getLuk();
    }
    
    public int getDex() {
        return this.getStat().getDex();
    }
    
    public int getHp() {
        return this.getStat().getHp();
    }
    
    public int getMp() {
        return this.getStat().getMp();
    }
    
    public int getMaxHp() {
        return this.getStat().getMaxHp();
    }
    
    public int getMaxMp() {
        return this.getStat().getMaxMp();
    }
    
    public void setHp(final int amount) {
        this.getStat().setHp(amount);
    }
    
    public void setMp(final int amount) {
        this.getStat().setMp(amount);
    }
    
    public void setMaxHp(final int amount) {
        this.getStat().setMaxHp((short)amount);
    }
    
    public void setMaxMp(final int amount) {
        this.getStat().setMaxMp((short)amount);
    }
    
    public void setStr(final int str) {
        this.stats.str = (short)str;
        this.stats.recalcLocalStats(false);
    }
    
    public void setLuk(final int luk) {
        this.stats.luk = (short)luk;
        this.stats.recalcLocalStats(false);
    }
    
    public void setDex(final int dex) {
        this.stats.dex = (short)dex;
        this.stats.recalcLocalStats(false);
    }
    
    public void setInt(final int int_) {
        this.stats.int_ = (short)int_;
        this.stats.recalcLocalStats(false);
    }
    
    public void setMeso(final int mesos) {
        this.meso = mesos;
    }
    
    public void updateFame() {
        this.updateSingleStat(MapleStat.FAME, (int)this.getFame());
    }
    
    public boolean inBossMap() {
        return MapConstants.inBossMap(this.getMapId());
    }
    
    public static boolean isVpn(final String ip) {
        int n = -1;
        switch (ip.hashCode()) {
            case -218956429: {
                if (ip.equals((Object)"/1.34.145.220")) {
                    n = 0;
                    break;
                }
                break;
            }
            case -641759509: {
                if (ip.equals((Object)"/59.125.5.52")) {
                    n = 1;
                    break;
                }
                break;
            }
            case -685134090: {
                if (ip.equals((Object)"/59.126.97.123")) {
                    n = 2;
                    break;
                }
                break;
            }
            case 645706596: {
                if (ip.equals((Object)"/60.251.73.100")) {
                    n = 3;
                    break;
                }
                break;
            }
            case 1562443102: {
                if (ip.equals((Object)"/61.219.216.173")) {
                    n = 4;
                    break;
                }
                break;
            }
            case 1562443103: {
                if (ip.equals((Object)"/61.219.216.174")) {
                    n = 5;
                    break;
                }
                break;
            }
            case 796693722: {
                if (ip.equals((Object)"/61.227.252.169")) {
                    n = 6;
                    break;
                }
                break;
            }
            case -1091107193: {
                if (ip.equals((Object)"/61.228.228.128")) {
                    n = 7;
                    break;
                }
                break;
            }
        }
        switch (n) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public boolean isKOC() {
        return this.job >= 1000 && this.job < 2000;
    }
    
    public boolean isAran() {
        return this.job >= 2000 && this.job <= 2112 && this.job != 2001;
    }
    
    public boolean isAdventurer() {
        return this.job >= 0 && this.job < 1000;
    }
    
    public boolean isCygnus() {
        return this.job >= 1000 && this.job <= 1512;
    }
    
    public boolean isGod() {
        return this.gmLevel >= 100;
    }
    
    public static String getCharacterNameById(final int id) {
        String name = null;
        MapleCharacter chr = getOnlineCharacterById(id);
        if (chr != null) {
            return chr.getName();
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            final Statement stmt = con.createStatement();
            ps = con.prepareStatement("select name from characters where id = ?");
            ps.setInt(1, id);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
            ps.close();
            rs.close();
        }
        catch (Exception ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return name;
    }
    
    public static String getCharacterNameById2(final int id) {
        String name = null;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            final Statement stmt = con.createStatement();
            ps = con.prepareStatement("select name from characters where id = ?");
            ps.setInt(1, id);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
            }
            ps.close();
            rs.close();
        }
        catch (Exception ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return name;
    }
    
    public static int getCharacterIdByName(final String name) {
        int id = -1;
        MapleCharacter chr = getOnlineCharacterByName(name);
        if (chr != null) {
            return chr.getId();
        }
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            final Statement stmt = con.createStatement();
            ps = con.prepareStatement("select id from characters where name = ?");
            ps.setString(1, name);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                id = rs.getInt("id");
            }
            ps.close();
            rs.close();
        }
        catch (Exception ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return id;
    }
    
    public static MapleCharacter getOnlineCharacterById(final int cid) {
        MapleCharacter chr = null;
        if (Find.findChannel(cid) >= 1) {
            chr = ChannelServer.getInstance(Find.findChannel(cid)).getPlayerStorage().getCharacterById(cid);
            if (chr != null) {
                return chr;
            }
        }
        return null;
    }
    
    public static MapleCharacter getOnlineCharacterByName(final String name) {
        MapleCharacter chr = null;
        if (Find.findChannel(name) >= 1) {
            chr = ChannelServer.getInstance(Find.findChannel(name)).getPlayerStorage().getCharacterByName(name);
            if (chr != null) {
                return chr;
            }
        }
        return null;
    }
    
    public static MapleCharacter getCharacterById(final int cid) {
        MapleCharacter chr = getOnlineCharacterById(cid);
        if (chr != null) {
            return chr;
        }
        final String name = getCharacterNameById(cid);
        return (name == null) ? null : loadCharFromDB(cid, new MapleClient(null, null, (Channel)new MockIOSession()), true);
    }
    
    public static MapleCharacter getCharacterByName(final String name) {
        MapleCharacter chr = getOnlineCharacterByName(name);
        if (chr != null) {
            return chr;
        }
        final int cid = getCharacterIdByName(name);
        return (cid == -1) ? null : loadCharFromDB(cid, new MapleClient(null, null, (Channel)new MockIOSession()), true);
    }
    
    public static void setMP(final Map<MapleCharacter, Integer> GiveList, final boolean showMessage) {
        final Iterator<MapleCharacter> iter = GiveList.keySet().iterator();
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = null;
            while (iter.hasNext()) {
                final StringBuilder sql = new StringBuilder();
                MapleCharacter chr = (MapleCharacter)iter.next();
                final int MP = (int)Integer.valueOf(GiveList.get((Object)chr));
                sql.append("Update Accounts set MP = ");
                sql.append(chr.getMP() + MP);
                sql.append(" Where id = ");
                sql.append(chr.getAccountID());
                ps = con.prepareStatement(sql.toString());
                ps.execute();
                if (showMessage) {
                    final String MSG = "「在線獎勵」获得在線點數 " + MP + " 若要領取請輸入 @在線點數/@jcds";
                    chr.dropMessage(MSG);
                }
            }
            if (ps != null) {
                ps.close();
            }
        }
        catch (SQLException ex) {
            System.err.println("[setMP]無法連接資料庫 " + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "setMP");
            System.err.println("[setMP]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public void setMP(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set MP = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[setMP]無法連接資料庫 " + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "setMP");
            System.err.println("[setMP]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public int getMP() {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int mp = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select MP from Accounts Where id = " + this.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("MP");
                    }
                    catch (Exception ex2) {}
                    if (debug != -1) {
                        mp = rs.getInt("MP");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getMP] 無法連接資料庫" + (Object)SQL);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "getMP");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return mp;
    }
    
    public boolean hasBlockedInventory(final boolean dead) {
        boolean has = false;
        if (dead) {
            has = (!this.isAlive() || this.getTrade() != null || this.getConversation() > 0 || this.getPlayerShop() != null);
        }
        else {
            has = (this.getTrade() != null || this.getConversation() > 0 || this.getPlayerShop() != null);
        }
        return has;
    }
    
    public boolean hasBlockedInventory2(final boolean dead) {
        boolean has = false;
        if (dead) {
            has = (!this.isAlive() || this.getTrade() != null || this.getPlayerShop() != null);
        }
        else {
            has = (this.getTrade() != null || this.getPlayerShop() != null);
        }
        return has;
    }
    
    public String getNowMacs() {
        return this.nowmacs;
    }
    
    public void setNowMacs(final String macs) {
        this.nowmacs = macs;
    }
    
    public int loadVip(final int accountID) {
        int vip = 0;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("SELECT vip FROM accounts WHERE id = ?");
            ps.setInt(1, accountID);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                vip = rs.getShort("vip");
                ps.close();
                rs.close();
            }
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
        }
        return vip;
    }
    
    public Map<Integer, MapleBuffStatValueHolder> getSkillID() {
        return this.skillID;
    }
    
    public void startMapEffect(final String msg, final int itemId) {
        this.startMapEffect(msg, itemId, 10000);
    }
    
    public void startMapEffect(final String msg, final int itemId, final int duration) {
        final MapleMapEffect mapEffect = new MapleMapEffect(msg, itemId);
        this.getClient().getSession().writeAndFlush((Object)mapEffect.makeStartData());
        EventTimer.getInstance().schedule((Runnable)new Runnable() {
            @Override
            public void run() {
                MapleCharacter.this.getClient().getSession().writeAndFlush((Object)mapEffect.makeDestroyData());
            }
        }, (long)duration);
    }
    
    public void forceCompleteQuest(final int id) {
        MapleQuest.getInstance(id).forceComplete(this, 0);
    }
    
    public String getLoginKey() {
        return this.loginkey;
    }
    
    public String getServerKey() {
        return this.serverkey;
    }
    
    public String getClientKey() {
        return this.clientkey;
    }
    
    public boolean chrdangerousIp(final String lip) {
        final String ip = lip.substring(1, lip.lastIndexOf(58));
        boolean ret = false;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dangerousip WHERE ? LIKE CONCAT(ip, '%')")) {
            ps.setString(1, ip);
            try (final ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    ret = true;
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("Error dangerousIp " + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public void setChrDangerousIp(final String lip) {
        final String ip = lip.substring(1, lip.lastIndexOf(58));
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("INSERT INTO dangerousip (ip) VALUES (?)");
            ps.setString(1, ip);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public void updateNewState(final int newstate, final int accountId) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE `accounts` SET `loggedin` = ? WHERE id = ?")) {
            ps.setInt(1, newstate);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
        catch (SQLException ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
    }
    
    public int getIntNoRecord(final int questID) {
        final MapleQuestStatus stat = this.getQuestNoAdd(MapleQuest.getInstance(questID));
        if (stat == null || stat.getCustomData() == null) {
            return 0;
        }
        return Integer.parseInt(stat.getCustomData());
    }
    
    public int getIntRecord(final int questID) {
        final MapleQuestStatus stat = this.getQuestNAdd(MapleQuest.getInstance(questID));
        if (stat.getCustomData() == null) {
            stat.setCustomData("0");
        }
        return Integer.parseInt(stat.getCustomData());
    }
    
    public void updatePetAuto() {
        if (this.getIntNoRecord(122221) > 0) {
            this.client.getSession().writeAndFlush((Object)MaplePacketCreator.petAutoHP(this.getIntRecord(122221)));
        }
        if (this.getIntNoRecord(122223) > 0) {
            this.client.getSession().writeAndFlush((Object)MaplePacketCreator.petAutoMP(this.getIntRecord(122223)));
        }
    }
    
    public long getChrMeso() {
        long meso = 0L;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                meso += (long)rs.getInt("meso");
            }
            rs.close();
        }
        catch (Exception e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            //e.printStackTrace();
        }
        return meso;
    }
    
    public long getStorageMeso() {
        long meso = 0L;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * FROM storages")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                meso += (long)rs.getInt("meso");
            }
            rs.close();
        }
        catch (Exception e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            //e.printStackTrace();
        }
        return meso;
    }
    
    public long getHiredMerchMeso() {
        long meso = 0L;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT * FROM hiredmerch")) {
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                meso += (long)rs.getInt("Mesos");
            }
            rs.close();
        }
        catch (Exception e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            //e.printStackTrace();
        }
        return meso;
    }
    
    public int getQianDaoTime(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from bosslog where characterid = ? and bossid = ? and lastattempt >= DATE_SUB(curdate(),INTERVAL 0 DAY)");
            ps.setInt(1, this.id);
            ps.setString(2, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getQianDaoAcLog(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int ret_count = 0;
            final PreparedStatement ps = con.prepareStatement("select count(*) from Aclog where accid = ? and bossid = ? and lastattempt >= DATE_SUB(curdate(),INTERVAL 0 DAY)");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt(1);
            }
            else {
                ret_count = 0;
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (Exception Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
            return -1;
        }
    }
    
    public boolean ChrDangerousAcc(final String acc) {
        boolean ret = false;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
             final PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM dangerousacc WHERE ? LIKE CONCAT(acc, '%')")) {
            ps.setString(1, acc);
            try (final ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    ret = true;
                }
            }
        }
        catch (SQLException ex) {
            System.err.println("Error dangerousname " + (Object)ex);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        return ret;
    }
    
    public void setChrDangerousAcc(final String acc) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("INSERT INTO dangerousacc (acc) VALUES (?)");
            ps.setString(1, acc);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public long getChangeTime() {
        return this.mapChangeTime;
    }
    
    public void setChangeTime(final boolean changeMap) {
        this.mapChangeTime = System.currentTimeMillis();
        if (changeMap) {
            this.getCheatTracker().resetInMapIimeCount();
        }
    }
    
    public int getDDJF() {
        return this.getDDJF(this);
    }
    
    public int getDDJF(MapleCharacter chr) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select DDJF from Accounts Where id = " + chr.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("DDJF");
                    }
                    catch (Exception ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("DDJF");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getDDJF]無法連接資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FilePrinter.printError("MapleCharacter.txt", (Throwable)ex, "getDDJF");
                System.err.println("[getDDJF]" + (Object)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setDDJF(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set DDJF = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[DDJF]無法連接資料庫");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            FilePrinter.printError("MapleCharacter.txt", (Throwable)ex2, "SetDDJFF");
            System.err.println("[setDDJF]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public final boolean canHold() {
        for (int i = 1; i <= 5; ++i) {
            if (this.getInventory(MapleInventoryType.getByType((byte)i)).getNextFreeSlot() <= -1) {
                return false;
            }
        }
        return true;
    }
    
    public void deleteAcLog(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("DELETE FROM Aclog WHERE accid = ? and bossid = ?");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public int getAcLogD(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from Aclog where accid = ? and bossid = ? and lastattempt >= DATE_SUB(curdate(),INTERVAL 0 DAY)");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getAclogY(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from Aclog where accid = ? and bossid = ? and DATE_FORMAT(lastattempt, '%Y%m') = DATE_FORMAT(CURDATE( ), '%Y%m')");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getBossLogS(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from bosslog where characterid = ? and bossid = ?");
            ps.setInt(1, this.id);
            ps.setString(2, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getBossLogC(final String bossid) {
        int ret_count = 0;
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from bosslog where characterid = ? and bossid = ?");
            ps.setInt(1, this.id);
            ps.setString(2, bossid);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret_count += rs.getInt(1);
            }
            ps.close();
            rs.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getAcLogC(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int ret_count = 0;
            final PreparedStatement ps = con.prepareStatement("select count(*) from Aclog where accid = ? and bossid = ?");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, bossid);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ret_count += rs.getInt(1);
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
            return -1;
        }
    }
    
    public int getBossLogD(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from bosslog where characterid = ? and bossid = ? and lastattempt >= DATE_SUB(curdate(),INTERVAL 0 DAY)");
            ps.setInt(1, this.id);
            ps.setString(2, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return 0;
        }
    }
    
    public int getBossLogY(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from bosslog where characterid = ? and bossid = ? and DATE_FORMAT(lastattempt, '%Y%m') = DATE_FORMAT(CURDATE( ), '%Y%m')");
            ps.setInt(1, this.id);
            ps.setString(2, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public long getMrqdTime() {
        return this.mrqdTime;
    }
    
    public void setMrqdTime(final long r) {
        this.mrqdTime = r;
    }
    
    public int getStChrLog() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from characterid where stlog = ?");
            ps.setInt(1, this.id);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public String getStChrNameLog(final int id) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        String name = "";
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select characterid from stlog Where stid = " + id);
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("characterid");
                    }
                    catch (SQLException ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("characterid");
                        name = name + getCharacterNameById(x) + ",";
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return name;
    }
    
    public int getStLog() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from stlog where characterid = ?");
            ps.setInt(1, this.id);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getStjfLog(final int id) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from stjflog where characterid = ?");
            ps.setInt(1, id);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int getStLogid(final int id) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select stid from stlog Where characterid = " + id);
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("stid");
                    }
                    catch (SQLException ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("stid");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setStLog(final int stid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into stlog (characterid, stid) values (?,?)");
            ps.setInt(1, this.id);
            ps.setInt(2, stid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    
    public void setStjfLog(final int id, final int stid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into stjflog (characterid, stjf) values (?,?)");
            ps.setInt(1, id);
            ps.setInt(2, stid);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    
    public void updateStjfLog(final int id, final int stid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update stjflog SET stjf = ? WHERE characterid = ?");
            ps.setInt(1, stid);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    
    public int getStjf(final int id) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select stjf from stjflog Where characterid = " + id);
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("stjf");
                    }
                    catch (SQLException ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("stjf");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public int getVip() {
        return this.vip;
    }
    
    public void setVip(final int r) {
        this.vip = r;
    }
    
    public boolean isVip() {
        return this.getVip() > 0;
    }
    
    public int getOfflinePoints(final MapleCharacter victim) {
        return this.getPoints(victim);
    }
    
    public int getPoints() {
        return this.getPoints(this);
    }
    
    public int getPoints(MapleCharacter chr) {
        final int maxtimes = 10;
        int nowtime = 0;
        final int delay = 500;
        boolean error = false;
        int x = 0;
        do {
            ++nowtime;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery("Select points from Accounts Where id = " + chr.getClient().getAccID());
                while (rs.next()) {
                    int debug = -1;
                    try {
                        debug = rs.getInt("points");
                    }
                    catch (SQLException ex2) {}
                    if (debug != -1) {
                        x = rs.getInt("points");
                        error = false;
                    }
                    else {
                        error = true;
                    }
                }
                rs.close();
            }
            catch (SQLException SQL) {
                System.err.println("[getPoints]無法連接資料庫");
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)SQL);
            }
            catch (Exception ex) {
                System.err.println("[getPoints]" + (Object)ex);
                FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
            }
            if (error) {
                try {
                    Thread.sleep((long)delay);
                }
                catch (Exception ex) {
                    FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
                }
            }
        } while (error && nowtime < maxtimes);
        return x;
    }
    
    public void setPoints(final int x) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update Accounts set points = ? Where id = ?");
            ps.setInt(1, x);
            ps.setInt(2, this.getClient().getAccID());
            ps.execute();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("[Points]無法連接資料庫");
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex);
        }
        catch (Exception ex2) {
            System.err.println("[setPoints]" + (Object)ex2);
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)ex2);
        }
    }
    
    public void gainVip() {
        final int rmb = this.getMoneyAll();
        if (rmb >= 2000 && rmb < 4000) {
            this.setVip(1);
        }
        else if (rmb >= 4000 && rmb < 6000) {
            this.setVip(2);
        }
        else if (rmb >= 6000 && rmb < 8000) {
            this.setVip(3);
        }
        else if (rmb >= 8000 && rmb < 10000) {
            this.setVip(4);
        }
        else if (rmb >= 10000) {
            this.setVip(5);
        }
    }
    
    public int getMoneyAll() {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            int money = 0;
            final PreparedStatement ps = con.prepareStatement("SELECT amount FROM donate WHERE username = ?");
            ps.setString(1, this.getClient().getAccountName());
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                money += rs.getInt("amount");
            }
            rs.close();
            ps.close();
            return money;
        }
        catch (SQLException e) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)e);
            return -1;
        }
    }
    public int getMoneyAll(int type) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            int var19;
            try {
                int money = 0;
                PreparedStatement ps;
                ResultSet rs;
                if (type != 1) {
                    ps = con.prepareStatement("SELECT moneyb FROM accounts WHERE id = ?");
                    ps.setInt(1, this.getClient().getAccID());
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        money = rs.getInt(1);
                    }

                    rs.close();
                    ps.close();
                } else {
                    ps = con.prepareStatement("SELECT amount FROM donate WHERE username = ?");
                    ps.setString(1, this.getClient().getAccountName());

                    for(rs = ps.executeQuery(); rs.next(); money += rs.getInt("amount")) {
                    }

                    rs.close();
                    ps.close();
                }

                var19 = money;
            } catch (Throwable var16) {
                var3 = var16;
                throw var16;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var15) {
                            var3.addSuppressed(var15);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var19;
        } catch (SQLException var18) {
            FileoutputUtil.outError("logs/资料库异常.txt", var18);
            return -1;
        }
    }



    public void setMoneyAll(int amount, String reason) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("Update Accounts set moneyb = moneyb + ? Where id = ?");
                ps.setInt(1, amount);
                ps.setInt(2, this.getClient().getAccID());
                ps.execute();
                ps = con.prepareStatement("insert into donate (username, amount, paymentMethod, date) values (?,?,?,?)");
                ps.setString(1, this.getClient().getAccountName());
                ps.setString(2, String.valueOf(amount));
                ps.setString(3, reason);
                ps.setString(4, FileoutputUtil.NowTime());
                ps.executeUpdate();
                ps.close();
            } catch (Throwable var14) {
                var4 = var14;
                throw var14;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var13) {
                            var4.addSuppressed(var13);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var16) {
            FileoutputUtil.outError("logs/资料库异常.txt", var16);
        }

    }

    public void setBuLingZanZu(final int bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into donate (username, amount, paymentMethod, date) values (?,?,?,?)");
            ps.setString(1, this.getClient().getAccountName());
            ps.setString(2, String.valueOf(bossid));
            ps.setString(3, "補領贊助");
            ps.setString(4, FileoutputUtil.NowTime());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Wx) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Wx);
        }
    }
    
    public void isSquadPlayerID() {
        if (this.getMapId() == 280030000) {
            final EventManager em = this.getClient().getChannelServer().getEventSM().getEventManager("ZakumBattle");
            final EventInstanceManager eim = em.getInstance("ZakumBattle");
            final String propsa = eim.getProperty("isSquadPlayerID_" + this.getId());
            if (eim != null && propsa != null && propsa.equals((Object)"0")) {
                eim.setProperty("isSquadPlayerID_" + this.getId(), "1");
            }
        }
        if (this.getMapId() == 551030200) {
            final EventManager em = this.getClient().getChannelServer().getEventSM().getEventManager("ScarTarBattle");
            final EventInstanceManager eim = em.getInstance("ScarTarBattle");
            final String propsa = eim.getProperty("isSquadPlayerID_" + this.getId());
            if (eim != null && propsa != null && propsa.equals((Object)"0")) {
                eim.setProperty("isSquadPlayerID_" + this.getId(), "1");
            }
        }
    }
    
    public void setCsMod(final int mod) {
        this.CsMod = mod;
    }
    
    public int getCsMod() {
        return this.CsMod;
    }
    
    public void setFxName(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("insert into fxlog (bossid, characterid) values (?,?)");
            ps.setString(1, bossid);
            ps.setInt(2, this.id);
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    
    public int getFxName(final String bossid) {
        try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection()) {
            final PreparedStatement ps = con.prepareStatement("select count(*) from fxlog where bossid = ?");
            ps.setString(1, bossid);
            int ret_count;
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }
                else {
                    ret_count = 0;
                }
            }
            ps.close();
            return ret_count;
        }
        catch (SQLException Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return -1;
        }
    }
    
    public int[] getSavedFaces() {
        return this.savedFaces;
    }
    
    public void setSavedFace(final int sel, final int id) {
        this.savedFaces[sel] = id;
    }
    
    public int getSavedFace(final int sel) {
        if (sel < this.savedFaces.length) {
            return this.savedFaces[sel];
        }
        return -1;
    }
    
    public int[] getSavedHairs() {
        return this.savedHairs;
    }
    
    public void setSavedHair(final int sel, final int id) {
        this.savedHairs[sel] = id;
    }
    
    public int getSavedHair(final int sel) {
        if (sel < this.savedHairs.length) {
            return this.savedHairs[sel];
        }
        return -1;
    }
    
    public void maxSkillsByJob() {
        for (final ISkill skil : SkillFactory.getAllSkills()) {
            if (skil.canBeLearnedBy((int)this.job) && skil.getId() >= 1000000) {
                this.changeSkillLevel(skil, skil.getMaxLevel(), skil.getMaxLevel());
            }
        }
    }
    
    public String getServerName() {
        return ServerConfig.SERVERNAME;
    }
    
    public int getDY() {
        return this.maplepoints;
    }
    
    public void setDY(final int set) {
        this.maplepoints = set;
    }
    
    public void gainDY(final int gain) {
        this.maplepoints += gain;
    }
    
    public int getItemQuantity(final int itemid) {
        final MapleInventoryType type = GameConstants.getInventoryType(itemid);
        return this.getInventory(type).countById(itemid);
    }
    
    public int getNX() {
        return this.getCSPoints(1);
    }
    
    public int getGamePoints() {
        try {
            int gamePoints = 0;
            Connection con = DatabaseConnection.getConnection();
            try (final PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, this.getClient().getAccID());
                ps.setInt(2, (int)this.getWorld());
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gamePoints = rs.getInt("gamePoints");
                        final Timestamp updateTime = rs.getTimestamp("updateTime");
                        final Calendar sqlcal = Calendar.getInstance();
                        if (updateTime != null) {
                            sqlcal.setTimeInMillis(updateTime.getTime());
                        }
                        if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                            gamePoints = 0;
                            try (final PreparedStatement psu = con.prepareStatement("UPDATE accounts_info SET gamePoints = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                                psu.setInt(1, this.getClient().getAccID());
                                psu.setInt(2, (int)this.getWorld());
                                psu.executeUpdate();
                                psu.close();
                            }
                        }
                    }
                    else {
                        try (final PreparedStatement psu2 = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, gamePoints) VALUES (?, ?, ?)")) {
                            psu2.setInt(1, this.getClient().getAccID());
                            psu2.setInt(2, (int)this.getWorld());
                            psu2.setInt(3, 0);
                            psu2.executeUpdate();
                            psu2.close();
                        }
                    }
                    rs.close();
                }
                ps.close();
            }
            return gamePoints;
        }
        catch (SQLException Ex) {
            System.err.println("获取角色帐号的在线时间点出现错误 - 数据库查询失败" + (Object)Ex);
            return -1;
        }
    }
    
    public int getGamePointsPD() {
        try {
            int gamePointsPD = 0;
            Connection con = DatabaseConnection.getConnection();
            try (final PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, this.getClient().getAccID());
                ps.setInt(2, (int)this.getWorld());
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gamePointsPD = rs.getInt("gamePointspd");
                        final Timestamp updateTime = rs.getTimestamp("updateTime");
                        final Calendar sqlcal = Calendar.getInstance();
                        if (updateTime != null) {
                            sqlcal.setTimeInMillis(updateTime.getTime());
                        }
                        if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                            gamePointsPD = 0;
                            try (final PreparedStatement psu = con.prepareStatement("UPDATE accounts_info SET gamePointspd = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                                psu.setInt(1, this.getClient().getAccID());
                                psu.setInt(2, (int)this.getWorld());
                                psu.executeUpdate();
                                psu.close();
                            }
                        }
                    }
                    else {
                        try (final PreparedStatement psu2 = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, gamePointspd) VALUES (?, ?, ?)")) {
                            psu2.setInt(1, this.getClient().getAccID());
                            psu2.setInt(2, (int)this.getWorld());
                            psu2.setInt(3, 0);
                            psu2.executeUpdate();
                            psu2.close();
                        }
                    }
                    rs.close();
                }
                ps.close();
            }
            return gamePointsPD;
        }
        catch (SQLException Ex) {
            System.err.println("获取角色帐号的在线时间点出现错误 - 数据库查询失败2" + (Object)Ex);
            return -1;
        }
    }
    
    public int get在线时间() {
        return this.在线时间;
    }
    
    public void set在线时间(final int 在线时间) {
        this.在线时间 = 在线时间;
    }
    
    public void gainGamePoints(final int amount) {
        this.set在线时间(this.在线时间 + amount);
        this.updateGamePoints(this.get在线时间());
    }
    
    public void gainGamePointsPD(final int amount) {
        final int gamePointsPD = this.getGamePointsPD() + amount;
        this.updateGamePointsPD(gamePointsPD);
    }
    
    public void resetGamePointsPD() {
        this.updateGamePointsPD(0);
    }
    
    public void updateGamePointsPD(final int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePointspd = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, amount);
                ps.setInt(2, this.getClient().getAccID());
                ps.setInt(3, (int)this.getWorld());
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (SQLException Ex) {
            System.err.println("更新角色帐号的在线时间出现错误 - 数据库更新失败." + (Object)Ex);
        }
    }
    
    public void updateGamePoints(final int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePoints = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, amount);
                ps.setInt(2, this.getClient().getAccID());
                ps.setInt(3, (int)this.getWorld());
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (SQLException Ex) {
            System.err.println("更新角色帐号的在线时间出现错误 - 数据库更新失败." + (Object)Ex);
        }
    }
    
    public int getGamePointsRQ() {
        try {
            int gamePointsRQ = 0;
            Connection con = DatabaseConnection.getConnection();
            try (final PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?")) {
                ps.setInt(1, this.getClient().getAccID());
                ps.setInt(2, (int)this.getWorld());
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gamePointsRQ = rs.getInt("gamePointsrq");
                        final Timestamp updateTime = rs.getTimestamp("updateTime");
                        final Calendar sqlcal = Calendar.getInstance();
                        if (updateTime != null) {
                            sqlcal.setTimeInMillis(updateTime.getTime());
                        }
                        if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                            gamePointsRQ = 0;
                            try (final PreparedStatement psu = con.prepareStatement("UPDATE accounts_info SET gamePointsrq = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?")) {
                                psu.setInt(1, this.getClient().getAccID());
                                psu.setInt(2, (int)this.getWorld());
                                psu.executeUpdate();
                            }
                        }
                    }
                    else {
                        try (final PreparedStatement psu2 = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, gamePointsrq) VALUES (?, ?, ?)")) {
                            psu2.setInt(1, this.getClient().getAccID());
                            psu2.setInt(2, (int)this.getWorld());
                            psu2.setInt(3, 0);
                            psu2.executeUpdate();
                        }
                    }
                }
            }
            return gamePointsRQ;
        }
        catch (SQLException Ex) {
            System.err.println("获取角色帐号的在线时间点出现错误 - 数据库查询失败3" + (Object)Ex);
            return -1;
        }
    }
    
    public void gainGamePointsRQ(final int amount) {
        final int gamePointsRQ = this.getGamePointsRQ() + amount;
        this.updateGamePointsRQ(gamePointsRQ);
    }
    
    public void resetGamePointsRQ() {
        this.updateGamePointsRQ(0);
    }
    
    public void updateGamePointsRQ(final int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePointsrq = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?");
            ps.setInt(1, amount);
            ps.setInt(2, this.getClient().getAccID());
            ps.setInt(3, (int)this.getWorld());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("更新角色帐号的在线时间出现错误 - 数据库更新失败." + (Object)Ex);
        }
    }
    
    public int getGamePointsPS() {
        try {
            int gamePointsRQ = 0;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_info WHERE accId = ? AND worldId = ?");
            ps.setInt(1, this.getClient().getAccID());
            ps.setInt(2, (int)this.getWorld());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                gamePointsRQ = rs.getInt("gamePointsps");
                final Timestamp updateTime = rs.getTimestamp("updateTime");
                final Calendar sqlcal = Calendar.getInstance();
                if (updateTime != null) {
                    sqlcal.setTimeInMillis(updateTime.getTime());
                }
                if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                    gamePointsRQ = 0;
                    final PreparedStatement psu = con.prepareStatement("UPDATE accounts_info SET gamePointsps = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?");
                    psu.setInt(1, this.getClient().getAccID());
                    psu.setInt(2, (int)this.getWorld());
                    psu.executeUpdate();
                    psu.close();
                }
            }
            else {
                final PreparedStatement psu2 = con.prepareStatement("INSERT INTO accounts_info (accId, worldId, gamePointsps) VALUES (?, ?, ?)");
                psu2.setInt(1, this.getClient().getAccID());
                psu2.setInt(2, (int)this.getWorld());
                psu2.setInt(3, 0);
                psu2.executeUpdate();
                psu2.close();
            }
            rs.close();
            ps.close();
            return gamePointsRQ;
        }
        catch (SQLException Ex) {
            System.err.println("获取角色帐号的跑商数据出现错误 - 数据库查询失败" + (Object)Ex);
            return -1;
        }
    }
    
    public void gainGamePointsPS(final int amount) {
        final int gamePointsPS = this.getGamePointsPS() + amount;
        this.updateGamePointsPS(gamePointsPS);
    }
    
    public void resetGamePointsPS() {
        this.updateGamePointsPS(0);
    }
    
    public void updateGamePointsPS(final int amount) {
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePointsps = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND worldId = ?");
            ps.setInt(1, amount);
            ps.setInt(2, this.getClient().getAccID());
            ps.setInt(3, (int)this.getWorld());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("更新" + this.name + "帐号的跑商数据出现错误 - 数据库更新失败." + (Object)Ex);
        }
    }
    
    public void Gaincharactera(final String Name, final int Channale, final int Piot) {
        try {
            int ret = this.Getcharactera(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;
                try {
                    ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO charactera (channel, Name,Point) VALUES (?, ?, ?)");
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
            ret += Piot;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps2 = con.prepareStatement("UPDATE charactera SET `Point` = ? WHERE Name = ? and channel = ?");
            ps2.setInt(1, ret);
            ps2.setString(2, Name);
            ps2.setInt(3, Channale);
            ps2.execute();
            ps2.close();
        }
        catch (SQLException sql) {
            System.err.println("Getcharactera!!55" + (Object)sql);
        }
    }
    
    public int Getcharactera(final String Name, final int Channale) {
        int ret = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM charactera WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            final ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
        }
        catch (SQLException ex) {}
        return ret;
    }
    
    public void Gainpersonal(final String Name, final int Channale, final int Piot) {
        try {
            int ret = this.Getpersonal(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;
                try {
                    ps = DatabaseConnection.getConnection().prepareStatement("INSERT INTO personal (channel, Name,Point) VALUES (?, ?, ?)");
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
            ret += Piot;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps2 = con.prepareStatement("UPDATE personal SET `Point` = ? WHERE Name = ? and channel = ?");
            ps2.setInt(1, ret);
            ps2.setString(2, Name);
            ps2.setInt(3, Channale);
            ps2.execute();
            ps2.close();
        }
        catch (SQLException sql) {
            System.err.println("personal!!55" + (Object)sql);
        }
    }
    
    public int Getpersonal(final String Name, final int Channale) {
        int ret = -1;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM personal WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            final ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            ps.close();
        }
        catch (SQLException ex) {}
        return ret;
    }
    
    public int getzs() {
        return this.zs;
    }
    
    public void gainzs(final int gain) {
        this.zs += gain;
    }
    
    public void setzs(final int set) {
        this.zs = set;
    }
    
    public int getjf() {
        return this.jf;
    }
    
    public void gainjf(final int gain) {
        this.jf += gain;
    }
    
    public void setjf(final int set) {
        this.jf = set;
    }
    
    public int getzdjf() {
        return this.zdjf;
    }
    
    public void gainzdjf(final int gain) {
        this.zdjf += gain;
    }
    
    public void setzdjf(final int set) {
        this.zdjf = set;
    }
    
    public int getrwjf() {
        return this.rwjf;
    }
    
    public void gainrwjf(final int gain) {
        this.rwjf += gain;
    }
    
    public void setrwjf(final int set) {
        this.rwjf = set;
    }
    
    public int getcz() {
        return this.cz;
    }
    
    public void gaincz(final int gain) {
        this.cz += gain;
    }
    
    public void setcz(final int set) {
        this.cz = set;
    }
    
    public int getdy() {
        return this.dy;
    }
    
    public void gaindy(final int gain) {
        this.dy += gain;
    }
    
    public void setdy(final int set) {
        this.dy = set;
    }
    
    public int getrmb() {
        return this.rmb;
    }
    
    public void gainrmb(final int gain) {
        this.rmb += gain;
    }
    
    public void setrmb(final int set) {
        this.rmb = set;
    }
    
    public int getyb() {
        return this.yb;
    }
    
    public void gainyb(final int gain) {
        this.yb += gain;
    }
    
    public void setyb(final int set) {
        this.yb = set;
    }
    
    public int getplayerPoints() {
        return this.playerPoints;
    }
    
    public void gainplayerPoints(final int gain) {
        this.playerPoints += gain;
    }
    
    public void setplayerPoints(final int set) {
        this.playerPoints = set;
    }
    
    public int getplayerEnergy() {
        return this.playerEnergy;
    }
    
    public void gainplayerEnergy(final int gain) {
        this.playerEnergy += gain;
    }
    
    public void setplayerEnergy(final int set) {
        this.playerEnergy = set;
    }
    
    public int getjf1() {
        return this.jf1;
    }
    
    public void gainjf1(final int gain) {
        this.jf1 += gain;
    }
    
    public void setjf1(final int set) {
        this.jf1 = set;
    }
    
    public int getjf2() {
        return this.jf2;
    }
    
    public void gainjf2(final int gain) {
        this.jf2 += gain;
    }
    
    public void setjf2(final int set) {
        this.jf2 = set;
    }
    
    public int getjf3() {
        return this.jf3;
    }
    
    public void gainjf3(final int gain) {
        this.jf3 += gain;
    }
    
    public void setjf3(final int set) {
        this.jf3 = set;
    }
    
    public int getjf4() {
        return this.jf4;
    }
    
    public void gainjf4(final int gain) {
        this.jf4 += gain;
    }
    
    public void setjf4(final int set) {
        this.jf4 = set;
    }
    
    public int getjf5() {
        return this.jf5;
    }
    
    public void gainjf5(final int gain) {
        this.jf5 += gain;
    }
    
    public void setjf5(final int set) {
        this.jf5 = set;
    }
    
    public int getjf6() {
        return this.jf6;
    }
    
    public void gainjf6(final int gain) {
        this.jf6 += gain;
    }
    
    public void setjf6(final int set) {
        this.jf6 = set;
    }
    
    public int getjf7() {
        return this.jf7;
    }
    
    public void gainjf7(final int gain) {
        this.jf7 += gain;
    }
    
    public void setjf7(final int set) {
        this.jf7 = set;
    }
    
    public int getjf8() {
        return this.jf8;
    }
    
    public void gainjf8(final int gain) {
        this.jf8 += gain;
    }
    
    public void setjf8(final int set) {
        this.jf8 = set;
    }
    
    public int getjf9() {
        return this.jf9;
    }
    
    public void gainjf9(final int gain) {
        this.jf9 += gain;
    }
    
    public void setjf9(final int set) {
        this.jf9 = set;
    }
    
    public int getjf10() {
        return this.jf10;
    }
    
    public void gainjf10(final int gain) {
        this.jf10 += gain;
    }
    
    public void setjf10(final int set) {
        this.jf10 = set;
    }
    
    public void 打开奖励() {
        NPCScriptManager.getInstance().start(this.client, 9000011, "5");
    }
    
    public void 击杀野外BOSS特效2() {
        this.map.broadcastMessage(MaplePacketCreator.environmentChange("dojang/end/clear", 3));
    }
    
    public void 击杀野外BOSS特效() {
        this.map.broadcastMessage(MaplePacketCreator.environmentChange("dojang/end/clear", 3));
    }
    
    public void 时间到() {
        this.map.broadcastMessage(MaplePacketCreator.environmentChange("summerboating/timeout", 3));
    }
    

    
    public int getTotalRMB() {
        int rmb = 0;
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT SUM(rmb) FROM paylog WHERE account = ?");
            ps.setString(1, this.getClient().getAccountName());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rmb = rs.getInt(1);
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取账号充值总数失败." + (Object)Ex);
        }
        return rmb;
    }
    
    public int getRMB() {
        int point = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT rmb FROM accounts WHERE name = ?");
            ps.setString(1, this.getClient().getAccountName());
            rs = ps.executeQuery();
            if (rs.next()) {
                point = rs.getInt("rmb");
            }
            ps.close();
            rs.close();
        }
        catch (SQLException ex) {
            System.err.println("获取角色rmb失败。" + (Object)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            }
            catch (SQLException ex2) {
                System.err.println("获取角色rmb失败。" + (Object)ex2);
            }
        }
        return point;
    }
    
    public void setRMB(final int point) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE accounts SET rmb = ? WHERE name = ?");
            ps.setInt(1, point);
            ps.setString(2, this.getClient().getAccountName());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("获取角色rmb失败。" + (Object)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                System.err.println("获取角色rmb失败。" + (Object)ex2);
            }
        }
    }

        public int getjifen() {
        int point = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT jbjf FROM accounts WHERE name = ?");
            ps.setString(1, this.getClient().getAccountName());
            rs = ps.executeQuery();
            if (rs.next()) {
                point = rs.getInt("jbjf");
            }
            ps.close();
            rs.close();
        }
        catch (SQLException ex) {
            System.err.println("获取角色金币积分失败。" + (Object)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
                if (rs != null) {
                    rs.close();
                }
            }
            catch (SQLException ex2) {
                System.err.println("获取角色金币积分失败。" + (Object)ex2);
            }
        }
        return point;
    }
    
    public void setjifen(final int point) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE accounts SET jbjf = ? WHERE name = ?");
            ps.setInt(1, point);
            ps.setString(2, this.getClient().getAccountName());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("获取角色金币积分失败。" + (Object)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                System.err.println("获取角色金币积分失败。" + (Object)ex2);
            }
        }
    }
    
    public void gainRMB(final int point) {
        PreparedStatement ps = null;
        try {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("UPDATE accounts SET rmb = rmb + ? WHERE name = ?");
            ps.setInt(1, point);
            ps.setString(2, this.getClient().getAccountName());
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("获取角色rmb失败。" + (Object)ex);
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex2) {
                System.err.println("获取角色rmb失败。" + (Object)ex2);
            }
        }
    }
    
    public int getPQLog(final String pqName) {
        return this.getPQLog(pqName, 0);
    }
    
    public int getPQLog(final String pqName, final int times) {
        return this.getPQLog(pqName, times, 1);
    }
    
    public int getDaysPQLog(final String pqName, final int times) {
        return this.getDaysPQLog(pqName, 0, times);
    }
    
    public int getDaysPQLog(final String pqName, final int times, final int day) {
        return this.getPQLog(pqName, times, day);
    }
    
    public int getPQLog(final String pqName, final int times, final int day) {
        try (Connection con = DatabaseConnection.getConnection()) {
            int n4 = 0;
            try (final PreparedStatement ps = con.prepareStatement("SELECT `count`,`time` FROM pqlog WHERE characterid = ? AND pqname = ?")) {
                ps.setInt(1, this.id);
                ps.setString(2, pqName);
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        n4 = rs.getInt("count");
                        final Timestamp timestamp = rs.getTimestamp("time");
                        rs.close();
                        ps.close();
                        if (times == 0) {
                            final Calendar calendar = Calendar.getInstance();
                            final Calendar calendar2 = Calendar.getInstance();
                            if (timestamp != null) {
                                calendar2.setTimeInMillis(timestamp.getTime());
                                calendar2.add(6, day);
                            }
                            int n5;
                            if (calendar.get(1) - calendar2.get(1) > 1) {
                                n5 = 0;
                            }
                            else if (calendar.get(1) - calendar2.get(1) >= 0) {
                                if (calendar.get(1) - calendar2.get(1) > 0) {
                                    calendar2.add(1, 1);
                                }
                                n5 = calendar.get(6) - calendar2.get(6);
                            }
                            else {
                                n5 = -1;
                            }
                            if (n5 >= 0) {
                                n4 = 0;
                                try (final PreparedStatement psi = con.prepareStatement("UPDATE pqlog SET count = 0, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
                                    psi.setInt(1, this.id);
                                    psi.setString(2, pqName);
                                    psi.executeUpdate();
                                }
                            }
                        }
                    }
                    else {
                        try (final PreparedStatement pss = con.prepareStatement("INSERT INTO pqlog (characterid, pqname, count, type) VALUES (?, ?, ?, ?)")) {
                            pss.setInt(1, this.id);
                            pss.setString(2, pqName);
                            pss.setInt(3, 0);
                            pss.setInt(4, times);
                            pss.executeUpdate();
                        }
                    }
                }
            }
            return n4;
        }
        catch (SQLException sQLException) {
            System.err.println("Error while get pqlog: " + (Object)sQLException);
            return -1;
        }
    }
    
    public void setPQLog(final String pqname) {
        this.setPQLog(pqname, 0);
    }
    
    public void setPQLog(final String pqname, final int type) {
        this.setPQLog(pqname, type, 1);
    }
    
    public void setPQLog(final String pqname, final int type, final int count) {
        final int times = this.getPQLog(pqname, type);
        try (Connection con = DatabaseConnection.getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE pqlog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
            ps.setInt(1, times + count);
            ps.setInt(2, type);
            ps.setInt(3, this.id);
            ps.setString(4, pqname);
            ps.executeUpdate();
        }
        catch (SQLException sQLException) {
            System.err.println("Error while set pqlog: " + (Object)sQLException);
        }
    }
    
    public void resetPQLog(final String pqname) {
        this.resetPQLog(pqname, 0);
    }
    
    public void resetPQLog(final String pqname, final int type) {
        try (Connection con = DatabaseConnection.getConnection();
             final PreparedStatement ps = con.prepareStatement("UPDATE pqlog SET count = ?, type = ?, time = CURRENT_TIMESTAMP() WHERE characterid = ? AND pqname = ?")) {
            ps.setInt(1, 0);
            ps.setInt(2, type);
            ps.setInt(3, this.id);
            ps.setString(4, pqname);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            System.err.println("Error while reset pqlog: " + (Object)e);
        }
    }
    
    public int getEventCount(final String eventId) {
        return this.getEventCount(eventId, 0);
    }
    
    public int getEventCount(final String eventId, final int type) {
        try {
            int count = 0;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM accounts_event WHERE accId = ? AND eventId = ?");
            ps.setInt(1, this.getClient().getAccID());
            ps.setString(2, eventId);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
                final Timestamp updateTime = rs.getTimestamp("updateTime");
                if (type == 0) {
                    final Calendar sqlcal = Calendar.getInstance();
                    if (updateTime != null) {
                        sqlcal.setTimeInMillis(updateTime.getTime());
                    }
                    if (sqlcal.get(5) + 1 <= Calendar.getInstance().get(5) || sqlcal.get(2) + 1 <= Calendar.getInstance().get(2) || sqlcal.get(1) + 1 <= Calendar.getInstance().get(1)) {
                        count = 0;
                        final PreparedStatement psu = con.prepareStatement("UPDATE accounts_event SET count = 0, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
                        psu.setInt(1, this.getClient().getAccID());
                        psu.setString(2, eventId);
                        psu.executeUpdate();
                        psu.close();
                    }
                }
            }
            else {
                final PreparedStatement psu2 = con.prepareStatement("INSERT INTO accounts_event (accId, eventId, count, type) VALUES (?, ?, ?, ?)");
                psu2.setInt(1, this.getClient().getAccID());
                psu2.setString(2, eventId);
                psu2.setInt(3, 0);
                psu2.setInt(4, type);
                psu2.executeUpdate();
                psu2.close();
            }
            rs.close();
            ps.close();
            return count;
        }
        catch (SQLException e) {
            System.err.println("获取 EventCount 次数 " + (Object)e);
            return -1;
        }
    }
    
    public void setEventCount(final String eventId) {
        this.setEventCount(eventId, 0);
    }
    
    public void setEventCount(final String eventId, final int type) {
        this.setEventCount(eventId, type, 1);
    }
    
    public void setEventCount(final String eventId, final int type, final int count) {
        final int eventCount = this.getEventCount(eventId, type);
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts_event SET count = ?, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
            ps.setInt(1, eventCount + count);
            ps.setInt(2, type);
            ps.setInt(3, this.getClient().getAccID());
            ps.setString(4, eventId);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("增加 EventCount 次数失败 " + (Object)e);
        }
    }
    
    public void resetEventCount(final String eventId) {
        this.resetEventCount(eventId, 0);
    }
    
    public void resetEventCount(final String eventId, final int type) {
        try {
            final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accounts_event SET count = 0, type = ?, updateTime = CURRENT_TIMESTAMP() WHERE accId = ? AND eventId = ?");
            ps.setInt(1, type);
            ps.setInt(2, this.getClient().getAccID());
            ps.setString(3, eventId);
            ps.executeUpdate();
            ps.close();
        }
        catch (SQLException e) {
            System.err.println("重置 EventCount 次数失败 " + (Object)e);
        }
    }
    
    public int getHiredChannel() {
        return (this.getMap() == null) ? -1 : MapleMap.getMerchantChannel(this);
    }
    
    public List<Integer> getiApprentice(final int charaid) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            final List<Integer> charid = new ArrayList<Integer>();
            final PreparedStatement ps = con1.prepareStatement("select * from Learnteacher where worker = ? ");
            ps.setInt(1, charaid);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                charid.add(Integer.valueOf(rs.getInt("characterid")));
            }
            rs.close();
            ps.close();
            return charid;
        }
        catch (Exception Ex) {
            return null;
        }
    }
    
    public int getiLearnTeacher(final int charaid, final int tybe) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            int charid = 0;
            final PreparedStatement ps = con1.prepareStatement("select * from Learnteacher where characterid = ? ");
            ps.setInt(1, charaid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                switch (tybe) {
                    case 0: {
                        charid = rs.getInt("worker");
                        break;
                    }
                    case 1: {
                        charid = rs.getInt("masterworker");
                        break;
                    }
                }
            }
            rs.close();
            ps.close();
            return charid;
        }
        catch (Exception Ex) {
            return -1;
        }
    }
    
    public void setLearnteacher(final int workid) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con1.prepareStatement("select * from Learnteacher where  characterid = ?");
            ps.setInt(1, workid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                final PreparedStatement pss = con1.prepareStatement("update Learnteacher set masterworker=? where  characterid = ?");
                pss.setInt(1, 3);
                pss.setInt(2, workid);
                pss.execute();
                pss.close();
            }
            else {
                final PreparedStatement psu = con1.prepareStatement("insert into Learnteacher (characterid, worker,masterworker) values (?,?,?)");
                psu.setInt(1, workid);
                psu.setInt(2, 0);
                psu.setInt(3, 3);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
        }
        catch (Exception ex) {}
    }
    
    public void setApprentice(final int workid, final int apprentid) {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            String caozuo = "";
            final PreparedStatement ps = con1.prepareStatement("select * from Learnteacher where  characterid = ?");
            ps.setInt(1, apprentid);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                caozuo = "Update Learnteacher set worker = '" + workid + "' , masterworker = 1 Where characterid = '" + apprentid + "'";
                final PreparedStatement pss = con1.prepareStatement(caozuo);
                pss.execute();
                pss.close();
            }
            else {
                final PreparedStatement psu = con1.prepareStatement("insert into Learnteacher (characterid, worker,masterworker) values (?,?,1)");
                psu.setInt(1, apprentid);
                psu.setInt(2, workid);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
        }
        catch (Exception ex) {}
    }
    
    public void updateApprentice(final int workerid, final int apprentid, final int tybe) {
        try (Connection con = DatabaseConnection.getConnection()) {
            String caozuo = "";
            switch (tybe) {
                case 0: {
                    caozuo = "Update Learnteacher set worker = '" + workerid + "' , masterworker = 2 Where characterid = '" + apprentid + "'";
                    break;
                }
                case 1: {
                    caozuo = "Update Learnteacher set worker = 0 , masterworker = 0 Where characterid = '" + apprentid + "'";
                    break;
                }
                case 2: {
                    caozuo = "Update Learnteacher set worker =0 , masterworker = 0 Where characterid = '" + apprentid + "'";
                    break;
                }
            }
            final PreparedStatement ps = con.prepareStatement(caozuo);
            ps.execute();
            ps.close();
        }
        catch (Exception ex) {}
    }
    
    public String getCharaName(final int id) {
        String data = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
            ps.setInt(1, id);
            try (final ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    data = rs.getString("name");
                }
                rs.close();
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("获取角色ID取名字出错 - 数据库查询失败：" + (Object)Ex);
        }
        if (data == null) {
            data = "";
        }
        return data;
    }
    
    public int getNowdaylog() {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            int ret_count = 0;
            final PreparedStatement ps = con1.prepareStatement("select * from nowdaylog where characterid = ? ");
            ps.setInt(1, this.id);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt("Point");
            }
            else {
                ret_count = 0;
            }
            rs.close();
            ps.close();
            return ret_count;
        }
        catch (Exception Ex) {
            return -1;
        }
    }
    
    public void setNowdaylog() {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            int nownuber = 0;
            String caozuo = "";
            final PreparedStatement ps = con1.prepareStatement("select * from nowdaylog where  characterid = ?");
            ps.setInt(1, this.id);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nownuber = rs.getInt("Point") + 1;
                caozuo = "Update nowdaylog set Point = '" + nownuber + "' Where characterid = '" + this.id + "'";
                final PreparedStatement pss = con1.prepareStatement(caozuo);
                pss.execute();
                pss.close();
            }
            else {
                final PreparedStatement psu = con1.prepareStatement("insert into nowdaylog (characterid,Point) values (?,1)");
                psu.setInt(1, this.id);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
        }
        catch (Exception ex) {}
    }
    
    public void RestNowdaylog() {
        Connection con1 = DatabaseConnection.getConnection();
        try {
            String caozuo = "";
            final PreparedStatement ps = con1.prepareStatement("select * from nowdaylog where  characterid = ?");
            ps.setInt(1, this.id);
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                caozuo = "Update nowdaylog set Point = 0 Where characterid = '" + this.id + "'";
                final PreparedStatement pss = con1.prepareStatement(caozuo);
                pss.execute();
                pss.close();
            }
            rs.close();
            ps.close();
        }
        catch (Exception ex) {}
    }
    
    public static int 取限制等级() {
        int 限制等级 = 0;
        try {
            final int cid = 4001128;
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM shijiexianzhidengji WHERE huoyaotongid=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                限制等级 = rs.getInt("xianzhidengji");
            }
            limitCheck.close();
            rs.close();
        }
        catch (SQLException ex) {}
        return 限制等级;
    }

    public int 取限制等级1(final int cid) {
        try {
            int 限制等级 = LtMS.ConfigValuesMap.get("等级初始上限");
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM shijiexianzhidengji WHERE huoyaotongid=" + cid + "");

            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                限制等级 = rs.getInt("xianzhidengji");
                rs.close();
                limitCheck.close();
                return 限制等级;
            }
            else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO shijiexianzhidengji (huoyaotongid, dangqianshuliang, zongshuliang, xianzhidengji) VALUES (?, ?, ?, ?)");
                psu.setInt(1, cid);
                psu.setInt(2, 0);
                psu.setInt(3, 0);
                psu.setInt(4, 限制等级);
                psu.executeUpdate();
                psu.close();
               // return 限制等级;
            }
            rs.close();
            limitCheck.close();
            return 限制等级;
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
            return LtMS.ConfigValuesMap.get("等级初始上限");
        }
    }
    public void 经验入池(final int cid,final long exp) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM lt_jingyanchi WHERE userId=" + cid + "");
            final ResultSet rs = limitCheck.executeQuery();
            if (rs.next()) {
                final PreparedStatement psu = con.prepareStatement("update lt_jingyanchi  set exps = exps + ? where userId = ?");
                psu.setLong(1, exp);
                psu.setInt(2, cid);
                psu.executeUpdate();
                psu.close();
            } else {
                final PreparedStatement psu = con.prepareStatement("INSERT INTO lt_jingyanchi (userId, exps) VALUES (?, ?)");
                psu.setInt(1, cid);
                psu.setLong(2, exp);
                psu.executeUpdate();
                psu.close();
                // return 限制等级;
            }
            rs.close();
            limitCheck.close();
        }
        catch (Exception Ex) {
            FileoutputUtil.outError("logs/資料庫異常.txt", (Throwable)Ex);
        }
    }
    public void updateOfflineTime1() {
        try (Connection con = DatabaseConnection.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update lefttime set lefttime = ? Where accid = ?");
            ps.setLong(1, System.currentTimeMillis());
            ps.setInt(2, this.getClient().getAccID());
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            System.out.println("离线挂机异常" + (Object)Ex);
        }
    }
    
    public void updateOfflineTime() {
        try (Connection con = DatabaseConnection.getConnection()) {
            final PreparedStatement ps = con.prepareStatement("Update lefttime set lefttime = ? Where accid = ?");
            ps.setLong(1, System.currentTimeMillis());
            ps.setInt(2, this.getClient().getAccID());
            ps.executeUpdate();
            ps.close();
        }
        catch (Exception Ex) {
            System.out.println("离线挂机异常" + (Object)Ex);
        }
    }
    
    public void updateOfflineTime3(final int x, final int y, final int chairid, final long lefttime) {
        Connection con1 = null;
        try {
            con1 = DatabaseConnection.getConnection();
            final PreparedStatement ps = con1.prepareStatement("select * from lefttime where  accid = ?");
            ps.setInt(1, this.getClient().getAccID());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                final PreparedStatement pss = con1.prepareStatement("update lefttime set (charid=?,x=?,y=?,chairid=?,lefttime=?) where accid = ?");
                pss.setInt(1, this.id);
                pss.setInt(2, x);
                pss.setInt(3, y);
                pss.setInt(4, chairid);
                pss.setLong(5, lefttime);
                pss.setInt(6, this.getClient().getAccID());
                pss.execute();
                pss.close();
            }
            else {
                final PreparedStatement psu = con1.prepareStatement("insert into lefttime (accid, charid,x,y,chairid,lefttime) values (?,?,?,?,?,?)");
                psu.setInt(1, this.getClient().getAccID());
                psu.setInt(2, this.id);
                psu.setInt(3, x);
                psu.setInt(4, y);
                psu.setInt(5, chairid);
                psu.setLong(6, lefttime);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
        }
        catch (Exception Ex) {
            System.out.println("结束客户端保存离线数据错误!" + (Object)Ex);
            try {
                if (con1 != null) {
                    con1.close();
                }
            }
            catch (Exception Ex2) {
                System.out.println("结束客户端保存离线数据自动关闭数据库异常" + (Object)Ex2);
            }
        }
        finally {
            try {
                if (con1 != null) {
                    con1.close();
                }
            }
            catch (Exception Ex2) {
                System.out.println("结束客户端保存离线数据自动关闭数据库异常" + (Object)Ex2);
            }
        }
    }
    
    public void updateOfflineTime2() {
        final 离线人偶 clones = null;
        ArrayList<离线人偶> clone = new ArrayList<离线人偶>();
        this.client.getChannelServer();
        clone = ChannelServer.clones;
        for (final 离线人偶 jr : clone) {
            this.updateOfflineTime3(jr.getX(), jr.getY(), jr.getChairId(), jr.getLiftTime());
        }
    }
    
    public 离线人偶 getPlayerclones() {
        离线人偶 clones = null;
        ArrayList<离线人偶> clone = new ArrayList<离线人偶>();
        this.client.getChannelServer();
        clone = ChannelServer.clones;
        for (final 离线人偶 jr : clone) {
            if (jr.AccId == this.getClient().getAccID()) {
                clones = jr;
                break;
            }
        }
        return clones;
    }
    
    public int getLastOfflineTime2() {
        Connection conn = null;
        int retnumber = -1;
        try {
            conn = DatabaseConnection.getConnection();
            final PreparedStatement ps = conn.prepareStatement("TRUNCATE TABLE lefttime");
            ps.executeUpdate();
            this.client.getChannelServer();
            final ArrayList<离线人偶> clone = ChannelServer.clones;
            for (final 离线人偶 jr : clone) {
                final PreparedStatement psu = conn.prepareStatement("insert into lefttime (accid,charid,x,y,chairid,lefttime,channel) values (?,?,?,?,?,?,?)");
                psu.setInt(1, jr.AccId);
                psu.setInt(2, jr.charId);
                psu.setInt(3, jr.x);
                psu.setInt(4, jr.y);
                psu.setInt(5, jr.chairId);
                psu.setLong(6, jr.liftTime);
                psu.setInt(7, jr.channel);
                psu.executeUpdate();
                psu.close();
            }
            ps.close();
            conn.close();
            retnumber = 1;
        }
        catch (Exception Ex) {
            System.out.println("离线挂机数据保存异常" + (Object)Ex);
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception Ex2) {
                System.out.println("结束客户端保存离线数据自动关闭数据库异常" + (Object)Ex2);
            }
        }
        finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            }
            catch (Exception Ex2) {
                System.out.println("结束客户端保存离线数据自动关闭数据库异常" + (Object)Ex2);
            }
        }
        return retnumber;
    }
    
    public long getLastOfflineTime() {
        long 离线时间 = 0L;
        try {
            Connection con1 = DatabaseConnection.getConnection();
            final PreparedStatement ps = con1.prepareStatement("select lefttime from lefttime WHERE accid = ?");
            ps.setInt(1, this.getClient().getAccID());
            final ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                离线时间 = rs.getLong("lefttime");
            }
            else {
                离线时间 = System.currentTimeMillis();
                final PreparedStatement psu = con1.prepareStatement("insert into lefttime (accid, lefttime) values (?,?)");
                psu.setInt(1, this.getClient().getAccID());
                psu.setLong(2, System.currentTimeMillis());
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();
            return 离线时间;
        }
        catch (Exception Ex) {
            System.out.println("离线挂机异常" + (Object)Ex);
            return -1L;
        }
    }
    
    static {
        MapleCharacter.ariantroomleader = new String[3];
        MapleCharacter.ariantroomslot = new int[3];
        MapleCharacter.记录当前血量 = 0;
        MapleCharacter.角色无敌指数 = 0;
        MapleCharacter.地图记录 = 0;
        MapleCharacter.记录范围 = 0L;
        MapleCharacter.记录技能 = 0;
        MapleCharacter.技能检测惩罚 = 0;
        MapleCharacter.登陆验证 = 0;
    }
    
    public enum FameStatus
    {
        OK, 
        NOT_TODAY, 
        NOT_THIS_MONTH;
    }
    
    public boolean isReincarnationMob() {
        for (MapleMonster monster : getMap().getAllMonster()) {
            if (monster.getId() == 9300329 && monster.getSpawnChrid() != this.getId()) { // 輪迴 mob id 轮回石碑
                return true;
            }
        }
        return false;
    }


    public int getCombat() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinStat1(equip);
                } else {
                    Num += this.RuinStat(equip);
                }
            }
        }
        return Num;
    }

    public int getComStr() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinStr(equip) * 10;
                } else {
                    Num += this.RuinStr(equip);
                }
            }
        }
        return Num;
    }

    public int getComDex() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinDex(equip) * 10;
                } else {
                    Num += this.RuinDex(equip);
                }
            }
        }
        return Num;
    }

    public int getComInt() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinInt(equip) * 10;
                } else {
                    Num += this.RuinInt(equip);
                }
            }
        }
        return Num;
    }

    public int getComLuk() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinLuk(equip) * 10;
                } else {
                    Num += this.RuinLuk(equip);
                }
            }
        }
        return Num;
    }

    public int getComWatk() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinWatk(equip) * 50;
                } else {
                    Num += this.RuinWatk(equip);
                }
            }
        }
        return Num;
    }

    public int getComMatk() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinMatk(equip) * 10;
                } else {
                    Num += this.RuinMatk(equip);
                }
            }
        }
        return Num;
    }

    public int getComMdef() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinMdef(equip) * 10;
                } else {
                    Num += this.RuinMdef(equip);
                }
            }
        }
        return Num;
    }

    public int getComWdef() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinWdef(equip) * 10;
                } else {
                    Num += this.RuinWdef(equip);
                }
            }
        }
        return Num;
    }

    public int getComHp() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinHp(equip) * 10;
                } else {
                    Num += this.RuinHp(equip);
                }
            }
        }
        return Num;
    }

    public int getComMp() {
        short[] TemporaryGroup = {-1, -2, -3, -4, -5, -6, -7, -8, -9, -10, -11, -12, -13, -15, -16, -17, -18, -19, -26, -27, -28, -29, -101, -102, -103, -104, -105, -106, -107, -108, -109, -110, -111, -112, -113, -114, -115, -116, -118, -119, -121, -127, -128};
        int Num = 0;
        for (short i = 0; i < TemporaryGroup.length; ++i) {
            IEquip equip = (IEquip) this.getInventory(MapleInventoryType.EQUIPPED).getItem(TemporaryGroup[i]);
            if (equip != null) {
                if (((Integer) LtMS.ConfigValuesMap.get("战力修正")).intValue() > 0) {
                    Num += this.RuinMp(equip) * 10;
                } else {
                    Num += this.RuinMp(equip);
                }
            }
        }
        return Num;
    }
    public int RuinStat(IEquip equip) {
        return equip.getStr() + equip.getDex() + equip.getInt() + equip.getLuk() + equip.getWatk() + equip.getMatk() + equip.getMdef() + equip.getWdef() + equip.getUpgradeSlots() + equip.getLevel();
    }
    public int RuinStat1(IEquip equip) {
        return equip.getStr() * 10 + equip.getDex() * 10 + equip.getInt() * 10 + equip.getLuk() * 10 + equip.getWatk() * 50 + equip.getMatk() * 50 + equip.getMdef() + equip.getWdef();
    }

    public int RuinStr(IEquip equip) {
        return equip.getStr();
    }

    public int RuinDex(IEquip equip) {
        return equip.getDex();
    }

    public int RuinInt(IEquip equip) {
        return equip.getInt();
    }

    public int RuinLuk(IEquip equip) {
        return equip.getLuk();
    }

    public int RuinWatk(IEquip equip) {
        return equip.getWatk();
    }

    public int RuinMatk(IEquip equip) {
        return equip.getMatk();
    }

    public int RuinMdef(IEquip equip) {
        return equip.getMdef();
    }

    public int RuinWdef(IEquip equip) {
        return equip.getWdef();
    }

    public int RuinHp(IEquip equip) {
        return equip.getHp();
    }

    public int RuinMp(IEquip equip) {
        return equip.getMp();
    }



    public int getAccountLog(String log1, int a) {
        if (a < 1) {
            return this.getAccountidBossLog(log1);
        }
        return this.getAccountidLog(log1);
    }

    public void setAccountLog(String log1, int a) {
        if (a < 1) {
            this.setAccountidBossLog(log1, 1);
        } else {
            this.setAccountidLog(log1, 1);
        }
    }

    public void setAccountLog(String log1, int a, int b) {
        if (a < 1) {
            this.setAccountidBossLog(log1, b);
        } else {
            this.setAccountidLog(log1, b);
        }
    }

    public int getAccountidBossLog(String log1) {
        int jf = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("select * from accountidbosslog where id =? and log = ?");
            ps.setInt(1, this.accountid);
            ps.setString(2, log1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                jf = rs.getInt("sz1");
            } else {
                PreparedStatement psu = con.prepareStatement("insert into accountidbosslog (id,log, sz1) VALUES (?,?, ?)");
                psu.setInt(1, this.accountid);
                psu.setString(2, log1);
                psu.setInt(3, 0);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();

        } catch (SQLException ex) {
            System.err.println("FZ3读取发生错误: " + ex);
        }
        return jf;
    }

    public void setAccountidBossLog(String log1) {
        this.setAccountidBossLog(log1, 1);
    }

    public void setAccountidBossLog(String log1, int slot) {
        int jf = this.getAccountidBossLog(log1);
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accountidbosslog SET sz1 = ? where id = ? AND log = ?");
            ps.setInt(1, jf + slot);
            ps.setInt(2, this.accountid);
            ps.setString(3, log1);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException ex) {
            System.err.println("accountidbosslog加减发生错误: " + ex);
        }
    }
    public void setPartyAccountidLog(MapleCharacter c, String log1, int slot) {
        if (c.getParty() == null || c.getParty().getMembers().size() == 1) {
            c.setAccountidLog(log1, slot);
            return;
        }
        for (MaplePartyCharacter chr : c.getParty().getMembers()) {
            MapleCharacter curChar = c.getMap().getCharacterById(chr.getId());
            if (curChar != null) {
                curChar.setAccountidLog(log1, slot);
            }
        }
    }

    public int getAccountidLog(String log1) {
        int jf = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("select * from accountidlog where id =? and log = ?");
            ps.setInt(1, this.accountid);
            ps.setString(2, log1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                jf = rs.getInt("sz1");
            } else {
                PreparedStatement psu = con.prepareStatement("insert into accountidlog (id,log, sz1) VALUES (?,?, ?)");
                psu.setInt(1, this.accountid);
                psu.setString(2, log1);
                psu.setInt(3, 0);
                psu.executeUpdate();
                psu.close();
            }
            rs.close();
            ps.close();

        } catch (SQLException ex) {
            System.err.println("FZ3读取发生错误: " + ex);
        }
        return jf;
    }

    public void setAccountidLog(String log1) {
        this.setAccountidLog(log1, 1);
    }

    public void setAccountidLog(String log1, int slot) {
        int jf = this.getAccountidLog(log1);
        try (Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps = con.prepareStatement("UPDATE accountidlog SET sz1 = ? where id = ? AND log = ?");
            ps.setInt(1, jf + slot);
            ps.setInt(2, this.accountid);
            ps.setString(3, log1);
            ps.executeUpdate();
            ps.close();

        } catch (SQLException ex) {
            System.err.println("FZ3加减发生错误: " + ex);
        }
    }


    public List<LifeMovementFragment> get吸怪Res() {
        return this.吸怪RES;
    }

    public void set吸怪Res(final List<LifeMovementFragment> 吸怪RES) {
        this.吸怪RES = 吸怪RES;
    }

    public void openSkill(final MapleCharacter victim) {
        for (final ISkill skil : SkillFactory.getAllSkills()) {
            if (victim.getSkillLevel(skil) < 1) {
                this.changeSkillLevel(skil, (byte)0, (byte)10);
            }
        }
    }

    public int getmoneyb() {
        int moneyb = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            final int cid = this.getAccountID();
            ResultSet rs;
            try (final PreparedStatement limitCheck = con.prepareStatement("SELECT * FROM accounts WHERE id=" + cid + "")) {
                rs = limitCheck.executeQuery();
                if (rs.next()) {
                    moneyb = rs.getInt("moneyb");
                }
                limitCheck.close();
            }
            rs.close();
            con.close();
        }catch (SQLException ex) {
            System.err.println("getmoneyb" + (Object)ex);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable)ex);
            ex.getStackTrace();
        }
        return moneyb;
    }
    public void setmoneyb(final int slot) {
        try (Connection con = DatabaseConnection.getConnection()) {
            final int cid = this.getAccountID();
            try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET moneyb = " + slot + " WHERE id = " + cid + "")) {
                ps.executeUpdate();
                ps.close();
            }
            con.close();
        }catch (SQLException ex) {
            System.err.println("setmoneyb" + (Object)ex);
            FileoutputUtil.outputFileError("logs/数据库异常.txt", (Throwable)ex);
            ex.getStackTrace();
        }
    }

    public void checkCopyItems() {
        final List<Integer> equipOnlyIds = new ArrayList<Integer>();
        final Map<Integer, Integer> checkItems = new HashMap<Integer, Integer>();

        for (final IItem item : this.getInventory(MapleInventoryType.EQUIP).list()) {
            final int equipOnlyId = (int)item.getEquipOnlyId();
            if (equipOnlyId > 0) {
                if (checkItems.containsKey(equipOnlyId)) {
                    if (checkItems.get(equipOnlyId) != item.getItemId()) {
                        continue;
                    }
                    equipOnlyIds.add(equipOnlyId);
                }
                else {
                    checkItems.put(equipOnlyId, item.getItemId());
                }
            }
        }
        for (final IItem item : this.getInventory(MapleInventoryType.EQUIPPED).list()) {
            final int equipOnlyId = (int)item.getEquipOnlyId();
            if (equipOnlyId > 0) {
                if (checkItems.containsKey(equipOnlyId)) {
                    if (checkItems.get(equipOnlyId) != item.getItemId()) {
                        continue;
                    }
                    equipOnlyIds.add(equipOnlyId);
                }
                else {
                    checkItems.put(equipOnlyId, item.getItemId());
                }
            }
        }
        boolean autoban = false;
        for (final Integer equipOnlyId2 : equipOnlyIds) {
            MapleInventoryManipulator.removeAllByEquipOnlyId(this.client, equipOnlyId2);
            autoban = true;
        }
        if (autoban) {
            AutobanManager.getInstance().autoban(this.client, "无理由.");
        }
        checkItems.clear();
        equipOnlyIds.clear();
    }

    public void forceUpdateItem(final IItem item) {
        this.forceUpdateItem(item, false);
    }

    public void forceUpdateItem(final IItem item, final boolean updateTick) {
        final List<ModifyInventory> mods = new LinkedList<ModifyInventory>();
        mods.add(new ModifyInventory(3, item));
        mods.add(new ModifyInventory(0, item));
        this.client.getSession().write(MaplePacketCreator.modifyInventory(updateTick, mods));
    }
   public int getAccountLog(String log1) {
       return this.getAccountidLog(log1);
   }

  public void setAccountLog(String log1) {
          this.setAccountidLog(log1, 1);
  }

    public void setAccountBossLog(String log1, int b) {
            this.setAccountidBossLog(log1, b);
    }
    public int getAccountBossLog(String log1) {
        return this.getAccountidBossLog(log1);
    }
    //回收装备
    public void recycleEquip(final MapleClient c, MapleCharacter chr) {

        try {
            if (!chr.haveItem(LtMS.ConfigValuesMap.get("VIP道具"),1) && !chr.haveItem(LtMS.ConfigValuesMap.get("VIP道具1"),1)){
                return;
            }
            if (!chr.is开启自动回收()){
                return;
            }
            if (chr.getInventory(MapleInventoryType.EQUIP).getItem((short)80) == null) {
                return;
            }
            int mesosGained = 0;
            MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
            for (int i = 25; i <= 96; i++) {
                if (chr.getInventory(MapleInventoryType.EQUIP).getItem((short)i) != null) {
                    IItem item = chr.getInventory(MapleInventoryType.EQUIP).getItem((short)i).copy();
                    if (ii.isCash(item.getItemId())) {
                        continue;
                    }
                    if(Start.NotParticipatingRecycling.contains(item.getItemId())){
                        continue;
                    }
                    MapleInventoryManipulator.removeFromSlot( c,MapleInventoryType.EQUIP,(short)i,(short)1,true);
                    int mesos = calculateMesos(item);
                    mesosGained += mesos;
                }
            }
            if (mesosGained>0){
                chr.gainMeso(mesosGained, true);
            }else{
                return;
            }
            chr.dropMessage(6, "装备回收成功，共获得 " + mesosGained + " 冒险币！");
        } catch (Exception e) {
            //e.printStackTrace();
        }

        try {
            if(chr.getMeso() >2000000000 && LtMS.ConfigValuesMap.get("金币道具自动兑换") >0 ){
                chr.gainMeso(-(LtMS.ConfigValuesMap.get("金币道具价值")), true);
                chr.gainItem(LtMS.ConfigValuesMap.get("金币道具"),1);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }  //回收装备
    public void openAutoSkillBuff(final MapleClient c, MapleCharacter chr) {

        try {

            if(LtMS.ConfigValuesMap.get("开启自动BUFF") ==0 ) {
                return;
            }
            if (!chr.haveItem(LtMS.ConfigValuesMap.get("VIP道具"),1) && !chr.haveItem(LtMS.ConfigValuesMap.get("VIP道具1"),1)){
                return;
            }
            long l = System.currentTimeMillis();
            if(l-this.buffTime < LtMS.ConfigValuesMap.get("BUFF时间间隔")) {
                return;
            }
            this.buffTime = l;
            //技能自动激活
            c.useSkill(c.getPlayer(),3121002,c.getPlayer().getSkillLevel(3121002));
            c.useSkill(c.getPlayer(),3221002,c.getPlayer().getSkillLevel(3221002));
            switch (c.getPlayer().getJob()){
                case 100:
                    c.useSkill(c.getPlayer(),1001003,c.getPlayer().getSkillLevel(1001003));
                    break;
                //英雄
                case 110:
                case 111:
                case 112:
                    c.useSkill(c.getPlayer(),1001003,c.getPlayer().getSkillLevel(1001003));
                    c.useSkill(c.getPlayer(),1121000,c.getPlayer().getSkillLevel(1121000));
                    c.useSkill(c.getPlayer(),1101004,c.getPlayer().getSkillLevel(1101004));
                    c.useSkill(c.getPlayer(),1101006,c.getPlayer().getSkillLevel(1101006));
                    //c.useSkill(c.getPlayer(),1111002,c.getPlayer().getSkillLevel(1111002));
                    c.useSkill(c.getPlayer(),1121002,c.getPlayer().getSkillLevel(1121002));
                    break;
                //圣骑士
                case 120:
                case 121:
                case 122:
                    c.useSkill(c.getPlayer(),1001003,c.getPlayer().getSkillLevel(1001003));
                    c.useSkill(c.getPlayer(),1221000,c.getPlayer().getSkillLevel(1221000));
                    c.useSkill(c.getPlayer(),1201005,c.getPlayer().getSkillLevel(1201005));
                    c.useSkill(c.getPlayer(),1221002,c.getPlayer().getSkillLevel(1221002));

                    break;
                //龙骑
                case 130:
                case 131:
                case 132:
                    c.useSkill(c.getPlayer(),1001003,c.getPlayer().getSkillLevel(1001003));
                    c.useSkill(c.getPlayer(),1301005,c.getPlayer().getSkillLevel(1301005));
                    c.useSkill(c.getPlayer(),1321000,c.getPlayer().getSkillLevel(1321000));
                    c.useSkill(c.getPlayer(),1301004,c.getPlayer().getSkillLevel(1301004));
                    c.useSkill(c.getPlayer(),1321002,c.getPlayer().getSkillLevel(1321002));
                    c.useSkill(c.getPlayer(),1301007,c.getPlayer().getSkillLevel(1301007));
                    c.useSkill(c.getPlayer(),1301006,c.getPlayer().getSkillLevel(1301006));
                    c.useSkill(c.getPlayer(),1311008,c.getPlayer().getSkillLevel(1311008));
                    c.useSkill(c.getPlayer(),1321007,c.getPlayer().getSkillLevel(1321007));

                    //法師
                case 200:
                    c.useSkill(c.getPlayer(),2001002,c.getPlayer().getSkillLevel(2001002));
                    c.useSkill(c.getPlayer(),2001003,c.getPlayer().getSkillLevel(2001003));
                    break;

                //火毒
                case 210:
                case 211:
                case 212:
                    c.useSkill(c.getPlayer(),2001002,c.getPlayer().getSkillLevel(2001002));
                    c.useSkill(c.getPlayer(),2001003,c.getPlayer().getSkillLevel(2001003));
                    c.useSkill(c.getPlayer(),2121000,c.getPlayer().getSkillLevel(2121000));
                    c.useSkill(c.getPlayer(),2101001,c.getPlayer().getSkillLevel(2101001));
                    c.useSkill(c.getPlayer(),2111005,c.getPlayer().getSkillLevel(2111005));
                    break;

                //冰雷
                case 220:
                case 221:
                case 222:
                    c.useSkill(c.getPlayer(),2001002,c.getPlayer().getSkillLevel(2001002));
                    c.useSkill(c.getPlayer(),2001003,c.getPlayer().getSkillLevel(2001003));
                    c.useSkill(c.getPlayer(),2221000,c.getPlayer().getSkillLevel(2221000));
                    c.useSkill(c.getPlayer(),2201001,c.getPlayer().getSkillLevel(2201001));
                    c.useSkill(c.getPlayer(),2211005,c.getPlayer().getSkillLevel(2211005));
                    break;
                //主教
                case 230:
                case 231:
                case 232:
                    c.useSkill(c.getPlayer(),2001002,c.getPlayer().getSkillLevel(2001002));
                    c.useSkill(c.getPlayer(),2001003,c.getPlayer().getSkillLevel(2001003));
                    c.useSkill(c.getPlayer(),2321000,c.getPlayer().getSkillLevel(2321000));
                    c.useSkill(c.getPlayer(),2311003,c.getPlayer().getSkillLevel(2311003));
                    c.useSkill(c.getPlayer(),2301003,c.getPlayer().getSkillLevel(2301003));
                    c.useSkill(c.getPlayer(),2301004,c.getPlayer().getSkillLevel(2301004));
                    break;
                //射手
                case 300:
                    c.useSkill(c.getPlayer(),3001003,c.getPlayer().getSkillLevel(3001003));
                    break;
                //神射
                case 310:
                case 311:
                case 312:
                    c.useSkill(c.getPlayer(),3001003,c.getPlayer().getSkillLevel(3001003));
                    c.useSkill(c.getPlayer(),3121000,c.getPlayer().getSkillLevel(3121000));
                    c.useSkill(c.getPlayer(),3101002,c.getPlayer().getSkillLevel(3101002));
                    c.useSkill(c.getPlayer(),3101004,c.getPlayer().getSkillLevel(3101004));
                    c.useSkill(c.getPlayer(),3121008,c.getPlayer().getSkillLevel(3121008));

                    break;
                //弩
                case 320:
                case 321:
                case 322:
                    c.useSkill(c.getPlayer(),3001003,c.getPlayer().getSkillLevel(3001003));
                    c.useSkill(c.getPlayer(),3221000,c.getPlayer().getSkillLevel(3221000));
                    c.useSkill(c.getPlayer(),3201002,c.getPlayer().getSkillLevel(3201002));
                    c.useSkill(c.getPlayer(),3201004,c.getPlayer().getSkillLevel(3201004));

                    break;
                //飞侠
                case 400:
                    break;
                //刀飞
                case 410:
                case 411:
                case 412:
                    c.useSkill(c.getPlayer(),4121000,c.getPlayer().getSkillLevel(4121000));
                    c.useSkill(c.getPlayer(),4101003,c.getPlayer().getSkillLevel(4101003));
                    c.useSkill(c.getPlayer(),4101004,c.getPlayer().getSkillLevel(4101004));
                    c.useSkill(c.getPlayer(),4111001,c.getPlayer().getSkillLevel(4111001));
                    c.useSkill(c.getPlayer(),4111002,c.getPlayer().getSkillLevel(4111002));
                    //c.useSkill(c.getPlayer(),4121006,c.getPlayer().getSkillLevel(4121006));

                    break;
                //标飞
                case 420:
                case 421:
                case 422:
                    c.useSkill(c.getPlayer(),4221000,c.getPlayer().getSkillLevel(4221000));
                    c.useSkill(c.getPlayer(),4201002,c.getPlayer().getSkillLevel(4201002));
                    c.useSkill(c.getPlayer(),4201003,c.getPlayer().getSkillLevel(4201003));
                    c.useSkill(c.getPlayer(),4211005,c.getPlayer().getSkillLevel(4211005));

                    break;
                //海盗
                case 500:
                    break;
                //拳手
                case 510:
                case 511:
                case 512:
                    c.useSkill(c.getPlayer(),5121000,c.getPlayer().getSkillLevel(5121000));
                    c.useSkill(c.getPlayer(),5101006,c.getPlayer().getSkillLevel(5101006));

                    break;
                //船长
                case 520:
                case 521:
                case 522:
                    c.useSkill(c.getPlayer(),5221000,c.getPlayer().getSkillLevel(5221000));
                    c.useSkill(c.getPlayer(),5201003,c.getPlayer().getSkillLevel(5201003));

                    break;
                //战童
                case 2000:
                    break;
                //战神
                case 2100:
                case 2110:
                case 2111:
                case 2112:
                    c.useSkill(c.getPlayer(),21001003,c.getPlayer().getSkillLevel(21001003));
                    c.useSkill(c.getPlayer(),21100005,c.getPlayer().getSkillLevel(21100005));
                    c.useSkill(c.getPlayer(),21111001,c.getPlayer().getSkillLevel(21111001));
                    c.useSkill(c.getPlayer(),21121003,c.getPlayer().getSkillLevel(21121003));
                    c.useSkill(c.getPlayer(),21121000,c.getPlayer().getSkillLevel(21121000));

                    break;
            }

        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
    public static int calculateMesos(IItem item) {
        int level = MapleItemInformationProvider.getInstance().getReqLevel( item.getItemId());
        if (level < 1) {
            level = 1;
        }

        int m = (Objects.nonNull(LtMS.ConfigValuesMap.get("装备回收金额")) ? LtMS.ConfigValuesMap.get("装备回收金额") : 1000);
        if (level < LtMS.ConfigValuesMap.get("装备回收最低等级") ) {
            m = LtMS.ConfigValuesMap.get("装备回收最低金额");
        }
        // 生成一个 20000-170000 之间的随机整数，作为金币数量
        double randomMesos = Math.floor(Math.random() * (1)) + m;//*(当前充值/1000+1)
        return (int)Math.floor(level * randomMesos); // 根据随机金币数量计算返还的金币数量
    }

    public Map<Integer, Integer> getPotentialMap() {
        return this.potentialMap;
    }
    public Map<Integer, Integer> getEquippedFuMoMap() {
        return this._equippedFuMoMap;
    }

    public Map<Integer, Integer> F() {
        return this._equippedFuMoMap;
    }

    public int getHyPay(int type) {
        int pay = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from hypay where accname = ?");
            ps.setString(1, this.getClient().getAccountName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                if (type == 1) {
                    pay = rs.getInt("pay");
                } else if (type == 2) {
                    pay = rs.getInt("payUsed");
                } else if (type == 3) {
                    pay = rs.getInt("pay") + rs.getInt("payUsed");
                } else if (type == 4) {
                    pay = rs.getInt("payReward");
                } else {
                    pay = 0;
                }
            } else {
                PreparedStatement psu = con.prepareStatement("insert into hypay (accname, pay, payUsed, payReward) VALUES (?, ?, ?, ?)");
                psu.setString(1, this.getClient().getAccountName());
                psu.setInt(2, 0);
                psu.setInt(3, 0);
                psu.setInt(4, 0);
                psu.executeUpdate();
                psu.close();
            }

            DBConPool.close(ps);
            rs.close();
        } catch (SQLException var7) {
            服务端输出信息.println_err("获充值信息发生错误: " + var7);
        }

        return pay;
    }

    public int gainHyPay(int hypay) {
        int pay = this.getHyPay(1);
        int payUsed = this.getHyPay(2);
        int payReward = this.getHyPay(4);
        if (hypay <= 0) {
            return 0;
        } else {
            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE hypay SET pay = ? ,payUsed = ? ,payReward = ? where accname = ?");
                ps.setInt(1, pay + hypay);
                ps.setInt(2, payUsed);
                ps.setInt(3, payReward);
                ps.setString(4, this.getClient().getAccountName());
                ps.executeUpdate();
                DBConPool.close(ps);
                return 1;
            } catch (SQLException var7) {
                服务端输出信息.println_err("加减充值信息发生错误: " + var7);
                return 0;
            }
        }
    }

    public int addHyPay(int hypay) {
        int pay = this.getHyPay(1);
        int payUsed = this.getHyPay(2);
        int payReward = this.getHyPay(4);
        if (hypay > pay) {
            return -1;
        } else {
            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE hypay SET pay = ? ,payUsed = ? ,payReward = ? where accname = ?");
                ps.setInt(1, pay - hypay);
                ps.setInt(2, payUsed + hypay);
                ps.setInt(3, payReward + hypay);
                ps.setString(4, this.getClient().getAccountName());
                ps.executeUpdate();
                DBConPool.close(ps);
                return 1;
            } catch (SQLException var7) {
                服务端输出信息.println_err("加减充值信息发生错误: " + var7);
                return -1;
            }
        }
    }

    public int delPayReward(int pay) {
        int payReward = this.getHyPay(4);
        if (pay <= 0) {
            return -1;
        } else if (pay > payReward) {
            return -1;
        } else {
            try {
                Connection con = DBConPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE hypay SET payReward = ? where accname = ?");
                ps.setInt(1, payReward - pay);
                ps.setString(2, this.getClient().getAccountName());
                ps.executeUpdate();
                DBConPool.close(ps);
                return 1;
            } catch (SQLException var5) {
                服务端输出信息.println_err("加减消费奖励信息发生错误: " + var5);
                return -1;
            }
        }
    }
    public void 刷新身上装备镶嵌汇总数据() {
        this._equippedFuMoMap.clear();

        try {
            Iterator var1 = this.getInventory(MapleInventoryType.EQUIPPED).list().iterator();

            while(true) {
                String mxmxdDaKongFuMo;
                do {
                    do {
                        Equip equip;
                        do {
                            if (!var1.hasNext()) {
                                return;
                            }

                            IItem item = (IItem)var1.next();
                            equip = (Equip)item;
                        } while(equip == null);

                        mxmxdDaKongFuMo = equip.getDaKongFuMo();
                    } while(mxmxdDaKongFuMo == null);
                } while(mxmxdDaKongFuMo.length() <= 0);

                String[] arr = mxmxdDaKongFuMo.split(",");
                String[] var6 = arr;
                int var7 = arr.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    String pair = var6[var8];
                    if (pair.length() != 0) {
                        String[] arr2 = pair.split(":");
                        int fumoType = Integer.parseInt(arr2[0]);
                        int fumoVal = Integer.parseInt(arr2[1]);
                        if (this._equippedFuMoMap.containsKey(fumoType)) {
                            this._equippedFuMoMap.put(fumoType, (Integer)this._equippedFuMoMap.get(fumoType) + fumoVal);
                        } else {
                            this._equippedFuMoMap.put(fumoType, fumoVal);
                        }
                    }
                }
            }
        } catch (Exception var13) {
            服务端输出信息.println_err("刷新身上装备镶嵌汇总数据出错：" + var13.getMessage());
            var13.printStackTrace();
        }
    }

    public void 刷新身上装备镶嵌汇总数据_数据库() {
        this._equippedFuMoMap.clear();
        String sqlQuery1 = "select b.mxmxd_dakong_fumo from inventoryitems a, inventoryequipment b where a.inventoryitemid = b.inventoryitemid and a.characterid = ? and a.inventorytype = -1 and b.mxmxd_dakong_fumo != '' and b.mxmxd_dakong_fumo is not NULL";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlQuery1);
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                label135:
                while(true) {
                    String mxmxdDaKongFuMo;
                    do {
                        do {
                            if (!rs.next()) {
                                break label135;
                            }

                            mxmxdDaKongFuMo = rs.getString("mxmxd_dakong_fumo");
                        } while(mxmxdDaKongFuMo == null);
                    } while(mxmxdDaKongFuMo.length() <= 0);

                    String[] arr = mxmxdDaKongFuMo.split(",");
                    String[] var8 = arr;
                    int var9 = arr.length;

                    for(int var10 = 0; var10 < var9; ++var10) {
                        String pair = var8[var10];
                        if (pair.length() != 0) {
                            String[] arr2 = pair.split(":");
                            int fumoType = Integer.parseInt(arr2[0]);
                            int fumoVal = Integer.parseInt(arr2[1]);
                            if (this._equippedFuMoMap.containsKey(fumoType)) {
                                this._equippedFuMoMap.put(fumoType, (Integer)this._equippedFuMoMap.get(fumoType) + fumoVal);
                            } else {
                                this._equippedFuMoMap.put(fumoType, fumoVal);
                            }
                        }
                    }
                }
            } catch (Throwable var23) {
                var5 = var23;
                throw var23;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var22) {
                            var5.addSuppressed(var22);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            DBConPool.close(ps);
        } catch (SQLException var25) {
            服务端输出信息.println_err("刷新身上装备镶嵌汇总数据_数据库 出错：" + var25.getMessage());
            var25.printStackTrace();
        }

    }

    public int 获取镶嵌汇总值(int fumoType) {
        return this._equippedFuMoMap.containsKey(fumoType) ? (Integer)this._equippedFuMoMap.get(fumoType) : 0;
    }
//    public void reloadPotentialMap() {
//
//
//        try {
//            Collection<IItem> list = this.getInventory(MapleInventoryType.EQUIPPED).list();
//            if(ListUtil.isEmpty(list) || list.size() == 0){
//                return;
//            }
//            this.potentialMap.clear();
//
//            list.forEach(iItem -> {
//                Equip equip = (Equip)iItem;
//                String potentials = equip.getPotentials();
//                String[] arr = potentials.split(",");
//                for (String s : arr) {
//                    String[] arr2 = s.split(":");
//                    int potentialType = Integer.parseInt(arr2[0]);
//                    int potentialVal = Integer.parseInt(arr2[1]);
//                    if (this.potentialMap.containsKey(potentialType)) {
//                        this.potentialMap.put(potentialType, (Integer)this.potentialMap.get(potentialType) + potentialVal);
//                    } else {
//                        this.potentialMap.put(potentialType, potentialVal);
//                    }
//                }
//            });
//        } catch (Exception var13) {
//            服务端输出信息.println_err("reloadPotentialMap出错：" + var13);
//            var13.printStackTrace();
//        }
//    }
    public void reloadPotentialMap() {
        this.potentialMap.clear();

        try {
            Iterator var1 = this.getInventory(MapleInventoryType.EQUIPPED).list().iterator();

            while(true) {
                String potentials;
                do {
                    do {
                        Equip equip;
                        do {
                            if (!var1.hasNext()) {
                                return;
                            }

                            IItem item = (IItem)var1.next();
                            equip = (Equip)item;
                        } while(equip == null);

                        potentials = equip.getPotentials();
                    } while(potentials == null);
                } while(potentials.length() <= 0);

                String[] arr = potentials.split(",");
                String[] var6 = arr;
                int var7 = arr.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    String pair = var6[var8];
                    if (pair.length() > 0) {
                        String[] arr2 = pair.split(":");
                        int potentialType = Integer.parseInt(arr2[0]);
                        int potentialVal = Integer.parseInt(arr2[1]);
                        if (this.potentialMap.containsKey(potentialType)) {
                            this.potentialMap.put(potentialType, (Integer)this.potentialMap.get(potentialType) + potentialVal);
                        } else {
                            this.potentialMap.put(potentialType, potentialVal);
                        }
                    }
                }
            }
        } catch (Exception var13) {
            服务端输出信息.println_err("reloadPotentialMap出错：" + var13);
            var13.printStackTrace();
        }
    }




    public void reloadPotentialMapFromDB() {
        this.potentialMap.clear();
        String sqlQuery1 = "select b.snail_potentials from inventoryitems a, inventoryequipment b where a.inventoryitemid = b.inventoryitemid and a.characterid = ? and a.inventorytype = -1 and b.snail_potentials != '' and b.snail_potentials is not NULL";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement(sqlQuery1);
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();
            Throwable var5 = null;

            try {
                label135:
                while(true) {
                    String potentials;
                    do {
                        do {
                            if (!rs.next()) {
                                break label135;
                            }

                            potentials = rs.getString("snail_potentials");
                        } while(potentials == null);
                    } while(potentials.length() <= 0);

                    String[] arr = potentials.split(",");
                    String[] var8 = arr;
                    int var9 = arr.length;

                    for(int var10 = 0; var10 < var9; ++var10) {
                        String pair = var8[var10];
                        if (pair.length() != 0) {
                            String[] arr2 = pair.split(":");
                            int potentialType = Integer.parseInt(arr2[0]);
                            int potentialVal = Integer.parseInt(arr2[1]);
                            if (this.potentialMap.containsKey(potentialType)) {
                                this.potentialMap.put(potentialType, (Integer)this.potentialMap.get(potentialType) + potentialVal);
                            } else {
                                this.potentialMap.put(potentialType, potentialVal);
                            }
                        }
                    }
                }
            } catch (Throwable var23) {
                var5 = var23;
                throw var23;
            } finally {
                if (rs != null) {
                    if (var5 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var22) {
                            var5.addSuppressed(var22);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            DBConPool.close(ps);
        } catch (SQLException var25) {
            服务端输出信息.println_err("reloadPotentialMapFromDB 出错：" + var25.getMessage());
            var25.printStackTrace();
        }

    }

    public int getPotential(int potentialType) {
        return this.potentialMap.containsKey(potentialType) ? (Integer)this.potentialMap.get(potentialType) : 0;
    }
    public void setMoney(int x) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("Update Accounts set money = ? Where id = ?");
                ps.setInt(1, x);
                ps.setInt(2, this.getClient().getAccID());
                ps.execute();
                ps.close();
            } catch (Throwable var14) {
                var3 = var14;
                throw var14;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var13) {
                            var3.addSuppressed(var13);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var16) {
            服务端输出信息.println_err("[Money]无法连接资料库");
            FileoutputUtil.outError("logs/资料库异常.txt", var16);
        } catch (Exception var17) {
            服务端输出信息.println_err("[setMoney]" + var17);
            FileoutputUtil.outError("logs/资料库异常.txt", var17);
        }

    }

    public int getGuildPoints() {
        return this.guildPoints;
    }

    public void setGuildPoints(int guildPoints) {
        this.guildPoints = guildPoints;
    }
    public void setMoneyAll(int amount) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            try {
                PreparedStatement ps = con.prepareStatement("Update Accounts set moneyb = moneyb + ? Where id = ?");
                ps.setInt(1, amount);
                ps.setInt(2, this.getClient().getAccID());
                ps.execute();
                ps = con.prepareStatement("insert into donate (username, amount, paymentMethod, date) values (?,?,?,?)");
                ps.setString(1, this.getClient().getAccountName());
                ps.setString(2, String.valueOf(amount));
                ps.setString(3, "累计赞助");
                ps.setString(4, FileoutputUtil.NowTime());
                ps.executeUpdate();
                ps.close();
            } catch (Throwable var13) {
                var3 = var13;
                throw var13;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var12) {
                            var3.addSuppressed(var12);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var15) {
            FileoutputUtil.outError("logs/资料库异常.txt", var15);
        }

    }


    public boolean 增加积分_数据库(int mount) {
        if (mount <= 0) {
            return false;
        } else {
            Connection con = DBConPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("Select * FROM snail_boss_points WHERE accountid = ?");
                ps.setInt(1, this.getAccountID());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int points = rs.getInt("points");
                    ps = con.prepareStatement("UPDATE snail_boss_points SET points = ? WHERE accountid = ?");
                    ps.setInt(1, points + mount);
                    ps.setInt(2, this.getAccountID());
                    ps.executeUpdate();
                } else {
                    ps = con.prepareStatement("INSERT INTO snail_boss_points (accountid, points) VALUES (?, ?)");
                    ps.setInt(1, this.getAccountID());
                    ps.setInt(2, mount);
                    ps.executeUpdate();
                }

                ps.close();
                rs.close();
                this.dropMessage(5, "积分数增加 " + mount);
                return true;
            } catch (SQLException var6) {
                服务端输出信息.println_err("增加积分，读取数据库错误，错误原因：" + var6);
                var6.printStackTrace();
                return false;
            }
        }
    }

    public boolean 减少积分_数据库(int mount) {
        if (mount <= 0) {
            return false;
        } else {
            Connection con = DBConPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("Select * FROM snail_boss_points WHERE accountid = ?");
                ps.setInt(1, this.getAccountID());
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    return false;
                }

                points = rs.getInt("points");
                if (points < mount) {
                    return false;
                }

                ps = con.prepareStatement("UPDATE snail_boss_points SET points = ? WHERE accountid = ?");
                ps.setInt(1, points - mount);
                ps.setInt(2, this.getAccountID());
                ps.executeUpdate();
                ps.close();
                rs.close();
                this.dropMessage(5, "积分数减少 " + mount);
            } catch (SQLException var6) {
                服务端输出信息.println_err("减少积分，读取数据库错误，错误原因：" + var6);
                var6.printStackTrace();
            }

            return true;
        }
    }

    public int 获得积分_数据库() {
        Connection con = DBConPool.getConnection();
        int point = 0;

        try {
            PreparedStatement ps = con.prepareStatement("Select * FROM snail_boss_points WHERE accountid = ?");
            ps.setInt(1, this.getAccountID());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                point = rs.getInt("points");
            }

            ps.close();
            rs.close();
        } catch (SQLException var5) {
            服务端输出信息.println_err("获得积分_数据库错误，错误原因：" + var5);
            var5.printStackTrace();
        }

        return point;
    }
    public int getCloneLookQuantity() {
        int count = 0;

        for(int i = 0; i < this.clones.length; ++i) {
            if (this.clones[i].get() != null) {
                ++count;
            }
        }

        return count;
    }

    public final void decreaseClone(int quantity) {
        try {
            int totalQuantity = this.getCloneLookQuantity();
            if (quantity > totalQuantity) {
                quantity = totalQuantity;
            }

            this.numClones = 0;

            for(int i = 0; i < this.clones.length; ++i) {
                if (this.clones[i].get() != null) {
                    this.map.removePlayer((MapleCharacter)this.clones[i].get());
                    ((MapleCharacter)this.clones[i].get()).getClient().disconnect(false, false);
                    this.clones[i] = new WeakReference((Object)null);
                    ++this.numClones;
                    if (this.numClones >= quantity) {
                        break;
                    }
                }
            }
        } catch (Exception var4) {
            服务端输出信息.println_err("【错误】：decreaseClone错误，错误原因：" + var4);
            var4.printStackTrace();
        }

    }
    public boolean isShowSkill() {
        return this.showSkill;
    }
    public void setShowSkill(boolean showSkill) {
        this.showSkill = showSkill;
    }
    public final long 判断物品数量(int itemid) {
        MapleInventoryType type = GameConstants.getInventoryType(itemid);
        return this.inventory[type.ordinal()].countById(itemid);
    }
    public boolean isDropOnMyFoot() {
        return GameConstants.isBanChannel(this.getClient().getChannel()) && !this.isGM() ? false : this.dropOnMyFoot;
    }

    public void setDropOnMyFoot(boolean dropOnMyFoot) {
        this.dropOnMyFoot = dropOnMyFoot;
    }

    public boolean isDropOnMyBag() {
        return GameConstants.isBanChannel(this.getClient().getChannel()) && !this.isGM() ? false : this.dropOnMyBag;
    }
    public void loadPackageList() {
        packageList = PackageOfEquipments.getInstance().getPackage(this);
    }
    public List<PackageOfEquipments.MyPackage> getPackageList() {
        return packageList;
    }
    public void setDropOnMyBag(boolean dropOnMyBag) {
        this.dropOnMyBag = dropOnMyBag;
    }
    public void solvePackageStats() {
        if (packageList != null) {
            this.package_str = 0;
            this.package_dex = 0;
            this.package_int = 0;
            this.package_luk = 0;
            this.package_all_ap = 0;
            this.package_watk = 0;
            this.package_matk = 0;
            this.package_wdef = 0;
            this.package_mdef = 0;
            this.package_acc = 0;
            this.package_avoid = 0;
            this.package_maxhp = 0;
            this.package_maxmp = 0;
            this.package_speed = 0;
            this.package_jump = 0;
            this.package_str_percent = 0;
            this.package_dex_percent = 0;
            this.package_int_percent = 0;
            this.package_luk_percent = 0;
            this.package_all_ap_percent = 0;
            this.package_watk_percent = 0;
            this.package_matk_percent = 0;
            this.package_wdef_percent = 0;
            this.package_mdef_percent = 0;
            this.package_acc_percent = 0;
            this.package_avoid_percent = 0;
            this.package_maxhp_percent = 0;
            this.package_maxmp_percent = 0;
            this.package_normal_damage_percent = 0;
            this.package_boss_damage_percent = 0;
            this.package_total_damage_percent = 0;
            Iterator var1 = packageList.iterator();

            while(var1.hasNext()) {
                PackageOfEquipments.MyPackage ret = (PackageOfEquipments.MyPackage)var1.next();
                if (ret != null) {
                    this.package_str += ret.getStr();
                    this.package_dex += ret.getDex();
                    this.package_int += ret.get_int();
                    this.package_luk += ret.getLuk();
                    this.package_all_ap += ret.getAll_ap();
                    this.package_watk += ret.getWatk();
                    this.package_matk += ret.getMatk();
                    this.package_wdef += ret.getWdef();
                    this.package_mdef += ret.getMdef();
                    this.package_acc += ret.getAcc();
                    this.package_avoid += ret.getAvoid();
                    this.package_maxhp += ret.getMaxhp();
                    this.package_maxmp += ret.getMaxmp();
                    this.package_speed += ret.getSpeed();
                    this.package_jump += ret.getJump();
                    this.package_str_percent += ret.getStr_percent();
                    this.package_dex_percent += ret.getDex_percent();
                    this.package_int_percent += ret.get_int_percent();
                    this.package_luk_percent += ret.getLuk_percent();
                    this.package_all_ap_percent += ret.getAll_ap_percent();
                    this.package_watk_percent += ret.getWatk_percent();
                    this.package_matk_percent += ret.getMatk_percent();
                    this.package_wdef_percent += ret.getWdef_percent();
                    this.package_mdef_percent += ret.getMdef_percent();
                    this.package_acc_percent += ret.getAcc_percent();
                    this.package_avoid_percent += ret.getAvoid_percent();
                    this.package_maxhp_percent += ret.getMaxhp_percent();
                    this.package_maxmp_percent += ret.getMaxmp_percent();
                    this.package_normal_damage_percent += ret.getNormal_damage_percent();
                    this.package_boss_damage_percent += ret.getBoss_damage_percent();
                    this.package_total_damage_percent += ret.getTotal_damage_percent();
                }
            }

        }
    }

    public int getOneTimeLogi(String log) {
        try {
            Connection con = DBConPool.getConnection();
            int ret_count = 0;
            try {
                PreparedStatement ps = con.prepareStatement("select count(*) from onetimelogi where ip = ? and log = ?");
                ps.setString(1, this.getClient().getSessionIPAddress());
                ps.setString(2, log);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ret_count = rs.getInt(1);
                }

                rs.close();
                ps.close();

            } catch (Exception ex) {
            } finally {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var16) {

                    }
                }

            }
            return ret_count;
        } catch (Exception var19) {
            FileoutputUtil.outError("logs/资料库异常.txt", var19);
            return -1;
        }
    }

    public void setOneTimeLogi(String log) {
        if (!this.isClone()) {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var3 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("insert into onetimelogi (ip, log) values (?,?)");
                    ps.setString(1, this.getClient().getSessionIPAddress());
                    ps.setString(2, log);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if (con != null) {
                        if (var3 != null) {
                            try {
                                con.close();
                            } catch (Throwable var12) {
                                var3.addSuppressed(var12);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (Exception var15) {
                FileoutputUtil.outError("logs/资料库异常.txt", var15);
            }

        }
    }


    public final void cloneLook1() {
        if (!this.clone) {
            for(int i = 0; i < this.clones.length; ++i) {
                if (this.clones[i].get() == null) {
                    MapleCharacter newp1 = this.cloneLooks1();
                    this.map.addPlayer(newp1);
                    this.map.broadcastMessage(MaplePacketCreator.updateCharLook(newp1));
                    this.map.movePlayer(newp1, this.getPosition());
                    this.clones[i] = new WeakReference<MapleCharacter>(newp1);
                    return;
                }
            }

        }
    }

    public MapleCharacter cloneLooks1() {
        MapleClient cs = new MapleClient((MapleAESOFB)null, (MapleAESOFB)null, new MockIOSession());
        int minus = this.getId() + Randomizer.nextInt(this.getId());
        MapleCharacter ret = new MapleCharacter(true);
        ret.id = minus;
        ret.client = cs;
        ret.exp = 0;
        ret.meso = 0;
        ret.beans = 0;
        ret.blood = this.blood;
        ret.month = this.month;
        ret.day = this.day;
        ret.charmessage = this.charmessage;
        ret.expression = this.expression;
        ret.constellation = this.constellation;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = this.name + "①";
        if (this.Getrobot("" + this.id + "", 11) <= 0) {
            ret.hair = this.hair;
        } else {
            ret.hair = (short)this.Getrobot("" + this.id + "", 11);
        }

        if (this.Getrobot("" + this.id + "", 12) <= 0) {
            ret.face = this.face;
        } else {
            ret.face = (short)this.Getrobot("" + this.id + "", 12);
        }

        if (this.Getrobot("" + this.id + "", 13) <= 0) {
            ret.skinColor = this.skinColor;
        } else {
            ret.skinColor = (byte)((short)this.Getrobot("" + this.id + "", 13));
        }

        if (this.Getrobot("" + this.id + "", 14) <= 0) {
            ret.level = 1;
        } else {
            ret.level = (short)this.Getrobot("" + this.id + "", 14);
        }

        ret.fame = 0;
        ret.job = this.job;
        ret.bookCover = this.bookCover;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = 0;
        ret.gender = 0;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(this.getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = this.guildid;
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll(this.effects);
        if (ret.effects.get(MapleBuffStat.ILLUSION) != null) {
            ret.effects.remove(MapleBuffStat.ILLUSION);
        }

        if (ret.effects.get(MapleBuffStat.SUMMON) != null) {
            ret.effects.remove(MapleBuffStat.SUMMON);
        }

        if (ret.effects.get(MapleBuffStat.REAPER) != null) {
            ret.effects.remove(MapleBuffStat.REAPER);
        }

        if (ret.effects.get(MapleBuffStat.PUPPET) != null) {
            ret.effects.remove(MapleBuffStat.PUPPET);
        }
        ret.max_damage = this.max_damage;
        ret.guildrank = this.guildrank;
        ret.allianceRank = this.allianceRank;
        ret.hidden = this.hidden;
        ret.setPosition(new Point(this.getPosition()));
        Iterator var4 = this.getInventory(MapleInventoryType.EQUIPPED).iterator();

        while(var4.hasNext()) {
            IItem equip = (IItem)var4.next();
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }

        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.rocks = this.rocks;
        ret.regrocks = this.regrocks;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;
        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.acash = this.acash;
        ret.lastGainHM = this.lastGainHM;
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());

        while(this.map.getCharacterById(ret.id) != null || this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null) {
            ++ret.id;
        }

        ret.client.setPlayer(ret);
        return ret;
    }

    public final void cloneLook2() {
        if (!this.clone) {
            for(int i = 0; i < this.clones.length; ++i) {
                if (this.clones[i].get() == null) {
                    MapleCharacter newp = this.cloneLooks2();
                    this.map.addPlayer(newp);
                    this.map.broadcastMessage(MaplePacketCreator.updateCharLook(newp));
                    this.map.movePlayer(newp, this.getPosition());
                    this.clones[i] = new WeakReference(newp);
                    return;
                }
            }

        }
    }

    public MapleCharacter cloneLooks2() {
        MapleClient cs = new MapleClient((MapleAESOFB)null, (MapleAESOFB)null, new MockIOSession());
        int minus = this.getId() + Randomizer.nextInt(this.getId());
        MapleCharacter ret = new MapleCharacter(true);
        ret.id = minus;
        ret.client = cs;
        ret.exp = 0;
        ret.meso = 0;
        ret.beans = 0;
        ret.blood = this.blood;
        ret.month = this.month;
        ret.day = this.day;
        ret.charmessage = this.charmessage;
        ret.expression = this.expression;
        ret.constellation = this.constellation;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = "[" + this.name + "][分身②]";
        if (this.Getrobot("" + this.id + "", 21) <= 0) {
            ret.hair = this.hair;
        } else {
            ret.hair = (short)this.Getrobot("" + this.id + "", 21);
        }

        if (this.Getrobot("" + this.id + "", 22) <= 0) {
            ret.face = this.face;
        } else {
            ret.face = (short)this.Getrobot("" + this.id + "", 22);
        }

        if (this.Getrobot("" + this.id + "", 23) <= 0) {
            ret.skinColor = this.skinColor;
        } else {
            ret.skinColor = (byte)((short)this.Getrobot("" + this.id + "", 23));
        }

        if (this.Getrobot("" + this.id + "", 24) <= 0) {
            ret.level = 1;
        } else {
            ret.level = (short)this.Getrobot("" + this.id + "", 24);
        }
        ret.max_damage = this.max_damage;
        ret.fame = this.fame;
        ret.job = this.job;
        ret.bookCover = this.bookCover;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = 0;
        ret.gender = 0;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(this.getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = this.guildid;
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll(this.effects);
        if (ret.effects.get(MapleBuffStat.ILLUSION) != null) {
            ret.effects.remove(MapleBuffStat.ILLUSION);
        }

        if (ret.effects.get(MapleBuffStat.SUMMON) != null) {
            ret.effects.remove(MapleBuffStat.SUMMON);
        }

        if (ret.effects.get(MapleBuffStat.REAPER) != null) {
            ret.effects.remove(MapleBuffStat.REAPER);
        }

        if (ret.effects.get(MapleBuffStat.PUPPET) != null) {
            ret.effects.remove(MapleBuffStat.PUPPET);
        }

        ret.guildrank = this.guildrank;
        ret.allianceRank = this.allianceRank;
        ret.hidden = this.hidden;
        ret.setPosition(new Point(this.getPosition()));
        Iterator var4 = this.getInventory(MapleInventoryType.EQUIPPED).iterator();

        while(var4.hasNext()) {
            IItem equip = (IItem)var4.next();
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }

        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.rocks = this.rocks;
        ret.regrocks = this.regrocks;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;
        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.acash = this.acash;
        ret.lastGainHM = this.lastGainHM;
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());

        while(this.map.getCharacterById(ret.id) != null || this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null) {
            ++ret.id;
        }

        ret.client.setPlayer(ret);
        return ret;
    }

    public void deleteOneTimeLoga(String log) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().deleteOneTimeLogaAll(this.getAccountID(), log);
        }
    }

    public void setOneTimeLoga(String log) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setOneTimeLoga(this.getAccountID(), log);
        }
    }

    public void setOneTimeLoga(String log, int count) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setOneTimeLoga(this.getAccountID(), log, count);
        }
    }


    public void 增加伤害上限值(long inc_damage) {
        long now_damage = this.max_damage;
        if (now_damage <= 199999L) {
            now_damage = 199999L;
        }

        if (inc_damage > 0L) {
            now_damage += inc_damage;
                this.max_damage = now_damage;
           long a = Math.abs((now_damage-199999L)/10000);
           if(a>=214700){
               this.PGSXDJ = 214700;
           }else{
               this.PGSXDJ = (int) a;
           }

        }

    }

    public void 减少伤害上限值(long inc_damage) {
        long now_damage = this.max_damage;
        if (now_damage <= 199999L) {
            now_damage = 199999L;
        }

        if (inc_damage > 0L) {
            now_damage -= inc_damage;
            if (now_damage <= 199999L) {
                now_damage = 199999L;
            }

            this.max_damage = now_damage;
        }

    }

    public long 读取伤害上限值() {
        return this.max_damage;
    }

//    public void 增加经验储备(long inc_exp) {
//        long now_exp = this.exp_reserve;
//        if (now_exp <= 0L) {
//            now_exp = 0L;
//        }
//
//        if (inc_exp > 0L) {
//            now_exp += inc_exp;
//            this.exp_reserve = now_exp;
//        }
//
//    }
//
//    public void 扣除经验储备(long inc_exp) {
//        long now_exp = this.exp_reserve;
//        if (now_exp <= 199999L) {
//            now_exp = 199999L;
//        }
//
//        if (inc_exp > 0L) {
//            now_exp -= inc_exp;
//            if (now_exp <= 0L) {
//                now_exp = 0L;
//            }
//
//            this.exp_reserve = now_exp;
//        }
//
//    }

//    public long 读取经验储备() {
//        return this.exp_reserve;
//    }

    public int Getcharacterz(String Name, int Channale) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM characterz WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            DBConPool.close(ps);
        } catch (SQLException var7) {
        }

        return ret;
    }

    public void 关闭仙人模式() {
        this.dropMessage(5, "仙人模式 - 关闭");
        this.关闭仙人模式信息();
        if (this.仙人模式线程 != null) {
            this.仙人模式线程.cancel(false);
            this.仙人模式线程 = null;
        }

        if (this.仙人模式BUFF线程 != null) {
            this.仙人模式BUFF线程.cancel(false);
            this.仙人模式BUFF线程 = null;
        }

        this.保护线程();
    }

    public void 开启仙人模式信息() {
        PreparedStatement ps1 = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps1 = con.prepareStatement("SELECT * FROM jiezoudashi ");
            rs = ps1.executeQuery();
            if (rs.next()) {
                String sqlString2 = null;
                sqlString2 = "update jiezoudashi set Val= 0 where Name = '仙人模式" + this.id + "'";
                PreparedStatement dropperid = con.prepareStatement(sqlString2);
                dropperid.executeUpdate(sqlString2);
                Start.读取技个人信息设置();
            }

            DBConPool.close(ps1);
        } catch (SQLException var6) {
        }

    }

    public void 关闭仙人模式信息() {
        PreparedStatement ps1 = null;
        ResultSet rs = null;

        try {
            Connection con = DBConPool.getConnection();
            ps1 = con.prepareStatement("SELECT * FROM jiezoudashi ");
            rs = ps1.executeQuery();
            if (rs.next()) {
                String sqlString2 = null;
                sqlString2 = "update jiezoudashi set Val= 1 where Name = '仙人模式" + this.id + "'";
                PreparedStatement dropperid = con.prepareStatement(sqlString2);
                dropperid.executeUpdate(sqlString2);
                Start.读取技个人信息设置();
            }
        } catch (SQLException var6) {
        }

    }

    public void 仙人模式() {
        this.保护线程();
        if (this.仙人模式线程 == null) {
            this.开启仙人模式信息();
            this.仙人模式BUFF();
            this.dropMessage(5, "仙人模式 - 开启");
            int 契合 = (Integer)Start.个人信息设置.get("BUFF增益" + this.id + "");
            if (契合 > 50 && 契合 <= 100) {
                this.仙人模式线程 = BuffTimer.getInstance().register(new Runnable() {
                    public void run() {
                        if (MapleCharacter.this.getMp() > 0) {
                            MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.15));
                        } else {
                            MapleCharacter.this.关闭仙人模式();
                        }

                        MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.15));
                    }
                }, 5000L);
            } else if (契合 > 100 && 契合 <= 150) {
                this.仙人模式线程 = BuffTimer.getInstance().register(new Runnable() {
                    public void run() {
                        if (MapleCharacter.this.getMp() > 0) {
                            MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.15));
                        } else {
                            MapleCharacter.this.关闭仙人模式();
                        }

                        MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.15));
                    }
                }, 4000L);
            } else if (契合 > 150) {
                this.仙人模式线程 = BuffTimer.getInstance().register(new Runnable() {
                    public void run() {
                        if (MapleCharacter.this.getMp() > 0) {
                            MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.2));
                        } else {
                            MapleCharacter.this.关闭仙人模式();
                        }

                        MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.2));
                    }
                }, 4000L);
            } else {
                this.仙人模式线程 = BuffTimer.getInstance().register(new Runnable() {
                    public void run() {
                        if (MapleCharacter.this.getMp() > 0) {
                            MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.1));
                        } else {
                            MapleCharacter.this.关闭仙人模式();
                        }

                        MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.1));
                    }
                }, 5000L);
            }
        } else {
            this.仙人模式线程.cancel(false);
            this.仙人模式线程 = null;
        }

    }

    public void 仙人模式BUFF() {
        if (this.仙人模式BUFF线程 == null) {
            int 间隔 = (int)((double)((Integer)Start.个人信息设置.get("聪明睿智" + this.id + "") * (Integer)Start.个人信息设置.get("BUFF增益" + this.id + "")) * 1.0E-4);
            if (间隔 > 10000) {
                间隔 = 10000;
            }

            this.仙人模式BUFF线程 = BuffTimer.getInstance().register(new Runnable() {
                public void run() {
                    int 值 = (Integer)Start.个人信息设置.get("BUFF增益" + MapleCharacter.this.id + "");
                    int buff = 2022359;
                    double a;
                    if (值 < 90) {
                        a = Math.ceil(Math.random() * 8.0);
                        buff = (int)((double)buff + a);
                    } else if (值 >= 90 && 值 < 180) {
                        a = Math.ceil(Math.random() * 17.0);
                        buff = (int)((double)buff + a);
                    } else if (值 >= 180 && 值 < 270) {
                        a = Math.ceil(Math.random() * 26.0);
                        buff = (int)((double)buff + a);
                    } else if (值 >= 270 && 值 < 360) {
                        a = Math.ceil(Math.random() * 35.0);
                        buff = (int)((double)buff + a);
                    } else if (值 >= 360 && 值 < 450) {
                        a = Math.ceil(Math.random() * 44.0);
                        buff = (int)((double)buff + a);
                    } else if (值 >= 450 && 值 < 540) {
                        a = Math.ceil(Math.random() * 53.0);
                        buff = (int)((double)buff + a);
                    } else if (值 >= 540) {
                        a = Math.ceil(Math.random() * 62.0);
                        buff = (int)((double)buff + a);
                    }

                    MapleItemInformationProvider.getInstance().getItemEffect(buff).applyTo(MapleCharacter.this.client.getPlayer());
                }
            }, (long)(20000 - 间隔));
        } else {
            this.仙人模式BUFF线程.cancel(false);
            this.仙人模式BUFF线程 = null;
        }

    }

    public void 修炼物理攻击力() {
        this.保护线程();
        if (this.getExp() < this.level * 10000) {
            this.dropMessage(1, "经验不足够修炼");
        } else if (this.魔法攻击力线程 != null) {
            this.dropMessage(1, "无法同时修炼");
        } else {
            if (Game.主城(this.getMapId())) {
                if (this.物理攻击力线程 == null) {
                    this.dropMessage(1, "修炼物理攻击力开启");
                    this.物理攻击力线程 = BuffTimer.getInstance().register(new Runnable() {
                        public void run() {
                            if (MapleCharacter.this.修仙 > 0) {
                                if (Game.主城(MapleCharacter.this.getMapId())) {
                                    if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 10000) {
                                        MapleCharacter.this.gainExp(-MapleCharacter.this.level * 10000, true, true, false);
                                        MapleCharacter.this.增加修炼("物理攻击力" + MapleCharacter.this.id);
                                        MapleCharacter.this.dropMessage(5, "物理攻击力 + 1");
                                    } else {
                                        MapleCharacter.this.物理攻击力线程.cancel(false);
                                        MapleCharacter.this.物理攻击力线程 = null;
                                        MapleCharacter.this.修仙 = 0;
                                        MapleCharacter.this.dropMessage(1, "修炼物理攻击力关闭,经验不足");
                                    }
                                } else {
                                    MapleCharacter.this.物理攻击力线程.cancel(false);
                                    MapleCharacter.this.物理攻击力线程 = null;
                                    MapleCharacter.this.修仙 = 0;
                                    MapleCharacter.this.dropMessage(1, "安全区内才可以修炼。");
                                }
                            } else {
                                ++MapleCharacter.this.修仙;
                            }

                        }
                    }, 30000L);
                } else {
                    this.物理攻击力线程.cancel(false);
                    this.物理攻击力线程 = null;
                    this.修仙 = 0;
                    this.dropMessage(1, "修炼物理攻击力关闭");
                }
            } else {
                this.物理攻击力线程.cancel(false);
                this.物理攻击力线程 = null;
                this.修仙 = 0;
                this.dropMessage(1, "安全区内才可以修炼。");
            }

        }
    }

    public void 修炼魔法攻击力() {
        this.保护线程();
        if (this.getExp() < this.level * 10000) {
            this.dropMessage(1, "经验不足够修炼");
        } else if (this.物理攻击力线程 != null) {
            this.dropMessage(1, "无法同时修炼");
        } else {
            if (Game.主城(this.getMapId())) {
                if (this.魔法攻击力线程 == null) {
                    this.dropMessage(1, "修炼魔法攻击力开启");
                    this.魔法攻击力线程 = BuffTimer.getInstance().register(new Runnable() {
                        public void run() {
                            if (MapleCharacter.this.修仙 > 0) {
                                if (Game.主城(MapleCharacter.this.getMapId())) {
                                    if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 10000) {
                                        MapleCharacter.this.gainExp(-MapleCharacter.this.level * 10000, true, true, false);
                                        MapleCharacter.this.增加修炼("魔法攻击力" + MapleCharacter.this.id);
                                        MapleCharacter.this.dropMessage(5, "魔法攻击力 + 1");
                                    } else {
                                        MapleCharacter.this.物理攻击力线程.cancel(false);
                                        MapleCharacter.this.物理攻击力线程 = null;
                                        MapleCharacter.this.修仙 = 0;
                                        MapleCharacter.this.dropMessage(1, "修炼魔法攻击力关闭,经验不足");
                                    }
                                } else {
                                    MapleCharacter.this.物理攻击力线程.cancel(false);
                                    MapleCharacter.this.物理攻击力线程 = null;
                                    MapleCharacter.this.修仙 = 0;
                                    MapleCharacter.this.dropMessage(1, "安全区内才可以修炼。");
                                }
                            } else {
                                ++MapleCharacter.this.修仙;
                            }

                        }
                    }, 30000L);
                } else {
                    this.魔法攻击力线程.cancel(false);
                    this.魔法攻击力线程 = null;
                    this.修仙 = 0;
                    this.dropMessage(1, "修炼魔法攻击力关闭");
                }
            } else {
                this.物理攻击力线程.cancel(false);
                this.物理攻击力线程 = null;
                this.修仙 = 0;
                this.dropMessage(1, "安全区内才可以修炼。");
            }

        }
    }

    public void 修炼硬化皮肤() {
        this.保护线程();
        if (this.getExp() < this.level * 10000) {
            this.dropMessage(1, "经验不足够修炼");
        } else if (this.物理攻击力线程 != null) {
            this.dropMessage(1, "无法同时修炼");
        } else if (this.魔法攻击力线程 != null) {
            this.dropMessage(1, "无法同时修炼");
        } else {
            if (Game.主城(this.getMapId())) {
                if (this.硬化皮肤线程 == null) {
                    this.dropMessage(1, "修炼硬化皮肤开启");
                    this.硬化皮肤线程 = BuffTimer.getInstance().register(new Runnable() {
                        public void run() {
                            if (MapleCharacter.this.修仙 > 0) {
                                if (Game.主城(MapleCharacter.this.getMapId())) {
                                    if ((double)MapleCharacter.this.getHp() > (double)MapleCharacter.this.stats.getMaxHp() * 0.3) {
                                        if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 30000) {
                                            MapleCharacter.this.gainExp(-MapleCharacter.this.level * 30000, true, true, false);
                                            MapleCharacter.this.增加修炼("硬化皮肤" + MapleCharacter.this.id);
                                            MapleCharacter.this.dropMessage(5, "硬化皮肤 + 1");
                                            MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.3));
                                        } else {
                                            MapleCharacter.this.硬化皮肤线程.cancel(false);
                                            MapleCharacter.this.硬化皮肤线程 = null;
                                            MapleCharacter.this.修仙 = 0;
                                            MapleCharacter.this.dropMessage(1, "修炼硬化皮肤关闭,经验不足");
                                        }
                                    } else {
                                        MapleCharacter.this.硬化皮肤线程.cancel(false);
                                        MapleCharacter.this.硬化皮肤线程 = null;
                                        MapleCharacter.this.修仙 = 0;
                                        MapleCharacter.this.dropMessage(1, "状态不健康。");
                                    }
                                } else {
                                    MapleCharacter.this.硬化皮肤线程.cancel(false);
                                    MapleCharacter.this.硬化皮肤线程 = null;
                                    MapleCharacter.this.修仙 = 0;
                                    MapleCharacter.this.dropMessage(1, "安全区内才可以修炼。");
                                }
                            } else {
                                ++MapleCharacter.this.修仙;
                            }

                        }
                    }, 60000L);
                } else {
                    this.硬化皮肤线程.cancel(false);
                    this.硬化皮肤线程 = null;
                    this.修仙 = 0;
                    this.dropMessage(1, "修炼硬化皮肤关闭");
                }
            } else {
                this.硬化皮肤线程.cancel(false);
                this.硬化皮肤线程 = null;
                this.修仙 = 0;
                this.dropMessage(1, "安全区内才可以修炼。");
            }

        }
    }

    public void 修炼BUFF增益() {
        this.保护线程();
        if (this.getExp() < this.level * 15000) {
            this.dropMessage(1, "经验不足够修炼");
        } else if (this.魔法攻击力线程 != null) {
            this.dropMessage(1, "无法同时修炼");
        } else if (this.物理攻击力线程 != null) {
            this.dropMessage(1, "无法同时修炼");
        } else {
            if (Game.主城(this.getMapId())) {
                if (this.修炼BUFF增益线程 == null) {
                    this.dropMessage(1, "修炼契合力开启");
                    this.修炼BUFF增益线程 = BuffTimer.getInstance().register(new Runnable() {
                        public void run() {
                            if (MapleCharacter.this.修仙 > 0) {
                                if (Game.主城(MapleCharacter.this.getMapId())) {
                                    if ((double)MapleCharacter.this.getMp() > (double)MapleCharacter.this.stats.getMaxMp() * 0.05 && (double)MapleCharacter.this.getHp() > (double)MapleCharacter.this.stats.getMaxHp() * 0.05) {
                                        int 程度 = MapleCharacter.this.判断修炼("BUFF增益" + MapleCharacter.this.id);
                                        int 经验xx;
                                        byte 成功;
                                        double 概率;
                                        if (程度 <= 50) {
                                            经验xx = 15000;
                                            成功 = 50;
                                            if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 经验xx) {
                                                概率 = Math.ceil(Math.random() * 100.0);
                                                MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.05));
                                                MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.05));
                                                MapleCharacter.this.gainExp(-MapleCharacter.this.level * 经验xx, true, true, false);
                                                if (概率 <= (double)成功) {
                                                    MapleCharacter.this.增加修炼("BUFF增益" + MapleCharacter.this.id);
                                                    MapleCharacter.this.dropMessage(5, "契合力 + 1");
                                                } else {
                                                    MapleCharacter.this.dropMessage(5, "契合力提升失败");
                                                }
                                            } else {
                                                MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                MapleCharacter.this.修炼BUFF增益线程 = null;
                                                MapleCharacter.this.修仙 = 0;
                                                MapleCharacter.this.dropMessage(1, "修炼契合力关闭,经验不足");
                                            }
                                        } else if (程度 > 50 && 程度 <= 100) {
                                            int 经验x = 30000;
                                            成功 = 40;
                                            if (MapleCharacter.this.haveItem(4031216, 200)) {
                                                if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 经验x) {
                                                    概率 = Math.ceil(Math.random() * 100.0);
                                                    MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.05));
                                                    MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.05));
                                                    MapleCharacter.this.gainExp(-MapleCharacter.this.level * 经验x, true, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4031216), 4031216,  200, true, false);
                                                    if (概率 <= (double)成功) {
                                                        MapleCharacter.this.增加修炼("BUFF增益" + MapleCharacter.this.id);
                                                        MapleCharacter.this.dropMessage(5, "契合力 + 1");
                                                    } else {
                                                        MapleCharacter.this.dropMessage(5, "契合力提升失败");
                                                    }
                                                } else {
                                                    MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                    MapleCharacter.this.修炼BUFF增益线程 = null;
                                                    MapleCharacter.this.修仙 = 0;
                                                    MapleCharacter.this.dropMessage(1, "修炼契合力关闭,经验不足");
                                                }
                                            } else {
                                                MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                MapleCharacter.this.修炼BUFF增益线程 = null;
                                                MapleCharacter.this.修仙 = 0;
                                                MapleCharacter.this.dropMessage(1, "修炼契合力关闭\r\n需要；\r\n蝙蝠怪的灵魂石*200");
                                            }
                                        } else if (程度 > 101 && 程度 <= 150) {
                                            int 经验 = '\uea60';
                                            成功 = 30;
                                            if (MapleCharacter.this.haveItem(4031216, 200) && MapleCharacter.this.haveItem(4005004, 1)) {
                                                if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 经验) {
                                                    概率 = Math.ceil(Math.random() * 100.0);
                                                    MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.1));
                                                    MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.1));
                                                    MapleCharacter.this.gainExp(-MapleCharacter.this.level * 经验, true, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4031216), 4031216, 200, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4005004), 4005004, 1, true, false);
                                                    if (概率 <= (double)成功) {
                                                        MapleCharacter.this.增加修炼("BUFF增益" + MapleCharacter.this.id);
                                                        MapleCharacter.this.dropMessage(5, "契合力 + 1");
                                                    } else {
                                                        MapleCharacter.this.dropMessage(5, "契合力提升失败");
                                                    }
                                                } else {
                                                    MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                    MapleCharacter.this.修炼BUFF增益线程 = null;
                                                    MapleCharacter.this.修仙 = 0;
                                                    MapleCharacter.this.dropMessage(1, "修炼契合力关闭,经验不足");
                                                }
                                            } else {
                                                MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                MapleCharacter.this.修炼BUFF增益线程 = null;
                                                MapleCharacter.this.修仙 = 0;
                                                MapleCharacter.this.dropMessage(1, "修炼契合力关闭\r\n需要；\r\n蝙蝠怪的灵魂石*200\r\n黑暗水晶*1");
                                            }
                                        } else if (程度 > 151 && 程度 <= 200) {
                                            经验xx = 100000;
                                            成功 = 30;
                                            if (MapleCharacter.this.haveItem(4005004, 1) && MapleCharacter.this.haveItem(4005000, 5) && MapleCharacter.this.haveItem(4005001, 5) && MapleCharacter.this.haveItem(4005002, 5) && MapleCharacter.this.haveItem(4005003, 5)) {
                                                if (MapleCharacter.this.getExp() > MapleCharacter.this.level * 经验xx) {
                                                    概率 = Math.ceil(Math.random() * 100.0);
                                                    MapleCharacter.this.addMP((int)((double)(-MapleCharacter.this.stats.getMaxMp()) * 0.1));
                                                    MapleCharacter.this.addHP((int)((double)(-MapleCharacter.this.stats.getMaxHp()) * 0.1));
                                                    MapleCharacter.this.gainExp(-MapleCharacter.this.level * 经验xx, true, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4005000), 4005000, 5, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4005001), 4005001, 5, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4005002), 4005002, 5, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4005003), 4005003, 5, true, false);
                                                    MapleInventoryManipulator.removeById(MapleCharacter.this.client, GameConstants.getInventoryType(4005004), 4005004, 1, true, false);
                                                    if (概率 <= (double)成功) {
                                                        MapleCharacter.this.增加修炼("BUFF增益" + MapleCharacter.this.id);
                                                        MapleCharacter.this.dropMessage(5, "契合力 + 1");
                                                    } else {
                                                        MapleCharacter.this.dropMessage(5, "契合力提升失败");
                                                    }
                                                } else {
                                                    MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                    MapleCharacter.this.修炼BUFF增益线程 = null;
                                                    MapleCharacter.this.修仙 = 0;
                                                    MapleCharacter.this.dropMessage(1, "修炼契合力关闭,经验不足");
                                                }
                                            } else {
                                                MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                                MapleCharacter.this.修炼BUFF增益线程 = null;
                                                MapleCharacter.this.修仙 = 0;
                                                MapleCharacter.this.dropMessage(1, "修炼契合力关闭\r\n需要；\r\n黑暗水晶*1\r\n力量水晶*5\r\n敏捷水晶*5\r\n智慧水晶*5\r\n幸运水晶*5");
                                            }
                                        } else {
                                            MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                            MapleCharacter.this.修炼BUFF增益线程 = null;
                                            MapleCharacter.this.修仙 = 0;
                                            MapleCharacter.this.dropMessage(1, "暂未开通更高契合修为");
                                        }
                                    } else {
                                        MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                        MapleCharacter.this.修炼BUFF增益线程 = null;
                                        MapleCharacter.this.修仙 = 0;
                                        MapleCharacter.this.dropMessage(1, "修炼契合力关闭,状态不健康");
                                    }
                                } else {
                                    MapleCharacter.this.修炼BUFF增益线程.cancel(false);
                                    MapleCharacter.this.修炼BUFF增益线程 = null;
                                    MapleCharacter.this.修仙 = 0;
                                    MapleCharacter.this.dropMessage(1, "安全区内才可以修炼。");
                                }
                            } else {
                                ++MapleCharacter.this.修仙;
                            }

                        }
                    }, 120000L);
                } else {
                    this.修炼BUFF增益线程.cancel(false);
                    this.修炼BUFF增益线程 = null;
                    this.修仙 = 0;
                    this.dropMessage(1, "修炼契合力关闭");
                }
            } else {
                this.物理攻击力线程.cancel(false);
                this.物理攻击力线程 = null;
                this.修仙 = 0;
                this.dropMessage(1, "安全区内才可以修炼。");
            }

        }
    }

    public final void 召唤假人(int a) {
        if (!this.clone) {
            for(int i = 0; i < this.clones.length; ++i) {
                if (this.clones[i].get() == null) {
                    MapleCharacter newp1 = this.假人系统(a);
                    this.map.addPlayer(newp1);
                    this.map.broadcastMessage(MaplePacketCreator.updateCharLook(newp1));
                    this.map.movePlayer(newp1, this.getPosition());
                    this.clones[i] = new WeakReference(newp1);
                    return;
                }
            }

        }
    }

    public MapleCharacter 假人系统(int a) {
        MapleClient cs = new MapleClient((MapleAESOFB)null, (MapleAESOFB)null, new MockIOSession());
        int minus = this.getId() + Randomizer.nextInt(this.getId());
        MapleCharacter ret = new MapleCharacter(true);
        ret.id = minus;
        ret.client = cs;
        ret.exp = 0;
        ret.meso = 0;
        ret.beans = this.beans;
        ret.blood = this.blood;
        ret.month = this.month;
        ret.day = this.day;
        ret.charmessage = this.charmessage;
        ret.expression = this.expression;
        ret.constellation = this.constellation;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = 取假人属性(a, "name");
        ret.level = (short)Integer.parseInt(取假人属性(a, "level"));
        ret.fame = (short)Integer.parseInt(取假人属性(a, "fame"));
        ret.job = (short)Integer.parseInt(取假人属性(a, "job"));
        ret.hair = (short)Integer.parseInt(取假人属性(a, "hair"));
        ret.face = (short)Integer.parseInt(取假人属性(a, "face"));
        ret.skinColor = this.skinColor;
        ret.bookCover = this.bookCover;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = this.gmLevel;
        ret.gender = this.gender;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(this.getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = (short)Integer.parseInt(取假人属性(a, "guildid"));
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll(this.effects);
        ret.guildrank = this.guildrank;
        ret.allianceRank = this.allianceRank;
        ret.hidden = this.hidden;
        ret.setPosition(new Point(this.getPosition()));
        Iterator var5 = this.getInventory(MapleInventoryType.EQUIPPED).iterator();

        while(var5.hasNext()) {
            IItem equip = (IItem)var5.next();
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }

        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.rocks = this.rocks;
        ret.regrocks = this.regrocks;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;
        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.acash = this.acash;
        ret.lastGainHM = this.lastGainHM;
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());

        while(this.map.getCharacterById(ret.id) != null || this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null) {
            ++ret.id;
        }

        ret.client.setPlayer(ret);
        return ret;
    }

    public static String 取假人属性(int a, String b) {
        String data = "";

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT " + b + " FROM characters WHERE id = ?");
            ps.setInt(1, a);
            ResultSet rs = ps.executeQuery();
            Throwable var6 = null;

            try {
                if (rs.next()) {
                    data = rs.getString(b);
                }
            } catch (Throwable var16) {
                var6 = var16;
                throw var16;
            } finally {
                if (rs != null) {
                    if (var6 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var15) {
                            var6.addSuppressed(var15);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            DBConPool.close(ps);
        } catch (SQLException var18) {
            服务端输出信息.println_err("取假人属性出错");
        }

        return data;
    }
    public int Getrobot(String Name, int Channale) {
        int ret = -1;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM robot WHERE channel = ? and Name = ?");
            ps.setInt(1, Channale);
            ps.setString(2, Name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            ret = rs.getInt("Point");
            rs.close();
            DBConPool.close(ps);
        } catch (SQLException var7) {
        }

        return ret;
    }

    public void 保护线程() {
        if (this.保护线程 == null) {
            this.保护线程 = BuffTimer.getInstance().register(new Runnable() {
                public void run() {
                    if (Find.findChannel(MapleCharacter.this.name) == 2) {
                        MapleCharacter.this.gainExp(1, false, false, false);
                    } else {
                        MapleCharacter.this.关闭保护线程();
                    }

                }
            }, 60000L);
        } else if (this.保护线程 != null) {
            this.保护线程.cancel(false);
            this.保护线程 = null;
        }

    }

    public void 关闭保护线程() {
        if (this.保护线程 != null) {
            this.保护线程.cancel(false);
            this.保护线程 = null;
        }

    }
    public void 增加修炼(String a) {
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet rs = null;
        int ret = this.判断修炼(a);
        ++ret;

        try {
            Connection con = DBConPool.getConnection();
            ps = con.prepareStatement("SELECT * FROM jiezoudashi ");
            rs = ps.executeQuery();
            if (rs.next()) {
                String sqlString1 = null;
                sqlString1 = "update jiezoudashi set Val = '" + ret + "' where Name = '" + a + "';";
                PreparedStatement Val = con.prepareStatement(sqlString1);
                Val.executeUpdate(sqlString1);
            }

            DBConPool.close(ps);
        } catch (SQLException var9) {
        }

    }

    public int 判断修炼(String a) {
        int data = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE Name = '" + a + "'");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                data = rs.getInt("Val");
            }

            DBConPool.close(ps);
        } catch (SQLException var6) {
        }

        return data;
    }

    public int 判断修炼2(String a) {
        int ret = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM jiezoudashi WHERE Name = `" + a + "`");
            Throwable var5 = null;

            try {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ret = rs.getInt("Val");
                }

                DBConPool.close(ps);
            } catch (Throwable var15) {
                var5 = var15;
                throw var15;
            } finally {
                if (ps != null) {
                    if (var5 != null) {
                        try {
                            ps.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        ps.close();
                    }
                }

            }
        } catch (SQLException var17) {
        }

        return ret;
    }
    public int getOfflineMoney(MapleCharacter victim) {
        return this.getMoney(victim);
    }

    public int getMoney() {
        return this.getMoney(this);
    }

    public int getMoney(MapleCharacter chr) {
        int maxtimes = 10;
        int nowtime = 0;
        int delay = 500;
        boolean error = false;
        int x = 0;

        do {
            ++nowtime;

            try {
                Connection con = DBConPool.getConnection();
                Throwable var8 = null;

                try {
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery("Select money from Accounts Where id = " + chr.getClient().getAccID());

                    while(rs.next()) {
                        int debug = -1;

                        try {
                            debug = rs.getInt("money");
                        } catch (SQLException var25) {
                        }

                        if (debug != -1) {
                            x = rs.getInt("money");
                            error = false;
                        } else {
                            error = true;
                        }
                    }

                    rs.close();
                } catch (Throwable var26) {
                    var8 = var26;
                    throw var26;
                } finally {
                    if (con != null) {
                        if (var8 != null) {
                            try {
                                con.close();
                            } catch (Throwable var24) {
                                var8.addSuppressed(var24);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var28) {
                服务端输出信息.println_err("[getMoney]无法连接资料库");
                FileoutputUtil.outError("logs/资料库异常.txt", var28);
            } catch (Exception var29) {
                服务端输出信息.println_err("[getMoney]" + var29);
                FileoutputUtil.outError("logs/资料库异常.txt", var29);
            }

            if (error) {
                try {
                    Thread.sleep((long)delay);
                } catch (Exception var23) {
                    FileoutputUtil.outError("logs/资料库异常.txt", var23);
                }
            }
        } while(error && nowtime < maxtimes);

        return x;
    }

    public boolean resetBossLog() {
        return this.isClone() ? false : TimeLogCenter.getInstance().deleteBossLogAll(this.id);
    }

    public boolean deleteBossLog(String name) {
        return this.isClone() ? false : TimeLogCenter.getInstance().deleteBossLogAll(this.id, name);
    }

    public boolean deleteBossLog(String name, int count) {
        if (this.isClone()) {
            return false;
        } else {
            TimeLogCenter.getInstance().setBossLog(this.id, name, -count);
            return true;
        }
    }
    public boolean resetBossLogA() {
        return this.isClone() ? false : TimeLogCenter.getInstance().deleteBossLogaAll(this.getAccountID());
    }

    public boolean deleteBossLoga(String name) {
        return this.isClone() ? false : TimeLogCenter.getInstance().deleteBossLogaAll(this.getAccountID(), name);
    }

    public boolean deleteBossLoga(String name, int count) {
        if (this.isClone()) {
            return false;
        } else {
            TimeLogCenter.getInstance().setBossLoga(this.getAccountID(), name, -count);
            return true;
        }
    }


    public boolean getBackupInventory() {
        return this.backupInventory;
    }

    public void setBackupInventory(boolean backupInventory) {
        this.backupInventory = backupInventory;
    }


    public void addMount(int mountId) {
        if (!this.mountList.contains(mountId)) {
            this.mountList.add(mountId);
        }

    }

    public void clearMountList() {
        this.mountList.clear();
    }

    public boolean saveMountListToDB(Connection con) {
        if (this.mountList.isEmpty()) {
            return false;
        } else {
            try {
                if (con == null || con.isClosed()) {
                    con = DBConPool.getNewConnection();
                }

                PreparedStatement ps = con.prepareStatement("DELETE FROM snail_chr_mount_list WHERE chrid = ?");
                ps.setInt(1, this.getId());
                ps.executeUpdate();
                ps = con.prepareStatement("INSERT INTO snail_chr_mount_list (chrid,mountid) VALUES ( ?, ?)");
                Iterator var3 = this.mountList.iterator();

                while(var3.hasNext()) {
                    int mountId = (Integer)var3.next();
                    ps.setInt(1, this.getId());
                    ps.setInt(2, mountId);
                    ps.executeUpdate();
                }

                ps.close();
                return true;
            } catch (SQLException var5) {
                服务端输出信息.println_err("【错误】saveMountListToDB 错误，错误原因：" + var5);
                var5.printStackTrace();
                return false;
            }
        }
    }
    public void setBossLoga(String bossid) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setBossLoga(this.getAccountID(), bossid, 1);
        }
    }

    public void setBossLoga(String bossid, int count) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setBossLoga(this.getAccountID(), bossid, count);
        }
    }
    public void loadMountListFromDB() {
        this.mountList.clear();

        try {
            Connection con = DBConPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_chr_mount_list WHERE chrid = ?");
                ps.setInt(1, this.getId());
                ResultSet rs = ps.executeQuery();

                while(rs.next()) {
                    this.mountList.add(rs.getInt("mountid"));
                }

                ps.close();
                rs.close();
            } catch (Exception var15) {
            } finally {
                if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                        }
                }

            }
        } catch (Exception var17) {
            服务端输出信息.println_err("【错误】loadMountListFromDB 错误，错误原因：" + var17);
            var17.printStackTrace();
        }
    }

    public boolean haveMount(int mountId) {
        if (this.mountList.isEmpty()) {
            this.loadMountListFromDB();
        }

        return this.mountList.contains(mountId);
    }
    public void Gaincharacterz(String Name, int Channale, int Piot) {
        try {
            int ret = this.Getcharacterz(Name, Channale);
            if (ret == -1) {
                ret = 0;
                PreparedStatement ps = null;

                try {
                    ps = DBConPool.getConnection().prepareStatement("INSERT INTO characterz (channel, Name,Point) VALUES (?, ?, ?)");
                    ps.setInt(1, Channale);
                    ps.setString(2, Name);
                    ps.setInt(3, ret);
                    ps.execute();
                } catch (SQLException var16) {
                    服务端输出信息.println_out("xxxxxxxx:" + var16);
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException var15) {
                        服务端输出信息.println_out("xxxxxxxxzzzzzzz:" + var15);
                    }

                }
            }

            ret += Piot;
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE characterz SET `Point` = ? WHERE Name = ? and channel = ?");
            ps.setInt(1, ret);
            ps.setString(2, Name);
            ps.setInt(3, Channale);
            ps.execute();
            ps.close();
        } catch (SQLException var18) {
            服务端输出信息.println_err("Getcharacterz!!55" + var18);
        }

    }
    public int getBossLogi(String bossid) {
        try {
            Connection con = DBConPool.getConnection();
            int ret_count = 0;
            try {
                PreparedStatement ps = con.prepareStatement("select count(*) from bosslogi where ip = ? and bossid = ? and lastattempt >= DATE_SUB(curdate(),INTERVAL 0 DAY)");
                ps.setString(1, this.getClient().getSessionIPAddress());
                ps.setString(2, bossid);
                ResultSet rs = ps.executeQuery();

                try {
                    if (rs.next()) {
                        ret_count = rs.getInt(1);
                    }
                } catch (Throwable var32) {

                } finally {
                    if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var31) {
                            }
                    }
                }
                ps.close();
                return ret_count;
            } catch (Throwable var34) {
            } finally {
                if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var30) {}
                }
            }
        } catch (Exception var36) {
            FileoutputUtil.outError("logs/资料库异常.txt", var36);
        }
        return -1;
    }

    public void setBossLogi(String bossid) {
        if (!this.isClone()) {
            try {
                Connection con = DBConPool.getConnection();
                Throwable var3 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("insert into bosslogi (ip, bossid) values (?,?)");
                    ps.setString(1, this.getClient().getSessionIPAddress());
                    ps.setString(2, bossid);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if (con != null) {
                        if (var3 != null) {
                            try {
                                con.close();
                            } catch (Throwable var12) {
                                var3.addSuppressed(var12);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (Exception var15) {
                FileoutputUtil.outError("logs/资料库异常.txt", var15);
            }

        }
    }

    public int getBossLoga(String bossid) {
        return TimeLogCenter.getInstance().getBossLoga(this.getAccountID(), bossid);
    }


    public boolean isShowChair() {
        return this.isShowChair;
    }

    public void setShowChair(boolean isShowChair) {
        this.isShowChair = isShowChair;
    }

    public boolean isShowEquip() {
        return this.isShowEquip;
    }

    public void setShowEquip(boolean isShowEquip) {
        this.isShowEquip = isShowEquip;
    }

    public void showEffect(boolean broadcast, String effect) {
        if (broadcast) {
            this.getMap().broadcastMessage(MaplePacketCreator.showEffect(effect));
        } else {
            this.getClient().sendPacket(MaplePacketCreator.showEffect(effect));
        }

    }

    public void 增加经验储备(long inc_exp) {
        long now_exp = this.exp_reserve;
        if (now_exp <= 0L) {
            now_exp = 0L;
        }

        if (inc_exp > 0L) {
            now_exp += inc_exp;
            this.exp_reserve = now_exp;
        }

    }

    public void 扣除经验储备(long inc_exp) {
        long now_exp = this.exp_reserve;
        if (now_exp <= 199999L) {
            now_exp = 199999L;
        }

        if (inc_exp > 0L) {
            now_exp -= inc_exp;
            if (now_exp <= 0L) {
                now_exp = 0L;
            }

            this.exp_reserve = now_exp;
        }

    }

    public long 读取经验储备() {
        return this.exp_reserve;
    }

    public int getTamingMobId() {
        return this.tamingMobId;
    }

    public void setTamingMobId(int tamingMobId) {
        this.tamingMobId = tamingMobId;
    }

    public int getTamingMobItemId() {
        return this.tamingMobItemId;
    }

    public void setTamingMobItemId(int tamingMobItemId) {
        this.tamingMobItemId = tamingMobItemId;
    }


    public byte getImprison() {
        return this.imprison;
    }

    public void setImprison(byte imprison) {
        this.imprison = imprison;
    }
    public int[] getCloneDamageRateList() {
        return this.cloneDamageRateList;
    }

    public void setCloneDamageRateList(int[] cloneDamageRateList) {
        this.cloneDamageRateList = cloneDamageRateList;
    }

    public int getStage() {
        return this.stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public String getStageName() {
        return this.getStageName(this.stage);
    }

    public String getStageName(int stage) {
        return GameConstants.getChrStageName(stage);
    }

    public int getStageCount(int stage) {
        return GameConstants.getChrStageCount(stage);
    }

    public int getStageCount() {
        return GameConstants.getChrStageCount(this.stage);
    }

    public int getNextStageCount() {
        return this.stage >= GameConstants.getMaxChrStage() ? -1 : GameConstants.getChrStageCount(this.stage + 1);
    }

    public Map<Integer, Pair<String, Integer>> getStageMap() {
        return GameConstants.getChrStageMap();
    }
    public int getBreakLevel() {
        return this.breakLevel;
    }

    public void setBreakLevel(int breakLevel) {
        this.breakLevel = breakLevel;
    }

    public float getExpRateChr() {
        return this.expRateChr;
    }

    public void setExpRateChr(float expRateChr) {
        this.expRateChr = expRateChr;
    }

    public void gainExpRateChr(float expRateChr) {
        this.expRateChr += expRateChr;
    }

    public float getMesoRateChr() {
        return this.mesoRateChr;
    }

    public void setMesoRateChr(float mesoRateChr) {
        this.mesoRateChr = mesoRateChr;
    }

    public void gainMesoRateChr(float mesoRateChr) {
        this.mesoRateChr += mesoRateChr;
    }

    public float getDropRateChr() {
        return this.dropRateChr;
    }

    public void setDropRateChr(float dropRateChr) {
        this.dropRateChr = dropRateChr;
    }

    public void gainDropRateChr(float dropRateChr) {
        this.dropRateChr += dropRateChr;
    }

    public int getLastTakeDamageValue() {
        return this.lastTakeDamageValue;
    }

    public void setLastTakeDamageValue(int lastTakeDamageValue) {
        this.lastTakeDamageValue = lastTakeDamageValue;
    }

    public long getLastPetHpTime() {
        return this.lastPetHpTime;
    }

    public void setLastPetHpTime(long lastPetHpTime) {
        this.lastPetHpTime = lastPetHpTime;
    }
    public void setId(int id) {
        this.id = id;
    }

    public static MapleCharacter loadCharFromDB(int charid, boolean channelserver, Connection con) {
        MapleCharacter ret = new MapleCharacter(channelserver);
        ret.client = new MapleClient((MapleAESOFB)null, (MapleAESOFB)null, new MockIOSession());
        ret.setId(charid);
        if (Game.调试2.equals("开")) {
            服务端输出信息.println_out("开始从数据库读取角色" + charid + "的数据：");
        }

        try {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = con.prepareStatement("SELECT * FROM characters WHERE id = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (!rs.next()) {
                    throw new RuntimeException("Loading the Char Failed (char not found)");
                }

                ret.Vip_Medal = rs.getShort("VipMedal") > 0;
                ret.name = rs.getString("name");
                ret.level = rs.getShort("level");
                ret.fame = rs.getShort("fame");
                ret.stats.str = rs.getShort("str");
                ret.stats.dex = rs.getShort("dex");
                ret.stats.int_ = rs.getShort("int");
                ret.stats.luk = rs.getShort("luk");
                ret.stats.maxhp = rs.getShort("maxhp");
                ret.stats.maxmp = rs.getShort("maxmp");
                ret.stats.hp = rs.getShort("hp");
                ret.stats.mp = rs.getShort("mp");
                ret.exp = rs.getInt("exp");
                ret.hpmpApUsed = rs.getShort("hpApUsed");
                String[] sp = rs.getString("sp").split(",");

                for(int i = 0; i < ret.remainingSp.length; ++i) {
                    ret.remainingSp[i] = Integer.parseInt(sp[i]);
                }

                if (Game.调试2.equals("开")) {
                    服务端输出信息.println_out("正在读取角色" + charid + " characters表的数据，角色名为：" + ret.name);
                }

                ret.remainingAp = rs.getShort("ap");
                ret.beans = rs.getInt("beans");
                ret.todayOnlineTime = rs.getInt("todayOnlineTime");
                ret.totalOnlineTime = rs.getInt("totalOnlineTime");
                ret.max_damage = rs.getLong("max_damage");

                if (ret.max_damage < 199999L) {
                    ret.max_damage = 199999L;
                }

                ret.meso = rs.getInt("meso");
                ret.gmLevel = rs.getByte("gm");
                ret.skinColor = rs.getByte("skincolor");
                ret.gender = rs.getByte("gender");
                ret.job = rs.getShort("job");
                ret.hair = rs.getInt("hair");
                ret.face = rs.getInt("face");
                ret.accountid = rs.getInt("accountid");
                ret.mapid = rs.getInt("map");
                ret.initialSpawnPoint = rs.getByte("spawnpoint");
                ret.world = rs.getByte("world");
                ret.guildid = rs.getInt("guildid");
                ret.guildrank = rs.getByte("guildrank");
                ret.allianceRank = rs.getByte("allianceRank");
                ret.currentrep = rs.getInt("currentrep");
                ret.totalrep = rs.getInt("totalrep");
                ret.makeMFC(rs.getInt("familyid"), rs.getInt("seniorid"), rs.getInt("junior1"), rs.getInt("junior2"));
                if (ret.guildid > 0) {
                    ret.mgc = new MapleGuildCharacter(ret);
                }

                ret.buddylist = new BuddyList(rs.getByte("buddyCapacity"));
                ret.subcategory = rs.getByte("subcategory");
                ret.mount = new MapleMount(ret, 0, ret.job > 1000 && ret.job < 2000 ? 10001004 : (ret.job < 2000 ? 1004 : (ret.job != 2001 && (ret.job < 2200 || ret.job > 2218) ? (ret.job >= 3000 ? 30001004 : 20001004) : 20011004)), (byte)0, (byte)1, 0);
                ret.rank = rs.getInt("rank");
                ret.rankMove = rs.getInt("rankMove");
                ret.jobRank = rs.getInt("jobRank");
                ret.jobRankMove = rs.getInt("jobRankMove");
                ret.marriageId = rs.getInt("marriageId");
                ret.charmessage = rs.getString("charmessage");
                ret.expression = rs.getInt("expression");
                ret.constellation = rs.getInt("constellation");
                ret.blood = rs.getInt("blood");
                ret.month = rs.getInt("month");
                ret.day = rs.getInt("day");
                ret.prefix = rs.getString("prefix");
                ret.gachexp = rs.getInt("gachexp");

                ret.exp_reserve = rs.getLong("exp_reserve");
                ret.imprison = rs.getByte("imprison");
                ret.guildPoints = rs.getInt("guildpoints");
                ret.stage = rs.getInt("stage");
                ret.breakLevel = rs.getInt("break_level");
                ret.expRateChr = rs.getFloat("exp_rate");
                ret.mesoRateChr = rs.getFloat("meso_rate");
                ret.dropRateChr = rs.getFloat("drop_rate");
                if (channelserver) {
                    MapleMapFactory mapFactory = ChannelServer.getInstance(1).getMapFactory();
                    ret.antiMacro = new MapleLieDetector(ret.id);
                    ret.map = mapFactory.getMap(ret.mapid);
                    if (ret.mapid == 801000210) {
                        ret.map = mapFactory.getMap(801000000);
                    }

                    if (ret.mapid == 801000110) {
                        ret.map = mapFactory.getMap(801000000);
                    }

                    if (ret.map == null) {
                        ret.map = mapFactory.getMap(100000000);
                    }

                    MaplePortal portal = ret.map.getPortal(ret.initialSpawnPoint);
                    if (portal == null) {
                        portal = ret.map.getPortal(0);
                        ret.initialSpawnPoint = 0;
                    }

                    ret.setPosition(portal.getPosition());
                    int partyid = rs.getInt("party");
                    if (partyid >= 0) {
                        MapleParty party = Party.getParty(partyid);
                        if (party != null && party.getMemberById(ret.id) != null) {
                            ret.party = party;
                        }
                    }

                    ret.bookCover = rs.getInt("monsterbookcover");
                    ret.dojo = rs.getInt("dojo_pts");
                    ret.dojoRecord = rs.getByte("dojoRecord");
                    String[] pets = rs.getString("pets").split(",");

                    for(int i = 0; i < ret.petStore.length; ++i) {
                        ret.petStore[i] = Byte.parseByte(pets[i]);
                    }

                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT achievementid FROM achievements WHERE accountid = ?");
                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();

                    while(rs.next()) {
                        ret.finishedAchievements.add(rs.getInt("achievementid"));
                    }

                    if (Game.调试2.equals("开")) {
                        服务端输出信息.println_out("正在读取角色" + charid + " achievements表的数据");
                    }
                }

                rs.close();
                ps.close();
                boolean compensate_previousEvans = false;
                ps = con.prepareStatement("SELECT * FROM queststatus WHERE characterid = ?");
                ps.setInt(1, charid);
                rs = ps.executeQuery();
                if (Game.调试2.equals("开")) {
                    服务端输出信息.println_out("正在读取角色" + charid + " queststatus表的数据");
                }

                PreparedStatement pse = con.prepareStatement("SELECT * FROM queststatusmobs WHERE queststatusid = ?");
                if (Game.调试2.equals("开")) {
                    服务端输出信息.println_out("正在读取角色" + charid + " queststatusmobs表的数据");
                }

                while(rs.next()) {
                    int id = rs.getInt("quest");
                    if (id == 170000) {
                        compensate_previousEvans = true;
                    }

                    MapleQuest q = MapleQuest.getInstance(id);
                    MapleQuestStatus status = new MapleQuestStatus(q, rs.getByte("status"));
                    long cTime = rs.getLong("time");
                    if (cTime > -1L) {
                        status.setCompletionTime(cTime * 1000L);
                    }

                    status.setForfeited(rs.getInt("forfeited"));
                    status.setCustomData(rs.getString("customData"));
                    ret.quests.put(q, status);
                    pse.setLong(1, rs.getLong("queststatusid"));
                    ResultSet rsMobs = pse.executeQuery();
                    Throwable var15 = null;

                    try {
                        while(rsMobs.next()) {
                            status.setMobKills(rsMobs.getInt("mob"), rsMobs.getInt("count"));
                        }
                    } catch (Throwable var41) {
                        var15 = var41;
                        throw var41;
                    } finally {
                        if (rsMobs != null) {
                            if (var15 != null) {
                                try {
                                    rsMobs.close();
                                } catch (Throwable var40) {
                                    var15.addSuppressed(var40);
                                }
                            } else {
                                rsMobs.close();
                            }
                        }

                    }
                }

                rs.close();
                ps.close();
                pse.close();
                Iterator var50;
                Pair mit;
                if (channelserver) {
                    ret.CRand = new PlayerRandomStream();
                    ret.monsterbook = MonsterBook.loadCards(charid);
                    ps = con.prepareStatement("SELECT * FROM inventoryslot where characterid = ?");
                    if (Game.调试2.equals("开")) {
                        服务端输出信息.println_out("正在读取角色" + charid + " inventoryslot表的数据");
                    }

                    ps.setInt(1, charid);
                    rs = ps.executeQuery();
                    if (!rs.next()) {
                        throw new RuntimeException("No Inventory slot column found in SQL. [inventoryslot]");
                    }

                    ret.getInventory(MapleInventoryType.EQUIP).setSlotLimit(rs.getByte("equip"));
                    ret.getInventory(MapleInventoryType.USE).setSlotLimit(rs.getByte("use"));
                    ret.getInventory(MapleInventoryType.SETUP).setSlotLimit(rs.getByte("setup"));
                    ret.getInventory(MapleInventoryType.ETC).setSlotLimit(rs.getByte("etc"));
                    ret.getInventory(MapleInventoryType.CASH).setSlotLimit(rs.getByte("cash"));
                    if (Game.调试2.equals("开")) {
                        服务端输出信息.println_out("equip=" + rs.getByte("equip"));
                    }

                    ps.close();
                    rs.close();
                    var50 = ItemLoader.INVENTORY.loadItems(con, false, new Integer[]{charid}).values().iterator();

                    while(var50.hasNext()) {
                        mit = (Pair)var50.next();
                        ret.getInventory((MapleInventoryType)mit.getRight()).addFromDB((IItem)mit.getLeft());
                        if (((IItem)mit.getLeft()).getPet() != null) {
                            ret.pets.add(((IItem)mit.getLeft()).getPet());
                        }
                    }

                    ps = con.prepareStatement("SELECT name, 2ndpassword, mPoints, vpoints, VIP, loginkey, serverkey, clientkey FROM accounts WHERE id = ?");
                    if (Game.调试2.equals("开")) {
                        服务端输出信息.println_out("正在读取角色" + charid + " accounts表的数据");
                    }

                    ps.setInt(1, ret.accountid);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        ret.getClient().setAccountName(rs.getString("name"));
                        ret.accountsecondPassword = rs.getString("2ndpassword");
                        ret.maplepoints = rs.getInt("mPoints");
                        ret.vpoints = rs.getInt("vpoints");
                        ret.vip = rs.getInt("VIP");
                        ret.loginkey = rs.getString("loginkey");
                        ret.serverkey = rs.getString("serverkey");
                        ret.clientkey = rs.getString("clientkey");
                    } else {
                        rs.close();
                    }

                    ps.close();
                    ps = con.prepareStatement("SELECT quest, customData FROM questinfo WHERE characterid = ?");
                    if (Game.调试2.equals("开")) {
                        服务端输出信息.println_out("正在读取角色" + charid + " questinfo表的数据");
                    }

                    ps.setInt(1, charid);
                    rs = ps.executeQuery();

                    while(rs.next()) {
                        ret.questinfo.put(rs.getInt("quest"), rs.getString("customData"));
                    }

                    rs.close();
                    ps.close();
                    ps = con.prepareStatement("SELECT skillid, skilllevel, masterlevel, expiration FROM skills WHERE characterid = ?");
                    if (Game.调试2.equals("开")) {
                        服务端输出信息.println_out("正在读取角色" + charid + " skills表的数据");
                    }

                    ps.setInt(1, charid);
                    rs = ps.executeQuery();

                    while(true) {
                        int[] var10000;
                        while(rs.next()) {
                            ISkill skil = SkillFactory.getSkill(rs.getInt("skillid"));
                            if (skil != null && GameConstants.isApplicableSkill(rs.getInt("skillid")) && rs.getByte("skilllevel") >= 0) {
                                ret.skills.put(skil, new SkillEntry(rs.getByte("skilllevel"), rs.getByte("masterlevel"), rs.getLong("expiration")));
                            } else if (skil == null) {
                                var10000 = ret.remainingSp;
                                int var10001 = GameConstants.getSkillBookForSkill(rs.getInt("skillid"));
                                var10000[var10001] += rs.getByte("skilllevel");
                            }
                        }

                        rs.close();
                        ps.close();
                        ret.expirationTask(false);
                        ps = con.prepareStatement("SELECT id, name, level FROM characters WHERE accountid = ? ORDER BY level DESC");
                        if (Game.调试2.equals("开")) {
                            服务端输出信息.println_out("正在读取角色" + charid + " characters表的数据");
                        }

                        ps.setInt(1, ret.accountid);
                        rs = ps.executeQuery();
                        byte maxlevel_ = 0;

                        while(true) {
                            int position;
                            while(rs.next()) {
                                if (rs.getInt("id") != charid) {
                                    byte maxlevel = (byte)(rs.getShort("level") / 10);
                                    if (maxlevel > 20) {
                                        maxlevel = 20;
                                    }

                                    if (maxlevel > maxlevel_) {
                                        maxlevel_ = maxlevel;
                                        ret.BlessOfFairy_Origin = rs.getString("name");
                                    }
                                } else if (charid < 17000 && !compensate_previousEvans && ret.job >= 2200 && ret.job <= 2218) {
                                    for(position = 0; position <= GameConstants.getSkillBook(ret.job); ++position) {
                                        var10000 = ret.remainingSp;
                                        var10000[position] += 2;
                                    }

                                    ret.setQuestAdd(MapleQuest.getInstance(170000), (byte)0, (String)null);
                                }
                            }

                            ret.skills.put(SkillFactory.getSkill(GameConstants.getBofForJob(ret.job)), new SkillEntry(maxlevel_, (byte)0, -1L));
                            ps.close();
                            rs.close();
                            ps = con.prepareStatement("SELECT * FROM skillmacros WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " skillmacros表的数据：");
                            }

                            ps.setInt(1, charid);

                            SkillMacro macro;
                            for(rs = ps.executeQuery(); rs.next(); ret.skillMacros[position] = macro) {
                                if (Game.调试2.equals("开")) {
                                    服务端输出信息.println_out("position " + rs.getInt("position") + " skill1 " + rs.getInt("skill1") + " skill2 " + rs.getInt("skill2") + " skill3 " + rs.getInt("skill3") + " name " + rs.getString("name") + " shout " + rs.getInt("shout"));
                                }

                                position = rs.getInt("position");
                                macro = new SkillMacro(rs.getInt("skill1"), rs.getInt("skill2"), rs.getInt("skill3"), rs.getString("name"), rs.getInt("shout"), position);
                            }

                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT `key`,`type`,`action` FROM keymap WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " keymap表的数据");
                            }

                            ps.setInt(1, charid);
                            rs = ps.executeQuery();
                            Map<Integer, Pair<Byte, Integer>> keyb = ret.keylayout.Layout();

                            while(rs.next()) {
                                keyb.put(rs.getInt("key"), new Pair(rs.getByte("type"), rs.getInt("action")));
                            }

                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT `locationtype`,`map` FROM savedlocations WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " savedlocations表的数据");
                            }

                            ps.setInt(1, charid);

                            for(rs = ps.executeQuery(); rs.next(); ret.savedLocations[rs.getInt("locationtype")] = rs.getInt("map")) {
                            }

                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT `characterid_to`,`when` FROM famelog WHERE characterid = ? AND DATEDIFF(NOW(),`when`) < 30");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " famelog表的数据");
                            }

                            ps.setInt(1, charid);
                            rs = ps.executeQuery();
                            ret.lastfametime = 0L;
                            ret.lastmonthfameids = new ArrayList(31);

                            while(rs.next()) {
                                ret.lastfametime = Math.max(ret.lastfametime, rs.getTimestamp("when").getTime());
                                ret.lastmonthfameids.add(rs.getInt("characterid_to"));
                            }

                            rs.close();
                            ps.close();
                            ret.buddylist.loadFromDb(charid);
                            ret.storage = MapleStorage.loadStorage(ret.accountid, con);
                            ret.cs = new CashShop(ret.accountid, charid, ret.getJob());
                            ps = con.prepareStatement("SELECT sn FROM wishlist WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " wishlist表的数据");
                            }

                            ps.setInt(1, charid);
                            rs = ps.executeQuery();

                            int i;
                            for(i = 0; rs.next(); ++i) {
                                ret.wishlist[i] = rs.getInt("sn");
                            }

                            while(i < 10) {
                                ret.wishlist[i] = 0;
                                ++i;
                            }

                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT mapid FROM trocklocations WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " trocklocations表的数据");
                            }

                            ps.setInt(1, charid);
                            rs = ps.executeQuery();

                            int r;
                            for(r = 0; rs.next(); ++r) {
                                ret.rocks[r] = rs.getInt("mapid");
                            }

                            while(r < 10) {
                                ret.rocks[r] = 999999999;
                                ++r;
                            }

                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT mapid FROM regrocklocations WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " regrocklocations表的数据");
                            }

                            ps.setInt(1, charid);
                            rs = ps.executeQuery();

                            for(r = 0; rs.next(); ++r) {
                                ret.regrocks[r] = rs.getInt("mapid");
                            }

                            while(r < 5) {
                                ret.regrocks[r] = 999999999;
                                ++r;
                            }

                            rs.close();
                            ps.close();
                            ps = con.prepareStatement("SELECT * FROM mountdata WHERE characterid = ?");
                            if (Game.调试2.equals("开")) {
                                服务端输出信息.println_out("正在读取角色" + charid + " mountdata表的数据");
                            }

                            ps.setInt(1, charid);
                            rs = ps.executeQuery();
                            if (!rs.next()) {
                                throw new RuntimeException("No mount data found on SQL column");
                            }

                            IItem mount = ret.getInventory(MapleInventoryType.EQUIPPED).getItem((short)-18);
                            ret.mount = new MapleMount(ret, mount != null ? mount.getItemId() : 0, ret.job > 1000 && ret.job < 2000 ? 10001004 : (ret.job >= 2000 ? (ret.job != 2001 && ret.job < 2200 ? (ret.job >= 3000 ? 30001004 : 20001004) : 20011004) : 1004), rs.getByte("Fatigue"), rs.getByte("Level"), rs.getInt("Exp"));
                            ps.close();
                            rs.close();
                            ret.skillSkin.loadChrSkill();
                            ret.loadMountListFromDB();
                           // ret.loadMonsterKillQuestFromDB(con);
                            ret.stats.recalcLocalStats(true);
                            return ret;
                        }
                    }
                } else {
                    var50 = ItemLoader.INVENTORY.loadItems(con, true, new Integer[]{charid}).values().iterator();

                    while(var50.hasNext()) {
                        mit = (Pair)var50.next();
                        ret.getInventory((MapleInventoryType)mit.getRight()).addFromDB((IItem)mit.getLeft());
                    }
                }
            } catch (SQLException var43) {
                FilePrinter.printError("MapleCharacter.txt", var43, "载入角色失败..");
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (SQLException var39) {
                    FileoutputUtil.outError("logs/资料库异常.txt", var39);
                }

            }
        } catch (Exception var45) {
            FileoutputUtil.outError("logs/资料库异常.txt", var45);
        }

        return ret;
    }

    public void forceCancelAllBuffs() {
        LinkedList<MapleBuffStatValueHolder> allBuffs = new LinkedList(this.effects.values());
        Iterator var2 = allBuffs.iterator();

        while(var2.hasNext()) {
            MapleBuffStatValueHolder mbsvh = (MapleBuffStatValueHolder)var2.next();
            if (this != null && mbsvh.effect != null) {
                this.cancelEffect(mbsvh.effect, false, mbsvh.startTime, mbsvh.effect.getStatups(), true);
            }
        }

    }
    public boolean 检测是否开店() {
        IMaplePlayerShop merchant = this.getPlayerShop();
        if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(this) && merchant.isAvailable()) {
            this.是否开店 = true;
            return true;
        } else {
            Iterator var2 = ChannelServer.getAllInstances().iterator();

            while(var2.hasNext()) {
                ChannelServer cs = (ChannelServer)var2.next();
                Iterator var4 = cs.getMapFactory().getAllMapThreadSafe().iterator();

                while(var4.hasNext()) {
                    MapleMap map = (MapleMap)var4.next();
                    Iterator var6 = map.getAllMerchant().iterator();

                    while(var6.hasNext()) {
                        MapleMapObject obj = (MapleMapObject)var6.next();
                        if (obj instanceof IMaplePlayerShop) {
                            IMaplePlayerShop ips = (IMaplePlayerShop)obj;
                            if (obj instanceof HiredMerchant) {
                                HiredMerchant merchant1 = (HiredMerchant)ips;
                                if (merchant1 != null && merchant1.getShopType() == 1 && merchant1.isOwner(this) && merchant1.isAvailable()) {
                                    this.是否开店 = true;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }

            this.是否开店 = false;
            return false;
        }
    }
    public boolean 是否开店() {
        return this.是否开店;
    }

    public int 读取开店经验加成() {
        return this.是否开店 ? (Integer)LtMS.ConfigValuesMap.get("开店经验加成") : 0;
    }

    public void 开启储备经验() {
        this.是否储备经验 = true;
    }

    public void 关闭储备经验() {
        this.是否储备经验 = false;
    }

    public boolean 是否储备经验() {
        return this.是否储备经验;
    }
    public boolean getSuperTransformation() {
        return this.superTransformation;
    }
    public void setSuperTransformation(boolean superTransformation) {
        this.superTransformation = superTransformation;
    }

    public long getLastSuperTransformationTime() {
        return this.lastSuperTransformationTime;
    }

    public void setLastSuperTransformationTime(long lastSuperTransformationTime) {
        this.lastSuperTransformationTime = lastSuperTransformationTime;
    }
    public void setCloneDamagePercentage(int cloneDamagePercentage) {
        this.cloneDamagePercentage = cloneDamagePercentage;
    }

    public int getCloneDamagePercentage() {
        return this.cloneDamagePercentage;
    }

    public boolean isSellWhenPickUpItem(int itemId) {
        return this.sellWhenPickUpItemList.contains(itemId);
    }
    public ArrayList<Integer> getSellWhenPickUpItemList() {
        return this.sellWhenPickUpItemList;
    }

    public void setSellWhenPickUpItemList(ArrayList<Integer> sellWhenPickUpItemList) {
        this.sellWhenPickUpItemList = sellWhenPickUpItemList;
    }

    public void addSellWhenPickUpItem(int itemId) {
        if (!this.sellWhenPickUpItemList.contains(itemId)) {
            this.sellWhenPickUpItemList.add(itemId);

            try {
                Connection con = DBConPool.getConnection();
                Throwable var3 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO snail_sell_pickup_items (character_id, item_id) VALUES (?, ?)");
                    ps.setInt(1, this.id);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var13) {
                    var3 = var13;
                    throw var13;
                } finally {
                    if (con != null) {
                        if (var3 != null) {
                            try {
                                con.close();
                            } catch (Throwable var12) {
                                var3.addSuppressed(var12);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var15) {
                服务端输出信息.println_err("【错误】addSellWhenPickUpItem错误，原因：" + var15);
                var15.printStackTrace();
            }
        }

    }

    public void deleteSellWhenPickUpItem(int itemId) {
        if (this.sellWhenPickUpItemList.contains(itemId)) {
            Iterator iter = this.sellWhenPickUpItemList.iterator();

            while(iter.hasNext()) {
                if (iter.next().equals(itemId)) {
                    iter.remove();
                }
            }

            try {
                Connection con = DBConPool.getConnection();
                Throwable var4 = null;

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM snail_sell_pickup_items WHERE character_id = ? and item_id = ?");
                    ps.setInt(1, this.id);
                    ps.setInt(2, itemId);
                    ps.executeUpdate();
                    ps.close();
                } catch (Throwable var14) {
                    var4 = var14;
                    throw var14;
                } finally {
                    if (con != null) {
                        if (var4 != null) {
                            try {
                                con.close();
                            } catch (Throwable var13) {
                                var4.addSuppressed(var13);
                            }
                        } else {
                            con.close();
                        }
                    }

                }
            } catch (SQLException var16) {
                服务端输出信息.println_err("【错误】addSellWhenPickUpItem错误，原因：" + var16);
                var16.printStackTrace();
            }
        }

    }
    public void loadSellWhenPickUpItemListFromDB(Connection con) {
        try {
            if (con == null || con.isClosed()) {
                con = DBConPool.getConnection();
            }

            ArrayList<Integer> ret = new ArrayList<>();
            PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_sell_pickup_items WHERE character_id = ?");
            ps.setInt(1, this.id);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                ret.add(rs.getInt("item_id"));
            }

            LinkedHashSet<Integer> set = new LinkedHashSet<>(ret);
            ArrayList<Integer> ret0 = new ArrayList<>(set);
            rs.close();
            ps.close();
            ret.clear();
            set.clear();
            this.sellWhenPickUpItemList.clear();
            this.sellWhenPickUpItemList = ret0;
        } catch (SQLException var7) {
            服务端输出信息.println_err("【错误】loadSellWhenPickUpItemListFromDB错误，原因：" + var7);
            var7.printStackTrace();
        }

    }
    public void loadSellWhenPickUpItemListFromDB() {
        this.loadSellWhenPickUpItemListFromDB((Connection)null);
    }

    public void saveSellWhenPickUpItemListToDB(Connection con) {
        try {
            if (con == null) {
                con = DBConPool.getConnection();
            }

            ArrayList<Integer> ret = new ArrayList(this.sellWhenPickUpItemList);
            PreparedStatement ps = con.prepareStatement("DELETE FROM snail_sell_pickup_items WHERE character_id = ?");
            ps.setInt(1, this.id);
            ps.executeUpdate();
            ps = con.prepareStatement("INSERT INTO snail_sell_pickup_items (character_id, item_id) VALUES (?, ?)");
            ps.setInt(1, this.id);
            Iterator var4 = ret.iterator();

            while(var4.hasNext()) {
                int itemId = (Integer)var4.next();
                ps.setInt(2, itemId);
                ps.executeUpdate();
            }

            ps.close();
            ret.clear();
        } catch (SQLException var6) {
            服务端输出信息.println_err("【错误】saveSellWhenPickUpItemListToDB 错误，原因：" + var6);
            var6.printStackTrace();
        }

    }

    public void setOneTimeStringLog(String logName, String logVal) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var4 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT id FROM snail_onetime_string_log WHERE characterid = ? AND log_name = ?");
                ps.setInt(1, this.id);
                ps.setString(2, logName);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    ps = con.prepareStatement("UPDATE snail_onetime_string_log SET log_val = ? WHERE id = ?");
                    ps.setString(1, logVal);
                    ps.setInt(2, rs.getInt(1));
                    ps.executeUpdate();
                } else {
                    ps = con.prepareStatement("INSERT INTO snail_onetime_string_log (characterid, log_name, log_val) VALUES (?,?,?)");
                    ps.setInt(1, this.id);
                    ps.setString(2, logName);
                    ps.setString(3, logVal);
                    ps.executeUpdate();
                }

                ps.close();
                rs.close();
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (con != null) {
                    if (var4 != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var17) {
            服务端输出信息.println_err("【错误】setOneTimeStringLog错误，原因：" + var17);
            var17.printStackTrace();
        }

    }
    public String getOneTimeStringLog(String logName) {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var3 = null;

            String var7;
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM snail_onetime_string_log WHERE characterid = ? AND log_name = ?");
                ps.setInt(1, this.id);
                ps.setString(2, logName);
                ResultSet rs = ps.executeQuery();
                String ret;
                if (!rs.next()) {
                    ps.close();
                    rs.close();
                    ret = "";
                    return ret;
                }

                ret = rs.getString("log_val");
                ps.close();
                rs.close();
                var7 = ret;
            } catch (Throwable var18) {
                var3 = var18;
                throw var18;
            } finally {
                if (con != null) {
                    if (var3 != null) {
                        try {
                            con.close();
                        } catch (Throwable var17) {
                            var3.addSuppressed(var17);
                        }
                    } else {
                        con.close();
                    }
                }

            }

            return var7;
        } catch (SQLException var20) {
            服务端输出信息.println_err("【错误】getOneTimeStringLog错误，原因：" + var20);
            var20.printStackTrace();
            return "";
        }
    }


    public void setWeekLog(String bossid, int count0) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setWeekLog(this.getId(), bossid, count0);
        }
    }

    public void setWeekLog(String bossid) {
        this.setWeekLog(bossid, 1);
    }

    public int getWeekLog(String bossid) {
        return TimeLogCenter.getInstance().getWeekLog(this.getId(), bossid);
    }

    public void deleteWeekLog(String bossid) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().deleteWeekLogAll(this.getId(), bossid);
        }
    }

    public void setWeekLoga(String bossid, int count0) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setWeekLoga(this.getAccountID(), bossid, count0);
        }
    }

    public void setWeekLoga(String bossid) {
        this.setWeekLoga(bossid, 1);
    }

    public int getWeekLoga(String bossid) {
        return TimeLogCenter.getInstance().getWeekLoga(this.getAccountID(), bossid);
    }

    public void deleteWeekLoga(String bossid) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().deleteWeekLogaAll(this.getAccountID(), bossid);
        }
    }

    public void setMonthLog(String bossid, int count0) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setMonthLog(this.getId(), bossid, count0);
        }
    }

    public void setMonthLog(String bossid) {
        this.setMonthLog(bossid, 1);
    }

    public int getMonthLog(String bossid) {
        return TimeLogCenter.getInstance().getMonthLog(this.getId(), bossid);
    }

    public void deleteMonthLog(String bossid) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().deleteMonthLogAll(this.getId(), bossid);
        }
    }

    public void setMonthLoga(String bossid, int count0) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().setMonthLoga(this.getAccountID(), bossid, count0);
        }
    }

    public void setMonthLoga(String bossid) {
        this.setMonthLoga(bossid, 1);
    }

    public int getMonthLoga(String bossid) {
        return TimeLogCenter.getInstance().getMonthLoga(this.getAccountID(), bossid);
    }

    public void deleteMonthLoga(String bossid) {
        if (!this.isClone()) {
            TimeLogCenter.getInstance().deleteMonthLogaAll(this.getAccountID(), bossid);
        }
    }
//
//    public boolean 是否防滑() {
//        return this.是否防滑;
//    }
//
//    public void 开启防滑() {
//        this.是否防滑 = true;
//    }
//
//    public void 关闭防滑() {
//        this.是否防滑 = false;
//    }


    public MapleCharacter fakeLooks() {
        return this.fakeLooks(100);
    }

    public MapleCharacter fakeLooks(int cloneDamagePercentage) {
        MapleClient cloneClient = new MapleClient((MapleAESOFB)null, (MapleAESOFB)null, new MockIOSession());
        int minus = this.getId() + 10000000;
        MapleCharacter ret = new MapleCharacter(true);
        ret.fake = true;
        ret.fakeOwnerId = this.id;
        ret.cloneDamagePercentage = cloneDamagePercentage;
        ret.id = minus;
        ret.client = cloneClient;
        ret.exp = 0;
        ret.meso = 0;
        ret.beans = this.beans;
        ret.todayOnlineTime = this.todayOnlineTime;
        ret.totalOnlineTime = this.totalOnlineTime;
        ret.max_damage = this.max_damage;
        ret.exp_reserve = this.exp_reserve;
        ret.blood = this.blood;
        ret.month = this.month;
        ret.day = this.day;
        ret.charmessage = this.charmessage;
        ret.expression = this.expression;
        ret.constellation = this.constellation;
        ret.remainingAp = 0;
        ret.fame = 0;
        ret.accountid = this.client.getAccID();
        ret.name = this.name;
        ret.level = this.level;
        ret.fame = this.fame;
        ret.job = this.job;
        ret.hair = this.hair;
        ret.face = this.face;
        ret.skinColor = this.skinColor;
        ret.bookCover = this.bookCover;
        ret.monsterbook = this.monsterbook;
        ret.mount = this.mount;
        ret.CRand = new PlayerRandomStream();
        ret.gmLevel = this.gmLevel;
        ret.gender = this.gender;
        ret.mapid = this.map.getId();
        ret.map = this.map;
        ret.setStance(this.getStance());
        ret.chair = this.chair;
        ret.itemEffect = this.itemEffect;
        ret.guildid = this.guildid;
        ret.currentrep = this.currentrep;
        ret.totalrep = this.totalrep;
        ret.stats = this.stats;
        ret.effects.putAll(this.effects);
        if (ret.effects.get(MapleBuffStat.ILLUSION) != null) {
            ret.effects.remove(MapleBuffStat.ILLUSION);
        }

        if (ret.effects.get(MapleBuffStat.SUMMON) != null) {
            ret.effects.remove(MapleBuffStat.SUMMON);
        }

        if (ret.effects.get(MapleBuffStat.PUPPET) != null) {
            ret.effects.remove(MapleBuffStat.PUPPET);
        }

        ret.guildrank = this.guildrank;
        ret.allianceRank = this.allianceRank;
        ret.hidden = this.hidden;
        ret.setPosition(new Point(this.getPosition()));
        Iterator var5 = this.getInventory(MapleInventoryType.EQUIPPED).iterator();

        while(var5.hasNext()) {
            IItem equip = (IItem)var5.next();
            ret.getInventory(MapleInventoryType.EQUIPPED).addFromDB(equip);
        }

        ret.skillMacros = this.skillMacros;
        ret.keylayout = this.keylayout;
        ret.questinfo = this.questinfo;
        ret.savedLocations = this.savedLocations;
        ret.wishlist = this.wishlist;
        ret.rocks = this.rocks;
        ret.regrocks = this.regrocks;
        ret.buddylist = this.buddylist;
        ret.keydown_skill = 0L;
        ret.lastmonthfameids = this.lastmonthfameids;
        ret.lastfametime = this.lastfametime;
        ret.storage = this.storage;
        ret.cs = this.cs;
        ret.client.setAccountName(this.client.getAccountName());
        ret.maplepoints = this.maplepoints;
        ret.clone = true;
        ret.client.setChannel(this.client.getChannel());
        ret.max_damage = this.max_damage;
        ret.exp_reserve = this.exp_reserve;
        ret.guildPoints = this.guildPoints;

        for(ret.breakLevel = this.breakLevel; this.map.getCharacterById(ret.id) != null || this.client.getChannelServer().getPlayerStorage().getCharacterById(ret.id) != null; ++ret.id) {
        }

        ret.client.setPlayer(ret);
        return ret;
    }

    public void sendMobSkill(int skillId, int level) {
        MobSkill mobSkill = MobSkillFactory.getMobSkill(skillId, level);
        MapleMonster monster = null;
        Point chrPoint = this.getPosition();
        int distance = 999999999;
        Iterator var7 = this.getMap().getAllMonstersThreadsafe().iterator();

        while(var7.hasNext()) {
            MapleMonster mob = (MapleMonster)var7.next();
            int d = (int)Math.pow(Math.pow((double)(mob.getPosition().x - chrPoint.x), 2.0) + Math.pow((double)(mob.getPosition().y - chrPoint.y), 2.0), 0.5);
            if (distance > d) {
                distance = d;
                monster = mob;
            }
        }

        if (monster != null && mobSkill != null && !mobSkill.checkCurrentBuff(this, monster)) {
            mobSkill.applyEffect(this, monster, true);
        }

    }
    public void burn(final short damagePercent, final int duration) {
        if (!this.isburnd) {
            if (!this.isHidden()) {
                ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
                singleThreadExecutor.execute(new Runnable() {
                    public void run() {
                        try {
                            if (MapleCharacter.this.getHp() == 0) {
                                return;
                            }

                            MapleCharacter.this.dropMessage(6, "你被灼烧了，接下来 " + duration / 1000 + " 秒将持续进入掉血掉蓝状态。");
                            MapleCharacter.this.isburnd = true;
                            int count = duration / 1000;
                            int damageHp = MapleCharacter.this.stats.getCurrentMaxHp() * damagePercent / 100;
                            int damageMp = MapleCharacter.this.stats.getCurrentMaxMp() * damagePercent / 100;

                            for(int i = 0; i < count && MapleCharacter.this.getHp() != 0; ++i) {
                                int newHp = MapleCharacter.this.getHp() - damageHp;
                                int newMp = MapleCharacter.this.getMp() - damageMp;
                                if (newHp <= 0) {
                                    newHp = 1;
                                }

                                if (newMp <= 0) {
                                    newMp = 1;
                                }

                                MapleCharacter.this.stats.setHp(newHp);
                                MapleCharacter.this.stats.setMp(newMp);
                                Map<MapleStat, Integer> hpmpupdate = new EnumMap(MapleStat.class);
                                hpmpupdate.put(MapleStat.MP, newMp);
                                hpmpupdate.put(MapleStat.HP, newHp);
                                MapleCharacter.this.getClient().sendPacket(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, MapleCharacter.this));
                                Thread.sleep(1000L);
                                if (Game.主城(MapleCharacter.this.getMapId()) && i >10) {
                                    return;
                                }
                            }

                            MapleCharacter.this.dropMessage(6, "随着时间的流逝，你身上的火焰逐渐减弱并消失了。");
                            MapleCharacter.this.isburnd = false;

                        } catch (InterruptedException var8) {
                            服务端输出信息.println_err("burn 子线程错误，错误原因： " + var8);
                        }

                    }
                });
            }

        }
    }


    public void killSelf() {

        this.setHp(0);
        this.updateSingleStat(MapleStat.HP, 0);
    }
}
