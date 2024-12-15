package client.messages.commands;

import constants.GameConstants;
import constants.PiPiConfig;
import client.SkillFactory;
import client.MapleCharacter;
import client.MapleStat;
import client.MapleClient;
import constants.ServerConstants.PlayerGMRank;
import gui.服务端输出信息;
import handling.channel.ChannelServer;
import server.Timer;
import snail.SkillSkin;
import server.life.MapleMonster;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.movement.LifeMovementFragment;
import tools.SearchGenerator;
import tools.packet.MobPacket;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GodCommand
{
    public static PlayerGMRank getPlayerLevelRequired() {
        return PlayerGMRank.神;
    }
    
    public static class MinStats extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            player.getStat().setHp(50);
            player.getStat().setMp(50);
            player.getStat().setMaxHp((short)50);
            player.getStat().setMaxMp((short)50);
            player.getStat().setStr((short)4);
            player.getStat().setDex((short)4);
            player.getStat().setInt((short)4);
            player.getStat().setLuk((short)4);
            player.setLevel((short)10);
            player.updateSingleStat(MapleStat.HP, 50);
            player.updateSingleStat(MapleStat.MP, 50);
            player.updateSingleStat(MapleStat.MAXHP, 50);
            player.updateSingleStat(MapleStat.MAXMP, 50);
            player.updateSingleStat(MapleStat.STR, 4);
            player.updateSingleStat(MapleStat.DEX, 4);
            player.updateSingleStat(MapleStat.INT, 4);
            player.updateSingleStat(MapleStat.LUK, 4);
            player.updateSingleStat(MapleStat.LEVEL, 10);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!MinStats - 预设属性").toString();
        }
    }
    
    public static class Buff extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            SkillFactory.getSkill(9001002).getEffect(1).applyTo(player);
            SkillFactory.getSkill(9001003).getEffect(1).applyTo(player);
            SkillFactory.getSkill(9001008).getEffect(1).applyTo(player);
            SkillFactory.getSkill(9001001).getEffect(1).applyTo(player);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Buff - 施放管理BUFF").toString();
        }
    }
    
    public static class Maxstats extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            player.getStat().setHp(30000);
            player.getStat().setMp(30000);
            player.getStat().setMaxHp((short)30000);
            player.getStat().setMaxMp((short)30000);
            player.getStat().setStr((short)32767);
            player.getStat().setDex((short)32767);
            player.getStat().setInt((short)32767);
            player.getStat().setLuk((short)32767);
            player.setLevel((short)199);
            player.updateSingleStat(MapleStat.HP, 30000);
            player.updateSingleStat(MapleStat.MP, 30000);
            player.updateSingleStat(MapleStat.MAXHP, 30000);
            player.updateSingleStat(MapleStat.MAXMP, 30000);
            player.updateSingleStat(MapleStat.STR, 32767);
            player.updateSingleStat(MapleStat.DEX, 32767);
            player.updateSingleStat(MapleStat.INT, 32767);
            player.updateSingleStat(MapleStat.LUK, 32767);
            player.updateSingleStat(MapleStat.LEVEL, 199);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Maxstats - 满属性").toString();
        }
    }
    
    public static class BanCommand extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            PiPiConfig.setCommandLock(!PiPiConfig.getCommandLock());
            c.getPlayer().dropMessage("指令封锁: " + PiPiConfig.getCommandLock());
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!BanCommand - 封锁指令").toString();
        }
    }
    
    public static class hair extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setHair(id);
            player.updateSingleStat(MapleStat.HAIR, id);
            player.dropMessage(5, "您当前发型的ＩＤ已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Hair <发型代码> - 修改发型").toString();
        }
    }
    
    public static class face extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setFace(id);
            player.updateSingleStat(MapleStat.FACE, id);
            player.dropMessage(5, "您当前脸型的ＩＤ已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Face <脸型代码> - 修改脸型").toString();
        }
    }
    
    public static class Str extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setStr(id);
            player.updateSingleStat(MapleStat.STR, id);
            player.dropMessage(5, "您当前力量已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Str <能力值> - 修改能力值").toString();
        }
    }
    
    public static class Int extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setInt(id);
            player.updateSingleStat(MapleStat.INT, id);
            player.dropMessage(5, "您当前智力已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Int <能力值> - 修改能力值").toString();
        }
    }
    
    public static class Luk extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setLuk(id);
            player.updateSingleStat(MapleStat.LUK, id);
            player.dropMessage(5, "您当前幸运已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Luk <能力值> - 修改能力值").toString();
        }
    }
    
    public static class Dex extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setDex(id);
            player.updateSingleStat(MapleStat.DEX, id);
            player.dropMessage(5, "您当前敏捷已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!Luk <能力值> - 修改能力值").toString();
        }
    }
    
    public static class HP extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setHp(id);
            player.setMaxHp(id);
            player.updateSingleStat(MapleStat.HP, id);
            player.updateSingleStat(MapleStat.MAXHP, id);
            player.dropMessage(5, "您当前HP已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!HP <能力值> - 修改能力值").toString();
        }
    }
    
    public static class MP extends CommandExecute
    {
        @Override
        public boolean execute(final MapleClient c, final String[] splitted) {
            final MapleCharacter player = c.getPlayer();
            int id = 0;
            if (splitted.length < 2) {
                return false;
            }
            id = Integer.parseInt(splitted[1]);
            player.setMp(id);
            player.setMaxMp(id);
            player.updateSingleStat(MapleStat.MP, id);
            player.updateSingleStat(MapleStat.MAXMP, id);
            player.dropMessage(5, "您当前MP已被改为: " + id);
            return true;
        }
        
        @Override
        public String getMessage() {
            return new StringBuilder().append("!MP <能力值> - 修改能力值").toString();
        }
    }

    public static class 物落背包 extends CommandExecute {
        public 物落背包() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            String chrName = "";
            if (c.getPlayer() == null) {
                return false;
            } else if (c.getPlayer().isDropOnMyBag()) {
                c.getPlayer().setDropOnMyBag(false);
                c.getPlayer().dropMessage(5, "物落背包已关闭!");
                return true;
            } else {
                c.getPlayer().setDropOnMyBag(true);
                c.getPlayer().dropMessage(5, "物落背包已开启!");
                return true;
            }
        }

        public String getMessage() {
            return "!物落背包 - 怪物掉落的物品直接进入角色背包";
        }
    }

    public static class 物落脚下 extends CommandExecute {
        public 物落脚下() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            String chrName = "";
            if (c.getPlayer() == null) {
                return false;
            } else if (c.getPlayer().isDropOnMyFoot()) {
                c.getPlayer().setDropOnMyFoot(false);
                c.getPlayer().dropMessage(5, "物落脚下已关闭!");
                return true;
            } else {
                c.getPlayer().setDropOnMyFoot(true);
                c.getPlayer().dropMessage(5, "物落脚下已开启!");
                return true;
            }
        }

        public String getMessage() {
            return "!物落脚下 - 怪物掉落的物品都落到角色脚下";
        }
    }
    public static class 平台吸怪 extends CommandExecute {
        public 平台吸怪() {
        }

        public boolean execute(final MapleClient c, String[] splitted) {
            String chrName = "";
            if (splitted.length < 2) {
                return false;
            } else if (c.getPlayer() == null) {
                return false;
            } else {
                final MapleMap map = c.getPlayer().getMap();
                if (map == null) {
                    c.getPlayer().dropMessage(5, "错误，找不到地图。");
                    return false;
                } else if (!GameConstants.isBossMap(map.getId()) && !GameConstants.isTownMap(map.getId())) {
                    final int mapId = c.getPlayer().getMap().getId();
                    final int channel = c.getChannel();
                    final Point position = c.getPlayer().getPosition();
                    final java.util.List<LifeMovementFragment> moves = c.getPlayer().getLastRes();
                    chrName = splitted[1];
                    Timer.MapTimer mapTimer = Timer.MapTimer.getInstance();
                    if (c.getPlayer().getMap().getOwnerList().size() > 0 && !c.getPlayer().getMap().getOwnerList().contains(c.getPlayer().getId())) {
                        MapleCharacter owner = c.getPlayer().getMap().getCharacterById((Integer)c.getPlayer().getMap().getOwnerList().get(0));
                        if (owner != null) {
                            c.getPlayer().dropMessage(5, "当前地图的所有者是 " + owner.getName() + " ，你无权使用。");
                            return false;
                        }

                        c.getPlayer().getMap().clearOwnerList();
                    }

                    c.getPlayer().getMap().addOwner(c.getPlayer().getId());
                    if (Integer.parseInt(chrName) <= 0 && c.getPlayer().getMap().MobVacSchedule != null && !c.getPlayer().getMap().MobVacSchedule.isCancelled()) {
                        if (c.getPlayer() != null) {
                            c.getPlayer().getMap().MobVacSchedule.cancel(false);
                            c.getPlayer().dropMessage(5, "吸怪已关闭。");
                            c.getPlayer().getMap().clearOwnerList();
                        } else {
                            map.MobVacSchedule.cancel(false);
                        }

                        return true;
                    } else {
                        if (c.getPlayer() != null && (c.getPlayer().getMap().MobVacSchedule == null || c.getPlayer().getMap().MobVacSchedule.isCancelled())) {
                            c.getPlayer().getMap().MobVacSchedule = mapTimer.register(new Runnable() {
                                public void run() {
                                    try {
                                        if (c.getPlayer() != null && mapId == c.getPlayer().getMap().getId() && channel == c.getChannel()) {
                                            Iterator var1 = c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

                                            while(var1.hasNext()) {
                                                MapleMapObject mmo = (MapleMapObject)var1.next();
                                                MapleMonster monster = (MapleMonster)mmo;
                                                if (monster != null && !monster.getStats().isBoss() && Math.abs(monster.getPosition().y - position.y) > 20) {
                                                    c.getPlayer().getMap().broadcastMessage(MobPacket.moveMonster(false, -1, 0, 0, 0, 0, monster.getObjectId(), monster.getPosition(), position, moves));
                                                    monster.setPosition(c.getPlayer().getPosition());
                                                    c.sendPacket(MobPacket.moveMonsterResponse(monster.getObjectId(), (short)((int)(Math.random() * 32767.0)), monster.getMp(), monster.isControllerHasAggro(), 0, 0));
                                                }
                                            }
                                        } else {
                                            map.clearOwnerList();
                                            map.MobVacSchedule.cancel(false);
                                            if (c.getPlayer() != null) {
                                                c.getPlayer().dropMessage(5, "因为你离开了地图，吸怪已自动关闭。");
                                            }
                                        }
                                    } catch (Exception var4) {
                                        服务端输出信息.println_err("【错误】吸怪命令子线程错误，错误原因：" + var4);
                                    }

                                }
                            }, (long)(Integer.parseInt(chrName) * 1000));
                        }

                        c.getPlayer().dropMessage(5, "吸怪已开启，间隔 " + chrName + " 秒。");
                        return true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "当前地图不允许吸怪。");
                    return false;
                }
            }
        }

        public String getMessage() {
            return "!平台吸怪 吸怪间隔（秒） - 每隔多少秒吸一次怪";
        }
    }

    public static class 吸怪 extends CommandExecute {
        public 吸怪() {
        }

        public boolean execute(final MapleClient c, String[] splitted) {
            String chrName = "";
            if (splitted.length < 2) {
                return false;
            } else if (c.getPlayer() == null) {
                return false;
            } else {
                final MapleMap map = c.getPlayer().getMap();
                if (map == null) {
                    c.getPlayer().dropMessage(5, "错误，找不到地图。");
                    return false;
                } else if (!GameConstants.isBossMap(map.getId()) && !GameConstants.isTownMap(map.getId())) {
                    final int mapId = c.getPlayer().getMap().getId();
                    final int channel = c.getChannel();
                    final Point position = c.getPlayer().getPosition();
                    final List<LifeMovementFragment> moves = c.getPlayer().getLastRes();
                    chrName = splitted[1];
                    Timer.MapTimer mapTimer = Timer.MapTimer.getInstance();
                    if (c.getPlayer().getMap().getOwnerList().size() > 0 && !c.getPlayer().getMap().getOwnerList().contains(c.getPlayer().getId())) {
                        MapleCharacter owner = c.getPlayer().getMap().getCharacterById((Integer)c.getPlayer().getMap().getOwnerList().get(0));
                        if (owner != null) {
                            c.getPlayer().dropMessage(5, "当前地图的所有者是 " + owner.getName() + " ，你无权使用。");
                            return false;
                        }

                        c.getPlayer().getMap().clearOwnerList();
                    }

                    c.getPlayer().getMap().addOwner(c.getPlayer().getId());
                    if (Integer.parseInt(chrName) <= 0 && c.getPlayer().getMap().MobVacSchedule != null && !c.getPlayer().getMap().MobVacSchedule.isCancelled()) {
                        if (c.getPlayer() != null) {
                            c.getPlayer().getMap().MobVacSchedule.cancel(false);
                            c.getPlayer().dropMessage(5, "吸怪已关闭。");
                            c.getPlayer().getMap().clearOwnerList();
                        } else {
                            map.MobVacSchedule.cancel(false);
                        }

                        return true;
                    } else {
                        if (c.getPlayer() != null && (c.getPlayer().getMap().MobVacSchedule == null || c.getPlayer().getMap().MobVacSchedule.isCancelled())) {
                            c.getPlayer().getMap().MobVacSchedule = mapTimer.register(new Runnable() {
                                public void run() {
                                    try {
                                        if (c.getPlayer() != null && mapId == c.getPlayer().getMap().getId() && channel == c.getChannel()) {
                                            Iterator var1 = c.getPlayer().getMap().getAllMonstersThreadsafe().iterator();

                                            while(var1.hasNext()) {
                                                MapleMapObject mmo = (MapleMapObject)var1.next();
                                                MapleMonster monster = (MapleMonster)mmo;
                                                if (monster != null && !monster.getStats().isBoss()) {
                                                    c.getPlayer().getMap().broadcastMessage(MobPacket.moveMonster(false, -1, 0, 0, 0, 0, monster.getObjectId(), monster.getPosition(), position, moves));
                                                    monster.setPosition(c.getPlayer().getPosition());
                                                }
                                            }
                                        } else {
                                            map.clearOwnerList();
                                            map.MobVacSchedule.cancel(false);
                                            if (c.getPlayer() != null) {
                                                c.getPlayer().dropMessage(5, "因为你离开了地图，吸怪已自动关闭。");
                                            }
                                        }
                                    } catch (Exception var4) {
                                        服务端输出信息.println_err("【错误】吸怪命令子线程错误，错误原因：" + var4);
                                    }

                                }
                            }, (long)(Integer.parseInt(chrName) * 1000));
                        }

                        c.getPlayer().dropMessage(5, "吸怪已开启，间隔 " + chrName + " 秒。");
                        return true;
                    }
                } else {
                    c.getPlayer().dropMessage(5, "当前地图不允许吸怪。");
                    return false;
                }
            }
        }

        public String getMessage() {
            return "!吸怪 吸怪间隔（秒） - 每隔多少秒吸一次怪";
        }
    }


    public static class 设置玩家指定技能皮肤 extends CommandExecute {
        public 设置玩家指定技能皮肤() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            String chrName = "";
            String skillType = "";
            String skillId = "";
            if (splitted.length < 4) {
                return false;
            } else {
                chrName = splitted[1];
                skillId = splitted[2];
                skillType = splitted[3];
                MapleCharacter chr0 = null;
                Iterator var7 = ChannelServer.getAllInstances().iterator();

                while(var7.hasNext()) {
                    ChannelServer cs = (ChannelServer)var7.next();
                    Iterator var9 = cs.getPlayerStorage().getAllCharacters().iterator();

                    while(var9.hasNext()) {
                        MapleCharacter chr = (MapleCharacter)var9.next();
                        if (chr.getName().equals(chrName)) {
                            chr0 = chr;
                            if (!chr.setSkillSkin(Integer.parseInt(skillId), Integer.parseInt(skillType))) {
                                return false;
                            }

                            Map<Integer, String> skillNames = SearchGenerator.getSearchData(SearchGenerator.SearchType.技能, skillId);
                            String skillName = "";
                            if (skillNames.containsKey(Integer.parseInt(skillId))) {
                                skillName = (String)skillNames.get(Integer.parseInt(skillId));
                            }

                            chr.dropMessage("[管理员提示] 你的 " + skillName + " 技能皮肤已被管理员切换为类型 " + skillType + " ！");
                            c.getPlayer().dropMessage("[系统提示]已成功将 " + chr.getName() + " 的 " + skillName + " 技能皮肤切换为类型 " + skillType + "。");
                            服务端输出信息.println_out("[系统提示]管理员'" + c.getPlayer().getName() + "'使用GM指令将" + chr.getName() + "的 " + skillName + " 技能皮肤切换为类型 " + skillType + "。");
                        }
                    }
                }

                if (chr0 == null) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        public String getMessage() {
            return "!设置玩家指定技能皮肤 [角色名] [技能id] [类型(1、2)]- 将指定玩家的指定技能皮肤设置为某一类型";
        }
    }

    public static class 设置玩家所有技能皮肤 extends CommandExecute {
        public 设置玩家所有技能皮肤() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            String chrName = "";
            String skillType = "";
            if (splitted.length < 3) {
                return false;
            } else {
                chrName = splitted[1];
                skillType = splitted[2];
                MapleCharacter chr0 = null;
                Iterator var6 = ChannelServer.getAllInstances().iterator();

                while(var6.hasNext()) {
                    ChannelServer cs = (ChannelServer)var6.next();
                    Iterator var8 = cs.getPlayerStorage().getAllCharacters().iterator();

                    while(var8.hasNext()) {
                        MapleCharacter chr = (MapleCharacter)var8.next();
                        if (chr.getName().equals(chrName)) {
                            chr0 = chr;
                            chr.setSkillSkinAll(Integer.parseInt(skillType));
                            chr.dropMessage("[管理员提示] 你的所有技能皮肤已被管理员切换为类型 " + skillType + " ！");
                            c.getPlayer().dropMessage("[系统提示]已成功将 " + chr.getName() + " 的所有技能皮肤切换为类型 " + skillType + "。");
                            服务端输出信息.println_out("[系统提示]管理员'" + c.getPlayer().getName() + "'使用GM指令将" + chr.getName() + "的所有技能皮肤切换为类型 " + skillType + "。");
                        }
                    }
                }

                if (chr0 == null) {
                    return false;
                } else {
                    return true;
                }
            }
        }

        public String getMessage() {
            return "!设置玩家所有技能皮肤 [角色名] [类型(1、2)]- 将指定玩家的所有技能皮肤设置为同一类型";
        }
    }

    public static class 重载技能皮肤 extends CommandExecute {
        public 重载技能皮肤() {
        }

        public boolean execute(MapleClient c, String[] splitted) {
            String chrName = "";
            SkillSkin.loadSkillList();
            c.getPlayer().dropMessage("[GM指令]技能皮肤已重载。");
            return true;
        }

        public String getMessage() {
            return "!重载技能皮肤 - 从数据库重载技能特效皮肤";
        }
    }
}
