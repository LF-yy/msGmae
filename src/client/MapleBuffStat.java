
package client;

import java.io.Serializable;

/**
 * 枚举类，用于表示枫之谷游戏中的各种缓冲状态统计属性
 * 实现了Serializable接口，以支持序列化
 */
public enum MapleBuffStat implements Serializable
{
    // 物理攻击
    WATK(0),
    // 物理防御
    WDEF(1),
    // 魔法攻击
    MATK(2),
    // 魔法防御
    MDEF(3),
    // 准确度
    ACC(4),
    // 闪避
    AVOID(5),
    // 持有物品数量
    HANDS(6),
    // 速度
    SPEED(7),
    // 跳跃力
    JUMP(8),
    // 魔法护盾
    MAGIC_GUARD(9),
    // 黑暗视觉
    DARKSIGHT(10),
    // 增速剂
    BOOSTER(11),
    // 力量护盾
    POWERGUARD(12),
    // 最大生命值
    MAXHP(13),
    // 最大魔法值
    MAXMP(14),
    // 无敌状态
    INVINCIBLE(15),
    // 灵魂箭
    SOULARROW(16),
    // 昏迷
    STUN(17),
    // 中毒
    POISON(18),
    // 封印
    SEAL(19),
    // 黑暗
    DARKNESS(20),
    // 连击
    COMBO(21),
    // 召唤
    SUMMON(21),
    // 战争冲锋
    WK_CHARGE(22),
    // 龙血
    DRAGONBLOOD(23),
    // 圣符
    HOLY_SYMBOL(24),
    // 金币上升
    MESOUP(25),
    // 影子伙伴
    SHADOWPARTNER(26),
    // 捡钱高手
    PICKPOCKET(27),
    // 傀儡
    PUPPET(28),
    // 金币护盾
    MESOGUARD(29),
    // 削弱
    WEAKEN(30),
    // 诅咒
    CURSE(31),
    // 减速
    SLOW(32),
    // 变形
    MORPH(33),
    // 恢复
    RECOVERY(34),
    // 生命值损失防护
    HP_LOSS_GUARD(34),
    // 枫之勇士
    MAPLE_WARRIOR(35),
    // 姿态
    STANCE(36),
    // 锐利目光
    SHARP_EYES(37),
    // 魔法反射
    MANA_REFLECTION(38),
    // 龙之咆哮
    DRAGON_ROAR(39),
    // 精神之爪
    SPIRIT_CLAW(40),
    // 无限
    INFINITY(41),
    // 圣盾
    HOLY_SHIELD(42),
    // 绊脚
    HAMSTRING(43),
    // 盲目
    BLIND(44),
    // 集中
    CONCENTRATE(45),
    // 僵尸化
    ZOMBIFY(46),
    // 英雄之魂
    ECHO_OF_HERO(47),
    // 未知3
    UNKNOWN3(48),
    // 金币掉落数量
    MESO_RATE(48),
    // 幽灵变形
    GHOST_MORPH(49),
    // 阿里安特竞技场免疫
    ARIANT_COSS_IMU(50),
    // 掉落率
    DROP_RATE(52),
    // 经验率
    EXPRATE(54),
    // A币获取率
    ACASH_RATE(55),
    // GM隐藏
    GM_HIDE(56),
    // 未知7
    UNKNOWN7(57),
    // 幻影
    ILLUSION(58),
    // 暴怒
    BERSERK_FURY(57),
    // 神圣之体
    DIVINE_BODY(60),
    // 电击
    SPARK(59),
    // 阿里安特竞技场免疫2
    ARIANT_COSS_IMU2(62),
    // 最终攻击
    FINALATTACK(61),
    // 元素重置
    ELEMENT_RESET(63),
    // 风行
    WIND_WALK(64),
    // 阿兰连击
    ARAN_COMBO(66),
    // 连击吸血
    COMBO_DRAIN(67),
    // 连击护盾
    COMBO_BARRIER(68),
    // 体压
    BODY_PRESSURE(69),
    // 智能击退
    SMART_KNOCKBACK(70),
    // 灵魂之石
    SOUL_STONE(73),
    // 能量充电
    ENERGY_CHARGE(77),
    // 冲刺速度
    DASH_SPEED(78),
    // 冲刺跳跃
    DASH_JUMP(79),
    // 怪物骑乘
    MONSTER_RIDING(80),
    // 速度注入
    SPEED_INFUSION(81),
    // 定位信标
    HOMING_BEACON(82),
    // 飞翔
    SOARING(82),
    // 冻结
    FREEZE(83),
    // 闪电充电
    LIGHTNING_CHARGE(84),
    // 镜像
    MIRROR_IMAGE(85),
    // 猫头鹰精神
    OWL_SPIRIT(86),
    // 收割者
    REAPER(2048),
    // 影子伙伴X
    SHADOWPARTNERX(83),
    // 召唤玩家1
    召唤玩家1(77),
    // 召唤玩家2
    召唤玩家2(78),
    // 召唤玩家3
    召唤玩家3(79),
    // 召唤玩家4
    召唤玩家4(80),
    // 召唤玩家5
    召唤玩家5(81),
    // 召唤玩家6
    召唤玩家6(82),
    // 召唤玩家7
    召唤玩家7(83),
    召唤玩家8(84);
    // 序列化ID
    private static final long serialVersionUID = 0L;
    // 缓冲状态的值
    private final int buffstat;
    // 第一个整数的位置
    private final int first;
    // 旧的数值
    private final long oldvalue;

    /**
     * 构造函数，初始化缓冲状态
     * @param buffstat 缓冲状态的值
     */
    private MapleBuffStat(final int buffstat) {
        this.buffstat = 1 << buffstat % 32;
        this.first = 3 - (int)Math.floor((double)(buffstat / 32));
        this.oldvalue = (long)new Long((long)this.buffstat) << 32 * (this.first % 2 + 1);
    }

    /**
     * 构造函数，初始化带有堆叠状态的缓冲状态
     * @param buffstat 缓冲状态的值
     * @param stacked 是否堆叠
     */
    private MapleBuffStat(final int buffstat, final boolean stacked) {
        this.buffstat = 1 << buffstat % 32;
        this.first = (int)Math.floor((double)(buffstat / 32));
        this.oldvalue = (long)new Long((long)this.buffstat) << 32 * (this.first % 2 + 1);
    }

    /**
     * 获取旧的数值
     * @return 旧的数值
     */
    public final long getOldValue() {
        return this.oldvalue;
    }

    /**
     * 获取第一个整数的位置
     * @return 第一个整数的位置
     */
    public final int getPosition() {
        return this.first;
    }

    /**
     * 根据是否从零开始获取位置
     * @param fromZero 是否从零开始
     * @return 位置
     */
    public final int getPosition(final boolean fromZero) {
        if (!fromZero) {
            return this.first;
        }
        switch (this.first) {
            case 4: {
                return 0;
            }
            case 3: {
                return 1;
            }
            case 2: {
                return 2;
            }
            case 1: {
                return 3;
            }
            default: {
                return 0;
            }
        }
    }

    /**
     * 获取缓冲状态的值
     * @return 缓冲状态的值
     */
    public final int getValue() {
        return this.buffstat;
    }
}