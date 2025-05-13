package server.life;

import java.util.HashMap;
import provider.MapleDataProviderFactory;
import provider.MapleData;
import provider.MapleDataTool;
import tools.StringUtil;
import tools.Pair;
import java.util.Map;
import provider.MapleDataProvider;

public class MobAttackInfoFactory
{
    private static final MobAttackInfoFactory instance;
    private static final MapleDataProvider dataSource;
    private static final Map<Pair<Integer, Integer>, MobAttackInfo> mobAttacks;
    
    public static MobAttackInfoFactory getInstance() {
        return MobAttackInfoFactory.instance;
    }

    /**
     * 获取怪物攻击信息
     *
     * @param mob    怪物对象，用于获取怪物的ID
     * @param attack 攻击类型，用于区分怪物的不同攻击方式
     * @return MobAttackInfo 返回怪物的攻击信息对象，包含攻击属性如致命攻击、MP燃烧等
     */
    public MobAttackInfo getMobAttackInfo(final MapleMonster mob, final int attack) {
        // 尝试从缓存中获取怪物的攻击信息
        MobAttackInfo ret = (MobAttackInfo)MobAttackInfoFactory.mobAttacks.get((Object)new Pair((Object)Integer.valueOf(mob.getId()), (Object)Integer.valueOf(attack)));
        if (ret != null) {
            return ret;
        }

        // 如果缓存中未找到，从数据源中获取怪物数据
        MapleData mobData = MobAttackInfoFactory.dataSource.getData(StringUtil.getLeftPaddedStr(Integer.toString(mob.getId()) + ".img", '0', 11));
        if (mobData != null) {
            // 检查是否有链接到其他怪物的数据
            final MapleData infoData = mobData.getChildByPath("info/link");
            if (infoData != null) {
                final String linkedmob = MapleDataTool.getString("info/link", mobData);
                mobData = MobAttackInfoFactory.dataSource.getData(StringUtil.getLeftPaddedStr(linkedmob + ".img", '0', 11));
            }

            // 根据攻击类型获取具体的攻击数据
            final MapleData attackData = mobData.getChildByPath("attack" + (attack + 1) + "/info");
            if (attackData != null) {
                ret = new MobAttackInfo();
                // 设置攻击是否为致命攻击
                ret.setDeadlyAttack(attackData.getChildByPath("deadlyAttack") != null);
                // 设置MP燃烧效果
                ret.setMpBurn(MapleDataTool.getInt("mpBurn", attackData, 0));
                // 设置疾病技能ID
                ret.setDiseaseSkill(MapleDataTool.getInt("disease", attackData, 0));
                // 设置疾病技能等级
                ret.setDiseaseLevel(MapleDataTool.getInt("level", attackData, 0));
                // 设置MP消耗
                ret.setMpCon(MapleDataTool.getInt("conMP", attackData, 0));
            }
        }

        // 将获取到的攻击信息存入缓存
        MobAttackInfoFactory.mobAttacks.put(new Pair<Integer, Integer>(Integer.valueOf(mob.getId()), Integer.valueOf(attack)), ret);
        return ret;
    }
    
    static {
        instance = new MobAttackInfoFactory();
        dataSource = MapleDataProviderFactory.getDataProvider("Mob.wz");
        mobAttacks = new HashMap<Pair<Integer, Integer>, MobAttackInfo>();
    }
}
