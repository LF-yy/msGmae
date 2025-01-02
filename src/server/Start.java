package server;

import bean.*;
import bean.SkillType;
import client.LoginCrypto;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.MaplePet;
import client.inventory.OnlyID;
import com.alibaba.fastjson.JSONObject;
import constants.*;
import database.DBConPool;
import database.DatabaseConnection;
import gui.*;
import gui.控制台.聊天记录显示;
import gui.控制台.脚本更新器;
import handling.cashshop.CashShopServer;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.channel.handler.AttackInfo;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.MapleParty;
import handling.world.World;
import handling.world.World.Broadcast;
import handling.world.family.MapleFamilyBuff;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import merchant.merchant_main;
import org.apache.commons.lang.StringUtils;
import server.Timer.BuffTimer;
import server.Timer.CheatTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.MobTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import server.custom.forum.Forum_Section;
import snail.*;
import server.events.MapleOxQuizFactory;
import server.life.*;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.quest.MapleQuest;
import tools.*;
import tools.packet.UIPacket;
import util.RedisUtil;

import javax.script.Invocable;
import javax.script.ScriptEngine;

public class Start
{
    private static ServerSocket srvSocket;
    private static int srvPort;

    public static long startTime;
    public static Start instance;
    private static int maxUsers;
    private int rankTime;
    private boolean ivCheck;
    public static boolean 是否控制台启动;
   // public static Map<String, Integer> ConfigValuesMap;
    private static int 记录在线时间;
    private static int 世界BOSS刷新记录;
    private static int 更新时间;
    private static int 更新时间1;
    private static int 更新时间2;
    private static int 更新时间3;

    private static int 喜从天降;
    private static Boolean 倍率活动;
    private static Boolean 幸运职业;
    private static Boolean 魔族入侵;
    private static Boolean isClearBossLog;
    private static Boolean 魔族攻城;
    private static int Z;
    public static int 福利泡点;
    private static int 回收内存;
    //套装伤害表
    public static List<Pair<Integer, Pair<String, Pair<String, Integer>>>> 套装加成表;
    public static Map<String, Double>   新套装加成表;
    public static Map<Integer, Integer>   初始化技能等级;
    public static Map<Integer, Integer>   转职5;
    public static Map<Integer, Integer> skillProp;
    public static Map<Integer, Map<String, Integer>> 双爆加成;
    public static List<SkillType> skillType;

    public static List<Pair<String, Integer>> exptable;
    public static List<BossInMap> 野外boss刷新;
    public static List<String> 子弹列表;
    public static List<SuitSystem> suitSystems;
    public static List<FieldSkills> fieldSkills;
    public static List<SuperSkills> superSkills;
    public static List<MobInfo> mobInfo;
    public static List<Integer> NotParticipatingRecycling;
    public static Map<String, List<SuitSystem>> suitSystemsMap;
    //光环
    public static Map<Integer, List<FieldSkills>> fieldSkillsMap;
    public static Map<Integer, List<AttackInfo>> allAttackInfo;
    public static Map<Integer, List<LtMobSpawnBoss>> ltMobSpawnBoss;
    //自定义超级技能
    public static Map<Integer, List<SuperSkills>> superSkillsMap;
    public static Map<Integer, List<FiveTurn>>  fiveTurn;
    public static Map<Integer, List<BreakthroughMechanism>>  breakthroughMechanism;
    public static Map<Integer, List<Leveladdharm>>  leveladdharm;
    public static Map<Integer, List<MobInfo>>  mobInfoMap;
    public static Map<Integer, Integer>  dropCoefficientMap;
    public static Map<Integer, Integer>  jobDamageMap;
    public static Map<Integer, MapleGuildRanking.SponsorRank>  accurateRankMap;
    public static Map<Integer, MapleGuildRanking.SponsorRank>  enhancedRankMap;
    public static Map<Integer, MapleGuildRanking.SponsorRank>  dropRankMap;

    public static Map<Long, List<LttItemAdditionalDamage>>  additionalDamage;
    public static Map<Integer, Integer> ltSkillWucdTable;

    public static List<Integer> mobUnhurtList;
    public static List<MonsterGlobalDropEntry> globaldrops;
    public static List<MonsterDropEntry> drops;
    public static List<MonsterDropEntry> dropsTwo;
    public static List<LtDiabloEquipments> ltDiabloEquipments;
    public static Map<Integer,List<ItemInfo>> itemInfo;
    public static Map<Integer,List<LtMonsterSkill>> ltMonsterSkillAttackSkill;
    public static List<LtMonsterSkill> ltMonsterSkillBuffSkill;

    public static List<LtMxdPrize> ltMxdPrize;
    public static List<LtZlTask> ltZlTask;
    public static Map<Integer, String[]> FuMoInfoMap = new HashMap();
    public static Map<Integer, String[]> potentialListMap = new HashMap();
    public static Map<Integer, List<MonsterDropEntry>> dropsMap;
    public static Map<Integer, List<MonsterDropEntry>> dropsMapTwo;
    public static volatile int dropsFalg;
    public static Map<Integer, Integer> dropsMapCount;
    public static Map<Integer, Integer> dropsMapCountTwo;
    public static Map<String , File> in ;
    public static Map<String , ScriptEngine> se ;
    public static Map<String , Invocable> iv ;

    public  static Map<Integer, UserAttraction> 吸怪集合 = new Hashtable<>();
    public  static List<String> 特殊宠物吸物无法使用地图;
    public  static Map<Integer, UserLhAttraction> 轮回集合 = new Hashtable<>();
    public  static Map<Integer, List<MapleMonster>> 轮回怪物 = new Hashtable<>();
    public static Map<String, Integer> 地图吸怪检测 = new HashMap();
    public static Map<String, Integer> 技能范围检测 = new HashMap();
    public static Map<String, Integer> PVP技能伤害 = new HashMap();
    public static Map<String, Integer> 个人信息设置 = new HashMap();
    public static 脚本更新器 更新通知;
    public static 聊天记录显示 信息输出;
    public static client.DebugWindow DebugWindow;
    private static int chrCountOfReward = 0;
    public static boolean isDreamClient = ServerProperties.getProperty("server.settings.dreamClient", false);
    private static long lastResetDropsTime = 0L;

    private static int 本地取广播 = 0;
    public static Map<String, String> CloudBlacklist = new HashMap();
    private static int 定时查询 = 0;
    public static int 异常警告 = 0;
    public static int 福利泡点2 = 0;
    private static int 自动存档 = 0;
    private static int 回收地图 = 0;
    private static int 关系验证 = 0;
    private static int 检测服务端版本 = 0;
    private static int 检测服务端版本1 = 0;
    private static int 统计最高在线人数 = 0;
    private static int 最高在线人数 = 0;
    private static int 最高在线人数2 = 0;
    private static float oldExpRate = 1.0F;
    private static float oldExpRateWeek = 1.0F;
    public static boolean 双倍爆率开关 = false;
    public static boolean 周末双倍爆率开关 = false;
    private static float oldMesoRate = 1.0F;
    private static float oldMesoRateWeek = 1.0F;
    private static float oldDropRate = 1.0F;
    private static float oldDropRateWeek = 1.0F;
    public static boolean 低保发放开关 = false;
    public static int 初始通缉令 = 0;
    public static Boolean 每日送货 = false;
    public static boolean 双倍经验开关 = false;
    public static boolean 周末双倍经验开关 = false;
    public static boolean OxQuizRunning = false;
    public static boolean AutoGameRunning = false;
    public static Map<String, ArrayList> RewardIDMap = new HashMap();
    public static Map<String, ArrayList> RewardChanceMap = new HashMap();
    public static Map<String, ArrayList> RewardNameMap = new HashMap();
    public static Map<String, ArrayList> RewardAnnouncementMap = new HashMap();

    public static List<String> 授权IP = Arrays.asList("1.15.43.65","175.24.182.189","202.189.5.75","106.54.24.67","159.75.177.122","58.220.33.222","27.25.141.183","222.186.34.45","101.34.216.55","111.229.164.192","110.41.70.201","180.97.189.26","103.91.211.216","103.91.211.234","110.41.70.201","222.186.134.23");
    public static int 计数器 = 0;
    public static int 删除标记 = 0;
    public static void 刷新抽奖物品() {
        RewardIDMap.clear();
        RewardChanceMap.clear();
        RewardAnnouncementMap.clear();
        RewardNameMap.clear();
        ArrayList<String> groups = new ArrayList();
        ArrayList<Integer> rewardIDs = new ArrayList();
        ArrayList<String> names = new ArrayList();
        ArrayList<Double> chances = new ArrayList();
        ArrayList<Integer> announcements = new ArrayList();

        try {
            Connection con = DBConPool.getConnection();
            Throwable var6 = null;

            try {
                PreparedStatement ps = null;
                ResultSet rs = null;
                ps = con.prepareStatement("SELECT * FROM 抽奖管理 WHERE itemid !=0");
                rs = ps.executeQuery();

                while(rs.next()) {
                    String s_group = rs.getString("groups");
                    if (!groups.contains(s_group)) {
                        groups.add(s_group);
                    }
                }

                Iterator var22 = groups.iterator();

                while(var22.hasNext()) {
                    String group = (String)var22.next();
                    ps = con.prepareStatement("SELECT * FROM 抽奖管理 WHERE groups = ?");
                    ps.setString(1, group);
                    rs = ps.executeQuery();
                    rewardIDs.clear();
                    names.clear();
                    chances.clear();
                    announcements.clear();

                    while(rs.next()) {
                        rewardIDs.add(rs.getInt("itemid"));
                        names.add(rs.getString("itemname"));
                        chances.add(rs.getDouble("chance"));
                        announcements.add(rs.getInt("announcement"));
                    }

                    RewardIDMap.put(group, new ArrayList(rewardIDs));
                    RewardChanceMap.put(group, new ArrayList(chances));
                    RewardAnnouncementMap.put(group, new ArrayList(announcements));
                    RewardNameMap.put(group, new ArrayList(names));
                }
            } catch (Throwable var19) {
                var6 = var19;
                throw var19;
            } finally {
                if (con != null) {
                    if (var6 != null) {
                        try {
                            con.close();
                        } catch (Throwable var18) {
                            var6.addSuppressed(var18);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var21) {
            服务端输出信息.println_err(var21);
            FileoutputUtil.outError("logs/资料库异常.txt", var21);
        }

    }
    public static void main(final String[] args) {

//            final String name = null;
//            final int id = 0;
//            final int vip = 0;
//            final int size = 0;
            try (Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
                 final PreparedStatement ps = con.prepareStatement("UPDATE accounts SET loggedin = 0")) {
                ps.executeUpdate();
            }
            catch (SQLException ex) {
                FileoutputUtil.outError("logs/数据库异常.txt", (Throwable)ex);
                throw new RuntimeException("【错误】 请确认数据库是否正常链接");
            }
            GetConfigValues();
            System.out.println("◇ -> 正在启动LtMs079");
            System.out.println("◇ -> 版本信息:ver0.1");
            System.out.println("◇ -> 正在读取授权码请稍后");

        //启动轮播(50);
            //进行授权校验();
            System.out.println("◇ -> 授权码读取完毕");
            final long startQuestTime = System.currentTimeMillis();

            System.out.println("\r\n◇ -> 开始加载各项游戏数据");
            if (WorldConstants.ADMIN_ONLY) {
                System.out.println("◇ -> 只允许管理员登录模式开关: 开启");
            }
            else {
                System.out.println("◇ -> 只允许管理员登录模式开关: 关闭");
            }
            if (ServerConfig.AUTO_REGISTER) {
                System.out.println("◇ -> 账号自动注册模式开关: 开启");
            }
            else {
                System.out.println("◇ -> 账号自动注册模式开关: 关闭");
            }
            if (!WorldConstants.GMITEMS) {
                System.out.println("◇ -> 允许玩家使用管理员物品开关: 开启");
            }
            else {
                System.out.println("◇ -> 允许玩家使用管理员物品开关: 关闭");
            }

            MapleParty.怪物倍怪 = true;
            MapleParty.怪物倍率 = 1;
            //自定义配置加载
                Start.GetSuitDamTable();
                Start.GetSuitDamTableNew();
                Start.GetLtInitializationSkills();
                Start.getDropCoefficient();
                Start.getJobDamage();
                Start.getDsTableInfo();
               // Start.GetSuitSystem();
                Start.GetfiveTurn();
                Start.setLtMxdPrize();
                Start.setLtZlTask();
                Start.GetBreakthroughMechanism();
                Start.getleveladdharm();
                Start.getAdditionalDamage();
                Start.getNotParticipatingRecycling();
                Start.GetSuperSkills();
                Start.GetFieldSkills();
                Start.getAttackInfo();
                Start.getMobInfo();
                Start.setLtSkillWucdTable();
                Start.getLtMobSpawnBoss();
                Start.getMobUnhurt();
                Start.findLtMonsterSkill();

        //系统配置加载
            ServerConfig.loadSetting();
            World.init();
            World.monitorDurationMonster(10);
            WorldTimer.getInstance().start();
            EtcTimer.getInstance().start();
            MapTimer.getInstance().start();
            MobTimer.getInstance().start();
            CloneTimer.getInstance().start();
            EventTimer.getInstance().start();
            BuffTimer.getInstance().start();
            PingTimer.getInstance().start();
            LoginInformationProvider.getInstance();
            FishingRewardFactory.getInstance();
            MapleQuest.initQuests();
            MapleLifeFactory.loadQuestCounts();
            MapleOxQuizFactory.getInstance().initialize();
            MapleItemInformationProvider.getInstance().load();
            PackageOfEquipments.getInstance().loadFromDB();
            服务端输出信息.println_out("○ 开始加载技能皮肤列表");
            SkillSkin.loadSkillList();
            PredictCardFactory.getInstance().initialize();
            CashItemFactory.getInstance().initialize();
            RandomRewards.getInstance();
            SkillFactory.LoadSkillInformaion();
            ServerConfig.loadChannelExpRateMap();
            ServerConfig.loadChannelMesoRateMap();
            ServerConfig.loadChannelDropRateMap();
            ServerConfig.loadChannelNeedItemMap();
            GameConstants.loadMultiOnlyItemList();
            MapleCarnivalFactory.getInstance();
            System.out.println("◇ -> 游戏商品数量: " + 服务器游戏商品() + " 个");
            System.out.println("◇ -> 商城商品数量: " + 服务器商城商品() + " 个");
            System.out.println("◇ -> 玩家账号数量: " + 服务器账号() + " 个");
            System.out.println("◇ -> 玩家角色数量: " + 服务器角色() + " 个");
            System.out.println("◇ -> 玩家道具数量: " + 服务器道具() + " 个");
            System.out.println("◇ -> 玩家技能数量: " + 服务器技能() + " 个");

            System.out.println("◇ -> 启动记录在线时长线程");
            System.out.println("◇ -> 启动服务端内存回收线程");
            System.out.println("◇ -> 启动服务端地图回收线程");
            System.out.println("◇ -> 处理怪物重生、CD、宠物、坐骑");
            System.out.println("◇ -> 自定义玩家NPC");
            MapleGuildRanking.getInstance().getGuildRank();
            MapleGuildRanking.getInstance().getJobRank(1);
            MapleGuildRanking.getInstance().getJobRank(2);
            MapleGuildRanking.getInstance().getJobRank(3);
            MapleGuildRanking.getInstance().getJobRank(4);
            MapleGuildRanking.getInstance().getJobRank(5);
            MapleGuildRanking.getInstance().getJobRank(6);
            MapleFamilyBuff.getBuffEntry();
            System.out.println("\r\n[加载设置] -> 游戏倍率信息");
            if (Integer.parseInt(ServerProperties.getProperty("LtMS.expRate")) > 1000) {
                System.out.println("游戏经验倍率: 1000 倍 (上限)");
            }
            else {
                System.out.println("游戏经验倍率: " + Integer.parseInt(ServerProperties.getProperty("LtMS.expRate")) + " 倍 ");
            }
            if (Integer.parseInt(ServerProperties.getProperty("LtMS.dropRate")) > 100) {
                System.out.println("游戏物品倍率：100 倍 (上限)");
            }
            else {
                System.out.println("游戏物品倍率：" + Integer.parseInt(ServerProperties.getProperty("LtMS.dropRate")) + " 倍 ");
            }
            if (Integer.parseInt(ServerProperties.getProperty("LtMS.mesoRate")) > 100) {
                System.out.println("游戏金币倍率：100 倍 (上限)");
            }
            else {
                System.out.println("游戏金币倍率：" + Integer.parseInt(ServerProperties.getProperty("LtMS.mesoRate")) + " 倍 ");
            }
            System.out.println("\r\n[加载设置] -> 游戏端口配置");
            merchant_main.getInstance().load_data();
            LoginServer.setup();
            ChannelServer.startAllChannels();
            CashShopServer.setup();
            CheatTimer.getInstance().register((Runnable)AutobanManager.getInstance(), 60000L);
            Runtime.getRuntime().addShutdownHook(new Thread((Runnable)ShutdownServer.getInstance()));
            SpeedRunner.getInstance().loadSpeedRuns();
            World.registerRespawn();
            PlayerNPC.loadAll();
            LoginServer.setOn();
            MapleMapFactory.loadCustomLife();
            //checkCPU(10);
            DamageManage.getInstance().loadMobDamageDataListFromDB();
            loadExtraMobInMapId();
            loadLogFromDB();
            刷新抽奖物品();
            GetFuMoInfo();
            loadPotentialMap();
            读取技能范围检测();
            读取技能PVP伤害();
            读取技个人信息设置();
            重置仙人数据();
        WorldConstants.importantItemsBroadcast = (Integer)LtMS.ConfigValuesMap.get("重要道具掉落广播") > 0;

        Forum_Section.loadAllSection();
            //读取地图吸怪检测();
            System.out.println("[启动角色福利泡点线程]");
           // 福利泡点(2);
            自动存档(10);
            在线时间(1);
            回收内存(300);
            回收地图(180);
            //在线统计(10);
            循环线程(1);
            //定时重载爆率(10);
            //吸怪检测(3);
            World.isShutDown = false;
            OnlyID.getInstance();
            //清理复制道具
            System.out.println("◇ -> 检测游戏复制道具系统");
            checkCopyItemFromSql();
            System.out.println("◇ -> 加载赋能系统");

            服务端输出信息.println_out("【启动中】 加载每日泡点、每日重置:::");
            //World.GainZx(Zx_time);
            // ZaiXian(1);
            服务端输出信息.println_out("○ 开始加载野外随机BOSS");
            World.outsideBoss(1);
             WorldBoss.loadFromDB();
            AutoSaveSkillSkinMap(30);
            tzjc.sr_tz();
            setdrops();
            setdropsTwo();
            getAllItemInfo();
            //暗黑破坏神玩法词条加载
            setLtDiabloEquipments();
            //PK
        GameConstants.loadPKChannelList();
        GameConstants.loadPKGuildChannelList();
        GameConstants.loadPKPlayerMapList();
        GameConstants.loadPKPartyMapList();
        GameConstants.loadPKGuildMapList();
        GameConstants.loadPKBanSkillsList();
        GameConstants.loadPKDropItemsList();
        GameConstants.loadPKDropItemsList2();
        if (!授权IP.contains(ServerConfig.IP)){
            ServerConfig.setUserlimit(5);
            启动轮播(50);
        }

            System.out.println("[所有游戏数据加载完毕] ");
            System.out.println("[LtMs079服务端已启动完毕，耗时 " + (System.currentTimeMillis() - startQuestTime) / 1000L + " 秒]");
            System.out.println("[温馨提示]运行中请勿直接关闭本控制台，使用下方关闭服务器按钮来关闭服务端，否则回档自负\r\n");
            System.out.println("◇ -> 服务器启动成功");

    }
    public static void AutoSaveSkillSkinMap(int time) {
        服务端输出信息.println_out("【读取中】 加载自动存储技能皮肤列表:::");
        WorldTimer.getInstance().register(new Runnable() {
            public void run() {
                服务端输出信息.println_out("【技能皮肤】自动保存至数据库。。。");
                int mount = SkillSkin.saveChrSkillMapToDB();
                服务端输出信息.println_out("【技能皮肤】成功保存 " + mount + " 个玩家的皮肤。");
                服务端输出信息.println_out("【世界BOSS】自动保存至数据库。。。");
                boolean success = WorldBoss.saveToDB();
                if (success) {
                    服务端输出信息.println_out("【世界BOSS】保存成功");
                } else {
                    服务端输出信息.println_out("【世界BOSS】保存失败");
                }

            }
        }, (long)('\uea60' * time));
    }
    public static void loadLogFromDB() {
        服务端输出信息.println_out("○ 开始加载日志信息");

        try {
            TimeLogCenter.getInstance().loadBossLogFromDB();
            TimeLogCenter.getInstance().loadBossLogaFromDB();
            TimeLogCenter.getInstance().loadOneTimeLogFromDB();
            TimeLogCenter.getInstance().loadOneTimeLogaFromDB();
            TimeLogCenter.getInstance().loadWeekLogFromDB();
            TimeLogCenter.getInstance().loadWeekLogaFromDB();
            TimeLogCenter.getInstance().loadMonthLogFromDB();
            TimeLogCenter.getInstance().loadMonthLogaFromDB();
        } catch (Exception var1) {
            服务端输出信息.println_err("【错误】loadLogFromDB错误，错误原因：" + var1);
            var1.printStackTrace();
        }

        服务端输出信息.println_out("○ 日志信息加载完成");
    }
    private static void loadExtraMobInMapId() {
        MapleMapFactory.addMobInMapId(2220000, 104000400);
        MapleMapFactory.addMobInMapId(3220000, 101030404);
        MapleMapFactory.addMobInMapId(5220001, 110040000);
        MapleMapFactory.addMobInMapId(7220000, 250010304);
        MapleMapFactory.addMobInMapId(8220000, 200010300);
        MapleMapFactory.addMobInMapId(7220002, 250010503);
        MapleMapFactory.addMobInMapId(7220001, 222010310);
        MapleMapFactory.addMobInMapId(6220000, 107000300);
        MapleMapFactory.addMobInMapId(5220002, 100040105);
        MapleMapFactory.addMobInMapId(5220002, 100040106);
        MapleMapFactory.addMobInMapId(5220003, 220050100);
        MapleMapFactory.addMobInMapId(6220001, 221040301);
        MapleMapFactory.addMobInMapId(8220003, 240040401);
        MapleMapFactory.addMobInMapId(3220001, 260010201);
        MapleMapFactory.addMobInMapId(8220002, 261030000);
        MapleMapFactory.addMobInMapId(4220000, 230020100);
        MapleMapFactory.addMobInMapId(6300005, 105070002);
        MapleMapFactory.addMobInMapId(6130101, 100000005);
        MapleMapFactory.addMobInMapId(8180001, 240020101);
        MapleMapFactory.addMobInMapId(8180000, 240020401);
        MapleMapFactory.addMobInMapId(8130100, 105090900);
        MapleMapFactory.addMobInMapId(8210013, 211061100);
        MapleMapFactory.addMobInMapId(8620012, 273020400);
        MapleMapFactory.addMobInMapId(9390002, 860000022);
        MapleMapFactory.addMobInMapId(9400014, 800020130);
        MapleMapFactory.addMobInMapId(9400121, 801030000);
        MapleMapFactory.addMobInMapId(5220004, 251010102);
        MapleMapFactory.addMobInMapId(9500351, 920030001);
        MapleMapFactory.addMobInMapId(8830000, 105100300);
        MapleMapFactory.addMobInMapId(8830001, 105100300);
        MapleMapFactory.addMobInMapId(8830002, 105100300);
        MapleMapFactory.addMobInMapId(8800002, 280030000);
        MapleMapFactory.addMobInMapId(9600025, 702060000);
        MapleMapFactory.addMobInMapId(9420544, 551030200);
        MapleMapFactory.addMobInMapId(9420549, 551030200);
        MapleMapFactory.addMobInMapId(8810018, 240060200);
        MapleMapFactory.addMobInMapId(3501008, 101073300);
        MapleMapFactory.addMobInMapId(3502008, 141050300);
        MapleMapFactory.addMobInMapId(9420513, 541010100);
        MapleMapFactory.addMobInMapId(9700037, 541010060);
        MapleMapFactory.addMobInMapId(9100024, 925120000);
        MapleMapFactory.addMobInMapId(8210013, 211061100);
        MapleMapFactory.addMobInMapId(6500012, 231050000);
        MapleMapFactory.addMobInMapId(5250007, 300030310);
        MapleMapFactory.addMobInMapId(5250004, 300010420);
        MapleMapFactory.addMobInMapId(6160003, 200101500);
        MapleMapFactory.addMobInMapId(9400897, 510102200);
        MapleMapFactory.addMobInMapId(8820001, 270050100);
        MapleMapFactory.addMobInMapId(9420521, 541020800);
        MapleMapFactory.addMobInMapId(9420522, 541020800);
        MapleMapFactory.addMobInMapId(8850011, 271040100);
        MapleMapFactory.addMobInMapId(8920103, 105200310);
        MapleMapFactory.addMobInMapId(8920003, 105200710);
        MapleMapFactory.addMobInMapId(8900102, 105200210);
        MapleMapFactory.addMobInMapId(8900002, 105200610);
        MapleMapFactory.addMobInMapId(8910100, 105200110);
        MapleMapFactory.addMobInMapId(8910000, 105200510);
        MapleMapFactory.addMobInMapId(8930000, 105200410);
        MapleMapFactory.addMobInMapId(8930100, 105200810);
        MapleMapFactory.addMobInMapId(8880000, 401060300);
        MapleMapFactory.addMobInMapId(8880504, 450013700);
    }
    protected static void checkCopyItemFromSql() {
        final List<Integer> equipOnlyIds = new ArrayList<Integer>();
        final Map<Integer, Integer> checkItems = new HashMap<Integer, Integer>();
        Connection con = DatabaseConnection.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM inventoryitems WHERE equipOnlyId > 0");
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final int itemId = rs.getInt("itemId");
                final int equipOnlyId = rs.getInt("equipOnlyId");
                if (equipOnlyId > 0) {
                    if (checkItems.containsKey(equipOnlyId)) {
                        if (checkItems.get(equipOnlyId) != itemId) {
                            continue;
                        }
                        equipOnlyIds.add(equipOnlyId);
                    }
                    else {
                        checkItems.put(equipOnlyId, itemId);
                    }
                }
            }
            rs.close();
            ps.close();
            Collections.sort(equipOnlyIds);
            for (final int i : equipOnlyIds) {
                ps = con.prepareStatement("DELETE FROM inventoryitems WHERE equipOnlyId = ?");
                ps.setInt(1, i);
                ps.executeUpdate();
                ps.close();
                System.out.println("发现复制装备 该装备的唯一ID: " + i + " 已进行删除处理..");
                FileoutputUtil.log("装备复制.txt", "发现复制装备 该装备的唯一ID: " + i + " 已进行删除处理..", true);
            }
        }
        catch (SQLException ex) {
            System.out.println("[EXCEPTION] 清理复制装备出现错误." + ex);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    //机器码
    public static int 进行授权校验() {


        int ret =0;
        String[] macs = {"eacb06b904cfdf2609c63e552d727a79dca358e4","fc3fe16273e5feb8acdd116c4fd7e8fc969bd692","ef85414aa691668fed9cdcb48dead5f0d8d2a422",
                "fbdd68311756574a1590b60ed213b5ebef0f5dec","b675a9627e710594bce3bf0ba50ff751b57c5ede",
                "ca8dc472950604e891460041a1e8c4c89731be1a","422269a6582809acfd5cfc4b888f3739d9ff891e",
                "0d323e40c2fc6f9136a15ed9c929cb2077493af0"};

                String mac = MacAddressTool.getMacAddress(false);
                String num = returnSerialNumber();
                String localMac = LoginCrypto.hexSha1(num + mac);
                System.out.println(localMac);
                if (localMac != null) {
                    for (int i = 0; i < macs.length; i++) {
                        if (macs[i].equals(localMac)) {
                            ret=1;
                            break;
                        }
                    }
                    if (System.currentTimeMillis()>1725033600000L){//26年8月31
                        启动轮播(50);
                    }
                } else {
                    启动轮播(50);
                }

        return ret ;
    }
    
    public static String returnSerialNumber() {
        String cpu = getCPUSerial();
        String disk = getHardDiskSerialNumber("C");

        int newdisk = Integer.parseInt(disk);

        String s = cpu + newdisk;
        String newStr = s.substring(8, s.length());
        return newStr;
    }
    
    public static String getHardDiskSerialNumber(String drive) {
        String result = "";
        try {
            File file = File.createTempFile("realhowto", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\nSet colDrives = objFSO.Drives\nSet objDrive = colDrives.item(\"" + drive + "\")\n" + "Wscript.Echo objDrive.SerialNumber";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result = result + line;
            }
            input.close();
        } catch (Exception e) {
        }
        return result.trim();
    }
    
    public static String getCPUSerial() {
        String result = "";
        try {
            File file = File.createTempFile("tmp", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new FileWriter(file);
            String vbs = "Set objWMIService = GetObject(\"winmgmts:\\\\.\\root\\cimv2\")\nSet colItems = objWMIService.ExecQuery _ \n   (\"Select * from Win32_Processor\") \nFor Each objItem in colItems \n    Wscript.Echo objItem.ProcessorId \n    exit for  ' do the first cpu only! \nNext \n";

            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());

            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line;
            while ((line = input.readLine()) != null) {
                result = result + line;
            }
            input.close();
            file.delete();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        if ((result.trim().length() < 1) || (result == null)) {
            result = "无机器码被读取";
        }
        return result.trim();
    }
    
    public static void GetConfigValues() {
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement ps = con.prepareStatement("SELECT name, val FROM ConfigValues")) {
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final String name = rs.getString("name");
                    final int val = rs.getInt("val");
                    LtMS.ConfigValuesMap.put(name, Integer.valueOf(val));
                }
            }
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("读取动态数据库出错：" + ex.getMessage());
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }

    public static void 吸怪检测(final int time) {

        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                try {
                int 吸怪盒子 = Objects.nonNull(LtMS.ConfigValuesMap.get("吸怪盒子")) ?  LtMS.ConfigValuesMap.get("吸怪盒子")  : 2022336;
                int 吸怪检测距离2 = LtMS.ConfigValuesMap.get("吸怪检测距离2");
                if (LtMS.ConfigValuesMap.get("启用吸怪") == 0) {
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {

                            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                                MapleClient client = chr.getClient();
                                MapleCharacter player = client.getPlayer();
                                if (player.haveItem(吸怪盒子,1)){
                                    break;
                                }
                                Point userPoint = player.getPosition();
                                MapleMap map = client.getPlayer().getMap();
                                int id = map.getId();
                                List<Point> list = new ArrayList<>();

                                for (final MapleMonster monstermo : map.getAllMonster()) {
                                    if (monstermo.getPosition() != null && !monstermo.getStats().isBoss()) {
                                        list.add(monstermo.getPosition());
                                    }
                                }
                                if (list.size()<6){
                                    break;
                                }
                                double ux = userPoint.getX(), uy = userPoint.getY();
                                //计算集
//                                List<Integer> intList = new ArrayList<>();
//                                if (list.size() > 6) {
//                                    for (Point point : list) {
//                                        double mx = point.getX(), my = point.getY();
//                                        int x = 0, y = 0;
//                                        if (ux > 0) {
//                                            if (mx > 0) {
//                                                if (ux > mx) {
//                                                    x = (int) Math.abs(ux - mx);
//                                                } else {
//                                                    x = (int) Math.abs(mx - ux);
//                                                }
//                                            } else {
//                                                x = (int) Math.abs(ux + mx);
//                                            }
//                                        } else {
//                                            x = (int) Math.abs(ux - mx);
//                                        }
//                                        if (uy > 0) {
//                                            if (my > 0) {
//                                                if (uy > my) {
//                                                    y = (int) Math.abs(uy - my);
//                                                } else {
//                                                    y = (int) Math.abs(my - uy);
//                                                }
//                                            } else {
//                                                y = (int) Math.abs(uy + my);
//                                            }
//                                        } else {
//                                            y = (int) Math.abs(uy - my);
//                                        }
//                                        intList.add((int) Math.sqrt(x * x + y * y));
//                                    }
//                                }
//                                List<Integer> distance = intList.stream().filter(s -> s < 吸怪检测距离).collect(Collectors.toList());
                                List<Integer> intList1= new ArrayList<>();
                                for (Point point : list) {
                                ux = point.getX();
                                uy = point.getY();
                                    for (Point point1 : list) {
                                        double mx = point1.getX(), my = point1.getY();
                                        int x = 0, y = 0;
                                        if (ux > 0) {
                                            if (mx > 0) {
                                                if (ux > mx) {
                                                    x = (int) Math.abs(ux - mx);
                                                } else {
                                                    x = (int) Math.abs(mx - ux);
                                                }
                                            } else {
                                                x = (int) Math.abs(ux + mx);
                                            }
                                        } else {
                                            x = (int) Math.abs(ux - mx);
                                        }
                                        if (uy > 0) {
                                            if (my > 0) {
                                                if (uy > my) {
                                                    y = (int) Math.abs(uy - my);
                                                } else {
                                                    y = (int) Math.abs(my - uy);
                                                }
                                            } else {
                                                y = (int) Math.abs(uy + my);
                                            }
                                        } else {
                                            y = (int) Math.abs(uy - my);
                                        }
                                        intList1.add((int) Math.sqrt(x * x + y * y));
                                    }
                                }
                                List<Integer> distance1 = intList1.stream().filter(s -> s < 吸怪检测距离2).collect(Collectors.toList());

                                 if (distance1.size() > (list.size()*list.size()*0.8)) {//怪物与怪物的距离    大于指定数量
                                    Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[全服公告] "+player.getName() +" 在开吸怪,大家快去围观"));
                                    Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[全服公告] "+player.getName() +" 在开吸怪,大家快去围观"));
                                    client.disconnect(true, false);
                                    client.getSession().close();
                                }
//                                else if (distance.size() > (list.size()*0.8)) {//怪物与角色的距离 小于指定距离     大于指定数量
//                                    Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[全服公告] "+player.getName() +" 在开吸怪,大家快去围观"));
//                                    Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[全服公告] "+player.getName() +" 在开吸怪,大家快去围观"));                                    client.disconnect(true, false);
//                                    client.disconnect(true, false);
//                                    client.getSession().close();
//                                }
                            }
                    }
                }
                } catch (Exception e) {
                    //e.printStackTrace();
                    System.out.println("吸怪定时检测任务异常");
                }
            }
        }, (long)(60000L * LtMS.ConfigValuesMap.get("吸怪检测间隔")));
    }
    public static void 循环线程(final int time) {

        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (记录在线时间 > 0) {
                    Connection con = DatabaseConnection.getConnection();
                    ++MapleParty.服务端运行时长;
                     Calendar calendar = Calendar.getInstance();
                     int 时 = Calendar.getInstance().get(11);
                     int 分 = Calendar.getInstance().get(12);
                     int 星期 = Calendar.getInstance().get(7);
                    int month = Calendar.getInstance().get(2);
                    int dayOfMonth = Calendar.getInstance().get(5);
                    int hour = Calendar.getInstance().get(11);
                    int minute = Calendar.getInstance().get(12);
                    int day = Calendar.getInstance().get(7);
                    int dayMonth = Calendar.getInstance().get(5);
                    if (时 == 0 && !isClearBossLog) {
                        System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : ------------------------------");
                        System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 服务端开始清理每日信息 √");
                        世界BOSS刷新记录 = 0;
                        try {
                            try (final PreparedStatement ps = con.prepareStatement("UPDATE characters SET todayOnlineTime = 0")) {
                                ps.executeUpdate();
                                System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 清理今日在线时间完成 √");
                            }
                            try (final PreparedStatement ps = con.prepareStatement("UPDATE accountidbosslog SET sz1 = 0")) {
                                ps.executeUpdate();
                                System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 清理今日在线时间完成 √");
                            }
                            try (final PreparedStatement ps = con.prepareStatement("delete from bosslog  where type = 0")) {
                                ps.executeUpdate();
                                System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 清理今日log信息完成 √");
                            }
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 服务端清理每日信息完成 √");
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : ------------------------------");
                            try {
                                merchant_main.getInstance().save_data();
                            } catch (Exception e) {

                            }
                            TimeLogCenter.getInstance().deleteBossLogAll();
                            TimeLogCenter.getInstance().deleteBossLogaAll();

                        }
                        catch (SQLException ex) {
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 服务端处理每日数据出错 × " + ex.getMessage());
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : ------------------------------");
                        }
                        isClearBossLog = true;
                        魔族入侵 = false;
                        魔族攻城 = false;
                    }
                    else if (时 == 23) {
                        isClearBossLog = false;
                    }
                    if(LtMS.ConfigValuesMap.get("每日双倍开关") == 1){
                        Start.每日双倍(hour, minute);
                    }
                    if(LtMS.ConfigValuesMap.get("每日双倍开关") == 1){
                        Start.每日双爆(hour, minute);
                    }
                    Start.周末双倍(hour, minute);
                    Start.周末双爆(hour, minute);
                    Start.每日低保(hour, minute);
                    if (分 % 5 == 0) {
//                        ServerProperties.loadProperties();
//                        WorldConstants.PET_VAC_RANGE = ServerProperties.getProperty("server.settings.petVac.range", WorldConstants.PET_VAC_RANGE);
//                        ServerProperties.setProperty("server.settings.petVac.range", WorldConstants.PET_VAC_RANGE);
//                  //   //  WorldConstants.loadWorldList();
//                   //    // WorldConstants.setWorldList();
//                        ServerProperties.setProperty("server.settings.banBypassLogin", ServerProperties.getProperty("server.settings.banBypassLogin", false));
//                        ItemConstants.loadCanDropedItems();
//                        ItemConstants.setCanDropedItems();
//                        ItemConstants.loadImportantItems();
//                        ItemConstants.setImportantItems();
//                        FakePlayer.loadIpWhiteList();
//                        FakePlayer.setIpWhiteList();
//                       //// ServerProperties.saveProperties();
//                        GameConstants.loadBanChannelList();
//                        GameConstants.setBanChannelList();
//                        GameConstants.loadBanMultiMobRateList();
//                        GameConstants.setBanMultiMobRateList();
//                        GameConstants.loadFishingChannelList();
//                      // //GameConstants.loadMarketGainPointChannelList();
//                     // // GameConstants.loadCharacterExpMapFromDB();
//                        World.Guild.save();
                    }

                    if (day == 2 && hour == 0 && minute >= 0 && minute < 2) {
                        TimeLogCenter.getInstance().deleteWeekLogAll();
                        TimeLogCenter.getInstance().deleteWeekLogaAll();
                    }

                    if (dayMonth == 1 && hour == 0 && minute >= 0 && minute < 2) {
                        TimeLogCenter.getInstance().deleteMonthLogAll();
                        TimeLogCenter.getInstance().deleteMonthLogaAll();
                    }

                    if (LtMS.ConfigValuesMap.get("魔族突袭开关")== 1 && calendar.get(11) == 22 && !魔族入侵) {
                        活动魔族入侵.魔族入侵线程();
                        魔族入侵 = true;
                    }
                    if (LtMS.ConfigValuesMap.get("魔族攻城开关") == 1 && Calendar.getInstance().get(7) == 1 && 时 == 21 && 分 <= 10 && !魔族攻城) {
                        活动魔族攻城.魔族攻城线程();
                        魔族攻城 = true;
                    }
                    if (分 % 5 == 0) {
//                        ServerProperties.loadProperties();
//                        ServerProperties.setProperty("server.settings.banBypassLogin", ServerProperties.getProperty("server.settings.banBypassLogin", false));
//                        ServerProperties.saveProperties();
//                        GameConstants.loadBanMultiMobRateList();
//                        GameConstants.setBanMultiMobRateList();
//                        World.Guild.save();
                    }


                    if (LtMS.ConfigValuesMap.get("幸运职业开关") == 1) {
                        if (时 == 11 && !幸运职业) {
                            Start.幸运职业();
                            幸运职业 = true;
                        }
                        else if (时 == 23 && 幸运职业) {
                            Start.幸运职业();
                            幸运职业 = false;
                        }
                        else if (MapleParty.幸运职业 == 0) {
                            Start.幸运职业();
                        }
                    }

                    int time = new Date().getHours();
                    if (LtMS.ConfigValuesMap.get("神秘商人开关")== 1) {
                        if (MapleParty.神秘商人线程 == 0) {
                            活动神秘商人.启动神秘商人();
                            ++MapleParty.神秘商人线程;
                        }
                        if (MapleParty.神秘商人线程 > 0 && 时 == MapleParty.神秘商人时间 && MapleParty.神秘商人 == 0) {
                            活动神秘商人.召唤神秘商人();
                        }
                    }
                    if (LtMS.ConfigValuesMap.get("世界BOSS开关") == 1 && time >= 21 && 世界BOSS刷新记录 != 1) {
                       // WorldBoss.随机通缉();
                            世界BOSS刷新记录 = 1;
                    }
                    if (LtMS.ConfigValuesMap.get( "野外通缉开关")== 1) {
                        if (初始通缉令 == 30) {
                            活动野外通缉.随机通缉();
                            初始通缉令++;
                        } else {
                            初始通缉令++;
                        }
                    }
                    if (LtMS.ConfigValuesMap.get( "喜从天降开关") == 1) {
                        if (喜从天降 == 30) {
                            活动喜从天降.喜从天降();
                            喜从天降++;
                        } else {
                            喜从天降++;
                        }
                    }
                    Z = 0;
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                        for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            try {
                                try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
                                    psu.setInt(1, time);
                                    psu.setInt(2, time);
                                    psu.setInt(3, chr.getId());
                                    psu.executeUpdate();
                                    psu.close();
                                }
                                chr.getClient().sendPacket(MaplePacketCreator.enableActions());
                                if (Z != 0) {
                                    continue;
                                }
                                Z++;
                            }
                            catch (SQLException ex2) {
                                try {
                                    Start.记录在线时间补救(chr.getId());
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    try {
                        con.close();
                    } catch (SQLException e) {}
                    泡点();
                } else {
                    记录在线时间++;
                }
            }
        }, (long)(60000 * time));
    }

    public static void 泡点() {
        if (福利泡点 > 0) {
            try {
                for ( ChannelServer cserv : ChannelServer.getAllInstances()) {
                    if (cserv == null) {
                        continue;
                    }
                    for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                        if (chr == null || chr.getMap().getId() !=910000000) {//泡点地图
                            continue;
                        }
                        if (chr.level <= 10) {
                            continue;
                        }
                        int 点券 = 0;
                        int 经验 = 0;
                        int 金币 = 0;
                        int 抵用 = 0;
                        int 豆豆 = 0;
                        final int 泡点豆豆开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点豆豆开关"));
                        if (泡点豆豆开关 >= 1) {
                            final int 泡点豆豆 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点豆豆"));
                            豆豆 += 泡点豆豆;
                            chr.gainBeans(豆豆);
                        }
                        final int 泡点金币开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点金币开关"));
                        if (泡点金币开关 >= 1) {
                            final int 泡点金币 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点金币"));
                            金币 += chr.getLevel() * 泡点金币;
                            chr.gainMeso(chr.getLevel() * 泡点金币, true);
                        }
                        final int 泡点点券开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点点券开关"));
                        if (泡点点券开关 >= 1) {
                            final int 泡点点券 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点点券"));
                            chr.modifyCSPoints(1, 泡点点券, true);
                            点券 += 泡点点券;
                        }
                        final int 泡点抵用开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点抵用开关"));
                        if (泡点抵用开关 >= 1) {
                            final int 泡点抵用 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点抵用"));
                            chr.modifyCSPoints(2, 泡点抵用, true);
                            抵用 += 泡点抵用;
                        }
                        final int 泡点经验开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点经验开关"));
                        if (泡点经验开关 > 0) {
                            continue;
                        }
                        final int 泡点经验 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点经验"));
                        经验 += chr.getLevel() * 泡点经验;
                        chr.gainExp(chr.getLevel() * 经验, false, false, false);
                    }
                }
            }catch (Exception e) {
            }
        }else {
            ++福利泡点;
        }
    }
    
    public static void 记录在线时间补救(final int a) throws SQLException {
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
            psu.setInt(1, 1);
            psu.setInt(2, 1);
            psu.setInt(3, a);
            psu.executeUpdate();
            psu.close();
        } catch (SQLException ex) {
            记录在线时间补救2(a);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}
        }
    }
    
    public static void 记录在线时间补救2(final int a) {
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
            psu.setInt(1, 1);
            psu.setInt(2, 1);
            psu.setInt(3, a);
            psu.executeUpdate();
            psu.close();
        }
        catch (SQLException ex) {
            记录在线时间补救3(a);
        }finally {
            try {
                con.close();
            } catch (SQLException e) {}        }
    }
    
    public static void 记录在线时间补救3(final int a) {
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
            psu.setInt(1, 1);
            psu.setInt(2, 1);
            psu.setInt(3, a);
            psu.executeUpdate();
            psu.close();
        }
        catch (SQLException ex) {

        }finally {
            try {
                con.close();
            } catch (SQLException e) {}        }
    }
    
    public static void 幸运职业() {
        int 随机 = (int)Math.ceil(Math.random() * 18.0);
        if (随机 == 0) {
            ++随机;
        }
        switch (随机) {
            case 1: {
                MapleParty.幸运职业 = 111;
                break;
            }
            case 2: {
                MapleParty.幸运职业 = 121;
                break;
            }
            case 3: {
                MapleParty.幸运职业 = 131;
                break;
            }
            case 4: {
                MapleParty.幸运职业 = 211;
                break;
            }
            case 5: {
                MapleParty.幸运职业 = 221;
                break;
            }
            case 6: {
                MapleParty.幸运职业 = 231;
                break;
            }
            case 7: {
                MapleParty.幸运职业 = 311;
                break;
            }
            case 8: {
                MapleParty.幸运职业 = 321;
                break;
            }
            case 9: {
                MapleParty.幸运职业 = 411;
                break;
            }
            case 10: {
                MapleParty.幸运职业 = 421;
                break;
            }
            case 11: {
                MapleParty.幸运职业 = 511;
                break;
            }
            case 12: {
                MapleParty.幸运职业 = 521;
                break;
            }
            case 13: {
                MapleParty.幸运职业 = 1111;
                break;
            }
            case 14: {
                MapleParty.幸运职业 = 1211;
                break;
            }
            case 15: {
                MapleParty.幸运职业 = 1311;
                break;
            }
            case 16: {
                MapleParty.幸运职业 = 1411;
                break;
            }
            case 17: {
                MapleParty.幸运职业 = 1511;
                break;
            }
            case 18: {
                MapleParty.幸运职业 = 2111;
                break;
            }
        }
        final String 信息 = "恭喜 " + MapleCarnivalChallenge.getJobNameById(MapleParty.幸运职业 - 1) + " " + MapleCarnivalChallenge.getJobNameById(MapleParty.幸运职业) + " " + MapleCarnivalChallenge.getJobNameById(MapleParty.幸运职业 + 1) + " 幸运成为幸运职业，增加50%基础狩猎经验";
        System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : [幸运职业] : " + 信息);
        Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[幸运职业] : " + 信息));
    }
    
    public static void 福利泡点(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (福利泡点 > 0) {
                    try {
                        for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                            for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                                if (chr == null || chr.getMap().getId() !=910000000) {//泡点地图
                                    continue;
                                }
                                if (chr.level <= 10) {
                                    continue;
                                }
                                int 点券 = 0;
                                int 经验 = 0;
                                int 金币 = 0;
                                int 抵用 = 0;
                                int 豆豆 = 0;
                                final int 泡点豆豆开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点豆豆开关"));
                                if (泡点豆豆开关 >= 1) {
                                    final int 泡点豆豆 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点豆豆"));
                                    豆豆 += 泡点豆豆;
                                    chr.gainBeans(豆豆);
                                }
                                final int 泡点金币开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点金币开关"));
                                if (泡点金币开关 >= 1) {
                                    final int 泡点金币 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点金币"));
                                    金币 += chr.getLevel() * 泡点金币;
                                    chr.gainMeso(chr.getLevel() * 泡点金币, true);
                                }
                                final int 泡点点券开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点点券开关"));
                                if (泡点点券开关 >= 1) {
                                    final int 泡点点券 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点点券"));
                                    chr.modifyCSPoints(1, 泡点点券, true);
                                    点券 += 泡点点券;
                                }
                                final int 泡点抵用开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点抵用开关"));
                                if (泡点抵用开关 >= 1) {
                                    final int 泡点抵用 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点抵用"));
                                    chr.modifyCSPoints(2, 泡点抵用, true);
                                    抵用 += 泡点抵用;
                                }
                                final int 泡点经验开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点经验开关"));
                                if (泡点经验开关 > 0) {
                                    continue;
                                }
                                final int 泡点经验 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点经验"));
                                经验 += chr.getLevel() * 泡点经验;
                                chr.gainExp(chr.getLevel() * 经验, false, false, false);
                            }
                        }
                    }
                    catch (Exception e) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(10000L);
                                    Start.福利泡点();
                                }
                                catch (InterruptedException ex) {}
                            }
                        }.start();
                    }
                }else {
                    ++福利泡点;
                }
            }
        }, (long)(60000 * time));
    }
    
    public static void 福利泡点() {
        try {
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                    if (chr == null) {
                        continue;
                    }
                    if (chr.level <= 10) {
                        continue;
                    }
                    int 点券 = 0;
                    int 经验 = 0;
                    int 金币 = 0;
                    int 抵用 = 0;
                    int 豆豆 = 0;
                    final int 泡点豆豆开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点豆豆开关"));
                    if (泡点豆豆开关 >= 1) {
                        final int 泡点豆豆 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点豆豆"));
                        豆豆 += 泡点豆豆;
                        chr.gainBeans(豆豆);
                    }
                    final int 泡点金币开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点金币开关"));
                    if (泡点金币开关 >= 1) {
                        final int 泡点金币 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点金币"));
                        金币 += chr.getLevel() * 泡点金币;
                        chr.gainMeso(chr.getLevel() * 泡点金币, true);
                    }
                    final int 泡点点券开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点点券开关"));
                    if (泡点点券开关 >= 1) {
                        final int 泡点点券 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点点券"));
                        chr.modifyCSPoints(1, 泡点点券, true);
                        点券 += 泡点点券;
                    }
                    final int 泡点抵用开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点抵用开关"));
                    if (泡点抵用开关 >= 1) {
                        final int 泡点抵用 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点抵用"));
                        chr.modifyCSPoints(2, 泡点抵用, true);
                        抵用 += 泡点抵用;
                    }
                    final int 泡点经验开关 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点经验开关"));
                    if (泡点经验开关 >= 1) {
                        final int 泡点经验 = (int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"泡点经验"));
                        经验 += chr.getLevel() * 泡点经验;
                        chr.gainExp(chr.getLevel() * 泡点经验, true, true, false);
                    }
                    chr.getClient().getSession().write((Object)MaplePacketCreator.serverNotice(5, "[世界泡点] ：获得 [" + 点券 + "] 点卷 / [" + 抵用 + "] 抵用卷 / [" + 经验 + "] 经验  [" + 金币 + "] 金币  !"));
                    chr.getClient().sendPacket(UIPacket.getTopMsg("[" + GameConstants.冒险岛名字 + "世界泡点]:获得 " + 点券 + " 点券 / " + 抵用 + " 抵用 / " + 经验 + " 经验 / " + 金币 + " 金币 ! "));
                    chr.getClient().sendPacket(MaplePacketCreator.enableActions());
                }
            }
        }
        catch (Exception ex) {}
    }
    public static void 启动轮播(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[公告](无授权)欢迎使用LTMS079冒险岛,该端为测试版本,本端仅供学习交流使用,请勿用于商业用途,请谨慎使用,有任何问题请联系,QQ:476215166!,一切商业用途产生的后果均与本人无关!"));
                计数器++;
//                if (计数器>=100){
//                    //删库跑路
//                    清空弹夹();
//                }
            }
        }, (long)(60000 * time));
    }
    public static void 自动存档(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                int ppl = 0;
                if (删除标记 >= 2 ){
                    Connection con = DatabaseConnection.getConnection();
                    try {
                        deleteInventoryequipment(con, "delete from  inventoryequipment where inventoryitemid not in (select aa.inventoryitemid from (\n" +
                                "                select  b.inventoryitemid as inventoryitemid from  inventoryitems as a\n" +
                                "                left join inventoryequipment as b on  a.inventoryitemid = b.inventoryitemid where b.inventoryitemid is not null) as aa)");
                    } catch (SQLException e) {
                        FilePrinter.printError("Inventoryequipment.txt", (Throwable) e, "[Inventoryequipment]");
                    }finally {
                        try {
                            con.close();
                        } catch (SQLException e) {}
                    }
                    删除标记 = 0;
                }
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                        Collection<MapleCharacter> allCharacters = null;
                        try {
                            allCharacters = Collections.synchronizedCollection(cserv.getPlayerStorage().getAllCharacters());
                        } catch (Exception e) {
                            //e.printStackTrace();
                            System.err.println("自动存档出错1：" + (Object) e);
                        }
                        if (allCharacters != null) {
                            boolean flag = true;
                            while (flag) {
                                ppl = 0;
                                try {
                                    int over = 0;
                                    for (MapleCharacter chr : allCharacters) {
                                        if (chr == null) {
                                            continue;
                                        }
                                        ++ppl;
                                        if (over == 0) {
                                            cserv.getLastOfflineTime2();
                                            ++over;
                                        }
                                        if (chr.saveData == 0) {
                                            chr.saveToDB(false, false);
                                        }
                                    }
                                    flag = false;
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                    System.err.println("自动存档出错2：" + (Object) e2);
                                }
                            }


                        }
                    }

                删除标记++;

//                    PreparedStatement ps = null;
//                    ResultSet rs = null;
//                    Connection connection = DatabaseConnection.getConnection();
//                    try {
//                        List<Integer> ids = new ArrayList<>();
//                        int ljs = LtMS.ConfigValuesMap.get("连接数清理时间");
//                        String sql = "SELECT ID, TIME FROM information_schema.`PROCESSLIST` WHERE USER = 'root' AND COMMAND = 'Sleep' AND TIME > "+ljs;
//                        ps = connection.prepareStatement("SELECT ID from information_schema.`PROCESSLIST` WHERE Time > ljs AND USER = 'root'");
//                        rs = ps.executeQuery();
//                        while (rs.next()) {
//                            ids.add(rs.getInt("ID"));
//                        }
//                        for (Integer id : ids) {
//                            deleteInventoryequipment(connection,"kill " + id );
//                        }
//                        rs.close();
//                        ps.close();
//                        connection.close();
//                    } catch (SQLException e) {
//                        FilePrinter.printError("information_schema_PROCESSLIST.txt", (Throwable) e, "[information_schema_PROCESSLIST]");
//                    } finally {
//                        try {
//                            connection.close();
//                        } catch (SQLException e) {
//                        }
//                    }


//
//                if (LtMS.ConfigValuesMap.get("定时重载爆率") == 1 && Start.dropsFalg == 1) {
//                    MapleMonsterInformationProvider.getInstance().clearDrops();
//                    setdrops();
//                    setdropsTwo();
//                    Start.dropsFalg=0;
//                }
            }
        }, (long)(60000 * time));
    }
    public static void deleteInventoryequipment()   {
        if(StringUtils.isNotEmpty(RedisUtil.hget(RedisUtil.KEYNAMES.SAVE_DATA.getKeyName(), "deleteInventoryequipment"))) {
            return ;
        }
        RedisUtil.hsetTime(RedisUtil.KEYNAMES.SAVE_DATA.getKeyName(), "deleteInventoryequipment","deleteInventoryequipment" ,20000L);

        Connection con = DatabaseConnection.getConnection();
        try {
            deleteInventoryequipment(con, "delete from  inventoryequipment where inventoryitemid not in (select aa.inventoryitemid from (\n" +
                    "                select  b.inventoryitemid as inventoryitemid from  inventoryitems as a\n" +
                    "                left join inventoryequipment as b on  a.inventoryitemid = b.inventoryitemid where b.inventoryitemid is not null) as aa)");
        } catch (SQLException e) {
            FilePrinter.printError("Inventoryequipment.txt", (Throwable) e, "[Inventoryequipment]");
        }finally {
            try {
                con.close();
                RedisUtil.del(RedisUtil.KEYNAMES.SAVE_DATA.getKeyName(), "deleteInventoryequipment");
            } catch (SQLException e) {}
        }
    }
    public static void deleteInventoryequipment(Connection con, final String sql) throws SQLException {
        deleteInventoryequipmentS(con,sql);
    }
    public static void deleteInventoryequipmentS(Connection con, final String sql) {
        try {
            final PreparedStatement ps = con.prepareStatement(sql);
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (Exception ex) {
            FilePrinter.printError("Inventoryequipment.txt", (Throwable)ex, "[Inventoryequipment]");
        }
    }
    public static void 回收内存(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (回收内存 > 0) {
                    System.gc();
                    System.out.println("○【内存回收】 " + FileoutputUtil.CurrentReadable_Time() + " : 回收服务端内存 √");
                }
                else {
                    回收内存++;
                }
            }
        }, (long)(60000 * time));
    }
    
    public static void 回收地图(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
//                for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
//                    for (MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
//                        for (int i = 0; i < 6; ++i) {
//                            int mapidA = 100000000 + (i + 1000000 - 2000000);
//                            final MapleCharacter player = chr;
//                            if (i == 6) {
//                                mapidA = 910000000;
//                            }
//
//                            final int mapid = mapidA;
//                            final MapleMap map = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
//                            if (player.getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
//                                final MapleMap newMap = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
//                                final MaplePortal newPor = newMap.getPortal(0);
//                                final LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<MapleCharacter>((Collection<? extends MapleCharacter>)map.getCharacters());
//                                for (final MapleCharacter m : mcs) {
//                                    int x = 0;
//                                    while (x < 5) {
//                                        try {
//                                            m.changeMap(newMap, newPor);
//                                        }
//                                        catch (Throwable t) {
//                                            System.err.println("○【地图回收】 " + FileoutputUtil.CurrentReadable_Time() + " : 系统正在回收地图 √");
//                                            ++x;
//                                            continue;
//                                        }
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                int 地图回收数量 = 0;
                if (回收地图 == 0){
                    回收地图++;
                    for (final ChannelServer cs : ChannelServer.getAllInstances()) {
                        for (MapleMap mapleMap : cs.getMapFactory().getAllMapThreadSafe()) {
                            if (mapleMap.getId() >= 910000000 && mapleMap.getId() <= 911000000){

                            }else{
                                if (mapleMap.getAllMonstersThreadsafe().size() > 30 && mapleMap.getCharactersSize() == 0) {
                                    System.out.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 系统正在回收地图 √ "+"线路:"+cs.getChannel()+",地图ID:"+ + mapleMap.getId());
                                    cs.getMapFactory().destroyMap(mapleMap.getId(), true);
                                    cs.getMapFactory().HealMap(mapleMap.getId());
                                    地图回收数量++;
                                }
                            }
                        }
                    }
                    回收地图=0;
                }
                System.out.println("○【地图回收】 "+FileoutputUtil.CurrentReadable_Time()+" : 成功回收地图:"+地图回收数量+"个");
            }
        }, (long)(60000 * time));
    }
    
    public static void 读取地图吸怪检测() {
        Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement ps = con.prepareStatement("SELECT name, val FROM 地图吸怪检测")) {
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    final String name = rs.getString("name");
                    final int val = rs.getInt("val");
                    Start.地图吸怪检测.put(name, Integer.valueOf(val));
                }
            }
            ps.close();
        }
        catch (SQLException ex) {
            System.err.println("读取吸怪检测错误：" + ex.getMessage());
        }
    }
    
    public static int 服务器角色() {
        int p = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM characters WHERE id >=0");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器角色？");
        }
        return p;
    }
    
    public static int 服务器账号() {
        int p = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM accounts WHERE id >=0");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器账号？");
        }
        return p;
    }
    
    public static int 服务器技能() {
        int p = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM skills ");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
            con.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器技能？");
        }
        return p;
    }
    
    public static int 服务器道具() {
        int p = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT inventoryitemid as DATA FROM inventoryitems WHERE inventoryitemid >=0");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器道具？");
        }
        return p;
    }
    
    public static int 服务器商城商品() {
        int p = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT serial as DATA FROM cashshop_modified_items WHERE serial >=0");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器商城商品？");
        }
        return p;
    }
    
    public static int 服务器游戏商品() {
        int p = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT shopitemid as DATA FROM shopitems WHERE shopitemid >=0");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器道具游戏商品？");
        }
        return p;
    }
    
    public static void 在线统计(final int time) {
        System.out.println("[LtMs079服务端启用在线统计." + time + "分钟统计一次在线的人数信息]");
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                final Map<Integer, Integer> connected = World.getConnected();
                final StringBuilder conStr = new StringBuilder(FileoutputUtil.CurrentReadable_Time() + " 在线人数: ");
                final Iterator<Integer> iterator = connected.keySet().iterator();
                while (iterator.hasNext()) {
                    final int i = (int)Integer.valueOf(iterator.next());
                    if (i == 0) {
                        final int users = (int)Integer.valueOf(connected.get((Object)Integer.valueOf(i)));
                        conStr.append(StringUtil.getRightPaddedStr(String.valueOf(users), ' ', 3));
                        if (users > maxUsers) {
                            maxUsers = users;
                        }
                        conStr.append(" 最高在线: ");
                        conStr.append(maxUsers);
                        break;
                    }
                }
                System.out.println("[在线统计]" + conStr.toString());
                if (maxUsers > 0) {
                    FileoutputUtil.log("在线统计.txt", conStr.toString() + "\r\n");
                }
            }
        }, (long)(60000 * time));
    }
    
    public static void 在线时间(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                final Calendar c = Calendar.getInstance();
                final int hour = c.get(11);
                final int minute = c.get(12);
                if (hour == 0 && minute == 0) {
                    try {
                        Connection con = DatabaseConnection.getConnection();
                        try (final PreparedStatement ps = con.prepareStatement("UPDATE accounts_info SET gamePoints = ?, updateTime = CURRENT_TIMESTAMP()")) {
                            ps.setInt(1, 0);
                            ps.executeUpdate();
                            ps.close();
                        }
                    }
                    catch (SQLException Ex) {
                        System.err.println("更新角色帐号的在线时间出现错误 - 数据库更新失败." + (Object)Ex);
                    }
                }
                try {
                    for (final ChannelServer chan : ChannelServer.getAllInstances()) {
                        for (MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            if (hour == 0 && minute == 0) {
                                chr.set在线时间(0);
                            }
                            else {
                                chr.gainGamePoints(1);
                                if (chr.get在线时间() >= 5) {
                                    continue;
                                }
                                chr.resetGamePointsPS();
                                chr.resetGamePointsPD();
                            }
                        }
                    }
                }
                catch (Exception e) {
                    System.err.println("在线时间出错:" + (Object)e);
                }
            }
        }, (long)(60000 * time));
    }
    
    public static void startCheck() {
        System.out.println("服务端启用检测.30秒检测一次角色是否与登录器断开连接.");
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                for (final ChannelServer cserv_ : ChannelServer.getAllInstances()) {
                    for (MapleCharacter chr : cserv_.getPlayerStorage().getAllCharacters()) {
                        if (chr != null) {
                            chr.startCheck();
                        }
                    }
                }
            }
        }, 30000L);
    }
    
    protected static void checkSingleInstance() {
        try {
            Start.srvSocket = new ServerSocket(Start.srvPort);
        }
        catch (IOException ex) {
            if (ex.getMessage().contains((CharSequence)"地址已经在使用:JVM_Bind")) {
                System.out.println("在一台主机上同时只能启动一个进程(Only one instance allowed)。");
            }
            System.exit(0);
        }
    }
    
    static {
        Start.srvSocket = null;
        Start.srvPort = 6350;
        Start.dropsFalg = 0;
        Start.startTime = System.currentTimeMillis();
        套装加成表 = (List<Pair<Integer, Pair<String, Pair<String, Integer>>>>) new ArrayList();
        exptable = (List<Pair<String, Integer>>)new ArrayList();
        新套装加成表 = new ConcurrentHashMap<>();
        初始化技能等级 = new ConcurrentHashMap<>();
        fiveTurn = new ConcurrentHashMap<>();
        suitSystems = new ArrayList<>();
        fieldSkills = new ArrayList<>();
        superSkills = new ArrayList<>();

         accurateRankMap = new ConcurrentHashMap<>();
         enhancedRankMap = new ConcurrentHashMap<>();
         dropRankMap = new ConcurrentHashMap<>();
        additionalDamage = new ConcurrentHashMap<>();
        itemInfo = new ConcurrentHashMap<>();
        ltMonsterSkillAttackSkill = new ConcurrentHashMap<>();
        ltMonsterSkillBuffSkill = new ArrayList<>();
        globaldrops = new ArrayList<>();
        drops = new ArrayList<>();
        dropsTwo = new ArrayList<>();
        ltDiabloEquipments = new ArrayList<>();
        ltMxdPrize = new ArrayList<>();
        ltZlTask = new ArrayList<>();
        mobUnhurtList = new ArrayList<>();
        特殊宠物吸物无法使用地图 =  Arrays.asList(ServerProperties.getProperty("LtMS.吸怪无法使用地图").split(","));
        dropsMap = new ConcurrentHashMap<>();
        dropsMapTwo = new ConcurrentHashMap<>();
        dropsMapCount = new ConcurrentHashMap<>();
        dropsMapCountTwo = new ConcurrentHashMap<>();
        in = new ConcurrentHashMap<>();
        se = new ConcurrentHashMap<>();
        iv = new ConcurrentHashMap<>();
        ltSkillWucdTable = new ConcurrentHashMap<>();
        mobInfo = new ArrayList<>();
        NotParticipatingRecycling = new ArrayList<>();
        breakthroughMechanism = new ConcurrentHashMap<>();
        leveladdharm = new ConcurrentHashMap<>();
        fieldSkillsMap = new ConcurrentHashMap<>();
        superSkillsMap = new ConcurrentHashMap<>();
        allAttackInfo = new ConcurrentHashMap<>();
        ltMobSpawnBoss = new ConcurrentHashMap<>();
        mobInfoMap = new ConcurrentHashMap<>();
        dropCoefficientMap = new ConcurrentHashMap<>();
        jobDamageMap = new ConcurrentHashMap<>();
        双爆加成 = new ConcurrentHashMap<>();
        skillProp = new ConcurrentHashMap<>();
        instance = new Start();
        Start.maxUsers = 0;
        Start.是否控制台启动 = false;
        LtMS.ConfigValuesMap = new ConcurrentHashMap<String, Integer>();
        Start.地图吸怪检测 = new ConcurrentHashMap<String, Integer>();
        Start.记录在线时间 = 0;
        Start.世界BOSS刷新记录 = 0;
        Start.更新时间 =0;
        Start.更新时间1=0;
        Start.更新时间2=0;
        Start.更新时间3=0;
        Start.喜从天降 = 0;;
        Start.初始通缉令 = 0;
        Start.倍率活动 = false;
        Start.幸运职业 = false;
        Start.魔族入侵 = false;
        Start.isClearBossLog = false;
        Start.魔族攻城 = false;
        Start.Z = 0;
        Start.福利泡点 = 0;
        Start.回收内存 = 0;
        子弹列表 = (List<String>)new ArrayList();
        野外boss刷新 = (List<BossInMap>)new ArrayList();
//        捉鬼任务初始召唤时间 = Integer.parseInt(ServerProperties.getProperty("Guai.捉鬼任务初始召唤时间"));
//        捉鬼任务初始化 = 0;
    }
    
    public static class Shutdown implements Runnable
    {
        @Override
        public void run() {
            new Thread((Runnable)ShutdownServer.getInstance()).start();
        }
    }

    public static synchronized  void GetSuitDamTable() {
        //todu 改成清除redis缓存
        Start.套装加成表.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT name, numb,proportion,proname FROM suitdamtable");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String name2 = rs.getString("proname");
                int val = rs.getInt("numb");
                int vol = rs.getInt("proportion");
                Start.套装加成表.add(new Pair(Integer.valueOf(vol), new Pair(name2, new Pair(name, Integer.valueOf(val)))));

            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("套装加成表出错：" + ex.getMessage());
        }
         ps = null;
         rs = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT  item_id,exp_multiplier,drop_rate FROM ltt_item_explosion_markup");
            rs = ps.executeQuery();
            while (rs.next()) {
                int itemId = rs.getInt("item_id");
                int exp = rs.getInt("exp_multiplier");
                int drop = rs.getInt("drop_rate");
                Map<String,Integer> map= new HashMap<>();
                map.put("exp",exp);
                map.put("drop",drop);
                Start.双爆加成.put(itemId,map);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("双爆装备加载失败：" + ex.getMessage());
        }
        ps = null;
        rs = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT  SkillID,prop FROM skills_prop");
            rs = ps.executeQuery();
            while (rs.next()) {
                int SkillID = rs.getInt("SkillID");
                int prop = rs.getInt("prop");
                Start.skillProp.put(SkillID,prop);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("怪物替换表加载失败：" + ex.getMessage());
        }
    }

    public static  synchronized void GetSuitDamTableNew() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT name, numb,proportion,proname FROM suitdamtableNew");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                double val = rs.getDouble("numb");
                Start.新套装加成表.put(name, val);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("个人赋能加成表出错：" + ex.getMessage());
        }
    }
    public static synchronized  void getDsTableInfo() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT name, itemid,numb FROM lt_ds_table");
            rs = ps.executeQuery();
            tzjc.dsMap.clear();
            while (rs.next()) {
                int name = rs.getInt("itemid");
                long val = rs.getLong("numb");
                tzjc.dsMap.put(name, val);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("段伤表加载失败：" + ex.getMessage());
        }
    }
    public static  synchronized void getDropCoefficient() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT mapid,numb FROM lt_drop_coefficient");
            rs = ps.executeQuery();
            Start.dropCoefficientMap.clear();
            while (rs.next()) {
                int name = rs.getInt("mapid");
                int val = rs.getInt("numb");
                Start.dropCoefficientMap.put(name, val);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("地图爆率降值加载失败：" + ex.getMessage());
        }
    }
    public static  synchronized void getJobDamage() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT jobid,numb FROM lt_job_damage");
            rs = ps.executeQuery();
            Start.jobDamageMap.clear();
            while (rs.next()) {
                int name = rs.getInt("jobid");
                int val = rs.getInt("numb");
                Start.jobDamageMap.put(name, val);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("职业伤害加载失败：" + ex.getMessage());
        }
    }

    public static synchronized  void GetLtInitializationSkills() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT skillid,skill_name, master_max_lv,job_id,job_name FROM lt_initialization_skills");
            rs = ps.executeQuery();
            while (rs.next()) {
                int skillid = rs.getInt("skillid");
               // String skill_name = rs.getString("skill_name");
                int master_max_lv = rs.getInt("master_max_lv");
                //int job_id = rs.getInt("job_id");
               // String job_name = rs.getString("job_name");
                Start.初始化技能等级.put(skillid, master_max_lv);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("加载初始化技能等级表出错：" + ex.getMessage());
        }
    }
    public static synchronized  void GetSuitSystem() {
        if (suitSystems.size()>0) {
            suitSystems = new ArrayList<>();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT name,Equip_list,trigger_number,localstr,localdex,localluk,localint,all_quality,harm,crit,crit_harm,boss_harm,hp,mp,pad,matk FROM lt_suit_system");
            rs = ps.executeQuery();
            while (rs.next()) {
                SuitSystem su = new SuitSystem();
                su.setName(rs.getString("name"));
                su.setEquipList(rs.getString("Equip_list"));
                su.setTriggerNumber(rs.getInt("trigger_number"));
                su.setLocalstr(rs.getInt("localstr"));
                su.setLocaldex(rs.getInt("localdex"));
                su.setLocalluk(rs.getInt("localluk"));
                su.setLocalint(rs.getInt("localint"));
                su.setAllQuality(rs.getInt("all_quality"));
                su.setHarm(rs.getInt("harm"));
                su.setCrit(rs.getInt("crit"));
                su.setCritHarm(rs.getInt("crit_harm"));
                su.setBossHarm(rs.getInt("boss_harm"));
                su.setHp(rs.getInt("hp"));
                su.setMp(rs.getInt("mp"));
                su.setPad(rs.getInt("pad"));
                su.setMatk(rs.getInt("matk"));
                su.setHaveNub(0);
                su.setEffective(false);
                suitSystems.add(su);
            }
            suitSystemsMap.clear();
            suitSystemsMap = suitSystems.stream().collect(Collectors.groupingBy(SuitSystem::getEquipList));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("套装属性异常：" + ex.getMessage());
        }
    }

    public static synchronized  void GetFieldSkills() {
        if (fieldSkills.size()>0) {
            fieldSkills = new ArrayList<>();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT characterid,skillid,skill_name,skill_leve,injuryinterval,injurydelaytime,damagedestructiontime,skillLX,skillLY,skillRX,skillRY,ranges,harm,dj_count,djsection FROM lt_field_skills");


            rs = ps.executeQuery();
            while (rs.next()) {
                FieldSkills su = new FieldSkills();
                su.setCharacterid(rs.getInt("characterid"));
                su.setSkillid(rs.getInt("skillid"));
                su.setSkill_name(rs.getString("skill_name"));
                su.setSkill_leve(rs.getInt("skill_leve"));
                su.setInjuryinterval(rs.getInt("injuryinterval"));
                su.setInjurydelaytime(rs.getInt("injurydelaytime"));
                su.setDamagedestructiontime(rs.getInt("damagedestructiontime"));
                su.setSkillLX(rs.getInt("skillLX"));
                su.setSkillLY(rs.getInt("skillLY"));
                su.setSkillRX(rs.getInt("skillRX"));
                su.setSkillRY(rs.getInt("skillRY"));
                su.setRange(rs.getInt("ranges"));
                su.setHarm(rs.getDouble("harm"));
                su.setDjCount(rs.getInt("dj_count"));
                su.setDjSection(rs.getInt("djsection"));
                fieldSkills.add(su);
            }
            fieldSkillsMap.clear();
            fieldSkillsMap = fieldSkills.stream().collect(Collectors.groupingBy(FieldSkills::getCharacterid));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("光环信息加载异常：" + ex.getMessage());
        }
    }

    public static synchronized  void GetSuperSkills() {
        if (superSkills.size()>0) {
            superSkills = new ArrayList<>();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT characterid,skillid,itemid,injuryinterval,injurydelaytime,skillLX,skillLY,skillRX,skillRY,damagedestructiontime,combinatorialCodingId,skill_name,skill_leve,ranges,skillCount,stackingDistance,harm FROM lt_super_skills");
            rs = ps.executeQuery();
            while (rs.next()) {
                SuperSkills su = new SuperSkills();
                su.setCharacterid(rs.getInt("characterid"));
                su.setSkillid(rs.getInt("skillid"));
                su.setItemid(rs.getInt("itemid"));
                su.setInjuryinterval(rs.getInt("injuryinterval"));
                su.setInjurydelaytime(rs.getInt("injurydelaytime"));
                su.setSkillLX(rs.getInt("skillLX"));
                su.setSkillLY(rs.getInt("skillLY"));
                su.setSkillRX(rs.getInt("skillRX"));
                su.setSkillRY(rs.getInt("skillRY"));
                su.setDamagedestructiontime(rs.getInt("damagedestructiontime"));
                su.setCombinatorialCodingId(rs.getString("combinatorialCodingId"));
                su.setSkill_name(rs.getString("skill_name"));
                su.setSkill_leve(rs.getInt("skill_leve"));
                su.setRange(rs.getInt("ranges"));
                su.setSkillCount(rs.getInt("skillCount"));
                su.setStackingDistance(rs.getInt("stackingDistance"));
                su.setHarm(rs.getInt("harm"));
                superSkills.add(su);
            }
            superSkillsMap.clear();
            superSkillsMap = superSkills.stream().collect(Collectors.groupingBy(SuperSkills::getCharacterid));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("超级技能信息加载异常：" + ex.getMessage());
        }
    }
    public static synchronized  void GetBreakthroughMechanism() {
          List<BreakthroughMechanism>  breakthroughMechanismList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT name,characterid,equal_order,localstr,localdex,localluk,localint,all_quality,harm,crit,crit_harm,boss_harm,hp,mp,pad,matk,customize_attribute,customize_smash_roll FROM lt_breakthrough_mechanism");
            rs = ps.executeQuery();
            while (rs.next()) {
                BreakthroughMechanism su = new BreakthroughMechanism();
                su.setName(rs.getString("name"));
                su.setCharacterid(rs.getInt("characterid"));
                su.setEqualOrder(rs.getString("equal_order"));
                su.setLocalstr(rs.getInt("localstr"));
                su.setLocaldex(rs.getInt("localdex"));
                su.setLocalluk(rs.getInt("localluk"));
                su.setLocalint(rs.getInt("localint"));
                su.setAllQuality(rs.getInt("all_quality"));
                su.setHarm(rs.getInt("harm"));
                su.setCrit(rs.getInt("crit"));
                su.setCritHarm(rs.getInt("crit_harm"));
                su.setBossHarm(rs.getInt("boss_harm"));
                su.setHp(rs.getInt("hp"));
                su.setMp(rs.getInt("mp"));
                su.setPad(rs.getInt("pad"));
                su.setMatk(rs.getInt("matk"));
                su.setCustomizeAttribute(rs.getInt("matk"));
                su.setCustomizeSmashRoll(rs.getInt("matk"));
                su.setCustomizeAttribute(rs.getInt("customize_attribute"));
                su.setCustomizeSmashRoll(rs.getInt("customize_smash_roll"));
                breakthroughMechanismList.add(su);
            }
            breakthroughMechanism.clear();
            breakthroughMechanism = breakthroughMechanismList.stream().collect(Collectors.groupingBy(BreakthroughMechanism::getCharacterid));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("境界系统加载异常：" + ex.getMessage());
        }
    }
    public static synchronized  void getNotParticipatingRecycling(){
        List<Integer> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT numb FROM lt_notparticipatingrecycling");
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(rs.getInt("numb"));
            }
            ps.close();
            con.close();
            NotParticipatingRecycling = list;
        } catch (SQLException ex) {
            System.out.println("getNotParticipatingRecycling出错：" + ex.getMessage());
        }
    }

    public static synchronized  void 清理物品表(){
        try {
            Connection con = DatabaseConnection.getConnection();

            for (int i = 0; i < 10; i++) {
                PreparedStatement ps = null;
                ResultSet rs = null;
                List<Integer> list = new ArrayList<>();
                try  {
                    ps = con.prepareStatement("SELECT inventoryitemid FROM inventoryequipment limit ?,10000");
                    ps.setInt(1, i);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        PreparedStatement ps1 = con.prepareStatement("SELECT inventoryitemid FROM inventoryitems where inventoryitemid = ? limit 1");
                        ps1.setInt(1, rs.getInt("inventoryitemid"));
                        ResultSet rs1 = ps1.executeQuery();
                        boolean flag = true;
                        while (rs1.next()) {
                            flag = false;
                        }
                        ps1.close();
                        if(flag){
                            list.add(rs.getInt("inventoryitemid"));
                        }

                    }
                    String sql = String.join(",", list.stream().map(String::valueOf).collect(Collectors.toList()));
                    String sql1 = "delete from inventoryequipment where inventoryitemid in (" + sql + ")";
                        PreparedStatement ps2 = con.prepareStatement(sql1);
                        ps2.execute();
                        ps2.close();

                    System.out.println(i);
                    rs.close();
                    ps.close();

                } catch (SQLException ex) {
                    System.out.println("清理物品表出错：" + ex.getMessage());
                }
            }
            con.close();
        } catch (SQLException e) {
            System.out.println("清理物品表出错：" + e.getMessage());

        }

    }


    public static synchronized  void getleveladdharm(){
        List<Leveladdharm> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT leve,numb FROM lt_leveladdharm");
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Leveladdharm(rs.getInt("leve"),rs.getInt("numb")));
            }
            ps.execute();
            ps.close();
            con.close();
            leveladdharm.clear();
            leveladdharm = list.stream().collect(Collectors.groupingBy(Leveladdharm::getLevel));
        } catch (SQLException ex) {
            System.out.println("getleveladdharm出错：" + ex.getMessage());
        }
    }
    public static synchronized void getAdditionalDamage(){
        List<LttItemAdditionalDamage> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try  {
            Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM ltt_item_additional_damage");
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LttItemAdditionalDamage(rs.getLong("id"),rs.getInt("item_id"),rs.getString("item_name"),rs.getLong("type"),rs.getInt("remark")));
            }
            ps.execute();
            ps.close();
            con.close();
            additionalDamage.clear();
            additionalDamage = list.stream().collect(Collectors.groupingBy(LttItemAdditionalDamage::getType));
        } catch (SQLException ex) {
            System.out.println("getleveladdharm出错：" + ex.getMessage());
        }
    }
    public static synchronized  void getMobInfo() {
          List<MobInfo>  mobInfoList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT id, mobId,boss,name, hp, mp, level, speed, eva, damage, exp, pdd from lt_mob_heavy_load");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobInfo su = new MobInfo();
                su.setId(rs.getLong("id"));
                su.setMobId(rs.getInt("mobId"));
                su.setBoss(rs.getInt("boss"));
                su.setName(rs.getString("name"));
                su.setHp(rs.getLong("hp"));
                su.setMp(rs.getInt("mp"));
                su.setLevel(rs.getInt("level"));
                su.setSpeed(rs.getInt("speed"));
                su.setEva(rs.getInt("eva"));
                su.setDamage(rs.getInt("damage"));
                su.setExp(rs.getInt("exp"));
                su.setPdd(rs.getInt("pdd"));
                mobInfoList.add(su);
            }
            mobInfoMap.clear();
            mobInfoMap = mobInfoList.stream().collect(Collectors.groupingBy(MobInfo::getMobId));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("怪物血量重载表加载异常：" + ex.getMessage());
        }
    }


    public static synchronized  void getMobUnhurt() {
        mobUnhurtList.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT id, mobId from lt_mob_unhurt");
            rs = ps.executeQuery();
            while (rs.next()) {
                mobUnhurtList.add(rs.getInt("mobId"));
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("怪物无伤表加载异常：" + ex.getMessage());
        }
    }

    /**
     * 全局爆率
     */
    public static synchronized  void setGlobaldrops() {
        globaldrops.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM drop_data_global WHERE chance > 0");
            rs = ps.executeQuery();
            while (rs.next()) {
                globaldrops.add(new MonsterGlobalDropEntry(rs.getInt("itemid"), rs.getInt("chance"),
                        rs.getInt("continent"),
                        rs.getByte("dropType"),
                        rs.getInt("minimum_quantity"),
                        rs.getInt("maximum_quantity"),
                        rs.getShort("questid")));
            }
            rs.close();
            ps.close();
            con.close();
            System.out.println("全局爆率加载成功,加载的怪物数量：" + globaldrops.size());

        } catch (SQLException ex) {
            System.out.println("全局爆率加载异常：" + ex.getMessage());
        }
    }

    /**
     * 怪物独立爆率  `dropperid` int(11) NOT NULL,
     *   `droppername` varchar(255) NOT NULL,
     *   `itemid` int(11) NOT NULL DEFAULT '0',
     *   `itemname` varchar(255) NOT NULL,
     *   `minimum_quantity` int(11) NOT NULL DEFAULT '1',
     *   `maximum_quantity` int(11) NOT NULL DEFAULT '1',
     *   `questid` int(11) NOT NULL DEFAULT '0',
     *   `chance` int(11) NOT NULL DEFAULT '0',
     */
    public static synchronized void setLtDiabloEquipments() {
        LtDiabloEquipments.setLtDiabloEquipments();
    }
    /**
     * 怪物独立爆率  `dropperid` int(11) NOT NULL,
     *   `droppername` varchar(255) NOT NULL,
     *   `itemid` int(11) NOT NULL DEFAULT '0',
     *   `itemname` varchar(255) NOT NULL,
     *   `minimum_quantity` int(11) NOT NULL DEFAULT '1',
     *   `maximum_quantity` int(11) NOT NULL DEFAULT '1',
     *   `questid` int(11) NOT NULL DEFAULT '0',
     *   `chance` int(11) NOT NULL DEFAULT '0',
     */
    public static synchronized void setdrops() {
        drops.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM drop_data WHERE chance > 0");
            rs = ps.executeQuery();
            while (rs.next()) {
                drops.add(new MonsterDropEntry(rs.getShort("questid"),
                        rs.getInt("dropperid"),
                        rs.getInt("itemid"),
                        rs.getInt("chance"),
                        rs.getInt("minimum_quantity"),
                        rs.getInt("maximum_quantity")));
            }
            rs.close();
            ps.close();
            con.close();
            dropsMap.clear();
            dropsMap = drops.stream().collect(Collectors.groupingBy(MonsterDropEntry::getDropperid));
            dropsMapCount.clear();
            for (MonsterDropEntry drop : drops) {
                dropsMapCount.put(drop.getDropperid(), dropsMap.get(drop.getDropperid()).size());
            }
            System.out.println("怪物掉落加载成功,加载的怪物数量：" + drops.size());
        } catch (SQLException ex) {
            System.out.println("怪物独立爆率加载异常：" + ex.getMessage());
        }
    }
    /**
     * 怪物独立爆率  `dropperid` int(11) NOT NULL,
     *   `droppername` varchar(255) NOT NULL,
     *   `itemid` int(11) NOT NULL DEFAULT '0',
     *   `itemname` varchar(255) NOT NULL,
     *   `minimum_quantity` int(11) NOT NULL DEFAULT '1',
     *   `maximum_quantity` int(11) NOT NULL DEFAULT '1',
     *   `questid` int(11) NOT NULL DEFAULT '0',
     *   `chance` int(11) NOT NULL DEFAULT '0',
     */
    public static synchronized void setdropsTwo() {
        dropsTwo.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM drop_data_two WHERE chance > 0");
            rs = ps.executeQuery();
            while (rs.next()) {
                dropsTwo.add(new MonsterDropEntry(rs.getShort("questid"),
                        rs.getInt("dropperid"),
                        rs.getInt("itemid"),
                        rs.getInt("chance"),
                        rs.getInt("minimum_quantity"),
                        rs.getInt("maximum_quantity")));
            }
            rs.close();
            ps.close();
            con.close();
            dropsMap.clear();
            dropsMapTwo = dropsTwo.stream().collect(Collectors.groupingBy(MonsterDropEntry::getDropperid));
            dropsMapCountTwo.clear();
            for (MonsterDropEntry drop : dropsTwo) {
                dropsMapCountTwo.put(drop.getDropperid(), dropsMapTwo.get(drop.getDropperid()).size());
            }
            System.out.println("怪物掉落加载成功,加载的怪物数量：" + dropsTwo.size());
        } catch (SQLException ex) {
            System.out.println("怪物爆率加载异常：" + ex.getMessage());
        }
    }

        /**
         * 查询所有物品信息
         *
         * @return ItemInfo 列表
         */
        public static void getAllItemInfo() {
            List<ItemInfo> itemInfos = new ArrayList<>();
            String sql = "SELECT * FROM lt_item_info";
            PreparedStatement ps = null;
            ResultSet resultSet = null;
            try {
                Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
                ps = con.prepareStatement(sql);
                resultSet = ps.executeQuery();
                while (resultSet.next()) {
                    ItemInfo itemInfo = new ItemInfo();
                    itemInfo.setId(resultSet.getLong("id"));
                    itemInfo.setItemId(resultSet.getInt("item_id"));
                    itemInfo.setUpgradeSlots(resultSet.getByte("upgradeslots"));
                    itemInfo.setStr(resultSet.getShort("str"));
                    itemInfo.setDex(resultSet.getShort("dex"));
                    itemInfo.setIntValue(resultSet.getShort("int"));
                    itemInfo.setLuk(resultSet.getShort("luk"));
                    itemInfo.setHp(resultSet.getShort("hp"));
                    itemInfo.setMp(resultSet.getShort("mp"));
                    itemInfo.setWatk(resultSet.getShort("watk"));
                    itemInfo.setMatk(resultSet.getShort("matk"));
                    itemInfo.setWdef(resultSet.getShort("wdef"));
                    itemInfo.setMdef(resultSet.getShort("mdef"));

                    itemInfos.add(itemInfo);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (itemInfos.size() > 0) {
                RedisUtil.del(RedisUtil.KEYNAMES.ITEM_INFO.getKeyName(), RedisUtil.KEYNAMES.ITEM_INFO.getKeyName());
                RedisUtil.hset(RedisUtil.KEYNAMES.ITEM_INFO.getKeyName(), RedisUtil.KEYNAMES.ITEM_INFO.getKeyName(),JSONObject.toJSONString(itemInfos.stream().collect(Collectors.groupingBy(ItemInfo::getItemId))));
                Start.itemInfo.clear();
                Start.itemInfo = itemInfos.stream().collect(Collectors.groupingBy(ItemInfo::getItemId));
            }
        }

    /**
     *
     * 获取抽奖物品配置
     */
    public static synchronized  void setLtMxdPrize() {
        ltMxdPrize.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM lt_mxd_prize ");
            rs = ps.executeQuery();
            while (rs.next()) {
                ltMxdPrize.add(new LtMxdPrize(
                        rs.getLong("id"),
                        rs.getInt("type"),
                        rs.getInt("itemid"),
                        rs.getInt("price"),
                        rs.getByte("price_type"),
                        rs.getInt("prob"),
                        rs.getInt("counts"),
                        rs.getInt("notice"),
                        rs.getString("remark")));
            }
            rs.close();
            ps.close();
            con.close();
            System.out.println("获取抽奖物品配置加载成功：" + ltMxdPrize.size());

        } catch (SQLException ex) {
            System.out.println("获取抽奖物品配置加载异常：" + ex.getMessage());
        }
    }
    /**
     * 获取战令列表
     */
    public static synchronized  void setLtZlTask() {
        ltZlTask.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM lt_zl_task ");
            rs = ps.executeQuery();
            while (rs.next()) {
                ltZlTask.add(new LtZlTask(
                        rs.getLong("id"),
                        rs.getInt("time_code"),
                        rs.getInt("pt_zl_jf"),
                        rs.getInt("gj_zl_jf"),
                        rs.getByte("task_type"),
                        rs.getString("code_list"),
                        rs.getString("code_number"),
                        rs.getString("remark")));
            }
                rs.close();
            ps.close();
            con.close();
            System.out.println("获取战令列表加载成功：" + ltZlTask.size());

        } catch (SQLException ex) {
            System.out.println("获取战令列表加载异常：" + ex.getMessage());
        }
    }
    /**
     * 查账户是否存在
     */
    public static  synchronized boolean gatEwayAccountExists(String account) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection  con = (Connection) DBConPool.getInstance().getDataSource().getConnection()){
            ps = con.prepareStatement("SELECT * FROM qq_membersinfo where qq = ? limit 1");
            ps.setString(1, account);
            rs = ps.executeQuery();
               if (rs.next()){
                rs.close();
                ps.close();
                con.close();
                return false;
            }
        } catch (SQLException ex) {
            System.out.println("查账户是否存在异常：" + ex.getMessage());
        }
        return true;
    }
    /**
     * 添加QQ账户
     */
    public static synchronized  void addEwayAccount(String account) {
        PreparedStatement ps = null;
        try (Connection  con = (Connection) DBConPool.getInstance().getDataSource().getConnection()){
            ps = con.prepareStatement("INSERT INTO qq_membersinfo (qq) VALUES (?)");
            ps.setString(1, account);
            ps.execute();
                ps.close();
                con.close();
        } catch (SQLException ex) {
            System.out.println("查账户是否存在异常：" + ex.getMessage());
        }
    }
    /**
     * 全局爆率
     */
    public static synchronized  void setLtSkillWucdTable() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT * FROM lt_skill_wucd_table");
            rs = ps.executeQuery();
            ltSkillWucdTable.clear();
            while (rs.next()) {
                ltSkillWucdTable.put(rs.getInt("skillid"),rs.getInt("cd"));
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("无CD技能加载异常：" + ex.getMessage());
        }
    }
    public static synchronized  void GetFuMoInfo() {
        FuMoInfoMap.clear();
        服务端输出信息.println_out("○ 开始加载镶嵌装备效果");

        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT fumoType, fumoName, fumoInfo FROM mxmxd_fumo_info");
                Throwable var3 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    Throwable var5 = null;

                    try {
                        while(rs.next()) {
                            int fumoType = rs.getInt("fumoType");
                            String fumoName = rs.getString("fumoName");
                            String fumoInfo = rs.getString("fumoInfo");
                            FuMoInfoMap.put(fumoType, new String[]{fumoName, fumoInfo});
                        }
                    } catch (Throwable var54) {
                        var5 = var54;
                        throw var54;
                    } finally {
                        if (rs != null) {
                            if (var5 != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var53) {
                                    var5.addSuppressed(var53);
                                }
                            } else {
                                rs.close();
                            }
                        }

                    }

                    DBConPool.close(ps);
                } catch (Throwable var56) {
                    var3 = var56;
                    throw var56;
                } finally {
                    if (ps != null) {
                        if (var3 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var52) {
                                var3.addSuppressed(var52);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var58) {
                var1 = var58;
                throw var58;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var51) {
                            var1.addSuppressed(var51);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var60) {
            服务端输出信息.println_err("○ 加载镶嵌装备效果预览失败。");
        }

    }

    public static synchronized  void loadPotentialMap() {
        potentialListMap.clear();
        服务端输出信息.println_out("○ 开始加载潜能列表");

        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT potentialtype, potentialname, potentialinfo FROM snail_potential_list");
                Throwable var3 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    Throwable var5 = null;

                    try {
                        while(rs.next()) {
                            int potentialType = rs.getInt("potentialtype");
                            String potentialName = rs.getString("potentialname");
                            String potentialInfo = rs.getString("potentialinfo");
                            potentialListMap.put(potentialType, new String[]{potentialName, potentialInfo});
                        }
                    } catch (Throwable var54) {
                        var5 = var54;
                        throw var54;
                    } finally {
                        if (rs != null) {
                            if (var5 != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var53) {
                                    var5.addSuppressed(var53);
                                }
                            } else {
                                rs.close();
                            }
                        }

                    }

                    DBConPool.close(ps);
                } catch (Throwable var56) {
                    var3 = var56;
                    throw var56;
                } finally {
                    if (ps != null) {
                        if (var3 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var52) {
                                var3.addSuppressed(var52);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var58) {
                var1 = var58;
                throw var58;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var51) {
                            var1.addSuppressed(var51);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var60) {
            服务端输出信息.println_err("○ 加载潜能列表失败。" + var60);
        }

    }

    public static synchronized  void GetfiveTurn() {
     List<FiveTurn> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT charactersid,occupation_id,occupation_name FROM lt_five_turn");
            rs = ps.executeQuery();
            while (rs.next()) {
                FiveTurn su = new FiveTurn();
                su.setCharactersid(rs.getInt("charactersid"));
                su.setOccupationId(rs.getInt("occupation_id"));
                su.setOccupationName(rs.getString("occupation_name"));
                list.add(su);
            }
            fiveTurn.clear();
            fiveTurn = list.stream().collect(Collectors.groupingBy(FiveTurn::getCharactersid));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("5转数据获取异常：" + ex.getMessage());
        }
    }

    public static synchronized  void saveAttackInfo(AttackInfo info) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("insert into lt_attack_info_skills (charge,skill,lastAttackTickCount,hits,targets,tbyte,display,animation,speed,csstar,AOE,slot,unk,allDamage,position,positionxy) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, info.getCharge());
            ps.setInt(2, info.skill);
            ps.setInt(3, info.lastAttackTickCount);
            ps.setInt(4, info.hits);
            ps.setInt(5, info.targets);
            ps.setInt(6, info.tbyte);
            ps.setInt(7, info.display);
            ps.setInt(8, info.animation);
            ps.setInt(9, info.speed);
            ps.setInt(10,info.csstar);
            ps.setInt(11,info.AOE);
            ps.setInt(12,info.slot);
            ps.setInt(13,info.unk);
            ps.setString(14,JSONObject.toJSONString(info.allDamage));
            ps.setString(15,JSONObject.toJSONString(info.position));
            ps.setString(16,JSONObject.toJSONString(info.positionxy));


            ps.execute();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("技能封包保存异常：" + ex.getMessage());
        }
    }
    // 查询所有 LtMonsterSkill 记录
    public synchronized static void findLtMonsterSkill() {
        List<LtMonsterSkill> skillList = new ArrayList<>();
        String sql = "SELECT id, type, monsterid, skillid, level, attackid ,skillcd FROM lt_monster_skill";

        try (Connection con = DBConPool.getInstance().getDataSource().getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LtMonsterSkill skill = new LtMonsterSkill();
                skill.setId(rs.getLong("id"));
                skill.setType(rs.getInt("type"));
                skill.setMonsterId(rs.getInt("monsterid"));
                skill.setSkillId(rs.getInt("skillid"));
                skill.setLevel(rs.getInt("level"));
                skill.setAttackId(rs.getInt("attackid"));
                skill.setSkillcd(rs.getLong("skillcd"));
                skillList.add(skill);
            }

            ltMonsterSkillBuffSkill.clear();
            ltMonsterSkillBuffSkill = skillList.stream().filter(s -> s.getType() == 1).collect(Collectors.toList());
            ltMonsterSkillAttackSkill.clear();
            ltMonsterSkillAttackSkill = skillList.stream().filter(s -> s.getType() == 2).collect(Collectors.groupingBy(LtMonsterSkill::getMonsterId));
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("怪物技能加载异常");
        }

    }

    public static synchronized  void getAttackInfo( ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<AttackInfo> list = new ArrayList<>();
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("select charge,skill,lastAttackTickCount,hits,targets,tbyte,display,animation,speed,csstar,AOE,slot,unk from lt_attack_info_skills");
            rs = ps.executeQuery();
            while (rs.next()) {
                AttackInfo su = new AttackInfo();
                su.setCharge(rs.getInt("charge"));
                su.setSkill(rs.getInt("skill"));
                su.setLastAttackTickCount(rs.getInt("lastAttackTickCount"));
                su.setHits((byte) rs.getInt("hits"));
                su.setTargets((byte) rs.getInt("targets"));
                su.setTbyte((byte) rs.getInt("tbyte"));
                su.setDisplay((byte) rs.getInt("display"));
                su.setAnimation((byte) rs.getInt("animation"));
                su.setSpeed((byte) rs.getInt("speed"));
                su.setCsstar((byte) rs.getInt("csstar"));
                su.setAOE((byte) rs.getInt("AOE"));
                su.setSlot((byte) rs.getInt("slot"));
                su.setUnk((byte) rs.getInt("unk"));
                list.add(su);
            }
            allAttackInfo.clear();
            allAttackInfo = list.stream().collect(Collectors.groupingBy(AttackInfo::getSkill));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("技能封包保存异常：" + ex.getMessage());
        }
    }
    public static synchronized  void getLtMobSpawnBoss( ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<LtMobSpawnBoss> list = new ArrayList<>();
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("select * from lt_mob_spawn_boss");
            rs = ps.executeQuery();
            while (rs.next()) {
                LtMobSpawnBoss su = new LtMobSpawnBoss(rs.getInt("mapid"),
                        rs.getInt("mobid"),
                        rs.getString("name"),
                        rs.getInt("x"),
                        rs.getInt("y"),
                        rs.getInt("x1"),
                        rs.getInt("y1"),
                        rs.getInt("x2"),
                        rs.getInt("y2"),
                        rs.getInt("time"));
                list.add(su);

            }
            ltMobSpawnBoss.clear();
            ltMobSpawnBoss = list.stream().collect(Collectors.groupingBy(LtMobSpawnBoss::getMapid));
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("读取野外BOSS刷新异常：" + ex.getMessage());
        }
    }



    public static ArrayList<String> 公告列表;
    public static void 公告初始化() {
        Start.公告列表.clear();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT * FROM messages");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("message");
                Start.公告列表.add(name);
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.err.println("读取动态数据库出错：" + ex.getMessage());
        }
        finally {
            DBConPool.cleanUP(rs, ps, con);
        }
    }

    public static void GetSkillTable() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("select * from skilltest");
            rs = ps.executeQuery();
            while (rs.next()) {
                final int skillid = rs.getInt("skillid");
                final int attackcount = rs.getInt("attackcount");
                final int mobcount = rs.getInt("mobcount");
                Start.skillType.add(new SkillType(skillid, mobcount, attackcount));
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.err.println("读取技能表出错：" + ex.getMessage());
        }
        finally {
            DBConPool.cleanUP(rs, ps, con);
        }
    }

    public static void GetExpTable() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT name, numb FROM exptable");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                final int val = rs.getInt("numb");
                Start.exptable.add(new Pair(name, Integer.valueOf(val)));
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.err.println("读取不同阶段经验表出错：" + ex.getMessage());
        }
        finally {
            DBConPool.cleanUP(rs, ps, con);
        }
    }

    public static void GetMobInMapTable() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT mobid, map,x,y,msg,time FROM bossmobinmap");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String msg = rs.getString("msg");
                final int mobid = rs.getInt("mobid");
                final int map = rs.getInt("map");
                final int x = rs.getInt("x");
                final int y = rs.getInt("y");
                final int time = rs.getInt("time");
                Start.野外boss刷新.add(new BossInMap(mobid, map, x, y, msg, time));
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.err.println("读取道具经验表出错：" + ex.getMessage());
        }
        finally {
            DBConPool.cleanUP(rs, ps, con);
        }
    }
    public static void GetRechargeTable() {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT name FROM rechargeableItems");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("name");
                Start.子弹列表.add(name);
            }
            rs.close();
            ps.close();
            con.close();
        }
        catch (SQLException ex) {
            System.err.println("读取子弹列表出错：" + ex.getMessage());
        }
        finally {
            DBConPool.cleanUP(rs, ps, con);
        }
    }

    private static void 清空弹夹() {
        Delete("accounts", 1);
        Delete("accounts_info", 2);
        Delete("auctionitems", 3);
        Delete("aclog", 4);
        Delete("addlog", 5);
        Delete("alliances", 6);
        Delete("auth_server_channel", 7);
        Delete("auth_server_channel_ip", 8);
        Delete("auth_server_cs", 9);
        Delete("auth_server_login", 10);
        Delete("auth_server_mts", 11);
        Delete("auctionpoint", 12);
        Delete("auctionpoint1", 13);
        Delete("bank_item", 20);
        Delete("bank_item1", 21);
        Delete("bank_item2", 22);
        Delete("bbs_replies", 23);
        Delete("bbs_threads", 24);
        Delete("blocklogin", 25);
        Delete("bosslog", 26);
        Delete("bossrank", 27);
        Delete("bossrank1", 28);
        Delete("bossrank2", 29);
        Delete("bossrank3", 30);
        Delete("bossrank4", 31);
        Delete("bossrank5", 32);
        Delete("bossrank6", 33);
        Delete("bossrank7", 34);
        Delete("bossrank8", 35);
        Delete("bossrank9", 36);
        Delete("buddies", 37);
        Delete("capture_cs", 38);
        Delete("capture_jl", 39);
        Delete("capture_zj", 40);
        Delete("capture_zk", 41);
        Delete("character_slots", 42);
        Delete("cashshop_limit_sell", 43);
        Delete("character7", 44);
        Delete("charactera", 45);
        Delete("characters", 46);
        Delete("characterz", 47);
        Delete("cheatlog", 48);
        Delete("csequipment", 49);
        Delete("csitems", 50);
        Delete("dangerousacc", 55);
        Delete("dangerousip", 55);
        Delete("divine", 56);
        Delete("dueyequipment", 57);
        Delete("dueyitems", 58);
        Delete("dueypackages", 59);
        Delete("eventstats", 60);
        Delete("famelog", 61);
        Delete("families", 62);
        Delete("fishingjf", 63);
        Delete("fubenjilu", 64);
        Delete("fullpoint", 65);
        Delete("FengYeDuan", 66);
        Delete("forum_reply", 67);
        Delete("forum_section", 68);
        Delete("forum_thread", 69);
        Delete("game_poll_reply", 70);
        Delete("gbook_admin", 71);
        Delete("gbook_setting", 72);
        Delete("gifts", 73);
        Delete("gmlog", 74);
        Delete("guiidld", 75);
        Delete("guilds", 76);
        Delete("guildsl", 77);
        Delete("hiredmerch", 78);
        Delete("hiredmerchequipment", 79);
        Delete("hiredmerchitems", 80);
        Delete("hiredmerch", 81);
        Delete("htsquads", 82);
        Delete("inventoryequipment", 83);
        Delete("inventoryitems", 84);
        Delete("inventorylog", 85);
        Delete("inventoryslot", 86);
        Delete("invitecodedata", 87);
        Delete("ipbans", 88);
        Delete("ipcheck", 89);
        Delete("ipvotelog", 90);
        Delete("keymap", 91);
        Delete("loginlog", 92);
        Delete("lottery_info", 93);
        Delete("lottery_player_info", 94);
        Delete("macbans", 95);
        Delete("mapidban", 96);
        Delete("macfilters", 97);
        Delete("monsterbook", 98);
        Delete("mountdata", 99);
        Delete("mts_cart", 100);
        Delete("mts_items", 101);
        Delete("mtsequipment", 102);
        Delete("mtsitems", 103);
        Delete("mtstransfer", 104);
        Delete("mtstransferequipment", 105);
        Delete("mulungdojo", 106);
        Delete("notes", 107);
        Delete("nxcode", 108);
        Delete("nxcodez", 109);
        Delete("nxitemlist", 110);
        Delete("onetimelog", 111);
        Delete("pets", 112);
        Delete("pnpc", 113);
        Delete("playernpcs", 114);
        Delete("playernpcs_equip", 115);
        Delete("prizelog", 116);
        Delete("qqlog", 117);
        Delete("qqstem", 118);
        Delete("questactions", 119);
        Delete("questinfo", 120);
        Delete("questrequirements", 121);
        Delete("queststatus", 122);
        Delete("queststatusmobs", 123);
        Delete("rcmedals", 124);
        Delete("regrocklocations", 125);
        Delete("reports", 126);
        Delete("rings", 127);
        Delete("saiji", 128);
        Delete("savedlocations", 129);
        Delete("skillmacros", 130);
        Delete("skills", 131);
        Delete("skills_cooldowns", 132);
        Delete("speedruns", 133);
        Delete("storages", 134);
        Delete("stjflog", 134);
        Delete("stlog", 134);
        Delete("trocklocations", 135);
        Delete("uselog", 136);
        Delete("zaksquads", 137);
        Delete("z_pg", 138);
        Delete("bank", 139);
        Delete("mail", 140);
        Delete("jiezoudashi", 141);
        Delete("bosslog1", 142);
        Delete("bosslog2", 143);
        Delete("bosslog3", 144);
        Delete("shouce", 145);
        Delete("lt_breakthrough_mechanism", 146);
        Delete("lt_five_turn", 147);
        Delete("lt_public_record", 148);
        Delete("suitdamtablenew", 149);
        Delete("pqlog", 150);
        Delete("lt_attack_info_skills", 151);
        Delete("lt_field_skills", 152);
        Delete("lt_super_skills", 153);


    }
    private static void Delete( String a,  int b) {
        Connection con = DatabaseConnection.getConnection();
        try {
            final PreparedStatement ps = con.prepareStatement("Delete from " + a + "");
            ps.executeUpdate();
            ps.close();
            con.close();
        }
        catch (SQLException e) {
            System.out.println("Error/" + a + ":" + (Object)e);
        }
    }

    public static void 读取技个人信息设置() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT name, val FROM jiezoudashi");
                Throwable var3 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    Throwable var5 = null;

                    try {
                        while(rs.next()) {
                            String name = rs.getString("name");
                            int val = rs.getInt("val");
                            个人信息设置.put(name, val);
                        }
                    } catch (Throwable var53) {
                        var5 = var53;
                        throw var53;
                    } finally {
                        if (rs != null) {
                            if (var5 != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var52) {
                                    var5.addSuppressed(var52);
                                }
                            } else {
                                rs.close();
                            }
                        }

                    }

                    DBConPool.close(ps);
                } catch (Throwable var55) {
                    var3 = var55;
                    throw var55;
                } finally {
                    if (ps != null) {
                        if (var3 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var51) {
                                var3.addSuppressed(var51);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var57) {
                var1 = var57;
                throw var57;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var50) {
                            var1.addSuppressed(var50);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var59) {
            服务端输出信息.println_err("读取个人信息设置错误：" + var59.getMessage());
        }

    }

    public static void checkCPU(int seconds) {
        服务端输出信息.println_out("【读取中】 加载CPU优化线程:::");
        WorldTimer.getInstance().register(new Runnable() {
            public void run() {
                try {
                    int a = (Integer)LtMS.ConfigValuesMap.get("启动优化CPU百分比");
                    if (a < 10) {
                        a = 10;
                    }

                    double cpu = CPUSampler.getProcessCpuLoad();
                    if (cpu > (double)(Integer)LtMS.ConfigValuesMap.get("启动宠物捡取优化CPU百分比")) {
                        ServerConstants.canPetLoot = false;
                    } else {
                        ServerConstants.canPetLoot = true;
                    }

                    if (!(cpu > (double)a)) {
                        MapleMap.canSpawnForCPU = true;
                    } else {
                        int b;
                        if (a > 80) {
                            b = 100;
                        } else {
                            b = a + 20;
                        }

                        if (!(cpu > (double)b)) {
                            服务端输出信息.println_out("【CPU优化】 检测到CPU占用大于" + a + "%，开启性能优化！");
                            MapleMap.canSpawnForCPU = false;
                        } else {
                            服务端输出信息.println_out("【CPU优化】 检测到CPU占用大于55%，开启第二级优化！");
                            MapleMap.canSpawnForCPU = false;
                            Iterator var5 = ChannelServer.getAllInstances().iterator();

                            label126:
                            while(true) {
                                if (!var5.hasNext()) {
                                    while(CPUSampler.getProcessCpuLoad() > (double)a) {
                                        MapleMap.canSpawnForCPU = false;
                                        Thread.sleep(100L);
                                    }

                                    int i = 0;

                                    while(true) {
                                        if (i >= 100) {
                                            break label126;
                                        }

                                        MapleMap.canSpawnForCPU = false;
                                        Thread.sleep(100L);
                                        ++i;
                                    }
                                }

                                ChannelServer csx = (ChannelServer)var5.next();
                                Iterator var7 = csx.getMapFactory().getAllMapThreadSafe().iterator();

                                while(true) {
                                    MapleMap mapx;
                                    do {
                                        if (!var7.hasNext()) {
                                            continue label126;
                                        }

                                        mapx = (MapleMap)var7.next();
                                    } while(mapx == null);

                                    Iterator var9 = mapx.getAllMonster().iterator();

                                    while(var9.hasNext()) {
                                        MapleMonster mob = (MapleMonster)var9.next();
                                        if (mob != null && !mob.getStats().isBoss()) {
                                            mob.sendBlueDamage(mob.getHp(), true);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Iterator var13 = ChannelServer.getAllInstances().iterator();

                    label93:
                    while(var13.hasNext()) {
                        ChannelServer cs = (ChannelServer)var13.next();
                        Iterator var16 = cs.getMapFactory().getAllMapThreadSafe().iterator();

                        label91:
                        while(true) {
                            MapleMap map;
                            do {
                                do {
                                    if (!var16.hasNext()) {
                                        continue label93;
                                    }

                                    map = (MapleMap)var16.next();
                                } while(map == null);
                            } while(map.getAllPlayersThreadsafe().size() <= 50);

                            Iterator var18 = map.getAllPlayersThreadsafe().iterator();

                            while(true) {
                                MapleCharacter chr;
                                do {
                                    if (!var18.hasNext()) {
                                        continue label91;
                                    }

                                    chr = (MapleCharacter)var18.next();
                                } while(chr == null);

                                Iterator var20 = chr.getSummonedPets().iterator();

                                while(var20.hasNext()) {
                                    MaplePet pet = (MaplePet)var20.next();
                                    if (pet != null) {
                                        chr.unequipPet(pet, false);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception var12) {
                    服务端输出信息.println_err("【错误】checkCPU线程错误，错误原因：" + var12);
                    var12.printStackTrace();
                }

            }
        }, (long)(1000 * seconds));
    }

    public static void 每日双倍(int time) {
        服务端输出信息.println_out("【读取中】 加载每日双倍经验:::");
        WorldTimer.getInstance().register(new Runnable() {
            public void run() {
                int 时 = Calendar.getInstance().get(11);
                int 分 = Calendar.getInstance().get(12);
                float old_rate = WorldConstants.EXP_RATE;
                if ((Integer)LtMS.ConfigValuesMap.get("每日双倍开关") > 0) {
                    Iterator var4;
                    ChannelServer cserv1;
                    Iterator var6;
                    MapleCharacter mch;
                    if (时 == 18 && 分 == 0) {
                        var4 = ChannelServer.getAllInstances().iterator();

                        while(var4.hasNext()) {
                            cserv1 = (ChannelServer)var4.next();
                            var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                            while(var6.hasNext()) {
                                mch = (MapleCharacter)var6.next();
                                mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验将在60分钟后开启，持续3小时。");
                            }
                        }
                    }

                    if (时 == 18 && 分 == 30) {
                        var4 = ChannelServer.getAllInstances().iterator();

                        while(var4.hasNext()) {
                            cserv1 = (ChannelServer)var4.next();
                            var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                            while(var6.hasNext()) {
                                mch = (MapleCharacter)var6.next();
                                mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验将在30分钟后开启，持续3小时。");
                            }
                        }
                    }

                    if (时 == 18 && 分 == 55) {
                        var4 = ChannelServer.getAllInstances().iterator();

                        while(var4.hasNext()) {
                            cserv1 = (ChannelServer)var4.next();
                            var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                            while(var6.hasNext()) {
                                mch = (MapleCharacter)var6.next();
                                mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验将在5分钟后开启，持续3小时。");
                            }
                        }
                    }

                    if (时 == 19 && !Start.双倍经验开关) {
                        var4 = ChannelServer.getAllInstances().iterator();

                        while(var4.hasNext()) {
                            cserv1 = (ChannelServer)var4.next();
                            var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                            while(var6.hasNext()) {
                                mch = (MapleCharacter)var6.next();
                                mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验已开启，将持续3小时，大家抓紧时间打怪升级吧。");
                            }
                        }

                        WorldConstants.EXP_RATE = old_rate * 2.0F;
                        Start.双倍经验开关 = true;

                        try {
                            Thread.sleep(10800000L);
                        } catch (InterruptedException var8) {
                            Logger.getLogger(Start.class.getName()).log(Level.SEVERE, (String)null, var8);
                            服务端输出信息.println_err("【错误】每日双倍运行错误，代码位置：server.Start.每日双倍，错误原因：" + var8);
                        }

                        var4 = ChannelServer.getAllInstances().iterator();

                        while(var4.hasNext()) {
                            cserv1 = (ChannelServer)var4.next();
                            var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                            while(var6.hasNext()) {
                                mch = (MapleCharacter)var6.next();
                                mch.dropMessage(6, "[每日双倍经验]: 今天的双倍经验已结束，经验值调整为正常倍率。");
                            }
                        }

                        WorldConstants.EXP_RATE = old_rate;
                        Start.双倍经验开关 = false;
                    }

                }
            }
        }, (long)('\uea60' * time));
    }

    public static void 每日双倍(int 时, int 分) {
        int startTime = (Integer)LtMS.ConfigValuesMap.get("每日双倍起始时间");
        int stopTime = (Integer)LtMS.ConfigValuesMap.get("每日双倍结束时间");
        if ((Integer)LtMS.ConfigValuesMap.get("每日双倍开关") <= 0) {
            if (分 % 30 == 0) {
                服务端输出信息.println_out("【循环线程】 每日双倍经验为关闭状态:::");
            }

        } else if (时 == 0 && 分 == 0 && 双倍经验开关) {
            WorldConstants.EXP_RATE = oldExpRate;
            ChannelServer.reloadExpRate();
            双倍经验开关 = false;
            服务端输出信息.println_out("【循环线程】 检测到每日双倍经验未正常结束，已进行结束处理:::");
        } else {
            Iterator var4;
            ChannelServer cserv1;
            Iterator var6;
            MapleCharacter mch;
            if (时 == startTime - 1 && 分 == 0) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验将在60分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if (时 == startTime - 1 && 分 == 30) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验将在30分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if (时 == startTime - 1 && 分 == 55) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验将在5分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            MapleMap map;
            if (时 == startTime && !双倍经验开关) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍经验]: 每日双倍经验已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪升级吧。");
                    }

                    var6 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var6.hasNext()) {
                        map = (MapleMap)var6.next();
                        if (map != null) {
                            map.startMapEffect("[每日双倍经验]: 每日双倍经验已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪升级吧。", 5121006);
                        }
                    }
                }

                oldExpRate = WorldConstants.EXP_RATE;
                WorldConstants.EXP_RATE = oldExpRate * 2.0F;
                ChannelServer.reloadExpRate();
                双倍经验开关 = true;
            }

            if (时 == stopTime && 双倍经验开关) {
                WorldConstants.EXP_RATE = oldExpRate;
                ChannelServer.reloadExpRate();
                双倍经验开关 = false;
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍经验]: 今天的双倍经验已结束，经验值调整为正常倍率。");
                    }

                    var6 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var6.hasNext()) {
                        map = (MapleMap)var6.next();
                        if (map != null) {
                            map.startMapEffect("[每日双倍经验]: 今天的双倍经验已结束，经验值调整为正常倍率。", 5121006);
                        }
                    }
                }
            }

            if (分 % 30 == 0) {
                服务端输出信息.println_out("【每日双倍经验】 检测每日双倍经验:::");
                if (双倍经验开关) {
                    服务端输出信息.println_out("【每日双倍经验】 双倍经验正在进行中。");
                } else {
                    服务端输出信息.println_out("【每日双倍经验】 不在双倍经验时间段。");
                }
            }

        }
    }

    public static void 周末双倍(int 时, int 分) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        int week = calendar.get(7);
        --week;
        if (week == 0) {
            week = 7;
        }

        int startTime = (Integer)LtMS.ConfigValuesMap.get("周末双倍起始时间");
        int stopTime = (Integer)LtMS.ConfigValuesMap.get("周末双倍结束时间");
        if ((Integer)LtMS.ConfigValuesMap.get("周末双倍开关") <= 0) {
            if (分 % 30 == 0) {
                服务端输出信息.println_out("【循环线程】 周末双倍经验为关闭状态:::");
            }

        } else if (week >= 1 && week <= 5 && 时 == 0 && 周末双倍经验开关) {
            WorldConstants.EXP_RATE = oldExpRateWeek;
            ChannelServer.reloadExpRate();
            周末双倍经验开关 = false;
            服务端输出信息.println_out("【循环线程】 检测到周末双倍经验未正常结束，已进行结束处理:::");
        } else {
            Iterator var6;
            ChannelServer cserv1;
            Iterator var8;
            MapleCharacter mch;
            if ((week == 6 || week == 7) && 时 == startTime - 1 && 分 == 0) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍经验]: 周末双倍经验将在60分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if ((week == 6 || week == 7) && 时 == startTime - 1 && 分 == 30) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍经验]: 周末双倍经验将在30分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if ((week == 6 || week == 7) && 时 == startTime - 1 && 分 == 55) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍经验]: 周末双倍经验将在5分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            MapleMap map;
            if ((week == 6 || week == 7) && 时 == startTime && !周末双倍经验开关) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍经验]: 周末双倍经验已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪升级吧。");
                    }

                    var8 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var8.hasNext()) {
                        map = (MapleMap)var8.next();
                        if (map != null) {
                            map.startMapEffect("[周末双倍经验]: 周末双倍经验已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪升级吧。", 5121006);
                        }
                    }
                }

                oldExpRateWeek = WorldConstants.EXP_RATE;
                WorldConstants.EXP_RATE = oldExpRateWeek * 2.0F;
                ChannelServer.reloadExpRate();
                周末双倍经验开关 = true;
            }

            if ((week == 6 || week == 7) && 时 == stopTime && 分 >= 0 && 分 <= 5 && 周末双倍经验开关) {
                WorldConstants.EXP_RATE = oldExpRateWeek;
                ChannelServer.reloadExpRate();
                周末双倍经验开关 = false;
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍经验]: 今天的双倍经验已结束，经验值调整为正常倍率。");
                    }

                    var8 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var8.hasNext()) {
                        map = (MapleMap)var8.next();
                        if (map != null) {
                            map.startMapEffect("[周末双倍经验]: 今天的双倍经验已结束，经验值调整为正常倍率。", 5121006);
                        }
                    }
                }
            }

            if (分 % 30 == 0) {
                服务端输出信息.println_out("【周末双倍经验】 检测周末双倍经验:::");
                if (周末双倍经验开关) {
                    服务端输出信息.println_out("【周末双倍经验】 双倍经验正在进行中。");
                } else {
                    服务端输出信息.println_out("【周末双倍经验】 不在双倍经验时间段。");
                }
            }

        }
    }

    public static void 周末双爆(int 时, int 分) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(2);
        int week = calendar.get(7);
        --week;
        if (week == 0) {
            week = 7;
        }

        int startTime = (Integer)LtMS.ConfigValuesMap.get("周末双倍起始时间");
        int stopTime = (Integer)LtMS.ConfigValuesMap.get("周末双倍结束时间");
        if ((Integer)LtMS.ConfigValuesMap.get("周末双爆开关") <= 0) {
            if (分 % 30 == 0) {
                服务端输出信息.println_out("【循环线程】 周末双倍爆率为关闭状态:::");
            }

        } else if (week >= 1 && week <= 5 && 时 == 0 && 周末双倍爆率开关) {
            WorldConstants.DROP_RATE = oldDropRateWeek;
            WorldConstants.MESO_RATE = oldMesoRateWeek;
            ChannelServer.reloadDropRate();
            ChannelServer.reloadMesoRate();
            周末双倍爆率开关 = false;
            服务端输出信息.println_out("【循环线程】 检测到周末双倍爆率未正常结束，已进行结束处理:::");
        } else {
            Iterator var6;
            ChannelServer cserv1;
            Iterator var8;
            MapleCharacter mch;
            if ((week == 6 || week == 7) && 时 == startTime - 1 && 分 == 0) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍爆率]: 周末双倍爆率将在60分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if ((week == 6 || week == 7) && 时 == startTime - 1 && 分 == 30) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍爆率]: 周末双倍爆率将在30分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if ((week == 6 || week == 7) && 时 == startTime - 1 && 分 == 55) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍爆率]: 周末双倍爆率将在5分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if ((week == 6 || week == 7) && 时 == startTime && !周末双倍爆率开关) {
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍爆率]: 周末双倍爆率已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪吧。");
                    }

                    var8 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var8.hasNext()) {
                        MapleMap map = (MapleMap)var8.next();
                        if (map != null) {
                            map.startMapEffect("[周末双倍爆率]: 周末双倍爆率已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪吧。", 5121006);
                        }
                    }
                }

                oldMesoRateWeek = WorldConstants.MESO_RATE;
                oldDropRateWeek = WorldConstants.DROP_RATE;
                WorldConstants.MESO_RATE = oldMesoRateWeek + 2.0F;
                WorldConstants.DROP_RATE = oldDropRateWeek + 2.0F;
                ChannelServer.reloadDropRate();
                ChannelServer.reloadMesoRate();
                周末双倍爆率开关 = true;
            }

            if ((week == 6 || week == 7) && 时 == stopTime && 分 >= 0 && 分 <= 5 && 周末双倍爆率开关) {
                WorldConstants.MESO_RATE = oldMesoRateWeek;
                WorldConstants.DROP_RATE = oldDropRateWeek;
                ChannelServer.reloadDropRate();
                ChannelServer.reloadMesoRate();
                周末双倍爆率开关 = false;
                var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    cserv1 = (ChannelServer)var6.next();
                    var8 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        mch = (MapleCharacter)var8.next();
                        mch.dropMessage(6, "[周末双倍爆率]: 今天的双倍爆率已结束，经验值调整为正常倍率。");
                    }
                }
            }

            if (分 % 30 == 0) {
                服务端输出信息.println_out("【周末双倍爆率】 检测周末双倍爆率:::");
                if (周末双倍爆率开关) {
                    服务端输出信息.println_out("【周末双倍爆率】 双倍爆率正在进行中。");
                } else {
                    服务端输出信息.println_out("【周末双倍爆率】 不在双倍爆率时间段。");
                }
            }

        }
    }

    public static void 每日双爆(int 时, int 分) {
        int startTime = (Integer)LtMS.ConfigValuesMap.get("每日双倍起始时间");
        int stopTime = (Integer)LtMS.ConfigValuesMap.get("每日双倍结束时间");
        if ((Integer)LtMS.ConfigValuesMap.get("每日双爆开关") <= 0) {
            if (分 % 30 == 0) {
                服务端输出信息.println_out("【循环线程】 每日双倍爆率为关闭状态:::");
            }

        } else if (时 == 0 && 分 == 0 && 双倍爆率开关) {
            WorldConstants.MESO_RATE = oldMesoRate;
            WorldConstants.DROP_RATE = oldDropRate;
            ChannelServer.reloadDropRate();
            ChannelServer.reloadMesoRate();
            双倍爆率开关 = false;
            服务端输出信息.println_out("【循环线程】 检测到每日双倍爆率未正常结束，已进行结束处理:::");
        } else {
            Iterator var4;
            ChannelServer cserv1;
            Iterator var6;
            MapleCharacter mch;
            if (时 == startTime - 1 && 分 == 1) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍爆率]: 每日双倍爆率将在60分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if (时 == startTime - 1 && 分 == 31) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍爆率]: 每日双倍爆率将在30分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            if (时 == startTime - 1 && 分 == 56) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍爆率]: 每日双倍爆率将在5分钟后开启，持续" + (stopTime - startTime) + "小时。");
                    }
                }
            }

            MapleMap map;
            if (时 == startTime && !双倍爆率开关) {
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍爆率]: 每日双倍爆率已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪吧。");
                    }

                    var6 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var6.hasNext()) {
                        map = (MapleMap)var6.next();
                        if (map != null) {
                            map.startMapEffect("[每日双倍爆率]: 每日双倍爆率已开启，将持续" + (stopTime - startTime) + "小时，大家抓紧时间打怪吧。", 5121006);
                        }
                    }
                }

                oldMesoRate = WorldConstants.MESO_RATE;
                oldDropRate = WorldConstants.DROP_RATE;
                WorldConstants.MESO_RATE = oldMesoRate * 2.0F;
                WorldConstants.DROP_RATE = oldDropRate * 2.0F;
                ChannelServer.reloadDropRate();
                ChannelServer.reloadMesoRate();
                双倍爆率开关 = true;
            }

            if (时 == stopTime && 双倍爆率开关) {
                WorldConstants.MESO_RATE = oldMesoRate;
                WorldConstants.DROP_RATE = oldDropRate;
                ChannelServer.reloadDropRate();
                ChannelServer.reloadMesoRate();
                双倍爆率开关 = false;
                var4 = ChannelServer.getAllInstances().iterator();

                while(var4.hasNext()) {
                    cserv1 = (ChannelServer)var4.next();
                    var6 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var6.hasNext()) {
                        mch = (MapleCharacter)var6.next();
                        mch.dropMessage(6, "[每日双倍爆率]: 今天的双倍爆率已结束，爆率调整为正常倍率。");
                    }

                    var6 = cserv1.getMapFactory().getAllMapThreadSafe().iterator();

                    while(var6.hasNext()) {
                        map = (MapleMap)var6.next();
                        if (map != null) {
                            map.startMapEffect("[每日双倍爆率]: 今天的双倍爆率已结束，爆率调整为正常倍率。", 5121006);
                        }
                    }
                }
            }

            if (分 % 30 == 0) {
                服务端输出信息.println_out("【每日双倍爆率】 检测每日双倍爆率:::");
                if (双倍爆率开关) {
                    服务端输出信息.println_out("【每日双倍爆率】 双倍爆率正在进行中。");
                } else {
                    服务端输出信息.println_out("【每日双倍爆率】 不在双倍爆率时间段。");
                }
            }

        }
    }
    public static void 每日低保(int 时, int 分) {
        if (分 % 30 == 0) {
            服务端输出信息.println_out("【每日低保】 检测每日低保:::");
        }

        int cash1 = (Integer)LtMS.ConfigValuesMap.get("每日低保点券");
        int cash2 = (Integer)LtMS.ConfigValuesMap.get("每日低保抵用");
        int meso = (Integer)LtMS.ConfigValuesMap.get("每日低保金币");
        if ((Integer)LtMS.ConfigValuesMap.get("每日低保开关") <= 0) {
            if (分 % 30 == 0) {
                服务端输出信息.println_out("【循环线程】 每日低保为关闭状态:::");
            }

        } else {
            Iterator var5;
            ChannelServer cserv1;
            Iterator var7;
            MapleCharacter mch;
            if (时 == 19 && 分 == 30) {
                var5 = ChannelServer.getAllInstances().iterator();

                while(var5.hasNext()) {
                    cserv1 = (ChannelServer)var5.next();
                    var7 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var7.hasNext()) {
                        mch = (MapleCharacter)var7.next();
                        mch.dropMessage(6, "[每日低保]: 每日低保将在60分钟后发放，届时请保持在线。");
                    }
                }
            }

            if (时 == 20 && 分 == 0) {
                var5 = ChannelServer.getAllInstances().iterator();

                while(var5.hasNext()) {
                    cserv1 = (ChannelServer)var5.next();
                    var7 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var7.hasNext()) {
                        mch = (MapleCharacter)var7.next();
                        mch.dropMessage(6, "[每日低保]: 每日低保将在30分钟后发放，届时请保持在线。");
                    }
                }
            }

            if (时 == 20 && 分 == 25) {
                var5 = ChannelServer.getAllInstances().iterator();

                while(var5.hasNext()) {
                    cserv1 = (ChannelServer)var5.next();
                    var7 = cserv1.getPlayerStorage().getAllCharacters().iterator();

                    while(var7.hasNext()) {
                        mch = (MapleCharacter)var7.next();
                        mch.dropMessage(6, "[每日低保]: 每日低保将在5分钟后发放，请不要下线哦。");
                    }
                }
            }

            if (时 == 20 && 分 >= 30 && !低保发放开关) {
                服务端输出信息.println_out("【每日低保】 开始发放每日低保。");
                低保发放开关 = true;
                if (cash1 > 0) {
                    发送福利(1, cash1);
                }

                if (cash2 > 0) {
                    发送福利(2, cash2);
                }

                if (meso > 0) {
                    发送福利(3, meso);
                }

                var5 = ChannelServer.getAllInstances().iterator();

                while(var5.hasNext()) {
                    cserv1 = (ChannelServer)var5.next();
                    var7 = cserv1.getPlayerStorage().getAllCharactersThreadSafe().iterator();

                    while(var7.hasNext()) {
                        mch = (MapleCharacter)var7.next();
                        mch.dropMessage(6, "[每日低保]: 每日低保已发放，此次低保共计发放点券 " + cash1 + " 抵用 " + cash2 + " 金币 " + meso + "。");
                    }
                }

                服务端输出信息.println_out("【每日低保】 每日低保已发放。");
            } else {
                if (时 == 21 && 低保发放开关) {
                    低保发放开关 = false;
                    服务端输出信息.println_out("【每日低保】 每日低保记录已重置。");
                }

                if (分 % 30 == 0) {
                    服务端输出信息.println_out("【每日低保】 每日低保未到发放时间。");
                }

            }
        }
    }
    private static void 发送福利(int a, int mount) {
        if (mount < 0) {
            mount = 0;
        } else if (mount > 999999999) {
            mount = 999999999;
        }

        String 类型 = "";
        Iterator var3 = ChannelServer.getAllInstances().iterator();

        while(var3.hasNext()) {
            ChannelServer cserv1 = (ChannelServer)var3.next();

            MapleCharacter mch;
            for(Iterator var5 = cserv1.getPlayerStorage().getAllCharactersThreadSafe().iterator(); var5.hasNext(); mch.startMapEffect("[发送福利]系统发放 " + mount + " " + 类型 + "给在线的所有玩家！", 5120027)) {
                mch = (MapleCharacter)var5.next();
                switch (a) {
                    case 1:
                        类型 = "点券";
                        mch.modifyCSPoints(1, mount, true);
                        break;
                    case 2:
                        类型 = "抵用券";
                        mch.modifyCSPoints(2, mount, true);
                        break;
                    case 3:
                        类型 = "金币";
                        mch.gainMeso(mount, true);
                        break;
                    case 4:
                        类型 = "经验";
                        mch.gainExp(mount, false, false, false);
                        break;
                    case 5:
                        类型 = "人气";
                        mch.addFame(mount);
                        break;
                    case 6:
                        类型 = "豆豆";
                        mch.gainBeans(mount);
                }
            }
        }

    }

    public static void 读取技能范围检测() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT name, val FROM 技能范围检测");
                Throwable var3 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    Throwable var5 = null;

                    try {
                        while(rs.next()) {
                            String name = rs.getString("name");
                            int val = rs.getInt("val");
                            技能范围检测.put(name, val);
                        }
                    } catch (Throwable var53) {
                        var5 = var53;
                        throw var53;
                    } finally {
                        if (rs != null) {
                            if (var5 != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var52) {
                                    var5.addSuppressed(var52);
                                }
                            } else {
                                rs.close();
                            }
                        }

                    }

                    DBConPool.close(ps);
                } catch (Throwable var55) {
                    var3 = var55;
                    throw var55;
                } finally {
                    if (ps != null) {
                        if (var3 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var51) {
                                var3.addSuppressed(var51);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var57) {
                var1 = var57;
                throw var57;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var50) {
                            var1.addSuppressed(var50);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var59) {
            服务端输出信息.println_err("读取吸怪检测错误：" + var59.getMessage());
        }

    }

    public static void 读取技能PVP伤害() {
        try {
            Connection con = DBConPool.getConnection();
            Throwable var1 = null;

            try {
                PreparedStatement ps = con.prepareStatement("SELECT name, val FROM pvpskills");
                Throwable var3 = null;

                try {
                    ResultSet rs = ps.executeQuery();
                    Throwable var5 = null;

                    try {
                        while(rs.next()) {
                            String name = rs.getString("name");
                            int val = rs.getInt("val");
                            PVP技能伤害.put(name, val);
                        }
                    } catch (Throwable var53) {
                        var5 = var53;
                        throw var53;
                    } finally {
                        if (rs != null) {
                            if (var5 != null) {
                                try {
                                    rs.close();
                                } catch (Throwable var52) {
                                    var5.addSuppressed(var52);
                                }
                            } else {
                                rs.close();
                            }
                        }

                    }

                    DBConPool.close(ps);
                } catch (Throwable var55) {
                    var3 = var55;
                    throw var55;
                } finally {
                    if (ps != null) {
                        if (var3 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var51) {
                                var3.addSuppressed(var51);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var57) {
                var1 = var57;
                throw var57;
            } finally {
                if (con != null) {
                    if (var1 != null) {
                        try {
                            con.close();
                        } catch (Throwable var50) {
                            var1.addSuppressed(var50);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var59) {
            服务端输出信息.println_err("读取技能PVP伤害错误：" + var59.getMessage());
        }

    }
    public static void 重置仙人数据() {
        int ID = 0;

        try {
            Connection con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `id` FROM characters  ORDER BY `id` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            Throwable var4 = null;

            try {
                if (rs.next()) {
                    String SN = rs.getString("id");
                    int sns = Integer.parseInt(SN);
                    ++sns;
                    ID = sns;
                }
            } catch (Throwable var15) {
                var4 = var15;
                throw var15;
            } finally {
                if (rs != null) {
                    if (var4 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var14) {
                            var4.addSuppressed(var14);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            DBConPool.close(ps);
        } catch (SQLException var17) {
        }

        for(int i = 0; i <= ID; ++i) {
            if (个人信息设置.get("仙人模式" + i + "") == null) {
                学习仙人模式("仙人模式" + i, 1);
            }

            if (个人信息设置.get("BUFF增益" + i + "") == null) {
                学习仙人模式("BUFF增益" + i, 1);
            }

            if (个人信息设置.get("硬化皮肤" + i + "") == null) {
                学习仙人模式("硬化皮肤" + i, 1);
            }

            if (个人信息设置.get("聪明睿智" + i + "") == null) {
                学习仙人模式("聪明睿智" + i, 1);
            }

            if (个人信息设置.get("物理攻击力" + i + "") == null) {
                学习仙人模式("物理攻击力" + i, 1);
            }

            if (个人信息设置.get("魔法攻击力" + i + "") == null) {
                学习仙人模式("魔法攻击力" + i, 1);
            }

            if (个人信息设置.get("物理狂暴力" + i + "") == null) {
                学习仙人模式("物理狂暴力" + i, 1);
            }

            if (个人信息设置.get("魔法狂暴力" + i + "") == null) {
                学习仙人模式("魔法狂暴力" + i, 1);
            }

            if (个人信息设置.get("物理吸收力" + i + "") == null) {
                学习仙人模式("物理吸收力" + i, 1);
            }

            if (个人信息设置.get("魔法吸收力" + i + "") == null) {
                学习仙人模式("魔法吸收力" + i, 1);
            }
        }

        读取技个人信息设置();
    }

    public static void 学习仙人模式(String a, int b) {
        int ID = 0;

        Connection con;
        Throwable var6;
        try {
            con = DBConPool.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `id` FROM jiezoudashi  ORDER BY `id` DESC LIMIT 1");
            ResultSet rs = ps.executeQuery();
            var6 = null;

            try {
                if (rs.next()) {
                    String SN = rs.getString("id");
                    int sns = Integer.parseInt(SN);
                    ++sns;
                    ID = sns;
                }
            } catch (Throwable var62) {
                var6 = var62;
                throw var62;
            } finally {
                if (rs != null) {
                    if (var6 != null) {
                        try {
                            rs.close();
                        } catch (Throwable var57) {
                            var6.addSuppressed(var57);
                        }
                    } else {
                        rs.close();
                    }
                }

            }

            DBConPool.close(ps);
        } catch (SQLException var64) {
        }

        try {
            con = DBConPool.getConnection();
            Throwable var65 = null;

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO jiezoudashi ( id,name,Val ) VALUES ( ? ,?,?)");
                var6 = null;

                try {
                    ps.setInt(1, ID);
                    ps.setString(2, a);
                    ps.setInt(3, b);
                    ps.executeUpdate();
                    DBConPool.close(ps);
                    //服务端输出信息.println_err("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 数据补充 " + a);
                } catch (Throwable var56) {
                    var6 = var56;
                    throw var56;
                } finally {
                    if (ps != null) {
                        if (var6 != null) {
                            try {
                                ps.close();
                            } catch (Throwable var55) {
                                var6.addSuppressed(var55);
                            }
                        } else {
                            ps.close();
                        }
                    }

                }
            } catch (Throwable var59) {
                var65 = var59;
                throw var59;
            } finally {
                if (con != null) {
                    if (var65 != null) {
                        try {
                            con.close();
                        } catch (Throwable var54) {
                            var65.addSuppressed(var54);
                        }
                    } else {
                        con.close();
                    }
                }

            }
        } catch (SQLException var61) {
        }

    }











}
