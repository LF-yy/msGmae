package constants;

import gui.CongMS;
import gui.LtMS;
import server.ServerProperties;

public class WorldConstants
{
    public static Option WORLD;
    public static boolean ADMIN_ONLY;
    public static boolean JZSD;
    public static boolean WUYANCHI;
    public static int petLootCoolTime;
    public static boolean LieDetector;
    public static boolean DropItem;
    public static int USER_LIMIT;
    public static boolean importantItemsBroadcast;
    public static int MAX_CHAR_VIEW;
    public static boolean GMITEMS;
    public static boolean CS_ENABLE;
    public static float EXP_RATE;
    public static float MESO_RATE;
    public static float DROP_RATE;
    public static byte FLAG;
    public static int CHANNEL_COUNT;
    public static String WORLD_TIP;
    public static String SCROLL_MESSAGE;
    public static boolean AVAILABLE;
    public static int gmserver = -1;
    public static byte recommended = -1;
    public static String recommendedmsg;
    public static int PET_VAC_RANGE;
    public static Option[] values() {
        return ServerConstants.TESPIA ? TespiaWorldOption.values() : WorldOption.values();
    }
    
    public static Option valueOf(final String name) {
        return ServerConstants.TESPIA ? TespiaWorldOption.valueOf(name) : WorldOption.valueOf(name);
    }
    
    public static Option getById(final int g) {
        for (final Option e : values()) {
            if (e.getWorld() == g) {
                return e;
            }
        }
        return null;
    }
    
    public static boolean isExists(final int id) {
        return getById(id) != null;
    }
    
    public static String getNameById(final int serverid) {
        if (getById(serverid) == null) {
            System.err.println("World doesn't exists exception. ID: " + serverid);
            return "";
        }
        return getById(serverid).name();
    }
    
    public static void loadSetting() {
        ADMIN_ONLY = ServerProperties.getProperty("LtMS.admin", WorldConstants.ADMIN_ONLY);
        FLAG = ServerProperties.getProperty("LtMS.flag", WorldConstants.FLAG);
        EXP_RATE = ServerProperties.getProperty("LtMS.expRate", WorldConstants.EXP_RATE);
        MESO_RATE = ServerProperties.getProperty("LtMS.mesoRate", WorldConstants.MESO_RATE);
        DROP_RATE = ServerProperties.getProperty("LtMS.dropRate", WorldConstants.DROP_RATE);
        WORLD_TIP = ServerProperties.getProperty("LtMS.eventMessage", WorldConstants.WORLD_TIP);
        SCROLL_MESSAGE = ServerProperties.getProperty("LtMS.serverMessage", WorldConstants.SCROLL_MESSAGE);
        CHANNEL_COUNT = ServerProperties.getProperty("LtMS.channel.count", WorldConstants.CHANNEL_COUNT);
        USER_LIMIT = Integer.valueOf(LtMS.ConfigValuesMap.get("服务端最大人数"));
        MAX_CHAR_VIEW = ServerProperties.getProperty("LtMS.maxCharView", WorldConstants.MAX_CHAR_VIEW);
        GMITEMS = ServerProperties.getProperty("LtMS.gmitems", WorldConstants.GMITEMS);
        CS_ENABLE = ServerProperties.getProperty("LtMS.cashshop.enable", WorldConstants.CS_ENABLE);
        PET_VAC_RANGE = ServerProperties.getProperty("server.settings.petVac.range", PET_VAC_RANGE);





    }
    
    static {
        WorldConstants.WORLD = WorldOption.绿水灵;
        WorldConstants.ADMIN_ONLY = true;
        WorldConstants.JZSD = false;
        WorldConstants.WUYANCHI = true;
        WorldConstants.LieDetector = false;
        WorldConstants.DropItem = true;
        WorldConstants.USER_LIMIT = 2;
        WorldConstants.MAX_CHAR_VIEW = 20;
        WorldConstants.GMITEMS = false;
        WorldConstants.CS_ENABLE = true;
        WorldConstants.EXP_RATE = 1;
        WorldConstants.MESO_RATE = 1;
        WorldConstants.DROP_RATE = 1;
        WorldConstants.FLAG = 3;
        PET_VAC_RANGE = 350;
        WorldConstants.CHANNEL_COUNT = 2;
        WorldConstants.WORLD_TIP = "请享受冒险岛的冒险之旅吧!";//請享受冒險島的冒險之旅吧!
        WorldConstants.SCROLL_MESSAGE = "";
        WorldConstants.AVAILABLE = true;
        recommendedmsg = "";
        petLootCoolTime = 5;
        importantItemsBroadcast = false;
        loadSetting();
    }
    
    public enum WorldOption implements Option
    {
        蓝蜗牛(0), 
        蘑菇仔(1), 
        绿水灵(2), 
        漂漂猪(3), 
        小青蛇(4), 
        红螃蟹(5), 
        大海龟(6), 
        章鱼怪(7), 
        顽皮猴(8), 
        星精灵(9), 
        胖企鹅(10), 
        童话村(121);
        
        private final int world;
        
        private WorldOption(final int world) {
            this.world = world;
        }
        
        @Override
        public int getWorld() {
            return this.world;
        }
    }
    
    public enum TespiaWorldOption implements Option
    {
        測試機("t0");
        
        private final int world;
        private final String worldName;
        
        private TespiaWorldOption(final String world) {
            this.world = Integer.parseInt(world.replaceAll("t", ""));
            this.worldName = world;
        }
        
        @Override
        public int getWorld() {
            return this.world;
        }
    }
    
    public interface Option
    {
        int getWorld();
        
        String name();
    }
}
