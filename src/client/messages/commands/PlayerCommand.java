package client.messages.commands;

import client.MapleCharacter;
import client.inventory.IItem;
import constants.MapConstants;
import constants.ServerConfig;
import gui.LtMS;
import handling.channel.ChannelServer;
import handling.world.MaplePartyCharacter;
import scripting.EventManager;
import server.ServerProperties;
import server.gashapon.GashaponFactory;
import client.inventory.MapleInventory;
import server.MapleInventoryManipulator;
import client.inventory.MapleInventoryType;
import server.shops.HiredMerchant;
import server.shops.IMaplePlayerShop;
import snail.FakePlayer;
import snail.Potential;
import tools.FileoutputUtil;
import handling.world.World.Broadcast;
import constants.PiPiConfig;
import tools.StringUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;

import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;

import tools.FilePrinter;
import tools.MaplePacketCreator;
import client.MapleStat;
import server.maps.MapleMap;
import server.maps.SavedLocationType;
import constants.GameConstants;
import scripting.NPCScriptManager;
import client.MapleClient;
import constants.ServerConstants.PlayerGMRank;
import tools.packet.PlayerShopPacket;

public class PlayerCommand
{
    
    
    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.普通玩家;
    }
    
    public static class 帮助 extends help
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9330079, "玩家指令查询");
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@帮助 - 帮助").toString();
        }
    }
    
    public static class help extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9330079, "玩家指令查询");
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@help - 帮助").toString();
        }
    }
    
    public abstract static class OpenNPCCommand extends CommandExecute
    {
        protected int npc;
        private static final int[] npcs;
        
        public OpenNPCCommand() {
            this.npc = -1;
        }
        
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            if (this.npc != 1 && c.getPlayer().getMapId() != 910000000) {
                for (final int i : GameConstants.blockedMaps) {
                    if (c.getPlayer().getMapId() == i) {
                        c.getPlayer().dropMessage(1, "你不能在这裡使用指令.");
                        return true;
                    }
                }
                if (this.npc != 2 && c.getPlayer().getLevel() < 10) {
                    c.getPlayer().dropMessage(1, "你的等级必须是10等.");
                    return true;
                }
                if (c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer().getMapId() >= 990000000) {
                    c.getPlayer().dropMessage(1, "你不能在这裡使用指令.");
                    return true;
                }
                if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502) || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000) || c.getPlayer().getMapId() / 100 == 1030008 || c.getPlayer().getMapId() / 100 == 922010 || c.getPlayer().getMapId() / 10 == 13003000) {
                    c.getPlayer().dropMessage(1, "你不能在这裡使用指令.");
                    return true;
                }
            }
            NPCScriptManager.getInstance().start(c, OpenNPCCommand.npcs[this.npc]);
            return true;
        }
        
        static {
            npcs = new int[] { 9010017, 9000001, 9000058, 9330082, 9209002 };
        }
    }
    
    public static class 丢装 extends DropCash
    {
        @Override
        public String getMessage() {
            return new StringBuilder().append("@丢装 - 呼叫清除现金道具npc").toString();
        }
    }
    
    public static class DropCash extends OpenNPCCommand
    {
        public DropCash() {
            this.npc = 0;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@dropbash - 呼叫清除现金道具npc").toString();
        }
    }
    
    public static class event extends OpenNPCCommand
    {
        public event() {
            this.npc = 1;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@event - 呼叫活动npc").toString();
        }
    }
    
    public static class npc extends 万能
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9900004, "拍卖功能");
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@npc - 呼叫万能npc").toString();
        }
    }
    
    public static class 万能 extends OpenNPCCommand
    {
        public 万能() {
            this.npc = 2;
        }
        
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9900004, "拍卖功能");
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@万能 - 呼叫万能npc").toString();
        }
    }
    
    public static class FM extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            for (final int i : GameConstants.blockedMaps) {
                if (c.getPlayer().getMapId() == i) {
                    c.getPlayer().dropMessage(5, "当前地图无法使用.");
                    return false;
                }
            }
            if (c.getPlayer().getLevel() < 10) {
                c.getPlayer().dropMessage(5, "你的等级不足10级无法使用.");
                return false;
            }
            if (c.getPlayer().hasBlockedInventory(true) || c.getPlayer().getMap().getSquadByMap() != null || c.getPlayer().getEventInstance() != null || c.getPlayer().getMap().getEMByMap() != null || c.getPlayer().getMapId() >= 990000000) {
                c.getPlayer().dropMessage(5, "请稍后再试");
                return false;
            }
            if (c.getPlayer().getMapId() == 180000001) {
                c.getPlayer().dropMessage(5, "该地图无法使用该功能!");
                return false;
            }
            if ((c.getPlayer().getMapId() >= 680000210 && c.getPlayer().getMapId() <= 680000502) || (c.getPlayer().getMapId() / 1000 == 980000 && c.getPlayer().getMapId() != 980000000) || c.getPlayer().getMapId() / 100 == 1030008 || c.getPlayer().getMapId() / 100 == 922010 || c.getPlayer().getMapId() / 10 == 13003000) {
                c.getPlayer().dropMessage(5, "请稍后再试.");
                return false;
            }
            c.getPlayer().saveLocation(SavedLocationType.FREE_MARKET, c.getPlayer().getMap().getReturnMap().getId());
            final MapleMap map = c.getChannelServer().getMapFactory().getMap(910000000);
            c.getPlayer().changeMap(map, map.getPortal(0));
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("FM - 回自由").toString();
        }
    }
    
    public static class expfix extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            c.getPlayer().setExp(0);
            c.getPlayer().updateSingleStat(MapleStat.EXP, c.getPlayer().getExp());
            c.getPlayer().dropMessage(5, "经验修复完成");
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@expfix - 经验归零").toString();
        }
    }
    
    public static class TSmega extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            c.getPlayer().setSmega();
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@TSmega - 开/关闭广播").toString();
        }
    }
    
    public static class Gashponmega extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            c.getPlayer().setGashponmega();
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("@Gashponmega - 开/关闭转蛋广播").toString();
        }
    }
    
    public static class 解卡 extends ea
    {
        @Override
        public String getMessage() {
            return new StringBuilder().append("@解卡 - 解卡").toString();
        }
    }
    
    public static class 查看 extends ea
    {
        @Override
        public String getMessage() {
            return new StringBuilder().append("@查看 - 解卡").toString();
        }
    }

    public static class 爆率 extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            NPCScriptManager.getInstance().start(c, 9010000, "怪物爆率");
            return true;
        }

        @Override
        public String getMessage() {
            return new StringBuilder().append("!查询爆率").toString();
        }
    }
//    public static class 吸物 extends xw
//    {
//        @Override
//        public String getMessage() {
//            return new StringBuilder().append("@查看 - 吸物").toString();
//        }
//    }


    public static class 复活 extends CommandExecute {
        public 复活() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getMapId() != 100000203) {
                if ((Integer)LtMS.ConfigValuesMap.get("免费复活开关") > 0) {
                    c.getPlayer().setHp(c.getPlayer().getStat().getMaxHp());
                    c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getMaxHp());
                    c.getPlayer().giveBuff(2438000, (short)30000, (short)0, (short)0, (short)0, (short)30000, (short)30000, (short)0, (short)30000, (short)60, (short)60, 30000, true);
                    c.getPlayer().dropMessage(5, "复活成功");
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(5, "[管理员信息]:指令信息 - [玩家:" + c.getPlayer().getName() + "]使用复活功能"));
                } else {
                    if (c.getPlayer().getBossLog("今日复活次数") >= (Integer)LtMS.ConfigValuesMap.get("最大复活次数")) {
                        c.getPlayer().dropMessage(5, "你今天已经复活了" + c.getPlayer().getBossLog("今日复活次数") + "次，达到了最大复活次数，无法继续原地复活。");
                        return true;
                    }

                    int need = (Integer)LtMS.ConfigValuesMap.get("复活消耗点券");
                    if (c.getPlayer().haveItem(5510000, 1)) {
                        c.getPlayer().setBossLog("今日复活次数");
                        c.getPlayer().gainItem(5510000, -1);
                        c.getPlayer().setHp(c.getPlayer().getStat().getMaxHp());
                        c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getMaxHp());
                        c.getPlayer().giveBuff(2438000, (short)30000, (short)0, (short)0, (short)0, (short)30000, (short)30000, (short)0, (short)30000, (short)60, (short)60, 30000, true);
                        c.getPlayer().dropMessage(5, "复活成功，检测到你身上持有原地复活术，自动消耗 1 件。");
                        Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(5, "[管理员信息]:指令信息 - [玩家:" + c.getPlayer().getName() + "]使用复活功能"));
                    } else if (c.getPlayer().getCSPoints(2) >= need) {
                        c.getPlayer().setBossLog("今日复活次数");
                        c.getPlayer().modifyCSPoints(2, -need, true);
                        c.getPlayer().setHp(c.getPlayer().getStat().getMaxHp());
                        c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getMaxHp());
                        c.getPlayer().giveBuff(2438000, (short)30000, (short)0, (short)0, (short)0, (short)30000, (short)30000, (short)0, (short)30000, (short)60, (short)60, 30000, true);
                        c.getPlayer().dropMessage(5, "复活成功，消耗 " + need + " 抵用券。");
                        Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(5, "[管理员信息]:指令信息 - [玩家:" + c.getPlayer().getName() + "]使用复活功能"));
                    } else if (c.getPlayer().getCSPoints(1) >= need) {
                        c.getPlayer().setBossLog("今日复活次数");
                        c.getPlayer().modifyCSPoints(1, -need, true);
                        c.getPlayer().setHp(c.getPlayer().getStat().getMaxHp());
                        c.getPlayer().updateSingleStat(MapleStat.HP, c.getPlayer().getStat().getMaxHp());
                        c.getPlayer().giveBuff(2438000, (short)30000, (short)0, (short)0, (short)0, (short)30000, (short)30000, (short)0, (short)30000, (short)60, (short)60, 30000, true);
                        c.getPlayer().dropMessage(5, "复活成功，消耗 " + need + " 点券。");
                        Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(5, "[管理员信息]:指令信息 - [玩家:" + c.getPlayer().getName() + "]使用复活功能"));
                    } else {
                        c.getPlayer().dropMessage(5, "你身上没有原地复活术，也没有足够的点券/抵用券，无法复活。");
                    }
                    if ((Integer)LtMS.ConfigValuesMap.get("潜能系统开关") > 0 ) {
                        c.getPlayer().getStat().recalcLocalStats();
                        c.getPlayer().givePotentialBuff(Potential.buffItemId, Potential.duration, true);
                    }
                }
            } else {
                c.getPlayer().dropMessage(5, "该地图无法使用复活。");
            }

            return true;
        }

        public String getMessage() {
            return "@复活";
        }
    }

    public static class fh extends 复活 {
        public fh() {
        }

        public String getMessage() {
            return "@复活";
        }
    }
    
    public static class ea extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            c.removeClickedNPC();
            NPCScriptManager.getInstance().dispose(c);
            c.sendPacket(MaplePacketCreator.enableActions());
            double DROP_RATE = 1.0F;
        if (LtMS.ConfigValuesMap.get("双爆频道开关") == 1 && ServerConfig.双倍线路.contains(c.getPlayer().getMap().getChannel())) {
            DROP_RATE = c.getChannelServer().getDropRate()*2.0F;
        }else{
            DROP_RATE = c.getChannelServer().getDropRate();
        }
            //double lastDrop = (c.getPlayer().getStat().realDropBuff - 100.0 <= 0.0) ? 100.0 : (c.getPlayer().getStat().realDropBuff - 100.0);
            DecimalFormat df = new DecimalFormat("#.00");
            String formatExp = df.format(c.getPlayer().getEXPMod()  * c.getChannelServer().getExpRate() * (c.getPlayer().getItemExpm()/100) * Math.round(c.getPlayer().getStat().expBuff / 100.0) *(c.getPlayer().getFairyExp()/100 +1)  );
            String formatDrop =""+( c.getPlayer().getDropMod() * (c.getPlayer().getStat().dropBuff / 100.0) * DROP_RATE + (int)(c.getPlayer().getItemDropm()/100) + (c.getPlayer().getDropm() > 1 ? c.getPlayer().getDropm() -1 : 0) );//
            String speciesDrop = df.format((c.getPlayer().getStat().mesoBuff / 100.0)  * c.getChannelServer().getMesoRate());

            c.sendPacket(MaplePacketCreator.sendHint(
                    "解卡完毕..\r\n"
                            + "当前系统时间" + FilePrinter.getLocalDateString() + " 星期" + getDayOfWeek() + "\r\n"
                            + "经验值倍率 " + formatExp
                            + "倍, 怪物爆率 " + formatDrop
                            + "倍, 金币倍率 " + speciesDrop /*+ "% VIP经验加成：" + c.getPlayer().getVipExpRate()*/ + "倍\r\n"
                            + "当前剩余 " + c.getPlayer().getCSPoints(1) + " 点券 " + c.getPlayer().getCSPoints(2) + " 抵用券\r\n"
                            + "当前延迟 " + c.getPlayer().getClient().getLatency() + " 毫秒\r\n"
                            + "角色坐标 " + "X:"+c.getPlayer().getPosition().x+"-Y:"+c.getPlayer().getPosition().y
                            + "\n\n+系统爆率加成:"+(c.getPlayer().getDropm() > 1 ? c.getPlayer().getDropm() -1 : "0")+"*爆率卡加成:"+c.getPlayer().getDropMod()+"*爆率buff加成:"+(c.getPlayer().getStat().dropBuff / 100.0)+"*频道爆率加:"+DROP_RATE+"+物品爆率加成:"+new BigDecimal((c.getPlayer().getItemDropm()/100)).setScale(2, RoundingMode.HALF_UP)
                    + "", 350, 5));
            return true;
        }
        @Override
        public String getMessage() {
            return new StringBuilder().append("@ea - 解卡").toString();
        }
        }

    public static void main(String[] args) {
        System.out.println("ssss"+new BigDecimal(0.01).setScale(2, RoundingMode.HALF_UP)+"wwwwww");
    }
//    public static class xw extends CommandExecute
//    {
//        @Override
//        public boolean execute(final MapleClient c, final String[] splitted) {
//            final boolean ItemVac = c.getPlayer().getItemVac();
//            if (!ItemVac) {
//                c.getPlayer().stopItemVac();
//                c.getPlayer().startItemVac();
//            }
//            else {
//                c.getPlayer().stopItemVac();
//            }
//            c.getPlayer().dropMessage(6, "目前自动捡物状态:" + (ItemVac ? "关闭" : "开启"));
//            return true;
//        }
//
//
//        @Override
//        public String getMessage() {
//            return new StringBuilder().append("!ItemVac - 全图吸物开关").toString();
//        }
//    }



        public static String getDayOfWeek() {
            final int dayOfWeek = Calendar.getInstance().get(7) - 1;
            String dd = String.valueOf(dayOfWeek);
            switch (dayOfWeek) {
                case 0: {
                    dd = "日";
                    break;
                }
                case 1: {
                    dd = "一";
                    break;
                }
                case 2: {
                    dd = "二";
                    break;
                }
                case 3: {
                    dd = "三";
                    break;
                }
                case 4: {
                    dd = "四";
                    break;
                }
                case 5: {
                    dd = "五";
                    break;
                }
                case 6: {
                    dd = "六";
                    break;
                }
            }
            return dd;
        }

    
    public static class 怪物 extends mob
    {
        @Override
        public String getMessage() {
            return "@怪物 - 查看怪物状态";
        }
    }
    
    public static class mob extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            MapleMonster monster = null;
            for (final MapleMapObject monstermo : c.getPlayer().getMap().getMapObjectsInRange(c.getPlayer().getPosition(), 999*999, Collections.singletonList(MapleMapObjectType.MONSTER))) {
                monster = (MapleMonster)monstermo;
                if (monster.isAlive()) {
                    c.getPlayer().dropMessage(6, "怪物 " + monster.toString());
                }
            }
            if (monster == null) {
                c.getPlayer().dropMessage(6, "找不到地图上的怪物");
            }
            return true;
        }
        
        @Override
        public String getMessage() {
            return "@mob - 查看怪物状态";
        }
    }

    public static class 在线人数 extends CommandExecute {

        @Override
        public boolean execute(MapleClient c, String[] splitted) {
            int total = 0;
            int curConnected = c.getChannelServer().getConnectedClients();
            c.getPlayer().dropMessage(6, "-------------------------------------------------------------------------------------");
            c.getPlayer().dropMessage(6, "频道: " + c.getChannelServer().getChannel() + " 线上人数: " + curConnected);
            total += curConnected;
            for (MapleCharacter chr : c.getChannelServer().getPlayerStorage().getAllCharacters()) {
                if (chr != null && c.getPlayer().getGMLevel() >= chr.getGMLevel()) {
                    StringBuilder ret = new StringBuilder();
                    ret.append(" 角色名称 ");
                    ret.append(StringUtil.getRightPaddedStr(chr.getName(), ' ', 13));
                    ret.append(" ID: ");
                    ret.append(StringUtil.getRightPaddedStr(chr.getId() + "", ' ', 5));
                    ret.append(" 等级: ");
                    ret.append(StringUtil.getRightPaddedStr(String.valueOf(chr.getLevel()), ' ', 3));
                    ret.append(" 职业: ");
                    ret.append(StringUtil.getRightPaddedStr(String.valueOf(chr.getJob()), ' ', 4));
                    if (chr.getMap() != null) {
                        ret.append(" 地图: ");
                        ret.append(chr.getMapId()).append("(").append(chr.getMap().getMapName()).append(")");
                        c.getPlayer().dropMessage(6, ret.toString());
                    }
                }
            }
            c.getPlayer().dropMessage(6, "当前频道总计线上人数: " + total);
            c.getPlayer().dropMessage(6, "-------------------------------------------------------------------------------------");
            int channelOnline = c.getChannelServer().getConnectedClients();
            int totalOnline = 0;
            /*服务器总人数*/
            for (ChannelServer cserv : ChannelServer.getAllInstances()) {
                totalOnline += cserv.getConnectedClients();
            }
            c.getPlayer().dropMessage(6, "当前服务器总计线上人数: " + totalOnline + "个");
            c.getPlayer().dropMessage(6, "-------------------------------------------------------------------------------------");

            return true;
        }

        @Override
        public String getMessage() {
            return "@在线人数 - 查看线上人数";
        }
    }

    public static class CGM extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            boolean autoReply = false;
            if (splitted.length < 2) {
                return false;
            }
            if(1==1){
                return false;
            }
            final String talk = StringUtil.joinStringFrom(splitted, 1);
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage(6, "因为你自己是GM所以无法使用此指令,可以尝试!cngm <讯息> 来建立GM聊天频道~");
            }
            else if (!c.getPlayer().getCheatTracker().GMSpam(100000, 1)) {
                boolean fake = false;
                boolean showmsg = true;
                if (PiPiConfig.getBlackList().containsKey((Object) c.getAccID())) {
                    fake = true;
                }
                if (talk.contains((CharSequence)"抢") && talk.contains((CharSequence)"图")) {
                    c.getPlayer().dropMessage(1, "抢图自行解决！！");
                    fake = true;
                    showmsg = false;
                }
                else if ((talk.contains((CharSequence)"被") && talk.contains((CharSequence)"骗")) || (talk.contains((CharSequence)"点") && talk.contains((CharSequence)"骗"))) {
                    c.getPlayer().dropMessage(1, "被骗请自行解决");
                    fake = true;
                    showmsg = false;
                }
                else if (talk.contains((CharSequence)"删") && (talk.contains((CharSequence)"角") || talk.contains((CharSequence)"脚")) && talk.contains((CharSequence)"错")) {
                    c.getPlayer().dropMessage(1, "删错角色请自行解决");
                    fake = true;
                    showmsg = false;
                }
                else if (talk.contains((CharSequence)"乱") && talk.contains((CharSequence)"名") && talk.contains((CharSequence)"声")) {
                    c.getPlayer().dropMessage(1, "请自行解决");
                    fake = true;
                    showmsg = false;
                }
                if (talk.toUpperCase().contains((CharSequence)"VIP") && (talk.contains((CharSequence)"领") || talk.contains((CharSequence)"获")) && talk.contains((CharSequence)"取")) {
                    c.getPlayer().dropMessage(1, "VIP将会于储值后一段时间后自行发放，请耐心等待");
                    autoReply = true;
                }
                else if (talk.contains((CharSequence)"贡献") || talk.contains((CharSequence)"666") || ((talk.contains((CharSequence)"取") || talk.contains((CharSequence)"拿") || talk.contains((CharSequence)"发") || talk.contains((CharSequence)"领")) && (talk.contains((CharSequence)"勳") || talk.contains((CharSequence)"徽") || talk.contains((CharSequence)"勋")) && talk.contains((CharSequence)"章"))) {
                    c.getPlayer().dropMessage(1, "勳章请去点拍卖NPC案领取勳章\r\n如尚未被加入清单请耐心等候GM。");
                    autoReply = true;
                }
                else if ((talk.contains((CharSequence)"商人") && talk.contains((CharSequence)"吃")) || (talk.contains((CharSequence)"商店") && talk.contains((CharSequence)"补偿"))) {
                    c.getPlayer().dropMessage(1, "目前精灵商人装备和枫币有机率被吃\r\n如被吃了请务必将当时的情况完整描述给管理员\r\n\r\nPS: 不会补偿任何物品");
                    autoReply = true;
                }
                else if (talk.contains((CharSequence)"档") && talk.contains((CharSequence)"案") && talk.contains((CharSequence)"受") && talk.contains((CharSequence)"损")) {
                    c.getPlayer().dropMessage(1, "档案受损请重新解压缩主程式唷");
                    autoReply = true;
                }
                else if ((talk.contains((CharSequence)"缺") || talk.contains((CharSequence)"少")) && ((talk.contains((CharSequence)"技") && talk.contains((CharSequence)"能") && talk.contains((CharSequence)"点")) || talk.toUpperCase().contains((CharSequence)"SP"))) {
                    c.getPlayer().dropMessage(1, "缺少技能点请重练，没有其他方法了唷");
                    autoReply = true;
                }
                if (showmsg) {
                    c.getPlayer().dropMessage(6, "讯息已经寄送给GM了!");
                }
                if (!fake) {
                    Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(6, "[管理员帮帮忙]频道 " + c.getPlayer().getClient().getChannel() + " 玩家 [" + c.getPlayer().getName() + "] (" + c.getPlayer().getId() + "): " + talk + (autoReply ? " -- (系统已自动回复)" : "")));
                }
                FileoutputUtil.logToFile("logs/data/管理员帮帮忙.txt", "\r\n " + FileoutputUtil.NowTime() + " 玩家[" + c.getPlayer().getName() + "] 帐号[" + c.getAccountName() + "]: " + talk + (autoReply ? " -- (系统已自动回复)" : "") + "\r\n");
            }
            else {
                c.getPlayer().dropMessage(6, "为了防止对GM刷屏所以每1分钟只能发一次.");
            }
            return true;
        }
        
        @Override
        public String getMessage() {
            return "@cgm - 跟GM回报";
        }
    }
    
    public static class 清除道具 extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            if (splitted.length < 4) {
                return false;
            }
            String Column = "null";
            int start = -1;
            int end = -1;
            try {
                Column = splitted[1];
                start = Integer.parseInt(splitted[2]);
                end = Integer.parseInt(splitted[3]);
            }
            catch (Exception ex) {}
            if (start == -1 || end == -1) {
                c.getPlayer().dropMessage("@清除道具 <装备栏/消耗栏/装饰栏/其他栏/特殊栏> <开始格数> <结束格数>");
                return true;
            }
            if (start < 1) {
                start = 1;
            }
            if (end > 96) {
                end = 96;
            }
            final String s = Column;
            int n = -1;
            switch (s.hashCode()) {
                case 34380653: {
                    if (s.equals((Object)"装备栏")) {
                        n = 0;
                        break;
                    }
                    break;
                }
                case 27989600: {
                    if (s.equals((Object)"消耗栏")) {
                        n = 1;
                        break;
                    }
                    break;
                }
                case 34891812: {
                    if (s.equals((Object)"装饰栏")) {
                        n = 2;
                        break;
                    }
                    break;
                }
                case 20692975: {
                    if (s.equals((Object)"其他栏")) {
                        n = 3;
                        break;
                    }
                    break;
                }
                case 29042174: {
                    if (s.equals((Object)"特殊栏")) {
                        n = 4;
                        break;
                    }
                    break;
                }
            }
            MapleInventoryType type = null;
            switch (n) {
                case 0: {
                    type = MapleInventoryType.EQUIP;
                    break;
                }
                case 1: {
                    type = MapleInventoryType.USE;
                    break;
                }
                case 2: {
                    type = MapleInventoryType.SETUP;
                    break;
                }
                case 3: {
                    type = MapleInventoryType.ETC;
                    break;
                }
                case 4: {
                    type = MapleInventoryType.CASH;
                    break;
                }
                default: {
                    type = null;
                    break;
                }
            }
            if (type == null) {
                c.getPlayer().dropMessage("@清除道具 <装备栏/消耗栏/装饰栏/其他栏/特殊栏> <开始格数> <结束格数>");
                return true;
            }
            final MapleInventory inv = c.getPlayer().getInventory(type);
            for (int i = start; i <= end; ++i) {
                if (inv.getItem((short)i) != null) {
                    MapleInventoryManipulator.removeFromSlot(c, type, (short)i, inv.getItem((short)i).getQuantity(), true);
                }
            }
            FileoutputUtil.logToFile("logs/Data/玩家指令.txt", "\r\n " + FileoutputUtil.NowTime() + " IP: " + c.getSession().remoteAddress().toString().split(":")[0] + " 帐号: " + c.getAccountName() + " 玩家: " + c.getPlayer().getName() + " 使用了指令 " + StringUtil.joinStringFrom(splitted, 0));
            c.getPlayer().dropMessage(6, "您已经清除了第 " + start + " 格到 " + end + "格的" + Column + "道具");
            return true;
        }
        
        @Override
        public String getMessage() {
            return "@清除道具 <装备栏/消耗栏/装饰栏/其他栏/特殊栏> <开始格数> <结束格数>";
        }
    }
    
    public static class jk_hm extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            c.getPlayer().RemoveHired();
            c.getPlayer().dropMessage("卡精灵商人已经解除");
            return true;
        }
        
        @Override
        public String getMessage() {
            return "@jk_hm - 卡精灵商人解除";
        }
    }
    
    public static class jcds extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            int gain = c.getPlayer().getMP();
            if (gain <= 0) {
                c.getPlayer().dropMessage("目前没有任何在线点数唷。");
                return true;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage("目前枫叶点数: " + c.getPlayer().getCSPoints(2));
                c.getPlayer().dropMessage("目前在线点数已经累积: " + gain + " 点，若要领取请输入 @jcds true");
            }
            else if ("true".equals((Object)splitted[1])) {
                gain = c.getPlayer().getMP();
                c.getPlayer().modifyCSPoints(2, gain, true);
                c.getPlayer().setMP(0);
                c.getPlayer().saveToDB(false, false);
                c.getPlayer().dropMessage("领取了 " + gain + " 点在线点数, 目前枫叶点数: " + c.getPlayer().getCSPoints(2));
            }
            return true;
        }
        
        @Override
        public String getMessage() {
            return "@jcds - 领取在线点数";
        }
    }
    
    public static class 在线点数 extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            int gain = c.getPlayer().getMP();
            if (gain <= 0) {
                c.getPlayer().dropMessage("目前没有任何在线点数唷。");
                return true;
            }
            if (splitted.length < 2) {
                c.getPlayer().dropMessage("目前枫叶点数: " + c.getPlayer().getCSPoints(2));
                c.getPlayer().dropMessage("目前在线点数已经累积: " + gain + " 点，若要领取请输入 @在线点数 是");
            }
            else if ("是".equals((Object)splitted[1])) {
                gain = c.getPlayer().getMP();
                c.getPlayer().modifyCSPoints(2, gain, true);
                c.getPlayer().setMP(0);
                c.getPlayer().saveToDB(false, false);
                c.getPlayer().dropMessage("领取了 " + gain + " 点在线点数, 目前枫叶点数: " + c.getPlayer().getCSPoints(2));
            }
            return true;
        }
        
        @Override
        public String getMessage() {
            return "@在线点数 - 领取在线点数";
        }
    }
    
//    public static class 出来吧皮卡丘 extends CommandExecute
//    {
//        @Override
//        public boolean execute(final MapleClient c, final String[] splitted) {
//            if (splitted.length < 2) {
//                return false;
//            }
//            final int id = Integer.parseInt(splitted[1]);
//            final int quantity = 1;
//            final int mod = Integer.parseInt(splitted[2]);
//            final String npcname = GashaponFactory.getInstance().getGashaponByNpcId(mod).getName();
//            final IItem item = MapleInventoryManipulator.addbyId_GachaponGM(c, id, (short)quantity);
//            Broadcast.broadcastGashponmega(MaplePacketCreator.getGachaponMega(c.getPlayer().getName(), " : x" + quantity + "恭喜玩家 " + c.getPlayer().getName() + " 在" + npcname + "获得！", item, (byte)1, c.getPlayer().getClient().getChannel()));
//            return true;
//        }
//
//        @Override
//        public String getMessage() {
//            return new StringBuilder().append("你就是傻逼").toString();
//        }
//    }
//
//    public static class 丢弃点装 extends CommandExecute
//    {
//        @Override
//        public boolean execute(final MapleClient c, final String[] splitted) {
//            c.sendPacket(MaplePacketCreator.enableActions());
//            NPCScriptManager.getInstance().start(c, 9010000, "丢弃点装");
//            return true;
//        }
//
//        @Override
//        public String getMessage() {
//            return "@" + this.getClass().getSimpleName().toLowerCase() + "丢弃点装 [点装在装备栏的位置]";
//        }
//    }
    
    public abstract static class DistributeStatCommands extends CommandExecute
    {
        protected MapleStat stat;
        
        public DistributeStatCommands() {
            this.stat = null;
        }
        
        private void setStat(final MapleCharacter player, final int amount) {
            switch (this.stat) {
                case STR: {
                    player.getStat().setStr((short)amount);
                    player.updateSingleStat(MapleStat.STR, (int)player.getStat().getStr());
                    break;
                }
                case DEX: {
                    player.getStat().setDex((short)amount);
                    player.updateSingleStat(MapleStat.DEX, (int)player.getStat().getDex());
                    break;
                }
                case INT: {
                    player.getStat().setInt((short)amount);
                    player.updateSingleStat(MapleStat.INT, (int)player.getStat().getInt());
                    break;
                }
                case LUK: {
                    player.getStat().setLuk((short)amount);
                    player.updateSingleStat(MapleStat.LUK, (int)player.getStat().getLuk());
                    break;
                }
            }
        }
        
        private int getStat(final MapleCharacter player) {
            switch (this.stat) {
                case STR: {
                    return player.getStat().getStr();
                }
                case DEX: {
                    return player.getStat().getDex();
                }
                case INT: {
                    return player.getStat().getInt();
                }
                case LUK: {
                    return player.getStat().getLuk();
                }
                default: {
                    throw new RuntimeException();
                }
            }
        }
        
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            if (splitted.length < 2) {
                c.getPlayer().dropMessage(5, "输入的数字无效.");
                return false;
            }
            int change = 0;
            try {
                change = Integer.parseInt(splitted[1]);
            }
            catch (NumberFormatException nfe) {
                c.getPlayer().dropMessage(5, "输入的数字无效.");
                return false;
            }
            if (change <= 0) {
                c.getPlayer().dropMessage(5, "您必须输入一个大于 0 的数字.");
                return false;
            }
            if (c.getPlayer().getRemainingAp() < change) {
                c.getPlayer().dropMessage(5, "您的能力点不足.");
                return false;
            }
            final int number = this.getStat(c.getPlayer()) + change;
            if (number >= 32767) {
                return false;
            }
            this.setStat(c.getPlayer(), this.getStat(c.getPlayer()) + change);
            c.getPlayer().setRemainingAp((short)(c.getPlayer().getRemainingAp() - change));
            c.getPlayer().updateSingleStat(MapleStat.AVAILABLEAP, (int)c.getPlayer().getRemainingAp());
            c.getPlayer().dropMessage(5, "加点成功您的 " + StringUtil.makeEnumHumanReadable(this.stat.name()) + " 提高了 " + change + " 点.");
            return true;
        }
    }
    public static class 力量 extends DistributeStatCommands
    {
        public 力量() {
            this.stat = MapleStat.STR;
        }
        
        @Override
        public String getMessage() {
            return "@力量 - 力量";
        }
    }
    
    public static class 敏捷 extends DistributeStatCommands
    {
        public 敏捷() {
            this.stat = MapleStat.DEX;
        }
        
        @Override
        public String getMessage() {
            return "@敏捷 - 敏捷";
        }
    }
    
    public static class 智力 extends DistributeStatCommands
    {
        public 智力() {
            this.stat = MapleStat.INT;
        }
        
        @Override
        public String getMessage() {
            return "@智力 - 智力";
        }
    }
    
    public static class 运气 extends DistributeStatCommands
    {
        public 运气() {
            this.stat = MapleStat.LUK;
        }
        
        @Override
        public String getMessage() {
            return "@运气 - 运气";
        }
    }

    public static class 解卡组队 extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            c.getPlayer().setParty(null);
            c.getPlayer().dropMessage(1, "解卡组队成功，赶紧去组队把。");
            return true;
        }

        @Override
        public String getMessage() {
            return "@解卡组队-游戏无法进行组队的时候输入";
        }
    }

    public static class 副本拉人 extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            if (c.getPlayer().getEventInstance() != null) {
                if (c.getChannelServer().getEventSMA().getEventManager(c.getPlayer().getEventInstance().getName()) != null) {
                    final EventManager em = c.getChannelServer().getEventSMA().getEventManager(c.getPlayer().getEventInstance().getName());
                }
                else if (c.getChannelServer().getEventSMB().getEventManager(c.getPlayer().getEventInstance().getName()) != null) {
                    c.getChannelServer().getEventSMB().getEventManager(c.getPlayer().getEventInstance().getName());
                }
                EventManager em;
                if (c.getChannelServer().getEventSMC().getEventManager(c.getPlayer().getEventInstance().getName()) != null) {
                    em = c.getChannelServer().getEventSMC().getEventManager(c.getPlayer().getEventInstance().getName());
                }
                else {
                    if (c.getChannelServer().getEventSM().getEventManager(c.getPlayer().getEventInstance().getName()) == null) {
                        c.getPlayer().dropMessage(1, "你这是什么鬼副本找都没找到");
                        return false;
                    }
                    em = c.getChannelServer().getEventSM().getEventManager(c.getPlayer().getEventInstance().getName());
                }
                final EventManager eim = em;
                for (final MaplePartyCharacter chr : c.getPlayer().getParty().getMembers()) {
                    final MapleCharacter curChar = c.getChannelServer().getPlayerStorage().getCharacterById(chr.getId());
                    if (eim.getProperty("joinid").indexOf("" + curChar.getId()) == -1) {
                        c.getPlayer().dropMessage(1, "偷鸡么,你想拉谁？搞事情啊？把偷鸡的人给我t了!");
                        return false;
                    }
                    final int Map = c.getPlayer().getMapId();
                    if (curChar.getMapId() == Map) {
                        continue;
                    }
                    curChar.dropMessage(1, "你的队友拉你回副本，稍后就会传送至队友身边。");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(3000L);
                                if (curChar != null) {
                                    eim.startInstancea(curChar);
                                }
                            }
                            catch (InterruptedException ex) {}
                        }
                    }.start();
                }
                return true;
            }
            c.getPlayer().dropMessage(1, "我拉你个pp虾,都不在副本拉拉,去厕所把！！");
            return false;
        }

        @Override
        public String getMessage() {
            return "@副本拉人-副本或者boss击杀过程中有人掉线可以拉回";
        }
    }

    public static class 存档 extends CommandExecute {
        public 存档() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            c.getPlayer().saveToDB(false, false, true);
            c.getPlayer().dropMessage(5, "当前时间是 " + FileoutputUtil.CurrentReadable_Time() + " ，角色信息存档成功了");
            Broadcast.broadcastGMMessage(MaplePacketCreator.serverNotice(5, "[管理员信息]:指令信息 - [玩家:" + c.getPlayer().getName() + "]使用个人存档功能"));
            return true;
        }

        public String getMessage() {
            return "@存档     <保存当前数据>";
        }
    }

    public static class cd extends 存档 {
        public cd() {
        }

        public String getMessage() {
            return "@存档     <保存当前数据>";
        }
    }


    public static class 我的雇佣 extends CommandExecute {
        public 我的雇佣() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
//            Iterator var3 = ChannelServer.getAllInstances().iterator();
//
//            label44:
//            while(var3.hasNext()) {
//                ChannelServer cs = (ChannelServer)var3.next();
//                Iterator var5 = cs.getMapFactory().getAllMapThreadSafe().iterator();
//
//                while(true) {
//                    MapleMap map;
//                    do {
//                        if (!var5.hasNext()) {
//                            continue label44;
//                        }
//
//                        map = (MapleMap)var5.next();
//                    } while(!MapConstants.isMarket(map.getId()));
//
//                    Iterator var7 = map.getAllMerchant().iterator();
//
//                    while(var7.hasNext()) {
//                        MapleMapObject obj = (MapleMapObject)var7.next();
//                        if (obj instanceof IMaplePlayerShop) {
//                            IMaplePlayerShop ips = (IMaplePlayerShop)obj;
//                            if (obj instanceof HiredMerchant) {
//                                HiredMerchant merchant1 = (HiredMerchant)ips;
//                                if (merchant1 != null && merchant1.getShopType() == 1 && merchant1.isOwner(c.getPlayer()) && merchant1.isAvailable()) {
//                                    c.getPlayer().dropMessage(1, "你的雇佣商店在 " + cs.getChannel() + " 频道 " + map.getStreetName() + ":" + map.getMapName());
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
            for (ChannelServer cs : ChannelServer.getAllInstances()) {
                for (MapleMap map : cs.getMapFactory().getAllMapThreadSafe()) {
                    if (!MapConstants.isMarket(map.getId())) {
                        continue;
                    }

                    for (MapleMapObject obj : map.getAllMerchant()) {
                        if (obj instanceof HiredMerchant) {
                            HiredMerchant merchant = (HiredMerchant) obj;
                            if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(c.getPlayer()) && merchant.isAvailable()) {
                                c.getPlayer().dropMessage(1, "你的雇佣商店在 " + cs.getChannel() + " 频道 " + map.getStreetName() + ":" + map.getMapName());
                                return true;
                            }
                        }
                    }
                }
            }
            c.getPlayer().dropMessage(1, "没有找到你的雇佣商店。");
            return false;
        }

        public String getMessage() {
            return "@我的雇佣 - 查询自己雇佣商店的位置";
        }
    }

    public static class 关闭雇佣 extends CommandExecute {
        public 关闭雇佣() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
//            IMaplePlayerShop merchant = c.getPlayer().getPlayerShop();
//            if (merchant != null && merchant.getShopType() == 1 && merchant.isOwner(c.getPlayer()) && merchant.isAvailable()) {
//                c.getPlayer().getClient().sendPacket(PlayerShopPacket.shopErrorMessage(21, 0));
//                c.getPlayer().getClient().sendPacket(MaplePacketCreator.serverNotice(1, "请去找富兰德里领取你的装备和金币"));
//                c.getPlayer().getClient().sendPacket(MaplePacketCreator.enableActions());
//                merchant.removeAllVisitors(-1, -1);
//                c.getPlayer().setPlayerShop((IMaplePlayerShop)null);
//                merchant.closeShop(true, true);
//                c.getPlayer().dropMessage(1, "你的雇佣商店已关闭！");
//                return true;
//            } else {
//                Iterator var4 = ChannelServer.getAllInstances().iterator();
//
//                label54:
//                while(var4.hasNext()) {
//                    ChannelServer cs = (ChannelServer)var4.next();
//                    Iterator var6 = cs.getMapFactory().getAllMapThreadSafe().iterator();
//
//                    while(true) {
//                        MapleMap map;
//                        do {
//                            if (!var6.hasNext()) {
//                                continue label54;
//                            }
//
//                            map = (MapleMap)var6.next();
//                        } while(!MapConstants.isMarket(map.getId()));
//
//                        Iterator var8 = map.getAllMerchant().iterator();
//
//                        while(var8.hasNext()) {
//                            MapleMapObject obj = (MapleMapObject)var8.next();
//                            if (obj instanceof IMaplePlayerShop) {
//                                IMaplePlayerShop ips = (IMaplePlayerShop)obj;
//                                if (obj instanceof HiredMerchant) {
//                                    HiredMerchant merchant1 = (HiredMerchant)ips;
//                                    if (merchant1 != null && merchant1.getShopType() == 1 && merchant1.isOwner(c.getPlayer()) && merchant1.isAvailable()) {
//                                        merchant1.removeAllVisitors(-1, -1);
//                                        merchant1.closeShop(true, true);
//                                        c.getPlayer().dropMessage(1, "你的雇佣商店已关闭！");
//                                        return true;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            IMaplePlayerShop merchant = c.getPlayer().getPlayerShop();
//            if (merchant instanceof HiredMerchant) {
//                HiredMerchant hiredMerchant = (HiredMerchant) merchant;
//                if (hiredMerchant != null && hiredMerchant.getShopType() == 1 && hiredMerchant.isOwner(c.getPlayer()) && hiredMerchant.isAvailable()) {
//                    closeHiredMerchant(hiredMerchant, c.getPlayer());
//                    return true;
//                }
//            }
//
//            for (ChannelServer cs : ChannelServer.getAllInstances()) {
//                for (MapleMap map : cs.getMapFactory().getAllMapThreadSafe()) {
//                    if (MapConstants.isMarket(map.getId())) {
//                        for (MapleMapObject obj : map.getAllMerchant()) {
//                            if (obj instanceof HiredMerchant) {
//                                HiredMerchant hiredMerchant = (HiredMerchant) obj;
//                                if (hiredMerchant != null && hiredMerchant.getShopType() == 1 && hiredMerchant.isOwner(c.getPlayer()) && hiredMerchant.isAvailable()) {
//                                    closeHiredMerchant(hiredMerchant, c.getPlayer());
//                                    return true;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//                c.getPlayer().dropMessage(1, "没有找到你的雇佣商店。");
                return false;
        }

        public String getMessage() {
            return "@关闭雇佣 - 关闭自己的雇佣商店";
        }
    }
    private static void closeHiredMerchant(HiredMerchant merchant, MapleCharacter player) {
        merchant.removeAllVisitors(-1, -1);
        merchant.closeShop(true, true);
        player.setPlayerShop(null);
        player.getClient().sendPacket(PlayerShopPacket.shopErrorMessage(21, 0));
        player.getClient().sendPacket(MaplePacketCreator.serverNotice(1, "请去找富兰德里领取你的装备和金币"));
        player.getClient().sendPacket(MaplePacketCreator.enableActions());
        player.dropMessage(1, "你的雇佣商店已关闭！");
    }
    public static class 我的位置 extends CommandExecute {
        public 我的位置() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            c.getPlayer().dropMessage(5, "地图: " + c.getPlayer().getMap().getMapName() + " ");
            if (c.getPlayer().isGM()) {
                c.getPlayer().dropMessage(5, "代码: " + c.getPlayer().getMap().getId() + " ");
            }

            c.getPlayer().dropMessage(5, "坐标: " + String.valueOf(c.getPlayer().getPosition().x) + " , " + c.getPlayer().getPosition().y + "");
            return true;
        }

        public String getMessage() {
            return "@我的位置 <查看地图位置>";
        }
    }

    public static class wdwz extends 我的位置 {
        public wdwz() {
        }

        public String getMessage() {
            return "@我的位置 <查看地图位置>";
        }
    }


    public static class 离线挂机 extends CommandExecute {
        public 离线挂机() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            if (c.getPlayer().getOneTimeLog("离线挂机权限") > 0) {
                return FakePlayer.copyChr(c.getPlayer());
            } else {
                c.getPlayer().dropMessage(1, "您暂未开通离线挂机权限！");
                return false;
            }
        }

        public String getMessage() {
            return "@离线挂机  - 开始离线挂机";
        }
    }


}
