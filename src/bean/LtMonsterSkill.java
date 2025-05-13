package bean;

import java.io.Serializable;

public class LtMonsterSkill implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

   //= "INT(11) DEFAULT '0' COMMENT '技能类型1 增益  2控制  3伤害'")
    private Integer type;

    private Integer monsterId;

    private Integer skillId;

    private Integer level;

    //'攻击编号'")
    private Integer attackId;
    private Long skillcd;
    //是否致命攻击
    private Boolean isDeadlyAttack;

    public Boolean getDeadlyAttack() {
        return isDeadlyAttack;
    }

    public void setDeadlyAttack(Boolean deadlyAttack) {
        isDeadlyAttack = deadlyAttack;
    }

    public Long getSkillcd() {
        return skillcd;
    }

    public void setSkillcd(Long skillcd) {
        this.skillcd = skillcd;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(Integer monsterId) {
        this.monsterId = monsterId;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getAttackId() {
        return attackId;
    }

    public void setAttackId(Integer attackId) {
        this.attackId = attackId;
    }

    @Override
    public String toString() {
        return "LtMonsterSkill{" +
                "id=" + id +
                ", type=" + type +
                ", monsterId=" + monsterId +
                ", skillId=" + skillId +
                ", level=" + level +
                ", attackId=" + attackId +
                '}';
    }
}

