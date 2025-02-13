package bean;

import java.io.Serializable;

public class LtCopyMapMonster implements Serializable {
    private Long id;

    private Integer typeId;
    private Integer isBoss;

    private String monsterName;

    private Integer monsterId;

    private Long monsterHp;

    private Integer monsterX;

    private Integer monsterY;

    private Integer monsterCount;

    public LtCopyMapMonster(Long id, Integer typeId,Integer isBoss, String monsterName, Integer monsterId, Long monsterHp, Integer monsterX, Integer monsterY, Integer monsterCount) {
        this.id = id;
        this.typeId = typeId;
        this.isBoss = isBoss;
        this.monsterName = monsterName;
        this.monsterId = monsterId;
        this.monsterHp = monsterHp;
        this.monsterX = monsterX;
        this.monsterY = monsterY;
        this.monsterCount = monsterCount;
    }

    public Integer getIsBoss() {
        return isBoss;
    }

    public void setIsBoss(Integer isBoss) {
        this.isBoss = isBoss;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public String getMonsterName() {
        return monsterName;
    }

    public void setMonsterName(String monsterName) {
        this.monsterName = monsterName;
    }

    public Integer getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(Integer monsterId) {
        this.monsterId = monsterId;
    }

    public Long getMonsterHp() {
        return monsterHp;
    }

    public void setMonsterHp(Long monsterHp) {
        this.monsterHp = monsterHp;
    }

    public Integer getMonsterX() {
        return monsterX;
    }

    public void setMonsterX(Integer monsterX) {
        this.monsterX = monsterX;
    }

    public Integer getMonsterY() {
        return monsterY;
    }

    public void setMonsterY(Integer monsterY) {
        this.monsterY = monsterY;
    }

    public Integer getMonsterCount() {
        return monsterCount;
    }

    public void setMonsterCount(Integer monsterCount) {
        this.monsterCount = monsterCount;
    }
}
