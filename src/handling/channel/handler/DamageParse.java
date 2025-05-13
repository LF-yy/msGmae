package handling.channel.handler;

import bean.ASkill;
import bean.BreakthroughMechanism;
import bean.HideAttribute;
import bean.LtDiabloEquipments;
import client.inventory.MapleWeaponType;
import constants.tzjc;
import database.DBConPool;
import gui.LtMS;
import handling.world.World;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import server.Start;
import server.maps.*;
import snail.Potential;
import tools.data.LittleEndianAccessor;

import java.awt.*;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

import client.MapleJob;

import server.life.Element;
import client.anticheat.CheatTracker;
import client.inventory.IItem;
import server.life.MapleMonsterStats;
import server.life.MapleMonster;
import client.PlayerStats;
import constants.ServerConfig;

import java.util.List;
import java.util.Map.Entry;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatusEffect;
import server.Randomizer;
import client.SkillFactory;
import java.awt.geom.Point2D;
import java.util.concurrent.ConcurrentHashMap;

import tools.FileoutputUtil;
import tools.Pair;
import client.status.MonsterStatus;
import client.MapleBuffStat;
import tools.AttackPair;
import constants.GameConstants;
import tools.MaplePacketCreator;
import client.anticheat.CheatingOffense;
import server.MapleStatEffect;
import client.MapleCharacter;
import client.ISkill;
import tools.packet.MobPacket;
import tools.packet.UIPacket;
import util.ListUtil;
import util.NumberUtils;

public class DamageParse {
    public static int 固定伤害;
    public static Map<Integer, Double> MobRedDam;
    public static Map<Integer, Double> MobData;
    public static void applyAttack(final AttackInfo attack, final ISkill theSkill, final MapleCharacter player, int attackCount, final double maxDamagePerMonster, final MapleStatEffect effect, final AttackType attack_type) {
        if (!player.isAlive()) {//判断角色是否还活着
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        if (attack.real) {//判断攻击是否真实的
          int ii =   player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
          if (ii==1){
              return;
          }
        }
        if (attack.skill != 0) {//判断技能是否为0
            if (effect == null) {//判断特效是否为空
                player.getClient().sendPacket(MaplePacketCreator.enableActions());
                return;
            }
            if (GameConstants.isMulungSkill(attack.skill)) {
                if (player.getmulungEnergy() < 10000) {
                    return;
                }
                if (player.getMapId() / 10000 != 92502) {
                    return;
                }
                player.mulungEnergyModify(false);
            }
            if (GameConstants.isPyramidSkill(attack.skill) && (player.getMapId() / 1000000 != 926 || player.getPyramidSubway() == null || !player.getPyramidSubway().onSkillUse(player))) {
                return;
            }
            if (GameConstants.isAran((int)player.getJob())) {
                final int reduce = player.Aran_ReduceCombo(attack.skill);
                if (reduce > 0) {
                    player.setCombo(player.getCombo() - reduce);
                }
            }
            int last = attackCount;
            boolean mirror_fix = false;
            if (player.getJob() >= 411 && player.getJob() <= 412) {
                mirror_fix = true;
            }
            if (player.getJob() >= 1400 && player.getJob() <= 1412) {
                mirror_fix = true;
            }
            if (player.getJob() >= 300 && player.getJob() <= 312) {
                mirror_fix = true;
            }
            if (attack.skill == 11101004) {
                last = 2;
            }
            if (attack.skill == 15111007) {
                last = 3;
            }
            if (mirror_fix) {
                last *= 2;
            }
            effect.getMobCount();
        }
        if (attack.hits > 0 && attack.targets > 0 && !player.getStat().checkEquipDurabilitys(player, -1)) {
            player.dropMessage(5, "An item has run out of durability but has no inventory room to go to.");
            return;
        }
        int totDamage = 0;
        final MapleMap map = player.getMap();
        if (attack.skill == 4211006) {
            for (final AttackPair oned : attack.allDamage) {
                if (oned.attack != null) {
                    continue;
                }
                final MapleMapObject mapobject = map.getMapObject(oned.objectid, MapleMapObjectType.ITEM);
                if (mapobject == null) {
                    player.getCheatTracker().registerOffense(CheatingOffense.EXPLODING_NONEXISTANT);
                    return;
                }
                final MapleMapItem mapitem = (MapleMapItem)mapobject;
                mapitem.getLock().lock();
                try {
                    if (mapitem.getMeso() <= 0) {
                        player.getCheatTracker().registerOffense(CheatingOffense.ETC_EXPLOSION);
                        return;
                    }
                    if (mapitem.isPickedUp()) {
                        return;
                    }
                    map.removeMapObject((MapleMapObject)mapitem);
                    map.broadcastMessage(MaplePacketCreator.explodeDrop(mapitem.getObjectId()));
                    mapitem.setPickedUp(true);
                }
                finally {
                    mapitem.getLock().unlock();
                }
            }
        }
        int 附加伤害总和 = 0;
        long newtotDamageToOneMonster = 0L;
        final PlayerStats stats = player.getStat();
        final int CriticalDamage = stats.passive_sharpeye_percent();
        byte ShdowPartnerAttackPercentage = 0;
        if (attack_type == AttackType.RANGED_WITH_SHADOWPARTNER || attack_type == AttackType.NON_RANGED_WITH_MIRROR) {
            MapleStatEffect shadowPartnerEffect;
            if (attack_type == AttackType.NON_RANGED_WITH_MIRROR) {
                shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.MIRROR_IMAGE);
            }
            else {
                shadowPartnerEffect = player.getStatForBuff(MapleBuffStat.SHADOWPARTNER);
            }
            if (shadowPartnerEffect != null) {
                if (attack.skill != 0 && attack_type != AttackType.NON_RANGED_WITH_MIRROR) {
                    ShdowPartnerAttackPercentage = (byte)shadowPartnerEffect.getY();
                }
                else {
                    ShdowPartnerAttackPercentage = (byte)shadowPartnerEffect.getX();
                }
            }
            attackCount /= 2;
        }
        HideAttribute hideAttribute = Start.hideAttributeMap.get(player.getId());

        int pDamagePercent = player.getPotential(29);
        int pDamagePercentBoss = player.getPotential(30);
        int pDamagePercentNormal = player.getPotential(31);
        int packageDamagePercent = player.package_total_damage_percent + hideAttribute.getTotal_total_damage_percent()+(Start.masterApprenticeGain.get(player.getId()) !=null ? Start.masterApprenticeGain.get(player.getId()) : 0);
        int packageDamagePercentBoss = player.package_boss_damage_percent + hideAttribute.getTotal_boss_damage_percent();
        int packageDamagePercentNormal = player.package_normal_damage_percent + hideAttribute.getTotal_normal_damage_percent();
        double skillDamage = 1.0;
        if(Objects.nonNull(hideAttribute.getSkillDamage()) && hideAttribute.getSkillDamage().size()>0 && Objects.nonNull(effect) && Objects.nonNull(hideAttribute.getSkillDamage().get(effect.getSourceId()))){
            skillDamage = hideAttribute.getSkillDamage().get(effect.getSourceId());
        }
        int pDamagePercentSkill = 0;
        if (Potential.isDamageSkill(attack.skill)) {
            pDamagePercentSkill = player.getPotential(attack.skill);
        }
        double 分身伤害加成 = 0.0;
        try {
            WeakReference<MapleCharacter>[] clones = player.getClones();
            if (clones != null && clones.length > 0) {
                for (WeakReference<MapleCharacter> mapleCharacterWeakReference : clones) {
                    MapleCharacter clone = mapleCharacterWeakReference.get();
                    if (clone != null) {
                        分身伤害加成 = 分身伤害加成 + clone.getCloneDamagePercentage();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("物理克隆伤害加成计算错误");
        }

        //特效攻击,如:致命一击 2倍伤害 3倍伤害 4倍伤害 5倍伤害
        if (ListUtil.isNotEmpty(player.LtDiabloEquipmentsList)){
            try {
                for (LtDiabloEquipments ltDiabloEquipments : player.LtDiabloEquipmentsList) {
                    int rom = RandomUtils.nextInt(1000000);
                    if(theSkill.getId() == 5221004 || theSkill.getId() == 13111000 || theSkill.getId() == 3121004 ){
                        rom = RandomUtils.nextInt(4000000);
                    }
                    if (rom >ltDiabloEquipments.getSkillDs() && rom<ltDiabloEquipments.getSkillSl()) {
                        分身伤害加成+= ltDiabloEquipments.getSkillDamage();
                        if(分身伤害加成> LtMS.ConfigValuesMap.get("倍率伤害加成上限")){
                            分身伤害加成 = LtMS.ConfigValuesMap.get("倍率伤害加成上限");
                        }
                        if(StringUtils.isNotEmpty(ltDiabloEquipments.getSkillTx())){
                            player.getClient().sendPacket(UIPacket.AranTutInstructionalBalloon(ltDiabloEquipments.getSkillTx()));
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (ListUtil.isNotEmpty(player.shieldEnhancementEquipmentsList)){
            try {
                boolean flg = false;
                String skillTx = "";
                for (LtDiabloEquipments ltDiabloEquipments : player.shieldEnhancementEquipmentsList) {
                    int rom = RandomUtils.nextInt(1000000);
                    if(theSkill.getId() == 5221004 || theSkill.getId() == 13111000 || theSkill.getId() == 3121004 ){
                        rom = RandomUtils.nextInt(4000000);
                    }
                    if (rom >ltDiabloEquipments.getSkillDs() && rom<ltDiabloEquipments.getSkillSl()) {
                        if(StringUtils.isNotEmpty(ltDiabloEquipments.getSkillTx())){
                            player.getStat().setHphd(player.getStat().getHphd() + ltDiabloEquipments.getSkillDamage());
                            flg = true;
                            skillTx = ltDiabloEquipments.getSkillTx();
                        }
                    }
                }
                if (flg && StringUtils.isNotEmpty(skillTx)) {
                    player.getClient().sendPacket(UIPacket.AranTutInstructionalBalloon(skillTx));
                }
            } catch (Exception e) {}
        }

        double maxDamagePerHit = 0.0;
        //词条加成额外计算

        for (final AttackPair oned2 : attack.allDamage) {
            final MapleMonster monster = map.getMonsterByOid(oned2.objectid);
            if (monster != null) {
                 newtotDamageToOneMonster = 0L;
                final MapleMonsterStats monsterstats = monster.getStats();
                final int fixeddmg = monsterstats.getFixedDamage();
                maxDamagePerHit = calculateMaxWeaponDamagePerHit(player, monster, attack, theSkill, effect, maxDamagePerMonster, CriticalDamage);
               // byte overallAttackCount = 0;
                //计算技能每段伤害
                for (final Pair<Integer, Boolean> eachde : oned2.attack) {
                    Integer eachd = eachde.left;
                  //  ++overallAttackCount;
                   // if (overallAttackCount - 1 == attackCount) {
                       // double min = maxDamagePerHit;
                        //final double shadow = ((double)ShdowPartnerAttackPercentage == 0.0) ? 1.0 : ((double)ShdowPartnerAttackPercentage);
                     //   if (ShdowPartnerAttackPercentage != 0) {
                      //      min = maxDamagePerHit / 100.0;
                      //  }
                       // final double dam = monsterstats.isBoss() ? stats.bossdam_r : stats.dam_r;
                       // final double last2 = maxDamagePerHit = min * (shadow * dam / 100.0);
                    //}
                    //固定伤害
                    if (fixeddmg != -1) {
                        //普通攻击
                        if (monsterstats.getOnlyNoramlAttack()) {
                            eachd = (attack.skill != 0) ? 0 : fixeddmg;
                        }
                        else {
                            eachd = fixeddmg;
                        }
                    }else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = (attack.skill != 0) ? 0 : Math.min((int) eachd, (int) maxDamagePerHit);
                    }
//                    if(effect!=null && player.getUserASkill()!=null && player.getUserASkill().size()>0 && player.getUserASkill().get(effect.getSourceId())!=null) {
//                        eachd = (int)(eachd * player.getUserASkill().get(effect.getSourceId()).getDamage());
//                    }
                    if(eachd < 0 ){
                        eachd = Integer.MAX_VALUE;
                    }
                    if (eachd > (player.读取伤害上限值() >Integer.MAX_VALUE? Integer.MAX_VALUE : player.读取伤害上限值())&& LtMS.ConfigValuesMap.get("破总伤") ==0) {
                        eachd = Math.toIntExact((player.读取伤害上限值() > Integer.MAX_VALUE ? Integer.MAX_VALUE : player.读取伤害上限值()));
                    }
                    newtotDamageToOneMonster += (long)eachd;
                    if (monster.getId() == 9300021 && player.getPyramidSubway() != null) {
                        player.getPyramidSubway().onMiss(player);
                    }
                }
//                if(effect!=null && ( checkDamage(player, effect.getDamage(), effect.getAttackCount()) * LtMS.ConfigValuesMap.get("封号标准倍率")) < newtotDamageToOneMonster){
//                    //封号
//                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + player.getName() + " 伤害检测异常，恭喜他成就了封号斗罗。"));
//                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + player.getName() + " 伤害检测异常，恭喜他成就了封号斗罗。"));
//                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + player.getName() + " 伤害检测异常，恭喜他成就了封号斗罗。"));
//                    player.ban("倍攻开挂", true, true, true);
//                }
                //职业伤害调整
                if (newtotDamageToOneMonster > player.读取伤害上限值() && LtMS.ConfigValuesMap.get("破总伤") >0) {
                    newtotDamageToOneMonster = player.读取伤害上限值();
                }
                totDamage += newtotDamageToOneMonster;
                player.checkMonsterAggro(monster);
                final double range = player.getPosition().distanceSq((Point2D)monster.getPosition());
                final double SkillRange = GameConstants.getAttackRange(player, effect, attack);
                if(LtMS.ConfigValuesMap.get("启用吸怪") ==0) {
                    if (player.getDebugMessage() && range > SkillRange*3) {
                        player.dropMessage("技能[" + attack.skill + "] 预计范围: " + (int) SkillRange + " 实际范围: " + (int) range + "");
                    }
                    if (range > SkillRange*3 && !player.inBossMap()) {
                        player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, "攻击范围异常,技能:" + attack.skill + "(" + SkillFactory.getName(attack.skill) + ")\u3000怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 計算范围:" + (int) range);
                        if (range > SkillRange * 4) {
                            player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_BAN, "超大攻击范围,技能:" + attack.skill + "(" + SkillFactory.getName(attack.skill) + ")\u3000怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 計算范围:" + (int) range);
                        }
                        return;
                    }
                }

                if (player.getBuffedValue(MapleBuffStat.PICKPOCKET) != null) {
                    switch (attack.skill) {
                        case 0:
                        case 4001334:
                        case 4201005:
                        case 4211002:
                        case 4211004:
                        case 4221003:
                        case 4221007: {
                            handlePickPocket(player, monster, oned2);
                            break;
                        }
                    }
                }
                final MapleStatEffect ds = player.getStatForBuff(MapleBuffStat.DARKSIGHT);
                if (ds != null && ds.getSourceId() != 9001004 && (ds.getSourceId() != 4330001 || !ds.makeChanceResult())) {
                    player.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
                }
                final MapleStatEffect wd = player.getStatForBuff(MapleBuffStat.WIND_WALK);
                if (wd != null && player.getJob() >= 1300 && player.getJob() <= 1312) {
                    player.cancelEffectFromBuffStat(MapleBuffStat.WIND_WALK);
                }


                if (player.getStatForBuff(MapleBuffStat.SHADOWPARTNER) != null) {
                        newtotDamageToOneMonster *= 2;
                }else if (attack.skill == 3221007) {
                    if (LtMS.ConfigValuesMap.get("一击要害伤害限制")==1){
                        newtotDamageToOneMonster = DamageParse.固定伤害;
                    }
                    final long 剩余血量 = monster.getHp() - (long)newtotDamageToOneMonster;
                    player.dropTopMsg("伤害技能造成 " + newtotDamageToOneMonster + "伤害 目标剩余HP " + 剩余血量 + "");
                }
                if (newtotDamageToOneMonster <= 0) {
                    continue;
                }

                //伤害加成计算
                newtotDamageToOneMonster =getNewtotDamageToOneMonster(player,newtotDamageToOneMonster, pDamagePercent, pDamagePercentBoss, pDamagePercentNormal, packageDamagePercent, packageDamagePercentBoss, packageDamagePercentNormal, pDamagePercentSkill, monster);

                newtotDamageToOneMonster *= skillDamage;


                List<BreakthroughMechanism> breakthroughMechanisms = Start.breakthroughMechanism.get(player.getId());
                if (ListUtil.isNotEmpty(breakthroughMechanisms)) {
                    newtotDamageToOneMonster +=breakthroughMechanisms.get(0).getHarm()*LtMS.ConfigValuesMap.get("境界伤害");
                }
                if (player.get追加伤害()>0) {
                    newtotDamageToOneMonster += player.get追加伤害();
                }
                //套装伤害计算
                tzjc t = new tzjc();
                realHarm(player, newtotDamageToOneMonster, monster,attack.allDamage.size());
                newtotDamageToOneMonster = 伤害减伤(monster, newtotDamageToOneMonster);
                newtotDamageToOneMonster = t.star_damage(player, newtotDamageToOneMonster, monster);
                newtotDamageToOneMonster = BigDecimal.valueOf((Start.jobDamageMap.get((int)player.getJob()) !=null ? Start.jobDamageMap.get((int)player.getJob()) : 100)/100.0).multiply(BigDecimal.valueOf(newtotDamageToOneMonster)).longValue();
                if (attack.skill != 1221011) {
                   // monster.damage(player, (long)totDamageToOneMonster, true, attack.skill);

                    newtotDamageToOneMonster = newtotDamageToOneMonster + (long)(newtotDamageToOneMonster * (分身伤害加成/100.0));
                    if(LtMS.ConfigValuesMap.get("世界BOSS")== monster.getId()){
                        newtotDamageToOneMonster = 999;
                    }
                    if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害气泡显示")).intValue() > 0 && (System.currentTimeMillis() - player.getQpStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                        player.showInstruction("【真实伤害 → " + NumberUtils.amountConversion(new BigDecimal(newtotDamageToOneMonster)) + "#k】", 240, 10);
                        player.setQpStartTime(System.currentTimeMillis());
                    }
                    if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害黄字喇叭显示")).intValue() > 0 && (System.currentTimeMillis() - player.getQpStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                        player.dropMessage(-1, "【真实伤害 →" + NumberUtils.amountConversion(new BigDecimal(newtotDamageToOneMonster)) + "】");
                        player.setQpStartTime(System.currentTimeMillis());
                    }

                    monster.damage(player, newtotDamageToOneMonster, true, attack.skill);
                } else {
                    monster.damage(player, monster.getStats().isBoss() ? 500000L : (monster.getHp() - 1L), true, attack.skill);
                }
                if (monster.getStats().isBoss() && LtMS.ConfigValuesMap.get("显示BOSS血量") > 0 && (System.currentTimeMillis() - player.getBossStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                    player.showInstruction("【怪物剩余HP → " + (calculateRemainingHealthPercentage(monster.getHp(), monster.getMobMaxHp())) + "#k】", 240, 10);
                    player.setBossStartTime(System.currentTimeMillis());
                }

                //BOSS技能反伤
                if (monster.isBuffed(MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
                    if ((Integer)LtMS.ConfigValuesMap.get("VIP无效BOSS技能开关") > 0 && player.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无效BOSS技能道具ID"))) {
                        player.dropMessage(5, "持有VIP道具，BOSS的伤害反射无法对你生效。");
                    }

                    if ((Integer)LtMS.ConfigValuesMap.get("VIP无敌开关") > 0 && player.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无敌道具ID"))) {
                        player.dropMessage(5, "持有VIP道具，BOSS的伤害反射无法对你生效。");
                    } else {
                        player.addHP(-(7000 + Randomizer.nextInt(8000)));
                    }
                }
                if (stats.hpRecoverProp > 0 && Randomizer.nextInt(100) <= stats.hpRecoverProp) {
                    player.healHP(stats.hpRecover);
                }
                if (stats.mpRecoverProp > 0 && Randomizer.nextInt(100) <= stats.mpRecoverProp) {
                    player.healMP(stats.mpRecover);
                }
                //战神吸血
                if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
                    try {
                        int hp = (int)Math.min(3000, (long)(totDamage * player.getStatForBuff(MapleBuffStat.COMBO_DRAIN).getX() / 100.0));
                        if(hp<0){
                            hp = 30000;
                        }
                        stats.setHp(stats.getHp() + hp, true);
                    } catch (Exception e) {
                        stats.setHp(stats.getHp() + 3000, true);
                    }
                }
//                if (player.getBuffedValue(MapleBuffStat.COMBO_DRAIN) != null) {
//                    stats.setHp(stats.getHp() + (int)Math.min(monster.getMobMaxHp(), (long)Math.min((int)((double)totDamage * (double)player.getStatForBuff(MapleBuffStat.COMBO_DRAIN).getX() / 100.0), stats.getMaxHp() / 2)), true);
//                }
                if(attack.skill  == 14101006){
                    if(totDamage > 0) {stats.setHp(stats.getHp() +(int) Math.min(monster.getMobMaxHp(), Math.min(((int) ((double) totDamage * (double) effect.getX() / 100.0)), stats.getMaxHp() / 2)));}
                }

                final int[] array;
                final int[] skillsl = array = new int[] { 4120005, 4220005, 14110004 };
                final int length = array.length;
                int k = 0;
                while (k < length) {
                    final int i = array[k];
                    final ISkill skill = SkillFactory.getSkill(i);
                    if (player.getSkillLevel(skill) > 0) {
                        final MapleStatEffect venomEffect = skill.getEffect((int)player.getSkillLevel(skill));
                        if (venomEffect.makeChanceResult()) {
                            monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.POISON, Integer.valueOf(1), i, null, false), true, (long)venomEffect.getDuration(), monster.getStats().isBoss(), venomEffect);
                            break;
                        }
                        break;
                    }
                    else {
                        ++k;
                    }
                }
                switch (attack.skill) {
                    case 4101005:
                    case 5111004:
                    case 15111001: {
                        final int getHP = (int)Math.min(monster.getMobMaxHp(), (long)Math.min((int)((double)totDamage * (double)theSkill.getEffect((int)player.getSkillLevel(theSkill)).getX() / 100.0), stats.getMaxHp() / 2));
                        stats.setHp(stats.getHp() + getHP, true);
                        break;
                    }
                    case 5211006:
                    case 5220011: {
                        player.setLinkMid(monster.getObjectId());
                        break;
                    }
                    case 1311005: {
                        final int remainingHP = stats.getHp() - totDamage * ((effect != null) ? effect.getX() : 0) / 100;
                        stats.setHp((remainingHP > 1) ? remainingHP : 1);
                        break;
                    }
                    case 4001002:
                    case 4001334:
                    case 4001344:
                    case 4111005:
                    case 4121007:
                    case 4201005:
                    case 4211002:
                    case 4211004:
                    case 4221001:
                    case 4221007:
                    case 14001002:
                    case 14001004:
                    case 14111002:
                    case 14111005: {
                        if (player.hasBuffedValue(MapleBuffStat.WK_CHARGE) && !monster.getStats().isBoss()) {
                            final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.WK_CHARGE);
                            if (eff != null) {
                                monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, Integer.valueOf(eff.getX()), eff.getSourceId(), null, false), false, (long)(eff.getY() * 1000), monster.getStats().isBoss(), eff);
                            }
                        }
                        if (player.hasBuffedValue(MapleBuffStat.BODY_PRESSURE) && !monster.getStats().isBoss()) {
                            final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BODY_PRESSURE);
                            if (eff != null && eff.makeChanceResult() && !monster.isBuffed(MonsterStatus.NEUTRALISE)) {
                                monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.NEUTRALISE, Integer.valueOf(1), eff.getSourceId(), null, false), false, (long)(eff.getX() * 1000), monster.getStats().isBoss(), eff);
                            }
                        }
                        final int[] array2;
                        final int[] skills = array2 = new int[] { 4120005, 4220005, 14110004 };
                        final int length2 = array2.length;
                        int l = 0;
                        while (l < length2) {
                            final int j = array2[l];
                            final ISkill skill2 = SkillFactory.getSkill(j);
                            if (player.getSkillLevel(skill2) > 0) {
                                final MapleStatEffect venomEffect2 = skill2.getEffect((int)player.getSkillLevel(skill2));
                                if (venomEffect2.makeChanceResult()) {
                                    monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.POISON, Integer.valueOf(1), j, null, false), true, (long)venomEffect2.getDuration(), monster.getStats().isBoss(), venomEffect2);
                                    break;
                                }
                                break;
                            }
                            else {
                                ++l;
                            }
                        }
                        break;
                    }
                    case 4201004: {
                        //飞侠神通术加成
                        monster.handleSteal(player);
                        break;
                    }
                    case 21000002:
                    case 21100001:
                    case 21100002:
                    case 21100004:
                    case 21110002:
                    case 21110003:
                    case 21110004:
                    case 21110006:
                    case 21110007:
                    case 21110008:
                    case 21120002:
                    case 21120005:
                    case 21120006:
                    case 21120009:
                    case 21120010: {
                        if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null && !monster.getStats().isBoss()) {
                            final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.WK_CHARGE);
                            if (eff != null) {
                                monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.SPEED, Integer.valueOf(eff.getX()), eff.getSourceId(), null, false), false, (long)(eff.getY() * 1000), monster.getStats().isBoss(), eff);
                            }
                        }
                        if (player.getBuffedValue(MapleBuffStat.BODY_PRESSURE) == null || monster.getStats().isBoss()) {
                            break;
                        }
                        final MapleStatEffect eff = player.getStatForBuff(MapleBuffStat.BODY_PRESSURE);
                        if (eff != null && eff.makeChanceResult() && !monster.isBuffed(MonsterStatus.NEUTRALISE)) {
                            monster.applyStatus(player, new MonsterStatusEffect(MonsterStatus.NEUTRALISE, Integer.valueOf(1), eff.getSourceId(), null, false), false, (long)(eff.getX() * 1000), true, eff);
                            break;
                        }
                        break;
                    }
                }
                if (newtotDamageToOneMonster > 0) {
                    final IItem weapon_ = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short)(-11));
                    if (weapon_ != null) {
                        final MonsterStatus stat = GameConstants.getStatFromWeapon(weapon_.getItemId());
                        if (stat != null && Randomizer.nextInt(100) < GameConstants.getStatChance()) {
                            final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(stat, Integer.valueOf(GameConstants.getXForStat(stat)), GameConstants.getSkillForStat(stat), null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, 10000L, monster.getStats().isBoss(), null);
                        }
                    }
                    if (player.hasBuffedValue(MapleBuffStat.BLIND)) {
                        final MapleStatEffect eff2 = player.getStatForBuff(MapleBuffStat.BLIND);
                        if (eff2 != null && eff2.makeChanceResult()) {
                            final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.ACC, Integer.valueOf(eff2.getX()), eff2.getSourceId(), null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, (long)(eff2.getY() * 1000), monster.getStats().isBoss(), eff2);
                        }
                    }
                    if (player.hasBuffedValue(MapleBuffStat.HAMSTRING)) {
                        final MapleStatEffect eff2 = player.getStatForBuff(MapleBuffStat.HAMSTRING);
                        if (eff2 != null && eff2.makeChanceResult()) {
                            final MonsterStatusEffect monsterStatusEffect = new MonsterStatusEffect(MonsterStatus.SPEED, Integer.valueOf(eff2.getX()), 3121007, null, false);
                            monster.applyStatus(player, monsterStatusEffect, false, (long)(eff2.getY() * 1000), monster.getStats().isBoss(), eff2);
                        }
                    }
                    if (player.getJob() == 121 || player.getJob() == 122) {
                        ISkill skill3 = SkillFactory.getSkill(1211006);
                        if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill3)) {
                            final MapleStatEffect eff3 = skill3.getEffect((int)player.getSkillLevel(skill3));
                            final MonsterStatusEffect monsterStatusEffect2 = new MonsterStatusEffect(MonsterStatus.FREEZE, Integer.valueOf(1), skill3.getId(), null, false);
                            monster.applyStatus(player, monsterStatusEffect2, false, (long)(eff3.getY() * 2000), monster.getStats().isBoss(), eff3);
                        }
                        skill3 = SkillFactory.getSkill(1211005);
                        if (player.isBuffFrom(MapleBuffStat.WK_CHARGE, skill3)) {
                            final MapleStatEffect eff3 = skill3.getEffect((int)player.getSkillLevel(skill3));
                            final MonsterStatusEffect monsterStatusEffect2 = new MonsterStatusEffect(MonsterStatus.FREEZE, Integer.valueOf(1), skill3.getId(), null, false);
                            monster.applyStatus(player, monsterStatusEffect2, false, (long)(eff3.getY() * 2000), monster.getStats().isBoss(), eff3);
                        }
                    }
                }
                if (effect == null || effect.getMonsterStati().size() <= 0 || !effect.makeChanceResult()) {
                    continue;
                }
                for (final Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                    monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus)z.getKey(), Integer.valueOf(z.getValue()), theSkill.getId(), null, false), effect.isPoison(), (long)effect.getDuration(), monster.getStats().isBoss(), effect);
                }
            }
        }
        if (effect != null && attack.skill != 0 && (attack.targets > 0 || (attack.skill != 4331003 && attack.skill != 4341002)) && attack.skill != 21101003 && attack.skill != 5110001 && attack.skill != 15100004 && attack.skill != 11101002 && attack.skill != 13101002 && attack.skill != 14111006) {
            effect.applyTo(player, attack.position);
        }
        //pvp入口
        if ((Integer)LtMS.ConfigValuesMap.get("PK总开关") > 0) {
            if (GameConstants.isPKChannel(player.getClient().getChannel())) {
                MaplePvp.doPvP(player, map, attack);
            } else if (GameConstants.isPKGuildChannel(player.getClient().getChannel())) {
                MaplePvp.doPvP(player, map, attack);
            } else if (GameConstants.isPKPlayerMap(player.getMapId())) {
                MaplePvp.doPvP(player, map, attack);
            } else if (GameConstants.isPKPartyMap(player.getMapId())) {
                MaplePvp.doPvP(player, map, attack);
            } else if (GameConstants.isPKGuildMap(player.getMapId())) {
                MaplePvp.doPvP(player, map, attack);
            }
        }
        //毒炸弹
        if (attack.skill == 14111006) {
            effect.applyTo(player, attack.positionxy);
        }
        if (totDamage > 1) {
            final CheatTracker tracker = player.getCheatTracker();
            tracker.setAttacksWithoutHit(true);
            if (tracker.getAttacksWithoutHit() > 50) {
                tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
            }
        }
    }
    public static String calculateRemainingHealthPercentage(long mobHp,long maxHp) {
        if (maxHp <= 0 || mobHp <= 0) {
            return "0%";
        }
        return ((double) mobHp / maxHp) * 100 + "%"; // 将 mobHp 转换为 double
    }
    private static long getNewtotDamageToOneMonster(MapleCharacter player,long newtotDamageToOneMonster, int pDamagePercent, int pDamagePercentBoss, int pDamagePercentNormal, int packageDamagePercent, int packageDamagePercentBoss, int packageDamagePercentNormal, int pDamagePercentSkill, MapleMonster monster) {
        if (pDamagePercent > 0) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) pDamagePercent / 100.0));
        }

        if (pDamagePercentBoss > 0  && monster.getStats().isBoss()) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) pDamagePercentBoss / 100.0));
        }

        if (pDamagePercentNormal > 0  && !monster.getStats().isBoss()) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) pDamagePercentNormal / 100.0));
        }

        if (pDamagePercentSkill > 0) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) pDamagePercentSkill / 100.0));
        }

        if (packageDamagePercent > 0) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) packageDamagePercent / 100.0));
        }

        if (packageDamagePercentBoss > 0  && monster.getStats().isBoss()) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) packageDamagePercentBoss / 100.0));
        }

        if (packageDamagePercentNormal > 0 && !monster.getStats().isBoss()) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) packageDamagePercentNormal / 100.0));
        }
        if (packageDamagePercentNormal > 0 && !monster.getStats().isBoss()) {
            newtotDamageToOneMonster += (long)(newtotDamageToOneMonster * ((double) packageDamagePercentNormal / 100.0));
        }
        return newtotDamageToOneMonster;
    }
    /**
     * 伤害附加计算
     */
    public static void 计算伤害(MapleCharacter applyfrom, MapleStatEffect effect, ASkill aSkill, AttackInfo attack) {
        Rectangle bounds = MapleStatEffect.calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft(), new Point(-400, -400), new Point(400, 400), 800);
        if (attack.allDamage ==null || attack.allDamage.size()==0 ){
            //System.out.println("无技能伤害");
            return ;
        }
        int attackCount = 0;
        int mobCount = 0;
        if(aSkill.getAttackCount()>0 && attack.allDamage.size()>0 && attack.allDamage.get(0).getAttack().size()>0){
            attackCount = aSkill.getAttackCount();
            attack.setHits((byte) attackCount);
        }
        if (aSkill.getMobCount()>0){
            mobCount = aSkill.getMobCount();
        }
//        System.out.println("开始计算1"+mobCount);
        if (attackCount>0){
            for (AttackPair attackPair : attack.allDamage) {
                List<Pair<Integer, Boolean>> attackPairAttack = attackPair.getAttack();
                if (!attackPairAttack.isEmpty()){
                    for (int i = 0; i < attackCount; i++) {
                        Integer left = attackPairAttack.get(0).left;
                        Boolean right = attackPairAttack.get(0).right;
                        attackPairAttack.add(new Pair<Integer, Boolean>(left,right));
                    }
                    attackPair.setAttack(attackPairAttack);
                }
            }
            //添加显示段数
            attack.setHits((byte) (attackCount+attack.getHits()));
            if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害气泡显示")).intValue() > 0 && (System.currentTimeMillis() - applyfrom.getQpStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                applyfrom.showInstruction("【词条额外段数 → " + attackCount + "#k】", 240, 10);
            }
            if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害黄字喇叭显示")).intValue() > 0 && (System.currentTimeMillis() - applyfrom.getQpStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                applyfrom.dropMessage(-1, "【词条额外段数 →" + attackCount + "】");
            }
        }
//        System.out.println("开始计算2"+attackCount);
        if (mobCount>0) {
            //设置对象
            MapleMist mist = new MapleMist(bounds, applyfrom, effect);
            try {
                int count = 0;
                List<MapleMapObject> mapObjectsInRect = applyfrom.getClient().getPlayer().getMap().getMapObjectsInRect(mist.getBox(), Collections.singletonList(MapleMapObjectType.MONSTER));
                if (mapObjectsInRect != null && mapObjectsInRect.size() > 0  && attack.allDamage.size() < mapObjectsInRect.size()) {
                    for (MapleMapObject mapleMapObject : mapObjectsInRect) {
                        if (attack.allDamage.stream().noneMatch(attackPair -> attackPair.getObjectid() == mapleMapObject.getObjectId())) {
                            if (mobCount <= count) {
                                break;
                            }
                            final AttackPair attackPair = attack.allDamage.get(0);
                            final List<Pair<Integer, Boolean>> attackPairAttack = attackPair.getAttack();
                            attack.allDamage.add(new AttackPair(mapleMapObject.getObjectId(), attackPairAttack));
                            count++;
                        }
                    }
                }
                if (count>0){
                    attack.setTargets((byte) (count+attack.getTargets()));
                }
//                System.out.println("开始计算3"+count);
            } catch (Exception e) {
            }
        }
    }
    public static void applyAttackMagic( AttackInfo attack, final ISkill theSkill, final MapleCharacter player, final MapleStatEffect effect) {
        if (!player.isAlive()) {
            player.getCheatTracker().registerOffense(CheatingOffense.ATTACKING_WHILE_DEAD);
            return;
        }
        if (attack.real) {
           int ii =  player.getCheatTracker().checkAttack(attack.skill, attack.lastAttackTickCount);
            if (ii==1){
                return;
            }
        }
        final int last = (effect.getAttackCount() > effect.getBulletCount()) ? effect.getAttackCount() : effect.getBulletCount();
        if (attack.hits > last && player.hasGmLevel(1) && LtMS.ConfigValuesMap.get("检测段数")>0) {
            //player.dropMessage("攻击次数异常攻击次数 " + (int)attack.hits + " 服务端判断正常攻击次数 " + last + " 技能ID " + attack.skill);
            System.out.println(player.getName() + "攻击次数异常攻击次数 " + (int)attack.hits + " 服务端判断正常攻击次数 " + last + " 技能ID " + attack.skill);
            player.getClient().disconnect(true, false);
            player.getClient().getSession().close();
            return;
        }
//        final int CheckCount = effect.getMobCount();
        if (attack.hits > 0 && attack.targets > 0 && !player.getStat().checkEquipDurabilitys(player, -1)) {
            player.dropMessage(5, "一件物品的耐用性不足，但没有库存空间可供使用。");
            return;
        }
        if (GameConstants.isMulungSkill(attack.skill)) {
            if (player.getMapId() / 10000 != 92502) {
                return;
            }
            player.mulungEnergyModify(false);
        }
        if (GameConstants.isPyramidSkill(attack.skill)) {
            if (player.getMapId() / 1000000 != 926) {
                return;
            }
            if (player.getPyramidSubway() == null || !player.getPyramidSubway().onSkillUse(player)) {
                return;
            }
        }
        final PlayerStats stats = player.getStat();
        double maxDamagePerHit;
        if (attack.skill == 1000 || attack.skill == 10001000 || attack.skill == 20001000 || attack.skill == 20011000 || attack.skill == 30001000) {
            maxDamagePerHit = 40.0;
        }
        else if (GameConstants.isPyramidSkill(attack.skill)) {
            maxDamagePerHit = 1.0;
        }
        else {
            final double v75 = (double)effect.getMatk() * 0.058;
            maxDamagePerHit = (double)stats.getTotalMagic() * ((double)stats.getInt() * 0.5 + v75 * v75 + (double)effect.getMatk() * 3.3) / 100.0;
        }
        maxDamagePerHit *= 1.04;
        final Element element = (player.getBuffedValue(MapleBuffStat.ELEMENT_RESET) != null) ? Element.NEUTRAL : theSkill.getElement();
        long totDamage = 0;
        //final int CriticalDamage = stats.passive_sharpeye_percent();
        final ISkill eaterSkill = SkillFactory.getSkill(GameConstants.getMPEaterForJob((int)player.getJob()));
        final int eaterLevel = player.getSkillLevel(eaterSkill);
        final MapleMap map = player.getMap();
        int 附加伤害总和 = 0;
        int pDamagePercent = player.getPotential(29);
        int pDamagePercentBoss = player.getPotential(30);
        int pDamagePercentNormal = player.getPotential(31);
        HideAttribute hideAttribute = Start.hideAttributeMap.get(player.getId());

        int packageDamagePercent = player.package_total_damage_percent + hideAttribute.getTotal_total_damage_percent()+(Start.masterApprenticeGain.get(player.getId()) !=null ? Start.masterApprenticeGain.get(player.getId()) : 0);
        int packageDamagePercentBoss = player.package_boss_damage_percent + hideAttribute.getTotal_boss_damage_percent();
        int packageDamagePercentNormal = player.package_normal_damage_percent + hideAttribute.getTotal_normal_damage_percent();
        double skillDamage = 1.0;
        if(Objects.nonNull(hideAttribute.getSkillDamage()) && hideAttribute.getSkillDamage().size()>0 && Objects.nonNull(hideAttribute.getSkillDamage().get(effect.getSourceId()))){
             skillDamage = hideAttribute.getSkillDamage().get(effect.getSourceId());
        }
        int pDamagePercentSkill = 0;
        if (Potential.isDamageSkill(attack.skill)) {
            pDamagePercentSkill = player.getPotential(attack.skill);
        }
        double 分身伤害加成 = 0.0;
        try {
            WeakReference<MapleCharacter>[] clones = player.getClones();
            if (clones != null && clones.length > 0) {
                for (WeakReference<MapleCharacter> mapleCharacterWeakReference : clones) {
                    MapleCharacter clone = mapleCharacterWeakReference.get();
                    if (clone != null) {
                        分身伤害加成 = 分身伤害加成 + clone.getCloneDamagePercentage();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("魔法克隆伤害加成计算错误");
        }
//特效攻击,如:致命一击 2倍伤害 3倍伤害 4倍伤害 5倍伤害
        if (ListUtil.isNotEmpty(player.LtDiabloEquipmentsList)) {
            try {
                for (LtDiabloEquipments ltDiabloEquipments : player.LtDiabloEquipmentsList) {
                    int rom = RandomUtils.nextInt(1000000);
                    if (theSkill.getId() == 5221004 || theSkill.getId() == 13111000 || theSkill.getId() == 3121004) {
                        rom = RandomUtils.nextInt(4000000);
                    }
                    if (rom > ltDiabloEquipments.getSkillDs() && rom < ltDiabloEquipments.getSkillSl()) {
                        分身伤害加成 += ltDiabloEquipments.getSkillDamage();
                        if (分身伤害加成 > LtMS.ConfigValuesMap.get("倍率伤害加成上限")) {
                            分身伤害加成 = LtMS.ConfigValuesMap.get("倍率伤害加成上限");
                        }
                        if (StringUtils.isNotEmpty(ltDiabloEquipments.getSkillTx())) {
                            //client.sendPacket(UIPacket.ShowWZEffect("Effect/ItemEff.img/1102865/effect/default"));
                            player.getClient().sendPacket(UIPacket.AranTutInstructionalBalloon(ltDiabloEquipments.getSkillTx()));
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        if (ListUtil.isNotEmpty(player.shieldEnhancementEquipmentsList)) {
            try {
                boolean flg = false;
                String skillTx = "";
                for (LtDiabloEquipments ltDiabloEquipments : player.shieldEnhancementEquipmentsList) {
                    int rom = RandomUtils.nextInt(1000000);
                    if (theSkill.getId() == 5221004 || theSkill.getId() == 13111000 || theSkill.getId() == 3121004) {
                        rom = RandomUtils.nextInt(4000000);
                    }
                    if (rom > ltDiabloEquipments.getSkillDs() && rom < ltDiabloEquipments.getSkillSl()) {
                        if (StringUtils.isNotEmpty(ltDiabloEquipments.getSkillTx())) {
                            player.getStat().setHphd(player.getStat().getHphd() + ltDiabloEquipments.getSkillDamage());
                            flg = true;
                            skillTx = ltDiabloEquipments.getSkillTx();
                        }
                    }
                }
                if (flg && StringUtils.isNotEmpty(skillTx)) {
                    player.getClient().sendPacket(UIPacket.AranTutInstructionalBalloon(skillTx));
                }
            } catch (Exception e) {
            }
        }
        for (final AttackPair oned : attack.allDamage) {
            final MapleMonster monster = map.getMonsterByOid(oned.objectid);
            if (monster != null) {
                final boolean Tempest = monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006 && !monster.getStats().isBoss();
                long totDamageToOneMonster = 0;
                long newtotDamageToOneMonster = 0L;
                final MapleMonsterStats monsterstats = monster.getStats();
                final int fixeddmg = monsterstats.getFixedDamage();
               // MaxDamagePerHit = calculateMaxMagicDamagePerHit(player, theSkill, monster, monsterstats, stats, element, Integer.valueOf(CriticalDamage), maxDamagePerHit);
               // byte overallAttackCount = 0;
                for (final Pair<Integer, Boolean> eachde : oned.attack) {
                    Integer eachd = Integer.valueOf(eachde.left);
                   // ++overallAttackCount;
                    if (!GameConstants.isElseSkill(attack.skill)) {
                        if (GameConstants.Novice_Skill(attack.skill)) {}
                        int atk = 500000;
                        if (!GameConstants.isAran((int)player.getJob()) && player.getLevel() > 10) {
                            boolean ban = false;
                            if (player.getLevel() <= 20) {
                                atk = 1000;
                            }
                            else if (player.getLevel() <= 30) {
                                atk = 2500;
                            }
                            else if (player.getLevel() <= 60) {
                                atk = 8000;
                            }
                            if (attack.skill == 1001004 || attack.skill == 11001002 || attack.skill == 5111002 || attack.skill == 15101005) {
                                atk *= 2;
                            }
                            if ((int)eachd >= atk && (double)(int)eachd > Math.ceil(maxDamagePerHit * 1.2)) {
                                ban = true;
                            }
                            if ((long)(int)eachd == monster.getMobMaxHp()) {
                                ban = false;
                            }
                            if (player.hasGmLevel(1)) {
                                ban = false;
                            }
                        }
                    }
                    if (fixeddmg != -1) {
                        eachd = Integer.valueOf(monsterstats.getOnlyNoramlAttack() ? 0 : fixeddmg);
                    }
                    else if (monsterstats.getOnlyNoramlAttack()) {
                        eachd = Integer.valueOf(0);
                    }
//                    if(effect!=null && player.getUserASkill()!=null && player.getUserASkill().size()>0 && player.getUserASkill().get(effect.getSourceId())!=null) {
//                        eachd = (int)(eachd * player.getUserASkill().get(effect.getSourceId()).getDamage());
//                    }
                    if(eachd < 0 ){
                        eachd = Integer.MAX_VALUE;
                    }
                    totDamageToOneMonster += (int)eachd;
                    if (eachd > (player.读取伤害上限值() >Integer.MAX_VALUE? Integer.MAX_VALUE : player.读取伤害上限值())&& LtMS.ConfigValuesMap.get("破总伤") ==0) {
                        eachd = Math.toIntExact((player.读取伤害上限值() > Integer.MAX_VALUE ? Integer.MAX_VALUE : player.读取伤害上限值()));
                    }
                    newtotDamageToOneMonster += (long)eachd;
                }
//                if(effect!=null && ( checkDamage(player, effect.getDamage(), effect.getAttackCount())*LtMS.ConfigValuesMap.get("封号标准倍率")) < newtotDamageToOneMonster){
//                    //封号
//                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + player.getName() + " 伤害检测异常，恭喜他成就了封号斗罗。"));
//                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + player.getName() + " 伤害检测异常，恭喜他成就了封号斗罗。"));
//                    World.Broadcast.broadcastMessage(MaplePacketCreator.serverNotice(6, "[封锁密语] " + player.getName() + " 伤害检测异常，恭喜他成就了封号斗罗。"));
//                    player.ban("倍攻开挂", true, true, true);
//                }
                //增加的段数伤害
                //统计段数  技能增加段数  装备增加段数
                if (newtotDamageToOneMonster > player.读取伤害上限值() && LtMS.ConfigValuesMap.get("破总伤") >0) {
                    newtotDamageToOneMonster = player.读取伤害上限值();
                }
               totDamage += totDamageToOneMonster;
                player.checkMonsterAggro(monster);
                final double range = player.getPosition().distanceSq((Point2D)monster.getPosition());
                final double SkillRange = GameConstants.getAttackRange(player, effect, attack);
                if(LtMS.ConfigValuesMap.get("启用吸怪") ==0) {
                    if (player.getDebugMessage() && range > SkillRange) {
                        player.dropMessage("技能[" + attack.skill + "] 预计范围: " + (int) SkillRange + " 实际范围: " + (int) range);
                    }
                    if (range > SkillRange && !player.inBossMap()) {
                        player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER, "攻击范围异常,技能:" + attack.skill + "(" + SkillFactory.getName(attack.skill) + ")\u3000正常范围:" + (int) SkillRange + " 計算范围:" + (int) range);
                        if (range > SkillRange * 2.0) {
                            player.getCheatTracker().registerOffense(CheatingOffense.ATTACK_FARAWAY_MONSTER_BAN, "超大攻击范围,技能:" + attack.skill + "(" + SkillFactory.getName(attack.skill) + ")\u3000怪物:" + monster.getId() + " 正常范围:" + (int) SkillRange + " 計算范围:" + (int) range);
                        }
                        return;
                    }
                }
                if (player.getStatForBuff(MapleBuffStat.SHADOWPARTNER) != null) {
                    if ((player.getJob() >= 410 && player.getJob() <= 413) || (player.getJob() >= 1410 && player.getJob() <= 1413)) {
                        if (player.getItemQuantity(3994720, false) > 0) {
                            newtotDamageToOneMonster *= 2;
                            final long 剩余血量 = monster.getHp() - (long)newtotDamageToOneMonster;
                            //player.dropTopMsg("[影分身] 实际造成" + newtotDamageToOneMonster + "伤害 目标剩余HP " + 剩余血量 + "");
                        }
                    }
                    else {
                        newtotDamageToOneMonster *= 2;
                        final long 剩余血量 = monster.getHp() - (long)newtotDamageToOneMonster;
                        //player.dropTopMsg("[影分身] 实际造成" + newtotDamageToOneMonster + "伤害 目标剩余HP " + 剩余血量 + "");
                    }
                }
                if (newtotDamageToOneMonster <= 0) {
                    continue;
                }
                //伤害加成计算
                newtotDamageToOneMonster = getNewtotDamageToOneMonster(player,newtotDamageToOneMonster, pDamagePercent, pDamagePercentBoss, pDamagePercentNormal, packageDamagePercent, packageDamagePercentBoss, packageDamagePercentNormal, pDamagePercentSkill, monster);
                newtotDamageToOneMonster *= skillDamage;
                List<BreakthroughMechanism> breakthroughMechanisms = Start.breakthroughMechanism.get(player.getId());
                if (ListUtil.isNotEmpty(breakthroughMechanisms)) {
                    newtotDamageToOneMonster +=breakthroughMechanisms.get(0).getHarm()*LtMS.ConfigValuesMap.get("境界伤害");
                }
                if (player.get追加伤害()>0) {
                    newtotDamageToOneMonster += player.get追加伤害();
                }
                tzjc t = new tzjc();
                long newDamage = 0L;
                realHarm(player, newtotDamageToOneMonster, monster,attack.allDamage.size());
                newtotDamageToOneMonster += newDamage;
                newtotDamageToOneMonster = 伤害减伤(monster, newtotDamageToOneMonster);
                newtotDamageToOneMonster = t.star_damage(player, newtotDamageToOneMonster, monster);
                newtotDamageToOneMonster = BigDecimal.valueOf((Start.jobDamageMap.get((int)player.getJob()) !=null ? Start.jobDamageMap.get((int)player.getJob()) : 100 )/100.0).multiply(BigDecimal.valueOf(newtotDamageToOneMonster)).longValue();


                newtotDamageToOneMonster = newtotDamageToOneMonster + (long)(newtotDamageToOneMonster * (分身伤害加成/100.0));
                if(LtMS.ConfigValuesMap.get("世界BOSS")== monster.getId()){
                    newtotDamageToOneMonster = 999;
                }
                if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害气泡显示")).intValue() > 0 && (System.currentTimeMillis() - player.getQpStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                    player.showInstruction("【真实伤害 → " + NumberUtils.amountConversion(new BigDecimal(newtotDamageToOneMonster)) + "#k】", 240, 10);
                    player.setQpStartTime(System.currentTimeMillis());
                }
                if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害黄字喇叭显示")).intValue() > 0 && (System.currentTimeMillis() - player.getQpStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                    player.dropMessage(-1, "【真实伤害 →" + NumberUtils.amountConversion(new BigDecimal(newtotDamageToOneMonster)) + "】");
                    player.setQpStartTime(System.currentTimeMillis());
                }

                monster.damage(player, newtotDamageToOneMonster, true, attack.skill);

                if (monster.getStats().isBoss() && LtMS.ConfigValuesMap.get("显示BOSS血量") > 0 && (System.currentTimeMillis() - player.getBossStartTime())>LtMS.ConfigValuesMap.get("伤害气泡显示时间")) {
                    player.showInstruction("【怪物剩余HP → " + (calculateRemainingHealthPercentage(monster.getHp(), monster.getMobMaxHp())) + "#k】", 240, 10);
                    player.setBossStartTime(System.currentTimeMillis());
                }

                if (monster.isBuffed(MonsterStatus.MAGIC_DAMAGE_REFLECT)) {
                    if ((Integer)LtMS.ConfigValuesMap.get("VIP无效BOSS技能开关") > 0 && player.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无效BOSS技能道具ID"))) {
                        player.dropMessage(5, "持有VIP道具，BOSS的伤害反射无法对你生效。");
                    } else if ((Integer)LtMS.ConfigValuesMap.get("VIP无敌开关") > 0 && player.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无敌道具ID"))) {
                        player.dropMessage(5, "持有VIP道具，BOSS的伤害反射无法对你生效。");
                    } else {
                        player.addHP(-(7000 + Randomizer.nextInt(8000)));
                    }
                }
                switch (attack.skill) {
                    case 2221003: {
                        monster.setTempEffectiveness(Element.FIRE, (long)theSkill.getEffect((int)player.getSkillLevel(theSkill)).getDuration());
                        break;
                    }
                    case 2121003: {
                        monster.setTempEffectiveness(Element.ICE, (long)theSkill.getEffect((int)player.getSkillLevel(theSkill)).getDuration());
                        break;
                    }
                }
                if (effect.getMonsterStati().size() >= 0 && effect.makeChanceResult()) {
                    for (final Entry<MonsterStatus, Integer> z : effect.getMonsterStati().entrySet()) {
                        monster.applyStatus(player, new MonsterStatusEffect((MonsterStatus)z.getKey(), Integer.valueOf(z.getValue()), theSkill.getId(), null, false), effect.isPoison(), (long)effect.getDuration(), monster.getStats().isBoss(), effect);
                    }
                }
                if (eaterLevel <= 0) {
                    continue;
                }
                eaterSkill.getEffect(eaterLevel).applyPassive(player, (MapleMapObject)monster);
            }
        }
        if (attack.skill != 2301002) {
            effect.applyTo(player);
        }
        if (totDamage > 1) {
            final CheatTracker tracker = player.getCheatTracker();
            tracker.setAttacksWithoutHit(true);
            if (tracker.getAttacksWithoutHit() > 1000) {
                tracker.registerOffense(CheatingOffense.ATTACK_WITHOUT_GETTING_HIT, Integer.toString(tracker.getAttacksWithoutHit()));
            }
        }
    }

    private static double calculateMaxMagicDamagePerHit(MapleCharacter chr, final ISkill skill, final MapleMonster monster, final MapleMonsterStats mobstats, final PlayerStats stats, final Element elem, final Integer sharpEye, final double maxDamagePerMonster) {
        final int dLevel = Math.max(mobstats.getLevel() - chr.getLevel(), 0);
        final int Accuracy = (int)(Math.floor((double)stats.getTotalInt() / 10.0) + Math.floor((double)stats.getTotalLuk() / 10.0));
        final int MinAccuracy = mobstats.getEva() * (dLevel * 2 + 51) / 120;
        if (MinAccuracy > Accuracy && skill.getId() != 1000 && skill.getId() != 10001000 && skill.getId() != 20001000 && skill.getId() != 20011000 && skill.getId() != 30001000 && !GameConstants.isPyramidSkill(skill.getId())) {
            return 0.0;
        }
        double elemMaxDamagePerMob = 0.0;
        switch (monster.getEffectiveness(elem)) {
            case IMMUNE: {
                elemMaxDamagePerMob = 1.0;
                break;
            }
            case NORMAL: {
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster, stats);
                break;
            }
            case WEAK: {
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * 1.5, stats);
                break;
            }
            case STRONG: {
                elemMaxDamagePerMob = ElementalStaffAttackBonus(elem, maxDamagePerMonster * 0.5, stats);
                break;
            }
            default: {
                throw new RuntimeException("Unknown enum constant");
            }
        }
        elemMaxDamagePerMob -= (double)mobstats.getMagicDefense() * 0.5;
        elemMaxDamagePerMob += elemMaxDamagePerMob / 100.0 * (double)(int)sharpEye;
        if (skill.getId() == 21120006) {
            elemMaxDamagePerMob *= 15.0;
        }
        if (skill.getId() == 2211006) {
            elemMaxDamagePerMob *= 2.0;
        }
        elemMaxDamagePerMob += elemMaxDamagePerMob * (mobstats.isBoss() ? stats.bossdam_r : stats.dam_r) / 100.0;
        switch (skill.getId()) {
            case 1000:
            case 10001000:
            case 20001000: {
                elemMaxDamagePerMob = 40.0;
                break;
            }
            case 1020:
            case 10001020:
            case 20001020: {
                elemMaxDamagePerMob = 1.0;
                break;
            }
        }
        if (elemMaxDamagePerMob > 500000.0) {
            elemMaxDamagePerMob = 500000.0;
        }
        else if (elemMaxDamagePerMob < 0.0) {
            elemMaxDamagePerMob = 1.0;
        }
        return elemMaxDamagePerMob;
    }
    
    private static final double ElementalStaffAttackBonus(final Element elem, final double elemMaxDamagePerMob, final PlayerStats stats) {
        switch (elem) {
            case FIRE: {
                return elemMaxDamagePerMob / 100.0 * (double)stats.element_fire;
            }
            case ICE: {
                return elemMaxDamagePerMob / 100.0 * (double)stats.element_ice;
            }
            case LIGHTING: {
                return elemMaxDamagePerMob / 100.0 * (double)stats.element_light;
            }
            case POISON: {
                return elemMaxDamagePerMob / 100.0 * (double)stats.element_psn;
            }
            default: {
                return elemMaxDamagePerMob / 100.0 * (double)stats.def;
            }
        }
    }

    //标飞偷窃金币技能效果
    private static void handlePickPocket(final MapleCharacter player, final MapleMonster mob, final AttackPair oned) {
        final int maxmeso = (int)player.getBuffedValue(MapleBuffStat.PICKPOCKET);
        final ISkill skill = SkillFactory.getSkill(4211003);
        final MapleStatEffect s = skill.getEffect((int)player.getSkillLevel(skill));
        for (final Pair eachde : oned.attack) {
            final Integer eachd = (Integer)eachde.left;
            if (s.makeChanceResult()) {
                player.getMap().spawnMesoDrop(Math.min((int)Math.max((double)(int)eachd / 20000.0 * (double)maxmeso, 1.0), maxmeso), new Point((int)(mob.getTruePosition().getX() + (double)Randomizer.nextInt(100) - 50.0), (int)mob.getTruePosition().getY()), (MapleMapObject)mob, player, false, (byte)0);
            }
        }
    }
    
    private static double calculateMaxWeaponDamagePerHit(final MapleCharacter player, final MapleMonster monster, final AttackInfo attack, final ISkill theSkill, final MapleStatEffect attackEffect, double maximumDamageToMonster, final Integer CriticalDamagePercent) {
        if (player.getMapId() / 1000000 == 914) {
            return 5000000.00;
        }
        final List<Element> elements = new ArrayList<Element>();
        boolean defined = false;
        if (theSkill != null) {
            elements.add(theSkill.getElement());
            if (monster.getStatusSourceID(MonsterStatus.FREEZE) == 21120006) {
                defined = true;
            }
            switch (theSkill.getId()) {
                case 3001004:
                case 3221001: {
                    defined = true;
                    break;
                }
                case 1000:
                case 10001000:
                case 20001000:
                case 20011000:
                case 30001000: {
                    maximumDamageToMonster = 40.0;
                    defined = true;
                    break;
                }
                case 1020:
                case 10001020:
                case 20001020:
                case 20011020:
                case 30001020: {
                    maximumDamageToMonster = 1.0;
                    defined = true;
                    break;
                }
                case 3221007: {
                    maximumDamageToMonster = (double)(monster.getStats().isBoss() ? 500000L : monster.getMobMaxHp());
                    defined = true;
                    break;
                }
                case 1221011: {
                    maximumDamageToMonster = (double)(monster.getStats().isBoss() ? 500000L : (monster.getHp() - 1L));
                    defined = true;
                    break;
                }
                case 4211006: {
                    maximumDamageToMonster = (double)(monster.getStats().isBoss() ? 500000L : monster.getMobMaxHp());
                    defined = true;
                    break;
                }
                case 1009:
                case 10001009:
                case 20001009:
                case 20011009:
                case 30001009: {
                    defined = true;
                    maximumDamageToMonster = (double)(monster.getStats().isBoss() ? (monster.getMobMaxHp() / 30L * 100L) : monster.getMobMaxHp());
                    break;
                }
                case 3211006: {
                    if (monster.getStatusSourceID(MonsterStatus.FREEZE) == 3211003) {
                        defined = true;
                        maximumDamageToMonster = (double)monster.getHp();
                        break;
                    }
                    break;
                }
                case 5121007: {
                    maximumDamageToMonster *= 2.8;
                    break;
                }
                case 1111008:
                case 1121006:
                case 1311001:
                case 1311006:
                case 4201005: {
                    maximumDamageToMonster *= 3.0;
                    break;
                }
                case 1001004:
                case 1121008:
                case 4221001: {
                    maximumDamageToMonster *= 2.5;
                    break;
                }
                case 1001005:
                case 1311004:
                case 3121004: {
                    maximumDamageToMonster *= 2.0;
                    break;
                }
            }
        }
        if (MapleJob.is狂狼勇士((int)player.getJob())) {
            maximumDamageToMonster *= 2.0;
        }
        else if (MapleJob.is拳霸((int)player.getJob())) {
            maximumDamageToMonster *= 1.1;
        }
        if (player.getBuffedValue(MapleBuffStat.WK_CHARGE) != null) {
            final int chargeSkillId = player.getBuffSource(MapleBuffStat.WK_CHARGE);
            switch (chargeSkillId) {
                case 1211003:
                case 1211004: {
                    elements.add(Element.FIRE);
                    break;
                }
                case 1211005:
                case 1211006:
                case 21111005: {
                    elements.add(Element.ICE);
                    break;
                }
                case 1211007:
                case 1211008:
                case 15101006: {
                    elements.add(Element.LIGHTING);
                    break;
                }
                case 1221003:
                case 1221004:
                case 11111007: {
                    elements.add(Element.HOLY);
                    break;
                }
                case 12101005: {
                    elements.clear();
                    break;
                }
            }
        }
        if (player.getBuffedValue(MapleBuffStat.LIGHTNING_CHARGE) != null) {
            elements.add(Element.LIGHTING);
        }
        double elementalMaxDamagePerMonster = maximumDamageToMonster;
        if (elements.size() > 0) {
            double elementalEffect = 0.0;
            switch (attack.skill) {
                case 3111003:
                case 3211003: {
                    elementalEffect = (double)attackEffect.getX() / 200.0;
                    break;
                }
                default: {
                    elementalEffect = 0.5;
                    break;
                }
            }
            for (final Element element : elements) {
                switch (monster.getEffectiveness(element)) {
                    case IMMUNE: {
                        elementalMaxDamagePerMonster = 1.0;
                        continue;
                    }
                    case WEAK: {
                        elementalMaxDamagePerMonster *= 1.0 + elementalEffect;
                        continue;
                    }
                    case STRONG: {
                        elementalMaxDamagePerMonster *= 1.0 - elementalEffect;
                        continue;
                    }
                }
            }
        }
        final short moblevel = monster.getStats().getLevel();
        final short d = (short)((moblevel > player.getLevel()) ? ((short)(moblevel - player.getLevel())) : 0);//元素最大伤害基数计算
        elementalMaxDamagePerMonster = elementalMaxDamagePerMonster * (1.0 - 0.01 * (double)d) - (double)monster.getStats().getPhysicalDefense() * 0.5;
        elementalMaxDamagePerMonster += elementalMaxDamagePerMonster / 100.0 * (double)(int)CriticalDamagePercent;
        if (theSkill != null && theSkill.isChargeSkill() && player.getKeyDownSkill_Time() == 0L && theSkill.getId() != 4111005) {
            return 0.0;
        }
        final MapleStatEffect homing = player.getStatForBuff(MapleBuffStat.HOMING_BEACON);
        if (homing != null && player.getLinkMid() == monster.getObjectId() && homing.getSourceId() == 5220011) {
            elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * (double)homing.getX();
        }
        final PlayerStats stat = player.getStat();
        elementalMaxDamagePerMonster += elementalMaxDamagePerMonster * (monster.getStats().isBoss() ? (stat.bossdam_r * 2.0) : stat.dam_r) / 100.0;
        switch (monster.getId()) {
            case 1110101: {
                elementalMaxDamagePerMonster *= 2.0;
                break;
            }
        }
        if (player.getDebugMessage()) {
            player.dropMessage("[伤害計算]屬性伤害：" + (int)Math.ceil(elementalMaxDamagePerMonster) + " BOSS伤害：" + (int)Math.ceil((monster.getStats().isBoss() ? player.getStat().bossdam_r : player.getStat().dam_r) - 100.0) + "%");
        }
        if (elementalMaxDamagePerMonster > 500000.0) {
            if (!defined) {
                elementalMaxDamagePerMonster = 500000.0;
            }
        }
        else if (elementalMaxDamagePerMonster < 0.0) {
            elementalMaxDamagePerMonster = 1.0;
        }
        return elementalMaxDamagePerMonster;
    }
    
    public static AttackInfo DivideAttack(final AttackInfo attack,  double rate) {
        attack.real = false;
        if (rate <= 1) {
            return attack;
        }

        for (final AttackPair p : attack.allDamage) {
            if (p.attack != null) {
                for (final Pair<Integer, Boolean> pair : p.attack) {
//                    final Pair<Integer, Boolean> eachd = pair;
                    pair.left = (int)((Integer.valueOf(pair.left) * rate));
                }
            }
        }
        return attack;
    }
    public static AttackInfo Modify_AttackCrit(final AttackInfo attack, MapleCharacter chr, final int type) {
        final int criticalRate = chr.getStat().passive_sharpeye_rate();
        final boolean shadow = (type == 2 && chr.getBuffedValue(MapleBuffStat.SHADOWPARTNER) != null) || (type == 1 && chr.getBuffedValue(MapleBuffStat.MIRROR_IMAGE) != null);
        if (attack.skill != 4211006 && attack.skill != 3211003 && attack.skill != 4111004 && (criticalRate > 0 || attack.skill == 4221001 || attack.skill == 3221007)) {
            for (final AttackPair attackPair : attack.allDamage) {
                if (attackPair.attack != null) {
                    int hit = 0;
                    final int midAtt = attackPair.attack.size() / 2;
                    final List<Pair<Integer, Boolean>> eachd_copy = new ArrayList<Pair<Integer, Boolean>>((Collection<? extends Pair<Integer, Boolean>>)attackPair.attack);
                    for (final Pair<Integer, Boolean> eachd : attackPair.attack) {
                        ++hit;
                        if (!(boolean)Boolean.valueOf(eachd.right)) {
                            if (attack.skill == 4221001) {
                                eachd.right = Boolean.valueOf(hit == 4 && Randomizer.nextInt(100) < 90);
                            }
                            else if (attack.skill == 3221007 || (int)Integer.valueOf(eachd.left) > 500000) {
                                eachd.right = Boolean.valueOf(true);
                            }
                            else if (shadow && hit > midAtt) {
                                eachd.right = ((Pair<Integer, Boolean>)eachd_copy.get(hit - 1 - midAtt)).right;
                            }
                            else {
                                eachd.right = Boolean.valueOf(Randomizer.nextInt(100) < criticalRate);
                            }
                            ((Pair<Integer, Boolean>)eachd_copy.get(hit - 1)).right = eachd.right;
                        }
                    }
                }
            }
        }
        return attack;
    }


    public static AttackInfo parseDmgMaNew(final LittleEndianAccessor lea) {
        final AttackInfo ret = new AttackInfo();
        //2121006    15121019
        //2221006   15121020
        //2321007   15121017   15121035
      if (lea.readInt() == 2121006){
          ret.skill = 2121006;
        }else if (lea.readInt() == 2221006){
          ret.skill = 15121020;
        }else if (lea.readInt() == 2221006){
          if (new Random().nextInt(10) >5){
              ret.skill = 15121017;
          }else{
              ret.skill = 15121035;
          }
      }else{
          return null;
      }
        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
        ret.hits = (byte)(ret.tbyte & 0xF);
        lea.skip(8);

        lea.skip(12);
        ret.charge = -1;
        lea.skip(1);
        ret.unk = 0;
        ret.display = lea.readByte();
        ret.animation = lea.readByte();
        lea.skip(1);
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt();
        ret.allDamage = new ArrayList<AttackPair>();
        for (int i = 0; i < ret.targets; ++i) {
            final int oid = lea.readInt();
            lea.skip(14);
            final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
            for (int j = 0; j < ret.hits; ++j) {
                final int damage = lea.readInt();
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(damage), Boolean.valueOf(false)));
            }
            lea.skip(4);
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();

        return ret;
    }

    public static AttackInfo parseDmgMa(final LittleEndianAccessor lea) {

        final AttackInfo ret = new AttackInfo();
        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
        ret.hits = (byte)(ret.tbyte & 0xF);
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12);
        switch (ret.skill) {
            case 2121001:
            case 2221001:
            case 2321001:
            case 22121000:
            case 22151001: {
                ret.charge = lea.readInt();
                break;
            }
            default: {
                ret.charge = -1;
                break;
            }
        }
        lea.skip(1);
        ret.unk = 0;
        ret.display = lea.readByte();
        ret.animation = lea.readByte();
        lea.skip(1);
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt();
        ret.allDamage = new ArrayList<AttackPair>();
        for (int i = 0; i < ret.targets; ++i) {
            final int oid = lea.readInt();
            lea.skip(14);
            final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
            for (int j = 0; j < ret.hits; ++j) {
                final int damage = lea.readInt();
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(damage), Boolean.valueOf(false)));
            }
            lea.skip(4);
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        return ret;
    }
    public static List<AttackPair> parseDamage(List<MapleMapObject> lea,long damageSum,int ds) {
        if (lea.size()<=0) {
            return null;
        }
        ArrayList<AttackPair> attackPairs = new ArrayList<>();
        for (MapleMapObject mapleMapObject : lea) {
            final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
           double damageAvg =(double) damageSum/ds;
            for (int j = 0; j < ds; ++j) {
                 int damage = damageAvg>Integer.MAX_VALUE?Integer.MAX_VALUE:(int)damageAvg;
                allDamageNumbers.add(new Pair<Integer, Boolean>(damage, Boolean.FALSE));
            }
            attackPairs.add(new AttackPair(mapleMapObject.getObjectId(), allDamageNumbers));
        }
        return attackPairs;
    }
    public static AttackInfo parseDmgM(final LittleEndianAccessor lea) {
        final AttackInfo ret = new AttackInfo();
        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
        ret.hits = (byte)(ret.tbyte & 0xF);
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12);
        switch (ret.skill) {
            case 5101004:
            case 5201002:
            case 14111006:
            case 15101003: {
                ret.charge = lea.readInt();
                break;
            }
            default: {
                ret.charge = 0;
                break;
            }
        }
        ret.unk = lea.readByte();
        ret.display = lea.readByte();
        ret.animation = lea.readByte();
        lea.skip(1);
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt();
        ret.allDamage = new ArrayList<AttackPair>();
        if (ret.skill == 4211006) {
            return parseExplosionAttack(lea, ret);
        }
        for (int i = 0; i < ret.targets; ++i) {
            final int oid = lea.readInt();
            lea.skip(14);
            final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
            for (int j = 0; j < ret.hits; ++j) {
                final int damage = lea.readInt();
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(damage), Boolean.valueOf(false)));
            }
            lea.skip(4);
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        ret.position = lea.readPos();
        if (ret.skill == 14111006) {
            ret.positionxy = lea.readPos();
        }
        return ret;
    }
    
    public static AttackInfo parseDmgR(final LittleEndianAccessor lea) {
        final AttackInfo ret = new AttackInfo();
        lea.skip(1);
        lea.skip(8);
        ret.tbyte = lea.readByte();
        ret.targets = (byte)(ret.tbyte >>> 4 & 0xF);
        ret.hits = (byte)(ret.tbyte & 0xF);
        lea.skip(8);
        ret.skill = lea.readInt();
        lea.skip(12);
        switch (ret.skill) {
            case 3121004:
            case 3221001:
            case 5221004:
            case 13111002: {
                lea.skip(4);
                break;
            }
        }
        ret.charge = -1;
        ret.unk = lea.readByte();
        ret.display = lea.readByte();
        ret.animation = lea.readByte();
        lea.skip(1);
        ret.speed = lea.readByte();
        ret.lastAttackTickCount = lea.readInt();
        ret.slot = (byte)lea.readShort();
        ret.csstar = (byte)lea.readShort();
        ret.AOE = lea.readByte();
        ret.allDamage = new ArrayList<AttackPair>();
        for (int i = 0; i < ret.targets; ++i) {
            final int oid = lea.readInt();
            lea.skip(14);
            final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
            for (int j = 0; j < ret.hits; ++j) {
                final int damage = lea.readInt();
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(damage), Boolean.valueOf(false)));
            }
            lea.skip(4);
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
        }
        lea.skip(4);
        ret.position = lea.readPos();

        return ret;
    }
    
    public static AttackInfo parseExplosionAttack(final LittleEndianAccessor lea, final AttackInfo ret) {
        if (ret.hits == 0) {
            lea.skip(4);
            final byte bullets = lea.readByte();
            for (int j = 0; j < bullets; ++j) {
                ret.allDamage.add(new AttackPair(lea.readInt(), null));
                lea.skip(1);
            }
            lea.skip(2);
            return ret;
        }
        for (int i = 0; i < ret.targets; ++i) {
            final int oid = lea.readInt();
            lea.skip(12);
            final byte bullets2 = lea.readByte();
            final List<Pair<Integer, Boolean>> allDamageNumbers = new ArrayList<Pair<Integer, Boolean>>();
            for (int k = 0; k < bullets2; ++k) {
                allDamageNumbers.add(new Pair<Integer, Boolean>(Integer.valueOf(lea.readInt()), Boolean.valueOf(false)));
            }
            ret.allDamage.add(new AttackPair(oid, allDamageNumbers));
            lea.skip(4);
        }
        lea.skip(4);
        final byte bullets = lea.readByte();
        for (int j = 0; j < bullets; ++j) {
            ret.allDamage.add(new AttackPair(lea.readInt(), null));
            lea.skip(1);
        }
        lea.skip(2);
        return ret;
    }
    public static long realHarm(MapleCharacter play, long damage, MapleMonster monster, int NumberOfSegments) {
        long 数值 = 0L;
        try {
            if (((Integer) LtMS.ConfigValuesMap.get("自定义伤害加成开关")).intValue() < 1) {
                return 数值;
            }
            //平均段数大于2147483647时
            if ((damage/NumberOfSegments) >= 2147483647L){
                数值 = play.getStat().damage * NumberOfSegments - damage;
                if (数值<=0){
                    数值 = 0L;
                }
            }

            if (((Integer) LtMS.ConfigValuesMap.get("扣除21E伤害")).intValue() > 0) {
                数值 -= 2147483647L;
            }
            if(数值>0) {
                play.getMap().broadcastMessage(MobPacket.damageMonster(monster.getObjectId(), 数值));
            }
        } catch (Exception e) {
            FileoutputUtil.outError("logs/额外伤害异常.txt", (Throwable) e);
            return 数值;
        }
        return 数值;
    }
    public static long 伤害减伤(final MapleMonster monster, long damage) {
        long 数值 = 0L;
        if (LtMS.ConfigValuesMap.get("怪物减伤开关")> 0) {
            数值 = (long) Math.floor((damage * getMobRedDam(monster.getLevel(),monster.getId())));
        } else {
            数值 = damage;
        }
        return 数值;
    }
    public static Double getMobRedDam(int level,int mobId) {
        if (DamageParse.MobData.get(mobId) != null) {
            return  DamageParse.MobData.get(Integer.valueOf(mobId));
        }
        if (DamageParse.MobRedDam.get(level) != null) {
            return  DamageParse.MobRedDam.get(Integer.valueOf(level));
        }
        return 1.0;
    }
    public static void readMobRedDam() {
        DamageParse.MobRedDam.clear();
        try (Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mobreddam");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Integer sn = rs.getInt("mobid");
                Double numb = rs.getDouble("numb");
                DamageParse.MobRedDam.put(sn, numb);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            FileoutputUtil.outError("logs/减伤读取异常.txt", (Throwable) e);
            //e.printStackTrace();
        }
    }
    //减伤数据
    public static void readMobData() {
        DamageParse.MobData.clear();
        try (Connection con = (Connection) DBConPool.getInstance().getDataSource().getConnection()) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM mobupdateid");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Integer sn = rs.getInt("mobid");
                Double numb = rs.getDouble("numb");
                DamageParse.MobData.put(sn, numb);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            FileoutputUtil.outError("logs/指定怪物减伤读取异常.txt", (Throwable) e);
            //e.printStackTrace();
        }
    }

    public static long checkDamage(MapleCharacter chr, int skillDamage, int 段数) {
            long maxbasedamage = 0;
             int job = chr.getJob();
             boolean magican = (job >= 200 && job <= 232) || (job >= 1200 && job <= 1212);
            int mainstat = 0;
            int secondarystat = 0;
           if (magican){
               //是法师
               maxbasedamage =  (long)(((chr.getStat().magic * skillDamage) /24)*段数 * (chr.getStat().bossdam_r/100));
           }else{
               //是战士系
               maxbasedamage =  (long)(((chr.getStat().damage * (skillDamage/100)) /24)* 段数 * (chr.getStat().bossdam_r/100));
           }
        return maxbasedamage;
    }

    static {
        DamageParse.固定伤害 = 50000;
        MobRedDam = new ConcurrentHashMap<>();
        MobData = new ConcurrentHashMap<>();
        readMobRedDam();
        readMobData();
    }

}
