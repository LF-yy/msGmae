package bean;

public class ASkill {
    private long id ;
    private int skillId ;
    private String name ;
    private byte maxLevel ;
    private int ltX  ;
    private int ltY ;
    private int rbX  ;
    private int rbY  ;
    private int range  ;
    private double damage  ;
    private int duration  ;
    private int attackCount  ;
    private int mobCount  ;

    public ASkill(int skillId, String name, byte maxLevel, int ltX, int ltY, int rbX, int rbY, int range, double damage, int duration, int attackCount, int mobCount) {
        this.skillId = skillId;
        this.name = name;
        this.maxLevel = maxLevel;
        this.ltX = ltX;
        this.ltY = ltY;
        this.rbX = rbX;
        this.rbY = rbY;
        this.range = range;
        this.damage = damage;
        this.duration = duration;
        this.attackCount = attackCount;
        this.mobCount = mobCount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(byte maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getLtX() {
        return ltX;
    }

    public void setLtX(int ltX) {
        this.ltX = ltX;
    }

    public int getLtY() {
        return ltY;
    }

    public void setLtY(int ltY) {
        this.ltY = ltY;
    }

    public int getRbX() {
        return rbX;
    }

    public void setRbX(int rbX) {
        this.rbX = rbX;
    }

    public int getRbY() {
        return rbY;
    }

    public void setRbY(int rbY) {
        this.rbY = rbY;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public void setAttackCount(int attackCount) {
        this.attackCount = attackCount;
    }

    public int getMobCount() {
        return mobCount;
    }

    public void setMobCount(int mobCount) {
        this.mobCount = mobCount;
    }

}
