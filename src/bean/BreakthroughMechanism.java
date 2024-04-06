package bean;

public class BreakthroughMechanism {
         private  long id;//                    bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
         private  int characterid;//                    int(11) NOT NULL DEFAULT '0' COMMENT '角色id',
         private  String name;//                    varchar(64) NOT NULL COMMENT '境界名称',
         private  String equalOrder;//                    text NOT NULL COMMENT '境界阶段',
         private  int localstr;//                    int(11) DEFAULT '0' COMMENT '力量',
         private  int localdex;//                    int(11) DEFAULT '0' COMMENT '敏捷',
         private  int localluk;//                    int(11) DEFAULT '0' COMMENT '运气',
         private  int localint;//                    int(11) DEFAULT '0' COMMENT '智力',
         private  int allQuality;//                    int(11) DEFAULT '0' COMMENT '全属性',
         private  int harm;//                    int(11) DEFAULT '0' COMMENT '伤害',
         private  int crit;//                    int(11) DEFAULT '0' COMMENT '暴击率',
         private  int critHarm;//                    int(11) DEFAULT '0' COMMENT '暴击伤害',
         private  int bossHarm;//                    int(11) DEFAULT '0' COMMENT 'BOSS伤害',
         private  int hp;//                    int(11) DEFAULT '0' COMMENT 'hp',
         private  int mp;//                    int(11) DEFAULT '0' COMMENT 'mp',
         private  int matk;//                    int(11) DEFAULT '0' COMMENT '魔攻',
         private  int pad;//                    int(11) DEFAULT '0' COMMENT '物攻',
         private  int customizeAttribute;//                    int(11) DEFAULT '0' COMMENT '自定义属性机会',
         private  int customizeSmashRoll;//                    int(11) DEFAULT '0' COMMENT '重置砸卷次数机会'

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEqualOrder() {
        return equalOrder;
    }

    public void setEqualOrder(String equalOrder) {
        this.equalOrder = equalOrder;
    }

    public int getLocalstr() {
        return localstr;
    }

    public void setLocalstr(int localstr) {
        this.localstr = localstr;
    }

    public int getLocaldex() {
        return localdex;
    }

    public void setLocaldex(int localdex) {
        this.localdex = localdex;
    }

    public int getLocalluk() {
        return localluk;
    }

    public void setLocalluk(int localluk) {
        this.localluk = localluk;
    }

    public int getLocalint() {
        return localint;
    }

    public void setLocalint(int localint) {
        this.localint = localint;
    }

    public int getAllQuality() {
        return allQuality;
    }

    public void setAllQuality(int allQuality) {
        this.allQuality = allQuality;
    }

    public int getHarm() {
        return harm;
    }

    public void setHarm(int harm) {
        this.harm = harm;
    }

    public int getCrit() {
        return crit;
    }

    public void setCrit(int crit) {
        this.crit = crit;
    }

    public int getCritHarm() {
        return critHarm;
    }

    public void setCritHarm(int critHarm) {
        this.critHarm = critHarm;
    }

    public int getBossHarm() {
        return bossHarm;
    }

    public void setBossHarm(int bossHarm) {
        this.bossHarm = bossHarm;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }

    public int getMatk() {
        return matk;
    }

    public void setMatk(int matk) {
        this.matk = matk;
    }

    public int getPad() {
        return pad;
    }

    public void setPad(int pad) {
        this.pad = pad;
    }

    public int getCustomizeAttribute() {
        return customizeAttribute;
    }

    public void setCustomizeAttribute(int customizeAttribute) {
        this.customizeAttribute = customizeAttribute;
    }

    public int getCustomizeSmashRoll() {
        return customizeSmashRoll;
    }

    public void setCustomizeSmashRoll(int customizeSmashRoll) {
        this.customizeSmashRoll = customizeSmashRoll;
    }
}
