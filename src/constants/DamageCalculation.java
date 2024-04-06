package constants;

import client.MapleCharacter;

import java.math.BigInteger;

public class DamageCalculation {

    //面板上限=（主属性*4+副属性）*武器系数*攻击力总和/100
    //输出上限=面板上限*总伤项系数*技能伤害系数
    //总伤项系数=技能总伤害+潜能总伤害+BOSS伤害

    public static BigInteger damageInfo(MapleCharacter mc){
        //面板伤害
       return new BigInteger("22");
    }
}
