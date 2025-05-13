package tools.packet;

import java.util.*;

import bean.UserAttraction;
import constants.ServerConfig;
import scripting.NPCConversationManager;
import server.Start;
import tools.MaplePacketCreator;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import server.movement.LifeMovementFragment;

import java.awt.Point;
import server.life.MapleMonster;
import handling.SendPacketOpcode;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;
import util.ListUtil;
import util.RedisUtil;

public class MobPacket
{
    /**
     * 伤害怪物的函数
     * 该函数用于创建一个数据包，指示对游戏中的一个怪物造成伤害
     *
     * @param oid 怪物的唯一标识符，用于在游戏世界中区分不同的怪物
     * @param damage 对怪物造成的伤害值，可以是任何非负整数
     * @return 返回一个包含伤害信息的数据包，用于在网络中传输
     */
    public static byte[] damageMonster(final int oid, final long damage) {
        // 创建一个数据包写入器，用于构建将要发送的数据包
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入数据包的操作码，标识这是一个伤害怪物的操作
        mplew.writeShort((int)SendPacketOpcode.DAMAGE_MONSTER.getValue());

        // 写入怪物的唯一标识符，告诉服务器哪个怪物受到了伤害
        mplew.writeInt(oid);

        // 保留的字节，目前未使用，写入0作为占位符
        mplew.write(0);

        // 如果伤害值超过了整数的最大值，写入整数的最大值作为标记
        if (damage > 2147483647L) {
            mplew.writeInt(Integer.MAX_VALUE);
        }
        else {
            // 否则，写入实际的伤害值
            mplew.writeInt((int)damage);
        }

        // 返回构建完成的数据包
        return mplew.getPacket();
    }

    /**
     * 友好地伤害怪物
     * 该方法用于生成一个数据包，指示客户端对一个怪物造成伤害该方法主要处理与网络数据包相关的编码，
     * 并确保即使伤害值或怪物的HP值超过Integer.MAX_VALUE，也能正确处理
     *
     * @param mob 被伤害的怪物对象
     * @param damage 对怪物造成的伤害值
     * @param display 是否在客户端显示伤害值
     * @return 包含伤害信息的数据包
     */
    public static byte[] damageFriendlyMob(final MapleMonster mob, final long damage, final boolean display) {
        // 创建一个数据包编码器
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入伤害怪物的数据包标识符
        mplew.writeShort((int)SendPacketOpcode.DAMAGE_MONSTER.getValue());

        // 写入怪物的唯一对象ID
        mplew.writeInt(mob.getObjectId());

        // 根据display参数决定是否显示伤害值
        mplew.write(display ? 1 : 2);

        // 如果伤害值超过Integer.MAX_VALUE，则写入最大整数值，否则写入实际伤害值
        if (damage > 2147483647L) {
            mplew.writeInt(Integer.MAX_VALUE);
        }
        else {
            mplew.writeInt((int)damage);
        }

        // 如果怪物的当前HP值超过Integer.MAX_VALUE，则写入一个比例值，否则写入实际HP值
        if (mob.getHp() > 2147483647L) {
            mplew.writeInt((int)((double)mob.getHp() / (double)mob.getMobMaxHp() * 2.147483647E9));
        }
        else {
            mplew.writeInt((int)mob.getHp());
        }

        // 如果怪物的最大HP值超过Integer.MAX_VALUE，则写入最大整数值，否则写入实际最大HP值
        if (mob.getMobMaxHp() > 2147483647L) {
            mplew.writeInt(Integer.MAX_VALUE);
        }
        else {
            mplew.writeInt((int)mob.getMobMaxHp());
        }

        // 返回编码后的数据包
        return mplew.getPacket();
    }
    
    public static byte[] killMonster(final int oid, final int animation) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.KILL_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(animation);
        return mplew.getPacket();
    }

    //治愈怪物
    public static byte[] healMonster(final int oid, final int heal) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.DAMAGE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.writeInt(-heal);
        return mplew.getPacket();
    }
    //显示怪物血量
    public static byte[] showMonsterHP(final int oid, final int remhppercentage) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SHOW_MONSTER_HP.getValue());
        mplew.writeInt(oid);
        mplew.write(remhppercentage);
        return mplew.getPacket();
    }
    
    public static byte[] showBossHP(final MapleMonster mob) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(mob.getId());
        if (mob.getHp() > 2147483647L) {
            mplew.writeInt((int)((double)mob.getHp() / (double)mob.getMobMaxHp() * 2.147483647E9));
        }
        else {
            mplew.writeInt((int)mob.getHp());
        }
        if (mob.getMobMaxHp() > 2147483647L) {
            mplew.writeInt(Integer.MAX_VALUE);
        }
        else {
            mplew.writeInt((int)mob.getMobMaxHp());
        }
        mplew.write(mob.getStats().getTagColor());
        mplew.write(mob.getStats().getTagBgColor());
        return mplew.getPacket();
    }
    
    public static byte[] showBossHP(final int monsterId, final long currentHp, final long maxHp) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.BOSS_ENV.getValue());
        mplew.write(5);
        mplew.writeInt(monsterId);
        if (currentHp > 2147483647L) {
            mplew.writeInt((int)((double)currentHp / (double)maxHp * 2.147483647E9));
        }
        else {
            mplew.writeInt((int)((currentHp <= 0L) ? -1L : currentHp));
        }
        if (maxHp > 2147483647L) {
            mplew.writeInt(Integer.MAX_VALUE);
        }
        else {
            mplew.writeInt((int)maxHp);
        }
        mplew.write(6);
        mplew.write(5);
        return mplew.getPacket();
    }
    
    public static byte[] moveMonster(final boolean useskill, final int skill, final int unk, final int oid, final Point startPos, final Point endPos, final List<LifeMovementFragment> moves) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.write(0);
        mplew.write((int)(useskill ? 1 : 0));
        mplew.write(skill);
        mplew.writeInt(unk);
        mplew.writePos(startPos);
        serializeMovementList(mplew, moves);
        return mplew.getPacket();
    }
    /**
     * 移动怪物的函数
     *
     * @param useskill 是否使用技能
     * @param skill 技能ID
     * @param skill1 第一个额外技能ID
     * @param skill2 第二个额外技能ID
     * @param skill3 第三个额外技能ID
     * @param skill4 第四个额外技能ID
     * @param oid 怪物对象ID
     * @param startPos 怪物的起始位置
     * @param endPos 怪物的目标位置
     * @param moves 怪物的移动片段列表
     * @return 移动怪物的封包数据
     *
     * 根据服务器版本选择不同的移动怪物实现方式如果服务器版本为85，
     * 则调用另一个重载的moveMonster函数；否则，构建封包并序列化怪物的移动信息
     */
    public static byte[] moveMonster(boolean useskill, int skill, int skill1, int skill2, int skill3, int skill4, int oid, Point startPos, Point endPos, List<LifeMovementFragment> moves) {
        // 根据服务器版本选择合适的处理方式
        if (ServerConfig.version == 85) {
            // 对于版本85，使用另一种重载的moveMonster方法
            return moveMonster(useskill, skill, 0, oid, startPos, endPos, moves, (List)null, (List)null);
        } else {
            // 创建封包写入器
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

            // 写入移动怪物的操作码
            mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());
            // 写入怪物对象ID
            mplew.writeInt(oid);
            // 未知字节，固定值为0
            mplew.write(0);
            // 写入是否使用技能
            mplew.write(useskill ? 1 : 0);
            // 写入技能ID
            mplew.write(skill);
            // 写入额外的技能ID
            mplew.write(skill1);
            mplew.write(skill2);
            mplew.write(skill3);
            mplew.write(skill4);
            // 写入起始位置
            mplew.writePos(startPos);
            // 序列化移动片段列表
            serializeMovementList(mplew, moves);
            // 返回构建好的封包数据
            return mplew.getPacket();
        }
    }

    /**
     * 构建移动怪物的网络数据包
     *
     * @param useskill 是否使用技能
     * @param skill 技能ID，如果未使用技能则此值无意义
     * @param unk 未知整数参数，用于控制移动行为
     * @param oid 怪物对象ID
     * @param startPos 怪物的起始位置
     * @param endPos 怪物的目标位置
     * @param moves 怪物移动动作片段列表
     * @param unk2 未知整数列表，可能与移动路径有关
     * @param unk3 未知整数对列表，可能与技能或移动有关
     * @return 返回构建好的移动怪物数据包
     */
    public static byte[] moveMonster(boolean useskill, int skill, int unk, int oid, Point startPos, Point endPos, List<LifeMovementFragment> moves, List<Integer> unk2, List<Pair<Integer, Integer>> unk3) {
        // 创建数据包写入器
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入移动怪物操作码
        mplew.writeShort(SendPacketOpcode.MOVE_MONSTER.getValue());

        // 写入怪物对象ID
        mplew.writeInt(oid);

        // 以下两个字节未知，可能为未来扩展保留
        mplew.write(0);
        mplew.write(0);

        // 写入是否使用技能的标志
        mplew.write(useskill ? 1 : 0);

        // 写入技能ID，即使未使用技能也会写入一个值
        mplew.write(skill);

        // 写入未知整数参数
        mplew.writeInt(unk);

        // 写入未知整数对列表的长度
        mplew.writeInt(unk3 == null ? 0 : unk3.size());

        // 如果未知整数对列表不为空，则遍历并写入每个整数对
        Iterator var10;
        if (unk3 != null) {
            var10 = unk3.iterator();

            while(var10.hasNext()) {
                Pair<Integer, Integer> i = (Pair)var10.next();
                mplew.writeInt((Integer)i.left);
                mplew.writeInt((Integer)i.right);
            }
        }

        // 写入未知整数列表的长度
        mplew.writeInt(unk2 == null ? 0 : unk2.size());

        // 如果未知整数列表不为空，则遍历并写入每个整数
        if (unk2 != null) {
            var10 = unk2.iterator();

            while(var10.hasNext()) {
                Integer i = (Integer)var10.next();
                mplew.writeInt(i);
            }
        }

        // 写入怪物的起始位置
        mplew.writePos(startPos);

        // 序列化并写入怪物移动动作片段列表
        PacketHelper.serializeMovementList(mplew, moves);

        // 返回构建好的数据包
        return mplew.getPacket();
    }

    private static void serializeMovementList(final MaplePacketLittleEndianWriter lew, final List<LifeMovementFragment> moves) {
        lew.write(moves.size());
        for (final LifeMovementFragment move : moves) {
            move.serialize(lew);
        }
    }

    /**
     * 地图自动刷怪逻辑
     * @param life
     * @param spawnType
     * @param effect
     * @param link
     * @return
     */
    public static byte[] spawnMonster(final MapleMonster life, final int spawnType, final int effect, final int link) {
     UserAttraction userAttraction = NPCConversationManager.getAttractList(life.getMap().getChannel(), life.getMap().getId());
//     if (Objects.isNull(userAttraction)) {
//          userAttraction = NPCConversationManager.getAttractLhList(life.getMap().getChannel(), life.getMap().getId());
//     }
        if(ListUtil.isNotEmpty(Start.mobInfoMap.get(life.getId()))){
            life.setHp(Start.mobInfoMap.get(life.getId()).get(0).getHp());
            life.setMp(Start.mobInfoMap.get(life.getId()).get(0).getMp());
            life.getStats().setHp(Start.mobInfoMap.get(life.getId()).get(0).getHp());
            life.getStats().setMp(Start.mobInfoMap.get(life.getId()).get(0).getMp());
            life.getStats().setLevel((short)Start.mobInfoMap.get(life.getId()).get(0).getLevel());
            life.getStats().setBoss(Start.mobInfoMap.get(life.getId()).get(0).getBoss());
        }



        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.SPAWN_MONSTER.getValue());
        mplew.writeInt(life.getObjectId());
        mplew.write(1);
        mplew.writeInt(life.getId());
        addMonsterStatus(mplew, life);
       if (Objects.nonNull(userAttraction)) {
           mplew.writeShort(userAttraction.getPosition().x);
           mplew.writeShort(userAttraction.getPosition().y);
       }else {
         mplew.writeShort(life.getPosition().x);
         mplew.writeShort(life.getPosition().y);
       }
        mplew.write(life.getStance());
        mplew.writeShort(0);
        mplew.writeShort(life.getFh());
        if (effect != 0 || link != 0) {
            mplew.write((effect != 0) ? effect : -3);
            mplew.writeInt(link);
        }
        else {
            if (spawnType == 0) {
                mplew.writeInt(effect);
            }
            mplew.write(spawnType);
        }
        mplew.write(life.getCarnivalTeam());
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    private static void writeMaskFromList(final MaplePacketLittleEndianWriter mplew, final Collection<MonsterStatusEffect> ss) {
        final int[] mask = new int[4];
        for (final MonsterStatusEffect statup : ss) {
            final int[] array = mask;
            final int position = statup.getStatus().getPosition();
            array[position] |= statup.getStatus().getValue();
        }
        for (int i = 0; i < mask.length; ++i) {
            mplew.writeInt(mask[i]);
        }
    }

    /**
     * 添加怪物状态信息到数据包中
     * 此方法用于将怪物的当前状态效果编码到一个MaplePacketLittleEndianWriter中，以便在网络中传输
     * 如果怪物没有状态效果，会添加一个空的状态效果以表示该怪物当前没有被任何状态影响
     *
     * @param mplew 用于写入怪物状态数据的MaplePacketLittleEndianWriter实例
     * @param life 表示怪物的实例，其状态将被编码并添加到mplew中
     */
    public static void addMonsterStatus(final MaplePacketLittleEndianWriter mplew, final MapleMonster life) {
        // 检查怪物是否没有任何状态效果，如果是，则添加一个空的状态效果
        if (life.getStati().size() <= 0) {
            life.addEmpty();
        }
        // 创建一个LinkedList来存储怪物的所有状态效果，以便于后续的编码操作
        final LinkedList<MonsterStatusEffect> buffs = new LinkedList<MonsterStatusEffect>((Collection<? extends MonsterStatusEffect>)life.getStati().values());
        // 调用方法将怪物的状态效果编码到mplew中，以便在网络中传输
        EncodeTemporary(mplew, (List<MonsterStatusEffect>)buffs);
    }


    /**
     * 生成控制怪物的网络数据包
     * 此方法用于创建一个控制怪物行为的数据包，包括是否新生成的怪物、是否具有攻击性，以及怪物的各种状态
     *
     * @param life 表示怪物的对象，包含怪物的相关信息和状态
     * @param newSpawn 指示怪物是否是新生成的
     * @param aggro 指示怪物是否具有攻击性
     * @return 返回控制怪物的网络数据包
     */
    public static byte[] controlMonster(final MapleMonster life, final boolean newSpawn, final boolean aggro) {
        // 创建一个网络数据包写入器
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入控制怪物的指令代码
        mplew.writeShort((int)SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());

        // 写入怪物的行为模式，2为攻击性，1为非攻击性
        mplew.write(aggro ? 2 : 1);

        // 写入怪物的对象ID
        mplew.writeInt(life.getObjectId());

        // 写入一个未知的固定值
        mplew.write(1);

        // 写入怪物的ID
        mplew.writeInt(life.getId());

        // 添加怪物的状态信息到数据包
        addMonsterStatus(mplew, life);

        // 写入怪物的位置坐标
        mplew.writeShort(life.getPosition().x);
        mplew.writeShort(life.getPosition().y);

        // 写入怪物的姿势
        mplew.write(life.getStance());

        // 写入怪物的头部高度，用于计算视角
        mplew.writeShort(life.getFh());
        mplew.writeShort(life.getFh());

        // 根据怪物是否是假的，以及是否是新生成的，写入不同的值
        mplew.write(life.isFake() ? 252 : (newSpawn ? -2 : -1));

        // 写入怪物所属的狂欢队伍信息
        mplew.write(life.getCarnivalTeam());

        // 写入一个未知的固定值
        mplew.writeInt(0);

        // 如果怪物的ID以961开头，写入一个空字符串
        if (life.getId() / 10000 == 961) {
            mplew.writeAsciiString("");
        }

        // 返回控制怪物的网络数据包
        return mplew.getPacket();
    }

    /**
     * 生成停止控制怪物的网络包
     * 此方法用于创建一个指令包，通知服务器停止控制特定的怪物
     *
     * @param oid 怪物的唯一标识符，用于指定需要停止控制的怪物
     * @return 返回包含指令的字节数组，用于发送给服务器
     */
    public static byte[] stopControllingMonster(final int oid) {
        // 创建一个网络包写入器实例
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入指令码，指示这是一个停止控制怪物的指令
        mplew.writeShort((int)SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());

        // 写入控制标记，0表示停止控制
        mplew.write(0);

        // 写入怪物的唯一标识符，指定需要停止控制的怪物
        mplew.writeInt(oid);

        // 返回构建好的指令包
        return mplew.getPacket();
    }


    /**
     * 生成使怪物隐形的指令包
     * 此方法用于创建一个指令包，其效果是使指定的怪物在游戏地图上变得隐形
     * 它通过编写特定的操作码和怪物的信息来实现这一功能
     *
     * @param life 即将变得隐形的怪物对象，不能为空
     * @return 返回包含编写的指令包的字节数组
     */
    public static byte[] makeMonsterInvisible(final MapleMonster life) {
        // 创建一个MaplePacketLittleEndianWriter对象用于编写指令包
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入操作码以标识这是一个生成怪物控制的指令包
        mplew.writeShort((int)SendPacketOpcode.SPAWN_MONSTER_CONTROL.getValue());

        // 写入一个字节表示怪物的可见状态，0表示隐形
        mplew.write(0);

        // 写入怪物的ObjectId，这是一个唯一标识符，用于确定游戏中的特定对象
        mplew.writeInt(life.getObjectId());

        // 返回编写的指令包的字节数组表示形式
        return mplew.getPacket();
    }

    /**
     * 将怪物对象转换为实际的游戏包
     * 此方法用于创建一个怪物在游戏中真实出现所需的字节流
     * 它通过MaplePacketLittleEndianWriter来构造这个字节流，其中包括了怪物的各种属性
     *
     * @param life 怪物对象，包含怪物的相关信息，如ID、位置等
     * @return 包含怪物信息的字节数组，用于在游戏中生成怪物
     */
    public static byte[] makeMonsterReal(final MapleMonster life) {
        // 创建一个MaplePacketLittleEndianWriter实例，用于构建字节流
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();

        // 写入怪物生成的操作码
        mplew.writeShort((int)SendPacketOpcode.SPAWN_MONSTER.getValue());

        // 写入怪物的唯一对象ID
        mplew.writeInt(life.getObjectId());

        // 写入一个固定值1，可能是协议的一部分或标志
        mplew.write(1);

        // 写入怪物的ID
        mplew.writeInt(life.getId());

        // 添加怪物的状态信息
        addMonsterStatus(mplew, life);

        // 写入怪物的X坐标
        mplew.writeShort(life.getPosition().x);

        // 写入怪物的Y坐标
        mplew.writeShort(life.getPosition().y);

        // 写入怪物的姿态
        mplew.write(life.getStance());

        // 写入一个固定值0，可能是保留字段或未使用的属性
        mplew.writeShort(0);

        // 写入怪物所站的地板高度
        mplew.writeShort(life.getFh());

        // 写入一个固定值-1，可能是协议的一部分或特殊标志
        mplew.writeShort(-1);

        // 写入一个固定值0，可能是保留字段或未使用的属性
        mplew.writeInt(0);

        // 返回构建好的字节流
        return mplew.getPacket();
    }


    public static byte[] moveMonsterResponse(final int objectid, final short moveid, final int currentMp, final boolean useSkills, final int skillId, final int skillLevel) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.MOVE_MONSTER_RESPONSE.getValue());
        mplew.writeInt(objectid);
        mplew.writeShort((int)moveid);
        mplew.write((int)(useSkills ? 1 : 0));
        mplew.writeShort(currentMp);
        mplew.write(skillId);
        mplew.write(skillLevel);
        return mplew.getPacket();
    }
    
    public static void SingleProcessStatSet(final MaplePacketLittleEndianWriter mplew, final MonsterStatusEffect buff) {
        final List<MonsterStatusEffect> ss = new LinkedList<MonsterStatusEffect>();
        ss.add(buff);
        ProcessStatSet(mplew, ss);
    }
    
    public static void EncodeTemporary(final MaplePacketLittleEndianWriter mplew, final List<MonsterStatusEffect> buffs) {
        final Set<MonsterStatus> mobstat = new HashSet<MonsterStatus>();
        writeMaskFromList(mplew, (Collection<MonsterStatusEffect>)buffs);
        Collections.sort(buffs, (Comparator<? super MonsterStatusEffect>)new Comparator<MonsterStatusEffect>() {
            @Override
            public int compare(final MonsterStatusEffect o1, final MonsterStatusEffect o2) {
                final int val1 = o1.getStatus().getOrder();
                final int val2 = o2.getStatus().getOrder();
                return (val1 < val2) ? -1 : ((val1 == val2) ? 0 : 1);
            }
        });
        final Collection<MonsterStatus> buffstatus = new LinkedList<MonsterStatus>();
        for (final MonsterStatusEffect buff : buffs) {
            buffstatus.add(buff.getStatus());
            if (buff.getStatus() != MonsterStatus.EMPTY) {
                mplew.writeShort((int)buff.getX());
                if (buff.getMobSkill() != null) {
                    mplew.writeShort(buff.getMobSkill().getSkillId());
                    mplew.writeShort(buff.getMobSkill().getSkillLevel());
                }
                else if (buff.getSkill() > 0) {
                    mplew.writeInt((buff.getSkill() > 0) ? buff.getSkill() : 0);
                }
                mplew.writeShort((int)(short)(int)((buff.getCancelTask() - System.currentTimeMillis()) / 1000L));
            }
        }
        if (buffstatus.contains((Object)MonsterStatus.EMPTY)) {
            final int result = 0;
            mplew.writeInt(result);
            for (int i = 0; i < result; ++i) {
                mplew.writeInt(0);
                mplew.writeInt(0);
                mplew.writeInt(0);
            }
        }
        if (buffstatus.contains((Object)MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
            mplew.writeInt(0);
        }
        if (buffstatus.contains((Object)MonsterStatus.MAGIC_DAMAGE_REFLECT)) {
            mplew.writeInt(0);
        }
        if (buffstatus.contains((Object)MonsterStatus.WEAPON_DAMAGE_REFLECT)) {
            mplew.writeInt(0);
        }
        if (buffstatus.contains((Object)MonsterStatus.SUMMON)) {
            mplew.write(0);
            mplew.write(0);
        }
    }
    
    public static void ProcessStatSet(final MaplePacketLittleEndianWriter mplew, final List<MonsterStatusEffect> buffs) {
        EncodeTemporary(mplew, buffs);
        mplew.writeShort(2);
        mplew.write(1);
        mplew.write(1);
    }
    
    public static byte[] applyMonsterStatus(final MapleMonster mons, final MonsterStatusEffect ms) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(mons.getObjectId());
        SingleProcessStatSet(mplew, ms);
        return mplew.getPacket();
    }
    
    public static byte[] applyMonsterStatus(final MapleMonster mons, final List<MonsterStatusEffect> mse) {
        if (mse.size() <= 0 || mse.get(0) == null) {
            return MaplePacketCreator.enableActions();
        }
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.APPLY_MONSTER_STATUS.getValue());
        mplew.writeInt(mons.getObjectId());
        ProcessStatSet(mplew, mse);
        return mplew.getPacket();
    }
    
    public static byte[] cancelMonsterStatus(final MapleMonster mons, final MonsterStatusEffect ms) {
        final List<MonsterStatusEffect> mse = new ArrayList<MonsterStatusEffect>();
        mse.add(ms);
        return cancelMonsterStatus(mons, mse);
    }
    
    public static byte[] cancelMonsterStatus(final MapleMonster mons, final List<MonsterStatusEffect> mse) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.CANCEL_MONSTER_STATUS.getValue());
        mplew.writeInt(mons.getObjectId());
        writeMaskFromList(mplew, (Collection<MonsterStatusEffect>)mse);
        final boolean cond = false;
        if (cond) {
            final int v6 = 0;
            mplew.writeInt(v6);
            for (int i = 0; i < v6; ++i) {
                mplew.writeInt(0);
            }
        }
        mplew.writeInt(0);
        return mplew.getPacket();
    }
    
    public static byte[] talkMonster(final int oid, final int itemId, final String msg) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.TALK_MONSTER.getValue());
        mplew.writeInt(oid);
        mplew.writeInt(500);
        mplew.writeInt(itemId);
        mplew.write((int)((itemId > 0) ? 1 : 0));
        mplew.write((int)((msg != null && msg.length() > 0) ? 1 : 0));
        if (msg != null && msg.length() > 0) {
            mplew.writeMapleAsciiString(msg);
        }
        mplew.writeInt(1);
        return mplew.getPacket();
    }
    
    public static byte[] removeTalkMonster(final int oid) {
        final MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort((int)SendPacketOpcode.REMOVE_TALK_MONSTER.getValue());
        mplew.writeInt(oid);
        return mplew.getPacket();
    }
}
