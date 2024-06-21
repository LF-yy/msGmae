package bean;

/**
 * '超级技能'
 */
public class SuperSkills {
    private long id;// bigint(20) NOT NULL AUTO_INCREMENT,
    private int characterid;// int(11) NOT NULL COMMENT '角色id',
    private int skillid;// int(11) NOT NULL COMMENT '技能编号',
    private int itemid;// int(11) NOT NULL COMMENT '物品编码',
    private int injuryinterval;// int(11) NOT NULL COMMENT '伤害间隔时间',
    private int injurydelaytime;// int(11) NOT NULL COMMENT '伤害延迟时间',
    private int skillLX;// int(11) NOT NULL COMMENT '技能范围左x轴',
    private int skillLY;// int(11) NOT NULL COMMENT '技能范围左Y轴',
    private int skillRX;// int(11) NOT NULL COMMENT '技能范围右x轴',
    private int skillRY;// int(11) NOT NULL COMMENT '技能范围右Y轴',
    private int damagedestructiontime;// int(11) NOT NULL COMMENT '伤害销毁时间',
    private String combinatorialCodingId;// varchar(100) NOT NULL COMMENT '组合编码',
    private String skill_name;// varchar(50) DEFAULT NULL COMMENT '技能名称',
    private int skill_leve;// tinyint(4) DEFAULT NULL COMMENT '技能等级',
    private int range;// tinyint(4) DEFAULT NULL COMMENT '技能范围',
    private int skillCount;
    private int stackingDistance;
    private double harm;// int(11) NOT NULL COMMENT '技能伤害',

    public double getHarm() {
        return harm;
    }

    public void setHarm(double harm) {
        this.harm = harm;
    }
    public int getSkillCount() {
        return skillCount;
    }

    public void setSkillCount(int skillCount) {
        this.skillCount = skillCount;
    }

    public int getStackingDistance() {
        return stackingDistance;
    }

    public void setStackingDistance(int stackingDistance) {
        this.stackingDistance = stackingDistance;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCharacterid() {
        return characterid;
    }

    public void setCharacterid(int characterid) {
        this.characterid = characterid;
    }

    public int getSkillid() {
        return skillid;
    }

    public void setSkillid(int skillid) {
        this.skillid = skillid;
    }

    public int getItemid() {
        return itemid;
    }

    public void setItemid(int itemid) {
        this.itemid = itemid;
    }

    public int getInjuryinterval() {
        return injuryinterval;
    }

    public void setInjuryinterval(int injuryinterval) {
        this.injuryinterval = injuryinterval;
    }

    public int getInjurydelaytime() {
        return injurydelaytime;
    }

    public void setInjurydelaytime(int injurydelaytime) {
        this.injurydelaytime = injurydelaytime;
    }

    public int getSkillLX() {
        return skillLX;
    }

    public void setSkillLX(int skillLX) {
        this.skillLX = skillLX;
    }

    public int getSkillLY() {
        return skillLY;
    }

    public void setSkillLY(int skillLY) {
        this.skillLY = skillLY;
    }

    public int getSkillRX() {
        return skillRX;
    }

    public void setSkillRX(int skillRX) {
        this.skillRX = skillRX;
    }

    public int getSkillRY() {
        return skillRY;
    }

    public void setSkillRY(int skillRY) {
        this.skillRY = skillRY;
    }

    public int getDamagedestructiontime() {
        return damagedestructiontime;
    }

    public void setDamagedestructiontime(int damagedestructiontime) {
        this.damagedestructiontime = damagedestructiontime;
    }

    public String getCombinatorialCodingId() {
        return combinatorialCodingId;
    }

    public void setCombinatorialCodingId(String combinatorialCodingId) {
        this.combinatorialCodingId = combinatorialCodingId;
    }

    public String getSkill_name() {
        return skill_name;
    }

    public void setSkill_name(String skill_name) {
        this.skill_name = skill_name;
    }

    public int getSkill_leve() {
        return skill_leve;
    }

    public void setSkill_leve(int skill_leve) {
        this.skill_leve = skill_leve;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
