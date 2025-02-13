package bean;

import client.*;
import client.inventory.Equip;
import client.inventory.IItem;
import database.DBConPool;
import gui.LtMS;
import server.Randomizer;
import server.Start;
import snail.Potential;
import util.GetRedisDataUtil;
import util.ListUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LtDiabloEquipments {

    private int id; // 自增ID
    private String entryName; // 词条名
    private int type; // 词条类型
    private int level; // 词条等级
    private int probabilityMin; // 概率小值
    private int probabilityMax; // 概率大值
    private short str; // 力量
    private short dex; // 敏捷
    private short _int; // 智力
    private short luk; // 运气
    private short watk; // 物理攻击
    private short matk; // 魔力
    private short wdef; // 物理防御
    private short mdef; // 魔法防御
    private short maxhp; // 血量
    private short maxmp; // 蓝量
    private short resistance; // 抗性（可抵抗BOSS释放的debuff技能）
    private short dodge; // 闪避（可闪避所有）
    private short strPercent; // 力量百分比
    private short dexPercent; // 敏捷百分比
    private short intPercent; // 智力百分比
    private short lukPercent; // 运气百分比
    private int skillId; // 附加技能
    private String skillName; // 附加技能
    private short skillType; // 技能类型
    private int keyPosition; // 键位
    private short skillDamage; // 技能伤害
    private int skillDs; // 技能段数
    private int skillSl; // 技能打击数量
    private String skillTx; // 特效
    private short watkPercent; // 物理攻击百分比
    private short matkPercent; // 魔力百分比
    private short wdefPercent; // 物理防御百分比
    private short mdefPercent; // 魔法防御百分比
    private short maxhpPercent; // 血量百分比
    private short maxmpPercent; // 蓝量百分比
    private short normalDamagePercent; // 普通怪物伤害加成
    private short bossDamagePercent; // boss伤害加成
    private short totalDamagePercent; // 总伤害加成
    private short dropRate; // 掉落率
    private short dropRateCount; // 掉落率数量
    private short expRate; // 经验倍率
    private short expRateCount; // 经验倍率
    private short mesoRate; // 掉落率
    private short mesoRateCount; // 掉落率

    /**
     * 取消装备转换
     */
    public static void dataStatisticalDelete(Equip target, MapleClient c, MapleCharacter character){
        List<LtDiabloEquipments> list = GetRedisDataUtil.getLtDiabloEquipments();
        int reqlv,dex,_int,luk,hp,mp,watk,matk,wdef,mdef;
        // 定义一个容器来存储所有属性的和
        short totalStr = 0;
        short totalDex = 0;
        short totalInt = 0;
        short totalLuk = 0;
        short totalWatk = 0;
        short totalMatk = 0;
        short totalWdef = 0;
        short totalMdef = 0;
        short totalMaxHp = 0;
        short totalMaxMp = 0;
        short totalStrPercent = 0;
        short totalDexPercent = 0;
        short totalIntPercent = 0;
        short totalLukPercent = 0;
        short totalWatkPercent = 0;
        short totalMatkPercent = 0;
        short totalWdefPercent = 0;
        short totalMdefPercent = 0;
        short totalMaxHpPercent = 0;
        short totalMaxMpPercent = 0;

        if (target != null && ListUtil.isNotEmpty(list)) {
            if ((Integer) LtMS.ConfigValuesMap.get("暗黑破坏神系统开关") > 0) {
                String str = target.getOwner();
                for (LtDiabloEquipments ltDiabloEquipments : list) {
                    //判断署名中是否包含当前词条的名称
                    if (str.contains(ltDiabloEquipments.entryName)) {
                        str = str.replace(ltDiabloEquipments.entryName, "");
                        // 累加属性的值
                        totalStr += ltDiabloEquipments.str;
                        totalDex += ltDiabloEquipments.dex;
                        totalInt += ltDiabloEquipments._int; // 注意变量名不能使用关键字，因此使用 _int
                        totalLuk += ltDiabloEquipments.luk;
                        totalWatk += ltDiabloEquipments.watk;
                        totalMatk += ltDiabloEquipments.matk;
                        totalWdef += ltDiabloEquipments.wdef;
                        totalMdef += ltDiabloEquipments.mdef;
                        totalMaxHp += ltDiabloEquipments.maxhp;
                        totalMaxMp += ltDiabloEquipments.maxmp;
                        totalStrPercent += ltDiabloEquipments.strPercent;
                        totalDexPercent += ltDiabloEquipments.dexPercent;
                        totalIntPercent += ltDiabloEquipments.intPercent;
                        totalLukPercent += ltDiabloEquipments.lukPercent;
                        totalWatkPercent += ltDiabloEquipments.watkPercent;
                        totalMatkPercent += ltDiabloEquipments.matkPercent;
                        totalWdefPercent += ltDiabloEquipments.wdefPercent;
                        totalMdefPercent += ltDiabloEquipments.mdefPercent;
                        totalMaxHpPercent += ltDiabloEquipments.maxhpPercent;
                        totalMaxMpPercent += ltDiabloEquipments.maxmpPercent;
                    }
                }
                reqlv = target.getStr() - totalStr;
                if (reqlv < 0) {
                    reqlv = 0;
                }

                target.setStr((short) reqlv);
                dex = target.getDex() - totalDex;
                if (dex < 0) {
                    dex = 0;
                }

                target.setDex((short) dex);
                _int = target.getInt() - totalInt;
                if (_int < 0) {
                    _int = 0;
                }

                target.setInt((short) _int);
                luk = target.getLuk() - totalLuk;
                if (luk < 0) {
                    luk = 0;
                }

                target.setLuk((short) luk);
                hp = target.getHp() - totalMaxHp;
                if (hp < 0) {
                    hp = 0;
                }

                target.setHp((short) hp);
                mp = target.getMp() - totalMaxMp;
                if (mp < 0) {
                    mp = 0;
                }

                target.setMp((short) mp);
                watk = target.getWatk() - totalWatk;
                if (watk < 0) {
                    watk = 0;
                }

                target.setWatk((short) watk);
                matk = target.getMatk() - totalMatk;
                if (matk < 0) {
                    matk = 0;
                }

                target.setMatk((short) matk);
                wdef = target.getWdef() - totalWdef;
                if (wdef < 0) {
                    wdef = 0;
                }

                target.setWdef((short) wdef);
                mdef = target.getMdef() - totalMdef;
                if (mdef < 0) {
                    mdef = 0;
                }

                target.setMdef((short) mdef);
                //抗性（可抵抗BOSS释放的debuff技能）
            }
                if (totalStrPercent > 0) {
                        Potential.setPotential(target, (short)14, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，力量-" + totalStrPercent + "%");
                    }

                    if (totalDexPercent > 0) {
                        Potential.setPotential(target, (short)15, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，敏捷-" + totalDexPercent + "%");
                    }

                    if (totalIntPercent > 0) {
                        Potential.setPotential(target, (short)16, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，智力-" + totalIntPercent + "%");
                    }

                    if (totalLukPercent > 0) {
                        Potential.setPotential(target, (short)17, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，运气-" + totalLukPercent + "%");
                    }

                    if (totalMaxHpPercent > 0) {
                        Potential.setPotential(target, (short)18, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxHp-" + totalMaxHpPercent + "%");
                    }

                    if (totalMaxMpPercent > 0) {
                        Potential.setPotential(target, (short)19, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxMp-" + totalMaxMpPercent + "%");
                    }

                    if (totalWatkPercent > 0) {
                        Potential.setPotential(target, (short)20, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，攻击力-" +  totalWatkPercent + "%");
                    }

                    if (totalMatkPercent > 0) {
                        Potential.setPotential(target, (short)21, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法力-" + totalMatkPercent + "%");
                    }

                    if (totalWdefPercent > 0) {
                        Potential.setPotential(target, (short)22, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，物理防御力-" + totalWdefPercent + "%");
                    }

                    if (totalMdefPercent > 0) {
                        Potential.setPotential(target, (short)23, 0, 0);
                        c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法防御力-" + totalMdefPercent + "%");
                    }
                    c.getPlayer().reFreshItem(target);
            }
     }


     /**
     * 统计转换
     */
    public static void dataStatisticalAdd(Equip source, MapleClient c, MapleCharacter character){
        List<LtDiabloEquipments> list = GetRedisDataUtil.getLtDiabloEquipments();
        int reqlv,dex,_int,luk,hp,mp,watk,matk,wdef,mdef;
        // 定义一个容器来存储所有属性的和
        short totalStr = 0;
        short totalDex = 0;
        short totalInt = 0;
        short totalLuk = 0;
        short totalWatk = 0;
        short totalMatk = 0;
        short totalWdef = 0;
        short totalMdef = 0;
        short totalMaxHp = 0;
        short totalMaxMp = 0;


        short totalStrPercent = 0;
        short totalDexPercent = 0;
        short totalIntPercent = 0;
        short totalLukPercent = 0;
        short totalWatkPercent = 0;
        short totalMatkPercent = 0;
        short totalWdefPercent = 0;
        short totalMdefPercent = 0;
        short totalMaxHpPercent = 0;
        short totalMaxMpPercent = 0;


        if (source != null && ListUtil.isNotEmpty(list)) {
            if ((Integer) LtMS.ConfigValuesMap.get("暗黑破坏神系统开关") > 0) {
                String str = source.getOwner();
                for (LtDiabloEquipments ltDiabloEquipments : list) {
                    //判断署名中是否包含当前词条的名称
                    if(str.contains(ltDiabloEquipments.entryName)){
                        str = str.replace(ltDiabloEquipments.entryName,"");
                        // 累加属性的值
                        totalStr += ltDiabloEquipments.str;
                        totalDex += ltDiabloEquipments.dex;
                        totalInt += ltDiabloEquipments._int; // 注意变量名不能使用关键字，因此使用 _int
                        totalLuk += ltDiabloEquipments.luk;
                        totalWatk += ltDiabloEquipments.watk;
                        totalMatk += ltDiabloEquipments.matk;
                        totalWdef += ltDiabloEquipments.wdef;
                        totalMdef += ltDiabloEquipments.mdef;
                        totalMaxHp += ltDiabloEquipments.maxhp;
                        totalMaxMp += ltDiabloEquipments.maxmp;
                        totalStrPercent += ltDiabloEquipments.strPercent;
                        totalDexPercent += ltDiabloEquipments.dexPercent;
                        totalIntPercent += ltDiabloEquipments.intPercent;
                        totalLukPercent += ltDiabloEquipments.lukPercent;
                        totalWatkPercent += ltDiabloEquipments.watkPercent;
                        totalMatkPercent += ltDiabloEquipments.matkPercent;
                        totalWdefPercent += ltDiabloEquipments.wdefPercent;
                        totalMdefPercent += ltDiabloEquipments.mdefPercent;
                        totalMaxHpPercent += ltDiabloEquipments.maxhpPercent;
                        totalMaxMpPercent += ltDiabloEquipments.maxmpPercent;
                    }
                }
                reqlv = source.getStr() + totalStr;
                if (reqlv > 32767) {
                    reqlv = 0;
                }

                source.setStr((short)reqlv);
                dex = source.getDex() + totalDex;
                if (dex > 32767) {
                    dex = 32767;
                }

                source.setDex((short)dex);
                _int = source.getInt() + totalInt;
                if (_int > 32767) {
                    _int = 32767;
                }

                source.setInt((short)_int);
                luk = source.getLuk() + totalLuk;
                if (luk > 32767) {
                    luk = 32767;
                }

                source.setLuk((short)luk);
                hp = source.getHp() + totalMaxHp;
                if (hp > 32767) {
                    hp = 32767;
                }

                source.setHp((short)hp);
                mp = source.getMp() + totalMaxMp;
                if (mp > 32767) {
                    mp = 32767;
                }

                source.setMp((short)mp);
                watk = source.getWatk() + totalWatk;
                if (watk > 32767) {
                    watk = 32767;
                }

                source.setWatk((short)watk);
                matk = source.getMatk() + totalMatk;
                if (matk > 32767) {
                    matk = 32767;
                }

                source.setMatk((short)matk);
                wdef = source.getWdef() + totalWdef;
                if (wdef > 32767) {
                    wdef = 32767;
                }

                source.setWdef((short)wdef);
                mdef = source.getMdef() + totalMdef;
                if (mdef > 32767) {
                    mdef = 32767;
                }

                source.setMdef((short)mdef);
                //抗性（可抵抗BOSS释放的debuff技能）
            }

            if (totalStrPercent > 0) {
                Potential.setPotential(source, (short)14, 5, totalStrPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，力量+" + totalStrPercent + "%");
            }

            if (totalDexPercent > 0) {
                Potential.setPotential(source, (short)15, 6, totalDexPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，敏捷+" + totalDexPercent + "%");
            }

            if (totalIntPercent > 0) {
                Potential.setPotential(source, (short)16, 7, totalIntPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，智力+" + totalIntPercent + "%");
            }

            if (totalLukPercent > 0) {
                Potential.setPotential(source, (short)17, 8, totalLukPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，运气-" + totalLukPercent + "%");
            }

            if (totalMaxHpPercent > 0) {
                Potential.setPotential(source, (short)18, 24, totalMaxHpPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxHp-" + totalMaxHpPercent + "%");
            }

            if (totalMaxMpPercent > 0) {
                Potential.setPotential(source, (short)19, 26, totalMaxMpPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，maxMp-" + totalMaxMpPercent + "%");
            }

            if (totalWatkPercent > 0) {
                Potential.setPotential(source, (short)20, 13, totalWatkPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，攻击力-" +  totalWatkPercent + "%");
            }

            if (totalMatkPercent > 0) {
                Potential.setPotential(source, (short)21, 14, totalMatkPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法力-" + totalMatkPercent + "%");
            }

            if (totalWdefPercent > 0) {
                Potential.setPotential(source, (short)22, 16, totalWdefPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，物理防御力-" + totalWdefPercent + "%");
            }

            if (totalMdefPercent > 0) {
                Potential.setPotential(source, (short)23, 18, totalMdefPercent);
                c.getPlayer().dropMessage(5, "该装备栏位取消装备，魔法防御力-" + totalMdefPercent + "%");
            }

            c.getPlayer().reFreshItem(source);
        }
    }




    /**
     * 统计转换
     */
    public static void dataStatisticalAdd(MapleCharacter character) {
        HideAttribute hideAttribute = new HideAttribute();

        List<LtDiabloEquipments> list = GetRedisDataUtil.getLtDiabloEquipments();
        // 定义一个容器来存储所有属性的和
        //抗性（可抵抗BOSS释放的debuff技能）
        Map<Integer,Double> skillDamage = new HashMap<>();
        short totalResistance = 0;
        //闪避
        short totalDodge = 0;

        short totalNormalDamagePercent = 0;
        short totalBossDamagePercent = 0;
        short totalDamagePercent = 0;

        short totalDropRate = 0;
        short totalDropRateCount = 0;
        short totalExpRate = 0;
        short totalExpRateCount = 0;
        short totalMesoRate = 0;
        short totalMesoRateCount = 0;
        Collection<IItem> hasEquipped = character.getHasEquipped();
        if (ListUtil.isNotEmpty(hasEquipped) && ListUtil.isNotEmpty(list)) {
            if ((Integer) LtMS.ConfigValuesMap.get("暗黑破坏神系统开关") > 0) {
                character.LtDiabloEquipmentsList.clear();
                character.triggeredEquipmentsList.clear();
                List<LtDiabloEquipments> ltDiabloEquipmentsList = GetRedisDataUtil.getLtDiabloEquipments();
                for (IItem iItem : hasEquipped) {
                    takeOnEquipNew(iItem,character.getClient(),ltDiabloEquipmentsList);
                    String str = iItem.getOwner();
                    for (LtDiabloEquipments ltDiabloEquipments : list) {
                        //判断署名中是否包含当前词条的名称
                        if (str.contains(ltDiabloEquipments.entryName)) {
                            str = str.replace(ltDiabloEquipments.entryName, "");
                            // 累加属性的值
                            totalResistance += ltDiabloEquipments.resistance;
                            totalDodge += ltDiabloEquipments.dodge;
                            totalNormalDamagePercent += ltDiabloEquipments.normalDamagePercent;
                            totalBossDamagePercent += ltDiabloEquipments.bossDamagePercent;
                            totalDamagePercent += ltDiabloEquipments.totalDamagePercent;
                            totalDropRate += ltDiabloEquipments.dropRate;
                            totalDropRateCount += ltDiabloEquipments.dropRateCount;
                            totalExpRate += ltDiabloEquipments.expRate;
                            totalExpRateCount += ltDiabloEquipments.expRateCount;
                            totalMesoRate += ltDiabloEquipments.mesoRate;
                            totalMesoRateCount += ltDiabloEquipments.mesoRateCount;
                            if (ltDiabloEquipments.getSkillId()!=0 && Objects.nonNull(skillDamage.get(ltDiabloEquipments.getSkillId()))){
                                skillDamage.put(ltDiabloEquipments.getSkillId(),ltDiabloEquipments.getSkillId()/100.00+skillDamage.get(ltDiabloEquipments.getSkillId()));
                            }else if (ltDiabloEquipments.getSkillId()!=0 && Objects.isNull(skillDamage.get(ltDiabloEquipments.getSkillId()))){
                                skillDamage.put(ltDiabloEquipments.getSkillId(),ltDiabloEquipments.getSkillId()/100.00+1);
                            }
                        }
                    }
                }
            }
        }
        //抗性（可抵抗BOSS释放的debuff技能）
        hideAttribute.setTotalResistance(totalResistance);

        hideAttribute.setTotalDodge(totalDodge);

        hideAttribute.setTotalDropRate(totalDropRate);

        hideAttribute.setTotalDropRateCount(totalDropRateCount);

        hideAttribute.setTotalExpRate(totalExpRate);

        hideAttribute.setTotalExpRateCount(totalExpRateCount);

        hideAttribute.setTotalMesoRate(totalMesoRate);

        hideAttribute.setTotalMesoRateCount(totalMesoRateCount);

        hideAttribute.setTotal_normal_damage_percent(totalNormalDamagePercent);

        hideAttribute.setTotal_boss_damage_percent(totalBossDamagePercent);
        hideAttribute.setTotal_total_damage_percent(totalDamagePercent);
        hideAttribute.setSkillDamage(skillDamage);
        Start.hideAttributeMap.put(character.getId(), hideAttribute);
    }
    /**
     * 组装词条
     */
    public static String assembleEntry(int level){
        List<LtDiabloEquipments> list = GetRedisDataUtil.getLtDiabloEquipments();
        List<String> matchedEntryNames = new ArrayList<>();
        if(ListUtil.isEmpty(list)){
            return null;
        }
        for (LtDiabloEquipments ltDiabloEquipments : list) {
            if (ltDiabloEquipments.getLevel() < level){
                return null;
            }
            int randomNum  =  Randomizer.nextInt(1000000);
                       // 如果满足条件，记录 entryName
            if ( randomNum >= ltDiabloEquipments.getProbabilityMin() && randomNum <= ltDiabloEquipments.getProbabilityMax()) {
                matchedEntryNames.add(ltDiabloEquipments.entryName);
                if(ltDiabloEquipments.getSkillType() == 4){
                    break;
                }
            }
            // 如果已记录 3 条，跳出循环
            if (matchedEntryNames.size() >= LtMS.ConfigValuesMap.get("暗黑破坏神系统词条数量")) {
                break;
            }
            }
        // 拼接 entryName
        StringBuilder result = new StringBuilder();

        // 根据记录的数量进行拼接
        if (!matchedEntryNames.isEmpty()) {
            for (String matchedEntryName : matchedEntryNames) {
                result.append(matchedEntryName);
            }
        }
        return result.length()==0 ? null :result.toString(); // 返回拼接结果
    }

    /**
     * 脱下装备
     * @return
     */
    public static void takeOffEquip(Equip target, MapleClient c){
        List<LtDiabloEquipments> list = GetRedisDataUtil.getLtDiabloEquipments();
        if (ListUtil.isNotEmpty(list)) {
            for (LtDiabloEquipments ltDiabloEquipments : list) {
                //判断署名中是否包含当前词条的名称
                if (target.getOwner().contains(ltDiabloEquipments.entryName)) {
                    if (ltDiabloEquipments.getSkillType() == 1 || ltDiabloEquipments.getSkillType() == 3) {
                        c.getPlayer().changeSkillLevel(SkillFactory.getSkill(ltDiabloEquipments.getSkillId()), (byte) 0, (byte) 0);
                        c.getPlayer().dropMessage(5, "你遗忘了 “" + ltDiabloEquipments.entryName + "”词条的 技能。");
                    }else if (ltDiabloEquipments.getSkillType() == 2) {
                        c.getPlayer().LtDiabloEquipmentsList.remove(ltDiabloEquipments);
                    }else if(ltDiabloEquipments.getSkillType() == 5){
                        c.getPlayer().getUserASkill().remove(ltDiabloEquipments.skillId);
                    }else{
                        c.getPlayer().triggeredEquipmentsList.remove(ltDiabloEquipments);
                    }
                }
            }
        }
    }
    /**
     * 穿上装备
     * @return
     */
    public static void takeOnEquip(Equip target, MapleClient c){
        List<LtDiabloEquipments> list = GetRedisDataUtil.getLtDiabloEquipments();
        double damage=1;
        if (ListUtil.isNotEmpty(list)) {
            for (LtDiabloEquipments ltDiabloEquipments : list) {
                //判断署名中是否包含当前词条的名称
                if (target.getOwner().contains(ltDiabloEquipments.entryName)) {
                    if (ltDiabloEquipments.getSkillType() == 1 || ltDiabloEquipments.getSkillType() == 3) {
                        Map<ISkill, SkillEntry> skills = c.getPlayer().getSkills();
                        if (skills.entrySet().stream().noneMatch(entry -> entry.getKey().getId() == ltDiabloEquipments.getSkillId())) {
                            c.getPlayer().newOnKeyboard(ltDiabloEquipments.getKeyPosition(), 1, ltDiabloEquipments.getSkillId());
                            c.getPlayer().dropMessage(5, "你学会了 “" + ltDiabloEquipments.entryName + "”词条的 技能。");
                        }
                    }else if (ltDiabloEquipments.getSkillType() == 2) {
                        c.getPlayer().LtDiabloEquipmentsList.add(ltDiabloEquipments);
                        //将可是放技能加载到个人数据里
                    }else if(ltDiabloEquipments.getSkillType() == 5){
//                        System.out.println("1");
                        ASkill aSkill = Start.ltASkill.get(ltDiabloEquipments.skillId);
                        if(aSkill!=null){
//                            System.out.println("2");
                            damage +=  (ltDiabloEquipments.getSkillDamage()/100.0);
                            aSkill.setDamage(damage);
                            aSkill.setAttackCount(aSkill.getAttackCount()+ltDiabloEquipments.getSkillDs());
                            aSkill.setMobCount(aSkill.getMobCount()+ltDiabloEquipments.getSkillSl());
                            c.getPlayer().setUserASkill(ltDiabloEquipments.skillId,aSkill);
//                            System.out.println(ltDiabloEquipments.skillId+" "+aSkill.getDamage());

                        }
                    }else{
                        c.getPlayer().triggeredEquipmentsList.add(ltDiabloEquipments);
                    }
                }
            }
        }
    }

    /**
     * 穿上装备
     * @return
     */
    public static void takeOnEquipNew(IItem iItem, MapleClient c, List<LtDiabloEquipments> list){
        if (ListUtil.isNotEmpty(list)) {
            for (LtDiabloEquipments ltDiabloEquipments : list) {
                //判断署名中是否包含当前词条的名称
                if (iItem.getOwner().contains(ltDiabloEquipments.entryName)) {
                    if (ltDiabloEquipments.getSkillType() == 1 || ltDiabloEquipments.getSkillType() == 3) {
                        Map<ISkill, SkillEntry> skills = c.getPlayer().getSkills();
                        if (skills.entrySet().stream().noneMatch(entry -> entry.getKey().getId() == ltDiabloEquipments.getSkillId())) {
                            c.getPlayer().newOnKeyboard(ltDiabloEquipments.getKeyPosition(), 1, ltDiabloEquipments.getSkillId());
                            c.getPlayer().dropMessage(5, "你学会了 “" + ltDiabloEquipments.entryName + "”词条的 技能。");
                        }
                    }else if (ltDiabloEquipments.getSkillType() == 2) {
                        c.getPlayer().LtDiabloEquipmentsList.add(ltDiabloEquipments);
                        //将可是放技能加载到个人数据里
                    }else if(ltDiabloEquipments.getSkillType() == 5){
                       // System.out.println("1");
                        ASkill aSkill = Start.ltASkill.get(ltDiabloEquipments.skillId);
                        if(aSkill!=null){
                         //   System.out.println("2");
                            double damage =  (ltDiabloEquipments.getSkillDamage()/100.0+1);
                            if (damage > Short.MAX_VALUE){
                                damage = Short.MAX_VALUE;
                            }
                            aSkill.setDamage((short) damage);
                            aSkill.setAttackCount(ltDiabloEquipments.getSkillDs());
                            aSkill.setMobCount(ltDiabloEquipments.getSkillSl());
                            c.getPlayer().setUserASkill(ltDiabloEquipments.skillId,aSkill);
                           // System.out.println(ltDiabloEquipments.skillId+" "+aSkill.getDamage());
                        }
                    }else if(ltDiabloEquipments.getSkillType() == 6){
                        c.getPlayer().shieldEnhancementEquipmentsList.add(ltDiabloEquipments);
                    }else{
                        c.getPlayer().triggeredEquipmentsList.add(ltDiabloEquipments);
                    }
                }
            }
        }
    }

    /**
     * 加载词条数据数据到redis中
     */
    public static synchronized void setLtDiabloEquipments() {
        List<LtDiabloEquipments> sss = new ArrayList<>();
        Start.sendMsgList.clear();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection();
            String query = "SELECT * FROM lt_diablo_equipments order by CHAR_LENGTH(entry_name) desc";
            PreparedStatement pstmt = con.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                LtDiabloEquipments equipment = new LtDiabloEquipments();
                equipment.setId(rs.getInt("id"));
                equipment.setEntryName(rs.getString("entry_name"));
                equipment.setProbabilityMin(rs.getInt("probability_min"));
                equipment.setProbabilityMax(rs.getInt("probability_max"));
                equipment.setType(rs.getInt("type"));
                equipment.setLevel(rs.getInt("level"));
                equipment.setStr(rs.getShort("str"));
                equipment.setDex(rs.getShort("dex"));
                equipment.set_int(rs.getShort("_int"));
                equipment.setLuk(rs.getShort("luk"));
                equipment.setWatk(rs.getShort("watk"));
                equipment.setMatk(rs.getShort("matk"));
                equipment.setWdef(rs.getShort("wdef"));
                equipment.setMdef(rs.getShort("mdef"));
                equipment.setMaxhp(rs.getShort("maxhp"));
                equipment.setMaxmp(rs.getShort("maxmp"));
                equipment.setResistance(rs.getShort("resistance"));
                equipment.setDodge(rs.getShort("dodge"));
                equipment.setStrPercent(rs.getShort("str_percent"));
                equipment.setDexPercent(rs.getShort("dex_percent"));
                equipment.setIntPercent(rs.getShort("_int_percent"));
                equipment.setLukPercent(rs.getShort("luk_percent"));
                equipment.setSkillId(rs.getInt("skill_id"));
                equipment.setSkillName(rs.getString("skill_name"));
                equipment.setSkillType(rs.getShort("skill_type"));
                equipment.setSkillDamage(rs.getShort("skill_damage"));
                equipment.setSkillDs(rs.getInt("skill_ds"));
                equipment.setSkillSl(rs.getInt("skill_sl"));
                equipment.setSkillTx(rs.getString("skill_tx"));
                equipment.setWatkPercent(rs.getShort("watk_percent"));
                equipment.setMatkPercent(rs.getShort("matk_percent"));
                equipment.setWdefPercent(rs.getShort("wdef_percent"));
                equipment.setMdefPercent(rs.getShort("mdef_percent"));
                equipment.setMaxhpPercent(rs.getShort("maxhp_percent"));
                equipment.setMaxmpPercent(rs.getShort("maxmp_percent"));
                equipment.setNormalDamagePercent(rs.getShort("normal_damage_percent"));
                equipment.setBossDamagePercent(rs.getShort("boss_damage_percent"));
                equipment.setTotalDamagePercent(rs.getShort("total_damage_percent"));
                equipment.setDropRate(rs.getShort("drop_rate"));
                equipment.setDropRateCount(rs.getShort("drop_rate_count"));
                equipment.setExpRate(rs.getShort("exp_rate"));
                equipment.setExpRateCount(rs.getShort("exp_rate_count"));
                equipment.setMesoRate(rs.getShort("meso_rate"));
                equipment.setMesoRateCount(rs.getShort("meso_rate_count"));
                if(rs.getInt("send_msg") ==1){
                    Start.sendMsgList.add(rs.getString("entry_name"));
                }
                sss.add(equipment);
            }
//            RedisUtil.hdel(RedisUtil.KEYNAMES.SET_LT_DIABLO_EQUIPMENTS.getKeyName(), RedisUtil.KEYNAMES.SET_LT_DIABLO_EQUIPMENTS.getKeyName());
//            RedisUtil.hset(RedisUtil.KEYNAMES.SET_LT_DIABLO_EQUIPMENTS.getKeyName(), RedisUtil.KEYNAMES.SET_LT_DIABLO_EQUIPMENTS.getKeyName(), JSONObject.toJSONString(sss) );
            Start.ltDiabloEquipments.clear();
            Start.ltDiabloEquipments.addAll(sss);
            rs.close();
            pstmt.close();
            con.close();
            System.out.println("暗黑破坏神玩法词条加载数量：" + sss.size());
        } catch (SQLException ex) {
            System.out.println("暗黑破坏神玩法词条加载异常：" + ex.getMessage());
        }
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getKeyPosition() {
        return keyPosition;
    }

    public void setKeyPosition(int keyPosition) {
        this.keyPosition = keyPosition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEntryName() {
        return entryName;
    }

    public void setEntryName(String entryName) {
        this.entryName = entryName;
    }

    public int getProbabilityMin() {
        return probabilityMin;
    }

    public void setProbabilityMin(int probability) {
        this.probabilityMin = probability;
    }

    public int getProbabilityMax() {
        return probabilityMax;
    }

    public void setProbabilityMax(int probabilityMax) {
        this.probabilityMax = probabilityMax;
    }

    public short getStr() {
        return str;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public short getDex() {
        return dex;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public short get_int() {
        return _int;
    }

    public void set_int(short _int) {
        this._int = _int;
    }

    public short getLuk() {
        return luk;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public short getWatk() {
        return watk;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public short getMatk() {
        return matk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public short getWdef() {
        return wdef;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public short getMaxhp() {
        return maxhp;
    }

    public void setMaxhp(short maxhp) {
        this.maxhp = maxhp;
    }

    public short getMaxmp() {
        return maxmp;
    }

    public void setMaxmp(short maxmp) {
        this.maxmp = maxmp;
    }

    public short getResistance() {
        return resistance;
    }

    public void setResistance(short resistance) {
        this.resistance = resistance;
    }

    public short getDodge() {
        return dodge;
    }

    public void setDodge(short dodge) {
        this.dodge = dodge;
    }

    public short getStrPercent() {
        return strPercent;
    }

    public void setStrPercent(short strPercent) {
        this.strPercent = strPercent;
    }

    public short getDexPercent() {
        return dexPercent;
    }

    public void setDexPercent(short dexPercent) {
        this.dexPercent = dexPercent;
    }

    public short getIntPercent() {
        return intPercent;
    }

    public void setIntPercent(short intPercent) {
        this.intPercent = intPercent;
    }

    public short getLukPercent() {
        return lukPercent;
    }

    public void setLukPercent(short lukPercent) {
        this.lukPercent = lukPercent;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public short getSkillType() {
        return skillType;
    }

    public void setSkillType(short skillType) {
        this.skillType = skillType;
    }

    public short getSkillDamage() {
        return skillDamage;
    }

    public void setSkillDamage(short skillDamage) {
        this.skillDamage = skillDamage;
    }

    public int getSkillDs() {
        return skillDs;
    }

    public void setSkillDs(int skillDs) {
        this.skillDs = skillDs;
    }

    public int getSkillSl() {
        return skillSl;
    }

    public void setSkillSl(int skillSl) {
        this.skillSl = skillSl;
    }

    public String getSkillTx() {
        return skillTx;
    }

    public void setSkillTx(String skillTx) {
        this.skillTx = skillTx;
    }

    public short getWatkPercent() {
        return watkPercent;
    }

    public void setWatkPercent(short watkPercent) {
        this.watkPercent = watkPercent;
    }

    public short getMatkPercent() {
        return matkPercent;
    }

    public void setMatkPercent(short matkPercent) {
        this.matkPercent = matkPercent;
    }

    public short getWdefPercent() {
        return wdefPercent;
    }

    public void setWdefPercent(short wdefPercent) {
        this.wdefPercent = wdefPercent;
    }

    public short getMdefPercent() {
        return mdefPercent;
    }

    public void setMdefPercent(short mdefPercent) {
        this.mdefPercent = mdefPercent;
    }

    public short getMaxhpPercent() {
        return maxhpPercent;
    }

    public void setMaxhpPercent(short maxhpPercent) {
        this.maxhpPercent = maxhpPercent;
    }

    public short getMaxmpPercent() {
        return maxmpPercent;
    }

    public void setMaxmpPercent(short maxmpPercent) {
        this.maxmpPercent = maxmpPercent;
    }

    public short getNormalDamagePercent() {
        return normalDamagePercent;
    }

    public void setNormalDamagePercent(short normalDamagePercent) {
        this.normalDamagePercent = normalDamagePercent;
    }

    public short getBossDamagePercent() {
        return bossDamagePercent;
    }

    public void setBossDamagePercent(short bossDamagePercent) {
        this.bossDamagePercent = bossDamagePercent;
    }

    public short getTotalDamagePercent() {
        return totalDamagePercent;
    }

    public void setTotalDamagePercent(short totalDamagePercent) {
        this.totalDamagePercent = totalDamagePercent;
    }

    public short getDropRate() {
        return dropRate;
    }

    public void setDropRate(short dropRate) {
        this.dropRate = dropRate;
    }

    public short getDropRateCount() {
        return dropRateCount;
    }

    public void setDropRateCount(short dropRateCount) {
        this.dropRateCount = dropRateCount;
    }

    public short getExpRate() {
        return expRate;
    }

    public void setExpRate(short expRate) {
        this.expRate = expRate;
    }

    public short getExpRateCount() {
        return expRateCount;
    }

    public void setExpRateCount(short expRateCount) {
        this.expRateCount = expRateCount;
    }

    public short getMesoRate() {
        return mesoRate;
    }

    public void setMesoRate(short mesoRate) {
        this.mesoRate = mesoRate;
    }

    public short getMesoRateCount() {
        return mesoRateCount;
    }

    public void setMesoRateCount(short mesoRateCount) {
        this.mesoRateCount = mesoRateCount;
    }
}
