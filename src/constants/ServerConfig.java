package constants;

import abc.Game;
import server.ServerProperties;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig
{
    public static boolean pvp;
    public static int pvpch;
    public static boolean LOG_MRECHANT;
    public static boolean LOG_CSBUY;
    public static boolean LOG_DAMAGE;
    public static boolean LOG_CHAT;
    public static boolean LOG_MEGA;
    public static boolean LOG_PACKETS;
    public static boolean CHRLOG_PACKETS;
    public static boolean AUTO_REGISTER;
    public static boolean LOCALHOST;
    public static boolean Encoder;
    public static boolean TESPIA;
    public static boolean shieldWardAll;
    public static boolean DISCOUNTED;
    public static boolean 泡点系统;
    public static int 泡点地图;
    private static int userlimit;
    public static int 点卷数量;
    public static int 抵用卷数量;
    public static int 豆豆数量;
    public static int 等级经验倍率;
    public static String SERVERNAME;
    public static short version;
    public static String TOUDING;
    public static String IP;
    public static String wzpath;
    private static String EVENTS;
    public static boolean DEBUG_MODE;
    public static boolean NMGB;
    public static boolean PDCS;
    public static int RSGS;
    public static int maxlevel;
    public static int kocmaxlevel;
    public static int BeiShu1;
    public static int BeiShu1Minlevel = (ServerProperties.getProperty("LtMS.BeiShu1Minlevel", ServerConfig.BeiShu1Minlevel));
    public static int BeiShu1Maxlevel = (ServerProperties.getProperty("LtMS.BeiShu1Maxlevel", ServerConfig.BeiShu1Maxlevel));
    public static int BeiShu2;
    public static int BeiShu2Minlevel = (ServerProperties.getProperty("LtMS.BeiShu2Minlevel", ServerConfig.BeiShu2Minlevel));
    public static int BeiShu2Maxlevel = (ServerProperties.getProperty("LtMS.BeiShu2Maxlevel", ServerConfig.BeiShu2Maxlevel));
    public static int BeiShu3;
    public static int BeiShu3Minlevel = (ServerProperties.getProperty("LtMS.BeiShu3Minlevel", ServerConfig.BeiShu3Minlevel));
    public static int BeiShu3Maxlevel = (ServerProperties.getProperty("LtMS.BeiShu3Maxlevel", ServerConfig.BeiShu3Maxlevel));
    public static String[] ipStr;
    public static byte[] Gateway_IP;
    public static byte[] Gateway_IP2;
    public static int ExpRate;
    public static int MesoRate;
    public static int DropRate;
    public static int CashRate;

    private static Map<Integer, Float> channelExpRateMap = new HashMap();
    private static Map<Integer, Float> channelMesoRateMap = new HashMap();
    private static Map<Integer, Float> channelDropRateMap = new HashMap();
    private static Map<Integer, Integer> channelNeedItemMap = new HashMap();
    private static ArrayList<Integer> cashInventoryBanItemList = new ArrayList();
    public static boolean isPvPChannel(final int ch) {
        return ServerConfig.pvp && ch == ServerConfig.pvpch;
    }
    public static void loadChannelExpRateMap() {
        Map<Integer, Float> ret = new HashMap();
        String list = ServerProperties.getProperty("server.settings.ChannelExpRateMap", "-1");
        if (list.equals("-1")) {
            list = "|1:1.0|2:1.0|3:1.0|4:1.0|5:1.0|6:1.0|7:1.0|8:1.0|9:1.0|10:1.0|\n|11:1.0|12:1.0|13:1.0|14:1.0|15:1.0|16:1.0|17:1.0|18:1.0|19:1.0|20:1.0|";
            ServerProperties.setProperty("server.settings.ChannelExpRateMap", list);
        }

        list = list.replace(" ", "");
        list = list.replace("/", "");
        list = list.replace("\n", "");
        list = list.replace("\r", "");
        String[] var2 = list.split("\\|");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String str = var2[var4];
            if (!str.equals("")) {
                String[] a = str.split(":");
                if (a.length >= 2) {
                    int channel = Integer.parseInt(a[0]);
                    float rate = Float.parseFloat(a[1]);
                    ret.put(channel, rate);
                }
            }
        }

        channelExpRateMap.clear();
        channelExpRateMap = ret;
    }
    public static void loadChannelMesoRateMap() {
        Map<Integer, Float> ret = new HashMap();
        String list = ServerProperties.getProperty("server.settings.ChannelMesoRateMap", "-1");
        if (list.equals("-1")) {
            list = "|1:1.0|2:1.0|3:1.0|4:1.0|5:1.0|6:1.0|7:1.0|8:1.0|9:1.0|10:1.0|\n|11:1.0|12:1.0|13:1.0|14:1.0|15:1.0|16:1.0|17:1.0|18:1.0|19:1.0|20:1.0|";
            ServerProperties.setProperty("server.settings.ChannelMesoRateMap", list);
        }

        list = list.replace(" ", "");
        list = list.replace("/", "");
        list = list.replace("\n", "");
        list = list.replace("\r", "");
        String[] var2 = list.split("\\|");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String str = var2[var4];
            if (!str.equals("")) {
                String[] a = str.split(":");
                if (a.length >= 2) {
                    int channel = Integer.parseInt(a[0]);
                    float rate = Float.parseFloat(a[1]);
                    ret.put(channel, rate);
                }
            }
        }

        channelMesoRateMap.clear();
        channelMesoRateMap = ret;
    }
    public static float getMyChannelExpRate(int channel) {
        if (channelExpRateMap.get(channel) != null) {
            return (Float)channelExpRateMap.get(channel);
        } else {
            channelExpRateMap.put(channel, 1.0F);
            return 1.0F;
        }
    }
    public static float getMyChannelMesoRate(int channel) {
        if (channelMesoRateMap.get(channel) != null) {
            return (Float)channelMesoRateMap.get(channel);
        } else {
            channelMesoRateMap.put(channel, 1.0F);
            return 1.0F;
        }
    }

    public static void loadChannelDropRateMap() {
        Map<Integer, Float> ret = new HashMap();
        String list = ServerProperties.getProperty("server.settings.ChannelDropRateMap", "-1");
        if (list.equals("-1")) {
            list = "|1:1.0|2:1.0|3:1.0|4:1.0|5:1.0|6:1.0|7:1.0|8:1.0|9:1.0|10:1.0|\n|11:1.0|12:1.0|13:1.0|14:1.0|15:1.0|16:1.0|17:1.0|18:1.0|19:1.0|20:1.0|";
            ServerProperties.setProperty("server.settings.ChannelDropRateMap", list);
        }

        list = list.replace(" ", "");
        list = list.replace("/", "");
        list = list.replace("\n", "");
        list = list.replace("\r", "");
        String[] var2 = list.split("\\|");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String str = var2[var4];
            if (!str.equals("")) {
                String[] a = str.split(":");
                if (a.length >= 2) {
                    int channel = Integer.parseInt(a[0]);
                    float rate = Float.parseFloat(a[1]);
                    ret.put(channel, rate);
                }
            }
        }

        channelDropRateMap.clear();
        channelDropRateMap = ret;
    }

    public static float getMyChannelDropRate(int channel) {
        if (channelDropRateMap.get(channel) != null) {
            return (Float)channelDropRateMap.get(channel);
        } else {
            channelDropRateMap.put(channel, 1.0F);
            return 1.0F;
        }
    }

    public static void loadChannelNeedItemMap() {
        Map<Integer, Integer> ret = new HashMap();
        String list = ServerProperties.getProperty("server.settings.ChannelNeedItemMap", "-1");
        if (list.equals("-1")) {
            list = "|11:4110002|12:4110003|";
            ServerProperties.setProperty("server.settings.ChannelNeedItemMap", list);
        }

        list = list.replace(" ", "");
        list = list.replace("/", "");
        list = list.replace("\n", "");
        list = list.replace("\r", "");
        String[] var2 = list.split("\\|");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String str = var2[var4];
            if (!str.equals("")) {
                String[] a = str.split(":");
                if (a.length >= 2) {
                    int channel = Integer.parseInt(a[0]);
                    int itemId = Integer.parseInt(a[1]);
                    ret.put(channel, itemId);
                }
            }
        }

        channelNeedItemMap.clear();
        channelNeedItemMap = ret;
    }

    public static void loadCashInventoryBanItemList() {
        ArrayList<Integer> ret = new ArrayList();
        String list = ServerProperties.getProperty("server.settings.CashInventoryBanItemList", "-1");
        if (list.equals("-1")) {
            list = "|5150043|5150037|5590001|1602008|1602009|1602010|";
            ServerProperties.setProperty("server.settings.CashInventoryBanItemList", list);
        }

        list = list.replace(" ", "");
        list = list.replace("/", "");
        list = list.replace("\n", "");
        list = list.replace("\r", "");
        String[] var2 = list.split("\\|");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String str = var2[var4];
            if (!str.equals("")) {
                int itemId = Integer.parseInt(str);
                if (itemId > 0) {
                    ret.add(itemId);
                }
            }
        }

        cashInventoryBanItemList.clear();
        cashInventoryBanItemList = ret;
    }

    public static boolean isCashInventoryBanItem(int itemId) {
        return cashInventoryBanItemList.contains(itemId);
    }

    public static int getMyChannelNeedItemId(int channel) {
        if (channelNeedItemMap.get(channel) != null) {
            return (Integer)channelNeedItemMap.get(channel);
        } else {
            channelNeedItemMap.put(channel, 0);
            return 0;
        }
    }


    public static String[] getEvents(final boolean reLoad) {
        return getEventList(reLoad).split(",");
    }
    
    public static String getEventList(final boolean reLoad) {
        if (ServerConfig.EVENTS == null || reLoad) {
            final File root = new File("scripts/event");
            final File[] files = root.listFiles();
            ServerConfig.EVENTS = "";
            for (final File file : files) {
                if (!file.isDirectory()) {
                    final String[] fileName = file.getName().split("\\.");
                    if (fileName.length > 1 && "js".equals((Object)fileName[fileName.length - 1])) {
                        for (int i = 0; i < fileName.length - 1; ++i) {
                            ServerConfig.EVENTS += fileName[i];
                        }
                        ServerConfig.EVENTS += ",";
                    }
                }
            }
        }
        return ServerConfig.EVENTS;
    }
    
    public static boolean isAutoRegister() {
        return ServerConfig.AUTO_REGISTER;
    }
    
    public static String getVipMedalName(final int lv) {
        String medal = "";
        if (ServerConfig.SERVERNAME.equals((Object)"冒险岛")) {
            switch (lv) {
                case 1: {
                    medal = " <普通VIP>";
                    break;
                }
                case 2: {
                    medal = " <進階VIP>";
                    break;
                }
                case 3: {
                    medal = " <高級VIP>";
                    break;
                }
                case 4: {
                    medal = " <尊貴VIP>";
                    break;
                }
                case 5: {
                    medal = " <至尊VIP>";
                    break;
                }
                default: {
                    medal = " <VIP" + medal + ">";
                    break;
                }
            }
        }
        else if (ServerConfig.SERVERNAME.equals((Object)"冒险岛")) {
            switch (lv) {
                case 1: {
                    medal = "☆";
                    break;
                }
                case 2: {
                    medal = "☆★";
                    break;
                }
                case 3: {
                    medal = "☆★☆";
                    break;
                }
                case 4: {
                    medal = "☆★☆★";
                    break;
                }
                case 5: {
                    medal = "☆★☆★☆";
                    break;
                }
                case 6: {
                    medal = "☆★☆★☆★";
                    break;
                }
                case 7: {
                    medal = "☆★☆★☆★☆";
                    break;
                }
                case 8: {
                    medal = "☆★☆★☆★☆★";
                    break;
                }
                case 9: {
                    medal = "☆★☆★☆★☆★☆";
                    break;
                }
                case 10: {
                    medal = "☆★☆★☆★☆★☆★";
                    break;
                }
                case 11: {
                    medal = "光之冒险岛第一土豪";
                    break;
                }
                default: {
                    medal = "<VIP" + medal + ">";
                    break;
                }
            }
        }
        return medal;
    }
    
    public static void loadSetting() {
        ServerConfig.LOG_MRECHANT = ServerProperties.getProperty("LtMS.merchantLog", ServerConfig.LOG_MRECHANT);
        ServerConfig.LOG_MEGA = ServerProperties.getProperty("LtMS.megaLog", ServerConfig.LOG_MEGA);
        ServerConfig.LOG_CSBUY = ServerProperties.getProperty("LtMS.csLog", ServerConfig.LOG_CSBUY);
        ServerConfig.LOG_DAMAGE = ServerProperties.getProperty("LtMS.damLog", ServerConfig.LOG_DAMAGE);
        ServerConfig.LOG_CHAT = ServerProperties.getProperty("LtMS.chatLog", ServerConfig.LOG_CHAT);
        ServerConfig.LOG_PACKETS = ServerProperties.getProperty("LtMS.packetLog", ServerConfig.LOG_PACKETS);
        ServerConfig.AUTO_REGISTER = ServerProperties.getProperty("LtMS.autoRegister", ServerConfig.AUTO_REGISTER);
        ServerConfig.SERVERNAME = ServerProperties.getProperty("LtMS.serverName", ServerConfig.SERVERNAME);
        ServerConfig.DEBUG_MODE = ServerProperties.getProperty("LtMS.debug", ServerConfig.DEBUG_MODE);
        ServerConfig.BeiShu1 = ServerProperties.getProperty("LtMS.BeiShu1", ServerConfig.BeiShu1);
        ServerConfig.BeiShu2 = ServerProperties.getProperty("LtMS.BeiShu2", ServerConfig.BeiShu2);
        ServerConfig.BeiShu3 = ServerProperties.getProperty("LtMS.BeiShu3", ServerConfig.BeiShu3);
        ServerConfig.IP =  ServerProperties.getProperty("LtMS.ip.listen", Game.IP地址);
        ServerConfig.setUserlimit(ServerProperties.getProperty("LtMS.userlimit", 2));
       // GameConstants.loadBanChannelList();
        GameConstants.loadBanMultiMobRateList();
        //GameConstants.loadMarketGainPointChannelList();
        GameConstants.loadFishingChannelList();
      //  GameConstants.loadCharacterExpMapFromDB();
        //ServerConfig.IP =  "101.34.216.55";
    }

    public static int getUserlimit() {
        return userlimit;
    }

    public static void setUserlimit(int userlimit) {
        ServerConfig.userlimit = userlimit;
    }

    static {
        ServerConfig.pvp = true;
        ServerConfig.pvpch = 1;
        ServerConfig.LOG_MRECHANT = true;
        ServerConfig.LOG_CSBUY = true;
        ServerConfig.LOG_DAMAGE = false;
        ServerConfig.LOG_CHAT = true;
        ServerConfig.LOG_MEGA = true;
        ServerConfig.LOG_PACKETS = false;
        ServerConfig.CHRLOG_PACKETS = false;
        ServerConfig.AUTO_REGISTER = true;
        ServerConfig.LOCALHOST = false;
        ServerConfig.Encoder = false;
        ServerConfig.TESPIA = false;
        ServerConfig.shieldWardAll = false;
        ServerConfig.DISCOUNTED = false;
        ServerConfig.泡点系统 = false;
        ServerConfig.泡点地图 = 910000000;
        ServerConfig.点卷数量 = 0;
        ServerConfig.抵用卷数量 = 0;
        ServerConfig.豆豆数量 = 0;
        ServerConfig.等级经验倍率 = 0;
        ServerConfig.SERVERNAME = "冒险岛";
        ServerConfig.version = 79;
        ServerConfig.TOUDING = "Ver.079版本";
        ServerConfig.IP = Game.IP地址;
        //ServerConfig.IP = "101.34.216.55";
        ServerConfig.wzpath = "E:\\新建文件夹 (2)\\ms079\\wz";
        ServerConfig.EVENTS = null;
        ServerConfig.DEBUG_MODE = false;
        ServerConfig.NMGB = true;
        ServerConfig.PDCS = false;
        ServerConfig.RSGS = 0;
        ServerConfig.maxlevel = 250;
        ServerConfig.kocmaxlevel = 200;
        ServerConfig.BeiShu1 = 1;
        ServerConfig.BeiShu2 = 1;
        ServerConfig.BeiShu3 = 1;
        ServerConfig.ExpRate = 2;
        ServerConfig.MesoRate = 1;
        ServerConfig.DropRate = 2;
        ServerConfig.CashRate = 1;
        ServerConfig.ipStr = ServerConfig.IP.split("\\.");
        Gateway_IP = new byte[] { (byte)Integer.parseInt(ServerConfig.ipStr[0]), (byte)Integer.parseInt(ServerConfig.ipStr[1]), (byte)Integer.parseInt(ServerConfig.ipStr[2]), (byte)Integer.parseInt(ServerConfig.ipStr[3]) };
        Gateway_IP2 = new byte[] { (byte)Integer.parseInt(ServerConfig.ipStr[0]), (byte)Integer.parseInt(ServerConfig.ipStr[1]), (byte)Integer.parseInt(ServerConfig.ipStr[2]), (byte)Integer.parseInt(ServerConfig.ipStr[3]) };
        loadSetting();
    }
}
