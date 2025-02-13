package server.maps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import bean.HideAttribute;
import client.*;
import client.inventory.Equip;
import client.inventory.IItem;
import client.inventory.ItemFlag;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import constants.SkillConstants;
import database.DBConPool;
import gui.LtMS;

import server.MapleInventoryManipulator;
import server.MapleItemInformationProvider;
import server.MapleStatEffect;
import server.Start;
import server.events.MapleEventType;
import server.events.MapleGuildMatch;
import server.life.MapleMonster;
import tools.MaplePacketCreator;
import server.life.MapleLifeFactory;
import handling.channel.handler.AttackInfo;

public class MaplePvp
{
    private static int pvpDamage;
    private static int maxDis;
    private static int maxHeight;
    private static boolean isAoe;
    public static boolean isLeft;
    public static boolean isRight;
    
    private static boolean isMeleeAttack(final AttackInfo attack) {
        switch (attack.skill) {
            case 1001004:
            case 1001005:
            case 1111003:
            case 1111004:
            case 1121006:
            case 1121008:
            case 1221007:
            case 1221009:
            case 1311001:
            case 1311002:
            case 1311003:
            case 1311004:
            case 1311005:
            case 1321003:
            case 4001334:
            case 4201005:
            case 4221001: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean isRangeAttack(final AttackInfo attack) {
        switch (attack.skill) {
            case 2001004:
            case 2001005:
            case 2101004:
            case 2101005:
            case 2111006:
            case 2121003:
            case 2201004:
            case 2211002:
            case 2211003:
            case 2211006:
            case 2221003:
            case 2221006:
            case 2301005:
            case 2321007:
            case 3001004:
            case 3001005:
            case 3111006:
            case 3121003:
            case 3121004:
            case 3211006:
            case 3221001:
            case 3221003:
            case 3221007:
            case 4001344:
            case 4101005:
            case 4111004:
            case 4111005:
            case 4121003:
            case 4121007:
            case 4211002:
            case 4221003:
            case 4221007: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    //设置是否AOE技能
    private static boolean isAoeAttack(final AttackInfo attack) {
        switch (attack.skill) {
            case 1111005:
            case 1111006:
            case 1211002:
            case 1221011:
            case 1311006:
            case 2111002:
            case 2111003:
            case 2121001:
            case 2121006:
            case 2121007:
            case 2201005:
            case 2221001:
            case 2221007:
            case 2311004:
            case 2321001:
            case 2321008:
            case 3101005:
            case 3111003:
            case 3111004:
            case 3201005:
            case 3211003:
            case 3211004:
            case 4121004:
            case 4121008:
            case 4211004:
            case 4221004: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static void getDirection(final AttackInfo attack) {
        MaplePvp.isRight = true;
        MaplePvp.isLeft = true;
    }

    private static void DamageBalancer(MapleCharacter chr, AttackInfo attack) {
        pvpDamage = (int)Math.floor(2.0 * Math.random() * 50.0 + 100.0);
        maxDis = 130;
        maxHeight = 35;
        if (attack.skill != 0 && chr != null) {
            int attackRate = (Integer) LtMS.ConfigValuesMap.get("PVP物理攻击技能伤害系数百分比");
            int magicAttackRate = (Integer)LtMS.ConfigValuesMap.get("PVP魔法攻击技能伤害系数百分比");
            ISkill skill = SkillFactory.getSkill(attack.skill);
            if (skill != null) {
                if (GameConstants.isPKBanSkill(skill.getId())) {
                    pvpDamage = 0;
                    maxDis = 0;
                    maxHeight = 0;
                    return;
                }

                MapleStatEffect effect = skill.getEffect(chr.getSkillLevel(skill.getId()));
                if (effect != null) {
                    if (effect.getLt() != null && effect.getRb() != null) {
                        maxDis = Math.max(Math.abs(effect.getLt().x), Math.abs(effect.getRb().x));
                        maxHeight = Math.max(Math.abs(effect.getLt().y), Math.abs(effect.getRb().y));
                    } else if (effect.getRange() > 0) {
                        maxDis = effect.getRange();
                        maxHeight = effect.getRange();
                    }

                    if (SkillConstants.isMagicSkill(skill.getId())) {
                        if (effect.getMatk() > 0) {
                            pvpDamage += (int)((double)effect.getMatk() * ((double)magicAttackRate / 100.0));
                        } else if (effect.getDamage() > 0) {
                            pvpDamage += (int)((double)effect.getDamage() * ((double)magicAttackRate / 100.0));
                        }
                    } else if (effect.getDamage() > 0) {
                        pvpDamage += (int)((double)effect.getDamage() * ((double)attackRate / 100.0));
                    } else if (effect.getMatk() > 0) {
                        pvpDamage += (int)((double)effect.getMatk() * ((double)attackRate / 100.0));
                    }
                }

                int range;
                ISkill skill1;
                MapleStatEffect effect1;
                if (skill.getId() == 4121007 || skill.getId() == 4111005 || skill.getId() == 4001344 || skill.getId() == 14111005 || skill.getId() == 14111002 || skill.getId() == 14001004) {
                    maxDis = 220;
                    maxHeight = 50;
                    range = 0;
                    if (chr.getSkillLevel(4000001) > 0) {
                        skill1 = SkillFactory.getSkill(4000001);
                        if (skill1 != null) {
                            effect1 = skill1.getEffect(chr.getSkillLevel(skill1.getId()));
                            if (effect1 != null && effect1.getRange() > 0 && effect1.getRange() > range) {
                                range = effect1.getRange();
                            }
                        }
                    }

                    if (chr.getSkillLevel(14000001) > 0) {
                        skill1 = SkillFactory.getSkill(14000001);
                        if (skill1 != null) {
                            effect1 = skill1.getEffect(chr.getSkillLevel(skill1.getId()));
                            if (effect1 != null && effect1.getRange() > 0 && effect1.getRange() > range) {
                                range = effect1.getRange();
                            }
                        }
                    }

                    if (range > 0) {
                        maxDis += range;
                        maxHeight = (int)((double)maxHeight + (double)range * 0.2);
                    }
                }

                if (skill.getId() == 3001004 || skill.getId() == 3001005 || skill.getId() == 3101005 || skill.getId() == 3111003 || skill.getId() == 3111006 || skill.getId() == 3121003 || skill.getId() == 3121004 || skill.getId() == 3201005 || skill.getId() == 3211003 || skill.getId() == 3211006 || skill.getId() == 3221003 || skill.getId() == 3221001 || skill.getId() == 3221007 || skill.getId() == 13001003 || skill.getId() == 13101002 || skill.getId() == 13101005 || skill.getId() == 13111001 || skill.getId() == 13111002 || skill.getId() == 13111006 || skill.getId() == 13111007 || skill.getId() == 3100001 || skill.getId() == 3200001) {
                    maxDis = 270;
                    maxHeight = 50;
                    range = 0;
                    if (chr.getSkillLevel(3000002) > 0) {
                        skill1 = SkillFactory.getSkill(3000002);
                        if (skill1 != null) {
                            effect1 = skill1.getEffect(chr.getSkillLevel(skill1.getId()));
                            if (effect1 != null && effect1.getRange() > 0 && effect1.getRange() > range) {
                                range = effect1.getRange();
                            }
                        }
                    }

                    if (chr.getSkillLevel(13000001) > 0) {
                        skill1 = SkillFactory.getSkill(13000001);
                        if (skill1 != null) {
                            effect1 = skill1.getEffect(chr.getSkillLevel(skill1.getId()));
                            if (effect1 != null && effect1.getRange() > 0 && effect1.getRange() > range) {
                                range = effect1.getRange();
                            }
                        }
                    }

                    if (range > 0) {
                        maxDis += range;
                        maxHeight = (int)((double)maxHeight + (double)range * 0.2);
                    }
                }
            }
        }

    }

    private static void monsterBomb(MapleCharacter player, MapleCharacter attackedPlayers, MapleMap map, AttackInfo attack) {
        if (!player.getName().equals(attackedPlayers.getName())) {
            if (!GameConstants.isPKPartyMap(player.getMapId()) || player.getParty() == null || attackedPlayers.getParty() == null || player.getParty().getId() != attackedPlayers.getParty().getId()) {
                if (!GameConstants.isPKGuildMap(player.getMapId()) || player.getGuildId() <= 0 || attackedPlayers.getGuildId() <= 0 || player.getGuildId() != attackedPlayers.getGuildId()) {
                    if (!GameConstants.isPKGuildChannel(player.getClient().getChannel()) || GameConstants.isPKPlayerMap(player.getMapId()) || GameConstants.isPKPartyMap(player.getMapId()) || player.getGuildId() <= 0 || attackedPlayers.getGuildId() <= 0 || player.getGuildId() != attackedPlayers.getGuildId()) {
                        if (attackedPlayers.getLevel() > player.getLevel() + 25) {
                            pvpDamage = (int)((double)pvpDamage * 1.35);
                        } else if (attackedPlayers.getLevel() < player.getLevel() - 25) {
                            pvpDamage = (int)((double)pvpDamage / 1.35);
                        } else if (attackedPlayers.getLevel() > player.getLevel() + 100) {
                            pvpDamage = (int)((double)pvpDamage * 1.5);
                        } else if (attackedPlayers.getLevel() < player.getLevel() - 100) {
                            pvpDamage = (int)((double)pvpDamage / 1.5);
                        }

                        Integer mguard = attackedPlayers.getBuffedValue(MapleBuffStat.MAGIC_GUARD);
                        Integer mesoguard = attackedPlayers.getBuffedValue(MapleBuffStat.MESOGUARD);
                        int magicattack = (player.getDex() + player.getInt() + player.getLuk() + player.getStr()) / 300;
                        pvpDamage += magicattack;
                        int magicat = (player.getStat().getTotalMagic() + player.getStat().getTotalWatk()) / 100;
                        pvpDamage += magicat;
                        if ((Integer)LtMS.ConfigValuesMap.get("PVP根据战斗力计算伤害开关") > 0) {
                            damageBalanceByPower(player, attackedPlayers);
                        }

                        if (pvpDamage > 99999) {
                            pvpDamage = 99999;
                        }

                        int mesoloss;
                        if (mguard != null) {
                            mesoloss = (int)((double)pvpDamage / 0.5);
                            pvpDamage = (int)((double)pvpDamage * 0.7);
                            if (mesoloss > attackedPlayers.getStat().getMp()) {
                                pvpDamage = (int)((double)pvpDamage / 0.7);
                                attackedPlayers.cancelEffectFromBuffStat(MapleBuffStat.MAGIC_GUARD);
                            } else {
                                attackedPlayers.setMp(attackedPlayers.getStat().getMp() - mesoloss);
                                attackedPlayers.updateSingleStat(MapleStat.MP, attackedPlayers.getStat().getMp());
                            }
                        } else if (mesoguard != null) {
                            mesoloss = (int)((double)pvpDamage * 0.75);
                            pvpDamage = (int)((double)pvpDamage * 0.75);
                            if (mesoloss > attackedPlayers.getMeso()) {
                                pvpDamage = (int)((double)pvpDamage / 0.75);
                                attackedPlayers.cancelEffectFromBuffStat(MapleBuffStat.MESOGUARD);
                            } else {
                                attackedPlayers.gainMeso((-mesoloss), false);
                            }
                        }

                        mesoloss = getBalanceRatio(player.getJob());
                        pvpDamage = pvpDamage * mesoloss / 100;
                        MapleMonster pvpMob = MapleLifeFactory.getMonster(9400711);
                        map.spawnMonsterOnGroundBelow(pvpMob, attackedPlayers.getPosition());

                        int attackedDamage;
                        for(attackedDamage = 0; attackedDamage < attack.hits; ++attackedDamage) {
                            attackedPlayers.addHP(-pvpDamage);
                        }

                        attackedDamage = pvpDamage;
                        attackedDamage = pvpDamage * attack.hits;
                        if (pvpMob != null) {
                            pvpMob.sendYellowDamage((long)attackedDamage, false);
                        }

                        attackedPlayers.getClient().sendPacket(MaplePacketCreator.getErrorNotice(player.getName() + " 打了 " + attackedDamage + " 点的伤害!"));
                        map.killMonster(pvpMob, player, false, false, (byte)-1);
                        if (attackedPlayers.getStat().getHp() <= 0 && !attackedPlayers.isAlive()) {
                            int expReward = attackedPlayers.getLevel() * (Integer)LtMS.ConfigValuesMap.get("PK击败得到经验系数");
                            int gpReward = (int)Math.floor(Math.random() * 150.0 + 50.0);
                            if ((double)player.getLevel() * 0.25 >= (double)player.getLevel()) {
                                expReward *= 20;
                            }

                            if (expReward > 0) {
                                player.gainExp(expReward, true, true, true);
                            }

                            player.getClient().sendPacket(MaplePacketCreator.getErrorNotice("你杀了 " + attackedPlayers.getName() + "!! !"));
                            attackedPlayers.getClient().sendPacket(MaplePacketCreator.getErrorNotice("无情的" + player.getName() + "杀了你"));
                            MapleGuildMatch guildEvent = (MapleGuildMatch)player.getClient().getChannelServer().getEvent(MapleEventType.家族对抗赛);
                            if (guildEvent != null && guildEvent.isBegain() && guildEvent.getTimeLeft() > 0L && guildEvent.containMapId(player.getMapId())) {
                                guildEvent.addPoints(player.getGuildId(), (Integer)LtMS.ConfigValuesMap.get("家族对抗赛_击败玩家获得积分"));
                                guildEvent.addPoints(attackedPlayers.getGuildId(), -(Integer)LtMS.ConfigValuesMap.get("家族对抗赛_被击败玩家扣除积分"));
                            }

                            boolean isDropedItem = false;
                            int random;
                            int mount;
                            if ((Integer)LtMS.ConfigValuesMap.get("PK死亡掉落装备开关") > 0) {
                                ArrayList existDropEquipList;
                                ArrayList existDropConsumeList;
                                ArrayList existDropInstallList;
                                ArrayList existDropEtcList;
                                ArrayList existDropCashList;
                                short i;
                               // Equip source;
                                int index0;
                                int count;
                                long dropMount;
                                Iterator var26;
                                Map.Entry entry;
                                ArrayList indexList;
                                short index1;
                                IItem source;
                                HashMap ret;
                                if (Math.random() * 100.0 < (double)(Integer)LtMS.ConfigValuesMap.get("PK死亡掉落装备概率")) {
                                    existDropEquipList = new ArrayList();
                                    existDropConsumeList = new ArrayList();
                                    existDropInstallList = new ArrayList();
                                    existDropEtcList = new ArrayList();
                                    existDropCashList = new ArrayList();
                                    i = 1;

                                    while(true) {
                                        if (i > 96) {
                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.USE).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropConsumeList.add(i);
                                                }
                                            }

                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.SETUP).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropInstallList.add(i);
                                                }
                                            }

                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.ETC).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropEtcList.add(i);
                                                }
                                            }

                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.CASH).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropCashList.add(i);
                                                }
                                            }

                                            ret = new HashMap();
                                            if (existDropEquipList.size() > 0) {
                                                ret.put("EQUIP", existDropEquipList);
                                            }

                                            if (existDropConsumeList.size() > 0) {
                                                ret.put("CONSUME", existDropConsumeList);
                                            }

                                            if (existDropInstallList.size() > 0) {
                                                ret.put("INSTALL", existDropInstallList);
                                            }

                                            if (existDropEtcList.size() > 0) {
                                                ret.put("ETC", existDropEtcList);
                                            }

                                            if (existDropCashList.size() > 0) {
                                                ret.put("CASH", existDropCashList);
                                            }

                                            if (ret.size() <= 0) {
                                                break;
                                            }

                                            index0 = (new Random()).nextInt(ret.size());
                                            count = 0;
                                            dropMount = (long)((new Random()).nextInt((Integer)LtMS.ConfigValuesMap.get("PK掉落道具最大数量") - (Integer)LtMS.ConfigValuesMap.get("PK掉落道具最小数量")) + (Integer)LtMS.ConfigValuesMap.get("PK掉落道具最小数量"));

                                            for(var26 = ret.entrySet().iterator(); var26.hasNext(); ++count) {
                                                entry = (Map.Entry)var26.next();
                                                if (index0 == count) {
                                                    indexList = (ArrayList)entry.getValue();
                                                    index1 = (Short)indexList.get((new Random()).nextInt(indexList.size()));
                                                    if (((String)entry.getKey()).equals("EQUIP")) {
                                                        source = (Equip)attackedPlayers.getInventory(MapleInventoryType.EQUIP).getItem(index1);
                                                        if (source.getQuantity() < dropMount) {
                                                            dropMount = source.getQuantity();
                                                        }

                                                        MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.EQUIP, index1, source.getQuantity());
                                                    } else if (((String)entry.getKey()).equals("CONSUME")) {
                                                        source = attackedPlayers.getInventory(MapleInventoryType.USE).getItem(index1);
                                                        if (source.getQuantity() < dropMount) {
                                                            dropMount = source.getQuantity();
                                                        }

                                                        MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.USE, index1, source.getQuantity());
                                                    } else if (((String)entry.getKey()).equals("INSTALL")) {
                                                        source = attackedPlayers.getInventory(MapleInventoryType.SETUP).getItem(index1);
                                                        if (source.getQuantity() < dropMount) {
                                                            dropMount = source.getQuantity();
                                                        }

                                                        MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.SETUP, index1, source.getQuantity());
                                                    } else if (((String)entry.getKey()).equals("ETC")) {
                                                        source = attackedPlayers.getInventory(MapleInventoryType.ETC).getItem(index1);
                                                        if (source.getQuantity() < dropMount) {
                                                            dropMount = source.getQuantity();
                                                        }

                                                        MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.ETC, index1, source.getQuantity());
                                                    } else if (((String)entry.getKey()).equals("CASH")) {
                                                        source = attackedPlayers.getInventory(MapleInventoryType.CASH).getItem(index1);
                                                        if (source.getQuantity() < dropMount) {
                                                            dropMount = source.getQuantity();
                                                        }

                                                        MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.CASH, index1, source.getQuantity());
                                                    }

                                                    attackedPlayers.dropMessage(5, "[PK警告] 您身上的一件道具掉落了出来。");
                                                    player.dropMessage(5, "[PK提示] 对方的一件道具被你打掉了。");
                                                }
                                            }

                                            isDropedItem = true;
                                            break;
                                        }

                                        source = (Equip)attackedPlayers.getInventory(MapleInventoryType.EQUIP).getItem(i);
                                        if (source != null && GameConstants.isPKDropItem(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                            existDropEquipList.add(i);
                                        }

                                        ++i;
                                    }
                                }

                                if (Math.random() * 100.0 < (double)(Integer)LtMS.ConfigValuesMap.get("PK死亡掉落装备概率2")) {
                                    existDropEquipList = new ArrayList();
                                    existDropConsumeList = new ArrayList();
                                    existDropInstallList = new ArrayList();
                                    existDropEtcList = new ArrayList();
                                    existDropCashList = new ArrayList();
                                    i = 1;

                                    while(true) {
                                        if (i > 96) {
                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.USE).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem2(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropConsumeList.add(i);
                                                }
                                            }

                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.SETUP).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem2(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropInstallList.add(i);
                                                }
                                            }

                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.ETC).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem2(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropEtcList.add(i);
                                                }
                                            }

                                            for(i = 1; i <= 96; ++i) {
                                                source = attackedPlayers.getInventory(MapleInventoryType.CASH).getItem(i);
                                                if (source != null && GameConstants.isPKDropItem2(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                                    existDropCashList.add(i);
                                                }
                                            }

                                            ret = new HashMap();
                                            if (existDropEquipList.size() > 0) {
                                                ret.put("EQUIP", existDropEquipList);
                                            }

                                            if (existDropConsumeList.size() > 0) {
                                                ret.put("CONSUME", existDropConsumeList);
                                            }

                                            if (existDropInstallList.size() > 0) {
                                                ret.put("INSTALL", existDropInstallList);
                                            }

                                            if (existDropEtcList.size() > 0) {
                                                ret.put("ETC", existDropEtcList);
                                            }

                                            if (existDropCashList.size() > 0) {
                                                ret.put("CASH", existDropCashList);
                                            }

                                            if (ret.size() > 0) {
                                                index0 = (new Random()).nextInt(ret.size());
                                                count = 0;
                                                dropMount = (long)((new Random()).nextInt((Integer)LtMS.ConfigValuesMap.get("PK掉落道具最大数量") - (Integer)LtMS.ConfigValuesMap.get("PK掉落道具最小数量")) + (Integer)LtMS.ConfigValuesMap.get("PK掉落道具最小数量"));

                                                for(var26 = ret.entrySet().iterator(); var26.hasNext(); ++count) {
                                                    entry = (Map.Entry)var26.next();
                                                    if (index0 == count) {
                                                        indexList = (ArrayList)entry.getValue();
                                                        index1 = (Short)indexList.get((new Random()).nextInt(indexList.size()));
                                                        if (((String)entry.getKey()).equals("EQUIP")) {
                                                            source = (Equip)attackedPlayers.getInventory(MapleInventoryType.EQUIP).getItem(index1);
                                                            if (source.getQuantity() < dropMount) {
                                                                dropMount = source.getQuantity();
                                                            }

                                                            MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.EQUIP, index1, source.getQuantity());
                                                        } else if (((String)entry.getKey()).equals("CONSUME")) {
                                                            source = attackedPlayers.getInventory(MapleInventoryType.USE).getItem(index1);
                                                            if (source.getQuantity() < dropMount) {
                                                                dropMount = source.getQuantity();
                                                            }

                                                            MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.USE, index1, source.getQuantity());
                                                        } else if (((String)entry.getKey()).equals("INSTALL")) {
                                                            source = attackedPlayers.getInventory(MapleInventoryType.SETUP).getItem(index1);
                                                            if (source.getQuantity() < dropMount) {
                                                                dropMount = source.getQuantity();
                                                            }

                                                            MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.SETUP, index1, source.getQuantity());
                                                        } else if (((String)entry.getKey()).equals("ETC")) {
                                                            source = attackedPlayers.getInventory(MapleInventoryType.ETC).getItem(index1);
                                                            if (source.getQuantity() < dropMount) {
                                                                dropMount = source.getQuantity();
                                                            }

                                                            MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.ETC, index1, source.getQuantity());
                                                        } else if (((String)entry.getKey()).equals("CASH")) {
                                                            source = attackedPlayers.getInventory(MapleInventoryType.CASH).getItem(index1);
                                                            if (source.getQuantity() < dropMount) {
                                                                dropMount = source.getQuantity();
                                                            }

                                                            MapleInventoryManipulator.drop(attackedPlayers.getClient(), MapleInventoryType.CASH, index1, source.getQuantity());
                                                        }

                                                        attackedPlayers.dropMessage(5, "[PK警告] 您身上的一件道具掉落了出来。");
                                                        player.dropMessage(5, "[PK提示] 对方的一件道具被你打掉了。");
                                                    }
                                                }
                                            }

                                            isDropedItem = true;
                                            break;
                                        }
                                        source = (Equip)attackedPlayers.getInventory(MapleInventoryType.EQUIP).getItem(i);
                                        if (source != null && GameConstants.isPKDropItem2(source.getItemId()) && source.getFlag() != ItemFlag.LOCK.getValue() && source.getFlag() != ItemFlag.UNTRADEABLE.getValue() && !MapleItemInformationProvider.getInstance().isCash(source.getItemId()) && (source.getExpiration() < 0L || (double)source.getExpiration() >= 4.7E12)) {
                                            existDropEquipList.add(i);
                                        }
                                        ++i;
                                    }
                                }

                                if (!isDropedItem) {
                                    random = (int)Math.floor((double)(Integer)LtMS.ConfigValuesMap.get("PK掉落金币最小数量") + Math.random() * (double)((Integer)LtMS.ConfigValuesMap.get("PK掉落金币最大数量") - (Integer)LtMS.ConfigValuesMap.get("PK掉落金币最小数量")));
                                    if (attackedPlayers.getMeso() >= random) {
                                        attackedPlayers.getMap().spawnMesoDrop(random, attackedPlayers.getPosition(), attackedPlayers, attackedPlayers, false, (byte)0);
                                        attackedPlayers.gainMeso((-random), true);
                                        attackedPlayers.getClient().sendPacket(MaplePacketCreator.getErrorNotice("无情的" + player.getName() + "杀了你 你损失了" + random + "金币!"));
                                    } else if (attackedPlayers.getMeso() > 0) {
                                        mount = attackedPlayers.getMeso();
                                        attackedPlayers.getMap().spawnMesoDrop(mount, attackedPlayers.getPosition(), attackedPlayers, attackedPlayers, false, (byte)0);
                                        attackedPlayers.gainMeso((-mount), true);
                                        attackedPlayers.getClient().sendPacket(MaplePacketCreator.getErrorNotice("无情的" + player.getName() + "杀了你 你损失了" + mount + "金币!"));
                                    } else {
                                        attackedPlayers.dropMessage("[系统警告] 您已经被搜刮的一穷二白了，光脚的不怕穿鞋的，干就完了。");
                                        player.dropMessage("[系统警告] 请不要再残害他，对方已被你榨干了。");
                                    }
                                }
                            } else {
                                random = (int)Math.floor((double)(Integer)LtMS.ConfigValuesMap.get("PK掉落金币最小数量") + Math.random() * (double)((Integer)LtMS.ConfigValuesMap.get("PK掉落金币最大数量") - (Integer)LtMS.ConfigValuesMap.get("PK掉落金币最小数量")));
                                if (attackedPlayers.getMeso() >= random) {
                                    attackedPlayers.getMap().spawnMesoDrop(random, attackedPlayers.getPosition(), attackedPlayers, attackedPlayers, false, (byte)0);
                                    attackedPlayers.gainMeso((-random), true);
                                    attackedPlayers.getClient().sendPacket(MaplePacketCreator.getErrorNotice("无情的" + player.getName() + "杀了你 你损失了" + random + "金币!"));
                                } else if (attackedPlayers.getMeso() > 0) {
                                    mount = attackedPlayers.getMeso();
                                    attackedPlayers.getMap().spawnMesoDrop(mount, attackedPlayers.getPosition(), attackedPlayers, attackedPlayers, false, (byte)0);
                                    attackedPlayers.gainMeso((-mount), true);
                                    attackedPlayers.getClient().sendPacket(MaplePacketCreator.getErrorNotice("无情的" + player.getName() + "杀了你 你损失了" + mount + "金币!"));
                                } else {
                                    attackedPlayers.dropMessage("[系统警告] 您已经被搜刮的一穷二白了，光脚的不怕穿鞋的，干就完了。");
                                    player.dropMessage("[系统警告] 请不要再残害他，对方已被你榨干了。");
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    //pvp对战
    public static void doPvP(MapleCharacter player, MapleMap map, AttackInfo attack) {
        DamageBalancer(player, attack);
        getDirection(attack);
//        Iterator var3 = player.getMap().getNearestPvpChar(player.getPosition(), (double)maxDis, (double)maxHeight, player.getMap().getCharacters()).iterator();
//
//        while(true) {
//            while(true) {
//                MapleCharacter attackedPlayers;
//                do {
//                    do {
//                        do {
//                            if (!var3.hasNext()) {
//                                return;
//                            }
//
//                            attackedPlayers = (MapleCharacter)var3.next();
//                        } while(attackedPlayers.getLevel() < 30);
//                    } while(!attackedPlayers.isAlive());
//                } while(player.getParty() != null && player.getParty() == attackedPlayers.getParty());
//
//                MapleGuildMatch guildEvent = (MapleGuildMatch)player.getClient().getChannelServer().getEvent(MapleEventType.家族对抗赛);
//                if (guildEvent != null && guildEvent.isBegain() && guildEvent.getTimeLeft() > 0L && guildEvent.containMapId(player.getMapId())) {
//                    if (player.getGuildId() != attackedPlayers.getGuildId()) {
//                        monsterBomb(player, attackedPlayers, map, attack);
//                    }
//                } else {
//                    monsterBomb(player, attackedPlayers, map, attack);
//                }
//            }
//        }
        // 获取玩家周围符合条件的PVP角色列表
        List<MapleCharacter> nearestPvpChars = (List<MapleCharacter>) player.getMap().getNearestPvpChar(player.getPosition(), (double)maxDis, (double)maxHeight, player.getMap().getCharacters());
        // 遍历这些角色，对符合条件的角色进行攻击
        for (MapleCharacter attackedPlayers : nearestPvpChars) {
            if (attackedPlayers.getLevel() < 30 || !attackedPlayers.isAlive()) {
                continue;
            }

            if (player.getParty() != null && player.getParty().equals(attackedPlayers.getParty())) {
                continue;
            }

            MapleGuildMatch guildEvent = (MapleGuildMatch) player.getClient().getChannelServer().getEvent(MapleEventType.家族对抗赛);
            if (guildEvent != null && guildEvent.isBegain() && guildEvent.getTimeLeft() > 0L && guildEvent.containMapId(player.getMapId())) {
                if (player.getGuildId() != attackedPlayers.getGuildId()) {
                    monsterBomb(player, attackedPlayers, map, attack);
                }
            } else {
                monsterBomb(player, attackedPlayers, map, attack);
            }
        }
    }

    public static void damageBalanceByPower(MapleCharacter player, MapleCharacter attackedPlayer) {
        double rate = Math.sqrt((double)player.getPower() / (double)attackedPlayer.getPower());
        pvpDamage = (int)((double)pvpDamage * rate);
        if (pvpDamage < 0) {
            pvpDamage = 0;
        }

        if (pvpDamage > 0) {
            int def0 = player.getStat().wdef + player.getStat().mdef;
            int def1 = attackedPlayer.getStat().wdef + attackedPlayer.getStat().mdef;
            if (def1 > def0) {
                rate = (double)def0 / (double)def1;
                pvpDamage = (int)((double)pvpDamage * 0.4 + (double)pvpDamage * rate * 0.6);
            }

            int acc = player.getStat().accuracy;
            int avoid = attackedPlayer.getStat().avoid;
            if (avoid > acc) {
                int a = (int)((1.0 - (double)acc / (double)avoid) * 10000.0);
                if (a > 7000) {
                    a = 7000;
                }

                if (Math.random() * 10000.0 < (double)a) {
                    pvpDamage = 0;
                }
            }

            if (pvpDamage > 0) {
                Map<ISkill, SkillEntry> skillMap = attackedPlayer.getSkills();
                Iterator var9 = skillMap.entrySet().iterator();

                while(true) {
                    ISkill skill;
                    do {
                        do {
                            if (!var9.hasNext()) {
                                return;
                            }

                            Map.Entry<ISkill, SkillEntry> key = (Map.Entry)var9.next();
                            skill = (ISkill)key.getKey();
                        } while(skill == null);
                    } while(skill.getId() != 4120002 && skill.getId() != 4220002);

                    if (Math.random() * 100.0 < (double)(skill.getEffect(attackedPlayer.getSkillLevel(skill)).getProb() / 2)) {
                        attackedPlayer.dropMessage(5, "假动作生效，你成功躲避了对手的攻击！");
                        attackedPlayer.sendSkillEffect(skill.getId(), 2);
                        pvpDamage = 0;
                        break;
                    }
                }
            }
            HideAttribute hideAttribute = Start.hideAttributeMap.get(attackedPlayer.getId());

            if(hideAttribute.totalDodge>0){
                Random rand = new Random();
                if (rand.nextInt(1000) <= hideAttribute.totalDodge) {
                    player.sendSkillEffect(4121004, 2);
                    player.dropMessage(5, "闪避生效，你成功躲避了敌人的伤害。");
                    pvpDamage = 0;
                }
            }


        }

    }

     public static Map<Integer, Integer> jobBalanceMap = new ConcurrentHashMap<>();
        private static final String QUERY = "SELECT * FROM snail_pk_job_balance";

        public static boolean loadJobBalanceMapFromDB() {
            jobBalanceMap.clear();

            try (Connection con = DBConPool.getConnection();
                 PreparedStatement ps = con.prepareStatement(QUERY);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    jobBalanceMap.put(rs.getInt("jobid"), rs.getInt("ratio"));
                }
                return true;

            } catch (SQLException e) {
                return false;
            }
        }

    public static int getBalanceRatio(int jobId) {
        if (jobBalanceMap.size() == 0) {
            loadJobBalanceMapFromDB();
        }

        return (Integer)jobBalanceMap.get(jobId);
    }
    static {
        MaplePvp.isAoe = false;
        MaplePvp.isLeft = false;
        MaplePvp.isRight = false;
    }
}
