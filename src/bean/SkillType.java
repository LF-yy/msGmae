package bean;

public class SkillType
{
    private int skillid;
    private int mobcount;
    private int attackcount;
    
    public SkillType(final int 技能id, final int 怪物个数, final int 技能段数) {
        this.skillid = 技能id;
        this.mobcount = 怪物个数;
        this.attackcount = 技能段数;
    }
    
    public int getSkillId() {
        return this.skillid;
    }
    
    public void setSkillId(final int skillid) {
        this.skillid = skillid;
    }
    
    public int getMobCount() {
        return this.mobcount;
    }
    
    public void setMobCount(final int mobcount) {
        this.mobcount = mobcount;
    }
    
    public int getAttackCount() {
        return this.attackcount;
    }
    
    public void setAttackCount(final int attackcount) {
        this.attackcount = attackcount;
    }
}
