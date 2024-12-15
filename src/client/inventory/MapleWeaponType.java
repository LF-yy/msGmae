package client.inventory;

public enum MapleWeaponType
{
    没有武器(0.0F, 0),
    闪亮克魯(1.2F, 25),
    灵魂射手(1.7F, 15),
    魔剑(1.3F, 20),
    能量剑(1.3125F, 20),
    幻兽棍棒(1.34F, 20),
    单手剑(1.2F, 20),
    单手斧(1.2F, 20),
    单手棍(1.2F, 20),
    短剑(1.3F, 20),
    双刀(1.3F, 20),
    手杖(1.3F, 20),
    短杖(1.0F, 25),
    长杖(1.0F, 25),
    双手剑(1.34F, 20),
    双手斧(1.34F, 20),
    双手棍(1.34F, 20),
    枪(1.49F, 20),
    矛(1.49F, 20),
    弓(1.3F, 15),
    弩(1.35F, 15),
    拳套(1.75F, 15),
    指虎(1.7F, 20),
    火枪(1.5F, 15),
    双弩枪(1.3F, 15),
    加農炮(1.5F, 15),
    太刀(1.25F, 20),
    扇子(1.35F, 25),
    琉(1.49F, 20),
    璃(1.34F, 20),
    ESP限制器(1.0F, 20),
    未知(0.0F, 0);
    
    private final float damageMultiplier;
    private final int baseMastery;
    
    private MapleWeaponType(final float maxDamageMultiplier, final int baseMastery) {
        this.damageMultiplier = maxDamageMultiplier;
        this.baseMastery = baseMastery;
    }
    
    public final float getMaxDamageMultiplier() {
        return this.damageMultiplier;
    }
    
    public final int getBaseMastery() {
        return this.baseMastery;
    }
}
