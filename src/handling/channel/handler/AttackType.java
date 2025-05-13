package handling.channel.handler;

/**
 * 定义攻击类型的枚举类
 * 该枚举类用于表示不同的攻击方式，包括远程攻击和近战攻击，以及特定角色的组合攻击方式
 */
public enum AttackType
{
    // 近战攻击
    NON_RANGED,
    // 远程攻击
    RANGED,
    // 与影子伙伴一起进行的远程攻击
    RANGED_WITH_SHADOWPARTNER,
    // 使用镜子角色的近战攻击
    NON_RANGED_WITH_MIRROR;
}
