package constants;

import bean.Leveladdharm;
import bean.LtDiabloEquipments;
import bean.LttItemAdditionalDamage;
import bean.SuitSystem;
import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.IItem;
import database.DBConPool;
import gui.LtMS;

import handling.channel.MapleGuildRanking;
import server.MapleItemInformationProvider;
import server.MapleSquad;
import server.Start;
import server.life.MapleMonster;
import tools.FileoutputUtil;
import tools.Pair;
import util.GetRedisDataUtil;
import util.ListUtil;
import util.RedisUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class tzjc {
    private static List<tz_model> tz_list;
    public static ConcurrentHashMap<Integer, Long> dsMap;
    public static ConcurrentHashMap<String,Double> tzMap;
    public static ConcurrentHashMap<Integer, Map<String, Integer>> sbMap;
    public static ConcurrentHashMap<String, List<SuitSystem>> suitSys;//套装列表
    public long star_damage(MapleCharacter player, long totDamageToOneMonster, MapleMonster monster) {
        try {
            if (player.get套装伤害加成() > 0.0) {
                double jc_damage = totDamageToOneMonster * player.get套装伤害加成();
//                if(jc_damage>100000000L){
//                    player.dropTopMsg("[赋能]:增伤" + Math.ceil(jc_damage/100000000L) + "亿");
//                }else if(jc_damage>10000){
//                    player.dropTopMsg("[赋能]:增伤" + Math.ceil(jc_damage/10000) + "万");
//                }else{
//                    player.dropTopMsg("[赋能]:增伤" + Math.ceil(jc_damage)  + "");
//                }
                totDamageToOneMonster = (long) ((double) totDamageToOneMonster + jc_damage)+player.get最高伤害();
                //player.getMap().broadcastMessage(MobPacket.healMonster(monster.getObjectId(), (int) jc_damage));
            }
        } catch (Exception e) {
            FileoutputUtil.outError("logs/套装伤害异常.txt", e);
            return totDamageToOneMonster;
        }
        return totDamageToOneMonster;
    }
    public long damage(MapleCharacter player, long totDamageToOneMonster, double damage) {
        try {
            totDamageToOneMonster = (long) ((double) totDamageToOneMonster + damage);
            if (player.get套装伤害加成() > 0.0) {
                double jc_damage = totDamageToOneMonster * player.get套装伤害加成();
                totDamageToOneMonster = (long) ((double) totDamageToOneMonster + jc_damage);
            }
            if (player.get最高伤害()>0){
                totDamageToOneMonster += player.get最高伤害();
            }
        } catch (Exception e) {
            FileoutputUtil.outError("logs/套装伤害异常.txt", e);
            return totDamageToOneMonster;
        }
        return totDamageToOneMonster;
    }
    public static long damage2(MapleCharacter player, long totDamageToOneMonster, double damage) {
        try {
            totDamageToOneMonster = (long) ((double) totDamageToOneMonster + damage);
            if (player.get套装伤害加成() > 0.0) {
                double jc_damage = totDamageToOneMonster * player.get套装伤害加成();
                totDamageToOneMonster = (long) ((double) totDamageToOneMonster + jc_damage);
            }
            if (player.get最高伤害()>0){
                totDamageToOneMonster += player.get最高伤害();
            }
        } catch (Exception e) {
            FileoutputUtil.outError("logs/套装伤害异常.txt", e);
            return totDamageToOneMonster;
        }
        return totDamageToOneMonster;
    }
    public static void sr_tz() {
        tzjc.tz_list.clear();
       // tzjc.tz_map.clear();
        System.out.println("[" + FileoutputUtil.CurrentReadable_Time() + "][========================================]");
        System.out.println("[" + FileoutputUtil.CurrentReadable_Time() + "][信息]:初始化赋能装备加成");
        if ( LtMS.ConfigValuesMap.get("赋能属性加成开关") > 0) {
            for (int i = 0; i < Start.套装加成表.size(); ++i) {
                if (((Integer) (Start.套装加成表.get(i)).getLeft()).intValue() == 0) {
                    tz_model tz = new tz_model();
                    tz.setName((String) ((Start.套装加成表.get(i)).getRight()).getLeft());
                    tz.setJc((double) ((Integer) (((Start.套装加成表.get(i)).getRight()).getRight()).getRight()).intValue() / 100.0);
                    int[] list = {Integer.valueOf((String) (((Start.套装加成表.get(i)).getRight()).getRight()).getLeft()).intValue()};
                    tz.setList(list);
                    tzjc.tz_list.add(tz);
                }
            }

            for (int b = 1; b < LtMS.ConfigValuesMap.get("套装个数"); ++b) {
                List<Integer> 套装 = (List<Integer>) new ArrayList();
                int 加成 = 0;
                String 套装名 = "";
                for (int j = 0; j < Start.套装加成表.size(); ++j) {
                    if (((Integer) (Start.套装加成表.get(j)).getLeft()).intValue() == b) {
                        套装.add(Integer.valueOf((String) (((Start.套装加成表.get(j)).getRight()).getRight()).getLeft()));
                        加成 += ((Integer) (((Start.套装加成表.get(j)).getRight()).getRight()).getRight()).intValue();
                        套装名 = (String) ((Start.套装加成表.get(j)).getRight()).getLeft();
                    }
                }
                tz_model tz2 = new tz_model();
                tz2.setName(套装名);
                tz2.setJc((double) 加成 / 100.0);
                int[] list2 = new int[套装.size()];
                for (int k = 0; k < list2.length; ++k) {
                    list2[k] = ((Integer) 套装.get(k)).intValue();
                }
                tz2.setList(list2);
                tzjc.tz_list.add(tz2);
            }

        }
        if (LtMS.ConfigValuesMap.get("个人赋能属性加成开关")> 0) {
            tzMap.putAll(Start.新套装加成表);
        }
        sbMap.putAll(Start.双爆加成);
       // suitSys.putAll(Start.suitSystemsMap);
    }

    public static double check_tz(MapleCharacter chr) {
        Collection<IItem> hasEquipped = chr.getHasEquipped();
        //accurateRankMap  段伤
        //enhancedRankMap  赋能
        //dropRankMap   爆率
        //int id = chr.getClient().getPlayer().getId();
        //段伤  .getChannelServer().removeMapleSquad("blackmage");  MapleSquadType.valueOf("blackmage".toLowerCase())
//        chr.getClient().getPlayer().getClient().getChannelServer().removeMapleSquad(MapleSquad.MapleSquadType.valueOf("blackmage".toLowerCase()));
        //加载护盾
        if (chr.getMapId() == 910000000 && Objects.nonNull(LtMS.ConfigValuesMap.get("护盾开关")) && LtMS.ConfigValuesMap.get("护盾开关") > 0){
            if(Start.ltCharactersHphd.get(chr.getId())!=null && Start.ltCharactersHphd.get(chr.getId()) != chr.getStat().getMaxHphd()){
                chr.getStat().setMaxHphd(Start.ltCharactersHphd.get(chr.getId()));
            }
        }else{
            if (chr.getStat().getMaxHphd()==0 && Objects.nonNull(LtMS.ConfigValuesMap.get("护盾开关")) && LtMS.ConfigValuesMap.get("护盾开关") > 0){
                if(Start.ltCharactersHphd.get(chr.getId())!=null && Start.ltCharactersHphd.get(chr.getId()) != chr.getStat().getMaxHphd()){
                    chr.getStat().setMaxHphd(Start.ltCharactersHphd.get(chr.getId()));
                }
            }
        }
        //装备隐藏属性装载
        LtDiabloEquipments.dataStatisticalAdd(chr);
        AtomicLong additionalDamage = new AtomicLong(0L);
        if ( LtMS.ConfigValuesMap.get("段伤开关") > 0) {
            try {
                    hasEquipped.forEach(iItem -> {
                        Long integer = dsMap.get(iItem.getItemId());
                        if (Objects.nonNull(integer)) {
                                if (integer > 0) {
                                    additionalDamage.updateAndGet(v -> (v + (integer/ 100L)));
                                }
                        }
                    });

            } catch (Exception e) {
                System.out.println("段伤装备计算异常");
            }
        try {
            Start.additionalDamage.forEach((k, v) -> {
                for (LttItemAdditionalDamage lttItemAdditionalDamage : v) {
                    additionalDamage.set(additionalDamage.get()+ (Math.max(chr.getItemQuantity(lttItemAdditionalDamage.getItemId()),0)*k));
                }
            });
        } catch (Exception e) {
            System.out.println("段伤物品计算异常");
        }
        chr.set最高伤害(additionalDamage.get());
        Start.accurateRankMap.put(chr.getId(), new MapleGuildRanking.SponsorRank(chr.getName(), (int)(additionalDamage.get()/100000L),chr.getStr(),chr.getDex(),chr.getInt(),chr.getLuk()));
        }

        AtomicInteger drop = new AtomicInteger(0);
        AtomicInteger exp = new AtomicInteger(0);
        AtomicReference<Double> number = new AtomicReference<>(0.0);
        try {

            hasEquipped.forEach(it->{

                Map<String, Integer> stringIntegerMap = sbMap.get(it.getItemId());
                if (Objects.nonNull(stringIntegerMap) && stringIntegerMap.size()>0){
                    drop.addAndGet(stringIntegerMap.get("drop"));
                    exp.addAndGet(stringIntegerMap.get("exp"));
                    if ( drop.get()>0) {
                        chr.dropMessage(5, "检测到佩戴双爆装备,物品掉落概率加：" + stringIntegerMap.get("drop") + "%");
                    }
                    if (exp.get()>0) {
                        chr.dropMessage(5, "检测到佩戴双爆装备,经验倍率加：" + stringIntegerMap.get("exp") + "%");
                    }
                }
            });
            if (exp.get()>0) {
                chr.setItemExpm(exp.get());
                chr.dropMessage(5, "佩戴双爆装备,经验倍率总计加成：" + exp.get() + "%");
            }
            if (drop.get()>0) {
                chr.setItemDropm(drop.get());
                chr.dropMessage(5, "佩戴双爆装备,物品掉落概率总计加成：" + drop.get() + "%");
            }
            Start.dropRankMap.put(chr.getId(), new MapleGuildRanking.SponsorRank(chr.getName(), drop.intValue(),chr.getStr(),chr.getDex(),chr.getInt(),chr.getLuk()));
        } catch (Exception e) {
            System.out.println("双爆装备装备加载异常");
        }
        try {
            if ( LtMS.ConfigValuesMap.get("赋能属性加成开关") > 0) {
                for (final tz_model tz : tzjc.tz_list) {
                    final int[] list = tz.getList();
                    boolean is_tz = true;
                    for (int j = 0; j < list.length; ++j) {
                        if (!chr.hasEquipped(list[j])) {
                            is_tz = false;
                        }
                    }
                    if (is_tz && tz.getJc() >0) {
                        number.updateAndGet(v -> (double) v + (tz.getJc()));
                        chr.dropMessage(5, "检测到佩戴了 [" + tz.getName() + "] 系统赋能装备,  加成伤害：" + (tz.getJc()*100)  + "%");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("套装属性装备加载异常");
        }
        try {
            if ( LtMS.ConfigValuesMap.get("个人赋能属性加成开关") > 0) {
                List<String> list = new ArrayList<>();
                hasEquipped.forEach(iItem -> {
                    Double integer = tzMap.get("赋能" + iItem.getItemId() + chr.getClient().getPlayer().getName() + "");
                    if (Objects.nonNull(integer)) {
                        if (!list.contains("赋能" + iItem.getItemId() + chr.getClient().getPlayer().getName() + "")) {
                            if (integer > 0) {
                                list.add("赋能" + iItem.getItemId() + chr.getClient().getPlayer().getName() + "");
                                number.updateAndGet(v -> (double) (v + (integer / 100.0)));
                                chr.dropMessage(5, "检测到佩戴个人赋能装备,加成伤害：" + integer + "%");
                            }
                        }
                    }

                });
            }
        } catch (Exception e) {
            System.out.println("个人赋能属性装备加载异常");
        }
        try {
            if(LtMS.ConfigValuesMap.get("装备等级附加") > 0){
                hasEquipped.forEach(iItem -> {
                    int reqLevel = MapleItemInformationProvider.getInstance().getReqLevel(iItem.getItemId());
                    List<Leveladdharm> leveladdharms = Start.leveladdharm.get(reqLevel);
                    if (ListUtil.isNotEmpty(leveladdharms)){
                        Leveladdharm leveladdharm = leveladdharms.get(0);
                        number.updateAndGet(v -> (double)(v + (leveladdharm.getHarm()/100.0)));
                        chr.dropMessage(5, "装备等级增伤：" + leveladdharm.getHarm() + "%");
                    }
    //120级的永恒  增加20%总伤 单件  130级  30%  140级40%  150级50%  160级 60%  200级  150%
                });
            }
        } catch (Exception e) {
            System.out.println("装备等级附加加载异常");
        }
        Start.enhancedRankMap.put(chr.getId(), new MapleGuildRanking.SponsorRank(chr.getName(), number.get().intValue()*100,chr.getStr(),chr.getDex(),chr.getInt(),chr.getLuk()));
        计算最大伤害(chr);
        String damage ;
            if(chr.getStat().damage>100000000L){
                  damage = Math.ceil(chr.getStat().damage/100000000.0) + "亿";
              }else if(chr.getStat().damage>10000){
                  damage = Math.ceil(chr.getStat().damage/10000.0) + "万";
              }else{
                  damage = Math.ceil(chr.getStat().damage)  + "";
              }
        String dsDamage ;
        if(additionalDamage.get()>100000000L){
            dsDamage = Math.ceil(additionalDamage.get()/100000000.0) + "亿";
        }else if(additionalDamage.get()>10000){
            dsDamage = Math.ceil(additionalDamage.get()/10000.0) + "万";
        }else{
            dsDamage = Math.ceil(additionalDamage.get())  + "";
        }
        String str = "角色姓名：" + chr.getClient().getPlayer().getName() + ">>"+
                "力量：" + chr.getStat().localstr + ">>" +
                "敏捷：" + chr.getStat().localdex + ">>" +
                "运气：" + chr.getStat().localluk + ">>" +
                "智力：" + chr.getStat().localint_ + ">>" +
                "物攻：" + chr.getStat().watk + ">>" +
                "魔攻：" + chr.getStat().magic + ">>" +
                "武器熟练度：" + chr.getStat().passive_mastery + ">>" +
                "爆击概率：" + chr.getStat().passive_sharpeye_rate + ">>" +
                "爆击最大伤害倍率：" + chr.getStat().passive_sharpeye_percent + ">>" +
                "最大伤害：" + damage + ">>" +
                "伤害加成：" + (chr.getStat().dam_r+chr.package_total_damage_percent+(Start.masterApprenticeGain.get(chr.getId()) !=null ? Start.masterApprenticeGain.get(chr.getId()) : 0)) + ">>" +
                "段伤加成：" + dsDamage + ">>" +
                "BOSS伤害加成：" +(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)) + ">>" + (number.get() >0 ? "赋能/套装伤害加成总计：" + number.get() * 100 + "%" : "赋能/套装伤害加成总计：0%")+">>";
        upsertBossDamageRanking(chr.id,chr.getName(),(int)(chr.getStat().bossdam_r+chr.package_boss_damage_percent+chr.getPotential(30)+(number.get()*100)));
        chr.showInstruction(str,240,10);
        chr.getClient().getPlayer().set开启自动回收(chr.getBossLog1("开启自动回收",1)>0 ? true : false) ;

        return number.get();
    }

    static {
        tz_list = (List<tz_model>) new LinkedList();
        dsMap = new ConcurrentHashMap<>();
        tzjc.tzMap = new ConcurrentHashMap<>();
        tzjc.sbMap = new ConcurrentHashMap<>();
        tzjc.suitSys = new ConcurrentHashMap<>();

    }

    public static void upsertBossDamageRanking(int charactersid, String charactersname, int bossdamage) {
        Connection conn = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            // 获取数据库连接
             conn = (Connection) DBConPool.getInstance().getDataSource().getConnection();

            // 查询记录
            String selectQuery = "SELECT id FROM lt_boss_damage_ranking WHERE charactersid = ?";
            selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setInt(1, charactersid);
            rs = selectStmt.executeQuery();

            if (rs.next()) {
                // 如果存在记录则更新
                String updateQuery = "UPDATE lt_boss_damage_ranking SET charactersname = ?, bossdamage = ? WHERE charactersid = ?";
                updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, charactersname);
                updateStmt.setInt(2, bossdamage);
                updateStmt.setInt(3, charactersid);
                updateStmt.executeUpdate();
            } else {
                // 如果不存在记录则插入
                String insertQuery = "INSERT INTO lt_boss_damage_ranking (charactersid, charactersname, bossdamage) VALUES (?, ?, ?)";
                insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, charactersid);
                insertStmt.setString(2, charactersname);
                insertStmt.setInt(3, bossdamage);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            try {
                if (rs != null) rs.close();
                if (selectStmt != null) selectStmt.close();
                if (updateStmt != null) updateStmt.close();
                if (insertStmt != null) insertStmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void 计算最大伤害(MapleCharacter chra){
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
                chra.getClient().getPlayer().getStat().damage = ( (long) ((chra.getClient().getPlayer().getStat().localstr*(LtMS.ConfigValuesMap.get("战士力量系数")/100.00)+chra.getClient().getPlayer().getStat().localdex*0.1+chra.getClient().getPlayer().getStat().localluk*0.1+chra.getClient().getPlayer().getStat().localint_*0.1)*(chra.getClient().getPlayer().getStat().watk*(LtMS.ConfigValuesMap.get("战士物理系数")/100.00)))+1L);
                break;
            case 2000:
            case 2100:
            case 2110:
            case 2111:
            case 2112:
                chra.getClient().getPlayer().getStat().damage = ( (long) ((chra.getClient().getPlayer().getStat().localstr*(LtMS.ConfigValuesMap.get("战神力量系数")/100.00)+chra.getClient().getPlayer().getStat().localdex*0.1+chra.getClient().getPlayer().getStat().localluk*0.1+chra.getClient().getPlayer().getStat().localint_*0.1)*(chra.getClient().getPlayer().getStat().watk*(LtMS.ConfigValuesMap.get("战神物理系数")/100.00)))+1L);
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
                chra.getClient().getPlayer().getStat().damage = ( (long) ((chra.getClient().getPlayer().getStat().localstr*0.1+chra.getClient().getPlayer().getStat().localdex*0.1+chra.getClient().getPlayer().getStat().localluk*0.1+chra.getClient().getPlayer().getStat().localint_*(LtMS.ConfigValuesMap.get("法师智力系数")/100.00))*(chra.getClient().getPlayer().getStat().magic*(LtMS.ConfigValuesMap.get("法师魔力系数")/100.00)))+1L);
                break;
            //射手
            case 300:
            case 310:
            case 311:
            case 312:
            case 320:
            case 321:
            case 322:

                chra.getClient().getPlayer().getStat().damage = ( (long) ((chra.getClient().getPlayer().getStat().localstr*0.1+chra.getClient().getPlayer().getStat().localdex*(LtMS.ConfigValuesMap.get("射手敏捷系数")/100.00)+chra.getClient().getPlayer().getStat().localluk*0.1+chra.getClient().getPlayer().getStat().localint_*0.1)*(chra.getClient().getPlayer().getStat().watk*(LtMS.ConfigValuesMap.get("射手物理系数")/100.00)))+1L);
                break;
            //飞侠
            case 400:
            case 410:
            case 411:
            case 412:
            case 420:
            case 421:
            case 422:

                chra.getClient().getPlayer().getStat().damage = (  (long) ((chra.getClient().getPlayer().getStat().localstr*0.1+chra.getClient().getPlayer().getStat().localdex*0.1+chra.getClient().getPlayer().getStat().localluk*(LtMS.ConfigValuesMap.get("飞侠运气系数")/100.00)+chra.getClient().getPlayer().getStat().localint_*0.1)*(chra.getClient().getPlayer().getStat().watk*(LtMS.ConfigValuesMap.get("飞侠物理系数")/100.00)))+1L);
                break;
            //海盗
            case 500:
            case 510:
            case 511:
            case 512:
                chra.getClient().getPlayer().getStat().damage = (  (long) ((chra.getClient().getPlayer().getStat().localstr*0.1+chra.getClient().getPlayer().getStat().localdex*(LtMS.ConfigValuesMap.get("拳手敏捷系数")/100.00)+chra.getClient().getPlayer().getStat().localluk*0.1+chra.getClient().getPlayer().getStat().localint_*0.1)*(chra.getClient().getPlayer().getStat().watk*(LtMS.ConfigValuesMap.get("海盗物理系数")/100.00)))+1L);
                break;
            case 520:
            case 521:
            case 522:
                chra.getClient().getPlayer().getStat().damage = (  (long) ((chra.getClient().getPlayer().getStat().localstr*0.1+chra.getClient().getPlayer().getStat().localdex*0.1+chra.getClient().getPlayer().getStat().localluk*(LtMS.ConfigValuesMap.get("船长运气系数")/100.00)+chra.getClient().getPlayer().getStat().localint_*0.1)*(chra.getClient().getPlayer().getStat().watk*(LtMS.ConfigValuesMap.get("海盗物理系数")/100.00)))+1L);
                break;
            default:
                chra.getClient().getPlayer().getStat().damage = (  (long) ((chra.getClient().getPlayer().getStat().localstr*0.3+chra.getClient().getPlayer().getStat().localdex*0.3+chra.getClient().getPlayer().getStat().localluk*0.3+chra.getClient().getPlayer().getStat().localint_*0.3)*(chra.getClient().getPlayer().getStat().watk*0.1))+1L);
                break;

        }
    }

    public static void selectBuffIfNotExists(MapleCharacter chra) {
        try{
                chra.buffs.clear();
                chra.buffs.addAll(chra.getBuffsByCharacterId(chra.id));

        } catch (Exception e) {
            //服务端输出信息.println_err("insertBuffIfNotExists 出错：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
