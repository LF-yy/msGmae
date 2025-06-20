package server.life;

import java.awt.Rectangle;
import java.util.*;

import bean.HideAttribute;
import client.ISkill;
import client.SkillFactory;
import constants.GameConstants;
import gui.LtMS;
import handling.world.MaplePartyCharacter;
import server.MapleStatEffect;
import server.Start;
import server.maps.MapleMist;
import client.MapleDisease;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;
import client.status.MonsterStatus;
import client.MapleCharacter;

import java.awt.Point;

public class MobSkill
{
    private final int skillId;
    private final int skillLevel;
    private int mpCon;
    private int spawnEffect;
    private int hp;
    private int x;
    private int y;
    private long duration;
    private long cooltime;
    private float prop;
    private short limit;
    private List<Integer> toSummon;
    private Point lt;
    private Point rb;
    private boolean summonOnce;
    
    public MobSkill(final int skillId, final int level) {
        this.toSummon = new ArrayList<Integer>();
        this.skillId = skillId;
        this.skillLevel = level;
    }
    
    public void setMpCon(final int mpCon) {
        this.mpCon = mpCon;
    }
    
    public void addSummons(final List<Integer> toSummon) {
        this.toSummon = toSummon;
    }
    
    public void setSpawnEffect(final int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }
    
    public void setHp(final int hp) {
        this.hp = hp;
    }
    
    public void setX(final int x) {
        this.x = x;
    }
    
    public void setY(final int y) {
        this.y = y;
    }
    
    public void setDuration(final long duration) {
        this.duration = duration;
    }
    
    public void setCoolTime(final long cooltime) {
        this.cooltime = cooltime;
    }
    
    public void setProp(final float prop) {
        this.prop = prop;
    }
    
    public void setLtRb(final Point lt, final Point rb) {
        this.lt = lt;
        this.rb = rb;
    }
    
    public void setLimit(final short limit) {
        this.limit = limit;
    }
    
    public boolean checkCurrentBuff(final MapleCharacter player, final MapleMonster monster) {
        boolean stop = false;
        switch (this.skillId) {
            case 100:
            case 110:
            case 150: {
                stop = monster.isBuffed(MonsterStatus.WEAPON_ATTACK_UP);
                break;
            }
            case 101:
            case 111:
            case 151: {
                stop = monster.isBuffed(MonsterStatus.WEAPON_DEFENSE_UP);
                break;
            }
            case 102:
            case 112:
            case 152: {
                stop = monster.isBuffed(MonsterStatus.MAGIC_ATTACK_UP);
                break;
            }
            case 103:
            case 113:
            case 153: {
                stop = monster.isBuffed(MonsterStatus.MAGIC_DEFENSE_UP);
                break;
            }
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145: {
                stop = (monster.isBuffed(MonsterStatus.DAMAGE_IMMUNITY) || monster.isBuffed(MonsterStatus.MAGIC_IMMUNITY) || monster.isBuffed(MonsterStatus.WEAPON_IMMUNITY));
                break;
            }
            case 200:
            case 201: {
                stop = (player.getMap().getNumMonsters() >= this.limit);
                break;
            }
        }
        return stop;
    }
    /*
     * 怪物BUFF解释
     * 100 = 物理攻击提高
     * 101 = 魔法攻击提高
     * 102 = 物理防御提高
     * 103 = 魔法防御提高
     * 104 = 致命攻击 难道就是血蓝为1？
     * 105 = 消费
     * 110 = 周边物理攻击提高
     * 111 = 周边魔法攻击提高
     * 112 = 周边物理防御提高
     * 113 = 周边魔法防御提高
     * 114 = HP恢复
     * 115 = 自己及周围移动速度变化
     * 120 = 封印
     * 121 = 黑暗
     * 122 = 虚弱
     * 123 = 晕眩
     * 124 = 诅咒
     * 125 = 中毒
     * 126 = 慢动作
     * 127 = 魔法无效
     * 128 = 诱惑
     * 129 = 逐出
     * 131 = 区域中毒
     * 133 = 不死化
     * 134 = 药水停止
     * 135 = 从不停止
     * 136 = 致盲
     * 137 = 中毒
     * 138 = 潜在能力无效
     * 140 = 物理防御
     * 141 = 魔法防御
     * 142 = 皮肤硬化
     * 143 = 物理反击免疫
     * 144 = 魔法反击免疫
     * 145 = 物理魔法反击免疫
     * 150 = PAD修改
     * 151 = MAD修改
     * 152 = PDD修改
     * 153 = MDD修改
     * 154 = ACC修改
     * 155 = EVA修改
     * 156 = Speed修改
     * 170 = 传送
     * 200 = 召唤
     */
    public void applyEffect(final MapleCharacter player, final MapleMonster monster, final boolean skill) {
        if (player.haveItem(LtMS.ConfigValuesMap.get("神圣之躯"),1)){
            return;
        }
        if ((Integer) LtMS.ConfigValuesMap.get("VIP无效BOSS技能开关") > 0 && player.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无效BOSS技能道具ID"))) {
            return;
        } else if ((Integer)LtMS.ConfigValuesMap.get("VIP无敌开关") > 0 && player.haveItem((Integer)LtMS.ConfigValuesMap.get("VIP无敌道具ID"))) {
            return;
        }

        try {
            MapleDisease disease = null;
            final Map<MonsterStatus, Integer> stats = new EnumMap<MonsterStatus, Integer>(MonsterStatus.class);
            final List<Integer> reflection = new LinkedList<Integer>();
            if (LtMS.ConfigValuesMap.get((Object)"怪物状态开关") >= 1) {
                switch (this.skillId) {
                    case 102:
                    case 112:
                    case 152: {
                        //monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [防御增幅]"));
                        break;
                    }
                    case 131:
                    case 180:
                    case 191:
                    {
                       // monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用无视防御技能 [召唤毒雾]"));
                        break;
                    }
                    case 128: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用跳舞技能 [诱导]"));
                        break;
                    }
                    case 103:
                    case 113:
                    case 153: {
                       // monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [魔法防御增幅]"));
                        break;
                    }
                    case 110:
                    case 100:
                    case 150: {
                     //   monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [物理攻击增幅]"));
                        break;
                    }
                    case 101:
                    case 111:
                    case 151: {
                       // monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [魔法攻击增幅]"));
                        break;
                    }
                    case 120: {
                        if (LtMS.ConfigValuesMap.get((Object)"启用封印")==0 ){
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [封印]"));
                        }
                        break;
                    }
                    case 121: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [黑暗]"));
                        break;
                    }
                    case 122: {
                        //monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [弱化]"));
                        break;
                    }
                    case 124: {
                      //  monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [诅咒]"));
                        break;
                    }
                    case 114: {
                       //monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [治愈]"));
                        break;
                    }
                    case 140: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [物理无效]"));
                        break;
                    }
                    case 141: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [魔法无效]"));
                        break;
                    }
                    case 127: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [驱散]"));
                        break;
                    }
                    case 129: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用技能 [乾坤大挪移]"));
                        break;
                    }
                    case 143: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 将开启物理反射"));
                        break;
                    }
                    case 144: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 将开启魔法反射"));

                        break;
                    }
                    case 145: {
                        monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 将开启物理魔法反射"));
                        break;
                    }
                    case 183:
                        if( monster.getLevel()>=160) {
                            player.dropMessage(5, "" + monster.stats.getName() + " 使用技能 [灼烧]");
                        }
                        break;
                    case 186:
                        player.dropMessage(5, "" + monster.stats.getName() + " 使用技能 [变身]");
                        break;
                    case 187:
                        if( monster.getLevel()>=230) {
                            player.dropMessage(5, "" + monster.stats.getName() + " 使用技能 [致死]");
                        }
                        break;
                    case 200:
                    case 201:
                        player.dropMessage(5, "" + monster.stats.getName() + " 使用技能 [召唤小弟]");
                    default: {
                        if (player.isGM()) {
                            monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("" + monster.stats.getName() + " 使用未知技能 " + this.skillId));
                        }
                        return;
                    }
                }
            }


            if (this.isCanAvoidSkill(this.skillId) && (player.getSkillLevel(4120002) > 0 || player.getSkillLevel(4220002) > 0)) {
                ISkill skill0 = SkillFactory.getSkill(4120002);
                if (skill0 != null) {
                    MapleStatEffect skillEff = skill0.getEffect(player.getSkillLevel(4120002));
                    if (skillEff != null) {
                        int targetMapId = skillEff.getProb();
                        Random rand = new Random();
                        if (rand.nextInt(100) <= targetMapId) {
                            player.sendSkillEffect(4120002, 2);
                            player.dropMessage(5, "假动作生效，你成功躲避了怪物的技能。");
                            return;
                        }
                    }
                }

                ISkill skill1 = SkillFactory.getSkill(4220002);
                if (skill1 != null) {
                    MapleStatEffect skillEff = skill1.getEffect(player.getSkillLevel(4220002));
                    if (skillEff != null) {
                        int prob = skillEff.getProb();
                        Random rand = new Random();
                        if (rand.nextInt(100) <= prob) {
                            player.sendSkillEffect(4220002, 2);
                            player.dropMessage(5, "假动作生效，你成功躲避了怪物的技能。");
                            return;
                        }
                    }
                }
            }
            HideAttribute hideAttribute = Start.hideAttributeMap.get(player.getId());

            if (this.isCanAvoidSkill(this.skillId)) {
                    if (hideAttribute.totalDodge > 0) {
                        Random rand = new Random();
                        if (rand.nextInt(10000) <= hideAttribute.totalDodge) {
                            player.sendSkillEffect(4220002, 2);
                            player.dropMessage(5, "闪避生效，你成功躲避了怪物的技能。");
                            return;
                        }
                    }
                    if (hideAttribute.totalResistance > 0) {
                        Random rand = new Random();
                        if (rand.nextInt(1000) <= hideAttribute.totalResistance) {
                            player.sendSkillEffect(4220002, 2);
                            player.dropMessage(5, "抗性生效，你成功抵抗了怪物的技能。");
                            return;
                        }
                    }
                }

            Iterator var14;
            Integer mobId;
            Iterator var36;
            MapleCharacter character;
            MapleCharacter curChar;
            MaplePartyCharacter character1;
            label508:
            switch (this.skillId) {
            case 100:
            case 110:
            case 150: {
                stats.put(MonsterStatus.WEAPON_ATTACK_UP, Integer.valueOf(this.x));
                break;
            }
            case 101:
            case 111:
            case 151: {
                stats.put(MonsterStatus.MAGIC_ATTACK_UP, Integer.valueOf(this.x));
                break;
            }
            case 102:
            case 112:
            case 152: {
                stats.put(MonsterStatus.WEAPON_DEFENSE_UP, Integer.valueOf(this.x));
                break;
            }
            case 103:
            case 113:
            case 153: {
                stats.put(MonsterStatus.MAGIC_DEFENSE_UP, Integer.valueOf(this.x));
                break;
            }
            case 154: {
                stats.put(MonsterStatus.ACC, Integer.valueOf(this.x));
                break;
            }
            case 155: {
                stats.put(MonsterStatus.AVOID, Integer.valueOf(this.x));
                break;
            }
            case 156: {
                stats.put(MonsterStatus.SPEED, Integer.valueOf(this.x));
                break;
            }
            case 157: {
                stats.put(MonsterStatus.SEAL, Integer.valueOf(this.x));
                break;
            }
                case 114: {
                    if (this.lt != null && this.rb != null && skill && monster != null) {
                        final List<MapleMapObject> objects = this.getObjectsInRange(monster, MapleMapObjectType.MONSTER);
//                        int healHP = this.getHP();
                        for (final MapleMapObject mons : objects) {
                            long hp = ((MapleMonster)mons).getHp()*LtMS.ConfigValuesMap.get("治愈比例")/100;
//                            if (hp > Integer.MAX_VALUE){
//                                healHP= Integer.MAX_VALUE;
//                            }else{
//                                healHP= (int)hp;
//                            }
                            ((MapleMonster)mons).healLong(hp, 0, true);
                        }
                        break;
                    }
                    if (monster != null) {
                        monster.healLong(this.getHP(), 0, true);
                        break;
                    }
                    break;
                }
            case 120:{
                disease = MapleDisease.getByMobSkill(this.skillId);
                break;
            }
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:{
                disease = MapleDisease.getByMobSkill(this.skillId);
                break;
            }
            case 128:{
                disease = MapleDisease.getByMobSkill(this.skillId);
                break;
            }

            case 132:
            case 133:
            case 134:
            case 135:
            case 136:
            case 137: {
                disease = MapleDisease.getByMobSkill(this.skillId);
                break;
            }
            case 127: {
                if (player == null || !player.isBuffedValue(1111002) && !player.isBuffedValue(1120003) && !player.isBuffedValue(1121002) && !player.isBuffedValue(1221002) && !player.isBuffedValue(1321002) && !player.isBuffedValue(4111002) && !player.isBuffedValue(4331002) && !player.isBuffedValue(21121003)) {
                    if (this.lt != null && this.rb != null && skill && monster != null && player != null) {
                        for (final MapleCharacter cc : this.getPlayersInRange(monster, player)) {
                            cc.dispel();
                        }
                        break;
                    }
                    if (player != null) {
                        player.dispel();
                        break;
                    }
                }
                break;
            }
            case 129: {
                if (monster == null) {
                    break;
                }
                if (monster.getEventInstance() != null && monster.getEventInstance().getName().contains((CharSequence)"BossQuest")) {
                    break;
                }
                final BanishInfo info = monster.getStats().getBanishInfo();
                if (info != null) {
                    if (this.lt != null && this.rb != null && skill && player != null) {
                        for (MapleCharacter chr : this.getPlayersInRange(monster, player)) {
                            chr.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                        }
                    }
                    else if (player != null) {
                        player.changeMapBanish(info.getMap(), info.getPortal(), info.getMsg());
                    }
                }
                break;
            }
            case 131: {
                if (monster != null) {
                    monster.getMap().spawnMist(new MapleMist(this.calculateBoundingBox(monster.getPosition(), true), monster, this), this.x * 10, false);
                }

                if (this.getSkillLevel() == 13) {
                    if (this.lt != null && this.rb != null && skill && monster != null && player != null) {
                        for (MapleCharacter mapleCharacter : this.getPlayersInRange(monster, player)) {
                            mapleCharacter.burn((short)12, (int)this.getDuration());
                        }
                    } else if (player != null) {
                        player.burn((short)12, (int)this.getDuration());
                    }
                }
                break;
            }
            case 140: {
                stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
                break;
            }
            case 141: {
                stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
                break;
            }
            case 142: {
                stats.put(MonsterStatus.DAMAGE_IMMUNITY, Integer.valueOf(this.x));
                break;
            }
            case 143: {

                stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                if (monster == null) {
                    break;
                }
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理反射状态。"));
                break;
            }
            case 144: {

                stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                if (monster == null) {
                    break;
                }
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了魔法反射状态。"));
                break;
            }
            case 145: {

                stats.put(MonsterStatus.WEAPON_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.WEAPON_IMMUNITY, Integer.valueOf(this.x));
                stats.put(MonsterStatus.MAGIC_DAMAGE_REFLECT, Integer.valueOf(this.x));
                stats.put(MonsterStatus.MAGIC_IMMUNITY, Integer.valueOf(this.x));
                reflection.add(Integer.valueOf(this.x));
                if (monster == null) {
                    break;
                }
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理和魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理和魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理和魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理和魔法反射状态。"));
                monster.getMap().broadcastMessage(MaplePacketCreator.yellowChat("[系统提示] 注意 " + monster.getStats().getName() + " 开启了物理和魔法反射状态。"));
                break;
            }
            //灼烧
                case 183:
                    if (this.lt != null && this.rb != null && skill && monster != null && monster.getLevel()>=160) {
                        var36 = this.getPlayersInRange(monster, player).iterator();
                        while(true) {
                            if (!var36.hasNext()) {
                                break label508;
                            }
                            ((MapleCharacter)var36.next()).burn((short)12, (int)this.getDuration());
                        }
                    } else {
                        player.burn((short)12, (int)this.getDuration());
                        break;
                    }
                    //变身
                case 186:
                    int morphId = 0;
                    var14 = this.getSummons().iterator();
                    if (var14.hasNext()) {
                        mobId = (Integer)var14.next();
                        morphId = mobId;
                    }

                    if (this.lt != null && this.rb != null && skill && monster != null) {
                        var14 = this.getPlayersInRange(monster, player).iterator();

                        while(true) {
                            if (!var14.hasNext()) {
                                break label508;
                            }

                            curChar = (MapleCharacter)var14.next();
                            if (curChar.getMorphState() == -1) {
                                curChar.setMorph(2210022, morphId, (int)this.getDuration(), false);
                            }
                        }
                    } else {
                        if (player.getMorphState() == -1) {
                            player.setMorph(2210022, morphId, (int)this.getDuration(), false);
                        }
                        break;
                    }
                    //致死
                case 187:
                    if (monster.getLevel()>=230) {
                        player.killSelf();
                    }
                    break;
                case 200:
                case 201:
                    if (monster == null) {
                        return;
                    }
                        var14 = this.getSummons().iterator();

                        label365:
                        while (true) {
                            MapleMonster toSpawn;
                            int ypos;
                            int xpos;
                            label363:
                            while (true) {
                                do {
                                    while (true) {
                                        if (!var14.hasNext()) {
                                            break label365;
                                        }

                                        mobId = (Integer) var14.next();

                                        try {
                                            toSpawn = MapleLifeFactory.getMonster(GameConstants.getCustomSpawnID(monster.getId(), mobId));
                                            break;
                                        } catch (RuntimeException var19) {
                                        }
                                    }
                                } while (toSpawn == null);

                                toSpawn.setPosition(monster.getPosition());
                                ypos = (int) monster.getPosition().getY();
                                xpos = (int) monster.getPosition().getX();
                                switch (mobId) {
                                    case 8500003:
                                        toSpawn.setFh((int) Math.ceil(Math.random() * 19.0));
                                        ypos = -590;
                                        break label363;
                                    case 8500004:
                                        xpos = (int) (monster.getPosition().getX() + Math.ceil(Math.random() * 1000.0) - 500.0);
                                        ypos = (int) monster.getPosition().getY();
                                        break label363;
                                    case 8510100:
                                        if (Math.ceil(Math.random() * 5.0) == 1.0) {
                                            ypos = 78;
                                            xpos = (int) (0.0 + Math.ceil(Math.random() * 5.0)) + (Math.ceil(Math.random() * 2.0) == 1.0 ? 180 : 0);
                                        } else {
                                            xpos = (int) (monster.getPosition().getX() + Math.ceil(Math.random() * 1000.0) - 500.0);
                                        }
                                        break label363;
                                    case 8820007:
                                        break;
                                    default:
                                        break label363;
                                }
                            }

                            switch (monster.getMap().getId()) {
                                case 220080001:
                                    if (xpos < -890) {
                                        xpos = (int) (-890.0 + Math.ceil(Math.random() * 150.0));
                                    } else if (xpos > 230) {
                                        xpos = (int) (230.0 - Math.ceil(Math.random() * 150.0));
                                    }
                                    break;
                                case 230040420:
                                    if (xpos < -239) {
                                        xpos = (int) (-239.0 + Math.ceil(Math.random() * 150.0));
                                    } else if (xpos > 371) {
                                        xpos = (int) (371.0 - Math.ceil(Math.random() * 150.0));
                                    }
                            }
                            //怪物释放技能
                            monster.getMap().spawnMonsterWithEffect(toSpawn, this.getSpawnEffect(), monster.getMap().calcPointBelow(new Point(xpos, ypos - 1)));
                        }
            default: {
                break;
            }
        }
        //赋予怪物技能
            if (stats.size() > 0 && monster != null) {
                if (this.lt != null && this.rb != null && skill) {
                    //群体buff
                    for (final MapleMapObject mons2 : this.getObjectsInRange(monster, MapleMapObjectType.MONSTER)) {
                        ((MapleMonster)mons2).applyMonsterBuff(stats, this.getX(), this.getSkillId(), this.getDuration(), this, reflection);
                    }
                }else {
                    //单体buff
                    monster.applyMonsterBuff(stats, this.getX(), this.getSkillId(), this.getDuration(), this, reflection);
                }
            }
            if (disease != null) {
                if (this.lt != null && this.rb != null && skill && monster != null) {
                    for (MapleCharacter chr2 : this.getPlayersInRange(monster, player)) {
                        chr2.getDiseaseBuff(disease, this);
                    }
                }
                else {
                    player.getDiseaseBuff(disease, this);
                }
            }
            if (monster != null) {
                monster.setMp(monster.getMp() - this.getMpCon());
            }
        } catch (Exception e) {}

    }
    public boolean isCanAvoidSkill(int skillId) {
        switch (skillId) {
            case 120:
            case 121:
            case 122:
            case 123:
            case 124:
            case 125:
            case 126:
            case 127:
            case 128:
            case 131:
            case 132:
            case 133:
            case 135:
            case 136:
            case 137:
            case 183:
            case 186:
            case 187:
            case 188:
                return true;
            case 129:
            case 130:
            case 134:
            case 138:
            case 139:
            case 140:
            case 141:
            case 142:
            case 143:
            case 144:
            case 145:
            case 146:
            case 147:
            case 148:
            case 149:
            case 150:
            case 151:
            case 152:
            case 153:
            case 154:
            case 155:
            case 156:
            case 157:
            case 158:
            case 159:
            case 160:
            case 161:
            case 162:
            case 163:
            case 164:
            case 165:
            case 166:
            case 167:
            case 168:
            case 169:
            case 170:
            case 171:
            case 172:
            case 173:
            case 174:
            case 175:
            case 176:
            case 177:
            case 178:
            case 179:
            case 180:
            case 181:
            case 182:
            case 184:
            case 185:
            default:
                return false;
        }
    }
    public int getSkillId() {
        return this.skillId;
    }
    
    public int getSkillLevel() {
        return this.skillLevel;
    }
    
    public int getMpCon() {
        return this.mpCon;
    }
    
    public List<Integer> getSummons() {
        return Collections.unmodifiableList((List<? extends Integer>)this.toSummon);
    }
    
    public int getSpawnEffect() {
        return this.spawnEffect;
    }
    
    public int getHP() {
        return this.hp;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public long getDuration() {
        return this.duration;
    }
    
    public long getCoolTime() {
        return this.cooltime;
    }
    
    public Point getLt() {
        return this.lt;
    }
    
    public Point getRb() {
        return this.rb;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public boolean makeChanceResult() {
        return (double)this.prop >= 1.0 || Math.random() < (double)this.prop;
    }
    
    private Rectangle calculateBoundingBox(final Point posFrom, final boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(this.lt.x + posFrom.x, this.lt.y + posFrom.y);
            myrb = new Point(this.rb.x + posFrom.x, this.rb.y + posFrom.y);
        }
        else {
            myrb = new Point(this.lt.x * -1 + posFrom.x, this.rb.y + posFrom.y);
            mylt = new Point(this.rb.x * -1 + posFrom.x, this.lt.y + posFrom.y);
        }
        final Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }
    
    private List<MapleCharacter> getPlayersInRange(final MapleMonster monster, final MapleCharacter player) {
        final Rectangle bounds = this.calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
        final List<MapleCharacter> players = new ArrayList<MapleCharacter>();
        players.add(player);
        return monster.getMap().getPlayersInRectThreadsafe(bounds, players);
    }
    
    private List<MapleMapObject> getObjectsInRange(final MapleMonster monster, final MapleMapObjectType objectType) {
        final Rectangle bounds = this.calculateBoundingBox(monster.getPosition(), monster.isFacingLeft());
        final List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInRect(bounds, objectTypes);
    }
    
    public void setOnce(final boolean o) {
        this.summonOnce = o;
    }
    
    public boolean onlyOnce() {
        return this.summonOnce;
    }
}
