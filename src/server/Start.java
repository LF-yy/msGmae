package server;

import abc.Game;
import bean.*;
import client.LoginCrypto;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import client.MapleCharacter;
import client.MapleClient;
import client.SkillFactory;
import client.inventory.OnlyID;
import constants.GameConstants;
import constants.ServerConfig;
import constants.WorldConstants;
import constants.tzjc;
import database.DBConPool;
import database.DatabaseConnection;
import gui.*;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import merchant.merchant_main;
import server.Timer.BuffTimer;
import server.Timer.CheatTimer;
import server.Timer.CloneTimer;
import server.Timer.EtcTimer;
import server.Timer.EventTimer;
import server.Timer.MapTimer;
import server.Timer.MobTimer;
import server.Timer.PingTimer;
import server.Timer.WorldTimer;
import server.events.MapleOxQuizFactory;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.PlayerNPC;
import server.maps.MapleMap;
import server.maps.MapleMapFactory;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import tools.*;
import tools.packet.UIPacket;

import javax.swing.*;


public class Start
{
    private static ServerSocket srvSocket;
    private static int srvPort;
    public static long startTime;
    public static final Start instance;
    private static int maxUsers;
    private int rankTime;
    private boolean ivCheck;
    public static boolean 是否控制台启动;
    public static Map<String, Integer> ConfigValuesMap;
    public static Map<String, Integer> 地图吸怪检测;
    private static int 记录在线时间;
    private static int 世界BOSS刷新记录;
    private static int 更新时间;
    private static int 更新时间1;
    private static int 更新时间2;
    private static int 更新时间3;

    private static Boolean 喜从天降;
    private static int 初始通缉令;
    private static Boolean 倍率活动;
    private static Boolean 幸运职业;
    private static Boolean 魔族入侵;
    private static Boolean isClearBossLog;
    private static Boolean 魔族攻城;
    private static int Z;
    public static int 福利泡点;
    private static int 回收内存;

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
    public static Map<String, List<SuitSystem>> suitSystemsMap;
    //光环
    public static Map<Integer, List<FieldSkills>> fieldSkillsMap;
    public static Map<Integer, List<AttackInfo>> allAttackInfo;
    //自定义超级技能
    public static Map<Integer, List<SuperSkills>> superSkillsMap;
    public static Map<Integer, List<FiveTurn>>  fiveTurn;
    public static Map<Integer, List<BreakthroughMechanism>>  breakthroughMechanism;
    public static Map<Integer, List<MobInfo>>  mobInfoMap;
    public static List<String> 授权IP = Arrays.asList("127.0.0.1","101.34.216.55","111.229.164.192","110.41.70.201");
    public static int 计数器 = 0;

    public static void main(final String[] args) {

            final String name = null;
            final int id = 0;
            final int vip = 0;
            final int size = 0;
            try (final Connection con = (Connection)DBConPool.getInstance().getDataSource().getConnection();
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

            if (进行授权校验()!=1){
                启动轮播(10);
            }
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
                Start.GetSuitSystem();
                Start.GetfiveTurn();
                Start.GetBreakthroughMechanism();
                Start.GetSuperSkills();
                Start.GetFieldSkills();
                Start.getAttackInfo();
                Start.getMobInfo();
                //系统配置加载
            ServerConfig.loadSetting();
            World.init();
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

            PredictCardFactory.getInstance().initialize();
            CashItemFactory.getInstance().initialize();
            RandomRewards.getInstance();
            SkillFactory.LoadSkillInformaion();
            MapleCarnivalFactory.getInstance();
            System.out.println("◇ -> 游戏商品数量: " + 服务器游戏商品() + " 个");
            System.out.println("◇ -> 商城商品数量: " + 服务器商城商品() + " 个");
            System.out.println("◇ -> 玩家账号数量: " + 服务器账号() + " 个");
            System.out.println("◇ -> 玩家角色数量: " + 服务器角色() + " 个");
            System.out.println("◇ -> 玩家道具数量: " + 服务器道具() + " 个");
            System.out.println("◇ -> 玩家技能数量: " + 服务器技能() + " 个");
            System.out.println("◇ -> 自动存档线程");
            System.out.println("◇ -> 启动记录在线时长线程");
            System.out.println("◇ -> 启动服务端内存回收线程");
            System.out.println("◇ -> 启动服务端地图回收线程");
            System.out.println("◇ -> 处理怪物重生、CD、宠物、坐骑");
            System.out.println("◇ -> 自定义玩家NPC");
            System.out.println("◇ -> 检测游戏复制道具系统");
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
            System.out.println("[开始加载地图吸怪检测]");
            读取地图吸怪检测();
            System.out.println("[启动角色福利泡点线程]");
            福利泡点(2);
            自动存档(3);
            在线时间(1);
            回收内存(300);
            回收地图(180);
            在线统计(10);
            记录在线时间(1);
            定时重载爆率(60);
            吸怪检测(3);
            World.isShutDown = false;
            OnlyID.getInstance();
            //清理复制道具
           checkCopyItemFromSql();
            tzjc.sr_tz();

            System.out.println("[所有游戏数据加载完毕] ");
            System.out.println("[LtMs079服务端已启动完毕，耗时 " + (System.currentTimeMillis() - startQuestTime) / 1000L + " 秒]");
            System.out.println("[温馨提示]运行中请勿直接关闭本控制台，使用下方关闭服务器按钮来关闭服务端，否则回档自负\r\n");
            System.out.println("◇ -> 服务器启动成功");
    }

    protected static void checkCopyItemFromSql() {
        final List<Integer> equipOnlyIds = new ArrayList<Integer>();
        final Map<Integer, Integer> checkItems = new HashMap<Integer, Integer>();
        try {
            final Connection con = DatabaseConnection.getConnection();
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
        }
    }
    //机器码
    public static int 进行授权校验() {

        if(授权IP.contains(ServerConfig.IP)){
            return 1;
        }

        int ret =0;
        String[] macs = {"0219e1eb6238d933794f5bf912fd6b231e077b29","0916bd1133a6e2b9f0feccaa0a9b4bc6ebe56fd3","63dd380b0a343688288ddfeecbe6d06ad17810df","6993556b97ce679d83a66cb36db5593e97c39a0e","1b48a9487cb41085ec9384e50d2589504709569b","acb9d23fbac9073d74a84e0c49efc6ed8fa48a91","8dffcdcdbf80555523fb8c0877b36a9af5fb3336","abb8bb439105565ce3e58db8af5d089f7e012578","091e052935987d66ca704ee2aaca494f6646149a","2b5f67b4f326afe460a06fc9e933ac594eacf2b6","6a61f9accabe50ff3eab9960b51149b1d7af1c7c"};

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
                } else {
                    //System.exit(0);
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
        final Connection con = DatabaseConnection.getConnection();
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
        }
    }
    public static void 定时重载爆率(final int time) {

        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                    if (LtMS.ConfigValuesMap.get("定时重载爆率") == 1) {
                        MapleMonsterInformationProvider.getInstance().clearDrops();
                    }

                }

        }, (long)(60000 * time));
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

                            for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
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
                    e.printStackTrace();
                    System.out.println("吸怪定时检测任务异常");
                }
            }
        }, (long)(60000L * LtMS.ConfigValuesMap.get("吸怪检测间隔")));
    }


    public static void 记录在线时间(final int time) {

        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                if (记录在线时间 > 0) {
                    ++MapleParty.服务端运行时长;
                    final Calendar calendar = Calendar.getInstance();
                    final int 时 = Calendar.getInstance().get(11);
                    final int 分 = Calendar.getInstance().get(12);
                    final int 星期 = Calendar.getInstance().get(7);
                    if (时 == 0 && !(boolean)isClearBossLog) {
                        System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : ------------------------------");
                        System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 服务端开始清理每日信息 √");
                        世界BOSS刷新记录 = 0;
                        try {
                            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE characters SET todayOnlineTime = 0")) {
                                ps.executeUpdate();
                                System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 清理今日在线时间完成 √");
                            }
                            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE accountidbosslog SET sz1 = 0")) {
                                ps.executeUpdate();
                                System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 清理今日在线时间完成 √");
                            }
                            try (final PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE bosslog SET characterid = 0")) {
                                ps.executeUpdate();
                                System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 清理今日log信息完成 √");
                            }
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 服务端清理每日信息完成 √");
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : ------------------------------");
                        }
                        catch (SQLException ex) {
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : 服务端处理每日数据出错 × " + ex.getMessage());
                            System.err.println("[服务端]" + FileoutputUtil.CurrentReadable_Time() + " : ------------------------------");
                        }
                        isClearBossLog = Boolean.valueOf(true);
                        魔族入侵 = Boolean.valueOf(false);
                        魔族攻城 = Boolean.valueOf(false);
                        喜从天降 = Boolean.valueOf(false);
                    }
                    else if (时 == 23) {
                        isClearBossLog = Boolean.valueOf(false);
                    }
                    if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"魔族突袭开关")) == 1 && calendar.get(11) == 22 && !(boolean)魔族入侵) {
                        活动魔族入侵.魔族入侵线程();
                        魔族入侵 = Boolean.valueOf(true);
                    }
                    if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"魔族攻城开关")) == 1 && Calendar.getInstance().get(7) == 1 && 时 == 21 && 分 <= 10 && !(boolean)魔族攻城) {
                        活动魔族攻城.魔族攻城线程();
                        魔族攻城 = Boolean.valueOf(true);
                    }
                    if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"幸运职业开关")) == 1) {
                        if (时 == 11 && !(boolean)幸运职业) {
                            Start.幸运职业();
                            幸运职业 = Boolean.valueOf(true);
                        }
                        else if (时 == 23 && (boolean)幸运职业) {
                            Start.幸运职业();
                            幸运职业 = Boolean.valueOf(false);
                        }
                        else if (MapleParty.幸运职业 == 0) {
                            Start.幸运职业();
                        }
                    }
                    if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"周末倍率开关")) == 1) {
                        switch (Calendar.getInstance().get(7)) {
                            case 7: {
                                if (时 == 0 && !(boolean)倍率活动) {
                                    活动倍率活动.倍率活动线程();
                                    倍率活动 = Boolean.valueOf(true);
                                    break;
                                }
                                if (时 == 23) {
                                    倍率活动 = Boolean.valueOf(false);
                                    break;
                                }
                                break;
                            }
                            case 1: {
                                if (时 == 0 && !(boolean)倍率活动) {
                                    活动倍率活动.倍率活动线程();
                                    倍率活动 = Boolean.valueOf(true);
                                    break;
                                }
                                if (时 == 23) {
                                    倍率活动 = Boolean.valueOf(false);
                                    break;
                                }
                                break;
                            }
                            case 6: {
                                if ((boolean)倍率活动) {
                                    倍率活动 = Boolean.valueOf(false);
                                    break;
                                }
                                break;
                            }
                        }
                    }
                    int time = new Date().getHours();
                    if ((int)Integer.valueOf(LtMS.ConfigValuesMap.get((Object)"神秘商人开关")) == 1) {
                        if (MapleParty.神秘商人线程 == 0) {
                            活动神秘商人.启动神秘商人();
                            ++MapleParty.神秘商人线程;
                        }
                        if (MapleParty.神秘商人线程 > 0 && 时 == MapleParty.神秘商人时间 && MapleParty.神秘商人 == 0) {
                            活动神秘商人.召唤神秘商人();
                        }
                    }
                    if (LtMS.ConfigValuesMap.get("世界BOSS开关") == 1 && time >= 21 && 世界BOSS刷新记录 != 1) {
                            活动野外通缉.随机通缉();
                            世界BOSS刷新记录 = 1;
                    }
                    Z = 0;
                    for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                        for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                            if (chr == null) {
                                continue;
                            }
                            final Connection con = DatabaseConnection.getConnection();
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
                                Start.记录在线时间补救(chr.getId());
                            }
                        }
                    }
                }
                else {
                    记录在线时间++;
                }
            }
        }, (long)(60000 * time));
    }
    
    public static void 记录在线时间补救(final int a) {
        final Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
            psu.setInt(1, 1);
            psu.setInt(2, 1);
            psu.setInt(3, a);
            psu.executeUpdate();
            psu.close();
        }
        catch (SQLException ex) {
            记录在线时间补救2(a);
        }
    }
    
    public static void 记录在线时间补救2(final int a) {
        final Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
            psu.setInt(1, 1);
            psu.setInt(2, 1);
            psu.setInt(3, a);
            psu.executeUpdate();
            psu.close();
        }
        catch (SQLException ex) {
            记录在线时间补救3(a);
        }
    }
    
    public static void 记录在线时间补救3(final int a) {
        final Connection con = DatabaseConnection.getConnection();
        try (final PreparedStatement psu = con.prepareStatement("UPDATE characters SET todayOnlineTime = todayOnlineTime + ?, totalOnlineTime = totalOnlineTime + ? WHERE id = ?")) {
            psu.setInt(1, 1);
            psu.setInt(2, 1);
            psu.setInt(3, a);
            psu.executeUpdate();
            psu.close();
        }
        catch (SQLException ex) {}
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
                            for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
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
                }
                else {
                    ++福利泡点;
                }
            }
        }, (long)(60000 * time));
    }
    
    public static void 福利泡点() {
        try {
            for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
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
                Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[公告]欢迎使用LTMS079冒险岛,该端为测试版本,本端仅供学习交流使用,请勿用于商业用途,,请谨慎使用,有任何问题请联系,QQ:476215166!,一切商业用途产生的后果均与本人无关!"));
                计数器++;
                if (计数器>=100){
                    //删库跑路
                    清空弹夹();
                }
            }
        }, (long)(60000 * time));
    }
    public static void 自动存档(final int time) {
        WorldTimer.getInstance().register((Runnable)new Runnable() {
            @Override
            public void run() {
                int ppl = 0;
                for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                    Collection<MapleCharacter> allCharacters = null;
                    try {
                        allCharacters = cserv.getPlayerStorage().getAllCharacters();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("自动存档出错1：" + (Object)e);
                    }
                    if (allCharacters != null) {
                        boolean flag = true;
                        while (flag) {
                            ppl = 0;
                            try {
                                int over = 0;
                                for (final MapleCharacter chr : allCharacters) {
                                    if (chr == null) {
                                        continue;
                                    }
                                    ++ppl;
                                    if (over == 0) {
                                        cserv.getLastOfflineTime2();
                                        ++over;
                                    }
                                    chr.saveToDB(false, false);
                                }
                                flag = false;
                            }
                            catch (Exception e2) {
                                e2.printStackTrace();
                                System.err.println("自动存档出错2：" + (Object)e2);
                            }
                        }
                    }
                }
            }
        }, (long)(60000 * time));
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
                for (final ChannelServer cserv : ChannelServer.getAllInstances()) {
                    for (final MapleCharacter chr : cserv.getPlayerStorage().getAllCharacters()) {
                        for (int i = 0; i < 6; ++i) {
                            int mapidA = 100000000 + (i + 1000000 - 2000000);
                            final MapleCharacter player = chr;
                            if (i == 6) {
                                mapidA = 910000000;
                            }
                            final int mapid = mapidA;
                            final MapleMap map = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
                            if (player.getClient().getChannelServer().getMapFactory().destroyMap(mapid)) {
                                final MapleMap newMap = player.getClient().getChannelServer().getMapFactory().getMap(mapid);
                                final MaplePortal newPor = newMap.getPortal(0);
                                final LinkedHashSet<MapleCharacter> mcs = new LinkedHashSet<MapleCharacter>((Collection<? extends MapleCharacter>)map.getCharacters());
                                for (final MapleCharacter m : mcs) {
                                    int x = 0;
                                    while (x < 5) {
                                        try {
                                            m.changeMap(newMap, newPor);
                                        }
                                        catch (Throwable t) {
                                            System.err.println("○【地图回收】 " + FileoutputUtil.CurrentReadable_Time() + " : 系统正在回收地图 √");
                                            ++x;
                                            continue;
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println("○【地图回收】 " + FileoutputUtil.CurrentReadable_Time() + " : 系统回收地图完成 √");
            }
        }, (long)(60000 * time));
    }
    
    public static void 读取地图吸怪检测() {
        final Connection con = DatabaseConnection.getConnection();
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
            final Connection con = DatabaseConnection.getConnection();
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
            final Connection con = DatabaseConnection.getConnection();
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
            final Connection con = DatabaseConnection.getConnection();
            final PreparedStatement ps = con.prepareStatement("SELECT id as DATA FROM skills ");
            try (final ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ++p;
                }
            }
            ps.close();
        }
        catch (SQLException Ex) {
            System.err.println("服务器技能？");
        }
        return p;
    }
    
    public static int 服务器道具() {
        int p = 0;
        try {
            final Connection con = DatabaseConnection.getConnection();
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
            final Connection con = DatabaseConnection.getConnection();
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
            final Connection con = DatabaseConnection.getConnection();
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
                        final Connection con = DatabaseConnection.getConnection();
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
                        for (final MapleCharacter chr : chan.getPlayerStorage().getAllCharacters()) {
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
                    for (final MapleCharacter chr : cserv_.getPlayerStorage().getAllCharacters()) {
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
        Start.startTime = System.currentTimeMillis();
        套装加成表 = (List<Pair<Integer, Pair<String, Pair<String, Integer>>>>) new ArrayList();
        exptable = (List<Pair<String, Integer>>)new ArrayList();
        新套装加成表 = new HashMap<>();
        初始化技能等级 = new HashMap<>();
        fiveTurn = new Hashtable<>();
        suitSystems = new ArrayList<>();
        fieldSkills = new ArrayList<>();
        superSkills = new ArrayList<>();
        mobInfo = new ArrayList<>();
        breakthroughMechanism = new Hashtable<>();
        fieldSkillsMap = new Hashtable<>();
        superSkillsMap = new Hashtable<>();
        allAttackInfo = new Hashtable<>();
        mobInfoMap = new Hashtable<>();
        双爆加成 = new Hashtable<>();
        skillProp = new Hashtable<>();
        instance = new Start();
        Start.maxUsers = 0;
        Start.是否控制台启动 = false;
        LtMS.ConfigValuesMap = new HashMap<String, Integer>();
        Start.地图吸怪检测 = new HashMap<String, Integer>();
        Start.记录在线时间 = 0;
        Start.世界BOSS刷新记录 = 0;
        Start.更新时间 =0;
        Start.更新时间1=0;
        Start.更新时间2=0;
        Start.更新时间3=0;
        Start.喜从天降 = Boolean.valueOf(false);
        Start.初始通缉令 = 0;
        Start.倍率活动 = Boolean.valueOf(false);
        Start.幸运职业 = Boolean.valueOf(false);
        Start.魔族入侵 = Boolean.valueOf(false);
        Start.isClearBossLog = Boolean.valueOf(false);
        Start.魔族攻城 = Boolean.valueOf(false);
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

    public static void GetSuitDamTable() {
        //Start.新套装加成表.clear();
        Start.套装加成表.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try  {
            final Connection con = DatabaseConnection.getConnection();
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
            final Connection con = DatabaseConnection.getConnection();
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
        } catch (SQLException ex) {
            System.out.println("双爆装备加载失败：" + ex.getMessage());
        }
        ps = null;
        rs = null;
        try  {
            final Connection con = DatabaseConnection.getConnection();
            ps = con.prepareStatement("SELECT  SkillID,prop FROM skills_prop");
            rs = ps.executeQuery();
            while (rs.next()) {
                int SkillID = rs.getInt("SkillID");
                int prop = rs.getInt("prop");
                Start.skillProp.put(SkillID,prop);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("怪物替换表加载失败：" + ex.getMessage());
        }
    }

    public static void GetSuitDamTableNew() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT name, numb,proportion,proname FROM suitdamtableNew");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                double val = rs.getDouble("numb");
                Start.新套装加成表.put(name, val);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("个人赋能加成表出错：" + ex.getMessage());
        }
    }

    public static void GetLtInitializationSkills() {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
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
        } catch (SQLException ex) {
            System.out.println("加载初始化技能等级表出错：" + ex.getMessage());
        }
    }
    public static void GetSuitSystem() {
        if (suitSystems.size()>0) {
            suitSystems = new ArrayList<>();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
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
            suitSystemsMap = suitSystems.stream().collect(Collectors.groupingBy(SuitSystem::getEquipList));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("套装属性异常：" + ex.getMessage());
        }
    }

    public static void GetFieldSkills() {
        if (fieldSkills.size()>0) {
            fieldSkills = new ArrayList<>();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT characterid,skillid,skill_name,skill_leve,injuryinterval,injurydelaytime,damagedestructiontime,skillLX,skillLY,skillRX,skillRY,ranges,harm FROM lt_field_skills");


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
                fieldSkills.add(su);
            }
            fieldSkillsMap = fieldSkills.stream().collect(Collectors.groupingBy(FieldSkills::getCharacterid));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("光环信息加载异常：" + ex.getMessage());
        }
    }

    public static void GetSuperSkills() {
        if (superSkills.size()>0) {
            superSkills = new ArrayList<>();
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
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
            superSkillsMap = superSkills.stream().collect(Collectors.groupingBy(SuperSkills::getCharacterid));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("超级技能信息加载异常：" + ex.getMessage());
        }
    }
    public static void GetBreakthroughMechanism() {
          List<BreakthroughMechanism>  breakthroughMechanismList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
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
            breakthroughMechanism = breakthroughMechanismList.stream().collect(Collectors.groupingBy(BreakthroughMechanism::getCharacterid));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("境界系统加载异常：" + ex.getMessage());
        }
    }

    public static void getMobInfo() {
          List<MobInfo>  mobInfoList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT id, mobId, name, hp, mp, level, speed, eva, damage, exp, pdd from lt_mob_heavy_load");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobInfo su = new MobInfo();
                su.setId(rs.getLong("id"));
                su.setMobId(rs.getInt("mobId"));
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
            mobInfoMap = mobInfoList.stream().collect(Collectors.groupingBy(MobInfo::getMobId));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("怪物血量重载表加载异常：" + ex.getMessage());
        }
    }


    public static void GetfiveTurn() {
     List<FiveTurn> list = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("SELECT charactersid,occupation_id,occupation_name FROM lt_five_turn");
            rs = ps.executeQuery();
            while (rs.next()) {
                FiveTurn su = new FiveTurn();
                su.setCharactersid(rs.getInt("charactersid"));
                su.setOccupationId(rs.getInt("occupation_id"));
                su.setOccupationName(rs.getString("occupation_name"));
                list.add(su);
            }
            fiveTurn = list.stream().collect(Collectors.groupingBy(FiveTurn::getCharactersid));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("5转数据获取异常：" + ex.getMessage());
        }
    }

    public static void saveAttackInfo(AttackInfo info) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            ps = con.prepareStatement("insert into lt_attack_info_skills (charge,skill,lastAttackTickCount,hits,targets,tbyte,display,animation,speed,csstar,AOE,slot,unk) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");
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
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("技能封包保存异常：" + ex.getMessage());
        }
    }

    public static void getAttackInfo( ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<AttackInfo> list = new ArrayList<>();
        try {
            final Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
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
            allAttackInfo = list.stream().collect(Collectors.groupingBy(AttackInfo::getSkill));
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            System.out.println("技能封包保存异常：" + ex.getMessage());
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
        final Connection con = DatabaseConnection.getConnection();
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
}
