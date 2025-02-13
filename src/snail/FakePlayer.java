//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package snail;

import client.MapleCharacter;
import constants.GameConstants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;

import gui.LtMS;
import server.ServerProperties;
import server.Start;
import tools.ArrayMap;
import tools.MaplePacketCreator;

public class FakePlayer {
    private static ArrayList<MapleCharacter> chrList = new ArrayList();
    private static ArrayMap<Integer, ExtraInfo> chrInfoMap = new ArrayMap();
    private static float CSPoint1 = 300.0F;
    private static float CSPoint2 = 400.0F;
    private static float meso = 100000.0F;
    private static float expRate = 100000.0F;
    private static float mileage = 1.0F;
    private static ArrayList<Long> ipWhiteList = new ArrayList();
    private static String ipWhiteListString = "";

    public FakePlayer() {
    }

    public static void clear() {
        chrList.clear();
        chrInfoMap.clear();
    }

    public static boolean copyChr(MapleCharacter chr) {
        if (chr == null) {
            return false;
        } else if (!chr.isGM() && (Integer) LtMS.ConfigValuesMap.get("离线挂机开关") <= 0) {
            chr.dropMessage(1, "管理已关闭离线挂机功能！");
            return false;
        } else {
            MapleCharacter newp = chr.fakeLooks();
            ExtraInfo info = new ExtraInfo(newp.getId());
            info.begainTime = Calendar.getInstance().getTimeInMillis();
            info.sessionIP = chr.getClient().getSessionIPAddress();
            Iterator var3 = chrList.iterator();

            while(var3.hasNext()) {
                MapleCharacter chr0 = (MapleCharacter)var3.next();
                if (chr0 != null && chr0.getAccountID() == chr.getAccountID() && !isWhiteIp(info.sessionIP)) {
                    chr.dropMessage(1, "你账号下的角色 " + chr0.getName() + " 正在离线挂机，请先去取消！");
                    return false;
                }
            }

            if ((Integer)LtMS.ConfigValuesMap.get("离线挂机IP限制开关") > 0) {
                int count = 0;
                Iterator var7 = chrInfoMap.entrySet().iterator();

                while(var7.hasNext()) {
                    Map.Entry<Integer, ExtraInfo> entry = (Map.Entry)var7.next();
                    if (((ExtraInfo)entry.getValue()).getSessionIP().equals(info.sessionIP) && !isWhiteIp(info.sessionIP)) {
                        ++count;
                        if (count >= (Integer)LtMS.ConfigValuesMap.get("离线挂机IP限制人数")) {
                            newp = null;
                            info = null;
                            chr.dropMessage(5, "离线挂机失败，你最多只能离线挂机 " + LtMS.ConfigValuesMap.get("离线挂机IP限制人数") + " 个角色！");
                            return false;
                        }
                    }
                }
            }

            chr.getMap().addPlayer(newp);
            chr.getMap().broadcastMessage(MaplePacketCreator.updateCharLook(newp));
            chr.getMap().movePlayer(newp, chr.getPosition());
            chrList.add(newp);
            chrInfoMap.put(newp.getId(), info);
            chr.dropMessage(1, "离线挂机成功，已将您的客户端断开链接！");
            chr.getClient().disconnect(true, false);
            return true;
        }
    }

    public static boolean rewardChr(MapleCharacter owner) {
        if (owner == null) {
            return false;
        } else {
            int index = 0;
            long duration = 0L;
            for (MapleCharacter chr : chrList) {
                if (chr != null && chr.isFake() && chr.getFakeOwnerId() == owner.getId()) {
                    ExtraInfo info = getExtraInfo(chr.getId());
                    if (info != null) {
                        duration = Calendar.getInstance().getTimeInMillis() - info.getBegainTime();
                        break;
                    }
                }
            }

            if (duration > 0L && chrList.get(index) != null) {
                if (duration > (long)((Integer)LtMS.ConfigValuesMap.get("离线挂机小时上限") * 60 * 1000 * 60)) {
                    duration = (long)((Integer)LtMS.ConfigValuesMap.get("离线挂机小时上限") * 60 * 1000 * 60);
                }

                MapleCharacter chr = (MapleCharacter)chrList.get(index);
                chrInfoMap.remove(chr.getId());
                chr.getMap().removePlayer(chr);
                chr.getClient().disconnect(false, false);
                chrList.remove(index);
                chr = null;
                if ((Integer)LtMS.ConfigValuesMap.get("离线挂机每小时点券") >= 0) {
                    CSPoint1 = (float)(Integer)LtMS.ConfigValuesMap.get("离线挂机每小时点券");
                }

                if ((Integer)LtMS.ConfigValuesMap.get("离线挂机每小时抵用券") >= 0) {
                    CSPoint2 = (float)(Integer)LtMS.ConfigValuesMap.get("离线挂机每小时抵用券");
                }

                if ((Integer)LtMS.ConfigValuesMap.get("离线挂机每小时金币") >= 0) {
                    meso = (float)(Integer)LtMS.ConfigValuesMap.get("离线挂机每小时金币");
                }

                if ((Integer)LtMS.ConfigValuesMap.get("离线挂机每小时经验比率") >= 0) {
                    expRate = (float)(Integer)LtMS.ConfigValuesMap.get("离线挂机每小时经验比率");
                }

                if ((Integer)LtMS.ConfigValuesMap.get("离线挂机每小时里程") >= 0) {
                    mileage = (float)(Integer)LtMS.ConfigValuesMap.get("离线挂机每小时里程");
                }

                int myCSPoint1 = (int)(CSPoint1 * (float)duration / 60.0F / 1000.0F / 60.0F);
                int myCSPoint2 = (int)(CSPoint2 * (float)duration / 60.0F / 1000.0F / 60.0F);
                int myMeso = (int)(meso * (float)duration / 60.0F / 1000.0F / 60.0F);
                int myExp = (int)((float)GameConstants.getExpNeededForLevel(owner.getLevel()) * expRate / 1000000.0F * (float)duration / 60.0F / 1000.0F / 60.0F);
                int myMileage = (int)(mileage * (float)duration / 60.0F / 1000.0F / 60.0F);
                String msg = "您本次离线挂机时长为 #b" + duration / 60L / 1000L + "#k 分钟，共获得了：";
                boolean canReward = false;
                if (myCSPoint1 > 0) {
                    owner.modifyCSPoints(1, myCSPoint1, true);
                    msg = msg + "#r点券x#b" + myCSPoint1 + " #k";
                    canReward = true;
                }

                if (myCSPoint2 > 0) {
                    owner.modifyCSPoints(2, myCSPoint2, true);
                    msg = msg + "#r抵用券x#b" + myCSPoint2 + " #k";
                    canReward = true;
                }

                if (myMeso > 0) {
                    owner.gainMeso(myMeso, true);
                    msg = msg + "#r金币x#b" + myMeso + " #k";
                    canReward = true;
                }

                if (myExp > 0) {
                    owner.gainExp(myExp, true, true, true);
                    msg = msg + "#r经验值x#b" + myExp + " #k";
                    canReward = true;
                }

                if (myMileage > 0) {
                    owner.增加积分_数据库(myMileage);
                    msg = msg + "#r积分x#b" + myMileage + " #k";
                    canReward = true;
                }

                if (!canReward) {
                    msg = "您本次离线挂机时长为 #b" + duration / 60L / 1000L + "#k 分钟，时间太短，没有获得奖励！";
                }

                if ((Integer)LtMS.ConfigValuesMap.get("离线挂机累计在线时长") > 0) {
                    owner.gainTotalOnlineTime((int)(duration / 60L / 1000L));
                    owner.gainTodayOnlineTime((int)(duration / 60L / 1000L));
                }

                owner.dropNPC(9900004, msg);
                return true;
            } else {
                return false;
            }
        }
    }

    public static ExtraInfo getExtraInfo(int chrId) {
        Iterator var1 = chrInfoMap.entrySet().iterator();

        Map.Entry entry;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            entry = (Map.Entry)var1.next();
        } while((Integer)entry.getKey() != chrId);

        return (ExtraInfo)entry.getValue();
    }

    public static void loadIpWhiteList() {
        ipWhiteList.clear();

        try {
            ipWhiteListString = ServerProperties.getProperty("server.settings.ipWhiteList", "");
            String list = ipWhiteListString;
            list = list.replace(" ", "");
            list = list.replace(".", "").replace("/", "");
            String[] var1 = list.split(",");
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                String str = var1[var3];
                ipWhiteList.add(Long.parseLong(str));
            }
        } catch (NumberFormatException e) {

        }

    }

    public static void setIpWhiteList() {
        try {
            ServerProperties.setProperty("server.settings.ipWhiteList", ipWhiteListString);
        } catch (Exception e) {

        }
    }

    public static ArrayList<Long> getIpWhiteList() {
        return ipWhiteList;
    }

    public static void clearIpWhiteList() {
        ipWhiteList.clear();
    }

    public static void addIpWhiteList(String ip) {
        ipWhiteList.add(Long.parseLong(ip.replace(".", "").replace("/", "")));
    }

    public static boolean removeIpWhiteList(String ip) {
        boolean contain = false;

        for(int i = 0; i < ipWhiteList.size(); ++i) {
            if ((Long)ipWhiteList.get(i) == Long.parseLong(ip.replace(".", "").replace("/", ""))) {
                ipWhiteList.remove(i);
                contain = true;
                --i;
            }
        }

        return contain;
    }

    public static boolean isWhiteIp(String ip) {
        return ipWhiteList.contains(Long.parseLong(ip.replace(".", "").replace("/", "")));
    }

    private static class ExtraInfo {
        private int chrId;
        private int CSPoint1;
        private int CSPoint2;
        private int meso;
        private int expRate;
        private int mileage;
        private long begainTime;
        private String sessionIP;

        private ExtraInfo(int chrId) {
            this.begainTime = 0L;
            this.chrId = chrId;
        }

        private void setBegainTime(long time) {
            this.begainTime = time;
        }

        private long getBegainTime() {
            return this.begainTime;
        }

        private String getSessionIP() {
            return this.sessionIP;
        }

        private void setSessionIP(String sessionIP) {
            this.sessionIP = sessionIP;
        }
    }
}
