package constants;

import bean.DsModel;
import bean.Leveladdharm;
import bean.LttItemAdditionalDamage;
import bean.SuitSystem;
import client.MapleCharacter;
import client.MapleStat;
import client.inventory.IItem;
import gui.CongMS;
import gui.LtMS;
import handling.channel.MapleGuildRanking;
import server.MapleItemInformationProvider;
import server.Start;
import server.life.MapleMonster;
import tools.FileoutputUtil;
import tools.MaplePacketCreator;
import util.ListUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class tzjc {
    private static List<tz_model> tz_list;
    public static Map<Integer, Long> dsMap;
    public static Map<String,Double> tzMap;
    public static Map<Integer, Map<String, Integer>> sbMap;
    public static Map<String, List<SuitSystem>> suitSys;//套装列表
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
            if (player.get套装伤害加成() > 0.0) {
                double jc_damage = totDamageToOneMonster * player.get套装伤害加成()*damage;
//                if(jc_damage>100000000L){
//                    player.dropTopMsg("[赋能]:增伤" + Math.ceil(jc_damage/100000000L) + "亿");
//                }else if(jc_damage>10000){
//                    player.dropTopMsg("[赋能]:增伤" + Math.ceil(jc_damage/10000) + "万");
//                }else{
//                    player.dropTopMsg("[赋能]:增伤" + Math.ceil(jc_damage)  + "");
//                }
                totDamageToOneMonster = (long) ((double) totDamageToOneMonster + jc_damage)+player.get最高伤害();
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
        suitSys.putAll(Start.suitSystemsMap);
    }

    public static double check_tz(MapleCharacter chr) {
        Collection<IItem> hasEquipped = chr.getHasEquipped();
        //accurateRankMap  段伤
        //enhancedRankMap  赋能
        //dropRankMap   爆率
        //int id = chr.getClient().getPlayer().getId();
        //段伤
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
        chr.dropMessage(5,
                "角色姓名：" + chr.getClient().getPlayer().getName() + ">>"+
                        "力量：" + chr.getStat().localstr + ">>" +
                        "敏捷：" + chr.getStat().localdex + ">>" +
                        "运气：" + chr.getStat().localluk + ">>" +
                        "智力：" + chr.getStat().localint_ + ">>" +
                        "物攻：" + chr.getStat().watk + ">>" +
                        "魔攻：" + chr.getStat().magic + ">>" +
                        "武器熟练度：" + chr.getStat().passive_mastery + ">>" +
                        "爆击概率：" + chr.getStat().passive_sharpeye_rate + ">>" +
                        "爆击最大伤害倍率：" + chr.getStat().passive_sharpeye_percent + ">>" +
                        "最大攻击力：" + chr.getStat().localmaxbasedamage + ">>" +
                        "伤害加成：" + chr.getStat().dam_r + ">>" +
                        "段伤加成：" + additionalDamage.get() + ">>" +
                        "BOSS伤害加成：" + chr.getStat().bossdam_r + ">>" + (number.get() >0 ? "赋能/套装伤害加成总计：" + number.get() * 100 + "%" : "赋能/套装伤害加成总计：0%")
        );
        return number.get();
    }

    static {
        tz_list = (List<tz_model>) new LinkedList();
        dsMap = new Hashtable<>();
        tzjc.tzMap = new Hashtable<>();
        tzjc.sbMap = new Hashtable<>();
        tzjc.suitSys = new Hashtable<>();

    }
}
