package util;


import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

public final class RedisUtil {

  //  private static final Logger log = LogManager.getLogManager().getLogger("RedisUtil");

    //Redis服务器IP
    private static final String ADDR = "127.0.0.1";

    //Redis的端口号
    private static final int PORT = 6379;
    //可用连接实例的最大数目，默认值为8；
    //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private static final int MAX_ACTIVE = -1;
    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static final int MAX_IDLE = 2000;
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static final int MAX_WAIT = 10000;
    private static final int TIMEOUT = 10000;
    //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static final boolean TEST_ON_BORROW = true;
    //访问密码
    private static String AUTH = "admin";
    private static JedisPool jedisPool = null;

    /*
      初始化Redis连接池
     */
    static {
//
        try {
            boolean isInstall = true;
            boolean isRunning = false;
            Process process = Runtime.getRuntime().exec("SC QUERY redserver");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
//                StringBuilder stringBuilder = new StringBuilder();
                String s;
                while ((s = bufferedReader.readLine()) != null) {
                    if (s.contains("1060")) {
                        System.out.println("1060");
                        isInstall = false;
                        break;
                    } else if (s.contains("STATE")) {
                        System.out.println("STATE");
                        isRunning = s.contains("RUNNING");
                        break;
                    }
//                    stringBuilder.append(s).append("\r\n");
                }
            }
           if (!isInstall) {
                System.out.println("isInstall");

                Process process1 = Runtime.getRuntime().exec("cmd /c redis-server.exe --service-install redis.windows-service.conf --loglevel verbose --service-name redserver", null, new File("config\\"));
                process1.waitFor();

            }

            if (!isRunning) {
                System.out.println("isRunning");

                Process process1 = Runtime.getRuntime().exec("cmd /c redis-server.exe --service-start --service-name redserver", null, new File("config\\redis\\"));
                process1.waitFor();
            }

        } catch (IOException | InterruptedException e) {//
            e.printStackTrace();
            System.out.println("Cache初始化失败");
        }

        try {
            System.out.println("CachePool开始初始化");
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CachePool初始化失败");
        }
        // redis-server --service-install redis.windows-service.conf --loglevel verbose
    }

    public static <T> T domain(RedisDomainInterface<T> interfaces) {
        T Object;
        Jedis jedis = getJedis();
        try {
            Object = interfaces.domain(jedis);
        } finally {
            returnResource(jedis);
        }
        return Object;
    }

    /**
     * 获取Jedis实例
     *
     * @return
     */
    public synchronized static Jedis getJedis() {
//        (new Exception()).printStackTrace();
        return jedisPool.getResource();
    }

    /**
     * 释放jedis资源
     *
     * @param jedis
     */
    public static void returnResource(final Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public static void hset(String key, String field, String value) {
        domain(jedis -> jedis.hset(key, field, value));
    }
    public static void hsetTime(String key, String field, String value,long time) {
        domain(jedis -> {
            jedis.hset(key, field, value);
            jedis.expire(field, time);
            return null;
        });
    }

    public static String hget(String key, String field) {
        return domain(jedis -> jedis.hget(key, field));
    }

    public static void hdel(String key, String field) {
        domain(jedis -> jedis.hdel(key, field));
    }

    public static boolean hexists(String key, String field) {
        return domain(jedis -> jedis.hexists(key, field));
    }

    public static Map<String, String> hgetAll(String key) {
        return domain(jedis -> jedis.hgetAll(key));
    }

    public static void del(String key, String field) {
        domain(jedis -> jedis.del(key, field));
    }
    public static void del(String key) {
        domain(jedis -> jedis.del(key));
    }

    public static boolean isMembers(String key, String value) {
        return domain(jedis -> jedis.sismember(key, value));
    }

    public static boolean exists(String key) {
        return domain(jedis -> jedis.exists(key));
    }

    public static void flushall() {
        domain(BinaryJedis::flushAll);
    }

    public static Set<String> smembers(String key) {
        return domain(jedis -> jedis.smembers(key));
    }


    public enum KEYNAMES {
        //套装伤害数据
        SET_BONUS_TABLE("setBonusTable"),
        SET_LT_DIABLO_EQUIPMENTS("setLtDiabloEquipments"),
        SAVE_DATA("saveData"),
        ITEM_INFO("ItemInfo"),
        DOMAIN_AURA("DomainAura"),
        ITEM_DATA("ItemData"),
        PET_FLAG("PetFlag"),
        SETITEM_DATA("SetItemInfo"),
        POTENTIAL_DATA("Potential"),
        SOCKET_DATA("Socket"),
        ITEM_NAME("ItemName"),
        ITEM_DESC("ItemDesc"),
        ITEM_MSG("ItemMsg"),
        HAIR_FACE_ID("HairFaces"),
        SKILL_DATA("SkillData"),
        SKILL_NAME("SkillName"),
        DELAYS("Delays"),
        SUMMON_SKILL_DATA("SummonSkillData"),
        MOUNT_ID("MountIDs"),
        FAMILIAR_DATA("Familiars"),
        CRAFT_DATA("Crafts"),
        SKILL_BY_JOB("SkillsByJob"),
        FINLA_ATTACK_SKILL("FinalAttackSkills"),
        MEMORYSKILL_DATA("MemorySkills"),
        //        DROP_DATA("DropData"),
//        DROP_DATA_GLOBAL("DropDataGlobal"),
//        DROP_DATA_SPECIAL("DropDataSpecial"),
        QUEST_COUNT("QuestCount"),
        NPC_NAME("NpcName"),
        PLAYER_DATA("PlayerData"),
        //        SHOP_DATA("ShopData"),
        MOBSKILL_DATA("MobSkillData"),
        MOB_NAME("MobName"),
        MOB_ID("MobIDs"),
        MAP_NAME("MapName"),
        MAP_LINKNPC("MapLinkNPC");

        public static final boolean DELETECACHE = false;
        private final String key;

        KEYNAMES(String key) {
            this.key = key;
        }

        public String getKeyName() {
            return key;
        }
    }

    public interface RedisDomainInterface<T> {
        T domain(Jedis jedis);
    }
}
